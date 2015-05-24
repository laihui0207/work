#include<stdio.h>
#include<stdlib.h>
#include<fcntl.h>
#include<errno.h>
#include<unistd.h>
#include<sys/ioctl.h>
#include<jni.h>  // 一定要包含此文件
#include<string.h>
#include<sys/types.h>
#include<sys/stat.h>
#include "android/log.h"

//驱动里的命令码.
//#define CMD_FLAG 'i'
//#define SET_LED_ON			_IOR(CMD_FLAG,0x00000000,__u32)
//#define SET_LED_OFF			_IOR(CMD_FLAG,0x00000001,__u32)
//#define GET_ACC_STATUS		_IOR(CMD_FLAG,0x00000002,__u32)

#define DEVICE_NAME "/dev/carbox"

static const char *TAG="CARBOX";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

int fd = -1;
/* * Class:     Linuxc 
* Method:    openled
* Signature: ()I 
*/
JNIEXPORT jint
JNICALL Java_com_tuyou_tsd_common_util_TsdHelper_openport(JNIEnv* env, jobject mc ,jstring file)
{
	char fileName[64];

	const jbyte *str;

	str = (*env)->GetStringUTFChars(env, file, NULL);

	if (str == NULL) {

		LOGI("Can't get file name: %s", file);
		return -1;

	}

	sprintf(fileName, "%s", str);

	LOGD("will open device node %s", fileName);

	(*env)->ReleaseStringUTFChars(env, file, str);

	return open(fileName, O_RDWR);
}

/* * Class:     Linuxc 
* Method:    clsoeled
* Signature: ()V
*/
JNIEXPORT void
JNICALL Java_com_tuyou_tsd_common_util_TsdHelper_closeport(JNIEnv* env, jobject mc, int fd)
{
	LOGD("dev close");
	close(fd);	
}

JNIEXPORT jint
JNICALL Java_com_tuyou_tsd_common_util_TsdHelper_ioctlport(JNIEnv* env,jobject mc, int fd, jint a, jint b)
{
	LOGD("dev ioctl");
	ioctl(fd,a,NULL);

	return 0;	
}

JNIEXPORT jint
Java_com_tuyou_tsd_common_util_TsdHelper_readport(JNIEnv* env, jobject thiz,int fd,jbyteArray buf,jint size)
{
    unsigned char *buf_char = (char*)((*env)->GetByteArrayElements(env,buf, NULL));
    read(fd, buf_char,  size);

    int result = buf_char != NULL ? buf_char[0] : -1;

    (*env)->ReleaseByteArrayElements(env, buf, buf_char, JNI_ABORT);

    LOGD("read from port result: %d", result);
    return result;
}

JNIEXPORT jint
Java_com_tuyou_tsd_common_util_TsdHelper_writeport(JNIEnv* env, jobject thiz,int fd,jbyteArray buf,jint size)
{
    unsigned char *buf_char = (char*)((*env)->GetByteArrayElements(env,buf, NULL));
    int result = write(fd, buf_char,  size);

    (*env)->ReleaseByteArrayElements(env, buf, buf_char, JNI_ABORT);

    LOGD("write to port result: %d", result);
    return result;
}

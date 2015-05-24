package com.tuyou.tsd.common;

import android.os.Environment;

public interface TSDConst {
	static final boolean buildForDevice = true; // true -- for Device; false -- for Cell phone.
	static final boolean useGPRSNetwork = true; // true -- GPRS; false -- WIFI

	static final String SERVER_HOST = "121.201.13.32";
	static final int MQTT_PORT = 1883;
	static final String CLIENT_TAG = "tsd";

	// 资源文件路径
	static final String TUYOU_ROOT_PATH = Environment.getExternalStorageDirectory() + "/com.tuyou";
	static final String LOG_FILE_PATH = TUYOU_ROOT_PATH + "/log";

	// 语音助手资源文件
	static final String VOICE_ASSISTANT_PATH = TUYOU_ROOT_PATH + "/voice";

	// 行车记录录像保存位置
	static final String CAR_ROOT_PATH = "/storage/sdcard1/";
	static final String CAR_DVR_TOOT_PATH = CAR_ROOT_PATH+"com.tuyou" ;
	static final String CAR_DVR_PATH = CAR_ROOT_PATH+"com.tuyou" + "/cardvr";
	static final String CAR_DVR_VIDEO_PATH = CAR_DVR_PATH + "/videos";
    static final String CAR_DVR_ACCIDENT_VIDEO_PATH = CAR_DVR_PATH + "/accidents";
    static final String CAR_DVR_ALERT_VIDEO_PATH = CAR_DVR_PATH + "/alerts";
    static final String CAR_DVR_PICTURES_PATH = CAR_DVR_PATH + "/pictures";

    // FTP server
	static final String FTP_CONFIG_PATH = TUYOU_ROOT_PATH + "/ftpConfig";    

	static final String CAR_DVR_START_CAM_PREVIEW_RES = "res";
	
	static final String UPLOAD_LOC_LAT = "latitude";
	static final String UPLOAD_LOC_LNG = "longitude";
	static final String UPLOAD_TIME_STAMP = "timestamp";

	static final String ACC_STATE = "acc_state";
	//更新文件路径
	static final String APK_PATH = "/TuYouDownload/";
	static final String VERSION_PATH = "Version/";
	
	static final int PLAY_SPACE = 500;
}

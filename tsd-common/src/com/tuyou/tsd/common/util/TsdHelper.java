package com.tuyou.tsd.common.util;

import android.util.Log;

public class TsdHelper {

	public static String CARBOX_DEV = "/dev/carbox";  //IO设备
	public static String FM_DEV = "/dev/fm-i2c8";  //FM频率设置和读取设备
	
	public static int SET_LED_ON 	= 0x00000000;
	public static int SET_LED_OFF 	= 0x00000001;
	public static int GET_ACC_STATUS = 0x00000002;
	public static int SET_RELAY_ON = 0x00000003;
	public static int SET_RELAY_OFF = 0x00000004;
	public static int SET_FM_ON 	= 0x00001000;
	public static int SET_FM_OFF 	= 0x00001001;
	
	public static int  SET_LED_ON_1		= 0x00003001;
	public static int  SET_LED_ON_2		= 0x00003002; 
	public static int  SET_LED_ON_3		= 0x00003003; 
	public static int  SET_LED_ON_4		= 0x00003004; 
	public static int  SET_LED_OFF_1		= 0x00003005;
	public static int  SET_LED_OFF_2		= 0x00003006;
	public static int  SET_LED_OFF_3		= 0x00003007;
	public static int  SET_LED_OFF_4		= 0x00003008;
	private static int  SET_LCD_ON 		= 0x00000005;
	private static int  SET_LCD_OFF 	= 0x00000006;
	
	static {
		try{
			Log.d("JNI","try to load libtsddriver.so");
			System.loadLibrary("tsddriver");  
		}catch (UnsatisfiedLinkError ule){
			Log.e("JNI","WARNING: Could not load libtsddriver.so");
		}
	}
	
	public static native int openport(String device);  
	public static native void closeport(int fd);
	public static native int ioctlport(int fd, int type);
	public static native int readport(int fd,  int buf[],int size);
	public static native int writeport(int fd, int  buf[],int size);
	
	/**
	 *打开按键灯
	 *
	 ***/
	public static void  setkeyLedOn(int key)
	{
		int fd_carbx = 0;
		fd_carbx =openport(CARBOX_DEV); 
		int type=SET_LED_ON_1;
		switch (key) {
		case 1:
			type = SET_LED_ON_1;
			break;
		case 2:
			type = SET_LED_ON_2;
			break;
		case 3:
			type = SET_LED_ON_3;
			break;
		case 4:
			type = SET_LED_ON_4;
			break;
		default:
			break;
		}
		Log.v("CARAPI:","onClick led_on");
		if(fd_carbx>0)
			ioctlport(fd_carbx, type); 
		if(fd_carbx>0)
			closeport(fd_carbx);
	}
	/**
	 *关闭按键灯
	 *
	 ***/
	public static void  setkeyLedOff(int key)
	{
		int fd_carbx = 0;
		fd_carbx =openport(CARBOX_DEV); 
		Log.v("CARAPI:","onClick led1_on");
		int type=SET_LED_OFF_1;
		switch (key) {
		case 1:
			type = SET_LED_OFF_1;
			break;
		case 2:
			type = SET_LED_OFF_2;
			break;
		case 3:
			type = SET_LED_OFF_3;
			break;
		case 4:
			type = SET_LED_OFF_4;
			break;
		default:
			break;
		}
		if(fd_carbx>0)
			ioctlport(fd_carbx, type); 
		if(fd_carbx>0)
			closeport(fd_carbx);
		
	}
	/**
	 *读取ACC状态
	 * ACC上电返回TRUE，否则返回FALSE
	 ***/
	public static boolean getAccStatus()
	{
		boolean result=false;
		int fd_carbx = 0;
		fd_carbx =openport(CARBOX_DEV); 
		
		int acc[] = {GET_ACC_STATUS};
		if(fd_carbx>0)
		{
			readport(fd_carbx, acc, 1);
			if(acc[0]>0)
			{
				result=true;
			}
		}
		Log.v("CARAPI:","acc: " + acc[0]);
		
		if(fd_carbx>0)
			closeport(fd_carbx);
		return result;
	}

	/**
	 *启动FM
	 *
	 ***/
	public static void  setFMOn()
	{
		int fm_port = openport(FM_DEV);
		if (fm_port > 0) {
			ioctlport(fm_port, SET_FM_ON);
			closeport(fm_port);
		}
		Log.d("CARAPI:", "set FM on");
	}
	/**
	 *关闭FM
	 *
	 ***/
	public static void  setFMOff()
	{
		int fm_port = openport(FM_DEV);
		if (fm_port > 0) {
			ioctlport(fm_port, SET_FM_OFF);
			closeport(fm_port);
		}
		Log.d("CARAPI:", "set FM off");
	}
	/**
	 *设置FM频率
	 *参数为整形频率值，注意参数为实际频率*100，比如101.7则设置参数为10170
	 ***/
	public static void setFMFreq(int freq)
	{
		int fm_port = openport(FM_DEV);
		if (fm_port > 0) {
			int channel[] = {freq}; //7600-10800 76 MHz ~ 108 MHz
			writeport(fm_port, channel, 1);
			closeport(fm_port);
		}
		Log.d("CARAPI:", "set FM freq to " + freq);
	}
	
	/**
	 *读取FM频率
	 *返回值为整形频率值，注意返回值为实际频率*100，比如101.7则设置返回值为10170
	 ***/
	public static int getFMFreq()
	{
		int fd_fm = openport(FM_DEV);
		int channel[] = {0};

		if (fd_fm > 0) {
			readport(fd_fm, channel, 1);
			closeport(fd_fm);		
		}
		
		Log.d("CARAPI:","got FM freq " + channel[0]);		
		return channel[0];
	}
	
	/**
	 *启动油路控制
	 *
	 ***/
	public static void  setRelayOn()
	{
		int fd_carbx = 0;
		fd_carbx =openport(CARBOX_DEV); 
		if(fd_carbx>0)
			ioctlport(fd_carbx, SET_RELAY_ON); 
		if(fd_carbx>0)
			closeport(fd_carbx);
	}
	
	/**
	 *关闭油路控制
	 *
	 ***/
	public static void  setRelayOff()
	{
		int fd_carbx = 0;
		fd_carbx =openport(CARBOX_DEV); 
		if(fd_carbx>0)
			ioctlport(fd_carbx, SET_RELAY_OFF); 
		if(fd_carbx>0)
			closeport(fd_carbx);
	}

	public static void setLCDOn() {
		int fd = openport(CARBOX_DEV);
		if (fd > 0) {
			ioctlport(fd, SET_LCD_ON);
			closeport(fd);
		}
	}

	public static void setLCDOff() {
		int fd = openport(CARBOX_DEV);
		if (fd > 0) {
			ioctlport(fd, SET_LCD_OFF);
			closeport(fd);
		}
	}
}
package com.tuyou.tsd.common.network;


public class GetDeviceInfoRes {    
	public String id;
	public String imei;
	public String radioFM;
	public String plateNumber;
	public Boolean tachographSwitch;
	public Boolean parkProtetion;
	public Boolean voiceWithVideo;
	
	public static class Power {
		public int  blankScreenIdleTime;
		public int  blankScreenSpeedLimit;
		public Boolean shutdownWhenDebus;
	};
	public Power power;
	public static class WifiConfig {
		public String  ssid;
		public String  security;
		public String password;
	};
	public WifiConfig wifi;
};

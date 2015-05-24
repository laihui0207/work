package com.tuyou.tsd.common.network;


public class LoginRes extends BaseRes{
	public static class Status {
		public int code;
	};
	public Status status;	
	public String accessToken;
	public String refreshToken;
	public String lastAuthc;
	public int expires;
	public static class Device {
		public String id;
	};
	public Device device;
	public String messages;
};

package com.tuyou.tsd.common.network;

public class SubmitDeviceStatusRes extends BaseRes {
	public static class Status {
		public int code;
	};
	public Status status;	
	public String message;
};

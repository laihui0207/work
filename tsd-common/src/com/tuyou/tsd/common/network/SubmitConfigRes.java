package com.tuyou.tsd.common.network;


public class SubmitConfigRes extends BaseRes{
	public static class Status {
		public int code;
		public String[] errors;
	};
	public Status status;	
	
};

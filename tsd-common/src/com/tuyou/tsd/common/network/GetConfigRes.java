package com.tuyou.tsd.common.network;

public class GetConfigRes extends BaseRes{
	public Status status;	
	public Config[] configs;

	public static class Status {
		public int code;
	};
	public static class Config {
		public String name;
		public Object content;
	};
};

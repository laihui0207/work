package com.tuyou.tsd.common.network;


public class GetConfigRes {
	public static class Status {
		public int code;
		public String[] errors;
	};
	public Status status;	
	public String id;
	public static class Config {
		public String name;
		public String content;
	};
	public Config[] configs;
};

package com.tuyou.tsd.common.network;


public class GetUpdateInfoRes extends BaseRes {	
	public Status status;	
	public static class Status {
		public int code;
	};
	public static class ApkInfo {
		public String  id;//系统自动生成	
		public String  type;
		public String  name;
		public String  description;
		public String  version;
		public String[]  notes;
		public String  timestamp;
		public boolean force;
		public AppInfo file;
		public ApkInfo[] apps;
	};
	public ApkInfo apk;
	
};


package com.tuyou.tsd.common.network;
//Get the upload token (qiniu) response  
public class GetUploadTokenRes extends BaseRes{
	public Status status;
	public Config[] configs;
	
	public static class Status {
		public int code;
	};
	
	public static class Config {
		public String name;
		public Content content;
		public static class Content {
			public String cardvr;
			public String mblog;
			public String cardvr_thumbnail;
			public String cardvr_video;
		}
	};
}
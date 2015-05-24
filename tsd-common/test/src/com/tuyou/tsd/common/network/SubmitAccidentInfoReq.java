package com.tuyou.tsd.common.network;

public class SubmitAccidentInfoReq {
	public String sender;
	public String timestamp;
	public int timeout;
	public static class MessageBody {
		public String module;
		public String type;
		public String url;
		public static class ContentBody {
			public String videoId;
			public String timestamp;
			public int lat;
			public int lng;
			public String district;
			public String address;
			public int expired;
		};
		public ContentBody content;
	};
	public MessageBody message;
};

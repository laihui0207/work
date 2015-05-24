package com.tuyou.tsd.common.network;

public class PushMessageInd {
	public String sender;
	public String timestamp;
	public int timeout;
	public static class MessageBody {
		public String module;
		public String type;
		public String url;
		public String title;
		public String content;
	};
	public MessageBody message;
};

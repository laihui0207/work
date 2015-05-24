package com.tuyou.tsd.common.network;


public class GetAudioCategoryListRes extends BaseRes{
	public static class Status {
		public int code;
	};
	public Status status;	
	public AudioCategory[] categories;
};

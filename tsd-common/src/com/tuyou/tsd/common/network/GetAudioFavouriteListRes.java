package com.tuyou.tsd.common.network;


public class GetAudioFavouriteListRes extends BaseRes{
	public static class Status {
		public int code;
	};
	public Status status;	
	public AudioItem[] items ;
};

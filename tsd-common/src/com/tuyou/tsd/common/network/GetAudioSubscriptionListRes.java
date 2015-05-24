package com.tuyou.tsd.common.network;


public class GetAudioSubscriptionListRes extends BaseRes{
	public static class Status {
		public int code;
	};
	public Status status;	
	public AudioSubscription[] albums ;
};

package com.tuyou.tsd.common.videoMeta;

public class VideoStatInf {
	public VIDEO_TYPE mVideoType;
	public int mVideoCount;
	public int mUnreadCount; //Only used for accident currently.
	
	public enum VIDEO_TYPE {
		NORMAL, ACCIDENT, FAVORITE,
	}
	
	public VideoStatInf(VIDEO_TYPE videoType, int videoCount, int unreadCount) {
		mVideoType = videoType;
		mVideoCount = videoCount;
		mUnreadCount = unreadCount;
	}
}

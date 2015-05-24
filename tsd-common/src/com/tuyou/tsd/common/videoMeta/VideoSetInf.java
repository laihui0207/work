package com.tuyou.tsd.common.videoMeta;

import java.util.ArrayList;

public class VideoSetInf {
	public long mStartTime;
	public ArrayList<VideoInf> mVideoList = new ArrayList<VideoInf>();
	
	public void  addVideo(VideoInf videoInf) {
		mVideoList.add(videoInf);
	}
}

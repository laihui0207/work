package com.tuyou.tsd.cardvr.utils;

import java.util.Comparator;

import com.tuyou.tsd.cardvr.bean.VideoInfo;

public class VideoInfoComparator implements Comparator<VideoInfo>{

	@Override
	public int compare(VideoInfo lhs, VideoInfo rhs) {
		return lhs.getTime().compareTo(rhs.getTime());
	}

}

package com.tuyou.tsd.audio.utils;

import java.util.Comparator;

import com.tuyou.tsd.common.network.AudioCategory;

public class OrderUtils implements Comparator<AudioCategory>{

	@Override
	public int compare(AudioCategory item1, AudioCategory item2) {
		int order1 = item1.order;
		int order2 = item2.order;
		if(order1>order2){
			return 1;
		}
		return -1;
	}
}

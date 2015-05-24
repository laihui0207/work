package com.tuyou.tsd.podcast.utils;

import java.util.Comparator;

import com.tuyou.tsd.common.network.AudioSubscription;


public class OrderUtils implements Comparator<AudioSubscription>{

	@Override
	public int compare(AudioSubscription item1, AudioSubscription item2) {
		int order1 = item1.status;
		int order2 = item2.status;
		if(order1>order2){
			return -1;
		}else if(order1==order2){
			return 0;
		}
		return 1;
	}
}

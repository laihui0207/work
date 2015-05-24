package com.tuyou.tsd.podcast.db;

import com.tuyou.tsd.common.network.AudioSubscription;

public class SubscriptionItemEntity {
	private long   id = -1;  //数据库记录标识
	private AudioSubscription detail;
	
	public SubscriptionItemEntity() 
	{
		super();
		this.id = -1;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public AudioSubscription getDetail() {
		return detail;
	}

	public void setDetail(AudioSubscription detail) {
		this.detail = detail;
	}

}

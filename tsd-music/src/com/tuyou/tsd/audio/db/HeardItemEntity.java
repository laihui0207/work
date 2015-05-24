package com.tuyou.tsd.audio.db;

import com.tuyou.tsd.common.network.AudioItem;

public class HeardItemEntity {
	private long   id = -1;  //数据库记录标识
	private AudioItem detail;
	
	public HeardItemEntity() 
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

	public AudioItem getDetail() {
		return detail;
	}

	public void setDetail(AudioItem detail) {
		this.detail = detail;
	}

}

package com.tuyou.tsd.audio.db;

import com.tuyou.tsd.common.network.AudioCategory;

public class AudioItemEntity {
	private long   id = -1;  //数据库记录标识
	private AudioCategory detail;
	
	public AudioItemEntity() 
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

	public AudioCategory getDetail() {
		return detail;
	}

	public void setDetail(AudioCategory detail) {
		this.detail = detail;
	}

}

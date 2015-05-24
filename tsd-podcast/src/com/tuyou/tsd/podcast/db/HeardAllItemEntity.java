package com.tuyou.tsd.podcast.db;


public class HeardAllItemEntity {
	private long   id = -1;  //数据库记录标识
	private String detail;
	
	public HeardAllItemEntity() 
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

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

}

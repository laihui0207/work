package com.tuyou.tsd.podcast.db;


public class SubscriptionRecordEntity {
	private long   id = -1;  //数据库记录标识
	private SubscriptionRecord detail;
	
	public SubscriptionRecordEntity() 
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

	public SubscriptionRecord getDetail() {
		return detail;
	}

	public void setDetail(SubscriptionRecord detail) {
		this.detail = detail;
	}

}

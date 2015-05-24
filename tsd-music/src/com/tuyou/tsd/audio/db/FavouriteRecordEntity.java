package com.tuyou.tsd.audio.db;


public class FavouriteRecordEntity {
	private long   id = -1;  //数据库记录标识
	private FavouriteRecord detail;
	
	public FavouriteRecordEntity() 
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

	public FavouriteRecord getDetail() {
		return detail;
	}

	public void setDetail(FavouriteRecord detail) {
		this.detail = detail;
	}

}

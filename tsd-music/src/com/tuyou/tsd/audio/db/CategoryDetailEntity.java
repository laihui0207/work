package com.tuyou.tsd.audio.db;


public class CategoryDetailEntity {
	private long   id = -1;  //数据库记录标识
	private CategoryDetail detail;
	
	public CategoryDetailEntity() 
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

	public CategoryDetail getDetail() {
		return detail;
	}

	public void setDetail(CategoryDetail detail) {
		this.detail = detail;
	}

}

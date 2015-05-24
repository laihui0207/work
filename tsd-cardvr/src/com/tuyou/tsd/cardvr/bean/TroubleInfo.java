package com.tuyou.tsd.cardvr.bean;

import java.io.Serializable;

public class TroubleInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4184076762664999392L;
	private String time;//时间
	private String path;//路径
	private String address;//路径
	private String district;
	private String name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	
	
}

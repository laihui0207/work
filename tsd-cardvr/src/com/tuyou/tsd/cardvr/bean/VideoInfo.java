package com.tuyou.tsd.cardvr.bean;

import java.io.Serializable;
import java.util.ArrayList;


import android.graphics.Bitmap;

public class VideoInfo implements Serializable{
	
	private static final long serialVersionUID = 7457477222562984652L;
	private String	name;
	private String	size;
	private String  path;
	private String type;
	private String [] specials;
	private Bitmap 	thumbnail;
	private String address;
	private String startTime;
	private String time;
	private int dur;
	
	public int getDur() {
		return dur;
	}

	public void setDur(int dur) {
		this.dur = dur;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	private Boolean read;
	
	public Boolean getRead() {
		return read;
	}

	public void setRead(Boolean read) {
		this.read = read;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String[] getSpecials() {
		return specials;
	}

	public void setSpecials(String[] specials) {
		this.specials = specials;
	}

	private ArrayList<TroubleInfo> list;
	
	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public ArrayList<TroubleInfo> getList() {
		return list;
	}

	public void setList(ArrayList<TroubleInfo> list) {
		this.list = list;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public VideoInfo() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Bitmap getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(Bitmap thumbnail) {
		this.thumbnail = thumbnail;
	}

//	public Bitmap getType() {
//		return type;
//	}
//
//	public void setType(Bitmap type) {
//		this.type = type;
//	}
	
}

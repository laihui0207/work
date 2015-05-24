package com.tuyou.tsd.common;

import android.net.Uri;

public class UploadInf {
	private Uri mUri;
	private String mLng;
	private String mLat;
	private String mDeviceId;
	private String mTimestamp;
	private String mType;
	private String mName;
	private String mSrcType;
	private String mDistrict;
	private String mAddress;
	
	
	public UploadInf(Uri uri, String deviceId, String timestamp, String lng, String lat,String type,String name,String srcType,
			String district,String address) {
		mUri = uri;
		mLng = lng;
		mLat = lat;
		mDeviceId = deviceId;
		mTimestamp = timestamp;
		mType=type;
		mName=name;
		mSrcType=srcType;
		mDistrict=district;
		mAddress=address;
	}
	
	public void setUri(Uri uri) {
		mUri = uri;
	}
	
	public void setDeviceId(String deviceId) {
		mDeviceId = deviceId;
	}
	
	public void setTimestamp(String timestamp) {
		mTimestamp = timestamp;
	}
	
	public void setLng(String lng) {
		mLng = lng;
	}
	
	public void setLat(String lat) {
		mLat = lat;
	}
	
	public void setType(String type) {
		mType = type;
	}
	
	public void setName(String name) {
		 mName=name;
	}
	public void setSrcType(String srcType) {
		mSrcType=srcType ;
	}
	public void setDistrict(String district) {
		 mDistrict=district;
	}
	public void setAddress(String address) {
		mAddress=address ;
	}
	
	public Uri getUri() {
		return mUri;
	}
	
	public String getDeviceId() {
		return mDeviceId;
	}
	
	public String getTimestamp() {
		return mTimestamp;
	}
	
	public String getLng() {
		return mLng;
	}
	
	public String getLat() {
		return mLat;
	}
	public String getType() {
		return mType ;
	}
	public String getName() {
		return mName;
	}
	public String getSrcType() {
		return mSrcType ;
	}
	public String getDistrict() {
		return mDistrict;
	}
	public String getAddress() {
		return mAddress ;
	}
	
	
}

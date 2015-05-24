package com.tuyou.tsd.common.videoMeta;

public class PictureInf {
	public class Location {
		public String lng;
		public String lat;
	}
	
	public String 	name;
	public Location location = new Location();
	public long timestamp; 
	public String district;
	public String address;
}

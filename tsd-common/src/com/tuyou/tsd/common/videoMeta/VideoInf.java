package com.tuyou.tsd.common.videoMeta;

public class VideoInf {
	public class Location {
		public String lng;
		public String lat;
	}
	
	public final static int UNOPEN_FLAG = 0x01;   //Open flag
	public final static int ACCIDENT_FLAG = 0x02;  //Accident flag
	public final static int FAVORITE_FLAG = 0x04;  //Favorite flag
	
	public String 	name;
	public Location location = new Location();
	public long timestamp; 
	public int tag;  //OPEN_FLAG|ACCIDENT_FLAG|FAVORITE_FLAG
	public String district;
	public String address;
}

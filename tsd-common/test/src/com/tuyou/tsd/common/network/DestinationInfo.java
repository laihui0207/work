package com.tuyou.tsd.common.network;

public class DestinationInfo {
	public String userId;
	public String timestamp;
	public int timeout;
	public static class Point {
		public int lat;
		public int lng;
	};
	public Point point;
	public String name;
	public String address;
	public String telephone;
	public String setby;	
};

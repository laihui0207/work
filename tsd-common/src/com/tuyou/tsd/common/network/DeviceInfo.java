package com.tuyou.tsd.common.network;


public class DeviceInfo {
	public String id;
	public String model;
	public static class Car {
		public String plateNumber;
		public static class Model {
			public String  brand;
			public String  version;
		};
		public Model model;
		};
	public Car car;
};

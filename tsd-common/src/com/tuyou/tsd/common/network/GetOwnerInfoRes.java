package com.tuyou.tsd.common.network;


public class GetOwnerInfoRes extends BaseRes{
	public static class Status {
		public int code;
	};
	public DeviceInfo  device;
	
	public static class Owner {
		public String id;
		public String name;
		public String nickName;
		public String mobile;
		public String email;
		public String gender;
		public String bindingTime;
	};
	public Owner[] owners;
};

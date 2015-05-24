package com.tuyou.tsd.common.network;


public class SubmitDeviceStatusReq {
	public String timestamp;
    public String state;
    public String screen;
    public Interaction interaction;
    public Audio audio;
    public Map map;
    public DVR dvr;
    public Update update;
    public Location location;

//    public String[] modes;
//    public RuntimeBody runtime;

//    public static class RuntimeBody {
//    	public boolean acc;
//    	public boolean dvr;
//    	public boolean alert;
//    }

    public static class Interaction {
    	public String name;
    	public int dialog;
    	public String state;
    	public String result;
    }

    public static class Audio {
    	public int id;
    	public String name;
    	public String type;
    	public String state;
    }

    public static class Map {
    	public String destination;
    }

    public static class DVR {
    	public boolean recording;
    	public boolean alert;
    	public boolean video;
    	public boolean accident;
    }
    
    public static class Location{
    	public String timestamp;
    	public double       lat;
    	public double       lng;
    	public String  district;
    	public String   address;
    	public double mileage;
    }

    public static class Update {
    	public String downloading;
    	public String updating;
    	public String name;
    }

    public SubmitDeviceStatusReq() {
    	interaction = new Interaction();
    	audio = new Audio();
    	map = new Map();
    	dvr = new DVR();
    	update = new Update();
    }
    
    
    
};

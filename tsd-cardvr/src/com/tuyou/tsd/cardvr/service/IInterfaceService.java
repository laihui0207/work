package com.tuyou.tsd.cardvr.service;

import org.json.JSONArray;
import org.json.JSONObject;




public interface IInterfaceService {
	abstract public JSONArray getVideoStats();
	abstract public JSONObject getVideoSets(String type,String startTime,String endTime);
	abstract public JSONArray getVideoSetItems(String startTime);
	abstract public String getVideo(String name);
	abstract public void setFavoriteVideo(String name,boolean isFavourite);
	abstract public void setRead(String name);
	abstract public String getVideoThumbnail(String name);
	abstract public boolean getFavoriteStatus(String name);
	abstract public boolean getIsAccident(String name);
}



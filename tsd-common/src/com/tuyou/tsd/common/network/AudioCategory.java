package com.tuyou.tsd.common.network;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;


public class AudioCategory implements Parcelable{
	public String category;//歌单ID
	public String type;//歌单类型
	public int total;//歌曲条数，目前没有用
	public String name;//歌单名称
	public String description;//歌单描述，目前没有用
	public String  image;//歌单封面ID
	public int order;//歌单序号，
	public int cache;//是否缓存，0，不缓存，1缓存
	public int mode; //模式，一次性或者循环，目前没有用
	public String start;//有效开始时间
	public String end;//有效结束时间
	public int playmode;//没有用了
	
	public ArrayList<AudioItem> item;//歌曲列表
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(category);
		dest.writeString(type);
		dest.writeInt(total);
		dest.writeString(name);
		dest.writeString(description);
		dest.writeString(image);
		
		dest.writeInt(order);
		dest.writeInt(cache);
		dest.writeInt(mode);
		dest.writeString(start);
		dest.writeString(end);
		dest.writeInt(playmode);
		
		dest.writeList(item);
	}
	
	
	
	public static final Parcelable.Creator<AudioCategory> CREATOR = new Creator<AudioCategory>(){  
		@SuppressWarnings("unchecked")
		@Override  
		public AudioCategory createFromParcel(Parcel source) {  
			AudioCategory audio = new AudioCategory();  
			audio.category = source.readString();
			audio.type = source.readString();
			audio.total = source.readInt();
			audio.name = source.readString();
			audio.description = source.readString();
			audio.image = source.readString();
			
			audio.order = source.readInt();
			audio.cache = source.readInt();
			audio.mode = source.readInt();
			audio.start = source.readString();
			audio.end = source.readString();
			audio.playmode = source.readInt();
			audio.item = source.readArrayList(AudioItem.class.getClassLoader());

			return audio;  
		}  
		@Override  
		public AudioCategory[] newArray(int size) {  
			return new AudioCategory[size];  
			}  
		};  
};

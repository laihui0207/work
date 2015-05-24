package com.tuyou.tsd.common.network;

import android.os.Parcel;
import android.os.Parcelable;

//上传audio播放状态
public class AudioState implements Parcelable{
	public String id;//歌单ID 
	public String type;//歌单类型
	public String name;//歌单名称
	public String state;//状态

	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(type);
		dest.writeString(name);
		dest.writeString(state);
		dest.writeString(id);
	}
	
	public static final Parcelable.Creator<AudioState> CREATOR = new Creator<AudioState>(){  
		@Override  
		public AudioState createFromParcel(Parcel source) {  
			AudioState audio = new AudioState();  
			audio.id = source.readString();
			audio.type = source.readString();
			audio.name = source.readString();
			audio.state = source.readString();								
			return audio;  
		}  
		@Override  
		public AudioState[] newArray(int size) {  
			return new AudioState[size];  
			}  
		}; 
};

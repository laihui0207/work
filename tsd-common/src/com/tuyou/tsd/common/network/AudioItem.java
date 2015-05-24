package com.tuyou.tsd.common.network;

import android.os.Parcel;
import android.os.Parcelable;


public class AudioItem implements Parcelable{
	public String item;//歌曲ID
	public String type;//歌曲类型
	public String name;//歌曲名称
	public String album;//歌曲专辑名称
	public String albumId;//歌曲专辑ID
	public String author;//歌曲作者
	public String description;//歌曲描述
	public int size;//歌曲大小，现在没有用
	public int duration;//播放时间
	public String  url;//歌曲URL
	public String  checksum;//歌曲校验
	public String icon;//专辑封面图片
	public String timestamp;
	
	//是否正在播放 (Local use only)
	public boolean isPlay;
	public boolean isOffLine;
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(item);
		dest.writeString(type);
		dest.writeString(name);
		dest.writeString(album);
		dest.writeString(albumId);
		dest.writeString(author);
		dest.writeString(description);
		dest.writeInt(size);
		dest.writeInt(duration);
		dest.writeString(url);
		dest.writeString(checksum);
		dest.writeString(icon);
		dest.writeString(timestamp);
		
		dest.writeByte((byte)(isPlay ?1:0));
		dest.writeByte((byte)(isOffLine ?1:0));
		
	}
	
	public static final Parcelable.Creator<AudioItem> CREATOR = new Creator<AudioItem>(){  
		@Override  
		public AudioItem createFromParcel(Parcel source) {  
			AudioItem audio = new AudioItem();  
			audio.item = source.readString();
			audio.type = source.readString();
			audio.name = source.readString();
			audio.album = source.readString();
			audio.albumId = source.readString();
			audio.author = source.readString();
			audio.description = source.readString();
			audio.size = source.readInt();
			audio.duration = source.readInt();
			audio.url = source.readString();
			audio.checksum = source.readString();
			audio.icon = source.readString();
			audio.timestamp = source.readString();
			
			if(source.readByte()!=0){
				audio.isPlay = true;
			}
			
			if(source.readByte()!=0){
				audio.isOffLine = true;
			}
			return audio;  
		}  
		@Override  
		public AudioItem[] newArray(int size) {  
			return new AudioItem[size];  
			}  
		}; 
};

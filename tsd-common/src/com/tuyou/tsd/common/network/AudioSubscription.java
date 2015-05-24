package com.tuyou.tsd.common.network;

import android.os.Parcel;
import android.os.Parcelable;


public class AudioSubscription implements Parcelable {
	public String type;//专辑类型
	public String name;//专辑名称
	public String album;//专辑ID
	public String coverUrl;//专辑封面URL
	public int status;//专辑状态 0为未订阅，1为订阅 (此字段仅在本地使用，后面优化代码时考虑去掉该字段)
//	public boolean isSub;
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(type);
		dest.writeString(name);
		dest.writeString(album);
		dest.writeString(coverUrl);
//		dest.writeByte((byte)(isSub ?1:0));
		dest.writeInt(status);
		
	}
	
	public static final Parcelable.Creator<AudioSubscription> CREATOR = new Creator<AudioSubscription>(){  
		@Override  
		public AudioSubscription createFromParcel(Parcel source) {  
			AudioSubscription audio = new AudioSubscription();  
			audio.type = source.readString();
			audio.name = source.readString();
			audio.album = source.readString();
			audio.coverUrl = source.readString();
			audio.status= source.readInt();
//			if(source.readByte()!=0){
//				audio.isSub = true;
//			}
			return audio;  
		}  
		@Override  
		public AudioSubscription[] newArray(int size) {  
			return new AudioSubscription[size];  
			}  
		}; 
};

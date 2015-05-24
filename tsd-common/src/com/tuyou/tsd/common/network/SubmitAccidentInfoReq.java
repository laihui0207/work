package com.tuyou.tsd.common.network;

import android.os.Parcel;
import android.os.Parcelable;

public class SubmitAccidentInfoReq implements Parcelable {
	public String timestamp;
	public String latitude;
	public String longitude;
	public String district;
	public String address;
	public String[] files;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(timestamp);
		dest.writeString(latitude);
		dest.writeString(longitude);
		dest.writeString(district);
		dest.writeString(address);
		dest.writeStringArray(files);
	}

	public static final Parcelable.Creator<SubmitAccidentInfoReq> CREATOR
			= new Parcelable.Creator<SubmitAccidentInfoReq>() {

		@Override
		public SubmitAccidentInfoReq createFromParcel(Parcel source) {
			return new SubmitAccidentInfoReq(source);
		}

		@Override
		public SubmitAccidentInfoReq[] newArray(int size) {
			return new SubmitAccidentInfoReq[size];
		}
	};

	public  SubmitAccidentInfoReq() {}

	private SubmitAccidentInfoReq(Parcel source) {
		timestamp = source.readString();
		latitude  = source.readString();
		longitude = source.readString();
		district  = source.readString();
		address   = source.readString();
		files     = source.createStringArray();
	}
};

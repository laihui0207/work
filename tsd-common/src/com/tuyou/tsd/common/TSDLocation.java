package com.tuyou.tsd.common;

import android.os.Parcel;
import android.os.Parcelable;

public class TSDLocation implements Parcelable {
	/**
	 * 详细地址
	 */
	private String addrStr;
	/**
	 * 省份
	 */
	private String province;
	/**
	 * 城市
	 */
	private String city;
	/**
	 * 区/县信息
	 */
	private String district;
	/**
	 * 一天的总里程数
	 */
	private double mileage;

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getStreet() {
		return Street;
	}

	public void setStreet(String street) {
		Street = street;
	}

	public String getStreetNumber() {
		return streetNumber;
	}

	public void setStreetNumber(String streetNumber) {
		this.streetNumber = streetNumber;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return Longitude;
	}

	public void setLongitude(double longitude) {
		Longitude = longitude;
	}

	public float getDirection() {
		return direction;
	}

	public void setDirection(float direction) {
		this.direction = direction;
	}

	/**
	 * 街道信息
	 */
	private String Street;
	/**
	 * 街道号码
	 */
	private String streetNumber;
	/**
	 * 纬度坐标
	 */
	private double latitude;
	/**
	 * 经度坐标
	 */
	private double Longitude;
	/**
	 * 手机当前方向
	 */
	private float direction;
	/**
	 * 定位精度
	 */
	private float radius;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(addrStr);
		parcel.writeString(province);
		parcel.writeString(city);
		parcel.writeString(district);
		parcel.writeString(Street);
		parcel.writeString(streetNumber);
		parcel.writeDouble(latitude);
		parcel.writeDouble(Longitude);
		parcel.writeFloat(direction);
		parcel.writeFloat(radius);
		parcel.writeDouble(mileage);
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public String getAddrStr() {
		return addrStr;
	}

	public void setAddrStr(String addrStr) {
		this.addrStr = addrStr;
	}

	public double getMileage() {
		return mileage;
	}

	public void setMileage(double mileage) {
		this.mileage = mileage;
	}

	public static final Parcelable.Creator<TSDLocation> CREATOR = new Creator<TSDLocation>() {
		public TSDLocation createFromParcel(Parcel source) {
			TSDLocation mBook = new TSDLocation();
			mBook.addrStr = source.readString();
			mBook.province = source.readString();
			mBook.city = source.readString();
			mBook.district = source.readString();
			mBook.Street = source.readString();
			mBook.streetNumber = source.readString();
			mBook.latitude = source.readDouble();
			mBook.Longitude = source.readDouble();
			mBook.direction = source.readFloat();
			mBook.radius = source.readFloat();
			mBook.mileage = source.readDouble();
			return mBook;
		}

		public TSDLocation[] newArray(int size) {
			return new TSDLocation[size];
		}
	};

}

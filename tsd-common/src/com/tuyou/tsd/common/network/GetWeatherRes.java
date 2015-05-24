package com.tuyou.tsd.common.network;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 天气数据结构
 */
public class GetWeatherRes implements Parcelable {
	public int errorCode;
	public Status status;		// Http应答状态
	public Weather weather;		// 天气数据

	/**
	 * Http应答状态
	 */
	public static class Status implements Parcelable {
		public int httpStatus;
		public int code;
		public int[] errors;

		//
		// Parcelable interface implements
		//
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(httpStatus);
			dest.writeInt(code);
			dest.writeIntArray(errors);
		}

		public static final Parcelable.Creator<Status> CREATOR
				= new Parcelable.Creator<GetWeatherRes.Status>() {
			@Override
			public Status createFromParcel(Parcel source) {
				return new Status(source);
			}

			@Override
			public Status[] newArray(int size) {
				return new Status[size];
			}
		};

		private Status(Parcel in) {
			httpStatus = in.readInt();
			code = in.readInt();
			errors = in.createIntArray();
		}
	};

	/**
	 * 天气数据
	 */
	public static class Weather implements Parcelable {
		public String cityId;
		public String cityName;
		public String lastUpdate;
		public String sunrise;
		public String sunset;         
		public Suggestion[] suggestions;
		public String code;
		public String text;
		public String temperature;
		public String temperatureHigh;
		public String temperatureLow;
		public String feelsLike;
		public String  windDirection;
		public String windSpeed;
		public String windGrade;
		public String humidity;
		public String visibility;
		public String pressure;
		public String  pressureRising;
		public AirQuality airQuality;

		/**
		 * 建议信息
		 */
		public static class Suggestion implements Parcelable {
			public String name;
			public String brief;
			public String details;

			//
			// Parcelable interface implements
			//
			@Override
			public int describeContents() {
				return 0;
			}

			@Override
			public void writeToParcel(Parcel dest, int flags) {
				dest.writeString(name);
				dest.writeString(brief);
				dest.writeString(details);
			}

			public static final Parcelable.Creator<Suggestion> CREATOR
					= new Parcelable.Creator<GetWeatherRes.Weather.Suggestion>() {
				@Override
				public Suggestion createFromParcel(Parcel source) {
					return new Suggestion(source);
				}

				@Override
				public Suggestion[] newArray(int size) {
					return new Suggestion[size];
				}
			};

			private Suggestion(Parcel in) {
				name = in.readString();
				brief = in.readString();
				details = in.readString();
			}
		};

		/**
		 * 空气质量
		 */
		public static class AirQuality implements Parcelable {
			public String aqi;
			public String pm25;
			public String pm10;
			public String so2;
			public String no2;
			public String co;
			public String o3;			
			public String quality;
			public String lastUpdate;

			//
			// Parcelable interface implements
			//
			@Override
			public int describeContents() {
				return 0;
			}

			@Override
			public void writeToParcel(Parcel dest, int flags) {
				dest.writeString(aqi);
				dest.writeString(pm25);
				dest.writeString(pm10);
				dest.writeString(so2);
				dest.writeString(no2);
				dest.writeString(co);
				dest.writeString(o3);
				dest.writeString(quality);
				dest.writeString(lastUpdate);
			}

			public static final Parcelable.Creator<AirQuality> CREATOR
					= new Parcelable.Creator<GetWeatherRes.Weather.AirQuality>() {
				@Override
				public AirQuality createFromParcel(Parcel source) {
					return new AirQuality(source);
				}

				@Override
				public AirQuality[] newArray(int size) {
					return new AirQuality[size];
				}
			};

			private AirQuality(Parcel in) {
				aqi = in.readString();
				pm25 = in.readString();
				pm10 = in.readString();
				so2  = in.readString();
				no2  = in.readString();
				co   = in.readString();
				o3   = in.readString();
				quality = in.readString();
				lastUpdate = in.readString();
			}
		};

		//
		// Parcelable interface implements
		//
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(cityId);
			dest.writeString(cityName);
			dest.writeString(lastUpdate);
			dest.writeString(sunrise);
			dest.writeString(sunset);
			dest.writeString(code);
			dest.writeString(text);
			dest.writeString(temperature);
			dest.writeString(temperatureHigh);
			dest.writeString(temperatureLow);
			dest.writeString(feelsLike);
			dest.writeString(windDirection);
			dest.writeString(windSpeed);
			dest.writeString(windGrade);
			dest.writeString(humidity);
			dest.writeString(visibility);
			dest.writeString(pressure);
			dest.writeString(pressureRising);
			airQuality.writeToParcel(dest, flags);
			dest.writeTypedArray(suggestions, flags);
		}

		public static final Parcelable.Creator<Weather> CREATOR
				= new Parcelable.Creator<GetWeatherRes.Weather>() {
			@Override
			public Weather createFromParcel(Parcel source) {
				return new Weather(source);
			}

			@Override
			public Weather[] newArray(int size) {
				return new Weather[size];
			}
		};

		private Weather(Parcel in) {
			cityId = in.readString();
			cityName = in.readString();
			lastUpdate = in.readString();
			sunrise = in.readString();
			sunset  = in.readString();
			code = in.readString();
			text = in.readString();
			temperature = in.readString();
			temperatureHigh = in.readString();
			temperatureLow = in.readString();
			feelsLike = in.readString();
			windDirection = in.readString();
			windSpeed = in.readString();
			windGrade = in.readString();
			humidity = in.readString();
			visibility = in.readString();
			pressure = in.readString();
			pressureRising = in.readString();
			airQuality = AirQuality.CREATOR.createFromParcel(in);
			suggestions = in.createTypedArray(Suggestion.CREATOR);
		}
	}

	//
	// Parcelable interface implements
	//
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		status.writeToParcel(dest, flags);
		weather.writeToParcel(dest, flags);
	}

	public static final Parcelable.Creator<GetWeatherRes> CREATOR
			= new Parcelable.Creator<GetWeatherRes>() {
		public GetWeatherRes createFromParcel(Parcel in) {
			return new GetWeatherRes(in);
		}

		public GetWeatherRes[] newArray(int size) {
			return new GetWeatherRes[size];
		}
	};

	private GetWeatherRes(Parcel in) {
		status = Status.CREATOR.createFromParcel(in);
		weather = Weather.CREATOR.createFromParcel(in);
	}
};

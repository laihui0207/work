package com.tuyou.tsd.common.network;



public class GetWeatherRes {
	public static class Status {
		public int httpStatus;
		public int code;
		public int[] errors;
	};
	public Status status;
	public static class Weather {	
         public int cityId;
         public String cityName;
         public String lastupdate;
         public String sunrise;
         public String sunset;         
		public static class Suggestion {
			public String name;
			public String brief;
			public String details;
		};
		public Suggestion[] suggestions;
		public int code;
		public String text;
		public int temperature;
		public int feelsLike;
		public String  windDirection;
		public double windSpeed;
		public int windGrade;
		public int humidity;
		public double visibility;
		public double pressure;
		public String  pressureRising;
		public static class AirQuality {
			public int aqi;
			public int pm25;
			public int pm10;
			public int so2;
			public int no2;
			public int co;
			public int o3;			
			public String quality;
			public String lastUpdate;
		};
		public AirQuality airQuality;
	}
	public Weather weather;
};

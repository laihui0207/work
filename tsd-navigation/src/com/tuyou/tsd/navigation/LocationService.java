package com.tuyou.tsd.navigation;

import java.sql.Date;
import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.TSDLocation;
import com.tuyou.tsd.common.util.LogUtil;

public class LocationService extends Service {
	private LocationClient mLocationClient;
	private BDLocationListener mLocationListener = new MyLocationListener();
	// private BDLocation mCurrentLocation;
	static final String preName = "location";
	private long cacheTime = 0;
	private SharedPreferences.Editor editor;
	private SharedPreferences spf;
	private BDLocation cacheLocation;
	private double mileage;

	@Override
	public void onCreate() {
		super.onCreate();
		SDKInitializer.initialize(getApplicationContext());
		spf = getSharedPreferences("navigator", 0);
		editor = spf.edit();
		mileage = Double.parseDouble(spf.getString("mileage", 0 + ""));
		cacheTime = spf.getLong("cacheTime", System.currentTimeMillis());
		LogUtil.v("LocationService", "mileage = " + mileage);
		// BDLocation init must in the main thread
		initAndStartLocation();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mLocationClient != null) {
			LogUtil.v("LocationService", "Start a locating...");
			mLocationClient.start();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		mLocationClient.unRegisterLocationListener(mLocationListener);
		editor.putString("mileage", mileage + "");
		editor.putLong("cacheTime", cacheTime);
		editor.commit();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * 初始化并启动定位
	 */
	private void initAndStartLocation() {
		LogUtil.d("LocationService", "init and start to locate...");
		// Init location service
		mLocationClient = new LocationClient(getApplicationContext());
		mLocationClient.registerLocationListener(mLocationListener);

		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);
		option.setCoorType("bd09ll");
		option.setScanSpan(5000); // 间隔5秒定位
		option.setIsNeedAddress(true);
		mLocationClient.setLocOption(option);
		mLocationClient.start();
	}

	/**
	 * BDLocation listener
	 */
	private class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location != null) {
				LogUtil.v(
						"LocationService",
						"Get the location: " + location.getCity()
								+ location.getDistrict()
								+ location.getAddrStr());

				if (location.getCity() != null) {
					distance(location);
					editor.putString("cachelat", location.getLatitude() + "");
					editor.putString("cachelng", location.getLongitude() + "");
					editor.putString("cachecity", location.getCity());
					editor.putString("cacheaddr", location.getAddrStr());
					editor.commit();
					TSDLocation tsdLocation = new TSDLocation();
					tsdLocation.setCity(location.getCity());
					tsdLocation.setDirection(location.getDirection());
					tsdLocation.setDistrict(location.getDistrict());
					tsdLocation.setLatitude(location.getLatitude());
					tsdLocation.setLongitude(location.getLongitude());
					tsdLocation.setProvince(location.getProvince());
					tsdLocation.setRadius(location.getRadius());
					tsdLocation.setAddrStr(location.getAddrStr());
					tsdLocation.setStreet(location.getStreet());
					tsdLocation.setStreetNumber(location.getStreetNumber());
					tsdLocation.setMileage(mileage);
					Intent intent = new Intent(TSDEvent.System.LOCATION_UPDATED);
					intent.putExtra("location", tsdLocation);
					sendBroadcast(intent);
				}
			}
		}
	}

	/**
	 * 记录一天的里程
	 */
	public void distance(BDLocation location) {
		Date date = new Date(cacheTime);
		if (areSameDay(date)) {
			if (cacheLocation != null && location != null) {
				try {
					mileage = mileage
							+ DistanceUtil.getDistance(
									new LatLng(cacheLocation.getLatitude(),
											cacheLocation.getLongitude()),
									new LatLng(location.getLatitude(), location
											.getLongitude()));
				} catch (Exception e) {
					LogUtil.d("LocationService", e.toString());
				}
			}
		} else {
			mileage = 0;
		}
		cacheTime = System.currentTimeMillis();
		cacheLocation = location;
		editor.putString("mileage", mileage + "");
		editor.putLong("cacheTime", cacheTime);
		editor.commit();
		LogUtil.v("LocationService", "里程数mileage = " + mileage);
	}

	/**
	 * 是否同一天
	 * 
	 * @param date
	 * @return
	 */
	public boolean areSameDay(Date date) {
		Calendar calDateA = Calendar.getInstance();
		calDateA.setTime(date);
		Calendar calDateB = Calendar.getInstance();
		return calDateA.get(Calendar.YEAR) == calDateB.get(Calendar.YEAR)
				&& calDateA.get(Calendar.MONTH) == calDateB.get(Calendar.MONTH)
				&& calDateA.get(Calendar.DAY_OF_MONTH) == calDateB
						.get(Calendar.DAY_OF_MONTH);
	}

}

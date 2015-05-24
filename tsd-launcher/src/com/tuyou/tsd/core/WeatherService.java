package com.tuyou.tsd.core;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.network.GetWeatherRes;
import com.tuyou.tsd.common.network.JsonOA2;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;

public class WeatherService extends Service {
	private static final String TAG = "WeatherService";
	private String mCity;
	private Timer mTimer;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		if (intent != null) {
			mCity = intent.getStringExtra("city");
			Log.d(TAG, "prepare to check weather for " + mCity);
			// 服务启动时主动进行一次查询
			new Thread(new QueryWeatherDataTask()).start();
			// 同时在第一次创建时设置一个定时查询任务
			if (mTimer == null) {
				mTimer = new Timer("queryWeatherData", true);
				mTimer.schedule(new QueryWeatherDataTask(), 0, 60 * 60 * 1000);
				LogUtil.d(TAG, "schedule QueryWeatherDataTask for one time per hour.");
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
			LogUtil.d(TAG, "cancel QueryWeatherDataTask.");
		}
		super.onDestroy();
	}

	private class QueryWeatherDataTask extends TimerTask {

		@Override
		public void run() {
			if (mCity != null) {
				GetWeatherRes data = JsonOA2.getInstance(WeatherService.this).getWeatherInfo(mCity, HelperUtil.getCurrentTimestamp());
				try {
					LogUtil.d(TAG, "Get the weather data, broadcast it.");

					Intent notifyMsg = new Intent(TSDEvent.System.WEATHER_UPDATED);
					notifyMsg.putExtra("data", data);
					sendBroadcast(notifyMsg);
				} catch (NullPointerException e) {
					LogUtil.w(TAG, "Weather data is null, ignore.");
				}
			} else {
				LogUtil.w(TAG, "No city info, ignore the weather quering.");
			}
		}
		
	}

}

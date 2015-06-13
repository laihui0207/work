package com.tuyou.tsd.common.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.tuyou.tsd.common.CommonApps;
import com.tuyou.tsd.common.TSDComponent;
import com.tuyou.tsd.common.TSDShare;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;

public class CommonSleep {
	private String TAG = "CommonSleep";
	private Context mContext;
	private volatile boolean mbStop = false;
	private long mCurTime = 0;
	private long mUpdateTime = 180 * 1000;
	private IntentFilter mIntentFilter;
	public SharedPreferences pref;
	public Editor editor;
	SleepCallback mSleepCallback = null;

	private final long SLEEP_INTERVAL = 2000;
	
	interface SleepCallback{
		boolean goSleep();
	}

	public CommonSleep(Context contex) {
		// 通过该Context获得所需的SharedPreference实例
		pref = HelperUtil.getCommonPreference(contex,
				TSDComponent.CORE_SERVICE_PACKAGE,
				TSDShare.SYSTEM_SETTING_PREFERENCES);
		if (pref != null) {
			editor = pref.edit();
			mUpdateTime = (Integer.parseInt(pref.getString("screen_off_value",
				3 * 60 + ""))) * 1000;
		}

		LogUtil.d(TAG, "初始化：mUpdateTime = " + mUpdateTime);
		mContext = contex;
		Log.v("fq", "new Sleep class = " + mContext.getClass().getName());
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(CommonApps.BROADCAST_SLEEP_TIME_UPDATE);
		mContext.registerReceiver(mReceiver, mIntentFilter);
	}

	public void setSleepCallback(SleepCallback cb){
		mSleepCallback = cb;
	}

	public void start() {
		Log.v("fq", "start class = " + mContext.getClass().getName());
		Thread thread = new Thread() {
			@Override
			public void run() {
				mCurTime = System.currentTimeMillis();
				while (!mbStop) {
					try {
						Thread.sleep(SLEEP_INTERVAL);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if ((System.currentTimeMillis() - mCurTime) > mUpdateTime) {

						String name = mContext.getClass().getName();
						Log.v("fq", "time up class = "+ name);
						
						if(mSleepCallback == null || mSleepCallback.goSleep()){
							goIntoSleep(name);
						}
					}
				}
			}
		};
		thread.start();
	}

	public void update() {
		Log.v("fq", "update class = " + mContext.getClass().getName());
		mCurTime = System.currentTimeMillis();
	}

	public void stop() {
		Log.v("fq", "stop class = " + mContext.getClass().getName());
		finish();
	}

	synchronized private void finish() {
		Log.v("fq", "finish mbStop=" + mbStop + " class = "
				+ mContext.getClass().getName());
		if (!mbStop) {
			mContext.unregisterReceiver(mReceiver);
			mbStop = true;
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v("fq", "new Sleep BroadcastReceiver class = "
					+ mContext.getClass().getName());
			String action = intent.getAction();
			if (action.equals(CommonApps.BROADCAST_SLEEP_TIME_UPDATE)) {
				long time = intent.getLongExtra(CommonApps.SLEEP_TIME_UPDATE,
						mUpdateTime);
				mUpdateTime = time;
				Log.v("fq", "SLEEP_TIME_UPDATE " + time + " class = "
						+ mContext.getClass().getName());
			}
		}

	};
	
	private void goIntoSleep(String name){
		Intent intent = new Intent(
				CommonApps.BROADCAST_SHOW_SLEEP);
		intent.putExtra(
				CommonApps.BROADCAST_SHOW_SLEEP_ACTIVITY_NAME,
				name);
		mContext.sendBroadcast(intent);
		finish();
	}
}

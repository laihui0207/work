package com.tuyou.commontest;



import android.app.Service;

import android.content.Intent;

import android.os.IBinder;
import android.util.Log;



public class UpdateService extends Service {
	private static final String LOG_TAG = "UpdateService";
	private static final boolean DEBUG = false;

		

	@Override
	public void onCreate() {
		Log.v(LOG_TAG, "onCreate()....");
		

	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}


   

}

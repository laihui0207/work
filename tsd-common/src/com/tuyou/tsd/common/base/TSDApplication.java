package com.tuyou.tsd.common.base;

import com.tuyou.tsd.common.util.CrashHandler;

import android.app.Application;

public class TSDApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler.getInstance().init(getApplicationContext());
	}

}

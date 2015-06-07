package com.tuyou.tsd.common.base;

import android.app.Application;
import android.content.Context;

import com.tuyou.tsd.common.util.CrashHandler;

public class TSDApplication extends Application {

    private static TSDApplication instance = null;
    
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		CrashHandler.getInstance().init(getApplicationContext());
	}

    public static Context getContext() {
        return instance.getApplicationContext();
    }
}

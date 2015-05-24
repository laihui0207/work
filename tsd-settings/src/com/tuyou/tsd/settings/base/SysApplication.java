package com.tuyou.tsd.settings.base;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;

public class SysApplication extends Application {
	@SuppressWarnings("rawtypes")
	private List activitys = new LinkedList();
	private static SysApplication instance;

	private SysApplication() {
	}

	public synchronized static SysApplication getInstance() {
		if (null == instance) {
			instance = new SysApplication();
		}
		return instance;
	}

	// add Activity
	@SuppressWarnings("unchecked")
	public void addActivity(Activity activity) {
		activitys.add(activity);
	}

	public void exit() {
		try {
			for (Object activity : activitys) {
				if (activity != null)
					((Activity) activity).finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
	}
}

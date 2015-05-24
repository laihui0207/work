package com.tuyou.tsd.navigation.mode;

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

	public List<Activity> getActivitys() {
		return activitys;
	}

	public void exit() {
		try {
			for (Object activity : activitys) {
				if (activity != null)
					((Activity) activity).finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 不调用该方法，否则导航的常驻服务会被关闭
		// finally {
		// System.exit(0);
		// }
	}

	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
	}
}

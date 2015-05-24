package com.tuyou.tsd.cardvr.comm;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.Application;

public class VideoApplication extends Application {
	private List<Activity> activityList = new ArrayList<Activity>();
	private static VideoApplication instance;
	private  VideoApplication() {
		super();
	}
	public static VideoApplication getInstance() {
		if (null == instance) {
			instance = new VideoApplication();
		}
		return instance;
	}
	public void addAcctivity(Activity activity) {
		activityList.add(activity);
	}

	public void exitAcitvitys() {
		for (Activity activity : activityList) {
			activity.finish();
		}
	}
}

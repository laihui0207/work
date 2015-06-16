package com.tuyou.tsd.navigation.mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tuyou.tsd.navigation.MainActivity;
import com.tuyou.tsd.navigation.SearchService;

public class StartNavBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent navIntent = new Intent();
		navIntent.setClassName("com.tuyou.tsd.navigation",
				"com.tuyou.tsd.navigation.BNavigatorActivity");
		if (navIntent.resolveActivity(context.getPackageManager()) != null
				&& SearchService.navIntent != null) {
			// 说明系统中不存在这个activity
			SearchService.navIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
			context.startActivity(SearchService.navIntent);
		} else {
			Intent i = new Intent();
			i.setClass(context, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
			context.startActivity(i);
		}

	}
}

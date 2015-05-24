package com.tuyou.tsd.navigation.mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tuyou.tsd.navigation.MainActivity;

public class StartNavBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent();
		i.setClass(context, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}
}

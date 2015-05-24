package com.tuyou.tsd.cardvr.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class VideoRecBroadcastReceiver extends BroadcastReceiver {
	static final String TAG = "VideoRecBroadcastReceiver";
	static final String INTENT_START_RECORD = "tsd.command.dvr.START_RECORD";
	static final String INTENT_STOP_RECORD 	= "tsd.command.dvr.STOP_RECORD";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		/*
		String action;
		if (intent == null || (action = intent.getAction()) == null) {
	        Log.v(TAG, "Received intent with empty action");
		    return;
		}
		*/
		/*
		Intent videoRecIntent = new Intent(context, VideoRec.class);
		
		Log.v(TAG, "Broadcast receiver, " + action);
		if (Intent.ACTION_BOOT_COMPLETED.equals(action) || INTENT_START_RECORD.equals(action)) {
		    context.startService(videoRecIntent);
		} else if (INTENT_STOP_RECORD.equals(action)) {
			context.stopService(videoRecIntent);
		} 
		*/
	}
}
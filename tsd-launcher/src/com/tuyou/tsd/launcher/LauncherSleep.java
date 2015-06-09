package com.tuyou.tsd.launcher;

import com.tuyou.tsd.common.CommonApps;
import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class LauncherSleep {

	private Context mContext ;
	private volatile boolean mbStop = false;
	private long mCurTime = 0;
	private long mUpdateTime = 3*1000;
	private IntentFilter mIntentFilter;
	
	private final long SLEEP_INTERVAL = 2000;
	
	public LauncherSleep(Context contex) {
		mContext = contex;
		Log.v("fq","new Sleep class = "+mContext.getClass().getName());
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(CommonApps.BROADCAST_SLEEP_TIME_UPDATE);
		mContext.registerReceiver(mReceiver, mIntentFilter);
	}

	public void start(){
		Log.v("fq","start class = "+mContext.getClass().getName());
		Thread thread = new Thread(){
			@Override
			public void run() {
				mCurTime = System.currentTimeMillis();
				while(!mbStop){
					try {
						Thread.sleep(SLEEP_INTERVAL);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if((System.currentTimeMillis()-mCurTime)>mUpdateTime){
						Log.v("fq","time up class = "+mContext.getClass().getName());
						String name = mContext.getClass().getName();
						Intent intent = new Intent(CommonApps.BROADCAST_SHOW_SLEEP);
						intent.putExtra(CommonApps.BROADCAST_SHOW_SLEEP_ACTIVITY_NAME, name);
						mContext.sendBroadcast(intent);
						finish();
					}
				}
			}
		};
		thread.start();
	}
	
	public void update(){
		Log.v("fq","update class = "+mContext.getClass().getName());
		mCurTime = System.currentTimeMillis();
	}
	
	public void stop(){
		Log.v("fq","stop class = "+mContext.getClass().getName());
		finish();
	}
	
	synchronized private void finish(){
		Log.v("fq","finish mbStop="+mbStop+" class = "+mContext.getClass().getName());
		if(!mbStop){
			mContext.unregisterReceiver(mReceiver);
			mbStop = true;
		}
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v("fq","new Sleep BroadcastReceiver class = "+mContext.getClass().getName());
			String action = intent.getAction();
			if (action.equals(CommonApps.BROADCAST_SLEEP_TIME_UPDATE)) {
				long time = intent.getLongExtra(CommonApps.SLEEP_TIME_UPDATE, mUpdateTime);
				mUpdateTime = time;
				Log.v("fq","SLEEP_TIME_UPDATE "+time+" class = "+mContext.getClass().getName());
			}
		}
		
	};
}

package com.tuyou.tsd.news.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.tuyou.tsd.news.base.MyBaseActivity;
import com.tuyou.tsd.news.service.AudioPlayerService;
import com.tuyou.tsd.common.base.BaseActivity;


public class MyBaseActivity extends BaseActivity{
	MyReceiver m_myReceiver=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		m_myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AudioPlayerService.PLAY_EXIT);
		registerReceiver(m_myReceiver, filter);
	}
	
	
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(m_myReceiver!=null)
		{
			this.unregisterReceiver(m_myReceiver);
		}
	}




	private class MyReceiver extends BroadcastReceiver {

		boolean isPause = false;
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.v("AudioPlayerService", "received the broadcast: " + action);
			if (action.equals(AudioPlayerService.PLAY_EXIT)) {
				MyBaseActivity.this.finish();
			}
		}
	}
}

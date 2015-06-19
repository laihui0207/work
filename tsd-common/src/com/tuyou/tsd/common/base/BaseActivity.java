package com.tuyou.tsd.common.base;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

import com.tuyou.tsd.common.TSDEvent;

public class BaseActivity extends Activity{
	private String TAG = "common-BaseActivity";
	public static long timeout = 0;
	
	final long TimeDelay = 2000;
	private boolean mbCanResponse = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mHandler.sendEmptyMessageDelayed(0, TimeDelay);
	}
	
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			mbCanResponse = true;
		};
	};
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(!mbCanResponse){
			return true;
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_F4:
			timeout = (System.currentTimeMillis()-timeout);
			if(timeout<2000){
				timeout = System.currentTimeMillis();
				Log.w("fq", "onKeyDown time wait !!!");
				break;
			}else{
				Log.w("fq", "onKeyDown time ="+System.currentTimeMillis());
				Intent itF1 = new Intent();
				itF1.setAction(TSDEvent.System.HARDKEY4_PRESSED);
				sendBroadcast(itF1);
			}
			break;
		case KeyEvent.KEYCODE_F3:
			Intent itF2 = new Intent();
			itF2.setAction(TSDEvent.System.HARDKEY3_PRESSED);
			sendBroadcast(itF2);
			break;
		case KeyEvent.KEYCODE_F2:
			Intent itF3 = new Intent();
			itF3.setAction(TSDEvent.System.HARDKEY2_PRESSED);
			sendBroadcast(itF3);
			break;
		case KeyEvent.KEYCODE_F1:
			Intent itF4 = new Intent();
			itF4.setAction(TSDEvent.System.HARDKEY1_PRESSED);
			sendBroadcast(itF4);
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

}

package com.tuyou.tsd.common.base;



import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;

import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.LogUtil;

public class BaseActivity extends Activity{
	private String TAG = "common-BaseActivity";
	public static long timeout = 0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_F4:
			timeout = (System.currentTimeMillis()-timeout);
			if(timeout<2000){
				timeout = System.currentTimeMillis();
				LogUtil.w("fq", "onKeyDown time wait !!!");
				break;
			}else{
				LogUtil.w("fq", "onKeyDown time ="+System.currentTimeMillis());
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
	
	/**
	 * �������ʱ���Ϳ��и��¸�
	 */
	public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
		sendBroadcast(new Intent(TSDEvent.Navigation.IDLE_NAV_UPDATE));
		LogUtil.d(TAG, "send:"+TSDEvent.Navigation.IDLE_NAV_UPDATE);
		return super.dispatchTouchEvent(ev);
	};

}

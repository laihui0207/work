package com.tuyou.tsd.navigation;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.base.CommonSleep;
import com.tuyou.tsd.common.util.LogUtil;

public class SleepBaseActivity extends BaseActivity {
	private CommonSleep commonSleep = null;
	private String TAG = "SleepBaseActivity";

	@Override
	protected void onDestroy() {
		if (commonSleep != null) {
			commonSleep.stop();
		}
		sendBroadcast(new Intent(TSDEvent.Navigation.IDLE_NAV_STOP));
		LogUtil.d(TAG, "send:" + TSDEvent.Navigation.IDLE_NAV_STOP);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO 自动生成的方法存根
		super.onResume();
		sendBroadcast(new Intent(TSDEvent.Navigation.IDLE_NAV_UPDATE));
		LogUtil.d(TAG, "send:" + TSDEvent.Navigation.IDLE_NAV_UPDATE);
		commonSleep = new CommonSleep(SleepBaseActivity.this);
		commonSleep.start();
	}

	@Override
	protected void onPause() {
		// TODO 自动生成的方法存根
		super.onPause();
		sendBroadcast(new Intent(TSDEvent.Navigation.IDLE_NAV_STOP));
		LogUtil.d(TAG, "send:" + TSDEvent.Navigation.IDLE_NAV_STOP);
		if (commonSleep != null) {
			commonSleep.stop();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO 自动生成的方法存根
		sendBroadcast(new Intent(TSDEvent.Navigation.IDLE_NAV_UPDATE));
		LogUtil.d(TAG, "send:" + TSDEvent.Navigation.IDLE_NAV_UPDATE);
		if (commonSleep != null) {
			commonSleep.update();
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO 自动生成的方法存根
		sendBroadcast(new Intent(TSDEvent.Navigation.IDLE_NAV_UPDATE));
		LogUtil.d(TAG, "send:" + TSDEvent.Navigation.IDLE_NAV_UPDATE);
		if (commonSleep != null) {
			commonSleep.update();
		}
		return super.dispatchTouchEvent(ev);
	}

//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		// TODO 自动生成的方法存根
//		switch (event.getAction()) {
//		// 触摸屏幕时刻
//		case MotionEvent.ACTION_DOWN:
//			break;
//		// 触摸并移动时刻
//		case MotionEvent.ACTION_MOVE:
//			break;
//		// 终止触摸时刻
//		case MotionEvent.ACTION_UP:
//			sendBroadcast(new Intent(TSDEvent.Navigation.IDLE_NAV_UPDATE));
//			LogUtil.d(TAG, "send:" + TSDEvent.Navigation.IDLE_NAV_UPDATE);
//			if (commonSleep != null) {
//				commonSleep.update();
//			}
//			break;
//		}
//		return super.onTouchEvent(event);
//	}

}

package com.tuyou.tsd.navigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Window;

import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDComponent;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.TSDShare;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.navigation.mode.SysApplication;

public class BaseActivity extends com.tuyou.tsd.common.base.BaseActivity {
	public Context shareContext;
	private String TAG = "BaseActivity";
	public SharedPreferences pref;
	public Editor editor;
	public SharedPreferences spf;
	public Editor edt;
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			SysApplication.getInstance().exit();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		IntentFilter filter = new IntentFilter();
		filter.addAction(TSDEvent.System.ACC_OFF);
		registerReceiver(receiver, filter);
		sendBroadcast(new Intent(TSDEvent.Navigation.APP_STARTED));
		LogUtil.d(TAG, "send action:" + TSDEvent.Navigation.APP_STARTED);
		// 通过该Context获得所需的SharedPreference实例
		pref = HelperUtil.getCommonPreference(BaseActivity.this,
				TSDComponent.CORE_SERVICE_PACKAGE,
				TSDShare.SYSTEM_SETTING_PREFERENCES);
		if (pref != null) {
			editor = pref.edit();
		}
		spf = getSharedPreferences("navigator", MODE_WORLD_WRITEABLE);
		edt = spf.edit();
		// initNav();
	}

	// public void initNav() {
	// BaiduNaviManager.getInstance().initEngine(this, getSdcardDir(),
	// mNaviEngineInitListener, new LBSAuthManagerListener() {
	// @Override
	// public void onAuthResult(int status, String msg) {
	// String str = null;
	// if (0 == status) {
	// str = getResources().getString(
	// R.string.route_key_success);
	// } else {
	// str = getResources().getString(
	// R.string.route_key_fail)
	// + msg;
	// }
	// LogUtil.e("ACCESS_KEY", str);
	// }
	// });
	// }
	//
	// private NaviEngineInitListener mNaviEngineInitListener = new
	// NaviEngineInitListener() {
	// public void engineInitSuccess() {
	// }
	//
	// public void engineInitStart() {
	// }
	//
	// public void engineInitFail() {
	//
	// }
	// };
	//
	// private String getSdcardDir() {
	// if (Environment.getExternalStorageState().equalsIgnoreCase(
	// Environment.MEDIA_MOUNTED)) {
	// return Environment.getExternalStorageDirectory().toString();
	// }
	// return null;
	// }

	/**
	 * 函数名称 : playBroadcast 功能描述 : 开始播报语音广播 参数及返回值说明：
	 */
	public void playBroadcast(String content, int pid) {
		Intent intent = new Intent();
		intent.setAction(CommonMessage.TTS_PLAY);
		// 要发送的内容
		intent.putExtra("package", "com.tuyou.tsd.tts");
		intent.putExtra("id", pid);
		intent.putExtra("content", content);
		// 发送 一个无序广播
		sendBroadcast(intent);
	}

	/**
	 * 函数名称 : stopBroadcast 功能描述 : 主动停止语音播报广播 参数及返回值说明：
	 */
	public void stopBroadcast() {
		Intent intent = new Intent();
		intent.setAction(CommonMessage.TTS_CLEAR);
		// 发送 一个无序广播
		sendBroadcast(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
		int size = SysApplication.getInstance().getActivitys().size();
		if (size == 1) {
			sendBroadcast(new Intent(TSDEvent.Navigation.APP_STOPPED));
			LogUtil.d(TAG, "send action:" + TSDEvent.Navigation.APP_STOPPED);
		}
		super.onDestroy();
	}

}

package com.tuyou.tsd.settings.base;

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

public class BaseActivity extends com.tuyou.tsd.common.base.BaseActivity {
	public Context shareContext;
	public SharedPreferences pref;
	public Editor editor;
	private String TAG = "BaseActivity";
	public static final String ACTION = "com.tuyou.tsd.trafficstats";
	public static final String NACTION = "com.tuyou.tsd.fm";
	public static final String FACTION = "com.tuyou.tsd.flow.redress";
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isInit = Boolean.parseBoolean(pref.getString("system_init",
					"false"));
			if (isInit) {
				LogUtil.d(TAG, intent.getAction());
				SysApplication.getInstance().exit();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		IntentFilter filter = new IntentFilter();
		filter.addAction(TSDEvent.System.ACC_OFF);
		registerReceiver(receiver, filter);
		// 通过该Context获得所需的SharedPreference实例
		pref = HelperUtil.getCommonPreference(BaseActivity.this,
				TSDComponent.CORE_SERVICE_PACKAGE,
				TSDShare.SYSTEM_SETTING_PREFERENCES);
		if (pref != null) {
			editor = pref.edit();
		}
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

	/**
	 * 函数名称 : playBroadcast 功能描述 : 开始播报语音广播 参数及返回值说明：
	 */
	public void playBroadcast(String content, int pid) {
		Intent intent = new Intent();
		intent.setAction(CommonMessage.TTS_PLAY);
		// 要发送的内容
		intent.putExtra("package", getPackageName());
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
	protected void onStop() {
		Intent intent = new Intent(TSDEvent.System.PUSH_CONFIG_INFO);
		sendBroadcast(intent);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
		super.onDestroy();
	}

}

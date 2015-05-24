package com.tuyou.tsd.settings.init;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.settings.R;
import com.tuyou.tsd.settings.base.BaseActivity;

public class WeclomeActivity extends BaseActivity {
	private Button start;
	private ImageView talk;
	private AnimationDrawable animationDrawable;
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			animationDrawable.stop();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init_weclome);
		// 动态注册接收语音播报结束广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonMessage.TTS_PLAY_FINISHED);
		registerReceiver(broadcastReceiver, filter);
		init();
	}

	private void init() {
		start = (Button) findViewById(R.id.btn_init_start);
		talk = (ImageView) findViewById(R.id.img_init_talk);
		talk.setImageResource(R.drawable.list_talk);
		animationDrawable = (AnimationDrawable) talk.getDrawable();
		animationDrawable.start();
		playBroadcast(getResources().getString(R.string.txt_init_tts_1), 0);
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(WeclomeActivity.this,
						InitMainActivity.class));
				stopBroadcast();
				finish();
			}
		});
	}

	/**
	 * 函数名称 : playBroadcast 功能描述 : 开始播报语音广播 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-8-13 下午1:57:48 修改人：wanghh 描述 ：
	 * 
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
	 * 
	 * 修改记录： 日期：2014-8-13 下午4:28:49 修改人：wanghh 描述 ：
	 * 
	 */
	public void stopBroadcast() {
		Intent intent = new Intent();
		intent.setAction(CommonMessage.TTS_CLEAR);
		// 发送 一个无序广播
		sendBroadcast(intent);
	}

	@Override
	protected void onDestroy() {
		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
		}
		super.onDestroy();
	}
}

package com.tuyou.tsd.settings.trafficstats;

import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.tuyou.tsd.settings.R;
import com.tuyou.tsd.settings.base.BaseActivity;
import com.tuyou.tsd.settings.base.SysApplication;

public class FlowActivity extends BaseActivity {
	private TextView flowValueTXT, back;
	private TasksCompletedView t;
	private SharedPreferences spf;
	private Button redressBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flow);
		SysApplication.getInstance().addActivity(this);
		init();
	}

	private void init() {
		spf = getSharedPreferences("trafficstats", 0);
		long total = spf.getLong("totalFlow", 0);
		if (!isWorked()) {
			Intent service = new Intent(this, TrafficStatsService.class);
			startService(service);
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION);
		filter.setPriority(Integer.MAX_VALUE);
		registerReceiver(wifiReceiver, filter);
		t = (TasksCompletedView) findViewById(R.id.bar_flow_value);
		flowValueTXT = (TextView) findViewById(R.id.txt_flow_vaule);
		back = (TextView) findViewById(R.id.btn_flow_back);
		redressBtn = (Button) findViewById(R.id.btn_flow_redress);
		flowValueTXT.setText((int) total / 1024 / 1024 + "");
		t.setProgress((int) total / 1024 / 1024);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		redressBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage("10001", null, "108", null, null);
			}
		});
	}

	private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			long total = intent.getLongExtra("total", 0);
			String totalString = Formatter.formatFileSize(FlowActivity.this,
					total);
			System.out.println("totalString = " + totalString);
			flowValueTXT.setText((int) total / 1024 / 1024 + "");
			t.setProgress((int) total / 1024 / 1024);
		}

	};

	/**
	 * 本方法判断一个service是否已经运行 函数名称 : isWorked 功能描述 : 参数及返回值说明：
	 * 
	 * @return 已运行返回true，未运行返回false
	 * 
	 *         修改记录： 日期：2014-7-22 下午1:57:57 修改人：wanghh 描述 ：
	 * 
	 */
	public boolean isWorked() {
		ActivityManager myManager = (ActivityManager) FlowActivity.this
				.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager
				.getRunningServices(30);
		for (int i = 0; i < runningService.size(); i++) {
			if (runningService.get(i).service.getClassName().toString()
					.equals("com.tuyou.tsd.trafficstats.TrafficStatsService")) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		if (wifiReceiver != null) {
			unregisterReceiver(wifiReceiver);
		}
		super.onDestroy();
	}

}

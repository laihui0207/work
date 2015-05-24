package com.tuyou.tsd.settings.fm;

import java.text.DecimalFormat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tuyou.tsd.settings.R;
import com.tuyou.tsd.settings.base.BaseActivity;
import com.tuyou.tsd.settings.base.SysApplication;

public class FMMainActivity extends BaseActivity implements OnClickListener {
	private ImageView fMopen;
	private Button btnTS;
	private LinearLayout openLayout, offLayout;
	private TextView sumTextView, back;
	private double frequency = 88.8;
	private DecimalFormat df = new DecimalFormat("#.0");
	private boolean isOpen = false;
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			isOpen = (Boolean) msg.obj;
			if (isOpen) {
				fMopen.setImageDrawable(getResources().getDrawable(
						R.drawable.bg_wifi_open));
				isAlter(true);
			} else {
				fMopen.setImageDrawable(getResources().getDrawable(
						R.drawable.bg_wifi_off));
				isAlter(false);
			}
		};
	};

	private BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BaseActivity.NACTION)
					&& intent.getStringExtra("type").equals("switch")) {
				if (isOpen != intent.getBooleanExtra("switch", false)) {
					Message msg = new Message();
					msg.obj = intent.getBooleanExtra("switch", false);
					handler.sendMessage(msg);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fm_main);
		SysApplication.getInstance().addActivity(this);
		init();
	}

	private void init() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BaseActivity.NACTION);
		registerReceiver(myReceiver, filter);
		fMopen = (ImageView) findViewById(R.id.img_fm_open);
		btnTS = (Button) findViewById(R.id.btn_fm_ts);
		back = (TextView) findViewById(R.id.btn_fm_main_back);
		sumTextView = (TextView) findViewById(R.id.txt_fm_fre_sum);
		openLayout = (LinearLayout) findViewById(R.id.layout_fm_open);
		offLayout = (LinearLayout) findViewById(R.id.layout_fm_off);
		fMopen.setOnClickListener(this);
		btnTS.setOnClickListener(this);
		back.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (pref != null) {
			isOpen = Boolean.parseBoolean(pref.getString("FMOpen", "false"));
			frequency = Double.parseDouble(pref.getString("fm_freq", "88.8"));
		}
		sumTextView.setText(df.format(frequency));
		if (isOpen) {
			fMopen.setImageDrawable(getResources().getDrawable(
					R.drawable.bg_wifi_open));
			isAlter(true);
		} else {
			fMopen.setImageDrawable(getResources().getDrawable(
					R.drawable.bg_wifi_off));
			isAlter(false);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_fm_open:
			if (isOpen) {
				isOpen = false;
				fMopen.setImageDrawable(getResources().getDrawable(
						R.drawable.bg_wifi_off));
				isAlter(false);
			} else {
				isOpen = true;
				fMopen.setImageDrawable(getResources().getDrawable(
						R.drawable.bg_wifi_open));
				isAlter(true);
			}
			Intent intent = new Intent(NACTION);
			intent.putExtra("type", "switch");
			intent.putExtra("switch", isOpen);
			sendBroadcast(intent);
			break;
		case R.id.btn_fm_ts:
			startActivity(new Intent(this, FMActivity.class));
			break;
		case R.id.btn_fm_main_back:
			finish();
			break;
		default:
			break;
		}
	}

	/**
	 * 是否显示修改频率按钮
	 * 
	 * @param isShow
	 */
	public void isAlter(boolean isShow) {
		if (isShow) {
			openLayout.setVisibility(View.VISIBLE);
			offLayout.setVisibility(View.GONE);
		} else {
			openLayout.setVisibility(View.GONE);
			offLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myReceiver != null) {
			unregisterReceiver(myReceiver);
		}
	}
}

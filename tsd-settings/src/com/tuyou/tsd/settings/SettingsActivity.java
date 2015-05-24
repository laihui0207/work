package com.tuyou.tsd.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tuyou.tsd.settings.about.AboutActivity;
import com.tuyou.tsd.settings.base.BaseActivity;
import com.tuyou.tsd.settings.base.SettingsService;
import com.tuyou.tsd.settings.base.SysApplication;
import com.tuyou.tsd.settings.camerapreview.CameraBootActivity;
import com.tuyou.tsd.settings.fm.FMMainActivity;
import com.tuyou.tsd.settings.information.InformationActivity;
import com.tuyou.tsd.settings.power.PowerActivity;
import com.tuyou.tsd.settings.trafficstats.FlowActivity;
import com.tuyou.tsd.settings.trafficstats.WifiActivity;

public class SettingsActivity extends BaseActivity implements OnClickListener {
	private ImageButton wifiButton, powerButton, infoButton, flowButton,
			fmButton, cameraButton;
	private LinearLayout aboutLayout;
	public PowerActivity activity;
	private ImageView back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		SysApplication.getInstance().addActivity(this);
		// 初始化
		init();
		// 监听
		registerListener();
	}

	private void registerListener() {
		// 流量管理
		wifiButton.setOnClickListener(this);
		// 电源管理
		powerButton.setOnClickListener(this);
		// 个人信息设置
		infoButton.setOnClickListener(this);
		// 流量管理
		flowButton.setOnClickListener(this);
		// FM发射设置
		fmButton.setOnClickListener(this);
		// 摄像头调节
		cameraButton.setOnClickListener(this);
		// 退出
		back.setOnClickListener(this);
		// 关于小宝
		aboutLayout.setOnClickListener(this);
	}

	private void init() {
		// 启动服务
		startService(new Intent(SettingsActivity.this, SettingsService.class));
		// 流量管理
		wifiButton = (ImageButton) findViewById(R.id.btn_setting_wifi);
		// 电源管理
		powerButton = (ImageButton) findViewById(R.id.btn_setting_power);
		// 个人信息设置
		infoButton = (ImageButton) findViewById(R.id.btn_setting_info);
		// 流量管理
		flowButton = (ImageButton) findViewById(R.id.btn_setting_flow);
		// FM发射设置
		fmButton = (ImageButton) findViewById(R.id.btn_setting_fm);
		// 摄像头调节
		cameraButton = (ImageButton) findViewById(R.id.btn_setting_camera);
		// 关于小宝
		aboutLayout = (LinearLayout) findViewById(R.id.layout_about);
		back = (ImageView) findViewById(R.id.img_back);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 流量管理
		case R.id.btn_setting_wifi:
			startActivity(new Intent(SettingsActivity.this, WifiActivity.class));
			break;
		// 电源管理
		case R.id.btn_setting_power:
			startActivity(new Intent(SettingsActivity.this, PowerActivity.class));
			break;
		// 个人信息设置
		case R.id.btn_setting_info:
			Intent itInfo = new Intent(this, InformationActivity.class);
			startActivity(itInfo);
			break;
		// 流量管理
		case R.id.btn_setting_flow:
			Intent itFlow = new Intent(this, FlowActivity.class);
			startActivity(itFlow);
			break;
		// FM发射设置
		case R.id.btn_setting_fm:
			Intent itFM = new Intent(this, FMMainActivity.class);
			startActivity(itFM);
			break;
		// 摄像头调节
		case R.id.btn_setting_camera:
			startActivity(new Intent(SettingsActivity.this,
					CameraBootActivity.class));
			break;
		// 退出
		case R.id.img_back:
			finish();
			break;
		// 关于小宝
		case R.id.layout_about:
			startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
			break;
		default:
			break;
		}
	}
}

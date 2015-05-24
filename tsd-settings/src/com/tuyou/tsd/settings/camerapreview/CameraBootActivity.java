package com.tuyou.tsd.settings.camerapreview;

import java.util.List;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.settings.R;
import com.tuyou.tsd.settings.base.BaseActivity;
import com.tuyou.tsd.settings.base.SysApplication;
import com.tuyou.tsd.settings.base.WaitDialog;

public class CameraBootActivity extends BaseActivity {
	private Button lookButton;
	private TextView back;
	private WaitDialog dialog;
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			dialog.dismiss();
			startActivity(new Intent(CameraBootActivity.this,
					CameraPreviewActivity.class));
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_boot);
		SysApplication.getInstance().addActivity(this);
		init();
	}

	private void init() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(TSDEvent.CarDVR.CAM_AVAILABLE);
		registerReceiver(myReceiver, filter);
		lookButton = (Button) findViewById(R.id.btn_camera_preview_look);
		lookButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isServiceRunning(CameraBootActivity.this,
						"com.tuyou.tsd.cardvr.service.VideoRec")) {
					startBroadcast();
					waitDialog();
				} else {
					startActivity(new Intent(CameraBootActivity.this,
							CameraPreviewActivity.class));
				}
			}
		});
		back = (TextView) findViewById(R.id.btn_camera_boot_back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * 发送广播通知cardvr要进行预览
	 */
	public void startBroadcast() {
		Intent intent = new Intent();
		intent.setAction(TSDEvent.CarDVR.START_CAM_PREVIEW);
		// 发送 一个无序广播
		sendBroadcast(intent);
		LogUtil.d("CameraBootActivity", "send:"+TSDEvent.CarDVR.START_CAM_PREVIEW);
	}

	/**
	 * 等待对话框
	 */
	public void waitDialog() {
		LayoutInflater inflater = CameraBootActivity.this.getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_wait, null);
		layout.findViewById(R.id.img_dialog_off).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						dialog = null;
					}
				});

		dialog = new WaitDialog(CameraBootActivity.this, layout);
		dialog.show();
	}

	/**
	 * 用来判断服务是否运行.
	 * 
	 * @param context
	 * @param className
	 *            判断的服务名字：包名+类名
	 * @return true 在运行, false 不在运行
	 */

	public boolean isServiceRunning(Context context, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(Integer.MAX_VALUE);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myReceiver != null) {
			unregisterReceiver(myReceiver);
		}
	}
}

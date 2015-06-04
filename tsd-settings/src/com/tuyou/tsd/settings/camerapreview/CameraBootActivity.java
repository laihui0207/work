package com.tuyou.tsd.settings.camerapreview;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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
	private String TAG = "CameraBootActivity";
	private Button lookButton;
	private TextView back;
	private WaitDialog dialog;
	private boolean isCanTimeOut = true;
	private boolean isFirst = true;
	private Timer timerOutTimer;
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.d(TAG, "action = " + action);
			if (action.equals(TSDEvent.CarDVR.CAM_AVAILABLE)) {
				timerOutTimer.cancel();
				if (isCanTimeOut) {
					isCanTimeOut = false;
					if (dialog != null) {
						dialog.dismiss();
					}
					startActivity(new Intent(CameraBootActivity.this,
							CameraPreviewActivity.class));
				}
			}
		}

	};
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				timerOutTimer.cancel();
				stopCameraBroadcast();
				if (isCanTimeOut) {
					isCanTimeOut = false;
					if (dialog != null) {
						dialog.dismiss();
					}
					tsDialog(getResources().getString(
							R.string.txt_preview_failure));
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {

						@Override
						public void run() {
							handler.sendEmptyMessage(1);
						}
					}, 3000);
				}
				break;
			case 1:
				if (dialog != null) {
					dialog.dismiss();
				}
				break;
			case 2:
				Intent intent = new Intent();
				intent.setAction(TSDEvent.CarDVR.START_CAM_PREVIEW);
				// 发送 一个无序广播
				sendBroadcast(intent);
				LogUtil.d("CameraBootActivity", "send:"
						+ TSDEvent.CarDVR.START_CAM_PREVIEW);
				break;
			default:
				break;
			}
		};
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
					isCanTimeOut = true;
					startBroadcast();
					waitDialog();
					timerOutTimer = new Timer();
					timerOutTimer.schedule(new TimerTask() {

						@Override
						public void run() {
							handler.sendEmptyMessage(0);
						}
					}, 8000);
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
		if (isFirst) {
			handler.sendEmptyMessage(2);
			isFirst = false;
		}else {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					handler.sendEmptyMessage(2);
				}
			}, 3000);
		}
	}
	/**
	 * 摄像头调节完成通知cardvr
	 */
	public void stopCameraBroadcast() {
		Intent intent = new Intent();
		intent.setAction(TSDEvent.CarDVR.STOP_CAM_PREVIEW);
		LogUtil.d(TAG, "send action:" + TSDEvent.CarDVR.STOP_CAM_PREVIEW);
		// 发送 一个无序广播
		sendBroadcast(intent);
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
						if (dialog != null) {
							dialog.dismiss();
							dialog = null;
						}
					}
				});

		dialog = new WaitDialog(CameraBootActivity.this, layout);
		dialog.show();
	}

	/**
	 * 请求失败提示框
	 */
	public void tsDialog(String content) {
		LayoutInflater inflater = CameraBootActivity.this.getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_wait, null);
		TextView contentTXT = (TextView) layout
				.findViewById(R.id.txt_dialog_content);
		contentTXT.setText(content);
		layout.findViewById(R.id.img_dialog_off).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (dialog != null) {
							dialog.dismiss();
							dialog = null;
						}
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

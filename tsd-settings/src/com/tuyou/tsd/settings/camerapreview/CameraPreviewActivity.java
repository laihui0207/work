package com.tuyou.tsd.settings.camerapreview;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.settings.R;
import com.tuyou.tsd.settings.base.SleepBaseActivity;
import com.tuyou.tsd.settings.base.SysApplication;

public class CameraPreviewActivity extends SleepBaseActivity implements
		SurfaceTextureListener {
	private String TAG = "CameraPreviewActivity";
	public Camera mCamera;
	private LinearLayout tsLayout;
	private TextureView mTextureView;
	private TextView back;
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			LogUtil.d(TAG, "action = " + intent.getAction());
			String action = intent.getAction();
			if (action.equals(CommonMessage.VOICE_COMM_TAKE_PICTURE)||action.equals(TSDEvent.System.HARDKEY1_PRESSED)) {
				stopBroadcast();
				playBroadcast(getResources().getString(R.string.tts_no_photograph), 0);
			}else if (action.equals(TSDEvent.System.ACC_OFF)) {
				release();
				finish();
			}
			
		}
	};

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				tsLayout.setVisibility(View.GONE);
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_came_pre);
		SysApplication.getInstance().addActivity(this);
		// 不允许设备进入休眠
		sendBroadcast(new Intent(TSDEvent.System.DISABLE_IDLE_CHECK));
		init();
	}

	private void init() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(TSDEvent.System.ACC_OFF);
		filter.addAction(CommonMessage.VOICE_COMM_TAKE_PICTURE);
		filter.addAction(TSDEvent.System.HARDKEY1_PRESSED);
		filter.setPriority(1000);
		registerReceiver(myReceiver, filter);
		tsLayout = (LinearLayout) findViewById(R.id.layout_preview_ts);
		// 创建预览类，并与Camera关联，最后添加到界面布局中
		mTextureView = (TextureView) findViewById(R.id.textureView_layout_came_preview_layout);
		back = (TextView) findViewById(R.id.btn_camera_preview_back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				release();
				finish();
			}
		});
		initCamera();
		// 5秒后隐藏提示文字
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				handler.sendEmptyMessage(0);
			}
		}, 5000);
	}

	public void initCamera() {
		mCamera = getCameraInstance();
		mTextureView.setSurfaceTextureListener(CameraPreviewActivity.this);
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
		try {
			if (mCamera == null) {
				mCamera = getCameraInstance();
			}
			if (mCamera != null) {
				mCamera.setPreviewTexture(surface);
				mCamera.startPreview();
			} else {
				LogUtil.d(TAG, "摄像头打开失败，无法获取界面");
				Toast.makeText(CameraPreviewActivity.this, "摄像头打开失败，无法获取界面",
						Toast.LENGTH_LONG).show();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		if (mCamera != null) {
			// mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		return true;
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
	}

	/**
	 * 函数名称 : getCameraInstance 功能描述 : 打开摄像头 参数及返回值说明：
	 * 
	 * @return
	 * 
	 *         修改记录： 日期：2014-8-22 上午10:22:09 修改人：wanghh 描述 ：
	 * 
	 */
	private Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
			LogUtil.d("initConfigure", "打开Camera失败失败");
			e.printStackTrace();
		}
		return c;
	}

	@Override
	protected void onDestroy() {
		// 允许设备进入休眠
		sendBroadcast(new Intent(TSDEvent.System.ENABLE_IDLE_CHECK));
		release();
		if (myReceiver != null) {
			unregisterReceiver(myReceiver);
		}
		super.onDestroy();
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
	 * 释放资源
	 */
	public void release() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
		stopCameraBroadcast();
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
}

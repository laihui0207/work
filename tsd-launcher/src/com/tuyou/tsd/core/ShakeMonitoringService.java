package com.tuyou.tsd.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class ShakeMonitoringService extends Service implements SensorEventListener {
	private static final int FORCE_THRESHOLD = 1000;
	private static final int TIME_THRESHOLD = 150;
	private static final int SHAKE_TIMEOUT = 5000;
	private static final int SHAKE_DURATION = 1000;
	private static final int SHAKE_COUNT = 5;

	private SensorManager mSensorMgr;
	private Sensor mSensor;

	private float mLastX=-1.0f, mLastY=-1.0f, mLastZ=-1.0f;
	private long mLastTime;
	private int mShakeCount = 0;
	private long mLastShake;
	private long mLastForce;

	private Messenger mMessenger;

	@Override
	public void onCreate() {
		super.onCreate();
		mSensorMgr = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		if (mSensorMgr == null) {
			throw new UnsupportedOperationException("Sensors not supported");
		}
		resume();
	}

	@Override
	public void onDestroy() {
		pause();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			mMessenger = intent.getParcelableExtra("callback");

			String param = intent.getStringExtra("setControl");
			if (param != null && param.equals("resume")) {
				resume();
			} else if (param != null && param.equals("pause")) {
				pause();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void resume() {
		if (mSensorMgr != null && mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
			mSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
		} else {
			throw new UnsupportedOperationException("Accelerometer not supported");
		}
	}

	private void pause() {
		if (mSensorMgr != null) {
			mSensorMgr.unregisterListener(this);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (mSensor != event.sensor)
			return;
		long now = System.currentTimeMillis();

		if ((now - mLastForce) > SHAKE_TIMEOUT) {
			mShakeCount = 0;
		}

		if ((now - mLastTime) > TIME_THRESHOLD) {
			long diff = now - mLastTime;
			float speed = Math.abs(event.values[0] +
					event.values[1] +
					event.values[2] -
					mLastX - mLastY - mLastZ) / diff * 10000;
			if (speed > FORCE_THRESHOLD) {
				if ((++mShakeCount >= SHAKE_COUNT) && (now - mLastShake > SHAKE_DURATION)) {
//					LogUtil.v("ShakeMonitoringService", "====shake shake====");
					mLastShake = now;
					mShakeCount = 0;
					// Notify the listener
					if (mMessenger != null) {
						Message msg = Message.obtain(null, 200);
						try {
							mMessenger.send(msg);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				mLastForce = now;
			}
			mLastTime = now;
			mLastX = event.values[0];
			mLastY = event.values[1];
			mLastZ = event.values[2];
		}
	}

}

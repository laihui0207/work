package com.tuyou.tsd.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.common.util.TsdHelper;

public class AccCheckingService extends Service {
	private static final String TAG = "AccCheckingService";

	private boolean mAccState;	// true -- ACC on; false -- ACC off
    private AccStateCheckThread mAccStateCheckThread;

	@Override
	public void onCreate() {
		super.onCreate();

		// Broadcast the first state
		broadcastAccState(TsdHelper.getAccStatus());

		if (mAccStateCheckThread == null) {
	    	mAccStateCheckThread = new AccStateCheckThread();
		    new Thread(mAccStateCheckThread).start();
		}
	}

	@Override
	public void onDestroy() {
		if (mAccStateCheckThread != null) {
			mAccStateCheckThread.isRunning = false;		
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void broadcastAccState(boolean isOn) {
		Intent intent = new Intent();
		intent.setAction(isOn ? TSDEvent.System.ACC_ON : TSDEvent.System.ACC_OFF);
		sendBroadcast(intent);
		LogUtil.v(TAG, "AccCheckingService.java::broadcastAccState=> 广播[" + (isOn? "点火" : "熄火") + "]信号.");
	}

	/**
     * ACC状态检查
     */
    private class AccStateCheckThread implements Runnable {
        private boolean isRunning= true;
    	 
        @Override
        public void run() {
        	LogUtil.v(TAG, "AccStateCheckThread is started.");
        	try {
        		while (isRunning) {
        			boolean currentState = TsdHelper.getAccStatus();
        			if (currentState != mAccState) {
    					mAccState = currentState;
    					broadcastAccState(mAccState);
        			}
					Thread.sleep(3000);
        		}
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        	LogUtil.v(TAG, "AccStateCheckThread is stopped.");
		}
    }

}

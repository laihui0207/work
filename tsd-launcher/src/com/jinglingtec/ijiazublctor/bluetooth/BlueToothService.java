package com.jinglingtec.ijiazublctor.bluetooth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.jinglingtec.ijiazublctor.sdk.aidl.IDeviceCallback;
import com.jinglingtec.ijiazublctor.sdk.aidl.IjiazuCallback;
import com.jinglingtec.ijiazublctor.sdk.aidl.IjiazuKeyEvent;
import com.jinglingtec.ijiazublctor.sdk.aidl.KeyEventConstants;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.LogUtil;

public class BlueToothService extends Service {
    private static final String TAG = "fq";//"BlueToothService";

    private IDeviceListener mDeviceListener = new IDeviceListener();

    private IjiazuListener mIjiazuListener = new IjiazuListener();

    /**
     * Called when the activity is first created.  
     */

    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onBind ");
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG,"onCreate ");
        // register with appID + activityName
        IjiazuController.getInstance().registerIjiazuCallback(
            AppConstants.APP_ID + BlueToothService.class.getSimpleName(), mIjiazuListener);
        IjiazuController.getInstance().registerDeviceCallback(
            AppConstants.APP_ID + BlueToothService.class.getSimpleName(), mDeviceListener);
		Log.d(TAG,"onCreate ");
	}
	
	
	private void sendBroadCast(){
		
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onStartCommand ");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG,"onUnbind ");
        // clear listener
        IjiazuController.getInstance().unRegisterIjiazuCallback(
            AppConstants.APP_ID + BlueToothService.class.getSimpleName(), mIjiazuListener);
        IjiazuController.getInstance().unRegisterDeviceCallback(
            AppConstants.APP_ID + BlueToothService.class.getSimpleName(), mDeviceListener);
		return super.onUnbind(intent);
	}
	
	@Override
	public void onRebind(Intent intent) {
		Log.d(TAG,"onRebind ");
		super.onRebind(intent);
	}
	
	
    private class IjiazuListener extends IjiazuCallback.Stub {

        @Override
        public void onInit(boolean result) throws RemoteException {
            Log.d(TAG, "onInit result " + result);
            if (result) {
                // set with appID
                IjiazuController.getInstance().setForeground(AppConstants.APP_ID);
            }
        }

        @Override
        public void onStatusChange(boolean active) throws RemoteException {
            Log.d(TAG, "onStatusChange active" + active);
        }

        @Override
        public void onRequestUpdateAppStatus() throws RemoteException {
            Log.d(TAG, "onRequestUpdateAppStatus");
            // TODO notify app status to ijiazu sdk
        }
    }

    private class IDeviceListener extends IDeviceCallback.Stub {
    	public IDeviceListener(){
    		
    	}
    	long timeout = 0;
        @Override
        public void onIjiazuKeyEvent(final IjiazuKeyEvent event) throws RemoteException {
            Log.d(TAG, "onIjiazuKeyEvent event is " + event.toString());
        	int keyCode = event.getKeyCode();
        	int actionCode = event.getActionCode();
			timeout = (System.currentTimeMillis()-timeout);
			if(timeout<2000 && KeyEventConstants.KEYCODE_FM!=keyCode){
				timeout = System.currentTimeMillis();
				return;
			}
			LogUtil.w("fq", "onIjiazuKeyEvent time  !!! "+timeout);
        	Intent intent = new Intent();
        	switch(keyCode){
			case KeyEventConstants.KEYCODE_NAVIGATOR:
				Log.d(TAG, "onIjiazuKeyEvent KEYCODE_NAVIGATOR");
				intent.setAction(TSDEvent.System.HARDKEY_PLAY_PRESSED);
				sendBroadcast(intent);
				break;
			case KeyEventConstants.KEYCODE_FM:
				Log.d(TAG, "onIjiazuKeyEvent KEYCODE_FM");
				intent.setAction(TSDEvent.System.HARDKEY1_PRESSED);
				sendBroadcast(intent);
				timeout = System.currentTimeMillis();
				break;
			case KeyEventConstants.KEYCODE_TELPHONE:
				Log.d(TAG, "onIjiazuKeyEvent KEYCODE_TELPHONE");
				intent.setAction(TSDEvent.System.HARDKEY_NEXT_PRESSED);
				sendBroadcast(intent);
				break;
			case KeyEventConstants.KEYCODE_MUSIC:
				Log.d(TAG, "onIjiazuKeyEvent KEYCODE_MUSIC");
				intent.setAction(TSDEvent.System.HARDKEY4_PRESSED);
				sendBroadcast(intent);
				break;
			case KeyEventConstants.KEYCODE_OK:
				Log.d(TAG, "onIjiazuKeyEvent KEYCODE_OK");
				break;
        		
        	}
            
        }
    }

}

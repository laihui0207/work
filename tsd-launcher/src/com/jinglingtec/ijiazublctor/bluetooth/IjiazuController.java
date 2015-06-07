package com.jinglingtec.ijiazublctor.bluetooth;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.jinglingtec.ijiazublctor.sdk.aidl.IAPIController;
import com.jinglingtec.ijiazublctor.sdk.aidl.IDeviceAPI;
import com.jinglingtec.ijiazublctor.sdk.aidl.IDeviceCallback;
import com.jinglingtec.ijiazublctor.sdk.aidl.IjiazuAPI;
import com.jinglingtec.ijiazublctor.sdk.aidl.IjiazuCallback;
import com.jinglingtec.ijiazublctor.sdk.aidl.InterfaceID;
import com.tuyou.tsd.common.base.TSDApplication;

/**
 * Created by jimiyu on 15-4-20.
 */
public class IjiazuController {
    private static final String TAG = "[yyn]IjiazuController";

    private static final String SERVICE_INTENT = "com.jinglingtec.ijiazublctor.service";

    private static IjiazuController instance = new IjiazuController();

    private IAPIController mAPIController;

    private IjiazuAPI mIjiazuAPI;

    private IDeviceAPI mIDeviceAPI;

    private HashMap<String, IjiazuCallback> mIjiazuListeners = new HashMap<String, IjiazuCallback>();
    private HashMap<String, IDeviceCallback> mDeviceListeners = new HashMap<String, IDeviceCallback>();

    private boolean mBind = false;

    private IjiazuController() {
        Log.d(TAG, "IjiazuController");
        Intent intent = new Intent(SERVICE_INTENT);
        mBind = TSDApplication.getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "IjiazuController bindService result " + mBind);
    }

    public static IjiazuController getInstance() {
        Log.d(TAG, "getInstance");
        return instance;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mAPIController = IAPIController.Stub.asInterface(service);

            Log.d(TAG, "onServiceConnected: register listeners");

            try {
                mIjiazuAPI = IjiazuAPI.Stub.asInterface(mAPIController.requestInterface(InterfaceID.ijiazuInterface));
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                mIDeviceAPI = IDeviceAPI.Stub.asInterface(mAPIController.requestInterface(InterfaceID.deviceInterface));
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }

            // register listeners which added before service connection
            // init ijiazu api
            if (null != mIjiazuAPI) {
                Iterator iter = mIjiazuListeners.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, IjiazuCallback> entry = (Map.Entry<String, IjiazuCallback>) iter.next();
                    try {
                        Log.d(TAG, "start to registerIjiazuListener");
                        mIjiazuAPI.registerIjiazuListener(entry.getKey(), entry.getValue());
                    }
                    catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mIjiazuListeners.clear();
            }

            if (null != mIDeviceAPI) {
                Iterator iter = mDeviceListeners.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, IDeviceCallback> entry = (Map.Entry<String, IDeviceCallback>) iter.next();
                    try {
                        Log.d(TAG, "start to registerDeviceListener");
                        mIDeviceAPI.registerDeviceListener(entry.getKey(), entry.getValue());
                    }
                    catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mDeviceListeners.clear();
            }

            // init app with app id and key
            try {
                mIjiazuAPI.init(AppConstants.APP_ID, AppConstants.APP_KEY);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected");
            mIjiazuAPI = null;
            mIDeviceAPI = null;
        }
    };

    public void setForeground(String appID) {
        Log.d(TAG, "setForeground");
        if (mAPIController != null) {
            try {
                mAPIController.setForeground(appID);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void finalize() {
        try {
            onDestory();
            super.finalize();
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void onDestory() {
        Log.d(TAG,"onDestroy");
        clearListener();
        Log.d(TAG, "onDestroy is bind? " + mBind);
        if (mBind) {
        	TSDApplication.getContext().unbindService(mConnection);
            mBind = false;
        }
    }

    private void clearListener() {
        try {
            mIDeviceAPI.clearListener(AppConstants.APP_ID);
            mIjiazuAPI.clearListener(AppConstants.APP_ID);
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean registerIjiazuCallback(String id, IjiazuCallback listener) {
        Log.d(TAG, "registerIjiazuCallback");
        if (null == listener) {
            Log.d(TAG, "registerIjiazuCallback listener is null");
            return false;
        }
        else if (null == mIjiazuAPI) {
            Log.d(TAG, "registerIjiazuCallback mIjiazuAPI is null, will add this listener when service connect");
            mIjiazuListeners.put(id, listener);
            return false;
        }
        else {
            try {
                mIjiazuAPI.registerIjiazuListener(id, listener);
            }
            catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return false;
            }
            Log.d(TAG, "registerIjiazuCallback finish");
            return true;
        }
    }

    public boolean registerDeviceCallback(String id, IDeviceCallback listener) {
        Log.d(TAG, "registerDeviceCallback");
        if (null == listener) {
            Log.d(TAG, "registerDeviceCallback listener is null");
            return false;
        }
        else if (null == mIDeviceAPI) {
            Log.d(TAG, "registerDeviceCallback mIDeviceAPI is null, will add this listener when service connect");
            mDeviceListeners.put(id, listener);
            return false;
        }
        else {
            try {
                mIDeviceAPI.registerDeviceListener(id, listener);
            }
            catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return false;
            }
            Log.d(TAG, "registerDeviceCallback finish");
            return true;
        }
    }

    public boolean unRegisterIjiazuCallback(String id, IjiazuCallback listener) {
        Log.d(TAG, "unRegisterIjiazuCallback");
        if (null == listener) {
            Log.d(TAG, "unRegisterIjiazuCallback listener is null");
            return false;
        }
        else if (null == mIjiazuAPI) {
            Log.d(TAG, "unRegisterIjiazuCallback mIjiazuAPI is null");
            mIjiazuListeners.remove(id);
            return false;
        }
        else {
            try {
                mIjiazuAPI.unRegisterIjiazuListener(id, listener);
            }
            catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return false;
            }
            Log.d(TAG, "unRegisterIjiazuCallback finish");
            return true;
        }
    }

    public boolean unRegisterDeviceCallback(String id, IDeviceCallback listener) {
        Log.d(TAG, "unRegisterDeviceCallback");
        if (null == listener) {
            Log.d(TAG, "unRegisterDeviceCallback listener is null");
            return false;
        }
        else if (null == mIDeviceAPI) {
            Log.d(TAG, "unRegisterDeviceCallback mIDeviceAPI is null");
            mDeviceListeners.remove(id);
            return false;
        }
        else {
            try {
                mIDeviceAPI.unRegisterDeviceListener(id, listener);
            }
            catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return false;
            }
            Log.d(TAG, "unRegisterDeviceCallback finish");
            return true;
        }
    }
}

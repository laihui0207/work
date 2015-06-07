package com.jinglingtec.ijiazublctor.sdk.aidl;

import com.jinglingtec.ijiazublctor.sdk.aidl.IDeviceCallback;

interface IDeviceAPI {
    boolean isBleConnect();

    void registerDeviceListener(String appID, IDeviceCallback listener);
    void unRegisterDeviceListener(String appID, IDeviceCallback listener);
    void clearListener(String appID);
}

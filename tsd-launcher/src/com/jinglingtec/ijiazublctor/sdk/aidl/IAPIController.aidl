package com.jinglingtec.ijiazublctor.sdk.aidl;

import android.os.IBinder;

interface IAPIController {
    IBinder requestInterface(String interfaceID);
    void setForeground(String appID);
}

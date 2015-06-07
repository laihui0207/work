package com.jinglingtec.ijiazublctor.sdk.aidl;

import com.jinglingtec.ijiazublctor.sdk.aidl.IjiazuKeyEvent;

interface IDeviceCallback {
    void onIjiazuKeyEvent(in IjiazuKeyEvent event);
}

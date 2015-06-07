package com.jinglingtec.ijiazublctor.sdk.aidl;

interface IjiazuCallback {
    void onInit(boolean result);
    void onStatusChange(boolean active);
    void onRequestUpdateAppStatus();
}

package com.jinglingtec.ijiazublctor.sdk.aidl;

import com.jinglingtec.ijiazublctor.sdk.aidl.IjiazuCallback;

interface IjiazuAPI {
    void init(String appID, String key);
    void getPermission(String appID);

    void registerIjiazuListener(String appID, IjiazuCallback listener);
    void unRegisterIjiazuListener(String appID, IjiazuCallback listener);
    void clearListener(String appID);
}

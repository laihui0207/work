package com.jinglingtec.ijiazublctor.bluetooth;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class DemoApplication extends Application {
    private static DemoApplication instance = null;

    public DemoApplication() {
        instance = this;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }
}

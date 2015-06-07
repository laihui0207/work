package com.jinglingtec.ijiazublctor.sdk.aidl;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jimiyu on 15-4-14.
 */
public class IjiazuKeyEvent implements Parcelable {
    int keyCode;
    int actionCode;

    public Bundle generateBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("keyCode", keyCode);
        bundle.putInt("actionCode", actionCode);
        return bundle;
    }

    public IjiazuKeyEvent() {
        this(KeyEventConstants.KEYCODE_DEFAULT, KeyEventConstants.KEYACTION_DEFAULT);
    }

    private IjiazuKeyEvent(int keyCode, int actionCode){
        this.keyCode = keyCode;
        this.actionCode = actionCode;
    }

    public IjiazuKeyEvent(Bundle bundle) {
        keyCode = bundle.getInt("keyCode", -1);
        actionCode = bundle.getInt("actionCode", -1);
    }

    public int getKeyCode() {
        return keyCode;
    }

    public int getActionCode() {
        return actionCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public void setActionCode(int actionCode) {
        this.actionCode = actionCode;
    }

    @Override
    public String toString() {
        return "IjiazuKeyEvent{" + "keyCode=" + keyCode + ", actionCode=" + actionCode + '}';
    }

    private static com.jinglingtec.ijiazublctor.sdk.aidl.IjiazuKeyEvent getKeyEvent(Parcel in) {
        Bundle bundle = in.readBundle();
        com.jinglingtec.ijiazublctor.sdk.aidl.IjiazuKeyEvent event = new com.jinglingtec.ijiazublctor.sdk.aidl.IjiazuKeyEvent(bundle);
        return event;
    }

    public static final Creator<IjiazuKeyEvent> CREATOR = new Creator<IjiazuKeyEvent>() {
        @Override
        public com.jinglingtec.ijiazublctor.sdk.aidl.IjiazuKeyEvent[] newArray(int size) {
            return new com.jinglingtec.ijiazublctor.sdk.aidl.IjiazuKeyEvent[size];
        }

        @Override
        public com.jinglingtec.ijiazublctor.sdk.aidl.IjiazuKeyEvent createFromParcel(Parcel in) {
            return (getKeyEvent(in));
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(generateBundle());
    }
}

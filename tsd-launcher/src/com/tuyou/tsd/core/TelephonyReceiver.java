package com.tuyou.tsd.core;

import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.tuyou.tsd.common.util.LogUtil;

public class TelephonyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();   

		if("android.intent.action.PHONE_STATE".equals(action)) {
			LogUtil.v("TelephonyReceiver", "android.intent.action.PHONE_STATE, extra state = " + intent.getStringExtra("state") +
					", phone number = " + intent.getStringExtra("incoming_number"));
			// "RINGING" -- incoming call
			// "OFFHOOK" -- outgoing call
			String state = intent.getStringExtra("state");
			if (state.equals("RINGING")) {
				TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				Class<?> c;
				try {
					LogUtil.v("TelephonyReceiver", "Block the coming call.");
					// Block the coming call
					c = Class.forName(manager.getClass().getName());
					Method m = c.getDeclaredMethod("getITelephony");
					m.setAccessible(true);
					ITelephony telephony = (ITelephony) m.invoke(manager);
					telephony.endCall();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
//		else if("android.provider.Telephony.SMS_RECEIVED".equals(action)){  
//			LogUtil.v("TelephonyReceiver", "Block the coming SMS message.");
//			abortBroadcast();  
//		}  
	}

}

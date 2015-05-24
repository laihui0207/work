package com.tuyou.tsd.cardvr.utils;


import com.tuyou.tsd.cardvr.bean.Sharebean;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;

public class SharedHelperUtil {
	
	
	public static Sharebean readSharedPreferences(Context context){
		Sharebean bean = null;
		 try {
			Context shareContext = context.createPackageContext("com.tuyou.tsd.core", 0);
			SharedPreferences pref = shareContext.getSharedPreferences("tsd_cardvr_settings", Context.MODE_MULTI_PROCESS);
			bean = new Sharebean();
			bean.setAlertEnabled(pref.getBoolean("alert_enabled", true));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		 
		return bean;
	}
	
	
	public static void writeSharedPreferences(Context context,Sharebean bean){
		Context shareContext;
		try {
		    shareContext = context.createPackageContext("com.tuyou.tsd.core", Context.MODE_MULTI_PROCESS);
		    Editor editor = shareContext.getSharedPreferences("tsd_cardvr_settings", 0).edit();
		    editor.putBoolean("alert_enabled", bean.getAlertEnabled());
		    editor.commit();
		} catch (NameNotFoundException e) {
		    e.printStackTrace();
		}
	}
}

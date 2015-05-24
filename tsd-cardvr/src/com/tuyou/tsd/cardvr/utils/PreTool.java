package com.tuyou.tsd.cardvr.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 鍐欏叆涓庤鍙栭厤缃枃浠�
 * 
 * @author ZL  
 * @date 2014骞�6鏈�6鏃� 涓婂崍11:36:24
 */
public abstract class PreTool {
	public static synchronized boolean getBoolean(String key,
			boolean defaultValue, String proferenceName, Context context) {
		boolean booleanValue = false;
		try {
			SharedPreferences sharedPre = context.getSharedPreferences(
					proferenceName, Context.MODE_MULTI_PROCESS);
			booleanValue = sharedPre.getBoolean(key, defaultValue);
		} catch (Exception e) {
			Log.e(e);
		}
		return booleanValue;
	}

	public static synchronized float getFloat(String key, float defaultValue,
			String proferenceName, Context context) {
		float floatValue = 0.0F;
		try {
			SharedPreferences sharedPre = context.getSharedPreferences(
					proferenceName, Context.MODE_MULTI_PROCESS);
			floatValue = sharedPre.getFloat(key, defaultValue);
		} catch (Exception e) {
			Log.e(e);
		}
		return floatValue;
	}

	public static synchronized int getInt(String key, int defaultValue,
			String proferenceName, Context context) {
		int intValue = 0;
		try {
			SharedPreferences sharedPre = context.getSharedPreferences(
					proferenceName, Context.MODE_MULTI_PROCESS);
			intValue = sharedPre.getInt(key, defaultValue);
		} catch (Exception e) {
			Log.e(e);
		}

		return intValue;
	}

	public static synchronized long getLong(String key, long defaultValue,
			String proferenceName, Context context) {
		long longValue = 0L;
		try {
			SharedPreferences sharedPre = context.getSharedPreferences(
					proferenceName, Context.MODE_MULTI_PROCESS);
			longValue = sharedPre.getLong(key, defaultValue);
		} catch (Exception e) {
			Log.e(e);
		}
		return longValue;
	}

	public static synchronized String getString(String key,
			String defaultValue, String proferenceName, Context context) {
		String strValue = null;
		try {
			SharedPreferences sharedPre = context.getSharedPreferences(
					proferenceName, Context.MODE_MULTI_PROCESS);
			strValue = sharedPre.getString(key, defaultValue);
		} catch (Exception e) {
			Log.e(e);
		}

		return strValue;
	}

	public static synchronized boolean putBoolean(String key, boolean value,
			String proferenceName, Context context) {
		try {
			SharedPreferences sharedPre = context.getSharedPreferences(
					proferenceName, Context.MODE_MULTI_PROCESS);
			SharedPreferences.Editor e = sharedPre.edit();
			e.putBoolean(key, value);
			e.commit();
		} catch (Exception e) {
			Log.e(e);
			return false;
		}

		return true;
	}

	public static synchronized boolean putFloat(String key, float value,
			String proferenceName, Context context) {
		try {
			SharedPreferences sharedPre = context.getSharedPreferences(
					proferenceName, Context.MODE_MULTI_PROCESS);
			SharedPreferences.Editor e = sharedPre.edit();
			e.putFloat(key, value);
			e.commit();
		} catch (Exception e) {
			Log.e(e);
			return false;
		}

		return true;
	}

	public static synchronized boolean putInt(String key, int value,
			String proferenceName, Context context) {
		try {
			SharedPreferences sharedPre = context.getSharedPreferences(
					proferenceName, Context.MODE_MULTI_PROCESS);
			SharedPreferences.Editor e = sharedPre.edit();
			e.putInt(key, value);
			e.commit();
		} catch (Exception e) {
			Log.e(e);
			return false;
		}

		return true;
	}

	public static synchronized boolean putLong(String key, long value,
			String proferenceName, Context context) {
		try {
			SharedPreferences sharedPre = context.getSharedPreferences(
					proferenceName, Context.MODE_MULTI_PROCESS);
			SharedPreferences.Editor e = sharedPre.edit();
			e.putLong(key, value);
			e.commit();
		} catch (Exception e) {
			Log.e(e);
			return false;
		}

		return true;
	}

	public static synchronized boolean putString(String key, String value,
			String proferenceName, Context context) {
		try {
			SharedPreferences sharedPre = context.getSharedPreferences(
					proferenceName, Context.MODE_MULTI_PROCESS);
			SharedPreferences.Editor e = sharedPre.edit();
			e.putString(key, value);
			e.commit();
		} catch (Exception e) {
			Log.e(e);
			return false;
		}

		return true;
	}
}

package com.tuyou.tsd.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Hashtable;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public final class HelperUtil {
	private static final String LOG_TAG = "HelperUtil";

	/**
	 * Returns the unique device ID, for example, the IMEI for GSM and the MEID
	 * or ESN for CDMA phones.
	 * 
	 * @param context
	 * @return
	 */
	public static String getDeviceId(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm != null) {
			return tm.getDeviceId();
		}
		return "";
	}

	/**
	 * Get the common shared preference
	 * 
	 * @param context
	 * @param packageName
	 * @param perfName
	 * @return
	 */
	public static SharedPreferences getCommonPreference(Context context,
			String packageName, String perfName) {
		Context shareContext;
		try {
			shareContext = context.createPackageContext(packageName, 0);
			return shareContext != null ? shareContext.getSharedPreferences(
					perfName, Context.MODE_MULTI_PROCESS) : null;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 启动Activity
	 * 
	 * @param context
	 * @param packageName
	 * @param clsName
	 */
	public static void startActivity(Context context, String packageName,
			String clsName) {
		PackageManager pm = context.getPackageManager();
		try {
			ComponentName component = new ComponentName(packageName, clsName);
			ActivityInfo ai = pm.getActivityInfo(component, 0);
			Log.v(LOG_TAG, "Found activity: " + ai);

			Intent intent = new Intent();
			intent.setComponent(component);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);

			Log.d(LOG_TAG, "startActivity, " + intent);
		} catch (NameNotFoundException e) {
			Log.e(LOG_TAG,
					"HelperUtil.startActivity(), NameNotFoundException, "
							+ clsName);
			e.printStackTrace();
		}
	}

	/**
	 * 获取Wifi AP状态
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifiApEnabled(Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		try {
			Method method = wm.getClass().getDeclaredMethod("isWifiApEnabled");
			return (Boolean) method.invoke(wm);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 设置Wifi AP开启/关闭
	 * 
	 * @param context
	 * @param enabled
	 * @return
	 */
	public static boolean setWifiApEnabled(Context context, boolean enabled) {
		boolean result = false;
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		Method method;
		try {
			if (enabled && wm.isWifiEnabled()) {
				// 因Wifi与AP不能共存，所以打开AP前需要先将Wifi关闭
				wm.setWifiEnabled(false);
			}
			// 获取默认的AP
			method = wm.getClass().getMethod("getWifiApConfiguration");
			WifiConfiguration conf = (WifiConfiguration) method.invoke(wm);
			Log.v(LOG_TAG, "getWifiApConfiguration return, SSID = " + conf.SSID
					+ ", key = " + conf.preSharedKey);
			// 打开AP
			method = wm.getClass().getMethod("setWifiApEnabled",
					WifiConfiguration.class, boolean.class);
			result = (Boolean) method.invoke(wm, conf, enabled);
			Log.v(LOG_TAG, "setWifiApEnabled return: " + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取2G/3G数据网络状态
	 * 
	 * @param context
	 * @return
	 */
	public static boolean getMobileDataEnabled(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			Method method = cm.getClass().getMethod("getMobileDataEnabled");
			return (Boolean) method.invoke(cm);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 打开/关闭数据网络连接
	 * 
	 * @param context
	 * @param enabled
	 */
	public static void setMobileDataEnabled(Context context, boolean enabled) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			Method m = cm.getClass().getDeclaredMethod("setMobileDataEnabled",
					Boolean.TYPE);
			m.invoke(cm, enabled);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// copy a file from srcFile to destFile, return true if succeed, return
	// false if fail
	public static boolean copyFile(File srcFile, File destFile) {
		boolean result = false;
		try {
			InputStream in = new FileInputStream(srcFile);
			try {
				result = copyToFile(in, destFile);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			result = false;
		}
		return result;
	}

	/**
	 * Copy data from a source stream to destFile. Return true if succeed,
	 * return false if failed.
	 */
	public static boolean copyToFile(InputStream inputStream, File destFile) {
		try {
			if (destFile.exists()) {
				destFile.delete();
			}
			FileOutputStream out = new FileOutputStream(destFile);
			try {
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) >= 0) {
					out.write(buffer, 0, bytesRead);
				}
			} finally {
				out.flush();
				try {
					out.getFD().sync();
				} catch (IOException e) {
				}
				out.close();
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	/**
	 * 函数名称 : create2DCode 功能描述 : 创建二维码bitmap 参数及返回值说明：
	 * 
	 * @param url
	 *            二维码内容
	 * @param picWidth
	 *            图片宽
	 * @param picHeight
	 *            图片高
	 * @return bitmap
	 * @throws WriterException
	 * 
	 */
	public static Bitmap create2DCode(String url, int picWidth, int picHeight) {
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		BitMatrix bitMatrix;
		Bitmap bitmap = null;
		try {
			bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE,
					picWidth, picHeight, hints);
			int width = bitMatrix.getWidth();
			int height = bitMatrix.getHeight();
			int[] pixels = new int[width * height];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * width + x] = 0xff000000;
					} else {
						pixels[y * width + x] = 0xffffffff;
					}
				}
			}
			bitmap = Bitmap.createBitmap(picWidth, picHeight,
					Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

}

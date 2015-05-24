package com.tuyou.commontest;

/**
 * @author zhaozhoujie
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;



import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class Params {

	private static Params instance;

	private Context context;
	private String projectID;
	private String osVersionName;
	private String userAgent;
	private String screenWidth;
	private String screenHeight;
	private int screenWidthInt;
	private int screenHeightInt;
	private List<String> imsiList;
	private List<String> imeiList;
	private String plmn;
	private String smsc;
	private int ramSize;
	private int romSize;
	private int sdcardSize;

	private String mcc;
	private String mnc;
	private String cid;
	private String lac;

	
	private String appVersionname;
	private int appVersion;
	
	private SharedPreferences setting;

	private String deviceCode;






	private Params(Context context) {
		this.context = context.getApplicationContext();
		this.setting = this.context.getSharedPreferences(
				Params.class.getName(), 0);

	}

	public static Params getInstance(Context context) {
		if (instance == null) {
			instance = new Params(context);
		}
		return instance;
	}

	public String getHallAppID() {
		return "SHMB_0_APP_1";
	}

	//读取 mmiap.xml文件
	private String getResFileContent(String filename, Context context) {
		InputStream is = context.getClassLoader().getResourceAsStream(filename);
		if (is == null) {
			return null;
		}
		
		StringBuilder builder = new StringBuilder();
		String content = "";
		BufferedReader bufferedReader = new BufferedReader(
		new InputStreamReader(is));
		try {
			while (bufferedReader.ready()) {
				content = bufferedReader.readLine();
				builder.append(content);
			}
			bufferedReader.close();
		} catch (Exception e) {
			return null;
		}
		
		return builder.toString();
	}
	
	private String mmChannelID = null;
	//获取MM配置的channelid
		public String getMmChannelID() {
			String channelID = "";
			if (mmChannelID != null)
				return mmChannelID;
			// 载入渠道字符串
			String channleStr = getResFileContent("mmiap.xml", context);
			// 解析文件
			XmlPullParserFactory factory;
			try {
				factory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = factory.newPullParser();
				byte[] data = channleStr.getBytes();
				ByteArrayInputStream bais = new ByteArrayInputStream(data);
				parser.setInput(bais, "utf-8");
				int event = parser.getEventType();
				while (event != XmlPullParser.END_DOCUMENT) {
					switch (event) {
						case XmlPullParser.START_DOCUMENT:
							break;
						case XmlPullParser.START_TAG:
							String tag = parser.getName();
							if ("channel".equals(tag)) {
								channelID = parser.nextText();
							}
							break;
						case XmlPullParser.END_TAG:
							break;
					}
					event = parser.next();
				}
			}catch (Exception e) {
				channelID = "";
				//LogUtil.i("parse xml exp:"+e.getMessage());
			}
			mmChannelID = channelID;
			return mmChannelID;
		}
	
	public String getProjectID() {
		if (projectID == null) {
			try {
				ApplicationInfo info = context.getPackageManager()
						.getApplicationInfo(context.getPackageName(),
								PackageManager.GET_META_DATA);
				projectID = info.metaData.getString("miogameplat_project_id");
				if(!TextUtils.isEmpty(getMmChannelID()))
				{
					projectID=projectID+"_"+getMmChannelID();
				}
				
			} catch (Exception e) {
				projectID = "dddd";//context
						//.getString(R.string.com_android_miogameplat_channel_id);
				e.printStackTrace();
			}
		}
		return projectID;
	}

	/*
	 * public String getProjectID() { if (projectID == null) { projectID =
	 * context.getString(R.string.com_android_miogameplat_channel_id); } return
	 * projectID; }
	 */
	public String getOsVersionName() {
		if (osVersionName == null) {
			osVersionName = "android" + Build.VERSION.RELEASE;
		}
		return osVersionName;
	}

	public String getUserAgent() {
		if (userAgent == null) {
			userAgent = Build.MANUFACTURER.replaceAll("_", "\\\\_") + "_"
					+ Build.BRAND.replaceAll("_", "\\\\_") + "_"
					+ Build.MODEL.replaceAll("_", "\\\\_");
		}
		return userAgent;
	}




	public String getScreenWidth() {
		if (screenWidth == null) {
			getScreenResolution();
		}
		return screenWidth;
	}

	public String getScreenHeight() {
		if (screenHeight == null) {
			getScreenResolution();
		}
		return screenHeight;
	}

	public int getScreenWidthInt() {
		if (screenWidthInt == 0) {
			getScreenResolution();
		}
		return screenWidthInt;
	}

	public int getScreenHeightInt() {
		if (screenHeightInt == 0) {
			getScreenResolution();
		}
		return screenHeightInt;
	}

	private void getScreenResolution() {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);
		screenWidth = String.valueOf(metrics.widthPixels);
		screenHeight = String.valueOf(metrics.heightPixels);
		screenWidthInt = metrics.widthPixels;
		screenHeightInt = metrics.heightPixels;
	}

	public synchronized List<String> getImsiList() {
		if (imsiList == null) {
			imsiList = new ArrayList<String>();
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			Method getSubscriberIdGemini = null;
			Method getSubscriberId = null;			
			try {
				getSubscriberIdGemini = TelephonyManager.class
						.getDeclaredMethod("getSubscriberIdGemini", int.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (getSubscriberIdGemini == null) {
				try {
					getSubscriberId = TelephonyManager.class.getDeclaredMethod(
							"getSubscriberId", int.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				if (getSubscriberIdGemini != null) {
					String imsi = (String) getSubscriberIdGemini.invoke(tm, 0);
					if (imsi != null) {
						imsiList.add(imsi);
					}
					imsi = (String) getSubscriberIdGemini.invoke(tm, 1);
					if (imsi != null) {
						imsiList.add(imsi);
					}
				} else if (getSubscriberId != null) {
					String imsi = (String) getSubscriberId.invoke(tm, 0);
					if (imsi != null) {
						imsiList.add(imsi);
					}
					imsi = (String) getSubscriberId.invoke(tm, 1);
					if (imsi != null) {
						imsiList.add(imsi);
					}
				} else {
					String imsi = tm.getSubscriberId();
					if (imsi != null) {
						imsiList.add(imsi);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (imsiList != null && imsiList.size() <= 0) {
			// imsiList.add("111111111111111");
		}
		return imsiList;
	}
	
	public synchronized String getSimSerialNumber()
	{
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getSimSerialNumber();
	}
	public synchronized String getImei()
	{
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = tm.getDeviceId();
		return imei;
	}
	public synchronized List<String> getImeiList() {
		if (imeiList == null) {
			imeiList = new ArrayList<String>();
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			Method getDeviceIdGemini = null;
			Method getDeviceId = null;
			
			try {
				getDeviceIdGemini = TelephonyManager.class.getDeclaredMethod(
						"getDeviceIdGemini", int.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (getDeviceIdGemini == null) {
				try {
					getDeviceId = TelephonyManager.class.getDeclaredMethod(
							"getDeviceId", int.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				if (getDeviceIdGemini != null) {
					String imei = (String) getDeviceIdGemini.invoke(tm, 0);
					if (imei != null) {
						imeiList.add(imei);
					}
					imei = (String) getDeviceIdGemini.invoke(tm, 1);
					if (imei != null) {
						imeiList.add(imei);
					}
				} else if (getDeviceId != null) {
					String imei = (String) getDeviceId.invoke(tm, 0);
					if (imei != null) {
						imeiList.add(imei);
					}
					imei = (String) getDeviceId.invoke(tm, 1);
					if (imei != null) {
						imeiList.add(imei);
					}
				} else {
					String imei = tm.getDeviceId();
					if (imei != null) {
						imeiList.add(imei);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (imeiList.size() <= 0) {
			imeiList.add("222222222222222");
		}
		return imeiList;
	}

	public String getPlmn() {
		if (plmn == null) {
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			plmn = tm.getNetworkOperator();
		}
		return plmn;
	}

	public String getSmsc() {
		if (smsc == null) {
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(
						Uri.parse("content://sms/inbox"),
						new String[] { "service_center" }, null, null,
						"date DESC LIMIT 10");
				while (cursor.moveToNext()) {
					smsc = cursor.getString(0);
					if (smsc != null && !smsc.equals("")) {
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		return smsc;
	}

	public int getRamSize() {
		if (ramSize == 0) {
			InputStreamReader ir = null;
			LineNumberReader input = null;
			try {
				//LogUtil.w("cat /proc/meminfo");
				Process pp = Runtime.getRuntime().exec("cat /proc/meminfo");
				ir = new InputStreamReader(pp.getInputStream());
				input = new LineNumberReader(ir);
				for (int i = 0; i < 100; i++) {
					String str = input.readLine();
					//LogUtil.w(str);
					if (str != null) {
						if (str.indexOf("MemTotal") > -1) {
							String memTotal = str.substring(
									str.indexOf(":") + 1, str.length() - 2);
							ramSize = Integer.parseInt(memTotal.trim());
							break;
						}
					} else {
						break;
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (ir != null) {
					try {
						ir.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return ramSize;
	}

	public int getRomSize() {
		if (romSize == 0) {
			File path = Environment.getDataDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			romSize = (int) (availableBlocks * blockSize / 1024);
		}
		return romSize;
	}

	public int getSdcardSize() {
		if (sdcardSize == 0 || sdcardSize == -1) {
			String st = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(st)) {
				File path = Environment.getExternalStorageDirectory();
				StatFs stat = new StatFs(path.getPath());
				long blockSize = stat.getBlockSize();
				long availableBlocks = stat.getAvailableBlocks();
				sdcardSize = (int) (availableBlocks * blockSize / 1024);
			} else {
				sdcardSize = -1;
			}
		}
		return sdcardSize;
	}

	public boolean isSystemApp() {
		try {
			ApplicationInfo ai = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(), 0);
			return (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getAppVersionName() {
		if (appVersionname == null) {
			try {
				PackageManager pm = (PackageManager) context
						.getPackageManager();
				PackageInfo info = pm.getPackageInfo(context.getPackageName(),
						0);
				appVersionname = info.versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return appVersionname;
	}



	public int getAppVersion() {
		if (appVersion == 0) {
			/*
			 * String sdkVersionCode =
			 * context.getString(R.string.com_android_miogameplat_version);
			 * appVersion = Integer.valueOf(sdkVersionCode);
			 */
			try {
				PackageManager pm = (PackageManager) context
						.getPackageManager();
				PackageInfo info = pm.getPackageInfo(context.getPackageName(),
						0);
				appVersion = info.versionCode;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return appVersion;
	}

	/*
	 * public int getAppVersionint() { if (appVersion == 0) { String
	 * sdkVersionCode = "10001";//
	 * context.getString(MyR.string.com_android_miogameplat_version); appVersion
	 * = Integer.valueOf(sdkVersionCode); } return appVersion; }
	 */
	public String getDeviceCode() {
		if (deviceCode == null) {
			deviceCode = setting.getString("deviceCode", null);
			if (deviceCode == null) {
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				String imei = tm.getDeviceId();
				if (imei == null) {
					imei = "000000000000000"; // 15
				}
				WifiManager wm = (WifiManager) context
						.getSystemService(Context.WIFI_SERVICE);
				String wifiMac;
				if (wm != null) {
					wifiMac = wm.getConnectionInfo().getMacAddress();
					if (wifiMac == null) {
						wifiMac = "0000000000000000"; // 16
					}
				} else {
					wifiMac = "0000000000000000"; // 16
				}

				BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
				String btMac;
				if (ba != null) {
					btMac = ba.getAddress();
					if (btMac == null) {
						btMac = "0000000000000000"; // 16
					}
				} else {
					btMac = "0000000000000000"; // 16
				}
				String cpuSerial = getCPUSerial();
				JSONObject json = new JSONObject();
				try {
					json.put("imei", imei);
					json.put("wifiMac", wifiMac);
					json.put("btMac", btMac);
					json.put("cpuSerial", cpuSerial);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				deviceCode = json.toString();
				setting.edit().putString("deviceCode", deviceCode).commit();
			}
		}
		return deviceCode;
	}

	private String getCPUSerial() {
		String cpuAddress = "9999999999999999";// 16
		InputStreamReader ir = null;
		LineNumberReader input = null;
		try {
			//LogUtil.w("cat /proc/cpuinfo");
			Process pp = Runtime.getRuntime().exec("cat /proc/cpuinfo");
			ir = new InputStreamReader(pp.getInputStream());
			input = new LineNumberReader(ir);
			for (int i = 0; i < 100; i++) {
				String str = input.readLine();
				//LogUtil.w(str);
				if (str != null) {
					if (str.indexOf("Serial") > -1) {
						String strCPU = str.substring(str.indexOf(":") + 1,
								str.length());
						cpuAddress = strCPU.trim();
						break;
					}
				} else {
					break;
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (ir != null) {
				try {
					ir.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return cpuAddress;
	}

	private void getLocation() {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String plmn = telephonyManager.getNetworkOperator();
		if (plmn != null && plmn.length() > 3) {
			mcc = plmn.substring(0, 3);
			mnc = plmn.substring(3);
		}
		GsmCellLocation cellLocation = null;
		if (telephonyManager.getCellLocation() instanceof GsmCellLocation) {
			cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
		}
		if (cellLocation != null) {
			lac = String.valueOf(cellLocation.getLac());
			cid = String.valueOf(cellLocation.getCid());
		}
	}

	public String getMcc() {
		if (mcc == null) {
			getLocation();
		}
		return mcc;
	}

	public String getMnc() {
		if (mnc == null) {
			getLocation();
		}
		return mnc;
	}

	public String getCid() {
		if (cid == null) {
			getLocation();
		}
		return cid;
	}

	public String getLac() {
		if (lac == null) {
			getLocation();
		}
		return lac;
	}

	public boolean wifiIsActiveAvailable(boolean checkConnected) {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager != null) {
			NetworkInfo nwInfo = connManager.getActiveNetworkInfo();
			if (nwInfo != null
					&& nwInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				if (checkConnected) {
					if (nwInfo.isConnected())
						return true;
					return false;
				}
				return true;
			}
		}

		return false;
	}


}

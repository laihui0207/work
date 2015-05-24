package com.tuyou.tsd.settings.trafficstats;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.settings.R;
import com.tuyou.tsd.settings.base.BaseActivity;
import com.tuyou.tsd.settings.base.SysApplication;

public class WifiActivity extends BaseActivity implements OnClickListener {
	private WifiManager wifiManager;
	private TextView back;
	private ImageView open;
	private LinearLayout openLayout, offLayout;
	private TextView psdTXT, nameTXT, typeTXT;
	// 密码的加密类型，1为开放，2为wap，3为wap2
	private int type = 2;
	/**
	 * 无线热点关闭状态
	 */
	public static final int WIFI_AP_STATE_DISABLED = 11;
	/**
	 * 无线热点开启状态
	 */
	public static final int WIFI_AP_STATE_ENABLED = 13;
	/**
	 * 无线热点异常
	 */
	public static final int WIFI_AP_STATE_FAILED = 14;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi);
		SysApplication.getInstance().addActivity(this);
		// 初始化
		init();
		// 监听
		monitor();
	}

	/**
	 * 函数名称 : monitor 功能描述 : 监听方法 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-7-22 上午10:06:47 修改人：wanghh 描述 ：
	 * 
	 */
	private void monitor() {
		open.setOnClickListener(this);
		back.setOnClickListener(this);
	}

	/**
	 * 函数名称 : init 功能描述 : 初始化方法 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-7-22 上午10:06:24 修改人：wanghh 描述 ：
	 * 
	 */
	private void init() {
		// 获取wifi管理服务
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		openLayout = (LinearLayout) findViewById(R.id.layout_wifi_open);
		offLayout = (LinearLayout) findViewById(R.id.layout_wifi_off);
		// 开启、关闭无线热点
		open = (ImageView) findViewById(R.id.img_wifi_open);
		back = (TextView) findViewById(R.id.btn_wifi_back);
		nameTXT = (TextView) findViewById(R.id.txt_flow_ssid_name);
		typeTXT = (TextView) findViewById(R.id.txt_flow_ssid_type);
		psdTXT = (TextView) findViewById(R.id.txt_flow_ssid_psd);
		nameTXT.setText(getResources().getString(R.string.wifi_ssid));
		psdTXT.setText(getResources().getString(R.string.wifi_psd));
		if (getWifiApState(wifiManager)) {
			open.setImageDrawable(getResources().getDrawable(
					R.drawable.bg_wifi_open));
		} else {
			open.setImageDrawable(getResources().getDrawable(
					R.drawable.bg_wifi_off));
		}
		open.invalidate();
		// getWIFI(WifiActivity.this);
	}

	/**
	 * 函数名称 : setWifiApEnabled 功能描述 : 无线热点开关以及设置无线热点信息 参数及返回值说明：
	 * 
	 * @param enabled
	 * @return
	 * 
	 *         修改记录： 日期：2014-7-22 上午9:58:02 修改人：wanghh 描述 ：
	 * 
	 */
	public boolean setWifiApEnabled(boolean enabled) {
		if (enabled) { // disable WiFi in any case
			// wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
			wifiManager.setWifiEnabled(false);
		}
		if (enabled) {
			stopBroadcast();
			playBroadcast(getResources().getString(R.string.wifi_open_ts), 0);
			open.setImageDrawable(getResources().getDrawable(
					R.drawable.bg_wifi_open));
			if (openLayout.getVisibility() != View.VISIBLE) {
				openLayout.setVisibility(View.VISIBLE);
				offLayout.setVisibility(View.GONE);
			}
			// Toast toast = Toast.makeText(WifiActivity.this, getResources()
			// .getString(R.string.wifi_open_on), Toast.LENGTH_LONG);
			// toast.setGravity(Gravity.BOTTOM, 0, 10);
			// toast.show();
		} else {
			stopBroadcast();
			playBroadcast(getResources().getString(R.string.wifi_off_ts), 0);
			open.setImageDrawable(getResources().getDrawable(
					R.drawable.bg_wifi_off));
			if (offLayout.getVisibility() != View.VISIBLE) {
				offLayout.setVisibility(View.VISIBLE);
				openLayout.setVisibility(View.GONE);
			}
			// Toast toast = Toast.makeText(WifiActivity.this, getResources()
			// .getString(R.string.wifi_open_off), Toast.LENGTH_LONG);
			// toast.setGravity(Gravity.BOTTOM, 0, 10);
			// toast.show();
		}
		open.invalidate();
		try {
			// 热点的配置类
			WifiConfiguration apConfig = new WifiConfiguration();
			// 配置热点的名称(可以在名字后面加点随机数什么的)
			apConfig.SSID = getResources().getString(R.string.wifi_ssid);
			// 配置热点的密码(注意密码必须大于8位)
			switch (type) {
			case 1:
				apConfig.allowedKeyManagement
						.set(WifiConfiguration.KeyMgmt.NONE);
				break;
			case 2:
				apConfig.allowedKeyManagement
						.set(WifiConfiguration.KeyMgmt.WPA_PSK);
				break;
			case 3:
				apConfig.allowedKeyManagement
						.set(WifiConfiguration.KeyMgmt.WPA_PSK);
				break;

			default:
				break;
			}
			apConfig.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			apConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			apConfig.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			apConfig.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			apConfig.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.CCMP);
			apConfig.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.TKIP);
			apConfig.preSharedKey = getResources().getString(R.string.wifi_psd);
			// 通过反射调用设置热点
			Method method = wifiManager.getClass().getMethod(
					"setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
			// 返回热点打开状态
			return (Boolean) method.invoke(wifiManager, apConfig, enabled);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 函数名称 : getWifiApState 功能描述 : 返回无线热点的连接状态 参数及返回值说明：
	 * 
	 * @param wifiManager
	 * @return 无线热点状态
	 * 
	 *         修改记录： 日期：2014-7-22 上午9:09:18 修改人：wanghh 描述 ：
	 * 
	 */
	public boolean getWifiApState(WifiManager wifiManager) {
		boolean isWifiApState = false;
		int i;
		try {
			Method method = wifiManager.getClass().getMethod("getWifiApState");
			i = (Integer) method.invoke(wifiManager);
		} catch (Exception e) {
			LogUtil.i("Cannot get WiFi AP state", e + "");
			i = WIFI_AP_STATE_FAILED;
		}
		switch (i) {
		case WIFI_AP_STATE_DISABLED:
			isWifiApState = false;
			break;
		case WIFI_AP_STATE_ENABLED:
			isWifiApState = true;
			break;
		case WIFI_AP_STATE_FAILED:
			isWifiApState = false;
			break;
		default:
			break;
		}
		return isWifiApState;

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!getWifiApState(wifiManager)) {
		} else {
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 开启关闭无线热点
		case R.id.img_wifi_open:
			if (getWifiApState(wifiManager)) {
				setWifiApEnabled(false);
			} else {
				setWifiApEnabled(true);
			}
			break;
		// 返回
		case R.id.btn_wifi_back:
			finish();
			break;
		default:
			break;
		}
	}

	// /**
	// * 函数名称 : getWIFI 功能描述 : 获取用户自己放出的无线热点的用户名和密码 参数及返回值说明：
	// *
	// * @param context
	// *
	// * 修改记录： 日期：2014-8-18 下午5:04:54 修改人：wanghh 描述 ：
	// *
	// */
	// public void getWIFI(Context context) {
	// WifiManager wm = (WifiManager) context
	// .getSystemService(Context.WIFI_SERVICE);
	// String typeString = "";
	// Method method;
	// try {
	// method = wm.getClass().getMethod("getWifiApConfiguration");
	// WifiConfiguration conf = (WifiConfiguration) method.invoke(wm);
	// for (int k = 0; k < conf.allowedKeyManagement.size(); k++) {
	// if (conf.allowedKeyManagement.get(k)) {
	// if (k < KeyMgmt.strings.length) {
	// typeString = KeyMgmt.strings[k];
	// }
	// }
	// }
	// if (typeString.equals("NONE")) {
	// typeString = "开放";
	// } else {
	// typeString = getResources().getString(R.string.flow_psd_wpa);
	// }
	// ssidString = conf.SSID;
	// passwordString = conf.preSharedKey;
	// nameTXT.setText(ssidString);
	// typeTXT.setText(typeString);
	// psdTXT.setText(passwordString);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
}

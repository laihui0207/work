package com.tuyou.tsd.settings.base;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDComponent;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.TSDShare;
import com.tuyou.tsd.common.network.GetConfigRes;
import com.tuyou.tsd.common.network.JsonOA2;
import com.tuyou.tsd.common.network.SubmitConfigRes;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.common.util.MyAsyncTask;
import com.tuyou.tsd.common.util.TsdHelper;
import com.tuyou.tsd.settings.R;
import com.tuyou.tsd.settings.fm.FMMainActivity;
import com.tuyou.tsd.settings.trafficstats.TrafficStatsService;

public class SettingsService extends Service {
	public SharedPreferences pref, spf;
	public Editor editor, edt;
	private NotificationManager nm;
	private Notification notification;
	public static final String SAVEFMACTION = "com.tuyou.tsd.save.fm";
	private boolean fmSwitch = false, isInit = false;
	private double frequency = 88.8;
	private DecimalFormat df = new DecimalFormat("#.0");
	private int nid = 19900;
	private String TAG = "SettingsService";
	private String spfName = "fmSetting";
	private int sum = 0;
	private int maxSum = 5;
	// 等待多少时间开启fm
	private int waitTime = 3000;
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.d(TAG, "action = " + action);
			if (action.equals(TSDEvent.System.PUSH_CONFIG_INFO)) {
				upSettings();
			} else if (action.equals(TSDEvent.System.FETCH_CONFIG_INFO)) {
				downSettings();
			} else if (action.equals(TSDEvent.Push.MESSAGE_ARRIVED)) {
				try {
					String message = intent.getStringExtra("message");
					JSONObject jsonObject = new JSONObject(message);
					String module = jsonObject.getString("module");
					if ("config".equals(module)) {
						push(intent);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if (action.equals(BaseActivity.NACTION)) {
				if (intent.getStringExtra("type").equals("switch")) {
					fmSwitch = intent.getBooleanExtra("switch", false);
					if (fmSwitch) {
						notification.icon = R.drawable.icon_fm_nofi_on;
						if (nm == null) {
							notification();
						} else {
							notification.contentView.setImageViewResource(
									R.id.img_notification_fm_switch,
									R.drawable.bg_wifi_open);
							openFM();
						}
					} else {
						TsdHelper.setFMOff();
						notification.icon = R.drawable.icon_fm_nofi_off;
						notification.contentView.setImageViewResource(
								R.id.img_notification_fm_switch,
								R.drawable.bg_wifi_off);
					}
					if (pref != null) {
						isInit = Boolean.parseBoolean(pref.getString(
								"system_init", "false"));
					}
					if (pref != null && isInit) {
						editor.putString("FMOpen", fmSwitch + "");
						editor.commit();
					}
					nm.notify(nid, notification);
				} else if (intent.getStringExtra("type").equals("alter")) {
					frequency = intent.getDoubleExtra("frequency", 88.8);
					TsdHelper.setFMFreq((int) (frequency * 100));
					if (nm != null) {
						notification.contentView.setTextViewText(
								R.id.txt_notification_fm_value,
								df.format(frequency) + "");
						nm.notify(nid, notification);
					}
				}
			} else if (action.equals(TSDEvent.System.ACC_OFF)) {
				if (fmSwitch) {
					TsdHelper.setFMOff();
					nm.cancel(nid);
				}
			} else if (action.equals(TSDEvent.System.ACC_ON)) {
				if (fmSwitch) {
					openFM();
					notification();
				}
			} else if (action.equals(SAVEFMACTION)) {
				if (pref != null) {
					editor.putString("FMOpen", fmSwitch + "");
					editor.commit();
				}
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@SuppressLint("CommitPrefEdits")
	@Override
	public void onCreate() {
		// System.out.println("onCreate");
		// 通过该Context获得所需的SharedPreference实例
		pref = HelperUtil.getCommonPreference(SettingsService.this,
				TSDComponent.CORE_SERVICE_PACKAGE,
				TSDShare.SYSTEM_SETTING_PREFERENCES);
		if (pref != null) {
			editor = pref.edit();
		}
		spf = getSharedPreferences(spfName, 0);
		edt = spf.edit();
		sum = spf.getInt("sum", 0);
		notification();
		Intent service = new Intent(this, TrafficStatsService.class);
		startService(service);
		Intent intent = new Intent(TSDEvent.System.FETCH_CONFIG_INFO);
		sendBroadcast(intent);
		super.onCreate();
	}

	@SuppressLint("CommitPrefEdits")
	public void init() {
		if (pref != null) {
			isInit = Boolean.parseBoolean(pref
					.getString("system_init", "false"));
			fmSwitch = Boolean.parseBoolean(pref.getString("FMOpen", "false"));
			frequency = Double.parseDouble(pref.getString("fm_freq", "88.8"));
		}
		if (fmSwitch) {
			openFM();
			notification();
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(TSDEvent.System.PUSH_CONFIG_INFO);
		filter.addAction(TSDEvent.System.FETCH_CONFIG_INFO);
		filter.addAction(TSDEvent.Push.MESSAGE_ARRIVED);
		filter.addAction(BaseActivity.NACTION);
		filter.addAction(TSDEvent.System.ACC_ON);
		filter.addAction(TSDEvent.System.ACC_OFF);
		filter.addAction(SAVEFMACTION);
		registerReceiver(myReceiver, filter);
	}

	/**
	 * 上传配置信息
	 */
	private void upSettings() {
		if (pref != null) {
			Map<String, ?> maps = pref.getAll();
			JSONObject jsonObj = new JSONObject();
			for (String key : maps.keySet()) {
				try {
					jsonObj.put(key, maps.get(key));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			SubmitConfigReqTask testtask = new SubmitConfigReqTask();
			testtask.execute(jsonObj);
		}
	}

	/**
	 * 下载配置信息
	 */
	private void downSettings() {
		GetConfigTask gTask = new GetConfigTask();
		gTask.execute(TSDShare.SYSTEM_SETTING_PREFERENCES);
	}

	/**
	 * push下来的配置
	 */
	private void push(Intent intent) {
		String message = intent.getStringExtra("message");
		try {
			JSONObject jsonObject = new JSONObject(message);// 解析收到的信息
			JSONObject jsonObject2 = jsonObject.getJSONObject("content");// 获取一个配置文件名下面的配置信息
			JSONObject content = jsonObject2.getJSONObject("content");// 获取一个配置文件名下面的配置信息
			Iterator<?> it = content.keys();
			String key = null;
			while (it.hasNext()) {// 遍历JSONObject
				key = it.next().toString();
				try {
					editor.putString(key, content.getString(key));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			editor.commit();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.d(TAG, "onStartCommand");
		init();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	public class SubmitConfigReqTask extends
			MyAsyncTask<JSONObject, Void, SubmitConfigRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final SubmitConfigRes result) {
			if (result != null) {
				switch (result.errorCode) {
				case 0:
					Gson gson = new Gson();
					String resultJson = gson.toJson(result);
					LogUtil.d(TAG, "resultJson = " + resultJson);
					break;
				case -1:
					Toast.makeText(SettingsService.this, "没有网络》》》》",
							Toast.LENGTH_LONG).show();
					break;

				default:
					break;
				}
			}
		}

		@Override
		protected SubmitConfigRes doInBackground(JSONObject... arg0) {
			return JsonOA2.getInstance(SettingsService.this).setConfigInfo(
					TSDShare.SYSTEM_SETTING_PREFERENCES, arg0[0]);
		}
	}

	public class GetConfigTask extends MyAsyncTask<String, Void, GetConfigRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetConfigRes result) {
			if (pref != null) {
				editor = pref.edit();
			} else {
				return;
			}
			if (result != null) {
				switch (result.errorCode) {
				case 0:
					if (result.configs == null) {
						return;
					}
					for (int i = 0; i < result.configs.length; i++) {
						try {
							Gson gson = new Gson();
							String content = gson
									.toJson(result.configs[i].content);
							JSONObject jObject = new JSONObject(content);
							Iterator<?> it = jObject.keys();
							String key = null;
							while (it.hasNext()) {// 遍历JSONObject
								key = it.next().toString();
								editor.putString(key, jObject.getString(key));
							}
							editor.commit();
						} catch (JSONException e1) {
							e1.printStackTrace();
						}
					}
					break;
				case -1:
					Toast.makeText(SettingsService.this, "没有网络》》》》",
							Toast.LENGTH_LONG).show();
					break;

				default:
					break;
				}
			}
		}

		@Override
		protected GetConfigRes doInBackground(String... arg0) {
			return JsonOA2.getInstance(SettingsService.this).getConfigInfo(
					arg0[0]);
		}
	}

	public void notification() {
		LogUtil.d(TAG, "开启fm通知栏");
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification = new Notification();
		notification.flags = Notification.FLAG_NO_CLEAR;
		notification.contentView = new RemoteViews(getPackageName(),
				R.layout.layout_fm_notification);
		notification.contentView.setTextViewText(
				R.id.txt_notification_fm_value, df.format(frequency) + "");
		if (fmSwitch) {
			notification.icon = R.drawable.icon_fm_nofi_on;
			notification.contentView.setImageViewResource(
					R.id.img_notification_fm_switch, R.drawable.bg_wifi_open);
		} else {
			notification.icon = R.drawable.icon_fm_nofi_off;
			notification.contentView.setImageViewResource(
					R.id.img_notification_fm_switch, R.drawable.bg_wifi_off);
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction("fm_switch");
		filter.addAction("fm_jump");
		registerReceiver(onClickReceiver, filter);
		Intent buttonIntent = new Intent("fm_switch");
		PendingIntent pendButtonIntent = PendingIntent.getBroadcast(this, 0,
				buttonIntent, 0);
		notification.contentView.setOnClickPendingIntent(
				R.id.img_notification_fm_switch, pendButtonIntent);
		Intent intent = new Intent("fm_jump");
		PendingIntent pendIntent = PendingIntent.getBroadcast(this, 0, intent,
				0);
		notification.contentIntent = pendIntent;
		nm.notify(nid, notification);
	}

	private BroadcastReceiver onClickReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.d(TAG, "action = " + action);
			if (action.equals("fm_switch")) {
				// 在这里处理点击事件
				if (fmSwitch) {
					fmSwitch = false;
					TsdHelper.setFMOff();
					Intent i = new Intent(BaseActivity.NACTION);
					i.putExtra("type", "switch");
					i.putExtra("switch", fmSwitch);
					context.sendBroadcast(i);
					notification.contentView.setImageViewResource(
							R.id.img_notification_fm_switch,
							R.drawable.bg_wifi_off);
				} else {
					fmSwitch = true;
					Intent i = new Intent(BaseActivity.NACTION);
					i.putExtra("type", "switch");
					i.putExtra("switch", fmSwitch);
					context.sendBroadcast(i);
					notification.contentView.setImageViewResource(
							R.id.img_notification_fm_switch,
							R.drawable.bg_wifi_open);
				}
				nm.notify(nid, notification);
			} else if (action.equals("fm_jump")) {
				if (pref != null) {
					isInit = Boolean.parseBoolean(pref.getString("system_init",
							"false"));
				}
				if (isInit) {
					startActivity(new Intent(SettingsService.this,
							FMMainActivity.class)
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				}
			}
		}
	};

	public void openFM() {
		if (sum < maxSum) {
			sum += 1;
			edt.putInt("sum", sum);
			edt.commit();
			LogUtil.d(TAG, "当前播报次数" + sum);
			stopBroadcast();
			playBroadcast(getResources().getString(R.string.fm_open_tts), 0);
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					TsdHelper.setFMOn();
					TsdHelper.setFMFreq((int) (frequency * 100));
				}
			}, waitTime);
		} else {
			TsdHelper.setFMOn();
			TsdHelper.setFMFreq((int) (frequency * 100));
		}
	}

	/**
	 * 函数名称 : playBroadcast 功能描述 : 开始播报语音广播 参数及返回值说明：
	 */
	public void playBroadcast(String content, int pid) {
		Intent intent = new Intent();
		intent.setAction(CommonMessage.TTS_PLAY);
		// 要发送的内容
		intent.putExtra("package", getPackageName());
		intent.putExtra("id", pid);
		intent.putExtra("content", content);
		// 发送 一个无序广播
		sendBroadcast(intent);
	}

	/**
	 * 函数名称 : stopBroadcast 功能描述 : 主动停止语音播报广播 参数及返回值说明：
	 */
	public void stopBroadcast() {
		Intent intent = new Intent();
		intent.setAction(CommonMessage.TTS_CLEAR);
		// 发送 一个无序广播
		sendBroadcast(intent);
	}
}

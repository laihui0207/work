package com.tuyou.tsd.settings.trafficstats;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.settings.base.BaseActivity;

public class TrafficStatsService extends Service {
	private long sum = 0;
	static final String spfName = "trafficstats";
	private SharedPreferences spf;
	private SharedPreferences.Editor editor;
	private String TAG = "TrafficStatsService";
	private MyBinder myBinder = new MyBinder();
	/**
	 * 缓存当月总流量
	 */
	private long cacheFlow;
	/**
	 * 缓存流量的毫秒数
	 */
	private long cacheTime;
	/**
	 * 由于不支持清空TrafficStats流量数据，用来缓存本次结算时系统缓存流量数据
	 */
	private long overFlow;

	@Override
	public IBinder onBind(Intent intent) {
		return myBinder;
	}

	public class MyBinder extends Binder {

		public TrafficStatsService getService() {
			return TrafficStatsService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		init();
		initRedress();
	}

	/**
	 * 函数名称 : init 功能描述 : 初始化方法 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-7-23 上午11:47:11 修改人：wanghh 描述 ：
	 * 
	 */
	private void init() {
		IntentFilter filter = new IntentFilter();
		filter.setPriority(1000);
		filter.addAction(BaseActivity.FACTION);
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		registerReceiver(broadcastReceiver, filter);
		spf = getSharedPreferences(spfName, 0);
		editor = spf.edit();
		cacheFlow = spf.getLong("totalFlow", 0);
		// cacheFlow = 250 * 1024 * 1024;
		overFlow = spf.getLong("overFlow", 0);
		cacheTime = spf.getLong("cacheTime", 0);
		new Thread() {
			@Override
			public void run() {
				super.run();
				while (true) {
					try {
						getTotalFlow();
						sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY_COMPATIBILITY;
	}

	public void onDestroy() {
		Intent localIntent = new Intent();
		localIntent.setClass(this, TrafficStatsService.class);
		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	/**
	 * 函数名称 : getTotalFlow 功能描述 : 获取除wifi外上传下载的总流量 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-7-22 上午10:19:16 修改人：wanghh 描述 ：
	 * 
	 */
	public void getTotalFlow() {
		// 如果返回-1，代表不支持使用该方法，注意必须是2.2以上的
		long rx = TrafficStats.getMobileRxBytes();
		// 如果返回-1，代表不支持使用该方法，注意必须是2.2以上的
		long tx = TrafficStats.getMobileTxBytes();
		if (rx == -1 && tx == -1) {
			return;
		}
		sum = rx + tx;
		// 判断是不是该结算流量
		if (!isSameMonth(cacheTime)) {
			cacheTime = System.currentTimeMillis();
			editor.putLong("cacheTime", cacheTime);
			sendBroadcast(new Intent(BaseActivity.FACTION));
		}

		long total = sum + cacheFlow - overFlow;
		LogUtil.v(TAG, "总流量 = " + total);
		Intent intent = new Intent(BaseActivity.ACTION);
		intent.putExtra("total", total);
		sendBroadcast(intent);
	}

	/**
	 * 函数名称 : isSameMonth 功能描述 : 判断传进来的毫秒数与当前时间是不是同一个月份 参数及返回值说明：
	 * 
	 * @param cacheTime
	 *            上次结算流量的日期
	 * @return
	 * 
	 *         修改记录： 日期：2014-7-23 上午10:55:06 修改人：wanghh 描述 ：
	 * 
	 */
	public boolean isSameMonth(long cacheTime) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(cacheTime));
		int month1 = c.get(Calendar.MONTH);
		c.setTime(new Date(System.currentTimeMillis()));
		int month2 = c.get(Calendar.MONTH);
		// System.out.println("month1 = " + month1 + "     month2 = " + month2);
		return month1 == month2;
	}

	public void initRedress() {
		LogUtil.d(TAG, "初始化矫正方法");
		AlarmManager am = (AlarmManager) TrafficStatsService.this
				.getSystemService(Context.ALARM_SERVICE);
		long start = System.currentTimeMillis();
		long interval = 1000 * 3600 * 24 * 3; // The service will be run one
												// time per every week
		PendingIntent operation = PendingIntent.getBroadcast(
				TrafficStatsService.this, 0, new Intent(BaseActivity.FACTION),
				PendingIntent.FLAG_UPDATE_CURRENT);
		am.setRepeating(AlarmManager.RTC, start, interval, operation);
	}

	/**
	 * 发送短信矫正流量
	 * 
	 * @param phone
	 *            目标手机号码
	 * @param content
	 *            发送内容
	 */
	public void redress(String phone, String content) {
		// Date date = new Date(System.currentTimeMillis());
		// Date date1 = new Date(spf.getLong("sms_time", 0));
		long time = System.currentTimeMillis() - spf.getLong("sms_time", 0);
		long days = time / (1000 * 60 * 60 * 24);
		LogUtil.d(TAG, "2次矫正短信的时间差" + days);
		if (days > 7) {
			LogUtil.d(TAG, "发送流量矫正短信");
			editor.putLong("sms_time", System.currentTimeMillis());
			editor.commit();
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(phone, null, content, null, null);
		}
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.d(TAG, "action = " + action);
			if (action.equals(BaseActivity.FACTION)) {
				redress("10001", "108");
			} else if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					Object[] pdus = (Object[]) bundle.get("pdus");
					SmsMessage[] messages = new SmsMessage[pdus.length];
					for (int i = 0; i < pdus.length; i++) {
						messages[i] = SmsMessage
								.createFromPdu((byte[]) pdus[i]);
					}
					String strMsg = "", sender = "";
					for (SmsMessage message : messages) {
						strMsg = strMsg + message.getDisplayMessageBody();
						sender = message.getOriginatingAddress();
					}
					if ("10001".equals(sender)) {
						Pattern pattern = Pattern
								.compile("[+-]?([0-9]*\\.?[0-9]+|[0-9]+\\.?[0-9]*)([eE][+-]?[0-9]+)?");
						Matcher matcher = pattern.matcher(strMsg);
						List<String> list = new ArrayList<String>();
						while (matcher.find()) {
							list.add(matcher.group());
						}
						if (list.size() > 6) {
							ctrlFlow(list.get(6));
							Log.d(TAG, "矫正流量为" + list.get(6));
						}
					}
				}
				abortBroadcast();
			}
		}
	};

	public void ctrlFlow(String flow) {
		double d = Double.parseDouble(flow);
		long tolFlow = (long) (d * 1024 * 1024);
		LogUtil.d(TAG, "保存流量为" + tolFlow);
		overFlow = sum;
		cacheFlow = tolFlow;
		cacheTime = System.currentTimeMillis();
		editor.putLong("overFlow", overFlow);
		editor.putLong("cacheTime", cacheTime);
		editor.putLong("totalFlow", tolFlow);
		editor.commit();
	}

}

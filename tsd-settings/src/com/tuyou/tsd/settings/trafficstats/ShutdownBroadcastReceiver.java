package com.tuyou.tsd.settings.trafficstats;

import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.TrafficStats;

public class ShutdownBroadcastReceiver extends BroadcastReceiver {
	private Context context;
	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		if (!isWorked()) {
			// 关闭
			context.stopService(new Intent(context, TrafficStatsService.class));
		}
		SharedPreferences spf = context.getSharedPreferences("trafficstats", 0);
		SharedPreferences.Editor editor = spf.edit();
		long cacheFlow = spf.getLong("totalFlow", 0);
		long overFlow = spf.getLong("overFlow", 0);
		// 如果返回-1，代表不支持使用该方法，注意必须是2.2以上的
		long rx = TrafficStats.getMobileRxBytes();
		// 如果返回-1，代表不支持使用该方法，注意必须是2.2以上的
		long tx = TrafficStats.getMobileTxBytes();
		cacheFlow = cacheFlow + rx + tx - overFlow;
		editor.putLong("totalFlow", cacheFlow);
		editor.putLong("overFlow", 0);
		editor.commit();

	}

	/**
	 * 本方法判断一个service是否已经运行 函数名称 : isWorked 功能描述 : 参数及返回值说明：
	 * 
	 * @return 已运行返回true，未运行返回false
	 * 
	 *         修改记录： 日期：2014-7-22 下午1:57:57 修改人：wanghh 描述 ：
	 * 
	 */
	public boolean isWorked() {
		ActivityManager myManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager
				.getRunningServices(30);
		for (int i = 0; i < runningService.size(); i++) {
			if (runningService.get(i).service.getClassName().toString()
					.equals("com.tuyou.tsd.trafficstats.TrafficStatsService")) {
				return true;
			}
		}
		return false;
	}
}

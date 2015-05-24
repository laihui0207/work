package com.tuyou.tsd.settings.trafficstats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// 后边的XXX.class就是要启动的服务
//		Intent service = new Intent(context, TrafficStatsService.class);
//		service.putExtra("type", 1);
//		context.startService(service);
	}

}

package com.tuyou.tsd.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tuyou.tsd.common.util.LogUtil;

/**
 * BootCompleteReceiver
 * @author ruhai
 * 2014-8-12
 */
public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			LogUtil.d("BootCompleteReceiver", "启动 CoreService...");
			context.startService(new Intent(context, CoreService.class));
		}
	}

}

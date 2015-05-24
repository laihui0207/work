package com.tuyou.tsd.voice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.voice.service.VoiceAssistant;

/**
 * BootCompleteReceiver
 * @author ruhai
 * 2014-8-12
 */
public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			LogUtil.d("BootCompleteReceiver", "启动 VoiceAssistant...");
			context.startService(new Intent(context, VoiceAssistant.class));
		}
	}

}

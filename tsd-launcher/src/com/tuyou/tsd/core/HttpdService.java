package com.tuyou.tsd.core;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.core.httpd.HttpServer;

public class HttpdService extends Service {
	private static final String LOG_TAG = "HttpdService";
	private HttpServer server;
	
	private class DefaultEventBroadcastor implements HttpServer.EventBroadcastor {
		@Override
		public void broadcast(Intent intent) {
			sendBroadcast(intent);
		}
	}
	
	@Override
	public void onCreate() {
		LogUtil.v(LOG_TAG, "onCreate()....");
		
		server = new HttpServer(8081);
		server.setEventBroadcastor(new DefaultEventBroadcastor());
		
		try {
			server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		registerEventReceiver();
	}
	
	@Override
	public void onDestroy() {
		LogUtil.d(LOG_TAG, "onDestroy....");
		unregisterReceiver(server.getBroadcastReceiver());
		
		server.stop();
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.v(LOG_TAG, "onStartCommand()....");
		return START_STICKY;
	}

	public class LocalBinder extends Binder {
		public HttpdService getService() {
			return HttpdService.this;
		}
	}
	private IBinder binder = new LocalBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return binder;
	}

	private void registerEventReceiver() {
		IntentFilter filter = new IntentFilter();
		// System event
		filter.addAction(TSDEvent.Httpd.FEED_BACK);
		
		registerReceiver(server.getBroadcastReceiver(), filter);
	}
}

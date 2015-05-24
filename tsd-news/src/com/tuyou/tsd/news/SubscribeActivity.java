package com.tuyou.tsd.news;

import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.tuyou.tsd.common.network.AudioSubscription;
import com.tuyou.tsd.news.adapter.SubscribeAdapter;
import com.tuyou.tsd.news.base.MyBaseActivity;
import com.tuyou.tsd.news.comm.Contents;
import com.tuyou.tsd.news.service.AudioPlayerService;
import com.tuyou.tsd.news.service.IAudioPlayerService;
import com.tuyou.tsd.news.utils.OrderUtils;

public class SubscribeActivity extends MyBaseActivity implements OnClickListener{
	private IAudioPlayerService countService;
	private List<AudioSubscription> listAudio;
	private SubscribeAdapter adapter;
	private ListView subscribeList;
	private int sum;
	private MyBroadCast cast;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.subscribe);
		initCast();
		initService();
		initView();
		
	}
	
	private void initCast() {
		cast = new MyBroadCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Contents.ADD_NEWS_SUB);
		filter.addAction(Contents.REMOVE_NEWS_SUB);
		filter.addAction(Contents.CANNOT_NEWS_ADD);
		filter.addAction(AudioPlayerService.SUBSCRIPTION_STATUS);
		filter.addAction(Contents.KILL_ALL_APP1);
		filter.addAction(Contents.KILL_ALL_APP2);
		registerReceiver(cast, filter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			if(adapter!=null){
				adapter.cancelAllTasks();
				adapter = null;
			}
			if(serviceConnection!=null){
				unbindService(serviceConnection);
				serviceConnection = null;
			}
			unregisterReceiver(cast);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initService() {
		this.startService(new Intent("com.tuyou.tsd.news.service.AudioPlayerService"));
		this.bindService( new Intent("com.tuyou.tsd.news.service.AudioPlayerService"),this.serviceConnection, BIND_AUTO_CREATE);
	}

	private void initView() {
		subscribeList = (ListView) findViewById(R.id.subscribe_list);
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.can_not_bg).setOnClickListener(this);
		findViewById(R.id.can_not_btn).setOnClickListener(this);
	}
	
	private ServiceConnection serviceConnection = new ServiceConnection() { 
		 
		@Override  
		public void onServiceConnected(ComponentName name, IBinder service) {  
		   countService = (IAudioPlayerService) service;  
		   Message msg = new Message();
		   msg.what = 5;
		   ViewHandler.sendMessage(msg);
		} 
		  
		@Override  
		public void onServiceDisconnected(ComponentName name) { 
		   countService = null;  
		} 
	};
	
	
	@SuppressLint("HandlerLeak")
	private Handler ViewHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 5:
				showData();
				break;

			default:
				break;
			}
		}

	};
	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.can_not_bg:
		case R.id.can_not_btn:
			findViewById(R.id.can_not_bg).setVisibility(View.GONE);
			try {
				subscribeList.smoothScrollToPosition(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			break;

		default:
			break;
		}
	}
	
	private void showData() {
		try {
			sum = 0;
			listAudio =  countService.getSubscriptionList("news");
			if(listAudio!=null && listAudio.size()>0){
				for(AudioSubscription audio : listAudio){
					if(audio.status==1){
						sum++;
					}
				}
				OrderUtils order = new OrderUtils();
				Collections.sort(listAudio, order);
				showTextNum(String.format(getResources().getString(R.string.sub_sum), sum));
				adapter = new SubscribeAdapter(SubscribeActivity.this, listAudio, subscribeList);
				if(sum>=Contents.ADD_BIG_NEWS_NUM){
					adapter.setClick(false);
				}else{
					adapter.setClick(true);
				}
				subscribeList.setAdapter(adapter);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class MyBroadCast extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent it) {
			if(it.getAction().equals(Contents.ADD_NEWS_SUB)){
				if(sum < 6){
					sum++;
					if(sum<=Contents.ADD_BIG_NEWS_NUM){
						showTextNum(String.format(getResources().getString(R.string.sub_sum), sum));
						countService.addSubscription(listAudio.get(it.getExtras().getInt("index")));
						listAudio.get(it.getExtras().getInt("index")).status = 1;
					}
					if(sum>=Contents.ADD_BIG_NEWS_NUM){
						Intent itCannot = new Intent();
						itCannot.setAction(Contents.ADD_NEWS_SUB_CANNOT);
						sendBroadcast(itCannot);
						adapter.setClick(false);
					}else{
						adapter.setClick(true);
					}
					adapter.setCanClick(true);
					adapter.notifyDataSetChanged();
				}
			}else if(it.getAction().equals(Contents.REMOVE_NEWS_SUB)){
				if(sum>0){
					sum--;
					showTextNum(String.format(getResources().getString(R.string.sub_sum), sum));
					countService.deleteSubscription(listAudio.get(it.getExtras().getInt("index")));
					listAudio.get(it.getExtras().getInt("index")).status = 0;
					if(sum>=Contents.ADD_BIG_NEWS_NUM){
						adapter.setClick(false);
					}else{
						adapter.setClick(true);
					}
					adapter.setCanClick(true);
					adapter.notifyDataSetChanged();
				}
			}else if(it.getAction().equals(Contents.CANNOT_NEWS_ADD)){
				adapter.setCanClick(true);
				findViewById(R.id.can_not_bg).setVisibility(View.VISIBLE);
			}else if(it.getAction().equals(AudioPlayerService.SUBSCRIPTION_STATUS)){
				showData();
			}else if(it.getAction().equals(Contents.KILL_ALL_APP1)||it.getAction().equals(Contents.KILL_ALL_APP2)){
				finish();
			}
		}
	}
	
	private void showTextNum(String str){
		SpannableString ss = new SpannableString(str); 
		ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.white)), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); 
		ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.blue)), 4, str.length()-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.white)), str.length()-1, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); 
		TextView hadSubscribe = (TextView) findViewById(R.id.had_subscribe);
		hadSubscribe.setText(ss);
	}

}

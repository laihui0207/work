package com.tuyou.tsd.audio;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.tuyou.tsd.audio.adapter.MusicPlayadapter;
import com.tuyou.tsd.audio.base.MyBaseActivity;
import com.tuyou.tsd.audio.comm.Contents;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.base.CommonSleep;
import com.tuyou.tsd.common.network.AudioItem;

public class MusicListActivity extends MyBaseActivity implements OnClickListener,OnItemClickListener{
	
	private ArrayList<AudioItem> list;
	private MusicPlayadapter adapter;
	
	private PlayMusicCast cast;
	
	private int playIndex = 0;
	private ListView musicPlayList;
	
//	private CommonSleep commonSleep = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_play);
		initCast();
		initView();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
//		commonSleep = new CommonSleep(this);
//		commonSleep.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
//		if (commonSleep != null) {
//			commonSleep.stop();
//		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		sendBroadcast(new Intent(TSDEvent.Navigation.IDLE_NAV_UPDATE));
//		if (commonSleep != null) {
//			commonSleep.update();
//		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		sendBroadcast(new Intent(TSDEvent.Navigation.IDLE_NAV_UPDATE));
//		if (commonSleep != null) {
//			commonSleep.update();
//		}
		return super.dispatchTouchEvent(ev);
	}

	private void initCast() {
		cast = new PlayMusicCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.tuyou.tsd.audio.playmusicnext");
		filter.addAction(TSDEvent.System.ACC_OFF);
		filter.addAction(Contents.ACTION_BUTTON);
		filter.addAction(Contents.KILL_ALL_APP1);
		filter.addAction(Contents.KILL_ALL_APP2);
		registerReceiver(cast, filter);
	}

	private void initView() {
		list = getIntent().getParcelableArrayListExtra("music_list");
		TextView musicPlayTitle = (TextView) findViewById(R.id.music_play_title);
		musicPlayTitle.setText(getIntent().getExtras().getString("music_title"));
		musicPlayList = (ListView) findViewById(R.id.music_play_list);
		String item = getIntent().getExtras().getString("music_now");
		if(item!=null){
			setPlayNow(item);
		}
		adapter = new MusicPlayadapter(this, list,musicPlayList);
		musicPlayList.setAdapter(adapter);
		musicPlayList.setOnItemClickListener(this);
		
		try {
			musicPlayList.setSelection(playIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;

		default:
			break;
		}
	}
	
	private void setPlayNow(String itemName){
		try {
			int size = list.size();
			for(int i=0;i<size;i++){
				if(list.get(i).item.equals(itemName)){
					list.get(i).isPlay = true;
					playIndex = i;
				}else{
					list.get(i).isPlay = false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Intent it = new Intent();
		it.setAction(Contents.MUSICPLAY_CATEGORY);
		it.putExtra("music_category", getIntent().getExtras().getString("music_category"));
		it.putExtra("music_list", list);
		it.putExtra("music_index", arg2);
		sendBroadcast(it);
	}
	
	private class PlayMusicCast extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if(intent.getAction().equals("com.tuyou.tsd.audio.playmusicnext")){
					AudioItem item = intent.getExtras().getParcelable("music_now_category");
					int size = list.size();
					for(int i=0;i<size;i++){
						if(list.get(i).item.equals(item.item)){
							list.get(i).isPlay = true;
						}else{
							list.get(i).isPlay = false;
						}
					}
					adapter.notifyDataSetChanged();
				}else if(intent.getAction().equals(TSDEvent.System.ACC_OFF)){
					finish();
				}else if(intent.getAction().equals(Contents.ACTION_BUTTON)){
					int buttonId = intent.getIntExtra(Contents.INTENT_BUTTONID_TAG, 0);
					switch (buttonId) {
					case Contents.BUTTON_CLEAN_ID:
						finish();
						break;
					default:
						break;
					}
				
				}else if(intent.getAction().equals(Contents.KILL_ALL_APP1)||intent.getAction().equals(Contents.KILL_ALL_APP2)){
					finish();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(cast);
	}
	
}

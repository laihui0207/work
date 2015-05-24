package com.tuyou.tsd.podcast;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.network.AudioItem;
import com.tuyou.tsd.podcast.adapter.MusicPlayadapter;
import com.tuyou.tsd.podcast.base.MyBaseActivity;
import com.tuyou.tsd.podcast.comm.Contents;

public class MusicListActivity extends MyBaseActivity implements OnClickListener,OnItemClickListener{
	
	private ArrayList<AudioItem> list;
	private MusicPlayadapter adapter;
	
	private PlayMusicCast cast;
	private int playIndex = 0;
	private ListView musicPlayList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_play);
		initCast();
		initView();
	}

	private void initCast() {
		cast = new PlayMusicCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Contents.PLAY_MUSIC_NEXT);
		filter.addAction(CommonMessage.EVT_ACC_OFF);
		filter.addAction(Contents.ACTION_BUTTON);
		
		filter.addAction(Contents.KILL_ALL_APP1);
		filter.addAction(Contents.KILL_ALL_APP2);
		
		registerReceiver(cast, filter);
	}

	private void initView() {
		TextView back = (TextView) findViewById(R.id.back);
		back.setOnClickListener(this);
		list = getIntent().getParcelableArrayListExtra("music_list");
		TextView musicPlayTitle = (TextView) findViewById(R.id.music_play_title);
		musicPlayTitle.setText(getIntent().getExtras().getString("music_title"));
		musicPlayList = (ListView) findViewById(R.id.music_play_list);
		String item = getIntent().getExtras().getString("music_now");
		if(item!=null){
			setPlayNow(item);
		}
		adapter = new MusicPlayadapter(this, list,musicPlayList,getIntent().getExtras().getBoolean("is_hot"));
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
//		,String musicNowCategory
//		if(musicNowCategory.equals(getIntent().getExtras().getString("music_category"))){
//			
//		}
		int size = list.size();
		for(int i=0;i<size;i++){
			if(list.get(i).item.equals(itemName)){
				list.get(i).isPlay = true;
				playIndex = i;
			}else{
				list.get(i).isPlay = false;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Intent it = new Intent();
		it.setAction(Contents.PLAY_PODCAST);
		it.putExtra("music_category", getIntent().getExtras().getString("music_category"));
		it.putExtra("music_list", list);
		it.putExtra("music_index", arg2);
		sendBroadcast(it);
	}
	
	private class PlayMusicCast extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if(intent.getAction().equals(Contents.PLAY_MUSIC_NEXT)){
					AudioItem item = intent.getExtras().getParcelable("music_now_category");
					int size = list.size();
					for(int i=0;i<size;i++){
						if(list.get(i).item.equals(item.item)){
							list.get(i).isPlay = true;
							playIndex = i;
						}else{
							list.get(i).isPlay = false;
						}
					}
					adapter.notifyDataSetChanged();
				}else if(intent.getAction().equals(CommonMessage.EVT_ACC_OFF)){
					finish();
				}else if(intent.getAction().equals(Contents.ACTION_BUTTON)){
					int buttonId = intent.getIntExtra(Contents.INTENT_PODCAST_BUTTONID_TAG, 0);
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

package com.tuyou.tsd.news;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.network.AudioCategory;
import com.tuyou.tsd.common.network.AudioItem;
import com.tuyou.tsd.common.network.AudioSubscription;
import com.tuyou.tsd.news.adapter.ImageAdapter;
import com.tuyou.tsd.news.base.MyBaseActivity;
import com.tuyou.tsd.news.comm.Contents;
import com.tuyou.tsd.news.service.AudioPlayerService;
import com.tuyou.tsd.news.service.IAudioPlayerService;
import com.tuyou.tsd.news.utils.Notify;
import com.tuyou.tsd.news.weight.CoverFlow;

public class MusicActivity extends MyBaseActivity implements OnClickListener,OnTouchListener,OnItemClickListener,OnItemSelectedListener{
	
	private TextView title;
	private ProgressBar bar;
	private LinearLayout musicListLayout;
	private ImageView musicList;
	private ImageView musicListPoint;
	private List<AudioCategory> list;
	private IAudioPlayerService countService;
	private ImageAdapter imageAdapter;
	private CoverFlow flow;
	private GetRecCast cast;
	private int index = 0;
	private AudioItem item;
	private String musicNowCategory = "";
	private boolean isTouch = false;
	
	private LinearLayout musicLove;
	
	public NotificationManager mNotificationManager;
	private ImageView musicState;
	private ImageView musicLoading;
	private List<AudioSubscription> listAudio;
//	private ImageView musicNull;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i = new Intent();
		i.setAction(Contents.KILL_ALL_APP3);
		sendBroadcast(i);
		setContentView(R.layout.main);
		initNot();
		initService();
		initView();
		initCast();
		Intent it = new Intent();
		it.setAction(TSDEvent.Audio.APP_STARTED);
		sendBroadcast(it);
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
		
	}
	
	private void initNot() {
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}
	
	private void playItemNow(){
		if(countService!=null){
			item = countService.getPlayingAudio();
			showNow();
		}
	}

	private void playItemNow(AudioItem audioItem){
		try {
			item = audioItem;
		} catch (Exception e) {
			e.printStackTrace();
		}
		showNow();
	}
	private void showNow() {
		Message msg = new Message();
		msg.what = 7;
		textViewHandler.sendMessage(msg);
	}
	
	private void setColor(TextView addText,boolean isFav){
		int size = 0;
		try {
			if(list!=null){
				if(listAudio!=null){
					for(AudioSubscription item : listAudio){
						if(countService.isSubscription(item.album)){
							size ++;
						}
					}
				}
				if(size>=Contents.ADD_BIG_NEWS_NUM){
					addText.setTextColor(getResources().getColor(R.color.gray));
					try {
						Notify.showButtonNotify(list.get(index),item, this, countService.isPlaying(), isFav,6);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else{
					addText.setTextColor(getResources().getColor(R.color.blue));
					try {
						Notify.showButtonNotify(list.get(index),item, this, countService.isPlaying(), isFav,0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else{
				try {
					Notify.showButtonNotify(list.get(index),item, this, countService.isPlaying(), isFav,0);
				} catch (Exception e) {
					e.printStackTrace();
				}
				addText.setTextColor(getResources().getColor(R.color.blue));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setFavourite(boolean isFavourite){
		if(item!=null){
			if(isFavourite){
				LinearLayout musicLove = (LinearLayout) findViewById(R.id.music_love); 
				musicLove.setBackgroundResource(R.drawable.subscribe_had);
				TextView addText = (TextView) findViewById(R.id.add_text);
				addText.setText(R.string.re_love);
				addText.setTextColor(getResources().getColor(R.color.gray));
				musicLove.setEnabled(false);
				musicLove.setOnClickListener(null);
			}else{
				TextView addText = (TextView) findViewById(R.id.add_text);
				addText.setText(R.string.add_love);
				LinearLayout musicLove = (LinearLayout) findViewById(R.id.music_love); 
				musicLove.setBackgroundResource(R.drawable.subscribe_white_select);
				musicLove.setEnabled(true);
				musicLove.setOnClickListener(this);
				setColor(addText,isFavourite);
				musicLove.setOnClickListener(MusicActivity.this);
			}
		}
	
	}
	

	@SuppressWarnings("deprecation")
	private void initCast() {
		cast = new GetRecCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AudioPlayerService.DATA_REFRESH);
		filter.addAction(AudioPlayerService.CHACHE_PROGRESS);
		filter.addAction(AudioPlayerService.PLAY_PROGRESS);
		filter.addAction(AudioPlayerService.NEXT_AUDIO);
		filter.addAction(AudioPlayerService.HEARD_LIST);
		filter.addAction(Contents.PLAY_NEWS_PODCAST);
		filter.addAction(CommonMessage.EVT_ACC_OFF);
		filter.addAction(Contents.ACTION_NEWS_BUTTON);
		filter.addAction(Contents.MUSICPLAY_STATE_NEWS_PLAY);
		filter.addAction(Contents.MUSICPLAY_STATE_NEWS_PAUSE);
		filter.addAction(TSDEvent.Audio.PAUSE);
		filter.addAction(TSDEvent.Audio.RESUME);
		filter.addAction(AudioPlayerService.PLAY_STATUS);
		filter.addAction(Contents.ADD_NEWS_SUB_CANNOT);
		filter.addAction(Contents.KILL_ALL_APP1);
		filter.addAction(Contents.KILL_ALL_APP2);
		filter.addAction(Contents.DATA_REFRESH_PUSH);
		filter.addAction(TSDEvent.System.HARDKEY3_PRESSED);
		registerReceiver(cast, filter);
	}

	private void initService() {
		MusicActivity.this.startService(new Intent("com.tuyou.tsd.news.service.AudioPlayerService"));
		MusicActivity.this.bindService( new Intent("com.tuyou.tsd.news.service.AudioPlayerService" ), MusicActivity.this.serviceConnection, BIND_AUTO_CREATE);
	}

	private void initView() {
		title = (TextView) findViewById(R.id.music_list_title);
		musicListLayout = (LinearLayout) findViewById(R.id.music_list_layout);
		musicList = (ImageView) findViewById(R.id.music_list);
		musicListPoint = (ImageView) findViewById(R.id.music_list_point);
		initListener();
		bar = (ProgressBar) findViewById(R.id.music_play_progress);
		musicState = ((ImageView)findViewById(R.id.music_play));
		musicLoading = (ImageView) findViewById(R.id.music_loading);
		findViewById(R.id.music_love).setOnClickListener(MusicActivity.this);
		setOnClick(R.id.music_prew);
		setOnClick(R.id.music_play);
		setOnClick(R.id.music_next);
		
		flow = (CoverFlow) findViewById(R.id.cover_flow);
		flow.setOnItemClickListener(MusicActivity.this);
		flow.setOnItemSelectedListener(MusicActivity.this);
		flow.setOnTouchListener(MusicActivity.this);
		
		musicLove = (LinearLayout) findViewById(R.id.subscribe);
		musicLove.setOnClickListener(MusicActivity.this);
		
		findViewById(R.id.can_not_bg).setOnClickListener(MusicActivity.this);
		findViewById(R.id.can_not_btn).setOnClickListener(MusicActivity.this);
		
	}
	
	void setIndex()
	{
		AudioCategory playing=countService.getPlayingCatogory();
		if((list!=null)&&(playing!=null))
		{	
			for(int i=0;i<list.size();i++)
			{
				if(playing.category.equals(list.get(i).category))
				{
					index=i;
					break;
				}
			}
			
		}
	}
	
	@SuppressWarnings("deprecation")
	private void getData(){
		try {
//			list = countService.getCategoryList();
//			if(list != null&&list.size()>0){
//				setIndex();
//				flow.setSelection(index);
////				Message msg = textViewHandler.obtainMessage();
////				msg.what= 8;
////				textViewHandler.sendMessage(msg);
//				if(imageAdapter==null){
//					imageAdapter = new ImageAdapter(this,list,flow);
//					flow.setAdapter(imageAdapter);
//					flow.setAnimationDuration(800);
//				}else{
//					imageAdapter.setList(list);
//					imageAdapter.notifyDataSetChanged();
//				}
//				flow.setSelection(index);
//				try {
//					title.setText(list.get(index).name);
//					setTitleEnable(index);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				if(countService.isSubscription(item.albumId)){
//					setFavourite(true);
//				}else{
//					setFavourite(false);
//				}
//				
////			}else{
////				Message msg = textViewHandler.obtainMessage();
////				msg.what= 9;
////				textViewHandler.sendMessage(msg);
//			}
			if(listAudio==null||listAudio.size()==0){
				listAudio =  countService.getSubscriptionList("news");
			}
			
			Message msg = textViewHandler.obtainMessage();
			msg.what = 6;
			textViewHandler.sendMessageDelayed(msg,1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setViewEnable(int id,boolean isEnable){
		findViewById(id).setEnabled(isEnable);
	}
	
	
	private void setOnClick(int id){
		ImageView view = (ImageView) findViewById(id);
		view.setOnClickListener(MusicActivity.this);
	}
	
	private void initListener() {
		setAlllistener(title);
		setAlllistener(musicListLayout);
		setAlllistener(musicList);
		setAlllistener(musicListPoint);
	}
	
	private void setAlllistener(View view){
		view.setOnClickListener(MusicActivity.this);
		view.setOnTouchListener(MusicActivity.this);
	}
	
	
	private void gotoMusicList(){
		try {
			ArrayList<AudioItem> listAudio = list.get(index).item;
			Intent it = new Intent();
			it.setClass(MusicActivity.this, MusicListActivity.class);
			if(list.size()>=2){
				if(index==1){
					it.putExtra("is_hot", true);
				}else{
					it.putExtra("is_hot", false);
				}
			}else{
				it.putExtra("is_hot", true);
			}
			it.putExtra("music_category", list.get(index).category);
			it.putExtra("music_list", listAudio);
			it.putExtra("music_title", list.get(index).name);
				if(item!=null&&item.item!=null){
					it.putExtra("music_now", item.item);
					it.putExtra("music_now_category", musicNowCategory);
				}
			try {
				imageAdapter.cancelAllTasks();
			} catch (Exception e) {
				e.printStackTrace();
			}
			startActivity(it);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.music_list_title:
		case R.id.music_list_layout:
		case R.id.music_list:
		case R.id.music_list_point:
			gotoMusicList();
			break;
		case R.id.music_prew:
			countService.prew();
			break;
		case R.id.music_next:
			countService.next();
			break;
		case R.id.music_play:
			playMusicRestar();
			break;
		case R.id.music_love:
			try {
				int size = 0;
				if(listAudio!=null){
					for(AudioSubscription cate : listAudio){
						if(countService.isSubscription(cate.album)){
							size ++;
						}
					}
					if(size>=Contents.ADD_BIG_NEWS_NUM){
						findViewById(R.id.can_not_bg).setVisibility(View.VISIBLE);
					}else{
						setLove();
					}
				}else{
					setLove();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.subscribe:
			Intent it = new Intent();
			it.setClass(MusicActivity.this, SubscribeActivity.class);
			startActivity(it);
			break;
		case R.id.can_not_bg:
		case R.id.can_not_btn:
			findViewById(R.id.can_not_bg).setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}
	
	private void addLove() {
		try {
			int size = 0;
			if(list!=null){
				if(listAudio!=null){
					for(AudioSubscription cate : listAudio){
						if(countService.isSubscription(cate.album)){
							size ++;
						}
					}
				}
				if(size>=Contents.ADD_BIG_NEWS_NUM){
					findViewById(R.id.can_not_bg).setVisibility(View.VISIBLE);
				}else{
					setLove();
				}
			}else{
				setLove();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setLove(){
		try {
			setlove();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setlove(){
		if(item!=null){
			if(!countService.isSubscription(item.albumId)){
				countService.addSubscription(item.albumId);
				setFavourite(true);
				try {
					int size = 0;
					if(listAudio!=null){
						for(AudioSubscription cate : listAudio){
							if(countService.isSubscription(cate.album)){
								size ++;
							}
						}
					}
					if(size>=Contents.ADD_BIG_NEWS_NUM){
						Notify.showButtonNotify(list.get(index),item, MusicActivity.this, countService.isPlaying(), true,6);
					}else{
						Notify.showButtonNotify(list.get(index),item, MusicActivity.this, countService.isPlaying(), true,0);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private void playMusicRestar(){
		try {			
			if(countService.isPlaying()){				
				countService.pause();				
				musicState.setImageResource(R.drawable.music_pause_select);				
				try {					
					int size = 0;					
					if(listAudio!=null){						
						for(AudioSubscription cate : listAudio){							
							if(countService.isSubscription(cate.album)){								
								size ++;							
								}						
							}					
						}					
					if(size>=Contents.ADD_BIG_NEWS_NUM){						
						Notify.showButtonNotify(list.get(index),item, MusicActivity.this, false, countService.isSubscription(item.albumId),6);					
						}else{						
							Notify.showButtonNotify(list.get(index),item, MusicActivity.this, false, countService.isSubscription(item.albumId),0);					
							}									
					} catch (Exception e) {					
						e.printStackTrace();			
						}		
				}else if(list!=null&&list.size()>0){	
					countService.rusume();			
					musicState.setImageResource(R.drawable.music_play_select);		
					try {					
						int size = 0;	
						if(listAudio!=null){		
							for(AudioSubscription cate : listAudio){			
								if(countService.isSubscription(cate.album)){	
									size ++;						
									}					
								}			
							}				
						if(size>=Contents.ADD_BIG_NEWS_NUM){	
							Notify.showButtonNotify(list.get(index),item, MusicActivity.this, true, countService.isSubscription(item.albumId),6);				
							}else{					
								Notify.showButtonNotify(list.get(index),item, MusicActivity.this, true, countService.isSubscription(item.albumId),0);	
								}										
						} catch (Exception e) {			
							e.printStackTrace();		
							}	
					}	
			} catch (Exception e) {		
				e.printStackTrace();
			}
	}
	

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
		case R.id.music_list_title:
		case R.id.music_list_layout:
		case R.id.music_list:
		case R.id.music_list_point:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				title.setTextColor(getResources().getColor(R.color.blue));
				musicList.setImageResource(R.drawable.music_list_click);
				musicListPoint.setImageResource(R.drawable.music_list_point_click); 
			}else if(event.getAction()==MotionEvent.ACTION_UP){
				title.setTextColor(getResources().getColor(R.color.white));
				musicList.setImageResource(R.drawable.music_list_unclick);
				musicListPoint.setImageResource(R.drawable.music_list_point_unclick);
			}
			break;
		case R.id.cover_flow:
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				isTouch = false;
				textViewHandler.removeMessages(2);
				break;
			case MotionEvent.ACTION_UP:
				isTouch = true;
				if(event.getAction()==MotionEvent.ACTION_UP){
					Message msg = new Message();
					msg.what = 1;
					textViewHandler.sendMessageDelayed(msg, 600);
					
					sendMessagePlayMusic();
				}
				break;
			default:
				break;
			}
			break;
		}
		return false;
	}


	
	private ServiceConnection serviceConnection = new ServiceConnection() { 
		 
			@Override  
			public void onServiceConnected(ComponentName name, IBinder service) {  
			   countService = (IAudioPlayerService) service;  
			   Message msg = new Message();
			   msg.what = 5;
			   textViewHandler.sendMessage(msg);
			} 
			  
			@Override  
			public void onServiceDisconnected(ComponentName name) { 
			   countService = null;  
			} 
	}; 
	
	@SuppressLint("NewApi")
	class GetDataTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			getData();
			playItemNow();
			return null;
		}
		
	}
	
	class GetRecCast extends BroadcastReceiver{

		@SuppressWarnings("deprecation")
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(AudioPlayerService.PLAY_STATUS)){
				switch (intent.getExtras().getInt("status")) {
				case 1:
					musicState.setImageResource(R.drawable.music_play_select);
					musicState.setEnabled(true);
					musicLoading.clearAnimation();
					musicLoading.setVisibility(View.GONE);
					try {
						int size = 0;
						if(listAudio!=null){
							for(AudioSubscription cate : listAudio){
								if(countService.isSubscription(cate.album)){
									size ++;
								}
							}
						}
						if(size>=Contents.ADD_BIG_NEWS_NUM){
							Notify.showButtonNotify(list.get(index),item, MusicActivity.this, true, countService.isSubscription(item.albumId),6);
						}else{
							Notify.showButtonNotify(list.get(index),item, MusicActivity.this, true, countService.isSubscription(item.albumId),0);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					break;
				case 2:
					musicState.setEnabled(false);
					musicState.setImageResource(R.drawable.unplay);
					musicLoading.setVisibility(View.VISIBLE);
					Animation animation = AnimationUtils.loadAnimation(MusicActivity.this, R.anim.loading_rotate); 
					musicLoading.startAnimation(animation);
					break;
				case 3:
					musicState.setImageResource(R.drawable.music_pause_select);
					musicState.setEnabled(true);
					musicLoading.clearAnimation();
					musicLoading.setVisibility(View.GONE);
					try {
						int size = 0;
						if(listAudio!=null){
							for(AudioSubscription cate : listAudio){
								if(countService.isSubscription(cate.album)){
									size ++;
								}
							}
						}
						if(size>=Contents.ADD_BIG_NEWS_NUM){
							Notify.showButtonNotify(list.get(index),item, MusicActivity.this, false, countService.isSubscription(item.albumId),6);
						}else{
							Notify.showButtonNotify(list.get(index),item, MusicActivity.this, false, countService.isSubscription(item.albumId),0);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
				}
			}else 
			if(intent.getAction().equals(AudioPlayerService.DATA_REFRESH)){
				
				getData();
				try {
					setFavourite(countService.isSubscription(item.albumId));
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(imageAdapter==null){
					imageAdapter = new ImageAdapter(MusicActivity.this, list, flow);
				}
				imageAdapter.notifyDataSetChanged();
			}else 
			if(intent.getAction().equals(AudioPlayerService.CHACHE_PROGRESS)){
				bar.setSecondaryProgress(intent.getExtras().getInt("chacheprogress"));
			}else
			if(intent.getAction().equals(AudioPlayerService.PLAY_PROGRESS)){
				bar.setProgress(intent.getExtras().getInt("progress"));
			}else
			if(intent.getAction().equals(AudioPlayerService.NEXT_AUDIO)){
				bar.setSecondaryProgress(0);
				bar.setProgress(0);
				playItemNow((AudioItem)intent.getParcelableExtra("audio"));
			}else 
			if(intent.getAction().equals(Contents.PLAY_NEWS_PODCAST)){
				ArrayList<AudioItem> listChild = intent.getParcelableArrayListExtra("music_list");
				countService.StartPlayer(intent.getExtras().getString("music_category"), listChild.get(intent.getExtras().getInt("music_index")).item);
			}
			else
			if(intent.getAction().equals(CommonMessage.EVT_ACC_OFF)){
				stopService(new Intent(MusicActivity.this,AudioPlayerService.class));
				finish();
			}else
			if(intent.getAction().equals(Contents.MUSICPLAY_STATE_NEWS_PAUSE)){
				musicState.setImageResource(R.drawable.music_pause_select);
			}else
			if(intent.getAction().equals(Contents.MUSICPLAY_STATE_NEWS_PLAY)){
				musicState.setImageResource(R.drawable.music_play_select);
			}else
			
			if(intent.getAction().equals(Contents.ACTION_NEWS_BUTTON)){
				int buttonId = intent.getIntExtra(Contents.INTENT_NEWS_BUTTONID_TAG, 0);
				switch (buttonId) {
				case Contents.BUTTON_CLEAN_ID:
					finish();
					break;
				case Contents.BUTTON_PLAY_ID:
					Message msgplay = new Message();
					msgplay.what = 3;
					textViewHandler.sendMessageDelayed(msgplay, 400);
					break;
				case Contents.BUTTON_LOVE_ID:
					Message msglove = new Message();
					msglove.what = 4;
					textViewHandler.sendMessageDelayed(msglove, 400);
					break;
				}
			}else if(intent.getAction().equals(Contents.ADD_NEWS_SUB_CANNOT)){
				try {
					Notify.showButtonNotify(list.get(index),item, MusicActivity.this, countService.isPlaying(), countService.isSubscription(item.albumId),6);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(intent.getAction().equals(Contents.KILL_ALL_APP1)||intent.getAction().equals(Contents.KILL_ALL_APP2)){
				finish();
			}else if(intent.getAction().equals(Contents.DATA_REFRESH_PUSH)){
				list = countService.getCategoryListPush();
				setAdapter();
			}else if(intent.getAction().equals(TSDEvent.System.HARDKEY3_PRESSED)){
				Message msglove = new Message();
				msglove.what =8;
				textViewHandler.sendMessageDelayed(msglove, 400);
			}
		}
	}
	
	private void setAdapter(){
		if(list != null&&list.size()>0){
			setViewEnable(R.id.music_love, true);
			setViewEnable(R.id.music_prew, true);
			setViewEnable(R.id.music_next, true);
			setViewEnable(R.id.music_play, true);
			countService.setList(listAudio);
			setIndex();
			if(imageAdapter==null){
				imageAdapter = new ImageAdapter(MusicActivity.this,list,flow);
				flow.setAdapter(imageAdapter);
				flow.setAnimationDuration(800);
			}else{
				imageAdapter.setList(list);
				imageAdapter.notifyDataSetChanged();
			}
			flow.setSelection(index);
			try {
				title.setText(list.get(index).name);
				setTitleEnable(index);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(item!=null){
				if(countService.isSubscription(item.albumId)){
					setFavourite(true);
					try {
						int sizeI = 0;
						if(listAudio!=null){
							for(AudioSubscription cate : listAudio){
								if(countService.isSubscription(cate.album)){
									sizeI ++;
								}
							}
						}
						if(sizeI>=Contents.ADD_BIG_NEWS_NUM){
							Notify.showButtonNotify(list.get(index),item, MusicActivity.this, countService.isPlaying(), true,6);
						}else{
							Notify.showButtonNotify(list.get(index),item, MusicActivity.this, countService.isPlaying(), true,0);
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}else{
					setFavourite(false);
					try {
						int sizeI = 0;
						if(listAudio!=null){
							for(AudioSubscription cate : listAudio){
								if(countService.isSubscription(cate.album)){
									sizeI ++;
								}
							}
						}
						if(sizeI>=Contents.ADD_BIG_NEWS_NUM){
							Notify.showButtonNotify(list.get(index), item,MusicActivity.this, countService.isPlaying(), false,6);
						}else{
							Notify.showButtonNotify(list.get(index), item,MusicActivity.this, countService.isPlaying(), false,0);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}
			flow.setVisibility(View.VISIBLE);
			imageAdapter.notifyDataSetChanged();
			flow.setAdapter(imageAdapter);
			flow.setSelection(index);
		}else{
			setViewEnable(R.id.music_love, false);
			setViewEnable(R.id.music_prew, false);
			setViewEnable(R.id.music_next, false);
			setViewEnable(R.id.music_play, false);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
		unregisterReceiver(cast);
		Intent it = new Intent();
		it.setAction(TSDEvent.Audio.APP_STOPPED);
		sendBroadcast(it);
	}
	
	private void isPlayMusic(){
		try {
			if(countService.getPlayingCatogory()!=null&&list.get(index).category!=null&&countService.getPlayingCatogory().category.equals(list.get(index).category)){
				int size = list.get(index).item.size();
				List<String> listItem = new ArrayList<String>();
				listItem.clear();
				for(int i = 0;i<size;i++){
					listItem.add(list.get(index).item.get(i).item);
				}
				try {
					if(item!=null&&item.item!=null&&listItem.contains(countService.getPlayingAudio().item)){
						if(!countService.getPlayingCatogory().category.equals(list.get(index).category)){
							playMusicRestar();
						}
					}else{
						countService.StartPlayer(list.get(index).category, null);
					}
				} catch (Exception e) {
					e.printStackTrace();
					countService.StartPlayer(list.get(index).category, null);
				}
			}else{
				try {
					countService.StartPlayer(list.get(index).category, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(index==arg2){
//			gotoMusicList();
			playMusicRestar();
		}else{
			index=arg2;
		}
	}
	
	public void sendMessagePlayMusic(){
		Message msg = new Message();
		msg.what = 2;
		textViewHandler.sendMessageDelayed(msg, TSDConst.PLAY_SPACE);
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		index = arg2%list.size();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}
	
	@SuppressLint("HandlerLeak")
	private Handler textViewHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				try {
					title.setText(list.get(index).name);
					setTitleEnable(index);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 2:
				if(isTouch){
					isPlayMusic();
				}
				break;
			case 3:
				if(countService.isPlaying()){
					musicState.setImageResource(R.drawable.music_play_select);
				}else{
					musicState.setImageResource(R.drawable.music_pause_select);
				}
				break;
			case 4:
				int size = 0;
				if(listAudio!=null){
					for(AudioSubscription cate : listAudio){
						if(countService.isSubscription(cate.album)){
							size ++;
						}
					}
				}
				if(size<6){
					try {
						setLove();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case 5:
				new GetDataTask().execute();
				break;
			case 6:
				list = countService.getCategoryList();
				setAdapter();
				break;
			case 7:
				try {
					AudioCategory category = countService.getPlayingCatogory();
					for(int i=0;i<list.size();i++){
						if(category.category == list.get(i).category){
							index = i;
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(item!=null){
					TextView musicName = (TextView) findViewById(R.id.music_name);
					musicName.setText(item.name);
					musicName.setVisibility(View.VISIBLE);
					TextView musicAuthor = (TextView) findViewById(R.id.music_author);
					musicAuthor.setText(item.album);
					musicAuthor.setVisibility(View.VISIBLE);
					setFavourite(countService.isSubscription(item.albumId));
					musicState.setImageResource(R.drawable.music_play_select);
					if(countService.isSubscription(item.albumId)){
						setFavourite(true);
					}else{
						setFavourite(false);
					}
				try {
					musicNowCategory = list.get(index).category;
					flow.setSelection(index);
					Intent it = new Intent();
					it.setAction(Contents.PLAY_MUSIC_NEWS_NEXT);
					it.putExtra("music_now_category", item);
					sendBroadcast(it);
				} catch (Exception e) {
					e.printStackTrace();
				}
				}
				break;
			case 8:
				try {
					if(countService.isSubscription(item.albumId)){
						setFavourite(true);
					}else{
						setFavourite(false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	};
	
	private void setTitleEnable(int index){
		try {
			if(list.get(index).item!=null&&list.get(index).item.size()>0){
				title.setTextColor(getResources().getColor(R.drawable.music_list_text_select));
				musicList.setImageResource(R.drawable.music_list_select);
			    musicListPoint.setImageResource(R.drawable.music_list_point_select);
			    setViewEnable(true);
			}else{
				title.setTextColor(getResources().getColor(R.color.text_gray));
				musicList.setImageResource(R.drawable.muslc_unable_list);
			    musicListPoint.setImageResource(R.drawable.music_unclick);
			    setViewEnable(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setViewEnable(boolean able){
		 title.setEnabled(able);
	     musicList.setEnabled(able);
	     musicListPoint.setEnabled(able);
	     musicListLayout.setEnabled(able);
	}
}
package com.tuyou.tsd.audio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.KeyEvent;
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

import com.tuyou.tsd.audio.adapter.ImageAdapter;
import com.tuyou.tsd.audio.base.MyBaseActivity;
import com.tuyou.tsd.audio.comm.Contents;
import com.tuyou.tsd.audio.service.AudioPlayerService;
import com.tuyou.tsd.audio.service.IAudioPlayerService;
import com.tuyou.tsd.audio.utils.Notify;
import com.tuyou.tsd.audio.utils.OrderUtils;
import com.tuyou.tsd.audio.weight.CoverFlow;
import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.base.CommonSleep;
import com.tuyou.tsd.common.network.AudioCategory;
import com.tuyou.tsd.common.network.AudioItem;

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
	private int index = 2;
	private AudioItem item;
	private String musicNowCategory = "";
	private boolean isTouch = false;
	
	public NotificationManager mNotificationManager;
	private ImageView musicState;
	private ImageView musicLoading;
	private TextView addLoveText;
	
//	private CommonSleep commonSleep = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		initService();
		super.onCreate(savedInstanceState);
		Intent i= new Intent();
		i.setAction(Contents.KILL_ALL_APP3);
		sendBroadcast(i);
		setContentView(R.layout.main);
		initNot();
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
		try {
			if(countService!=null){
				item = countService.getPlayingAudio();
				showNow();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		try {
			if(item!=null){
				TextView musicName = (TextView) findViewById(R.id.music_name);
				musicName.setText(item.name);
				TextView musicAuthor = (TextView) findViewById(R.id.music_author);
				musicAuthor.setText(item.author);
				setFavourite(countService.isFavourite(item));
				musicState.setImageResource(R.drawable.music_play_select);
				try {
					Notify.showButtonNotify(item, this, true, countService.isFavourite(item));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				flow.setSelection(index);
				musicNowCategory = list.get(index).category;
				Intent it = new Intent();
				it.setAction("com.tuyou.tsd.audio.playmusicnext");
				it.putExtra("music_now_category", item);
				sendBroadcast(it);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setFavourite(boolean isFavourite){
		if(isFavourite){
			ImageView musicLove = (ImageView) findViewById(R.id.music_love); 
			musicLove.setImageResource(R.drawable.music_love);
		}else{
			ImageView musicLove = (ImageView) findViewById(R.id.music_love); 
			musicLove.setImageResource(R.drawable.music_unlove);
		}
	}
	

	private void initCast() {
		cast = new GetRecCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AudioPlayerService.DATA_REFRESH);
		filter.addAction(AudioPlayerService.CHACHE_PROGRESS);
		filter.addAction(AudioPlayerService.PLAY_PROGRESS);
		filter.addAction(AudioPlayerService.NEXT_AUDIO);
		filter.addAction(AudioPlayerService.HEARD_LIST);
		filter.addAction(Contents.MUSICPLAY_CATEGORY);
		filter.addAction(TSDEvent.System.ACC_OFF);
		filter.addAction(Contents.ACTION_BUTTON);
		filter.addAction(Contents.MUSICPLAY_STATE_PLAY);
		filter.addAction(Contents.MUSICPLAY_STATE_PAUSE);
		filter.addAction(TSDEvent.Audio.PAUSE);
		filter.addAction(TSDEvent.Audio.RESUME);
		filter.addAction(AudioPlayerService.PLAY_STATUS);
		filter.addAction(Contents.KILL_ALL_APP1);
		filter.addAction(Contents.KILL_ALL_APP2);
		filter.addAction(Contents.TSD_AUDIO_PLAY_MUSIC_RESULT);
		
		filter.addAction(TSDEvent.System.HARDKEY3_PRESSED);
		
		registerReceiver(cast, filter);
	}

	private void initService() {
		this.startService(new Intent("com.tuyou.tsd.audio.service.AudioPlayerService"));
		this.bindService( new Intent("com.tuyou.tsd.audio.service.AudioPlayerService" ), 
        		this.serviceConnection, BIND_AUTO_CREATE);
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
		addLoveText = (TextView) findViewById(R.id.add_love_text);
		setOnClick(R.id.music_love);
		setOnClick(R.id.music_prew);
		setOnClick(R.id.music_play);
		setOnClick(R.id.music_next);
		setOnClick(R.id.music_order);
		
		flow = (CoverFlow) findViewById(R.id.cover_flow);
		flow.setOnItemClickListener(this);
		flow.setOnItemSelectedListener(this);
		
		flow.setOnTouchListener(this);
		try {
			setMusicOrder(getOrder());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int getOrder(){
		SharedPreferences preferences = getSharedPreferences("music_order",Context.MODE_PRIVATE);
		return preferences.getInt("order", 1);
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
			list = countService.getCategoryList();
			if(list != null){
//				int size = list.size();
//				for(int i =0;i<size;i++){
////					if(list.get(i).item==null||list.get(i).item.size()==0){
////						list.remove(i);
////						i--;
////						size--;
////					}
//					if(list.get(i).order==0){
//						list.get(i).image ="0";
//					}else if(list.get(i).order==1){
//						
//					}
//				}
				OrderUtils roder  = new OrderUtils();
				Collections.sort(list,roder);
				setIndex();
				imageAdapter = new ImageAdapter(this,list,flow);
				flow.setAdapter(imageAdapter);
//				flow.setSelection(list.size()*1000+index);
				flow.setSelection(index);
				flow.setAnimationDuration(800);
				try {
					title.setText(list.get(index).name);
					setTitleEnable(index);
				} catch (Exception e) {
					e.printStackTrace();
				}
				setViewEnable(R.id.music_love, true);
				setViewEnable(R.id.music_prew, true);
				setViewEnable(R.id.music_next, true);
				setViewEnable(R.id.music_play, true);
				setViewEnable(R.id.music_order, true);
				findViewById(R.id.music_null).setVisibility(View.GONE);
				
			}else{
				findViewById(R.id.music_null).setVisibility(View.VISIBLE);
				setViewEnable(R.id.music_love, false);
				setViewEnable(R.id.music_prew, false);
				setViewEnable(R.id.music_next, false);
				setViewEnable(R.id.music_play, false);
				setViewEnable(R.id.music_order, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setViewEnable(int id,boolean isEnable){
		findViewById(id).setEnabled(isEnable);
	}
	
	
	private void setOnClick(int id){
		ImageView view = (ImageView) findViewById(id);
		view.setOnClickListener(this);
	}
	
	private void initListener() {
		setAlllistener(title);
		setAlllistener(musicListLayout);
		setAlllistener(musicList);
		setAlllistener(musicListPoint);
	}
	
	private void setAlllistener(View view){
		view.setOnClickListener(this);
		view.setOnTouchListener(this);
	}
	
	
	private void gotoMusicList(){
		try {
			ArrayList<AudioItem> listAudio = list.get(index).item;
			Intent it = new Intent();
			it.setClass(this, MusicListActivity.class);
			it.putExtra("music_category", list.get(index).category);
			it.putExtra("music_list", listAudio);
			it.putExtra("music_title", list.get(index).name);
				if(item!=null&&item.item!=null){
					it.putExtra("music_now", item.item);
					it.putExtra("music_now_category", musicNowCategory);
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
		case R.id.music_order:
			setMusicOrder(countService.setMusicOrder());
			break;
		case R.id.music_prew:
			countService.prew();
			setMusicOrder(countService.setMusicOrder(1));
			setMusicOrder(1);
			break;
		case R.id.music_next:
			setMusicOrder(countService.setMusicOrder(1));
			setMusicOrder(1);
			countService.next();
			break;
		case R.id.music_play:
			playMusicRestar();
			break;
		case R.id.music_love:
			setLove();
			break;
		default:
			break;
		}
	}
	
	private void setLove(){
		if(item!=null){
			if(countService.isFavourite(item)){
				countService.deleteFavourite(item);
				setFavourite(false);
				((ImageView)findViewById(R.id.music_love)).setImageResource(R.drawable.music_unlove);
				addFavoriList(false, item);
				try {
					Notify.showButtonNotify(item, this, countService.isPlaying(), false);
				} catch (Exception e) {
					e.printStackTrace();
				}
				textViewHandler.removeMessages(6);
				addLoveText.setVisibility(View.VISIBLE);
				addLoveText.setText(R.string.re_love);
				Message msg = new Message();
				msg.what = 6;
				textViewHandler.sendMessageDelayed(msg, 2000);
			}else{
				countService.addFavourite(item);
				setFavourite(true);
				((ImageView)findViewById(R.id.music_love)).setImageResource(R.drawable.music_love);
				addFavoriList(true, item);
				try {
					Notify.showButtonNotify(item, this, countService.isPlaying(), true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				textViewHandler.removeMessages(6);
				addLoveText.setVisibility(View.VISIBLE);
				addLoveText.setText(R.string.add_love);
				Message msg = new Message();
				msg.what = 6;
				textViewHandler.sendMessageDelayed(msg, 2000);
			}
		}
	}
	
	private void addFavoriList(boolean isFavoriList,AudioItem item){
		int size = list.size();
		boolean has = true;
		if(isFavoriList){
			for(int i=0;i<size;i++){
				if(list.get(i).order==-3){
					list.get(i).image = item.icon;
					if(!list.get(i).item.contains(item)&&!list.get(i).item.equals(item.item)){
						list.get(i).item.add(0,item);
					}
					has = false;
					break;
				}
			}
			if(has){
				AudioCategory audio = new AudioCategory();
				audio.order  = -3;
				audio.name = "我的收藏";
				ArrayList<AudioItem> itemAudio = new ArrayList<AudioItem>();
				audio.category ="favourite";
//				audio.image = item.icon;
				itemAudio.add(item);
				audio.item = itemAudio;
				list.add(audio);
				OrderUtils order = new OrderUtils();
				Collections.sort(list,order);
//				imageAdapter.notifyDataSetChanged();
				if(list.get(index).order<0){
					
				}else{
					index++;
				}
//				flow.setSelection(list.size()*1000+index);
				flow.setSelection(index);
				setTitleEnable(index);
				title.setText(list.get(index).name);
			}
			imageAdapter.notifyDataSetChanged();
		}else{
			for(int i=0;i<size;i++){
				if(list.get(i).order == 0){
					for(int j=0;j<list.get(i).item.size();j++){
						if(list.get(i).item.get(j).item.equals(item.item)){
							list.get(i).item.remove(j);
							break;
						}
					}
					if(list.get(i).item.size()==0){
//						int oldorder=list.get(index).order;
//						list.remove(i);
						imageAdapter.notifyDataSetChanged();
//						if(oldorder<=0){
//						}else{
//							index--;
//						}
					}else{
//						list.get(i).image = list.get(i).item.get(0).icon;
						imageAdapter.notifyDataSetChanged();
					}
//					flow.setSelection(list.size()*1000+index);
					if(index==list.size()){
						index--;
					}
					flow.setSelection(index);
					setTitleEnable(index);
					title.setText(list.get(index).name);
					break;
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
					Notify.showButtonNotify(item, this, false, countService.isFavourite(item));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}else if(list!=null&&list.size()>0){
				countService.rusume();
				musicState.setImageResource(R.drawable.music_play_select);
				try {
					Notify.showButtonNotify(item, this, true, countService.isFavourite(item));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setMusicOrder(int order){
		switch (order) {
		case 1:
			((ImageView)findViewById(R.id.music_order)).setImageResource(R.drawable.music_order_1_select);
//			MyToast.showToast(MusicActivity.this,R.string.music_order_1,Gravity.RIGHT);
			break;
		case 2:
			((ImageView)findViewById(R.id.music_order)).setImageResource(R.drawable.music_order_2_select);
			break;
//		case 3:
//			((ImageView)findViewById(R.id.music_order)).setImageResource(R.drawable.music_order_3_select);
//			break;
		default:
			break;
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
	
	class GetRecCast extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(AudioPlayerService.PLAY_STATUS)){
				switch (intent.getExtras().getInt("status")) {
				case 1:
					musicState.setImageResource(R.drawable.music_play_select);
					musicState.setEnabled(true);
					musicLoading.clearAnimation();
					musicLoading.setVisibility(View.GONE);
					
					
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
					break;
				default:
					break;
				}
			}else 
			if(intent.getAction().equals(AudioPlayerService.DATA_REFRESH)){
				index = 2;
				getData();
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
				for(int i=0;i<list.size();i++){
					if(countService.getPlayingCatogory().category.equals(list.get(i).category)){
						index = i;
						flow.setSelection(i);
						try {
							setTitleEnable(index);
							title.setText(list.get(index).name);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					}
				}
			}else 
			if(intent.getAction().equals(Contents.MUSICPLAY_CATEGORY)){
				ArrayList<AudioItem> listChild = intent.getParcelableArrayListExtra("music_list");
				countService.StartPlayer(intent.getExtras().getString("music_category"), listChild.get(intent.getExtras().getInt("music_index")).item);
			}else
			if(intent.getAction().equals(AudioPlayerService.HEARD_LIST)){
				int size = list.size();
				boolean isHasHos = true;
				for(int i=0;i<size;i++){
					if(list.get(i).order==-4){
						list.remove(i);
						list.add(i, countService.getHeardCatogory());
						isHasHos = false;
						imageAdapter.notifyDataSetChanged();
						break;
					}
				}
				if(isHasHos){

					AudioCategory audio = new AudioCategory();
					audio.order  = -4;
					audio.name = "最近听的歌";
					ArrayList<AudioItem> itemAudio = new ArrayList<AudioItem>();
					audio.category ="myheard";
					itemAudio.add(item);
					audio.item = itemAudio;
					list.add(audio);
					OrderUtils order = new OrderUtils();
					Collections.sort(list,order);
					imageAdapter.notifyDataSetChanged();
					flow.setSelection(index);
					try {
						setTitleEnable(index);
						title.setText(list.get(index).name);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else{
					imageAdapter.notifyDataSetChanged();
				}
			}else
			if(intent.getAction().equals(TSDEvent.System.ACC_OFF)){
				stopService(new Intent(MusicActivity.this,AudioPlayerService.class));
				finish();
			}else
			if(intent.getAction().equals(Contents.MUSICPLAY_STATE_PAUSE)){
				musicState.setImageResource(R.drawable.music_pause_select);
				try {
					Notify.showButtonNotify(item, MusicActivity.this, false, countService.isFavourite(item));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}else
			if(intent.getAction().equals(Contents.MUSICPLAY_STATE_PLAY)){
				musicState.setImageResource(R.drawable.music_play_select);
				try {
					Notify.showButtonNotify(item, MusicActivity.this, true, countService.isFavourite(item));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else
			
			if(intent.getAction().equals(Contents.ACTION_BUTTON)){
				int buttonId = intent.getIntExtra(Contents.INTENT_BUTTONID_TAG, 0);
				switch (buttonId) {
				case Contents.BUTTON_CLEAN_ID:
					finish();
					break;
				case Contents.BUTTON_LOVE_ID:
					Message msglove = new Message();
					msglove.what = 4;
					textViewHandler.sendMessageDelayed(msglove, 400);
					break;
				}
			}else if(intent.getAction().equals(Contents.KILL_ALL_APP1)||intent.getAction().equals(Contents.KILL_ALL_APP2)){
				finish();
			}else if(intent.getAction().equals(Contents.TSD_AUDIO_PLAY_MUSIC_RESULT)){
				Message msg = textViewHandler.obtainMessage();
				msg.what = 7;
				msg.obj = intent.getStringExtra("item");
				textViewHandler.sendMessageDelayed(msg, 500);
			}else if(intent.getAction().equals(TSDEvent.System.HARDKEY3_PRESSED)){
				Message msglove = new Message();
				msglove.what = 8;
				textViewHandler.sendMessageDelayed(msglove, 400);
			}
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
//		try {
//			if(index==arg2%list.size()){
//				playMusicRestar();
//			}else{
//				sendMessagePlayMusic();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		if(index==arg2){
//			gotoMusicList();
			playMusicRestar();
		}else{
			index=arg2;
			setMusicOrder(countService.setMusicOrder(1));
			setMusicOrder(1);
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
		if(index!=arg2%list.size()){
			index = arg2%list.size();
			//TODO
		}
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
//			
			case 4:
				if(countService.isFavourite(item)){
					((ImageView)findViewById(R.id.music_love)).setImageResource(R.drawable.music_love);
					addFavoriList(true, item);
				}else{
					((ImageView)findViewById(R.id.music_love)).setImageResource(R.drawable.music_unlove);
					addFavoriList(false, item);
				}
				break;
			case 5:
				getData();
				playItemNow();
				break;
			case 6:
				addLoveText.setVisibility(View.GONE);
				break;
			case 7:
				try {
					list = countService.getCategoryList();
					if(list!=null){
						OrderUtils roder  = new OrderUtils();
						Collections.sort(list,roder);
						flow.setSelection(1);
						index = 1;
						title.setText(list.get(index).name);
						setTitleEnable(index);
						countService.StartPlayer("favourite", String.valueOf(msg.obj));
//						System.out.println("===="+String.valueOf(msg.obj));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 8:
				if(countService.isFavourite(item)){
					((ImageView)findViewById(R.id.music_love)).setImageResource(R.drawable.music_love);
				}else{
					((ImageView)findViewById(R.id.music_love)).setImageResource(R.drawable.music_unlove);
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
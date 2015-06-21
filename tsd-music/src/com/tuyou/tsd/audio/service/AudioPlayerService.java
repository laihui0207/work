package com.tuyou.tsd.audio.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuyou.tsd.audio.MusicActivity;
import com.tuyou.tsd.audio.comm.Contents;
import com.tuyou.tsd.audio.db.AudioItemDAO;
import com.tuyou.tsd.audio.db.AudioItemEntity;
import com.tuyou.tsd.audio.db.CategoryDAO;
import com.tuyou.tsd.audio.db.CategoryDetail;
import com.tuyou.tsd.audio.db.CategoryDetailDAO;
import com.tuyou.tsd.audio.db.CategoryDetailEntity;
import com.tuyou.tsd.audio.db.CategoryEntity;
import com.tuyou.tsd.audio.db.FavouriteItemDAO;
import com.tuyou.tsd.audio.db.FavouriteItemEntity;
import com.tuyou.tsd.audio.db.FavouriteRecord;
import com.tuyou.tsd.audio.db.FavouriteRecordDAO;
import com.tuyou.tsd.audio.db.FavouriteRecordEntity;
import com.tuyou.tsd.audio.db.HeardAllItemDAO;
import com.tuyou.tsd.audio.db.HeardAllItemEntity;
import com.tuyou.tsd.audio.db.HeardItemDAO;
import com.tuyou.tsd.audio.db.HeardItemEntity;
import com.tuyou.tsd.audio.utils.GetFileSizeUtil;
import com.tuyou.tsd.audio.utils.Notify;
import com.tuyou.tsd.common.CommonApps;
import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.network.AudioCategory;
import com.tuyou.tsd.common.network.AudioItem;
import com.tuyou.tsd.common.network.GetAudioCategoryDetailRes;
import com.tuyou.tsd.common.network.GetAudioCategoryListRes;
import com.tuyou.tsd.common.network.GetAudioFavouriteListRes;
import com.tuyou.tsd.common.network.JsonOA2;
import com.tuyou.tsd.common.network.AudioState;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.common.util.MyAsyncTask;

@SuppressLint("NewApi")
public class AudioPlayerService extends Service implements IAudioPlayerService,
		OnBufferingUpdateListener, OnCompletionListener, OnInfoListener,
		OnErrorListener {

	public static final String DATA_REFRESH = "com.tuyou.tsd.audio.data_refresh";/* 刷新歌单 */
	public static final String PLAY_PROGRESS = "com.tuyou.tsd.audio.playprogress";/* 播放进度 */
	public static final String CHACHE_PROGRESS = "com.tuyou.tsd.audio.chacheprogress";/* 缓存进度 */
	public static final String NEXT_AUDIO = "com.tuyou.tsd.audio.nextaudio";/* 切换播放歌曲 */
	public static final String HEARD_LIST = "com.tuyou.tsd.audio.heardlist";/* 最近歌曲列表 */
	public static final String PLAY_STATUS = "com.tuyou.tsd.audio.playstatus";/* 播放状态 */
	
	public static final String PLAY_EXIT = "com.tuyou.tsd.audio.exit";/* 刷新歌单 */
	public static final String PLAY_ABANDON = "com.tuyou.tsd.audio.abandon";/* 放弃播放*/

	private static final int MSG_GETCATEGORYLIST = 100;
	private static final int MSG_GETFAVOUTITE = 101;
	private static final int MSG_GETAUDIOITEMLIST = 102;
	private static final int MSG_GETPUSHCATEGORY = 103;
	private static final int MSG_GETPUSHAUDIOITEMLIST = 104;

	private static final int MSG_AUTOPLAYSTART = 200;// 自动播放歌曲

	private static final int MSG_CATEGORYREFRESH = 300;
	private static final int MSG_TEST = 1000;
	
	private static final int MSG_MUSIC_PLAY = 201;

	private MyReceiver m_myReceiver = null;
	private String currentCategory = "test";
	private List<AudioCategory> mCategories = null;// 当前从服务器拉到的歌单列表
	private List<AudioCategory> myCategories = null;// 当前所有歌曲列表
	private ServiceBinder serviceBinder = new ServiceBinder();
	private StreamingMediaPlayer mMediaPlayer = null; // 播放器
	private GetCategoryListTask categoryListTask = null; // 获取歌单异步任务
	private GetCategoryDetailListTask categoryDetailTask = null;// 获取歌单歌曲列表异步任务
	private GetFavouriteListTask favouriteListTask = null;// 获取我的收藏歌曲列表异步任务

	private int categoryIndex = 0;
	private AudioCategory currentPlayingCategory = null;// 当前播放歌单
	public AudioItem currentPlayingAudio = null; // 当前播放歌曲
	private AudioItem nextPlayAudio = null;// 下一首歌
	private FavouriteManager mFavouriteManager = null; // 我的收藏管理模块
	private AudioCategory myFavourite = null; // 我的收藏歌歌单信息
	private boolean mIsActivity = false; // 是否有界面绑定
	private boolean mIsPlaying = false;
	private GetPushCategoryTask getPushCategoryTask = null; // 获取PUSH歌单信息
	private GetPushCategoryDetailListTask pushCategoryDetailTask = null;// 获取PUSH歌单歌曲信息
	private AudioCategory pushCategory = null;// PUSH歌单列表
	private List<AudioCategory> mPushCategorys = null;
	private SharedPreferences mPref = null;
	private boolean isGetCategory=false; //是否已经更新过歌单，一般在启动时更新，如果碰到启动时无网络，则有网络后更新
    private boolean isAutoPlayed=false;
    private int mTrygetCategoryTime=0;
    private boolean mIsAccOn=false;
    
    private boolean isPlayNow = false;
    
    private static final int MSG_MUSIC_DOBACKGROUND = 1001; 
    
    private AudioItem resultNow = null;
    private int playIndex;
    
    private boolean isTTSPause = false;
    
	@Override
	public void onCreate() {
		initCast();
		super.onCreate();
//		getCategoryList();
//		System.out.println("=========================================音乐开始了");
		mPref = getSharedPreferences("audioservice", Context.MODE_PRIVATE);
		mIsAccOn=true;
		

		if (JsonOA2.getInstance(this).checkNetworkInfo() == -1) {

		} else {
			startGetCategory();
			mTrygetCategoryTime=1;
			isGetCategory=true;
		}
		readPushCategory();
		isAutoPlayed=false;
		mHandler.sendEmptyMessageDelayed(801, 1000*30);
		this.sendBroadcast(new Intent(TSDEvent.Audio.SERVICE_STARTED));		
	}

	private void initCast() {
		m_myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(TSDEvent.Push.AUDIO_CATEGORY);
		filter.addAction(TSDEvent.System.ACC_ON);
		filter.addAction(TSDEvent.System.ACC_OFF);
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		filter.addAction(Contents.ACTION_BUTTON);
		filter.addAction(PLAY_ABANDON);
		filter.addAction(CommonMessage.VOICE_COMM_SHUT_UP);
		filter.addAction(TSDEvent.Interaction.INTERACTION_START);
		filter.addAction(TSDEvent.Interaction.INTERACTION_FINISH_FROM_CORE_SERVICE);
		filter.addAction(TSDEvent.Interaction.INTERACTION_ERROR);
		filter.addAction(Contents.KILL_ALL_APP1);
		filter.addAction(Contents.KILL_ALL_APP2);
		filter.addAction(Contents.PUSH_AUDIO_CATEGORY);
		filter.addAction(TSDEvent.Audio.PLAY);
		filter.addAction(CommonMessage.TTS_PLAY);
		filter.addAction(CommonMessage.TTS_PLAY_FINISHED);
		filter.addAction(TSDEvent.System.HARDKEY3_PRESSED);
		filter.addAction(TSDEvent.System.HARDKEY2_PRESSED);
		
		filter.addAction(CommonApps.SLEEP_PLAY_NEXT);
		filter.addAction(CommonApps.SLEEP_PLAY_MUSIC);
		filter.addAction(CommonApps.SLEEP_PLAY_PRE);
		
		filter.addAction(CommonApps.SLEEP_START);
		
		registerReceiver(m_myReceiver, filter);
	}

	@Override
	public void onDestroy() {
		
		super.onDestroy();
		if (m_myReceiver != null) {
			this.unregisterReceiver(m_myReceiver);
		}
		if (categoryListTask != null) {
			categoryListTask.cancel(true);
		}
		if (categoryDetailTask != null) {
			categoryDetailTask.cancel(true);
		}
		if (favouriteListTask != null) {
			favouriteListTask.cancel(true);
		}
		if (mFavouriteManager != null) {
			mFavouriteManager.release();
		}

		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
		}		
		
		mIsAccOn=false;
		mPushCategorys=null;
		clearPushCategory();
		if (mMediaPlayer != null)  {
			mMediaPlayer.stop();
		}
		stop();
		mHandler.removeMessages(801);
		
		this.sendBroadcast(new Intent(TSDEvent.Audio.SERVICE_STOPPED));
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(200);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		mHandler.sendEmptyMessage(800);
		return START_STICKY;//super.onStartCommand(intent, flags, startId);
	}
    
	@Override
	public IBinder onBind(Intent intent) {
		
		mIsActivity = true;
		isAutoPlayed=false;
		mHandler.sendEmptyMessageDelayed(MSG_CATEGORYREFRESH, 100);
		mHandler.sendEmptyMessageDelayed(MSG_AUTOPLAYSTART, TSDConst.PLAY_SPACE);
		return serviceBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		
		mIsActivity = false;
		return true;// super.onUnbind(intent);
	}

	@Override
	public void onRebind(Intent intent) {
		
		mIsActivity = true;
		isAutoPlayed=false;
		mHandler.sendEmptyMessageDelayed(MSG_CATEGORYREFRESH, 100);
		mHandler.sendEmptyMessageDelayed(MSG_AUTOPLAYSTART, TSDConst.PLAY_SPACE);
		super.onRebind(intent);
	}

	private void startGetCategory() {
		categoryListTask = new GetCategoryListTask();
		categoryListTask.execute();
	}

	private void startGetCategoryDetail() {
		categoryIndex = 0;
		currentCategory = mCategories.get(categoryIndex).category;
		categoryDetailTask = new GetCategoryDetailListTask();
		categoryDetailTask.execute();

	}

	private void startGetFavourite() {
		favouriteListTask = new GetFavouriteListTask();
		favouriteListTask.execute();
	}

	private void startGetPushCategoryDetail() {
		pushCategoryDetailTask = new GetPushCategoryDetailListTask();
		pushCategoryDetailTask.execute();
	}

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			

			switch (msg.what) {
			case MSG_GETCATEGORYLIST:
				GetAudioCategoryListRes categoryListResres = (GetAudioCategoryListRes) msg.obj;
				if (categoryListResres.categories != null) {
					mCategories = new ArrayList<AudioCategory>();
					for (int i = 0; i < categoryListResres.categories.length; i++) {
						mCategories.add(categoryListResres.categories[i]);
					}
					categoryListTask = null;
					startGetCategoryDetail();
				}
				break;
			case MSG_GETFAVOUTITE:
				GetAudioFavouriteListRes favouriteListRes = (GetAudioFavouriteListRes) msg.obj;
				AudioItem[] favourites = favouriteListRes.items;
				FavouriteItemDAO.getInstance(AudioPlayerService.this)
						.deleteAll();
				if (favourites != null) {
					for (int i = 0; i < favourites.length; i++) {
						FavouriteItemEntity cc = new FavouriteItemEntity();
						cc.setDetail(favourites[i]);
						FavouriteItemDAO.getInstance(AudioPlayerService.this)
								.save(cc);
					}
				}
				favouriteListTask = null;
				AudioPlayerService.this.sendBroadcast(new Intent(DATA_REFRESH));
				break;
			case MSG_GETAUDIOITEMLIST:
				GetAudioCategoryDetailRes categoryDetailRes = (GetAudioCategoryDetailRes) msg.obj;
				if (categoryDetailRes.items != null) {
					ArrayList<AudioItem> items = new ArrayList<AudioItem>();
					for (int i = 0; i < categoryDetailRes.items.length; i++) {
						items.add(categoryDetailRes.items[i]);
					}
					mCategories.get(categoryIndex).item = items;
				}
				if (categoryIndex < mCategories.size() - 1) {
					categoryIndex++;
					currentCategory = mCategories.get(categoryIndex).category;
					categoryDetailTask.execute();
				} else {
					CategoryDAO.getInstance(AudioPlayerService.this).deleteAll();
					for (int i = 0; i < mCategories.size(); i++) {
						CategoryEntity cc = new CategoryEntity();
						cc.setDetail(mCategories.get(i));
						CategoryDAO.getInstance(AudioPlayerService.this).save(cc);
					}
					categoryDetailTask = null;
					startGetFavourite();
				}
				break;
			case MSG_GETPUSHCATEGORY:
				GetAudioCategoryListRes pushcategoryListResres = (GetAudioCategoryListRes) msg.obj;
				if ((pushcategoryListResres.categories != null)&& (pushcategoryListResres.categories.length > 0)) {
					pushCategory = pushcategoryListResres.categories[0];
					startGetPushCategoryDetail();
				}
				getPushCategoryTask = null;
				break;
			case MSG_GETPUSHAUDIOITEMLIST:
				GetAudioCategoryDetailRes pushListRes = (GetAudioCategoryDetailRes) msg.obj;
				if (pushListRes.items != null) {
					ArrayList<AudioItem> items = new ArrayList<AudioItem>();
					for (int i = 0; i < pushListRes.items.length; i++) {
						items.add(pushListRes.items[i]);
					}
					pushCategory.item = items;
				}
				pushCategoryDetailTask = null;

				if ((pushCategory != null) && (pushCategory.item != null)) {
					
					isAutoPlayed=true;
					addPushCategory();
					getCategoryList();
					AudioPlayerService.this.StartPlayer(pushCategory.category,pushCategory.item.get(0).item);
					
					if (!mIsActivity) {
					
						Intent it = new Intent(AudioPlayerService.this,MusicActivity.class);
						it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						AudioPlayerService.this.startActivity(it);
					}					
					AudioPlayerService.this.sendBroadcast(new Intent(DATA_REFRESH));	
					//mHandler.sendEmptyMessageDelayed(901, 200);
				}
				break;

			case MSG_AUTOPLAYSTART:
				//if((currentPlayingAudio==null)||(currentPlayingCategory==null))
				{
				  autoPlayAudio();
				}
				break;
			case MSG_CATEGORYREFRESH:
				AudioPlayerService.this.sendBroadcast(new Intent(DATA_REFRESH));
				break;
			case 401://播放中网络断开
				AudioPlayerService.this.onCompletion(null);				
				break;
			case 402://获取歌单失败
				//mCategories=null;
				mHandler.sendEmptyMessageDelayed(999, 1000*20);//20秒后重新更新
				break;
			case 800: //检查获取歌单
				if ((mCategories == null) && (categoryListTask == null)&& (categoryDetailTask == null)&& (favouriteListTask == null)) {
					startGetCategory();
				}
				break;
			case 801:
				if (mCategories == null) {
					if (categoryListTask != null) {
						categoryListTask.cancel(true);
					}
					if (categoryDetailTask != null) {
						categoryDetailTask.cancel(true);
					}
					if (favouriteListTask != null) {
						favouriteListTask.cancel(true);
					}
					startGetCategory();
					mHandler.sendEmptyMessageDelayed(801, 1000 * 50);
				}
				break;
			case 999:
				if (JsonOA2.getInstance(AudioPlayerService.this).checkNetworkInfo() != -1) {
					if (mTrygetCategoryTime < 5) {
						startGetCategory();
						mTrygetCategoryTime++;
					}
				}
				break;
			case 901:  //播放PUSH歌单
				AudioPlayerService.this.StartPlayer(pushCategory.category,
						pushCategory.item.get(0).item);
				break;
			case MSG_TEST:
				disposePushCategory("0a89b221-a878-4d98-9d2f-b04b121fe2d8");
				break;
			case MSG_MUSIC_PLAY:
				if(isPlaying()){
					isPlayNow = true;
					pause();
					try {
						Notify.showButtonNotify(getPlayingAudio(), AudioPlayerService.this, false, isFavourite(getPlayingAudio()));
					} catch (Exception e) {
						e.printStackTrace();
					}
					Intent it = new Intent();
					it.setAction(Contents.MUSICPLAY_STATE_PAUSE);
					sendBroadcast(it);
				}else{
					if(isPlayNow){
						if(!isPlaying()){
							isPlayNow = false;
							rusume();
							try {
								Notify.showButtonNotify(getPlayingAudio(), AudioPlayerService.this, true, isFavourite(getPlayingAudio()));
							} catch (Exception e) {
								e.printStackTrace();
							}
							Intent it = new Intent();
							it.setAction(Contents.MUSICPLAY_STATE_PLAY);
							sendBroadcast(it);
						}
					}
				}
				break;
			case MSG_MUSIC_DOBACKGROUND:
				try {
					currentPlayingAudio = (AudioItem)msg.obj;
					nextPlayAudio = getNextAudio(currentPlayingAudio);
					int result=mMediaPlayer.startStreaming(currentPlayingAudio.url, currentPlayingAudio.item, currentPlayingAudio.size,0);
					mIsPlaying = true;
					showButtonNotify(currentPlayingAudio,true,isFavourite(currentPlayingAudio));				
					doAfterPlayAudio();
					if (result== 1) {
						AudioPlayerService.this.onBufferingUpdate(null, 100);
						sendPlayStatus(1);		
					} else {
						sendPlayStatus(2);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			default:
				break;
			}

		}

	};
	private String mPushCategory = null;

	private void disposePushCategory(String label) {
		if ((getPushCategoryTask == null) && (pushCategoryDetailTask == null)) {
			getPushCategoryTask = new GetPushCategoryTask();
			mPushCategory = label;
			getPushCategoryTask.execute();
		}
	}
	
	private void addPushCategory()
	{
		if(mPushCategorys==null)
		{
		    mPushCategorys= new ArrayList<AudioCategory>();
		}
		else
		{
			for(int i=0;i<mPushCategorys.size();i++)//去重复
			{
				if(mPushCategorys.get(i).category.equals(pushCategory.category))
				{
					mPushCategorys.remove(i);
					i--;
				}
			}
		}
		mPushCategorys.add(pushCategory);		
		
		List<AudioItemEntity> pushcategorys=AudioItemDAO.getInstance(AudioPlayerService.this).readAll();
		if (pushcategorys != null) {
			for(int k=0;k<pushcategorys.size();k++)
			{
				if(pushcategorys.get(k).getDetail().category.equals(pushCategory.category))
				{
					AudioItemDAO.getInstance(AudioPlayerService.this).delete(pushcategorys.get(k).getId());
				}
			}
		}
		
		AudioItemEntity entity=new AudioItemEntity();
		entity.setDetail(pushCategory);
		AudioItemDAO.getInstance(AudioPlayerService.this).save(entity);
	}
	private void readPushCategory()
	{		
		List<AudioItemEntity> pushcategorys=AudioItemDAO.getInstance(AudioPlayerService.this).readAll();
		if (pushcategorys != null) {
			if (mPushCategorys == null) {
				mPushCategorys = new ArrayList<AudioCategory>();
			} else {
				mPushCategorys.clear();
			}
			for(int k=0;k<pushcategorys.size();k++)
			{
				
				if(pushcategorys.get(k).getDetail().mode==1)
				{
					String timestr=pushcategorys.get(k).getDetail().end;
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ssZ", Locale.CHINA);
					String strTimestamp = dateFormat.format(new Date(System
							.currentTimeMillis()));
					strTimestamp = strTimestamp.substring(0, 22) + ":"
							+ strTimestamp.substring(22);
					if(timestr.compareTo(strTimestamp)>0)
					{
						mPushCategorys.add(pushcategorys.get(k).getDetail());
					}
					else
					{
						AudioItemDAO.getInstance(AudioPlayerService.this).delete(pushcategorys.get(k).getId());
					}
					
				}
				else
				{
					mPushCategorys.add(pushcategorys.get(k).getDetail());
				}				
			}
		}
	}

	private void clearPushCategory()
	{		
		List<AudioItemEntity> pushcategorys=AudioItemDAO.getInstance(AudioPlayerService.this).readAll();
		if (pushcategorys != null) {
			for(int k=0;k<pushcategorys.size();k++)
			{				
				if(pushcategorys.get(k).getDetail().mode==1)
				{
					String timestr=pushcategorys.get(k).getDetail().end;
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ssZ", Locale.CHINA);
					String strTimestamp = dateFormat.format(new Date(System
							.currentTimeMillis()));
					strTimestamp = strTimestamp.substring(0, 22) + ":"
							+ strTimestamp.substring(22);
					if(timestr.compareTo(strTimestamp)>0)
					{
	
					}
					else
					{
						AudioItemDAO.getInstance(AudioPlayerService.this).delete(pushcategorys.get(k).getId());
					}					
				}
				else
				{
					AudioItemDAO.getInstance(AudioPlayerService.this).delete(pushcategorys.get(k).getId());;
				}				
			}
		}
	}
	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();			
			if(intent.getAction().equals(Contents.KILL_ALL_APP1)||intent.getAction().equals(Contents.KILL_ALL_APP2)){
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				notificationManager.cancelAll();
				tellLauncher("");
				isPlayNow = false;
				mIsAccOn=false;
				mPushCategorys=null;
				try {
					clearPushCategory();
				} catch (Exception e) {
					e.printStackTrace();
				}
				stop();
			}else if (action.equals(TSDEvent.Push.AUDIO_CATEGORY)) {
				if(mIsAccOn)
				{
					String url = intent.getStringExtra("params");
					String label = url.substring(url.lastIndexOf("=") + 1);
					disposePushCategory(label);
				}
			}
			else if(action.equals("android.net.conn.CONNECTIVITY_CHANGE"))
			{
				ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
		        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);  
		        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
		       // NetworkInfo activeInfo = manager.getActiveNetworkInfo();  
		        if(mobileInfo.isConnected()||wifiInfo.isConnected())
		        {
		        	if(!isGetCategory)
		        	{
		        		startGetCategory();
		    			isGetCategory=true;
		        	}		        	
		        }
		        if((!mobileInfo.isConnected())&&(!wifiInfo.isConnected()))
		        {
		        	//Toast.makeText(AudioPlayerService.this, "亲，网络断开了！",Toast.LENGTH_SHORT).show();
		        }
			}
			else if (action.equals(TSDEvent.System.ACC_ON)) {				
				mIsAccOn=true;
			} else if (action.equals(TSDEvent.System.ACC_OFF)) {
				mIsAccOn=false;
				mPushCategorys=null;
				clearPushCategory();
				if (mMediaPlayer != null)  {
					mMediaPlayer.stop();
				}
				isPlayNow = false;
				tellLauncher("");
				stop();
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				notificationManager.cancelAll();
			} else if (action.equals(TSDEvent.Audio.PAUSE)) {
				if(AudioPlayerService.this.isPlaying()) 
				{
					isPlayNow = true;
					AudioPlayerService.this.pause();
					try {
						Notify.showButtonNotify(getPlayingAudio(), AudioPlayerService.this, false, isFavourite(getPlayingAudio()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else if (action.equals(TSDEvent.Audio.RESUME)) {
				if(isPlayNow)
				{
					if(!isPlaying()){
						AudioPlayerService.this.rusume();
						isPlayNow=false;
						try {
							Notify.showButtonNotify(getPlayingAudio(), AudioPlayerService.this, true, isFavourite(getPlayingAudio()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}else if(action.equals(TSDEvent.Interaction.INTERACTION_START)){
				if(AudioPlayerService.this.isPlaying()) 
				{
					isPlayNow=true;
					isTTSPause = true;
					AudioPlayerService.this.pause();
				try {
					Notify.showButtonNotify(getPlayingAudio(), AudioPlayerService.this, false, isFavourite(getPlayingAudio()));
				} catch (Exception e) {
					e.printStackTrace();
				}
				}

			}else  if(action.equals(TSDEvent.Interaction.INTERACTION_FINISH_FROM_CORE_SERVICE)){
				if(isTTSPause){
					if (isPlayNow) {
						if(!isPlaying()){
							AudioPlayerService.this.rusume();
							isPlayNow = false;
							isTTSPause = false;
							try {
								Notify.showButtonNotify(getPlayingAudio(), AudioPlayerService.this, true, isFavourite(getPlayingAudio()));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}	
			else if(action.equals(CommonMessage.VOICE_COMM_SHUT_UP)){
				if(isPlaying()){
					isPlayNow = true;
					pause();
					Intent it = new Intent();
					it.setAction(Contents.MUSICPLAY_STATE_PAUSE);
					sendBroadcast(it);
				}
			}
			else if(action.equals(PLAY_ABANDON))
			{
				int pp=intent.getIntExtra("source", 0);
				if (pp!=1) {
					NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					notificationManager.cancel(200);
					AudioPlayerService.this.stop();
					AudioPlayerService.this.sendBroadcast(new Intent(AudioPlayerService.PLAY_EXIT));
				}
			}
			else if (action.equals(Contents.ACTION_BUTTON)) {
				int buttonId = intent.getIntExtra(Contents.INTENT_BUTTONID_TAG, 0);
				switch (buttonId) {
				case Contents.BUTTON_PREV_ID:
					prew();
					break;
				case Contents.BUTTON_NEXT_ID:
					next();
					break;
				case Contents.BUTTON_CLEAN_ID:
					isPlayNow = false;
					tellLauncher("");
					NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					notificationManager.cancel(200);
					AudioPlayerService.this.stop();
					
					break;
				case Contents.BUTTON_PLAY_ID:
					Message msg = mHandler.obtainMessage(MSG_MUSIC_PLAY);
					mHandler.sendMessage(msg);
					break;
				case Contents.BUTTON_LOVE_ID:
					try {
						if(isFavourite(getPlayingAudio())){
							deleteFavourite(getPlayingAudio());
							Notify.showButtonNotify(getPlayingAudio(), context, isPlaying(), false);
						}else{
							addFavourite(getPlayingAudio());
							Notify.showButtonNotify(getPlayingAudio(), context, isPlaying(), true);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}else if(intent.getAction().equals(TSDEvent.Audio.PLAY)){
				try {
					if(intent.getExtras().getString("type").equals("music")){
						String str = intent.getExtras().getString("playlist");
						ArrayList<AudioItem> lcs = new Gson().fromJson(str,new TypeToken<ArrayList<AudioItem>>() {}.getType());  
						for(int i=0;i<lcs.size();i++){
							addFavourite(lcs.get(i));
						}
						Intent it = new Intent();
						it.setAction(Contents.TSD_AUDIO_PLAY_MUSIC_RESULT);
						it.putExtra("item", lcs.get(0).item);
						sendBroadcast(it);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}else if(intent.getAction().equals(CommonMessage.TTS_PLAY)){
				try {
					if(isPlaying()){
						mMediaPlayer.mediaPlayer.setVolume(0.3f, 0.3f);
						isPlayNow = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(intent.getAction().equals(CommonMessage.TTS_PLAY_FINISHED)){
				try {
					if(isPlayNow){
						isPlayNow = false;
						mMediaPlayer.mediaPlayer.setVolume(1.0f, 1.0f);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}else if(intent.getAction().equals(TSDEvent.System.HARDKEY3_PRESSED)){
				if(isPlaying()){
					if(isFavourite(getPlayingAudio())){
						deleteFavourite(getPlayingAudio());
					}else{
						addFavourite(getPlayingAudio());
					}
					try {
						Notify.showButtonNotify(getPlayingAudio(), AudioPlayerService.this, isPlaying(), isFavourite(getPlayingAudio()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else if(isPlayNow){
					if(isFavourite(getPlayingAudio())){
						deleteFavourite(getPlayingAudio());
					}else{
						addFavourite(getPlayingAudio());
					}
					try {
						Notify.showButtonNotify(getPlayingAudio(), AudioPlayerService.this, isPlaying(), isFavourite(getPlayingAudio()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else if(intent.getAction().equals(TSDEvent.System.HARDKEY2_PRESSED)){
				if(isPlaying()){
					pause();
					try {
						Notify.showButtonNotify(getPlayingAudio(), AudioPlayerService.this, false, isFavourite(getPlayingAudio()));
					} catch (Exception e) {
						e.printStackTrace();
					}
					Intent it = new Intent();
					it.setAction(Contents.MUSICPLAY_STATE_PAUSE);
					sendBroadcast(it);
					isPlayNow = true;
				}else if(isPlayNow){
					isPlayNow = false;
					rusume();
					try {
						Notify.showButtonNotify(getPlayingAudio(), AudioPlayerService.this, true, isFavourite(getPlayingAudio()));
					} catch (Exception e) {
						e.printStackTrace();
					}
					Intent it = new Intent();
					it.setAction(Contents.MUSICPLAY_STATE_PLAY);
					sendBroadcast(it);
				}
			}else if (action.equals(TSDEvent.Interaction.INTERACTION_ERROR)) {
				if(isTTSPause){
					if (isPlayNow) {
						AudioPlayerService.this.rusume();
						isPlayNow = false;
						isTTSPause = false;
					}
				}
				
			}else if(action.equals(CommonApps.SLEEP_PLAY_NEXT)){
				if(isPlaying()){
					next();
					try {
						Notify.showButtonNotify(getPlayingAudio(), AudioPlayerService.this, isPlaying(), isFavourite(getPlayingAudio()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else if(action.equals(CommonApps.SLEEP_PLAY_MUSIC)){
				if(isPlaying()){
					pause();
					isPlayNow = true;
					try {
						Notify.showButtonNotify(getPlayingAudio(), AudioPlayerService.this, false, isFavourite(getPlayingAudio()));
					} catch (Exception e) {
						e.printStackTrace();
					}
					Intent it = new Intent();
					it.setAction(Contents.MUSICPLAY_STATE_PAUSE);
					sendBroadcast(it);
					
				}else if(isPlayNow){
					if(!isPlaying()){
						isPlayNow = false;
						rusume();
						try {
							Notify.showButtonNotify(getPlayingAudio(), AudioPlayerService.this, true, isFavourite(getPlayingAudio()));
						} catch (Exception e) {
							e.printStackTrace();
						}
						Intent it = new Intent();
						it.setAction(Contents.MUSICPLAY_STATE_PLAY);
						sendBroadcast(it);
					}
				}
			}else if(action.equals(CommonApps.SLEEP_PLAY_PRE)){
				if(isPlaying()){
					prew();
					try {
						Notify.showButtonNotify(getPlayingAudio(), AudioPlayerService.this, isPlaying(), isFavourite(getPlayingAudio()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else if(action.equals(CommonApps.SLEEP_START)){
				if(isPlaying()){
					tellLauncherContent(getPlayingAudio().name, true);
				}else if(isPlayNow){
					tellLauncherContent(getPlayingAudio().name, false);
				}
			}
		}
	}

	public List<AudioCategory> readAllCategory() {
		List<CategoryEntity> reads = CategoryDAO.getInstance(this).readAll();
		if (reads == null)
			return null;
		int size = reads.size();
		List<AudioCategory> outs = null;
		if (size > 0) {
			outs = new ArrayList<AudioCategory>();
			for (int i = 0; i < size; i++) {
				AudioCategory rec = reads.get(i).getDetail();
				outs.add(rec);
			}
		}
		return outs;
	}

	private List<AudioItem> readFavouriteItem() {
		List<FavouriteItemEntity> reads = FavouriteItemDAO.getInstance(this)
				.readAll();
		if (reads == null)
			return null;
		int size = reads.size();
		List<AudioItem> outs = null;
		if (size > 0) {
			outs = new ArrayList<AudioItem>();
			for (int i = size - 1; i >= 0; i--) {
				AudioItem rec = reads.get(i).getDetail();
				outs.add(rec);
			}
		}
		return outs;
	}

	private AudioItem getAudioItem(String item, List<AudioItem> itemAll) {
		AudioItem result = null;
		if (itemAll != null) {
			for (int i = 0; i < itemAll.size(); i++) {
				if (item.equals(itemAll.get(i).item)) {
					result = itemAll.get(i);
					break;
				}
			}
		}
		return result;
	}

	public class GetCategoryListTask extends
			MyAsyncTask<Void, Void, GetAudioCategoryListRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetAudioCategoryListRes result) {
			if (result != null) {
				Gson gson = new Gson();
				String resultJson = gson.toJson(result);
				System.out.println("resultJson = " + resultJson);
				if (result.errorCode == 0) {
					Message msg = mHandler.obtainMessage(MSG_GETCATEGORYLIST,
							result);
					mHandler.sendMessage(msg);
					return;
				}
			}
			mHandler.sendEmptyMessage(402);
			categoryListTask=null;
		}

		@Override
		protected GetAudioCategoryListRes doInBackground(Void... arg0) {
			return JsonOA2.getInstance(AudioPlayerService.this)
					.getAudioCategoryList("music");
		}
	}

	public class GetPushCategoryTask extends
			MyAsyncTask<Void, Void, GetAudioCategoryListRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetAudioCategoryListRes result) {
			if (result != null) {
				Gson gson = new Gson();
				String resultJson = gson.toJson(result);
				System.out.println("resultJson = " + resultJson);
				if (result.errorCode == 0) {
					Message msg = mHandler.obtainMessage(MSG_GETPUSHCATEGORY,
							result);
					mHandler.sendMessage(msg);
					return;
				}
			}
		}

		@Override
		protected GetAudioCategoryListRes doInBackground(Void... arg0) {
			return JsonOA2.getInstance(AudioPlayerService.this)
					.getAudioCategory(mPushCategory);
		}
	}

	public class GetFavouriteListTask extends
			MyAsyncTask<Void, Void, GetAudioFavouriteListRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetAudioFavouriteListRes result) {
			if (result != null) {
				Gson gson = new Gson();
				String resultJson = gson.toJson(result);
				System.out.println("resultJsonFavourite = " + resultJson);
				if (result.errorCode == 0) {
					Message msg = mHandler.obtainMessage(MSG_GETFAVOUTITE,
							result);
					mHandler.sendMessage(msg);
					return;
				}
			}
			favouriteListTask=null;
			mHandler.sendEmptyMessage(402);
		}

		@Override
		protected GetAudioFavouriteListRes doInBackground(Void... arg0) {
			return JsonOA2.getInstance(AudioPlayerService.this)
					.getAudioFavouriteList();
		}
	}

	public class GetCategoryDetailListTask extends
			MyAsyncTask<Void, Void, GetAudioCategoryDetailRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetAudioCategoryDetailRes result) {
			if (result != null) {
				Gson gson = new Gson();
				String resultJson = gson.toJson(result);
				System.out.println("resultJson = " + resultJson);
				if (result.errorCode == 0) {
					Message msg = mHandler.obtainMessage(MSG_GETAUDIOITEMLIST,
							result);
					mHandler.sendMessage(msg);
					return;
				}
			}
			categoryDetailTask=null;
			mHandler.sendEmptyMessage(402);
		}

		@Override
		protected GetAudioCategoryDetailRes doInBackground(Void... arg0) {
			return JsonOA2.getInstance(AudioPlayerService.this)
					.getAudioCategoryDetailList(currentCategory);
		}
	}

	public class GetPushCategoryDetailListTask extends
			MyAsyncTask<Void, Void, GetAudioCategoryDetailRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetAudioCategoryDetailRes result) {
			if (result != null) {
				Gson gson = new Gson();
				String resultJson = gson.toJson(result);
				System.out.println("resultJson = " + resultJson);
				if (result.errorCode == 0) {
					Message msg = mHandler.obtainMessage(
							MSG_GETPUSHAUDIOITEMLIST, result);
					mHandler.sendMessage(msg);
				}
			}
		}

		@Override
		protected GetAudioCategoryDetailRes doInBackground(Void... arg0) {
			return JsonOA2.getInstance(AudioPlayerService.this)
					.getAudioCategoryDetailList(pushCategory.category);
		}
	}

	private List<AudioItem> getCacheList() {
		try {
			List<AudioItem> result = new ArrayList<AudioItem>();
			List<AudioItem> items = currentPlayingCategory.item;

			for (int i = 0; i < items.size(); i++) {
				AudioItem temp = items.get(i);
				String filename = items.get(i).item;
				File file = new File(Contents.MP3_PATH, filename);
				if (file.exists()) {// && file.length() == items.get(i).size

					result.add(temp);
				}
			}
			if (result.size() > 0)
				return result;
			else
				return null;
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
    
	public void sendPlayStatus(int status)
	{
		Intent intent=new Intent(PLAY_STATUS);
		intent.putExtra("status", status);
		this.sendBroadcast(intent);
		LogUtil.i("audioplayer","audioplayer status="+status);
	}
	
	private void playAudio(AudioItem item) {
		Intent intent =new Intent(PLAY_ABANDON);
		intent.putExtra("source", 1);
		sendBroadcast(intent);
		if (item != null) {
			try {
				/*
				 * mMediaPlayer.reset(); mMediaPlayer.setDataSource(out.url );
				 * mMediaPlayer.prepare(); mMediaPlayer.start();
				 */
//				boolean isChache=true;
//				if(currentPlayingCategory.cache==1)
//				{
//					isChache=true;
//				}
				Message msg = mHandler.obtainMessage();
				msg.what = MSG_MUSIC_DOBACKGROUND;
				msg.obj = item;
				mHandler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public AudioItem getNextAudio(AudioItem now) {
		AudioItem result = null;
		SharedPreferences preferences = getSharedPreferences("music_order",
				Context.MODE_PRIVATE);
//		int currentMode = 1;
		int currentMode = preferences.getInt("order", 1);

		if (currentPlayingCategory != null) {
			if(currentPlayingCategory.playmode==1)
			{
				currentMode=3;
			}
			List<AudioItem> itemlist = null;
			if (JsonOA2.getInstance(this).checkNetworkInfo() == -1) {
				itemlist = getCacheList();
			} else {
				itemlist = currentPlayingCategory.item;
			}
			if((itemlist!=null)&&(itemlist.size()>0))
			{
			AudioItem item;
			if (now != null) {
				int index = -1;
				int size = itemlist.size();
				for (int i = 0; i < size; i++) {
					if (now.item.equals(itemlist.get(i).item)) {
						index = i;
						playIndex = index;
					}
				}
				if (index != -1) {
					if (currentMode == 1) {
						if (index == size - 1) {
							index = 0;
						} else {
							index++;
						}
						item = itemlist.get(index);
					} else if (currentMode == 3) {
						double temp = Math.random();
						temp = temp * size;
						int tempd = (int) temp;
						if (tempd >= size - 1) {
							tempd = size - 1;
						}
						if (tempd == index) //
						{
							if (index == size - 1) {
								index = 0;
							} else {
								index++;
							}
							item = itemlist.get(index);
						} else {
							item = itemlist.get(tempd);
						}

					} else if (currentMode == 2) {
						item = now;
//						if (index == size - 1) {
//							index = 0;
//						} else {
//							index++;
//						}
//						item = itemlist.get(index);
					} else {
						item = now;
					}

				} else {
					if(playIndex>=currentPlayingCategory.item.size()){
						playIndex = 0;
					}
					item = itemlist.get(playIndex);
				}
			} else {
				item = itemlist.get(0);

			}
			result = item;
			}
		}

		return result;
	}

	public AudioItem getPrewAudio(AudioItem now) {
		AudioItem result = null;
//		SharedPreferences preferences = getSharedPreferences("music_order",
//				Context.MODE_PRIVATE);
		int currentMode =1;// preferences.getInt("order", 1);

		if (currentPlayingCategory != null) {
//			if(currentPlayingCategory.playmode==1)
//			{
//				currentMode=3;
//			}			
			List<AudioItem> itemlist = null;
			if (JsonOA2.getInstance(this).checkNetworkInfo() == -1) {
				itemlist = getCacheList();
			} else {
				itemlist = currentPlayingCategory.item;
			}
			if((itemlist!=null)&&(itemlist.size()>0))
			{
			AudioItem item;
			if (now != null) {
				int index = -1;
				int size = itemlist.size();
				for (int i = 0; i < size; i++) {
					if (now.item.equals(itemlist.get(i).item)) {
						index = i;
						playIndex = index;
					}
				}
				if (index != -1) {
					if (currentMode == 1) {
						if (index == 0) {
							index = size - 1;
						} else {
							index--;
						}
						item = itemlist.get(index);
//					} else if (currentMode == 3) {
//						double temp = Math.random();
//						temp = temp * size;
//						int tempd = (int) temp;
//						if (tempd >= size - 1) {
//							tempd = size - 1;
//						}
//						if (tempd == index) //
//						{
//							if (index == size - 1) {
//								index = 0;
//							} else {
//								index++;
//							}
//							item = itemlist.get(index);
//						} else {
//							item = itemlist.get(tempd);
//						}
//
					} else if (currentMode == 2) {
						item = now;
					} else {
						item = now;
					}

				} else {
					try {
						if(playIndex<=0){
							playIndex = currentPlayingCategory.item.size()-1;
						}
						item = itemlist.get(playIndex);
					} catch (Exception e) {
						playIndex = 0;
						item = itemlist.get(playIndex);
					}
				}
			} else {
				item = itemlist.get(0);

			}

			result = item;
			}
		}

		return result;
	}

	private void produceFavourite() {
		List<AudioItem> favouriteItem = readFavouriteItem();
		List<AudioItem> favouriteItemAdded = mFavouriteManager
				.AddLocalFavourite(favouriteItem);
		myFavourite = new AudioCategory();
		myFavourite.cache = 1;
		myFavourite.category = "favourite";
		myFavourite.description = "favourite";
		myFavourite.image = null;
		myFavourite.name = "我的收藏";
		myFavourite.order = -3;
		if (favouriteItemAdded != null) {
			myFavourite.total = favouriteItemAdded.size();
			myFavourite.item = (ArrayList<AudioItem>) favouriteItemAdded;
			Collections.reverse(myFavourite.item);
		} else {
			myFavourite.total = 0;
			myFavourite.item = null;
		}
	}

	@Override
	public void StartPlayer(String category, String item) {
		
		try {
			if (mMediaPlayer == null) {
				mMediaPlayer = new StreamingMediaPlayer(this);
			} else {
				mMediaPlayer.stop();
			}
			int index = 0;
			for (index = 0; index < myCategories.size(); index++) {
				if (myCategories.get(index).category.equals(category)) {
					break;
				}
			}
			currentPlayingCategory = myCategories.get(index);
			sendAudioStateBroadcast("play");
			AudioItem last = null;
			if (item == null)// 传入参数为空时则自动找歌播放
			{
				last = getCategoryLastPlayItem(currentPlayingCategory.category);
			}			
			if (last != null) {
				playAudio(last);
			} else {
				if (JsonOA2.getInstance(this).checkNetworkInfo() == -1) {
					List<AudioItem> items = getCacheList();
					if (items != null) {
						if (item != null) {
							AudioItem out = getAudioItem(item, items);
							if (out != null) {
								playAudio(out);
							} else {
								playAudio(items.get(0));
							}
						} else {
							playAudio(items.get(0));
						}
					}
				} else {
					if (item == null) {
						playAudio(currentPlayingCategory.item.get(0));
					} else {
						AudioItem out = getAudioItem(item,
								currentPlayingCategory.item);
						if (out != null) {
							playAudio(out);
						} else {
							playAudio(currentPlayingCategory.item.get(0));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void setProgressBroadCast() {
		try {
			if ((mMediaPlayer.mediaPlayer.isPlaying())
					&& (currentPlayingAudio != null)) {
				Intent intent = new Intent(PLAY_PROGRESS);
				Bundle mBundle = new Bundle();
				mBundle.putInt(
						"progress",
						100
								* mMediaPlayer.mediaPlayer.getCurrentPosition()
								/ 1000
								/ (mMediaPlayer.mediaPlayer.getDuration() / 1000));
				intent.putExtras(mBundle);
				AudioPlayerService.this.sendBroadcast(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		
		{
			Intent intent = new Intent(CHACHE_PROGRESS);
			Bundle mBundle = new Bundle();
			mBundle.putInt("chacheprogress", percent);
			intent.putExtras(mBundle);
			AudioPlayerService.this.sendBroadcast(intent);
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		
		sendPlayStatus(0);
		mIsPlaying = false;
		if ((currentPlayingAudio != null) && (currentPlayingCategory != null)) {
			doAfterPlayCompleted();
		}
		mMediaPlayer.stop();
		AudioItem next = nextPlayAudio;
		if (next != null) {
			playAudio(next);
		}
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		
		sendPlayStatus(100);
		mIsPlaying = false;
		return false;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		
		return false;
	}

	@Override
	public void pause() {
		
		mIsPlaying = false;
		isPlayNow = true;
		tellLauncherState(false);
		sendAudioStateBroadcast("pause");
		try {
			if ((mMediaPlayer != null) && (mMediaPlayer.mediaPlayer != null))
			{
				mMediaPlayer.mediaPlayer.pause();    
				mMediaPlayer.mPlayMediaThread.handler.sendEmptyMessage(2);
			}
			sendPlayStatus(3);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void rusume() {
		isPlayNow = false;
		tellLauncherState(true);
		try {
			sendAudioStateBroadcast("resume");
			if ((mMediaPlayer != null) && (mMediaPlayer.mediaPlayer != null)) {
				mMediaPlayer.mediaPlayer.start();
				mIsPlaying = true;
				sendPlayStatus(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		mIsPlaying = false;
		try {
			if ((mMediaPlayer != null) ) {
				mMediaPlayer.stop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}

	@Override
	public List<AudioCategory> getCategoryList() {
		
		if (mFavouriteManager == null)
			mFavouriteManager = new FavouriteManager(this);
		produceFavourite();
		AudioCategory heard = getHeardItemList();
		List<AudioCategory> result = readAllCategory();
		if(result==null)
		{
			return result;
		}
		if (myFavourite != null)// &&(myFavourite.item!=null)&&(myFavourite.item.size()
		{
			if ((myFavourite.item != null) && (myFavourite.item.size() > 0)) {
				myFavourite.image = myFavourite.item.get(0).icon;
			} else {
				myFavourite.image = null;
			}
			boolean isAdded = false;
			for (int i = 0; i < result.size(); i++) {
				if (result.get(i).order >= 0) {

					result.add(i, myFavourite);
					if (heard != null) {
						result.add(i + 1, heard);
					}
					isAdded = true;
					break;
				}
			}
			if (!isAdded) {
				result.add(myFavourite);
				if (heard != null) {
					result.add(heard);
				}
			}
			readPushCategory();
			if (mPushCategorys != null) {
				isAdded = false;
				for (int i = 0; i < result.size(); i++) {
					if (result.get(i).order > 1) {
						for (int j = 0; j < mPushCategorys.size(); j++) {
							result.add(i, mPushCategorys.get(j));
						}
						isAdded = true;
						break;
					}
				}

				if (!isAdded) {
					if (mPushCategorys != null) {
						for (int j = 0; j < mPushCategorys.size(); j++) {
							result.add(mPushCategorys.get(j));
						}
					}
				}			
			}
			
		}
		
		// getIsCache(result);
		myCategories = result;
		if((mIsActivity)&&(myCategories!=null)&&(!isAutoPlayed))
		{
			mHandler.sendEmptyMessage(MSG_AUTOPLAYSTART);
		}		
		return result;
	}

	@Override
	public List<AudioItem> getCategoryDetailList(String category) {
		
		return null;// getCategoryDetail(category);
	}

	@Override
	public AudioItem prew() {
		
		this.stop();
		AudioItem result = null;
		result = getPrewAudio(currentPlayingAudio);
		if (result != null) {
			playAudio(result);
		}
		return result;
	}

	@Override
	public AudioItem next() {
		
		AudioItem result = null;
		this.stop();
		result = nextPlayAudio;
		if (result != null) {
			playAudio(result);
		}
		return result;
	}
	
	@Override
	public int getProgress() {
		
		int result = 0;
		try {
			if ((mMediaPlayer != null) && (mMediaPlayer.mediaPlayer != null)
					&& (mMediaPlayer.mediaPlayer.isPlaying())) {
				result = mMediaPlayer.mediaPlayer.getCurrentPosition() / 1000;
				result = 100 * result / mMediaPlayer.mediaPlayer.getDuration();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public AudioItem getPlayingAudio() {
		
		AudioItem result = resultNow;
		try {
			if (currentPlayingCategory != null) {
				if (currentPlayingAudio != null) {
					int index = -1;
					int size = currentPlayingCategory.item.size();
					for (int i = 0; i < size; i++) {
						if (currentPlayingAudio.item
								.equals(currentPlayingCategory.item.get(i).item)) {
							index = i;
						}
					}
					if (index != -1) {
						result = currentPlayingCategory.item.get(index);
					}
				}
			}
			if(result!=null) //如果在播的歌曲是存在的则广播一下缓存进度
				resultNow = result;
			{
				File filetemp = new File(Contents.MP3_PATH,result.item);
				if (filetemp.exists()) {
					this.onBufferingUpdate(null, 100);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(result==null){
			return resultNow;
		}
		return result;
	}

	@Override
	public int getCacheProgress() {
		
		return 0;
	}

	@Override
	public int setMusicOrder() {
		
		SharedPreferences preferences = getSharedPreferences("music_order",
				Context.MODE_PRIVATE);
		int order = preferences.getInt("order", 1);
//		if (order == 3) {
//			order = 1;
//		} else {
//			order++;
//		}
		if (order == 2) {
			order = 1;
		}else{
			order = 2;
		}
		Editor editor = preferences.edit();
		editor.putInt("order", order);
		editor.commit();
		if (currentPlayingAudio != null) {
			nextPlayAudio = getNextAudio(currentPlayingAudio);
		}
		return order;
	}

	@Override
	public boolean isPlaying() {
		
		boolean result = false;
		try {
			if ((mMediaPlayer != null) && (mMediaPlayer.mediaPlayer != null))
				result = mMediaPlayer.mediaPlayer.isPlaying();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private void changeFavourite(AudioItem item,int type)
	{
		if(type==1)
		{
			if(myFavourite==null)
			{
				myFavourite = new AudioCategory();
				myFavourite.cache = 1;
				myFavourite.category = "favourite";
				myFavourite.description = "favourite";
				myFavourite.image = null;
				myFavourite.name = "我的收藏";
				myFavourite.order = -3;
				myFavourite.total = 0;
				myFavourite.item = new ArrayList<AudioItem>();
			}
			else
			{
				
			}
			
			if((myFavourite!=null)&& (myFavourite.item != null))
			{
				for (int i = 0; i < myFavourite.item.size(); i++) {
					if (item.item.equals(myFavourite.item.get(i).item)) {
						myFavourite.item.remove(i);
						myFavourite.total--;
						i--;
					}
				}
			}
			else if(myFavourite.item==null)
			{
				myFavourite.item = new ArrayList<AudioItem>();
			}
			myFavourite.total++;
			myFavourite.item.add(0,item);
			
		}
		else if(type==2)
		{
			if((myFavourite!=null)&& (myFavourite.item != null))
			{
				for (int i = 0; i < myFavourite.item.size(); i++) {
					if (item.item.equals(myFavourite.item.get(i).item)) {
						myFavourite.item.remove(i);
						myFavourite.total--;
						i--;
					}
				}
			}
			else
			{
				
			}
		}
		LogUtil.i("testaudio", "Favourte="+myFavourite.item.size())	;
		if ((myFavourite.item == null) || (myFavourite.item.size() == 0)) {
			if ((currentPlayingCategory != null)
					&& (currentPlayingCategory.category
							.equals(myFavourite.category))) {
				int index = -1;
				for (int ii = 0; ii < myCategories.size(); ii++) {
					if ((myCategories.get(ii).order == 1)
							&& (myCategories.get(ii).item != null)
							&& (myCategories.get(ii).item.size() > 0)) {
						currentPlayingCategory = myCategories.get(ii);
						index = ii;
						break;
					}
				}
				if (index >= 0) {
					currentPlayingCategory = myCategories.get(index);
				} else {
					currentPlayingCategory = myCategories.get(0);
				}
				nextPlayAudio = getNextAudio(currentPlayingAudio);
			}
		}
		
	}
	
	@Override
	public boolean addFavourite(AudioItem item) {
		
		if (item == null)
			return false;
		if (mFavouriteManager == null)
			mFavouriteManager = new FavouriteManager(this);

		FavouriteRecordEntity cc = new FavouriteRecordEntity();
		FavouriteRecord record = new FavouriteRecord();
		record.item = item;
		record.type = 1;
		cc.setDetail(record);
		FavouriteRecordDAO.getInstance(AudioPlayerService.this).save(cc);
		//produceFavourite();
		changeFavourite(item,1);
		mFavouriteManager.disposeSyn();
		return true;
	}

	@Override
	public boolean deleteFavourite(AudioItem item) {
		
		if (item == null)
			return false;

		if (mFavouriteManager == null)
			mFavouriteManager = new FavouriteManager(this);
		FavouriteRecordEntity cc = new FavouriteRecordEntity();
		FavouriteRecord record = new FavouriteRecord();
		record.item = item;
		record.type = 2;
		cc.setDetail(record);
		FavouriteRecordDAO.getInstance(AudioPlayerService.this).save(cc);
		//produceFavourite();
		changeFavourite(item,2);
		mFavouriteManager.disposeSyn();
		return true;
	}

	@Override
	public boolean isFavourite(AudioItem item) {
		
		if (item != null) {
			if ((myFavourite != null) && (myFavourite.item != null)) {
				for (int i = 0; i < myFavourite.item.size(); i++) {
					if (item.item.equals(myFavourite.item.get(i).item)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public AudioCategory getPlayingCatogory() {
		
		return currentPlayingCategory;
	}

	@Override
	public AudioCategory getHeardCatogory() {
		
		return getHeardItemList();
	}

	public class ServiceBinder extends Binder implements IAudioPlayerService {

		@Override
		public void StartPlayer(String category, String item) {
			
			AudioPlayerService.this.StartPlayer(category, item);
		}

		@Override
		public void pause() {
			
			AudioPlayerService.this.pause();
		}

		@Override
		public void rusume() {
			
			AudioPlayerService.this.rusume();
		}

		@Override
		public void stop() {
			
			AudioPlayerService.this.stop();
		}

		@Override
		public List<AudioCategory> getCategoryList() {
			
			return AudioPlayerService.this.getCategoryList();
		}

		@Override
		public List<AudioItem> getCategoryDetailList(String category) {
			
			return AudioPlayerService.this.getCategoryDetailList(category);
		}

		@Override
		public AudioItem prew() {
			
			return AudioPlayerService.this.prew();
		}

		@Override
		public AudioItem next() {
			
			return AudioPlayerService.this.next();
		}

		@Override
		public int getProgress() {
			
			return AudioPlayerService.this.getProgress();
		}

		@Override
		public AudioItem getPlayingAudio() {
			
			return AudioPlayerService.this.getPlayingAudio();
		}

		@Override
		public int getCacheProgress() {
			
			return AudioPlayerService.this.getCacheProgress();
		}

		@Override
		public int setMusicOrder() {
			
			return AudioPlayerService.this.setMusicOrder();
		}

		@Override
		public boolean isPlaying() {
			
			return AudioPlayerService.this.isPlaying();
		}

		@Override
		public boolean addFavourite(AudioItem item) {
			
			return AudioPlayerService.this.addFavourite(item);
		}

		@Override
		public boolean isFavourite(AudioItem item) {
			
			return AudioPlayerService.this.isFavourite(item);
		}

		@Override
		public AudioCategory getPlayingCatogory() {
			
			return AudioPlayerService.this.getPlayingCatogory();
		}

		@Override
		public boolean deleteFavourite(AudioItem item) {
			
			return AudioPlayerService.this.deleteFavourite(item);
		}

		@Override
		public AudioCategory getHeardCatogory() {
			
			return AudioPlayerService.this.getHeardCatogory();
		}

		@Override
		public void showButtonNotify(AudioItem item,boolean isPlay,boolean isLove) {
			AudioPlayerService.this.showButtonNotify(item,isPlay,isLove);
		}

		@Override
		public int setMusicOrder(int order) {
			return AudioPlayerService.this.setMusicOrder(order);
		}
	}

	public boolean isPlayingAudio() {
		boolean result = false;
		try {
			if ((mMediaPlayer != null) && (mMediaPlayer.mediaPlayer != null))
				if (mMediaPlayer.mediaPlayer.isPlaying()) {
					result = true;
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private void addHeardItem(AudioItem item) {
		List<HeardItemEntity> allHeardItems = HeardItemDAO.getInstance(this)
				.readAll();
		if ((allHeardItems == null) || (allHeardItems.size() == 0)) {
			HeardItemEntity entity = new HeardItemEntity();
			entity.setDetail(item);
			HeardItemDAO.getInstance(this).save(entity);
			return;
		}
		for (int i = 0; i < allHeardItems.size(); i++) {
			if (allHeardItems.get(i).getDetail().item.equals(item.item)) {
				HeardItemDAO.getInstance(this).delete(
						allHeardItems.get(i).getId());
				HeardItemEntity entity = new HeardItemEntity();
				entity.setDetail(item);
				HeardItemDAO.getInstance(this).save(entity);
				return;
			}
		}

		if (allHeardItems.size() >= 50) {
			HeardItemDAO.getInstance(this).delete(allHeardItems.get(0).getId());
		}
		HeardItemEntity entity = new HeardItemEntity();
		entity.setDetail(item);
		HeardItemDAO.getInstance(this).save(entity);
	}

	private AudioCategory getHeardItemList() {
		AudioCategory result = null;
		List<AudioItem> items = null;

		result = new AudioCategory();
		result.cache = 1;
		result.category = "myheard";
		result.description = "myheard";
		result.image = null;
		result.name = "最近听的歌";
		result.order = -4;
		result.item = null;
		result.total = 0;

		List<HeardItemEntity> allHeardItems = HeardItemDAO.getInstance(this)
				.readAll();
		if ((allHeardItems != null) && (allHeardItems.size() > 0)) {
			items = new ArrayList<AudioItem>();
			int size = allHeardItems.size();
			for (int i = size - 1; i >= 0; i--) {
				boolean added = false;
				for (int j = 0; j < items.size(); j++) {
					if (items.get(j).item.equals(allHeardItems.get(i)
							.getDetail().item)) {
						HeardItemDAO.getInstance(this).delete(
								allHeardItems.get(i).getId());
						added = true;
						break;
					}
				}
				if (!added) {
					items.add(allHeardItems.get(i).getDetail());
				}
			}
			result.total = items.size();
			result.item = (ArrayList<AudioItem>) items;
			result.image = items.get(0).icon;

		}

		return result;
	}

	public AudioItem getNextChache() {
		AudioItem result = null;
		AudioItem next1 = nextPlayAudio; // 查询该歌单下一首
		{
			if ((next1 != null) && (next1.item != null)) {
				File temp = new File(next1.item);
				if (!temp.exists()) {
					result = next1;
					return result;
				}
			}
		}
		return result;
	}

	private void addAllHeardItem(AudioItem item) {
		List<HeardAllItemEntity> allHeardItems = HeardAllItemDAO.getInstance(
				this).readAll();
		if ((allHeardItems == null) || (allHeardItems.size() == 0)) {
			HeardAllItemEntity entity = new HeardAllItemEntity();
			entity.setDetail(item.item);
			HeardAllItemDAO.getInstance(this).save(entity);
			return;
		}
		for (int i = 0; i < allHeardItems.size(); i++) {
			if (allHeardItems.get(i).getDetail().equals(item.item)) {
				HeardAllItemDAO.getInstance(this).delete(
						allHeardItems.get(i).getId());
				HeardAllItemEntity entity = new HeardAllItemEntity();
				entity.setDetail(item.item);
				HeardAllItemDAO.getInstance(this).save(entity);
				return;
			}
		}
		if (allHeardItems.size() >= 1000) {
			HeardAllItemDAO.getInstance(this).delete(
					allHeardItems.get(0).getId());
		}

		HeardAllItemEntity entity = new HeardAllItemEntity();
		entity.setDetail(item.item);
		HeardAllItemDAO.getInstance(this).save(entity);
	}
	
	private void clearChacheIconFiles() {
		File dir = new File(Contents.IMAGE_PATH);
		long dirsize = 0;
		if (dir.exists()) {
			try {
				dirsize = GetFileSizeUtil.getInstance().getFileSize(dir);
				if (dirsize >= 30 * 1024 * 1024) {
					File flist[] = dir.listFiles();
					for (int i = 0; i < flist.length; i++) {
						if(flist[i].isFile())
						{
							boolean isFavourite=false;
							String filename=flist[i].getName();
							List<AudioItem> favouritelist = null;
							if ((myFavourite != null)&&(myFavourite.item!=null)) {
								favouritelist = myFavourite.item;
								for(int j=0;j<favouritelist.size();j++)
								{
									String url=favouritelist.get(j).icon;
									String name=url.substring(url.lastIndexOf("/") + 1);
									if((name!=null)&&(name.equals(filename)))
									{
										isFavourite=true;
										break;
									}
								}
							}							
							List<AudioCategory> categorylist=getCategoryList();
							if(categorylist!=null)
							{
								for(int j=0;j<categorylist.size();j++)
								{
									String url=categorylist.get(j).image;
									String name=url.substring(url.lastIndexOf("/") + 1);
									if((name!=null)&&(name.equals(filename)))
									{
										isFavourite=true;
										break;
									}
								}
							}
							
							if(!isFavourite)
							{
								flist[i].delete();
							}							
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	private void clearChacheFiles() {
		File dir = new File(Contents.MP3_PATH);
		long dirsize = 0;
		if (dir.exists()) {
			try {
				dirsize = GetFileSizeUtil.getInstance().getFileSize(dir);
				if (dirsize >= 1000 * 1024 * 1024) {
					do {
						try {
							dirsize = GetFileSizeUtil.getInstance()
									.getFileSize(dir);
						} catch (Exception e) {
							e.printStackTrace();
							break;
						}
						if (dirsize <= 800 * 1024 * 1024) {
							break;
						}
						try {
							clearOneChacheFile(dir);
						} catch (Exception e) {
							break;
						}
					} while (true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void clearOneChacheFile(File dir) throws Exception {
		List<AudioItem> favouritelist = null;
		List<AudioItem> heard50list = null;
		boolean deleted = false;
		if (myFavourite != null) {
			favouritelist = myFavourite.item;
		}
		AudioCategory heard50category = getHeardItemList();
		if (heard50category != null) {
			heard50list = heard50category.item;
		}
		File flist[] = dir.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isFile()) {
				String filename = flist[i].getName();
				if (favouritelist != null) {
					if (isOneOfList(filename, favouritelist)) {
						continue;
					}
				}
				if (heard50list != null) {
					if (isOneOfList(filename, heard50list)) {
						continue;
					}
				}
				flist[i].delete();
				deleted = true;
				break;
			}
		}
		if (deleted) {
			return;
		} else {
			if (deleteMyFavourite()) {
				LogUtil.v("AuidoPlaerservice", "删除收藏成功");
			} else {
				LogUtil.v("AuidoPlaerservice", "删除收藏不成功");
				throw new Exception();
			}
		}
	}

	private boolean deleteMyFavourite()// 删除最近50首之外的，收藏文件，
	{
		boolean result = false;
		List<HeardAllItemEntity> allHeardItems = HeardAllItemDAO.getInstance(
				this).readAll();
		List<AudioItem> heard50list = null;
		AudioCategory heard50category = getHeardItemList();
		if (heard50category != null) {
			heard50list = heard50category.item;
		}
		// List<String> alllist= new ArrayList<String>();
		for (int i = 0; i < allHeardItems.size(); i++) {
			if (isOneOfList(allHeardItems.get(i).getDetail(), heard50list)) {
				continue;
			} else {
				File file = new File(Contents.MP3_PATH, allHeardItems.get(i)
						.getDetail());
				if (file.exists()) {
					file.delete();
					HeardAllItemDAO.getInstance(this).delete(
							allHeardItems.get(i).getId());
					result = true;
					break;
				}
			}
		}
		return result;
	}

	private boolean isOneOfList(String filename, List<AudioItem> list) {
		boolean result = false;
		for (int j = 0; j < list.size(); j++) {
			if (filename.equals(list.get(j).item)) {
				result = true;
				break;
			}
		}
		return result;
	}

	private void doAfterPlayCompleted() {
		if (currentPlayingAudio != null) {
			try {
				if (currentPlayingAudio.item.equals(currentPlayingCategory.item
						.get(currentPlayingCategory.item.size() - 1).item)) {
					sendAudioStateBroadcast("stop");
				}
				
				File file = new File(Contents.MP3_PATH,
						currentPlayingAudio.item);
				if (file.exists()) {
					addHeardItem(currentPlayingAudio);
					addAllHeardItem(currentPlayingAudio);
//				} else {
//
				}
				
				this.sendBroadcast(new Intent(AudioPlayerService.HEARD_LIST));
				clearChacheFiles();
				clearChacheIconFiles();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String getLastAudioItem() {
		return mPref.getString("audioitem", null);
	}

	private void setLastAudioItem(String audioitem) {
		mPref.edit().putString("audioitem", audioitem).commit();
	}

	private void writeLastCategory(AudioCategory category) {
		Gson gson = new Gson();
		String categorystr = gson.toJson(category);
		try {
			FileOutputStream fout = openFileOutput("lastcategory", MODE_PRIVATE);// 获得FileOutputStream
			// 将要写入的字符串转换为byte数组
			byte[] bytes = categorystr.getBytes();
			fout.write(bytes);// 将byte数组写入文件
			fout.close();// 关闭文件输出流
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private AudioCategory readLastCategory() {
		AudioCategory result = null;
		Gson gson = new Gson();
		String categorystr = null;
		try {
			FileInputStream fin = openFileInput("lastcategory");
			// 获取文件长度
			int lenght = fin.available();
			byte[] buffer = new byte[lenght];
			fin.read(buffer);
			// 将byte数组转换成指定格式的字符串
			categorystr = new String(buffer);// EncodingUtils.getString(buffer,
												// ENCODING);
			result = gson.fromJson(categorystr, AudioCategory.class);
			fin.close();// 关闭文件输出流
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private AudioItem getCategoryLastPlayItem(String category) {
		AudioItem result = null;
		String item = null;
		List<CategoryDetailEntity> allrec = CategoryDetailDAO.getInstance(this)
				.readAll();
		if (allrec != null) {
			for (int i = 0; i < allrec.size(); i++) {
				if (allrec.get(i).getDetail().category.equals(category)) {
					item = allrec.get(i).getDetail().item;
					break;
				}
			}
			if ((item != null) && (myCategories != null)) {
				for (int k = 0; k < myCategories.size(); k++) {
					if (myCategories.get(k).category.equals(category)) {
						if (JsonOA2.getInstance(this).checkNetworkInfo() == -1) {
							List<AudioItem> items = getCacheList();
							if (items != null) {
								result = getAudioItem(item, items);
							}
						} else {
							result = getAudioItem(item,
									myCategories.get(k).item);
						}
						break;
					}
				}
			}
		}

		return result;
	}

	private void setCategoryLastPlayItem(String category, String item) {
		List<CategoryDetailEntity> allrec = CategoryDetailDAO.getInstance(this)
				.readAll();
		if (allrec != null) {
			for (int i = 0; i < allrec.size(); i++) {
				if (allrec.get(i).getDetail().category.equals(category)) {
					CategoryDetailDAO.getInstance(this).delete(
							allrec.get(i).getId());
				}
			}
		}
		CategoryDetailEntity newEntity = new CategoryDetailEntity();
		CategoryDetail newDetail = new CategoryDetail();
		newDetail.category = category;
		newDetail.item = item;
		newEntity.setDetail(newDetail);
		CategoryDetailDAO.getInstance(this).save(newEntity);
	}

	private void doAfterPlayAudio() {
		setCategoryLastPlayItem(currentPlayingCategory.category,
				currentPlayingAudio.item);
		setLastAudioItem(currentPlayingAudio.item);
		writeLastCategory(currentPlayingCategory);
	}

	private void autoStartPlayer(String category, String item) {
		
		try {
			if (mMediaPlayer == null) {
				mMediaPlayer = new StreamingMediaPlayer(this);
			} else {
				mMediaPlayer.stop();
			}
			int index = 0;
			for (index = 0; index < myCategories.size(); index++) {
				if (myCategories.get(index).category.equals(category)) {
					break;
				}
			}
			currentPlayingCategory = myCategories.get(index);
			sendAudioStateBroadcast("play");
			AudioItem out = getAudioItem(item, currentPlayingCategory.item);
			if (out == null) {
				playAudio(currentPlayingCategory.item.get(0));
				isAutoPlayed=true;
			} else {
				playAudio(out);
				isAutoPlayed=true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void autoPlayAudio() {
		if (!mIsPlaying) {
			String lastaudio = getLastAudioItem();
			AudioCategory lastcategory = readLastCategory();
			if ((lastaudio != null) && (lastcategory != null)) {
				if (myCategories != null) {
					for (int k = 0; k < myCategories.size(); k++) {
						if (myCategories.get(k).category
								.equals(lastcategory.category)) {
							if ((myCategories.get(k).item!=null)&&(myCategories.get(k).item.size() > 0)) {
								autoStartPlayer(myCategories.get(k).category,
										lastaudio);
								return;
							}
						}
					}
				}
			}
			if (myCategories == null) // 没有歌单
			{
				return;
			} else {
				for (int i = 0; i < myCategories.size(); i++) {
					if (myCategories.get(i).item != null) {
						if (myCategories.get(i).order == -2) // 运营歌单
						{
							if (myCategories.get(i).item.size() > 0) {
								autoStartPlayer(myCategories.get(i).category,
										myCategories.get(i).item.get(0).item);
								return;
							}
						}
						if (myCategories.get(i).order == -4) // 最近听的歌
						{
							try {
								if (myCategories.get(i).item.size() > 0) {
									if (lastaudio != null) {
										AudioItem out = getAudioItem(lastaudio,
												currentPlayingCategory.item);
										if (out != null) {
											autoStartPlayer(
													myCategories.get(i).category,
													out.item);
										} else {
											autoStartPlayer(
													myCategories.get(i).category,
													myCategories.get(i).item.get(0).item);
										}
										return;
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						if (myCategories.get(i).order == -3) // 收藏歌单
						{
							if (myCategories.get(i).item.size() > 0) {
								autoStartPlayer(myCategories.get(i).category,
										myCategories.get(i).item.get(0).item);
								return;
							}
						}
					}
				}
			}
		} else {

		}
	}
	
	private void tellLauncher(String text){
		Intent it = new Intent(CommonApps.SLEEP_SHOW_CONTENT);
		it.putExtra(CommonApps.SLEEP_CONTENT_TITLE, text);
		AudioPlayerService.this.sendBroadcast(it);
	}
	
	private void tellLauncherContent(String text,boolean isPlay){
		Intent it = new Intent(CommonApps.SLEEP_SHOW_CONTENT);
		it.putExtra(CommonApps.SLEEP_CONTENT_TITLE, text);
		it.putExtra(CommonApps.SLEEP_PLAY_MUSIC_NEED_PLAY, isPlay);
		AudioPlayerService.this.sendBroadcast(it);
	}
	
	private void tellLauncherState(boolean isPlay){
		Intent it = new Intent(CommonApps.SLEEP_BEEN_PLAY_MUSIC);
		it.putExtra(CommonApps.SLEEP_PLAY_MUSIC_NEED_PLAY, isPlay);
		AudioPlayerService.this.sendBroadcast(it);
	}

	@Override
	public void showButtonNotify(AudioItem item,boolean isPlay,boolean isLove) {
		Intent intent = new Intent(NEXT_AUDIO);
		Bundle mBundle = new Bundle();
		mBundle.putParcelable("audio", item);
		intent.putExtras(mBundle);
		AudioPlayerService.this.sendBroadcast(intent);
		
		Notify.showButtonNotify(item, this, isPlay, isLove);
		tellLauncher(item.name);
		
	}
	
	void playTtsStart(String text)
	{
		Intent intent= new Intent(CommonMessage.TTS_PLAY);
		intent.putExtra("content", text);
		intent.putExtra("notify", false);
		this.sendBroadcast(intent);		
	}
	
	void sendAudioStateBroadcast(String state)
	{
		if (currentPlayingCategory != null) {
			Intent intent = new Intent(TSDEvent.Audio.STATE);
			AudioState audioState = new AudioState();
			audioState.id = currentPlayingCategory.category;
			audioState.name = currentPlayingCategory.name;
			audioState.type = currentPlayingCategory.type;
			audioState.state = state;
			intent.putExtra("content", audioState);
			this.sendBroadcast(intent);
			LogUtil.i("AudioPlayer","sendAudioState="+state);
		}
		
	}

	@Override
	public int setMusicOrder(int order) {
		SharedPreferences preferences = getSharedPreferences("music_order",Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putInt("order", order);
		editor.commit();
		if (currentPlayingAudio != null) {
			nextPlayAudio = getNextAudio(currentPlayingAudio);
		}
		return order;
	}
}

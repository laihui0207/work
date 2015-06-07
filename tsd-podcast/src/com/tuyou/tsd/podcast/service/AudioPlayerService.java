package com.tuyou.tsd.podcast.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.tuyou.tsd.common.CommonApps;
import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.network.AudioCategory;
import com.tuyou.tsd.common.network.AudioItem;
import com.tuyou.tsd.common.network.AudioState;
import com.tuyou.tsd.common.network.AudioSubscription;
import com.tuyou.tsd.common.network.GetAudioCategoryDetailRes;
import com.tuyou.tsd.common.network.GetAudioCategoryListRes;
import com.tuyou.tsd.common.network.GetAudioSubscriptionListRes;
import com.tuyou.tsd.common.network.JsonOA2;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.common.util.MyAsyncTask;
import com.tuyou.tsd.podcast.MusicActivity;
import com.tuyou.tsd.podcast.comm.Contents;
import com.tuyou.tsd.podcast.db.AudioItemDAO;
import com.tuyou.tsd.podcast.db.AudioItemEntity;
import com.tuyou.tsd.podcast.db.CategoryDAO;
import com.tuyou.tsd.podcast.db.CategoryDetail;
import com.tuyou.tsd.podcast.db.CategoryDetailDAO;
import com.tuyou.tsd.podcast.db.CategoryDetailEntity;
import com.tuyou.tsd.podcast.db.CategoryEntity;
import com.tuyou.tsd.podcast.db.HeardAllItemDAO;
import com.tuyou.tsd.podcast.db.HeardAllItemEntity;
import com.tuyou.tsd.podcast.db.SubscriptionCategoryDAO;
import com.tuyou.tsd.podcast.db.SubscriptionCategoryEntity;
import com.tuyou.tsd.podcast.db.SubscriptionItemDAO;
import com.tuyou.tsd.podcast.db.SubscriptionItemEntity;
import com.tuyou.tsd.podcast.db.SubscriptionRecord;
import com.tuyou.tsd.podcast.db.SubscriptionRecordDAO;
import com.tuyou.tsd.podcast.db.SubscriptionRecordEntity;
import com.tuyou.tsd.podcast.utils.GetFileSizeUtil;
import com.tuyou.tsd.podcast.utils.Notify;

@SuppressLint("NewApi")
public class AudioPlayerService extends Service implements IAudioPlayerService,
		OnBufferingUpdateListener, OnCompletionListener, OnInfoListener,
		OnErrorListener {

	public static final String DATA_REFRESH = "com.tuyou.tsd.podcast.data_refresh";/* 刷新歌单 */
	public static final String PLAY_PROGRESS = "com.tuyou.tsd.podcast.playprogress";/* 播放进度 */
	public static final String CHACHE_PROGRESS = "com.tuyou.tsd.podcast.chacheprogress";/* 缓存进度 */
	public static final String NEXT_AUDIO = "com.tuyou.tsd.podcast.nextaudio";/* 切换播放歌曲 */
	public static final String HEARD_LIST = "com.tuyou.tsd.podcast.heardlist";/* 最近歌曲列表 */
	public static final String PLAY_STATUS = "com.tuyou.tsd.podcast.playstatus";/* 播放状态 */
	public static final String SUBSCRIPTION_STATUS = "com.tuyou.tsd.podcast.subscriptionstatus";/* 播放状态 */

	public static final String PLAY_EXIT = "com.tuyou.tsd.podcast.exit";/* 刷新歌单 */

	public static final String PLAY_ABANDON = "com.tuyou.tsd.audio.abandon";/* 放弃播放 */

	private static final int MSG_GETCATEGORYLIST = 100;
	private static final int MSG_GETFAVOUTITE = 101;
	private static final int MSG_GETAUDIOITEMLIST = 102;
	private static final int MSG_GETPUSHCATEGORY = 103;
	private static final int MSG_GETPUSHAUDIOITEMLIST = 104;
	private static final int MSG_GETALLSUBSCRIPTIONLIST = 105;
	private static final int MSG_GETMYSUBSCRIPTIONLIST = 106;
	private static final int MSG_GETSUBSCRIPTIONITEMLISST = 107;

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
	private GetMySubscriptionListTask mySubscriptionTask = null;
	private GetAllSubscriptionListTask allSubscriptionTask = null;

	private int categoryIndex = 0;
	private AudioCategory currentPlayingCategory = null;// 当前播放歌单
	private AudioItem currentPlayingAudio = null; // 当前播放歌曲
	private AudioItem nextPlayAudio = null;// 下一首歌
	private SubscriptionManager mSubscriptionManager = null; // 我的收藏管理模块
//	private List<AudioCategory> mSubscriptionCategorys = null; // 我订阅的栏目单信息
	private boolean mIsActivity = false; // 是否有界面绑定
	private boolean mIsPlaying = false;
	private GetPushCategoryTask getPushCategoryTask = null; // 获取PUSH歌单信息
	private GetPushCategoryDetailListTask pushCategoryDetailTask = null;// 获取PUSH歌单歌曲信息
	private AudioCategory pushCategory = null;// PUSH歌单列表
	private List<AudioCategory> mPushCategorys = null;
	private SharedPreferences mPref = null;
	private boolean isGetCategory = false; // 是否已经更新过歌单，一般在启动时更新，如果碰到启动时无网络，则有网络后更新
	private boolean isAutoPlayed = false;
	private int mTrygetCategoryTime = 0;
	private List<AudioSubscription> mAudioSubscriptions = null;
	private GetSubscriptionDetailListTask subscriptionDetailTask = null;
//	private AudioCategory mySubscriptionCategory = null;// 当前订阅歌单
	private List<AudioCategory> mSubscriptionCategories = null;// 当前从服务器拉到的歌单列表
	private int subscriptionIndex = 0;
	private boolean isPush = false;
	
	private boolean isPlayNow = false;

	private List<AudioSubscription> listAudio;
	private ArrayList<AudioSubscription> lcs;
	@Override
	public void onCreate() {
		super.onCreate();
		mPref = getSharedPreferences("audioservice", Context.MODE_PRIVATE);

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
//		filter.addAction(TSDEvent.Interaction.INTERACTION_FINISH);
		filter.addAction(TSDEvent.Interaction.INTERACTION_FINISH_FROM_CORE_SERVICE);
		filter.addAction(TSDEvent.Interaction.INTERACTION_ERROR);
//		filter.addAction(TSDEvent.Interaction.INTERACTION_FAILED);
//		filter.addAction(TSDEvent.Interaction.INTERACTION_CANCELED);
		filter.addAction(Contents.KILL_ALL_APP1);
		filter.addAction(Contents.KILL_ALL_APP2);
		filter.addAction(TSDEvent.Audio.PLAY);
		
		filter.addAction(CommonMessage.TTS_PLAY);
		filter.addAction(CommonMessage.TTS_PLAY_FINISHED);
		
		filter.addAction(TSDEvent.System.HARDKEY3_PRESSED);
		filter.addAction(TSDEvent.System.HARDKEY2_PRESSED);
		
		filter.addAction(CommonApps.SLEEP_PLAY_NEXT);
		filter.addAction(CommonApps.SLEEP_PLAY_MUSIC);
		filter.addAction(CommonApps.SLEEP_PLAY_PRE);
		
		registerReceiver(m_myReceiver, filter);

		if (JsonOA2.getInstance(this).checkNetworkInfo() == -1) {

		} else {
			startGetCategory();
			mTrygetCategoryTime = 1;
			isGetCategory = true;
		}
		readPushCategory();
		isAutoPlayed = false;
		mHandler.sendEmptyMessageDelayed(801, 1000 * 30);
		this.sendBroadcast(new Intent(TSDEvent.Podcast.SERVICE_STARTED));
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

		if (mSubscriptionManager != null) {
			mSubscriptionManager.release();
		}
		if (mySubscriptionTask != null) {
			mySubscriptionTask.cancel(true);
		}
		if (allSubscriptionTask != null) {
			allSubscriptionTask.cancel(true);
		}

		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
		}

		mHandler.removeMessages(801);

		this.sendBroadcast(new Intent(TSDEvent.Podcast.SERVICE_STOPPED));
		readLocalSubscription();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		mHandler.sendEmptyMessage(800);
		return START_STICKY;// super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {

		
		mIsActivity = true;
		isAutoPlayed = false;
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
		isAutoPlayed = false;
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

	private void startGetSubscriptionDetail() {
		if ((mSubscriptionCategories != null)
				&& (mSubscriptionCategories.size() > 0)) {
			subscriptionIndex = 0;
			subscriptionDetailTask = new GetSubscriptionDetailListTask();
			subscriptionDetailTask.execute(mSubscriptionCategories
					.get(subscriptionIndex).category);
		}

	}

	private AudioCategory getAudioCategoryFromSubscription(
			AudioSubscription subscription) {
		AudioCategory temp = new AudioCategory();
		temp.category = subscription.album;
		temp.cache = 1;
		temp.description = subscription.name;
		temp.name = subscription.name;
		temp.type = "podcast";
		temp.mode = 1;
		temp.order = 10;
		temp.image = subscription.coverUrl;
		return temp;
	}

	private void startGetPushCategoryDetail() {
		pushCategoryDetailTask = new GetPushCategoryDetailListTask();
		pushCategoryDetailTask.execute();
	}

	private void startGetAllSubscription() {
		allSubscriptionTask = new GetAllSubscriptionListTask();
		allSubscriptionTask.execute();
	}

	private void startGetMySubscription() {
		mySubscriptionTask = new GetMySubscriptionListTask();
		mySubscriptionTask.execute();
	}

	private void readLocalSubscription() {
		try {
			mAudioSubscriptions = readSubscriptions();
			mAudioSubscriptions = mSubscriptionManager.AddLocalSubscription(mAudioSubscriptions);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
					
					//TODO
					startGetCategoryDetail();
				}
				break;
			case MSG_GETFAVOUTITE:

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
					CategoryDAO.getInstance(AudioPlayerService.this)
							.deleteAll();
					for (int i = 0; i < mCategories.size(); i++) {
						CategoryEntity cc = new CategoryEntity();
						cc.setDetail(mCategories.get(i));
						CategoryDAO.getInstance(AudioPlayerService.this).save(
								cc);
					}
					categoryDetailTask = null;
					AudioPlayerService.this.sendBroadcast(new Intent(
							DATA_REFRESH));
					startGetAllSubscription();
				}

				break;
			case MSG_GETPUSHCATEGORY:
				GetAudioCategoryListRes pushcategoryListResres = (GetAudioCategoryListRes) msg.obj;
				if ((pushcategoryListResres.categories != null)
						&& (pushcategoryListResres.categories.length > 0)) {
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

					isAutoPlayed = true;
					addPushCategory();
					getCategoryList();
					AudioPlayerService.this.StartPlayer(pushCategory.category,
							pushCategory.item.get(0).item);

					if (!mIsActivity) {

						Intent it = new Intent(AudioPlayerService.this,
								MusicActivity.class);
						it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						AudioPlayerService.this.startActivity(it);
					}
					AudioPlayerService.this.sendBroadcast(new Intent(
							DATA_REFRESH));
					// mHandler.sendEmptyMessageDelayed(901, 200);
				}
				break;

			case MSG_AUTOPLAYSTART:
			// if((currentPlayingAudio==null)||(currentPlayingCategory==null))
			{
				autoPlayAudio();
			}
				break;
			case MSG_CATEGORYREFRESH:
				AudioPlayerService.this.sendBroadcast(new Intent(DATA_REFRESH));
				break;

			case MSG_GETALLSUBSCRIPTIONLIST:
				if (mAudioSubscriptions != null) {
					mAudioSubscriptions.clear();
				}
				mAudioSubscriptions = new ArrayList<AudioSubscription>();
				GetAudioSubscriptionListRes subscriptionListRes = (GetAudioSubscriptionListRes) msg.obj;
				if ((subscriptionListRes != null)
						&& (subscriptionListRes.albums != null)) {
					for (int i = 0; i < subscriptionListRes.albums.length; i++) {
						AudioSubscription temp = subscriptionListRes.albums[i];
						mAudioSubscriptions.add(temp);
					}
				}
				//TODO
				startGetMySubscription();
				break;
			case MSG_GETMYSUBSCRIPTIONLIST:
				
				//TODO
				GetAudioSubscriptionListRes subscriptionListRes1 = (GetAudioSubscriptionListRes) msg.obj;
				if ((subscriptionListRes1 != null)
						&& (subscriptionListRes1.albums != null)) {
					mSubscriptionCategories = new ArrayList<AudioCategory>();
					for (int i = 0; i < subscriptionListRes1.albums.length; i++) {
						AudioSubscription temp = subscriptionListRes1.albums[i];
						for (int k = 0; k < mAudioSubscriptions.size(); k++) {
							if (temp.album
									.equals(mAudioSubscriptions.get(k).album)) {
								mAudioSubscriptions.get(k).status = 1;
								mSubscriptionCategories
										.add(getAudioCategoryFromSubscription(mAudioSubscriptions
												.get(k)));
								break;
							}
						}
					}
				}

				{
					SubscriptionItemDAO.getInstance(AudioPlayerService.this)
							.deleteAll();
					for (int i = 0; i < mAudioSubscriptions.size(); i++) {
						SubscriptionItemEntity cc = new SubscriptionItemEntity();
						cc.setDetail(mAudioSubscriptions.get(i));
						SubscriptionItemDAO
								.getInstance(AudioPlayerService.this).save(cc);
					}
				}
				AudioPlayerService.this.sendBroadcast(new Intent(
						SUBSCRIPTION_STATUS));
				startGetSubscriptionDetail();
				break;
			case MSG_GETSUBSCRIPTIONITEMLISST:
				GetAudioCategoryDetailRes subscriptionDetailRes = (GetAudioCategoryDetailRes) msg.obj;
				if(isPush){
					AudioCategory aduio = new AudioCategory();
					if (subscriptionDetailRes.items != null) {
						ArrayList<AudioItem> items = new ArrayList<AudioItem>();
						for (int i = 0; i < subscriptionDetailRes.items.length; i++) {
							items.add(subscriptionDetailRes.items[i]);
						}
						aduio.item = items;
						aduio.name = lcs.get(0).name;
						aduio.category = lcs.get(0).album;
						aduio.image = lcs.get(0).coverUrl;
						if(mSubscriptionCategories == null){
							mSubscriptionCategories = new ArrayList<AudioCategory>();
						}
						if(myCategories == null){
							getCategoryList();
						}
						for(int i=0;i<myCategories.size();i++){
							if(myCategories.get(i).category.equals(aduio.category)){
								StartPlayer(lcs.get(0).album, items.get(0).item);
								isPush = false;
								return;
							}
						}
						myCategories.add(aduio);
						mSubscriptionCategories.add(aduio);
						int size = 0;
						if(listAudio==null||listAudio.size()==0){
							listAudio =  getSubscriptionList("podcast");
						}
						if(listAudio!=null){
							for(AudioSubscription cate : listAudio){
								if(isSubscription(cate.album)){
									size ++;
								}
							}
						}
						if(size<6){
							addSubscription(lcs.get(0));
						}else{
							sendBroadcast(new Intent(Contents.DATA_REFRESH_PUSH));
						}
						StartPlayer(lcs.get(0).album, items.get(0).item);
						isPush = false;
					}
				}else{
					if (subscriptionDetailRes.items != null) {
						ArrayList<AudioItem> items = new ArrayList<AudioItem>();
						for (int i = 0; i < subscriptionDetailRes.items.length; i++) {
							items.add(subscriptionDetailRes.items[i]);
						}
						mSubscriptionCategories.get(subscriptionIndex).item = items;
					}
					if (subscriptionIndex < mSubscriptionCategories.size() - 1) {
						subscriptionIndex++;
						subscriptionDetailTask.execute(mSubscriptionCategories
								.get(subscriptionIndex).category);
					} else {
						SubscriptionCategoryDAO
								.getInstance(AudioPlayerService.this).deleteAll();
						for (int i = 0; i < mSubscriptionCategories.size(); i++) {
							SubscriptionCategoryEntity cc = new SubscriptionCategoryEntity();
							cc.setDetail(mSubscriptionCategories.get(i));
							SubscriptionCategoryDAO.getInstance(
									AudioPlayerService.this).save(cc);
						}
						subscriptionDetailTask = null;
						AudioPlayerService.this.sendBroadcast(new Intent(
								DATA_REFRESH));
					}
				}
				break;
			case 401:// 播放中网络断开
				AudioPlayerService.this.onCompletion(null);
				break;
			case 402:// 获取歌单失败
				// mCategories=null;
				mHandler.sendEmptyMessageDelayed(999, 1000 * 20);// 20秒后重新更新
				break;
			case 403:// 获取订阅单失败
				// mCategories=null;
				mHandler.sendEmptyMessageDelayed(999, 1000 * 20);// 20秒后重新更新
				break;
			case 800: // 检查获取歌单
				if ((mCategories == null) && (categoryListTask == null)
						&& (categoryDetailTask == null)) {
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

					startGetCategory();
					mHandler.sendEmptyMessageDelayed(801, 1000 * 50);
				}
				break;
			case 999:
				if (JsonOA2.getInstance(AudioPlayerService.this)
						.checkNetworkInfo() != -1) {
					if (mTrygetCategoryTime < 5) {
						startGetCategory();
						mTrygetCategoryTime++;
					}
				}
				break;
			case 901: // 播放PUSH歌单
				AudioPlayerService.this.StartPlayer(pushCategory.category,
						pushCategory.item.get(0).item);
				break;
			case MSG_TEST:
				disposePushCategory("0a89b221-a878-4d98-9d2f-b04b121fe2d8");
				break;
			case MSG_MUSIC_PLAY:
				if (isPlaying()) {
					pause();
					isPlayNow = true;
					tellLauncherState(false);
					try {
						int size = 0;
						if(listAudio!=null){
							for(AudioSubscription cate : listAudio){
								if(isSubscription(cate.album)){
									size ++;
								}
							}
						}
						if(size>=Contents.ADD_BIG_NUM){
							Notify.showButtonNotify(getPlayingCatogory(),getPlayingAudio(),AudioPlayerService.this, false,isSubscription(getPlayingAudio().albumId),6);
						}else{
							Notify.showButtonNotify(getPlayingCatogory(),getPlayingAudio(),AudioPlayerService.this, false,isSubscription(getPlayingAudio().albumId),0);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					Intent it = new Intent();
					it.setAction(Contents.MUSICPLAY_STATE_PAUSE);
					sendBroadcast(it);
				} else {
					rusume();
					isPlayNow = false;
					tellLauncherState(true);
					try {
						int size = 0;
						if(listAudio!=null){
							for(AudioSubscription cate : listAudio){
								if(isSubscription(cate.album)){
									size ++;
								}
							}
						}
						if(size>=Contents.ADD_BIG_NUM){
							Notify.showButtonNotify(getPlayingCatogory(),getPlayingAudio(),AudioPlayerService.this, true,isSubscription(getPlayingAudio().albumId),6);
						}else{
							Notify.showButtonNotify(getPlayingCatogory(),getPlayingAudio(),AudioPlayerService.this, true,isSubscription(getPlayingAudio().albumId),0);
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					Intent it = new Intent();
					it.setAction(Contents.MUSICPLAY_STATE_PLAY);
					sendBroadcast(it);
				}
				break;
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

	private void addPushCategory() {
		if (mPushCategorys == null) {
			mPushCategorys = new ArrayList<AudioCategory>();
		} else {
			for (int i = 0; i < mPushCategorys.size(); i++)// 去重复
			{
				if (mPushCategorys.get(i).category
						.equals(pushCategory.category)) {
					mPushCategorys.remove(i);
					i--;
				}
			}
		}
		mPushCategorys.add(pushCategory);

		List<AudioItemEntity> pushcategorys = AudioItemDAO.getInstance(
				AudioPlayerService.this).readAll();
		if (pushcategorys != null) {
			for (int k = 0; k < pushcategorys.size(); k++) {
				if (pushcategorys.get(k).getDetail().category
						.equals(pushCategory.category)) {
					AudioItemDAO.getInstance(AudioPlayerService.this).delete(
							pushcategorys.get(k).getId());
				}
			}
		}

		AudioItemEntity entity = new AudioItemEntity();
		entity.setDetail(pushCategory);
		AudioItemDAO.getInstance(AudioPlayerService.this).save(entity);
	}

	private void readPushCategory() {
		List<AudioItemEntity> pushcategorys = AudioItemDAO.getInstance(
				AudioPlayerService.this).readAll();
		if (pushcategorys != null) {
			if (mPushCategorys == null) {
				mPushCategorys = new ArrayList<AudioCategory>();
			} else {
				mPushCategorys.clear();
			}
			for (int k = 0; k < pushcategorys.size(); k++) {

				if (pushcategorys.get(k).getDetail().mode == 1) {
					String timestr = pushcategorys.get(k).getDetail().end;
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ssZ", Locale.CHINA);
					String strTimestamp = dateFormat.format(new Date(System
							.currentTimeMillis()));
					strTimestamp = strTimestamp.substring(0, 22) + ":"
							+ strTimestamp.substring(22);
					if (timestr.compareTo(strTimestamp) > 0) {
						mPushCategorys.add(pushcategorys.get(k).getDetail());
					} else {
						AudioItemDAO.getInstance(AudioPlayerService.this)
								.delete(pushcategorys.get(k).getId());
					}

				} else {
					mPushCategorys.add(pushcategorys.get(k).getDetail());
				}
			}
		}
	}

	private void clearPushCategory() {
		try {
			List<AudioItemEntity> pushcategorys = AudioItemDAO.getInstance(
					AudioPlayerService.this).readAll();
			if (pushcategorys != null) {
				for (int k = 0; k < pushcategorys.size(); k++) {
					if (pushcategorys.get(k).getDetail().mode == 1) {
						String timestr = pushcategorys.get(k).getDetail().end;
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyy-MM-dd'T'HH:mm:ssZ", Locale.CHINA);
						String strTimestamp = dateFormat.format(new Date(System
								.currentTimeMillis()));
						strTimestamp = strTimestamp.substring(0, 22) + ":"
								+ strTimestamp.substring(22);
						if (timestr.compareTo(strTimestamp) > 0) {

						} else {
							AudioItemDAO.getInstance(AudioPlayerService.this)
									.delete(pushcategorys.get(k).getId());
						}
					} else {
						AudioItemDAO.getInstance(AudioPlayerService.this).delete(
								pushcategorys.get(k).getId());
						;
					}
				}
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void clearMySubscriptions() {
		List<SubscriptionCategoryEntity> reads = SubscriptionCategoryDAO
				.getInstance(this).readAll();
		if (reads == null)
			return;
		readLocalSubscription();
		if (mAudioSubscriptions == null)
			return;

		int size = reads.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				AudioCategory rec = reads.get(i).getDetail();
				String id = rec.category;
				if (!isSubscription(id)) {
					SubscriptionCategoryDAO.getInstance(this).delete(
							reads.get(i).getId());
				}
			}
		}

	}
	
	private void isAddSub(){
		if (!isSubscription(getPlayingAudio().albumId)) {

			try {
				int size = 0;
				if(listAudio!=null){
					for(AudioSubscription cate : listAudio){
						if(isSubscription(cate.album)){
							size ++;
						}
					}
				}
				if(size<=6){
					addSubscription((getPlayingAudio().albumId));
					Notify.showButtonNotify(getPlayingCatogory(),getPlayingAudio(),AudioPlayerService.this,isPlaying(), true,0);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		} 
	}

	private class MyReceiver extends BroadcastReceiver {

//		boolean isPause = false;

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(TSDEvent.Push.AUDIO_CATEGORY)) {
				String url = intent.getStringExtra("params");
				String label = url.substring(url.lastIndexOf("=") + 1);
				disposePushCategory(label);
			} else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
				ConnectivityManager manager = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo mobileInfo = manager
						.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				NetworkInfo wifiInfo = manager
						.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				// NetworkInfo activeInfo = manager.getActiveNetworkInfo();
				if (mobileInfo.isConnected() || wifiInfo.isConnected()) {
					if (!isGetCategory) {
						startGetCategory();
						isGetCategory = true;
					}
				}
				if ((!mobileInfo.isConnected()) && (!wifiInfo.isConnected())) {
					// Toast.makeText(AudioPlayerService.this,
					// "亲，网络断开了！",Toast.LENGTH_SHORT).show();
				}
			} else if (action.equals(TSDEvent.System.ACC_ON)) {

			} else if (action.equals(TSDEvent.System.ACC_OFF)) {
				mPushCategorys = null;
				clearPushCategory();
				if (mMediaPlayer != null) {
					mMediaPlayer.stop();
				}
				isPlayNow = false;
				tellLauncher("");
				clearMySubscriptions();
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				notificationManager.cancel(200);
				stop();
			} else if (action.equals(TSDEvent.News.PAUSE)) {
				if (AudioPlayerService.this.isPlaying()) {
					isPlayNow = true;
					AudioPlayerService.this.pause();
					tellLauncherState(false);
				}
			} else if (action.equals(TSDEvent.Podcast.RESUME)) {
				if (isPlayNow) {
					if(!isPlaying()){
						AudioPlayerService.this.rusume();
						isPlayNow = false;
						tellLauncherState(true);
					}
				}
			} else if (action.equals(CommonMessage.VOICE_COMM_SHUT_UP)) {
				if(isPlaying()){
					isPlayNow = true;
					pause();
					Intent it = new Intent();
					it.setAction(Contents.MUSICPLAY_STATE_PAUSE);
					sendBroadcast(it);
				}
				
			} else if (action.equals(TSDEvent.Interaction.INTERACTION_START)) {
				if (AudioPlayerService.this.isPlaying()) {
					isPlayNow = true;
					AudioPlayerService.this.pause();
					tellLauncherState(false);
				}
			} else if (action.equals(TSDEvent.Interaction.INTERACTION_FINISH_FROM_CORE_SERVICE)) {
				if (isPlayNow) {
					if(!isPlaying()){
						AudioPlayerService.this.rusume();
						isPlayNow = false;
						tellLauncherState(true);
					}
				}
		    } else if (action.equals(PLAY_ABANDON)) {
				int pp = intent.getIntExtra("source", 0);
				if (pp != 2) {
					isPlayNow = false;
					tellLauncher("");
					NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					notificationManager.cancel(200);
					AudioPlayerService.this.stop();
					AudioPlayerService.this.sendBroadcast(new Intent(AudioPlayerService.PLAY_EXIT));
				}
			} else if (action.equals(Contents.ACTION_BUTTON)) {
				int buttonId = intent.getIntExtra(Contents.INTENT_PODCAST_BUTTONID_TAG,
						0);
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
					isAddSub();
					break;
				}
			}else if(intent.getAction().equals(Contents.KILL_ALL_APP1)||intent.getAction().equals(Contents.KILL_ALL_APP2)){
				if (mMediaPlayer != null) {
					mMediaPlayer.stop();
				}
				isPlayNow = false;
				tellLauncher("");
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				notificationManager.cancel(200);
			}else if(intent.getAction().equals(TSDEvent.Audio.PLAY)){
				if(intent.getExtras().getString("type").equals("podcast")){
					isPush = true;
					String str = intent.getExtras().getString("playlist");
					lcs = new Gson().fromJson(str,new TypeToken<ArrayList<AudioSubscription>>() {}.getType());
					subscriptionDetailTask = new GetSubscriptionDetailListTask();
					subscriptionDetailTask.execute(lcs.get(0).album);
				}
			}
			else if(intent.getAction().equals(CommonMessage.TTS_PLAY)){
				if(isPlaying()){
					pause();
					try {
						int size = 0;
						if(listAudio!=null){
							for(AudioSubscription cate : listAudio){
								if(isSubscription(cate.album)){
									size ++;
								}
							}
						}
						if(size>=Contents.ADD_BIG_NUM){
							Notify.showButtonNotify(getPlayingCatogory(),getPlayingAudio(),AudioPlayerService.this, false,isSubscription(getPlayingAudio().albumId),6);
						}else{
							Notify.showButtonNotify(getPlayingCatogory(),getPlayingAudio(),AudioPlayerService.this, false,isSubscription(getPlayingAudio().albumId),0);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					Intent it = new Intent();
					it.setAction(Contents.MUSICPLAY_STATE_PAUSE);
					sendBroadcast(it);
					isPlayNow = true;
					tellLauncherState(false);
				}
				
			}else if(intent.getAction().equals(CommonMessage.TTS_PLAY_FINISHED)){
				if(isPlayNow){
					if(!isPlaying()){
						isPlayNow = false;
						tellLauncherState(true);
						rusume();
						try {
							int size = 0;
							if(listAudio!=null){
								for(AudioSubscription cate : listAudio){
									if(isSubscription(cate.album)){
										size ++;
									}
								}
							}
							if(size>=Contents.ADD_BIG_NUM){
								Notify.showButtonNotify(getPlayingCatogory(),getPlayingAudio(),AudioPlayerService.this, true,isSubscription(getPlayingAudio().albumId),6);
							}else{
								Notify.showButtonNotify(getPlayingCatogory(),getPlayingAudio(),AudioPlayerService.this, true,isSubscription(getPlayingAudio().albumId),0);
							}
							
						} catch (Exception e) {
							e.printStackTrace();
						}
						Intent it = new Intent();
						it.setAction(Contents.MUSICPLAY_STATE_PLAY);
						sendBroadcast(it);
					}
				}
			}else if(intent.getAction().equals(TSDEvent.System.HARDKEY3_PRESSED)){
				if(isPlaying()||isPlayNow){
					isAddSub();
				}
			}else if(intent.getAction().equals(TSDEvent.System.HARDKEY2_PRESSED)){
				if(isPlayNow){
					Message msg = mHandler.obtainMessage(MSG_MUSIC_PLAY);
					mHandler.sendMessage(msg);
				}else if(isPlaying()){
					Message msg = mHandler.obtainMessage(MSG_MUSIC_PLAY);
					mHandler.sendMessage(msg);
				}
			}else if (action.equals(TSDEvent.Interaction.INTERACTION_ERROR)) {
				if (isPlayNow) {
					if(!isPlaying()){
						AudioPlayerService.this.rusume();
						isPlayNow = false;
						tellLauncherState(true);
					}
				}
			}else if(action.equals(CommonApps.SLEEP_PLAY_NEXT)){
				if(isPlaying()){
					next();
					try {
						int size = 0;
						if(listAudio!=null){
							for(AudioSubscription cate : listAudio){
								if(isSubscription(cate.album)){
									size ++;
								}
							}
						}
						if(size>=Contents.ADD_BIG_NUM){
							Notify.showButtonNotify(getPlayingCatogory(), getPlayingAudio(),AudioPlayerService.this, true, isSubscription(getPlayingAudio().albumId),6);
						}else{
							Notify.showButtonNotify(getPlayingCatogory(), getPlayingAudio(),AudioPlayerService.this, true, isSubscription(getPlayingAudio().albumId),0);
						}
						 
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else if(action.equals(CommonApps.SLEEP_PLAY_MUSIC)){
				if(isPlaying()){
					pause();
					try {
						int size = 0;
						if(listAudio!=null){
							for(AudioSubscription cate : listAudio){
								if(isSubscription(cate.album)){
									size ++;
								}
							}
						}
						if(size>=Contents.ADD_BIG_NUM){
							Notify.showButtonNotify(getPlayingCatogory(), getPlayingAudio(),AudioPlayerService.this, false, isSubscription(getPlayingAudio().albumId),6);
						}else{
							Notify.showButtonNotify(getPlayingCatogory(), getPlayingAudio(),AudioPlayerService.this, false, isSubscription(getPlayingAudio().albumId),0);
						}
						 
					} catch (Exception e) {
						e.printStackTrace();
					}
					Intent it = new Intent();
					it.setAction(Contents.MUSICPLAY_STATE_PAUSE);
					sendBroadcast(it);
					isPlayNow = true;
					tellLauncherState(false);
				}else if(isPlayNow){
					if(!isPlaying()){

						isPlayNow = false;
						tellLauncherState(true);
						rusume();
						try {
							int size = 0;
							if(listAudio!=null){
								for(AudioSubscription cate : listAudio){
									if(isSubscription(cate.album)){
										size ++;
									}
								}
							}
							if(size>=Contents.ADD_BIG_NUM){
								Notify.showButtonNotify(getPlayingCatogory(), getPlayingAudio(),AudioPlayerService.this, true, isSubscription(getPlayingAudio().albumId),6);
							}else{
								Notify.showButtonNotify(getPlayingCatogory(), getPlayingAudio(),AudioPlayerService.this, true, isSubscription(getPlayingAudio().albumId),0);
							}
							 
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
						int size = 0;
						if(listAudio!=null){
							for(AudioSubscription cate : listAudio){
								if(isSubscription(cate.album)){
									size ++;
								}
							}
						}
						if(size>=Contents.ADD_BIG_NUM){
							Notify.showButtonNotify(getPlayingCatogory(), getPlayingAudio(),AudioPlayerService.this, true, isSubscription(getPlayingAudio().albumId),6);
						}else{
							Notify.showButtonNotify(getPlayingCatogory(), getPlayingAudio(),AudioPlayerService.this, true, isSubscription(getPlayingAudio().albumId),0);
						}
						 
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public List<AudioCategory> readCommonCategory() {
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

	public List<AudioCategory> readSubscriptionCategory() {
		List<SubscriptionCategoryEntity> reads = SubscriptionCategoryDAO
				.getInstance(this).readAll();
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

	public List<AudioSubscription> readSubscriptions() {
		List<SubscriptionItemEntity> reads = SubscriptionItemDAO.getInstance(
				this).readAll();
		if (reads == null)
			return null;
		int size = reads.size();
		List<AudioSubscription> outs = null;
		if (size > 0) {
			outs = new ArrayList<AudioSubscription>();
			for (int i = 0; i < size; i++) {
				AudioSubscription rec = reads.get(i).getDetail();
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
			categoryListTask = null;
		}

		@Override
		protected GetAudioCategoryListRes doInBackground(Void... arg0) {
			return JsonOA2.getInstance(AudioPlayerService.this)
					.getAudioCategoryList("podcast");
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
				System.out.println("resultJson5 = " + resultJson);
				if (result.errorCode == 0) {
					Message msg = mHandler.obtainMessage(MSG_GETAUDIOITEMLIST,
							result);
					mHandler.sendMessage(msg);
					return;
				}
			}
			categoryDetailTask = null;
			mHandler.sendEmptyMessage(402);
		}

		@Override
		protected GetAudioCategoryDetailRes doInBackground(Void... arg0) {
			return JsonOA2.getInstance(AudioPlayerService.this)
					.getAudioCategoryDetailList(currentCategory);
		}
	}

	public class GetSubscriptionDetailListTask extends
			MyAsyncTask<String, Void, GetAudioCategoryDetailRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetAudioCategoryDetailRes result) {
			if (result != null) {
				Gson gson = new Gson();
				String resultJson = gson.toJson(result);
				System.out.println("resultJson2 = " + resultJson);
				if (result.errorCode == 0) {
					Message msg = mHandler.obtainMessage(
							MSG_GETSUBSCRIPTIONITEMLISST, result);
					mHandler.sendMessage(msg);
					return;
				}
			}
			subscriptionDetailTask = null;
		}

		@Override
		protected GetAudioCategoryDetailRes doInBackground(String... arg0) {
			return JsonOA2.getInstance(AudioPlayerService.this)
					.getAudioSubscriptionDetail(arg0[0]);
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

	public class GetAllSubscriptionListTask extends
			MyAsyncTask<Void, Void, GetAudioSubscriptionListRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetAudioSubscriptionListRes result) {
			if (result != null) {
				Gson gson = new Gson();
				String resultJson = gson.toJson(result);
				System.out.println("resultJson4 = " + resultJson);
				if (result.errorCode == 0) {
					Message msg = mHandler.obtainMessage(
							MSG_GETALLSUBSCRIPTIONLIST, result);
					mHandler.sendMessage(msg);
				}
			}
		}

		@Override
		protected GetAudioSubscriptionListRes doInBackground(Void... arg0) {
			return JsonOA2.getInstance(AudioPlayerService.this)
					.getAudioAllSubscriptionList("podcast");
		}
	}

	public class GetMySubscriptionListTask extends
			MyAsyncTask<Void, Void, GetAudioSubscriptionListRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetAudioSubscriptionListRes result) {
			if (result != null) {
				Gson gson = new Gson();
				String resultJson = gson.toJson(result);
				System.out.println("resultJson3 = " + resultJson);
				if (result.errorCode == 0) {
					Message msg = mHandler.obtainMessage(
							MSG_GETMYSUBSCRIPTIONLIST, result);
					mHandler.sendMessage(msg);
				}
			}
		}

		@Override
		protected GetAudioSubscriptionListRes doInBackground(Void... arg0) {
			return JsonOA2.getInstance(AudioPlayerService.this)
					.getAudioMySubscriptionList("podcast");
		}
	}

	private List<AudioItem> getCacheList() {
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
	}

	public void sendPlayStatus(int status) {
		Intent intent = new Intent(PLAY_STATUS);
		intent.putExtra("status", status);
		this.sendBroadcast(intent);
		LogUtil.i("audioplayer", "audioplayer status=" + status);
	}

	private void playAudio(AudioItem item, int progress) {
		Intent intent = new Intent(PLAY_ABANDON);
		intent.putExtra("source", 2);
		sendBroadcast(intent);
		if (item != null) {
			try {

				boolean isChache = false;
				if (currentPlayingCategory.cache == 1) {
					isChache = true;
				}
				currentPlayingAudio = item;
				nextPlayAudio = getNextAudio(currentPlayingAudio);
				int result = mMediaPlayer.startStreaming(item.url, item.item,
						item.size, progress * 1000, isChache);
				try {
					showButtonNotify(item, true, isSubscription(item.albumId));
				} catch (Exception e) {
					e.printStackTrace();
				}
				doAfterPlayAudio();
				mIsPlaying = true;
				if (result == 1) {
					this.onBufferingUpdate(null, 100);
					sendPlayStatus(1);
				} else {
					sendPlayStatus(2);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setLastAudioProgress(0);
	}

	public AudioItem getNextAudio(AudioItem now) {
		AudioItem result = null;

		if (currentPlayingCategory != null) {
			List<AudioItem> itemlist = null;
			if (JsonOA2.getInstance(this).checkNetworkInfo() == -1) {
				itemlist = getCacheList();
			} else {
				itemlist = currentPlayingCategory.item;
			}
			if ((itemlist != null) && (itemlist.size() > 0)) {
				AudioItem item = null;
				if (now != null) {
					int index = -1;
					int size = itemlist.size();
					for (int i = 0; i < size; i++) {
						if (now.item.equals(itemlist.get(i).item)) {
							index = i;
						}
					}
					if (index != -1) {
						if (index < size - 1) {
							index++;
							item = itemlist.get(index);
						}
					}
					result = item;
				}
			}
		}
		return result;
	}

	public AudioItem getPrewAudio(AudioItem now) {
		AudioItem result = null;

		if (currentPlayingCategory != null) {
			List<AudioItem> itemlist = null;
			if (JsonOA2.getInstance(this).checkNetworkInfo() == -1) {
				itemlist = getCacheList();
			} else {
				itemlist = currentPlayingCategory.item;
			}
			if ((itemlist != null) && (itemlist.size() > 0)) {
				AudioItem item;
				if (now != null) {
					int index = -1;
					int size = itemlist.size();
					for (int i = 0; i < size; i++) {
						if (now.item.equals(itemlist.get(i).item)) {
							index = i;
						}
					}
					if (index != -1) {
						if (index > 0) {
							index--;
							item = itemlist.get(index);
							result = item;
						}
					}
				}
			}
		}
		return result;
	}

//	private void produceFavourite() {
//
//	}

	@Override
	public void StartPlayer(String category, String item) {
		
		try {
			if (mMediaPlayer == null) {
				mMediaPlayer = new StreamingMediaPlayer(this);
			} else {
				if ((mMediaPlayer.mediaPlayer != null)
						&& (mMediaPlayer.mediaPlayer.isPlaying())) {
					setCategoryLastPlayItem(
							currentPlayingCategory.category,
							currentPlayingAudio.item,
							mMediaPlayer.mediaPlayer.getCurrentPosition() / 1000);
				}
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
			LastAudioItem last = null;
			if (item == null)// 传入参数为空时则自动找歌播放
			{
				last = getCategoryLastPlayItem(currentPlayingCategory.category);
			}
			if (last != null) {
				playAudio(last.item, last.progress);
			} else {
				if (item == null) {
					playAudio(currentPlayingCategory.item.get(0), 0);
				} else {
					AudioItem out = getAudioItem(item,
							currentPlayingCategory.item);
					if (out != null) {
						playAudio(out, 0);
					} else {
						playAudio(currentPlayingCategory.item.get(0), 0);
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
				setLastAudioProgress(mMediaPlayer.mediaPlayer
						.getCurrentPosition() / 1000);
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
			playAudio(next, 0);
		} else {
			currentPlayingAudio = null;
			currentPlayingCategory = null;
			sendPlayStatus(3);
			setLastAudioProgress(100);
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
		sendAudioStateBroadcast("pause");
		try {
			if ((mMediaPlayer != null) && (mMediaPlayer.mediaPlayer != null)) {
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
			if ((mMediaPlayer != null)) {
				mMediaPlayer.stop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public List<AudioCategory> getCategoryList() {
		
		if (mSubscriptionManager == null)
			mSubscriptionManager = new SubscriptionManager(this);

//		produceFavourite();

		List<AudioCategory> result = readCommonCategory();

		List<AudioCategory> result1 = readSubscriptionCategory();

		if ((result == null) && (result1 == null)) {
			return result;
		}
		if (result1 != null) {
//			boolean first = true;
			for (int i = 0; i < result1.size(); i++) {
				if (isSubscription(result1.get(i).category)) {
//					if (first) {
//						first = false;
//						result.add(0, result1.get(i));
//					} else {
//						result.add(result1.get(i));
//					}
					result.add(result1.get(i));
				} else {
					if ((currentPlayingCategory != null)
							&& (result1.get(i).category
									.equals(currentPlayingCategory.category))) {
//						if (first) {
//							first = false;
//							result.add(0, result1.get(i));
//						} else {
//							result.add(result1.get(i));
//						}
						result.add(result1.get(i));
					}
				}
			}
		}
		myCategories = result;
		if ((mIsActivity) && (myCategories != null) && (!isAutoPlayed)) {
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
		
		AudioItem result = null;
		result = getPrewAudio(currentPlayingAudio);
		if (result != null) {
			this.stop();
			playAudio(result, 0);
		}
		return result;
	}

	@Override
	public AudioItem next() {
		
		AudioItem result = null;
		result = nextPlayAudio;
		if (result != null) {
			this.stop();
			playAudio(result, 0);
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
		AudioItem result = null;
		try {
			if (currentPlayingCategory != null) {
				if (currentPlayingAudio != null) {
					int index = -1;
					int size = currentPlayingCategory.item.size();
					for (int i = 0; i < size; i++) {
						if (currentPlayingAudio.item.equals(currentPlayingCategory.item.get(i).item)) {
							index = i;
						}
					}
					if (index != -1) {
						result = currentPlayingCategory.item.get(index);
					}
				}
			}
			if (result != null) // 如果在播的歌曲是存在的则广播一下缓存进度
			{
				File filetemp = new File(Contents.MP3_PATH, result.item);
				if (filetemp.exists()) {
					this.onBufferingUpdate(null, 100);
				}
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public int getCacheProgress() {
		
		return 0;
	}

	@Override
	public int setMusicOrder() {
		

		return 1;
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

	private void changeFavourite(AudioItem item, int type) {
		if (type == 1) {

		} else if (type == 2) {

		}
	}

//	@Override
//	public boolean addFavourite(AudioItem item) {
//		
//		if (item == null)
//			return false;
//		changeFavourite(item, 1);
//		return true;
//	}

//	@Override
//	public boolean deleteFavourite(AudioItem item) {
//		
//		if (item == null)
//			return false;
//		changeFavourite(item, 2);
//		return true;
//	}
//
//	@Override
//	public boolean isFavourite(AudioItem item) {
//		
//		if (item != null) {
//
//		}
//		return false;
//	}

	@Override
	public List<AudioSubscription> getSubscriptionList(String type) {
		
		if (mSubscriptionManager == null)
			mSubscriptionManager = new SubscriptionManager(this);
		if (mAudioSubscriptions == null) {
			readLocalSubscription();
		}

		return mAudioSubscriptions;
	}

	private void changeSubscription(AudioSubscription item, int type) {
		if (type == 1) {
			if (mAudioSubscriptions != null) {
				for (int i = 0; i < mAudioSubscriptions.size(); i++) {
					if (item.album.equals(mAudioSubscriptions.get(i).album)) {
						mAudioSubscriptions.get(i).status = 1;
						return;
					}
				}
				item.status = 1;
				mAudioSubscriptions.add(item);
			} else {
				mAudioSubscriptions = new ArrayList<AudioSubscription>();
				item.status = 1;
				mAudioSubscriptions.add(item);
			}
		} else if (type == 2) {
			if (mAudioSubscriptions != null) {
				for (int i = 0; i < mAudioSubscriptions.size(); i++) {
					if (item.album.equals(mAudioSubscriptions.get(i).album)) {
						mAudioSubscriptions.get(i).status = 0;
						return;
					}
				}
				item.status = 0;
				mAudioSubscriptions.add(item);
			} else {
				mAudioSubscriptions = new ArrayList<AudioSubscription>();
				item.status = 0;
				mAudioSubscriptions.add(item);
			}
		}
	}

	@Override
	public boolean addSubscription(AudioSubscription item) {
		
		if (item == null)
			return false;
		if (mAudioSubscriptions == null) {
			readLocalSubscription();
		}
		if (mSubscriptionManager == null)
			mSubscriptionManager = new SubscriptionManager(this);

		SubscriptionRecordEntity cc = new SubscriptionRecordEntity();
		SubscriptionRecord record = new SubscriptionRecord();
		record.item = item;
		record.type = 1;
		cc.setDetail(record);
		SubscriptionRecordDAO.getInstance(AudioPlayerService.this).save(cc);
		changeSubscription(item, 1);
		mSubscriptionManager.disposeSyn();
		return true;
	}

	@Override
	public boolean addSubscription(String album) {
		
		AudioSubscription item = null;
		if (album == null) {
			return false;
		}
		if (mAudioSubscriptions == null) {
			readLocalSubscription();
		}
		if (mSubscriptionManager == null)
			mSubscriptionManager = new SubscriptionManager(this);

		for (int i = 0; i < mAudioSubscriptions.size(); i++) {
			if (album.equals(mAudioSubscriptions.get(i).album)) {
				item = mAudioSubscriptions.get(i);
			}
		}
		if (item == null) {
			return false;
		} else {
			return addSubscription(item);
		}
	}

	@Override
	public boolean deleteSubscription(AudioSubscription item) {
		
		if (item == null)
			return false;
		if (mAudioSubscriptions == null) {
			readLocalSubscription();
		}
		if (mSubscriptionManager == null)
			mSubscriptionManager = new SubscriptionManager(this);

		SubscriptionRecordEntity cc = new SubscriptionRecordEntity();
		SubscriptionRecord record = new SubscriptionRecord();
		record.item = item;
		record.type = 2;
		cc.setDetail(record);
		SubscriptionRecordDAO.getInstance(AudioPlayerService.this).save(cc);
		changeSubscription(item, 2);
		mSubscriptionManager.disposeSyn();

		if ((currentPlayingCategory != null)
				&& (currentPlayingCategory.category.equals(item.album))) {
		} else {
			List<SubscriptionCategoryEntity> ccList = SubscriptionCategoryDAO
					.getInstance(AudioPlayerService.this).readAll();
			if (ccList != null) {
				for (int i = 0; i < ccList.size(); i++) {
					if (ccList.get(i).getDetail().category.equals(item.album)) {
						SubscriptionCategoryDAO.getInstance(
								AudioPlayerService.this).delete(
								ccList.get(i).getId());
						break;
					}
				}
			}
		}
		this.sendBroadcast(new Intent(DATA_REFRESH));
		return true;
	}

	@Override
	public boolean isSubscription(AudioSubscription item) {
		
		if (mAudioSubscriptions == null) {
			readLocalSubscription();
		}
		if (mAudioSubscriptions == null)
			return false;
		for (int i = 0; i < mAudioSubscriptions.size(); i++) {
			if (item.album.equals(mAudioSubscriptions.get(i).album)) {
				if (mAudioSubscriptions.get(i).status == 0) {
					return false;
				} else {
					return true;
				}
			}
		}
		return false;
	}
	

	@Override
	public boolean isSubscription(String item) {
		if (mAudioSubscriptions == null) {
			readLocalSubscription();
		}
		if (mAudioSubscriptions == null)
			return false;
		if (item == null) {
			return false;
		}
		for (int i = 0; i < mAudioSubscriptions.size(); i++) {
			if (item.equals(mAudioSubscriptions.get(i).album)) {
				if (mAudioSubscriptions.get(i).status == 0) {
					return false;
				} else {
					return true;
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
		
		return null;// getHeardItemList();
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
		public List<AudioCategory> getCategoryListPush(){
			return  AudioPlayerService.this.getCategoryListPush();
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

//		@Override
//		public boolean addFavourite(AudioItem item) {
//			
//			return AudioPlayerService.this.addFavourite(item);
//		}
//
//		@Override
//		public boolean isFavourite(AudioItem item) {
//			
//			return AudioPlayerService.this.isFavourite(item);
//		}

		@Override
		public AudioCategory getPlayingCatogory() {
			
			return AudioPlayerService.this.getPlayingCatogory();
		}

//		@Override
//		public boolean deleteFavourite(AudioItem item) {
//			
//			return AudioPlayerService.this.deleteFavourite(item);
//		}

		@Override
		public AudioCategory getHeardCatogory() {
			
			return AudioPlayerService.this.getHeardCatogory();
		}

		@Override
		public void showButtonNotify(AudioItem item, boolean isPlay,
				boolean isLove) {
			AudioPlayerService.this.showButtonNotify(item, isPlay, isLove);
		}

		@Override
		public List<AudioSubscription> getSubscriptionList(String type) {
			
			return AudioPlayerService.this.getSubscriptionList(type);
		}

		@Override
		public boolean addSubscription(AudioSubscription item) {
			
			return AudioPlayerService.this.addSubscription(item);
		}

		@Override
		public boolean deleteSubscription(AudioSubscription item) {
			
			return AudioPlayerService.this.deleteSubscription(item);
		}

		@Override
		public boolean isSubscription(AudioSubscription item) {
			
			return AudioPlayerService.this.isSubscription(item);
		}

		@Override
		public boolean isSubscription(String item) {
			
			return AudioPlayerService.this.isSubscription(item);
		}

		@Override
		public boolean addSubscription(String album) {
			
			return AudioPlayerService.this.addSubscription(album);
		}

		@Override
		public boolean isHeard(String item) {
			
			return AudioPlayerService.this.isHeard(item);
		}

		@Override
		public void setList(List<AudioSubscription> listAudio) {
			
			 AudioPlayerService.this.setList(listAudio);
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

	private void clearChacheIconFiles() {
		File dir = new File(Contents.IMAGE_PATH);
		long dirsize = 0;
		if (dir.exists()) {
			try {
				dirsize = GetFileSizeUtil.getInstance().getFileSize(dir);
				if (dirsize >= 30 * 1024 * 1024) {
					File flist[] = dir.listFiles();
					for (int i = 0; i < flist.length; i++) {
						if (flist[i].isFile()) {
							boolean isFavourite = false;
							String filename = flist[i].getName();
							if (mAudioSubscriptions != null) {
								for (int j = 0; j < mAudioSubscriptions.size(); j++) {
									String url = mAudioSubscriptions.get(j).coverUrl;
									String name = url.substring(url
											.lastIndexOf("/") + 1);
									if ((name != null)
											&& (name.equals(filename))) {
										isFavourite = true;
										break;
									}
								}
							}
							List<AudioCategory> categorylist = getCategoryList();
							if (categorylist != null) {
								for (int j = 0; j < categorylist.size(); j++) {
									String url = categorylist.get(j).image;
									String name = url.substring(url
											.lastIndexOf("/") + 1);
									if ((name != null)
											&& (name.equals(filename))) {
										isFavourite = true;
										break;
									}
								}
							}

							if (!isFavourite) {
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
		if (dir.exists()) {
			try {
				File flist[] = dir.listFiles();
				for (int i = 0; i < flist.length; i++) {
					if (flist[i].isFile()) {
						long lasttime = flist[i].lastModified();
						Date nowtime = new Date();
						if (nowtime.getTime() > (lasttime + 24 * 3600 * 1000)) {
							flist[i].delete();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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

				} else {

				}
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

	private int getLastAudioProgress() {
		return mPref.getInt("audioprogress", 0);
	}

	private void setLastAudioProgress(int audioprogress) {
		mPref.edit().putInt("audioprogress", audioprogress).commit();
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

	public class LastAudioItem {
		public AudioItem item;
		public int progress;
	}

	private LastAudioItem getCategoryLastPlayItem(String category) {
		AudioItem result = null;
		CategoryDetail item = null;
		List<CategoryDetailEntity> allrec = CategoryDetailDAO.getInstance(this)
				.readAll();
		if (allrec != null) {
			for (int i = 0; i < allrec.size(); i++) {
				if (allrec.get(i).getDetail().category.equals(category)) {
					item = allrec.get(i).getDetail();
					break;
				}
			}
			if ((item != null) && (myCategories != null)) {
				for (int k = 0; k < myCategories.size(); k++) {
					if (myCategories.get(k).category.equals(category)) {
						if (JsonOA2.getInstance(this).checkNetworkInfo() == -1) {
							List<AudioItem> items = getCacheList();
							if (items != null) {
								result = getAudioItem(item.item, items);
							}
						} else {
							result = getAudioItem(item.item,
									myCategories.get(k).item);
						}
						break;
					}
				}
			}
		}
		if ((result == null) || (item == null))
			return null;
		else {
			LastAudioItem end = new LastAudioItem();
			end.item = result;
			end.progress = item.progress;
			return end;
		}

	}

	private void setCategoryLastPlayItem(String category, String item,
			int progress) {
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
		newDetail.progress = progress;
		newEntity.setDetail(newDetail);
		CategoryDetailDAO.getInstance(this).save(newEntity);
	}

	private void doAfterPlayAudio() {
		setCategoryLastPlayItem(currentPlayingCategory.category,
				currentPlayingAudio.item, 0);
		setLastAudioItem(currentPlayingAudio.item);
		writeLastCategory(currentPlayingCategory);
		HeardAllItemEntity entity= new HeardAllItemEntity();
		entity.setDetail(currentPlayingAudio.item);
		HeardAllItemDAO.getInstance(this).save(entity);
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
				playAudio(currentPlayingCategory.item.get(0), 0);
				isAutoPlayed = true;
			} else {
				playAudio(out, getLastAudioProgress());
				isAutoPlayed = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void autoPlayAudio() {
		try {

			if (!mIsPlaying) {
				String lastaudio = getLastAudioItem();
				AudioCategory lastcategory = readLastCategory();
				int lastprogress=getLastAudioProgress();
				if ((lastprogress>=0)&&(lastaudio != null) && (lastcategory != null)) {
					if (myCategories != null) {
						for (int k = 0; k < myCategories.size(); k++) {
							if (myCategories.get(k).category
									.equals(lastcategory.category)) {
								if (myCategories.get(k).item.size() > 0) {
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
						if (myCategories.get(i).order == 1) // 最近听的歌
						{
							if (myCategories.get(i).item != null) {
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
									} else {
										autoStartPlayer(
												myCategories.get(i).category,
												myCategories.get(i).item.get(0).item);
										return;
									}

								}
							}
						}
						if (myCategories.get(i).order == -2) // 运营歌单
						{
							if (myCategories.get(i).item.size() > 0) {
								autoStartPlayer(myCategories.get(i).category,
										myCategories.get(i).item.get(0).item);
								return;
							}
						}
					}
					if ((myCategories.size() > 0)
							&& (myCategories.get(0).item != null)) {
						autoStartPlayer(myCategories.get(0).category,
								myCategories.get(0).item.get(0).item);
						return;
					}

				}
//			} else {
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void showButtonNotify(AudioItem item, boolean isPlay, boolean isLove) {
		Intent intent = new Intent(NEXT_AUDIO);
		Bundle mBundle = new Bundle();
		mBundle.putParcelable("audio", item);
		intent.putExtras(mBundle);
		AudioPlayerService.this.sendBroadcast(intent);

		try {
			int size = 0;
			if(listAudio!=null){
				for(AudioSubscription cate : listAudio){
					if(isSubscription(cate.album)){
						size ++;
					}
				}
			}
			if(size>=Contents.ADD_BIG_NUM){
				Notify.showButtonNotify(getPlayingCatogory(), getPlayingAudio(),AudioPlayerService.this, isPlay, isLove,6);
			}else{
				Notify.showButtonNotify(getPlayingCatogory(), getPlayingAudio(),AudioPlayerService.this, isPlay, isLove,0);
			}
			 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		tellLauncher(item.name);
	}
	
	
	private void tellLauncher(String text){
		Intent it = new Intent(CommonApps.SLEEP_SHOW_CONTENT);
		it.putExtra(CommonApps.SLEEP_CONTENT_TITLE, text);
		AudioPlayerService.this.sendBroadcast(it);
	}
	
	private void tellLauncherState(boolean isPlay){
		Intent it = new Intent(CommonApps.SLEEP_BEEN_PLAY_MUSIC);
		it.putExtra(CommonApps.SLEEP_PLAY_MUSIC_NEED_PLAY, isPlay);
		AudioPlayerService.this.sendBroadcast(it);
	}

	void playTtsStart(String text) {
		Intent intent = new Intent(CommonMessage.TTS_PLAY);
		intent.putExtra("content", text);
		intent.putExtra("notify", false);
		this.sendBroadcast(intent);
	}

	void sendAudioStateBroadcast(String state) {
		if (currentPlayingCategory != null) {
			Intent intent = new Intent(TSDEvent.Podcast.STATE);
			AudioState audioState = new AudioState();
			audioState.id = currentPlayingCategory.category;
			audioState.name = currentPlayingCategory.name;
			audioState.type = currentPlayingCategory.type;
			audioState.state = state;
			intent.putExtra("content", audioState);
			this.sendBroadcast(intent);
			LogUtil.i("AudioPlayer", "sendAudioState=" + state);
		}

	}

	@Override
	public boolean isHeard(String item) {
		
		return HeardAllItemDAO.getInstance(this).isHaveRecord(item);
	}
	
	@Override
	public void setList(List<AudioSubscription> listAudio){
		this.listAudio = listAudio;
	}

	@Override
	public List<AudioCategory> getCategoryListPush() {
		return myCategories;
	}

}

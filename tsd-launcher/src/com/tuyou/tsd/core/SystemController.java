package com.tuyou.tsd.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.tuyou.tsd.R;
import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDComponent;
import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.TSDLocation;
import com.tuyou.tsd.common.TSDShare;
import com.tuyou.tsd.common.network.GetWeatherRes;
import com.tuyou.tsd.common.network.JsonOA2;
import com.tuyou.tsd.common.network.LoginReq;
import com.tuyou.tsd.common.network.LoginRes;
import com.tuyou.tsd.common.network.SubmitAccidentInfoReq;
import com.tuyou.tsd.common.network.SubmitDeviceStatusReq;
import com.tuyou.tsd.common.network.SubmitDeviceStatusReq.Location;
import com.tuyou.tsd.common.network.SubmitDeviceStatusRes;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.common.util.TsdHelper;
import com.tuyou.tsd.core.CoreService.ContentType;
import com.tuyou.tsd.core.CoreService.ServiceState;
import com.tuyou.tsd.core.CoreService.WorkingMode;
import com.tuyou.tsd.core.im.MessageService;

public class SystemController {
	private static final String LOG_TAG = "CoreService.SystemController";

	// 定义必须安装的组件
	private final String[] EXP_PACKAGE_LIST = {
			TSDComponent.VOICE_ASSISTANT_PACKAGE,
			TSDComponent.NAVIGATOR_PACKAGE,
			TSDComponent.SETTINGS_PACKAGE,
			TSDComponent.AUDIO_PACKAGE,
			TSDComponent.CAR_DVR_PACKAGE,
			TSDComponent.UPDATE_PACKAGE
			};

	// Interaction template
	private static final String TEMPLATE_NAME = "template";
	private static final String TEMPLATE_WAKEUP = "GENERIC";
	private static final String TEMPLATE_DEST_QUERY = "DEST_QUERY";
	// TTS intent parameters
	private static final String PARAM_PACKAGE = "package";
	private static final String PARAM_ID = "id";
	private static final String PARAM_CONTENT = "content";
	private static final String PARAM_NOTIFY  = "notify";

	// TTS playing sequence id
//	private static final int TTS_GREETING = 1;
	private static final int TTS_GOODBYE  = 2;

	private static CoreService mService;
	private MessageService mMessageClient;

	private boolean mNetworkEnabled;	// true -- Network enabled; false -- Network disabled
	private boolean mStorageMounted;	// true -- SDCard mounted; false -- SDCard unmounted
	private boolean mIsLoggedIn;		// true -- Logged in successfully; false -- Not log in yet
	private boolean mPendingLogin;

	private String[] mMissingPackages;

	private TSDLocation mCurrentLocation;
	private WakeLock mWakeLock = null;

    // Thread synchronization locks
//	private static Object mLock = new Object();

	private GetWeatherRes mCachedWeatherData;

    private IdleCheckThread mIdleThread; // 空闲检查
    private static int mCurrentScreenBrightness = 0;
    
    private double mLat;
	private double mLng;
	private String  mAddress;
	private String mDistrict;
	private double mileage;

	private Timer mTimer = null;

	public SystemController(CoreService context) {
		mService = context;
		mTimer = new Timer(true);
	    registerBroadcastReceiver();

		LogUtil.d(LOG_TAG, "create the instance of Message service.");
		mMessageClient = MessageService.getInstance(mService);
		// 启动messageClient在登录成功后进行，因为需要deviceId
	}

	public void start() {
		startSystemServices();
	}

	public void stop() {
		mTimer.cancel();
		unregisterBroadcastReceiver();
	}

	/**
	 * 硬按键广播消息监听
	 */
	private final BroadcastReceiver mSystemEventsReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.v(LOG_TAG, "received the broadcast: " + action);

			// HardKey pressed
			if (action.equals(TSDEvent.System.HARDKEY1_PRESSED)) {
				mService.changeMode(WorkingMode.MODE_TAKE_PICTURE);
			}
			else if (action.equals(TSDEvent.System.HARDKEY2_PRESSED)) {
				
			}
			else if (action.equals(TSDEvent.System.HARDKEY3_PRESSED)) {
				
			}
			else if (action.equals(TSDEvent.System.HARDKEY4_PRESSED)) {
				onWakeUp();
			}
			// Network connection
			else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				mNetworkEnabled = checkNetworkEnabled();
				LogUtil.i(LOG_TAG, "Detected the network connection changed, now is: " + mNetworkEnabled);
				if (mNetworkEnabled) {
					onNetworkConnected();
				} else {
					onNetworkDisconnected();
				}
			}
			// External storage
			else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
				mStorageMounted = true;
				LogUtil.i(LOG_TAG, "External storage MOUNTED.");
			}
			else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
				mStorageMounted = false;
				LogUtil.i(LOG_TAG, "External storage UNMOUNTED.");
			}
			// Enable/Disable go to sleeping
			else if (action.equals(TSDEvent.System.ENABLE_IDLE_CHECK)) {
				resumeIdleCheckThread();
			}
			else if (action.equals(TSDEvent.System.DISABLE_IDLE_CHECK)) {
				stopIdleCheckThread();
			}
		}
	};

	/**
	 * 业务广播消息监听
	 */
	private final BroadcastReceiver mBusinessEventsReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
//			LogUtil.v(LOG_TAG, "received the broadcast: " + action);

			// Video playing
			if (action.equals(TSDEvent.CarDVR.APP_STARTED)) {
				mService.changeMode(WorkingMode.MODE_VIDEO_PLAYING);
			}
			else if (action.equals(TSDEvent.CarDVR.APP_STOPPED)) {
				mService.changeMode(WorkingMode.MODE_STANDBY);
			}

			// Query weather
			else if (action.equals(TSDEvent.System.QUERY_WEATHER)) {
				queryWeather();
			}
			else if (action.equals(TSDEvent.System.WEATHER_UPDATED)) {
				mCachedWeatherData = intent.getParcelableExtra("data");
			}
			// Location
			else if (action.equals(TSDEvent.System.LOCATION_UPDATED)) {
				TSDLocation location = intent.getParcelableExtra("location");
				onLocationUpdated(location);
				mLat = location.getLatitude();
    			mLng = location.getLongitude();
    			mAddress=location.getAddrStr();
    			mDistrict=location.getDistrict();
    			mileage = location.getMileage();
			}
			//set sleep time
			else if (action.equals(TSDEvent.System.IDLE_INTERVAL_TIME_UPDATED)) {
				stopIdleCheckThread();
				resumeIdleCheckThread();
			}
			// Send feed back to server
			else if (action.equals(TSDEvent.Push.FEED_BACK)) {
				String msg = intent.getStringExtra("message");
				replyFeedBackToServer(msg);
			}else if(TSDEvent.System.LOCATION_UPDATED.equals(action)){
				TSDLocation location = intent.getParcelableExtra("location");
    			mLat = location.getLatitude();
    			mLng = location.getLongitude();
    			mAddress=location.getAddrStr();
    			mDistrict=location.getDistrict();
    			mileage = location.getMileage();
			}
		}
	};

	/**
	 * 交互广播消息监听
	 */
	private final BroadcastReceiver mIntactEventsReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.v(LOG_TAG, "received the broadcast: " + action);
			
			// Wake up
			if (action.equals(CommonMessage.VOICE_COMM_WAKEUP)) {
				Log.v(LOG_TAG,"VOICE_COMM_WAKEUP");
				onWakeUp();
			}
			else if (action.equals(CommonMessage.VOICE_COMM_TAKE_PICTURE)) {
				onTakePictureMode();
			}
			else if (action.equals(CommonMessage.VOICE_COMM_SHUT_UP)) {
				onStandbyMode();
			}
			else if (action.equals(TSDEvent.Interaction.INTERACTION_START)) {
				onInteractionStart();
			}
			else if (action.equals(TSDEvent.Interaction.INTERACTION_FINISH)) {
				onInteractionFinish(intent.getStringExtra("template"),
						intent.getStringExtra("answerType"),
						intent.getStringExtra("answer"),
						intent.getStringExtra("extra"));
			}
			else if (action.equals(TSDEvent.Interaction.INTERACTION_ERROR)) {
				onInteractionError(intent.getStringExtra("template"),
						intent.getStringExtra("reason"),
						intent.getStringExtra("description"));
			}
			// TTS play finished
			else if (action.equals(CommonMessage.TTS_PLAY_FINISHED)) {
				String pkg = intent.getStringExtra(PARAM_PACKAGE);
				if (pkg != null && pkg.equals(TSDComponent.CORE_SERVICE_PACKAGE)) {
					switch (intent.getIntExtra(PARAM_ID, 0)) {
					case TTS_GOODBYE:
						LogUtil.v(LOG_TAG, "熄火问候已结束.");
//						synchronized (mLock) { mLock.notify(); }
						break;
					default:
						// Default has nothing to do
					}
				}
			}
			// Picture has been taken
			else if (action.equals(TSDEvent.CarDVR.PICTURE_TAKEN_COMPLETED)) {
				onPictureHasBeenTaken();
			}
		}
	};

	public void onSystemLoading() {
		LogUtil.v(LOG_TAG, "onSystemLoading()...");

		String[] missingPackages = checkComponentsIntegrated();
		if (missingPackages != null && missingPackages.length > 0) {
			mMissingPackages = missingPackages;
			mService.changeState(ServiceState.STATE_ERROR);
		} else {
			// check the network status and sdcard mounted.
			mNetworkEnabled = checkNetworkEnabled();

			new Thread(new Runnable() {
				@Override
				public void run() {
					//
					// 语音助手、行车记录等服务初始化时需要从SD卡上读取资源文件，所以需要等待SD卡加载成功后再继续启动各个服务
					//
					long start = System.currentTimeMillis();
					while (checkStorageIsReady() == false) {
						try { Thread.sleep(200); } catch (InterruptedException e) {}
					}
					mStorageMounted = true;
					LogUtil.d(LOG_TAG, "Waited for SD card ready, total waited: " + (System.currentTimeMillis() - start) + "ms");
					//
					// 发送通知以继续进行初始化
					//
					Message msg = Message.obtain(null, CoreService.EXTERNAL_STORAGE_READY);
					try {
						mService.mMessenger.send(msg);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}

	public void onSystemInit() {
		// Start InitSettings application
		HelperUtil.startActivityWithFadeInAnim(mService, TSDComponent.INIT_SETTINGS_PACKAGE, TSDComponent.INIT_SETTINGS_MAIN_ACTIVITY);
	}

	public void onSystemStarted() {
		LogUtil.v(LOG_TAG, "onSystemStart()...");

		// ACC ON时若有网络则登录，否则待网络恢复正常时再执行登录
		if (mNetworkEnabled && !mIsLoggedIn) {
			new LoginTask().execute();
		} else {
			mPendingLogin = true;
		}

		startBusinessServices();
	}

	@SuppressWarnings("unused")
	public void onSystemResumed() {
		LogUtil.v(LOG_TAG, "onSystemResume()...");

		// 设置系统不进入休眠
		LogUtil.v(LOG_TAG, "create PARTIAL_WAKE_LOCK...");
		PowerManager pm = (PowerManager) mService.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "WakeLock");
		mWakeLock.acquire();

		turnOnScreen();
		turnOnKeyLight();

		// 开始录像
		recordVideo(true);

		uploadDeviceState();

		// Display the upgrade info since last time
		mService.sendBroadcast(new Intent(TSDEvent.Update.DISPLAY_INFO));
		// Check upgrade and start download if available
		mService.sendBroadcast(new Intent(TSDEvent.Update.START_DOWNLOAD));

		LogUtil.d(LOG_TAG, "Display upgrade info.");
	   	LogUtil.d(LOG_TAG, "Start to download upgrade packages.");
    	LogUtil.d(LOG_TAG, "CoreService resumed, now is running....");

    	// 点火后跳转到主页面
    	HelperUtil.startActivityWithFadeInAnim(mService, TSDComponent.LAUNCHER_PACKAGE, TSDComponent.HOME_ACTIVITY);

    	// 启动后首先进入standby状态
    	mService.changeMode(WorkingMode.MODE_STANDBY);
	}

	public void onSystemSuspended() {
    	// First stop the core thread
    	stopIdleCheckThread();

    	new Thread(new Runnable() {
			@SuppressWarnings("unused")
			public void run() {
				LogUtil.v(LOG_TAG, "onSystemSuspend()...");

				// Stop the current task, prepare to suspend
				mService.changeMode(WorkingMode.MODE_SLEEP);

				// 停止录像
				recordVideo(false);

				// 熄火问候
				TtsMessage ttsmsg = new TtsMessage();
				ttsmsg.callbackNotify = true;
				ttsmsg.needToNotify = true;
				ttsmsg.content = mService.getResources().getString(R.string.GoodbyeMessage);
				ttsmsg.id = TTS_GOODBYE;
				playTtsText(ttsmsg, null/*new Intent(CommonMessage.EVT_GOODBYE_BEGIN)*/);

//				try {
//					synchronized (mLock) {
//						mLock.wait();
//					}
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}

				if (mWakeLock != null) {
					LogUtil.v(LOG_TAG, "release PARTIAL_WAKE_LOCK...");
					mWakeLock.release();
					mWakeLock = null;
				}

				turnOffScreen();
				turnOffKeyLight();

				uploadDeviceState();

				// Install the new version apps if available
				mService.sendBroadcast(new Intent(TSDEvent.Update.START_INSTALL));

				LogUtil.d(LOG_TAG, "Install upgrade packages if they are ready.");
				LogUtil.d(LOG_TAG, "CoreService suspended.");
			}
		}).start();

    	// 熄火后跳转到睡觉页面
    	HelperUtil.startActivityWithFadeInAnim(mService, TSDComponent.LAUNCHER_PACKAGE, TSDComponent.SLEEPING_ACTIVITY);
	}

	public void onSystemStopped() {
		LogUtil.v(LOG_TAG, "onSystemStop()...");

		uploadDeviceState();

		// Turn off GRPS
		if (HelperUtil.getMobileDataEnabled(mService))
			HelperUtil.setMobileDataEnabled(mService, false);

		stopBusinessServices();
		stopSystemServices();

		LogUtil.d(LOG_TAG, "CoreService stopped.");

		// TODO: Put the system goto sleep
	}

	public void onSystemError() {
		Bundle bundle = new Bundle();
		bundle.putStringArray("missingComponents", mMissingPackages);
		HelperUtil.startActivity(mService, TSDComponent.LAUNCHER_PACKAGE,
				"com.tuyou.tsd.launcher.ErrorActivity", bundle);
	}

	private void onNetworkConnected() {
		LogUtil.v(LOG_TAG, "onNetworkConnected");
		// 若ACC ON时由于没有网络未进行登录，则在网络恢复时重新进行登录
		if (mPendingLogin && !mIsLoggedIn) {
			new LoginTask().execute();
			mPendingLogin = false;
		}
	}

	private void onNetworkDisconnected() {
		LogUtil.v(LOG_TAG, "onNetworkDisconnected");
	}

	private void onWakeUp() {
		if ((mService.mCurrentState == ServiceState.STATE_RESUME ||
				 mService.mCurrentState == ServiceState.STATE_START) &&
				 mService.mCurrentMode != WorkingMode.MODE_INTERACTING)
		{
			LogUtil.d(LOG_TAG, "onWakeUp, prepare to start an interaction.");
			turnOnScreen();

			// 唤醒时若有播报内容则先清除（唤醒优先于播报）
			mService.sendBroadcast(new Intent(CommonMessage.TTS_CLEAR));
			try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }

			Intent intent = new Intent(TSDEvent.Interaction.RUN_INTERACTION);
			intent.putExtra(TEMPLATE_NAME, TEMPLATE_WAKEUP);
			mService.sendBroadcast(intent);
		} else {
			LogUtil.w(LOG_TAG, "onWakeUp, ignore wake up message. current state=" + mService.mCurrentState +
					", current mode=" + mService.mCurrentMode);
		}
	}

	private void onInteractionStart() {
		if ((mService.mCurrentState == ServiceState.STATE_RESUME ||
			 mService.mCurrentState == ServiceState.STATE_START) &&
			 mService.mCurrentMode != WorkingMode.MODE_INTERACTING)
		{
			// 唤醒后转换到交互状态
			mService.changeMode(WorkingMode.MODE_INTERACTING);
		}
	}

	private void onInteractionFinish(String template, String answerType, String answer, String extra) {
		Log.d(LOG_TAG, "onInteractionFinish, template=" + template + ", answerType=" + answerType + ", answer=" + answer + ", extra="+extra);
		if (canResponseToVoiceCommand()) {
			if (template.equals(TEMPLATE_WAKEUP) ||
				template.equals(TEMPLATE_DEST_QUERY)) {
				doActionAfterWakeUpInteraction(answerType, answer, extra);
			}
		}
		mService.changeMode(WorkingMode.MODE_STANDBY);
	}

	private void onInteractionError(String template, String reason, String description) {
		Log.d(LOG_TAG, "onInteractionError, template=" + template + ", reason=" + reason + ", description=" + description);
		if (template.equals(TEMPLATE_WAKEUP) ||
			template.equals(TEMPLATE_DEST_QUERY)) {
			mService.sendBroadcast(new Intent(TSDEvent.Interaction.FINISH_ACTIVITY));
		}

		// 交互失败、取消时除之前为idle状态外，均返回至原先的状态
		if (mService.mPrevMode == WorkingMode.MODE_IDLE ||
			reason.equals("ERR_USER_CANCELLED_AND_GO_HOME")) {
			mService.changeMode(WorkingMode.MODE_STANDBY);
		} else {
			mService.changeMode(mService.mPrevMode, mService.mPrevType);
		}
	}

	public void onStandbyMode() {
		LogUtil.v(LOG_TAG, "Start to standby...");

		turnOnScreen();
		// 进入待机状态后，重置空闲状态检查
		stopIdleCheckThread();
		resumeIdleCheckThread();
	}

	public void onIdleMode() {
		stopIdleCheckThread();
		HelperUtil.startActivityWithFadeInAnim(mService, TSDComponent.LAUNCHER_PACKAGE, TSDComponent.SLEEPING_ACTIVITY);
	}

	public void onInteractingMode(ContentType param) {
		LogUtil.v(LOG_TAG, "Start to interact...");

		stopIdleCheckThread();

		HelperUtil.startActivityWithFadeInAnim(mService,
				TSDComponent.VOICE_ASSISTANT_PACKAGE,
				TSDComponent.INTERACTION_ACTIVITY);

//		uploadDeviceState();
	}

	public void onNavigatingMode(ContentType type) {
		stopIdleCheckThread();

		if (type == ContentType.TYPE_MAP) {
//			HelperUtil.startActivityWithFadeInAnim(mService,
//					TSDComponent.NAVIGATOR_PACKAGE,
//					TSDComponent.NAVIGATOR_MAIN_ACTIVITY);
			//启动导航改为发送广播由导航自己启动
			mService.sendBroadcast(new Intent("com.tuyou.tsd.start_nav"));
		}

//		uploadDeviceState();
	}

	public void onAudioMode(ContentType type) {
		// 在音乐模式中允许小宝进入休眠，故重置空闲检查时间
		stopIdleCheckThread();
		resumeIdleCheckThread();

		switch (type) {
		case TYPE_MUSIC:
			HelperUtil.startActivityWithFadeInAnim(mService, TSDComponent.AUDIO_PACKAGE, TSDComponent.AUDIO_MAIN_ACTIVITY);
			break;
		case TYPE_NEWS:
			HelperUtil.startActivityWithFadeInAnim(mService, TSDComponent.NEWS_PACKAGE, TSDComponent.NEWS_MAIN_ACTIVITY);
			break;
		case TYPE_JOKE:
			HelperUtil.startActivityWithFadeInAnim(mService, TSDComponent.PODCAST_PACKAGE, TSDComponent.PODCAST_MAIN_ACTIVITY);
			break;
		default:
		}

//		uploadDeviceState();
	}

	public void onVideoPlayingMode() {
		stopIdleCheckThread();
	}

	public void onTakePictureMode() {
		LogUtil.d(LOG_TAG, "Start to take a picture...");
		mService.sendBroadcast(new Intent(TSDEvent.CarDVR.TAKE_PICTURE));
	}

	private void onPictureHasBeenTaken() {
	    LogUtil.d(LOG_TAG, "onPictureHasBeenTaken");
	    Toast.makeText(mService, "拍照完毕.", Toast.LENGTH_SHORT).show();

        MediaPlayer player = MediaPlayer.create(mService, R.raw.photo);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                mp = null;
            }
        });
	    player.start();

		// 拍照完毕后切换回standby模式
		mService.changeMode(WorkingMode.MODE_STANDBY, ContentType.TYPE_NONE);
	}

	public void onAccident() {
		// TODO Auto-generated method stub
		
	}

	public void onAlert() {
		// TODO Auto-generated method stub
		
	}

	public void onSleep() {
		// TODO Auto-generated method stub
		
	}

	public void onUpdate() {
		// TODO Auto-generated method stub
		
	}

    public void onShake() {
		Intent intent = new Intent();
		if (TsdHelper.getAccStatus()) {
			// Accident occurs
			intent.setAction(TSDEvent.CarDVR.ACCIDENT_TRIGGERED);
			if (mCurrentLocation != null) {
    			SubmitAccidentInfoReq param = new SubmitAccidentInfoReq();
    			param.timestamp = HelperUtil.getCurrentTimestamp();
    			param.latitude = String.valueOf(mCurrentLocation.getLatitude());
    			param.longitude = String.valueOf(mCurrentLocation.getLongitude());
    			param.district = mCurrentLocation.getDistrict();
    			param.address  = mCurrentLocation.getAddrStr();
    			intent.putExtra("data", param);
			}
			// test
			Toast.makeText(mService, "事故触发", Toast.LENGTH_SHORT).show();
		} else {
			// Alert occurs
			intent.setAction(TSDEvent.CarDVR.ALERT_TRIGGERED);
			if (mCurrentLocation != null) {
				intent.putExtra(TSDConst.UPLOAD_LOC_LAT, String.valueOf(mCurrentLocation.getLatitude()));
				intent.putExtra(TSDConst.UPLOAD_LOC_LNG, String.valueOf(mCurrentLocation.getLongitude()));
				intent.putExtra(TSDConst.UPLOAD_TIME_STAMP, HelperUtil.getCurrentTimestamp());
			}
			// test
			Toast.makeText(mService, "停车报警触发", Toast.LENGTH_SHORT).show();
		}
		LogUtil.d(LOG_TAG, "Send broadcast " + intent);
		mService.sendBroadcast(intent);
    }

    public void onNavigatingStart() {
    }

    public void onNavigatingFinish() {
    	// 导航结束后切换回standby模式
		mService.changeMode(WorkingMode.MODE_STANDBY, ContentType.TYPE_NONE);
    }

    public void onVideoPlayingStart() {
    }

    public void onVideoPlayingFinish() {
    	// 视频结束后切换回standby模式
		mService.changeMode(WorkingMode.MODE_STANDBY, ContentType.TYPE_NONE);
    }

    public void onAudioPlayingStart() {
    }

    public void onAudioPlayingFinish() {
    	// 音乐结束后切换回standby模式
		mService.changeMode(WorkingMode.MODE_STANDBY, ContentType.TYPE_NONE);
    }

    private String[] checkComponentsIntegrated() {
    	List<PackageInfo> installedPackages = mService.getPackageManager().getInstalledPackages(PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES);
    	List<String> missingPackages = new ArrayList<String>();

    	for (int i = 0; i < EXP_PACKAGE_LIST.length; i++) {
        	boolean result = false;
    		for (PackageInfo info : installedPackages) {
        		if (info.packageName.equals(EXP_PACKAGE_LIST[i])) {
        			result = true;
        			break;
        		}
    		}
    		if (!result) {
    			missingPackages.add(EXP_PACKAGE_LIST[i]);
    		}
    	}
    	return missingPackages.toArray(new String[missingPackages.size()]);
    }

    /**
	 * Check the network connection and enable it if necessary
	 */
	@SuppressWarnings("unused")
	private boolean checkNetworkEnabled() {
		ConnectivityManager cm = (ConnectivityManager) mService.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNet = cm.getActiveNetworkInfo();
		if (activeNet != null) {
			LogUtil.d(LOG_TAG, "Active network: " + activeNet.getTypeName() + ", " + activeNet.getState());
			return true;
		} else {
			LogUtil.w(LOG_TAG, "No active network found...");
			if (TSDConst.useGPRSNetwork && !HelperUtil.getMobileDataEnabled(mService)) {
				LogUtil.d(LOG_TAG, "Trying to enable GPRS...");
				HelperUtil.setMobileDataEnabled(mService, true);
			}
			return false;
		}

	}

	private boolean checkStorageIsReady() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? true : false;
	}

	private void registerBroadcastReceiver() {
		registerSystemEventsReceiver();
		registerIntactEventsReceiver();
		registerBusinessEventsReceiver();
	}

	private void registerSystemEventsReceiver() {
		IntentFilter filter = new IntentFilter();
		// Hard key
		filter.addAction(TSDEvent.System.HARDKEY1_PRESSED);
		filter.addAction(TSDEvent.System.HARDKEY2_PRESSED);
		filter.addAction(TSDEvent.System.HARDKEY3_PRESSED);
		filter.addAction(TSDEvent.System.HARDKEY4_PRESSED);
		// Network
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		// External storage
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		// enable/disable go to sleeping
		filter.addAction(TSDEvent.System.ENABLE_IDLE_CHECK);
		filter.addAction(TSDEvent.System.DISABLE_IDLE_CHECK);

		mService.registerReceiver(mSystemEventsReceiver, filter);
	}

	private void registerIntactEventsReceiver() {
		IntentFilter filter = new IntentFilter();
		// 语音指令
		filter.addAction(CommonMessage.VOICE_COMM_WAKEUP);		// 语音唤醒
		filter.addAction(CommonMessage.VOICE_COMM_TAKE_PICTURE);	// 拍照
		filter.addAction(CommonMessage.VOICE_COMM_SHUT_UP);		// 闭嘴
		filter.addAction(CommonMessage.VOICE_COMM_POSITIVE);	// 肯定回答
		filter.addAction(CommonMessage.VOICE_COMM_NEGATIVE);	// 否定回答
		filter.addAction(CommonMessage.VOICE_COMM_MUSIC);		// 听音乐
		filter.addAction(CommonMessage.VOICE_COMM_MAP);			// 地图导航
		filter.addAction(CommonMessage.VOICE_COMM_NEWS);		// 听新闻
		filter.addAction(CommonMessage.VOICE_COMM_JOKE);
		// 交互结果
		filter.addAction(TSDEvent.Interaction.INTERACTION_START);
		filter.addAction(TSDEvent.Interaction.INTERACTION_FINISH);
		filter.addAction(TSDEvent.Interaction.INTERACTION_ERROR);

		filter.addAction(CommonMessage.TTS_PLAY_FINISHED);
		filter.addAction(TSDEvent.CarDVR.PICTURE_TAKEN_COMPLETED);

		mService.registerReceiver(mIntactEventsReceiver, filter);
	}

	private void registerBusinessEventsReceiver() {
		IntentFilter filter = new IntentFilter();
		// 业务消息
		filter.addAction(TSDEvent.System.QUERY_WEATHER);
		filter.addAction(TSDEvent.System.WEATHER_UPDATED);
		filter.addAction(TSDEvent.System.LOCATION_UPDATED);
		filter.addAction(TSDEvent.CarDVR.APP_STARTED);
		filter.addAction(TSDEvent.CarDVR.APP_STOPPED);
		filter.addAction(TSDEvent.System.IDLE_INTERVAL_TIME_UPDATED);
		filter.addAction(TSDEvent.Push.FEED_BACK);
		
		mService.registerReceiver(mBusinessEventsReceiver, filter);
	}

	private void unregisterBroadcastReceiver() {
		mService.unregisterReceiver(mSystemEventsReceiver);
		mService.unregisterReceiver(mIntactEventsReceiver);
		mService.unregisterReceiver(mBusinessEventsReceiver);
	}

	/**
	 * 启动系统相关服务
	 */
	private void startSystemServices() {
		// Voice assistant
		mService.startService(new Intent(TSDComponent.VOICE_ASSISTANT_SERVICE));
		LogUtil.v(LOG_TAG, "Start VoiceAssistant service.");
	
		// Car DVR
		mService.startService(new Intent(TSDComponent.CAR_DVR_SERVICE));
		LogUtil.v(LOG_TAG, "Start CarDVR service.");
	
		// Update
		mService.startService(new Intent(TSDComponent.UPDATE_SERVICE));
		LogUtil.v(LOG_TAG, "Start Upgrade service.");
	
		// Audio
		mService.startService(new Intent(TSDComponent.AUDIO_SERVICE));
		LogUtil.v(LOG_TAG, "Start Audio service.");	
		
		// navigation
		mService.startService(new Intent(TSDComponent.NAVIGATOR_SERVICE));
		LogUtil.v(LOG_TAG, "Start navigation service.");	
		
		// settings
		mService.startService(new Intent(TSDComponent.SETTINGS_SERVICE));
		LogUtil.v(LOG_TAG, "Start settings service.");
	}

	/**
	 * 停止系统相关服务
	 */
	private void stopSystemServices() {
		// Stop voice assistant
		mService.stopService(new Intent(TSDComponent.VOICE_ASSISTANT_SERVICE));
		LogUtil.v(LOG_TAG, "Stop VoiceAssistant service.");
	
		// Stop car DVR
		mService.stopService(new Intent(TSDComponent.CAR_DVR_SERVICE));
		LogUtil.v(LOG_TAG, "Stop CarDVR service.");
	
		// Audio
		mService.stopService(new Intent(TSDComponent.AUDIO_SERVICE));
		mService.stopService(new Intent(TSDComponent.PODCAST_SERVICE));
		mService.stopService(new Intent(TSDComponent.NEWS_SERVICE));
		LogUtil.v(LOG_TAG, "Stop Audio service.");
	
		// Stop push listening.
		mMessageClient.stopWork();
		LogUtil.d(LOG_TAG, "Stop Message service.");		
	}

	/**
	 * 启动业务相关服务
	 */
	private void startBusinessServices() {
		// Start the Shake Monitoring service
		mService.startService(new Intent(mService, ShakeMonitoringService.class)
								.putExtra("callback", mService.mMessenger));
		// Start the HTTPD service
	    mService.startService(new Intent(mService, HttpdService.class));

	    // Periodical check the ACC state
	    if (TSDConst.buildForDevice) {
	    	mService.startService(new Intent(mService, AccCheckingService.class));
	    }
	}

	/**
	 * 停止业务相关服务
	 */
	private void stopBusinessServices() {
		mService.stopService(new Intent(mService, ShakeMonitoringService.class));

		if (TSDConst.buildForDevice) {
			mService.stopService(new Intent(mService, AccCheckingService.class));
		}
		
		mService.stopService(new Intent(mService, HttpdService.class));
	}

	/**
	 * 点亮屏幕
	 */
	private void turnOnScreen() {
		if (mCurrentScreenBrightness == 0) {
			mCurrentScreenBrightness = 100;
			HelperUtil.setScreenBrightness(mService, mCurrentScreenBrightness);
		}
	}

	private void turnOnKeyLight() {
		for (int i = 1; i <= 4; i++) {
			TsdHelper.setkeyLedOn(i);
		}
	}

	/**
	 * 关闭屏幕
	 */
	private void turnOffScreen() {
		if (mCurrentScreenBrightness > 0) {
			mCurrentScreenBrightness = 0;
			HelperUtil.setScreenBrightness(mService, mCurrentScreenBrightness);
		}
	}

	private void turnOffKeyLight() {
		for (int i = 1; i <= 4; i++) {
			TsdHelper.setkeyLedOff(i);
		}
	}

	private void recordVideo(boolean start) {
		Intent i = new Intent();
		if (start) {
			LogUtil.v(LOG_TAG, "开始录像...");
			i.setAction(TSDEvent.CarDVR.START_REC);
		} else {
			LogUtil.v(LOG_TAG, "停止录像...");
			i.setAction(TSDEvent.CarDVR.STOP_REC);
		}
		LogUtil.d(LOG_TAG, "Send broadcast: " + i);
		mService.sendBroadcast(i);
	}

	private void uploadDeviceState() {
		SubmitDeviceStatusReq req = new SubmitDeviceStatusReq();
		req.state = mService.mCurrentState.value;
//		req.modes = new String[] { mCurrentMode.value };
		req.timestamp = HelperUtil.getCurrentTimestamp();
		if(req.location==null){
			Location location = new Location();
			location.address = mAddress;
			location.district = mDistrict;
			location.lat = mLat;
			location.lng = mLng;
			location.mileage = mileage;
			req.location = location;
		}else{
			req.location.address = mAddress;
			req.location.district = mDistrict;
			req.location.lat = mLat;
			req.location.lng = mLng;
			req.location.mileage = mileage;
		}
		
		new UpdateDeviceStateTask().execute(req);
	}

	private void playTtsText(TtsMessage ttsMsg, Intent notifyMsg) {
		Intent ttsIntent = new Intent(CommonMessage.TTS_PLAY);
		if (ttsMsg.callbackNotify) {
			// Add the call back identification
			ttsIntent.putExtra(PARAM_PACKAGE, TSDComponent.CORE_SERVICE_PACKAGE);
			ttsIntent.putExtra(PARAM_ID, ttsMsg.id);
		}
		ttsIntent.putExtra(PARAM_CONTENT, ttsMsg.content);
		ttsIntent.putExtra(PARAM_NOTIFY, ttsMsg.needToNotify);
		mService.sendBroadcast(ttsIntent);

		// Broadcast the notify message to listener, e.g. UI
		if (notifyMsg != null) {
			LogUtil.d(LOG_TAG, "Send the broadcast: " + notifyMsg);
			mService.sendBroadcast(notifyMsg);
		}
	}

	/**
	 * 恢复空闲检查线程
	 */
	private void resumeIdleCheckThread() {
		if (mIdleThread != null) {
			LogUtil.v(LOG_TAG, "resume IdleCheckThread");
			mIdleThread.resumeIfNecessary();
		} else {
			int idleTime = 300;
			SharedPreferences pref = HelperUtil.getCommonPreference(mService,
					TSDComponent.CORE_SERVICE_PACKAGE, TSDShare.SYSTEM_SETTING_PREFERENCES);
			if (pref != null) {
				idleTime = Integer.parseInt(pref.getString("screen_off_value", idleTime+""));
			}
			LogUtil.v(LOG_TAG, "create IdleCheckThread with idle_time: " + idleTime + "s.");
			mIdleThread = new IdleCheckThread();
			mIdleThread.setIdleTime(idleTime);
	    	new Thread(mIdleThread).start();
		}
	}

	/**
	 * 停止空闲检查线程
	 */
	private void stopIdleCheckThread() {
    	if (mIdleThread != null) {
    		LogUtil.v(LOG_TAG, "stopIdleCheckThread...");
    		// 若线程当前为wait()状态，则需先notify后恢复其执行，才能将其停止
    		// 否则会导致ANR错误
    		mIdleThread.resumeIfNecessary();
    		mIdleThread.stop = true;
    		mIdleThread = null;
    	}
	}

	/**
	 * 判断系统当前状态下是否需要对语音指令作出响应
	 * @return
	 */
	private boolean canResponseToVoiceCommand() {
		if ((mService.mCurrentState == ServiceState.STATE_RESUME ||
			 mService.mCurrentState == ServiceState.STATE_START) &&
			mService.mCurrentMode == WorkingMode.MODE_INTERACTING)
			return true;
		else
			return false;
	}

	private void doActionAfterWakeUpInteraction(String answerType, String answer, String extra) {
		if (answerType.equals("command")) {
			if (answer.equals(CommonMessage.VOICE_COMM_TAKE_PICTURE)) {
				mService.changeMode(WorkingMode.MODE_TAKE_PICTURE);
			}
			else if (answer.equals(CommonMessage.VOICE_COMM_MAP)) {
				mService.changeMode(WorkingMode.MODE_MAP, ContentType.TYPE_MAP);
			}
			else if (answer.equals(CommonMessage.VOICE_COMM_MUSIC)) {
				mService.changeMode(WorkingMode.MODE_AUDIO, ContentType.TYPE_MUSIC);
			}
			else if (answer.equals(CommonMessage.VOICE_COMM_NEWS)) {
				mService.changeMode(WorkingMode.MODE_AUDIO, ContentType.TYPE_NEWS);
			}
			else if (answer.equals(CommonMessage.VOICE_COMM_JOKE)) {
				mService.changeMode(WorkingMode.MODE_AUDIO, ContentType.TYPE_JOKE);
			}
		}
		else if (answerType.equals("#location")) {
			Log.d(LOG_TAG, "sendBroadcast to start navigation.");
			Intent navIntent = new Intent(TSDEvent.Navigation.START_NAVIGATION);
			navIntent.putExtra("destination", extra);
			mService.sendBroadcast(navIntent);

			mService.changeMode(WorkingMode.MODE_MAP, ContentType.TYPE_NAVIGATION);
		}
		else if (answerType.equals("#music")) {
			Log.d(LOG_TAG, "sendBroadcast to start audio player.");

			Intent audioIntent = new Intent(TSDEvent.Audio.PLAY);
			if (extra.contains("\"type\":\"music\"")) {
				audioIntent.putExtra("type", "music");
				Intent it=new Intent();  
				it.setClassName("com.tuyou.tsd.audio", "com.tuyou.tsd.audio.MusicActivity"); 
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
				it.putExtra("playlist", extra);
				mService.startActivity(it);
			} else if (extra.contains("\"type\":\"news\"")) {
				audioIntent.putExtra("type", "news");
				Intent it=new Intent();  
				it.setClassName("com.tuyou.tsd.news", "com.tuyou.tsd.news.MusicActivity"); 
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
				it.putExtra("playlist", extra);
				mService.startActivity(it);
			} else if (extra.contains("\"type\":\"podcast\"")) {
				audioIntent.putExtra("type", "podcast");
				Intent it=new Intent();  
				it.setClassName("com.tuyou.tsd.podcast", "com.tuyou.tsd.podcast.MusicActivity"); 
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
				it.putExtra("playlist", extra);
				mService.startActivity(it);
			}
			audioIntent.putExtra("playlist", extra);
			mService.sendBroadcast(audioIntent);

			mService.changeMode(WorkingMode.MODE_AUDIO);
		}

		mService.sendBroadcast(new Intent(TSDEvent.Interaction.FINISH_ACTIVITY));
	}

	private void queryWeather() {
		Log.d(LOG_TAG, "queryWeather");
		if (mCachedWeatherData != null) {
			// Since the data will be refreshed every hour, so it's OK just to return the cached data
			Intent notifyMsg = new Intent(TSDEvent.System.WEATHER_UPDATED);
			notifyMsg.putExtra("data", mCachedWeatherData);
			mService.sendBroadcast(notifyMsg);
		}
		else if (mCurrentLocation != null) {
			mService.startService(new Intent(mService, WeatherService.class)
					.putExtra("city", mCurrentLocation.getCity()));
		}
//		else {
//			LogUtil.w(LOG_TAG, "Ignore the weather querying request due to no location info.");
//			mService.startService(new Intent(mService, LocationService.class));
//		}
	}

	private void onLocationUpdated(TSDLocation location) {
		// 为防止登录后由于还没有完成定位导致没有天气数据，所以这里再请求一次
		if (mCurrentLocation == null) {
			mService.startService(new Intent(mService, WeatherService.class)
						.putExtra("city", location.getCity()));
		}
		mCurrentLocation = location;
	}

	private void replyFeedBackToServer(String message) {
		try {
			JSONObject obj = new JSONObject(message);
			String id = obj.getString("routeKey");
			if (mMessageClient != null) {
				mMessageClient.sendFeedBack(id, message);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
     * Login to server and get the access token
     */
    private class LoginTask extends AsyncTask<Void, Void, LoginRes> {

		@Override
		protected LoginRes doInBackground(Void... params) {
			JsonOA2 jsonOA2 = JsonOA2.getInstance(mService);
			LoginReq req = new LoginReq();
			req.imei = HelperUtil.getDeviceId(mService);
			req.password = "1234567890";
			LoginRes result = jsonOA2.login(req);

			// Upload WIFI AP name and password to server
			// Network access must be in background thread.
			SharedPreferences sp = HelperUtil.getCommonPreference(mService,
					TSDComponent.SETTINGS_PACKAGE, TSDShare.SYSTEM_SETTING_PREFERENCES);
			String ssid = sp.getString("ssid", "");
			String ssidPwd = sp.getString("ssid_psd", "");
			Log.d(LOG_TAG, "ssid=" + ssid + ", password=" + ssidPwd);
			if (!ssid.isEmpty() && !ssidPwd.isEmpty()) {
				jsonOA2.updateDeviceInfo(ssid, ssidPwd);
			}

			return result;
		}

		@Override
		protected void onPostExecute(LoginRes result) {
			if (result != null) {
				switch (result.errorCode) {
				case 0: // success
					// 首次登录成功时启动天气查询服务和推送消息接收服务
					if (!mIsLoggedIn) {
						// Query the weather data after login
						if (mCurrentLocation != null) {
							mService.startService(new Intent(mService, WeatherService.class)
									.putExtra("city", mCurrentLocation.getCity()));
						}

						// Connect to message server
						mMessageClient.startWork(TSDConst.SERVER_HOST,
									TSDConst.MQTT_PORT,
									TSDConst.CLIENT_TAG,
									(String) HelperUtil.readFromCommonPreference(mService, "device_id", "string"));
						LogUtil.d(LOG_TAG, "start the MESSAGE service.");
					}

					// Schedule the re-login task after the token has expired
					Log.d(LOG_TAG, "result.expires = " + result.expires);
					if (result.expires > 0) {
						long time = result.expires * 1000;
						Log.d(LOG_TAG, "Schedule next login task after " + time + " ms.");
						try {
							mTimer.schedule(new TimerTask() {
								@Override
								public void run() {
									LogUtil.d(LOG_TAG, "Token has expired, re-login.");
									new LoginTask().execute();
								}
							}, time);
						} catch (IllegalStateException e) {
							
						}
					}

					mIsLoggedIn = true;
					break;
				case -1: // no network connection
					mIsLoggedIn = false;
					LogUtil.w(LOG_TAG, "Login failed. (" + result.errorCode + ")");
					Toast.makeText(mService, R.string.NetworkDisconnected, Toast.LENGTH_SHORT).show();
					break;
				default:
					mIsLoggedIn = false;
					LogUtil.w(LOG_TAG, "Login failed. (" + result.errorCode + ")");
					Toast.makeText(mService, R.string.LoginFailedMsg, Toast.LENGTH_SHORT).show();
				}
			}
		}

    }

    /**
     * Upload the device state to server
     */
    private static class UpdateDeviceStateTask extends AsyncTask<SubmitDeviceStatusReq, Void, Void> {

		@Override
		protected Void doInBackground(SubmitDeviceStatusReq... params) {
			LogUtil.v(LOG_TAG, "上传设置状态...");
			SubmitDeviceStatusReq req = params[0];
			SubmitDeviceStatusRes resp = JsonOA2.getInstance(mService).submitDeviceStatus(req);
			LogUtil.v(LOG_TAG, "收到应答...");
			if (resp != null && resp.errorCode == 0 && resp.message != null) {
				LogUtil.d(LOG_TAG, "检测到SubmitDeviceStatusRes包含有交互模板信息，走Push消息接收流程进行处理...");
				// 当附带Push内容时，走Push消息接收处理流程
				// 2014-10-30 Alex
	        	try {
	        		JSONObject json = new JSONObject(resp.message);
	        		String pushMsg = json.getString("message");

	            	Intent intent = new Intent(TSDEvent.Push.MESSAGE_ARRIVED);
	            	intent.putExtra("message", pushMsg);

	            	mService.sendBroadcast(intent);
	        	} catch (Exception e) {
	        		e.printStackTrace();
	        	}
			}
			return null;
		}
    	
    }

	/**
     * 空闲状态检查线程
     */
    private static class IdleCheckThread implements Runnable {
    	private static final int SLEEP_TIME = 5 * 1000;
    	private static int IDLE_TIME = 30 * 1000;

    	private Object lock = new Object();
    	private boolean stop, paused;

    	private long idleTime = System.currentTimeMillis();

    	IdleCheckThread() {}

    	void resumeIfNecessary() {
    		if (!paused) {
    			return;
    		}
    		synchronized (lock) {
    			lock.notify();  
    			paused = false;
    		}
       		idleTime = System.currentTimeMillis();
    	}

    	void setIdleTime(int seconds) {
    		IDLE_TIME = seconds * 1000;
    	}

    	@Override
		public void run() {
    		LogUtil.v(LOG_TAG, "启动空闲状态检查线程.");

				while (!stop) {
					long now = System.currentTimeMillis();
					if ((now - idleTime) > IDLE_TIME) {
						LogUtil.v(LOG_TAG, "准备切换到空闲状态.");
						mService.changeMode(WorkingMode.MODE_IDLE);
						// hang up the thread till next time need to check idle
						LogUtil.v(LOG_TAG, "挂起空闲状态检查线程.");
						synchronized (lock) {
							paused = true;
							try { lock.wait(); } catch (InterruptedException e) { e.printStackTrace(); }        					
						}
						LogUtil.v(LOG_TAG, "恢复空闲状态检查线程.");
					}
					else if ((now - idleTime) > IDLE_TIME - 5000 && mCurrentScreenBrightness > 0) {
						// Dim the screen before idle
						mCurrentScreenBrightness = 0;
						HelperUtil.setScreenBrightness(mService, mCurrentScreenBrightness);
					}
					else {
						LogUtil.v(LOG_TAG, "非空闲, 继续检查....");
						try { Thread.sleep(SLEEP_TIME); } catch (InterruptedException e) { e.printStackTrace(); }
					}
				}

			LogUtil.v(LOG_TAG, "空闲状态检查线程退出.");
		}

    }

    /**
     * 用户教育线程
     */
//    private class TeachingTimerTask extends TimerTask {
//
//		@Override
//		public void run() {
//			TeachingContent content = TeachingTemplete.getRandomContent();
//			if (content != null) {
//				Intent intent = new Intent(CommonMessage.EVT_TEACHING_BEGIN);
//				intent.putExtra("title", content.title);
//				intent.putExtra("subtitle", content.subtitle);
//				intent.putExtra("icon", content.icon.toString());
//				sendBroadcast(intent);
//				LogUtil.v(LOG_TAG, "sendBroadcast " + CommonMessage.EVT_TEACHING_BEGIN);
//			} else {
//				LogUtil.w(LOG_TAG, "UserTeachingMap.getRandomContent() return null.");
//			}
//		}
//    	
//    }

    private static class TtsMessage {
    	boolean callbackNotify;	// 是否处理回调消息
    	boolean needToNotify;	// 是否需要将内容显示到UI上
    	int id;					// 执行顺序id
    	String content;			// 播报内容
    }

}

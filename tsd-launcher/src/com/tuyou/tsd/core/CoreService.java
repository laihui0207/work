package com.tuyou.tsd.core;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import com.tuyou.tsd.common.CommonApps;
import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDComponent;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.TSDShare;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.common.util.TsdHelper;
import com.tuyou.tsd.launcher.NewSleepingActivity;

/**
 * CoreService是图小宝系统（以下简称系统）最主要的后台进程，在系统整个生命周期内一直存在。
 * 它在系统启动后开始运行，仅在系统关闭前才会退出。主要职责为：
 * a) 管理系统运状态（如启动、退出、休眠、唤醒等）；
 * b) 负责根据系统不同状态对其它Service及App进行调度；
 * c) 负责监听来自硬件、Android系统及车载OBD发出的消息事件，并做出响应。
 *    如：硬按键、G-Sensor、ACC on/off、低电休眠等。
 *
 * @author ruhai
 * 2014-8
 */
public class CoreService extends Service {
	private static final String LOG_TAG = "CoreService";
	private static final boolean DEBUG = false;

	/**
	 * 系统运行状态定义
	 */
	public static enum ServiceState {
		STATE_NONE("none"),
		STATE_LOADING("loading"),
		STATE_START("starting"),	// 启动状态
		STATE_INIT("initializing"),	// 初始化状态
		STATE_RESUME("working"),	// 唤醒状态
		STATE_SUSPEND("suspend"),	// 休眠状态
		STATE_STOP("hibernated"),	// 停止状态
		STATE_ERROR("error");
		public String value;
		private ServiceState(String v) {value = v;}
	}

	/**
	 * 运行时工作模式定义
	 */
	public static enum WorkingMode {
		MODE_NONE("none"),
		MODE_STANDBY("standby"),		 // 待机模式
		MODE_IDLE("idle"),				 // 空闲模式
		MODE_INTERACTING("interaction"), // 交互模式
		MODE_MAP("navigation"),			 // 地图模式
		MODE_AUDIO("music"),	 // 音乐播放模式
		MODE_ACCIDENT("accident"),		 // 事故模式
		MODE_TAKE_PICTURE("picturing"),	 // 拍照模式
		MODE_VIDEO_PLAYING("video"),	 // 查看视频（行车记录）
		MODE_SLEEP("sleep"),			 // 休眠模式
		MODE_UPDATE("update"),			 // 升级模式
		MODE_ALERT("alert");			 // 报警模式
		public String value;
		private WorkingMode(String v) {value = v;}
	}

	/**
	 * 工作模式类别, 如音乐模式下的新闻、音乐、笑话等
	 */
	public static enum ContentType {
		TYPE_NONE,
		TYPE_MUSIC,			// 音乐
		TYPE_NEWS,			// 新闻
		TYPE_JOKE,			// 笑话
		TYPE_VOICE_MENU,	// 语音菜单交互
		TYPE_MAP,			// 导航首页
		TYPE_NAVIGATION		// 路线查看
	}
	
	//music
	public static String SLEEP_MUSIC_NAME = "";
	public static boolean SLEEP_MUSIC_IS_PLAYING = true;

	// 业务逻辑控制器，将来要把跟业务逻辑相关的内容放到这个类中去
	// CoreService只做为一个纯粹的状态机
	private SystemController mController;

    ServiceState mCurrentState = ServiceState.STATE_NONE;	// 当前运行状态
    ServiceState mPrevState = ServiceState.STATE_NONE;		// 之前运行状态

    WorkingMode mCurrentMode = WorkingMode.MODE_NONE;		// 当前工作模式
	WorkingMode mPrevMode = WorkingMode.MODE_NONE;			// 之前工作模式

	ContentType mCurrentType = ContentType.TYPE_NONE;		// 当前模式类别, 目前仅在音乐播放模式时使用
	ContentType mPrevType = ContentType.TYPE_NONE;			// 之前类别

	boolean mLoadingCompleted;

    // Services running state
	static final int VOICE_SERVICE = 0x01;
	static final int CARDVR_SERVICE = 0x02;
	static final int AUDIO_SERVICE = 0x04;
	static final int NAVIGATION_SERVICE   = 0x08;
	static final int MESSAGE_SERVICE  = 0x16;
	static final int UPDATE_SERVICE = 0x32;
	int mServiceState;

	//sleep
	public static volatile boolean SLEEP_ACTIVITY = false;
	
	// Apps running state
	static final int VOICE_APP = 0x01;
	static final int CARDVR_APP = 0x02;
	static final int AUDIO_APP = 0x04;
	static final int NAVIGATION_APP = 0x08;
	int mAppState;

	static final int EXTERNAL_STORAGE_READY = 1;
	static final int LOADING_COMPLETED = 100;
	static final int SHAKE_HAPPENED = 200;
	
	//test
	public static String TestYZSstr = null;

	private final Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EXTERNAL_STORAGE_READY:	// In loading state
				LogUtil.v(LOG_TAG, "EXTERNAL_STORAGE_READY.");
				// Now continue to start system
				mController.start();
				new InitCheckingThread().start();
				break;

			case SHAKE_HAPPENED:
				LogUtil.v(LOG_TAG, "SHAKE_HAPPENED.");
				mController.onShake();
				break;

			default:
				super.handleMessage(msg);
			}
		}
	};
	final Messenger mMessenger = new Messenger(mHandler);

	private class InitCheckingThread extends Thread {

		@Override
		public void run() {
			LogUtil.v(LOG_TAG, "InitCheckingThread is started.");
			while (true) {
				// 目前只限制等待语音服务启动后系统才进入运行状态，后面如果需要更加严格的限制，则只需打开
				// 这里的条件即可。by Alex, 2014-11-20
				if ((mServiceState & VOICE_SERVICE) == VOICE_SERVICE //&&
					/*(mServiceState & VIDEO_SERVICE) == VIDEO_SERVICE &&*/
					/*(mServiceState & AUDIO_SERVICE) == AUDIO_SERVICE &&*/
					/*(mServiceState & MAP_SERVICE) == MAP_SERVICE &&*/
					/*(mServiceState & UPDATE_SERVICE) == UPDATE_SERVICE &&*/
					/*(mServiceState & MESSAGE_SERVICE) == MESSAGE_SERVICE*/)
				{
					LogUtil.d(LOG_TAG, "All services are started.");
					sendBroadcast(new Intent(TSDEvent.System.LOADING_COMPLETE));
					mLoadingCompleted = true;
					break;
				}
			}
			LogUtil.v(LOG_TAG, "InitCheckingThread is quited.");
		}
	}

	/**
	 * 系统广播消息监听
	 */
	private final BroadcastReceiver mSystemEventReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		LogUtil.v(LOG_TAG, "Received the broadcast: " + action);

    		if (action.equals(TSDEvent.System.LOADING_COMPLETE)) {
				if (!checkFirstInitFinished()) {
					changeState(ServiceState.STATE_INIT);
				} else {
	    			// First go to START state
	   				changeState(ServiceState.STATE_START);
	   				// Then, if ACC is on, change to RESUME state
	   				if (TsdHelper.getAccStatus()) {
	   					changeState(ServiceState.STATE_RESUME);
	   				}
				}
    		}
    		// Initialization completed
    		else if (action.equals(CommonMessage.INIT_COMPLETE)) {
    			// First go to START state
   				changeState(ServiceState.STATE_START);
   				// Then, if ACC is on, change to RESUME state
   				if (TsdHelper.getAccStatus()) {
   					changeState(ServiceState.STATE_RESUME);
   				}
    		}
    		// ACC on
    		else if (action.equals(TSDEvent.System.ACC_ON)) {
    		    LogUtil.v(LOG_TAG, "CoreService.java::onReceive=> [点火触发提示]");
    			Toast.makeText(context, "点火触发.", Toast.LENGTH_SHORT).show();

    		    if (mCurrentState == ServiceState.STATE_START ||
    		    	mCurrentState == ServiceState.STATE_SUSPEND) {
    		    	changeState(ServiceState.STATE_RESUME);	
    		    }
    		    else if (mCurrentState == ServiceState.STATE_STOP) {
    		    	changeState(ServiceState.STATE_START);
   					changeState(ServiceState.STATE_RESUME);
    		    }
    		}
    		// ACC off
    		else if (action.equals(TSDEvent.System.ACC_OFF)) {
    		    LogUtil.v(LOG_TAG, "CoreService.java::onReceive=> [熄火触发提示]");
    			Toast.makeText(context, "熄火触发.", Toast.LENGTH_SHORT).show();

    			// 若当前系统正在运行，则休眠
    		    if (mCurrentState == ServiceState.STATE_START ||
    		    	mCurrentState == ServiceState.STATE_RESUME) {
        		   	changeState(ServiceState.STATE_SUSPEND);	
        		}
    		}
    		// Battery low
    		else if (action.equals(Intent.ACTION_BATTERY_LOW)) {
    			Toast.makeText(context, "电池电量不足.", Toast.LENGTH_SHORT).show();

    			// 低电状态，系统关闭
    			switch (mCurrentState) {
    			case STATE_INIT:
    			case STATE_START:
    			case STATE_RESUME:
    				// 若系统当前未在休眠状态，则先将系统转为休眠状态后再关闭
    				changeState(ServiceState.STATE_SUSPEND);

    			default:
   					changeState(ServiceState.STATE_STOP);
    			}
    		}
    		else if (action.equals(TSDEvent.System.QUERY_SYSTEM_STATE) ||
    				 action.equals(TSDEvent.System.QUERY_SYSTEM_MODE)) {
    			// TODO:
    		}else if(action.equals(TSDEvent.System.WELCOME_FINISHED)){
    			SharedPreferences pref = HelperUtil.getCommonPreference(CoreService.this,
    					TSDComponent.CORE_SERVICE_PACKAGE,
    					TSDShare.SYSTEM_SETTING_PREFERENCES);
    			if(pref != null){
    				pref.edit().putString("system_init", "true").commit();
    			}
    		}
		}
		
	};

	/**
	 * 各组件广播消息监听
	 */
	private final BroadcastReceiver mServicesEventReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		LogUtil.v(LOG_TAG, "Received the broadcast: " + action);
    		
    		if(action.equals(CommonApps.SLEEP_SHOW_CONTENT)){
    			String musictitle = intent.getStringExtra(CommonApps.SLEEP_CONTENT_TITLE);
    			Log.v(LOG_TAG,"SLEEP_SHOW_CONTENT ="+musictitle);
    			if(musictitle==null || musictitle.equals("")){
    				SLEEP_MUSIC_NAME = "";
    			}else{
    				SLEEP_MUSIC_NAME = musictitle;
    			}
    		}
    		if(action.equals(CommonApps.SLEEP_BEEN_PLAY_MUSIC)){
    			SLEEP_MUSIC_IS_PLAYING = intent.getBooleanExtra(CommonApps.SLEEP_PLAY_MUSIC_NEED_PLAY, true);
    			System.out.println("sssssss-------"+SLEEP_MUSIC_IS_PLAYING);
    		}
    		
    		if(action.equals(CommonApps.BROADCAST_SHOW_SLEEP)){
    			if(!SLEEP_ACTIVITY){
    				SLEEP_ACTIVITY = true;
        			Intent sleepIntent = new Intent(CoreService.this,NewSleepingActivity.class);
        			sleepIntent.putExtra(CommonApps.BROADCAST_SHOW_SLEEP_ACTIVITY_NAME,
        					intent.getStringExtra(CommonApps.BROADCAST_SHOW_SLEEP_ACTIVITY_NAME));
        			sleepIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        			startActivity(sleepIntent);
    			}
    		}
    		
    		if(action.equals(CommonApps.BROADCAST_RESTART_VOICE)){
    			Log.v("fq","BROADCAST_RESTART_VOICE");
    			startService(new Intent(TSDComponent.VOICE_ASSISTANT_SERVICE));
    		}else if(action.equals(CommonApps.BROADCAST_TEST_VOICE_RESULT)){
				Log.v("fq","BROADCAST_TEST_VOICE_RESULT core service");
				String str = intent.getStringExtra(CommonApps.BROADCAST_TEST_VOICE_STRING);
				TestYZSstr = str;
			}
			
    		// 各个服务的启动和销毁的通知
    		if (action.equals(TSDEvent.Interaction.SERVICE_STARTED)) {
    			LogUtil.i(LOG_TAG, "VoiceAssistant service is started-up.");
    			mServiceState |= VOICE_SERVICE;
    		}
    		else if (action.equals(TSDEvent.Interaction.SERVICE_STOPPED)) {
    			LogUtil.i(LOG_TAG, "VoiceAssistant service is down.");
    			mServiceState &= ~VOICE_SERVICE;
    		}
    		else if (action.equals(TSDEvent.Audio.SERVICE_STARTED)) {
    			LogUtil.i(LOG_TAG, "Audio service is started-up.");
    			mServiceState |= AUDIO_SERVICE;
    		}
    		else if (action.equals(TSDEvent.Audio.SERVICE_STOPPED)) {
    			LogUtil.i(LOG_TAG, "Audio service is down.");
    			mServiceState &= ~AUDIO_SERVICE;
    		}
    		else if (action.equals(TSDEvent.CarDVR.SERVICE_STARTED)) {
    			LogUtil.i(LOG_TAG, "CarDVR service is started-up.");
    			mServiceState |= CARDVR_SERVICE;
    		}
    		else if (action.equals(TSDEvent.CarDVR.SERVICE_STOPPED)) {
    			LogUtil.i(LOG_TAG, "CarDVR service is down.");
    			mServiceState &= ~CARDVR_SERVICE;
    		}
    		else if (action.equals(TSDEvent.Navigation.SERVICE_STARTED)) {
    			LogUtil.i(LOG_TAG, "Navigation service is started-up.");
    			mServiceState |= NAVIGATION_SERVICE;
    		}
    		else if (action.equals(TSDEvent.Navigation.SERVICE_STOPPED)) {
    			LogUtil.i(LOG_TAG, "Navigation service is down.");
    			mServiceState &= ~NAVIGATION_SERVICE;
    		}
    		else if (action.equals(TSDEvent.Update.SERVICE_STARTED)) {
    			LogUtil.i(LOG_TAG, "Update service is started-up.");
    			mServiceState |= UPDATE_SERVICE;
    		}
    		else if (action.equals(TSDEvent.Update.SERVICE_STOPPED)) {
    			LogUtil.i(LOG_TAG, "Update service is down.");
    			mServiceState &= ~UPDATE_SERVICE;
    		}
    		else if (action.equals(TSDEvent.Push.SERVICE_STARTED)) {
    			LogUtil.i(LOG_TAG, "Message service is started-up.");
    			mServiceState |= MESSAGE_SERVICE;
    		}
    		else if (action.equals(TSDEvent.Push.SERVICE_STOPPED)) {
    			LogUtil.i(LOG_TAG, "Message service is down.");
    			mServiceState &= ~MESSAGE_SERVICE;
    		}
    		// 各个应用启动与退出消息
    		else if (action.equals(TSDEvent.Interaction.APP_STARTED)) {
    			LogUtil.i(LOG_TAG, "VoiceAssistant app is started-up.");
    			mAppState |= VOICE_APP;
    		}
    		else if (action.equals(TSDEvent.Interaction.APP_STOPPED)) {
    			LogUtil.i(LOG_TAG, "VoiceAssistant app is down.");
    			mAppState &= ~VOICE_APP;
    		}
    		else if (action.equals(TSDEvent.Audio.APP_STARTED)) {
    			LogUtil.i(LOG_TAG, "Audio app is started-up.");
    			mAppState |= AUDIO_APP;
    			mController.onAudioPlayingStart();
    		}
    		else if (action.equals(TSDEvent.Audio.APP_STOPPED)) {
    			LogUtil.i(LOG_TAG, "Audio app is down.");
    			mAppState &= ~AUDIO_APP;
    			mController.onAudioPlayingFinish();
    		}
    		else if (action.equals(TSDEvent.CarDVR.APP_STARTED)) {
    			LogUtil.i(LOG_TAG, "CarDVR app is started-up.");
    			mAppState |= CARDVR_APP;
    			mController.onVideoPlayingStart();
    		}
    		else if (action.equals(TSDEvent.CarDVR.APP_STOPPED)) {
    			LogUtil.i(LOG_TAG, "CarDVR app is down.");
    			mAppState &= ~CARDVR_APP;
    			mController.onVideoPlayingFinish();
    		}
    		else if (action.equals(TSDEvent.Navigation.APP_STARTED)) {
    			LogUtil.i(LOG_TAG, "Navigation app is started-up.");
    			mAppState |= NAVIGATION_APP;
    			mController.onNavigatingStart();
    		}
    		else if (action.equals(TSDEvent.Navigation.APP_STOPPED)) {
    			LogUtil.i(LOG_TAG, "Navigation app is down.");
    			mAppState &= ~NAVIGATION_APP;
    			mController.onNavigatingFinish();
    		}
		}
		
	};

	@Override
	public void onCreate() {
		LogUtil.v(LOG_TAG, "onCreate()....");

		if (DEBUG)
			android.os.Debug.waitForDebugger();

		registerBroadcastReceiver();

		// 业务逻辑控制器，所有跟业务逻辑相关的内容由这个类进行控制
		// CoreService只担当状态机的角色
		mController = new SystemController(this);

		changeState(ServiceState.STATE_LOADING);
	}

	@Override
	public void onDestroy() {
		LogUtil.d(LOG_TAG, "onDestroy....");
		mController.stop();
		unregisterBroadcastReceiver();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.v(LOG_TAG, "onStartCommand()....");
		return START_STICKY;
	}

	public class LocalBinder extends Binder {
		public CoreService getService() {
			return CoreService.this;
		}
	}
	private IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		LogUtil.v(LOG_TAG, "onBind()....");		
		return mBinder;
	}

	/**
	 * Change the system state
	 * @param destState
	 */
	public void changeState(ServiceState destState) {
		if (mCurrentState == destState)
			return;

		LogUtil.d(LOG_TAG, "changeState, " + mCurrentState + " --> " + destState);

		mPrevState = mCurrentState;
		mCurrentState = destState;
	
		switch (mCurrentState) {
		case STATE_LOADING:
			mController.onSystemLoading();
			break;
		case STATE_INIT:
			mController.onSystemInit();
			break;
		case STATE_START:
			mController.onSystemStarted();
			break;
		case STATE_RESUME:
			mController.onSystemResumed();
			break;
		case STATE_SUSPEND:
			mController.onSystemSuspended();
			break;
		case STATE_STOP:
			mController.onSystemStopped();
			break;
		case STATE_ERROR:
			mController.onSystemError();
		default:
			// default do nothing
		}
	}

	public void changeMode(WorkingMode destMode) {
		changeMode(destMode, ContentType.TYPE_NONE);
	}

	/**
	 * Change the working mode
	 * @param destMode
	 * @param param
	 */
	public void changeMode(WorkingMode destMode, ContentType param) {
		if (mCurrentMode == destMode && mCurrentType == param)
			return;

		LogUtil.d(LOG_TAG, "change RunningMode, " + mCurrentMode + " --> " + destMode);

		mPrevMode = mCurrentMode;
		mPrevType = mCurrentType;

		mCurrentMode = destMode;
		mCurrentType = param;
	
		switch (destMode) {
		case MODE_STANDBY:
			mController.onStandbyMode();
			break;
		case MODE_IDLE:
			mController.onIdleMode();
			break;
		case MODE_INTERACTING:
			mController.onInteractingMode(param);
			break;
		case MODE_AUDIO:
			mController.onAudioMode(param);
			break;
		case MODE_TAKE_PICTURE:
			mController.onTakePictureMode();
			break;
		case MODE_MAP:
			mController.onNavigatingMode(param);
			break;
		case MODE_VIDEO_PLAYING:
			mController.onVideoPlayingMode();
			break;
		default:
			break;
		}
	
	}

	public ServiceState getCurrentState() {
		return mCurrentState;
	}

	public WorkingMode getCurrentWorkingMode() {
		return mCurrentMode;
	}
	
	public void reSetWorkingMode(){
		LogUtil.v(LOG_TAG, "reSetWorkingMode");
	    mCurrentMode = WorkingMode.MODE_NONE;	
	    mPrevMode = WorkingMode.MODE_NONE;
	}

	public boolean isLoadingCompleted() {
		return mLoadingCompleted;
	}

	public void wakeUpDevice() {
		// return to previous mode 
		changeMode(mPrevMode, mPrevType);
	}

	/**
     * 注册广播消息监听
     */
	private void registerBroadcastReceiver() {
		registerSystemEventReceiver();
		registerServicesReceiver();
	}

	private void registerSystemEventReceiver() {
		IntentFilter filter = new IntentFilter();
		// System event
		filter.addAction(TSDEvent.System.LOADING_COMPLETE);
		filter.addAction(CommonMessage.INIT_COMPLETE);			// 初始化完成
		filter.addAction(TSDEvent.System.ACC_ON);			// ACC on
		filter.addAction(TSDEvent.System.ACC_OFF);			// ACC off
		filter.addAction(Intent.ACTION_BATTERY_LOW);			// 低电
		filter.addAction(TSDEvent.System.QUERY_SYSTEM_STATE);
		filter.addAction(TSDEvent.System.QUERY_SYSTEM_MODE);
		filter.addAction(TSDEvent.System.WELCOME_FINISHED);

		registerReceiver(mSystemEventReceiver, filter);
	}

	private void registerServicesReceiver() {
		IntentFilter filter = new IntentFilter();
		// 各个服务的启动与销毁
		filter.addAction(TSDEvent.Interaction.SERVICE_STARTED);
		filter.addAction(TSDEvent.Interaction.SERVICE_STOPPED);
		filter.addAction(TSDEvent.CarDVR.SERVICE_STARTED);
		filter.addAction(TSDEvent.CarDVR.SERVICE_STOPPED);
		filter.addAction(TSDEvent.Audio.SERVICE_STARTED);
		filter.addAction(TSDEvent.Audio.SERVICE_STOPPED);
		filter.addAction(TSDEvent.Navigation.SERVICE_STARTED);
		filter.addAction(TSDEvent.Navigation.SERVICE_STOPPED);
		filter.addAction(TSDEvent.Update.SERVICE_STARTED);
		filter.addAction(TSDEvent.Update.SERVICE_STOPPED);
		filter.addAction(TSDEvent.Push.SERVICE_STARTED);
		filter.addAction(TSDEvent.Push.SERVICE_STOPPED);
		// 各个应用的启动与退出
		filter.addAction(TSDEvent.Interaction.APP_STARTED);
		filter.addAction(TSDEvent.Interaction.APP_STOPPED);
		filter.addAction(TSDEvent.CarDVR.APP_STARTED);
		filter.addAction(TSDEvent.CarDVR.APP_STOPPED);
		filter.addAction(TSDEvent.Audio.APP_STARTED);
		filter.addAction(TSDEvent.Audio.APP_STOPPED);
		filter.addAction(TSDEvent.Navigation.APP_STARTED);
		filter.addAction(TSDEvent.Navigation.APP_STOPPED);
		
		//sleep activity
		filter.addAction(CommonApps.SLEEP_SHOW_CONTENT);
		filter.addAction(CommonApps.SLEEP_BEEN_PLAY_MUSIC);
		filter.addAction(CommonApps.BROADCAST_SHOW_SLEEP);
		filter.addAction(CommonApps.BROADCAST_RESTART_VOICE);
		
		//test
		filter.addAction(CommonApps.BROADCAST_TEST_VOICE_RESULT);
		
		registerReceiver(mServicesEventReceiver, filter);
	}

	private void unregisterBroadcastReceiver() {
		unregisterReceiver(mSystemEventReceiver);
		unregisterReceiver(mServicesEventReceiver);
	}

	/**
	 * 检查设备是否已初始化
	 * @return
	 */
	private boolean checkFirstInitFinished() {
		SharedPreferences pref = HelperUtil.getCommonPreference(this,
				TSDComponent.CORE_SERVICE_PACKAGE,
				TSDShare.SYSTEM_SETTING_PREFERENCES);
		boolean r = false;
		if(pref != null){
			r = Boolean.parseBoolean(pref.getString("system_init", "false"));
//			pref.edit().putString("system_init", "true").commit();
		}
		LogUtil.v(LOG_TAG, "checkFirstInitFinished, return " + r+"  pref="+pref);
		return r;
	}

//	private void checkRunningServices() {
//		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//		List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
//		if (processes != null) {
//			for (RunningAppProcessInfo info : processes) {
//				if (info.processName.startsWith("com.tuyou")) {
//					LogUtil.d(LOG_TAG, "detect the process " + info.processName + " is already running.");
//					checkProcess(info.processName);
//				}
//			}
//		}
//	}

//	private void checkProcess(String processName) {
//		if (processName.equals(TSDComponent.VOICE_ASSISTANT_PACKAGE)) {
//			mServiceState |= VOICE_SERVICE;
//		}
//		else if (processName.equals(TSDComponent.AUDIO_PACKAGE)) {
//			mServiceState |= AUDIO_SERVICE;
//		}
//		else if (processName.equals(TSDComponent.CAR_DVR_PACKAGE)) {
//			mServiceState |= CARDVR_SERVICE;
//		}
//		else if (processName.equals(TSDComponent.NAVIGATOR_PACKAGE)) {
//			mServiceState |= NAVIGATION_SERVICE;
//		}
//		else if (processName.equals("com.tuyou.tsd.updatesoft")) {
//			mServiceState |= UPDATE_SERVICE;
//		}
//		else if (processName.equals("com.tuyou.tsd:bdservice_v1")) {
//			mServiceState |= PUSH_SERVICE;
//		}
//	}
}

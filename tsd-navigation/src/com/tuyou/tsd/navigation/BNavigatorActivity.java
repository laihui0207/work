package com.tuyou.tsd.navigation;

import java.sql.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.comapi.mapcontrol.BNMapController;
import com.baidu.navisdk.comapi.routeplan.BNRoutePlaner;
import com.baidu.navisdk.comapi.setting.BNSettingManager;
import com.baidu.navisdk.comapi.setting.SettingParams;
import com.baidu.navisdk.comapi.tts.BNTTSPlayer;
import com.baidu.navisdk.comapi.tts.BNavigatorTTSPlayer;
import com.baidu.navisdk.comapi.tts.IBNTTSPlayerListener;
import com.baidu.navisdk.model.datastruct.LocData;
import com.baidu.navisdk.model.datastruct.SensorData;
import com.baidu.navisdk.ui.routeguide.BNavigator;
import com.baidu.navisdk.ui.routeguide.IBNavigatorListener;
import com.baidu.navisdk.ui.widget.RoutePlanObserver;
import com.baidu.navisdk.ui.widget.RoutePlanObserver.IJumpToDownloadListener;
import com.baidu.nplatform.comapi.map.MapGLSurfaceView;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.navigation.mode.SysApplication;

public class BNavigatorActivity extends BaseActivity implements
		OnClickListener, OnCheckedChangeListener {
	private String TAG = "BNavigatorActivity";
	private final int TYPE_NULL = 0, TYPE_MENU = 1, TYPE_MODE = 2,
			TYPE_CONTENT = 3, TYPE_DAYNIGHT = 4;
	private Date cacheDate;
	private MapGLSurfaceView nMapView;
	private Button modeBtn, contentBtn, dayNightBtn, modeXXBtn, modeJDBtn,
			modeBZBtn, dayBtn, nightBtn, aovtn;
	private ImageView menuImageView;
	private LinearLayout menuLayout, dayNightLayout, modeLayout, contentLayout;
	private View layout;
	private CheckBox dzyBox, xstxBox, aqjsBox, qflkBox, zxBox;
	/**
	 * 是否进行语音播报
	 */
	public boolean isPlayTTS = true;
	private String playString = "";
	private boolean isTTS = true;
	private int idleTime = 0;
	private int onNavTime = 60;
	private boolean isComeNav = false, isOnPause = false;
	private AudioManager mAudioManager;
	private idleThread idleThread;

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// Toast.makeText(context, "语音播报结束", Toast.LENGTH_SHORT).show();
			String action = intent.getAction();
			LogUtil.d(TAG, "action = " + action);
			if (action.equals("tsd.tts.PLAY_FINISHED")) {
				isTTS = true;
			} else if (action.equals(TSDEvent.Navigation.IDLE_NAV_UPDATE)) {
				if (isOnPause) {
					idleTime = 0;
					if (idleThread == null) {
						LogUtil.d(TAG, "启动空闲一分钟会导航线程");
						isComeNav = true;
						idleThread = new idleThread();
						idleThread.start();
					}
				}
			} else if (action.equals(TSDEvent.Navigation.IDLE_NAV_STOP)) {
				isComeNav = false;
			}
			// else if (action.equals("tsd.tts.CALL_BACK_PLAY_BEGIN")) {
			// isPlayTTS = false;
			// } else if (action.equals("tsd.tts.CALL_BACK_PLAY_END")) {
			// isPlayTTS = true;
			// }

		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 创建NmapView
		if (Build.VERSION.SDK_INT < 14) {
			BaiduNaviManager.getInstance().destroyNMapView();
		}
		nMapView = BaiduNaviManager.getInstance().createNMapView(this);

		// 创建导航视图
		View navigatorView = BNavigator.getInstance().init(
				BNavigatorActivity.this, getIntent().getExtras(), nMapView);
		// 填充视图
		setContentView(navigatorView);
		SysApplication.getInstance().addActivity(this);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				RoutePlanActivity.musicM, 0);
		sendBroadcast(new Intent(TSDEvent.Navigation.NAV_STARTED));
		//因为特殊需求，导航中恢复音乐播放，所以此处发送交互介绍广播
		sendBroadcast(new Intent(TSDEvent.Interaction.INTERACTION_FINISH_FROM_CORE_SERVICE));
		LogUtil.d(TAG, "send:" + TSDEvent.Navigation.NAV_STARTED);
		LogUtil.d(TAG, "send:" + TSDEvent.Interaction.INTERACTION_FINISH_FROM_CORE_SERVICE);
		BNMapController.getInstance().setDrawNaviLogo(false);
		LayoutInflater inflater = BNavigatorActivity.this.getLayoutInflater();
		layout = inflater.inflate(R.layout.layout_nav_window, null);
		initView();
		addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		cacheDate = new Date(System.currentTimeMillis());
		BNavigator.getInstance().setListener(mBNavigatorListener);
		BNavigator.getInstance().startNav();
		BNRoutePlaner.getInstance().setObserver(
				new RoutePlanObserver(this, new IJumpToDownloadListener() {

					@Override
					public void onJumpToDownloadOfflineData() {

					}
				}));
		// 动态注册接收语音播报结束广播
		IntentFilter filter = new IntentFilter();
		filter.addAction("tsd.tts.PLAY_FINISHED");
		filter.addAction(TSDEvent.Navigation.IDLE_NAV_UPDATE);
		filter.addAction(TSDEvent.Navigation.IDLE_NAV_STOP);
		// 接收广播停止发送播报
		// filter.addAction("tsd.tts.CALL_BACK_PLAY_BEGIN");
		// filter.addAction("tsd.tts.CALL_BACK_PLAY_END");
		registerReceiver(broadcastReceiver, filter);
		// 初始化TTS. 或者也可以使用独立TTS模块，不用使用导航SDK提供的TTS
		// BNTTSPlayer.initPlayer();
		// 设置TTS播放回调
		BNavigatorTTSPlayer.setTTSPlayerListener(new IBNTTSPlayerListener() {

			@Override
			public int playTTSText(String arg0, int arg1) {
				LogUtil.d(TAG, "isPlayTTS = " + isPlayTTS);
				if (isPlayTTS) {
					if (arg0.contains("米行驶")) {
						arg0 = arg0.replace("米行驶", "米 行驶");
					}
					if (!arg0.startsWith("路径规划成功") && !playString.equals(arg0)) {
						playString = arg0;
						playBroadcast(arg0, 0);
					}
					if (arg0.contains("导航结束")) {
						isPlayTTS = false;
					}
					isTTS = false;
				}
				return 0;
			}

			@Override
			public void phoneHangUp() {
				// 手机挂断
			}

			@Override
			public void phoneCalling() {
				// 通话中
			}

			@Override
			public int getTTSState() {
				if (isTTS) {
					return 1;
				} else {
					return 0;
				}

			}
		});
	}

	private IBNavigatorListener mBNavigatorListener = new IBNavigatorListener() {

		@Override
		public void onYawingRequestSuccess() {
			// TODO 偏航请求成功

		}

		@Override
		public void onYawingRequestStart() {
			// TODO偏航请求

		}

		@Override
		public void onPageJump(int jumpTiming, Object arg) {
			if (IBNavigatorListener.PAGE_JUMP_WHEN_GUIDE_END == jumpTiming) {
				Date newDate = new Date(System.currentTimeMillis());
				long times = (newDate.getTime() - cacheDate.getTime())
						/ (1000 * 60);
				if (times >= 5) {
					SysApplication.getInstance().exit();
				} else {
					stopBroadcast();
					finish();
				}
			} else if (IBNavigatorListener.PAGE_JUMP_WHEN_ROUTE_PLAN_FAIL == jumpTiming) {
				finish();
			}
		}

		@Override
		public void notifyGPSStatusData(int arg0) {
		}

		@Override
		public void notifyLoacteData(LocData arg0) {
			LogUtil.v("navDate", "定位精度  = " + arg0.accuracy + " 纬度:"
					+ arg0.latitude + " 经度:" + arg0.longitude + " 方向角度 :"
					+ arg0.direction + " 卫星数:" + arg0.satellitesNum + " 速度:"
					+ arg0.speed + " 经纬度默认值:" + arg0.LOCDEFAULT + " 海拔高度:"
					+ arg0.altitude);
		}

		@Override
		public void notifyNmeaData(String arg0) {
		}

		@Override
		public void notifySensorData(SensorData arg0) {
		}

		@Override
		public void notifyStartNav() {
			BaiduNaviManager.getInstance().dismissWaitProgressDialog();
		}

		@Override
		public void notifyViewModeChanged(int arg0) {

		}
	};

	@Override
	public void onResume() {
		isOnPause = false;
		sendBroadcast(new Intent(TSDEvent.System.DISABLE_IDLE_CHECK));
		LogUtil.d(TAG, "send:" + TSDEvent.System.DISABLE_IDLE_CHECK);
		BNavigator.getInstance().resume();
		super.onResume();
		BNMapController.getInstance().onResume();
	};

	@Override
	public void onPause() {
		isOnPause = true;
		sendBroadcast(new Intent(TSDEvent.System.ENABLE_IDLE_CHECK));
		LogUtil.d(TAG, "send:" + TSDEvent.System.ENABLE_IDLE_CHECK);
		BNavigator.getInstance().pause();
		super.onPause();
		BNMapController.getInstance().onPause();
	}

	public void onBackPressed() {
		BNavigator.getInstance().onBackPressed();
	}

	@Override
	public void onDestroy() {
		sendBroadcast(new Intent(TSDEvent.Navigation.NAV_STOPPED));
		LogUtil.d(TAG, "send:" + TSDEvent.Navigation.NAV_STOPPED);
		BNTTSPlayer.releaseTTSPlayer();
		stopBroadcast();
		SearchService.navIntent = null;
		BNavigator.destory();
		BNRoutePlaner.getInstance().setObserver(null);
		sendBroadcast(new Intent(TSDEvent.Navigation.APP_STOPPED));
		sendBroadcast(new Intent(TSDEvent.System.ENABLE_IDLE_CHECK));
		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
		}
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
	}

	public void initView() {
		menuLayout = (LinearLayout) layout.findViewById(R.id.layout_nav_menu);
		dayNightLayout = (LinearLayout) layout
				.findViewById(R.id.layout_nav_daynight);
		modeLayout = (LinearLayout) layout.findViewById(R.id.layout_nav_mode);
		menuImageView = (ImageView) layout.findViewById(R.id.img_nav_menu);
		contentLayout = (LinearLayout) layout
				.findViewById(R.id.layout_nav_connect);
		modeBtn = (Button) layout.findViewById(R.id.btn_nav_play_mode);
		contentBtn = (Button) layout.findViewById(R.id.btn_nav_play_content);
		dayNightBtn = (Button) layout.findViewById(R.id.btn_nav_daynight);
		modeXXBtn = (Button) layout.findViewById(R.id.btn_nav_mode_xx);
		modeJDBtn = (Button) layout.findViewById(R.id.btn_nav_mode_jd);
		modeBZBtn = (Button) layout.findViewById(R.id.btn_nav_mode_bz);
		dayBtn = (Button) layout.findViewById(R.id.btn_nav_daynight_day);
		nightBtn = (Button) layout.findViewById(R.id.btn_nav_daynight_night);
		aovtn = (Button) layout.findViewById(R.id.btn_nav_daynight_avo);
		dzyBox = (CheckBox) layout.findViewById(R.id.check_connect_dzy);
		xstxBox = (CheckBox) layout.findViewById(R.id.check_connect_xstz);
		aqjsBox = (CheckBox) layout.findViewById(R.id.check_connect_aqjs);
		qflkBox = (CheckBox) layout.findViewById(R.id.check_connect_qflk);
		zxBox = (CheckBox) layout.findViewById(R.id.check_connect_zxtx);
		dzyBox.setOnCheckedChangeListener(this);
		xstxBox.setOnCheckedChangeListener(this);
		aqjsBox.setOnCheckedChangeListener(this);
		qflkBox.setOnCheckedChangeListener(this);
		zxBox.setOnCheckedChangeListener(this);
		menuImageView.setOnClickListener(this);
		modeBtn.setOnClickListener(this);
		contentBtn.setOnClickListener(this);
		dayNightBtn.setOnClickListener(this);
		modeXXBtn.setOnClickListener(this);
		modeJDBtn.setOnClickListener(this);
		modeBZBtn.setOnClickListener(this);
		dayBtn.setOnClickListener(this);
		nightBtn.setOnClickListener(this);
		aovtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_nav_menu:
			if (menuLayout.getVisibility() == View.GONE) {
				showLayout(TYPE_MENU);
			} else {
				showLayout(TYPE_NULL);
			}
			break;
		case R.id.btn_nav_play_mode:
			if (modeLayout.getVisibility() == View.GONE) {
				showLayout(TYPE_MODE);
			} else {
				modeLayout.setVisibility(View.GONE);
			}
			break;
		case R.id.btn_nav_play_content:
			if (contentLayout.getVisibility() == View.GONE) {
				showLayout(TYPE_CONTENT);
			} else {
				contentLayout.setVisibility(View.GONE);
			}
			break;
		case R.id.btn_nav_daynight:
			if (dayNightLayout.getVisibility() == View.GONE) {
				showLayout(TYPE_DAYNIGHT);
			} else {
				dayNightLayout.setVisibility(View.GONE);
			}
			break;
		case R.id.btn_nav_mode_xx:
			BNSettingManager.getInstance(this).setVoiceMode(0);
			initNavView();
			break;
		case R.id.btn_nav_mode_jd:
			BNSettingManager.getInstance(this).setVoiceMode(1);
			initNavView();
			break;
		case R.id.btn_nav_mode_bz:
			BNSettingManager.getInstance(this).setVoiceMode(2);
			initNavView();
			break;
		case R.id.btn_nav_daynight_day:
			BNSettingManager.getInstance(this).setNaviDayAndNightMode(
					SettingParams.Action.DAY_NIGHT_MODE_DAY);
			initNavView();
			break;
		case R.id.btn_nav_daynight_night:
			BNSettingManager.getInstance(this).setNaviDayAndNightMode(
					SettingParams.Action.DAY_NIGHT_MODE_NIGHT);
			nMapView = BaiduNaviManager.getInstance().createNMapView(this);
			initNavView();
			break;
		case R.id.btn_nav_daynight_avo:
			BNSettingManager.getInstance(this).setNaviDayAndNightMode(
					SettingParams.Action.DAY_NIGHT_MODE_AUTO);
			initNavView();
			break;
		default:
			break;
		}
	}

	/**
	 * 要显示那个layout
	 * 
	 * @param what
	 *            0为关闭全部；1为菜单；2为播报模式；3为播报内容；4为昼夜模式
	 */
	public void showLayout(int what) {
		modeLayout.setVisibility(View.GONE);
		contentLayout.setVisibility(View.GONE);
		dayNightLayout.setVisibility(View.GONE);
		switch (what) {
		case TYPE_NULL:
			menuLayout.setVisibility(View.GONE);
			break;
		case TYPE_MENU:
			menuLayout.setVisibility(View.VISIBLE);
			break;
		case TYPE_MODE:
			modeLayout.setVisibility(View.VISIBLE);
			break;
		case TYPE_CONTENT:
			contentLayout.setVisibility(View.VISIBLE);
			break;
		case TYPE_DAYNIGHT:
			dayNightLayout.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
		layout.requestLayout();
	}

	/**
	 * 重新初始化导航界面，使昼夜模式设置等生效
	 */
	public void initNavView() {
		// 创建导航视图
		View navigatorView = BNavigator.getInstance().init(
				BNavigatorActivity.this, getIntent().getExtras(), nMapView);
		// 填充视图
		setContentView(navigatorView);
		addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.check_connect_dzy:
			BNSettingManager.getInstance(this).setOtherCameraSpeakEnable(
					isChecked);
			initNavView();
			break;
		case R.id.check_connect_xstz:
			BNSettingManager.getInstance(this).setOverSpeedSpeakEnable(
					isChecked);
			initNavView();
			break;
		case R.id.check_connect_aqjs:
			isPlayTTS = !isChecked;
			break;
		case R.id.check_connect_qflk:
			BNSettingManager.getInstance(this)
					.setsNaviRealHistoryITS(isChecked);
			initNavView();
			break;
		case R.id.check_connect_zxtx:
			BNSettingManager.getInstance(this)
					.setStraightSpeakEnable(isChecked);
			initNavView();
			break;

		default:
			break;
		}
	}

	/**
	 * 判断是否需要会导航线程
	 * 
	 * @author Administrator
	 * 
	 */
	class idleThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				while (isComeNav) {
					LogUtil.d(TAG, "idleTime = " + idleTime);
					if (idleTime < onNavTime) {
						idleTime++;
					} else {
						isComeNav = false;
						sendBroadcast(new Intent(SearchService.navingAction));
						idleThread = null;
					}
					sleep(1000);
				}
			} catch (InterruptedException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
	}

}

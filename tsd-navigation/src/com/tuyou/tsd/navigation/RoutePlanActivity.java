package com.tuyou.tsd.navigation;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.CommonParams.Const.ModelName;
import com.baidu.navisdk.comapi.mapcontrol.BNMapController;
import com.baidu.navisdk.comapi.mapcontrol.MapParams.Const.LayerMode;
import com.baidu.navisdk.comapi.routeplan.BNRoutePlaner;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.baidu.navisdk.comapi.setting.SettingParams;
import com.baidu.navisdk.comapi.tts.BNTTSPlayer;
import com.baidu.navisdk.comapi.tts.BNavigatorTTSPlayer;
import com.baidu.navisdk.comapi.tts.IBNTTSPlayerListener;
import com.baidu.navisdk.model.NaviDataEngine;
import com.baidu.navisdk.model.RoutePlanModel;
import com.baidu.navisdk.model.datastruct.RoutePlanNode;
import com.baidu.navisdk.ui.routeguide.BNavConfig;
import com.baidu.navisdk.ui.routeguide.BNavigator;
import com.baidu.navisdk.util.common.PreferenceHelper;
import com.baidu.navisdk.util.common.ScreenUtil;
import com.baidu.nplatform.comapi.map.MapGLSurfaceView;
import com.tuyou.tsd.common.TSDLocation;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.navigation.mode.SysApplication;

public class RoutePlanActivity extends BaseActivity implements OnClickListener {
	private RoutePlanModel mRoutePlanModel = null;
	private MapGLSurfaceView mMapView = null;
	private Button recommendBtn, avoidCongestionBtn, distanceBtn, tollBtn;
	private TextView timeTextView, distanceTextView;
	private String name, addree, myname, myaddree;
	private Double sX = 0.0, sY = 0.0, eX = 0.0, eY = 0.0;
	private BNaviPoint startPoint, endPoint;
	private ImageView back, navImageView;
	private boolean isNavigation = false;
	private boolean isReal = true;
	private TSDLocation location = null;
	private int routeType;
	private Bundle configParam = null;
	private String source = null;
	/**
	 * 是否进行语音播报
	 */
	public static boolean isPlayTTS = true;
	private String playString = "";
	private boolean isTTS = true;
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// Toast.makeText(context, "语音播报结束", Toast.LENGTH_SHORT).show();
			isTTS = true;
		}
	};
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				BNRoutePlaner.getInstance().zoomToRouteBound();
				break;

			default:
				break;
			}
		};
	};

	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.activity_routeplan);
		SysApplication.getInstance().addActivity(this);
		// 动态注册接收语音播报结束广播
		IntentFilter filter = new IntentFilter();
		filter.addAction("tsd.tts.PLAY_FINISHED");
		registerReceiver(broadcastReceiver, filter);
		initDate(getIntent());
		startPoint = new BNaviPoint(sY, sX, myname,
				BNaviPoint.CoordinateType.BD09_MC);
		endPoint = new BNaviPoint(eY, eX, name,
				BNaviPoint.CoordinateType.BD09_MC);
		// 初始化TTS. 或者也可以使用独立TTS模块，不用使用导航SDK提供的TTS
		BNTTSPlayer.initPlayer();
		// 设置TTS播放回调
		BNavigatorTTSPlayer.setTTSPlayerListener(new IBNTTSPlayerListener() {

			@Override
			public int playTTSText(String arg0, int arg1) {
				if (isPlayTTS) {
					if (arg0.contains("米行驶")) {
						arg0 = arg0.replace("米行驶", "米 行驶");
					}
					if (!playString.equals(arg0)) {
						playString = arg0;
						playBroadcast(arg0, 0);
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
		initView();
		initMapView();
		listener();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		initDate(intent);
		startPoint = new BNaviPoint(sY, sX, myname,
				BNaviPoint.CoordinateType.BD09_MC);
		endPoint = new BNaviPoint(eY, eX, name,
				BNaviPoint.CoordinateType.BD09_MC);
		initMapView();
	}

	public void initDate(Intent intent) {
		source = intent.getStringExtra("source");
		location = SearchService.location;
		// 防止有时定位信息为空崩溃，为空时采用上次缓存的定位信息
		try {
			sX = location.getLatitude();
			sY = location.getLongitude();
			myname = location.getAddrStr();
			myaddree = location.getAddrStr();
		} catch (Exception e) {
			sX = Double.parseDouble(spf.getString("cachelat", "0.0"));
			sY = Double.parseDouble(spf.getString("cachelng", "0.0"));
			myname = spf.getString("cacheaddr",
					getResources().getString(R.string.main_my_location));
			myaddree = spf.getString("cacheaddr",
					getResources().getString(R.string.main_my_location));
		}
		try {
			JSONObject jObject = new JSONObject(
					intent.getStringExtra("destination"));
			eX = Double.parseDouble(jObject.getString("lat"));
			eY = Double.parseDouble(jObject.getString("long"));
			name = jObject.getString("name");
			addree = jObject.getString("addr");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void initView() {
		recommendBtn = (Button) findViewById(R.id.btn_plan_Recommend);
		avoidCongestionBtn = (Button) findViewById(R.id.btn_plan_avoid_congestion);
		distanceBtn = (Button) findViewById(R.id.btn_plan_min_distance);
		tollBtn = (Button) findViewById(R.id.btn_plan_min_toll);
		back = (ImageView) findViewById(R.id.img_plan_back);
		navImageView = (ImageView) findViewById(R.id.img_plan_nav);
		timeTextView = (TextView) findViewById(R.id.txt_plan_time);
		distanceTextView = (TextView) findViewById(R.id.txt_plan_distance);
		routeType = NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME;
		select(0);
	}

	private void initMapView() {
		if (Build.VERSION.SDK_INT < 14) {
			BaiduNaviManager.getInstance().destroyNMapView();
		}
		mMapView = BaiduNaviManager.getInstance().createNMapView(this);
		BNMapController.getInstance().setDrawNaviLogo(false);
		BNMapController.getInstance().setLevel(19);
		BNMapController.getInstance().setLayerMode(
				LayerMode.MAP_LAYER_MODE_BROWSE_MAP);
		String strLat = spf.getString("cachelat", "26.081941");
		String strLng = spf.getString("cachelng", "119.310718");
		double lat = Double.parseDouble(strLat);
		double lng = Double.parseDouble(strLng);
		BNMapController.getInstance().locateWithAnimation((int) (lng * 1e5),
				(int) (lat * 1e5));
		updateCompassPosition();
	}

	private void listener() {
		back.setOnClickListener(this);
		navImageView.setOnClickListener(this);
		recommendBtn.setOnClickListener(this);
		avoidCongestionBtn.setOnClickListener(this);
		distanceBtn.setOnClickListener(this);
		tollBtn.setOnClickListener(this);
	}

	@Override
	public void onDestroy() {
		BNavigator.destory();
		BNRoutePlaner.getInstance().setObserver(null);
		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
		}
		stopBroadcast();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		BNRoutePlaner.getInstance().setRouteResultObserver(null);
		((ViewGroup) (findViewById(R.id.mapview_layout))).removeAllViews();
		BNMapController.getInstance().onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView = BaiduNaviManager.getInstance().createNMapView(this);
		((ViewGroup) (findViewById(R.id.mapview_layout))).addView(mMapView);
		BNMapController.getInstance().onResume();
		routePlan(isReal);
	}

	/**
	 * 更新指南针位置
	 */
	private void updateCompassPosition() {
		// int screenW = this.getResources().getDisplayMetrics().widthPixels;
		BNMapController.getInstance().resetCompassPosition(
				ScreenUtil.dip2px(this, 30), ScreenUtil.dip2px(this, 100), -1);
	}

	public void routePlan(boolean isReal) {
		BaiduNaviManager.getInstance().launchNavigator(RoutePlanActivity.this,
				startPoint, endPoint, routeType, isReal,
				BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY,
				new OnStartNavigationListener() {

					@Override
					public void onJumpToNavigator(Bundle configParams) {
						configParam = configParams;
						configParam.putInt(BNavConfig.KEY_ROUTEGUIDE_MENU_TYPE,
								BNavConfig.pRGMenuType);
						BaiduNaviManager.getInstance()
								.dismissWaitProgressDialog();
						BNMapController.getInstance().setLayerMode(
								LayerMode.MAP_LAYER_MODE_ROUTE_DETAIL);
						mRoutePlanModel = (RoutePlanModel) NaviDataEngine
								.getInstance().getModel(ModelName.ROUTE_PLAN);
						LogUtil.d(
								"RoutePlanActivity",
								"时间：" + mRoutePlanModel.getTotalTime()
										+ "   距离："
										+ mRoutePlanModel.getDistance()
										+ "   路段："
										+ mRoutePlanModel.getFirstRoadName()
										+ "   红路灯数："
										+ mRoutePlanModel.getNodeNum());
						timeTextView.setText(mRoutePlanModel.getTotalTime());
						distanceTextView.setText(mRoutePlanModel.getDistance());
						if (isNavigation) {
							isNavigation = false;
							PreferenceHelper.getInstance(
									getApplicationContext()).putBoolean(
									SettingParams.Key.SP_TRACK_LOCATE_GUIDE,
									false);
							startNavi(configParam);
						}
						// 解决百度抽风不把地图移到中间的bug
						Timer timer = new Timer();
						timer.schedule(new TimerTask() {

							@Override
							public void run() {
								handler.sendEmptyMessage(0);
							}
						}, 500);
					}

					@Override
					public void onJumpToDownloader() {

					}
				});
	}

	/**
	 * 开始导航
	 * 
	 * @param configParams
	 */
	private void startNavi(Bundle configParams) {
		if (mRoutePlanModel == null) {
			Toast.makeText(this, "请先算路！", Toast.LENGTH_LONG).show();
			return;
		}
		// 获取路线规划结果起点
		RoutePlanNode startNode = mRoutePlanModel.getStartNode();
		// 获取路线规划结果终点
		RoutePlanNode endNode = mRoutePlanModel.getEndNode();
		if (null == startNode || null == endNode) {
			return;
		}
		// 获取路线规划算路模式
		routeType = BNRoutePlaner.getInstance().getCalcMode();
		Intent intent = new Intent(RoutePlanActivity.this,
				BNavigatorActivity.class);
		intent.putExtras(configParams);
		startActivity(intent);
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_plan_Recommend:
			select(0);
			routeType = NE_RoutePlan_Mode.ROUTE_PLAN_MOD_RECOMMEND;
			routePlan(isReal);
			break;
		case R.id.btn_plan_avoid_congestion:
			select(1);
			routeType = NE_RoutePlan_Mode.ROUTE_PLAN_MOD_AVOID_TAFFICJAM;
			routePlan(isReal);
			break;
		case R.id.btn_plan_min_distance:
			select(2);
			routeType = NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_DIST;
			routePlan(isReal);
			break;
		case R.id.btn_plan_min_toll:
			select(3);
			routeType = NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TOLL;
			routePlan(isReal);
			break;
		case R.id.img_plan_back:
			if (null == source) {
				Intent i = new Intent();
				i.setClass(RoutePlanActivity.this, RouteResultsActivity.class);
				i.putExtra("result", SearchService.poiJson.toString());
				startActivity(i);
			}
			finish();
			break;
		case R.id.img_plan_nav:
			startNavi(configParam);
			break;
		default:
			break;
		}
	}

	/**
	 * 设置选中的效果
	 * 
	 * @param i
	 */
	@SuppressWarnings("deprecation")
	public void select(int i) {
		recommendBtn.setBackgroundColor(getResources().getColor(
				android.R.color.transparent));
		avoidCongestionBtn.setBackgroundColor(getResources().getColor(
				android.R.color.transparent));
		distanceBtn.setBackgroundColor(getResources().getColor(
				android.R.color.transparent));
		tollBtn.setBackgroundColor(getResources().getColor(
				android.R.color.transparent));
		switch (i) {
		case 0:
			recommendBtn.setBackgroundResource(R.drawable.bg_plan_select_type);
			break;
		case 1:
			avoidCongestionBtn
					.setBackgroundResource(R.drawable.bg_plan_select_type);
			break;
		case 2:
			distanceBtn.setBackgroundResource(R.drawable.bg_plan_select_type);
			break;
		case 3:
			tollBtn.setBackgroundResource(R.drawable.bg_plan_select_type);
			break;

		default:
			break;
		}
	}
}

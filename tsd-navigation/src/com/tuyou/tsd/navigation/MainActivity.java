package com.tuyou.tsd.navigation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.TSDLocation;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.navigation.mode.PeripheryAdapter;
import com.tuyou.tsd.navigation.mode.SysApplication;
import com.tuyou.tsd.navigation.mode.WaitDialog;
import com.tuyou.tsd.navigation.mode.hisPoiInfo;

public class MainActivity extends SleepBaseActivity implements
		BaiduMap.OnMapClickListener, BaiduMap.OnMapLongClickListener,
		OnGetGeoCoderResultListener, OnClickListener,
		BaiduMap.OnMapLoadedCallback, BaiduMap.OnMapTouchListener {
	public static String LOCATION_ACTION = "location";
	private MapView mMapView = null;
	// 目的地类型、自己位置类型、城市类型
	private final int SEARCH_DES_NEARBY = 2, SEARCH_MY_NEARBY = 1,
			SEARCH_CITY = 0;
	private int peripheryType = SEARCH_MY_NEARBY;
	private BaiduMap mBaiduMap;
	// 定位相关
	private TSDLocation location;
	private LocationMode mCurrentMode;
	public MyLocationListener mMyLocationListener;
	boolean isSearch = false, isFirLoc = true, isNormal = true,
			islocation = false, isPoi = false;
	private DecimalFormat df = new DecimalFormat("0.0");
	private ImageView seachImageView, locationImageView, zoomImageView,
			narrowImageView, tckzImageView, sslkImageView;
	private EditText contentEditText;
	private Button hisBtn, peripheryBtn;
	// 缩放级别
	private int zoom = 17;
	private WaitDialog dialog;
	private WaitDialog popDialog;
	private GeoCoder mSearch = null; // 搜索模块
	private TextView addrTextView, disTextView, circleTextView;
	private String pickAddr, poiName;
	private LatLng pickLatLng;
	private InfoWindow mInfoWindow;
	private String[] poiHot;
	private List<String> hisList = new ArrayList<String>();
	private List<hisPoiInfo> list = new ArrayList<hisPoiInfo>();
	private List<String> list2 = new ArrayList<String>();
	private boolean isCtrl = false;
	private String TAG = "MainActivity";
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				System.out.println("isCtrl = " + isCtrl);
				if (!isCtrl) {
					isCtrl = true;
					LogUtil.d(TAG, "send:"
							+ TSDEvent.Interaction.RUN_INTERACTION);
					Intent intent = new Intent(
							TSDEvent.Interaction.RUN_INTERACTION);
					intent.putExtra("template", "DEST_QUERY");
					sendBroadcast(intent);
				}
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		SysApplication.getInstance().addActivity(this);
		mCurrentMode = LocationMode.NORMAL;
		init();
		listenter();
		if (!isCtrl) {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					handler.sendEmptyMessage(0);
				}
			}, 2000);
		}
	}

	public void search(String content) {
		contentEditText.setText("");
		if (!isSearch) {
			if (location == null) {
				Toast toast = Toast.makeText(MainActivity.this, getResources()
						.getString(R.string.searchservice_no_location),
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 40);
				toast.show();
				return;
			}
			if (null == content || content.length() == 0) {
				Toast toast = Toast.makeText(MainActivity.this, getResources()
						.getString(R.string.main_enter_destination),
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 40);
				toast.show();
				return;
			}
			Intent intent = new Intent(TSDEvent.Navigation.POI_SEACH);
			intent.putExtra("searchPOI", content);
			intent.putExtra("type", SEARCH_CITY);
			sendBroadcast(intent);
			isSearch = true;
			waitDialog(null);
		} else {
			Toast toast = Toast.makeText(MainActivity.this, getResources()
					.getString(R.string.main_searching), Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 40);
			toast.show();
		}

	}

	private void listenter() {
		zoomImageView.setOnClickListener(this);
		tckzImageView.setOnClickListener(this);
		sslkImageView.setOnClickListener(this);
		narrowImageView.setOnClickListener(this);
		seachImageView.setOnClickListener(this);
		peripheryBtn.setOnClickListener(this);
		locationImageView.setOnClickListener(this);
		hisBtn.setOnClickListener(this);
		contentEditText
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							search(contentEditText.getText().toString().trim());
						}
						return false;
					}
				});
	}

	private void init() {
		// 启动服务
		startService(new Intent(MainActivity.this, SearchService.class));
		IntentFilter filter = new IntentFilter();
		filter.addAction(TSDEvent.Navigation.POI_SEARCH_RESULT);
		filter.addAction(TSDEvent.System.LOCATION_UPDATED);
		registerReceiver(myReceiver, filter);
		hisBtn = (Button) findViewById(R.id.btn_main_his);
		zoomImageView = (ImageView) findViewById(R.id.img_map_zoom);
		tckzImageView = (ImageView) findViewById(R.id.img_main_mode);
		sslkImageView = (ImageView) findViewById(R.id.img_main_traffic);
		narrowImageView = (ImageView) findViewById(R.id.img_map_narrow);
		seachImageView = (ImageView) findViewById(R.id.img_main_seach);
		peripheryBtn = (Button) findViewById(R.id.btn_main_periphery);
		locationImageView = (ImageView) findViewById(R.id.img_main_location);
		contentEditText = (EditText) findViewById(R.id.edt_main_seach_content);
		mMapView = (MapView) findViewById(R.id.mapview_main_map);
		addrTextView = (TextView) findViewById(R.id.txt_main_addr_name);
		disTextView = (TextView) findViewById(R.id.txt_main_addr_distance);
		circleTextView = (TextView) findViewById(R.id.txt_main_addr_circle);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMyLocationEnabled(true);
		mMapView.showZoomControls(false); // 设置启用内置的缩放控件
		mBaiduMap.setMapStatus(MapStatusUpdateFactory
				.newMapStatus(new MapStatus.Builder().zoom(zoom).build()));// 设置缩放级别
		String strLat = spf.getString("cachelat", "26.081941");
		String strLng = spf.getString("cachelng", "119.310718");
		double lat = Double.parseDouble(strLat);
		double lng = Double.parseDouble(strLng);
		LatLng ll = new LatLng(lat, lng);// 默认移到上次定位位置
		// LatLng ll = new LatLng(26.081941, 119.310718);// 默认移到福州信息广场
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);
		// 初始化默认为3D地图
		perfomOverlook(false);
		// 地图点击事件处理
		mBaiduMap.setOnMapClickListener(this);
		mBaiduMap.setOnMapLoadedCallback(this);
		mBaiduMap.setOnMapLongClickListener(this);
		mBaiduMap.setOnMapTouchListener(this);
		// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
		poiHot = getResources().getStringArray(R.array.poiHot);
		for (int i = 0; i < poiHot.length; i++) {
			list2.add(poiHot[i]);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myReceiver != null) {
			unregisterReceiver(myReceiver);
		}
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
		mMapView = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		isCtrl = true;
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	@Override
	public void onMapClick(LatLng arg0) {
		mBaiduMap.clear();
	}

	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		String[] temp = arg0.getName().split("\\\\");
		if (temp.length > 1) {
			poiName = temp[0] + temp[1];
		} else {
			poiName = arg0.getName();
		}
		// poiName = pbString(poiName);
		isPoi = true;
		// 反Geo搜索
		mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(arg0
				.getPosition()));
		return true;
	}

	public String pbString(String s) {
		String string = "";
		if (s.length() > 16) {
			string = s.substring(0, 15) + "...";
		} else {
			string = s;
		}
		return string;
	}

	/**
	 * 实现实位回调监听
	 */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// 异常捕获是为防止dialog出现莫名的错误
			try {
				if (dialog != null && !isSearch) {
					dialog.dismiss();
				}
			} catch (Exception e) {
				LogUtil.d("MainActivity", "对话框出错");
			}

		}
	}

	public void waitDialog(String str) {
		LayoutInflater inflater = MainActivity.this.getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_wait, null);
		if (str != null) {
			TextView textView = (TextView) layout
					.findViewById(R.id.txt_dialog_content);
			textView.setText(str);
		}
		layout.findViewById(R.id.img_dialog_off).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (isSearch) {
							stopService(new Intent(MainActivity.this,
									SearchService.class));
							isSearch = false;
						}
						dialog.dismiss();
					}
				});
		dialog = new WaitDialog(MainActivity.this, layout);
		dialog.show();
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
	}

	private BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.d(TAG, "action = " + action);
			if (action.equals(TSDEvent.System.LOCATION_UPDATED)) {
				location = intent.getParcelableExtra("location");
				// map view 销毁后不在处理新接收的位置
				if (location == null || mMapView == null) {
					return;
				}
				MyLocationData locData = new MyLocationData.Builder()
						.accuracy(location.getRadius())
						.latitude(location.getLatitude())
						.longitude(location.getLongitude()).build();
				mBaiduMap.setMyLocationData(locData);
				mBaiduMap
						.setMyLocationConfigeration(new MyLocationConfiguration(
								mCurrentMode, true, null));
				if (isFirLoc) {
					isFirLoc = false;
					LatLng ll = new LatLng(location.getLatitude(),
							location.getLongitude());
					MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
					mBaiduMap.animateMapStatus(u);
				}
			} else if (action.equals(TSDEvent.Navigation.POI_SEARCH_RESULT)) {
				if (dialog != null) {
					dialog.dismiss();
					isSearch = false;
				}
				try {
					String jsonString = intent.getStringExtra("result");
					JSONObject jsonObject = new JSONObject(jsonString);
					String result = jsonObject.getString("result");
					if (result.equals("ok")) {
						if (isTopActivity()) {
							contentEditText.setText("");
							Intent i = new Intent();
							i.setClass(MainActivity.this,
									RouteResultsActivity.class);
							i.putExtra("result",
									intent.getStringExtra("result"));
							startActivity(i);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	};

	@Override
	public void onMapLongClick(LatLng arg0) {
		isPoi = false;
		// 反Geo搜索
		mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(arg0));
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {

	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast toast = Toast.makeText(MainActivity.this, getResources()
					.getString(R.string.main_no_result), Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 40);
			toast.show();
			return;
		}
		if (location == null) {
			Toast toast = Toast.makeText(MainActivity.this, getResources()
					.getString(R.string.main_location_fail), Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 40);
			toast.show();
			return;
		}
		mBaiduMap.clear();
		// 构建Marker图标
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_addr);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().position(
				result.getLocation()).icon(bitmap);
		// 在地图上添加Marker，并显示
		mBaiduMap.addOverlay(option);
		pickLatLng = result.getLocation();
		pickAddr = result.getAddressDetail().city
				+ result.getAddressDetail().province
				+ result.getAddressDetail().district
				+ result.getAddressDetail().street
				+ result.getAddressDetail().streetNumber;
		addrTextView.setText(pickAddr);
		disTextView
				.setText(df.format(DistanceUtil.getDistance(
						new LatLng(location.getLatitude(), location
								.getLongitude()), pickLatLng) / 1000)
						+ "千米");
		circleTextView.setText(result.getBusinessCircle());
		if (!isPoi) {
			poiName = result.getAddress();
		}
		String showPoiName = pbString(poiName);
		showPOI(result.getLocation(), showPoiName);
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(result
				.getLocation());
		mBaiduMap.animateMapStatus(u);
	}

	@Override
	public void onClick(View v) {
		isCtrl = true;
		switch (v.getId()) {
		case R.id.img_map_zoom:
			if (zoom < 19) {
				zoom += 1;
				mBaiduMap.setMapStatus(MapStatusUpdateFactory
						.newMapStatus(new MapStatus.Builder().zoom(zoom)
								.build()));// 设置缩放级别
			}
			break;
		case R.id.btn_main_his:
			hisBtn.setBackgroundResource(R.drawable.bg_mian_his_2);
			hisDialog();
			break;
		case R.id.img_main_mode:
			if (islocation) {
				tckzImageView.setImageResource(R.drawable.img_main_car);
				mylocation();
			} else {
				if (isNormal) {
					tckzImageView.setImageResource(R.drawable.img_main_puble);
					mCurrentMode = LocationMode.COMPASS;
					isNormal = false;
				} else {
					tckzImageView.setImageResource(R.drawable.img_main_car);
					mCurrentMode = LocationMode.NORMAL;
					isNormal = true;
				}
				mBaiduMap
						.setMyLocationConfigeration(new MyLocationConfiguration(
								mCurrentMode, true, null));
			}
			break;
		case R.id.img_main_traffic:
			boolean isSelect = !mBaiduMap.isTrafficEnabled();
			if (isSelect) {
				sslkImageView.setImageResource(R.drawable.img_main_lk_2);
			} else {
				sslkImageView.setImageResource(R.drawable.img_main_lk_1);
			}

			mBaiduMap.setTrafficEnabled(!mBaiduMap.isTrafficEnabled());
			break;
		case R.id.img_map_narrow:
			if (zoom > 3) {
				zoom -= 1;
				mBaiduMap.setMapStatus(MapStatusUpdateFactory
						.newMapStatus(new MapStatus.Builder().zoom(zoom)
								.build()));// 设置缩放级别
			}
			break;
		case R.id.img_main_seach:
			search(contentEditText.getText().toString().trim());
			break;
		case R.id.btn_main_periphery:
			peripheryBtn.setBackgroundResource(R.drawable.bg_main_periphery_2);
			peripheryType = SEARCH_MY_NEARBY;
			peripheryDialog();
			break;
		case R.id.img_main_location:
			mylocation();
			break;
		case R.id.btn_poi_nav:
			if (location != null) {
				changeDate(poiName, pickLatLng.latitude, pickLatLng.longitude);
				jumpLine();
			} else {
				Toast.makeText(MainActivity.this, "请先定位", Toast.LENGTH_LONG)
						.show();
			}

			break;
		case R.id.btn_poi_periphery:
			peripheryType = SEARCH_DES_NEARBY;
			peripheryDialog();
			break;
		default:
			break;
		}
	}

	/**
	 * 跳转到路线规划页面
	 */
	public void jumpLine() {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("name", poiName);
			jsonObject.put("addr", pickAddr);
			jsonObject.put("lat", pickLatLng.latitude + "");
			jsonObject.put("long", pickLatLng.longitude + "");
			Intent lineIntent = new Intent(MainActivity.this,
					RoutePlanActivity.class);
			lineIntent.putExtra("destination", jsonObject.toString());
			lineIntent.putExtra("source", "map");
			startActivity(lineIntent);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMapLoaded() {
		mBaiduMap.getUiSettings().setCompassEnabled(true);
		mBaiduMap.getUiSettings().setCompassPosition(new Point(40, 140));
	}

	/**
	 * 允许楼块效果
	 */
	private void perfomOverlook(boolean is3D) {
		try {
			mBaiduMap.setBuildingsEnabled(is3D);
			// 设置地图俯视角度
			// MapStatus ms = new MapStatus.Builder(mBaiduMap.getMapStatus())
			// .overlook(angle).build();
			// MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(ms);
			// mBaiduMap.animateMapStatus(u);
		} catch (NumberFormatException e) {
			Toast.makeText(this, "请输入正确的俯角", Toast.LENGTH_SHORT).show();
		}
	}

	public void showPOI(LatLng ll, String name) {
		LayoutInflater mInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflater.inflate(R.layout.layout_poi_info, null);
		TextView nameTextView = (TextView) view.findViewById(R.id.txt_poi_name);
		nameTextView.setText(name);
		view.findViewById(R.id.btn_poi_nav).setOnClickListener(this);
		view.findViewById(R.id.btn_poi_periphery).setOnClickListener(this);
		mInfoWindow = new InfoWindow(view, ll, -47);
		mBaiduMap.showInfoWindow(mInfoWindow);
	}

	@Override
	public void onTouch(MotionEvent arg0) {
		isCtrl = true;
		tckzImageView.setImageResource(R.drawable.img_main_location);
		if (!isNormal) {
			mCurrentMode = LocationMode.NORMAL;
			isNormal = true;
			mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
					mCurrentMode, true, null));
		}
		islocation = true;
	}

	public void mylocation() {
		if (location != null) {
			LatLng ll = new LatLng(location.getLatitude(),
					location.getLongitude());
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
			mBaiduMap.animateMapStatus(u);
		} else {
			Toast toast = Toast.makeText(MainActivity.this, getResources()
					.getString(R.string.main_location_fail), Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 40);
			toast.show();
		}
		islocation = false;
	}

	public void peripheryDialog() {
		if (popDialog != null) {
			popDialog.dismiss();
			hisBtn.setBackgroundResource(R.drawable.bg_mian_his_1);
		}
		LayoutInflater inflater = MainActivity.this.getLayoutInflater();
		View layout = inflater.inflate(R.layout.layout_periphery, null);
		keyListener(layout);
		GridView gridview = (GridView) layout
				.findViewById(R.id.gridView_periphery);
		LinearLayout perLayout = (LinearLayout) layout
				.findViewById(R.id.layout_periphery_connect);
		PeripheryAdapter adapter = new PeripheryAdapter(MainActivity.this,
				list2);
		// 添加并且显示
		gridview.setAdapter(adapter);
		gridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				popDialog.dismiss();
				peripheryBtn
						.setBackgroundResource(R.drawable.bg_main_periphery_1);
				Intent intent = new Intent("tsd.event.navigation.search_nearby");
				intent.putExtra("poiHot", poiHot[arg2]);
				intent.putExtra("type", peripheryType);
				if (peripheryType == SEARCH_DES_NEARBY) {
					intent.putExtra("latitude", pickLatLng.latitude);
					intent.putExtra("longitude", pickLatLng.longitude);
				}
				sendBroadcast(intent);
				isSearch = true;
				waitDialog(null);
			}
		});
		layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				popDialog.dismiss();
				peripheryBtn
						.setBackgroundResource(R.drawable.bg_main_periphery_1);
			}
		});
		Animation translateIn = new TranslateAnimation(0, 0, 500, 0);
		translateIn.setDuration(500);
		perLayout.startAnimation(translateIn);
		popDialog = new WaitDialog(MainActivity.this, layout,
				R.style.Dialog_Fullscreen);
		popDialog.show();
	}

	/**
	 * 展开历史记录对话框
	 */
	public void hisDialog() {
		if (popDialog != null) {
			popDialog.dismiss();
			peripheryBtn.setBackgroundResource(R.drawable.bg_main_periphery_1);
		}
		hisList.clear();
		list.clear();
		LayoutInflater inflater = MainActivity.this.getLayoutInflater();
		View layout = inflater.inflate(R.layout.layout_his, null);
		keyListener(layout);
		LinearLayout connect = (LinearLayout) layout
				.findViewById(R.id.layout_his_content);
		ListView listView = (ListView) layout.findViewById(R.id.list_his_date);
		int size = spf.getInt("size", 0);
		for (int i = 0; i < size; i++) {
			String name = spf.getString("his" + i, null);
			double latitude = Double.parseDouble(spf.getString("latitude" + i,
					null));
			double longitude = Double.parseDouble(spf.getString(
					"longitude" + i, null));
			hisList.add(name);
			hisPoiInfo hInfo = new hisPoiInfo();
			hInfo.setName(spf.getString("his" + i, null));
			hInfo.setLatitude(latitude);
			hInfo.setLongitude(longitude);
			list.add(hInfo);
		}
		if (list.size() == 0) {
			hisBtn.setBackgroundResource(R.drawable.bg_mian_his_1);
			stopBroadcast();
			playBroadcast(getResources().getString(R.string.main_no_his), 0);
		} else {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					MainActivity.this, R.layout.item_his,
					R.id.txt_item_his_name, hisList);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					if (location != null) {
						if (0.0 == list.get(arg2).getLatitude()
								&& 0.0 == list.get(arg2).getLongitude()) {
							search(list.get(arg2).getName());
						} else {
							double lat = list.get(arg2).getLatitude();
							double lng = list.get(arg2).getLongitude();
							changeDate(list.get(arg2).getName(), lat, lng);
							try {
								JSONObject jsonObject = new JSONObject();
								jsonObject
										.put("name", list.get(arg2).getName());
								jsonObject
										.put("addr", list.get(arg2).getName());
								jsonObject.put("lat", lat);
								jsonObject.put("long", lng);
								Intent lineIntent = new Intent(
										MainActivity.this,
										RoutePlanActivity.class);
								lineIntent.putExtra("destination",
										jsonObject.toString());
								lineIntent.putExtra("source", "map");
								startActivity(lineIntent);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					} else {
						playBroadcast("没有定位信息，请先定位", 0);
					}
					popDialog.dismiss();
					hisBtn.setBackgroundResource(R.drawable.bg_mian_his_1);
				}
			});
			layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					popDialog.dismiss();
					hisBtn.setBackgroundResource(R.drawable.bg_mian_his_1);
				}
			});
			Animation translateIn = new TranslateAnimation(0, 0, 500, 0);
			translateIn.setDuration(500);
			connect.startAnimation(translateIn);
			popDialog = new WaitDialog(MainActivity.this, layout,
					R.style.Dialog_Fullscreen);
			popDialog.show();
		}
	}

	public void initDate() {
		hisList.clear();
		list.clear();
		int size = spf.getInt("size", 0);
		for (int i = 0; i < size; i++) {
			String name = spf.getString("his" + i, null);
			double latitude = Double.parseDouble(spf.getString("latitude" + i,
					"0.0"));
			double longitude = Double.parseDouble(spf.getString(
					"longitude" + i, "0.0"));
			hisList.add(name);
			hisPoiInfo hInfo = new hisPoiInfo();
			hInfo.setName(spf.getString("his" + i, null));
			hInfo.setLatitude(latitude);
			hInfo.setLongitude(longitude);
			list.add(hInfo);
		}
	}

	/**
	 * 把content保存到历史数据里面
	 * 
	 * @param content
	 *            要保存的内容
	 */
	public void changeDate(String name, double latitude, double longitude) {
		initDate();
		int size = hisList.size();
		for (int i = 0; i < size; i++) {
			if (hisList.get(i).equals(name)) {
				hisList.remove(i);
				list.remove(i);
				break;
			}
		}
		hisList.add(0, name);
		hisPoiInfo hInfo = new hisPoiInfo();
		hInfo.setLatitude(latitude);
		hInfo.setLongitude(longitude);
		hInfo.setName(name);
		list.add(0, hInfo);
		if (hisList.size() > 10) {
			hisList.remove(hisList.size() - 1);
			list.remove(list.size() - 1);
		}
		size = hisList.size();
		edt.putInt("size", size);
		for (int i = 0; i < size; i++) {
			edt.putString("his" + i, list.get(i).getName());
			edt.putString("latitude" + i, list.get(i).getLatitude() + "");
			edt.putString("longitude" + i, list.get(i).getLongitude() + "");
		}
		edt.commit();
	}

	private boolean isTopActivity() {
		boolean isTop = false;
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		if (cn.getClassName().contains("MainActivity")) {
			isTop = true;
		}
		return isTop;
	}

	/**
	 * 设置按键监听
	 * 
	 * @param v
	 *            要监听的view
	 */
	public void keyListener(View v) {
		v.setFocusableInTouchMode(true);
		v.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_F4:
					Intent itF1 = new Intent();
					itF1.setAction(TSDEvent.System.HARDKEY4_PRESSED);
					sendBroadcast(itF1);
					break;
				case KeyEvent.KEYCODE_F3:
					Intent itF2 = new Intent();
					itF2.setAction(TSDEvent.System.HARDKEY3_PRESSED);
					sendBroadcast(itF2);
					break;
				case KeyEvent.KEYCODE_F2:
					Intent itF3 = new Intent();
					itF3.setAction(TSDEvent.System.HARDKEY2_PRESSED);
					sendBroadcast(itF3);
					break;
				case KeyEvent.KEYCODE_F1:
					Intent itF4 = new Intent();
					itF4.setAction(TSDEvent.System.HARDKEY1_PRESSED);
					sendBroadcast(itF4);
					break;
				}
				return false;
			}
		});
	}
}

package com.tuyou.tsd.navigation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption.DrivingPolicy;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.tuyou.tsd.common.TSDLocation;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.navigation.mode.SysApplication;
import com.tuyou.tsd.navigation.mode.WaitDialog;

/**
 * GPS导航
 */
public class RouteGuideActivity extends BaseActivity implements
		BaiduMap.OnMapLoadedCallback, OnGetGeoCoderResultListener,
		BaiduMap.OnMapClickListener, BaiduMap.OnMapLongClickListener {
	private double sX = 0, sY = 0, eX = 0, eY = 0;
	private String name, myname;
	private String routePolicies;
	private int routeType;
	// private String addree,myaddree;
	private BNaviPoint startPoint;
	private BNaviPoint endPoint;
	private PlanNode stNode;
	private PlanNode enNode;
	private MapView mMapView = null;
	private BaiduMap mBaiduMap;
	private ImageView back, navigation;
	// private TextView time, distance;
	// private TextView ways, node;
	private RoutePlanSearch mSearch;
	private WaitDialog waitDialog;
	private Bundle bundle;
	private ViewPager viewPager;
	private MyPagerAdapter adapter;
	private List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
	private boolean isNavigation = false;
	private TSDLocation location = null;
	private GeoCoder mGeoSearch = null; // 搜索模块
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int i = msg.what;
			switch (i) {
			case 0:
				loadDialog();
				break;
			case 1:
				if (waitDialog != null) {
					waitDialog.dismiss();
				}
				break;
			default:
				break;
			}
		};
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routeguide);
		SysApplication.getInstance().addActivity(this);
		location = SearchService.location;
		bundle = getIntent().getBundleExtra("bundle");
		isNavigation = bundle.getBoolean("navigation");
		init();
		listener();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		mBaiduMap.clear();
		bundle = intent.getBundleExtra("bundle");
		isNavigation = bundle.getBoolean("navigation");
		sX = location.getLatitude();
		sY = location.getLongitude();
		// sX = Double.parseDouble(bundle.getString("mylatitude"));
		// sY = Double.parseDouble(bundle.getString("mylongitude"));
		eX = Double.parseDouble(bundle.getString("latitude"));
		eY = Double.parseDouble(bundle.getString("longitude"));
		stNode = PlanNode.withLocation(new LatLng(sX, sY));
		enNode = PlanNode.withLocation(new LatLng(eX, eY));
		name = bundle.getString("name");
		// addree = bundle.getString("addr");
		myname = location.getAddrStr();
		// myname = bundle.getString("myname");
		// myaddree = bundle.getString("myaddr");
		startPoint = new BNaviPoint(sY, sX, myname,
				BNaviPoint.CoordinateType.BD09_MC);
		endPoint = new BNaviPoint(eY, eX, name,
				BNaviPoint.CoordinateType.BD09_MC);
		mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode)
				.to(enNode).policy(DrivingPolicy.ECAR_TIME_FIRST));
		waitDialog();
	}

	private void init() {
		sX = location.getLatitude();
		sY = location.getLongitude();
		// sX = Double.parseDouble(bundle.getString("mylatitude"));
		// sY = Double.parseDouble(bundle.getString("mylongitude"));
		eX = Double.parseDouble(bundle.getString("latitude"));
		eY = Double.parseDouble(bundle.getString("longitude"));
		// Log.d("坐标", "sX = " + sX + "   sY=" + sY + "  eX = " + eX +
		// "   eY = "
		// + eY);
		routePolicies = bundle.getString("routePolicies");

		if (null != routePolicies && routePolicies.equals("avoid_tolls")) {
			routeType = NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TOLL;
		} else {
			routeType = NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME;
		}
		name = bundle.getString("name");
		// addree = bundle.getString("addr");
		myname = location.getAddrStr();
		// myname = bundle.getString("myname");
		// myaddree = bundle.getString("myaddr");
		startPoint = new BNaviPoint(sY, sX, myname,
				BNaviPoint.CoordinateType.BD09_MC);
		endPoint = new BNaviPoint(eY, eX, name,
				BNaviPoint.CoordinateType.BD09_MC);
		viewPager = (ViewPager) findViewById(R.id.routeguide_viewpage);
		back = (ImageView) findViewById(R.id.img_guide_back);
		navigation = (ImageView) findViewById(R.id.img_guide_navigation);
		// time = (TextView) findViewById(R.id.txt_guide_time);
		// distance = (TextView) findViewById(R.id.txt_guide_distance);
		// ways = (TextView) findViewById(R.id.txt_guide_ways);
		// node = (TextView) findViewById(R.id.txt_guide_node);
		mMapView = (MapView) findViewById(R.id.mapview_routeguide);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory
				.newMapStatus(new MapStatus.Builder().zoom(17).build()));// 设置缩放级别
		LatLng ll = new LatLng(26.081941, 119.310718);// 默认移到福州信息广场
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);
		mBaiduMap.setOnMapLoadedCallback(this);
		mBaiduMap.setOnMapLongClickListener(this);
		mGeoSearch = GeoCoder.newInstance();
		mGeoSearch.setOnGetGeoCodeResultListener(this);
		mBaiduMap.setOnMapClickListener(this);
		mMapView.showZoomControls(false); // 设置启用内置的缩放控件
		// 初始化搜索模块，注册事件监听
		mSearch = RoutePlanSearch.newInstance();
		mSearch.setOnGetRoutePlanResultListener(listener);
		stNode = PlanNode.withLocation(new LatLng(sX, sY));
		enNode = PlanNode.withLocation(new LatLng(eX, eY));
		// 没有路线规划时允许进行路线规划
		mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode)
				.to(enNode).policy(DrivingPolicy.ECAR_TIME_FIRST));
		waitDialog();
	}

	private void listener() {
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

		navigation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				launchNavigator2();
			}
		});
	}

	/**
	 * 指定导航起终点启动GPS导航.起终点可为多种类型坐标系的地理坐标。 前置条件：导航引擎初始化成功
	 */
	private void launchNavigator2() {
		// 这里给出一个起终点示例，实际应用中可以通过POI检索、外部POI来源等方式获取起终点坐标
		BaiduNaviManager.getInstance().launchNavigator(this, startPoint, // 起点（可指定坐标系）
				endPoint, // 终点（可指定坐标系）
				routeType, // 算路方式,默认时间优先
				true, // 真实导航
				BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY, // 在离线策略
				new OnStartNavigationListener() { // 跳转监听

					@Override
					public void onJumpToNavigator(Bundle configParams) {
						isNavigation = false;
						Intent intent = new Intent(RouteGuideActivity.this,
								BNavigatorActivity.class);
						intent.putExtras(configParams);
						startActivity(intent);
						// finish();
					}

					@Override
					public void onJumpToDownloader() {
					}
				});
	}

	@Override
	protected void onPause() {
		if (mMapView != null) {
			mMapView.onPause();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (mMapView != null) {
			mMapView.onResume();
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (mSearch != null) {
			mSearch.destroy();
		}
		if (mMapView != null) {
			mMapView.onDestroy();
		}
		waitDialog = null;
		super.onDestroy();
	}

	private OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
		public void onGetWalkingRouteResult(WalkingRouteResult result) {
			// 获取步行线路规划结果
		}

		public void onGetTransitRouteResult(TransitRouteResult result) {
			// 获取公交换乘路径规划结果
		}

		public void onGetDrivingRouteResult(DrivingRouteResult result) {
			if (waitDialog != null) {
				waitDialog.dismiss();
				waitDialog = null;
			}
			// 获取驾车线路规划结果
			if (result == null
					|| result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
				Toast.makeText(RouteGuideActivity.this, "抱歉，未找到线路",
						Toast.LENGTH_SHORT).show();
			}
			if (result.error == SearchResult.ERRORNO.NO_ERROR) {
				if (list.size() == 4) {
					list.clear();
				}
				DrivingRouteLine dline = result.getRouteLines().get(0);
				String strtime = dline.getDuration() / 60 + "分钟";
				DecimalFormat df = new DecimalFormat("0.0");
				String strdis = df.format((double) dline.getDistance() / 1000)
						+ "公里";
				HashMap<String, Object> hashMap = new HashMap<String, Object>();
				hashMap.put("time", strtime);
				hashMap.put("distance", strdis);
				// time.setText(strtime);
				// distance.setText(strdis);
				DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);
				mBaiduMap.setOnMarkerClickListener(overlay);
				overlay.setData(result.getRouteLines().get(0));
				hashMap.put("line", overlay);
				list.add(hashMap);
				System.out.println("list.size() = " + list.size());
				switch (list.size()) {
				case 1:
					overlay.addToMap();
					overlay.zoomToSpan();
					new Thread() {
						public void run() {
							mSearch.drivingSearch((new DrivingRoutePlanOption())
									.from(stNode).to(enNode)
									.policy(DrivingPolicy.ECAR_DIS_FIRST));
						};
					}.start();
					break;
				case 2:
					new Thread() {
						public void run() {
							mSearch.drivingSearch((new DrivingRoutePlanOption())
									.from(stNode).to(enNode)
									.policy(DrivingPolicy.ECAR_FEE_FIRST));
						};
					}.start();
					break;
				case 3:
					new Thread() {
						public void run() {
							mSearch.drivingSearch((new DrivingRoutePlanOption())
									.from(stNode).to(enNode)
									.policy(DrivingPolicy.ECAR_AVOID_JAM));
						};
					}.start();
					break;
				default:
					break;
				}
				adapter = new MyPagerAdapter(list);
				viewPager.setAdapter(adapter);
				viewPager.setCurrentItem(0);
				viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
				if (isNavigation) {
					launchNavigator2();
				}
			}
		}
	};

	/**
	 * 等待对话框
	 */
	public void waitDialog() {
		LayoutInflater inflater = RouteGuideActivity.this.getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_wait, null);
		layout.findViewById(R.id.img_dialog_off).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (waitDialog != null) {
							waitDialog.dismiss();
							waitDialog = null;
						}
					}
				});
		if (waitDialog != null) {
			waitDialog.dismiss();
			waitDialog = null;
		}
		waitDialog = new WaitDialog(RouteGuideActivity.this, layout);
		waitDialog.show();
	}

	/**
	 * 等待对话框
	 */
	public void loadDialog() {
		LayoutInflater inflater = RouteGuideActivity.this.getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_load, null);
		if (waitDialog != null) {
			waitDialog.dismiss();
			waitDialog = null;
		}
		waitDialog = new WaitDialog(RouteGuideActivity.this, layout);
		waitDialog.show();
	}

	@Override
	public void onMapLoaded() {
		mBaiduMap.getUiSettings().setCompassEnabled(true);
		mBaiduMap.getUiSettings().setCompassPosition(new Point(40, 140));
	}

	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		eX = arg0.getPosition().latitude;
		eY = arg0.getPosition().longitude;
		enNode = PlanNode.withLocation(new LatLng(eX, eY));
		name = arg0.getName();
		endPoint = new BNaviPoint(eY, eX, name,
				BNaviPoint.CoordinateType.BD09_MC);
		mBaiduMap.clear();
		mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode)
				.to(enNode).policy(DrivingPolicy.ECAR_TIME_FIRST));
		return true;
	}

	@Override
	public void onMapLongClick(LatLng arg0) {
		eX = arg0.latitude;
		eY = arg0.longitude;
		enNode = PlanNode.withLocation(new LatLng(eX, eY));
		startPoint = new BNaviPoint(sY, sX, myname,
				BNaviPoint.CoordinateType.BD09_MC);
		// 反Geo搜索
		mGeoSearch.reverseGeoCode(new ReverseGeoCodeOption().location(arg0));
	}

	/**
	 * Pager适配器
	 */
	public class MyPagerAdapter extends PagerAdapter {
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		List<View> views = new ArrayList<View>();

		public MyPagerAdapter(List<HashMap<String, Object>> list) {
			this.list = list;
			int a = 1, b = 2;
			String type = null;
			for (int i = 0; i < list.size(); i++) {
				LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = mInflater.inflate(R.layout.layout, null);
				((TextView) view.findViewById(R.id.txt_guide_time))
						.setText(list.get(i).get("time").toString());
				((TextView) view.findViewById(R.id.txt_guide_distance))
						.setText(list.get(i).get("distance").toString());
				switch (i) {
				case 0:
					a = 1;
					b = 2;
					type = "时间最短";
					break;
				case 1:
					a = 2;
					b = 3;
					type = "路程最短";
					break;
				case 2:
					a = 3;
					b = 4;
					type = "费用最少";
					break;
				case 3:
					a = 4;
					type = "躲避拥堵";
					((TextView) view.findViewById(R.id.txt_guide_two))
							.setVisibility(View.GONE);
					break;
				default:
					break;
				}
				((TextView) view.findViewById(R.id.txt_guide_one)).setText(a
						+ "");
				((TextView) view.findViewById(R.id.txt_guide_two)).setText(b
						+ "");
				((TextView) view.findViewById(R.id.txt_guide_recommendation))
						.setText(type);
				view.findViewById(R.id.img_guide_navigation)
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								launchNavigator2();
							}
						});
				views.add(view);
			}
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			ViewPager pViewPager = ((ViewPager) container);
			pViewPager.removeView(views.get(position));
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			ViewPager pViewPager = ((ViewPager) arg0);
			pViewPager.addView(views.get(arg1));
			return views.get(arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}

	/**
	 * 页卡切换监听
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(final int arg0) {
			mBaiduMap.clear();
			try {
				switch (arg0) {
				case 0:
					new Thread() {
						public void run() {
							handler.sendEmptyMessage(0);
							routeType = NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME;
							DrivingRouteOverlay overlay0 = (DrivingRouteOverlay) list
									.get(arg0).get("line");
							overlay0.addToMap();
							overlay0.zoomToSpan();
							handler.sendEmptyMessage(1);
						};
					}.start();

					break;
				case 1:
					new Thread() {
						public void run() {
							handler.sendEmptyMessage(0);
							routeType = NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_DIST;
							DrivingRouteOverlay overlay1 = (DrivingRouteOverlay) list
									.get(arg0).get("line");
							overlay1.addToMap();
							overlay1.zoomToSpan();
							handler.sendEmptyMessage(1);
						};
					}.start();

					break;
				case 2:
					new Thread() {
						public void run() {
							handler.sendEmptyMessage(0);
							routeType = NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TOLL;
							DrivingRouteOverlay overlay2 = (DrivingRouteOverlay) list
									.get(arg0).get("line");
							overlay2.addToMap();
							overlay2.zoomToSpan();
							handler.sendEmptyMessage(1);
						};
					}.start();

					break;
				case 3:
					new Thread() {
						public void run() {
							handler.sendEmptyMessage(0);
							routeType = NE_RoutePlan_Mode.ROUTE_PLAN_MOD_AVOID_TAFFICJAM;
							DrivingRouteOverlay overlay3 = (DrivingRouteOverlay) list
									.get(arg0).get("line");
							overlay3.addToMap();
							overlay3.zoomToSpan();
							handler.sendEmptyMessage(1);
						};
					}.start();
					break;
				}
			} catch (Exception e) {
				Toast.makeText(RouteGuideActivity.this,
						"加载出错" + arg0 + arg0 + arg0, Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult arg0) {

	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
		name = arg0.getAddress();
		endPoint = new BNaviPoint(eY, eX, name,
				BNaviPoint.CoordinateType.BD09_MC);
		mBaiduMap.clear();
		mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode)
				.to(enNode).policy(DrivingPolicy.ECAR_TIME_FIRST));
	}

	@Override
	public void onMapClick(LatLng arg0) {

	}

}

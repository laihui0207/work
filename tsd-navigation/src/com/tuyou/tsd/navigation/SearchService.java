package com.tuyou.tsd.navigation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.utils.DistanceUtil;
import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.TSDLocation;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.navigation.mode.SysApplication;
import com.tuyou.tsd.navigation.mode.hisPoiInfo;

public class SearchService extends Service {
	private MyBinder myBinder = new MyBinder();
	private ArrayList<HashMap<String, String>> lines = new ArrayList<HashMap<String, String>>();
	private String city, strAddr, myAddr, poiHot;
	private double mylatitude, mylongitude;
	private boolean isEarchfinish = false;
	public static String navingAction = "tsd.event.navigation.naving";
	private PoiSearch mPoiSearch;
	private DecimalFormat df = new DecimalFormat("0.0");
	public static TSDLocation location = null;
	private int page = 0;
	private String TAG = "SearchService";
	private int sumPage = 1, radius = 5000, type = 0;
	private int timeOut = 10000;// 超时时间
	public static JSONObject poiJson;
	private JSONArray poiArray;
	private LatLng latLng = null;
	private SharedPreferences spf;
	private Editor edt;
	private int sum = 50;// 每次搜索获取的最多条数
	private List<String> hisList = new ArrayList<String>();
	private List<hisPoiInfo> list = new ArrayList<hisPoiInfo>();
	public static Intent navIntent = null;
	
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.v(TAG, "action = " + action);
			if (action.equals(TSDEvent.System.LOCATION_UPDATED)) {
				location = intent.getParcelableExtra("location");
			} else if (action.equals(TSDEvent.Push.NAV_ADDR)) {
				try {
					SysApplication.getInstance().exit();
					String params = intent.getStringExtra("params");
					JSONObject jsonObject = new JSONObject(params);
					String lat = jsonObject.getString("lat");
					String lng = jsonObject.getString("lng");
					String name = jsonObject.getString("name");
					Intent i = new Intent();
					i.setClass(SearchService.this, RoutePlanActivity.class);
					if (location != null) {
						JSONObject json = new JSONObject();
						json.put("name", name);
						json.put("addr", name);
						json.put("lat", lat);
						json.put("long", lng);
						i.putExtra("destination", json.toString());
						i.putExtra("source", "push");
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(i);
					} else {
						playBroadcast(
								getResources().getString(
										R.string.searchservice_no_location), 0);
					}
					changeDate(name, Double.parseDouble(lat),
							Double.parseDouble(lng));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if (action.equals(TSDEvent.Navigation.POI_SEACH)) {
				seach(intent);
			} else if (action.equals("nav_page")) {
				// 翻页
				page = intent.getIntExtra("page", 0);
				if (page < sumPage) {
					poiSeach(page);
				}
			} else if (action.equals("tsd.event.navigation.search_nearby")) {
				// 周边
				seach(intent);
			} else if (action.equals(TSDEvent.Navigation.START_NAVIGATION)) {
				// 跳转到路线规划页面
				Intent i = new Intent();
				i.setClass(SearchService.this, RoutePlanActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("destination", intent.getStringExtra("destination"));
				startActivity(i);
			} else if (action.equals(navingAction)) {
				// 说明系统中存在这个activity
				if (navIntent != null) {
					LogUtil.d(TAG, "跳转到：BNavigatorActivity");
					// 跳转到导航中页面
					navIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(navIntent);
				}
			}
		}
	};
	@SuppressLint("HandlerLeak")
	private Handler myHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (isEarchfinish) {
				return;
			}
			playBroadcast(
					getResources().getString(R.string.searchservice_time_out),
					0);
			packNoPoi(getResources().getString(R.string.searchservice_time_out));
			pushResult();
		};
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return myBinder;
	}

	public class MyBinder extends Binder {

		public Service getService() {
			return SearchService.this;
		}
	}

	@Override
	public void onCreate() {
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		IntentFilter filter = new IntentFilter();
		filter.addAction(TSDEvent.Push.NAV_ADDR);
		filter.addAction(TSDEvent.System.LOCATION_UPDATED);
		filter.addAction(TSDEvent.Navigation.POI_SEACH);
		filter.addAction("tsd.event.navigation.search_nearby");
		filter.addAction(TSDEvent.Navigation.START_NAVIGATION);
		filter.addAction(navingAction);
		filter.addAction("nav_page");
		registerReceiver(myReceiver, filter);
		// 启动定位服务
		startService(new Intent(SearchService.this, LocationService.class));
		spf = getSharedPreferences("navigator", MODE_WORLD_WRITEABLE);
		edt = spf.edit();
		// initDate();
		// 初始化搜索模块，注册事件监听
		// 实例化PoiSearch对象
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
		super.onCreate();
		// 跳转到该页面初始化导航，该页面为全透明
		Intent i = new Intent();
		i.setClass(SearchService.this, InitNavActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	public void seach(Intent intent) {
		poiHot = "";
		strAddr = "";
		poiJson = new JSONObject();
		poiArray = new JSONArray();
		page = 0;
		if (lines != null) {
			lines.clear();
		} else {
			lines = new ArrayList<HashMap<String, String>>();
		}
		if (location != null) {
			city = location.getCity();
			myAddr = location.getAddrStr();
			mylatitude = location.getLatitude();
			mylongitude = location.getLongitude();
		} else {
			city = spf.getString("cachecity",
					getResources().getString(R.string.cache_city));
			myAddr = spf.getString("cacheaddr",
					getResources().getString(R.string.main_my_location));
			mylatitude = Double.parseDouble(spf.getString("cachelat", "0.0"));
			mylongitude = Double.parseDouble(spf.getString("cachelng", "0.0"));
		}
		type = intent.getIntExtra("type", 0);
		switch (type) {
		case 0:
			strAddr = intent.getStringExtra("searchPOI");
			String strCity = intent.getStringExtra("searchCity");
			if (null != strCity && !strCity.isEmpty()
					&& !strCity.equals(strAddr) && !strCity.equals(city)) {
				strAddr = strCity + strAddr;
			}
			changeDate(strAddr, 0.0, 0.0);
			break;
		case 1:
			// 自己的周边
			poiHot = intent.getStringExtra("poiHot");
			latLng = new LatLng(location.getLatitude(), location.getLongitude());
			break;
		case 2:
			// 目的地的周边
			poiHot = intent.getStringExtra("poiHot");
			double lat = intent.getDoubleExtra("latitude", 0.0);
			double log = intent.getDoubleExtra("longitude", 0.0);
			latLng = new LatLng(lat, log);
			break;
		default:
			break;
		}
		poiSeach(page);
		LogUtil.d(TAG, "strAddr = " + strAddr + "    poiHot = " + poiHot);
	}

	@Override
	public void onDestroy() {
		seachDestroy();
		super.onDestroy();
	}

	public void seachDestroy() {
		if (mPoiSearch != null) {
			mPoiSearch.destroy();
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	public void poiSeach(int page) {
		if (!HelperUtil.isNetworkConnected(SearchService.this)) {
			packNoPoi(getResources().getString(R.string.searchservice_no_net));
			pushResult();
			playBroadcast(
					getResources().getString(R.string.searchservice_no_net), 0);
			return;
		}
		if (type == 0) {
			if (strAddr.length() <= 0) {
				return;
			}
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					myHandler.sendEmptyMessage(1);
				}
			}, timeOut);
			mPoiSearch.searchInCity((new PoiCitySearchOption()).city(city)
					.keyword(strAddr).pageNum(page).pageCapacity(sum));
		} else if (type == 1) {
			if (poiHot.length() <= 0) {
				return;
			}
			zbSeach(page);
		} else if (type == 2) {
			if (poiHot.length() <= 0) {
				return;
			}
			zbSeach(page);
		}
	}

	private OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
		@SuppressWarnings("unchecked")
		public void onGetPoiResult(PoiResult result) {
			LogUtil.d(TAG, "result.error = " + result.error);
			// 获取POI检索结果
			if (result == null
					|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
				playBroadcast(
						getResources().getString(
								R.string.searchservice_no_result), 0);
			}
			if (result.error == SearchResult.ERRORNO.NO_ERROR) {
				sumPage = result.getTotalPageNum();
				lines.clear();
				int inx = 0;
				List<PoiInfo> lists = result.getAllPoi();
				if (lists != null) {
					for (int i = 0; i < lists.size(); i++) {
						if (lists.get(i).location != null) {
							HashMap<String, String> lInfo = new HashMap<String, String>();
							double dis = DistanceUtil.getDistance(new LatLng(
									mylatitude, mylongitude),
									lists.get(i).location);
							lInfo.put("name", lists.get(i).name);
							// 暂时取不到
							lInfo.put("distance", df.format(dis / 1000) + "千米");
							lInfo.put("addr", lists.get(i).address);
							lInfo.put("latitude",
									lists.get(i).location.latitude + "");
							lInfo.put("longitude",
									lists.get(i).location.longitude + "");
							lInfo.put("mylatitude", mylatitude + "");
							lInfo.put("mylongitude", mylongitude + "");
							lInfo.put("myname", "我的位置");
							lInfo.put("myaddr", myAddr);
							if (type == 0) {
								lInfo.put(
										"xsd",
										getSimilarityRatio(lists.get(i).name,
												strAddr) + "");

							} else {
								lInfo.put("dis", dis + "");
							}
							lines.add(inx, lInfo);
						}
					}
				} else {
					// search();
					playBroadcast(
							getResources().getString(
									R.string.searchservice_no_result), 0);
				}
			}
			isEarchfinish = true;
			// 按距离排序
			ComparatorHashMap comparator = new ComparatorHashMap();
			Collections.sort(lines, comparator);
			packPoi();
			pushResult();
		}

		public void onGetPoiDetailResult(PoiDetailResult result) {
			if (result.error != SearchResult.ERRORNO.NO_ERROR) {
				Toast.makeText(SearchService.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(SearchService.this,
						result.getName() + ": " + result.getAddress(),
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * 函数名称 : playBroadcast 功能描述 : 开始播报语音广播 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-8-13 下午1:57:48 修改人：wanghh 描述 ：
	 * 
	 */
	public void playBroadcast(String content, int pid) {
		Intent intent = new Intent();
		intent.setAction(CommonMessage.TTS_PLAY);
		// 要发送的内容
		intent.putExtra("package", getPackageName());
		intent.putExtra("id", pid);
		intent.putExtra("content", content);
		// 发送 一个无序广播
		sendBroadcast(intent);
	}

	public class ComparatorHashMap implements Comparator {

		public int compare(Object arg0, Object arg1) {

			HashMap<String, String> map = (HashMap<String, String>) arg0;
			HashMap<String, String> map2 = (HashMap<String, String>) arg1;
			int flag;
			if (type == 0) {
				Integer num = (int) (Float
						.parseFloat(map.get("xsd").toString()) * 100000);
				Integer num2 = (int) (Float.parseFloat(map2.get("xsd")
						.toString()) * 100000);
				flag = num2.compareTo(num);
			} else {
				Integer num = (int) (Float
						.parseFloat(map.get("dis").toString()));
				Integer num2 = (int) (Float.parseFloat(map2.get("dis")
						.toString()));
				flag = num.compareTo(num2);
			}
			return flag;
		}
	}

	/**
	 * 发送查询结果
	 */
	private void pushResult() {
		if (lines == null || lines.size() == 0) {
			// SearchService.this.sendBroadcast(new
			// Intent(TSDEvent.Audio.RESUME));
		}
		Intent intent = new Intent(TSDEvent.Navigation.POI_SEARCH_RESULT);
		intent.putExtra("result", poiJson.toString());
		SearchService.this.sendBroadcast(intent);
		LogUtil.d(TAG, "send：" + TSDEvent.Navigation.POI_SEARCH_RESULT);
	}

	private int compare(String str, String target) {
		int d[][]; // 矩阵
		int n = str.length();
		int m = target.length();
		int i; // 遍历str的
		int j; // 遍历target的
		char ch1; // str的
		char ch2; // target的
		int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n + 1][m + 1];
		for (i = 0; i <= n; i++) { // 初始化第一列
			d[i][0] = i;
		}

		for (j = 0; j <= m; j++) { // 初始化第一行
			d[0][j] = j;
		}

		for (i = 1; i <= n; i++) { // 遍历str
			ch1 = str.charAt(i - 1);
			// 去匹配target
			for (j = 1; j <= m; j++) {
				ch2 = target.charAt(j - 1);
				if (ch1 == ch2) {
					temp = 0;
				} else {
					temp = 1;
				}

				// 左边+1,上边+1, 左上角+temp取最小
				d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1]
						+ temp);
			}
		}
		return d[n][m];
	}

	private int min(int one, int two, int three) {
		return (one = one < two ? one : two) < three ? one : three;
	}

	/**
	 * 获取两字符串的相似度
	 * 
	 * @param str
	 * @param target
	 * @return
	 */

	public float getSimilarityRatio(String str, String target) {
		return 1 - (float) compare(str, target)
				/ Math.max(str.length(), target.length());
	}

	/**
	 * 分装有结果poi
	 */
	public void packPoi() {
		try {
			List<HashMap<String, String>> poiList = new ArrayList<HashMap<String, String>>();
			if (poiHot.length() > 0) {
				for (int i = 0; i < lines.size(); i++) {
					HashMap<String, String> poiInfo = lines.get(i);
					LatLng poiLatLng = new LatLng(Double.parseDouble(poiInfo
							.get("latitude")), Double.parseDouble(poiInfo
							.get("longitude")));
					double dis = DistanceUtil.getDistance(latLng, poiLatLng);
					if (dis < 6000) {
						LogUtil.d(TAG, "距离:" + dis);
						poiList.add(poiInfo);
					}
				}
			} else {
				poiList = lines;
			}
			poiJson = new JSONObject();
			poiArray = new JSONArray();
			poiJson.put("result", "ok");
			poiJson.put("type", "poi");
			for (HashMap<String, String> poiInfo : poiList) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("name", poiInfo.get("name"));
				jsonObject.put("addr", poiInfo.get("addr"));
				jsonObject.put("lat", poiInfo.get("latitude"));
				jsonObject.put("long", poiInfo.get("longitude"));
				jsonObject.put("distance", poiInfo.get("distance"));
				poiArray.put(jsonObject);
			}
			poiJson.put("data", poiArray);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * 分装没有结果的poi返回结果
	 * 
	 * @param error
	 */
	public void packNoPoi(String error) {
		try {
			poiJson = new JSONObject();
			poiJson.put("result", "error");
			JSONObject errorJson = new JSONObject();
			errorJson.put("detail", error);
			poiJson.put("error", errorJson);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void zbSeach(int page) {
		if (!HelperUtil.isNetworkConnected(SearchService.this)) {
			packNoPoi(getResources().getString(R.string.searchservice_no_net));
			pushResult();
			playBroadcast(
					getResources().getString(R.string.searchservice_no_net), 0);
			return;
		}
		if (poiHot.length() <= 0) {
			return;
		}
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				myHandler.sendEmptyMessage(1);
			}
		}, timeOut);
		PoiNearbySearchOption po = new PoiNearbySearchOption();
		po.keyword(poiHot);
		po.location(latLng);
		LogUtil.d(TAG, "lat:" + latLng.latitude + "   lng:" + latLng.longitude);
		LogUtil.d(TAG, "mylat:" + location.getLatitude() + "   mylng:"
				+ location.getLongitude());
		po.radius(radius);
		po.pageNum(page);
		po.pageCapacity(sum);
		po.sortType(PoiSortType.distance_from_near_to_far);
		mPoiSearch.searchNearby(po);
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
}

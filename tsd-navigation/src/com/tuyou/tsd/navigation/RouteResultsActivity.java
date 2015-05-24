package com.tuyou.tsd.navigation;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.widget.ArrayListAdapter;
import com.tuyou.tsd.navigation.mode.SysApplication;

public class RouteResultsActivity extends BaseActivity implements
		OnScrollListener {
	private ImageButton backButton;
	private ListView linesListView;
	// private ArrayList<HashMap<String, String>> lines;
	private SearchResultAdapter adapter;
	private LinearLayout resoultLayout;
	private boolean isSearch = false;
	private int visibleLastIndex;
	private int page = 0;
	private List<ResultItem> listDate = new ArrayList<RouteResultsActivity.ResultItem>();

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(TSDEvent.Navigation.POI_SEARCH_RESULT)) {
				isSearch = false;
				adapter.notifyDataSetChanged();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routeresults);
		SysApplication.getInstance().addActivity(this);
		try {
			JSONObject jObject = new JSONObject(getIntent().getStringExtra(
					"result"));
			JSONArray jArray = jObject.getJSONArray("data");
			for (int i = 0; i < jArray.length(); i++) {
				ResultItem rItem = new ResultItem();
				JSONObject jsonObject = jArray.getJSONObject(i);
				rItem.addr = jsonObject.getString("addr");
				rItem.name = jsonObject.getString("name");
				rItem.lat = jsonObject.getString("lat");
				rItem.lng = jsonObject.getString("long");
				rItem.distance = jsonObject.getString("distance");
				listDate.add(rItem);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(TSDEvent.Navigation.POI_SEARCH_RESULT);
		registerReceiver(mReceiver, filter);
		init();
		listener();
	}

	private void listener() {
		linesListView.setOnItemSelectedListener(onItemSelectedListener);
		// 由于当前的adpter存在bug所以滑动监听没有效果，改代码为分页查询用的
		linesListView.setOnScrollListener(this); // 添加滑动监听
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void init() {
		backButton = (ImageButton) findViewById(R.id.btn_route_back);
		linesListView = (ListView) findViewById(R.id.list_route_lines);
		resoultLayout = (LinearLayout) findViewById(R.id.layout_result);
		adapter = new SearchResultAdapter(RouteResultsActivity.this,
				R.layout.search_result_item, listDate);
		linesListView.setAdapter(adapter);
	}

	private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			// 当此选中的item的子控件需要获得焦点时
			parent.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			parent.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		}
	};

	/**
	 * 滑动时被调用
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
	}

	/**
	 * 滑动状态改变时被调用
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		int itemsLastIndex = adapter.getCount() - 1; // 数据集最后一项的索引
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
				&& visibleLastIndex == itemsLastIndex) {
			if (isSearch) {
				return;
			}
			isSearch = true;
			page += 1;
			// 如果是自动加载,可以在这里放置异步加载数据的代码
			Log.i("LOADMORE", "page = " + page);
			Intent intent = new Intent("nav_page");
			intent.putExtra("page", page);
			sendBroadcast(intent);
		}
	}

	protected void onDestroy() {
		super.onDestroy();
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
	};

	private class ResultItem {
		String lat;
		String name;
		String addr;
		String distance;
		String lng;
	}

	private static class SearchResultAdapter extends
			ArrayListAdapter<ResultItem> {
		private List<ResultItem> objects = new ArrayList<RouteResultsActivity.ResultItem>();
		private Context context;

		public SearchResultAdapter(Context context, int resource) {
			super(context, resource);
		}

		public SearchResultAdapter(Context context, int resource,
				List<ResultItem> objects) {
			super(context, resource, objects);
			this.objects = objects;
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			TextView tv = (TextView) view
					.findViewById(R.id.result_number_textView);
			TextView tv2 = (TextView) view
					.findViewById(R.id.result_title_textView);
			TextView tv3 = (TextView) view
					.findViewById(R.id.result_subtitle_textView);
			TextView tv4 = (TextView) view
					.findViewById(R.id.result_memo_textView);

			ResultItem item = getItem(position);
			tv.setText(position + 1 + "");
			tv2.setText(item.name);
			tv3.setText(item.addr);
			if (item.distance != null) {
				tv4.setText(item.distance);
			}
			viewListener(view, position);
			return view;
		}

		public void viewListener(View view, final int position) {
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("name", objects.get(position).name);
						jsonObject.put("addr", objects.get(position).addr);
						jsonObject.put("lat", objects.get(position).lat);
						jsonObject.put("long", objects.get(position).lng);
						jsonObject.put("distance",
								objects.get(position).distance);
						// 跳转到路线规划页面
						Intent i = new Intent();
						i.setClass(context, RoutePlanActivity.class);
						i.putExtra("destination", jsonObject.toString());
						context.startActivity(i);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
}

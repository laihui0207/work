package com.tuyou.tsd.cardvr.activitys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tuyou.tsd.cardvr.BaseActivity;
import com.tuyou.tsd.cardvr.R;
import com.tuyou.tsd.cardvr.adapter.GuidePageAdapter;
import com.tuyou.tsd.cardvr.adapter.HorizontalListViewAdapter;
import com.tuyou.tsd.cardvr.bean.VideoInfo;
import com.tuyou.tsd.cardvr.service.IInterfaceService;
import com.tuyou.tsd.cardvr.utils.VideoInfoComparator;

public class DrivingRecordActivity extends BaseActivity implements
		OnClickListener,OnPageChangeListener {
	private ViewPager pager;
	private ArrayList<View> viewList;
	private GuidePageAdapter adapter;
	
	private TextView drivingToday;
	private TextView drivingYesterday;
	private TextView drivingEarlier;
	private ArrayList<TextView> listTextView;
	
	private LinearLayout drivingTitleLayout;
	private int lenght;
	private int fromlenght = 0;
	private ImageView drivingIndexImg;
	
	
	private ArrayList<TreeMap<String,ArrayList<VideoInfo>>> listTotal;
	
	private GataTask task;
	private IInterfaceService countService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.driving_record);

		initView();
	}

	private void initView() {
		this.bindService( new Intent("com.tuyou.tsd.cardvr.service.InterfaceService" ), 
        		this.serviceConnection, BIND_AUTO_CREATE);
		
		initViewText();

		viewList = new ArrayList<View>();// 将要分页显示的View装入数组中  
		pager = (ViewPager) findViewById(R.id.driving_view_pager);
		pager.setOnPageChangeListener(DrivingRecordActivity.this);
		TextView drivingBack = (TextView) findViewById(R.id.back);
		drivingBack.setOnClickListener(this);
		
		drivingTitleLayout = (LinearLayout) findViewById(R.id.driving_title_layout);
		drivingIndexImg = (ImageView) findViewById(R.id.driving_index_img);
		
		LinearLayout drivingEarlierLayout = (LinearLayout) findViewById(R.id.driving_earlier_layout);
		LinearLayout drivingYestardayLayout = (LinearLayout) findViewById(R.id.driving_yesterday_layout);
		LinearLayout drivingTodayLayout = (LinearLayout) findViewById(R.id.driving_today_layout);
		drivingEarlierLayout.setOnClickListener(this);
		drivingYestardayLayout.setOnClickListener(this);
		drivingTodayLayout.setOnClickListener(this);
		drivingEarlierLayout.setVisibility(View.INVISIBLE);
		drivingYestardayLayout.setVisibility(View.INVISIBLE);
		drivingTodayLayout.setVisibility(View.INVISIBLE);
	}
	
	@SuppressLint("HandlerLeak")
	private Handler moveHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				LayoutInflater lf = LayoutInflater.from(DrivingRecordActivity.this); 
				if(listTotal!=null&&listTotal.size()>0){
					int size = listTotal.size();
					for(int i=0;i<size;i++){
						View view = lf.inflate(R.layout.driving_record_view_pager, null);  
						ListView listView = (ListView) view.findViewById(R.id.driving_listview);
						HorizontalListViewAdapter Hadapter = new HorizontalListViewAdapter(DrivingRecordActivity.this,listTotal.get(i),i);
						listView.setAdapter(Hadapter);
						viewList.add(view);
						Set<String> set = listTotal.get(i).keySet();
						List<String> listString = new ArrayList<String>();
						for(Iterator<String> iter = set.iterator(); iter.hasNext();){
							listString.add(iter.next());
						}
						if(size==3&&i==size-1){
							listTextView.get(i).setText("更早");
						}else{
							listTextView.get(i).setText(listString.get(0).substring(5, 10).replace("-", "月")+"日");
						}
						
						drivingTitleLayout.getChildAt(i+1).setVisibility(View.VISIBLE);
					}
					adapter = new GuidePageAdapter(viewList);
					pager.setAdapter(adapter);
					
					lenght = drivingTitleLayout.getWidth()/drivingTitleLayout.getChildCount();
					TranslateAnimation animation = new TranslateAnimation(fromlenght, lenght,0, 0); 
					animation.setDuration(100);
					animation.setFillAfter(true);
					fromlenght = lenght;
					drivingIndexImg.startAnimation(animation);
					drivingIndexImg.setVisibility(View.VISIBLE);
					
				}else{
					RelativeLayout  videoNoneLayout = (RelativeLayout) findViewById(R.id.video_none_layout);
					videoNoneLayout.setVisibility(View.VISIBLE);
					pager.setVisibility(View.INVISIBLE);
					TextView videoNoneMsg = (TextView) findViewById(R.id.video_none_msg);
					videoNoneMsg.setText(R.string.no_all_video);
					TextView videoNoneMessage = (TextView) findViewById(R.id.video_none_message);
					videoNoneMessage.setText(R.string.message_all_video);
				}
				break;
			case 2:
				task = new GataTask();
				task.execute();
				break;
			default:
				break;
			}
		}
		
	};
	
	private void initViewText() {
		drivingToday = (TextView) findViewById(R.id.driving_today);
		drivingYesterday = (TextView) findViewById(R.id.driving_yesterday);
		drivingEarlier = (TextView) findViewById(R.id.driving_earlier);
		listTextView = new ArrayList<TextView>();
		listTextView.add(drivingToday);
		listTextView.add(drivingYesterday);
		listTextView.add(drivingEarlier);
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(event.getAction()==KeyEvent.KEYCODE_BACK){
			onActivityStop();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			onActivityStop();
			finish();
			break;
		case R.id.driving_earlier_layout:
//			if((listToday!=null&&listToday.size()>0)&&(listYesterday!=null&&listYesterday.size()>0)){
//				pager.setCurrentItem(2);
//			}else if((listToday!=null&&listToday.size()>0)&&(listYesterday==null||listYesterday.size()==0)||(listToday==null||listToday.size()==0)&&(listYesterday!=null||listYesterday.size()>0)){
//				pager.setCurrentItem(1);
//			}else{
//				pager.setCurrentItem(0);
//			}
			pager.setCurrentItem(2);
			
			break;
		case R.id.driving_yesterday_layout:
			pager.setCurrentItem(1);
//			if(listToday!=null&&listToday.size()>0){
//				pager.setCurrentItem(1);
//			}else{
//				pager.setCurrentItem(0);
//			}
			break;
		case R.id.driving_today_layout:
			pager.setCurrentItem(0);
			break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}
	
	private void showPoint(int id1,int id2,int id3){
		((TextView)findViewById(id1)).setTextColor(getResources().getColor(R.color.blue));
		((TextView)findViewById(id2)).setTextColor(getResources().getColor(R.color.white));
		((TextView)findViewById(id3)).setTextColor(getResources().getColor(R.color.white));
	}

	@Override
	public void onPageSelected(int arg0) {
		lenght = drivingTitleLayout.getWidth()/drivingTitleLayout.getChildCount();
		switch (arg0) {
		case 0:
			showPoint(R.id.driving_today,R.id.driving_yesterday,R.id.driving_earlier);
			TranslateAnimation animation = new TranslateAnimation(fromlenght, lenght,0, 0); 
			animation.setDuration(100);
			animation.setFillAfter(true);
			fromlenght = lenght;
			drivingIndexImg.startAnimation(animation);
			break;
		case 1:
			showPoint(R.id.driving_yesterday,R.id.driving_today,R.id.driving_earlier);
			TranslateAnimation animation2 = new TranslateAnimation(fromlenght, lenght*2,0, 0); 
			animation2.setDuration(100);
			animation2.setFillAfter(true);
			fromlenght = lenght*2;
			drivingIndexImg.startAnimation(animation2);
			break;
		case 2:
			showPoint(R.id.driving_earlier,R.id.driving_today,R.id.driving_yesterday);
			TranslateAnimation animation3 = new TranslateAnimation(fromlenght, lenght*3,0, 0); 
			animation3.setDuration(100);
			animation3.setFillAfter(true);
			fromlenght = lenght*3;
			drivingIndexImg.startAnimation(animation3);
			break;
		}
	}
	
	class GataTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			if(countService!=null){
				//getData(1);
				//getData(2);
				//getData(3);
				listTotal=getVideoSetAll();
			}
			return null;
		}
		
		private ArrayList<TreeMap<String,ArrayList<VideoInfo>>> getVideoSetAll()
		{
			JSONObject obj= countService.getVideoSets("normal",null, null);;
			
			ArrayList<TreeMap<String,ArrayList<VideoInfo>>> result=null;
			TreeMap<String, ArrayList<VideoInfo>> listDay1 = new TreeMap<String, ArrayList<VideoInfo>>(new Comparator<String>() {
				@Override
				public int compare(String lhs, String rhs) {
					
					return rhs.compareTo(lhs);
				}
			});
			TreeMap<String, ArrayList<VideoInfo>> listDay2 = new TreeMap<String, ArrayList<VideoInfo>>(new Comparator<String>() {
				@Override
				public int compare(String lhs, String rhs) {
					
					return rhs.compareTo(lhs);
				}
			});
			TreeMap<String, ArrayList<VideoInfo>> listDay3 = new TreeMap<String, ArrayList<VideoInfo>>(new Comparator<String>() {
				@Override
				public int compare(String lhs, String rhs) {
					
					return rhs.compareTo(lhs);
				}
			});
			TreeMap<String, ArrayList<VideoInfo>> listAll = new TreeMap<String, ArrayList<VideoInfo>>(new Comparator<String>() {
				@Override
				public int compare(String lhs, String rhs) {
					
					return rhs.compareTo(lhs);
				}
			});
			try {
				JSONArray arrayt = obj.getJSONArray("content");
				int size = arrayt.length();
				if(size<=0)
				{
					return result;
				}
				else
				{
					result=new ArrayList<TreeMap<String,ArrayList<VideoInfo>>>();
				}
				VideoInfoComparator com = new VideoInfoComparator();
				for (int i = 0; i < size; i++) {
					VideoInfo info = new VideoInfo();
					JSONObject objt = arrayt.getJSONObject(i);
					info.setTime(objt.getString("startTime"));
					try {
						JSONArray arrayS = objt.getJSONArray("specials");
						String[] s = new String[arrayS.length()];
						for (int k = 0; k < arrayS.length(); k++) {
							s[k] = arrayS.get(k).toString();
						}
						info.setSpecials(s);
					} catch (Exception e) {
						e.printStackTrace();
					}
					String indexTime = objt.getString("startTime").substring(0, 13)
							+ ":00";
					{
						if (listAll.get(indexTime) != null) {
							listAll.get(indexTime).add(info);
							Collections.sort(listAll.get(indexTime), com);
						} else {
							ArrayList<VideoInfo> value = new ArrayList<VideoInfo>();
							value.add(info);
							listAll.put(indexTime, value);
							Collections.sort(listAll.get(indexTime), com);
						}
					}
				}
//				
				{
					Set<String> keySet = listAll.keySet();
					String dayinit=null;
					TreeMap<String, ArrayList<VideoInfo>> current=listAll;
					for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
						String datetime = it.next();
						String day=datetime.substring(0, 10);
						if(dayinit==null)
						{
							current=listDay1;
							current.put(datetime, listAll.get(datetime));
							result.add(listDay1);
						}
						else
						{
							if(day.equals(dayinit))
							{
								current.put(datetime, listAll.get(datetime));								
							}
							else
							{
								if(current==listDay1)
								{
									current=listDay2;
									current.put(datetime, listAll.get(datetime));
									result.add(listDay2);
								}
								else if(current==listDay2)
								{
									current=listDay3;								
									current.put(datetime, listAll.get(datetime));
									result.add(listDay3);
								}
								else
								{
									current.put(datetime, listAll.get(datetime));
								}
							}
						}
						dayinit=day;					
					}
				}			
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}
		
//		private void getData(int type){
//			String time = null;
//			JSONObject obj= null;
//			if(type==1){
//				time = Tools.getTime(System.currentTimeMillis(),"yyyy-MM-dd");
//				obj = countService.getVideoSets("normal",time+"T00:00:00+08:00", time+"T23:59:59+08:00");
//			}else if(type==2){
//				time = Tools.getTime(System.currentTimeMillis()-24*60*60*1000,"yyyy-MM-dd");
//				obj = countService.getVideoSets("normal",time+"T00:00:00+08:00", time+"T23:59:59+08:00");
//			}else{
//				time = Tools.getTime(System.currentTimeMillis()-2*24*60*60*1000,"yyyy-MM-dd");
//				obj = countService.getVideoSets("normal",null, time+"T23:59:59+08:00");
//			}
//			try {
//				JSONArray arrayt = obj.getJSONArray("content");
//				int size = arrayt.length();
//				for(int i=0;i<size;i++){
//					VideoInfo info = new VideoInfo();
//					JSONObject objt = arrayt.getJSONObject(i);
//					info.setTime(objt.getString("startTime"));
//					try {
//						JSONArray arrayS = objt.getJSONArray("specials");
//						String [] s = new String[arrayS.length()];
//						for(int k=0;k<arrayS.length();k++){
//							s[k] = arrayS.get(k).toString();
//						}
//						info.setSpecials(s);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					String indexTime =objt.getString("startTime").substring(0, 13)+":00";
//					if(type==1){
//						if(listToday.get(indexTime)!=null){
//							listToday.get(indexTime).add(info);
//						}else{
//							ArrayList<VideoInfo> value = new ArrayList<VideoInfo>();
//							value.add(info);
//							listToday.put(indexTime, value);
//						}
//						listTotal.add(listToday);
//					}else if(type==2){
//						if(listYesterday.get(indexTime)!=null){
//							listYesterday.get(indexTime).add(info);
//						}else{
//							ArrayList<VideoInfo> value = new ArrayList<VideoInfo>();
//							value.add(info);
//							listYesterday.put(indexTime, value);
//						}
//						listTotal.add(listYesterday);
//					}else{
//						if(listEarlier.get(indexTime)!=null){
//							listEarlier.get(indexTime).add(info);
//						}else{
//							ArrayList<VideoInfo> value = new ArrayList<VideoInfo>();
//							value.add(info);
//							listEarlier.put(indexTime, value);
//						}
//						listTotal.add(listEarlier);
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		
//		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
//			for(int i=0;i<PAGE_SIZE;i++){
//				if(listTotal.get(i)==null||listTotal.get(i).size()==0){
//					viewList.remove(i);
//					drivingTitleLayout.removeViewAt(i+1);
//					listTotal.remove(i);
//					i--;
//					PAGE_SIZE--;
//				}else{
//					ListView listView = (ListView) viewList.get(i).findViewById(R.id.driving_listview);
//					HorizontalListViewAdapter Hadapter = new HorizontalListViewAdapter(DrivingRecordActivity.this,listTotal.get(i),i);
//					listView.setAdapter(Hadapter);
//				}
//			}
//			adapter = new GuidePageAdapter(viewList);
//			pager.setAdapter(adapter);
			Message msg = new Message();
			msg.what = 1;
			moveHandler.sendMessage(msg);
		}
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		onActivityStop();
		unbindService(serviceConnection);
	}
	
	private void onActivityStop(){
		try {
			task.cancel(true);
			task = null;
			adapter = null;
			pager.setAdapter(null);
			pager = null;
		} catch (Exception e) {
			
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private ServiceConnection serviceConnection = new ServiceConnection() { 
		 
		@Override  
		public void onServiceConnected(ComponentName name, IBinder service) {  
			countService = (IInterfaceService) service; 
			Message msg = new Message();
			msg.what = 2;
			moveHandler.sendMessage(msg);
		} 
		  
		@Override  
		public void onServiceDisconnected(ComponentName name) { 
		   countService = null;  
		} 
}; 
}

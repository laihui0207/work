package com.tuyou.tsd.cardvr;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tuyou.tsd.cardvr.activitys.DrivingRecordActivity;
import com.tuyou.tsd.cardvr.activitys.SettingsActivity;
import com.tuyou.tsd.cardvr.activitys.TroubleActivity;
import com.tuyou.tsd.cardvr.comm.Constant;
import com.tuyou.tsd.cardvr.service.IInterfaceService;
import com.tuyou.tsd.cardvr.service.VideoRec;
import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.TSDEvent;

/**
 * 首页
 * 
 * @date 2014年7月11日 下午3:08:24
 */
public class MainActivity extends BaseActivity implements OnClickListener,OnTouchListener{
	private ImageView home_settings_btn, home_exit_btn;
	private RelativeLayout homeDrivingLayout,homeAccidentLayout,homeRemindLayout;
	private IInterfaceService countService;
	private int count = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_layout);
		initPhotoRecord();
		initView();
		
	}
	
	
	
	private void initPhotoRecord() {
		Intent intent = new Intent(TSDEvent.System.FETCH_CONFIG_INFO);
		sendBroadcast(intent);
	}
	
	

	private void initView() {
		Intent it = new Intent();
		it.setClass(this, VideoRec.class);
		startService(it);
		it.setAction(TSDEvent.CarDVR.START_REC);
		sendBroadcast(it);
		this.bindService( new Intent("com.tuyou.tsd.cardvr.service.InterfaceService" ), this.serviceConnection, BIND_AUTO_CREATE);
		homeDrivingLayout = (RelativeLayout) findViewById(R.id.home_driving_layout);
		homeAccidentLayout = (RelativeLayout)findViewById(R.id.home_accident_layout);
		homeRemindLayout = (RelativeLayout)findViewById(R.id.home_remind_layout);
		
		home_settings_btn = (ImageView) findViewById(R.id.home_settings_btn);
		home_exit_btn = (ImageView) findViewById(R.id.home_exit_btn);

		homeDrivingLayout.setOnClickListener(this);
		homeAccidentLayout.setOnClickListener(this);
		homeRemindLayout.setOnClickListener(this);
		
		homeDrivingLayout.setOnTouchListener(this);
		homeAccidentLayout.setOnTouchListener(this);
		homeRemindLayout.setOnTouchListener(this);
		
		home_settings_btn.setOnClickListener(this);
		home_exit_btn.setOnClickListener(this);
		
		

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(count == 0){
			count++;
		}else{
			if(countService!=null){
				showData();
			}
		}
		
	}
	
	private void quit(){
		finish();
	}
	
	@Override
	public void onBackPressed() {
		quit();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.home_driving_layout:
			// 行车记录仪视频
			startActivity(new Intent(this, DrivingRecordActivity.class));
			break;
		case R.id.home_accident_layout:
			// 事故视频
			Intent itAccidents = new Intent(this, TroubleActivity.class);
			itAccidents.putExtra(Constant.TROUBLE_PATH, TSDConst.CAR_DVR_ACCIDENT_VIDEO_PATH);
			itAccidents.putExtra("isAccidents", true);
			startActivity(itAccidents);
			break;
		case R.id.home_remind_layout:
			// 防刮蹭提醒
			Intent it = new Intent(this, TroubleActivity.class);
			it.putExtra(Constant.TROUBLE_PATH, TSDConst.CAR_DVR_ALERT_VIDEO_PATH);
			it.putExtra("isAccidents", false);
			startActivity(it);
			break;
		case R.id.home_settings_btn:
			// 设置
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.home_exit_btn:
			// 退出
			quit();
			break;
		}
	}
	
	private ServiceConnection serviceConnection = new ServiceConnection() { 
		 
		@Override  
		public void onServiceConnected(ComponentName name, IBinder service) {  
			countService = (IInterfaceService) service; 
			Message msg = new Message();
			msg.what = 1;
			moveHandler.sendMessage(msg);
		} 
		  
		@Override  
		public void onServiceDisconnected(ComponentName name) { 
		   countService = null;  
		} 
	}; 
	
	private void showData(){
		JSONArray array = countService.getVideoStats();
		int num1 = 0,num2=0,num3=0;
		try {
		int size = array.length();
		for(int i=0;i<size;i++){
			
				JSONObject obj = array.getJSONObject(i);
				if(obj.getString("type").equals("normal")){
//					TextView homeText = (TextView) findViewById(R.id.home_driving_num);
//					homeText.setText(obj.getString("total"));
					num1 = Integer.valueOf(obj.getString("total"));
				}else if(obj.getString("type").equals("event")){
					TextView homeText = (TextView) findViewById(R.id.home_accident_num);
					homeText.setText(obj.getString("total"));
					num2 = Integer.valueOf(obj.getString("total"));
					int how = obj.getInt("unread");
					if(how>0){
						TextView numText = (TextView) findViewById(R.id.home_accident_num_text);
						numText.setText(String.valueOf(how));
						numText.setVisibility(View.VISIBLE);
					}
				}else{
					TextView homeText = (TextView) findViewById(R.id.home_remind_num);
					homeText.setText(obj.getString("total"));
					num3 = Integer.valueOf(obj.getString("total"));
				}
			
		}
		TextView homeText = (TextView) findViewById(R.id.home_driving_num);
		homeText.setText(String.valueOf(num1+num2+num3));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	@SuppressLint("HandlerLeak")
	private Handler moveHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				showData();
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
	}



	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			switch (v.getId()) {
			case R.id.home_driving_layout:
				homeDrivingLayout.setEnabled(true);
				homeAccidentLayout.setEnabled(false);
				homeRemindLayout.setEnabled(false);
				break;
			case R.id.home_accident_layout:
				homeDrivingLayout.setEnabled(false);
				homeAccidentLayout.setEnabled(true);
				homeRemindLayout.setEnabled(false);
				break;
			case R.id.home_remind_layout:
				homeDrivingLayout.setEnabled(false);
				homeAccidentLayout.setEnabled(false);
				homeRemindLayout.setEnabled(true);
				break;
			default:
				break;
			}
			
			break;
		case MotionEvent.ACTION_UP:
			homeDrivingLayout.setEnabled(true);
			homeAccidentLayout.setEnabled(true);
			homeRemindLayout.setEnabled(true);
			break;
		default:
			break;
		}
		return false;
	};
	
}

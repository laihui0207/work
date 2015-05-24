package com.tuyou.tsd.cardvr.activitys;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.tuyou.tsd.cardvr.BaseActivity;
import com.tuyou.tsd.cardvr.R;
import com.tuyou.tsd.cardvr.bean.VideoInfo;
import com.tuyou.tsd.cardvr.service.IInterfaceService;
import com.tuyou.tsd.cardvr.utils.Tools;
import com.tuyou.tsd.cardvr.utils.VideoInfoComparator;
import com.tuyou.tsd.common.CommonConsts;
import com.tuyou.tsd.common.TSDConst;

public class CheckOutActivity extends BaseActivity implements OnClickListener,OnSeekBarChangeListener {

	private IInterfaceService countService;
	private ArrayList<VideoInfo> list;
	private int index= 0;
	private TextView checkOutTouch;
	private ImageView checkOutBg;
	private int width;
	private SeekBar seekBar;
	private ArrayList<VideoInfo> listVideoInfo; 
	private int indexCheck = 0;
	private ImageView checkOutPrew;
	private ImageView checkOutNext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.check_out);
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

		width = wm.getDefaultDisplay().getWidth();
		try {
			FileInputStream fileInputStream = new FileInputStream(TSDConst.CAR_DVR_VIDEO_PATH+"/VideoInfo.txt");           
	  		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);           
	  		listVideoInfo =(ArrayList<VideoInfo>) objectInputStream.readObject(); 
	  		indexCheck = getIntent().getExtras().getInt("index_check");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		initView();
	}

	private void initView() {
		this.bindService( new Intent("com.tuyou.tsd.cardvr.service.InterfaceService" ), this.serviceConnection, BIND_AUTO_CREATE);
		ImageView checkOutPlay = (ImageView) findViewById(R.id.check_out_play);
		checkOutPlay.setOnClickListener(this);
		checkOutTouch = (TextView) findViewById(R.id.check_out_touch);
		checkOutBg = (ImageView) findViewById(R.id.check_out_bg);
		TextView back = (TextView) findViewById(R.id.back);
		back.setOnClickListener(this);

		TextView musicPlayTitle = (TextView) findViewById(R.id.music_play_title);
		musicPlayTitle.setText(R.string.check_out_title);
		
		checkOutPrew = (ImageView) findViewById(R.id.check_out_prew);
		checkOutPrew.setOnClickListener(this);
		
		checkOutNext = (ImageView) findViewById(R.id.check_out_next);
		checkOutNext.setOnClickListener(this);
		
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(this);
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.check_out_play:
			Intent intent = new Intent();
			intent.setClass(this, VideoPlayActivity.class);
			intent.putExtra("data", list);
			intent.putExtra("index", index);
			startActivity(intent);
			break;
		case R.id.check_out_prew:
			if(indexCheck>0){
				indexCheck--;
				Message msg = new Message();
				msg.what = 1;
				moveHandler.sendMessage(msg);
			}
			
			break;
		case R.id.check_out_next:
			try {
				if(indexCheck<listVideoInfo.size()-1){
					indexCheck++;
					Message msg = new Message();
					msg.what = 1;
					moveHandler.sendMessage(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.back:
			finish();
			break;

		default:
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
	private Handler moveHandler = new Handler() {
		
		@SuppressLint("NewApi")
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				try {
					list = new ArrayList<VideoInfo>();
	          		JSONArray array = countService.getVideoSetItems(listVideoInfo.get(indexCheck).getTime());
					for(int i=0;i<array.length();i++){
						VideoInfo info = new VideoInfo();
						JSONObject obj = array.getJSONObject(i);
						info.setTime(obj.getString("timestamp"));
						info.setName(obj.getString("name"));
						info.setDur(obj.getInt("duration"));
						list.add(info);
					}
					VideoInfoComparator com = new VideoInfoComparator();
					Collections.sort(list, com);
					
					TextView checkOutTime  = (TextView) findViewById(R.id.check_out_time);
					checkOutTime.setText(Tools.getTime(Long.valueOf(list.get(0).getTime()),"yyyyMMdd_hh:mm:ss"));
					
					TextView checkOutStartTime = (TextView) findViewById(R.id.check_out_start_time);
					checkOutStartTime.setText(Tools.getTime(Long.valueOf(list.get(0).getTime()),"hh:mm:ss"));
					TextView checkOutEndTime = (TextView) findViewById(R.id.check_out_end_time);
					checkOutEndTime.setText(Tools.getTime(Long.valueOf(list.get(list.size()-1).getTime()),"hh:mm:ss"));
					index= 0;
					seekBar.setProgress(0);
					Drawable draw = Drawable.createFromPath(countService.getVideoThumbnail(list.get(index).getName()));
					if(draw!=null){
						checkOutBg.setBackground(draw);
					}
					
					showEnable();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	};
	
	private void showEnable(){
		if(listVideoInfo.size()==1){
			checkOutPrew.setEnabled(false);
			checkOutNext.setEnabled(false);
		}else{
			if(indexCheck==listVideoInfo.size()-1){
				checkOutNext.setEnabled(false);
			}else if(indexCheck==0){
				checkOutPrew.setEnabled(false);
			}else{
				checkOutPrew.setEnabled(true);
				checkOutNext.setEnabled(true);
			}
		}
		
	}
	@SuppressLint("NewApi")
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
		try {

			LayoutParams param=(LayoutParams)checkOutTouch.getLayoutParams();
			int temp = index;
			int size = progress*width/100;
			if(size-60<40){
				param.setMargins(40, 0, 0, 0);
			}else if(size>=width-160){
				param.setMargins(width-160, 0, 0, 0);
			}else{
				param.setMargins(size-60, 0, 0, 0);
			}
			index = progress*(list.size()-1)/100;
			checkOutTouch.setText(Tools.getTime(Long.valueOf(list.get(index).getTime()),"hh:mm:ss"));
			TextView checkOutTime  = (TextView) findViewById(R.id.check_out_time);
			checkOutTime.setText(Tools.getTime(Long.valueOf(list.get(index).getTime()),"yyyyMMdd_hh:mm:ss"));
			if(temp!=index){
				Drawable draw = Drawable.createFromPath(countService.getVideoThumbnail(list.get(index).getName()));
				if(draw!=null){
					checkOutBg.setBackground(draw);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		checkOutTouch.setVisibility(View.VISIBLE);
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		checkOutTouch.setVisibility(View.GONE);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
	}

}

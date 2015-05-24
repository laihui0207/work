package com.tuyou.tsd.cardvr.activitys;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tuyou.tsd.cardvr.BaseActivity;
import com.tuyou.tsd.cardvr.R;
import com.tuyou.tsd.cardvr.adapter.TroubleAdapter;
import com.tuyou.tsd.cardvr.bean.TroubleInfo;
import com.tuyou.tsd.cardvr.bean.VideoInfo;
import com.tuyou.tsd.cardvr.customView.NoScrollGridView;
import com.tuyou.tsd.cardvr.service.IInterfaceService;


public class TroubleActivity extends BaseActivity implements OnClickListener{
	private TroubleAdapter adapter;
	private IInterfaceService countService;
	private ArrayList<VideoInfo> list;
	private LinearLayout troubleLayout;
	private int count = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trouble);
		initView();
	}

	private void initView() {
		list = new ArrayList<VideoInfo>();
		this.bindService( new Intent("com.tuyou.tsd.cardvr.service.InterfaceService" ), this.serviceConnection, BIND_AUTO_CREATE);
		
		RelativeLayout titleParent = (RelativeLayout) findViewById(R.id.title_parent);
		titleParent.setBackgroundColor(0);
		
		TextView back = (TextView) findViewById(R.id.back);
		back.setOnClickListener(this);
		TextView musicPlayTitle = (TextView) findViewById(R.id.music_play_title);
		if(getIntent().getExtras().getBoolean("isAccidents")){
			musicPlayTitle.setText(R.string.title_trouble);
		}else{
			musicPlayTitle.setText(R.string.title_love);
		}
		
		troubleLayout = (LinearLayout) findViewById(R.id.trouble_layout);
	}


	
	private void showNone(){
		LinearLayout videoLayout = (LinearLayout) findViewById(R.id.video_layout);
		videoLayout.setVisibility(View.GONE);
		RelativeLayout videoNoneLayout = (RelativeLayout) findViewById(R.id.video_none_layout);
		videoNoneLayout.setVisibility(View.VISIBLE);
		TextView videoNoneMsg = (TextView) findViewById(R.id.video_none_msg);
		
		TextView videoNoneMessage = (TextView) findViewById(R.id.video_none_message);
		if(getIntent().getExtras().getBoolean("isAccidents")){
			videoNoneMsg.setText(R.string.event_msg);
			videoNoneMessage.setText(R.string.event_message);
		}else{
			videoNoneMsg.setText(R.string.love_msg);
			videoNoneMessage.setText(R.string.love_message);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			onActivityStop();
			finish();
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(event.getAction()==KeyEvent.KEYCODE_BACK){
			onActivityStop();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		onActivityStop();
		unbindService(serviceConnection);
	}
	
	private void onActivityStop(){
		try {
			adapter.cancelAllTasks();
			adapter = null;
		} catch (Exception e) {
			
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(count==0){
			count++;
		}else{
			Message msg = new Message();
			msg.what = 1;
			moveHandler.sendMessage(msg);
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
	
	
	private Handler moveHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				if(list!=null){
					list.clear();
					troubleLayout.removeAllViews();
				}
				JSONObject obj = null;
				if(getIntent().getExtras().getBoolean("isAccidents")){
					obj = countService.getVideoSets("event", null, null);
				}else{
					obj = countService.getVideoSets("favourite", null, null);
				}
				 
				 try {
					JSONArray array = obj.getJSONArray("content");
					int size = array.length();
					for(int i= 0;i<size;i++){
						VideoInfo info = new VideoInfo();
						ArrayList<TroubleInfo> tlist = new ArrayList<TroubleInfo>();
						JSONObject objV = array.getJSONObject(i);
						JSONArray arrayV = objV.getJSONArray("videoes");
						int sizeV = arrayV.length();
						for(int j=0;j<sizeV;j++){
							JSONObject objVie = arrayV.getJSONObject(j);
							TroubleInfo tinfo = new TroubleInfo();
							tinfo.setName(objVie.getString("name"));
							tinfo.setAddress(objV.getString("address"));
							tinfo.setDistrict(objV.getString("district"));
							tinfo.setTime(objVie.getString("timestamp"));
							tlist.add(tinfo);
						}
						info.setAddress(objV.getString("address"));
						info.setStartTime(objV.getString("startTime"));
						info.setRead(objV.getBoolean("read"));
						info.setList(tlist);
						list.add(info);
					}
					if(list!=null&&list.size()>0){
						for(final VideoInfo info :list){
							View view = LayoutInflater.from(TroubleActivity.this).inflate(R.layout.trouble_gridview, null);
							NoScrollGridView viewGridview = (NoScrollGridView) view.findViewById(R.id.trouble_gridview);
							final ImageView read = (ImageView) view.findViewById(R.id.view_state);
							viewGridview.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> arg0,View arg1, int arg2, long arg3) {
									int size = info.getList().size();
									for(int i=0;i<size;i++){
										countService.setRead(info.getList().get(i).getName());
									}
									Intent intent = new Intent(TroubleActivity.this,VideoPlayActivity.class);
									intent.putExtra("index", arg2);
									intent.putExtra("data", info.getList());
									intent.putExtra("is_child", true);
									startActivity(intent);
									read.setVisibility(View.GONE);
								}
							});
							if(getIntent().getExtras().getBoolean("isAccidents")){
								if(info.getRead()){
									read.setVisibility(View.GONE);
								}else{
									read.setVisibility(View.VISIBLE);
								}
							}
							TextView address = (TextView) view.findViewById(R.id.address);
							try{
								if(info.getAddress()!=null&&!info.getAddress().equals("")){
									address.setText(info.getAddress()+"\t\t"+info.getStartTime().substring(0, info.getStartTime().indexOf("T")).replaceAll("-", "."));
								}else{
									address.setText(info.getStartTime().substring(0, info.getStartTime().indexOf("T")).replaceAll("-", "."));
								}
							}catch(Exception e){
								e.printStackTrace();
							}
							if(address.getText().equals("")){
								address.setText(R.string.no_address);
							}
							
							troubleLayout.addView(view);
							if(getIntent().getExtras().getBoolean("isAccidents")){
								adapter = new TroubleAdapter(TroubleActivity.this, info.getList(),viewGridview,1,countService);
							}else{
								adapter = new TroubleAdapter(TroubleActivity.this, info.getList(),viewGridview,0,countService);
							}
							
							viewGridview.setAdapter(adapter);
						}
			        }else{
			        	showNone();
			        }
				} catch (Exception e) {
					showNone();
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	};
	
	
}

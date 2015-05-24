package com.tuyou.tsd.updatesoft;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.tuyou.tsd.common.network.GetUpdateInfoRes;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class UpdateInfoActivity extends Activity implements OnClickListener{
	
	GetUpdateInfoRes updateInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.displayinfo);
		 Intent intent=getIntent();  
		 Gson gson = new Gson();
		 String result=intent.getStringExtra("updateinfo");  
		 updateInfo = gson.fromJson(result, GetUpdateInfoRes.class); 		
		initView();
	}

	private void initView() {
		TextView message = (TextView) findViewById(R.id.message);
		
		List<String> list = new ArrayList<String>();		
		list.add("版本号"+updateInfo.apk.version);
		for(int i=0;i<updateInfo.apk.notes.length;i++)
		{
			list.add(updateInfo.apk.notes[i]);
		}
		ListView listView = (ListView) findViewById(R.id.list);
		TextAdapter adapter = new TextAdapter(this, list);
		listView.setAdapter(adapter);
		
		TextView iknow = (TextView) findViewById(R.id.iknow);
		iknow.setOnClickListener(this);
		handler.postDelayed(runnable, 3000);  
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iknow:
			finish();
			break;
		default:
			break;
		}
	}
	
	 Handler handler = new Handler();  
	     Runnable runnable = new Runnable() {  
	   
	        @Override  
	         public void run() {  
	            // handler自带方法实现定时器   
	             try {  
	            	 UpdateInfoActivity.this.finish(); 

	            } catch (Exception e) {  
	                e.printStackTrace();  
	             }  
	        }  
	    };  


}

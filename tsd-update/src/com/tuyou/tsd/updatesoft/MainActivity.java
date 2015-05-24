package com.tuyou.tsd.updatesoft;

import com.tuyou.tsd.common.CommonMessage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Button updateBtn = (Button) findViewById(R.id.btnUpdate);
		updateBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(CommonMessage.UPDATE_DOWNLOAD);				
				sendBroadcast(intent);
			}
		});
		
		Button installBtn = (Button) findViewById(R.id.btnInstall);
		installBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(CommonMessage.UPDATE_INSTALL);				
				sendBroadcast(intent);
			}
		});
		
		Button displayBtn = (Button) findViewById(R.id.btnDisplay);
		displayBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(CommonMessage.UPDATE_DISPLAYINFO);				
				sendBroadcast(intent);
				
			}
		});
		
		startService(new Intent(MainActivity.this,
				UpdateSoftService.class));

	}
}
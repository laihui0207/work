package com.tuyou.tsd.voice;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.tuyou.tsd.common.CommonApps;
import com.tuyou.tsd.common.TSDEvent;

public class BackActivity extends Activity {

	public BackActivity() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ImageView im = new ImageView(this);
		im.setBackgroundResource(R.drawable.ic_launcher);
		setContentView(im);
		
//		startActivity(new Intent(this, InteractingActivity.class));
		
		im.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				 
			}
		});
//		startActivityForResult(new Intent(this, InteractingActivity.class), 0);
		Log.v("fq","BackActivity  sendEmptyMessageDelayed");

		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonApps.APP_VOICE_INTERACTINGACTIVITY);
		registerReceiver(mSystemEventsReceiver, filter);
	}
	
	private final BroadcastReceiver mSystemEventsReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(CommonApps.APP_VOICE_INTERACTINGACTIVITY)) {
				boolean run = intent.getBooleanExtra(CommonApps.APP_VOICE_INTERACTINGACTIVITY_RUNNING, false);
				Log.v("fq","BackActivity run="+run);
				if(!run){
					startActivity(new Intent(BackActivity.this, InteractingActivity.class));
				}
				
			}
		}
		
	};
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			Log.v("fq","BackActivity startActivity");
			
		};
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.v("fq","onActivityResult resultCode="+resultCode);
//		finish();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.v("fq","BackActivity  onPause");
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
}

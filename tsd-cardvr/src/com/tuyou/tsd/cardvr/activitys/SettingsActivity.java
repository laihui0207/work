package com.tuyou.tsd.cardvr.activitys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuyou.tsd.cardvr.BaseActivity;
import com.tuyou.tsd.cardvr.R;
import com.tuyou.tsd.common.TSDComponent;
import com.tuyou.tsd.common.TSDEvent;

public class SettingsActivity extends BaseActivity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_layout);

		initView();
	}

	private void initView() {
		TextView back = (TextView) findViewById(R.id.back);
		back.setOnClickListener(this);
		ImageView photoandrecordImg = (ImageView) findViewById(R.id.photoandrecord_img);
		photoandrecordImg.setOnClickListener(this);
		Context shareContext;
		try {
			shareContext = createPackageContext(TSDComponent.CORE_SERVICE_PACKAGE, 0);
			SharedPreferences pref = shareContext.getSharedPreferences(getPackageName(), Context.MODE_MULTI_PROCESS);
			if(pref.getBoolean(TSDEvent.CarDVR.PHOTO_RECORD, false)==true){
				photoandrecordImg.setImageResource(R.drawable.isrecord);
			}else{
				photoandrecordImg.setImageResource(R.drawable.unrecord);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.photoandrecord_img:
			Context shareContext;
			try {
				shareContext = createPackageContext(TSDComponent.CORE_SERVICE_PACKAGE, 0);
				SharedPreferences pref = shareContext.getSharedPreferences(getPackageName(), Context.MODE_MULTI_PROCESS);
				if(pref.getBoolean(TSDEvent.CarDVR.PHOTO_RECORD, false)==true){
					Editor edit = pref.edit();
					edit.putBoolean(TSDEvent.CarDVR.PHOTO_RECORD, false);
					edit.commit();
					ImageView photoandrecordImg = (ImageView) findViewById(R.id.photoandrecord_img);
					photoandrecordImg.setImageResource(R.drawable.unrecord);
					
				}else{
					Editor edit = pref.edit();
					edit.putBoolean(TSDEvent.CarDVR.PHOTO_RECORD, true);
					edit.commit();
					ImageView photoandrecordImg = (ImageView) findViewById(R.id.photoandrecord_img);
					photoandrecordImg.setImageResource(R.drawable.isrecord);
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			Intent intent = new Intent(TSDEvent.System.PUSH_CONFIG_INFO);
			startService(intent);
			break;
		}
	}
}

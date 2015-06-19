package com.tuyou.tsd.settings.about;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.base.CommonSleep;
import com.tuyou.tsd.common.network.AppVersionInfo;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.settings.R;
import com.tuyou.tsd.settings.base.SleepBaseActivity;

public class AboutActivity extends SleepBaseActivity implements OnClickListener {
	private String TAG = "AboutActivity";
	private TextView backButton;
	private TextView versionNum, versionContent;
	private LinearLayout mainView, versionView, orderView;
	private RelativeLayout versionLayout, orderLayout;
	private String UPDATE_FILE_PATH = Environment.getExternalStorageDirectory()
			+ TSDConst.APK_PATH + TSDConst.VERSION_PATH;
	private List<AppVersionInfo> updateFile = new ArrayList<AppVersionInfo>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		init();

	}

	private void init() {
		getFiles(UPDATE_FILE_PATH);
		versionNum = (TextView) findViewById(R.id.txt_about_version_num);
		versionContent = (TextView) findViewById(R.id.txt_about_version_content);
		versionLayout = (RelativeLayout) findViewById(R.id.layout_about_main_version);
		orderLayout = (RelativeLayout) findViewById(R.id.layout_about_main_order);
		backButton = (TextView) findViewById(R.id.btn_about_back);
		mainView = (LinearLayout) findViewById(R.id.layout_about_main);
		versionView = (LinearLayout) findViewById(R.id.layout_about_version);
		orderView = (LinearLayout) findViewById(R.id.layout_about_order);
		backButton.setOnClickListener(this);
		orderLayout.setOnClickListener(this);
		versionLayout.setOnClickListener(this);
		if (updateFile.size() > 0) {
			versionNum.setText(updateFile.get(0).version);
			String content = "";
			for (int i = 0; i < updateFile.get(0).notes.length; i++) {
				content = content + updateFile.get(0).notes[i] + "\n";
			}
			versionContent.setText(content);
		}else {
			versionNum.setText("暂时为空");
		}
		// }else {
		// playBroadcast(getResources()
		// .getString(R.string.about_no_update),0);
		// }

	}

	private void getFiles(String path) {
		try {
			File[] allFiles = new File(path).listFiles();
			String content = "";
			if (allFiles == null) {
//				Toast toast = Toast
//						.makeText(
//								AboutActivity.this,
//								getResources().getString(
//										R.string.about_no_update),
//								Toast.LENGTH_LONG);
//				toast.setGravity(Gravity.BOTTOM, 0, 10);
//				toast.show();
				return;
			}
			for (File file : allFiles) {
				LogUtil.d(TAG, "filePath = " + file.getPath());
				try {
					InputStream is = new FileInputStream(file);
					if (is != null) {
						InputStreamReader inputreader = new InputStreamReader(
								is);
						BufferedReader buffreader = new BufferedReader(
								inputreader);
						String line;
						// 分行读取
						while ((line = buffreader.readLine()) != null) {
							content += line;
							LogUtil.d(TAG, "line = " + line);
						}
						is.close();
						Gson gson = new Gson();
						LogUtil.d(TAG, "content = " + content);
						AppVersionInfo appInfo = gson.fromJson(content,
								AppVersionInfo.class);
						updateFile.add(appInfo);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			LogUtil.d(TAG, getResources().getString(R.string.about_no_update));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.layout_about_main_version:
			showView(versionView);
			break;
		case R.id.layout_about_main_order:
			showView(orderView);
			break;
		case R.id.btn_about_back:
			if (mainView.getVisibility() == View.VISIBLE) {
				finish();
			} else if (versionView.getVisibility() == View.VISIBLE
					|| orderLayout.getVisibility() == View.VISIBLE) {
				showView(mainView);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 展示view
	 * 
	 * @param view
	 *            要展示的view
	 */
	public void showView(View view) {
		mainView.setVisibility(View.GONE);
		versionView.setVisibility(View.GONE);
		orderView.setVisibility(View.GONE);
		view.setVisibility(View.VISIBLE);
	}
	
	
	CommonSleep mCommonSleep = null;
	@Override
	protected void onResume() {
		mCommonSleep = new CommonSleep(this);
		mCommonSleep.start();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if(mCommonSleep != null){
			mCommonSleep.stop();
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mCommonSleep != null){
			mCommonSleep.stop();
		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(mCommonSleep != null){
			mCommonSleep.update();
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(mCommonSleep != null){
			mCommonSleep.update();
		}
		return super.dispatchTouchEvent(ev);
	}
}

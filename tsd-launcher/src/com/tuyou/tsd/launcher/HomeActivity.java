package com.tuyou.tsd.launcher;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.jinglingtec.ijiazublctor.bluetooth.BlueToothService;
import com.tuyou.tsd.R;
import com.tuyou.tsd.common.CommonApps;
import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.base.BaseActivity;
import com.tuyou.tsd.common.base.CommonSleep;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.core.CoreService;
import com.tuyou.tsd.core.CoreService.ContentType;
import com.tuyou.tsd.core.CoreService.WorkingMode;

public class HomeActivity extends BaseActivity {
	private static final String TAG = "HomeActivity";

	private static ProgressDialog mLoadingDialog;
	private IntentFilter mIntentFilter;

	private ImageView mFaceView;
	private ImageButton mMusicBtn, mNewsBtn, mNavBtn, mPodBtn;
	private ImageButton mSettingBtn, mAllAppsBtn;

	private CoreService mCoreService;
	
	private int mTestCount = 0;

	// For cell phone debug only
//	private boolean mAccState; // true -- on; false -- off.
//	private int mClickedTimes;

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.v(TAG, "onServiceDisconnected.");
			mCoreService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.v(TAG, "onServiceConnected.");
			mCoreService = ((CoreService.LocalBinder)service).getService();
			if (mCoreService.isLoadingCompleted() && mLoadingDialog != null) {
				LogUtil.v(TAG, "dismiss the loading diaLogUtil.");
				mLoadingDialog.dismiss();
				mLoadingDialog = null;
			} else {
				LogUtil.w(TAG, "ignore dismiss dialog, mCoreService.isLoadingCompleted()=" + mCoreService.isLoadingCompleted()
						+ ", mLoadingDialog=" + mLoadingDialog);
			}
		}
	};

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			LogUtil.d(TAG, "receive the broadcast: " + intent);
			String action = intent.getAction();

			if (action.equals(CommonMessage.VOICE_COMM_WAKEUP)) {
				onWakeUp();
			}else if (action.equals(TSDEvent.System.HARDKEY4_PRESSED)) {
				onWakeUp();
			}

			if (action.equals(TSDEvent.System.LOADING_COMPLETE)) {
				if (mLoadingDialog != null) {
					LogUtil.v(TAG, "dismiss the loading diaLogUtil.");
					mLoadingDialog.dismiss();
					mLoadingDialog = null;
				} else {
					LogUtil.w(TAG, "LoadingDialog is null, skip the event.");
				}
			}
		}
		
	};

	CommonSleep mLauncherSleep = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.home_activity_2);

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(TSDEvent.System.LOADING_COMPLETE);
		mIntentFilter.addAction(CommonMessage.VOICE_COMM_WAKEUP);
		mIntentFilter.addAction(TSDEvent.System.HARDKEY4_PRESSED);

		initView();
		initService();
		showLoadingDialog();
		
		//blue tooth
		startService(new Intent(this,BlueToothService.class));
		
		LogUtil.v("fq", "Start blue tooth service.");
	}

	@Override
	protected void onResume() {
		Log.v(TAG, "onResume");
		super.onResume();
		setIconClick(false);
		if(mCoreService != null){
			mCoreService.reSetWorkingMode();
		}
		Intent intent = new Intent(TSDEvent.Interaction.FINISH_ACTIVITY);
		sendBroadcast(intent);
		
		mFaceView.setBackgroundResource(R.drawable.xiaobao_nor);
		
		registerReceiver(mReceiver, mIntentFilter);
		bindService(new Intent(this, CoreService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
		
		mLauncherSleep = new CommonSleep(this);
		mLauncherSleep.start();
		
		//test
		if(CoreService.TestYZSstr!=null){
			AlertDialog.Builder builder = new Builder(this);
			builder.setMessage(CoreService.TestYZSstr);
			builder.create().show();
			CoreService.TestYZSstr = null;
		}
	}

	@Override
	protected void onPause() {
		Log.v(TAG, "onPause");
		super.onPause();

		unregisterReceiver(mReceiver);
		unbindService(mServiceConnection);
		
		if(mLauncherSleep != null){
			mLauncherSleep.stop();
		}
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy");
		if(mLauncherSleep != null){
			mLauncherSleep.stop();
		}
		super.onDestroy();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if(mLauncherSleep != null){
			mLauncherSleep.update();
		}
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if(mLauncherSleep != null){
			mLauncherSleep.update();
		}
		return super.dispatchKeyEvent(event);
	}

//	@Override
//	protected void onSaveInstanceState(Bundle outState) {
//		outState.putParcelable("weather", mWeatherData);
//		super.onSaveInstanceState(outState);
//	}

	private void initView() {
		mFaceView = (ImageView) findViewById(R.id.home_xiaobao_face);
		mMusicBtn = (ImageButton) findViewById(R.id.home_music_btn);
		mNewsBtn  = (ImageButton) findViewById(R.id.home_news_btn);
		mNavBtn   = (ImageButton) findViewById(R.id.home_nav_btn);
		mPodBtn   = (ImageButton) findViewById(R.id.home_pod_btn);
		mSettingBtn  = (ImageButton) findViewById(R.id.home_setting_btn);
		mAllAppsBtn = (ImageButton) findViewById(R.id.home_allapps_btn);

		mMusicBtn.setOnClickListener(mClickListener);
		mNewsBtn.setOnClickListener(mClickListener);
		mNavBtn.setOnClickListener(mClickListener);
		mPodBtn.setOnClickListener(mClickListener);
		mSettingBtn.setOnClickListener(mClickListener);
		mAllAppsBtn.setOnClickListener(mClickListener);
		mFaceView.setOnClickListener(mClickListener);
	}
	

	private void initService() {
		startService(new Intent(this, CoreService.class));
	}

	private void onWakeUp() {
		mFaceView.setBackgroundResource(R.drawable.xiaobao_wakeup);
	}


	private void showLoadingDialog() {
//		LogUtil.v(TAG, "showLoadingDialog()");
		if (mLoadingDialog == null) {
			LogUtil.v(TAG, "create the loading diaLogUtil.");
			LoadingDialog df = LoadingDialog.newInstance();
			df.show(getFragmentManager(), "loadingDialog");
		} else {
			LogUtil.w(TAG, "Loading dialog is already shown, ignore.");
		}
	}

	public static class LoadingDialog extends DialogFragment {

		static LoadingDialog newInstance() {
			return new LoadingDialog();
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			mLoadingDialog = new ProgressDialog(getActivity());
			mLoadingDialog.setMessage("系统正在启动中...");
			mLoadingDialog.setIndeterminate(true);
//			mLoadingDiaLogUtil.setCancelable(true);
//			LogUtil.v(TAG, "LoadingDiaLogUtil.onCreateDialog, mLoadingDialog = " + mLoadingDialog);
            return mLoadingDialog;
		}
		
	}

	private MyOnClickListener mClickListener = new MyOnClickListener();
	private volatile boolean mbIsClickedIcon = false;
	private void setIconClick(boolean bClick){
		LogUtil.w(TAG, "setIconClick mbIsClickedIcon="+mbIsClickedIcon);
		mbIsClickedIcon = bClick;
	}
	private boolean isIconClicked(){
		LogUtil.w(TAG, "isIconClicked mbIsClickedIcon="+mbIsClickedIcon);
		return mbIsClickedIcon;
	}
	private class MyOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			LogUtil.w(TAG, "MyOnClickListener mbIsClickedIcon="+mbIsClickedIcon);
			if(v.getId() == R.id.home_xiaobao_face){
				mTestCount++;
				return;
			}else if(v.getId() == R.id.home_music_btn){
				if(mTestCount==5){
					Intent intent = new Intent(CommonApps.BROADCAST_TEST_ON);
					sendBroadcast(intent);
					Toast.makeText(HomeActivity.this, "Test State Change !", 0).show();
					mTestCount = 0;
					return;
				}
			}else{
				mTestCount = 0;
			}
			
			if(!isIconClicked()){
				setIconClick(true);
			}else{
				return;
			}
			switch (v.getId()) {
			case R.id.home_music_btn:
				if (mCoreService != null) {
					mCoreService.changeMode(WorkingMode.MODE_AUDIO, ContentType.TYPE_MUSIC);
				}
				break;
			case R.id.home_news_btn:
				if (mCoreService != null) {
					mCoreService.changeMode(WorkingMode.MODE_AUDIO, ContentType.TYPE_NEWS);
				}
				break;
			case R.id.home_nav_btn:
				if (mCoreService != null) {
					mCoreService.changeMode(WorkingMode.MODE_MAP, ContentType.TYPE_MAP);
				}
				break;
			case R.id.home_pod_btn:
				if (mCoreService != null) {
					mCoreService.changeMode(WorkingMode.MODE_AUDIO, ContentType.TYPE_JOKE);
				}
				break;
			case R.id.home_setting_btn:
				// TODO:
				HelperUtil.startActivityWithFadeInAnim(HomeActivity.this, 
					"com.tuyou.tsd.settings", "com.tuyou.tsd.settings.SettingsActivity");
				// 用intent启动拨打电话
			/*	Intent intent = new Intent(Intent.ACTION_CALL, Uri
						.parse("tel:4008936008"));
				startActivity(intent);*/
				break;
			case R.id.home_allapps_btn:
				startActivity(new Intent(HomeActivity.this, AppsActivity.class));
				break;
			}
		}
	}
}

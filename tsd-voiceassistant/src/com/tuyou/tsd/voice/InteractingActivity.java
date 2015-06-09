package com.tuyou.tsd.voice;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.tuyou.tsd.common.CommonApps;
import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.voice.service.VoiceAssistant;
import com.tuyou.tsd.voice.service.VoiceEngine.ErrorType;

public class InteractingActivity extends Activity {
	private static final String TAG = "InteractingActivity";
	private boolean mbFinishActivity = false;

	private Fragment mRecordFragment, mRecogFragment, mSearchFragment, mErrorFragment;
//	private ListView mResultListView;
//	private DestinationAdapter mDestAdapter;

//	private boolean mIsRecording = false;

	//
	// 用于与VoiceAssistant service
	//
	private static Messenger mVoiceService = null;
	private final Messenger mMessenger = new Messenger(new VoiceEngineMsgHandler());

	enum FRAGMENT_TYPE {
		RECORD,
		RECOG,
		SEARCH,
		ERROR
	}
	
	enum VOICE_STATE{
		VOICE_STATE_NONE,
		VOICE_STATE_RECORDING,
		VOICE_STATE_RECOGNITION,
		VOICE_STATE_FINISH
	}
	private static boolean CANCEL_RECONG = false;
	private boolean INTERACTION_ING = false;
	private boolean mbIsSearchView = false;
	private volatile boolean mbCanceling = false;
	private VOICE_STATE mVoiceState = VOICE_STATE.VOICE_STATE_NONE;
	private void setVoiceState(VOICE_STATE state){
		LogUtil.d(TAG,"setVoiceState ---------- > "+state);
		mVoiceState = state;
	}
	
	VoiceSleep mVoiceSleep = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		LogUtil.d(TAG,"onCreate...");
		LogUtil.d("fq","InteractingActivity onCreate...");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.interacting_activity);
		setVoiceState(VOICE_STATE.VOICE_STATE_NONE);
		mbIsSearchView = false;
		mbCanceling = false;
		mbFinishActivity = false;
		initView();

		bindService(new Intent(this, VoiceAssistant.class), mVoiceServiceConnection, Service.BIND_AUTO_CREATE);

		IntentFilter filter = new IntentFilter();
		filter.addAction(TSDEvent.Interaction.FINISH_ACTIVITY);
		filter.addAction(CommonMessage.VOICE_COMM_WAKEUP);
		filter.addAction(TSDEvent.System.HARDKEY4_PRESSED);
		registerReceiver(mReceiver, filter);
		
		Intent intent = new Intent(CommonApps.APP_VOICE_INTERACTINGACTIVITY);
		intent.putExtra(CommonApps.APP_VOICE_INTERACTINGACTIVITY_RUNNING, true);
		sendBroadcast(intent);
	}

	@Override
	protected void onResume() {
		LogUtil.d(TAG, "onResume...");
		if (mVoiceService != null) {
			try {
				// Register self for reply message
				Message msg = Message.obtain(null, CommonMessage.VoiceEngine.REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mVoiceService.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		super.onResume();
	}

	@Override
	protected void onPause() {
		LogUtil.d(TAG, "onPause...");
		mbFinishActivity = true;
		if (mVoiceService != null) {
			try {
				// Register self for reply message
				Message msg = Message.obtain(null, CommonMessage.VoiceEngine.UNREGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mVoiceService.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		sendMessageToService(CommonMessage.VoiceEngine.CANCEL_RECOGNITION, null);
		sendBroadcast(new Intent(TSDEvent.Interaction.CANCEL_INTERACTION_BY_TP));
		super.onPause();
		finish();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if(mVoiceSleep != null){
			mVoiceSleep.update();
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void finish() {
		mbFinishActivity = true;
		super.finish();
	}
	
	@Override
	protected void onDestroy() {
		LogUtil.d(TAG, "onDestroy...");
		LogUtil.d("fq","InteractingActivity onDestroy...");
		if(mVoiceSleep != null){
			mVoiceSleep.stop();
		}
		
		mbFinishActivity = true;
		unbindService(mVoiceServiceConnection);
		unregisterReceiver(mReceiver);
		mbCanceling = false;
		super.onDestroy();
		
		Intent intent = new Intent(CommonApps.APP_VOICE_INTERACTINGACTIVITY);
		intent.putExtra(CommonApps.APP_VOICE_INTERACTINGACTIVITY_RUNNING, false);
		sendBroadcast(intent);
	}

	private void initView() {
		mRecordFragment = new RecordFragment();
		mRecogFragment = new RecognitionFragment();
		mSearchFragment = new SearchFragment();
		mErrorFragment = new ErrorFragment();

		FragmentManager fm = getFragmentManager();
		FragmentTransaction trans = fm.beginTransaction();
		trans.add(R.id.fragment_container, mRecordFragment);
		trans.commit();
	}

	private void playBing() {
		// Play hint sound
		MediaPlayer player = MediaPlayer.create(this, R.raw.altair);
		player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.release();
				mp = null;
			}
		});
		player.start();
	}

	private FRAGMENT_TYPE mFragmentType = FRAGMENT_TYPE.RECORD;
	private void transFragment(FRAGMENT_TYPE type) {
		LogUtil.d(TAG,"transFragment FRAGMENT_TYPE "+type+"  mbFinishActivity="+mbFinishActivity);
		if(mbFinishActivity){
			return;
		}
		
		Fragment fragment = null;
		mbIsSearchView = false;
		mFragmentType = type;
		switch(type){
		case RECORD:
			fragment = mRecordFragment;
			mbCanceling = false;
			break;
		case RECOG:
			fragment = mRecogFragment;
			break;
		case SEARCH:
			fragment = mSearchFragment;
			mbIsSearchView = true;
			Intent searchIntent = new Intent(TSDEvent.Interaction.CANCEL_INTERACTION_BY_TP);
			searchIntent.putExtra(VoiceAssistant.INTO_SEARCH_VIEW, true);
			sendBroadcast(searchIntent);
			
			mVoiceSleep = new VoiceSleep(this);
			mVoiceSleep.start();
			break;
		case ERROR:
			fragment = mErrorFragment;
			break;
		}
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_container, fragment);
		transaction.commitAllowingStateLoss();
	}

	/**
	 * 语音助手service binding状态回调
	 */
	private ServiceConnection mVoiceServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mVoiceService = null;
//			Toast.makeText(getApplicationContext(), "VoiceAssistant service disconnected.", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mVoiceService = new Messenger(service);
	
			try {
				// Register self for reply message
				Message msg = Message.obtain(null, CommonMessage.VoiceEngine.REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mVoiceService.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
	
//			Toast.makeText(getApplicationContext(), "VoiceAssistant service connected.", Toast.LENGTH_SHORT).show();			
		}
	};

	void sendMessageToService(int what, Bundle data) {
		if (mVoiceService != null) {
			try {
				Message msg = Message.obtain(null, what);
				msg.replyTo = mMessenger;
				if (data != null) {
					msg.setData(data);
				}
				mVoiceService.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 用于同VoiceAssistant service通信
	 */
	String mRecongText ;
	private class VoiceEngineMsgHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			Bundle tempBundle = null;
			String resStr = null;
			switch(msg.what) {
//			case CommonMessage.VoiceEngine.TTS_PLAY_BEGIN:
//				mFaceButton.setBackgroundResource(R.drawable.intact_face_speaking);
//				break;

			case CommonMessage.VoiceEngine.INTERACTION_START:
				LogUtil.d(TAG,"@@@@@@@@@@@@@@@@@@@@@@@@@  INTERACTION_START ---------->");
				INTERACTION_ING = true;
				break;

			case CommonMessage.VoiceEngine.INTERACTION_STOP:
				LogUtil.d(TAG,"@@@@@@@@@@@@@@@@@@@@@@@@@  INTERACTION_START <----------");
				INTERACTION_ING = false;
				break;

			case CommonMessage.VoiceEngine.RECORDING_START:
				LogUtil.d(TAG,"@@@@@@@@@@@@@@@@@@@@@@@@@  RECORDING_START");
				setVoiceState(VOICE_STATE.VOICE_STATE_RECORDING);
				((RecordFragment)mRecordFragment).setBtnClickable(true);
				break;

			case CommonMessage.VoiceEngine.RECORDING_STOP:
				LogUtil.d(TAG,"@@@@@@@@@@@@@@@@@@@@@@@@@  RECORDING_STOP");
				((RecordFragment)mRecordFragment).stopAnimation();
				break;

			case CommonMessage.VoiceEngine.RECORDING_VOLUME:
				Bundle tempvolume = msg.getData();
				((RecordFragment)mRecordFragment).doVoiceView(tempvolume.getInt("volume"));
				break;

			case CommonMessage.VoiceEngine.RECOGNITION_START:
				LogUtil.d(TAG,"@@@@@@@@@@@@@@@@@@@@@@@@@  RECOGNITION_START");
				setVoiceState(VOICE_STATE.VOICE_STATE_RECOGNITION);
				transFragment(FRAGMENT_TYPE.RECOG);
				break;

			case CommonMessage.VoiceEngine.RECOGNITION_COMPLETE:
				LogUtil.d(TAG,"@@@@@@@@@@@@@@@@@@@@@@@@@  RECOGNITION_COMPLETE");
				setVoiceState(VOICE_STATE.VOICE_STATE_FINISH);
				tempBundle = msg.getData();
				mRecongText = tempBundle.getString("result");
				if(tempBundle != null){
					((RecognitionFragment)mRecogFragment).setResultText(mRecongText);
					playBing();
				}
				break;

			case CommonMessage.VoiceEngine.RECOGNITION_CANCEL:
			case CommonMessage.VoiceEngine.RECOGNITION_ERROR:
				LogUtil.d(TAG,"@@@@@@@@@@@@@@@@@@@@@@@@@  RECOGNITION_CANCEL || RECOGNITION_ERROR");
				setVoiceState(VOICE_STATE.VOICE_STATE_FINISH);
				String error = msg.getData().getString("result");
				if ( !error.equals("用户取消") ) {
					transFragment(FRAGMENT_TYPE.ERROR);
					((ErrorFragment)mErrorFragment).setErrorText(error);
					((ErrorFragment)mErrorFragment).setTalkText(mRecongText);
					playBing();
				}
				break;

			case CommonMessage.VoiceEngine.SEARCH_BEGIN:
				LogUtil.d(TAG,"@@@@@@@@@@@@@@@@@@@@@@@@@  SEARCH_BEGIN");
				((RecognitionFragment)mRecogFragment).setStatusText(getResources().getString(R.string.searching));
				break;

			case CommonMessage.VoiceEngine.SEARCH_END:
				LogUtil.d(TAG,"@@@@@@@@@@@@@@@@@@@@@@@@@  SEARCH_END");
				transFragment(FRAGMENT_TYPE.SEARCH);
				((SearchFragment)mSearchFragment).setResultData(msg.getData().getString("result"));
				break;

			default:
				super.handleMessage(msg);
			}
		}

	}
	
	public static void setCancelRecong(boolean bOn){
		LogUtil.d(TAG,"setCancelRecong = "+bOn);
		CANCEL_RECONG = bOn;
	}
	public static boolean isCancelRecong(){
		LogUtil.d(TAG,"isCancelRecong = "+CANCEL_RECONG);
		return CANCEL_RECONG;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		LogUtil.d(TAG, "keycode = " + keyCode);
		if (KeyEvent.KEYCODE_F4 == keyCode) {
			if (mbIsSearchView && !mbCanceling) {
				Log.v(TAG, "onKeyDown in search view");
				quit();
			}
		}

		return super.onKeyDown(keyCode, event);
	}
	
	private void quit(){
		mbCanceling = true;
		notifyInteractionError();
	}
	
	boolean finishOut = false;
	private void notifyInteractionError() {
		LogUtil.d(TAG, "notifyInteractionError return to laucher! finishOut="+finishOut);
		if(finishOut){
			return;
		}
		finishOut = true;
		
		ErrorType error = ErrorType.ERR_USER_CANCELLED;
		String template_wakeup = "GENERIC";
		String reason = error.name();
		String description = error.value;
		
		Intent intent = new Intent(TSDEvent.Interaction.INTERACTION_ERROR);
		intent.putExtra("template", template_wakeup);
		intent.putExtra("reason", reason);
		intent.putExtra("description", description);
		sendBroadcast(intent);
		
	/*	try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		Intent resultIntent = new Intent(CommonMessage.VOICE_COMM_WAKEUP);
		sendBroadcast(resultIntent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		LogUtil.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.d(TAG, "BroadcastReceiver.onReceive, action: " + action);

			if (action.equals(TSDEvent.Interaction.FINISH_ACTIVITY)) {
				HelperUtil.finishActivity(InteractingActivity.this, android.R.anim.fade_in, android.R.anim.fade_out);
			}else if(action.equals(CommonMessage.VOICE_COMM_WAKEUP)){
				LogUtil.d(TAG, "BroadcastReceiver VOICE_COMM_WAKEUP xxx");
				if (mbIsSearchView && !mbCanceling) {
					Log.v(TAG, "VOICE_COMM_WAKEUP in search view");
					quit();
				}
			}else if(action.equals(TSDEvent.System.HARDKEY4_PRESSED)){
				Log.v(TAG, "HARDKEY4_PRESSED");
				if (mbIsSearchView && !mbCanceling) {
					Log.v(TAG, "HARDKEY4_PRESSED in search view");
					quit();
				}
			}
		}
	};
}

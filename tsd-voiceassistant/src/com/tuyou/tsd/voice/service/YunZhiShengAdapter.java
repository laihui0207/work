package com.tuyou.tsd.voice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import cn.yunzhisheng.common.util.ErrorUtil;
import cn.yunzhisheng.vui.recognizer.IRecognizerTalkListener;
import cn.yunzhisheng.vui.recognizer.RecognizerTalk;
import cn.yunzhisheng.vui.wakeup.IWakeupListener;
import cn.yunzhisheng.vui.wakeup.IWakeupOperate;

final class YunZhiShengAdapter {
	private static final String LOG_TAG = "YunZhiShengAdapter";

	private boolean mRecognitionRecordingState = false;	// 识别服务录音状态
	private boolean mRecognitionState = false;			// 识别状态
	private boolean mWakeupRecordingState = false;		// 唤醒服务录音状态
	private boolean mWakeupInitDone = false;			// 唤醒服务初始化状态
	private boolean mRecognitionInitDone = false;		// 识别服务初始化状态
	private boolean mDataInitDone = false;				// 识别服务数据初始化状态
	private boolean mRequestToStartRecog = false;
	private boolean mRequestToStartWakeUp = false;

	private RecognizerTalk mRecognizer;
	private IWakeupOperate mWakeupOperate = null;

	private MyWakeUpListener mWakeUpListener = new MyWakeUpListener();
	private MyRecognizerListener mRecognizerListener = new MyRecognizerListener();

	private Context mContext;
	private VoiceEngine mCallback;

    long[] StartTalkPerf = {0, 0};                      //语音引擎“待监听”中间态耗时

	private static YunZhiShengAdapter mInstance;
	
	private String TestString = null;

	private YunZhiShengAdapter(Context context, VoiceEngine callback) {
		mContext = context;
		mCallback = callback;
	}

	static YunZhiShengAdapter getInstance(Context context, VoiceEngine callback) {
		if (mInstance == null)
			mInstance = new YunZhiShengAdapter(context, callback);
		return mInstance;
	}

	void init() {
		// 语音魔方初始化
		mRecognizer = new RecognizerTalk(mContext);
		mRecognizer.setListener(mRecognizerListener);
//		mRecognizer.setNetMode(RecognizerTalk.NET_MODE_DEFAULT);
//		mRecognizer.setRecordingTimeout(5000);
//		mRecognizer.setVADTimeout(3000, 1000);
		mRecognizer.init();

		// 获取唤醒插件句柄【若没有唤醒功能，此处得到的是null】
		Object o = mRecognizer.getOperate("OPERATE_WAKEUP");

		if (o != null && o instanceof IWakeupOperate) {
			mWakeupOperate = (IWakeupOperate) o;
		}
		// 设置唤醒回调
		if (mWakeupOperate != null) {
			mWakeupOperate.setBenchmark(-3.1f);
			mWakeupOperate.setWakeupListener(mWakeUpListener);
		}
	}

	void quit() {
		mRecognizer.cancel(true);
		// 释放语音识别句柄
		mRecognizer.release();
	}

	boolean getWakeUpRecordingState() {
		return mWakeupRecordingState;
	}

	private void setRequestToStartWakeUp(boolean bUp){
		Log.d(LOG_TAG,"setRequestToStartWakeUp = "+bUp);
		mRequestToStartWakeUp = bUp;
	}
	
	void startWakeUpListening() {
		Log.d(LOG_TAG, "YunZhiShengAdapter.startWakeUpListening, recogRecordingState=" + mRecognitionRecordingState +
				", mRecogState=" + mRecognitionState);
		if (mWakeupRecordingState) {
			Log.w(LOG_TAG, "wake up service already ran, ignore.");
			return;
		}
		boolean wakeup = false;
		if (!mRecognitionRecordingState && !mRecognitionState) {
			// 若识别没在录音，则直接开始唤醒
			wakeup = requestStartWakeup();
			setRequestToStartWakeUp(false);
		}
		if (!wakeup) {
			Log.d(LOG_TAG, "mRecognizer.cancel();");
			// 否则先将识别停止，以释放录音设备资源
			setRequestToStartWakeUp(true);
			mRecognizer.cancel();
		}
	}

	void stopWakeUpListening() {
		if (mWakeupRecordingState) {
			Log.d(LOG_TAG, "################### stopWakeup #######################");
			mWakeupOperate.stopWakeup();
		}
	}

	void startRecognition() {
		Log.d(LOG_TAG, "YunZhiShengAdapter.startRecognition, mWakeupRecordingState=" + mWakeupRecordingState);
		if (mRecognitionRecordingState) {
			Log.w(LOG_TAG, "recognition service already ran, ignore.");
		}
		if (!mWakeupRecordingState) {
			// 若唤醒没在录音，则直接开始识别
			requestStartTalk();
		} else {
			Log.d(LOG_TAG, "mRecognizer.stopWakeup();");
			mRequestToStartRecog = true;
			// 否则先将唤醒识别停止，以释放录音设备资源
			Log.d(LOG_TAG, "################### stopWakeup #######################");
			mWakeupOperate.stopWakeup();
		}
	}

	boolean mbWakeAfterStop = false;
	void stopRecognition() {
		Log.d(LOG_TAG, "Stop recognition...");
		mRecognizer.stop();
	}

	void cancelRecognition() {
		Log.d(LOG_TAG, "Cancel recognition...");
		mRecognizer.cancel(true);
	}

	/**
	 * 开启唤醒
	 * @param wakeupInitDone
	 * @param recognitionInitDone
	 * @param recordingState
	 */
	private boolean requestStartWakeup() {
		Log.v(LOG_TAG, "requestStartWakeup, wakeupInitDone: " + mWakeupInitDone +
					   ", recognitionInitDone: " + mRecognitionInitDone +
					   ", dataInitDone: " + mDataInitDone +
					   ", recordingState: " + mRecognitionRecordingState +
					   ", recognitionState: " + mRecognitionState +
					   ", wakeupRecordingState: " + mWakeupRecordingState);

		if (mWakeupInitDone && mRecognitionInitDone && mDataInitDone &&
			!mRecognitionRecordingState && !mRecognitionState && !mWakeupRecordingState) {
			// 自定义唤醒命令
			List<String> command = new ArrayList<String>();
			command.add(VoiceAssistant.WAKE_UP_COMMAND_1);
			command.add(VoiceAssistant.WAKE_UP_COMMAND_2);
			command.add(VoiceAssistant.WAKE_UP_COMMAND_3);
			mWakeupOperate.setCommandData(command);

			StartTalkPerf[0] = System.currentTimeMillis();
			Log.d(LOG_TAG, "################### startWakeup #######################");
			mWakeupOperate.startWakeup();
			Log.d(LOG_TAG, "Start wakeup listening...");
			return true;
		}
		return false;
	}

	/**
	 * 开启语音识别
	 * @param wakeupResult
	 * @param recordingState
	 */
	private void requestStartTalk() {
		Log.v(LOG_TAG, "requestStartTalk, mWakeupRecordingState = " + mWakeupRecordingState);
		if (!mWakeupRecordingState) {
			mRequestToStartRecog = false;
			mRecognizer.start();
			Log.d(LOG_TAG, "requestStartTalk Start to recognize...");
		}
	}

	/**
	 * 唤醒服务回调接口
	 */
	private class MyWakeUpListener implements IWakeupListener {

		@Override
		public void onInitDone() {
			Log.v(LOG_TAG, "#####################IWakeupListener######## onInitDone");
			mWakeupInitDone = true;
			setRequestToStartWakeUp(!requestStartWakeup());
		}

		@Override
		public void onStart() {
			Log.v(LOG_TAG, "#####################IWakeupListener######## onStart");
			mWakeupRecordingState = true;
			mCallback.onStart();
		}

		@Override
		public void onStop() {
			Log.v(LOG_TAG, "#####################IWakeupListener######## onStop");
			mWakeupRecordingState = false;
			if (mRequestToStartRecog) {
				// 若已有识别请求，则开始语音识别
				requestStartTalk();
			}
		}

		@Override
		public void onSuccess(String arg0) {
			Log.v(LOG_TAG, "#####################IWakeupListener######## onSuccess");
			String word = arg0.trim();
			Log.v(LOG_TAG, "IWakeupListener.onSuccess, word = " + word);
			mCallback.onWakeUp(word);
		}

		@Override
		public void onError(ErrorUtil arg0) {
			Log.v(LOG_TAG, "#####################IWakeupListener######## onError");
			Log.v(LOG_TAG, "IWakeupListener.onError " + arg0);
		}
	
	}

	private void reuqestToWake(){
		Log.v(LOG_TAG, "reuqestToWake mRequestToStartWakeUp="+mRequestToStartWakeUp);
		if (mRequestToStartWakeUp) {
			// 若有唤醒请求，则开始唤醒服务
			setRequestToStartWakeUp(!requestStartWakeup());
		}
	}
	/**
	 * 语音识别服务回调接口
	 */
	private class MyRecognizerListener implements IRecognizerTalkListener {

		@Override
		public void onInitDone() {
			Log.v(LOG_TAG, "***********IRecognizerTalkListener************onInitDone");
			mRecognitionInitDone = true;

//			public static final String TAG_CONTACT = "Contact"; // 联系人
//			public static final String TAG_APPS = "Apps"; // 应用名
//			public static final String TAG_SONG = "Song"; // 音乐名
//			public static final String TAG_SINGER = "Singer"; // 歌手
//			public static final String TAG_ALBUM = "Album"; // 专辑
			Map<String, List<String>> data = new HashMap<String, List<String>>();
//			data.put(TAG_CONTACT,);
//			data.put(TAG_SONG,);
//			data.put(TAG_SINGER,);
//			data.put(TAG_ALBUM,);
//			data.put(TAG_APPS,);
			mRecognizer.setUserData(data);

			reuqestToWake();
		}

		@Override
		public void onDataDone() {
			Log.v(LOG_TAG, "***********IRecognizerTalkListener************onDataDone");
			mDataInitDone = true;
			reuqestToWake();
		}

		@Override
		public void onUserDataCompile() {
			// TODO Auto-generated method stub
			Log.v(LOG_TAG, "***********IRecognizerTalkListener************onUserDataCompile");
			
		}

		@Override
		public void onUserDataCompileDone() {
			// TODO Auto-generated method stub
			Log.v(LOG_TAG, "***********IRecognizerTalkListener************onUserDataCompileDone");
		}

		@Override
		public void onTalkStart() {
			Log.v(LOG_TAG, "***********IRecognizerTalkListener************onTalkStart");
			mCallback.onTalkStart();
		}

		@Override
		public void onTalkStop() {
			Log.v(LOG_TAG, "***********IRecognizerTalkListener************onTalkStop");
		}

		@Override
		public void onTalkRecordingStart() {
			Log.v(LOG_TAG, "***********IRecognizerTalkListener************onTalkRecordingStart");
            StartTalkPerf[1] = System.currentTimeMillis();
		    Log.d(LOG_TAG, "YunZhiShengAdapter.java::onTalkRecordingStart=> 语音引擎待监听中间态耗时=>[" + (StartTalkPerf[1]-StartTalkPerf[0]) + "] ms.");
			mRecognitionRecordingState = true;
			mCallback.onStartRecording();
		}

		@Override
		public void onTalkRecordingStop() {
			Log.v(LOG_TAG, "***********IRecognizerTalkListener************onTalkRecordingStop");
			mRecognitionRecordingState = false;
			mRecognitionState = true;
			reuqestToWake();
			mCallback.onStopRecording();
			// 录音结束即开始识别
			mCallback.onStartRecognition();
		}

		@Override
		public void onVolumeUpdate(int volume) {
//			Log.v(LOG_TAG, "***********IRecognizerTalkListener************onVolumeUpdate="+volume);
			mCallback.onVolume(volume);
		}

		@Override
		public void onActiveStatusChanged(int arg0) {
			Log.v(LOG_TAG, "***********IRecognizerTalkListener************onActiveStatusChanged="+arg0);
		}

		@Override
		public void onTalkResult(String result) {
			Log.v(LOG_TAG, "***********IRecognizerTalkListener************onTalkResult");
			Log.v(LOG_TAG, "IRecognizerTalkListener.onTalkResult: " + result);
			TestString = "onTalkResult = "+result+"\n"+"-----******-----\n";
			// 此问题为云知声最新sdk在识别结果的句子末尾增加了一个句号，导致后续进行指令匹配时出现问题。
			// 现将末尾句号过滤以临时解决此问题。2015-4-9
			if (result.endsWith("。"))
				result = result.substring(0, result.length() - 1);
			mCallback.onFinishRecognition(result, false);
		}

		@Override
		public void onTalkCancel() {
			Log.v(LOG_TAG, "***********IRecognizerTalkListener************onTalkCancel");
			mRecognitionState = false;
			mRecognitionRecordingState = false;
			mCallback.onCancelRecognition();

			// 若有唤醒请求，则开始唤醒服务
			reuqestToWake();
		}

		@Override
		public void onTalkError(ErrorUtil arg0) {
			Log.v(LOG_TAG, "***********IRecognizerTalkListener************onTalkCancel="+arg0);
			mRecognitionState = false;
			mCallback.onFailedRecognition(arg0.code, arg0.message);

			// 若有唤醒请求，则开始唤醒服务
			reuqestToWake();
		}

		@Override
		public void onTalkParticalResult(String arg0) {
			Log.v(LOG_TAG, "***********IRecognizerTalkListener************onTalkResult="+ arg0);
		}

		@Override
		public void onTalkProtocal(String protocol) {
			Log.v(LOG_TAG, "***********IRecognizerTalkListener************onTalkProtocal");
			Log.v(LOG_TAG, "IRecognizerTalkListener.onTalkProtocal: " + protocol);
			mRecognitionState = false;
			
			String errorStr = null ;
			if (protocol.matches(".+semantic.+")) {
				mCallback.onFinishRecognition(protocol, true);
				errorStr = "semantic OK";
			}else{
				mCallback.onFinishRecognition1("");
				errorStr="Error";
			}
			
			//test
			TestString += "onTalkProtocal="+errorStr+"\n"+protocol+"******\n";
			mCallback.testYZS(TestString);

			// 若有唤醒请求，则开始唤醒服务
			reuqestToWake();
		}

		@Override
		public void isActive(boolean arg0) {
			// TODO Auto-generated method stub
			Log.v(LOG_TAG, "***********IRecognizerTalkListener************isActive="+arg0);
		}

	}
}

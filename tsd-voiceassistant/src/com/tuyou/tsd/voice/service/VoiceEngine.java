package com.tuyou.tsd.voice.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.iflytek.tts.TtsService.Tts;
import com.iflytek.tts.TtsService.TtsSpeaker;
import com.tuyou.tsd.common.CommonApps;
import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.network.JsonOA2;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.voice.R;
import com.tuyou.tsd.voice.service.interaction.Dialog;
import com.tuyou.tsd.voice.service.interaction.Speech;

/**
 * 语音引擎接口定义，对内提供统一接口。从而避免因改换第三方SDK带来的接口不一致问题。
 * @author ruhai
 * 2014-7
 */
public class VoiceEngine implements TtsSpeaker.Callback {
	public enum State {
		STATE_READY,
		STATE_RECOGNITION,	// 录音识别状态
		STATE_SEARCH,		// 搜索状态
		STATE_SUCCESS,		// 
		STATE_ERROR
	}

	enum AnswerType {
		RAW("raw"),
		COMMAND("command"),
		LOCATION("#location"),
		MUSIC("#music");
		public String value;
		private AnswerType(String v) {value = v;}
	}

	public enum ErrorType {
		ERR_NET("网络不给力"),
		ERR_RECORD("小宝没听懂"),
		ERR_RECOG("小宝没听懂"),
		ERR_NO_SPEECH("监听超时"),
		ERR_NO_MATCH_ANSWER("小宝没听懂"),
		ERR_TIME_OUT("超时错误"),
		ERR_USER_CANCELLED("用户取消"),
		ERR_USER_CANCELLED_AND_GO_HOME("取消并回主页"), // 临时增加，后面考虑用更好的解决办法替换
		ERR_SEARCH("关键词无效");
		public String value;
		private ErrorType(String v) {value = v;}
	}

	public interface WakeUpCallback {
		void onWakeUp(String word);
	}

	public interface RecognitionCallback {
		void onInteractSuccessful(String answerType, String answer/*, String answerData*/);
		void onInteractFailed(ErrorType error);
	}

	public interface TTSCallback {
		void onPlayEnd();
	}

	private static final String LOG_TAG = "VoiceEngine";

	private static VoiceEngine mInstance;
	private Context mContext;
	private List<Messenger> mHandler = new ArrayList<Messenger>();

	private static List<VoiceEngine.WakeUpCallback> mWakeUpCallbackListener = null;
	private static List<VoiceEngine.RecognitionCallback> mCallbackListener = null;
	private static List<VoiceEngine.TTSCallback> mTTSCallbackListener = null;

	private Dialog mCurrentDialog;		// 当前执行的对话
	private String mCurrentAnswer;
	private String mCurrentAnswerType;
	private ErrorType mCurrentDialogError;
	private SemanticProtocolResult mCurrentSemanticProtocolResult;
	private static Queue<TtsInfo> mTtsSpeakList = new ArrayDeque<TtsInfo>();	// 按播放顺序记录每段TTS播放完后是否需要发起识别
//	private boolean mStartPendingRecognizer;

	private VoiceEngine.State mState = VoiceEngine.State.STATE_READY;

	private TtsSpeaker mTTS = TtsSpeaker.getInstance();
	private YunZhiShengAdapter mVoiceAdapter;

	Timer timer;
	private AudioPlayer mPlayer;

	//
	// 方法体
	//
	private VoiceEngine(Context context) {
		mContext = context;
		mVoiceAdapter = YunZhiShengAdapter.getInstance(context, this);
	}
	
	public void testYZS(String str){
		String netStr = "无网络\n";
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
		NetworkInfo activeNet = cm.getActiveNetworkInfo();
		if (activeNet != null) {
			netStr = "网络:"+activeNet.getTypeName() + ", " + activeNet.getState()+"\n";
		}
		netStr += "-----******-----\n";
		Intent intent = new Intent(CommonApps.BROADCAST_TEST_VOICE_RESULT);
		intent.putExtra(CommonApps.BROADCAST_TEST_VOICE_STRING,netStr+str);
		mContext.sendBroadcast(intent);
	}

	public static VoiceEngine getInstance(Context context) {
		if (mInstance == null)
			mInstance = new VoiceEngine(context);
		return mInstance;
	}

	public synchronized void addMsgHandler(Messenger handler) {
		if (!mHandler.contains(handler)) {
			mHandler.add(handler);
			LogUtil.d(LOG_TAG, "addMsgHandler " + handler);
		}
	}

	public synchronized void removeMsgHandler(Messenger handler) {
		mHandler.remove(handler);
		LogUtil.d(LOG_TAG, "removeMsgHandler " + handler);
	}

	public synchronized void registerWakeUpListener(WakeUpCallback listener) {
		if (mWakeUpCallbackListener == null) {
			mWakeUpCallbackListener = new ArrayList<VoiceEngine.WakeUpCallback>();
		}
		if (!mWakeUpCallbackListener.contains(listener)) {
			mWakeUpCallbackListener.add(listener);
		}
	}

	public synchronized void unregisterWakeUpListener(WakeUpCallback listener) {
		if (mWakeUpCallbackListener != null) {
			mWakeUpCallbackListener.remove(listener);
		}
	}

	public synchronized void registerListener(RecognitionCallback listener) {
		if (mCallbackListener == null) {
			mCallbackListener = new ArrayList<VoiceEngine.RecognitionCallback>();
		}
		if (!mCallbackListener.contains(listener)) {
			mCallbackListener.add(listener);
		}
	}

	public synchronized void unregisterListener(RecognitionCallback listener) {
		if (mCallbackListener != null) {
			mCallbackListener.remove(listener);
		}
	}

	public synchronized void registerTTSListener(TTSCallback listener) {
		if (mTTSCallbackListener == null) {
			mTTSCallbackListener = new ArrayList<VoiceEngine.TTSCallback>();
		}
		if (!mTTSCallbackListener.contains(listener)) {
			mTTSCallbackListener.add(listener);
		}
	}

	public synchronized void unregisterTTSListener(TTSCallback listener) {
		if (mTTSCallbackListener != null) {
			mTTSCallbackListener.remove(listener);
		}
	}

	public boolean INITDONE = false;
	public void startEngine(String resPath) {
		LogUtil.d(LOG_TAG, "startEngine...");

		long start = System.currentTimeMillis();
		mVoiceAdapter.init();
		LogUtil.v(LOG_TAG, "Interaction Voice Engine started, total used " + (System.currentTimeMillis() - start) + " ms.");

		start = System.currentTimeMillis();
		// Init TTS
		String filename = resPath + "/Resource.irf";
		LogUtil.d(LOG_TAG, "初始化TTS, load resource file: " + filename);
		Tts.JniInit(filename);
		// Register listener
		mTTS.setCallbackListener(this);
		mTTS.start();
		LogUtil.d(LOG_TAG, "TTS引擎初始化完成.");

		mPlayer = new AudioPlayer(mContext);
		mPlayer.init();
		
		INITDONE = true;
		LogUtil.v(LOG_TAG, "TTS Engine started, total used " + (System.currentTimeMillis() - start) + " ms.");
	}

	public void stopEngine() {
		LogUtil.d(LOG_TAG, "stopEngine...");
		INITDONE = false;

		mVoiceAdapter.quit();

		// Stop TTS
		Tts.JniStop();
		mTTS.stop();
		// 解除对象引用，以便GC回收内存
		mTTS.setCallbackListener(null);

		mPlayer.release();
	}

	public void changeState(State destState) {
		LogUtil.i(LOG_TAG, "************************prepare change state: " + mState + " ==> " + destState);

		if ((mState == State.STATE_READY ||
			 mState == State.STATE_SUCCESS ||
			 mState == State.STATE_ERROR) &&
			destState == State.STATE_RECOGNITION)
		{
			onStateRecognition();
		}
		else if (mState == State.STATE_RECOGNITION &&
				 destState == State.STATE_SEARCH)
		{
			onStateSearch();
		}
		else if ((mState == State.STATE_RECOGNITION ||
				  mState == State.STATE_SEARCH) &&
				 destState == State.STATE_SUCCESS)
		{
			onStateSuccess();
		}
		else if ((mState == State.STATE_RECOGNITION ||
				  mState == State.STATE_SEARCH) &&
				 destState == State.STATE_ERROR)
		{
			onStateError();
		}

		mState = destState;
		LogUtil.i(LOG_TAG, "state changed out : " + destState);
	}

	void onStateRecognition() {
		// Play hint sound
		final long time_consuming_start = System.currentTimeMillis();

		mPlayer.playResource(R.raw.altair, new AudioPlayer.OnCompleteListener(){
			@Override
			void onPlayCompleted(){
			    LogUtil.d(LOG_TAG, "VoiceEngine.java::onStateRecognition=> 播放声音耗时=>[" + (System.currentTimeMillis()-time_consuming_start) + "] ms.");			    
			}
		});
		startRecognition();
	}

	void onStateSearch() {
		LogUtil.i(LOG_TAG,"onStateSearch ");
		if (mCurrentSemanticProtocolResult != null) {
			doSendMessage(CommonMessage.VoiceEngine.SEARCH_BEGIN, null);

			String searchType = mCurrentSemanticProtocolResult.type;
			LogUtil.i(LOG_TAG,"onStateSearch searchType="+searchType);
			if (searchType.equals(AnswerType.LOCATION.value)) {
				// search POI
				Intent poiIntent = new Intent(TSDEvent.Navigation.POI_SEACH);
				poiIntent.putExtra("searchCity", mCurrentSemanticProtocolResult.result[0]);
				poiIntent.putExtra("searchPOI", mCurrentSemanticProtocolResult.result[1]);
				mContext.sendBroadcast(poiIntent);
			} else if (searchType.equals(AnswerType.MUSIC.value)) {
				// search music
				new MusicSearchTask().execute(mCurrentSemanticProtocolResult.result[0],
						mCurrentSemanticProtocolResult.result[1],
						mCurrentSemanticProtocolResult.result[2]);
			}else{
				LogUtil.i(LOG_TAG,"onStateSearch no searchType");
			}

			mCurrentSemanticProtocolResult = null;
		}
	}

	void onStateSuccess() {
		synchronized (this) {
			if (mCallbackListener != null) {
				for (RecognitionCallback callback : mCallbackListener) {
					LogUtil.v(LOG_TAG, "通知监听对象: " + callback);
					callback.onInteractSuccessful(mCurrentAnswerType, mCurrentAnswer);
				}
			}
			mCurrentAnswer = null;
//			mCurrentDialogAnswerData = null;
		}
	}

	void onStateError() {
		// Send the result back to service
		Bundle data = new Bundle();
		data.putString("result", mCurrentDialogError.value);
		doSendMessage(CommonMessage.VoiceEngine.RECOGNITION_ERROR, data);

		synchronized (this) {
			if (mCallbackListener != null && mCurrentDialogError != null) {
				for (RecognitionCallback callback : mCallbackListener) {
					LogUtil.v(LOG_TAG, "通知监听对象: " + callback);
					callback.onInteractFailed(mCurrentDialogError);
				}
			}
			mCurrentDialogError = null;
		}
	}

//	void onStateCancel() {
//		synchronized (this) {
//			if (mCallbackListener != null) {
//				for (RecognitionCallback callback : mCallbackListener) {
//					LogUtil.v(LOG_TAG, "通知监听对象: " + callback);
//					callback.onInteractCancelled();
//				}
//			}
//		}
//	}

	//
	// Public interfaces
	//

	public void startWakeUpListening() {
		LogUtil.v(LOG_TAG, "startWakeUpListening xxxxxxxxxxxxxxxxxxxxx");
		mVoiceAdapter.startWakeUpListening();
	}

	public void stopWakeUpListening() {
		LogUtil.v(LOG_TAG, "stopWakeUpListening xxxxxxxxxxxxxxxxxxxxx");
		mVoiceAdapter.stopWakeUpListening();
	}

	public void startRecognition() {
		mVoiceAdapter.startRecognition();
	}

	public void stopRecognition() {
		mVoiceAdapter.stopRecognition();
	}

	public void cancelRecognition() {
		mVoiceAdapter.cancelRecognition();
	}

	public void ttsPlay(String text, String appName, int id,
			boolean needToNotify) {
		ttsPlay(text, appName, id, false, needToNotify);
	}

	public void ttsPlay(Speech text, boolean needToNotify) {
		ttsPlay(text.getContent(), null, 0, needToNotify);
	}

	public void ttsStop() {
		LogUtil.v(LOG_TAG, "TTS Stop called.");
		mTTS.stopCurrentVoice();
	}

	public void ttsClear() {
		mTTS.clear();
	}

	public void ttsPause() {
		// TODO Auto-generated method stub
		
	}

	public void ttsResume() {
		// TODO Auto-generated method stub
		
	}

	//
	// Interaction relative interfaces
	//

	public void startDialog(Dialog a) {
		mCurrentDialog = a;
		LogUtil.v(LOG_TAG, "startDialog " + mCurrentDialog);

		// 判断是否为新发起的对话，若是新发起的对话则先播放提示语；
		// 否则为识别错误重新识别，此时不需要播放提示语。但此时需判断是否需要立即开始识别
		// 还是等错误提示播完后再开始识别
		Speech question = a.getQuestion();
		if (question != null) {
			playSpeech(question);
		} else {
			changeState(State.STATE_RECOGNITION);
		}
	}

	public void finishDialog(String type, String answer) {
		if (mCurrentDialog != null) {
			Log.d(LOG_TAG, "finishDialog(), type=" + type + ", answer=" + answer);
			preFinishInteraction();

			mCurrentAnswerType = type;
			mCurrentAnswer = answer;
			changeState(State.STATE_SUCCESS);
		}
	}

	public void stopDialog(ErrorType reason) {
		if (mCurrentDialog != null) {
			Log.d(LOG_TAG, "stopDialog "+reason);
			// 首先取消当前语音识别
			cancelRecognition();
			// 然后结束当前对话
			mCurrentDialog = null;
			// 最后通知上层交互结束
			mCurrentDialogError = reason;
			changeState(State.STATE_ERROR);
		}else{
			Log.d(LOG_TAG, "stopDialog FINISH_ACTIVITY");
			Intent intent = new Intent(TSDEvent.Interaction.FINISH_ACTIVITY);
			mContext.sendBroadcast(intent);
		}
	}

	@Override
	public void onTtsEnd() {
		Log.d(LOG_TAG, "onTtsEnd");
		synchronized (this) {
			if (mTTSCallbackListener != null) {
				for (TTSCallback callback : mTTSCallbackListener) {
					callback.onPlayEnd();
				}
			}
		}

		TtsInfo info = mTtsSpeakList.poll();
		if (info.app != null && !info.app.isEmpty()) {
			// 广播TTS播报完成
			Intent intent = new Intent(CommonMessage.TTS_PLAY_FINISHED);
			intent.putExtra("package", info.app);
			intent.putExtra("id", info.id);
			mContext.sendBroadcast(intent);
		}

		if (info.isDialog) {
			continueDialogAfterSpeech();
		} else {
			// 若当前不在执行交互，则恢复唤醒监听 2015-5-12
			if (mVoiceAdapter != null && !mVoiceAdapter.getWakeUpRecordingState()) {
				mVoiceAdapter.startWakeUpListening();
			}
		}
	};

	//
	// YunZhiShengAdapter callbacks
	//

	void onWakeUp(String word) {
		synchronized (this) {
			if (mWakeUpCallbackListener != null) {
				for (WakeUpCallback callback : mWakeUpCallbackListener) {
					callback.onWakeUp(word);
				}
			}
		}
	}
	
	void onStart(){
		
	}

	void onStartRecording() {
		doSendMessage(CommonMessage.VoiceEngine.RECORDING_START, null);
	}

	void onStopRecording() {
		doSendMessage(CommonMessage.VoiceEngine.RECORDING_STOP, null);
	}

	void onVolume(int volume) {
		Bundle data = new Bundle();
		data.putInt("volume", volume);
		doSendMessage(CommonMessage.VoiceEngine.RECORDING_VOLUME, data);
	}

	void onStartRecognition() {
		doSendMessage(CommonMessage.VoiceEngine.RECOGNITION_START, null);
	}


	private boolean checkNetwork() {
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNet = cm.getActiveNetworkInfo();
		LogUtil.d(LOG_TAG, "checkNetwork: ");
		if (activeNet != null) {
			return true;
		} else {
			return false;
		}
	}
	
	void onFinishRecognition1(String result){
		LogUtil.w(LOG_TAG, "onFinishRecognition1");
		preFinishInteraction();
		
		if(checkNetwork()){
			mCurrentDialogError = ErrorType.ERR_RECOG;
		}else{
			mCurrentDialogError = ErrorType.ERR_NET;
		}
		
		changeState(State.STATE_ERROR);
	}

	private String mRecognitionResult = null;
	void onFinishRecognition(String result, boolean isSemantic) {
		LogUtil.d(LOG_TAG, "onFinishRecognition, result = " + result + ", isSemantic = " + isSemantic);

		// 若当前无交互则忽略
		if (mCurrentDialog != null) {
			// User doesn't speak
			if (checkEmptyAnswer(result)) {
				preFinishInteraction();

				mCurrentDialogError = ErrorType.ERR_NO_SPEECH;
				changeState(State.STATE_ERROR);
				return;
			}

			boolean finish = false, success = false, gotoSearch = false;
			if (!isSemantic) {	// phonetic
				// Send the result back to service
				/*Bundle data = new Bundle();
				data.putString("result", result);
				doSendMessage(CommonMessage.VoiceEngine.RECOGNITION_COMPLETE, data);*/
				mRecognitionResult = result;

				if ( !checkCommandAnswer(result) ) {
					LogUtil.d(LOG_TAG, "wait for the following analysed result...");
					try {
						if (timer != null) {
							Log.v(LOG_TAG, "cancel the pervious timer...");
							timer.cancel();
							timer = null;
						}
						// 为了防止后续没有分析结果到达，这里需要设一个超时机制防止交互无法结束
						TimerTask timeoutTask = new TimerTask() {
							@Override
							public void run() {
								LogUtil.w(LOG_TAG, "Time is out, cancel the recognition.");
								mVoiceAdapter.cancelRecognition();

								mCurrentDialogError = ErrorType.ERR_SEARCH;
								changeState(State.STATE_ERROR);
							}
						};
						timer = new Timer("TimeoutTask", true);
						timer.schedule(timeoutTask, 10000);
						Log.v(LOG_TAG, "schedule a new timer...");
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
				} else {
					finish = true;
					success = true;
				}
			} else {	// semantic
				if ( !checkSemanticAnswer(result) ) {
					mCurrentDialogError = ErrorType.ERR_NO_MATCH_ANSWER;
					finish = true;
					success = false;
				} else {
					// need to switch to search state
					finish = false;
					gotoSearch = true;
				}
				Bundle data = null;
				if(gotoSearch){
					
					data = new Bundle();
					data.putString("result", mRecognitionResult);
				}
				if(mCurrentSemanticProtocolResult != null && mCurrentSemanticProtocolResult.type.equals("act_open")){
					Intent intent = new Intent();
					intent.setAction(CommonMessage.VOICE_COMM_OPEN_WIFI_AP);
					mContext.sendBroadcast(intent);
				}
				else
					doSendMessage(CommonMessage.VoiceEngine.RECOGNITION_COMPLETE, data);
			}
			if (finish) {
				preFinishInteraction();

				if (success) {
					changeState(State.STATE_SUCCESS);
				} else {
					changeState(State.STATE_ERROR);
				}
			} else if (gotoSearch) {
				changeState(State.STATE_SEARCH);
			}
		}
	}

	public void onTalkStart() {
//		doSendMessage(VoiceAssistant.VOICEASSISTANT_START_INTERACTION, null);
		
		LogUtil.w(LOG_TAG," onTalkStart BACK_TO_LISTENING="+VoiceAssistant.BACK_TO_LISTENING
				+"BACK_TO_WAKING = "+VoiceAssistant.BACK_TO_WAKING);

	}
	
	void onFailedRecognition(int code, String error) {
		LogUtil.w(LOG_TAG, "onFailedRecognition, code=" + code + ", error=" + error);
		preFinishInteraction();

		mCurrentDialogError = ErrorType.ERR_RECOG;
		changeState(State.STATE_ERROR);
	}

	void onCancelRecognition() {
		LogUtil.d(LOG_TAG, "onCancelRecognition");
		preFinishInteraction();

		mCurrentDialogError = ErrorType.ERR_USER_CANCELLED;
		changeState(State.STATE_ERROR);
	}

	void onSearchResult(String result) {
		LogUtil.d(LOG_TAG, "onSearchResult, result=" + result);

		if (mCurrentDialog != null) {
			// 检查返回数据中是否包含"error"，若不包含则为结果数据，通知UI进行显示；
			// 若包含则为错误，结束交互
			if ( !result.matches(".+error.+") ) {
				Bundle param = new Bundle();
				param.putString("result", result);
				doSendMessage(CommonMessage.VoiceEngine.SEARCH_END, param);

				if (timer != null) {
					timer.cancel();
					timer = null;
				}
			} else {
				preFinishInteraction();
				mCurrentDialogError = ErrorType.ERR_SEARCH;
				changeState(State.STATE_ERROR);
			}
		}
	}

	private void continueDialogAfterSpeech() {
		Log.d(LOG_TAG, "continueDialogAfterSpeech, currentDialog = " + mCurrentDialog);
		if (mCurrentDialog != null) {
			String[] expAnswers = mCurrentDialog.getExpectedKeyword();
			if (expAnswers != null && expAnswers.length > 0) {
				// 当有期望答案时为正常的dialog处理，
				changeState(State.STATE_RECOGNITION);
			} else {
				// 当模板没有期望答案时直接返回
				mCurrentDialogError = ErrorType.ERR_USER_CANCELLED;
				changeState(State.STATE_ERROR);
			}
		}
	}

	private void preFinishInteraction() {
		LogUtil.d(LOG_TAG, "preFinishInteraction");
		// mCurrentDialog要在回调VoiceAssistant之前执行,
		// 否则有可能会出现后面的Dialog交互不能正确执行的问题.
		// 其原因是在VoiceAssistant.onInteractFail()或onInteractSuccessful()中最后会调用notify()恢复被
		// 挂起的线程，但此时有可能mCurrentDialog = null尚未被执行，当此线程重新获得执行权时，置空操作有可能会将本
		// 该是下一次交互的Dialog对象冲掉，从而导致交互不能继续。
		mCurrentDialog = null;

		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	private void playSpeech(Speech speech) {
		Log.v(LOG_TAG, "playSpeech");
		if (speech.getMode().equals("audio")) {
			mPlayer.playUrl(speech.getContent(), new AudioPlayer.OnCompleteListener() {
				@Override
				void onPlayCompleted() {
					continueDialogAfterSpeech();
				}
			});
		} else {
		    ttsPlay(speech.getContent(), null, 0, true, true);
		}			
	}
	/**
	 * Play text by TTS
	 * @param text
	 * @param startRecAfterPlay true -- 表示需要在TTS结束后发起对话;
	 *                  false -- 表示单独的TTS播放
	 */
	private void ttsPlay(String text, String appName, int id, boolean startRecAfterPlay, boolean needToNotify) {
		LogUtil.v(LOG_TAG, "TTS play: " + text + ", " + startRecAfterPlay);

		// 播放TTS前先将唤醒服务停止，以防止TTS引起误唤醒。唤醒服务会在TTS播放完成后再恢复。2015-5-12
		if (mVoiceAdapter != null && mVoiceAdapter.getWakeUpRecordingState()) {
			mVoiceAdapter.stopWakeUpListening();
		}

		TtsInfo info = new TtsInfo(appName, id, text);
		info.isDialog = startRecAfterPlay;
		mTtsSpeakList.add(info);

		mTTS.talk(text);

		if (needToNotify) {
			Bundle data = new Bundle();
			data.putString("hint", text);
			doSendMessage(CommonMessage.VoiceEngine.TTS_PLAY_BEGIN, data);
		}
	}

	private void doSendMessage(int message, Bundle data) {
		// Send the result back to service
		Message msg = Message.obtain(null, message);
		LogUtil.d(LOG_TAG, "doSendMessage ");
		if (data != null){
			LogUtil.d(LOG_TAG, "doSendMessage setData");
			msg.setData(data);
		}

		synchronized (this) {
			if (mHandler != null) {
				LogUtil.d(LOG_TAG, "doSendMessage mHandler size = "+mHandler.size());
				try {
					for (Messenger h : mHandler) {
						LogUtil.d(LOG_TAG, "doSendMessage to " + h);
						h.send(msg);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean checkEmptyAnswer(String answer) {
		if (answer == null || answer.isEmpty()) {
			return true;
		}
		return false;
	}

	private boolean checkCommandAnswer(String candidateAnswer) {
		Log.v(LOG_TAG, "checkRawAnswer");
		boolean foundAnswer = false, containSearch = false;

		// Check if the result is matching to a voice command
		String cmdAction = VoiceCommand.map.get(candidateAnswer);
		cmdAction = cmdAction == null ? candidateAnswer : cmdAction;

		String[] expAnswers = mCurrentDialog.getExpectedKeyword();
		for (String ans : expAnswers) {
			if (ans.startsWith("#")) {
				containSearch = true;
			}

			if (ans.equals(cmdAction)) {
				LogUtil.d(LOG_TAG, "找到匹配的语音指令: " + ans);
				mCurrentAnswer = ans;
				mCurrentAnswerType = AnswerType.COMMAND.value;
				foundAnswer = true;
			}
		}

		return foundAnswer ? true : containSearch ? false : true;
	}

	private boolean checkSemanticAnswer(String protocol) {
		Log.v(LOG_TAG, "checkProtocolAnswer");
		boolean foundAnswer = false;
		SemanticProtocolResult analyzedAnswer = null;
		
		// 从识别的结果中解析出语义分析结果，然后从中提取目的地地址
		JSONTokener jsonTokener = new JSONTokener(protocol);
//		LogUtil.v(LOG_TAG, "json = " + jsonTokener);
		try {
			Object obj = jsonTokener.nextValue();
			if (obj != null) {
				if (obj instanceof JSONObject) {
					analyzedAnswer = handleProtocal((JSONObject)obj);
				} else if (obj instanceof JSONArray) {
					// 同音字时多个返回结果，每一项解析参照上面的JSONObject
				}
			}
		} catch (JSONException e) {
			LogUtil.e(LOG_TAG, "Exception raised at checkProtocolAnswer(), message: " + e.getMessage());
			e.printStackTrace();
		}

		if (analyzedAnswer != null) {
			// check the expected answer type, then start search if necessary
			String[] expAnswers = mCurrentDialog.getExpectedKeyword();
			for (String ans : expAnswers) {
				if (ans.equals(analyzedAnswer.type)) {
					// found match
					mCurrentSemanticProtocolResult = analyzedAnswer;
					foundAnswer = true;
					break;
				}
			}
			
			if (analyzedAnswer.type.equals("act_open")){
				mCurrentSemanticProtocolResult = analyzedAnswer;
				foundAnswer = true;
			}
		}
		Log.v(LOG_TAG, "checkProtocolAnswer foundAnswer="+foundAnswer);
		return foundAnswer;
	}

	private void checkError(String error) {
		LogUtil.v(LOG_TAG, "onSpeechEnd, error = " + error);
	}

	private void broadcastVoiceAction(String action) {
		Intent intent = new Intent();
		intent.setAction(VoiceCommand.map.get(action));
		mContext.sendBroadcast(intent);
		LogUtil.d(LOG_TAG, "广播语音指令: " + intent.getAction());
	}

	private SemanticProtocolResult handleProtocal(JSONObject json) throws JSONException {
		SemanticProtocolResult r = null;
		String service = json.optString("service");
		String code = json.optString("code");

		LogUtil.v(LOG_TAG, "service = " + service + ", code = " + code);

		// 路线和位置
		if (service.equals("cn.yunzhisheng.map") &&
			(code.equals("ROUTE") || code.equals("POSITION")))
		{
			JSONObject obj = json.getJSONObject("semantic").getJSONObject("intent");
			if (obj != null) {
				String toCity = obj.optString("toCity");
				String toPOI = obj.optString("toPOI");

				if (toCity.equals("CURRENT_CITY"))	// 当前城市
					toCity = "";
				if (toPOI.equals("CURRENT_LOC"))	// 当前位置
					toPOI = "";

				r = new SemanticProtocolResult(AnswerType.LOCATION.value, new String[]{	toCity, toPOI });
			} else {
				// Normally, we should never reach here.
				LogUtil.w(LOG_TAG, "Json object is null, ignore...");
			}
		}
		// 周边搜索
		else if (service.equals("cn.yunzhisheng.localsearch") &&
				(code.equals("BUSINESS_SEARCH") || code.equals("DEAL_SEARCH") ||
				 code.equals("NONBUSINESS_SEARCH")))
		{
			JSONObject obj = json.getJSONObject("semantic").getJSONObject("intent");
			if (obj != null) {
				r = new SemanticProtocolResult(AnswerType.LOCATION.value, new String[]{
						"", // Current city
						obj.optString("category")});
			} else {
				// Normally, we should never reach here.
				LogUtil.w(LOG_TAG, "Json object is null, ignore...");
			}
		}
		// 音乐
		else if (service.equals("cn.yunzhisheng.music") &&
				(code.equals("SEARCH_SONG") || code.equals("SEARCH_ARTIST") ||
				 code.equals("SEARCH_RANDOM") || code.equals("SEARCH_BILLBOARD")))
		{
			JSONObject obj = json.getJSONObject("semantic").getJSONObject("intent");
			if (obj != null) {
				r = new SemanticProtocolResult(AnswerType.MUSIC.value, new String[]{
						obj.optString("song"),
						obj.optString("artist"),
						obj.optString("genre")
				});
			} else {
				// Normally, we should never reach here.
				LogUtil.w(LOG_TAG, "Json object is null, ignore...");
			}
		}
		// 新闻
		else if (service.equals("cn.yunzhisheng.news") && code.equals("SEARCH"))
		{
			JSONObject obj = json.getJSONObject("semantic").getJSONObject("intent");
			if (obj != null) {
				r = new SemanticProtocolResult(AnswerType.MUSIC.value, new String[]{
						obj.optString("section"),
						"",
						""
				});
			} else {
				// Normally, we should never reach here.
				LogUtil.w(LOG_TAG, "Json object is null, ignore...");
			}
		}
		else if (service.equals("cn.yunzhisheng.setting")) {
			JSONObject obj = json.getJSONObject("semantic").getJSONObject("intent");
			if(obj != null) {
				String strOperaror = obj.getString("operator");
				String strOperands = obj.getString("operands");
				if(strOperaror != null && strOperands != null && strOperaror.equals("ACT_OPEN") 
						&& strOperands.equals("OBJ_WIFI_SPOT")){
					r = new SemanticProtocolResult("act_open", new String[]{
							"",
							"",
							""
					});
				}
			} else {
				// Normally, we should never reach here.
				LogUtil.w(LOG_TAG, "Json object is null, ignore...");
			}
		}

		return r;
	}

	private class TtsInfo {
		String app;			// 请求TTS播报的程序名称
		int id;			// 请求播报内容的序列号
		String contentText;	// 播报内容
		boolean isDialog;

		TtsInfo(String app, int id, String text) {
			this.app = app;
			this.id = id;
			this.contentText = text;
		}
	}

	private class SemanticProtocolResult {
		String type;
		String[] result;
		SemanticProtocolResult(String type, String[] result) {
			this.type = type;
			this.result = result;
		}
	}

	private class MusicSearchTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			if (params != null && params.length == 3) {
				String name = params[0];
				String author = params[1];
				String genre = params[2];
				LogUtil.w(LOG_TAG, "MusicSearchTask name="+name+" author="+author+" genre="+genre);
				String result = JsonOA2.getInstance(mContext).queryAudio(name, author, genre);
				LogUtil.w(LOG_TAG, "MusicSearchTask result="+result);
				if (result != null && !result.matches(".+errorCode.+")) {
					try {
						JSONObject data = new JSONObject(result);
						JSONObject outData = new JSONObject();
						String type = data.getString("type");

						outData.put("result", "ok");
						outData.put("type", type);

						if (type.equals("music")) {
							outData.put("data", data.getJSONArray("items"));
						} else {
							outData.put("data", data.getJSONArray("albums"));
						}

						onSearchResult(outData.toString());
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}else{
					LogUtil.w(LOG_TAG, "MusicSearchTask error result="+result);
					preFinishInteraction();
					mCurrentDialogError = ErrorType.ERR_SEARCH;
					changeState(State.STATE_ERROR);
					/*Bundle data = new Bundle();
					data.putString("result","关键词无效");
					doSendMessage(CommonMessage.VoiceEngine.SEARCH_BEGIN, data);*/
				}
			}
			return null;
		}
		
	}

	private static class AudioPlayer implements OnCompletionListener, OnErrorListener, OnPreparedListener {
		private Context context;
		private MediaPlayer player;
		private OnCompleteListener listener;

		static abstract class OnCompleteListener {
			abstract void onPlayCompleted();
		}

		AudioPlayer(Context context) {
			this.context = context;
		}

		void init() {
			player = new MediaPlayer();
			player.setOnCompletionListener(this);
			player.setOnErrorListener(this);
			player.setOnPreparedListener(this);
		}

		void release() {
			player.release();
		}

		void playUrl(String url, OnCompleteListener listener) {
			Log.d(LOG_TAG, "AudioPlayer.playUrl");
			this.listener = listener;
			player.reset();
			try {
				player.setDataSource(url);
				player.prepareAsync();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		void playResource(int resid, OnCompleteListener completeAction) {
			Log.d(LOG_TAG, "AudioPlayer.playResource");
			this.listener = completeAction;
			player.reset();
	        try {
	            AssetFileDescriptor afd = context.getResources().openRawResourceFd(resid);
	            if (afd == null)
	            	return;

	            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
	            afd.close();
				player.prepareAsync();
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			Log.d(LOG_TAG, "AudioPlayer.onPrepared");
			mp.start();
		}

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Log.e(LOG_TAG, "AudioPlayer.onError, what=" + what + ", extra=" + extra);
			mp.stop();
			return false;
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			Log.d(LOG_TAG, "AudioPlayer.onCompletion");
			mp.stop();
			if (listener != null) {
				listener.onPlayCompleted();
				listener = null;
			}
		}
	}
}

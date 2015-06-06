package com.tuyou.tsd.voice.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDComponent;
import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.voice.R;
import com.tuyou.tsd.voice.service.VoiceEngine.AnswerType;
import com.tuyou.tsd.voice.service.VoiceEngine.ErrorType;
import com.tuyou.tsd.voice.service.interaction.Action.FailedAction;
import com.tuyou.tsd.voice.service.interaction.Action.SuccessfulAction;
import com.tuyou.tsd.voice.service.interaction.Dialog;
import com.tuyou.tsd.voice.service.interaction.InteractionParser;
import com.tuyou.tsd.voice.service.interaction.Jump;
import com.tuyou.tsd.voice.service.interaction.Jump.HardKeyFunction;
import com.tuyou.tsd.voice.service.interaction.Scene;

/**
 * 语音助手服务
 * @author ruhai
 * 2014-7
 */
public class VoiceAssistant extends Service implements VoiceEngine.WakeUpCallback {
	static final String WAKE_UP_COMMAND_1 = "小宝小宝";
	static final String WAKE_UP_COMMAND_2 = "小宝拍照";
	static final String WAKE_UP_COMMAND_3 = "小宝闭嘴";
	static final String WAKE_UP_COMMAND_OPEN_WIFI_AP = "打开热点";

	private static final String LOG_TAG = "VoiceAssistant";

	private static final boolean DEBUG = false;
	private static final int ENG_DIALOG = 999;	// Magic number

	private static Map<String, Scene> mInteractionMap = null;
	private static VoiceEngine mEngine = null;

	private static InteractionExecuteThread mInteractionThread = null;
	private BroadcastReceiver mReceiver = null;

	private static List<Messenger> mClients = new ArrayList<Messenger>();

	private static Object mLock = new Object();
	private static Vector<Scene> mSceneList = new Vector<Scene>();

	private boolean mInitialized;
	
	private enum State {
		STATE_NONE,
		STATE_LISTENING,	// 唤醒监听状态
		STATE_INTERACTING,	// 交互状态
	}
	private static State mState = State.STATE_NONE;
	public static boolean BACK_TO_LISTENING = false;
	public static boolean BACK_TO_WAKING = false;
	public static final int CMD_VOICE_LISTEN = 0x1001;
	public static final String INTO_SEARCH_VIEW = "com.tuyou.tsd.voice.service.searchview";
	public volatile static boolean mbStopInSearchView = false;

	void changeState(State destState) {
		LogUtil.i(LOG_TAG, "prepare to change state: " + mState + " ==> " + destState);
		if ((mState == State.STATE_NONE || mState == State.STATE_INTERACTING) &&
		    destState == State.STATE_LISTENING)
		{
			// start to listen
			onStateListening();
		}
		else if (mState == State.STATE_LISTENING && destState == State.STATE_INTERACTING) {
			// start to interact
			onStateInteracting();
		}
		mState = destState;
		LogUtil.i(LOG_TAG, "state changed: " + destState);
	}

	public static State getState() {
		return mState;
	}

	private void onStateListening() {
		if (mEngine != null) {
			LogUtil.e(LOG_TAG,"startWakeUpListening xxxxxxxxxxxxxxxxxxxxxxx");
			mEngine.startWakeUpListening();
		} else {
			LogUtil.e(LOG_TAG, "VoiceEngine didn't initialize properly.");
			throw new RuntimeException("VoiceEngine didn't initialize properly.");
		}
	}

	private void onStateInteracting() {
		if (mEngine != null) {
			LogUtil.e(LOG_TAG,"onStateInteracting xxxxxxxxxxxxxxxxxxxxxxx");
			mEngine.stopWakeUpListening();
		} else {
			LogUtil.e(LOG_TAG, "VoiceEngine didn't initialize properly.");
			throw new RuntimeException("VoiceEngine didn't initialize properly.");
		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	private static class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case CommonMessage.VoiceEngine.REGISTER_CLIENT:
				LogUtil.v(LOG_TAG, "Got the message VoiceEngine.REGISTER_CLIENT");
				if (mEngine != null) {
					synchronized (VoiceAssistant.class) {
						if (!mClients.contains(msg.replyTo)) {
							mClients.add(msg.replyTo);
							LogUtil.v(LOG_TAG, "mClients.add(msg.replyTo); " + msg.replyTo);
						}
					}
					mEngine.addMsgHandler(msg.replyTo);
				}
				break;

			case CommonMessage.VoiceEngine.UNREGISTER_CLIENT:
				LogUtil.v(LOG_TAG, "Got the message VoiceEngine.UNREGISTER_CLIENT");
				if (mEngine != null) {
					synchronized (VoiceAssistant.class) {
						mClients.remove(msg.replyTo);
						LogUtil.v(LOG_TAG, "mClients.remove(msg.replyTo); " + msg.replyTo);
					}
					mEngine.removeMsgHandler(msg.replyTo);
				}
				break;

			case CommonMessage.VoiceEngine.RECOGNITION_CANCEL:
				LogUtil.v(LOG_TAG, "Got the message VoiceEngine.RECOGNITION_CANCEL");
				if (mInteractionThread != null) {
					mInteractionThread.cancelCurrentInteraction();
				}
				break;

			case CommonMessage.VoiceEngine.START_WAKEUP_LISTENING:
				if (mEngine != null) {
					mEngine.startWakeUpListening();
				}
				break;

			case CommonMessage.VoiceEngine.STOP_WAKEUP_LISTENING:
				if (mEngine != null) {
					mEngine.stopWakeUpListening();
				}
				break;

			case CommonMessage.VoiceEngine.START_RECOGNITION:
				if (mEngine != null) {
					mEngine.startRecognition();
				}
				break;

			case CommonMessage.VoiceEngine.STOP_RECOGNITION:
				if (mEngine != null) {
					mEngine.stopRecognition();
				}
				break;

			case CommonMessage.VoiceEngine.CANCEL_RECOGNITION:
				if (mEngine != null) {
					mEngine.cancelRecognition();
				}
				break;

			default:
				super.handleMessage(msg);
			}
		}
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.d(LOG_TAG, "Receive the broadcast: " + action);

			// TTS相关消息
			if (action.equals(CommonMessage.TTS_PLAY)) {
				String app = intent.getStringExtra("package");
				int id = intent.getIntExtra("id", 0);
				String text = intent.getStringExtra("content");
				boolean notify = intent.getBooleanExtra("notify", false);
				LogUtil.d(LOG_TAG, "extra: [" + app + ", " + id + ", " + text + "]");

				if (text != null)
					ttsPlay(text, app, id, notify);
			}
			else if (action.equals(CommonMessage.TTS_STOP)) {
				if (mEngine != null)
					mEngine.ttsStop();
			}
			else if (action.equals(CommonMessage.TTS_PAUSE)) {
				if (mEngine != null)
					mEngine.ttsPause();
			}
			else if (action.equals(CommonMessage.TTS_CLEAR)) {
				if (mEngine != null)
					mEngine.ttsClear();
			}
			else if (action.equals(CommonMessage.TTS_RESUME)) {
				if (mEngine != null)
					mEngine.ttsResume();
			}
			// 推送消息
			else if(action.equals(TSDEvent.Push.MESSAGE_ARRIVED)) {
				String msg = intent.getStringExtra("message");
				handlePushMessage(msg);
			}
			// 交互消息
			else if (action.equals(TSDEvent.Interaction.RUN_INTERACTION)) {
				checkNetworkEnabled();
				
				LogUtil.w(LOG_TAG, "executeInteraction from RUN_INTERACTION");
				executeInteraction(intent.getStringExtra("template"), true);
			}
			// 交互过程中收到硬按键消息
			else if (action.equals(TSDEvent.System.HARDKEY1_PRESSED) ||
					action.equals(TSDEvent.System.HARDKEY2_PRESSED) ||
					action.equals(TSDEvent.System.HARDKEY3_PRESSED) ||
					action.equals(TSDEvent.System.HARDKEY4_PRESSED))
			{
				LogUtil.w(LOG_TAG, "MyBroadcastReceiver HARDKEY "+action);
//				onHardKeyPressed(action);
			}
			// 交互过程中收到触屏消息
			else if (action.equals(TSDEvent.Interaction.FINISH_INTERACTION_BY_TP)) {
				if (mEngine != null) {
					mEngine.finishDialog(intent.getStringExtra("type"), intent.getStringExtra("answer"));
				}
			}
			else if (action.equals(TSDEvent.Interaction.CANCEL_INTERACTION_BY_TP)) {
				boolean inSearchView = intent.getBooleanExtra(INTO_SEARCH_VIEW, false);
				if(inSearchView){
					mbStopInSearchView = true;
				}
				boolean gohome = intent.getBooleanExtra("gohome", false);
				if (mEngine != null) {
					mEngine.stopDialog(gohome ?
							ErrorType.ERR_USER_CANCELLED_AND_GO_HOME : ErrorType.ERR_USER_CANCELLED);
				}
				
				LogUtil.v(LOG_TAG, "CANCEL_INTERACTION_BY_TP inSearchView="+inSearchView+" gohome="+gohome);
			}
			// POI Search Result
			else if (action.equals(TSDEvent.Navigation.POI_SEARCH_RESULT)) {
				if (mEngine != null) {
					mEngine.onSearchResult(intent.getStringExtra("result"));
				}
			}
			else if (action.equals(TSDEvent.System.ACC_ON)) {
				if (mEngine != null) {
					mEngine.startWakeUpListening();
				}
			}
			else if (action.equals(TSDEvent.System.ACC_OFF)) {
				if (mEngine != null) {
					// Cancel the interaction if we are during in an interaction.
					// Otherwise the wake up message will be ignored by the engine when ACC turns on next time.
					mEngine.stopDialog(ErrorType.ERR_USER_CANCELLED);
					// Stop the wake up service after ACC is off.
					mEngine.stopWakeUpListening();
				}
			}
		}
	}

    /**
	 * Check the network connection and enable it if necessary
	 */
	@SuppressWarnings("unused")
	private boolean checkNetworkEnabled() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNet = cm.getActiveNetworkInfo();
		if (activeNet != null) {
			LogUtil.d(LOG_TAG, "Active network: " + activeNet.getTypeName() + ", " + activeNet.getState());
			return true;
		} else {
			LogUtil.w(LOG_TAG, "No active network found...");
			if (TSDConst.useGPRSNetwork && !HelperUtil.getMobileDataEnabled(this)) {
				LogUtil.d(LOG_TAG, "Trying to enable GPRS...");
				HelperUtil.setMobileDataEnabled(this, true);
			}
			return false;
		}

	}
	
	@Override
	public void onCreate() {
		LogUtil.v(LOG_TAG, "create VoiceAssistant service.");

		if (DEBUG)
			android.os.Debug.waitForDebugger();

		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			initService();

			mEngine.registerWakeUpListener(this);
			mInitialized = true;
			// Broadcast the service state changed
			sendBroadcast(new Intent(TSDEvent.Interaction.SERVICE_STARTED));

			changeState(State.STATE_LISTENING);
		} else {
			LogUtil.w(LOG_TAG, "No sdcard detected...");
			Toast.makeText(this, R.string.noExternalStorageMounted, Toast.LENGTH_LONG).show();
			// TODO: when sdcard is ready, re-init the service
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.v(LOG_TAG, "onStartCommand.");
		// 为避免CoreService发生异常重启后重新调用startService启动服务，此时由于服务已经启动，故不会有广播发出。
		// 导致CoreService的初始化检查不能完成，所以这里增加一个广播发送。
		if (mInitialized) {
			sendBroadcast(new Intent(TSDEvent.Interaction.SERVICE_STARTED));
		}
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	@Override
	public void onDestroy() {
		LogUtil.v(LOG_TAG, "service onDestroy...");
		if (mInteractionThread != null) {
			// Let the thread finished
			synchronized (mLock) { mLock.notifyAll(); }
			// Then stop the thread
			mInteractionThread.stop = true;
			mInteractionThread = null;
		}

		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		if (mEngine != null) {
			mEngine.unregisterWakeUpListener(this);
			mEngine.stopEngine();
			mEngine = null;
		}

		// Broadcast the service state changed
		sendBroadcast(new Intent(TSDEvent.Interaction.SERVICE_STOPPED));
		super.onDestroy();
	}

	@Override
	public void onWakeUp(String word) {
		Log.d(LOG_TAG, "onWakeUp: " + word);
		if (word.equals(WAKE_UP_COMMAND_1)) {
			Intent resultIntent = new Intent(CommonMessage.VOICE_COMM_WAKEUP);
			Log.d(LOG_TAG, "xiaobaoxiaoba");
			sendBroadcast(resultIntent);
		} else if (word.equals(WAKE_UP_COMMAND_2)) {
			Intent resultIntent = new Intent(CommonMessage.VOICE_COMM_TAKE_PICTURE);
			sendBroadcast(resultIntent);
		} else if (word.equals(WAKE_UP_COMMAND_3)) {
			Intent resultIntent = new Intent(CommonMessage.VOICE_COMM_SHUT_UP);
			sendBroadcast(resultIntent);
		} else if (word.equals(WAKE_UP_COMMAND_OPEN_WIFI_AP)) {
			Intent resultIntent = new Intent(CommonMessage.VOICE_COMM_OPEN_WIFI_AP);
			sendBroadcast(resultIntent);
		}
	}

	private void initService() {
		Log.v(LOG_TAG, "=========initService");
		long start = System.currentTimeMillis();

		if (checkResourceDir()) {
			prepareResources();
		}

		// Init the voice engine
		if (mEngine == null) {
			mEngine = VoiceEngine.getInstance(getApplicationContext());
			mEngine.startEngine(TSDConst.VOICE_ASSISTANT_PATH);
		}
		// Load the interact resources
		if (mInteractionMap == null) {
			mInteractionMap = new HashMap<String, Scene>();
			loadResources();
		}

		// Start the interaction execute thread
		if (mInteractionThread == null) {
			mInteractionThread = new InteractionExecuteThread(this);
			mInteractionThread.start();
			LogUtil.v(LOG_TAG, "create InteractionExecuteThread, tid=" + mInteractionThread.getId() + ", tnam=" + mInteractionThread.getName());
		} 

		// Register the broadcast
		IntentFilter filter = new IntentFilter();
		filter.addAction(TSDEvent.System.ACC_ON);
		filter.addAction(TSDEvent.System.ACC_OFF);
		filter.addAction(TSDEvent.Interaction.RUN_INTERACTION);

		filter.addAction(CommonMessage.TTS_PLAY);
		filter.addAction(CommonMessage.TTS_STOP);
		filter.addAction(CommonMessage.TTS_CLEAR);

		filter.addAction(TSDEvent.Push.MESSAGE_ARRIVED);

		filter.addAction(TSDEvent.System.HARDKEY1_PRESSED);
		filter.addAction(TSDEvent.System.HARDKEY2_PRESSED);
		filter.addAction(TSDEvent.System.HARDKEY3_PRESSED);
		filter.addAction(TSDEvent.System.HARDKEY4_PRESSED);

		filter.addAction(TSDEvent.Interaction.FINISH_INTERACTION_BY_TP);
		filter.addAction(TSDEvent.Interaction.CANCEL_INTERACTION_BY_TP);

		filter.addAction(TSDEvent.Navigation.POI_SEARCH_RESULT);
		
		mReceiver = new MyBroadcastReceiver();
		registerReceiver(mReceiver, filter);
		LogUtil.d(LOG_TAG, "service initialized, total used " + (System.currentTimeMillis() - start) + " ms.");
	}

	private boolean checkResourceDir() {
		boolean r = true;

		File resDir = new File(TSDConst.VOICE_ASSISTANT_PATH);
		if (!resDir.exists()) {
			r=resDir.mkdirs();
		}
		LogUtil.d(LOG_TAG,"checkResourceDir r="+r);
		return r;
	}

	private void prepareResources() {
		LogUtil.d(LOG_TAG, "准备将资源文件复制到SD卡上...");
		AssetManager assets = getResources().getAssets();
		copyResources(assets, "tts");
		copyResources(assets, "interact");
	}

	private void copyResources(AssetManager assets, String path) {
		try {
			String[] reslist = assets.list(path);
			for (int i = 0; i < reslist.length; i++) {
				File destFile = new File(TSDConst.VOICE_ASSISTANT_PATH + File.separator + reslist[i]);
				boolean r = HelperUtil.copyToFile(assets.open(path + File.separator + reslist[i]), destFile);
				LogUtil.d(LOG_TAG, "Copy " + reslist[i] + " " + (r ? "succeed." : "failed."));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadResources() {
		InputStream is = null;
		try {
			File resDir = new File(TSDConst.VOICE_ASSISTANT_PATH);
			String[] tempFileList = resDir.list();
			for (String filename : tempFileList) {
				if (!filename.endsWith(".json"))
					continue;

				LogUtil.v(LOG_TAG, "读取交互模板: " + filename);
				// use the org.json to parse
				is = new FileInputStream(TSDConst.VOICE_ASSISTANT_PATH + File.separator +filename);
				String content = loadRes(is);
				Scene scene = InteractionParser.parseScene(content);
				// for SDK 11 could use below method to parse
//				Scene scene = InteractionParser.parseScene(assets.open("interact/" + filename));
				if (scene != null) {
					LogUtil.d(LOG_TAG, "模板\"" + scene.getName() + "\"加载成功.");
					mInteractionMap.put(scene.getName(), scene);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {	
			LogUtil.e(LOG_TAG, "模板解析时发生异常: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {}
			}
		}
	}

//	private void unloadResources() {
//		if (mInteractionMap != null && !mInteractionMap.isEmpty()) {
//			mInteractionMap.clear();
//			mInteractionMap = null;
//		}
//	}

	private String loadRes(InputStream fileInputStream) {
		String content = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(fileInputStream));
			StringBuilder builder = new StringBuilder();
			String strLine;
			while ((strLine = reader.readLine()) != null) {
				builder.append(strLine);
			}
			content = builder.toString();
		} catch (IOException e) {
			LogUtil.w(LOG_TAG, "loadRes()发生异常: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try { reader.close(); } catch (IOException e) {}
			}
		}
		return content;
	}

	/**
	 * TTS播报
	 * @param text
	 */
	private void ttsPlay(String text, String app, int id, boolean needToNotify) {
		if (mEngine != null) {
			LogUtil.d(LOG_TAG, "ttsPlay with text: " + text);
			mEngine.ttsPlay(text, app, id, needToNotify);
		}
	}

	/**
	 * 执行交互
	 * @param eventMsg
	 */
	public volatile static boolean mbRunningInteraction = false;
	private void executeInteraction(String eventMsg, boolean isLocal) {
		if(mEngine==null || !mEngine.INITDONE){
			LogUtil.w(LOG_TAG, "executeInteraction from engine not ready");
			return;
		}
		LogUtil.w(LOG_TAG, "executeInteraction from mbRunningInteraction="+mbRunningInteraction);
		if(mbRunningInteraction){
			return ;
		}
		mbRunningInteraction = true;
		
		Scene scene = null;
		LogUtil.w(LOG_TAG, "executeInteraction isLocal="+isLocal);
		if (isLocal) {
			if (mInteractionMap != null) {
				if (mEngine == null)
					throw new RuntimeException("VoiceEngine未初始化");
	
				// Execute the interact scene
				scene = mInteractionMap.get(eventMsg);
			}
		} else {
			try {
				scene = InteractionParser.parseScene(eventMsg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		if (scene != null) {
			synchronized(mLock) {
				LogUtil.v(LOG_TAG, "将交互模板 \"" + scene.getName() + "\" 加入队列.");
				mSceneList.add(scene);
				mLock.notify();
			}
		} else {
			LogUtil.w(LOG_TAG, "交互模板不存在, 忽略...");
		}
	}

	private void handlePushMessage(String message) {
		JSONObject json;
		String module = null, content = null;
		try {
			json = new JSONObject(message);
			module = json.getString("module");

	    	if (module.equals(CommonMessage.PUSH_MESSAGE_TYPE_INTERACTION) && !mbRunningInteraction) {
				content = json.getJSONObject("content").toString();

				HelperUtil.startActivity(this, TSDComponent.VOICE_ASSISTANT_PACKAGE, TSDComponent.INTERACTION_ACTIVITY);
				
				LogUtil.w(LOG_TAG, "executeInteraction from handlePushMessage");
	    		executeInteraction(content, false);
	    	}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
	}

//	private void onHardKeyPressed(String keycode) {
//		String hardkey = "";
//		if (keycode.equals(TSDEvent.System.HARDKEY1_PRESSED)) {
//			hardkey = VoiceEngine.ANS_HK1;
//		} else if (keycode.equals(TSDEvent.System.HARDKEY2_PRESSED)) {
//			hardkey = VoiceEngine.ANS_HK2;
//		} else if (keycode.equals(TSDEvent.System.HARDKEY3_PRESSED)) {
//			hardkey = VoiceEngine.ANS_HK3;
//		} else if (keycode.equals(TSDEvent.System.HARDKEY4_PRESSED)) {
//			hardkey = VoiceEngine.ANS_HK4;
//		}
//		if (mEngine != null) {
//			mEngine.onFinishRecognition(hardkey, AnswerType.ANSWER_TYPE_HK);
//		}		
//	}

	/**
	 * 交互执行线程
	 * @author ruhai
	 *
	 */
	private static class InteractionExecuteThread extends Thread {
		private Context context;
		private boolean stop;
		private ExecuteInteraction currentInteraction;

		InteractionExecuteThread(Context context) {
			this.context = context;
		}

		void cancelCurrentInteraction() {
			if (currentInteraction != null) {
				currentInteraction.cancel();
			}
		}

		@Override
		public void run() {
			LogUtil.d(LOG_TAG, "InteractionExecuteThread started. tid=" + getId() + ", tname=" + getName());
			while (!stop) {
				Scene currentScene = null;

				synchronized (mLock) {
					if (!mSceneList.isEmpty()) {
						currentScene = mSceneList.remove(0);
						LogUtil.d(LOG_TAG, "Get an interaction scene from the pool.");
						((VoiceAssistant)context).changeState(VoiceAssistant.State.STATE_INTERACTING);
					} else {
						try {
							LogUtil.v(LOG_TAG, "当前队列内无交互模板，等待...");
							((VoiceAssistant)context).changeState(VoiceAssistant.State.STATE_LISTENING);
							
							LogUtil.w(LOG_TAG, "executeInteraction from to end !");
							mbRunningInteraction = false;
							mLock.wait();
							LogUtil.v(LOG_TAG,"mLock up ###########################################");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				// Execute the interaction
				if (currentScene != null) {
					LogUtil.d(LOG_TAG, "currentScene = "+currentScene.getName());
					currentInteraction = new ExecuteInteraction(context, currentScene);
					try {
						Thread t = new Thread(currentInteraction);
						t.start();
						// Wait till the interaction is finished
						LogUtil.d(LOG_TAG, "Wait till the interaction is finished...");
						t.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}else{
					LogUtil.v(LOG_TAG,"no currentScene !!!!!!!!!!!!!!!!!!!!!!");
				}
				LogUtil.d(LOG_TAG, "Prepare to execute the next interaction...");
			};
			LogUtil.d(LOG_TAG, "InteractionExecuteThread stopped. tid=" + getId() + ", tname=" + getName());
		}
	}

	/**
	 * 单个交互执行线程
	 * @author ruhai
	 *
	 */
	private static class ExecuteInteraction implements Runnable, VoiceEngine.RecognitionCallback,
													   VoiceEngine.TTSCallback {
		private Context context;
		private Scene scene;

		private String lock = "";
		private boolean cancel;

		private Dialog currentDialog;	// 当前执行的dialog
		private String lastAnswer;		// 当前执行dialog的结果
		private int nextDialogId;		// 下一次执行的dialog id

		private PendingSuccessfulAction pendingSuccessfulAction;
		private PendingFailedAction pendingFailedAction;

		ExecuteInteraction(Context context, Scene scene) {
			this.context = context;
			this.scene = scene;
		}

		void cancel() {
			LogUtil.d(LOG_TAG, "交互会话取消...");
			cancel = true;
		}

		public void run() {
			mEngine.registerListener(this);
			mEngine.registerTTSListener(this);

			// Start the interaction
			if (scene != null) {
				LogUtil.d(LOG_TAG," scene !!!!!!!!!!!!!!!!!!!!!!!!! in");
				context.sendBroadcast(new Intent(TSDEvent.Interaction.INTERACTION_START));

				// Notify interaction start
				Message msg = Message.obtain(null, CommonMessage.VoiceEngine.INTERACTION_START);
				notifyClients(msg);

				LogUtil.d(LOG_TAG, "开始执行交互场景\"" + scene.getName() + "\"");
				invokeInteraction();
				LogUtil.d(LOG_TAG, "场景\"" + scene.getName() + "\"执行完毕.");

				// Notify interaction end
				msg = Message.obtain(null, CommonMessage.VoiceEngine.INTERACTION_STOP);
				notifyClients(msg);
				LogUtil.d(LOG_TAG," scene !!!!!!!!!!!!!!!!!!!!!!!!! out");
			}else{
				LogUtil.d(LOG_TAG,"no scene !!!!!!!!!!!!!!!!!!!!!!!!!");
			}
	
			mEngine.unregisterListener(this);
			mEngine.unregisterTTSListener(this);
			LogUtil.d(LOG_TAG, "交互会话完成，线程结束");
		}

		private void invokeInteraction() {
			Dialog[] dialogs = scene.getDialogs();
			int i = 0;
//			retryTimes = 0;
			nextDialogId = 0;
			while (i < dialogs.length && !cancel) {
				currentDialog = dialogs[i];
				mEngine.startDialog(dialogs[i]);

				LogUtil.d(LOG_TAG, "挂起线程，等待对话交互结果...");
				synchronized(lock) {
					try { lock.wait(); } catch (InterruptedException e) {}
				}
				LogUtil.d(LOG_TAG, "收到对话交互结果，继续执行交互... nextDialogId = " + nextDialogId);

				i = nextDialogId;
			}
		}

		@Override
		public void onInteractSuccessful(String answerType, String answer) {
			LogUtil.d(LOG_TAG, "######### onInteractSuccessful, answer = " + answer + ", type = " + answerType);

			if (currentDialog == null) {
				LogUtil.d(LOG_TAG, "onInteractSuccessful,continueToInvokeInteraction return !!!");
				continueToInvokeInteraction();
				return;
			}

			boolean currentDialogFinished = true;
			// translate the answer based on the answer type
			String matchedAnswer = answerType;
			if (answerType.equals(AnswerType.COMMAND.value) || answerType.equals(AnswerType.RAW.value))
				matchedAnswer = answer;

			// Check the next steps according to the current answer
			SuccessfulAction[] nextSteps = currentDialog.getSuccessActions();
			if (nextSteps != null && nextSteps.length > 0) {
				LogUtil.d(LOG_TAG, "nextSteps");
				int i = 0;
				// find the match action
				while (i < nextSteps.length) {
					if (nextSteps[i].getValue().equals(matchedAnswer))
						break;
					i++;
				}
				if (i < nextSteps.length) {
					if (nextSteps[i].getSpeech() != null) {
						// Play hint message
						mEngine.ttsPlay(nextSteps[i].getSpeech(), true);

						// invoke the action after TTS playing finished
						pendingSuccessfulAction = new PendingSuccessfulAction();
						pendingSuccessfulAction.action = nextSteps[i];
						pendingSuccessfulAction.answerType = answerType;
						pendingSuccessfulAction.answer = matchedAnswer;
						pendingSuccessfulAction.extra = answer;

						currentDialogFinished = false;
					} else {
						invokeSuccessfulAction(nextSteps[i], answerType, matchedAnswer, answer);
					}
				} else {
					// Undefined, use default action
					notifyInteractionFinish(answerType, matchedAnswer, answer);
					nextDialogId = ENG_DIALOG;
				}
			} else {
				// 若未定义跳转动作则返回当前交互结果
				LogUtil.d(LOG_TAG, "notifyInteractionFinish");
				notifyInteractionFinish(answerType, matchedAnswer, answer);
				nextDialogId = ENG_DIALOG;
			}

			LogUtil.d(LOG_TAG,"currentDialogFinished = "+currentDialogFinished);
					
			lastAnswer = answer;
			if (currentDialogFinished) {
				LogUtil.d(LOG_TAG, "本此对话结束（成功）");
				continueToInvokeInteraction();
			}
		}

		@Override
		public void onInteractFailed(ErrorType error) {
			LogUtil.w(LOG_TAG, "#######################onInteractFail, error = " + error);

			if (currentDialog == null) {
				LogUtil.w(LOG_TAG, "onInteractFail,continueToInvokeInteraction"); 
				continueToInvokeInteraction();
				return;
			}

			boolean currentDialogFinished = true;
			FailedAction[] failSteps = currentDialog.getFailureActions();
			if (failSteps == null) {
				// 若未定义跳转动作则返回Unknown
				LogUtil.w(LOG_TAG, "onInteractFail notifyInteractionError");
				notifyInteractionError(error.name(), error.value);
				nextDialogId = ENG_DIALOG;
			} else {
				LogUtil.w(LOG_TAG, "onInteractFail failSteps");
				if (failSteps != null && failSteps.length > 0) {
					int i = 0;
					while (i < failSteps.length) {
						if (failSteps[i].getReason().equalsIgnoreCase(error.name()))
							break;
						i++;
					}
					if (i < failSteps.length) {
						if (failSteps[i].getSpeech() != null) {
							// Play hint message
							mEngine.ttsPlay(failSteps[i].getSpeech(), true);

							// Invoke failed action after TTS playing finished
							pendingFailedAction = new PendingFailedAction();
							pendingFailedAction.action = failSteps[i];
							pendingFailedAction.reason = error.name();
							pendingFailedAction.description = error.value;

							currentDialogFinished = false;
						} else {
							invokeFailedAction(failSteps[i], error.name(), error.value);
						}
					} else {
						// Undefined, use default action
						LogUtil.w(LOG_TAG, "onInteractFail failSteps notifyInteractionError "+mbStopInSearchView);
						if(!mbStopInSearchView){
							notifyInteractionError(error.name(), error.value);
						}else{
							mbStopInSearchView = false;
						}
						nextDialogId = ENG_DIALOG;
					}
				}
			}

			LogUtil.d(LOG_TAG, "currentDialogFinished="+currentDialogFinished);
			if (currentDialogFinished) {
				LogUtil.d(LOG_TAG, "本此对话结束（失败）");
				continueToInvokeInteraction();
			}
		}

//		@Override
//		public void onInteractCancelled() {
//			LogUtil.d(LOG_TAG, "onInteractCancelled.");
//
//			if (scene == null) {
//				synchronized(lock) {
//					lock.notify();
//				}
//				return;
//			}
//
//			// 结束当前对话，直接继续执行后面的对话
//			nextDialogId++;
//			if (nextDialogId >= scene.getDialogs().length) {
//				sendInteractResult("Canceled", CANCELED);
//			}
//			LogUtil.d(LOG_TAG, "本此对话结束（取消），恢复挂起线程继续执行交互.");
//			synchronized(lock) {
//				lock.notify();
//			}
//		}

		@Override
		public void onPlayEnd() {
			Log.d(LOG_TAG, "onPlayEnd");

			if (pendingSuccessfulAction != null) {
				invokeSuccessfulAction(pendingSuccessfulAction.action,
									   pendingSuccessfulAction.answerType,
									   pendingSuccessfulAction.answer,
									   pendingSuccessfulAction.extra);

				pendingSuccessfulAction = null;
				continueToInvokeInteraction();
			}

			if (pendingFailedAction != null) {
				invokeFailedAction(pendingFailedAction.action,
								   pendingFailedAction.reason,
								   pendingFailedAction.description);

				pendingFailedAction = null;
				continueToInvokeInteraction();
			}
		}

		private void invokeHardKeyAction(HardKeyFunction action) {
			// TODO:
		}

		private void invokeSuccessfulAction(SuccessfulAction action,
				String answerType, String answer, String extra)
		{
			String str = action.getAction();
			if (str.equals("return")) {
				// Send the result
				notifyInteractionFinish(answerType, answer, extra);

				// To let invokeInteraction() to finish after notify()
				nextDialogId = ENG_DIALOG;
			} else if (str.equals("jump") && action.getJump() != null) {
				Jump nextAction = action.getJump();
				nextDialogId = nextAction.getDialogId();
				if (nextAction.getHardKeyFunction() != null) {
					invokeHardKeyAction(nextAction.getHardKeyFunction());
				}
			}

			// Send message to other apps if necessary
			if (action.getMessage() != null) {
				sendMessage(action.getMessage(), action.getParams());
			}
		}

		private void invokeFailedAction(FailedAction action,
				String reason, String description)
		{
			String str = action.getAction();
			if (str.equals("return")) {
				// Send the result
				notifyInteractionError(reason, description);

				// To let invokeInteraction() to finish after notify()
				nextDialogId = ENG_DIALOG;
			} else if (str.equals("jump") && action.getJump() != null) {
				Jump nextAction = action.getJump();
				nextDialogId = nextAction.getDialogId();
				if (nextAction.getHardKeyFunction() != null) {
					invokeHardKeyAction(nextAction.getHardKeyFunction());
				}
			}					
		}

		private void continueToInvokeInteraction() {
			LogUtil.d(LOG_TAG, "恢复挂起线程继续执行交互.");
			synchronized(lock) {
				lock.notify();
			}
		}

		private void notifyInteractionFinish(String answerType, String answer, String extra) {
			LogUtil.d(LOG_TAG, "广播交互结果: scene=" + scene.getName() + ", answerType=" + answerType
					+ ", answer=" + answer + ", extra=" + extra);

			Intent intent = new Intent(TSDEvent.Interaction.INTERACTION_FINISH);
			intent.putExtra("template", scene.getName());
			intent.putExtra("answerType", answerType);
			intent.putExtra("answer", answer);
			intent.putExtra("extra", extra);
			context.sendBroadcast(intent);			
		}

		private void notifyInteractionError(String reason, String description) {
			LogUtil.d(LOG_TAG, "广播交互结果: scene=" + scene.getName() + ", reason=" + reason + ", description=" + description);
			
			Intent intent = new Intent(TSDEvent.Interaction.INTERACTION_ERROR);
			intent.putExtra("template", scene.getName());
			intent.putExtra("reason", reason);
			intent.putExtra("description", description);

			context.sendBroadcast(intent);
		}

		private void sendMessage(String msg, String params) {
			LogUtil.d(LOG_TAG, "Send message: action=" + msg + ", params = " + params);
			Intent intent = new Intent(msg);
			intent.putExtra("params", params);
			context.sendBroadcast(intent);
		}

		private void notifyClients(Message msg) {
			if (mClients != null) {
				synchronized (VoiceAssistant.class) {
					try {
						for (Messenger c : mClients) {
							c.send(msg);
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	private static class PendingSuccessfulAction {
		SuccessfulAction action;
		String answerType;
		String answer;
		String extra;
	}

	private static class PendingFailedAction {
		FailedAction action;
		String reason;
		String description;
	}
}

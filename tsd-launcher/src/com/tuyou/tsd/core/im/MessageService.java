package com.tuyou.tsd.core.im;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.LogUtil;

public class MessageService {
//	public static final String MESSAGE_ARRIVED = "tsd.event.push.MESSAGE_ARRIVED";
	private static final String TAG = "MessageClient";

	private static MessageService mInstance;
	private Context mContext;
	private MQTTConnection mConn;
	private IMqttClient mqttClient = null;

	private MessageService(Context context) { this.mContext = context; }
	public static MessageService getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new MessageService(context);
		}
		return mInstance;
	}

	/**
	 * 建立连接
	 * @param host
	 * @param port
	 * @param deviceType
	 * @param deviceId
	 */
	public void startWork(String host, int port, String deviceType, String deviceId) {
		registerReceiver();
		mConn = new MQTTConnection(host, port, deviceType, deviceId);
		try {
			mConn.connect();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		// Notify the service is started
		mContext.sendBroadcast(new Intent(TSDEvent.Push.SERVICE_STARTED));
	}
	
	private void registerReceiver(){
		IntentFilter mFilter = new IntentFilter();         
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);         
		mContext.registerReceiver(mReceiver, mFilter); 
	}

	/**
	 * 断开连接
	 */
	public void stopWork() {
		mContext.unregisterReceiver(mReceiver);
		mConn.disconnect();
		// Notify the service is stopped
		mContext.sendBroadcast(new Intent(TSDEvent.Push.SERVICE_STOPPED));
	}

	/**
	 * 发送返回消息
	 * @param id
	 * @param content
	 */
	public void sendFeedBack(String id, String content) {
		try {
			mConn.publishTopic(id, content);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 广播到达消息
	 * @param message
	 */
	private void onMessageArrived(String message) {
		//
        if (!TextUtils.isEmpty(message)) {
        	try {
        		JSONObject json = new JSONObject(message);
        		String pushMsg = json.getString("message");

            	Intent intent = new Intent(TSDEvent.Push.MESSAGE_ARRIVED);
            	intent.putExtra("message", pushMsg);

            	mContext.sendBroadcast(intent);
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
	}

	private void onConnectionLost() {
		// TODO:
	}

    private class MQTTConnection implements MqttCallback {
		private MemoryPersistence persistence = new MemoryPersistence();

		private String host;
		private int port;
		private String arg0, arg1;

		private MQTTConnection(String host, int port, String arg0, String arg1) {
			this.host = host;
			this.port = port;
			this.arg0 = arg0;
			this.arg1 = arg1;
		}

		private void connect() throws MqttException {
			String broker = "tcp://" + host + ":" + port;
			String clientID = arg0 + "." + arg1 + "/";
			LogUtil.d(TAG, "connect to: " + broker + ", " + clientID);

			mqttClient = new MqttClient(broker, clientID, persistence);
			// 
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(false);
            connOpts.setConnectionTimeout(30);
			connOpts.setKeepAliveInterval(30);
			mqttClient.setCallback(this);
			mqttClient.connect(connOpts);
			
			if (mqttClient != null && mqttClient.isConnected()) {
				mqttClient.subscribe(new String[] {arg1},new int[] {1});
				LogUtil.d(TAG, "create MQTT connection is successful.");
			} else {
				LogUtil.d(TAG, "create MQTT connection is failed.");
			}
		}
		
		private void disconnect() {
			try {
				mqttClient.disconnect();
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}

		private void publishTopic(String topic, String content) throws MqttPersistenceException, MqttException {
			MqttMessage message = new MqttMessage(content.getBytes());
			mqttClient.publish(topic, message);
		}

		@Override
		public void connectionLost(Throwable cause) {
			LogUtil.d(TAG, "create MQTT connection is lost...");
			onConnectionLost();
		}

		@Override
		public void messageArrived(String topic, MqttMessage message)
				throws Exception {
			LogUtil.d(TAG, "messageArrived, topic = " + topic + ", message = " + message);
			onMessageArrived(message.toString());
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {
			LogUtil.d(TAG, "deliveryComplete");
		}
	}    
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            	ConnectivityManager connectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            	NetworkInfo info = connectivityManager.getActiveNetworkInfo();  
                if(info != null && info.isAvailable()) {
                	try {
                		if(!mqttClient.isConnected()){
                			mConn.connect();
                		}
					} catch (Exception e) {
						e.printStackTrace();
					}
                }else{
                	mConn.disconnect();
                }
            }
        }
    };
}

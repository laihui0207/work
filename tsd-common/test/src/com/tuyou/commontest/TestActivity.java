package com.tuyou.commontest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.google.gson.Gson;
import com.tuyou.tsd.common.network.GetConfigRes;
import com.tuyou.tsd.common.network.GetDeviceInfoRes;
import com.tuyou.tsd.common.network.GetOwnerInfoRes;
import com.tuyou.tsd.common.network.GetUpdateInfoRes;
import com.tuyou.tsd.common.network.GetWeatherRes;
import com.tuyou.tsd.common.network.JsonOA;
import com.tuyou.tsd.common.network.LoginReq;
import com.tuyou.tsd.common.network.LoginRes;
import com.tuyou.tsd.common.network.LogoutRes;
import com.tuyou.tsd.common.network.SubmitAccidentInfoReq;
import com.tuyou.tsd.common.network.SubmitAccidentInfoRes;
import com.tuyou.tsd.common.network.SubmitConfigReq;
import com.tuyou.tsd.common.network.SubmitConfigRes;
import com.tuyou.tsd.common.network.SubmitDeviceStatusReq;
import com.tuyou.tsd.common.network.SubmitDeviceStatusRes;
import com.tuyou.tsd.common.network.TokenRes;
import com.tuyou.tsd.common.util.MyAsyncTask;
import com.tuyou.tsd.common.util.SHA256Encrypt;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class TestActivity extends Activity implements OnItemClickListener {
	public static long LAST_MENU_ITEM_ID = 0L;

	private static final Long MENU_ITEM_ID_PAYMENT_1 = allocId();
	private static final Long MENU_ITEM_ID_PAYMENT_2 = allocId();
	private static final Long MENU_ITEM_ID_PAYMENT_3 = allocId();
	private static final Long MENU_ITEM_ID_PAYMENT_4 = allocId();
	private static final Long MENU_ITEM_ID_PAYMENT_5 = allocId();
	private static final Long MENU_ITEM_ID_PAYMENT_6 = allocId();
	private static final Long MENU_ITEM_ID_PAYMENT_7 = allocId();
	private static final Long MENU_ITEM_ID_PAYMENT_8 = allocId();
	private static final Long MENU_ITEM_ID_PAYMENT_9 = allocId();
	private static final Long MENU_ITEM_ID_PAYMENT_10 = allocId();
	public static TextView textView;
	
	DownloadCompleteReceiver receiver;  
	DownloadManager downloadManager; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.com_android_test_layout);

		ListView listView = (ListView) this
				.findViewById(R.id.com_idaqiuer_gameplatform_test_listview);
		textView = (TextView) this
				.findViewById(R.id.com_idaqiuer_gameplatform_test_text);
		MyAdapter adapter = new MyAdapter(this);

		adapter.appendTestEntry(MENU_ITEM_ID_PAYMENT_1, "登陆");
		adapter.appendTestEntry(MENU_ITEM_ID_PAYMENT_2, "获取天气");
		adapter.appendTestEntry(MENU_ITEM_ID_PAYMENT_3, "上传设备状态");
		adapter.appendTestEntry(MENU_ITEM_ID_PAYMENT_4, "获取设备配置");
		adapter.appendTestEntry(MENU_ITEM_ID_PAYMENT_5, "上传报警");

		adapter.appendTestEntry(MENU_ITEM_ID_PAYMENT_6, "获取所有设置");
		adapter.appendTestEntry(MENU_ITEM_ID_PAYMENT_7, "获取车主信息");
		adapter.appendTestEntry(MENU_ITEM_ID_PAYMENT_8, "获取更新信息");
		adapter.appendTestEntry(MENU_ITEM_ID_PAYMENT_9, "上传设置");
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY, "Z5BGgyUbgBXpeKoTpgl7fw9G");

		textView.setText("显示测试信息");
		String iccid = Params.getInstance(this).getSimSerialNumber();
		String iccid_1 = SHA256Encrypt.bin2hex(iccid);
		textView.setText(iccid + "\n" + iccid_1);
		downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);  
		 receiver = new DownloadCompleteReceiver(); 
		 registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));  
	}

	private static long allocId() {
		LAST_MENU_ITEM_ID++;
		return LAST_MENU_ITEM_ID;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		if (id == MENU_ITEM_ID_PAYMENT_1) {
			LoginReqTask test = new LoginReqTask();
			LoginReq loginreq=new LoginReq();
			loginreq.imei=Params.getInstance(this).getImei();
			loginreq.password=SHA256Encrypt.bin2hex(Params.getInstance(this).getSimSerialNumber());
			test.execute(loginreq);
		} else if (id == MENU_ITEM_ID_PAYMENT_2) {
			 GetWeatherTask testtask= new GetWeatherTask();
			 testtask.execute();			
		} else if (id == MENU_ITEM_ID_PAYMENT_3) {

			SubmitDeviceStatusReq aaa = new SubmitDeviceStatusReq();
			aaa.workMode = "idle";
			aaa.state = "work";
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssZ", Locale.CHINA);
			String strTimestamp = dateFormat.format(new Date(System
					.currentTimeMillis()));
			aaa.timestamp = strTimestamp;
			SubmiDeviceStatusTask testtask = new SubmiDeviceStatusTask();
			testtask.execute(aaa);
		} else if (id == MENU_ITEM_ID_PAYMENT_4) {
			GetConfigTask testTask = new GetConfigTask();
			testTask.execute();
		} else if (id == MENU_ITEM_ID_PAYMENT_5) {			
			SubmitAccidentInfoReq test = new SubmitAccidentInfoReq();
			test.sender = "aaa";
			test.timeout = 10;
			SubmitAccidentTask testtask = new SubmitAccidentTask();
			testtask.execute(test);			
			
		} else if (id == MENU_ITEM_ID_PAYMENT_6) {
			//downloadTest();
			GetConfigInfoTask ddd=new GetConfigInfoTask();
			ddd.execute();
		} else if (id == MENU_ITEM_ID_PAYMENT_7) {
			GetOwnerInfoReqTask test = new GetOwnerInfoReqTask();
			test.execute();
		} else if (id == MENU_ITEM_ID_PAYMENT_8) {
			GetUpdateInfoReqTask test = new GetUpdateInfoReqTask();
			test.execute();			
		} 
		 else if (id == MENU_ITEM_ID_PAYMENT_9) {		
				SubmitConfigReq req = new SubmitConfigReq();
				req.content = "93.4";
				SubmitConfigReqTask testtask = new SubmitConfigReqTask();
				testtask.execute();
			} 


	}

	private class MyAdapter extends BaseAdapter {
		LayoutInflater mInflater;
		List<Map<String, Object>> mListItems = new ArrayList<Map<String, Object>>();

		public MyAdapter(Context context) {
			super();
			mInflater = LayoutInflater.from(context);
		}

		private void appendTestEntry(Long id, String text) {
			Map<String, Object> map = new HashMap<String, Object>();

			map.put("id", id);
			map.put("text", text);
			mListItems.add(map);
		}

		@Override
		public int getCount() {
			return mListItems.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return (Long) mListItems.get(position).get("id");
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = null;
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.com_android_test_listitem_layout, null);
				textView = (TextView) convertView
						.findViewById(R.id.com_idaqiuer_gameplatform_test_textview);
				convertView.setTag(textView);
			} else {
				textView = (TextView) convertView.getTag();
			}
			String itemText = (String) mListItems.get(position).get("text");
			textView.setText(itemText);
			return convertView;
		}

	}

	static class GetWeatherTask extends MyAsyncTask<Void, Void, GetWeatherRes> {
		String cityId;
		String timeStamp;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			cityId = "31000000";
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssZ", Locale.CHINA);
			String strTimestamp = dateFormat.format(new Date(System
					.currentTimeMillis()));
			strTimestamp = strTimestamp.substring(0, 22) + ":"
					+ strTimestamp.substring(22);
			timeStamp = strTimestamp;
		}

		@Override
		protected void onPostExecute(final GetWeatherRes result) {
			Gson gson = new Gson();
			String resultJson = gson.toJson(result);
			textView.setText(resultJson);
		}

		@Override
		protected GetWeatherRes doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return JsonOA.getWeatherInfo(cityId, timeStamp);
		}
	}

	static class GetDeviceInfoTask extends
			MyAsyncTask<Void, Void, GetDeviceInfoRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetDeviceInfoRes result) {
			Gson gson = new Gson();
			String resultJson = gson.toJson(result);
			textView.setText(resultJson);
		}

		@Override
		protected GetDeviceInfoRes doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return JsonOA.getDeviceInfo();
		}
	}

	static class GetConfigInfoTask extends
			MyAsyncTask<Void, Void, GetConfigRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetConfigRes result) {
			Gson gson = new Gson();
			String resultJson = gson.toJson(result);
			textView.setText(resultJson);
		}

		@Override
		protected GetConfigRes doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return JsonOA.getConfigInfo("all");
		}
	}

	static class SubmiDeviceStatusTask extends
			MyAsyncTask<SubmitDeviceStatusReq, Void, SubmitDeviceStatusRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
		}

		@Override
		protected void onPostExecute(final SubmitDeviceStatusRes result) {
			Gson gson = new Gson();
			String resultJson = gson.toJson(result);
			textView.setText(resultJson);
		}

		@Override
		protected SubmitDeviceStatusRes doInBackground(
				SubmitDeviceStatusReq... params) {
			// TODO Auto-generated method stub
			return JsonOA.submitDeviceStatus(params[0]);
		}
	}

	static class SubmitAccidentTask extends
			MyAsyncTask<SubmitAccidentInfoReq, Void, SubmitAccidentInfoRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final SubmitAccidentInfoRes result) {
			Gson gson = new Gson();
			String resultJson = gson.toJson(result);
			textView.setText(resultJson);
		}

		@Override
		protected SubmitAccidentInfoRes doInBackground(
				SubmitAccidentInfoReq... params) {
			// TODO Auto-generated method stub
			return JsonOA.submitDeviceStatus(params[0]);
		}
	}

	static class GetTokenTask extends MyAsyncTask<Void, Void, TokenRes> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(final TokenRes result) {
			Gson gson = new Gson();
			String resultJson = gson.toJson(result);
			textView.setText(resultJson);
		}

		@Override
		protected TokenRes doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return JsonOA.getToken();
		}
	}

	public class GetConfigTask extends MyAsyncTask<Void, Void, GetConfigRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetConfigRes result) {
			Gson gson = new Gson();
			String resultJson = gson.toJson(result);
			System.out.println("resultJson = " + resultJson);
			textView.setText(resultJson);
		}

		@Override
		protected GetConfigRes doInBackground(Void... arg0) {
			return JsonOA.getConfigInfo("fm_freq");
		}
	}

	public class SubmitConfigReqTask extends
			MyAsyncTask<Void, Void, SubmitConfigRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final SubmitConfigRes result) {
			Gson gson = new Gson();
			String resultJson = gson.toJson(result);
			System.out.println("resultJson = " + resultJson);
			textView.setText(resultJson);
		}

		@Override
		protected SubmitConfigRes doInBackground(Void... arg0) {
			return JsonOA.setConfigInfo("fm_freq", "93.4");
		}
	}

	public class LoginReqTask extends MyAsyncTask<LoginReq, Void, LoginRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final LoginRes result) {
			Gson gson = new Gson();
			String resultJson = gson.toJson(result);
			System.out.println("resultJson = " + resultJson);
			textView.setText(resultJson);
		}

		@Override
		protected LoginRes doInBackground(LoginReq... arg0) {
			return JsonOA.login(arg0[0]);
		}
	}

	public class LogoutReqTask extends MyAsyncTask<Void, Void, LogoutRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final LogoutRes result) {
			Gson gson = new Gson();
			String resultJson = gson.toJson(result);
			System.out.println("resultJson = " + resultJson);
			textView.setText(resultJson);
		}

		@Override
		protected LogoutRes doInBackground(Void... arg0) {
			return JsonOA.logout();
		}
	}

	public class GetOwnerInfoReqTask extends MyAsyncTask<Void, Void, GetOwnerInfoRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetOwnerInfoRes result) {
			Gson gson = new Gson();
			String resultJson = gson.toJson(result);
			System.out.println("resultJson = " + resultJson);
			textView.setText(resultJson);
		}

		@Override
		protected GetOwnerInfoRes doInBackground(Void... arg0) {
			return JsonOA.getOwnerInfo();
		}
	}
	
	public class GetUpdateInfoReqTask extends MyAsyncTask<Void, Void, GetUpdateInfoRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetUpdateInfoRes result) {
			Gson gson = new Gson();
			String resultJson = gson.toJson(result);
			System.out.println("resultJson = " + resultJson);
			textView.setText(resultJson);
		}

		@Override
		protected GetUpdateInfoRes doInBackground(Void... arg0) {
			return JsonOA.getUpdateInfo();
		}
	}
	
	public String encodeGB(String string)  
    {  
        //转换中文编码  
        String split[] = string.split("/");  
        for (int i = 1; i < split.length; i++) {  
            try {  
                split[i] = URLEncoder.encode(split[i], "GB2312");  
            } catch (UnsupportedEncodingException e) {  
                e.printStackTrace();  
            }  
            split[0] = split[0]+"/"+split[i];  
        }  
        split[0] = split[0].replaceAll("\\+", "%20");//处理空格  
        return split[0];  
    }  
	public void downloadTest()
	{
		 
		 
		 String url = "http://dl.google.com/android/ndk/android-ndk-r6-linux-x86.tar.bz2"; 
		 Uri resource = Uri.parse(url);//Uri.parse(encodeGB(url));   
         DownloadManager.Request request = new DownloadManager.Request(resource);   
         request.setAllowedNetworkTypes(Request.NETWORK_MOBILE | Request.NETWORK_WIFI);   
         request.setAllowedOverRoaming(false);   
         //设置文件类型  
         MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();  
         String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));  
         request.setMimeType(mimeString);  
         //在通知栏中显示   
        request.setShowRunningNotification(false);  
         request.setVisibleInDownloadsUi(false);  
         //sdcard的目录下的download文件夹  
         request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "android-ndk-r6-linux-x86.tar.bz2");  
         request.setTitle("移动G3广告");  
         
         request.addRequestHeader("hello", "hello");
         long id = downloadManager.enqueue(request);   		
     
	}
	class DownloadCompleteReceiver extends BroadcastReceiver {  
        @Override  
        public void onReceive(Context context, Intent intent) {  
            if(intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){  
                long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);  
                Log.v("test"," download complete! id : "+downId);  
                Toast.makeText(context, intent.getAction()+"id : "+downId, Toast.LENGTH_SHORT).show();  
            }  
        }  
    }  
	
}

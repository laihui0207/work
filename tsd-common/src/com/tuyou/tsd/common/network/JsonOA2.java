package com.tuyou.tsd.common.network;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;

import com.google.gson.Gson;
import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;

public class JsonOA2 {
	private static final String LOG_TAG = "Communication";
	private static JsonOA2 mInstance;
	private Context mContext;

	private static String URL = "http://" + TSDConst.SERVER_HOST + "/xbot/v1/";
	/**
	 * 与服务端通信请求所用的token串，登录后获得。
	 */
	public static String mAccesToken = "Bearer 383w9JKJLJFw4ewpie2wefmjdlJLDJF";
	/**
	 * Device ID, 登录后从服务端获取
	 */
	public static String mDeviceId=null;

	private JsonOA2(Context context) {
		mContext = context;
	}

	public static JsonOA2 getInstance(Context context) {
		if (mInstance == null)
			mInstance = new JsonOA2(context);
		return mInstance;
	}


	/**
	 * 车机登陆
	 * 
	 * 
	 **/
	public LoginRes login(LoginReq req) {
		String URL1 = URL + "user/devices/authc";
		LoginRes result = postForObject(URL1, req, LoginRes.class);
		if (result != null && result.errorCode == 0) {
			// Save the access token and device id
			HashMap<String, Object> values = new HashMap<String, Object>();
			values.put("access_token", result.accessToken);
			values.put("device_id", result.device.id);
			HelperUtil.writeToCommonPreference(mContext, values);
			LogUtil.d(LOG_TAG, "登录成功，保存token: " + result.accessToken + ", Device id = " + result.device.id);
		}
		return result;
	}

	public GeneralRes updateDeviceInfo(String ssid, String ssidPwd) {
		if (mDeviceId == null)
			prepare();

		JSONObject reqObj = new JSONObject();
		try {
			reqObj.put("ssid", ssid);
			reqObj.put("ssidPassword", ssidPwd);

			String URL1 = URL + "user/devices/" + mDeviceId;
			return putForObject(URL1, reqObj, GeneralRes.class);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取版本更新信息
	 * 
	 * 
	 **/
	public GetUpdateInfoRes getUpdateInfo() {
		String URL1 = URL + "update/app/latest?type=tsd";
		return getForObject(URL1, GetUpdateInfoRes.class);
	}

	/**
	 * 上传设备状态信息
	 * 
	 * 
	 **/
	public SubmitDeviceStatusRes submitDeviceStatus(
			SubmitDeviceStatusReq req) {
		if (mDeviceId == null)
			prepare();
		String URL1 = URL + "device/state/" + mDeviceId;
		return postForObject(URL1, req, SubmitDeviceStatusRes.class);
	}

	/**
	 * 上传事故信息
	 * 
	 * 
	 **/
	public SubmitAccidentInfoRes submitAccidentStatus(
			SubmitAccidentInfoReq req) {
		if (mDeviceId == null)
			prepare();
		String URL1 = URL + "cardvr/accidents/" + mDeviceId;
		return postForObject(URL1, req, SubmitAccidentInfoRes.class);
	}

	/**
	 * 获取天气预报信息
	 * 
	 * 
	 **/
	public GetWeatherRes getWeatherInfo(String city, String timestamp) {
		String URL1 = URL + "weather.json?city=" + city + "&time="
				+ timestamp;
		return getForObject(URL1, GetWeatherRes.class);
	}
	/**
	 * 获取设备配置信息
	 * 
	 * 
	 **/
	public GetConfigRes getConfigInfo(String module) {
		if (mDeviceId == null)
			prepare();
		String URL1 = URL + "configs/" + module + "/" + mDeviceId;
		return getForObject(URL1, GetConfigRes.class);
	}
	
	/**
	 * 获取上传token
	 * @param type 照片－－"mblog"；视频－－"cardvr"
	 * @return
	 */
	public GetUploadTokenRes getUploadToken(String type) {
		if (mDeviceId == null)
			prepare();
		String reqUrl = URL + "configs/system-qiniu-tokens/" + mDeviceId +"?item=" + type;
		return getForObject(reqUrl, GetUploadTokenRes.class);
	}

	/**
	 * 上传设备配置信息
	 * 
	 * 
	 **/
	public SubmitConfigRes setConfigInfo(String name, JSONObject content) {
		if (mDeviceId == null)
			prepare();
		String URL1 = URL + "configs/" + name + "/" + mDeviceId;
		return postForObject(URL1, content, SubmitConfigRes.class);
	}

	/**
	 * HTTP PUT 上传服务器数据
	 *   如果没有处于联网状态，则直接返回状态码errorCode=-1；
	 *   如果处于联网状态，联网返回值状态码不在200-207，这返回通信失败的HTTP状态码，如404，等
	 **/
	private <T> T putForObject(String urlString, Object request,
			Class<T> responseType) {
		LogUtil.d(LOG_TAG, "putForObject: " + urlString);
		T result = null;
		Gson gson = new Gson();

		if(checkNetworkInfo()==-1)
		{
			String networkError="{\"errorCode\":-1}";
			result = gson.fromJson(networkError, responseType);
			return result;
		}
		
		if (mAccesToken == null)
			prepare();

		HttpClient httpClient;
		if (urlString.toLowerCase().startsWith("https://")) {
			httpClient = new DefaultHttpClient();
		} else {
			httpClient = new DefaultHttpClient();
		}

		
		HttpPut httput = new HttpPut(urlString);
		httput.setHeader("User-Agent", "xiaobao-tsd-1.0");
		httput.setHeader("Authorzation", mAccesToken);

		if (request != null) {
			String requestJson = request instanceof JSONObject ?
					request.toString() : gson.toJson(request);
			LogUtil.d(LOG_TAG, "requestJson = " + requestJson);
			StringEntity stringEntity;
			try {
				stringEntity = new StringEntity(requestJson, "UTF-8");
				httput.setEntity(stringEntity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		try {
			HttpResponse response = httpClient.execute(httput);
			int resStatus=response.getStatusLine().getStatusCode();
			if ((resStatus>=HttpStatus.SC_OK)&&( resStatus<300)) {
				InputStream is = new BufferedInputStream(response.getEntity()
						.getContent());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[2048];
				int readLen;
				while ((readLen = is.read(buf)) != -1) {
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException();
					}
					baos.write(buf, 0, readLen);
				}
				String responseJson = new String(baos.toByteArray());
				LogUtil.v(LOG_TAG, responseJson);
				result = gson.fromJson(responseJson, responseType);
			}
			else
			{
				String networkError=String.format("{\"errorCode\":%d}", resStatus);
				result = gson.fromJson(networkError, responseType);
			}
		} catch (Exception e) {
			LogUtil.e(LOG_TAG, "Exception raised at putForObject(), " + e.getMessage());
			e.printStackTrace();
			result = null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

	/**
	 * HTTP POST 上传服务器数据
	 *   如果没有处于联网状态，则直接返回状态码errorCode=-1；
	 *   如果处于联网状态，联网返回值状态码不在200-207，这返回通信失败的HTTP状态码，如404，等
	 **/
	private <T> T postForObject(String urlString, Object request,
			Class<T> responseType) {
		LogUtil.d(LOG_TAG, "postForObject: " + urlString);
		T result = null;
		Gson gson = new Gson();

		if(checkNetworkInfo()==-1)
		{
			String networkError="{\"errorCode\":-1}";
			result = gson.fromJson(networkError, responseType);
			return result;
		}
		
		if (mAccesToken == null)
			prepare();

		HttpClient httpClient;
		if (urlString.toLowerCase().startsWith("https://")) {
			httpClient = new DefaultHttpClient();
		} else {
			httpClient = new DefaultHttpClient();
		}

		
		HttpPost httpost = new HttpPost(urlString);
		httpost.setHeader("User-Agent", "xiaobao-tsd-1.0");
		httpost.setHeader("Authorzation", mAccesToken);

		if (request != null) {
			String requestJson = request instanceof JSONObject ?
					request.toString() : gson.toJson(request);
			LogUtil.d(LOG_TAG, "requestJson = " + requestJson);
			StringEntity stringEntity;
			try {
				stringEntity = new StringEntity(requestJson, "UTF-8");
				httpost.setEntity(stringEntity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		try {
			HttpResponse response = httpClient.execute(httpost);
			int resStatus=response.getStatusLine().getStatusCode();
			if ((resStatus>=HttpStatus.SC_OK)&&( resStatus<300)) {
				InputStream is = new BufferedInputStream(response.getEntity()
						.getContent());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[2048];
				int readLen;
				while ((readLen = is.read(buf)) != -1) {
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException();
					}
					baos.write(buf, 0, readLen);
				}
				String responseJson = new String(baos.toByteArray());
				LogUtil.v(LOG_TAG, responseJson);
				result = gson.fromJson(responseJson, responseType);
			}
			else
			{
				String networkError=String.format("{\"errorCode\":%d}", resStatus);
				result = gson.fromJson(networkError, responseType);
			}
		} catch (Exception e) {
			LogUtil.e(LOG_TAG, "Exception raised at postForObject(), " + e.getMessage());
			e.printStackTrace();
			result = null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

	/**
	 * HTTP GET 获取服务器数据
	 *   如果没有处于联网状态，则直接返回状态码errorCode=-1；
	 *   如果处于联网状态，联网返回值状态码不在200-207，这返回通信失败的HTTP状态码，如404，等
	 **/
	private <T> T getForObject(String urlString, Class<T> responseType) {
		LogUtil.v(LOG_TAG, urlString);
		T result = null;
		Gson gson = new Gson();

		if(checkNetworkInfo()==-1)
		{
			String networkError="{\"errorCode\":-1}";
			result = gson.fromJson(networkError, responseType);
			return result;
		}
		if (mAccesToken == null)
			prepare();

		HttpClient httpClient;
		if (urlString.toLowerCase().startsWith("https://")) {
			httpClient = new DefaultHttpClient();
		} else {
			httpClient = new DefaultHttpClient();
		}

		HttpGet httget = new HttpGet(urlString);
		httget.setHeader("User-Agent", "xiaobao-tsd-1.0");
		httget.setHeader("Authorzation", mAccesToken);
		httget.setHeader("Content-Type", "application/json");

		
		try {
			HttpResponse response = httpClient.execute(httget);
			int resStatus=response.getStatusLine().getStatusCode();
			if ((resStatus>=HttpStatus.SC_OK)&&( resStatus<300))  {
				InputStream is = new BufferedInputStream(response.getEntity()
						.getContent());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[2048];
				int readLen;
				while ((readLen = is.read(buf)) != -1) {
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException();
					}
					baos.write(buf, 0, readLen);
				}
				String responseJson =baos.toString(); 
				LogUtil.v(LOG_TAG, responseJson);
				result = gson.fromJson(responseJson, responseType);
			}
			else
			{
				String networkError=String.format("{\"errorCode\":%d}", resStatus);
				result = gson.fromJson(networkError, responseType);
			}
		} catch (Exception e) {
			LogUtil.e(LOG_TAG, "Exception raised at getForObject(), " + e.getMessage());
			e.printStackTrace();
			result = null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

	private String doGetRequest(String request) {
		String result;

		if(checkNetworkInfo()==-1) {
			return "{\"errorCode\":-1}";
		}
		if (mAccesToken == null)
			prepare();

		HttpClient httpClient;
		if (request.toLowerCase().startsWith("https://")) {
			httpClient = new DefaultHttpClient();
		} else {
			httpClient = new DefaultHttpClient();
		}

		HttpGet httget = new HttpGet(request);
		httget.setHeader("User-Agent", "xiaobao-tsd-1.0");
		httget.setHeader("Authorzation", mAccesToken);
		httget.setHeader("Content-Type", "application/json");

		
		try {
			HttpResponse response = httpClient.execute(httget);
			int resStatus = response.getStatusLine().getStatusCode();
			if ( resStatus >= HttpStatus.SC_OK && resStatus < 300 ) {
				InputStream is = new BufferedInputStream(response.getEntity()
						.getContent());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[2048];
				int readLen;
				while ((readLen = is.read(buf)) != -1) {
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException();
					}
					baos.write(buf, 0, readLen);
				}
				result = baos.toString();
			}
			else {
				result = String.format("{\"errorCode\":%d}", resStatus);
			}
		} catch (Exception e) {
			LogUtil.e(LOG_TAG, "Exception raised at getForObject(), " + e.getMessage());
			e.printStackTrace();
			result = null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

	private <T> T deleteObject(String urlString, Class<T> responseType) {
		LogUtil.v(LOG_TAG, urlString);
		T result = null;
		Gson gson = new Gson();

		if(checkNetworkInfo()==-1)
		{
			String networkError="{\"errorCode\":-1}";
			result = gson.fromJson(networkError, responseType);
			return result;
		}
		if (mAccesToken == null)
			prepare();

		HttpClient httpClient;
		if (urlString.toLowerCase().startsWith("https://")) {
			httpClient = new DefaultHttpClient();
		} else {
			httpClient = new DefaultHttpClient();
		}

		HttpDelete httdelete = new HttpDelete(urlString);
		httdelete.setHeader("User-Agent", "xiaobao-tsd-1.0");
		httdelete.setHeader("Authorzation", mAccesToken);
		httdelete.setHeader("Content-Type", "application/json");

		
		try {
			HttpResponse response = httpClient.execute(httdelete);
			int resStatus=response.getStatusLine().getStatusCode();
			if ((resStatus>=HttpStatus.SC_OK)&&( resStatus<300))  {
				InputStream is = new BufferedInputStream(response.getEntity()
						.getContent());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[2048];
				int readLen;
				while ((readLen = is.read(buf)) != -1) {
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException();
					}
					baos.write(buf, 0, readLen);
				}
				String responseJson = new String(baos.toByteArray());
				LogUtil.v(LOG_TAG, responseJson);
				result = gson.fromJson(responseJson, responseType);
			}
			else
			{
				String networkError=String.format("{\"errorCode\":%d}", resStatus);
				result = gson.fromJson(networkError, responseType);
			}
		} catch (Exception e) {
			LogUtil.e(LOG_TAG, "Exception raised at deleteObject(), " + e.getMessage());
			e.printStackTrace();
			result = null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}
	
	private void prepare() {
		if (mContext != null) {
			mAccesToken = (String) HelperUtil.readFromCommonPreference(mContext, "access_token", "string");
			mDeviceId = (String) HelperUtil.readFromCommonPreference(mContext, "device_id", "string");
		}
	}
	
	public  int checkNetworkInfo()
    {
		int result=-1;
        ConnectivityManager conMan = (ConnectivityManager) mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);

        //mobile 3G Data Network
        State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

        //wifi
        State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

        
        //如果3G网络和wifi网络都未连接，且不是处于正在连接状态 则进入Network Setting界面 由用户配置网络连接
        if(mobile==State.CONNECTED||wifi==State.CONNECTED)
        	result=0;
        if(mobile==State.CONNECTING||wifi==State.CONNECTING)
        	result=1;   
        
        return result;        
    }

	/**
	 * 获取歌单列表
	 * 
	 * 
	 **/
	public GetAudioCategoryListRes getAudioCategoryList(String type) {
		String URL1 = URL + "audio/categorylist?type="+type;
		return getForObject(URL1, GetAudioCategoryListRes.class);
	}
	
	/**
	 * 获取指定歌单列表信息
	 * 
	 * 
	 **/
	public GetAudioCategoryListRes getAudioCategory(String category) {
		String URL1 = URL + "audio/categorylist?category="+category;
		return getForObject(URL1, GetAudioCategoryListRes.class);
	}
	
	/**
	 * 获取歌单歌曲列表
	 * 
	 * 
	 **/
	public GetAudioCategoryDetailRes getAudioCategoryDetailList(String category) {
		String URL1 = URL + "audio/category?category="+category;
		return getForObject(URL1, GetAudioCategoryDetailRes.class);
	}
	
	/**
	 * 获取歌单歌曲列表
	 * 
	 * 
	 **/
	public GetAudioFavouriteListRes getAudioFavouriteList() {
		if (mDeviceId == null)
			prepare();
		String URL1 = URL + "audio/favourite?user="+mDeviceId;
		return getForObject(URL1, GetAudioFavouriteListRes.class);
	}
	
	/**
	 * 添加收藏
	 * 
	 * 
	 **/
	public AudioRes submitAudioFavouriteAdd(AudioFavouriteAddReq favourites) {
			if (mDeviceId == null)
				prepare();
			String URL1 = URL + "audio/favourite?user="+mDeviceId+"&item="+favourites.items[0];
			favourites.user=mDeviceId;
			return postForObject(URL1, favourites, AudioRes.class);
	}
	/**
	 * 删除收藏
	 * 
	 * 
	 **/   
	public AudioRes submitAudioFavouriteDelete(AudioFavouriteDeleteReq favourites) {
			if (mDeviceId == null)
				prepare();
			String URL1 = URL + "audio/favourite?user="+mDeviceId+"&item="+favourites.items[0];
			favourites.user=mDeviceId;
			return deleteObject(URL1,  AudioRes.class);
	}
	
	/**
	 * 添加订阅
	 * 
	 * 
	 **/
	public AudioRes submitAudioSubscriptionAdd(String album,String type) {
			if (mDeviceId == null)
				prepare();
			String URL1 = URL + "audio/subscribes?device="+mDeviceId+"&album="+album+"&type="+type;
			return postForObject(URL1, null, AudioRes.class);
	}
	/**
	 * 删除订阅
	 * 
	 * 
	 **/   
	public AudioRes submitAudioSubscriptionDelete(String album,String type) {
			if (mDeviceId == null)
				prepare();
			String URL1 = URL + "audio/subscribes?device="+mDeviceId+"&album="+album+"&type="+type;
			return deleteObject(URL1,  AudioRes.class);
	}  
	
	/**
	 * 获取我订阅列表
	 * 
	 * 
	 **/
	public GetAudioSubscriptionListRes getAudioMySubscriptionList(String type) {
		if (mDeviceId == null)
			prepare();
		String URL1 = URL + "audio/subscribes?device="+mDeviceId+"&type="+type;
		return getForObject(URL1, GetAudioSubscriptionListRes.class);
	}
	
	/**
	 * 获取所有可以订阅列表
	 * 
	 * 
	 **/
	public GetAudioSubscriptionListRes getAudioAllSubscriptionList(String type) {
		if (mDeviceId == null)
			prepare();
		String URL1 = URL + "audio/subscribings?device="+mDeviceId+"&type="+type;
		return getForObject(URL1, GetAudioSubscriptionListRes.class);
	}
	
	/**
	 * 获取订阅列表详细信息
	 **/
	public GetAudioCategoryDetailRes getAudioSubscriptionDetail(String subscription) {
		String URL1 = URL + "audio/album/items?albums="+subscription;
		return getForObject(URL1, GetAudioCategoryDetailRes.class);
	}
	
	
	/**
	 * 音频搜索
	 * @param name 音频名称，支持模糊查询，可选项
	 * @param author 作者，支持模糊查询，可选项
	 * @param genre 曲风，如rap/古典，仅当type为music时，此字段有效
	 * @return
	 */
	public String queryAudio(String name, String author, String genre) {
		if (mDeviceId == null)
			prepare();
		String URL1 = URL + "audio/search?deviceId=" + mDeviceId + "&name=" + name
				+ "&author=" + author + "&genre=" + genre;
		return doGetRequest(URL1);
	}

	/**
	 * 发送更新log
	 * 
	 * 
	 **/
	public AudioRes submitUpdateLog(String app) {
			if (mDeviceId == null)
				prepare();
			String URL1 = URL + "update/log?id=id"+mDeviceId+"&apkId="+app;
			return postForObject(URL1, null, AudioRes.class);
	}
	
	
	/**
	 * 获取绑定车主信息机车机信息
	 * 
	 * 
	 **/
	public GetOwnerInfoRes getOwnerInfo(String detail) {
		if (mDeviceId == null)
			prepare();
		String URL1 = URL + "user/devices/owners/" + mDeviceId+"?detail=" + detail;
		return getForObject(URL1, GetOwnerInfoRes.class);
	}

}

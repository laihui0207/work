package com.tuyou.tsd.common.network;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

public class JsonOA {
	public static final String PROTO = ".json";
	private static String IMEI = "12345";
	private static String URL = "http://121.40.68.137/xiaobao/api/v1/";

	/**
	 * 获取TOKEN，暂定
	 * 
	 * 
	 **/
	public static TokenRes getToken() {
		String URL1 = "http://aituyou.gring.cn/oauth/oauth2/token?grant_type=client_credential&client_id=2DcAyIIO0L5dCV2fVgWF25&client_secret=6ytvEbwgm8qKUd6UdC9jlV";
		return getForObject(URL1, TokenRes.class);
	}

	/**
	 * 上传百度PUSH相关信息
	 * 
	 * 
	 **/
	public static PushRegisterRes loginPush(PushRegisterReq req) {
		String URL1 = URL + "push/register/" + IMEI + PROTO;
		return postForObject(URL1, req, PushRegisterRes.class);
	}

	/**
	 * 车机登陆
	 * 
	 * 
	 **/
	public static LoginRes login(LoginReq req) {
		String URL1 = URL + "user/devices/authc";
		return postForObject(URL1, req, LoginRes.class);
	}

	/**
	 * 车机登出
	 * 
	 * 
	 **/
	public static LogoutRes logout() {
		String URL1 = URL + "user/device/logout.json";
		return getForObject(URL1, LogoutRes.class);
	}

	/**
	 * 获取绑定车主信息机车机信息
	 * 
	 * 
	 **/
	public static GetOwnerInfoRes getOwnerInfo() {
		String URL1 = URL + "user/device/" + IMEI + "/owners" + PROTO
				+ "?detail=true";
		return getForObject(URL1, GetOwnerInfoRes.class);
	}

	/**
	 * 获取版本更新信息
	 * 
	 * 
	 **/
	public static GetUpdateInfoRes getUpdateInfo() {
		String URL1 = URL + "update/device/release.json";
		return getForObject(URL1, GetUpdateInfoRes.class);
	}

	/**
	 * 上传设备状态信息
	 * 
	 * 
	 **/
	public static SubmitDeviceStatusRes submitDeviceStatus(
			SubmitDeviceStatusReq req) {
		String URL1 = URL + "device/state/" + IMEI + PROTO;
		return postForObject(URL1, req, SubmitDeviceStatusRes.class);
	}

	/**
	 * 上传事故信息
	 * 
	 * 
	 **/
	public static SubmitAccidentInfoRes submitDeviceStatus(
			SubmitAccidentInfoReq req) {
		String URL1 = URL + "cardvr/accidents/" + IMEI + PROTO;
		return postForObject(URL1, req, SubmitAccidentInfoRes.class);
	}

	/**
	 * 获取天气预报信息
	 * 
	 * 
	 **/
	public static GetWeatherRes getWeatherInfo(String city, String timestamp) {
		String URL1 = URL + "weather/weather.json?city=" + city + "&time="
				+ timestamp;
		return getForObject(URL1, GetWeatherRes.class);
	}

	/**
	 * 获取设备信息
	 * 
	 * 
	 **/
	public static GetDeviceInfoRes getDeviceInfo() {
		String URL1 = "http://aituyou.gring.cn/xiaobao/api/v1/device/112233445566?oauth_token=32tgD5ADeueAXxMNrOQAB2";
		return getForObject(URL1, GetDeviceInfoRes.class);
	}

	/**
	 * 获取设备配置信息
	 * 
	 * 
	 **/
	public static GetConfigRes getConfigInfo(String module) {
		String URL1 = URL + "configs/" + module + "/" + IMEI + PROTO;
		return getForObject(URL1, GetConfigRes.class);
	}

	/**
	 * 上传设备配置信息
	 * 
	 * 
	 **/
	public static SubmitConfigRes setConfigInfo(String name,
			String content) {
		String URL1 = URL + "configs/" + name + "/" + IMEI + PROTO;
		SubmitConfigReq object= new SubmitConfigReq();
		object.content=content;
		return postForObject(URL1, object, SubmitConfigRes.class);
	}

	/**
	 * HTTP POST 上传服务器数据
	 * 
	 * 
	 **/
	public static <T> T postForObject(String urlString, Object request,
			Class<T> responseType) {
		HttpClient httpClient;
		if (urlString.toLowerCase().startsWith("https://")) {
			// httpClient = new MyHttpClient(context);
			httpClient = new DefaultHttpClient();
		} else {
			httpClient = new DefaultHttpClient();
		}
		Gson gson = new Gson();
		HttpPost httpost = new HttpPost(urlString);
		httpost.setHeader("User-Agent", "xiaobao-tsd-1.0");
		httpost.setHeader("Authorzation", "Bearer 32tgD5ADeueAXxMNrOQAB2");
	//	httpost.setHeader("Content-Type", " application/x-www-form-urlencoded");
		if (request != null) {
			String requestJson = gson.toJson(request);
			StringEntity stringEntity;
			try {
				stringEntity = new StringEntity(requestJson, "UTF-8");
				httpost.setEntity(stringEntity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		T result=null;
		try {
			HttpResponse response = httpClient.execute(httpost);
			int test = response.getStatusLine().getStatusCode();
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
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
				Log.v("communication", responseJson);
				 result = gson.fromJson(responseJson, responseType);
				//return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
			result=null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}	

	/**
	 * HTTP GET 获取服务器数据
	 * 
	 * 
	 **/
	public static <T> T getForObject(String urlString, Class<T> responseType) {
		HttpClient httpClient;
		Log.v("communication", urlString);
		if (urlString.toLowerCase().startsWith("https://")) {
			// httpClient = new MyHttpClient(context);
			httpClient = new DefaultHttpClient();
		} else {
			httpClient = new DefaultHttpClient();
		}
		Gson gson = new Gson();
		HttpGet httget = new HttpGet(urlString);
		httget.setHeader("User-Agent", "xiaobao-tsd-1.0");
		httget.setHeader("Authorzation", "Bearer 32tgD5ADeueAXxMNrOQAB2");
		httget.setHeader("Content-Type", "application/json");
		
		T result=null;
		try {
			HttpResponse response = httpClient.execute(httget);
			int test = response.getStatusLine().getStatusCode();
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
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
				Log.v("communication", responseJson);
				 result = gson.fromJson(responseJson, responseType);
				//return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
			result=null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}
}

package com.tuyou.tsd.common;

import java.util.Vector;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.qiniu.auth.Authorizer;
import com.qiniu.io.IO;
import com.qiniu.rs.CallBack;
import com.qiniu.rs.CallRet;
import com.qiniu.rs.PutExtra;
import com.qiniu.rs.UploadCallRet;
import com.tuyou.tsd.common.network.GetUploadTokenRes;
import com.tuyou.tsd.common.network.JsonOA2;

public class UploadManager {
	private static final String TAG = "UploadManager";
	private static Context mContext;
	private static UploadManager mUploadManager; 

	private UploadManager(Context context) {
		mContext = context;
	}
	
	public static UploadManager getUploadManager(Context context) {
		if (mUploadManager == null) {
			mUploadManager = new UploadManager(context);
		}
		return mUploadManager;
	}	
	
	private static Authorizer mVideoAuth = null;
	private static Authorizer mBlogAuth  = null;
	private static Authorizer mVideoAuth1 = null;
	private static Authorizer mThunmbnail  = null;
    private UploadThread mUploadProcess = new UploadThread();  
	private Vector<UploadInf> mUploadReqList = new Vector<UploadInf>();
	private Object mLock = new Object();

	public void start() {
		if (mUploadProcess != null) {
	   		mUploadProcess.start();	
		}
	}

	public void stop() {
		if (mUploadProcess != null) {
			mUploadProcess.stop = true;
		}
	}

	public void addUploadTask(UploadInf uploadInf) {
	    mUploadReqList.add(uploadInf);
		Log.d(TAG, "New upload task is added...");
    	synchronized (mLock) {
    		mLock.notify();
    	}
    }

	private class UploadThread extends Thread {
		private volatile boolean uploading = false;
		private boolean stop;

		@Override  
        public void run() {  
    		Log.d(TAG, "Upload thread started...");
        	while(!stop) {
        		if (uploading) {
        			Log.d(TAG, "Uploading, please wait...");
        			synchronized (mLock) {
        				try { mLock.wait(); } catch (InterruptedException e) {}
        			}
        		} else {
        			if(mUploadReqList.isEmpty()) {
        				Log.d(TAG, "Upload queue is empty, waiting...");
        				synchronized (mLock) {
        					try { mLock.wait(); } catch (InterruptedException e) {}
        				}
        			} else {
        				Log.d(TAG, "Start to upload...");
        				UploadInf uploadInf = (UploadInf)mUploadReqList.get(0);
        				preUpload(uploadInf.getUri(),uploadInf.getType());
        				doUpload(uploadInf);
        			}
        		}
        	}
    		Log.d(TAG, "Upload thread has stopped.");
        }  

		private void preUpload(Uri uri,String type) {
			final String reqType=type;
				
			Authorizer auth ;
			if(reqType.equals("mblog"))
			{
				auth=mBlogAuth;
			}			
			else if(reqType.equals("cardvr_video"))
			{
				auth=mVideoAuth1;
			}
			else if(reqType.equals("cardvr_thumbnail"))
			{
				auth=mThunmbnail;
			}
			else 
			{
				auth=mVideoAuth;
			}

			if (auth == null) {
				Log.d(TAG, "Upload token is empty, query one...");
    			new GetUploadTokenReqTask().execute(reqType);
    			synchronized (mLock) {
					try { mLock.wait(); } catch (InterruptedException e) {}
				}
			}
		}

		private void doUpload(UploadInf uploadInf) {
			Log.d(TAG, "doUpload uri =" + uploadInf.getUri() + ", deviceId = " + uploadInf.getDeviceId()+","+uploadInf.getDistrict()+","+uploadInf.getTimestamp()+","+uploadInf.getTimestamp());
			uploading = true;
			String key = IO.UNDEFINED_KEY; // Generate key automatically
//			String key = String.valueOf(System.currentTimeMillis());
			final String reqType = uploadInf.getType();		
			
			Authorizer auth ;
			if(reqType.equals("mblog"))
			{
				auth=mBlogAuth;
			}			
			else if(reqType.equals("cardvr_video"))
			{
				auth=mVideoAuth1;
			}
			else if(reqType.equals("cardvr_thumbnail"))
			{
				auth=mThunmbnail;
			}
			else 
			{
				auth=mVideoAuth;
			}
			
			
			PutExtra extra = new PutExtra();
			extra.params.put("x:device", uploadInf.getDeviceId());
			extra.params.put("x:timestamp", uploadInf.getTimestamp());
			extra.params.put("x:lat", uploadInf.getLat());
			extra.params.put("x:lng", uploadInf.getLng());
			extra.params.put("x:name", uploadInf.getName());
			extra.params.put("x:type", uploadInf.getType());
			extra.params.put("x:district", uploadInf.getDistrict());
			extra.params.put("x:address", uploadInf.getAddress());
			Log.d(TAG,"uploadInf.getDeviceId() = "+ uploadInf.getDeviceId());
			Log.d(TAG,"uploadInf.getTimestamp() = "+ uploadInf.getTimestamp());
			Log.d(TAG,"uploadInf.getLat() = " + uploadInf.getLat());
			Log.d(TAG,"uploadInf.getLng() = " + uploadInf.getLng());
			Log.d(TAG,"uploadInf.getName() = " +uploadInf.getName());
			Log.d(TAG,"uploadInf.getType() = " +uploadInf.getType());
			Log.d(TAG,"uploadInf.getDistrict() " +uploadInf.getDistrict());
			Log.d(TAG,"uploadInf.getAddress() =" + uploadInf.getAddress());
			
			IO.putFile(mContext, auth, key, uploadInf.getUri(), extra, new CallBack() {
				@Override
				public void onProcess(long current, long total) {
					int percent = (int)(current*100/total);
					Log.d(TAG, "uploading " + current + "/" + total + "  " + current/1024 + "K/" + total/1024 + "K; " + percent + "%");
				}

				@Override
				public void onSuccess(UploadCallRet ret) {
					uploading = false;
					Log.d(TAG, "Successed to upload! ret: " + ret.toString());
					mUploadReqList.remove(0);
					
			    	synchronized (mLock) {
			    		mLock.notify();
			    	}
				}

				@Override
				public void onFailure(CallRet ret) {
					uploading = false;
					switch(ret.getStatusCode()) {
					case 413:  //The upload content size exceeds the limitation.
					case 614:  //The target source already exists.
						mUploadReqList.remove(0);
						
				    	synchronized (mLock) {
				    		mLock.notify();
				    	}
						break;
					case 401:  //Token is expired or invalid.
						Log.d(TAG, "Token is expired or invalid. Query a new one...");
						/*if (reqType.equals("mblog")) {
							mBlogAuth = null;
						} else {
							mVideoAuth = null;
						}*/
						if(reqType.equals("mblog"))
						{
							mBlogAuth = null;
						}			
						else if(reqType.equals("cardvr_video"))
						{
							mVideoAuth1 = null;
						}
						else if(reqType.equals("cardvr_thumbnail"))
						{
							mThunmbnail = null;
						}
						else 
						{
							mVideoAuth = null;
						}
	        			new GetUploadTokenReqTask().execute(reqType);
						break;
					default:
						break;
					}
					Log.d(TAG, "Err: " + ret.toString());
				}
			});
		}

	}

	class GetUploadTokenReqTask extends AsyncTask<String, Void, GetUploadTokenRes> {
		private String reqType;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(final GetUploadTokenRes result) {
			Gson gson = new Gson();
			String resultJson = gson.toJson(result);
			Log.d(TAG, "resultJson = " + resultJson);
			if(result != null && result.configs != null) {
				if (reqType.equals("cardvr")) {
					String token = result.configs[0].content.cardvr;
					if (mVideoAuth == null)
						mVideoAuth = new Authorizer();
					mVideoAuth.setUploadToken(token);
				}
				else if (reqType.equals("mblog")) {
					String token = result.configs[0].content.mblog;
					if (mBlogAuth == null)
						mBlogAuth = new Authorizer();
					mBlogAuth.setUploadToken(token);
				}
				else if(reqType.equals("cardvr_video"))
				{
					String token = result.configs[0].content.cardvr_video;
					if (mVideoAuth1 == null)
						mVideoAuth1 = new Authorizer();
					mVideoAuth1.setUploadToken(token);
				}
				else if(reqType.equals("cardvr_thumbnail"))
				{
					String token = result.configs[0].content.cardvr_thumbnail;
					if (mThunmbnail == null)
						mThunmbnail = new Authorizer();
					mThunmbnail.setUploadToken(token);
				}
			}
			Log.d(TAG, "Got the token, continue uploading...");
	    	synchronized (mLock) {
	    		mLock.notify();
	    	}
		}

		@Override
		protected GetUploadTokenRes doInBackground(String... arg0) {
			if (arg0 == null)
				return null;
			reqType = arg0[0];
//			Log.d(TAG, "GetUploadTokenReqTask, reqType = " + reqType);
			return JsonOA2.getInstance(mContext).getUploadToken(reqType);
		}
	}
}

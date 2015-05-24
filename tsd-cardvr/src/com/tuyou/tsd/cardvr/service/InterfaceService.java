package com.tuyou.tsd.cardvr.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

import com.tuyou.tsd.cardvr.service.videoMeta.PictureInfoDAO;
import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.common.videoMeta.PictureInf;
import com.tuyou.tsd.common.videoMeta.VideoInf;
import com.tuyou.tsd.common.videoMeta.VideoManager;
import com.tuyou.tsd.common.videoMeta.VideoSetInf;
import com.tuyou.tsd.common.videoMeta.VideoStatInf;
import com.tuyou.tsd.common.videoMeta.VideoStatInf.VIDEO_TYPE;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class InterfaceService extends Service {

	static final String META_PATH = TSDConst.TUYOU_ROOT_PATH + "/meta";

	private ServiceBinder mServiceBinder = new ServiceBinder();
	private MyReceiver mReceiver = null;
	private VideoManager mVideoManager = null;

	@Override
	public void onCreate() {
		
		super.onCreate();
		mReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(TSDEvent.Push.MESSAGE_ARRIVED);
		filter.addAction(TSDEvent.Httpd.MESSAGE_ARRIVED);
		registerReceiver(mReceiver, filter);

		mVideoManager = VideoManager.getInstance(this);
	}

	@Override
	public void onDestroy() {
		
		super.onDestroy();
		if (mReceiver != null) {
			this.unregisterReceiver(mReceiver);
		}
	}

	@Override
	public void onRebind(Intent intent) {
		
		super.onRebind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		
		return mServiceBinder;
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			

			switch (msg.what) {
			case 100:
				break;
			default:
				break;
			}
		}
	};

	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			
			String action = arg1.getAction();
			LogUtil.v("AudioPlayerService", "received the broadcast: " + action);
			if (action.equals(TSDEvent.Push.MESSAGE_ARRIVED)) {
				disposeMessage(0, arg1);
			} else if (action.equals(TSDEvent.Httpd.MESSAGE_ARRIVED)) {
				disposeMessage(1, arg1);
			}
		}

	}

	public void disposeMessage(int src, Intent arg1) {
		try {
			JSONObject jsobject = new JSONObject(arg1.getStringExtra("message"));
			LogUtil.i("cardvr", "httpd broadcast=" + jsobject.toString());

			if (jsobject.getString("module").equals("cardvr")) {
				String type = jsobject.getString("type");
				String seq = null;
				String routeKey = null;
				if (src == 0) {
					seq = jsobject.getString("seq");
					routeKey = jsobject.getString("routeKey");
				}
				Intent intent;
				if (src == 0) {
					intent = new Intent(TSDEvent.Push.FEED_BACK);
				} else {
					intent = new Intent(TSDEvent.Httpd.FEED_BACK);
				}
				JSONObject result = new JSONObject();
				result.put("module", "cardvr");
				if (src == 0) {
					result.put("seq", seq);
					result.put("routeKey", routeKey);
				}
				result.put("type", type);

				if (type.equals("getVideoStats")) {
					result.put("content", getVideoStats());
					intent.putExtra("message", result.toString());
					InterfaceService.this.sendBroadcast(intent);

				} else if (type.equals("getVideoSets")) {
					JSONObject content = jsobject.getJSONObject("content");
					String subtype = content.getString("type");
					String starttime = null;
					String endtime = null;
					try {
						starttime = content.getString("startTime");
						endtime = content.getString("endTime");
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (subtype.equals("normal")) {
						result.put("content",
								getNormalVideoSets(starttime, endtime));
					} else if (subtype.equals("favourite")) {
						result.put(
								"content",
								getSpecialVideoSets("favourite", starttime,
										endtime));
					} else if (subtype.equals("event")) {
						result.put(
								"content",
								getSpecialVideoSets("event", starttime, endtime));
					}
					intent.putExtra("message", result.toString());
					InterfaceService.this.sendBroadcast(intent);

				} else if (type.equals("getVideoSetItems")) {
					JSONObject content = jsobject.getJSONObject("content");
					String starttime = content.getString("startTime");
					result.put("content", getVideoSetItems(starttime));
					intent.putExtra("message", result.toString());
					InterfaceService.this.sendBroadcast(intent);
				} else if (type.equals("getVideo")) {
					JSONObject content = jsobject.getJSONObject("content");
					String name = content.getString("name");
					boolean needUpload = content.getBoolean("needUpload");

					if (needUpload) {// 上传文件
						upLoadVideo(name);
						intent.putExtra("message", result.toString());
						InterfaceService.this.sendBroadcast(intent);
					} else {
						result.put("content", getVideo(name));
						intent.putExtra("message", result.toString());
						InterfaceService.this.sendBroadcast(intent);
					}

				} else if (type.equals("updateVideo")) {
					JSONArray content = jsobject.getJSONArray("content");
					updateVideo(content);
					intent.putExtra("message", result.toString());
					InterfaceService.this.sendBroadcast(intent);
				} else if (type.equals("deleteVideo")) {
					JSONArray content = jsobject.getJSONArray("content");
					for (int i = 0; i < content.length(); i++) {
						String obj = content.getString(i);
						String name = obj;// obj.toString();
						mVideoManager.deleteVideo(name);
					}
					intent.putExtra("message", result.toString());
					InterfaceService.this.sendBroadcast(intent);
				} else if (type.equals("getVideoThumbnail")) {
					JSONObject content = jsobject.getJSONObject("content");
					JSONArray jsonname = content.getJSONArray("videoNames");
					boolean needUpload = content.getBoolean("needUpload");
					String[] names = new String[jsonname.length()];
					for (int i = 0; i < jsonname.length(); i++) {
						names[i] = jsonname.getString(i);
					}

					if (needUpload) {// 七牛
						upLoadThumbnail(names);
						intent.putExtra("message", result.toString());
						InterfaceService.this.sendBroadcast(intent);
					} else {
						result.put("content", getVideoThumbnail(names));
						intent.putExtra("message", result.toString());
						InterfaceService.this.sendBroadcast(intent);
					}
				} else if (type.equals("deleteImage")) {
					intent.putExtra("message", result.toString());
					InterfaceService.this.sendBroadcast(intent);
				}
				LogUtil.i("cardvr", "httpd result=" + result.toString());
			}else if(jsobject.getString("module").equals("mblogs")){
				Intent intent;
				if (src == 0) {
					intent = new Intent(TSDEvent.Push.FEED_BACK);
				} else {
					intent = new Intent(TSDEvent.Httpd.FEED_BACK);
				}
				JSONObject result = new JSONObject();
				result.put("module", "mblogs");
				result.put("type", "getPhotoStats");
				result.put("content", PictureInfoDAO.getInstance(InterfaceService.this).getVideoSize().size());
				intent.putExtra("message", result.toString());
				InterfaceService.this.sendBroadcast(intent);
					
			}else if(jsobject.getString("module").equals("mblogsDetailed")){
				Intent intent;
				if (src == 0) {
					intent = new Intent(TSDEvent.Push.FEED_BACK);
				} else {
					intent = new Intent(TSDEvent.Httpd.FEED_BACK);
				}
				JSONObject result = new JSONObject();
				result.put("module", "mblogsDetailed");
				result.put("type", "getPhoto");
				JSONObject content = jsobject.getJSONObject("content");
				ArrayList<PictureInf> list = PictureInfoDAO.getInstance(InterfaceService.this).getVideoSize(content.getString("value").substring(content.getString("value").indexOf("=")+1,content.getString("value").indexOf("=")+19),Integer.valueOf(content.getString("value").substring(content.getString("value").lastIndexOf("=")+1)));
				result.put("content", list);
				JSONArray objArr = new JSONArray();
				for(int i=0;i<list.size();i++){
					JSONObject json = new JSONObject();
					PictureInf inf = list.get(i);
					json.put("mblogId", inf.name);
					json.put("timestamp", HelperUtil.getCurrentTimestamp(inf.timestamp));
					json.put("lat", inf.location.lat);
					json.put("lng", inf.location.lng);
					json.put("address", inf.address);
					json.put("district", inf.district);
					JSONObject obj = new JSONObject();
					obj.put("url","/pictures/"+inf.name+".jpg");
					json.put("url", obj);
					objArr.put(json);
				}
				result.put("content", objArr);
				intent.putExtra("message", result.toString());
				InterfaceService.this.sendBroadcast(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void upLoadVideo(String name) {
		VideoInf video = mVideoManager.getVideo(name);
		String type=null;
		if((video.tag&VideoInf.ACCIDENT_FLAG)>0)
		{
			type="event";
		}
		else if((video.tag&VideoInf.FAVORITE_FLAG)>0)
		{
			type="favourite";
		}
		else
		{
			type="normal";
		}
			
		if (video != null) {
			String path=mVideoManager.getVideoPath(name);
			mVideoManager.uploadFile(path,
					String.valueOf(video.timestamp),
					String.valueOf(video.location.lng),
					String.valueOf(video.location.lng),"cardvr_video",
					video.name,type,video.district,video.address);
		}
	}

	private void upLoadThumbnail(String[] names) {
		for (int i = 0; i < names.length; i++) {
			VideoInf video = mVideoManager.getVideo(names[i]);
			String type=null;
			if((video.tag&VideoInf.ACCIDENT_FLAG)>0)
			{
				type="event";
			}
			else if((video.tag&VideoInf.FAVORITE_FLAG)>0)
			{
				type="favourite";
			}
			else
			{
				type="normal";
			}
			if (video != null) {
				String path = TSDConst.CAR_DVR_VIDEO_PATH + "/" + names[i]
						+ ".jpg";
				mVideoManager.uploadFile(path,
						String.valueOf(video.timestamp),
						String.valueOf(video.location.lng),
						String.valueOf(video.location.lng),"cardvr_thumbnail",
						names[i],type,"test","test");
			}
		}
	}

	private void updateVideo(JSONArray content) {
		for (int i = 0; i < content.length(); i++) {
			try {
				JSONObject v = content.getJSONObject(i);
				String name = v.getString("name");
				try {
					String type = v.getString("type");
					if (type.equals("favourite")) {
						mVideoManager.setFavoriteVideo(name);
					} else {
						mVideoManager.cancelFavoriteVideo(name);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					boolean read = v.getBoolean("read");
					if (read) {
						mVideoManager.setVideoAsRead(name);
					} else {

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private JSONArray getVideoThumbnail(String[] names) {
		JSONArray content = new JSONArray();
		try {
			for (int i = 0; i < names.length; i++) {
				JSONObject v = new JSONObject();
				v.put("name", names[i]);
				v.put("thumbnail", TSDConst.CAR_DVR_VIDEO_PATH+ "/" + names[i]
						+ ".jpg");
				content.put(i, v);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}

	public String getVideoThumbnail(String filename) {
		
		String result = TSDConst.CAR_DVR_VIDEO_PATH + "/" + filename + ".jpg";
		return result;
	}

	private JSONObject getVideo(String name) {

		String videopath = mVideoManager.getVideoPath(name);
		JSONObject result = new JSONObject();
		try {
			result.putOpt("name", name);
			if(mVideoManager.getVideoType(name).toString().toLowerCase().equals("accident")){
				result.putOpt("type", "event");
			}else if(mVideoManager.getVideoType(name).toString().toLowerCase().equals("favorite")){
				result.putOpt("type", "favourite");
			}else{
				result.putOpt("type", "normal");
			}
			
			result.putOpt("url", videopath);
			result.putOpt("address", mVideoManager.getVideo(name).address);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private String getVideo1(String name) {
		String result = mVideoManager.getVideoPath(name);
		return result;
	}

	private JSONArray getVideoSetItems(String startTime) {

		long start = 0;
		long end = 0;
		JSONArray content = new JSONArray();
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssZ");
			Date convertedDate = new Date();
			if (startTime != null) {
				try {
					convertedDate = dateFormat.parse(startTime);
					start = convertedDate.getTime();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			end = start + 10 * 60 * 1000;

			// List<VideoSetInf> setinfs = mVideoManager.getVideoSets(0, start,
			// end);
			List<VideoInf> videoset = mVideoManager.getVideos(0, start, end);
			if ((videoset != null) && (videoset.size() > 0)) {
				for (int i = 0; i < videoset.size(); i++) {
					try {
						JSONObject v = new JSONObject();
						VideoInf video = videoset.get(i);
						v.put("name", video.name);
						v.put("timestamp", video.timestamp);
						v.put("duration", 10);
						if ((video.tag & VideoInf.FAVORITE_FLAG) > 0) {
							v.put("favourite", true);
						} else {
							v.put("favourite", false);
						}
						content.put(i, v);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}
/*
	private List<VideoSetInf> getVideoSetInf(int type, String startTime,
			String endTime) {
		long start = 0;
		long end = 0;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssZ");
			Date convertedDate = new Date();
			if (startTime != null) {
				try {
					convertedDate = dateFormat.parse(startTime);
					start = convertedDate.getTime();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (endTime != null) {
				try {
					convertedDate = dateFormat.parse(endTime);
					end = convertedDate.getTime();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<VideoSetInf> setinfs = mVideoManager.getVideoSets(0, start, end);
		return setinfs;
	}
*/
	private List<VideoSetInf> getVideoSetlocal(List<VideoInf> videos) {
		List<VideoSetInf> result = null;

		if ((videos != null) && (videos.size() > 0)) {
			result = new ArrayList<VideoSetInf>();
			VideoSetInf videoset = new VideoSetInf();
			long starttime = videos.get(0).timestamp;
			videoset.addVideo(videos.get(0));
			for (int i = 1; i < videos.size(); i++) {
				VideoInf video = videos.get(i);
				if (video.timestamp < (starttime + 10 * 60 * 1000)) {
					videoset.addVideo(video);
				} else {
					videoset.mStartTime = videoset.mVideoList.get(0).timestamp;
					result.add(0, videoset);
					videoset = new VideoSetInf();
					starttime = video.timestamp;
					videoset.addVideo(video);
				}
			}
			videoset.mStartTime = videoset.mVideoList.get(0).timestamp;
			result.add(0, videoset);
		}

		return result;
	}

	private JSONArray getNormalVideoSets(String startTime, String endTime) {

		LogUtil.d("Interface", "Enter getNormalVideoSets, " + ", startTime: "
				+ startTime + ", endTime: " + endTime);

		JSONArray content = new JSONArray();
		try {
			// List<VideoSetInf> setinfs = getVideoSetInf(0, startTime,
			// endTime);
			long start = 0;
			long end = 0;
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ssZ");
				Date convertedDate = new Date();
				if (startTime != null) {
					try {
						convertedDate = dateFormat.parse(startTime);
						start = convertedDate.getTime();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (endTime != null) {
					try {
						convertedDate = dateFormat.parse(endTime);
						end = convertedDate.getTime();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			List<VideoInf> videoinfs = mVideoManager.getVideos(0, start, end);
			List<VideoSetInf> setinfs = getVideoSetlocal(videoinfs);

			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssZ");

			if ((setinfs != null) && (setinfs.size() > 0)) {
				for (int i = 0; i < setinfs.size(); i++) {
					VideoSetInf videoset = setinfs.get(i);
					JSONObject jsonobj = new JSONObject();
					Date date = new Date(videoset.mStartTime);
					try {
						jsonobj.put("startTime", dateFormat.format(date));
						jsonobj.put("type", "normal");

						JSONArray special = new JSONArray();
						boolean isHaveFlag = false;
						for (int k = 0; k < videoset.mVideoList.size(); k++) {
							if ((videoset.mVideoList.get(k).tag & VideoInf.ACCIDENT_FLAG) > 0) {
								special.put("event");
								isHaveFlag = true;
								break;
							}
						}
						for (int k = 0; k < videoset.mVideoList.size(); k++) {
							if ((videoset.mVideoList.get(k).tag & VideoInf.FAVORITE_FLAG) > 0) {
								special.put("favourite");
								isHaveFlag = true;
								break;
							}
						}
						if (isHaveFlag) {
							jsonobj.put("specials", special);
						}
						content.put(i, jsonobj);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogUtil.d("Interface", "Exit getNormalVideoSets, " + ", startTime: "
				+ startTime + ", endTime: " + endTime);
		return content;
	}

	private JSONArray getSpecialVideoSets(String type, String startTime,
			String endTime) {
		LogUtil.d("Interface", "Enter getSpecialVideoSets, " + ", startTime: "
				+ startTime + ", endTime: " + endTime);
		int tt = 1;
		if (type.equals("event")) {
			tt = 1;
		} else if (type.equals("favourite")) {
			tt = 2;
		}
		long start = 0;
		long end = 0;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssZ");
			Date convertedDate = new Date();
			if (startTime != null) {
				try {
					convertedDate = dateFormat.parse(startTime);
					start = convertedDate.getTime();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (endTime != null) {
				try {
					convertedDate = dateFormat.parse(endTime);
					end = convertedDate.getTime();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONArray content = new JSONArray();
		try {

			List<VideoInf> videoinfs = mVideoManager.getVideos(tt, start, end);
			List<VideoSetInf> videosetinfs = new ArrayList<VideoSetInf>();
			if ((videoinfs != null) && (videoinfs.size() > 0)) {
				VideoSetInf newSet = new VideoSetInf();
				newSet.addVideo(videoinfs.get(0));
				for (int i = 1; i < videoinfs.size(); i++) {
					VideoInf video = videoinfs.get(i);
					if (video.timestamp < (videoinfs.get(i - 1).timestamp + 20 * 1000)) {
						newSet.addVideo(video);
					} else {
						videosetinfs.add(0, newSet);
						newSet = new VideoSetInf();
						newSet.addVideo(videoinfs.get(i));
					}
				}
				videosetinfs.add(0, newSet);
			}
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssZ");
			if ((videosetinfs != null) && (videosetinfs.size() > 0)) {
				for (int i = 0; i < videosetinfs.size(); i++) {
					VideoSetInf videoset = videosetinfs.get(i);
					JSONObject jsonobj = new JSONObject();
					Date date = new Date(videoset.mVideoList.get(0).timestamp);
					try {
						jsonobj.put("startTime", dateFormat.format(date));
						jsonobj.put("type", type);						
						jsonobj.put("read", true);
						JSONArray videos = new JSONArray();
						for (int j = 0; j < videoset.mVideoList.size(); j++) {
							JSONObject v = new JSONObject();
							VideoInf video = videoset.mVideoList.get(j);
							v.put("name", video.name);
							v.put("timestamp", video.timestamp);
							v.put("duration", 10);
							videos.put(j, v);

							if (j == 0) {
								if ((video.tag & VideoInf.UNOPEN_FLAG) > 0) {
									jsonobj.put("read", false);
								}
								jsonobj.put("lng", video.location.lng);
								jsonobj.put("lat", video.location.lat);
								jsonobj.put("district", video.district);
								jsonobj.put("address", video.address);
							}
						}
						jsonobj.put("videoes", videos);
					} catch (Exception e) {
						e.printStackTrace();
					}
					content.put(i, jsonobj);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogUtil.d("Interface", "Exit getSpecialVideoSets, " + ", startTime: "
				+ startTime + ", endTime: " + endTime);
		return content;
	}

	private JSONArray getVideoStats() {
		JSONArray content = new JSONArray();
		JSONObject[] videostats = new JSONObject[3];
		videostats[0] = new JSONObject();
		videostats[1] = new JSONObject();
		videostats[2] = new JSONObject();
		List<VideoStatInf> videoStatInf = mVideoManager.getVideoStats();
		if ((videoStatInf != null) && (videoStatInf.size() > 0)) {
			for (int i = 0; i < videoStatInf.size(); i++) {
				VideoStatInf vvv = videoStatInf.get(i);
				try {
					if (vvv.mVideoType == VideoStatInf.VIDEO_TYPE.NORMAL) {
						videostats[0].put("type", "normal");
						videostats[0].put("total", vvv.mVideoCount);
						videostats[0].put("unread", vvv.mUnreadCount);
						content.put(0, videostats[0]);
					} else if (vvv.mVideoType == VideoStatInf.VIDEO_TYPE.ACCIDENT) {
						videostats[1].put("type", "event");
						videostats[1].put("total", vvv.mVideoCount);
						videostats[1].put("unread", vvv.mUnreadCount);
						content.put(1, videostats[1]);
					} else if (vvv.mVideoType == VideoStatInf.VIDEO_TYPE.FAVORITE) {
						videostats[2].put("type", "favourite");
						videostats[2].put("total", vvv.mVideoCount);
						videostats[2].put("unread", vvv.mUnreadCount);
						content.put(2, videostats[2]);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return content;
	}

	public void setFavoriteVideo(String name) {
		

	}


	public class ServiceBinder extends Binder implements IInterfaceService {
		@Override
		public JSONArray getVideoStats() {
			
			return InterfaceService.this.getVideoStats();
		}

		@Override
		public void setFavoriteVideo(String name, boolean isFavourite) {
			
			if (isFavourite) {
				mVideoManager.setFavoriteVideo(name);
				// isFavouritetest=isFavourite;
			} else {
				mVideoManager.cancelFavoriteVideo(name);
				// isFavouritetest=isFavourite;
			}
		}

		@Override
		public String getVideoThumbnail(String filename) {
			
			return InterfaceService.this.getVideoThumbnail(filename);
		}

		@Override
		public JSONObject getVideoSets(String type, String startTime,
				String endTime) {
			
			JSONObject result = new JSONObject();
			try {
				if (type.equals("normal")) {
					result.put("content",
							getNormalVideoSets(startTime, endTime));
				} else if (type.equals("favourite")) {
					result.put(
							"content",
							getSpecialVideoSets("favourite", startTime, endTime));
				} else if (type.equals("event")) {
					result.put("content",
							getSpecialVideoSets("event", startTime, endTime));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		public JSONArray getVideoSetItems(String startTime) {
			
			return InterfaceService.this.getVideoSetItems(startTime);
		}

		@Override
		public String getVideo(String name) {
			
			return getVideo1(name);
		}

		@Override
		public void setRead(String name) {
			
			mVideoManager.setVideoAsRead(name);
		}

		@Override
		public boolean getFavoriteStatus(String name) {
			
			VIDEO_TYPE type = mVideoManager.getVideoType(name);
			if (type == VIDEO_TYPE.FAVORITE)
				return true;
			else
				return false;
		}

		@Override
		public boolean getIsAccident(String name) {
			
			VIDEO_TYPE type = mVideoManager.getVideoType(name);
			if (type == VIDEO_TYPE.ACCIDENT)
				return true;
			else
				return false;
		}

	}

}

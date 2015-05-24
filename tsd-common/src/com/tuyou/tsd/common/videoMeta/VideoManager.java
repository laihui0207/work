package com.tuyou.tsd.common.videoMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.tuyou.tsd.common.TSDConst;
import com.tuyou.tsd.common.UploadInf;
import com.tuyou.tsd.common.UploadManager;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.videoMeta.VideoContent.VideoMeta;
import com.tuyou.tsd.common.videoMeta.VideoStatInf.VIDEO_TYPE;

public class VideoManager {
	private static final String LOG_TAG = "VideoManager";
	Context mContext;
	private static VideoManager mVideoManager = null;
	private static UploadManager mUploadManager;
	private static String mDeviceId = null;
	
	private VideoManager(Context context) {
		mContext = context;
		mDeviceId = (String) HelperUtil.readFromCommonPreference(context, "device_id", "string");
	}
	
	public static VideoManager getInstance(Context context) {
		if (mVideoManager == null) {
			mVideoManager = new VideoManager(context);
			mUploadManager = UploadManager.getUploadManager(context);
	        mUploadManager.start();
		}
		
		return mVideoManager;
	}
	
	public List<VideoStatInf> getVideoStats() {
		Log.d(LOG_TAG, "Enter getVideoStats");
		
		ArrayList<VideoStatInf> videoStatList = new ArrayList<VideoStatInf>();
		for (int i = 0 ; i < 3; i++) {
			Cursor cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.UNREAD_FLAG}, VideoMeta.TYPE + "=" + i, null, null);
			VIDEO_TYPE videoType;
			switch(i) {
				case 0:
					videoType = VIDEO_TYPE.NORMAL;
					break;
				
				case 1:
					videoType = VIDEO_TYPE.ACCIDENT;
					break;
					
				case 2:
				default:
					videoType = VIDEO_TYPE.FAVORITE;
			}
			
			if (cursor == null) {
				continue;
			}
			
			int count = cursor.getCount();
			int unreadCount = 0;
			if (i==1) {		//Accident, get the unread count.
				while (cursor.moveToNext()) {
					if (cursor.getInt(0) == 1) {
						unreadCount ++;
					}
				}
			}
			Log.d(LOG_TAG, "type: " + videoType + ", count: " + count + ", unreadCount: " + unreadCount);
			
			videoStatList.add(new VideoStatInf(videoType, count, unreadCount));
			cursor.close();
		}
		
		Log.d(LOG_TAG, "Leave getVideoStats");
		return videoStatList;
	}
	
	public List<VideoInf> getVideos(int type, long startTime, long endTime) {
		Log.d(LOG_TAG, "Enter getVideos, type: " + type +  ", startTime: " + new Date(startTime) + ", endTime: " + new Date(endTime));
		
		Cursor cursor = null;
		ArrayList <VideoInf> videoList = new ArrayList<VideoInf>();
		if (startTime == 0L && endTime == 0L) {
			if ( type == 0) {//All types
				cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
						VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP, VideoMeta.TYPE, VideoMeta.UNREAD_FLAG,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
						null, null, VideoMeta.TIMESTAMP + " ASC");
			} else  { // 1: Accident, 2: Favorite
				cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
						VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP, VideoMeta.TYPE, VideoMeta.UNREAD_FLAG,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
						VideoMeta.TYPE + "=" + type, null, VideoMeta.TIMESTAMP + " ASC");
			}
		} else if (startTime == 0L) {
			if ( type == 0) { //All types
				cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
						VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP, VideoMeta.TYPE, VideoMeta.UNREAD_FLAG,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
						VideoMeta.TIMESTAMP + "<=" + endTime, null, VideoMeta.TIMESTAMP + " ASC");
			} else  { // 1: Accident, 2: Favorite
				cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
						VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP, VideoMeta.TYPE, VideoMeta.UNREAD_FLAG,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
						VideoMeta.TYPE + "=" + type + " AND " +  VideoMeta.TIMESTAMP + "<=" + endTime, null, VideoMeta.TIMESTAMP + " ASC");
			}
		} else if (endTime == 0L) {
			if ( type == 0) { //All types
				cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
						VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP, VideoMeta.TYPE, VideoMeta.UNREAD_FLAG,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
						VideoMeta.TIMESTAMP + ">=" + startTime, null, VideoMeta.TIMESTAMP + " ASC");
			} else  { // 1: Accident, 2: Favorite
				cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
						VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP, VideoMeta.TYPE, VideoMeta.UNREAD_FLAG,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
						VideoMeta.TYPE + "=" + type + " AND " + VideoMeta.TIMESTAMP + ">=" + startTime, null, VideoMeta.TIMESTAMP + " ASC");
			}
		} else {
			if ( type == 0) { //All types
				cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
						VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP, VideoMeta.TYPE, VideoMeta.UNREAD_FLAG,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
						VideoMeta.TIMESTAMP + ">=" + startTime + " AND " +  VideoMeta.TIMESTAMP + "<=" + endTime, null, VideoMeta.TIMESTAMP + " ASC");
			} else  { // 1: Accident, 2: Favorite
				cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
						VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP, VideoMeta.TYPE, VideoMeta.UNREAD_FLAG,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
						VideoMeta.TYPE + "=" + type + " AND " + VideoMeta.TIMESTAMP + ">=" + startTime + " AND " +  VideoMeta.TIMESTAMP + "<=" + endTime, 
						null, VideoMeta.TIMESTAMP + " ASC");
			}
		}
		
		if (cursor != null) {
			if(cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					VideoInf videoInf = new VideoInf();
					videoInf.name = cursor.getString(0);
					videoInf.location.lng = cursor.getString(1);
					videoInf.location.lat = cursor.getString(2);
					videoInf.timestamp = cursor.getLong(3);
					switch(cursor.getInt(4)) {
					case 0:
						videoInf.tag = 0;
						break;
					
					case 1:
						if (cursor.getInt(5) ==1 ) {
							videoInf.tag = VideoInf.ACCIDENT_FLAG | VideoInf.UNOPEN_FLAG;
						} else {
							videoInf.tag = VideoInf.ACCIDENT_FLAG;	
						}
						break;
					
					case 2:
						videoInf.tag = VideoInf.FAVORITE_FLAG;
						break;
					
					default:
						break;
					}
					videoInf.district=cursor.getString(6);
					videoInf.address=cursor.getString(7);
					videoList.add(videoInf);
					Log.d(LOG_TAG, "VideoInf: name: " + videoInf.name + ", timestamp: " + videoInf.timestamp + ", tag: " + videoInf.tag);
				}
			}
			cursor.close();
			cursor = null;
		}
		
		return videoList;
	}
	
	public List<VideoSetInf> getVideoSets(int type, long startTime, long endTime) {
		Log.d(LOG_TAG, "Enter getVideoSets, type: " + type +  ", startTime: " + new Date(startTime) + ", endTime: " + new Date(endTime));
		
		Cursor cursor = null;
		int timeOffset = 10 * 60 * 1000; //10 mins
		
		ArrayList <VideoSetInf> videoSetList = new ArrayList<VideoSetInf>();
		
		//Don't set the startTime, we need get the first timestamp from our DB.
		if (startTime == 0L) {
			if (endTime == 0L) {
				cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.TIMESTAMP}, 
							null , null, VideoMeta.TIMESTAMP + " ASC");
			} else {
				cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.TIMESTAMP}, 
							VideoMeta.TIMESTAMP + "<=" + endTime , null, VideoMeta.TIMESTAMP + " ASC");
			}
			
			if (cursor != null) {
				if (cursor.getCount() == 0) {
					cursor.close();
					cursor = null;
					return videoSetList;
				}
				
				cursor.moveToFirst();
				startTime = cursor.getLong(0);
				cursor.close();
				cursor = null;
				
				if (endTime != 0L) {
					startTime = HelperUtil.alignStartTimestamp(startTime);
					endTime = HelperUtil.alignEndTimestamp(endTime);
					Log.d(LOG_TAG, "startTime is " +  new Date(startTime) + ", endTime is " + new Date(endTime));
					
					for (long time = endTime; time > startTime; time -= timeOffset) {
						//All type
						if ( type == 0) {
							cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
									VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP, VideoMeta.TYPE, VideoMeta.UNREAD_FLAG,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
									VideoMeta.TIMESTAMP + ">=" + (time - timeOffset) + " AND " +  VideoMeta.TIMESTAMP + "<=" + time, null, VideoMeta.TIMESTAMP + " ASC");
						} else  { // 1: Accident, 2: Favorite
							cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
									VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP, VideoMeta.TYPE, VideoMeta.UNREAD_FLAG,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
									VideoMeta.TYPE + "=" + type + " AND " + VideoMeta.TIMESTAMP + ">=" + (time - timeOffset) + " AND " +  VideoMeta.TIMESTAMP + "<=" + time, null, VideoMeta.TIMESTAMP + " ASC");
						}
							
						if (cursor != null) { 
							if(cursor.getCount() > 0) {
								VideoSetInf videoSetInf = new VideoSetInf();
								videoSetInf.mStartTime = 0L;
								
								while (cursor.moveToNext()) {
									if (videoSetInf.mStartTime == 0L) {
										videoSetInf.mStartTime = cursor.getLong(3);
									}
									VideoInf videoInf = new VideoInf();
									videoInf.name = cursor.getString(0);
									videoInf.location.lng = cursor.getString(1);
									videoInf.location.lat = cursor.getString(2);
									videoInf.timestamp = cursor.getLong(3);
									switch(cursor.getType(4)) {
									case 0:
										videoInf.tag = 0;
										break;
									
									case 1:
										if (cursor.getInt(5) ==1 ) {
											videoInf.tag = VideoInf.ACCIDENT_FLAG | VideoInf.UNOPEN_FLAG;
										} else {
											videoInf.tag = VideoInf.ACCIDENT_FLAG;	
										}
										break;
									
									case 2:
										videoInf.tag = VideoInf.FAVORITE_FLAG;
										break;
									
									default:
										break;
									}
									videoInf.district=cursor.getString(6);
									videoInf.address=cursor.getString(7);
									videoSetInf.addVideo(videoInf);
									Log.d(LOG_TAG, "VideoInf: name: " + videoInf.name + ", timestamp: " + videoInf.timestamp + ", tag: " + videoInf.tag);
								}
								videoSetList.add(videoSetInf);
							}
							cursor.close();
							cursor = null;
						}
					}
					Log.d(LOG_TAG, "Leave getVideoSets");
					return videoSetList;
				}
			}
		} 
		
		long updatedTime; 
		//Don't set the endTime, we need get the last timestamp from our DB.
		if (endTime == 0L) { 
			Log.d(LOG_TAG, "endTime is 0");
			cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.TIMESTAMP}, 
					VideoMeta.TIMESTAMP + ">=" + startTime , null, VideoMeta.TIMESTAMP + " DESC");
			if (cursor != null) {
				cursor.moveToFirst();
				endTime = cursor.getLong(0);
				Log.d(LOG_TAG, "updated endTime is " + new Date(endTime));
				cursor.close();
			}
			updatedTime = endTime;
		} else {
			updatedTime = endTime - timeOffset;
		}
		
		startTime = HelperUtil.alignStartTimestamp(startTime);
		updatedTime = HelperUtil.alignEndTimestamp(updatedTime);
		Log.d(LOG_TAG, "startTime is " +  new Date(startTime) + ", endTime is " + new Date(updatedTime));
		
		for (long time = startTime; time <= updatedTime; time += timeOffset) { 
			if ( type == 0) {	//0: All type
				cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
						VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP, VideoMeta.TYPE, VideoMeta.UNREAD_FLAG,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
						VideoMeta.TIMESTAMP + ">=" + time + " AND " +  VideoMeta.TIMESTAMP + "<=" + (time + timeOffset) , null, VideoMeta.TIMESTAMP + " ASC");
			} else {	//1: Accident, 2: Favorite
				cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
						VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP, VideoMeta.TYPE, VideoMeta.UNREAD_FLAG,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
						VideoMeta.TYPE + "=" + type + " AND " + VideoMeta.TIMESTAMP + ">=" + time + " AND " +  VideoMeta.TIMESTAMP + "<=" + (time + timeOffset) , null, VideoMeta.TIMESTAMP + " ASC");
			}
			
			if (cursor != null) 
				if (cursor.getCount() == 0) {
					cursor.close();
				} else {
					VideoSetInf videoSetInf = new VideoSetInf();
					videoSetInf.mStartTime = 0L;
					
					cursor.moveToFirst();
					do {
						if (videoSetInf.mStartTime == 0L) {
							videoSetInf.mStartTime = cursor.getLong(3);
						}
						VideoInf videoInf = new VideoInf();
						videoInf.name = cursor.getString(0);
						videoInf.location.lng = cursor.getString(1);
						videoInf.location.lat = cursor.getString(2);
						videoInf.timestamp = cursor.getLong(3);
						int videoType = cursor.getInt(4);
						switch(videoType) {
						case 0:
							videoInf.tag = 0;
							break;
						
						case 1:
							{	
								int unreadFlag = cursor.getInt(5);
								if (unreadFlag ==1 ) {
									videoInf.tag = VideoInf.ACCIDENT_FLAG | VideoInf.UNOPEN_FLAG;
								} else {
									videoInf.tag = VideoInf.ACCIDENT_FLAG;	
								}
							}
							break;
						
						case 2:
							videoInf.tag = VideoInf.FAVORITE_FLAG;
							break;
						
						default:
							break;
						}
						videoInf.district=cursor.getString(6);
						videoInf.address=cursor.getString(7);
						Log.d(LOG_TAG, "VideoInf: name: " + videoInf.name + ", timestamp: " + new Date(videoInf.timestamp) + ", tag: " + videoInf.tag);
						videoSetInf.addVideo(videoInf);
					} while (cursor.moveToNext());
				cursor.close();
				videoSetList.add(videoSetInf);
			}
		}
		Log.d(LOG_TAG, "Leave getVideoSets");
		return videoSetList;
	}
	
	public String getVideoPath(String videoName) {
		return TSDConst.CAR_DVR_VIDEO_PATH + "/" + videoName + ".mp4";
	}
	
	public void addVideo(VideoInf videoInf) {
		Log.d(LOG_TAG, "Enter addVideo, videoName: " + videoInf.name + ", tag: " + videoInf.tag);
		ContentValues values = new ContentValues();
        values.put(VideoMeta.NAME, videoInf.name);
        values.put(VideoMeta.LOCATION_LONG, videoInf.location.lng);
        values.put(VideoMeta.LOCATION_LAT, videoInf.location.lat);
        values.put(VideoMeta.TIMESTAMP, videoInf.timestamp);
        values.put(VideoMeta.UNREAD_FLAG, videoInf.tag & VideoInf.UNOPEN_FLAG);
        
        if ((videoInf.tag & VideoInf.ACCIDENT_FLAG) > 0) {
        	values.put(VideoMeta.TYPE, 1);
        } else if ((videoInf.tag & VideoInf.FAVORITE_FLAG) > 0) {
        	values.put(VideoMeta.TYPE, 2);
        } else {
        	values.put(VideoMeta.TYPE, 0);
        }
        values.put(VideoMeta.DISTRICT, videoInf.district);
        values.put(VideoMeta.ADDRESS, videoInf.address);
		mContext.getContentResolver().insert(VideoMeta.CONTENT_URI, values);
		Log.d(LOG_TAG, "Leave addVideo.");
	}
	
	public void setFavoriteVideo(String videoName) {
		Log.d(LOG_TAG, "Enter setFavoriteVideo, videoName: " + videoName);
		ContentValues values = new ContentValues();
        values.put(VideoMeta.TYPE, 2);
		mContext.getContentResolver().update(VideoMeta.CONTENT_URI, values, VideoMeta.NAME + "=?", new String[] {videoName});
		Log.d(LOG_TAG, "Leave setFavoriteVideo.");
	}
	
	public void cancelFavoriteVideo(String videoName) {
		Log.d(LOG_TAG, "Enter cancelFavoriteVideo, videoName: " + videoName);
		ContentValues values = new ContentValues();
        values.put(VideoMeta.TYPE, 0);
		mContext.getContentResolver().update(VideoMeta.CONTENT_URI, values, VideoMeta.NAME + "=?", new String[] {videoName});
		Log.d(LOG_TAG, "Leave cancelFavoriteVideo.");
	}

	public void setAccidentVideo(String videoName) {
		Log.d(LOG_TAG, "Enter setAccidentVideo, videoName: " + videoName);
		ContentValues values = new ContentValues();
        values.put(VideoMeta.TYPE, 1);
        values.put(VideoMeta.UNREAD_FLAG, 1);
		mContext.getContentResolver().update(VideoMeta.CONTENT_URI, values, VideoMeta.NAME + "=?", new String[] {videoName});
		Log.d(LOG_TAG, "Leave setAccidentVideo");
	}

	public void setVideoAsRead(String videoName) {
		Log.d(LOG_TAG, "Enter setVideoAsRead, videoName: " + videoName);
		ContentValues values = new ContentValues();
        values.put(VideoMeta.UNREAD_FLAG, 0);
		mContext.getContentResolver().update(VideoMeta.CONTENT_URI, values, VideoMeta.NAME + "=?", new String[] {videoName});
		Log.d(LOG_TAG, "Leave setVideoAsRead.");
	}
	
	public VIDEO_TYPE getVideoType(String videoName) {
		Cursor cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.TYPE}, VideoMeta.NAME + "=?", new String[]{videoName}, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				int type = cursor.getInt(0);

				switch (type) {
				case 0:
					return VIDEO_TYPE.NORMAL;

				case 1:
					return VIDEO_TYPE.ACCIDENT;

				case 2:
					return VIDEO_TYPE.FAVORITE;

				default:
					break;
				}
				cursor.close();
			}
		}
		return VIDEO_TYPE.NORMAL;
	}
	
	public void deleteVideo(String videoName) {
		mContext.getContentResolver().delete(VideoMeta.CONTENT_URI, VideoMeta.NAME + "=?", new String[] {videoName});
		 File file = new File( TSDConst.CAR_DVR_VIDEO_PATH + "/" + videoName + ".mp4") ;
		 if (file.exists()) {
			 file.delete();
		 }  
		 
		 file = new File( TSDConst.CAR_DVR_VIDEO_PATH + "/" + videoName + ".jpg") ;
		 if (file.exists()) {
			 file.delete();
		 }  
	}
	
	public void uploadFile(UploadInf uploadInf) {
		mUploadManager.addUploadTask(uploadInf);
	}
	
	public void uploadFile(String fileFullPathName, String timestamp, String lng, String lat,String type,String name,String srcType,String district,String address) {
		if (mDeviceId == null) {
			mDeviceId = (String) HelperUtil.readFromCommonPreference(mContext, "device_id", "string");
		}
		mUploadManager.addUploadTask(new UploadInf(Uri.parse(fileFullPathName), mDeviceId, timestamp, lng, lat,type,name,srcType,district,address));
	}
	public VideoInf getVideo(String name) {
		Log.d(LOG_TAG, "Enter getVideo, name: " + name) ;
		VideoInf videoInf=null;
		Cursor cursor = null;
    	cursor = mContext.getContentResolver().query(VideoMeta.CONTENT_URI, new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
						VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP, VideoMeta.TYPE, VideoMeta.UNREAD_FLAG,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
						VideoMeta.NAME + "=" + name , null, VideoMeta.TIMESTAMP + " ASC");
		
		if (cursor != null) {
			if(cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					videoInf = new VideoInf();
					videoInf.name = cursor.getString(0);
					videoInf.location.lng = cursor.getString(1);
					videoInf.location.lat = cursor.getString(2);
					videoInf.timestamp = cursor.getLong(3);
					switch(cursor.getInt(4)) {
					case 0:
						videoInf.tag = 0;
						break;
					
					case 1:
						if (cursor.getInt(5) ==1 ) {
							videoInf.tag = VideoInf.ACCIDENT_FLAG | VideoInf.UNOPEN_FLAG;
						} else {
							videoInf.tag = VideoInf.ACCIDENT_FLAG;	
						}
						break;
					
					case 2:
						videoInf.tag = VideoInf.FAVORITE_FLAG;
						break;
					
					default:
						break;
					}
					videoInf.district=cursor.getString(6);
					videoInf.address=cursor.getString(7);
					Log.d(LOG_TAG, "VideoInf: name: " + videoInf.name + ", timestamp: " + videoInf.timestamp + ", tag: " + videoInf.tag);
				}
			}
			cursor.close();
			cursor = null;
		}
		
		return videoInf;
	}
}
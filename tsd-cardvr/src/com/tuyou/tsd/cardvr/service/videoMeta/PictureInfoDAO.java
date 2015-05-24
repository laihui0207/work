package com.tuyou.tsd.cardvr.service.videoMeta;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tuyou.tsd.common.videoMeta.PictureInf;
import com.tuyou.tsd.common.videoMeta.VideoContent.VideoMeta;

public class PictureInfoDAO extends BaseDAO {

	private static PictureInfoDAO instance = null;

	private PictureInfoDAO(Context mContext) {
		super(mContext, DatabaseHelper.PICTURE_META_TABLE_NAME);
	}

	public static PictureInfoDAO getInstance(Context context) {
		if (instance == null)
			instance = new PictureInfoDAO(context);
		return instance;
	}

	/**
	 * 保存或新增entity. 若entity的jobId为0，则新增job，否则更新数据项
	 * 
	 * @param entity
	 * @return 返回JobId。-1表示失败
	 */
	public long save(PictureInf entity) {
		long rowId = -1;

		SQLiteDatabase db = this.openWritableDb();
		if (db == null)
			return 0;

		ContentValues values = new ContentValues();
		values.put(VideoMeta.NAME, entity.name);
        values.put(VideoMeta.LOCATION_LONG, entity.location.lng);
        values.put(VideoMeta.LOCATION_LAT, entity.location.lat);
        values.put(VideoMeta.TIMESTAMP, entity.timestamp);
        values.put(VideoMeta.DISTRICT, entity.district);
        values.put(VideoMeta.ADDRESS, entity.address);

		rowId = db.insert(getTableName(), null, values);
		// 关闭数据库
		db.close();
		if (rowId < 0)
			return -1;
		return rowId; 
	}
	
	public PictureInf getVideo(String name) {
		PictureInf pictureInf=null;
		SQLiteDatabase db = this.openWritableDb();
		if (db == null)
			return null;
		Cursor cursor = null;
    	cursor = db.query(getTableName(), new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
						VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
						VideoMeta.NAME+"=?",new String[] { name }, null,null, null);
		
		if (cursor != null) {
			if(cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					pictureInf = new PictureInf();
					pictureInf.name = cursor.getString(0);
					pictureInf.location.lng = cursor.getString(1);
					pictureInf.location.lat = cursor.getString(2);
					pictureInf.timestamp = cursor.getLong(3);					
					pictureInf.district=cursor.getString(4);
					pictureInf.address=cursor.getString(5);
				}
			}
			cursor.close();
			cursor = null;
		}
		
		return pictureInf;
	}
	
	
	public ArrayList<PictureInf> getVideoSize(String time,int size) {

		ArrayList list = new ArrayList<PictureInf>();
		PictureInf pictureInf=null;
		SQLiteDatabase db = this.openWritableDb();
		if (db == null)
			return null;
		Cursor cursor = null;
//    	cursor = db.query(getTableName(), new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
//						VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
//						null,null, null,null, null);
	    SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    Date date = null;
		try {
			date = format.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	 
		cursor = db.query(getTableName(), new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
						VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, VideoMeta.TIMESTAMP+"<?",new String[] { String.valueOf(date.getTime()) },null,null, VideoMeta.NAME +" desc",""+size);
		if (cursor != null) {
			if(cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					pictureInf = new PictureInf();
					pictureInf.name = cursor.getString(0);
					pictureInf.location.lng = cursor.getString(1);
					pictureInf.location.lat = cursor.getString(2);
					pictureInf.timestamp = cursor.getLong(3);					
					pictureInf.district=cursor.getString(4);
					pictureInf.address=cursor.getString(5);
					list.add(pictureInf);
				}
			}
			cursor.close();
			cursor = null;
		}
		
		return list;
	}
	
	public ArrayList<PictureInf> getVideoSize() {
		ArrayList list = new ArrayList<PictureInf>();
		PictureInf pictureInf=null;
		SQLiteDatabase db = this.openWritableDb();
		if (db == null)
			return null;
		Cursor cursor = null;
    	cursor = db.query(getTableName(), new String[]{VideoMeta.NAME, VideoMeta.LOCATION_LONG, 
						VideoMeta.LOCATION_LAT, VideoMeta.TIMESTAMP,VideoMeta.DISTRICT,VideoMeta.ADDRESS}, 
						null,null, null,null, null);
		
		if (cursor != null) {
			if(cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					pictureInf = new PictureInf();
					pictureInf.name = cursor.getString(0);
					pictureInf.location.lng = cursor.getString(1);
					pictureInf.location.lat = cursor.getString(2);
					pictureInf.timestamp = cursor.getLong(3);					
					pictureInf.district=cursor.getString(4);
					pictureInf.address=cursor.getString(5);
					list.add(pictureInf);
				}
			}
			cursor.close();
			cursor = null;
		}
		
		return list;
	}
	
	@Override
	public int delete(long id) {
		SQLiteDatabase db;
		db = this.openWritableDb();
		if (db == null)
			return 0;
		int rows = db.delete(DatabaseHelper.PICTURE_META_TABLE_NAME, VideoMeta.NAME+"=?", new String[]{String.valueOf(id)
		});
		db.close();
		Log.i("BaseDAO","delete rows:"+rows);
		return super.delete(id);
	}
}
	



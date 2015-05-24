package com.tuyou.tsd.cardvr.service.videoMeta;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tuyou.tsd.common.videoMeta.VideoContent.VideoMeta;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String LOG_TAG = "DatabaseHelper";
	
	public static final String DATABASE_NAME = "videoMeta.db";
	
	public static final int DATABASE_VERSION = 3;
	
	public static final String VIDEO_META_TABLE_NAME = "video_meta";
	public static final String PICTURE_META_TABLE_NAME = "picture_meta";
	
	public DatabaseHelper(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE "+ VIDEO_META_TABLE_NAME +" ("
				+ VideoMeta._ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ VideoMeta.NAME + " TEXT, "
				+ VideoMeta.LOCATION_LONG + " TEXT, "
				+ VideoMeta.LOCATION_LAT + " TEXT, "
				+ VideoMeta.TYPE + " INTEGER, "
				+ VideoMeta.UNREAD_FLAG + " INTEGER, "
				+ VideoMeta.TIMESTAMP + " INTEGER,"
				+ VideoMeta.DISTRICT + " TEXT,"
				+ VideoMeta.ADDRESS + " TEXT"				
				+");");
		db.execSQL("CREATE TABLE "+ PICTURE_META_TABLE_NAME +" ("
				+ VideoMeta._ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ VideoMeta.NAME + " TEXT, "
				+ VideoMeta.LOCATION_LONG + " TEXT, "
				+ VideoMeta.LOCATION_LAT + " TEXT, "
				+ VideoMeta.TIMESTAMP + " INTEGER,"
				+ VideoMeta.DISTRICT + " TEXT,"
				+ VideoMeta.ADDRESS + " TEXT"				
				+");");
		
		db.execSQL("CREATE INDEX IDX_TIMESTAMP ON " + VIDEO_META_TABLE_NAME + " (" + VideoMeta.TIMESTAMP + " ASC)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
            if (oldVersion <= 1) {
            	db.execSQL("CREATE INDEX IDX_TIMESTAMP ON " + VIDEO_META_TABLE_NAME + " (" + VideoMeta.TIMESTAMP + " ASC)");
            } 
        } catch (SQLException e) {
        	Log.e(LOG_TAG, "upgrade database failed " + e);
        }
	}
}

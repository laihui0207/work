package com.tuyou.tsd.news.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBOpenHelper extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "audioplayer.db";	
	public static String CATEGORY_TABLE_NAME = "category"; //运营歌单列表
	public static String SUBSCRIPTION_CATEGORY = "subscriptioncategory"; //运营歌单列表
	public static String DETAIL_TABLE_NAME = "categorydetail";//每个歌单当前当前播放歌曲
	public static String ITEM_TABLE_NAME = "audioitem"; //推送歌单
	public static String FAVOURITE_TABLE_NAME = "favouriteitem";//我的收藏列表
	public static String FAVOURITE_RECORD_TABLE_NAME = "favouriterecord";//收藏临时记录
	
	public DBOpenHelper(Context context) {
		super(context, DB_NAME, null, 3);//Params.getInstance(context).getAppVersion()
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i("DBOpenHelper","onCreate");
		db.execSQL("CREATE TABLE "+CATEGORY_TABLE_NAME+" (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"detail BLOB" + 
				");");
		db.execSQL("CREATE TABLE "+SUBSCRIPTION_CATEGORY+" (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"detail BLOB" + 
				");");
		db.execSQL("CREATE TABLE "+DETAIL_TABLE_NAME+" (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"detail BLOB" + 
				");");
		db.execSQL("CREATE TABLE "+ITEM_TABLE_NAME+" (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"detail BLOB" + 
				");");
		db.execSQL("CREATE TABLE "+FAVOURITE_TABLE_NAME+" (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"detail BLOB" + 
				");");
		db.execSQL("CREATE TABLE "+FAVOURITE_RECORD_TABLE_NAME+" (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"detail BLOB" + 
				");");
		
	}
	
	@Override  
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i("DBOpenHelper","drop db");
		db.execSQL("DROP TABLE IF EXISTS "+CATEGORY_TABLE_NAME+";");
		db.execSQL("DROP TABLE IF EXISTS "+DETAIL_TABLE_NAME+";");
		db.execSQL("DROP TABLE IF EXISTS "+ITEM_TABLE_NAME+";");
		db.execSQL("DROP TABLE IF EXISTS "+FAVOURITE_TABLE_NAME+";");
		db.execSQL("DROP TABLE IF EXISTS "+FAVOURITE_RECORD_TABLE_NAME+";");
		db.execSQL("DROP TABLE IF EXISTS "+SUBSCRIPTION_CATEGORY+";");
		onCreate(db);
	}
}

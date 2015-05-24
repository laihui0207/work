package com.tuyou.tsd.podcast.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BaseDAO {
	private Context mContext = null;
	private String  mTableName = null;

	public BaseDAO(Context mContext,String mTableName) {
		super();
		this.mContext = mContext;
		this.mTableName = mTableName;
	}
	
	protected String getTableName()
	{
		return this.mTableName;
	}
	
	protected SQLiteDatabase openWritableDb()
	{
		Log.i("BaseDAO","BaseDAO::openWritableDb()");
		
		DBOpenHelper dbHelper = new DBOpenHelper(mContext);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		if (db == null)
		{
			Log.i("BaseDAO","openWritableDb failure");
		}
		
		return db;
	}
	
	protected SQLiteDatabase openReadableDb()
	{
		Log.i("BaseDAO","BaseDAO::openReadableDb()");
		DBOpenHelper dbHelper = new DBOpenHelper(mContext);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		if (db == null)
		{
			Log.i("BaseDAO","openReadableDb failure");
		}
		return db;
	}
	
	/**
	 * 删除id对应的表项
	 * @param id
	 * @return 返回被删除的记录条数
	 */
	public int delete(long id)
	{
		SQLiteDatabase db;
		db = this.openWritableDb();
		
		if (db == null)
			return 0;
		
		int rows = db.delete(mTableName, "_id=?", new String[]{
				String.valueOf(id)
		});
		
		db.close();
		
		Log.i("BaseDAO","delete rows:"+rows);
		
		return rows;
	}
	
	/**
	 * 删除表中所有记录
	 * @return 返回被删除的记录条数
	 */
	public int deleteAll()
	{
		SQLiteDatabase db;
		db = this.openWritableDb();
		
		if (db == null)
			return 0;
		
		int affectRows = db.delete(mTableName, null, null);
		
		db.close();
		
		Log.i("BaseDAO","deleteAll rows:"+affectRows);
		
		return affectRows;
	}
}

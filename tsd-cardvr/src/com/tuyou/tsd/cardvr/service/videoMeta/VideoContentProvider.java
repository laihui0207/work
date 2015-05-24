package com.tuyou.tsd.cardvr.service.videoMeta;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.tuyou.tsd.common.videoMeta.VideoContent;
import com.tuyou.tsd.common.videoMeta.VideoContent.VideoMeta;

public class VideoContentProvider extends ContentProvider {
	public static final String TAG = "videoContentProvider";	
	
	DatabaseHelper mOpenHelper;
	UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static final int VIDEO_META = 1;
	
	public VideoContentProvider() {
		mUriMatcher.addURI(VideoContent.AUTHORITY, VideoMeta.PATH, VIDEO_META);
	}
	
	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		mOpenHelper.getReadableDatabase();
        mOpenHelper.getWritableDatabase();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		String tableName;
        
        // Get the database to run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        
		switch (mUriMatcher.match(uri)) {
		case VIDEO_META:
			tableName = DatabaseHelper.VIDEO_META_TABLE_NAME;
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		Cursor c = null;
        try {
            c = db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
            // Tell the cursor what uri to watch, so it knows when its source data changes
            c.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (Exception e) {
            Log.e(TAG, "query: " + tableName + selection + e);
            if (c != null) {
                c.close();
                c = null;
            }
        }
        return c;
	}

	@Override
	public String getType(Uri uri) {
		 switch (mUriMatcher.match(uri)) {
	        case VIDEO_META:
	            return VideoMeta.CONTENT_TYPE;
	
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
		 }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String tableName;
		
		switch (mUriMatcher.match(uri)) {
		    case VIDEO_META:
		    	tableName = DatabaseHelper.VIDEO_META_TABLE_NAME;
		        break;
		        
		    default:
		        throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		Uri result = null;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        long rowId = 0;
        db.beginTransaction();
        try {
            rowId = db.insert(tableName, null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
       
        if (rowId > 0) {
            result = ContentUris.withAppendedId(uri, rowId);
        }
        
        return result;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String tableName;
		int count=0;  
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        switch(mUriMatcher.match(uri)){  
	        case VIDEO_META:
	        	tableName = DatabaseHelper.VIDEO_META_TABLE_NAME;
	        	break;
	            
	        default:  
	            throw new IllegalArgumentException("Unknow Uri:" + uri.toString());  
        }
        
        count = db.delete(tableName, selection, selectionArgs);
        return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		String tableName;
		int count=0;  
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        switch(mUriMatcher.match(uri)){  
	        case VIDEO_META:
	        	tableName = DatabaseHelper.VIDEO_META_TABLE_NAME;
	        	break;
	            
	        default:  
	            throw new IllegalArgumentException("Unknow Uri:" + uri.toString());  
        }
        
        count = db.update(tableName, values, selection, selectionArgs);
        return count;
	}
}

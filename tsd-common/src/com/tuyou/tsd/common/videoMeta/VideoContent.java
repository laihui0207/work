package com.tuyou.tsd.common.videoMeta;

import android.net.Uri;
import android.provider.BaseColumns;

public class VideoContent {
	private VideoContent() { }
	
	public static final String AUTHORITY  = "com.tuyou.cardvr.videoContentProvider";
	
	public interface VideoMetaColumns extends BaseColumns {
		public static final String NAME = "name";
		public static final String LOCATION_LONG = "loc_long";
		public static final String LOCATION_LAT = "loc_lat";
		public static final String TIMESTAMP = "timestamp";
		public static final String TYPE = "type";  //0: normal, 1: accident, 2: favorite  
		public static final String UNREAD_FLAG = "unread";
		public static final String DISTRICT = "district";
		public static final String ADDRESS = "address";
	}

	public static final class VideoMeta implements VideoMetaColumns {
		private VideoMeta() {}
		public static final String PATH = "videoMeta";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"+PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tsd.videometa";
	}
}






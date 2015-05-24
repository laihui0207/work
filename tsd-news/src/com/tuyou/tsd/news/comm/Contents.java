package com.tuyou.tsd.news.comm;


import android.os.Environment;

public class Contents {
	
	public final static String ROOT_PATH = Environment.getExternalStorageDirectory()+"/com.tuyou";
	
	public final static String IMAGE_PATH = ROOT_PATH+"/news_img";
	
	public final static String MP3_PATH = ROOT_PATH+"/news_mp3";
	
	public final static String ACTION_NEWS_BUTTON = "com.tsd.tuyou.news_notifications";
	
	public final static int BUTTON_PREV_ID = 1;
	public final static int BUTTON_PLAY_ID = 2;
	public final static int BUTTON_NEXT_ID = 3;
	public final static int BUTTON_CLEAN_ID = 4;
	public final static int BUTTON_LOVE_ID = 5;
//	public final static int BUTTON_PARENT_ID = 6;
	public final static String INTENT_NEWS_BUTTONID_TAG = "news_ButtonId";
	
	public final static String MUSICPLAY_STATE_NEWS_PAUSE= "com.tsd.news_playstate_pause"; 
	public final static String MUSICPLAY_STATE_NEWS_PLAY= "com.tsd.news_playstate_play"; 
	
	public final static String ADD_NEWS_SUB = "com.tuyou.sub_add.news";
	
	public final static String REMOVE_NEWS_SUB = "com.tuyou.sub_remove.news";
	
	public final static String CANNOT_NEWS_ADD = "com.tuyou.cannot_add.news";
	
	public final static String PLAY_MUSIC_NEWS_NEXT = "com.tuyou.tsd.news.playmusicnext.news";
	
	public final static String PLAY_NEWS_PODCAST = "com.tuyou.tsd.news.category.news";
	
	public final static String ADD_NEWS_SUB_CANNOT = "com.tuyou.new_sub_add_cannot.news";
	
	public final static int ADD_BIG_NEWS_NUM = 6;
	
	public final static String KILL_ALL_APP1 = "com.tsd.audio" ;
	public final static String KILL_ALL_APP2 = "com.tsd.podcast" ;
	
	public final static String KILL_ALL_APP3 = "com.tsd.news" ;
	
	public final static String TSD_AUDIO_PLAY_MUSIC_RESULT = "TSD_AUDIO_PLAY_MUSIC_RESULT_NEWS";
	
	public final static String DATA_REFRESH_PUSH = "com.tsd.new_push_sub";
}

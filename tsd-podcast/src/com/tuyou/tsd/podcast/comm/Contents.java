package com.tuyou.tsd.podcast.comm;


import android.os.Environment;

public class Contents {
	
	public final static String ROOT_PATH = Environment.getExternalStorageDirectory()+"/com.tuyou";
	
	public final static String IMAGE_PATH = ROOT_PATH+"/podcast_img";
	
	public final static String MP3_PATH = ROOT_PATH+"/podcast_mp3";
	
	public final static String ACTION_BUTTON = "com.tsd.tuyou.podcast_notifications";
	
	public final static int BUTTON_PREV_ID = 1;
	public final static int BUTTON_PLAY_ID = 2;
	public final static int BUTTON_NEXT_ID = 3;
	public final static int BUTTON_CLEAN_ID = 4;
	public final static int BUTTON_LOVE_ID = 5;
//	public final static int BUTTON_PARENT_ID = 6;
	public final static String INTENT_PODCAST_BUTTONID_TAG = "podcast_ButtonId";
	
	public final static String MUSICPLAY_STATE_PAUSE= "com.tsd.podcast_playstate_pause"; 
	public final static String MUSICPLAY_STATE_PLAY= "com.tsd.podcast_playstate_play"; 
	
	public final static String ADD_SUB = "com.tuyou.sub_add";
	
	public final static String REMOVE_SUB = "com.tuyou.sub_remove";
	
	public final static String CANNOT_ADD = "com.tuyou.cannot_add";
	
	public final static String PLAY_MUSIC_NEXT = "com.tuyou.tsd.podcast.playmusicnext";
	
	public final static String PLAY_PODCAST = "com.tuyou.tsd.podcast.category";
	
	public final static String ADD_SUB_CANNOT = "com.tuyou.sub_add_cannot";
	
	public final static int ADD_BIG_NUM = 6;
	
	public final static String KILL_ALL_APP1 = "com.tsd.audio" ;
	public final static String KILL_ALL_APP2 = "com.tsd.news" ;
	public final static String KILL_ALL_APP3 = "com.tsd.prodcast" ;
	
	public final static String TSD_AUDIO_PLAY_MUSIC_RESULT = "TSD_AUDIO_PLAY_MUSIC_RESULT_PRODCAST";
	
	public final static String DATA_REFRESH_PUSH = "com.tsd.podcast_push_sub";
}

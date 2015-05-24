package com.tuyou.tsd.audio.comm;


import android.os.Environment;

public class Contents {
	
	public final static String ROOT_PATH = Environment.getExternalStorageDirectory()+"/com.tuyou";
	
	public final static String IMAGE_PATH = ROOT_PATH+"/audio_img";
	
	public final static String MP3_PATH = ROOT_PATH+"/audio_mp3";
	
	public final static String ACTION_BUTTON = "com.tsd.tuyou.notifications";
	
	public final static int BUTTON_PREV_ID = 1;
	public final static int BUTTON_PLAY_ID = 2;
	public final static int BUTTON_NEXT_ID = 3;
	public final static int BUTTON_CLEAN_ID = 4;
	public final static int BUTTON_LOVE_ID = 5;
//	public final static int BUTTON_PARENT_ID = 6;
	public final static String INTENT_BUTTONID_TAG = "ButtonId";
	
	public final static String MUSICPLAY_STATE_PAUSE= "com.tsd.music_playstate_pause"; 
	public final static String MUSICPLAY_STATE_PLAY= "com.tsd.music_playstate_play"; 
	public final static String MUSICPLAY_CATEGORY = "com.tuyou.tsd.music.category";
	
	public final static String KILL_ALL_APP1 = "com.tsd.news" ;
	public final static String KILL_ALL_APP2 = "com.tsd.podcast" ;
	public final static String KILL_ALL_APP3 = "com.tsd.audio" ;
	
	public final static String PUSH_AUDIO_CATEGORY = "push_audio_category";
	
	public final static String TSD_AUDIO_PLAY_MUSIC = "TSD.AUDIO.PLAY_MUSIC";
	
	public final static String TSD_AUDIO_PLAY_MUSIC_RESULT = "TSD_AUDIO_PLAY_MUSIC_RESULT_AUDIO";
}

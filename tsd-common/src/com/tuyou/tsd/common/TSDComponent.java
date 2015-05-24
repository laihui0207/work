package com.tuyou.tsd.common;

public interface TSDComponent {

	// Launcher
	static final String LAUNCHER_PACKAGE = "com.tuyou.tsd";
	static final String HOME_ACTIVITY   = "com.tuyou.tsd.launcher.HomeActivity";
	static final String SLEEPING_ACTIVITY = "com.tuyou.tsd.launcher.SleepingActivity";
	static final String ALL_APPS_ACTIVITY = "com.tuyou.tsd.launcher.AppsActivity";

	// Core service
	static final String CORE_SERVICE_PACKAGE = "com.tuyou.tsd";
	static final String CORE_SERVICE = "com.tuyou.tsd.core.CoreService";

	// Voice assistant
	static final String VOICE_ASSISTANT_PACKAGE = "com.tuyou.tsd.voice";
	static final String WAKEUP_ACTIVITY = "com.tuyou.tsd.voice.WakeUpActivity";
	static final String INTERACTION_ACTIVITY = "com.tuyou.tsd.voice.InteractingActivity";
	static final String VOICE_ASSISTANT_SERVICE = "com.tuyou.tsd.voice.service.VoiceAssistant";

	// 系统设置app
	static final String SETTINGS_PACKAGE = "com.tuyou.tsd.settings";
	static final String SETTINGS_MAIN_ACTIVITY = "com.tuyou.tsd.settings.MainActivity";
	static final String SETTINGS_SERVICE = "com.tuyou.tsd.settings.base.SettingsService";

	// 初始化设置app
	static final String INIT_SETTINGS_PACKAGE = "com.tuyou.tsd.settings";
	static final String INIT_SETTINGS_MAIN_ACTIVITY = "com.tuyou.tsd.settings.init.WeclomeActivity";

	// 设置同步service
	static final String SYNC_SETTINGS_SERVICE_PACKAGE = "com.tuyou.tsd.settings.sync";
	static final String SYNC_SETTINGS_SERVICE_CLASS = "com.tuyou.tsd.settings.sync.SyncService";

	// 行车记录视频浏览
	static final String CAR_DVR_PACKAGE = "com.tuyou.tsd.cardvr";
	static final String CAR_DVR_MAIN_ACTIVITY = "com.tuyou.tsd.cardvr.MainActivity";

	// 行车记录service
	static final String CAR_DVR_SERVICE = "com.tuyou.tsd.cardvr.service.VideoRec";

	// 图声微博service
	static final String WEIBO_SERVICE = "com.tuyou.tsd.weibo.WBService";

	// 地图导航app
	static final String NAVIGATOR_PACKAGE = "com.tuyou.tsd.navigation";
	static final String NAVIGATOR_MAIN_ACTIVITY = "com.tuyou.tsd.navigation.MainActivity";
	static final String NAVIGATOR_SERVICE = "com.tuyou.tsd.navigation.SearchService";

	// 更新service
	static final String UPDATE_PACKAGE = "com.tuyou.tsd.updatesoft";
	static final String UPDATE_SERVICE = "com.tuyou.tsd.updatesoft.UpdateSoftService";

	// Audio player
	static final String AUDIO_PACKAGE = "com.tuyou.tsd.audio";
	static final String AUDIO_MAIN_ACTIVITY = "com.tuyou.tsd.audio.MusicActivity";
	static final String AUDIO_SERVICE = "com.tuyou.tsd.audio.service.AudioPlayerService";
	
	// podcast player
	static final String PODCAST_PACKAGE = "com.tuyou.tsd.podcast";
	static final String PODCAST_MAIN_ACTIVITY = "com.tuyou.tsd.podcast.MusicActivity";
	static final String PODCAST_SERVICE = "com.tuyou.tsd.podcast.service.AudioPlayerService";

	// news player
	static final String NEWS_PACKAGE = "com.tuyou.tsd.news";
	static final String NEWS_MAIN_ACTIVITY = "com.tuyou.tsd.news.MusicActivity";
	static final String NEWS_SERVICE = "com.tuyou.tsd.news.service.AudioPlayerService";		
	
	// Httpd Service
	static final String HTTPD_SERVICE = "com.tuyou.tsd.core.HttpdService";

}

package com.tuyou.tsd.common;

import android.os.Environment;

/**
 * 公共常量定义
 * @author ruhai
 * 2014-8
 */
public interface CommonConsts {
	/**
	 * 请用TSDConst
	 */
	@Deprecated
	static final boolean buildForDevice = false; // true -- for Device; false -- for Cell phone.
	/**
	 * 请用TSDConst
	 */
	@Deprecated
	static final boolean useGPRSNetwork = false; // true -- GPRS; false -- WIFI

	// 资源文件路径
	/**
	 * 请用TSDConst
	 */
	@Deprecated
	static final String TUYOU_ROOT_PATH = Environment.getExternalStorageDirectory() + "/com.tuyou";

	// 语音助手资源文件
	/**
	 * 请用TSDConst
	 */
	@Deprecated
	static final String VOICE_ASSISTANT_PATH = TUYOU_ROOT_PATH + "/voice";

	// 行车记录录像保存位置
	/**
	 * 请用TSDConst
	 */
	@Deprecated
	static final String CAR_DVR_PATH = TUYOU_ROOT_PATH + "/cardvr";
	/**
	 * 请用TSDConst
	 */
	@Deprecated
	static final String CAR_DVR_VIDEO_PATH = CAR_DVR_PATH + "/videos";
	/**
	 * 请用TSDConst
	 */
	@Deprecated
    static final String CAR_DVR_ACCIDENT_VIDEO_PATH = CAR_DVR_PATH + "/accidents";
	/**
	 * 请用TSDConst
	 */
	@Deprecated
    static final String CAR_DVR_ALERT_VIDEO_PATH = CAR_DVR_PATH + "/alerts";
	/**
	 * 请用TSDConst
	 */
	@Deprecated
    static final String CAR_DVR_PICTURES_PATH = CAR_DVR_PATH + "/pictures";

    // FTP server
	/**
	 * 请用TSDConst
	 */
	@Deprecated
	static final String FTP_CONFIG_PATH = TUYOU_ROOT_PATH + "/ftpConfig";    

	//
	// Common shared preferences filename
	//
	/**
	 * 系统设置, 请用TSDShare
	 */
	@Deprecated
	static final String SYSTEM_SETTING_PREFERENCES = "tsd_system_settings";
	/**
	 * 行车记录设置, 请用TSDShare
	 */
	@Deprecated
	static final String CARDVR_SETTING_PREFERENCES = "tsd_cardvr_settings";
	/**
	 * Common shared preference, 请用TSDShare
	 */
	@Deprecated
	static final String COMMON_SHARED_PREFERENCES = "tsd_common"; 

	//
	// 组件名称
	//

	// Launcher
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String LAUNCHER_PACKAGE = "com.tuyou.tsd";
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String HOME_ACTIVITY   = "com.tuyou.tsd.launcher.HomeActivity";
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String SLEEPING_ACTIVITY = "com.tuyou.tsd.launcher.SleepingActivity";
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String ALL_APPS_ACTIVITY = "com.tuyou.tsd.launcher.AppsActivity";

	// Core service
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String CORE_SERVICE_PACKAGE = "com.tuyou.tsd";
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String CORE_SERVICE = "com.tuyou.tsd.core.CoreService";

	// Voice assistant
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String VOICE_ASSISTANT_PACKAGE = "com.tuyou.tsd.voice";
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String WAKEUP_ACTIVITY = "com.tuyou.tsd.voice.WakeUpActivity";
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String INTERACTION_ACTIVITY = "com.tuyou.tsd.voice.InteractingActivity";
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String VOICE_ASSISTANT_SERVICE = "com.tuyou.tsd.voice.service.VoiceAssistant";

	// 系统设置app
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String SETTINGS_PACKAGE = "com.tuyou.tsd.settings";
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String SETTINGS_MAIN_ACTIVITY = "com.tuyou.tsd.settings.MainActivity";

	// 初始化设置app
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String INIT_SETTINGS_PACKAGE = "com.tuyou.tsd.settings.init";
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String INIT_SETTINGS_MAIN_ACTIVITY = "com.tuyou.tsd.settings.init.MainActivity";

	// 设置同步service
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String SYNC_SETTINGS_SERVICE_PACKAGE = "com.tuyou.tsd.settings.sync";
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String SYNC_SETTINGS_SERVICE_CLASS = "com.tuyou.tsd.settings.sync.SyncService";

	// 行车记录视频浏览
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String CAR_DVR_PACKAGE = "com.tuyou.tsd.cardvr";
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String CAR_DVR_MAIN_ACTIVITY = "com.tuyou.tsd.cardvr.MainActivity";

	// 行车记录service
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String CAR_DVR_SERVICE = "com.tuyou.tsd.cardvr.service.VideoRec";

	// 图声微博service
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String WEIBO_SERVICE = "com.tuyou.tsd.weibo.WBService";

	// 地图导航app
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String NAVIGATOR_PACKAGE = "com.tuyou.tsd.navigation";
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String NAVIGATOR_MAIN_ACTIVITY = "com.tuyou.tsd.navigation.MainActivity";

	// 更新service
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String UPGRADE_SERVICE = "com.tuyou.tsd.updatesoft.UpdateSoftService";

	// Audio player
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String AUDIO_PACKAGE = "com.tuyou.tsd.audio";
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String AUDIO_MAIN_ACTIVITY = "com.tuyou.tsd.audio.MusicActivity";
	/**
	 * 请用TSDComponent
	 */
	@Deprecated
	static final String AUDIO_SERVICE = "com.tuyou.tsd.audio.service.AudioPlayerService";
	
	/**
	 * 请用TSDConst
	 */
	@Deprecated
	static final String CAR_DVR_START_CAM_PREVIEW_RES = "res";
	
	/**
	 * 请用TSDConst
	 */
	@Deprecated
	static final String UPLOAD_LOC_LAT = "latitude";
	/**
	 * 请用TSDConst
	 */
	@Deprecated
	static final String UPLOAD_LOC_LNG = "longitude";
	/**
	 * 请用TSDConst
	 */
	@Deprecated
	static final String UPLOAD_TIME_STAMP = "timestamp";

	/**
	 * 请用TSDConst
	 */
	@Deprecated
	static final String ACC_STATE = "acc_state";	
}

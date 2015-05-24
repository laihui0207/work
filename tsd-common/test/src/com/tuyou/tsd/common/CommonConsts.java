package com.tuyou.tsd.common;

import android.os.Environment;

/**
 * 公共常量定义
 * @author ruhai
 * 2014-8
 */
public interface CommonConsts {

	// 资源文件路径
	static final String TUYOU_ROOT_PATH = Environment.getExternalStorageDirectory() + "/com.tuyou";

	// 语音助手资源文件
	static final String VOICE_ASSISTANT_PATH = TUYOU_ROOT_PATH + "/voice";

	// 行车记录录像保存位置
	static final String CAR_DVR_PATH = TUYOU_ROOT_PATH + "/cardvr";
    static final String CAR_DVR_ACCIDENT_VIDEO_PATH = CAR_DVR_PATH + "/accidents";
    static final String CAR_DVR_ALERT_VIDEO_PATH = CAR_DVR_PATH + "/alerts";
    static final String CAR_DVR_PICTURES_PATH = CAR_DVR_PATH + "/pictures";

    // FTP server
	static final String FTP_CONFIG_PATH = TUYOU_ROOT_PATH + "/ftpConfig";    

	//
	// Common shared preferences filename
	//
	/**
	 * 系统设置
	 */
	static final String SYSTEM_SETTING_PREFERENCES = "tsd_system_settings";
	/**
	 * 行车记录设置
	 */
	static final String CARDVR_SETTING_PREFERENCES = "tsd_cardvr_settings";

	//
	// 组件名称
	//

	// Core service
	static final String CORE_SERVICE_PACKAGE = "com.tuyou.tsd.core";
	static final String CORE_SERVICE = "com.tuyou.tsd.core.CoreService";

	// Voice assistant
	static final String VOICE_ASSISTANT_SERVICE = "com.tuyou.tsd.voice.VoiceAssistant";

	// 系统设置app
	static final String SETTINGS_PACKAGE = "com.tuyou.tsd.settings";
	static final String SETTINGS_MAIN_CLASS = "com.tuyou.tsd.settings.MainActivity";

	// 初始化设置app
	static final String INIT_SETTINGS_PACKAGE = "com.tuyou.tsd.settings.init";
	static final String INIT_SETTINGS_MAIN_CLASS = "com.tuyou.tsd.settings.init.MainActivity";

	// 设置同步service
	static final String SYNC_SETTINGS_SERVICE_PACKAGE = "com.tuyou.tsd.settings.sync";
	static final String SYNC_SETTINGS_SERVICE_CLASS = "com.tuyou.tsd.settings.sync.SyncService";

	// 行车记录视频浏览
	static final String CAR_DVR_PACKAGE = "com.tuyou.tsd.cardvr";
	static final String CAR_DVR_MAIN_CLASS = "com.tuyou.tsd.cardvr.MainActivity";

	// 行车记录service
	static final String CAR_DVR_SERVICE = "com.tuyou.tsd.cardvr.service.VideoRec";

	// 图声微博service
	static final String WEIBO_SERVICE = "com.tuyou.tsd.weibo.WBService";

	// 地图导航app
	static final String NAVIGATOR_PACKAGE = "com.tuyou.tsd.map";
	static final String NAVIGATOR_MAIN_CLASS = "com.tuyou.tsd.map.MainActivity";
}

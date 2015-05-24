package com.tuyou.tsd.common;

public interface TSDEvent {

	// 系统状态通知
	interface System {
		// 系统初始化，资源加载完成，在此之前不允许用户操作
		static final String LOADING_COMPLETE = "tsd.event.system.loading_complete";

		// 主动查询系统状态和工作模式
		static final String QUERY_SYSTEM_STATE = "tsd.event.system.query_state";
		static final String QUERY_SYSTEM_MODE  = "tsd.event.system.query_mode";

		// 系统状态和工作模式改变时的通知
		static final String SYSTEM_STATE_CHANGED = "tsd.event.system.state_changed";
		static final String SYSTEM_MODE_CHANGED = "tsd.event.system.mode_changed";

		// ACC
		static final String ACC_ON = "tsd.event.system.acc_on";
		static final String ACC_OFF = "tsd.event.system.acc_off";

		static final String HARDKEY1_PRESSED = "tsd.event.system.hardkey1_pressed";
		static final String HARDKEY2_PRESSED = "tsd.event.system.hardkey2_pressed";
		static final String HARDKEY3_PRESSED = "tsd.event.system.hardkey3_pressed";
		static final String HARDKEY4_PRESSED = "tsd.event.system.hardkey4_pressed";

		/**
		 * 查询天气
		 */
		static final String QUERY_WEATHER = "tsd.event.system.query_weather";
		/**
		 * 天气数据返回
		 */
		static final String WEATHER_UPDATED = "tsd.event.system.weather_updated";

		/**
		 * 定位信息更新
		 */
		static final String LOCATION_UPDATED = "tsd.event.system.location_updated";

		/**
		 * 同步设置项（上传）
		 */
		static final String PUSH_CONFIG_INFO = "tsd.event.system.push_config_info";
		/**
		 * 同步设置项（下载）
		 */
		static final String FETCH_CONFIG_INFO = "tsd.event.system.fetch_config_info";
		/**
		 * 更新休眠时间
		 */
		static final String IDLE_INTERVAL_TIME_UPDATED = "tsd.event.system.idle_interval_time_updated";

		/**
		 * 不允许设备进入休眠（小宝睡觉页面）
		 */
		static final String DISABLE_IDLE_CHECK = "tsd.event.system.disable_idle_check";
		/**
		 * 允许设备进入休眠（小宝睡觉页面）
		 */
		static final String ENABLE_IDLE_CHECK = "tsd.event.system.enable_idle_check";
	}

	interface Interaction {
		/**
		 * 服务启动, 语音服务启动时主动发送
		 */
		static final String SERVICE_STARTED = "tsd.event.interaction.service_started";
		/**
		 * 服务停止, 语音服务停止时主动发送
		 */
		static final String SERVICE_STOPPED = "tsd.event.interaction.service_stopped";
		/**
		 * 应用启动, 交互界面启动时主动发送
		 */
		static final String APP_STARTED = "tsd.event.interaction.app_started";
		/**
		 * 应用退出, 交互界面退出时主动发送
		 */
		static final String APP_STOPPED = "tsd.event.interaction.app_stopped";
		/**
		 * 运行交互脚本（控制命令）
		 */
		static final String RUN_INTERACTION = "tsd.event.interaction.RUN";
		/**
		 * 交互开始, 由语音助手服务发送
		 */
		static final String INTERACTION_START = "tsd.event.interaction.start";
		/**
		 * 交互结束（成功）, 由语音助手服务发送
		 */
		static final String INTERACTION_FINISH = "tsd.event.interaction.finish";
		/**
		 * 交互结束（失败）, 由语音助手服务发送
		 */
		static final String INTERACTION_ERROR = "tsd.event.interaction.error";
		/**
		 * 交互结束，原因是用户进行了触屏选择
		 */
		static final String FINISH_INTERACTION_BY_TP = "tsd.event.interaction.FINISH_BY_TP";
		/**
		 * 交互取消，原因是用户进行了触屏选择
		 */
		static final String CANCEL_INTERACTION_BY_TP = "tsd.event.interaction.CANCEL_BY_TP";
		/**
		 * 关闭交互页面（控制命令，由CoreService发送。其它组件不要直接发送此消息）
		 */
		static final String FINISH_ACTIVITY = "tsd.event.interaction.FINISH_ACTIVITY";
	}

	interface Audio {
		/**
		 * 服务启动, 音乐服务启动时主动发送
		 */
		static final String SERVICE_STARTED = "tsd.event.audio.service_started";
		/**
		 * 服务停止, 音乐服务停止时主动发送
		 */
		static final String SERVICE_STOPPED = "tsd.event.audio.service_stopped";
		/**
		 * 应用启动, 音乐应用启动时主动发送
		 */
		static final String APP_STARTED = "tsd.event.audio.app_started";
		/**
		 * 应用退出, 音乐应用退出时主动发送
		 */
		static final String APP_STOPPED = "tsd.event.audio.app_stopped";
		/**
		 * 开始播放, 由中控模块发送, 音乐模块接收该消息
		 */
		static final String PLAY = "tsd.event.audio.play";
		/**
		 * 停止播放, 由中控模块发送, 音乐模块接收该消息
		 */
		static final String STOP = "tsd.event.audio.stop";
		/**
		 * 暂停播放, 由中控模块发送, 音乐模块接收该消息
		 */
		static final String PAUSE = "tsd.event.audio.pause";
		/**
		 * 恢复播放, 由中控模块发送, 音乐模块接收该消息
		 */
		static final String RESUME = "tsd.event.audio.resume";
		/**
		 * 音乐音量增大，音量由intent参数("volume")传递，取值范围0〜100
		 */
		static final String VOLUME_UP = "tsd.event.audio.volume_up";
		/**
		 * 音乐音量减小，音量由intent参数("volume")传递，取值范围0〜100
		 */
		static final String VOLUME_DOWN = "tsd.event.audio.volume_down";
		/**
		 * 播放器状态汇报
		 */
		static final String STATE = "tsd.event.audio.state";
	}
	
	interface Podcast {
		/**
		 * 服务启动, 音乐服务启动时主动发送
		 */
		static final String SERVICE_STARTED = "tsd.event.audio.service_started";
		/**
		 * 服务停止, 音乐服务停止时主动发送
		 */
		static final String SERVICE_STOPPED = "tsd.event.audio.service_stopped";
		/**
		 * 应用启动, 音乐应用启动时主动发送
		 */
		static final String APP_STARTED = "tsd.event.audio.app_started";
		/**
		 * 应用退出, 音乐应用退出时主动发送
		 */
		static final String APP_STOPPED = "tsd.event.audio.app_stopped";
		/**
		 * 开始播放, 由中控模块发送, 音乐模块接收该消息
		 */
		static final String PLAY = "tsd.event.audio.play";
		/**
		 * 停止播放, 由中控模块发送, 音乐模块接收该消息
		 */
		static final String STOP = "tsd.event.audio.stop";
		/**
		 * 暂停播放, 由中控模块发送, 音乐模块接收该消息
		 */
		static final String PAUSE = "tsd.event.audio.pause";
		/**
		 * 恢复播放, 由中控模块发送, 音乐模块接收该消息
		 */
		static final String RESUME = "tsd.event.audio.resume";
		/**
		 * 音乐音量增大，音量由intent参数("volume")传递，取值范围0〜100
		 */
		static final String VOLUME_UP = "tsd.event.audio.volume_up";
		/**
		 * 音乐音量减小，音量由intent参数("volume")传递，取值范围0〜100
		 */
		static final String VOLUME_DOWN = "tsd.event.audio.volume_down";
		/**
		 * 播放器状态汇报
		 */
		static final String STATE = "tsd.event.audio.state";
	}
	
	interface News {
		/**
		 * 服务启动, 音乐服务启动时主动发送
		 */
		static final String SERVICE_STARTED = "tsd.event.audio.service_started";
		/**
		 * 服务停止, 音乐服务停止时主动发送
		 */
		static final String SERVICE_STOPPED = "tsd.event.audio.service_stopped";
		/**
		 * 应用启动, 音乐应用启动时主动发送
		 */
		static final String APP_STARTED = "tsd.event.audio.app_started";
		/**
		 * 应用退出, 音乐应用退出时主动发送
		 */
		static final String APP_STOPPED = "tsd.event.audio.app_stopped";
		/**
		 * 开始播放, 由中控模块发送, 音乐模块接收该消息
		 */
		static final String PLAY = "tsd.event.audio.play";
		/**
		 * 停止播放, 由中控模块发送, 音乐模块接收该消息
		 */
		static final String STOP = "tsd.event.audio.stop";
		/**
		 * 暂停播放, 由中控模块发送, 音乐模块接收该消息
		 */
		static final String PAUSE = "tsd.event.audio.pause";
		/**
		 * 恢复播放, 由中控模块发送, 音乐模块接收该消息
		 */
		static final String RESUME = "tsd.event.audio.resume";
		/**
		 * 音乐音量增大，音量由intent参数("volume")传递，取值范围0〜100
		 */
		static final String VOLUME_UP = "tsd.event.audio.volume_up";
		/**
		 * 音乐音量减小，音量由intent参数("volume")传递，取值范围0〜100
		 */
		static final String VOLUME_DOWN = "tsd.event.audio.volume_down";
		/**
		 * 播放器状态汇报
		 */
		static final String STATE = "tsd.event.audio.state";
	}

	interface CarDVR {
		/**
		 * 服务启动, 行车记录服务启动时主动发送
		 */
		static final String SERVICE_STARTED = "tsd.event.cardvr.service_started";
		
		static final String CLEAR_PHOTO = "tsd.event.cardvr.clear_photo";
		/**
		 * 服务停止, 行车记录服务停止时主动发送
		 */
		static final String SERVICE_STOPPED = "tsd.event.cardvr.service_stopped";
		/**
		 * 应用启动, 行车记录应用启动时主动发送
		 */
		static final String APP_STARTED = "tsd.event.cardvr.app_started";
		/**
		 * 应用退出, 行车记录应用退出时主动发送
		 */
		static final String APP_STOPPED = "tsd.event.cardvr.app_stopped";
		/**
		 * 录像开启, 由中控模块发送, 行车记录模块接收该消息
		 */
		static final String START_REC = "tsd.event.cardvr.start_record";
		/**
		 * 录像停止, 由中控模块发送, 行车记录模块接收该消息
		 */
		static final String STOP_REC = "tsd.event.cardvr.stop_record";
		/**
		 * 报警事件触发, 由中控模块发送, 行车记录模块接收该消息
		 */
		static final String ALERT_TRIGGERED = "tsd.event.cardvr.alert_triggered";
		/**
		 * 事故事件触发, 由中控模块发送, 行车记录模块接收该消息
		 */
		static final String ACCIDENT_TRIGGERED = "tsd.event.cardvr.accident_triggered";
		/**
		 * 拍照, 由中控模块发送, 行车记录模块接收该消息
		 */
		static final String TAKE_PICTURE = "tsd.event.cardvr.take_picture";
		/**
		 * 拍照完成
		 * 该消息由carDvr service发出。
		 */
		static final String PICTURE_TAKEN_COMPLETED = "tsd.event.cardvr.picture_taken_completed";
		/**
		 * 开始摄像头预览和调整
		 * 该消息由摄像头调整的程序发出，由carDvr service来处理。
		 */
		static final String START_CAM_PREVIEW = "tsd.event.cardvr.start_camera_preview";
		/**
		 * 停止摄像头预览和调整
		 * 该消息由摄像头调整的程序发出，由carDvr service来处理。
		 */
		static final String STOP_CAM_PREVIEW = "tsd.event.cardvr.stop_camera_preview";
		/**
		 * 摄像头空闲下来
		 * 该消息由carDvr service收到DVR_START_CAM_PREVIEW消息，停止录像，释放摄像头资源后发送给摄像头预览和调整的程序。
		 */
		static final String CAM_AVAILABLE = "tsd.event.cardvr.camera_available";
		
		/**
		 *拍照时是否同时锁定录像 
		 */
		static final String  PHOTO_RECORD = "tsd.event.cardvr.photo_record";
		
		/**
		 *是否收藏 
		 */
		static final String IS_LOVE = "tsd.event.cardvr.is_love";
	}

	interface Push {
		/**
		 * 服务启动, 推送服务启动时主动发送
		 */
		static final String SERVICE_STARTED = "tsd.event.push.service_started";
		/**
		 * 服务停止, 推送服务停止时主动发送
		 */
		static final String SERVICE_STOPPED = "tsd.event.push.service_stopped";
		/**
		 * 收到推送消息, 由各模块自行处理
		 */
		static final String MESSAGE_ARRIVED = "tsd.event.push.message_arrived";
		/**
		 * 发送反馈消息给服务器
		 */
		static final String FEED_BACK = "tsd.event.push.feed_back";
		/**
		 * 推送歌单广播, 由音乐模块接收处理
		 */
		static final String AUDIO_CATEGORY = "tsd.event.push.audio_category";
		/**
		 * 推送目的地消息，由导航模块接收处理
		 */
		static final String NAV_ADDR = "tsd.push.nav.dest";
	}

	interface Httpd {
		/**
		 * 服务启动, 推送服务启动时主动发送
		 */
		static final String SERVICE_STARTED = "tsd.event.httpd.service_started";
		/**
		 * 服务停止, 推送服务停止时主动发送
		 */
		static final String SERVICE_STOPPED = "tsd.event.httpd.service_stopped";
		/**
		 * 收到推送消息, 由各模块自行处理
		 */
		static final String MESSAGE_ARRIVED = "tsd.event.httpd.message_arrived";
		/**
		 * 发送反馈消息给服务器
		 */
		static final String FEED_BACK = "tsd.event.httpd.feed_back";
	}

	interface Update {
		/**
		 * 服务启动, 更新服务启动时主动发送
		 */
		static final String SERVICE_STARTED = "tsd.event.update.service_started";
		/**
		 * 服务停止, 更新服务停止时主动发送
		 */
		static final String SERVICE_STOPPED = "tsd.event.update.service_stopped";
		/**
		 * 更新查询及下载, 由更新模块接收处理
		 */
		static final String START_DOWNLOAD = "tsd.event.update.start_download";
		/**
		 * 更新安装, 由更新模块接收处理
		 */
		static final String START_INSTALL = "tsd.event.update.start_install";
		/**
		 * 更新信息显示, 由更新模块接收处理
		 */
		static final String DISPLAY_INFO = "tsd.event.update.display_info";
	}

	interface Navigation {
		/**
		 * 服务启动, 导航服务启动时主动发送
		 */
		static final String SERVICE_STARTED = "tsd.event.navigation.service_started";
		/**
		 * 服务停止, 导航服务停止时主动发送
		 */
		static final String SERVICE_STOPPED = "tsd.event.navigation.service_stopped";
		/**
		 * 应用启动, 导航应用启动时主动发送
		 */
		static final String APP_STARTED = "tsd.event.navigation.app_started";
		/**
		 * 应用停止, 导航应用停止时主动发送
		 */
		static final String APP_STOPPED = "tsd.event.navigation.app_stopped";
		/**
		 * 调用查询目的地方法
		 */
		static final String POI_SEACH = "tsd.event.navigation.POI_SEARCH";
		/**
		 * poi查询返回的结果, 由导航模块接收处理
		 */
		static final String POI_SEARCH_RESULT = "tsd.event.navigation.POI_SEARCH_RESULT";
		/**
		 * 用户选择路线，跳转activity
		 */
		static final String START_NAVIGATION = "tsd.event.navigation.START_NAVIGATION";
	}
}

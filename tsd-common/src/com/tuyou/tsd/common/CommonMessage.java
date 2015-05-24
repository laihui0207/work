package com.tuyou.tsd.common;

/**
 * 公共消息定义
 * @author ruhai
 * 2014-7
 */
public interface CommonMessage {
	
	/**
	 * F1-F4对应的广播
	 **/	
	@Deprecated
	public static final String HARDKEY1= "tsd.event.system.hardkey1_pressed";
	@Deprecated
	public static final String HARDKEY2= "tsd.event.system.hardkey2_pressed";
	@Deprecated
	public static final String HARDKEY3= "tsd.event.system.hardkey3_pressed";
	@Deprecated
	public static final String HARDKEY4= "tsd.event.system.hardkey4_pressed";
	
	/**
	 * 设备初始化完成
	 */
	static final String INIT_COMPLETE = "tsd.init.COMPLETE";

	//
	// TTS
	//
	/**
	 * TTS播报
	 */
	static final String TTS_PLAY = "tsd.tts.PLAY";
	/**
	 * TTS播报完成
	 */
	static final String TTS_PLAY_FINISHED = "tsd.tts.PLAY_FINISHED";
	/**
	 * 停止TTS播报
	 */
	static final String TTS_STOP = "tsd.tts.STOP";
	/**
	 * 停止TTS播报并清空队列
	 */
	static final String TTS_CLEAR = "tsd.tts.CLEAR";
	/**
	 * 暂停TTS播报
	 */
	static final String TTS_PAUSE = "tsd.tts.PAUSE";
	/**
	 * 恢复TTS播报
	 */
	static final String TTS_RESUME = "tsd.tts.RESUME";

	//
	// 交互场景
	//

	/**
	 * 基本语音菜单交互触发
	 */
	static final String INTERACT_WAKE_UP = "tsd.intact.GENERIC";
	/**
	 * 基本语音菜单交互结果
	 */
	static final String RESULT_INTERACT_WAKE_UP = "tsd.intact.result.GENERIC";
	/**
	 * 导航目的地询问交互
	 */
	static final String INTERACT_QUERY_NAV_DEST = "tsd.intact.NAV_QUERY_DEST";
	/**
	 * 导航目的地询问交互结果
	 */
	static final String RESULT_INTERACT_QUERY_NAV_DEST = "tsd.intact.result.NAV_QUERY_DEST";

	//
	// 语音指令
	//
	/**
	 * 语音唤醒交互结束
	 */
	static final String VOICE_COMM_WAKEUP = "tsd.command.WAKE_UP";
	/**
	 * 指令: 拍照
	 */
	static final String VOICE_COMM_TAKE_PICTURE = "tsd.command.TAKE_PICTURE";
	/**
	 * 指令: 导航
	 */
	static final String VOICE_COMM_MAP = "tsd.command.MAP";
	/**
	 * 指令: 新闻
	 */
	static final String VOICE_COMM_NEWS = "tsd.command.NEWS";
	/**
	 * 指令: 笑话
	 */
	static final String VOICE_COMM_JOKE = "tsd.command.JOKE";
	/**
	 * 指令: 音乐
	 */
	static final String VOICE_COMM_MUSIC = "tsd.command.MUSIC";
	/**
	 * 指令: 好
	 */
	static final String VOICE_COMM_POSITIVE = "tsd.command.POSITIVE";
	/**
	 * 指令: 不好
	 */
	static final String VOICE_COMM_NEGATIVE = "tsd.command.NEGATIVE";
	/**
	 * 指令: 闭嘴
	 */
	static final String VOICE_COMM_SHUT_UP = "tsd.command.SHUT_UP";
//	/**
//	 * 指令: 暂停
//	 */
//	static final String VOICE_COMM_PAUSE = "tsd.command.PAUSE";
//	/**
//	 * 指令: 继续
//	 */
//	static final String VOICE_COMM_RESUME = "tsd.command.RESUME";
//	/**
//	 * 指令: 上一首
//	 */
//	static final String VOICE_COMM_PREVIOUS = "tsd.command.PREVIOUS";
//	/**
//	 * 指令: 下一首
//	 */
//	static final String VOICE_COMM_NEXT = "tsd.command.NEXT";
//	/**
//	 * 指令: 赞
//	 */
//	static final String VOICE_COMM_LIKE = "tsd.command.LIKE";
//	/**
//	 * 指令: 踩
//	 */
//	static final String VOICE_COMM_DISLIKE = "tsd.command.DISLIKE";
//	/**
//	 * 指令: 赞上一首
//	 */
//	static final String VOICE_COMM_LIKE_PREV = "tsd.command.LIKE_PREV";

	//
	// 行车记录消息
	//
	/**
	 * 录像开启, 建议使用TSDEvent.CarDVR中相应消息替换
	 */
	@Deprecated
	static final String DVR_START_REC = "tsd.event.cardvr.start_record";
	/**
	 * 录像停止, 建议使用TSDEvent.CarDVR中相应消息替换
	 */
	@Deprecated
	static final String DVR_STOP_REC = "tsd.event.cardvr.stop_record";
	/**
	 * 报警事件触发, 建议使用TSDEvent.CarDVR中相应消息替换
	 */
	@Deprecated
	static final String DVR_ALERT = "tsd.event.cardvr.alert_triggered";
	/**
	 * 事故事件触发, 建议使用TSDEvent.CarDVR中相应消息替换
	 */
	@Deprecated
	static final String DVR_ACCIDENT = "tsd.event.cardvr.accident_triggered";
	/**
	 * 拍照, 建议使用TSDEvent.CarDVR中相应消息替换
	 */
	@Deprecated
	static final String DVR_TAKE_PICTURE = "tsd.event.cardvr.take_picture";
	/**
	 * 拍照完成
	 * 该消息由carDvr service发出。建议使用TSDEvent.CarDVR中相应消息替换
	 */
	@Deprecated
	static final String DVR_TAKE_PICTURE_COMPLETED = "tsd.event.cardvr.picture_taken_completed";
	/**
	 * 开始摄像头预览和调整
	 * 该消息由摄像头调整的程序发出，由carDvr service来处理。建议使用TSDEvent.CarDVR中相应消息替换
	 */
	@Deprecated
	static final String DVR_START_CAM_PREVIEW = "tsd.event.cardvr.start_camera_preview";
	/**
	 * 停止摄像头预览和调整
	 * 该消息由摄像头调整的程序发出，由carDvr service来处理。建议使用TSDEvent.CarDVR中相应消息替换
	 */
	@Deprecated
	static final String DVR_STOP_CAM_PREVIEW = "tsd.event.cardvr.stop_camera_preview";
	/**
	 * 摄像头空闲下来
	 * 该消息由carDvr service收到DVR_START_CAM_PREVIEW消息，停止录像，释放摄像头资源后发送给摄像头预览和调整的程序。
	 * 建议使用TSDEvent.CarDVR中相应消息替换
	 */
	@Deprecated
	static final String DVR_CAM_AVAILABLE = "tsd.event.cardvr.camera_available";
	
	//
	// ACC state
	//
	/**
	 * 建议使用TSDEvent.System中相应消息替换
	 */
	@Deprecated
	static final String EVT_ACC_ON = "tsd.event.system.acc_on";
	/**
	 * 建议使用TSDEvent.System中相应消息替换
	 */
	@Deprecated
	static final String EVT_ACC_OFF = "tsd.event.system.acc_off";

	//
	// System state
	//
	/**
	 * 视频播放开始, 建议使用TSDEvent.CarDVR中相应消息替换
	 */
	@Deprecated
	static final String EVT_VIDEO_PLAYING_BEGIN = "tsd.event.cardvr.app_started";
	/**
	 * 视频播放结束, 建议使用TSDEvent.CarDVR中相应消息替换
	 */
	@Deprecated
	static final String EVT_VIDEO_PLAYING_END = "tsd.event.cardvr.service_stopped";
	
	/**
	 * poi查询返回的结果, 建议使用TSDEvent.Navigation相应消息替换
	 */
	@Deprecated
	static final String EVT_NAVIGATION_SEARCH_RESULT = "tsd.event.navigation.dest_search_result";

	//
	// Update相关
	//
	/**
	 * 更新查询及下载, 建议使用TSDEvent.Update中相应消息替换
	 */
	@Deprecated
	static final String UPDATE_DOWNLOAD = "tsd.event.update.start_download";
	/**
	 * 更新安装, 建议使用TSDEvent.Update中相应消息替换
	 */
	@Deprecated
	static final String UPDATE_INSTALL = "tsd.event.update.start_install";
	/**
	 * 更新信息显示, 建议使用TSDEvent.Update中相应消息替换
	 */
	@Deprecated
	static final String UPDATE_DISPLAYINFO = "tsd.event.update.display_info";

	// Push相关的消息
	//
	/**
	 * 收到push消息, 建议使用TSDEvent.Push中相应消息替换
	 */
	@Deprecated
	static final String PUSH_MESSAGE_ARRIVED = "tsd.event.push.message_arrived";
	//Push message type of interaction. It must be same as server.
	static final String PUSH_MESSAGE_TYPE_INTERACTION = "interaction";
	//static final String PUSH_MESSAGE_TYPE_xxxx;
	
//	static final String PUSH_MESSAGE_RESULT = "tsd.push.message.result";
	/**
	 * 推送歌单广播, 建议使用TSDEvent.Push中相应消息替换
	 */
	@Deprecated
	static final String PUSH_CATEGORY = "tsd.event.push.audio_category";
	
	//
	// IPC Messages
	//
	/**
	 * 语音助手服务IPC通信消息定义
	 */
	static interface VoiceEngine {
		/**
		 * 注册MessengerHandler
		 */
		static final int REGISTER_CLIENT = 1;
		/**
		 * 取消MessengerHandler监听
		 */
		static final int UNREGISTER_CLIENT = 2;
		/**
		 * 状态：录音开始
		 */
		static final int RECORDING_START = 3;
		/**
		 * 状态：录音结束
		 */
		static final int RECORDING_STOP = 4;
		/**
		 * 状态：录音音量
		 */
		static final int RECORDING_VOLUME = 5;
		/**
		 * 状态：语音识别开始
		 */
		static final int RECOGNITION_START = 6;
		/**
		 * 状态：语音识别结束（成功）
		 */
		static final int RECOGNITION_COMPLETE = 7;
		/**
		 * 状态：语音识别结束（错误）
		 */
		static final int RECOGNITION_ERROR = 8;
		/**
		 * 状态：语音识别结束（取消）
		 */
		static final int RECOGNITION_CANCEL = 9; 
		/**
		 * 状态：交互对话开始
		 */
		static final int INTERACTION_START = 10;
		/**
		 * 状态：交互对话结束
		 */
		static final int INTERACTION_STOP = 11;
		/**
		 * 状态：TTS开始播放
		 */
		static final int TTS_PLAY_BEGIN = 12;
		/**
		 * 状态：TTS播放完成
		 */
		static final int TTS_PLAY_END = 13;
		/**
		 * 状态：搜索开始
		 */
		static final int SEARCH_BEGIN = 14;
		/**
		 * 状态：搜索结束
		 */
		static final int SEARCH_END = 15;

		/**
		 * 控制：启动唤醒监听
		 */
		static final int START_WAKEUP_LISTENING = 20;
		/**
		 * 控制：停止唤醒监听
		 */
		static final int STOP_WAKEUP_LISTENING = 21;
		/**
		 * 控制：开始录音识别
		 */
		static final int START_RECOGNITION = 22;
		/**
		 * 控制：停止识别
		 */
		static final int STOP_RECOGNITION = 23;
		/**
		 * 控制：取消识别
		 */
		static final int CANCEL_RECOGNITION = 24;
		/**
		 * 控制：开始执行交互
		 */
		static final int START_INTERACTION = 25;
		/**
		 * 控制：停止执行交互
		 */
		static final int STOP_INTERACTION = 26;
		/**
		 * 控制：取消执行交互
		 */
		static final int CANCEL_INTERACTION = 27;
	}
}

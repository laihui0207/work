package com.tuyou.tsd.common;

/**
 * 公共消息定义
 * @author ruhai
 * 2014-7
 */
public interface CommonMessage {
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
	static final String INTERACT_MENU_SELECT = "tsd.intact.MENU_SELECT";
	/**
	 * 基本语音菜单交互结果
	 */
	static final String RESULT_INTERACT_MENU_SELECT = "tsd.intact.result.MENU_SELECT";
	/**
	 * 高级语音菜单交互触发
	 */
	static final String INTERACT_ADV_MENU_SELECT = "tsd.intact.ADV_MENU_SELECT";
	/**
	 * 高级语音菜单交互结果
	 */
	static final String RESULT_INTERACT_ADV_MENU_SELECT = "tsd.intact.result.ADV_MENU_SELECT";
	/**
	 * 空闲询问
	 */
	static final String INTERACT_IDLE_ASKING = "tsd.intact.IDLE_ASKING";
	/**
	 * 空闲询问交互结果
	 */
	static final String RESULT_INTERACT_IDLE_ASKING = "tsd.intact.result.IDLE_ASKING";

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
	/**
	 * 指令: 暂停
	 */
	static final String VOICE_COMM_PAUSE = "tsd.command.PAUSE";
	/**
	 * 指令: 继续
	 */
	static final String VOICE_COMM_RESUME = "tsd.command.RESUME";
	/**
	 * 指令: 上一首
	 */
	static final String VOICE_COMM_PREVIOUS = "tsd.command.PREVIOUS";
	/**
	 * 指令: 下一首
	 */
	static final String VOICE_COMM_NEXT = "tsd.command.NEXT";
	/**
	 * 指令: 赞
	 */
	static final String VOICE_COMM_LIKE = "tsd.command.LIKE";
	/**
	 * 指令: 踩
	 */
	static final String VOICE_COMM_DISLIKE = "tsd.command.DISLIKE";
	/**
	 * 指令: 赞上一首
	 */
	static final String VOICE_COMM_LIKE_PREV = "tsd.command.LIKE_PREV";

	//
	// 行车记录消息
	//
	/**
	 * 录像开启
	 */
	static final String DVR_START_REC = "tsd.dvr.START_RECORD";
	/**
	 * 录像停止
	 */
	static final String DVR_STOP_REC = "tsd.dvr.STOP_RECORD";
	/**
	 * 报警事件触发
	 */
	static final String DVR_ALERT = "tsd.dvr.ALERT";
	/**
	 * 事故事件触发
	 */
	static final String DVR_ACCIDENT = "tsd.dvr.ACCIDENT";
	/**
	 * 拍照
	 */
	static final String DVR_TAKE_PICTURE = "tsd.dvr.TAKE_PICTURE";
	/**
	 * 拍照完成
	 * 该消息由carDvr service发出。
	 */
	static final String DVR_TAKE_PICTURE_COMPLETED = "tsd.dvr.TAKE_PICTURE_COMPLETED";
	
	//
	// ACC state
	//
	static final String EVT_ACC_ON = "tsd.event.ACC_ON";
	static final String EVT_ACC_OFF = "tsd.event.ACC_OFF";

	//
	// System state
	//
	/**
	 * 开机问候播报开始
	 */
	static final String EVT_GREETING_BEGIN = "tsd.event.GREETING_BEGIN";
	/**
	 * 开机问候播报结束
	 */
	static final String EVT_GREETING_END = "tsd.event.GREETING_END";
	/**
	 * 熄火问候播报开始
	 */
	static final String EVT_GOODBYE_BEGIN = "tsd.event.GOODBYE_BEGIN";
	/**
	 * 熄火问候播报结束
	 */
	static final String EVT_GOODBYE_END = "tsd.event.GOODBYE_END";
	/**
	 * 音乐播放开始
	 */
	static final String EVT_MUSIC_BEGIN = "tsd.event.MUSIC_BEGIN";
	/**
	 * 音乐播放结束
	 */
	static final String EVT_MUSIC_END = "tsd.event.MUSIC_END";
	/**
	 * 交互开始
	 */
	static final String EVT_INTERACT_BEGIN = "tsd.event.INTERACTION_BEGIN";
	/**
	 * 交互结束
	 */
	static final String EVT_INTERACT_END = "tsd.event.INTERACTION_END";
	/**
	 * 语音唤醒
	 */
	static final String EVT_DEVICE_WAKEUP = "tsd.event.DEVICE_WAKEUP";
	/**
	 * 导航结束
	 */
	static final String EVT_NAVIGATION_FINISHED = "tsd.event.NAVIGATION_FINISHED";
	/**
	 * 用户教育开始
	 */
	static final String EVT_TEACHING_BEGIN = "tsd.event.TEACHING_BEGIN";
	/**
	 * 用户教育结束
	 */
	static final String EVT_TEACHING_END = "tsd.event.TEACHING_END";
	/**
	 * 视频播放开始
	 */
	static final String EVT_VIDEO_PLAYING_BEGIN = "tsd.event.VIDEO_PLAYING_BEGIN";
	/**
	 * 视频播放结束
	 */
	static final String EVT_VIDEO_PLAYING_END = "tsd.event.VIDEO_PLAYING_END";

}

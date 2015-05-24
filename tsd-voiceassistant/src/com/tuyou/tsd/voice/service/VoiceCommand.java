package com.tuyou.tsd.voice.service;

import java.util.HashMap;
import java.util.Map;

import com.tuyou.tsd.common.CommonMessage;

/**
 * 本地指令关系映射
 * @author ruhai
 * 2014-8-9
 */
public final class VoiceCommand {
	static final Map<String, String> map = new HashMap<String, String>();
	// 定义指令关系映射
	static {
//		map.put("拍照",	 CommonMessage.VOICE_COMM_TAKE_PICTURE);
//		map.put("拍张照",	 CommonMessage.VOICE_COMM_TAKE_PICTURE);
//		map.put("拍个照", CommonMessage.VOICE_COMM_TAKE_PICTURE);

		map.put("导航",   CommonMessage.VOICE_COMM_MAP);
		map.put("打开导航",   CommonMessage.VOICE_COMM_MAP);
		map.put("我要导航",   CommonMessage.VOICE_COMM_MAP);
		map.put("我想导航",   CommonMessage.VOICE_COMM_MAP);

		map.put("新闻",	 CommonMessage.VOICE_COMM_NEWS);
		map.put("打开新闻",	 CommonMessage.VOICE_COMM_NEWS);
		map.put("听新闻",	 CommonMessage.VOICE_COMM_NEWS);
		map.put("播新闻",	 CommonMessage.VOICE_COMM_NEWS);
		map.put("放新闻",	 CommonMessage.VOICE_COMM_NEWS);
		map.put("帮我打开新闻",	 CommonMessage.VOICE_COMM_NEWS);
		map.put("给我打开新闻",	 CommonMessage.VOICE_COMM_NEWS);
		map.put("我要打开新闻",	 CommonMessage.VOICE_COMM_NEWS);
		map.put("我要听新闻",	 CommonMessage.VOICE_COMM_NEWS);

		map.put("播客",	 CommonMessage.VOICE_COMM_JOKE);
		map.put("打开播客",	 CommonMessage.VOICE_COMM_JOKE);
		map.put("听播客",	 CommonMessage.VOICE_COMM_JOKE);
		map.put("放播客",	 CommonMessage.VOICE_COMM_JOKE);
		map.put("帮我打开播客",	 CommonMessage.VOICE_COMM_JOKE);
		map.put("给我打开播客",	 CommonMessage.VOICE_COMM_JOKE);
		map.put("我要打开播客",	 CommonMessage.VOICE_COMM_JOKE);
		map.put("我要听播客",	 CommonMessage.VOICE_COMM_JOKE);

		map.put("音乐",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("听音乐",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("听歌",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("放音乐",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("放歌",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("唱歌",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("给我唱个歌",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("给我放个歌",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("给我播个歌",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("播歌",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("播音乐",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("播个歌",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("播个音乐",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("打开音乐",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("我想听音乐",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("我要听音乐",	 CommonMessage.VOICE_COMM_MUSIC);
		map.put("我想听歌",	 CommonMessage.VOICE_COMM_MUSIC);

		map.put("好",	 CommonMessage.VOICE_COMM_POSITIVE);
		map.put("是",	 CommonMessage.VOICE_COMM_POSITIVE);
		map.put("要",	 CommonMessage.VOICE_COMM_POSITIVE);
		map.put("行",	 CommonMessage.VOICE_COMM_POSITIVE);
		map.put("可以",	 CommonMessage.VOICE_COMM_POSITIVE);
		map.put("好的",	 CommonMessage.VOICE_COMM_POSITIVE);

		map.put("不",	 CommonMessage.VOICE_COMM_NEGATIVE);
		map.put("不好",	 CommonMessage.VOICE_COMM_NEGATIVE);
		map.put("不行",	 CommonMessage.VOICE_COMM_NEGATIVE);
		map.put("不要",	 CommonMessage.VOICE_COMM_NEGATIVE);
		map.put("不是",	 CommonMessage.VOICE_COMM_NEGATIVE);
		map.put("不可以",	 CommonMessage.VOICE_COMM_NEGATIVE);

//		map.put("闭嘴",	 CommonMessage.VOICE_COMM_SHUT_UP);
//		map.put("别说话",	 CommonMessage.VOICE_COMM_SHUT_UP);
//		map.put("安静",	 CommonMessage.VOICE_COMM_SHUT_UP);

//		map.put("暂停",	 CommonMessage.VOICE_COMM_PAUSE);
//		map.put("继续",	 CommonMessage.VOICE_COMM_RESUME);
//		map.put("上一首",	 CommonMessage.VOICE_COMM_PREVIOUS);
//		map.put("下一首",	 CommonMessage.VOICE_COMM_NEXT);
//		map.put("赞",	 CommonMessage.VOICE_COMM_LIKE);
//		map.put("踩",	 CommonMessage.VOICE_COMM_DISLIKE);
//		map.put("赞上一首", CommonMessage.VOICE_COMM_LIKE_PREV);
	}
}

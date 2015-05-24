package com.tuyou.tsd.voice.test;

import android.test.AndroidTestCase;

import com.iflytek.tts.TtsService.Tts;
import com.tuyou.tsd.common.CommonConsts;
import com.tuyou.tsd.voice.service.MyVoiceEngine;

public class TtsTest extends AndroidTestCase {
	private MyVoiceEngine engine;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		engine = MyVoiceEngine.getInstance(mContext);
		engine.startEngine(CommonConsts.VOICE_ASSISTANT_PATH);
//		String filename = CommonConsts.VOICE_ASSISTANT_PATH + "/Resource.irf";
//		Tts.JniInit(filename);
	}

	@Override
	protected void tearDown() throws Exception {
//		Tts.JniStop();
		engine.stopEngine();
		super.tearDown();
	}

//	public void testTts() {
//		for (int i = 0; i < 5; i++) {
//			Tts.JniSpeak("TTS测试" + i);
//		}
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for (int i = 0; i < 5; i++) {
//			Tts.JniSpeak("TTS测试" + i);
//		}
//	}

//	public void testTts2() {
//		for (int i = 0; i < 3; i++) {
//			engine.ttsPlay("TTS测试" + i, "", 0, false);
//		}
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for (int i = 0; i < 3; i++) {
//			engine.ttsPlay("TTS测试" + i, "", 0, false);
//		}
//	}

	public void testTts3() {
		engine.ttsPlay("不知什么原因~", "", 0, false);
		engine.ttsPlay("由于同步播放会在第二次播放时发生异常，所以只能采用异步播放方式。", "", 0, false);
		engine.ttsPlay("但为保证输入识别在TTS播报后才开始，故增加此回调方法.~", "", 0, false);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

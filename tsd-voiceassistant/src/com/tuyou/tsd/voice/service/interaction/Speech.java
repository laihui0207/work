package com.tuyou.tsd.voice.service.interaction;

/**
 * 提示语
 * @author ruhai
 * 2014-7
 */
public class Speech {

	/**
	 * 提示语内容，string | filename
	 */
	String content;
	/**
	 * 播放方式：tts －－ TTS语音播放；audio －－ 音频资源文件直接播放
	 */
	String mode;
	/**
	 * 角色类别，控制TTS播报方式。目前仅支持"male", "female"
	 */
	String role;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}

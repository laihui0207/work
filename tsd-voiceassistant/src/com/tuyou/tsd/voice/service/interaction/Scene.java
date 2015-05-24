package com.tuyou.tsd.voice.service.interaction;

/**
 * 交互场景
 * @author ruhai
 * 2014-7
 */
public class Scene {

	/**
	 * 场景名称
	 */
	String name;
	/**
	 * 交互对话
	 */
	Dialog[] dialogs;

	public Scene() {}
	public Scene(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Dialog[] getDialogs() {
		return dialogs;
	}

	public void setDialogs(Dialog[] dialogs) {
		this.dialogs = dialogs;
	}
}

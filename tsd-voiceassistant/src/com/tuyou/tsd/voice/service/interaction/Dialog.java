package com.tuyou.tsd.voice.service.interaction;

import com.tuyou.tsd.voice.service.interaction.Action.FailedAction;
import com.tuyou.tsd.voice.service.interaction.Action.SuccessfulAction;

/**
 * 交互对话
 * @author ruhai
 * 2014-7
 */
public class Dialog {

	/**
	 * 对话ID
	 */
	int id;
	/**
	 * 窗口显示的内容
	 */
	Display display;
	/**
	 * 问题提示语
	 */
	Speech question;
	/**
	 * 候选答案列表
	 */
	String[] expectedKeyword;
	/**
	 * 超时等待时间
	 */
	int timeout;
	/**
	 * 当对话成功后执行的动作
	 */
	SuccessfulAction[] successfulActions;
	/**
	 * 当对话失败后执行的动作（未识别、超时或答案不在修选列表中均算作失败）
	 */
	FailedAction[] failedActions;

	public Dialog() {}
	public Dialog(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Display getDisplay() {
		return display;
	}
	public void setDisplay(Display display) {
		this.display = display;
	}
	public Speech getQuestion() {
		return question;
	}

	public void setQuestion(Speech question) {
		this.question = question;
	}

	public String[] getExpectedKeyword() {
		return expectedKeyword;
	}

	public void setExpectedKeyword(String[] expectedKeyword) {
		this.expectedKeyword = expectedKeyword;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public SuccessfulAction[] getSuccessActions() {
		return successfulActions;
	}

	public void setSuccessfulActions(SuccessfulAction[] successAction) {
		this.successfulActions = successAction;
	}

	public FailedAction[] getFailureActions() {
		return failedActions;
	}

	public void setFailedActions(FailedAction[] failureAction) {
		this.failedActions = failureAction;
	}

}

package com.tuyou.tsd.voice.service.interaction;


/**
 * 跳转步骤，用于定义一次对话完成后的跳转
 * @author ruhai
 * 2014-7
 */
public abstract class Action {

	/**
	 * 动作类别.
	 *   return -- 结束对话并返回，返回值为value；
	 *   jump   -- 跳转，跳转内容在next中定义
	 */
	String action;
	/**
	 * 提示语
	 */
	Speech speech;
	/**
	 * 下一步执行的动作
	 */
	Jump next;
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public Speech getSpeech() {
		return speech;
	}
	public void setSpeech(Speech speech) {
		this.speech = speech;
	}
	public Jump getJump() {
		return next;
	}
	public void setJump(Jump next) {
		this.next = next;
	}

	public static class SuccessfulAction extends Action {
		/**
		 * 对话结果，即识别出的答案
		 */
		String value;
		/**
		 * 
		 */
		String message;
		/**
		 * Json param
		 */
		String params;
		
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String getParams() {
			return params;
		}
		public void setParams(String params) {
			this.params = params;
		}
	}

	/**
	 * 对话失败后的跳转步骤
	 * @author ruhai
	 *
	 */
	public static class FailedAction extends Action {
		String reason;

		public String getReason() {
			return reason;
		}
		public void setReason(String reason) {
			this.reason = reason;
		}

	}
}

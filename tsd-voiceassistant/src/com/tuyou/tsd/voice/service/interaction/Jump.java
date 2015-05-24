package com.tuyou.tsd.voice.service.interaction;


/**
 * 动作定义
 * @author ruhai
 * 2014-7
 */
public class Jump {

	/**
	 * 待跳转的dialog id
	 */
	int dialogId;
	/**
	 * 定义硬按键功能
	 */
	HardKeyFunction hardKeyFunction;

	public int getDialogId() {
		return dialogId;
	}

	public void setDialogId(int dialogId) {
		this.dialogId = dialogId;
	}

	public HardKeyFunction getHardKeyFunction() {
		return hardKeyFunction;
	}

	public void setHardKeyFunction(HardKeyFunction hardKeyAction) {
		this.hardKeyFunction = hardKeyAction;
	}

	/**
	 * 硬按键功能定义
	 * @author ruhai
	 *
	 */
	public static class HardKeyFunction {
		/**
		 * 数字表示对应的硬按键，如[1, 2, 3, 4]
		 */
		Integer[] keys;
		/**
		 * 功能定义，light－－按键亮；twinkle－－按键闪烁
		 */
		String command;

		public Integer[] getKeys() {
			return keys;
		}

		public void setKeys(Integer[] keys) {
			this.keys = keys;
		}

		public String getCommand() {
			return command;
		}

		public void setCommand(String command) {
			this.command = command;
		}
	}

}

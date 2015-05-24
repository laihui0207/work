package com.tuyou.tsd.voice.service.interaction;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.voice.service.interaction.Action.FailedAction;
import com.tuyou.tsd.voice.service.interaction.Action.SuccessfulAction;
import com.tuyou.tsd.voice.service.interaction.Jump.HardKeyFunction;

/**
 * 交互模块解析
 * @author ruhai
 * 2014-7
 */
public abstract class InteractionParser {
	private static final String LOG_TAG = "InteractionParser";

	public static Scene parseScene(String template) throws JSONException {
		long start = System.currentTimeMillis();
		LogUtil.v(LOG_TAG, "parseScene()...");

		JSONTokener jTok = new JSONTokener(template);
		JSONObject jObj = (JSONObject) jTok.nextValue();
		Scene scene = null;
		if (jObj != null) {
			String name = jObj.optString("name");
			LogUtil.d(LOG_TAG, "开始解析模板: " + name);

			scene = new Scene(name);
			scene.dialogs = parseDialogs(jObj.getJSONArray("dialog"));
		}
		LogUtil.d(LOG_TAG, "模板解析完毕, 总共用时" + (System.currentTimeMillis() - start) + " ms.");

		return scene;
	}

	/**
	 * 解析Interaction
	 * @param jArray
	 * @return
	 * @throws JSONException 
	 */
	private static Dialog[] parseDialogs(JSONArray jArray) throws JSONException {
		List<Dialog> dialogList = new ArrayList<Dialog>();
		if (jArray != null) {
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject obj = jArray.optJSONObject(i);
				Dialog dialog = parseDialog(obj);
				if (dialog != null)
					dialogList.add(dialog);
			}
		}
		return dialogList.size() > 0 ? dialogList.toArray(new Dialog[dialogList.size()]) : null;
	}

	/**
	 * Parse the Dialog
	 * @param jObj
	 * @return
	 * @throws JSONException
	 */
	private static Dialog parseDialog(JSONObject jObj) throws JSONException {
		Dialog dialog = null;
		if (jObj != null) {
			dialog = new Dialog(jObj.getInt("id"));
			dialog.display = parseDisplay(jObj.optJSONObject("display"));
			dialog.question = parseSpeech(jObj.optJSONObject("speech"));
			dialog.expectedKeyword = parseExpectedKeywords(jObj.optJSONArray("expectedAnswer"));
			dialog.timeout = jObj.optInt("timeout");
			dialog.successfulActions = parseSuccessfulActions(jObj.optJSONArray("onSuccess"));
			dialog.failedActions = parseFailedActions(jObj.optJSONArray("onFailed"));
		}
		return dialog;
	}

	private static Display parseDisplay(JSONObject jObj) throws JSONException {
		Display result = null;
		if (jObj != null) {
			result = new Display();
			result.title = jObj.optString("title");
			result.content = jObj.optString("content");
			result.hint = jObj.optString("hint");
		}
		return result;
	}

	/**
	 * Parse the Speech
	 * @param jObj
	 * @return
	 * @throws JSONException
	 */
	private static Speech parseSpeech(JSONObject jObj) throws JSONException {
		Speech result = null;
		if (jObj != null) {
			result = new Speech();
			result.content = jObj.getString("content");
			result.mode = jObj.getString("mode");
			result.role = jObj.optString("role");
		}
		return result;
	}

	/**
	 * Parse the expected keywords
	 * @param jArray
	 * @return
	 * @throws JSONException
	 */
	private static String[] parseExpectedKeywords(JSONArray jArray) throws JSONException {
		List<String> result = new ArrayList<String>();
		if (jArray != null) {
			for (int i = 0; i < jArray.length(); i++) {
				result.add(jArray.getString(i));
			}
		}
		return result.size() > 0 ? result.toArray(new String[result.size()]) : null;
	}

	/**
	 * Parse the successful branch action
	 * @param jArray
	 * @return
	 * @throws JSONException
	 */
	private static SuccessfulAction[] parseSuccessfulActions(JSONArray jArray) throws JSONException {
		List<SuccessfulAction> stepList = new ArrayList<SuccessfulAction>();
		if (jArray != null) {
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject jObj = jArray.getJSONObject(i);
				SuccessfulAction step = parseSuccessfulAction(jObj);
				if (step != null)
					stepList.add(step);
			}
		}
		return stepList.size() > 0 ? stepList.toArray(new SuccessfulAction[stepList.size()]) : null;
	}

	/**
	 * Parse the failure branch action
	 * @param jObj
	 * @return
	 * @throws JSONException
	 */
	private static FailedAction[] parseFailedActions(JSONArray jArray) throws JSONException {
		List<FailedAction> stepList = new ArrayList<FailedAction>();
		if (jArray != null) {
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject jObj = jArray.getJSONObject(i);
				FailedAction step = parseFailureAction(jObj);
				if (step != null)
					stepList.add(step);
			}
		}
		return stepList.size() > 0 ? stepList.toArray(new FailedAction[stepList.size()]) : null;
	}

	private static FailedAction parseFailureAction(JSONObject jObj) throws JSONException {
		FailedAction result = null;
		if (jObj != null) {
			result = new FailedAction();
//			result.retry = jObj.optInt("retry");
			result.reason = jObj.optString("reason");
			result.speech = parseSpeech(jObj.optJSONObject("speech"));
			result.action = jObj.getString("action");
			if (result.action.equals("jump")) {
				result.next = parseJumpAction(jObj.getJSONObject("next"));
			}
		}
		return result;
	}

	/**
	 * Parse the step of successful
	 * @param jObj
	 * @return
	 * @throws JSONException
	 */
	private static SuccessfulAction parseSuccessfulAction(JSONObject jObj) throws JSONException {
		SuccessfulAction result = null;
		if (jObj != null) {
			result = new SuccessfulAction();
			result.value = jObj.getString("value");
			result.speech = parseSpeech(jObj.optJSONObject("speech"));
			result.action = jObj.getString("action");
			if (result.action.equals("jump")) {
				result.next = parseJumpAction(jObj.getJSONObject("next"));
			}
			String message = jObj.optString("message");
			if (message != null && !message.isEmpty()) {
				result.message = message;
			}
			JSONObject params = jObj.optJSONObject("params");
			if (params != null) {
				if (params.has("url")) {
					// 修改由于直接调用JSONObject.toString()导致url内容中的"/"变成"＼/"的问题，
					// 由于目前只有url内容，所以先暂时写死，后面考虑更合适的方法进行处理。
					// 2014-10-29 Alex
					String strParam = params.optString("url");
					result.params = "url:" + strParam;
				}else {
					result.params = params.toString();
				}
			}
		}
		return result;
	}

	/**
	 * Parse the jump action
	 * @param jObj
	 * @return
	 * @throws JSONException
	 */
	private static Jump parseJumpAction(JSONObject jObj) throws JSONException {
		Jump result = null;
		if (jObj != null) {
			result = new Jump();
			result.dialogId = jObj.getInt("did");
			result.hardKeyFunction = parseHardkeyAction(jObj.optJSONObject("hardKey"));
		}
		return result;
	}

	/**
	 * Parse the hard key action
	 * @param jObj
	 * @return
	 * @throws JSONException
	 */
	private static HardKeyFunction parseHardkeyAction(JSONObject jObj) throws JSONException {
		HardKeyFunction result = null;
		if (jObj != null) {
			result = new HardKeyFunction();
			result.keys = parseKeys(jObj.getJSONArray("keys"));
			result.command = jObj.getString("command");
		}
		return result;
	}

	/**
	 * Parse the key's id
	 * @param jArray
	 * @return
	 * @throws JSONException
	 */
	private static Integer[] parseKeys(JSONArray jArray) throws JSONException {
		List<Integer> keys = new ArrayList<Integer>();
		if (jArray != null) {
			for (int i = 0; i < jArray.length(); i++) {
				keys.add(jArray.getInt(i));
			}
		}
		return keys.size() > 0 ? keys.toArray(new Integer[keys.size()]) : null;
	}

}

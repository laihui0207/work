package com.tuyou.tsd.settings.init.layout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDComponent;
import com.tuyou.tsd.common.TSDShare;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.settings.init.InitMainActivity;

@SuppressLint("ValidFragment")
public class BaseFragment extends Fragment {
	public SharedPreferences pref;
	public Editor editor;
	public ViewPager viewPager;
	private Activity activity;

	public BaseFragment(Activity activity) {
		this.activity = activity;
		viewPager = InitMainActivity.viewPager;
		// 通过该Context获得所需的SharedPreference实例
		pref = HelperUtil.getCommonPreference(activity,
				TSDComponent.CORE_SERVICE_PACKAGE,
				TSDShare.SYSTEM_SETTING_PREFERENCES);
		if (pref != null) {
			editor = pref.edit();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Intent intent = new Intent(
				"com.tuyou.tsd.settings.sync.SyncDownService");
		activity.startService(intent);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		Intent intent = new Intent("com.tuyou.tsd.settings.sync.SyncUpService");
		activity.startService(intent);
		super.onDestroyView();
	}

	/**
	 * 函数名称 : playBroadcast 功能描述 : 开始播报语音广播 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-8-13 下午1:57:48 修改人：wanghh 描述 ：
	 * 
	 */
	public void playBroadcast(String content, int pid) {
		Intent intent = new Intent();
		intent.setAction(CommonMessage.TTS_PLAY);
		// 要发送的内容
		intent.putExtra("package", "com.tuyou.tsd.settings");
		intent.putExtra("id", pid);
		intent.putExtra("content", content);
		// 发送 一个无序广播
		getActivity().sendBroadcast(intent);
	}

	/**
	 * 函数名称 : stopBroadcast 功能描述 : 主动停止语音播报广播 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-8-13 下午4:28:49 修改人：wanghh 描述 ：
	 * 
	 */
	public void stopBroadcast() {
		Intent intent = new Intent();
		intent.setAction(CommonMessage.TTS_CLEAR);
		// 发送 一个无序广播
		getActivity().sendBroadcast(intent);
	}

	/**
	 * 函数名称 : finishBroadcast 功能描述 : 初始化完成发送广播通知服务 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-8-13 下午1:50:21 修改人：wanghh 描述 ：
	 * 
	 */
	public void finishBroadcast() {
		Intent intent = new Intent();
		intent.setAction(CommonMessage.INIT_COMPLETE);
		// 发送 一个无序广播
		getActivity().sendBroadcast(intent);
	}
}

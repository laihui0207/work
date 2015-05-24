package com.tuyou.tsd.settings.init.layout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.tuyou.tsd.settings.R;
import com.tuyou.tsd.settings.base.SettingsService;

@SuppressLint("ValidFragment")
public class InitFinishFragment extends BaseFragment {
	private Button finishButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.layout_init_finish, container, false);
	}

	public InitFinishFragment(Activity activity) {
		super(activity);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	private void init() {
		View view = getView();
		finishButton = (Button) view.findViewById(R.id.btn_init_finish);
		finishButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 把系统是否初始化完成标记为完成/存入设备共享文件
				if (editor != null) {
					editor.putString("system_init", true + "");
					editor.putString("ssid",
							getResources().getString(R.string.wifi_ssid));
					editor.putString("ssid_psd",
							getResources().getString(R.string.wifi_psd));
					editor.commit();
				}
				// 通知服务保存fm状态
				getActivity().sendBroadcast(
						new Intent(SettingsService.SAVEFMACTION));
				stopBroadcast();
				finishBroadcast();
				// 退出程序
				getActivity().finish();
			}
		});
	}

}

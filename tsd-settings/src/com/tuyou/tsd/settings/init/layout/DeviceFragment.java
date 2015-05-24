package com.tuyou.tsd.settings.init.layout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tuyou.tsd.settings.R;

@SuppressLint("ValidFragment")
public class DeviceFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.layout_device_redress, container,
				false);
	}

	public DeviceFragment(Activity activity) {
		super(activity);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

}

package com.tuyou.tsd.settings.base;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.tuyou.tsd.settings.R;

public class WaitDialog extends Dialog {

	public WaitDialog(Context context, View view) {
		super(context, R.style.TransparentProgressDialog);
		WindowManager.LayoutParams wlmp = getWindow().getAttributes();
		wlmp.gravity = Gravity.CENTER_HORIZONTAL;
		getWindow().setAttributes(wlmp);
		setTitle(null);
		setCancelable(false);
		setOnCancelListener(null);
		setContentView(view);
	}

	public WaitDialog(Context context, View view, int style) {
		super(context, style);
		WindowManager.LayoutParams wlmp = getWindow().getAttributes();
		wlmp.gravity = Gravity.CENTER_HORIZONTAL;
		getWindow().setAttributes(wlmp);
		setTitle(null);
		setCancelable(false);
		setOnCancelListener(null);
		setContentView(view);
	}

	@Override
	public void show() {
		super.show();
	}
}
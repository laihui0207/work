package com.tuyou.tsd.cardvr;

import android.os.Bundle;
import android.view.Window;

public class BaseActivity extends com.tuyou.tsd.common.base.BaseActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

}

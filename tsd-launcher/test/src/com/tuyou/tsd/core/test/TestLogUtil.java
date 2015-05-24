package com.tuyou.tsd.core.test;

import android.test.AndroidTestCase;

import com.tuyou.tsd.common.util.LogUtil;

public class TestLogUtil extends AndroidTestCase {

	public void testLogWrite() {
		LogUtil.v("TestLogUtil", "log trace...");
		LogUtil.d("TestLogUtil", "log debug...");
		LogUtil.w("TestLogUtil", "log warn...");
		LogUtil.i("TestLogUtil", "log info...");
		LogUtil.e("TestLogUtil", "log error...");
	}
}

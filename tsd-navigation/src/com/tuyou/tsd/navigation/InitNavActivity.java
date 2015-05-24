package com.tuyou.tsd.navigation;

import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.tuyou.tsd.common.util.LogUtil;

import android.os.Bundle;
import android.os.Environment;

public class InitNavActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_initnav);
		initNav();
		finish();
	}

	public void initNav() {
		BaiduNaviManager.getInstance().initEngine(this, getSdcardDir(),
				mNaviEngineInitListener, new LBSAuthManagerListener() {
					@Override
					public void onAuthResult(int status, String msg) {
						String str = null;
						if (0 == status) {
							str = getResources().getString(
									R.string.route_key_success);
						} else {
							str = getResources().getString(
									R.string.route_key_fail)
									+ msg;
						}
						LogUtil.e("ACCESS_KEY", str);
					}
				});
	}

	private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {
		public void engineInitSuccess() {
		}

		public void engineInitStart() {
		}

		public void engineInitFail() {

		}
	};

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}
}

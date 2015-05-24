package com.tuyou.tsd.core.test;

import android.test.AndroidTestCase;

import com.tuyou.tsd.common.network.JsonOA;
import com.tuyou.tsd.common.network.LoginReq;
import com.tuyou.tsd.common.network.LoginRes;
import com.tuyou.tsd.common.network.SubmitDeviceStatusReq;
import com.tuyou.tsd.common.util.HelperUtil;

public class TestPostDeviceState extends AndroidTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		LoginReq req = new LoginReq();
		req.imei = "1234567890987654321";
		req.password = "1234567890";
		LoginRes result = JsonOA.getInstance(getContext()).login(req);

		assertNotNull(result);
		assertNotNull(result.accessToken);
		assertNotNull(result.device.id);
	}

	public void testPostDeviceState() {
		SubmitDeviceStatusReq req = new SubmitDeviceStatusReq();
		req.timestamp = HelperUtil.getCurrentTimestamp();
		req.state = "working";
//		req.modes = new String[] {"navigation", "music"};
		JsonOA.getInstance(getContext()).submitDeviceStatus(req);
	}

}

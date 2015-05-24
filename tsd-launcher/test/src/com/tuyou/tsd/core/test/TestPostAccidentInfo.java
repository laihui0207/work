package com.tuyou.tsd.core.test;

import android.test.AndroidTestCase;

import com.tuyou.tsd.common.network.JsonOA;
import com.tuyou.tsd.common.network.LoginReq;
import com.tuyou.tsd.common.network.LoginRes;
import com.tuyou.tsd.common.network.SubmitAccidentInfoReq;
import com.tuyou.tsd.common.util.HelperUtil;

public class TestPostAccidentInfo extends AndroidTestCase {

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
		SubmitAccidentInfoReq req = new SubmitAccidentInfoReq();
		req.timestamp = HelperUtil.getCurrentTimestamp();
		req.latitude = "32.111";
		req.longitude = "132.223";
		req.district = "上海市闸北区";
		req.address  = "和田路288弄1号";
		req.files = new String[] {"20140812T121145.mp4", "20140812T121345.mp4", "20140812T121545.mp4"};
		JsonOA.getInstance(getContext()).submitAccidentStatus(req);
	}

}

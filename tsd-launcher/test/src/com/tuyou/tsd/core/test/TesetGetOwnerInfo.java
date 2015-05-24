package com.tuyou.tsd.core.test;

import android.test.AndroidTestCase;

import com.tuyou.tsd.common.network.GetOwnerInfoRes;
import com.tuyou.tsd.common.network.JsonOA;
import com.tuyou.tsd.common.network.LoginReq;
import com.tuyou.tsd.common.network.LoginRes;

public class TesetGetOwnerInfo extends AndroidTestCase {

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

	public void testGetOwnerInfo() {
		GetOwnerInfoRes result = JsonOA.getInstance(getContext()).getOwnerInfo("");
		assertNotNull(result);
		assertNotNull(result.owners);

		for (int i = 0; i < result.owners.length; i++) {
			assertNotNull(result.owners[i].id);
			assertNotNull(result.owners[i].name);
			assertNotNull(result.owners[i].nickName);
			assertNotNull(result.owners[i].bindingTime);
//			assertNotNull(result.owners[i].email);
			assertNotNull(result.owners[i].gender);
			assertNotNull(result.owners[i].mobile);
		}
	}

}

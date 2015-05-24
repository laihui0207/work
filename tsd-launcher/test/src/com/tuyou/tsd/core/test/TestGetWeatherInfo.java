package com.tuyou.tsd.core.test;

import android.test.AndroidTestCase;

import com.tuyou.tsd.common.network.GetWeatherRes;
import com.tuyou.tsd.common.network.JsonOA;
import com.tuyou.tsd.common.network.LoginReq;
import com.tuyou.tsd.common.network.LoginRes;
import com.tuyou.tsd.common.util.HelperUtil;

public class TestGetWeatherInfo extends AndroidTestCase {

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

	public void testGetWeatherData() {
		GetWeatherRes weather = JsonOA.getInstance(getContext()).getWeatherInfo("上海市", HelperUtil.getCurrentTimestamp());

		assertNotNull(weather);
		assertEquals("上海", weather.weather.cityName);
		assertNotNull(weather.weather.lastUpdate);
		assertNotNull(weather.weather.text);
		assertNotNull(weather.weather.temperature);
		assertNotNull(weather.weather.suggestions);
	}
}

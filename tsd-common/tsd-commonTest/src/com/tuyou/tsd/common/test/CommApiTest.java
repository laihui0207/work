package com.tuyou.tsd.common.test;

import junit.framework.TestCase;

import com.tuyou.tsd.common.network.GetWeatherRes;
import com.tuyou.tsd.common.network.JsonOA;
import com.tuyou.tsd.common.network.LoginReq;
import com.tuyou.tsd.common.network.LoginRes;
import com.tuyou.tsd.common.util.HelperUtil;

public class CommApiTest extends TestCase {

	public void testLogin() {
		LoginReq req = new LoginReq();
		req.imei = "1234567890987654321";
		req.password = "1234567890";
		LoginRes result = JsonOA.login(req);

		assertNotNull(result);
		assertNotNull(result.accessToken);
		assertNotNull(result.device.id);
	}

//	public void testPostDeviceState() {
//		SubmitDeviceStatusReq req = new SubmitDeviceStatusReq();
//		req.timestamp = HelperUtil.getCurrentTimestamp();
//		req.state = "working";
//		req.modes = new String[] {"navigation", "music"};
//		JsonOA.submitDeviceStatus(req);
//	}

	public void testGetWeatherData() {
		GetWeatherRes weather = JsonOA.getWeatherInfo("上海市", HelperUtil.getCurrentTimestamp());

		assertNotNull(weather);
		assertEquals("上海", weather.weather.cityName);
		assertNotNull(weather.weather.lastUpdate);
		assertNotNull(weather.weather.text);
		assertNotNull(weather.weather.temperature);
		assertNotNull(weather.weather.suggestions);
	}
}

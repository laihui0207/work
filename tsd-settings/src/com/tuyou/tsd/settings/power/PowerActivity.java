package com.tuyou.tsd.settings.power;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.settings.R;
import com.tuyou.tsd.settings.base.BaseActivity;
import com.tuyou.tsd.settings.base.SysApplication;

public class PowerActivity extends BaseActivity implements OnClickListener {
	private Button dormancy1Button, dormancy2Button, dormancy3Button,
			dormancy4Button, dormancy5Button;
	private TextView back;
	private int idleTime = 3;
	private int idleBG = R.id.btn_power_off_3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_power);
		SysApplication.getInstance().addActivity(this);
		// 初始化
		init();
		// 监听
		registerListener();
	}

	private void registerListener() {
		dormancy1Button.setOnClickListener(this);
		dormancy2Button.setOnClickListener(this);
		dormancy3Button.setOnClickListener(this);
		dormancy4Button.setOnClickListener(this);
		dormancy5Button.setOnClickListener(this);
		back.setOnClickListener(this);
	}

	private void init() {
		initValue();
		dormancy1Button = (Button) findViewById(R.id.btn_power_off_1);
		dormancy2Button = (Button) findViewById(R.id.btn_power_off_2);
		dormancy3Button = (Button) findViewById(R.id.btn_power_off_3);
		dormancy4Button = (Button) findViewById(R.id.btn_power_off_4);
		dormancy5Button = (Button) findViewById(R.id.btn_power_off_5);
		back = (TextView) findViewById(R.id.btn_power_back);
		initOFF();
	}

	/**
	 * 函数名称 : initValue 功能描述 : 获取共享配置文件中的值 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-8-26 下午3:27:59 修改人：wanghh 描述 ：
	 * 
	 */
	public void initValue() {
		if (pref != null) {
			String timeType = pref.getString("screen_off_mode", "time");
			if (timeType.equals("time")) {
				idleTime = (Integer.parseInt(pref.getString("screen_off_value",
						5 * 60 + ""))) / 60;
				switch (idleTime) {
				case 1:
					idleBG = R.id.btn_power_off_1;
					break;
				case 2:
					idleBG = R.id.btn_power_off_2;
					break;
				case 3:
					idleBG = R.id.btn_power_off_3;
					break;
				case 4:
					idleBG = R.id.btn_power_off_4;
					break;
				case 5:
					idleBG = R.id.btn_power_off_5;
					break;
				default:
					break;
				}
			}
		}

	}

	/**
	 * 函数名称 : initOFF 功能描述 : 初始化休眠设置 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-8-21 下午2:02:15 修改人：wanghh 描述 ：
	 * 
	 */
	public void initOFF() {
		idleClick(idleBG);
		if (pref != null) {
			editor.putString("screen_off_value", idleTime * 60 + "");
			editor.commit();
		}
		dormancy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_power_off_1:
			idleTime = 1;
			idleBG = R.id.btn_power_off_1;
			initOFF();
			break;
		case R.id.btn_power_off_2:
			idleTime = 2;
			idleBG = R.id.btn_power_off_2;
			initOFF();
			break;
		case R.id.btn_power_off_3:
			idleTime = 3;
			idleBG = R.id.btn_power_off_3;
			initOFF();
			break;
		case R.id.btn_power_off_4:
			idleTime = 4;
			idleBG = R.id.btn_power_off_4;
			initOFF();
			break;
		case R.id.btn_power_off_5:
			idleTime = 5;
			idleBG = R.id.btn_power_off_5;
			initOFF();
			break;
		case R.id.btn_power_back:
			finish();
			break;
		default:
			break;
		}
	}

	public void dormancy() {
		Intent intent = new Intent(TSDEvent.System.IDLE_INTERVAL_TIME_UPDATED);
		sendBroadcast(intent);
		LogUtil.d("发广播", intent.toString());
	}

	/**
	 * 函数名称 : idleClick 功能描述 : 设置休眠时间按钮操作 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-8-18 上午9:49:23 修改人：wanghh 描述 ：
	 * 
	 */
	public void idleClick(int button) {
		dormancy1Button.setBackgroundResource(R.drawable.bg_off_sceen_item);
		dormancy2Button.setBackgroundResource(R.drawable.bg_off_sceen_item);
		dormancy3Button.setBackgroundResource(R.drawable.bg_off_sceen_item);
		dormancy4Button.setBackgroundResource(R.drawable.bg_off_sceen_item);
		dormancy5Button.setBackgroundResource(R.drawable.bg_off_sceen_item);
		switch (button) {
		case R.id.btn_power_off_1:
			dormancy1Button.setBackgroundResource(R.drawable.bg_off_sceen_1);
			break;
		case R.id.btn_power_off_2:
			dormancy2Button.setBackgroundResource(R.drawable.bg_off_sceen_2);
			break;
		case R.id.btn_power_off_3:
			dormancy3Button.setBackgroundResource(R.drawable.bg_off_sceen_2);
			break;
		case R.id.btn_power_off_4:
			dormancy4Button.setBackgroundResource(R.drawable.bg_off_sceen_2);
			break;
		case R.id.btn_power_off_5:
			dormancy5Button.setBackgroundResource(R.drawable.bg_off_sceen_3);
			break;
		default:
			break;
		}
	}

	/**
	 * 函数名称 : alterColor 功能描述 : 修改textview的部分字体颜色 参数及返回值说明：
	 * 
	 * @param textView
	 * 
	 *            修改记录： 日期：2014-8-21 下午2:57:47 修改人：wanghh 描述 ：
	 * 
	 */
	public void alterColor(TextView textView) {
		SpannableStringBuilder builder = new SpannableStringBuilder(textView
				.getText().toString());

		// ForegroundColorSpan 为文字前景色，BackgroundColorSpan为文字背景色
		ForegroundColorSpan orangeSpan = new ForegroundColorSpan(getResources()
				.getColor(R.drawable.orange));
		ForegroundColorSpan orangeSpan1 = new ForegroundColorSpan(
				getResources().getColor(R.drawable.orange));

		builder.setSpan(orangeSpan, 23, 24, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		builder.setSpan(orangeSpan1, 39, 41, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		textView.setText(builder);
	}
}

package com.tuyou.tsd.settings.information;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tuyou.tsd.common.network.GetOwnerInfoRes;
import com.tuyou.tsd.common.network.JsonOA2;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.MyAsyncTask;
import com.tuyou.tsd.settings.R;
import com.tuyou.tsd.settings.base.BaseActivity;
import com.tuyou.tsd.settings.base.SysApplication;
import com.tuyou.tsd.settings.base.WaitDialog;

public class InformationActivity extends BaseActivity implements
		OnClickListener {
	// 返回和二维码图片
	private TextView back;
	private TextView nameTextView, sexTextView, phoneTextView;
	// private TextView carTextView;
	private String strName = "", strSex = "", strPhone = "";
	// private String strCar = "";
	private ImageView codeImageView;
	private LinearLayout infoLayout, noInfoLayout;
	private String imei;
	private int codeWH = 140;
	private WaitDialog dialog;
	private boolean isRequrest = false;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (dialog != null && isRequrest) {
				if (dialog != null) {
					dialog.dismiss();
				}
				Toast.makeText(InformationActivity.this, "连接超时",
						Toast.LENGTH_LONG).show();
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		SysApplication.getInstance().addActivity(this);
		initView();
	}

	private void initView() {
		back = (TextView) findViewById(R.id.btn_info_back);
		nameTextView = (TextView) findViewById(R.id.txt_info_name);
		sexTextView = (TextView) findViewById(R.id.txt_info_sex);
		phoneTextView = (TextView) findViewById(R.id.txt_info_phone);
		// carTextView = (TextView) findViewById(R.id.txt_info_car);
		codeImageView = (ImageView) findViewById(R.id.img_info_code);
		imei = HelperUtil.getDeviceId(InformationActivity.this);
		codeImageView
				.setImageBitmap(HelperUtil.create2DCode(getResources()
						.getString(R.string.app_url) + "?IMEI=" + imei, codeWH,
						codeWH));
		infoLayout = (LinearLayout) findViewById(R.id.layout_info);
		noInfoLayout = (LinearLayout) findViewById(R.id.layout_info_no);
		infoLayout.setVisibility(View.VISIBLE);
		noInfoLayout.setVisibility(View.GONE);
		back.setOnClickListener(this);
		waitDialog();
		isRequrest = true;
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				handler.sendEmptyMessage(1);
			}
		}, 10000);
		GetOwnerInfoTask getOwnerInfoTask = new GetOwnerInfoTask();
		getOwnerInfoTask.execute("true");
		// initVaule();
	}

	/**
	 * 函数名称 : initVaule 功能描述 : 给用户信息赋值 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-8-22 上午11:24:27 修改人：wanghh 描述 ：
	 * 
	 */
	public void initVaule() {
		if (strName.length() > 0 || strSex.length() > 0
				|| strPhone.length() > 0) {
			nameTextView.setText(strName);
			sexTextView.setText(strSex);
			phoneTextView.setText(strPhone);
			// carTextView.setText(strCar);
			infoLayout.setVisibility(View.VISIBLE);
			noInfoLayout.setVisibility(View.GONE);
		} else {
			infoLayout.setVisibility(View.GONE);
			noInfoLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_info_back:
			finish();
			break;

		default:
			break;
		}
	}

	public class GetOwnerInfoTask extends
			MyAsyncTask<String, Void, GetOwnerInfoRes> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(final GetOwnerInfoRes result) {
			isRequrest = false;
			if (dialog != null) {
				dialog.dismiss();
			}
			String name = null, phone = null, sex = null;
			if (result != null) {
				switch (result.errorCode) {
				case 0:
					if (result.owners != null) {
						for (int i = 0; i < result.owners.length; i++) {
							name = result.owners[0].nickName;
							phone = result.owners[0].mobile;
							sex = result.owners[0].gender;
						}
						if (name != null) {
							strName = name;
						}
						if (phone != null) {
							strPhone = phone;
						}
						if (sex != null) {
							if (sex.equals("male")) {
								strSex = "男";
							} else {
								strSex = "女";
							}
						}
					}
					// if (result.device != null) {
					// strCar = result.device.car.plateNumber;
					// }
					initVaule();
					break;
				case -1:
					Toast.makeText(InformationActivity.this, "网络连接断开请连接网络",
							Toast.LENGTH_LONG).show();
					break;
				default:
					break;
				}
			}
		}

		@Override
		protected GetOwnerInfoRes doInBackground(String... arg0) {
			return JsonOA2.getInstance(InformationActivity.this).getOwnerInfo(
					arg0[0]);
		}
	}

	/**
	 * 等待对话框
	 */
	public void waitDialog() {
		LayoutInflater inflater = InformationActivity.this.getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_wait, null);
		TextView contentTextView = (TextView) layout
				.findViewById(R.id.txt_dialog_content);
		contentTextView.setText(getResources().getString(
				R.string.txt_dialog_wait));
		layout.findViewById(R.id.img_dialog_off).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (dialog != null) {
							dialog.dismiss();
							dialog = null;
						}
					}
				});

		dialog = new WaitDialog(InformationActivity.this, layout);
		dialog.show();
	}

}

package com.tuyou.tsd.settings.init;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDComponent;
import com.tuyou.tsd.common.TSDShare;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.settings.R;
import com.tuyou.tsd.settings.base.WaitDialog;
import com.tuyou.tsd.settings.init.FragmentViewPagerAdapter.OnExtraPageChangeListener;
import com.tuyou.tsd.settings.init.layout.FMFragment;

public class InitMainActivity extends FragmentActivity implements
		ViewPager.OnPageChangeListener, OnClickListener {
	public static boolean isInitFinish = false;
	private WaitDialog popdialog;
	public SharedPreferences pref;
	public Editor editor;
	public static ViewPager viewPager;
	public static InitMainActivity activity;
	private OnExtraPageChangeListener onExtraPageChangeListener; // ViewPager切换页面时的额外功能添加接口
	private int currentPageIndex = 0; // 当前page索引（切换之前）
	private boolean isFM = false, isCamera = false;
	private ImageView imageView;
	/** 将小圆点的图片用数组表示 */
	private ImageView[] imageViews;
	private int pages = 3;
	// 包裹小圆点的LinearLayout
	private LinearLayout mViewPoints;
	private RelativeLayout navLayout;
	private Button topButton, nextButton;
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
		}
	};

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				dialogFM();
				break;
			case 1:
				dialogCamera();
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
		setContentView(R.layout.activity_initconfigure);
		// 动态注册接收语音播报结束广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonMessage.TTS_PLAY_FINISHED);
		registerReceiver(broadcastReceiver, filter);
		// 通过该Context获得所需的SharedPreference实例
		init();
	}

	public void init() {
		navLayout = (RelativeLayout) findViewById(R.id.layout_init_nav);
		viewPager = (ViewPager) findViewById(R.id.vpg_initconfigure);
		topButton = (Button) findViewById(R.id.btn_init_top);
		topButton.setVisibility(View.GONE);
		nextButton = (Button) findViewById(R.id.btn_init_next);
		topButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		activity = InitMainActivity.this;
		pref = HelperUtil.getCommonPreference(activity,
				TSDComponent.CORE_SERVICE_PACKAGE,
				TSDShare.SYSTEM_SETTING_PREFERENCES);
		if (pref != null) {
			editor = pref.edit();
		}
		FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(
				this.getSupportFragmentManager());
		adapter.setOnExtraPageChangeListener(new FragmentViewPagerAdapter.OnExtraPageChangeListener() {
			@Override
			public void onExtraPageSelected(int i) {
				System.out.println("Extra...i: " + i);
			}
		});
		// 创建imageviews数组，大小是要显示的图片的数量
		imageViews = new ImageView[pages];
		// 实例化小圆点的linearLayout和viewpager
		mViewPoints = (LinearLayout) findViewById(R.id.layout_group);
		// 添加小圆点的图片
		for (int i = 0; i < pages; i++) {
			imageView = new ImageView(InitMainActivity.this);
			// 设置小圆点imageview的参数
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					10, 10);
			layoutParams.setMargins(15, 0, 15, 0);
			imageView.setLayoutParams(layoutParams);// 创建一个宽高均为20 的布局
			// 将小圆点layout添加到数组中
			imageViews[i] = imageView;
			// 默认选中的是第一张图片，此时第一个小圆点是选中状态，其他不是
			if (i == 0) {
				imageViews[i]
						.setBackgroundResource(R.drawable.navigate_pressed);
			} else {
				imageViews[i].setBackgroundResource(R.drawable.navigate_normal);
			}

			// 将imageviews添加到小圆点视图组
			mViewPoints.addView(imageViews[i]);
		}
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(this);
		// 此操作放到初始化里面只执行一次
		if (!isFM) {
			handler.sendEmptyMessage(0);
			playBroadcast(getResources().getString(R.string.txt_init_tts_2), 1);
		}
	}

	@Override
	public void onPageScrollStateChanged(int i) {
		if (null != onExtraPageChangeListener) { // 如果设置了额外功能接口
			onExtraPageChangeListener.onExtraPageScrollStateChanged(i);
		}
	}

	@Override
	public void onPageScrolled(int i, float v, int i2) {
		if (null != onExtraPageChangeListener) { // 如果设置了额外功能接口
			onExtraPageChangeListener.onExtraPageScrolled(i, v, i2);
		}
	}

	@Override
	public void onPageSelected(int position) {
		currentPageIndex = position;
		stopBroadcast();
		switch (position) {
		case 0:
			isFinish(false);
			topButton.setVisibility(View.GONE);
			break;
		case 1:
			isFinish(false);
			FMFragment fm = (FMFragment) getSupportFragmentManager()
					.findFragmentByTag(
							"android:switcher:" + R.id.vpg_initconfigure + ":0");
			fm.switchFragment();
			if (!isCamera) {
				handler.sendEmptyMessage(1);
				playBroadcast(
						getResources().getString(R.string.txt_init_tts_4), 1);
			}
			break;
		case 2:
			isFinish(true);
			break;
		default:
			break;
		}
		for (int i = 0; i < imageViews.length; i++) {
			imageViews[position]
					.setBackgroundResource(R.drawable.navigate_pressed);
			// 不是当前选中的page，其小圆点设置为未选中的状态
			if (position != i) {
				imageViews[i].setBackgroundResource(R.drawable.navigate_normal);
			}
		}

		if (null != onExtraPageChangeListener) { // 如果设置了额外功能接口
			onExtraPageChangeListener.onExtraPageSelected(position);
		}

	}

	private void isFinish(boolean isfinish) {
		if (isfinish) {
			topButton.setVisibility(View.VISIBLE);
			nextButton.setVisibility(View.GONE);
			navLayout.setVisibility(View.GONE);
		} else {
			topButton.setVisibility(View.VISIBLE);
			navLayout.setVisibility(View.VISIBLE);
			nextButton.setVisibility(View.VISIBLE);
		}
	}

	public OnExtraPageChangeListener getOnExtraPageChangeListener() {
		return onExtraPageChangeListener;
	}

	/**
	 * 设置页面切换额外功能监听器
	 * 
	 * @param onExtraPageChangeListener
	 */
	public void setOnExtraPageChangeListener(
			OnExtraPageChangeListener onExtraPageChangeListener) {
		this.onExtraPageChangeListener = onExtraPageChangeListener;
	}

	/**
	 * 函数名称 : finishBroadcast 功能描述 : 初始化完成发送广播通知服务 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-8-13 下午1:50:21 修改人：wanghh 描述 ：
	 * 
	 */
	public void finishBroadcast() {
		Intent intent = new Intent();
		intent.setAction(CommonMessage.INIT_COMPLETE);
		// 发送 一个无序广播
		sendBroadcast(intent);
	}

	/**
	 * 函数名称 : playBroadcast 功能描述 : 开始播报语音广播 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-8-13 下午1:57:48 修改人：wanghh 描述 ：
	 * 
	 */
	public void playBroadcast(String content, int pid) {
		Intent intent = new Intent();
		intent.setAction(CommonMessage.TTS_PLAY);
		// 要发送的内容
		intent.putExtra("package", "com.tuyou.tsd.tts");
		intent.putExtra("id", pid);
		intent.putExtra("content", content);
		// 发送 一个无序广播
		sendBroadcast(intent);
	}

	/**
	 * 函数名称 : stopBroadcast 功能描述 : 主动停止语音播报广播 参数及返回值说明：
	 * 
	 * 修改记录： 日期：2014-8-13 下午4:28:49 修改人：wanghh 描述 ：
	 * 
	 */
	public void stopBroadcast() {
		Intent intent = new Intent();
		intent.setAction(CommonMessage.TTS_CLEAR);
		// 发送 一个无序广播
		sendBroadcast(intent);
	}

	@Override
	protected void onDestroy() {
		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
		}
		super.onDestroy();
	}

	public void dialogFM() {
		// 获取LayoutInflater实例
		LayoutInflater inflater = (LayoutInflater) InitMainActivity.this
				.getSystemService(InitMainActivity.this.LAYOUT_INFLATER_SERVICE);
		// 获取弹出菜单的布局
		View view = inflater.inflate(R.layout.layout_fm_guide_1, null);
		Button button = (Button) view
				.findViewById(R.id.btn_layout_fm_guide_1_ok);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopBroadcast();
				playBroadcast(
						getResources().getString(R.string.txt_init_tts_3), 2);
				isFM = true;
				popdialog.dismiss();
			}
		});

		popdialog = new WaitDialog(activity, view, R.style.Dialog_Fullscreen);
		popdialog.show();
	}

	public void dialogCamera() {
		// 获取LayoutInflater实例
		LayoutInflater inflater = (LayoutInflater) InitMainActivity.this
				.getSystemService(InitMainActivity.this.LAYOUT_INFLATER_SERVICE);
		// 获取弹出菜单的布局
		View view = inflater.inflate(R.layout.layout_device_redress, null);
		Button button = (Button) view
				.findViewById(R.id.btn_init_camera_preview_look);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopBroadcast();
				isCamera = true;
				popdialog.dismiss();
			}
		});

		popdialog = new WaitDialog(activity, view, R.style.Dialog_Fullscreen);
		popdialog.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_init_top:
			if (currentPageIndex - 1 >= 0) {
				if (currentPageIndex != 1) {
					viewPager.setCurrentItem(currentPageIndex - 1);
				} else {
					if (isCamera) {
						viewPager.setCurrentItem(currentPageIndex - 1);
					}
				}
			}
			break;
		case R.id.btn_init_next:
			if (currentPageIndex + 1 <= 2) {
				if (currentPageIndex != 1) {
					viewPager.setCurrentItem(currentPageIndex + 1);
				} else {
					if (isCamera) {
						viewPager.setCurrentItem(currentPageIndex + 1);
					}
				}
			}
			break;
		case R.id.btn_init_finish:
			// 把系统是否初始化完成标记为完成/存入设备共享文件
			/*if (editor != null) {
				editor.putString("system_init", true + "");
				editor.commit();
			}*/
			SharedPreferences pref = HelperUtil.getCommonPreference(this,
					TSDComponent.CORE_SERVICE_PACKAGE,
					TSDShare.SYSTEM_SETTING_PREFERENCES);
			Log.v("fq","SYSTEM_SETTING_PREFERENCES ");
			if(pref != null){
				Log.v("fq","SYSTEM_SETTING_PREFERENCES system_init true");
				pref.edit().putString("system_init", "true").commit();
			}
			isInitFinish = true;
			stopBroadcast();
			finishBroadcast();
			// 退出程序
			activity.finish();
			break;
		default:
			break;
		}
	}
}

package com.tuyou.tsd.settings.init;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.tuyou.tsd.settings.init.layout.CPreviewFragment;
import com.tuyou.tsd.settings.init.layout.DeviceFragment;
import com.tuyou.tsd.settings.init.layout.InitFinishFragment;
import com.tuyou.tsd.settings.init.layout.FMFragment;

/**
 * 为ViewPager添加布局（Fragment），绑定和处理fragments和viewpager之间的逻辑关系
 * 
 * Created with IntelliJ IDEA. Author: wangjie email:tiantian.china.2@gmail.com
 * Date: 13-10-11 Time: 下午3:03
 */
public class FragmentViewPagerAdapter extends FragmentPagerAdapter {
	public InitMainActivity activity;

	public FragmentViewPagerAdapter(FragmentManager fm) {
		super(fm);
		activity = InitMainActivity.activity;
	}

	private OnExtraPageChangeListener onExtraPageChangeListener; // ViewPager切换页面时的额外功能添加接口

	@Override
	public int getCount() {
		return 3;
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
	 * page切换额外功能接口
	 */
	static class OnExtraPageChangeListener {
		public void onExtraPageScrolled(int i, float v, int i2) {
		}

		public void onExtraPageSelected(int i) {
		}

		public void onExtraPageScrollStateChanged(int i) {
		}
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case 0:
			return new FMFragment(activity);
		case 1:
			return new CPreviewFragment(activity);
		case 2:
			return new InitFinishFragment(activity);
		default:
			return null;
		}
	}

}

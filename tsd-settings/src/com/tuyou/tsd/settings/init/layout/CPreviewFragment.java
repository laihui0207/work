package com.tuyou.tsd.settings.init.layout;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.settings.R;
import com.tuyou.tsd.settings.init.InitMainActivity;

@SuppressLint("ValidFragment")
public class CPreviewFragment extends BaseFragment implements
		SurfaceTextureListener {

	public Camera mCamera;
	private TextureView mTextureView;
	private final String TAG = "CPreviewFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.layout_came_preview, container, false);
	}

	public CPreviewFragment(Activity activity) {
		super(activity);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	private void init() {
		// 不允许设备进入休眠
		getActivity().sendBroadcast(
				new Intent(TSDEvent.System.DISABLE_IDLE_CHECK));
		mCamera = getCameraInstance();
		View view = getView();
		// 创建预览类，并与Camera关联，最后添加到界面布局中
		mTextureView = (TextureView) view
				.findViewById(R.id.textureView_layout_came_preview_layout);
		mTextureView.setSurfaceTextureListener(this);
	}

	/**
	 * 打开一个Camera
	 * 
	 * @return
	 * @author ZL
	 * @date 2014-7-22 上午10:42:28
	 */
	private Camera getCameraInstance() {
		Camera c = null;
		try {
			LogUtil.d("initConfigure", "打开Camera");
			c = Camera.open();
		} catch (Exception e) {
			Log.d("initConfigure", "打开Camera失败失败");
			e.printStackTrace();
		}
		return c;
	}

	public void recover() {
		System.out.println("释放摄像头");
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
		try {
			if (mCamera == null) {
				mCamera = getCameraInstance();
			}
			if (mCamera != null) {
				mCamera.setPreviewTexture(surface);
				mCamera.startPreview();
			} else {
				LogUtil.d(TAG, "摄像头打开失败，无法获取界面");
				Toast.makeText(getActivity(), "摄像头打开失败，无法获取界面",
						Toast.LENGTH_LONG).show();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		if (mCamera != null) {
			// mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		return true;
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
	}

	public void onDestroyView() {
		// 允许设备进入休眠
		getActivity().sendBroadcast(
				new Intent(TSDEvent.System.ENABLE_IDLE_CHECK));
		recover();
		if (InitMainActivity.isInitFinish) {
			LogUtil.d("CPreviewFragment", "摄像头释放初始化结束");
			// 等摄像头释放了再发初始化完成的广播防止摄像头未释放倒置行车记录崩溃
			finishBroadcast();
		}
		super.onDestroyView();
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
		getActivity().sendBroadcast(intent);
	}
}

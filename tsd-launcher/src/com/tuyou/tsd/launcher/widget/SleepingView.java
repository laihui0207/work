package com.tuyou.tsd.launcher.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tuyou.tsd.R;

public class SleepingView extends RelativeLayout {
	private ImageView mHead, mMark;
	private AnimationTask mAnimTask = null;

	public SleepingView(Context context) {
		super(context);
		initView(context);
	}

	public SleepingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SleepingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	private void initView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.sleeping_layout, null, false);
		mHead = (ImageView) v.findViewById(R.id.sleeping_face_imgView);
		mMark = (ImageView) v.findViewById(R.id.sleeping_mark_imgView);
		addView(v);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mAnimTask == null) {
			mAnimTask = new AnimationTask();
			mAnimTask.start();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mAnimTask != null) {
			mAnimTask.stop = true;
			mAnimTask = null;
		}
	}

	private static enum SleepingState {
		STATE_0,
		STATE_1,
		STATE_2,
		STATE_3;

		SleepingState next() {
			SleepingState[] vals = SleepingState.values();
			int next = (this.ordinal() + 1) % vals.length;
//			LogUtil.d(VIEW_LOG_TAG, "next = " + vals[next]);
			return vals[next];
		}
	};

	private Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 100) {
				SleepingState state = (SleepingState) msg.obj;
				switch (state) {
				case STATE_0:
					mMark.setVisibility(View.GONE);
					break;
				case STATE_1:
					mMark.setVisibility(View.VISIBLE);
					mMark.setImageResource(R.drawable.sleeping_mark_0);
					break;
				case STATE_2:
					mMark.setImageResource(R.drawable.sleeping_mark_1);
					break;
				case STATE_3:
					mMark.setImageResource(R.drawable.sleeping_mark_2);
					break;
				}
			}
		}
		
	};

	private class AnimationTask extends Thread {
		private boolean stop;
		private SleepingState state = SleepingState.STATE_0;

		@Override
		public void run() {
			Looper.getMainLooper();
			while (!stop) {
				Message msg = Message.obtain(null, 100, state);
				mHandler.sendMessage(msg);
				state = state.next();
				try { Thread.sleep(1000); } catch (InterruptedException e) {}
			}
		}
		
	}
}

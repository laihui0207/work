package com.tuyou.tsd.voice.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tuyou.tsd.voice.R;

public class MicrophoneView extends RelativeLayout {
	private ImageButton mBtn;
	private ImageView mEffect;
	private Animation mAnim;
	private Context context;

	public MicrophoneView(Context context) {
		super(context);
		this.context = context;
		initView(context);
	}

	public MicrophoneView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		this.context = context;
	}

	public MicrophoneView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		initView(context);
	}

	private void initView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.mic_layout, null, false);
		mBtn = (ImageButton) v.findViewById(R.id.mic_btn);
		mEffect = (ImageView) v.findViewById(R.id.mic_effect_bg);
		mEffect.setVisibility(View.INVISIBLE);
		mAnim = AnimationUtils.loadAnimation(context, R.anim.alpha_anim);
		addView(v);
	}

	public void setOnClickListener(View.OnClickListener listener) {
		if (mBtn != null) {
			mBtn.setOnClickListener(listener);
		}
	}

	public void showAnimation() {
		mEffect.setVisibility(View.VISIBLE);
		mEffect.startAnimation(mAnim);
	}

	public void hideAnimation() {
		mEffect.clearAnimation();
		mEffect.setVisibility(View.INVISIBLE);
	}
}

package com.tuyou.tsd.launcher.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tuyou.tsd.R;

public class ImageTextView extends RelativeLayout {
	private View mView;
	private TextView mTextView;

	public ImageTextView(Context context) {
		super(context);
		initView(context);
	}

	public ImageTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImageTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.ImageTextView,
				0, 0);
		try {
			Drawable d = a.getDrawable(R.styleable.ImageTextView_backImg);
			if (d != null) {
				mView.setBackgroundDrawable(d);
			}

			String text = a.getString(R.styleable.ImageTextView_content);
			if (text != null) {
				mTextView.setText(text);
			}
			int left = a.getDimensionPixelOffset(R.styleable.ImageTextView_content_marginLeft, 0);
			int top  = a.getDimensionPixelOffset(R.styleable.ImageTextView_content_marginTop, 0);
			int right = a.getDimensionPixelOffset(R.styleable.ImageTextView_content_marginRight, 0);
			int bottom = a.getDimensionPixelOffset(R.styleable.ImageTextView_content_marginBottom, 0);
			mTextView.setPadding(left, top, right, bottom);
		} finally {
			a.recycle();
		}
	}

	private void initView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.image_text_layout, null, false);
		mTextView = (TextView) mView.findViewById(R.id.imgtxt_textView);
		addView(mView);
	}

	public void setBackground(int resid) {
		if (mView != null) {
			mView.setBackgroundResource(resid);
		}
	}

	public void setText(String text) {
		if (mTextView != null) {
			mTextView.setText(text);
		}
	}

	public TextView getTextView() {
		return mTextView;
	}
}

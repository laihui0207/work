package com.tuyou.tsd.launcher.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tuyou.tsd.R;
import com.tuyou.tsd.common.network.GetWeatherRes;
import com.tuyou.tsd.common.network.GetWeatherRes.Weather.Suggestion;

public class WeatherView extends LinearLayout {
//	private static final String TAG = "WeatherView";

	private Context mContext;
	private TextView mTempView, mDateView;
	private ImageTextView mSugestView;
	private ImageView mImgView;

	public WeatherView(Context context) {
		super(context);
		initView(context);
	}

	public WeatherView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public WeatherView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	private void initView(Context context) {
		mContext = context;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.weather_layout, null, false);
		mTempView = (TextView) v.findViewById(R.id.temperature_text);
		mDateView = (TextView) v.findViewById(R.id.date_text);
		mSugestView = (ImageTextView) v.findViewById(R.id.suggestion_view);
		mImgView = (ImageView) v.findViewById(R.id.weather_img);

		addView(v);
	}

	public void setWeatherData(GetWeatherRes data) {
		if (data != null && data.errorCode == 0) {
			String temperature = data.weather.temperature;
			if (mTempView != null) {
				mTempView.setText(temperature);
			}

			TypedArray icons = mContext.getResources().obtainTypedArray(R.array.weather_icons_array);
			if (mImgView != null) {
				Drawable d = icons.getDrawable(Integer.valueOf(data.weather.code));
				mImgView.setImageDrawable(d);
			}
			icons.recycle();

			Suggestion[] suggestions = data.weather.suggestions;
			if (suggestions != null) {
				for (Suggestion s : suggestions) {
					if (s.name.equals("car_washing")) {
						mSugestView.setText(s.brief + "洗车");
						if (s.brief.equals("非常适宜") || s.brief.equals("适宜") || s.brief.equals("比较适宜")) {
							mSugestView.setVisibility(View.VISIBLE);
							mSugestView.setBackground(R.drawable.recommend_bg);
						} else if (s.brief.equals("不太适宜") || s.brief.equals("不适宜")){
							mSugestView.setVisibility(View.VISIBLE);
							mSugestView.setBackground(R.drawable.not_recommend_bg);							
						} else {
							mSugestView.setVisibility(View.GONE);
						}
						break;
					}
				}
			}
		}
	}

	public void setDate(String date) {
		if (mDateView != null) {
			mDateView.setText(date);
		}
	}

}

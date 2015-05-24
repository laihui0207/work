package com.tuyou.tsd.voice;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tuyou.tsd.common.TSDEvent;

public class ErrorFragment extends Fragment {
	private Activity mParentActivity;
	private ImageButton mCloseBtn;
	private TextView mMsgView;
	private String mErrorText;

	@Override
	public void onAttach(Activity activity) {
		mParentActivity = activity;
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.error_fragment, container, false);

		mMsgView = (TextView) view.findViewById(R.id.error_txtView);
		mMsgView.setText(mErrorText);

		mCloseBtn = (ImageButton) view.findViewById(R.id.error_close_btn);
		mCloseBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mParentActivity.sendBroadcast(new Intent(TSDEvent.Interaction.CANCEL_INTERACTION_BY_TP));
			}
		});
		return view;
	}

	void setErrorText(String error) {
		mErrorText = error;
	}
}

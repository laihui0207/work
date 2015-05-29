package com.tuyou.tsd.voice;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDComponent;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;
import com.tuyou.tsd.voice.widget.MicrophoneView;

public class RecordFragment extends Fragment {
	private Activity mParentActivity;
	private MicrophoneView mMicButton;
	private ImageButton mCloseButton;
	private Button mHomeButton;
	private boolean mShowAnimation;
	private final String TAG = "RecordFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.record_fragment, container, false);
		LogUtil.d(TAG, "onCreateView");
		mMicButton = (MicrophoneView) view.findViewById(R.id.record_mic_View);	
		mMicButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setBtnClickable(false);
				if (mParentActivity != null) {
					((InteractingActivity)mParentActivity).sendMessageToService(CommonMessage.VoiceEngine.STOP_RECOGNITION, null);
				}
			}
		});

		mCloseButton = (ImageButton) view.findViewById(R.id.record_close_btn);
		mCloseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setBtnClickable(false);
				if (mParentActivity != null) {
					((InteractingActivity)mParentActivity).sendMessageToService(
							CommonMessage.VoiceEngine.CANCEL_RECOGNITION, null);
				}
				mParentActivity.sendBroadcast(new Intent(TSDEvent.Interaction.CANCEL_INTERACTION_BY_TP));
			}
		});

		mHomeButton = (Button) view.findViewById(R.id.record_home_btn);
		mHomeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setBtnClickable(false);
				if (mParentActivity != null) {
					((InteractingActivity)mParentActivity).sendMessageToService(
							CommonMessage.VoiceEngine.CANCEL_RECOGNITION, null);
				}
				mParentActivity.sendBroadcast(new Intent(TSDEvent.Interaction.CANCEL_INTERACTION_BY_TP)
													.putExtra("gohome", true));
				HelperUtil.startActivityWithFadeInAnim(getActivity(), TSDComponent.LAUNCHER_PACKAGE, TSDComponent.HOME_ACTIVITY);
			}
		});

		setBtnClickable(false);
		LogUtil.d(TAG, "onCreateView done!!");
		return view;
	}

	public void setBtnClickable(boolean bClickable){
		if(mMicButton!=null){
			mMicButton.setClickable(bClickable);
		}
		if(mCloseButton!=null){
			mCloseButton.setClickable(bClickable);
		}
		if(mHomeButton!=null){
			mHomeButton.setClickable(bClickable);
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		mParentActivity = activity;
		super.onAttach(activity);
	}

	@Override
	public void onPause() {
		mMicButton.hideAnimation();
		super.onPause();
	}

	@Override
	public void onResume() {
		if (mShowAnimation) {
			mMicButton.showAnimation();
		}
		super.onResume();
	}

	void startAnimation() {
		mShowAnimation = true;
		if (mMicButton != null) {
			mMicButton.showAnimation();
		}
	}

	void stopAnimation() {
		mShowAnimation = false;
		if (mMicButton != null) {
			mMicButton.hideAnimation();
		}
	}
}

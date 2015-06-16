package com.tuyou.tsd.voice;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tuyou.tsd.common.CommonMessage;
import com.tuyou.tsd.common.TSDComponent;
import com.tuyou.tsd.common.TSDEvent;
import com.tuyou.tsd.common.TSDLocation;
import com.tuyou.tsd.common.util.HelperUtil;
import com.tuyou.tsd.common.util.LogUtil;

public class RecordFragment extends Fragment {
	private Activity mParentActivity;
	private ImageView mCloseButton;
	private ImageView mHomeButton;
	private ImageView mVoiceMask;
	private ImageView mIcallButton;
	private ImageView mText;
	private RelativeLayout mBottomLayout = null;
	private final String TAG = "RecordFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.record_fragment, container, false);
		LogUtil.d(TAG, "onCreateView");

		mBottomLayout = (RelativeLayout)view.findViewById(R.id.record_bottom_view);
		mBottomLayout.setVisibility(View.VISIBLE);
		
		mCloseButton = (ImageView) view.findViewById(R.id.record_close_btn);
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

		mHomeButton = (ImageView) view.findViewById(R.id.record_home_btn);
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

		mVoiceMask = (ImageView) view.findViewById(R.id.img_voice_mask);
		
		mIcallButton = (ImageView) view.findViewById(R.id.record_icall_btn);
		mIcallButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setBtnClickable(false);
				if (mParentActivity != null) {
					((InteractingActivity)mParentActivity).sendMessageToService(
							CommonMessage.VoiceEngine.CANCEL_RECOGNITION, null);
				}
				mParentActivity.sendBroadcast(new Intent(TSDEvent.Interaction.CANCEL_INTERACTION_BY_TP));	
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:4008936008"));
				startActivity(intent);
			}
		});
		
		mText = (ImageView) view.findViewById(R.id.record_text);
		
		setBtnClickable(true);
		LogUtil.d(TAG, "onCreateView done!!");
		return view;
	}
	
	public void setListenView(){
		if(mText!=null){
			mText.setBackgroundResource(R.drawable.text_destination);
		}
		if(mHomeButton!=null){
			mHomeButton.setVisibility(View.INVISIBLE);
		}
		if(mIcallButton!=null){
			mIcallButton.setVisibility(View.INVISIBLE);
		}
	}

	public void setBtnClickable(boolean bClickable){
		if(mCloseButton!=null){
			mCloseButton.setClickable(bClickable);
		}
		if(mHomeButton!=null){
			mHomeButton.setClickable(bClickable);
		}
		if(mIcallButton!=null){
			mIcallButton.setClickable(bClickable);
		}
	}
	
	public void doVoiceView( int degree){
		LogUtil.d(TAG, "doVoiceView degree="+degree);
		if(mVoiceMask != null){
			degree = (degree%101)/5;
			float tempdegree = degree*9+180;
			mVoiceMask.setRotation(tempdegree);
		}

	}
	
	@Override
	public void onAttach(Activity activity) {
		mParentActivity = activity;
		super.onAttach(activity);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	void startAnimation() {
	}

	void stopAnimation() {
	}
}

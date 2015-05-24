package com.tuyou.tsd.podcast.weight;



import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;
import android.widget.ImageView;
@SuppressWarnings("deprecation")
@SuppressLint("NewApi")
public class CoverFlow extends Gallery {

	private Camera mCamera = new Camera();
	private int mMaxRotationAngle = 50;
//	private int mMaxZoom = -240;
	private int mCoveflowCenter;
	private boolean mAlphaMode = true;
	private boolean mCircleMode = true;
	
	public CoverFlow(Context context) {
		super(context);
		this.setStaticTransformationsEnabled(true);
	}

	public CoverFlow(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setStaticTransformationsEnabled(true);
	}

	public CoverFlow(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setStaticTransformationsEnabled(true);
	}

	public int getMaxRotationAngle() {
		return mMaxRotationAngle;
	}

	public void setMaxRotationAngle(int maxRotationAngle) {
		mMaxRotationAngle = maxRotationAngle;
	}

	public boolean getCircleMode() {
		return mCircleMode;
	}

	public void setCircleMode(boolean isCircle) {
		mCircleMode = isCircle;
	}

	public boolean getAlphaMode() {
		return mAlphaMode;
	}

	public void setAlphaMode(boolean isAlpha) {
		mAlphaMode = isAlpha;
	}

//	public int getMaxZoom() {
//		return mMaxZoom;
//	}
//
//	public void setMaxZoom(int maxZoom) {
//		mMaxZoom = maxZoom;
//	}

	private int getCenterOfCoverflow() {
		return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2
				+ getPaddingLeft();
	}

	private static int getCenterOfView(View view) {
		return view.getLeft() + view.getWidth() / 2;
	}
	
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		int childCenter = getCenterOfView(child);
		int childWidth = child.getWidth();
		int rotationAngle = 0;
		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);
		if (childCenter == mCoveflowCenter) {
			transformImageBitmap((ImageView)child, t, 0);
		} else {
			rotationAngle = (int) (((float) (mCoveflowCenter - childCenter) / childWidth) * mMaxRotationAngle);
			if (Math.abs(rotationAngle) > mMaxRotationAngle) {
				rotationAngle = (rotationAngle < 0) ? -mMaxRotationAngle : mMaxRotationAngle;
			}
			transformImageBitmap((ImageView)child, t, rotationAngle);
		}
		return true;
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mCoveflowCenter = getCenterOfCoverflow();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private void transformImageBitmap(ImageView child, Transformation t,int rotationAngle) {
		mCamera.save();
		Matrix imageMatrix = t.getMatrix();
		int imageHeight = child.getLayoutParams().height;
		int imageWidth = child.getLayoutParams().width;
		int rotation = Math.abs(rotationAngle);
//		mCamera.translate(0.0f, 0.0f, 100.0f);

		if (rotation <= mMaxRotationAngle) {
//			float zoomAmount = (float) (mMaxZoom + (rotation * 1.5));
			if (mCircleMode) {
				mCamera.translate(0.0f, 0.0f, 0.0f);
//				if (rotation < 40){
//					mCamera.translate(0.0f, 0.0f, zoomAmount);
//				}else{
//					mCamera.translate(0.0f, -10.0f, (float)(zoomAmount*0.9));
//				}
			}
			try {
				if (mAlphaMode) {
					if((255-rotation * 4)>0){
						int alpha = (int) (255 - rotation * 4);
						if(alpha<150){
							child.setAlpha(200);
						}else{
							child.setAlpha((int) (255 - rotation * 4));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		mCamera.getMatrix(imageMatrix);
		imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));
		imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
		mCamera.restore();

	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}
	
}
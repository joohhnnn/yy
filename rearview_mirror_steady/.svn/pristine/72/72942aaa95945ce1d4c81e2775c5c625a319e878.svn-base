package com.txznet.record.view.rec;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.record.lib.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.view.View;

public class RecordAnimView extends View {
	private static final String TAG = RecordAnimView.class.getSimpleName();

	private int mWidth;
	private int mHeight;
	private float mCenterX;
	private float mCenterY;
	private int mCurState = AbsStepView.STATE_ENDED;

	RecordDrawable[] mDrawables = new RecordDrawable[6];
	int[] mDrawablesId = { R.drawable.ic_recording1, R.drawable.ic_recording2, R.drawable.ic_recording3,
			R.drawable.ic_recording4, R.drawable.ic_recording5, R.drawable.ic_recording6 };

//	private int[] index = { 0, 0, 0, 0, 0, 0 };
//	float[][] mDrawableScaleY = {
//			{ 0.05f, 0.1f, 0.2f, 0.3f, 0.4f, 0.6f, 0.9f, 1, 0.9f, 0.8f, 0.7f, 0.6f, 0.4f, 0.3f, 0.3197568f, 0.3383292f,
//					0.363f, 0.3780892f, 0.39479682f, 0.412f, 0.43184918f, 0.4533168f, 0.47500002f, 0.4996092f,
//					0.5258368f, 0.55200005f, 0.58136916f, 0.6123568f, 0.643f, 0.6771292f, 0.7128768f, 0.748f,
//					0.7868892f, 0.82739675f, 0.867f, 0.91064924f, 0.9559168f, 1.0f },
//			{ 0.05f, 0.1f, 0.2f, 0.3f, 0.4f, 0.6f, 0.9f, 1, 0.9f, 0.8f, 0.7f, 0.6f, 0.4f, 0.4f, 0.40661502f,
//					0.41283345f, 0.42109376f, 0.42614594f, 0.43174002f, 0.4375f, 0.44414595f, 0.45133376f, 0.45859376f,
//					0.46683344f, 0.475615f, 0.484375f, 0.49420846f, 0.5045838f, 0.51484376f, 0.526271f, 0.53824f, 0.55f,
//					0.56302094f, 0.57658374f, 0.58984375f, 0.60445845f, 0.619615f, 0.63437504f, 0.6505835f, 0.6673338f,
//					0.68359375f, 0.701396f, 0.71974003f, 0.7375f, 0.75689596f, 0.7768338f, 0.79609376f, 0.8170835f,
//					0.83861506f, 0.859375f, 0.8819585f, 0.9050838f, 0.9273437f, 0.9515209f, 0.97624004f, 1.0f },
//			{ 0.05f, 0.1f, 0.2f, 0.3f, 0.4f, 0.6f, 0.9f, 1, 0.9f, 0.8f, 0.7f, 0.8f, 0.3f, 0.3f, 0.31008002f,
//					0.31955573f, 0.33214286f, 0.33984146f, 0.34836572f, 0.35714287f, 0.36727002f, 0.37822288f,
//					0.3892857f, 0.40184143f, 0.41522285f, 0.42857143f, 0.4435557f, 0.45936573f, 0.47500002f,
//					0.49241287f, 0.5106514f, 0.5285715f, 0.54841286f, 0.56908f, 0.58928573f, 0.6115557f, 0.6346514f,
//					0.6571429f, 0.6818414f, 0.70736575f, 0.7321428f, 0.75926995f, 0.78722286f, 0.8142857f, 0.8438415f,
//					0.8742228f, 0.90357137f, 0.93555576f, 0.9683657f, 1.0f },
//			{ 0.05f, 0.1f, 0.2f, 0.3f, 0.4f, 0.6f, 0.9f, 1, 0.9f, 0.8f, 0.7f, 0.6f, 0.5f, 0.4f, 0.4f, 0.44704002f,
//					0.49126f, 0.55f, 0.58592665f, 0.6257067f, 0.66666675f, 0.7139267f, 0.76504f, 0.81666666f, 0.87526f,
//					0.9377067f, 1.0f },
//			{ 0.05f, 0.1f, 0.2f, 0.3f, 0.4f, 0.6f, 0.9f, 1, 0.9f, 0.8f, 0.7f, 0.6f, 0.3f, 0.3f, 0.33087f, 0.3598894f,
//					0.3984375f, 0.4220144f, 0.44812f, 0.47500002f, 0.5060144f, 0.5395575f, 0.5734375f, 0.61188936f,
//					0.65286994f, 0.69375f, 0.7396394f, 0.78805745f, 0.8359375f, 0.8892644f, 0.94512f, 1.0f },
//			{ 0.05f, 0.1f, 0.2f, 0.3f, 0.4f, 0.6f, 0.9f, 1, 0.9f, 0.8f, 0.7f, 0.6f, 0.3f, 0.3f, 0.32439113f,
//					0.34732002f, 0.37777779f, 0.39640644f, 0.41703308f, 0.4382716f, 0.4627768f, 0.48928f, 0.5160494f,
//					0.54643106f, 0.5788109f, 0.61111116f, 0.6473694f, 0.6856257f, 0.7234568f, 0.7655916f, 0.80972445f,
//					0.8530864f, 0.9010978f, 0.9511072f, 1.0f } };
//	private int[] length = { mDrawableScaleY[0].length, mDrawableScaleY[1].length, mDrawableScaleY[2].length,
//			mDrawableScaleY[3].length, mDrawableScaleY[4].length, mDrawableScaleY[5].length };
	
	private boolean isDrawLine = true;
	private Paint mLinePaint;
	private int mLineSpeed;
	private int mCount = 7;
	private int mLineWidth;
	private AbsStepView parentView;
	private float mDefaultWidth;
	private float mScaleX = 1;
	private float mMaxScaleY = 1f;

	public RecordAnimView(AbsStepView parentView, Context context) {
		super(context);
		this.parentView = parentView;
		mDefaultWidth = getResources().getDimension(R.dimen.x230);
		init(context);
	}

	private void init(Context context) {
		for (int i = 0; i < mDrawablesId.length; i++) {
			mDrawables[i] = new RecordDrawable(mDrawablesId[i]);
			mDrawables[i].setBounds(-mDrawables[i].width / 2, -mDrawables[i].height / 2, mDrawables[i].width / 2,
					mDrawables[i].height / 2);
		}

		mLinePaint = new Paint();
		mLinePaint.setColor(Color.WHITE);
		mLinePaint.setAntiAlias(true);
		mLinePaint.setStyle(Style.FILL);
		mLineWidth = mDrawables[0].width;
		mLineSpeed = (int) ((mLineWidth / 2) / mCount);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (changed) {
			mWidth = getWidth();
			mHeight = getHeight();
			mCenterX = mWidth / 2;
			mCenterY = mHeight / 2;

			resetLineOffset();

			if (mWidth < mDefaultWidth) {
				mScaleX = (float) mWidth / (float)mDefaultWidth;
			}
			mMaxScaleY = (float)mHeight/(float)mDrawables[0].height;
			if (mMaxScaleY > 1) {
				mMaxScaleY = 1;
			}
		}

	}

	private void resetLineOffset() {
		mLineStartXCur = (int) mCenterX;
		mLineStopXCur = (int) mCenterX;
		mLineStartX = (int) (mCenterX - mLineWidth / 2);
		mLineStopX = (int) (mCenterX + mLineWidth / 2);

		for (RecordDrawable rd : mDrawables) {
			rd.scaleY = 0.1f;
		}
		mDestinScale = 1.0f;
		mMaxVol = 0;
	}

	private int tmpIndex = 0;
	private boolean isUpdateWithVol;

	private float mMaxVol = 0;

	public void updateVol(int vol) {
		if (vol > mMaxVol) {
			if (mMaxVol == 0) {
				mMaxVol = 2 * vol;
			}else {
				mMaxVol = vol;
			}
		}

		// if (vol < mMinVol) {
		// mMinVol = vol;
		// }

		float volK = 1;
		if (mMaxVol != 0)
			volK = vol / mMaxVol;
//		LogUtil.loge("updateVol:" + vol + ",k:" + volK);

		if (procVolUpdate(volK)) {
			return;
		}

//		if (!isDrawLine) {
//			updateHeight(vol);
//
//			if (Looper.getMainLooper() == Looper.myLooper()) {
//				invalidate();
//			} else {
//				postInvalidate();
//			}
//		}
	}

	private boolean procVolUpdate(float scaleY) {
//		for (int i = 0; i < 6; i++) {
//			mDrawables[i].scaleY = scaleY;
//		}
//
//		if (Looper.getMainLooper() == Looper.myLooper()) {
//			invalidate();
//		} else {
//			postInvalidate();
//		}
		mDestinScale = scaleY;
		return true;
	}

//	private void updateHeight(int vol) {
//		if (isUpdateWithVol) {
//			for (int i = 0; i < 6; i++) {
//				if (vol < 10) {
//					index[i] = 1;
//				} else if (vol < 20) {
//					index[i] = 2;
//				} else if (vol < 30) {
//					index[i] = 3;
//				} else if (vol < 40) {
//					index[i] = 4;
//				} else if (vol < 50) {
//					index[i] = 5;
//				} else if (vol < 60) {
//					index[i] = 6;
//				} else if (vol < 70) {
//					index[i] = 7;
//				} else if (vol < 80) {
//					index[i] = 8;
//				} else if (vol < 90) {
//					index[i] = 9;
//				} else if (vol < 100) {
//					index[i] = 10;
//				} else {
//					index[i] = 5;
//				}
//				mDrawables[i].scaleY = mDrawableScaleY[i][index[i]];
//			}
//		} else {
//			tmpIndex++;
//			if (tmpIndex > 80) {
//				isUpdateWithVol = true;
//			} else {
//				isUpdateWithVol = false;
//			}
//			for (int i = 0; i < 6; i++) {
//				mDrawables[i].scaleY = mDrawableScaleY[i][tmpIndex / 10];
//			}
//		}
//	}

	float mDestinScale =1f;
	float[][] mDrawableOffset = {
			{0.3f,0.6f,1.0f,0.1f},
			{0.3f,0.6f,1.0f,0.14f},
			{0.3f,0.6f,1.0f,0.18f},
			{0.3f,0.6f,1.0f,0.15f},
			{0.3f,0.6f,1.0f,0.16f},
			{0.3f,0.6f,1.0f,0.13f}
	};
	
	boolean[] mScaleUp = { true, true, true, true, true, true };
	boolean[] mMidFlag = { false, false, false, false, false, false };

	private Runnable mDrawVoiceRunnable = new Runnable() {
		@Override
		public void run() {
			// for (int i = 0; i < index.length; i++) {
			// index[i]++;
			// if (index[i] >= length[i] - 1) {
			// index[i] = 4;
			// }
			// mDrawables[i].scaleY = mDrawableScaleY[i][index[i]];
			// }
			for (int i = 0; i < mDrawables.length; i++) {
				if (mDestinScale <= 0) {
					mDestinScale = mDrawableOffset[i][0];
				}

				mDrawableOffset[i][2] = mDestinScale;

				float curOft = mDrawables[i].scaleY;
				if (curOft >= mDrawableOffset[i][2]) {
					mScaleUp[i] = false;
				} else if (curOft <= mDrawableOffset[i][0]) {
					mScaleUp[i] = true;
					mMidFlag[i] = !mMidFlag[i];
				} else {
					if (!mScaleUp[i]) {
						if (Math.abs(curOft - mDrawableOffset[i][1]) < mDrawableOffset[i][3]) {
							if (mMidFlag[i]) {
								mScaleUp[i] = true;
							}
						}
					}
				}

				float speed = mDrawableOffset[i][3];
				if (curOft < 0.5f) {
					speed -= speed * (1 - curOft);
				} else {
					speed += speed * (curOft);
				}

				

				if (mScaleUp[i]) {
					mDrawables[i].scaleY += speed;
				} else {
					mDrawables[i].scaleY -= speed;
				}
//				LogUtil.loge("speed:" + speed+"; "+i+"-scalyY:"+mDrawables[i].scaleY);

				if (mDrawables[i].scaleY > 1) {
					mDrawables[i].scaleY = 1;
				} else if (mDrawables[i].scaleY <= 0) {
					mDrawables[i].scaleY = 0.15f;
				}
			}

			invalidate();

			removeCallbacks(mDrawVoiceRunnable);
			postDelayed(mDrawVoiceRunnable, 55);
		}
	};
	
	private Runnable mDrawLineRunnable = new Runnable() {
		@Override
		public void run() {
			invalidate();
			removeCallbacks(mDrawLineRunnable);
			postDelayed(mDrawLineRunnable, 25);
		}
	};

	public void playStartAnim() {
		resetLineOffset();

		tmpIndex = 0;
		isDrawLine = true;
		mCurState = AbsStepView.STATE_START;
		post(mDrawLineRunnable);
		if (parentView.mListener != null) {
			parentView.mListener.onAnimationStep(parentView, AbsStepView.STATE_START);
		}
	}

	public void playEndAnim() {
		removeCallbacks(mDrawLineRunnable);
		removeCallbacks(mDrawVoiceRunnable);
		mCurState = AbsStepView.STATE_ENDED;
		if (parentView.mListener != null) {
			parentView.mListener.onAnimationStep(parentView, AbsStepView.STATE_ENDED);
		}
	}

	public boolean isAniming() {
		return mCurState != AbsStepView.STATE_ENDED;
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		canvas.save();
		canvas.scale(mScaleX, mScaleX, mCenterX, mCenterY);
		if (isDrawLine) {
			drawLineView(canvas);
		} else {
			drawVoiceView(canvas);
		}

	}

	private int mLineStartXCur = 0;
	private int mLineStopXCur = 0;
	private int mLineStartX = 0;
	private int mLineStopX = 0;

	private void drawLineView(Canvas canvas) {
		canvas.save();
		mLineStartXCur -= mLineSpeed;
		mLineStopXCur += mLineSpeed;
		if (mLineStartXCur <= mLineStartX) {
			mLineStartXCur = mLineStartX;
			isDrawLine = false;
		}
		if (mLineStopXCur >= mLineStopX) {
			mLineStopXCur = mLineStopX;
			isDrawLine = false;
		}
		if (!isDrawLine) {
			// 停止画白线
			removeCallbacks(mDrawLineRunnable);
			// 开始画声控动画
			post(mDrawVoiceRunnable);
			if (parentView.mListener != null) {
				parentView.mListener.onAnimationStep(parentView, AbsStepView.STATE_LOOP);
			}
			return;
		}

		canvas.drawLine(mLineStartXCur, mCenterY, mLineStopXCur, mCenterY, mLinePaint);

		canvas.restore();
	}

	private void drawVoiceView(Canvas canvas) {
		canvas.save();
		canvas.translate(mCenterX, mCenterY);
		mDrawables[0].draw(canvas);
		mDrawables[1].draw(canvas);
		mDrawables[5].draw(canvas);
		mDrawables[3].draw(canvas);
		mDrawables[4].draw(canvas);
		mDrawables[2].draw(canvas);
		canvas.restore();
	}

	class RecordDrawable {
		Drawable drawable;
		int width;
		int height;
		float scaleX = 1;
		float scaleY = 1;
		float mCenterX = 0;
		float mCenterY = 0;

		public RecordDrawable(int resId) {
			drawable = getResources().getDrawable(resId);
			width = drawable.getIntrinsicWidth();
			height = drawable.getIntrinsicHeight();
			
			scaleY = 0.1f;
		}

		void setBounds(int left, int top, int right, int bottom) {
			drawable.setBounds(left, top, right, bottom);
			mCenterX = left + width / 2;
			mCenterY = top + height / 2;
		}

		void draw(Canvas canvas) {
			if (drawable != null) {
				canvas.save();
				canvas.scale(scaleX, scaleY*mMaxScaleY, mCenterX, mCenterY);
				drawable.draw(canvas);
				canvas.restore();
			}
		}
	}
}
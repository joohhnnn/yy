package com.txznet.record.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.record.lib.R;
import com.txznet.txz.util.TXZHandler;

/**
 * Created by ASUS User on 2015/9/11.
 */
public class WaveformViewDefImpl extends WaveformView {
	private Drawable mCenter;
	private Drawable mValEmptyStyle;
	private Drawable mValRealStyle;
	private int mRange = 6;

	private int mIconWidth;
	private int mIconHeight;

	private int mCenterOffset;
	private int mValOffset;

	private Rect mCenterRect;
	private Rect mShortValRef;
	private Rect mLongValRef;

	private boolean mIsDrawVoice = true;
	private boolean mStopAnimation = false;

	private int lastWidth;
	private int lastHeight;

	public WaveformViewDefImpl(Context context) {
		super(context);
	}

	public WaveformViewDefImpl(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveformViewDefImpl);
		mRange = typedArray.getInt(R.styleable.WaveformViewDefImpl_wvRange, 0);
		mCenter = typedArray.getDrawable(R.styleable.WaveformViewDefImpl_wvIcon);
		mValRealStyle = typedArray.getDrawable(R.styleable.WaveformViewDefImpl_wvRealVal);
		mValEmptyStyle = typedArray.getDrawable(R.styleable.WaveformViewDefImpl_wvEmptyVal);

		mIconWidth = typedArray.getDimensionPixelSize(R.styleable.WaveformViewDefImpl_wvIconWidth, 0);
		mIconHeight = typedArray.getDimensionPixelSize(R.styleable.WaveformViewDefImpl_wvIconHeight, 0);
		mCenterOffset = typedArray.getDimensionPixelSize(R.styleable.WaveformViewDefImpl_wvIconOffset, 0);
		mValOffset = typedArray.getDimensionPixelSize(R.styleable.WaveformViewDefImpl_wvValOffset, 0);
		mIsDrawVoice = typedArray.getBoolean(R.styleable.WaveformViewDefImpl_wvIsDrawVoice, true);
		mStopAnimation = typedArray.getBoolean(R.styleable.WaveformViewDefImpl_wvAnimationStatus, false);

		typedArray.recycle();
	}

	@Override
	public void updateAmplitude(float amplitude) {

	}

	@Override
	public float getAmplitude() {
		return 0;
	}

	private int val = 0;

	public void setVal(int val) {
		if (val >= 0 || val <= mRange) {
			this.val = val;
			invalidate();
		}
	}

	public void setDrawVoice(boolean isDraw) {
		mIsDrawVoice = isDraw;
		invalidate();
	}

	public void setStopAnimation(boolean stop) {
		if (mStopAnimation == stop) {
			return;
		}

		mStopAnimation = stop;
		setVal(0);
		invalidate();
	}

	@Override
	public void invalidate() {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			super.invalidate();
		} else {
			postInvalidate();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mCenter == null || mValRealStyle == null || mValEmptyStyle == null) {
			return;
		}

		int w = getWidth();
		int h = getHeight();

		if (w == 0 || h == 0) {
			return;
		}

		if (mIsDrawVoice) {
			if (mCenterRect == null || (lastHeight != h || lastWidth != w)) {// 众鸿科技，长短屏同现
				lastHeight = h;
				lastWidth = w;
				mCenterRect = new Rect(w / 2 - h / 2, 0, w / 2 + h / 2, h);
				mCenter.setBounds(mCenterRect.centerX() - mIconWidth / 2, mCenterRect.centerY() - mIconHeight / 2,
						mCenterRect.centerX() + mIconWidth / 2, mCenterRect.centerY() + mIconHeight / 2);

				float wScale = 0.025f;
				float hLongScale = 0.20f;
				float hShortScale = 0.15f;
				mShortValRef = new Rect(-(int) (wScale * h), h / 2 - (int) (h * hShortScale), (int) (wScale * h),
						h / 2 + (int) (h * hShortScale));
				mLongValRef = new Rect(-(int) (wScale * h), h / 2 - (int) (h * hLongScale), (int) (wScale * h),
						h / 2 + (int) (h * hLongScale));
			}
			mCenter.draw(canvas);
		} else {
			if (mCenterRect == null || (lastHeight != h || lastWidth != w)) {
				mCenterRect = new Rect(w / 2, 0, w / 2, 0);

				float wScale = (float) (3.0 / 140.0);
				float hLongScale = 0.20f;
				float hShortScale = 0.15f;
				mShortValRef = new Rect(-(int) (wScale * h), h / 2 - (int) (h * hShortScale), (int) (wScale * h),
						h / 2 + (int) (h * hShortScale));
				mLongValRef = new Rect(-(int) (wScale * h), h / 2 - (int) (h * hLongScale), (int) (wScale * h),
						h / 2 + (int) (h * hLongScale));
			}
		}

		try {
			for (int i = 1; i <= mRange; i++) {
				Rect rectRef = null;
				if (i % 2 == 0) {
					rectRef = mLongValRef;
				} else {
					rectRef = mShortValRef;
				}
				Drawable target = null;
				if (i <= val) {
					target = mValRealStyle;
				} else {
					target = mValEmptyStyle;
				}
				target.setBounds(mCenterRect.left - mCenterOffset + rectRef.left - (i - 1) * mValOffset, rectRef.top,
						mCenterRect.left - mCenterOffset + rectRef.right - (i - 1) * mValOffset, rectRef.bottom);
				target.draw(canvas);
			}

			for (int i = 1; i <= mRange; i++) {
				Rect rectRef = null;
				if (i % 2 == 0) {
					rectRef = mLongValRef;
				} else {
					rectRef = mShortValRef;
				}
				Drawable target = null;
				if (i <= val) {
					target = mValRealStyle;
				} else {
					target = mValEmptyStyle;
				}
				target.setBounds(mCenterRect.right + mCenterOffset + rectRef.left + (i - 1) * mValOffset, rectRef.top,
						mCenterRect.right + mCenterOffset + rectRef.right + (i - 1) * mValOffset, rectRef.bottom);
				target.draw(canvas);
			}

			if (mStopAnimation) {
				return;
			}

			val = ++val % 7;

			mUiHandler.removeCallbacks(mInvalidateTask);
			mUiHandler.postDelayed(mInvalidateTask, 1000 / 10);
		} catch (Exception e) {
			LogUtil.loge("WaveformAnimation:" + e.toString());
		}
	}
	
	private TXZHandler mUiHandler = new TXZHandler(Looper.getMainLooper());
	
	private Runnable mInvalidateTask = new  Runnable() {
		public void run() {
			invalidate();
		}
	};

	@Override
	public void onStart() {
		setVisibility(View.VISIBLE);
	}

	@Override
	public void onEnd() {
		setVisibility(View.INVISIBLE);
	}

	@Override
	public void onIdle() {
		setVisibility(View.INVISIBLE);
	}
}

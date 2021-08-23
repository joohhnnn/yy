package com.txznet.record.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import com.txznet.record.view.WaveformView;

/**
 * WaveformView.
 *
 * @author kang
 * @since 14/9/24.
 */
public class WaveformViewImpl extends WaveformView {
	private static final float MIN_AMPLITUDE = 0.0575f;
	private float mPrimaryWidth = 2.5f;
	private float mSecondaryWidth = 0.7f;
	private float mAmplitude = MIN_AMPLITUDE;
	private int mWaveColor = Color.parseColor("#34BFFF");
	private int mDensity = 15; // 影响平滑
	private int mWaveCount = 7; // 线条数量
	private float mFrequency = 0.1575f; // 间隔
	private float mPhaseShift = -0.2575f; // 速率
	private float mPhase = mPhaseShift;

	private Paint mPrimaryPaint;
	private Paint mSecondaryPaint;

	private Path mPath;

	private float mLastX;
	private float mLastY;

	public WaveformViewImpl(Context context) {
		this(context, null);
	}

	public WaveformViewImpl(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WaveformViewImpl(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	private void initialize() {
		mPrimaryPaint = new Paint();
		mPrimaryPaint.setStrokeWidth(mPrimaryWidth);
		mPrimaryPaint.setAntiAlias(true);
		mPrimaryPaint.setStyle(Paint.Style.STROKE);
		mPrimaryPaint.setColor(mWaveColor);

		mSecondaryPaint = new Paint();
		mSecondaryPaint.setStrokeWidth(mSecondaryWidth);
		mSecondaryPaint.setAntiAlias(true);
		mSecondaryPaint.setStyle(Paint.Style.STROKE);
		mSecondaryPaint.setColor(mWaveColor);

		mPath = new Path();
	}

	public void updateAmplitude(float amplitude) {
		mAmplitude = Math.max(amplitude, MIN_AMPLITUDE);
	}

	public float getAmplitude() {
		return mAmplitude;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = getWidth();
		int height = getHeight();

		for (int l = 0; l < mWaveCount; ++l) {
			float midH = height / 2.0f;
			float midW = width / 2.0f;

			float maxAmplitude = midH / 2f - 4.0f;
			float progress = 1.0f - l * 1.0f / mWaveCount;
			float normalAmplitude = (1.5f * progress - 0.5f) * mAmplitude;

			// float multiplier = (float) Math.min(1.0,
			// (progress / 3.0f * 2.0f) + (1.0f / 3.0f));

			if (l != 0) {
				mSecondaryPaint.setAlpha(50 + (int) ((l + 1) * 1f / mWaveCount * 150));
			} else {
				mSecondaryPaint.setAlpha(240);
			}

			mPath.reset();
			for (int x = 0; x < width + mDensity; x += mDensity) {
				float scaling = 1f - (float) Math.pow(1 / midW * (x - midW), 2);
				float y = scaling * maxAmplitude * normalAmplitude
						* (float) Math.sin(180 * x * mFrequency / (width * Math.PI) + mPhase) + midH;
				if (x == 0) {
					mPath.moveTo(x, y);
				} else {
					mPath.lineTo(x, y);
				}
			}

			if (l == 0) {
				canvas.drawPath(mPath, mPrimaryPaint);
			} else {
				canvas.drawPath(mPath, mSecondaryPaint);
			}
		}
		mPhase += mPhaseShift;
		mUiHandler.removeCallbacks(mInvalidateTask);
		mUiHandler.postDelayed(mInvalidateTask, 1000 / 160);
    }
    
    private Handler mUiHandler = new Handler(Looper.getMainLooper());
	
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

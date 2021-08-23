package com.txznet.music.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.txznet.loader.AppLogic;
import com.txznet.music.R;

public class MusicMoveView extends View {

	private Paint mPaint;
	private Handler mHandler;

	// 整个view默认宽度高度
	private int mDefaultWidth = (int) (31 * AppLogic.density);
	private int mDefaultHeight = (int) (25 * AppLogic.density);

	// 每个柱状图间距和宽度
	private int mChildsSpace = (int) (4 * AppLogic.density);
	private int mChildsWidth = (int) (8 * AppLogic.density);

	// private int mDefaultColor = 0xff1cc859;
	private int mDefaultColor = 0xff34bfff;

	// 最高
	private float[] mChildsMaxHeight = { 15 * AppLogic.density, 40 * AppLogic.density, 20 * AppLogic.density, 25 * AppLogic.density };
	// 最低
	private float[] mChildsMinHeight = { 4 * AppLogic.density, 4 * AppLogic.density, 4 * AppLogic.density, 4 * AppLogic.density };
	// 高度
	private float[] mChildsHeight = { 5 * AppLogic.density, 25 * AppLogic.density, 10 * AppLogic.density, 18 * AppLogic.density };
	// 速度
	private float[] mChildsSpeed = { 5 * AppLogic.density, 10 * AppLogic.density, 5 * AppLogic.density, 10 * AppLogic.density };

	private boolean[] mChildsUp = { true, true, true, false };

	private final int mSpeedMillis = 100;
//	private final int mSpeedMillis = (int) (20 * AppLogic.density);

	public MusicMoveView(Context context) {
		this(context, null);
	}

	public MusicMoveView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MusicMoveView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		init();
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MusicMoveView);
		int textColor = array.getColor(R.styleable.MusicMoveView_MusicMoveColor, mDefaultColor); // 提供默认值，放置未指定
		mPaint.setColor(textColor);
		array.recycle(); // 一定要调用，否则这次的设定会对下次的使用造成影响
	}

	private void init() {
		mPaint = new Paint();
		mHandler = new Handler();
		mHandler.post(mUpdateView);
		mHandler.post(mUpdateHeight);
	}

	private Runnable mUpdateView = new Runnable() {
		@Override
		public void run() {
			if (getVisibility() != View.VISIBLE) {
				mHandler.postDelayed(mUpdateView, mSpeedMillis);
				return;
			}
			if (isStop) {
				mHandler.postDelayed(mUpdateView, mSpeedMillis);
				return;
			}
			invalidate();
			mHandler.postDelayed(mUpdateView, mSpeedMillis);
		}
	};
	boolean isStop = false;// 当前是否不播放音乐

	private Runnable mUpdateHeight = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (getVisibility() != View.VISIBLE || isStop) {
				mHandler.postDelayed(mUpdateHeight, mSpeedMillis);
				return;
			}
			for (int i = 0; i < mChildsHeight.length; i++) {
				if (mChildsUp[i]) {
					mChildsHeight[i] += mChildsSpeed[i];
					if (mChildsHeight[i] > mChildsMaxHeight[i]) {
						mChildsHeight[i] -= mChildsSpeed[i];
						mChildsUp[i] = false;
					}
				} else {
					mChildsHeight[i] -= mChildsSpeed[i];
					if (mChildsHeight[i] < mChildsMinHeight[i]) {
						mChildsHeight[i] += mChildsSpeed[i];
						mChildsUp[i] = true;
					}
				}
			}
			mHandler.postDelayed(mUpdateHeight, mSpeedMillis);
		}
	};

	@Override
	protected void onDraw(Canvas canvas) {
		int height = getMeasuredHeight();
		for (int i = 0; i < mChildsHeight.length; i++) {
			canvas.drawRect((mChildsSpace + mChildsWidth) * i, height - mChildsHeight[i], ((mChildsSpace + mChildsWidth) * i) + mChildsWidth, height, mPaint);
		}
		super.onDraw(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = 0;
		int height = 0;

		int specMode = MeasureSpec.getMode(widthMeasureSpec);
		int specSize = MeasureSpec.getSize(widthMeasureSpec);
		switch (specMode) {
		case MeasureSpec.UNSPECIFIED:
		case MeasureSpec.EXACTLY:
			width = getPaddingLeft() + getPaddingRight() + specSize;
			break;
		case MeasureSpec.AT_MOST:
			width = getPaddingLeft() + getPaddingRight() + mDefaultWidth;
			break;
		}

		specMode = MeasureSpec.getMode(heightMeasureSpec);
		specSize = MeasureSpec.getSize(heightMeasureSpec);
		switch (specMode) {
		case MeasureSpec.UNSPECIFIED:
		case MeasureSpec.EXACTLY:
			height = getPaddingTop() + getPaddingBottom() + specSize;
			break;
		case MeasureSpec.AT_MOST:
			height = getPaddingTop() + getPaddingBottom() + mDefaultHeight;
			break;
		}

		setMeasuredDimension(width, height);
	}

	public void setPaintColor(int color) {
		mPaint.setColor(color);
	}

	public void stop() {
		isStop = true;
	}

	public void start() {
		isStop = false;
	}
}

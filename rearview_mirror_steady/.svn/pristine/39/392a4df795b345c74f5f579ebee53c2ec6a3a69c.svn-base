package com.txznet.music.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.txznet.music.R;

public class MusicMoveView extends View {

	private Paint mPaint;
	private Context mContext;
	private Handler mHandler;

	// 整个view默认宽度高度
	private int mDefaultWidth = 31;
	private int mDefaultHeight = 25;

	// 每个柱状图间距和宽度
	private int mChildsSpace = 5;
	private int mChildsWidth = 4;

	private int mDefaultColor = 0xff34bfff;

	// 最高
	private int[] mChildsMaxHeight = { 10, 25, 15, 20 };
	// 最低
	private int[] mChildsMinHeight = { 4, 4, 4, 4 };
	// 高度
	private int[] mChildsHeight = { 5, 25, 10, 18 };
	// 速度
	private int[] mChildsSpeed = { 5, 10, 5, 10 };

	private boolean[] mChildsUp = { true, true, true, false };

	private final int mSpeedMillis = 100;

	public MusicMoveView(Context context) {
		this(context, null);
	}

	public MusicMoveView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MusicMoveView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		mContext = context;
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
			invalidate();
			mHandler.postDelayed(mUpdateView, mSpeedMillis);
		}
	};

	private Runnable mUpdateHeight = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (getVisibility() != View.VISIBLE) {
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
			canvas.drawRect((mChildsSpace + mChildsWidth) * i, height - mChildsHeight[i],
					((mChildsSpace + mChildsWidth) * i) + mChildsWidth, height, mPaint);
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
}

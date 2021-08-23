package com.txznet.music.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class SwipeLinearLayout extends LinearLayout {
	final int FLING_MIN_DISTANCE = 100, FLING_MIN_VELOCITY = 200; 
	Scroller scorll;
	GestureDetector detector;

	@SuppressLint("NewApi")
	public SwipeLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context ctx) {
		scorll = new Scroller(ctx);
		detector = new GestureDetector(ctx,
				new GestureDetector.OnGestureListener() {

					@Override
					public boolean onSingleTapUp(MotionEvent e) {
						return false;
					}

					@Override
					public void onShowPress(MotionEvent e) {

					}

					@Override
					public boolean onScroll(MotionEvent e1, MotionEvent e2,
							float distanceX, float distanceY) {
						return false;
					}

					@Override
					public void onLongPress(MotionEvent e) {

					}

					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {
						if (velocityX > FLING_MIN_VELOCITY && e1.getX() - e2.getX() > FLING_MIN_DISTANCE) {// 显示右边内容
							if (null != listener) {
								listener.leftFlip();
							}
						} else if (velocityX > FLING_MIN_VELOCITY
								&& e1.getX() - e2.getX() < -FLING_MIN_DISTANCE) {// 显示左边内容
							if (null != listener) {
								listener.rightFlip();
							}
						}
						return true;
					}

					@Override
					public boolean onDown(MotionEvent e) {
						return false;
					}
				});
	}

	public SwipeLinearLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SwipeLinearLayout(Context context) {
		this(context, null);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}

	public float startX;
	public float currentX;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		detector.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			startX = event.getX();
			currentX = event.getX();
			break;
		case MotionEvent.ACTION_CANCEL:

			break;
		case MotionEvent.ACTION_MOVE:
			currentX = event.getX();
			scrollTo((int) (startX - currentX), 0);
			break;
		case MotionEvent.ACTION_UP:
			if (Math.abs(startX - currentX) > 100) {// 大于100个像素
				if (startX - currentX > 100) {
					listener.leftFlip();
				} else {
					listener.rightFlip();
				}
			}
			scrollTo(0, 0);
			break;
		}

		return true;
	}

	public OnFlipListener listener;

	public void setFlipListener(OnFlipListener listener) {
		this.listener = listener;
	}

	public interface OnFlipListener {
		public void leftFlip();

		public void rightFlip();
	}

}

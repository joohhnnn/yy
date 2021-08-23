package com.txznet.music.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.txznet.music.R;

public class CustomSeekBar1 extends SeekBar {

	private static final int DEFAULT_PROGRESS_COLOR = R.color.progress_color;
	private static final int DEFAULT_FINISHED_PROGRESS_COLOR = R.color.finished_progress_color;
	private static final int DEFAULT_BUFFERED_PROGRESS_COLOR = R.color.buffered_progress_color;
	// 当前进度
	private float finishedProgress = 0;// 默认是零
	private float bufferedProgress = 0;// 默认是零
	private float totalProgress = 1;// 默认是零

	private int finishedColor = DEFAULT_FINISHED_PROGRESS_COLOR;// 播放的进度条颜色
	private int bufferedColor = DEFAULT_BUFFERED_PROGRESS_COLOR;// 缓冲的进度条颜色
	private int progressColor = DEFAULT_PROGRESS_COLOR;// 总共的进度条颜色

	private int progressBarWidth;
	private int progressBarHeight;
	private boolean isActionDown;

	public CustomSeekBar1(Context context) {
		this(context, null);
	}

	public CustomSeekBar1(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CustomSeekBar1(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 设置滚动条进度（绿色）
	 * 
	 * @param progress
	 */
	public void setFinishedProgress(float finishedProgress) {
		this.finishedProgress = finishedProgress;
	}

	/**
	 * 设置滚动条播放进度条
	 * 
	 * @param progressColor
	 */
	public void setFinishedProgressColor(int finishedColor) {
		this.finishedColor = finishedColor;
	}

	/**
	 * 设置缓冲进度（深灰色）
	 * 
	 * @param progress
	 */
	public void setBufferedProgress(float bufferedProgress) {
		this.bufferedProgress = bufferedProgress;
	}

	public float getBufferedProgress() {
		return bufferedProgress;
	}

	/**
	 * 设置缓冲区的颜色
	 * 
	 * @param bufferColor
	 */
	public void setBufferColor(int bufferColor) {
		this.bufferedColor = bufferColor;
	}

	/**
	 * 设置滚动条总共的长度
	 * 
	 * @param totalProgress
	 */
	public void setProgress(int totalProgress) {
		this.totalProgress = totalProgress;
	}

	/**
	 * 设置滚动条的背景颜色
	 * 
	 * @param ProgressColor
	 */
	public void setProgressColor(int progressColor) {
		this.progressColor = progressColor;
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	protected void onDraw(Canvas canvas) {
		progressBarWidth = getWidth();
		progressBarHeight = getHeight();

		// if (bufferedProgress == 0 && finishedProgress == 0) {// 准备阶段
		drawRect(canvas, totalProgress, progressColor);
		// }
		drawRect(canvas, bufferedProgress, bufferedColor);
		drawRect(canvas, finishedProgress, finishedColor);

		// drawRect(canvas, Float.parseFloat(getProgress()*1.0 / getMax()+""),
		// finishedColor);
		// super.onDraw(canvas);

	}

	// @Override
	// public void invalidate() {
	// if (isActionDown) {
	// return;
	// }
	// super.invalidate();
	// }

	public void drawRect(Canvas canvas, float progress, int progressColor) {
		// //滚动条背景颜色
		Paint progressPaint = new Paint();
		progressPaint.setColor(getResources().getColor(progressColor));
		Rect progressRect = new Rect();
		progressRect.set(0, 0, (int) (progressBarWidth * progress), progressBarHeight);
		canvas.drawRect(progressRect, progressPaint);
	}

	private float startX;
	private float startY;
	private float currentX;
	private float currentY;

	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// switch (event.getAction()) {
	// case MotionEvent.ACTION_DOWN:
	// isActionDown = true;
	// startX = getX();
	// break;
	// case MotionEvent.ACTION_MOVE:
	// currentX = getX();
	// getMeasuredWidth();
	// getMeasuredHeight();
	//
	// break;
	// case MotionEvent.ACTION_UP:
	// isActionDown = false;
	// if (null != listener) {
	//
	// }
	//
	// break;
	//
	// default:
	// break;
	// }
	// return super.onTouchEvent(event);
	// }

	private SeekToListener listener;

	public interface SeekToListener {
		public void seekTo(float position);
	}

	public void setListener(SeekToListener listener) {
		this.listener = listener;
	}

}
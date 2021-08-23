package com.txznet.music.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueeText extends AppCompatTextView implements Runnable {
	private int currentScrollX = 0;// 初始滚动的位置
	private int firstScrollX = 0;
	private boolean isStop = false;
	private int textWidth;
	private int mWidth = 0; // 控件宽度
	private int speed = 2;
	private int delayed = 1000;
	private int endX; // 滚动到哪个位置
	private boolean isFirstDraw = true; // 当首次或文本改变时重置

	public MarqueeText(Context context) {
		super(context);

	}

	public MarqueeText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MarqueeText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (isFirstDraw) {
			getTextWidth();
			firstScrollX = getScrollX(); // 起始位置不一定为0,改变内容后会变，需重新赋值
			currentScrollX = firstScrollX;
			mWidth = this.getWidth();
			endX = firstScrollX + textWidth - mWidth / 2;
			isFirstDraw = false;
		}
	}

	// 每次滚动几点

	public void setSpeed(int sp) {
		speed = sp;
	}

	// 滚动间隔时间,毫秒

	public void setDelayed(int delay) {
		delayed = delay;
	}

	/**
	 * 获取文字宽度
	 */
	private void getTextWidth() {
		Paint paint = this.getPaint();
		String str = this.getText().toString();
		textWidth = (int) paint.measureText(str);
	}

	@Override
	public void run() {
		// currentScrollX += 1;// 滚动速度
		currentScrollX += speed;// 滚动速度,每次滚动几点
		scrollTo(currentScrollX, 0);
		if (isStop) {
			return;
		}
		// 从头开始
		if (currentScrollX >= endX) {
			// scrollTo(0, 0);
			// currentScrollX = 0; //原文重置为0,发现控件所放的位置不同，初始位置不一定为0
			scrollTo(firstScrollX, 0);
			currentScrollX = firstScrollX;
			postDelayed(this, 4000);
		} else {
			postDelayed(this, delayed);
		}
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {

		isStop = true; // 停止滚动
		this.removeCallbacks(this); // 清空队列
		currentScrollX = firstScrollX; // 滚动到初始位置
		this.scrollTo(currentScrollX, 0);
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		isFirstDraw = true; // 需重新设置参数
		isStop = false;
		postDelayed(this, 4000); // 头部停4秒

	}

	// 开始滚动
	public void startScroll() {
		isStop = false;
		this.removeCallbacks(this);
		postDelayed(this, 4000);
	}

	// 停止滚动
	public void stopScroll() {
		isStop = true;
	}

	// 从头开始滚动
	public void startFor0() {
		currentScrollX = 0;
		startScroll();
	}
}
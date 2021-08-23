package com.txznet.music.widget;

import com.txznet.music.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author telenewbie
 * @version 创建时间：2016年6月14日 下午3:22:20
 * 
 */
public class ScanView extends View {

	String text;// 显示的值
	float currentArc;// 当前的值(相对于360度)
	Paint mPaint;
	Context ctx;
	private Rect mBounds;

	public ScanView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		ctx = context;
		init();
	}

	public ScanView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScanView(Context context) {
		this(context, null);
	}

	private void init() {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStrokeWidth(1f);
		mPaint.setStyle(Style.STROKE);
		mPaint.setColor(ctx.getResources().getColor(R.color.green));
		mBounds = new Rect();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = getWidth();
		int height = getHeight();
		int littleWidth = 10;
		int radio = width > height ? height : width;
		// 外圆
		mPaint.setStrokeWidth(1f);
		canvas.drawCircle(width / 2, height / 2, radio / 2 - 5, mPaint);

		// 内圆
		mPaint.setStrokeWidth(5f);
		@SuppressLint("DrawAllocation")
		RectF oval = new RectF(littleWidth, littleWidth, width - littleWidth,
				height - littleWidth);
		canvas.drawArc(oval, -90f, currentArc, false, mPaint);

		// 测量字体的宽高
		mPaint.setTextSize(ctx.getResources().getDimension(R.dimen.y30));
		if (!TextUtils.isEmpty(text)) {
			mPaint.getTextBounds(text, 0, text.length(), mBounds);
			// 画字
			canvas.drawText(text, width / 2 - (mBounds.right - mBounds.left)
					/ 2, height / 2, mPaint);
		}
		super.onDraw(canvas);
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setCurrentArc(int currentArc) {
		this.currentArc = currentArc;
	}

	public void setTextArc(String text, float currentArc) {
		this.text = text;
		this.currentArc = currentArc;
		postInvalidate();
	}

}

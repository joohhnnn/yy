package com.txznet.music.widget;

import android.R.integer;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.GridView;

//自适应屏幕宽度并填充屏幕
public class MyGridView extends GridView {

	public MyGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setGravity(Gravity.CENTER);
	}

	public MyGridView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public MyGridView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int left = getPaddingLeft();
		int right = getPaddingRight();
		int top = getPaddingTop();
		int bottom = getPaddingBottom();

		int width = 0;
		int height = 0;
		if (widthMode == MeasureSpec.EXACTLY) {// 宽度是有
			width = getWidth();
			int width1 = getMeasuredWidth();
		}
		if (heightMode == MeasureSpec.EXACTLY) {
			height = getHeight();
			int height1 = getMeasuredHeight();
		}
		
		int childItemWidth=widthSize/150;
		setNumColumns(childItemWidth);

		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View view = getChildAt(i);
			
			int childMeasurewidth = view.getMeasuredWidth();
			int childwidth = view.getWidth();

			int childmeasureHeight = view.getMeasuredHeight();
			view.measure(150, 150);// 测量的宽高
		}

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
	}

}

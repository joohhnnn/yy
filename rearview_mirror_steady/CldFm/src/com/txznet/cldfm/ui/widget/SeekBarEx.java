package com.txznet.cldfm.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * 添加禁止滑动
 */
public class SeekBarEx extends SeekBar{

	public SeekBarEx(Context context) {
		super(context);
	}

	public SeekBarEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public SeekBarEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}
	
}

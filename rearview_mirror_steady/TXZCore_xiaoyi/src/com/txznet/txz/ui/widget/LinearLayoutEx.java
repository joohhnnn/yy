package com.txznet.txz.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class LinearLayoutEx extends LinearLayout{
	
	private OnSizeChangeListener mOnSizeChangeListener;

	public LinearLayoutEx(Context context) {
		this(context,null);
	}
	
	public LinearLayoutEx(Context context,AttributeSet attr){
		super(context,attr);
	}
	
	public void setOnSizeChangeListener(OnSizeChangeListener listener){
		mOnSizeChangeListener = listener;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if(mOnSizeChangeListener != null){
			mOnSizeChangeListener.onSizeChanged(w, h, oldw, oldh);
		}
	}
	
	public static interface OnSizeChangeListener{
		public void onSizeChanged(int w, int h, int oldw, int oldh);
	}
}

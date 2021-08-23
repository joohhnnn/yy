package com.txznet.nav.ui.widget;

import com.txznet.comm.remote.util.LogUtil;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class LinearLayoutEx extends LinearLayout{

	public LinearLayoutEx(Context context) {
		this(context,null);
	}
	
	public LinearLayoutEx(Context context,AttributeSet attr){
		super(context,attr);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		LogUtil.logd("onSizeChanged w:"+w+",h:"+h+",oldw:"+oldw+",oldh:"+oldh);
	}
}

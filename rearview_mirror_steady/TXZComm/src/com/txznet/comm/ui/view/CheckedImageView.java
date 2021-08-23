package com.txznet.comm.ui.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.txznet.comm.ui.IKeepClass;
import com.txznet.txz.comm.R;

public class CheckedImageView extends ImageView implements IKeepClass{
	private Drawable mOnDrawable;
	private Drawable mOffDrawable;

	public CheckedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public CheckedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public CheckedImageView(Context context) {
		super(context);
		initView(context);
	}

	private void initView(Context context) {
		mOnDrawable = getResources().getDrawable(R.drawable.asr_switch_on);
		mOffDrawable = getResources().getDrawable(R.drawable.asr_switch_off);
	}

	protected boolean checked;

	public void setChecked(boolean checked) {
		this.checked = checked;
		setImageDrawable(checked ? mOnDrawable : mOffDrawable);
	}
	
	public boolean isChecked(){
		return checked;
	}
}

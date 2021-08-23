package com.txznet.comm.ui.dialog;

import android.graphics.drawable.AnimationDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.txz.comm.R;

public class WinProgress extends WinDialog {
	private TextView mText;
	private AnimationDrawable mAnim;

	public WinProgress() {
		this("正在处理中...");
	}

	public WinProgress(String txt) {
		super();
		setText(txt);
	}

	public WinProgress(String txt, int drawableid) {
		super();
		setText(txt);
		setDrawableResouceId(drawableid);
	}

	public WinProgress(String txt, boolean isSystem) {
		super(isSystem);
		setText(txt);
	}

	Object mMessageData;

	protected WinProgress setMessageData(Object data) {
		mMessageData = data;
		return this;
	}

	public Object getMessageData() {
		return mMessageData;
	}

	public <T> T getMessageData(Class<T> cls) {
		return (T) mMessageData;
	}

	private int drawableId = 0;
	private ImageView mAnimImage;

	@Override
	protected View createView() {
		View context = LayoutInflater.from(getContext()).inflate(R.layout.comm_win_progress, null);
		mText = (TextView) context.findViewById(R.id.prgProgress_Text);
		mAnimImage = (ImageView) context.findViewById(R.id.imgProgress_Anim);
		if (drawableId != 0) {
			// 外部设置图片进入
			mAnimImage.setImageResource(drawableId);
			RotateAnimation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			mAnimImage.startAnimation(animation);
		} else if (mAnimImage.getDrawable() != null && mAnimImage.getDrawable() instanceof AnimationDrawable) {
			mAnim = (AnimationDrawable) mAnimImage.getDrawable();
			mAnim.start();
		}
		return context;
	}

	public WinProgress setDrawableID(int drawableId) {
		this.drawableId = drawableId;
		return this;
	}

	public void setText(String txt) {
		mText.setText(txt);
	}

	public void setDrawableResouceId(int drawableid) {
		this.drawableId = drawableid;
		mAnimImage.setImageResource(drawableId);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_ENTER){
			cancel();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
}

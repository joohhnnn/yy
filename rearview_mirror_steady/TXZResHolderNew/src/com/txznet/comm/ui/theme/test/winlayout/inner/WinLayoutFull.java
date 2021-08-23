package com.txznet.comm.ui.theme.test.winlayout.inner;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;

public class WinLayoutFull extends IWinLayout {

	private static WinLayoutFull sInstance = new WinLayoutFull();
	private IWinLayout mWinLayoutImpl;

	private WinLayoutFull() {
	}

	public static WinLayoutFull getInstance() {
		return sInstance;
	}

	/**
	 * 添加录音图标
	 */
	@Override
	public void addRecordView(View recordView) {
		// 这种形式的录音动画直接在聊天View里面的
		if (mWinLayoutImpl != null) {
			mWinLayoutImpl.addRecordView(recordView);
		}
	}

	@Override
	public Object removeLastView() {
		return mWinLayoutImpl.removeLastView();
	}

	@Override
	public View get() {
		return mWinLayoutImpl.get();
	}

	/**
	 * 添加View到对应的地方
	 */
	@Override
	public Object addView(int targetView, View view, ViewGroup.LayoutParams layoutParams) {
		if (mWinLayoutImpl != null) {
			mWinLayoutImpl.addView(targetView, view, layoutParams);
		}
		return null;
	}

	/**
	 * 释放内存，界面关闭时会调用
	 */
	@Override
	public void release() {
		if (mWinLayoutImpl != null) {
			mWinLayoutImpl.release();
			mWinLayoutImpl = null;
		}
	}

	/**
	 * 重置聊天记录，界面关闭时会调用
	 */
	@Override
	public void reset() {
		if (mWinLayoutImpl != null) {
			mWinLayoutImpl.reset();
		}
	}

	@Override
	public void init() {
		if (mWinLayoutImpl == null) {
			LogUtil.logd(WinLayout.logTag+"WinLayoutFull:"+"init weightRecord:");
			//if (ConfigUtil.getLayoutType() == ConfigUtil.LAYOUT_TYPE_HORIZONTAL) {
			if (WinLayout.isVertScreen) {
				WinLayoutFullV.getInstance().init();
				mWinLayoutImpl = WinLayoutFullV.getInstance();
			}else {
				WinLayoutFullH.getInstance().init();
				mWinLayoutImpl = WinLayoutFullH.getInstance();
			}
		}
	}

	@Override
	public void setBackground(Drawable drawable) {
		LogUtil.d("WinLayoutFull setBackground.");
		if(mWinLayoutImpl != null){
			mWinLayoutImpl.setBackground(drawable);
		}
	}

	@Override
	public void setBannerAdvertisingView(View view) {
		if(mWinLayoutImpl != null){
			mWinLayoutImpl.setBannerAdvertisingView(view);
		}
	}

	@Override
	public void removeBannerAdvertisingView() {
		if(mWinLayoutImpl != null){
			mWinLayoutImpl.removeBannerAdvertisingView();
		}
	}

	//设置引导语
	public void setGuideText(){
		//if (ConfigUtil.getLayoutType() == ConfigUtil.LAYOUT_TYPE_HORIZONTAL) {
		if (!WinLayout.isVertScreen) {
			WinLayoutFullH.getInstance().setmHintText();
		}else {
			WinLayoutFullV.getInstance().setmHintText();
		}
	}

}

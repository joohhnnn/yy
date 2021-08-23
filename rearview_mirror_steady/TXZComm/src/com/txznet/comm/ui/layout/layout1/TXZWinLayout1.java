package com.txznet.comm.ui.layout.layout1;

import com.txznet.comm.ui.layout.IWinLayout;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class TXZWinLayout1 extends IWinLayout{
	
	private static TXZWinLayout1 sInstance = new TXZWinLayout1();
	private TXZWinLayout1Impl mTrueLayout;
	private View mRecordView;
	
	private TXZWinLayout1() {
	}

	
	public static TXZWinLayout1 getInstance(){
		return sInstance;
	}

	
	private void createLayout() {
		mTrueLayout = new TXZWinLayout1Impl();
		mTrueLayout.init();
		if (mRecordView != null) {
			mTrueLayout.addRecordView(mRecordView);
		}
	}
	
	private void addRecordView(){
		if (mRecordView != null) {
			mTrueLayout.addRecordView(mRecordView);
		}
	}
	
	
	@Override
	public void release() {
		if (mTrueLayout != null) {
			mTrueLayout.reset();
			mTrueLayout.release();
			mTrueLayout = null;
			mRecordView = null;
		}
	}
	
	@Override
	public Object addView(int targetView, View view, LayoutParams layoutParams) {
		if (mTrueLayout == null) {
			createLayout();
		}
		return mTrueLayout.addView(targetView, view, layoutParams);
	}

	@Override
	public void reset() {
		mTrueLayout.reset();
	}

	@Override
	public void init() {
		super.init();
		TXZWinLayout1Impl.initHeight();
	}
	
	
	@Override
	public void addRecordView(View recordView) {
		mRecordView = recordView;
		if (mTrueLayout == null) {
			createLayout();
		}
		mTrueLayout.addRecordView(mRecordView);
	}
	
	@Override
	public View get() {
		if (mTrueLayout == null) {
			createLayout();
			addRecordView();
		}
		return mTrueLayout.get();
	}

	@Override
	public Object removeLastView() {
		if (mTrueLayout == null) {
			return null;
		}
		return mTrueLayout.removeLastView();
	}

	@Override
	public void updateContentMode(int mode) {
		if (mTrueLayout != null) {
			mTrueLayout.updateContentMode(mode);
		}
	}

	@Override
	public void setBackground(Drawable drawable) {
		if (mTrueLayout != null) {
			mTrueLayout.setBackground(drawable);
		}
	}

	@Override
	public void setBannerAdvertisingView(View view) {
		if(mTrueLayout != null){
			mTrueLayout.setBannerAdvertisingView(view);
		}
	}

	@Override
	public void removeBannerAdvertisingView() {
		if (mTrueLayout != null) {
			mTrueLayout.removeBannerAdvertisingView();
		}
	}
}

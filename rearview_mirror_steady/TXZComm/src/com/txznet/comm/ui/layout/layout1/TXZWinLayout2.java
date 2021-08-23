package com.txznet.comm.ui.layout.layout1;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.layout.IWinLayout;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class TXZWinLayout2 extends IWinLayout {

	private static TXZWinLayout2 sInstance = new TXZWinLayout2();
	private TXZWinLayout2Impl mTrueLayout;
	private View mRecordView;
	private static final String TAG = TXZWinLayout2.class.getSimpleName();

	private TXZWinLayout2() {
	}

	public static TXZWinLayout2 getInstance() {
		return sInstance;
	}

	private void createLayout() {
		mTrueLayout = new TXZWinLayout2Impl();
		mTrueLayout.init();
	}

	private void addRecordView(){
		if (mRecordView != null) {
			mTrueLayout.addRecordView(mRecordView);
		}
	}
	
	@Override
	public TXZWinLayout2Impl getTrueLayout(){
		return mTrueLayout;
	}
	
	@Override
	public void release() {
		LogUtil.logd(TAG + " release");
		if (mTrueLayout != null) {
			mTrueLayout.reset();
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
	public void init() {
		super.init();
		TXZWinLayout2Impl.initWeight();
	}

	@Override
	public void reset() {
		mTrueLayout.reset();
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
	public Object removeLastView() {
		if (mTrueLayout == null) {
			return null;
		}
		return mTrueLayout.removeLastView();
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
		if (mTrueLayout != null) {
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

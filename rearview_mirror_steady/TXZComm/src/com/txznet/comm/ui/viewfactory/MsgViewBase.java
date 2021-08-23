package com.txznet.comm.ui.viewfactory;

public abstract class MsgViewBase extends ViewBase {

	public IViewStateListener mViewStateListener;

	protected int mViewType = 0;

	public void setIViewStateListener(IViewStateListener viewStateListener) {
		this.mViewStateListener = viewStateListener;
	}

	public boolean hasViewAnimation() {
		return false;
	}

	public int getViewType() {
		return mViewType;
	}
}

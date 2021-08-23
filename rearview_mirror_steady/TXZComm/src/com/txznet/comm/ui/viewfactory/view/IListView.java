package com.txznet.comm.ui.viewfactory.view;

import android.annotation.SuppressLint;

import com.txznet.comm.ui.viewfactory.MsgViewBase;

@SuppressLint("NewApi")
public abstract class IListView extends MsgViewBase {

	public int mCurPage; // 当前页
	public int mMaxPage; // 总页数

	public abstract void updateProgress(int progress, int selection);

	public abstract void snapPage(boolean next);

	public int getCurPage() {
		return mCurPage;
	}

	public int getMaxPage() {
		return mMaxPage;
	}

	/**
	 * 列表项被选中
	 * @param selection 选中item的索引
	 */
	public abstract void updateItemSelect(int selection);
}

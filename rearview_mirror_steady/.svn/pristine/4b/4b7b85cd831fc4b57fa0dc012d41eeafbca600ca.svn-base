package com.txznet.record.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

public abstract class AbsLv extends ListView {

	protected IndexView mIndexView;

	public AbsLv(Context context) {
		super(context);
		super.setOnScrollListener(l);
	}

	public AbsLv(Context context, AttributeSet attr) {
		super(context, attr);
		super.setOnScrollListener(l);
	}

	public AbsLv(Context context, AttributeSet attr, int defValue) {
		super(context, attr, defValue);
		super.setOnScrollListener(l);
	}

	public void setIndexView(IndexView idv) {
		this.mIndexView = idv;
	}

	public int setPrePager() {
		int sel = 0;
		int preIndex = getFirstPos();
		sel = preIndex - getSnapCount();
		if (sel < 0 && preIndex == 0) {
			if (isViewTop()) {
				if (mOnPagerListener != null) {
					mOnPagerListener.onPrePager(-1);
				}
				return -1;
			}
			sel = 0;
		} else if (sel < 0) {
			sel = 0;
		}

		setSelection(sel);
		if (mIndexView != null) {
			mIndexView.onPrePager(sel);
		}
		if (mOnPagerListener != null) {
			mOnPagerListener.onPrePager(sel);
		}

		return sel;
	}

	public int setNextPager() {
		int sel = 0;
		int nextIndex = getLastPos();
		if ((nextIndex + 1) == getCount()) {
			if (isViewBottom()) {
				if (mOnPagerListener != null) {
					mOnPagerListener.onNextPager(-1);
				}
				return -1;
			}
		}
		sel = nextIndex + 1;
		setSelection(sel);
		if (mIndexView != null) {
			mIndexView.onNextPager(sel);
		}
		if (mOnPagerListener != null) {
			mOnPagerListener.onNextPager(sel);
		}
		return sel;
	}

	public int getSnapCount() {
		int preIndex = getFirstPos();
		int nextIndex = getLastPos();
		return nextIndex - preIndex + 1;
	}

	public int getFirstPos() {
		int pos = getFirstVisiblePosition();
		final View view = getChildAt(0);
		if (view != null && view.getTop() < 0 && Math.abs(view.getTop()) > getHalfHeight(view)) {
			pos++;
		}

		return pos;
	}

	public int getLastPos() {
		int pos = getLastVisiblePosition();
		final View view = getChildAt(getChildCount() - 1);
		if (view != null && view.getBottom() > (getHeight() + getHalfHeight(view))) {
			pos--;
		}
		return pos;
	}

	protected boolean isViewTop() {
		final View view = getChildAt(0);
		if (view != null) {
			return view.getTop() == 0;
		}
		return true;
	}

	protected boolean isViewBottom() {
		final View view = getChildAt(getChildCount() - 1);
		if (view != null) {
			int offset = (int) ((1.0f / 4.0f) * getHalfHeight(view));
			int overShot = view.getBottom() - getHeight();
			return Math.abs(overShot) <= offset;
		}
		return true;
	}

	protected boolean isLvTop() {
		return getFirstPos() == 0 && isViewTop();
	}

	protected boolean isLvBottom() {
		return (getLastPos() == getCount() - 1) && isViewBottom();
	}

	private int getHalfHeight(View view) {
		if (view != null) {
			int h = view.getHeight();
			return h / 2;
		}
		return 0;
	}

	OnScrollListener mOnScrollListener;

	public void setOnScrollListener(OnScrollListener osl) {
		this.mOnScrollListener = osl;
	}

	OnPagerListener mOnPagerListener;

	public void setOnPagerListener(OnPagerListener opl) {
		this.mOnPagerListener = opl;
	}

	private OnScrollListener l = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (mOnScrollListener != null) {
				mOnScrollListener.onScrollStateChanged(view, scrollState);
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			AbsLv.this.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			if (mOnScrollListener != null) {
				mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		}
	};

	protected void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

	}
}
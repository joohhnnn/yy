package com.txznet.txz.component.selector;

import java.util.ArrayList;
import java.util.List;

public class PageHelper implements PagerAdapter {

	private List mSrcLists;
	private List mDesLists;

	private Object[] mSrcObjs;
	private Object[] mDesObjs;

	protected int mCurPage;
	protected int mMaxPage;
	protected int mPageCount;
	protected boolean mUseNewSelector;

	protected OnListChangeListener mListener;

	public PageHelper(List list, int pageCount, boolean useNewSelector) {
		this.mSrcLists = list;
		this.mPageCount = pageCount;
		this.mUseNewSelector = useNewSelector;
		this.mDesLists = new ArrayList();
	}

	@Override
	public boolean nextPager() {
		int curPage = getCurPager();
		mCurPage = ++curPage;
		if (mCurPage > getMaxPager() - 1) {
			mCurPage = getMaxPager() - 1;
			return false;
		}

		if (!mUseNewSelector) {
			return true;
		}

		if (mListener != null) {
			mListener.onListChange(getSortList());
		}

		return true;
	}

	private List getSortList() {
		if (!mUseNewSelector) {
			return mSrcLists;
		}

		mDesLists.clear();
		int curPage = getCurPager();
		int sIndex = curPage * getPagerCount();
		int start = sIndex;
		for (int i = 0; i < getPagerCount(); i++) {
			if (start >= mSrcLists.size()) {
				break;
			}
			mDesLists.add(mSrcLists.get(start++));
		}

		return mDesLists;
	}

	public void onPageSelect(int page) {
		mCurPage = page - 1;
		if (!mUseNewSelector) {
			return;
		}
		
		if (mListener != null) {
			mListener.onListChange(getSortList());
		}
	}

	@Override
	public boolean prevPager() {
		int curPage = getCurPager();
		mCurPage = --curPage;
		if (curPage < 0) {
			mCurPage = 0;
			return false;
		}

		if (!mUseNewSelector) {
			return true;
		}

		if (mListener != null) {
			mListener.onListChange(getSortList());
		}

		return true;
	}

	@Override
	public int getCurPager() {
		return mCurPage;
	}

	@Override
	public int getMaxPager() {
		int pager = getPagerCount();
		if (pager < 1) {
			pager = 1;
		}

		int maxPage = mSrcLists.size() / pager;
		if (mSrcLists.size() % pager != 0) {
			maxPage++;
		}
		return maxPage;
	}

	@Override
	public int getPagerCount() {
		return mPageCount;
	}

	@Override
	public void reset() {
		mCurPage = 0;
		mMaxPage = 0;
	}

	@Override
	public void setOnListChangeListener(OnListChangeListener listener) {
		mListener = listener;
	}

	@Override
	public void onUpdate(List list, int showCount, boolean useNewSelector) {
		mSrcLists = list;
		mPageCount = showCount;
		mUseNewSelector = useNewSelector;
		reset();

		if (mListener != null) {
			mListener.onListChange(getSortList());
		}
	}
}
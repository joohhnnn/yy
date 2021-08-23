package com.txznet.txz.component.choice.page;

import java.util.ArrayList;
import java.util.List;

public abstract class ResListPage<E> extends ResourcePage<List<E>, E> {

	public ResListPage(List<E> resources) {
		super(resources, resources.size());
	}

	/**
	 * 拿不到所有数据情况下
	 * 
	 * @param totalSize
	 */
	public ResListPage(int totalSize) {
		super(null, totalSize);
	}

	@Override
	protected void clearCurrRes(List<E> currRes) {
		if (currRes != null) {
			currRes.clear();
		}
	}

	@Override
	protected List<E> notifyPage(int sIdx, int len, List<E> sourceRes) {
		if (mCurrRes == null) {
			mCurrRes = new ArrayList<E>();
		}

		if (sourceRes == null) {
			return mCurrRes;
		}

		mCurrRes.addAll(sourceRes.subList(sIdx, sIdx + len));
		return mCurrRes;
	}

	@Override
	protected abstract int numOfPageSize();

	@Override
	protected int getCurrResSize(List<E> currRes) {
		if (currRes != null) {
			return currRes.size();
		}
		return 0;
	}

	@Override
	public E getItemFromCurrPage(int idx) {
		if (mCurrRes != null) {
			if (mCurrRes.size() > idx && idx >= 0) {
				return mCurrRes.get(idx);
			}
		}
		return null;
	}

	@Override
	public E getItemFromSource(int idx) {
		if (mSourceRes != null) {
			if (mSourceRes.size() > idx && idx >= 0) {
				return mSourceRes.get(idx);
			}
		}
		return null;
	}
}

package com.txznet.txz.component.choice.repo;

import java.util.List;

public abstract class Repo<T> {
	protected OnRepoCallback<T> mCallback;

	public void inject(OnRepoCallback<T> callback) {
		mCallback = callback;
	}

	public abstract boolean nextPage();

	public abstract boolean lastPage();

	public abstract boolean selectPage(int page, boolean needVoice, boolean needData);
	
	public abstract void requestCurrPage();

	public abstract T removeFromSource(T t);

	public abstract void reset();

	public static interface OnRepoCallback<T> {
		void onGetList(List<T> list);

		void onNextPage(boolean bSucc);

		void onLastPage(boolean bSucc);

		void onSelectPage(boolean bSucc);
	}
}
package com.txznet.comm.ui;

import android.database.Observable;
import com.txznet.comm.ui.ScrollObservable.OnSizeObserver;

public class ScrollObservable extends Observable<OnSizeObserver> {

	public static interface OnSizeObserver {
		public void onResSize();
	}

	public void notifyChanged() {
		for (int i = mObservers.size() - 1; i >= 0; i--) {
			mObservers.get(i).onResSize();
		}
	}
}

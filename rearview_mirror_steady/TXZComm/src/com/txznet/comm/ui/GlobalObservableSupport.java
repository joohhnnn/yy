package com.txznet.comm.ui;

import com.txznet.comm.remote.GlobalContext;

public class GlobalObservableSupport {
	private static HomeObservable mHomeObservable;

	public static HomeObservable getHomeObservable() {
		if (GlobalContext.get() != null) {
			if (mHomeObservable == null) {
				synchronized (GlobalObservableSupport.class) {
					if (mHomeObservable == null) {
						mHomeObservable = new HomeObservable(GlobalContext.get());
					}
				}
			}
		}
		return mHomeObservable;
	}

	private static ScrollObservable mScrollObservable;

	public static ScrollObservable getScrollObservable() {
		if (mScrollObservable == null) {
			synchronized (GlobalObservableSupport.class) {
				if (mScrollObservable == null) {
					mScrollObservable = new ScrollObservable();
				}
			}
		}
		return mScrollObservable;
	}

	private static WinRecordObserver mWinRecordObserver;

	public static WinRecordObserver getWinRecordObserver() {
		if (mWinRecordObserver == null) {
			synchronized (GlobalObservableSupport.class) {
				if (mWinRecordObserver == null) {
					mWinRecordObserver = new WinRecordObserver();
				}
			}
		}
		return mWinRecordObserver;
	}

	private static ReverseObservable mRevereObservable;
	public static ReverseObservable getRevereObservable() {
		if (mRevereObservable == null) {
			synchronized (GlobalObservableSupport.class) {
				if (mRevereObservable == null) {
					mRevereObservable = new ReverseObservable();
				}
			}
		}
		return mRevereObservable;
	}
}

package com.txznet.record;

import android.database.Observable;

public class WinRecordObserver extends Observable<WinRecordObserver.WinRecordCycleObserver> {
	public static interface WinRecordCycleObserver {

		public void getFocus();

		public void show();

		public void loseFocus();

		public void dismiss();

	}

	public void onGetFocus() {
		synchronized (mObservers) {
			for (int i = mObservers.size() - 1; i >= 0; i--) {
				mObservers.get(i).getFocus();
			}
		}
	}

	public void onShow() {
		synchronized (mObservers) {
			for (int i = mObservers.size() - 1; i >= 0; i--) {
				mObservers.get(i).show();
			}
		}
	}

	public void onLoseFocus() {
		synchronized (mObservers) {
			for (int i = mObservers.size() - 1; i >= 0; i--) {
				mObservers.get(i).loseFocus();
			}
		}
	}

	public void onDismiss() {
		synchronized (mObservers) {
			for (int i = mObservers.size() - 1; i >= 0; i--) {
				mObservers.get(i).dismiss();
			}
		}
	}
}

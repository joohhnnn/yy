package com.txznet.txz.ui.win.login;

import com.txznet.txz.ui.win.BaseContract;

public interface ViewContract {

	interface View extends BaseContract.View {
		void showSimPager();

		void showLoginPager();

		boolean isShowing();

		void show();

		void dismiss();
	}

	interface Presenter extends BaseContract.Presenter<View> {
	}
}
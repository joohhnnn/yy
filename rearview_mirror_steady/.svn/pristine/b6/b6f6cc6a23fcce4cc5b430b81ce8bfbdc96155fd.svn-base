package com.txznet.txz.ui.win.login;

import com.txznet.txz.ui.win.BaseContract;

/**
 * Created by TXZ-METEORLUO on 2017/5/18.
 */
public interface LoginContract {

	interface View extends BaseContract.View {
		void showPhoneError();

		void showIdCodeError();

		void showPhoneErrorWithouToast();

		void showIdCodeErrorWithoutToast();

		void clearAllError();

		void resetCountDown();

		void inputPhone(String phone);

		void inputCode(String code);

		void showToast(String txt);

		void startCountDown(int time);

		void toastErrorWithOpenGuide(String txt);
	}

	interface Presenter extends BaseContract.Presenter<View> {

		void doLogin(String phone, String code);

		void getIdCodeByPhone(String phone);

		int getRequestCRCount();

		boolean incErrorJump();
	}
}

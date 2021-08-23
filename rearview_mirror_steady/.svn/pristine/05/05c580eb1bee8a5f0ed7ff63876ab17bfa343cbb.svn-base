package com.txznet.txz.module.login;

import com.txz.equipment_manager.EquipmentManager.Resp_Verification_Code;
import com.txznet.txz.module.account.AccountManager;

/**
 * Created by TXZ-METEORLUO on 2017/5/19.
 */
public class LoginManagerImpl {

	public static abstract class OnGetVerifyCodeListener {
		public int req_session;

		public abstract void onGetVerifyCode(Resp_Verification_Code code);

		public void onVerifyCode(int retCode, String reson) {
		}
	}

	private static LoginManagerImpl sInstance = new LoginManagerImpl();

	public static LoginManagerImpl getInstance() {
		return sInstance;
	}

	public void login(String phone, String idcode, OnGetVerifyCodeListener listener) {
		AccountManager.getInstance().requestRegister(phone, idcode, listener);
	}

	public String getIdCode(String phone, OnGetVerifyCodeListener listener) {
		AccountManager.getInstance().requestVerificationCode(phone, listener);
		return null;
	}

	public String getCachePhone() {
		return AccountManager.getInstance().getUserPhone();
	}
}
package com.txznet.txz.ui.win.login;

import com.txz.equipment_manager.EquipmentManager;
import com.txz.equipment_manager.EquipmentManager.Resp_Verification_Code;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.account.AccountManager;
import com.txznet.txz.module.login.LoginManagerImpl;
import com.txznet.txz.module.login.LoginManagerImpl.OnGetVerifyCodeListener;

import android.text.TextUtils;

/**
 * Created by TXZ-METEORLUO on 2017/5/18.
 */
public class LoginPresenter implements LoginContract.Presenter {
	LoginContract.View mView;
	// 标记请求验证码的次数
	private int mReqCRCount;
	private int mInputCodeErrorCount;

	private OnGetVerifyCodeListener mVerifyListener = new OnGetVerifyCodeListener() {

		@Override
		public void onGetVerifyCode(Resp_Verification_Code code) {
			if (code != null && code.time != null) {
				mView.startCountDown(code.time);
			}
		}

		@Override
		public void onVerifyCode(int retCode, String reson) {
			if (mView == null) {
				return;
			}

			if (retCode != EquipmentManager.EC_CODE_OK) {
				switch (retCode) {
				case EquipmentManager.EC_CODE_ERROR:
					if (incErrorJump()) {
						// 验证码输入3次错误，延迟3S跳过
						mView.toastErrorWithOpenGuide(NativeData.getResString("RS_VOICE_LOGIN_ERROR_JUMP"));
						break;
					}
				case EquipmentManager.EC_CODE_TIME:
					mView.showToast(reson);
					mView.showIdCodeErrorWithoutToast();
					break;
				default:
					if (!TextUtils.isEmpty(reson)) {
						mView.showToast(reson);
					}
					mView.clearAllError();
					break;
				}
				
//				AppLogic.runOnUiGround(new Runnable() {
//					public void run() {
//						// 提示错误Tips并把倒计时清掉
//						mView.resetCountDown();
//					}
//				}, 0);
			} else {
				// 成功后清空错误次数
				mReqCRCount = 0;
			}
		}
	};
	
	@Override
	public boolean incErrorJump() {
		++mInputCodeErrorCount;
		return mInputCodeErrorCount >= 3 
				&& AccountManager.getInstance().canSkipLogin();
	}
	
	@Override
	public void doLogin(String phone, String code) {
		LoginManagerImpl.getInstance().login(phone, code, mVerifyListener);
	}

	@Override
	public void getIdCodeByPhone(String phone) {
		mReqCRCount++;
		String code = LoginManagerImpl.getInstance().getIdCode(phone, mVerifyListener);
		inputCode(code);
	}

	@Override
	public int getRequestCRCount() {
		return mReqCRCount;
	}

	private void inputCode(String code) {
		if (!TextUtils.isEmpty(code)) {
			if (mView != null) {
				mView.inputCode(code);
			}
		}
	}

	@Override
	public void onAttachView(LoginContract.View view) {
		mView = view;

		String phone = LoginManagerImpl.getInstance().getCachePhone();
		if (!TextUtils.isEmpty(phone)) {
			mView.inputPhone(phone);
		}
	}

	@Override
	public void onDetachView() {
		mView = null;
		mReqCRCount = 0;
	}
}

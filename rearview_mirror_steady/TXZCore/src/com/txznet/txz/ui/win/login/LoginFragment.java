package com.txznet.txz.ui.win.login;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.account.AccountManager;
import com.txznet.txz.ui.win.BaseFragment;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by TXZ-METEORLUO on 2017/5/18.
 */
public class LoginFragment extends BaseFragment<LoginContract.Presenter>
		implements LoginContract.View, View.OnClickListener, OnFocusChangeListener {
	public static int DEFAULT_COUNTDOWN = 60;

	EditText mPhoneEt;
	LinearLayout mPhoneLy;

	LinearLayout mCodeLy;
	EditText mCodeEt;
	TextView mCodeTv;
	TextView mLoginTv;
	TextView mSmTv;
	TextView mDisclaimerTv;
	TextView mBackTv;
	WebView mWebView;
	View mLoginLy;
	View mDisclaimerLy;

	int mCountNow;
	boolean mCountEnd;

	@Override
	public int getLayoutId() {
		return R.layout.fragment_login_ly;
	}

	@Override
	public void onViewCreated(View view) {
		mPhoneEt = (EditText) view.findViewById(R.id.phone_et);
		mPhoneLy = (LinearLayout) view.findViewById(R.id.iphone_ly);
		mCodeLy = (LinearLayout) view.findViewById(R.id.code_ly);
		mCodeEt = (EditText) view.findViewById(R.id.idcode_et);
		mCodeTv = (TextView) view.findViewById(R.id.getCode_tv);
		mLoginTv = (TextView) view.findViewById(R.id.login_tv);
		mSmTv = (TextView) view.findViewById(R.id.saomao_tv);
		mDisclaimerTv = (TextView) view.findViewById(R.id.txz_disclaimer_tv);
		mWebView = (WebView) view.findViewById(R.id.webView);
		mBackTv = (TextView) view.findViewById(R.id.back_tv);
		mLoginLy = view.findViewById(R.id.login_ly);
		mDisclaimerLy = view.findViewById(R.id.disclaimer_ly);

		mCodeTv.setOnClickListener(this);
		mLoginTv.setOnClickListener(this);
		mDisclaimerTv.setOnClickListener(this);
		mBackTv.setOnClickListener(this);

		mPhoneEt.setOnFocusChangeListener(this);
		mCodeEt.setOnFocusChangeListener(this);
		
		mWebView.setBackgroundColor(Color.TRANSPARENT);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (v == mPhoneEt && !hasFocus) {
			clearPhoneError();
		} else if (v == mCodeEt && !hasFocus) {
			clearIdCodeError();
		}
	}

	@Override
	public LoginContract.Presenter createPresenter() {
		return new LoginPresenter();
	}

	@Override
	public void showPhoneError() {
		showPhoneErrorWithouToast();
		showToast(getString(R.string.string_input_correct_phone));
	}
	
	@Override
	public void showPhoneErrorWithouToast() {
		mPhoneEt.requestFocus();
		mPhoneEt.setTextColor(getColor(R.color.login_input_error));
		mPhoneLy.setEnabled(false);
	}

	@Override
	public void showIdCodeError() {
		showIdCodeErrorWithoutToast();
		showToast(getString(R.string.string_input_correct_code));
	}
	
	@Override
	public void showIdCodeErrorWithoutToast() {
		mCodeEt.requestFocus();
		mCodeEt.setTextColor(getColor(R.color.login_input_error));
		mCodeLy.setEnabled(false);
	}
	
	@Override
	public void configureView() {
		super.configureView();
		mWebView.loadUrl("file:///android_asset/disclaimer.html");
	}

	/**
	 * 进入免责声明界面
	 */
	private void showDisclaimer() {
		mLoginLy.setVisibility(View.GONE);
		mDisclaimerLy.setVisibility(View.VISIBLE);
	}

	/**
	 * 显示登录界面
	 */
	private void showLogin() {
		mDisclaimerLy.setVisibility(View.GONE);
		mLoginLy.setVisibility(View.VISIBLE);
	}

	private void clearPhoneError() {
		mPhoneEt.setTextColor(Color.WHITE);
		mPhoneLy.setEnabled(true);
	}

	private void clearIdCodeError() {
		mCodeEt.setTextColor(Color.WHITE);
		mCodeLy.setEnabled(true);
	}

	@Override
	public void clearAllError() {
		clearPhoneError();
		clearIdCodeError();
	}
	
	Toast mToast = null;

	@Override
	public void showToast(String txt) {
		if (mToast != null) {
			mToast.cancel();
		}
		LogUtil.logd("toast:" + txt);
		mToast = Toast.makeText(getContext(), txt, Toast.LENGTH_LONG);
		mToast.show();
	}

	@Override
	public void inputPhone(String phone) {
		mPhoneEt.setText(phone);
		mPhoneEt.setSelection(mPhoneEt.getText().length());
	}

	@Override
	public void inputCode(String code) {
		mCodeEt.setText(code);
	}

	@Override
	public void onClick(View v) {
		if (v == mLoginTv) {
			doLogin();
		} else if (v == mCodeTv) {
			checkToGetIdCode();
		} else if (v == mDisclaimerTv) {
			showDisclaimer();
		} else if (v == mBackTv) {
			showLogin();
		}
	}
	
	/**
	 * 检测是否可以跳过验证码，直接登录
	 * 
	 * @return
	 */
	private boolean enableJumpLogin() {
		String phone = mPhoneEt.getText().toString();
		if (TextUtils.isEmpty(phone) || !AccountManager.isMobile(phone)) {
			return false;
		}

		// 如果请求验证码两次没有返回并且计时已经结束，允许跳过
		if (canSkip() && mCountEnd) {
			return true;
		}
		return false;
	}
	
	private boolean checkLegalAccessLogin() {
		String phone = mPhoneEt.getText().toString();
		String idCode = mCodeEt.getText().toString();
		if (TextUtils.isEmpty(phone) || !AccountManager.isMobile(phone)) {
			showPhoneError();
			return false;
		}
		AccountManager.getInstance().saveUserPhone(phone);

		if (TextUtils.isEmpty(idCode)) {
			if (mPresenter.incErrorJump()) {
				// 验证码输入3次错误，延迟3S跳过
				toastErrorWithOpenGuide(NativeData.getResString("RS_VOICE_LOGIN_ERROR_JUMP"));
				return false;
			}
			showIdCodeError();
			return false;
		}
		return true;
	}
	
	/**
	 * Toast错误并进行下一步
	 * @param toastTxt
	 */
	@Override
	public void toastErrorWithOpenGuide(String toastTxt) {
		showToast(toastTxt);
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				AccountManager.getInstance().openGuideApp();
			}
		}, 3000);
	}
	
	private void doLogin() {
		String phone = mPhoneEt.getText().toString();
		// 用户输入可以跳过验证的密钥
		if (!TextUtils.isEmpty(phone) && phone.equals(AccountManager.JUMP_PHONE)) {
			LoginView.getInstance(GlobalContext.get()).dismiss();
			return;
		}
		
		if (enableJumpLogin()) {
			// 先跳过登录，下次开机继续提示
			AccountManager.getInstance().openGuideApp();
		} else {
			if (checkLegalAccessLogin()) {
				clearAllError();
				if (mPresenter != null) {
					String code = mCodeEt.getText().toString();
					mPresenter.doLogin(phone.replaceAll("\\s", ""), code.replaceAll("\\s", ""));
				}
			}
		}
	}

	/**
	 * 点击获取验证码
	 */
	private void checkToGetIdCode() {
		String phone = mPhoneEt.getText().toString();
		if (TextUtils.isEmpty(phone) || !AccountManager.isMobile(phone)) {
			showPhoneError();
			return;
		} else {
			clearPhoneError();
		}

		if (mCountNow > 0) {
			// 倒计时未完
			return;
		}

		if (mPresenter != null) {
			mPresenter.getIdCodeByPhone(mPhoneEt.getText().toString());
			// 开始默认1分钟计时
			mCountEnd = false;
			mCodeTv.setEnabled(false);
			AppLogic.removeBackGroundCallback(mStartCountDownTask);
			AppLogic.runOnBackGround(mStartCountDownTask, 2000);
		}
	}

	Runnable mStartCountDownTask = new Runnable() {

		@Override
		public void run() {
			startCountDown(DEFAULT_COUNTDOWN);
		}
	};
	
	private boolean canSkip() {
		return mPresenter != null && mPresenter.getRequestCRCount() >= 2 && AccountManager.getInstance().canSkipLogin();
	}

	/**
	 * 还原获取验证码
	 */
	@Override
	public void resetCountDown() {
		AppLogic.removeBackGroundCallback(mStartCountDownTask);
		mCodeTv.setText(getString(R.string.string_get_code));
		mCodeTv.setEnabled(true);
		mCountEnd = true;

		// 倒计时结束后提示3s跳过
		if (canSkip()) {
			toastErrorWithOpenGuide(NativeData.getResString("RS_VOICE_LOGIN_ERROR_GET_CODE"));
		}
	}
	
	Runnable mCountDownTask = new Runnable() {

		@Override
		public void run() {
			if (mCountNow == 0) {
				resetCountDown();
				AppLogic.removeUiGroundCallback(this);
				return;
			}
			mCountNow--;
			mCodeTv.setText(mCountNow + "");
			mCodeTv.setEnabled(false);
			AppLogic.removeUiGroundCallback(this);
			AppLogic.runOnUiGround(this, 1000);
		}
	};

	@Override
	public void onDestory() {
		clearAllError();
		mCodeTv.setText(getString(R.string.string_get_code));
	}

	/**
	 * 开启倒计时
	 */
	@Override
	public void startCountDown(int time) {
		// 如果验证码带时间，则以下发的时间为准
		AppLogic.removeBackGroundCallback(mStartCountDownTask);
		mCountNow = time;
		mCountEnd = false;
		AppLogic.removeUiGroundCallback(mCountDownTask);
		AppLogic.runOnUiGround(mCountDownTask, 0);
	}
}
package com.txznet.txz.ui.win.login;

import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.account.AccountManager;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.login.SimNetManagerImpl;
import com.txznet.txz.module.login.SimNetManagerImpl.OnSimNetChangeListener;
import com.txznet.txz.ui.win.BaseFragment;
import com.txznet.txz.ui.win.login.ViewContract.View;
import com.txznet.txz.util.DeviceUtil;

import android.content.Context;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

public class LoginView extends WinDialog implements View, OnSimNetChangeListener {

	public static interface BaseView {
		void onCreate(Context context);

		android.view.View getView();

		void configureView();

		void onDestory();
	}

	private volatile boolean mIsShowing;
	private LoginFragment mLoginFragment;
	private SimCheckFragment mSimCheckFragment;
	private BaseFragment mCurrFragment;

	private static LoginView sView;

	public static LoginView getInstance(Context context) {
		if (sView == null) {
			synchronized (LoginView.class) {
				if (sView == null) {
					DialogBuildData data = new DialogBuildData();
					data.setWindowType(WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 2);
					data.setCancelable(false);
					sView = new LoginView(data);
				}
			}
		}
		return sView;
	}

	public LoginView(DialogBuildData context) {
		super(context);
	}

	private void initView() {
		releaseView();
	}

	private void releaseView() {
		if (mSimCheckFragment != null) {
			mSimCheckFragment.onDestory();
		}
		if (mLoginFragment != null) {
			mLoginFragment.onDestory();
		}

		mLoginFragment = null;
		mSimCheckFragment = null;
	}

	@Override
	public void onShow() {
		if (mIsShowing) {
			configureView();
			return;
		}
		mIsShowing = true;
		SimNetManagerImpl.getInstance().registerListener(this);
		initView();

		showInnerView();
	}
	
	/**
	 * 刷新显示的数据
	 */
	private void configureView() {
		if (mCurrFragment != null) {
			mCurrFragment.configureView();
		}
	}

	/**
	 * 先将界面关掉
	 */
	public void goWifiSettings() {
		// 跳转到wifi界面
		PackageManager.getInstance().goWifiSettings();
		super.dismiss("");
		checkReshowDialog();
	}

	/**
	 * 检测当前是否退出了设置界面
	 */
	private void checkReshowDialog() {
		AppLogic.removeUiGroundCallback(mReshowTask);
		AppLogic.runOnUiGround(mReshowTask, 1000);
	}

	Runnable mReshowTask = new Runnable() {

		@Override
		public void run() {
			if (!PackageManager.getInstance().isWifiSettings()) {
				AppLogic.removeUiGroundCallback(this);
				if (!isShowing()) {
					return;
				}
				// 如果不在WIFI设置界面，尝试恢复对话框
				reshowDialog();
				return;
			}

			AppLogic.removeUiGroundCallback(this);
			AppLogic.runOnUiGround(this, 1000);
		}
	};

	/**
	 * 重新将界面唤醒
	 */
	public void reshowDialog() {
		if (!isShowing()) {
			// 被关掉后不再恢复显示
			return;
		}
		super.show();
	}
	
	@Override
	public void onBackPressed() {
		// 屏蔽返回键
	}

	@Override
	public boolean isShowing() {
		return mIsShowing;
	}

	@Override
	public void dismiss() {
		if (mIsShowing) {
			mIsShowing = false;
			releaseView();
			SimNetManagerImpl.getInstance().unRegisterListener(this);
			SimNetManagerImpl.getInstance().release();
			AppLogic.removeUiGroundCallback(mReshowTask);
			AppLogic.removeBackGroundCallback(mAutoSkipTask);
			sView = null;
		}
		super.dismiss("");
	}

	@Override
	public void showSimPager() {
		if (mCurrFragment != null && mSimCheckFragment != null && mCurrFragment == mSimCheckFragment) {
			return;
		}

		getContentView().removeAllViewsInLayout();
		if (mSimCheckFragment == null) {
			mSimCheckFragment = new SimCheckFragment();
			mSimCheckFragment.onCreate(getContext());
		}
		mCurrFragment = mSimCheckFragment;
		mSimCheckFragment.configureView();
		getContentView().addView(mSimCheckFragment.getView());
	}

	@Override
	public void showLoginPager() {
		if (mCurrFragment != null && mLoginFragment != null && mCurrFragment == mLoginFragment) {
			return;
		}

		getContentView().removeAllViewsInLayout();
		if (mLoginFragment == null) {
			mLoginFragment = new LoginFragment();
			mLoginFragment.onCreate(getContext());
		}
		mCurrFragment = mLoginFragment;
		mLoginFragment.configureView();
		getContentView().addView(mLoginFragment.getView());
	}

	protected FrameLayout getContentView() {
		if (mView != null && mView instanceof FrameLayout) {
			return (FrameLayout) mView;
		}
		return null;
	}

	@Override
	protected android.view.View createView() {
		FrameLayout fLayout = new FrameLayout(getContext());
		fLayout.setBackgroundResource(R.drawable.login_gradient_bg);
		return fLayout;
	}

	@Override
	public void onChange(int status) {
		showInnerView();
		AppLogic.removeBackGroundCallback(mAutoSkipTask);
		checkAutoJump(status);
	}
	
	private void checkAutoJump(int status) {
		// 如果没有连接WIFI并且SIM卡不可用
		boolean canSkip = !DeviceUtil.isWifiConnect(getContext()) && !DeviceUtil.isSimAvaible()
				&& AccountManager.getInstance().canSkipLogin();
		// 如果没有连接WIFI并且SIM卡检测出网络不可访问
		boolean netError = !DeviceUtil.isWifiConnect(getContext()) && status == OnSimNetChangeListener.STATE_NET_ERROR
				&& AccountManager.getInstance().canSkipLogin();
		if (canSkip || netError) {
			autoSkipLogin();
		}
	}
	
	private void autoSkipLogin() {
		AppLogic.removeBackGroundCallback(mAutoSkipTask);
		AppLogic.runOnBackGround(mAutoSkipTask, 20 * 1000);
	}
	
	Runnable mAutoSkipTask = new Runnable() {

		@Override
		public void run() {
			Toast.makeText(getContext(), NativeData.getResString("RS_TIPS_LOGIN_ERROR_AUTO_JUMP"), Toast.LENGTH_LONG)
					.show();
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					AccountManager.getInstance().openGuideApp();
				}
			}, 3000);
		}
	};

	private void showInnerView() {
		// 正在检测
		if (SimNetManagerImpl.getInstance().isChecking()) {
			showSimPager();
		} else {
			showLoginPager();
		}
	}

	@Override
	public String getReportDialogId() {
		return "Login_Dialog";
	}
}
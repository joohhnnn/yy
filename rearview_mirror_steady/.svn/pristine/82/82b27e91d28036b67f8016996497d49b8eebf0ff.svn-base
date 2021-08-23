package com.txznet.txz.ui.win.login;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.module.login.SimNetManagerImpl;
import com.txznet.txz.module.login.SimNetManagerImpl.OnSimNetChangeListener;

/**
 * Created by TXZ-METEORLUO on 2017/5/18.
 */
public class SimPresenter implements SimContract.Presenter {

	SimContract.View mView;

	private OnSimNetChangeListener mListener = new OnSimNetChangeListener() {
		@Override
		public void onChange(int status) {
			switch (status) {
			case STATE_CHECKING:
				if (mView != null) {
					mView.showNetChecking();
				}
				break;
			case STATE_HAVE_SIM:
				if (mView != null) {
					mView.showSimDone();
				}
				break;
			case STATE_NET_ERROR:
				if (mView != null) {
					mView.showNetError();
				}
				break;
			case STATE_NET_ALL_WELL:
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						if (LoginView.getInstance(GlobalContext.get()).isShowing()) {
							LoginView.getInstance(GlobalContext.get()).showLoginPager();
						}
					}
				}, 0);
				break;
			case STATE_NET_WELL:
				if (mView != null) {
					mView.showNetWell();
					AppLogic.runOnUiGround(new Runnable() {

						@Override
						public void run() {
							if (LoginView.getInstance(GlobalContext.get()).isShowing()) {
								LoginView.getInstance(GlobalContext.get()).showLoginPager();
							}
						}
					}, 2000);
				}
				break;
			case STATE_NO_SIM:
				if (mView != null) {
					mView.showNoSim();
				}
				break;
			}
		}
	};

	@Override
	public void onAttachView(SimContract.View view) {
		mView = view;
		SimNetManagerImpl.getInstance().registerListener(mListener);
		SimNetManagerImpl.getInstance().requestState();
	}

	@Override
	public void onDetachView() {
		mView = null;
		SimNetManagerImpl.getInstance().unRegisterListener(mListener);
	}
}
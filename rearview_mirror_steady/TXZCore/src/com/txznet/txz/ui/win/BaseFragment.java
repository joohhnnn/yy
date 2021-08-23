package com.txznet.txz.ui.win;

import com.txznet.txz.ui.win.login.LoginView.BaseView;

import android.content.Context;
import android.view.View;

/**
 * Created by TXZ-METEORLUO on 2017/5/18.
 */
public abstract class BaseFragment<P extends BaseContract.Presenter> implements BaseView, BaseContract.View {

	protected P mPresenter;
	protected View mContentView;

	@Override
	public void onCreate(Context context) {
		mContentView = View.inflate(context, getLayoutId(), null);
		onViewCreated(mContentView);
	}

	@Override
	public void configureView() {
		if (mPresenter == null) {
			mPresenter = createPresenter();
		}
		mPresenter.onAttachView(this);
	}

	@Override
	public void onDestory() {
		if (mPresenter != null) {
			mPresenter.onDetachView();
		}

		mContentView = null;
	}

	@Override
	public View getView() {
		return mContentView;
	}

	protected void onViewCreated(View view) {

	}

	public View findViewById(int id) {
		return mContentView.findViewById(id);
	}

	public String getString(int id) {
		return mContentView.getContext().getString(id);
	}

	public int getColor(int id) {
		return mContentView.getContext().getResources().getColor(id);
	}

	public Context getContext() {
		return mContentView.getContext();
	}

	public abstract int getLayoutId();

	public abstract P createPresenter();
}
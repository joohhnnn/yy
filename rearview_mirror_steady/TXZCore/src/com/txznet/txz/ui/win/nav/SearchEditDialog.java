package com.txznet.txz.ui.win.nav;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable.HomeObserver;
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.viewfactory.view.ISearchEditView;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.component.choice.list.PoiWorkChoice;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONObject;

@SuppressLint("NewApi")
public class SearchEditDialog extends SearchEditDialogBase {

	public static class SearchEditDialogBuildData extends DialogBuildData{
		public ISearchEditView mSearchEditView;
		public int mIntWhere;
		public String mKey;
		public String mCity;

		public SearchEditDialogBuildData setCity(String mCity) {
			this.mCity = mCity;
			return this;
		}

		public SearchEditDialogBuildData setIntWhere(int mIntWhere) {
			this.mIntWhere = mIntWhere;
			return this;
		}

		public SearchEditDialogBuildData setKey(String mKey) {
			this.mKey = mKey;
			return this;
		}

		public SearchEditDialogBuildData setSearchEditView(ISearchEditView mSearchEditView) {
			this.mSearchEditView = mSearchEditView;
			return this;
		}

		@Override
		public void check() {
			super.check();
			setFullScreen(mFullScreen);
			setWindowType(mWindowType);
			setWindowFlag(mWindowFlag);
		}
	}

	private SearchEditDialog(SearchEditDialogBuildData data) {
		super(data);
	}

	@Override
	protected View createView() {
		return ((SearchEditDialogBuildData)mBuildData).mSearchEditView.getView(null).view;
	}

	private volatile boolean mNeedCloseDialog = true;


	public static SearchEditDialog naviDefault(String keyWord, String city) {
		SearchEditDialog searchEditDialog = createDialog(keyWord,city,SearchEditManager.LOCATION_NONE);
		searchEditDialog.show();
		return searchEditDialog;
	}

	public static SearchEditDialog naviHome(String keyWord, String city) {
		SearchEditDialog searchEditDialog = createDialog(keyWord,city,SearchEditManager.LOCATION_HOME);
		searchEditDialog.show();
		return searchEditDialog;
	}

	public static SearchEditDialog naviCompany(String keyWord, String city) {
		SearchEditDialog searchEditDialog = createDialog(keyWord,city,SearchEditManager.LOCATION_COMPANY);
		searchEditDialog.show();
		return searchEditDialog;
	}

	public static SearchEditDialog naviJingYou(String keyWord, String city) {
		SearchEditDialog searchEditDialog = createDialog(keyWord,city,SearchEditManager.LOCATION_JINGYOU);
		searchEditDialog.show();
		return searchEditDialog;
	}
	
	public static SearchEditDialog naviEnd(String keyWord, String city) {
		SearchEditDialog searchEditDialog = createDialog(keyWord,city,SearchEditManager.LOCATION_END);
		searchEditDialog.show();
		return searchEditDialog;
	}
	private static SearchEditDialog sSearchEditDialog;
	private static SearchEditDialog createDialog(String keyWord, String city,int where) {
		SearchEditDialogBuildData searchEditDialogBuildData = null;
		LogUtil.loge("honge sSearchEditDialog == null " + (sSearchEditDialog == null));
		if (sSearchEditDialog == null) {
			searchEditDialogBuildData = new SearchEditDialogBuildData()
					.setSearchEditView(WinLayoutManager.getInstance().getSearchEditView())
					.setCity(city)
					.setIntWhere(where)
					.setKey(keyWord);
			sSearchEditDialog = new SearchEditDialog(searchEditDialogBuildData);
		} else {
			searchEditDialogBuildData = (SearchEditDialogBuildData) sSearchEditDialog.mBuildData;
			searchEditDialogBuildData.setCity(city);
			searchEditDialogBuildData.setIntWhere(where);
			searchEditDialogBuildData.setSearchEditView(WinLayoutManager.getInstance().getSearchEditView());
			searchEditDialogBuildData.setKey(keyWord);
		}
		return sSearchEditDialog;
	}

	@Override
	protected void onStart() {
		super.onStart();
		((SearchEditDialogBuildData)mBuildData).mSearchEditView.onStart(((SearchEditDialogBuildData)mBuildData).mKey);

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			onBackPressed();
			return true;
		default:
			break;
		}
		return false;
	}

	public void setNeedCloseDialog(boolean isClose) {
		this.mNeedCloseDialog = isClose;
	}

	public void dismiss() {
		if (!mNeedCloseDialog) {
			return;
		}

		if (isShowing()) {
			mNeedCloseDialog = true;
			((SearchEditDialogBuildData)mBuildData).mSearchEditView.onDismiss();
			super.dismiss("");
		}

		if (mRegisted) {
			mRegisted = false;
			try {
				GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeObserver);
			}catch (Exception e) {

			}
		}
	}

	private boolean mRegisted;
	
	@Override
	public void onShow() {
		super.show();
		mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		if (!mRegisted) {
			mRegisted = true;
			GlobalObservableSupport.getHomeObservable().registerObserver(mHomeObserver);
		}
	}

	private HomeObserver mHomeObserver = new HomeObserver() {
		@Override
		public void onHomePressed() {
			// 在编辑界面按HOME键RecoderWin状态错乱问题
			onBackPressed();
			RecorderWin.dismiss();
		}
	};

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		ChoiceManager.getInstance().selectBackAsr();
		mNeedCloseDialog = true;
		dismiss();
	}

	@Override
	public String getReportDialogId() {
		return "SearchEditDialog";
	}
}
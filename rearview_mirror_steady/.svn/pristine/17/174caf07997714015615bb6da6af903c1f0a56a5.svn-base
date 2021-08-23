package com.txznet.txz.ui.win.nav;

import java.util.ArrayList;
import java.util.List;

import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable.HomeObserver;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.viewfactory.data.SelectCityViewData;
import com.txznet.comm.ui.viewfactory.view.ISelectCityView;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;


public class SelectCityDialog extends SearchEditDialogBase {

	public static class SelectCityDialogBuildData extends DialogBuildData{
		public ISelectCityView mSelectCityView;
		public int mIntWhere;
		public String mKey;
		public String mCity;

		public SelectCityDialogBuildData setCity(String mCity) {
			this.mCity = mCity;
			return this;
		}

		public SelectCityDialogBuildData setIntWhere(int mIntWhere) {
			this.mIntWhere = mIntWhere;
			return this;
		}

		public SelectCityDialogBuildData setKey(String mKey) {
			this.mKey = mKey;
			return this;
		}

		public SelectCityDialogBuildData setSelectCityView(ISelectCityView mSelectCityView) {
			this.mSelectCityView = mSelectCityView;
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
	private volatile boolean mNeedCloseDialog = true;

	public SelectCityDialog(DialogBuildData data) {
		super(data);
	}

	public void setNeedCloseDialog(boolean isClose) {
		this.mNeedCloseDialog = isClose;
	}

	private FrameLayout flContent;
	@Override
	protected View createView() {
		flContent = new FrameLayout(GlobalContext.get());
		flContent.addView(getSelectCityView());
		return flContent;
	}

	public void updateSelectCityView(){
		flContent.removeAllViews();
		flContent.addView(getSelectCityView());
	}

	private View getSelectCityView(){
		List<String> isExistCity  = new ArrayList<String>();
		List<String> curCityList  =new ArrayList<String>();

		LocationInfo lastLocation = LocationManager.getInstance().getLastLocation();
		if (lastLocation != null && lastLocation.msgGeoInfo != null
				&& !TextUtils.isEmpty(lastLocation.msgGeoInfo.strCity)) {
			String str = lastLocation.msgGeoInfo.strCity;
			curCityList.add(lastLocation.msgGeoInfo.strCity);
		}
		dereplication(curCityList, isExistCity);

		List<String> tarCityList = NavManager.getInstance().getTargetCity();
		dereplication(tarCityList, isExistCity);

		List<String> nomCityList = NavManager.getInstance().getNomCityList();
		dereplication(nomCityList, isExistCity);

		List<String> perCityList = NavManager.getInstance().getCommonCity();
		dereplication(perCityList, isExistCity);
		SelectCityViewData selectCityViewData = new SelectCityViewData();
		selectCityViewData.curCityList = curCityList;
		selectCityViewData.nomCityList = nomCityList;
		selectCityViewData.perCityList = perCityList;
		selectCityViewData.tarCityList = tarCityList;
		return ((SelectCityDialogBuildData)mBuildData).mSelectCityView.getView(selectCityViewData).view;
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

	private void  dereplication(List<String> list, List<String> allList){
		for(int i = 0; i< list.size() ;){
			String city1 = list.get(i);
			boolean isSame = false;
			if(city1.contains("{") || city1.contains("[")){
				list.remove(i);
				continue;
			}
			for(String city2 : allList){
				if( !city2.endsWith("市")){
					city2 = city2+"市";
				}
				if( !city1.endsWith("市")){
					city1 = city1+"市";
				}
				if(city1.equals(city2)){
					list.remove(i);
					isSame = true;
					break;
				}
			}
			if(!isSame){
				allList.add(city1);
				i++;
			}
		}
	}

	
	public static SelectCityDialog naviDefault(String keyWord,String city) {
		SelectCityDialog selectCityDialog = createDialog(keyWord,city,SearchEditManager.LOCATION_NONE);
		selectCityDialog.show();
		return selectCityDialog;
	}

	public static SelectCityDialog naviHome(String keyWord,String city) {
		SelectCityDialog selectCityDialog = createDialog(keyWord,city,SearchEditManager.LOCATION_HOME);
		selectCityDialog.show();
		return selectCityDialog;
	}

	public static SelectCityDialog naviCompany(String keyWord,String city) {
		SelectCityDialog selectCityDialog = createDialog(keyWord,city,SearchEditManager.LOCATION_COMPANY);
		selectCityDialog.show();
		return selectCityDialog;
	}
	public static SelectCityDialog naviJingYou(String keyWord,String city) {
		SelectCityDialog selectCityDialog = createDialog(keyWord,city,SearchEditManager.LOCATION_JINGYOU);
		selectCityDialog.show();
		return selectCityDialog;
	}
	public static SelectCityDialog naviEnd(String keyWord,String city) {
		SelectCityDialog selectCityDialog = createDialog(keyWord,city,SearchEditManager.LOCATION_END);
		selectCityDialog.show();
		return selectCityDialog;
	}

	private static SelectCityDialog sSelectCityDialog;
	private static SelectCityDialog createDialog(String keyWord, String city,int where) {
		SelectCityDialogBuildData selectCityDialogBuildData = null;
		if (sSelectCityDialog == null) {
			selectCityDialogBuildData = new SelectCityDialogBuildData()
					.setSelectCityView(WinLayoutManager.getInstance().getSelectCityView())
					.setCity(city)
					.setIntWhere(where)
					.setKey(keyWord);
			sSelectCityDialog = new SelectCityDialog(selectCityDialogBuildData);
		} else {
			selectCityDialogBuildData = (SelectCityDialogBuildData) sSelectCityDialog.mBuildData;
			selectCityDialogBuildData.setCity(city);
			selectCityDialogBuildData.setIntWhere(where);
			selectCityDialogBuildData.setSelectCityView(WinLayoutManager.getInstance().getSelectCityView());
			selectCityDialogBuildData.setKey(keyWord);
			sSelectCityDialog.updateSelectCityView();
		}
		return sSelectCityDialog;
	}

	private boolean mRegisted;

	@Override
	public void onShow() {
		super.show();
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

	public void dismiss() {
		if (!mNeedCloseDialog) {
			return;
		}
		if (isShowing()) {
			mNeedCloseDialog = true;
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

	@Override
	public String getReportDialogId() {
		return "SelectCityDialog";
	}
}

package com.txznet.txz.ui.win.nav;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.overlay.PoiOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.util.DateUtils;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.R;
import com.txznet.txz.component.selector.SelectorHelper;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.TXZHandler;

import android.os.HandlerThread;
import android.view.View;
import android.widget.CheckBox;

class WinMapDialog extends WinPoiMapBase {
	private static final int ACTION_NONE = 0;
	private static final int ACTION_HOME = 1;
	private static final int ACTION_COMPANY = 2;

	private Marker mMyMarker;
	private int mAction = ACTION_NONE;
	private List<Poi> mListPoi = new Vector<Poi>();

	private TXZHandler mMapHandler;
	private HandlerThread mMapHandlerThread;

	private MyPoiOverlay mPoiOverlay;
	private List<PoiItem> mPoiItemMarkers = new ArrayList<PoiItem>();

	private static WinMapDialog mDialog;

	private WinMapDialog() {
		super(GlobalContext.get());
		mMapHandlerThread = new HandlerThread("poi-thread");
		mMapHandlerThread.start();
		mMapHandler = new TXZHandler(mMapHandlerThread.getLooper());
	}

	private static WinMapDialog getInstance() {
		if (mDialog == null) {
			synchronized (WinMapDialog.class) {
				if (mDialog == null) {
					mDialog = new WinMapDialog();
				}
			}
		}
		return mDialog;
	}

	private int mInitPoiIndex = -1;

	public void refreshWithPoiResult(List<BusinessPoiDetail> mBus, int selectIndex, boolean bus, int action) {
		List<Poi> pois = new ArrayList<Poi>();
		for (BusinessPoiDetail bpd : mBus) {
			pois.add(bpd);
		}
		refreshWithPoiResult(pois, selectIndex, action);
	}

	public void refreshWithPoiResult(List<Poi> pois, int selectIndex, int action) {
		this.mListPoi = pois;
		this.mAction = action;
		if (mListPoi != null && mListPoi.size() > 0) {
			Poi poi = mListPoi.get(0);
			if (poi instanceof BusinessPoiDetail) {
				mTypeBusiness = true;
			} else {
				mTypeBusiness = false;
			}
		} else {
			dismiss();
			return;
		}
		mCurrentSelIndex = 0;
		mInitPoiIndex = selectIndex;
		mHasInitSelIndex = false;
		initView();
		// 可以不用隐藏SelScrollView
		initSelectorIndexs(getPoiCount());
		procNightTopic();

		// runBackGroundTask(mParserPoiItem, 0);
		runOnUiGround(mParserPoiItem, 0);
		LogUtil.logd(">>>show thread:" + Thread.currentThread().getName());
		super.show();
	}

	Runnable mParserPoiItem = new Runnable() {

		@Override
		public void run() {
			mPoiItemMarkers.clear();
			for (int i = 0; i < mListPoi.size(); i++) {
				Poi poi = mListPoi.get(i);
				PoiItem item = new PoiItem(i + "", new LatLonPoint(poi.getLat(), poi.getLng()), poi.getName(),
						poi.getGeoinfo());
				mPoiItemMarkers.add(item);
			}
			mAddToMapRunnable.run();
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					if (mInitSelTask != null) {
						AppLogic.runOnUiGround(mInitSelTask, 0);
					} else {
						onItemSelected(0);
					}
				}
			}, 0);
		}
	};

	Runnable mAddToMapRunnable = new Runnable() {

		@Override
		public void run() {
			addToMap();
		}
	};

	private void addToMap() {
		try {
			if (mAMap == null) {
				return;
			}

			mAMap.clear();
			mAMap = mapView.getMap();
			if (mPoiItemMarkers.size() < 1) {
				return;
			}

			if (mPoiOverlay != null) {
				mPoiOverlay.removeFromMap();
				mPoiOverlay = null;
			}

			mPoiOverlay = new MyPoiOverlay(mAMap, mPoiItemMarkers);
			mPoiOverlay.removeFromMap();
			mPoiOverlay.addToMap();
			mPoiOverlay.zoomToSpan();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initView() {
		if (!mTypeBusiness) {
			if (mPoiNav != null) {
				mPoiNav.setVisibility(View.GONE);
			}
		} else {
			if (mLlNav != null) {
				mLlNav.setVisibility(View.GONE);
			}

			if (mPoiNav != null) {
				mPoiNav.setVisibility(View.INVISIBLE);
			}
		}

		if (mListPoi.size() < 1) {
			showNavBar(false);
			moveToMyLocation();
			return;
		}

		showNavBar(true);
		// switch (mAction) {
		// case ACTION_NONE:
		// mStartNaviTv.setText("出发");
		// mPoiBtnStartNav.setText("出发");
		// break;
		// case ACTION_HOME:
		// mStartNaviTv.setText("设置");
		// mPoiBtnStartNav.setText("设置");
		// break;
		//
		// case ACTION_COMPANY:
		// mStartNaviTv.setText("设置");
		// mPoiBtnStartNav.setText("设置");
		// break;
		//
		// default:
		// break;
		// }
	}

	private void removeMapGroundCallback(Runnable r) {
		mMapHandler.removeCallbacks(r);
	}

	private void runOnMapBackGround(Runnable r, long delay) {
		mMapHandler.postDelayed(r, delay);
	}

	private void runBackGroundTask(Runnable task, long delay) {
		removeMapGroundCallback(task);
		runOnMapBackGround(task, delay);
	}

	private void runOnUiGround(Runnable task, long delay) {
		AppLogic.removeUiGroundCallback(task);
		AppLogic.runOnUiGround(task, delay);
	}

	private void procNightTopic() {
		if (DateUtils.isNight()) {
			mAMap.setMapType(AMap.MAP_TYPE_NIGHT);
			mZoominIb.setImageResource(R.drawable.nav_view_zoom_out_n);
			mZoomoutIb.setImageResource(R.drawable.nav_view_zoom_in_n);
			mBtnMyLocation.setBackgroundResource(R.drawable.activity_check_map_n_ic_bg);
		} else {
			mAMap.setMapType(AMap.MAP_TYPE_NORMAL);
			mZoominIb.setImageResource(R.drawable.nav_view_zoom_out);
			mZoomoutIb.setImageResource(R.drawable.nav_view_zoom_in);
			mBtnMyLocation.setBackgroundResource(R.drawable.activity_search_ic_bg);
		}
	}

	@Override
	public int getInitPoiIndex() {
		return mInitPoiIndex;
	}

	@Override
	public int getPoiCount() {
		return mListPoi != null ? mListPoi.size() : 0;
	}

	@Override
	public boolean onMapMarkerClick(Marker marker) {
		try {
			CheckBox mCb = mCheckBoxs.get(mCurrentSelIndex);
			if (mCb != null) {
				mActiveCheckEvent = false;
				mCb.setChecked(false);
				mActiveCheckEvent = true;
			}

			int index = (Integer) marker.getObject();
			onItemSelected(index);
			if (mCheckBoxs != null && mCheckBoxs.size() > 0 && index < mCheckBoxs.size() && index > -1) {
				mActiveCheckEvent = false;
				mCheckBoxs.get(index).setChecked(true);
				mActiveCheckEvent = true;
			}
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		}
		return false;
	}

	@Override
	public void dismiss() {
		try {
			if (isShowing()) {
				AppLogic.runOnUiGround(mCloseRunnable, 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	Runnable mCloseRunnable = new Runnable() {

		@Override
		public void run() {
			WinMapDialog.super.dismiss();
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					setContentView(createView());
				}
			}, 50);
		}
	};

	@Override
	public void moveToMyLocation() {
		LocationInfo location = LocationManager.getInstance().getLastLocation();

		if (location == null || location.msgGpsInfo == null)
			return;

		double lat = location.msgGpsInfo.dblLat;
		double lng = location.msgGpsInfo.dblLng;

		LatLng ll = new LatLng(lat, lng);

		if (mMyMarker != null) {
			mMyMarker.remove();
			mMyMarker.destroy();
			mMyMarker = null;
		}

		if (mMyMarker == null) {
			MarkerOptions mo = new MarkerOptions();
			mo.position(ll);
			mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker));
			mo.anchor(0.5f, 0.5f);
			mMyMarker = mAMap.addMarker(mo);
		}

		CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(ll, 16);
		mAMap.animateCamera(cu);
	}

	@Override
	public void procPerformNavi() {
		SelectorHelper.selectCancel();
		dismiss();
		String name, address;
		double lat, lng;
		Poi poi = mListPoi.get(mCurrentSelIndex);
		name = poi.getName();
		address = poi.getGeoinfo();
		lat = poi.getLat();
		lng = poi.getLng();
		if (ACTION_NONE == mAction) {
		} else if (ACTION_HOME == mAction) {
			NavManager.getInstance().setHomeLocation(name, address, lat, lng, UiMap.GPS_TYPE_GCJ02);
		} else if (ACTION_COMPANY == mAction) {
			NavManager.getInstance().setCompanyLocation(name, address, lat, lng, UiMap.GPS_TYPE_GCJ02);
		}
		NavManager.getInstance().NavigateTo(poi);
		// 上报数据
		ReportUtil.doReport(new ReportUtil.Report.Builder().setType("navi").setAction("go").putExtra("index", mCurrentSelIndex).buildTouchReport());
	}

	@Override
	public void makeCall(String name, String phone) {
		CallManager.getInstance().makeSimpleCall(phone, name);
	}

	@Override
	public void procPoiInfoLayoutPerform() {
		if (mCurrentSelIndex < 0 || mCurrentSelIndex >= mListPoi.size()) {
			return;
		}

		Poi poi = mListPoi.get(mCurrentSelIndex);
		if (poi != null) {
			double lat = poi.getLat();
			double lng = poi.getLng();
			moveToCurrentMarker(lat, lng);
		}
	}

	private void moveToCurrentMarker(double lat, double lng) {
		LatLng ll = new LatLng(lat, lng);
		CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(ll, 16);
		mAMap.animateCamera(cu);
	}

	@Override
	public void procBackPerform() {
		if (mHasInit) {
			dismiss();
		}
	}

	@Override
	public void onItemSelected(int index) {
		if (index < 0 || index >= getPoiCount())
			return;
		mCurrentSelIndex = index;
		mAMap.clear();
		mPoiOverlay.addToMap();

		mTxtName.setText(LanguageConvertor.toLocale(mListPoi.get(index).getName()));
		mTxtDes.setText(LanguageConvertor.toLocale(mListPoi.get(index).getGeoinfo()));
		mTxtNamePoi.setText(LanguageConvertor.toLocale(mListPoi.get(index).getName()));
		mTxtDesPoi.setText(LanguageConvertor.toLocale(mListPoi.get(index).getGeoinfo()));
		// showNavBar(true);
		moveToMarker(index);
		onSelectBusinessPoi(index);
		mBtnMyLocation.setImageResource(R.drawable.activity_search_ic_point);
		// 上报数据
		ReportUtil.doReport(new ReportUtil.Report.Builder().setType("navi").setAction("view").putExtra("index", index).buildTouchReport());
	}

	private void moveToMarker(int index) {
		if (mListPoi == null)
			return;
		if (index < 0 || index > mListPoi.size())
			return;
		LatLng ll = new LatLng(mListPoi.get(index).getLat(), mListPoi.get(index).getLng());

		CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(ll, 16);
		mAMap.animateCamera(cu);
	}

	@Override
	public Poi getPoiByIndex(int index) {
		return mListPoi != null ? mListPoi.get(index) : null;
	}

	private class MyPoiOverlay extends PoiOverlay {

		public MyPoiOverlay(AMap aMap, List<PoiItem> mPoiItems) {
			super(aMap, mPoiItems);
		}

		@Override
		protected BitmapDescriptor getBitmapDescriptor(int arg0) {
			if (mCurrentSelIndex == arg0) {
				return BitmapDescriptorFactory.fromResource(R.drawable.activity_search_ic_mark_selected);
			}

			return BitmapDescriptorFactory.fromResource(getResourceId(arg0));
		}
	}

	private int getResourceId(int arg0) {
		int resId = R.drawable.activity_search_ic_red_dot;
		switch (arg0) {
		case 0:
			resId = R.drawable.activity_search_ic_mark1;
			break;

		case 1:
			resId = R.drawable.activity_search_ic_mark2;
			break;

		case 2:
			resId = R.drawable.activity_search_ic_mark3;
			break;

		case 3:
			resId = R.drawable.activity_search_ic_mark4;
			break;

		case 4:
			resId = R.drawable.activity_search_ic_mark5;
			break;

		case 5:
			resId = R.drawable.activity_search_ic_mark6;
			break;

		case 6:
			resId = R.drawable.activity_search_ic_mark7;
			break;

		case 7:
			resId = R.drawable.activity_search_ic_mark8;
			break;

		case 8:
			resId = R.drawable.activity_search_ic_mark9;
			break;

		case 9:
			resId = R.drawable.activity_search_ic_mark10;
			break;
		default:
			break;
		}
		return resId;
	}
}

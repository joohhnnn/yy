package com.txznet.nav.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.view.RouteOverLay;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.R;
import com.txznet.nav.manager.NavManager;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NaviCustomView implements OnClickListener, OnCheckedChangeListener {
	private ImageView mDirectionIv;
	private ImageButton mNaviTypeMultiCar;
	private CheckBox mTracCheckBox;
	private ImageButton mZoomout;
	private ImageButton mOverrideCb;
	private ImageButton mZoomin;
	private LinearLayout mZoomLayout;
	private LinearLayout mFooterLayout;
	private TextView mExitTv;
	private TextView mNavTv;

	private TextView mIconType;
	private TextView mRoadNameTv;
	private TextView mTimeTv;
	private TextView mDistanceTv;
	private ImageView mCrossIv;

	private AMap mAMap;
	private RouteOverLay mRouteOverLay;

	private NavViewActivity mActivity;
	private AMapNaviView mAMapNaviView;
	private DistancePopWin mDistancePopWin;

	private boolean mIsInit = false;
	private boolean isHudView = false;
	private static NaviCustomView instance;

	private NaviCustomView() {
	}

	public static NaviCustomView getInstance() {
		if (instance == null) {
			synchronized (NaviCustomView.class) {
				if (instance == null) {
					instance = new NaviCustomView();
				}
			}
		}

		return instance;
	}

	public void initAMapNaviView(AMapNaviView aMapNaviView) {
		this.mAMapNaviView = aMapNaviView;
		if (mAMapNaviView == null) {
			return;
		}
		init();
	}

	private void init() {
		isHudView = false;
		mAMap = mAMapNaviView.getMap();
		initCustomWidget();
		checkMultinav();
		mIsInit = true;

		if (mAMapNaviView.getViewOptions().isNaviNight()) {
			updateNaviNight(true);
		} else {
			updateNaviNight(false);
		}
	}

	public void attachActivity(NavViewActivity activity) {
		this.mActivity = activity;
	}

	private void initCustomWidget() {
		mDistancePopWin = new DistancePopWin(mAMapNaviView.getRootView());
		View myView = LayoutInflater.from(AppLogic.getApp()).inflate(R.layout.nav_view_layout, null);
		this.mDirectionIv = (ImageView) myView.findViewById(R.id.direction_iv);
		this.mNaviTypeMultiCar = (ImageButton) myView.findViewById(R.id.friend_ib);
		this.mTracCheckBox = (CheckBox) myView.findViewById(R.id.trac_cb);
		this.mZoomout = (ImageButton) myView.findViewById(R.id.zoom_out_ib);
		this.mOverrideCb = (ImageButton) myView.findViewById(R.id.override_cb);
		this.mZoomin = (ImageButton) myView.findViewById(R.id.zoom_in_ib);
		this.mZoomLayout = (LinearLayout) myView.findViewById(R.id.zoom_layout);
		this.mFooterLayout = (LinearLayout) myView.findViewById(R.id.footer_ly);
		this.mExitTv = (TextView) myView.findViewById(R.id.exit_tv);
		this.mNavTv = (TextView) myView.findViewById(R.id.nav_tv);

		this.mIconType = (TextView) myView.findViewById(R.id.icon_type_tv);
		this.mRoadNameTv = (TextView) myView.findViewById(R.id.road_name_tv);
		this.mTimeTv = (TextView) myView.findViewById(R.id.remain_time_tv);
		this.mDistanceTv = (TextView) myView.findViewById(R.id.remain_distance_tv);
		this.mCrossIv = (ImageView) myView.findViewById(R.id.cross_iv);

		this.mCrossIv.setVisibility(View.GONE);
		this.mDirectionIv.setVisibility(View.VISIBLE);

		setListener();
		mAMapNaviView.addView(myView, 1);
		if (mAMapNaviView.getViewOptions().isTrafficLine()) {
			mTracCheckBox.setChecked(true);
		} else {
			mTracCheckBox.setChecked(false);
		}

		mTracCheckBox.setOnCheckedChangeListener(this);
		setVisible(false);
	}

	private void setListener() {
		mIconType.setOnClickListener(this);
		mNaviTypeMultiCar.setOnClickListener(this);
		mZoomout.setOnClickListener(this);
		mOverrideCb.setOnClickListener(this);
		mZoomin.setOnClickListener(this);
		mExitTv.setOnClickListener(this);
		mNavTv.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.friend_ib:
			if (mDistancePopWin != null) {
				if (mDistancePopWin.isShowing()) {
					mDistancePopWin.dismiss();
				} else {
					mDistancePopWin.showPopupWin();
				}
			}
			break;

		case R.id.zoom_out_ib:
			zoomout();
			break;

		case R.id.override_cb:
			lookAllPath(false, true);
			break;

		case R.id.zoom_in_ib:
			zoomin();
			break;

		case R.id.exit_tv:
			if (mActivity != null) {
				mActivity.onBackPressed();
			}
			break;

		case R.id.nav_tv:
			setCarLock(true);
			break;

		case R.id.icon_type_tv:
			if (isHudView) {
				mActivity.mAMapHudView.setVisibility(View.GONE);
			} else {
				mActivity.mAMapHudView.setVisibility(View.VISIBLE);
			}
			break;
		}
	}

	public void lookAllPath(Boolean needReload, boolean isZoomSpan) {
		AMapNaviPath np = AMapNavi.getInstance(AppLogic.getApp()).getNaviPath();
		if (mRouteOverLay != null) {
			mRouteOverLay.removeFromMap();
			mRouteOverLay.destroy();
			mRouteOverLay = null;
		}

		mRouteOverLay = new RouteOverLay(mAMap, np, AppLogic.getApp());

		// 获取路径规划线路，显示到地图上
		mRouteOverLay.addToMap();
		if (mTracCheckBox.isChecked()) {
			mRouteOverLay.setTrafficLine(true);
		} else {
			mRouteOverLay.setTrafficLine(false);
		}

		if (isZoomSpan) {
			mRouteOverLay.zoomToSpan();
		}
	}

	public void updateNaviInfo(NaviInfo naviInfo) {
		if (naviInfo == null || !mIsInit) {
			return;
		}

		if (mIconType != null) {
			int iconType = naviInfo.getIconType();
			int sourceId = getIconResourceId(iconType);
			Drawable top = AppLogic.getApp().getResources().getDrawable(sourceId);
			top.setBounds(0, 0, getW(), getH());
			AppLogic.runOnUiGround(new Runnable1<Drawable>(top) {

				@Override
				public void run() {
					mIconType.setCompoundDrawables(null, mP1, null, null);
				}
			}, 0);

			int distance = naviInfo.getCurStepRetainDistance();
			if (distance >= 1000) {
				double r = Math.round(distance / 100.0) / 10.0;
				setTextView(mIconType, r + "公里");
			} else {
				setTextView(mIconType, distance + "米");
			}

		}

		if (mRoadNameTv != null) {
			String roadName = naviInfo.getCurrentRoadName();
			LogUtil.logd("roadName:" + roadName);
			if (!TextUtils.isEmpty(roadName)) {
				setTextView(mRoadNameTv, roadName);
			}
		}

		if (mTimeTv != null) {
			int time = naviInfo.getPathRetainTime();
			if (time > 60) {
				String speakTxt = "";
				if (time >= 3600) {
					int r = time % 3600;
					int h = time / 3600;
					int m = r / 60;
					speakTxt = h + "小时" + (m > 0 ? m + "分钟" : "");
				} else {
					speakTxt = (time / 60) + "分钟";
				}

				setTextView(mTimeTv, speakTxt);
			} else {
				setTextView(mTimeTv, time + "秒");
			}
		}

		if (mDistanceTv != null) {
			int distance = naviInfo.getPathRetainDistance();
			if (distance > 1000) {
				String txt = "  " + (Math.round(distance / 100.0) / 10.0) + "公里";
				setTextView(mDistanceTv, txt);
			} else {
				String txt = "  " + distance + "米";
				setTextView(mDistanceTv, txt);
			}
		}

		if (mDirectionIv != null) {
			int rotation = naviInfo.getDirection();
			AppLogic.runOnUiGround(new Runnable1<Integer>(rotation) {

				@Override
				public void run() {
					mDirectionIv.setRotation(-mP1);
				}
			}, 0);
		}
	}

	private void setTextView(TextView tv, String txt) {
		AppLogic.runOnUiGround(new Runnable2<TextView, String>(tv, txt) {

			@Override
			public void run() {
				if (mP1 != null) {
					if (TextUtils.isEmpty(mP2)) {
						mP2 = "";
					}

					mP1.setText(mP2);
				}
			}
		}, 0);
	}

	private void zoomout() {
		mAMap.animateCamera(CameraUpdateFactory.zoomOut());
	}

	private void zoomin() {
		mAMap.animateCamera(CameraUpdateFactory.zoomIn());
	}

	/**
	 * 点击全览
	 */
	public void performOverrideClick() {
		if (mOverrideCb == null) {
			return;
		}

		((ImageButton) mOverrideCb).performClick();
	}

	/**
	 * 点击是否打开路况
	 * 
	 * @param check
	 */
	public void checkTraffic(boolean check) {
		if (mTracCheckBox != null) {
			mTracCheckBox.setChecked(check);
		}
	}

	/**
	 * 显示路况放大图
	 * 
	 * @param anc
	 */
	public void showCross(AMapNaviCross anc) {
		Bitmap bitmap = anc.getBitmap();
		if (bitmap != null) {
			this.mCrossIv.setImageBitmap(bitmap);
			this.mCrossIv.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 隐藏路况放大图
	 */
	public void hideCross() {
		this.mCrossIv.setVisibility(View.GONE);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.trac_cb:
			lookAllPath(false, false);
			if (mRouteOverLay != null) {
				mRouteOverLay.setTrafficLine(isChecked);
			}

			AMapConfig.getInstance().setTrafficLineEnable(isChecked);
			break;
		}
	}

	private void checkMultinav() {
		if (NavManager.getInstance().isMultiNav()) {
			mNaviTypeMultiCar.setVisibility(View.VISIBLE);
		} else {
			mNaviTypeMultiCar.setVisibility(View.GONE);
		}
	}

	public void setCustomView(boolean visible) {
		setVisible(visible);
	}

	private void setVisible(boolean isVisible) {
		if (mZoomLayout == null || mFooterLayout == null) {
			return;
		}

		if (isVisible) {
			mZoomLayout.setVisibility(View.VISIBLE);
			mFooterLayout.setVisibility(View.VISIBLE);
		} else {
			mZoomLayout.setVisibility(View.GONE);
			mFooterLayout.setVisibility(View.GONE);
		}

		if (!isVisible) {
			if (mDistancePopWin != null && mDistancePopWin.isShowing()) {
				mDistancePopWin.dismiss();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setCarLock(boolean lock) {
		if (!mIsInit) {
			return;
		}
		Class cla = mAMapNaviView.getClass();
		try {
			Method method = cla.getDeclaredMethod("setCarLock", boolean.class);
			method.setAccessible(true);
			method.invoke(mAMapNaviView, lock);
		} catch (IllegalArgumentException e) {
			LogUtil.loge(e.toString());
		} catch (NoSuchMethodException e) {
			LogUtil.loge(e.toString());
		} catch (IllegalAccessException e) {
			LogUtil.loge(e.toString());
		} catch (InvocationTargetException e) {
			LogUtil.loge(e.toString());
		}
	}

	public void updateNaviNight(boolean isNight) {
		if (!mIsInit) {
			return;
		}

		if (isNight) {
			mNaviTypeMultiCar.setImageResource(R.drawable.nav_view_friend_n);
			mTracCheckBox.setBackgroundResource(R.drawable.nav_view_trac_n);
			mZoomin.setImageResource(R.drawable.nav_view_zoom_out_n);
			mZoomout.setImageResource(R.drawable.nav_view_zoom_in_n);
			mOverrideCb.setImageResource(R.drawable.nav_view_route_n);
			mDirectionIv.setImageResource(R.drawable.map_direction_n);
		} else {
			mNaviTypeMultiCar.setImageResource(R.drawable.nav_view_friend);
			mTracCheckBox.setBackgroundResource(R.drawable.nav_view_trac);
			mZoomin.setImageResource(R.drawable.nav_view_zoom_out);
			mZoomout.setImageResource(R.drawable.nav_view_zoom_in);
			mOverrideCb.setImageResource(R.drawable.nav_view_route);
			mDirectionIv.setImageResource(R.drawable.map_direction);
		}

		setCarLock(false);
	}

	public void setHudMode(boolean open) {
		if (mActivity != null && NavManager.getInstance().isNavi()) {
			mActivity.setUpHudMode(open);
		}
	}

	public boolean dispatchTouchEvent(MotionEvent event) {
		if (mDistancePopWin != null && mDistancePopWin.isShowing()) {
			mDistancePopWin.dismiss();
			return true;
		}

		if (NavManager.getInstance().isMultiNav()) {
			setCarLock(false);
		}

		return false;
	}

	public void onDestory() {
		if (mDistancePopWin != null && mDistancePopWin.isShowing()) {
			mDistancePopWin.dismiss();
		}

		instance = null;
		mIsInit = false;
	}

	public void resumeNavi() {
		AppLogic.removeBackGroundCallback(mResumeNaviRunnable);
		AppLogic.runOnBackGround(mResumeNaviRunnable, 0);
	}

	Runnable mResumeNaviRunnable = new Runnable() {

		@Override
		public void run() {
			// setCarLock(true);
			AMapNavi.getInstance(AppLogic.getApp()).resumeNavi();
		}
	};

	// ////////////////////////////////////////////////////////////////////////////////
	private int getW() {
		return (int) AppLogic.getApp().getResources().getDimension(R.dimen.x64);
	}

	private int getH() {
		return (int) AppLogic.getApp().getResources().getDimension(R.dimen.y64);
	}

	private int getIconResourceId(int index) {
		int resourceId = R.drawable.sou9;
		switch (index) {
		case 1:
			resourceId = R.drawable.caricon;
			break;
		case 2:
			resourceId = R.drawable.sou2;
			break;
		case 3:
			resourceId = R.drawable.sou3;
			break;
		case 4:
			resourceId = R.drawable.sou4;
			break;
		case 5:
			resourceId = R.drawable.sou5;
			break;
		case 6:
			resourceId = R.drawable.sou6;
			break;
		case 7:
			resourceId = R.drawable.sou7;
			break;
		case 8:
			resourceId = R.drawable.sou8;
			break;
		case 9:
			resourceId = R.drawable.sou9;
			break;
		case 10:
			resourceId = R.drawable.sou10;
			break;
		case 11:
			resourceId = R.drawable.sou11;
			break;
		case 12:
			resourceId = R.drawable.sou12;
			break;
		case 13:
			resourceId = R.drawable.sou13;
			break;
		case 14:
			resourceId = R.drawable.sou14;
			break;
		case 15:
			resourceId = R.drawable.sou15;
			break;
		case 16:
			resourceId = R.drawable.sou16;
			break;
		case 17:
			resourceId = R.drawable.sou17;
			break;
		case 18:
			resourceId = R.drawable.sou18;
			break;
		case 19:
			resourceId = R.drawable.sou19;
			break;
		default:
			break;
		}

		return resourceId;
	}
}
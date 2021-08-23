package com.txznet.txz.ui.win.nav;

import java.util.ArrayList;
import java.util.List;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMapLongClickListener;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMap.OnPOIClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.dialog.WinDialog;
import com.txznet.loader.AppLogic;
import com.txznet.record.util.ScreenUtil;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.R;
import com.txznet.txz.util.LanguageConvertor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public abstract class WinPoiMapBase extends WinDialog {

	protected MapView mapView;
	protected AMap mAMap;

	protected FrameLayout mFlMap; // 地图layout
	protected LinearLayout mLlNav; // 导航条
	protected LinearLayout mPoiNav;

	protected TextView mTxtName; // 导航条名字
	protected TextView mTxtDes; // 导航条目的地址
	protected TextView mTxtNamePoi; // 导航条名字
	protected TextView mTxtDesPoi; // 导航条目的地址

	protected View mDescriptionLyView;

	/** 放大缩小按钮 **/
	protected ImageButton mZoomoutIb;
	protected ImageButton mZoominIb;

	// 大众点评相关控件
	protected LinearLayout mMarkLayout;
	protected ImageView mStarsIv;
	protected TextView mCostTv;
	protected ImageView mHuiIv;
	protected ImageView mTuanIv;

	protected TextView mTasteTv;
	protected TextView mEnvTv;
	protected TextView mServerTv;
	protected TextView mPhoneTv;

	/** 选择控件 **/
	protected LinearLayout mSelIndexLy;
	protected LinearLayout mSelIndexsLy;
	protected HorizontalScrollView mSelScrollView;

	protected LinearLayout mContainer;
	protected List<CheckBox> mCheckBoxs = new ArrayList<CheckBox>();

	protected LinearLayout mCallLayout;
	protected LinearLayout mBtnStartNav; // 导航条导航按钮
	protected TextView mStartNaviTv;
	protected Button mPoiBtnStartNav;

	protected ImageButton mBtnMyLocation; // 回到我的位置按钮
	protected ImageButton mBtnOpenSearchView; // 页面上的后退按钮

	protected boolean mHasInit = true;

	public WinPoiMapBase(Context context) {
		super(true);
		getWindow().setType(WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 3);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
				| WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE);
	}

	@Override
	protected View createView() {
		View contentView = null;
		try {
			contentView = LayoutInflater.from(AppLogic.getApp()).inflate(R.layout.win_poi_map_ly, null);
			findAndInitWidget(contentView);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return contentView;
	}

	private void findAndInitWidget(View view) {
		try {
			mapView = (MapView) view.findViewById(R.id.map);
			mapView.onCreate((Bundle) null);
			mAMap = mapView.getMap();
			mAMap.setOnMapLoadedListener(new OnMapLoadedListener() {

				@Override
				public void onMapLoaded() {
					mHasInit = true;
				}
			});
		} catch (Exception e1) {
			LogUtil.loge(e1.toString());
		}

		if (mapView == null) {
			if (isShowing()) {
				dismiss();
			}
			return;
		}

		mFlMap = (FrameLayout) view.findViewById(R.id.flMap);
		mBtnOpenSearchView = (ImageButton) view.findViewById(R.id.btnOpenSearch);
		mLlNav = (LinearLayout) view.findViewById(R.id.flNav);
		mPoiNav = (LinearLayout) view.findViewById(R.id.flNav_poi);

		mTxtName = (TextView) view.findViewById(R.id.txtName);
		mTxtDes = (TextView) view.findViewById(R.id.txtDes);

		mDescriptionLyView = view.findViewById(R.id.poi_des_ly);

		mZoomoutIb = (ImageButton) view.findViewById(R.id.zoom_out_ib);
		mZoominIb = (ImageButton) view.findViewById(R.id.zoom_in_ib);

		mMarkLayout = (LinearLayout) view.findViewById(R.id.mark_layout);
		mStarsIv = (ImageView) view.findViewById(R.id.star_grade_iv);
		mCostTv = (TextView) view.findViewById(R.id.cost_tv);
		mHuiIv = (ImageView) view.findViewById(R.id.hui_iv);
		mTuanIv = (ImageView) view.findViewById(R.id.tuan_iv);
		mTasteTv = (TextView) view.findViewById(R.id.taste_tv);
		mEnvTv = (TextView) view.findViewById(R.id.env_tv);
		mServerTv = (TextView) view.findViewById(R.id.server_tv);
		mPhoneTv = (TextView) view.findViewById(R.id.phone_tv);

		mTxtNamePoi = (TextView) view.findViewById(R.id.txtName_poi);
		mTxtDesPoi = (TextView) view.findViewById(R.id.txtDes_poi);

		mSelIndexLy = (LinearLayout) view.findViewById(R.id.sel_index_ly);
		mSelIndexsLy = (LinearLayout) view.findViewById(R.id.sel_indexs_ly);
		mSelScrollView = (HorizontalScrollView) view.findViewById(R.id.sel_index_container);

		mCallLayout = (LinearLayout) view.findViewById(R.id.call_ly);
		mBtnStartNav = (LinearLayout) view.findViewById(R.id.btnStartNav);
		mStartNaviTv = (TextView) view.findViewById(R.id.start_nav_tv);
		mPoiBtnStartNav = (Button) view.findViewById(R.id.btnStartNav_poi);

		mBtnMyLocation = (ImageButton) view.findViewById(R.id.btnMoveToMyLocation);

		mAMap.setOnMapTouchListener(new OnMapTouchListener() {

			@Override
			public void onTouch(MotionEvent arg0) {
				mBtnMyLocation.setImageResource(R.drawable.activity_search_ic_point);
			}
		});

		mAMap.setOnPOIClickListener(new OnPOIClickListener() {

			@Override
			public void onPOIClick(com.amap.api.maps.model.Poi arg0) {
			}
		});

		mAMap.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng arg0) {
			}
		});

		UiSettings us = mAMap.getUiSettings();
		if (us != null) {
			us.setZoomControlsEnabled(false);
		}

		initOnClick();
	}

	protected void initOnClick() {
		mOnClickListener = new android.view.View.OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {

				case R.id.btnOpenSearch:
					procBackPerform();
					break;

				case R.id.poi_des_ly:
					procPoiInfoLayoutPerform();
					break;

				case R.id.zoom_out_ib:
					zoomout();
					break;

				case R.id.zoom_in_ib:
					zoomin();
					break;

				case R.id.call_ly:
					String phoneNum = mPhoneTv.getText().toString();
					String name = mTxtName.getText().toString();
					makeCall(name, phoneNum);
					break;

				case R.id.btnStartNav:
					// // 导航
					procPerformNavi();

					break;

				case R.id.btnStartNav_poi:
					mBtnStartNav.performClick();
					break;

				case R.id.btnMoveToMyLocation:
					moveToMyLocation();
					mBtnMyLocation.setImageResource(R.drawable.activity_search_ic_follow);
					break;
				}
			}
		};
		mBtnOpenSearchView.setOnClickListener(mOnClickListener);
		mDescriptionLyView.setOnClickListener(mOnClickListener);
		mZoomoutIb.setOnClickListener(mOnClickListener);
		mZoominIb.setOnClickListener(mOnClickListener);
		mCallLayout.setOnClickListener(mOnClickListener);
		mBtnStartNav.setOnClickListener(mOnClickListener);
		mPoiBtnStartNav.setOnClickListener(mOnClickListener);
		mBtnMyLocation.setOnClickListener(mOnClickListener);

		// 设置overlay可以处理标注点击事件
		mAMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker arg0) {
				// try {
				// int index = (Integer) arg0.getObject();
				// onItemSelected(index);
				// if (mCheckBoxs != null && mCheckBoxs.size() > 0
				// && index < mCheckBoxs.size() && index > -1) {
				// mActiveCheckEvent = false;
				// mCheckBoxs.get(index).setChecked(true);
				// mActiveCheckEvent = true;
				// }
				// } catch (Exception e) {
				// LogUtil.loge(e.toString());
				// }
				// return false;
				return onMapMarkerClick(arg0);
			}
		});
	}

	private android.view.View.OnClickListener mOnClickListener = null;

	protected boolean mTypeBusiness;

	protected void showNavBar(boolean b) {
		if (b)
			if (!mTypeBusiness) {
				mLlNav.setVisibility(View.GONE);
				mPoiNav.setVisibility(View.VISIBLE);
			} else {
				mPoiNav.setVisibility(View.GONE);
				mLlNav.setVisibility(View.VISIBLE);
			}
		else if (!mTypeBusiness) {
			mLlNav.setVisibility(View.GONE);
		} else {
			mPoiNav.setVisibility(View.GONE);
		}
	}

	protected void initSelectorIndexs(int count) {
		mCheckBoxs.clear();
		if (count == 1) {
			mSelIndexLy.setVisibility(View.GONE);
			mSelScrollView.setVisibility(View.GONE);
			return;
		}

		if (count > 5) {
			mSelIndexLy.setVisibility(View.GONE);
			mSelScrollView.setVisibility(View.VISIBLE);
			mContainer = mSelIndexsLy;
		} else {
			mSelIndexLy.setVisibility(View.VISIBLE);
			mSelScrollView.setVisibility(View.GONE);
			mContainer = mSelIndexLy;
		}

		mContainer.removeAllViews();
		mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				mContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				// if (mInitPoiIndex != -1) {
				// initHorizontalScrollPos(mInitPoiIndex);
				// }
				// 初始化滑动偏移量
				initHorizontalScrollPos(getInitPoiIndex());
			}
		});
		for (int i = 0; i < count; i++) {
			boolean show = true;
			if (count == 1) {
				show = false;
			}
			if (i == (count - 1)) {
				show = false;
			}

			View biv = buildIndexView(i, i + 1 + "", show, count > 5 ? true : false);
			mContainer.addView(biv);
		}
	}

	public int getInitPoiIndex() {
		return 0;
	}

	public int getPoiCount() {
		return 0;
	}

	@Override
	protected void onLoseFocus() {
		if (mapView != null) {
			mapView.onPause();
		}
		super.onLoseFocus();
	}

	@Override
	protected void onGetFocus() {
		if (mapView != null) {
			mapView.onResume();
		}
		super.onGetFocus();
	}

	@Override
	public void dismiss() {
		try {
			// if (mAMap != null) {
			// mAMap.clear();
			// }
			// if (mapView != null) {
			// mapView.onDestroy();
			// }
			LogUtil.logd(">>>dismiss thread:" + Thread.currentThread().getName());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			super.dismiss();
		}
	}

	protected boolean mHasInitSelIndex;

	protected int mCurrentSelIndex;

	private void initHorizontalScrollPos(int pos) {
		final int totalCount = getPoiCount();
		final int totalWidth = mContainer.getMeasuredWidth();
		final int screenWidth = mSelScrollView.getWidth();
		final int eachWidth = totalWidth / totalCount;
		int scrollX = (pos - 2) * eachWidth;
		final int min = 0;
		final int max = totalWidth - screenWidth;
		if (scrollX > max) {
			scrollX = max;
		}
		if (scrollX < min) {
			scrollX = min;
		}

		mSelScrollView.setSmoothScrollingEnabled(true);
		doScrollX(scrollX);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void doScrollX(int scrollX) {
		// try {
		// Method method =
		// HorizontalScrollView.class.getDeclaredMethod("doScrollX",
		// Integer.class);
		// method.setAccessible(true);
		// method.invoke(mSelScrollView, scrollX);
		// } catch (NoSuchMethodException e) {
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// e.printStackTrace();
		// } catch (IllegalArgumentException e) {
		// e.printStackTrace();
		// } catch (InvocationTargetException e) {
		// e.printStackTrace();
		// }
		mSelScrollView.smoothScrollTo(scrollX, 0);
	}

	protected boolean mActiveCheckEvent = true;

	private View buildIndexView(final int index, String txt, boolean showLine, boolean fixed) {
		LinearLayout vg = (LinearLayout) LayoutInflater.from(AppLogic.getApp()).inflate(R.layout.poi_sel_item_view,
				null);
		final CheckBox cb = (CheckBox) vg.findViewById(R.id.index_tv);

		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					AppLogic.runOnUiGround(new Runnable() {
						public void run() {
							initHorizontalScrollPos(index);
						}
					}, 200);
				}

				if (!mActiveCheckEvent) {
					return;
				}
				if (!isChecked) {
					if (index == mCurrentSelIndex) {
						cb.setChecked(true);
					}
				} else {
					CheckBox cb = mCheckBoxs.get(mCurrentSelIndex);
					onItemSelected(index);
					if (getInitPoiIndex() == 0 && !mHasInitSelIndex) {
						mHasInitSelIndex = true;
						return;
					}

					if (cb != null && cb.isChecked()) {
						cb.setChecked(false);
					}
				}
			}
		});
		cb.setText(txt);
		if (index == getInitPoiIndex()) {
			// 初始化为选中
			// cb.setChecked(true);
			mInitSelTask = new SelTask(cb, true);
		}

		mCheckBoxs.add(cb);

		View line = vg.findViewById(R.id.line_view);
		if (!showLine) {
			line.setVisibility(View.GONE);
		}

		int width = 0;
		int height = LayoutParams.MATCH_PARENT;
		if (fixed) {
			width = (int) AppLogic.getApp().getResources().getDimension(R.dimen.x130);

			int count = getPoiCount();
			if (width * count <= ScreenUtil.getScreenWidth()) {
				fixed = false;
			}
		}

		LayoutParams params = (LayoutParams) vg.getLayoutParams();
		if (params == null) {
			params = new LayoutParams(width, height);
		}
		if (!fixed) {
			params.weight = 1.0f;
		}

		vg.setLayoutParams(params);
		return vg;
	}

	protected SelTask mInitSelTask = null;

	private class SelTask implements Runnable {

		private CheckBox mSelCb;
		private boolean mChecked;

		public SelTask(CheckBox cb, boolean check) {
			this.mSelCb = cb;
			this.mChecked = check;
		}

		@Override
		public void run() {
			if (mSelCb != null) {
				mSelCb.setChecked(mChecked);
			}

			mInitSelTask = null;
		}
	}

	private void zoomout() {
		mAMap.animateCamera(CameraUpdateFactory.zoomOut());
	}

	private void zoomin() {
		mAMap.animateCamera(CameraUpdateFactory.zoomIn());
	}

	protected void onSelectBusinessPoi(int index) {
		Poi poi = getPoiByIndex(index);
		if (poi instanceof BusinessPoiDetail) {
			BusinessPoiDetail bpd = (BusinessPoiDetail) poi;

			mMarkLayout.setVisibility(View.VISIBLE);
			mCostTv.setVisibility(View.VISIBLE);
			mStarsIv.setVisibility(View.VISIBLE);

			double score = bpd.getScore();
			if (score < 1) {
				mStarsIv.setVisibility(View.GONE);
			} else {
				int resId = getSoreMark(score);
				mStarsIv.setImageResource(resId);
			}

			if (bpd.isHasCoupon()) {
				mHuiIv.setVisibility(View.VISIBLE);
			} else {
				mHuiIv.setVisibility(View.GONE);
			}

			if (bpd.isHasDeal()) {
				mTuanIv.setVisibility(View.VISIBLE);
			} else {
				mTuanIv.setVisibility(View.GONE);
			}

			int price = (int) bpd.getAvgPrice();
			if (price > 0) {
				String txt = String.format("￥%d/人", price);
				mCostTv.setText(txt);
			} else {
				mCostTv.setVisibility(View.GONE);
			}

			double scoreProduct = bpd.getScoreProduct();
			double scoreEnv = bpd.getScoreDecoration();
			double scoreService = bpd.getScoreService();

			String ptxt = String.format("口味:%.1f", scoreProduct);
			String etxt = String.format("环境:%.1f", scoreEnv);
			String stxt = String.format("服务:%.1f", scoreService);

			if (scoreProduct <= 0) {
				mTasteTv.setVisibility(View.GONE);
			} else {
				mTasteTv.setVisibility(View.VISIBLE);
			}

			if (scoreEnv <= 0) {
				mEnvTv.setVisibility(View.GONE);
			} else {
				mEnvTv.setVisibility(View.VISIBLE);
			}

			if (scoreService <= 0) {
				mServerTv.setVisibility(View.GONE);
			} else {
				mServerTv.setVisibility(View.VISIBLE);
			}

			mTasteTv.setText(LanguageConvertor.toLocale(ptxt));
			mEnvTv.setText(LanguageConvertor.toLocale(etxt));
			mServerTv.setText(LanguageConvertor.toLocale(stxt));

			String telephone = bpd.getTelephone();
			if (!TextUtils.isEmpty(telephone)) {
				mPhoneTv.setText(telephone);
				mCallLayout.setVisibility(View.VISIBLE);
				mPhoneTv.setVisibility(View.VISIBLE);
			} else {
				mCallLayout.setVisibility(View.GONE);
			}
		}
	}

	protected int getSoreMark(double score) {
		if (score < 1.0f) {
			return R.drawable.dz_icon_star0;
		} else if (score < 2.0f) {
			return R.drawable.dz_icon_star1;
		} else if (score < 3.0f) {
			return R.drawable.dz_icon_star2;
		} else if (score < 4.0f) {
			return R.drawable.dz_icon_star3;
		} else if (score < 5.0f) {
			return R.drawable.dz_icon_star4;
		} else if (score < 6.0f) {
			return R.drawable.dz_icon_star5;
		} else if (score < 7.0f) {
			return R.drawable.dz_icon_star6;
		} else if (score < 8.0f) {
			return R.drawable.dz_icon_star7;
		} else if (score < 9.0f) {
			return R.drawable.dz_icon_star8;
		} else if (score < 10.0f) {
			return R.drawable.dz_icon_star9;
		} else {
			return R.drawable.dz_icon_star10;
		}
	}

	@Override
	public void onBackPressed() {
		procBackPerform();
	}

	public abstract boolean onMapMarkerClick(Marker marker);

	public abstract void moveToMyLocation();

	public abstract void procPerformNavi();

	public abstract void makeCall(String name, String phone);

	public abstract void procPoiInfoLayoutPerform();

	public abstract void procBackPerform();

	public abstract void onItemSelected(int index);

	public abstract Poi getPoiByIndex(int index);
}

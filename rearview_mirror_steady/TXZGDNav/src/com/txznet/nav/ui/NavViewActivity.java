package com.txznet.nav.ui;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.AMapHudView;
import com.amap.api.navi.AMapHudViewListener;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.AsrUtil.IWakeupAsrCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.ui.dialog.WinConfirm;
import com.txznet.comm.ui.dialog.WinConfirmAsr;
import com.txznet.loader.AppLogic;
import com.txznet.nav.R;
import com.txznet.nav.manager.AMapInitor;
import com.txznet.nav.manager.NavManager;
import com.txznet.nav.manager.NaviDataManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class NavViewActivity extends BaseActivity {
	private static final String WAKEUP_TASK_ID = "NavViewActivity";

	public AMap mAMap;
	public AMapHudView mAMapHudView;

	public ImageView mDirIv;

	private Context mContext;
	private AMapNaviView mAMapNaviView;
	private WinConfirm mConfirmExitWin = null;

	private AMapInitor mInitor = null;

	public static void navigate(Context context) {
		Intent intent = new Intent(context, NavViewActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppLogic.addActivity(this);
		initBeforeSetContentView();
		setContentView(R.layout.activity_nav_lc);
		init(savedInstanceState);
	}

	private void initBeforeSetContentView() {
		mContext = AppLogic.getApp();
		if (AMapNavi.getInstance(mContext).getNaviPath() == null) {
			finish();
			return;
		}

		mConfirmExitWin = new WinConfirm() {
			@Override
			public void onClickOk() {
				NavManager.getInstance().stopNavi(true);
			}
		}.setMessage("?????????????????????");
	}

	private void init(Bundle savedInstanceState) {
		try {
			mAMapNaviView = (AMapNaviView) findViewById(R.id.nav_view);
			mAMapNaviView.onCreate(savedInstanceState);
			mAMapHudView = (AMapHudView) findViewById(R.id.hud_view);
			mAMapHudView.onCreate(savedInstanceState);
			mAMap = mAMapNaviView.getMap();

			if ("hud".equals(NavManager.getInstance().mNaviDisplayType)) {
				setUpHudMode(true);
			}

			mAMapHudView.setHudViewListener(new AMapHudViewListener() {

				@Override
				public void onHudViewCancel() {
					if (mAMapHudView.getVisibility() == View.VISIBLE) {
						mAMapHudView.setVisibility(View.GONE);
					}
				}
			});

			mInitor = new AMapInitor(mAMap, mAMapNaviView);
			mInitor.initBeforeGpsNaviStart();
			mInitor.entryNaviActivity((AMapNaviView) null, this);
			NaviCustomView.getInstance().lookAllPath(true, false);

			AppLogic.runOnUiGround(mGpsNavi, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	Runnable mEmulatorNavi = new Runnable() {

		@Override
		public void run() {
			AMapNavi.getInstance(AppLogic.getApp()).startNavi(AMapNavi.EmulatorNaviMode);
		}
	};

	Runnable mGpsNavi = new Runnable() {

		@Override
		public void run() {
			AMapNavi.getInstance(mContext).startNavi(AMapNavi.GPSNaviMode);
		}
	};

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mAMapNaviView.onSaveInstanceState(outState);
		mAMapHudView.onSaveInstanceState(outState);
	}

	IWakeupAsrCallback mAsrCommands = new AsrComplexSelectCallback() {
		@Override
		public String getTaskId() {
			return WAKEUP_TASK_ID;
		}

		@Override
		public boolean needAsrState() {
			return false;
		}

		public void onCommandSelected(String type, String command) {
			LogUtil.logd("trigger wakeup command: " + type + "-" + command);
			if (type.equals("ZOOM_OUT")) {
				AMapNavi.getInstance(mContext).pauseNavi();
				mAMap.animateCamera(CameraUpdateFactory.zoomOut());
				return;
			}
			if (type.equals("ZOOM_IN")) {
				AMapNavi.getInstance(mContext).pauseNavi();
				mAMap.animateCamera(CameraUpdateFactory.zoomIn());
				return;
			}
			if (type.equals("ZOMM_ALL")) {
				AMapNavi.getInstance(mContext).pauseNavi();
				mAMap.setLoadOfflineData(true);
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						NaviCustomView.getInstance().setCarLock(false);
						NaviCustomView.getInstance().performOverrideClick();
						AppLogic.removeUiGroundCallback(r);
						AppLogic.runOnUiGround(r, 10000);
					}
				}, 0);
				return;
			}
			if (type.equals("ASK_ROUTE")) {
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						AMapNavi.getInstance(mContext).readNaviInfo();
					}
				}, 0);
				return;
			}
			if (type.equals("ASK_TRAFFIC")) {
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						AMapNavi.getInstance(mContext).readTrafficInfo(5000);
					}
				}, 0);
				return;
			}
			if (type.equals("ASK_REMAIN")) {
				int t = NaviDataManager.getInstance().getRemainTime();
				int l = NaviDataManager.getInstance().getRemainDistance();
				StringBuilder speakTxt = new StringBuilder();
				if (l >= 1000)
					speakTxt.append("????????????" + (Math.round(l / 100.0) / 10.0) + "???????????????");
				else
					speakTxt.append("????????????" + l + "????????????");

				if (t >= 60) {
					if (t >= 3600) {
						String prefix = "";
						if (t >= 86400) {
							prefix = t / 86400 + "???";
							t = t % 86400;
						}
						int r = t % 3600;
						int h = t / 3600;
						int m = r / 60;
						speakTxt.append(prefix + h + "??????" + (m > 0 ? m + "??????" : ""));
					} else {
						speakTxt.append((t / 60) + "??????");
					}
				} else
					speakTxt.append(t + "???");
				TtsUtil.speakText(speakTxt.toString());
				return;
			}
			if (type.equals("OPEN_HUD")) {
				setUpHudMode(true);
			}
			if (type.equals("CLOSE_HUD")) {
				setUpHudMode(false);
			}
			if (type.equals("AVOID")) {
				if (AMapNavi.getInstance(mContext).getNaviPath().getStrategy() == AMapNavi.DrivingFastestTime) {
					TtsUtil.speakText("?????????????????????????????????????????????");
					return;
				}
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						WinConfirmAsr mWinConfirmWithAsr = new WinConfirmAsr() {
							@Override
							public void onClickOk() {
								TtsUtil.speakText("????????????????????????????????????????????????");
								AMapNavi.getInstance(mContext).reCalculateRoute(AMapNavi.DrivingFastestTime);
								NaviListener.getInstance().mIsRePlanByHand = true;
							}
						}.setSureText("??????", new String[] { "??????" }).setCancelText("??????", new String[] { "??????" })
								.setHintTts("?????????????????????????????????????????????").setMessage("?????????????????????????????????????????????");
						mWinConfirmWithAsr.show();
					}
				}, 0);
				return;
			}
			if (type.equals("LESS_MONEY")) {
				if (AMapNavi.getInstance(mContext).getNaviPath().getStrategy() == AMapNavi.DrivingSaveMoney) {
					TtsUtil.speakText("??????????????????????????????????????????");
					return;
				}
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						WinConfirmAsr mWinConfirmWithAsr = new WinConfirmAsr() {
							@Override
							public void onClickOk() {
								TtsUtil.speakText("?????????????????????????????????????????????");
								AMapNavi.getInstance(mContext).reCalculateRoute(AMapNavi.DrivingSaveMoney);
								NaviListener.getInstance().mIsRePlanByHand = true;
							}
						}.setSureText("??????", new String[] { "??????" }).setCancelText("??????", new String[] { "??????" })
								.setHintTts("??????????????????????????????????????????").setMessage("??????????????????????????????????????????");
						mWinConfirmWithAsr.show();
					}
				}, 0);
				return;
			}
			if (type.equals("LESS_DISTANCE")) {
				if (AMapNavi.getInstance(mContext).getNaviPath().getStrategy() == AMapNavi.DrivingShortDistance) {
					TtsUtil.speakText("??????????????????????????????????????????");
					return;
				}
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						WinConfirmAsr mWinConfirmWithAsr = new WinConfirmAsr() {
							@Override
							public void onClickOk() {
								TtsUtil.speakText("?????????????????????????????????????????????");
								AMapNavi.getInstance(mContext).reCalculateRoute(AMapNavi.DrivingShortDistance);
								NaviListener.getInstance().mIsRePlanByHand = true;
							}
						}.setSureText("??????", new String[] { "??????" }).setCancelText("??????", new String[] { "??????" })
								.setHintTts("??????????????????????????????????????????").setMessage("??????????????????????????????????????????");
						mWinConfirmWithAsr.show();
					}
				}, 0);
				return;
			}
			if (type.equals("CONTINUE")) {
				AMapNavi.getInstance(mContext).resumeNavi();
				NaviCustomView.getInstance().setCarLock(true);
				return;
			}
			if (type.equals("EXIT")) {
				if (this.isWakeupResult() == false) {
					TtsUtil.speakText("?????????????????????", new ITtsCallback() {
						public void onEnd() {
							AppLogic.runOnUiGround(new Runnable() {

								@Override
								public void run() {
									NavManager.getInstance().stopNavi(true);
								}
							}, 0);
						};
					});
					return;
				}
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						WinConfirmAsr mWinConfirmWithAsr = new WinConfirmAsr() {
							@Override
							public void onClickOk() {
								NavManager.getInstance().stopNavi(true);
							}
						}.setSureText("??????", new String[] { "??????", "??????" }).setCancelText("??????", new String[] { "??????" })
								.setHintTts("???????????????????????????").setMessage("???????????????????????????");
						mWinConfirmWithAsr.show();
					}
				}, 0);
				return;
			}
			if ("OPEN_TRAC".equals(type)) {
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						NaviCustomView.getInstance().checkTraffic(true);
					}
				}, 0);
			}
			if ("CLOSE_TRAC".equals(type)) {
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						NaviCustomView.getInstance().checkTraffic(false);
					}
				}, 0);
			}
			if ("SIMULATE".equals(type)) {
				AppLogic.runOnBackGround(mEmulatorNavi, 0);
				return;
			}
			if ("GPS".equals(type)) {
				AppLogic.runOnBackGround(mGpsNavi, 0);
				return;
			}
		};
	}.addCommand("ZOOM_OUT", "??????").addCommand("ZOOM_IN", "??????").addCommand("ZOMM_ALL", "????????????", "??????")
			.addCommand("ASK_ROUTE", "?????????", "?????????", "?????????").addCommand("ASK_TRAFFIC", "????????????", "?????????")
			.addCommand("ASK_REMAIN", "????????????", "????????????", "????????????").addCommand("OPEN_HUD", "??????HUD??????", "??????HUD")
			.addCommand("CLOSE_HUD", "??????HUD??????", "??????HUD", "??????HUD").addCommand("AVOID", "????????????")
			.addCommand("CONTINUE", "????????????", "??????").addCommand("LESS_MONEY", "?????????").addCommand("LESS_DISTANCE", "?????????")
			.addCommand("EXIT", "????????????", "????????????", "????????????").addCommand("OPEN_TRAC", "????????????").addCommand("CLOSE_TRAC", "????????????")
			.addCommand("SIMULATE", "????????????").addCommand("GPS", "????????????");

	public void setUpHudMode(boolean open) {
		int visible = mAMapHudView.getVisibility();
		if (open) {
			if (visible != View.VISIBLE) {
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						mAMapHudView.setVisibility(View.VISIBLE);
					}
				}, 0);
			}
		} else {
			if (visible != View.GONE) {
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						mAMapHudView.setVisibility(View.GONE);
					}
				}, 0);
			}
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (NaviCustomView.getInstance().dispatchTouchEvent(ev)) {
			return true;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onLoseFocus() {
		AsrUtil.recoverWakeupFromAsr(WAKEUP_TASK_ID);
		super.onLoseFocus();
	}

	@Override
	public void onGetFocus() {
		AsrUtil.useWakeupAsAsr(mAsrCommands);
		super.onGetFocus();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AMapNavi.getInstance(mContext).resumeNavi();
		LogUtil.logd("onResume set wakeup command");

		if (mAMapNaviView != null) {
			mAMapNaviView.onResume();
		}
		if (mAMapHudView != null) {
			mAMapHudView.onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		LogUtil.logd("onPause clear wakeup command");
		if (mAMapHudView != null) {
			mAMapHudView.onPause();
		}
		if (mAMapNaviView != null) {
			mAMapNaviView.onPause();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mInitor != null) {
			mInitor.endNaviActivity();
		}
		if (mAMapHudView != null) {
			mAMapHudView.onDestroy();
		}
		mAMap = null;
		mContext = null;
		mAMapHudView = null;
		mAMapNaviView = null;
		mConfirmExitWin = null;
	}

	@Override
	public void onBackPressed() {
		if (!NavManager.getInstance().mHasInitParams && mAMapHudView.getVisibility() == View.VISIBLE) {
			mAMapHudView.setVisibility(View.GONE);
			return;
		}

		if (!mConfirmExitWin.isShowing()) {
			mConfirmExitWin.show();
		} else {
			mConfirmExitWin.dismiss();
		}
	}

	Runnable r = new Runnable() {

		@Override
		public void run() {
			AMapNavi.getInstance(mContext).resumeNavi();
		}
	};

	public void addTestMarker() {
		MarkerOptions mo = new MarkerOptions();
		mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.caricon));
		mo.position(new LatLng(22.537984, 113.952429));
		mLat = 22.537984;
		mLng = 113.952429;
		mMarker = mAMap.addMarker(mo);
		AppLogic.removeBackGroundCallback(mCheckRunnable);
		AppLogic.runOnBackGround(mCheckRunnable, 20);
	}

	private Marker mMarker = null;

	double mLat, mLng;

	Runnable mCheckRunnable = new Runnable() {

		@Override
		public void run() {
			mLat += 0.00001;
			mLng += 0.00001;
			mMarker.setPosition(new LatLng(mLat, mLng));
			AppLogic.removeBackGroundCallback(mCheckRunnable);
			AppLogic.runOnBackGround(mCheckRunnable, 20);
		}
	};
}
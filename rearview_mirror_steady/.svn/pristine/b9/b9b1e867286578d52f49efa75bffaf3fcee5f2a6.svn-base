package com.txznet.nav.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.comapi.mapcontrol.BNMapController;
import com.baidu.navisdk.comapi.routeplan.BNRoutePlaner;
import com.baidu.navisdk.comapi.tts.BNavigatorTTSPlayer;
import com.baidu.navisdk.model.datastruct.LocData;
import com.baidu.navisdk.model.datastruct.SensorData;
import com.baidu.navisdk.ui.routeguide.BNavigator;
import com.baidu.navisdk.ui.routeguide.IBNavigatorListener;
import com.baidu.navisdk.ui.widget.RoutePlanObserver;
import com.baidu.navisdk.ui.widget.RoutePlanObserver.IJumpToDownloadListener;
import com.baidu.nplatform.comapi.map.MapGLSurfaceView;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.dialog.WinConfirm;
import com.txznet.nav.MyApplication;
import com.txznet.nav.NavManager;
import com.txznet.nav.R;
import com.txznet.nav.multinav.MultiNavManager;
import com.txznet.nav.multinav.MultiNavService;

public class NavStartActivity extends BaseActivity {

	private IBNavigatorListener mBNavigatorListener;
	private LinearLayout mllNavLayout;;
	private NavTtsPlayer mNavTtsPlayer;
	private View mNavView;
	private Bundle mBundle;

	private MultiNavService mMultiNav;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NavManager.getInstance().setIsNavi(true);
		init();

		setContentView(R.layout.activity_start);
		processIntent();

		mllNavLayout = (LinearLayout) findViewById(R.id.llNavLayout);

		// 创建NmapView
		if (Build.VERSION.SDK_INT < 14) {
			BaiduNaviManager.getInstance().destroyNMapView();
		}

		addNavView();

		try {
			if (NavManager.getInstance().isMultiNav()) {
				mMultiNav = MultiNavService.getInstance().init(nMapView,
						mNavView);
			}
		} catch (Exception e) {
			LogUtil.loge("MultiNavService.getInstance().init() error!");
		}

		BNavigator.getInstance().setListener(mBNavigatorListener);
		BNavigator.getInstance().startNav();

		BNRoutePlaner.getInstance().setObserver(
				new RoutePlanObserver(this, new IJumpToDownloadListener() {
					@Override
					public void onJumpToDownloadOfflineData() {
					}
				}));
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		processIntent();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		BNavigator.getInstance().resume();
		BNMapController.getInstance().onResume();
		catchExitButton();
		if (mMultiNav != null) {
			mMultiNav.onResume();
		}
	}

	@Override
	protected void onPause() {
		BNMapController.getInstance().onPause();
		BNavigator.getInstance().pause();
		super.onPause();
		if (mMultiNav != null) {
			mMultiNav.onPause();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mMultiNav != null) {
			mMultiNav.onStop();
		}
	}

	@Override
	protected void onDestroy() {
		mConfirmExitWin.dismiss();
		super.onDestroy();
		if (mMultiNav != null) {
			mMultiNav.onDestroy();
		}
	}

	private void processIntent() {
		Intent intent = getIntent();
		mBundle = intent.getBundleExtra("navi");
	}

	private void init() {
		mNavTtsPlayer = new NavTtsPlayer();
		BNavigatorTTSPlayer.setTTSPlayerListener(mNavTtsPlayer);
		mBNavigatorListener = new BNavigatorListener();

		initForMultiNav();
	}

	private void initForMultiNav() {
		try {
			if (MultiNavManager.getInstance().isTestEnv()) {
				MultiNavManager.getInstance().test();
			}

			if (NavManager.getInstance().isMultiNav()) {
				NavManager.getInstance().beginMultiNav();
			}
		} catch (Exception e) {
			LogUtil.loge("beginMultiNav error!");
		}

	}

	private void catchExitButton() {
		final int EXIT_BUTTON_ID = 2131165343;
		if (mNavView == null)
			return;
		try {
			ImageButton bt = (ImageButton) mNavView
					.findViewById(EXIT_BUTTON_ID);
			bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					doClickExit();
				}
			});
		} catch (Exception e) {
			LogUtil.loge("bt == null!");
		}
	}

	private MapGLSurfaceView nMapView;

	private void addNavView() {
		if (mllNavLayout != null) {

			nMapView = BaiduNaviManager.getInstance().createNMapView(this);
			if (nMapView == null) {
				MyApplication.showToast("创建导航视图失败(createNMapView)！");
				LogUtil.loge("BaiduNaviManager.getInstance().createNMapView fail: return null.");
				return;
			}

			// 创建导航视图
			mNavView = BNavigator.getInstance().init(this, mBundle, nMapView);
			if (mNavView == null) {
				MyApplication.showToast("创建导航视图失败(BNavigator.init)！");
				LogUtil.loge("BNavigator.getInstance().init fail: return null.");
				return;
			}

			ViewParent parent = mNavView.getParent();
			if (parent != null) {
				((ViewGroup) parent).removeView(mNavView);
			}
			mllNavLayout.removeAllViews();
			mllNavLayout.addView(mNavView);
		} else {
			LogUtil.loge("mllNavLayout is null.");
		}
	}

	private class BNavigatorListener implements IBNavigatorListener {
		@Override
		public void onYawingRequestSuccess() {
		}

		@Override
		public void onYawingRequestStart() {
		}

		@Override
		public void onPageJump(int jumpTiming, Object arg) {
			if (IBNavigatorListener.PAGE_JUMP_WHEN_GUIDE_END == jumpTiming
					|| IBNavigatorListener.PAGE_JUMP_WHEN_ROUTE_PLAN_FAIL == jumpTiming) {
				mNavTtsPlayer.disbaled();
				MyApplication.getInstance().finishActivity(
						NavStartActivity.this);
				if (IBNavigatorListener.PAGE_JUMP_WHEN_ROUTE_PLAN_FAIL == jumpTiming) {
					LogUtil.loge("路径规划失败");
				}
			}
		}

		@Override
		public void notifyGPSStatusData(int arg0) {
		}

		@Override
		public void notifyLoacteData(LocData locData) {
			if (MultiNavManager.getInstance().isMultiNav()) {
				MultiNavManager.getInstance().updateCurrentUser(
						locData.latitude, locData.longitude, locData.direction);
			}
		}

		@Override
		public void notifyNmeaData(String arg0) {
			Log.d("BNav", "BNav -- > notifyNmeaData arg:" + arg0);
		}

		@Override
		public void notifySensorData(SensorData arg0) {
			Log.d("BNav", "BNav -- > notifyNmeaData arg:" + arg0.toString());
		}

		@Override
		public void notifyStartNav() {
			BaiduNaviManager.getInstance().dismissWaitProgressDialog();
		}

		@Override
		public void notifyViewModeChanged(int arg0) {
			Log.d("BNav", "BNav -- > notifyViewModeChanged arg:" + arg0);
		}
	}

	private void doClickExit() {
		mConfirmExitWin.show();
	}

	private WinConfirm mConfirmExitWin = new WinConfirm() {
		@Override
		public void onClickOk() {
			NavManager.getInstance().stopNavi();
		}
	}.setMessage("确定退出导航？");

	@Override
	public void onBackPressed() {
		if (!mConfirmExitWin.isShowing()) {
			mConfirmExitWin.show();
		} else {
			mConfirmExitWin.dismiss();
		}
	}
}

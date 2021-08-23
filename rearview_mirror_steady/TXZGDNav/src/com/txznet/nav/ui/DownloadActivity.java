package com.txznet.nav.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.ui.dialog.WinWaiting;
import com.txznet.loader.AppLogic;
import com.txznet.nav.R;
import com.txznet.nav.offline.DownloadMapManager;
import com.txznet.nav.offline.DownloadMapManager.OnInitListener;
import com.txznet.nav.offline.OfflineMapDownloadManager;
import com.txznet.nav.ui.n.DownloadingWin;
import com.txznet.nav.ui.n.OfflineWin;
import com.txznet.nav.ui.widget.TabViewGroup;
import com.txznet.nav.ui.widget.ViewPagerEx;

public class DownloadActivity extends BaseActivity {

	private TabViewGroup mTvg;
	private ViewPagerEx mViewPagerEx;
	private ViewPagerAdapter mAdapter;
	private WinWaiting mWinWaiting;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		findWidget();
		// initNew();
		initOld();
	}

	private void findWidget() {
		mTvg = (TabViewGroup) findViewById(R.id.tab_tvg);
		mViewPagerEx = (ViewPagerEx) findViewById(R.id.main_viewpager);
		mTvg.setViewPager(mViewPagerEx);
	}

	private void initOld() {
		List<View> views = new ArrayList<View>();
		ListView v1 = (ListView) LayoutInflater.from(this).inflate(
				R.layout.list, null);
		View v2 = LayoutInflater.from(this).inflate(R.layout.expandlist, null);

		DownloadWin.getInstance().init(v1);
		OfflineListView.getInstance().init(v2);

		views.add(v1);
		views.add(v2);

		mAdapter = new ViewPagerAdapter(this, views);
		mViewPagerEx.setAdapter(mAdapter);
		mViewPagerEx.setOffscreenPageLimit(1);

		AppLogic.runOnUiGround(showDialogRunnable, 0);

		DownloadMapManager.addOnInitListener(new OnInitListener() {

			@Override
			public void initSuccess() {
				AppLogic.removeUiGroundCallback(showDialogRunnable);
				AppLogic.removeUiGroundCallback(dismissDialogRunnable);
				AppLogic.runOnUiGround(dismissDialogRunnable, 0);
			}
		});

		if (DownloadMapManager.getInstance().mHasInited) {
			DownloadMapManager.invokeOnRefreshListener();
		}

		AppLogic.removeUiGroundCallback(dismissDialogRunnable);
		AppLogic.runOnUiGround(dismissDialogRunnable, 15000);
	}

	private void initNew() {
		List<View> views = new ArrayList<View>();
		View v1 = LayoutInflater.from(this).inflate(R.layout.expandlist, null);
		View v2 = LayoutInflater.from(this).inflate(R.layout.expandlist, null);

		DownloadingWin.getInstance().init(v1);
		OfflineWin.getInstance().init(v2);

		views.add(v1);
		views.add(v2);

		mAdapter = new ViewPagerAdapter(this, views);
		mViewPagerEx.setAdapter(mAdapter);
		mViewPagerEx.setOffscreenPageLimit(1);

		OfflineMapDownloadManager.getInstance().init();
	}

	private Runnable showDialogRunnable = new Runnable() {

		@Override
		public void run() {
			if (DownloadWin.getInstance().isInit()) {
				return;
			}

			showDialog();
		}
	};

	private Runnable dismissDialogRunnable = new Runnable() {

		@Override
		public void run() {
			dismissDialog();
		}
	};

	private void showDialog() {
		if (mWinWaiting == null) {
			mWinWaiting = new WinWaiting("正在加载数据");
		}
		mWinWaiting.show();
	}

	private void dismissDialog() {
		if (mWinWaiting != null && mWinWaiting.isShowing()) {
			mWinWaiting.dismiss();
			mWinWaiting = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DownloadMapManager.getInstance().destory();
		DownloadMapManager.getInstance().cancelAllBackgroundRunnable();
	}

	private class ViewPagerAdapter extends PagerAdapter {

		List<View> mViewList;

		public ViewPagerAdapter(Context context, List<View> views) {
			mViewList = views;
			if (mViewList == null) {
				mViewList = new ArrayList<View>();
			}
		}

		@Override
		public int getCount() {
			return mViewList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView(mViewList.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			((ViewPager) container).addView(mViewList.get(position), 0);
			return mViewList.get(position);
		}
	}
}

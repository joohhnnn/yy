package com.txznet.launcher.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.R;
import com.txznet.launcher.bean.AppInfo;
import com.txznet.launcher.ui.widget.ImageTextView;
import com.txznet.launcher.ui.widget.MyViewPager;
import com.txznet.launcher.util.AllAppsUtils;
import com.txznet.loader.AppLogic;

public class AllAppView extends Activity {

	private AppInstallReceiver mAppInstallReceiver;
	private final int NUMS_PER_PAGE = 8;
	private MyViewPager mViewPager;
	private ArrayList<GridView> mPageViews;
	private ArrayList<GridViewAdapter> mGridViewAdapters;
	private ViewPageAdapter mViewPageAdapter;
	private ViewPageChangeListener mViewPageChangeListener;
	private ArrayList<AppInfo> mAllApps;
	private LinearLayout mLlIndicator;
	private ImageView[] mIndicators;
	private int mCurrentPage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launcher_view_pager);
		init();
		setupViews();

		if (mAppInstallReceiver == null) {
			mAppInstallReceiver = new AppInstallReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
			intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
			intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			intentFilter.addDataScheme("package");
			registerReceiver(mAppInstallReceiver, intentFilter);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		refreshPageView();
		refreshIndicator();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mAppInstallReceiver);
		mAppInstallReceiver = null;
		super.onDestroy();
	}

	private void init() {
		mPageViews = new ArrayList<GridView>();
		mGridViewAdapters = new ArrayList<GridViewAdapter>();

		mViewPager = (MyViewPager) findViewById(R.id.vp_main);
		mViewPageAdapter = new ViewPageAdapter();
		mViewPageChangeListener = new ViewPageChangeListener();
		mViewPager.setAdapter(mViewPageAdapter);
		mViewPager.setOnPageChangeListener(mViewPageChangeListener);

		mLlIndicator = (LinearLayout) findViewById(R.id.llIndicator);
	}

	/**
	 * 设置view
	 */
	private void setupViews() {
		mPageViews.clear();
		mGridViewAdapters.clear();
		mLlIndicator.removeAllViews();
		mAllApps = AllAppsUtils.getAllApps(AllAppView.this);
		int pages = mAllApps.size() / 8;
		if (mAllApps.size() % 8 > 0)
			pages++;

		// 指示器
		mIndicators = new ImageView[pages];
		// 添加gridview
		for (int i = 0; i < pages; i++) {
			GridView gridView = (GridView) View.inflate(AllAppView.this, R.layout.launcher_resident_app_page, null);
			GridViewAdapter gridViewAdapter = new GridViewAdapter(i);
			gridView.setAdapter(gridViewAdapter);
			mGridViewAdapters.add(gridViewAdapter);
			mPageViews.add(gridView);

			// 添加指示器
			mIndicators[i] = new ImageView(AllAppView.this);
			float imagePadding = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP, 12, AllAppView.this
							.getResources().getDisplayMetrics());
			mIndicators[i].setPadding((int) imagePadding, 0,
					(int) imagePadding, 0);
			mLlIndicator.addView(mIndicators[i]);
		}
	}

	/**
	 * 刷新gridview
	 */
	private void refreshPageView() {
		int pages = mAllApps.size() / 8;
		if (mAllApps.size() % 8 > 0)
			pages++;
		mViewPageAdapter.notifyDataSetChanged();
		if (mCurrentPage >= pages)
			mCurrentPage--;
		mViewPager.setCurrentItem(mCurrentPage);

		for (int i = 0; i < pages; i++) {
			mGridViewAdapters.get(i).notifyDataSetChanged();
		}
	}

	/**
	 * 刷新底部原点指示器
	 */
	private void refreshIndicator() {
		for (int i = 0; i < mIndicators.length; i++) {
			if (i == mCurrentPage) {
				mIndicators[i].setImageResource(R.drawable.launcher_indicator_selected);
			} else {
				mIndicators[i].setImageResource(R.drawable.launcher_indicator_normal);
			}
		}
	}

	private class GridViewAdapter extends BaseAdapter {

		private int mPageIndex;

		public GridViewAdapter(int pageIndex) {
			mPageIndex = pageIndex;
		}

		@Override
		public int getCount() {
			int count = 0;
			int length = mAllApps.size();
			count = length - mPageIndex * NUMS_PER_PAGE;
			if (count > NUMS_PER_PAGE) {
				count = NUMS_PER_PAGE;
			}
			return count;
		}

		@Override
		public Object getItem(int position) {
			return mAllApps.get(position + mPageIndex * NUMS_PER_PAGE);
		}

		@Override
		public long getItemId(int position) {
			return position + mPageIndex * NUMS_PER_PAGE;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = new ImageTextView(AllAppView.this);
			}

			ImageTextView button = (ImageTextView) convertView;

			button.setMinimumWidth((int) getResources().getDimension(
					R.dimen.x150));
			button.setMinimumHeight((int) getResources().getDimension(
					R.dimen.x160));
			String text = new String(mAllApps.get(
					position + mPageIndex * NUMS_PER_PAGE).getAppName());
			button.setMaxText(6);
			button.setText(text);
			button.setTextSize(20);
			button.setTextColor(AppLogic.getApp().getResources()
					.getColor(android.R.color.white));
			button.setImageDrawable(mAllApps.get(
					position + mPageIndex * NUMS_PER_PAGE).getIcon());
			// 尺寸不为96时可能有问题，因为利用Matrix缩放时有些缩放比例会导致图片严重失真。
			button.setImageMiniHeight((int) getResources().getDimension(
					R.dimen.y96));
			button.setImageMiniWidth((int) getResources().getDimension(
					R.dimen.x96));
			button.setImageWidthHeight(
					(int) getResources().getDimension(R.dimen.x96),
					(int) getResources().getDimension(R.dimen.y96));
			button.setTextImageSpace((int) getResources().getDimension(
					R.dimen.x6));
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					int position = (Integer) v.getTag();
					AppInfo info = mAllApps.get(position + mPageIndex
							* NUMS_PER_PAGE);
					startActivity(info.getPackageName(), info.getClassName());
				}
			});

			button.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					String packageName = null;
					int position = 0;

					position = (Integer) v.getTag();
					packageName = mAllApps.get(
							position + mPageIndex * NUMS_PER_PAGE)
							.getPackageName();

					AllAppsUtils.uninstallApp(AllAppView.this, packageName);
					return true;
				}

			});

			button.setTag(position);

			return button;
		}
	}

	private class ViewPageAdapter extends PagerAdapter {

		// 销毁position位置的界面
		@Override
		public void destroyItem(View v, int position, Object arg2) {
			if (v == null || mPageViews == null)
				return;
			if (mPageViews.size() <= position)
				return;
			if (mPageViews.get(position) == null)
				return;
			((ViewPager) v).removeView(mPageViews.get(position));
		}

		// 获取当前窗体界面数
		@Override
		public int getCount() {
			if (mPageViews != null) {
				return mPageViews.size();
			}
			return 0;
		}

		// 初始化position位置的界面
		@Override
		public Object instantiateItem(View v, int position) {
			if (v == null || mPageViews == null)
				return null;
			if (mPageViews.size() <= position)
				return null;
			if (mPageViews.get(position) == null)
				return null;
			GridView gridView = mPageViews.get(position);
			if (gridView.getParent() != null)
				return null;
			((ViewPager) v).addView(mPageViews.get(position));
			return mPageViews.get(position);
		}

		// 判断是否由对象生成界面
		@Override
		public boolean isViewFromObject(View v, Object arg1) {
			return v == arg1;
		}
	}

	private void rebuildView() {
		init();
		setupViews();
		refreshPageView();
		refreshIndicator();
	}

	private class ViewPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int position) {
			mCurrentPage = position;
			refreshIndicator();
		}
	}

	/**
	 * 监听应用的增删改
	 * 
	 * @author ZYH
	 **/
	private class AppInstallReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			LogUtil.logi("onReceive");

			String action = intent.getAction();
			if (action.equals(Intent.ACTION_PACKAGE_ADDED)
					|| action.equals(Intent.ACTION_PACKAGE_REPLACED)
					|| action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						rebuildView();
					}
				}, 0);
			}
		}
	}

	private void startActivity(String packageName, String className) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		ComponentName cn = new ComponentName(packageName, className);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(cn);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		try {
			startActivity(intent);
		} catch (Exception e) {
			LogUtil.loge("start " + packageName + "fail!");
		}
	}
}

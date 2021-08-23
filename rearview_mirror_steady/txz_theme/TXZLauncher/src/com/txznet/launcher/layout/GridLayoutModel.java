package com.txznet.launcher.layout;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.R;
import com.txznet.launcher.bean.AppInfo;
import com.txznet.launcher.helper.ResidentApp;
import com.txznet.launcher.ui.base.ProxyContext;
import com.txznet.launcher.ui.widget.AppIcon;
import com.txznet.loader.AppLogic;

import java.util.ArrayList;
import java.util.List;

public class GridLayoutModel extends LayoutModel implements
		View.OnClickListener {
	public static final int TYPE = LayoutModel.LAYOUT_TYPE_GRID;

	/* 表格布局 */
	private ViewPager mViewPager;
	private ViewPagerAdapter mViewPagerAdapter;
	private LinearLayout mLlIndicator;
	private ImageView[] mIndicators;
	private int mPageCount;
	private int mCurrentPage;
	private boolean mIsViewVisiable;
	private List<AppIcon> appIcons;

	private List<View> mViews;

	public GridLayoutModel(Activity activity, ProxyContext proxyContext) {
		super(activity, proxyContext);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView("activity_main");
		initView();
		mViews = new ArrayList<View>();
	}

	private void initView() {
		mViewPager = (ViewPager) findViewById(R.id.launcher_view_pager);
		mLlIndicator = (LinearLayout) findViewById(R.id.llIndicator);
		appIcons = ResidentApp.buildAppIcons(mActivity, mProxyContext,
				GridLayoutModel.this);
		mViewPagerAdapter = new ViewPagerAdapter() {

			private int appIndex = 0;

			@Override
			public View createView(int position) {
				LayoutInflater inflater = LayoutInflater.from(mActivity);
				TableLayout layout = (TableLayout) inflater.inflate(
						R.layout.launcher_grid, null);
				layout.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return false;
					}
				});
				TableRow[] rows = new TableRow[2];
				for (int i = 0; i < rows.length; i++) {
					rows[i] = (TableRow) inflater.inflate(
							R.layout.launcher_grid_row, layout, false);
					layout.addView(rows[i]);
				}
				for (int i = 0; i < 8; i++) {
					boolean isPluginHolder = false;
					TableRow currRow = rows[i / 4];
					// 优先处理插件
					if (plugins != null) {
						for (Plugin plugin : plugins) {
							// 判断该位置是否被插件占据
							if (i + (position * 8) <= plugin.end
									&& i + (position * 8) >= plugin.start) {
								isPluginHolder = true;
								// 判断是否该插件的起始位置
								if (i + (position * 8) == plugin.start) {
									// TODO 插入插件
									View view = null;
									if (plugin.name.equals("digital_clock")) {
										view = inflater.inflate(
												R.layout.layout_digital_plugin,
												currRow, false);
									} else if (plugin.name.equals("weather")) {
										view = inflater.inflate(
												R.layout.layout_weather_plugin,
												currRow, false);
									} else if (plugin.name
											.equals("digital_clock_date")) {
										view = inflater
												.inflate(
														R.layout.layout_digital_plugin_date,
														currRow, false);
									} else if (plugin.name.equals("weather_pm")) {
										view = inflater.inflate(
												R.layout.layout_weather_pm,
												currRow, false);
									} else {
										view = new View(mActivity);
									}
									currRow.addView(view);
								}
							}
						}
					}
					// 插入普通元素
					if (!isPluginHolder) {
						View view = getAppIcon(position, i);
						TableRow.LayoutParams params = new TableRow.LayoutParams(
								0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
						view.setLayoutParams(params);
						currRow.addView(view);
					}
				}

				return layout;
			}

			public AppIcon getAppIcon(int page, int position) {
				int pluginSize = 0;
				if (plugins != null) {
					for (Plugin plugin : plugins) {
						// 判断该位置之前有多少个位置被插件占用
						if (plugin.end < (page * 8) + position) {
							pluginSize += plugin.end - plugin.start + 1;
						}
					}
				}
				int index = page * 8 + position - pluginSize;
				if (index >= 0 && index <= appIcons.size() - 1) {
					return appIcons.get(index);
				}
				return new AppIcon(mActivity);
			}

			@Override
			public int getCount() {
				int appCount = appIcons.size();
				int pluginSize = 0;
				if (plugins != null) {
					for (Plugin plugin : plugins) {
						pluginSize += plugin.end - plugin.start + 1;
					}
				}
				mPageCount = (appCount + pluginSize) / (2 * 4);
				if ((appCount + pluginSize) % (2 * 4) > 0)
					mPageCount++;
				return mPageCount;
			}
		};
		mViewPager.setAdapter(mViewPagerAdapter);
		mViewPager.setOffscreenPageLimit(mPageCount);// 设置缓存页数，可能占用内存会较大
		mViewPager.setCurrentItem(0);
		mViewPager
				.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						mCurrentPage = position;
						refreshIndicator();
					}
				});
		initIndicators(mViewPagerAdapter.getCount());
		refreshIndicator();
	}

	private void initIndicators(int pageCount) {
		mIndicators = new ImageView[pageCount];
		for (int i = 0; i < pageCount; i++) {
			mIndicators[i] = (ImageView) LayoutInflater.from(mActivity)
					.inflate(R.layout.launcher_indicator, null);
			int size = mActivity.getResources().getDimensionPixelSize(
					R.dimen.x10);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					size, size);
			layoutParams.rightMargin = mActivity.getResources()
					.getDimensionPixelSize(R.dimen.x12);
			layoutParams.leftMargin = mActivity.getResources()
					.getDimensionPixelSize(R.dimen.x12);
			mIndicators[i].setLayoutParams(layoutParams);
			mLlIndicator.addView(mIndicators[i]);
		}
	}

	/**
	 * 刷新底部原点指示器
	 */
	private void refreshIndicator() {
		for (int i = 0; i < mIndicators.length; i++) {
			if (i == mCurrentPage) {
				mIndicators[i].setImageDrawable(mProxyContext
						.getDrawable("launcher_indicator_selected"));
			} else {
				mIndicators[i].setImageDrawable(mProxyContext
						.getDrawable("launcher_indicator_normal"));
			}
		}
	}

	private void startActivity(String packageName, String className) {
		Intent intent = null;
		if (!TextUtils.isEmpty(className)) {
			intent = new Intent(Intent.ACTION_MAIN);
			ComponentName cn = new ComponentName(packageName, className);
			// intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setComponent(cn);
		} else {
			intent = mActivity.getPackageManager().getLaunchIntentForPackage(
					packageName);
		}
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		}
		try {
			mActivity.startActivity(intent);
		} catch (Exception e) {
			LogUtil.loge("start " + packageName + " fail!");
		}
	}

	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();
		AppInfo appInfo = ((AppIcon) v).getAppInfo();
		Intent intent = null;
		ComponentName componentName = null;
		if ("com.txznet.nav".equals(appInfo.getPackageName())) {
			ContentResolver contentResolver = AppLogic.getApp()
					.getContentResolver();
			Uri selectUri = Uri
					.parse("content://com.txznet.settings.DefaultNaviProvider/default_navi");
			Cursor cursor = contentResolver.query(selectUri, null, null, null,
					null);
			String packageName = null;
			String className = null;
			if (cursor != null) {
				while (cursor.moveToNext()) {
					packageName = cursor.getString(2);
					className = cursor.getString(3);
				}
				cursor.close();
			}
			if (packageName == null || packageName.equals("com.txznet.nav")) {
				startActivity("com.txznet.nav",
						"com.txznet.nav.ui.MainActivity");
			} else if (className != null && !className.isEmpty()) {
				startActivity(packageName, className);
			} else {
				intent = AppLogic.getApp().getPackageManager()
						.getLaunchIntentForPackage(packageName);
				try {
					mActivity.startActivity(intent);
				} catch (Exception e) {
					LogUtil.loge("start " + packageName + " fail!");
				}
			}
			return;
		} else {
			String p = appInfo.getPackageName();
			String c = appInfo.getClassName();
			startActivity(p, c);
			return;
		}
		/*
		 * switch (position) { case 1: ContentResolver contentResolver =
		 * MyApplication.getApp().getContentResolver(); Uri selectUri =
		 * Uri.parse
		 * ("content://com.txznet.settings.DefaultNaviProvider/default_navi");
		 * Cursor cursor = contentResolver.query(selectUri, null, null, null,
		 * null); String packageName = null; String className = null; if (cursor
		 * != null) { while (cursor.moveToNext()) { packageName =
		 * cursor.getString(2); className = cursor.getString(3); }
		 * cursor.close(); } if (packageName == null ||
		 * packageName.equals("com.txznet.nav")) {
		 * startActivity("com.txznet.nav", "com.txznet.nav.ui.MainActivity"); }
		 * else if (className != null && !className.isEmpty()) {
		 * startActivity(packageName, className); } else { intent =
		 * MyApplication.getApp().getPackageManager()
		 * .getLaunchIntentForPackage(packageName); try {
		 * mActivity.startActivity(intent); } catch (Exception e) {
		 * LogUtil.loge("start " + packageName + " fail!"); } } return; case 0:
		 * case 2: case 3: case 4: case 5: case 6: case 8: String p =
		 * appInfo.getPackageName(); String c = appInfo.getClassName();
		 * startActivity(p, c); return; case 7:
		 * MyApplication.getApp(MyApplication.class).openAllAppsView(); return;
		 * default: break; }
		 */
	}

	@Override
	public void onStart() {
		super.onStart();
		mIsViewVisiable = true;
	}

	@Override
	public void onStop() {
		super.onStop();
		mIsViewVisiable = false;
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (mIsViewVisiable && mViewPager != null) {
			mViewPager.setCurrentItem(0);
		}
	}

	private abstract class ViewPagerAdapter extends PagerAdapter {

		public Object instantiateItem(ViewGroup container, int position) {
			View view = null;
			view = createView(position);
			container.addView(view);
			return view;
		}

		public abstract View createView(int position);

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mViews.get(position));
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}

}

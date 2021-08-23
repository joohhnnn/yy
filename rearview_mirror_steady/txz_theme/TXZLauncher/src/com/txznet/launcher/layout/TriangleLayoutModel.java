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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.R;
import com.txznet.launcher.bean.AppInfo;
import com.txznet.launcher.helper.ResidentApp;
import com.txznet.launcher.ui.base.ProxyContext;
import com.txznet.launcher.ui.widget.AppIcon;
import com.txznet.loader.AppLogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS User on 2015/9/29.
 */
public class TriangleLayoutModel extends LayoutModel implements View.OnClickListener {
    public static final int TYPE = LayoutModel.LAYOUT_TYPE_TRIANGLE;

    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private LinearLayout mLlIndicator;
    private ImageView[] mIndicators;
    private int mPageCount;
    private int mCurrentPage;
    private boolean mIsViewVisiable;

    public TriangleLayoutModel(Activity act, ProxyContext proxyContext) {
        super(act, proxyContext);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView("activity_main");
        initView();
    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.launcher_view_pager);
        mLlIndicator = (LinearLayout) findViewById(R.id.llIndicator);

        mViewPagerAdapter = new ViewPagerAdapter() {
            private List<AppIcon> appIcons = ResidentApp.buildSmallAppIcons(mActivity, mProxyContext, TriangleLayoutModel.this);

            @Override
            public View createView(int position) {
                LinearLayout linearLayout = new LinearLayout(mActivity);
                linearLayout.setGravity(Gravity.CENTER);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                for (int i = 0; i < 5; i++) {
                    AppIcon appIcon = null;
                    if (position * 5 + i > appIcons.size() - 1) {
                        appIcon = new AppIcon(mActivity);
                    } else {
                        appIcon = appIcons.get(position * 5 + i);
                    }
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                    layoutParams.weight = 1;
                    layoutParams.gravity = Gravity.CENTER;
                    appIcon.setLayoutParams(layoutParams);
                    linearLayout.addView(appIcon);
                }
                return linearLayout;
            }

            @Override
            public int getCount() {
                int appCount = ResidentApp.RESIDENT_APP_PACKAGE.length;
                mPageCount = appCount / 8;
                if (appCount % 8 > 0)
                    mPageCount++;
                return mPageCount;
            }
        };
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
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
            mIndicators[i] = (ImageView) LayoutInflater.from(mActivity).inflate(R.layout.launcher_indicator, null);
            int size = mActivity.getResources().getDimensionPixelSize(R.dimen.x10);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
            layoutParams.rightMargin = mActivity.getResources().getDimensionPixelSize(R.dimen.x12);
            layoutParams.leftMargin = mActivity.getResources().getDimensionPixelSize(R.dimen.x12);
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
                mIndicators[i].setImageDrawable(mProxyContext.getDrawable("launcher_indicator_selected"));
            } else {
                mIndicators[i].setImageDrawable(mProxyContext.getDrawable("launcher_indicator_normal"));
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
            mActivity.startActivity(intent);
        } catch (Exception e) {
            LogUtil.loge("start " + packageName + "fail!");
        }
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
        if(mIsViewVisiable &&  mViewPager != null){
            mViewPager.setCurrentItem(0);
        }
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        AppInfo appInfo = ((AppIcon) v).getAppInfo();
        Intent intent = null;
        ComponentName componentName = null;
        switch (position) {
            case 1:
                ContentResolver contentResolver = AppLogic.getApp().getContentResolver();
                Uri selectUri = Uri.parse("content://com.txznet.settings.DefaultNaviProvider/default_navi");
                Cursor cursor = contentResolver.query(selectUri, null, null, null, null);
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
                    startActivity("com.txznet.nav", "com.txznet.nav.ui.MainActivity");
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
            case 0:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 8:
                String p = appInfo.getPackageName();
                String c = appInfo.getClassName();
                startActivity(p, c);
                return;
            case 7:
                AppLogic.openAllAppsView();
                return;
            default:
                break;
        }
    }

    private abstract class ViewPagerAdapter extends PagerAdapter {
        List<View> mViews = new ArrayList<View>();

        public Object instantiateItem(ViewGroup container, int position) {
            View view = null;
            if (mViews.size() - 1 <= position || mViews.get(position) == null) {
                view = createView(position);
                mViews.add(view);
            }
            container.addView(view);
            return view;
        }

        public abstract View createView(int position);

        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViews.get(position));
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

}

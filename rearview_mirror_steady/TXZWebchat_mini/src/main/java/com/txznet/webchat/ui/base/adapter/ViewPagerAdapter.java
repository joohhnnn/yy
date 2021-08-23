package com.txznet.webchat.ui.base.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by J on 2016/10/9.
 */

public class ViewPagerAdapter extends PagerAdapter {

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

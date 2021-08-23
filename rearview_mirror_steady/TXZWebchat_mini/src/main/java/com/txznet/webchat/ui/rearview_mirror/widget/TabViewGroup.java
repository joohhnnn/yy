package com.txznet.webchat.ui.rearview_mirror.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusOperationPresenter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusView;
import com.txznet.webchat.R;

import java.util.ArrayList;
import java.util.List;

public class TabViewGroup extends FrameLayout implements OnPageChangeListener, OnClickListener, IFocusView, IFocusOperationPresenter {

    private List<IconTextView> mIconTextViews;

    private ViewPager mViewPager;

    private View mContentView;

    private OnTabChangeListener mListener;

    public TabViewGroup(Context context) {
        this(context, null);
    }

    public TabViewGroup(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public TabViewGroup(Context context, AttributeSet attr, int defValue) {
        super(context, attr, defValue);
        setUpView(context);
        init();
    }

    @SuppressLint("InflateParams")
    private void setUpView(Context context) {
        mIconTextViews = new ArrayList<IconTextView>();
        mContentView = LayoutInflater.from(context).inflate(R.layout.icon_text_view_list_layout, null);

        IconTextView mPositionOne = (IconTextView) mContentView.findViewById(R.id.id_indicator_one);
        IconTextView mPositionTwo = (IconTextView) mContentView.findViewById(R.id.id_indicator_two);

        mIconTextViews.add(mPositionOne);
        mIconTextViews.add(mPositionTwo);

        for (IconTextView itv : mIconTextViews) {
            itv.setOnClickListener(this);
        }

        removeAllViews();
        addView(mContentView);
    }

    /**
     * 初始化，第一个选项选中
     */
    public void init() {
        resetOtherTab(-1);
        mIconTextViews.get(0).setIconAlpha(1.0f);
        mIconTextViews.get(0).setSel(true);
    }

    public void setOnTabChangeListener(OnTabChangeListener listener) {
        mListener = listener;
    }

    // 打开入口
    public void showTab(int index) {
        if (mIconTextViews.get(index).getVisibility() == GONE) {
            mIconTextViews.get(index).setEnabled(true);
            mIconTextViews.get(index).setVisibility(VISIBLE);
            onClickTabIndex(index);
        }
    }

    // 关闭入口
    public void hideTab(int index) {
        if (mIconTextViews.get(index).getVisibility() == VISIBLE) {
            mIconTextViews.get(index).setEnabled(false);
            mIconTextViews.get(index).setVisibility(GONE);
            onClickTabIndex((index + 1) % 2);
        }
    }

    /**
     * 获取视图
     *
     * @return
     */
    public View getTabView() {
        return mContentView;
    }

    /**
     * 绑定ViewPager
     *
     * @param viewPager
     */
    public void setViewPager(ViewPager viewPager) {
        if (viewPager == null) {
            return;
        }
        this.mViewPager = viewPager;
        this.mViewPager.setOnPageChangeListener(this);
    }

    /**
     * 根据index点击某一个tab
     *
     * @param index
     */
    public void onClickTabIndex(int index) {
        onClick(mIconTextViews.get(index));
    }

    /**
     * 启用点击效果
     *
     * @param enable
     * @param index
     */
    public void enableTabClick(boolean enable, int index) {
        for (int i = 0; i < mIconTextViews.size(); i++) {
            if (i == index) {
                mIconTextViews.get(i).setEnabled(enable);
            } else {
                mIconTextViews.get(i).setEnabled(!enable);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int position) {
        /*for (int i = 0; i < mIconTextViews.size(); i++) {
            if (i == position) {
                continue;
            }
            mIconTextViews.get(i).setSel(false);
        }
        mIconTextViews.get(position).setSel(true);*/
        resetOtherTab(-1);
        mIconTextViews.get(position).setIconAlpha(1.0f);
        mIconTextViews.get(position).setSel(true);
    }

    @Override
    public void onClick(View v) {

        resetOtherTab(-1);
        int[] viewId = getViewIdArray();
        for (int i = 0; i < viewId.length; i++) {
            if (viewId[i] == v.getId()) {
                switchToTab(i, false);
                break;
            }
        }
    }

    private void resetOtherTab(int position) {
        for (int i = 0; i < mIconTextViews.size(); i++) {
            if (i == position) {
                continue;
            }
            mIconTextViews.get(i).setIconAlpha(0.0f);
            mIconTextViews.get(i).setSel(false);
        }
    }

    public void switchToTab(int index, boolean smoothScroll) {
        if (View.VISIBLE != mIconTextViews.get(index).getVisibility()) {
            return;
        }

        mIconTextViews.get(index).setIconAlpha(1.0f);
        mIconTextViews.get(index).setSel(true);
        mViewPager.setCurrentItem(index, smoothScroll);

        if (mListener != null) {
            mListener.onTabChange(index);
        }
    }

    private int[] getViewIdArray() {
        return new int[]{R.id.id_indicator_one,
                R.id.id_indicator_two
        };
    }

    public View getTabAt(int index) {
        return mIconTextViews.get(index);
    }

    @Override
    public boolean onNavOperation(int operation) {
        switch(operation) {
            case FocusSupporter.NAV_BTN_NEXT:
                switchToTab(1, true);
                return true;

            case FocusSupporter.NAV_BTN_PREV:
                switchToTab(0, true);
                return true;
        }

        return false;
    }

    @Override
    public boolean showDefaultSelectIndicator() {
        return false;
    }

    @Override
    public void onNavGainFocus(Object rawFocus, int operation) {

    }

    @Override
    public void onNavLoseFocus(Object newFocus, int operation) {

    }

    public interface OnTabChangeListener {
        void onTabChange(int newIndex);
    }
}
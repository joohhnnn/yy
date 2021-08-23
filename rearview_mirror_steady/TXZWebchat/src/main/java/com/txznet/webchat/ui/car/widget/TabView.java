package com.txznet.webchat.ui.car.widget;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusOperationPresenter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusView;
import com.txznet.webchat.R;
import com.txznet.webchat.stores.WxThemeStore;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by J on 2016/10/8.
 */

public class TabView extends LinearLayout implements View.OnClickListener, IFocusView, IFocusOperationPresenter {
    @Bind(R.id.rl_tab_view_left)
    RelativeLayout mRlContainerLeft;
    @Bind(R.id.rl_tab_view_right)
    RelativeLayout mRlContainerRight;
    @Bind(R.id.tv_tab_view_text_left)
    TextView mTvLeft;
    @Bind(R.id.tv_tab_view_text_right)
    TextView mTvRight;
    @Bind(R.id.iv_tab_view_indicator_left)
    ImageView mIvLeft;
    @Bind(R.id.iv_tab_view_indicator_right)
    ImageView mIvRight;

    private int currentFocus = 0;
    private OnTabChangeListener mTabChangeListener;
    private ViewPager mViewPager;

    private GradientDrawable mDrawableIndicator;

    public TabView(Context context) {
        this(context, null);
    }

    public TabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        View v;
        if (WxThemeStore.get().isPortraitTheme()) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.layout_tab_view_portrait, this);
        } else {
            v = LayoutInflater.from(getContext()).inflate(R.layout.layout_tab_view, this);
        }

        ButterKnife.bind(this, v);
        mDrawableIndicator = (GradientDrawable) getResources().getDrawable(R.drawable.shape_tabhost_indicator_dot);
        // 重新设置下indicator颜色适配主题色替换
        mDrawableIndicator.setColor(getResources().getColor(R.color.color_accent));
        mIvLeft.setImageDrawable(mDrawableIndicator);
        mIvRight.setImageDrawable(mDrawableIndicator);
        mTvLeft.setText(getResources().getString(R.string.lb_login_wechat_title));
        mTvRight.setText(getResources().getString(R.string.lb_login_control_title));

        switchToTab(0);

        mRlContainerLeft.setOnClickListener(this);
        mRlContainerRight.setOnClickListener(this);
    }

    public void showTab(int index) {
        setTabEnabled(index, true);
    }

    public void hideTab(int index) {
        setTabEnabled(index, false);
    }

    public void setTabEnabled(int index, boolean enable) {
        int visibleStat = enable ? View.VISIBLE : View.GONE;

        if (index == 0) {
            mRlContainerLeft.setVisibility(visibleStat);
        } else {
            mRlContainerRight.setVisibility(visibleStat);
        }

        if (!enable && currentFocus == index) {
            switchToTab((index + 1) % 2);
        }
    }

    public boolean isTabEnabled(int index) {
        return (0 == index && View.VISIBLE == mRlContainerLeft.getVisibility())
                || (1 == index && View.VISIBLE == mRlContainerRight.getVisibility());
    }

    public void setOnTabChangeListener(OnTabChangeListener listener) {
        mTabChangeListener = listener;
    }

    public void setViewPager(ViewPager vp) {
        this.mViewPager = vp;

        if (null != mViewPager) {
            mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    switchToTab(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    public void switchToTab(int index) {
        switchToTab(index, false);
    }

    public void switchToTab(int index, boolean smoothScroll) {
        if (!isTabEnabled(index)) {
            return;
        }

        currentFocus = index;
        if (0 == index) {
            mTvLeft.setTextColor(getResources().getColor(R.color.color_accent));
            mIvLeft.setVisibility(View.VISIBLE);
            mTvRight.setTextColor(getResources().getColor(R.color.color_primary_dark));
            mIvRight.setVisibility(View.GONE);
        } else {
            mTvLeft.setTextColor(getResources().getColor(R.color.color_primary_dark));
            mIvLeft.setVisibility(View.GONE);
            mTvRight.setTextColor(getResources().getColor(R.color.color_accent));
            mIvRight.setVisibility(View.VISIBLE);
        }

        if (mTabChangeListener != null) {
            mTabChangeListener.onTabChange(currentFocus);
        }

        if (mViewPager != null && mViewPager.getCurrentItem() != currentFocus) {
            mViewPager.setCurrentItem(currentFocus, smoothScroll);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_tab_view_left:
                switchToTab(0);
                break;

            case R.id.rl_tab_view_right:
                switchToTab(1);
                break;
        }
    }

    public interface OnTabChangeListener {
        void onTabChange(int currentIndex);
    }

    @Override
    public boolean onNavOperation(int operation) {
        switch (operation) {
            case FocusSupporter.NAV_BTN_BACK:
                switchToTab((currentFocus - 1 + 2) % 2);
                return true;

            case FocusSupporter.NAV_BTN_NEXT:
                switchToTab((currentFocus + 1) % 2);
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
}

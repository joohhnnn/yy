package com.txznet.webchat.ui.rearview_mirror;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.activity.ReserveStandardActivity2;
import com.txznet.webchat.R;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.WxThemeStore;
import com.txznet.webchat.ui.base.AppBaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * 远程控制能做什么页面
 * Created by J on 2016/3/28.
 */
public class BindReasonActivity extends AppBaseActivity {
    private static final String INTENT_DATA_IGNORE = "intent_data_ignore";

    @Bind(R.id.fl_bind_reason_root)
    FrameLayout mFlRoot;
    @Bind(R.id.btn_bind_reason_back)
    ImageButton mBtnBack;
    @Bind(R.id.vp_bind_reason_page)
    ViewPager mVpPage;
    @Bind(R.id.ll_bind_reason_dots)
    LinearLayout mGuideDots;

    private List<View> mGuideViews;

    @Override
    protected int getLayout() {
        String currentTheme = WxThemeStore.get().getCurrentTheme();
        if (WxThemeStore.THEME_CAR_PORTRAIT_T700.equals(currentTheme)) {
            return R.layout.activity_bind_reason_t700;
        }
        return R.layout.activity_bind_reason;
    }

    @Override
    protected Store[] getRegisterStores() {
        return new Store[0];
    }

    public static void show(Context context) {
        show(context, false);
    }

    public static void show(Context context, boolean ignoreLoginStatus) {
        Intent intent = new Intent(context, ReserveStandardActivity2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (ignoreLoginStatus) {
            intent.putExtra(INTENT_DATA_IGNORE, true);
        }
        context.startActivity(intent);
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        // init theme
        String currentTheme = WxThemeStore.get().getCurrentTheme();
        if (WxThemeStore.THEME_CAR_PORTRAIT_T700.equals(currentTheme)) {
            // 定制主题不需要资源替换逻辑
        } else {
            mFlRoot.setBackground(getResources().getDrawable(R.drawable.shape_activity_background_login));
        }


        initGuidePager();

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (getIntent().hasExtra(INTENT_DATA_IGNORE)) {
            mIgnoreLoginStatus = true;
        }
    }

    @Override
    protected void initFocusViewList() {
        getNavBtnSupporter().setViewList(mBtnBack);
    }

    @Subscribe
    @Override
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }

    private void initGuidePager() {
        initGuideViews();
        mVpPage.setAdapter(new ViewPagerAdapter(mGuideViews));
        mVpPage.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                for (int i = 0; i < mGuideDots.getChildCount(); i++) {
                    if (i == arg0) {
                        mGuideDots.getChildAt(i).setSelected(true);
                    } else {
                        mGuideDots.getChildAt(i).setSelected(false);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    // 初始化原因介绍图片
    private void initGuideViews() {
        int[] imgRes = new int[]{
                //R.drawable.src_bind_reason_1,
                R.drawable.src_bind_reason_2,
                R.drawable.src_bind_reason_3,
                R.drawable.src_bind_reason_4};
        String[] contents = getResources().getStringArray(R.array.arr_bind_reason);
        String[] titles = getResources().getStringArray(R.array.arr_bind_reason_title);
        mGuideViews = new ArrayList<View>(imgRes.length);
        for (int i = 0, len = imgRes.length; i < len; i++) {
            mGuideViews.add(buildGuideView(imgRes[i], titles[i], contents[i]));
        }
        initGuideDots(imgRes.length);
    }

    // 初始化底部的圆点
    private void initGuideDots(int count) {
        for (int i = 0; i < count; i++) {
            mGuideDots.addView(buildDot());
        }
        mGuideDots.getChildAt(0).setSelected(true);
    }

    private View buildDot() {
        View v = LayoutInflater.from(AppLogic.getApp()).inflate(R.layout.layout_bind_reason_dot, null);
        ((ImageButton) v.findViewById(R.id.btn_dot)).setImageDrawable(getResources().getDrawable(R.drawable.selector_bind_reason_dot));
        return v;
    }

    private View buildGuideView(int img, String title, String content) {
        int layoutResId = WxThemeStore.get().isPortraitTheme() ?
                R.layout.item_bind_reason_portrait : R.layout.item_bind_reason;
        View v = LayoutInflater.from(AppLogic.getApp()).inflate(layoutResId, null);
        ImageView mIvImage = (ImageView) v.findViewById(R.id.iv_bind_reason_image);
        TextView mTvTitle = (TextView) v.findViewById(R.id.tv_bind_reason_title);
        TextView mTvContent = (TextView) v.findViewById(R.id.tv_bind_reason_content);
        mIvImage.setImageResource(img);
        mTvTitle.setText(title);
        mTvContent.setText(content);

        return v;
    }


    private class ViewPagerAdapter extends PagerAdapter {
        private List<View> data;

        public ViewPagerAdapter(List<View> data) {
            super();
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(data.get(position));
            return data.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(data.get(position));
        }
    }
}

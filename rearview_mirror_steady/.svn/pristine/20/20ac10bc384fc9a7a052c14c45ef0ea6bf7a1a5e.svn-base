package com.txznet.music.albumModule.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.R;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.ui.AudioBaseFragment;
import com.txznet.music.data.entity.Category;
import com.txznet.music.utils.AttrUtils;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.widget.LoadingView;
import com.txznet.music.widget.OnEffectiveClickListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

import static android.support.design.widget.TabLayout.MODE_SCROLLABLE;

/**
 * Created by brainBear on 2018/2/23.
 */

public class CategoryFragment extends AudioBaseFragment implements CategoryContract.View {


    private static final String TAG = "CategoryFragment:";

    @Bind(R.id.tab)
    TabLayout mTab;
    @Bind(R.id.vp_content)
    ViewPager mPage;
    @Bind(R.id.lv_loading)
    LoadingView mLoadingView;
    List<Category> mCategorys = new ArrayList<>();
    private CategoryPresenter mPresenter;

    private CategoryFragmentAdapter mAdapter;

    @Override
    public String getFragmentId() {
        return "CategoryFragment";
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        mPresenter.unregister();
        mPresenter = null;
    }

    @Override
    protected int getLayout() {
        if (ScreenUtils.isPhonePortrait()) {
            return R.layout.fragment_category_phone_portrait;
        }
        return R.layout.fragment_category;
    }

    @Override
    protected void initView(View view) {
        mTab.setTabTextColors(getResources().getColorStateList(R.color.type_text));
        mAdapter = new CategoryFragmentAdapter(getChildFragmentManager(), mCategorys);
        mTab.setTabMode(MODE_SCROLLABLE);


        if (ScreenUtils.isPhonePortrait()) {

        } else if (ScreenUtils.isVerticalUI()) {

        } else {
            //横屏版本的recyclerView高度定义，为了紧凑居中，其它版本不做处理
            ViewGroup.LayoutParams params = mPage.getLayoutParams();
            float icon_height = AttrUtils.getAttrDimension(getActivity(), R.attr.album_item_content_icon_height, 160);
            float padding_top = AttrUtils.getAttrDimension(getActivity(), R.attr.album_item_content_tv_padding_top, 10);
            float text_top = AttrUtils.getAttrDimension(getActivity(), R.attr.album_item_icon_with_text_top, 9);
            float margin_top = AttrUtils.getAttrDimension(getActivity(), R.attr.album_item_content_margin_top, 20);
            float text_size = AttrUtils.getAttrDimension(getActivity(), R.attr.text_size_h3, 22);
            int height = (int) (icon_height + padding_top + text_top + margin_top + text_size * 2 + 8);
            LogUtil.d("recyclerViewItem", "height==:" + height);
            params.height = height;
            mPage.setLayoutParams(params);
        }

        mLoadingView.setErrorHintListener(new OnEffectiveClickListener() {
            @Override
            public void onEffectiveClick(View v) {
                mPresenter.queryCategories();
            }
        });
    }


    @Override
    protected void initData(Bundle savedInstanceState) {
        mPresenter = new CategoryPresenter(this);
        mPresenter.register();
        mPresenter.queryCategories();
    }

    @Override
    public void showLoading() {
        mLoadingView.showLoading(R.drawable.fm_album_loading_rotate, R.drawable.fm_album_loading_icon, "正在加载中...");
    }

    @Override
    public void setPresenter(CategoryContract.Presenter presenter) {
    }

    @Override
    public void showCategories(List<Category> categories) {
        Logger.d(TAG, categories);
        mLoadingView.showContent();

        mCategorys.clear();
        mCategorys.addAll(onCategoriesFilter(categories));

        mTab.setSelectedTabIndicatorHeight(0);
        mPage.setAdapter(mAdapter);
        mPage.setOffscreenPageLimit(1);
        //Viewpager 去掉两侧的光晕效果
        mPage.setOverScrollMode(ViewPager.OVER_SCROLL_NEVER);
        mTab.setupWithViewPager(mPage);
        if(ScreenUtils.isPhonePortrait()){
            settingTabWidth();
        }

        mTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //主要做不同的处理
                if (mCategorys.size() > 0 && mCategorys.size() > tab.getPosition()) {
                    CategoryFragment.this.onTabSelected(mCategorys.get(tab.getPosition()));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void onTabSelected(Category category) {

    }

    @Override
    public void showError(int code) {
        mLoadingView.showError(Constant.RS_VOICE_SPEAK_TIPS_UNKNOWN, R.drawable.fm_me_no_result_network, Constant.RS_VOICE_MUSIC_CLICK_RETRY);
    }

    protected List<Category> onCategoriesFilter(List<Category> categories) {
        return categories;
    }


    public static class CategoryFragmentAdapter extends FragmentStatePagerAdapter {

        private List<Category> mCategories;

        public CategoryFragmentAdapter(FragmentManager fm, List<Category> categories) {
            super(fm);
            mCategories = categories;
        }

        @Override
        public Fragment getItem(int position) {
            return AlbumListFragment.newInstance(mCategories.get(position));
        }

        @Override
        public int getCount() {
            return null == mCategories ? 0 : mCategories.size();
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return mCategories.get(position).getDesc();
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            //java.lang.NullPointerException: Attempt to invoke virtual method 'void android.support.v4.app.FragmentManagerImpl.performPendingDeferredStart(android.support.v4.app.Fragment)' on a null object reference
            try {
                super.setPrimaryItem(container, position, object);
            } catch (Exception e) {

            }
        }

        //        public void replaceData(List<Category> categories) {
//            this.mCategories.clear();
//            this.mCategories.addAll(categories);
//        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            int position = mTab.getSelectedTabPosition() - 1;
            int offset = 0;
            Object initFragment;
            while (true) {
                if (offset == 2) {
                    break;
                }
                try {
                    initFragment = mAdapter.instantiateItem(null, position);
                    if (initFragment != null && initFragment instanceof AlbumListFragment) {
                        ((AlbumListFragment) initFragment).clearData();
                    }
                } catch (Exception e) {
                }
                position += 2;
                offset++;
            }
        } else {
            int position = mTab.getSelectedTabPosition();
            Object initFragment;
            try {
                initFragment = mAdapter.instantiateItem(null, position);
                if (initFragment != null && initFragment instanceof AlbumListFragment) {
                    ((AlbumListFragment) initFragment).refreshData();
                }
            } catch (Exception e) {
            }
            position = position - 1;
            int offset = 0;
            while (true) {
                if (offset == 2) {
                    break;
                }
                try {
                    initFragment = mAdapter.instantiateItem(null, position);
                    if (initFragment != null && initFragment instanceof AlbumListFragment) {
                        ((AlbumListFragment) initFragment).requestData();
                    }
                } catch (Exception e) {
                }
                position += 2;
                offset++;
            }
        }
    }

    public void settingTabWidth() {
        //了解源码得知 线的宽度是根据 tabView的宽度来设置的
        mTab.post(() -> {

            try {
                //拿到tabLayout的mTabStrip属性
                Field mTabStripField = mTab.getClass().getDeclaredField("mTabStrip");
                mTabStripField.setAccessible(true);

                LinearLayout mTabStrip = (LinearLayout) mTabStripField.get(mTab);
                int dp10 = getResources().getDimensionPixelSize(R.dimen.m12);

                for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                    View tabView = mTabStrip.getChildAt(i);

                    //拿到tabView的mTextView属性
                    Field mTextViewField = tabView.getClass().getDeclaredField("mTextView");
                    mTextViewField.setAccessible(true);

                    TextView mTextView = (TextView) mTextViewField.get(tabView);

                    tabView.setPadding(20, 3, 20, 3);

                    //因为我想要的效果是   字多宽线就多宽，所以测量mTextView的宽度
                    int width = 0;
                    width = mTextView.getWidth();
                    if (width == 0) {
                        mTextView.measure(0, 0);
                        width = mTextView.getMeasuredWidth();
                    }
                    width += tabView.getPaddingLeft() + tabView.getPaddingRight();
                    //设置tab左右间距为10dp  注意这里不能使用Padding 因为源码中线的宽度是根据 tabView的宽度来设置的
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                    params.width = width;
                    params.leftMargin = dp10;
                    params.rightMargin = dp10;
                    tabView.setLayoutParams(params);

                    tabView.invalidate();
                }

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        });
    }
}

package com.txznet.comm.ui.theme.test.smarthandyhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.viewfactory.data.SmartHandyHomeViewData;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.resholder.R;
import com.txznet.txz.util.MD5Util;

import java.util.ArrayList;
import java.util.List;

/**
 * 说明：
 *
 * @author xiaolin
 * create at 2020-11-07 10:22
 */
public class HomeLogoHolder {

    private static HomeLogoHolder instance = new HomeLogoHolder();

    public static HomeLogoHolder getInstance() {
        return instance;
    }

    private String lastDataMd5 = "";

    private View mRootView;
    private ViewPager viewPager;
    private TextView tvSkinName;
    private TextView tvSkinVip;
    private ImageButton imgBtnPrev;
    private ImageButton imgBtnNext;

    private int defaultSkinPosition = -1;// 默认皮肤


    public View getView() {
        if (mRootView == null) {
            Context context = UIResLoader.getInstance().getModifyContext();
            mRootView = LayoutInflater.from(context).inflate(R.layout.smart_handy_home_item_logo, (ViewGroup) null);
            init();
        }
        return mRootView;
    }

    private void init() {
        viewPager = mRootView.findViewById(R.id.viewPager);
        tvSkinName = mRootView.findViewById(R.id.tvSkinName);
        tvSkinVip = mRootView.findViewById(R.id.tvSkinVip);
        imgBtnPrev = mRootView.findViewById(R.id.imgBtnPrev);
        imgBtnNext = mRootView.findViewById(R.id.imgBtnNext);

        imgBtnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });
        imgBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });
    }

    public void update(SmartHandyHomeViewData.LogoData data) {
        try {
            String md5 = MD5Util.generateMD5(JSONObject.toJSONString(data));
            if (md5.equals(lastDataMd5)) {
                return;
            }
            lastDataMd5 = md5;
        } catch (Exception e) {
            e.printStackTrace();
            lastDataMd5 = null;
        }

        final ArrayList<SmartHandyHomeViewData.LogoData.Bean> beans = new ArrayList<>(data.beans);
        {
            SmartHandyHomeViewData.LogoData.Bean bean = new SmartHandyHomeViewData.LogoData.Bean();
            bean.skinName = "默认皮肤";
            bean.type = 1;
            bean.skinImage = R.drawable.logo_frame_large_smile_8 + "";
            beans.add(bean);
            defaultSkinPosition = beans.size() - 1;
        }


        viewPager.setAdapter(new MyPagerAdapter(beans));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                refreshSwitchPageButton(position, beans.size());
                SmartHandyHomeViewData.LogoData.Bean bean = beans.get(position);
                tvSkinName.setText(bean.skinName);
                switch (bean.type) {
                    case 1:// 普通皮肤
                        tvSkinVip.setVisibility(View.GONE);
                        break;
                    case 2:// VIP
                        tvSkinVip.setVisibility(View.VISIBLE);
                        tvSkinVip.setText("VIP");
                        break;
                    case 3:// 限免
                        tvSkinVip.setVisibility(View.VISIBLE);
                        tvSkinVip.setText("限免");
                        break;
                    default:
                        tvSkinVip.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        SmartHandyHomeViewData.LogoData.Bean firstBean = beans.get(0);
        tvSkinName.setText(firstBean.skinName);
        switch (firstBean.type) {
            case 1:// 普通皮肤
                tvSkinVip.setVisibility(View.GONE);
                break;
            case 2:// VIP
                tvSkinVip.setVisibility(View.VISIBLE);
                tvSkinVip.setText("VIP");
                break;
            case 3:// 限免
                tvSkinVip.setVisibility(View.VISIBLE);
                tvSkinVip.setText("限免");
                break;
        }

        refreshSwitchPageButton(0, beans.size());
    }

    /**
     * 刷新翻页按钮
     *
     * @param pos
     * @param size
     */
    private void refreshSwitchPageButton(int pos, int size) {
        if (size <= 1) {
            imgBtnPrev.setClickable(false);
            imgBtnPrev.setImageResource(R.drawable.smart_handy_icon_prepage_disable);
            imgBtnNext.setClickable(false);
            imgBtnNext.setImageResource(R.drawable.smart_handy_icon_nextpage_disable);
            return;
        }

        if (pos == 0) {
            imgBtnPrev.setClickable(false);
            imgBtnPrev.setImageResource(R.drawable.smart_handy_icon_prepage_disable);
        } else {
            imgBtnPrev.setClickable(true);
            imgBtnPrev.setImageResource(R.drawable.smart_handy_icon_prepage_able);
        }

        if (pos + 1 == size) {
            imgBtnNext.setClickable(false);
            imgBtnNext.setImageResource(R.drawable.smart_handy_icon_nextpage_disable);
        } else {
            imgBtnNext.setClickable(true);
            imgBtnNext.setImageResource(R.drawable.smart_handy_icon_nextpage_able);
        }
    }

    private class MyPagerAdapter extends PagerAdapter {

        private List<SmartHandyHomeViewData.LogoData.Bean> beans = new ArrayList<>();

        public MyPagerAdapter(List<SmartHandyHomeViewData.LogoData.Bean> beans) {
            this.beans = beans;
        }

        @Override
        public int getCount() {
            return beans.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Context context = UIResLoader.getInstance().getModifyContext();
            View view = LayoutInflater.from(context).inflate(R.layout.smart_handy_home_item_logo_item, null);
            ImageView ivImage = view.findViewById(R.id.ivImage);

            SmartHandyHomeViewData.LogoData.Bean bean = beans.get(position);
            if (position == defaultSkinPosition) {// 默认皮肤
                ivImage.setImageResource(Integer.parseInt(bean.skinImage));
            } else {
                loadDrawableByUrl(ivImage, bean.skinImage);
            }

            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(view, lp);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // super.destroyItem(container,position,object); 这一句要删除，否则报错
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private static void loadDrawableByUrl(final ImageView ivHead, String uri) {
        ImageLoaderInitialize.ImageLoaderImpl.getInstance().displayImage(uri, ivHead, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                super.onLoadingStarted(imageUri, view);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                if (loadedImage != null) {
                    ((ImageView) view).setImageBitmap(loadedImage);
                    view.setVisibility(View.VISIBLE);
                }
            }
        });
    }

}

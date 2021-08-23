package com.txznet.music.ui.splash;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.GlideApp;
import com.txznet.music.R;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.entity.PlayConfs;
import com.txznet.music.ui.IDelayLaunch;
import com.txznet.music.ui.base.BaseFragment;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.Logger;
import com.txznet.music.util.TimeManager;
import com.txznet.music.util.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;

/**
 * @author zackzhou
 * @date 2019/1/26,17:50
 */

public class SplashFragment extends BaseFragment {

    @Bind(R.id.iv_bg)
    ImageView ivBg;
    @Bind(R.id.iv_note)
    ImageView ivNote;
    @Bind(R.id.vg_icons)
    ViewGroup vgIcons;

    private long showTime = Configuration.DefVal.SPLASH_PAGE_SHOW_TIME;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());

    @Override
    protected int getLayout() {
        return R.layout.splash_fragment;
    }

    @Override
    protected void initView(View view) {
        AppLogic.runOnUiGround(() -> {
            // 背景图设置
            PlayConfs playConfs = AudioUtils.getConfig();
            List<PlayConfs.FlashPage> pages = playConfs.launchPage;
            Logger.d(Constant.LOG_TAG_SPLASH, "read cache=" + pages);
            if (pages != null && !pages.isEmpty()) {
                boolean hasSet = false;
                for (PlayConfs.FlashPage page : pages) {
                    try {
                        long start_time = mDateFormat.parse(page.start_time).getTime();
                        long end_time = mDateFormat.parse(page.end_time).getTime();
                        long now = TimeManager.getInstance().getTimeMillis();
                        Logger.d(Constant.LOG_TAG_SPLASH, "check splash,  if now=%s, in [start=%s, end=%s]", now, start_time, end_time);
                        if (now >= start_time && now <= end_time) {
                            if (new File(page.url).exists()) {
                                ivNote.setVisibility(View.GONE);
                                GlideApp.with(SplashFragment.this).load(page.url)
                                        .addListener(new RequestListener<Drawable>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                ivNote.setVisibility(View.VISIBLE);
                                                ivBg.setImageResource(R.drawable.splash_bg);
                                                beginTask();
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                beginTask();
                                                return false;
                                            }
                                        }).priority(Priority.IMMEDIATE).into(ivBg);
                                showTime = page.time;
                                hasSet = true;
                                break;
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Logger.e(Constant.LOG_TAG_SPLASH, "set splash error, msg=" + e);
                        beginTask();
                    }
                }
                if (!hasSet) {
                    ivBg.setImageResource(R.drawable.splash_bg);
                    ivNote.setImageResource(R.drawable.splash_note_bg);
                    beginTask();
                }
            } else {
                ivBg.setImageResource(R.drawable.splash_bg);
                ivNote.setImageResource(R.drawable.splash_note_bg);
                beginTask();
            }

            // logo设置
            List<String> launchLogo = playConfs.launchLogo;
            Logger.d(Constant.LOG_TAG_SPLASH, "read cache logo=" + launchLogo);
            if (launchLogo == null || launchLogo.size() == 0) {
                Logger.w(Constant.LOG_TAG_SPLASH, "cache logo not found, use default");
                vgIcons.removeAllViews();
                final int[] resIds = new int[]{R.drawable.bran_xmly_icon, R.drawable.brand_qq_music_icon, R.drawable.brand_kl_icon};
                for (int resId : resIds) {
                    ImageView imageView = genIconItem();
                    vgIcons.addView(imageView);
                    GlideApp.with(SplashFragment.this).load(resId).into(imageView);
                }
            } else {
                vgIcons.removeAllViews();
                for (String url : launchLogo) {
                    ImageView imageView = genIconItem();
                    vgIcons.addView(imageView);
                    GlideApp.with(SplashFragment.this).load(url).into(imageView);
                }
            }
        });
    }

    private void beginTask() {
        AppLogic.runOnUiGround(() -> {
            if (getActivity() != null) {
                if (getActivity() instanceof IDelayLaunch) {
                    ((IDelayLaunch) getActivity()).initContentView();
                }
            }
        }, 500);
        AppLogic.runOnUiGround(mHideSplashTask, 500 + showTime);
        AppLogic.runOnUiGround(() -> {
            if (getActivity() != null) {
                getActivity().findViewById(R.id.ll_content).setVisibility(View.VISIBLE);
            }
        }, showTime - 500 > 1500 ? showTime - 500 : 500);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
    }

    private ImageView genIconItem() {
        ImageView imageView = new ImageView(getContext());
        int width = getResources().getDimensionPixelSize(R.dimen.m24);
        int height = getResources().getDimensionPixelSize(R.dimen.m24);
        int margin = getResources().getDimensionPixelSize(R.dimen.m6);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        lp.leftMargin = margin;
        lp.rightMargin = margin;
        imageView.setLayoutParams(lp);
        return imageView;
    }

    private Runnable mHideSplashTask = () -> {
        if (!isDetached()) {
            dismissAllowingStateLoss();
            if (getActivity() != null) {
                if (getActivity() instanceof IDelayLaunch) {
                    ((IDelayLaunch) getActivity()).doLaunch();
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            getView().setFocusableInTouchMode(true);
            getView().requestFocus();
            getView().setOnKeyListener((view, i, keyEvent) -> {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                    Log.d(TAG, "onBackPressed");
                    Utils.onBackRun(getActivity());
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public void onDestroyView() {
        AppLogic.removeUiGroundCallback(mHideSplashTask);
        vgIcons.removeAllViews();
        ivNote.setImageDrawable(null);
        ivBg.setImageDrawable(null);
        super.onDestroyView();
    }
}

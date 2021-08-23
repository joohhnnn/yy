package com.txznet.music.ui.push;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.GlideApp;
import com.txznet.music.R;
import com.txznet.music.helper.DrawablePool;
import com.txznet.music.helper.LottieHelper;
import com.txznet.music.model.INormalCallback;
import com.txznet.music.model.PushInterceptor;
import com.txznet.music.util.Logger;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by ASUS User on 2016/12/15.
 */

public class PushNotification extends FrameLayout {

    private static final String TAG = Constant.LOG_TAG_PUSH + ":Notification";
    @Bind(R.id.iv_first_logo)
    ImageView ivFirstLogo;
    @Bind(R.id.tv_first_title)
    TextView tvFirstTitle;
    @Bind(R.id.ll_first_range)
    LinearLayout llFirstRange;
    @Bind(R.id.iv_second_logo)
    ImageView ivSecondLogo;
    @Bind(R.id.tv_second_title)
    TextView tvSecondTitle;
    @Bind(R.id.ll_second_range)
    LinearLayout llSecondRange;
    @Bind(R.id.tv_cancel)
    TextView tvCancel;
    @Bind(R.id.rl_root)
    ViewGroup rlRoot;
    @Bind(R.id.iv_playing)
    LottieAnimationView ivPlaying;

    private WindowManager mWinManager;
    private boolean isShow;

    //单行
    public static final int STYLE_SINGLE = 1;
    //两行
    public static final int STYLE_DOUBLE = 2;
    private WindowManager.LayoutParams mLayoutParam;
    private View mView;

    private String textCloseInit = "";

    private int mStyle = STYLE_SINGLE;


    public PushNotification(int style) {
        super(GlobalContext.get());
        initView(GlobalContext.get());
        mWinManager = (WindowManager) GlobalContext.get().getSystemService(Context.WINDOW_SERVICE);
        mLayoutParam = new WindowManager.LayoutParams();
        // TODO: 2019/1/24 继续推送界面
        changeLine(style);
        mStyle = style;
    }

    private void changeLine(int style) {
        ViewGroup.LayoutParams layoutParams = mView.findViewById(R.id.rl_root).getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        if (STYLE_DOUBLE == style) {
            layoutParams.height = ((int) GlobalContext.get().getResources().getDimension(R.dimen.m112));
        } else if (STYLE_SINGLE == style) {
            llSecondRange.setVisibility(GONE);
            layoutParams.height = ((int) GlobalContext.get().getResources().getDimension(R.dimen.m72));
        }
        mView.findViewById(R.id.rl_root).setLayoutParams(layoutParams);
    }


    private void initView(Context context) {
        mView = View.inflate(context, R.layout.push_notification_view, this);

        ButterKnife.bind(this, mView);
    }

    public void setFirstIcon(String url) {
        GlideApp.with(GlobalContext.get()).load(url)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(DrawablePool.get(R.drawable.window_default_icon))
                .transform(new RoundedCornersTransformation(getResources().getDimensionPixelOffset(R.dimen.m4), 0))
                .into(ivFirstLogo);
    }

    public void setFirstIcon(int drawableID) {
        ivFirstLogo.setImageResource(drawableID);
    }

    public void setSecondIcon(String url) {
        GlideApp.with(GlobalContext.get()).load(url)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(DrawablePool.get(R.drawable.window_default_icon))
                .transform(new RoundedCornersTransformation(getResources().getDimensionPixelOffset(R.dimen.m4), 0))
                .into(ivSecondLogo);
    }

    public void setSecondIcon(int drawableID) {
        ivSecondLogo.setImageResource(drawableID);
    }


    public void setFirstTitle(String text) {
        tvFirstTitle.setText(text);
    }

    public void setSecondTitle(String text) {
        tvSecondTitle.setText(text);
    }


    public void setOnItemSecondClickListener(OnClickListener listener) {
        llSecondRange.setOnClickListener(listener);
    }

    public void setOnItemFirstClickListener(OnClickListener listener) {
        llFirstRange.setOnClickListener(listener);
    }


    public void setCloseListener(OnClickListener listener) {
        tvCancel.setOnClickListener(listener);
    }

    public void setCloseText(String text) {
        textCloseInit = text;
        tvCancel.setText(text);
    }

    public void show() {
        Logger.d(TAG, "show:" + isShow);
        if (isShow) {
            return;
        }
        isShow = true;
        PushInterceptor.getInstance().showView(new INormalCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (!aBoolean) {
                    mLayoutParam.width = WindowManager.LayoutParams.MATCH_PARENT;
                    mLayoutParam.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    mLayoutParam.type = WindowManager.LayoutParams.TYPE_PHONE;
                    mLayoutParam.flags = 40;
                    mLayoutParam.format = PixelFormat.RGBA_8888;
                    mLayoutParam.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
                    mWinManager.addView(PushNotification.this, mLayoutParam);
                }
            }

            @Override
            public void onError() {

            }
        });

    }

    public void dismiss() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            AppLogic.runOnUiGround(this::dismiss, 0);
            return;
        }

        Logger.d(TAG, "dismiss:" + isShow);
        if (!isShow) {
            return;
        }

        isShow = false;

        if (PushInterceptor.getInstance().dismissView()) {
            return;
        }

        if (ivPlaying != null) {
            LottieHelper.cancelAnimation(ivPlaying);
        }

        try {
            mWinManager.removeView(this);
        } catch (Exception e) {
            //
        }
        mLayoutParam = null;

        // clear cache
        LottieHelper.removeLottieCompositionCache("asset_window_playing.json");
        LottieHelper.removeLottieCompositionCache("window_playing.json");

        GlideApp.with(GlobalContext.get()).pauseAllRequests();
        Glide.get(GlobalContext.get()).clearMemory();
    }

    public boolean isShow() {
        return isShow;
    }

    public void setCountDown(int count) {
        if (count > 0) {
            if (mStyle == STYLE_SINGLE) {
                tvCancel.setText(String.format(Locale.getDefault(), "%s(%ds)", textCloseInit, count));
            } else {
                //两行
                tvCancel.setText(String.format(Locale.getDefault(), "%s\n(%ds)", textCloseInit, count));
            }
        } else {
            tvCancel.setText(textCloseInit);
        }
    }

}

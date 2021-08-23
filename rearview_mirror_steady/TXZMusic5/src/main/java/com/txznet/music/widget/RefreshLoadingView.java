package com.txznet.music.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.R;

public class RefreshLoadingView extends LinearLayout {

    LottieAnimationView iv_loading;

    public RefreshLoadingView(Context context) {
        super(context);
        initView();
    }

    public RefreshLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RefreshLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.base_loading_view, this, true);
        iv_loading = findViewById(R.id.iv_loading);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (VISIBLE == ((ViewGroup) getParent()).getVisibility()) {
            if (BuildConfig.DEBUG) {
                Log.d(Constant.LOG_TAG_UI_DEBUG, "playAnimation");
            }
            iv_loading.playAnimation();
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(Constant.LOG_TAG_UI_DEBUG, "pauseAnimation");
            }
            iv_loading.pauseAnimation();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (BuildConfig.DEBUG) {
            Log.d(Constant.LOG_TAG_UI_DEBUG, "setVisibility " + visibility);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        iv_loading.cancelAnimation();
    }
}

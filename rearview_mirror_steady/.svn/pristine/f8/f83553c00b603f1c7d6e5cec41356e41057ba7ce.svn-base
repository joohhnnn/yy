package com.txznet.music.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.aspsine.swipetoloadlayout.SwipeRefreshTrigger;
import com.aspsine.swipetoloadlayout.SwipeTrigger;
import com.txznet.music.R;

/**
 * SwipeToLoadLayout Header
 *
 * @author zackzhou
 * @date 2019/1/10,10:24
 */

public class RefreshHeaderView extends RelativeLayout implements SwipeRefreshTrigger, SwipeTrigger {
    TextView tvState;
    LottieAnimationView ivPlaying;

    public RefreshHeaderView(Context context) {
        super(context);
        initView();
    }

    public RefreshHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RefreshHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // 初始化界面
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.swipe_refresh_header, this, true);
        tvState = findViewById(R.id.tv_state);
        ivPlaying = findViewById(R.id.iv_playing);

    }

    @Override
    public void onRefresh() {
        ivPlaying.setVisibility(VISIBLE);
        tvState.setText("加载中");
    }

    @Override
    public void onPrepare() {
        ivPlaying.setVisibility(VISIBLE);
        ivPlaying.playAnimation();
        tvState.setText("加载中");
    }

    @Override
    public void onMove(int yScrolled, boolean isComplete, boolean automatic) {
        if (!isComplete) {
            if (yScrolled >= getHeight()) {
                ivPlaying.setVisibility(GONE);
                tvState.setText("释放加载");
            } else {
                ivPlaying.setVisibility(GONE);
                tvState.setText("下拉加载");
            }
        } else {
            ivPlaying.setVisibility(GONE);
        }
    }


    @Override
    public void onRelease() {

    }

    @Override
    public void onComplete() {
        ivPlaying.setVisibility(VISIBLE);
        tvState.setText("加载中");
    }

    @Override
    public void onReset() {
        ivPlaying.setVisibility(VISIBLE);
        ivPlaying.cancelAnimation();
        tvState.setText("加载中");
        tvState.setTextColor(getResources().getColor(R.color.white_40));
    }

    public void noMore() {
        ivPlaying.setVisibility(GONE);
        tvState.setText("没有更多");
    }

    public void loadFailed() {
        ivPlaying.setVisibility(GONE);
        tvState.setText("加载失败");
        tvState.setTextColor(getResources().getColor(R.color.red));
    }

    public void destroy() {
        if (ivPlaying != null) {
            ivPlaying.cancelAnimation();
        }
    }
}

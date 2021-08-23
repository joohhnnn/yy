package com.txznet.comm.ui.theme.test.anim;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;

public class LogoAnimView extends IImageView {
    private static final String TAG = LogoAnimView.class.getSimpleName();

    public enum Anim {
        COOL, SPOILED, SMILE, DANCE
    }

    private FrameAnimation animCool;// 酷
    private FrameAnimation animSpoiled;// 撒娇
    private FrameAnimation animSmile;// 笑
    private FrameAnimation animDance;// 跳舞

    public LogoAnimView(Context context) {
        super(context);
        LogUtil.logd(WinLayout.logTag + "AnimNoneView: init");
        init();
    }

    public LogoAnimView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LogoAnimView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    //初始化资源
    private void init() {
        initReportAnim();

        //设置默认帧，避免在性能弱的设备上，动画加载有延时
        this.setImageDrawable(LayouUtil.getDrawable("logo_frame_cool_1"));
    }

    //初始化播报状态动画资源
    private void initReportAnim() {
        String name = "";
        animCool = new FrameAnimation();
        for (int i = 1; i <= 20; i++) {
            name = "logo_frame_large_cool_" + i;
            Drawable drawable = LayouUtil.getDrawable(name);
            animCool.addFrame(drawable, 42);
        }

        animSpoiled = new FrameAnimation();
        for (int i = 1; i <= 20; i++) {
            name = "logo_frame_large_spoiled_" + i;
            Drawable drawable = LayouUtil.getDrawable(name);
            animSpoiled.addFrame(drawable, 42);
        }

        animSmile = new FrameAnimation();
        for (int i = 1; i <= 20; i++) {
            name = "logo_frame_large_smile_" + i;
            Drawable drawable = LayouUtil.getDrawable(name);
            animSmile.addFrame(drawable, 42);
        }

        animDance = new FrameAnimation();
        for (int i = 1; i <= 20; i++) {
            name = "logo_frame_large_dance_" + i;
            Drawable drawable = LayouUtil.getDrawable(name);
            animDance.addFrame(drawable, 42);
        }
    }

    //开始动画
    @Override
    public void playStartAnim(int State) {

    }

    public void play(Anim anim) {
        FrameAnimation animation = null;
        switch (anim) {
            case COOL:
                animation = animCool;
                break;
            case SPOILED:
                animation = animSpoiled;
                break;
            case SMILE:
                animation = animSmile;
                break;
            case DANCE:
                animation = animDance;
                break;
        }

        animation.setOneShot(false);
        setImageDrawable(animation);
        animation.stop();
        animation.start();
    }

    //停止动画
    @Override
    public void playEndAnim(int State) {

    }

    @Override
    public void stopAllAnim() {

    }

    //回收帧动画资源
    @Override
    public void destory() {
        if (animCool != null) {
            animCool.destory();
            animCool = null;
        }
        if (animSpoiled != null) {
            animSpoiled.destory();
            animSpoiled = null;
        }
        this.setImageBitmap(null);
        this.setImageDrawable(null);
    }

}
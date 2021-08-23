package com.txznet.comm.ui.theme.test.anim;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.test.skin.SK;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.loader.AppLogicBase;

public class AnimNoneView extends IImageView {
    private static final String TAG = AnimNoneView.class.getSimpleName();

    private int reportNum;

    private FrameAnimation reportAnim1;//播报,眨眼
    private FrameAnimation reportAnim2;//播报,讲话
    private FrameAnimation listeningAnim1;//倾听,起始动画
    private FrameAnimation listeningAnim2;//倾听，重复动画
    private FrameAnimation handleAnim1;//处理,起始动画
    private FrameAnimation handleAnim2;//处理，重复动画
    private FrameAnimation handleAnim3;//处理，结束动画

    public AnimNoneView(Context context) {
        super(context);
        LogUtil.logd(WinLayout.logTag + "AnimNoneView: init");
        init();
    }

    public AnimNoneView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimNoneView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    //初始化资源
    private void init() {

        initReportAnim();
        initListeningAnim();
        initHandleAnim();

        //hasHandle = false;
        reportNum = 0;
        //设置默认帧，避免在性能弱的设备上，动画加载有延时
        this.setImageDrawable(LayouUtil.getDrawable("none_report_00000"));
    }

    //初始化播报状态动画资源
    private void initReportAnim() {
        String name = "";
        reportAnim1 = new FrameAnimation();
        reportAnim2 = new FrameAnimation();
//        for (int i = 1; i <= 14; i++) {
//            name = "logo_frame_bobao" + i;
//            Drawable drawable = LayouUtil.getDrawable(name);
//            reportAnim1.addFrame(drawable, 60);
//        }

//        for (int i = 1; i <= 14; i++) {
//            name = "logo_frame_bobao" + i;
//            Drawable drawable = LayouUtil.getDrawable(name);
//            reportAnim2.addFrame(drawable, 120);
//        }
        SK.AnimInfo animInfoBobao = SK.getAnim(SK.ANIM.bobao);
        for (int i = 0; i < animInfoBobao.pictureNames.length; i++) {
            Drawable drawable = SK.getDrawable(animInfoBobao.pictureNames[i]);
            reportAnim1.addFrame(drawable, animInfoBobao.duration);
            reportAnim2.addFrame(drawable, animInfoBobao.duration);
        }

        reportAnim1.setOnFrameAnimationListener(new FrameAnimation.OnFrameAnimationListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onEnd() {
                AppLogicBase.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        showReportAnim2();
                    }
                });
            }
        });
        reportAnim2.setOnFrameAnimationListener(new FrameAnimation.OnFrameAnimationListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onEnd() {
                if (++reportNum == 5) {
                    reportNum = 0;
                    AppLogicBase.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            showReportAnim1();
                        }
                    });
                } else if (reportNum > 0) {
                    AppLogicBase.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            showReportAnim2();
                        }
                    });
                }
            }
        });
    }

    //初始化倾听状态动画资源
    private void initListeningAnim() {
        String name = "";
        listeningAnim1 = new FrameAnimation();
        listeningAnim2 = new FrameAnimation();
//        for (int i = 1; i <= 25; i++) {
//        	name = "logo_frame_ting" + i;
//            Drawable drawable = LayouUtil.getDrawable(name);
//            listeningAnim1.addFrame(drawable, 60);
//        }
//        for (int i = 1; i <= 25; i++) {
//            name = "logo_frame_ting" + i;
//            Drawable drawable = LayouUtil.getDrawable(name);
//            listeningAnim2.addFrame(drawable, 60);
//        }

        SK.AnimInfo animInfoTing = SK.getAnim(SK.ANIM.ting);
        for (int i = 0; i < animInfoTing.pictureNames.length; i++) {
            Drawable drawable = SK.getDrawable(animInfoTing.pictureNames[i]);
            listeningAnim1.addFrame(drawable, animInfoTing.duration);
            listeningAnim2.addFrame(drawable, animInfoTing.duration);
        }

        listeningAnim1.setOnFrameAnimationListener(new FrameAnimation.OnFrameAnimationListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onEnd() {
                AppLogicBase.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        showListeningAnim2();
                    }
                });
            }
        });
    }

    private void showListeningAnim2() {
        this.setImageDrawable(listeningAnim2);
        listeningAnim2.setOneShot(false);
        listeningAnim2.stop();
        listeningAnim2.start();
    }

    //初始化处理状态动画资源
    private void initHandleAnim() {
        String name = "";
        handleAnim1 = new FrameAnimation();
        handleAnim2 = new FrameAnimation();
        handleAnim3 = new FrameAnimation();
//        for (int i = 1; i <= 16; i++) {
//            name = "logo_frame_sikao" + i;
//            Drawable drawable = LayouUtil.getDrawable(name);
//            handleAnim1.addFrame(drawable, 120);
//        }
//		for (int i = 1; i <= 16; i++) {
//        	name = "logo_frame_sikao" + i;
//            Drawable drawable = LayouUtil.getDrawable(name);
//            handleAnim2.addFrame(drawable, 120);
//        }
//		for (int i = 1; i <= 16; i++) {
//            name = "logo_frame_sikao" + i;
//            Drawable drawable = LayouUtil.getDrawable(name);
//            handleAnim3.addFrame(drawable, 120);
//        }

        SK.AnimInfo animInfoSikao = SK.getAnim(SK.ANIM.sikao);
        for (int i = 0; i < animInfoSikao.pictureNames.length; i++) {
            Drawable drawable = SK.getDrawable(animInfoSikao.pictureNames[i]);
            handleAnim1.addFrame(drawable, animInfoSikao.duration);
            handleAnim2.addFrame(drawable, animInfoSikao.duration);
            handleAnim3.addFrame(drawable, animInfoSikao.duration);
        }

        handleAnim1.setOnFrameAnimationListener(new FrameAnimation.OnFrameAnimationListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onEnd() {
                AppLogicBase.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        showHandleAnim2();
                    }
                });
            }
        });
    }

    private void showHandleAnim2() {
        if (handleAnim2 != null) {
            this.setImageDrawable(handleAnim2);
            handleAnim2.setOneShot(false);
            handleAnim2.stop();
            handleAnim2.start();
        }
    }

    private void showHandleAnim3() {
        if (handleAnim3 != null) {
            this.setImageDrawable(handleAnim3);
            handleAnim3.setOneShot(true);
            handleAnim3.stop();
            handleAnim3.start();
        }
    }

    private void showReportAnim1() {
        if (reportAnim1 != null) {
            this.setImageDrawable(reportAnim1);
            reportAnim1.setOneShot(true);
            reportAnim1.stop();
            reportAnim1.start();
        }
    }

    private void showReportAnim2() {
        if (reportAnim2 != null) {
            this.setImageDrawable(reportAnim2);
            reportAnim2.setOneShot(true);
            reportAnim2.stop();
            reportAnim2.start();
        }
    }

    //开始动画
    @Override
    public void playStartAnim(int State) {
        switch (State) {
            case 0:
                reportNum = 0;
                reportAnim1.setOneShot(false);
                this.setImageDrawable(reportAnim1);
                reportAnim1.stop();
                reportAnim1.start();
                //hasHandle = false;
                break;
            case 1:
                listeningAnim1.setOneShot(true);
                this.setImageDrawable(listeningAnim1);
                listeningAnim1.stop();
                listeningAnim1.start();
                break;
            case 2:
                //hasHandle = true;
                handleAnim1.setOneShot(true);
                this.setImageDrawable(handleAnim1);
                handleAnim1.stop();
                handleAnim1.start();
                break;
        }
    }

    //停止动画
    @Override
    public void playEndAnim(int State) {
        switch (State) {
            case 0:
                reportNum = -1;
                this.setImageDrawable(reportAnim1);
                reportAnim1.stop();
                reportAnim2.stop();
                break;
            case 1:
                this.setImageDrawable(listeningAnim1);
                listeningAnim1.stop();
                listeningAnim2.stop();
                break;
            case 2:
                this.setImageDrawable(handleAnim1);
                handleAnim1.stop();
                handleAnim2.stop();
                handleAnim3.stop();
				/*this.setImageDrawable(handleAnim2);
				handleAnim2.start();*/
                break;
        }
    }

    @Override
    public void stopAllAnim() {
        reportNum = -1;
        reportAnim1.stop();
        reportAnim2.stop();
        listeningAnim1.stop();
        listeningAnim2.stop();
        handleAnim1.stop();
        handleAnim2.stop();
        handleAnim3.stop();
    }


    //回收帧动画资源
    @Override
    public void destory() {
        if (reportAnim1 != null) {
            reportAnim1.destory();
            reportAnim1 = null;
        }
        if (reportAnim2 != null) {
            reportAnim2.destory();
            reportAnim2 = null;
        }
        if (listeningAnim1 != null) {
            listeningAnim1.destory();
            listeningAnim1 = null;
        }
        if (listeningAnim2 != null) {
            listeningAnim2.destory();
            listeningAnim2 = null;
        }
        if (handleAnim1 != null) {
            handleAnim1.destory();
            handleAnim1 = null;
        }
        if (handleAnim2 != null) {
            handleAnim2.destory();
            handleAnim2 = null;
        }
        if (handleAnim3 != null) {
            handleAnim3.destory();
            handleAnim3 = null;
        }

        this.setImageBitmap(null);
        this.setImageDrawable(null);
    }

}
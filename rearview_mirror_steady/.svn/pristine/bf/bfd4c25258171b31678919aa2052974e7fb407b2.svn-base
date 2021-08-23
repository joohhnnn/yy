package com.txznet.comm.ui.theme.test.anim;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;

public class FrameAnimation extends AnimationDrawable {
    private Handler handler;
    private Runnable runnable;
    private OnFrameAnimationListener onFrameAnimationListener;
    private int maxDuration;
    public FrameAnimation() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                //获取最后一帧，和当前帧做比较，如果相等，就结束动画，调用动画结束回调
                if (getFrame(getNumberOfFrames() - 1) != getCurrent()) {
                    initHandler(); //如果不是最后一帧，重新启动handler
                } else {
                    unRunning(); //如果是最后一帧，触发结束回调
                }
            }
        };
    }

    /**
     * 动画不在运行，触发结束回调
     */
    private void unRunning() {
        if (onFrameAnimationListener != null) {
            onFrameAnimationListener.onEnd();//触发动画停止回调
        }
        handler.removeCallbacks(runnable);
    }

    /**
     * 重写开始方法监听动画是否结束
     */
    @Override
    public void start() {
        super.start();
        initHandler();
        if (onFrameAnimationListener != null) {
            onFrameAnimationListener.onStart();//触发动画开始回调
        }
    }

    private void initHandler() {
        handler.postDelayed(runnable, maxDuration == 0 ? getMaxDuration() : maxDuration);
    }

    @Override
    public void stop() {
        super.stop();
        handler.removeCallbacks(runnable);
    }

    /**
     * 获取持续时间最长的帧的持续时间
     *
     * @return 时间  如果这一帧大于1秒，则返回 1 秒，否则返回这一帧的持续时间
     */
    private int getMaxDuration() {
        for (int i = 0; i < this.getNumberOfFrames(); i++) {
            if (maxDuration < getDuration(i)) {
                maxDuration = getDuration(i);
            }
        }

        return maxDuration > 1000 ? 1000 : maxDuration;
    }

    //回收bitmap资源
    public void destory(){
        this.stop();
        for (int i = 0;i < this.getNumberOfFrames();i++){
            Drawable frame = this.getFrame(i);
            if (frame instanceof BitmapDrawable){
                Bitmap mBitmap = ((BitmapDrawable) frame).getBitmap();
                if (mBitmap != null && !mBitmap.isRecycled()){
                    //mBitmap.recycle();
                    mBitmap = null;
                }
            }
            frame.setCallback(null);
        }
        this.setCallback(null);

        //主动回收内存
        System.gc();
    }

    /**
     * 设置动画监听器
     *
     * @param onFrameAnimationListener 监听器
     */
    public void setOnFrameAnimationListener(OnFrameAnimationListener onFrameAnimationListener) {
        this.onFrameAnimationListener = onFrameAnimationListener;
    }

    /**
     * 动画监听器
     */
    public interface OnFrameAnimationListener {
        /**
         * 动画开始
         */
        void onStart();

        /**
         * 动画结束
         */
        void onEnd();
    }

}

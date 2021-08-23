package com.txznet.comm.ui.theme.test.anim;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

public abstract class IImageView extends ImageView {
    public IImageView(Context context) {
        super(context);
    }

    public IImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public IImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //开始动画
    public abstract void playStartAnim(int state);

    //停止动画
    public abstract void playEndAnim(int state);

	//停止所有动画
    public abstract void stopAllAnim();

    //回收动画资源
    public abstract void destory();
}

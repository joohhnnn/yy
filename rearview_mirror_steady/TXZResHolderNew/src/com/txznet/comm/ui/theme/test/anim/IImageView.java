package com.txznet.comm.ui.theme.test.anim;

import android.content.Context;
import android.widget.ImageView;

public abstract class IImageView extends ImageView {
    public IImageView(Context context) {
        super(context);
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

package com.txznet.launcher.listener;

import android.view.animation.Animation;

/**
 * 动画监听，适用于ScaleAnimation等旧的动画方法。
 * 主要作用是减少写监听要重写的方法，如果没有这个每次监听都一定要重写三个方法即使我们不需要。有了这个我们可以根据需要重写需要的方法。
 */
public class SimpleAnimationListener implements Animation.AnimationListener{
    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}

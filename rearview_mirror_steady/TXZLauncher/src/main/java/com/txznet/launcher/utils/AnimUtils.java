package com.txznet.launcher.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ArrayRes;
import android.widget.ImageView;

public class AnimUtils {

    private AnimUtils() {
    }

    /**
     * 获取指定的帧动画
     */
    public static IAnim load(ImageView view, @ArrayRes int resId, int duration, boolean isRepeat) {
        return new FrameAnimation(view, getRes(view.getContext(), resId), duration, isRepeat);
    }

    /**
     * fixme 为什么getIntArray获取不到id呢？是什么原理？
     */
    public static int[] getRes(Context ctx, @ArrayRes int resId) {
        TypedArray typedArray = ctx.getResources().obtainTypedArray(resId);
        int len = typedArray.length();
        int[] resIdArr = new int[len];
        for (int i = 0; i < len; i++) {
            resIdArr[i] = typedArray.getResourceId(i, -1);
        }
        typedArray.recycle();
        return resIdArr;
    }
}
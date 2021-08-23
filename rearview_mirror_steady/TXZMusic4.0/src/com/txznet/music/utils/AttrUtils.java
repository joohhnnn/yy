package com.txznet.music.utils;

import android.content.Context;
import android.content.res.TypedArray;

import com.txznet.comm.base.ActivityStack;

/**
 * Created by brainBear on 2017/11/3.
 * 获取attr属性的工具类
 */

public class AttrUtils {

    private AttrUtils() {

    }


    public static float getAttrDimension(Context context, int attr, float defValue) {
        if (ActivityStack.getInstance().currentActivity() != null && !ActivityStack.getInstance().currentActivity().isDestroyed()) {
            context = ActivityStack.getInstance().currentActivity();
        }
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{attr});
        float dimension = typedArray.getDimension(0, defValue);
        typedArray.recycle();
        return dimension;
    }

}

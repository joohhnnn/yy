package com.txznet.music.utils;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;


/******************************************
 * 类描述： 动画工具类 类名称：AnimationUtil
 *
 * @version: 1.0
 * @author: shaoningYang
 * @time: 2015-4-27 14:56
 ******************************************/
public class AnimationUtil {

    /**
     * 创建一个平顺的不停旋转动画对象
     *
     * @param context
     * @return
     */
    public static Animation createSmoothForeverAnimation(Context context) {
        Context ctx = context == null ? GlobalContext.get() : context;
        Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.rotate_round);
        animation.setInterpolator(new LinearInterpolator());
        return animation;
    }

    /**
     * 创建一个平顺的不停旋转动画对象
     * 　逆时针旋转
     *
     * @param context
     * @return
     */
    public static Animation createSmoothForeverAnimationAntiClock(Context context) {
        Context ctx = context == null ? GlobalContext.get() : context;
        Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.rotate_round_anti_clock);
        animation.setInterpolator(new LinearInterpolator());
        return animation;
    }
}

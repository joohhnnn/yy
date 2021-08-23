package com.txznet.music.utils;

import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

/**
 * @author telenewbie
 * @version 创建时间：2016年3月17日 下午9:58:59
 * 
 */
public class AnimationUtils {

	public static Animation getRotateAnimation() {
		RotateAnimation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setRepeatCount(Animation.INFINITE);
		animation.setDuration(1000);
		animation.setFillAfter(true);
		animation.setInterpolator(new LinearInterpolator());// 匀速
		return animation;
	}
}

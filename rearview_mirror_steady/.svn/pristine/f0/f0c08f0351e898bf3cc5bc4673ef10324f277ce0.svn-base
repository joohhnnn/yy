package com.txznet.txz.util;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AnimDrawableUtil {
    private AnimDrawableUtil() {

    }

    public static void start(Drawable drawable) {
        if (drawable instanceof AnimationDrawable) {
            AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
            animationDrawable.start();
        }
    }

    public static void stop(Drawable drawable) {
        if (drawable instanceof AnimationDrawable) {
            AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
            if (animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
        }
    }

    public static void reset(Drawable drawable) {
        stop(drawable);
        if (drawable instanceof AnimationDrawable) {
            AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
            animationDrawable.selectDrawable(0);
//            try {
//                Method method = AnimationDrawable.class.getDeclaredMethod("setFrame", int.class, boolean.class, boolean.class);
//                method.setAccessible(true);
//                method.invoke(drawable, 0, true, false);
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
        }
    }
}

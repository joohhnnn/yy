package com.txznet.music.helper;

import android.support.v4.util.ArrayMap;
import android.support.v4.util.LruCache;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.LottieCompositionCache;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author zackzhou
 * @date 2019/6/24,19:31
 */

public class LottieHelper {
    private static final String TAG = "LottieHelper";

    private static LottieHelper sInstance = new LottieHelper();

    private Map<String, SoftReference<LottieComposition>> mLottieCompositionRef = new ArrayMap<>(1);

    public static LottieHelper get() {
        return sInstance;
    }

    public static void cancelAnimation(LottieAnimationView animationView) {
        animationView.cancelAnimation();
        animationView.setImageDrawable(null);
        try {
            Method method = LottieAnimationView.class.getDeclaredMethod("clearComposition");
            method.setAccessible(true);
            method.invoke(animationView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeLottieCompositionCache(String key) {
        try {
            Method method = LottieCompositionCache.class.getMethod("getInstance");
            LottieCompositionCache compositionCache = (LottieCompositionCache) method.invoke(null);
            Field field = LottieCompositionCache.class.getDeclaredField("cache");
            field.setAccessible(true);
            LruCache cache = (LruCache) field.get(compositionCache);
            cache.remove(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        mLottieCompositionRef.clear();
    }

    public static void release() {
        try {
            Method method = LottieCompositionCache.class.getMethod("getInstance");
            LottieCompositionCache compositionCache = (LottieCompositionCache) method.invoke(null);
            Field field = LottieCompositionCache.class.getDeclaredField("cache");
            field.setAccessible(true);
            LruCache cache = (LruCache) field.get(compositionCache);
            cache.evictAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.txznet.music.helper;

import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.SparseArray;

import com.txznet.comm.remote.GlobalContext;

import java.lang.ref.SoftReference;

/**
 * Glide加载
 *
 * @author zackzhou
 * @date 2019/1/21,17:24
 */

public class DrawablePool {

    private static SparseArray<SoftReference<Drawable>> sDrawablePool = new SparseArray<>();

    private DrawablePool() {

    }

    public synchronized static Drawable get(@DrawableRes int res) {
        if (res == 0) {
            return null;
        }
        Drawable drawable = null;
        SoftReference<Drawable> drawableRef = sDrawablePool.get(res);
        if (drawableRef == null || drawableRef.get() == null) {
            drawable = GlobalContext.get().getResources().getDrawable(res);
            sDrawablePool.put(res, new SoftReference<>(drawable));
        } else {
            drawable = drawableRef.get();
        }
        return drawable;
    }

    public synchronized static void remove(@DrawableRes int res) {
        sDrawablePool.remove(res);
    }

    public synchronized static void clear() {
        sDrawablePool.clear();
    }
}

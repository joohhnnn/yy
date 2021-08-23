package com.txznet.music.util;

import android.support.annotation.DrawableRes;
import android.util.SparseArray;
import android.widget.ImageView;

import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.helper.GlideHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zackzhou
 * @date 2019/6/18,18:49
 */

public class IvRecycler {
    private static class IvData {
        WeakReference<ImageView> ivRef;
        String url;
        @DrawableRes
        int res;
    }

    public interface ResumeInvoker {
        void onResume(ImageView iv, String url, @DrawableRes int res);
    }

    public interface RecycleInvoker {
        void onRecycle(ImageView iv);
    }

    private SparseArray<IvData> mIvCache = new SparseArray<>();

    public void mark(ImageView iv, String url, @DrawableRes int res) {
        if (iv != null) {
            IvData ivData = new IvData();
            ivData.ivRef = new WeakReference<>(iv);
            ivData.url = url;
            ivData.res = res;
            mIvCache.put(iv.hashCode(), ivData);
        }
    }

    public void clear() {
        mIvCache.clear();
    }

    public void resume(ImageView iv, ResumeInvoker invoker) {
        if (iv != null) {
            IvData ivData = mIvCache.get(iv.hashCode());
            if (ivData != null) {
                invoker.onResume(iv, ivData.url, ivData.res);
            }
        }
    }

    public void recycleAll(Object obj, RecycleInvoker invoker, boolean deep) {
        if (BuildConfig.DEBUG) {
            Logger.d(Constant.LOG_TAG_MONITOR, "recycleAll size=" + mIvCache.size());
        }
        List<Integer> keys = new ArrayList<>();
        for (int i = 0, len = mIvCache.size(); i < len; i++) {
            int key = mIvCache.keyAt(i);
            IvData ivData = mIvCache.get(key);
            if (ivData.ivRef != null && ivData.ivRef.get() != null) {
                if (invoker != null) {
                    invoker.onRecycle(ivData.ivRef.get());
                }
                if (deep) {
                    GlideHelper.clear(obj, ivData.ivRef.get());
                }
            } else {
                keys.add(key);
            }
        }
        for (Integer key : keys) {
            if (key != null) {
                mIvCache.remove(key);
            }
        }
    }

    public void resumeAll(Object obj, ResumeInvoker invoker) {
        if (BuildConfig.DEBUG) {
            Logger.d(Constant.LOG_TAG_MONITOR, "resumeAll size=" + mIvCache.size());
        }
        for (int i = 0, len = mIvCache.size(); i < len; i++) {
            int key = mIvCache.keyAt(i);
            IvData ivData = mIvCache.get(key);
            if (ivData.ivRef != null && ivData.ivRef.get() != null) {
                if (invoker != null) {
                    invoker.onResume(ivData.ivRef.get(), ivData.url, ivData.res);
                }
            }
        }
    }
}

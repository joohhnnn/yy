package com.txznet.comm.ui;

import android.graphics.Bitmap;
import android.util.LruCache;

public class BitmapCache {
    private LruCache<String, Bitmap> mCachePost;
    private static final BitmapCache INSTANCE = new BitmapCache();
    private BitmapCache(){
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        mCachePost = new LruCache<String, Bitmap>(maxMemory / 8);
    }

    public static BitmapCache getInstance() {
        return INSTANCE;
    }

    public Bitmap getBitmap(String key) {
        if (key == null) {
            return null;
        }
        return mCachePost.get(key);
    }

    public void putBitmap(String key, Bitmap bitmap) {
        mCachePost.put(key, bitmap);
    }
}

package com.txznet.music;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.txznet.music.util.Logger;
import com.txznet.proxy.util.StorageUtil;

/**
 * Created by telenewbie on 2017/9/25.
 */

@GlideModule
public class MyAppGlideModule extends AppGlideModule {
    private static final String TAG = Constant.LOG_TAG_Glide;

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context).build();
        int defaultMemoryCacheSize = calculator.getMemoryCacheSize();
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();

        int customMemoryCacheSize = (int) (0 * defaultMemoryCacheSize);
        int customBitmapPoolSize = (int) (0 * defaultBitmapPoolSize);

        Logger.d(Constant.LOG_TAG_Glide, "customMemoryCacheSize=" + StorageUtil.formatSize(customMemoryCacheSize));
        Logger.d(Constant.LOG_TAG_Glide, "customBitmapPoolSize=" + StorageUtil.formatSize(customBitmapPoolSize));

        builder.setMemoryCache(new LruResourceCache(customMemoryCacheSize));
        builder.setBitmapPool(new LruBitmapPool(customBitmapPoolSize));

        final int diskSize = 1024 * 1024 * 50;
        builder.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context, diskSize));
        //默认为565
        builder.setDefaultRequestOptions(RequestOptions.formatOf(DecodeFormat.PREFER_RGB_565).disallowHardwareConfig().diskCacheStrategy(DiskCacheStrategy.DATA).timeout(5000));

        if (BuildConfig.DEBUG) {
            builder.setLogLevel(Log.DEBUG);
        }
    }
}

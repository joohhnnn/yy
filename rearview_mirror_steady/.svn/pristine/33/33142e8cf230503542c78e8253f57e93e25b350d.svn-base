package com.txznet.loader;

import android.content.Context;
import android.os.Environment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.txznet.comm.remote.util.LogUtil;

import java.io.File;

/**
 * Created by telenewbie on 2017/9/25.
 */

@GlideModule
public class MyAppGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        super.registerComponents(context, glide, registry);
    }

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
//        int memClass = ((android.app.ActivityManager) context
//                .getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
//        memClass = memClass > 32 ? 32 : memClass;
        // 使用可用内存的1/8作为图片缓存
        final int cacheSize = 1024 * 1024 /** memClass / 32*/;
//        LogUtil.logd("music:test:size:" + memClass + "," + cacheSize);
        File cacheDirectory = context.getExternalCacheDir();
        LogUtil.logd("music:test:path:" + cacheDirectory.getAbsolutePath());
//
//        int memoryCacheSizeBytes = 1024 * 1024 * 20; // 20mb
        builder.setMemoryCache(new LruResourceCache(cacheSize));
//        builder.setBitmapPool(new BitmapPoolAdapter());
        builder.setDefaultRequestOptions(RequestOptions.timeoutOf(5000));
        int diskSize = 100 * 1024 * 1024;
//        new ExternalCacheDiskCacheFactory(context, "GlideCache", diskSize);
        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context,diskSize));
//        ActivityManager activityManager= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        if (activityManager!=null){
//            ActivityManager.MemoryInfo memoryInfo=new ActivityManager.MemoryInfo();
//            activityManager.getMemoryInfo(memoryInfo);
//            RequestOptions format = new RequestOptions().format(DecodeFormat.PREFER_RGB_565);
//            builder.setDefaultRequestOptions(format);
//        }
    }
}

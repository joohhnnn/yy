package com.txznet.loader;

import android.content.Context;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.txznet.webchat.Constant;

import java.io.File;

@GlideModule
public class MyAppGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        super.registerComponents(context, glide, registry);
    }

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        int memClass = ((android.app.ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        memClass = memClass > 32 ? 32 : memClass;
        // 使用可用内存的1/8作为图片缓存
        final int cacheSize = 1024 * 1024 * memClass / 32;
        builder.setMemoryCache(new LruResourceCache(cacheSize));
        builder.setBitmapPool(new BitmapPoolAdapter());
        builder.setDiskCache(new WxDiskCacheFactory("glideCache", 1024 * 1024 * 100));
        builder.setDefaultRequestOptions(new RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
                .timeout(3000));
    }

    class WxDiskCacheFactory extends DiskLruCacheFactory {

        public WxDiskCacheFactory(final String diskCacheName, final int diskCacheSize) {
            super(new CacheDirectoryGetter() {
                @Nullable
                private File getInternalCacheDirectory() {
                    File cacheDirectory = new File(Constant.PATH_HEAD_CACHE);
                    if (diskCacheName != null) {
                        return new File(cacheDirectory, diskCacheName);
                    }
                    return cacheDirectory;
                }

                @Override
                public File getCacheDirectory() {
                    File cacheDirectory = new File(Constant.PATH_HEAD_CACHE);
                    if (diskCacheName != null) {
                        return new File(cacheDirectory, diskCacheName);
                    }
                    return cacheDirectory;
                }
            }, diskCacheSize);
        }

        @Override
        public DiskCache build() {
            return super.build();
        }
    }
}

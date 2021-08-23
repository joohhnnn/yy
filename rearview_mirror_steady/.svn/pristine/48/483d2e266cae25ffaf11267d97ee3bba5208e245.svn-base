package com.txznet.txzcar;

import android.graphics.Bitmap;

import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.txznet.comm.base.BaseApplication;
import com.txznet.txzcar.util.ResUtil;

public class MyApplication extends BaseApplication{
	
	private static MemoryCache mMemoryCache;
	private static DisplayImageOptions mDefaultImageOptions;	
	@Override
	public void onCreate() {
		super.onCreate();
		ResUtil.getInstance();
		NavManager.getInstance();
		
		getApp().runOnBackGround(new Runnable() {
			
			@Override
			public void run() {
				initImageLoader();
			}
		}, 0);
	}
	
	@Override
	protected void destroy() {
		BaiduNaviManager.getInstance().uninit();
		super.destroy();
	}
	
	private void initImageLoader() {
        int memoryCacheSize = (int) (Runtime.getRuntime().maxMemory() / 5); // 内存的1/5
        mMemoryCache = new LruMemoryCache(memoryCacheSize);

        mDefaultImageOptions = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .resetViewBeforeLoading(false)
                .showImageForEmptyUri(R.drawable.default_headimage)
                .build();

        ImageLoaderConfiguration.Builder configBuilder = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(mDefaultImageOptions)
                .memoryCache(mMemoryCache)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .threadPoolSize(4);
        ImageLoaderConfiguration config = configBuilder.build();
        ImageLoader.getInstance().init(config);
    }
}

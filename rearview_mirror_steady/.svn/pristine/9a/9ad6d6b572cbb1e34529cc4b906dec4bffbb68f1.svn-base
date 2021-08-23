package com.txznet.loader;

import java.io.File;
import java.util.Stack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;

import com.amap.api.maps.MapsInitializer;
import com.amap.api.navi.AMapNavi;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.nav.R;
import com.txznet.nav.TimeService;
import com.txznet.nav.manager.DataSourceManager;

public class AppLogic extends AppLogicBase {
	private static MemoryCache mMemoryCache;
	private static DisplayImageOptions mDefaultImageOptions;

	private static Stack<Activity> mActivities = new Stack<Activity>();

	@Override
	public void onCreate() {
		super.onCreate();
		initImageLoader();
		runTimeObserverService();
		try {
			// MapsInitializer.initialize(GlobalContext.get());
			// test();
			// MapsInitializer.sdcardDir = "/mnt/ext_sd/txzNav";
		} catch (Exception e) {
			;
		} finally {
			// DataSourceManager.getInstance().resetSourcePath(false);
		}
	}

	private void test() {
		MapsInitializer.sdcardDir = Environment.getExternalStorageDirectory() + File.separator + "txzNav";
	}

	private void runTimeObserverService() {
		GlobalContext.get().startService(new Intent(GlobalContext.get(), TimeService.class));
	}

	@Override
	public void destroy() {
		super.destroy();
		AMapNavi.getInstance(AppLogic.getApp()).destroy();
	}

	private static void initImageLoader() {
		int memoryCacheSize = (int) (Runtime.getRuntime().maxMemory() / 5); // 内存的1/5
		mMemoryCache = new LruMemoryCache(memoryCacheSize);

		mDefaultImageOptions = new DisplayImageOptions.Builder().bitmapConfig(Bitmap.Config.RGB_565).cacheInMemory(true)
				.cacheOnDisk(false).resetViewBeforeLoading(false).showImageForEmptyUri(R.drawable.default_headimage)
				.build();

		ImageLoaderConfiguration.Builder configBuilder = new ImageLoaderConfiguration.Builder(GlobalContext.get())
				.defaultDisplayImageOptions(mDefaultImageOptions).memoryCache(mMemoryCache)
				.tasksProcessingOrder(QueueProcessingType.LIFO).threadPriority(Thread.NORM_PRIORITY - 2)
				.threadPoolSize(4);
		ImageLoaderConfiguration config = configBuilder.build();
		ImageLoader.getInstance().init(config);
	}

	public static void addActivity(Activity activity) {
		synchronized (mActivities) {
			for (Activity a : mActivities) {
				if (a.equals(activity)) {
					a.finish();
					a = null;
					break;
				}
			}
			mActivities.push(activity);
		}
	}

	@SuppressLint("NewApi")
	public static void finishActivity(Class cla) {
		synchronized (mActivities) {
			for (Activity a : mActivities) {
				if (a.getClass().equals(cla)) {
					mActivities.remove(a);
					if (!a.isFinishing()) {
						a.finish();
					}
				}
			}
		}
	}

	public static Activity getActivity(Class cla) {
		synchronized (mActivities) {
			if (mActivities.size() < 1) {
				return null;
			}

			for (Activity activity : mActivities) {
				if (activity.getClass().equals(cla)) {
					return activity;
				}
			}
			return null;
		}
	}

	public void finishAllActivity() {
		synchronized (mActivities) {
			for (Activity activity : mActivities) {
				if (!activity.isFinishing()) {
					activity = mActivities.remove(0);
					activity.finish();
				}
			}
		}
	}

	@Override
	public void caughtException() {
		LogUtil.logd("finishAllActivity");
		finishAllActivity();
	}
}

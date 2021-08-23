package com.txznet.nav;

import java.util.Stack;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;

import com.baidu.mapapi.SDKInitializer;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.txznet.comm.base.BaseApplication;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.nav.ui.InitActivity;
import com.txznet.nav.ui.MainActivity;

public class MyApplication extends BaseApplication {

	private Stack<Activity> mActivityStack;

	@Override
	public void onCreate() {
		super.onCreate();
		SDKInitializer.initialize(this);
		NavManager.getInstance();

		if (!NavManager.getInstance().isInit()) {
			Intent i = new Intent(MyApplication.getInstance(),
					InitActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			MyApplication.getInstance().startActivity(i);
		}

		ServiceManager.getInstance().keepConnection(ServiceManager.TXZ,
				new Runnable() {
					@Override
					public void run() {
						ServiceManager.getInstance().sendInvoke(
								ServiceManager.TXZ,
								"txz.nav.inner.notifyNavStatus",
								("" + NavManager.getInstance().isNavi())
										.getBytes(), null);
						ServiceManager.getInstance().sendInvoke(
								ServiceManager.TXZ,
								"txz.nav.inner.notifyInitStatus",
								("" + NavManager.getInstance().isInit())
										.getBytes(), null);
					}
				});

		getApp().runOnBackGround(new Runnable() {

			@Override
			public void run() {
				initImageLoader();
			}
		}, 0);
	}

	private static DisplayImageOptions mDefaultImageOptions;
	private static MemoryCache mMemoryCache;

	private void initImageLoader() {
		int memoryCacheSize = (int) (Runtime.getRuntime().maxMemory() / 5); // 内存的1/5
		mMemoryCache = new LruMemoryCache(memoryCacheSize);

		mDefaultImageOptions = new DisplayImageOptions.Builder()
				.bitmapConfig(Bitmap.Config.RGB_565).cacheInMemory(true)
				.cacheOnDisk(false).resetViewBeforeLoading(false)
				.showImageForEmptyUri(R.drawable.default_headimage).build();

		ImageLoaderConfiguration.Builder configBuilder = new ImageLoaderConfiguration.Builder(
				this).defaultDisplayImageOptions(mDefaultImageOptions)
				.memoryCache(mMemoryCache)
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.threadPriority(Thread.NORM_PRIORITY - 2).threadPoolSize(4);
		ImageLoaderConfiguration config = configBuilder.build();
		ImageLoader.getInstance().init(config);
	}

	public static MyApplication getInstance() {
		return (MyApplication) getApp();
	}

	/**
	 * 添加Activity到堆栈
	 */
	public void addActivity(Activity activity) {
		if (mActivityStack == null) {
			mActivityStack = new Stack<Activity>();
		}
		Activity a = getActivity(activity.getClass());
		if (a != null) {
			removeActivity(a);
		}
		mActivityStack.add(activity);
	}

	/**
	 * pop
	 * 
	 * @param a
	 */
	public void removeActivity(Activity activity) {
		if (mActivityStack == null)
			return;
		mActivityStack.remove(activity);
	}

	/**
	 * 获取当前Activity（堆栈中最后一个压入的）
	 */
	public Activity getCurrentActivity() {
		if (mActivityStack == null || mActivityStack.size() < 1) {
			return null;
		}
		Activity activity = mActivityStack.lastElement();
		return activity;
	}

	public Activity getActivity(Class<?> cls) {
		if (mActivityStack == null)
			return null;

		for (Activity activity : mActivityStack) {
			if (activity.getClass().equals(cls)) {
				return activity;
			}
		}
		return null;
	}

	/**
	 * 结束当前Activity（堆栈中最后一个压入的）
	 */
	public void finishActivity() {
		if (mActivityStack == null || mActivityStack.size() < 1) {
			return;
		}
		Activity activity = mActivityStack.lastElement();
		finishActivity(activity);
	}

	/**
	 * 结束指定的Activity
	 */
	public void finishActivity(Activity activity) {
		if (mActivityStack == null || mActivityStack.size() < 1) {
			return;
		}
		if (activity != null) {
			mActivityStack.remove(activity);
			activity.finish();
			activity = null;
		}
	}

	/**
	 * 结束指定类名的Activity
	 */
	public void finishActivity(Class<?> cls) {
		if (mActivityStack == null || mActivityStack.size() < 1) {
			return;
		}
		for (Activity activity : mActivityStack) {
			if (activity.getClass().equals(cls)) {
				finishActivity(activity);
			}
		}
	}

	/**
	 * 结束所有Activity
	 */
	public void finishAllActivity() {
		if (mActivityStack == null || mActivityStack.size() < 1) {
			return;
		}
		for (int i = 0, size = mActivityStack.size(); i < size; i++) {
			if (null != mActivityStack.get(i)) {
				finishActivity(mActivityStack.get(i));
			}
		}
		mActivityStack.clear();
	}

	/**
	 * 刷新当前界面
	 */
	public void refreshCurrentActivity() {
		Activity a = mActivityStack.get(mActivityStack.size() - 1);
		if (a.getClass() == MainActivity.class) {
			return;
		}
		Intent intent = new Intent();
		intent.setClass(this, a.getClass());
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}

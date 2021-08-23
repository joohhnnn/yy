package com.txznet.nav.tool;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.nav.tool.UserBitmapManager.BitmapDownloadTool;
import com.txznet.nav.tool.UserBitmapManager.BitmapOption;
import com.txznet.nav.tool.UserBitmapManager.DownloadListener;
import com.txznet.txz.util.runnables.Runnable2;

public class ImageLoaderDownloadTool implements BitmapDownloadTool {

	Handler mHandler;
	HandlerThread mHandlerThread;
	DownloadListener mDownloadListener = null;

	@Override
	public void onLoadImage(BitmapOption bo, DownloadListener dl) {
		mDownloadListener = dl;
		try {
			ImageLoader.getInstance().loadImage(bo.getImageUrl(),
					new SimpleImageLoadingListener() {

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							super.onLoadingComplete(imageUri, view, loadedImage);
							if (loadedImage != null
									&& loadedImage instanceof Bitmap) {
								mDownloadListener.onSuccess(imageUri,
										loadedImage);
							}
						};
					});
		} catch (Exception e) {
			mDownloadListener.onError("");
		}
	}

	List<Runnable> mDownloadRunnableList = new ArrayList<Runnable>();

	public void cancelAllDownloadTask() {
		LogUtil.logd("cancelAllDownloadTask");
		synchronized (mDownloadRunnableList) {
			mDownloadRunnableList.clear();
		}
	}

	static boolean isDownloadBusy;

	public void downloadImage(String url, DownloadListener dll) {
		if (TextUtils.isEmpty(url)) {
			if (dll != null) {
				dll.onError("url is null");
			}
			return;
		}

		init();
		BitmapOption bo = new BitmapOption();
		bo.setImageUrl(url);

		Runnable runnable = new Runnable2<BitmapOption, DownloadListener>(bo,
				dll) {

			@Override
			public void run() {
				isDownloadBusy = true;
				onLoadImage(mP1, new DownloadListener() {

					@Override
					public void onSuccess(String imageUrl, Bitmap bitmap) {
						mP2.onSuccess(imageUrl, bitmap);
						procQueue();
					}

					@Override
					public void onError(String reson) {
						mP2.onError(reson);
						procQueue();
					}
				});
			}
		};

		if (isDownloadBusy) {
			synchronized (mDownloadRunnableList) {
				mDownloadRunnableList.add(runnable);
			}
		} else {
			mHandler.post(runnable);
		}
	}

	private void procQueue() {
		isDownloadBusy = false;
		synchronized (mDownloadRunnableList) {
			if (!mDownloadRunnableList.isEmpty()) {
				Runnable r = mDownloadRunnableList.remove(0);
				mHandler.post(r);
			}
		}
	}

	private void init() {
		if (mHandlerThread == null) {
			synchronized (ImageLoaderDownloadTool.class) {
				if (mHandlerThread == null) {
					mHandlerThread = new HandlerThread("downloadThread");
					mHandlerThread.setPriority(Thread.MIN_PRIORITY);
					mHandlerThread.start();
					mHandler = new Handler(mHandlerThread.getLooper());
				}
			}
		}
	}
}

package com.txznet.nav.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.R;
import com.txznet.nav.tool.BitmapProvider.OnBitmapInitedListener;
import com.txznet.nav.ui.widget.CircleImageView;
import com.txznet.nav.ui.widget.MarkerView;
import com.txznet.txz.util.runnables.Runnable1;

public class UserViewManager {

	private static Map<String, MarkerView> mId2ViewMap = new ConcurrentHashMap<String, MarkerView>();
	private static Map<String, MarkerView> mId2DefaultViewMap = new ConcurrentHashMap<String, MarkerView>();

	private static UserViewManager instance = null;

	private UserViewManager() {
		init();
	}

	Handler downloadHandler;
	HandlerThread downloadThread;

	public static UserViewManager getInstance() {
		if (instance == null) {
			synchronized (UserViewManager.class) {
				if (instance == null) {
					instance = new UserViewManager();
				}
			}
		}

		return instance;
	}

	private void init() {
		downloadThread = new HandlerThread("imageloader-thread");
		downloadThread.start();
		downloadHandler = new Handler(downloadThread.getLooper());
	}

	public void destory() {
		mId2ViewMap.clear();
		mId2DefaultViewMap.clear();
		instance = null;
	}

	public void removeUid(String uid) {
		if (mId2ViewMap.containsKey(uid)) {
			mId2ViewMap.remove(uid);
		}

		if (mId2DefaultViewMap.containsKey(uid)) {
			mId2DefaultViewMap.remove(uid);
		}
	}

	public Bitmap getDrawableByUId(final String uid, final String imagePath,
			final float degree, final OnBitmapInitedListener oil) {
		if (TextUtils.isEmpty(uid)) {
			return null;
		}

		MarkerView mv = mId2ViewMap.get(uid);
		if (mv == null) {
			mv = mId2DefaultViewMap.get(uid);
			if (mv == null) {
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						MarkerView mv = createMv(null);
						mId2DefaultViewMap.put(uid, mv);
						OverlayManager.getInstance().updateMarkerDrawable(uid,
								degree);
					}
				}, 0);
			}

			downloadHandler.post(new Runnable() {

				@Override
				public void run() {
					if (TextUtils.isEmpty(imagePath)) {
						return;
					}

					ImageLoader.getInstance().loadImage(imagePath,
							new SimpleImageLoadingListener() {

								@Override
								public void onLoadingComplete(String imageUri,
										View view, Bitmap loadedImage) {
									super.onLoadingComplete(imageUri, view,
											loadedImage);
									AppLogic.runOnUiGround(
											new Runnable1<Bitmap>(loadedImage) {

												@Override
												public void run() {
													if (mP1 != null) {
														MarkerView mv = createMv(mP1);
														LogUtil.logd("下载头像成功 uid:"
																+ uid);
														mId2ViewMap
																.put(uid, mv);
														OverlayManager
																.getInstance()
																.updateMarkerDrawable(
																		uid,
																		degree);
													}
												}
											}, 0);
								}
							});
				}
			});
		}

		if (mv == null) {
			return null;
		}

		AppLogic.runOnUiGround(new Runnable1<MarkerView>(mv) {

			@Override
			public void run() {
				mP1.setCarDirection(degree);
				Bitmap drawable = getCacheBitmap(mP1);
				int retry = 0;
				while (drawable == null && retry < 10) {
					try {
						drawable = getCacheBitmap(mP1);
						retry++;
					} catch (Exception e) {
						LogUtil.loge(e.toString());
					}
				}

				oil.onInited(drawable);
			}
		}, 0);

		return null;
	}

	private static MarkerView createMv(Bitmap bitmap) {
		MarkerView mv = (MarkerView) LayoutInflater
				.from(AppLogic.getApp()).inflate(R.layout.marker_view,
						null);
		CircleImageView civ = (CircleImageView) LayoutInflater.from(
				AppLogic.getApp()).inflate(R.layout.civ_layout, null);
		if (bitmap != null) {
			civ.setImageBitmap(bitmap);
		} else {
			civ.setImageResource(R.drawable.default_headimage);
		}
		Drawable drawable = getCacheDrawable(civ);

		int retry = 0;
		while (drawable == null && retry < 10) {
			try {
				Thread.sleep(30);
				drawable = getCacheDrawable(civ);
				retry++;
			} catch (InterruptedException e) {
				LogUtil.loge(e.toString());
			}
		}

		if (drawable == null) {
			return mv;
		}
		mv.setTag(civ);
		mv.setHeadImageDrawable(drawable);
		return mv;
	}

	private static Drawable getCacheDrawable(View view) {
		view.setDrawingCacheEnabled(true);
		view.measure(MeasureSpec.makeMeasureSpec(80, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(80, MeasureSpec.EXACTLY));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bm = view.getDrawingCache();
		BitmapDrawable bd = new BitmapDrawable(AppLogic.getApp()
				.getResources(), bm);
		bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
		return bd;
	}

	private static Bitmap getCacheBitmap(View view) {
		view.setDrawingCacheEnabled(true);
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bm = view.getDrawingCache();
		return bm;
	}
}

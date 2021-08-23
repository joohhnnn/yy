package com.txznet.nav.tool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.tool.MarkerViewManager.MarkViewItem;
import com.txznet.nav.tool.UserBitmapManager.DownloadListener;
import com.txznet.txz.util.runnables.Runnable1;

public class BitmapProvider {

	// 通过用户名下载的图片
	Map<String, Bitmap> mOriginalBitmapMap = new ConcurrentHashMap<String, Bitmap>();

	Map<String, MarkViewItem> mUidToMarkViewMap = new ConcurrentHashMap<String, MarkerViewManager.MarkViewItem>();

	ImageLoaderDownloadTool mDownloadTool = null;

	private static BitmapProvider mBitmapProvider = null;

	private BitmapProvider() {
		mDownloadTool = new ImageLoaderDownloadTool();
	}

	public Bitmap getOverlayBitmap(final String uid, final String imagePath,
			final float degree, final OnBitmapInitedListener oil) {
		final MarkViewItem mvi = mUidToMarkViewMap.get(uid);
		if (mvi == null) {
			Bitmap b = mOriginalBitmapMap.get(uid);
			if (b == null) {
				mDownloadTool.downloadImage(imagePath, new DownloadListener() {

					@Override
					public void onSuccess(String imageUrl, Bitmap bitmap) {
						LogUtil.logd("头像下载成功：uid-" + uid);
						AppLogic.runOnUiGround(new Runnable1<Bitmap>(bitmap) {
							
							@Override
							public void run() {
								if (mOriginalBitmapMap.containsKey(uid)) {
									mOriginalBitmapMap.remove(uid);
								}

								mOriginalBitmapMap.put(uid, mP1);
								MarkViewItem mv = MarkerViewManager.getInstance()
										.createMarkerView(mP1, uid, degree);
								if (mv != null) {
									if (mUidToMarkViewMap.containsKey(uid)) {
										mUidToMarkViewMap.remove(uid);
									}

//									mUidToMarkViewMap.put(uid, mv);
									if (oil != null) {
										oil.onInited(null);
									}
								}
							}
						}, 0);
					}

					@Override
					public void onError(String reson) {
					}
				});
			} else {
				AppLogic.runOnUiGround(new Runnable1<Bitmap>(b) {
					
					@Override
					public void run() {
						MarkViewItem mv = MarkerViewManager.getInstance()
								.createMarkerView(mP1, uid, degree);
						if (mv != null) {
//							mUidToMarkViewMap.put(uid, mv);
							if (oil != null) {
								oil.onInited(null);
							}
						}
					}
				}, 0);
			}

			return null;
		} else {
			if (mvi.isRebuild()) {
				Bitmap b = mOriginalBitmapMap.get(uid);
				if (b == null) {
					mDownloadTool.downloadImage(imagePath,
							new DownloadListener() {

								@Override
								public void onSuccess(String imageUrl,
										Bitmap bitmap) {
									if (mOriginalBitmapMap.containsKey(uid)) {
										mOriginalBitmapMap.remove(uid);
									}

									mOriginalBitmapMap.put(uid, bitmap);
									AppLogic.runOnUiGround(new Runnable1<Bitmap>(bitmap) {
										
										@Override
										public void run() {
											Bitmap bm = MarkerViewManager.getInstance()
													.convertBitmap(mP1, mvi);
											if (oil != null) {
												oil.onInited(bm);
											}
										}
									}, 0);
								}

								@Override
								public void onError(String reson) {
								}
							});
				} else {
					AppLogic.runOnUiGround(new Runnable1<Bitmap>(b) {
						
						@Override
						public void run() {
							Bitmap bm = MarkerViewManager.getInstance().convertBitmap(
									mP1, mvi);
							if (oil != null) {
								oil.onInited(bm);
							}
						}
					}, 0);
				}
			}

			return MarkerViewManager.getInstance().genUserBitmap(mvi, degree);
		}
	}

	public void removeUid(String uid) {
		mOriginalBitmapMap.remove(uid);
		mUidToMarkViewMap.remove(uid);
	}

	public void destory() {
		mUidToMarkViewMap.clear();
		mOriginalBitmapMap.clear();
	}

	public static BitmapProvider getInstance() {
		if(mBitmapProvider == null){
			synchronized (BitmapProvider.class) {
				if(mBitmapProvider == null){
					mBitmapProvider = new BitmapProvider();
				}
			}
		}
		return mBitmapProvider;
	}

	public void setDownloadTool(ImageLoaderDownloadTool downloadTool) {
		this.mDownloadTool = downloadTool;
	}

	public static interface OnBitmapInitedListener {
		public void onInited(Bitmap bitmap);
	}
}
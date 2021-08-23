package com.txznet.nav.helper;

import android.graphics.Bitmap;

import com.txznet.nav.manager.UserViewManager;
import com.txznet.nav.tool.BitmapProvider;
import com.txznet.nav.tool.BitmapProvider.OnBitmapInitedListener;

public class OverlayImageSelector {

	private static final int STRATEGY_USERVIEWMANAGER = 1;
	private static final int STRATEGY_BITMAPPROVIDER = 2;

	int mUseStrategy = STRATEGY_USERVIEWMANAGER;
	
	private static OverlayImageSelector instance = null;

	private OverlayImageSelector() {

	}

	public static OverlayImageSelector getInstance() {
		if (instance == null) {
			synchronized (OverlayImageSelector.class) {
				if (instance == null) {
					instance = new OverlayImageSelector();
				}
			}
		}

		return instance;
	}

	public void destory() {
		if (mUseStrategy == STRATEGY_USERVIEWMANAGER) {
			UserViewManager.getInstance().destory();
		} else if (mUseStrategy == STRATEGY_BITMAPPROVIDER) {
			BitmapProvider.getInstance().destory();
		}
		instance = null;
	}

	public void removeUid(String uid) {
		if (mUseStrategy == STRATEGY_USERVIEWMANAGER) {
			UserViewManager.getInstance().removeUid(uid);
		} else if (mUseStrategy == STRATEGY_BITMAPPROVIDER) {
			BitmapProvider.getInstance().removeUid(uid);
		}
	}

	public Bitmap getUserBitmap(final String uid, final String imagePath,
			final float degree) {
		return null;
	}

	public Bitmap getUserBitmap(String uid, String imagePath, float degree,
			OnBitmapInitedListener oil) {
		if (mUseStrategy == STRATEGY_USERVIEWMANAGER) {
			return UserViewManager.getInstance().getDrawableByUId(uid,
					imagePath, degree, oil);
		} else if (mUseStrategy == STRATEGY_BITMAPPROVIDER) {
			return BitmapProvider.getInstance().getOverlayBitmap(uid,
					imagePath, degree, oil);
		}
		return null;
	}
}

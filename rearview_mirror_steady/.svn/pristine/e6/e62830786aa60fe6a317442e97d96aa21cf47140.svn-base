package com.txznet.nav.helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

import com.google.protobuf.nano.MessageNano;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.loader.AppLogic;

public class NavInfoCache {
	private static final String CACHE_NAME = ".txz_nav_info_cache";
	private static NavInfoCache sInstance;

	private static final String KEY_OF_REMAIN_DISTANCE = "remain_distance";
	private static final SharedPreferences mSp = AppLogic.getApp()
			.getSharedPreferences("", Context.MODE_PRIVATE);
	private static final Editor mEditor = mSp.edit();

	private NavInfoCache(Context context) {
	}

	public static NavInfoCache getInstance() {
		if (sInstance == null) {
			synchronized (NavInfoCache.class) {
				if (sInstance == null) {
					sInstance = new NavInfoCache(AppLogic.getApp());
				}
			}
		}
		return sInstance;
	}

	public boolean hasCache() {
		File file = new File(Environment.getExternalStorageDirectory(),
				CACHE_NAME);
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	public void saveCache(final NavigateInfo navigateInfo) {
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				setRemainDistance(0);
				File cacheFile = new File(
						Environment.getExternalStorageDirectory(), CACHE_NAME);
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(cacheFile);
					fos.write(MessageNano.toByteArray(navigateInfo));
				} catch (Exception e) {

				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}, 0);
	}

	public NavigateInfo getCache() {
		File cacheFile = new File(Environment.getExternalStorageDirectory(),
				CACHE_NAME);
		if (cacheFile.exists()) {
			FileInputStream fis = null;
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				fis = new FileInputStream(cacheFile);
				byte[] buff = new byte[2048];
				int hasRead = -1;
				while ((hasRead = fis.read(buff)) != -1) {
					bos.write(buff, 0, hasRead);
				}
				return NavigateInfo.parseFrom(bos.toByteArray());
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public void setRemainDistance(int distance) {
		mEditor.putInt(KEY_OF_REMAIN_DISTANCE, distance);
		mEditor.commit();
	}

	public int getDistance() {
		return mSp.getInt(KEY_OF_REMAIN_DISTANCE, 0);
	}

	public void reset() {
		File cacheFile = new File(Environment.getExternalStorageDirectory(),
				CACHE_NAME);
		if (cacheFile.exists()) {
			cacheFile.delete();
		}
	}
}

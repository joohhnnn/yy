package com.txznet.nav.tool;

import android.graphics.Bitmap;

public class UserBitmapManager {

	static UserBitmapManager instance = new UserBitmapManager();

	private UserBitmapManager() {

	}

	public static interface BitmapDownloadTool {
		public void onLoadImage(BitmapOption bo, DownloadListener dl);
	}

	public static class BitmapOption {
		private String imageUrl;
		private float degree;

		public void setImageUrl(String url) {
			imageUrl = url;
		}

		public String getImageUrl() {
			return imageUrl;
		}

		public void setDegree(float degree) {
			this.degree = degree;
		}

		public float getDegree() {
			return this.degree;
		}
	}

	public static interface DownloadListener {

		public void onSuccess(String imageUrl, Bitmap bitmap);

		public void onError(String reson);
	}
}

package com.txznet.launcher.bean;

import android.graphics.drawable.Drawable;

public class ResidentIconInfo implements LauncherIconInfo {

	private String mTitle;
	private Drawable mIcon;
	private Object mData;

	public interface OnClickLisener {
		public void onClick(Object mData);
	}

	private OnClickLisener mClickLisener;

	public ResidentIconInfo() {
	}

	public ResidentIconInfo setTitle(String title) {
		mTitle = title;
		return this;
	}

	public ResidentIconInfo setIcon(Drawable icon) {
		mIcon = icon;
		return this;
	}

	public ResidentIconInfo setData(Object data) {
		mData = data;
		return this;
	}
	
	public ResidentIconInfo setOnClickLisener(OnClickLisener lisener) {
		mClickLisener = lisener;
		return this;
	}

	@Override
	public String getTitle() {
		return mTitle;
	}

	@Override
	public Drawable getIcon() {
		return mIcon;
	}

	@Override
	public void onClick() {
		if (mClickLisener != null) {
			mClickLisener.onClick(mData);
		}
	}
}

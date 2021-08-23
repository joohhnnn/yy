package com.txznet.comm.ui;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;

import android.text.TextUtils;

/**
 * UI Version Manager
 * 
 * @author Terry
 *
 */
public class UIVersionManager {

	private static UIVersionManager sInstance = new UIVersionManager();

	private String mSkinVersion = null;
	private int mVersionCode = 0;
	
	private UIVersionManager() {
	}

	public static UIVersionManager getInstance(){
		return sInstance;
	}
	
	public void init() {
		LogUtil.logd("UIVersionManager init");
		try {
			mSkinVersion = UIResLoader.getInstance().getString("version");
			LogUtil.logd("UIVersionManager "+mSkinVersion);
		} catch (Exception e) {
		}
		if(TextUtils.isEmpty(mSkinVersion)){
			mSkinVersion = "0.9";
		}
		mVersionCode = transferVersionCode(mSkinVersion); 
	}

	/**
	 * 1.0 ->100
	 * 1.1 ->101
	 * 1.10->110 
	 */
	public int transferVersionCode(String version) {
		if (TextUtils.isEmpty(version)) {
			return 9;
		}
		String[] subStrings = version.split("\\.");
		int weight = 1;
		int versionCode = 0;
		for (int i = subStrings.length-1; i >= 0; i--) {
			try {
				int num = Integer.parseInt(subStrings[i]);
				versionCode += num*weight;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			weight = weight * 100;
		}
		return versionCode;
	}
	
	public String getVersionName() {
		return mSkinVersion;
	}

	public int getVersionCode() {
		return mVersionCode;
	}
	
}

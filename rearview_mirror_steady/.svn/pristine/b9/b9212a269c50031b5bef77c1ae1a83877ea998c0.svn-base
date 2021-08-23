package com.txznet.comm.remote;

import com.txznet.comm.util.ScreenUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

public class GlobalContext {
	private static Context mContext;
	//private static Context mModifiedContext;
	private static boolean mIsTXZ = false;
	
	public static boolean isTXZ(){
		return mIsTXZ;
	}

	public static void set(Context context) {
		mContext = context.getApplicationContext();
		mIsTXZ = ServiceManager.TXZ.equals(mContext.getApplicationInfo().packageName);
	}

	public static Context get() {
		if(mContext == null){
			throw new IllegalStateException("you have not yet initialized the sdk context !");
		}
		return mContext;
	}

	/**
	 * 更新GlobalContext的Configuration配置
	 *
	 * @return 配置是否更新
	 */
	public static boolean updateResourceConfig() {
		if (null == mContext) {
			Log.d("GlobalContext", "updateResourceConfig: mContext not initialized yet");
			return false;
		}

		Configuration conf = mContext.getResources().getConfiguration();

		int screenWidth = ScreenUtils.getScreenWidthDp();
		int screenHeight = ScreenUtils.getScreenHeightDp();

		if (0 == screenWidth || 0 == screenHeight) {
			Log.d("GlobalContext",
                    String.format("updateResourceConfig: configuration not valid: %s x %s",
                            screenWidth, screenHeight));
			return false;
		}

		if (screenWidth != conf.screenWidthDp
				|| screenHeight != conf.screenHeightDp) {
			Log.d("GlobalContext",
                    String.format("updateResourceConfig: do update to: %s x %s",
                            screenWidth, screenHeight));
			// update configuration
			Configuration configuration = mContext.getResources().getConfiguration();
			configuration.screenWidthDp = screenWidth;
			configuration.screenHeightDp = screenHeight;
			mContext.getResources().updateConfiguration(configuration,
					mContext.getResources().getDisplayMetrics());

			return true;
		}

		Log.d("GlobalContext", "updateResourceConfig: configuration not changed");
		return false;
	}

	/**
	 * 修改了Configuration后的Context
	 *
	 * @deprecated 逻辑修改: 适配分辨率发生变化时, 会直接更新GlobalContext的相关配置, 不再重新
	 * 创建新的Context, 解决显示位置广播响应后适配分辨率未更新的问题, 同时规避新Context中attr相关字段丢失问题
	 *
	 * @return
	 */
	@SuppressLint("NewApi")
	@Deprecated
	public static Context getModified() {
		return get();
		/*if (mModifiedContext != null) {
			return mModifiedContext;
		}
		if (mContext == null) {
			throw new IllegalStateException("you have not yet initialized the sdk context !");
		}
		if (ScreenUtils.getScreenWidthDp() == 0 || ScreenUtils.getScreenHeightDp() == 0) {
			return mContext;
		}
		Configuration configuration = mContext.getResources().getConfiguration();
		configuration.screenWidthDp = ScreenUtils.getScreenWidthDp();
		configuration.screenHeightDp = ScreenUtils.getScreenHeightDp();
		mModifiedContext = mContext.createConfigurationContext(configuration);
		return mModifiedContext;*/
	}
	
}

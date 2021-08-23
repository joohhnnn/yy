package com.txznet.loader;

import com.txznet.team.MainActivity;

public class AppLogic extends AppLogicBase {

	private static MainActivity mMainActivity;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public static void setMainActivity(MainActivity m) {
		mMainActivity = m;
	}

	public static void finishMainActivity() {
		if (mMainActivity != null) {
			mMainActivity.finish();
			mMainActivity = null;
		}
	}

	public static MainActivity getMainActivity() {
		return mMainActivity;
	}
}

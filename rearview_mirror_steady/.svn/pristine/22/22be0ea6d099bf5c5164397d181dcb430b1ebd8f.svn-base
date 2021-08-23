package com.txznet.music.ui;

import android.content.Intent;
import android.os.Bundle;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.bean.Configuration;
import com.txznet.music.helper.RequestHelpe;

public class SplashActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int skinVersion = Configuration.getInstance().getInteger(
				Configuration.TXZ_SKIN);
		LogUtil.logd(TAG + "current skin version " + skinVersion);
		Intent intent = null;
		switch (skinVersion) {
		case 1:
			intent = new Intent(this, MainActivity.class);
			break;
		case 2:
			intent = new Intent(this, SingleActivity.class);
			break;
		default:
			intent = new Intent(this, MainActivity.class);
			break;
		}
		startActivity(intent);
		finish();
	}
	@Override
	protected void onStart() {

		super.onStart();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}

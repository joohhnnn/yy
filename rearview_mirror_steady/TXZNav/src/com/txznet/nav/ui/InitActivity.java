package com.txznet.nav.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;

import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.util.verify.BNKeyVerifyListener;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.nav.MyApplication;
import com.txznet.nav.NavService;
import com.txznet.nav.R;

public class InitActivity extends BaseActivity {

	static private boolean isInit;
	static private boolean isKeyVerify;

	// private ImageView mAnim;
	private View mClose;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BaiduNaviManager.getInstance().initEngine(this, getSdcardDir(), "nav",
				mNaviEngineInitListener,
				MyApplication.getApp().getString(R.string.appid_baidumap),
				mKeyVerifyListener);
		setContentView(R.layout.activity_init);
		// mAnim = (ImageView) findViewById(R.id.imgInit_Anim);
		// if (mAnim.getDrawable() != null
		// && mAnim.getDrawable() instanceof AnimationDrawable) {
		// AnimationDrawable animDrawable = (AnimationDrawable) mAnim
		// .getDrawable();
		// animDrawable.start();
		// }
		mClose = findViewById(R.id.btnInit_Close);
		mClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				InitActivity.this.finish();
			}
		});

		MyApplication.getInstance().runOnUiGround(new Runnable() {

			@Override
			public void run() {
				InitActivity.this.finish();
			}
		}, 3000);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	static private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {
		public void engineInitSuccess() {
			LogUtil.logd("engineInitSuccess success: isKeyVerify="
					+ isKeyVerify);
			isInit = true;
			if (isKeyVerify) {
				initComplete();
			}
		}

		public void engineInitStart() {
			LogUtil.logd("engineInitStart");
		}

		public void engineInitFail() {
			String str = "engineInitFail";
			LogUtil.loge(str);
			MyApplication.showToast(str);
			isInit = false;
		}
	};

	static private BNKeyVerifyListener mKeyVerifyListener = new BNKeyVerifyListener() {

		@Override
		public void onVerifySucc() {
			LogUtil.logd("onVerifySucc: isInit=" + isInit);
			isKeyVerify = true;
			if (isInit) {
				initComplete();
			}
		}

		@Override
		public void onVerifyFailed(int arg0, String arg1) {
			String str = "onVerifyFailed: " + arg0 + "-" + arg1;
			LogUtil.loge(str);
			MyApplication.showToast(str);
			isKeyVerify = false;
		}
	};

	static private void initComplete() {
		NavService.getInstance().setIsInit(true);
		LogUtil.logd("initComplete");
	}
}

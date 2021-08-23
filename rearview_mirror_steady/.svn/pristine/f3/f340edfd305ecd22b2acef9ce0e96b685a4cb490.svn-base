package com.txznet.reserve.activity;

import java.io.File;

import com.txznet.comm.base.BaseActivity;
import com.txznet.txz.jni.JNIHelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * 辅助安装Apk的Activity
 */
public class ReserveStandardActivity0 extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		procIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		procIntent(intent);
	}

	private void procIntent(Intent intent) {
		String url = intent.getStringExtra("url");
		JNIHelper.logd("ReserveStandardActivity0 procIntent url:" + url);
		if (TextUtils.isEmpty(url)) {
			finish();
			return;
		}

		installApk(url);
		// 关闭界面
		finish();
	}

	private void installApk(String url) {
		if (url.startsWith("http://")) {
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		} else {
			File fNewApk = new File(url);
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);
			// intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
			// intent.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
			intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
			intent.setDataAndType(Uri.fromFile(fNewApk), "application/vnd.android.package-archive");
			startActivityForResult(intent, 2);
		}
	}
}
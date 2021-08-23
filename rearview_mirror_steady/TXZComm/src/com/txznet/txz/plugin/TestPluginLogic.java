package com.txznet.txz.plugin;

import android.app.ProgressDialog;
import android.view.WindowManager;

import com.txznet.loader.AppLogicBase;

public class TestPluginLogic {
	public static void test() {
		AppLogicBase.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				ProgressDialog mHintDialog = new ProgressDialog(AppLogicBase.getApp());
				mHintDialog.getWindow().setType(
						WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				mHintDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mHintDialog.setMessage("测试一下插件");
				mHintDialog.setCancelable(true);
				mHintDialog.setIndeterminate(false);
				mHintDialog.show();
			}
		}, 0);
	}
}

package com.txznet.debugtool;

import java.io.File;

import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;

import com.txznet.comm.base.BaseApplication;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.debugtool.util.FileUtil;
import com.txznet.loader.AppLogic;
import com.txznet.widget.DebugButton;

public class UpgradeTestActivity extends BaseDebugActivity {
	public final String APK_SRC_FILE = Environment
			.getExternalStorageDirectory().getPath() + "/TXZCore.apk";
	public final String APK_DST_FILE = Environment
			.getExternalStorageDirectory().getPath() + "/TXZCoreUpgrade.apk";

	@Override
	protected void onInitButtons() {
		addDemoButtons(new DebugButton(this, "静默升级", new OnClickListener() {
			@Override
			public void onClick(View v) {
				AppLogic.showToast("正在复制文件");
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						FileUtil.copyFile(new File(APK_SRC_FILE), new File(
								APK_DST_FILE));
						AppLogic.showToast("复制文件完成");
						JSONBuilder json = new JSONBuilder();
						json.put(BaseApplication.SP_KEY_APK, APK_DST_FILE);
						ServiceManager.getInstance().sendInvoke(
								ServiceManager.TXZ, "comm.update.upgrade",
								json.toBytes(), null);
					}
				}, 0);
			}
		}), new DebugButton(this, "提示升级", new OnClickListener() {
			@Override
			public void onClick(View v) {
				AppLogic.showToast("正在复制文件");
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						FileUtil.copyFile(new File(APK_SRC_FILE), new File(
								APK_DST_FILE));
						AppLogic.showToast("复制文件完成");
						JSONBuilder json = new JSONBuilder();
						json.put(BaseApplication.SP_KEY_APK, APK_DST_FILE);
						json.put("desc", "发现一个非常非常酷的新版本");
						ServiceManager.getInstance().sendInvoke(
								ServiceManager.TXZ, "comm.update.upgrade",
								json.toBytes(), null);
					}
				}, 0);
			}
		}), new DebugButton(this, "强制升级", new OnClickListener() {
			@Override
			public void onClick(View v) {
				AppLogic.showToast("正在复制文件");
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						FileUtil.copyFile(new File(APK_SRC_FILE), new File(
								APK_DST_FILE));
						AppLogic.showToast("复制文件完成");
						JSONBuilder json = new JSONBuilder();
						json.put(BaseApplication.SP_KEY_APK, APK_DST_FILE);
						json.put("desc", "发现一个非常非常酷的新版本");
						json.put("force", true);
						ServiceManager.getInstance().sendInvoke(
								ServiceManager.TXZ, "comm.update.upgrade",
								json.toBytes(), null);
					}
				}, 0);
			}
		}));

		addDemoButtons(new DebugButton(this, "回滚版本", new OnClickListener() {
			@Override
			public void onClick(View v) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"comm.update.rollback", null, null);
			}
		}));
	}

}

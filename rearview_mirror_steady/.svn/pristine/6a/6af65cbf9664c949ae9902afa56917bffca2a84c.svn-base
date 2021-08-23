package com.txznet.comm.update;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.widget.Toast;

import com.txznet.comm.base.BaseApplication;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.dialog2.WinConfirm;
import com.txznet.comm.ui.dialog2.WinMessageBox;
import com.txznet.comm.ui.dialog2.WinNotice;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogicBase;

public class UpdateCenter {
	public static void rollback(Context context) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences(
				context.getApplicationInfo().packageName
						+ BaseApplication.SP_SUFFIX, Context.MODE_PRIVATE);
		String oldApk = mSharedPreferences.getString(
				BaseApplication.SP_KEY_APK, "");
		Editor editor = mSharedPreferences.edit();
		File f = new File(oldApk);
		if (f.exists()) {
			f.delete();
		}
		editor.remove(BaseApplication.SP_KEY_APK);
		editor.remove(BaseApplication.SP_KEY_SIZE);
		editor.remove(BaseApplication.SP_KEY_TIME);
		editor.commit();
	}
	
	public static void showRestartDeviceNotification(final long time) {
		final String hint = "语音助手已升级成功\n建议您手动重启设备";
		// WinNotice.showNotice(hint, false, true, null); //不使用弹窗，避免频繁crash引发影响用户操作
		AppLogicBase.runOnUiGround(new Runnable() {
			long count = 0;
			@Override
			public void run() {
				LogUtil.logd("hint upgrde reboot");
				Toast.makeText(GlobalContext.get(), hint, Toast.LENGTH_LONG)
						.show();
				count += 3000;
				if (time <= 0 || count < time) {
					AppLogicBase.runOnUiGround(this, 3000);
				}
			}
		}, 0);
	}

	public static void processUpdateApk(String newApk) {
		SharedPreferences mSharedPreferences = GlobalContext.get()
				.getSharedPreferences(
						GlobalContext.get().getApplicationInfo().packageName
								+ BaseApplication.SP_SUFFIX,
						Context.MODE_PRIVATE);
		File fApk = new File(newApk);
		long size = fApk.length();
		long time = fApk.lastModified();
		String oldApk = mSharedPreferences.getString(
				BaseApplication.SP_KEY_APK, "");
		Editor editor = mSharedPreferences.edit();
		if (!oldApk.equals(newApk)) {
			// 退出时清理老版本升级包
			File f = new File(oldApk);
			if (f.exists()) {
				f.delete();
			}
		}
		editor.remove(BaseApplication.SP_KEY_LAUNCH_TIMES); // 升级或回滚时需要把之前启动时间清除
		editor.putLong(BaseApplication.SP_KEY_SIZE, size);
		editor.putLong(BaseApplication.SP_KEY_TIME, time);
		editor.putString(BaseApplication.SP_KEY_APK, newApk);
		editor.commit();
		fApk.setLastModified(time);
	}

	public static byte[] process(String packageName, String command, byte[] data) {
		// 回滚
		if ("rollback".equals(command)) {
			rollback(GlobalContext.get());
			AppLogicBase.restartProcess();
			return null;
		}
		//重启
		if ("restart".equals(command)) {
			AppLogicBase.restartProcess();
			return null;
		}
		//升级快速测试接口
		if ("quickUpgrade".equals(command)) {
			LogUtil.logw("upgrade quick restart");
			processUpdateApk(new String(data));
			AppLogicBase.restartProcess();
			return null;
		}
		// 升级
		if ("upgrade".equals(command)) {
			JSONBuilder json = new JSONBuilder(data);
			final String newApk = json.getVal(BaseApplication.SP_KEY_APK,
					String.class);
			File fApk = new File(newApk);
			if (!fApk.exists()) {
				LogUtil.logw("upgrade file[" + newApk + "] not exist");
				return null;
			}
			final String desc = json.getVal("desc", String.class);
			if (TextUtils.isEmpty(desc)) {
				LogUtil.logw("upgrade silence restart");
				processUpdateApk(newApk);
				AppLogicBase.restartProcess();
				return null;
			}
			final boolean bForce = json.getVal("force", Boolean.class, false);
			AppLogicBase.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					WinMessageBox dlg;
					if (bForce) {
						processUpdateApk(newApk);
						WinNotice.WinNoticeBuildData buildData = new WinNotice.WinNoticeBuildData();
						buildData.setMessageText(desc);
						buildData.setSureText("重启");
						buildData.setSystemDialog(true);
						dlg = new WinNotice(buildData) {
							@Override
							public void onClickOk() {
								LogUtil.logw("upgrade silence restart");
								AppLogicBase.restartProcess();
							}

							@Override
							public String getReportDialogId() {
								return "upgrade_force_restart";
							}
						};
					} else {
						WinConfirm.WinConfirmBuildData buildData = new WinConfirm.WinConfirmBuildData();
						buildData.setMessageText(desc);
						buildData.setSureText("重启");
						buildData.setSystemDialog(true);
						dlg = new WinConfirm(buildData) {
							@Override
							public void onClickOk() {
								LogUtil.logw("upgrade silence restart");
								processUpdateApk(newApk);
								AppLogicBase.restartProcess();
							}

							@Override
							public String getReportDialogId() {
								// TODO Auto-generated method stub
								return "upgrade_normal_restart";
							}
						};
					}
					dlg.show();
				}
			}, 0);
			return null;
		}
		return null;
	}
}

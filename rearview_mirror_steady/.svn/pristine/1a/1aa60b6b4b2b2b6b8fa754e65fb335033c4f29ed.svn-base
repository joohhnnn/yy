package com.txznet.txz.module.version;

import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.base.BaseApplication;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.ui.dialog.WinConfirm;
import com.txznet.comm.ui.dialog.WinNotice;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.util.PackageInstaller;

/**
 * 版本管理模块，负责版本数据管理，版本更新
 * 
 * @author User
 *
 */
public class UpgradeManager extends IModule {
	static UpgradeManager sModuleInstance = new UpgradeManager();

	private UpgradeManager() {
		// 读取系统里其他包的版本
	}

	public static UpgradeManager getInstance() {
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_INSTALL_APK);
		return super.initialize_BeforeStartJni();
	}
	
	final static int FLAG_FORCE_HINT_UPGRADE = 0x1; // 强制提示升级
	final static int FLAG_FORCE_INSTALL_INNER_APK = 0x2; // 强制内部apk升级
	final static int FLAG_FORCE_INSTALL = 0x4; //强制升级

	void procInstallApk(final UiEquipment.Notify_InstallApk pbNotifyInstallApk) {
		if (pbNotifyInstallApk.uint32Flag == null ) {
			pbNotifyInstallApk.uint32Flag = 0;
		}
		boolean bSupportOut = supportOutterApk(pbNotifyInstallApk.strPackageName);
		boolean bSilentUpdate = ((pbNotifyInstallApk.uint32Flag & FLAG_FORCE_HINT_UPGRADE) == 0)
				&& (bSupportOut || PackageManager.getInstance().checkAppExist(
						"com.txznet.apkinstaller"));

		JNIHelper.logd("begin install package: "
				+ pbNotifyInstallApk.strApkPath + ", flag=" + pbNotifyInstallApk.uint32Flag);
		
		String _shortName = "unk";
		if (pbNotifyInstallApk.strPackageName.startsWith("com.txznet.")) {
			_shortName = pbNotifyInstallApk.strPackageName.substring("com.txznet.".length());
		}
		final String shortName = _shortName;
		
		MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_ENTER_PREFIX + "all", MonitorUtil.UPGRADE_ENTER_PREFIX + shortName);
		
		pbNotifyInstallApk.bForceUpgrade = ((pbNotifyInstallApk.uint32Flag & FLAG_FORCE_INSTALL) != 0);

		// 判断是否使用外部apk升级方式
		if ((pbNotifyInstallApk.uint32Flag & FLAG_FORCE_INSTALL_INNER_APK) == 0
				&& bSupportOut) {
			MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_LOADER_PREFIX + "all", MonitorUtil.UPGRADE_LOADER_PREFIX + shortName);
			
			JSONBuilder json = new JSONBuilder();
			// 按时间重新移动到新位置
			// String newName = pbNotifyInstallApk.strApkPath + "."
			// + System.currentTimeMillis();
//			File f = new File(pbNotifyInstallApk.strApkPath);
//			f.renameTo(new File(newName));
			json.put(BaseApplication.SP_KEY_APK, pbNotifyInstallApk.strApkPath);
			json.put("force", pbNotifyInstallApk.bForceUpgrade);
			if (!bSilentUpdate) {
				json.put("desc", pbNotifyInstallApk.strInstallTips);
			}
			ServiceManager.getInstance().sendInvoke(
					pbNotifyInstallApk.strPackageName, "comm.update.upgrade",
					json.toBytes(), null);
			return;
		}

		// 静默升级
		if (bSilentUpdate) {
			MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_SILENT_PREFIX + "all", MonitorUtil.UPGRADE_SILENT_PREFIX + shortName);
			if (PackageInstaller
					.installApkByPackageManager(pbNotifyInstallApk.strApkPath)) {
				return;
			}
		}

		// 非静默升级
		String strInstall;
		if (pbNotifyInstallApk.strApkPath.startsWith("http://")) {
			MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_DOWN_PREFIX + "all", MonitorUtil.UPGRADE_DOWN_PREFIX + shortName);
			strInstall = "下载更新";
		} else {
			MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_INSTALL_PREFIX + "all", MonitorUtil.UPGRADE_INSTALL_PREFIX + shortName);
			strInstall = "立即升级";
		}
		if (pbNotifyInstallApk.bForceUpgrade != null
				&& pbNotifyInstallApk.bForceUpgrade == true) {
			MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_HINT_PREFIX + "all", MonitorUtil.UPGRADE_HINT_PREFIX + shortName);
			
			WinNotice win = new WinNotice() {
				@Override
				public void onClickOk() {
					MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_CLOSE_PREFIX + "all", MonitorUtil.UPGRADE_CLOSE_PREFIX + shortName);
					
					PackageInstaller
							.installApkByIntent(pbNotifyInstallApk.strApkPath);
				}
			}.setTitle("软件更新").setMessage(pbNotifyInstallApk.strInstallTips)
					.setSureText(strInstall);
			win.setTextScroll(true);
			win.show();
		} else {
			MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_CONFIRM_PREFIX + "all", MonitorUtil.UPGRADE_CONFIRM_PREFIX + shortName);
			
			WinConfirm win = new WinConfirm() {
				@Override
				public void onClickOk() {
					MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_SURE_PREFIX + "all", MonitorUtil.UPGRADE_SURE_PREFIX + shortName);
					
					PackageInstaller
							.installApkByIntent(pbNotifyInstallApk.strApkPath);
				}

				@Override
				public void onClickCancel() {
					MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_CANCEL_PREFIX + "all", MonitorUtil.UPGRADE_CANCEL_PREFIX + shortName);
				}

				@Override
				public void onBackPressed() {
					MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_CANCEL_PREFIX + "all", MonitorUtil.UPGRADE_CANCEL_PREFIX + shortName);
					this.dismiss();
				}
			}.setTitle("软件更新").setMessage(pbNotifyInstallApk.strInstallTips)
					.setSureText(strInstall).setCancelText("下次再说");
			win.setTextScroll(true);
			win.show();
		}
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		if (eventId == UiEvent.EVENT_ACTION_EQUIPMENT) {
			switch (subEventId) {
			case UiEquipment.SUBEVENT_NOTIFY_INSTALL_APK:
				// TODO 打开升级询问窗口或者静默升级
				try {

					UiEquipment.Notify_InstallApk pbNotifyInstallApk = UiEquipment.Notify_InstallApk
							.parseFrom(data);
					JNIHelper.logi("package="
							+ pbNotifyInstallApk.strPackageName
							+ "need to install to version="
							+ pbNotifyInstallApk.strVersion + ", path="
							+ pbNotifyInstallApk.strApkPath);
					procInstallApk(pbNotifyInstallApk);

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}

		}

		return super.onEvent(eventId, subEventId, data);
	}

	public static boolean supportOutterApk(String packageName) {
		android.content.pm.PackageManager pm = GlobalContext.get()
				.getPackageManager();
		try {
			return pm.getApplicationInfo(packageName,
					android.content.pm.PackageManager.GET_META_DATA).metaData
					.getBoolean("TXZApkLoader", false);
		} catch (Exception e) {
		}
		return false;
	}

}

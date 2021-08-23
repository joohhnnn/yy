package com.txznet.txz.module.version;

import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.dialog2.WinConfirm;
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.comm.ui.dialog2.WinNotice;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.component.media.base.IMediaTool;
import com.txznet.txz.component.music.txz.MusicTongTing;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.PackageInstaller;
import com.txznet.txz.util.runnables.Runnable1;

/**
 * 版本管理模块，负责版本数据管理，版本更新
 * 
 * @author User
 *
 */
public class VersionManager extends IModule {
	static VersionManager sModuleInstance = null;

	private VersionManager() {

	}

	public static VersionManager getInstance() {
		if (sModuleInstance == null) {
			synchronized (VersionManager.class) {
				if (sModuleInstance == null)
					sModuleInstance = new VersionManager();
			}
		}
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
//		regEvent(UiEvent.EVENT_ACTION_NEW_VERSION_READY);
//		regEvent(UiEvent.EVENT_VERSION_LISENCE_ALLOWED);
//		regEvent(UiEvent.EVENT_VERSION_LISENCE_FORBIDDEN);
		return super.initialize_BeforeStartJni();
	}

	public String getUserVersionNumber() {
		return GlobalContext.get().getResources().getString(R.string.app_version);
	}

	public String getProjectName() {
		return LicenseManager.getInstance().getAppId();
	}

	boolean mLisenceForbidden = false;
	Runnable mRecheckLisence = new Runnable() {
		@Override
		public void run() {
//			JNIHelper.sendEvent(UiEvent.EVENT_VERSION_LISENCE_CHECK, 0);
		}
	};

	public boolean isLisenceForbidden() {
		if (mLisenceForbidden) {
			AppLogic.removeBackGroundCallback(mRecheckLisence);
			AppLogic.runOnBackGround(mRecheckLisence, 10000);
		}
		return mLisenceForbidden;
	}

	Thread mInstallThread;
	WinDialog mWinConfirmUpdate;

	void procNewVersionInfo(UiEvent.VersionInfo newVersionInfo) {
		if (null != mWinConfirmUpdate) {
			mWinConfirmUpdate.dismiss("The Dialog did not close last time");
			mWinConfirmUpdate = null;
		}
		Thread t = mInstallThread;
		mInstallThread = null;
		if (t != null)
			t.interrupt();
		mInstallThread = new Thread(new Runnable1<UiEvent.VersionInfo>(
				newVersionInfo) {
			@Override
			public void run() {
				boolean bSilentUpdate = true; /*
											 * (PackageManager.PERMISSION_GRANTED
											 * == TXZApp .getApp()
											 * .getPackageManager()
											 * .checkPermission(
											 * "android.permission.INSTALL_PACKAGES"
											 * ,
											 * TXZApp.getApp().getPackageName()
											 * ));
											 */

				while (mInstallThread != null) {
					boolean bBusy = false;
					do {
						// 声控导航帮助
						boolean isNav = NavManager.getInstance().isNavi();
						boolean isRecordWinShow = RecorderWin.isOpened();

						if (bSilentUpdate
								&& !isNav && !isRecordWinShow) // 界面打开中，不允许静默升级
						{
							JNIHelper
									.logw("waiting for update version: UI is busy");
							bBusy = true;
							break;
						}
						if (AsrManager.getInstance().isBusy()) // 录音识别中
						{
							JNIHelper
									.logw("waiting for update version: Asr is busy");
							bBusy = true;
							break;
						}
						if (TtsManager.getInstance().isBusy()) // Tts播报中
						{
							JNIHelper
									.logw("waiting for update version: Tts is busy");
							bBusy = true;
							break;
						}
						if (CallManager.getInstance().isIdle() == false) // 电话处理中
						{
							JNIHelper
									.logw("waiting for update version: Call is busy");
							bBusy = true;
							break;
						}
						if (NavManager.getInstance().isNavi()) // 导航中
						{
							JNIHelper
									.logw("waiting for update version: Nav is busy");
							bBusy = true;
							break;
						}
						if (IMediaTool.PLAYER_STATUS.PLAYING ==
								MusicTongTing.getInstance().getStatus()) // 音乐中
						{
							JNIHelper
									.logw("waiting for update version: Music is busy");
							bBusy = true;
							break;
						}
					} while (false);
					if (bBusy == false)
						break;
					try {
						Thread.sleep(5000); // 等待应用程序不工作，5秒后尝试升级
					} catch (Exception e) {
						JNIHelper
								.logw("interrupt by another update version request");
						return;
					}
				}

				if (mInstallThread == null)
					return;

				JNIHelper.logd("begin install package: " + mP1.strUrl);

				do {
					if (bSilentUpdate) {
						if (PackageInstaller
								.installApkByPackageManager(mP1.strUrl)) {
							// 开始安装后一天内不再进行版本校验
//							JNIHelper
//									.sendEvent(
//											UiEvent.EVENT_ACTION_CHECK_NEW_VERSION_NEXT,
//											0);
							break;
						}
					}

					String strInstall = "升级(免流量)";
					if (mP1.strUrl.startsWith("http://"))
						strInstall = "下载更新";
					if (mP1.boolForce != null && mP1.boolForce == true) {
						WinNotice.WinNoticeBuildData buildData = new WinNotice.WinNoticeBuildData();
						buildData.setSureText(strInstall)
								.setMessageText(mP1.strDesc)
								.setTitleText("软件更新");
						WinNotice win = new WinNotice(buildData) {
							@Override
							public void onClickOk() {
								PackageInstaller.installApkByIntent(mP1.strUrl);
							}

							@Override
							public String getReportDialogId() {
								return "upgrade_force_apk";
							}
						};
						win.show();
						mWinConfirmUpdate = win;
					} else {
						WinConfirm.WinConfirmBuildData buildData = new WinConfirm.WinConfirmBuildData();
						buildData.setSureText(strInstall).setCancelText("下次再说")
								.setMessageText(mP1.strDesc)
								.setTitleText("软件更新");
						WinConfirm win = new WinConfirm(buildData) {
							@Override
							public void onClickOk() {
								PackageInstaller.installApkByIntent(mP1.strUrl);
							}

							@Override
							public void onClickCancel() {
//								JNIHelper
//										.sendEvent(
//												UiEvent.EVENT_ACTION_CHECK_NEW_VERSION_NEXT,
//												0);
							}

							@Override
							public void onBackPressed() {
								this.dismissInner();
							}

							@Override
							public String getReportDialogId() {
								return "upgrade_normal_apk";
							}
						};
						win.show();
						mWinConfirmUpdate = win;
					}
				} while (false);

				mInstallThread = null;
			}
		});
		mInstallThread.start();
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
//		switch (eventId) {
//		case UiEvent.EVENT_ACTION_NEW_VERSION_READY:
//			// TODO 打开升级询问窗口或者静默升级
//			try {
//				UiEvent.VersionInfo newVersionInfo = UiEvent.VersionInfo
//						.parseFrom(data);
//				procNewVersionInfo(newVersionInfo);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			break;
//		case UiEvent.EVENT_VERSION_LISENCE_ALLOWED:
//			mLisenceForbidden = false;
//			TXZApp.getApp().removeBackGroundCallback(mRecheckLisence);
//			break;
//		case UiEvent.EVENT_VERSION_LISENCE_FORBIDDEN:
//			if (mLisenceForbidden == false) {
//				TtsManager
//						.getInstance()
//						.speakText(
//								NativeData
//										.getResString("RS_TIPS_VERSION_LISENCE_FORBIDDEN"));
//			}
//			mLisenceForbidden = true;
//			break;
//		}
		return super.onEvent(eventId, subEventId, data);
	}
}

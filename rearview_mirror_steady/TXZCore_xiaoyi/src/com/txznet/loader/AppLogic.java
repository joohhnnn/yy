package com.txznet.loader;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;

import com.txznet.comm.base.CrashCommonHandler;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.comm.version.ApkVersion;
import com.txznet.comm.version.TXZVersion;
import com.txznet.txz.CoreService;
import com.txznet.txz.R;
import com.txznet.txz.db.SQLiteHelper;
import com.txznet.txz.db.SQLiteRawUtil;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.ModuleManager;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.audio.AudioManager;
import com.txznet.txz.module.bt.BluetoothManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.camera.CameraManager;
import com.txznet.txz.module.cmd.CmdManager;
import com.txznet.txz.module.contact.ContactManager;
import com.txznet.txz.module.download.BackdoorManager;
import com.txznet.txz.module.download.DownloadManager;
import com.txznet.txz.module.fm.FmManager;
import com.txznet.txz.module.launch.LaunchManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.remoteregcmd.RemoteRegCmdManager;
import com.txznet.txz.module.remoteservice.RemoteServiceManager;
import com.txznet.txz.module.resource.ResourceManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.team.TeamManager;
import com.txznet.txz.module.text.TextManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.version.LicenseManager;
import com.txznet.txz.module.version.UpgradeManager;
import com.txznet.txz.module.version.VersionManager;
import com.txznet.txz.module.volume.VolumeManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.service.TXZService;
import com.txznet.txz.ui.win.help.WinHelpDetail;
import com.txznet.txz.ui.win.help.WinHelpDetailTops;
import com.txznet.txz.ui.win.help.WinHelpTops;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.WinRecordCycler;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.NativeHelper;
import com.txznet.txz.util.NativeHelper.UnzipOption;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.SDCardUtil;

public class AppLogic extends AppLogicBase {
	@Override
	public void onCreate() {
		super.onCreate();

		printTimeSinceLast("begin");
		// JNIHelper.addLibraryPath(GlobalContext.get(), solibs_path);
		// BaseApplication.printTimeSinceLast("addLibraryPath");

		com.loc.w.sUncaughtExceptionHandler = CrashCommonHandler.getInstance();
		
		// ????????????????????????
		if (!isMainProcess()) {
			return;
		}
		
		updateAppInfo();
	}

	// /////////////////////////////////////////////////////////////////////////

	static void initModuleList() {
		ModuleManager.getInstance().addModule(ResourceManager.getInstance());
		ModuleManager.getInstance().addModule(TtsManager.getInstance());
		ModuleManager.getInstance().addModule(AsrManager.getInstance());
		ModuleManager.getInstance().addModule(WakeupManager.getInstance());
		ModuleManager.getInstance().addModule(TextManager.getInstance());
		ModuleManager.getInstance().addModule(CallManager.getInstance());
		ModuleManager.getInstance().addModule(PackageManager.getInstance());
		ModuleManager.getInstance().addModule(BluetoothManager.getInstance());
		ModuleManager.getInstance().addModule(MusicManager.getInstance());
		ModuleManager.getInstance().addModule(VolumeManager.getInstance());
		ModuleManager.getInstance().addModule(NetworkManager.getInstance());
		ModuleManager.getInstance().addModule(ContactManager.getInstance());
		ModuleManager.getInstance().addModule(VersionManager.getInstance());
		ModuleManager.getInstance().addModule(LaunchManager.getInstance());
		ModuleManager.getInstance().addModule(NavManager.getInstance());
		ModuleManager.getInstance().addModule(CmdManager.getInstance());
		ModuleManager.getInstance().addModule(RemoteServiceManager.getInstance());
		// ModuleManager.getInstance().addModule(WifiDirectManager.getInstance());
		ModuleManager.getInstance().addModule(RecordManager.getInstance());
		ModuleManager.getInstance().addModule(WeixinManager.getInstance());
		ModuleManager.getInstance().addModule(LocationManager.getInstance());
		ModuleManager.getInstance().addModule(SenceManager.getInstance());
		ModuleManager.getInstance().addModule(RemoteRegCmdManager.getInstance());
		ModuleManager.getInstance().addModule(CameraManager.getInstance());
		ModuleManager.getInstance().addModule(LicenseManager.getInstance());
		ModuleManager.getInstance().addModule(UpgradeManager.getInstance());
		ModuleManager.getInstance().addModule(TeamManager.getInstance());
		ModuleManager.getInstance().addModule(FmManager.getInstance());
		ModuleManager.getInstance().addModule(AudioManager.getInstance());
		ModuleManager.getInstance().addModule(DownloadManager.getInstance());
		ModuleManager.getInstance().addModule(BackdoorManager.getInstance());
	}

	static boolean mInited = false;

	public static boolean isInited() {
		return mInited;
	}
	
	@Override
	public void onReset() {
		super.onReset();
		
		String appDir = GlobalContext.get().getApplicationInfo().dataDir;
		FileUtil.removeDirectory(appDir + "/grm");
		FileUtil.removeDirectory(appDir + "/cfg");
		FileUtil.removeDirectory(appDir + "/files");
	}

	public static void initWhenStart() {
		JNIHelper.logd("==============begin init all: " + mInited);
		if (mInited)
			return;

		// ??????3?????????
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(GlobalContext.get()
						.getApplicationInfo().packageName + ".onCreateApp");
				GlobalContext.get().sendBroadcast(intent);
			}
		}, 3000);

		// sdcard????????????????????????
		if (SDCardUtil.checkFileReadOnly(SDCardUtil.DEFAULT_SDCARD_PATH)) {
			// ??????sp???sdcard??????
			String spSDCardPath = PreferenceUtil.getInstance().getSDCardPath();
			SDCardUtil.setSDCardPath(spSDCardPath);
		} else {
			// ??????????????????
			SDCardUtil.setSDCardPath(SDCardUtil.DEFAULT_SDCARD_PATH);
		}

		// ???????????????
		String appDir = GlobalContext.get().getApplicationInfo().dataDir;
		NativeHelper.unzipFiles(
				GlobalContext.get().getApplicationInfo().sourceDir,
				new UnzipOption[] {
						UnzipOption.createUnzipDirOption("assets/data/", appDir
								+ "/data/"),
						UnzipOption.createUnzipDirOption(
								"assets/tts_yunzhisheng/", appDir
										+ "/tts_yunzhisheng/"),
						UnzipOption.createUnzipDirOption("assets/tts_ifly/",
								appDir + "/tts_ifly/") }, 5000);
		printTimeSinceLast("ApkFile_unzip");

		// ??????CoreService??????
		Intent in = new Intent(GlobalContext.get(), CoreService.class);
		try {
			GlobalContext.get().startService(in);
		} catch (Exception e) {
		}

		// ?????????????????????
		initModuleList();
		printTimeSinceLast("initModuleList");

		// ????????????????????????
		ModuleManager.getInstance().initialize_BeforeLoadLibrary();
		printTimeSinceLast("initialize_BeforeLoadLibrary");
		JNIHelper.initNativeLibrary();
		printTimeSinceLast("initNativeLibrary");
		ModuleManager.getInstance().initialize_AfterLoadLibrary();
		printTimeSinceLast("initialize_AfterLoadLibrary");

		// ??????JNI??????
		ModuleManager.getInstance().initialize_BeforeStartJni();
		printTimeSinceLast("initialize_BeforeStartJni");

		JNIHelper.start();

		ModuleManager.getInstance().initialize_AfterStartJni();
		printTimeSinceLast("initialize_AfterStartJni");

		// ???????????????UI??????
		RecorderWin.close();
		WinRecordCycler.getInstance();
		WinHelpTops.getInstance();
		WinHelpDetail.getInstance();
		WinHelpDetailTops.getInstance();
		// WinMapDialog.getInstance();

		// ?????????ImageLoader
		ImageLoaderInitialize.initImageLoader(AppLogicBase.getApp());

		ServiceManager.getInstance().broadInvoke("comm.exitTXZ.inited", null);

		ServiceManager.getInstance().sendInvoke(ServiceManager.BT,
				"comm.exitTXZ.inited", null, null);
		ServiceManager.getInstance().sendInvoke(ServiceManager.LAUNCHER,
				"comm.exitTXZ.inited", null, null);
		ServiceManager.getInstance().sendInvoke(ServiceManager.FM,
				"comm.exitTXZ.inited", null, null);

		mInited = true;

		TXZService.notifyInited();

		PackageManager.getInstance().refreshAllTXZVersionInfo();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		// ??????????????????????????????app??????
		// ?????????????????????????????????????????????????????????????????????????????????????????????
		// ?????????????????????????????????????????????????????????????????????
		// ???????????????PackageManager????????????????????????????????????
		if (!PackageManager.getInstance().mInitedInChinese && ("zh".equals(newConfig.locale.getLanguage()))){
			PackageManager.getInstance().refreshAppList();
		}

	}
	
	public void updateAppInfo() {
		SQLiteHelper sqLiteHelper = null;
		SQLiteDatabase db = null;
		try {
			sqLiteHelper = new SQLiteHelper(getApp());
			db = sqLiteHelper.getWritableDatabase();
			SQLiteRawUtil.insertAppInfo(db,ApkVersion.versionName + "_" + TXZVersion.PACKTIME, getResources().getString(R.string.copyright));
		} catch (Exception e) {
		} finally {
			if (db != null) {
				db.close();
			}
			if (sqLiteHelper != null) {
				sqLiteHelper.close();
			}
		}
	}
}

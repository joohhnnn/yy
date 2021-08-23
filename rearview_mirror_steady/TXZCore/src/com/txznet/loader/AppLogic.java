package com.txznet.loader;

import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.recordwin.RecordWin2;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
//import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.comm.version.ApkVersion;
import com.txznet.comm.version.FactoryVersion;
import com.txznet.record.ui.WinRecord;
import com.txznet.txz.CoreService;
import com.txznet.txz.R;
import com.txznet.txz.component.home.HomeControlManager;
import com.txznet.txz.component.media.MediaPriorityManager;
import com.txznet.txz.db.AppInfoProvider;
import com.txznet.txz.db.SQLiteHelper;
import com.txznet.txz.db.SQLiteRawUtil;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.ModuleManager;
import com.txznet.txz.module.film.FilmManager;
import com.txznet.txz.module.ac.ACManager;
import com.txznet.txz.module.account.AccountManager;
import com.txznet.txz.module.advertising.AdvertisingManager;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.audio.AudioManager;
import com.txznet.txz.module.bt.BluetoothManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.camera.CameraManager;
import com.txznet.txz.module.cmd.CmdManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.constellation.ConstellationManager;
import com.txznet.txz.module.contact.ContactManager;
import com.txznet.txz.module.device.BindDeviceManager;
import com.txznet.txz.module.dns.DnsManager;
import com.txznet.txz.module.download.BackdoorManager;
import com.txznet.txz.module.download.DownloadManager;
import com.txznet.txz.module.fake.FakeReqManager;
import com.txznet.txz.module.feedback.FeedbackManager;
import com.txznet.txz.module.fm.FmManager;
import com.txznet.txz.module.home.CarControlHomeManager;
import com.txznet.txz.module.launch.LaunchManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.mtj.MtjModule;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.news.NewsManager;
import com.txznet.txz.module.offlinepromote.OfflinePromoteManager;
import com.txznet.txz.module.plugin.PluginControlManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.reminder.ReminderManager;
import com.txznet.txz.module.remoteregcmd.RemoteRegCmdManager;
import com.txznet.txz.module.remoteservice.RemoteServiceManager;
import com.txznet.txz.module.resource.ResourceManager;
import com.txznet.txz.module.roadtraffic.RoadTrafficManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.sensor.SensorControlManager;
import com.txznet.txz.module.sim.SimManager;
import com.txznet.txz.module.team.TeamManager;
import com.txznet.txz.module.text.TextManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.ticket.QiWuTicketManager;
import com.txznet.txz.module.ticket.TicketManager;
import com.txznet.txz.module.transfer.TransferManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.userconf.UserConf;
import com.txznet.txz.module.version.LicenseManager;
import com.txznet.txz.module.version.TXZVersion;
import com.txznet.txz.module.version.UpgradeManager;
import com.txznet.txz.module.version.VersionManager;
import com.txznet.txz.module.version.VisualUpgradeManager;
import com.txznet.txz.module.voiceprintrecognition.VoiceprintRecognitionManager;
import com.txznet.txz.module.volume.VolumeManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.weather.WeatherManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.service.TXZService;
import com.txznet.txz.udprpc.TXZUdpServer;
import com.txznet.txz.udprpc.UdpCmdDispatcher;
import com.txznet.txz.ui.widget.MarkFloatView;
import com.txznet.txz.ui.win.help.WinHelpDetailTops;
import com.txznet.txz.ui.win.help.WinHelpManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.WinRecordCycler;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.NativeHelper;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.TXZHandler;
import com.txznet.txz.util.NativeHelper.UnzipOption;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.SDCardUtil;

import android.content.Intent;
import static com.txznet.txz.component.command.CommandManager.updateAdapterLocalCommandFile;
import static com.txznet.txz.component.command.CommandManager.updateCoreLocalCommandFile;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;

import java.util.HashMap;

public class AppLogic extends AppLogicBase {
	@Override
	public void onCreate() {
		super.onCreate();
		
		// 提升进程优先级
		TXZHandler.updateMaxPriority();
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				TXZHandler.updateMaxPriority();
			}
		}, 0);
		
		if (null == FactoryVersion.COMPUTER_TXZ) {
			FactoryVersion.COMPUTER_TXZ = TXZVersion.COMPUTER;
			FactoryVersion.PACKTIME_TXZ = TXZVersion.PACKTIME;
			FactoryVersion.SVNVERSION_TXZ = TXZVersion.SVNVERSION;
		}

		printTimeSinceLast("begin");
		// JNIHelper.addLibraryPath(GlobalContext.get(), solibs_path);
		// BaseApplication.printTimeSinceLast("addLibraryPath");
		
		// 判断是否为主进程
		if (!isMainProcess()) {
			return;
		}
		
		TXZPowerControl.notifyRlease();
		
		updateAppInfo();
		TXZUdpServer.getInstance().init();
		TXZUdpServer.getInstance().setCmdDispatcher(new UdpCmdDispatcher());

		mDeviceBootTime = SystemClock.elapsedRealtime();
		runOnBackGround(mTimerTask, mDelayTime);
	}

	// /////////////////////////////////////////////////////////////////////////

	static void initModuleList() {
		ModuleManager.getInstance().addModule(ResourceManager.getInstance());
		ModuleManager.getInstance().addModule(ConfigManager.getInstance());
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
		ModuleManager.getInstance().addModule(RoadTrafficManager.getInstance());
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
		ModuleManager.getInstance().addModule(TextResultHandle.getInstance());
		ModuleManager.getInstance().addModule(SimManager.getInstance());
		ModuleManager.getInstance().addModule(MtjModule.getInstance());
		ModuleManager.getInstance().addModule(WinManager.getInstance());
		ModuleManager.getInstance().addModule(TicketManager.getInstance());
		ModuleManager.getInstance().addModule(WinHelpManager.getInstance());
		ModuleManager.getInstance().addModule(ACManager.getInstance());
		ModuleManager.getInstance().addModule(PluginControlManager.getInstance());
		ModuleManager.getInstance().addModule(SensorControlManager.getInstance());
		ModuleManager.getInstance().addModule(AccountManager.getInstance());
		ModuleManager.getInstance().addModule(UserConf.getInstance());
		ModuleManager.getInstance().addModule(DnsManager.getInstance());
		ModuleManager.getInstance().addModule(FakeReqManager.getInstance());
		ModuleManager.getInstance().addModule(ReminderManager.getInstance());
		ModuleManager.getInstance().addModule(VisualUpgradeManager.getInstance());
		ModuleManager.getInstance().addModule(MediaPriorityManager.getInstance());
		ModuleManager.getInstance().addModule(TransferManager.getInstance());
		ModuleManager.getInstance().addModule(WeatherManager.getInstance());
		ModuleManager.getInstance().addModule(NewsManager.getInstance());
        ModuleManager.getInstance().addModule(HomeControlManager.getInstance());
        ModuleManager.getInstance().addModule(VoiceprintRecognitionManager.getInstance());
        ModuleManager.getInstance().addModule(AdvertisingManager.getInstance());
		ModuleManager.getInstance().addModule(FilmManager.getInstance());
		ModuleManager.getInstance().addModule(CarControlHomeManager.getInstance());
		ModuleManager.getInstance().addModule(BindDeviceManager.getInstance());
		ModuleManager.getInstance().addModule(ConstellationManager.getInstance());
		ModuleManager.getInstance().addModule(FeedbackManager.getInstance());
		ModuleManager.getInstance().addModule(QiWuTicketManager.getInstance());
		ModuleManager.getInstance().addModule(OfflinePromoteManager.getInstance());
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
	
	
	@Override
	public void onConfigChange(Bundle bundle) {
		super.onConfigChange(bundle);
		if (TextUtils.equals("screen", bundle.getString("type"))) {
			int[] location = new int[4];
			location[0] = bundle.getInt("x", 0);
			location[1] = bundle.getInt("y", 0);
			location[2] = bundle.getInt("width", 0);
			location[3] = bundle.getInt("height", 0);
			if (location[2] > 0 && location[3] > 0) {
				if (WinManager.getInstance().isRecordWin2()) {
					RecordWin2.getInstance().updateDisplayArea(location[0], location[1], location[2], location[3]);
				} else if (!WinManager.getInstance().hasThirdImpl()){
					WinRecord.getInstance().updateDisplayArea(location[0], location[1], location[2], location[3]);
				}
			}
		}
	}
	
	private static void notifyInitModules() {
		ServiceManager.getInstance().broadInvoke("comm.exitTXZ.inited", null);

		if (PackageManager.getInstance().checkAppExist(ServiceManager.BT)) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.BT,
					"comm.exitTXZ.inited", null, null);
		}
		if (PackageManager.getInstance().checkAppExist(ServiceManager.LAUNCHER)) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.LAUNCHER,
					"comm.exitTXZ.inited", null, null);
		}
		if (PackageManager.getInstance().checkAppExist(ServiceManager.FM)) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.FM,
					"comm.exitTXZ.inited", null, null);
		}
	}

	public static void initWhenStart() {
		JNIHelper.logd("==============begin init all: " + mInited);
		
		if (mInited) {
			if (TXZPowerControl.hasReleased()) {
				notifyInitModules();
			}
			TXZPowerControl.reinitTXZ();
			return;
		}

		// 延迟3秒发送
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(GlobalContext.get()
						.getApplicationInfo().packageName + ".onCreateApp");
				GlobalContext.get().sendBroadcast(intent);
			}
		}, 3000);

		// sdcard是否有只读状态过
		if (SDCardUtil.checkFileReadOnly(SDCardUtil.DEFAULT_SDCARD_PATH)) {
			// 设置sp的sdcard路径
			String spSDCardPath = PreferenceUtil.getInstance().getSDCardPath();
			SDCardUtil.setSDCardPath(spSDCardPath);
		} else {
			// 设置默认路径
			SDCardUtil.setSDCardPath(SDCardUtil.DEFAULT_SDCARD_PATH);
		}

		// 初始化资源
		String appDir = GlobalContext.get().getApplicationInfo().dataDir;
		if (checkHandlerThreadDelay == null) {
			HashMap<String, String> config = TXZFileConfigUtil.getConfig(TXZFileConfigUtil.KEY_CHECK_HANDLER_THREAD_DELAY);
			checkHandlerThreadDelay = 0L;
			if (config != null && config.get(TXZFileConfigUtil.KEY_CHECK_HANDLER_THREAD_DELAY) != null) {
				try {
					checkHandlerThreadDelay = Long.parseLong(config.get(TXZFileConfigUtil.KEY_CHECK_HANDLER_THREAD_DELAY));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			LogUtil.logd("AppLogic::" + checkHandlerThreadDelay);
		}
		NativeHelper.unzipFiles(
				GlobalContext.get().getApplicationInfo().sourceDir,
				new UnzipOption[] {
						UnzipOption.createUnzipDirOption("assets/data/", appDir
								+ "/data/"),
						UnzipOption.createUnzipTreeOption(
								"assets/dingdang/", appDir
										+ "/dingdang/"),
						UnzipOption.createUnzipDirOption(
								"assets/tts_yunzhisheng/", appDir
										+ "/tts_yunzhisheng/"),
						UnzipOption.createUnzipDirOption("assets/tts_ifly/",
								appDir + "/tts_ifly/") }, checkHandlerThreadDelay > 0 ? checkHandlerThreadDelay : 5000);
		printTimeSinceLast("ApkFile_unzip");

		updateCoreLocalCommandFile();
		updateAdapterLocalCommandFile();
		// 增加CoreService服务
		Intent in = new Intent(GlobalContext.get(), CoreService.class);
		try {
			GlobalContext.get().startService(in);
		} catch (Exception e) {
		}

		// 加入需要的模块
		initModuleList();
		printTimeSinceLast("initModuleList");

		// 按顺序执行初始化
		ModuleManager.getInstance().initialize_addPluginCommandProcessor();
		ModuleManager.getInstance().initialize_BeforeLoadLibrary();
		printTimeSinceLast("initialize_BeforeLoadLibrary");
		JNIHelper.initNativeLibrary();
		printTimeSinceLast("initNativeLibrary");
		ModuleManager.getInstance().initialize_AfterLoadLibrary();
		printTimeSinceLast("initialize_AfterLoadLibrary");

		// 启动JNI模块
		ModuleManager.getInstance().initialize_BeforeStartJni();
		printTimeSinceLast("initialize_BeforeStartJni");

		JNIHelper.start();

		ModuleManager.getInstance().initialize_AfterStartJni();
		printTimeSinceLast("initialize_AfterStartJni");

		// 初始化构建UI组件
		RecorderWin.close();
		WinRecordCycler.getInstance();
		WinHelpDetailTops.getInstance();
		ViewConfiger.getInstance().initCommConfig();
		if (BaseActivity.enableTestMask()) {
			MarkFloatView.getInstance(GlobalContext.get());
		}

		// 初始化ImageLoader
		ImageLoaderInitialize.initImageLoader(AppLogicBase.getApp());

		//初始化帮助信息
		WinHelpManager.getInstance().unZipHelpData();

		notifyInitModules();
		mInited = true;
		TXZPowerControl.notifyInit();
		TXZService.notifyInited();
		PackageManager.getInstance().refreshAllTXZVersionInfo();
	}
	

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		// 判断是否需要重新刷新app列表
		// 刷新目前仅限于第一次初始化为其他语言，之后修改系统语言为中文时
		// 一旦在中文环境中刷新列表，之后都不再做刷新处理
		// 简繁体转换PackageManager内已做处理，此处不做处理
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
			SQLiteRawUtil.insertAppInfo(db, ApkVersion.versionName, getResources().getString(R.string.copyright));
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
	
	
	public static void exit() {
		AppInfoProvider.release();
		AppLogicBase.exit();
	}

	private int mDelayTime = 60 * 1000;
	private long mDeviceBootTime;
	private long mLastRunningTime;
	Runnable mTimerTask = new Runnable() {
		@Override
		public void run() {
			removeBackGroundCallback(mTimerTask);
			//当前软件运行时间
			long currentRunningTime = SystemClock.elapsedRealtime() - mDeviceBootTime;
			PreferenceUtil.getInstance().setCurrentRunningTime(currentRunningTime);
			//总运行时长
			long totalRunningTime = PreferenceUtil.getInstance().getTotalRunningTime() + currentRunningTime - mLastRunningTime;
			PreferenceUtil.getInstance().setTotalRunningTime(totalRunningTime);
			//记录上次运行时长
			mLastRunningTime = currentRunningTime;
			runOnBackGround(mTimerTask, mDelayTime);
		}
	};
}

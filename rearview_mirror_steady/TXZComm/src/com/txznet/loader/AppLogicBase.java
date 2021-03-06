package com.txznet.loader;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.widget.Toast;

import com.txznet.comm.base.BaseApplication;
import com.txznet.comm.base.CrashCommonHandler;
import com.txznet.comm.base.CrashCommonHandler.CrashLisener;
import com.txznet.comm.base.CrashCommonHandler.OnCrashListener;
import com.txznet.comm.config.NavControlConfiger;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.udprpc.TXZUdpClient;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.resource.ResLoaderManager;
import com.txznet.comm.util.ScreenUtils;
import com.txznet.comm.version.ApkVersion;
import com.txznet.comm.version.FactoryVersion;
import com.txznet.comm.version.TXZVersion;
import com.txznet.txz.plugin.PluginLoader;
import com.txznet.txz.util.NativeHelper;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.TXZHandler;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppLogicBase {
    protected static AppLogicBase sInstance;
    protected static Application sApplication;
    public final static String SP_KEY_NEW_FILE_SIZE = "fileSize";
    public final static String SP_KEY_NEW_FILE = "fileList";
    public final static String SP_KEY_NEW_FILE_TYPE = "fileType";
    public final static String SP_KEY_RESET_FILE = "resetedList";
	
	public static Long checkHandlerThreadDelay = null;

    private static final String BROADCAST_ACTION_SUFFIX_FOREGROUND = ".status.foreground";
    private static final String BROADCAST_ACTION_SUFFIX_BACKGROUND = ".status.background";

    public static Application getApp() {
        return sApplication;
    }

    public static <T extends Application> T getApp(Class<T> clazz) {
        return (T) sApplication;
    }

    public static AppLogicBase getInstance() {
        return sInstance;
    }

    public void onCreate(Application app) {
        sInstance = this;
        sApplication = app;
        GlobalContext.set(sApplication);

        if (null == FactoryVersion.ApkPath) {
            FactoryVersion.ApkPath = sApplication.getApplicationInfo().sourceDir;
            FactoryVersion.versionCode = ApkVersion.versionCode;
            FactoryVersion.versionName = ApkVersion.versionName;
            FactoryVersion.COMPUTER = TXZVersion.COMPUTER;
            FactoryVersion.BRANCH = TXZVersion.BRANCH;
            FactoryVersion.SVNVERSION = TXZVersion.SVNVERSION;
            FactoryVersion.PACKTIME = TXZVersion.PACKTIME;
        }

        // ????????????????????????
        try {
            Method needReset = app.getClass().getDeclaredMethod("needReset");
            if ((Boolean) needReset.invoke(app)) {
                this.onReset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ??????so??????
        String LIB_PATH = app.getApplicationInfo().dataDir + "/solibs/";
        NativeHelper.addLibraryPath(app, LIB_PATH);
        NativeHelper.addLibraryPath_DexClassLoader(this.getClassLoader(), LIB_PATH);

		if (isMainProcess()) {
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
				LogUtil.logd("AppLogicBase::" + checkHandlerThreadDelay);
			}
			NativeHelper.unzipLibFiles(app.getApplicationInfo().sourceDir,
					LIB_PATH, checkHandlerThreadDelay > 0 ? checkHandlerThreadDelay : 5000);


            LogUtil.loge(sApplication.getPackageName() + ": VERSION="
                    + getVersionString() + " & " + ApkVersion.versionName + "_"
                    + ApkVersion.versionCode);

            TXZFileConfigUtil.checkAndCopyUpgradeCfgFile();

            // File LIB_DIR = new File(LIB_PATH);
            // for (File f : LIB_DIR.listFiles(new FileFilter() {
            // @Override
            // public boolean accept(File pathname) {
            // if (pathname.getName().endsWith(".so")) {
            // return true;
            // }
            // return false;
            // }
            // })) {
            // LogUtil.logd("load library: " + f.getAbsolutePath());
            // System.load(f.getAbsolutePath());
            // }
        }

        // ??????????????????
        try {
            String dir = Environment.getExternalStorageDirectory().getPath();
            CrashCommonHandler.init(sApplication, new CrashLisener(dir
                    + "/txz/report/"));
            CrashCommonHandler.setOnCrashListener(new OnCrashListener() {

                @Override
                public void onCaughtException() {
                    caughtException();
                }
            });
        } catch (Exception e) {
        }

        // ??????????????????
        TXZHandler.initLockWatch();

        // ????????????
        mBackThread = new HandlerThread("AppBack");
        mBackThread.start();
        mBackHandler = new TXZHandler(mBackThread.getLooper());

        // ?????????????????????????????????????????????
        mSlowThread = new HandlerThread("AppSlow");
        mSlowThread.start();
        mSlowHandler = new TXZHandler(mSlowThread.getLooper());
        mSlowHandler.post(new Runnable() {
			@Override
			public void run() {
				TXZHandler.updateToPriorityPriority(Process.THREAD_PRIORITY_BACKGROUND);
			}
		});

        // ??????Resources
        ResLoaderManager.getInstance().reloadRes();

        if (isMainProcess()) {
            // String packageName = sInstance.getApplicationInfo().packageName;

            // ??????
            Intent intent = new Intent(
                    sApplication.getApplicationInfo().packageName
                            + ".service.TXZService");
            intent.setPackage(sApplication.getApplicationInfo().packageName);
            PackageManager pm = sApplication.getPackageManager();
            List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent,
                    PackageManager.GET_SERVICES);
            if (resolveInfos != null && resolveInfos.size() > 0) {
                sApplication.startService(intent);
            }


            // // ???????????????????????????????????????
            // mRecordStatusObservable = new RecordStatusObservable(this);

            // ??????????????????????????????
            runOnBackGround(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(
                            sApplication.getApplicationInfo().packageName
                                    + ".onCreateApp");
                    sApplication.sendBroadcast(intent);
                }
            }, 3000);

            runOnBackGround(new Runnable() {

                @Override
                public void run() {
                    Intent intent = new Intent(sApplication.getApplicationInfo().packageName + ".onCreate");
                    sApplication.sendBroadcast(intent);
                }
            }, 0);

            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                    "txz.package.refresh", null, null);

            // ????????????????????????????????????????????????????????????
            if (SystemClock.elapsedRealtime() <= BaseApplication.MIN_RECORD_CLOCK) {
                clearStartTimeRecord();
            }

            //????????????5??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????0???????????????
            runOnSlowGround(new Runnable() {
                @Override
                public void run() {
                    clearStartTimeRecord();
                }
            }, BaseApplication.MIN_RUN_TIME);
        }

		IntentFilter installFilter = new IntentFilter(
				Intent.ACTION_PACKAGE_ADDED);
		installFilter.addDataScheme("package");
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				try {
					String packageName = intent.getDataString();
					if (packageName.startsWith("package:")) {
						packageName = packageName.substring(packageName
								.indexOf("package:") + "package:".length());
					}
					if (context.getApplicationInfo().packageName.equals(packageName)) {
						LogUtil.logw("recv self update notify, need restart self");
						restartProcess();
					}
				} catch (Exception e) {
				}
			}
		}, installFilter);
		
		LogUtil.logd("start progress: " + getProcessName() + ", old_ver=" + ApkVersion.versionCode + ", apk=" + sApplication.getApplicationInfo().sourceDir);
        
        IntentFilter filter = new IntentFilter(getApp().getPackageName() + ".config.change");
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if ((getApp().getPackageName() + ".config.change").equals(intent.getAction())) {
                    Bundle bundle = intent.getExtras();
                    if (null == bundle) {
                        LogUtil.e("AppLogicBase::onConfigChange: intent has no extras, cancel dispatching");
                        return;
                    }

                    AppLogicBase.this.onConfigChange(bundle);
                }
            }
        }, filter);
		if (!isMainProcess() || !GlobalContext.isTXZ()) {
			TXZUdpClient.getInstance().init();
		}
        this.onCreate();
    }

    public void onConfigChange(Bundle bundle) {

    }

    // ///////////////////////////////////////////////////////////////

    public void onCreate() {
        if (isMainProcess()) {
            NavControlConfiger.getInstance().init();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
		// int configWidthDp = ScreenUtils.getScreenWidthDp();
		// int configHeightDp = ScreenUtils.getScreenHeightDp();
		// if (configHeightDp != 0 && configWidthDp != 0) {
		// if (newConfig.screenWidthDp != configWidthDp
		// || newConfig.screenHeightDp != configHeightDp) {
		// Resources resources = GlobalContext.get().getResources();
		// newConfig.screenWidthDp = configWidthDp;
		// newConfig.screenHeightDp = configHeightDp;
		// resources.updateConfiguration(newConfig,
		// resources.getDisplayMetrics());
		// }
		// }
    }

    public void onTerminate() {
    }

    public void onLowMemory() {
    }

    public void onTrimMemory(int level) {
    }

    public void caughtException() {
    }

    public void destroy() {
    }

    /**
     * app???????????????????????????????????????????????????
     * ?????????????????????home/????????????????????????
     *
     * @param visible app???????????????????????????????????????
     */
    public void onVisibilityChanged(boolean visible) {
        if (visible) {
            sApplication.sendBroadcast(new Intent(sApplication.getApplicationInfo().packageName + BROADCAST_ACTION_SUFFIX_FOREGROUND));
        } else {
            sApplication.sendBroadcast(new Intent(sApplication.getApplicationInfo().packageName + BROADCAST_ACTION_SUFFIX_BACKGROUND));
        }
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????crash
     */
    public void onReset() {
        LogUtil.logd("need reset on AppLogic");
        resetData();
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param path
     * @param className
     * @param data
     * @return
     */
    public Object onLoadPlugin(String path, String className, byte[] data) {
        return PluginLoader.loadPlugin(path, className, data);
    }

    // ///////////////////////////////////////////////////////////////

    public ClassLoader getClassLoader() {
        return GlobalContext.get().getClassLoader();
    }

    public AssetManager getAssets() {
        return GlobalContext.get().getAssets();
    }

    public Resources getResources() {
        return GlobalContext.get().getResources();
    }

    // ///////////////////////////////////////////////////////////////

    public static String getVersionString() {
        if (null == sInstance) {
            return getDefaultVersionString();
        }

        return sInstance.getOverrideVersionString();
    }


    public static String getDefaultVersionString() {
        return "" + TXZVersion.PACKTIME + "_" + TXZVersion.COMPUTER + "_"
                + TXZVersion.SVNVERSION;
    }

    /**
     * ?????????????????????????????????crash???????????????????????????????????????
     *
     * @return ????????????
     */
    public String getOverrideVersionString() {
        return getDefaultVersionString();
    }

    // /////////////////////////////////////////////////////////////////////////////////////

    protected static HandlerThread mBackThread;
    protected static TXZHandler mBackHandler;
    protected static HandlerThread mSlowThread;
    protected static TXZHandler mSlowHandler;
    protected static Handler mUiHandler = new Handler(Looper.getMainLooper()); //UI?????????handler???????????????TXZHandler???????????????

    public static void runOnBackGround(Runnable r){
        runOnBackGround(r, 0);
    }


    public static void runOnSlowGround(Runnable r) {
        runOnSlowGround(r, 0);
    }

    public static void runOnUiGround(Runnable r) {
        runOnUiGround(r, 0);
    }

    public static Looper getBackgroundLooper() {
        if (mBackThread != null) {
            return mBackThread.getLooper();
        }
        return null;
    }

    public static void runOnBackGround(Runnable r, long delay) {
        if (delay > 0) {
            mBackHandler.postDelayed(r, delay);
        } else {
            mBackHandler.post(r);
        }
    }

    public static void removeBackGroundCallback(Runnable r) {
        mBackHandler.removeCallbacks(r);
    }

    public static void runOnSlowGround(Runnable r, long delay) {
        if (delay > 0) {
            mSlowHandler.postDelayed(r, delay);
        } else {
            mSlowHandler.post(r);
        }
    }

    public static void removeSlowGroundCallback(Runnable r) {
        mSlowHandler.removeCallbacks(r);
    }

    public static void heartbeatSlowGround() {
        mSlowHandler.heartbeat();
    }

    public static void runOnUiGround(Runnable r, long delay) {
        if (delay > 0) {
            mUiHandler.postDelayed(r, delay);
        } else {
            mUiHandler.post(r);
        }
    }

    public static void removeUiGroundCallback(Runnable r) {
        mUiHandler.removeCallbacks(r);
    }

    // /////////////////////////////////////////////////////////////////////////////////////

    public static String getProcessName() {
        String currentProcName = "";
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) GlobalContext.get()
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningAppProcessInfo processInfo : manager
                .getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                currentProcName = processInfo.processName;
                break;
            }
        }
        return currentProcName;
    }

    static Boolean mIsMainProcess = null;

    public static boolean isMainProcess() {
        if (mIsMainProcess == null) {
            mIsMainProcess = getProcessName().equals(
                    GlobalContext.get().getApplicationInfo().packageName);
        }
        return mIsMainProcess;
    }

    public static void restartProcess() {
        LogUtil.logd("app restart");
        
        clearStartTimeRecord();
        clearReceiverSet();
        
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static void showToast(final String tip) {
        runOnUiGround(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApp(), tip, Toast.LENGTH_SHORT).show();
            }
        }, 0);
    }

    public static void showToast(final String tip, int duration) {
        final int nDuration = duration > 0 ? Toast.LENGTH_LONG
                : Toast.LENGTH_SHORT;
        runOnUiGround(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApp(), tip, nDuration).show();
            }
        }, 0);
    }

    public static void showTip(String tip) {
        LogUtil.logd(tip);
    }

    public static void clearStartTimeRecord() {
        if (isMainProcess()) {
            try {
                LogUtil.logd("clear start time record");

                SharedPreferences mSharedPreferences = GlobalContext
                        .get()
                        .getSharedPreferences(
                                GlobalContext.get().getApplicationInfo().packageName
                                        + BaseApplication.SP_SUFFIX,
                                Context.MODE_PRIVATE);
                // ???????????????????????????????????????????????????
                if (mSharedPreferences.getString(
                        BaseApplication.SP_KEY_LAUNCH_TIMES, null) != null) {
                    Editor editor = mSharedPreferences.edit();
                    editor.remove(BaseApplication.SP_KEY_LAUNCH_TIMES); // ?????????????????????????????????????????????????????????????????????????????????
                    editor.commit();
                }
            } catch (Exception e) {
            }
        }
    }

    public static void exit() {
        LogUtil.logd("app exit");

        clearStartTimeRecord();
        clearReceiverSet();

        System.exit(0);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////

    public static long mLongLastTime = SystemClock.elapsedRealtime();

    public static void printTimeSinceLast(String tag) {
        long now = SystemClock.elapsedRealtime();
        long cost = now - mLongLastTime;
        LogUtil.recordLogStack(1);
        LogUtil.logd("" + Process.myPid() + " " + tag + " TimeSinceLast cost "
                + cost + "ms ");
        mLongLastTime = now;
    }

    public static void printStatementCycle(String tag) {
        LogUtil.recordLogStack(1);
        LogUtil.logd("" + Process.myPid() + " StatementCycle " + tag);
    }

    public void onCloseApp() {
        LogUtil.logw("Subclass should override this method !!!");
        android.os.Process.killProcess(Process.myPid());
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????
     * ?????????crash????????????????????????????????????????????????????????????????????????????????????
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param type  ????????????crash???????????????????????????1??????????????????0????????????????????????fileName.bak
     * @param files ???????????????????????????????????????String????????????new File(fileName);??????
     */
    public static void putFileIntoResetList(int type, Set<String> files) {
        synchronized (AppLogicBase.class) {
            try {
                SharedPreferences mSharedPreferences = GlobalContext
                        .get()
                        .getSharedPreferences(
                                GlobalContext.get().getApplicationInfo().packageName
                                        + BaseApplication.SP_SUFFIX,
                                Context.MODE_PRIVATE);
                int fileSize = mSharedPreferences.getInt(SP_KEY_NEW_FILE_SIZE, 0);
                Editor editor = mSharedPreferences.edit();
                editor.putInt(SP_KEY_NEW_FILE_SIZE, fileSize + 1);
                editor.putInt(SP_KEY_NEW_FILE_TYPE + (fileSize + 1), type);
                editor.putStringSet(SP_KEY_NEW_FILE + (fileSize + 1), files);
                LogUtil.logd("Reset:input size=" + (fileSize + 1) + ",files=" + files.size());
                editor.commit();
            } catch (Exception e) {
            }
        }
    }

    /**
     * ????????????????????????????????????crash???????????????
     *
     * @return
     */
    public static Set<String> getFileResetedList() {
        try {
            SharedPreferences mSharedPreferences = GlobalContext
                    .get()
                    .getSharedPreferences(
                            GlobalContext.get().getApplicationInfo().packageName
                                    + BaseApplication.SP_SUFFIX,
                            Context.MODE_PRIVATE);
            return mSharedPreferences.getStringSet(SP_KEY_RESET_FILE, null);
        } catch (Exception e) {
        }
        return null;
    }

    private void resetData() {
        try {
            SharedPreferences mSharedPreferences = GlobalContext
                    .get()
                    .getSharedPreferences(
                            GlobalContext.get().getApplicationInfo().packageName
                                    + BaseApplication.SP_SUFFIX,
                            Context.MODE_PRIVATE);
            if (mSharedPreferences.contains(SP_KEY_NEW_FILE_SIZE)) {
                int fileSize = mSharedPreferences.getInt(SP_KEY_NEW_FILE_SIZE, 0);
                LogUtil.logd("Reset:fileSize="+fileSize);
                if (fileSize > 0) {
                    Editor editor = mSharedPreferences.edit();
                    Set<String> fileNames = mSharedPreferences.getStringSet(SP_KEY_NEW_FILE+fileSize,null);
                    int type = mSharedPreferences.getInt(SP_KEY_NEW_FILE_TYPE+fileSize, 0);
                    Set<String> resetList = mSharedPreferences.getStringSet(SP_KEY_RESET_FILE, new HashSet<String>());
                    if (fileNames != null && fileNames.size() > 0) {
                        for (String fileName : fileNames) {
                            File file = new File(fileName);
                            if (file.exists()) {
                                resetList.add(fileName);
                                switch (type) {
                                    case 1:
                                        file.delete();
                                        LogUtil.logd("Reset:rename from["+fileName+"] to ["+fileName+".bak]");
                                        break;
                                    default:
                                        LogUtil.logd("Reset:remove file:"+fileName);
                                        File newFile = new File(fileName+".bak");
                                        file.renameTo(newFile);
                                        break;
                                }
                            }
                        }
                    }
                    editor.putStringSet(SP_KEY_RESET_FILE, resetList);
                    editor.remove(SP_KEY_NEW_FILE+fileSize);
                    editor.remove(SP_KEY_NEW_FILE_TYPE+fileSize);
                    editor.putInt(SP_KEY_NEW_FILE_SIZE, fileSize - 1);
                    editor.commit();
                    return ;
                }
            }
        } catch (Exception e) {
        }
    }

    private static HashSet<BroadcastReceiver> receiverSet = new HashSet<BroadcastReceiver>();

    public static boolean addReceiver(BroadcastReceiver receiver) {
        synchronized (receiverSet) {
            return receiverSet.add(receiver);
        }
    }

    public static boolean removeReceiver(BroadcastReceiver receiver) {
        synchronized (receiverSet) {
            return receiverSet.remove(receiver);
        }
    }

    private static void clearReceiverSet() {
        LogUtil.logd("Receiver:clear receivers");
        if (sApplication ==null) {
            LogUtil.logd("Receiver:Application == null");
            return;
        }

		synchronized (receiverSet) {
		BroadcastReceiver[] receivers = null;
			while (receiverSet.size() > 0) {
				receivers = receiverSet.toArray(new BroadcastReceiver[receiverSet.size()]);
				for (BroadcastReceiver broadcastReceiver : receivers) {
					sApplication.unregisterReceiver(broadcastReceiver);
				}
			}
		}
	}

	public  void  reBindCore(){

    }
}

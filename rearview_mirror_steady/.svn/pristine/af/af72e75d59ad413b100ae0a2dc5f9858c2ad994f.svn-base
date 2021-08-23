package com.txznet.loader;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.txznet.audio.ProcessMemoryMonitor;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.ui.dialog.WinNotice;
import com.txznet.comm.version.TXZVersion;
import com.txznet.music.BuildConfig;
import com.txznet.music.R;
import com.txznet.music.Time.TimeManager;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState.Operation;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.baseModule.plugin.PluginMusicManager;
import com.txznet.music.dao.DaoManager;
import com.txznet.music.data.kaola.KaoLaSDK;
import com.txznet.music.historyModule.bean.HistoryData;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.localModule.LocalAudioDataSource;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.push.PushManager;
import com.txznet.music.receiver.MusicManager;
import com.txznet.music.receiver.ShowAppReceiver;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.bean.ExitEvent;
import com.txznet.music.soundControlModule.asr.AsrManager;
import com.txznet.music.ui.InfoPopView;
import com.txznet.music.utils.DataInterfaceBroadcastHelper;
import com.txznet.music.utils.NetworkUtil;
import com.txznet.music.utils.PlayerCommunicationManager;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.SyncCoreData;
import com.txznet.music.utils.UIHelper;
import com.txznet.music.utils.UpdateToCoreUtil;

import org.json.JSONObject;

import java.util.List;


public class AppLogic extends AppLogicBase {

    private static final String TAG = "Music:App:";
    private final static int PREDATA = 5;// 音量增减的倍数
    private static final String ACTION_QUICK = "com.txznet.music.push.send";
    private static final String ACTION_TEST = "com.txznet.music.push.test";
    public static int width;
    // public static int height;
    public static float density;
    public static RefWatcher watcher;
    public static int txzVersion = 0;
    private DisplayMetrics displayMetrics;
    private boolean isFatal;

    // /**
    // * 配置ImageLoader
    // */
    // private void configImageLoader() {
    // ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApp()));
    // }
    private WinNotice mSpaceWarn = null;
    private ServiceManager.ConnectionListener mConnectionListener = new ServiceManager.ConnectionListener() {
        @Override
        public void onConnected(String serviceName) {
            if (ServiceManager.TXZ.equals(serviceName)) {
                initTXZVersion();
            }
        }

        @Override
        public void onDisconnected(String serviceName) {

        }
    };

    public static RefWatcher getRefWatcher() {
        return watcher;
    }


    @Override
    public void onCloseApp() {
        ReportEvent.reportExitEvent(ExitEvent.ACTION_EXIT_SOUND);
        UIHelper.exit();
    }

    @Override
    public void onCreate() {
        LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::onCreate()");
        MultiDex.install(GlobalContext.get());
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(GlobalContext.get())) {
            return;
        }
        watcher = LeakCanary.install((Application) GlobalContext.get());

        // 注册监听器
        DataInterfaceBroadcastHelper.initListeners();
        PluginMusicManager.getInstance();//装载插件
        if (!isMainProcess()) {
            return;
        }
        Constant.setIsExit(false);

        //先默认设置一个theme，准确的theme需要计算屏幕宽高
        GlobalContext.get().setTheme(R.style.AppThemeCheji);

        LogUtil.d(TAG + "Music VersionInfo: " + BuildConfig.VERSION_NAME + " " + BuildConfig.CODE_VERSION + " " + BuildConfig.BUILD_TIME + " " + TXZVersion.BRANCH);
        AsrManager.getInstance().regCMD();

        configImageLoader();
        DBManager.getInstance();
        WindowManager manager = (WindowManager) getApp().getSystemService(
                Context.WINDOW_SERVICE);
        if (displayMetrics == null) {
            displayMetrics = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(displayMetrics);
        }

        width = displayMetrics.widthPixels;
        // height = displayMetrics.heightPixels;
        density = displayMetrics.density;

        if (BuildConfig.DEBUG) {
            watcher = LeakCanary.install(getApp());
            // StrictMode.setThreadPolicy(new
            // StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            // StrictMode.setVmPolicy(new
            // StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        }

        String config = SharedPreferencesUtils.getConfig();
        if (TextUtils.isEmpty(config)) {
            config = "{\"arrPlay\":[{\"bNeedProcess\":false,\"play\":1,\"sid\":1,\"type\":2},{\"bNeedProcess\":false,\"play\":1,\"sid\":2,\"type\":1},{\"bNeedProcess\":false,\"play\":1,\"sid\":3,\"type\":2},{\"bNeedProcess\":false,\"play\":1,\"sid\":4,\"type\":2},{\"bNeedProcess\":false,\"play\":1,\"sid\":5,\"type\":3},{\"bNeedProcess\":false,\"play\":1,\"sid\":6,\"type\":1},{\"bNeedProcess\":false,\"play\":1,\"sid\":7,\"type\":1}],\"logoTag\":7}";
            SharedPreferencesUtils.setConfig(config);
        }

        LogUtil.logd(TAG + "last exit play::" + SharedPreferencesUtils.getIsPlay() + ",set play::"
                + SharedPreferencesUtils.getAppFirstPlay());
        if (SharedPreferencesUtils.getIsPlay()
                && SharedPreferencesUtils.getAppFirstPlay()) {
            PlayEngineFactory.getEngine().play(Operation.auto);
        } else {
            LogUtil.logd(TAG + "set is play false");
            SharedPreferencesUtils.setIsPlay(false);
        }


        MusicManager.getInstance();
        registerListener();

        InfoPopView.getInstance().addObserver();

        TimeManager.getInstance();

        // 音乐上报数据
//        ReportHelper.getInstance().sendReportData(Action.ACT_LOGIN);

        // 自动扫描sd卡中的数据
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                LocalAudioDataSource.getInstance().scanLocal(null);
            }
        }, 10 * 1000);


//        HeadSetHelper.getInstance().open(GlobalContext.get());

        SyncCoreData.syncCurStatusFullStyle();

        if (AppLogic.isMainProcess()) {
            IntentFilter filter = new IntentFilter(
                    "com.txznet.music.action.REQ_SYNC");
            getApp().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    SyncCoreData.syncCurMusicModel();
                    SyncCoreData.syncCurPlayerStatus();
                }
            }, filter);
            ProcessMemoryMonitor.getInstance().addMonitorProcess(new ProcessMemoryMonitor.MonitorProcess(Constant.PACKAGE_MAIN, null));
        }

        ServiceManager.getInstance().addConnectionListener(mConnectionListener);
        initTXZVersion();
        initData();
    }

    private void initTXZVersion() {
        try {
            PackageInfo packInfo = GlobalContext.get().getPackageManager()
                    .getPackageInfo(ServiceManager.TXZ, 0);
            txzVersion = packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "comm.PackageInfo", null, new ServiceManager.GetDataCallback() {
                    @Override
                    public void onGetInvokeResponse(ServiceManager.ServiceData data) {
                        if (null == data) {
                            return;
                        }
                        JSONObject json = data.getJSONObject();
                        try {
                            if (json == null) {
                                LogUtil.logw("get package VERSION failed: "
                                        + ServiceManager.TXZ
                                        + ", try to get from package manager");
                                json = new JSONObject();
                            } else {

                            }
                            PackageInfo packInfo = GlobalContext.get().getPackageManager()
                                    .getPackageInfo(ServiceManager.TXZ, 0);
                            txzVersion = packInfo.versionCode;
                            txzVersion = json.optInt("versionCode",
                                    packInfo.versionCode);
                        } catch (Exception e) {
                            LogUtil.loge("getTXZCoreVersion: " + e.getMessage());
                        }
                    }
                }, 60 * 1000);
    }

    private void initData() {
        // 在线的话禁用缓存列表，下次重新请求
//        if (!NetworkUtil.isNetworkAvailable(getApp())) {
//            AppLogic.runOnBackGround(new Runnable() {
//                @Override
//                public void run() {
//                    HistoryData newestHistory = DBManager.getInstance().findNewestHistory();
//                    if (newestHistory != null) {
//                        Logger.i(TAG, "newest history:" + newestHistory.toString());
//                        if (newestHistory.getType() == HistoryData.TYPE_AUDIO) {
//                            List<HistoryData> history = DBManager.getInstance().findMusicHistory();
//                            List<Audio> audios = DBManager.getInstance().convertHistoryDataToAudio(history);
//
//                            PlayEngineFactory.getEngine().setAudios(Operation.auto, audios, null, 0, PlayInfoManager.DATA_HISTORY);
//                        } else {
//                            Album album = newestHistory.getAlbum();
//                            Audio audio = newestHistory.getAudio();
//                            if (album == null || audio == null) {
//                                return;
//                            }
//                            List<Audio> audiosByAlbumId = DBManager.getInstance().findAudiosByAlbumId(album.getId());
//                            PlayEngineFactory.getEngine().setAudios(Operation.auto, audiosByAlbumId, album, audiosByAlbumId.indexOf(audio), PlayInfoManager.DATA_HISTORY);
//
//                        }
//                    }
//                }
//            });
//        }


        //初始化考拉
        KaoLaSDK.initData(GlobalContext.get());

    }

    private void registerListener() {
        PlayerCommunicationManager.getInstance();
        registerScreenListener();
        registerShowListener();
//        registerSearchListener();
        registerQuickPlayerListener();
    }

    private void registerQuickPlayerListener() {
        IntentFilter filter = new IntentFilter(ACTION_QUICK);
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                PushManager.getInstance().test();
            }
        }, filter);

        filter = new IntentFilter(ACTION_TEST);
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            }
        }, filter);

    }

//    private void registerSearchListener() {
//        IntentFilter filter=new IntentFilter(SearchAudioListener.INTENT_LISTENER_SEARCH);
//        GlobalContext.get().registerReceiver(new SearchAudioListener(),filter);
//    }

    private void registerShowListener() {
        IntentFilter filter = new IntentFilter(ShowAppReceiver.INTENT_SET_MOVE);
        filter.addAction(ShowAppReceiver.INTENT_SET_SHOW);
        GlobalContext.get().registerReceiver(new ShowAppReceiver(), filter);
    }

    private void registerScreenListener() {
//        IntentFilter filter = new IntentFilter(ShowScreenBroadcast.INTENT_SET_SHOW_SIZE);
//        GlobalContext.get().registerReceiver(new ShowScreenBroadcast(), filter);
    }


    /**
     * 配置ImageLoader
     */
    private void configImageLoader() {
        ImageFactory.getInstance();
    }

    @Override
    public void onTerminate() {
        LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::onTerminate()");
        super.onTerminate();
    }

    @Override
    public void caughtException() {
        LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::caughtException()");
        super.caughtException();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogUtil.logd(TAG + "[life][" + this.hashCode()
                + "]::onConfigurationChanged()" + newConfig);
        SyncCoreData.syncCurStatusFullStyle();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::onLowMemory()");
//        HeadSetHelper.getInstance().close(getApp());
        ImageFactory.getInstance().onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::onTrimMemory()，"
                + level);
        ImageFactory.getInstance().onTrimMemory(level);
        super.onTrimMemory(level);
    }

    // 播放列表共同维护同一份列表
    // public static List<Audio> audios = new ArrayList<Audio>();
    // public static List<HistoryAudio> historyAudios = new
    // ArrayList<HistoryAudio>();

    @Override
    public void destroy() {
        LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::destroy()");
        super.destroy();
    }

    @Override
    public void onReset() {
        super.onReset();
//        String appDir = GlobalContext.get().getApplicationInfo().dataDir;
//        FileUtil.removeDirectory(appDir + "/databases");
        GlobalContext.get().deleteDatabase(DaoManager.DB_NAME);
    }

    @Override
    public ClassLoader getClassLoader() {
        LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::getClassLoader()");
        return super.getClassLoader();
    }

    @Override
    public AssetManager getAssets() {
        LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::getAssets()");
        return super.getAssets();
    }

    @Override
    public Resources getResources() {
        LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::getResources()");
        return super.getResources();
    }

    @Override
    public void reBindCore() {
        //todo:需要同步本地数据给到Core
        LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::reBindCore()");
        UpdateToCoreUtil.updateMusicModel(LocalAudioDataSource.getInstance().getCache());
        super.reBindCore();
    }
}

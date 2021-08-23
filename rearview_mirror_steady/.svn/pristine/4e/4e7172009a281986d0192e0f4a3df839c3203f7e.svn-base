package com.txznet.loader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;

import com.nostra13.universalimageloader.utils.L;
import com.squareup.leakcanary.LeakCanary;
import com.txznet.audio.ProcessMemoryMonitor;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.ui.dialog.WinNotice;
import com.txznet.comm.util.ScreenUtils;
import com.txznet.comm.version.TXZVersion;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.BuildConfig;
import com.txznet.music.R;
import com.txznet.music.Time.TimeManager;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.AlbumLogic;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState.Operation;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.baseModule.logic.ManufacturerInvoker;
import com.txznet.music.baseModule.plugin.PluginMusicManager;
import com.txznet.music.data.dao.DaoManager;
import com.txznet.music.historyModule.bean.HistoryData;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.localModule.LocalAudioDataSource;
import com.txznet.music.playerModule.logic.PlayHelper;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.push.PushManager;
import com.txznet.music.receiver.ShowAppReceiver;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.bean.ExitEvent;
import com.txznet.music.soundControlModule.asr.AsrManager;
import com.txznet.music.ui.InfoPopView;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.DataInterfaceBroadcastHelper;
import com.txznet.music.utils.FileConfigUtil;
import com.txznet.music.utils.PlayerCommunicationManager;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.SyncCoreData;
import com.txznet.music.utils.UIHelper;
import com.txznet.music.utils.UpdateToCoreUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import org.json.JSONObject;

import java.util.List;

import static com.txznet.music.baseModule.bean.PlayerInfo.PLAYER_UI_STATUS_RELEASE;


public class AppLogic extends AppLogicBase {

    private static final String TAG = "Music:App:";
    private final static int PREDATA = 5;// 音量增减的倍数
    private static final String ACTION_QUICK = "com.txznet.music.push.send";
    private static final String ACTION_TEST = "com.txznet.music.push.test";
    //    public static int width;
    // public static int height;
//    public static float density;
//    public static RefWatcher watcher;
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
//    private ServiceManager.ConnectionListener mConnectionListener = new ServiceManager.ConnectionListener() {
//        @Override
//        public void onConnected(String serviceName) {
//            if (ServiceManager.TXZ.equals(serviceName)) {
//                initTXZVersion();
//            }
//        }
//
//        @Override
//        public void onDisconnected(String serviceName) {
//
//        }
//    };

//    public static RefWatcher getRefWatcher() {
//        return watcher;
//    }


    @Override
    public void onCloseApp() {
        ReportEvent.reportExitEvent(ExitEvent.ACTION_EXIT_SOUND);
        UIHelper.exit();
    }

    @Override
    public void onCreate() {
        LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::onCreate()");
        MultiDex.install(GlobalContext.get());
        LogUtil.logd("pid : " + android.os.Process.myPid() + ", sourceDir:" + GlobalContext.get().getApplicationInfo().sourceDir);
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(GlobalContext.get())) {
            return;
        }
//        watcher = LeakCanary.install((Application) GlobalContext.get());

        // 注册监听器
        DataInterfaceBroadcastHelper.initListeners();
        PluginMusicManager.getInstance();//装载插件
        if (!isMainProcess()) {
            return;
        }

        Constant.setIsExit(false);
        //发送当前的状态为暂停的状态
        PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_EXIT);
        PlayerCommunicationManager.getInstance().sendPlayerUIStatus(PLAYER_UI_STATUS_RELEASE);

        //注释的原因，发现注释掉之后，也没有出现任何的问题，也忘记了之前为啥，需要在此处加主题了。
        // 【【4.4.2】首次打开UI不适配，歌单/专辑推荐词第二行只能显示一半】
        // 原因：推送里面需要用到这个主题，涉及text_h1的写法
        //https://www.tapd.cn/21711881/bugtrace/bugs/view?bug_id=1121711881001004109
//        int screenStyle = FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MUSIC_SCREEN_STYLE, 0);
//        if (screenStyle != 0) {
//            GlobalContext.get().setTheme(R.style.AppThemeShortHoushijing_fullScreen);
//        } else {
//            GlobalContext.get().setTheme(R.style.AppThemeHoushijing_Translucent);
//        }

        //先默认设置一个theme，准确的theme需要计算屏幕宽高
//        GlobalContext.get().setTheme(R.style.AppThemeCheji);

        LogUtil.d(TAG + "Music VersionInfo: " + BuildConfig.VERSION_NAME + " " + BuildConfig.CODE_VERSION + " " + BuildConfig.BUILD_TIME + " " + TXZVersion.BRANCH);
        AsrManager.getInstance().initCmd();//增长1~2M内存

//        configImageLoader();
//        DBManager.getInstance();
//        WindowManager manager = (WindowManager) getApp().getSystemService(
//                Context.WINDOW_SERVICE);
//        if (displayMetrics == null) {
//            displayMetrics = new DisplayMetrics();
//            manager.getDefaultDisplay().getMetrics(displayMetrics);
//        }

//        width = displayMetrics.widthPixels;
        // height = displayMetrics.heightPixels;
//        density = displayMetrics.density;

        if (BuildConfig.DEBUG) {
//            watcher = LeakCanary.install(getApp());
            // StrictMode.setThreadPolicy(new
            // StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            // StrictMode.setVmPolicy(new
            // StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        }
//
//
//        LogUtil.logd(TAG + "last exit play::" + SharedPreferencesUtils.getIsPlay() + ",set play::"
//                + SharedPreferencesUtils.getAppFirstPlay());
//        if (SharedPreferencesUtils.getIsPlay()
//                && SharedPreferencesUtils.getAppFirstPlay()) {
//            PlayEngineFactory.getEngine().play(Operation.auto);
//        } else {
//            LogUtil.logd(TAG + "set is play false");
//            SharedPreferencesUtils.setIsPlay(false);
//        }


//        MusicManager.getInstance();
        registerListener();

        InfoPopView.getInstance().addObserver();

        TimeManager.getInstance();

        // 音乐上报数据
//        ReportHelper.getInstance().sendReportData(Action.ACT_LOGIN);

        // 自动扫描sd卡中的数据
//        AppLogic.runOnUiGround(new Runnable() {
//            @Override
//            public void run() {
//                LocalAudioDataSource.getInstance().scanLocal(null);
//            }
//        }, 30 * 1000);


//        HeadSetHelper.getInstance().open(GlobalContext.get());

        SyncCoreData.syncCurStatusFullStyle();
        initTXZVersion();
        initData();
        resetMyData();

        if (BuildConfig.DEBUG) {
            setDebugInitData();
        }
    }

    /**
     * 设置只有在debug的环境下才会加载的数据
     */
    private void setDebugInitData() {
        ProcessMemoryMonitor.getInstance().addMonitorProcess(new ProcessMemoryMonitor.MonitorProcess("com.txznet.music", null));
    }

    /**
     * 重置数据
     */
    private void resetMyData() {
        //TTS播报空间不足的提示次数（tapd——1001192）
//        SharedPreferencesUtils.clearTTSSpeakNotEnoughSpaceIDs();
    }

    private void initTXZVersion() {
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
                                LogUtil.logw("get package VERSION failed: " + ServiceManager.TXZ + ", try to get from package manager");
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
//        AppLogic.runOnBackGround(new Runnable() {
//            @Override
//            public void run() {
//                HistoryData newestHistory = DBManager.getInstance().findNewestHistory();
//                if (newestHistory != null) {
//                    Logger.i(TAG, "newest history:" + newestHistory.toString());
//                    if (newestHistory.getType() == HistoryData.TYPE_AUDIO) {
//                        PlayHelper.playHistoryMusic(Operation.auto, false);
//                    } else {
//                        PlayHelper.playHistoryRadio(Operation.auto, false);
//                    }
//                }
//            }
//        });
        initThirdData();
    }

    /**
     * 初始化三方数据
     */
    private void initThirdData() {
        String sdkListenerPackageName = SharedPreferencesUtils.getSDKListenerPackageName();
        if (StringUtils.isNotEmpty(sdkListenerPackageName)) {
            ManufacturerInvoker.getInstance(sdkListenerPackageName).registerListener();
        }
    }

    private void registerListener() {
        //三方状态监控
        PlayerCommunicationManager.getInstance();
        //
        registerScreenListener();
//        registerShowListener();
//        registerSearchListener();
        if (BuildConfig.DEBUG) {
            registerQuickPlayerListener();
        }
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
                String packageName = "com.txznet.sdkdemo";

                String cmd = intent.getStringExtra("cmd");
                byte[] paramByte = null;

                String param = intent.getStringExtra("param");

                if (param != null) {
                    paramByte = param.getBytes();
                }

                ManufacturerInvoker.getInstance(packageName).invoke(packageName, cmd, paramByte);
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

    @Override
    public void onConfigChange(Bundle bundle) {
        super.onConfigChange(bundle);
        String type = bundle.getString("type");
        L.d("telenewbie::", "onConfigChange: type = " + type);

        if ("screen".equals(type)) {
            UIConfig newConfig = new UIConfig();
            newConfig.x = bundle.getInt("x", 0);
            newConfig.y = bundle.getInt("y", 0);
            newConfig.width = bundle.getInt("width", 0);
            newConfig.height = bundle.getInt("height", 0);
            newConfig.gravity = bundle.getInt("gravity", Gravity.LEFT | Gravity.TOP);
            L.i("telenewbie::", "onConfigChange: config change: " + newConfig.toString());
            mUIConfig = newConfig;
            ScreenUtils.updateScreenSize(newConfig.width, newConfig.height, true);

        } else if ("visible".equals(type)) {
            boolean visible = bundle.getBoolean("action", false);
            L.i("telenewbie::", "onConfigChange: visible change: " + visible);
//            if (visible) {
//                UIHandler.getInstance().launchUI();
//            } else {
//                UIHandler.getInstance().hideUI();
//            }

        }
    }

    public static UIConfig mUIConfig = null;
}

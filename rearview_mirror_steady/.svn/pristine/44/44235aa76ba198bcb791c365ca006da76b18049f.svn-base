package com.txznet.loader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.view.Gravity;

import com.bumptech.glide.Glide;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.txznet.audio.player.IMediaPlayer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.ScreenUtils;
import com.txznet.comm.version.TXZVersion;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.action.FavourActionCreator;
import com.txznet.music.action.LocalActionCreator;
import com.txznet.music.action.SubscribeActionCreator;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.LocalAudioDao;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.helper.AsrManager;
import com.txznet.music.helper.PlayerConfigHelper;
import com.txznet.music.helper.SyncCoreData;
import com.txznet.music.model.AiPushModel;
import com.txznet.music.model.AlbumModel;
import com.txznet.music.model.FavourModel;
import com.txznet.music.model.HistoryModel;
import com.txznet.music.model.HomePageModel;
import com.txznet.music.model.LocalAudioModel;
import com.txznet.music.model.LyricModel;
import com.txznet.music.model.NetworkCheckModel;
import com.txznet.music.model.PlayerErrorModel;
import com.txznet.music.model.PlayerModel;
import com.txznet.music.model.PlayerQueueModel;
import com.txznet.music.model.PowerModel;
import com.txznet.music.model.SearchModel;
import com.txznet.music.model.SettingModel;
import com.txznet.music.model.SubscribeModel;
import com.txznet.music.model.WxPushModel;
import com.txznet.music.receiver.MediaReceiver;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.SysExitEvent;
import com.txznet.music.store.ReportStore;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.Logger;
import com.txznet.music.util.ProgramUtils;
import com.txznet.music.util.TXZAppUtils;
import com.txznet.music.util.TimeManager;
import com.txznet.music.util.Utils;
import com.txznet.proxy.util.TimeUtils;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxFlux;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;


public class AppLogic extends AppLogicBase {
    public static final String TAG = Constant.LOG_TAG_APPLICATION;

    private static RefWatcher refWatcher;

    @Override
    public void onCreate() {
        Logger.d(TAG, "[life][" + this.hashCode() + "]::onCreate()");
        MultiDex.install(GlobalContext.get());
        Logger.d(TAG, "pid : " + android.os.Process.myPid() + ", sourceDir:" + GlobalContext.get().getApplicationInfo().sourceDir);
        super.onCreate();

        if (!isMainProcess()) {
            return;
        }

        new URLConnectionNoCache(null);

        if (!ProgramUtils.isProgram() && BuildConfig.DEBUG) {
            if (!LeakCanary.isInAnalyzerProcess(getApp())) {
                refWatcher = LeakCanary.install(getApp());
            }
            StrictMode.enableDefaults();
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, getClass().getSimpleName() + ", onCreate：" + getProcessName() + ":::" + GlobalContext.get().getApplicationInfo().packageName);
        }

        //同步给Core, 清空当前播放信息
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.inner.syncMusicList", null, null);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.isNewVersion", "true".getBytes(), null);
        SyncCoreData.syncCurMusicModel(null);
        SyncCoreData.syncCurPlayerStatus(false);
        SyncCoreData.sendStatusByCurrent(IMediaPlayer.STATE_ON_IDLE);

        RxJavaPlugins.setErrorHandler(throwable -> {
        });

        AppLogic.runOnUiGround(() -> {
            TimeUtils.hit("##### runOnUiGround");

            initComponents();    // 初始化组件
            // 同步后台配置项
            syncPlayConf();
            // 同步收藏和订阅内容
            syncSubscribeAndFavour();
            // 若本地列表没有音频，则自动扫描
            checkLocalAudio();

            TimeUtils.hit("##### runOnUiGround");
        });

        AppLogic.runOnBackGround(() -> {
            TimeUtils.hit("##### runOnBackGround");
            initUtils(); // 初始化工具
            AudioUtils.getConfig();

            JSONBuilder jsonBuilder = new JSONBuilder();
            jsonBuilder.put("pkgName", "com.txznet.music");
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.applets.set", jsonBuilder.toBytes(), null);

            JarURLMonitor.start(getApp());
            TimeUtils.hit("##### runOnBackGround");
        });

        Glide.get(GlobalContext.get());
    }

    public static RefWatcher getRefWatcher() {
        return refWatcher;
    }

    private void initTest() {
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_MEDIA_EJECT).build());
            }
        }, new IntentFilter("ACTION_MEDIA_EJECT"));
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_MEDIA_MOUNTED).build());
            }
        }, new IntentFilter("ACTION_MEDIA_MOUNTED"));
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_MEDIA_SCANNER_STARTED).build());
            }
        }, new IntentFilter("ACTION_MEDIA_SCANNER_STARTED"));
    }

    @Override
    public void reBindCore() {
        // FIXME: 2019/8/9 这个方法不会触发，转到MyService.soundInitSuccess里处理
        super.reBindCore();
        Logger.d(TAG, "[life][" + this.hashCode() + "]::reBindCore()");
        SyncCoreData.updateLocalMusic();
    }

    private void initComponents() {
        PlayerConfigHelper.get().initPlayer(); // 初始化播放器
        initModel(); // 初始化业务模块
        initReceiver(); // 初始化广播接收器
        AppLogic.runOnBackGround(() -> {
            AsrManager.getInstance().initCmd(); // 初始化命令词
        });
    }

    private void initUtils() {
        TimeManager.getInstance(); // 时间校对
        TXZAppUtils.initTXZVersion(); // 获取core版本
    }

    private void initModel() {
        RxFlux.initWorkflow(
                new HomePageModel(),
                new PlayerModel(),
                new PlayerErrorModel(),
                new PlayerQueueModel(),
                new AiPushModel(),
                new LocalAudioModel(),
                new HistoryModel(),
                new SearchModel(),
                new FavourModel(),
                new SubscribeModel(),
                new SettingModel(),
                new WxPushModel(),
                new AlbumModel(),
                new LyricModel(),
                new NetworkCheckModel(),
                new PowerModel()
        );
        // register report
        new ReportStore();
    }

    private void initReceiver() {
        MediaReceiver.register();
    }

    // 同步后台配置项
    private void syncPlayConf() {
        TXZMusicDataSource.get().getPlayConf()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> AudioUtils.getConfig())
                .subscribe();
    }

    // 同步订阅和收藏
    private void syncSubscribeAndFavour() {
        AppLogic.runOnBackGround(() -> {
            if (!SharedPreferencesUtils.hasSyncFavourData()) {
                FavourActionCreator.getInstance().getData(Operation.AUTO, null);
            }
            if (!SharedPreferencesUtils.hasSyncSubscribeData()) {
                SubscribeActionCreator.getInstance().getSubscribeData(Operation.AUTO, null);
            }
        });
    }

    // 检测本音乐
    private void checkLocalAudio() {
        Schedulers.io().scheduleDirect(() -> {
            LocalAudioDao localAudioDao = DBUtils.getDatabase(GlobalContext.get()).getLocalAudioDao();
            if (localAudioDao.getCount() == 0) {
                Logger.w(Constant.LOG_TAG_LOGIC, "checkLocalAudio empty, begin auto san");
                LocalActionCreator.get().scan(Operation.AUTO);
            }
        });
    }

    @Override
    public void destroy() {
//        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onTrimMemory(int i) {
        super.onTrimMemory(i);
        Glide.get(getApp()).trimMemory(i);
    }

    @Override
    public void onCloseApp() {
        ReportEvent.reportExit(SysExitEvent.EXIT_TYPE_VOICE);
        super.onCloseApp();
    }

    @Override
    public void onConfigChange(Bundle bundle) {
        super.onConfigChange(bundle);
        String type = bundle.getString("type");
        Logger.d(Constant.LOG_TAG_APPLICATION, "onConfigChange: type = " + type);
        if ("screen".equals(type)) {
            UIConfig newConfig = new UIConfig();
            newConfig.x = bundle.getInt("x", 0);
            newConfig.y = bundle.getInt("y", 0);
            newConfig.width = bundle.getInt("width", 0);
            newConfig.height = bundle.getInt("height", 0);
            newConfig.gravity = bundle.getInt("gravity", Gravity.LEFT | Gravity.TOP);
            Logger.d(Constant.LOG_TAG_APPLICATION, "onConfigChange: config change: " + newConfig.toString());
            mUIConfig = newConfig;
            ScreenUtils.updateScreenSize(newConfig.width, newConfig.height, true);
        } else if ("visible".equals(type)) {
            boolean visible = bundle.getBoolean("action", false);
            Logger.d(Constant.LOG_TAG_APPLICATION, "onConfigChange: visible change: " + visible);
            if (visible) {
                Utils.back2Home();
            } else {
                Utils.hideUi();
            }
        }
    }

    public static UIConfig mUIConfig = null;

    @Override
    public String getOverrideVersionString() {
        return "" + BuildConfig.BUILD_TIME + "_" + TXZVersion.COMPUTER + "_"
                + BuildConfig.CODE_VERSION;
    }

    public class URLConnectionNoCache extends URLConnection {

        protected URLConnectionNoCache(URL url) {
            super(url);
            setDefaultUseCaches(false);
        }

        public void connect() throws IOException {
        }
    }
}

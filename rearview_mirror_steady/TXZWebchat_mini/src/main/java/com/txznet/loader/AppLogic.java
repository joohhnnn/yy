package com.txznet.loader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.ScreenUtils;
import com.txznet.webchat.BuildConfig;
import com.txznet.webchat.Config;
import com.txznet.webchat.Constant;
import com.txznet.webchat.RecordStatusObservable;
import com.txznet.webchat.actions.MediaFocusActionCreator;
import com.txznet.webchat.actions.TXZBindActionCreator;
import com.txznet.webchat.helper.WxNetworkHelper;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.WxUIConfig;
import com.txznet.webchat.stores.TXZBindStore;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.stores.WxWindowConfigStore;
import com.txznet.webchat.ui.base.UIHandler;
import com.txznet.webchat.util.FileUtil;
import com.txznet.webchat.util.WxHelpGuideManager;

public class AppLogic extends AppLogicBase {
    private static final String LOG_TAG = "AppLogic";

    @Override
    public void onCreate() {
        super.onCreate();

        // 版本信息打印
        L.i(LOG_TAG, String.format("TXZWebchat start, version = %s(%s)_%s",
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, BuildConfig.SVN_VERSION));

        mRecordStatusObservable = new RecordStatusObservable(AppLogic.getApp());
        initAppDir();
        initModule();
        initHomeObserver();
        initUIReceiver();
        initVersionCheck();
        initConfig();
    }

    @Override
    public void caughtException() {

    }

    private void initModule() {
        WxConfigStore.getInstance();
        WxNetworkHelper.getInstance();

        TXZBindStore.get();


        UIHandler.getInstance();

        MediaFocusActionCreator.getInstance();
        TXZBindActionCreator.get().subscribeBindInfo();
    }

    /*
        初始化应用文件夹
     */
    private void initAppDir() {
        FileUtil.initFileDir(Constant.PATH_WECHAT_DIR_BASE, true);
        FileUtil.initFileDir(Constant.PATH_MSG_VOICE_CACHE, true);
        FileUtil.initFileDir(Constant.PATH_MSG_VOICE_CACHE_SELF, true);
        FileUtil.initFileDir(Constant.PATH_HEAD_CACHE, false);
    }

    private void initHomeObserver() {
        IntentFilter intentFilter = new IntentFilter(
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                String reason = intent.getStringExtra("reason");
                if ("homekey".equals(reason)) {

                }
            }
        }, intentFilter);
    }

    private void initUIReceiver() {
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                String target = intent.getStringExtra("target");

                if ("settings".equals(target)) {
                } else if ("help".equals(target)) {
                }
            }
        }, new IntentFilter("com.txznet.webchat.ui.navigate"));
    }

    private static RecordStatusObservable mRecordStatusObservable;

    public static boolean isRecordWinShowing() {
        if (mRecordStatusObservable != null) {
            return mRecordStatusObservable.isShowing();
        }
        return false;
    }

    public static void registerRecordStatusObserver(
            RecordStatusObservable.StatusObserver observer) {
        if (mRecordStatusObservable != null) {
            mRecordStatusObservable.registerObserver(observer);
        }
    }

    public static void unregisterRecordStatusObserver(
            RecordStatusObservable.StatusObserver observer) {
        if (mRecordStatusObservable != null) {
            try {
                mRecordStatusObservable.unregisterObserver(observer);
            } catch (IllegalStateException e) {

            }
        }
    }

    /*
        检测版本
     */
    private void initVersionCheck() {
        checkForTXZ();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addDataScheme("package");
        getApp().registerReceiver(new BroadcastReceiver() {
            private static final int PACKAGE_NAME_START_INDEX = 8;

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    return;
                }
                String data = intent.getDataString();
                if (data == null || data.length() <= PACKAGE_NAME_START_INDEX) {
                    return;
                }
                String packageName = data.substring(PACKAGE_NAME_START_INDEX);
                if (packageName.equals(ServiceManager.TXZ)) {
                    checkForTXZ();
                }
            }
        }, filter);
    }

    /**
     * 对配置文件中定义的部分配置项进行初始化
     */
    private void initConfig() {
        WxUIConfig configFromFile = WxConfigStore.getInstance().getUIConfig();

        if (null != configFromFile) {
            WxWindowConfigStore.getInstance().updateUIConfig(configFromFile);
        }
    }

    private void checkForTXZ() {
        try {
            PackageManager manager = GlobalContext.get().getPackageManager();
            PackageInfo info = manager.getPackageInfo(ServiceManager.TXZ, 0);
            if (info != null) {
                int version = info.versionCode;
                if (version >= 14) {
                    Config.SupportNewRecord = true;
                    L.d(LOG_TAG, "TXZRecorder:switch to online record mode");
                } else {
                    L.d(LOG_TAG, "TXZRecorder:use base record mode");
                }
            }
        } catch (Exception e) {
            L.d(LOG_TAG, "TXZRecorder:check TXZ version encountered error," +
                    " switch to online record mode");
            Config.SupportNewRecord = true;
        }
    }

    @Override
    public void destroy() {
        L.d(LOG_TAG, "app application onDestroy");
        super.destroy();
    }

    @Override
    public void onCloseApp() {

    }

    @Override
    public void onConfigChange(Bundle bundle) {
        super.onConfigChange(bundle);

        String type = bundle.getString("type");
        L.d(LOG_TAG, "onConfigChange: type = " + type);

        if ("screen".equals(type)) {
            WxUIConfig newConfig = new WxUIConfig();
            newConfig.x = bundle.getInt("x", 0);
            newConfig.y = bundle.getInt("y", 0);
            newConfig.width = bundle.getInt("width", 0);
            newConfig.height = bundle.getInt("height", 0);
            newConfig.gravity = bundle.getInt("gravity", Gravity.LEFT | Gravity.TOP);

            L.i(LOG_TAG, "onConfigChange: config change: " + newConfig.toString());
            ScreenUtils.updateScreenSize(newConfig.width, newConfig.height, true);
            //ConfigActionCreator.getInstance().changeUILayoutConfig(newConfig);
            WxWindowConfigStore.getInstance().updateUIConfig(newConfig);
        } else if ("visible".equals(type)) {
            boolean visible = bundle.getBoolean("action", false);
            L.i(LOG_TAG, "onConfigChange: visible change: " + visible);
            if (visible) {
                UIHandler.getInstance().launchUI();
            } else {
                UIHandler.getInstance().hideUI();
            }

        }
    }

    private static boolean bForeground = false;

    @Override
    public void onVisibilityChanged(boolean visible) {
        super.onVisibilityChanged(visible);

        bForeground = visible;

        if (bForeground) {
            WxHelpGuideManager.getInstance().showHelpGuide(WxHelpGuideManager.GuideScene.FOREGROUND,
                    null);
        } else {

        }
    }

    /**
     * 获取当前应用是否处于前台状态
     *
     * @return
     */
    public static boolean isForeground() {
        return bForeground;
    }
}

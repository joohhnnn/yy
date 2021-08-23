package com.txznet.webchat.stores;

import android.os.Environment;
import android.text.TextUtils;
import android.view.WindowManager;

import com.txznet.comm.remote.util.TextUtil;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.webchat.BuildConfig;
import com.txznet.webchat.actions.Action;
import com.txznet.webchat.actions.ActionType;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.WxUIConfig;
import com.txznet.webchat.sp.WxConfigSp;
import com.txznet.webchat.util.FileUtil;
import com.txznet.webchat.util.HttpUtil;
import com.txznet.webchat.util.UidUtil;

import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 微信设置Store
 * 负责管理配置文件中读取的相关设置
 * Created by J on 2017/5/24.
 */

public class WxConfigStore extends Store {
    public static final String EVENT_TYPE_ALL = "wx_config_store";
    public static WxConfigStore sInstance = new WxConfigStore(Dispatcher.get());

    private AtomicBoolean bFileConfigLoaded = new AtomicBoolean(false);

    // 已知的Key
    private static final String KEY_WX_UI_WECHAT_TAB_ENABLE = "WX_UI_WECHAT_TAB_ENABLE";
    private static final String KEY_WX_UI_CONTROL_TAB_ENABLE = "WX_UI_CONTROL_TAB_ENABLE";
    private static final String KEY_WX_UI_TAB_VISIBLE = "WX_UI_TAB_VISIBLE";
    private static final String KEY_WX_UI_MAIN_CONTROL_ENTRY_ENABLE = "WX_UI_MAIN_CONTROL_ENTRY_ENABLE";
    private static final String KEY_WX_UI_THEME = "WX_UI_THEME";
    private static final String KEY_WX_UI_BACK_BUTTON_VISIBLE = "WX_UI_BACK_BUTTON_VISIBLE";
    private static final String KEY_WX_UI_TRANSPARENT_BACKGROUND = "WX_UI_TRANSPARENT_BACKGROUND";
    private static final String KEY_WX_MSG_LOC_MSG_ENABLE = "WX_MSG_LOC_MSG_ENABLE";
    private static final String KEY_WX_MSG_LOC_SHARE_ENABLE = "WX_MSG_LOC_SHARE_ENABLE";
    private static final String KEY_WX_TTS_FORCE_BROAD_NAV_TIP = "WX_TTS_FORCE_BROAD_NAV_TIP";
    private static final String KEY_WX_MSG_FILE_ENABLE = "WX_MSG_FILE_ENABLE";
    private static final String KEY_WX_MSG_FILE_SIZE_THRESHOLD = "WX_MSG_FILE_SIZE_THRESHOLD";
    private static final String KEY_WX_MSG_SUPPORT_FILE_SUFFIX = "WX_MSG_FILE_SUPPORT_SUFFIX";
    private static final String KEY_WX_FILE_DOWNLOAD_PATH = "WX_MSG_FILE_DOWNLOAD_PATH";
    private static final String KEY_WX_UI_DIALOG_TYPE = "WX_UI_DIALOG_TYPE";
    private static final String KEY_WX_UI_DIALOG_FLAG = "WX_UI_DIALOG_FLAG";
    private static final String KEY_WX_UI_SYSTEM_DIALOG_TYPE = "WX_UI_SYSTEM_DIALOG_TYPE";
    private static final String KEY_WX_UI_SYSTEM_DIALOG_FLAG = "WX_UI_SYSTEM_DIALOG_FLAG";
    private static final String KEY_WX_UI_SYSTEM_DIALOG_OFFSET_X = "WX_UI_SYSTEM_DIALOG_OFFSET_X";
    private static final String KEY_WX_UI_SYSTEM_DIALOG_OFFSET_Y = "WX_UI_SYSTEM_DIALOG_OFFSET_Y";
    private static final String KEY_WX_UI_SCREEN_TYPE = "WX_UI_SCREEN_TYPE";
    private static final String KEY_WX_UI_SCREEN_FLAG = "WX_UI_SCREEN_FLAG";
    private static final String KEY_WX_UI_DPAD_SUPPORT = "WX_UI_DPAD_SUPPORT";
    private static final String KEY_WX_TTS_BEEP_VOLUME = "WX_TTS_BEEP_VOLUME";
    // 窗口化配置相关key
    private static final String KEY_WX_UI_WINDOW_X = "WX_UI_WINDOW_X";
    private static final String KEY_WX_UI_WINDOW_Y = "WX_UI_WINDOW_Y";
    private static final String KEY_WX_UI_WINDOW_WIDTH = "WX_UI_WINDOW_WIDTH";
    private static final String KEY_WX_UI_WINDOW_HEIGHT = "WX_UI_WINDOW_HEIGHT";
    private static final String KEY_WX_UI_WINDOW_GRAVITY = "WX_UI_WINDOW_GRAVITY";

    // 配置路径
    private static final String KEY_WX_SERVER_CONFIG_PATH = "WX_SERVER_CONFIG_PATH";

        private static final String WX_SERVER_CONFIG_PATH_DEFAULT = "http://wx.txzing.com/module/other/service/wxCloseDesc?tdsourcetag=s_pctim_aiomsg";
//    private static final String WX_SERVER_CONFIG_PATH_DEFAULT = "http://wxtest.txzing.com/module/other/service/wxCloseDesc?tdsourcetag=s_pctim_aiomsg";

    // 部分默认值
    private static final long FILE_SIZE_THRESHOLD_DEFAULT = 10 * 1024 * 1024;

    private String mWxUITheme = "mirror"; // 微信主题, 默认后视镜版本
    private Boolean bEnableWechatTab = true; // 扫码页面微信tab是否开启(本地配置)
    /*
     * 扫码页面微信tab是否开启(远程下发)
     * 微信tab开关存在后台下发的情况, 需要特殊处理
     * 只有后台下发和本地配置文件均配置打开的情况下, 才认为微信tab开启
     * */
    private boolean bEnableWechatTabRemote = true;
    private boolean bEnableControlTabRemote = true;
    private boolean bEnableControlTab = true; // 扫码页面远程控制tab是否开启
    private boolean bTabVisible = true; // 扫码页面tab可见性
    private boolean bEnableMainControlEntry = true; // 车镜版主界面右下角关注设备是否开启
    private boolean bEnableBackButton = true; // 是否显示界面中的返回按钮
    private boolean bEnableTransparentBackground = true; // 是否启用透明背景
    private boolean bEnableLocMsg = true; // 是否支持位置消息处理
    private boolean bEnableLocShare = true; // 是否支持位置分享
    private boolean bForceBroadNavTip = false; // 是否强制播报导航过去的语音提示
    private boolean bEnableFileMsg = false; // 是否支持文件消息
    private long mFileSizeThreshold = FILE_SIZE_THRESHOLD_DEFAULT; // 微信下载文件消息大小限制, 默认10M
    private String[] mSupportedFileMsgSuffix = {}; // 支持打开的文件消息格式
    private String mFileDownloadPath = Environment.getExternalStorageDirectory() + "/wechatfiles/";
    private int mDialogType = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT; // Dialog的默认WindowType
    private int mDialogFlag = 0; // Dialog默认添加的flag
    private int mSystemDialogType; // 系统级别的Dialog默认的WindowType
    private int mSystemDialogFlag; // 系统级别的Dialog默认添加的flag
    private int mSystemDialogOffsetX = 0; // 系统级别的Dialog Window偏移(X)
    private int mSystemDialogOffsetY = 0; // 系统级别的Dialog Window偏移(Y)
    private int mScreenType = WindowManager.LayoutParams.TYPE_APPLICATION;
    private int mScreenFlag = 0;
    private boolean bEnableDpadSupport = true;
    private float mBeepVolume = -1f;
    // 窗口化相关字段
    private int mWindowX = 0;
    private int mWindowY = 0;
    private int mWindowWidth = -1;
    private int mWindowHeight = -1;
    private int mWindowGravity = 51;

    // 配置相关
    private String mWxServerConfigPath = WX_SERVER_CONFIG_PATH_DEFAULT;

    private String mWxCloseDesc;

    public static WxConfigStore getInstance() {
        return sInstance;
    }

    private WxConfigStore(Dispatcher dispatcher) {
        super(dispatcher);

        loadConfigFromFile();
        loadRemoteConfigs();
    }

    private void loadConfigFromFile() {
        ArrayList<String> list = new ArrayList<>();
        list.add(KEY_WX_UI_WECHAT_TAB_ENABLE);
        list.add(KEY_WX_UI_CONTROL_TAB_ENABLE);
        list.add(KEY_WX_UI_TAB_VISIBLE);
        list.add(KEY_WX_UI_MAIN_CONTROL_ENTRY_ENABLE);
        list.add(KEY_WX_UI_THEME);
        list.add(KEY_WX_UI_BACK_BUTTON_VISIBLE);
        list.add(KEY_WX_UI_TRANSPARENT_BACKGROUND);
        list.add(KEY_WX_MSG_LOC_MSG_ENABLE);
        list.add(KEY_WX_MSG_LOC_SHARE_ENABLE);
        list.add(KEY_WX_TTS_FORCE_BROAD_NAV_TIP);
        list.add(KEY_WX_MSG_FILE_ENABLE);
        list.add(KEY_WX_MSG_FILE_SIZE_THRESHOLD);
        list.add(KEY_WX_MSG_SUPPORT_FILE_SUFFIX);
        list.add(KEY_WX_FILE_DOWNLOAD_PATH);
        list.add(KEY_WX_UI_DIALOG_TYPE);
        list.add(KEY_WX_UI_DIALOG_FLAG);
        list.add(KEY_WX_UI_SYSTEM_DIALOG_TYPE);
        list.add(KEY_WX_UI_SYSTEM_DIALOG_FLAG);
        list.add(KEY_WX_UI_SYSTEM_DIALOG_TYPE);
        list.add(KEY_WX_UI_SYSTEM_DIALOG_FLAG);
        list.add(KEY_WX_UI_SYSTEM_DIALOG_OFFSET_X);
        list.add(KEY_WX_UI_SYSTEM_DIALOG_OFFSET_Y);
        list.add(KEY_WX_UI_SCREEN_TYPE);
        list.add(KEY_WX_UI_SCREEN_FLAG);
        list.add(KEY_WX_UI_DPAD_SUPPORT);
        list.add(KEY_WX_TTS_BEEP_VOLUME);
        // 窗口化相关字段
        list.add(KEY_WX_UI_WINDOW_X);
        list.add(KEY_WX_UI_WINDOW_Y);
        list.add(KEY_WX_UI_WINDOW_WIDTH);
        list.add(KEY_WX_UI_WINDOW_HEIGHT);
        list.add(KEY_WX_UI_WINDOW_GRAVITY);
        // 配置相关
        list.add(KEY_WX_SERVER_CONFIG_PATH);

        HashMap<String, String> mapConfig = TXZFileConfigUtil.getConfig(list);
        if (null == mapConfig) {
            L.i("WxConfigStore", "config map is null");
            return;
        }

        bEnableWechatTab = readBoolean(KEY_WX_UI_WECHAT_TAB_ENABLE, true, mapConfig);
        bEnableControlTab = readBoolean(KEY_WX_UI_CONTROL_TAB_ENABLE, !BuildConfig.FORCE_WECHAT_MODE, mapConfig);
        bTabVisible = readBoolean(KEY_WX_UI_TAB_VISIBLE, true, mapConfig);
        bEnableMainControlEntry = readBoolean(KEY_WX_UI_MAIN_CONTROL_ENTRY_ENABLE, BuildConfig.SHOW_DEVICE_ITEM, mapConfig);
        mWxUITheme = readString(KEY_WX_UI_THEME, BuildConfig.THEME, mapConfig);
        bEnableBackButton = readBoolean(KEY_WX_UI_BACK_BUTTON_VISIBLE, true, mapConfig);
        bEnableTransparentBackground = readBoolean(KEY_WX_UI_TRANSPARENT_BACKGROUND, true, mapConfig);
        bForceBroadNavTip = readBoolean(KEY_WX_TTS_FORCE_BROAD_NAV_TIP, false, mapConfig);
        bEnableLocMsg = readBoolean(KEY_WX_MSG_LOC_MSG_ENABLE, true, mapConfig);
        bEnableLocShare = readBoolean(KEY_WX_MSG_LOC_SHARE_ENABLE, true, mapConfig);
        bEnableFileMsg = readBoolean(KEY_WX_MSG_FILE_ENABLE, false, mapConfig);
        mFileSizeThreshold = readLong(KEY_WX_MSG_FILE_SIZE_THRESHOLD, FILE_SIZE_THRESHOLD_DEFAULT, mapConfig);
        mSupportedFileMsgSuffix = readStringArr(KEY_WX_MSG_SUPPORT_FILE_SUFFIX, mapConfig);
        mFileDownloadPath = readString(KEY_WX_FILE_DOWNLOAD_PATH, Environment.getExternalStorageDirectory() + "/wechatfiles/", mapConfig);
        mDialogType = readInt(KEY_WX_UI_DIALOG_TYPE, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, mapConfig);
        mDialogFlag = readInt(KEY_WX_UI_DIALOG_FLAG, 0, mapConfig);
        mSystemDialogType = readInt(KEY_WX_UI_SYSTEM_DIALOG_TYPE, mDialogType, mapConfig);
        mSystemDialogFlag = readInt(KEY_WX_UI_SYSTEM_DIALOG_FLAG, mDialogFlag, mapConfig);
        mSystemDialogOffsetX = readInt(KEY_WX_UI_SYSTEM_DIALOG_OFFSET_X, 0, mapConfig);
        mSystemDialogOffsetY = readInt(KEY_WX_UI_SYSTEM_DIALOG_OFFSET_Y, 0, mapConfig);
        mScreenType = readInt(KEY_WX_UI_SCREEN_TYPE, WindowManager.LayoutParams.TYPE_APPLICATION, mapConfig);
        mScreenFlag = readInt(KEY_WX_UI_SCREEN_FLAG, 0, mapConfig);
        bEnableDpadSupport = readBoolean(KEY_WX_UI_DPAD_SUPPORT, true, mapConfig);
        mBeepVolume = readFloat(KEY_WX_TTS_BEEP_VOLUME, -1f, mapConfig);
        // 窗口化相关字段
        mWindowWidth = readInt(KEY_WX_UI_WINDOW_WIDTH, -1, mapConfig);
        mWindowHeight = readInt(KEY_WX_UI_WINDOW_HEIGHT, -1, mapConfig);
        mWindowX = readInt(KEY_WX_UI_WINDOW_X, 0, mapConfig);
        mWindowY = readInt(KEY_WX_UI_WINDOW_Y, 0, mapConfig);
        mWindowGravity = readInt(KEY_WX_UI_WINDOW_GRAVITY, 51, mapConfig);
        // 配置相关
        mWxServerConfigPath = readString(KEY_WX_SERVER_CONFIG_PATH, WX_SERVER_CONFIG_PATH_DEFAULT, mapConfig);

        // 若开启了文件消息功能, 尝试初始化文件下载路径
        if (bEnableFileMsg) {
            FileUtil.initFileDir(mFileDownloadPath, false);
        }

        bFileConfigLoaded.getAndSet(true);
    }

    /**
     * 加载配置文件配置项未加载完成时缓存下来的远程配置
     */
    private void loadRemoteConfigs() {
        for (String key : mRemoteSettingsCache.keySet()) {
            updateRemoteSetting(key, mRemoteSettingsCache.get(key));
        }

        emitChange(EVENT_TYPE_ALL);
    }

    public String getUITheme() {
        return mWxUITheme;
    }

    public boolean isMainControlEntryEnabled() {
        return bEnableMainControlEntry;
    }

    public boolean isWxTabEnabled() {
        // FIXME: 2019/5/9 无视后台配置，强制打开车载微信，用于临时编译。
        if (BuildConfig.FORCE_WECHAT_MODE) {
            return true;
        }
        return bEnableWechatTab && bEnableWechatTabRemote;
    }

    public boolean isControlTabEnabled() {
        return bEnableControlTab && bEnableControlTabRemote;
    }

    public boolean isTabVisible() {
        return bTabVisible;
    }

    public boolean isTransparentBackgroundEnabled() {
        return bEnableTransparentBackground;
    }

    public boolean getLocMsgEnabled() {
        return bEnableLocMsg;
    }

    public boolean getLocShareEnabled() {
        return bEnableLocShare;
    }

    public boolean forceBroadNavTip() {
        return bForceBroadNavTip;
    }

    public boolean isFileMsgEnabled() {
        return bEnableFileMsg;
    }

    public String getFileDownloadPath() {
        return mFileDownloadPath;
    }

    public boolean isFileSizeSupported(long size) {
        return size <= mFileSizeThreshold && size > 0;
    }

    public boolean isFileSuffixSupported(String suffix) {
        for (String sfx : mSupportedFileMsgSuffix) {
            if (sfx.equalsIgnoreCase(suffix)) {
                return true;
            }
        }

        return false;
    }

    public boolean isBackButtonEnabled() {
        return bEnableBackButton;
    }

    public int getDialogWindowType() {
        return mDialogType;
    }

    public int getDialogWindowFlag() {
        return mDialogFlag;
    }

    public int getSystemDialogWindowType() {
        return mSystemDialogType;
    }

    public int getSystemDialogWindowFlag() {
        return mSystemDialogFlag;
    }

    public int getSystemDialogOffsetX() {
        return mSystemDialogOffsetX;
    }

    public int getSystemDialogOffsetY() {
        return mSystemDialogOffsetY;
    }

    public int getScreenWindowType() {
        return mScreenType;
    }

    public int getScreenWindowFlag() {
        return mScreenFlag;
    }

    public boolean getDpadSupportEnabled() {
        return bEnableDpadSupport;
    }

    public float getBeepVolume() {
        return mBeepVolume;
    }

    public WxUIConfig getUIConfig() {
        if (mWindowWidth < 0 || mWindowHeight < 0) {
            return null;
        }

        WxUIConfig config = new WxUIConfig();
        config.x = mWindowX;
        config.y = mWindowY;
        config.width = mWindowWidth;
        config.height = mWindowHeight;
        config.gravity = mWindowGravity;

        return config;
    }

    public String getWxServerConfigPath() {
        return mWxServerConfigPath;
    }

    @Override
    public void onDispatch(Action action) {
        switch (action.getType()) {
            case ActionType.WX_LOC_MSG_PROC_ENABLE:
                updateRemoteSetting(KEY_WX_MSG_LOC_MSG_ENABLE, true);
                break;

            case ActionType.WX_LOC_MSG_PROC_DISABLE:
                updateRemoteSetting(KEY_WX_MSG_LOC_MSG_ENABLE, false);
                break;

            case ActionType.WX_LOC_SHARE_ENABLE:
                updateRemoteSetting(KEY_WX_MSG_LOC_SHARE_ENABLE, true);
                break;

            case ActionType.WX_LOC_SHARE_DISABLE:
                updateRemoteSetting(KEY_WX_MSG_LOC_SHARE_ENABLE, false);
                break;

            case ActionType.WX_ENTRY_ENABLE:
                updateRemoteSetting(KEY_WX_UI_WECHAT_TAB_ENABLE, true);
                break;

            case ActionType.WX_ENTRY_DISABLE:
                updateRemoteSetting(KEY_WX_UI_WECHAT_TAB_ENABLE, false);
                break;
            case ActionType.WX_CONTROL_ENABLE:
                updateRemoteSetting(KEY_WX_UI_CONTROL_TAB_ENABLE, true);
                break;
            case ActionType.WX_CONTROL_DISABLE:
                updateRemoteSetting(KEY_WX_UI_CONTROL_TAB_ENABLE, false);
                break;
            case ActionType.WX_SERVER_CONFIG_REQ:
                invokeServerConfigReq();
                break;
        }
    }

    private void invokeServerConfigReq() {
        UidUtil.getInstance().getTXZUID(new UidUtil.UidCallback() {
            @Override
            public void onSuccess(final String uid) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            L.d("WxConfigStore", "req server conf, url=" + WxConfigStore.getInstance().getWxServerConfigPath() + ", uid=" + uid);
                            String serverConf = HttpUtil.sendPost(WxConfigStore.getInstance().getWxServerConfigPath() + "&tab=1&uid=" + uid, null);
                            if (serverConf != null) {
                                serverConf = serverConf.replaceAll("/n", "");
                                serverConf = serverConf.replaceAll("\n", "");
                                serverConf = serverConf.replaceAll("\r\n", "");
                                L.d("WxConfigStore", "get server conf=" + serverConf);
                                JSONObject jObj = new JSONObject(serverConf);
                                String wx_close_desc = jObj.getString("wx_close_desc");
                                if (!TextUtils.isEmpty(wx_close_desc)) {
                                    WxConfigSp.getInstance().setWxCloseDesc(URLDecoder.decode(wx_close_desc));
                                }
                            }
                        } catch (Exception e) {
                            L.d("WxConfigStore", "get server conf wrong, msg=" + e + ":" + e.getMessage());
                        }
                    }
                }.start();
            }
        });
    }

    private HashMap<String, Object> mRemoteSettingsCache = new HashMap<>();

    private void updateRemoteSetting(String key, Object value) {
        /*
         * 为保证远程配置(通过sdk接口等途径设置的配置)的优先级, 在文件配置项加载完成前的远程配置修改暂时先缓存,
         * 待文件配置项加载完毕后再进行响应
         * */
        if (!bFileConfigLoaded.get()) {
            mRemoteSettingsCache.put(key, value);
            return;
        }

        switch (key) {
            case KEY_WX_MSG_LOC_MSG_ENABLE:
                bEnableLocMsg = (boolean) value;
                break;

            case KEY_WX_MSG_LOC_SHARE_ENABLE:
                bEnableLocShare = (boolean) value;
                WxConfigSp.getInstance().setWxEntryEnabled(true);
                break;

            case KEY_WX_UI_WECHAT_TAB_ENABLE:
                bEnableWechatTabRemote = (boolean) value;
                WxConfigSp.getInstance().setWxEntryEnabled(bEnableWechatTabRemote);
                break;
            case KEY_WX_UI_CONTROL_TAB_ENABLE:
                bEnableControlTabRemote = (boolean) value;
                WxConfigSp.getInstance().setWxControlEnabled(bEnableControlTabRemote);
                break;
        }

        emitChange(EVENT_TYPE_ALL);
    }

    private String readString(String key, String defValue, HashMap<String, String> configMap) {
        if (configMap.containsKey(key)) {
            return configMap.get(key);
        }

        return defValue;
    }

    private boolean readBoolean(String key, Boolean defValue, HashMap<String, String> configMap) {
        if (configMap.containsKey(key)) {
            return Boolean.valueOf(configMap.get(key));
        }

        return defValue;
    }

    private int readInt(String key, Integer defValue, HashMap<String, String> configMap) {
        if (configMap.containsKey(key)) {
            return Integer.valueOf(configMap.get(key));
        }

        return defValue;
    }

    private long readLong(String key, Long defValue, HashMap<String, String> configMap) {
        if (configMap.containsKey(key)) {
            return Long.valueOf(configMap.get(key));
        }

        return defValue;
    }

    private float readFloat(String key, Float defValue, HashMap<String, String> configMap) {
        if (configMap.containsKey(key)) {
            return Float.valueOf(configMap.get(key));
        }

        return defValue;
    }

    private String[] readStringArr(String key, HashMap<String, String> configMap) {
        if (configMap.containsKey(key)) {
            String[] ret;
            String rawString = configMap.get(key);

            if (rawString.length() <= 2) {
                return new String[]{};
            }

            ret = rawString.substring(1, rawString.length() - 1).split(",");

            // 去除可能被写入配置文件的空格
            for (int i = 0; i < ret.length; i++) {
                ret[i] = ret[i].trim();
            }

            return ret;
        }

        return new String[]{};
    }

}

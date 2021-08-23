package com.txznet.txz.util;

import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.util.FilePathConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author Terry
 */
public class TXZFileConfigUtil {

    private TXZFileConfigUtil() {
    }

    private static final String TAG = TXZFileConfigUtil.class.getSimpleName();

    //	//////////////////////安全配置//////////////////////////
    public static final String KEY_INVOKE_SECURITY_WHITE_LIST = "invokeWhiteList"; //AIDL远程调用白名单，配置后只允许里面包含的
    public static final String KEY_SHOW_MSG_WHEN_RECORD_ERROR = "showMsgWhenRecordError"; // 录音出错时，是否弹出提示(toast)

    //	//////////////////////一些通用配置//////////////////////////
    public static final String KEY_SCREEN_LOCK = "enableScreenLock"; // 是否使用ScreenLock
    public static final String KEY_SIM_WEB_ACTIVITY = "enableSimWebActivity"; // 是否使用SimWebActivity
    public static final String KEY_SIM_EMPTY_CHECK_DELAY_SEC = "checkEmptySimDelay"; // 检查是否无SIM卡的延迟，单位秒
    public static final String KEY_APP_REMOTE_INSTALL = "enableAppRemoteInstall"; // 是否启用App远程下发安装
    public static final String KEY_NAV_SEARCHING_TIP_DELAY = "navSearchingTipDelay"; // POI搜索中提示语延迟时间，默认1800ms
    public static final String KEY_TTS_DOWN_VOLUME_RATE = "ttsDownVolumeRate"; //TTS减半播报时，降低音量的程度0-1.0f
	public static final String KEY_DISABLE_WAKEUP_START_BEEP_PLAY = "disableWakeupStartBeepPlay"; // 唤醒起识别时不提示beep音，boolen
	public static final String KEY_ENABLE_WAKEUP_WHEN_RECOGONIZING = "enableWakeupWhenRecogonizing"; // 识别转圈中也可以开启唤醒，boolen
	public static final String KEY_ENABLE_HIDE_FLOAT_ON_RECORD = "enableHideFloatOnRecord"; // 是否在语音界面起来时隐藏语音图标，默认隐藏，boolen
    public static final String KEY_IS_HALF_VIEW_ON_BOTTOM = "isHalfViewOnBottom"; // 半屏模式的语音界面，是否展示在底部，默认底部，boolen
    public static final String KEY_IS_VERTICAL_FULL_VIEW_ON_BOTTOM = "isVerticalFullViewOnBottom"; // 竖屏全屏模式的语音界面语音动画部分，是否展示在底部，默认底部，boolen
	public static final String KEY_ENABLE_SCAN_MEDIA_LIBRARY = "enableScanMediaLibrary"; // 是否关闭扫描系统媒体库功能
    public static final String KEY_ENABLE_FOREGROUND_SERVICE = "enableForegroundService";// 是否启用前台服务

    // ////////////////////// 界面相关设置项 ///////////////////////
    public static final String KEY_WIN_TYPE = "winType"; // 悬浮窗层级
    public static final String KEY_WIN_FLAGS = "winFlags"; // 悬浮窗flags
	public static final String KEY_WIN_SOFT = "winSoft"; // 与软键盘的交互模式
    public static final String KEY_BACK_WIN_HELP = "backWinHelp";//是否返回帮助界面
    public static final String KEY_IF_SET_WINDOW_BG = "winSetWindowBg"; // 是否将背景图设置为整个window的背景图
    public static final String KEY_WINDOW_CONTENT_WIDTH = "winContentWidth"; // 内容显示最大宽度
    public static final String KEY_SCREEN_WIDTH_DP = "screenWidthDp"; // 屏幕宽度dp , 影响最终使用的 values-w100xxxdp-h100xxxdp
    public static final String KEY_SCREEN_HEIGHT_DP = "screenHeightDp"; // 屏幕高度dp ，  影响最终使用的 values-w100xxxdp-h100xxxdp
    public static final String KEY_SCREEN_WIDTH = "screenWidth"; // 可用屏幕宽度 ， 影响横竖屏判断
    public static final String KEY_SCREEN_HEIGHT = "screenHeight"; // 可用屏幕高度 ， 影响横竖屏判断
    public static final String KEY_SCREEN_PADDING_LEFT = "paddingLeft";//显示区域距离左边距，需和可用屏幕宽高一起使用
    public static final String KEY_SCREEN_PADDING_TOP = "paddingTop";//显示区域距离上边距，需和可用屏幕宽高一起使用
    public static final String KEY_SCREEN_PADDING_RIGHT = "paddingRight";//显示区域距离右边距，需和可用屏幕宽高一起使用
    public static final String KEY_SCREEN_PADDING_BOTTOM = "paddingBottom";//显示区域距离下边距，需和可用屏幕宽高一起使用
    public static final String KEY_FLOAT_VIEW_WIN_TYPE = "floatViewWinType"; // 悬浮图标层级
    public static final String KEY_ENABLE_FLOAT_VIEW_TYPE = "enableFloatView"; // 是否显示悬浮图标
    public static final String KEY_SYSTEM_UI_VISIBILITY = "systemUiVisibility"; // 设置是否显示状态栏
    public static final String KEY_ENABLE_LIST_ANIM = "enableListAnim"; // 是否开启列表动画
    public static final String KEY_ENABLE_CHAT_ANIM = "enableChatAnim"; // 是否开启聊天信息进入动画
    public static final String KEY_HELP_FLOAT_REFRESH_DELAY = "helpFloatRefreshDelay";// 设置重绘帮助浮窗的延时时间，解决切换模式发生移动，设备相关，默认20ms
	public static final String KEY_SUPPORT_MORE_RECORD_STATE = "supportMoreRecordState";//是否支持显示更多的声控状态
    public static final String KEY_CAN_SHOW_FEEDBACK_QRCODE = "canShowFeedbackQrCode"; //我要吐槽是否展示二维码，默认显示，true
    public static final String KEY_ENABLE_FLOAT_VIEW_TOUCH_RECT = "enableFloatViewTouchRect"; //点击声控图标是否需要判断在点击图标上，默认显示，true


    public static final String KEY_UI3_BAND_HALF_MODEL = "ui3BanHalfModel";
    public static final String KEY_UI3_BAND_NONE_MODEL = "ui3BanNoneModel";

    // ////////////////////////金手指相关设置项 //////////////////////
    public static final String KEY_HELP_FLOAT_WAKEUP = "helpFloatWakeup";//是否支持唤醒和免唤醒打开金手指
    public static final String KEY_HELP_FLOAT_DETAIL_WIDTH = "helpFloatDetailWidth";//设置展示界面的宽度
    public static final String KEY_HELP_FLOAT_DETAIL_HEIGHT = "helpFloatDetailHeight";//设置展示界面的高度
    public static final String KEY_HELP_TEXT_SIZE = "helpTextSize";//声控界面的帮助提示字体大小
    public static final String KEY_IS_SHOW_HELP_TIPS = "isShowHelpTips";//是否显示帮助指令

    public static final String KEY_CHECK_HANDLER_THREAD_DELAY = "CheckhandlerThreadDelay";//checkThread延迟时间
    public static final String KEY_CLOSE_MTJ_MODULE = "closemtjmodule";//关闭MtjModule模块
    public static final String KEY_CLOSE_ZH_CONVERTER = "closeZHConverter";//关闭繁简体转化

    public static final String KEY_WIN_FIT_SCREEN_CHANGE = "winFitScreenChange"; // 是否动态监测屏幕大小变化改变现在分辨率
    public static final String KEY_PATH_SKIN_APK = "pathSkinApk"; // 皮肤包路径（指的是微信/同听/语音通用的那个资源替换APK）
    public static final String KEY_TEXT_SIZE_RATIO = "textSizeRatio"; // 字体大小系数

    public static final String KEY_SHOW_HELP_TIPS = "showHelpTips";//设置显示声控界面的帮助

    public static final String KEY_KEYBOARD_FULL_SCREEN = "keyboardFullScreen"; // 是否把键盘设置为全屏，即默认显示，默认值false，半屏显示

    // ////////////////////// 方控相关 ///////////////////////////
    public static final String KEY_NAV_CONTROL_MODE = "navControlMode"; // 方控按键模式

	// ////////////////////////GPS相关设置项 //////////////////////
	public static String KEY_GPS_INTERVAL_SPEED = "gpsIntervalSpeed"; //GPS返回时间间隔
	public static String KEY_GPS_NOTIFY_INTERVAL = "gpsNotifyInterval"; //GPS位置更新通知时间间隔

    //////////////////////离线二维码相关 //////////////////////////
    public static String KEY_OFFICIAL_OFFLINE_QR_CODE = "keyOfficialQrCode"; // 是否采用离线正式网二维码，默认为true

    // ////////////////////////同听相关设置项 //////////////////////
    public static final String KEY_MUSIC_SCREEN_TYPE = "screenType"; // 屏幕类型,1车机,2车镜,3短屏后视镜,4车机竖屏，5手机竖屏
    public static final String KEY_MUSIC_SCREEN_ALBUM_COL_NUM = "verticalColCount"; // 竖屏界面的时候，一行显示的列数
    // TODO: 2018/6/13 同听的主题，默认为啥？全屏or非 透明or非？使用艾米进行测试一波
    public static final String KEY_MUSIC_SCREEN_STYLE = "screenStyle"; // 主题.默认为0透明主题,1,不透明 2 不透明+全屏
    public static final String KEY_MUSIC_INSAMPLESIZE = "bg_inSampleSize"; // 背景图片压缩的比例
    public static final String KEY_MUSIC_MAX_CACHE_SIZE = "maxCacheSize"; //最大可缓存文件的大小,默认500M,单位为M
    public static final String KEY_MUSIC_REVERSING_PLAY = "reversingPlay"; //倒车过程中是否播放
    public static final String KEY_MUSIC_KEYCODE_NEXT = "keyNext"; //
    public static final String KEY_MUSIC_KEYCODE_PREV = "keyPrev"; //
    public static final String KEY_MUSIC_LOSS_TRANSIENT_M = "focusLossTransientMusic"; //0(表示不处理),1(表示降低音量,默认),2(表示暂停)
    public static final String KEY_MUSIC_LOSS_TRANSIENT_R = "focusLossTransientRadio"; //0(表示不处理),1(表示降低音量),2(表示暂停,默认)
    public static final String KEY_MUSIC_LOSS_TRANSIENT_FACTOR = "focusLossTransientFactor"; //[0.0---1.0]
    public static final String KEY_MUSIC_LOSS = "focusLoss"; //0(表示不处理),1(默认,表示停止,释放音频焦点),2(表示暂停,不释放音频焦点)
    public static final String KEY_MUSIC_FOCUS_CAN_REQUEST = "focusCanRequest";//可以重新申请焦点的INT,100(默认,系统一般不会返回),适用场景,申请焦点时,申请不到,之后可以申请时,接收到回调(值),则重新申请音频焦点
    public static final String KEY_MEDIA_BUTTON = "keyMediaButton"; //1(m默认,采用mediasession的方式),2(采用广播的方式)
    public static final String KEY_MUSIC_SEARCH_TIPS_DELAY = "searchTipsDelay"; //“正在为您搜索”提示语的延时时间，如果结果提前回来则不播报，默认为0,单位ms
    public static final String KEY_MUSIC_UI_WINDOW_X = "MUSIC_UI_WINDOW_X";
    public static final String KEY_MUSIC_UI_WINDOW_Y = "MUSIC_UI_WINDOW_Y";
    public static final String KEY_MUSIC_UI_WINDOW_WIDTH = "MUSIC_UI_WINDOW_WIDTH";
    public static final String KEY_MUSIC_UI_WINDOW_HEIGHT = "MUSIC_UI_WINDOW_HEIGHT";
    public static final String KEY_MUSIC_UI_WINDOW_GRAVITY = "MUSIC_UI_WINDOW_GRAVITY";
	public static final String KEY_MUSIC_RESUME_PLAY_AFTER_WAKEUP = "resumePlayAfterWakeup"; // 设备唤醒起来后是否恢复播放

    // //////////////////////// WinDialog样式相关设置项 //////////////////////
    public static final String KEY_WIN_DIALOG_STYLE_WIN_TYPE = "winDialog.style.winType"; // int
    public static final String KEY_WIN_DIALOG_STYLE_IS_FULL = "winDialog.style.isFull"; // boolean
    public static final String KEY_WIN_DIALOG_STYLE_IS_SYSTEM = "winDialog.style.isSystem"; // boolean
    public static final String KEY_WIN_DIALOG_STYLE_CANCELABLE = "winDialog.style.cancelable"; // boolean
    public static final String KEY_WIN_DIALOG_STYLE_CANCEL_OUTSIDE = "winDialog.style.cancelOutSide"; // boolean
    public static final String KEY_WIN_DIALOG_STYLE_CANCEL_SCREEN_LOCK_TIME = "winDialog.style.cancelScreenLockTime"; // int
    public static final String KEY_WIN_DIALOG_STYLE_STOP_COUNT_DOWN_WHEN_LOSE_FOCUS = "winDialog.style.stopCountDownWhenLoseFocus"; // boolean
    public static final String KEY_WIN_DIALOG_STYLE_PREEMPT_TYPE = "winDialog.style.preemptType"; // string

    // 联系人选择是否保留老功能
    public static final String KEY_CONTACTS_HOLD_ORIGINAL = "bHoldOriginal";

    // //////////////////////// ASR相关 //////////////////////
    /**
     * 用户说话结束，识别到结果，等待语义结果返回超时，单位ms，最小值为5000，最大值为20000，超过范围无效
     */
    public static final String KEY_ASR_SEMANTIC_TIMEOUT = "asrSemanticTimeout";
    /**
     * 识别录音结束，等待语义结果返回，等待时间过长时进行提示。bool值，默认为true
     */
    public static final String KEY_ASR_SEMANTIC_HINT_ENABLE = "asrSemanticHintEnable";

    /**
     * 声瀚poi模型放置路径
     */
    public static final String KEY_ASR_UVOICE_POI_DECODER_PATH = "uvoiceDecoderPath";
    public static final String KEY_ASR_UVOICE_POI_RESCORE_PATH = "uvoiceRescorePath";
    /**
     * 后端静音超时，单位ms。默认1000ms
     */
    public static final String KEY_ASR_UVOICE_VAD_SIL_TIMEOUT = "uvoiceSilTimeout";
    /**
     * vad阈值，默认0.7
     */
    public static final String KEY_ASR_UVOICE_VAD_THRESHOLD = "uvoiceVadThreshold";
    /**
     * 声瀚离线识别阈值，默认9000
     */
    public static final String KEY_ASR_UVOICE_SEMANTICS_THRESHOLD = "uvoiceAsrThreshold";
    /**
     * 激活文件备份路径（文件夹路径）
     */
    public static final String KEY_ASR_UVOICE_ACTIVATE_BACKUP_DIR = "uvoiceActivateBackup";


    // //////////////////////// TTS相关 //////////////////////
    // TTS 目前的测试值针对云知声有效
    /**
     * AudioTrack缓冲区大小
     */
    public static final String KEY_TTS_AUDIO_BUFFER_TIME = "ttsAudioBufferTime";
    /**
     * TTS前端静音
     * 最大值为1000ms,默认0ms
     * 设置这个仅仅对合成的tts有效， 如果使用的是主题包中的录音，那就不会有前端静音
     */
    public static final String KEY_TTS_FRONT_BUFFER_TIME = "ttsFrontBufferTime";
    /**
     * TTS后端静音
     * 最大值为1000ms, 默认100ms
     */
    public static final String KEY_TTS_BACK_BUFFER_TIME = "ttsBackBufferTime";
    /**
     * 防止tts播报导致的误唤醒，根据tts播报内容去判断是否阻断误唤醒，bool值
     */
    public static final String KEY_TTS_NEED_INTERCEPT_WAKEUP = "ttsNeedInterceptWakeup";
    /**
     * 是否允许使用TTS切换主题指令，bool值
     */
    public static final String KEY_TTS_ENABLE_CHANGE_THEME = "enableChangeTtsTheme";
    /**
     * 是否强制开启外放TTS主题包选择列表，bool值
     */
    public static final String KEY_TTS_FORCE_SHOW_CHOICE_VIEW = "ttsForceShowChoiceView";
	/**增加tts主题包预置目录*/
	public static final String KEY_TTS_THEME_CUSTOM_PATH = "ttsThemeForCustomPath";
	/**
     * 增加百度TTS主题包上播报结束延时回调，目前该配置项只对ID为10005的V4,10006的V3生效，更新新的主题包的时候记得更新这条注释
     */
    public static final String KEY_TTS_SPEAK_FINISH_DELAY = "ttsSpeakFinishDelay";

    /**
     * 自动收敛误打断算法的容错区间，默认300，小于等于 0 表示不使用该算法
     */
    public static final String KEY_WAKEUP_STAMP_CACHE_TOLERANCE_TIME = "wakeupStampToleranceTime";
    /**
     * 在某些设备上唤醒速度较慢时，无法完全阻断误唤醒情况，需要增加阻断唤醒时间区间，int值
     */
    public static final String KEY_WAKEUP_INTERCEPT_DELAY_TIME = "wakeupInterceptDelayTime";



    /**GPS相关上报，bool值，默认为false 不上报*/
	public static final String KEY_REPORT_GPS_CFG = "reportGPSConfig";
    /***指定离线激活检验文件的存放路径, 没指定的话, 默认指向/etc/txz/lv_activate.db**/
    public static final String KEY_ACTIVATE_LOCAL_VERIFICATE_FILE_PATH = "activateLocalVerificateFilePath";



	//////////////////////// POI相关 //////////////////////
	/**
	 * 是否强制关闭沿途搜索，默认为false，即默认为沿途搜索
	 */
	public static final String KEY_FORCE_CLOSE_ONWAY_SEARCH = "keyForceCloseOnwaySearch";

    /**
     * 是否响应高德 手机端发来位置信息的操作,KEY_GAODE_MAP_NAV_PHONE
     * 目前在很多项目中，高德都不支持该功能点，修改为默认关闭，需要打开的话，再从适配中打开
     */
	public static final String KEY_GAODE_MAP_NAV_PHONE="keyGaoDeMapNavPhone";


	//////////////////////// Android权限相关 //////////////////////
	/**
	 * 是否需要动态申请对应标记位的Android权限 0:不需要 1:需要
	 */
	public static final String KEY_NEED_REQUEST_RUNTIME_PERMISSIONS = "keyNeedRequestRuntimePermission";
	/**
	 * 提示用户手动授权的提示语内容
	 * 未配置:显示默认提示语内容
	 * 空字符串:不显示Toast
	 */
	public static final String KEY_REQUEST_RUNTIME_PERMISSIONS_TIPS = "keyNeedRequestRuntimePermissionTips";

	///////////////////////声纹识别///////////////////////////

    //////////////////////// 地平线相关 //////////////////////
    /**
     * 是否加载libhrsc.so文件
     */
    public static final String KEY_HOBOT_NEED_LOAD_HRSC = "keyNeedLoadHrsLib";

	//////////////////////// 电影票查询及购买 /////////////////////
	/**
	 * 配置电影票购买交互过程中列表页面超时选择关闭的时间，单位：ms
	 * **/
	public static final String WAN_MI_FILM_LIST_OUT_TIME_CLEAR = "wanMiFilmListOutTimeClear";
    /**
     * 是否开启反馈功能
     */
    public static final String KEY_FEEDBACK_FEATURE_ENABLE = "keyFeedbackFeatureEnable";

	/*
	* 配置电影票支付结果弹窗的遮罩的透明度。取值0~1，1表示不透明
	* */
	public static final String WAN_MI_FILM_PAY_REQUEST_DIALOG_SHAPE = "wanMiFilmPayRequestDialogShape";

    /*
     * 配置电影票支付结果弹窗背景的透明度。取值0~1，1表示不透明
     * */
    public static final String WAN_MI_FILM_PAY_REQUEST_DIALOG_BACKGROUND = "wanMiFilmPayRequestDialogBackground";
    public static final String HEARTBEAT_ENABLE = "heartbeatEnable";
    public static final String CONSOLE_LOG_LEVEL = "consoleLogLevel";

    /************飞机票火车票购买乘客信息填写弹窗TYPE*************/
    public static final String  TICKET_USE_INFO_DIALOG_TYPE = "TicketUseInfoDialogType"; // 飞机票火车票购买乘客信息填写弹窗TYPE
	/**
	 * 加载配置项 <br>
	 * 
	 * @param configKeys
	 *            想要获取配置的key
	 * @return 获取到的配置项
	 */
	public static HashMap<String, String> getConfig(String... configKeys) {
		List<String> list = Arrays.asList(configKeys);
		return getConfig(list);
	}


    private static boolean sIsConfigLoaded = false;
    private static HashMap<String, String> sConfigs;

    private static void loadConfigs() {
        if (!sIsConfigLoaded) {
            if (FilePathConstants.mConfigFileName == null || FilePathConstants.CONFIG_PATH_DEFAULT == null) {
                FilePathConstants.mConfigFileName = GlobalContext.get().getPackageName() + ".cfg";
                FilePathConstants.CONFIG_PATH_DEFAULT = GlobalContext.get().getApplicationInfo().dataDir + "/cfg/";
            }

            synchronized (TXZFileConfigUtil.class) {
                if (!sIsConfigLoaded) {
                    List<String> filePathList = new ArrayList<String>();
                    filePathList.add(FilePathConstants.CONFIG_PATH_DEFAULT + FilePathConstants.mConfigFileName); // fileDefault
                    filePathList.add(FilePathConstants.CONFIG_PATH_DEFAULT + FilePathConstants.FILE_NAME_COMM_CONFIG); // fileDefaultComm
                    // fileUser
                    for (String path : FilePathConstants.getUserConfigPath()) {
                        filePathList.add(path + FilePathConstants.mConfigFileName);
                        filePathList.add(path + FilePathConstants.FILE_NAME_COMM_CONFIG);
                    }

                    filePathList.add(FilePathConstants.CONFIG_PATH_PRIOR + FilePathConstants.mConfigFileName); // filePrior
                    filePathList.add(FilePathConstants.CONFIG_PATH_PRIOR + FilePathConstants.FILE_NAME_COMM_CONFIG); // filePriorComm


                    for (String filePath : filePathList) {
                        File file = new File(filePath);
                        if (file.exists()) {
                            loadConfigFromFile(file);
                        }
                    }
                    sIsConfigLoaded = true;
                }
            }
        }
    }


    /**
     * 获取单项配置
     *
     * @param config
     * @return
     */
    public static String getSingleConfig(String config) {
        loadConfigs();

        if (sConfigs == null || sConfigs.size() == 0) {
            return null;
        }

        return sConfigs.get(config);
    }


    public static <T> T getSingleConfig(String configKey, Class<T> configClass, T defaultValue) {
        String configValue = getSingleConfig(configKey);
        if (TextUtils.isEmpty(configValue)) {
            return defaultValue;
        }
        try {
            if (configClass == Double.class) {
                return (T) new Double(configValue);
            } else if (configClass == Integer.class) {
                return (T) new Integer(configValue);
            } else if (configClass == Float.class) {
                return (T) new Float(configValue);
            } else if (configClass == Boolean.class) {
                return (T) new Boolean(configValue);
            }else if(configClass == String.class){
				return (T) new String(configValue);
			}else if(configClass == Long.class){
				return (T) new Long(configValue);
			}
		} catch (Exception e) {
			LogUtil.loge("read config error:" + configValue);
		}
		return defaultValue;
	}


    public static int getIntSingleConfig(String config, int defaultValue) {
        String configValue = getSingleConfig(config);
        if (!TextUtils.isEmpty(configValue)) {
            try {
                return Integer.parseInt(configValue);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return defaultValue;
    }

    public static double getDoubleSingleConfig(String config, double defaultValue) {
        String configValue = getSingleConfig(config);
        if (!TextUtils.isEmpty(configValue)) {
            try {
                return Double.parseDouble(configValue);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return defaultValue;
    }


    public static boolean getBooleanSingleConfig(String config, boolean defaultValue) {
        String configValue = getSingleConfig(config);
        if (!TextUtils.isEmpty(configValue)) {
            try {
                return Boolean.parseBoolean(configValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defaultValue;
    }


    /**
     * 加载配置项 <br>
     *
     * @param configKeys 想要获取配置的key
     * @return 获取到的配置项
     */
    public static HashMap<String, String> getConfig(List<String> configKeys) {
        if (configKeys == null || configKeys.size() == 0) {
            return null;
        }

        HashMap<String, String> cfgs = new HashMap<String, String>();

        loadConfigs();

        if (sConfigs == null || sConfigs.size() == 0) {
            return cfgs;
        }
        for (String key : configKeys) {
            if (TextUtils.isEmpty(key)) {
                continue;
            }
            String value = sConfigs.get(key);
            if (TextUtils.isEmpty(value)) {
                continue;
            }
            cfgs.put(key, value);
        }
        return cfgs;
    }

    private static void loadConfigFromFile(File file) {
        LogUtil.logd("loadConfigFromFile:" + file);
        FileInputStream fis = null;
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            fis = new FileInputStream(file);
            reader = new InputStreamReader(fis);
            bufferedReader = new BufferedReader(reader);
            String lineTxt = null;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                LogUtil.logd("readLine:" + lineTxt);
                if (TextUtils.isEmpty(lineTxt))
                    continue;
                String[] subStrings = lineTxt.split("=");
                if (subStrings.length != 2 || TextUtils.isEmpty(subStrings[0])
                        || TextUtils.isEmpty(subStrings[1])) {
                    LogUtil.loge(TAG + "config format error:" + lineTxt);
                    continue;
                }
                if (sConfigs == null) {
                    sConfigs = new HashMap<String, String>();
                }
                sConfigs.put(subStrings[0].trim(), subStrings[1].trim());
            }
        } catch (FileNotFoundException e) {
            LogUtil.loge(TAG, e);
        } catch (IOException e) {
            LogUtil.loge(TAG, e);
        } finally {
            try {
                if (fis != null)
                    fis.close();
                if (reader != null)
                    reader.close();
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                LogUtil.loge(TAG, e);
            }
        }
    }

    /**
     * 检测并拷贝upgradeFile到/cfg/
     * 更新拷贝时机：upgradeFile存在且与/cfg/下配置文件不同时进行拷贝
     */
    public static void checkAndCopyUpgradeCfgFile() {
        FilePathConstants.mConfigFileNameUpgrade = GlobalContext.get().getPackageName() + ".cfg.upgrade";
        File upgradeCfgFile = new File(
                FilePathConstants.CONFIG_PATH_UPGRADE + FilePathConstants.mConfigFileNameUpgrade);
        if (!upgradeCfgFile.exists()) {
            LogUtil.logd("upgrade cfg file not exist");
            return;
        }
        FilePathConstants.CONFIG_PATH_DEFAULT = GlobalContext.get().getApplicationInfo().dataDir + "/cfg/";
        FilePathConstants.mConfigFileName = GlobalContext.get().getPackageName() + ".cfg";
        File defaultCfgFile = new File(FilePathConstants.CONFIG_PATH_DEFAULT + FilePathConstants.mConfigFileName);
        if (!defaultCfgFile.exists() || TextUtils.isEmpty(MD5Util.generateMD5(defaultCfgFile))
                || !MD5Util.generateMD5(defaultCfgFile).equals(MD5Util.generateMD5(upgradeCfgFile))) {
            MonitorUtil.monitorCumulant(MonitorUtil.CFG_FILE_COPY_ENTER);
            LogUtil.logd("start copy upgrade file to default path");
            if (!defaultCfgFile.exists()) {
                try {
                    File dir = new File(FilePathConstants.CONFIG_PATH_DEFAULT);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    defaultCfgFile.createNewFile();
                } catch (IOException e) {
                    LogUtil.loge("error create file ", e);
                }
            }
            if (defaultCfgFile.exists() && FileUtil.copyFile(
                    FilePathConstants.CONFIG_PATH_UPGRADE + FilePathConstants.mConfigFileNameUpgrade,
                    FilePathConstants.CONFIG_PATH_DEFAULT + FilePathConstants.mConfigFileName)) {
                MonitorUtil.monitorCumulant(MonitorUtil.CFG_FILE_COPY_SUCC);
            } else {
                LogUtil.loge("Failed copy upgrade cfg file");
                MonitorUtil.monitorCumulant(MonitorUtil.CFG_FILE_COPY_ERROR);
            }
        } else {
            LogUtil.logd("default cfg file is same with upgrade cfg file,no need to copy");
        }
    }

}

package com.txznet.music.config;

import com.txznet.txz.util.TXZFileConfigUtil;

/**
 * 外部文件设置的工具类
 *
 * @author telen
 * @date 2018/12/28,19:53
 */
public class ConfigUtils {


    /**
     * 单例对象
     */
    private volatile static ConfigUtils singleton;

    private ConfigUtils() {
    }

    public static ConfigUtils getInstance() {
        if (singleton == null) {
            synchronized (ConfigUtils.class) {
                if (singleton == null) {
                    singleton = new ConfigUtils();
                }
            }
        }
        return singleton;
    }

    private static final String KEY_NOTOPENAPPPNAME = "openAppPlay";//声控后是否打开播放器界面
    private static final String KEY_RELEASEAUDIOFOCUS = "releaseFocusWhenLoss";//电台之家丢失音频焦点后是否释放焦点
    private static final String KEY_WAKEUPPLAY = "wakeupPlay";//设置电台之家在休眠唤醒后是否继续播放
    private static final String KEY_RESUME_AUTO_PLAY = "openPlay";//设置打开同听后是否自动开始播放
    private static final String KEY_SHORTPLAYENABLE = "shortPlayEnable";//设置默认是否开启快报推送，
    private static final String KEY_SHORTPLAYDEFAULT = "shortPlayDefault";//设置快报推送,默认值
    private static final String KEY_SEARCHSIZE = "searchSize";//设置本地扫描最小扫描文件大小,默认为500K
    private static final String KEY_SEARCHPATH = "searchPath";//设置本地扫描路径
    private static final String KEY_NEEDASR = "asrEnable";//设置App是否启用全局唤醒词
    private static final String KEY_WAKEUP_DEFAULT = "asrDefault";//设置同听是否注册免唤醒词的默认值，用户修改后以用户设置为准l

    /**
     * 声控后是否打开播放器界面
     *
     * @return
     */
    public boolean isOpenAppPlay() {
        return TXZFileConfigUtil.getBooleanSingleConfig(KEY_NOTOPENAPPPNAME, true);
    }

    /**
     * 丢失焦点的时候是否需要释放焦点
     *
     * @return
     */
    public boolean isReleaseFocusWhenLoss() {
        return TXZFileConfigUtil.getBooleanSingleConfig(KEY_RELEASEAUDIOFOCUS, true);
    }

    /**
     * 电台之家在休眠唤醒后是否继续播放
     *
     * @return
     */
    public boolean isWakeUpNeedPlay() {
        return TXZFileConfigUtil.getBooleanSingleConfig(KEY_WAKEUPPLAY, true);
    }

    /**
     * 打开同听后是否自动开始播放
     *
     * @return
     */
    public boolean isNeedPlayWhenBoot() {
        return TXZFileConfigUtil.getBooleanSingleConfig(KEY_RESUME_AUTO_PLAY, true);
    }

    /**
     * 是否开启快报推送(如果false则,设置界面就改选项就不见了)
     *
     * @return
     */
    public boolean isNeedShortPlayDefault() {
        return TXZFileConfigUtil.getBooleanSingleConfig(KEY_SHORTPLAYDEFAULT, true);

    }

    /**
     * 判断快报是否可用
     *
     * @return
     */
    public boolean isShorPlayEnable() {
        return TXZFileConfigUtil.getBooleanSingleConfig(KEY_SHORTPLAYENABLE, true);
    }

    /**
     * 获取本地扫描可扫描文件的最小文件大小
     *
     * @return
     */
    public int getSearchSize() {
        return TXZFileConfigUtil.getIntSingleConfig(KEY_SEARCHSIZE, 500);
    }

    /**
     * 获取可搜索的文件路径
     *
     * @return
     */
    public String getSearchPath() {
        return TXZFileConfigUtil.getSingleConfig(KEY_SEARCHPATH);
    }


    /**
     * 判断免唤醒词是否可用
     *
     * @return
     */
    public boolean isAsrEnable() {
        return TXZFileConfigUtil.getBooleanSingleConfig(KEY_NEEDASR, true);
    }

    /**
     * 获取同听是否注册免唤醒词,[外部设置]
     *
     * @return
     */
    public boolean isNeedAsrDefault() {
        return TXZFileConfigUtil.getBooleanSingleConfig(KEY_WAKEUP_DEFAULT, true);
    }


}

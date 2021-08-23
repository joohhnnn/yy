package com.txznet.music.config;

import com.txznet.music.report.ReportEvent;
import com.txznet.music.soundControlModule.asr.AsrManager;
import com.txznet.music.utils.SharedPreferencesUtils;

/**
 * Created by Terry on 2017/5/11.
 */

public class ConfigManager {

    private static ConfigManager sInstance = new ConfigManager();

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        return sInstance;
    }

//    public void init() {
//        // 从配置文件读取之前设置
//    }

    public void setBootRadio(boolean enable) {
        SharedPreferencesUtils.setBootRadio(enable);
    }

    public void switchBootRadio() {
        boolean enable = SharedPreferencesUtils.isOpenPush();
        SharedPreferencesUtils.setOpenPush(!enable);
        ReportEvent.clickSettingPagePush(enable ? 0 : 1);
    }

    /**
     * 开机广播
     */
    public boolean isBootRadioEnabled() {
        return SharedPreferencesUtils.isOpenPush();
    }

    public boolean isPersSkinEnabled() {
        return SharedPreferencesUtils.isOpenPersonalizedSkin();
    }


    public void setEnterPlayActivity(boolean enter) {
        SharedPreferencesUtils.setEnterPlayActivity(enter);
    }


    public void switchEnterPlayActivity() {
        boolean enable = SharedPreferencesUtils.getEnterPlayActivity();
        SharedPreferencesUtils.setEnterPlayActivity(!enable);
    }

    /**
     * 是否自动打开播放器界面
     *
     * @return
     */
    public boolean isEnterPlayActivity() {
        return SharedPreferencesUtils.getEnterPlayActivity();
    }

    public void setAudioQuality(int audioQuality) {
        SharedPreferencesUtils.setAudioQuality(audioQuality);
    }

    /**
     * 音频品质
     *
     * @return
     */
    public int getAudioQuality() {
        return SharedPreferencesUtils.getAudioQuality();
    }


    public boolean isEnableWakeup() {
        return SharedPreferencesUtils.isWakeupEnable();
    }


    public void switchWakeup() {
        boolean wakeupEnable = SharedPreferencesUtils.isWakeupEnable();
        SharedPreferencesUtils.setWakeupEnable(!wakeupEnable);
        if (wakeupEnable) {
            AsrManager.getInstance().unregCMD();
        } else {
            AsrManager.getInstance().regCMD();
        }
        ReportEvent.clickSettingPageWakeUp(wakeupEnable ? 0 : 1);
    }

    public void switchPersonalizedSkin() {
        boolean enable = SharedPreferencesUtils.isOpenPersonalizedSkin();
        SharedPreferencesUtils.setOpenPersonalizedSkin(!enable);
        ReportEvent.clickSettingSkin(enable ? 0 : 1);
    }

}

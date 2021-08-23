package com.txznet.music.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArraySet;
import android.util.Log;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.BuildConfig;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.sdk.bean.LocationData;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SharedPreferencesUtils {

    private static final String PRE_NAME_TXZ = "txz_music";

    public static boolean isResumeAutoPlay() {
        return getBoolean(SharePreferenceKeyParams.KEY_RESUME_AUTO_PLAY, false);
    }

    public static void setResumeAutoPlay(boolean set) {
        put(SharePreferenceKeyParams.KEY_RESUME_AUTO_PLAY, set);
    }

    public static boolean getDefaultShortPlayEnable() {
        return getBoolean(SharePreferenceKeyParams.KEY_DEFAULT_SHORT_PLAY_ENABLE, true);
    }

    public static boolean getDefaultPersSkinEnable() {
        return getBoolean(SharePreferenceKeyParams.KEY_PERSONALIZED_SKIN_ENABLE, false);
    }

    public static void setDefaultShortPlayEnable(boolean enable) {
        put(SharePreferenceKeyParams.KEY_DEFAULT_SHORT_PLAY_ENABLE, enable);
    }

    public static boolean getWakeupDefaultValue() {
        return getBoolean(SharePreferenceKeyParams.KEY_WAKEUP_DEFAULT_VALUE, false);
    }

    public static void setWakeupDefaultValue(boolean defaultValue) {
        put(SharePreferenceKeyParams.KEY_WAKEUP_DEFAULT_VALUE, defaultValue);
    }

    public static boolean isWakeupEnable() {
        if (getNeedAsr()) {
            return getBoolean(SharePreferenceKeyParams.KEY_WAKEUP_ENABLE, getWakeupDefaultValue());
        }
        return false;
    }

    public static void setWakeupEnable(boolean enable) {
        put(SharePreferenceKeyParams.KEY_WAKEUP_ENABLE, enable);
    }

    public static int getWakeupCount() {
        return getInt(SharePreferenceKeyParams.KEY_WAKEUP_COUNT, 0);
    }

    public static void setWakeupCount(int count) {
        put(SharePreferenceKeyParams.KEY_WAKEUP_COUNT, count);
    }

    public static int getLastPid() {
        return getInt(SharePreferenceKeyParams.KEY_LAST_PID, 0);
    }

    public static void setLastPid(int pid) {
        put(SharePreferenceKeyParams.KEY_LAST_PID, pid);
    }

//    public static boolean getIsShortPlayNeedTrigger() {
//        return getBoolean(SharePreferenceKeyParams.KEY_SHORT_PLAY_TRIGGER, false);
//    }
//
//    public static void setShortPlayNeedTrigger(boolean enable) {
//        put(SharePreferenceKeyParams.KEY_SHORT_PLAY_TRIGGER, enable);
//    }

    public static String getAPPID() {
        return getString(SharePreferenceKeyParams.KEY_DEVICE_APPID, "");
    }

    public static void setAPPID(String appid) {
        put(SharePreferenceKeyParams.KEY_DEVICE_APPID, appid);
    }

    public static String getSDKListenerPackageName() {
        return getString(SharePreferenceKeyParams.KEY_SDK_LISTENER_PACKAGENAME, "");
    }

    /**
     * 设置监听的状态，远程服务端
     *
     * @param packageName
     */
    public static void setSDKListenerPackageName(String packageName) {
        put(SharePreferenceKeyParams.KEY_SDK_LISTENER_PACKAGENAME, packageName);
    }

    public static boolean getIsShortPlayFirstPlay() {
        return getBoolean(SharePreferenceKeyParams.KEY_IS_SHORT_PLAY_FIRST_PLAY, true);
    }

    public static void setShortPlayFirstPlay(boolean isFirst) {
        put(SharePreferenceKeyParams.KEY_IS_SHORT_PLAY_FIRST_PLAY, isFirst);
    }

    public static boolean getShowExitDialog() {
        return getBoolean(SharePreferenceKeyParams.KEY_SHOW_EXIT_DIALOG, true);
    }

    public static void setShowExitDialog(boolean show) {
        put(SharePreferenceKeyParams.KEY_SHOW_EXIT_DIALOG, show);
    }

    public static LocationData getLocationInfo() {
        return JsonHelper.toObject(LocationData.class, getString(SharePreferenceKeyParams.KEY_CURRENT_LOCATIONDATA, ""));
    }

    public static void setLocationInfo(LocationData locationData) {
        put(SharePreferenceKeyParams.KEY_CURRENT_LOCATIONDATA, JsonHelper.toJson(locationData));
    }

    public static boolean getExitWithPlay() {
        return getBoolean(SharePreferenceKeyParams.KEY_SETTINGS_EXIT_WITH_PLAY, true);
    }

    public static void setExitWithPlay(boolean withPlay) {
        put(SharePreferenceKeyParams.KEY_SETTINGS_EXIT_WITH_PLAY, withPlay);
    }

    public static boolean getExtraTypeFace() {
        return getBoolean(SharePreferenceKeyParams.KEY_SETTINGS_NEED_EXTRA_TYPEFACE, false);
    }

    public static void setExtraTypeFace(boolean needExtra) {
        put(SharePreferenceKeyParams.KEY_SETTINGS_NEED_EXTRA_TYPEFACE, needExtra);
    }

    public static boolean isBackVisible() {
        return getBoolean(SharePreferenceKeyParams.KEY_BACK_VISIBLE, true);
    }

    public static void setBackVisible(boolean backVisible) {
        put(SharePreferenceKeyParams.KEY_BACK_VISIBLE, backVisible);
    }

    public static boolean isEnableSplash() {
        return getBoolean(SharePreferenceKeyParams.KEY_ENABLE_SPLASH, true);
    }

    public static void setEnableSplash(boolean enable) {
        put(SharePreferenceKeyParams.KEY_ENABLE_SPLASH, enable);
    }

    public static boolean isEnableFloatingPlayer() {
        return getBoolean(SharePreferenceKeyParams.KEY_ENABLE_FLOATING_PLAYER, false);
    }

    public static void setEnableFloatingPlayer(boolean enable) {
        put(SharePreferenceKeyParams.KEY_ENABLE_FLOATING_PLAYER, enable);
    }

    public static boolean isOpenPush() {
        return getBoolean(SharePreferenceKeyParams.KEY_OPEN_PUSH, getDefaultShortPlayEnable());
    }

    public static void setOpenPush(boolean isopen) {
        put(SharePreferenceKeyParams.KEY_OPEN_PUSH, isopen);
    }

    public static boolean isOpenPersonalizedSkin() {
        return getBoolean(SharePreferenceKeyParams.KEY_PERSONALIZED_SKIN_ENABLE, getDefaultPersSkinEnable());
    }

    public static void setOpenPersonalizedSkin(boolean isOpen) {
        put(SharePreferenceKeyParams.KEY_PERSONALIZED_SKIN_ENABLE, isOpen);
    }

    public static int getQulityMode() {
        return getInt(SharePreferenceKeyParams.KEY_QULITY_MODE, 0);
    }

    public static void setQulityMode(int qulity) {
        put(SharePreferenceKeyParams.KEY_QULITY_MODE, qulity);
    }

    public static boolean isAutoOpen() {
        return getBoolean(SharePreferenceKeyParams.KEY_OPEN_APP_AUTO, false);
    }

    public static void setAutoOpen(boolean isopen) {
        put(SharePreferenceKeyParams.KEY_OPEN_APP_AUTO, isopen);
    }

    public static boolean getWakeupPlay() {
        return getBoolean(SharePreferenceKeyParams.KEY_WAKEUP_PLAY, true);
    }

    public static void setWakeupPlay(boolean isPlay) {
        put(SharePreferenceKeyParams.KEY_WAKEUP_PLAY, isPlay);
    }

    public static boolean getFullScreen() {
        return getBoolean(SharePreferenceKeyParams.KEY_Full_SCREEN, false);
    }

    public static void setFullScreen(boolean full) {
        put(SharePreferenceKeyParams.KEY_Full_SCREEN, full);
    }

    public static boolean isReleaseAudioFocus() {
        return getBoolean(SharePreferenceKeyParams.KEY_RELEASE_AUDIO_FOCUS, true);
    }

    public static void setReleaseAudioFocus(boolean isRelease) {
        put(SharePreferenceKeyParams.KEY_RELEASE_AUDIO_FOCUS, isRelease);
    }

    public static boolean isCloseVolume() {
        return getBoolean(SharePreferenceKeyParams.KEY_CLOSEVOLUME, true);
    }

    public static void setCloseVolume(boolean value) {
        put(SharePreferenceKeyParams.KEY_CLOSEVOLUME, value);
    }

    public static String getNotOpenAppPName() {
        return getString(SharePreferenceKeyParams.KEY_NOTAPPPNAME, "");
    }

    public static void setNotOpenAppPName(String pNames) {
        put(SharePreferenceKeyParams.KEY_NOTAPPPNAME, pNames);
    }

    public static String getSdcardPath() {
        return getString(SharePreferenceKeyParams.KEY_SDCARD_PATHS, "");
    }

    public static void setSdcardPath(String sdcardPath) {
        put(SharePreferenceKeyParams.KEY_SDCARD_PATHS, sdcardPath);
    }

    public static String getLocalPaths() {
        return getString(SharePreferenceKeyParams.KEY_LOCALPATHS, "");
    }

    public static void setLocalPaths(String paths) {
        put(SharePreferenceKeyParams.KEY_LOCALPATHS, paths);
    }

    public static Long getSearchSize() {
        return getLong(SharePreferenceKeyParams.KEY_SEARCHSIZE, 200 * 1024L);
    }

    public static void setSearchSize(Long searchSize) {
        put(SharePreferenceKeyParams.KEY_SEARCHSIZE, searchSize);
    }

    public static String getConfig() {
        return getString(SharePreferenceKeyParams.KEY_CONFIG, "{\"arrPlay\":[{\"bNeedProcess\":false,\"play\":1,\"sid\":1,\"type\":2},{\"bNeedProcess\":false,\"play\":1,\"sid\":2,\"type\":1},{\"bNeedProcess\":false,\"play\":1,\"sid\":3,\"type\":2},{\"bNeedProcess\":false,\"play\":1,\"sid\":4,\"type\":2},{\"bNeedProcess\":false,\"play\":1,\"sid\":5,\"type\":3},{\"bNeedProcess\":false,\"play\":1,\"sid\":6,\"type\":1},{\"bNeedProcess\":false,\"play\":1,\"sid\":7,\"type\":1}],\"logoTag\":7}");
    }

    public static void setConfig(String config) {
        put(SharePreferenceKeyParams.KEY_CONFIG, config);
    }

    @PlayerInfo.PlayerMode
    public static int getPlayMode() {
        return getInt(SharePreferenceKeyParams.KEY_PLAY_MODE, PlayerInfo.PLAYER_MODE_SEQUENCE);
    }

    public static void setPlayMode(@PlayerInfo.PlayerMode int mode) {
        put(SharePreferenceKeyParams.KEY_PLAY_MODE, mode);
    }

    public static int getAudioSource() {
        return getInt(SharePreferenceKeyParams.KEY_AUDIO_SOUCRE, 0);
    }

    public static void setAudioSource(int source) {
        put(SharePreferenceKeyParams.KEY_AUDIO_SOUCRE, source);
    }

    public static int getPlayListScence() {
        return getInt(SharePreferenceKeyParams.KEY_PLAY_LIST_SCENE, 0);
    }

    public static void setPlayListScence(int source) {
        put(SharePreferenceKeyParams.KEY_PLAY_LIST_SCENE, source);
    }


    //    public static int getTTSSpeakNotEnoughSpaceCount() {
//        return getInt(SharePreferenceKeyParams.KEY_TTS_SPEAK_NOT_ENOUGH_SPACE, 0);
//    }
//
//    public static void setTTSSpeakNotEnoughSpaceCount(int source) {
//        put(SharePreferenceKeyParams.KEY_TTS_SPEAK_NOT_ENOUGH_SPACE, source);
//    }

    public static String getTTSSpeakNotEnoughSpaceIDs() {
        return getString(SharePreferenceKeyParams.KEY_TTS_SPEAK_NOT_ENOUGH_SPACE, "");
    }

    public static void addTTSSpeakNotEnoughSpaceIDs(String ids) {
        StringBuffer stringBuffer = new StringBuffer(getTTSSpeakNotEnoughSpaceIDs());
        if (stringBuffer.length() > 1) {
            stringBuffer.append(",");
        }
        stringBuffer.append(ids);
        put(SharePreferenceKeyParams.KEY_TTS_SPEAK_NOT_ENOUGH_SPACE, stringBuffer.toString());
    }

    public static void removeTTSSpeakNotEnoughSpaceIDs() {
        SharedPreferences.Editor editor = getEditor();
        editor.remove(SharePreferenceKeyParams.KEY_TTS_SPEAK_NOT_ENOUGH_SPACE);
        editor.apply();
    }


//    public static Set<String> getTTSSpeakNotEnoughSpaceIDs() {
//        if (getSet(SharePreferenceKeyParams.KEY_TTS_SPEAK_NOT_ENOUGH_SPACE, null) == null) {
//            SharedPreferences sp = getSharedPreferences(GlobalContext.get(),
//                    SharedPreferenceType.LOCAL);
//            SharedPreferences.Editor editor = sp.edit();
//            editor.putStringSet(SharePreferenceKeyParams.KEY_TTS_SPEAK_NOT_ENOUGH_SPACE, new HashSet<String>(3));
//            editor.apply();
//        }
//        return getSet(SharePreferenceKeyParams.KEY_TTS_SPEAK_NOT_ENOUGH_SPACE, new HashSet<String>(3));
//    }

//    public static void addTTSSpeakNotEnoughSpaceIDs(String ids) {
//        Set<String> ttsSpeakNotEnoughSpaceIDs = getTTSSpeakNotEnoughSpaceIDs();
//        ttsSpeakNotEnoughSpaceIDs.add(ids);
//        SharedPreferences.Editor editor = getEditor();
//        editor.putStringSet(SharePreferenceKeyParams.KEY_TTS_SPEAK_NOT_ENOUGH_SPACE, ttsSpeakNotEnoughSpaceIDs);
//        editor.apply();
//    }
//
//    public static void clearTTSSpeakNotEnoughSpaceIDs() {
//        getTTSSpeakNotEnoughSpaceIDs().clear();
//        SharedPreferences.Editor editor = getEditor();
//        editor.remove(SharePreferenceKeyParams.KEY_TTS_SPEAK_NOT_ENOUGH_SPACE);
//        editor.apply();
//    }


    public static int getReqInsterestTagCount() {
        return getInt(SharePreferenceKeyParams.KEY_REQ_INSTEREST_TAG, 0);
    }

    public static void setReqInsterestTagCount(int count) {
        put(SharePreferenceKeyParams.KEY_REQ_INSTEREST_TAG, count);
    }

    private static SharedPreferences.Editor getEditor() {
        return getSharedPreferences(GlobalContext.get(), SharedPreferenceType.LOCAL).edit();
    }

    public static int getReqFMInterestTagCount() {
        return getInt(SharePreferenceKeyParams.KEY_REQ_FM_INTEREST_TAG, 0);
    }

    public static void setReqFMInterestTagCount(int count) {
        put(SharePreferenceKeyParams.KEY_REQ_FM_INTEREST_TAG, count);
    }

    public static boolean isFirst() {
        return getBoolean(SharePreferenceKeyParams.KEY_IS_FIRST, true);
    }

    public static void setIsFirst(boolean value) {
        put(SharePreferenceKeyParams.KEY_IS_FIRST, value);
    }

    public static boolean getIsPlay() {
        return getBoolean(SharePreferenceKeyParams.KEY_IS_PLAY, false);
    }

    public static void setIsPlay(boolean b) {
        put(SharePreferenceKeyParams.KEY_IS_PLAY, b);
    }

    public static boolean getShouldTip() {
        return getBoolean(SharePreferenceKeyParams.KEY_IS_FIRST_TIP, false);
    }

    public static void setShouldTip(boolean b) {
        put(SharePreferenceKeyParams.KEY_IS_FIRST_TIP, b);
    }

    public static boolean getAppFirstPlay() {
        return getBoolean(SharePreferenceKeyParams.KEY_APPFIRSTPLAY, false);
    }

    public static void setAppFirstPlay(boolean b) {
        put(SharePreferenceKeyParams.KEY_APPFIRSTPLAY, b);
    }

    public static boolean getFatalExit() {
        return getBoolean(SharePreferenceKeyParams.KEY_FATALEXIT, false);
    }

    public static void setFatalExit(boolean b) {
        put(SharePreferenceKeyParams.KEY_FATALEXIT, b);
    }

    public static boolean getEnterPlayActivity() {
        return getBoolean(SharePreferenceKeyParams.KEY_ENTER_PLAY_ACTIVITY, false);
    }

    public static void setEnterPlayActivity(boolean b) {
        put(SharePreferenceKeyParams.KEY_ENTER_PLAY_ACTIVITY, b);
    }

    public static boolean getBootRadio() {
        return getBoolean(SharePreferenceKeyParams.KEY_BOOT_RADIO, true);
    }

    public static void setBootRadio(boolean b) {
        put(SharePreferenceKeyParams.KEY_BOOT_RADIO, b);
    }

    public static int getAudioQuality() {
        return getInt(SharePreferenceKeyParams.KEY_AUDIO_QUALITY, 0);
    }

    public static void setAudioQuality(int qualityMode) {
        put(SharePreferenceKeyParams.KEY_AUDIO_QUALITY, qualityMode);
    }

    public static boolean getNeedAsr() {
        return getBoolean(SharePreferenceKeyParams.KEY_APP_NEED_ASR, true);
    }

    // 设置是否需要全局唤醒词
    public static void setNeedAsr(boolean b) {
        put(SharePreferenceKeyParams.KEY_APP_NEED_ASR, b);
    }

    /**
     * 30秒无操作是否自动跳到播放器
     *
     * @return
     */
    public static boolean getAutoJumpPlayerPage() {
        return getBoolean(SharePreferenceKeyParams.KEY_SETTINGS_AUTO_JUMP_TO_PLAYER, true);
    }

    /**
     * 30秒无操作是否自动跳到播放器
     *
     * @param b
     */
    public static void setAutoJumpPlayerPage(boolean b) {
        put(SharePreferenceKeyParams.KEY_SETTINGS_AUTO_JUMP_TO_PLAYER, b);
    }

    public static void setCanNotShowPushWin(boolean showWindowView) {
        put(SharePreferenceKeyParams.KEY_SHOW_PUSH_WINDOW_VIEW, showWindowView);
    }

    public static boolean canNotShowPushWin() {
        return getBoolean(SharePreferenceKeyParams.KEY_SHOW_PUSH_WINDOW_VIEW, false);
    }


    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param key    键
     * @param object 值
     */
    public static void put(String key, Object object) {
        SharedPreferences sp = getSharedPreferences(GlobalContext.get(),
                SharedPreferenceType.LOCAL);
        SharedPreferences.Editor editor = sp.edit();
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else if (object instanceof Set) {
            editor.putStringSet(key, (Set<String>) object);
        } else {
            editor.putString(key, object.toString());
        }
        editor.apply();
//         SharedPreferencesCompat.apply(editor);
    }

//    /**
//     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
//     *
//     * @param key           键
//     * @param defaultObject 默认值
//     * @return 返回值
//     */
//    public static Object get(String key, Object defaultObject) {
//        SharedPreferences sp = getSharedPreferences(GlobalContext.get(),
//                SharedPreferenceType.LOCAL);
//        if (defaultObject instanceof String) {
//            return sp.getString(key, (String) defaultObject);
//        } else if (defaultObject instanceof Integer) {
//            return sp.getInt(key, (Integer) defaultObject);
//        } else if (defaultObject instanceof Boolean) {
//            return sp.getBoolean(key, (Boolean) defaultObject);
//        } else if (defaultObject instanceof Float) {
//            return sp.getFloat(key, (Float) defaultObject);
//        } else if (defaultObject instanceof Long) {
//            return sp.getLong(key, (Long) defaultObject);
//        }
//
//        return null;
//    }

    private static boolean getBoolean(String key, boolean defaultValue) {
        SharedPreferences sp = getSharedPreferences(GlobalContext.get(), SharedPreferenceType.LOCAL);
        return sp.getBoolean(key, defaultValue);
    }

    private static String getString(String key, String defaultValue) {
        SharedPreferences sp = getSharedPreferences(GlobalContext.get(), SharedPreferenceType.LOCAL);
        return sp.getString(key, defaultValue);
    }

    private static int getInt(String key, int defaultValue) {
        SharedPreferences sp = getSharedPreferences(GlobalContext.get(), SharedPreferenceType.LOCAL);
        return sp.getInt(key, defaultValue);
    }

    private static long getLong(String key, long defaultValue) {
        SharedPreferences sp = getSharedPreferences(GlobalContext.get(), SharedPreferenceType.LOCAL);
        return sp.getLong(key, defaultValue);
    }

    private static float getFloat(String key, float defaultValue) {
        SharedPreferences sp = getSharedPreferences(GlobalContext.get(), SharedPreferenceType.LOCAL);
        return sp.getFloat(key, defaultValue);
    }

    private static Set<String> getSet(String key, Set<String> defaultValue) {
        SharedPreferences sp = getSharedPreferences(GlobalContext.get(), SharedPreferenceType.LOCAL);
        return sp.getStringSet(key, defaultValue);
    }

    /**
     * clear all sharePreference data
     *
     * @param clearFlag
     */
    public static void clear(Context mContext, SharedPreferenceType clearFlag) {
        SharedPreferences sp = null;
        switch (clearFlag) {
            case LOCAL:
                sp = mContext.getSharedPreferences(PRE_NAME_TXZ,
                        Activity.MODE_PRIVATE);
                break;
            case PROCESS:
                sp = mContext.getSharedPreferences(PRE_NAME_TXZ,
                        Activity.MODE_MULTI_PROCESS);
                break;
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.clear().apply();
    }

    /**
     * 根据
     *
     * @param clearFlag
     * @return
     */
    public static SharedPreferences getSharedPreferences(Context mContext,
                                                         SharedPreferenceType clearFlag) {
        SharedPreferences sp = null;
        switch (clearFlag) {
            case LOCAL:
                sp = mContext.getSharedPreferences(PRE_NAME_TXZ,
                        Activity.MODE_PRIVATE);
                return sp;
            case PROCESS:
                sp = mContext.getSharedPreferences(PRE_NAME_TXZ,
                        Activity.MODE_MULTI_PROCESS);
                return sp;
            default:
                return sp;
        }
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param key
     * @return
     */
    public static boolean contains(Context mContext, String key,
                                   SharedPreferenceType sharePreferenceType) {
        SharedPreferences sp = getSharedPreferences(mContext,
                sharePreferenceType);
        return sp.contains(key);
    }

    /**
     * 返回所有的键值对
     *
     * @return
     */
    public static Map<String, ?> getAll(Context mContext,
                                        SharedPreferenceType flag) {
        SharedPreferences sp = getSharedPreferences(mContext, flag);
        return sp.getAll();
    }

    /**
     * the flag for clear data
     */
    public enum SharedPreferenceType {
        /**/
        LOCAL, PROCESS
    }

    interface SharePreferenceKeyParams {

        String KEY_CLOSEVOLUME = "key_closevolume";
        String KEY_NOTAPPPNAME = "key_notappname";
        String KEY_LOCALPATHS = "key_local_paths";
        String KEY_SDCARD_PATHS = "key_sdcard_paths";
        String KEY_SEARCHSIZE = "key_searchsize";
        String KEY_CONFIG = "key_config";
        String KEY_PLAY_MODE = "key_play_mode";
        String KEY_CURRENT_ALBUM_ID = "key_current_album_id";
        String KEY_IS_FIRST = "key_is_first";
        String KEY_AUDIO_SOUCRE = "key_audio_soucre";
        String KEY_PLAY_LIST_SCENE = "key_play_list_scene";
        String KEY_REQ_INSTEREST_TAG = BuildConfig.VERSION_NAME + "key_req_insterest_tag";
        String KEY_REQ_FM_INTEREST_TAG = BuildConfig.VERSION_NAME + "key_req_fm_interest_tag";
        String KEY_IS_PLAY = "key_is_play";
        String KEY_IS_FIRST_TIP = "key_is_fist_tip";
        String KEY_APPFIRSTPLAY = "key_appfirstplay";
        String KEY_FATALEXIT = "key_fatalexit";
        String KEY_APP_NEED_ASR = "key_app_need_asr";
        String KEY_RELEASE_AUDIO_FOCUS = "key_release_audio_focus";
        String KEY_WAKEUP_PLAY = "key_wakeup_play";
        String KEY_Full_SCREEN = "key_full_screen";
        String KEY_OPEN_PUSH = "key_open_push";
        String KEY_QULITY_MODE = "key_qulity_mode";
        String KEY_OPEN_APP_AUTO = "key_open_app_auto";
        String KEY_ENABLE_FLOATING_PLAYER = "enable_floating_player";
        String KEY_ENABLE_SPLASH = "enable_splash";
        String KEY_BACK_VISIBLE = "key_back_visible";
        String KEY_SETTINGS_AUTO_JUMP_TO_PLAYER = "key_settings_auto_jump_to_player";
        String KEY_SETTINGS_EXIT_WITH_PLAY = "key_settings_exit_with_play";
        String KEY_SETTINGS_NEED_EXTRA_TYPEFACE = "key_settings_need_extra_typeface";
        String KEY_CURRENT_LOCATIONDATA = "key_current_locationdata";
        String KEY_SHOW_EXIT_DIALOG = "key_show_exit_dialog";
        String KEY_IS_SHORT_PLAY_FIRST_PLAY = "key_is_short_play_first_play";
        String KEY_DEVICE_APPID = "key_device_appid";
        String KEY_SHORT_PLAY_TRIGGER = "key_short_play_trigger";
        String KEY_LAST_PID = "key_last_pid";
        String KEY_ENTER_PLAY_ACTIVITY = "key_enter_play_activity"; // 是否进入播放界面
        String KEY_BOOT_RADIO = "key_boot_radio"; // 是否播放开机广播
        String KEY_AUDIO_QUALITY = "key_audio_quality"; // 音质
        String KEY_WAKEUP_COUNT = "key_wakeup_time"; //免唤醒次数
        String KEY_WAKEUP_ENABLE = "key_wakeup_enable"; //是否使用免唤醒功能，用户设置
        String KEY_WAKEUP_DEFAULT_VALUE = "key_wakeup_default_value"; //是否使用免唤醒功能，方案商设置，默认为空
        String KEY_DEFAULT_SHORT_PLAY_ENABLE = "key_default_short_play"; //快报开关的默认值，默认打开
        String KEY_RESUME_AUTO_PLAY = "key_resume_auto_play";   //打开同听是否自动播放，默认不播放
        String KEY_TTS_SPEAK_NOT_ENOUGH_SPACE = "key_tts_speak_not_enough_space_v1";   //记录开机播报次数，每次进程启动，则最大提示三次
        String KEY_PERSONALIZED_SKIN_ENABLE = "key_personalized_skin_enable";//记录个性化皮肤开启，默认打开
        String KEY_SDK_LISTENER_PACKAGENAME = "key_listener_packageName";

        String KEY_SHOW_PUSH_WINDOW_VIEW = "key_show_push_window_view";//开机横幅是否展示过
    }

}

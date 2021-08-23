package com.txznet.music.data.sp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.config.ConfigUtils;
import com.txznet.music.data.entity.PlayMode;

import java.util.Map;

public class SharedPreferencesUtils {

    private static final String PRE_NAME_TXZ = "txz_music";


    public static Long getSearchSize() {
        return getLong(SharePreferenceKeyParams.KEY_SEARCHSIZE, 500 * 1024L);
    }

    public static void setSearchSize(Long searchSize) {
        put(SharePreferenceKeyParams.KEY_SEARCHSIZE, searchSize);
    }

    public static String getLocalPaths() {
        return getString(SharePreferenceKeyParams.KEY_LOCALPATHS, "");
    }

    public static void setLocalPaths(String paths) {
        put(SharePreferenceKeyParams.KEY_LOCALPATHS, paths);
    }

    public static String getConfig() {
        return getString(SharePreferenceKeyParams.KEY_CONFIG, "{\"logoTag\":\"30\",\"arrPlay\":[{\"sid\":\"1\",\"play\":\"1\",\"type\":\"2\",\"sourceFrom\":\"考拉\",\"logo\":null},{\"sid\":\"2\",\"play\":\"1\",\"type\":\"1\",\"sourceFrom\":\"QQ音乐\",\"logo\":null},{\"sid\":\"3\",\"play\":\"1\",\"type\":\"2\",\"sourceFrom\":\"喜马拉雅\",\"logo\":null},{\"sid\":\"7\",\"play\":\"1\",\"type\":\"1\",\"sourceFrom\":\"QQ音乐\",\"logo\":null},{\"sid\":\"8\",\"play\":\"1\",\"type\":\"3\",\"sourceFrom\":\"乐听\",\"logo\":null},{\"sid\":\"9\",\"play\":\"1\",\"type\":\"2\",\"sourceFrom\":\"\",\"logo\":null},{\"sid\":\"20\",\"play\":\"1\",\"type\":\"1\",\"sourceFrom\":\"QQ音乐\",\"logo\":null},{\"sid\":\"21\",\"play\":\"1\",\"type\":\"1\",\"sourceFrom\":\"QQ音乐\",\"logo\":null},{\"sid\":\"22\",\"play\":\"1\",\"type\":\"1\",\"sourceFrom\":\"QQ音乐\",\"logo\":null},{\"sid\":\"23\",\"play\":\"1\",\"type\":\"1\",\"sourceFrom\":\"喜马拉雅\",\"logo\":null},{\"sid\":\"24\",\"play\":\"1\",\"type\":\"1\",\"sourceFrom\":\"微信推送\",\"logo\":null},{\"sid\":\"30\",\"play\":\"1\",\"type\":\"1\",\"sourceFrom\":\"\",\"logo\":null},{\"sid\":\"31\",\"play\":\"1\",\"type\":\"1\",\"sourceFrom\":\"\",\"logo\":null},{\"sid\":\"32\",\"play\":\"1\",\"type\":\"1\",\"sourceFrom\":\"\",\"logo\":null},{\"sid\":\"33\",\"play\":\"1\",\"type\":\"1\",\"sourceFrom\":\"\",\"logo\":null},{\"sid\":\"34\",\"play\":\"1\",\"type\":\"1\",\"sourceFrom\":\"\",\"logo\":null},{\"sid\":\"35\",\"play\":\"1\",\"type\":\"2\",\"sourceFrom\":\"\",\"logo\":null},{\"sid\":\"36\",\"play\":\"1\",\"type\":\"2\",\"sourceFrom\":\"\",\"logo\":null},{\"sid\":\"37\",\"play\":\"1\",\"type\":\"2\",\"sourceFrom\":\"\",\"logo\":null},{\"sid\":\"38\",\"play\":\"1\",\"type\":\"2\",\"sourceFrom\":\"\",\"logo\":null},{\"sid\":\"39\",\"play\":\"1\",\"type\":\"2\",\"sourceFrom\":\"\",\"logo\":null}],\"errCode\":0}");
    }

    public static void setConfig(String config) {
        put(SharePreferenceKeyParams.KEY_CONFIG, config);
    }

    public static boolean isReleaseAudioFocus() {
        return getBoolean(SharePreferenceKeyParams.KEY_RELEASE_AUDIO_FOCUS, true);
    }

    public static void setReleaseAudioFocus(boolean isRelease) {
        put(SharePreferenceKeyParams.KEY_RELEASE_AUDIO_FOCUS, isRelease);
    }

    public static PlayMode getMusicPlayModel() {
        return PlayMode.valueOf(getString(SharePreferenceKeyParams.KEY_MUSIC_PLAY_MODE, PlayMode.QUEUE_LOOP.name()));
    }

    public static void setMusicPlayMode(PlayMode playMode) {
        put(SharePreferenceKeyParams.KEY_MUSIC_PLAY_MODE, playMode.name());
    }

    public static boolean isOpenPush() {
        return getBoolean(SharePreferenceKeyParams.KEY_OPEN_PUSH, true);
    }

    public static void setOpenPush(boolean isopen) {
        put(SharePreferenceKeyParams.KEY_OPEN_PUSH, isopen);
    }

    public static boolean getDefaultShortPlayEnable() {
        return getBoolean(SharePreferenceKeyParams.KEY_DEFAULT_SHORT_PLAY_ENABLE, ConfigUtils.getInstance().isNeedShortPlayDefault());
    }

    public static boolean getDefaultPersSkinEnable() {
        return getBoolean(SharePreferenceKeyParams.KEY_PERSONALIZED_SKIN_ENABLE, false);
    }

    public static boolean getWakeupDefaultValue() {
        return getBoolean(SharePreferenceKeyParams.KEY_WAKEUP_DEFAULT_VALUE, ConfigUtils.getInstance().isNeedAsrDefault());
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


    public static boolean getNeedAsr() {
        return getBoolean(SharePreferenceKeyParams.KEY_APP_NEED_ASR, true);
    }

    // 设置是否需要全局唤醒词
    public static void setNeedAsr(boolean b) {
        put(SharePreferenceKeyParams.KEY_APP_NEED_ASR, b);
    }


    public static boolean isLocalSortByTime() {
        return getBoolean(SharePreferenceKeyParams.KEY_LOCAL_SORT_BY_TIME, true);
    }

    public static void setLocalSortByTime(boolean sortByTime) {
        put(SharePreferenceKeyParams.KEY_LOCAL_SORT_BY_TIME, sortByTime);
    }

    public static void setRecPageCache(String cache) {
        put(SharePreferenceKeyParams.KEY_REC_PAGE_CACHE, cache);
    }

    public static String getRecPageCache() {
        return getString(SharePreferenceKeyParams.KEY_REC_PAGE_CACHE, null);
    }

    public static void setMusicPageCache(String cache) {
        put(SharePreferenceKeyParams.KEY_MUSIC_PAGE_CACHE, cache);
    }

    public static String getMusicPageCache() {
        return getString(SharePreferenceKeyParams.KEY_MUSIC_PAGE_CACHE, null);
    }

    public static void setRadioPageCache(String cache) {
        put(SharePreferenceKeyParams.KEY_RADIO_PAGE_CACHE, cache);
    }

    public static String getRadioPageCache() {
        return getString(SharePreferenceKeyParams.KEY_RADIO_PAGE_CACHE, null);
    }

    public static int getDiskSpaceInsufficientTipCount() {
        return getInt(SharePreferenceKeyParams.KEY_DISK_SPACE_INSUFFICIENT_TIP_COUNT, 0);
    }

    public static void setDiskSpaceInsufficientTipCount(int count) {
        put(SharePreferenceKeyParams.KEY_DISK_SPACE_INSUFFICIENT_TIP_COUNT, count);
    }

    public static String getAiRadioLogoUrl() {
        return getString(SharePreferenceKeyParams.KEY_AI_RADIO_LOGO_CACHE, null);
    }

    public static void setAiRadioLogoUrl(String url) {
        put(SharePreferenceKeyParams.KEY_AI_RADIO_LOGO_CACHE, url);
    }

    public static String getRecommendLogoUrl() {
        return getString(SharePreferenceKeyParams.KEY_RECOMMEND_LOGO_CACHE, null);
    }

    public static void setRecommendLogoUrl(String url) {
        put(SharePreferenceKeyParams.KEY_RECOMMEND_LOGO_CACHE, url);
    }

    public static void setQrCodeInfoCache(String qrCodeInfo) {
        put(SharePreferenceKeyParams.KEY_QRCODE_INFO_CACHE, qrCodeInfo);
    }

    public static String getQrCodeInfoCache() {
        return getString(SharePreferenceKeyParams.KEY_QRCODE_INFO_CACHE, null);
    }

    public static boolean getIsPlay() {
        return getBoolean(SharePreferenceKeyParams.KEY_IS_PLAY, false);
    }

    public static void setIsPlay(boolean b) {
        put(SharePreferenceKeyParams.KEY_IS_PLAY, b);
    }

    public static boolean hasSyncFavourData() {
        return getBoolean(SharePreferenceKeyParams.KEY_HAS_SYNC_FAVOUR_DATA, false);
    }

    public static void setHasSyncFavourData(boolean b) {
        put(SharePreferenceKeyParams.KEY_HAS_SYNC_FAVOUR_DATA, b);
    }

    public static boolean hasSyncSubscribeData() {
        return getBoolean(SharePreferenceKeyParams.KEY_HAS_SYNC_SUBSCRIBE_DATA, false);
    }

    public static void setHasSyncSubscribeData(boolean b) {
        put(SharePreferenceKeyParams.KEY_HAS_SYNC_SUBSCRIBE_DATA, b);
    }

    // ----- comm method

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
        } else {
            editor.putString(key, object.toString());
        }
        editor.apply();
    }

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

    public static void setFloatUIType(int floatUIType) {
        put(SharePreferenceKeyParams.KEY_FLOAT_UI_TYPE, floatUIType);
    }

    public static int getFloatUIType() {
        return getInt(SharePreferenceKeyParams.KEY_FLOAT_UI_TYPE, 1);
    }

    public static void setShowWindowView(boolean showWindowView) {
        put(SharePreferenceKeyParams.KEY_SHOW_PUSH_WINDOW_VIEW, showWindowView);
    }

    public static boolean getShowWindowView() {
        return getBoolean(SharePreferenceKeyParams.KEY_SHOW_PUSH_WINDOW_VIEW, false);
    }

    /*
    public static final int PLAYER_MODE_SEQUENCE = 0;
    public static final int PLAYER_MODE_SINGLE_CIRCLE = 1;
    public static final int PLAYER_MODE_RANDOM = 2;

     */

    @Deprecated
    public static int getPlayMode() {
        return getInt(SharePreferenceKeyParams.KEY_PLAY_MODE, 0);
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
        String KEY_KAOLA_OPENID = "key_kaola_openid";
        String KEY_LOCALPATHS = "key_local_paths";
        String KEY_SDCARD_PATHS = "key_sdcard_paths";
        String KEY_SEARCHSIZE = "key_searchsize";
        String KEY_CONFIG = "key_config";
        String KEY_PLAY_MODE = "key_play_mode";
        String KEY_CURRENT_ALBUM_ID = "key_current_album_id";
        String KEY_IS_FIRST = "key_is_first";
        String KEY_AUDIO_SOUCRE = "key_audio_soucre";
        String KEY_IS_PLAY = "key_is_play";
        String KEY_IS_FIRST_TIP = "key_is_fist_tip";
        String KEY_APPFIRSTPLAY = "key_appfirstplay";
        String KEY_FATALEXIT = "key_fatalexit";
        String KEY_APP_NEED_ASR = "key_app_need_asr";
        String KEY_RELEASE_AUDIO_FOCUS = "key_release_audio_focus";
        String KEY_WAKEUP_PLAY = "key_wakeup_play";
        String KEY_Full_SCREEN = "key_full_screen";
        String KEY_OPEN_PUSH = "key_open_push"; // 是否打开点火智能播放
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

        String KEY_LOCAL_SORT_BY_TIME = "key_local_sort_by_time";   //本地音乐排序方式，默认按时间排序
        String KEY_MUSIC_PLAY_MODE = "key_music_play_model";   // 音乐专辑的播放模式，默认列表循环
        String KEY_PERSONALIZED_SKIN_ENABLE = "key_personalized_skin_enable";//记录个性化皮肤开启，默认打开
        String KEY_FLOAT_UI_TYPE = "key_float_ui_type";//悬浮窗设置界面的类别 ，1，2，3

        String KEY_REC_PAGE_CACHE = "key_rec_page_cache"; // 推荐页缓存
        String KEY_MUSIC_PAGE_CACHE = "key_music_page_cache"; // 音乐页缓存
        String KEY_RADIO_PAGE_CACHE = "key_radio_page_cache"; // 电台页缓存

        String KEY_SHOW_PUSH_WINDOW_VIEW = "key_show_push_window_view";//开机横幅是否展示过

        String KEY_CACHE_SIZE_LIMIT_TIP_COUNT = "key_cache_size_limit_tip_count"; // 缓存不足提示次数
        String KEY_DISK_SPACE_INSUFFICIENT_TIP_COUNT = "key_disk_space_insufficient_tip_count"; // 磁盘空间不足

        String KEY_AI_RADIO_LOGO_CACHE = "key_ai_radio_logo_cache"; // AI电台LOGO缓存路径
        String KEY_RECOMMEND_LOGO_CACHE = "key_recommend_logo_cache"; // 每日推荐LOGO缓存路径

        String KEY_QRCODE_INFO_CACHE = "key_qr_code_info_cache"; // 二维码信息缓存

        String KEY_HAS_SYNC_SUBSCRIBE_DATA = "key_has_sync_subscribe_data"; // 是否同步过订阅信息
        String KEY_HAS_SYNC_FAVOUR_DATA = "key_has_sync_favour_data"; // 是否同步过首次信息
    }

}

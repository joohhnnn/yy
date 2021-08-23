package com.txznet.txzsetting.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.txznet.sdk.TXZTtsManager;
import com.txznet.txzsetting.TXZApplication;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by ASUS User on 2017/6/5.
 */

public class SPThreshholdUtil {
    public static final String TAG = "nickhu";
    public static final String SP_KEY_TTSROLE = "setting_tts_role";
    public static final String SP_KEY_THRESHHOLD = "setting_threshhold";
    public static final String SP_KEY_WAKUP = "setting_wakup";
    public static final String SP_KEY_WAKUP_EXAMPLE = "setting_wakup_example";
    public static final String SP_KEY_WAKUP_NAME = "setting_wakup_name";
    public static final String SP_KEY_WELCOME = "setting_welcome";
    public static final String SP_KEY_FLOAT_TOOL = "setting_float_tool";

    public static final String SP_KEY_WAKEUP_COMMAND = "setting_wakeup_command";

    public static final String SP_NAME_SETTING = "txz_setting";

    public static final Float THRESHHOLD_NORMAL = 3.1f;
    public static final Float THRESHHOLD_HIGH_VERY = 4.0f;
    public static final Float THRESHHOLD_HIGH = 3.6f;
    public static final Float THRESHHOLD_LOW = 2.9f;
    public static final Float THRESHHOLD_LOW_VERY = 2.7f;

    public static final int TTS_ROLE_DEFAULT = 1;//默认
    public static final int TTS_ROLE_102 = 102;//志玲
    public static final int TTS_ROLE_104 = 104;//杨贵妃
    public static final int TTS_ROLE_105 = 105;//萌萌
    public static final int TTS_ROLE_106 = 106;//糖糖
    public static final int TTS_ROLE_10001 = 10001;//云知声男童
    public static final int TTS_ROLE_10003 = 10003;//国语女声
    public static final int TTS_ROLE_10004 = 10004;//甜甜
    public static final int TTS_ROLE_10005 = 10005;//BDbzn
    public static final int TTS_ROLE_10006 = 10006;//百度情感男
    public static final int TTS_ROLE_10701 = 10701;//天之眼女声

    public static void setSharedPreferencesData(Context context, String name, String key, int value) {
        SharedPreferences settings = TXZApplication.getApp().getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.apply();

    }


    public static void setSharedPreferencesData(Context context, String name, String key, float value) {
        SharedPreferences settings = TXZApplication.getApp().getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        editor.apply();

    }

    public static void setSharedPreferencesData(Context context, String name, String key, boolean value) {
        SharedPreferences settings = TXZApplication.getApp().getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void setSharedPreferencesData(Context context, String name, String key, String value) {
        SharedPreferences settings = TXZApplication.getApp().getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void setSharedPreferencesData(Context context, String name, String key, String[] value) {
        SharedPreferences settings = TXZApplication.getApp().getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        Set set = new HashSet<String>();
        for (int i = 0; i < value.length; i++) {
            set.add(value[i]);
        }
        editor.putStringSet(key, set);
        editor.apply();
    }

    /**
     * 获取显示的悬浮窗示例
     *
     * @param context 171204改为默认false-迪恩杰
     * @return
     */
    public static boolean getFloatToolExample(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME_SETTING,
                Activity.MODE_PRIVATE);
        boolean spFloatToolExample = sharedPreferences.getBoolean(SP_KEY_FLOAT_TOOL, true);
        return spFloatToolExample;
    }

    /**
     * 获取显示的唤醒词示例
     *
     * @param context
     * @return
     */
    public static String getWakupShowExample(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME_SETTING,
                Activity.MODE_PRIVATE);
        String spWakupShowExample = sharedPreferences.getString(SP_KEY_WAKUP_EXAMPLE, "您可以给我改名字哦");
        return spWakupShowExample;
    }

    /**
     * 获取保存的欢迎语
     *
     * @param context
     * @return
     */
    public static String getWelcomeData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME_SETTING,
                Activity.MODE_PRIVATE);
        String spWelcome = sharedPreferences.getString(SP_KEY_WELCOME, "默认");
        return spWelcome;
    }

    /**
     * 获取保存的唤醒词
     *
     * @param context
     * @return
     */
    @Nullable
    public static String[] getWakupNameData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME_SETTING,
                Activity.MODE_PRIVATE);

        Set spWakupName = sharedPreferences.getStringSet(SP_KEY_WAKUP_NAME, new HashSet<String>());
        Iterator it = spWakupName.iterator();
        Log.d(TAG, "spWakupName size = " + spWakupName.size());
        String[] stringWakupName = new String[spWakupName.size()];
        for (int i = 0; i < spWakupName.size(); i++) {
            if (it.hasNext()) {
                String str = it.next().toString();
                Log.d(TAG, "stringWakupName[" + i + "] =" + it.toString());
                stringWakupName[i] = str;

            }
        }
        if (spWakupName.size() == 0 || spWakupName == null)
            return null;
        it.remove();
        return stringWakupName;
    }

    /**
     * 获取存储的唤醒开关状态
     *
     * @param context
     * @return
     */
    public static boolean getWakupData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME_SETTING,
                Activity.MODE_PRIVATE);
        Boolean spWakup = sharedPreferences.getBoolean(SP_KEY_WAKUP, true);
        if (spWakup) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取存储的全局免唤醒指令开关状态
     *
     * @param context
     * @return
     */
    public static boolean getWakupCommandData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME_SETTING,
                Activity.MODE_PRIVATE);
        Boolean spWakup = sharedPreferences.getBoolean(SP_KEY_WAKEUP_COMMAND, true);
        if (spWakup) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取存储的阀值
     *
     * @param context
     * @return
     */
    public static String getThreshholdData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME_SETTING,
                Activity.MODE_PRIVATE);
        Float spThreshhold = sharedPreferences.getFloat(SP_KEY_THRESHHOLD, -3.1f);
        if (spThreshhold.equals(THRESHHOLD_HIGH)) {
            return "高";
        } else if (spThreshhold.equals(THRESHHOLD_HIGH_VERY)) {
            return "极高";
        } else if (spThreshhold.equals(THRESHHOLD_LOW)) {
            return "低";
        } else if (spThreshhold.equals(THRESHHOLD_LOW_VERY)) {
            return "极低";
        } else if (spThreshhold.equals(THRESHHOLD_NORMAL)) {
            return "正常";
        } else {
            Log.e(TAG, "sp float equals false");
            return "";
        }
    }

    /**
     * 获取存储的TTS
     *
     * @param context
     * @return
     */
    public static int getTtsRoleData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME_SETTING,
                Activity.MODE_PRIVATE);
        TXZTtsManager.TtsTheme[] ttsThemes = TXZTtsManager.getInstance().getTtsThemes();
        Integer spTts = 0;

        if (ttsThemes != null) {
            spTts = sharedPreferences.getInt(SP_KEY_TTSROLE, TTS_ROLE_DEFAULT);//如果不为null则默认值设为默认语音
        } else {
            spTts = sharedPreferences.getInt(SP_KEY_TTSROLE, 0);
        }

        int ttsrole = spTts.intValue();

        switch (ttsrole) {
            case TTS_ROLE_DEFAULT:
                return TTS_ROLE_DEFAULT;
            case TTS_ROLE_102:
                return TTS_ROLE_102;
            case TTS_ROLE_104:
                return TTS_ROLE_104;
            case TTS_ROLE_105:
                return TTS_ROLE_105;
            case TTS_ROLE_106:
                return TTS_ROLE_106;
            case TTS_ROLE_10001:
                return TTS_ROLE_10001;
            case TTS_ROLE_10003:
                return TTS_ROLE_10003;
            case TTS_ROLE_10004:
                return TTS_ROLE_10004;
            case TTS_ROLE_10005:
                return TTS_ROLE_10005;
            case TTS_ROLE_10006:
                return TTS_ROLE_10006;
            case TTS_ROLE_10701:
                return TTS_ROLE_10701;
            default:
                return 0;
        }

    }
}

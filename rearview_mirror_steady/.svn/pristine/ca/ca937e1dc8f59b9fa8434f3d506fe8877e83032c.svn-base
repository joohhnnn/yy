package com.txznet.webchat.sp;

import android.content.Context;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.sp.CommonSp;

import java.util.HashMap;

/**
 * 语音提示文本管理，管理语音播报提示内容
 * Created by J on 2016/7/23.
 */
public class TipManager extends CommonSp {
    public static final String PLACEHOLDER_ARG0 = "%arg0%";
    public static final String PLACEHOLDER_ARG1 = "%arg1%";
    public static final String PLACEHOLDER_ARG2 = "%arg2%";
    public static final String PLACEHOLDER_ARG3 = "%arg3%";

    private static final String SP_NAME = "webchat_tipmanager";

    private static final String UNDEFINED = "webchat_tip_undefined";
    // tts keys
    public static final String KEY_TIP_NEED_LOGIN = "WECHAT_TIP_NEED_LOGIN"; // 扫码提示
    public static final String KEY_TIP_LOGIN_SUCCESS = "WECHAT_TIP_LOGIN_SUCCESS"; // 登陆成功
    public static final String KEY_TIP_LOGIN_SUCCESS_TIP = "WECHAT_TIP_LOGIN_SUCCESS_TIP"; // 登录成功+首次登录提示
    public static final String KEY_TIP_LOGIN_SUCCESS_INTRO_AUTO_LOGIN = "KEY_TIP_LOGIN_SUCCESS_INTRO_AUTO_LOGIN"; // 登录成功, 引导设置自动登录
    public static final String KEY_TIP_LOGIN_FAILED = "WECHAT_TIP_LOGIN_FAILED"; // 登陆失败
    public static final String KEY_TIP_LOGIN_RESTORE_SUCCESS = "WECHAT_TIP_LOGIN_RESTORE_SUCCESS"; // 自动登陆成功
    public static final String KEY_TIP_LOGIN_RESTORE_FAILED = "WECHAT_TIP_LOGIN_RESTORE_FAILED"; // 自动登陆失败
    public static final String KEY_TIP_LOGOUT = "WECHAT_TIP_LOGOUT"; // 退出登录

    // ui keys
    public static final String KEY_TIP_UI_RECORD_COUNTDOWN_TEXT = "WECHAT_TIP_UI_RECORD_COUNTDOWN_TEXT"; // 录音界面倒计时文本

    // 默认的tips
    private HashMap<String, String> mDefaultTips = new HashMap<>();

    private static TipManager sInstance = new TipManager(GlobalContext.get(), SP_NAME);

    private TipManager(Context context, String spName) {
        super(context, spName);

        // init default tip values
        mDefaultTips.put(KEY_TIP_NEED_LOGIN, "您还未登录微信助手，请先登录");
        mDefaultTips.put(KEY_TIP_LOGIN_SUCCESS, "微信助手登陆成功");
        mDefaultTips.put(KEY_TIP_LOGIN_SUCCESS_TIP, "微信助手登陆成功, 您可以唤醒语音助手，然后说我要发微信给张三来发送微信。更多功能请查看新手指南");
        mDefaultTips.put(KEY_TIP_LOGIN_SUCCESS_INTRO_AUTO_LOGIN, "微信助手登录成功, 您可以前往设置开启开机登录功能");
        mDefaultTips.put(KEY_TIP_LOGIN_FAILED, "微信助手初始化发生异常");
        mDefaultTips.put(KEY_TIP_LOGIN_RESTORE_SUCCESS, "已主动帮您登录微信助手");
        mDefaultTips.put(KEY_TIP_LOGIN_RESTORE_FAILED, "主动帮您登录微信助手失败");
        mDefaultTips.put(KEY_TIP_LOGOUT, "微信助手已注销");
        mDefaultTips.put(KEY_TIP_UI_RECORD_COUNTDOWN_TEXT, "%arg0%秒后自动发送");
    }

    /**
     * 判断指定tip是否已被修改
     *
     * @return
     */
    public static boolean isTipModified(String key) {
        return UNDEFINED.equals(sInstance.getValue(key, UNDEFINED));
    }

    /**
     * 获取指定key的提示文本
     *
     * @param key 提示key
     * @return
     */
    public static String getTip(String key) {
        String tip = sInstance.getValue(key, UNDEFINED);

        if (tip.equals(UNDEFINED)) {
            return sInstance.mDefaultTips.get(key);
        }

        return tip;
    }

    /**
     * 设置提示文本
     *
     * @param key   提示key
     * @param value 文本内容，null表示恢复默认
     */
    public static void setTip(String key, String value) {
        if (sInstance.mDefaultTips.containsKey(key)) {
            if (null == value) {
                sInstance.setValue(key, UNDEFINED);
            } else {
                sInstance.setValue(key, value);
            }
        }
    }
}

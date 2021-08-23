package com.txznet.txzsetting.util;

import android.content.ContentResolver;

import com.txznet.sdk.TXZReportManager;
import com.txznet.txzsetting.TXZApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Created by nick on 2017/11/10.
 */

public class TxzReportUtil {
    public static final String KEY_CODE_BACK = "back";
    public static final String KEY_CODE_HOME = "home";
    public static final String DESTROY_UNKNOWN = "unknown";
    public static final String WELCOME_DEFAULT = "default";
    public static final String POI_MODE_LIST = "poi_mode_list";
    public static final String POI_MODE_BLEND = "poi_mode_blend";


    /**
     * 进入主界面上报
     */
    public static void doReportMain() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", "txz_setting");
            jsonObject.put("type", "main");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TXZReportManager.getInstance().doReport(jsonObject.toString());
    }

    /**
     * 退出设置上报
     * 返回键：back
     * home键：home
     */
    public static void doReportDestroy(String key) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", "txz_setting");
            jsonObject.put("type", "destroy");
            jsonObject.put("keycode", key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TXZReportManager.getInstance().doReport(jsonObject.toString());
    }

    /**
     * 上报唤醒词
     *
     * @param name 自定义唤醒词
     */
    public static void doReportWakeup(String name) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", "txz_setting");
            jsonObject.put("type", "wakeup");
            jsonObject.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TXZReportManager.getInstance().doReport(jsonObject.toString());
    }

    public static void doReportWakeupEnable(boolean enable) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", "txz_setting");
            jsonObject.put("type", "wakeup");
            jsonObject.put("enable", enable);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TXZReportManager.getInstance().doReport(jsonObject.toString());
    }

    /**
     * 欢迎语
     *
     * @param text
     */
    public static void doReportWelcome(String text) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", "txz_setting");
            jsonObject.put("type", "welcome");
            jsonObject.put("text", text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TXZReportManager.getInstance().doReport(jsonObject.toString());

    }

    public static void doReportPoiMapMode(String mode) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", "txz_setting");
            jsonObject.put("type", "poi_map_mode");
            jsonObject.put("mode", mode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TXZReportManager.getInstance().doReport(jsonObject.toString());

    }

    public static void doReportThreshhold(double old_threshhold, double new_threshhold) {

        old_threshhold = getThreshhold(old_threshhold);
        new_threshhold = getThreshhold(new_threshhold);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", "txz_setting");
            jsonObject.put("type", "threshhold");
            jsonObject.put("num_old", old_threshhold);
            jsonObject.put("num_new", new_threshhold);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TXZReportManager.getInstance().doReport(jsonObject.toString());
    }

    private static double getThreshhold(double threshhold) {
        BigDecimal bg = new BigDecimal(threshhold).setScale(2, BigDecimal.ROUND_HALF_UP);
        threshhold = bg.doubleValue();
        return threshhold;
    }
}

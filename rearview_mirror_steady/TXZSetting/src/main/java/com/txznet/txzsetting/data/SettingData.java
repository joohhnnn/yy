package com.txznet.txzsetting.data;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import com.txznet.txzsetting.util.JsonIntentUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by ASUS User on 2017/6/19.
 */

public class SettingData implements Serializable {
    public static final String TAG = SettingData.class.getSimpleName();
    private double threshhold = 0;
    private String[] wakeupWords = null;
    private boolean wakeupEnable = true;
    private String welcomeTest = null;
    private int poiMode = 0;
//    private Boolean tencentIsuse = null;
//
//    public void setTencentIsuse (Boolean isuse){
//        this.tencentIsuse = isuse;
//    }

    public void setPoiMode(int poiMode) {
        this.poiMode = poiMode;
    }

    public int getPoiMode() {
        return poiMode;
    }

    public void setThreshhold(Double threshhold) {
        this.threshhold = threshhold;
    }

    public double getThreshhold() {
        return threshhold;
    }

    public void setWakeupWords(String[] wakeupWords) {
        this.wakeupWords = wakeupWords;
    }

    public String[] getWakeupWords() {
        return wakeupWords;
    }

    public void setWakeupEnable(boolean wakeupEnable) {
        this.wakeupEnable = wakeupEnable;
    }

    public boolean isWakeupEnable() {
        return wakeupEnable;
    }

    public void setWelcomeTest(String welcomeTest) {
        this.welcomeTest = welcomeTest;
    }

    public String getWelcomeTest() {
        return welcomeTest;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public String toString() {
        String toStringTest = "";
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            if (wakeupWords != null) {
                for (int i = 0; i < wakeupWords.length; i++) {
                    jsonArray.put(wakeupWords[i]);
                }
                jsonObject.put(JsonIntentUtil.JSON_WAKEUP_WORDS, jsonArray);
            }
            if (welcomeTest != null) {
                jsonObject.put(JsonIntentUtil.JSON_WELCOME_TEXT, welcomeTest);
            }
            if (threshhold != 0) {
                BigDecimal bg = new BigDecimal(threshhold).setScale(2, BigDecimal.ROUND_HALF_UP);
                threshhold = bg.doubleValue();
                Log.d(TAG, "threshhold = " + threshhold);
                jsonObject.put(JsonIntentUtil.JSON_THRESHHOLD, threshhold);
            }

            jsonObject.put(JsonIntentUtil.JSON_WAKEUP_ENABLE, wakeupEnable);

            if (poiMode != 0) {
                jsonObject.put(JsonIntentUtil.JSON_POI_MAP_MODE, poiMode);
            }
//            if (tencentIsuse != null) {
//                jsonObject.put(JsonIntentUtil.JSON_ENGINE_TENCENT_ISUSE, tencentIsuse);
//            }
            toStringTest = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "toString =" + toStringTest);
        return toStringTest;
    }
}

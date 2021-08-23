package com.txznet.launcher.component.nav;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import java.util.List;

/**
 * Created by TXZ-METEORLUO on 2018/2/7.
 */

public abstract class NavAppComponent implements INav {
    public interface OnNavStateListener {
        void onForebackGround(boolean isFocus);

        void onNavState(boolean isNaving);

        void onNavEnter();

        void onNavExit();
    }

    public interface HUDUpdateListener {
        void onHudUpdate(HUDInfo info);
    }

    public static class HUDInfo {
        public String navPkn;
        public String dirDes; // 如左转右转
        public int limitSpeed;// 道路限速 km/h
        public int cameraSpeed;// 电子眼限速
        public int dirDistance;// 多少米后
        public String curRoad;// 当前道路
        public String nextRoad;// 下一条道路

        public String destName;// 目的地
        public int remainTime;//剩余时间
        public int remainDistance;//剩余距离

        @Override
        public String toString() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("navPkn", navPkn);
                jsonObject.put("dirDes", dirDes);
                jsonObject.put("limitSpeed", limitSpeed);
                jsonObject.put("cameraSpeed", cameraSpeed);
                jsonObject.put("dirDistance", dirDistance);
                jsonObject.put("curRoad", curRoad);
                jsonObject.put("nextRoad", nextRoad);
                jsonObject.put("destName", destName);
                jsonObject.put("remainTime", remainTime);
                jsonObject.put("remainDistance", remainDistance);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject.toString();
        }

        public static HUDInfo fromString(String jsonData) {
            if (TextUtils.isEmpty(jsonData)) {
                return null;
            }
            HUDInfo info = new HUDInfo();
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                info.navPkn = jsonObject.optString("navPkn");
                info.dirDes = jsonObject.optString("dirDes");
                info.limitSpeed = jsonObject.optInt("limitSpeed");
                info.cameraSpeed = jsonObject.optInt("cameraSpeed");
                info.dirDistance = jsonObject.optInt("dirDistance");
                info.curRoad = jsonObject.optString("curRoad");
                info.nextRoad = jsonObject.optString("nextRoad");
                info.destName = jsonObject.optString("destName");
                info.remainTime = jsonObject.optInt("remainTime");
                info.remainDistance = jsonObject.optInt("remainDistance");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return info;
        }
    }

    private HUDInfo mInfo;
    private HUDUpdateListener mListener;
    protected OnNavStateListener mStateListener;

    public void setHUDInfoListener(HUDUpdateListener listener) {
        mListener = listener;
    }

    public void setNavStateListener(OnNavStateListener listener) {
        this.mStateListener = listener;
    }

    public HUDInfo getHUDInfo() {
        return mInfo;
    }

    public void onHudInfoUpdate(HUDInfo info) {
        mInfo = info;
        if (mListener != null) {
            mListener.onHudUpdate(info);
        }
    }
}
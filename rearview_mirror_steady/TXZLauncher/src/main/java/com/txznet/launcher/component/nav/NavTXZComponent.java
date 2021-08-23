package com.txznet.launcher.component.nav;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.launcher.domain.settings.SettingsManager;
import com.txznet.sdk.TXZNavManager;
import com.txznet.sdk.TXZResourceManager;
import com.txznet.sdk.TXZSceneManager;
import com.txznet.sdk.bean.Poi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by TXZ-METEORLUO on 2018/2/7.
 * 这个类封装了接收导航状态还有地址下发的接受
 */

public class NavTXZComponent extends NavAppComponent implements PoiIssuedTool {
    private boolean mIsFocus;
    private boolean mIsInNav;
    private boolean mIsExited;
    private String mNavPkg;

    private OnPoiIssuedListener mPoiIssuedListener;

    @Override
    public void init() {
        TXZNavManager.getInstance().setNavStatusListener(new TXZNavManager.NavStatusListener() {
            @Override
            public void onEnter(String packageName) {
                if (mStateListener != null) {
                    mStateListener.onNavEnter();
                }
            }

            @Override
            public void onExit(String packageName) {
                mIsExited = true;
                if (mStateListener != null) {
                    mStateListener.onNavExit();
                }
            }

            @Override
            public void onBeginNav(String packageName, Poi poi) {
            }

            @Override
            public void onStart(String navPkg) {
                mIsInNav = true;
                checkIsInNav();
            }

            @Override
            public void onEnd(String navPkg) {
                mIsInNav = false;
                checkIsInNav();
                parseNavInfo(null);
            }

            @Override
            public void onForeground(String packageName, boolean isForeground) {
                mIsFocus = isForeground;
                checkIsInFocus();
            }

            @Override
            public void onStatusUpdate(String pkn) {
            }

            @Override
            public void onDefaultNavHasSeted(String pkn) {
            }
        });

        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String info = intent.getStringExtra("KEY_NAVI_INFO");
                LogUtil.logd("txz nav recv:" + info);
                if (!TextUtils.isEmpty(info)) {
                    parseNavInfo(info);
                }
            }
        }, new IntentFilter("com.txznet.txz.NAVI_INFO"));

        TXZSceneManager.getInstance().setSceneTool(TXZSceneManager.SceneType.SCENE_TYPE_WECHAT, new TXZSceneManager.SceneTool() {
            @Override
            public boolean process(TXZSceneManager.SceneType type, String data) {
                if (type == TXZSceneManager.SceneType.SCENE_TYPE_WECHAT) {
                    try {
                        JSONBuilder builder = new JSONBuilder(data);
                        String t= builder.getVal("type",String.class);
                        // 这里收到下发地址的通知
                        if("navigate".equals(t)) {
                            SettingsManager.getInstance().ctrlScreen(true);
                            onNavigateInfoNotify(builder);
                            return true;
                        }

                        if ("history".equals(builder.getVal("action",String.class))) {
                            TXZResourceManager.getInstance().speakTextOnRecordWin("暂不支持该操作!", true, null);
                            return true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
    }

    /**
     * jb.put("toolPKN", toolPKN);
     * jb.put("direction", direction);
     * jb.put("dirDes", dirDes);
     * jb.put("dirDistance", dirDistance);
     * jb.put("dirTime", dirTime);
     * jb.put("remainDistance", remainDistance);
     * jb.put("remainTime", remainTime);
     * jb.put("carDir", carDir);
     * jb.put("longitude", longitude);
     * jb.put("latitude", latitude);
     * jb.put("totalDistance", totalDistance);
     * jb.put("totalTime", totalTime);
     * jb.put("currentLimitedSpeed", currentLimitedSpeed);
     * jb.put("currentRoadName", currentRoadName);
     * jb.put("nextRoadName", nextRoadName);
     * jb.put("currentRoadType", currentRoadType);
     * jb.put("currentSpeed", currentSpeed);
     * jb.put("hasArrive", hasArrive);
     *
     * @param navJson
     */
    private void parseNavInfo(String navJson) {
        try {
            if (TextUtils.isEmpty(navJson)) {
                onHudInfoUpdate(null);
                return;
            }

            JSONObject obj = new JSONObject(navJson);
            if (obj != null) {
                HUDInfo info = new HUDInfo();
                info.navPkn = obj.optString("toolPKN");
                info.curRoad = obj.optString("currentRoadName");
                info.nextRoad = obj.optString("nextRoadName");
                info.limitSpeed = (int) obj.optLong("currentLimitedSpeed");
                info.dirDes = obj.optString("dirDes");
                info.dirDistance = (int) obj.optLong("dirDistance");
                info.remainTime = (int) obj.optLong("remainTime");
                info.remainDistance = (int) obj.optLong("remainDistance");
                info.destName = obj.optString("destName");
                onHudInfoUpdate(info);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通知了导航目的地
     *
     * @param info
     */
    private void onNavigateInfoNotify(JSONBuilder info) {
        if (info == null) {
            return;
        }

        IssuedPoi item = convert(info);
        if (mPoiIssuedListener != null) {
            mPoiIssuedListener.onPoiIssued(item);
        }
    }

    private IssuedPoi convert(JSONBuilder builder) {
        String name = builder.getVal("name", String.class);
        String addr = builder.getVal("addr", String.class);
        String city = builder.getVal("city", String.class);
        double lat = builder.getVal("lat", double.class);
        double lng = builder.getVal("lng", double.class);
        String msg = builder.getVal("msg", String.class);

        IssuedPoi poi = new IssuedPoi();
        poi.source = "同行者车车互联微信公众号";
        poi.lat = lat;
        poi.lng = lng;
        poi.poiName = name;
        poi.poiAddress = addr;
        poi.message = msg;
        return poi;
    }

    private void checkIsInNav() {
        if (mStateListener != null) {
            mStateListener.onNavState(mIsInNav);
        }
    }

    private void checkIsInFocus() {
        if (mStateListener != null) {
            mStateListener.onForebackGround(mIsFocus);
        }
    }

    @Override
    public boolean isFocus() {
        return mIsFocus && !mIsExited;
    }

    @Override
    public boolean isInNav() {
        return mIsInNav && !mIsExited;
    }

    @Override
    public boolean isBackgroundRunning() {
        return mIsExited;
    }

    @Override
    public void reqPois() {
    }

    @Override
    public void setPoiIssuedListener(OnPoiIssuedListener listener) {
        mPoiIssuedListener = listener;
    }
}
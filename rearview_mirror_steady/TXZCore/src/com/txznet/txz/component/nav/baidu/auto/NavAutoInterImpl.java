package com.txznet.txz.component.nav.baidu.auto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.nav.IMapInterface;
import com.txznet.txz.module.app.PackageManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by TXZ-METEORLUO on 2018/10/25.
 */

public class NavAutoInterImpl implements IMapInterface {
    private String navPkg;
    private AutoControlInterface mInterface;
    private AutoControlInterface.RecvListener mListener;

    public NavAutoInterImpl(AutoControlInterface.RecvListener listener) {
        mListener = listener;
    }

    @Override
    public void initialize() {
        mInterface = new AutoControlInterface(navPkg);
        mInterface.setRecvListener(mListener);

        regTestRecv();
    }

    @Override
    public void enterNav() {
        PackageManager.getInstance().openApp(navPkg);
    }

    @Override
    public void setPackageName(String pkn) {
        navPkg = pkn;
    }

    @Override
    public void zoomAll(Runnable run) {
        try {
            mInterface.sendRequest("showFullRoute", null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AppLogic.removeBackGroundCallback(autoResume);
        AppLogic.runOnBackGround(autoResume, 10000);
    }

    Runnable autoResume = new Runnable() {
        @Override
        public void run() {
            backNavi();
        }
    };

    @Override
    public void appExit() {
        try {
            mInterface.sendRequest("exitApp", null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void naviExit() {
        try {
            mInterface.sendRequest("exitNav", null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void zoomMap(boolean isZoomin) {
        try {
            String cmd = isZoomin ? "zoomIn" : "zoomOut";
            mInterface.sendRequest(cmd, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void switchLightNightMode(boolean isLight) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("isLightModel", isLight);
            mInterface.sendRequest("switchLightNightMode", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????????????????
     */
    public void switchAutoMode() {
        try {
            mInterface.sendRequest("autoLightNightMode", null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void switchTraffic(boolean isShowTraffic) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("isON", isShowTraffic);
            mInterface.sendRequest("switchTraffic", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void switch23D(boolean is2D, int val) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("is2d", is2D);
            mInterface.sendRequest("switchTo2D", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void switchCarDirection() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("is2d", false);
            mInterface.sendRequest("switchTo2D", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void switchNorthDirection() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("is2d", true);
            mInterface.sendRequest("switchTo2D", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * prefer???int
     * 0???????????????
     * 1???????????????
     * 2???????????????
     * 3???????????????
     *
     * @param ps
     */
    @Override
    public void switchPlanStyle(PlanStyle ps) {
        JSONObject jsonObject = new JSONObject();
        try {
            int prefer = 0;
            if (ps == PlanStyle.BUZOUGAOSU) {
                prefer = 3;
            } else if (ps == PlanStyle.DUOBIYONGDU) {
                prefer = 1;
            } else if (ps == PlanStyle.GAOSUYOUXIAN) {
                prefer = 2;
            } else if (ps == PlanStyle.TUIJIAN) {
                prefer = 0;
            }
            jsonObject.put("prefer", prefer);
            mInterface.sendRequest("switchPreferInNav", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void backNavi() {
        try {
            mInterface.sendRequest("resumeNavi", null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void switchBroadcastRole(int role) {

    }

    @Override
    public void navigateTo(String name, double lat, double lng, int planStyle) {
        mInterface.naviTo(name, "", lat, lng);
    }

    @Override
    public void frontTraffic() {

    }

    @Override
    public void switchSimpleMode(boolean isOpen) {

    }

    @Override
    public void queryCollectionPoint() {

    }

    @Override
    public void intoTeam() {

    }

    /**
     * ????????????????????????????????????
     */
    public void queryNavState(String sessionId) {
        try {

            mInterface.sendRequest(sessionId, "isNavigating", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????????????????????????????
     */
    public void queryNavFocusState(String sessionId) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("isForeground", true);
            mInterface.sendRequest(sessionId, "isForeground", jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????????????????
     * 1 ???????????????
     * 0 ??? ????????????
     */
    public void switchBroadcastMode(int mode) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("switchVoiceModeStatus", mode);
            mInterface.sendRequest("switchVoiceModeStatus", jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????/??????????????????
     */
    public void switchCruiserStatus(boolean isOpen) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("switchCruiserStatus", isOpen);
            mInterface.sendRequest("switchCruiserStatus", jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ???????????????LatLng
     */
    public void queryDestGps() {
        try {
            mInterface.sendRequest("getNaviDesCoordinate", null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * ?????????????????????
     */
    public void queryDestName() {
        try {
            mInterface.sendRequest("getNaviDesName", null);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * ??????????????????
     *
     * @throws JSONException
     */
    public void nextTurnDistance(String sessionId) {
        mInterface.sendRequest(sessionId, "nextTurnDistance", null);
    }

    /**
     * ???????????????
     *
     * @throws JSONException
     */
    public void nextRoad(String sessionId) {
        mInterface.sendRequest(sessionId, "aheadRoadName", null);
    }

    /**
     * ??????????????????
     * @throws JSONException
     */
    public void remainTime(String sessionId) {
        mInterface.sendRequest(sessionId, "remainLeftTime", null);
    }

    /**
     * ??????????????????
     */
    public void remainDistance(String sessionId) {
        mInterface.sendRequest(sessionId, "remainLeftDistance", null);
    }

    /**
     * ????????????
     */
    public void startNav() {
        try {
            mInterface.sendRequest("enterNav", null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param i
     */
    public void selectPlanRoute(int i) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("index", i); // ????????????0
            mInterface.sendRequest("switchRoute", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????
     * <p>
     * 0:  "?????????"
     * 1: "?????????"
     * 2:  "?????????
     * 3: "??????ATM???
     * 4: "???????????????
     *
     * @param keywords
     */
    public void searchWay(String keywords) {
        int i = 0;
        if (keywords.contains("?????????")) {
            i = 0;
        } else if (keywords.contains("?????????") || keywords.contains("?????????")) {
            i = 1;
        } else if (keywords.contains("??????")) {
            i = 2;
        } else if (keywords.contains("??????") || keywords.contains("ATM")) {
            i = 3;
        } else if (keywords.contains("??????")) {
            i = 4;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("searchType", i);
            mInterface.sendRequest("naviSearchAlongWay", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void regTestRecv() {
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int action = intent.getIntExtra("action", -1);
                switch (action) {
                    case 1:
                        enterNav();
                        break;
                    case 2:
                        zoomAll(null);
                        break;
                    case 3:
                        appExit();
                        break;
                    case 4:
                        naviExit();
                        break;
                    case 5:
                        zoomMap(true);
                        break;
                    case 6:
                        zoomMap(false);
                        break;
                    case 7:
                        switchLightNightMode(true);
                        break;
                    case 8:
                        switchLightNightMode(false);
                        break;
                    case 9:
                        switchAutoMode();
                        break;
                    case 10:
                        switchTraffic(true);
                        break;
                    case 11:
                        switchTraffic(false);
                        break;
                    case 12:
                        switch23D(true, 0);
                        break;
                    case 13:
                        switch23D(false, 0);
                        break;
                    case 14:
                        switchPlanStyle(PlanStyle.BUZOUGAOSU);
                        break;
                    case 15:
                        switchPlanStyle(PlanStyle.DUOBIYONGDU);
                        break;
                    case 16:
                        switchPlanStyle(PlanStyle.GAOSUYOUXIAN);
                        break;
                    case 17:
                        switchPlanStyle(PlanStyle.TUIJIAN);
                        break;
                    case 18:
                        backNavi();
                        break;
                    case 19:
                        queryNavState(984616+"");
                        break;
                    case 20:
                        queryDestGps();
                        break;
                    case 21:
                        queryDestName();
                        break;
                    case 22:
                        startNav();
                        break;
                    case 23:
                        selectPlanRoute(1);
                        break;
                    case 24:
                        searchWay("?????????");
                        break;
                    case 25:
                        searchWay("?????????");
                        break;
                    case 26:
                        searchWay("??????");
                        break;
                    case 27:
                        searchWay("??????");
                        break;
                    case 28:
                        searchWay("??????");
                        break;
                    case 29:
                        queryNavFocusState(123456789 + "");
                        break;
                    case 30:
                        // ????????????
                        switchBroadcastMode(1);
                        break;
                    case 31:
                        // ????????????
                        switchBroadcastMode(0);
                        break;
                    case 32:
                        // ????????????
                        switchCruiserStatus(true);
                        break;
                    case 33:
                        // ????????????
                        switchCruiserStatus(false);
                        break;
                }
            }
        }, new IntentFilter("baiduauto.test"));
    }
}
package com.txznet.txz.component.nav.baidu.auto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.ui.win.nav.BDLocationUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

/**
 * Created by TXZ-METEORLUO on 2018/10/24.
 */

public class AutoControlInterface {
    public static final String REQUEST_ACTION =
            "com.baidu.baidumaps.opencontrol.ACTION.REQUEST";
    public static final String SERVICE_KEY_PACKAGE_NAME = "package_name";
    public static final String SERVICE_KEY_SDK_VERSION = "version";
    public static final String SERVICE_KEY_ACTION = "content";

    public static String BAIDU_MAP_PACKAGE = "com.baidu.BaiduMap.auto";
    public static final String SDK_SERVICE_CLASS = "com.baidu.mapframework.opencontrol.service.OpenControlService";
    public static final String SDK_VERSION = "1.0.0";

    //调用者包名称，调用服务成功后，会回调调用者的 KEY_CALLER_PACK_NAME + ".BDEntryService" 服务
//    public static final String KEY_CALLER_PACK_NAME = "com.baidu.testDemon.opencontrol";
    // 此处为消息标识，回调消息时，会返回相同的transaction，用于做消息的对应
    public static final String SERVICE_KEY_TRANSACTION = "transaction";
    // 调用接口类型标识,具体看下表
    public static final String KEY_FUNCTION = "method";
    // 调用接口传参,具体看下表
    public static final String KEY_PARAMS = "param";

    public AutoControlInterface(String navPkg) {
        BAIDU_MAP_PACKAGE = navPkg;
        regRecv();
    }

    private RecvListener mRecvListener;

    public void setRecvListener(RecvListener listener) {
        mRecvListener = listener;
    }

    public String getPackageName() {
        return BAIDU_MAP_PACKAGE;
    }

    public String sendRequest(String function, JSONObject param) throws JSONException {
        JSONObject jsonAction = new JSONObject();
        jsonAction.put(KEY_FUNCTION, function);
        if (param != null) {
            jsonAction.put(KEY_PARAMS, param);
        }
        String action = jsonAction.toString();
        String sessionId = "t" + System.currentTimeMillis() + function;

        Intent intent = new Intent();
        intent.setAction(REQUEST_ACTION);
        intent.setPackage(BAIDU_MAP_PACKAGE);
        intent.setClassName(BAIDU_MAP_PACKAGE, SDK_SERVICE_CLASS);
        intent.putExtra(SERVICE_KEY_PACKAGE_NAME, "com.txznet.txz");
        intent.putExtra(SERVICE_KEY_SDK_VERSION, SDK_VERSION);
        intent.putExtra(SERVICE_KEY_ACTION, action);
        intent.putExtra(SERVICE_KEY_TRANSACTION, sessionId);
        //在安卓8.0以上，百度无法正常开启服务，需要适配不同得到服务开启方式
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                Method startForegroundService = Context.class.getDeclaredMethod("startForegroundService",Intent.class);
                startForegroundService.setAccessible(true);
                startForegroundService.invoke(GlobalContext.get(), intent);
            } catch (Exception e) {
                LogUtil.loge("baidu startForegroundService", e);
                try {
                    GlobalContext.get().startService(intent);
                }catch (Exception e1) {
                    LogUtil.loge("baidu startService", e1);
                }
            }
        } else {
            GlobalContext.get().startService(intent);
        }
        LogUtil.logd("sendRequest func:" + function + ",param:" + (param != null ? param.toString() : "null"));
        return sessionId;
    }

    public void sendRequest(String sessionId, String function, JSONObject param) {
        JSONObject jsonAction = new JSONObject();
        try {
            jsonAction.put(KEY_FUNCTION, function);
            if (param != null) {
                jsonAction.put(KEY_PARAMS, param);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String action = jsonAction.toString();
        Intent intent = new Intent();
        intent.setAction(REQUEST_ACTION);
        intent.setPackage(BAIDU_MAP_PACKAGE);
        intent.setClassName(BAIDU_MAP_PACKAGE, SDK_SERVICE_CLASS);
        intent.putExtra(SERVICE_KEY_PACKAGE_NAME, "com.txznet.txz");
        intent.putExtra(SERVICE_KEY_SDK_VERSION, SDK_VERSION);
        intent.putExtra(SERVICE_KEY_ACTION, action);
        intent.putExtra(SERVICE_KEY_TRANSACTION, sessionId);
        GlobalContext.get().startService(intent);
        LogUtil.logd("sendRequest func:" + function + ",param:" + (param != null ? param.toString() : "null"));
    }

    public void naviTo(String name, String address, double lat, double lng) {
        double[] gps = BDLocationUtil.Convert_GCJ02_To_BD09(lat, lng);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("longitude", gps[1]);
            jsonObject.put("latitude", gps[0]);
            jsonObject.put("destName", name);
            jsonObject.put("prefer", 0);
            sendRequest("navToLoc", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    ///////////////////////////////////////////////////////
    //action 对应业务模块
    // method 对应相应功能
    public final static String NOTIFY_OBSERVER_ACTION_NAVI_STATUE = "com.baidu.map.auto.NOTIFY.ACTION_NAVI_STATUE"; // 通知消息的主键，导航状态
    public final static String NOTIFY_OBSERVER_ACTION_NAVI_INDUCED = "com.baidu.map.auto.NOTIFY.ACTION_NAVI_INDUCUD"; // 通知消息的主键，诱导
    public final static String NOTIFY_OBSERVER_ACTION_MAP_INFO = "com.baidu.map.auto.NOTIFY.ACTION_MAP_INFO"; // 通知消息的主键，地图信息
    public final static String NOTIFY_OBSERVER_ACTION_ROUTE_INFO = "com.baidu.map.auto.NOTIFY.ACTION_ROUTE_INFO"; // 路线规划相关信息

    private void regRecv() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFY_OBSERVER_ACTION_MAP_INFO);
        filter.addAction(NOTIFY_OBSERVER_ACTION_NAVI_STATUE);
        filter.addAction(NOTIFY_OBSERVER_ACTION_NAVI_INDUCED);
        filter.addAction(NOTIFY_OBSERVER_ACTION_ROUTE_INFO);
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mRecvListener != null) {
                    mRecvListener.onReceive(context, intent);
                }
            }
        }, filter);
        LogUtil.logd("regAutoCv");

        /*
         * 2021/07/31
         * 【鸿泉金龙】项目中，对应的广播状态的返回需要打开对应的开关才会返回
         */
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("isON",true);
            jsonObject.put("mode",20);
            sendRequest("turnOnSwitch",jsonObject);
            jsonObject.put("isON",true);
            jsonObject.put("mode",22);
            sendRequest("turnOnSwitch",jsonObject);
            jsonObject.put("isON",true);
            jsonObject.put("mode",21);
            sendRequest("turnOnSwitch",jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static interface RecvListener {
        void onReceive(Context context, Intent intent);
    }
}
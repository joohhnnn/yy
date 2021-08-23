package com.txznet.webchat.actions;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.txznet.loader.AppLogic;
import com.txznet.webchat.BuildConfig;
import com.txznet.webchat.Constant;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.helper.WxNetworkHelper;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.WxServerConfigBean;
import com.txznet.webchat.util.UidUtil;
import com.txznet.webchat.util.WxMonitorUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信服务器配置下发相关ActionCreator
 * Created by J on 2017/7/13.
 */

public class WxServerConfigActionCreator {
    private static final String LOG_TAG = "serverConfig";

    /**
     * 自动更新服务器配置基础时间间隔(默认6小时)
     */
    private static final long INTERVAL_CHECK_SERVER_CONFIG_UPDATE_BASE = 6 * 60 * 1000 * 60;

    /**
     * 自动更新服务器配置时间波动范围(4小时)
     * <p>
     * 每次延迟更新服务器配置时采用的推迟时间为 [基础时间间隔 + 波动范围], 即每次更新服务器
     * 配置后, 下次更新配置的时间间隔为 [6 ~ 10] 小时, 采用波动间隔是因为方案商一般会在
     * 固定时间点重启设备, 采用固定间隔会导致所有设备在每天的同一时间点进行服务器配置更新.
     * 可能会导致一服务器问题, 且无法规避服务器短时间不可用等风险.
     */
    private static final long INTERVAL_CHECK_SERVER_CONFIG_UPDATE_ADD = 4 * 60 * 1000 * 60;

    /**
     * 更新服务器配置失败时重试的时间间隔(默认2分钟)
     */
    private static final long INTERVAL_CHECK_SERVER_CONFIG_UPDATE_RETRY = 1000 * 60 * 2;

    private HashMap<String, String> mMapCheckUpdateHeaders;
    private HashMap<String, String> mMapCheckUpdateParams;

    // single instance
    private static WxServerConfigActionCreator sInstance;

    public static WxServerConfigActionCreator getInstance() {
        if (null == sInstance) {
            synchronized (WxServerConfigActionCreator.class) {
                if (null == sInstance) {
                    sInstance = new WxServerConfigActionCreator();
                }
            }
        }

        return sInstance;
    }

    private WxServerConfigActionCreator() {
        mMapCheckUpdateHeaders = new HashMap<>(1);
        mMapCheckUpdateHeaders.put("Content-Type", "application/x-www-form-urlencoded");
        mMapCheckUpdateParams = new HashMap<>(2);
    }
    // eof single instance

    /**
     * 从服务器更新微信配置
     */
    public void updateConfigFromServer() {
        // 获取uid后开始网络请求
        UidUtil.getInstance().getTXZUID(new UidUtil.UidCallback() {
            @Override
            public void onSuccess(final String uid) {
                performUpdateConfigRequest(uid);
            }
        });
    }

    private void performUpdateConfigRequest(final String uid) {
        StringRequest request = new StringRequest(Request.Method.POST, Constant
                .URL_WX_SERVER_CONFIG_UPDATE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        WxServerConfigBean bean = null;
                        try {
                            bean = JSON.parseObject(s, WxServerConfigBean.class);
                        } catch (Exception e) {
                            L.e(LOG_TAG, "convert config bean failed: " + e.toString());
                            WxMonitorUtil.doMonitor(WxMonitorUtil
                                    .WX_SERVER_CONFIG_UPDATE_FAILED_JSON);
                        }

                        if (null == bean || null == bean.wxConfig) {
                            retryUpdateConfigFromServer();
                            WxMonitorUtil.doMonitor(WxMonitorUtil
                                    .WX_SERVER_CONFIG_UPDATE_FAILED_JSON);
                            return;
                        }

                        L.d(LOG_TAG, "update server config success, new config = " + bean.wxConfig);
                        mRetryUpdateConfigCount = 0;
                        Dispatcher.get().dispatch(new Action<>(ActionType
                                .WX_SERVER_CONFIG_CHANGED, bean.wxConfig));
                        WxMonitorUtil.doMonitor(WxMonitorUtil.WX_SERVER_CONFIG_UPDATE_SUCCESS);

                        // 通知WxPluginActionCreator检查插件更新
                        WxPluginActionCreator.getInstance().checkPluginUpdate(bean.wxConfig.plugin);

                        scheduleUpdateConfigFromServer();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        L.e(LOG_TAG, "update server config encountered network error: " +
                                volleyError.toString());
                        retryUpdateConfigFromServer();
                        WxMonitorUtil.doMonitor(WxMonitorUtil
                                .WX_SERVER_CONFIG_UPDATE_FAILED_NETWORK);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                L.d(LOG_TAG, "generating request params, uid = " + uid + ", clientVersion = " +
                        BuildConfig.VERSION_NAME);
                mMapCheckUpdateParams.put("uid", uid);
                mMapCheckUpdateParams.put("clientVersion", BuildConfig.VERSION_NAME);
                return mMapCheckUpdateParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return mMapCheckUpdateHeaders;
            }
        };
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 0));
        WxNetworkHelper.getInstance().doRequest(request);
    }

    private int mRetryUpdateConfigCount = 0;

    private void retryUpdateConfigFromServer() {
        if (mRetryUpdateConfigCount++ < 3) {
            L.d(LOG_TAG, "retrying update server config, current times = " + mRetryUpdateConfigCount);
            AppLogic.removeBackGroundCallback(mUpdateServerConfigTask);
            AppLogic.runOnBackGround(mUpdateServerConfigTask, INTERVAL_CHECK_SERVER_CONFIG_UPDATE_RETRY);
            return;
        }

        L.e(LOG_TAG, "update server config failed for 3 times, stop retrying");
        mRetryUpdateConfigCount = 0;
        scheduleUpdateConfigFromServer();
    }

    private void scheduleUpdateConfigFromServer() {
        // // TODO: 2017/7/20 PLUGIN 根据休眠状态决定是否需要停止更新配置
        AppLogic.removeBackGroundCallback(mUpdateServerConfigTask);
        long nextCheckInterval = INTERVAL_CHECK_SERVER_CONFIG_UPDATE_BASE +
                (long) (Math.random() * INTERVAL_CHECK_SERVER_CONFIG_UPDATE_ADD);
        AppLogic.runOnBackGround(mUpdateServerConfigTask, nextCheckInterval);
        L.d(LOG_TAG, "scheduling update server config after " + nextCheckInterval + "ms");
    }

    private Runnable mUpdateServerConfigTask = new Runnable() {
        @Override
        public void run() {
            updateConfigFromServer();
        }
    };
}

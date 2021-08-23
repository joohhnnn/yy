package com.txznet.txz.component.poi.remote;

import android.os.SystemClock;
import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGaodeWebImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.ui.win.nav.BDLocationUtil;

import org.json.JSONObject;

public class PoiSearchToolRemoteImpl implements TXZPoiSearchManager.PoiSearchTool {

    private static String mRemoteService = null;
    private static TXZPoiSearchManager.PoiSearchResultListener mResultListener = null;
    private static int mSessionId = 0;
    private static TXZPoiSearchManager.PoiConfig mPoiConfig;

    private static Runnable mSearchTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            JNIHelper.logd(mRemoteService + " remote poi tool search timeout");
            TXZPoiSearchManager.PoiSearchResultListener listener = mResultListener;
            mResultListener = null;
            if (listener != null) {
                MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_REMOTE);
                listener.onError(TXZPoiSearchManager.ERROR_CODE_TIMEOUT, "远程工具超时");
            }
        }
    };
    private static ServiceManager.ConnectionListener mConnectionListener = new ServiceManager.ConnectionListener() {
        @Override
        public void onConnected(String serviceName) {

        }

        @Override
        public void onDisconnected(String serviceName) {
            // 监听适配程序连接断开
            if (serviceName.equals(mRemoteService)) {
                JNIHelper.logd(mRemoteService + " remote poi tool ondisconnected");
                AppLogic.removeBackGroundCallback(mSearchTimeoutRunnable);

                setRemoteService(null);
                TXZPoiSearchManager.PoiSearchResultListener listener = mResultListener;
                mResultListener = null;
                if (listener != null) {
                    MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_REMOTE);
                    listener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW, "远程工具连接断开");
                }
            }
        }
    };

    public static boolean useRemoteTool(boolean offline) {
        if (TextUtils.isEmpty(mRemoteService)) {
            return false;
        }
        if (mPoiConfig == null) {
            return false;
        }
        boolean usable = false;
        switch (mPoiConfig.netMode) {
            case NET_MODE_OFFLINE:
                usable = offline;
                break;
            case NET_MODE_ONLINE:
                usable = !offline;
                break;
            case NET_MODE_ALL:
                usable = true;
                break;
        }
        return usable;
    }

    public static boolean isUseInnerTool() {
        // 是否是用内部搜索工具
        if (null != mPoiConfig) {
            return  mPoiConfig.isUseOption;
        }
        return true;
    }

    public static void setRemoteService(String serviceName) {
        synchronized (PoiSearchToolRemoteImpl.class) {
            mRemoteService = serviceName;
            JNIHelper.logd("update remote poi tool service: " + mRemoteService);
            if (mRemoteService != null) {
                ServiceManager.getInstance().sendInvoke(mRemoteService, "", null, null);
            }
        }
    }

    public static byte[] procRemoteResponse(String serviceName, String command,
                                            byte[] data) {
        if ("setTool".equals(command)) {
            mPoiConfig = TXZPoiSearchManager.PoiConfig.parse(data);
            ServiceManager.getInstance().addConnectionListener(mConnectionListener);
            setRemoteService(serviceName);
            return null;
        } else if ("clearTool".equals(command)) {
            ServiceManager.getInstance().removeConnectionListener(mConnectionListener);
            setRemoteService(null);
            return null;
        }

        if (!serviceName.equals(mRemoteService)) {
            return null;
        }
        if (null == mResultListener) {
            return null;
        }
        async_processRmtResponse(command, data);
        return null;
    }

    private static void async_processRmtResponse(final String command,
                                                 final byte[] data) {
        if (data == null) {
            return;
        }
        Runnable oRun = new Runnable() {

            @Override
            public void run() {
                JSONBuilder builder = new JSONBuilder(data);
                if (builder.getVal("id", Integer.class, -1) != mSessionId) {
                    return;
                }
                if ("result".equals(command)) {
                    AppLogic.removeBackGroundCallback(mSearchTimeoutRunnable);
                    mSearchTime = SystemClock.elapsedRealtime() - mSearchTime;
                    NavManager.reportBack("remoteImpl", mSearchTime);

                    TXZPoiSearchManager.PoiResult result = TXZPoiSearchManager.PoiResult.parse(
                            builder.getVal("result", JSONObject.class));
                    if (result == null) {
                        MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_REMOTE);
                        mResultListener.onError(
                                TXZPoiSearchManager.ERROR_CODE_UNKNOW, "remote poi tool search failed");
                        return;
                    }
                    JNIHelper.logd("POISearchLog:remoteImpl onPoiSearched errorCode  = " + result.errorCode );
                    if (result.errorCode != TXZPoiSearchManager.PoiResult.ERROR_CODE_SUCCESS) {
                        switch (result.errorCode) {
                            case TXZPoiSearchManager.PoiResult.ERROR_CODE_EMPTY:
                                MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_REMOTE);
                                mResultListener.onError(
                                        TXZPoiSearchManager.ERROR_CODE_EMPTY, result.errDesc);
                                break;
                            case TXZPoiSearchManager.PoiResult.ERROR_CODE_TIMEOUT:
                                MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_REMOTE);
                                mResultListener.onError(
                                        TXZPoiSearchManager.ERROR_CODE_TIMEOUT, result.errDesc);
                                break;
                            case TXZPoiSearchManager.PoiResult.ERROR_CODE_UNKNOW:
                            default:
                                MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_REMOTE);
                                mResultListener.onError(
                                        TXZPoiSearchManager.ERROR_CODE_UNKNOW, result.errDesc);
                                break;
                        }
                        return;
                    }
                    if (result.pois == null || result.pois.size() == 0) {
                        MonitorUtil
                                .monitorCumulant(MonitorUtil.POISEARCH_EMPTY_REMOTE);
                        mResultListener.onError(
                                TXZPoiSearchManager.ERROR_CODE_EMPTY, result.errDesc);
                    } else {
                        for (Poi poi : result.pois) {
                            poi.setDistance(BDLocationUtil.calDistance(poi.getLat(), poi.getLng()));
                        }
                        MonitorUtil
                                .monitorCumulant(MonitorUtil.POISEARCH_SUCCESS_REMOTE);
                        JNIHelper.logd("POISearchLog: remoteImpl return Poi count= " + result.pois.size());
                        NavManager.getInstance().setNomCityList(result.pois);
                        PoiSearchToolGaodeWebImpl.getPoisCity(/*mOption.getTimeout() - mSearchTime*/3000, mResultListener, result.pois);
                    }
                }
            }
        };
        AppLogic.runOnBackGround(oRun, 0);
    }

    /**
     * 执行次数，保留暂不用
     */
    private int mRetryCount = -1;
    private static long mSearchTime = 0;

    @Override
    public TXZPoiSearchManager.SearchReq searchInCity(TXZPoiSearchManager.CityPoiSearchOption option, TXZPoiSearchManager.PoiSearchResultListener listener) {
        return search(new JSONBuilder(), option, listener);
    }

    @Override
    public TXZPoiSearchManager.SearchReq searchNearby(TXZPoiSearchManager.NearbyPoiSearchOption option, TXZPoiSearchManager.PoiSearchResultListener listener) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("centerLatitude", option.getCenterLat());
        jsonBuilder.put("centerLongitude", option.getCenterLng());
        jsonBuilder.put("radius", option.getRadius());
        jsonBuilder.put("hasCenter", true);
        return search(jsonBuilder, option, listener);
    }

    private TXZPoiSearchManager.SearchReq search(final JSONBuilder jsonBuilder, TXZPoiSearchManager.CityPoiSearchOption option, TXZPoiSearchManager.PoiSearchResultListener listener) {
        if (mRemoteService == null) {
            listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "没有远程工具");
            return new TXZPoiSearchManager.SearchReq() {
                @Override
                public void cancel() {

                }
            };
        }
        MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL, MonitorUtil.POISEARCH_ENTER_REMOTE);
        AppLogic.removeBackGroundCallback(mSearchTimeoutRunnable);
        synchronized (PoiSearchToolRemoteImpl.class) {
            mSessionId++;
            mResultListener = listener;
        }
        final int id = mSessionId;
        jsonBuilder.put("id", id);
        jsonBuilder.put("num", option.getNum());
        jsonBuilder.put("timeout", option.getTimeout());
        jsonBuilder.put("keyword", option.getKeywords());
        jsonBuilder.put("city", option.getCity());
        jsonBuilder.put("region", option.getRegion());
        jsonBuilder.put("useCurrentCity", option.isUseCurrentCity());

        if (option.getSearchInfo() != null && mRetryCount == -1) {
            mRetryCount = option.getSearchInfo().getPoiRetryCount();
        }
        mSearchTime = SystemClock.elapsedRealtime();
        sendInvoke("tool.poi.search", jsonBuilder.toBytes(), null);
        // 增加 500 毫秒的延时，避免 AIDL 阻塞耗时
        AppLogic.runOnBackGround(mSearchTimeoutRunnable, option.getTimeout() + 500);
        return new TXZPoiSearchManager.SearchReq() {
            @Override
            public void cancel() {
                mResultListener = null;
                JSONBuilder jsonBuilder1 = new JSONBuilder();
                jsonBuilder1.put("id", id);
                AppLogic.removeBackGroundCallback(mSearchTimeoutRunnable);
                sendInvoke("tool.poi.cancel", jsonBuilder1.toBytes(), null);
            }
        };
    }

    @Override
    public void stopPoiSearchTool(int disShowPoiType) {
//        sendInvoke("tool.poi.stop", null, null);
        // 如果设置了远程工具，必须等远程工具结果不能取消
    }

    @Override
    public int getPoiSearchType() {
        return 0;
    }

    private int sendInvoke(String command, byte[] data, ServiceManager.GetDataCallback callback) {
        return ServiceManager.getInstance().sendInvoke(mRemoteService, command, data, callback);
    }
}

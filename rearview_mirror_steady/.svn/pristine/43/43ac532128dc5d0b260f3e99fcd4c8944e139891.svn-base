package com.txznet.music.helper;

import android.os.SystemClock;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.txz.ui.audio.UiAudio;
import com.txznet.comm.err.Error;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.comm.util.StringUtils;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.config.Configuration;
import com.txznet.music.service.impl.NetCommand;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 跟core的网络请求工具
 */
public class TXZNetRequest {
    private static final String TAG = Constant.LOG_TAG_NET + ":NetReq";

    private static final class Holder {
        private static final TXZNetRequest INSTANCE = new TXZNetRequest();
    }

    /**
     * 不需要回话关系的统一索引值
     */
    private static final int NEEDNT_ID = Integer.MAX_VALUE;
    private static final int CHECK_TIME_OUT_INTERVAL = 1000;
    private static int sRequestId = new Random().nextInt();
    private ConcurrentHashMap<Integer, RequestWrapper> mRequestArray = new ConcurrentHashMap<>();

    public interface RequestRawCallBack {
        void onResponse(UiAudio.Resp_DataInterface resp_dataInterface);

        void onError(String cmd, Error error);
    }

    public static abstract class RequestCallBack<T> implements RequestRawCallBack {

        public Class<T> tClazz;
        private UiAudio.Resp_DataInterface responseData;

        public RequestCallBack(Class<T> tClazz) {
            this.tClazz = tClazz;
        }

        public abstract void onResponse(T data);

        @Override
        public void onResponse(UiAudio.Resp_DataInterface respDataInterface) {
            AppLogic.runOnBackGround(() -> {
                responseData = respDataInterface;
                String data = new String(responseData.strData);
                T response = getResponse(responseData.strCmd, data);
                if (response != null) {
                    onResponse(response);
                } else {
                    Logger.e(TAG, "response  is  null,because json error???");
                }
            });
        }

        public T getResponse(String cmd, String data) {
            T response = null;
            if (StringUtils.isNotEmpty(data)) {
                if (tClazz == String.class) {
                    response = (T) data;
                } else {
                    try {
                        response = JsonHelper.fromJson(data, tClazz);
                    } catch (Exception e) {
                        response = null;
                        onError(cmd, new Error(ErrCode.ERROR_JSON_PARSER, "json解析错误:" + e.toString(), "服务器繁忙,请稍后重试"));
                        Logger.e(TAG, "请求" + cmd + ",发生json解析异常 " + e.toString());
                    }
                }
            }
            return response;
        }
    }

    /**
     * 检测请求队列中超时的任务
     */
    private final Runnable mCheckTimeoutTask = new Runnable() {
        @Override
        public void run() {
            AppLogic.removeBackGroundCallback(this);
            for (int key : mRequestArray.keySet()) {
                RequestWrapper requestWrapper = mRequestArray.get(key);
                if (requestWrapper == null) {
                    mRequestArray.remove(key);
                    continue;
                }
                if (SystemClock.elapsedRealtime() - requestWrapper.getRequestTime() > requestWrapper.getTimeOut()) {
                    Logger.e(TAG, "time out " + requestWrapper.getDataInterface().strCmd + " id:" + requestWrapper.getDataInterface().uint32Seq);
                    mRequestArray.remove(key);
                    RequestRawCallBack callBack = requestWrapper.getCallBack();
                    if (null != callBack) {
                        callBack.onError(requestWrapper.getDataInterface().strCmd,
                                new Error(ErrCode.ERROR_CLIENT_NET_TIMEOUT));
                    }
                }
            }
            AppLogic.runOnBackGround(this, CHECK_TIME_OUT_INTERVAL);
        }
    };

    private TXZNetRequest() {
        AppLogic.runOnBackGround(mCheckTimeoutTask, CHECK_TIME_OUT_INTERVAL);
    }

    public static TXZNetRequest get() {
        return Holder.INSTANCE;
    }

//    public int sendSeqRequestToCore(String url, Object obj, RequestRawCallBack callBack) {
//        String json = JsonHelper.toJson(obj);
//        return sendSeqRequestToCore(url, json.getBytes(), callBack);
//    }

    public int sendSeqRequestToCore(final String url, byte[] reqData, final RequestRawCallBack callBack) {
        return sendSeqRequestToCore(NetCommand.mStrDataInterface, url, reqData, Configuration.DefVal.DEFAULT_TIME_OUT, callBack);
    }

    public int sendSeqRequestToCore(final String url, byte[] reqData, long timeout, final RequestRawCallBack callBack) {
        return sendSeqRequestToCore(NetCommand.mStrDataInterface, url, reqData, timeout, callBack);
    }

    public int sendSeqRequestToCore(String coreCmd, final String url, byte[] reqData, long timeout, final RequestRawCallBack callBack) {
        int newRequestId = getNewRequestId();

        UiAudio.Req_DataInterface reqDataInterface = new UiAudio.Req_DataInterface();
        reqDataInterface.strCmd = url;
        reqDataInterface.strData = reqData;
        reqDataInterface.uint32Seq = newRequestId;
        Logger.e(TAG, " url=" + url + ", request id:" + newRequestId + ",params=" + (reqData == null ? "null" : new String(reqData, 0, reqData.length > 500 ? 500 : reqData.length)));
        if (!NetworkUtil.isNetworkAvailable(GlobalContext.get()) && null != callBack) {
            AppLogic.runOnBackGround(() -> callBack.onError(url, new Error(ErrCode.ERROR_CLIENT_NET_OFFLINE)));
            return newRequestId;
        }

        RequestWrapper requestWrapper = new RequestWrapper(reqDataInterface, timeout, callBack);
        mRequestArray.put(newRequestId, requestWrapper);

        NetCommand.getInstance().request(coreCmd, MessageNano.toByteArray(reqDataInterface));
        return newRequestId;
    }


    private int getNewRequestId() {
        return ++sRequestId;
    }

    public byte[] handleDataInterface(byte[] data) {
        UiAudio.Resp_DataInterface dataInterface = null;
        try {
            dataInterface = UiAudio.Resp_DataInterface.parseFrom(data);
        } catch (InvalidProtocolBufferNanoException e) {
            Logger.e(TAG, e);
        }

        if (null == dataInterface || null == dataInterface.uint32Seq) {
            // TODO: 2017/6/16 网络请求出错
            Logger.d(TAG, "handle data interface error:data or seq id empty");
            return null;
        }
        dispatchResponse(dataInterface);
        return new byte[0];
    }


    private void dispatchResponse(final UiAudio.Resp_DataInterface dataInterface) {
        AppLogic.runOnBackGround(() -> {
            String strData = new String(dataInterface.strData);
            int retCode = 0;
            try {
                JSONObject jsonObject = new JSONObject(strData);
                if (jsonObject.has("errCode")) {
                    retCode = jsonObject.getInt("errCode");
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Logger.e(TAG, e.toString());
            }

            RequestWrapper requestWrapper = mRequestArray.remove(dataInterface.uint32Seq);
            Logger.d(TAG, dataInterface.strCmd + " request wrapper is null?" + (requestWrapper == null) + " id:" + dataInterface.uint32Seq);
            RequestRawCallBack callBack = null;
            if (null != requestWrapper) {
                callBack = requestWrapper.getCallBack();
//                    mRequestArray.remove(dataInterface.uint32Seq);
            } else {
                Logger.i(TAG, " id:" + dataInterface.uint32Seq + " cmd:" + dataInterface.strCmd + " request wrapper is null");
            }

            if (0 != dataInterface.uint32ErrCode || 0 != retCode) {
                Logger.e(TAG, " cmd:" + dataInterface.strCmd + " errorCode:" + dataInterface.uint32ErrCode + " retCode:" + retCode);
            }

            if (null != callBack) {
                if (dataInterface.uint32ErrCode != 0) {
                    callBack.onError(dataInterface.strCmd, new Error(ErrCode.ERROR_CORE_RESP_WRONG, "core_resp error, uint32ErrCode=" + dataInterface.uint32ErrCode, "Core响应异常"));
                } else if (retCode != 0) {
                    callBack.onError(dataInterface.strCmd, new Error(ErrCode.ERROR_SVR_RESP_WRONG, "svr_resp error, retCode=" + retCode, "服务器响应异常"));
                } else {
                    Logger.eLarge(TAG, "response url=" + dataInterface.strCmd + ", result=" + new String(dataInterface.strData));
                    callBack.onResponse(dataInterface);
                }
            } else {
                Logger.d(TAG, " id:" + dataInterface.uint32Seq + " cmd:" + dataInterface.strCmd + " callback is null");
            }
        }, 0);
    }
}

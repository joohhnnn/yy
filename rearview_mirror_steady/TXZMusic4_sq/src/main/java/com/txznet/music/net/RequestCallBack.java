package com.txznet.music.net;

import com.txz.ui.audio.UiAudio;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.StringUtils;

/**
 * Created by brainBear on 2017/6/16.
 */

public abstract class RequestCallBack<T> extends RequestRawCallBack {

    public Class<T> tClazz;
    private UiAudio.Resp_DataInterface responseData;

    public RequestCallBack(Class<T> tClazz) {
        this.tClazz = tClazz;
    }

    public abstract void onResponse(T data);

    @Override
    public void onResponse(UiAudio.Resp_DataInterface respDataInterface) {
        responseData = respDataInterface;
        String data = new String(responseData.strData);
        T response = getResponse(responseData.strCmd, data);
        if (response != null) {
            onResponse(response);
        } else {
            LogUtil.loge(NetManager.TAG + "response  is  null,because json error???");
        }
    }

    public T getResponse(String cmd, String data) {
        T response = null;
        if (StringUtils.isNotEmpty(data)) {
            if (tClazz == String.class) {
                response = (T) data;
            } else {
                try {
                    response = JsonHelper.toObject(tClazz, data);
                } catch (Exception e) {
                    response = null;
                    onError(cmd, new Error(Error.ERROR_CLIENT_JSON_PARSER, "json解析错误:" + e.toString(), "服务器繁忙,请稍后重试"));
                    Logger.e(NetManager.TAG, "请求" + cmd + ",发生json解析异常 " + e.toString());
                }
            }
        }
        return response;
    }
}

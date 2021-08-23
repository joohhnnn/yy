package com.txznet.proxy;

import android.support.v4.util.ArraySet;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.proxy.server.response.MediaResponseBase;

import java.util.Set;

public class ProxySession {
    public String tag;
    public String[] oriUrls;
    public String proxyUrl;
    public ProxyParam param;
    public Set<MediaResponseBase> responses = new ArraySet<>();
    public long len; //文件大小，用于做拖动

    public void addResponse(MediaResponseBase res) {
        synchronized (responses) {
            responses.add(res);
        }
    }

    public void cancelAllResponse() {
        LogUtil.logw("cancelAllResponse start");
        synchronized (responses) {
            for (MediaResponseBase response : responses) {
                LogUtil.logw("cancelAllResponse " + hashCode());
                response.cancel();
            }
            responses.clear();
        }
        LogUtil.logw("cancelAllResponse end");
    }

    public String getLogId() {
        return "" + this.hashCode();
    }

    @Override
    public String toString() {
        return hashCode() + "-" + tag;
    }
}

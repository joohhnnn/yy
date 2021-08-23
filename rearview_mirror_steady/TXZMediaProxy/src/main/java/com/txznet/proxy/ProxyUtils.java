package com.txznet.proxy;


import com.txznet.proxy.server.LocalMediaServer;

import java.util.Random;

/**
 * 本地代理工具
 */
public class ProxyUtils {

    /**
     * 获取代理Url
     *
     * @param oriUrls 目标资源的url(含候选url)长度至少为1
     * @param param   代理参数
     * @return 代理地址
     */
    public static String getProxyUrl(String[] oriUrls, ProxyParam param) {
        ProxySession session = new ProxySession();
        String proxyUrl = "http://127.0.0.1:" + LocalMediaServer.getInstance().getPort()
                + "/" + session.hashCode() + "?s=" + session.hashCode() + "&r="
                + new Random().nextInt();
        session.oriUrls = oriUrls;
        session.proxyUrl = proxyUrl;
        session.param = param;
        session.tag = param.tag;
        SessionManager.get().addSession(session);
        return proxyUrl;
    }

    /**
     * 获取代理服务器端口
     */
    public static int getProxyPort() {
        return LocalMediaServer.getInstance().getPort();
    }
}
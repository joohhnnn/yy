package com.txznet.proxy.server.response;

import com.txznet.proxy.ProxySession;

import junit.framework.Assert;

import java.net.Socket;

public class MediaResponseFactory {
    public static MediaResponseBase createResponse(Socket socket, ProxySession sess, String method, long from, long to) {

        // 中断
        if (!"head".equalsIgnoreCase(method)) {
            sess.cancelAllResponse();
        }

        Assert.assertTrue("oriUrl can't empty", sess.oriUrls.length > 0);

//        return new HttpMediaResponse(sess, method, from, to);

        //判断第一个参数是否是http开头，true 则为需要网络请求
        if (sess.oriUrls[0].startsWith("http")) {
            return new HttpMediaResponse(socket, sess, method, from, to);
        } else {
            return new FileMediaResponse(socket, sess, method, from, to);
        }
    }
}

package com.txznet.proxy.server;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.proxy.BuildConfig;
import com.txznet.proxy.ProxySession;
import com.txznet.proxy.SessionManager;
import com.txznet.proxy.server.response.MediaResponseFactory;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

/**
 * 基于NanoHTTPD改写的服务器
 */
public class LocalMediaServer extends NanoHTTPD {

    private static LocalMediaServer sInstance;

    static {
        try {
            sInstance = new LocalMediaServer();
        } catch (Exception e) {
        }
    }

    public static LocalMediaServer getInstance() {
        return sInstance;
    }

    private LocalMediaServer() throws IOException {
        super(25555, new File("."));
    }

    @Override
    public Response serve(Socket socket, String uri, String method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        try {
            long from = -1;
            long to = -1;
            int sessionId = 0;
            // 解析header，获取from和to，Range: bytes=11100-12184830
            try {
                if (header != null) {
                    for (String key : header.keySet()) {
                        if ("range".equalsIgnoreCase(key)) {
                            String[] rangVal = header.get(key).substring(6).split("-");
                            from = Long.parseLong(rangVal[0]);
                            if (rangVal.length > 1) {
                                to = Long.parseLong(rangVal[1]);
                            }
                        }
                    }
                }
                sessionId = Integer.parseInt(uri.substring(1));
            } catch (Exception e) {
                LogUtil.loge("media session request error: " + e.getMessage());
                return new Response(NanoHTTPD.HTTP_BADREQUEST, MIME_AUDIO, e.getMessage());
            }
            ProxySession session = SessionManager.get().getSession(sessionId);

            if (session == null) {
                LogUtil.loge("media session not found: " + sessionId);
                return new Response(NanoHTTPD.HTTP_BADREQUEST, MIME_AUDIO,
                        "media session not found: " + sessionId);
            }

            Thread.currentThread().setName("MediaSession#" + session);

            if (BuildConfig.DEBUG) {
                LogUtil.logd("media session[" + session + "] create method: " + method + ", from/to=" + from
                        + "/" + to + "\r\n headers=" + header);
            } else {
                LogUtil.logd("media session[" + session + "] create method: " + method + ", from/to=" + from
                        + "/" + to);
            }
            return MediaResponseFactory.createResponse(socket, session, method, from, to);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.serve(socket, uri, method, header, parms, files);
    }

    public int getPort() {
        return myTcpPort;
    }
}

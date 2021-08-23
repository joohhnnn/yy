package com.txznet.audio.player.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
    private HttpUtil() {
    }

    /**
     * 获取网络文件大小
     */
    public static long getFileLength(String downloadUrl) throws IOException {
        if (downloadUrl == null || "".equals(downloadUrl)) {
            return 0L;
        }
        URL url = new URL(downloadUrl);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; U; Android 2.3.7; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
            return (long) conn.getContentLength();
        } catch (IOException e) {
            return 0L;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}

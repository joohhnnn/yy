package com.txznet.webchat.plugin.preset.logic.util;

import android.text.TextUtils;

import com.txznet.webchat.comm.plugin.utils.PluginLogUtil;
import com.txznet.webchat.plugin.preset.logic.http.SerializableHttpCookie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 可被序列化的CookieStore，用于处理微信网络请求身份校验与登录数据的持久化
 * Created by J on 2017/3/22.
 */

public class SerializableCookieStore implements CookieStore, Serializable {
    private static final String LOG_TAG = "SerializableCookieStore";

    private ConcurrentHashMap<String, ConcurrentHashMap<String, SerializableHttpCookie>> mCookies;

    private CookieChangeListener mCookieChangeListener;

    public SerializableCookieStore() {
        mCookies = new ConcurrentHashMap<>();
    }

    public void setCookieChangeListener(CookieChangeListener listener) {
        this.mCookieChangeListener = listener;
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
        if (null == cookie) {
            PluginLogUtil.e(LOG_TAG, "cookie is null");
        }

        PluginLogUtil.d(LOG_TAG, "add: uri = " + uri.getPath() + ", cookie = " + cookie.getName() + ", " + cookie.getValue());
        // Save cookie, or remove if expired
        if (!cookie.hasExpired()) {
            if (!mCookies.containsKey(cookie.getDomain()))
                mCookies.put(cookie.getDomain(), new ConcurrentHashMap<String, SerializableHttpCookie>());
            mCookies.get(cookie.getDomain()).put(cookie.getName(), new SerializableHttpCookie(cookie));
            notifyCookieUpdated(cookie);
        } else {
            if (mCookies.containsKey(cookie.getDomain())) {
                notifyCookieDomainRemove(cookie.getDomain());
                mCookies.get(cookie.getDomain()).remove(cookie.getDomain());
            }
        }
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        ArrayList<HttpCookie> ret = new ArrayList<HttpCookie>();
        for (String key : mCookies.keySet()) {
            if (uri.getHost().contains(key)) {
                for (SerializableHttpCookie cookie : mCookies.get(key).values()) {
                    ret.add(cookie.getCookie());
                }
            }
        }
        return ret;
    }

    @Override
    public List<HttpCookie> getCookies() {
        ArrayList<HttpCookie> ret = new ArrayList<HttpCookie>();
        for (String key : mCookies.keySet()) {
            for (SerializableHttpCookie cookie : mCookies.get(key).values()) {
                ret.add(cookie.getCookie());
            }
        }

        return ret;
    }

    @Override
    public List<URI> getURIs() {
        ArrayList<URI> ret = new ArrayList<>();
        for (String key : mCookies.keySet())
            try {
                ret.add(new URI(key));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        return ret;
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        ConcurrentHashMap<String, SerializableHttpCookie> cookieMap = mCookies.get(cookie.getDomain());
        if (null != cookieMap) {
            for (String key : cookieMap.keySet()) {
                if (cookie.getName().equals(cookieMap.get(key).getCookie().getName())) {
                    notifyCookieRemove(cookie);
                    cookieMap.remove(key);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean removeAll() {
        mCookies.clear();
        return true;
    }

    public String encodeStr() {
        if (null == mCookies || mCookies.isEmpty()) {
            return "";
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(mCookies);
        } catch (IOException e) {
            PluginLogUtil.e(LOG_TAG, "IOException while serializing Cookie: " + e.toString());
            return null;
        }

        return byteArrayToHexString(os.toByteArray());
    }

    public void decodeStr(String str) {
        if (TextUtils.isEmpty(str)) {
            return;
        }

        byte[] bytes = hexStringToByteArray(str);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            mCookies = ((ConcurrentHashMap<String, ConcurrentHashMap<String, SerializableHttpCookie>>) objectInputStream.readObject());
        } catch (IOException e) {
            PluginLogUtil.d(LOG_TAG, "IOException in decodeCookie" + e);
        } catch (ClassNotFoundException e) {
            PluginLogUtil.d(LOG_TAG, "ClassNotFoundException in decodeCookie" + e);
        } catch (ClassCastException e) {
            PluginLogUtil.d(LOG_TAG, "ClassCastException in decodeCookie" + e);
        }

        // 反序列化失败重新初始化cookieStore
        if (null == mCookies) {
            mCookies = new ConcurrentHashMap<String, ConcurrentHashMap<String, SerializableHttpCookie>>();
        }
    }

    /**
     * 将Cookie序列化后的byte数组转换为hex string, 便于持久化存储
     *
     * @param bytes byte array to be converted
     * @return string containing hex values
     */
    private String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte element : bytes) {
            int v = element & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    /**
     * 将指定hex string还原为byte数组, 用于Cookie反序列化
     *
     * @param hexString string of hex-encoded values
     * @return decoded byte array
     */
    private byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    private void notifyCookieUpdated(HttpCookie cookie) {
        if (null != mCookieChangeListener) {
            mCookieChangeListener.onCookieUpdated(cookie);
        }
    }

    private void notifyCookieDomainRemove(String domainName) {
        ConcurrentHashMap<String, SerializableHttpCookie> cookieMap = mCookies.get(domainName);
        if (null != cookieMap) {
            for (String key : cookieMap.keySet()) {
                notifyCookieRemove(cookieMap.get(key).getCookie());
            }
        }
    }

    private void notifyCookieRemove(HttpCookie cookie) {
        if (null != mCookieChangeListener) {
            mCookieChangeListener.onCookieRemove(cookie);
        }
    }

    public interface CookieChangeListener {
        /**
         * Cookie被更新时调用, 注意可能有某些cookie被连续设置为同样值的情况, 此时onCookieUpdated
         * 仍旧会被调用, 如果需要在指定Cookie确定改变时执行某些逻辑, 需要自行判断
         * <p>
         * 该方法会在Cookie更新之后调用
         *
         * @param cookie
         */
        void onCookieUpdated(HttpCookie cookie);

        /**
         * Cookie被移除时调用
         * <p>
         * 该方法会在Cookie被移除之前调用
         */
        void onCookieRemove(HttpCookie cookie);
    }
}

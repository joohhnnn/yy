package com.txznet.webchat.plugin.preset.logic.util;

import android.os.Looper;
import android.text.TextUtils;
import android.util.LruCache;

import com.txznet.webchat.comm.plugin.utils.PluginLogUtil;
import com.txznet.webchat.comm.plugin.utils.PluginMonitorUtil;
import com.txznet.webchat.plugin.preset.logic.consts.MonitorConsts;
import com.txznet.webchat.plugin.preset.logic.model.PoiInfo;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 微信地址反解工具
 * Created by J on 2016/8/25.
 */
public class LocationDecodeUtil {
    private static final String LOG_TAG = "LocationDecodeUtil";
    private static LruCache<String, PoiInfo> mDecodeCache;

    public interface DecodeCallback {
        void onCallback(PoiInfo info);
    }

    public static void decodeLocation(final JSONObject json, final DecodeCallback callback) {
        final String url;
        try {
            url = json.getString("Url");
        } catch (Exception e) {
            callback.onCallback(null);
            PluginLogUtil.e(LOG_TAG, "decodeLocation: resolve url encountered error: " + json);
            return;
        }

        // 判断是否有对应缓存
        PoiInfo infoCache = getCacheByUrl(url);
        if (null != infoCache) {
            PluginLogUtil.d(LOG_TAG, "cache found for url: " + url);
            callback.onCallback(infoCache);
            return;
        }

        ThreadManager.getPool().execute(new Runnable() {
            @Override
            public void run() {
                decodeLocationByStep(url, json, callback);
            }
        });
    }

    /**
     * 解析位置消息中包含的地理信息
     * <p>
     * 地理信息包含3部分: 地址/经度/纬度
     * 解析步骤:
     * 1.尝试从Content字段中解析地址, 并从url字段中解析经纬度
     * 2.尝试从OriContent字段中解析地址和经纬度
     * 3.尝试根据url字段链接跳转信息解析地址和经纬度
     * <p>
     * 每一步执行时会对当前结果还未填充的字段进行填充, 然后检测结果是否已完整, 若已完整直接跳过后续步骤直接回调
     * 解析成功, 若
     *
     * @param json
     */
    public static void decodeLocationByStep(final String url, final JSONObject json,
                                            final DecodeCallback callback) {
        PoiInfo result = new PoiInfo();
        PluginLogUtil.d(LOG_TAG, "start decode location for url: " + url);
        // step 1-1 解析地址和经纬度
        String address = decodeAddressFromContent(json);
        if (!TextUtils.isEmpty(address)) {
            result.setGeoInfo(address);
        }

        PoiInfo urlInfo = getCoordinateFromUrl(url);
        mergePoiInfo(result, urlInfo);

        PluginLogUtil.d(LOG_TAG, "poiInfo after Content resolve: " + result);
        // step 1-2 判断信息是否已完整
        if (result.isPoiComplete()) {
            notifyDecodeCallback(url, result, callback);
            return;
        }

        // step 2-1 根据OriContent解析补充缺失的字段
        PoiInfo oriInfo = decodeLocationFromOriContent(json);
        mergePoiInfo(result, oriInfo);

        PluginLogUtil.d(LOG_TAG, "poiInfo after OriContent resolve: " + result);
        // step 2-2 重新判断信息是否完整
        if (result.isPoiComplete()) {
            notifyDecodeCallback(url, result, callback);
            return;
        }

        // step 3 经过前两步地址信息仍不完整, 根据url跳转信息解析
        PoiInfo jumpInfo = decodeLocationByUrl(url);
        mergePoiInfo(result, jumpInfo);

        PluginLogUtil.d(LOG_TAG, "poiInfo after url jump info resolve: " + result);
        notifyDecodeCallback(url, result, callback);
    }

    private static PoiInfo getCacheByUrl(String url) {
        if (null == mDecodeCache) {
            return null;
        }

        return mDecodeCache.get(url);
    }

    private static PoiInfo mergePoiInfo(PoiInfo rawInfo, PoiInfo newInfo) {
        if (null == newInfo) {
            return rawInfo;
        }

        // merge addr info
        if (TextUtils.isEmpty(rawInfo.getGeoInfo())) {
            rawInfo.setGeoInfo(newInfo.getGeoInfo());
        }

        if ((0 == rawInfo.getLng() && 0 == rawInfo.getLat())) {
            rawInfo.setLat(newInfo.getLat());
            rawInfo.setLng(newInfo.getLng());
        }

        return rawInfo;
    }

    private static void notifyDecodeCallback(final String url, final PoiInfo info,
                                             final DecodeCallback callback) {
        if (null == info) {
            callback.onCallback(null);
            return;
        }

        // 对于完整的解析结果进行缓存
        if (info.isPoiComplete()) {
            if (null == mDecodeCache) {
                mDecodeCache = new LruCache<>(10);
            }

            mDecodeCache.put(url, info);
        }

        // 对于合法的解析结果, 回调成功
        if (info.isPoiLegal()) {
            callback.onCallback(info);
            return;
        }

        callback.onCallback(null);
    }

    private static String decodeAddressFromContent(final JSONObject json) {
        /*
        *位置消息内容有两种格式：
        * 单聊： (地址详情):<br/>/cgi-bin/mmwebwx-bin/webwxgetpubliclinkimg?url=xxx&msgid=299552722629768303&pictype=location
        * 群聊： (发送用户id):<br/>(地址详情):<br/>/cgi-bin/mmwebwx-bin/webwxgetpubliclinkimg?url=xxx&msgid=299552722629768303&pictype=location
        * */
        String content;
        try {
            content = json.getString("Content");

            if (content.startsWith("@")) {
                int start = content.indexOf(":<br/>") + 6;
                int end = content.indexOf(":<br/>", start);
                return content.substring(start, end);
            } else {
                return content.substring(0, content.indexOf(":<br/>"));
            }
        } catch (Exception e) {
            PluginLogUtil.e(LOG_TAG, "resolve content from json encountered error: " + e);
        }

        return null;
    }

    private static PoiInfo getCoordinateFromUrl(String url) {
        Pattern pattern;

        if (url.contains("apis.map.qq.com")) {
            pattern = Pattern.compile("(.+)coord=(\\d+\\.\\d+),(\\d+\\.\\d+)");
        } else if (url.contains("map.google.com")) {
            pattern = Pattern.compile("(.+)q=(\\d+\\.\\d+)&(\\d+\\.\\d+)");
        } else {
            PluginLogUtil.e(LOG_TAG, "unknown location url: " + url);
            return null;
        }

        Matcher matcher = pattern.matcher(url);
        if (matcher.find() && matcher.groupCount() >= 3) {
            PoiInfo info = new PoiInfo();
            info.setLat(Double.parseDouble(matcher.group(2)));
            info.setLng(Double.parseDouble(matcher.group(3)));
            return info;
        }

        PluginLogUtil.e(LOG_TAG, "resolve coordinate from url failed for url: " + url);
        return null;
    }

    private static PoiInfo decodeLocationFromOriContent(final JSONObject json) {
        String oriContent;
        try {
            oriContent = json.getString("OriContent");
        } catch (Exception e) {
            PluginLogUtil.e(LOG_TAG, "get OriContent failed: " + e.toString());
            return null;
        }

        PoiInfo info = new PoiInfo();
        Pattern regex = Pattern.compile("(.+)x=\"(.+)\" y=\"(.+)\" (.+) label=\"(.+)\" maptype=(.+)");
        Matcher matcher = regex.matcher(oriContent.trim());

        if (matcher.find() && matcher.groupCount() >= 5) {
            try {
                info.setLat(Double.parseDouble(matcher.group(2)));
                info.setLng(Double.parseDouble(matcher.group(3)));
                String addr = matcher.group(5);
                if (!TextUtils.isEmpty(addr)) {
                    info.setGeoInfo(addr);
                }
            } catch (Exception e) {
                PluginLogUtil.e(LOG_TAG, "resolve oriContent failed: " + e.toString());
                PluginMonitorUtil.doMonitor(MonitorConsts.WX_LOC_XML_RESOLVE_FAILED);
            }

        }

        return info;
    }

    private static PoiInfo decodeLocationByUrl(String wxLocUrl) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return null;
        }

        String url = convertLocationUrl(wxLocUrl);
        if (TextUtils.isEmpty(url)) {
            PluginLogUtil.e(LOG_TAG, "cannot convert location url: " + wxLocUrl);
            return null;
        }

        BufferedReader reader = null;
        try {
            PoiInfo poi = new PoiInfo();
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:42.0) Gecko/20100101 Firefox/42.0");
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "GBK"));
            String line;
            while ((line = reader.readLine()) != null) {
                PluginLogUtil.d(LOG_TAG, "url jump info read line: " + line);
                if (line.contains("http://map.qq.com?type=marker&isopeninfowin=1&markertype=1")) {
                    Pattern regex = Pattern.compile("(.+)name=(.*)&addr=(.*)&(.+)&(.+)&coord=(\\d+\\.\\d+)%2C(\\d+\\.\\d+)");
                    Matcher matcher = regex.matcher(line.trim());
                    if (matcher.find()) {
                        String rawName = URLDecoder.decode(matcher.group(2), "UTF-8"); // 中国，广东省，深圳市，南山区
                        String addr = URLDecoder.decode(matcher.group(3), "UTF-8"); // 高新南一道6号
                        String lat = matcher.group(6);
                        String lon = matcher.group(7);

                        // 处理地名信息，从后向前按逗号截取最后3段
                        String name = null;
                        int len = rawName.length();
                        int commaCount = 0;
                        for (int i = len - 1; i > 0; i--) {
                            if (rawName.charAt(i) == ',') {
                                commaCount++;
                            }
                            if (commaCount == 3) {
                                name = rawName.substring(i).replaceAll(",", "");
                                break;
                            }
                        }

                        if (name == null) {
                            name = rawName;
                        }

                        poi.setLat(Double.parseDouble(lat));
                        poi.setLng(Double.parseDouble(lon));
                        poi.setGeoInfo(name + addr);

                        if (mDecodeCache == null) {
                            mDecodeCache = new LruCache<String, PoiInfo>(3);
                        }
                        mDecodeCache.put(wxLocUrl, poi);
                        return poi;
                    }
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            PluginLogUtil.e(LOG_TAG, "decode location encountered error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static String convertLocationUrl(String rawUrl) {
        if (rawUrl.startsWith("http://apis.map.qq.com")) {
            return rawUrl;
        } else if (rawUrl.startsWith("https://www.google.com")) {
            // 解析经纬度
            Pattern regex = Pattern.compile("https://(.+)q=(.+)&(.+)");
            Matcher matcher = regex.matcher(rawUrl.trim());

            if (matcher.find()) {
                String locStr = matcher.group(2);
                return "http://apis.map.qq.com/uri/v1/geocoder?coord=" + locStr;
            }
        }

        PluginLogUtil.e(LOG_TAG, "cannot convert location url: " + rawUrl);
        PluginMonitorUtil.doMonitor(MonitorConsts.WX_LOC_RESOLVE_URL_FAILED);

        return "";
    }
}

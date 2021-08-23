package com.txznet.music.helper;

import com.google.gson.reflect.TypeToken;
import com.txznet.music.Constant;
import com.txznet.music.util.JsonHelper;

import java.util.Map;

/**
 * TXZUri: txz://{strProcessingUrl}@@@{strDownloadUrl}@@@{downloadType}
 */
public class TXZUri {
    public static final String PREFIX_V1 = "txz://v1/";
    public static final String PREFIX_V2 = "txz://v2/";
    public final String progressUrl;
    public final String downloadUrl;
    public final String downloadType;
    public final int processIsPost;
    public final Map<String, Object> processHeader;

    private TXZUri(String progressUrl, String downloadUrl, String downloadType) {
        this(progressUrl, downloadUrl, downloadType, 0, null);
    }

    private TXZUri(String progressUrl, String downloadUrl, String downloadType, int processIsPost, Map<String, Object> processHeader) {
        this.progressUrl = progressUrl == null ? null : progressUrl.replaceAll("&#39;", "'");
        this.downloadUrl = downloadUrl == null ? null : downloadUrl.replaceAll("&#39;", "'");
        this.downloadType = downloadType;
        this.processIsPost = processIsPost;
        this.processHeader = processHeader;
    }

    public static TXZUri fromParts(String progressUrl, String downloadUrl, String downloadType) {
        return new TXZUri(progressUrl, downloadUrl, downloadType);
    }

    public static TXZUri fromParts(String progressUrl, String downloadUrl, String downloadType, int processIsPost, Map<String, Object> processHeader) {
        return new TXZUri(progressUrl, downloadUrl, downloadType, processIsPost, processHeader);
    }

    public static TXZUri parse(String uriStr) {
        if (uriStr == null) {
            return null;
        }
        if (uriStr.startsWith(PREFIX_V1)) {
            uriStr = uriStr.substring(PREFIX_V1.length());
            String[] splitStr = uriStr.split(Constant.URL_SPLIT);
            return new TXZUri(splitStr[0], splitStr[1], splitStr[2]);
        }
        if (uriStr.startsWith(PREFIX_V2)) {
            uriStr = uriStr.substring(PREFIX_V2.length());
            String[] splitStr = uriStr.split(Constant.URL_SPLIT);
            return new TXZUri(splitStr[0], splitStr[1], splitStr[2], Integer.parseInt(splitStr[3]), JsonHelper.fromJson(splitStr[4], new TypeToken<Map<String, Object>>() {
            }));
        }
        return null;
    }

    @Override
    public String toString() {
        if (processIsPost == 1) {
            return PREFIX_V2 + progressUrl + Constant.URL_SPLIT + downloadUrl + Constant.URL_SPLIT + downloadType + Constant.URL_SPLIT + processIsPost + Constant.URL_SPLIT + JsonHelper.toJson(processHeader);
        } else {
            return PREFIX_V1 + progressUrl + Constant.URL_SPLIT + downloadUrl + Constant.URL_SPLIT + downloadType;
        }
    }
}

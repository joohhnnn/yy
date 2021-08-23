package com.txznet.webchat.plugin.preset.logic.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ASUS User on 2015/12/10.
 */
public class RespParser {
    private RespParser() {
    }

    // 解析 key1=val1 ; key2=val2
    public static Map<String, String> parseSplitUrl(String splitUrl) {
        Map<String, String> params = new HashMap<String, String>();
        splitUrl = splitUrl.replaceAll(" ", "").replaceAll("\r", "").replace("\n", "");
        String[] resValues = splitUrl.split(";", 2);
        for (int i = 0; i < resValues.length; i++) {
            try {
                String[] kv = resValues[i].split("=", 2);
                String key = kv[0];
                String val = kv[1].replaceAll("\"", "").replaceAll(";", "");
                params.put(key, val);
            } catch (ArrayIndexOutOfBoundsException e) {

            }
        }
        return params;
    }

    // 解析 <xml>msg</xml>
    public static Map<String, String> parseXml(String xml) {
        Map<String, String> params = new HashMap<String, String>();
        Pattern elePattern = Pattern.compile("</(.*?)>");
        Matcher eleMatcher = elePattern.matcher(xml);
        while (eleMatcher.find()) {
            String ele = eleMatcher.group().replace("<", "").replace("/", "").replace(">", "");
            Pattern innerPattern = Pattern.compile("<" + ele + ">(.*?)</" + ele + ">");
            Matcher innerMatcher = innerPattern.matcher(xml);
            while (innerMatcher.find()) {
                params.put(ele, innerMatcher.group().replace("<" + ele + ">", "").replace("</" + ele + ">", ""));
            }
        }
        return params;
    }
}

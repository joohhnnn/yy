package com.example.app3.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author ...随便写点什么防止自动换行
 * -------------------------------------------------------------------------------------------------
 * 正式稳定再写版本号
 * @version 0.0.18.0927
 * 1）增加transParamToByte方法；
 * 2）去掉transParamToJson中没有意义的代码；
 * -------------------------------------------------------------------------------------------------
 */
public class JsonUtil {
    private static final String TAG = "JsonUtil";

    /**
     * 翻译成json
     *
     * @param objects param
     * @return string
     */
    public static String transParamToJson(Object... objects) {
        if (objects == null) {
            Log.e(TAG, "GetJsonString : param is null");
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        if (!((objects.length % 2) == 0)) {
            Log.e(TAG, "GetJsonString : param is invalid");
            return null;
        }
        try {
            for (int i = 0; i < objects.length; i += 2) {
                String jsonKey = objects[i] + "";
                Object jsonValve = objects[i + 1];
                jsonObject.put(jsonKey, jsonValve);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * 把相应字段转成byte[]
     *
     * @param objects 字段
     * @return 数据
     */
    public static byte[] transParamToBytes(Object... objects) {
        String jsonString = transParamToJson(objects);
        if (jsonString == null)
            return null;
        return jsonString.getBytes();
    }

    /**
     * 获取Json的String
     *
     * @param key           key
     * @param json          json
     * @param defaultString 默认
     * @return 值
     */
    public static String getStringFromJson(String key, String json, String defaultString) {
        if (json == null || json.length() == 0) {
            return defaultString;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            defaultString = jsonObject.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defaultString;
    }

    /**
     * 获取byte[] Json的String
     *
     * @param key           key
     * @param jsonData      json
     * @param defaultString 默认
     * @return 值
     */
    public static String getStringFromJson(String key, byte[] jsonData, String defaultString) {
        if (jsonData == null || jsonData.length == 0) {
            return defaultString;
        }
        try {
            JSONObject jsonObject = new JSONObject(new String(jsonData));
            defaultString = jsonObject.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defaultString;
    }

    /**
     * 获取Json的int
     *
     * @param key        key
     * @param json       json
     * @param defaultInt 默认
     * @return 值
     */
    public static int getIntFromJson(String key, String json, int defaultInt) {
        if (json == null || json.length() == 0) {
            return defaultInt;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            defaultInt = jsonObject.getInt(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defaultInt;
    }

    /**
     * 获取byte[] Json的int
     *
     * @param key        key
     * @param jsonData   json
     * @param defaultInt 默认
     * @return 值
     */
    public static int getIntFromJson(String key, byte[] jsonData, int defaultInt) {
        if (jsonData == null || jsonData.length == 0) {
            return defaultInt;
        }
        try {
            JSONObject jsonObject = new JSONObject(new String(jsonData));
            defaultInt = jsonObject.getInt(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defaultInt;
    }


    /**
     * 获取Json的boolean
     *
     * @param key            key
     * @param json           json
     * @param defaultBoolean 默认
     * @return 值
     */
    public static boolean getBooleanFromJson(String key, String json, boolean defaultBoolean) {
        if (json == null || json.length() == 0) {
            return defaultBoolean;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            defaultBoolean = jsonObject.getBoolean(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defaultBoolean;
    }

    /**
     * 获取Json的boolean
     *
     * @param key            key
     * @param jsonData       json
     * @param defaultBoolean 默认
     * @return 值
     */
    public static boolean getBooleanFromJson(String key, byte[] jsonData, boolean defaultBoolean) {
        if (jsonData == null || jsonData.length == 0) {
            return defaultBoolean;
        }
        try {
            JSONObject jsonObject = new JSONObject(new String(jsonData));
            defaultBoolean = jsonObject.getBoolean(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defaultBoolean;
    }

    /**
     * 获取Json的Double
     *
     * @param key           key
     * @param json          json
     * @param defaultDouble 默认
     * @return 值
     */
    public static double getDoubleFromJson(String key, String json, double defaultDouble) {
        if (json == null || json.length() == 0) {
            return defaultDouble;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            defaultDouble = jsonObject.getDouble(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defaultDouble;
    }

    /**
     * 获取byte[] Json的Double
     *
     * @param key           key
     * @param jsonData      json
     * @param defaultDouble 默认
     * @return 值
     */
    public static double getDoubleFromJson(String key, byte[] jsonData, double defaultDouble) {
        if (jsonData == null || jsonData.length == 0) {
            return defaultDouble;
        }
        try {
            JSONObject jsonObject = new JSONObject(new String(jsonData));
            defaultDouble = jsonObject.getDouble(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defaultDouble;
    }


}

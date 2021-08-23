package com.txznet.music.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class JsonHelper {
    // //////////////////////////////////FastJson
    // 下面是FastJson的简介：常用的方法！
    // Fastjson API入口类是com.alibaba.fastjson.JSON，常用的序列化操作都可以在JSON类上的静态方法直接完成。
    // public static final Object parse(String text); //
    // 把JSON文本parse为JSONObject或者JSONArray
    // public static final JSONObject parseObject(String text)； //
    // 把JSON文本parse成JSONObject
    // public static final <T> T parseObject(String text, Class<T> clazz); //
    // 把JSON文本parse为JavaBean
    // public static final JSONArray parseArray(String text); //
    // 把JSON文本parse成JSONArray
    // public static final <T> List<T> parseArray(String text, Class<T> clazz);
    // //把JSON文本parse成JavaBean集合
    // public static final String toJSONString(Object object); //
    // 将JavaBean序列化为JSON文本
    // public static final String toJSONString(Object object, boolean
    // prettyFormat); // 将JavaBean序列化为带格式的JSON文本
    // public static final Object toJSON(Object javaObject);
    // 将JavaBean转换为JSONObject或者JSONArray（和上面方法的区别是返回值是不一样的）
    // public static <T> String toJson(T t) {
    // return JSON.toJSONString(t);
    // }
    // ///////////////////////////////////////
    // /////////////////////////////////Gson
    public static <T> String toJson(T t) {
//        try {
            return new GsonBuilder().disableHtmlEscaping().create().toJson(t);
//        } catch (Exception e) {
//            LogUtil.loge("music:error:toJson" + t, e);
//        }
//        return "";
//		return new Gson().toJson(t);
    }

    // 转成对象
    public static <T> T toObject(Class<T> t, String value) {
        return new Gson().fromJson(value, t);
    }

    public static <T> T toObject(String value, Type type) {
        return new Gson().fromJson(value, type);
    }

    // ////////////////////////////////////
}

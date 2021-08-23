package com.txznet.music.util;

/**
 * 对象工具
 *
 * @author teln
 */
public class Objects {
    public static String getObj2String(Object object) {
        if (object != null) {
            return object.toString();
        }
        return "";
    }
}
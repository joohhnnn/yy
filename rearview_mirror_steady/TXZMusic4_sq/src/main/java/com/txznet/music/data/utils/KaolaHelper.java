package com.txznet.music.data.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by telenewbie on 2018/2/5.
 */

public class KaolaHelper {

    /**
     * 获取考拉激活所需的签名
     */
    public static String getActiveSign(String deviceId) {
        String result = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("md5");
            String src = "9fedb96091d41ecf882524d29ae8a20a" +
                    "0appid=wt2713" +
                    "1deviceid=" + deviceId +
                    "2os=web" +
                    "3packagename=com.sgm.carlink" +
                    "9fedb96091d41ecf882524d29ae8a20a";
            result = convertToHex(messageDigest.digest(src.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 获取考拉请求数据所需的签名
     */
    public static String getOtherSign(String deviceId, String openid) {
        String result = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("md5");
            String src = "9fedb96091d41ecf882524d29ae8a20a" +
                    "0appid=wt2713" +
                    "1deviceid=" + deviceId +
                    "2openid=" + openid +
                    "3os=web" +
                    "4packagename=com.sgm.carlink" +
                    "9fedb96091d41ecf882524d29ae8a20a";
            result = convertToHex(messageDigest.digest(src.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return result;
    }

    /****************************************************************/
    // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    private final static char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    /**
     * 实现字节数组转换十六进制字符串
     */
    private static String convertToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_CHARS[v >>> 4];
            hexChars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }
}

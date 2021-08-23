package com.txznet.txz.util;

import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;

/**
 * Created by ASUS User on 2018/7/2.
 */

public class StringUtils {
    //基本汉字unicode数值范围 4E00-9FA5
    public static final int CHINESE_UNICODE_MIN = 0x4e00;
    public static final int CHINESE_UNICODE_MAX = 0x9fa5;
    public static String filterSpecialChar(String strText){
        StringBuilder sBuilder = new StringBuilder();
        for (int i = 0; i < strText.length(); ++i){
            char c = strText.charAt(i);
            if ((c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= CHINESE_UNICODE_MIN && c <= CHINESE_UNICODE_MAX)){
                sBuilder.append(c);
            }
        }
        return sBuilder.toString();
    }

    public static boolean startWithSpecialChar(String strText){
        char c = strText.charAt(0);
        if ((c >= '0' && c <= '9')
                || (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= CHINESE_UNICODE_MIN && c <= CHINESE_UNICODE_MAX)){
            return false;
        }
        return true;
    }

    public static String subZeroAndDot(String s){
        if(s.indexOf(".") > 0){
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return s;
    }

    public static String getNumber(String strText){
        String dest = strText.replaceAll("[^-0-9.]", "");
        LogUtil.logd("getNumber strText:"+strText+" sBuilder:"+dest);
        return dest;
    }
}

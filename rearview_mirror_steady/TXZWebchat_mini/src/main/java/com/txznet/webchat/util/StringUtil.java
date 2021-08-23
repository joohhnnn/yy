package com.txznet.webchat.util;

public class StringUtil {
    private StringUtil() {
    }

    /**
     * 截取指定长度的字符串(中文算两个字符)
     *
     * @param str
     * @param maxLength 最大长度
     * @param modifier  超出长度后的省略修饰符
     * @return
     */
    public static String handleLength(String str, int maxLength, String modifier) {
        int subLength = maxLength - modifier.length();

        int count = 0;
        int offset = 0;

        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] > 256) {
                offset = 2;
                count += 2;
            } else {
                offset = 1;
                count++;
            }
            if (count == subLength) {
                return str.substring(0, i + 1).concat(modifier);
            }
            if ((count == subLength + 1 && offset == 2)) {
                return str.substring(0, i).concat(modifier);
            }
        }

        return str;
    }

}

package com.txznet.txzsetting.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by ASUS User on 2017/6/7.
 */

public class TextUtil {

    /**
     * 字符数组转换成字符串，并以、隔开
     */
    public static String convertArrayToString(String[] array) {
        if (array == null || array.length == 0)
            return "";
        StringBuffer sb = new StringBuffer(array[0]);
        int length = array.length;
        for (int i = 1; i < length; i++) {
            sb.append("、" + array[i]);
        }
        return sb.toString();
    }

    /**
     * 判断是否含指定符号
     *
     * @param str
     * @return
     */
    public static boolean hasNumber(String str) {
        String regEx = "[0123456789]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
    /**
     * 判断是否含指定符号
     *
     * @param str
     * @return
     */
    public static boolean hasComma(String str) {
        String regEx = "[,，.。！! ]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }


    /**
     * 输入的文字是否合法
     * @param str
     * @return
     * @throws PatternSyntaxException
     */
    public static String isOkText(String str) throws PatternSyntaxException {
        // 只允许字母数字中文
        String regEx = "[^a-zA-Z,，.。！! 0-9\u4e00-\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }
    /**
     * 输入的第一个文字是否合法
     * @param str
     * @return
     * @throws PatternSyntaxException
     */
    public static String isFristOkText(String str) throws PatternSyntaxException {
        // 只允许字母数字中文
        String regEx = "[^a-zA-Z0-9\u4e00-\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }
    /**
     * 只允许中文
     * @param str
     * @return
     * @throws PatternSyntaxException
     */
    public static String isChineseOkText(String str) throws PatternSyntaxException {
        // 只允许中文
        String regEx = "[^\u4e00-\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    /**
     * 判断是否含中文
     *
     * @param str
     * @return
     */
    public static boolean hasChinese(String str) {
        String regEx = "[\u4e00-\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 判断一个字符串的首字符是否为字母
     *
     * @param s
     * @return
     */
    public static boolean fristIsEnglish(String s) {
        char c = s.charAt(0);
        int i = (int) c;
        if ((i >= 65 && i <= 90) || (i >= 97 && i <= 122)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断字符串是否为汉字
     *
     * @param str
     * @return
     */
    public boolean isChinese(String str) {
        char[] chars = str.toCharArray();
        boolean isGB2312 = false;
        for (int i = 0; i < chars.length; i++) {
            byte[] bytes = ("" + chars[i]).getBytes();
            if (bytes.length == 2) {
                int[] ints = new int[2];
                ints[0] = bytes[0] & 0xff;
                ints[1] = bytes[1] & 0xff;

                if (ints[0] >= 0x81 && ints[0] <= 0xFE &&
                        ints[1] >= 0x40 && ints[1] <= 0xFE) {
                    isGB2312 = true;
                    break;
                }
            }
        }
        return isGB2312;
    }

    /**
     * 判断是否含英文
     *
     * @param str
     * @return
     */
    public static boolean hasEnglish(String str) {
        String regEx = "[a-zA-Z]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
}

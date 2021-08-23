package com.txznet.music.utils;

import android.text.TextUtils;

import com.txznet.comm.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StringUtils {
    public static final String EMPTY = "";

    public static boolean isEmpty(String value) {
        return null == value || value.isEmpty() || "null".equals(value);
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    public static String getSource(int sid) {
        switch (sid) {
            case 0:
                return "本地音乐";
            case 1:
                return "考拉";
            case 2:
                return "QQ音乐";
            case 3:
                return "喜马拉雅";
            default:
                return "未知来源";
        }
    }

    /**
     * 转换成为String
     *
     * @return
     */
    public static String toString(Collection val) {
        StringBuffer buffer = new StringBuffer();
        if (CollectionUtils.isNotEmpty(val)) {
            for (Iterator iterator = val.iterator(); iterator.hasNext(); ) {
                buffer.append(iterator.next());
                buffer.append(",");
            }
        }
        if (buffer.length() > 0) {
            return buffer.substring(0, buffer.length() - 1);
        }
        return "";

    }

    public static String toString(String[] val) {


        return toString(val, ',');
    }

    public static String toString(String[] val, char split) {
        StringBuffer sb = new StringBuffer();
        if (null != val && val.length > 0) {

            for (int i = 0; i < val.length; i++) {
                if (i != 0) {
                    sb.append(split);
                }
                val[i].replaceAll(" ", "");
                sb.append(val[i]);
            }
        }

        return sb.toString();
    }

    // 过滤特殊字符
    public static String StringFilter(String str) throws PatternSyntaxException {
        // 只允许字母和数字
        // String regEx = "[^a-zA-Z0-9]";
        // 清除掉所有特殊字符
        String regEx = "[`~!@#$%^&*()+=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll(" ").trim();
    }

    public static boolean check(String s, String regionExpression) {
        if (isEmpty(s)) {
            return false;
        }
        if (isEmpty(regionExpression)) {
            return false;
        }
        Pattern p = Pattern.compile(regionExpression);
        return p.matcher(s).matches();
    }

    public static boolean isLetter(String s) {
        return check(s, "[a-zA-Z]+");
    }

    public static boolean isNumeric(String s) {
        return check(s, "[0-9]+");
    }

    public static boolean isInteger(String s) {
        return check(s, "^-?\\d+$");
    }

    public static boolean isFloat(String s) {
        return check(s, "^(-?\\d+)(\\.\\d+)?$");
    }

    public static boolean isPhone(String s) {
        return check(s, "[0-9]{3,4}-[0-9]{7,8}");
    }

    public static boolean isPhoneCheck(String s) {
        return check(s, "^(\\d{3,4}[-]{0,1})?\\d{7,8}$");
    }

    public static boolean isPhoneCheckMustAreaCode(String s) {
        // return check(s, "^\\(?(\\d{3,4}[-\\)])?\\d{7,8}$");
        return check(s, "^\\d{3,4}[-\\)]?\\d{7,8}$");
    }

    public static boolean isPhoneCheckFjii(String s) {
        if (isMobile(s)) {
            return true;
        }
        return check(s, "^0(10|2[0-5789]|\\d{3})\\d{7,8}$");
    }

    public static boolean isMobile(String s) {
        return check(s, "1[0-9]{10}");
    }

    public static boolean isEmail(String s) {
        return check(s, "^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$");
    }

    public static String replaceBlank(String s) {
        if (isEmpty(s)) {
            return s;
        }
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        return p.matcher(s).replaceAll("");
    }

    public static String replace(String ori, String replace) {
        return TextUtils.replace(ori, new String[]{"%CMD%"}, new String[]{replace}).toString();
    }

    /**
     * @param s
     * @return
     * @desc <pre>
     * 计算字符串长度，双字节字符长度为2，单字节字符串长度为1
     * </pre>
     * @author Erich Lee
     * @date Jul 16, 2013
     */
    public static int len(String s) {
        if (isEmpty(s)) {
            return 0;
        }
        Pattern p = Pattern.compile("[^\\x00-\\xff]");
        return p.matcher(s).replaceAll("aa").length();
    }

    public static String[] split(String s, String split) {
        if (isEmpty(s)) {
            return new String[0];
        }
        if (isEmpty(split)) {
            return new String[]{s};
        }
        return s.split(split);
    }

    public static <T> String join(Collection<T> c, String sep) {
        if (CollectionUtils.isEmpty(c) || sep == null) {
            return EMPTY;
        }
        StringBuffer sb = new StringBuffer();
        for (T s : c) {
            sb.append(s).append(sep);
        }
        return sb.substring(0, sb.length() - sep.length());
    }

    public static String filterHtml(String str) {
        Pattern pattern = Pattern.compile("<([^>]*)>");
        Matcher matcher = pattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        boolean result1 = matcher.find();
        while (result1) {
            matcher.appendReplacement(sb, "");
            result1 = matcher.find();
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * @param s
     * @param len
     * @return
     * @desc <pre>
     * 字符串等长度截断，英文字符或者数字按半个汉字计算
     * </pre>
     * @author Erich Lee
     * @date Jul 16, 2013
     */
    public static String[] split(String s, int len) {
        if (isEmpty(s)) {
            return new String[0];
        }
        if (len <= 0 || len >= s.length()) {
            return new String[]{s};
        }
        List<String> slist = new ArrayList<String>();
        String tmp = "";
        for (int i = 0; i < s.length(); i++) {
            tmp = tmp + s.substring(i, i + 1);
            if (len(tmp) == 2) {
                // 双字节字符
                slist.add(tmp);
                tmp = "";
            } else {
                // 单字节字符
                if (i < s.length() - 1 && len(s.substring(i + 1, i + 2)) == 2) {
                    // 与单字节字符相邻的字符为双字节字符
                    slist.add(tmp);
                    tmp = "";
                }
            }
        }
        int count = slist.size() / len;
        count = (slist.size() % len == 0 ? count : count + 1);
        String[] result = new String[count];
        int end = 0;
        for (int i = 0; i < count; i++) {
            end = (i + 1) * len;
            end = (end < slist.size() ? end : slist.size());
            result[i] = join(slist.subList(i * len, end), "");
        }
        return result;
    }


    public static String trim(Object o) {
        if (o == null) {
            return EMPTY;
        }
        if (o instanceof String) {
            return ((String) o).trim();
        }
        return o.toString().trim();
    }

    public static String trim(String text) {
        if (text == null) {
            return EMPTY;
        }
        return text.trim();
    }

    public static String getReportString(String report, String name) {
        if (isEmpty(report)) {
            return name;
        }
        return report;
    }

    public static String[] toStringArray(List<String> values) {
        String[] value = null;
        if (values != null) {
            value = new String[values.size()];
            for (int i = 0; i < values.size(); i++) {
                value[i] = values.get(i);
            }
        }
        return value;
    }
}

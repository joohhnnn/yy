package com.txznet.comm.util;

import android.text.TextUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	public static boolean isEmpty(String value) {
		return null == value || value.isEmpty();
	}

	public static boolean isNotEmpty(String value) {
		return !isEmpty(value);
	}

	/**
	 * 替换掉字符串中的换行、空格、制表符等
	 * @param str
	 * @return
	 */
    public static String replaceBlank(String str) {  
        String dest = "";  
        if (str!=null) {  
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");  
            Matcher m = p.matcher(str);  
            dest = m.replaceAll("");  
        }  
        return dest;  
    }  
	
	public static String getSource(int sid) {
		switch (sid) {
		case 0:
			return "本地音乐";
		case 1:
			return "考拉";
		case 2:
			return "QQ";
		default:
			return "未知来源";
		}
	}

	public static String toString(String[] val) {
		StringBuilder sb = new StringBuilder();
		if (null != val && val.length > 0) {

			for (int i = 0; i < val.length; i++) {
				if (i != 0) {
					sb.append(",");
				}
				sb.append(val[i]);
			}
		}

		return sb.toString();
	}

	/**
	 * 转换成为String
	 *
	 * @return
	 */
	public static String toString(Collection val) {
		StringBuilder builder = new StringBuilder();
		if (CollectionUtils.isNotEmpty(val)) {
            for (Object o : val) {
                builder.append(o);
                builder.append(",");
            }
		}
		if (builder.length() > 0) {
			return builder.substring(0, builder.length() - 1);
		}
		return "";

	}

	/**
	 *隐藏字符串
	 * @param str  原始字符串
	 * @param startCount 前面显示的字符数
	 * @param endCount 后面显示的字符数
	 * @return
	 */
	public static String getHideString(String str,int startCount,int endCount){
    	if (TextUtils.isEmpty(str)) {
    		return str;
		}
		int length = str.length();
    	if (startCount >= length || endCount >= length) {
    		return str;
		}

		return str.substring(0, startCount) + "******" + str.substring(str.length() - endCount, str.length());
	}

}

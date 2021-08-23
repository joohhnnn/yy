package com.txznet.txz.util;

import java.util.Locale;

import taobe.tec.jcc.JChineseConvertor;

/**
 * 简繁体转换工具，提供当前语言环境检测及字符串转换
 * 
 * @author J
 *
 */
public class LanguageConvertor {

	/**
	 * 根据当前语言环境对指定字符串进行简繁体转换
	 * 
	 * @param str
	 *            待转字符串
	 * @return
	 */
	public static String toLocale(String str) {
		if (isSimplified()) {
			//return toSimplified(str);
			// 默认简体，不做转换
			return str;
		}

		return toTraditional(str);
	}

	/**
	 * 判断当前语言环境是否是简体
	 * 
	 * @return 简体返回true
	 */
	public static boolean isSimplified() {
		Locale locale = Locale.getDefault();
		String country = locale.getCountry();

		if ("TW".equals(country) || "HK".equals(country)) {
			return false;
		}

		return true;
	}

	/**
	 * 将给定字符串转换为简体
	 * 
	 * @param str
	 * @return
	 */
	public static String toSimplified(String str) {
		try {
			return JChineseConvertor.getInstance().t2s(str);
		} catch (Exception e) {

		}

		return "";
	}

	/**
	 * 将给定字符串转换为繁体
	 * 
	 * @param str
	 * @return
	 */
	public static String toTraditional(String str) {
		try {
			return JChineseConvertor.getInstance().s2t(str);
		} catch (Exception e) {

		}

		return "";
	}
}

package com.txznet.txz.util;

import android.text.TextUtils;

public class CallUtil {
	public static String[] seqs = { "零", "妖", "二", "三", "四", "五", "六", "七",
			"八", "九" };

	public static String converToSpeechDigits(String src) {
		if (TextUtils.isEmpty(src))
			return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < src.length(); i++) {
			if (src.charAt(i) >= '0' && src.charAt(i) <= '9') {
				int index = src.charAt(i) - '0';
				sb.append(seqs[index]);
			} else {
				sb.append(src.charAt(i));
			}
		}
		return sb.toString();
	}

	/**
	 * 将1转换成妖加入到唤醒词中
	 * @param src
	 * @return
	 */
	public static String converToWakeupKeyword(String src) {
		if (TextUtils.isEmpty(src))
			return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < src.length(); i++) {
			if (src.charAt(i) == '1') {
				int index = src.charAt(i) - '0';
				sb.append(seqs[index]);
			} else {
				sb.append(src.charAt(i));
			}
		}
		return sb.toString();
	}
}

package com.txznet.txz.util;

import android.text.TextUtils;

public class KeyWordFilter {
	public static boolean hasIgnoredChar(String strKeyword){
		if (TextUtils.isEmpty(strKeyword)){
			return true;
		}
		return strKeyword.contains("<") || strKeyword.contains(">");
	}
}

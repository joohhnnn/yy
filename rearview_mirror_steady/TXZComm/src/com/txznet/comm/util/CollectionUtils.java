package com.txznet.comm.util;

import java.util.Collection;
import java.util.List;

public class CollectionUtils {

	public static boolean isEmpty(Collection coll) {
		return null == coll || coll.size() <= 0;
	}

	public static boolean isNotEmpty(Collection coll) {
		return !isEmpty(coll);
	}

	public static String toString(List collection) {
		if (CollectionUtils.isEmpty(collection)) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < collection.size(); i++) {
			sb.append(collection.get(i).toString());
			if (i != collection.size() - 1) {// 最后一个不加","
				sb.append(",");
			}
		}
		return sb.toString();
	}

	public static String[] toStrings(List col) {
		if (isEmpty(col)) {
			return null;
		}
		String [] str=new String[col.size()];
		for (int i = 0; i < col.size(); i++) {
			str[i]=col.get(i).toString();
		}
		return str;
	}
}

package com.txznet.comm.util;

import android.text.TextUtils;

public class DatasUtil {
	static StringBuilder sb = new StringBuilder();
	
	public static byte[] convertBytes(long...datas){
		String text = convertStr(datas);
		if(TextUtils.isEmpty(text)){
			return null;
		}
		return text.getBytes();
	}
	
	public static String convertStr(long...datas){
		sb.delete(0, sb.length());
		for(long d:datas){
			sb.append(d);
			sb.append(",");
		}
		
		sb.delete(sb.length() - 1, sb.length());
		return sb.toString();
	}
	
	public static long[] decodeBytes(byte[] datas){
		String s = new String(datas);
		String[] values = s.split(",");
		long[] result = new long[values.length];
		for(int i = 0;i<values.length;i++){
			result[i] = Long.parseLong(values[i]);
		}
		
		return result;
	}
}

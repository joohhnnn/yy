package com.txznet.comm.util;

import com.google.protobuf.nano.MessageNano;

public class ProtoBufferUtil {

	public static boolean isEqual(MessageNano a, MessageNano b) {
		byte[] ba = MessageNano.toByteArray(a);
		byte[] bb = MessageNano.toByteArray(b);
		if (ba == null && bb == null)
			return true;
		if (ba == null || bb == null)
			return false;
		if (ba.length != bb.length)
			return false;
		for (int i = 0; i < ba.length; ++i) {
			if (ba[i] != bb[i])
				return false;
		}
		return true;
	}
	
	public static boolean isStringEmpty(String s) {
		return s == null || s.isEmpty();
	}
	
	public static boolean isStringEqual(String src, String dst) {
		if (src == null && dst == null)return true;
		if (src == null || dst == null)return false;
		return src.equals(dst);
	}
	
	public static boolean isIntegerZero(Integer i) {
		return i == null || i == 0;
	}

	public static boolean isIntegerEqual(Integer src, int dst) {
		if (src == null) return false;
		return src == dst;
	}
	
	public static boolean isTrue(Boolean b) {
		if (b != null && b == true) {
			return true;
		}
		return false;
	}
}

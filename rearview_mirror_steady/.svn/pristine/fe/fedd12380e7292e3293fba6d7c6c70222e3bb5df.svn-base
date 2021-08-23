package com.txznet.txz.util;

import java.security.MessageDigest;

public class EncodeUtil {
	public static String HexStr(byte[] data) {
		if (data == null)
			return null;
		StringBuilder hexValue = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			int val = ((int) data[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}

	// //////////////////////////////////////////////////////////////////////////////

	public static byte[] Md5Bytes(byte[] data) {
		try {
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			byte[] bs = mdInst.digest(data);
			return bs;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String Md5Str(byte[] data) {
		return HexStr(Md5Bytes(data));
	}

	public static byte[] Md5Bytes(String data) {
		return Md5Bytes(data.getBytes());
	}

	public static String Md5Str(String data) {
		return Md5Str(data.getBytes());
	}

	// //////////////////////////////////////////////////////////////////////////////

	public static byte[] Sha1Bytes(byte[] data) {
		try {
			MessageDigest mdInst = MessageDigest.getInstance("SHA-1");
			byte[] bs = mdInst.digest(data);
			return bs;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String Sha1Str(byte[] data) {
		return HexStr(Sha1Bytes(data));
	}

	public static byte[] Sha1Bytes(String data) {
		return Sha1Bytes(data.getBytes());
	}

	public static String Sha1Str(String data) {
		return Sha1Str(data.getBytes());
	}

	// //////////////////////////////////////////////////////////////////////////////

	public static byte[] Sha256Bytes(byte[] data) {
		try {
			MessageDigest mdInst = MessageDigest.getInstance("SHA-256");
			byte[] bs = mdInst.digest(data);
			return bs;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String Sha256Str(byte[] data) {
		return HexStr(Sha256Bytes(data));
	}

	public static byte[] Sha256Bytes(String data) {
		return Sha256Bytes(data.getBytes());
	}

	public static String Sha256Str(String data) {
		return Sha256Str(data.getBytes());
	}
}

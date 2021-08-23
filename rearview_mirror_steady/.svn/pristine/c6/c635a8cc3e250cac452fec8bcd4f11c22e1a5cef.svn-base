package com.txznet.txz.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class MD5Util {
	
	/**
	 * 生成文件的MD5校验值
	 * @param file
	 * @return 32位字符串，如果文件不存在返回null
	 */
	public static String generateMD5(File file) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return convertToHex(digest.digest());
	}

	/**
	 * 生成文件的MD5校验值
	 * @return 32位字符串，如果InputStream不存在返回null
	 */
	public static String generateMD5(InputStream fIn) {
		if (fIn == null) {
			return null;
		}
		MessageDigest digest = null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			while ((len = fIn.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			fIn.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return convertToHex(digest.digest());
	}
	
	
	/**
	 * 生成字符串的MD5校验值
	 * @param str
	 * @return 32位字符串，如果字符串为空返回null
	 */
	public static String generateMD5(String str){
		if (str == null) {
			// 字符串为空时
			return null;
		}
		
		return generateMD5(str.getBytes());
	}
	
	/**
	 * 生成字节数组的MD5校验值
	 * @param input
	 * @return 32位字符串，如果字节数组为空返回null
	 */
	public static String generateMD5(byte[] input) {
		if (input == null) {
			// 字节数组为空时
			return null;
		}
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(input);
			return convertToHex(md.digest());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/****************************************************************/
	// http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
	private final static char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	/** 实现字节数组转换十六进制字符串 */
	private static String convertToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_CHARS[v >>> 4];
			hexChars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
		}
		return new String(hexChars);
	}

}

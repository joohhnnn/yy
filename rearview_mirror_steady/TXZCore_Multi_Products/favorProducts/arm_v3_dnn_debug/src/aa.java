package com.unisound.common;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.txznet.txz.cfg.ProjectCfg;

public class aa {
	private static  String sAppkey = "b25kmr3ztigd2m5i4qazswkm7uytykbwbsrflwqv";//"d4536169cea20692d19b02f4db6f08b1";
	
	public static void setAppKey(String strAppkey){
		sAppkey = strAppkey;
	}
	
	public static String a() {
		//return "d4536169cea20692d19b02f4db6f08b1";
		return "#" + a(sAppkey);
	}

	public static String a(String paramString) {
		MessageDigest localMessageDigest = null;
		try {
			localMessageDigest = MessageDigest.getInstance("MD5");
		} catch (Exception localException) {
			System.out.println(localException.getMessage());
		}
		byte[] arrayOfByte1 = localMessageDigest.digest(paramString.getBytes());
		StringBuilder localStringBuilder = new StringBuilder(40);
		for (int k : arrayOfByte1) {
			if ((k & 0xFF) >> 4 == 0) {
				localStringBuilder.append("0").append(
						Integer.toHexString(k & 0xFF));
			} else {
				localStringBuilder.append(Integer.toHexString(k & 0xFF));
			}
		}
		return localStringBuilder.toString();
	}

	public static boolean a(String paramString, File paramFile) {
		if ((TextUtils.isEmpty(paramString)) || (paramFile == null)) {
			t.a("MD5 checkMD5: md5 String NULL or File NULL");
			return false;
		}
		String str = a(paramFile);
		if (str == null) {
			t.a("MD5 checkMD5: calculatedDigest NULL");
			return false;
		}
		//t.b(new Object[] { "MD5 checkMD5: Calculated digest: ", str });
		//t.b(new Object[] { "MD5 checkMD5: Provided digest: ", paramString });
		return str.equalsIgnoreCase(paramString);
	}

	public static String a(File paramFile) {
		MessageDigest localMessageDigest;
		try {
			localMessageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
			t.a("MD5 checkMD5: Exception while getting Digest: "
					+ localNoSuchAlgorithmException);
			localNoSuchAlgorithmException.printStackTrace();
			return null;
		}
		FileInputStream localFileInputStream;
		try {
			localFileInputStream = new FileInputStream(paramFile);
		} catch (FileNotFoundException localFileNotFoundException) {
			t.a("MD5 checkMD5: Exception while getting FileInputStream: "
					+ localFileNotFoundException);
			localFileNotFoundException.printStackTrace();
			return null;
		}
		byte[] arrayOfByte1 = new byte[1024*32];
		try {
			int i;
			while ((i = localFileInputStream.read(arrayOfByte1)) > 0) {
				localMessageDigest.update(arrayOfByte1, 0, i);
			}
			byte[] arrayOfByte2 = localMessageDigest.digest();
			BigInteger localBigInteger = new BigInteger(1, arrayOfByte2);
			String str1 = localBigInteger.toString(16);
			str1 = String.format("%32s", new Object[] { str1 }).replace(' ',
					'0');
			String str2 = str1;
			return str2;
		} catch (IOException localIOException1) {
			throw new RuntimeException("Unable to process file for MD5",
					localIOException1);
		} finally {
			try {
				localFileInputStream.close();
			} catch (IOException localIOException3) {
				localIOException3.printStackTrace();
				t.a("MD5 checkMD5: Exception on closing MD5 input stream: "
						+ localIOException3);
			}
		}
	}

	public static String a(InputStream paramInputStream) {
		MessageDigest localMessageDigest;
		try {
			localMessageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
			t.a("MD5 checkMD5: Exception while getting Digest: "
					+ localNoSuchAlgorithmException);
			localNoSuchAlgorithmException.printStackTrace();
			return null;
		}
		byte[] arrayOfByte1 = new byte[1024*32];
		try {
			int i;
			while ((i = paramInputStream.read(arrayOfByte1)) > 0) {
				localMessageDigest.update(arrayOfByte1, 0, i);
			}
			byte[] arrayOfByte2 = localMessageDigest.digest();
			BigInteger localBigInteger = new BigInteger(1, arrayOfByte2);
			String str1 = localBigInteger.toString(16);
			str1 = String.format("%32s", new Object[] { str1 }).replace(' ',
					'0');
			String str2 = str1;
			return str2;
		} catch (IOException localIOException1) {
			throw new RuntimeException("Unable to process file for MD5",
					localIOException1);
		} finally {
			try {
				paramInputStream.close();
			} catch (IOException localIOException3) {
				localIOException3.printStackTrace();
				t.a("MD5 checkMD5: Exception on closing MD5 input stream: "
						+ localIOException3);
			}
		}
	}
}

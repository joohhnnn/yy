package com.txznet.txz.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.DisplayMetrics;

import com.txznet.comm.remote.GlobalContext;

public class SignatureUtil {
	public static byte[] getSignInfo(String packName) {
		try {
			PackageInfo packageInfo = GlobalContext.get().getPackageManager()
					.getPackageInfo(packName, PackageManager.GET_SIGNATURES);
			return packageInfo.signatures[0].toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getSignMd5(String packName) {
		return EncodeUtil.Md5Str(getSignInfo(packName));
	}

	public static String getSignSha1(String packName) {
		return EncodeUtil.Sha1Str(getSignInfo(packName));
	}

	public static X509Certificate getSignCertificate(String packName) {
		try {
			CertificateFactory certFactory = CertificateFactory
					.getInstance("X.509");
			X509Certificate cert = (X509Certificate) certFactory
					.generateCertificate(new ByteArrayInputStream(
							getSignInfo(packName)));
			return cert;
			// PublicKey key = cert.getPublicKey();
			// String pubKey = key.toString(); // 输出的是16进制的公钥
			// String signNumber = cert.getSerialNumber().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// http://www.blogjava.net/zh-weir/archive/2011/07/19/354663.html

	public static byte[] getSignInfoFromApk(String archiveFilePath) {
		if (Build.VERSION.SDK_INT > 20) {
			try {
				Class<?> clsApkParser = Class
						.forName("android.content.pm.PackageParser");
				Constructor<?> conApkParser = clsApkParser
						.getConstructor();
				Object objApkParser = conApkParser.newInstance();

				File sourceFile = new File(archiveFilePath);
				Method parsePackage = clsApkParser.getDeclaredMethod("parsePackage", File.class,
						int.class);
				parsePackage.setAccessible(true);
				Object objPackage = parsePackage.invoke(objApkParser, sourceFile,PackageManager.GET_SIGNATURES);
				Method collectCertificates = clsApkParser.getDeclaredMethod(
						"collectCertificates", objPackage.getClass(), int.class);
				collectCertificates.invoke(objApkParser, objPackage, 0);
				Field fldSigns = objPackage.getClass().getDeclaredField("mSignatures");
				fldSigns.setAccessible(true);
				Signature[] sign = (Signature[]) fldSigns.get(objPackage);
				return sign[0].toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		
		try {
			Class<?> clsApkParser = Class
					.forName("android.content.pm.PackageParser");
			Constructor<?> conApkParser = clsApkParser
					.getConstructor(String.class);
			Object objApkParser = conApkParser.newInstance(archiveFilePath);
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();
			File sourceFile = new File(archiveFilePath);
			Method parsePackage = clsApkParser.getDeclaredMethod(
					"parsePackage", File.class, String.class,
					DisplayMetrics.class, int.class);
			Object objPackage = parsePackage.invoke(objApkParser, sourceFile,
					archiveFilePath, metrics, 0);
			Class<?> clsPackage = Class
					.forName("android.content.pm.PackageParser$Package");
			Method collectCertificates = clsApkParser.getDeclaredMethod(
					"collectCertificates", clsPackage, int.class);
			collectCertificates.invoke(objApkParser, objPackage, 0);
			Field fldSigns = clsPackage.getDeclaredField("mSignatures");
			fldSigns.setAccessible(true);
			Signature[] sign = (Signature[]) fldSigns.get(objPackage);
			return sign[0].toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getSignMd5FromApk(String archiveFilePath) {
		return EncodeUtil.Md5Str(getSignInfoFromApk(archiveFilePath));
	}

	public static String getSignSha1FromApk(String archiveFilePath) {
		return EncodeUtil.Sha1Str(getSignInfoFromApk(archiveFilePath));
	}

	public static X509Certificate getSignCertificateFromApk(
			String archiveFilePath) {
		try {
			CertificateFactory certFactory = CertificateFactory
					.getInstance("X.509");
			X509Certificate cert = (X509Certificate) certFactory
					.generateCertificate(new ByteArrayInputStream(
							getSignInfoFromApk(archiveFilePath)));
			return cert;
			// PublicKey key = cert.getPublicKey();
			// String pubKey = key.toString(); // 输出的是16进制的公钥
			// String signNumber = cert.getSerialNumber().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

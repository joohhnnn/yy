package com.txznet.txz.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import com.txznet.txz.module.version.LicenseManager;

public class TtsAuthorizeUtil {

	private final static int SECRECT_KEY_LENGHT = 16;

	/*
	 * TTS主题音频文件名加密方式：md5(appid + txzing.com+原始文件名)
	 * TTS文件内容加密方式：异或。加密Key:md5(appid+ACSDFLKlasdkfkjllasdf)
	 */
	// private final static String TTS_THEME_AUDIO_NAME_PUBLIC_KEY =
	// "txzing.com";
	public final static String TTS_PUBLIC_KEY = "ACSDFLKlasdkfkjllasdf";

	public static boolean authorize(File f, byte[] authorization) {
		boolean bRet = false;
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(f, true);
			out.write(authorization, 0, authorization.length);
			bRet = true;
		} catch (Exception e) {

		}
		if (out != null) {
			try {
				out.close();
			} catch (Exception e) {
			}
		}
		return bRet;
	}

	public static boolean checkAuthorization(File f, byte[] authorization) {
		boolean bRet = false;
		FileInputStream in = null;
		do {
			try {
				in = new FileInputStream(f);
				int len = authorization.length;
				byte[] data = new byte[len];
				long fileLength = f.length();
				long skipLength = fileLength - len;
				if (skipLength <= 0) {
					break;
				}

				// 跳过指定长度的字节
				long skipedBytes = 0;
				do {
					long ret = 0;
					ret = in.skip(skipLength - skipedBytes);
					if (ret < 0) {
						break;
					}
					skipedBytes += ret;
				} while (skipedBytes < skipLength);

				// 长度不满足要求
				if (skipLength != skipedBytes) {
					break;
				}

				// 读满len个字节的数据
				int read = 0;
				do {
					int ret = -1;
					ret = in.read(data, read, len - read);
					if (ret < 0) {
						break;
					}
					read += ret;
				} while (read < len);

				// 长度不满足要求
				if (read != len) {
					break;
				}

				// 比较授权信息
				bRet = Arrays.equals(authorization, data);

			} catch (Exception e) {

			}
		} while (false);

		if (in != null) {
			try {
				in.close();
			} catch (Exception e) {

			}
		}
		return bRet;
	}

	public static boolean removeAuthorization(File source, File dest, byte[] authorization) {
		boolean result = false;
		result = checkAuthorization(source, authorization);
		if (result) {
			try {
				copyFileUsingFileChannels(source, dest, authorization.length);
				result = true;
			} catch (Exception e) {
				result = false;
			}
		}
		return result;
	}
	
	/**
	 * 使用默认授权码
	 * 
	 * @param f
	 * @return
	 */
	public static boolean authorize(File f) {
		return authorize(f, getAuthorization());
	}

	/**
	 * 使用默认授权码校验
	 * 
	 * @param f
	 * @return
	 */
	public static boolean checkAuthorization(File f) {
		return checkAuthorization(f, getAuthorization());

	}
	
	/**
	 * 使用默认授权码移除授权授权码
	 * @param source
	 * @param dest
	 * @return
	 */
	public static boolean removeAuthorization(File source, File dest) {
		return removeAuthorization(source, dest, getAuthorization());
	}

	private static byte[] getAuthorization() {
		return getBytesFromMD5(getAuthorizedMD5(), SECRECT_KEY_LENGHT);
	}

	// 返回用于授权TTS主题的MD5值
	private static String getAuthorizedMD5() {
		return MD5Util.generateMD5(LicenseManager.getInstance().getAppId() + TTS_PUBLIC_KEY);
	}

	private static byte[] getBytesFromMD5(String strMD5, int len) {
		byte[] data = null;
		do {
			if (strMD5 == null) {
				break;
			}
			if (len < 0) {
				break;
			}
			if (len * 2 > strMD5.length()) {
				len = strMD5.length() / 2;
			}
			data = new byte[len];

			for (int i = 0; i < data.length; i++) {
				int j = 2 * i;
				String strHex = strMD5.substring(j, j + 2);
				int value = Integer.parseInt(strHex, 16);
				data[i] = (byte) (value & 0x00ff);
			}
		} while (false);
		return data;
	}
	
	private static void copyFileUsingFileChannels(File source, File dest, int offset) throws IOException {    
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
    try {
        inputChannel = new FileInputStream(source).getChannel();
        outputChannel = new FileOutputStream(dest).getChannel();
        outputChannel.transferFrom(inputChannel, 0, inputChannel.size() - offset);
    } finally {
        inputChannel.close();
        outputChannel.close();
    }
	}
}

package com.txznet.webchat.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.txznet.loader.AppLogic;
import com.txznet.txz.util.MD5Util;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {
    public static String toMD5(String inStr) {
        StringBuffer sb = new StringBuffer();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(inStr.getBytes());
            return convertToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final static char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    /**
     * 实现字节数组转换十六进制字符串
     */
    private static String convertToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_CHARS[v >>> 4];
            hexChars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * 对指定文件进行md5校验
     *
     * @param filePath 文件路径
     * @param md5      需要比对的md5
     * @param callback 回调
     */
    public static void checkFileMd5(final String filePath, final String md5, @NonNull final CheckCallback callback) {
        AppLogic.runOnSlowGround(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(filePath)) {
                    callback.onError("file path is empty!");
                }

                if (TextUtils.isEmpty(md5)) {
                    callback.onError("md5 is empty!");
                }

                File file = new File(filePath);
                if (!file.exists()) {
                    callback.onError("target file not exist!");
                }

                String fileMd5 = MD5Util.generateMD5(file);

                // 生成的文件MD5值可能为null
                if (md5.equals(fileMd5)) {
                    callback.onSuccess(true);
                } else {
                    callback.onSuccess(false);
                }
            }
        });
    }

    public interface CheckCallback {
        void onSuccess(boolean isCorrect);

        void onError(String reason);
    }
}

package com.txznet.webchat.util;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信联系人加密管理工具
 * Created by J on 2016/5/3.
 */
public class ContactEncryptUtil {
    private static Map<String, String> mEncryptMap = new HashMap<>();

    public static String decrypt(String id) {
        String encryptedId = mEncryptMap.get(id);
        if (!TextUtils.isEmpty(encryptedId)) {
            return encryptedId;
        } else {
            return "";
        }
    }

    public static String encrypt(String id) {
        // 优先检查EncryptMap里是否有记录，避免重复计算md5
        String encryptedId = mEncryptMap.get(id);
        if (!TextUtils.isEmpty(encryptedId)) {
            return encryptedId;
        }

        String encryptStr = Md5Util.toMD5(id);
        mEncryptMap.put(encryptStr, id);
        mEncryptMap.put(id, encryptedId);

        return encryptStr;
    }

}

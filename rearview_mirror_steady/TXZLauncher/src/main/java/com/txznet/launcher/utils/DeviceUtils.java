package com.txznet.launcher.utils;

import android.text.TextUtils;

import com.txznet.txz.util.DeviceInfo;
import com.txznet.txz.util.PreUrlUtil;

/**
 * 获取设备相关内容的工具
 */
public class DeviceUtils {

    private static String mCacheUid;

    private static String mCacheImei;

    private DeviceUtils() {

    }

    /**
     * 获取uid
     * 使用同行者的服务，是要有appid和apptoken去注册的。这个过程完成后就有uid了。
     */
    public static String getCoreID() {
        if (mCacheUid == null || TextUtils.isEmpty(mCacheUid)) {
            mCacheUid = PreUrlUtil.getUid() + "";
        }
        return mCacheUid;
    }

    /**
     * 获取设备的imei，只要14位。
     */
    public static String getIMEI() {
        // 从缓存中获取
        if (!TextUtils.isEmpty(mCacheImei)) {
            return mCacheImei;
        }

        // 从设备中获取imei并截取14位
        String imei = DeviceInfo.getIMEI();
        if (imei != null && imei.length() > 14) {
            imei = imei.substring(0, 14);
            // 截取成功要保存到缓存中。
            mCacheImei = imei;
        }
        return imei;
    }

    // deviceId = core_id + imei(14)
    public static String getDeviceID() {
        return getCoreID() + getIMEI();
    }
}

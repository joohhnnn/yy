package com.txznet.music.data.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.security.MessageDigest;

/**
 * Created by telenewbie on 2018/2/5.
 */

public class DeviceInfo {

    static String mStrDeviceSerialNumber;

    /**
     * 获取设备唯一标志字符串
     *
     * @return
     */
    public static String getDeviceSerialNumber(Context ctx) {
        if (null == mStrDeviceSerialNumber) {
            synchronized (DeviceInfo.class) {
                if (null == mStrDeviceSerialNumber) {
                    StringBuilder strId = new StringBuilder("txz");
                    strId.append('\0');
                    strId.append(getIMEI(ctx));
                    strId.append('\0');
                    strId.append(getWifiMacAddress(ctx));
                    strId.append('\0');
                    strId.append(getCPUSerialNumber());

                    try {
                        MessageDigest mdInst = MessageDigest.getInstance("MD5");
                        byte[] md5Bytes = mdInst.digest(strId.toString()
                                .getBytes());
                        StringBuilder hexValue = new StringBuilder();
                        for (int i = 0; i < md5Bytes.length; i++) {
                            int val = ((int) md5Bytes[i]) & 0xff;
                            if (val < 16)
                                hexValue.append("0");
                            hexValue.append(Integer.toHexString(val));
                        }
                        mStrDeviceSerialNumber = hexValue.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return mStrDeviceSerialNumber;
    }

    static String mStrIMEI;

    /**
     * 获取IMEI
     *
     * @return
     */
    private static String getIMEI(Context ctx) {
        if (mStrIMEI == null || mStrIMEI.isEmpty()) {
            try {
                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return mStrIMEI;
                }
                mStrIMEI = ((TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            } catch (Exception e) {
            }
        }
        return mStrIMEI;
    }

    static String mStrWifiMacAddress;

    /**
     * 获取wifi的mac地址
     *
     * @return
     */
    private static String getWifiMacAddress(Context ctx) {
        if (mStrWifiMacAddress == null) {
            WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = (null == wifi ? null : wifi.getConnectionInfo());
            // if (!wifi.isWifiEnabled()) {
            // // 必须先打开，才能获取到MAC地址
            // wifi.setWifiEnabled(true);
            // wifi.setWifiEnabled(false);
            // }
            if (null != info) {
                mStrWifiMacAddress = info.getMacAddress();
            }
        }

        return mStrWifiMacAddress;
    }

    static String mStrCPUSerialNumber;
    static boolean mNeedGetCPUSerialNumber = true;

    /**
     * 获取cpu序列号
     *
     * @return
     */
    private static String getCPUSerialNumber() {
        if (mNeedGetCPUSerialNumber) {
            synchronized (DeviceInfo.class) {
                if (mNeedGetCPUSerialNumber) {
                    InputStreamReader ir = null;
                    LineNumberReader input = null;
                    try {
                        Process pp = Runtime.getRuntime().exec(
                                "/system/bin/cat /proc/cpuinfo");
                        ir = new InputStreamReader(pp.getInputStream());
                        input = new LineNumberReader(ir);
                        String str;
                        do {
                            str = input.readLine();
                            if (str == null) {
                                break;
                            }
                            String[] ss = str.split("\\:");
                            if (ss.length != 2)
                                continue;
                            String key = ss[0].trim();
                            if (key.equals("Serial")) {
                                mStrCPUSerialNumber = ss[1].trim();
                                break;
                            }
                        } while (null != str);
                        mNeedGetCPUSerialNumber = false;
                    } catch (Exception e) {
                    } finally {
                        if (input != null) {
                            try {
                                input.close();
                            } catch (IOException e) {
                            }
                        }
                        if (ir != null) {
                            try {
                                ir.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            }
        }
        return mStrCPUSerialNumber;
    }
}

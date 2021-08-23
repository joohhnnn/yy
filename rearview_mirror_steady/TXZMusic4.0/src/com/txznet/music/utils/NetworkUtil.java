package com.txznet.music.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.telephony.TelephonyManager;

public class NetworkUtil {
    /**
     * 是否去需要去校验当前网络是否可用true为是，false为否
     */
    private static boolean bCheckedNetInfo = true;

    /**
     * 检测当前网络是否为WAP上网方式
     *
     * @param context
     * @return true为是，false为否
     */
    public static boolean isWAPStatic(Context context) {
        ConnectivityManager connectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo info = connectManager.getActiveNetworkInfo();
            if (info == null || !info.isConnected()) {
                return false;
            }
            if (info.getExtraInfo().contains("wap") || info.getExtraInfo().contains("WAP")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns whether the network is available
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
            } else {
                NetworkInfo infos = connectivity.getActiveNetworkInfo();
                if (infos != null && infos.isConnected()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检测当前网络是否连接(针对点击某个控件跳转下一界面需要联网请求数据，或在当前界面需要发起联网请求的时候使用)
     *
     * @param context
     * @param bNoNetToast 是否需要弹出默认Toast true为是，false为否
     * @return
     */
    public static boolean checkNetworkAvailableOnClick(Context context, boolean bNoNetToast) {
        if (!bCheckedNetInfo) {
            return true;
        }
        return isNetworkAvailable(context, bNoNetToast);
    }

    /**
     * 检测当前网络是否连接
     *
     * @param context
     * @param bNoNetToast 是否需要弹出默认Toast true为是，false为否
     * @return
     */
    public static boolean isNetworkAvailable(Context context, boolean bNoNetToast) {
        boolean netFlag = false;
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
            } else {
                NetworkInfo infos = connectivity.getActiveNetworkInfo();
                if (infos != null && infos.isConnected()) {
                    netFlag = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!netFlag && bNoNetToast) {
//            Toast.makeText(context, R.string.no_network, Toast.LENGTH_SHORT).show();
        }
        return netFlag;
    }

    /**
     * 判断当前是否为漫游连接
     */
    public static boolean isNetworkRoaming(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
            } else {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    TelephonyManager telManager = (TelephonyManager) context
                            .getSystemService(Context.TELEPHONY_SERVICE);
                    if (telManager != null && telManager.isNetworkRoaming()) {
                        return true;
                    } else {
                    }
                } else {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * wifi连接
     *
     * @param context
     * @return 如果是wifi 并且 也处于连接状态中则返回真
     */
    public static boolean isWifiNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectManager.getActiveNetworkInfo();
            if (info == null || !info.isConnected()) {
                return false;
            }
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 3G网
     * 3G 类型繁多，如果非2G, 4G ，则归为3G
     *
     * @param context
     * @return 如果是手机上网并且 也处于连接状态中则返回真
     */
    public static boolean isNGNetworkAvailable(Context context) {
        try {
            NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();
//            String _strSubTypeName = info.getSubtypeName();
            if (info == null || !info.isConnected()) {
                return false;
            }
//            KL.d(NetworkUtil.class, "_strSubTypeName = {}", _strSubTypeName);
//            //运营商 3G
//            if (!TextUtils.isEmpty(_strSubTypeName)) {
//                if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
//                    return true;
//                }
//            }
            int currentNetworkType = info.getType();
            if (currentNetworkType == ConnectivityManager.TYPE_MOBILE) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断当前网络是否已经连接，并且是2G状态.
     *
     * @param context
     * @return true, or false
     */
    public static boolean is2GMobileNetwork(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info;
        try {
            info = manager.getActiveNetworkInfo();
            if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                int currentNetworkType = info.getSubtype();
                if (currentNetworkType == TelephonyManager.NETWORK_TYPE_GPRS
                        || currentNetworkType == TelephonyManager.NETWORK_TYPE_CDMA
                        || currentNetworkType == TelephonyManager.NETWORK_TYPE_EDGE
                        || currentNetworkType == TelephonyManager.NETWORK_TYPE_1xRTT
                        || currentNetworkType == TelephonyManager.NETWORK_TYPE_IDEN) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 判断当前网络是否已经连接，并且是4G状态. 根据产品的定义，4G为LTE;
     *
     * @param context
     * @return true, or false
     */
    public static boolean is4GMobileNetwork(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info;
        try {
            info = manager.getActiveNetworkInfo();
            if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                int currentNetworkType = info.getSubtype();
                if (currentNetworkType == TelephonyManager.NETWORK_TYPE_LTE) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static State getState(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if (info == null) {
            return State.UNKNOWN;
        }
        return info.getState();
    }

    /**
     * 获取当前可使用的网络类型。
     *
     * @return 结果可能为：wifi 4g 3g 2g nonet none
     */
    public static String getCurrentAvailableNetworkType(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                return "none";
            }
            if (!isNetworkAvailable(context)) {
                return "nonet";
            }
            if (isWifiNetworkAvailable(context)) {
                return "wifi";
            }
            if (is2GMobileNetwork(context)) {
                return "2g";
            }
            if (is4GMobileNetwork(context)) {
                return "4g";
            }
            if (isNGNetworkAvailable(context)) {
                return "3g";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "nonet";
    }

//    /**
//     * get network type. contains: 3G,2G,none.
//     *
//     * @param context
//     * @return
//     */
//    public static String getNetwork(Context context) {
//        try {
//            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
//                return "wifi";
//            } else if ((networkInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_EDGE
//                    || networkInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_GPRS
//                    || networkInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_CDMA)) {
//                return "2G";
//            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
//                return "3G";
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "none";
//    }

    public static int getNetworkIndex(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo == null) {
                return 0;
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return 1;
            } else if ((networkInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_EDGE
                    || networkInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_GPRS
                    || networkInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_CDMA)) {
                return 2;
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return 3;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}

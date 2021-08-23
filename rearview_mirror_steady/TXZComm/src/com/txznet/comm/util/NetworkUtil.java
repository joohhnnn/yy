package com.txznet.comm.util;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.txz.ui.data.UiData;
/**
 * 
 */
public class NetworkUtil {
	
	public static int getSystemNetwork(Context context)
    {
        int netType = UiData.NETWORK_STATUS_NONE;
        ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = mgr.getActiveNetworkInfo();
        int connectType = ConnectivityManager.TYPE_DUMMY;
        int mobileType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
        if (info != null && info.isAvailable())
        {
            connectType = info.getType();
            switch (connectType)
            {
            case ConnectivityManager.TYPE_ETHERNET:
                netType = UiData.NETWORK_STATUS_UNKNOW;
                break;

            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_WIMAX:
                netType = UiData.NETWORK_STATUS_WIFI;
                break;

            case ConnectivityManager.TYPE_MOBILE:
            case ConnectivityManager.TYPE_MOBILE_DUN:
            case ConnectivityManager.TYPE_MOBILE_SUPL:
            case ConnectivityManager.TYPE_MOBILE_MMS:
            case ConnectivityManager.TYPE_MOBILE_HIPRI:
            {
                TelephonyManager tmgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                mobileType = tmgr.getNetworkType();
                switch (mobileType)
                {
                case TelephonyManager.NETWORK_TYPE_LTE://
                    // TODO 4g
                	netType = UiData.NETWORK_STATUS_4G;
                	break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                case 17:   //NETWORK_TYPE_TD_SCDMA:
                    netType = UiData.NETWORK_STATUS_3G;
                    break;

                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:

                case TelephonyManager.NETWORK_TYPE_UNKNOWN:

                default:
                    netType = UiData.NETWORK_STATUS_2G;
                    break;
                }
            }
                break;
            default:
                break;
            }
        }
        return netType;
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
}

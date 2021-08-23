package com.txznet.music.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.music.Constant;

import org.json.JSONObject;

/**
 * TXZ相关应用辅助工具
 *
 * @author zackzhou
 * @date 2019/4/22,11:34
 */

public class TXZAppUtils {
    private static final String TAG = Constant.LOG_TAG_UTILS + ":TXZAppUtils";

    private static int sCoreVerCode = 0;
    private static String sCoreVerName = "";

    private TXZAppUtils() {
    }

    /**
     * 获取core版本号
     */
    public static int getCoreVerCode() {
        return sCoreVerCode;
    }

    /**
     * 获取core版本名
     */
    public static String getCoreVerName() {
        return sCoreVerName;
    }

    public static void initTXZVersion() {
        try {
            PackageInfo packInfo = GlobalContext.get().getPackageManager()
                    .getPackageInfo(ServiceManager.TXZ, 0);
            sCoreVerCode = packInfo.versionCode;
            sCoreVerName = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "comm.PackageInfo", null, new ServiceManager.GetDataCallback() {
                    @Override
                    public void onGetInvokeResponse(ServiceManager.ServiceData data) {
                        if (null == data) {
                            return;
                        }
                        JSONObject json = data.getJSONObject();
                        try {
                            if (json == null) {
                                Logger.w(TAG, "get package VERSION failed: "
                                        + ServiceManager.TXZ
                                        + ", try to get from package manager");
                                json = new JSONObject();
                            }
                            PackageInfo packInfo = GlobalContext.get().getPackageManager()
                                    .getPackageInfo(ServiceManager.TXZ, 0);
                            sCoreVerCode = packInfo.versionCode;
                            sCoreVerName = packInfo.versionName;
                            sCoreVerCode = json.optInt("versionCode", packInfo.versionCode);
                            sCoreVerName = json.optString("versionName", packInfo.versionName);
                            Logger.e(TAG, "getTXZCoreVersion: code=" + sCoreVerCode + ", name=" + sCoreVerName);
                        } catch (Exception e) {
                            Logger.e(TAG, "getTXZCoreVersion: " + e.getMessage());
                        }
                    }
                }, 60 * 1000);
    }
}

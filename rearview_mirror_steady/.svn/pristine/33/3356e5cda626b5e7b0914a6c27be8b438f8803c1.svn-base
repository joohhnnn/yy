package com.txznet.comm.remote;

import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.update.UpdateCenter;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.version.ApkVersion;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZConfigManager;

public class ServiceHandler {


    private ServiceHandler() {

    }

    // 预处理
    public static byte[] preInvoke(String packageName, String command,
                                   byte[] data) {




        byte[] ret = ServiceHandlerBase.preInvoke(packageName, command, data);

        if (command.equals("comm.closeApp")) {
            AppLogicBase.getInstance().onCloseApp();
            return ret;
        }
        if (command.startsWith("comm.update.")) {
            return UpdateCenter.process(packageName,
                    command.substring("comm.update.".length()), data);
        }
        // 获取包信息
        if (command.equals("comm.PackageInfo")) {
            // LogUtil.logd("request VERSION from: " + packageName +", result: "
            // + ApkVersion.versionName);
            JSONBuilder json = new JSONBuilder();
            json.put("versionCode", ApkVersion.versionCode);
            json.put("versionName", ApkVersion.versionName);
            json.put("sourceDir",
                    GlobalContext.get().getApplicationInfo().sourceDir);
            json.put("versionCompile", TXZConfigManager.VERSION);
            json.put("channelName",ApkVersion.channelName);
            return json.toBytes();
        }
        return ret;
    }
}

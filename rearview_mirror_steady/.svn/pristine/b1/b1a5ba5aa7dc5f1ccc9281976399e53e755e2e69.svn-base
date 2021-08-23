package com.txznet.music.util;

import com.txznet.audio.player.audio.QQMusicAudio;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;

public class TtsUtilCompatible {
    public static Integer VERSION = 20920;

    public static boolean getCoreApkVersion() {
        ServiceManager.ServiceData data = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ, "comm.PackageInfo", null);
        JSONBuilder jsonBuilder = new JSONBuilder(data.getBytes());
        Integer versionAPK = jsonBuilder.getVal("versionCode", Integer.class, 0);
        LogUtil.e("VERSION", VERSION);
        LogUtil.e("versionAPK", versionAPK);
        if (versionAPK >= VERSION) {
            return true;
        }
        return false;
    }

    public static void ttsSpeakResource() {

    }

    //兼容的问题，才需要如此
    public static void speakTextOnRecordWin(String resId,String sText, boolean close,
                                            Runnable oRun) {
        if (!getCoreApkVersion()) {
            TtsUtil.speakTextOnRecordWin(resId,sText, close, oRun);
        } else {
            if (oRun != null) {
                oRun.run();
            }
        }
    }
}

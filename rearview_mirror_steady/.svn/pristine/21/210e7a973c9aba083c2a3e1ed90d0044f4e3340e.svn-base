package com.txznet.launcher.cfg;

import android.os.Environment;

import java.io.File;

/**
 * Created by ASUS User on 2018/5/22.
 */

public class DebugCfg {
    public static File getDebugRoot() {
        try {
            return new File(Environment.getExternalStorageDirectory(), "txz");
        } catch (Exception e) {
            return new File(".");
        }
    }

    // adb shell touch /sdcard/txz/asr_result.debug
    public static boolean YF_ASR_RESULT_DEBUG = new File(getDebugRoot(), "asr_result.debug").exists();

    // adb shell touch /sdcard/txz/today_notice.debug
    public static boolean TODAY_NOTICE_DEBUG = new File(getDebugRoot(), "today_notice.debug").exists();

    // adb shell touch /sdcard/txz/pass_anjixing.debug
    public static boolean PASS_ANJIXING_DEBUG = new File(getDebugRoot(), "pass_anjixing.debug").exists();

    // adb shell touch /sdcard/txz/ota_upgrade.debug
    public static boolean OTA_UPGRADE_DEBUG = new File(getDebugRoot(),"ota_upgrade.debug").exists();

    // adb shell touch /sdcard/txz/anjixing_test_environment.debug
    public static boolean ANJIXING_TEST_ENVIRONMENT_DEBUG = new File(getDebugRoot(),"anjixing_test_environment.debug").exists();

}

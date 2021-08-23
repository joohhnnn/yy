package com.txznet.webchat.util;


import android.os.Environment;

import java.io.File;

public class ClearUtil {
    private ClearUtil() {
    }

    public static void clearCache() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            clearDir(Environment.getExternalStorageDirectory().getPath() + "/txz/webchat/cache/Head/");
            clearDir(Environment.getExternalStorageDirectory().getPath() + "/txz/webchat/cache/Self/");
            clearDir(Environment.getExternalStorageDirectory().getPath() + "/txz/webchat/cache/Voice/");
        }
    }


    private static void clearDir(String dirPath) {
        try {
            File file = new File(dirPath);
            if (file.exists() && file.isDirectory()) {
                File[] subFiles = file.listFiles();
                for (File subFile : subFiles) {
                    if (subFile.isFile()) {
                        subFile.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

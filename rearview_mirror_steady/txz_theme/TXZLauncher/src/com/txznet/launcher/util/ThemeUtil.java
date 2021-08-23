package com.txznet.launcher.util;

import android.content.Context;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ASUS User on 2015/9/21.
 */
public class ThemeUtil {
    private ThemeUtil() {
    }

    public static File installTheme(Context context, String themeName, String from, boolean force) {
        OutputStream os = null;
        InputStream is = null;
        File outFile = null;
        try {
            File outDir = new File(context.getFilesDir() + "/Theme/" + themeName);
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            outFile = new File(outDir, "theme.res");
            if (outFile.exists() && !force) {
                return outFile;
            }
            is = new FileInputStream(from);
            os = new BufferedOutputStream(new FileOutputStream(outFile), 4096);
            byte[] buffer = new byte[4096];
            int hasRead = -1;
            while ((hasRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, hasRead);
            }
        } catch (Exception e) {

        } finally {
            try {
                if (is != null)
                    is.close();
                if (os != null)
                    os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return outFile;
    }

    public static File installThemeFromAsset(Context context, String themeName, String fileName, boolean force) {
        OutputStream os = null;
        InputStream is = null;
        File outFile = null;
        try {
            File outDir = new File(context.getFilesDir() + "/Theme/" + themeName);
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            outFile = new File(outDir, "theme.res");
            if (outFile.exists() && !force) {
                return outFile;
            }
            is = context.getAssets().open(fileName);
            os = new BufferedOutputStream(new FileOutputStream(outFile), 4096);
            byte[] buffer = new byte[4096];
            int hasRead = -1;
            while ((hasRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, hasRead);
            }
        } catch (Exception e) {

        } finally {
            try {
                if (is != null)
                    is.close();
                if (os != null)
                    os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return outFile;
    }
}

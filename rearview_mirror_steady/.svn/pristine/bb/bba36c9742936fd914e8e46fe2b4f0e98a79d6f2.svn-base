package com.txznet.launcher.helper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.txznet.launcher.sp.ThemeSp;
import com.txznet.launcher.ui.base.ProxyContext;
import com.txznet.loader.AppLogic;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;

/**
 * Created by ASUS User on 2015/9/23.
 */
public class ThemeManager {
    private static final String DEFAULT_THEME_RES_NAME = "theme.so";
    private static final String DEFAULT_THEME = "base.so";

    private Context mContext;
    private ProxyContext mProxyContext;
    private Handler mHandler = new Handler();

    private ThemeManager(Context context) {
        mContext = context;
        initRes();
    }

    private static ThemeManager sInstance;

    public static ThemeManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ThemeManager.class) {
                sInstance = new ThemeManager(context);
            }
        }
        return sInstance;
    }

    public ProxyContext getProxyContext() {
        return mProxyContext;
    }

    // 初始化
    private void initRes() {
        // 拷贝基础资源到内部存储
        File base = copyFromAsset("Base", DEFAULT_THEME, false);
        String curTheme = ThemeSp.getInstance(mContext).getCurrentTheme("Base");
        // 拷贝当前主题包到内部存储
        // TODO 先看内部有没有该资源，没有从sdcard拷贝，再没有用原始
        File res = null;
        if (curTheme.equals("Base")) {
            res = base;
        } else {
            res = copyFromSdcard(curTheme, false);
            if (res == null) {
                res = base;
            }
        }
        mProxyContext = new ProxyContext(mContext);
        mProxyContext.loadResources(res.getAbsolutePath(), mContext.getPackageName() + ".theme");
    }

    public void installTheme(final String themeName, final Runnable callback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                File res = copyFromSdcard(themeName, false);
                if (res != null) {
                    mProxyContext = new ProxyContext(mContext);
                    mProxyContext.loadResources(res.getAbsolutePath(), mContext.getPackageName() + ".theme");
                    ThemeSp.getInstance(mContext).setCurrentTheme(themeName);
                    AppLogic.runOnUiGround(callback, 0);
                }
            }
        });
    }

    public File copyFromSdcard(String themeName, boolean force) {
        OutputStream os = null;
        InputStream is = null;
        File outFile = null;
        try {
            File outDir = new File(mContext.getFilesDir() + "/Theme/" + themeName);
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            outFile = new File(outDir, DEFAULT_THEME_RES_NAME);
            if (outFile.exists() && !force) {
                return outFile;
            }
            is = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/txz/theme/" + themeName + "/" + mContext.getPackageName() + "/" + DEFAULT_THEME_RES_NAME);
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

    public File copyFromAsset(String themeName, String assetsRes, boolean force) {
        OutputStream os = null;
        InputStream is = null;
        File outFile = null;
        try {
            File outDir = new File(mContext.getFilesDir() + "/Theme/" + themeName);
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            outFile = new File(outDir, DEFAULT_THEME_RES_NAME);
            if (outFile.exists() && !force) {
                return outFile;
            }
            is = mContext.getAssets().open(assetsRes);
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
    public void reloadProxyContext(){
    	initRes();
    } 
}

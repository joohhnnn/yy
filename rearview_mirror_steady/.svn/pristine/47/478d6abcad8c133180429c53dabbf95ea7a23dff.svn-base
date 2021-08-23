package com.txznet.txz.component.media.loader;

import android.content.Context;

import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.media.plugin.IPluginMediaTool;
import com.txznet.txz.module.media.plugin.PluginMediaToolDataInterface;
import com.txznet.txz.util.FileUtils;
import com.txznet.txz.util.MD5Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import dalvik.system.DexClassLoader;

/**
 * 媒体工具装载工具
 *
 * Created by J on 2019/3/5.
 */
public class MediaToolLoader {
    private static final String LOG_TAG = "MediaToolLoader";
    private static String mDexPath = AppLogicBase.getApp().getApplicationInfo().dataDir
            + "/dex";
    private static String mLibPath = AppLogicBase.getApp().getApplicationInfo().dataDir
            + "/solibs";

    private static String mInnerToolPath = AppLogic.getApp().getDir("media_tool",
            Context.MODE_PRIVATE).getPath();

    /**
     * 通过文件路径进行媒体工具装载
     *
     * @param dexFile       dex路径(非assets路径)
     * @param info          装载信息(通过.chk文件解析)
     * @param dataInterface 初始化媒体工具时传入的通讯接口
     * @return 装载的媒体工具, 失败返回null或抛出对应异常
     */
    public static IPluginMediaTool loadMediaToolFile(File dexFile, MediaToolLoadInfo info,
                                                     PluginMediaToolDataInterface dataInterface)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException,
            NoSuchMethodException, InvocationTargetException {
        if (!checkMd5(dexFile, info.checkMd5)) {
            log("md5 check failed for dex: " + dexFile.getPath());
            return null;
        }

        // ensure optimize directories
        FileUtils.createOrExistsDir(mDexPath);
        FileUtils.createOrExistsDir(mLibPath);

        DexClassLoader loader = new DexClassLoader(dexFile.getAbsolutePath(), mDexPath, mLibPath,
                MediaToolLoader.class.getClassLoader());
        Class clazz = loader.loadClass(info.entryClass);
        Constructor<IPluginMediaTool> constructor =
                clazz.getConstructor(PluginMediaToolDataInterface.class);

        return constructor.newInstance(dataInterface);
    }

    /**
     * 通过assets路径装载MediaTool
     * @param assetsPath assets路径
     * @param info 装载信息
     * @param dataInterface 通信接口
     * @return 装载失败返回null
     */
    public static IPluginMediaTool loadMediaToolFromAssets(String assetsPath,
                                                           MediaToolLoadInfo info,
                                                           PluginMediaToolDataInterface
                                                                   dataInterface)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException,
            NoSuchMethodException, InvocationTargetException {
        String targetPath = mInnerToolPath + File.separator + info.targetPackageName + ".jar";
        if (!releaseAndCheckAssetsDex(assetsPath, targetPath, info.checkMd5)) {
            log("assets release failed, can not load media tool.");
            return null;
        }


        return loadMediaToolFile(new File(targetPath), info, dataInterface);
    }

    /**
     * 将dex文件从assets目录释放并进行校验
     *
     * 若指定文件已存在, 直接进行校验, 并在校验失败后重新拷贝一次
     *
     * @param assetsPath assets文件路径
     * @param targetPath 目标文件路径
     * @param checkMd5 校验md5
     * @return 是否拷贝成功
     */
    private static boolean releaseAndCheckAssetsDex(String assetsPath, String targetPath,
                                                 String checkMd5) {
        log(String.format("releasing assets file: %s, dst: %s", assetsPath, targetPath));

        int retryCount = 0;
        File targetFile = new File(targetPath);

        // 最多重复拷贝2次
        while (retryCount++ < 2) {
            if (targetFile.exists() && checkMd5(targetFile, checkMd5)) {
                return true;
            }

            targetFile.delete();
            copyFileFromAssets(assetsPath, targetPath);
        }

        log(String.format("assets file check failed, src: %s, dst: %s", assetsPath, targetPath));
        return false;
    }

    private static boolean checkMd5(File dexFile, String md5) {
        String fileMd5 = MD5Util.generateMD5(dexFile);
        log(String.format("md5 check: %s : %s", fileMd5, md5));
        return md5.equalsIgnoreCase(fileMd5);
    }

    /**
     * 将Assets目录中的指定文件释放到指定目录
     *
     * @param assetsPath assets路径
     * @param dst 目标路径
     * @return 是否拷贝成功
     */
    private static boolean copyFileFromAssets(String assetsPath, String dst) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = AppLogicBase.getApp().getAssets().open(assetsPath);
            out = new FileOutputStream(dst);
            byte[] buf = new byte[2048];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            in = null;
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private static void log(String msg) {
        JNIHelper.logd(LOG_TAG + "::" + msg);
    }
}

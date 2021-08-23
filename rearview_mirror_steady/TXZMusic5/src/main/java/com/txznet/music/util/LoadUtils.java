package com.txznet.music.util;

import android.os.Environment;

import com.txznet.jni.TXZStrComparator;

/**
 * 加载类
 * Created by telenewbie on 2017/4/17.
 */

public class LoadUtils {

    //加载拼音so库所需要的数据
    public static void loadPinyinData() {
        FileUtils.copyAssertFile("pinyin/unipy.dat", desPath);
    }

    public static int initPinyin() {
        return TXZStrComparator.initialize(getFinalPinyinFile());
    }

    private static String desPath = "/txz/unipy.dat";

    public static String getFinalPinyinFile() {
        return Environment.getExternalStorageDirectory() + desPath;
    }

}

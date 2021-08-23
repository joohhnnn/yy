package com.txznet.txzsetting.util;

import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.txznet.sdk.TXZConfigManager;
import com.txznet.txzsetting.TXZApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by nick Huliangyi on 2017/6/5.
 */

public class FileUtil {

    public static final String TAG = "FileUtil";

    /**
     * 获取TXZ UID
     *
     * @param path
     * @return
     */
    @Nullable
    public static String getFileName(String path) {
        String result = "";
        String uidName = "";
        File[] files = new File(path).listFiles();
        if (files == null) {
            Log.d(TAG, "get file name is null");
            return "";
        }
        for (File file : files) {
            result += file.getPath() + "\n";
            if (file.getName().startsWith("uid_")) {
                uidName = file.getName().replace(".txt", "").replace("uid_", "");
                Log.d(TAG, "uid == " + uidName);
            }
        }
        if (result.equals("")) {
            result = "找不到文件!!";
            return "";
        }
        return uidName;
    }

    /**
     * Buffer读文件
     *
     * @param path
     * @param fileName
     * @return
     */
    @Nullable
    public static String getFileContentBuffer(String path, String fileName) {
        String readline = "";
        try {
            File file;
            if (fileName == null){
                file = new File(path);
            }else {
                file = new File(path, fileName);
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuffer sb = new StringBuffer();
            while ((readline = br.readLine()) != null) {
                Log.d(TAG, "readline:" + readline);
                sb.append(readline);
            }
            br.close();
            Log.d(TAG, "读取成功：" + sb.toString());
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "读取文件失败");
            return "";
        }
    }

    /**
     * 判断文件是否存在
     */
    public static boolean fileIsExists(String filePath, String fileName) {
        try {
            Log.d(TAG, "fileIsExists = " + filePath + "/" + fileName);
            File file;
            if (fileName == null){
                file = new File(filePath);
            }else {
                file = new File(filePath, fileName);
            }
            if (!file.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 删除文件
     *
     * @param filePath
     * @return
     */
    public static boolean deleteFile(String filePath, String name) {
        File file = new File(filePath , name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.d(TAG, "删除文件成功：" + name);
                return true;
            } else {
                Log.d(TAG, "删除文件失败：" + name);
                return false;
            }
        } else {
            Log.d(TAG, "删除文件失败：" + name + "不存在");
            return false;
        }
    }

    /**
     * 创建文件
     *
     * @param path
     * @param name
     */
    public static void makeFile(String path, String name) {
        makeFileFolder(path);
        File file = new File(path , name);
        Log.d(TAG, "file.exists() = " + file.exists());
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
        }
    }

    /**
     * 创建文件夹
     *
     * @param path
     */
    public static void makeFileFolder(String path) {
        //新建一个File，传入文件夹目录
        File file = new File(path);
        Log.d(TAG, "makeFileFolder file.exists() = " + file.exists());
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}

package com.txznet.txz.component.media.util;

import com.txznet.loader.AppLogicBase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * 简单文件处理工具
 * Created by J on 2019/4/24.
 */
public class FileReader {

    /**
     * 读取简单文件, 一次性返回文件文本内容
     * @param path 文件路径
     * @return 文件文本内容
     */
    public static String readFile(String path) {
        File file = new File(path);

        // 限制文件大小为100kb, 防止异常大的文件被错误读取
        if (file.isDirectory() || file.length() > 1024 * 100) {
            return null;
        }

        StringBuilder fileContents = new StringBuilder((int)file.length());
        Scanner scanner = null;

        try {
            scanner = new Scanner(file);
            while(scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + "\n");
            }
            return fileContents.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (null != scanner) {
                scanner.close();
            }
        }

        return null;
    }

    /**
     * 读取assets目录下简单文件, 一次性返回文件文本内容
     * @param assetsPath assets文件路径
     * @return 文件文本内容
     */
    public static String readFileFromAssets(String assetsPath) {
        StringBuilder fileContents = new StringBuilder();

        InputStream is = null;
        Scanner scanner = null;
        try {
            is = AppLogicBase.getApp().getAssets().open(assetsPath);
            scanner = new Scanner(is);
            while(scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + "\n");
            }
            return fileContents.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != scanner) {
                scanner.close();
            }

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}

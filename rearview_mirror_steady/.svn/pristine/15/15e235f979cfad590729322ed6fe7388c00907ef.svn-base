package com.txznet.marketing.HttpRequest;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *文件读写工具类
 * Create by JackPan on 2019/01/17.
 */
public class FileUtil {

    //判断SD卡是否存在
    private static boolean isSdCardExist() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static String getFileContent(String filePath) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isSdCardExist()){
            File file = new File(Environment.getExternalStorageDirectory(),filePath);
            try {
                String str = null;
                InputStream is = new FileInputStream(file);
                InputStreamReader input = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(input);
                while ((str = reader.readLine()) != null) {
                    stringBuilder.append(str);
                }

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return stringBuilder.toString();
    }


}

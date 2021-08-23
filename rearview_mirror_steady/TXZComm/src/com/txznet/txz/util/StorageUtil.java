package com.txznet.txz.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
import android.text.TextUtils;

public class StorageUtil {
    public static String getInnerSDCardPath() {
        try {
            return Environment.getExternalStorageDirectory().getPath();
        } catch (Exception e) {
            return "";
        }
    }

    public static List<String> getAllExterSdcardPath() {
        List<String> SdList = new ArrayList<String>();
        String innerSdcardPath = getInnerSDCardPath();
        File fInnerSdcardPath = null;
        if (!TextUtils.isEmpty(innerSdcardPath)) {
            fInnerSdcardPath = new File(innerSdcardPath);
        }
        // 得到路径
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                // 将常见的linux分区过滤掉
                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;
//				if (line.contains("media"))
//					continue;
                if (line.contains("system") || line.contains("cache")
                        || line.contains("sys") || line.contains("data")
                        || line.contains("tmpfs") || line.contains("shell")
                        || line.contains("root") || line.contains("acct")
                        || line.contains("proc") || line.contains("misc")
                        || line.contains("obb")) {
                    continue;
                }

                if (!(line.contains("fat") || line.contains("fuse") || (line
                        .contains("ntfs")))) {
                    continue;
                }
                String columns[] = line.split(" ");
                if (columns == null || columns.length < 2) {
                    continue;
                }
                String path = columns[1];
                if (TextUtils.isEmpty(path) || SdList.contains(path)
                        /*|| !(path.toLowerCase().contains("sd")||path.toLowerCase().contains("storage"))*/) {
                    continue;
                }
                if (fInnerSdcardPath != null
                        && 0 == fInnerSdcardPath.compareTo(new File(path))) {
                    continue;
                }
                SdList.add(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SdList;
    }
}

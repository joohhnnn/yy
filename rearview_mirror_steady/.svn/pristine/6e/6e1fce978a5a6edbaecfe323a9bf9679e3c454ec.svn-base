package com.txznet.music.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.comm.util.StringUtils;
import com.txznet.music.Constant;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.List;

/**
 * @author Erich Lee
 * @desc <pre></pre>
 * @Date Mar 9, 2013
 */
public class FileUtils {
    private static final String TAG = Constant.LOG_TAG_UTILS + ":FileUtils";

    public static String mBaseStorePath = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "TXZ"
            + File.separator;// :sd卡根目录/TXZ/

    public static boolean isExistSDCard = Environment.getExternalStorageState()
            .equals(android.os.Environment.MEDIA_MOUNTED);

    /*
     * 检查SD卡是否存在
     */
    public static boolean chekSDCardExist() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static void closeQuietly(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static boolean isExist(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return Environment.getStorageState(file).equals(Environment.MEDIA_MOUNTED) && file.exists();
    }

    public static boolean isExist(File file) {
        if (file == null) {
            return false;
        }
        return isExist(file.getAbsolutePath());
    }

    public static void removeDir(String dirPath) {
        if (StringUtils.isEmpty(dirPath)) {
            return;
        }
        removeDir(new File(dirPath));
    }

    public static void removeDir(File dir) {
        if (!FileUtils.isExist(dir)) {
            return;
        }
        if (dir.isFile()) {
            dir.delete();
            return;
        }
        File[] listFiles = dir.listFiles();
        if (listFiles == null || listFiles.length == 0) {
            dir.delete();
            return;
        }
        // 删除目录中的文件
        for (File file : listFiles) {
            removeDir(file);
        }
        // 删除目录
        String[] list = dir.list();
        if (Array.getLength(list) <= 0) {
            dir.delete();
        }
    }

    public static void recycleBitmaps(List<Bitmap> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Bitmap bitmap : list) {
            recycleBitmap(bitmap);
        }
        list.clear();
    }

    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    /**
     * 获得指定文件的byte数组
     */
    public static byte[] getBytes(String filePath) {
        byte[] buffer = null;
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024 * 1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            buffer = bos.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } catch (OutOfMemoryError e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            closeQuietly(fis);
            closeQuietly(bos);
        }
        return buffer;
    }

    /**
     * 获得指定文件的byte数组
     */
    public static byte[] getByteList(String filePath) {
        byte[] buffer = null;
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;

        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);

            }
            buffer = bos.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } catch (OutOfMemoryError e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            closeQuietly(fis);
            closeQuietly(bos);
        }
        return buffer;
    }

    public static byte[] file2BetyArray(String filePath) {
        FileInputStream fileInputStream = null;
        File file = new File(filePath);
        byte[] bFile = null;
        try {
            bFile = new byte[(int) file.length()];
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            FileUtils.closeQuietly(fileInputStream);
            // bFile.clone();

        }
        return bFile;
    }

    /**
     * 根据byte数组，生成文件
     */
    public static void getFile(byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            File dir = new File(filePath);
            if (!FileUtils.isExist(dir)) {// 判断文件目录是否存在
                dir.mkdirs();
            }
            File file = new File(filePath, fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            closeQuietly(bos);
            closeQuietly(fos);
        }
    }

    /**
     * 删除文件夹
     */
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); // 删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath;
            delFile(filePath);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public static boolean delFile(String filePath) {
        java.io.File myFilePath = new java.io.File(filePath);
        boolean result = false;
        if (isExist(myFilePath)) {
            result = myFilePath.getAbsoluteFile().delete(); // 删除空文件夹
            Logger.d(TAG, "删除成功" + filePath);
        } else {
            Logger.d(TAG, "没有该文件" + filePath);
            result = true;
        }
//        if (!result) {//如果删除失败，则切换到root权限
//            try {
//                Process process = Runtime.getRuntime().exec("su");
//                DataOutputStream dos = new DataOutputStream(process.getOutputStream());
//                String command = "rm -rf " + filePath;
//                Logger.d(TAG + "file delete command" + command);
//                dos.write(command.getBytes(Charset.forName("utf-8")));
//                dos.flush();
//                dos.writeBytes("exit\n");
//                dos.flush();
//                process.waitFor();//等待执行完毕
//                BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//                String line;
//                StringBuffer sb = new StringBuffer();
//                while ((line = br.readLine()) != null) {
//                    sb.append(line);
//                }
//                Logger.d(TAG + "file delete message:" + sb.toString());
//                result = StringUtils.isEmpty(sb.toString());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
        return result;
    }

    public static boolean delFile(String filePath, List<String> keepDir) {
        java.io.File myFilePath = new java.io.File(filePath);
        if (keepDir != null) {
            for (String dirPath : keepDir) {
                if (filePath.startsWith(dirPath)) {
                    Logger.d(TAG, "删除失败，第三方文件 " + filePath);
                    return false;
                }
            }
        }
        boolean result = false;
        if (isExist(myFilePath)) {
            result = myFilePath.getAbsoluteFile().delete(); // 删除空文件夹
            Logger.d(TAG, "删除成功" + filePath);
        } else {
            Logger.d(TAG, "没有该文件" + filePath);
            result = true;
        }
        return result;
    }

    /**
     * 删除文件夹下的文件
     */
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!isExist(file)) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (String s : tempList) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + s);
            } else {
                temp = new File(path + File.separator + s);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + s);// 先删除文件夹里面的文件
                delFolder(path + "/" + s);// 再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }


    public static boolean isFoundApp(Context mContext, String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = mContext.getPackageManager().getPackageInfo(
                    packageName, 0);
        } catch (NameNotFoundException e) {
            packageInfo = null;
            Log.e(TAG, e.toString());
        }
        if (packageInfo == null) {
            System.out.println("没有安装");
            return false;
        } else {
            System.out.println("已经安装");
            return true;
        }
    }

    public static File getFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!isExist(file)) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!isExist(file)) {
                file.mkdir();
            }
        } catch (Exception e) {

        }
    }

    /**
     * 复制存放在Assets中的文件到指定目录,如果存在则不保存
     *
     * @param srcPath 存放在Assets中的文件路径(相对路径)
     * @param desPath 相对于Sd卡的路径
     */
    public static void copyAssertFile(String srcPath, String desPath) {
        FileOutputStream fos = null;
        InputStream is = null;
        File file2 = null;
        try {
            file2 = new File(Environment.getExternalStorageDirectory(), desPath);
            if (!isExist(file2)) {
                file2.createNewFile();
            } else {
                Logger.d(TAG, "music:local:pinyin:file:data:exist,no need copy to sdcard");
                return;
            }
            is = GlobalContext.get().getAssets().open(srcPath);
            fos = new FileOutputStream(file2);
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                // buffer字节
                fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
            }
            fos.flush();// 刷新缓冲区
        } catch (IOException e) {
            e.printStackTrace();
            if (isExist(file2)) {
                file2.delete();
            }
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

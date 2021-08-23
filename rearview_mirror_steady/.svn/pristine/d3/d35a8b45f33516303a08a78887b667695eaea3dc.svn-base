package com.txznet.txz.component.advertising.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.DeviceInfo;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AdvertisingUtils {
    /**
     * 根据URL生成bitmap
     *
     * @param url
     * @return
     */
    public static Bitmap getBitmap(String url, int reqHeight, int reqWidth) {
        BitmapFactory.Options options = new BitmapFactory.Options();
//            //只会解析图片宽高信息
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(url, options);
        options.inSampleSize = calculateInSampleSize(options, reqHeight, reqWidth);
        //真实加载图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(url, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqHeight, int reqWidth) {
        int height = options.outHeight;
        int width = options.outWidth;
        LogUtil.d("advertising calculateInSampleSize height:" + height + ",width:" + width);
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            //计算缩放比，是2的指数
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        LogUtil.d("advertising calculateInSampleSize inSampleSize:" + inSampleSize);
        return inSampleSize;
    }

    /**
     * 根据本地图片路径，生成drawable
     * @param url
     * @return
     */
    public static Drawable getDrawable(String url, int reqHeight, int reqWidht) {
        return new BitmapDrawable(GlobalContext.get().getResources(), getBitmap(url, reqHeight, reqWidht));
    }

    public static Bitmap cropBitmap(Bitmap srcBitmap, int needHeight) {
        /**裁剪保留下部分的第一个像素的Y坐标*/
        int needY = srcBitmap.getHeight() - needHeight;
        /**裁剪关键步骤*/
        Bitmap cropBitmap = Bitmap.createBitmap(srcBitmap,0,needY,srcBitmap.getWidth(),needHeight);
        srcBitmap.recycle();
        return cropBitmap;
    }

    public static Drawable bitmap2Drawable(Bitmap bitmap){
        return new BitmapDrawable(GlobalContext.get().getResources(),bitmap);
    }

    public static Bitmap setBitmapSize(Bitmap bm, int newWidth ,int newHeight){
        // 获得图片的宽高.
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例.
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数.
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片.
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    /**
     * 比较图片真实宽高和实际分辨率是否一致
     */
    public static boolean compareWidthAndHeight(String url){
        BitmapFactory.Options options = new BitmapFactory.Options();
        //只会解析图片宽高信息
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(url, options);
        if (options.outWidth == DeviceInfo.getScreenWidth() && options.outHeight == DeviceInfo.getScreenHeight()) {
            return true;
        }
        return false;
    }


    public static long getSDFreeSize() {
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //空闲的数据块的数量
        long freeBlocks = sf.getAvailableBlocks();
        //返回SD卡空闲大小
        //return freeBlocks * blockSize;  //单位Byte
        //return (freeBlocks * blockSize)/1024;   //单位KB
        return (freeBlocks * blockSize) / 1024 / 1024; //单位MB
    }

    public static long getCacheSize() {
        //取得SD卡文件路径
        File file = new File(Environment.getExternalStorageDirectory(), "txz/cache/advertising");
        return getFileSizes(file) / 1024 / 1024; //单位MB
    }
    /**
     * 获取指定文件夹的大小
     *
     * @param f
     * @return
     * @throws Exception
     */
    public static long getFileSizes(File f) {
        long size = 0;
        File flist[] = f.listFiles();//文件夹目录下的所有文件
        if (flist == null) {//4.2的模拟器空指针。
            return 0;
        }
        if (flist != null) {
            for (int i = 0; i < flist.length; i++) {
                if (flist[i].isDirectory()) {//判断是否父目录下还有子目录
                    size = size + getFileSizes(flist[i]);
                } else {
                    size = size + getFileSize(flist[i]);
                }
            }
        }
        return size;
    }

    /**
     * 获取指定文件的大小
     *
     * @return
     * @throws Exception
     */
    public static long getFileSize(File file) {

        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);//使用FileInputStream读入file的数据流
                size = fis.available();//文件的大小
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
        }
        return size;
    }
    public static void freeCacheFiles(File root) {
        File[] fs = root.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return true;
            }
        });
        if (fs != null) {
            for (File f : fs) {
                if (f.isDirectory()) {
                    freeCacheFiles(f);
                    f.delete();
                } else {
                    JNIHelper.logd("clear cache file: " + f.getPath());
                    f.delete();
                }
            }
        }
        // root.delete();
    }

}

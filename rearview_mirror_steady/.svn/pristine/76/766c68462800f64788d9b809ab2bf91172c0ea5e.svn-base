package com.txznet.music.localModule.logic;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.utils.FileConfigUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * @author ASUS User
 */
public class StorageUtil {

    public static final long MAX_CACHE_SIZE_M = 500;
    public static final long MAX_CACHE_SIZE = 1024 * 1024 * MAX_CACHE_SIZE_M;

    public static final long MIN_DISK_SIZE = 1024 * 1024 * 50;
    private static final long MAX_TMD_SIZE = 1024 * 1024 * 500;
    private static final String TAG = "StorageUtil";

    public static List<String> getVolumeState(Context ctx) {
        List<String> volumePaths = new ArrayList<String>();
        StorageManager sm = (StorageManager) ctx.getSystemService(Context.STORAGE_SERVICE);
        try {
            String[] paths = (String[]) sm.getClass().getMethod("getVolumePaths").invoke(sm);
            for (int i = 0; i < paths.length; i++) {
                String status = (String) sm.getClass().getMethod("getVolumeState", String.class).invoke(sm, paths[i]);
                if (status.equals(android.os.Environment.MEDIA_MOUNTED)) {
                    volumePaths.add(paths[i]);
                }
            }
        } catch (Exception e) {

        }
        return volumePaths;
    }

    public static long getMaxTMDSize() {

        long longConfig = FileConfigUtil.getLongConfig(TXZFileConfigUtil.KEY_MUSIC_MAX_CACHE_SIZE, MAX_CACHE_SIZE_M);
        Logger.d(TAG, "MaxTMDSize:" + longConfig + " M ");
        longConfig = longConfig * 1024 * 1024;//单位默认是M，方案商设置过来的。
        return longConfig;
    }

    /**
     * 获取歌曲的缓存目录
     *
     * @return 歌曲的缓存目录
     */
    public static String getSongCacheDir() {
        return Environment.getExternalStorageDirectory() + "/txz/cache/song";
    }

    /**
     * 获取非歌曲(电台，新闻，脱口秀等)在线音频的缓存目录
     *
     * @return 其他在线音频的缓存目录
     */
    public static String getOtherCacheDir() {
        return Environment.getExternalStorageDirectory() + "/txz/cache/other";
    }


    /**
     * 获取TMD文件目录
     *
     * @return TMD文件目录
     */
    public static String getTmdDir() {
        return Environment.getExternalStorageDirectory() + "/txz/audio/song";
    }


    /**
     * 检测缓存数据，如果存储空间不足则删除全部的缓存数据，如果缓存数据超过指定的大小就删除指定百分比的缓存数据
     *
     * @return 存储空间是否大于指定的大小
     */
    public static boolean checkCacheSize() {
        if (getAvailableSize() <= MIN_DISK_SIZE) {
            deleteCache(1.0f);
        } else if (getCacheSize() > getMaxTMDSize()) {
            deleteCache(0.3f);
        }
        return getAvailableSize() > MIN_DISK_SIZE;
    }

    /**
     * 删除缓存的tmp数据
     *
     * @param percent 要删除的缓存数据占总缓存的百分比，取值0-1
     */
    public static void deleteCache(float percent) {
        long totalSize = 0;
        List<File> fileList = new ArrayList<>();
        FilenameFilter tmpFileNameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".tmp")) {
                    return true;
                }
                return false;
            }
        };

        File songCacheFile = new File(getSongCacheDir());
        if (songCacheFile.exists()) {
            File[] files = songCacheFile.listFiles(tmpFileNameFilter);
            if (null != files) {
                for (File f : files) {
                    totalSize += f.length();
                    fileList.add(f);
                }
            }
        }

        File otherCacheFile = new File(getOtherCacheDir());
        if (otherCacheFile.exists()) {
            File[] files = otherCacheFile.listFiles(tmpFileNameFilter);
            if (null != files) {
                for (File f : files) {
                    totalSize += f.length();
                    fileList.add(f);
                }
            }
        }

        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                if (lhs.lastModified() == rhs.lastModified()) {
                    return 0;
                }
                return lhs.lastModified() < rhs.lastModified() ? -1 : 1;
            }
        });

        LogUtil.d(TAG + "delete cache, total size:" + formatSize(totalSize) + " percent:" + percent);
        long size = (long) (totalSize * percent);
        for (File f : fileList) {
            LogUtil.d(TAG + "delete cache " + f.getName() + " " + formatSize(f.length()));
            if (f.delete()) {
                size -= f.length();
            }
            if (size <= 0) {
                break;
            }
        }

    }


    /**
     * 获取缓存的tmp数据大小
     *
     * @return 缓存的tmp数据大小
     */
    public static long getCacheSize() {
        long songCacheSize = 0;
        long otherCacheSize = 0;

        FilenameFilter tmpFileNameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".tmp")) {
                    return true;
                }
                return false;
            }
        };

        File songCacheFile = new File(getSongCacheDir());
        if (songCacheFile.exists()) {
            File[] files = songCacheFile.listFiles(tmpFileNameFilter);
            if (null != files) {
                for (File f : files) {
                    songCacheSize += f.length();
                }
            }
        }

        File otherCacheFile = new File(getOtherCacheDir());
        if (otherCacheFile.exists()) {
            File[] files = otherCacheFile.listFiles(tmpFileNameFilter);
            if (null != files) {
                for (File f : files) {
                    otherCacheSize += f.length();
                }
            }
        }
        LogUtil.d(TAG + "song cache size " + formatSize(songCacheSize) + " other cache size:" + formatSize(otherCacheSize));
        return songCacheSize + otherCacheSize;
    }

    /**
     * 获取外部存储空间可用大小
     *
     * @return 外部存储空间可用大小 单位b
     */
    public static long getAvailableSize() {
        File sdcard = Environment.getExternalStorageDirectory();
        StatFs statFs = null;
        try {
            statFs = new StatFs(sdcard.getPath());
        } catch (Exception e) {
            LogUtil.loge("getAvailableSize error", e);
            return 0;
        }
        long blockSize;
        long blockCount;
        long availableBlockCount;
        if (android.os.Build.VERSION.SDK_INT >= 18) {
            availableBlockCount = statFs.getAvailableBlocksLong();
            blockCount = statFs.getBlockCountLong();
            blockSize = statFs.getBlockSizeLong();
        } else {
            availableBlockCount = statFs.getAvailableBlocks();
            blockCount = statFs.getBlockCount();
            blockSize = statFs.getBlockSize();
        }
        return availableBlockCount * blockSize;
    }


    /**
     * 将long类型的大小格式化成带单位的字符串
     *
     * @param size 大小
     * @return String类型的大小
     */
    public static String formatSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format(Locale.getDefault(), "%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format(Locale.getDefault(), "%d B", size);
    }


    /**
     * 获取已经缓存的tmd音频总大小
     *
     * @return 已经缓存的tmd音频总大小
     */
    public static long getTmdSize() {
        long size = 0;
        File tmdFile = new File(getTmdDir());
        if (tmdFile.exists()) {
            File[] files = tmdFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.endsWith(".tmd")) {
                        return true;
                    }
                    return false;
                }
            });
            if (null != files) {
                for (File f : files) {
                    size += f.length();
                }
            }
        }
        LogUtil.d(TAG + "tmd size " + formatSize(size));
        return size;
    }


    /**
     * 判断是否有足够的存储空间
     *
     * @return 是否有足够的存储空间
     */
    public static boolean isDiskSpaceEnough() {
        return getAvailableSize() > MIN_DISK_SIZE;
    }
}

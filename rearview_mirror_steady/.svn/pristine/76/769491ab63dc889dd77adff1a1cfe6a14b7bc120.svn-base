package com.txznet.music.util;

import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.entity.BlackListAudio;
import com.txznet.music.data.entity.LocalAudio;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.helper.AudioConverts;
import com.txznet.proxy.util.StorageUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by brainBear on 2018/1/20.
 */

public class ScanUtil {

    /**
     * 扫描回调
     */
    public interface ScanCallback {
        /**
         * 扫描结果发生改变
         *
         * @param count 扫描个数
         */
        void onScanCountChanged(int count);

        /**
         * 扫描结束
         */
        void onScanFinish(boolean isIntercept);
    }

    private static final String TAG = Constant.LOG_TAG_UTILS + ":ScanUtil";

    private static volatile boolean isIntercepted = false; // 是否打断扫描

    private static boolean enableProxy;

    static FilenameFilter filenameFilter = (dir, name) -> {
        // 需求支持的格式 tmd,mp3,m4a,aac,wav,flac

        // 文件系统排序，0-9 A-Z a-z
        if (name.endsWith("3")) {
            if (name.endsWith(".mp3")) {
                return true;
            }
        } else if (name.endsWith("a")) {
            if (name.endsWith(".nomedia")) {
                return true;
            }
            if (name.endsWith(".m4a")) {
                return true;
            }
        } else if (name.endsWith("c")) {
            if (name.endsWith(".aac")) {
                return true;
            }
            if (name.endsWith(".flac")) {
                return true;
            }
        } else if (enableProxy && name.endsWith("d")) {
            if (name.endsWith(".tmd")) {
                return true;
            }
        } else if (name.endsWith("v")) {
            if (name.endsWith(".wav")) {
                return true;
            }
        }
        return name.lastIndexOf(".") == -1;
    };

    private ScanUtil() {

    }

    private static void scanRecursively(String path, Set<LocalAudio> audios, List<BlackListAudio> blackListAudioList) {
        if (isIntercepted) {
            return;
        }
        File file = new File(path);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "scanRecursively file->" + path + ", time=" + SystemClock.currentThreadTimeMillis());
        }
        String[] list = file.list(filenameFilter);
        if (list == null) {
            return;
        }
        for (String s : list) {
            if (s.endsWith(".nomedia")) {
                //去掉nomedia的
                return;
            }
        }

        long start = TimeManager.getInstance().getTimeMillis();
        for (String s : list) {
            if (isIntercepted) {
                return;
            }
            File f = new File(path + File.separator + s);
            if (!s.contains(".")) {
                if (TXZFileConfigUtil.getBooleanSingleConfig(Configuration.Key.HIDE_THIRD_SOURCE, Configuration.DefVal.HIDE_THIRD_SOURCE)) {
                    if (Configuration.ThirdPath.PATHS.contains(f.getPath())) {
                        continue;
                    }
                }
                String txzPath = new File(Environment.getExternalStorageDirectory().getPath() + "/txz").getPath();
                if (f.getPath().equals(txzPath)) {
                    scanRecursively(StorageUtil.getTmdDir(), audios, blackListAudioList);
                } else {
                    scanRecursively(f.getAbsolutePath(), audios, blackListAudioList);
                }
            } else {
                if (isIntercepted) {
                    return;
                }
                if (checkLocalFile(f)) {
                    long now = SystemClock.currentThreadTimeMillis();
                    LocalAudio audio = AudioConverts.convert2LocalAudio(f);
                    Log.d(TAG, "scanRecursively bconvert->" + f + ";" + audio + ", cost=" + (SystemClock.currentThreadTimeMillis() - now));
                    if (null != audio) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "scanRecursively audio->" + audio + ", time=" + SystemClock.currentThreadTimeMillis());
                        }
                        if (blackListAudioList.contains(audio)) {
                            Logger.i(TAG, "blacklist audio :" + f);
                        } else {
                            if (isIntercepted) {
                                return;
                            }
                            audio.createTime = start++;
                            if (audios.add(audio)) {
                                if (callback != null) {
                                    callback.onScanCountChanged(audios.size());
                                }
                            } else {
                                Logger.i(TAG, "more one same audios :" + f);
                            }
                        }
                    }
                }
            }
        }
    }

    private static ScanCallback callback;


    public static void scanRecursively
            (Set<LocalAudio> audios, List<BlackListAudio> blackListAudioList, ScanCallback
                    callback) {
        enableProxy = TXZFileConfigUtil.getBooleanSingleConfig(Configuration.Key.ENABLE_MEDIA_PROXY, Configuration.DefVal.ENABLE_MEDIA_PROXY);
        isIntercepted = false;
        isScanning = true;
        long start = SystemClock.elapsedRealtime();
//        Logger.printCurrentMemory("recursively method start");
        ScanUtil.callback = callback;
        for (String path : getPathList()) {
            ScanUtil.scanRecursively(path, audios, blackListAudioList);
        }
        GcTrigger.runGc();
        int scanCount = audios.size();
        isScanning = false;

        if (ScanUtil.callback != null) {
            ScanUtil.callback.onScanFinish(isIntercepted);
            ScanUtil.callback = null;
        }
//        Logger.printCurrentMemory("recursively method end, cost=" + (SystemClock.elapsedRealtime() - start));
        Logger.i(TAG, "scan size:%d, cost=%s", scanCount, SystemClock.elapsedRealtime() - start);
        if (isIntercepted) {
            Logger.d(TAG, "interceptScan cost time=" + (SystemClock.elapsedRealtime() - interceptTime));
        }
    }

    private static volatile boolean isScanning;

    public static boolean isScanning() {
        return isScanning;
    }

    private static long interceptTime;

    public static void interceptScan() {
        interceptTime = SystemClock.elapsedRealtime();
        Logger.d(TAG, "interceptScan " + SystemClock.elapsedRealtime());
        isIntercepted = true;
        if (ScanUtil.callback != null) {
            ScanUtil.callback.onScanFinish(true);
        }
    }

    public static void interceptScanSync() {
        long now = SystemClock.elapsedRealtime();
        interceptScan();
        while (isScanning()) ;
        Logger.d(TAG, "interceptScanSync cost time=" + (SystemClock.elapsedRealtime() - now));
    }

    public static boolean isIntercepted() {
        return isIntercepted;
    }

    private static Long sSearchSize;

    private static boolean checkLocalFile(File file) {
        if (sSearchSize == null) {
            sSearchSize = SharedPreferencesUtils.getSearchSize();
        }
        if (FileUtils.isExist(file)) {
            return file.length() > sSearchSize;
        }
        return false;


    }

    public static List<String> getPathList() {
        List<String> volumePath = StorageUtil.getVolumeState(GlobalContext.get());

        Logger.i(TAG, "volume path:%s", volumePath);
        List<String> pathList = new ArrayList<>(volumePath);
//        pathList.add(StorageUtil.getTmdDir());

        String localPaths = SharedPreferencesUtils.getLocalPaths();
        if (!TextUtils.isEmpty(localPaths)) {
            JSONBuilder jsonBuilder = new JSONBuilder(localPaths);
            String[] data = jsonBuilder.getVal("data", String[].class);

            if (null != data && data.length > 0) {
                Logger.i(TAG, "local path:%s", (Object[]) data);

                for (String path : data) {
                    if (!pathList.contains(path)) {
                        pathList.add(path);
                    }
                }
            }
        }

        String innerSDCardPath = com.txznet.txz.util.StorageUtil.getInnerSDCardPath();
        Logger.i(TAG, "inner path:%s", innerSDCardPath);

        if (!pathList.contains(innerSDCardPath)) {
            pathList.add(innerSDCardPath);
        }

        return pathList;
    }
}

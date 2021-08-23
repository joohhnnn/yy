package com.txznet.proxy.server.response;

import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.util.ArrayMap;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogicBase;
import com.txznet.proxy.BuildConfig;
import com.txznet.proxy.Constant;
import com.txznet.proxy.ErrCode;
import com.txznet.proxy.ProxySession;
import com.txznet.proxy.cache.LocalBuffer;
import com.txznet.proxy.cache.TmdFile;
import com.txznet.proxy.util.GcTrigger;
import com.txznet.proxy.util.StorageUtil;
import com.txznet.proxy.util.TestUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 临界值，数据段（读取/写入 块），
 *
 * @author telenewbie
 * @version 2016年6月17日
 */
public class CacheInfo {
    private static final int DEFAULT_CACHE_SIZE = 1024 * 1024;// 默认缓存大小
    private static final long DEFAULT_SAVE_TIME = 3000;// 默认落地时间
    private File mCacheTmpFile;
    private File mFinalFile;
    private long mTotal;
    private ProxySession mSession;
    public static final String TAG = "music:cacheContent:";
    private TmdFile mTmdFile = null;// 文件句柄
    private final Object mTmdHandlerLock = new Object();
    private final static Object mCacheInfoLock = new Object();
    private final static Map<String, CacheInfo> memoryCache = new ArrayMap<>();// CacheId
    private final List<LocalBuffer> mCacheList = new ArrayList<>();
    private long mLastUseTime = SystemClock.elapsedRealtime();
    public final static int MIN_BUFFER_SIZE = 1024 * 256;

    private void lastUse() {
        mLastUseTime = SystemClock.elapsedRealtime();
        AppLogicBase.removeBackGroundCallback(mRunnableRelease);
    }

    private CacheInfo(ProxySession sess, long total) {
        String cacheId = sess.param.cacheId;
        mCacheTmpFile = new File(sess.param.cacheDir, cacheId + ".tmp");
        mFinalFile = sess.param.finalFile;
        LogUtil.logd("media session[" + sess + "] finalFile=" + sess.param.finalFile);
        mSession = sess;
        mTotal = total;
        loadCache();
    }

    public static CacheInfo createCacheInfo(ProxySession sess, long total) {
        String cacheId = sess.param.cacheId;
        File cache = new File(sess.param.cacheDir, cacheId + ".tmp");
        File finalFile = sess.param.finalFile;
        CacheInfo ret = null;
        LogUtil.logd(CacheInfo.TAG + "media session[" + sess + "]create cacheInfo begin");
        synchronized (memoryCache) {
            try {
                for (Entry<String, CacheInfo> entry : memoryCache.entrySet()) {
                    entry.getValue().releaseDelay();
                }

                if (null != memoryCache.get(cacheId)) {
                    LogUtil.logd(TAG + "hit cache, media session=" + sess);
                    ret = memoryCache.get(cacheId);
                    ret.mSession.param = sess.param;
                    ret.mSession.responses = sess.responses;
                    if (BuildConfig.DEBUG) {
                        Iterator<String> iterator = memoryCache.keySet().iterator();
                        LogUtil.logd(TAG + "-------------------------------cache output begin");
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            LogUtil.logd(CacheInfo.TAG + "media session[" + memoryCache.get(key).mSession + "]");
                        }
                        LogUtil.logd(TAG + "-------------------------------cache output end");
                    }
                    synchronized (mCacheInfoLock) {
                        if (ret.mCacheList.size() == 1) {
                            LocalBuffer buffer = ret.mCacheList.get(0);
                            if (buffer.getFromP() == 0 && buffer.getToP() == 1) {
                                if ((ret.mFinalFile != null && !ret.mFinalFile.exists())) {
                                    LogUtil.loge(TAG + "tmd file lose, reloadCache");
                                    ret.loadCache();
                                }
                            }
                        } else if (ret.mCacheTmpFile != null && !ret.mCacheTmpFile.exists()) {
                            LogUtil.loge(TAG + "tmp file lose, reloadCache");
                            ret.loadCache();
                        }
                    }
                    return ret;
                }
                LogUtil.logd(TAG + "read from tmd file:" + (finalFile != null ? finalFile.getAbsolutePath() : "no tmd file"));
                if (finalFile != null && finalFile.exists()) {
                    if (total < 0) {
                        total = TmdFile.getDataSize(finalFile);
                        if (total > 0) {
                            cache.delete();
                            ret = new CacheInfo(sess, total);
                            CacheInfo old = memoryCache.put(cacheId, ret);
                            if (old != null && old.mSession != null) {
                                old.mSession.cancelAllResponse();
                            }
                            return ret;
                        }
                    } else {
                        // TODO checkFile
                        ret = new CacheInfo(sess, total);
                        CacheInfo old = memoryCache.put(cacheId, ret);
                        if (old != null && old.mSession != null) {
                            old.mSession.cancelAllResponse();
                        }
                        return ret;
                    }
                    finalFile.delete();
                }
                LogUtil.logd(TAG + "read from tmp file:" + cache.getAbsolutePath());
                if (total < 0) {
                    total = TmdFile.getDataSize(cache);
                    if (total > 0) {
                        ret = new CacheInfo(sess, total);
                        CacheInfo old = memoryCache.put(cacheId, ret);
                        if (old != null && old.mSession != null) {
                            old.mSession.cancelAllResponse();
                        }
                        return ret;
                    }
                } else {
                    ret = new CacheInfo(sess, total);
                    CacheInfo old = memoryCache.put(cacheId, ret);
                    if (old != null && old.mSession != null) {
                        old.mSession.cancelAllResponse();
                    }
                    return ret;
                }
            } finally {
                if (ret != null) {
                    ret.lastUse();
                }
                TestUtil.printMap("memroryloCache", memoryCache);
            }
        }
        LogUtil.logd(CacheInfo.TAG + "media session[" + sess + "]create cacheInfo end");
        LogUtil.logi(CacheInfo.TAG + "media session[" + sess + "] not have cache data");

        return ret;

    }

    /**
     * 装载缓存
     */
    private void loadCache() {
        synchronized (mCacheInfoLock) {
            mCacheList.clear();
            if (mFinalFile != null) {
                if (mFinalFile.exists()) {
                    if (TmdFile.checkFile(mFinalFile, mTotal)) {
                        LogUtil.logd("media session[" + mSession + "] load cache from final file: " + mFinalFile.getPath());
                        rebuildTempCache(true);
                        mCacheList.add(LocalBuffer.buildFull(mTotal));
                        mTmdFile = TmdFile.openFile(mFinalFile, mTotal);
                        return;
                    }
                    LogUtil.logw("media session[" + mSession + "]check final file failed: " + mFinalFile.getPath());
                    mFinalFile.delete();
                }
            }
            if (TmdFile.checkFile(mCacheTmpFile)) {
                byte[] info = TmdFile.loadInfo(mCacheTmpFile, mTotal);
                if (info != null) {
                    LogUtil.logd("media session[" + mSession + "] load cache from tmp file: " + mCacheTmpFile.getPath());
                    List<LocalBuffer> buffers = LocalBuffer.buildList(info);
                    if (buffers != null) {
                        mCacheList.clear();
                        mCacheList.addAll(buffers);
                    }
                }
            }
        }
        StorageUtil.checkCacheSize(mTotal);
        if (mTmdFile != null) {
            mTmdFile.closeQuitely();
        }
        TmdFile f = TmdFile.openFile(mCacheTmpFile, mTotal);// 创建文件
        if (f == null) {
            rebuildTempCache();
            f = TmdFile.openFile(mCacheTmpFile, mTotal);
        }
        mTmdFile = f;
    }

    /**
     * 保存信息字段的缓存
     */
    private void saveCache() {
        lastUse();
        if (mFinalFile != null && mTmdFile.getFile().getAbsolutePath().equals(mFinalFile.getAbsolutePath())) {
            return;
        }
        // TODO buffer区间合并，这里的算法有BUG
        for (int i = 0; i < mCacheList.size() - 1; ) {
            if (mCacheList.get(i + 1).getFrom() == mCacheList.get(i).getTo() + 1
                    && mCacheList.get(i).getBufferData() == null
                    && mCacheList.get(i + 1).getBufferData() == null) {
                mCacheList.get(i + 1).setFrom(mCacheList.get(i).getFrom());
                mCacheList.remove(i);
                continue;
            }
            ++i;
        }

        byte[] info = LocalBuffer.toBytes(mCacheList);
        if (!mTmdFile.saveInfo(info)) {
            LogUtil.logw("media session[" + mSession + "]save cache index failed size: " + info.length);
            // rebuildTempCache(false);
        }

        // 判断是否已经下载完整
        int downloadSize = 0;

        for (int i = 0; i < mCacheList.size(); i++) {
            LocalBuffer buffer = mCacheList.get(i);
            if (buffer != null) {//存储完毕才可以计算下载的量
                downloadSize += buffer.getTo() - buffer.getFrom() + 1;
            }
        }

        if (downloadSize >= mTotal) {
            LogUtil.logi("media session[" + mSession + "]download cache data complete");
            if (mFinalFile != null) {
                LogUtil.logi("media session[" + mSession + "]mFinalFile=" + mFinalFile.getAbsolutePath());
                try {
                    mTmdFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String desc = "cache check, curr_tmd_size=" + StorageUtil.formatSize(StorageUtil.getTmdSize()) + ", new_tmd_size=" + StorageUtil.formatSize(mTotal) + ", disk_space=" + StorageUtil.formatSize(StorageUtil.getAvailableSize());
                LogUtil.d(TAG + desc);
                if (StorageUtil.getTmdSize() + mTotal >= StorageUtil.getMaxTMDSize()) {
                    mFinalFile = mCacheTmpFile;
                    desc = "cache size limit";
                    LogUtil.e(TAG + desc);
                    mSession.param.callback.onError(ErrCode.ERROR_CLIENT_MEDIA_CACHE_SIZE_LIMIT, desc, "缓存已达上限");
                } else if (!StorageUtil.isDiskSpaceEnough()) {
                    mFinalFile = mCacheTmpFile;
                    desc = "disk space is not enough";
                    LogUtil.e(TAG + desc);
                    mSession.param.callback.onError(ErrCode.ERROR_CLIENT_MEDIA_DISK_SPACE_INSUFFICIENT, desc, "磁盘空间不足");
                } else {
                    mFinalFile.getParentFile().mkdirs();
                    mCacheTmpFile.renameTo(mFinalFile);
                    mSession.param.callback.onDownloadComplete();
                }
                //【【同听4.4.1】【播放异常-3】缓存达到上限后，正在播放在线歌曲“有你的快乐”，重新打开同听，报播放异常-3，切到下一首歌播放】
                //https://www.tapd.cn/21711881/bugtrace/bugs/view?bug_id=1121711881001004537
                mTmdFile = TmdFile.openFile(mFinalFile, mTotal);
                mTmdFile.saveInfo(mSession.param.info);
            }
        }
    }

    public boolean canSaveCacheFile() {
        //1. 必须是歌曲
        //2. 必须在设置的最大可存储空间内
        //3. 剩余空间必须大于50m的情况下
        if (mFinalFile == null) {//歌曲才会设置mFileFile
            return false;
        }
        if (StorageUtil.getTmdSize() >= StorageUtil.getMaxTMDSize()) {
            return false;
        }
        return StorageUtil.isDiskSpaceEnough();

    }

    private void debugCacheList(String prefix) {
        if (BuildConfig.DEBUG) {
            LogUtil.logd("media session[" + mSession + "]" + prefix + ": cache count=" + mCacheList.size());
            for (int i = 0; i < mCacheList.size(); ++i) {
                LocalBuffer buffer = mCacheList.get(i);
                LogUtil.logd("media session[" + mSession + "]" + prefix + ": [" + buffer.getFrom() + "~" + buffer.getTo() + "]|" + mTotal);
            }
        }
    }

    private boolean isFinished;

    //表示是否需要完成任务并关闭
    public void setFinishTask(boolean isFinish) {
        isFinished = isFinish;
    }

    private byte[] copyBufferData(byte[] data, int offset, int len, boolean need) {
        if (!need) {
            return data;
        }
        byte[] tmpdata = new byte[len < MIN_BUFFER_SIZE ? MIN_BUFFER_SIZE : len];
        LogUtil.logd("new byte " + tmpdata.length);
        System.arraycopy(data, offset, tmpdata, 0, len);
        return tmpdata;
    }

    /**
     * 合并缓存计算，先不管crc32
     */
    private void mergeCache(long from, long to, byte[] data, int offset, int len) {
        if (Constant.ISTEST) {
            LogUtil.logd("media session[" + mSession + "] merge cache: [" + from + "~" + to + "]|" + mTotal);
        }
        boolean needCopyData = true;
        byte[] newdata = null;
        int i = 0;
        synchronized (mCacheInfoLock) {
            if (mCacheList.size() == 1 && mCacheList.get(0).getFromP() == 0 && mCacheList.get(0).getToP() == 1) {
                return;
            }
            while (i < mCacheList.size()) {
                LocalBuffer buffer = mCacheList.get(i);
                if (from < buffer.getFrom()) {
                    // from在当前block左侧
                    if (to < buffer.getFrom() - 1) {// 不相交
                        break; // 在当前block左侧，完全无法叠加，跳出插入到当前位置
                    } else if (to >= buffer.getTo()) {// 包含
                        buffer.setBufferData(null);
                        mCacheList.remove(i); // 新插入的block大于当前block，抛弃当前block，与下一块比较
                    } else {// 交集
                        // 有数据和没有数据进行合并生成两个字段
                        if (null != buffer.getBufferData()) {// 该段也是有数据的则直接进行合并
                            // 拼起来重复块
                            int newlen = (int) (buffer.getTo() - from) + 1;
                            if (!needCopyData && data.length >= newlen) {
                                newdata = data;
                            } else {
                                newdata = new byte[newlen < MIN_BUFFER_SIZE ? MIN_BUFFER_SIZE : newlen];
                                LogUtil.logd("new byte " + newdata.length);
                                System.arraycopy(data, offset, newdata, 0, (int) (to - from + 1));
                            }
                            System.arraycopy(buffer.getBufferData(), (int) (to - buffer.getFrom() + 1), newdata, (int) (to - from + 1), newlen - ((int) (to - from + 1)));
                            buffer.setFrom(from); // 更新当前block
                            buffer.setBufferData(newdata);// 截断一部分数据
                            writeTestData("merge", from, buffer.getTo(), newdata, offset);
                            return;
                        } else {
                            buffer.setFrom(to + 1);
                            break;
                        }
                    }
                } else if (from > buffer.getTo() + 1) {
                    ++i;
                    // from在当前block右侧，下一块处理
                } else {
                    if (to <= buffer.getTo()) {
                        if (null != buffer.getBufferData()) {
                            // 更新老的数据
                            System.arraycopy(data, offset, buffer.getBufferData(), (int) (from - buffer.getFrom()), (int) (to - from + 1));
                        } else {
                            if (from == buffer.getFrom()) {
                                if (to == buffer.getTo()) {
                                    buffer.setBufferData(copyBufferData(data, offset, len, needCopyData));
                                    writeTestData("update", from, to, data, offset);
                                    return;
                                } else {
                                    buffer.setFrom(to + 1);
                                    break;
                                }
                            } else {
                                if (to == buffer.getTo()) {
                                    buffer.setTo(from - 1);
                                    ++i;
                                    break;
                                } else {
                                    buffer.setFrom(to + 1);
                                    mCacheList.add(i, LocalBuffer.build(buffer.getFrom(), from - 1, buffer.getTotal(), LocalBuffer.CRC32, null));
                                    break;
                                }
                            }
                        }
                        return; // 当前block已经包含了from~to
                    } else {
                        if (null == buffer.getBufferData()) {
                            ++i;
                            buffer.setTo(from - 1);
                        } else {
                            int newlen = (int) (to - buffer.getFrom() + 1);
                            if (buffer.getBufferData().length >= newlen) {
                                newdata = buffer.getBufferData();
                            } else {
                                newdata = new byte[newlen < MIN_BUFFER_SIZE ? MIN_BUFFER_SIZE : newlen];
                                LogUtil.logd("new byte " + newdata.length);
                                System.arraycopy(buffer.getBufferData(), 0, newdata, 0, (int) (from - buffer.getFrom()));
                            }
                            System.arraycopy(data, offset, newdata, (int) (from - buffer.getFrom()), (int) (to - from + 1));
                            data = newdata;
                            offset = 0;
                            len = newlen;
                            needCopyData = false;
                            // from可以和当前block衔接
                            buffer.setBufferData(null);
                            mCacheList.remove(i);
                            from = buffer.getFrom();
                        }
                    }
                }
            }
            writeTestData("insert", from, to, data, offset);
            mCacheList.add(i, LocalBuffer.build(from, to, mTotal, LocalBuffer.CRC32, copyBufferData(data, offset, len, needCopyData)));
        }
    }

    private void writeTestData(String type, long from, long to, byte[] data,
                               int offset) {
        // TODO：测试数据
        if (false) {
            File file = new File(Environment.getExternalStorageDirectory(),
                    "txz/audio/mem/" + mSession.param.cacheId
                            + "_" + type + "_" + from + "_" + to + ".dat");
            file.getParentFile().mkdirs();
            FileOutputStream fosMemCache = null;
            try {
                fosMemCache = new FileOutputStream(file);
                fosMemCache.write(data, offset, (int) (to - from + 1));
                fosMemCache.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fosMemCache != null) {
                    try {
                        fosMemCache.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void rebuildTempCache() {
        rebuildTempCache(true);
    }

    private void rebuildTempCache(boolean cleanList) {
        if (cleanList) {
            synchronized (mCacheInfoLock) {
                mCacheList.clear();
            }
        }
        mCacheTmpFile.delete();
    }

    /**
     * 重置缓存
     */
    protected void rebuild() {
        mFinalFile.delete();
        rebuildTempCache();
    }

    /**
     * 更新缓存数据
     */
    public boolean addCacheBlock(long from, byte[] data, int offset, int len) {
        lastUse();
        if (len == 0) {
            return true;
        }
        long to = from + len - 1;
        if (from >= mTotal || to >= mTotal) {
            LogUtil.loge("media session[" + mSession + "]write cache data too long: from=" + from + ", len=" + len + ", total=" + mTotal);
            return false;
        }
        long startTime = SystemClock.elapsedRealtime();
        synchronized (mCacheInfoLock) {
            debugCacheList("before merge cache");
            mergeCache(from, to, data, offset, len);
            if (mCacheList.size() > 0) {
                int cacheTotalSize = 0;
                for (int i = 0; i < mCacheList.size(); i++) {
                    if (null != mCacheList.get(i).getBufferData()) {
                        cacheTotalSize += mCacheList.get(i).getTo() - mCacheList.get(i).getFrom() + 1;
                    }
                }
                AppLogicBase.removeBackGroundCallback(mRunnableWriteCacheFile);
                if (cacheTotalSize > DEFAULT_CACHE_SIZE) {
                    AppLogicBase.runOnBackGround(mRunnableWriteCacheFile, 0);
                } else {
                    AppLogicBase.runOnBackGround(mRunnableWriteCacheFile, DEFAULT_SAVE_TIME);
                }
            }
            debugCacheList("after merge cache");
        }
        if (Constant.ISTEST) {
            LogUtil.logd("mergeCache use time:" + (SystemClock.elapsedRealtime() - startTime));
        }
        return true;
    }

    // 当data超过3s就落地文件，当文件大小超过100K就落地文件
    private Runnable mRunnableWriteCacheFile = new Runnable() {
        @Override
        public void run() {
            lastUse();
            synchronized (mTmdHandlerLock) {
                if (mTmdFile == null) {
                    AppLogicBase.runOnBackGround(this, DEFAULT_SAVE_TIME);
                    return;
                }
                synchronized (mCacheInfoLock) {
                    LogUtil.d("mRunnableWriteCacheFile before" + mCacheList);
                    int size = mCacheList.size();
                    for (int i = size - 1; i >= 0; i--) {
                        LocalBuffer cache = mCacheList.get(i);
                        if (cache.getBufferData() != null) {
                            if (mTmdFile.saveDataBlock(cache.getFrom(), cache.getBufferData(), 0, (int) (cache.getTo() - cache.getFrom() + 1))) {
                                cache.setBufferData(null);// 清空内存数据
                                GcTrigger.runGc();
                                LogUtil.d("mRunnableWriteCacheFile release " + cache);
                            } else {
                                cache.setBufferData(null);// 清空内存数据
                                mCacheList.remove(i);
                            }
                        }
                    }
                    LogUtil.d("mRunnableWriteCacheFile after" + mCacheList);
                    saveCache();
                    GcTrigger.runGc();
                }
            }
        }
    };

    public List<LocalBuffer> getCacheBlocks() {
        lastUse();

        List<LocalBuffer> buffers = new ArrayList<>();
        synchronized (mCacheInfoLock) {
            // 剔除掉data字段的数据
            if (mCacheList.size() > 0) {
                for (int i = 0; i < mCacheList.size(); i++) {
                    buffers.add(LocalBuffer.build(mCacheList.get(i).getFrom(),
                            mCacheList.get(i).getTo(), mCacheList.get(i)
                                    .getTotal(), mCacheList.get(i).getCrc32()));
                }
            }
        }
        return buffers;
    }

    public long getTotalSize() {
        lastUse();
        return mTotal;
    }

    public static class CacheData {
        public byte[] data = null;
        public int len = 0;
        public long next = -1;
    }

    public boolean getCacheDataOri(CacheData data, long from, long to, boolean greedy) {
        lastUse();
        synchronized (mTmdHandlerLock) {
            if (BuildConfig.DEBUG) {
                LogUtil.logd("media session[" + mSession + "]read cache data: from=" + from + ", to=" + to + ", cache=" + mCacheList);
            }
            synchronized (mCacheInfoLock) {
                int i = 0;
                data.len = 0; // 默认没有取到数据
                data.next = -1; // 默认没有下一块
                for (; i < mCacheList.size(); ++i) {
                    LocalBuffer buffer = mCacheList.get(i);
                    LogUtil.logd("media session[" + mSession + "]" + from + "(" + buffer.getFrom() + "/" + buffer.getTo() + ")");
                    // 左边无叠加，不存在
                    if (to < buffer.getFrom()) {
                        data.next = buffer.getFrom();
                        return false;
                    }
                    // from处于该块中
                    if (from >= buffer.getFrom() && from <= buffer.getTo()) {
                        int endIndex = i;
                        for (int j = i; j < mCacheList.size(); ++j) {
                            if (j > i && mCacheList.get(j).getFrom() != mCacheList.get(j - 1).getTo() + 1) {
                                break;
                            }
                            if (j < mCacheList.size() - 1) {
                                data.next = mCacheList.get(j + 1).getFrom();
                            } else {
                                data.next = -1;
                            }
                            if (greedy && to <= mCacheList.get(j).getTo()) {
                                to = mCacheList.get(j).getTo();
                            }
                            endIndex = j;
                        }
                        if (to > mCacheList.get(endIndex).getTo()) {
                            to = mCacheList.get(endIndex).getTo();
                        }
                        data.len = (int) (to - from + 1);
                        if (data.data == null || to - from + 1 > data.data.length) {
                            try {
                                data.data = new byte[data.len];
                                LogUtil.logd("new byte " + data.data.length);
                            } catch (Exception e) {
                                LogUtil.loge("allocate memory error", e);
                                data.data = null;
                                return false;
                            } catch (java.lang.Error error) {
                                LogUtil.loge("allocate memory error", error);
                                data.data = null;
                                return false;
                            }
                        }
                        int offset = 0;
                        for (int j = i; j <= endIndex; ++j) {
                            int l = 0;
                            if (from < mCacheList.get(j).getFrom()) {
                                l = (int) (mCacheList.get(j).getTo() - mCacheList.get(j).getFrom() + 1);
                            } else {
                                l = (int) (mCacheList.get(j).getTo() - from + 1);
                            }
                            if (offset + l > data.len) {
                                l = data.len - offset;
                            }
                            LogUtil.logd("media session[" + mSession + "]write begin data, j=" + j + ",from=" + from + "/" + mCacheList.get(j).getFrom() + "" + ",l=" + l);
                            if (l <= 0)
                                break;
                            if (mCacheList.get(j).getBufferData() != null) {
                                LogUtil.logd("media session[" + mSession + "]write begin" + (int) (from - mCacheList.get(j).getFrom()) + "" +
                                        ",l=" + l + "" +
                                        ",cache length=" + mCacheList.get(j).getBufferData().length + "" +
                                        ",offset=" + offset);
                                if (from < mCacheList.get(j).getFrom()) {
                                    System.arraycopy(mCacheList.get(j).getBufferData(), 0, data.data, offset, l);
                                } else {
                                    System.arraycopy(mCacheList.get(j).getBufferData(), (int) (from - mCacheList.get(j).getFrom()), data.data, offset, l);
                                }
                                LogUtil.logd("media session[" + mSession + "]write end");
                            } else {
                                boolean readSucc = false;
                                try {
                                    if (mTmdFile == null) {
                                        loadCache();
                                    }
                                    readSucc = mTmdFile.readData(data.data, offset, (int) from, l);
                                } catch (Exception e) {
                                    LogUtil.loge("media session[" + mSession + "]read excepiton: ", e);
                                }
                                LogUtil.logw("media session[" + mSession + "]read finished j=" + j + ",read from tmd:" + readSucc);
                                if (!readSucc) {
                                    data.len = offset;
                                    return true;
                                }
                            }
                            LogUtil.logw("media session[" + mSession + "]read finished j=" + j);
                            offset += l;
                        }
                        return true;
                    }
                }
            }
        }
        return true;
    }

    private void releaseDelay() {
        AppLogicBase.removeBackGroundCallback(mRunnableRelease);
        AppLogicBase.runOnBackGround(mRunnableRelease, 5000);
        if (BuildConfig.DEBUG) {
            LogUtil.logd(CacheInfo.TAG + "media session[" + mSession + "]release delay");
        }
    }

    private Runnable mRunnableRelease = new Runnable() {
        @Override
        public void run() {
            // 缓存文件没有创建好，等待5秒后再释放
            if (mTmdFile == null) {
                releaseDelay();
                return;
            }

            // 缓存数据还有没落地的，等待5秒后再释放
            synchronized (mTmdHandlerLock) {
                synchronized (mCacheInfoLock) {
                    for (int i = 0; i < mCacheList.size(); ++i) {
                        if (mCacheList.get(i).getBufferData() != null) {
                            releaseDelay();
                            return;
                        }
                    }
                }
            }

            String cacheId = mSession.param.cacheId;
            LogUtil.logd(CacheInfo.TAG + "media session[" + mSession + "]release begin");
            synchronized (memoryCache) {
                // 缓存最后3秒内有被使用
                if (SystemClock.elapsedRealtime() - mLastUseTime < 3000) {
                    releaseDelay();
                    return;
                }

                CacheInfo cacheInfo = memoryCache.remove(cacheId);
//                if (cacheInfo != null) {
//                    if (cacheInfo.mCacheList != null) {
//                        for (LocalBuffer buffer : cacheInfo.mCacheList) {
//                            buffer.setBufferData(null);
//                        }
//                        cacheInfo.mCacheList.clear();
//                    }
//                }
                mTmdFile.closeQuitely();
                mTmdFile = null;
            }
            LogUtil.logd(CacheInfo.TAG + "media session[" + mSession + "]release end, " + cacheId);
            GcTrigger.runGc();
        }
    };
}

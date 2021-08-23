package com.txznet.audio.player.audio;

import android.os.Environment;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.txznet.music.utils.Utils.getProcessIdByPkgName;

/**
 * TXZ_MEDIA_DATA_FILE(TXZ媒体数据文件)协议描述：
 *
 * @author bihongpi
 * @固定文件头 + 预留段 + 数据段 + 信息段(缓存信息或音频信息)
 * @文件头定义：
 * @3个字节: TMD
 * @1个字节: 版本号1~255，默认1，V
 * @4个字节: 随机值，用于加密，R
 * @8个字节: 校验值，用于校验，C
 * @4个字节: 预留段长度，默认0
 * @4个字节: 数据段长度，真实媒体数据长度，L ^ R
 * @4个字节: 信息段长度，存储媒体描述信息，L ^ R
 * @段数据存储算法：KEY(L ^ R) ^ 序列化数据
 */

public class TmdFile implements Closeable {
    private static String TAG = "music:tmdfile:";

    private static boolean isTestFullData = false;//tmd文件中属于数据的测试开关：测试完整数据

    // 保存数据块，失败返回false
    public static boolean saveDataBlock(File tmdFile, long total, long seek,
                                        byte[] data, int offset, int len) {

        TmdFile f = TmdFile.openFile(tmdFile, total);
        if (f == null)
            return false;
        try {
            return f.saveDataBlock(seek, data, offset, len);
        } finally {
            f.closeQuitely();
        }
    }

    private static void saveDataToTestFile(File tmdFile, long seek, byte[] data, int offset, int len) {
        if (isTestFullData) {
            File testFile = new File(Environment.getExternalStorageDirectory() + "/txz/audio/test", "mp3_" + tmdFile.getName());
            if (!testFile.exists()) {
                testFile.getParentFile().mkdirs();
                try {
                    testFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            RandomAccessFile mp3File = null;
            try {
                mp3File = new RandomAccessFile(testFile, "rw");
                mp3File.seek(seek);
                mp3File.write(data, offset, len);
                mp3File.seek(0);//seek到原位置
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mp3File != null) {
                    try {
                        mp3File.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


        }
    }


    public static boolean saveMoreDataBlock(File tempFile, long total,
                                            List<LocalBuffer> caches) {
        if (null != caches && caches.size() > 0) {
            TmdFile f = TmdFile.openFile(tempFile, total);
            if (f == null) {
                return false;
            }
            synchronized (caches) {
                for (int i = 0; i < caches.size(); i++) {
                    LocalBuffer cache = caches.get(i);
                    if (cache != null) {
                        if (cache.getBufferData() != null) {
                            f.saveDataBlock(cache.getFrom(),
                                    cache.getBufferData(), 0,
                                    cache.getBufferData().length);
                        }
                    }
                    cache.setBufferData(null);// 清空数据
                }

            }
            f.closeQuitely();
        }
        return true;
    }

    public static boolean saveDataBlock(File tmdFile, long total, int seek,
                                        byte[] data) {
        return saveDataBlock(tmdFile, total, seek, data, 0, data.length);
    }

    // 保存信息，失败返回false
    public static boolean saveInfo(File tmdFile, long total, byte[] data,
                                   int offset, int len) {
        TmdFile f = TmdFile.openFile(tmdFile, total);
        if (f == null)
            return false;
        try {
            return f.saveInfo(data, offset, len);
        } finally {
            f.closeQuitely();
        }
    }

    public static boolean saveInfo(File tmdFile, long total, byte[] data) {
        return saveInfo(tmdFile, total, data, 0, data.length);
    }

    // //////////////////////////////////////////////////////////////////////

    // 校验文件
    public static boolean checkFile(File tmdFile, long total) {
        TmdFile f = openFile(tmdFile, total);
        if (f == null) {
            return false;
        }
        f.closeQuitely();
        return true;
    }

    // 校验文件
    public static boolean checkFile(File tmdFile) {
        TmdFile f = openFile(tmdFile);
        if (f == null) {
            return false;
        }
        f.closeQuitely();
        return true;
    }

    // 合法文件返回数据段长度，否则返回-1
    public static long getDataSize(File tmdFile) {
        TmdFile f = openFile(tmdFile);
        if (f != null) {
            int ret = f.mDataSize;
            f.closeQuitely();
            return ret;
        }
        return -1;
    }

    // 读取信息，失败返回null
    public static byte[] loadInfo(File tmdFile, long total) {
        TmdFile f = TmdFile.openFile(tmdFile, total);
        if (f == null)
            return null;
        try {
            return f.loadInfo();
        } finally {
            f.closeQuitely();
        }
    }

    // 读取数据，返回实际读取到的数据量，失败返回null
    public static byte[] readData(File tmdFile, long total, int offset, int len) {
        TmdFile f = TmdFile.openFile(tmdFile, total);
        if (f == null)
            return null;
        try {
            return f.readData(offset, len);
        } finally {
            f.closeQuitely();
        }
    }

    // 读取数据，返回实际读取到的数据量，失败返回false
    public static boolean readData(File tmdFile, long total, byte[] data,
                                   int offset, int location, int len) {
        TmdFile f = TmdFile.openFile(tmdFile, total);
        if (f == null)
            return false;
        try {
            return f.readData(data, offset, location, len);
        } finally {
            f.closeQuitely();
        }
    }

    // //////////////////////////////////////////////////////////////////////

    private static byte calKey(int key) {
        return (byte) (((key >> 0) & 0xFF) | ((key >> 8) & 0xFF)
                | ((key >> 16) & 0xFF) | ((key >> 24) & 0xFF));
    }

    // 打开已存在的文件，如果校验不通过返回null
    public static TmdFile openFile(File tmdFile) {
        return openFile(tmdFile, -1);
    }

    // 通过长度打开文件，如果打开已存在的文件，则会校验文件，校验失败返回null，不存在则创建
    public static TmdFile openFile(File tmdFile, long total) {
        return openFile(tmdFile, total, true);
    }

    // 通过长度打开文件，如果打开已存在的文件，则会校验文件，校验失败返回null，不存在则创建
    public static TmdFile openFile(File tmdFile, long total, boolean isLock) {
        TmdFile errFile = null;
        try {
            if (tmdFile.exists()) {
                TmdFile f = errFile = new TmdFile();
                f.mFilePath = tmdFile;
                f.mFile = new RandomAccessFile(tmdFile, "rw");
                if (isLock) {
                    f.mFileLock = lockFile(f.mFile, true);
                } else {
//					f.mFileLock = f.mFile.getChannel().tryLock();
//					if (f.mFileLock == null) {
//						LogUtil.logd(TAG+" file has been locked");
//						return null;
//					}
                }
                byte T = f.mFile.readByte();
                byte M = f.mFile.readByte();
                byte D = f.mFile.readByte();
                if (T != (byte) 'T' || M != (byte) 'M' || D != (byte) 'D') {
                    // 文件头不对
                    return null;
                }
                f.mVersion = f.mFile.readByte();
                if (f.mVersion != 1) {
                    // 版本号不支持
                    return null;
                }
                f.mRandomKey = f.mFile.readInt();
                f.mCrc32 = f.mFile.readLong();
                int key = f.mFile.readInt();
                f.mReseveSize = key ^ f.mRandomKey;
                key = f.mFile.readInt();
                f.mDataSize = key ^ f.mRandomKey;
                if (total >= 0 && f.mDataSize != total) {
                    // 文件长度与入参不一致
                    return null;
                }
                f.mDataKey = calKey(key);
                key = f.mFile.readInt();
                f.mInfoSize = key ^ f.mRandomKey;
                f.mInfoKey = calKey(key);
                f.mHeadSize = (int) f.mFile.getFilePointer();
                f.mDataOffset = f.mHeadSize + f.mReseveSize;
                f.mInfoOffset = f.mDataOffset + f.mDataSize;
                if (f.mInfoOffset + f.mInfoSize != tmdFile.length()) {
                    // 文件总长度不对
                    return null;
                }
                // TODO 校验CRC32
                errFile = null;
                f.mFile.seek(f.mDataOffset);
                long test = f.mFile.readLong();
                if (test == 0) {
                    return f;
                }
                return f;
            } else {
                if (total < 0) {
                    // 文件不存在
                    return null;
                }
                TmdFile f = errFile = new TmdFile();
                f.mFilePath = tmdFile;
                f.mVersion = 1;
                f.mReseveSize = 0;
                f.mDataSize = (int) total;
                f.mInfoSize = 0;
                f.mCrc32 = new Random().nextLong(); // TODO 生成校验
                tmdFile.getParentFile().mkdirs();
                f.mFile = new RandomAccessFile(tmdFile, "rw");
                f.mFileLock = f.mFile.getChannel().lock();
                f.mFile.writeByte('T');
                f.mFile.writeByte('M');
                f.mFile.writeByte('D');
                f.mFile.writeByte(f.mVersion);
                f.mRandomKey = new Random().nextInt();
                f.mFile.writeInt(f.mRandomKey);
                f.mFile.writeLong(f.mCrc32);
                f.mFile.writeInt(f.mReseveSize ^ f.mRandomKey);
                f.mFile.writeInt(f.mDataSize ^ f.mRandomKey);
                f.mFile.writeInt(f.mInfoSize ^ f.mRandomKey);
                f.mHeadSize = (int) f.mFile.getFilePointer();
                f.mDataOffset = f.mHeadSize + f.mReseveSize;
                f.mInfoOffset = f.mDataOffset + f.mDataSize;
                f.mFile.setLength(f.mInfoOffset + f.mInfoSize);
                f.mDataKey = calKey(f.mDataSize ^ f.mRandomKey);
                f.mInfoKey = calKey(f.mInfoSize ^ f.mRandomKey);
                errFile = null;
                return f;
            }
        } catch (Exception e) {
            LogUtil.loge(TAG, e);
            return null;
        } finally {
            if (errFile != null) {
                errFile.closeQuitely();
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////

    private File mFilePath;
    private RandomAccessFile mFile;
    private Object mFileLock;
    private byte mVersion;
    private int mRandomKey;
    private long mCrc32;
    private int mHeadSize;
    private int mReseveSize;
    private int mDataOffset;
    private int mDataSize;
    private int mInfoOffset;
    private int mInfoSize;
    private byte mDataKey;
    private byte mInfoKey;
    private byte[] mBuffer = new byte[200 * 1024];

    public File getFile() {
        return mFilePath;
    }

    public void closeQuitely() {
        if (mFileLock != null) {
            try {
                releaseLock(mFileLock);
            } catch (Exception e) {
                LogUtil.loge(TAG + "release lock", e);
            }
            mFileLock = null;
        }
        if (mFile != null) {
            try {
                mFile.close();
            } catch (Exception e) {
                LogUtil.loge(TAG + "close file", e);
            }
            mFile = null;
        }
    }

    // 关闭文件
    @Override
    public void close() throws IOException {
        if (mFile != null) {
            mFile.close();
            mFile = null;
        }
    }

    private void readDataHere(byte[] data, int offset, int len, byte key)
            throws IOException {
        int t = 0;
        while (t < len) {
            int r = mFile.read(data, offset + t, len - t);
            t += r;
        }
        for (int i = 0; i < len; ++i) {
            data[offset + i] ^= key;
        }
    }

    private void writeDataHere(byte[] data, int offset, int len, byte key)
            throws IOException {
        int n = 0;
        for (int i = 0; i < len; ++i) {
            mBuffer[n] = (byte) (data[offset + i] ^ key);
            ++n;
            if (n >= mBuffer.length || i == len - 1) {
                mFile.write(mBuffer, 0, n);
                n = 0;
            }
        }
    }

    // 保存数据块，失败返回false
    public boolean saveDataBlock(long seek, byte[] data, int offset, int len) {
        try {
            if (seek < 0 || seek + len > mDataSize) {
                LogUtil.loge(TAG + "saveDataBlock:" + seek + "/" + len + "(" + mDataSize + ")");
                return false;
            }
            saveDataToTestFile(mFilePath, seek, data, offset, len);
            mFile.seek(mDataOffset + seek);
            writeDataHere(data, offset, len, mDataKey);
            mFile.seek(mDataOffset);
            long test = mFile.readLong();
            if (test == 0) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.loge(TAG + "saveDataBlock", e);
            return false;
        }
        return true;
    }

    public boolean saveDataBlock(long seek, byte[] data) {
        return saveDataBlock(seek, data, 0, data.length);
    }

    // 保存信息，失败返回false
    public boolean saveInfo(byte[] data, int offset, int len) {
        try {
            // 记录信息段长度
            mInfoSize = len;
            mFile.seek(24);
            int key = mInfoSize ^ mRandomKey;
            mFile.writeInt(key);
            mInfoKey = calKey(key);
            // 记录信息段
            mFile.seek(mInfoOffset);
            writeDataHere(data, offset, len, mInfoKey);
            long location = mFile.getFilePointer();
            mFile.setLength(location);
            mFile.seek(mDataOffset);
            long test = mFile.readLong();
            if (test == 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean saveInfo(byte[] data) {
        return saveInfo(data, 0, data.length);
    }

    // //////////////////////////////////////////////////////////////////////

    // 读取信息，失败返回null
    public byte[] loadInfo() {
        // 跳过预留段，数据段，信息段长度
        try {
            mFile.seek(mInfoOffset);
            byte[] buffer = new byte[mInfoSize];
            readDataHere(buffer, 0, mInfoSize, mInfoKey);
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 读取数据
    public boolean readData(byte[] data, int offset, int location, int len) {
        if (location < 0 || location + len > mDataSize) {
            return false;
        }
        try {
            mFile.seek(mDataOffset + location);
            readDataHere(data, offset, len, mDataKey);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 读取数据，失败返回null
    public byte[] readData(int location, int len) {
        if (location < 0 || location + len > mDataSize) {
            return null;
        }
        byte[] buffer = new byte[len];
        try {
            mFile.seek(mDataOffset + location);
            readDataHere(buffer, 0, len, mDataKey);
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 读取一个字节，失败返回-1
    private int readData(int offset) {
        if (offset >= mDataSize) {
            return -1;
        }
        try {
            mFile.seek(mDataOffset + offset);
            return mFile.readByte() ^ mDataKey;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // //////////////////////////////////////////////////////////////////////

    // 打开数据段的输入流
    public static InputStream openInputStream(File tmdFile, long total) {
        final TmdFile f = TmdFile.openFile(tmdFile, (int) total);
        if (f == null) {
            return null;
        }
        return new InputStream() {
            int mSeek = 0;

            @Override
            public int read() throws IOException {
                if (mSeek >= f.mDataSize) {
                    return -1;
                }
                return f.readData(mSeek++);
            }

            @Override
            public int read(byte[] buffer, int byteOffset, int byteCount)
                    throws IOException {
                if (mSeek >= f.mDataSize) {
                    return -1;
                }
                // 判断能读取到的数据量
                if (byteCount > f.mDataSize - mSeek) {
                    byteCount = f.mDataSize - mSeek;
                }
                f.mFile.seek(f.mDataOffset + mSeek);
                f.readDataHere(buffer, byteOffset, byteCount, f.mDataKey);
                mSeek += byteCount;
                return byteCount;
            }

            @Override
            public void close() throws IOException {
                f.closeQuitely();
            }

            @Override
            public long skip(long byteCount) throws IOException {
                return mSeek += byteCount;
            }

            @Override
            public int available() throws IOException {
                int len = f.mDataSize - mSeek;
                if (len > 0) {
                    return len;
                }
                return 0;
            }
        };
    }


    /**
     * 获取文件锁
     *
     * @param isNew 是否是新文件
     */
    private static Object lockFile(RandomAccessFile file, boolean isNew) throws IOException {
        //如果是单进程（则采用文件读写锁）
        if (android.os.Process.myPid() == getProcessIdByPkgName(GlobalContext.get().getApplicationInfo().packageName)) {
            ReadWriteLock readWriteLock;
            if (isNew) {
                readWriteLock = new ReentrantReadWriteLock();
                readWriteLock.writeLock().lock();
                return readWriteLock.writeLock();
            }
            return null;
        } else {
            //如果是多进程（则采用FileChannel的lock）
            return file.getChannel().lock();
        }
    }

    private static void releaseLock(Object lock) throws IOException {
        if (lock != null) {
            if (lock instanceof Lock) {
                ((Lock) lock).unlock();
            } else if (lock instanceof FileLock) {
                ((FileLock) lock).release();
            }
        }
    }

}

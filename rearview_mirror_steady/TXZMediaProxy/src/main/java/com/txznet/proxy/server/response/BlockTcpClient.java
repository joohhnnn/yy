package com.txznet.proxy.server.response;

import android.os.SystemClock;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.proxy.Constant;
import com.txznet.proxy.util.TimeUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public abstract class BlockTcpClient {
    public static final String TAG = "music:tcp:";
    private static final int DEFAULT_SUGGEST_RETRY_DELAY_TIME = 1000;
    private static final int DEFAULT_SELECT_TIMEOUT = 1000;

    private int mConnectTimeout = 5000;
    private int mReadTimeout = 5000;

    private int sleepTime = 1;//Selector.select(sleepTime);//因为系统的该方法不能阻塞，导致这里必须使用睡眠来使用
    /**
     * 其实read返回0有3种情况，
     * 一是某一时刻socketChannel中当前（注意是当前）没有数据可以读，这时会返回0，
     * 其次是bytebuffer的position等于limit了，即bytebuffer的remaining等于0，这个时候也会返回0，
     * 最后一种情况就是客户端的数据发送完毕了，这个时候客户端想获取服务端的反馈调用了recv函数，若服务端继续read，这个时候就会返回0。
     */

    protected boolean mConnected;
    protected boolean mClosed;
    protected boolean mCanceled;
    protected boolean mIsError;
    protected boolean mIsTimeout;

    public BlockTcpClient() {
    }

    private SocketChannel mSocketChannel;
    private Selector mSelector;

    int mSuggestRetryDelay = 0;

    protected void setSuggestRetryDelay(int delay) {
        mSuggestRetryDelay = delay;
    }

    public int getSuggestRetryDelay() {
        return mSuggestRetryDelay;
    }

    public abstract void onConnect() throws IOException;

    public void onIdle() throws IOException {

    }

    public void onDisconnect() throws IOException {
        LogUtil.logd(TAG + "onDisconnect");
    }

    public abstract void onRead(byte[] data, int offset, int len)
            throws IOException;

    public void onError(String errDesc) throws IOException {

    }

    public void onConnectTimeout() throws IOException {

    }

    public void onReadTimeout() throws IOException {

    }

    private void _onError(String errDesc) throws IOException {
        mIsError = true;
        onError(errDesc);
    }

    private void reset() {
        release();
        mConnected = false;
        mClosed = false;
        mCanceled = false;
        mIsError = false;
        mIsTimeout = false;
        mSuggestRetryDelay = 0;
    }

    public void connect(String server, int port) throws IOException {
        LogUtil.logi(Constant.SPEND_TAG + "start get net data ");
        LogUtil.logd(TAG + "create:connect," + this.hashCode());
        TimeUtils.startTime("getNetData");
        reset();
        try {
            try {
                mSocketChannel = SocketChannel.open();
                LogUtil.logi(TAG + "create:SocketChannel," + mSocketChannel.hashCode() + ",validOps=" + mSocketChannel.validOps());
            } catch (IOException e) {
                setSuggestRetryDelay(DEFAULT_SUGGEST_RETRY_DELAY_TIME);
                this._onError("create error: " + e.getMessage());
                return;
            }
            try {
                mSocketChannel.configureBlocking(false);
            } catch (IOException e) {
                setSuggestRetryDelay(DEFAULT_SUGGEST_RETRY_DELAY_TIME);
                this._onError("config error: " + e.getMessage());
                return;
            }

            try {
                mSelector = Selector.open();
                LogUtil.logi(TAG + "create:selecor," + mSelector.hashCode());
            } catch (IOException e) {
                setSuggestRetryDelay(DEFAULT_SUGGEST_RETRY_DELAY_TIME);
                this._onError("create selector error: " + e.getMessage());
                return;
            }

            long start = SystemClock.elapsedRealtime();

            try {
                mSocketChannel.connect(new InetSocketAddress(server, port));
            } catch (Throwable e) {
                setSuggestRetryDelay(DEFAULT_SUGGEST_RETRY_DELAY_TIME);
                this._onError("connect error: [" + server + "/" + server + ":" + port + "]"
                        + e);
                return;
            }
            try {
                mSocketChannel.register(mSelector, SelectionKey.OP_CONNECT
                        | SelectionKey.OP_READ);
            } catch (ClosedChannelException e) {
                setSuggestRetryDelay(DEFAULT_SUGGEST_RETRY_DELAY_TIME);
                this._onError("register selector error: " + e.getMessage());
                return;
            }

            while (!mCanceled) {
                // 取消判断
                // 空闲处理
                onIdle();
                if (mClosed)
                    return;
                if (mConnected) {
                    if (SystemClock.elapsedRealtime() - start > mReadTimeout) {
                        setSuggestRetryDelay(DEFAULT_SUGGEST_RETRY_DELAY_TIME);
                        mIsTimeout = true;
                        onReadTimeout();
                        return;
                    }
                } else {
                    if (SystemClock.elapsedRealtime() - start > mConnectTimeout) {
                        setSuggestRetryDelay(DEFAULT_SUGGEST_RETRY_DELAY_TIME);
                        mIsTimeout = true;
                        onConnectTimeout();
                        return;
                    }
                }

                // 开始select
                int ret = 0;
                try {
                    ret = mSelector.select(DEFAULT_SELECT_TIMEOUT);
                } catch (IOException e) {
                    setSuggestRetryDelay(DEFAULT_SUGGEST_RETRY_DELAY_TIME);
                    this._onError("select error: " + e.getMessage());
                }

                // 处理select结果
                if (ret == 0) {
//                    if (BuildConfig.DEBUG) {
//                        LogUtil.logd("ret is zero " + ret + "/" + DEFAULT_SELECT_TIMEOUT);
//                    }
                    if (sleepTime < 128) {
                        sleepTime = sleepTime << 1;
                    }
                    SystemClock.sleep(sleepTime);
                    continue;
                }

                TimeUtils.endTime("getNetData");
                sleepTime = 1;
                Iterator<SelectionKey> keyIter = mSelector.selectedKeys().iterator();

                //FIXME 减少大小，涨CPU，
                ByteBuffer mByteBufferRead = ByteBuffer.allocate(1024 * 32);

                while (keyIter.hasNext()) {
                    SelectionKey key = keyIter.next();
                    keyIter.remove();
                    if (key.isReadable()) {
                        if (!mConnected) {
                            setSuggestRetryDelay(DEFAULT_SUGGEST_RETRY_DELAY_TIME);
                            this._onError("connect status error");
                            return;
                        }

                        try {
                            while (true) {
                                onIdle();
                                start = SystemClock.elapsedRealtime();
                                TimeUtils.startTime("mByteBufferRead.clear");
                                mByteBufferRead.clear();
                                TimeUtils.endTime("mByteBufferRead.clear");
                                int r = mSocketChannel.read(mByteBufferRead);
                                if (r < 0) {
                                    cancel();
                                    break;
                                } else {
                                    onRead(mByteBufferRead.array(), 0, r);
                                }
                            }
                        } catch (Exception e) {
                            if (!mCanceled) {
                                setSuggestRetryDelay(DEFAULT_SUGGEST_RETRY_DELAY_TIME);
                                mClosed = true;
                                onDisconnect();
                            }
                            return;
                        }
                        continue;
                    }
                    if (key.isConnectable()) {
                        try {
                            mConnected = mSocketChannel.finishConnect();
                        } catch (IOException e) {
                            this._onError("connect error: " + e.getMessage());
                            return;
                        }
                        start = SystemClock.elapsedRealtime();
                        onConnect();
                        if (mClosed)
                            return;
                    }
                }
            }
        } catch (RuntimeException e) {
        } finally {
            cancel();
        }
    }

    private void release() {
        mClosed = true;
        LogUtil.logd(TAG + "release:close," + this.hashCode());
        if (mSocketChannel != null) {
            try {
                LogUtil.logd(TAG + "SocketChannel:close," + mSocketChannel.hashCode());
                mSocketChannel.close();
                mSocketChannel = null;
            } catch (Exception e) {
                LogUtil.logd(TAG + "SocketChannel:close:error");
            }
        }
        if (mSelector != null) {
            try {
                LogUtil.logd(TAG + "Selector:close," + mSelector.hashCode());
                mSelector.close();
                mSelector = null;
            } catch (Exception e) {
                LogUtil.logd(TAG + "Selector:close:error");
            }
        }
    }

    public boolean isError() {
        return mIsError;
    }

    public boolean isTimeout() {
        return mIsTimeout;
    }

    public boolean isClosed() {
        return mClosed;
    }

    public boolean isConnected() {
        return mConnected;
    }

    public boolean isCanceled() {
        return mCanceled;
    }

    public void cancel() {
        mCanceled = true;
        release();
    }

    public void write(byte[] data, int offset, int len) throws IOException {
        if (mSocketChannel != null) {
            try {
                mSocketChannel.write(ByteBuffer.wrap(data, offset, len));
            } catch (Exception e) {
                _onError("write data[" + len + "] error: " + e.getMessage());
                release();
            }
        }
    }

    public void write(byte[] data) throws IOException {
        write(data, 0, data.length);
    }

    public void write(String data) throws IOException {
        write(data.getBytes());
    }
}

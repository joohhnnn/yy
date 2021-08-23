package com.txznet.audio.server.response;

import android.os.SystemClock;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.util.TimeUtils;

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
    long mTimeStamp;
    int mConnectTimeout = 5000;
    int mReadTimeout = 5000;
    int mSuggestRetryDelayDefault = 1000;
    private int mSelectTimeout = 1000;
    protected long mContentLength = 0;
    protected static long testRPGS = 0;//测试流量的消耗
    protected int mHeadLen = 0;
    protected int mHeadPos = 0;

    private int sleepTime = 1;//Selector.select(sleepTime);//因为系统的该方法不能阻塞，导致这里必须使用睡眠来使用
    /**
     * 其实read返回0有3种情况，
     * 一是某一时刻socketChannel中当前（注意是当前）没有数据可以读，这时会返回0，
     * 其次是bytebuffer的position等于limit了，即bytebuffer的remaining等于0，这个时候也会返回0，
     * 最后一种情况就是客户端的数据发送完毕了，这个时候客户端想获取服务端的反馈调用了recv函数，若服务端继续read，这个时候就会返回0。
     */

    boolean mConnected = false;
    boolean mClosed = false;
    boolean mCanceled = false;
    boolean mIsError = false;
    boolean mIsTimeout = false;

    SocketChannel mSocketChannel = null;
    Selector mSelector = null;
    ByteBuffer mByteBufferRead = ByteBuffer.allocate(1024 * 200);

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

    public void onConnectTimout() throws IOException {

    }

    public void onReadTimout() throws IOException {

    }

    private void _onError(String errDesc) throws IOException {
        mIsError = true;
//		LogUtil.loge(TAG+"BlockTcpClient[" + this.hashCode() + "] " + errDesc);
        onError(errDesc);
    }

    public void setConnectTimeout(int t) {
        mConnectTimeout = t;
    }

    public void setReadTimeout(int t) {
        mReadTimeout = t;
    }

//	public void setSelectTimeout(int t) {
//		mSelectTimeout = t;
//	}

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
                setSuggestRetryDelay(mSuggestRetryDelayDefault);
                this._onError("create error: " + e.getMessage());
                return;
            }
            try {
                mSocketChannel.configureBlocking(false);
            } catch (IOException e) {
                setSuggestRetryDelay(mSuggestRetryDelayDefault);
                this._onError("config error: " + e.getMessage());
                return;
            }

            try {
                mSelector = Selector.open();
                LogUtil.logi(TAG + "create:selecor," + mSelector.hashCode());
            } catch (IOException e) {
                setSuggestRetryDelay(mSuggestRetryDelayDefault);
                this._onError("create selector error: " + e.getMessage());
                return;
            }

            mTimeStamp = SystemClock.elapsedRealtime();

            try {
                mSocketChannel.connect(new InetSocketAddress(server, port));
            } catch (Exception e) {
                setSuggestRetryDelay(mSuggestRetryDelayDefault);
//                this._onError("connect Exception: [" + server + ":" + port
//                        + "]" + e);
                return;
            } catch (Error e) {
                setSuggestRetryDelay(mSuggestRetryDelayDefault);
//                this._onError("connect error: [" + server + ":" + port + "]"
//                        + e);
                return;
            }
            try {
                mSocketChannel.register(mSelector, SelectionKey.OP_CONNECT
                        | SelectionKey.OP_READ);
            } catch (ClosedChannelException e) {
                setSuggestRetryDelay(mSuggestRetryDelayDefault);
                this._onError("register selector error: " + e.getMessage());
                return;
            }

            while (true) {
                // 取消判断
                if (mCanceled) {
                    break;
                }

                // 空闲处理
                onIdle();

                if (mClosed)
                    return;

                if (mConnected) {
                    if (SystemClock.elapsedRealtime() - mTimeStamp > mReadTimeout) {
//						LogUtil.logw("BlockTcpClient[" + this.hashCode()
//								+ "] onReadTimout: " + mReadTimeout);

                        setSuggestRetryDelay(mSuggestRetryDelayDefault);

                        mIsTimeout = true;
                        onReadTimout();

                        return;
                    }
                } else {
                    if (SystemClock.elapsedRealtime() - mTimeStamp > mConnectTimeout) {
//						LogUtil.logw("BlockTcpClient[" + this.hashCode()
//								+ "] onConnectTimout: " + mConnectTimeout);

                        setSuggestRetryDelay(mSuggestRetryDelayDefault);

                        mIsTimeout = true;
                        onConnectTimout();

                        return;
                    }
                }

                // 开始select
                int ret = 0;
                try {
                    ret = mSelector.select(mSelectTimeout);
                } catch (IOException e) {
                    setSuggestRetryDelay(mSuggestRetryDelayDefault);
                    this._onError("select error: " + e.getMessage());
                }

                // 处理select结果
                if (ret == 0) {
//					if (Constant.ISTESTDATA) {
//						LogUtil.logd("ret is zero" + ret+"/"+mSelectTimeout);
//					}

                    if (sleepTime < 128) {
                        sleepTime = sleepTime << 1;
                    }
                    SystemClock.sleep(sleepTime);
                    continue;
                }
                TimeUtils.endTime("getNetData");
                sleepTime = 1;
                Iterator<SelectionKey> keyIter = mSelector.selectedKeys()
                        .iterator();
                while (keyIter.hasNext()) {
                    SelectionKey key = keyIter.next();
                    keyIter.remove();
                    if (key.isReadable()) {
                        if (!mConnected) {
                            setSuggestRetryDelay(mSuggestRetryDelayDefault);
                            this._onError("connect status error");
                            return;
                        }

                        try {
                            int countR = 0;
                            int needlog = 0;
                            while (true) {
                                onIdle();
                                mTimeStamp = SystemClock.elapsedRealtime();
                                mByteBufferRead.clear();
                                int r = mSocketChannel.read(mByteBufferRead);
//                                if (Constant.ISTEST) {
//                                    LogUtil.logi(Constant.SPEND_TAG + " get  QQ data size:" + r);
//                                }
                                if (r < 0) {
                                    cancel();
                                    break;
                                } else if (r >= 0) {
                                    needlog = 0;
                                    countR += r;
                                    onRead(mByteBufferRead.array(), 0, r);
                                } else {
//                                    if (countR >= (mHeadPos + mContentLength + 2)) {//2个字节为换行符/r/n
                                        break;
//                                    }
                                }
//                                if (++needlog < 10) {
//                                    Thread.sleep(100);
//                                }else{
//                                    LogUtil.logi(Constant.SPEND_TAG + "writeData(" + mHeadLen + "/" + mHeadPos + ")" + ",(" + countR + "/" + mContentLength + ")");
//                                    this._onError("too empty data be return ");
//                                    MonitorUtil.monitorCumulant(Constant.M_SOCKET_READ_EMPTY);
//                                }
                            }
                        } catch (Exception e) {
                            if (mCanceled == false) {
                                setSuggestRetryDelay(mSuggestRetryDelayDefault);
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

                        mTimeStamp = SystemClock.elapsedRealtime();

                        onConnect();

                        if (mClosed)
                            return;

                        continue;
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

package com.txznet.proxy.server.response;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.proxy.BuildConfig;
import com.txznet.proxy.ProxySession;
import com.txznet.proxy.server.NanoHTTPD;
import com.txznet.proxy.util.GcTrigger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static com.txznet.proxy.server.NanoHTTPD.MIME_AUDIO;

/**
 * 请求处理响应基类
 */
public abstract class MediaResponseBase extends NanoHTTPD.Response {
    protected static final String TAG = "music:http:response:";

    protected final static int DEFAULT_BUFFER_SIZE = 1024 * 8; // 默认读写缓冲区大小
    protected final static long MAX_CACHE_SIZE = 1024 * 256; // 下载的每一片大小
    protected final static byte[] EMPTY_DATA = new byte[0];
    protected final static int WAIT_DATA_TIME = 1000;

    protected Socket mSocket;
    protected ProxySession mSess;
    protected long mFrom;
    protected long mTo;
    protected long mLen; // 资源文件总长
    protected long mWrite;
    protected String mMethod;
    private int mPrintTotal = 0;

    protected MediaResponseBase(Socket socket, ProxySession sess, String method, long from, long to) {
        mSocket = socket;
        mSess = sess;
        mFrom = from;
        mTo = to;
        mMethod = method;
        mSess.addResponse(this);
        LogUtil.logi("media session[" + mSess + "] create response: Range=" + mFrom + "~" + mTo);
        init();
    }

    private void init() {
        try {
            printEmptyData();
            // 保持通道存活
            mSess.len = mLen = getContentLength();
            LogUtil.logi("media session[" + mSess + "] create response: Range=" + mFrom + "~" + mTo + ", len=" + mSess.len);
            if (mLen < 0) {
                return;
            }
            printEmptyData();
            prepareResponseHeader();
            // head请求不输出内容
            if (isHeadRequest()) {
                return;
            }
            data = getData();
        } catch (IOException e) {
            cancel();
        }
    }

    // 获取资源总长
    protected abstract long getContentLength() throws IOException;

    // 获取资源数据
    protected abstract NanoHTTPD.InputStreamWrapper getData() throws IOException;

    // 设置响应头
    protected void prepareResponseHeader() {
        addHeader("Accept-Ranges", "bytes");
        addHeader("Connection", "close");
        addHeader("Content-Type", MIME_AUDIO);
        mimeType = MIME_AUDIO;
        // 重新定义碎片范围
        if (mTo == -1) {
            mTo = mLen - 1;
        }
        status = (mFrom == -1 ? NanoHTTPD.HTTP_OK : NanoHTTPD.HTTP_PARTIALCONTENT);
        // 416 HTTP_RANGE_NOT_SATISFIABLE
        if (mFrom > mTo || mTo > mLen - 1 || mFrom > mLen - 1) {
            status = NanoHTTPD.HTTP_RANGE_NOT_SATISFIABLE;
            addHeader("Content-Range", "bytes */" + mLen);
            return;
        }
        // 计算Content-Length长度
        long ContentLength = mLen;
        if (mFrom >= 0) {
            // Content-Range: bytes 0-12184829/12184830
            ContentLength = mTo - mFrom + 1;
            addHeader("Content-Range", "bytes " + mFrom + "-" + mTo + "/" + mLen);
        }
        addHeader("Content-Length", "" + (ContentLength <= 0 ? 0 : ContentLength));

        if (BuildConfig.DEBUG) {
            LogUtil.logd("media session[" + mSess + "] prepareResponseHeader: " + mMethod + ",from/to=" + mFrom
                    + "/" + mFrom + "\r\n" + header);
        }
    }

    protected void printData(final byte[] data, final int offset, final int len) throws IOException {
        try {
            if (mSocket != null) {
                OutputStream out = mSocket.getOutputStream();
                mPrintTotal += len;
                if (BuildConfig.DEBUG) {
                    if (len > 0) {
                        if (mPrintTotal % (1024 * 512) == 0) {
                            LogUtil.logd("media session[" + mSess + "] write data length: " + mPrintTotal);
                        }
                    }
                }
                try {
                    out.write(EMPTY_DATA);
                    out.write(data, offset, len);
                    mWrite += len;
                    out.write(EMPTY_DATA);
                } catch (Exception e) {
                    e.printStackTrace();
                    cancel();
                }
            }
        } catch (Exception e) {
            LogUtil.logw("media session[" + mSess + "] write data error");
            throw e;
        }
    }

    private float lastP;
    private int saveCount;

    protected boolean waitNeedData() throws IOException {
        printEmptyData();
        while (true) {
            printEmptyData();
            // TODO: 2018/11/7  限流的算法？
            try {
                if (mSocket == null || mSocket.isClosed()) {
                    throw new IOException("socket is closed");
                }
                if (mWrite == 0) {
                    break;
                }
                if (mSess.param.needMoreWriteData) {
                    break;
                }
                long wTotal = mFrom + mWrite;
                float wPercent = wTotal * 1f / mLen;
                float pPercent = mSess.param.callback.getPlayPercent();

                if (pPercent != 0 && lastP == pPercent) {
                    saveCount++;
                } else {
                    lastP = pPercent;
                    saveCount = 0;
                }
                if (saveCount > 2 && mSess.param.callback.isPlaying()) {
                    saveCount = 0;
                    if (BuildConfig.DEBUG) {
                        LogUtil.loge("media session[" + mSess + "] need more data force (" + pPercent + "/" + wPercent + ", " + wTotal + "/" + mLen + ")");
                    }
                    break;
                }

                // 写入量小于播放量, 如seekTo操作，放行
                if (wPercent < pPercent) {
                    break;
                }
                long duration = mSess.param.callback.getDuration();
                if (duration == 0) {
                    break;
                }
                // 写入量小于播放量 + 20%
                if (wPercent - pPercent < 0.20) {
                    // 音频总长大于10分钟
                    if (duration >= 60 * 1000 * 10 && pPercent > 0) {
                        long bSize = (long) (mLen * 1f / duration); // 一秒的数据量
                        // 最大写入2分钟的数据量
                        long pSize = (long) ((pPercent * duration + 60 * 1000 * 2) * bSize);
                        if (wTotal < pSize) {
                            if (BuildConfig.DEBUG) {
                                LogUtil.logd("media session[" + mSess + "] need more data pass (" + pPercent + "/" + wPercent + ", " + wTotal + "/" + mLen + ")");
                            }
                            break;
                        }
                    } else if (mLen > 1024 * 1024 * 10) { // 文件大小大于10mb，但播放时长小于10分钟(高质量音频)
                        long bSize = (long) (mLen * 1f / duration); // 一秒的数据量
                        // 最大写入1分钟的数据量
                        long pSize = (long) ((pPercent * duration + 60 * 1000) * bSize);
                        if (wTotal < pSize) {
                            if (BuildConfig.DEBUG) {
                                LogUtil.logd("media session[" + mSess + "] need more data pass (" + pPercent + "/" + wPercent + ", " + wTotal + "/" + mLen + ")");
                            }
                            break;
                        }
                    } else {
                        break;
                    }
                }
                try {
                    if (BuildConfig.DEBUG) {
                        LogUtil.logd("media session[" + mSess + "] need more data wait (" + pPercent + "/" + wPercent + ", " + wTotal + "/" + mLen + ")");
                    }
                    printEmptyData();
                    Thread.sleep(WAIT_DATA_TIME);
                } catch (InterruptedException e) {
                }
            } catch (Exception e) {
                return false;
            }
        }
        printEmptyData();
        return true;
    }

    public void cancel() {
        LogUtil.logw("media session[" + mSess + "] cancel response: Range=" + mFrom + "~" + mTo);
        LogUtil.logd(BlockTcpClient.TAG + "http:release");
        if (mSocket != null) {
            try {
                LogUtil.logd(BlockTcpClient.TAG + "socket:close," + mSocket.hashCode() + ", " + mFrom + "/" + mTo);
                mSocket.getInputStream().close();
                mSocket.getOutputStream().close();
                mSocket.close();
            } catch (Exception e) {
                LogUtil.logd(BlockTcpClient.TAG + "socket:error");
            }
            mSocket = null;
        }
        GcTrigger.runGc();
    }

    private boolean isHeadRequest() {
        return "head".equalsIgnoreCase(mMethod);
    }

    protected void printData(byte[] data) throws IOException {
        printData(data, 0, data.length);
    }

    protected void printEmptyData() throws IOException {
        printData(EMPTY_DATA);
    }
}

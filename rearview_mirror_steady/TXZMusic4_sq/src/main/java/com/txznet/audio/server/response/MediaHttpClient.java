package com.txznet.audio.server.response;

import android.os.SystemClock;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.util.TimeUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public abstract class MediaHttpClient<T> extends BlockTcpClient {
    public final static String TAG_HTTP = "music:http:";
    private final static String HEAD = "HEAD";
    private final static String GET = "GET";

    URI mUri;
    long mFrom;
    long mTo;
    String mMethod;
    int countEmpty;//在一次HTTP请求中,Socket读取到0的次数

    public MediaHttpClient(T d) {
        mData = d;
    }

    private T mData;

    public T getData() {
        return mData;
    }

    public void getMedia(URI uri, long from, long to) throws IOException {
        LogUtil.logd("test:play:error:requestUrl:[" + from + "/" + to + "]" + uri);
        clear();

        mMethod = GET;
        this.mUri = uri;
        this.mFrom = from;
        this.mTo = to;
        int port = uri.getPort();
        if (port == -1) {
            port = 80;
        }
        super.connect(uri.getHost(), port);
    }

    public void headMedia(URI uri) throws IOException {
        clear();

        mMethod = HEAD;
        this.mUri = uri;

        int port = uri.getPort();
        if (port == -1) {
            port = 80;
        }

        super.connect(uri.getHost(), port);
    }

    @Override
    public void onConnect() throws IOException {
        String requestHeaderString = mMethod
                + " "
                + mUri.getRawPath()
                + (mUri.getRawQuery() == null ? "" : ("?" + mUri.getRawQuery()))
                + " HTTP/1.1\r\n"
                + "Host: "
                + mUri.getHost()
                + "\r\n"
                + (mFrom >= 0 ? ("Range: bytes=" + mFrom + "-"
                + (mTo >= 0 ? ("" + mTo) : "") + "\r\n") : "")
                + "Connection: Keep-Alive\r\n\r\n";
        if (Constant.ISTESTDATA) {
            LogUtil.logd(TAG_HTTP + "local http request header :\r\n" + requestHeaderString);
        }
        this.write(requestHeaderString);
    }

    private static final byte[] LINE_SPLITER = new byte[]{'\r', '\n'};
    private static final String TAG = "[MUSIC][HTTP]";

    private int _searchBytes(byte[] data, byte[] search, int offset, int len) {
        for (int i = offset; i <= len - search.length; ++i) {
            int j = 0;
            for (; j < search.length; ++j) {
                if (search[j] != data[i + j])
                    break;
            }
            if (j >= search.length) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onRead(byte[] data, int offset, int len) throws IOException {
        if (mHasHeader) {
            _onReadData(data, offset, len);
            return;
        }
        int l = mHeadBuffer.length - mHeadLen;
        if (l > len) {
            l = len;
        }
        System.arraycopy(data, offset, mHeadBuffer, mHeadLen, l);
        offset += l;
        len -= l;
        mHeadLen += l;
        if (Constant.ISTESTDATA) {
            LogUtil.logd(TAG_HTTP + "real http response data:\r\n" + new String(mHeadBuffer, 0, mHeadBuffer.length > 500 ? 500 : mHeadBuffer.length));
        }
        if (!mHasStatusLine) {
            int f = _searchBytes(mHeadBuffer, LINE_SPLITER, mSearchStatusPos,
                    mHeadLen);
            if (f > 0) {
                mSearchStatusPos = f + 2;
                mHeadPos = mSearchStatusPos;
                String statusLine = new String(mHeadBuffer, 0, f);
                String[] status = statusLine.split(" ");
                if (status.length < 3 || !(status[0].startsWith("HTTP/"))) {
                    onMediaError(new Error(Error.ERROR_CLIENT_MEDIA_BAD_DATA,
                            "bad status line: " + statusLine, "音频数据访问异常"));
                    cancel();
                    return;
                }
                try {
                    mStatusCode = Integer.parseInt(status[1]);
                } catch (Exception e) {
                    onMediaError(new Error(Error.ERROR_CLIENT_MEDIA_BAD_DATA,
                            "bad status line: " + statusLine, "音频数据访问异常"));
                    cancel();
                    return;
                }
                mStatusLine = status[2];
                for (int i = 3; i < status.length; ++i) {
                    mStatusLine += " " + status[i];
                }
                mHasStatusLine = true;
                onResponse(mStatusCode, mStatusLine);
            } else {
                mSearchStatusPos = mHeadLen - LINE_SPLITER.length + 1;
            }
        }
        if (mCanceled) {
            return;
        }
        if (mHasStatusLine && !mHasHeader) {
            while (true) {
                int f = _searchBytes(mHeadBuffer, LINE_SPLITER,
                        mSearchStatusPos, mHeadLen);

                // 没有下一个分隔符，等待下一个包
                if (f < 0) {
                    mSearchStatusPos = mHeadLen - LINE_SPLITER.length + 1;
                    break;
                }

                // 头结束
                if (f == mHeadPos) {
                    mHasHeader = true;

                    onGetInfo(mMapHeaders, mMineType, mContentLength);

                    if (!mMethod.equals("GET")) {
                        onComplete();
                        cancel();
                        return;
                    }

                    _onReadData(mHeadBuffer, mHeadPos + LINE_SPLITER.length,
                            mHeadLen - mHeadPos - LINE_SPLITER.length);

                    _onReadData(data, offset, len);

                    break;
                }
                String head = new String(mHeadBuffer, mHeadPos, f - mHeadPos);
                int p = head.indexOf(':');
                if (p < 0) {
                    onMediaError(new Error(Error.ERROR_CLIENT_MEDIA_BAD_DATA,
                            "bad head line: " + head, "音频数据访问异常"));
                    cancel();
                    return;
                }
                String key = head.substring(0, p).trim();
                String val = head.substring(p + 1).trim();

                if (key.compareToIgnoreCase("Content-Length") == 0) {
                    try {
                        mContentLength = Long.parseLong(val);
                        testRPGS += mContentLength;
                    } catch (Exception e) {
                        onMediaError(new Error(Error.ERROR_CLIENT_MEDIA_BAD_DATA,
                                "bad content length: " + val, "音频数据访问异常"));
                        cancel();
                        return;
                    }
                    if (Constant.ISTEST) {
                        LogUtil.logd("供消耗流量:" + testRPGS);
                    }
                } else if (key.compareToIgnoreCase("Content-Type") == 0) {
                    mMineType = val;
                } else if (key.compareToIgnoreCase("Location") == 0
                        && mStatusCode == 302) {
                    // 重定向到真实可下载的地址
                    try {
                        this.mUri = new URI(val);
                        if (GET.equals(mMethod)) {
                            getMedia(this.mUri, this.mFrom, this.mTo);
                        } else {
                            headMedia(this.mUri);
                        }
                        return;
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        onMediaError(new Error(Error.ERROR_CLIENT_MEDIA_BAD_DATA,
                                "bad content length: " + val, "音频数据访问异常"));
                        cancel();
                        return;
                    }
                }

                mMapHeaders.put(key, val);

                mSearchStatusPos = mHeadPos = f + LINE_SPLITER.length;
            }
        }

        if (mHeadLen >= mHeadBuffer.length && !mHasHeader) {
            onMediaError(new Error(Error.ERROR_CLIENT_MEDIA_BAD_DATA,
                    "over max header length", "音频数据访问异常"));
            cancel();
        }
    }

    int mStatusCode = 0;
    String mStatusLine = null;
    Map<String, String> mMapHeaders = new HashMap<String, String>();
    boolean mHasStatusLine = false;
    boolean mHasHeader = false;

    long mReadCount = 0;
    String mMineType = null;

    byte[] mHeadBuffer = new byte[8192]; // 相应头buffer

    int mSearchStatusPos = 0;

    private void clear() {
        mFrom = mTo = -1;

        mStatusCode = 0;
        mStatusLine = null;
        mMapHeaders.clear();
        mHasStatusLine = false;
        mHasHeader = false;
        mContentLength = 0;
        mReadCount = 0;
        mMineType = null;

        mHeadLen = 0;
        mSearchStatusPos = 0;
        mHeadPos = 0;
    }

    @Override
    public void onError(String errDesc) throws IOException {
        onMediaError(new Error(Error.ERROR_CLIENT_MEDIA_ERR_IO, errDesc, "播放发生错误"));
    }

    public abstract void onMediaError(Error err) throws IOException;

    public abstract void onResponse(int statusCode, String statusLine)
            throws IOException;

    public abstract void onGetInfo(Map<String, String> headers,
                                   String mimeType, long contentLength) throws IOException;

    public long getReadCount() {
        return mReadCount;
    }

    private void _onReadData(byte[] data, int offset, int len)
            throws IOException {
        if (len <= 0) {
            countEmpty++;
            if (countEmpty < 10) {
                SystemClock.sleep(500);
            } else {
                MonitorUtil.monitorCumulant(Constant.M_SOCKET_READ_EMPTY);
                onReadEmpty(getReadCount());
            }
            return;
        }
        countEmpty = 0;
//		if (Constant.ISTESTDATA) {
//			LogUtil.logd("mReadCount=" + mReadCount + "/" + mContentLength);
//		}
        if (mReadCount + len > mContentLength) {
            onReadData(data, offset, (int) (mContentLength - mReadCount));
            mReadCount = mContentLength;
        } else {
            onReadData(data, offset, len);
            mReadCount += len;
        }
//		if (Constant.ISTESTDATA) {
//			LogUtil.logd("mReadCount=" + mReadCount + "/" + mContentLength);
//		}

        if (mReadCount >= mContentLength) {
            onComplete();
            cancel();
        }
    }

    public abstract void onReadData(byte[] data, int offset, int len)
            throws IOException;

    public void onComplete() throws IOException {
    }

    //读取到0的时候处于的end的位置
    public void onReadEmpty(long endPos) {

    }
}

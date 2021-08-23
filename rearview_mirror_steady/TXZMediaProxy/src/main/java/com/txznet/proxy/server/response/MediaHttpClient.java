package com.txznet.proxy.server.response;

import android.os.SystemClock;
import android.support.v4.util.ArrayMap;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.proxy.BuildConfig;
import com.txznet.proxy.Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.txznet.proxy.ErrCode.ERROR_CLIENT_MEDIA_BAD_DATA;
import static com.txznet.proxy.ErrCode.ERROR_CLIENT_MEDIA_ERR_IO;

public abstract class MediaHttpClient extends BlockTcpClient {
    private final static String TAG_HTTP = "music:http:";
    private final static String HEAD = "HEAD";
    private final static String GET = "GET";

    private static long testRPGS;//测试流量的消耗
    private long mContentLength;
    private int mHeadLen;
    private int mHeadPos;

    private URI mUri;
    private long mFrom;
    private long mTo;
    private String mMethod;
    private int countEmpty;//在一次HTTP请求中,Socket读取到0的次数
    private int countRedirect; // 重定向次数
    private ArrayMap<String, URI> redirectMapper;


    public URI getMedia(URI uri, long from, long to) throws IOException {
        clear();
        mMethod = GET;
        if (redirectMapper != null && redirectMapper.containsKey(uri.toString())) {
            this.mUri = redirectMapper.get(uri.toString());
            LogUtil.logd("test:play:error:requestUrl:[" + from + "/" + to + "]" + uri);
        } else {
            this.mUri = uri;
            LogUtil.logd("test:play:error:requestUrl:[" + from + "/" + to + "]" + uri);
        }
        this.mFrom = from;
        this.mTo = to;
        int port = uri.getPort();
        if (port == -1) {
            port = 80;
        }
        super.connect(uri.getHost(), port);
        return this.mUri;
    }

    public URI getUri() {
        return mUri;
    }

    public URI headMedia(URI uri) throws IOException {
        clear();
        mMethod = HEAD;
        if (redirectMapper != null && redirectMapper.containsKey(uri.toString())) {
            this.mUri = redirectMapper.get(uri.toString());
        } else {
            this.mUri = uri;
        }
        int port = uri.getPort();
        if (port == -1) {
            port = 80;
        }
        super.connect(uri.getHost(), port);
        return this.mUri;
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

        byte[] headBuffer = new byte[8192];
        LogUtil.logd("new byte " + 8192);

        int l = headBuffer.length - mHeadLen;
        if (l > len) {
            l = len;
        }
        System.arraycopy(data, offset, headBuffer, mHeadLen, l);
        offset += l;
        len -= l;
        mHeadLen += l;
        if (BuildConfig.DEBUG) {
            LogUtil.logd(TAG_HTTP + "real http response data:\r\n" + new String(headBuffer, 0, 500));
        }
        if (!mHasStatusLine) {
            int f = _searchBytes(headBuffer, LINE_SPLITER, mSearchStatusPos, mHeadLen);
            if (f > 0) {
                mSearchStatusPos = f + 2;
                mHeadPos = mSearchStatusPos;
                String statusLine = new String(headBuffer, 0, f);
                String[] status = statusLine.split(" ");
                if (status.length < 2 || !(status[0].startsWith("HTTP/"))) {
                    onMediaError(ERROR_CLIENT_MEDIA_BAD_DATA, "bad status line: " + statusLine, "音频数据访问异常");
                    cancel();
                    return;
                }
                try {
                    mStatusCode = Integer.parseInt(status[1]);
                } catch (Exception e) {
                    onMediaError(ERROR_CLIENT_MEDIA_BAD_DATA, "bad status line: " + statusLine, "音频数据访问异常");
                    cancel();
                    return;
                }
                if (status.length >= 3) {
                    mStatusLine = status[2];
                    for (int i = 3; i < status.length; ++i) {
                        mStatusLine += " " + status[i];
                    }
                } else {
                    mStatusLine = null;
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
                int f = _searchBytes(headBuffer, LINE_SPLITER, mSearchStatusPos, mHeadLen);
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
                    _onReadData(headBuffer, mHeadPos + LINE_SPLITER.length, mHeadLen - mHeadPos - LINE_SPLITER.length);
                    _onReadData(data, offset, len);
                    break;
                }
                String head = new String(headBuffer, mHeadPos, f - mHeadPos);
                int p = head.indexOf(':');
                if (p < 0) {
                    onMediaError(ERROR_CLIENT_MEDIA_BAD_DATA, "bad head line: " + head, "音频数据访问异常");
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
                        onMediaError(ERROR_CLIENT_MEDIA_BAD_DATA, "bad content length: " + val, "音频数据访问异常");
                        cancel();
                        return;
                    }
                    if (BuildConfig.DEBUG) {
                        LogUtil.logd("消耗流量:" + testRPGS);
                    }
                } else if (key.compareToIgnoreCase("Content-Type") == 0) {
                    mMineType = val;
                } else if (key.compareToIgnoreCase("Location") == 0
                        && mStatusCode == 302) {
                    // 重定向到真实可下载的地址
                    ++countRedirect;
                    if (countRedirect > 3) {
                        onMediaError(ERROR_CLIENT_MEDIA_BAD_DATA, "redirect too much time", "音频数据访问异常");
                        cancel();
                        return;
                    }
                    try {
                        URI redirectUri = new URI(val);
                        if (redirectMapper == null) {
                            redirectMapper = new ArrayMap<>();
                        }
                        redirectMapper.put(this.mUri.toString(), redirectUri);
                        this.mUri = redirectUri;
                        if (GET.equals(mMethod)) {
                            getMedia(this.mUri, this.mFrom, this.mTo);
                        } else {
                            headMedia(this.mUri);
                        }
                        return;
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        onMediaError(ERROR_CLIENT_MEDIA_BAD_DATA, "bad content length: " + val, "音频数据访问异常");
                        cancel();
                        return;
                    }
                }
                mMapHeaders.put(key, val);
                mSearchStatusPos = mHeadPos = f + LINE_SPLITER.length;
            }
        }
        if (mHeadLen >= headBuffer.length && !mHasHeader) {
            onMediaError(ERROR_CLIENT_MEDIA_BAD_DATA, "over max header length", "音频数据访问异常");
            cancel();
        }
    }

    private int mStatusCode;
    private String mStatusLine;
    private Map<String, String> mMapHeaders = new ArrayMap<>();
    private boolean mHasStatusLine;
    private boolean mHasHeader;
    private long mReadCount;
    private String mMineType;
    private int mSearchStatusPos;

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
        onMediaError(ERROR_CLIENT_MEDIA_ERR_IO, errDesc, "播放发生错误");
    }

    public abstract void onMediaError(int errorCode, String desc, String hint) throws IOException;

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
        if (mReadCount + len > mContentLength) {
            onReadData(data, offset, (int) (mContentLength - mReadCount));
            mReadCount = mContentLength;
        } else {
            onReadData(data, offset, len);
            mReadCount += len;
        }
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

package com.txznet.proxy.server.response;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.proxy.ProxySession;
import com.txznet.proxy.cache.LocalBuffer;
import com.txznet.proxy.cache.TmdFile;
import com.txznet.proxy.server.NanoHTTPD;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.txznet.proxy.ErrCode.ERROR_CLIENT_MEDIA_FILE_CHECK_FAIL;
import static com.txznet.proxy.ErrCode.ERROR_CLIENT_MEDIA_NOT_FOUND;

/**
 * 本地音频文件/tmd文件
 */
public class FileMediaResponse extends MediaResponseBase {

    protected FileMediaResponse(Socket socket, ProxySession sess, String method, long from, long to) {
        super(socket, sess, method, from, to);
    }

    @Override
    protected long getContentLength() throws IOException {
        String path = mSess.oriUrls[0];
        File f = new File(path);
        // TODO: 2018/11/7  轮询所有的路径，直至所有的路径都不可以
        if (!f.exists()) {
            LogUtil.loge("media session[" + mSess + "] file not exist: " + path);
            mSess.param.callback.onError(ERROR_CLIENT_MEDIA_NOT_FOUND, "file not exist", "文件不存在");
            return -1;
        }
        if (path.toLowerCase(Locale.CHINESE).endsWith(".tmd")) {
            long ret = TmdFile.getDataSize(f);
            if (ret < 0) {
                LogUtil.loge(TAG + "file check error:" + path);
                mSess.param.callback.onError(ERROR_CLIENT_MEDIA_FILE_CHECK_FAIL, "file check error", "文件校验错误");
            }
            return ret;
        } else {
            return f.length();
        }
    }

    private void getFileData() throws IOException {
        String path = mSess.oriUrls[0];
        File f = new File(path);
        InputStream in = null;
        try {
            if (path.toLowerCase(Locale.CHINESE).endsWith(".tmd")) {
                in = TmdFile.openInputStream(f, -1);
            } else {
                in = new FileInputStream(f);
            }
            if (mFrom > 0) {
                in.skip(mFrom);
            }
            long total = mFrom;
            // 计算需要的数据量
            long need = MAX_CACHE_SIZE;
            // 限制单位时间内的写入数据量，避免内存暴涨
            byte[] READ_BUFFER = new byte[DEFAULT_BUFFER_SIZE];
            // 开始读文件
            while (true) {
                if (total + need > mTo + 1) {
                    need = mTo - total + 1;
                }
                LogUtil.logd("media session[" + mSess + "]need data size=" + need + ", now=" + total + ", to=" + mTo);

                int t = 0;
                while (t < need) {
                    int r = (int) need - t;
                    if (r > READ_BUFFER.length) {
                        r = READ_BUFFER.length;
                    }
                    r = in.read(READ_BUFFER, 0, r);
                    if (r < 0) {
                        break;
                    }
                    printData(READ_BUFFER, 0, r);
                    t += r;
                }
                LogUtil.logd("media session[" + mSess.getLogId() + "]write file data size=" + t);
                total += t;

                if (total >= mTo) {
                    LogUtil.logi("media session[" + mSess.getLogId() + "]write file data complete");
                    break;
                }
                // 等待需要更多的数据
                if (!waitNeedData()) {
                    return;
                }
            }
        } catch (RuntimeException e) {
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }
    }


    @Override
    protected NanoHTTPD.InputStreamWrapper getData() throws IOException {
        List<LocalBuffer> lst = Collections.singletonList(LocalBuffer.buildFull(mLen));
        mSess.param.callback.onBufferingUpdate(lst);
        printEmptyData();
        return new NanoHTTPD.InputStreamWrapper() {
            @Override
            public void printData(OutputStream os) throws IOException {
                getFileData();
            }
        };
    }
}

package com.txznet.audio.server.response;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.audio.bean.SessionInfo;
import com.txznet.audio.player.SysAudioPlayer;
import com.txznet.audio.player.audio.FileAudio;
import com.txznet.audio.player.audio.TmdFile;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.baseModule.bean.Error;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileMediaResponse extends MediaResponseBase {
    FileAudio audio;

    protected FileMediaResponse(Socket out, SessionInfo sess, long from, long to) {
        super(out, sess, from, to);

        audio = (FileAudio) mSess.audio;
    }

    @Override
    protected long getLength(OutputStream out) throws IOException {
        String path = audio.getPath();
        File f = new File(path);
        if (!f.exists()) {
            LogUtil.loge("media session[" + mSess.getLogId() + "] file not exist: " + path);
            mSess.player.notifyError(new Error(Error.ERROR_CLIENT_MEDIA_NOT_FOUND
                    , "file not exist", "文件不存在"));
            return -1;
        }
        if (path.toLowerCase(Locale.CHINESE).endsWith(".tmd")) {
            long ret = TmdFile.getDataSize(f);
            if (ret < 0) {
                LogUtil.loge(TAG + "file check error:" + path);
                mSess.player.notifyError(new Error(Error.ERROR_CLIENT_MEDIA_FILE_CHECK_FAIL
                        , "file check error", "文件校验错误"));
            }
            return ret;
        } else {
            return f.length();
        }
    }

    protected void getFileData(OutputStream out) throws IOException {
        String path = audio.getPath();
        File f = new File(path);
        InputStream in = null;
        long total = mFrom;

        try {
            if (path.toLowerCase(Locale.CHINESE).endsWith(".tmd")) {
                in = TmdFile.openInputStream(f, -1);
            } else {
                in = new FileInputStream(f);
            }
            if (mFrom > 0) {
                in.skip(mFrom);
            }

            // 开始读文件
            while (true) {
                // 计算需要的数据量
                long need;

//                if (mSess.player instanceof SysAudioPlayer && mSess.audio instanceof FileAudio) {
                    // 系统播放器播放本地音频就不做限流了，一次性返回全部长度
                    need = mLen;
//                } else {
//                    need = mSess.player.getDataPieceSize();
//                }

                LogUtil.logd("media session[" + mSess.getLogId() + "]need data size=" + need + ", now=" + total + ", to=" + mTo);

                if (total + need > mTo + 1) {
                    need = mTo - total + 1;
                }

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
                    printData(out, READ_BUFFER, 0, r);
                    t += r;
                }

                LogUtil.logd("media session[" + mSess.getLogId() + "]write file data size=" + t);

                total += t;

                if (total >= mTo) {
                    LogUtil.logi("media session[" + mSess.getLogId() + "]write file data complete");
                    break;
                }

                // 等待需要更多的数据
                if (!waitNeedData(out)) {
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
    protected void getData(OutputStream out) throws IOException {
        List<LocalBuffer> lst = new ArrayList<LocalBuffer>();
        lst.add(LocalBuffer.buildFull(mLen));
        mSess.player.notifyDownloading(lst);

        printEmptyData(out);

        getFileData(out);
    }
}

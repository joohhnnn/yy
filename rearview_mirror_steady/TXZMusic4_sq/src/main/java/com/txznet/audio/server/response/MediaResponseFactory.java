package com.txznet.audio.server.response;

import com.txznet.audio.bean.SessionInfo;
import com.txznet.audio.player.audio.FileAudio;
import com.txznet.audio.player.audio.NetAudio;
import com.txznet.audio.player.audio.PlayerAudio;
import com.txznet.audio.player.audio.QQMusicAudio;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.baseModule.Constant;

import java.net.Socket;

public class MediaResponseFactory {
    public static MediaResponseBase createResponse(Socket out,
                                                   SessionInfo sess, long from, long to) {
        PlayerAudio audio = sess.audio;

        if (Constant.ISTESTDATA) {
            LogUtil.logd("media session[" + sess.getLogId()
                    + "] create Response");
        }
        sess.cancelAllResponse();

//        if (audio instanceof QQMusicAudio) {
//            return new QQMusicMediaResponse(out, sess, from, to);
//        }
//        if (audio instanceof FileAudio) {
//            return new FileMediaResponse(out, sess, from, to);
//        }
//        if (audio instanceof NetAudio) {
//            return new HttpMediaResponse(out, sess, from, to);
//        }
//        return null;

        if (audio instanceof FileAudio) {
            return new FileMediaResponse(out, sess, from, to);
        }else {
            return new HttpMediaResponse(out, sess, from, to);
        }

    }
}

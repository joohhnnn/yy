package com.txznet.audio.player;

import android.util.SparseArray;

import com.txznet.audio.bean.SessionInfo;
import com.txznet.audio.player.audio.PlayerAudio;
import com.txznet.audio.server.LocalMediaServer;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.plugin.CommandString;
import com.txznet.music.baseModule.plugin.PluginMusicManager;
import com.txznet.music.util.BrokenThread;
import com.txznet.txz.plugin.PluginManager;

import java.util.Random;

import static com.txznet.music.baseModule.Constant.PRELOAD_TAG;

public class SessionManager {
    private static SessionManager sInstance = new SessionManager();

    private SessionManager() {

    }

    public static SessionManager getInstance() {
        return sInstance;
    }

    public static String genLocalMediaUrl(SessionInfo sess) {
//		 return
//		 "http://audio.xmcdn.com/group7/M0A/3C/20/wKgDX1WcfSSh1ddlAFoUZBSo4Xo791.m4a";//喜马拉雅
//		 "http://c203.duotin.com/M02/37/15/wKgB5Vc0D5qAJXdQAFtBw-PoEi8807.mp3";//多听
//		return  "http://192.168.0.25:8080/xmly.m4a";
//		return "http://image.kaolafm.net/mz2/outopus_64/20150619/b14cceab-8b4d-4e49-aa3a-6960025614e8.opus";
//        return "http://182.92.22.218/1/playlist.m3u8";
        //转换成为本地服务器上
        // @see  LocalMediaServer
        return "http://127.0.0.1:" + LocalMediaServer.getInstance().getPort()
                + "/" + sess.hashCode() + "?s=" + sess.hashCode() + "&r="
                + new Random().nextInt();
    }

    public synchronized TXZAudioPlayer createPlayer(PlayerAudio audio) {
        String localUrl = null;
        SessionInfo sess = new SessionInfo(audio);
        //本地服务器
//        localUrl=audio instanceof NetAudio ? ((NetAudio) audio).getUrl() : genLocalMediaUrl(sess);

        localUrl = genLocalMediaUrl(sess);


        //装载插件
        PluginMusicManager.getInstance().scanLocalPlugin();


        Object obj = PluginManager.invoke(CommandString.PLUGIN_PLAYER_DOUBLE + CommandString.GETAUDIOPLAYER, sess, localUrl);
        boolean interceptValue = (obj != null && obj instanceof TXZAudioPlayer);
        if (!interceptValue) {
//            if (audio.needCodecPlayer()) {
//                TXZMediaClient mediaClient = new HttpMediaClient(localUrl);
//                sess.player = new CodecAudioPlayer(sess, audio.getStreamType(), mediaClient, audio.getDuration());
//            } else {
//            sess.player = new FFMPEGAudioPlayer(sess, audio.getStreamType(), localUrl);
            sess.player = new IjkAudioPlayer(sess, audio.getStreamType(), localUrl);
//            sess.player = new KaolaTxzPlayer(sess, localUrl);
//            }
        } else {
            sess.player = (TXZAudioPlayer) obj;
        }

        SessionManager.getInstance().addSessionInfo(sess.hashCode(), sess);

//        if (brokenThread != null /*&& audio.getIsNeedPreloadData()*/) {
//            brokenThread.setBolock(true);
//            brokenThread.interrupt();
//            brokenThread = null;
//        }

//        LogUtil.logd("media session[" + sess.getLogId() + "] create session["
//                + audio.toString() + "]: player[" + sess.player.toString()
//                + "], url=" + localUrl);

        return sess.player;
    }

    public SessionInfo getSessionInfo(int playerSessionId) {
        synchronized (mAudioSessions) {
            return mAudioSessions.get(playerSessionId);
        }
    }

    public void addSessionInfo(int playSessionId, SessionInfo sess) {
        synchronized (mAudioSessions) {
            mAudioSessions.put(playSessionId, sess);
            LogUtil.logd("music:oom:add:" + playSessionId);
        }
    }

    public void removeSessionInfo(int playerSessionId) {
        synchronized (mAudioSessions) {
            LogUtil.logd("music:oom:remove:" + playerSessionId);
            mAudioSessions.remove(playerSessionId);
            LogUtil.logd("music:oom:size:" + mAudioSessions.size());
        }
    }

    private SparseArray<SessionInfo> mAudioSessions = new SparseArray<SessionInfo>();


    private static BrokenThread brokenThread = null;

    public static void preloadData(final Audio nextAudio) {
        if (brokenThread == null) {
            brokenThread = new BrokenThread("t_preload");
        } else {
            brokenThread.setBolock(true);
            brokenThread = null;
            preloadData(nextAudio);
            return;
        }
        LogUtil.logd(PRELOAD_TAG + "receive preload request,nextAudio is " + ((nextAudio != null) ? nextAudio.toString() : ""));

        brokenThread.setNextAudio(nextAudio);

        brokenThread.start();

    }

}

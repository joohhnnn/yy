package com.txznet.music.util;

import android.os.Bundle;
import android.os.Message;
import android.os.Process;

import com.txznet.audio.bean.SessionInfo;
import com.txznet.audio.player.RemoteAudioPlayer;
import com.txznet.audio.player.SessionManager;
import com.txznet.audio.player.SysAudioPlayer;
import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.audio.player.factory.PlayAudioFactory;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.playerModule.bean.PlayItem;
import com.txznet.music.playerModule.logic.PlayItemManager;
import com.txznet.music.playerModule.logic.factory.TxzAudioPlayerFactory;
import com.txznet.music.utils.DataInterfaceBroadcastHelper;
import com.txznet.music.utils.JsonHelper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.txznet.audio.player.PlayerServiceConstants.CLIENT_ACTION_SET_AUDIO;
import static com.txznet.audio.player.PlayerServiceConstants.CLIENT_ACTION_THEURL;
import static com.txznet.audio.player.PlayerServiceConstants.KEY_AUDIO;
import static com.txznet.audio.player.PlayerServiceConstants.KEY_KEY;
import static com.txznet.audio.player.PlayerServiceConstants.KEY_PID;
import static com.txznet.audio.player.PlayerServiceConstants.KEY_PLAY_ITEM;
import static com.txznet.audio.player.PlayerServiceConstants.KEY_SID;
import static com.txznet.music.baseModule.Constant.PRELOAD_TAG;


/**
 * Created by telenewbie on 2017/4/11.
 */

public class BrokenThread extends Thread {
    protected boolean isBolock = false;
    private Audio nextAudio;

    public void setBolock(boolean isBolock) {
        this.isBolock = isBolock;
    }

    public Audio getNextAudio() {
        return nextAudio;
    }

    public void setNextAudio(Audio nextAudio) {
        this.nextAudio = nextAudio;
    }

    @Override
    public synchronized void run() {/*
        SessionInfo sess = null;
        try {
            if (nextAudio != null) {//表示没有下一首歌曲
                LogUtil.logd(PRELOAD_TAG + "预缓存:[" + nextAudio.getName() + "]开始");
                //TODO:预加载下一首
                final TXZAudioPlayer player = TxzAudioPlayerFactory.createPlayer(nextAudio);
                if (player != null) {
                    player.setOnPreparedListener(new TXZAudioPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(TXZAudioPlayer ap) {
                            LogUtil.logd(PRELOAD_TAG + "预缓存:[" + nextAudio.getName() + "]结束");
                            player.release();
                        }
                    });
                    player.prepareAsync();
                }
            } else {
                LogUtil.logd(PRELOAD_TAG + "nextAudio:" + "null");
            }
        } catch (Exception ex) {
            LogUtil.loge(PRELOAD_TAG + "预缓存:[" + nextAudio.getName() + "]occur error", ex);
        } finally {
            if (sess != null) {
                SessionManager.getInstance().removeSessionInfo(sess.hashCode());
            }
            nextAudio = null;
        }

        super.run();
    */}

    public BrokenThread(Runnable runnable, String threadName) {
        super(runnable, threadName);
    }

    public BrokenThread(String threadName) {
        super(threadName);
    }
}

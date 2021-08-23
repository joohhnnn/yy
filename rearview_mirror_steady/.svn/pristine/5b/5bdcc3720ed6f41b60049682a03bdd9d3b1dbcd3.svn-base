package com.txznet.music.util;

import com.txznet.music.albumModule.bean.Audio;


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

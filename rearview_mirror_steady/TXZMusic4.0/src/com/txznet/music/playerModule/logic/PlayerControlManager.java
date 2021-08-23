package com.txznet.music.playerModule.logic;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by telenewbie on 2017/6/29.
 */

public class PlayerControlManager {
    //##创建一个单例类##
    private volatile static PlayerControlManager singleton;

    private PlayerControlManager() {
    }

    public static PlayerControlManager getInstance() {
        if (singleton == null) {
            synchronized (PlayerControlManager.class) {
                if (singleton == null) {
                    singleton = new PlayerControlManager();
                }
            }
        }
        return singleton;
    }


    ConcurrentHashMap<Integer, List<IPlayerStateListener>> map = new ConcurrentHashMap<Integer, List<IPlayerStateListener>>();

    private void dispatchCurrentState(IPlayerStateListener listener) {
        switch (PlayEngineFactory.getEngine().getState()) {
            case PlayerInfo.PLAYER_STATUS_PLAYING:
                listener.onPlayerPlaying(PlayEngineFactory.getEngine().getCurrentAudio());
                break;
            case PlayerInfo.PLAYER_STATUS_RELEASE:
            case PlayerInfo.PLAYER_STATUS_PAUSE:
                listener.onPlayerPaused(PlayEngineFactory.getEngine().getCurrentAudio());
                break;
            case PlayerInfo.PLAYER_STATUS_BUFFER:
                listener.onPlayerPreparing(PlayEngineFactory.getEngine().getCurrentAudio());
                break;
        }
    }
    //回调播放完成

    public synchronized void addStatusListener(int sid, IPlayerStateListener listener) {
        if (map.get(sid) == null) {
            map.put(sid, new CopyOnWriteArrayList<>());
        }
        map.get(sid).add(listener);
    }

    public synchronized void remoteAllCallback(int sid) {
        if (map.get(sid) != null) {
            map.get(sid).clear();
            map.remove(sid);
        }
    }

    public synchronized void notifyOnPreparedListener(int sid, Audio audio) {
        if (map.get(sid) != null) {
            for (IPlayerStateListener listener : map.get(sid)) {
                listener.onPlayerPreparing(audio);
            }
        }
    }

    public synchronized void notifyOnPreparedStartListener(int sid, Audio audio) {
        if (map.get(sid) != null) {
            for (IPlayerStateListener listener : map.get(sid)) {
                listener.onPlayerPrepareStart(audio);
            }
        }
    }

    public synchronized void notifyOnPlayingListener(int sid, Audio audio) {
        if (map.get(sid) != null) {
            for (IPlayerStateListener listener : map.get(sid)) {
                listener.onPlayerPlaying(audio);
            }
        }
    }

    public synchronized void notifyOnPauseListener(int sid, Audio audio) {
        if (map.get(sid) != null) {
            for (IPlayerStateListener listener : map.get(sid)) {
                listener.onPlayerPaused(audio);
            }
        }
    }

    public synchronized void notifyOnBufferStartListener(int sid, Audio audio) {
        if (map.get(sid) != null) {
            for (IPlayerStateListener listener : map.get(sid)) {
                listener.onBufferingStart(audio);
            }
        }
    }

    public synchronized void notifyOnBufferEndListener(int sid, Audio audio) {
        if (map.get(sid) != null) {
            for (IPlayerStateListener listener : map.get(sid)) {
                listener.onBufferingEnd(audio);
            }
        }
    }

    public synchronized void notifyOnSeekStartListener(int sid, Audio audio) {
        if (map.get(sid) != null) {
            for (IPlayerStateListener listener : map.get(sid)) {
                listener.onSeekStart(audio);
            }
        }
    }

    public synchronized void notifyOnSeekCompleteListener(int sid, Audio audio, long seekTime) {
        if (map.get(sid) != null) {
            for (IPlayerStateListener listener : map.get(sid)) {
                listener.onSeekComplete(audio, seekTime);
            }
        }
    }

    public synchronized void notifyOnPlayCompleteListener(int sid, Audio audio) {
        if (map.get(sid) != null) {
            for (IPlayerStateListener listener : map.get(sid)) {
                listener.onPlayerEnd(audio);
            }
        }
    }

    public synchronized void notifyOnErrorListener(int sid, Audio audio, Error error) {
        if (map.get(sid) != null) {
            for (IPlayerStateListener listener : map.get(sid)) {
                listener.onPlayerFailed(audio, error);
            }
        }
    }

    public synchronized void notifyOnProgressListener(int sid, Audio audio, long position, long duration) {
        if (map.get(sid) != null) {
            for (IPlayerStateListener listener : map.get(sid)) {
                listener.onProgress(audio, position, duration);
            }
        }
    }

    public synchronized void notifyOnBufferProgressListener(int sid, Audio audio, List<LocalBuffer> buffers) {
        if (map.get(sid) != null) {
            for (IPlayerStateListener listener : map.get(sid)) {
                listener.onBufferProgress(audio, buffers);
            }
        }
    }


    public synchronized void notifyOnStopListener(int sid, Audio mAudio) {
        if (map.get(sid) != null) {
            for (IPlayerStateListener listener : map.get(sid)) {
                listener.onIdle(mAudio);
            }
        }
    }
}

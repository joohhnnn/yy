package com.txznet.music.playerModule.logic;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.util.TimeUtils;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 保存播放的信息，例如播放列表和当前播放的歌曲信息
 * Created by brainBear on 2017/6/7.
 */
public class PlayInfoManager {
    public static final int DATA_NET = 1;
    public static final int DATA_HISTORY = 2;
    public static final int DATA_LOCAL = 3;
    public static final int DATA_SEARCH = 4;
    public static final int DATA_FAVOUR = 5;
    public static final int DATA_SUBSCRIBE = 6;
    public static final int DATA_PUSH = 7;
    public static final int DATA_MESSAGE = 8;

    private static final String TAG = "music:info:";
    private static PlayInfoManager sInstance;
    private static List<IPlayListChangedListener> mPlayListChangedListeners = new CopyOnWriteArrayList<IPlayListChangedListener>();
    private static List<PlayerInfoUpdateListener> sPlayerInfoUpdateListeners = new CopyOnWriteArrayList<>();

    /**
     * 当前播放列表
     */
    private List<Audio> mPlayList = new ArrayList<>();
    /**
     * 音频时长
     */
    private long duration;
    /**
     * 当前播放进度
     */
    private long progress;
    private long mCurrentPosition;//当前播放的位置
    private int mPosition;// audio相对于播放列表的位置
    // 当前播放的音频所属的专辑
    private Album mCurrentAlbum;
    // 当前播放的音频
    private Audio mCurrentAudio;
    // 当前的播放模式
    @PlayerInfo.PlayerMode
    private int mCurrentPlayMode = PlayerInfo.PLAYER_MODE_SEQUENCE;
    private int nextPosition = -1;
    //表示头部是否到头
    private boolean isFirstEnd = false;
    //表示尾部是否到头
    private boolean isLastEnd = false;
    /**
     * 当前专辑总数量
     */
    private int mPlayListTotalNum = 0;
    /**
     * 当前播放状态
     */
    private int mCurrentPlayerUIStatus;
    private int mCurrentScene = 0;
    private int mCurrentPage = 0;//当前的页数


    private List<LocalBuffer> mBufferProgress;

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.mCurrentPage = currentPage;
    }

    private PlayInfoManager() {
    }

    public static PlayInfoManager getInstance() {
        if (null == sInstance) {
            synchronized (PlayInfoManager.class) {
                if (null == sInstance) {
                    sInstance = new PlayInfoManager();
                }
            }
        }
        return sInstance;
    }

    public int getCurrentScene() {
        return mCurrentScene;
    }

    @PlayerInfo.PlayerUIStatus
    public int getCurrentPlayerUIStatus() {
        return mCurrentPlayerUIStatus;
    }

    public void setCurrentPlayerUIStatus(@PlayerInfo.PlayerUIStatus int currentPlayerStatus) {
        if (this.mCurrentPlayerUIStatus != currentPlayerStatus) {
            this.mCurrentPlayerUIStatus = currentPlayerStatus;
            notifyPlayerStatusUpdated();
        }
    }


    public void setProgress(long progress, long duration) {
        this.progress = progress;
        this.duration = duration;
        notifyProgressUpdated();
    }


    public List<LocalBuffer> getBufferProgress() {
        return mBufferProgress;
    }

    public void setBufferProgress(List<LocalBuffer> bufferProgress) {
        this.mBufferProgress = bufferProgress;
        notifyBufferProgressUpdated();
    }

    public long getDuration() {
        return duration;
    }


    public long getProgress() {
        return progress;
    }


    public boolean isLastEnd() {
        return isLastEnd;
    }

    public void setLastEnd(boolean lastEnd) {
        isLastEnd = lastEnd;
    }

    public boolean isFirstEnd() {
        return isFirstEnd;
    }

    public void setFirstEnd(boolean firstEnd) {
        isFirstEnd = firstEnd;
    }

    public int getPlayListTotalNum() {
        return mPlayListTotalNum;
    }

    public void setPlayListTotalNum(int mPlayListTotalNum) {
        this.mPlayListTotalNum = mPlayListTotalNum;
    }

    /**
     * @param force 是否强制
     * @return
     */
    public synchronized Audio getNextAudio(boolean force) {
        int size = getPlayListSize();
        do {
            if (nextPosition >= 0) {
                break;
            }
            switch (getCurrentPlayMode()) {
                case PlayerInfo.PLAYER_MODE_SEQUENCE:
                    nextPosition = getPosition() + 1;
                    break;
                case PlayerInfo.PLAYER_MODE_SINGLE_CIRCLE:
                    if (force) {
                        nextPosition = getPosition() + 1;
                    } else {
                        nextPosition = getPosition();
                    }

                    break;
                case PlayerInfo.PLAYER_MODE_RANDOM:
                    int randomInt = (int) (Math.random() * size);
                    if (getPosition() == randomInt) {
                        nextPosition = getPosition() + 1;
                    } else {
                        nextPosition = randomInt;
                    }
                    break;
            }
        } while (false);
        LogUtil.logd(TAG + "---1 next audio index=" + nextPosition + "/" + getPosition() + "/" + getCurrentPlayMode());
        if (nextPosition >= size || nextPosition < 0) {
            nextPosition = 0;
        }
        LogUtil.logd(TAG + "---2 next audio index=" + nextPosition);
        if (!PlayInfoManager.getInstance().isPlayListEmpty() && size > nextPosition) {
            return getPlayListAudio(nextPosition);
        } else if (size > 0) {
            nextPosition = 0;
            return getPlayListAudio(nextPosition);
        } else {
            nextPosition = -1;
            return null;
        }
    }

    /**
     * 获取播放列表，最好不要直接调用这个方法，应该调用封装好的方法
     *
     * @return 播放列表
     */
    @Deprecated
    public List<Audio> getPlayList() {
        return mPlayList;
    }

    /**
     * 判断播放列表是否为空
     *
     * @return true为空
     */
    public boolean isPlayListEmpty() {
        return null == mPlayList || mPlayList.isEmpty();
    }

    /**
     * 按下标获取当前播放列表的Audio
     *
     * @param index 下标
     * @return 下标对应的Audio
     */
    public Audio getPlayListAudio(int index) {
        return mPlayList.get(index);
    }

    /**
     * 获取当前播放列表中歌曲数量
     *
     * @return 歌曲数量
     */
    public int getPlayListSize() {
        return mPlayList.size();
    }

    /**
     * 判断指定Audio是否在当前播放列表中
     *
     * @param audio 指定的Audio
     * @return 如果存在则返回true
     */
    public boolean playListContain(Audio audio) {
        return mPlayList.contains(audio);
    }

    /**
     * 返回指定Audio在当前播放列表中的下标
     *
     * @param audio 指定Audio
     * @return 指定Audio在当前播放列表的下标，如果不存在则返回-1
     */
    public int playListIndexOf(Audio audio) {
        return mPlayList.indexOf(audio);
    }

    public long getCurrentPosition() {
        return mCurrentPosition;
    }

    public void setCurrentPosition(long mCurrentPosition) {
        this.mCurrentPosition = mCurrentPosition;
    }

    public int getPosition() {
//        return getPlayList().indexOf(mCurrentAudio);
        return mPosition;
    }

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    public Album getCurrentAlbum() {
        return mCurrentAlbum;
    }

    public void setCurrentAlbum(Album mCurrentAlbum) {
        this.mCurrentAlbum = mCurrentAlbum;
        if (null != mCurrentAlbum) {
            Logger.d(TAG, "current album:" + mCurrentAlbum.toString());
        }
    }

    public Audio getCurrentAudio() {
        return mCurrentAudio;
    }

    public synchronized void setCurrentAudio(Audio currentAudio) {
        nextPosition = -1;
        setPosition(playListIndexOf(currentAudio));
        if (this.mCurrentAudio != currentAudio) {
            this.mCurrentAudio = currentAudio;
            notifyPlayInfoUpdated();
        }

        if (null != mCurrentAudio) {
            Logger.d(TAG, "current audio:" + mCurrentAudio.toString());
        }
    }

    @PlayerInfo.PlayerMode
    public int getCurrentPlayMode() {
        return mCurrentPlayMode;
    }

    public synchronized void setCurrentPlayMode(@PlayerInfo.PlayerMode int currentPlayMode) {
        nextPosition = -1;
        if (this.mCurrentPlayMode != currentPlayMode) {
            this.mCurrentPlayMode = currentPlayMode;
            notifyPlayerModeUpdated();
        }
    }

    @PlayerInfo.PlayerMode
    public int getNextPlayerMode() {
        int currentPlayMode = getCurrentPlayMode() + 1;
        currentPlayMode %= 3;
        int result = PlayerInfo.PLAYER_MODE_SEQUENCE;
        switch (currentPlayMode) {
            case PlayerInfo.PLAYER_MODE_RANDOM:
                result = PlayerInfo.PLAYER_MODE_RANDOM;
                break;
            case PlayerInfo.PLAYER_MODE_SEQUENCE:
                result = PlayerInfo.PLAYER_MODE_SEQUENCE;
                break;
            case PlayerInfo.PLAYER_MODE_SINGLE_CIRCLE:
                result = PlayerInfo.PLAYER_MODE_SINGLE_CIRCLE;
                break;
        }
        return result;
    }

    public void addPlayListChangedListener(final IPlayListChangedListener iPlayListChangedListener) {
        if (!mPlayListChangedListeners.contains(iPlayListChangedListener)) {
            mPlayListChangedListeners.add(iPlayListChangedListener);

            iPlayListChangedListener.onPlayListChanged(mPlayList);
        }
    }

    public void removePlayListChangedListener(final IPlayListChangedListener iPlayListChangedListener) {
        mPlayListChangedListeners.remove(iPlayListChangedListener);
    }

    public void notifyPlayListChanged() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                for (IPlayListChangedListener listener : mPlayListChangedListeners) {
                    listener.onPlayListChanged(mPlayList);
                }
            }
        });
    }

    public boolean removePlayListAudio(Audio audio) {
        boolean result = mPlayList.remove(audio);
        if (result) {
            if (audio != null && audio.equals(getCurrentAudio())) {
                if (PlayEngineFactory.getEngine().isNextOperationLastTime()) {
                    nextPosition -= 1;
                    LogUtil.logd("reset position, position=" + (getPosition() - 1) + ", nextPosition=" + nextPosition);
                    setPosition(getPosition() - 1);
                } else {
                    nextPosition += 1;
                    LogUtil.logd("reset position, position=" + (getPosition() - 1) + ", nextPosition=" + nextPosition);
                    setPosition(getPosition());
                }
                if (getPosition() >= 0 && getPosition() < mPlayList.size()) {
                    mCurrentAudio = mPlayList.get(getPosition());
                }
            }
            notifyPlayListChanged();
        }
        return result;
    }

    public boolean removePlayListAudioNotSkip(Audio audio) {
        boolean result = mPlayList.remove(audio);
        if (result) {
            notifyPlayListChanged();
        }
        return result;
    }


    public boolean removePlayListAudios(List<Audio> audios) {
        boolean result = mPlayList.removeAll(audios);
        if (result) {
            notifyPlayListChanged();
        }
        return result;
    }

    public synchronized void addAudios(boolean isAddLast, List<Audio> audios) {
        TimeUtils.startTime(Constant.SPEND_TAG + "quick:addAudios");
        if (com.txznet.comm.util.CollectionUtils.isNotEmpty(audios)) {
            deleteSameAudiosFromSource(audios);
            if (isAddLast) {
                setCurrentPage(getCurrentPage() + 1);
                mPlayList.addAll(audios);
            } else {
                setCurrentPage(1);
                mPlayList.addAll(0, audios);
            }
            if (null != mCurrentAudio) {
                nextPosition = -1;
                setPosition(PlayInfoManager.getInstance().playListIndexOf(mCurrentAudio));
            }
            notifyPlayListChanged();
        }
        TimeUtils.endTime(Constant.SPEND_TAG + "quick:addAudios");
    }

    /**
     * @param audios
     * @param ori    内容来源
     */
    public void setAudios(List<Audio> audios, int ori) {
        mPlayList.clear();
        mPlayList.addAll(CollectionUtils.expertNullItem(audios));
        mCurrentScene = ori;
        setPosition(0);
        notifyPlayListChanged();
    }


    private void deleteSameAudiosFromSource(List<Audio> audios) {
        if (!PlayInfoManager.getInstance().isPlayListEmpty() && com.txznet.comm.util.CollectionUtils.isNotEmpty(audios)) {
            for (Audio audio : audios) {
                if (playListContain(audio)) {
                    removePlayListAudio(audio);
                }
            }
        }
    }

    /**
     * 移除播放列表中被删除的本地歌曲
     */
    public void removeNotExistLocalFile() {
        Iterator<Audio> iterator = mPlayList.iterator();
        final List<Audio> audios = new ArrayList<>();
        while (iterator.hasNext()) {
            Audio next = iterator.next();
            if (next.getSid() == 0 && !new File(next.getStrDownloadUrl()).exists()) {
                audios.add(next);
                iterator.remove();
            }
        }
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                DBManager.getInstance().removeLocalAudios(audios);
            }
        });
        notifyPlayListChanged();
    }

    public void addPlayerInfoUpdateListener(final PlayerInfoUpdateListener listener) {
        if (!sPlayerInfoUpdateListeners.contains(listener)) {
            sPlayerInfoUpdateListeners.add(listener);
        }
    }


    public void removePlayerInfoUpdateListener(final PlayerInfoUpdateListener listener) {
        sPlayerInfoUpdateListeners.remove(listener);
    }


    private void notifyPlayInfoUpdated() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                for (PlayerInfoUpdateListener listener : sPlayerInfoUpdateListeners) {
                    listener.onPlayInfoUpdated(getCurrentAudio(), getCurrentAlbum());
                }
            }
        });
    }

    private void notifyProgressUpdated() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                for (PlayerInfoUpdateListener listener : sPlayerInfoUpdateListeners) {
                    listener.onProgressUpdated(getProgress(), getDuration());
                }
            }
        });
    }

    private void notifyPlayerModeUpdated() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                for (PlayerInfoUpdateListener listener : sPlayerInfoUpdateListeners) {
                    listener.onPlayerModeUpdated(getCurrentPlayMode());
                }
            }
        });
    }

    private void notifyPlayerStatusUpdated() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                for (PlayerInfoUpdateListener listener : sPlayerInfoUpdateListeners) {
                    listener.onPlayerStatusUpdated(getCurrentPlayerUIStatus());
                }
            }
        });
    }

    private void notifyBufferProgressUpdated() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                for (PlayerInfoUpdateListener listener : sPlayerInfoUpdateListeners) {
                    listener.onBufferProgressUpdated(getBufferProgress());
                }
            }
        });
    }

}

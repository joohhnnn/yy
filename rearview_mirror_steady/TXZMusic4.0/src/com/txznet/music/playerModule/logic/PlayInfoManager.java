package com.txznet.music.playerModule.logic;

import android.content.Intent;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.bean.BreakpointAudio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.data.http.resp.RespCarFmCurTops;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.playerModule.logic.listener.IPlayerListCarfmOtherListener;
import com.txznet.music.playerModule.logic.listener.IPlayerListEndListener;
import com.txznet.music.util.TimeUtils;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.Utils;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 保存播放的信息，例如播放列表和当前播放的歌曲信息
 * Created by brainBear on 2017/6/7.
 */
public class PlayInfoManager {
    public static final int DATA_INIT = 0;
    //        public static final int DATA_NET = 1;
    public static final int DATA_ALBUM = 1;
    public static final int DATA_ALBUM_MANUFACTORY = 50;//50开始
    public static final int DATA_HISTORY = 2;
    public static final int DATA_LOCAL = 3;
    public static final int DATA_SEARCH = 4;
    public static final int DATA_FAVOUR = 5;
    public static final int DATA_SUBSCRIBE = 6;
    public static final int DATA_PUSH = 7;//推送  后台定义，值为100-200（后台有特殊用处）（客户端可以判断100-200来判断是否是推送，并可以做区别处理）
    public static final int DATA_MESSAGE = 8;
    public static final int DATA_CHEZHU_FM = 9;//车主FM,因为拥有特殊操作,播放完毕则跳过.且轮询之后,不跳过.
    public static final int DATA_CHEZHU_TYPE_FM = 10;//分类FM


//    @IntDef({
//
//    })
//    public @interface DATA_SCREEN {
//
//    }


    private static final String TAG = "music:info:";
    private static PlayInfoManager sInstance;
    private static List<IPlayListChangedListener> mPlayListChangedListeners = new CopyOnWriteArrayList<IPlayListChangedListener>();
    private static List<PlayerInfoUpdateListener> sPlayerInfoUpdateListeners = new CopyOnWriteArrayList<>();
    private static IPlayerListEndListener sPlayerListEndListener = null;
    private static IPlayerListCarfmOtherListener sIPlayerListCarfmOtherListener = null;
    private static List<Album> mCarFmAlbums = new CopyOnWriteArrayList<>();

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
    /**
     * 当前收藏状态
     */
    private int mCurrentFavourMode;
    private int mCurrentScene = DATA_INIT;


    private List<LocalBuffer> mBufferProgress;
    private Audio requestMoreAudio;

    private PlayInfoManager() {
    }

    /**
     * 初始化数据
     */
    public void initData() {
        mPlayListTotalNum = 0;
        mCurrentScene = DATA_INIT;
        mBufferProgress = null;
        requestMoreAudio = null;
        mCurrentAlbum = null;
        mCurrentAudio = null;
        duration = 0;
        progress = 0;
        mCurrentPosition = 0;
        nextPosition = -1;
        mCurrentPlayMode = PlayerInfo.PLAYER_MODE_SEQUENCE;
        isFirstEnd = false;
        isLastEnd = false;
        setAudios(null, DATA_INIT);
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

    public void setCompleteState(Audio audio) {
        notifyPlayComplete(audio);
    }

    private void notifyPlayComplete(final Audio audio) {
//        AppLogic.runOnUiGround(new Runnable() {
//            @Override
//            public void run() {
//                for (PlayerInfoUpdateListener listener : sPlayerInfoUpdateListeners) {
//                    listener.onPlayComplete(audio);
//                }
//            }
//        });
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

    /**
     * 获取需要订阅的专辑
     *
     * @return
     */
    public Album getSubscribeAlbum() {
        Album album = mCurrentAlbum;
        if (getCurrentAudio() != null) {
            if (getCurrentAudio().getAlbum() != null) {
                album = getCurrentAudio().getAlbum();
            }
        }
        return album;
    }

    public Album getParentAlbum() {
        Album album = null;
        if (mCurrentAlbum != null) {
            album = mCurrentAlbum.getParentAlbum();
        }
        return album;
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


    /**
     * 是否需要拦截播放结束时候的逻辑
     *
     * @param audio
     * @param album
     * @return
     */
    public boolean needInterceptPlayListLogic(final Audio audio, final Album album) {
        if (sPlayerListEndListener != null) {
            return sPlayerListEndListener.onListEnd(audio, album);
        }
        return false;
    }

    public void setPlayerListEndListener(IPlayerListEndListener playerListEndListener) {
        sPlayerListEndListener = playerListEndListener;
    }

    public void clearPlayerListEndListener() {
        sPlayerListEndListener = null;
    }

    /**
     * 是否需要拦截当前分时段变更的逻辑
     *
     * @return
     */
    public boolean needInterceptPlayListCarFmLogic(RespCarFmCurTops data) {
        if (sIPlayerListCarfmOtherListener != null) {
            return sIPlayerListCarfmOtherListener.otherCarFm(data);
        }
        return false;
    }


    public void setsIPlayerListCarFmOtherListener(IPlayerListCarfmOtherListener playerListCarFmOtherListener) {
        sIPlayerListCarfmOtherListener = playerListCarFmOtherListener;
    }

    public void clearPlayerListCarFmListener() {
        sIPlayerListCarfmOtherListener = null;
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
        boolean result = false;
        int i = mPlayList.indexOf(audio);
        if (i >= 0) {
            int currentPosition = PlayInfoManager.getInstance().playListIndexOf(mCurrentAudio);
            result = mPlayList.remove(audio);

            if (result) {
                notifyPlayListChanged();
                if (audio != null) {
                    if (currentPosition > i) {
                        nextPosition -= 1;
                        setPosition(getPosition() - 1);
                    } else if (currentPosition == i) {
                        //切歌
                    }
                }
            }
        }
        return result;
    }

    public boolean removePlayListAudios(List<Audio> audios) {
        //先删除不包含在播放的音乐，然后再删除播放的音乐（此时需要获取，当前的索引值）
        boolean needRemoveCurrentAudio = false;
        boolean result = false;
        if (audios.contains(mCurrentAudio)) {
            audios.remove(mCurrentAudio);
            needRemoveCurrentAudio = true;
        }
        result = mPlayList.removeAll(audios);

        //获取当前的索引
        int playIndex = mPlayList.indexOf(mCurrentAudio);

        int currentPosition = PlayInfoManager.getInstance().playListIndexOf(mCurrentAudio);
        if (needRemoveCurrentAudio) {
            audios.add(mCurrentAudio);//将数据还原
            mPlayList.remove(mCurrentAudio);
            nextPosition = currentPosition;
            setPosition(-1);
        } else {
            nextPosition = currentPosition;
            setPosition(playIndex);
        }


        if (result) {
            notifyPlayListChanged();
        }
        return result;
    }

//
//
//    public boolean removePlayListAudios(List<Audio> audios) {
//        boolean result = mPlayList.removeAll(audios);
//        if (result) {
//            notifyPlayListChanged();
//        }
//        return result;
//    }


    public synchronized void addAudios(boolean isAddLast, List<Audio> audios) {
        TimeUtils.startTime(Constant.SPEND_TAG + "quick:addAudios");
        if (com.txznet.comm.util.CollectionUtils.isNotEmpty(audios)) {

            deleteSameAudiosFromSource(audios);
            if (isAddLast) {
                mPlayList.addAll(audios);
            } else {
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

    public synchronized void addAudio(Audio audio, int index) {
        TimeUtils.startTime(Constant.SPEND_TAG + "quick:addAudio");
        Assert.assertNotNull(audio);
        if (index < 0) {
            index = 0;
        } else if (index > mPlayList.size()) {
            index = mPlayList.size();
        }

        mPlayList.add(index, audio);
        if (null != mCurrentAudio) {
            nextPosition = -1;
            setPosition(PlayInfoManager.getInstance().playListIndexOf(mCurrentAudio));
        }
        notifyPlayListChanged();
        TimeUtils.endTime(Constant.SPEND_TAG + "quick:addAudio");
    }

    /**
     * @param audios
     * @param ori    内容来源
     */
    public void setAudios(List<Audio> audios, int ori) {
        mPlayList.clear();
        if (CollectionUtils.isNotEmpty(audios)) {
            mPlayList.addAll(audios);
        }
        mCurrentScene = ori;
        setPosition(0);
        notifyPlayListChanged();

        if (CollectionUtils.isEmpty(audios)) {
            notifyPlayInfoUpdated();
        }
    }


    /**
     * 删除重复的值，传递进来的值也有可能存在重复，所有对传递进来的值进行去重
     *
     * @param audios
     * @return
     */
    private void deleteSameAudiosFromSource(List<Audio> audios) {
        if (!PlayInfoManager.getInstance().isPlayListEmpty() && com.txznet.comm.util.CollectionUtils.isNotEmpty(audios)) {
            Set<Audio> audioSet = new LinkedHashSet<Audio>();
            audioSet.addAll(audios);
            for (Audio audio : audioSet) {
                if (playListContain(audio)) {
                    removePlayListAudio(audio);
                }
            }
            audios.clear();
            audios.addAll(audioSet);
        }

        return;
    }

    /**
     * 移除播放列表中被删除的本地歌曲
     */
    public void removeNotExistLocalFile() {
        Iterator<Audio> iterator = mPlayList.iterator();
        final List<Audio> audios = new ArrayList<>();
        while (iterator.hasNext()) {
            Audio next = iterator.next();
            if (Utils.isLocalSong(next.getSid())) {
                if (!next.isLocal()) {
                    audios.add(next);
                    iterator.remove();
                }
            } else if (Utils.isSong(next.getSid())) {
                if (next.getStrDownloadUrl().endsWith(".tmd")) {
                    if (!next.isLocal()) {
                        audios.add(next);
                        iterator.remove();
                    }
                }
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
            listener.onPlayInfoUpdated(PlayInfoManager.getInstance().getCurrentAudio(), PlayInfoManager.getInstance().getCurrentAlbum());
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

    private void notifyFavourStatus(final int favourState) {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                for (PlayerInfoUpdateListener listener : sPlayerInfoUpdateListeners) {
                    listener.onFavourStatusUpdated(favourState);
                }
            }
        });
    }

    public void setRequestMoreAudio(Audio requestMoreAudio) {
        this.requestMoreAudio = requestMoreAudio;
    }

    public Audio getRequestMoreAudio() {
        return requestMoreAudio;
    }

    public int getCurrentFavourMode() {
        return mCurrentFavourMode;
    }

    public void setCurrentFavourMode(Album album, Audio audio) {

        if (album != null) {
            if (getCurrentAlbum() == null || !getCurrentAlbum().equals(album)) {
                return;
            }
        }
        if (audio != null) {
            if (getCurrentAudio() == null || !getCurrentAudio().equals(audio)) {
                return;
            }
        }
        this.mCurrentFavourMode = Utils.getFavourFlag(album, audio);
        notifyFavourStatus(mCurrentFavourMode);
    }

    /**
     * 获取播放列表中,播放仅一次,并且没有播放断点的记录
     *
     * @return
     */
    public Audio getOnceLastPlayAudio(int startPosition) {
//判断当前播放列表是否全部播放完毕一次
        List<BreakpointAudio> breakpointAudios = DBManager.getInstance().findBreakpoint(getCurrentAlbum());

        int i = startPosition;
        int max = getPlayListSize();
        int count = 0;
        Logger.d(TAG, "music:test:::size:" + getPlayListSize() + "," + breakpointAudios.size());

        for (; i < max; ) {

            count++;
            Audio audio = mPlayList.get(i);
            BreakpointAudio breakpointAudio = new BreakpointAudio();
            breakpointAudio.setSid(audio.getSid());
            breakpointAudio.setId(audio.getId());
            int i1 = breakpointAudios.indexOf(breakpointAudio);
            if (i1 < 0) {
                return audio;
            }
            BreakpointAudio breakpointAudio1 = breakpointAudios.get(i1);
            Logger.d(TAG, "music:test:::index:" + i + "," + max + "," + count + "," + breakpointAudio1.toString());
            if (breakpointAudio1.getBreakpoint() > 0) {
                return audio;
            }
            if (breakpointAudio1.getPlayEndCount() == 0 && breakpointAudio1.getBreakpoint() == 0) {
                return audio;
            }
            if (breakpointAudio1.getPlayEndCount() > 1 && breakpointAudios.size() == getPlayListSize()) {
                return audio;
            }
            if (breakpointAudio1.getPlayEndCount() == 1) {
                if (count == getPlayListSize()) {//全部比较完毕
                    return null;
                }
            }


            if (i == getPlayListSize() - 1) {
                i = 0;
                max = startPosition;
            } else {
                i++;
            }
        }
        return null;
    }

    /**
     * 播放列表是否全部播放完毕,仅一次
     *
     * @return
     */
    public Audio isAllPlayEndOnce() {
        //判断当前播放列表是否全部播放完毕一次

        return getOnceLastPlayAudio(getPosition());
    }

    public static final String INTENT_FIELD_ALBUM = "album";
    public static final String INTENT_FIELD_PARENT_ALBUM = "albumID";
    public static final String INTENT_FIELD_CATEGORY_ID = "categoryId";
    public static final String INTENT_FIELD_SHOW_URL = "albumShowURL";
    public static final String INTENT_FIELD_TYPE = "type";
    public static final String INTENT_FIELD_SCREEN = "screen";
    public static final int TYPE_CAR_FM = 1;//车主FM
    public static final int TYPE_NORMAL_FM = 2;//分类Fm
    public static final int TYPE_NORMAL_ALBUM = 3;//正常的专辑

    /**
     * @param intent
     * @param screen         来源
     * @param readyPlayAlbum
     * @param categoryID
     * @param albumShowURL
     * @param type
     * @param parentAlbum    父类的album
     * @return
     */
    public Intent addReadyPlayInfoIntent(Intent intent, int screen, Album readyPlayAlbum, Album parentAlbum, long categoryID, String albumShowURL, int type) {
        if (intent == null) {
            intent = new Intent();
        }
        intent.putExtra(INTENT_FIELD_ALBUM, JsonHelper.toJson(readyPlayAlbum));
        intent.putExtra(INTENT_FIELD_PARENT_ALBUM, JsonHelper.toJson(parentAlbum));
        intent.putExtra(INTENT_FIELD_CATEGORY_ID, categoryID);
        intent.putExtra(INTENT_FIELD_SHOW_URL, albumShowURL);
        intent.putExtra(INTENT_FIELD_TYPE, type);
        intent.putExtra(INTENT_FIELD_SCREEN, screen);
        return intent;
    }

    /**
     * @param intent
     * @param screen         来源
     * @param readyPlayAlbum
     * @param categoryID
     * @param albumShowURL
     * @param type
     * @return
     */
    public Intent addReadyPlayInfoIntent(Intent intent, int screen, Album readyPlayAlbum, long categoryID, String albumShowURL, int type) {
        if (intent == null) {
            intent = new Intent();
        }
        intent.putExtra(INTENT_FIELD_ALBUM, JsonHelper.toJson(readyPlayAlbum));
        intent.putExtra(INTENT_FIELD_CATEGORY_ID, categoryID);
        intent.putExtra(INTENT_FIELD_SHOW_URL, albumShowURL);
        intent.putExtra(INTENT_FIELD_TYPE, type);
        intent.putExtra(INTENT_FIELD_SCREEN, screen);
        return intent;
    }
//    public Intent addReadyPlayInfoIntent(Intent intent, Album readyPlayAlbum, long categoryID, String albumShowURL) {
//        if (intent == null) {
//            intent = new Intent();
//        }
//        intent.putExtra("album", JsonHelper.toJson(readyPlayAlbum));
//        intent.putExtra("categoryId", categoryID);
//        intent.putExtra("albumShowURL", albumShowURL);
//        return intent;
//    }


//    public void addReadyRadioFMPlayInfo(Album albumFM, long categoryID, String albumShowURL) {
//        this.albumFM = albumFM;
//        this.categoryID = categoryID;
//        this.albumShowURL = albumShowURL;
//    }

    public static List<Album> getCarFmAlbums() {
        return mCarFmAlbums;
    }

    public static void setCarFmAlbums(List<Album> mCarFmAlbums) {
        PlayInfoManager.mCarFmAlbums = mCarFmAlbums;
    }


    //下一个分时段的主题（即当前分时段主题）
    RespCarFmCurTops nextCarFmTime;

    /**
     * 设置即将播放的当前时段
     *
     * @param data
     */
    public void setNextCarFmTime(RespCarFmCurTops data) {
//        Album album = new Album();
//        album.setId(data.getAlbum_id());
//        ArrayList<Long> categorys = new ArrayList<Long>();
//        categorys.add(data.getCategory_id());
//        album.setArrCategoryIds(categorys);
//        List<Album> carFmAlbums = PlayInfoManager.getCarFmAlbums();
//        if (CollectionUtils.isNotEmpty(carFmAlbums)) {
//            int i = carFmAlbums.indexOf(album);
//            if (i >= 0) {
//                nextCarFmTime = carFmAlbums.get(i);
//            }
//        }
        nextCarFmTime = data;
        Logger.d(TAG, "setting next car fmTime:" + (data == null ? 0 : data.toString()));
    }

    public RespCarFmCurTops getNextCarFmTime() {
        return nextCarFmTime;
    }

    public boolean isPause() {
        int state = PlayEngineFactory.getEngine().getState();
        if (state == PlayerInfo.PLAYER_STATUS_PAUSE || state == PlayerInfo.PLAYER_STATUS_RELEASE) {
            return true;
        }
        return false;
    }
}
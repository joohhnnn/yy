package com.txznet.music.playerModule.logic;

import android.support.annotation.NonNull;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.logic.net.response.ResponseAlbumAudio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.favor.bean.FavourBean;
import com.txznet.music.net.RequestAudioCallBack;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.utils.AudioUtils;

import java.util.List;
import java.util.Observable;

/**
 * 播放列表的P层
 */
public class PlayListPresent {


    private static final String TAG = "music:playlist:";
    private int mRetryCount = 0;


    //##创建一个单例类##
    private volatile static PlayListPresent singleton;

    private PlayListPresent() {
    }

    public static PlayListPresent getInstance() {
        if (singleton == null) {
            synchronized (PlayListPresent.class) {
                if (singleton == null) {
                    singleton = new PlayListPresent();
                }
            }
        }
        return singleton;
    }

    public Audio getNextReqAudio() {
        Audio audio = null;
        if (!PlayInfoManager.getInstance().isPlayListEmpty()) {
            //对这个要做处理,audio应该是上一次拉回来的列表的最后一个,而不是当前列表的最后一个
            if (PlayInfoManager.getInstance().getRequestMoreAudio() != null) {
                audio = PlayInfoManager.getInstance().getRequestMoreAudio();
            } else {
                audio = PlayInfoManager.getInstance().getPlayListAudio(PlayInfoManager.getInstance().getPlayListSize() - 1);
            }
        }
        return audio;
    }

    public Audio getLastReqAudio() {
        Audio audio = null;
        if (!PlayInfoManager.getInstance().isPlayListEmpty()) {
            audio = PlayInfoManager.getInstance().getPlayListAudio(0);
        }
        return audio;
    }


    /**
     * 拉取之后的数据
     */
    public void switchMoreAudios(EnumState.Operation operation, Audio audio, @NonNull final ISwitchAudioListener listener) {
        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
        if (PlayInfoManager.getInstance().getCurrentScene() == PlayInfoManager.DATA_FAVOUR) {
            //收藏
            FavorHelper.getFavourData(audio.getSid(), audio.getId(), audio.getOperTime(), Constant.PAGECOUNT, new FavorHelper.SendFavourListener<FavourBean>() {
                @Override
                public void onResponse(List<FavourBean> list) {
                    if (null == list || list.isEmpty()) {
                        listener.onAudioUnavailable(ISwitchAudioListener.ERROR_EMPTY_DATA, null);
                        return;
                    }
                    List<Audio> audios = AudioUtils.fromFavourBeans(list);
                    PlayEngineFactory.getEngine().addAudios(operation, audios, true);
                    PlayInfoManager.getInstance().setRequestMoreAudio(audios.get(audios.size() - 1));
                    listener.onAudioReady(audios.get(0));
                }

                @Override
                public void onError() {
                    listener.onAudioUnavailable(new Error(Error.ERROR_CLIENT_NET_TIMEOUT).getErrorCode(), null);
                }
            });
            return;
        }
        if (null == currentAlbum) {
            listener.onAudioUnavailable(ISwitchAudioListener.ERROR_NULL_ALBUM, null);
            return;
        }
        AlbumEngine.getInstance().requestMoreAudio(operation, currentAlbum,
                audio, currentAlbum.getCategoryId(), true,
                new RequestAudioCallBack(currentAlbum) {
                    @Override
                    public void onResponse(List<Audio> audios, Album album, ResponseAlbumAudio responseAlbumAudio) {
                        mRetryCount = 0;
                        if (null == audios || audios.isEmpty()) {
                            listener.onAudioUnavailable(ISwitchAudioListener.ERROR_EMPTY_DATA, null);
                            return;
                        }
                        LogUtil.d(TAG + "switch more audios success " + audios.size());
                        PlayEngineFactory.getEngine().addAudios(operation, audios, true);
                        PlayInfoManager.getInstance().setRequestMoreAudio(audios.get(audios.size() - 1));
                        listener.onAudioReady(audios.get(0));
                    }

                    @Override
                    public void onError(String cmd, Error error) {
                        super.onError(cmd, error);
                        LogUtil.e(TAG + "switch more audios failed " + error.getErrorCode());
                        if (error.getErrorCode() == Error.ERROR_CLIENT_NET_TIMEOUT) {
                            mRetryCount++;
                            if (mRetryCount > 2) {
                                LogUtil.d(TAG, "request timeout, retry " + mRetryCount);
                                switchMoreAudios(operation, audio, listener);
                                return;
                            }
                        }
                        listener.onAudioUnavailable(error.getErrorCode(), null);
                    }
                });
    }

    /**
     * 拉取之前的数据
     */
    public void getLasterAudios(EnumState.Operation operation, Audio audio, @NonNull final ISwitchAudioListener listener) {
        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();

        if (null == currentAlbum) {
            listener.onAudioUnavailable(ISwitchAudioListener.ERROR_NULL_ALBUM, null);
            return;
        }
        AlbumEngine.getInstance().requestMoreAudio(operation, currentAlbum,
                audio, currentAlbum.getCategoryId(), false,
                new RequestAudioCallBack(currentAlbum) {
                    @Override
                    public void onResponse(List<Audio> audios, Album album, ResponseAlbumAudio responseAlbumAudio) {
                        mRetryCount = 0;
                        if (null == audios || audios.isEmpty()) {
                            listener.onAudioUnavailable(ISwitchAudioListener.ERROR_EMPTY_DATA, null);
                            return;
                        }
                        LogUtil.d(TAG + "switch more audios success " + audios.size());
                        PlayEngineFactory.getEngine().addAudios(operation, audios, false);
                        listener.onAudioReady(audios.get(audios.size() - 1));
                    }

                    @Override
                    public void onError(String cmd, Error error) {
                        super.onError(cmd, error);
                        LogUtil.e(TAG + "switch more audios failed " + error.getErrorCode());
                        if (error.getErrorCode() == Error.ERROR_CLIENT_NET_TIMEOUT) {
                            mRetryCount++;
                            if (mRetryCount > 2) {
                                LogUtil.d(TAG, "request timeout, retry " + mRetryCount);
                                getLasterAudios(operation, audio, listener);
                                return;
                            }
                        }
                        listener.onAudioUnavailable(error.getErrorCode(), null);
                    }
                });
    }


}

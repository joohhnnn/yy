package com.txznet.music.playerModule;

import com.txznet.comm.remote.util.Logger;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.bean.BreakpointAudio;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.dao.DaoManager;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.playerModule.logic.IPlayListChangedListener;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by brainBear on 2017/12/21.
 */

public class PlayListItemDataSource implements IPlayListChangedListener, Observer {

    private static final String TAG = "PlayListItemDataSource:";
    private static PlayListItemDataSource sInstance;
    private List<PlayListItem> mPlayListItems;
    private List<PlayListItemChangedListener> playListItemChangedListeners = new ArrayList<>();
    private Audio mCurrentAudio;

    private PlayListItemDataSource() {

        PlayInfoManager.getInstance().addPlayListChangedListener(this);
        ObserverManage.getObserver().addObserver(this);

    }

    public static PlayListItemDataSource getInstance() {
        if (null == sInstance) {
            synchronized (PlayListItemDataSource.class) {
                if (null == sInstance) {
                    sInstance = new PlayListItemDataSource();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void onPlayListChanged(List<Audio> audios) {
        List<Audio> newAudios = new ArrayList<>();
        newAudios.addAll(audios);
        io.reactivex.Observable.just(newAudios)
                .map(new Function<List<Audio>, List<PlayListItem>>() {
                    @Override
                    public List<PlayListItem> apply(List<Audio> audios) throws Exception {
                        List<PlayListItem> playListItems = new ArrayList<>();

                        List<BreakpointAudio> breakpointAudios = DaoManager.getInstance().findBreakpointAudios(audios);
                        for (int i = 0; i < audios.size(); i++) {
                            Audio audio = audios.get(i);
                            PlayListItem playListItem = new PlayListItem();
                            playListItem.setAudio(audio);

                            if (Utils.isSong(audio.getSid())) {
                                playListItem.setFavorEnable(true);
                                playListItem.setShowProgress(false);

                                if (FavorHelper.isSupportFavour(audio)) {
                                    playListItem.setShowFavor(true);
                                    playListItem.setFavor(FavorHelper.isFavour(audio));
                                }
                            } else {
                                playListItem.setShowFavor(false);

                                BreakpointAudio breakpointAudio = breakpointAudios.get(i);
                                if (null != breakpointAudio) {
                                    long duration = breakpointAudio.getDuration();
                                    int breakpoint = breakpointAudio.getBreakpoint();

                                    if (duration != 0 && breakpoint != 0) {
                                        playListItem.setShowProgress(true);
                                        playListItem.setProgress((float) breakpoint / duration);
                                    } else if (breakpointAudio.getPlayEndCount() > 0) {
                                        playListItem.setShowProgress(true);
                                        playListItem.setProgress(1f);
                                    } else {
                                        playListItem.setShowProgress(false);
                                    }
                                } else {
                                    playListItem.setShowProgress(false);
                                }
                            }
                            playListItems.add(playListItem);
                        }
                        return playListItems;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PlayListItem>>() {
                    @Override
                    public void accept(List<PlayListItem> playListItems) throws Exception {
                        mPlayListItems = playListItems;
                        notifyPlayListItemChanged();
                        updateCurrentAudio(PlayInfoManager.getInstance().getCurrentAudio());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.e(TAG, "play list changed:" + throwable.toString());
                    }
                });
    }

    private void notifyPlayListItemChanged() {
        for (PlayListItemChangedListener listener : playListItemChangedListeners) {
            listener.onPlayListItemChanged(mPlayListItems);
        }
    }


    private void notifyPlayItemChanged(int pos) {
        for (PlayListItemChangedListener listener : playListItemChangedListeners) {
            listener.onPlayItemChanged(pos);
        }
    }

    public void addOnPlayListItemChangedListener(PlayListItemChangedListener listener) {
        if (null == listener) {
            return;
        }
//        if (!playListItemChangedListeners.contains(listener)) {
//            playListItemChangedListeners.add(listener);
//
//            listener.onPlayListItemChanged(mPlayListItems);
//        }
        io.reactivex.Observable.just(listener)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PlayListItemChangedListener>() {
                    @Override
                    public void accept(PlayListItemChangedListener listener) throws Exception {
                        if (!playListItemChangedListeners.contains(listener)) {
                            playListItemChangedListeners.add(listener);

                            listener.onPlayListItemChanged(mPlayListItems);
                        }
                    }
                });
    }

    public void removeOnPlayListItemChangedListener(PlayListItemChangedListener listener) {
        if (null == listener) {
            return;
        }
//        playListItemChangedListeners.remove(listener);
        io.reactivex.Observable.just(listener)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PlayListItemChangedListener>() {
                    @Override
                    public void accept(PlayListItemChangedListener listener) throws Exception {
                        playListItemChangedListeners.remove(listener);
                    }
                });
    }

    private int checkFavorChanged(long id, long sid, boolean isFavor) {
        int result = -1;
        for (int i = 0; i < mPlayListItems.size(); i++) {
            PlayListItem playListItem = mPlayListItems.get(i);
            if (playListItem.getAudio().getId() == id && playListItem.getAudio().getSid() == sid) {
                playListItem.setFavor(isFavor);
                result = i;
                break;
            }
        }
        return result;
    }

    @Override
    public void update(Observable o, Object arg) {
        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        if (arg instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) arg;
            switch (info.getType()) {
                case InfoMessage.PLAYER_CURRENT_AUDIO:
                    updateCurrentAudio(currentAudio);
                    break;
                case InfoMessage.FAVOUR_MUSIC:
                    if (info.getObj() instanceof Audio) {
                        Audio bean = (Audio) info.getObj();
                        int i = checkFavorChanged(bean.getId(), bean.getSid(), true);
                        if (i >= 0) {
                            notifyPlayItemChanged(i);
                        }
                    }

                    break;
                case InfoMessage.UNFAVOUR_MUSIC:
                    if (info.getObj() instanceof Audio) {
                        Audio bean = (Audio) info.getObj();
                        int i = checkFavorChanged(bean.getId(), bean.getSid(), true);
                        if (i >= 0) {
                            notifyPlayItemChanged(i);
                        }
                    }
                    break;
            }
        }
    }

    private void updateCurrentAudio(Audio currentAudio) {
        if (currentAudio == null) {
            return;
        }
        io.reactivex.Observable.just(currentAudio)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<Audio>() {
                    @Override
                    public void accept(Audio audio) throws Exception {
                        if (null != mCurrentAudio && !Utils.isSong(mCurrentAudio.getSid())) {
                            for (PlayListItem item : mPlayListItems) {
                                if (item.getAudio().getId() == mCurrentAudio.getId() && item.getAudio().getSid() == mCurrentAudio.getSid()) {
                                    BreakpointAudio breakpointAudio = DBManager.getInstance().findBreakpoint(mCurrentAudio);
                                    if (null != breakpointAudio) {
                                        long duration = breakpointAudio.getDuration();
                                        int breakpoint = breakpointAudio.getBreakpoint();

                                        if (duration != 0 && breakpoint != 0) {
                                            item.setShowProgress(true);
                                            item.setProgress((float) breakpoint / duration);
                                        } else if (breakpointAudio.getPlayEndCount() > 0) {
                                            item.setShowProgress(true);
                                            item.setProgress(1f);
                                        } else {
                                            item.setShowProgress(false);
                                        }
                                    } else {
                                        item.setShowProgress(false);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Audio>() {
                    @Override
                    public void accept(Audio audio) throws Exception {
                        for (PlayListItem playListItem : mPlayListItems) {
                            if (playListItem.getAudio().getId() == audio.getId() && playListItem.getAudio().getSid() == audio.getSid()) {
                                playListItem.setStyle(PlayListItem.STYLE_HIGHLINGHT);
                            } else if (playListItem.isShowProgress()) {
                                playListItem.setStyle(PlayListItem.STYLE_GREY);
                            } else {
                                playListItem.setStyle(PlayListItem.STYLE_NORMAL);
                            }
                        }
                        mCurrentAudio = audio;
                        notifyPlayListItemChanged();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.e(TAG, "updateCurrentAudio:" + throwable.toString());
                    }
                });
    }

    public interface PlayListItemChangedListener {
        void onPlayListItemChanged(List<PlayListItem> playListItems);


        void onPlayItemChanged(int pos);
    }
}

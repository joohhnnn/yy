package com.txznet.music.model;

import com.txznet.comm.err.Error;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.HistoryAlbumDao;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.HistoryAlbum;
import com.txznet.music.data.entity.HistoryAudio;
import com.txznet.music.helper.AlbumConverts;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.util.AlbumUtils;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.Logger;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxWorkflow;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 历史的业务处理
 *
 * @author telen
 * @date 2018/12/3,11:39
 */
public class HistoryModel extends RxWorkflow {

    public HistoryModel() {
    }

    @Override
    public void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_GET_DEL_ITEM_HISTORY_MUSIC:
                sendDeleteHistoryMusic(action, (List<HistoryAudio>) action.data.get(Constant.HistoryConstant.KEY_HISTORY_MUSIC_AUDIOS));
                break;
            case ActionType.ACTION_MEDIA_SCANNER_STARTED:
            case ActionType.ACTION_GET_HISTORY_MUSIC:
                sendGetHistoryMusic(RxAction.type(ActionType.ACTION_GET_HISTORY_MUSIC).build());
                break;
            case ActionType.ACTION_GET_DEL_ITEM_HISTORY_ALBUM:
                sendDeleteHistoryAlbum(action, (List<HistoryAlbum>) action.data.get(Constant.HistoryConstant.KEY_HISTORY_ALBUMS_DELETE));
                break;
            case ActionType.ACTION_GET_HISTORY_ALBUM:
                sendGetHistoryAlbum(action);
                break;
            case ActionType.ACTION_PLAYER_ON_INFO_CHANGE:
                //保存历史
                //
                AudioV5 audioV5 = (AudioV5) action.data.get(Constant.PlayConstant.KEY_AUDIO);
                if (audioV5 == null) {
                    return;
                }

                //判断当前播放的是音乐还是电台,从而分别进行存储
                if (AudioUtils.isSong(audioV5.sid)) {
                    saveHistoryMusic(audioV5);
                } else {
                    Album album = PlayHelper.get().getCurrAlbum();
                    // AI电台特殊处理
                    if (AlbumUtils.isAiRadio(album)) {
                        album = new Album();
                        album.sid = audioV5.albumSid;
                        album.id = audioV5.albumId;
                        album.name = audioV5.albumName;
                        album.logo = audioV5.logo;
                    }
                    if (audioV5.albumId == album.id) {
                        saveHistoryAlbum(album, audioV5);
                    }
                }
                break;

            default:
                break;
        }
    }

    private void saveHistoryMusic(AudioV5 audioV5) {
        if (audioV5 == null) {
            return;
        }

        Disposable disposable = Observable.create(emitter -> {
            //转换
            HistoryAudio historyAudio = AudioConverts.convert2HistoryAudio(audioV5);
            //保存数据库
            DBUtils.getDatabase(GlobalContext.get()).getHistoryAudioDao().saveOrUpdate(historyAudio);
            emitter.onNext(historyAudio);
            emitter.onComplete();
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(

                o -> {
                    postRxData(RxAction.type(ActionType.ACTION_GET_ADD_ITEM_HISTORY_MUSIC).bundle(Constant.HistoryConstant.KEY_HISTORY_MUSIC_AUDIO, o).build());
                }, throwable -> {
                    if (BuildConfig.DEBUG) {
                        Logger.d(Constant.LOG_TAG_LOGIC, getClass().getSimpleName() + ",saveHistoryMusic");
                    }
                }

        );

    }

    private void saveHistoryAlbum(Album album, AudioV5 audio) {
        Logger.d(Constant.LOG_TAG_LOGIC, "saveHistoryAlbum album=" + album + ", audio=" + audio);
        if (album == null) {
            return;
        }
        //转换
        Disposable disposable = Observable.create((ObservableOnSubscribe<HistoryAlbum>) emitter -> {
            //保存数据库
            HistoryAlbumDao historyAlbumDao = DBUtils.getDatabase(GlobalContext.get()).getHistoryAlbumDao();
            HistoryAlbum historyAlbum = historyAlbumDao.findByAlbum(album.sid, album.id);
            if (historyAlbum == null) {
                historyAlbum = AlbumConverts.convert2HistoryAlbum(album);
            }
            historyAlbum.flag = HistoryAlbum.FLAG_NORMAL;
            historyAlbum.audio = audio;
            DBUtils.getDatabase(GlobalContext.get()).getHistoryAlbumDao().saveOrUpdate(historyAlbum);
            emitter.onNext(historyAlbum);
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(o -> {
            postRxData(RxAction.type(ActionType.ACTION_GET_ADD_ITEM_HISTORY_ALBUM).bundle(Constant.HistoryConstant.KEY_HISTORY_ALBUM, o).build());
        }, throwable -> {
            if (BuildConfig.DEBUG) {
                Logger.d(Constant.LOG_TAG_LOGIC, getClass().getSimpleName() + ",saveHistoryMusic");
            }
        });
    }

    private void sendGetHistoryMusic(RxAction action) {
        //从数据库中获取
        Disposable subscribe = Observable.create(emmit -> {

            List<HistoryAudio> historyAudios = DBUtils.getDatabase(GlobalContext.get()).getHistoryAudioDao().listAll();
            // FIXME: 2018/12/11 room自带排序效果,按照插入更新顺序,因为不知道怎么修改顺序规则,所以这里,将他的结果进行一层倒序.
            Collections.reverse(historyAudios);
            AudioUtils.removeLocalNotExists(historyAudios);
            emmit.onNext(historyAudios);
            emmit.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(value -> {
            action.data.put(Constant.HistoryConstant.KEY_HISTORY_MUSIC_AUDIOS, value);
            postRxData(action);


        }, e -> postRxError(action, new Error(ErrCode.ERROR_INNER_WRONG)));

        addRxAction(action, subscribe);
    }

    private void sendDeleteHistoryMusic(RxAction action, List<HistoryAudio> historyAuidos) {

        //从数据库中获取
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Integer>) emmit -> {
            emmit.onNext(DBUtils.getDatabase(GlobalContext.get()).getHistoryAudioDao().delete(historyAuidos));
            emmit.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(value -> {
//            if (value == 1) {
            action.data.put(Constant.HistoryConstant.KEY_HISTORY_MUSICS_DELETE, historyAuidos);
            postRxData(action);
//            } else {
//                postRxError(action, new Error(ErrCode.ERROR_OPERATION_WRONG));
//            }

        }, e -> postRxError(action, new Error(ErrCode.ERROR_INNER_WRONG)));
        addRxAction(action, subscribe);
    }

    private void sendDeleteHistoryAlbum(RxAction action, List<HistoryAlbum> historyAlbums) {

        //从数据库中获取
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Integer>) emmit -> {
            if (historyAlbums != null) {
                for (HistoryAlbum historyAlbum : historyAlbums) {
                    historyAlbum.flag = HistoryAlbum.FLAG_HIDDEN;
                }
                DBUtils.getDatabase(GlobalContext.get()).getHistoryAlbumDao().saveOrUpdate(historyAlbums);
            }
            emmit.onNext(1);
            emmit.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(value -> {
//            if (value == 1) {
            action.data.put(Constant.HistoryConstant.KEY_HISTORY_ALBUMS_DELETE, historyAlbums);
            postRxData(action);
//            } else {
//                postRxError(action, new Error(ErrCode.ERROR_OPERATION_WRONG));
//            }

        }, e -> postRxError(action, new Error(ErrCode.ERROR_INNER_WRONG)));
        addRxAction(action, subscribe);
    }

    private void sendGetHistoryAlbum(RxAction action) {
        //从数据库中获取
        Disposable subscribe = Observable.create(emmit -> {
            List<HistoryAlbum> historyAlbumList = DBUtils.getDatabase(GlobalContext.get()).getHistoryAlbumDao().listAllNotHidden();
            // FIXME: 2019/2/1 room自带排序效果,按照插入更新顺序,因为不知道怎么修改顺序规则,所以这里,将他的结果进行一层倒序.
            Collections.reverse(historyAlbumList);

            emmit.onNext(historyAlbumList);
            emmit.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(value -> {
            action.data.put(Constant.HistoryConstant.KEY_HISTORY_MUSIC_AUDIOS, value);
            postRxData(action);


        }, e -> postRxError(action, new Error(ErrCode.ERROR_INNER_WRONG)));

        addRxAction(action, subscribe);
    }
}

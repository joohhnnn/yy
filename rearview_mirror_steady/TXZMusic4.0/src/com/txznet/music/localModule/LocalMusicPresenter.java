package com.txznet.music.localModule;

import android.util.Log;

import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.PlayerBizLogic;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.PlayerCommunicationManager;
import com.txznet.music.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * Created by brainBear on 2018/1/10.
 */

public class LocalMusicPresenter implements LocalContract.Presenter, Observer {


    private static final String TAG = "LocalMusicPresenter";
    private LocalContract.View mView;
    private LocalContract.DataSource mDataSource;
    private LocalContract.ILocalScanListener mScanListener = new LocalContract.ILocalScanListener() {
        @Override
        public void onScanCount(int count) {
//            mView.showScanCount(count);
        }

        @Override
        public void onScanFinish(List<Audio> audios) {
            ToastUtils.showShortOnUI("已扫描到" + audios.size() + "首歌曲");
            mView.dismissScanning();
            if (audios.isEmpty()) {
                mView.showEmpty();
            } else {
                mView.showLocalData(audios);
            }

            //如果有新增的歌曲则需要添加进播单
            PlayerBizLogic.getInstance().updatePlayerList(audios, PlayInfoManager.DATA_LOCAL);

            ReportEvent.clickScanEnd(audios.size());

            PlayerCommunicationManager.getInstance().sendScanFinish();
        }
    };


    public LocalMusicPresenter(LocalContract.View view) {
        mView = view;
        mDataSource = LocalAudioDataSource.getInstance();
    }

    @Override
    public void register() {
        ObserverManage.getObserver().addObserver(this);


        getLocalAudio();
//        List<Audio> cache = mDataSource.getCache();
//        if (cache == null || cache.isEmpty()) {
//            startScan();
//        } else {
//            mView.showLocalData(cache);
//        }
    }

    @Override
    public void unregister() {
        ObserverManage.getObserver().deleteObserver(this);
        mView = null;
    }

    @Override
    public boolean isScanning() {
        return mDataSource.isScanning();
    }

    @Override
    public void startScan() {
        mView.showScanning();
        mDataSource.scanLocal(mScanListener);
        ReportEvent.clickScanBegin(mDataSource.getCache().size());
    }

    @Override
    public void stopScan() {
        mView.dismissScanning();
        mView.showLocalData(mDataSource.getCache());
        ReportEvent.clickScanInterrupt(mDataSource.getCache().size());
    }

    @Override
    public void favor(Audio audio) {
        ReportEvent.clickLocalFavour(audio, audio.getName());
        FavorHelper.favor(audio, EnumState.Operation.manual);
    }

    @Override
    public void unFavor(Audio audio) {
        ReportEvent.clickLocalUnFavour(audio, audio.getName());
        FavorHelper.unfavor(audio, EnumState.Operation.manual);
    }

    /**
     * 删除本地音乐
     *
     * @param audios
     */
    @Override
    public void deleteLocalAudio(List<Audio> audios, boolean isNeedDeleteFinalFile) {
        mView.showLoadingView(true);
        Log.e(TAG, "deleteLocalAudio: " + audios.size());
        Log.e("test:::delete", "deleteLocalAudio: " + audios.size());
        checkItems(audios, false);
        if (audios.size() == mDataSource.getCache().size()) {
            mView.hideCheckAllBtn(true);
        }
        mDataSource.deleteAudios(audios, isNeedDeleteFinalFile, new LocalContract.IDeleteListener() {
            @Override
            public void onSuccess(List<Audio> audios) {
                Logger.d("test:::delete", "onSuccess");
                PlayerBizLogic.getInstance().deleteAudiosFromPlayList(audios, PlayInfoManager.DATA_LOCAL);
                mView.showLoadingView(false);
                mView.deleteAudios(audios, true);
            }

            @Override
            public void onError(List<Audio> audios) {
                Logger.d("test:::delete", "onError");
                //有些歌曲删除不掉
                mView.showLoadingView(false);
                mView.deleteAudios(audios, false);
            }
        });

    }

//
//    @Override
//    public void deleteLocalAudio(final List<Audio> audios, final int position) {
//        if (position < 0 || position >= audios.size()) {
//            Logger.e(TAG, "deleteLocalAudio index error");
//            return;
//        }
//
//        final Audio audio = audios.get(position);
//
//        PlayerBizLogic.getInstance().deleteAudioFromPlayList(audio, PlayInfoManager.DATA_LOCAL);
//        DBManager.getInstance().removeLocalAudios(audio);
//        audios.remove(audio);
//        mView.refreshItem(-1);
//        if (audios.isEmpty()) {
//            mView.showEmpty();
//        }
//        ReportEvent.clickLocalDelete(audio.getSid(), audio.getId(), audio.getName());
//
//
//    }

    @Override
    public void play(List<Audio> audios, int position) {
        if (position < 0 || position >= audios.size()) {
            Logger.e(TAG, "play index error");
            return;
        }

        Audio audio = audios.get(position);
        if (mDataSource.isAvailable(audio)) {
            PlayEngineFactory.getEngine().setAudios(EnumState.Operation.manual, audios, null, position, PlayInfoManager.DATA_LOCAL);
            PlayEngineFactory.getEngine().playOrPause(EnumState.Operation.manual);
            ReportEvent.clickLocalAudio(audio, audio.getName());
        } else {
            Logger.w(TAG, "not exist:" + (audio != null ? audio.toString() : ""));
            TtsUtil.speakResource("RS_VOICE_SPEAKNOTEXIST_TIPS", Constant.RS_VOICE_SPEAKNOTEXIST_TIPS);
            mDataSource.deleteNotAvailableAudios(new LocalContract.ILocalScanListener() {
                @Override
                public void onScanCount(int count) {
                }

                @Override
                public void onScanFinish(List<Audio> audios) {
                    if (audios.isEmpty()) {
                        mView.showEmpty();
                    } else {
                        mView.showLocalData(audios);
                    }
                }
            });
        }


    }


    @Override
    public void checkItem(Audio audio, boolean isCheck) {
        if (isCheck) {
            mDataSource.getCheckIndexs().add(audio);
        } else {
            mDataSource.getCheckIndexs().remove(audio);
        }

        if (mDataSource.getCheckIndexs().size() == mDataSource.getCache().size()) {
            mView.showAllCheckState(true);
        } else {
            mView.showAllCheckState(false);
        }

    }

    @Override
    public void checkItems(List<Audio> audios, boolean isCheck) {
        if (isCheck) {
            mDataSource.getCheckIndexs().addAll(audios);
            mView.showAllCheckState(true);
        } else {
            mDataSource.getCheckIndexs().removeAll(audios);
            mView.showAllCheckState(false);
        }

//        if (mDataSource.getCheckIndexs().size() == mDataSource.getCache().size()) {
//            mView.showAllCheckState(true);
//        } else {
//            mView.showAllCheckState(false);
//        }
    }

    @Override
    public void getLocalAudio() {
        if (isScanning()) {
            mView.showScanning();
            LocalAudioDataSource.getInstance().scanLocal(mScanListener);
        } else {
            List<Audio> cache = LocalAudioDataSource.getInstance().getCache();
            if (cache.isEmpty()) {
                //从数据库读
                List<Audio> audios = new ArrayList<>();
                LocalAudioDataSource.getInstance().findLocalDBDate()
                        .subscribe(v -> {
                            audios.add(v);
                        }, e -> {
                            mView.showEmpty();
                        }, () -> {
                            if (audios.isEmpty()) {
                                mView.showEmpty();
                            } else {
                                mView.showContent();
                                mView.showLocalData(audios);
                            }
                        });
            } else {
                mView.showContent();
                mView.showLocalData(cache);
            }
        }
    }


    @Override
    public void update(Observable o, Object data) {
        if (data instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) data;
            Audio obj = null;
            switch (info.getType()) {
                case InfoMessage.FAVOUR_MUSIC:
                    obj = (Audio) info.getObj();
                    if (mDataSource.getCache().contains(obj)) {
                        mView.refreshItem(mDataSource.getCache().indexOf(obj));
                    }
                    break;
                case InfoMessage.UNFAVOUR_MUSIC:
                    obj = (Audio) info.getObj();
                    if (mDataSource.getCache().contains(obj)) {
                        mView.refreshItem(mDataSource.getCache().indexOf(obj));
                    }
                    break;
                case InfoMessage.PLAYER_CURRENT_AUDIO:
                    mView.refreshItem(-1);
                    break;
                case InfoMessage.REFRESH_NOT_EXIT_AUDIO:
                    //删除不存在的歌曲
                    mDataSource.deleteNotAvailableAudios(new LocalContract.ILocalScanListener() {
                        @Override
                        public void onScanCount(int count) {
                        }

                        @Override
                        public void onScanFinish(List<Audio> audios) {
                            if (audios.isEmpty()) {
                                mView.showEmpty();
                            } else {
                                mView.showLocalData(audios);
                            }
                        }
                    });
                    break;
                case InfoMessage.PLAY_DOWNLOAD_COMPLETE:
                    //下载完成的时候会通知过来
                    Audio audio = JsonHelper.toObject(Audio.class, (String) info.getObj());

                    mDataSource.addAudio(audio).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<Audio>>() {
                        @Override
                        public void accept(List<Audio> audios) throws Exception {
                            mView.showLocalData(audios);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Logger.d(TAG, throwable.getMessage());
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    }
}

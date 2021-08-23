package com.txznet.music.localModule;

import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.utils.FileUtils;
import com.txznet.music.utils.TtsUtilWrapper;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

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
            mView.showScanCount(count);
        }

        @Override
        public void onScanFinish(List<Audio> audios) {
            mView.dismissScanning();
            if (audios.isEmpty()) {
                mView.showEmpty();
            } else {
                mView.showLocalData(audios);
            }
            ReportEvent.clickScanEnd(audios.size());
        }
    };


    public LocalMusicPresenter(LocalContract.View view) {
        mView = view;
        mDataSource = LocalAudioDataSource.getInstance();
    }

    @Override
    public void register() {
        ObserverManage.getObserver().addObserver(this);
        List<Audio> cache = mDataSource.getCache();
        if (cache == null || cache.isEmpty()) {
            startScan();
        } else {
            mView.showLocalData(cache);
        }
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
        ReportEvent.clickLocalFavour(audio.getSid(), audio.getId(), audio.getName());
        FavorHelper.favor(audio,EnumState.Operation.manual);
    }

    @Override
    public void unFavor(Audio audio) {
        ReportEvent.clickLocalUnFavour(audio.getSid(), audio.getId(), audio.getName());
        FavorHelper.unfavor(audio,EnumState.Operation.manual);
    }

    @Override
    public void deleteLocalAudio(List<Audio> audios, int position) {
        if (position < 0 || position >= audios.size()) {
            Logger.e(TAG, "deleteLocalAudio index error");
            return;
        }

        Audio audio = audios.get(position);
        if (FileUtils.delFile(audio.getStrDownloadUrl())) {
            DBManager.getInstance().removeLocalAudios(audio);
            audios.remove(audio);

            mView.refreshItem(-1);

            Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
            if (PlayInfoManager.getInstance().getCurrentScene() == PlayInfoManager.DATA_LOCAL) {
                if (currentAudio != null && currentAudio.getSid() == audio.getSid() && currentAudio.getId() == audio.getId()) {
                    PlayEngineFactory.getEngine().next(EnumState.Operation.error);
                }
                PlayInfoManager.getInstance().removePlayListAudio(audio);
            }

        }

        if (audios.isEmpty()) {
            mView.showEmpty();
        }
        ReportEvent.clickLocalDelete(audio.getSid(), audio.getId(), audio.getName());
    }

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
            ReportEvent.clickLocalAudio(audio.getSid(), audio.getId(), audio.getName());
        } else {
            TtsUtilWrapper.speakResource("RS_VOICE_SPEAKNOTEXIST_TIPS", Constant.RS_VOICE_SPEAKNOTEXIST_TIPS);
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
                default:
                    break;
            }
        }
    }
}

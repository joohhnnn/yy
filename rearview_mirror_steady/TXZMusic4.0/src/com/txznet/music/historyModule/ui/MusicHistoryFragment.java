//package com.txznet.music.historyModule.ui;
//
//import android.view.View;
//import android.widget.AdapterView;
//
//import com.txznet.comm.remote.util.LogUtil;
//import com.txznet.music.albumModule.bean.Audio;
//import com.txznet.music.baseModule.Constant;
//import com.txznet.music.baseModule.bean.EnumState;
//import com.txznet.music.baseModule.dao.DBManager;
//import com.txznet.music.historyModule.logic.HistoryEngine;
//import com.txznet.music.localModule.logic.LocalMusicEngine;
//import com.txznet.music.playerModule.logic.PlayInfoManager;
//import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
//import com.txznet.music.report.ReportEvent;
//import com.txznet.music.utils.SharedPreferencesUtils;
//import com.txznet.music.utils.Utils;
//
//import java.util.List;
//
//public class MusicHistoryFragment extends BaseHistoryFragment {
//
//
//    @Override
//    public void onItemClickHandle(AdapterView<?> parent, View view, int position, long id) {
//        if (mAudios == null || mAudios.size() <= position) {
//            setDataChange();
//            return;
//        }
//        Audio audio = mAudios.get(position);
//        LogUtil.logd(TAG + "[" + getFragmentId() + "]onItemClick:" + audio.getName() + "(" + position + ")");
//        if (LocalMusicEngine.getInstance().isValid(audio)) {
//            PlayEngineFactory.getEngine().setAudios(EnumState.Operation.manual, mAudios,
//                    null, position, PlayInfoManager.DATA_HISTORY);
////			PlayEngineFactory.getEngine().release(EnumState.OperState.manual);
//            SharedPreferencesUtils.setAudioSource(Constant.HISTORY_TYPE);
//            PlayEngineFactory.getEngine().playOrPause(EnumState.Operation.manual);
//        } else {
//            DBManager.getInstance().removeHistoryAudio(audio);
//            mAudios.remove(position);
//            mAdapter.notifyDataSetChanged();
//        }
//
//        ReportEvent.clickHistoryMusicPlay(audio.getSid(), audio.getId(), audio.getName());
//    }
//
//    @Override
//    public List<Audio> getHistories() {
//        return HistoryEngine.getInstance().getMusicHistory();
//    }
//
//    @Override
//    public void refreshPlayPosition(Audio currentAudio) {
//        if (currentAudio != null && mAudios != null && Utils.isSong(currentAudio.getSid())) {
//            if (mAudios.contains(currentAudio)) {
//                mAudios.remove(currentAudio);
//            }
//            mAudios.add(0, currentAudio);
//            setDataChange();
//        }
//    }
//
//    @Override
//    public void reqData() {
//        HistoryEngine.getInstance().queryMusicHistory();
//    }
//
//    @Override
//    public String getFragmentId() {
//        return "MusicHistoryFragment#" + this.hashCode() + "/历史音乐";
//    }
//
//}

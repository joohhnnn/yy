//package com.txznet.music.historyModule.ui;
//
//import android.view.View;
//import android.widget.AdapterView;
//
//import com.txznet.comm.remote.util.LogUtil;
//import com.txznet.music.albumModule.bean.Album;
//import com.txznet.music.albumModule.bean.Audio;
//import com.txznet.music.albumModule.logic.AlbumEngine;
//import com.txznet.music.baseModule.Constant;
//import com.txznet.music.baseModule.bean.EnumState.Operation;
//import com.txznet.music.historyModule.logic.HistoryEngine;
//import com.txznet.music.net.NetManager;
//import com.txznet.music.report.ReportEvent;
//import com.txznet.music.utils.StringUtils;
//import com.txznet.music.utils.ToastUtils;
//import com.txznet.music.utils.Utils;
//
//import java.util.List;
//
//public class RadioHistoryFragment extends BaseHistoryFragment {
//
//    private static final String TAG = "Music:RadioHistory:";
//
//    @Override
//    public void onItemClickHandle(AdapterView<?> parent, View view, int position, long id) {
//        if (!NetManager.isNetworkConnected()) {
//            ToastUtils.showShort(Constant.RS_VOICE_SPEAK_NONE_NET);
//            return;
//        }
//
//        if (position < 0 || position >= mAudios.size()) {
//            LogUtil.e(TAG, "onItemClickHandle invalid index " + position + " size:" + mAudios.size());
//            setDataChange();
//            return;
//        }
//        Audio audio = mAudios.get(position);
//        LogUtil.logd(TAG + "[" + getFragmentId() + "]onItemClick:" + audio.getName() + "(" + position + ")");
//
//        Album album = new Album();
//        album.setId(Long.parseLong(audio.getAlbumId()));
//        album.setSid(audio.getSid());
//        album.setName(audio.getAlbumName());
//
//        int categoryId;
//        if (audio.getStrCategoryId() == null) {
//            categoryId = 0;
//        } else if (StringUtils.isNumeric(audio.getStrCategoryId())) {
//            categoryId = Integer.parseInt(audio.getStrCategoryId());
//        } else {
//            categoryId = Integer.parseInt(StringUtils.split(audio.getStrCategoryId(), ",")[0]);
//        }
//        album.setCategoryId(categoryId);
//        album.setLogo(audio.getLogo());
//
//        AlbumEngine.getInstance().playAlbumWithAudio(Operation.manual, album, audio, false);
//
//        ReportEvent.clickHistoryRadioPlay(album.getSid(), album.getId(), album.getName());
//    }
//
//    @Override
//    public List<Audio> getHistories() {
//        return HistoryEngine.getInstance().getRadioHistory();
//    }
//
//    @Override
//    public void refreshPlayPosition(Audio currentAudio) {
//        if (currentAudio != null && mAudios != null && !Utils.isSong(currentAudio.getSid())) {
//            if (currentAudio != null) {
//                for (int i = 0; i < mAudios.size(); i++) {
//                    if (currentAudio.getAlbumId().equals(mAudios.get(i).getAlbumId())) {
//                        mAudios.remove(i);
//                        break;
//                    }
//                }
//            }
//            mAudios.add(0, currentAudio);
//            setDataChange();
//        }
//    }
//
//    @Override
//    public void reqData() {
//        HistoryEngine.getInstance().queryRadioHistory();
//    }
//
//
//    @Override
//    public String getFragmentId() {
//        return "RadioHistoryFragment#" + this.hashCode() + "/历史电台";
//    }
//}

//package com.txznet.music.historyModule.logic;
//
//import com.txznet.fm.bean.InfoMessage;
//import com.txznet.fm.manager.ObserverManage;
//import com.txznet.loader.AppLogic;
//import com.txznet.music.albumModule.bean.Audio;
//import com.txznet.music.baseModule.Constant;
//import com.txznet.music.baseModule.dao.DBManager;
//import com.txznet.music.ui.bean.PlayConf;
//import com.txznet.music.utils.Utils;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by ASUS User on 2016/11/8.
// */
//public class HistoryEngine {
//
//    private static final String TAG = "Music:HistoryEngine:";
//    private static HistoryEngine mInstance;
//    private List<Audio> mMusicList;
//    private List<Audio> mRadioList;
//    private List<Audio> mAllHistoryList;
//
//
//    private HistoryEngine() {
//        mMusicList = new ArrayList<>();
//        mRadioList = new ArrayList<>();
//        mAllHistoryList = new ArrayList<>();
//    }
//
//    public static HistoryEngine getInstance() {
//        if (mInstance == null) {
//            synchronized (HistoryEngine.class) {
//                if (mInstance == null) {
//                    mInstance = new HistoryEngine();
//                }
//            }
//        }
//        return mInstance;
//    }
//
//    public List<Audio> getmAllHistoryList() {
//        return mAllHistoryList;
//    }
//
//    public void queryMusicHistory() {
//        AppLogic.runOnBackGround(new Runnable() {
//            @Override
//            public void run() {
//                List<Audio> historyAudioBySid = DBManager.getInstance().findHistoryAudioBySid(Utils.getSongSid());
//                mMusicList.clear();
//                mMusicList.addAll(historyAudioBySid);
//                sendQueryFinished();
//            }
//        }, 0);
//    }
//
//    private void sendQueryFinished() {
//        ObserverManage.getObserver().send(InfoMessage.REFRESH_HISTORY_MUSIC_LIST);
//    }
//
//
//    /**
//     * @return
//     */
//    public List<Audio> getRadioHistorySync() {
//        List<Integer> sidType = Utils.getSidByType(PlayConf.NEWS_TYPE);
//        sidType.addAll(Utils.getSidByType(PlayConf.RADIO_TYPE));
//        List<Audio> historyAudioBySid = DBManager.getInstance().findHistoryAudioBySid(sidType);
//        return historyAudioBySid;
//    }
//
//    public void queryRadioHistory() {
//        AppLogic.runOnBackGround(new Runnable() {
//            @Override
//            public void run() {
//                List<Integer> sidType = Utils.getSidByType(PlayConf.NEWS_TYPE);
//                sidType.addAll(Utils.getSidByType(PlayConf.RADIO_TYPE));
//                List<Audio> historyAudioBySid = DBManager.getInstance().findHistoryAudioBySid(sidType);
//                mRadioList.clear();
//                mRadioList.addAll(historyAudioBySid);
//                sendQueryFinished();
//            }
//        }, 0);
//
//    }
//
//    public List<Audio> getMusicHistory() {
//        return mMusicList;
//
//    }
//
//    public List<Audio> getRadioHistory() {
//        return mRadioList;
//    }
//
//
//}

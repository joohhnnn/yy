//package com.txznet.music.report;
//
//import android.os.SystemClock;
//
//import com.txz.report_manager.ReportManager;
//import com.txznet.comm.remote.util.LogUtil;
//import com.txznet.comm.remote.util.ReportUtil;
//import com.txznet.music.albumModule.bean.Audio;
//import com.txznet.music.baseModule.Constant;
//import com.txznet.music.baseModule.bean.Error;
//import com.txznet.music.baseModule.net.request.ReqDataStats;
//import com.txznet.music.net.NetManager;
//import com.txznet.music.net.RequestCallBack;
//import com.txznet.music.playerModule.logic.PlayInfoManager;
//import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
//import com.txznet.music.playerModule.logic.net.request.ReqError;
//import com.txznet.music.utils.StringUtils;
//
///**
// * 上报帮助类
// * Created by telenewbie on 2017/7/10.
// */
//
//public class ReportHelper {
//    private static final String TAG = "Music:ReportHelper:";
//    //##创建一个单例类##
//    private volatile static ReportHelper singleton;
//
//    private ReportHelper() {
//    }
//
//    public static ReportHelper getInstance() {
//        if (singleton == null) {
//            synchronized (ReportHelper.class) {
//                if (singleton == null) {
//                    singleton = new ReportHelper();
//                }
//            }
//        }
//        return singleton;
//    }
//
//
////    /**
////     * 实时上报播放数据给后台
////     *
////     * @param currentAudio 上报的audio
////     * @param type         状态值 0 开始播放 1 播放结束 2 切歌
////     */
////    public void reportHistoryToServer(Audio currentAudio, int type) {
//////        Audio currentAudio = PlayEngineFactory.getEngine().getCurrentAudio();
////        if (currentAudio != null && currentAudio.getSid() != 0) {
////            ReportHistory reportHistory = new ReportHistory();
////            reportHistory.sid = currentAudio.getSid();
////            if (StringUtils.isEmpty(currentAudio.getAlbumId()) && PlayEngineFactory.getEngine().getCurrentAlbum() != null) {
////                reportHistory.albumId = String.valueOf(PlayEngineFactory.getEngine().getCurrentAlbum().getId());
////                reportHistory.categoryId = String.valueOf(PlayEngineFactory.getEngine().getCurrentAlbum().getCategoryId());
////            } else {
////                reportHistory.albumId = currentAudio.getAlbumId();
////                reportHistory.categoryId = currentAudio.getStrCategoryId();
////            }
////            reportHistory.audioId = currentAudio.getId();
////            reportHistory.type = type;
////            LogUtil.d(TAG + "report history " + currentAudio.getName() + " type:" + type);
////            sendReportData(ReqDataStats.Action.ACT_PREPARED, reportHistory);
////        }
////    }
//
//    public void sendReportData(ReqDataStats.Action actionName) {
//        if (PlayEngineFactory.getEngine().getCurrentAudio() != null) {
//            Audio currentAudio = PlayEngineFactory.getEngine().getCurrentAudio();
//            sendReportData(currentAudio.getId()
//                    , currentAudio.getSid()
//                    , currentAudio.getDuration()
//                    , PlayInfoManager.getInstance().getCurrentPosition()
//                    , StringUtils.toString(currentAudio.getArrArtistName())
//                    , currentAudio.getName(), actionName);
//        } else {
//            sendReportData(0, 0, 0, 0, "", "", actionName);
//        }
//    }
//
////    public void reportError(ReqError error) {
////        sendReportData(ReqDataStats.Action.ACT_ERROR_URL, error);
////    }
//
//    public void sendReportData(long id, int sid, long duration,
//                               float currentPercent, String artists, String title,
//                               ReqDataStats.Action actionName) {
//
//        ReqDataStats.ReportInfo reportInfo = new ReqDataStats.ReportInfo(id, sid, duration, currentPercent, artists, title);
//        ReqDataStats dataStats = new ReqDataStats(reportInfo, SystemClock.elapsedRealtime(), actionName);
//        sendReportData(actionName, dataStats);
//    }
//
//    public void sendReportData(ReqDataStats.Action actionName, final Object json) {
//        LogUtil.logd("music:report:" + json.toString());
//
//        if (ReqDataStats.Action.ACT_PREPARED.equals(actionName)) {
//            NetManager.getInstance().sendRequestToCore(Constant.GET_REPORT, json, new RequestCallBack<String>(String.class) {
//                int retry = 0;
//
//                @Override
//                public void onError(String cmd, Error error) {
//                    if (error.getErrorCode() == Error.ERROR_CLIENT_NET_TIMEOUT) {
//                        if (retry < 10) {
//                            retry++;
//                            NetManager.getInstance().sendRequestToCore(Constant.GET_REPORT, json, this);
//                        }
//                    }
//                }
//
//                @Override
//                public void onResponse(String data) {
//
//                }
//            });
//        } else if (ReqDataStats.Action.ACT_ERROR_URL.equals(actionName)) {
//            NetManager.getInstance().sendRequestToCore(Constant.GET_REPORT_ERROR, json, null);
//        } else {
//            ReportUtil.doReportImmediate(ReportManager.UAT_MUSIC, json.toString().getBytes());
//        }
//    }
//
//}

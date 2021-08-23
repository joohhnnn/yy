package com.txznet.music.report;

import android.text.TextUtils;

import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestCallBack;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.report.bean.EventBase;
import com.txznet.music.report.bean.PushEvent;
import com.txznet.music.utils.JsonHelper;

/**
 * Created by brainBear on 2018/1/24.
 */

public class ReportManager {

    private static final String TAG = "ReportManager:";
    private static ReportManager sInstance;

    private ReportManager() {

    }

    public static ReportManager getInstance() {
        if (null == sInstance) {
            synchronized (ReportManager.class) {
                if (null == sInstance) {
                    sInstance = new ReportManager();
                }
            }
        }
        return sInstance;
    }


    public void report(EventBase event) {
        String json = JsonHelper.toJson(event);
        Logger.d(TAG, "report:%s", json);
        ReportUtil.doReport(com.txz.report_manager.ReportManager.UAT_MUSIC, json.getBytes());
    }

    public void reportImmediate(EventBase event) {
        String json = JsonHelper.toJson(event);
        Logger.d(TAG, "reportImmediate:%s", json);
        ReportUtil.doReportImmediate(com.txz.report_manager.ReportManager.UAT_MUSIC, json.getBytes());
    }


    public void reportRepeatedly(Object object) {
        final String json = JsonHelper.toJson(object);
        Logger.d(TAG, "reportRepeatedly:%s", json);
        NetManager.getInstance().sendRequestToCore(Constant.GET_REPORT, json.getBytes(), new RequestCallBack<String>(String.class) {
            int retry = 0;

            @Override
            public void onError(String cmd, Error error) {
                if (error.getErrorCode() == Error.ERROR_CLIENT_NET_TIMEOUT) {
                    if (retry < 10) {
                        retry++;
                        NetManager.getInstance().sendRequestToCore(Constant.GET_REPORT, json.getBytes(), this);
                    }
                }
            }

            @Override
            public void onResponse(String data) {

            }
        });
    }

    public void reportInRealTime(Object object) {
        String json = JsonHelper.toJson(object);
        Logger.d(TAG, "reportInRealTime:%s", json);
        NetManager.getInstance().sendRequestToCore(Constant.GET_REPORT_ERROR, json.getBytes(), null);
    }


    public void reportAudioPlay(Audio currentAudio, int type) {
        if (currentAudio != null && currentAudio.getSid() != 0) {
            ReportHistory reportHistory = new ReportHistory();
            reportHistory.sid = currentAudio.getSid();
            if (TextUtils.isEmpty(currentAudio.getAlbumId()) && PlayEngineFactory.getEngine().getCurrentAlbum() != null) {
                reportHistory.albumId = String.valueOf(PlayEngineFactory.getEngine().getCurrentAlbum().getId());
                reportHistory.categoryId = String.valueOf(PlayEngineFactory.getEngine().getCurrentAlbum().getCategoryId());
            } else {
                reportHistory.albumId = currentAudio.getAlbumId();
                reportHistory.categoryId = currentAudio.getStrCategoryId();
            }
            reportHistory.audioId = currentAudio.getId();
            reportHistory.type = type;
            reportRepeatedly(reportHistory);
        }
    }
}

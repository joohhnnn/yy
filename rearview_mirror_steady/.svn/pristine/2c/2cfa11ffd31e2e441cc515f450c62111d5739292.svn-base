package com.txznet.music.report;

import com.txznet.comm.err.Error;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.data.http.api.txz.TXZMusicApi;
import com.txznet.music.helper.TXZNetRequest;
import com.txznet.music.report.entity.BaseEvent;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.Logger;


/**
 * Created by brainBear on 2018/1/24.
 */

public class ReportManager {

    private static final String TAG = Constant.LOG_TAG_REPORT;
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

    /**
     * 上报，一定量后上报
     */
    public void report(BaseEvent event) {
        if (BuildConfig.DEBUG) {
            reportImmediate(event);
        } else {
            String json = JsonHelper.toJson(event);
            Logger.d(TAG, "report:%s", json);
            ReportUtil.doReport(com.txz.report_manager.ReportManager.UAT_MUSIC, json.getBytes());
        }
    }

    /**
     * 立即上报
     */
    public void reportImmediate(BaseEvent event) {
        String json = JsonHelper.toJson(event);
        Logger.d(TAG, "reportImmediate:%s", json);
        ReportUtil.doReportImmediate(com.txz.report_manager.ReportManager.UAT_MUSIC, json.getBytes());
    }

    /**
     * 接口上报
     */
    public void reportRepeatedly(Object object) {
        final String json = JsonHelper.toJson(object);
        Logger.d(TAG, "reportRepeatedly:%s", json);
        TXZNetRequest.get().sendSeqRequestToCore(TXZMusicApi.GET_REPORT, json.getBytes(), new TXZNetRequest.RequestCallBack<String>(String.class) {
            int retry = 0;

            @Override
            public void onError(String cmd, Error error) {
                if (error.errorCode == ErrCode.ERROR_CLIENT_NET_TIMEOUT) {
                    if (retry < 10) {
                        retry++;
                        TXZNetRequest.get().sendSeqRequestToCore(TXZMusicApi.GET_REPORT, json.getBytes(), this);
                    }
                }
            }

            @Override
            public void onResponse(String data) {

            }
        });
    }
}

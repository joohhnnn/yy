package com.txznet.webchat.actions;

import com.txz.report_manager.ReportManager;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.webchat.Config;
import com.txznet.webchat.log.L;
import com.txznet.webchat.ui.base.UIHandler;

import org.json.JSONObject;

import java.util.Map;

/**
 * 数据上报ActionCreator
 * Created by ASUS User on 2016/4/19.
 */
public class TXZReportActionCreator {
    private static TXZReportActionCreator sInstance;

    public static TXZReportActionCreator getInstance() {
        if (null == sInstance) {
            synchronized (TXZReportActionCreator.class) {
                if (null == sInstance) {
                    sInstance = new TXZReportActionCreator();
                }
            }
        }

        return sInstance;
    }

    private TXZReportActionCreator() {
    }

    public void report(int actionType) {
        if (!Config.REPORT) {
            return;
        }

        doReport(actionType, null);
    }

    private void doReport(int actionType, Map data) {
        JSONObject msg = new JSONObject();
        try {
            msg.put("actionName", actionType);
            //msg.put("SDK", !Config.UI_ENABLED);
            msg.put("SDK", !UIHandler.getInstance().getWxUIEnabled());
            if (null == data) {
                msg.put("data", new JSONObject());
            } else {
                JSONObject obj = new JSONObject(data);
                msg.put("data", obj);
            }

            ReportUtil.doReport(ReportManager.UAT_WEBCHAT, msg);
            L.d("report###", msg.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

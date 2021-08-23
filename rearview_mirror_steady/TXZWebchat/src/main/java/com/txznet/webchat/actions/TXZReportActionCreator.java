package com.txznet.webchat.actions;

import com.txz.report_manager.ReportManager;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.webchat.Config;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.ReportMessage;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.ui.base.UIHandler;

import org.json.JSONObject;

import java.util.HashMap;
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

    public void reportLogin(boolean fromVoice) {
        if (!Config.REPORT) {
            return;
        }

        WxContact contact = WxContactStore.getInstance().getLoginUser();
        Map map = new HashMap();
        map.put("openId", contact.mUserOpenId);
        map.put("nick", contact.mNickName);
        map.put("gender", contact.mSex.ordinal());

        if (fromVoice) {
            doReport(ReportMessage.REPORT_VOICE_LOGIN_WECHAT, map);
        } else {
            doReport(ReportMessage.REPORT_WECHAT_LOGIN, map);
        }
    }

    public void reportLogout(boolean fromVoice) {
        if (!Config.REPORT) {
            return;
        }

        WxContact contact = WxContactStore.getInstance().getLoginUser();
        if (null == contact) {
            L.e("report logout failed, current user is null");
            return;
        }

        Map map = new HashMap();
        map.put("openId", contact.mUserOpenId);
        map.put("nick", contact.mNickName);
        map.put("gender", contact.mSex.ordinal());

        if (fromVoice) {
            doReport(ReportMessage.REPORT_VOICE_LOGOUT_WECHAT, map);
        } else {
            doReport(ReportMessage.REPORT_WECHAT_LOGOUT, map);
        }
    }

    public void reportHelpItemClick(String title) {
        if (!Config.REPORT) {
            return;
        }

        Map map = new HashMap();
        map.put("title", title);

        doReport(ReportMessage.REPORT_UI_HELP_OPEN, map);
    }


    private void doReport(int actionType, Map data) {
        JSONObject msg = new JSONObject();
        try {
            msg.put("actionName", actionType);
            //msg.put("SDK", !Config.UI_ENABLED);
            msg.put("SDK", !UIHandler.getInstance().getWxUIEnabled());
            // 添加用户标识码
            // // TODO: 2016/11/28 上报添加用户uuid
            if (WxContactStore.getInstance().getLoginUser() != null) {
                msg.put("uid", WxContactStore.getInstance().getLoginUser().mUserOpenId);
            }
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

package com.txznet.webchat.helper;

import android.content.pm.PackageInfo;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZAsrManager.CommandListener;
import com.txznet.sdk.TXZResourceManager;
import com.txznet.webchat.actions.AppStatusActionCreator;
import com.txznet.webchat.actions.LoginActionCreator;
import com.txznet.webchat.actions.TXZReportActionCreator;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.ReportMessage;
import com.txznet.webchat.stores.WxLoginStore;

public class TXZCmdHelper {
    private static final String TXZ_PACKAGE_NAME = "com.txznet.txz";
    private static TXZCmdHelper sInstance;

    private TXZCmdHelper() {

    }

    public static TXZCmdHelper getInstance() {
        if (sInstance == null) {
            synchronized (TXZCmdHelper.class) {
                if (sInstance == null) {
                    sInstance = new TXZCmdHelper();
                }
            }
        }
        return sInstance;
    }

    private static final String CMD_EXIT_WX = "CMD_EXIT_WX";
    private static final String CMD_OPEN_AUTO_SPEAK = "CMD_OPEN_AUTO_SPEAK";
    private static final String CMD_CLOSE_AUTO_SPEAK = "CMD_CLOSE_AUTO_SPEAK";
    private static final String CMD_OPEN_GROUP_MSG = "CMD_OPEN_GROUP_MSG";
    private static final String CMD_CLOSE_GROUP_MSG = "CMD_CLOSE_GROUP_MSG";

    private CommandListener mWxCmdListener = new CommandListener() {

        @Override
        public void onCommand(String cmd, String data) {
            if (!WxLoginStore.get().isLogin()) {
                TXZResourceManager.getInstance().speakTextOnRecordWin("您尚未登陆微信助手，如需登录，请说打开微信助手", false, null);
                return;
            }
            if (data.equals(CMD_EXIT_WX)) {
                TtsUtil.speakText("即将为您退出微信助手", new TtsUtil.ITtsCallback() {
                    public void onEnd() {
                        LoginActionCreator.get().doLogout(true);
                        TXZReportActionCreator.getInstance().reportLogout(true);
                    }
                });
            } else if (data.equals(CMD_CLOSE_AUTO_SPEAK)) {
                AppStatusActionCreator.get().disableAutoSpeak();
                TtsUtil.speakText("微信自动播报已关闭");
                TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_VOICE_NOTIFY_DISABLE);
            } else if (data.equals(CMD_OPEN_AUTO_SPEAK)) {
                AppStatusActionCreator.get().enableAutoSpeak();
                TtsUtil.speakText("微信自动播报已开启");
                TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_VOICE_NOTIFY_ENABLE);
            } else if (data.equals(CMD_OPEN_GROUP_MSG)) {
                AppStatusActionCreator.get().enableGroupMsgSpeak();
                TtsUtil.speakText("群消息播报已开启");
            } else if (data.equals(CMD_CLOSE_GROUP_MSG)) {
                AppStatusActionCreator.get().disableGroupMsgSpeak();
                TtsUtil.speakText("群消息播报已关闭");
            }
            TXZResourceManager.getInstance().dissmissRecordWin();
        }
    };

    public void registerWxCmd() {
        if (getTXZVersionCode() >= 240) {
            L.d("TXZCmdHelper::registerWxCmd::core version >= 240, skipping register cmd");
            return;
        }
        TXZAsrManager.getInstance().addCommandListener(mWxCmdListener);
        TXZAsrManager.getInstance().regCommand(new String[]{"退出微信", "退出车载微信", "结束微信", "退掉微信", "注销微信", "关闭微信", "关微信", "关闭微信助手", "退出微信助手"}, CMD_EXIT_WX);
        TXZAsrManager.getInstance().regCommand(new String[]{"打开自动播报", "打开微信自动播报", "打开微信播报", "打开播报"}, CMD_OPEN_AUTO_SPEAK);
        TXZAsrManager.getInstance().regCommand(new String[]{"关闭自动播报", "关闭微信自动播报", "关闭微信播报", "关闭播报"}, CMD_CLOSE_AUTO_SPEAK);
        TXZAsrManager.getInstance().regCommand(new String[]{"关闭群消息", "关闭群消息播报", "关闭微信群消息", "关闭微信群消息播报", "屏蔽群消息", "屏蔽微信群消息", "屏蔽群", "屏蔽微信群"}, CMD_CLOSE_GROUP_MSG);
        TXZAsrManager.getInstance().regCommand(new String[]{"打开群消息", "打开群消息播报", "打开微信群消息", "打开微信群消息播报", "解除群消息屏蔽", "解除微信群消息屏蔽", "解除群屏蔽", "解除微信群屏蔽", "取消群消息屏蔽", "取消微信群消息屏蔽", "取消群屏蔽", "取消微信群屏蔽"}, CMD_OPEN_GROUP_MSG);
    }

    public void unregisterWxCmd() {
        TXZAsrManager.getInstance().unregCommand(new String[]{"退出微信", "退出车载微信", "结束微信", "退掉微信", "注销微信", "关闭微信", "关微信", "关闭微信助手", "退出微信助手"});
        TXZAsrManager.getInstance().unregCommand(new String[]{"打开自动播报", "打开微信自动播报", "打开微信播报", "打开播报"});
        TXZAsrManager.getInstance().unregCommand(new String[]{"关闭自动播报", "关闭微信自动播报", "关闭微信播报", "关闭播报"});
        TXZAsrManager.getInstance().unregCommand(new String[]{"关闭群消息", "关闭群消息播报", "关闭微信群消息", "关闭微信群消息播报", "屏蔽群消息", "屏蔽微信群消息", "屏蔽群", "屏蔽微信群"});
        TXZAsrManager.getInstance().unregCommand(new String[]{"打开群消息", "打开群消息播报", "打开微信群消息", "打开微信群消息播报", "解除群消息屏蔽", "解除微信群消息屏蔽", "解除群屏蔽", "解除微信群屏蔽", "取消群消息屏蔽", "取消微信群消息屏蔽", "取消群屏蔽", "取消微信群屏蔽"});
        TXZAsrManager.getInstance().removeCommandListener(mWxCmdListener);
    }

    private int getTXZVersionCode() {
        PackageInfo info = null;

        try {
            info = GlobalContext.get().getPackageManager()
                    .getPackageInfo(TXZ_PACKAGE_NAME, 0);

            return info.versionCode;
        } catch (Exception e) {
            L.w("TXZCmdHelper::get txz version encountered error: "
                    + e.toString());
        }

        return 0;
    }
}

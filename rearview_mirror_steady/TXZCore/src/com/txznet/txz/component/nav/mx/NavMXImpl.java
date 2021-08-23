package com.txznet.txz.component.nav.mx;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;

import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.ui.dialog2.WinConfirmAsr;
import com.txznet.comm.ui.dialog2.WinConfirmAsr.WinConfirmAsrBuildData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZMediaFocusManager;
import com.txznet.txz.component.nav.INavHighLevelInterface;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class NavMXImpl extends NavThirdApp implements INavHighLevelInterface {
    private static final String RECEIVER_ACTION = "NAVI_TO_VC_COMMUNICATE_MESSAGE";
    private static final String ACTION_TYPE = "CMD_TYPE_ENUM";
    private static final String ACTION_CONTENT = "COMMUNICATE_INFO_CONTENT";

    private static final String BROADCAST_ACTION_TTS_START = "com.mxnavi.broadcast.startplaysound";
    private static final String BROADCAST_ACTION_TTS_END = "com.mxnavi.broadcast.stopplaysound";

    public static final String MX_PACKAGE_NAME = "com.mxnavi.mxnavi";

    // 退出导航
    private static final int CMD_SHUT_DOWN = 1;
    // 放大
    private static final int CMD_SETTING_SCALE_UP = 5;
    // 缩小
    private static final int CMD_SETTING_SCALE_DOWN = 6;
    // 查询剩余距离
    private static final int CMD_QUERY_DISTANCE = 83;
    // 查询剩余时间
    private static final int CMD_QUERY_TIME = 84;

    //停止导航
    private static final int CMD_ROUTE_STOP_GUIDE = 60;
    // 同步家和公司地址
    private static final int TYPE_UPDATE_HOME_COMPANY = 114;

    boolean mViewAll;
    private boolean mHasBeenEnter;
    private MXNaviInfo mMXNaviInfo;

    private static String[] MX_DIRECTION = new String[]{"直行", "左转", "左前方", "左后方", "左调头", "右转", "右前方", "右后方", "右调头",
            "隧道入口"};

    public NavMXImpl() {
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction("com.mxnavi.mxnavi.NToC_NAVI_SHOW");
        iFilter.addAction("com.mxnavi.mxnavi.NToC_NAVI_HIDE");
        iFilter.addAction("com.mxnavi.mxnavi.TO_CTRL_TURNING_INFO");
        iFilter.addAction(BROADCAST_ACTION_TTS_START);
        iFilter.addAction(BROADCAST_ACTION_TTS_END);
        iFilter.addAction(RECEIVER_ACTION);
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                handleRecv(intent);
            }
        }, iFilter);

        LogUtil.logd("NavMXImpl >>isAppTop:" + isAppOnTop());
        if (isAppOnTop()) {
            regNavUiCommands();
        }
    }

    private void dismissDialog() {
        AppLogic.runOnUiGround(new Runnable() {

            @Override
            public void run() {
                if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
                    mWinConfirmAsr.dismiss("");
                }
            }
        }, 0);
    }


    @Override
    public void enterNav() {
        LogUtil.logd("enterNav com.mxnavi.mxnavi");
        ComponentName comp = new ComponentName("com.mxnavi.mxnavi", "com.mxnavi.mxnavi.MXNavi");
        Intent intent = new Intent();
        intent.setComponent(comp);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            GlobalContext.get().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void exitNav() {
        try {
            HelpGuideManager.getInstance().notifyCloseNav();
            sendCommandToNav(CMD_SHUT_DOWN, null);
            dismissDialog();
            super.exitNav();
        } catch (Exception e) {
        }

        // 退出了应用
        onExitApp();
    }

    private boolean isAppOnTop() {
        try {
            ActivityManager am = (ActivityManager) GlobalContext.get().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (tasks != null && !tasks.isEmpty()) {
                ComponentName topActivity = tasks.get(0).topActivity;
                if (topActivity.getPackageName().equals(getPackageName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getPackageName() {
        return MX_PACKAGE_NAME;
    }

    private static final int CMD_SEARCH_WAY_RECOMEND = 1; // 推荐
    private static final int CMD_SEARCH_WAY_NEAR = 2; // 最短
    private static final int CMD_SEARCH_WAY_QUICK = 3; // 最快
    private static final int CMD_SEARCH_WAY_CHEAP = 4; // 经济

    @Override
    public boolean NavigateTo(NavPlanType plan, final NavigateInfo info) {
        super.NavigateTo(plan, info);
        int cat = CMD_SEARCH_WAY_RECOMEND;
        if (plan == NavPlanType.NAV_PLAN_TYPE_RECOMMEND) {
            cat = CMD_SEARCH_WAY_RECOMEND;
        } else if (plan == NavPlanType.NAV_PLAN_TYPE_LEAST_COST) {
            cat = CMD_SEARCH_WAY_CHEAP;
        } else if (plan == NavPlanType.NAV_PLAN_TYPE_LEAST_DISTANCE) {
            cat = CMD_SEARCH_WAY_NEAR;
        } else if (plan == NavPlanType.NAV_PLAN_TYPE_LEAST_TIME) {
            cat = CMD_SEARCH_WAY_QUICK;
        }

        if (info == null || info.msgGpsInfo == null) {
            return false;
        }
        final int ct = cat;
        final String lat = String.valueOf(info.msgGpsInfo.dblLat);
        final String lng = String.valueOf(info.msgGpsInfo.dblLng);
        final String dp = lng + "," + lat;

        long delay = mHasBeenEnter ? 0 : 5000;
        if (!PackageManager.getInstance().isAppRunning(getPackageName())) {
            final Intent intent = GlobalContext.get().getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                GlobalContext.get().startActivity(intent);
            }
            delay = 5000;
        }

        AppLogic.runOnBackGround(new Runnable() {

            @Override
            public void run() {
                mHasBeenEnter = true;
                Intent i = new Intent("com.mxnavi.mxnavi.ONE_KEY_MSG");
                i.addFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);
                i.putExtra("Destination", info.strTargetName);
                i.putExtra("Encryption", 1);
                i.putExtra("SearchWay", ct);
                i.putExtra("DestPoint", dp);
                i.putExtra("WayPoint", "");
                LogUtil.logd("NavMXImpl >>  NavigateTo Destination " + info.strTargetName);
                GlobalContext.get().sendBroadcast(i);
            }
        }, delay);

        return true;
    }

    public boolean NavigateByMCode(String name, String mcode) {
        String params = new JSONBuilder().put("name", name).put("mcode", mcode).toString();
        sendCommandToNav(71, new String[]{"1.0", params, "0"});
        return true;
    }

    private void sendCommandToNav(int type, String[] params) {
        Bundle b = new Bundle();
        b.putInt("CMD_TYPE_ENUM", type);
        if (params != null) {
        } else {
            params = new String[3];
            params[0] = "1.0";
            params[1] = "";
            params[2] = "0";
        }
        b.putStringArray("COMMUNICATE_INFO_CONTENT", params);
        Intent intent = new Intent("VC_TO_NAVI_COMMUNICATE_MESSAGE");
        intent.putExtras(b);
        GlobalContext.get().sendBroadcast(intent);
        LogUtil.logd("NavMXImpl >> start sendCommandToNav action:VC_TO_NAVI_COMMUNICATE_MESSAGE,  type:" + type);
    }

    public void regNavUiCommands() {
        AsrUtil.AsrComplexSelectCallback callback = new AsrUtil.AsrComplexSelectCallback() {

            @Override
            public boolean needAsrState() {
                return false;
            }

            @Override
            public String getTaskId() {
                return "NAV_CTRL#" + getPackageName();
            }

            @Override
            public void onCommandSelected(String type, String command) {
                onNavCommand(isWakeupResult(), type, command);
            }
        };

        callback.addCommand("ZOOM_IN", "放大地图", "地图放大")
                .addCommand("ASK_REMAIN_TIME", "还有多久", "还要多久")
                .addCommand("ASK_REMAIN_DISTANCE", "还有多远")
                .addCommand("ASK_HOW", "前面怎么走").addCommand("ZOOM_OUT", "缩小地图", "地图缩小")
                .addCommand("VIEW_ALL", "查看全程").addCommand("TUIJIANLUXIAN", "推荐路线")
                .addCommand("BACK_NAVI", "继续导航", "恢复导航").addCommand("ZUIKUAILUXIAN", "最快路线")
                .addCommand("ZUIDUANLUCHENG", "最短路线", "少路程").addCommand("JINGJI", "经济路线", "少收费")
                .addCommand("EXIT_NAV", "退出导航", "关闭导航").addCommand("STOP_NAV", "停止导航");
        HelpGuideManager.getInstance().notifyNavHelp(Arrays.asList(callback.genKeywords()));
        WakeupManager.getInstance().useWakeupAsAsr(callback);
    }

    public void unregNavUiCommands() {
        HelpGuideManager.getInstance().notifyNavHelp(null);
        WakeupManager.getInstance().recoverWakeupFromAsr("NAV_CTRL#" + getPackageName());
    }

    private class MXNaviInfo {
        // 转向的ID图标
        public int turnId = -1;
        // 还剩余多少距离到达目的地
        public int remainDistance;
        // 当前道路名称
        public String curName;
        // 下一跳道路名称
        public String nextName;
        // 目的地名称
        public String destName;
        // 剩余的时间
        public int remainTime;
        // 距离转向点距离
        public int turnDistance;
    }

    @Override
    public void addStatusListener() {
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onEnd(boolean arrive) {
    }

    @Override
    public void onPlanComplete() {
    }

    @Override
    public void onPlanError(int errCode, String errDesc) {
    }

    @Override
    public void onResume() {
        mIsFocus = true;
        if (mNavStatusListener != null) {
            mNavStatusListener.onForeground(getPackageName(), true);
        }
        HelpGuideManager.getInstance().notifyNavStatus(true, false);
        regNavUiCommands();
    }

    private void handleRecv(Intent intent) {
        String action = intent.getAction();
        LogUtil.logd("NavMXImpl onReceive action:" + action);
        if (action.equals("com.mxnavi.mxnavi.NToC_NAVI_SHOW")) {
            onResume();
            return;
        } else if (action.equals("com.mxnavi.mxnavi.NToC_NAVI_HIDE")) {
            onPause();
            return;
        } else if (action.equals("com.mxnavi.mxnavi.TO_CTRL_TURNING_INFO")) {
            // 导航转向协议
            /**
             * 0 （直行） 1 （左转） 2 （左前方） 3 （左后方） 4 （左调头） 5 （右转） 6 （右前方） 7 （右后方） 8
             * （右调头） 32（隧道入口）
             */
            if (mMXNaviInfo == null) {
                mMXNaviInfo = new MXNaviInfo();
            }
            try {
                mMXNaviInfo.turnId = intent.getIntExtra("turnID", 0);
                mMXNaviInfo.remainDistance = intent.getIntExtra("destdistance", -1);
                mMXNaviInfo.curName = intent.getStringExtra("roadname");
                mMXNaviInfo.nextName = intent.getStringExtra("nextroadname");
                mMXNaviInfo.destName = intent.getStringExtra("destname");
                mMXNaviInfo.remainTime = intent.getIntExtra("desttime", -1);
                mMXNaviInfo.turnDistance = intent.getIntExtra("turndistance", -1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (BROADCAST_ACTION_TTS_START.equals(action)) {
            TXZMediaFocusManager.getInstance().requestFocus();
        } else if (BROADCAST_ACTION_TTS_END.equals(action)) {
            TXZMediaFocusManager.getInstance().releaseFocus();
        }

        onRecv(intent);
    }

    @Override
    public void handleIntent(Intent intent) {
        handleRecv(intent);
    }

    @Override
    public void onPause() {
        mIsFocus = false;
        if (mNavStatusListener != null) {
            mNavStatusListener.onForeground(getPackageName(), false);
        }
        dismissDialog();
        HelpGuideManager.getInstance().notifyNavStatus(false, false);
        unregNavUiCommands();
    }

    @Override
    public void onNavCommand(boolean isWakeup, String type, final String command) {
        if ("EXIT_NAV".equals(type)) {
            JSONBuilder json = new JSONBuilder();
            json.put("sence", "nav");
            json.put("text", command);
            json.put("action", "exit");
            if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
                return;
            }
        }

        LogUtil.logd("NavMXImpl >> onCommandSelected type:" + type + ",cmd:" + command);
        if ("ZOOM_IN".equals(type)) {
            AsrManager.getInstance().setNeedCloseRecord(true);
            String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", command);
            RecorderWin.speakTextWithClose(spk, new Runnable() {
                @Override
                public void run() {
                    sendCommandToNav(CMD_SETTING_SCALE_UP, null);
                }
            });
            return;
        }
        if ("ZOOM_OUT".equals(type)) {
            AsrManager.getInstance().setNeedCloseRecord(true);
            String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", command);
            RecorderWin.speakTextWithClose(spk, new Runnable() {

                @Override
                public void run() {
                    sendCommandToNav(CMD_SETTING_SCALE_DOWN, null);
                }
            });
            return;
        }
        if ("ASK_REMAIN_DISTANCE".equals(type)) {
            if (mMXNaviInfo == null) {
                LogUtil.logd("CMD_QUERY_DISTANCE");
                AppLogic.runOnBackGround(new Runnable() {

                    @Override
                    public void run() {
                        sendCommandToNav(CMD_QUERY_DISTANCE, null);
                    }
                }, 2000);
                RecorderWin.close();
                return;
            }

            Integer remainTime = mMXNaviInfo.remainTime;
            Integer remainDistance = mMXNaviInfo.remainDistance;

            String rt = "";//getRemainTime(remainTime);
            String rd = getRemainDistance(remainDistance);
            String hint = "";
            if (TextUtils.isEmpty(rt) && TextUtils.isEmpty(rd)) {
                hint = "";
            }
            if (!TextUtils.isEmpty(rt) && !TextUtils.isEmpty(rd)) {
                hint = NativeData.getResString("RS_MAP_DESTINATION_ABOUT").replace("%DISTANCE%", rd).replace("%TIME%",
                        rt);
            } else if (!TextUtils.isEmpty(rd)) {
                hint = NativeData.getResPlaceholderString("RS_MAP_DESTINATION_DIS", "%DISTANCE%", rd);
            } else if (!TextUtils.isEmpty(rt)) {
                hint = NativeData.getResPlaceholderString("RS_MAP_DESTINATION_TIME", "%TIME%", rt);
            }

            AsrManager.getInstance().setNeedCloseRecord(true);
            RecorderWin.speakTextWithClose(hint, null);
            return;
        }
        if ("ASK_REMAIN_TIME".equals(type)) {
            if (mMXNaviInfo == null) {
                LogUtil.logd("NCMD_QUERY_TIME");
                AppLogic.runOnBackGround(new Runnable() {

                    @Override
                    public void run() {
                        sendCommandToNav(CMD_QUERY_TIME, null);
                    }
                }, 2000);
                RecorderWin.close();
                return;
            }

            Integer remainTime = mMXNaviInfo.remainTime;
            Integer remainDistance = mMXNaviInfo.remainDistance;

            String rt = getRemainTime(remainTime);
            String rd = "";//getRemainDistance(remainDistance);
            String hint = "";
            if (TextUtils.isEmpty(rt) && TextUtils.isEmpty(rd)) {
                hint = "";
            }
            if (!TextUtils.isEmpty(rt) && !TextUtils.isEmpty(rd)) {
                hint = NativeData.getResString("RS_MAP_DESTINATION_ABOUT").replace("%DISTANCE%", rd).replace("%TIME%",
                        rt);
            } else if (!TextUtils.isEmpty(rd)) {
                hint = NativeData.getResPlaceholderString("RS_MAP_DESTINATION_DIS", "%DISTANCE%", rd);
            } else if (!TextUtils.isEmpty(rt)) {
                hint = NativeData.getResPlaceholderString("RS_MAP_DESTINATION_TIME", "%TIME%", rt);
            }

            AsrManager.getInstance().setNeedCloseRecord(true);
            RecorderWin.speakTextWithClose(hint, null);
            return;
        }
        if ("ASK_HOW".equals(type)) {
            if (true) {
                AsrManager.getInstance().setNeedCloseRecord(true);
                RecorderWin.speakTextWithClose("", new Runnable() {

                    @Override
                    public void run() {
                        sendCommandToNav(121, null);
                    }
                });
                return;
            }
            if (mMXNaviInfo == null) {
                RecorderWin.close();
                return;
            }

            int index = mMXNaviInfo.turnId;
            if (index == -1) {
                RecorderWin.close();
                return;
            }

            String dirTxt = MX_DIRECTION[0];
            if (index == 32) {
                dirTxt = MX_DIRECTION[MX_DIRECTION.length];
            } else {
                if (index < MX_DIRECTION.length) {
                    dirTxt = MX_DIRECTION[index];
                }
            }

            long remainDistance = mMXNaviInfo.turnDistance;
            String distance = "";
            if (remainDistance > 1000) {
                distance = (Math.round(remainDistance / 100.0) / 10.0) + "公里";
            } else if (remainDistance > 0) {
                distance = remainDistance + "米";
            } else {
                RecorderWin.close();
                return;
            }

            String nextRoad = mMXNaviInfo.nextName;
            String hint = NativeData.getResPlaceholderString("RS_MAP_FRONT", "%DISTANCE%", distance + dirTxt);
            if (!TextUtils.isEmpty(nextRoad)) {
                hint = NativeData.getResString("RS_MAP_FRONT_INTO").replace("%DISTANCE", distance + dirTxt)
                        .replace("%ROAD%", nextRoad);
            }
            AsrManager.getInstance().setNeedCloseRecord(true);
            RecorderWin.speakTextWithClose(hint, null);
            return;
        }
        if ("STOP_NAV".equals(type)) {
            if (!isWakeup) {
                AsrManager.getInstance().setNeedCloseRecord(true);
                String navEixt = NativeData.getResString("RS_MAP_NAV_EXIT");
                String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", navEixt);
                RecorderWin.speakTextWithClose(spk, new Runnable() {
                    @Override
                    public void run() {
                        sendCommandToNav(CMD_ROUTE_STOP_GUIDE, null);
                    }
                });
                return;
            }
            if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
                return;
            }

            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    String navEixt = NativeData.getResString("RS_MAP_NAV_STOP");
                    final String sureSpk = NativeData.getResString("RS_MAP_CONFIRM_EXIT_SURE").replace("%COMMAND%",
                            navEixt);
                    final String hintSpk = NativeData.getResString("RS_MAP_CONFIRM_EXIT_HINT").replace("%COMMAND%",
                            navEixt);
                    WinConfirmAsrBuildData data = new WinConfirmAsrBuildData();
                    data.setSureText("确定", new String[]{"确定", "退出"});
                    data.setCancelText("取消", new String[]{"取消"});
                    data.setHintTts(hintSpk);
                    data.setMessageText(hintSpk);
                    data.setContext(GlobalContext.getModified());
					data.setSystemDialog(true);
                    mWinConfirmAsr = new WinConfirmAsr(data) {

                        @Override
                        public String getReportDialogId() {
                            return "NavMXImpl_dialog";
                        }

                        @Override
                        public void onClickOk() {
                            TtsManager.getInstance().speakText(sureSpk, new ITtsCallback() {
                                @Override
                                public void onEnd() {
                                    sendCommandToNav(CMD_ROUTE_STOP_GUIDE, null);
                                }

                                ;
                            });
                        }
                    };
                    showConfirmDialog();
                }
            }, 0);
            return;
        }

        if ("EXIT_NAV".equals(type)) {
            if (!isWakeup) {
                AsrManager.getInstance().setNeedCloseRecord(true);
                String navEixt = NativeData.getResString("RS_MAP_NAV_EXIT");
                String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", navEixt);
                RecorderWin.speakTextWithClose(spk, new Runnable() {
                    @Override
                    public void run() {
                        exitNav();
                    }
                });
                return;
            }
            if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
                return;
            }

            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    String navEixt = NativeData.getResString("RS_MAP_NAV_EXIT");
                    final String sureSpk = NativeData.getResString("RS_MAP_CONFIRM_EXIT_SURE").replace("%COMMAND%",
                            navEixt);
                    final String hintSpk = NativeData.getResString("RS_MAP_CONFIRM_EXIT_HINT").replace("%COMMAND%",
                            navEixt);
                    WinConfirmAsrBuildData data = new WinConfirmAsrBuildData();
                    data.setSureText("确定", new String[]{"确定", "退出"});
                    data.setCancelText("取消", new String[]{"取消"});
                    data.setHintTts(hintSpk);
                    data.setMessageText(hintSpk);
                    data.setContext(GlobalContext.getModified());
					data.setSystemDialog(true);
                    mWinConfirmAsr = new WinConfirmAsr(data) {

                        @Override
                        public String getReportDialogId() {
                            return "NavMXImpl_dialog";
                        }

                        @Override
                        public void onClickOk() {
                            TtsManager.getInstance().speakText(sureSpk, new ITtsCallback() {
                                @Override
                                public void onEnd() {
                                    exitNav();
                                }

                                ;
                            });
                        }
                    };
                    showConfirmDialog();
                }
            }, 0);
            return;
        }

        if ("VIEW_ALL".equals(type)) {
            AsrManager.getInstance().setNeedCloseRecord(true);
//            String spk = NativeData.getResPlaceholderString("RS_VOICE_DOING_COMMAND", "%CMD%", command);
            String spk = NativeData.getResString("RS_MAP_VIEW_ALL");
            RecorderWin.speakTextWithClose(spk, new Runnable() {

                @Override
                public void run() {
                    mViewAll = true;
                    sendCommandToNav(85, null);
                }
            });
            return;
        }
        if ("BACK_NAVI".equals(type)) {
            if (!mViewAll) {
                RecorderWin.close();
                return;
            }
            mViewAll = false;
            AsrManager.getInstance().setNeedCloseRecord(true);
            sendCommandToNav(93, null);
            RecorderWin.speakText(NativeData.getResPlaceholderString("RS_VOICE_ALREAD_DO_COMMAND", "%CMD%", command),
                    null);
            return;
        }
        if ("TUIJIANLUXIAN".equals(type)) {
            if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
                return;
            }

            AppLogic.runOnUiGround(new Runnable() {

                @Override
                public void run() {
                    String xiTong = NativeData.getResString("RS_PATH_XITONG");
                    final String sureSpk = NativeData.getResString("RS_MAP_CONFIRM_SURE_SPK").replace("%COMMAND%",
                            xiTong);
                    final String hintSpk = NativeData.getResString("RS_MAP_CONFIRM_HINT_SPK").replace("%COMMAND%",
                            xiTong);
                    WinConfirmAsrBuildData data = new WinConfirmAsrBuildData();
                    data.setSureText("确定", new String[]{"确定"});
                    data.setCancelText("取消", new String[]{"取消"});
                    data.setHintTts(hintSpk);
                    data.setMessageText(hintSpk);
                    data.setContext(GlobalContext.getModified());
					data.setSystemDialog(true);
                    mWinConfirmAsr = new WinConfirmAsr(data) {

                        @Override
                        public String getReportDialogId() {
                            return "NavMXImpl_dialog";
                        }

                        @Override
                        public void onClickOk() {
                            TtsManager.getInstance().speakText(sureSpk, new ITtsCallback() {

                                public void onSuccess() {
                                    sendCommandToNav(86, null);
                                }
                            });
                        }
                    };
                    showConfirmDialog();
                }
            }, 0);
            return;
        }
        if ("ZUIKUAILUXIAN".equals(type)) {
            if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
                return;
            }

            AppLogic.runOnUiGround(new Runnable() {

                @Override
                public void run() {
                    String hint = NativeData.getResString("RS_MAP_DUOBIYONGDU");
                    if ("最快路线".equals(command)) {
                        hint = NativeData.getResString("RS_MAP_PATH_FASTEST");
                    }

                    final String ttsTxt = hint;
                    final String sureSpk = NativeData.getResString("RS_MAP_CONFIRM_SURE_SPK").replace("%COMMAND%",
                            ttsTxt);
                    final String hintSpk = NativeData.getResString("RS_MAP_CONFIRM_HINT_SPK").replace("%COMMAND%",
                            ttsTxt);
                    WinConfirmAsrBuildData data = new WinConfirmAsrBuildData();
                    data.setSureText("确定", new String[]{"确定"});
                    data.setCancelText("取消", new String[]{"取消"});
                    data.setHintTts(hintSpk);
                    data.setMessageText(hintSpk);
                    data.setContext(GlobalContext.getModified());
					data.setSystemDialog(true);
                    mWinConfirmAsr = new WinConfirmAsr(data) {

                        @Override
                        public void onClickOk() {
                            TtsManager.getInstance().speakText(sureSpk, new ITtsCallback() {
                                public void onSuccess() {
                                    sendCommandToNav(87, null);
                                }
                            });
                        }

                        @Override
                        public String getReportDialogId() {
                            return "NavMXImpl_dialog";
                        }
                    };
                    showConfirmDialog();
                }
            }, 0);
            return;
        }
        if ("ZUIDUANLUCHENG".equals(type)) {
            if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
                return;
            }

            AppLogic.runOnUiGround(new Runnable() {

                @Override
                public void run() {
                    String hint = NativeData.getResString("RS_MAP_PATH_DISTANCE_LESS");
                    if ("最短路线".equals(command)) {
                        hint = NativeData.getResString("RS_MAP_PATH_SHORTEST");
                    }

                    final String ttsTxt = hint;
                    final String sureSpk = NativeData.getResString("RS_MAP_CONFIRM_SURE_SPK").replace("%COMMAND%",
                            ttsTxt);
                    final String hintSpk = NativeData.getResString("RS_MAP_CONFIRM_HINT_SPK").replace("%COMMAND%",
                            ttsTxt);
                    WinConfirmAsrBuildData data = new WinConfirmAsrBuildData();
                    data.setSureText("确定", new String[]{"确定"});
                    data.setCancelText("取消", new String[]{"取消"});
                    data.setHintTts(hintSpk);
                    data.setMessageText(hintSpk);
                    data.setContext(GlobalContext.getModified());
					data.setSystemDialog(true);
                    mWinConfirmAsr = new WinConfirmAsr(data) {
                        @Override
                        public String getReportDialogId() {
                            return "NavMXImpl_dialog";
                        }

                        @Override
                        public void onClickOk() {
                            TtsManager.getInstance().speakText(sureSpk, new ITtsCallback() {

                                public void onSuccess() {
                                    sendCommandToNav(88, null);
                                }
                            });
                        }
                    };
                    showConfirmDialog();
                }
            }, 0);
            return;
        }
        if ("JINGJI".equals(type)) {
            if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
                return;
            }

            AppLogic.runOnUiGround(new Runnable() {

                @Override
                public void run() {
                    String cheap = NativeData.getResString("RS_MAP_PATH_CHEAP");
                    final String sureSpk = NativeData.getResString("RS_MAP_CONFIRM_SURE_SPK").replace("%COMMAND%",
                            cheap);
                    final String hintSpk = NativeData.getResString("RS_MAP_CONFIRM_HINT_SPK").replace("%COMMAND%",
                            cheap);
                    WinConfirmAsrBuildData data = new WinConfirmAsrBuildData();
                    data.setSureText("确定", new String[]{"确定"});
                    data.setCancelText("取消", new String[]{"取消"});
                    data.setHintTts(hintSpk);
                    data.setMessageText(hintSpk);
                    data.setContext(GlobalContext.getModified());
					data.setSystemDialog(true);
                    mWinConfirmAsr = new WinConfirmAsr(data) {
                        @Override
                        public String getReportDialogId() {
                            return "NavMXImpl_dialog";
                        }

                        @Override
                        public void onClickOk() {
                            TtsManager.getInstance().speakText(sureSpk, new ITtsCallback() {

                                public void onSuccess() {
                                    sendCommandToNav(89, null);
                                }
                            });
                        }
                    };
                    showConfirmDialog();
                }
            }, 0);
            return;
        }
    }

    private void showConfirmDialog() {
        if (RecorderWin.isOpened()) {
            RecorderWin.close();
        }
        mWinConfirmAsr.showImediately();
    }

    private void onRecv(Intent intent) {
        int key_type = intent.getIntExtra("key_type", -1);
        switch (key_type) {
            case 10001: // 导航发出地址更新的广播
                onNavNavigateInfo(intent);
                break;
            case 10002: // 导航获取语音保存的地址的广播
                syncNavigateInfo();
                break;
        }
    }

    private void syncNavigateInfo() {
        LogUtil.logd("syncNavigateInfo");
        NavigateInfo info = NavManager.getInstance().getHomeNavigateInfo();
        updateHomeLocation(info);
        info = NavManager.getInstance().getCompanyNavigateInfo();
        updateCompanyLocation(info);
    }

    private void onNavNavigateInfo(Intent intent) {
        int action = intent.getIntExtra("action", -1); // 1为更新，2为删除
        int addr_type = intent.getIntExtra("addr_type", -1);// 1为家，2为公司
        double lat = intent.getDoubleExtra("lat", -1);
        double lng = intent.getDoubleExtra("lng", -1);
        String name = intent.getStringExtra("name");
        String addr = intent.getStringExtra("address");
        LogUtil.logd("onRecvNavNavigateInfo:" + action + "," + addr_type + ","
                + lat + "," + lng + "," + name + "," + addr);
        switch (action) {
            case 1:
                switch (addr_type) {
                    case 1:
                        NavManager.getInstance().setHomeLocation(name, addr, lat, lng,
                                UiMap.GPS_TYPE_GCJ02, false);
                        break;
                    case 2:
                        NavManager.getInstance().setCompanyLocation(name, addr, lat, lng,
                                UiMap.GPS_TYPE_GCJ02, false);
                        break;
                }
                break;
            case 2:
                switch (addr_type) {
                    case 1:
                        NavManager.getInstance().clearHomeLocation();
                        break;
                    case 2:
                        NavManager.getInstance().clearCompanyLocation();
                        break;
                }
                break;
        }
    }

    @Override
    public boolean enableWorkWithoutResume() {
        return true;
    }

    @Override
    public void updateCompanyLocation(NavigateInfo navigateInfo) {
        setCommNavigate(false, navigateInfo);
    }

    @Override
    public void updateHomeLocation(NavigateInfo navigateInfo) {
        setCommNavigate(true, navigateInfo);
    }

    /**
     * {"action":"update","addr_type":"1", "name":"世界之窗","address"："广东省深圳市世界之窗"，"longitude":"113.15466","latitude":"35.21546"}
     * @param isHome
     * @param info
     */
    private void setCommNavigate(boolean isHome, NavigateInfo info) {
        JSONObject obj = new JSONObject();
        try {
            if (info == null || info.msgGpsInfo == null) {
                obj.put("action", "clear");
            } else {
                obj.put("action", "update");
                obj.put("name", info.strTargetName);
                obj.put("address", info.strTargetAddress);
                obj.put("longitude", info.msgGpsInfo.dblLng);
                obj.put("latitude", info.msgGpsInfo.dblLat);
            }
            if (isHome) {
                obj.put("addr_type", "1");
            } else {
                obj.put("addr_type", "2");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String params1 = obj.toString();

        String[] params = new String[3];
        params[0] = "1.0";
        params[1] = params1;
        params[2] = "0";

        sendCommandToNav(TYPE_UPDATE_HOME_COMPANY, params);
        LogUtil.logd("setCommNavigate isHome:" + isHome + ",navinfo:" + params1);
    }
}

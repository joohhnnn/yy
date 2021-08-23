package com.txznet.txz.component.nav.baidu.auto;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.text.TextUtils;
import android.widget.Toast;

import com.txz.ui.map.UiMap;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZAsrKeyManager;
import com.txznet.txz.BDEntryService;
import com.txznet.txz.component.nav.IMapInterface;
import com.txznet.txz.component.nav.NavInfo;
import com.txznet.txz.component.nav.NavThirdComplexApp;
import com.txznet.txz.component.nav.tx.internal.TNBroadcastSender;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.net.NetworkUtil;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.StringUtil;
import com.txznet.txz.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TXZ-METEORLUO on 2018/10/24.
 */

public class NavBaiduMapAutoImpl extends NavThirdComplexApp {
    private NavAutoInterImpl mInterface;

    public NavBaiduMapAutoImpl() {
        mInterface = new NavAutoInterImpl(new AutoControlInterface.RecvListener() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String act = intent.getAction();
                String content = intent.getStringExtra(AutoControlInterface.SERVICE_KEY_ACTION);
                LogUtil.logd("baiduMap act:" + act + ",content:" + content);
                if (AutoControlInterface.NOTIFY_OBSERVER_ACTION_NAVI_INDUCED.equals(act)) {
                    parseGuideInfo(content);
                } else if (AutoControlInterface.NOTIFY_OBSERVER_ACTION_NAVI_STATUE.equals(act)) {
                    parseNavState(content);
                } else if (AutoControlInterface.NOTIFY_OBSERVER_ACTION_MAP_INFO.equals(act)) {
                    parseMapInfo(content);
                } else if (AutoControlInterface.NOTIFY_OBSERVER_ACTION_ROUTE_INFO.equals(act)) { // 路径规划界面
                    parseRoutePlaned(content);
                }
            }
        });
    }

    /**
     * 前后台广播通知
     * @param content
     */
    private void parseMapInfo(String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            if (jsonObject.has("method")) {
                String method = jsonObject.optString("method");
                if ("map_foreground".equals(method)) {
                    onResume();
                } else if ("map_backgound".equals(method)) {
                    onPause();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseRoutePlaned(String content) {
        JSONBuilder jsonBuilder = new JSONBuilder(content);
        String method = jsonBuilder.getVal("method", String.class);
        if ("ROUTE_START".equals(method)) {
            bEnd = false;
        } else if ("ROUTE_END".equals(method)) {
            bEnd = true;
            cancelRouteWakeupAsr();
        } else if ("ROUTE_COUNT".equals(method)) {
            String count = jsonBuilder.getVal("result", String.class);
            if (!TextUtils.isEmpty(count)) {
                routeCount = Integer.parseInt(count);
                registerRouteWakeupAsr();
                TtsManager.getInstance().speakText(String.format("找到%s条路线，请选择", routeCount));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelRouteWakeupAsr();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerRouteWakeupAsr();
    }

    private int routeCount = 0;
    private boolean bEnd = false;

    private void registerRouteWakeupAsr() {
        if (bEnd || routeCount == 0) {
            LogUtil.logd("NavBaiduMapAutoImpl bEnd:" + bEnd + ",routeCount:" + routeCount);
            return;
        }

        LogUtil.logd("NavBaiduMapAutoImpl registerRouteWakeupAsr:" + routeCount);
        AsrUtil.AsrComplexSelectCallback callback = new AsrUtil.AsrComplexSelectCallback() {
            @Override
            public boolean needAsrState() {
                return false;
            }

            @Override
            public void onCommandSelected(String type, String command) {
                super.onCommandSelected(type, command);
                mInterface.selectPlanRoute(Integer.parseInt(type.substring("ITEM_INDEX_".length())));
                mInterface.startNav();
            }

            @Override
            public String getTaskId() {
                return "TASK_NAVBAIDU_ROUTE_SELECT";
            }
        };
        for (int i = 0; i < routeCount; i++) {
            String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i + 1);
            if (i == 0) {
                callback.addCommand("ITEM_INDEX_" + i, NativeData.getResStringArray("RS_CMD_SELECT_FIRST"));
            } else {
                callback.addCommand("ITEM_INDEX_" + i, "第" + strIndex + "个", "第" + strIndex + "条");
            }
        }
        WakeupManager.getInstance().useWakeupAsAsr(callback);
    }

    private void cancelRouteWakeupAsr() {
        WakeupManager.getInstance().recoverWakeupFromAsr("TASK_NAVBAIDU_ROUTE_SELECT");
    }

    @Override
    public int initialize(IInitCallback oRun) {
        if (!checkServiceRegistered()) {
            oRun.onInit(false);
            return 0;
        }

        mInterface.setPackageName(getPackageName());
        mInterface.initialize();
        queryNavFocus();
        queryNaving();
        return super.initialize(oRun);
    }

    /**
     * 判断清单文件中有没有BDEntryService
     *
     * @return
     */
    private boolean checkServiceRegistered() {
        ComponentName cn = new ComponentName(GlobalContext.get(), BDEntryService.class);
        try {
            ServiceInfo info = GlobalContext.get().getPackageManager().getServiceInfo(cn, PackageManager.GET_META_DATA);
            if (info != null) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void exitNav() {
        super.exitNav();
        mInterface.naviExit();
        mInterface.appExit();
    }

    @Override
    public boolean NavigateTo(NavPlanType plan, UiMap.NavigateInfo info) {
        mInterface.navigateTo(info.strTargetName, info.msgGpsInfo.dblLat, info.msgGpsInfo.dblLng, -1);
        return super.NavigateTo(plan, info);
    }

    @Override
    public void onNavCommand(boolean fromWakeup, String type, String command) {
        if (TXZAsrKeyManager.AsrKeyType.EXIT_NAV.equals(type) || TXZAsrKeyManager.AsrKeyType.CLOSE_MAP.equals(type)) {
            JSONBuilder json = new JSONBuilder();
            json.put("scene", "nav");
            json.put("text", command);
            json.put("action", "exit");
            if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
                return;
            }
        }
        JNIHelper
                .logd("onNavCommSelect:[" + fromWakeup + "," + type + "," + command + "," + getPackageName() + "]");
        if (TXZAsrKeyManager.AsrKeyType.ZOOM_IN.equals(type)) {
            doConfirmShow(type, command, "RS_MAP_ZOOMIN", new Runnable() {

                @Override
                public void run() {
                    mInterface.zoomMap(true);
                }
            }, false);
        }
        if (TXZAsrKeyManager.AsrKeyType.ZOOM_OUT.equals(type)) {
            doConfirmShow(type, command, "RS_MAP_ZOOMOUT", new Runnable() {

                @Override
                public void run() {
                    mInterface.zoomMap(false);
                }
            }, false);
        }
        if (TXZAsrKeyManager.AsrKeyType.NIGHT_MODE.equals(type)) {
            doConfirmShow(type, command, "RS_MAP_NIGHT_MODE", new Runnable() {

                @Override
                public void run() {
                    mInterface.switchLightNightMode(false);
                }
            }, false);
        }
        if (TXZAsrKeyManager.AsrKeyType.LIGHT_MODE.equals(type)) {
            doConfirmShow(type, command, "RS_MAP_LIGHT_MODE", new Runnable() {

                @Override
                public void run() {
                    mInterface.switchLightNightMode(true);
                }
            }, false);
        }
        if (TXZAsrKeyManager.AsrKeyType.AUTO_MODE.equals(type)) {
            doConfirmShow(type, command, "RS_MAP_AUTO_MODE", new Runnable() {

                @Override
                public void run() {
                    mInterface.switchAutoMode();
                }
            }, false);
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.OPEN_TRAFFIC.equals(type)) {
            if (!NetworkUtil.isConnectedOrConnecting(GlobalContext.get())) {
                RecorderWin.speakText(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_CHECK_NET"), null);
                return;
            }
            doConfirmShow(type, command, "RS_MAP_OPEN_TRAFFIC", new Runnable() {

                @Override
                public void run() {
                    mInterface.switchTraffic(true);
                }
            }, false);
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.CLOSE_TRAFFIC.equals(type)) {
            doConfirmShow(type, command, "RS_MAP_CLOSE_TRAFFIC", new Runnable() {

                @Override
                public void run() {
                    mInterface.switchTraffic(false);
                }
            }, false);
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.TWO_MODE.equals(type)) {
            doConfirmShow(type, command, "RS_MAP_TWO_MODE", new Runnable() {

                @Override
                public void run() {
                    mInterface.switch23D(true, 0);
                }
            }, false);
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.THREE_MODE.equals(type)) {
            doConfirmShow(type, command, "RS_MAP_THREE_MODE", new Runnable() {

                @Override
                public void run() {
                    mInterface.switch23D(false, 1);
                }
            }, false);
        }
        if (TXZAsrKeyManager.AsrKeyType.CAR_DIRECT.equals(type)) {
            doConfirmShow(type, command, "RS_MAP_CAR_DIRECT", new Runnable() {

                @Override
                public void run() {
                    mInterface.switchCarDirection();
                }
            }, false);
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.NORTH_DIRECT.equals(type)) {
            doConfirmShow(type, command, "RS_MAP_NORTH_DIRECT", new Runnable() {

                @Override
                public void run() {
                    mInterface.switchNorthDirection();
                }
            }, false);
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.VIEW_ALL.equals(type)) {
            String tts = NativeData.getResString("RS_MAP_VIEW_ALL");
            tts = tts.replace("%COMMAND%", command);
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    mInterface.zoomAll(new Runnable() {

                        @Override
                        public void run() {
                        }
                    });
                }
            };

            if (fromWakeup) {
                task.run();
                tts = NativeData.getResString("RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_VIEW_ALL").replace("%CMD%", tts);
                AsrManager.getInstance().setNeedCloseRecord(true);
                RecorderWin.speakTextWithClose(tts, null);
            } else {
                tts = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", tts);
                AsrManager.getInstance().setNeedCloseRecord(true);
                RecorderWin.speakTextWithClose(tts, task);
            }
        }
        if (TXZAsrKeyManager.AsrKeyType.TUIJIANLUXIAN.equals(type)) {
            doRePlanWakeup(type, command, "RS_MAP_TUIJIANLUXIAN", new Runnable() {

                @Override
                public void run() {
                    mInterface.switchPlanStyle(IMapInterface.PlanStyle.TUIJIAN);
                    JNIHelper.logd("NavAmapAutoNavImpl start TUIJIAN");
                }
            });
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.DUOBIYONGDU.equals(type)) {
            doRePlanWakeup(type, command, "RS_MAP_DUOBIYONGDU", new Runnable() {

                @Override
                public void run() {
                    mInterface.switchPlanStyle(IMapInterface.PlanStyle.DUOBIYONGDU);
                    JNIHelper.logd("NavAmapAutoNavImpl start DUOBIYONGDU");
                }
            });
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.BUZOUGAOSU.equals(type)) {
            doRePlanWakeup(type, command, "RS_MAP_BUZOUGAOSU", new Runnable() {

                @Override
                public void run() {
                    mInterface.switchPlanStyle(IMapInterface.PlanStyle.BUZOUGAOSU);
                    JNIHelper.logd("NavAmapAutoNavImpl start BUZOUGAOSU");
                }
            });
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.GAOSUYOUXIAN.equals(type)) {
            doRePlanWakeup(type, command, "RS_MAP_GAOSUYOUXIAN", new Runnable() {

                @Override
                public void run() {
                    mInterface.switchPlanStyle(IMapInterface.PlanStyle.GAOSUYOUXIAN);
                    JNIHelper.logd("NavAmapAutoNavImpl start GAOSUYOUXIAN");
                }
            });
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.MEADWAR_MODE.equals(type)) {
            doConfirmShow(type, command, "RS_MAP_MEADWAR_MODE", new Runnable() {

                @Override
                public void run() {
                    mInterface.switchBroadcastMode(1);
                }
            }, false);
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.EXPORT_MODE.equals(type)) {
            doConfirmShow(type, command, "RS_MAP_EXPERT_MODE", new Runnable() {

                @Override
                public void run() {
                    mInterface.switchBroadcastMode(0);
                }
            }, false);
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.OPEN_DOG.equals(type)) {
            // 打开巡航
            mInterface.switchCruiserStatus(true);
            String spkTxt = NativeData.getResString("RS_VOICE_BD_DOG_NAVI", 0);
            AsrManager.getInstance().setNeedCloseRecord(true);
            RecorderWin.speakTextWithClose(spkTxt, null);
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.CLOSE_DOG.equals(type)) {
            // 关闭巡航
            mInterface.switchCruiserStatus(false);
            String spkTxt = NativeData.getResString("RS_VOICE_CLOSE_DOG");
            AsrManager.getInstance().setNeedCloseRecord(true);
            RecorderWin.speakTextWithClose(spkTxt, null);
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.HOW_NAVI.equals(type)) {
            speakHowNavi(fromWakeup);
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.ASK_REMAIN.equals(type)) {
            speakAskRemain(fromWakeup);
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.BACK_NAVI.equals(type)) {
            mInterface.backNavi();
            AsrManager.getInstance().setNeedCloseRecord(true);
            String tts = NativeData.getResString("RS_MAP_NAV_CONTINUE");
            String spk = NativeData.getResString("RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_BACK_NAV").replace("%CMD%", tts);
            RecorderWin.speakTextWithClose(spk, null);
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.START_NAVI.equals(type)) {
            startNavByInner();
            RecorderWin.close();
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.EXIT_NAV.equals(type)) {
            if (!fromWakeup) {
                AsrManager.getInstance().setNeedCloseRecord(true);
                String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%",
                        NativeData.getResString("RS_MAP_NAV_EXIT"));
                RecorderWin.speakTextWithClose(spk, new Runnable() {

                    @Override
                    public void run() {
                        NavManager.getInstance().exitAllNavTool();
                    }
                });
                return;
            }
            if (!enableWakeupExitNav) {
                return;
            }
            doExitConfirm(type, NativeData.getResString("RS_MAP_NAV_EXIT"), new Runnable() {

                @Override
                public void run() {
                    NavManager.getInstance().exitAllNavTool();
                }
            });
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.CANCEL_NAV.equals(type)) {
            if (preNavCancelCommand(fromWakeup, command)) {
                return;
            }

            if (!fromWakeup && !isInNav()) {
                AsrManager.getInstance().setNeedCloseRecord(true);
                String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%",
                        NativeData.getResString("RS_MAP_NAV_EXIT"));
                RecorderWin.speakTextWithClose(spk, new Runnable() {

                    @Override
                    public void run() {
                        NavManager.getInstance().exitAllNavTool();
                    }
                });
                return;
            }
            doExitConfirm(type,
                    isInNav() ? NativeData.getResString("RS_MAP_NAV_STOP") : NativeData.getResString("RS_MAP_NAV_EXIT"),
                    new Runnable() {

                        @Override
                        public void run() {
                            if (!isInNav()) {
                                NavManager.getInstance().exitAllNavTool();
                                return;
                            }
                            mInterface.naviExit();
                        }
                    });
            return;
        }
        if (TXZAsrKeyManager.AsrKeyType.CLOSE_MAP.equals(type)) {
            if (!fromWakeup) {
                AsrManager.getInstance().setNeedCloseRecord(true);
                String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", command);
                RecorderWin.speakTextWithClose(spk, new Runnable() {

                    @Override
                    public void run() {
                        NavManager.getInstance().exitAllNavTool();
                    }
                });
                return;
            }
            doExitConfirm(type, command, new Runnable() {

                @Override
                public void run() {
                    NavManager.getInstance().exitAllNavTool();
                }
            });
            return;
        }
    }

    /**
     * 查询地图是否前台
     */
    private void queryNavFocus() {
        String sessionId = "t" + System.currentTimeMillis() + "queryNavFocusState";
        BDEntryService.Task task = new BDEntryService.Task();
        task.transaction = sessionId;
        task.callback = new BDEntryService.TaskCallback() {
            @Override
            public void onCallResult(String result) {
                JSONBuilder valBuilder = new JSONBuilder(result);
                Boolean val = valBuilder.getVal("value", Boolean.class);
                if (val != null && val) {
                    onResume();
                } else {
                    onPause();
                }
            }
        };
        BDEntryService.addTaskRunnable(task);
        mInterface.queryNavFocusState(sessionId);
    }

    private void queryNaving() {
        String sessionId = "t" + System.currentTimeMillis() + "queryNavState";
        BDEntryService.Task task = new BDEntryService.Task();
        task.transaction = sessionId;
        task.callback = new BDEntryService.TaskCallback() {
            @Override
            public void onCallResult(String result) {
                JSONBuilder valBuilder = new JSONBuilder(result);
                Boolean val = valBuilder.getVal("value", Boolean.class);
                if (val != null && val) {
                    onStart();
                } else {
                    onEnd(false);
                }
            }
        };
        BDEntryService.addTaskRunnable(task);
        mInterface.queryNavState(sessionId);
    }

    @Override
    public void speakHowNavi(boolean isWakeupResult) {
        String sessionId = "t" + System.currentTimeMillis() + "nextRoad";
        BDEntryService.Task task = new BDEntryService.Task();
        task.transaction = sessionId;
        task.callback = new BDEntryService.TaskCallback() {
            @Override
            public void onCallResult(String result) {
                JSONBuilder jsonBuilder = new JSONBuilder(result);
                String val = jsonBuilder.getVal("value", String.class);
                if (!TextUtils.isEmpty(val)) {
                    AsrManager.getInstance().setNeedCloseRecord(true);
                    RecorderWin.speakTextWithClose(val, null);
                }
            }
        };
        BDEntryService.addTaskRunnable(task);
        mInterface.nextRoad(sessionId);
        sessionId = "t" + System.currentTimeMillis() + "nextTurnDistance";
        task = new BDEntryService.Task();
        task.transaction = sessionId;
        task.callback = new BDEntryService.TaskCallback() {
            @Override
            public void onCallResult(String result) {

            }
        };
        BDEntryService.addTaskRunnable(task);
        mInterface.nextTurnDistance(sessionId);
    }

    @Override
    public void speakAskRemain(boolean isWakeupResult) {
        String sessionId = "t" + System.currentTimeMillis() + "remainLeftDistance";
        BDEntryService.Task task = new BDEntryService.Task();
        task.transaction = sessionId;
        task.callback = new BDEntryService.TaskCallback() {
            @Override
            public void onCallResult(String result) {
                JSONBuilder jsonBuilder = new JSONBuilder(result);
                String value = jsonBuilder.getVal("value", String.class);
                if (!TextUtils.isEmpty(value)) {
                    AsrManager.getInstance().setNeedCloseRecord(true);
                    RecorderWin.speakTextWithClose(value, null);
                }
            }
        };
        BDEntryService.addTaskRunnable(task);
        mInterface.remainDistance(sessionId);

        sessionId = "t" + System.currentTimeMillis() + "remainTime";
        task = new BDEntryService.Task();
        task.transaction = sessionId;
        task.callback = new BDEntryService.TaskCallback() {
            @Override
            public void onCallResult(String result) {
                JSONBuilder jsonBuilder = new JSONBuilder(result);
                String value = jsonBuilder.getVal("value", String.class);
                if (!TextUtils.isEmpty(value)) {
                    AsrManager.getInstance().setNeedCloseRecord(true);
                    RecorderWin.speakTextWithClose(value, null);
                }
            }
        };
        BDEntryService.addTaskRunnable(task);
        mInterface.remainTime(sessionId);
    }

    @Override
    public List<String> getBanCmds() {
        return null;
    }

    @Override
    public List<String> getCmdNavOnly() {
        List<String> cmds = new ArrayList<String>();
        cmds.add(TXZAsrKeyManager.AsrKeyType.CANCEL_NAV);
        cmds.add(TXZAsrKeyManager.AsrKeyType.VIEW_ALL);
        cmds.add(TXZAsrKeyManager.AsrKeyType.TUIJIANLUXIAN);
        cmds.add(TXZAsrKeyManager.AsrKeyType.DUOBIYONGDU);
        cmds.add(TXZAsrKeyManager.AsrKeyType.BUZOUGAOSU);
        cmds.add(TXZAsrKeyManager.AsrKeyType.GAOSUYOUXIAN);
        cmds.add(TXZAsrKeyManager.AsrKeyType.HOW_NAVI);
        cmds.add(TXZAsrKeyManager.AsrKeyType.ASK_REMAIN);
        cmds.add(TXZAsrKeyManager.AsrKeyType.BACK_NAVI);
        cmds.add(TXZAsrKeyManager.AsrKeyType.MEADWAR_MODE);
        cmds.add(TXZAsrKeyManager.AsrKeyType.EXPORT_MODE);
        return cmds;
    }

    @Override
    public void startNavByInner() {
        mInterface.backNavi();
    }

    @Override
    public String[] getSupportCmds() {
        return new String[]{TXZAsrKeyManager.AsrKeyType.ZOOM_IN,
                TXZAsrKeyManager.AsrKeyType.ZOOM_OUT,
                TXZAsrKeyManager.AsrKeyType.NIGHT_MODE,
                TXZAsrKeyManager.AsrKeyType.LIGHT_MODE,
                TXZAsrKeyManager.AsrKeyType.AUTO_MODE,
                TXZAsrKeyManager.AsrKeyType.EXIT_NAV,
                TXZAsrKeyManager.AsrKeyType.CANCEL_NAV,
                TXZAsrKeyManager.AsrKeyType.CLOSE_MAP,
                TXZAsrKeyManager.AsrKeyType.VIEW_ALL,
                TXZAsrKeyManager.AsrKeyType.TUIJIANLUXIAN,
                TXZAsrKeyManager.AsrKeyType.DUOBIYONGDU,
                TXZAsrKeyManager.AsrKeyType.BUZOUGAOSU,
                TXZAsrKeyManager.AsrKeyType.GAOSUYOUXIAN,
                TXZAsrKeyManager.AsrKeyType.HOW_NAVI,
                TXZAsrKeyManager.AsrKeyType.ASK_REMAIN,
                TXZAsrKeyManager.AsrKeyType.BACK_NAVI,
                TXZAsrKeyManager.AsrKeyType.START_NAVI,
                TXZAsrKeyManager.AsrKeyType.OPEN_TRAFFIC,
                TXZAsrKeyManager.AsrKeyType.CLOSE_TRAFFIC,
                TXZAsrKeyManager.AsrKeyType.TWO_MODE,
                TXZAsrKeyManager.AsrKeyType.THREE_MODE,
                TXZAsrKeyManager.AsrKeyType.CAR_DIRECT,
                TXZAsrKeyManager.AsrKeyType.NORTH_DIRECT,
                TXZAsrKeyManager.AsrKeyType.MEADWAR_MODE,
                TXZAsrKeyManager.AsrKeyType.EXPORT_MODE,
                TXZAsrKeyManager.AsrKeyType.OPEN_DOG,
                TXZAsrKeyManager.AsrKeyType.CLOSE_DOG
        };
    }

    /**
     * 获取导航发过来的状态
     *
     * @param content
     */
    private void parseNavState(String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            String method = jsonObject.getString("method");
            if ("NAVI_START".equals(method)) {
                // 开始导航
                onStart();
            } else if ("NAVI_END".equals(method)) {
                // 结束导航
                onEnd(false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 导航引导信息
     */
    private void parseGuideInfo(String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            String method = jsonObject.getString("method");
            String result = jsonObject.getString("result");
            LogUtil.logd("parseGuideInfo me:" + method + ",result:" + result);
            if (mNavInfo == null) {
                mNavInfo = new NavInfo();
            }



            /*
             * 2021/07/31
             * 出现result为空的情况
             */
            //百度返回的数值数据中可能会包含中文单位，导致core异常退出
            if ("TURN_ICONINFO".equals(method)) {
                mNavInfo.dirDes = result;
            } else if ("CUR_ROAD_NAME".equals(method)) {
                mNavInfo.currentRoadName = result;
            } else if ("LEFT_DISTANCE".equals(method)) {
                if(!result.equals("")){
                    mNavInfo.remainDistance = (long)Double.parseDouble(StringUtils.getNumber(result));
                }
            } else if ("LEFT_TIME".equals(method)) {
                if(!result.equals("")){
                    mNavInfo.remainTime = (long)Double.parseDouble(StringUtils.getNumber(result));
                }
            } else if ("NEXT_ROAD_NAME".equals(method)) {
                mNavInfo.nextRoadName = result;
            } else if ("ALL_DISTANCE".equals(method)) {
                if(!result.equals("")){
                    mNavInfo.totalDistance = (long)Double.parseDouble(StringUtils.getNumber(result));
                }
            } else if ("ALL_TIME".equals(method)) {
                if(!result.equals("")){
                    mNavInfo.totalTime = (long)Double.parseDouble(StringUtils.getNumber(result));
                }
            } else if ("CUR_SPEED".equals(method)) {
                if(!result.equals("")){
                    mNavInfo.currentSpeed = (long)Double.parseDouble(StringUtils.getNumber(result));
                }
            } else if ("NEXT_TURN_ICON_DISTANCE".equals(method)) {
                if(!result.equals("")){
                    mNavInfo.dirDistance = (long)Double.parseDouble(StringUtils.getNumber(result));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
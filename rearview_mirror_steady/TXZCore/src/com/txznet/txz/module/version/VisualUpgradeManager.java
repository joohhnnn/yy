package com.txznet.txz.module.version;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.os.HandlerThread;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.txz.push_manager.PushManager;
import com.txz.ui.data.UiData;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.innernet.UiInnerNet;
import com.txz.ui.makecall.UiMakecall;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.dialog2.WinConfirm;
import com.txznet.comm.ui.dialog2.WinConfirmAsr;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.activity.ReserveStandardActivity1;
import com.txznet.sdk.TXZUpgradeManager;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.app.MyInstallReceiver;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.download.DownloadManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.TXZHandler;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * created 2018/5/2 zackzhou
 * 可视化升级模块，用于第三方应用下发安装/升级功能
 * 1. 弹窗确认是否下载
 * 2. 确认后拆包转发给DownloadManager
 * 3. 订阅相关数据刷新可视化下载界面
 * 4. 支持外部工具设置
 * 5. 对话框确认和安装会有超时保护，防止适配无响应导致任务列队阻塞（默认1分钟）
 * 6. 默认安装完后删除安装包
 */
public class VisualUpgradeManager extends IModule {

    private static VisualUpgradeManager sModuleInstance = new VisualUpgradeManager();
    private List<PushManager.NotifyUpgrade> mUpgradeList = new ArrayList<PushManager.NotifyUpgrade>();
    private PushManager.NotifyUpgrade mCurrentUpgrade; // 当前处理中的升级任务
    private String mCurrentUpgradeAppName = ""; // 当前处理中升级
    private HandlerThread mUpgradeThread = null;
    private TXZHandler mUpgradeHandler = null;
    private boolean bDeleteApkAfterInstall = true;

    private final long INSTALL_TIMEOUT = 1000 * 60 * 2; // 安装超时时间
    private final long DIALOG_OPERATION_TIMEOUT = 1000 * 60; // 对话框操作超时时间

    // 对话框取消任务
    private Runnable mRunnableDialogOperationTimeout = new Runnable() {
        @Override
        public void run() {
            if (mCurrentUpgrade != null) {
                JNIHelper.logd("dialog operation pkgName=" + mCurrentUpgrade.pbNotifyUpgrade.strPackageName + ", version=" + mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion);
                UiEquipment.Req_ReportUserOp reportUserOp = new UiEquipment.Req_ReportUserOp();
                reportUserOp.operation = UiEquipment.USER_OP_CANCEL;
                JSONBuilder jBuilder = new JSONBuilder();
                jBuilder.put("type", isUserPullUpgrade ? 2 : 1);
                jBuilder.put("pkg_name", mCurrentUpgrade.pbNotifyUpgrade.strPackageName);
                reportUserOp.strInfo = jBuilder.toBytes();
                JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_REPORT_USER_OP, reportUserOp);
                mCurrentUpgrade = null;
                mCurrentUpgradeAppName = "";
            }
            processUpgradeList();
        }
    };

    // 安装任务超时
    private Runnable mRunnableInstallTimeout = new Runnable() {
        @Override
        public void run() {
            if (mCurrentUpgrade != null) {
                PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_VISUAL_UPGRADE_CUR_PROCESS, null);
                JNIHelper.logd("install timeout pkgName=" + mCurrentUpgrade.pbNotifyUpgrade.strPackageName + ", version=" + mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion);
                // 通知栏同步
                mNotificationTool.notify(mCurrentUpgrade.pbNotifyUpgrade.strPackageName,
                        mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion,
                        mCurrentUpgradeAppName,
                        TXZUpgradeManager.NotificationTool.EVENT_INSTALL_FAILED, null);
                // 数据上报
                UiEquipment.Req_RptInsAppRes reqRptInsAppRes = new UiEquipment.Req_RptInsAppRes();
                reqRptInsAppRes.uint32Stage = UiEquipment.APP_INSTALL_INSTALLING;
                reqRptInsAppRes.uint32ErrCode = UiEquipment.APP_COMMON_EC_UNKNOWN;
                reqRptInsAppRes.strAppName = mCurrentUpgrade.pbNotifyUpgrade.strPackageName.getBytes();
                reqRptInsAppRes.version = mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion.getBytes();
                reqRptInsAppRes.strMsg = "timeout".getBytes();
                JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_REPORT_APP_INS_STATUS, reqRptInsAppRes);
                mCurrentUpgrade = null;
                mCurrentUpgradeAppName = "";
            }
            processUpgradeList();
        }
    };

    private VisualUpgradeManager() {
    }

    public static VisualUpgradeManager getInstance() {
        return sModuleInstance;
    }

    private void createHandler() {
        if (mUpgradeThread == null) {
            mUpgradeThread = new HandlerThread("VisualUpgradeThread");
            mUpgradeThread.start();
            mUpgradeHandler = new TXZHandler(mUpgradeThread.getLooper());
        }
    }

    private void releaseHandler() {
        if (mUpgradeThread != null) {
            mUpgradeThread.quit();
            mUpgradeThread = null;
            mUpgradeHandler = null;
        }
    }

    @Override
    public int initialize_BeforeStartJni() {
        // 注册需要处理的事件
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_APP_DL_INFO); // 请求后台下发的回包
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_APP_DL_INFO); // 后台下发请求
        regEvent(UiEvent.EVENT_INNER_NET, UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_RESP); // 下载结果通知
        regEvent(UiEvent.EVENT_SYSTEM_CALL, UiMakecall.SUBEVENT_INCOMING_CALL_NOTIFY);
        return super.initialize_BeforeStartJni();
    }

    @Override
    public int initialize_AfterStartJni() {
        return super.initialize_AfterStartJni();
    }

    @Override
    public int initialize_AfterInitSuccess() {
        String locTask = PreferenceUtil.getInstance().getString(PreferenceUtil.KEY_VISUAL_UPGRADE_CUR_PROCESS, null);
        if (locTask != null) {
            mCurrentUpgrade = JSON.parseObject(locTask, PushManager.NotifyUpgrade.class);
            mCurrentUpgradeAppName = getUpgradeAppName(mCurrentUpgrade);
            createHandler();
            beginDownload();
        }
        String locList = PreferenceUtil.getInstance().getString(PreferenceUtil.KEY_VISUAL_UPGRADE_TASK_LIST, null);
        if (locList != null) {
            mUpgradeList = JSON.parseArray(locList, PushManager.NotifyUpgrade.class);
            processUpgradeList();
        }
        return super.initialize_AfterInitSuccess();
    }

    @Override
    public int onEvent(int eventId, int subEventId, byte[] data) {
        JNIHelper.logd("eventId=" + eventId + ", subEventId=" + subEventId);
        switch (eventId) {
            case UiEvent.EVENT_SYSTEM_CALL:
                if (UiMakecall.SUBEVENT_INCOMING_CALL_NOTIFY == subEventId) {
                    if (mDialogTool != null && mDialogTool instanceof LocalDialogTool) {
                        ((LocalDialogTool) mDialogTool).dismiss();


                    }
                }
                break;
            case UiEvent.EVENT_ACTION_EQUIPMENT:
                switch (subEventId) {
                    case UiEquipment.SUBEVENT_NOTIFY_APP_DL_INFO: // 后台请求下载
                        if (data != null) {
                            try {
                                final PushManager.NotifyUpgrade notifyUpgrade = PushManager.NotifyUpgrade.parseFrom(data);
                                addTask(notifyUpgrade);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case UiEquipment.SUBEVENT_RESP_APP_DL_INFO: // 请求后台下发资源的回包
                        isUserPullUpgrade = true;
                        break;
                }
                break;
            case UiEvent.EVENT_INNER_NET:
                switch (subEventId) {
                    case UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_RESP: // 资源下载任务状态变化
                        if (data != null) {
                            try {
                                if (mCurrentUpgrade == null) {
                                    break;
                                }
                                final UiInnerNet.DownloadHttpFileTask task = UiInnerNet.DownloadHttpFileTask.parseFrom(data);
                                if (mCurrentUpgrade.pbNotifyUpgrade.strFullDownloadUrl.equals(task.strUrl)) {
                                    DownloadManager.getInstance().unregisterDownloadTaskStatusChangeListener(
                                            ".upgrade_" + mCurrentUpgrade.pbNotifyUpgrade.strPackageName); // 移除下载进度监听
                                    if (UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_SUCCESS == task.int32ResultCode) { // 下载完毕
                                        JSONBuilder jBuilder = new JSONBuilder();
                                        jBuilder.put("file_path", task.strFile);
                                        // 通知栏同步
                                        mNotificationTool.notify(mCurrentUpgrade.pbNotifyUpgrade.strPackageName,
                                                mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion,
                                                mCurrentUpgradeAppName,
                                                TXZUpgradeManager.NotificationTool.EVENT_END_DOWNLOAD, jBuilder.toPostString());

                                        TtsUtil.speakText(mCurrentUpgradeAppName + getVersionTts(mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion) + "下载完成", new TtsUtil.ITtsCallback() {
                                            @Override
                                            public void onEnd() {
                                                super.onEnd();
                                                // 通知栏同步
                                                mNotificationTool.notify(mCurrentUpgrade.pbNotifyUpgrade.strPackageName,
                                                        mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion,
                                                        mCurrentUpgradeAppName,
                                                        TXZUpgradeManager.NotificationTool.EVENT_BEGIN_INSTALL, null);

                                                JNIHelper.logd("checking download file path=" + task.strFile);

                                                // 安装校验
                                                if (!isInstallSafety(task.strFile, mCurrentUpgrade.pbNotifyUpgrade)) {
                                                    return;
                                                }

                                                // 开始安装
                                                if (mApkInstaller == null) {
                                                    mApkInstaller = new LocalApkInstaller();
                                                }
                                                JNIHelper.logd("begin install, installer=" + mApkInstaller.getClass() + ", pkgName=" + mCurrentUpgrade.pbNotifyUpgrade.strPackageName
                                                        + ", version=" + mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion);
                                                mApkInstaller.install(mCurrentUpgrade.pbNotifyUpgrade.strPackageName,
                                                        mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion,
                                                        task.strFile, new TXZUpgradeManager.ApkInstaller.ApkInstallListener() {
                                                            @Override
                                                            public void onSuccess() {
                                                                PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_VISUAL_UPGRADE_CUR_PROCESS, null);
                                                                JNIHelper.logd("install success, pkgName=" + mCurrentUpgrade.pbNotifyUpgrade.strPackageName + ", version=" + mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion);
                                                                mUpgradeHandler.removeCallbacks(mRunnableInstallTimeout);
                                                                // 通知栏同步
                                                                mNotificationTool.notify(mCurrentUpgrade.pbNotifyUpgrade.strPackageName,
                                                                        mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion,
                                                                        mCurrentUpgradeAppName,
                                                                        TXZUpgradeManager.NotificationTool.EVENT_INSTALL_SUCCESS, null);
                                                                // 数据上报
                                                                UiEquipment.Req_RptInsAppRes reqRptInsAppRes = new UiEquipment.Req_RptInsAppRes();
                                                                reqRptInsAppRes.uint32Stage = UiEquipment.APP_INSTALL_INSTALLING;
                                                                reqRptInsAppRes.uint32ErrCode = UiEquipment.APP_COMMON_EC_OK;
                                                                reqRptInsAppRes.strAppName = mCurrentUpgrade.pbNotifyUpgrade.strPackageName.getBytes();
                                                                reqRptInsAppRes.version = mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion.getBytes();
                                                                JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_REPORT_APP_INS_STATUS, reqRptInsAppRes);

                                                                TtsUtil.speakText(mCurrentUpgradeAppName + getVersionTts(mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion) + "已安装成功");

                                                                if (bDeleteApkAfterInstall) {
                                                                    File localApp = new File(task.strFile);
                                                                    if (localApp.exists()) {
                                                                        localApp.delete();
                                                                    }
                                                                }
                                                                mCurrentUpgrade = null;
                                                                mCurrentUpgradeAppName = "";
                                                                processUpgradeList();
                                                            }

                                                            @Override
                                                            public void onFailed(@Nullable String msg) {
                                                                PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_VISUAL_UPGRADE_CUR_PROCESS, null);
                                                                processInstallFailed(msg);
                                                            }
                                                        });
                                                mUpgradeHandler.postDelayed(mRunnableInstallTimeout, INSTALL_TIMEOUT);
                                            }
                                        });

                                        // 数据上报
                                        UiEquipment.Req_RptInsAppRes reqRptInsAppRes = new UiEquipment.Req_RptInsAppRes();
                                        reqRptInsAppRes.uint32Stage = UiEquipment.APP_INSTALL_DOWNLOADING;
                                        reqRptInsAppRes.uint32ErrCode = UiEquipment.APP_COMMON_EC_OK;
                                        reqRptInsAppRes.strAppName = mCurrentUpgrade.pbNotifyUpgrade.strPackageName.getBytes();
                                        reqRptInsAppRes.version = mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion.getBytes();
                                        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_REPORT_APP_INS_STATUS, reqRptInsAppRes);
                                    } else {
                                        // 数据上报
                                        UiEquipment.Req_RptInsAppRes reqRptInsAppRes = new UiEquipment.Req_RptInsAppRes();
                                        reqRptInsAppRes.uint32Stage = UiEquipment.APP_INSTALL_DOWNLOADING;
                                        switch (task.int32ResultCode) {
                                            case UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_HTTP:
                                                reqRptInsAppRes.uint32ErrCode = UiEquipment.APP_COMMON_EC_SERVER;
                                                break;
                                            case UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_IO:
                                                reqRptInsAppRes.uint32ErrCode = UiEquipment.APP_COMMON_EC_IO;
                                                break;
                                            case UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_REQUEST:
                                                reqRptInsAppRes.uint32ErrCode = UiEquipment.APP_COMMON_EC_NETWORK;
                                                break;
                                        }
                                        reqRptInsAppRes.strAppName = mCurrentUpgrade.pbNotifyUpgrade.strPackageName.getBytes();
                                        reqRptInsAppRes.version = mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion.getBytes();
                                        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_REPORT_APP_INS_STATUS, reqRptInsAppRes);

                                        // 请求类型的异常，继续下载
                                        if (UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_REQUEST == task.int32ResultCode) {
                                            beginDownload();
                                        } else {
                                            PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_VISUAL_UPGRADE_CUR_PROCESS, null);
                                            mNotificationTool.notify(mCurrentUpgrade.pbNotifyUpgrade.strPackageName,
                                                    mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion,
                                                    mCurrentUpgradeAppName,
                                                    TXZUpgradeManager.NotificationTool.EVENT_DOWNLOAD_FAILED, null);

                                            TtsUtil.speakText(mCurrentUpgradeAppName + getVersionTts(mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion) + "下载失败");
                                            File localTmp = new File(DownloadManager.DOWNLOAD_FILE_ROOT, task.strTaskId + ".tmp");
                                            if (localTmp.exists()) {
                                                localTmp.delete();
                                            }

                                            mCurrentUpgrade = null;
                                            mCurrentUpgradeAppName = "";
                                            processUpgradeList();
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
                break;
        }
        return super.onEvent(eventId, subEventId, data);
    }


    private void processInstallFailed(String errMsg) {
        JNIHelper.logd("install failed msg=" + errMsg + ", pkgName=" + mCurrentUpgrade.pbNotifyUpgrade.strPackageName + ", version=" + mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion);
        mUpgradeHandler.removeCallbacks(mRunnableInstallTimeout);
        // 通知栏同步
        mNotificationTool.notify(mCurrentUpgrade.pbNotifyUpgrade.strPackageName,
                mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion,
                mCurrentUpgradeAppName,
                TXZUpgradeManager.NotificationTool.EVENT_INSTALL_FAILED, null);
        // 数据上报
        UiEquipment.Req_RptInsAppRes reqRptInsAppRes = new UiEquipment.Req_RptInsAppRes();
        reqRptInsAppRes.uint32Stage = UiEquipment.APP_INSTALL_INSTALLING;
        reqRptInsAppRes.uint32ErrCode = UiEquipment.APP_COMMON_EC_UNKNOWN;
        reqRptInsAppRes.strAppName = mCurrentUpgrade.pbNotifyUpgrade.strPackageName.getBytes();
        reqRptInsAppRes.version = mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion.getBytes();
        reqRptInsAppRes.strMsg = errMsg == null ? null : errMsg.getBytes();
        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_REPORT_APP_INS_STATUS, reqRptInsAppRes);
        mCurrentUpgrade = null;
        mCurrentUpgradeAppName = "";
        processUpgradeList();
    }


    // 插入到升级队列中
    private void addTask(PushManager.NotifyUpgrade notifyUpgrade) {
        String targetPkgName = notifyUpgrade.pbNotifyUpgrade.strPackageName;
        String targetVer = notifyUpgrade.pbNotifyUpgrade.strTargetVersion;

        JNIHelper.logd("add upgrade task: pkgName=" + targetPkgName + ", version=" + targetVer);
        synchronized (mUpgradeList) {

            // 本机已安装版本一致或更高的版本，跳过
            String locVersion = PackageManager.getInstance().getVersionName(targetPkgName);
            if (!TextUtils.isEmpty(locVersion) && isLowerVersionThan(targetVer, locVersion)) {
                JNIHelper.logd("lower version upgrade task, passed: pkgName=" + targetPkgName + ", version=" + targetVer + ", locVer=" + locVersion);
                return;
            }

            // 跟当前处理的任务一致或版本更低，跳过
            if (mCurrentUpgrade != null
                    && mCurrentUpgrade.pbNotifyUpgrade.strPackageName.equals(targetPkgName)) {
                if (mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion.equals(targetVer)) {
                    JNIHelper.logd("duplicate upgrade task, passed: pkgName=" + targetPkgName + ", version=" + targetVer);
                    return;
                }
                if (isLowerVersionThan(targetVer, mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion)) { // 比当前版本任务低
                    JNIHelper.logd("lower version upgrade task, passed: pkgName=" + targetPkgName + ", version=" + targetVer);
                    return;
                }
            }
            // 队列中的重复任务跳过
            for (PushManager.NotifyUpgrade upgrade : mUpgradeList) {
                if (upgrade.pbNotifyUpgrade.strPackageName.equals(targetPkgName)) {
                    if (upgrade.pbNotifyUpgrade.strTargetVersion.equals(targetVer)) {
                        JNIHelper.logd("duplicate upgrade task, passed: pkgName=" + targetPkgName + ", version=" + targetVer);
                        return;
                    }
                    if (isLowerVersionThan(targetVer, upgrade.pbNotifyUpgrade.strTargetVersion)) { // 存在比推送任务高的版本
                        JNIHelper.logd("lower version upgrade task, passed: pkgName=" + targetPkgName + ", version=" + targetVer);
                        return;
                    }
                }
            }
            mUpgradeList.add(notifyUpgrade);
            processUpgradeList();
        }
    }

    // 处理
    private void processUpgradeList() {
        synchronized (mUpgradeList) {
            if (mCurrentUpgrade == null) {
                if (mUpgradeList.isEmpty()) {
                    JNIHelper.logd("all upgrade task completed");
                    PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_VISUAL_UPGRADE_TASK_LIST, null);
                    releaseHandler();
                    return;
                }
                createHandler();
                mCurrentUpgrade = mUpgradeList.remove(0);
                mCurrentUpgradeAppName = getUpgradeAppName(mCurrentUpgrade);
                PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_VISUAL_UPGRADE_TASK_LIST, JSON.toJSONString(mUpgradeList));

                mUpgradeHandler.removeCallbacks(mRunnableProcessCurrentUpgrade);
                mUpgradeHandler.post(mRunnableProcessCurrentUpgrade);
            }
        }
    }

    private String getUpgradeAppName(PushManager.NotifyUpgrade notifyUpgrade) {
        if (!TextUtils.isEmpty(notifyUpgrade.pbNotifyUpgrade.strInstallTips)) {
            String[] infoPart = notifyUpgrade.pbNotifyUpgrade.strInstallTips.split("_");
            if (infoPart.length == 3) {
                return infoPart[1];
            }
        }
        return null;
    }

    private boolean isTXZInited() {
        return AsrManager.getInstance().isInitSuccessed()
                && TtsManager.getInstance().isInitSuccessed()
                && WakeupManager.getInstance().isInitSuccessed();
    }

    private Runnable mRunnableProcessCurrentUpgrade = new Runnable() {
        @Override
        public void run() {
            if (!isTXZInited()
                    || RecorderWin.isOpened() // 录音界面开启
                    || (CallManager.getInstance().hasRemoteProcTool() && !CallManager.getInstance().isIdle())) {
                mUpgradeHandler.removeCallbacks(mRunnableProcessCurrentUpgrade);
                mUpgradeHandler.postDelayed(mRunnableProcessCurrentUpgrade, 2000);
                return;
            }
            synchronized (mUpgradeList) {
                if (mCurrentUpgrade == null) {
                    return;
                }
                JNIHelper.logd("start upgrade task: pkgName=" + mCurrentUpgrade.pbNotifyUpgrade.strPackageName + ", version=" + mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion);
                showDownloadConfirmDialog(mCurrentUpgrade);
            }
        }
    };

    private boolean isUserPullUpgrade; // 是否用户主动拉取的升级，上报用

    // 弹出确认提示框
    private void showDownloadConfirmDialog(final PushManager.NotifyUpgrade notifyUpgrade) {
        String appName = "";
        String tip = "";
        String version = notifyUpgrade.pbNotifyUpgrade.strTargetVersion;
        if (!TextUtils.isEmpty(notifyUpgrade.pbNotifyUpgrade.strInstallTips)) {
            String[] infoPart = notifyUpgrade.pbNotifyUpgrade.strInstallTips.split("_");
            if (infoPart.length == 3) {
                tip = infoPart[0];
                appName = infoPart[1];
                version = infoPart[2];
            }
        }

        String totalSize = "0M";
        try {
            if (mCurrentUpgrade != null) {
                totalSize = getFormatSize(getRemoteFileTotalSize(mCurrentUpgrade.pbNotifyUpgrade.strFullDownloadUrl));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String typeName = PackageManager.getInstance().isNavApp(notifyUpgrade.pbNotifyUpgrade.strPackageName) ? "导航" : "";
        String action = mCurrentUpgrade.pbNotifyUpgrade.strOldMd5 == null ? "下载" : "升级";
        JSONBuilder jBuilder = new JSONBuilder();
        jBuilder.put("pkgName", notifyUpgrade.pbNotifyUpgrade.strPackageName);
        jBuilder.put("version", notifyUpgrade.pbNotifyUpgrade.strTargetVersion);
        jBuilder.put("appName", appName);
        jBuilder.put("totalSize", totalSize);

        mDialogTool.showConfirmDialog(typeName + action,
                tip,
                jBuilder.toPostString(),
                new TXZUpgradeManager.UpgradeDialogTool.DialogListener() {
                    private boolean bOk;

                    @Override
                    public void onClickOk() {
                        bOk = true;
                    }

                    @Override
                    public void onClickCancel() {
                        bOk = false;
                    }

                    @Override
                    public void onDismiss() {
                        mUpgradeHandler.removeCallbacks(mRunnableDialogOperationTimeout);
                        // 数据上报
                        UiEquipment.Req_ReportUserOp reportUserOp = new UiEquipment.Req_ReportUserOp();
                        reportUserOp.operation = bOk ? UiEquipment.USER_OP_CONFIRM : UiEquipment.USER_OP_CANCEL;
                        JSONBuilder jBuilder = new JSONBuilder();
                        jBuilder.put("type", isUserPullUpgrade ? 2 : 1);
                        jBuilder.put("pkg_name", notifyUpgrade.pbNotifyUpgrade.strPackageName);
                        reportUserOp.strInfo = jBuilder.toBytes();
                        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_REPORT_USER_OP, reportUserOp);
                        if (bOk) {
                            // 通知栏同步
                            mNotificationTool.notify(notifyUpgrade.pbNotifyUpgrade.strPackageName,
                                    notifyUpgrade.pbNotifyUpgrade.strTargetVersion,
                                    mCurrentUpgradeAppName,
                                    TXZUpgradeManager.NotificationTool.EVENT_BEGIN_DOWNLOAD, null);
                            // 开始下载
                            beginDownload();
                        } else {
                            mCurrentUpgrade = null;
                            mCurrentUpgradeAppName = "";
                            processUpgradeList();
                        }
                    }
                });
        mUpgradeHandler.postDelayed(mRunnableDialogOperationTimeout, DIALOG_OPERATION_TIMEOUT);
    }

    // 获取远端资源大小
    private long getRemoteFileTotalSize(String downloadUrl) throws IOException {
        if (downloadUrl == null || "".equals(downloadUrl)) {
            return 0L;
        }
        URL url = new URL(downloadUrl);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            if (conn.getResponseCode() == 200) {
                return (long) conn.getContentLength();
            }
        } catch (IOException e) {
            return 0L;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return 0;
    }

    // 格式化
    private String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }
        double megaByte = kiloByte / 1024;
        BigDecimal result = new BigDecimal(Double.toString(megaByte));
        return result.setScale(1, BigDecimal.ROUND_HALF_UP)
                .toPlainString() + "M";
    }


    // retry count;
    private int mRetryCount = 0;

    // 开始下载
    private void beginDownload() {
        mUpgradeHandler.removeCallbacks(mRunnableBeginDownload);
        long delay = (long) Math.pow(mRetryCount, 2) * 1000;
        if (delay > 1000 * 30) {
            delay = 1000 * 30;
        }
        mUpgradeHandler.postDelayed(mRunnableBeginDownload, delay);
        mRetryCount++;
    }

    private Runnable mRunnableBeginDownload = new Runnable() {
        @Override
        public void run() {
            if (mCurrentUpgrade == null) {
                return;
            }
            PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_VISUAL_UPGRADE_CUR_PROCESS, JSON.toJSONString(mCurrentUpgrade)); // 序列化当前任务
            UiInnerNet.DownloadHttpFileTask task = new UiInnerNet.DownloadHttpFileTask();
            task.strUrl = mCurrentUpgrade.pbNotifyUpgrade.strFullDownloadUrl;
            task.strTaskId = ".upgrade_" + mCurrentUpgrade.pbNotifyUpgrade.strPackageName;
            task.bForbidUseReservedSpace = true; // 禁止使用预留空间
            JNIHelper.sendEvent(UiEvent.EVENT_INNER_NET, UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_REQ, task);
            DownloadManager.getInstance().registerDownloadTaskStatusChangeListener(task.strTaskId, new DownloadManager.DownloadTaskProgressChangeListener() {
                @Override
                public void onProgressChange(UiInnerNet.DownloadHttpFileTask task) {
                    mRetryCount = 0;
                    JSONBuilder jBuilder = new JSONBuilder();
                    jBuilder.put("progress", task.uint32DlProgress);
                    mNotificationTool.notify(mCurrentUpgrade.pbNotifyUpgrade.strPackageName,
                            mCurrentUpgrade.pbNotifyUpgrade.strTargetVersion,
                            mCurrentUpgradeAppName,
                            TXZUpgradeManager.NotificationTool.EVENT_PROGRESS_CHANGE, jBuilder.toPostString());
                }
            });
        }
    };


    public void reqAppDownload(int appType) {
        reqAppDownload(appType, null, null);
    }

    /**
     * 请求下载
     *
     * @param appType app的类型, AppType中定义的值
     * @param pkgName app的包名
     * @param version app的当前版本(在客户端的版本) 客户端当前app没有就是0
     */
    public void reqAppDownload(int appType, @Nullable String pkgName, @Nullable String version) {
        JNIHelper.logd("reqAppDownload appType=" + appType + ", pkgName=" + pkgName + ", version=" + version);
        UiEquipment.Req_AppDlInfo reqAppDlInfo = new UiEquipment.Req_AppDlInfo();
        reqAppDlInfo.appType = appType;
        if (pkgName != null) {
            reqAppDlInfo.appPkgName = pkgName.getBytes();
        }
        reqAppDlInfo.appVersion = (version == null ? "0" : version).getBytes();
        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_APP_DL_INFO, reqAppDlInfo);
    }

    // 应用安装默认实现
    private class LocalApkInstaller implements TXZUpgradeManager.ApkInstaller {
        private String mInstallPkg; // 安装中包名
        private String mInstallVersion; // 安装中包名
        private ApkInstallListener mInstallListener;

        private BroadcastReceiver mInstallerViewCloseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mInstallListener != null && mInstallPkg != null) {
                    if (!PackageManager.getInstance().checkAppExist(mInstallPkg)) {
                        mInstallListener.onFailed(null);
                    }
                }
            }
        };

        private MyInstallReceiver.InstallObservable.InstallObserver mInstallObserver = new MyInstallReceiver.InstallObservable.InstallObserver() {
            @Override
            public void onApkInstall(String packageName) {
                if (packageName.equals(mInstallPkg)) {
                    UiData.AppInfo packInfo = PackageManager.getInstance().getAppInfo(packageName);
                    if (packInfo.strVersion.equals(mInstallVersion)) {
                        if (mInstallListener != null) {
                            mInstallListener.onSuccess();
                            mInstallPkg = null;
                            mInstallListener = null;
                        }
                    } else {
                        mInstallListener.onFailed("version invalid, loc_ver=" + packInfo.strVersion + ", push_ver=" + mInstallVersion);
                        mInstallPkg = null;
                        mInstallListener = null;
                    }
                }
            }

            @Override
            public void onApkUnInstall(String packageName) {

            }
        };


        private LocalApkInstaller() {
            LocalBroadcastManager.getInstance(AppLogic.getApp()).registerReceiver(mInstallerViewCloseReceiver,
                    new IntentFilter("com.txznet.txz.action.INSTALLER_CLOSE"));
            MyInstallReceiver.SINSTALL_OBSERVABLE.registerObserver(mInstallObserver);
        }

        @Override
        public void install(@NonNull String pkgName, @NonNull String version, @NonNull String apkPath, @Nullable ApkInstallListener listener) {
            // FIXME 多任务实现
            mInstallPkg = pkgName;
            mInstallVersion = version;
            mInstallListener = listener;
            Intent intent = new Intent(GlobalContext.get(), ReserveStandardActivity1.class);
            intent.putExtra("url", apkPath);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            GlobalContext.get().startActivity(intent);
        }

        public void release() {
            LocalBroadcastManager.getInstance(AppLogic.getApp()).unregisterReceiver(mInstallerViewCloseReceiver);
            MyInstallReceiver.SINSTALL_OBSERVABLE.unregisterObserver(mInstallObserver);
        }
    }

    // 远程实现
    private class RemoteApkInstaller implements TXZUpgradeManager.ApkInstaller {
        private String mRemotePkgName;
        private final HashMap<String, ApkInstallListener> mInstallListenerMapper = new HashMap<String, ApkInstallListener>();

        private RemoteApkInstaller(String remotePkgName) {
            this.mRemotePkgName = remotePkgName;
        }

        @Override
        public void install(@NonNull String pkgName, @NonNull String version, @NonNull String apkPath, @NonNull ApkInstallListener listener) {
            synchronized (mInstallListenerMapper) {
                mInstallListenerMapper.put(pkgName + version, listener);
            }
            Parcel p = Parcel.obtain();
            p.writeString(pkgName);
            p.writeString(version);
            p.writeString(apkPath);
            ServiceManager.getInstance().sendInvoke(mRemotePkgName, UPGRADE_CMD_PREFIX + TOOL_INSTALLER_PREFIX + CMD_INSTALL, p.marshall(), null);
            p.recycle();
        }

        private void processInvoke(String packageName, String command, byte[] data) {
            Parcel p = Parcel.obtain();
            if (null != data) {
                p.unmarshall(data, 0, data.length);
                p.setDataPosition(0);
            }
            if (INVOKE_INSTALL_SUCCESS.equals(command)) {
                JNIHelper.logd("remote installer install success");
                final String pkgName = p.readString();
                final String version = p.readString();
                String key = pkgName + version;
                synchronized (mInstallListenerMapper) {
                    if (mInstallListenerMapper.containsKey(key)) {
                        mInstallListenerMapper.get(key).onSuccess();
                        mInstallListenerMapper.remove(key);
                    }
                }
            } else if (INVOKE_INSTALL_FAILED.equals(command)) {
                JNIHelper.logd("remote installer install failed");
                final String pkgName = p.readString();
                final String version = p.readString();
                final String msg = p.readString();
                String key = pkgName + version;
                synchronized (mInstallListenerMapper) {
                    if (mInstallListenerMapper.containsKey(key)) {
                        mInstallListenerMapper.get(key).onFailed(msg);
                        mInstallListenerMapper.remove(key);
                    }
                }
            }
            p.recycle();
        }
    }

    private TXZUpgradeManager.ApkInstaller mApkInstaller;


    // 默认通知实现
    private class LocalNotificationTool implements TXZUpgradeManager.NotificationTool {

        @Override
        public void notify(@NonNull String pkgName, @NonNull String version, @NonNull String appName, int event, @Nullable String data) {

        }

        @Override
        public void cancelAll() {

        }
    }

    // 远程实现
    private class RemoteNotificationTool implements TXZUpgradeManager.NotificationTool {
        private String mRemotePkgName;

        private RemoteNotificationTool(String pkgName) {
            mRemotePkgName = pkgName;
        }

        @Override
        public void notify(@NonNull String pkgName, @NonNull String version, @NonNull String appName, int event, @Nullable String data) {
            Parcel p = Parcel.obtain();
            p.writeString(pkgName);
            p.writeString(version);
            p.writeString(mCurrentUpgradeAppName);
            p.writeInt(event);
            p.writeString(data);
            ServiceManager.getInstance().sendInvoke(mRemotePkgName, UPGRADE_CMD_PREFIX + TOOL_NOTIFICATION_PREFIX + CMD_NOTIFY, p.marshall(), null);
            p.recycle();
        }

        @Override
        public void cancelAll() {

        }
    }

    private TXZUpgradeManager.NotificationTool mNotificationTool = new LocalNotificationTool();


    private class LocalDialogTool implements TXZUpgradeManager.UpgradeDialogTool {
        private WinConfirm mWinConfirm;
        private DialogListener mListener;

        @Override
        public void showConfirmDialog(final String title, final String content, String detailInfo, DialogListener listener) {
            WinConfirmAsr.WinConfirmAsrBuildData buildData = new WinConfirmAsr.WinConfirmAsrBuildData();
            buildData.setTitleText(title);
            String totalSize = "0M";
            try {
                totalSize = new JSONBuilder(detailInfo).getJSONObject().getString("totalSize");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            buildData.setMessageText(content + "(" + totalSize + ")");
            buildData.setSureText("确定", new String[]{"是", "确定", "好的"});
            buildData.setCancelText("取消", new String[]{"否", "取消", "不要"});
            buildData.setHintTts(getContentTts(content));
            mListener = listener;
            mWinConfirm = new WinConfirmAsr(buildData) {
                @Override
                public String getReportDialogId() {
                    return "visual_upgrade";
                }

                @Override
                public void onClickOk() {
                    mListener.onClickOk();
                    mListener.onDismiss();
                }

                @Override
                public void onClickCancel() {
                    mListener.onClickCancel();
                    mListener.onDismiss();
                }
            };
            mWinConfirm.show();
            mWinConfirm.clickCancelCountDown(20);
        }

        public void dismiss() {
            if (mListener != null){
                mListener.onDismiss();
                mListener = null;
            }
            if (mWinConfirm != null) {
                mWinConfirm.dismiss("INCOMING_CALL");
                mWinConfirm = null;
            }
        }
    }

    private class RemoteDialogTool implements TXZUpgradeManager.UpgradeDialogTool {
        private String mRemotePkgName;
        private final HashMap<String, DialogListener> mDialogListenerMapper = new HashMap<String, DialogListener>();

        private RemoteDialogTool(String pkgName) {
            mRemotePkgName = pkgName;
        }

        @Override
        public void showConfirmDialog(String title, String content, String detailInfo, DialogListener listener) {
            JNIHelper.logd("show remote confirm dialog title=" + title + ", content=" + content + ", detailInfo=" + detailInfo);
            synchronized (mDialogListenerMapper) {
                mDialogListenerMapper.put(detailInfo, listener);
            }
            Parcel p = Parcel.obtain();
            p.writeString(title);
            p.writeString(content);
            p.writeString(detailInfo);
            ServiceManager.getInstance().sendInvoke(mRemotePkgName, UPGRADE_CMD_PREFIX + TOOL_DIALOG_PREFIX + CMD_SHOW, p.marshall(), null);
            p.recycle();
        }

        private void processInvoke(String packageName, String command, byte[] data) {
            Parcel p = Parcel.obtain();
            if (null != data) {
                p.unmarshall(data, 0, data.length);
                p.setDataPosition(0);
            }
            String detailInfo = p.readString();
            if (INVOKE_DIALOG_CONFIRM.equals(command)) {
                JNIHelper.logd("remote dialog click onConfirm");
                synchronized (mDialogListenerMapper) {
                    if (mDialogListenerMapper.containsKey(detailInfo)) {
                        mDialogListenerMapper.get(detailInfo).onClickOk();
                    }
                }
            } else if (INVOKE_DIALOG_CANCEL.equals(command)) {
                JNIHelper.logd("remote dialog click onCancel");
                synchronized (mDialogListenerMapper) {
                    if (mDialogListenerMapper.containsKey(detailInfo)) {
                        mDialogListenerMapper.get(detailInfo).onClickCancel();
                    }
                }
            } else if (INVOKE_DIALOG_DISMISS.equals(command)) {
                JNIHelper.logd("remote dialog click onDismiss");
                synchronized (mDialogListenerMapper) {
                    if (mDialogListenerMapper.containsKey(detailInfo)) {
                        mDialogListenerMapper.get(detailInfo).onDismiss();
                        mDialogListenerMapper.remove(detailInfo);
                    }
                }
            }
            p.recycle();
        }
    }

    private TXZUpgradeManager.UpgradeDialogTool mDialogTool = new LocalDialogTool();


    private static final String UPGRADE_CMD_PREFIX = "txz.upgrade.cmd."; // TXZCore -> SDK
    private static final String UPGRADE_INVOKE_PREFIX = "txz.upgrade.invoke."; // SDK -> TXZCore

    // TXZCore::VisualUpgradeManager/SDK 双向同步，请勿修改

    // common
    private static final String INVOKE_DELETE_APK_AFTER_INSTALL = "delete_apk_after_install";

    // dialog
    private static final String INVOKE_SET_UPGRADE_DIALOG = "set_upgrade_dialog";
    private static final String INVOKE_CLEAR_UPGRADE_DIALOG = "clear_upgrade_dialog";
    private static final String TOOL_DIALOG_PREFIX = "dialog.";
    private static final String CMD_SHOW = "show";
    private static final String INVOKE_DIALOG_CONFIRM = TOOL_DIALOG_PREFIX + "dialog_confirm";
    private static final String INVOKE_DIALOG_CANCEL = TOOL_DIALOG_PREFIX + "dialog_cancel";
    private static final String INVOKE_DIALOG_DISMISS = TOOL_DIALOG_PREFIX + "dialog_dismiss";

    // notification
    private static final String INVOKE_SET_NOTIFICATION_TOOL = "set_notification_tool";
    private static final String INVOKE_CLEAR_NOTIFICATION_TOOL = "clear_notification_tool";
    private static final String TOOL_NOTIFICATION_PREFIX = "notification.";
    private static final String CMD_NOTIFY = "notify";

    // installer
    private static final String INVOKE_SET_APK_INSTALLER = "set_apk_installer";
    private static final String INVOKE_CLEAR_APK_INSTALLER = "clear_apk_installer";
    private static final String TOOL_INSTALLER_PREFIX = "installer.";
    private static final String CMD_INSTALL = "install";
    private static final String INVOKE_INSTALL_SUCCESS = TOOL_INSTALLER_PREFIX + "install_succ";
    private static final String INVOKE_INSTALL_FAILED = TOOL_INSTALLER_PREFIX + "install_failed";

    // 远程命令处理
    public byte[] processInvoke(String packageName, String command, byte[] data) {
        if (command.startsWith(TOOL_INSTALLER_PREFIX)) {
            if (mApkInstaller != null && mApkInstaller instanceof RemoteApkInstaller) {
                ((RemoteApkInstaller) mApkInstaller).processInvoke(packageName, command, data);
            }
        }
        if (command.startsWith(TOOL_DIALOG_PREFIX)) {
            if (mDialogTool != null && mDialogTool instanceof RemoteDialogTool) {
                ((RemoteDialogTool) mDialogTool).processInvoke(packageName, command, data);
            }
        }
        Parcel p = Parcel.obtain();
        if (null != data) {
            p.unmarshall(data, 0, data.length);
            p.setDataPosition(0);
        }
        if (INVOKE_SET_APK_INSTALLER.equals(command)) {
            JNIHelper.logd("set remote installer");
            if (mApkInstaller != null && mApkInstaller instanceof LocalApkInstaller) {
                ((LocalApkInstaller) mApkInstaller).release();
            }
            mApkInstaller = new RemoteApkInstaller(packageName);
        } else if (INVOKE_CLEAR_APK_INSTALLER.equals(command)) {
            JNIHelper.logd("clear remote installer");
            mApkInstaller = new LocalApkInstaller();
        }
        if (INVOKE_SET_NOTIFICATION_TOOL.equals(command)) {
            JNIHelper.logd("set remote notification tool");
            mNotificationTool = new RemoteNotificationTool(packageName);
        } else if (INVOKE_CLEAR_NOTIFICATION_TOOL.equals(command)) {
            JNIHelper.logd("clear remote notification tool");
            mNotificationTool = new LocalNotificationTool();
        }
        if (INVOKE_SET_UPGRADE_DIALOG.equals(command)) {
            JNIHelper.logd("set remote dialog tool");
            mDialogTool = new RemoteDialogTool(packageName);
        } else if (INVOKE_CLEAR_UPGRADE_DIALOG.equals(command)) {
            JNIHelper.logd("clear remote dialog tool");
            mDialogTool = new LocalDialogTool();
        }
        if (INVOKE_DELETE_APK_AFTER_INSTALL.equals(command)) {
            bDeleteApkAfterInstall = p.readByte() == 1;
        }
        p.recycle();
        return null;
    }

    // 校验本地apk版本是否一致
    private boolean checkLocalApkVersion(String apkPath, String version) {
        android.content.pm.PackageManager pm = GlobalContext.get().getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkPath, android.content.pm.PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            if (pkgInfo.versionName.equals(version)) {
                return true;
            } else {
                JNIHelper.logd("checkLocalApkVersion failed, loc_version=" + pkgInfo.versionName + ", push_version=" + version);
            }
        }
        return false;
    }

    // 校验本地apk包名是否一致
    private boolean checkLocalApkPkgName(String apkPath, String pkgName) {
        android.content.pm.PackageManager pm = GlobalContext.get().getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkPath, android.content.pm.PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            if (pkgInfo.packageName.equals(pkgName)) {
                return true;
            } else {
                JNIHelper.logd("checkLocalApkPkgName failed, loc_pkgname=" + pkgInfo.packageName + ", push_pkgname=" + pkgName);
            }
        }
        return false;
    }

    // 校验本地apk md5是否一致
    private boolean checkLocalApkMD5(String apkPath, String md5) {
        String localMd5 = getLocalFileMD5(apkPath);
        if (localMd5.equals(md5)) {
            return true;
        } else {
            JNIHelper.logd("checkLocalApkMD5 failed, local_md5=" + localMd5 + ", remote_md5=" + md5);
        }
        return false;
    }

    private String encryptionMD5(byte[] byteStr) {
        MessageDigest messageDigest = null;
        StringBuilder md5StrBuff = new StringBuilder();
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(byteStr);
            byte[] byteArray = messageDigest.digest();
            for (byte aByteArray : byteArray) {
                if (Integer.toHexString(0xFF & aByteArray).length() == 1) {
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF & aByteArray));
                } else {
                    md5StrBuff.append(Integer.toHexString(0xFF & aByteArray));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5StrBuff.toString();
    }

    // 获取文件MD5
    private String getLocalFileMD5(String path) {
        BigInteger bi = null;
        try {
            byte[] buffer = new byte[8192];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");
            File f = new File(path);
            FileInputStream fis = new FileInputStream(f);
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fis.close();
            byte[] b = md.digest();
            bi = new BigInteger(1, b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bi.toString(16);
    }

    // 获取tts播报
    private String getVersionTts(String oriVersion) {
        StringBuilder tts = new StringBuilder();
        String[] splitArr = oriVersion.split("\\.");
        for (String splitPart : splitArr) {
            if (tts.length() > 0) {
                tts.append(".");
            }
            for (int i = 0; i < splitPart.length(); i++) {
                if (i > 0) {
                    tts.append(",");
                }
                tts.append(splitPart.charAt(i));
            }
        }
        return tts.toString();
    }

    // 获取tts播报
    private String getContentTts(String oriContent) {
        Pattern pattern = Pattern.compile("(\\d+\\.)+\\d+[A-Z]?");
        Matcher matcher = pattern.matcher(oriContent);

        String version = "";
        while (matcher.find()) {
            version = matcher.group();
            break; // 只匹配一个
        }
        oriContent = oriContent.replace(version, "$hold$");
        return oriContent.replace("$hold$", getVersionTts(version));
    }

    // 安装校验
    private boolean isInstallSafety(String apkPath, PushManager.PushCmd_NotifyUpgrade upgrade) {
        if (!checkLocalApkPkgName(apkPath, upgrade.strPackageName)) {
            processInstallFailed("install not safety, pkgName may be wrong");
            return false;
        }

        if (!checkLocalApkVersion(apkPath, upgrade.strTargetVersion)) {
            processInstallFailed("install not safety, version may be wrong");
            return false;
        }

        // FIXME 需要计算整个包，耗时
//        if (upgrade.strNewMd5 != null && !checkLocalApkMD5(apkPath, new String(upgrade.strNewMd5))) {
//            processInstallFailed("install not safety, md5 may be wrong");
//            return false;
//        }
        return true;
    }

    // 比较两个应用名高低
    private static boolean isLowerVersionThan(String lhsVer, String rhsVer) {
        lhsVer = lhsVer.replaceAll("\\D+", "");
        rhsVer = rhsVer.replaceAll("\\D+", "");
        String[] lhsParts = lhsVer.split("\\.");
        String[] rhsParts = rhsVer.split("\\.");
        if (lhsParts.length > rhsParts.length) {
            return false;
        }
        boolean isFullSame = false;
        for (int i = 0, len = lhsParts.length; i < len; i++) {
            Integer lhsPart = Integer.parseInt(lhsParts[i]);
            Integer rhsPart = Integer.parseInt(rhsParts[i]);
            isFullSame |= lhsPart.equals(rhsPart);
            if (lhsPart > rhsPart) {
                return false;
            }
        }
        return !isFullSame;
    }
}

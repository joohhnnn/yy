package com.txznet.txz.module.version;

import android.content.pm.PackageInfo;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.push_manager.PushManager;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.innernet.UiInnerNet;
import com.txznet.comm.base.BaseApplication;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.ui.dialog2.WinConfirm;
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.comm.ui.dialog2.WinNotice;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdkinner.TXZInnerUpgradeManager;
import com.txznet.txz.INoProguard;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.download.DownloadManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.ui.widget.UpgradeDialog;
import com.txznet.txz.ui.widget.UpgradeFloatView;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.PackageInstaller;
import com.txznet.txz.util.SDCardUtil;
import com.txznet.txz.util.TXZHandler;
import com.txznet.txz.util.runnables.Runnable1;

/**
 * 版本管理模块，负责版本数据管理，版本更新
 * 1.push升级
 * 2.重启恢复升级
 * // * 3.设置升级
 * // * 4.声控升级
 *
 * @author User
 */
public class UpgradeManager extends IModule {
    static UpgradeManager sModuleInstance = new UpgradeManager();

    private UpgradeManager() {
        // 读取系统里其他包的版本
    }

    public static UpgradeManager getInstance() {
        return sModuleInstance;
    }

    // /////////////////////////////////////////////////////////////////////////

    private HandlerThread mUpgradeThread;
    private TXZHandler mUpgradeHandler;

    private void createHandler() {
        if (mUpgradeThread == null) {
            mUpgradeThread = new HandlerThread("UpgradeThread");
            mUpgradeThread.start();
            mUpgradeHandler = new TXZHandler(
                    mUpgradeThread.getLooper());
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
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
                UiEquipment.SUBEVENT_NOTIFY_INSTALL_APK);
        regEvent(UiEvent.EVENT_INNER_NET, UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_RESP);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_APP_UPGRADE_INFO); // 后台下发请求
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_INIT_SUCCESS);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_APP_DL_INFO);
        return super.initialize_BeforeStartJni();
    }

    @Override
    public int initialize_AfterStartJni() {
        createHandler();

        checkUpgradeFinishInner();

        return super.initialize_AfterStartJni();
    }

    @Override
    public int onEvent(int eventId, int subEventId, byte[] data) {
        if (eventId == UiEvent.EVENT_ACTION_EQUIPMENT) {
            switch (subEventId) {
                case UiEquipment.SUBEVENT_NOTIFY_INSTALL_APK:
                    // TODO 打开升级询问窗口或者静默升级
                    try {
                        UiEquipment.Notify_InstallApk pbNotifyInstallApk = UiEquipment.Notify_InstallApk
                                .parseFrom(data);
                        JNIHelper.logi("package="
                                + pbNotifyInstallApk.strPackageName
                                + "need to install to version="
                                + pbNotifyInstallApk.strVersion + ", path="
                                + pbNotifyInstallApk.strApkPath);
                        procInstallApk(pbNotifyInstallApk);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case UiEquipment.SUBEVENT_NOTIFY_APP_UPGRADE_INFO: // 后台请求下载
                    if (data != null) {
                        mUpgradeHandler.post(new Runnable1<byte[]>(data) {
                            @Override
                            public void run() {
                                try {
                                    final PushManager.NotifyUpgrade notifyUpgrade = PushManager.NotifyUpgrade.parseFrom(mP1);

                                    UpgradeInfo mUpgradeInfo = new UpgradeInfo();
                                    mUpgradeInfo.pbUpgrade = notifyUpgrade.pbNotifyUpgrade;
                                    mUpgradeInfo.mFromType = TYPE_FROM_PUSH;

                                    long delayTime = 0;
                                    //处理继续升级时也是从后台推送下来的情况
                                    if (mUpgradeInfo.pbUpgrade.strUserParam != null) {
                                        JSONBuilder jsonBuilder = new JSONBuilder(mUpgradeInfo.pbUpgrade.strUserParam);
                                        LogUtil.logd("params:" + jsonBuilder.toString());
                                        if (jsonBuilder.getVal("is_continue", Integer.class, 0) == 1) {
                                            mUpgradeInfo.mFromType = TYPE_FROM_CONTINUE;
                                        } else {
                                            mUpgradeInfo.mFromType = TYPE_FROM_PUSH;
                                        }
                                        //延时多少毫秒，后台增加的是秒
                                        delayTime = jsonBuilder.getVal("delay_time", Integer.class, 0) * 1000L;
                                    }

                                    //延时添加到队列中
                                    mUpgradeHandler.postDelayed(new Runnable1<UpgradeInfo>(mUpgradeInfo) {
                                        @Override
                                        public void run() {
                                            addUpgradeTask(mP1);
                                        }
                                    }, delayTime);

                                } catch (Exception e) {
                                }
                            }
                        });
                    }
                    break;
            }

        } else if (eventId == UiEvent.EVENT_INNER_NET) {
            switch (subEventId) {
                case UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_RESP:
                    mUpgradeHandler.post(new Runnable1<byte[]>(data) {
                        @Override
                        public void run() {
                            try {
                                UiInnerNet.DownloadHttpFileTask downloadHttpFileTask = UiInnerNet.DownloadHttpFileTask.parseFrom(mP1);
                                onDownloadFinish(downloadHttpFileTask);
                            } catch (InvalidProtocolBufferNanoException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    break;
            }
        }

        return super.onEvent(eventId, subEventId, data);
    }


    final static int FLAG_FORCE_HINT_UPGRADE = 0x1; // 强制提示升级
    final static int FLAG_FORCE_INSTALL_INNER_APK = 0x2; // 强制内部apk升级
    final static int FLAG_FORCE_INSTALL = 0x4; //强制升级

    HashMap<String, WinDialog> mMapUpgradeWins = new HashMap<String, WinDialog>();

    //升级队列
    private final HashMap<String, UpgradeInfo> mapUpgradeInfo = new HashMap<String, UpgradeInfo>();
    //升级队列刷新时间
    private long PROC_UPGRADE_TIME = 30000;
    //检测升级完成时间
    private long PROC_CHECK_UPGRADE_FINISH_TIME = 60000;

    public final static String ACTION_NOTIFY_UPGRADE = "com.txznet.txz.ACTION_NOTIFY_UPGRADE";

    //升级下载任务的前缀2.0版本
    public static final String DOWNLOAD_TASK_PRE = ".upgrade_2_";

    //下载状态
    public static final int UPGRADE_STATE_NONE = 0; //初始化状态
    public static final int UPGRADE_STATE_DOWNLOADING = 1; //正在下载
    public static final int UPGRADE_STATE_DOWNLOAD_STOP = 2; //停止下载
    public static final int UPGRADE_STATE_DOWNLOAD_FAIL = 3;//下载失败
    public static final int UPGRADE_STATE_DOWNLOAD_FINISH = 4;//下载完成
    public static final int UPGRADE_STATE_INSTALLING = 5;//安装中，安装中的状态不好判断，使用下载完成代替
    public static final int UPGRADE_STATE_INSTALL_FAIL = 6;//安装成功
    public static final int UPGRADE_STATE_INSTALL_FINISH = 7;//安装失败
    public static final int UPGRADE_STATE_DIALOG_SHOWING = 8;//显示对话框
    public static final int UPGRADE_STATE_DOWNLOAD_WAITING = 9;//下载等待中

    //升级在哪里触发的
    public static final int TYPE_FROM_NONE = 0;
    public static final int TYPE_FROM_PUSH = 1;
    public static final int TYPE_FROM_VOICE = 2;
    public static final int TYPE_FROM_SETTINGS = 3;
    public static final int TYPE_FROM_CONTINUE = 4;

    private TXZInnerUpgradeManager.UpgradeDialogTool mUpgradeDialogTool = new LocalDialogTool();
    private TXZInnerUpgradeManager.NotificationTool mNotificationTool = new LocalNotificationTool();
    private UpgradeFloatView mUpgradeFloatView = null;

    //sdk通知当前繁忙，不允许升级
    private boolean mSdkBusy = false;

    /**********************************************************************************************
     ********************************升级队列的处理**************************************************
     **********************************************************************************************/

    /**
     * 判断添加到升级队列，计算md5，耗时，需要切线程
     *
     * @param upgradeInfo
     */
    public void addUpgradeTask(UpgradeInfo upgradeInfo) {
        synchronized (mapUpgradeInfo) {

            //检测当前版本是否已经是目标版本
            PackageInfo apkInfo = PackageManager.getInstance().getApkInfo(upgradeInfo.pbUpgrade.strPackageName);
            if (apkInfo != null && apkInfo.applicationInfo != null) {
                if (TextUtils.equals(apkInfo.versionName, upgradeInfo.pbUpgrade.strTargetVersion)) {
                    if (TextUtils.equals(generateMD5(apkInfo.applicationInfo.sourceDir), convertToHex(upgradeInfo.pbUpgrade.strNewMd5))) {
                        mapUpgradeInfo.remove(upgradeInfo.pbUpgrade.strPackageName);
                        reportUpgradeFinish(upgradeInfo);
                        return;
                    }
                }
            }

            UpgradeInfo mUpgradeInfo = mapUpgradeInfo.get(upgradeInfo.pbUpgrade.strPackageName);

            if (mUpgradeInfo == null) {
                mUpgradeInfo = upgradeInfo;
                mapUpgradeInfo.put(mUpgradeInfo.pbUpgrade.strPackageName, mUpgradeInfo);
            } else {
                //升级信息有部分不相同的情况，就停止掉当前的任务
                if (!equalsUpgrade(mUpgradeInfo.pbUpgrade, upgradeInfo.pbUpgrade)) {
                    stopDownload(mUpgradeInfo);
                }
                mUpgradeInfo.mFromType = upgradeInfo.mFromType;
                mUpgradeInfo.pbUpgrade = upgradeInfo.pbUpgrade;
            }

            //如果上次下载失败重新开始
            if (mUpgradeInfo.mUpgradeStatus == UPGRADE_STATE_DOWNLOAD_FAIL
                    || mUpgradeInfo.mUpgradeStatus == UPGRADE_STATE_INSTALLING
                    || mUpgradeInfo.mUpgradeStatus == UPGRADE_STATE_DOWNLOAD_STOP) {
                mUpgradeInfo.mUpgradeStatus = UPGRADE_STATE_NONE;
            }

            //检测当前添加任务是否已经下载完成
//            if (checkDownloadFinish(mUpgradeInfo)) {
//                mUpgradeInfo.mUpgradeStatus = UPGRADE_STATE_DOWNLOAD_FINISH;
//            }

            if (mUpgradeInfo.mUpgradeStatus != UPGRADE_STATE_DOWNLOADING) {
                //检测当前添加任务是否下载了部分了
                DownloadManager.DownloadFileSizeInfo fileSizeInfo = DownloadManager.getInstance().getDownloadFileSizeInfo(getDownloadTaskId(mUpgradeInfo.pbUpgrade.strPackageName), mUpgradeInfo.pbUpgrade.strFullDownloadUrl);
                mUpgradeInfo.mCacheSize = fileSizeInfo.mCacheSize;
                mUpgradeInfo.mFullSize = fileSizeInfo.mFullSize;
            }


            //添加后通知升级队列
            procUpgradeTask(5);

            //
            procCheckUpgradeFinish(5);
        }
    }

    /**
     * 包含处理下载和安装的逻辑
     * 当对话框显示或正在下载时，表示当前有任务正在处理<br>
     * 目前增加了在来电状态，休眠释放了时，倒车时不能进行升级，但保存了队列任务<br>
     * 等 {@link #PROC_UPGRADE_TIME} 重试<br>
     */
    private Runnable procUpgradeTaskRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mapUpgradeInfo) {
                if (mapUpgradeInfo.size() == 0) {
                    mUpgradeHandler.removeCallbacks(this);
                    return;
                }
                boolean bDoing = false;
                boolean isBusy = canUpgrade();
                UpgradeInfo needRunUpgradeInfo = null;
                for (UpgradeInfo upgradeInfo : mapUpgradeInfo.values()) {

                    switch (upgradeInfo.mUpgradeStatus) {
                        case UPGRADE_STATE_DOWNLOADING:
                        case UPGRADE_STATE_DIALOG_SHOWING:
                            bDoing = true;
                            break;
                        case UPGRADE_STATE_NONE:
                        case UPGRADE_STATE_DOWNLOAD_FINISH:
                            needRunUpgradeInfo = upgradeInfo;
                            break;
                        case UPGRADE_STATE_DOWNLOAD_WAITING:
                            //等待网络中，如果有网，开始执行任务
                            if (NetworkManager.getInstance().hasNet()) {
                                needRunUpgradeInfo = upgradeInfo;
                            } else {
                                bDoing = true;
                            }
                            break;
                    }
                }
                if (bDoing || isBusy) {
                    procUpgradeTask(PROC_UPGRADE_TIME);
                } else {
                    if (needRunUpgradeInfo != null) {
                        if (checkDownloadFinish(needRunUpgradeInfo)) {
                            if (needRunUpgradeInfo.mUpgradeStatus != UPGRADE_STATE_DOWNLOAD_FINISH) {
                                //本次不是走下载那边过来的，需要上报一下下载状态给后台计数
                                reportUpgradeRes(needRunUpgradeInfo, UiEquipment.APP_INSTALL_DOWNLOADING, UiEquipment.APP_COMMON_EC_OK);
                            }
                            procInstall(needRunUpgradeInfo);
                            procCheckUpgradeFinish(5 * 1000);
                        } else {
                            startCurrentUpgrade(needRunUpgradeInfo);
                        }
                        procUpgradeTask(PROC_UPGRADE_TIME);
                    }
                }
            }
        }
    };

    private void startCurrentUpgrade(UpgradeInfo upgradeInfo) {
        if (upgradeInfo.mUpgradeStatus == UPGRADE_STATE_DOWNLOAD_WAITING) {
            startDownload(upgradeInfo);
        } else if (upgradeInfo.mFromType != TYPE_FROM_CONTINUE
                && upgradeInfo.pbUpgrade.uint32Flag != null
                && (upgradeInfo.pbUpgrade.uint32Flag & PushManager.FLAG_SHOW_UPGRADE_MESSAGE_TXZ) != 0) {
            //TODO 当前继续升级的任务是不需要弹框的，后续需考虑从后台下发配置
            upgradeInfo.mUpgradeStatus = UPGRADE_STATE_DIALOG_SHOWING;
            showUpgradeConfirm(upgradeInfo);
        } else {
            startDownload(upgradeInfo);
        }
    }

    public void procUpgradeTask(long detail) {
        mUpgradeHandler.removeCallbacks(procUpgradeTaskRunnable);
        mUpgradeHandler.postDelayed(procUpgradeTaskRunnable, detail);
    }


    public void startDownload(UpgradeInfo upgradeInfo) {
        upgradeInfo.mUpgradeStatus = UPGRADE_STATE_DOWNLOADING;
        boolean bFullInstall = true;
        PackageInfo apkInfo = PackageManager.getInstance().getApkInfo(upgradeInfo.pbUpgrade.strPackageName);
        if (apkInfo != null && apkInfo.applicationInfo != null) {
            LogUtil.logd(String.format("packageName = %s , sourceDir = %s ,versionName = %s", apkInfo.packageName, apkInfo.applicationInfo.sourceDir, apkInfo.versionName));
            if (upgradeInfo.pbUpgrade.strOldMd5 != null) {
                upgradeInfo.sOldApkSourcePath = apkInfo.applicationInfo.sourceDir;
                String localPackageMD5 = generateMD5(apkInfo.applicationInfo.sourceDir);
                String responseMD5 = convertToHex(upgradeInfo.pbUpgrade.strOldMd5);
                if (TextUtils.equals(localPackageMD5, responseMD5)) {
                    if (TextUtils.isEmpty(upgradeInfo.pbUpgrade.strIncDownloadUrl)) {
                        LogUtil.logd("inc download url is empty.");
                    } else {
                        bFullInstall = false;
                    }
                } else {
                    LogUtil.loge(String.format("old version package md5 not match package[%s], response[%s], and use full download install", localPackageMD5, responseMD5));
                }
            } else {
                LogUtil.logd("old version package md5 is empty.");
            }
        } else {
            LogUtil.loge(String.format("getApkInfo error : %s", upgradeInfo.pbUpgrade.strPackageName));
        }

        if (bFullInstall) {
            sendDownloadEvent(upgradeInfo.pbUpgrade.strFullDownloadUrl, upgradeInfo.pbUpgrade.strPackageName, upgradeInfo.pbUpgrade.strTargetVersion, upgradeInfo.pbUpgrade.uint32Flag == null ? 0 : upgradeInfo.pbUpgrade.uint32Flag);
        } else {
            sendDownloadEvent(upgradeInfo.pbUpgrade.strIncDownloadUrl, upgradeInfo.pbUpgrade.strPackageName, upgradeInfo.pbUpgrade.strTargetVersion, upgradeInfo.pbUpgrade.uint32Flag == null ? 0 : upgradeInfo.pbUpgrade.uint32Flag);
        }
    }

    public void sendDownloadEvent(String url, String pkg, String version, int flag) {
        UiInnerNet.DownloadHttpFileTask task = new UiInnerNet.DownloadHttpFileTask();
        task.strTaskId = getDownloadTaskId(pkg);
        task.strUrl = url;
        task.strDefineParam = pkg;
        showProgressFloatView(pkg, version, flag);
        JNIHelper.sendEvent(UiEvent.EVENT_INNER_NET, UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_REQ, task);
    }

    public void stopDownload(UpgradeInfo upgradeInfo) {
        LogUtil.logd("stopDownload:" + upgradeInfo.pbUpgrade.strPackageName);
        upgradeInfo.mUpgradeStatus = UPGRADE_STATE_DOWNLOAD_STOP;
        UiInnerNet.DownloadHttpFileTask pbReqHttpDownload = new UiInnerNet.DownloadHttpFileTask();
        pbReqHttpDownload.strTaskId = getDownloadTaskId(upgradeInfo.pbUpgrade.strPackageName);
        JNIHelper.sendEvent(UiEvent.EVENT_INNER_NET, UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_STOP, pbReqHttpDownload);
    }

    //下载框架下载完成后的回调
    public void onDownloadFinish(UiInnerNet.DownloadHttpFileTask downloadHttpFileTask) {
        synchronized (mapUpgradeInfo) {
            //判断是2.0的升级任务
            if (downloadHttpFileTask.strTaskId.startsWith(DOWNLOAD_TASK_PRE)) {

                //判断pkg在升级队列中
                UpgradeInfo upgradeInfo = mapUpgradeInfo.get(downloadHttpFileTask.strDefineParam);
                if (upgradeInfo == null) {
                    dismissProgressFloatView(downloadHttpFileTask.strTaskId.substring(DOWNLOAD_TASK_PRE.length()), "", 0);
                    LogUtil.logd(String.format("can't find package name = %s", downloadHttpFileTask.strDefineParam));
                } else {
                    dismissProgressFloatView(downloadHttpFileTask.strTaskId.substring(DOWNLOAD_TASK_PRE.length()), upgradeInfo.pbUpgrade.strTargetVersion, upgradeInfo.pbUpgrade.uint32Flag == null ? 0 : upgradeInfo.pbUpgrade.uint32Flag);
                    if (downloadHttpFileTask.int32ResultCode == UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_SUCCESS) {

                        String newApkFile = genInstallApkPath(upgradeInfo.pbUpgrade);
                        boolean bFullInstall = true;
                        //全量升级
                        if (TextUtils.equals(downloadHttpFileTask.strUrl, upgradeInfo.pbUpgrade.strFullDownloadUrl)) {
                            if (FileUtil.copyFile(downloadHttpFileTask.strFile, newApkFile)) {
                                LogUtil.logd(String.format("full install: rename %s -> %s", downloadHttpFileTask.strFile, newApkFile));
                                new File(downloadHttpFileTask.strFile).delete();
                            } else {
                                LogUtil.loge(String.format("move file(%s) error,  and use download_tmp_file to install.", downloadHttpFileTask.strFile));
                                newApkFile = downloadHttpFileTask.strFile;
                            }
                        } else {
                            //增量升级
                            bFullInstall = false;
                            if (NativeData.comboFileByNames(upgradeInfo.sOldApkSourcePath, downloadHttpFileTask.strFile, newApkFile)) {
                                if (!new File(downloadHttpFileTask.strFile).delete()) {
                                    LogUtil.loge(String.format("remove (%s) error.", downloadHttpFileTask.strFile));
                                }
                                LogUtil.logd(String.format("inc install: comboPatchFile=%s", newApkFile));
                            } else {
                                LogUtil.loge("combo patch file error, and try to full download install");
                                sendDownloadEvent(upgradeInfo.pbUpgrade.strFullDownloadUrl, upgradeInfo.pbUpgrade.strPackageName, upgradeInfo.pbUpgrade.strTargetVersion, upgradeInfo.pbUpgrade.uint32Flag == null ? 0 : upgradeInfo.pbUpgrade.uint32Flag);
                                upgradeInfo.mUpgradeStatus = UPGRADE_STATE_DOWNLOADING;
                                return;
                            }
                        }

                        String newApkFileMD5 = generateMD5(newApkFile);
                        String newMD5 = convertToHex(upgradeInfo.pbUpgrade.strNewMd5);
                        if (TextUtils.equals(newApkFileMD5, newMD5)) {
                            upgradeInfo.sNewApkSourcePath = newApkFile;
                            upgradeInfo.mUpgradeStatus = UPGRADE_STATE_DOWNLOAD_FINISH;
                        } else {
                            if (bFullInstall) {
                                LogUtil.loge(String.format("new version package md5 not match package[%s], response[%s]", newApkFileMD5, newMD5));
                                upgradeInfo.mUpgradeStatus = UPGRADE_STATE_DOWNLOAD_FAIL;
                                mapUpgradeInfo.remove(upgradeInfo.pbUpgrade.strPackageName);
                                reportUpgradeRes(upgradeInfo, UiEquipment.APP_INSTALL_DOWNLOADING, UiEquipment.APP_COMMON_EC_UNKNOWN);
                            } else {
                                LogUtil.loge(String.format("new version package md5 not match package[%s], response[%s], and try to full download install", newApkFileMD5, newMD5));
                                upgradeInfo.mUpgradeStatus = UPGRADE_STATE_DOWNLOADING;
                                sendDownloadEvent(upgradeInfo.pbUpgrade.strFullDownloadUrl, upgradeInfo.pbUpgrade.strPackageName, upgradeInfo.pbUpgrade.strTargetVersion, upgradeInfo.pbUpgrade.uint32Flag == null ? 0 : upgradeInfo.pbUpgrade.uint32Flag);
                            }
                            return;
                        }
                        reportUpgradeRes(upgradeInfo, UiEquipment.APP_INSTALL_DOWNLOADING, UiEquipment.APP_COMMON_EC_OK);
                    } else {
                        upgradeInfo.mUpgradeStatus = UPGRADE_STATE_DOWNLOAD_FAIL;
                        if (downloadHttpFileTask.int32ResultCode == UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_HTTP
                                && downloadHttpFileTask.int32StatusCode == 404
                                && TextUtils.equals(downloadHttpFileTask.strUrl, upgradeInfo.pbUpgrade.strIncDownloadUrl)) {
                            LogUtil.loge("download inc url fail, and try again full url.");
                            upgradeInfo.mUpgradeStatus = UPGRADE_STATE_DOWNLOADING;

                            sendDownloadEvent(upgradeInfo.pbUpgrade.strFullDownloadUrl, upgradeInfo.pbUpgrade.strPackageName, upgradeInfo.pbUpgrade.strTargetVersion, upgradeInfo.pbUpgrade.uint32Flag == null ? 0 : upgradeInfo.pbUpgrade.uint32Flag);
                        } else if (downloadHttpFileTask.int32ResultCode == UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_REQUEST) {
                            if (upgradeInfo.mNetRetryCount < upgradeInfo.mNetRetryRepeat) {
                                //当网络错误时，需要等网络恢复时，恢复下载
                                upgradeInfo.mUpgradeStatus = UPGRADE_STATE_DOWNLOAD_WAITING;
                            } else {
                                mapUpgradeInfo.remove(upgradeInfo.pbUpgrade.strPackageName);
                            }
                            upgradeInfo.mNetRetryCount ++;
                        } else { //失败时移除队列
                            int errCode = UiEquipment.APP_COMMON_EC_UNKNOWN;
                            switch (downloadHttpFileTask.int32ResultCode) {
                                case UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_HTTP:
                                    errCode = UiEquipment.APP_COMMON_EC_SERVER;
                                    break;
                                case UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_IO:
                                    errCode = UiEquipment.APP_COMMON_EC_IO;
                                    break;
                                case UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_REQUEST:
                                    errCode = UiEquipment.APP_COMMON_EC_NETWORK;
                                    break;
                            }
                            reportUpgradeRes(upgradeInfo, UiEquipment.APP_INSTALL_DOWNLOADING, errCode);
                            mapUpgradeInfo.remove(upgradeInfo.pbUpgrade.strPackageName);
                        }
                    }
                }
            }
        }
    }


    private void showUpgradeConfirm(final UpgradeInfo upgradeInfo) {
        String totalSize = "0M";
        try {
            totalSize = getFormatSize(upgradeInfo.mFullSize - upgradeInfo.mCacheSize);
        } catch (Exception e) {

        }
        showDialog(upgradeInfo, totalSize, new TXZInnerUpgradeManager.UpgradeDialogTool.DialogListener() {
            boolean bFinish = false;
            @Override
            public void onClickOk() {
                if (!bFinish) {
                    bFinish = true;
                    reportUpgradeOperation(upgradeInfo, USER_OP_CONFIRM);
                    mUpgradeHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            startDownload(upgradeInfo);
                        }
                    });
                }
            }

            @Override
            public void onClickCancel() {
                if (!bFinish) {
                    bFinish = true;
                    reportUpgradeOperation(upgradeInfo, USER_OP_CANCEL);
                    synchronized (mapUpgradeInfo) {
                        mapUpgradeInfo.remove(upgradeInfo.pbUpgrade.strPackageName);
                    }
                }
            }

            @Override
            public void onDismiss() {

            }
        }, TXZInnerUpgradeManager.UpgradeDialogTool.TYPE_UPGRADE);
    }

    private void showContinueDownloadConfirm(final UpgradeInfo upgradeInfo) {
        String totalSize = "0M";
        try {
            totalSize = getFormatSize(upgradeInfo.mFullSize - upgradeInfo.mCacheSize);
        } catch (Exception e) {

        }
        showDialog(upgradeInfo, totalSize, new TXZInnerUpgradeManager.UpgradeDialogTool.DialogListener() {
            @Override
            public void onClickOk() {
                reportUpgradeOperation(upgradeInfo, USER_OP_CONFIRM);
                mUpgradeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startDownload(upgradeInfo);
                    }
                });
            }

            @Override
            public void onClickCancel() {
                reportUpgradeOperation(upgradeInfo, USER_OP_CANCEL);
                upgradeInfo.mUpgradeStatus = UPGRADE_STATE_DOWNLOAD_STOP;
                synchronized (mapUpgradeInfo) {
                    mapUpgradeInfo.remove(upgradeInfo.pbUpgrade.strPackageName);
                }
            }

            @Override
            public void onDismiss() {

            }
        }, TXZInnerUpgradeManager.UpgradeDialogTool.TYPE_CONTINUE_UPGRADE);
    }

    private void showDialog(final UpgradeInfo upgradeInfo, String totalSize, TXZInnerUpgradeManager.UpgradeDialogTool.DialogListener listener, int type) {
        //预先关掉同包名的升级提升窗口，可能前一次的升级提示没有关闭
        WinDialog dlg = mMapUpgradeWins.remove(upgradeInfo.pbUpgrade.strPackageName);
        if (dlg != null) {
            dlg.dismiss("The Dialog did not close last time");
        }
        JSONBuilder detailInfo = new JSONBuilder();
        detailInfo.put("pkgName", upgradeInfo.pbUpgrade.strPackageName);
        detailInfo.put("version", upgradeInfo.pbUpgrade.strTargetVersion);
//        detailInfo.put("appName",upgradeInfo.pbUpgrade);
        detailInfo.put("totalSize", totalSize);
        detailInfo.put("type", type);
        detailInfo.put("hintTts", upgradeInfo.pbUpgrade.strInstallTips);
        detailInfo.put("upgradeInfo", "");
        if (upgradeInfo.pbUpgrade.strUserParam != null) {
            JSONBuilder jsonBuilder = new JSONBuilder(upgradeInfo.pbUpgrade.strUserParam);
            String msg = jsonBuilder.getVal("upgrade_info", String.class);
            if (!TextUtils.isEmpty(msg)) {
                detailInfo.put("upgradeInfo", msg);
            }
            String voiceTip = jsonBuilder.getVal("voice_tip", String.class);
            if (!TextUtils.isEmpty(voiceTip)) {
                detailInfo.put("hintTts", voiceTip);
            }

        }

        mUpgradeDialogTool.showConfirmDialog("温馨提示", upgradeInfo.pbUpgrade.strInstallTips, detailInfo.toString(), listener);
    }


    private boolean checkDownloadFinish(UpgradeInfo upgradeInfo) {
        boolean isDownloadFinish = false;
        String path = genInstallApkPath(upgradeInfo.pbUpgrade);
        String localFileMd5 = generateMD5(path);
        String newMd5 = convertToHex(upgradeInfo.pbUpgrade.strNewMd5);
        LogUtil.logd("path md5 :" + localFileMd5 + " ; md5 : " + newMd5);
        if (TextUtils.equals(localFileMd5, newMd5)) {
            upgradeInfo.sNewApkVersion = upgradeInfo.pbUpgrade.strTargetVersion;
            upgradeInfo.sNewApkSourcePath = path;
            isDownloadFinish = true;
        }
        return isDownloadFinish;
    }

    /**
     * 准备安装
     *
     * @param upgradeInfo
     */
    private void procInstall(UpgradeInfo upgradeInfo) {
        UiEquipment.Notify_InstallApk pbNotifyInstalApk = new UiEquipment.Notify_InstallApk();
        pbNotifyInstalApk.strPackageName = upgradeInfo.pbUpgrade.strPackageName;
        pbNotifyInstalApk.strInstallTips = upgradeInfo.pbUpgrade.strInstallTips;
        pbNotifyInstalApk.strApkPath = upgradeInfo.sNewApkSourcePath;
        pbNotifyInstalApk.uint32Flag = upgradeInfo.pbUpgrade.uint32Flag;

        procInstallApk(pbNotifyInstalApk);
    }

    private void procInstallApk(final UiEquipment.Notify_InstallApk pbNotifyInstallApk) {
        if (null == pbNotifyInstallApk) {
            return;
        }

        final UpgradeInfo upgradeInfo = mapUpgradeInfo.get(pbNotifyInstallApk.strPackageName);

        //预先关掉同包名的升级提升窗口，可能前一次的升级提示没有关闭
        WinDialog dlg = mMapUpgradeWins.remove(pbNotifyInstallApk.strPackageName);
        if (dlg != null) {
            dlg.dismiss("The Dialog did not close last time");
        }

        if (pbNotifyInstallApk.uint32Flag == null) {
            pbNotifyInstallApk.uint32Flag = 0;
        }

        boolean bSupportOut = supportOutterApk(pbNotifyInstallApk.strPackageName);
        boolean bSilentUpdate = ((pbNotifyInstallApk.uint32Flag & FLAG_FORCE_HINT_UPGRADE) == 0)
                && (bSupportOut || PackageManager.getInstance().checkAppExist(
                "com.txznet.apkinstaller"));
        boolean bNewUpgrade =  ((pbNotifyInstallApk.uint32Flag & PushManager.FLAG_SHOW_UPGRADE_MESSAGE_TXZ) == PushManager.FLAG_SHOW_UPGRADE_MESSAGE_TXZ);

        JNIHelper.logd("begin install package: "
                + pbNotifyInstallApk.strApkPath + ", flag=" + pbNotifyInstallApk.uint32Flag);

        String _shortName = "unk";
        if (pbNotifyInstallApk.strPackageName.startsWith("com.txznet.")) {
            _shortName = pbNotifyInstallApk.strPackageName.substring("com.txznet.".length());
        }
        final String shortName = _shortName;

        MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_ENTER_PREFIX + "all", MonitorUtil.UPGRADE_ENTER_PREFIX + shortName);

        pbNotifyInstallApk.bForceUpgrade = ((pbNotifyInstallApk.uint32Flag & FLAG_FORCE_INSTALL) != 0);

        // 判断是否使用外部apk升级方式
        if ((pbNotifyInstallApk.uint32Flag & FLAG_FORCE_INSTALL_INNER_APK) == 0
                && bSupportOut) {
            MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_LOADER_PREFIX + "all", MonitorUtil.UPGRADE_LOADER_PREFIX + shortName);

            JSONBuilder json = new JSONBuilder();
            // 按时间重新移动到新位置
            // String newName = pbNotifyInstallApk.strApkPath + "."
            // + System.currentTimeMillis();
//			File f = new File(pbNotifyInstallApk.strApkPath);
//			f.renameTo(new File(newName));
            json.put(BaseApplication.SP_KEY_APK, pbNotifyInstallApk.strApkPath);
            json.put("force", pbNotifyInstallApk.bForceUpgrade);
            //新版本的静默升级不需要这个

            if (!bSilentUpdate && !bNewUpgrade) {
                json.put("desc", pbNotifyInstallApk.strInstallTips);
            }
            ServiceManager.getInstance().sendInvoke(
                    pbNotifyInstallApk.strPackageName, "comm.update.upgrade",
                    json.toBytes(), null);
            if (upgradeInfo != null) {
                upgradeInfo.mUpgradeStatus = UPGRADE_STATE_INSTALL_FINISH;
            }
            return;
        }

        //如果是新版的先显示弹窗的情况，直接调用系统的安装
        if (bNewUpgrade) {
            PackageInstaller.installApkByIntent(pbNotifyInstallApk.strApkPath);
            if (upgradeInfo != null) {
                upgradeInfo.mUpgradeStatus = UPGRADE_STATE_INSTALLING;
            }
            return;
        }

        // 静默升级
        if (bSilentUpdate) {
            MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_SILENT_PREFIX + "all", MonitorUtil.UPGRADE_SILENT_PREFIX + shortName);

            if (PackageInstaller
                    .installApkByPackageManager(pbNotifyInstallApk.strApkPath)) {
                return;
            }
        }

        // 非静默升级
        String strInstall;
        if (pbNotifyInstallApk.strApkPath.startsWith("http://")) {
            MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_DOWN_PREFIX + "all", MonitorUtil.UPGRADE_DOWN_PREFIX + shortName);
            strInstall = "下载更新";
        } else {
            MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_INSTALL_PREFIX + "all", MonitorUtil.UPGRADE_INSTALL_PREFIX + shortName);
            strInstall = "立即升级";
        }

        if (upgradeInfo != null) {
            upgradeInfo.mUpgradeStatus = UPGRADE_STATE_DIALOG_SHOWING;
        }

        if (pbNotifyInstallApk.bForceUpgrade != null
                && pbNotifyInstallApk.bForceUpgrade == true) {
            MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_HINT_PREFIX + "all", MonitorUtil.UPGRADE_HINT_PREFIX + shortName);
            WinNotice.WinNoticeBuildData buildData = new WinNotice.WinNoticeBuildData();
            buildData.setSureText(strInstall)
                    .setMessageText(pbNotifyInstallApk.strInstallTips)
                    .setTitleText("软件更新").setMessageAllowScroll(true);
            WinNotice win = new WinNotice(buildData) {
                @Override
                public void onClickOk() {
                    MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_CLOSE_PREFIX + "all", MonitorUtil.UPGRADE_CLOSE_PREFIX + shortName);

                    PackageInstaller
                            .installApkByIntent(pbNotifyInstallApk.strApkPath);
                    if (upgradeInfo != null) {
                        upgradeInfo.mUpgradeStatus = UPGRADE_STATE_INSTALLING;
                    }
                }

                @Override
                public String getReportDialogId() {
                    return "upgrade_force_apk";
                }
            };
            win.show();
            mMapUpgradeWins.put(pbNotifyInstallApk.strPackageName, win);
        } else {
            MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_CONFIRM_PREFIX + "all", MonitorUtil.UPGRADE_CONFIRM_PREFIX + shortName);

            WinConfirm.WinConfirmBuildData buildData = new WinConfirm.WinConfirmBuildData();
            buildData.setSureText(strInstall).setCancelText("下次再说")
                    .setMessageText(pbNotifyInstallApk.strInstallTips)
                    .setTitleText("软件更新").setMessageAllowScroll(true);
            WinConfirm win = new WinConfirm(buildData) {
                @Override
                public void onClickOk() {
                    MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_SURE_PREFIX + "all", MonitorUtil.UPGRADE_SURE_PREFIX + shortName);
                    if (upgradeInfo != null) {
                        upgradeInfo.mUpgradeStatus = UPGRADE_STATE_INSTALLING;
                    }
                    PackageInstaller
                            .installApkByIntent(pbNotifyInstallApk.strApkPath);
                }

                @Override
                public void onClickCancel() {
                    if (upgradeInfo != null) {
                        upgradeInfo.mUpgradeStatus = UPGRADE_STATE_INSTALL_FAIL;
                        mapUpgradeInfo.remove(upgradeInfo.pbUpgrade.strPackageName);
                    }
                    MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_CANCEL_PREFIX + "all", MonitorUtil.UPGRADE_CANCEL_PREFIX + shortName);
                }

                @Override
                public void onBackPressed() {
                    if (upgradeInfo != null) {
                        upgradeInfo.mUpgradeStatus = UPGRADE_STATE_INSTALL_FAIL;
                        mapUpgradeInfo.remove(upgradeInfo.pbUpgrade.strPackageName);
                    }
                    MonitorUtil.monitorCumulant(MonitorUtil.UPGRADE_CANCEL_PREFIX + "all", MonitorUtil.UPGRADE_CANCEL_PREFIX + shortName);
                    this.dismissInner();
                }

                @Override
                public String getReportDialogId() {
                    return "upgrade_normal_apk";
                }
            };
            win.show();
            mMapUpgradeWins.put(pbNotifyInstallApk.strPackageName, win);
        }
    }

    private static boolean supportOutterApk(String packageName) {
        android.content.pm.PackageManager pm = GlobalContext.get()
                .getPackageManager();
        try {
            return pm.getApplicationInfo(packageName,
                    android.content.pm.PackageManager.GET_META_DATA).metaData
                    .getBoolean("TXZApkLoader", false);
        } catch (Exception e) {
        }
        return false;
    }


    // 远程命令处理
    public byte[] processInvoke(String packageName, String command, byte[] data) {
        if (command.startsWith(TXZInnerUpgradeManager.TOOL_DIALOG_PREFIX)) {
            if (mUpgradeDialogTool != null && mUpgradeDialogTool instanceof RemoteDialogTool) {
                ((RemoteDialogTool) mUpgradeDialogTool).processInvoke(packageName, command, data);
            }
        }
        Parcel p = Parcel.obtain();
        if (null != data) {
            p.unmarshall(data, 0, data.length);
            p.setDataPosition(0);
        }
        if (TXZInnerUpgradeManager.INVOKE_SET_UPGRADE_DIALOG.equals(command)) {
            JNIHelper.logd("set remote dialog tool");
            if (!(mUpgradeDialogTool != null
                    && mUpgradeDialogTool instanceof RemoteDialogTool
                    && TextUtils.equals(packageName, ((RemoteDialogTool) mUpgradeDialogTool).mRemotePkgName))) {
                mUpgradeDialogTool = new RemoteDialogTool(packageName);
            }
        } else if (TXZInnerUpgradeManager.INVOKE_CLEAR_UPGRADE_DIALOG.equals(command)) {
            JNIHelper.logd("clear remote dialog tool");
            if (!(mUpgradeDialogTool != null
                    && mUpgradeDialogTool instanceof LocalDialogTool)) {
                mUpgradeDialogTool = new LocalDialogTool();
            }
        }

        if (TXZInnerUpgradeManager.INVOKE_SET_NOTIFICATION_TOOL.equals(command)) {
            JNIHelper.logd("set remote notification tool");
            if (!(mNotificationTool != null
                    && mNotificationTool instanceof RemoteNotificationTool
                    && TextUtils.equals(packageName, ((RemoteNotificationTool) mNotificationTool).mRemotePkgName))) {
                mNotificationTool = new RemoteNotificationTool(packageName);
            }
        } else if (TXZInnerUpgradeManager.INVOKE_CLEAR_NOTIFICATION_TOOL.equals(command)) {
            JNIHelper.logd("clear remote notification tool");
            if (!(mNotificationTool != null
                    && mNotificationTool instanceof LocalNotificationTool)) {
                mNotificationTool = new LocalNotificationTool();
            }
        } else if (TXZInnerUpgradeManager.INVOKE_SET_UPGRADE_STATUS.equals(command)) {
            mSdkBusy = p.readByte() == 1;
            JNIHelper.logd("set_upgrade_status" + mSdkBusy);
        }

        p.recycle();
        return null;
    }

    /**
     * 本地对话框工具
     */
    private class LocalDialogTool implements TXZInnerUpgradeManager.UpgradeDialogTool {
        @Override
        public void showConfirmDialog(@NonNull String title, @NonNull String content, @NonNull String detailInfo, @NonNull final DialogListener listener) {

            JSONBuilder jsonBuilder = new JSONBuilder(detailInfo);
            String pkgName = jsonBuilder.getVal("pkgName", String.class, "");
            String version = jsonBuilder.getVal("version", String.class, "");
            String appName = jsonBuilder.getVal("appName", String.class, "");
            String totalSize = jsonBuilder.getVal("totalSize", String.class, "0M");
            int type = jsonBuilder.getVal("type", Integer.class, TXZInnerUpgradeManager.UpgradeDialogTool.TYPE_UPGRADE);
            String hintTts = jsonBuilder.getVal("hintTts", String.class, "");
            String upgradeInfo = jsonBuilder.getVal("upgradeInfo", String.class, "");


            String sureText = "确定(" + totalSize + ")";
            switch (type) {
                case TXZInnerUpgradeManager.UpgradeDialogTool.TYPE_UPGRADE:
                    break;
                case TXZInnerUpgradeManager.UpgradeDialogTool.TYPE_CONTINUE_UPGRADE:
                    sureText = "继续(剩余(" + totalSize + ")";
                    break;
                case TXZInnerUpgradeManager.UpgradeDialogTool.TYPE_NOTIFY_INSTALL:
                    sureText = "安装";
                    break;
                case TXZInnerUpgradeManager.UpgradeDialogTool.TYPE_NOTIFY_FORCE_INSTALL:
                    sureText = "安装";
                    break;
            }

            UpgradeDialog.UpgradeDialogBuildDate buildData = new UpgradeDialog.UpgradeDialogBuildDate();
            buildData.setSureText(sureText, new String[]{"继续升级", "确定", "好的", "是的"})
                    .setTips(content)
                    .setTitle(title)
                    .setHintTts(hintTts.replaceAll("\\.","点"))
                    .setCancelOutside(false);
            if (type != TXZInnerUpgradeManager.UpgradeDialogTool.TYPE_NOTIFY_FORCE_INSTALL) {
                buildData.setCancelText("取消", new String[]{"放弃升级", "取消", "不要"});
            }

            if (!TextUtils.isEmpty(upgradeInfo)) {
                buildData.setContent("更新内容:\r\n" + upgradeInfo);
            }

            UpgradeDialog win = new UpgradeDialog(buildData) {
                @Override
                public void onClickOk() {
                    listener.onClickOk();
                    listener.onDismiss();
                }

                @Override
                public void onClickCancel() {
                    listener.onClickCancel();
                    listener.onDismiss();
                }

                @Override
                public String getReportDialogId() {
                    return "upgrade_notify";
                }
            };
            win.show();
            mMapUpgradeWins.put(pkgName, win);
        }

        @Override
        public void cancelAll() {

        }

        @Override
        public void dismissConfirmDialog(@NonNull String title, @NonNull String content, @NonNull String detailInfo) {

        }
    }

    private final long DIALOG_OPERATION_TIMEOUT = 1000 * 60; // 对话框操作超时时间
    /**
     * 远程对话框工具
     */
    private class RemoteDialogTool implements TXZInnerUpgradeManager.UpgradeDialogTool {
        private String mRemotePkgName;
        private final HashMap<String, DialogListener> mDialogListeners = new HashMap<String, DialogListener>();
        private final HashMap<String, Runnable1<byte[]>> mDialogTimeoutRunnable = new HashMap<String, Runnable1<byte[]>>();

        RemoteDialogTool(String pkg) {
            mRemotePkgName = pkg;
        }

        @Override
        public void showConfirmDialog(@NonNull String title, @NonNull String content, @NonNull String detailInfo, @NonNull final DialogListener listener) {
            LogUtil.logd("show remote dialog title : " + title);

            String md5Key = MD5Util.generateMD5(detailInfo);
            synchronized (mDialogListeners) {
                mDialogListeners.put(md5Key, listener);
            }
            Parcel p = Parcel.obtain();
            p.writeString(title);
            p.writeString(content);
            p.writeString(detailInfo);
            ServiceManager.getInstance().sendInvoke(mRemotePkgName, TXZInnerUpgradeManager.UPGRADE_CMD_PREFIX + TXZInnerUpgradeManager.TOOL_DIALOG_PREFIX + TXZInnerUpgradeManager.CMD_SHOW, p.marshall(), null);

            Runnable1<byte[]> timeoutRunnable = new Runnable1<byte[]>(p.marshall()) {
                @Override
                public void run() {
                    ServiceManager.getInstance().sendInvoke(mRemotePkgName, TXZInnerUpgradeManager.UPGRADE_CMD_PREFIX + TXZInnerUpgradeManager.TOOL_DIALOG_PREFIX + TXZInnerUpgradeManager.CMD_DISMISS, mP1, null);
                    mUpgradeHandler.removeCallbacks(this);
                    listener.onClickCancel();
                    listener.onDismiss();
                }
            };
            mUpgradeHandler.postDelayed(timeoutRunnable,DIALOG_OPERATION_TIMEOUT);

            synchronized (mDialogTimeoutRunnable) {
                mDialogTimeoutRunnable.put(md5Key,timeoutRunnable);
            }

            p.recycle();
        }


        @Override
        public void dismissConfirmDialog(@NonNull String title, @NonNull String content, @NonNull String detailInfo) {

        }

        @Override
        public void cancelAll() {

        }

        private void processInvoke(String packageName, String command, byte[] data) {
            Parcel p = Parcel.obtain();
            if (null != data) {
                p.unmarshall(data, 0, data.length);
                p.setDataPosition(0);
            }
            String detailInfoMd5 = p.readString();
            if (TXZInnerUpgradeManager.INVOKE_DIALOG_CONFIRM.equals(command)) {
                JNIHelper.logd("remote dialog click onConfirm");
                synchronized (mDialogListeners) {
                    if (mDialogListeners.containsKey(detailInfoMd5)) {
                        mDialogListeners.get(detailInfoMd5).onClickOk();
                    }
                }
                synchronized (mDialogTimeoutRunnable) {
                    Runnable1<byte[]> timeoutRunnable = mDialogTimeoutRunnable.remove(detailInfoMd5);
                    if (timeoutRunnable != null) {
                        mUpgradeHandler.removeCallbacks(timeoutRunnable);
                    }
                }
            } else if (TXZInnerUpgradeManager.INVOKE_DIALOG_CANCEL.equals(command)) {
                JNIHelper.logd("remote dialog click onCancel");
                synchronized (mDialogListeners) {
                    if (mDialogListeners.containsKey(detailInfoMd5)) {
                        mDialogListeners.get(detailInfoMd5).onClickCancel();
                    }
                }
                synchronized (mDialogTimeoutRunnable) {
                    Runnable1<byte[]> timeoutRunnable = mDialogTimeoutRunnable.remove(detailInfoMd5);
                    if (timeoutRunnable != null) {
                        mUpgradeHandler.removeCallbacks(timeoutRunnable);
                    }
                }
            } else if (TXZInnerUpgradeManager.INVOKE_DIALOG_DISMISS.equals(command)) {
                JNIHelper.logd("remote dialog click onDismiss");
                synchronized (mDialogListeners) {
                    if (mDialogListeners.containsKey(detailInfoMd5)) {
                        mDialogListeners.get(detailInfoMd5).onDismiss();
                        mDialogListeners.remove(detailInfoMd5);
                    }
                }
            }
            p.recycle();
        }
    }

    /**
     * 本地进度显示
     */
    private class LocalNotificationTool implements TXZInnerUpgradeManager.NotificationTool {

        @Override
        public void notify(@NonNull String pkgName, @NonNull String version, @NonNull String apkName, int event, @Nullable String data) {
            switch (event) {
                case TXZInnerUpgradeManager.NotificationTool.EVENT_BEGIN_DOWNLOAD:
                    if (mUpgradeFloatView == null) {
                        mUpgradeFloatView = new UpgradeFloatView(GlobalContext.get());
                    }
                    if (Looper.getMainLooper() == Looper.myLooper()) {
                        mUpgradeFloatView.open();
                    } else {
                        AppLogic.runOnUiGround(new Runnable() {
                            @Override
                            public void run() {
                                mUpgradeFloatView.open();
                            }
                        });
                    }
                    break;
                case TXZInnerUpgradeManager.NotificationTool.EVENT_PROGRESS_CHANGE:
                    JSONBuilder jsonBuilder = new JSONBuilder(data);
                    mUpgradeFloatView.notifyProgressChanged(jsonBuilder.getVal("progress", Integer.class, 0), apkName);
                    break;
                case TXZInnerUpgradeManager.NotificationTool.EVENT_END_DOWNLOAD:
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        if (mUpgradeFloatView != null) {
                            mUpgradeFloatView.close();
                        }
                    } else {
                        AppLogic.runOnUiGround(new Runnable() {
                            @Override
                            public void run() {
                                if (mUpgradeFloatView != null) {
                                    mUpgradeFloatView.close();
                                }
                            }
                        });
                    }
                    break;
            }
        }

        @Override
        public void cancelAll() {

        }
    }

    /**
     * 进度显示的远程实现
     */
    private class RemoteNotificationTool implements TXZInnerUpgradeManager.NotificationTool {
        private String mRemotePkgName;

        private RemoteNotificationTool(String pkgName) {
            mRemotePkgName = pkgName;
        }

        @Override
        public void notify(@NonNull String pkgName, @NonNull String version, @NonNull String appName, int event, @Nullable String data) {
            Parcel p = Parcel.obtain();
            p.writeString(pkgName);
            p.writeString(version);
            p.writeString("");//暂时不提供appName
            p.writeInt(event);
            p.writeString(data);
            ServiceManager.getInstance().sendInvoke(mRemotePkgName, TXZInnerUpgradeManager.UPGRADE_CMD_PREFIX + TXZInnerUpgradeManager.TOOL_NOTIFICATION_PREFIX + TXZInnerUpgradeManager.CMD_NOTIFY, p.marshall(), null);
            p.recycle();
        }

        @Override
        public void cancelAll() {

        }
    }


    private void showProgressFloatView(final String pkg, final String version, int flag) {
        if (needShowProgress(pkg, flag)) {
            UpgradeInfo upgradeInfo = mapUpgradeInfo.get(pkg);
            JSONBuilder jsonBuilder = new JSONBuilder();
            jsonBuilder.put("progress", 0);
            if (upgradeInfo != null) {
                jsonBuilder.put("progress",(int) (upgradeInfo.mCacheSize * 1f / upgradeInfo.mFullSize) * 100);
            }


            mNotificationTool.notify(pkg, version, getPackageName(pkg), TXZInnerUpgradeManager.NotificationTool.EVENT_BEGIN_DOWNLOAD, null);

            DownloadManager.getInstance().registerDownloadTaskStatusChangeListener(getDownloadTaskId(pkg), new DownloadManager.DownloadTaskProgressChangeListener() {
                @Override
                public void onProgressChange(UiInnerNet.DownloadHttpFileTask task) {
                    LogUtil.logd("onProgressChange:" + task.uint32DlProgress);
                    JSONBuilder jsonBuilder = new JSONBuilder();
                    jsonBuilder.put("progress", task.uint32DlProgress);
                    mNotificationTool.notify(pkg, version, getPackageName(pkg), TXZInnerUpgradeManager.NotificationTool.EVENT_PROGRESS_CHANGE, jsonBuilder.toString());
                }
            });
        }
    }

    private void dismissProgressFloatView(String pkg, String version, int flag) {
        if (needShowProgress(pkg, flag)) {
            mNotificationTool.notify(pkg, version, getPackageName(pkg), TXZInnerUpgradeManager.NotificationTool.EVENT_END_DOWNLOAD, null);
            DownloadManager.getInstance().unregisterDownloadTaskStatusChangeListener(getDownloadTaskId(pkg)); // 移除下载进度监听
        }
    }


    //还需要支持适配增加繁忙状态，考虑实现
    private boolean canUpgrade() {
        boolean isBusy = (!CallManager.getInstance().isIdle() && !CallManager.getInstance().isRinging()) //来电中不允许进行升级
                || TXZPowerControl.hasReleased() //休眠时不能进行升级
                || TXZPowerControl.isEnterReverse()//倒车时不能进行升级
                || NavManager.getInstance().isNavi()//在导航时不允许升级
                || mSdkBusy //适配中繁忙的情况
                || RecorderWin.isOpened()
                ;
        if (isBusy) {
            LogUtil.logd(String.format("upgrade is busy:%b , callIdle:%b , isRinging:%b , hasReleased:%b , isEnterReverse:%b , isNavi:%b , mSdkBusy:%b",
                    isBusy,
                    CallManager.getInstance().isIdle(),
                    CallManager.getInstance().isRinging(),
                    TXZPowerControl.hasReleased(),
                    TXZPowerControl.isEnterReverse(),
                    NavManager.getInstance().isNavi(),
                    mSdkBusy,
                    RecorderWin.isOpened()
            ));
        }

        return isBusy;
    }

    /****************************************************************************
     ***************************检测升级完成的任务**********************************
     ****************************************************************************/

    public void procCheckUpgradeFinish(long detail) {
        mUpgradeHandler.removeCallbacks(checkUpgradeFinishRunnable);
        mUpgradeHandler.postDelayed(checkUpgradeFinishRunnable, detail);
    }

    private Runnable checkUpgradeFinishRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mapUpgradeInfo) {
                checkUpgradeFinishInner();
            }
        }
    };

    private void checkUpgradeFinishInner() {
        synchronized (mapUpgradeInfo) {
            LogUtil.logd("checkUpgradeFinishRunnable:" + mapUpgradeInfo.size());
            if (mapUpgradeInfo.size() == 0) {
                //将检查队列停了吧
                mUpgradeHandler.removeCallbacks(checkUpgradeFinishRunnable);
                return;
            }

            //TODO 不仅仅是txzApp列表
            UiEquipment.VersionInfo versionInfo = PackageManager.getInstance().getTxzAppList();
            for (int i = 0; i < versionInfo.rptMsgPackageList.length; i++) {
                UpgradeInfo upgradeInfo = mapUpgradeInfo.get(versionInfo.rptMsgPackageList[i].strPackageName);

                if (upgradeInfo != null && upgradeInfo.pbUpgrade != null && versionInfo.rptMsgPackageList[i] != null) {
                    LogUtil.logd("checkUpgradeFinishRunnable:" + upgradeInfo.pbUpgrade.strTargetVersion + ";version: " + versionInfo.rptMsgPackageList[i].strPackageVersion);
                    if (TextUtils.equals(upgradeInfo.pbUpgrade.strTargetVersion, versionInfo.rptMsgPackageList[i].strPackageVersion)) {
                        mapUpgradeInfo.remove(upgradeInfo.pbUpgrade.strPackageName);
                        reportUpgradeFinish(upgradeInfo);
                        //升级成功播报
                        if (upgradeInfo.pbUpgrade.strUserParam != null) {
                            JSONBuilder userParam = new JSONBuilder(upgradeInfo.pbUpgrade.strUserParam);
                            String tts = userParam.getVal("success_tip", String.class);
                            if (!TextUtils.isEmpty(tts)) {
                                TtsManager.getInstance().speakText(tts);
                            }
                        }
                    }
                }
            }
            procCheckUpgradeFinish(PROC_CHECK_UPGRADE_FINISH_TIME);
        }
    }


//    /***************************************************************
//     * **********************跟设置的交互*****************************
//     ***************************************************************/
//
//    /**
//     * 从设置发送的升级请求
//     */
//    private Runnable notifyUpgradeFromSetting = new Runnable() {
//        @Override
//        public void run() {
//            LogUtil.logd("notifyUpgradeFromSetting");
//            UpgradeInfo upgradeInfo = mapUpgradeInfo.get(ServiceManager.TXZ);
//            if (upgradeInfo != null) {
//                if (NetworkManager.getInstance().hasNet()) {
//
//                    switch (upgradeInfo.mUpgradeStatus) {
//                        case UPGRADE_STATE_DOWNLOADING:
//                            AppLogic.showToast(NativeData.getResString("RS_VOICE_UPGRADE_IS_UPGRADING"));
//                            break;
//                        case UPGRADE_STATE_NONE:
//                        case UPGRADE_STATE_DOWNLOAD_STOP:
//                        case UPGRADE_STATE_DOWNLOAD_FAIL:
//                            upgradeInfo.mFromType = TYPE_FROM_SETTINGS;
//                            upgradeInfo.mNotifyRepeat = NOTIFY_REPEAT_FROM_SETTINGS;
//                            reportUpgradeOperation(upgradeInfo, USER_OP_ACTION);
//                            showContinueDownloadConfirm(upgradeInfo);
//                            break;
//                        case UPGRADE_STATE_DOWNLOAD_FINISH:
//                        case UPGRADE_STATE_INSTALL_FAIL:
//                            upgradeInfo.mFromType = TYPE_FROM_SETTINGS;
//                            upgradeInfo.mNotifyRepeat = NOTIFY_REPEAT_FROM_SETTINGS;
//                            reportUpgradeOperation(upgradeInfo, USER_OP_ACTION);
//                            if (checkDownloadFinish(upgradeInfo)) {
//                                showContinueInstallConfirm(upgradeInfo);
//                            } else {
//                                showContinueDownloadConfirm(upgradeInfo);
//                            }
//                            break;
//                        case UPGRADE_STATE_INSTALL_FINISH:
//                            //理论上不会走这边，安装好就移除了
//                            reportUpgradeFinish(upgradeInfo);
//                            break;
//                        default:
//
//                            break;
//                    }
//                } else {
//                    AppLogic.showToast(NativeData.getResString("RS_VOICE_UPGRADE_NETWORK_ANOMALIES"));
//                }
//            } else {
//                AppLogic.showToast(NativeData.getResString("RS_VOICE_UPGRADE_NOT_AVAILABLE"));
//            }
//        }
//    };
//
//    /**
//     * 只有core升级这么操作
//     * 通知设置显示升级按钮
//     */
//    private void notifySettings(UpgradeInfo upgradeInfo, boolean bFinish) {
//        LogUtil.logd("notifySettings:" + upgradeInfo.pbUpgrade.strPackageName);
//        if (TextUtils.equals(upgradeInfo.pbUpgrade.strPackageName, ServiceManager.TXZ)) {
//            UserConf.getInstance().getFactoryConfigData().mUpgradeInfo = bFinish ? "" : upgradeInfo.pbUpgrade.strTargetVersion;
//            UserConf.getInstance().saveFactoryConfigData();
//        }
//    }


    /***************************************************************
     * **********************工具方法/类*****************************
     ***************************************************************/
    private boolean needShowProgress(String pkg, int flag) {
        //老版本的逻辑不走显示下载进度
        return (flag & PushManager.FLAG_SHOW_UPGRADE_MESSAGE_TXZ) != 0;
    }

    //TODO 需要统一命名
    private String getPackageName(String pkg) {
        if (ServiceManager.TXZ.equals(pkg)) {
            return "语音助手";
        }
        return "";
    }


    public String getDownloadTaskId(String pkg) {
        return DOWNLOAD_TASK_PRE + pkg;
    }


    public boolean equalsUpgrade(PushManager.PushCmd_NotifyUpgrade a, PushManager.PushCmd_NotifyUpgrade b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }

        if (!TextUtils.equals(a.strTargetVersion, b.strTargetVersion)) {
            return false;
        }

        if (!TextUtils.equals(a.strIncDownloadUrl, b.strIncDownloadUrl)) {
            return false;
        }

        if (!TextUtils.equals(a.strFullDownloadUrl, b.strFullDownloadUrl)) {
            return false;
        }

        if (!Arrays.equals(a.strNewMd5, b.strNewMd5)) {
            return false;
        }
        if (!Arrays.equals(a.strOldMd5, b.strOldMd5)) {
            return false;
        }

        return true;
    }

    /**
     * 获取存储路径
     *
     * @param pbUpgradeInfo
     * @return
     */
    public String genInstallApkPath(PushManager.PushCmd_NotifyUpgrade pbUpgradeInfo) {
        String suffix = "apk";
        if (pbUpgradeInfo.strInstallSuffix != null && pbUpgradeInfo.strInstallSuffix.length > 0) {
            suffix = new String(pbUpgradeInfo.strInstallSuffix);
            LogUtil.logd("has install suffix : " + suffix);
        }
        return String.format("%s/txz/install/%s_%s.%s", SDCardUtil.getSDCardPath(), pbUpgradeInfo.strPackageName, pbUpgradeInfo.strTargetVersion, suffix);
    }

    /**
     * 获取32位的小写md5
     *
     * @param path
     * @return
     */
    public static String generateMD5(String path) {
        try {
            return MD5Util.generateMD5(new File(path));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private final static char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private static String convertToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_CHARS[v >>> 4];
            hexChars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * 升级信息包装
     */
    class UpgradeInfo implements INoProguard {
        public int mNotifyCount = 0;//提示继续下载、继续安装的次数
        public int mNotifyRepeat = 3;//重复提示的次数
        public int mFromType = TYPE_FROM_NONE;
        public int mUpgradeStatus = UPGRADE_STATE_NONE;
        public String sOldApkSourcePath;
        public String sOldApkVersion;
        public String sNewApkSourcePath;
        public String sNewApkVersion;
        public PushManager.PushCmd_NotifyUpgrade pbUpgrade;

        public long mCacheSize = 0;
        public long mFullSize = 0;

        public int mNetRetryRepeat = 2;//网络恢复后重试的次数
        public int mNetRetryCount = 0;//网络出错的次数
    }


//    private synchronized void saveUpgradeInfo(HashMap<String, UpgradeInfo> mapUpgradeInfo) {
//        PreferenceUtil.getInstance().putHashMapData(PreferenceUtil.KEY_UPGRADE_TASK_LIST, mapUpgradeInfo);
//    }
//
//    private synchronized HashMap<String, UpgradeInfo> loadUpgradeInfo() {
//        return PreferenceUtil.getInstance().getHashMapData(PreferenceUtil.KEY_UPGRADE_TASK_LIST, UpgradeInfo.class);
//    }

//    private void removeLocalUpgradeInfo(String pkg) {
//        HashMap<String, UpgradeInfo> tmp = loadUpgradeInfo();
//        if (tmp.remove(pkg) != null) {
//            saveUpgradeInfo(tmp);
//        }
//    }

    // 格式化
    private String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }
        double megaByte = kiloByte / 1024;
        BigDecimal result = new BigDecimal(Double.toString(megaByte));
        return result.setScale(0, BigDecimal.ROUND_HALF_UP)
                .toPlainString() + "M";
    }


    /****************************************************************************
     * *********************************上报相关*********************************
     ***************************************************************************/

    /**
     * 上报升级完成
     *
     * @param upgradeInfo
     */
    public void reportUpgradeFinish(UpgradeInfo upgradeInfo) {
        LogUtil.logd("check upgrade finish : " + upgradeInfo.pbUpgrade.strPackageName + ";version:" + upgradeInfo.pbUpgrade.strTargetVersion);

        //通知设置安装完成
//        notifySettings(upgradeInfo, true);

        // 数据上报
        reportUpgradeRes(upgradeInfo, UiEquipment.APP_INSTALL_INSTALLING, UiEquipment.APP_COMMON_EC_OK);
    }

    /**
     * 上报状态
     *
     * @param upgradeInfo
     * @param state       升级状态，下载or安装
     * @param errorCode   错误码
     */
    public void reportUpgradeRes(UpgradeInfo upgradeInfo, int state, int errorCode) {
        LogUtil.logd("reportUpgradeError : " + upgradeInfo.pbUpgrade.strPackageName + ";version:" + upgradeInfo.pbUpgrade.strTargetVersion);

        // 数据上报
        UiEquipment.Req_RptInsAppRes reqRptInsAppRes = new UiEquipment.Req_RptInsAppRes();
        reqRptInsAppRes.uint32Stage = state;
        reqRptInsAppRes.uint32ErrCode = errorCode;
        reqRptInsAppRes.strAppName = upgradeInfo.pbUpgrade.strPackageName.getBytes();
        reqRptInsAppRes.version = upgradeInfo.pbUpgrade.strTargetVersion.getBytes();
        if (upgradeInfo.pbUpgrade.strUserParam != null) {
            JSONBuilder userParam = new JSONBuilder(upgradeInfo.pbUpgrade.strUserParam);
            userParam.remove("upgrade_info");
            userParam.remove("voice_tip");
            userParam.remove("success_tip");
            reqRptInsAppRes.strMsg = userParam.toBytes();
        }
        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_REPORT_APP_INS_STATUS, reqRptInsAppRes);
    }

    //确定按钮
    public static final int USER_OP_CONFIRM = UiEquipment.USER_OP_CONFIRM;
    //取消按钮
    public static final int USER_OP_CANCEL = UiEquipment.USER_OP_CANCEL;
    //特定触发上报,从语音/设置触发升级
    public static final int USER_OP_ACTION = 3;

    /**
     * 上报操作信息
     *
     * @param upgradeInfo 当前需要升级的信息
     * @param operation   {@link #USER_OP_CONFIRM,#USER_OP_CANCEL,#USER_OP_ACTION}
     */
    public void reportUpgradeOperation(UpgradeInfo upgradeInfo, int operation) {
        LogUtil.logd("reportUpgrade:" + upgradeInfo.pbUpgrade.strPackageName + ";operation:" + operation);
        String strParams = null;
        switch (operation) {
            case USER_OP_CONFIRM:
            case USER_OP_CANCEL:
                strParams = genDialogReportMsg(upgradeInfo);
                break;
            case USER_OP_ACTION:
                strParams = genActionReportMsg(upgradeInfo);
                break;
        }
        if (strParams != null) {
            LogUtil.logd("reportUpgrade params : " + strParams);
            UiEquipment.Req_ReportUserOp reportUserOp = new UiEquipment.Req_ReportUserOp();
            reportUserOp.operation = operation;
            reportUserOp.strInfo = strParams.getBytes();
            JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_REPORT_USER_OP, reportUserOp);
        }
    }

    //"push"
    private String genDialogReportMsg(UpgradeInfo upgradeInfo) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("scene", getScene(upgradeInfo.mFromType));
        jsonBuilder.put("pkg", upgradeInfo.pbUpgrade.strPackageName);
        if (upgradeInfo.pbUpgrade.strUserParam != null) {
            JSONBuilder userParam = new JSONBuilder(upgradeInfo.pbUpgrade.strUserParam);
            userParam.remove("upgrade_info");
            userParam.remove("voice_tip");
            userParam.remove("success_tip");
            jsonBuilder.put("param", userParam.getJSONObject());
        }
        return jsonBuilder.toString();
    }

    private String genActionReportMsg(UpgradeInfo upgradeInfo) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("action", "upgrade");
        jsonBuilder.put("scene", getScene(upgradeInfo.mFromType));
        jsonBuilder.put("pkg", upgradeInfo.pbUpgrade.strPackageName);
        if (upgradeInfo.pbUpgrade.strUserParam != null) {
            JSONBuilder userParam = new JSONBuilder(upgradeInfo.pbUpgrade.strUserParam);
            userParam.remove("upgrade_info");
            userParam.remove("voice_tip");
            userParam.remove("success_tip");
            jsonBuilder.put("param", userParam.getJSONObject());
        }
        return jsonBuilder.toString();
    }

    //"device|wx|voice|setting|continue"
    private String getScene(int type) {
        String scene = "push";
        switch (type) {
            case TYPE_FROM_PUSH:
                scene = "push";
                break;
//            case TYPE_FROM_VOICE:
//                scene = "voice";
//                break;
//            case TYPE_FROM_SETTINGS:
//                scene = "setting";
//                break;
//            case TYPE_FROM_CONTINUE:
//                scene = "continue";
//                break;
        }
        return scene;
    }

    //"device|wx|voice|setting|continue"
    private int getFromType(String from) {
        int type = TYPE_FROM_NONE;
        if (TextUtils.equals(from, "continue")) {
            type = TYPE_FROM_CONTINUE;
        } else if (TextUtils.equals(from, "voice")) {
            type = TYPE_FROM_VOICE;
        } else if (TextUtils.equals(from, "setting")) {
            type = TYPE_FROM_SETTINGS;
        } else {
            type = TYPE_FROM_PUSH;
        }
        return type;
    }

}

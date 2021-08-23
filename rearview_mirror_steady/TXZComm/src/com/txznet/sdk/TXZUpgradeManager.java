package com.txznet.sdk;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.loader.AppLogicBase;

/**
 * 可视化升级SDK Manager
 * Created zackzhou 2018/5/3
 */
public class TXZUpgradeManager {
    private static final String LOG_TAG = "TXZUpgradeManager";
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

    private static TXZUpgradeManager sInstance = new TXZUpgradeManager();

    private Boolean mDeleteApkAfterInstall;
    private UpgradeDialogTool mUpgradeDialogTool;
    private NotificationTool mNotificationTool;
    private ApkInstaller mApkInstaller;

    private TXZUpgradeManager() {

    }

    /**
     * 获取单例
     */
    public static TXZUpgradeManager getInstance() {
        return sInstance;
    }

    /**
     * 重连时需要重新通知同行者的操作放这里
     */
    void onReconnectTXZ() {
        if (mDeleteApkAfterInstall != null) {
            setDeleteApkAfterInstall(mDeleteApkAfterInstall);
        }
        if (mUpgradeDialogTool != null) {
            setUpgradeDialogTool(mUpgradeDialogTool);
        }
        if (mNotificationTool != null) {
            setNotificationTool(mNotificationTool);
            AppLogicBase.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    mNotificationTool.cancelAll();
                }
            });
        }
        if (mApkInstaller != null) {
            setApkInstaller(mApkInstaller);
        }
    }

    /**
     * 设置是否在安装后清除Apk文件，默认true
     *
     * @param bDelete 是否清除
     */
    public void setDeleteApkAfterInstall(boolean bDelete) {
        mDeleteApkAfterInstall = bDelete;
        Parcel p = Parcel.obtain();
        p.writeByte((byte) (bDelete ? 1 : 0));
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                UPGRADE_INVOKE_PREFIX + INVOKE_DELETE_APK_AFTER_INSTALL, p.marshall(), null);
        p.recycle();
    }

    /**
     * 自定义升级提示对话框
     */
    public void setUpgradeDialogTool(UpgradeDialogTool dialogTool) {
        mUpgradeDialogTool = dialogTool;
        if (mUpgradeDialogTool == null) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                    UPGRADE_INVOKE_PREFIX + INVOKE_CLEAR_UPGRADE_DIALOG, null, null);
            return;
        }
        TXZService.setCommandProcessor(UPGRADE_CMD_PREFIX + TOOL_DIALOG_PREFIX, mDialogProcessor);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                UPGRADE_INVOKE_PREFIX + INVOKE_SET_UPGRADE_DIALOG, null, null);
    }

    private TXZService.CommandProcessor mDialogProcessor = new TXZService.CommandProcessor() {

        @Override
        public byte[] process(String packageName, String command, byte[] data) {
            if (mUpgradeDialogTool == null) {
                return null;
            }
            Parcel p = Parcel.obtain();
            if (null != data) {
                p.unmarshall(data, 0, data.length);
                p.setDataPosition(0);
            }
            if (CMD_SHOW.equals(command)) {
                final String title = p.readString();
                final String content = p.readString();
                final String detailInfo = p.readString();
                AppLogicBase.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        if (mUpgradeDialogTool != null) {
                            mUpgradeDialogTool.showConfirmDialog(title, content, detailInfo, new UpgradeDialogTool.DialogListener() {
                                @Override
                                public void onClickOk() {
                                    Parcel p = Parcel.obtain();
                                    p.writeString(detailInfo);
                                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, UPGRADE_INVOKE_PREFIX + INVOKE_DIALOG_CONFIRM, p.marshall(), null);
                                    p.recycle();
                                }

                                @Override
                                public void onClickCancel() {
                                    Parcel p = Parcel.obtain();
                                    p.writeString(detailInfo);
                                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, UPGRADE_INVOKE_PREFIX + INVOKE_DIALOG_CANCEL, p.marshall(), null);
                                    p.recycle();
                                }

                                @Override
                                public void onDismiss() {
                                    Parcel p = Parcel.obtain();
                                    p.writeString(detailInfo);
                                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, UPGRADE_INVOKE_PREFIX + INVOKE_DIALOG_DISMISS, p.marshall(), null);
                                    p.recycle();
                                }
                            });
                        }
                    }
                });
            }
            p.recycle();
            return null;
        }
    };

    /**
     * 自定义升级提示对话框
     * 注意：
     * 由于升级任务是以队列的形式逐一处理，为了防止队列阻塞，在触发showConfirmDialog后，需要在一定时间内回调DialogListener，否则判断为超时(默认1分钟)
     */
    public interface UpgradeDialogTool {

        /**
         * 显示确认对话框
         *
         * @param title  标题
         * @param content  正文
         * @param detailInfo  额外附加参数(json)，扩展用，支持如下
         *                    {
         *                      "pkgName" : ${pkgName},
         *                      "version" : ${version},
         *                      "appName" : ${appName},
         *                      "totalSize" : ${totalSize}
         *                    }
         * @param listener 对话框状态监听器
         */
        @UiThread
        void showConfirmDialog(@NonNull String title, @NonNull String content, @NonNull String detailInfo, @NonNull DialogListener listener);

        /**
         * 对话框状态监听
         */
        public interface DialogListener {
            /**
             * 点击确认键
             */
            void onClickOk();

            /**
             * 点击取消键
             */
            void onClickCancel();

            /**
             * 窗口关闭
             */
            void onDismiss();
        }
    }

    /**
     * 自定义通知栏工具
     */
    public void setNotificationTool(NotificationTool notificationTool) {
        mNotificationTool = notificationTool;
        if (mNotificationTool == null) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                    UPGRADE_INVOKE_PREFIX + INVOKE_CLEAR_NOTIFICATION_TOOL, null, null);
            return;
        }
        TXZService.setCommandProcessor(UPGRADE_CMD_PREFIX + TOOL_NOTIFICATION_PREFIX, notificationProcessor);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                UPGRADE_INVOKE_PREFIX + INVOKE_SET_NOTIFICATION_TOOL, null, null);
    }

    private TXZService.CommandProcessor notificationProcessor = new TXZService.CommandProcessor() {

        @Override
        public byte[] process(String packageName, String command, byte[] data) {
            if (mNotificationTool == null) {
                return null;
            }
            Parcel p = Parcel.obtain();
            if (null != data) {
                p.unmarshall(data, 0, data.length);
                p.setDataPosition(0);
            }
            if (CMD_NOTIFY.equals(command)) {
                final String pkgName = p.readString();
                final String version = p.readString();
                final String apkName = p.readString();
                final int event = p.readInt();
                final String d = p.readString();
                AppLogicBase.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        if (mNotificationTool != null) {
                            mNotificationTool.notify(pkgName, version, apkName, event, d);
                        }
                    }
                });
            }
            p.recycle();
            return null;
        }
    };

    /**
     * 通知栏工具
     */
    public static interface NotificationTool {
        /**
         * 开始下载
         */
        int EVENT_BEGIN_DOWNLOAD = 0x0;
        /**
         * 进度值变化， data携带 "{ progress : $val }"
         */
        int EVENT_PROGRESS_CHANGE = 0x1;
        /**
         * 结束下载，data携带 "{ file_path : $val }"
         */
        int EVENT_END_DOWNLOAD = 0x2;
        /**
         * 下载失败
         */
        int EVENT_DOWNLOAD_FAILED = 0x3;
        /**
         * 开始安装
         */
        int EVENT_BEGIN_INSTALL = 0x4;
        /**
         * 安装成功
         */
        int EVENT_INSTALL_SUCCESS = 0x5;
        /**
         * 安装失败
         */
        int EVENT_INSTALL_FAILED = 0x6;

        @UiThread
        void notify(@NonNull String pkgName, @NonNull String version, @NonNull String apkName,
                    int event, @Nullable String data);

        @UiThread
        void cancelAll();
    }


    /**
     * 自定义安装器
     */
    public void setApkInstaller(ApkInstaller apkInstaller) {
        mApkInstaller = apkInstaller;
        if (mApkInstaller == null) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                    UPGRADE_INVOKE_PREFIX + INVOKE_CLEAR_APK_INSTALLER, null, null);
            return;
        }
        TXZService.setCommandProcessor(UPGRADE_CMD_PREFIX + TOOL_INSTALLER_PREFIX, apkInstallerProcessor);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                UPGRADE_INVOKE_PREFIX + INVOKE_SET_APK_INSTALLER, null, null);
    }

    private TXZService.CommandProcessor apkInstallerProcessor = new TXZService.CommandProcessor() {
        @Override
        public byte[] process(String packageName, String command, byte[] data) {
            if (mApkInstaller == null) {
                return null;
            }
            Parcel p = Parcel.obtain();
            if (null != data) {
                p.unmarshall(data, 0, data.length);
                p.setDataPosition(0);
            }
            if (CMD_INSTALL.equals(command)) {
                final String pkgName = p.readString();
                final String version = p.readString();
                String apkPath = p.readString();
                mApkInstaller.install(pkgName, version, apkPath, new ApkInstaller.ApkInstallListener() {
                    @Override
                    public void onSuccess() {
                        Parcel p = Parcel.obtain();
                        p.writeString(pkgName);
                        p.writeString(version);
                        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, UPGRADE_INVOKE_PREFIX + INVOKE_INSTALL_SUCCESS, p.marshall(), null);
                        p.recycle();
                    }

                    @Override
                    public void onFailed(@Nullable String msg) {
                        Parcel p = Parcel.obtain();
                        p.writeString(pkgName);
                        p.writeString(version);
                        p.writeString(msg == null ? "" : msg);
                        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, UPGRADE_INVOKE_PREFIX + INVOKE_INSTALL_FAILED, p.marshall(), null);
                        p.recycle();
                    }
                });
            }

            p.recycle();
            return null;
        }
    };

    /**
     * Apk应用安装器
     */
    public static interface ApkInstaller {

        /**
         * 安装指定路径下的Apk应用
         */
        @WorkerThread
        void install(@NonNull String pkgName, @NonNull String version, @NonNull String apkPath, @NonNull ApkInstallListener listener);

        /**
         * 安装状态Listener
         */
        public interface ApkInstallListener {
            /**
             * 安装成功
             */
            void onSuccess();

            /**
             * 安装失败
             *
             * @param msg 失败描述
             */
            void onFailed(@Nullable String msg);
        }
    }

}

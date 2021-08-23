package com.txznet.sdkinner;


import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZService;
import com.txznet.txz.util.MD5Util;

/**
 * 内部使用的升级工具
 */
public class TXZInnerUpgradeManager {
	private static TXZInnerUpgradeManager sInstance = new TXZInnerUpgradeManager();

	public static final String UPGRADE_CMD_PREFIX = "txz.upgradeInner.cmd."; // TXZCore -> SDK
	public static final String UPGRADE_INVOKE_PREFIX = "txz.upgradeInner.invoke."; // SDK -> TXZCore

	// dialog
	public static final String INVOKE_SET_UPGRADE_DIALOG = "set_upgrade_dialog";
	public static final String INVOKE_CLEAR_UPGRADE_DIALOG = "clear_upgrade_dialog";
	public static final String TOOL_DIALOG_PREFIX = "dialog.";
	public static final String CMD_SHOW = "show";
	public static final String CMD_DISMISS = "dismiss";
	public static final String INVOKE_DIALOG_CONFIRM = TOOL_DIALOG_PREFIX + "dialog_confirm";
	public static final String INVOKE_DIALOG_CANCEL = TOOL_DIALOG_PREFIX + "dialog_cancel";
	public static final String INVOKE_DIALOG_DISMISS = TOOL_DIALOG_PREFIX + "dialog_dismiss";

	// notification
	public static final String INVOKE_SET_NOTIFICATION_TOOL = "set_notification_tool";
	public static final String INVOKE_CLEAR_NOTIFICATION_TOOL = "clear_notification_tool";
	public static final String TOOL_NOTIFICATION_PREFIX = "notification.";
	public static final String CMD_NOTIFY = "notify";

	//status
	public static final String INVOKE_SET_UPGRADE_STATUS = "set_upgrade_status";


	private UpgradeDialogTool mUpgradeDialogTool;
	private NotificationTool mNotificationTool;

	private TXZInnerUpgradeManager() {

	}

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static TXZInnerUpgradeManager getInstance() {
		return sInstance;
	}
	
	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	public void onReconnectTXZ() {
		if (mUpgradeDialogTool != null) {
			setUpgradeDialogTool(mUpgradeDialogTool);
//			AppLogicBase.runOnUiGround(new Runnable() {
//				@Override
//				public void run() {
//					mUpgradeDialogTool.cancelAll();
//				}
//			});
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

		if (isBusy != null) {
			notifyStatusToUpgrade(isBusy);
		}
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
		TXZServiceCommandDispatcher.setCommandProcessor(UPGRADE_CMD_PREFIX + TOOL_DIALOG_PREFIX, mDialogProcessor);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				UPGRADE_INVOKE_PREFIX + INVOKE_SET_UPGRADE_DIALOG, null, null);
	}

	private TXZServiceCommandDispatcher.CommandProcessor mDialogProcessor = new TXZServiceCommandDispatcher.CommandProcessor() {

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
				final String detailInfoMd5 = MD5Util.generateMD5(detailInfo);
				AppLogicBase.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						if (mUpgradeDialogTool != null) {
							mUpgradeDialogTool.showConfirmDialog(title, content, detailInfo, new UpgradeDialogTool.DialogListener() {
								@Override
								public void onClickOk() {
									Parcel p = Parcel.obtain();
									p.writeString(detailInfoMd5);
									ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, UPGRADE_INVOKE_PREFIX + INVOKE_DIALOG_CONFIRM, p.marshall(), null);
									p.recycle();
								}

								@Override
								public void onClickCancel() {
									Parcel p = Parcel.obtain();
									p.writeString(detailInfoMd5);
									ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, UPGRADE_INVOKE_PREFIX + INVOKE_DIALOG_CANCEL, p.marshall(), null);
									p.recycle();
								}

								@Override
								public void onDismiss() {
									Parcel p = Parcel.obtain();
									p.writeString(detailInfoMd5);
									ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, UPGRADE_INVOKE_PREFIX + INVOKE_DIALOG_DISMISS, p.marshall(), null);
									p.recycle();
								}
							});
						}
					}
				});
			}else if (CMD_DISMISS.equals(command)) {
				final String title = p.readString();
				final String content = p.readString();
				final String detailInfo = p.readString();
//				final String detailInfoMd5 = MD5Util.generateMD5(detailInfo);
				AppLogicBase.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						if (mUpgradeDialogTool != null) {
							mUpgradeDialogTool.dismissConfirmDialog(title, content, detailInfo);
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
		 * 默认的升级弹框
		 */
		public final int TYPE_UPGRADE = 1;
		/**
		 * 继续升级弹框
		 */
		public final int TYPE_CONTINUE_UPGRADE = 2;
		/**
		 * 强制安装弹窗
		 */
		public final int TYPE_NOTIFY_FORCE_INSTALL = 3;
		/**
		 * 通知安装弹窗
		 */
		public final int TYPE_NOTIFY_INSTALL = 4;

		/**
		 * 显示确认对话框
		 *
		 * @param title  标题
		 * @param content  正文
		 * @param detailInfo  额外附加参数(json)，扩展用，支持如下
		 *                    {
		 *                      "pkgName" : ${pkgName},
		 *                      "version" : ${version},
		 *                      #"appName" : ${appName},
		 *                      "totalSize" : ${totalSize},
		 *                    "type":${type},
		 *                    "hintTts":${hintTts},
		 *                    "upgradeInfo":${upgradeInfo}
		 *
		 *                    }
		 * @param listener 对话框状态监听器
		 */
		@UiThread
		void showConfirmDialog(@NonNull String title, @NonNull String content, @NonNull String detailInfo, @NonNull DialogListener listener);

		@UiThread
		void cancelAll();

		@UiThread
		void dismissConfirmDialog(@NonNull String title, @NonNull String content, @NonNull String detailInfo);

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
		TXZServiceCommandDispatcher.setCommandProcessor(UPGRADE_CMD_PREFIX + TOOL_NOTIFICATION_PREFIX, notificationProcessor);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				UPGRADE_INVOKE_PREFIX + INVOKE_SET_NOTIFICATION_TOOL, null, null);
	}

	private TXZServiceCommandDispatcher.CommandProcessor notificationProcessor = new TXZServiceCommandDispatcher.CommandProcessor() {

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
		 * 结束下载
		 */
		int EVENT_END_DOWNLOAD = 0x2;
//		/**
//		 * 下载失败
//		 */
//		int EVENT_DOWNLOAD_FAILED = 0x3;
//		/**
//		 * 开始安装
//		 */
//		int EVENT_BEGIN_INSTALL = 0x4;
//		/**
//		 * 安装成功
//		 */
//		int EVENT_INSTALL_SUCCESS = 0x5;
//		/**
//		 * 安装失败
//		 */
//		int EVENT_INSTALL_FAILED = 0x6;

		@UiThread
		void notify(@NonNull String pkgName, @NonNull String version, @Nullable String apkName,
					int event, @Nullable String data);

		@UiThread
		void cancelAll();
	}


	private Boolean isBusy = null;
	/**
	 * 通知Core当前属于繁忙状态，不允许进行升级
	 * @param isBusy
	 */
	public void notifyStatusToUpgrade(boolean isBusy) {
		this.isBusy = isBusy;
		Parcel p = Parcel.obtain();
		p.writeByte((byte) (isBusy ? 1 : 0));
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, UPGRADE_INVOKE_PREFIX + INVOKE_SET_UPGRADE_STATUS, p.marshall(), null);
		p.recycle();
	}
}

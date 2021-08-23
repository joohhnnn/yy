package com.txznet.txz.module.resource;

import java.util.HashMap;
import java.util.Map;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TextUtil;
import com.txznet.comm.ui.dialog2.WinConfirmAsr;
import com.txznet.comm.util.StringUtils;
import com.txznet.txz.jni.JNIHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.text.TextUtils;
/**
 * 弹窗广播接收器，接收到广播后，监听事件返回相应的广播
 * 
 */
public class DialogActionReceiver extends BroadcastReceiver {
	public static final String SURE_TEXT = "sureText";
	public static final String CANCEL_TEXT = "cancelText";
	public static final String MESSAGE = "message";
	public static final String DIALOG_ID = "dialogID";
	public static final String FUNCTION = "function";
	static Map<String, WinConfirmAsr> dialogMap = new HashMap<String, WinConfirmAsr>();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		JNIHelper.logd("recive action: " + action);
		if (action.equals("com.txznet.txz.dialog.confirmAsr")) {
			DialogInfo dialogInfo = readDialogInfo(intent);
			if (dialogInfo != null && "create".equals(dialogInfo.function)) {// 创建dialog
				createDialog(dialogInfo);
			} else if (dialogInfo != null
					&& "dismiss".equals(dialogInfo.function)) {// 关闭dialog
				dismissDialog(dialogInfo);
			}
		}

	}

	/**
	 * 关闭dialog
	 * 
	 * @param dialogInfo
	 */
	private void dismissDialog(DialogInfo dialogInfo) {
		WinConfirmAsr win = dialogMap.get(dialogInfo.dialogID);
		if (win != null) {
			win.dismiss("receive dismiss dialog");
			Intent intent = new Intent("com.txznet.txz.dialog.confirmAsr.reply");
			intent.putExtra("type", "cancel");
			intent.putExtra("dialogID", dialogInfo.dialogID);
			GlobalContext.get().sendBroadcast(intent);
		}
	}

	/**
	 * 创建dialog
	 * 
	 * @param dialogInfo
	 */
	private void createDialog(final DialogInfo dialogInfo) {
		WinConfirmAsr.WinConfirmAsrBuildData buildData = new WinConfirmAsr.WinConfirmAsrBuildData();
		buildData.setMessageText(dialogInfo.message);
		buildData.setSureText(dialogInfo.sureText, new String[] { dialogInfo.sureText });
		buildData.setCancelText(dialogInfo.cancelText, new String[] { dialogInfo.cancelText });
		buildData.setHintTts(dialogInfo.message);
		WinConfirmAsr win = new WinConfirmAsr(buildData) {
			@Override
			public void onClickOk() {
				Intent intent = new Intent(
						"com.txznet.txz.dialog.confirmAsr.reply");
				intent.putExtra("type", "sure");
				intent.putExtra("dialogID", dialogInfo.dialogID);
				GlobalContext.get().sendBroadcast(intent);

			}

			@Override
			public void onClickCancel() {
				Intent intent = new Intent(
						"com.txznet.txz.dialog.confirmAsr.reply");
				intent.putExtra("type", "cancel");
				intent.putExtra("dialogID", dialogInfo.dialogID);
				GlobalContext.get().sendBroadcast(intent);
			}

			@Override
			public String getReportDialogId() {
				return "txz_receive_confirmAsr";
			}
		};

		dialogMap.put(dialogInfo.dialogID, win);
		win.show();
	}

	public class DialogInfo {
		public String message = "确定吗？";
		public String sureText = "确定";
		public String cancelText = "取消";
		public String dialogID;
		public String function;// 功能，create/dismiss

	}

	/**
	 * 读取dialog的信息
	 * 
	 * @param intent
	 */
	private DialogInfo readDialogInfo(Intent intent) {
		DialogInfo dialogInfo = new DialogInfo();
		if (intent.hasExtra(SURE_TEXT)
				&& StringUtils.isNotEmpty(intent.getStringExtra(SURE_TEXT))) {
			dialogInfo.sureText = intent.getStringExtra(SURE_TEXT);
		}
		if (intent.hasExtra(CANCEL_TEXT)
				&& StringUtils.isNotEmpty(intent.getStringExtra(CANCEL_TEXT))) {
			dialogInfo.cancelText = intent.getStringExtra(CANCEL_TEXT);
		}
		if (intent.hasExtra(MESSAGE)
				&& StringUtils.isNotEmpty(intent.getStringExtra(MESSAGE))) {
			dialogInfo.message = intent.getStringExtra(MESSAGE);
		}
		if (intent.hasExtra(FUNCTION)
				&& StringUtils.isNotEmpty(intent.getStringExtra(FUNCTION))) {
			dialogInfo.function = intent.getStringExtra(FUNCTION);
		} else {
			return null;
		}
		if (intent.hasExtra(DIALOG_ID)
				&& StringUtils.isNotEmpty(intent.getStringExtra(DIALOG_ID))) {
			dialogInfo.dialogID = intent.getStringExtra(DIALOG_ID);
		} else {
			return null;
		}

		return dialogInfo;

	}

}

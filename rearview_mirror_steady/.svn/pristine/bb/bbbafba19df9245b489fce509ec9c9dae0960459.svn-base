package com.txznet.txz.component.nav.tx.internal;

import com.txznet.txz.component.nav.tx.internal.SRActionDispatcher.OnFeedbackListener;
import com.txznet.txz.component.nav.tx.internal.TNBroadcastReceiver.OnNaviMsgListener;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.tts.TtsManager;

import android.content.Context;
import android.os.Bundle;

public class TNBroadcastManager {
	private String mCmd;

	private static class LayzerHolder {
		private static final TNBroadcastManager sIntance = new TNBroadcastManager();
	}

	private TNBroadcastManager() {
	}

	public static TNBroadcastManager getInstance() {
		return LayzerHolder.sIntance;
	}

	private Context mContext;
	private TNFeedbackListener mFeedbackListener;

	public void init(Context context) {
		mContext = context.getApplicationContext();
		SRActionDispatcher.getInstance().init();
		TNBroadcastReceiver.getInstance().init();
		TNBroadcastSender.getInstance().setConfirmDialogAutoDimissDelay(20);
		SRActionDispatcher.getInstance().setFeedbackListener(new OnFeedbackListener() {

			@Override
			public void onTimeOut() {
				if (mFeedbackListener != null) {
					mFeedbackListener.onTimeOut();
				}
			}

			@Override
			public void onFeedback(int errorCode, Bundle data, String strTtsWording) {
				int keyType = data.getInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE);
				switch (keyType) {
				case ExternalDefaultBroadcastKey.TYPE.REQ_CUR_ADDRESS: {
					String address = data.getString(ExternalDefaultBroadcastKey.KEY.ADDRESS);
					if (mFeedbackListener != null) {
						mFeedbackListener.onRevWhereAmI(errorCode, address, strTtsWording);
					}
					break;
				}
				case ExternalDefaultBroadcastKey.TYPE.REQ_REMAIN_TIME_DISTANCE: {
					int opera = data.getInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA);
					if (opera == 0) {
						int time = data.getInt(ExternalDefaultBroadcastKey.KEY.TIME);
						if (mFeedbackListener != null) {
							mFeedbackListener.onRevRemainTime(errorCode, time, strTtsWording);
						}
					} else {
						int distance = data.getInt(ExternalDefaultBroadcastKey.KEY.DISTANCE);
						if (mFeedbackListener != null) {
							mFeedbackListener.onRevRemainDistance(errorCode, distance, strTtsWording);
						}
					}
					break;
				}
				case ExternalDefaultBroadcastKey.TYPE.NAVI_SET_HOME_COMPANY_ADDR: {
					int type = data.getInt(ExternalDefaultBroadcastKey.KEY.EXTRA_TYPE);
					int rst = data.getInt(ExternalDefaultBroadcastKey.KEY.RST);
					if (rst == 0) { // 设置失败
						// TODO 修改播报文本
						TtsManager.getInstance().speakText("导航设置地址失败");
					} else {
						// 设置成功
						JNIHelper.logd("feedback NAVI_SET_HOME_COMPANY_ADDR:" + type);
						if (type == 0) {
							TNBroadcastSender.getInstance().queryHome();
						} else if (type == 1) {
							TNBroadcastSender.getInstance().queryCompany();
						}
					}
					break;
				}
				default: {
					if (mFeedbackListener != null) {
						mFeedbackListener.onFeedback(errorCode, data, strTtsWording);
					}
				}
				}
			}
		});
	}

	public void unInit() {
		SRActionDispatcher.getInstance().unInit();
		TNBroadcastReceiver.getInstance().unInit();
	}

	public void setMsgListener(OnNaviMsgListener listener) {
		TNBroadcastReceiver.getInstance().setMsgListener(listener);
	}

	public void setOnFeedbackListener(TNFeedbackListener listener) {
		mFeedbackListener = listener;
	}

	public Context getContext() {
		return mContext;
	}

	public void setCmd(String cmd) {
		mCmd = cmd;
	}

	public String getCmd() {
		return mCmd;
	}
}

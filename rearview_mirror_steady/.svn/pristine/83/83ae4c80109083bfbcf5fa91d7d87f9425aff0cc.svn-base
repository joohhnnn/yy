package com.txznet.txz.component.nav.kgo.internal;

import java.util.Map;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.util.runnables.Runnable1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;

public class KgoBroadcastDispatcher {
	private long mSessionId;
	private OnReceiverListener mListener;
	private static KgoBroadcastDispatcher dispatcher = new KgoBroadcastDispatcher();

	private KgoBroadcastDispatcher() {

	}

	public static KgoBroadcastDispatcher getInstance() {
		return dispatcher;
	}

	public void init(Context context) {
		IntentFilter intentFilter = new IntentFilter(KgoKeyConstants.BROADCAST_ACTION.BROADCAST_RECV);
		context.registerReceiver(mRecvReceiver, intentFilter);
		LogUtil.logd("register kgo rec... action:" + KgoKeyConstants.BROADCAST_ACTION.BROADCAST_RECV);
	}

	public void unInit(Context context) {
		context.unregisterReceiver(mRecvReceiver);
	}

	public void setOnReceiverListener(OnReceiverListener listener) {
		mListener = listener;
	}

	public void action(int action) {
		action(action, 0);
	}

	public void action(int action, int timeout) {
		action(action, (Bundle) null, timeout);
	}

	public void action(int action, Bundle bundle, int timeout) {
		action(action, bundle, null, timeout);
	}

	public void action(int action, Map<String, Object> kvs, int timeout) {
		action(action, null, kvs, timeout);
	}

	public void action(int action, Bundle bundle, Map<String, Object> kvs, int timeout) {
		sendInnerAction(action, bundle, kvs, timeout);
	}

	private void sendInnerAction(int action, Bundle bundle, Map<String, Object> kvs, int timeout) {
		// 产生SessionId，保证会话
		mSessionId = SystemClock.elapsedRealtime();
		if (timeout > 0) {
			// 超时机制
			AppLogic.removeBackGroundCallback(mTimeoutTask);
			AppLogic.runOnBackGround(mTimeoutTask, timeout);
		}

		sendBroadcastAction(action, bundle, kvs);
	}

	private void sendBroadcastAction(int action, Bundle bundle, Map<String, Object> kvs) {
		Intent intent = new Intent(KgoKeyConstants.BROADCAST_ACTION.BROADCAST_SEND);
		intent.putExtra(KgoKeyConstants.KEY.SESSION_ID, mSessionId);
		intent.putExtra(KgoKeyConstants.KEY.ACTION, action);
		intent.putExtra(KgoKeyConstants.KEY.SOURCE_APP, "txz");
		if (bundle != null && !bundle.isEmpty()) {
			intent.putExtras(bundle);
		}
		if (kvs != null && kvs.size() > 0) {
			for (String key : kvs.keySet()) {
				Object val = kvs.get(key);
				if (val.getClass() == Double.class) {
					intent.putExtra(key, (Double) val);
				} else if (val.getClass() == String.class) {
					intent.putExtra(key, (String) val);
				} else if (val.getClass() == Integer.class) {
					intent.putExtra(key, (Integer) val);
				} else if (val.getClass() == Float.class) {
					intent.putExtra(key, (Float) val);
				} else {
					LogUtil.loge("unknow cls type！");
				}
			}
		}
		intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		GlobalContext.get().sendBroadcast(intent);
		LogUtil.logd("send action:" + action + ",bundle:" + intent.getExtras() + ",mSessionId:" + mSessionId);
	}

	private BroadcastReceiver mRecvReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String sessison_id = intent.getStringExtra(KgoKeyConstants.KEY.SESSION_ID);
			int key_type = intent.getIntExtra(KgoKeyConstants.KEY.ACTION, -999);
			LogUtil.logd("recv kgo session_id:" + sessison_id + ",req_session:" + mSessionId + ",keyType:" + key_type + ",mListener:" + mListener);
			// 切到后台线程执行
			AppLogic.runOnBackGround(new Runnable1<Intent>(intent) {

				@Override
				public void run() {
					try {
						long sessison_id = mP1.getLongExtra(KgoKeyConstants.KEY.SESSION_ID, -999);
						AppLogic.removeBackGroundCallback(mTimeoutTask);
						// TODO 如果是导航主动发出的广播怎么处理
						// return;

						if (mListener != null) {
							mListener.onReceiver(KgoKeyConstants.ERROR_CODE.ERROR_SUCCESS, mP1);
						}
					} catch (Exception e) {
						LogUtil.loge(e.getMessage());
					}
				}
			}, 0);
		}
	};

	Runnable mTimeoutTask = new Runnable() {

		@Override
		public void run() {
			mSessionId = 0;
			if (mListener != null) {
				mListener.onReceiver(KgoKeyConstants.ERROR_CODE.ERROR_TIMEOUT, null);
			}
		}
	};

	public interface OnReceiverListener {
		/**
		 * @param errorCode
		 *            错误码，超时或者其它
		 * @param intent
		 *            导航返回的数据
		 */
		public void onReceiver(int errorCode, Intent intent);
	}
}
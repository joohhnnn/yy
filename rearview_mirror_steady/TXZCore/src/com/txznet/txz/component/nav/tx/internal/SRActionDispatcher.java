package com.txznet.txz.component.nav.tx.internal;

import java.util.Map;

import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;

/**
 * 语音指令分发者，负责处理导航具体指令执行
 */
public class SRActionDispatcher {
    private static final String TAG = SRActionDispatcher.class.getSimpleName();
    public long mTimeoutDelay;
    private String mSessionid = "";
    private OnFeedbackListener mListener = null;
    private Map<String,String> mFeedbackTtsMap = null;
    private static final String FEEDBACK_TTS_TEMPLATE_ID_PREFIX = "m";
    private static volatile SRActionDispatcher sInstance = null;

    public static SRActionDispatcher getInstance() {
        if (null == sInstance) {
            synchronized (SRActionDispatcher.class) {
                if (null == sInstance) {
                    sInstance = new SRActionDispatcher();
                }
            }
        }
        return sInstance;
    }

    public void init() {
		IntentFilter filter = new IntentFilter(ExternalDefaultBroadcastKey.BROADCAST_ACTION.FEEDBACK);
		TNBroadcastManager.getInstance().getContext().registerReceiver(mFeedbackReceiver, filter);
    }

    public void unInit() {
        TNBroadcastManager.getInstance().getContext().unregisterReceiver(mFeedbackReceiver);
    }

    private SRActionDispatcher() {
    }

    public interface OnFeedbackListener {
        /**
         *
         * @param errorCode //0:成功//-1:不支持//1:执行失败//2:2次交互->选择第几个//3:2次交互重试
         * @param data  返回具体数据
         * @param strTtsWording 建议TTS术语
         */
        public   void onFeedback(int errorCode, Bundle data, String strTtsWording);

        /**
         * 超时
         */
        public   void onTimeOut();
    }

    public void setFeedbackListener(OnFeedbackListener l){
        mListener = l;
    }

	public void action(int action, Bundle bundle) {
		// 产生sessionid并缓存，用于超时后中止本次会话
		if (!bundle.containsKey(ExternalDefaultBroadcastKey.KEY.TAG)) {
			mSessionid = SystemClock.elapsedRealtime() + "";
		} else {
			mSessionid = bundle.getString(ExternalDefaultBroadcastKey.KEY.TAG);
		}

		// 超时机制: 超过mTimeoutDelay无响应则返回超时
		if (mTimeoutDelay <= 0) {
			// 不超时
		} else {
			if (bundle != null) {
				boolean timeout = bundle.getBoolean("notimeout");
				if (!timeout) {
					AppLogic.removeBackGroundCallback(mTimeoutRunnable);
					AppLogic.runOnBackGround(mTimeoutRunnable, mTimeoutDelay);
				}
			} else {
				AppLogic.removeBackGroundCallback(mTimeoutRunnable);
				AppLogic.runOnBackGround(mTimeoutRunnable, mTimeoutDelay);
			}
		}

		sendActionBroadCast(action, bundle);
	}

    private Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            mSessionid = "";
            if(mListener != null){
                mListener.onTimeOut();
            }
        }
    };

	private void sendActionBroadCast(int action, Bundle bundle) {
		Intent intent = new Intent(ExternalDefaultBroadcastKey.BROADCAST_ACTION.RECV);
		if (!bundle.containsKey(ExternalDefaultBroadcastKey.KEY.TAG)) {
			intent.putExtra(ExternalDefaultBroadcastKey.KEY.TAG, mSessionid + "");
		}
		intent.putExtra(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, action);
		if (bundle != null && !bundle.isEmpty()) {
			intent.putExtras(bundle);
		}
		intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

		TNBroadcastManager.getInstance().getContext().sendBroadcast(intent);

		String strActionLog = "action. raw text = " + TNBroadcastManager.getInstance().getCmd() + ", Action = " + action
				+ ", object = " + bundle + ", mSessionid:" + mSessionid;
		JNIHelper.logd(strActionLog);
	}

	private BroadcastReceiver mFeedbackReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String sessionid = intent.getStringExtra(ExternalDefaultBroadcastKey.KEY.TAG);
			JNIHelper.logd("FeedbackReceiver onReceive mSessionid:" + mSessionid + " Feedbacksessionid:" + sessionid);

			// 会话非法则无响应
			if (TextUtils.isEmpty(sessionid) || !sessionid.equals(mSessionid)) {
				JNIHelper.logw("session id error!");
				return;
			}

			AppLogic.removeBackGroundCallback(mTimeoutRunnable);
			int errorCode = intent.getIntExtra(ExternalDefaultBroadcastKey.KEY.FEEDBACK_CODE,
					ExternalDefaultBroadcastKey.FEEDBACK_CODE.SUCCUESS);
			String word = intent.getStringExtra(ExternalDefaultBroadcastKey.KEY.FEEDBACK_WORD);
			Bundle data = intent.getExtras();
			if (!data.containsKey(ExternalDefaultBroadcastKey.KEY.TAG)) {
				data.putString(ExternalDefaultBroadcastKey.KEY.TAG, sessionid);
			}
			if (mListener != null) {
				mListener.onFeedback(errorCode, data, word);
			}
		}
	};
}

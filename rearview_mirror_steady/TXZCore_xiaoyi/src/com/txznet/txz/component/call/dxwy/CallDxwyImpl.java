package com.txznet.txz.component.call.dxwy;

import java.util.HashSet;
import java.util.Set;

import android.content.Intent;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.call.ICall;
import com.txznet.txz.jni.JNIHelper;

public class CallDxwyImpl implements ICall {
	static Set<CallDxwyImpl> sCallListeners = new HashSet<CallDxwyImpl>();

	public CallDxwyImpl() {
		sCallListeners.add(this);
	}

	@Override
	public int getState() {
		return mRealtimeState;
	}

	@Override
	public int initialize(final IInitCallback oRun) {
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				oRun.onInit(true);
			}
		}, 0);
		return 0;
	}

	@Override
	public void release() {
	}

	String mMakingcallNumber;

	@Override
	public boolean makeCall(String num, String name) {
		Intent callIntent = new Intent("bluetooth_call_from_third_action");
		callIntent.putExtra("actionType", "dialCall");
		callIntent.putExtra("number", num);
		callIntent.putExtra("name", name);
		GlobalContext.get().sendBroadcast(callIntent);
		return true;
	}

	@Override
	public boolean stopCall() {
		Intent callIntent = new Intent("bluetooth_call_from_third_action");
		callIntent.putExtra("actionType", "hangupCall");
		GlobalContext.get().sendBroadcast(callIntent);
		return true;
	}

	@Override
	public boolean answerCall() {
		Intent callIntent = new Intent("bluetooth_call_from_third_action");
		callIntent.putExtra("actionType", "answerCall");
		GlobalContext.get().sendBroadcast(callIntent);
		return true;
	}

	@Override
	public boolean rejectCall() {
		Intent refuseIntent = new Intent("bluetooth_call_from_third_action");
		refuseIntent.putExtra("actionType", "refuseCall");
		GlobalContext.get().sendBroadcast(refuseIntent);
		return true;
	}

	@Override
	public boolean silenceCall() {
		// TODO 暂时不需要支持
		return false;
	}

	@Override
	public boolean sendKey(String key) {
		// TODO 暂时不需要支持
		return false;
	}

	String mIncomingNumber;

	ICallStateListener mStateListener;

	@Override
	public boolean setStateListener(ICallStateListener listener) {
		mStateListener = listener;
		return true;
	}

	int mRealtimeState = STATE_IDLE;
	int mRecordState = STATE_IDLE;

	void onCallStateUpdate(Intent intent) {
		String action = intent.getAction();
		if ("action.PHONE_STATE_RECOVERY".equals(action)) {
			mRealtimeState = STATE_IDLE;
		} else if ("action.COME.PHONE".equals(action)) {
			mRealtimeState = STATE_RINGING;
		} else if ("action.ON.CALLING".equals(action)) {
			mRealtimeState = STATE_OFFHOOK;
		} else if ("action.OUT.CALL".equals(action)) {
			//mRealtimeState = STATE_MAKING;
			mRealtimeState = STATE_OFFHOOK;
		} else {
			JNIHelper.logw("unknow call intent");
			return;
		}

		if (mRealtimeState == mRecordState)
			return;

		// 状态变繁忙
		if (mRealtimeState != STATE_IDLE && mRecordState == STATE_IDLE
				&& mStateListener != null)
			mStateListener.onBusy();

		switch (mRealtimeState) {
		case STATE_IDLE:
			if (mStateListener != null) {
				if (mRecordState == STATE_RINGING) {
					mStateListener.onIncomingReject(mIncomingNumber, null);
				}
				mStateListener.onCallStop();
			}
			break;
		case STATE_RINGING:
			mIncomingNumber = intent.getStringExtra("phoneNumber");
			if (mStateListener != null)
				mStateListener.onIncomingRing(mIncomingNumber, null);
			break;
		case STATE_MAKING:
			mMakingcallNumber = intent
					.getStringExtra(android.content.Intent.EXTRA_PHONE_NUMBER);
			if (mStateListener != null)
				mStateListener.onMakecall(mMakingcallNumber, null);
			break;
		case STATE_OFFHOOK:
			if (mStateListener != null) {
				if (mRecordState == STATE_RINGING)
					mStateListener.onIncomingAnswer(mIncomingNumber, null);
				mStateListener.onOffhook();
			}
			break;
		}

		// 非接通状态，清空号码记录
		if (mRealtimeState != STATE_OFFHOOK) {
			if (mRealtimeState != STATE_RINGING) {
				mIncomingNumber = null;
			}
			if (mRealtimeState != STATE_MAKING) {
				mMakingcallNumber = null;
			}
		}

		mRecordState = mRealtimeState;
	}

	static void onSysStateUpdate(Intent intent) {
		for (CallDxwyImpl call : sCallListeners) {
			call.onCallStateUpdate(intent);
		}
	}
}

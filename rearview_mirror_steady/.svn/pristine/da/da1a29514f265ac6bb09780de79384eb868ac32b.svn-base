package com.txznet.txz.component.call.dxwy;

import org.json.JSONObject;

import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.call.ICall;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.call.CallManager;

public class CallToolImpl implements ICall {
	public CallToolImpl() {
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
		CallManager.getInstance().sendInvoke("tool.call.getStatus", null,
				new GetDataCallback() {
					@Override
					public void onGetInvokeResponse(ServiceData data) {
						if (data != null) {
							if (data.getString().equals("ringring")) {
								mRealtimeState = STATE_RINGING;
								return;
							}
							if (data.getString().equals("offhook")) {
								mRealtimeState = STATE_OFFHOOK;
								return;
							}
							mRealtimeState = STATE_IDLE;
						}
					}
				});
		return 0;
	}

	@Override
	public void release() {
	}

	static String mMakingcallNumber;

	@Override
	public boolean makeCall(String num, String name) {
		try {
			JSONObject json = new JSONObject();
			json.put("name", name);
			json.put("num", num);
			return CallManager.getInstance().sendInvoke("tool.call.makeCall",
					json.toString().getBytes(), null);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean stopCall() {
		return CallManager.getInstance().sendInvoke("tool.call.hangupCall",
				null, null);
	}

	@Override
	public boolean answerCall() {
		return CallManager.getInstance().sendInvoke("tool.call.acceptIncoming",
				null, null);
	}

	@Override
	public boolean rejectCall() {
		return CallManager.getInstance().sendInvoke("tool.call.rejectIncoming",
				null, null);
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

	static String mIncomingNumber;

	static ICallStateListener mStateListener;

	@Override
	public boolean setStateListener(ICallStateListener listener) {
		mStateListener = listener;
		return true;
	}

	static int mRealtimeState = STATE_IDLE;
	static int mRecordState = STATE_IDLE;

	public static void onCallStateUpdate(String command, byte[] data) {
		if (command.equals("notifyMakeCall")) {
			mRealtimeState = STATE_MAKING;
		} else if (command.equals("notifyIncoming")) {
			mRealtimeState = STATE_RINGING;
		} else if (command.equals("notifyOffhook")) {
			mRealtimeState = STATE_OFFHOOK;
		} else if (command.equals("notifyIdle")) {
			mRealtimeState = STATE_IDLE;
		} else {
			JNIHelper.logw("unknow call notify");
			return;
		}

		JNIHelper.logd("onCallStateUpdate: from " + mRecordState + "to"
				+ mRealtimeState);

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
			String name = null;
			boolean tts = true,
			asr = true;
			try {
				JSONObject json = new JSONObject(new String(data));
				mIncomingNumber = json.optString("num", "");
				if (json.has("name"))
					name = json.getString("name");
				if (json.has("tts"))
					tts = json.getBoolean("tts");
				if (json.has("asr"))
					asr = json.getBoolean("asr");
				CallManager.getInstance().setIncomingOption(tts, asr);
			} catch (Exception e) {

			}
			if (mStateListener != null)
				mStateListener.onIncomingRing(mIncomingNumber, name);
			break;
		case STATE_MAKING:
			try {
				JSONObject json = new JSONObject(new String(data));
				mMakingcallNumber = json.getString("num");
			} catch (Exception e) {

			}
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

}

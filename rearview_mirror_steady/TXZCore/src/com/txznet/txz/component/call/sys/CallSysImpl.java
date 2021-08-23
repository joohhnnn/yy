package com.txznet.txz.component.call.sys;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.call.ICall;

public class CallSysImpl implements ICall {

	static Set<CallSysImpl> sCallListeners = new HashSet<CallSysImpl>();

	public CallSysImpl() {
		sCallListeners.add(this);
	}

	// //////////////////////////////////////////////////////////////////////////////////

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

	// //////////////////////////////////////////////////////////////////////////////////

	boolean makeCall_TelephonyManager(String num, String name) {
		TelephonyManager tm = (TelephonyManager) GlobalContext.get()
				.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Class<?> c = Class.forName(tm.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			Object telephonyService = (Object) m.invoke(tm);
			c = Class.forName(telephonyService.getClass().getName());
			Class<?>[] args1 = new Class[2];
			args1[0] = String.class;
			args1[1] = String.class;
			m = c.getDeclaredMethod("call", args1);
			m.setAccessible(true);
			m.invoke(telephonyService,
					GlobalContext.get().getApplicationInfo().packageName, num);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	String mMakingcallNumber;

	@Override
	public boolean makeCall(String num, String name) {
		mMakingcallNumber = num;
		return makeCall_TelephonyManager(num, name);
	}

	boolean stopCall_TelephonyManager() {
		TelephonyManager tm = (TelephonyManager) GlobalContext.get()
				.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Class<?> c = Class.forName(tm.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			Object telephonyService = (Object) m.invoke(tm);
			c = Class.forName(telephonyService.getClass().getName());
			m = c.getDeclaredMethod("endCall");
			m.setAccessible(true);
			m.invoke(telephonyService);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	boolean stopCall_HeadsetPress() {
		Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
		buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
				KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
		try {
			GlobalContext.get().sendOrderedBroadcast(buttonDown,
					"android.permission.CALL_PRIVILEGED");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	boolean stopCall_HeadsetUnplugged() {
		Intent headSetUnPluggedintent = new Intent(Intent.ACTION_HEADSET_PLUG);
		headSetUnPluggedintent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
		//这里应该是填0的吧？// 0 = unplugged 1 = Headset with microphone 2 =
		// Headset without microphone
		headSetUnPluggedintent.putExtra("state", 0);
		headSetUnPluggedintent.putExtra("name", "Headset");
		// TODO: Should we require a permission?
		try {
			GlobalContext.get().sendOrderedBroadcast(headSetUnPluggedintent, null);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	boolean stopCall_TelephonyService() {
		try {
			// String serviceManagerName = "android.os.IServiceManager";
			String serviceManagerName = "android.os.ServiceManager";
			String serviceManagerNativeName = "android.os.ServiceManagerNative";
			String telephonyName = "com.android.internal.telephony.ITelephony";

			Class<?> telephonyClass;
			Class<?> telephonyStubClass;
			Class<?> serviceManagerClass;
			// Class<?> serviceManagerStubClass;
			Class<?> serviceManagerNativeClass;
			// Class<?> serviceManagerNativeStubClass;

			// Method telephonyCall;
			Method telephonyEndCall;
			// Method telephonyAnswerCall;
			// Method getDefault;

			// Method[] temps;
			// Constructor<?>[] serviceManagerConstructor;

			// Method getService;
			Object telephonyObject;
			Object serviceManagerObject;

			telephonyClass = Class.forName(telephonyName);
			telephonyStubClass = telephonyClass.getClasses()[0];
			serviceManagerClass = Class.forName(serviceManagerName);
			serviceManagerNativeClass = Class.forName(serviceManagerNativeName);

			Method getService = // getDefaults[29];
			serviceManagerClass.getMethod("getService", String.class);

			Method tempInterfaceMethod = serviceManagerNativeClass.getMethod(
					"asInterface", IBinder.class);

			Binder tmpBinder = new Binder();
			tmpBinder.attachInterface(null, "fake");

			serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
			IBinder retbinder = (IBinder) getService.invoke(
					serviceManagerObject, "phone");
			Method serviceMethod = telephonyStubClass.getMethod("asInterface",
					IBinder.class);

			telephonyObject = serviceMethod.invoke(null, retbinder);
			// telephonyCall = telephonyClass.getMethod("call", String.class);
			telephonyEndCall = telephonyClass.getMethod("endCall");
			// telephonyAnswerCall =
			// telephonyClass.getMethod("answerRingingCall");

			telephonyEndCall.invoke(telephonyObject);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean stopCall() {
		boolean ret = false;
		if (stopCall_TelephonyManager())
			ret = true;
		if (stopCall_HeadsetPress())
			ret = true;
		if (stopCall_HeadsetUnplugged())
			ret = true;
		if (stopCall_TelephonyService())
			ret = true;
		return ret;
	}

	boolean answerCall_HeadsetPress() {
		Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
		buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
				KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
		try {
			GlobalContext.get().sendOrderedBroadcast(buttonUp,
					"android.permission.CALL_PRIVILEGED");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	boolean answerCall_HeadsetPlugin() {
		Intent headSetUnPluggedintent = new Intent(Intent.ACTION_HEADSET_PLUG);
		headSetUnPluggedintent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
		// 0 = unplugged 1 = Headset with microphone 2 = Headset without
		// microphone
		headSetUnPluggedintent.putExtra("state", 1);
		headSetUnPluggedintent.putExtra("name", "Headset");
		// TODO: Should we require a permission?
		try {
			GlobalContext.get().sendOrderedBroadcast(headSetUnPluggedintent, null);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean answerCall() {
		boolean ret = false;
		if (answerCall_HeadsetPress())
			ret = true;
		if (answerCall_HeadsetPlugin())
			ret = true;
		return ret;
	}

	@Override
	public boolean rejectCall() {
		return stopCall();
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

	ICallStateListener mStateListener;

	@Override
	public boolean setStateListener(ICallStateListener listener) {
		mStateListener = listener;
		return true;
	}

	static final int CALL_STATE_RING_UNKNOW = 99;
	int mRecordState = STATE_IDLE;

	String mIncomingNumber;

	boolean isRingingState(int state) {
		return state == STATE_RINGING || state == CALL_STATE_RING_UNKNOW;
	}

	void onCallStateUpdate(int sysState, Intent intent) {
		if (sysState == mRecordState)
			return;

		// 状态变繁忙
		if (sysState != STATE_IDLE && mRecordState == STATE_IDLE
				&& mStateListener != null)
			mStateListener.onBusy();

		switch (sysState) {
		case STATE_IDLE:
			if (mStateListener != null) {
				if (isRingingState(mRecordState)) {
					mStateListener.onIncomingReject(mIncomingNumber, null);
				}
				mStateListener.onCallStop();
			}
			break;
		case STATE_RINGING:
			if (intent == null) {
				mRecordState = CALL_STATE_RING_UNKNOW;
				break;
			}
			mIncomingNumber = intent.getStringExtra("incoming_number");
			if (mStateListener != null)
				mStateListener.onIncomingRing(mIncomingNumber, null);
			break;
		case STATE_MAKING:
			mMakingcallNumber = intent
					.getStringExtra(android.content.Intent.EXTRA_PHONE_NUMBER);
			if (mStateListener != null)
				mStateListener.onMakecall(mIncomingNumber, null);
			break;
		case STATE_OFFHOOK:
			if (mStateListener != null) {
				if (isRingingState(mRecordState))
					mStateListener.onIncomingAnswer(mIncomingNumber, null);
				mStateListener.onOffhook();
			}
			break;
		}

		// 非接通状态，清空号码记录
		if (sysState != STATE_OFFHOOK) {
			if (isRingingState(sysState) == false) {
				mIncomingNumber = null;
			}
			if (sysState != STATE_MAKING) {
				mMakingcallNumber = null;
			}
		}

		mRecordState = sysState;

		if (mRecordState != STATE_IDLE)
			startMonitorPhoneState();
		else
			stopMonitorPhoneState();
	}

	static void onSysStateUpdate(int sysState, Intent intent) {
		for (CallSysImpl call : sCallListeners) {
			call.onCallStateUpdate(sysState, intent);
		}
	}

	@Override
	public int getState() {
		refreshState();

		TelephonyManager tm = (TelephonyManager) GlobalContext.get()
				.getSystemService(Service.TELEPHONY_SERVICE);
		int state = tm.getCallState();
		switch (state) {
		case TelephonyManager.CALL_STATE_IDLE:
			return STATE_IDLE;
		case TelephonyManager.CALL_STATE_RINGING:
			return STATE_RINGING;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			return STATE_OFFHOOK;
		}
		return STATE_IDLE;
	}

	// //////////////////////////////////////////////////////////////////////////

	static Runnable mUpdateCallState = new Runnable() {
		@Override
		public void run() {
			TelephonyManager tm = (TelephonyManager) GlobalContext.get()
					.getSystemService(Service.TELEPHONY_SERVICE);
			switch (tm.getCallState()) {
			case TelephonyManager.CALL_STATE_RINGING:
				CallSysImpl.onSysStateUpdate(ICall.STATE_RINGING, null);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				CallSysImpl.onSysStateUpdate(ICall.STATE_OFFHOOK, null);
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				CallSysImpl.onSysStateUpdate(ICall.STATE_IDLE, null);
				break;
			}
		}
	};

	static void refreshState() {
		AppLogic.removeUiGroundCallback(mUpdateCallState);
		AppLogic.runOnUiGround(mUpdateCallState, 0);
		AppLogic.runOnUiGround(mUpdateCallState, 1000);
	}

	static Runnable mUpdatePhoneStateLoop = new Runnable() {
		@Override
		public void run() {
			AppLogic.runOnUiGround(mUpdatePhoneStateLoop, 1000); // 这里提前放进队列，否则onStateUpdate里本来要移除的，却移除不掉
			mUpdateCallState.run();
		}
	};

	static void startMonitorPhoneState() {
		stopMonitorPhoneState();
		AppLogic.runOnUiGround(mUpdatePhoneStateLoop, 1000);
	}

	static void stopMonitorPhoneState() {
		AppLogic.removeUiGroundCallback(mUpdatePhoneStateLoop);
	}
}

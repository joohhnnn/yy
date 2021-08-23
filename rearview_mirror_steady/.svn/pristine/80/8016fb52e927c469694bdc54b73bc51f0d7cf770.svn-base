package com.txznet.txz.component.call.sys;

import com.txznet.txz.component.call.ICall;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class PhoneStatReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Service.TELEPHONY_SERVICE);
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			CallSysImpl.onSysStateUpdate(ICall.STATE_MAKING, intent);
		} else {
			switch (tm.getCallState()) {
			case TelephonyManager.CALL_STATE_RINGING:
				CallSysImpl.onSysStateUpdate(ICall.STATE_RINGING, intent);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				CallSysImpl.onSysStateUpdate(ICall.STATE_OFFHOOK, intent);
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				CallSysImpl.onSysStateUpdate(ICall.STATE_IDLE, intent);
				break;
			}
		}
	}

}

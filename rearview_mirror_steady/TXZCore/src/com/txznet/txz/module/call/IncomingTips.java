package com.txznet.txz.module.call;

import android.text.TextUtils;

import com.txz.ui.contact.ContactData.MobileContact;
import com.txz.ui.event.UiEvent;
import com.txz.ui.makecall.UiMakecall;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.IAsrCallback;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.bt.BluetoothManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.util.CallUtil;

public class IncomingTips {
	int speakId;
	MobileContact con;

	static IAsrCallback mAsrCallBack = new IAsrCallback() {
		@Override
		public void onAbort(AsrOption option, int error) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_CALL,
					UiMakecall.SUBEVENT_INCOMING_CALL_REPEAT);
		}

		@Override
		public void onError(AsrOption option, int error, String desc,
				String speech, int error2) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_CALL,
					UiMakecall.SUBEVENT_INCOMING_CALL_REPEAT);
		}
	};

	public IncomingTips() {
		speakId = TtsManager.INVALID_TTS_TASK_ID;
	}

	public void speakCon(MobileContact con, boolean record) {
		this.con = con;
		speakId = TtsManager.INVALID_TTS_TASK_ID;
		speakIncomingInfo(record);
	}

	public void stopSpeakCon() {
		TtsManager.getInstance().cancelSpeak(speakId);
		AppLogic.removeUiGroundCallback(speakIncomingRun);
		speakId = TtsManager.INVALID_TTS_TASK_ID;
	}

	ITtsCallback procIncomingByVoice = new ITtsCallback() {
		@Override
		public void onSuccess() {
			AsrManager.getInstance().start(false,
					VoiceData.GRAMMAR_SENCE_INCOMING_MAKE_SURE, mAsrCallBack);
		}

		@Override
		public boolean isNeedStartAsr() {
			return true;
		}
	};

	ITtsCallback repeateIncomingInfo = new ITtsCallback() {
		@Override
		public void onSuccess() {
			AppLogic.runOnUiGround(speakIncomingRun, 2000);
		}
	};

	Runnable speakIncomingRun = new Runnable() {
		@Override
		public void run() {
			speakIncomingInfo(false);
		}
	};

	public void speakIncomingInfo(boolean record) {
		if (!(CallManager.getInstance().isRinging())) {
			return;
		}
		TtsManager.getInstance().cancelSpeak(speakId);
		String speech;
		String suffix = "";
		if (BluetoothManager.getInstance().isBluetoothDeviceConnected()) {
			record = false;
			suffix = "，" + NativeData.getResString("RS_CALL_INCOMING_BTCONN_SUFFIX");
		}
		if (record)
			suffix = "，" + NativeData.getResString("RS_CALL_INCOMING_SUFFIX");

		if (!TextUtils.isEmpty(con.name)) {
			speech = NativeData.getResString("RS_CALL_INCOMING_PREFIX");
		} else {
			speech = NativeData.getResString("RS_CALL_INCOMING_PREFIX");
		}
		speech += suffix;

		if (!TextUtils.isEmpty(con.name)) {
			speech = speech.replace("%SLOT%", con.name);
		} else {
			speech = speech.replace("%SLOT%",
					NativeData.getPhoneArea(con.phones[0]) + CallUtil.converToSpeechDigits(con.phones[0]));
		}
		
		if (record) {
			speakId = TtsManager.getInstance().speakText(
					TtsUtil.DEFAULT_TTS_STREAM, speech, procIncomingByVoice);
		} else {
			speakId = TtsManager.getInstance().speakText(
					TtsUtil.DEFAULT_TTS_STREAM, speech, repeateIncomingInfo);
		}
	}
}

package com.txznet.txz.module.call;

import android.text.TextUtils;

import com.google.protobuf.nano.MessageNano;
import com.txz.ui.contact.ContactData.MobileContact;
import com.txz.ui.event.UiEvent;
import com.txz.ui.makecall.UiMakecall;
import com.txznet.txz.component.call.ICall.ICallStateListener;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;

public class CallStateListener extends ICallStateListener {
	@Override
	public void onBusy() {
		WakeupManager.getInstance().stop();
		// TODO 关闭呼叫中窗口
		CallSelectControl.selectCancel(false);
		MusicManager.getInstance().onBeginCall();
	}

	@Override
	public void onMakecall(String num, String name) {
		// TODO 根据业务需要打开呼叫中窗口
		super.onMakecall(num, name);
	}

	@Override
	public void onIncomingRing(String num, String name) {
		AsrManager.getInstance().cancel();
		RecorderWin.close();
		JNIHelper.logd("RINGING :" + num);
		if (TextUtils.isEmpty(num))
			num = "私密号码";
		MobileContact con = new MobileContact();
		con.name = name;
		con.phones = new String[1];
		con.phones[0] = num;
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_CALL,
				UiMakecall.SUBEVENT_INCOMING_CALL_NOTIFY, MessageNano.toByteArray(con));
		super.onIncomingRing(num, name);
	}

	@Override
	public void onIncomingAnswer(String num, String name) {
		// TODO Auto-generated method stub
		super.onIncomingAnswer(num, name);
	}

	@Override
	public void onIncomingReject(String num, String name) {
		// TODO Auto-generated method stub
		super.onIncomingReject(num, name);
	}

	@Override
	public void onOffhook() {
		// 关闭联系人选择窗口
		CallSelectControl.selectCancel(false);
		// TODO 打开呼叫中窗口
		// CallManager.getInstance().unregCommand("CALL_CANCEL_CALL");
		CallManager.getInstance().stopIncomingInteraction(); 
	}

	@Override
	public void onCallStop() {
		// TODO 关闭来电窗口
		// 关闭联系人选择窗口
		CallSelectControl.selectCancel(false);
		// TODO 关闭呼叫中窗口
		MusicManager.getInstance().onEndCall();
		// 去掉取消呼叫的指令注册
		CallManager.getInstance().unregCommand("CALL_CANCEL_CALL");
		CallManager.getInstance().stopIncomingInteraction();
		//启动唤醒
		WakeupManager.getInstance().start();
	}

}

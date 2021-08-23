package com.txznet.txz.module.call;

import android.text.TextUtils;

import com.google.protobuf.nano.MessageNano;
import com.txz.ui.contact.ContactData.MobileContact;
import com.txz.ui.event.UiEvent;
import com.txz.ui.makecall.UiMakecall;
import com.txznet.txz.component.call.ICall.ICallStateListener;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.feedback.FeedbackManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.news.NewsManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.widget.QiWuTicketReminderView;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.recordcenter.TXZSourceRecorderManager;

public class CallStateListener extends ICallStateListener {
	@Override
	public void onBusy() {
		WakeupManager.getInstance().stop();
		// TODO 关闭呼叫中窗口
		ChoiceManager.getInstance().selectCallCancel();
		MusicManager.getInstance().onBeginCall();
	}

	@Override
	public void onMakecall(String num, String name) {
		// TODO 根据业务需要打开呼叫中窗口
		FeedbackManager.getInstance().cancel();
		super.onMakecall(num, name);
	}

	@Override
	public void onIncomingRing(String num, String name) {
		AsrManager.getInstance().cancel();
		QiWuTicketReminderView.closePushView();
		RecorderWin.close();
		NewsManager.getInstance().stop();//来电新闻需要停止,长安欧尚需求
		// 停止当前RecordManager录音, 防止微信正在录音时来电，来电播报被录到微信录音中并发送
		//RecordManager.getInstance().stop();
		RecordManager.getInstance().cancel();
		FeedbackManager.getInstance().cancel();
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
		ChoiceManager.getInstance().selectCallCancel();
		// TODO 打开呼叫中窗口
		// CallManager.getInstance().unregCommand("CALL_CANCEL_CALL");
		CallManager.getInstance().stopIncomingInteraction();
		TXZSourceRecorderManager.stop();
		FeedbackManager.getInstance().cancel();
	}

	@Override
	public void onCallStop() {
		// TODO 关闭来电窗口
		// 关闭联系人选择窗口
		ChoiceManager.getInstance().selectCallCancel();
		// TODO 关闭呼叫中窗口
		MusicManager.getInstance().onEndCall();
		// 去掉取消呼叫的指令注册
		CallManager.getInstance().unregCommand("CALL_CANCEL_CALL");
		CallManager.getInstance().stopIncomingInteraction();
		TXZSourceRecorderManager.start();
		//启动唤醒
		WakeupManager.getInstance().start();
	}

}

package com.txznet.txz.module.record;

import com.txz.ui.event.UiEvent;
import com.txz.ui.wechat.UiWechat;
import com.txz.ui.wechat.UiWechat.WechatVoiceTask;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;

public class OnlineParseRecorder extends Recorder{
	final long mTaskId;
	
	public OnlineParseRecorder(long taskId) {
		mTaskId = taskId;
	}
	
	@Override
	public void write(byte[] data, int len) {
		final byte[] tmp = new byte[len];
		System.arraycopy(data, 0, tmp, 0, len);
		Runnable run = new Runnable() {
			@Override
			public void run() {
				WechatVoiceTask task = new WechatVoiceTask();
				task.uint64Timestamp = mTaskId;
				task.bytesPcm = tmp;
				JNIHelper.sendEvent(UiEvent.EVENT_ACTION_WECHAT, UiWechat.SUBEVENT_UPLOAD_VOICE, task);
			}
		};
		AppLogic.runOnUiGround(run, 0);
		super.write(data, len);
	}
}

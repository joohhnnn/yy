package com.txznet.txz.component.text.ifly;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.txz.component.text.IText;
import com.txznet.txz.component.text.IText.ITextCallBack;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.plugin.interfaces.NlpTransitionToTxz;

public class TextIflyImpl implements IText {

	@Override
	public int initialize(IInitCallback oRun) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setVoiceData(VoiceParseData parseData, ITextCallBack callBack) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setText(String text, ITextCallBack callBack) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}
	public static NlpTransitionToTxz mTransitionImpl = null;
	public static VoiceParseData iflyDataToTxzScene(VoiceParseData parseData) {
		// TODO 待添加具体转换函数
		// 创建新的VoiceParseData并把txzJson放到strVoiceData中。注意json中一定要有text字段
		if (mTransitionImpl != null) {
			VoiceParseData impl = mTransitionImpl.TransitionToTxz(parseData);
			if (impl != null)
				return impl;
		}
		VoiceParseData newData = new VoiceParseData();
		try {
			newData = VoiceParseData.parseFrom(VoiceParseData
					.toByteArray(parseData));
		} catch (InvalidProtocolBufferNanoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		IflyJsonConver conver = new IflyJsonConver(parseData);
		newData.strText = conver.getRawText();
		newData.strVoiceData = conver.getJson();
		newData.floatTextScore = conver.getScore();

		JNIHelper.logd("score:" + conver.getScore() + " voiceData:"
				+ conver.getJson());
		return newData;
	}

	@Override
	public void setPriority(int priority) {
	}

	@Override
	public int getPriority() {
		return 0;
	}

}

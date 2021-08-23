package com.txznet.fm.receiver;

import java.util.Observable;

import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.fm.receiver.abstraction.AReceiver;
import com.txznet.music.Constant;

/**
 * 直播实际请求类
 * @author ASUS User
 *
 */
public class DirectReceiver extends AReceiver {

	@Override
	public void next() {
		TtsUtil.speakResource("RS_VOICE_SPEAK_SUPPORT_NOT_LIVE",Constant.RS_VOICE_SPEAK_SUPPORT_NOT_LIVE);
		
	}

	@Override
	public void last() {
		TtsUtil.speakResource("RS_VOICE_SPEAK_SUPPORT_NOT_LIVE",Constant.RS_VOICE_SPEAK_SUPPORT_NOT_LIVE);
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub

	}

}

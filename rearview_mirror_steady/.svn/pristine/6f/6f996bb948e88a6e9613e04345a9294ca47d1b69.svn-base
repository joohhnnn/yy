package com.txznet.txz.component.wakeup.mix;

import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.txz.component.wakeup.IWakeup;
import com.txznet.txz.component.wakeup.mix.WakeupProxy;
import com.txznet.txz.jni.JNIHelper;

public class WakeupImpl implements IWakeup {
	private IWakeup mWakeup = null;
	
	@Override
	public int initialize(String[] cmds, IInitCallback oRun) {
		mWakeup = WakeupProxy.getProxy();
		mWakeup.initialize(cmds, oRun);
		return 0;
	}

	@Override
	public int start(WakeupOption oOption) {
		if (mWakeup == null){
			JNIHelper.logd("mWakeup = " + mWakeup);
			return 0;
		}
		mWakeup.start(oOption);
		return 0;
	}

	@Override
	public int startWithRecord(IWakeupCallback oCallback, RecordOption options,
			String[] overTag) {
		return 0;
	}

	@Override
	public void stop() {
		if (mWakeup == null){
			JNIHelper.logd("mWakeup = " + mWakeup);
			return;
		}
		mWakeup.stop();
	}

	@Override
	public void stopWithRecord() {
		
	}

	@Override
	public void setWakeupKeywords(String[] keywords) {
		if (mWakeup == null){
			JNIHelper.logd("mWakeup = " + mWakeup);
			return;
		}
		mWakeup.setWakeupKeywords(keywords);
	}

	@Override
	public void setWakeupThreshold(float val) {
		if (mWakeup == null){
			JNIHelper.logd("mWakeup = " + mWakeup);
			return;
		}
		mWakeup.setWakeupThreshold(val);
	}

	@Override
	public void enableVoiceChannel(boolean enable) {
		if (mWakeup == null){
			JNIHelper.logd("mWakeup = " + mWakeup);
			return;
		}
		JNIHelper.logd("enable voice channel = " + enable);
		mWakeup.enableVoiceChannel(enable);
	}

}

package com.txznet.txz.component.wakeup.mix;

import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.txz.component.asr.mix.VoiceRecogitionController;
import com.txznet.txz.component.wakeup.IWakeup;

public class WakeupImpl implements IWakeup {

	@Override
	public int initialize(String[] cmds, IInitCallback oRun) {
		return VoiceRecogitionController.getInstance().initialize(oRun);
	}

	@Override
	public int start(WakeupOption oOption) {
		VoiceRecogitionController.getInstance().start(oOption);
		return 0;
	}

	@Override
	public int startWithRecord(IWakeupCallback oCallback, RecordOption options,
			String[] overTag) {
		return 0;
	}

	@Override
	public void stop() {
		VoiceRecogitionController.getInstance().stop(new WakeupOption());
	}

	@Override
	public void stopWithRecord() {
	
	}

	@Override
	public void setWakeupKeywords(String[] keywords) {
		VoiceRecogitionController.getInstance().setWakeupKeywords(keywords);
	}

	@Override
	public void setWakeupThreshold(float val) {
		
	}

	@Override
	public void enableVoiceChannel(boolean enable) {	
		VoiceRecogitionController.getInstance().enableVoiceChannel(enable);
	}
	
}

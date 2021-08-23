package com.txznet.debugtool.base;

import com.txznet.debugtool.util.DrawWaveAudioRecord;
import com.txznet.debugtool.view.WaveSurfaceView;

public class SystemAudioDrawWave extends BaseDrawWave {

	@Override
	public void start(WaveSurfaceView vLeft, WaveSurfaceView vRight) {
		waveLeftRunnable = new DrawWaveAudioRecord(vLeft, vRight);
		Thread audioRecordThread = new Thread(waveLeftRunnable);
		audioRecordThread.setName("AudioRecord-Thread");
		audioRecordThread.start();
	}

	@Override
	public String getName() {
		return "SystemAudioDrawWave";
	}

}

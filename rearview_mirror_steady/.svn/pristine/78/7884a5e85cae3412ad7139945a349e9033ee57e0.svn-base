package com.txznet.debugtool.base;

import com.txznet.debugtool.util.DrawWaveRunnable;
import com.txznet.debugtool.view.WaveSurfaceView;

public abstract class BaseDrawWave {

	DrawWaveRunnable waveLeftRunnable;
	DrawWaveRunnable waveRightRunnable;

	public abstract void start(WaveSurfaceView vLeft, WaveSurfaceView vRight);

	public void stop() {
		// 暂停录音
		if (waveLeftRunnable != null || waveRightRunnable != null) {
			if (waveLeftRunnable != null && waveLeftRunnable.ifRecording()) {
				waveLeftRunnable.stop();
				waveLeftRunnable = null;
			}
			if (waveRightRunnable != null && waveRightRunnable.ifRecording()) {
				waveRightRunnable.stop();
				waveRightRunnable = null;
			}
		}
	}

	public abstract String getName();

}

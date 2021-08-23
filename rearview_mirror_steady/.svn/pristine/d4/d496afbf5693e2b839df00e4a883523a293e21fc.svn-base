package com.txznet.debugtool.base;

import com.txznet.debugtool.WaveformActivity;
import com.txznet.debugtool.util.DrawWaveTXZRecord;
import com.txznet.debugtool.view.WaveSurfaceView;
import com.txznet.txz.util.recordcenter.ITXZSourceRecorder;

public class TXZAudioDrawWave extends BaseDrawWave {

	@Override
	public void start(WaveSurfaceView vLeft, WaveSurfaceView vRight) {
		if (WaveformActivity.ifLeftShow) {
			waveLeftRunnable = new DrawWaveTXZRecord(
					ITXZSourceRecorder.READER_TYPE_MIC, vLeft);
			Thread leftRecordThread = new Thread(waveLeftRunnable);
			leftRecordThread.setName("LeftRecord-Thread");
			leftRecordThread.start();
		}

		if (WaveformActivity.ifRightShow) {
			waveRightRunnable = new DrawWaveTXZRecord(
					ITXZSourceRecorder.READER_TYPE_REFER, vRight);
			Thread rightRecordThread = new Thread(waveRightRunnable);
			rightRecordThread.setName("RightRecord-Thread");
			rightRecordThread.start();
		}
	}

	@Override
	public String getName() {
		return "TXZAudioDrawWave";
	}

}

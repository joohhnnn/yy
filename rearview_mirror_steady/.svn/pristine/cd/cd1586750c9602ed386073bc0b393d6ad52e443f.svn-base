package com.txznet.debugtool.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.txznet.debugtool.WaveformActivity;
import com.txznet.debugtool.view.WaveSurfaceView;
import com.txznet.widget.DebugUtil;

public class DrawWaveAudioRecord extends DrawWaveRunnable {

	private static final String TAG = "DrawWaveTXZRecord";

	AudioRecord recorder = null;

	public WaveSurfaceView mWSfvLeft;
	public WaveSurfaceView mWSfvRight;

	private DrawWaveRunn drawLeftRun;
	private DrawWaveRunn drawRightRun;

	public DrawWaveAudioRecord(WaveSurfaceView wsfvL, WaveSurfaceView wsfvR) {
		super();
		this.mWSfvLeft = wsfvL;
		this.mWSfvRight = wsfvR;
	}

	@Override
	public void run() {

		recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, 16000,
				AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT,
				AudioRecord.getMinBufferSize(16000,
						AudioFormat.CHANNEL_IN_STEREO,
						AudioFormat.ENCODING_PCM_16BIT));
		recorder.startRecording();

		if (WaveformActivity.ifLeftShow) {
			drawLeftRun = new DrawWaveRunn(mWSfvLeft, 4);
			Thread drawLeftThread = new Thread(drawLeftRun);
			drawLeftThread.setName("LeftDraw-Thread");
			drawLeftThread.start();
		}

		if (WaveformActivity.ifRightShow) {
			drawRightRun = new DrawWaveRunn(mWSfvRight, 2);
			Thread drawRightThread = new Thread(drawRightRun);
			drawRightThread.setName("RightDraw-Thread");
			drawRightThread.start();
		}

		int n = 0;
		while (recorder != null) {
			if ((n = recorder.read(mRecordBuff, mRecordDataCount, 8000)) > 0) {
				mRecordDataCount += n;
			} else {
				if (n < 0) {
					try {
						DebugUtil.showTips("启动录音失败，稍后再试!");
					} catch (Exception e) {
					}
				}
				break;
			}

		}
	}

	@Override
	public boolean ifRecording() {
		return recorder != null;
	}

	@Override
	public void stop() {
		if (recorder != null) {
			AudioRecord tempRecorder = recorder;
			recorder = null;
			tempRecorder.release();
			tempRecorder = null;
			super.savePCM("sys_" + "stereo" + ".pcm");

		}

		if(drawLeftRun != null)
			drawLeftRun.ifDraw = false;
		if(drawRightRun != null)
			drawRightRun.ifDraw = false;
	}

}

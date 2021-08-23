package com.txznet.txz.module.ui.view;

import android.widget.Toast;

import com.txznet.txz.module.ui.TestActivity;
import com.txznet.txz.util.recordcenter.TXZAudioRecorder;

public class DrawWaveTXZRecord extends DrawWaveRunnable {

	private static final String TAG = "DrawWaveTXZRecord";

	int mRecordType;
	TXZAudioRecorder recorder = null;

	public WaveSurfaceView mWSfv;
	private DrawWaveRunn drawRun;

	public DrawWaveTXZRecord(int recordType, WaveSurfaceView wsfv) {
		super();
		this.mRecordType = recordType;
		this.mWSfv = wsfv;
	}

	@Override
	public void run() {

		recorder = new TXZAudioRecorder();
		recorder.setType(mRecordType);
		recorder.startRecording();

		drawRun = new DrawWaveRunn(mWSfv, 2);
		Thread drawThread = new Thread(drawRun);
		drawThread.setName("Draw"  +mRecordType + "-Thread");
		drawThread.start();

		int n = 0;
		while (recorder != null) {
			// 获取录音数据
			if ((n = recorder.read(mRecordBuff, mRecordDataCount, 8000)) > 0) {
				mRecordDataCount += n;
			} else {
				if (n < 0) {
					try {
						Toast.makeText(TestActivity.sInstance, "启动录音失败", Toast.LENGTH_LONG).show();
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
			TXZAudioRecorder tempRecorder = recorder;
			recorder = null;
			tempRecorder.release();
			tempRecorder = null;
			super.savePCM("txz_" + (mRecordType == 1 ? "left" : "right")
					+ ".pcm");
		}
		drawRun.ifDraw = false;
	}

}

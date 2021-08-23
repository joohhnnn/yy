package com.txznet.txz.module.ui;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.module.ui.view.DrawWaveRunnable;
import com.txznet.txz.module.ui.view.DrawWaveTXZRecord;
import com.txznet.txz.module.ui.view.WaveSurfaceView;
import com.txznet.txz.service.TXZService;
import com.txznet.txz.ui.widget.SDKFloatView;

public class TestActivity extends Activity {

	public static TestActivity sInstance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sInstance = this;
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.activity_wave);
		initView();

	}

	WaveSurfaceView waveSfvLeft;
	WaveSurfaceView waveSfvRight;

	private void initView() {

		waveSfvLeft = (WaveSurfaceView) findViewById(R.id.wavesfvLeft);
		waveSfvRight = (WaveSurfaceView) findViewById(R.id.wavesfvRight);

		if (waveSfvLeft != null) {
			waveSfvLeft.setLine_off(40);
			// 解决surfaceView黑色闪动效果
			waveSfvLeft.setZOrderOnTop(true);
			waveSfvLeft.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		}

		if (waveSfvRight != null) {
			waveSfvRight.setLine_off(40);
			// 解决surfaceView黑色闪动效果
			waveSfvRight.setZOrderOnTop(true);
			waveSfvRight.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		}
	}

	DrawWaveRunnable waveLeftRunnable;
	DrawWaveRunnable waveRightRunnable;

	public void beginRec(View v) {
		if (TXZService.checkSdkInitResult()) {
			waveLeftRunnable = new DrawWaveTXZRecord(1, waveSfvLeft);
			Thread leftRecordThread = new Thread(waveLeftRunnable);
			leftRecordThread.setName("LeftRecord-Thread");
			leftRecordThread.start();

			waveRightRunnable = new DrawWaveTXZRecord(3, waveSfvRight);
			Thread rightRecordThread = new Thread(waveRightRunnable);
			rightRecordThread.setName("RightRecord-Thread");
			rightRecordThread.start();
			
			Toast.makeText(this, "开始录音", Toast.LENGTH_SHORT).show();

		} else {
			Toast.makeText(this, "语音未初始化成功", Toast.LENGTH_SHORT).show();

		}


	}

	public void endRec(View v) {

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
			Toast.makeText(this, "暂停录音", Toast.LENGTH_SHORT).show();
		}
	}

	public void floatTop(View v) {
		TXZService.setFloatToolType("FLOAT_TOP");
	}

	public void floatNone(View v) {
		TXZService.setFloatToolType("FLOAT_NONE");
	}

	public void testNullEx(View v) {
		String strText = null;
		try {
			int lenght = strText.length();
			AppLogic.showToast("字符串长度 : " + lenght);
		} catch (Exception e) {
			AppLogic.showToast("成功捕获到空指针异常");

		}
	}

	public void throwNullEx(View v) {
		String strText = null;
		int len = strText.length();
		AppLogic.showToast("字符串长度 : " + len);
	}

}

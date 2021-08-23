package com.txznet.debugtool;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.txznet.debugtool.base.BaseDrawWave;
import com.txznet.debugtool.base.SystemAudioDrawWave;
import com.txznet.debugtool.base.TXZAudioDrawWave;
import com.txznet.debugtool.util.SPThreshholdUtil;
import com.txznet.debugtool.view.WaveSurfaceView;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZConfigManager;

public class WaveformActivity extends Activity {
	private static final String TAG = "WaveformActivity";

	public static boolean ifLeftShow = true;
	public static boolean ifRightShow = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.activity_wave);
		initView();
	}

	WaveSurfaceView waveSfvLeft;
	WaveSurfaceView waveSfvRight;

	private void initView() {
		WaveformActivity.ifRightShow = SPThreshholdUtil.getSPData(
				AppLogic.getApp(), SPThreshholdUtil.APPLICATION_SP_NAME,
				SPThreshholdUtil.IFRIGHTSHOW);

		WaveformActivity.ifLeftShow = SPThreshholdUtil.getSPData(
				AppLogic.getApp(), SPThreshholdUtil.APPLICATION_SP_NAME,
				SPThreshholdUtil.IFLESHOW);

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


	
	
	BaseDrawWave baseDW;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			break;

		case MotionEvent.ACTION_UP:
			
			
			if(baseDW !=null) {
				baseDW.stop();
				baseDW = null;
				Toast.makeText(this, "停止录音", Toast.LENGTH_SHORT).show();

			} else {
				// 使用TXZ录音
				if (TXZConfigManager.getInstance().isInitedSuccess()) {
					baseDW = new TXZAudioDrawWave();
				} else {
					baseDW = new SystemAudioDrawWave();
				}
				baseDW.start(waveSfvLeft , waveSfvRight);
				Toast.makeText(this, "使用 " + baseDW.getName() + " 开始录音", Toast.LENGTH_SHORT).show();
			}
			
			break;

		default:
			break;
		}
		// 拦截点击， 分别进行开始录音和暂停录音
		return super.onTouchEvent(event);
	}

}

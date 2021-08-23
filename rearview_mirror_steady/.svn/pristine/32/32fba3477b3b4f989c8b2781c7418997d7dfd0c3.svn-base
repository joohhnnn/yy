package com.txznet.debugtool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.widget.DebugButton;
import com.txznet.widget.DebugUtil;

public class LongRecordActivity extends BaseDebugActivity {

	private final static String TAG = "LongRecordActivity";

	private static boolean ifStereo = true;
	private static boolean ifContinue = true;

	static DebugButton toastButton;

	protected static Handler uiHandler = new Handler(Looper.getMainLooper());

	public static void runOnUiGround(Runnable r, long delay) {
		if (delay > 0) {
			uiHandler.postDelayed(r, delay);
		} else {
			uiHandler.post(r);
		}
	}

	public static void removeUiGroundCallback(Runnable r) {
		uiHandler.removeCallbacks(r);
	}

	@Override
	protected void onInitButtons() {

		toastButton = new DebugButton(this, "", new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		addDemoButtons(new DebugButton(this, "转换单双声道",
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (ifStereo) {
							ifStereo = false;
							Toast.makeText(LongRecordActivity.this, "使用单声道",
									Toast.LENGTH_LONG).show();
							toastButton.setText("使用单声道");

						} else {
							ifStereo = true;
							Toast.makeText(LongRecordActivity.this, "使用立体声",
									Toast.LENGTH_LONG).show();
							toastButton.setText("使用立体声");

						}

					}
				}), new DebugButton(this, "开始长录音", new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				new Thread(recordRunnable).start();

			}
		}), new DebugButton(this, "停止长录音", new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ifContinue = false;

			}
		}));

		addDemoButtons(toastButton);
	}

	AudioRecord mAudioRecord;
	long mRecordTime = 0;

	int mBufferCount = 0;

	// 2 * 2 * 16000 * 60 * 30 = 半小时 ， 109兆
	byte[] mBuffer = new byte[2 * 2 * 16000 * 60 * 15];
	// byte[] mBuffer = new byte[2 * 2 * 16000 * 30]; //

	String fileSuffixName;

	private static final int audioSource = AudioSource.DEFAULT;
	private static final int sampleRateInHz = 16000;
	private static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
	private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	private static long count = 0;

	
	
	Runnable recordRunnable = new Runnable() {

		@Override
		public void run() {

			if (ifStereo) {
				channelConfig = AudioFormat.CHANNEL_IN_STEREO;
				fileSuffixName = "stereo.pcm";
			} else {
				channelConfig = AudioFormat.CHANNEL_IN_MONO;
				fileSuffixName = "mono.pcm";
			}


			mRecordTime = System.currentTimeMillis();
			mAudioRecord = new AudioRecord(audioSource, sampleRateInHz,
					channelConfig, audioFormat, AudioRecord.getMinBufferSize(
							sampleRateInHz, channelConfig, audioFormat));

			runOnUiGround(new Runnable() {
				public void run() {
					toastButton.setText("after new audiorecord");
				}
			}, 0);

			try {
				mAudioRecord.startRecording();
				ifContinue = true;
				mBufferCount = 0;
				DebugUtil.showTips("已启动录音 " + (ifStereo ? "立体声" : "单声道"));
				runOnUiGround(new Runnable() {
					public void run() {
						toastButton.setText("已启动录音 " + (ifStereo ? "立体声" : "单声道"));
					}
				}, 0);

			} catch (Exception e) {
				e.printStackTrace();
				DebugUtil.showTips("启动录音失败，稍后再试!");
				runOnUiGround(new Runnable() {
					public void run() {
						toastButton.setText("启动录音失败，稍后再试!");
					}
				}, 0);
				return;
			}

			count = 0;
			int n = 0;
			while (true) {
				if (!ifContinue) {
					DebugUtil.showTips("停止录音...");
					runOnUiGround(new Runnable() {
						public void run() {
							toastButton.setText("停止录音...");
						}
					}, 0);
					break;
				}
				if (mAudioRecord != null) {
					if ((n = mAudioRecord.read(mBuffer, mBufferCount, 12000)) > 0) {
						// Log.d(TAG, "record data: " + n + "/" + mBufferCount);
						mBufferCount += n;

						if ((mBufferCount + n) > mBuffer.length) { // 超出buffer长度了，做一次本地保存
							break;
						}

						++count;
						if ((count % 50) == 0) {
							if(count > 10000) {
								count = 0;
							}
							DebugUtil.showTips("录音中...");
							runOnUiGround(new Runnable() {
								public void run() {
									toastButton.setText("录音中..." + count);
								}
							}, 0);
						}
					} else {
						if (n < 0) {
							try {
								DebugUtil.showTips("录音启动失败" + n + "，状态"
										+ mAudioRecord.getState());
							} catch (Exception e) {
							}
						}
						break;
					}
				}

			}

			savePCM(mBuffer, mBufferCount, fileSuffixName);
			DebugUtil.showTips(mRecordTime + "录音文件保存成功!");
			runOnUiGround(new Runnable() {
				public void run() {
					toastButton.setText(mRecordTime + "录音文件保存成功!");
				}
			}, 0);

			if (ifContinue) {
				new Thread(this).start();
			}

		}
	};

	String currentName = "";

	private void savePCM(byte[] data, int count, String name) {
		if (count <= 0)
			return;
		try {
			File d = new File(Environment.getExternalStorageDirectory()
					.getPath(), "txz/recordTest");
			d.mkdirs();
			String fileName = "" + mRecordTime + "_" + name;
			File fout = new File(d, fileName);
			currentName = fout.getPath();
			LogUtil.logd("currentName: " + currentName);
			OutputStream out = new FileOutputStream(fout);
			out.write(data, 0, count);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

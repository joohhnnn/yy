package com.txznet.sdkdemo.ui;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.txznet.sdk.TXZTtsManager;
import com.txznet.sdk.TXZTtsManager.ITtsCallback;
import com.txznet.sdk.TXZTtsManager.TtsCallback;
import com.txznet.sdk.TXZTtsManager.TtsOption;
import com.txznet.sdk.TXZTtsManager.TtsTool;
import com.txznet.sdkdemo.SDKDemoApp;
import com.txznet.sdkdemo.bean.DebugUtil;
import com.txznet.sdkdemo.bean.DemoButton;

public class TtsActivity extends BaseActivity {

	int mTtsTaskId = TXZTtsManager.INVALID_TTS_TASK_ID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addDemoButtons(new DemoButton(this, "一般播报", new OnClickListener() {
			@Override
			public void onClick(View v) {
				mTtsTaskId = TXZTtsManager.getInstance().speakText(
						"现在正在进行一般播报测试");
			}
		}), new DemoButton(this, "高级播报", new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 高级播报，可以设置播报使用的流，播报的处理回调
				mTtsTaskId = TXZTtsManager.getInstance().speakText(
						AudioManager.STREAM_MUSIC, "现在正在进行高级播报测试",
						TXZTtsManager.PreemptType.PREEMPT_TYPE_NEXT,
						new ITtsCallback() {
							@Override
							public void onSuccess() {
								DebugUtil.showTips("播报完成");
							}

							@Override
							public void onCancel() {
								DebugUtil.showTips("播报取消");
							}
						});
			}
		}), new DemoButton(this, "取消播报", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZTtsManager.getInstance().cancelSpeak(mTtsTaskId);
			}
		}));

		addDemoButtons(new DemoButton(this, "低速播报", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZTtsManager.getInstance().setVoiceSpeed(10);

				mTtsTaskId = TXZTtsManager.getInstance().speakText(
						"现在正在进行低速播报测试");
			}
		}), new DemoButton(this, "正常播报", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZTtsManager.getInstance().setVoiceSpeed(70);

				mTtsTaskId = TXZTtsManager.getInstance().speakText(
						"现在正在进行正常速度播报测试");
			}
		}), new DemoButton(this, "高速播报", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZTtsManager.getInstance().setVoiceSpeed(100);

				mTtsTaskId = TXZTtsManager.getInstance().speakText(
						"现在正在进行高速播报测试");
			}
		}));

		addDemoButtons(new DemoButton(this, "默认音乐流", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZTtsManager.getInstance().setDefaultAudioStream(
						AudioManager.STREAM_MUSIC);

				mTtsTaskId = TXZTtsManager.getInstance().speakText(
						"已切换为音乐流进行语音播报");
			}
		}), new DemoButton(this, "默认闹钟流", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZTtsManager.getInstance().setDefaultAudioStream(
						AudioManager.STREAM_ALARM);

				mTtsTaskId = TXZTtsManager.getInstance().speakText(
						"已切换为闹钟流进行语音播报");
			}
		}));

		addDemoButtons(new DemoButton(this, "自定义播报工具", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZTtsManager.getInstance().setTtsTool(new TtsTool() {
					@Override
					public void start(int stream, String text,
							TtsCallback callback) {
						mTtsCallback = callback;
						DebugUtil.showTips("开始播报: " + text, false);
						SDKDemoApp.runOnUiGround(mRunnableTtsSuccess, 2000);
					}

					@Override
					public void setOption(TtsOption option) {

					}

					@Override
					public void cancel() {
						SDKDemoApp.removeUiGroundCallback(mRunnableTtsSuccess);
						DebugUtil.showTips("取消播报", false);
						if (mTtsCallback != null) {
							mTtsCallback.onCancel();
							mTtsCallback = null;
						}
					}
				});
			}
		}));
	}

	TtsCallback mTtsCallback = null;
	Runnable mRunnableTtsSuccess = new Runnable() {
		@Override
		public void run() {
			DebugUtil.showTips("成功播报", false);
			if (mTtsCallback != null) {
				mTtsCallback.onSuccess();
				mTtsCallback = null;
			}
		}
	};

}

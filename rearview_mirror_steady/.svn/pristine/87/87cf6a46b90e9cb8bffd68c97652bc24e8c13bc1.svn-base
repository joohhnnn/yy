package com.txznet.txz.module.tts;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.text.TextUtils;

import com.spreada.utils.chinese.ZHConverter;
import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZMediaFocusManager;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.component.tts.remote.TtsRemoteImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.bt.BluetoothManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.volume.VolumeManager;
import com.txznet.txz.service.TXZService;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.BeepPlayer;
import com.txznet.txz.util.TXZStatisticser;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

/**
 * TTS语音合成管理模块，负责语音队列管理，任务调度，接口适配，语音播报
 * 
 * @author bihongpi
 *
 */
public class TtsManager extends IModule {
	public static final int DEFAULT_STREAM_TYPE = -1;
	public static final String BEEP_VOICE_URL = "$BEEP";
	static final ITtsCallback DEFAULT_TTS_CALLBACK = null;
	static final PreemptType DEFAULT_PREEMPT_FLAG = PreemptType.PREEMPT_TYPE_NONE;
	static final long DEFAULT_TIMEOUT = 10000;// 丢弃TTS的超时时间
	private boolean isRealCancle = true;

	// ///////////////////////////////////////////////////////////////////

	static TtsManager sModuleInstance = new TtsManager();

	private TtsManager() {
		mInited = false;
		mInitSuccessed = false;
	}

	public static TtsManager getInstance() {
		return sModuleInstance;
	}

	// ///////////////////////////////////////////////////////////////////

	public void initializeComponent() {
		if (mTts != null)
			return;

		if (TextUtils.isEmpty(ImplCfg.getTtsImplClass()))
			return;

		try {
			mTts = (ITts) Class.forName(ImplCfg.getTtsImplClass())
					.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}

		mTts.initialize(new ITts.IInitCallback() {
			@Override
			public void onInit(boolean bSuccess) {
				JNIHelper.logd("init tts: " + bSuccess);
				mInited = true;
				mInitSuccessed = bSuccess;
				speakNext();

				TXZService.checkSdkInitResult();
			}
		});

		// 注册receiver， 监听第三方媒体焦点占用情况
		IntentFilter filter = new IntentFilter();
		filter.addAction(TXZMediaFocusManager.INTENT_FOCUS_GAINED);
		filter.addAction(TXZMediaFocusManager.INTENT_FOCUS_RELEASED);
		GlobalContext.get().registerReceiver(mWxResReceiver, filter);

	}

	@Override
	public int initialize_AfterLoadLibrary() {
		return ERROR_SUCCESS;
	}

	// ///////////////////////////////////////////////////////////////////

	/**
	 * 监听第三方音频焦点的Receiver
	 */
	BroadcastReceiver mWxResReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (TXZMediaFocusManager.INTENT_FOCUS_GAINED.equals(intent
					.getAction())) {
				setVolumeRate(0.4f);
			} else if (TXZMediaFocusManager.INTENT_FOCUS_RELEASED.equals(intent
					.getAction())) {
				setVolumeRate(1.0f);
			}
		}
	};

	private float mTtsVolumeRate = 1.0f;

	public float getVolumeRate() {
		return mTtsVolumeRate;
	}

	public void setVolumeRate(float rate) {
		mTtsVolumeRate = rate;
		mVoicePlayer.setVolume(mTtsVolumeRate, mTtsVolumeRate);
	}

	public int speakText(int iStream, String sText, PreemptType bPreempt) {
		return speakText(iStream, sText, bPreempt, DEFAULT_TTS_CALLBACK);
	}

	public int speakText(int iStream, String sText, ITtsCallback oRun) {
		return speakText(iStream, sText, DEFAULT_PREEMPT_FLAG, oRun);
	}

	public int speakText(int iStream, String sText) {
		return speakText(iStream, sText, DEFAULT_PREEMPT_FLAG,
				DEFAULT_TTS_CALLBACK);
	}

	public int speakText(String sText, PreemptType bPreempt, ITtsCallback oRun) {
		return speakText(DEFAULT_STREAM_TYPE, sText, bPreempt, oRun);
	}

	public int speakText(String sText, PreemptType bPreempt) {
		return speakText(DEFAULT_STREAM_TYPE, sText, bPreempt,
				DEFAULT_TTS_CALLBACK);
	}

	public int speakText(String sText, ITtsCallback oRun) {
		return speakText(DEFAULT_STREAM_TYPE, sText, DEFAULT_PREEMPT_FLAG, oRun);
	}
	
	public int speakText(String sText) {
		return speakText(DEFAULT_STREAM_TYPE, sText, DEFAULT_PREEMPT_FLAG,
				DEFAULT_TTS_CALLBACK);
	}
	
	/*******************************************
	 * 播放tts时即使开启了回音消除，也不允许唤醒
	 * 
	 *******************************************/
	
	public int speakTextNoWakeup(int iStream, String sText, PreemptType bPreempt) {
		return speakTextNoWakeup(iStream, sText, bPreempt, DEFAULT_TTS_CALLBACK);
	}

	public int speakTextNoWakeup(int iStream, String sText, ITtsCallback oRun) {
		return speakTextNoWakeup(iStream, sText, DEFAULT_PREEMPT_FLAG, oRun);
	}

	public int speakTextNoWakeup(int iStream, String sText) {
		return speakTextNoWakeup(iStream, sText, DEFAULT_PREEMPT_FLAG,
				DEFAULT_TTS_CALLBACK);
	}

	public int speakTextNoWakeup(String sText, PreemptType bPreempt, ITtsCallback oRun) {
		return speakTextNoWakeup(DEFAULT_STREAM_TYPE, sText, bPreempt, oRun);
	}

	public int speakTextNoWakeup(String sText, PreemptType bPreempt) {
		return speakTextNoWakeup(DEFAULT_STREAM_TYPE, sText, bPreempt,
				DEFAULT_TTS_CALLBACK);
	}

	public int speakTextNoWakeup(String sText, ITtsCallback oRun) {
		return speakTextNoWakeup(DEFAULT_STREAM_TYPE, sText, DEFAULT_PREEMPT_FLAG, oRun);
	}
	
	public int speakTextNoWakeup(String sText) {
		return speakTextNoWakeup(DEFAULT_STREAM_TYPE, sText, DEFAULT_PREEMPT_FLAG,
				DEFAULT_TTS_CALLBACK);
	}
	

	public int speakVoice(String sText, String voiceUrl, PreemptType bPreempt,
			ITtsCallback oRun) {
		return speakVoice(DEFAULT_STREAM_TYPE, sText,
				new String[] { voiceUrl }, bPreempt, oRun);
	}

	public int speakVoice(int iStream, String sText, String[] voiceUrls,
			PreemptType bPreempt, ITtsCallback oRun) {
		return speakVoice(iStream, sText, voiceUrls, bPreempt, false, oRun);
	}

	public int speakVoice(int iStream, String sText, String[] voiceUrls,
			PreemptType bPreempt, Boolean fromRemote, ITtsCallback oRun) {
		JNIHelper.logd("speakVoice: stream=" + iStream + " ,text=" + sText
				+ ",url=" + voiceUrls == null ? null : Arrays
				.toString(voiceUrls) + ",bPreempt=" + bPreempt);
		// 创建新的任务
		TtsTask t = new TtsTask();
		t.iStream = iStream;
		t.sText = sText;
		t.lstVoice = voiceUrls;
		t.oRun = oRun;
		t.fromRemote = fromRemote;
		synchronized (mSpeakEndCallback) {
			t.iTaskId = mNextTaskId++;
			if (mNextTaskId <= 0)
				mNextTaskId = 1;
		}
		if (oRun != null) {
			oRun.setTaskId(t.iTaskId);
		}
		insertSpeakTask(bPreempt, t);
		return t.iTaskId;
	}

	public int speakVoice(String sText, String voiceUrl, PreemptType bPreempt) {
		return speakVoice(sText, voiceUrl, bPreempt, null);
	}

	public int speakVoice(String sText, String voiceUrl) {
		return speakVoice(sText, voiceUrl, DEFAULT_PREEMPT_FLAG, null);
	}

	public int speakVoice(String voiceUrl, PreemptType bPreempt) {
		return speakVoice("", voiceUrl, bPreempt, null);
	}

	public int speakVoice(String voiceUrl) {
		return speakVoice("", voiceUrl, DEFAULT_PREEMPT_FLAG, null);
	}

	public int speakVoice(String sText, String voiceUrl, ITtsCallback oRun) {
		return speakVoice(sText, voiceUrl, DEFAULT_PREEMPT_FLAG, oRun);
	}

	public int speakVoice(String voiceUrl, PreemptType bPreempt,
			ITtsCallback oRun) {
		return speakVoice("", voiceUrl, bPreempt, oRun);
	}

	public int speakVoice(String voiceUrl, ITtsCallback oRun) {
		return speakVoice("", voiceUrl, DEFAULT_PREEMPT_FLAG, oRun);
	}

	// /////////////////////////////////////////////////////////////////

	public static final int INVALID_TTS_TASK_ID = 0;

	ITts mTts = null;
	TtsRemoteImpl mRemoteTool = new TtsRemoteImpl();

	ITts getTtsTool() {
		return TtsRemoteImpl.useRemoteTtsTool() ? mRemoteTool : mTts;
	}

	int mNextTaskId = 1; // 下一次分配给Tts的任务ID
	List<TtsTask> mTtsTaskQueue = new ArrayList<TtsTask>(); // TTS的等待任务列表
	TtsTask mCurTask = null; // 当前的Tts任务
	TtsTask mOldCurTask = null; // 备份的Tts任务

	public class TtsTask {
		int iTaskId = INVALID_TTS_TASK_ID;
		int iStream = DEFAULT_STREAM_TYPE;
		String sText = "";
		int iVoiceIndex = 0;
		String[] lstVoice = null; // 语音列表
		ITtsCallback oRun;
		long createdTime = 0;
		boolean fromRemote = false; // 标识任务是否来自第三方
		boolean forceStopWakeup = false; // 播放时即使开启了回音消除也不允许打断

		public void enableForceStopWakeup(boolean froceStopWakeUp) {
			this.forceStopWakeup = froceStopWakeUp;
		}

		public boolean isForceStopWakeup() {
			return forceStopWakeup;
		}
	}

	int speakText(TtsTask t) {
		if (TextUtils.isEmpty(ImplCfg.getTtsImplClass())) {
			if (t != null && t.oRun != null) {
				AppLogic.runOnBackGround(new Runnable1<TtsTask>(t) {
					@Override
					public void run() {
						JNIHelper.logd("speakText end onError: id="
								+ mP1.iTaskId);
						mP1.oRun.onError(ERROR_ABORT);
						mP1.oRun.onEnd();
					}
				}, 0);
			}
			return 0;
		}

		if (!TextUtils.isEmpty(t.sText)) {
			t.sText = ZHConverter.convert(t.sText, ZHConverter.SIMPLIFIED);
		}

		mCurTask = t;

		if (System.currentTimeMillis() - t.createdTime > DEFAULT_TIMEOUT) {
			JNIHelper.loge("createTime:" + t.createdTime
					+ ",drop overtimed tts task!");
			if (mSpeakEndCallback != null) {
				AppLogic.runOnBackGround(new Runnable() {
					@Override
					public void run() {
						mSpeakEndCallback.onCancel();
					}
				}, 0);

			}
			return 0;
		}

		JNIHelper.logd("speakText begin: id=" + t.iTaskId + ",stream="
				+ t.iStream + ",text=" + t.sText + ",sco="
				+ BluetoothManager.getInstance().isScoStateOn());

		int filterIndex = -1;
		filterIndex = filterText(t.sText);

		do {
			if (filterIndex != -1) {
				String voicePathPrefix = null;
				if (getTtsTool().getClass().getName()
						.endsWith("TtsYunzhishengImpl")
						|| getTtsTool().getClass().getName()
								.endsWith("TtsYunzhisheng_3_0_Impl")) {
					voicePathPrefix = GlobalContext.get().getApplicationInfo().dataDir
							+ "/tts_yunzhisheng/";
				} else if (getTtsTool().getClass().getName()
						.endsWith("TtsIflyImpl")) {
					voicePathPrefix = GlobalContext.get().getApplicationInfo().dataDir
							+ "/tts_ifly/";
				} else {
					// 不支持录音版TTS
					filterIndex = -1;
					break;
				}
				String endString = ".ogg";
				// 判断录音文件是否存在
				String tempStr = voicePathPrefix + tts_map[filterIndex + 1]
						+ endString;
				File file = new File(tempStr);
				if (!file.exists()) {
					JNIHelper.loge(tempStr + " is not existed");
					filterIndex = -1;
					break;
				}

				String[] voice = null;
				if (t.lstVoice == null) {
					voice = new String[1];
					voice[0] = voicePathPrefix + tts_map[filterIndex + 1]
							+ endString;
				} else {
					int length = t.lstVoice.length;
					voice = new String[length + 1];
					voice[0] = voicePathPrefix + tts_map[filterIndex + 1]
							+ endString;
					for (int i = 1; i < length + 1; i++) {
						voice[i] = t.lstVoice[i - 1];
					}
				}
				t.lstVoice = voice;

			}
		} while (false);

		if (mCurTask.iStream == DEFAULT_STREAM_TYPE) {
			if (BluetoothManager.getInstance().isScoStateOn())
				mCurTask.iStream = AudioManager.STREAM_VOICE_CALL;
			else
				mCurTask.iStream = TtsUtil.DEFAULT_TTS_STREAM; // 默认使用通知的通道
		}
		// 校验通道的音量，太小给出震动提示
		VolumeManager.getInstance().checkVolume(mCurTask.iStream, true, true);

		if (mCurTask == null || TextUtils.isEmpty(mCurTask.sText)) {
			// 文本为空时强制跳过文本播报
			filterIndex = 0;
		}

		// 统计TTS说法 andyzhao 2016-06-01
		TXZStatisticser.append(mCurTask.sText);

		int ret = ITts.ERROR_UNKNOW;
		if (filterIndex != -1) {
			ret = ITts.ERROR_SUCCESS;
		} else {
			ret = getTtsTool().start(mCurTask.iStream,
					mCurTask.sText.replace("同行者", "同形者"), mSpeakEndCallback);
		}
		if (ret == ITts.ERROR_SUCCESS) {
			TtsTask curTask = mCurTask;
			int curStream = -1;
			if (curTask != null) {
				curStream = curTask.iStream;
			}
			MusicManager.getInstance().onBeginTts(curStream, curTask);
			// requestMediaFocus();
		}

		if (filterIndex != -1) {
			speakNextVoice();
		}
		return ret;
	}

	// 处理需要抢占焦点时的逻辑
	/*
	 * private void requestMediaFocus(){ if(mCurTask.fromRemote &&
	 * TXZMediaFocusManager.getInstance().isFocusGained()){ setVolumeRate(0.4f);
	 * }else{ setVolumeRate(1.0f);
	 * MusicManager.getInstance().onBeginTts(mCurTask.iStream); } }
	 */

	MediaPlayer mVoicePlayer = new MediaPlayer();

	void speakNextVoice() {
		AppLogic.removeBackGroundCallback(mRunnableSpeakNextVoice);
		AppLogic.runOnBackGround(mRunnableSpeakNextVoice, 0);
	}

	Runnable mRunnableSpeakNextVoice = new Runnable() {
		@Override
		public void run() {
			if (mCurTask == null || mCurTask.lstVoice == null
					|| mCurTask.iVoiceIndex < 0
					|| mCurTask.iVoiceIndex >= mCurTask.lstVoice.length) {
				if (mCurTask != null) {
					JNIHelper.logd("speakText end: id=" + mCurTask.iTaskId
							+ ",stream=" + mCurTask.iStream + ",text="
							+ mCurTask.sText);

					AppLogic.runOnBackGround(new Runnable1<TtsTask>(mCurTask) {
						@Override
						public void run() {
							if (mP1 != null && mP1.oRun != null) {
								JNIHelper.logd("speakText end onSuccess: id="
										+ mP1.iTaskId);
								mP1.oRun.onSuccess();
								mP1.oRun.onEnd();
							}
						}
					}, 0);
				}
				mCurTask = null;
				speakNext();
				return;
			}
			// 播放下一段声音文件
			JNIHelper.logd("begin play voice[" + mCurTask.iVoiceIndex + "/"
					+ mCurTask.lstVoice.length + "]");
			// 播放下一段声音文件
			JNIHelper.logd("begin play voice["
					+ mCurTask.lstVoice[mCurTask.iVoiceIndex] + "]");
			if (mCurTask.lstVoice[mCurTask.iVoiceIndex].isEmpty()) {
				mCurTask.iVoiceIndex++;
				speakNextVoice();
				return;
			}
			//播放BEEP
			if (BEEP_VOICE_URL.equals(mCurTask.lstVoice[mCurTask.iVoiceIndex])) {
				mCurTask.iVoiceIndex++;
				BeepPlayer.play(mCurTask.iStream, new Runnable() {
					@Override
					public void run() {
						speakNextVoice();
					}
				});
				return;
			}
			mVoicePlayer.setAudioStreamType(mCurTask.iStream);
			mVoicePlayer.reset();
			try {
				mVoicePlayer
						.setDataSource(mCurTask.lstVoice[mCurTask.iVoiceIndex]);
			} catch (Exception e) {
				mCurTask.iVoiceIndex++;
				speakNextVoice();
				return;
			}
			mCurTask.iVoiceIndex++;
			mVoicePlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					mVoicePlayer.start();
					if (mCurTask != null) {
						MusicManager.getInstance().onBeginTts(mCurTask.iStream,mCurTask);
						// requestMediaFocus();
					} else {
						speakNext();
					}
				}
			});
			mVoicePlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					mVoicePlayer.reset();
					MusicManager.getInstance().onEndTts();
					speakNextVoice();
				}
			});
			mVoicePlayer.prepareAsync();
		}
	};

	Runnable mRunnableSpeakNext = new Runnable() {
		@Override
		public void run() {
			JNIHelper.logd("really speak next: queue=" + mTtsTaskQueue.size());
			if (mCurTask != null) {
				JNIHelper.loge("speakNext error: current task is not end");
				return;
			}

			if (mTtsTaskQueue.isEmpty()) {
				mCurTask = null;
				MusicManager.getInstance().onEndTts();
				return;
			}

			if (canSpeakNow()) {
				mCurTask = mTtsTaskQueue.get(0);
				mTtsTaskQueue.remove(0);
				speakText(mCurTask);
			}
		}
	};

	void speakNext() {
		JNIHelper.logd("speakNext: queue=" + mTtsTaskQueue.size());
		AppLogic.removeBackGroundCallback(mRunnableSpeakNext);
		AppLogic.runOnBackGround(mRunnableSpeakNext, 0);
	}

	ITtsCallback mSpeakEndCallback = new ITtsCallback() {
		@Override
		public void onError(int iError) {
			if (mCurTask != null)
				JNIHelper.loge("speakText end: id=" + mCurTask.iTaskId
						+ ",stream=" + mCurTask.iStream + ",text="
						+ mCurTask.sText + ",error=" + iError);

			AppLogic.runOnBackGround(new Runnable2<TtsTask, Integer>(mCurTask,
					iError) {
				@Override
				public void run() {
					if (mP1 != null && mP1.oRun != null) {
						JNIHelper.logd("speakText end onError: id="
								+ mP1.iTaskId);
						mP1.oRun.onError(mP2);
						mP1.oRun.onEnd();
					}
				}
			}, 0);

			mCurTask = null;

			speakNext();
		}

		@Override
		public void onCancel() {
			if (mCurTask != null)
				JNIHelper.logd("speakText end: id=" + mCurTask.iTaskId
						+ ",stream=" + mCurTask.iStream + ",text="
						+ mCurTask.sText);
			if (isRealCancle)
				AppLogic.runOnBackGround(new Runnable1<TtsTask>(mCurTask) {
					@Override
					public void run() {
						if (mP1 != null && mP1.oRun != null) {
							JNIHelper.logd("speakText end onCancel: id="
									+ mP1.iTaskId);
							mP1.oRun.onCancel();
							mP1.oRun.onEnd();
						}
					}
				}, 0);

			mCurTask = null;

			speakNext();
		}

		@Override
		public void onSuccess() {
			speakNextVoice();
		}
	};

	boolean canSpeakNow() {
		if (getTtsTool() == null // 组件未构造
				|| isInitSuccessed() == false // 初始化未完成
				|| mCurTask != null // 当前有TTS任务
				|| AsrManager.getInstance().isBusy() // 正在录音中
				|| RecordManager.getInstance().isBusy()
				|| (!CallManager.getInstance().isIdle() && !CallManager
						.getInstance().isRinging()))// 电话忙，并且不是来电响铃
			return false;
		return true;
	}

	public void insertSpeakTask(PreemptType bPreempt, TtsTask t) {
		t.createdTime = System.currentTimeMillis();
		isRealCancle = true;
		AppLogic.runOnBackGround(new Runnable2<PreemptType, TtsTask>(bPreempt,
				t) {
			@Override
			public void run() {
				PreemptType bPreempt = mP1;
				TtsTask t = mP2;
				JNIHelper.logd("really begin play tts: " + t.iTaskId);
				if (bPreempt == PreemptType.PREEMPT_TYPE_FLUSH) {
					clearSpeak(t);
					return;
				}
				// 添加到队列
				if (!canSpeakNow()) {
					JNIHelper.logd("push in tts queue: bPreempt=" + bPreempt);
					if (bPreempt != PreemptType.PREEMPT_TYPE_NONE)
						mTtsTaskQueue.add(0, t);
					else
						mTtsTaskQueue.add(t);
					if (bPreempt == PreemptType.PREEMPT_TYPE_IMMEADIATELY) {
						if (mCurTask == null)
							speakNext();
						else
							cancelCurTask();
					}
					if (bPreempt == PreemptType.PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE) {
						if (mCurTask == null)
							speakNext();
						else {
							isRealCancle = false;
							mOldCurTask = mCurTask;
							cancelCurTask();
							AppLogic.runOnBackGround(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									insertSpeakTask(
											PreemptType.PREEMPT_TYPE_NEXT,
											mOldCurTask);
								}
							}, 200);
						}
					}
					return;
				}
				// 直接合成
				if (ITts.ERROR_SUCCESS != speakText(t)) {
					if (t.oRun != null) {
						JNIHelper
								.logd("speakText end onError: id=" + t.iTaskId);
						t.oRun.onError(ITts.ERROR_UNKNOW);
						t.oRun.onEnd();
					}
				}
			}
		}, 0);
	}

	public int speakText(int iStream, String sText, PreemptType bPreempt, ITtsCallback oRun) {
		JNIHelper.logd("speakText: stream=" + iStream + ",text=" + sText
				+ ",bPreempt=" + bPreempt);

		// 创建新的任务
		TtsTask t = new TtsTask();
		t.iStream = iStream;
		t.sText = sText;
		t.oRun = oRun;
		synchronized (mSpeakEndCallback) {
			t.iTaskId = mNextTaskId++;
			if (mNextTaskId <= 0)
				mNextTaskId = 1;
		}
		if (oRun != null) {
			oRun.setTaskId(t.iTaskId);
		}
		insertSpeakTask(bPreempt, t);
		return t.iTaskId;
	}
	
	
	public int speakTextNoWakeup(int iStream, String sText, PreemptType bPreempt, ITtsCallback oRun){
		JNIHelper.logd("speakText: stream=" + iStream + ",text=" + sText
				+ ",bPreempt=" + bPreempt);
		// 创建新的任务
		TtsTask t = new TtsTask();
		t.iStream = iStream;
		t.sText = sText;
		t.oRun = oRun;
		t.forceStopWakeup = true;
		synchronized (mSpeakEndCallback) {
			t.iTaskId = mNextTaskId++;
			if (mNextTaskId <= 0)
				mNextTaskId = 1;
		}
		if (oRun != null) {
			oRun.setTaskId(t.iTaskId);
		}
		insertSpeakTask(bPreempt, t);
		return t.iTaskId;
	}
	
	// /////////////////////////////////////////////////////////////////

	void cancelCurTask() {
		// 不用处理当前mCurTask，stop后会回调speakEnd，里面会进行处理
		if (null != mCurTask) {
			if (getTtsTool() != null && getTtsTool().isBusy())
				getTtsTool().stop();
			else {
				mVoicePlayer.reset();
				mSpeakEndCallback.onCancel();
			}
		}
	}

	public void errorCurTask() {
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				if (null != mCurTask) {
					JNIHelper.logd("speakText end errorCurTask : id="
							+ mCurTask.iTaskId);
					final ITtsCallback cb = mCurTask.oRun;
					if (cb != null) {
						mCurTask.oRun = new ITtsCallback() {
							@Override
							public void onCancel() {
								cb.onError(ERROR_ABORT);
							}

							@Override
							public void onEnd() {
								cb.onEnd();
							}

							@Override
							public void onSuccess() {
								cb.onSuccess();
							}

							@Override
							public void onError(int iError) {
								cb.onError(iError);
							}
						};
					}
					cancelCurTask();
				}
			}
		}, 0);
	}

	public void cancelSpeak(int iTaskId) {
		if (iTaskId == INVALID_TTS_TASK_ID)
			return;
		TtsTask t = mCurTask;
		if (t == null)
			t = new TtsTask();
		JNIHelper.recordLogStack(1);
		JNIHelper.logd("cancelSpeak[" + iTaskId + "]: curTask=" + t.iTaskId
				+ ",text=" + t.sText);
		AppLogic.runOnBackGround(new Runnable1<Integer>(iTaskId) {
			@Override
			public void run() {
				int iTaskId = mP1;
				JNIHelper.logd("really begin cancel tts: " + iTaskId);
				if (mCurTask != null && mCurTask.iTaskId == iTaskId) {
					cancelCurTask();
					return;
				}
				for (int i = 0; i < mTtsTaskQueue.size(); ++i) {
					if (mTtsTaskQueue.get(i).iTaskId == iTaskId) {
						if (mTtsTaskQueue.get(i).oRun != null) {
							AppLogic.runOnBackGround(new Runnable1<TtsTask>(
									mTtsTaskQueue.get(i)) {
								@Override
								public void run() {
									JNIHelper
											.logd("speakText end onCancel: id="
													+ mP1.iTaskId);
									mP1.oRun.onCancel();
									mP1.oRun.onEnd();
								}
							}, 0);
						}
						mTtsTaskQueue.remove(i);
						return;
					}
				}
			}
		}, 0);
	}

	// /////////////////////////////////////////////////////////////////

	/**
	 * 清理所有tts语音，暂时不开放，业务不应该有该调用
	 */
	protected void clearSpeak(TtsTask newTask) {
		AppLogic.runOnBackGround(new Runnable1<TtsTask>(newTask) {
			@Override
			public void run() {
				TtsTask newTask = mP1;
				List<TtsTask> q = mTtsTaskQueue;
				mTtsTaskQueue = new ArrayList<TtsTask>();// 必须先清空列表
				TtsTask old = mCurTask;
				mCurTask = null;
				for (int i = 0; i < q.size(); ++i) {
					TtsTask t = q.get(i);
					if (t.oRun != null) {
						JNIHelper.logd("speakText end onCancel: id="
								+ t.iTaskId);
						t.oRun.onCancel();
						t.oRun.onEnd();
					}
				}
				if (newTask != null) {
					mTtsTaskQueue.add(newTask);
				}
				mCurTask = old;
				if (mCurTask != null) {
					cancelCurTask();
				} else {
					speakNext();
				}
			}
		}, 0);
	}

	// /////////////////////////////////////////////////////////////////
	public boolean isBusy() {
		return mCurTask != null || mTtsTaskQueue.isEmpty() == false;
	}

	// /////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SPEAK_WORDS);
		regEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_SPEAK_WORDS_CLOSE_RECORD);
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		switch (eventId) {
		case UiEvent.EVENT_VOICE:
			switch (subEventId) {
			case VoiceData.SUBEVENT_VOICE_SPEAK_WORDS:
				speakText(new String(data), PreemptType.PREEMPT_TYPE_NEXT);
				break;
			case VoiceData.SUBEVENT_VOICE_SPEAK_WORDS_CLOSE_RECORD:
				RecorderWin.speakTextWithClose(new String(data), null);
				break;
			}
		}
		return super.onEvent(eventId, subEventId, data);
	}

	/**
	 * 暂停TTS，暂时直接停掉当前任务
	 */
	public void pause() {
		TtsTask t = mCurTask;
		if (t == null)
			t = new TtsTask();
		JNIHelper.logd("pause: curTask=" + t.iTaskId + ",text=" + t.sText);
		cancelCurTask();
	}

	/**
	 * 恢复TTS
	 */
	public void resume() {
		JNIHelper.logd("resume: " + mTtsTaskQueue.size());
		speakNext();
	}

	private String[] tts_map = { "没有匹配的结果", "RS_NONE_MATCH", "呼叫已取消",
			"RS_CALL_CANCEL", "抱歉，我不太理解您的意思", "RS_VOICE_UNKNOW_LOCAL",
			"没有听清楚，请再说一遍", "RS_VOICE_UNKNOW", "没有找到相关应用",
			"RS_VOICE_APP_NOT_FOUND", "抱歉，当前不支持该操作",
			"RS_VOICE_UNSUPPORT_OPERATE", "请问你要去哪里",
			"RS_VOICE_UNKNOW_NAVIGATE_TARGET", "没有找到目的地",
			"RS_VOICE_POI_NOT_FOUND", "网络不稳定，无法搜索歌曲",
			"RS_VOICE_MUSIC_NOT_FOUND", "当前没有任何收藏音乐",
			"RS_VOICE_MUSIC_NO_FAVOURITED", "抱歉，本地未找到音乐文件，请先下载",
			"RS_VOICE_NONE_MUSIC", "没有找到相关新闻", "RS_VOICE_NEWS_NOT_FOUND",
			"抱歉，暂时没有下载到新闻资源", "RS_VOICE_NONE_NEWS", "您可以说打电话给张三",
			"RS_VOICE_WHO_DO_YOU_WANT_TO_MAKE_CALL", "您要导航到什么地方",
			"RS_VOICE_WHERE_DO_YOU_WANT_TO_NAVIGATE", "没有找到相关联系人",
			"RS_VOICE_CONTACT_NOT_FOUND", "找到以下联系人，请选择",
			"RS_VOICE_MAKE_CALL_LIST", "更新联系人完成",
			"RS_VOICE_UPDATE_CONTACTS_COMPLETED", "创建别名完成",
			"RS_VOICE_CREATE_CONTACTS_ALIAS_COMPLETED", "导入服务号码列表完成",
			"RS_VOICE_REFRESH_SERVICE_CONTACTS_COMPLETED", "本地未找到，即将上网进行搜索",
			"RS_VOICE_SEARCH_MUSIC", "抱歉，未能找到结果", "RS_VOICE_NAV_NONE_FOUND",
			"导航结束", "RS_VOICE_NAV_END", "找到如下结果，请说第几个选择，或取消",
			"RS_VOICE_NAV_SELECT", "找到如下结果，您要选择第几个为公司的地址",
			"RS_VOICE_NAV_SET_COMPANY", "找到如下结果，您要选择第几个为家的地址",
			"RS_VOICE_NAV_SET_HOME", "找到一个结果，即将开始导航，确定还是取消",
			"RS_VOICE_NAV_CONFIRM", "已经连接上HDIT数据云中心，数据将实时更新!", "RS_HDIT",
			"语音功能已开启，唤醒词为你好小维，你好魔方", "RS_DXWY", "先走一步",
			"RS_VOICE_ASR_CHAT_END_HINT_3", "我在呢", "RS_VOICE_ASR_START_HINT_1",
			"乐意为您效劳", "RS_VOICE_ASR_START_HINT_4", "请在嘀的一声后开始说话",
			"RS_VOICE_FIRST", "臣妾在", "RS_VOICE_ASR_START_HINT_KING", "需要帮忙吗",
			"RS_VOICE_ASR_START_HINT_5", "有什么可以帮您",
			"RS_VOICE_ASR_START_HINT_6", "臣妾告退",
			"RS_VOICE_ASR_CHAT_END_HINT_KING", "哈喽",
			"RS_VOICE_ASR_START_HINT_3", "您好", "RS_VOICE_ASR_START_HINT_2",
			"下次见", "RS_VOICE_ASR_CHAT_END_HINT_1", "持续为您服务",
			"RS_VOICE_ASR_CHAT_END_HINT_2","语音引擎初始化成功","RS_VOICE_SDK_INIT_SUCCESS" };

	public int filterText(String text) {
		if (null == text) {
			return -1;
		}
		for (int i = 0; i < tts_map.length;) {
			if (text.equals(tts_map[i])) {
				return i;
			}
			i = i + 2;
		}
		return -1;
	}

	public void setVoiceSpeed(int speed) {
		// TODO
		LogUtil.logd("setVoiceSpeed speed=" + speed);
		if (mInitSuccessed && getTtsTool() != null) {
			getTtsTool().setVoiceSpeed(speed);
			ConfigManager.getInstance().notifyRemoteSync();
		}
	}

	public int getVoiceSpeed() {
		if (mInitSuccessed && getTtsTool() != null) {
			return getTtsTool().getVoiceSpeed();
		}
		return 0;
	}

	public byte[] invokeTXZTts(String serviceName, String command, byte[] data) {
		if (command.equals("set.voicespeed")) {
			if (data == null) {
				return null;
			}
			setVoiceSpeed(Integer.parseInt(new String(data)));
			return null;
		}
		return null;
	}

	public void setTtsModel(String strModel) {
		if (mInitSuccessed && getTtsTool() != null) {
			getTtsTool().setTtsModel(strModel);
		}
	}
}

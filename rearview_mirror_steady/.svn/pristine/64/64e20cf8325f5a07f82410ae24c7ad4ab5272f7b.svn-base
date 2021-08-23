package com.txznet.txz.component.choice.list;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.WinRecordCycler;

import android.os.SystemClock;

/**
 * 做自动选择和首次TTS播报
 * 
 * @param <T>
 * @param <E>
 */
public abstract class AbsWorkChoice<T, E> extends AbstractChoice<T, E> {
	public static final int TOTAL_AUTO_CALL_TIME = 4000;
	public static final int SELECT_OUT_TIME = 30000;
	public static final int AUTO_CALL_PERIOD = 100;
	protected CompentOption<E> mCompentOption;

	private int mProgress = 0;
	protected long mProgressBeginTime = 0; // 进度启动时间

	private boolean mCanAutoSelect = false;
	private boolean mHasSpeakTtsEnd = false;

	Runnable mRunnableAddProgress = new Runnable() {
		@Override
		public void run() {
			AppLogic.removeBackGroundCallback(mRunnableAddProgress);
			if (mCanAutoSelect == false)
				return;
			mProgress += (100.0 * AUTO_CALL_PERIOD / mCompentOption.getProgressDelay());
			RecorderWin.refreshProgressBar(Math.round(mProgress), 0);
			if (mProgress >= 100.0) {
				onProgressEnd();
				return;
			}
			AppLogic.runOnBackGround(mRunnableAddProgress, AUTO_CALL_PERIOD);
		}
	};
	Runnable mRunnableContinueProgress = new Runnable() {
		@Override
		public void run() {
			continueProgress();
		}
	};

	public AbsWorkChoice(CompentOption<E> option) {
		mCompentOption = option;
	}

	public void updateCompentOption(CompentOption<E> option, boolean updateShow) {
		LogUtil.logd("updateCompentOption:" + option);
		this.mCompentOption = option;
		if (updateShow) {
			showChoices(mData);
		}
	}

	public CompentOption<E> getOption() {
		return this.mCompentOption;
	}
	
	public void resumeWakeupAsrOption() {
		if (mAsr != null) {
			activeWakeupAsr(mAsr);
		}
	}
	
	AsrComplexSelectCallback mAsr;
	
	@Override
	protected void activeWakeupAsr(AsrComplexSelectCallback acsc) {
		mAsr = acsc;
		Boolean banWp = getOption().getBanWakeup();
		if (banWp != null && banWp) {
			WakeupManager.getInstance().recoverWakeupFromAsr(getReportId());
			WinRecordCycler.getInstance().clearAsrComplexSelectCallback(getReportId());
			return;
		}
		super.activeWakeupAsr(acsc);
	}
	
	@Override
	public void showChoices(T data) {
		mHasSpeakTtsEnd = false;
		mCanAutoSelect = false;

		super.showChoices(data);
		analyzeOption(mCompentOption);
	}
	
	protected void analyzeOption(CompentOption<E> option) {
		if (option == null) {
			// 一般不能为空
			return;
		}

		String tts = "";
		tts = mCompentOption.getTtsText();
		Integer isProgress = mCompentOption.getProgressDelay();
		if (isProgress != null) {
			mCanAutoSelect = isProgress > 0;
		}
		// 监听器
		registerListener(mCompentOption.getCallbackListener());

		speakTts(tts, TtsManager.BEEP_VOICE_URL);
	}

	private void speakTts(String tts, String voiceUrl) {
		mSpeechTaskId = TtsManager.getInstance().speakVoice(tts,
				InterruptTts.getInstance().isInterruptTTS() ? "" : voiceUrl, PreemptType.PREEMPT_TYPE_NEXT,
				new TtsUtil.ITtsCallback() {

					@Override
					public void onBegin() {
						onSpeakTtsBegin();
					}
					
					@Override
					public void onCancel() {
						LogUtil.logd("onEnd isOpen:" + RecorderWin.isOpened());
						if (RecorderWin.isOpened() && isSelecting()) {
							onSuccess();
							return;
						}
					}

					@Override
					public void onEnd() {
						onSpeakTtsEnd();
					}

					@Override
					public void onSuccess() {
						AsrManager.getInstance().mSenceRepeateCount = 0;
						JNIHelper.logd("call select SenceRepeateCount: " + AsrManager.getInstance().mSenceRepeateCount);
						if (isCoexistAsrAndWakeup() && !InterruptTts.getInstance().isInterruptTTS()) {
							AsrManager.getInstance().mSenceRepeateCount++;
							if (AsrManager.getInstance().mSenceRepeateCount < AsrManager.ASR_SENCE_REPEATE_COUNT) {
								AsrManager.getInstance().start(createSelectAgainAsrOption());
							}
						}

						onSpeakTtsSuccess();
					}

					@Override
					public boolean isNeedStartAsr() {
						return true;
					}
				});
	}

	@Override
	protected void onPreWakeupSelect(String command) {
		checkTimeout(true);
	}

	public void continueProgress() {
		JNIHelper.logd("continueProgress");
		AppLogic.removeBackGroundCallback(mRunnableAddProgress);
		AppLogic.runOnBackGround(mRunnableAddProgress, AUTO_CALL_PERIOD);
	}

	public void pauseProgress() {
		JNIHelper.logd("pauseProgress");
		AppLogic.removeBackGroundCallback(mRunnableAddProgress);
	}

	public void clearProgress() {
		JNIHelper.logd("clearProgress");
		mProgress = 0;
		mCanAutoSelect = false;
		AppLogic.removeBackGroundCallback(mRunnableContinueProgress);
		mProgressBeginTime = 0;
		AppLogic.removeBackGroundCallback(mRunnableAddProgress);
		RecorderWin.hideProgressBar(0);
	}

	public void checkTimeout(boolean clearProgress) {
		if (clearProgress) {
			clearProgressMark();
		}

		Long timeout = mCompentOption.getTimeout();
		LogUtil.logd("checkTimeout:" + timeout);
		AppLogic.removeBackGroundCallback(mTimeoutTask);
		if (timeout != null && timeout > 0) {
			AppLogic.runOnBackGround(mTimeoutTask, timeout);
		}
	}

	public void clearTimeout() {
		clearProgressMark();
		AppLogic.removeBackGroundCallback(mTimeoutTask);
	}

	private void clearProgressMark() {
		clearProgress();
		mCanAutoSelect = false;
	}

	Runnable mTimeoutTask = new Runnable() {

		@Override
		public void run() {
			AppLogic.removeBackGroundCallback(mTimeoutTask);
			onTimeout();
		}
	};

	@Override
	public boolean nextPage(String fromVoice) {
		checkTimeout(true);
		if (!is2_0Version()) {
			WinManager.getInstance().getAdapter().snapPager(true);
			return true;
		}
		return super.nextPage(fromVoice);
	}

	@Override
	public boolean lastPage(String fromVoice) {
		checkTimeout(true);
		if (!is2_0Version()) {
			WinManager.getInstance().getAdapter().snapPager(false);
			return true;
		}
		return super.lastPage(fromVoice);
	}

	@Override
	public boolean selectPage(int page, String fromVoice) {
		checkTimeout(true);
		return super.selectPage(page, fromVoice);
	}

	protected void onTimeout() {
		clearIsSelecting();
		selectCancel(SELECT_TYPE_OVERTIME, null);
	}

	protected void onProgressEnd() {
		LogUtil.logd("onProgressEnd");
		// 默认走当前列表第一项
		selectIndex(0, null);
		// selectSure(null);
	}
	
	private boolean mIsTtsSpeaking;

	protected void onSpeakTtsBegin() {
		mIsTtsSpeaking = true;
		RecorderWin.refreshState(RecorderWin.STATE_NORMAL);
	}

	protected void onSpeakTtsEnd() {
		mIsTtsSpeaking = false;
		AsrManager.getInstance().mSenceRepeateCount = 0;
	}
	
	protected boolean onCommandSelect(String type, String command) {
		// 开了v3防误唤醒，tts播报时，不响应“确定”和“取消”
		if (ProjectCfg.getAECPreventFalseWakeup()) {
			if (mIsTtsSpeaking) {
				if ("CANCEL".equals(type) || "SURE".equals(type)) {
					return true;
				} 
			}
		}
		return false;
	}

	protected void onSpeakTtsSuccess() {
		RecorderWin.refreshState(RecorderWin.STATE_RECORD_START);
		mHasSpeakTtsEnd = true;
		mProgressBeginTime = SystemClock.elapsedRealtime();
		checkTimeout(false);
		RecorderWin.addCloseRunnable(new Runnable() {
			@Override
			public void run() {
				AppLogicBase.removeBackGroundCallback(mTimeoutTask);
			}
		});
		if (mCanAutoSelect) {
			LogUtil.logd("start progress:" + getOption().getProgressDelay());
			continueProgress();
		}
	}
	
	@Override
	protected void clearFirstSelecting() {
		clearTimeout();
		super.clearFirstSelecting();
	}

	@Override
	protected void onSpeechBegin() {
		pauseProgress();
		AppLogic.removeBackGroundCallback(mRunnableContinueProgress);
	}

	@Override
	protected void onSpeechEnd() {
		if (mCanAutoSelect && mHasSpeakTtsEnd) {
			AppLogic.runOnBackGround(mRunnableContinueProgress, 0);
		}
	}
	
	@Override
	protected void onClearSelecting() {
		clearProgressMark();
		mAsr = null;
		mHasSpeakTtsEnd = false;
		mProgressBeginTime = 0;
		if (getOption().getChoiceCallback() != null) {
			getOption().getChoiceCallback().onClearIsSelecting();
		}
	}
}
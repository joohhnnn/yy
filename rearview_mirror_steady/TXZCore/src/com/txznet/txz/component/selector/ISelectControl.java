package com.txznet.txz.component.selector;

import java.util.List;

import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.selector.PagerAdapter.OnListChangeListener;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.mtj.MtjModule;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.WinRecordCycler;
import com.txznet.txz.util.runnables.Runnable2;

import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.animation.Animation;

public abstract class ISelectControl {
	private static final String REGISTER_WAKEUP = "registerWakeup";
	private static final String COMMAND_SELECT = "commSelect";
	private static final String INDEX_SELECT = "indexSelect";
	
	public static final String ASR_CANCEL_HINT = NativeData.getResString("RS_SELECTOR_OPERATION_CANCEL");
	public static final String ASR_CANCEL_BACK_HINT = NativeData.getResString("RS_SELECTOR_HELP");

	protected volatile boolean mIsSelecting = false;
	protected boolean mUseAutoPerform = false;
	protected boolean mHasPopGrammar = true;
	protected boolean mNeedPlayWavTip = true;
	protected boolean mCanAutoPerform;
	public boolean mExitWithBack = false;
	public boolean mUseNewSelector = false;

	protected int mPageCount = 4;
	public static long sDismissDelay = 0;
	public static int mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;

	protected String mLastHintTxt = "";
	protected String mVoiceUrl = TtsManager.BEEP_VOICE_URL;

	protected List mTmpList;
	protected List mSourceList;

	protected DelayTask mDelayTask;
	protected OnItemSelectListener mOnItemSelectListener;
	public PageHelper mPageHelper;

	public ISelectControl(int pageCount) {
		mPageCount = pageCount;
	}

	public void beginSelectorParse(List src, String spk) {
		if (src == null) {
			return;
		}

		mSourceList = src;
		mTmpList = mSourceList;

		// 初始化显示参数
		initBeginSelector(spk);

		if (src.size() == 0 || !isDelayAddWkWords()) {
			speakHintText();
		} else {
			WinManager.getInstance().addViewStateListener(new IViewStateListener(){
				@Override
				public void onAnimateStateChanged(Animation animation, int state) {
					if(IViewStateListener.STATE_ANIM_ON_START == state){
						speakHintText();
						WinManager.getInstance().removeViewStateListener(this);
					}
					super.onAnimateStateChanged(animation, state);
				}
			});
		}
	}

	public void initBeginSelector(String spkTxt) {
		// 清除上次的记录
		clearIsSelecting();
		mIsSelecting = true;
		if (mSourceList.size() == 0) {
			mIsSelecting = false;
			mVoiceUrl = "";
		} else {
			if (mHasPopGrammar) {
				mHasPopGrammar = false;
				AsrManager.getInstance().pushGrammarId(getSenceGrammar());
			}
			mVoiceUrl = TtsManager.BEEP_VOICE_URL;
		}

		mLastHintTxt = getBeginSelectorHint();

		if (!TextUtils.isEmpty(spkTxt)) {
			mLastHintTxt = spkTxt;
		}
		
		RecorderWin.show();

		// 更新页码器
		updatePageHelper();
		// 更新到时计数器
		updateDelayTask();
	}

	AsrOption mAsrOptionSelectAgain = new AsrOption().setManual(false).setNeedStopWakeup(false).setPlayBeepSound(false);

	public void selectAgain() {
		JNIHelper.logd("selectAgain IsSelecting:" + mIsSelecting);
		if (!mIsSelecting) {
			return;
		}

		JNIHelper.logd("call select SenceRepeateCount: " + AsrManager.getInstance().mSenceRepeateCount);
		if (ProjectCfg.mCoexistAsrAndWakeup && AsrManager.getInstance().mSenceRepeateCount != -1 && !InterruptTts.getInstance().isInterruptTTS()) {
			AsrManager.getInstance().mSenceRepeateCount++;
			if (AsrManager.getInstance().mSenceRepeateCount < AsrManager.ASR_SENCE_REPEATE_COUNT) {
				AsrManager.getInstance().start(mAsrOptionSelectAgain);
			}
		}

		useWakeupSelector(false);
	}
	
	private byte[] genJosn(String action,String type,String speech){
		JSONBuilder jb = new JSONBuilder();
		jb.put("scene", "selector");
		if (!TextUtils.isEmpty(type)) {
			jb.put("type", type);
		}
		if (!TextUtils.isEmpty(speech)) {
			jb.put("speech", speech);
		}
		jb.put("action", action);
		return jb.toBytes();
	}

	private void speakHintText() {
		mSpeechTaskId = TtsManager.getInstance().speakVoice(mLastHintTxt, InterruptTts.getInstance().isInterruptTTS()?"":mVoiceUrl, PreemptType.PREEMPT_TYPE_NEXT,
				new TtsUtil.ITtsCallback() {

					@Override
					public void onSuccess() {
						AsrManager.getInstance().mSenceRepeateCount = 0;
						if (!isSelecting()) {
							int grammarId = AsrManager.getInstance().getLastGrammarId();
							if(grammarId == VoiceData.GRAMMAR_SENCE_SET_HOME || grammarId == VoiceData.GRAMMAR_SENCE_SET_COMPANY){
								AsrManager.getInstance().start(new AsrOption().setGrammar(grammarId));
							}else{
								AsrManager.getInstance().start();
							}
							return;
						}

						JNIHelper.logd("call select SenceRepeateCount: " + AsrManager.getInstance().mSenceRepeateCount);
						if (ProjectCfg.mCoexistAsrAndWakeup && !InterruptTts.getInstance().isInterruptTTS()) {
							AsrManager.getInstance().mSenceRepeateCount++;
							if (AsrManager.getInstance().mSenceRepeateCount < AsrManager.ASR_SENCE_REPEATE_COUNT) {
								AsrManager.getInstance().start(mAsrOptionSelectAgain);
							}
						}

						mCanAutoPerform = true;
						if (mUseAutoPerform) {
							if (!isPauseProgress) {
								mDelayTask.continueProgress();
							}
							isPauseProgress = false;
						}

						checkDismissTask();
					}
					
					@Override
					public boolean isNeedStartAsr() {
						return true;
					}
				});
	}
	
	public void cancelProgress() {
		RecorderWin.refreshProgressBar(0, 0);
		mUseAutoPerform = false;
		if (mDelayTask != null) {
			mDelayTask.clearProgress();
		}
	}
	
	
	private boolean isPauseProgress = false;
	
	private void addWakeupCmds(){
		AsrComplexSelectCallback acsc = new AsrComplexSelectCallback() {
			Runnable removeBackground = null;
			Runnable2<Object, String> taskRunnable = null;
			private static final int speechDelay = 700;
			private static final int handleDelay = 800;
			private boolean isEnd = false;
			private long mLastSpeechEndTime = 0;
			@Override
			public boolean needAsrState() {
				if (InterruptTts.getInstance().isInterruptTTS()) {//如果是识别模式，就不需要开启beep音
					return false;
				}else {
					return true;
				}
			}

			@Override
			public String getTaskId() {
				return getAsrTaskId();
			}

			@Override
			public void onSpeechBegin() {
				super.onSpeechBegin();
				if (mUseAutoPerform && mCanAutoPerform) {
					if (null != mDelayTask) {
						isPauseProgress = true;
						mDelayTask.pauseProgress();
					}
				}
			}

			@Override
			public void onSpeechEnd() {
				super.onSpeechEnd();
				if (mUseAutoPerform && mCanAutoPerform) {
					if (null != mDelayTask) {
						isPauseProgress = false;
						mDelayTask.continueProgress();
					}
				}
				mLastSpeechEndTime = SystemClock.elapsedRealtime();
				if(removeBackground != null){
					AppLogic.removeBackGroundCallback(removeBackground);
				}
			}

			@Override
			public void onCommandSelected(final String type, String command) {
				if (taskRunnable != null) {
					AppLogic.removeBackGroundCallback(taskRunnable);
					taskRunnable = null;
				}
				
				taskRunnable = new Runnable2<Object, String>(type,command) {
					
					@Override
					public void run() {
						
						JNIHelper.logd("do onCommandSelected");
						
						String type = (String) mP1;
						String command = mP2;
						isEnd = true;
						if ("滴个".equals(command)) {
							command = "第一个";
						}
						
						if (mUseAutoPerform && mDelayTask != null) {
							mDelayTask.clearProgress();
						}
						checkDismissTask();
		
						MtjModule.getInstance().event(MtjModule.EVENTID_VOICE_MULTIPLE);
						if (SenceManager.getInstance().noneedProcSence("selector", genJosn(COMMAND_SELECT, type, command))) {
							return;
						}
				
						if (InterruptTts.getInstance().isInterruptTTS() || ProjectCfg.mCoexistAsrAndWakeup) {
							//唤醒结果执行时，如果还在录音，则取消掉
							if (AsrManager.getInstance().isBusy()) {
								AsrManager.getInstance().cancel();
							}
						}
	
						if (onWakeupItemSelect(isWakeupResult(), type, command)) {
							return;
						}
		
						if ("PRE_PAGER".equals(type)) {
							boolean prev = mPageHelper.prevPager();
							snapPager(false, prev, command);
							return;
						}
		
						if ("NEXT_PAGER".equals(type)) {
							boolean next = mPageHelper.nextPager();
							snapPager(true, next, command);
							return;
						}
						if ("SURE".equals(type)) {
							selectSure(true);
							return;
						}
		
						if ("CANCEL".equals(type)) {
							selectWithCancel(command);
							return;
						}
		
						if (type.startsWith("PAGE_INDEX_")) {
							int index = Integer.parseInt(type.substring("PAGE_INDEX_".length()));
							selectSpeech(NativeData.getResPlaceholderString("RS_SELECTOR_SELECT", "%CMD%", command));
							mPageHelper.onPageSelect(index);
							return;
						}
						if (type.startsWith("ITEM_INDEX_")) {
							int index = Integer.parseInt(type.substring("ITEM_INDEX_".length()));
							onSelect(mTmpList, index, command);
							return;
						}
						if (type.equals("AFRONT$")) {
							onSelect(mTmpList, 0, command);
							return;
						}
						if (type.equals("ABEHIND$")) {
							onSelect(mTmpList, 1, command);
							return;
						}
						if (type.equals("MOST_AFRONT$")) {
							if (SenceManager.getInstance().noneedProcSence("selector",
									genJosn(INDEX_SELECT, "SELECT_FIRST", command))) {
		
							}
							onSelect(mTmpList, 0, command);
							return;
						}
						if (type.startsWith("MOST_ABEHIND$_")) {
							if (SenceManager.getInstance().noneedProcSence("selector",
									genJosn(INDEX_SELECT, "SELECT_LAST", command))) {
		
							}
							int index = Integer.parseInt(type.substring("MOST_ABEHIND$_".length()));
							onSelect(mTmpList, index, command);
							return;
						}
					}
				};
				removeBackground = new Runnable() {
	
					@Override
					public void run() {
//						AppLogic.removeBackGroundCallback(taskRunnable);
					}
				};
				if (InterruptTts.getInstance().isInterruptTTS() || ProjectCfg.mCoexistAsrAndWakeup) {
					if (isWakeupResult()) {//是唤醒的结果
						isEnd = false;
						//判断唤醒的说话结束了
						if (SystemClock.elapsedRealtime() - mLastSpeechEndTime < 300) {
							AppLogic.runOnBackGround(taskRunnable, 0);
							AppLogic.removeBackGroundCallback(removeBackground);
						}else {
							AppLogic.runOnBackGround(removeBackground, speechDelay);
							AppLogic.runOnBackGround(taskRunnable, handleDelay);							
						}
					} else if (!isEnd) {//识别到的唤醒词并且唤醒没有执行完成
						AppLogic.runOnBackGround(taskRunnable, 0);
						AppLogic.removeBackGroundCallback(removeBackground);
					}
				}else {
					taskRunnable.run();
				}
			}

			@Override
			public void onIndexSelected(final List<Integer> indexs, final String command) {
				MtjModule.getInstance().event(MtjModule.EVENTID_VOICE_MULTIPLE);
				String prefix = "SELECT_";
				String suffix = "" + indexs.get(0);
				if (mUseAutoPerform && mDelayTask != null) {
					mDelayTask.clearProgress();
				}
				checkDismissTask();
				if (SenceManager.getInstance().noneedProcSence("selector",
						genJosn(INDEX_SELECT, prefix + suffix, command))) {
					return;
				}
				if (taskRunnable != null) {
					AppLogic.removeBackGroundCallback(taskRunnable);
					taskRunnable = null;
				}
				taskRunnable = new Runnable2<Object,String>(indexs,command) {
					@Override
					public void run() {
						if (InterruptTts.getInstance().isInterruptTTS() || ProjectCfg.mCoexistAsrAndWakeup) {
							if (AsrManager.getInstance().isBusy()) {
								AsrManager.getInstance().cancel();
							}
						}
						List<Integer> indexs = (List<Integer>) mP1;
						String command = mP2; 
						isEnd = true;
						ISelectControl.this.onIndexSelected(isWakeupResult(), indexs, command);
					}
				};
				removeBackground = new Runnable() {
	
					@Override
					public void run() {
						AppLogic.removeBackGroundCallback(taskRunnable);
					}
				};
				if (InterruptTts.getInstance().isInterruptTTS() || ProjectCfg.mCoexistAsrAndWakeup) {
				
					if (isWakeupResult()) {
						isEnd = false;
						//判断唤醒的说话结束了
						if (SystemClock.elapsedRealtime() - mLastSpeechEndTime < 300) {
							AppLogic.runOnBackGround(taskRunnable, 0);
							AppLogic.removeBackGroundCallback(removeBackground);
						} else {
							AppLogic.runOnBackGround(removeBackground, speechDelay);
							AppLogic.runOnBackGround(taskRunnable, handleDelay);
						}
					} else if (!isEnd) {
						AppLogic.runOnBackGround(taskRunnable, 0);
						AppLogic.removeBackGroundCallback(removeBackground);
					}
				}else {
					taskRunnable.run();
				}
			}
		};

		acsc.addCommand("CANCEL", NativeData.getResStringArray("RS_CMD_SELECT_CANCEL"));
		// 如果是大于一页的个数，或者是自定义了界面，注册上下页
		if (mSourceList.size() > mPageCount || !mUseNewSelector) {
			acsc.addCommand("PRE_PAGER", NativeData.getResStringArray("RS_CMD_SELECT_PRE"));
			acsc.addCommand("NEXT_PAGER", NativeData.getResStringArray("RS_CMD_SELECT_NEXT"));
		}
		if (mSourceList.size() == 1) {
			acsc.addCommand("SURE", NativeData.getResStringArray("RS_CMD_SELECT_SURE"));
		}

		int pageCount = mSourceList.size() / mPageCount;
		if (mSourceList.size() % mPageCount != 0) {
			pageCount++;
		}

		if (mUseNewSelector) {
			for (int i = 1; i <= pageCount; i++) {
				String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i);
				acsc.addCommand("PAGE_INDEX_" + i, "第" + strIndex + "页");
			}
		}
		for (int i = 0; i < mTmpList.size(); i++) {
			String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i + 1);
			if (i == 0) {
				acsc.addCommand("ITEM_INDEX_" + i, NativeData.getResStringArray("RS_CMD_SELECT_FIRST"));
			} else {
				acsc.addCommand("ITEM_INDEX_" + i, "第" + strIndex + "个", "第" + strIndex + "条");
			}
		}
		
		if (mTmpList.size() == 2) {
			acsc.addCommand("AFRONT$", NativeData.getResStringArray("RS_CMD_SELECT_AFRONT"));
			acsc.addCommand("ABEHIND$", NativeData.getResStringArray("RS_CMD_SELECT_ABEHIND"));
		} else if (mTmpList.size() > 2) {
			acsc.addCommand("MOST_AFRONT$", NativeData.getResStringArray("RS_CMD_SELECT_MOST_FRONT"));
			acsc.addCommand("MOST_ABEHIND$_" + (mTmpList.size() - 1),
					NativeData.getResStringArray("RS_CMD_SELECT_MOST_BEHIND"));
		}

		onAsrComplexSelect(acsc);
		WakeupManager.getInstance().useWakeupAsAsr(acsc);
		WinRecordCycler.getInstance().addAsrComplexSelectCallback(acsc, getAsrTaskId());
	}
	
	private static final int DELAY_MAX_ADD_CMDS = 3000;// 最晚3秒添加唤醒词，以避免异常情况下没有回调
	Runnable mTaskAddCmds = new Runnable() {
		@Override
		public void run() {
			if(!isSelecting()){
				return;
			}
			addWakeupCmds();
		}
	};
	
	
	private void useWakeupSelector(boolean playWavTips) {
		if (SenceManager.getInstance().noneedProcSence("selector", genJosn(REGISTER_WAKEUP, "", ""))) {
			return;
		}

		if (mSourceList.size() == 0) {
			JNIHelper.logd("useWakeupSelector is null");
			return;
		}

		// 延迟注册以优化性能
		if (isDelayAddWkWords()) {
			WakeupManager.getInstance().setWakeupBeginTime(SystemClock.elapsedRealtime());
			WinManager.getInstance().addViewStateListener(new IViewStateListener() {
				@Override
				public void onAnimateStateChanged(Animation animation, int state) {
					if (IViewStateListener.STATE_ANIM_ON_END == state) {
						if (!isSelecting()) {
							return;
						}
						AppLogic.removeBackGroundCallback(mTaskAddCmds);
						addWakeupCmds();
						WinManager.getInstance().removeViewStateListener(this);
						
					}
				}
			});
			AppLogic.removeBackGroundCallback(mTaskAddCmds);
			AppLogic.runOnBackGround(mTaskAddCmds, DELAY_MAX_ADD_CMDS);
		} else {
			addWakeupCmds();
		}
		
		if (playWavTips) {
			WakeupManager.getInstance().playAsrTipSound();
		}
	}
	
	private void onIndexSelected(boolean fromWakeup, final List<Integer> indexs, final String command) {
		if (onWakeupIndexSelect(fromWakeup, indexs, command)) {
			return;
		}

		// 反注册唤醒词
		if (!mUseNewSelector) {
			clearIsSelecting();
		}else {
			clearFirstSelect();
		}
//		clearIsSelecting();

		int index = (indexs != null && indexs.size() > 0) ? indexs.get(0) : 0;

		if (mSourceList.size() > index) {
			onCommandSelect(mSourceList, index, command);
		}
	}
	
	public final void onSelect(List tmp, int index, String speech) {
		if (!mUseNewSelector) {
			clearIsSelecting();
		}else {
			clearFirstSelect();
		}
//		clearIsSelecting();
		onCommandSelect(tmp, index, speech);
	}
	
	private void snapPager(final boolean isNext, final boolean bSucc, final String command) {
		if (!mUseNewSelector) {
			WinManager.getInstance().getAdapter().snapPager(isNext);
			return;
		}
		if (isDelayAddWkWords() && bSucc) {
			WinManager.getInstance().addViewStateListener(new IViewStateListener(){
				@Override
				public void onAnimateStateChanged(Animation animation, int state) {
					if(IViewStateListener.STATE_ANIM_ON_START == state){
						String endSpk = "";
						String pager = command;
						if (command.contains("翻")) {
							pager = NativeData.getResString("RS_SELECTOR_SELECT_PAGE").replace("%CMD%", command);
						} else {
							pager = NativeData.getResPlaceholderString("RS_SELECTOR_SELECT", "%CMD%", command);
						}
						
						if (!bSucc) {
							String slot = "";
							if (isNext) {
								slot = NativeData.getResString("RS_SELECTOR_THE_LAST");
							} else {
								slot = NativeData.getResString("RS_SELECTOR_THE_FIRST");
							}
							
							endSpk = NativeData.getResString("RS_SELECTOR_PAGE_COMMAND").replace("%NUM%", slot);
						}
						
						String spk = bSucc ? pager : endSpk;
						selectSpeech(spk);
						WinManager.getInstance().removeViewStateListener(this);
					}
					super.onAnimateStateChanged(animation, state);
				}
			});
			return;
		}
		String endSpk = "";
		String pager = command;
		if (command.contains("翻")) {
			pager = NativeData.getResString("RS_SELECTOR_SELECT_PAGE").replace("%CMD%", command);
		} else {
			pager = NativeData.getResPlaceholderString("RS_SELECTOR_SELECT", "%CMD%", command);
		}
		
		if (!bSucc) {
			String slot = "";
			if (isNext) {
				slot = NativeData.getResString("RS_SELECTOR_THE_LAST");
			} else {
				slot = NativeData.getResString("RS_SELECTOR_THE_FIRST");
			}

			endSpk = NativeData.getResString("RS_SELECTOR_PAGE_COMMAND").replace("%NUM%", slot);
		}

		String spk = bSucc ? pager : endSpk;
		selectSpeech(spk);
	}

	public void selectSpeech(String spk) {
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		mSpeechTaskId = TtsManager.getInstance().speakVoice(
				spk,TtsManager.BEEP_VOICE_URL,
				new TtsUtil.ITtsCallback() {

					@Override
					public void onSuccess() {
						AsrManager.getInstance().mSenceRepeateCount = 0;

						JNIHelper.logd("call select SenceRepeateCount: " + AsrManager.getInstance().mSenceRepeateCount);
						if (ProjectCfg.mCoexistAsrAndWakeup && !InterruptTts.getInstance().isInterruptTTS()) {
							AsrManager.getInstance().mSenceRepeateCount++;
							if (AsrManager.getInstance().mSenceRepeateCount < AsrManager.ASR_SENCE_REPEATE_COUNT) {
								AsrManager.getInstance().start(mAsrOptionSelectAgain);
							}
						}
						RecorderWin.refreshState(RecorderWin.RECORD_RECORNIZE);
						if (mUseAutoPerform && mDelayTask != null) {
							if (!isPauseProgress) {
								mDelayTask.continueProgress();
							}
							isPauseProgress = false;
						}
					}
					@Override
					public boolean isNeedStartAsr() {
						return true;
					}
				});
	}

	public void speakWithTips(String spk) {
//		if (ProjectCfg.mCoexistAsrAndWakeup) {
//			//在切换上一页下一页之后取消的之前的识别
//			AsrManager.getInstance().cancel();
//		}
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.getInstance().speakVoice(spk, InterruptTts.getInstance().isInterruptTTS()?"":mVoiceUrl,
				new TtsUtil.ITtsCallback() {
					@Override
					public void onSuccess() {
						AsrManager.getInstance().mSenceRepeateCount = 0;

						JNIHelper.logd("call select SenceRepeateCount: " + AsrManager.getInstance().mSenceRepeateCount);
						if (ProjectCfg.mCoexistAsrAndWakeup && !InterruptTts.getInstance().isInterruptTTS()) {
							AsrManager.getInstance().mSenceRepeateCount++;
							if (AsrManager.getInstance().mSenceRepeateCount < AsrManager.ASR_SENCE_REPEATE_COUNT) {
								AsrManager.getInstance().start(mAsrOptionSelectAgain);
							}
						}
					}
					@Override
					public boolean isNeedStartAsr() {
						return true;
					}
				});
	}

	public void setOnItemSelectListener(OnItemSelectListener listener) {
		mOnItemSelectListener = listener;
	}

	private void updatePageHelper() {
		if (mPageHelper == null) {
			mPageHelper = new PageHelper(mSourceList, mPageCount, mUseNewSelector);
			mPageHelper.setOnListChangeListener(new OnListChangeListener() {

				@Override
				public void onListChange(List list) {
					onSelectorListChange(list);
				}
			});
		}
		mPageHelper.mUseNewSelector = mUseNewSelector;
		mPageHelper.onUpdate(mSourceList, mPageCount, mUseNewSelector);
	}

	private void updateDelayTask() {
		if (mDelayTask == null) {
			mDelayTask = new DelayTask(new Runnable() {

				@Override
				public void run() {
					if (mUseAutoPerform) {
						// TODO:
						mDelayTask = null;
						selectIndexFromPage(0, null);
					}
				}
			}, getProgressDelay());
		}

		mDelayTask.clearProgress();
	}

	protected void onSelectorListChange(List tmp) {
		if (tmp == null || tmp.size() == 0) {
			boolean addMsg = false;
			if (WinManager.getInstance().hasThirdImpl()) {
				addMsg = true;
			}
			if (addMsg) {
				RecorderWin.addSystemMsg(getBeginSelectorHint());
				return;
			}
		}

		mTmpList = tmp;
		onSrcListUpdate(mTmpList);
		if (!isSelecting()) {
			return;
		}

		useWakeupSelector(false);
	}

	protected boolean isDelayAddWkWords() {
		return false;
	}

	/**
	 * 获取TTS文本
	 * 
	 * @return
	 */
	protected abstract String getBeginSelectorHint();

	/**
	 * 发送更新后的列表
	 */
	protected abstract void onSrcListUpdate(List tmp);

	/**
	 * 任务ID号
	 * 
	 * @return
	 */
	protected abstract String getAsrTaskId();

	/**
	 * 识别场景
	 * 
	 * @return
	 */
	protected abstract int getSenceGrammar();

	/**
	 * 选中的对象
	 * 
	 * @param obj
	 */
	protected abstract void onItemSelect(Object obj, int index, String fromVoice);

	/**
	 * 通过语音选择的条目
	 * 
	 * @param tmp
	 * @param index
	 * @param speech
	 */
	protected abstract void onCommandSelect(List tmp, int index, String speech);

	/**
	 * @param acsc
	 */
	protected abstract void onAsrComplexSelect(AsrComplexSelectCallback acsc);

	/**
	 * 唤醒识别处理
	 * 
	 * @param isWakeupResult
	 * @param type
	 * @param command
	 * @return
	 */
	protected abstract boolean onWakeupItemSelect(boolean isWakeupResult, String type, String command);

	/**
	 * 唤醒识别索引处理
	 * 
	 * @param isWakeupResult
	 * @param indexs
	 * @param command
	 * @return
	 */
	protected abstract boolean onWakeupIndexSelect(boolean isWakeupResult, List<Integer> indexs, String command);

	protected long getProgressDelay() {
		return 4000;
	}

	public void clearIsSelecting() {
		JNIHelper.logd("IsSelecting clearIsSelecting:" + mIsSelecting);
		if (!mIsSelecting) {
			TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
			mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
			return;
		}
		
		clearFirstSelect();

		mIsSelecting = false;
		AppLogic.removeUiGroundCallback(mTaskAddCmds);
		WinManager.getInstance().removeAllViewStateListener();
		WakeupManager.getInstance().setWakeupBeginTime(0);
		stopTtsAndAsr();
		endWakeupSelect();
		removeDismissTask();
		if (!mHasPopGrammar) {
			mHasPopGrammar = true;
			AsrManager.getInstance().popGrammarId(getSenceGrammar());
		}

		WinRecordCycler.getInstance().clearAsrComplexSelectCallback(getAsrTaskId());
	}
	
	// 选中后就清除，不等重新选择
	protected void clearFirstSelect(){
		mCanAutoPerform = false;
		mUseAutoPerform = false;
		if (mDelayTask != null) {
			mDelayTask.clearProgress();
			RecorderWin.hideProgressBar(0);
		}
		
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		AsrManager.getInstance().cancel();
		TextResultHandle.getInstance().cancel();
	}

	public void removeDismissTask() {
		AppLogic.removeBackGroundCallback(mDismissTask);
	}

	public void checkDismissTask() {
		JNIHelper.logd("start checkDismissTask");
		AppLogic.removeBackGroundCallback(mDismissTask);
		if (sDismissDelay > 1000) {
			AppLogic.runOnBackGround(mDismissTask, sDismissDelay);
		}
	}

	Runnable mDismissTask = new Runnable() {

		@Override
		public void run() {
			selectWithCancel(null);
		}
	};

	public boolean isSelecting() {
		JNIHelper.logd("isSelecting:" + mIsSelecting);
		return mIsSelecting;
	}

	public void stopTtsAndAsr() {
		JNIHelper.logd("stopTtsAndAsr");
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		AsrManager.getInstance().cancel();
		TextResultHandle.getInstance().cancel();

		if (!mHasPopGrammar) {
			mHasPopGrammar = true;
			AsrManager.getInstance().popGrammarId(getSenceGrammar());
		}
	}

	public void endWakeupSelect() {
		WakeupManager.getInstance().recoverWakeupFromAsr(getAsrTaskId());
		RecorderWin.refreshState(RecorderWin.STATE_NORMAL);
		// AsrManager.getInstance().cancelIFlyOnlineOnly();
	}

	public void selectSure(boolean fromVoice) {
		JNIHelper.logd("selectSure:" + mUseAutoPerform);
		if (!mUseAutoPerform) {
			return;
		}
		selectIndexFromPage(0, fromVoice ? "" : null);
	}

	public void selectWithCancel(String fromVoice) {
		if (mExitWithBack) {
			selectCancel(fromVoice != null);
			return;
		}

		if (!TextUtils.isEmpty(fromVoice)) {
			RecorderWin.setLastUserText(fromVoice);
			GlobalContext.get().sendBroadcast(
					new Intent("com.txznet.txz.select.cancel"));
		}
		backAsrWithCancel(ASR_CANCEL_BACK_HINT);
	}

	public void selectSureDirect() {
		selectIndexFromPage(0, "");
	}

	public boolean selectIndexFromPage(int index, String fromVoice) {
		JNIHelper.logd("selectIndexFromPage:" + index);
		return selectIndexFromList(mTmpList, index, fromVoice);
	}

	public boolean selectIndexFromAll(int index, String fromVoice) {
		JNIHelper.logd("selectIndexFromAll:" + index);
		return selectIndexFromList(mSourceList, index, fromVoice);
	}

	private boolean selectIndexFromList(List src, int index, String fromVoice) {
		JNIHelper.logd("selectIndexFromPage:" + index);
		if (src == null || src.size() < 1) {
			JNIHelper.logw("selectIndex src is empty");
			return false;
		}

		if (index < 0 || index >= src.size()) {
			return false;
		}

		if (!mIsSelecting) {
			return false;
		}
		
		if (!mUseNewSelector) {
			clearIsSelecting();
		}else {
			clearFirstSelect();
		}
//		clearIsSelecting();

		final Object obj = src.get(index);

		onItemSelect(obj, index, fromVoice);
		return true;
	}

	public void selectCancel(boolean fromVoice) {
		LogUtil.logd("selectCancel:" + fromVoice);
		if (fromVoice)
			TtsManager.getInstance().speakText(ASR_CANCEL_HINT);
		clearIsSelecting();
		RecorderWin.dismiss();
	}

	public void backAsrWithCancel(String hint) {
		clearIsSelecting();
		RecorderWin.open(hint);
	}

	public void updatePageCount(int count) {
		if (count <= 0) {
			JNIHelper.logw("pageCount shouldn't 0");
			return;
		}

		JNIHelper.logd("updatePageCount:" + count);
		mPageCount = count;
		if (isSelecting()) {
			updatePageHelper();
		}
	}

	public byte[] procInvoke(String packageName, String command, byte[] data) {
		if ("txz.record.ui.event.list.ontouch".equals(command)) {
			if (mUseAutoPerform && mDelayTask != null) {
				mDelayTask.clearProgress();
				mUseAutoPerform = false;
			}
			checkDismissTask();
		}
		return null;
	}

	public static interface OnItemSelectListener {
		public void onItemSelect(List srcList, int index, Object obj);
	}
}
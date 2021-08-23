package com.txznet.txz.component.selector;

import java.util.List;

import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.android.volley.NetworkError;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.selector.PagerAdapter.OnListChangeListener;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecordInvokeFactory;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.WinRecordCycler;

import android.text.TextUtils;

public abstract class ISelectControl {
	private static final String REGISTER_WAKEUP = "registerWakeup";
	private static final String COMMAND_SELECT = "commSelect";
	private static final String INDEX_SELECT = "indexSelect";
	
	public static final String PREFIX = NativeData.getResString("RS_SELECTOR_SELECT");
	public static final String ASR_CANCEL_HINT = NativeData.getResString("RS_SELECTOR_OPERATION_CANCEL");
	public static final String ASR_CANCEL_BACK_HINT = NativeData.getResString("RS_SELECTOR_HELP");

	protected volatile boolean mIsSelecting = false;
	protected final boolean mUseAutoPerform = false;
	protected boolean mHasPopGrammar = true;
	protected boolean mExitWithBack = false;
	protected boolean mNeedPlayWavTip = true;
	protected boolean mUseNewSelector = false;
	protected boolean mCanAutoPerform;

	protected int mPageCount = 4;
	protected int mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;

	protected String mLastHintTxt = "";
	protected String mVoiceUrl = TtsManager.BEEP_VOICE_URL;

	protected List mTmpList;
	protected List mSourceList;

	protected DelayTask mDelayTask;
	protected PageHelper mPageHelper;
	protected OnItemSelectListener mOnItemSelectListener;

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

//		final String[] spks = filterKeywords(mLastHintTxt);
//		if (spks.length > 1) {
//			mSpeechTaskId = TtsManager.getInstance().speakText(spks[0], new TtsUtil.ITtsCallback() {
//				public void onSuccess() {
//					speakwords(spks[1], PreemptType.PREEMPT_TYPE_IMMEADIATELY, true);
//				}
//			});
//			return;
//		}

		speakwords(mLastHintTxt, PreemptType.PREEMPT_TYPE_NEXT, false);
	}
	
	private String[] filterKeywords(String kws) {
		if (kws.contains("确定还是取消")) {
			String[] tmp = kws.split("确定还是取消");
			if (tmp.length == 1) {
				if (tmp[0].equals("确定还是取消")) {
					return new String[] { "", tmp[0] };
				}
				return new String[] { tmp[0], "确定还是取消" };
			}
			
			return new String[] { "", kws };
		}

		return new String[] { kws };
	}
	
	private void speakwords(String words, PreemptType type, boolean stopWakeup) {
		if (stopWakeup) {
			TtsManager.getInstance().speakTextNoWakeup(words, type, new TtsUtil.ITtsCallback() {

				@Override
				public void onSuccess() {
					AsrManager.getInstance().mSenceRepeateCount = 0;
					if (!isSelecting()) {
						AsrManager.getInstance().start();
						return;
					}

					mCanAutoPerform = true;
					if (mUseAutoPerform) {
						mDelayTask.continueProgress();
					}

					checkDismissTask();
				}
			});
			return;
		}

		mSpeechTaskId = TtsManager.getInstance().speakVoice(words, mVoiceUrl, type, new TtsUtil.ITtsCallback() {

			@Override
			public void onSuccess() {
				AsrManager.getInstance().mSenceRepeateCount = 0;
				if (!isSelecting()) {
					AsrManager.getInstance().start();
					return;
				}

				mCanAutoPerform = true;
				if (mUseAutoPerform) {
					mDelayTask.continueProgress();
				}

				checkDismissTask();
			}
		});
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

		// 更新页码器
		updatePageHelper();
		// 更新到时计数器
		updateDelayTask();
	}

	AsrOption mAsrOptionSelectAgain = new AsrOption().setManual(false).setNeedStopWakeup(false);

	public void selectAgain() {
		JNIHelper.logd("selectAgain IsSelecting:" + mIsSelecting);
		if (!mIsSelecting) {
			return;
		}

		if (ProjectCfg.mCoexistAsrAndWakeup && AsrManager.getInstance().mSenceRepeateCount >= 0) {
			AsrManager.getInstance().mSenceRepeateCount++;
			JNIHelper.logd("call select SenceRepeateCount: " + AsrManager.getInstance().mSenceRepeateCount);
			if (AsrManager.getInstance().mSenceRepeateCount < AsrManager.ASR_SENCE_REPEATE_COUNT) {
				AsrManager.getInstance().start(mAsrOptionSelectAgain);
				useWakeupSelector(false);
				return;
			}
		}

		useWakeupSelector(true);
	}
	
	private byte[] genJosn(String action,String type,String speech){
		JSONBuilder jb = new JSONBuilder();
		jb.put("sence", "selector");
		if (!TextUtils.isEmpty(type)) {
			jb.put("type", type);
		}
		if (!TextUtils.isEmpty(speech)) {
			jb.put("speech", speech);
		}
		jb.put("action", action);
		return jb.toBytes();
	}

	private void useWakeupSelector(boolean playWavTips) {
		if (SenceManager.getInstance().noneedProcSence("selector", genJosn(REGISTER_WAKEUP, "", ""))) {
			return;
		}
		
		if (mSourceList.size() == 0) {
			JNIHelper.logd("useWakeupSelector is null");
			return;
		}

		AsrComplexSelectCallback acsc = new AsrComplexSelectCallback() {

			@Override
			public boolean needAsrState() {
				return true;
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
						mDelayTask.pauseProgress();
					}
				}
			}

			@Override
			public void onSpeechEnd() {
				super.onSpeechEnd();
				if (mUseAutoPerform && mCanAutoPerform) {
					if (null != mDelayTask) {
						mDelayTask.continueProgress();
					}
				}
			}

			@Override
			public void onCommandSelected(final String type, final String command) {
				if (SenceManager.getInstance().noneedProcSence("selector", genJosn(COMMAND_SELECT, type, command))) {
					return;
				}
				
				if (mUseAutoPerform && mDelayTask != null) {
					mDelayTask.clearProgress();
				}
				checkDismissTask();

				if (onWakeupItemSelect(isWakeupResult(), type, command)) {
					return;
				}

				if ("PRE_PAGER".equals(type)) {
					boolean prev = mPageHelper.prevPager();
					snapPager(false, prev,command);
				}

				if ("NEXT_PAGER".equals(type)) {
					boolean next = mPageHelper.nextPager();
					snapPager(true, next, command);
				}
				if ("SURE".equals(type)) {
					selectSure(false);
				}

				if ("CANCEL".equals(type)) {
					if (mExitWithBack) {
						selectCancel(true);
						return;
					}

					RecorderWin.setLastUserText(command);
					backAsrWithCancel(ASR_CANCEL_BACK_HINT);
				}
			}

			@Override
			public void onIndexSelected(final List<Integer> indexs, final String command) {
				String prefix = "SELECT_";
				String suffix = "" + indexs.get(0);
				if (command.equals("最上面那个")) {
					suffix = "FIRST";
				} else if (command.equals("最下面那个")) {
					suffix = "LAST";
				}
				if (SenceManager.getInstance().noneedProcSence("selector",
						genJosn(INDEX_SELECT, prefix + suffix, command))) {
					return;
				}
				
				ISelectControl.this.onIndexSelected(isWakeupResult(), indexs, command);
			}
		};

		acsc.addCommand("CANCEL", "取消", "返回", "放弃", "关闭", "再见", "取消取消", "返回返回", "放弃放弃", "关闭关闭", "再见再见");
		if (mSourceList.size() > mPageCount) {
			acsc.addCommand("PRE_PAGER", "上一页", "上翻", "上翻上翻");
			acsc.addCommand("NEXT_PAGER", "下一页", "下翻", "下翻下翻");
		}
		if (mSourceList.size() == 1) {
			acsc.addCommand("SURE", "确定", "确定确定");
		}

		int pageCount = mSourceList.size() / mPageCount;
		if (mSourceList.size() % mPageCount != 0) {
			pageCount++;
		}

		if (mUseNewSelector) {
			for (int i = 1; i <= pageCount; i++) {
				String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i);
				acsc.addIndex(i, "第" + strIndex + "页");
			}
		}

		for (int i = 0; i < mTmpList.size(); i++) {
			String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i + 1);
			acsc.addIndex(i, "第" + strIndex + "个", "第" + strIndex + "条");
		}

		if (mTmpList.size() == 2) {
			acsc.addIndex(0, "上面那个", "前面那个");
			acsc.addIndex(1, "下面那个", "后面那个");
		} else if (mTmpList.size() > 2) {
			acsc.addIndex(0, "最上面那个");
			acsc.addIndex(mTmpList.size() - 1, "最下面那个");
		}

		onAsrComplexSelect(acsc);
		WakeupManager.getInstance().useWakeupAsAsr(acsc);
		if (playWavTips) {
			WakeupManager.getInstance().playAsrTipSound();
		}

		WinRecordCycler.getInstance().addAsrComplexSelectCallback(acsc, getAsrTaskId());
	}
	
	private void onIndexSelected(boolean fromWakeup, final List<Integer> indexs, final String command) {
		if (mUseAutoPerform && mDelayTask != null) {
			mDelayTask.clearProgress();
		}
		checkDismissTask();

		do {
			if (indexs.size() != 1) {
				// 一般不会走到这里
				break;
			}

			if (command.startsWith("第") 
					&& command.endsWith("页")) {
				int index = indexs.get(0);
				selectSpeech(PREFIX + command);
				mPageHelper.onPageSelect(index);
				return;
			}

			// 反注册唤醒词
			clearIsSelecting();

			int index = (indexs!=null&&indexs.size()>0)?indexs.get(0):0;
//			int index =0;
			if (command.startsWith("第") 
					&& ((command.endsWith("个") 
					|| command.endsWith("条")))) {
				index = indexs.get(0);
			}

			if (command.equals("上面那个") 
					|| command.equals("前面那个") 
					|| command.equals("最上面那个")) {
				index = 0;
			}
			
			if (command.equals("下面那个") 
					|| command.equals("后面那个") 
					|| command.equals("最下面那个")) {
				index = mTmpList.size() - 1;
			}
			
			if (mTmpList.size() > index) {
				onCommandSelect(mTmpList, index, command);
				return;
			}
		} while (false);

		if (onWakeupIndexSelect(fromWakeup, indexs, command)) {
			return;
		}
	}
	
	private void snapPager(boolean isNext, boolean bSucc, String command) {
		if (!mUseNewSelector) {
			RecordInvokeFactory.getAdapter().snapPager(isNext);
			return;
		}

		String endSpk = "";
		String pager = command;
		if (command.contains("翻")) {
			pager = "已为您" + command;
		} else {
			pager = PREFIX + command;
		}
		
		if (!bSucc) {
			String slot = "";
			if (isNext) {
				slot = NativeData.getResString("RS_SELECTOR_THE_LAST");
			} else {
				slot = NativeData.getResString("RS_SELECTOR_THE_FIRST");
			}

			endSpk = NativeData.getResString("RS_SELECTOR_PAGE_COMMAND").replace("%CMD%", slot);
			//endSpk = endSpk.replace("%SLOT%", slot);
		}

		String spk = bSucc ? pager : endSpk;
		selectSpeech(spk);
	}

	public void selectSpeech(String spk) {
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		mSpeechTaskId = TtsManager.getInstance().speakText(spk, new TtsUtil.ITtsCallback() {

			@Override
			public void onSuccess() {
				RecorderWin.refreshState(RecorderWin.RECORD_RECORNIZE);
				if (mUseAutoPerform && mDelayTask != null) {
					mDelayTask.continueProgress();
				}
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
						selectIndexFromPage(mPageHelper.getCurPager()
								* mPageHelper.getPagerCount(), null);
					}
				}
			}, getProgressDelay());
		}

		mDelayTask.clearProgress();
	}

	protected void onSelectorListChange(List tmp) {
		if (tmp == null || tmp.size() == 0) {
			boolean addMsg = false;
			if ((this instanceof PoiSelectorControl) == false) {
				addMsg = true;
			}
			if (RecordInvokeFactory.hasThirdImpl()) {
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

		mIsSelecting = false;
		mCanAutoPerform = false;
		if (mDelayTask != null) {
			mDelayTask.clearProgress();
			RecorderWin.hideProgressBar(0);
		}

		stopTtsAndAsr();
		endWakeupSelect();
		releaseAsrGlobalCallback();
		removeDismissTask();
		if (!mHasPopGrammar) {
			mHasPopGrammar = true;
			AsrManager.getInstance().popGrammarId(getSenceGrammar());
		}
	}

	public void removeDismissTask() {
		AppLogic.removeBackGroundCallback(mDismissTask);
	}

	public void checkDismissTask() {
		JNIHelper.logd("start checkDismissTask");
		AppLogic.removeBackGroundCallback(mDismissTask);
		if (SelectorHelper.sDismissDelay > 1000) {
			AppLogic.runOnBackGround(mDismissTask, SelectorHelper.sDismissDelay);
		}
	}

	Runnable mDismissTask = new Runnable() {

		@Override
		public void run() {
			if (mExitWithBack) {
				selectCancel(true);
				return;
			}
			
			backAsrWithCancel(ASR_CANCEL_BACK_HINT);
		}
	};

	public void releaseAsrGlobalCallback() {
		WinRecordCycler.getInstance().clearAsrComplexSelectCallback(getAsrTaskId());
	}

	public boolean isSelecting() {
		JNIHelper.logd("isSelecting:" + mIsSelecting);
		return mIsSelecting;
	}

	public void stopTtsAndAsr() {
		JNIHelper.logd("stopTtsAndAsr");
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		AsrManager.getInstance().cancel();
	}

	public void endWakeupSelect() {
		WakeupManager.getInstance().recoverWakeupFromAsr(getAsrTaskId());
		RecorderWin.refreshState(RecorderWin.STATE_NORMAL);
		AsrManager.getInstance().cancelIFlyOnlineOnly();
	}

	public void selectSure(boolean fromVoice) {
		JNIHelper.logd("selectSure:" + mUseAutoPerform);
		if (!mUseAutoPerform) {
			return;
		}
		selectIndexFromPage(0, fromVoice ? "" : null);
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
		clearIsSelecting();

		final Object obj = src.get(index);

		// TODO 去掉了选择反馈，看是否在实现类写
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
		JNIHelper.logd("updatePageCount:" + count);
		mPageCount = count;
		updatePageHelper();
	}

	public byte[] procInvoke(String packageName, String command, byte[] data) {
		if ("txz.record.ui.event.list.ontouch".equals(command)) {
			if (mUseAutoPerform && mDelayTask != null) {
				mDelayTask.clearProgress();
			}
			checkDismissTask();
		}
		return null;
	}

	public static interface OnItemSelectListener {
		public void onItemSelect(List srcList, int index, Object obj);
	}
}
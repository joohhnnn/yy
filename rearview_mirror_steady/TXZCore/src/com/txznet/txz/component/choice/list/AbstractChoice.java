package com.txznet.txz.component.choice.list;

import java.util.List;

import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.record.ui.WinRecord;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.choice.IChoice;
import com.txznet.txz.component.choice.IReport;
import com.txznet.txz.component.choice.OnItemSelectListener;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.component.choice.page.ResourcePage.OnGetDataCallback;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.advertising.AdvertisingManager;
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

import android.os.SystemClock;
import android.text.TextUtils;
import android.view.animation.Animation;

/**
 * 做列表数据分页显示，插词和选择
 * 
 * @param <T>
 * @param <E>
 */
public abstract class AbstractChoice<T, E> implements IChoice<T>, IReport {
	public static final String ACTION_DELETE = "delete";
	
	private static final String REGISTER_WAKEUP = "registerWakeup";
	private static final String COMMAND_SELECT = "commSelect";
	private static final String INDEX_SELECT = "indexSelect";
	
	// unknow
	public static final int SELECT_TYPE_UNKNOW = 0;
	// 点击
	public static final int SELECT_TYPE_CLICK = 1;
	// 声控
	public static final int SELECT_TYPE_VOICE = 2;
	// 方控
	public static final int SELECT_TYPE_PARTY_CONTROL = 3;
	// 返回键
	public static final int SELECT_TYPE_BACK = 4;
	// 倒计时
	public static final int SELECT_TYPE_COUNT_DOWN = 5;
	// 超时
	public static final int SELECT_TYPE_OVERTIME = 6;
	// 业务需求
	public static final int SELECT_TYPE_BUSNIESS = 7;

	protected boolean mIsSelecting;
	protected T mData;
	protected ResourcePage<T, E> mPage;
	protected OnItemSelectListener<E> mItemListener;
	private boolean mHasPopGrammar = true;

	private OnGetDataCallback<T> mDataCallback = new OnGetDataCallback<T>() {

		@Override
		public void onGetData(T res) {
			updateDisplay(res);
			useWakeupAsrTask();
		}
	};

	@Override
	public void showChoices(T data) {
		clearIsSelecting();
		this.mData = data;

		RecorderWin.show();
		RecorderWin.showUserText();
		mIsSelecting = true;
		mPage = createPage(data);
		if (mPage != null) {
			mPage.requestCurrPage(mDataCallback);
		}
		if (mHasPopGrammar) {
			mHasPopGrammar = false;
			AsrManager.getInstance().pushGrammarId(getSenceGrammar());
		}
		AdvertisingManager.getInstance().clearAdvertising();
	}

	/**
	 * 重新刷新列表
	 * 
	 * @param data
	 */
	public final void refreshData(T data) {
		mPage = null;
		mPage = createPage(data);
		if(mPage != null){
			mPage.requestCurrPage(mDataCallback);
		}
	}

	/**
	 * 重置选择器数据
	 */
	protected void resetPage() {
		if (mPage != null) {
			mPage.reset();
		}
	}

	/**
	 * 获取分页器
	 * 
	 * @return
	 */
	public ResourcePage<T, E> getPageControl() {
		return this.mPage;
	}

	protected void clearPage() {
		if (mPage != null) {
			mPage.clearPage();
		}
		mPage = null;
	}

	public void selectSure(String fromVoice) {
		if (needSureCmd()) {
			selectIndex(0, fromVoice);
			return;
		}
		LogUtil.logw("selectSure fail！");
	}

	public void cancelWithClose() {
		clearIsSelecting();
		RecorderWin.close();
	}

	public void selectCancel(int selectType, String fromVoice) {
		clearIsSelecting();

		String spk = NativeData.getResString("RS_SELECTOR_HELP");
		if (selectType == SELECT_TYPE_OVERTIME) {
			spk = NativeData.getResString("RS_SELECTOR_TIMEOUT");
		}
		if (!TextUtils.isEmpty(fromVoice)) {
			RecorderWin.setLastUserText(fromVoice);
		}
		RecorderWin.showUserText();
		RecorderWin.open(spk);
	}

	public boolean nextPage(String fromVoice) {
		boolean bSucc = mPage != null ? mPage.nextPage() : false;
		if (bSucc) {
			refreshCurrPage();
		}
		onSnapPager(true, bSucc, fromVoice);
		return bSucc;
	}

	public boolean lastPage(String fromVoice) {
		boolean bSucc = mPage != null ? mPage.lastPage() : false;
		if (bSucc) {
			refreshCurrPage();
		}
		onSnapPager(false, bSucc, fromVoice);
		return bSucc;
	}

	/**
	 * 更新当前页数据
	 */
	public void refreshCurrPage() {
		if (mPage != null) {
			mPage.requestCurrPage(mDataCallback);
		}
	}

	protected void onSnapPager(final boolean isNext, final boolean bSucc, final String command) {
		if (TextUtils.isEmpty(command)) {
			// 手动翻页不播报
			return;
		}
		if (isDelayAddWkWords() && bSucc) {
			WinManager.getInstance().addViewStateListener(new IViewStateListener() {
				@Override
				public void onAnimateStateChanged(Animation animation, int state) {
					if (IViewStateListener.STATE_ANIM_ON_START == state) {
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

	public boolean selectPage(int page, String fromVoice) {
		boolean bSucc = mPage != null ? mPage.selectPage(page) : false;
		if (bSucc) {
			refreshCurrPage();
		}
		if (!TextUtils.isEmpty(fromVoice)) {
			selectSpeech(NativeData.getResPlaceholderString("RS_SELECTOR_SELECT", "%CMD%", fromVoice));
		}
		return bSucc;
	}

	/**
	 * 从当前页选择某一个索引
	 * 
	 * @param idx
	 * @param fromVoice
	 */
	public void selectIndex(int idx, String fromVoice) {
		selectIndex(true, idx, fromVoice);
	}

	/**
	 * 从所有数据中选择某一个索引
	 * 
	 * @param idx
	 * @param fromVoice
	 */
	public void selectAllIndex(int idx, String fromVoice) {
		selectIndex(false, idx, fromVoice);
	}

	protected void selectIndex(boolean isPage, int idx, String fromVoice) {
		E targetObj = null;
		if (isPage) {
			targetObj = mPage.getItemFromCurrPage(idx);
		} else {
			targetObj = mPage.getItemFromSource(idx);
		}
				
		// 开了v3防误唤醒，POI选择完后的tts播报时，不响应“第X个”
		if (!ProjectCfg.getAECPreventFalseWakeup()) {
			clearFirstSelecting();
		} else {
			clearIsSelecting();
		}
		
		LogUtil.logd("selectIndex isPage:" + isPage + ",idx:" + idx + ",fromVoice:" + fromVoice);
		if (fromVoice != null) {
			RecorderWin.refreshItemSelect(idx);
		}
		onItemSelect(targetObj, isPage, idx, fromVoice);

		if (mItemListener != null && mItemListener.onItemSelected(true, targetObj, isPage, idx, fromVoice)) {
			return;
		}

		onSelectIndex(targetObj, isPage, idx, fromVoice);
	}
	
	public void selectIdxWithAction(int idx, boolean curPage, String action) {
		if (action != null) {
			if (ACTION_DELETE.equals(action)) {
				notifyDeleteItem(idx, curPage);
			}
		}
	}
	
	protected void notifyDeleteItem(int idx, boolean curPage) {
		E b = null;
		if (mPage != null) {
			b = mPage.notifyRemoveIdx(idx, curPage);
		}
		if (b != null) {
			onNotifyItemDeleted(b);
		}
	}
	
	protected void onNotifyItemDeleted(E e) {
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				refreshCurrPage();
			}
		}, 200);
	}

	public boolean notifySelectSuccess(E targetObj, boolean isPage, int idx, String fromVoice) {
		if (mItemListener != null && mItemListener.onItemSelected(false, targetObj, isPage, idx, fromVoice)) {
			return true;
		}
		return false;
	}
	
	protected abstract void onItemSelect(E item, boolean isFromPage, int idx, String fromVoice);
	
	protected abstract void onSelectIndex(E item, boolean isFromPage, int idx, String fromVoice);

	protected void updateDisplay(T data) {
		if (mPage == null) {
			return;
		}
		String sData = convToJson(data).toString();
		LogUtil.logd("send data:" + sData);
		// 发送到界面
		RecorderWin.sendSelectorList(sData);
	}

	public static int mSpeechTaskId;

	protected AsrOption createSelectAgainAsrOption() {
		AsrOption op = new AsrOption().setManual(false).setNeedStopWakeup(false).setPlayBeepSound(false);
		op.mTtsId = TtsManager.getInstance().getCurTaskId();
		op.mGrammar = getSenceGrammar();
		return op;
	}

	public void selectAgain() {
		JNIHelper.logd("selectAgain IsSelecting:" + mIsSelecting);
		if (!isSelecting()) {
			return;
		}

		JNIHelper.logd("call select SenceRepeateCount: " + AsrManager.getInstance().mSenceRepeateCount);
		if (isCoexistAsrAndWakeup() && AsrManager.getInstance().mSenceRepeateCount != -1
				&& !InterruptTts.getInstance().isInterruptTTS()) {
			AsrManager.getInstance().mSenceRepeateCount++;
			if (AsrManager.getInstance().mSenceRepeateCount < AsrManager.ASR_SENCE_REPEATE_COUNT) {
				AsrManager.getInstance().start(createSelectAgainAsrOption());
			}
		}

		useWakeupAsrTask();
	}

	private static final int DELAY_MAX_ADD_CMDS = 3000;// 最晚3秒添加唤醒词，以避免异常情况下没有回调
	Runnable mTaskAddCmds = new Runnable() {
		@Override
		public void run() {
			if (!isSelecting()) {
				return;
			}
			addWakeupCmds(mPage.getResource());
		}
	};
	
	public boolean isCoexistAsrAndWakeup() {
		return ProjectCfg.mCoexistAsrAndWakeup;
	}

	protected boolean isDelayAddWkWords() {
		return false;
	}

	protected void useWakeupAsrTask() {
		if (SenceManager.getInstance().noneedProcSence("selector", genJosn(REGISTER_WAKEUP, "", ""))) {
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
						addWakeupCmds(mPage.getResource());
						WinManager.getInstance().removeViewStateListener(this);

					}
				}
			});
			AppLogic.removeBackGroundCallback(mTaskAddCmds);
			AppLogic.runOnBackGround(mTaskAddCmds, DELAY_MAX_ADD_CMDS);
		} else {
			addWakeupCmds(mPage.getResource());
		}
	}
	
	protected void activeWakeupAsr(AsrComplexSelectCallback acsc) {
		WakeupManager.getInstance().useWakeupAsAsr(acsc);
		WinRecordCycler.getInstance().addAsrComplexSelectCallback(acsc, getReportId());
	}

	protected void addWakeupCmds(T data) {
		AsrComplexSelectCallback acsc = new AsrComplexSelectCallback() {
			Runnable removeBackground = null;
			Runnable taskRunnable = null;
			private static final int speechDelay = 700;
			private static final int handleDelay = 800;
			private boolean isEnd = false;
			private long mLastSpeechEndTime = 0;

			@Override
			public boolean needAsrState() {
				if (InterruptTts.getInstance().isInterruptTTS()) {// 如果是识别模式，就不需要开启beep音
					return false;
				} else {
					return true;
				}
			}

			@Override
			public void onSpeechBegin() {
				AbstractChoice.this.onSpeechBegin();
			}

			@Override
			public void onSpeechEnd() {
				AbstractChoice.this.onSpeechEnd();
				mLastSpeechEndTime = SystemClock.elapsedRealtime();
				if (removeBackground != null) {
					AppLogic.removeBackGroundCallback(removeBackground);
				}
			}

			@Override
			public String getTaskId() {
				return getReportId();
			}

			@Override
			public void onCommandSelected(String type, String command) {
				checkUseAsr(new Runnable2<String, String>(type, command) {

					@Override
					public void run() {
						isEnd = true;
						if (InterruptTts.getInstance().isInterruptTTS() || isCoexistAsrAndWakeup()) {
                            //唤醒结果执行时，如果还在录音，则取消掉
                            if (AsrManager.getInstance().isBusy()) {
                                AsrManager.getInstance().cancel();
                            }
                        }
						AbstractChoice.this.commandSelect(mP1, mP2);
					}
				});
			}

			@Override
			public void onIndexSelected(List<Integer> indexs, String command) {
				checkUseAsr(new Runnable2<List<Integer>, String>(indexs, command) {

					@Override
					public void run() {
						isEnd = true;
						if (InterruptTts.getInstance().isInterruptTTS() || isCoexistAsrAndWakeup()) {
                            //唤醒结果执行时，如果还在录音，则取消掉
                            if (AsrManager.getInstance().isBusy()) {
                                AsrManager.getInstance().cancel();
                            }
                        }
						AbstractChoice.this.indexSelect(mP1, mP2);
					}
				});
			}

			private void checkUseAsr(Runnable run) {
				if (taskRunnable != null) {
					AppLogic.removeBackGroundCallback(taskRunnable);
					taskRunnable = null;
				}

				taskRunnable = run;
				removeBackground = new Runnable() {

					@Override
					public void run() {
						// AppLogic.removeBackGroundCallback(taskRunnable);
					}
				};

				if (InterruptTts.getInstance().isInterruptTTS() || isCoexistAsrAndWakeup()) {
					if (isWakeupResult()) {// 是唤醒的结果
						isEnd = false;
						// 判断唤醒的说话结束了
						if (SystemClock.elapsedRealtime() - mLastSpeechEndTime < 300) {
							AppLogic.runOnBackGround(taskRunnable, 0);
							AppLogic.removeBackGroundCallback(removeBackground);
						} else {
							AppLogic.runOnBackGround(removeBackground, speechDelay);
							AppLogic.runOnBackGround(taskRunnable, handleDelay);
						}
					} else if (!isEnd) {// 识别到的唤醒词并且唤醒没有执行完成
						AppLogic.runOnBackGround(taskRunnable, 0);
						AppLogic.removeBackGroundCallback(removeBackground);
					}
				} else {
					taskRunnable.run();
				}
			}
		};
		// 开始插词
		onAddWakeupAsrCmd(acsc, data);
		activeWakeupAsr(acsc);
	}

	/**
	 * @param acsc
	 * @param data
	 */
	protected void onAddWakeupAsrCmd(AsrComplexSelectCallback acsc, T data) {
		// 取消
		acsc.addCommand("CANCEL", NativeData.getResStringArray("RS_CMD_SELECT_CANCEL"));
		if (mPage.getMaxPage() > 1) {
			acsc.addCommand("PRE_PAGER", NativeData.getResStringArray("RS_CMD_SELECT_PRE"));
			acsc.addCommand("NEXT_PAGER", NativeData.getResStringArray("RS_CMD_SELECT_NEXT"));
		}
		if (needSureCmd()) {
			acsc.addCommand("SURE", NativeData.getResStringArray("RS_CMD_SELECT_SURE"));
		}

		if (is2_0Version()) {
			if (mPage.getMaxPage() > 1) {
				int i = 1;
				for (; i <= mPage.getMaxPage(); i++) {
					String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i);
					acsc.addCommand("PAGE_INDEX_" + i, "第" + strIndex + "页");
				}
				acsc.addCommand("PAGE_INDEX_" + (i - 1), "最后一页");
			}
		}

		final int currPageSize = mPage.getCurrPageSize();
		for (int i = 0; i < currPageSize; i++) {
			String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i + 1);
			if (i == 0) {
				acsc.addCommand("ITEM_INDEX_" + i, NativeData.getResStringArray("RS_CMD_SELECT_FIRST"));
			} else {
				acsc.addCommand("ITEM_INDEX_" + i, "第" + strIndex + "个", "第" + strIndex + "条");
			}
		}

		if (currPageSize == 2) {
			acsc.addCommand("AFRONT$", NativeData.getResStringArray("RS_CMD_SELECT_AFRONT"));
			acsc.addCommand("ABEHIND$", NativeData.getResStringArray("RS_CMD_SELECT_ABEHIND"));
		} else if (currPageSize > 2) {
			acsc.addCommand("MOST_AFRONT$", NativeData.getResStringArray("RS_CMD_SELECT_MOST_FRONT"));
			acsc.addCommand("MOST_ABEHIND$_" + (mPage.getCurrPageSize() - 1),
					NativeData.getResStringArray("RS_CMD_SELECT_MOST_BEHIND"));
		}
	}

	protected void commandSelect(String type, String command) {
		onPreWakeupSelect(command);
		if ("滴个".equals(command)) {
			command = "第一个";
		}

		MtjModule.getInstance().event(MtjModule.EVENTID_VOICE_MULTIPLE);
		if (SenceManager.getInstance().noneedProcSence("selector", genJosn(COMMAND_SELECT, type, command))) {
			return;
		}

		if (onCommandSelect(type, command)) {
			return;
		}

		if ("PRE_PAGER".equals(type)) {
			lastPage(command);
			return;
		}
		if ("NEXT_PAGER".equals(type)) {
			nextPage(command);
			return;
		}
		if ("SURE".equals(type)) {
			selectSure("");
			return;
		}
		if ("CANCEL".equals(type)) {
			selectCancel(SELECT_TYPE_VOICE, command);
			return;
		}
		if (type.startsWith("PAGE_INDEX_")) {
			int index = Integer.parseInt(type.substring("PAGE_INDEX_".length()));
			selectPage(index, command);
			return;
		}
		if (type.startsWith("ITEM_INDEX_")) {
			int index = Integer.parseInt(type.substring("ITEM_INDEX_".length()));
			selectIndex(index, command);
			return;
		}
		if (type.equals("AFRONT$")) {
			selectIndex(0, command);
			return;
		}
		if (type.equals("ABEHIND$")) {
			selectIndex(1, command);
			return;
		}
		if (type.equals("MOST_AFRONT$")) {
			if (SenceManager.getInstance().noneedProcSence("selector",
					genJosn(INDEX_SELECT, "SELECT_FIRST", command))) {

			}
			selectIndex(0, command);
			return;
		}
		if (type.startsWith("MOST_ABEHIND$_")) {
			if (SenceManager.getInstance().noneedProcSence("selector", genJosn(INDEX_SELECT, "SELECT_LAST", command))) {

			}
			int index = Integer.parseInt(type.substring("MOST_ABEHIND$_".length()));
			selectIndex(index, command);
			return;
		}
	}

	/**
	 * 场景外放
	 * 
	 * @param action
	 * @param type
	 * @param speech
	 * @return
	 */
	private byte[] genJosn(String action, String type, String speech) {
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

	public void registerListener(OnItemSelectListener<E> listener) {
		mItemListener = listener;
	}

	public void clearListener() {
		mItemListener = null;
	}

	protected void onPreWakeupSelect(String command) {
	}

	protected boolean onCommandSelect(String type, String command) {
		return false;
	}

	private void indexSelect(List<Integer> indexs, String command) {
		onPreWakeupSelect(command);
		if (onIndexSelect(indexs, command)) {
			return;
		}

		int index = (indexs != null && indexs.size() > 0) ? indexs.get(0) : 0;
		selectAllIndex(index, command);
	}

	protected boolean onIndexSelect(List<Integer> indexs, String command) {
		return false;
	}

	protected void onSpeechBegin() {

	}

	protected void onSpeechEnd() {

	}

	protected void selectSpeech(String spk) {
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		mSpeechTaskId = TtsManager.getInstance().speakVoice(spk, TtsManager.BEEP_VOICE_URL, new TtsUtil.ITtsCallback() {

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
				RecorderWin.refreshState(RecorderWin.RECORD_RECORNIZE);
			}

			@Override
			public boolean isNeedStartAsr() {
				return true;
			}
		});
	}

	public void speakWithTips(String spk) {
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.getInstance().speakVoice(spk,
				InterruptTts.getInstance().isInterruptTTS() ? "" : TtsManager.BEEP_VOICE_URL,
				new TtsUtil.ITtsCallback() {
					@Override
					public void onBegin() {
						super.onBegin();
						RecorderWin.refreshState(RecorderWin.STATE_NORMAL);
					}

					@Override
					public void onEnd() {
						super.onEnd();
						RecorderWin.refreshState(RecorderWin.STATE_RECORD_START);
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
					}

					@Override
					public boolean isNeedStartAsr() {
						return true;
					}
				});
	}

	@Override
	public boolean isSelecting() {
		LogUtil.logd("isSelecting:" + getReportId() + mIsSelecting);
		return mIsSelecting;
	}

	protected void clearFirstSelecting() {
		stopTtsAndAsr();
	}

	@Override
	public final void clearIsSelecting() {
		LogUtil.logd("clearIsSelecting:" + getReportId() + " isSelecting:" + isSelecting());
		if (!isSelecting()) {
			TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
			mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
			return;
		}
		mIsSelecting = false;
		resetPage();
		WinManager.getInstance().removeAllViewStateListener();
		WakeupManager.getInstance().setWakeupBeginTime(0);
		clearFirstSelecting();
		endWakeupSelect();

		onClearSelecting();
	}

	public void stopTtsAndAsr() {
		JNIHelper.logd("stopTtsAndAsr");
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		AsrManager.getInstance().cancel();
		TextResultHandle.getInstance().cancel();
	}

	public void endWakeupSelect() {
		WakeupManager.getInstance().recoverWakeupFromAsr(getReportId());
		WinRecordCycler.getInstance().clearAsrComplexSelectCallback(getReportId());
		RecorderWin.refreshState(RecorderWin.STATE_NORMAL);
		if (!mHasPopGrammar) {
			mHasPopGrammar = true;
			AsrManager.getInstance().popGrammarId(getSenceGrammar());
		}
	}

	protected int getSenceGrammar() {
		return VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL;
	}

	protected abstract JSONBuilder convToJson(T ts);

	protected abstract ResourcePage<T, E> createPage(T sources);

	protected void onClearSelecting() {
	}

	protected abstract boolean needSureCmd();

	protected boolean is2_0Version() {
		return true;
	}
}
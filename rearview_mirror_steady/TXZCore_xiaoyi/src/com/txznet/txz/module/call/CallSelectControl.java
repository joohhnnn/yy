package com.txznet.txz.module.call;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.txz.ui.contact.ContactData.MobileContact;
import com.txz.ui.contact.ContactData.MobileContacts;
import com.txz.ui.data.UiData;
import com.txz.ui.makecall.UiMakecall;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.Contact;
import com.txznet.txz.ui.win.record.RecorderWin.STATE;
import com.txznet.txz.util.CallUtil;
import com.txznet.txz.util.KeywordsParser;

import android.text.TextUtils;

public class CallSelectControl {
	static final String WAKEUP_TASK_ID = "CallSelectControl";

	static MobileContacts mMobileContacts;
	static int mSourceEvent;
	static final boolean mCanAutoCall = false;
	static boolean mCallMakeSureAsr = false;
	static int mGrammarSence = VoiceData.GRAMMAR_SENCE_DEFAULT;
	static int mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
	static int mListCount = 0;
	static float mProgress = 0;
	static final int TOTAL_AUTO_CALL_TIME = 4000;
	static final int SELECT_OUT_TIME = 30000;
	static final int AUTO_CALL_PERIOD = 100;
	public static String mLastHintTts = null;

	static Runnable mRunnableAddProgress = new Runnable() {
		@Override
		public void run() {
			AppLogic.removeBackGroundCallback(mRunnableAddProgress);
			if (mCanAutoCall == false)
				return;
			mProgress += (100.0 * AUTO_CALL_PERIOD / TOTAL_AUTO_CALL_TIME);
			RecorderWin.refreshProgressBar(Math.round(mProgress), 0);
			if (mProgress >= 100.0) {
				selectSure(false);
				return;
			}
			AppLogic.runOnBackGround(mRunnableAddProgress, AUTO_CALL_PERIOD);
		}
	};

	public static void continueProgress() {
		JNIHelper.logd("continueProgress");
		AppLogic.removeBackGroundCallback(mRunnableAddProgress);
		AppLogic.runOnBackGround(mRunnableAddProgress, AUTO_CALL_PERIOD);
	}

	public static void pauseProgress() {
		JNIHelper.logd("pauseProgress");
		AppLogic.removeBackGroundCallback(mRunnableAddProgress);
	}

	public static void clearProgress() {
		JNIHelper.logd("clearProgress");
		// mCanAutoCall = false;
		AppLogic.removeBackGroundCallback(mRunnableContinueProgress);
		mProgressBeginTime = 0;
		AppLogic.removeBackGroundCallback(mRunnableAddProgress);
		RecorderWin.hideProgressBar(0);
	}

	public static void stopTtsAndAsr() {
		JNIHelper.logd("stopTtsAndAsr");
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		AsrManager.getInstance().cancel();
	}

	public static void showContactSelectList(int event, MobileContacts cons) {
		showContactSelectList(event, cons, null);
	}

	public static void showContactSelectList(int event, MobileContacts cons, String ttsHint) {
		mLastHintTts = NativeData.getResString("RS_CALL_MAKE_CALL_SELECT_CALL");
		AsrManager.getInstance().pushGrammarId(VoiceData.GRAMMAR_SENCE_CALL_SELECT);

		RecorderWin.show();
		clearProgress();

		mIsSelecting = true;
		JNIHelper.logd("showContactSelectList IsSelecting : " + mIsSelecting);

		mProgress = 0;

		mSourceEvent = event;
		mMobileContacts = cons;

		String strPrefix = NativeData.getResString("RS_CALL_PREFIX");
		String strName = mMobileContacts.cons[0].name;
		String strSuffix = "";
		String strSpeakText = "";
		// mCanAutoCall = false;
		mCallMakeSureAsr = false;
		boolean bMutilName = false;
		mListCount = 0;

		switch (mSourceEvent) {
		case UiMakecall.SUBEVENT_MAKE_CALL_DIRECT:
			strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_DIRECT").replace("%NAME%", strName);
			mLastHintTts = NativeData.getResString("RS_CALL_MAKE_CALL_SURE").replace("%NAME%", strName);
			mCallMakeSureAsr = true;
			mGrammarSence = VoiceData.GRAMMAR_SENCE_CALL_MAKE_SURE;
			// mCanAutoCall = true;
			break;
		case UiMakecall.SUBEVENT_MAKE_CALL_LIST_NUMBER:
			strPrefix = "";
			strSuffix = NativeData.getResString("RS_CALL_MULITIPLE_SELECT")
					.replace("%CMD%", String.valueOf(mMobileContacts.cons[0].phones.length));
			String phoneNum = mMobileContacts.cons[0].phones[0];
			UiData.Resp_PhoneArea phoneArea = NativeData.getPhoneInfo(phoneNum);
			String numberInfo = phoneNum;
			String numberNum = "" + mMobileContacts.cons[0].phones.length;
			boolean bSameHead = false, bSameTail = false, bSameArea = false, bSameIsp = false, bIsShort = false;
			if (phoneNum.length() >= 3 && phoneNum.length() <= 6) {
				bIsShort = true;
			}
			if (phoneArea == null || phoneArea.strProvince == null || phoneArea.strCity == null) {
				bSameArea = true;
			}
			if (phoneArea == null || phoneArea.strIsp == null) {
				bSameIsp = true;
			}
			for (int i = 1; i < mMobileContacts.cons[0].phones.length; ++i) {
				String phoneNumIndex = mMobileContacts.cons[0].phones[i];
				UiData.Resp_PhoneArea phoneAreaIndex = NativeData.getPhoneInfo(phoneNumIndex);
				if (phoneNumIndex.substring(0, 3).equals(phoneNum.substring(0, 3))) {
					bSameHead = true;
				}
				if (bSameIsp == false && phoneAreaIndex != null && phoneArea.strIsp.equals(phoneAreaIndex.strIsp)) {
					bSameIsp = true;
				}
				if (bSameArea == false && phoneAreaIndex != null
						&& phoneArea.strProvince.equals(phoneAreaIndex.strProvince)
						&& phoneArea.strCity.equals(phoneAreaIndex.strCity)) {
					bSameArea = true;
				}
				if (phoneNumIndex.length() >= 4 && phoneNum.length() >= 4 && phoneNumIndex
						.substring(phoneNumIndex.length() - 4).equals(phoneNum.substring(phoneNum.length() - 4))) {
					bSameTail = true;
				}
				if (bIsShort == true && phoneNumIndex.length() >= 3 && phoneNumIndex.length() <= 6) {
					bIsShort = false;
				}
			}
			if (!bSameArea) {
				numberInfo = phoneArea.strProvince + phoneArea.strCity;
			} else if (!bSameIsp) {
				numberInfo = phoneArea.strIsp;
			} else if (bIsShort) {
				numberInfo = "短号";
			} else if (!bSameHead) {
				numberInfo = CallUtil.converToSpeechDigits(phoneNum.substring(0, 3)) + "开头";
			} else if (!bSameTail) {
				if (phoneNum.length() >= 4) {
					numberInfo = CallUtil.converToSpeechDigits(phoneNum.substring(phoneNum.length() - 4)) + "结尾";
				}
			}
			String sst = NativeData.getResString("RS_CALL_MAKE_CALL_AUTO").replace("%NAME%", strName);
			sst = sst.replace("%COUNT%", numberNum);
			sst = sst.replace("%NUMBER%", numberInfo);
			strSpeakText = sst;
			// mCanAutoCall = true;
			mCallMakeSureAsr = true;
			mGrammarSence = VoiceData.GRAMMAR_SENCE_CALL_SELECT;
			mListCount = mMobileContacts.cons[0].phones.length;
			break;
		case UiMakecall.SUBEVENT_MAKE_CALL_CHECK:
			if (mMobileContacts.cons[0].phones.length > 1) {
				strPrefix = "";
				strSuffix = NativeData.getResString("RS_CALL_MULITIPLE_SELECT")
						.replace("%CMD%", String.valueOf(mMobileContacts.cons[0].phones.length));
				strSpeakText = strName + NativeData.getResString("RS_CALL_MULITIPLE_CALL")
						.replace("%CMD%", String.valueOf(mMobileContacts.cons[0].phones.length));
				mGrammarSence = VoiceData.GRAMMAR_SENCE_CALL_SELECT;
				mListCount = mMobileContacts.cons[0].phones.length;
			} else {
				strPrefix = "找到联系人：";
				strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_FOUND").replace("%NAME%", strName);
				mLastHintTts = NativeData.getResString("RS_CALL_MAKE_CALL_SURE").replace("%NAME%", strName);
				mGrammarSence = VoiceData.GRAMMAR_SENCE_CALL_MAKE_SURE;
				mCallMakeSureAsr = true;
			}
			break;
		case UiMakecall.SUBEVENT_MAKE_CALL_NUMBER_DIRECT:
			strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_DIRECT").replace("%NAME%", strName);
			mLastHintTts = NativeData.getResString("RS_CALL_MAKE_CALL_SURE").replace("%NAME%", strName);
			// mCanAutoCall = true;
			mCallMakeSureAsr = true;
			mGrammarSence = VoiceData.GRAMMAR_SENCE_CALL_MAKE_SURE;
			break;
		case UiMakecall.SUBEVENT_MAKE_CALL_NUMBER:
			mCallMakeSureAsr = true;
			strPrefix = "请核对";
			strName = mMobileContacts.cons[0].phones[0];
			strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_CHECK").replace("%NUMBER%",
					CallUtil.converToSpeechDigits(strName));
			mLastHintTts = NativeData.getResString("RS_CALL_MAKE_CALL_SURE").replace("%NAME%", strName);
			break;
		case UiMakecall.SUBEVENT_MAKE_CALL_LIST:
			strPrefix = NativeData.getResString("RS_CALL_MULITIPLE_CONTACTS")
					.replace("%CMD%", String.valueOf(mMobileContacts.cons.length));;
			strName = "";
			strSpeakText = NativeData.getResString("RS_CALL_FIND_CONTACTS");
			if (mMobileContacts.cons.length > 0)
				strSpeakText += NativeData.getResPlaceholderString("RS_CALL_FIRST", "%CMD%", mMobileContacts.cons[0].name);;
			if (mMobileContacts.cons.length > 1)
				strSpeakText +=  NativeData.getResPlaceholderString("RS_CALL_SECOND", "%CMD%", mMobileContacts.cons[1].name);;
			if (mMobileContacts.cons.length > 2)
				strSpeakText += NativeData.getResPlaceholderString("RS_CALL_THIRD", "%CMD%", mMobileContacts.cons[2].name);
			strSpeakText += NativeData.getResString("RS_CALL_WITCH");
			bMutilName = true;
			mListCount = mMobileContacts.cons.length;
			break;
		case UiMakecall.SUBEVENT_MAKE_CALL_CANDIDATE:
			strPrefix = NativeData.getResString("RS_CALL_MULITIPLE_CONTACTS")
					.replace("%CMD%", String.valueOf(mMobileContacts.cons.length));
			strName = "";
			strSpeakText = NativeData.getResString("RS_CALL_FIND_CONTACTS");
			if (mMobileContacts.cons.length > 0)
				strSpeakText += NativeData.getResPlaceholderString("RS_CALL_FIRST", "%CMD%", mMobileContacts.cons[0].name);;
			if (mMobileContacts.cons.length > 1)
				strSpeakText += NativeData.getResPlaceholderString("RS_CALL_SECOND", "%CMD%", mMobileContacts.cons[1].name);
			if (mMobileContacts.cons.length > 2)
				strSpeakText += NativeData.getResPlaceholderString("RS_CALL_THIRD", "%CMD%", mMobileContacts.cons[2].name);;
			strSpeakText += NativeData.getResString("RS_CALL_CALL_FIRST");;
			bMutilName = true;
			// mCanAutoCall = true;
			mListCount = mMobileContacts.cons.length;
			break;
		}

		if (ttsHint != null) {
			// RecorderWin.addSystemMsg(ttsHint);
			strSpeakText = ttsHint;
		}

		showSelectList(strPrefix, strName, strSuffix, bMutilName);

		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		AsrManager.getInstance().cancel();

		beginWakeupSelect(false);

		final String[] spks = filterKeywords(strSpeakText);
		if (spks.length > 1) {
			JNIHelper.logd("CallSelectorControl spks[0]:" + spks[0] + ",spks[1]:" + spks[1]);
			mSpeechTaskId = TtsManager.getInstance().speakText(spks[0], new TtsUtil.ITtsCallback() {
				public void onSuccess() {
					speakWords(spks[1], PreemptType.PREEMPT_TYPE_IMMEADIATELY, true);
				}
			});
			return;
		}
		speakWords(strSpeakText, PreemptType.PREEMPT_TYPE_NEXT, false);
	}

	private static String[] filterKeywords(String kws) {
		if (kws.contains("呼叫电话还是取消。")) {
			return getKeywords(kws, "呼叫电话还是取消。");
		} else if (kws.contains("呼叫电话还是取消")) {
			return getKeywords(kws, "呼叫电话还是取消");
		} else if (kws.contains("确定还是取消")) {
			return getKeywords(kws, "确定还是取消");
		} else if (kws.contains("选择还是取消")) {
			return getKeywords(kws, "选择还是取消");
		} else if (kws.contains("呼叫还是取消")) {
			return getKeywords(kws, "呼叫还是取消");
		} else if (kws.contains("呼叫还是取消。")) {
			return getKeywords(kws, "呼叫还是取消。");
		}
		return new String[] { kws };
	}

	private static String[] getKeywords(String kws, String slot) {
		if (kws.contains(slot)) {
			String[] tmp = kws.split(slot);
			if (tmp.length == 1) {
				if (tmp[0].equals(slot)) {
					return new String[] { "", tmp[0] };
				}
				return new String[] { tmp[0], slot };
			} else if (tmp.length > 1) {
				return new String[] { tmp[0], kws.substring(tmp[0].length()) };
			}

			return new String[] { "", kws };
		}

		return new String[] { kws };
	}

	private static void speakWords(String words, PreemptType type, boolean stopWakeup) {
		if (stopWakeup) {
			mSpeechTaskId = TtsManager.getInstance().speakTextNoWakeup(words, type, new ITtsCallback() {
				@Override
				public void onEnd() {
					AsrManager.getInstance().mSenceRepeateCount = 0;
					selectAgain();
				}

				public void onSuccess() {
					mProgressBeginTime = System.currentTimeMillis();
					AppLogicBase.removeBackGroundCallback(selectTimeOutTask);
					AppLogicBase.runOnBackGround(selectTimeOutTask, SELECT_OUT_TIME);
					RecorderWin.addCloseRunnable(new Runnable() {
						@Override
						public void run() {
							AppLogicBase.removeBackGroundCallback(selectTimeOutTask);
						}
					});
					if (mCanAutoCall)
						continueProgress();
				}
			});
			return;
		}
		mSpeechTaskId = TtsManager.getInstance().speakText(words, type, new ITtsCallback() {
			@Override
			public void onEnd() {
				AsrManager.getInstance().mSenceRepeateCount = 0;
				selectAgain();
			}

			@Override
			public void onSuccess() {
				mProgressBeginTime = System.currentTimeMillis();
				AppLogicBase.removeBackGroundCallback(selectTimeOutTask);
				AppLogicBase.runOnBackGround(selectTimeOutTask, SELECT_OUT_TIME);
				RecorderWin.addCloseRunnable(new Runnable() {
					@Override
					public void run() {
						AppLogicBase.removeBackGroundCallback(selectTimeOutTask);
					}
				});
				if (mCanAutoCall)
					continueProgress();
			}
		});
	}

	static AsrOption mAsrOptionSelectAgain = new AsrOption().setManual(false).setNeedStopWakeup(false);

	public static void selectAgain() {
		JNIHelper.logd("selectAgain IsSelecting : " + mIsSelecting);

		if (!mIsSelecting)
			return;

		if (ProjectCfg.mCoexistAsrAndWakeup && AsrManager.getInstance().mSenceRepeateCount >= 0) {
			AsrManager.getInstance().mSenceRepeateCount++;
			JNIHelper.logd("call select SenceRepeateCount: " + AsrManager.getInstance().mSenceRepeateCount);
			if (AsrManager.getInstance().mSenceRepeateCount < AsrManager.ASR_SENCE_REPEATE_COUNT) {
				AsrManager.getInstance().start(mAsrOptionSelectAgain);
				beginWakeupSelect(false);
				return;
			}
		}
		if (ProjectCfg.mEnableAEC) {
			beginWakeupSelect(false);
		} else {
			beginWakeupSelect(true);
		}
	}

	static class CallAfterTts extends ITtsCallback {
		String mNumber;
		String mName;

		public CallAfterTts(String num, String name) {
			mNumber = num;
			mName = name;
		}

		@Override
		public void onSuccess() {
			CallManager.getInstance().makeCall(mNumber, mName);
			RecorderWin.close();
			super.onSuccess();
		}
	};

	public static void selectIndex(int index, String fromVoice) {
		if (index < 0 || (mListCount > 0 && index >= mListCount))
			return;

		endWakeupSelect();

		if (mMobileContacts == null || mMobileContacts.cons == null || mMobileContacts.cons.length <= 0) {
			JNIHelper.loge("wrong contacts data");
			return;
		}
		clearProgress();
		String hintTts = null;
		String hintShow = null;
		CallAfterTts callAfterTts = null;

		if (mMobileContacts.cons.length > 1) {
			if (index >= 0 && index < mMobileContacts.cons.length) {
				if (mMobileContacts.cons[index].phones.length == 1) {
					// mCanAutoCall = false;
					callAfterTts = new CallAfterTts(mMobileContacts.cons[index].phones[0],
							mMobileContacts.cons[index].name);
					if (fromVoice != null) {
						hintTts = "正在呼叫" + mMobileContacts.cons[index].name;
					}
				} else {
					mSourceEvent = UiMakecall.SUBEVENT_MAKE_CALL_LIST_NUMBER;
					MobileContact con = mMobileContacts.cons[index];
					mMobileContacts.cons = new MobileContact[1];
					mMobileContacts.cons[0] = con;
					showContactSelectList(mSourceEvent, mMobileContacts);
					if (TextUtils.isEmpty(fromVoice)) {
						// 数据上报
						ReportUtil.doReport(new ReportUtil.Report.Builder().setType("call").setAction("select")
								.putExtra("index", index).buildTouchReport());
					}
					return;
				}
			}
		} else if (index >= 0 && index < mMobileContacts.cons[0].phones.length) {
			// mCanAutoCall = false;
			callAfterTts = new CallAfterTts(mMobileContacts.cons[0].phones[index], mMobileContacts.cons[0].name);
			if (fromVoice != null) {
				if (mMobileContacts.cons[0].phones.length > 1) {
					hintTts =  NativeData.getResString("RS_CALL_NUMBER")
							.replace("%CMD%",
									CallUtil.converToSpeechDigits(fromVoice));
					hintShow = NativeData.getResString("RS_CALL_NUMBER")
							.replace("%CMD%", fromVoice);
				} else {
					hintTts = "正在呼叫" + CallUtil.converToSpeechDigits(fromVoice) + "号码";
					hintShow = "正在呼叫" + fromVoice + "号码";
				}
			}
		}
		if (hintShow == null) {
			hintShow = hintTts;
		}

		if (fromVoice != null) {
			RecorderWin.addSystemMsg(hintShow);
			mSpeechTaskId = TtsManager.getInstance().speakText(hintTts, PreemptType.PREEMPT_TYPE_IMMEADIATELY,
					callAfterTts);
		} else {
			if (callAfterTts != null)
				callAfterTts.onSuccess();
		}

		if (TextUtils.isEmpty(fromVoice)) {
			// 数据上报
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType("call").setAction("select")
					.putExtra("index", index).buildTouchReport());
		}
	}

	public static void selectSure(boolean fromVoice) {
		if (!mCanAutoCall && !mCallMakeSureAsr)
			return;
		selectIndex(0, fromVoice ? "" : null);
	}

	public static void selectCancel(boolean fromVoice) {
		String spk = NativeData.getResString("RS_CALL_CANCEL");
		if (fromVoice)
			TtsManager.getInstance().speakText(spk);
		clearIsSelecting();
		RecorderWin.dismiss();
	}

	static Runnable mRunnableContinueProgress = new Runnable() {
		@Override
		public void run() {
			mProgressBeginTime = 0;
			continueProgress();
		}
	};
	static long mProgressBeginTime = 0; // 进度启动时间

	static boolean mWakeupSelect = false;

	public static void beginWakeupSelect(final boolean withStartTip) {

		AsrComplexSelectCallback wakeupAsr = new AsrComplexSelectCallback() {
			Runnable removeBackgourd = null;
			Runnable taskRunnable = null;
			private static final int speechDelay = 700;
			private static final int handleDelay = 800;
			private boolean isEnd = false;
			private long mLastSpeechEndTime = 0;

			@Override
			public String getTaskId() {
				return WAKEUP_TASK_ID;
			}

			@Override
			public boolean needAsrState() {
				return true;
			}

			@Override
			public void onVolume(int volume) {
				// 自动呼叫下第一次有人说话时暂停进度条
				// if (volume > 30 && mCanAutoCall && mProgressBeginTime > 0) {
				// long diff = System.currentTimeMillis() - mProgressBeginTime;
				// if (diff > 500 && diff < 6000) {
				// pauseProgress();
				// AppLogic.removeBackGroundCallback(mRunnableContinueProgress);
				// AppLogic.runOnBackGround(mRunnableContinueProgress,
				// 5000);
				// }
				// }
			}

			@Override
			public void onSpeechBegin() {
				pauseProgress();
				AppLogic.removeBackGroundCallback(mRunnableContinueProgress);
			}

			@Override
			public void onSpeechEnd() {
				mLastSpeechEndTime = System.currentTimeMillis();
				AppLogic.runOnBackGround(mRunnableContinueProgress, 0);
				if (removeBackgourd != null) {
					AppLogic.removeBackGroundCallback(removeBackgourd);
				}
			}

			@Override
			public void onCommandSelected(final String type, String command) {
				if (taskRunnable != null) {
					AppLogic.removeBackGroundCallback(taskRunnable);
					taskRunnable = null;
				}
				taskRunnable = new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						JNIHelper.logd("liTest:now-command");
						isEnd = true;
						if ("SURE".equals(type)) {
							selectSure(true);
							return;
						}
						if ("CANCEL".equals(type)) {
							clearIsSelecting();
							String spk = NativeData.getResString("RS_CALL_CANCEL_HELP");
							RecorderWin.open(spk);
							return;
						}
					}
				};
				removeBackgourd = new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						AppLogic.removeBackGroundCallback(taskRunnable);
					}
				};
				if (this.isWakeupResult()) {
					isEnd = false;
					if (System.currentTimeMillis() - mLastSpeechEndTime < 300
							|| !AsrManager.getInstance().mUseIflyOnline) {
						AppLogic.runOnBackGround(taskRunnable, 0);
						AppLogic.removeBackGroundCallback(removeBackgourd);
					} else {
						AppLogic.runOnBackGround(removeBackgourd, speechDelay);
						AppLogic.runOnBackGround(taskRunnable, handleDelay);
					}
				} else if (!isEnd) {
					AppLogic.runOnBackGround(taskRunnable, 0);
					AppLogic.removeBackGroundCallback(removeBackgourd);
				}
			}

			@Override
			public void onIndexSelected(final List<Integer> indexs, final String command) {
				if (taskRunnable != null) {
					AppLogic.removeBackGroundCallback(taskRunnable);
					taskRunnable = null;
				}
				taskRunnable = new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						isEnd = true;
						if (indexs.size() != 1) {
							if (mMobileContacts == null || mMobileContacts.cons == null)
								return;
							String newCommand;
							if (command.matches("^\\d+((开头)|(结尾)?那个)?$")) {
								newCommand = CallUtil.converToSpeechDigits(command);
							} else if (command.endsWith("那个"))
								newCommand = command.substring(0, command.length() - 2);
							else {
								newCommand = command;
							}
							MobileContacts newCons = new MobileContacts();
							int newEvent = 0;
							if (mMobileContacts.cons.length > 1) {
								newCons.cons = new MobileContact[indexs.size()];
								for (int i = 0; i < indexs.size(); ++i) {
									newCons.cons[i] = mMobileContacts.cons[indexs.get(i)];
								}
								newEvent = UiMakecall.SUBEVENT_MAKE_CALL_LIST;
							} else {
								if (mMobileContacts.cons.length == 0)
									return;
								if (mMobileContacts.cons[0] == null || mMobileContacts.cons[0].phones == null
										|| mMobileContacts.cons[0].phones.length < 2)
									return;
								newCons.cons = new MobileContact[1];
								newCons.cons[0] = mMobileContacts.cons[0];
								String[] newPhones = new String[indexs.size()];
								for (int i = 0; i < indexs.size(); ++i) {
									newPhones[i] = mMobileContacts.cons[0].phones[indexs.get(i)];
								}
								newCons.cons[0].phones = newPhones;
								newEvent = UiMakecall.SUBEVENT_MAKE_CALL_LIST_NUMBER;
							}
							clearProgress();
							resetSelectTime();
							String spk = NativeData.getResPlaceholderString(
									"RS_CALL_RESELECR", "%CMD%", indexs.size()
											+ "");
							showContactSelectList(newEvent, newCons, newCommand + spk);
							clearProgress();
							return;
						}
						selectIndex(indexs.get(0), command);
					}
				};
				removeBackgourd = new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						AppLogic.removeBackGroundCallback(taskRunnable);
					}
				};
				if (this.isWakeupResult()) {
					isEnd = false;
					if (System.currentTimeMillis() - mLastSpeechEndTime < 300
							|| !AsrManager.getInstance().mUseIflyOnline) {
						AppLogic.runOnBackGround(taskRunnable, 0);
						AppLogic.removeBackGroundCallback(removeBackgourd);
					} else {
						AppLogic.runOnBackGround(removeBackgourd, speechDelay);
						AppLogic.runOnBackGround(taskRunnable, handleDelay);
					}
				} else if (!isEnd) {
					AppLogic.runOnBackGround(taskRunnable, 0);
					AppLogic.removeBackGroundCallback(removeBackgourd);
				}
			}
		};

		mWakeupSelect = true;
		wakeupAsr.addCommand("CANCEL", "取消", "返回", "放弃", "挂断", "再见", "取消取消", "返回返回", "放弃放弃", "挂断挂断", "再见再见");
		if (mCallMakeSureAsr) {
			wakeupAsr.addCommand("SURE", "呼叫", "确定", "呼叫呼叫", "确定确定", "呼叫电话");
		}
		if (mListCount == 2) {
			wakeupAsr.addIndex(0, "上面那个", "前面那个");
			wakeupAsr.addIndex(1, "下面那个", "后面那个");
		} else if (mListCount > 2) {
			wakeupAsr.addIndex(0, "最上面那个", "最前面那个");
			wakeupAsr.addIndex(mListCount - 1, "最下面那个", "最后面那个", "最后一个");
		}

		for (int i = 0; i < mListCount; ++i) {
			wakeupAsr.addIndex(i, AsrUtil.COMMON_INDEX_KEYWORDS[i]);
		}

		if (mMobileContacts.cons.length > 1) {
			for (int i = 0; i < mMobileContacts.cons.length; ++i) {
				for (String kw : KeywordsParser.splitKeywords(mMobileContacts.cons[i].name))
					wakeupAsr.addIndex(i, kw);
				UiData.Resp_PhoneArea phoneArea = getContactPhoneInfo(mMobileContacts.cons[i]);
				if (phoneArea != null) {
					if (phoneArea.strCity != null) {
						wakeupAsr.addIndex(i, phoneArea.strCity);
					}
					if (phoneArea.strProvince != null)
						wakeupAsr.addIndex(i, phoneArea.strProvince);
				}
			}
		} else if (mMobileContacts.cons[0].phones.length > 1) {
			for (int i = 0; i < mMobileContacts.cons[0].phones.length; ++i) {
				// 号码规则
				String phoneNum = mMobileContacts.cons[0].phones[i];
				if (phoneNum.length() > 3) {
					wakeupAsr.addIndex(i, phoneNum.substring(0, 3));
					wakeupAsr.addIndex(i, phoneNum.substring(0, 3) + "开头");
					if (phoneNum.length() > 4) {
						wakeupAsr.addIndex(i, phoneNum.substring(phoneNum.length() - 4));
						wakeupAsr.addIndex(i, phoneNum.substring(phoneNum.length() - 4) + "结尾");
						if (phoneNum.length() == 11) {
							wakeupAsr.addIndex(i, phoneNum.substring(3, phoneNum.length() - 4));
						}
					}
				}
				// 座机规则那个
				int phoneType = NativeData.getPhoneType(phoneNum);
				JNIHelper.logd(phoneNum + " phone type: " + phoneType);
				switch (phoneType) {
				case 1:
					wakeupAsr.addIndex(i, "短号");
					break;
				case 2:
					wakeupAsr.addIndex(i, "手机");
					break;
				case 3:
					wakeupAsr.addIndex(i, "座机");
					wakeupAsr.addIndex(i, "座机");
					break;
				}
				// 归属地运营商规则
				UiData.Resp_PhoneArea phoneArea = NativeData.getPhoneInfo(phoneNum);
				if (phoneArea != null && phoneArea.bResult != null && phoneArea.bResult) {
					if (phoneArea.strIsp != null)
						wakeupAsr.addIndex(i, phoneArea.strIsp);
					if (phoneArea.strCity != null) {
						wakeupAsr.addIndex(i, phoneArea.strCity);
					}
					if (phoneArea.strProvince != null)
						wakeupAsr.addIndex(i, phoneArea.strProvince);
				}
			}
		}
		RecorderWin.addCloseRunnable(new Runnable() {
			@Override
			public void run() {
				AppLogicBase.removeBackGroundCallback(selectTimeOutTask);
			}
		});

		WakeupManager.getInstance().useWakeupAsAsr(wakeupAsr);

		if (withStartTip) {
			WakeupManager.getInstance().playAsrTipSound();
		}
		if (AsrManager.getInstance().mSenceRepeateCount >= 0) {
			AsrManager.getInstance().mSenceRepeateCount++;
			JNIHelper.logd("call select SenceRepeateCount: " + AsrManager.getInstance().mSenceRepeateCount);
			if (AsrManager.getInstance().mSenceRepeateCount < AsrManager.ASR_SENCE_REPEATE_COUNT) {
				if (mSourceEvent != UiMakecall.SUBEVENT_MAKE_CALL_LIST_NUMBER)
					AppLogic.runOnBackGround(new Runnable() {

						@Override
						public void run() {
							AsrManager.getInstance().startIFlyOnlineOnly(
									new AsrOption().setGrammar(VoiceData.GRAMMAR_SENCE_CALL_SELECT));
						}
					}, 700);
				else
					AppLogic.runOnBackGround(new Runnable() {

						@Override
						public void run() {
							AsrManager.getInstance().startIFlyOnlineOnly(
									new AsrOption().setGrammar(VoiceData.GRAMMAR_SENCE_INCOMING_MAKE_SURE));
						}
					}, 700);
			}
		}
	}

	public static void endWakeupSelect() {
		AppLogicBase.removeBackGroundCallback(selectTimeOutTask);
		if (mWakeupSelect) {
			WakeupManager.getInstance().recoverWakeupFromAsr(WAKEUP_TASK_ID);
			mWakeupSelect = false;
		}
		RecorderWin.setState(STATE.STATE_END);
		AsrManager.getInstance().cancelIFlyOnlineOnly();
	}

	private static void showSelectList(String strPrefix, String strName, String strSuffix, boolean isMultilName) {
		List<Contact> list = new ArrayList<Contact>();
		int count = isMultilName ? mMobileContacts.cons.length : mMobileContacts.cons[0].phones.length;
		if (count > 9) {
			count = 9;
		}

		for (int i = 0; i < count; i++) {
			Contact info = new Contact();
			if (isMultilName) {
				info.name = mMobileContacts.cons[i].name;
				info.number = mMobileContacts.cons[i].phones[0];
				UiData.Resp_PhoneArea phoneInfo = getContactPhoneInfo(mMobileContacts.cons[i]);
				if (phoneInfo != null) {
					info.province = phoneInfo.strProvince;
					info.city = phoneInfo.strCity;
					info.isp = phoneInfo.strIsp;
				}
			} else {
				info.name = mMobileContacts.cons[0].name;
				info.number = mMobileContacts.cons[0].phones[i];
				UiData.Resp_PhoneArea phoneInfo = NativeData.getPhoneInfo(info.number);
				if (phoneInfo != null) {
					info.province = phoneInfo.strProvince;
					info.city = phoneInfo.strCity;
					info.isp = phoneInfo.strIsp;
				}
			}
			list.add(info);
		}

		RecorderWin.sendContactList(strPrefix, strName, strSuffix, isMultilName, list);
	}

	// 选择超时，防止用户感觉界面卡死
	static Runnable selectTimeOutTask = new Runnable() {
		@Override
		public void run() {
			AppLogicBase.removeBackGroundCallback(selectTimeOutTask);
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_CALL_TIMEOUT");
			RecorderWin.speakTextWithClose(spk, null);
			clearIsSelecting();
		}
	};

	static void resetSelectTime() {
		AppLogicBase.removeBackGroundCallback(selectTimeOutTask);
		AppLogicBase.runOnBackGround(selectTimeOutTask, SELECT_OUT_TIME);
	}

	// 获取联系人的手机信息
	static UiData.Resp_PhoneArea getContactPhoneInfo(MobileContact con) {
		if (con == null)
			return null;
		if (con.phones == null || con.phones.length == 0)
			return null;
		UiData.Resp_PhoneArea ret = NativeData.getPhoneInfo(con.phones[0]);
		if (ret == null || ret.bResult == null || ret.bResult == false)
			return null;
		if (ret.strProvince == null || ret.strCity == null)
			return null;
		for (int i = 1; i < con.phones.length; ++i) {
			UiData.Resp_PhoneArea reti = NativeData.getPhoneInfo(con.phones[i]);
			if (reti == null || reti.bResult == null || reti.bResult == false)
				return null;
			if (!ret.strProvince.equals(reti.strProvince))
				return null;
			if (!ret.strCity.equals(reti.strCity))
				return null;
			if (ret.strIsp != null && !ret.strIsp.equals(reti.strIsp))
				ret.strIsp = null;
		}
		return ret;
	}

	static boolean mIsSelecting = false;

	public static void clearIsSelecting() {
		JNIHelper.logd("IsSelecting clearIsSelecting");

		mIsSelecting = false;

		clearProgress();
		stopTtsAndAsr();
		endWakeupSelect();
		AsrManager.getInstance().popGrammarId(VoiceData.GRAMMAR_SENCE_CALL_SELECT);
	}

	public static boolean isSelecting() {
		JNIHelper.logd("IsSelecting : " + mIsSelecting);
		return mIsSelecting;
	}
}

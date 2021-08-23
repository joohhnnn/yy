package com.txznet.txz.ui.win.record;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.database.Observable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.txz.ui.data.UiData;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.ui.dialog2.WinConfirm;
import com.txznet.comm.ui.dialog2.WinConfirm.WinConfirmBuildData;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.viewfactory.view.IRecordView;
import com.txznet.comm.util.FilePathConstants;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.record.bean.ChatMessage;
import com.txznet.record.ui.WinRecord;
import com.txznet.record.ui.WinRecordImpl2;
import com.txznet.txz.R;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.choice.list.AbstractChoice;
import com.txznet.txz.component.home.HomeControlManager;
import com.txznet.txz.component.media.MediaPriorityManager;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.audio.AudioManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.film.FilmManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.launch.LaunchManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.mtj.MtjModule;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.music.focus.MusicFocusManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.news.NewsManager;
import com.txznet.txz.module.reminder.ReminderManager;
import com.txznet.txz.module.roadtraffic.RoadTrafficManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.sim.SimManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.ticket.QiWuTicketManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.userconf.UserConf;
import com.txznet.txz.module.voiceprintrecognition.VoiceprintRecognitionManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.module.wheelcontrol.WheelControlManager;
import com.txznet.txz.module.wheelcontrol.WheelControlManager.OnTXZWheelControlListener;
import com.txznet.txz.notification.NotificationManager;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;
import com.txznet.txz.ui.widget.QiWuTicketReminderView;
import com.txznet.txz.ui.widget.SDKFloatView;
import com.txznet.txz.ui.win.help.WinHelpDetailSelector;
import com.txznet.txz.ui.win.help.WinHelpDetailTops;
import com.txznet.txz.ui.win.help.WinHelpManager;
import com.txznet.txz.ui.win.nav.SearchEditManager;
import com.txznet.txz.util.BeepPlayer;
import com.txznet.txz.util.PackageInstaller;
import com.txznet.txz.util.PermissionUtils;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.recordcenter.TXZSourceRecorderManager;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;
import com.txznet.txz.module.feedback.FeedbackManager;


public class RecorderWin {
	public static final String START_ASR_HINT = "hinTxt";
	public static final String START_ASR_GRAMMAR = "grammar";
	public static final String START_KEEP_GRAMMAR = "keep";
	public static final String START_ASR_INTENT_FLAGS = "intentFlags";
	public static final String START_ASR_ACTION = "com.txznet.txz.asr.start.action";
	public static boolean enableInterruptTips = false;
	public static boolean enableInterruptTips_SDK = true;


	static {
		GlobalContext.get().registerReceiver(new StartAsrReceiver(),
				new IntentFilter(START_ASR_ACTION));
	}

	public static class StatusObervable extends
			Observable< StatusObervable.StatusObserver> {
		public static interface StatusObserver {
			public void onShow();

			public void onDismiss();
		}

		public void notifyShow() {
			synchronized (mObservers) {
				for (int i = mObservers.size() - 1; i >= 0; i--) {
					mObservers.get(i).onShow();
				}
			}
		}

		public void notifyDismiss() {
			synchronized (mObservers) {
				for (int i = mObservers.size() - 1; i >= 0; i--) {
					mObservers.get(i).onDismiss();
				}
			}
		}
	}

	public static StatusObervable OBSERVABLE = new StatusObervable();

	public static interface RecorderWinStateObserver {
		public void onShow();

		public void onDismiss();
	}

	public static final int CallSence = 0;
	public static final int WeChatSence = 1;
	public static final int PoiChoiceSence = 2;
	public static final int RadioSence = 3;
	public static final int AudioSence = 4;
	public static final int SimSence = 5;
	public static final int TtsThemeSence = 6;
	public static final int PluginSence = 7;
	// 简单数据类型，显示序号和name，皮肤包已经用了9
	public static final int SimpleSence = 11;
	public static final int ReminderSence = 12;
	public static final int FlightSence = 13;
	public static final int TRAIN_SENCE = 14;
	public static final int STYLE_SENCE = 15;
    public static final int FILM_SENCE_FILM = 16;
    //电影票场景中的电影院选择页面
    public static final int FILM_SENCE_MOVICE_THEATER = 17;
    //电影票场景中的电影场次选择页面
    public static final int FILM_SENCE_MOVICE_TIME = 18;
    public static final int MI_HOME_SCENE = 22;
    //赛事选择界面
    public static final int COMPETITION_LIST = 23;
    public static final int COMPETITION_DETAIL = 24;
	//齐悟火车票选择界面
	public static final int TARIN_TICKET = 25;
	//齐悟火车票选择页面
	public static final int FLIGHT_TICKET = 26;

	//齐悟票务支付页面
	public static final int TICKET_PAY = 27;

	public static final int STATE_NORMAL = 0; // 正常状态，显示一个录音图标
	public static final int STATE_RECORD_START = 1; // 录音开始，显示一个声纹动画
	public static final int STATE_RECORD_END = 2; // 录音结束，显示一个处理中动画

	public static final int OWNER_SYS = 0; // 系统发起
	public static final int OWNER_USER = 1; // 用户发起
	public static final int OWNER_USER_PART = 2; // 用户发起，打字效果

	public static boolean mIsAlreadyOpened = false;

	private static String mLastUserText = null;

	private static boolean mHideLastSystemText = false;

	public static void setLastUserText(String s) {
		mLastUserText = TextResultHandle.getInstance().getShowText(s);
		if (mLastUserText != null && mLastUserText.length() == 0) {
			mLastUserText = NativeData.getResString("RS_USER_EMPTY_TEXT");
		}
	}

	public static enum STATE {
		/**
		 * 开启录音中
		 */
		STATE_START,
		/**
		 * 请说话
		 */
		STATE_RECORD,
		/**
		 * 正在倾听
		 */
		STATE_LISTEN,
		/**
		 * 正在识别
		 */
		STATE_RECOGONIZE,
		/**
		 * 正在处理
		 */
		STATE_PROCESSING,
		/**
		 * 结束
		 */
		STATE_END,
		/**
		 * 区别唤醒需要的录音状态展示
		 */
		STATE_WAKEUP_RECORD,
	};

	public static class Contact {
		public String name;
		public String number;
		public String province;
		public String city;
		public String isp;
	}

	private RecorderWin() {
	}

	public static boolean mFirst = true;
	private static boolean mWillStartAsrAfterTts = false;
	public static boolean mEnableNameUser = false;
	public static boolean mUnknowFirst = true;//语音无法识别是否是首次的标志位

	private static String getWelcomeMsg() {
		String txt = null;
		if (mFirst) {
			if (InterruptTts.getInstance().isInterruptTTS()) {
				txt = NativeData.getResString("RS_VOICE_ASR_START_HINT");
			}else {
				txt = NativeData.getResString("RS_RECORD_DI");
			}
			mFirst = false;
		} else {
			txt = NativeData.getResString("RS_VOICE_ASR_START_HINT");
			String strUserConfMsg = UserConf.getInstance().getUserConfigData().mDeviceWelcomeMsg;
			//用户配置了欢迎语。非null即配置了欢迎语,即使是空字符""
			if (strUserConfMsg != null){
				txt = strUserConfMsg;
			}
		}
		return txt;
	}

	public static void open() {
		open(getWelcomeMsg());
	}

	public static void open(String txt) {
		open(txt, AsrManager.getInstance().getCurrentGrammarId());
	}
	
	public static void open(String txt, int grammar) {
		open(txt, grammar, null);
	}

	/**
	 * 免唤醒识别入口
	 */
	public static void openInstantAsr() {
		open("", AsrManager.getInstance().getCurrentGrammarId(), true, null);
	}

	public static void open(String txt, final int grammar, final Runnable speakEnd){
		open(txt, grammar, false, speakEnd);
	}

	public static void open(String txt, final int grammar, boolean instantAsr, 
			final Runnable speakEnd) {
		mFirst = false;
		mUnknowFirst = true;
		LaunchManager.getInstance().mStartWithInstanceAsr = false;
		
		MtjModule.getInstance().event(MtjModule.EVENTID_VOICE_TRIGGER);
		ChoiceManager.getInstance().clearIsSelecting();
		HelpGuideManager.getInstance().recordTime();
		AsrUtil.openRecordWinLock();
		NewsManager.getInstance().stop();//打开录音界面新闻需要停止,长安欧尚需求

		if (txt != null && (txt.endsWith("吗") || txt.contains("什么"))) {
			AsrManager.getInstance().regCommand(
                    "GLOBAL_CMD_END_CHAT_BY_QUESTION");
		}

		if (mLastUserText != null && isOpened()) {
			sendMsg(RecorderWin.OWNER_USER, mLastUserText);
		}
		mLastUserText = null;
		// 启动声控时要停止自动导航
		ServiceManager.getInstance().sendInvoke(ServiceManager.NAV,
				"nav.action.navipreviewstopcount", null, null);
		AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
        show(instantAsr || TextUtils.isEmpty(txt));
        refreshWinState(IRecordView.STATE_WIN_OPEN);
		FilmManager.getInstance().setBeClearWanMi();
		// 免唤醒识别
		if(instantAsr){
			AsrManager.getInstance().startWithInstantAsr(true, grammar, true);
			return;
		}
        // 重置标志位
        FeedbackManager.getInstance().setCanShowHelpTips(true);
        // 打开声控，取消反馈录音
        if (FeedbackManager.getInstance().isBusy()) {
            FeedbackManager.getInstance().cancel();
        }
		if (TextUtils.isEmpty(txt)) {
			AsrManager.getInstance().setCloseRecordWinWhenProcEnd(true);
			AsrManager.getInstance().start(true, grammar, true);
			return;
		}

		RecorderWin.sendMsg(RecorderWin.OWNER_SYS, txt);

		mWillStartAsrAfterTts = true;
		mSpeechTaskId = TtsManager.getInstance().speakText(txt,
				PreemptType.PREEMPT_TYPE_IMMEADIATELY, new ITtsCallback() {
					@Override
					public void onEnd() {
						if (speakEnd != null)
							speakEnd.run();
//						mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
					}

					@Override
					public void onSuccess() {
						AsrManager.getInstance().setCloseRecordWinWhenProcEnd(
								true);
						AsrManager.getInstance().start(true, grammar, true);
						mWillStartAsrAfterTts = false;
					}

					@Override
					public void onCancel() {
						AsrManager.getInstance().setCloseRecordWinWhenProcEnd(
								true);
						mWillStartAsrAfterTts = false;
					}

					@Override
					public void onError(int iError) {
						AsrManager.getInstance().setCloseRecordWinWhenProcEnd(
								true);
						AsrManager.getInstance().cancel();
						RecorderWin.close();
						// AsrManager.getInstance().start(true, grammar);
						mWillStartAsrAfterTts = false;
					}

					@Override
					public boolean isNeedStartAsr() {
						return true;
					}
				});
	}
	
	private static final String TASK_INTERRUPT_KW = "InterruptKws";
	private static String[] lastInterruptKws = null;
	
	private static Runnable mRunnableDelInterruptKws = new Runnable() {
		@Override
		public void run() {
			String[] kws = lastInterruptKws;
			if (kws == null || kws.length == 0
			 || RecorderWin.isOpened()
			 || WakeupManager.getInstance().needDelInterruptKws() == false
			 || TtsManager.getInstance().isBusy()
			) {
				return;
			}
			WakeupManager.getInstance().recoverWakeupFromAsr(TASK_INTERRUPT_KW);
			lastInterruptKws = null;
		}
	};

	private static Runnable mRunnableAddInterruptKws =  new Runnable() {
		@Override
		public void run() {
			String[] kws = WakeupManager.getInstance().getInterruptTips();
			if (Arrays.equals(kws, lastInterruptKws)) {

				return;
			}
			if ((kws == null || kws.length == 0) && !(lastInterruptKws == null || lastInterruptKws.length == 0)) {
				WakeupManager.getInstance().recoverWakeupFromAsr(TASK_INTERRUPT_KW);
				return;
			}
			if (!enableInterruptTips_SDK) {
				return;
			}
			lastInterruptKws = kws;
			if (kws == null || kws.length == 0) {
				return;
			}
			AsrComplexSelectCallback acsc = new AsrComplexSelectCallback() {
				
				@Override
				public boolean needAsrState() {
					return false;
				}
				
				@Override
				public String getTaskId() {
					return TASK_INTERRUPT_KW;
				}
				
				@Override
				public void onCommandSelected(String type, String command) {
				    if(!isWakeupResult()){
				        speakText(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"), null);
				    }else if("Interrupt".equals(type) && enableInterruptTips && enableInterruptTips_SDK) {
				    	if (isOpened()) {
							TtsManager.getInstance().cancelSpeak(TtsManager.getInstance().getCurTaskId());
							speakText("好的",null);
							if(WakeupManager.getInstance().mInterruptTipsCount < 3){
								PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_INTERRUPT_TIPS_COUNT, ++WakeupManager.getInstance().mInterruptTipsCount);
							}
						} else {
							LogUtil.logd("record window not open");
						}
					}
					
				}
			}.addCommand("Interrupt", kws);
			WakeupManager.getInstance().useWakeupAsAsr(acsc);
		}
	};

	private static final String TASK_JSZ_INTERRUPT_KW = "InterruptJSZKws";
	private static String lastInterruptJSZKws = null;
	//金手指逻辑
	private static Runnable mRunnableAddInterruptJSZKws =  new Runnable() {
		@Override
		public void run() {
			String kws = WakeupManager.getInstance().getJSZInterruptKw();
			if ( TextUtils.equals(kws, lastInterruptJSZKws)) {
				return;
			}
			if (TextUtils.isEmpty(kws) && !TextUtils.isEmpty(lastInterruptJSZKws)) {
				WakeupManager.getInstance().recoverWakeupFromAsr(TASK_JSZ_INTERRUPT_KW);
				return;
			}
			lastInterruptJSZKws = kws;
			if (kws == null || kws.length() == 0) {
				return;
			}
			AsrComplexSelectCallback acsc = new AsrComplexSelectCallback() {

				@Override
				public boolean needAsrState() {
					return false;
				}

				@Override
				public String getTaskId() {
					return TASK_JSZ_INTERRUPT_KW;
				}

				@Override
				public void onCommandSelected(String type, String command) {
					LogUtil.d("JSZCommand :"+command);
					if("Interrupt".equals(type)) {
						if (isOpened()) {
							speakTextWithClose("好的", new Runnable() {
								@Override
								public void run() {
									HelpGuideManager.getInstance().toggleDetailMode();
									ReportUtil.doReport(new ReportUtil.Report.Builder().setSessionId()
											.setAction("openJSZ").putExtra("isRecorderWin",true).buildCommReport());
								}
							});
						} else {
							HelpGuideManager.getInstance().toggleDetailMode();
							ReportUtil.doReport(new ReportUtil.Report.Builder().setSessionId()
									.setAction("openJSZ").putExtra("isRecorderWin",false).buildCommReport());
							LogUtil.logd("record window not open");
						}
					}else if("CLOSE_JSZ".equals(type)){
                        if (isOpened()) {
                            speakTextWithClose("好的", new Runnable() {
                                @Override
                                public void run() {
                                    HelpGuideManager.getInstance().toggleFloatBtnMode();
									ReportUtil.doReport(new ReportUtil.Report.Builder().setSessionId()
											.setAction("closeJSZ").putExtra("isRecorderWin",true).buildCommReport());
                                }
                            });
                        } else {
                            HelpGuideManager.getInstance().toggleFloatBtnMode();
							ReportUtil.doReport(new ReportUtil.Report.Builder().setSessionId()
									.setAction("closeJSZ").putExtra("isRecorderWin",false).buildCommReport());
                            LogUtil.logd("record window not open");
                        }
                    }

				}
			}.addCommand("Interrupt", kws)
					.addCommand("CLOSE_JSZ", "关闭金手指");
			WakeupManager.getInstance().useWakeupAsAsr(acsc);
		}
	};
	
	public static void delInterruptKws() {
		AppLogic.removeBackGroundCallback(mRunnableAddInterruptKws);
		AppLogic.removeBackGroundCallback(mRunnableDelInterruptKws);
		AppLogic.runOnBackGround(mRunnableDelInterruptKws);
	}
	
	public static void addInterruptKws() {
		AppLogic.removeBackGroundCallback(mRunnableAddInterruptKws);
		AppLogic.removeBackGroundCallback(mRunnableDelInterruptKws);
		AppLogic.runOnBackGround(mRunnableAddInterruptKws);
	}

	//打开金手指
	public static void addInterruptJSZKws() {
		AppLogic.removeBackGroundCallback(mRunnableAddInterruptJSZKws);
		AppLogic.runOnBackGround(mRunnableAddInterruptJSZKws);
	}

	// 显示录音窗口
	public static void show() {
        show(false);
    }
    /**
     *
     * @param bRecordState
     */
    public static void show(boolean bRecordState) {
		AsrManager.getInstance().regCommand("GLOBAL_CMD_END_CHAT");
		//通知微信关闭录音窗口
		ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.ctrl.cancelRecord", null, null);
		if (mIsAlreadyOpened) {
			return;
		}
		PermissionUtils.grantOverlayPermission();
		MediaPriorityManager.getInstance().recordMediaToolStatus();
		ReportUtil.doReport(new ReportUtil.Report.Builder().setSessionId().setType("window")
				.setAction("open").buildCommReport());
		mIsAlreadyOpened = true;
		AppLogic.printStatementCycle("show");
		HelpGuideManager.getInstance().notifyRecordShow();
		WinManager.getInstance().getAdapter().show();
        if (bRecordState) {
            refreshState(STATE_RECORD_START);
        } else {
            refreshState(STATE_NORMAL);
        }
		NotificationManager.getInstance().cancel();
		WheelControlManager.getInstance().registerWheelControlListener(mWheelControlListener);
		NavThirdApp.dismiss();
		notifyShow();
		TXZSourceRecorderManager.requestWinRecorder();
		WeixinManager.getInstance().cancelDialog();
		AsrManager.getInstance().isFirstRecord = true;
		AsrManager.getInstance().isShowHelp = true;
	}

	private static OnTXZWheelControlListener mWheelControlListener = new OnTXZWheelControlListener() {

		@Override
		public void onKeyEvent(int eventId) {
			JNIHelper.logd("onKeyEvent:" + eventId);
			WinManager.getInstance().getAdapter().dealKeyEvent(eventId);
		}

	};
	
	public static void close() {
		if (AsrManager.getInstance().mStartFromWeakup) {
			MtjModule.getInstance().event(MtjModule.EVENTID_VOICE_INVALID);
			AsrManager.getInstance().mStartFromWeakup = false;
		}
		if (SearchEditManager.getInstance().isShowing()) {
			ChoiceManager.getInstance().selectBackAsr();
			return;
		}
		HomeControlManager.getInstance().resetState();
		ChoiceManager.getInstance().clearIsSelecting();
		// 取消底层识别
		NativeData.getNativeData(UiData.DATA_ID_VOICE_CANCEL_PARSE);
        dismiss();
        refreshWinState(IRecordView.STATE_WIN_CLOSE);
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		AsrManager.getInstance().cancel();
		AsrManager.getInstance().setCloseRecordWinWhenProcEnd(true);
//		TextManager.getInstance().cancel();
		TextResultHandle.getInstance().cancel();
	}
	
	public static void cancel() {
		if (AsrManager.getInstance().mStartFromWeakup) {
			MtjModule.getInstance().event(MtjModule.EVENTID_VOICE_INVALID);
			AsrManager.getInstance().mStartFromWeakup = false;
		}
		if (SearchEditManager.getInstance().isShowing()) {
			ChoiceManager.getInstance().selectBackAsr();
			return;
		}

		ChoiceManager.getInstance().clearIsSelecting();
		// 取消底层识别
		NativeData.getNativeData(UiData.DATA_ID_VOICE_CANCEL_PARSE);
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		AsrManager.getInstance().cancel();
		AsrManager.getInstance().setCloseRecordWinWhenProcEnd(true);
//		TextManager.getInstance().cancel();
		TextResultHandle.getInstance().cancel();
	}

	private static DelayRunnable mRunnableCloseDelay = new DelayRunnable();

	private static class DelayRunnable implements Runnable {
		private volatile boolean mEnable = true;

		@Override
		public void run() {
			JNIHelper.logd("close delay:" + mEnable);
			if (mEnable) {
				close();
			}
		}

		public void enable(boolean enable) {
			JNIHelper.logd("enable close:" + enable);
			mEnable = enable;
		}

	}

	public static void cancelClose() {
		mRunnableCloseDelay.enable(false);
		AppLogic.removeBackGroundCallback(mRunnableCloseDelay);
	}

	public static void closeDelay(long delay) {

		mRunnableCloseDelay.enable(false);
		AppLogic.removeBackGroundCallback(mRunnableCloseDelay);

		mRunnableCloseDelay.enable(true);
		AppLogic.runOnBackGround(mRunnableCloseDelay, delay);
	}

	// 隐藏录音窗口
	public static void dismiss() {
		AsrManager.getInstance().mKeepGrammar = VoiceData.GRAMMAR_SENCE_DEFAULT;
		AsrManager.getInstance().isFirstRecord = true;

		ChoiceManager.getInstance().clearIsSelecting();
		removeHelpTip();
		if (ReminderManager.getInstance().getIsReminding()) {
			ReminderManager.getInstance().setIsNeedClearReminder(true);
		}
		QiWuTicketManager.getInstance().setBeClearQIWu(true);
		mRunnableCloseDelay.enable(false);
		AppLogic.removeBackGroundCallback(mRunnableCloseDelay);
		if (!mIsAlreadyOpened) {
			return;
		}
		mIsAlreadyOpened = false;
		QiWuTicketReminderView.speakTtsTip();
		ReportUtil.doReport(new ReportUtil.Report.Builder().setSessionId().setType("window")
				.setAction("dismiss").buildCommReport());
		AsrManager.getInstance().mAsrCount = 0;
		ReportUtil.bNeedReset = true;
		MusicFocusManager.getInstance().releaseAudioFocusImmediately();
		delInterruptKws();
		AppLogic.printStatementCycle("dismiss");
		refreshState(STATE_NORMAL);
		WinManager.getInstance().getAdapter().dismiss();
		// GlobalContext.get().unregisterReceiver(mReceiverTest);
		WheelControlManager.getInstance().unregisterWheelControlListener(mWheelControlListener);
		notifyDismiss();
		// 规避录音窗口中，启动唤醒过程中，录音被stop的BUG。(云知声新版本已修正
		// WakeupManager.getInstance().stop();
		// WakeupManager.getInstance().start();

        /*
         * 、ANDYZHAO 2016/06/22 1、防止关闭界面之前，唤醒没有被启动，尤其是在不断点击声控图标的情况下
         * 2、如果唤醒已经被启动了，调用以下接口，并不会执行实际耗时操作。否则，自然需要启动唤醒。
         */

		//【ID1039129】【大众朗逸】调起语音界面出现闪退现象
		//点击返回按钮后，界面已经退出，WakeupManager出现阻塞
		// 重新启动语音后，WakeupManager阻塞消失，继续往下走，导致直接关闭，造成闪退的效果
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				WakeupManager.getInstance().start();
			}
		});

        TXZSourceRecorderManager.releaseWinRecorder();
        LogUtil.d("skyward mVoiceUsage =" + mVoiceUsage + " sHasShowVoiceprintDialog =" + sHasShowVoiceprintDialog);
        if (VoiceprintRecognitionManager.getInstance().isVoiceprintRecognitionEnable() && !sHasShowVoiceprintDialog) {
            ++mVoiceUsage;
            if (mVoiceUsage >= 2) {
                AppLogic.removeBackGroundCallback(mShowVoiceprintDialogRunnable);
                AppLogic.runOnBackGround(mShowVoiceprintDialogRunnable, 10000);
            }
        }
    }

    private static boolean sHasShowVoiceprintDialog = PreferenceUtil.getInstance().getVoiceRecognitionHadShowTips();
    private static Runnable mShowVoiceprintDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (RecorderWin.isOpened()) {
                AppLogic.removeBackGroundCallback(mShowVoiceprintDialogRunnable);
                AppLogic.runOnBackGround(mShowVoiceprintDialogRunnable, 10000);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(GlobalContext.get());
                View rootView = LayoutInflater.from(GlobalContext.get()).inflate(R.layout.dialog_voice_print, null);
                final AlertDialog alertDialog = builder.setView(rootView).create();
                alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                rootView.findViewById(R.id.bt_create).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent();
                            intent.setAction("com.txznet.setting.voiceprint");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (isIntentAvailable(GlobalContext.get(), intent)) {
                                GlobalContext.get().startActivity(intent);
                            } else {
                                LogUtil.e("activity not exist");
                            }
                        } catch (Exception e) {
                            LogUtil.loge("start setting Activity error : " + e.getMessage());
                        }
                        alertDialog.dismiss();
                        TtsManager.getInstance().cancelSpeak(mVoiceprintDialogTtsId);
                    }
                });
                rootView.findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        TtsManager.getInstance().cancelSpeak(mVoiceprintDialogTtsId);
                    }
                });
                alertDialog.show();
                String name = "XX";
                String[] nicks = WakeupManager.getInstance().getDeviceNicks();
                if (nicks != null && nicks.length > 0) {
                    name = nicks[0];
                }
                mVoiceprintDialogTtsId = TtsManager.getInstance().speakText("HI，我是你的语音助手" + name + "，创建声纹，我可以为你提供专属服务哦！");
                sHasShowVoiceprintDialog = true;
                PreferenceUtil.getInstance().setVoiceRecognitionHadShowTips(sHasShowVoiceprintDialog);
            }
        }
    };
    private static int mVoiceprintDialogTtsId = -1;

    public static boolean isIntentAvailable(Context context, Intent intent) {
        final android.content.pm.PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                android.content.pm.PackageManager.GET_ACTIVITIES);
        return list.size() > 0;
    }

    /**
     * 刷新窗口状态
     */
    public static void refreshWinState(int state) {
        if (WinManager.getInstance().isSupportMoreRecordState()) {
            refreshState(state);
        }
    }
    private static int mVoiceUsage = 0;
	// 更新录音窗口状态
	public static void refreshState(int state) {
		LogUtil.logd("RecorderWin state:" + state);
		WinManager.getInstance().getAdapter().refreshState("record", state);
	}

	// 通知音量改变 0 - 20
	public static void notifyVolumeChanged(int volume) {
		WinManager.getInstance().getAdapter().refreshVolume(volume);
	}

	/**
	 * 更新进度条 progress 0-100 progress < 0 时隐藏进度条
	 */
	public static void refreshProgressBar(int progress, int selection) {
		WinManager.getInstance().getAdapter().refreshProgress(progress, selection);
	}

	/// 更新选中的item
	public static void refreshItemSelect(int selection) {
		WinManager.getInstance().getAdapter().refreshItemSelect(selection);
	}

	/**
	 * 隐藏进度条
	 */
	public static void hideProgressBar(int selection) {
		refreshProgressBar(-1, selection);
	}
	
	private static HashMap<String, String> realFictitiousMap;

	public static void setRealFictitiousCmds(String json){
		if (realFictitiousMap==null) {
			synchronized (RecorderWin.class) {
				if (realFictitiousMap == null) {
					realFictitiousMap = new HashMap<String, String>();
				}
			}
		}
		try {
			JSONArray realJry = new JSONArray(json);
			for (int i = 0; i < realJry.length(); i++) {
				JSONObject job = realJry.getJSONObject(i);
				if (job != null) {
					realFictitiousMap.put(job.getString("fictitious"), job.getString("real"));
				}
			}
		} catch (Exception e) {
			
		}
	}
	
	public static void removeRealFictitiousCmds(String json) {
		if (realFictitiousMap == null) {
			return;
		}
		try {
			JSONArray jry = new JSONArray(json);
			for (int i = 0; i < jry.length(); i++) {
				realFictitiousMap.remove(jry.getString(i));
			}
		} catch (Exception e) {
			
		}
	}
	
	// 发送消息
	private static void sendMsg(int owner, String text) {
		if (text != null) {
			text = text.replace("歪伐", "WIFI");
			text = text.replace("歪法", "WIFI");
			text = text.replace("埃服埃牧", "FM");
			text = text.replace("埃服埃", "FM");
			text = text.replace("修改加的地址", "修改家的地址");
			text = text.replace("修改加的位置", "修改家的位置");
			text = text.replace("咖life", "Carlife");
			text = text.replace("咖赖夫", "Carlife");
			text = text.replace("卡乐夫", "Carlife");
			text = text.replace("喀腊", "Carlife");
		}
		if (text != null) {
			if (realFictitiousMap!=null&&realFictitiousMap.size()>0) {
				for (Entry<String, String> entry : realFictitiousMap.entrySet()) {
					text = text.replace(entry.getKey(), entry.getValue());
				}
			}
		}
		if(text != null && text.contains("</font>")){
			if(WinManager.getInstance().isSupportNewContent()){
				WinManager.getInstance().getAdapter().addData(generateHighLight(text));
			}else{
				text = Html.fromHtml(text).toString();
				WinManager.getInstance().getAdapter().addMsg(owner, text);
			}
		}else if(text != null && isShowInterruptTips(owner, text) && WinManager.getInstance().isSupportNewContent()){
			WinManager.getInstance().getAdapter().addData(generateTipsSysText(text));
		}else{
			WinManager.getInstance().getAdapter().addMsg(owner, text);
		}
	}
	
	/**
	 * 构建带打断tips的系统文本格式
	 * @param text
	 * @return
	 */
	private static String generateTipsSysText(String text) {
		if(text == null){
			text = "";
		}
		try {
			JSONObject json = new JSONObject();
			json.put("type", 5);
			json.put("text", text);
			String[] tips = WakeupManager.getInstance().getInterruptTips();
			if(tips == null || tips.length == 0){
				json.put("tips", NativeData.getResString("RS_VOICE_SHOW_INTERRUPT_TIPS")+"<b><font color='#00B8FE'>“停止播报”</font></b>");
			}else{
				StringBuilder stringBuilder = new StringBuilder();
				int i = 0;
				for (; i < tips.length - 1; i++) {
					stringBuilder.append("“");
					stringBuilder.append(tips[i]);
					stringBuilder.append("”,");
				}
				stringBuilder.append("“");
				stringBuilder.append(tips[i]);
				stringBuilder.append("”");
				json.put("tips", NativeData.getResString("RS_VOICE_SHOW_INTERRUPT_TIPS")+"<b><font color='#00B8FE'>"+stringBuilder+"</font></b>");
			}
			return json.toString();
		} catch (JSONException e) {
		}
		return null;
	}

	/**
	 * 是否展示文本过长，打断词的提示
	 * @param text 
	 * @param owner 
	 * @return
	 */
	private static boolean isShowInterruptTips(int owner, String text){
		if(TextUtils.isEmpty(text)){
			return false;
		}
		int count = WakeupManager.getInstance().mInterruptTipsCount;
		return enableInterruptTips_SDK&&enableInterruptTips && !ProjectCfg.needStopWkWhenTts() && (owner == ChatMessage.OWNER_SYS) && text.length() > 40 && (count < 3);
	}

	/**
	 * 判断是否支持停止播报功能
	 * @return
	 */
	public static boolean isSupportInterruptTips() {
		String[] kws = WakeupManager.getInstance().getInterruptTips();
		if (kws == null || kws.length == 0) {
			return false;
		}
		if (!enableInterruptTips_SDK) {
			return false;
		}

		return enableInterruptTips && !ProjectCfg.needStopWkWhenTts();
	}

	private static String generateHighLight(String text) {
		if(text == null){
			text = "";
		}
		try {
			JSONObject json = new JSONObject();
			json.put("type", 4);
			json.put("rawText", text);
			return json.toString();
		} catch (JSONException e) {
		}
		return text;
	}

	public static void showUserText() {
		if (mLastUserText != null) {
			sendMsg(RecorderWin.OWNER_USER, mLastUserText);
			mLastUserText = null;
		}
	}

    public static void showHelpTips() {
        if (enableShowHelpTips()) {//帮助界面和反馈结束提示打开时不展示
            LogUtil.logd("start show help  showHelpTips....");
            //WinHelpManager.getInstance().loadHelpTips();
            removeHelpTip();
            isCancelUpdate = false;
            AppLogic.runOnUiGround(updateHelpMsg);
        }
    }

	private static boolean enableShowHelpTips() {
		return !WinHelpDetailTops.getInstance().isSelecting()
				&& FeedbackManager.getInstance().isCanShowHelpTips()
				&& WinManager.getInstance().canShowHelpTips()
				&& WinHelpManager.getInstance().isNotNullHelpTips();
	}

	public static void onEndBeep(){
		AppLogic.runOnBackGround(showHelpRunnable,2000);
	}

    static Runnable showHelpRunnable = new Runnable() {
        @Override
        public void run() {
            if (AsrManager.getInstance().isShowHelp && AsrManager.getInstance().isFirstRecord) {
                    LogUtil.logd("start show help  showHelpTips....");
                    showHelpTips();
            }
        }
    };

    public static void removeHelpTip() {
        isCancelUpdate = true;
        AppLogic.removeUiGroundCallback(updateHelpMsg);
        AppLogic.removeBackGroundCallback(showHelpRunnable);
    }

	private static boolean isCancelUpdate = false;

	public static Runnable updateHelpMsg = new Runnable() {
		@Override
		public void run() {
			if(!enableShowHelpTips()) {
				return;
			}
			JSONBuilder jsonBuilder = new JSONBuilder();
			jsonBuilder.put("action","addMsg");
			jsonBuilder.put("type",6);
			jsonBuilder.put("title",NativeData.getResString("RS_HELP_TIP"));

			JSONArray jsonArray = WinHelpManager.getInstance().getHelpTips();
			LogUtil.d("showHelp updateHelpMsg.");

			jsonBuilder.put("data", jsonArray);

			if (isCancelUpdate) {
				removeHelpTip();
				return;
			} else {
				WinManager.getInstance().getAdapter().addData(jsonBuilder.toString());
			}
			AppLogic.removeUiGroundCallback(updateHelpMsg);
			isCancelUpdate = false;
			AppLogic.runOnUiGround(updateHelpMsg, 1500);
		}
	};

	public static void addSystemMsg(String text) {
		if (!mIsAlreadyOpened) {
			LogUtil.logd("addSystemMsg alreadyOpen false");
			return;
		}

		showUserText();
		if (mHideLastSystemText)
			mHideLastSystemText = false;
		// else
		sendMsg(RecorderWin.OWNER_SYS, text);
	}

	public static void showPartMsg(String text) {
		//当显示帮助的时候，先将帮助的提示去掉
		if (!isCancelUpdate) {
			removeHelpTip();
		}

		if (DebugCfg.TYPING_EFFECT_DEBUG) {
			LogUtil.logd("partialResult: " + text);
		}

		if (!isSelecting() && ProjectCfg.enableTypingEffect()) {
			sendMsg(RecorderWin.OWNER_USER_PART, text);
		}

		//如果需要外放实时打字的流式文本，通过场景工具外放出去
		if (SenceManager.getInstance().isEnablePartScene()) {
			JSONBuilder root = new JSONBuilder();
			root.put("scene", "part");
			root.put("action", "part");
			root.put("text", text);
			SenceManager.getInstance().procSenceByRemote("part", root.toBytes());
		}
	}

	// 发送选择器的列表数据
	public static void sendSelectorList(String dataJson) {
//		showUserText();
		WinManager.getInstance().getAdapter().addListMsg(dataJson);
	}

	// 显示股票信息
	public static void showStockInfo(byte[] info) {
		mHideLastSystemText = true;
		WinManager.getInstance().getAdapter().showStock(info);
		ReportUtil.doReport(new ReportUtil.Report.Builder().setType("stock").setAction("show")
				.setSessionId().buildCommReport());
	}
	
	// 显示地图信息
	public static void showMapInfo(byte[] info) {
		mHideLastSystemText = true;
		WinManager.getInstance().getAdapter().showMap(info);
	}
    public static void showConstellationFortune(String data) {
        showData(data);
		mLastUserText = null;
    }

    public static void showConstellationMatching(String data) {
        showData(data);
		mLastUserText = null;
    }

    // 显示天气信息
    public static void showWeatherInfo(byte[] info) {
        mHideLastSystemText = true;
        WinManager.getInstance().getAdapter().showWeather(info);
        ReportUtil.doReport(new ReportUtil.Report.Builder().setType("weather").setAction("show")
                .setSessionId().buildCommReport());
    }

    // 显示其他类型的数据
    public static void showData(String data) {
        WinManager.getInstance().getAdapter().addData(data);
    }

    // 发送数据给界面
    public static void sendInformation(String data) {
        WinManager.getInstance().getAdapter().sendInformation(data);
    }

	public static void addPluginCommandProcessor() {
		//2.5.1版本添加
		PluginManager.addCommandProcessor("txz.recorderwin.", new CommandProcessor() {

			@Override
			public Object invoke(String command, Object[] args) {
				if (TextUtils.equals(command, "speakText")) {
					if (args.length > 4) {
						if (args[4] instanceof Boolean) {
							AsrManager.getInstance().setNeedCloseRecord((Boolean) args[4]);
						}
					}
					if (args.length > 3) {
						if (args[0] instanceof String && args[1] instanceof Boolean && args[2] instanceof Boolean) {
							Runnable runnable = null;
							if (args[3] != null && args[3] instanceof Runnable) {
								runnable = (Runnable) args[3];
							}
							speakTextWithClose((String) args[0], (Boolean) args[1], (Boolean) args[2], runnable);
						}
					}
				}else if(TextUtils.equals(command, "close")){
					close();
				}else if(TextUtils.equals(command, "show")){
					show();
				}
				return null;
			}
		});
	}

	/**
	 * 提示内容，并不关闭界面
	 * 
	 * @param text
	 * @param onEnd
	 * @return
	 */
	public static int speakText(String text, final Runnable onEnd) {
		AsrManager.getInstance().setNeedCloseRecord(false);
		return speakTextWithClose(text, true, onEnd);
	}
	
	public static int speakMsg(String text, Runnable onEnd) {
		return speakText(text, onEnd);
	}

	public static int speakTextWithClose(String text, final Runnable onEnd) {
		return speakTextWithClose(text, true, onEnd);
	}

	public static int speakTextWithClose(String text, final boolean needAsr,
			final Runnable onEnd) {
		return speakTextWithClose(text, needAsr, true, onEnd);
	}
	public static int speakTextWithClose(String text, final boolean needAsr,final boolean isCancleExecute,
			final Runnable onEnd) {
		mRunnableCloseDelay.enable(false);
		AppLogic.removeBackGroundCallback(mRunnableCloseDelay);
		if (!TextUtils.isEmpty(text)) {
			addSystemMsg(text);
			if(text.indexOf("<font") != -1){
				text = Html.fromHtml(text).toString();
			}
		}
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.getInstance().speakText(text,
				PreemptType.PREEMPT_TYPE_NEXT, new ITtsCallback() {
					@Override
					public void onCancel() {
						if (onEnd != null&&isCancleExecute) {
							onEnd.run();
						}
					}

					@Override
					public void onSuccess() {
						boolean b = AsrManager.getInstance().needCloseRecord();
						JNIHelper.logd("NeedCloseRecord: " + b);
						if (b) {
							dismiss();
						} else if (isOpened())
							if (needAsr) {
								if (InterruptTts.getInstance().isInterruptTTS()) {
									JNIHelper.logd(";isBusy:"+AsrManager.getInstance().isBusy()
											+";AsrId:"+AsrManager.getInstance().getTtsId()
											+";mTaskId:"+mTaskId
											+";isBeginSpeech:"+InterruptTts.getInstance().isBeginSpeech());
									//如果当前正在识别的id和本次tts的id一致，并且当前识别没有检测到开始说话，则重启识别
									if (AsrManager.getInstance().isBusy()) {
										if (AsrManager.getInstance().getTtsId() == mTaskId) {
											if (!InterruptTts.getInstance().isBeginSpeech()) {
												AsrManager.getInstance().cancel();
											}
										}
									}
								}
								AsrManager.getInstance().start(needAsr, true);
							}
						if (onEnd != null) {
							onEnd.run();
						}
					}

					@Override
					public void onError(int iError) {
						dismiss();
					}

					@Override
					public boolean isNeedStartAsr() {
						boolean b = AsrManager.getInstance().needCloseRecord();
						if (!b && isOpened()){
							if (needAsr) {
								return true;
							}
						}
						return false;
					}
					@Override
		            public void onEnd() {
		                super.onEnd();
		                refreshWinState(IRecordView.STATE_SPEAK_END);
		            }

		            @Override
		            public void onBegin() {
		                super.onBegin();
		                refreshWinState(IRecordView.STATE_SPEAK_START);
		            }
				});
		return mSpeechTaskId;
	}
	
	public static int speakTextNotEqualsDisplay(String spk, String display) {
		return speakTextNotEqualsDisplay(spk,display,null);
	}

	public static int speakTextNotEqualsDisplay(String spk, String display,final Runnable onEnd) {
		if (!TextUtils.isEmpty(display)) {
			addSystemMsg(display);
		}
		if(!TextUtils.isEmpty(spk)&&spk.indexOf("<font") != -1){
			spk = Html.fromHtml(spk).toString();
		}
		int taskId = TtsManager.getInstance().speakText(spk, PreemptType.PREEMPT_TYPE_NEXT, new TtsUtil.ITtsCallback() {

			@Override
			public void onSuccess() {
				boolean b = AsrManager.getInstance().needCloseRecord();
				if (b) {
					dismiss();
				} else if (isOpened()) {
					AsrManager.getInstance().start(true, true);
				}
				if (onEnd != null) {
					onEnd.run();
				}
			}

			@Override
			public void onError(int iError) {
				dismiss();
			}

			@Override
			public boolean isNeedStartAsr() {
				boolean b = AsrManager.getInstance().needCloseRecord();
				if (b) {
				} else if (isOpened()) {
					return true;
				}
				return false;
            }

            @Override
            public void onEnd() {
                super.onEnd();
                refreshWinState(IRecordView.STATE_SPEAK_END);
            }

            @Override
            public void onBegin() {
                super.onBegin();
                refreshWinState(IRecordView.STATE_SPEAK_START);
            }
		});
		synchronized (sSpeakIds) {
			sSpeakIds.add(taskId);
		}

		return taskId;
	}

	static List<Integer> sSpeakIds = new ArrayList<Integer>();

	public static int mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;

	public static void setNetworkTipsVisibility(boolean visible) {
		AppLogic.runOnUiGround(new Runnable1<Boolean>(visible) {
			@Override
			public void run() {
			}
		}, 0);
	}

	public static void setStateText(String txt) {
		AppLogic.runOnUiGround(new Runnable1<String>(txt) {
			@Override
			public void run() {
			}
		}, 0);
	}

	private static Intent sendRecStatusBroadIntent;
	public final static String RECORD_STATUS_CHANGED_ACTION = "com.txznet.txz.record.STATUS_CHANGED";
	private static final String TAG = "Core:Win:";
	public static String RECORD_STATUS_KEY = "record_status";
	public static int RECORD_START = 0;
	public static int RECORD_RECORNIZE = 1;
	public static int RECORD_END = 2;
	private static STATE mCurRecordStatus; //当前录音状态

	public static void recStatusHelper(int recStatus, String recStatusStr) {
		sendRecStatusBroadIntent = new Intent(RECORD_STATUS_CHANGED_ACTION,
				null);
		sendRecStatusBroadIntent.putExtra(RECORD_STATUS_KEY, recStatus);
		AppLogic.getApp().sendBroadcast(sendRecStatusBroadIntent);
		JNIHelper.logi(RECORD_STATUS_CHANGED_ACTION + "当前录音状态:" + recStatusStr
				+ ":" + recStatus);
	}

	public static void setState(STATE state) {
		mCurRecordStatus = state;
		switch (state) {
		case STATE_START:
			RecorderWin.refreshState(RecorderWin.STATE_RECORD_START);
			recStatusHelper(RECORD_START, "RECORD_START");
			break;
		case STATE_RECORD:
		case STATE_WAKEUP_RECORD:
			RecorderWin.refreshState(RecorderWin.STATE_RECORD_START);
			recStatusHelper(RECORD_START, "RECORD_START");
			break;
		case STATE_LISTEN:
			break;
		case STATE_RECOGONIZE:
			RecorderWin.refreshState(RecorderWin.STATE_RECORD_END);
			recStatusHelper(RECORD_RECORNIZE, "RECORD_RECORNIZE");
			break;
		case STATE_PROCESSING:
			RecorderWin.refreshState(RecorderWin.STATE_RECORD_END);
			break;
		case STATE_END:
			RecorderWin.refreshState(RecorderWin.STATE_NORMAL);
			recStatusHelper(RECORD_END, "RECORD_END");
			break;
		}
	}

	public static boolean isOpened() {
		return mIsAlreadyOpened;
	}

	static Set<Runnable> mCloseRunnables = new HashSet<Runnable>();

	public static void addCloseRunnable(Runnable r) {
		synchronized (mCloseRunnables) {
			mCloseRunnables.add(r);
		}
	}
	
	
	private static void pauseRecord(){
		sPauseRecordWin = true;
		AsrManager.getInstance().mSenceRepeateCount = -1;
		ChoiceManager.getInstance().stopTtsAndAsr();
		if (mSpeechTaskId != TtsManager.INVALID_TTS_TASK_ID) {
			TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
			mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
			// 上报数据
			ReportUtil.doReport(
					new ReportUtil.Report.Builder().setType("core").setAction("interrupt_tts").buildTouchReport());
		}
  		FilmManager.getInstance().cancelSpeak();
		if (mWillStartAsrAfterTts) {
			AsrManager.getInstance().start(true,true);
			mWillStartAsrAfterTts = false;
		} else if (AsrManager.getInstance().isBusy()) {
			switch (AsrManager.getInstance().mState) {
			case STATE_IDLE:
				break;
			case STATE_PLAYING_START_TIP_VOICE:
				break;
			case STATE_RECOGONIZING:
				break;
			case STATE_RECORDING:
				AsrManager.getInstance().stop();
				// 上报数据
				ReportUtil.doReport(
						new ReportUtil.Report.Builder().setType("core").setAction("interrupt_asr").buildTouchReport());
				break;
			case STATE_STARTING_SCO:
				break;
			default:
				break;
			}
		}
	}
	
	public static boolean isSelecting() {
		return ChoiceManager.getInstance().isSelecting();
	}
	
	private static void startRecord() {
		// 取消正在请求的延时任务
		NavManager.getInstance().cancelAllPoiSearch();
		MusicManager.getInstance().cancelSearchMedia();
		AudioManager.getInstance().cancelAllRequest();
		LocationManager.getInstance().cancelReverseGeo();
		LocationManager.getInstance().cancelQueryTraffic();
		LocationManager.getInstance().cancelRequestGeoCode();

		synchronized (mCloseRunnables) {
			for (Runnable r : mCloseRunnables) {
				AppLogic.runOnBackGround(r, 0);
			}
			mCloseRunnables.clear();
		}
		
		AsrManager.getInstance().setCloseRecordWinWhenProcEnd(true);
		AsrManager.getInstance().start(true, true);
	}
	
	public static boolean sPauseRecordWin = false;

	/*
	 * 处理录音对话窗口的UI事件 event: record.ui.event.item.selected data: index event:
	 * record.ui.event.button.cancel data: null event: record.ui.event.button.ok
	 * data: null event: record.ui.event.button.back data: null event:
	 * record.ui.event.button.pause data: null
	 */
	public static byte[] dealRecorderUIEvent(String event, byte[] data) {
		JNIHelper.logd("dealRecorderUIEvent:" + event);
		if (event.equals("txz.record.ui.event.button.cancel")) {
			ChoiceManager.getInstance().selectCancel(AbstractChoice.SELECT_TYPE_CLICK);
			return null;
		} else if (event.equals("txz.record.ui.event.button.ok")) {
			ChoiceManager.getInstance().selectSure();
			return null;
		} else if (event.equals("txz.record.ui.event.button.back")) {
			ChoiceManager.getInstance().clearIsSelecting();
			return null;
		} else if (event.equals("txz.record.ui.event.button.record.back")) {

			if (WinHelpDetailSelector.getInstance().isSelecting()) {//先判断三级界面
				WinHelpDetailSelector.getInstance().onBackPress(false);
			} else if (ChoiceManager.getInstance().isSelecting()) {//再判断选择列表
				ChoiceManager.getInstance().clearIsSelecting();
				RecorderWin.open();
			} else {
				GlobalContext.get().sendBroadcast(new Intent("com.txznet.txz.record.dismiss.button"));
				close();
			}
			return null;
		} else if (event.equals("txz.record.ui.event.button.help.back")) {
			if (WinHelpDetailSelector.getInstance().isSelecting()) {
				WinHelpDetailSelector.getInstance().onBackPress(false);
			}
		}else if (event.equals("txz.record.ui.event.button.pause")) {
			pauseRecord();
		} else if ("txz.record.ui.event.button.record".equals(event)) {
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType("touch")
					.setAction("button").setSessionId().buildCommReport());
			AppLogic.runOnBackGround(new Runnable() {
				@Override
				public void run() {
					if ((mCurRecordStatus == STATE.STATE_END||mCurRecordStatus == STATE.STATE_WAKEUP_RECORD )&& (!isSelecting() || ChoiceManager.getInstance().isCoexistAsrAndWakeup())) {// 录音结束，正在播放tts中，且不是处于选择当中则打断当前播报，开始下一次录音
						startRecord();
					} else if (isSelecting() && ChoiceManager.getInstance().isCoexistAsrAndWakeup() && !AsrManager.getInstance().isBusy()) {
						startRecord();
					} else {
						pauseRecord();
					}
				}
			});
		} else if (event.equals("txz.record.ui.event.dismiss")) {
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					mRunnableCloseDelay.enable(false);
					AppLogic.removeBackGroundCallback(mRunnableCloseDelay);
					AsrManager.getInstance().mUnknowCount = 0;
					AsrManager.getInstance().mEmptyCount = 0;
					AsrManager.getInstance().unregCommand("GLOBAL_CMD_END_CHAT",
							"GLOBAL_CMD_END_CHAT_BY_QUESTION");
					ChoiceManager.getInstance().clearIsSelecting();
					RecorderWin.close();
				}
			});
		} else if (event.equals("txz.record.ui.status.isShowing")) {
			return Boolean.valueOf(isOpened()).toString().getBytes();
		} else if (event.equals("txz.record.ui.event.clearProgress")) {
			ChoiceManager.getInstance().clearProgress();
			return null;
		} else if (event.equals("txz.record.ui.event.button.setting")) {
			AsrManager.getInstance().cancel();
			TtsManager.getInstance().pause();
			ChoiceManager.getInstance().clearIsSelecting();
			RecorderWin.openVoiceSetting(false);
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType("touch")
					.setAction("setting").setSessionId().buildCommReport());
			return null;
		} else if(event.equals("txz.record.ui.event.background")) {//对话界面背景
			LogUtil.logd("window touch background");
			ReportUtil.doReport(new ReportUtil.Report.Builder().setSessionId()
					.setType("touch").setAction("background").buildCommReport());
		}
		AppLogic.runOnBackGround(new Runnable2<String, byte[]>(event, data) {

			@Override
			public void run() {
				ChoiceManager.getInstance().invokeCommand("", mP1, mP2);
			}
		});
		
		return null;
	}
	
	public static WinConfirm mInstallSettingDialog;
	public static void dismissInstallSettingDialog() {
	    if (mInstallSettingDialog != null && mInstallSettingDialog.isShowing()) {
	        mInstallSettingDialog.dismiss("logic");
	    }
	    mInstallSettingDialog = null;
	}
	
	public static void openVoiceSetting(boolean fromVoice) {
		Runnable mVoiceSettingRunnable;
		if (!TextUtils.isEmpty(ProjectCfg.getSDKSettingPackage())) {
			mVoiceSettingRunnable = new Runnable() {
				@Override
				public void run() {
					PackageManager.getInstance().openApp(ProjectCfg.getSDKSettingPackage());
					dismiss();
				}
			};
			if (fromVoice) {
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_ANSWER_OPEN_SETTING"),
						mVoiceSettingRunnable);
			} else {
				mVoiceSettingRunnable.run();
			}
			return;
		} else if (PackageManager.getInstance().mInstalledSetting) {
			mVoiceSettingRunnable = new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent();
					intent.setPackage("com.txznet.txzsetting");
					intent.setAction(Intent.ACTION_MAIN);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra(PackageManager.KEY_START_FROM_WHERE, "txz");
					intent.setComponent(
							new ComponentName("com.txznet.txzsetting", "com.txznet.txzsetting.activity.MainActivity"));
					intent.setData(Uri.parse("txznet://com.txznet.txz.txzsetting"));
					GlobalContext.get().startActivity(intent);
					dismiss();
				}
			};
			if (fromVoice) {
				try {
					RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_ANSWER_OPEN_SETTING"),
							mVoiceSettingRunnable);
					return;
				} catch (Exception e) {
				}
			} else {
				try {
					mVoiceSettingRunnable.run();
					return;
				} catch (Exception e) {
				}
			}
		} else {
			try {
			    TtsManager.getInstance().speakText("如需启用设置功能，请先安装语音设置应用。");
			    if (mInstallSettingDialog != null && mInstallSettingDialog.isShowing()) {
			        dismiss();
			        return;
                }
			    WinConfirm.WinConfirmBuildData buildData = new WinConfirmBuildData();
			    buildData.setMessageText("如需启用设置功能，请先安装语音设置应用。").setMessageAllowScroll(true).setLeftText("安装（免流量）").setRightText("取消");
			    mInstallSettingDialog = new WinConfirm(buildData) {
	                @Override
	                public void onClickOk() {
	                    AppLogic.runOnBackGround(new Runnable() {
                            
                            @Override
                            public void run() {
                                if (copyApkFromAssets(GlobalContext.get(), "data/TXZSetting.apk", FilePathConstants.TXZ_SETTING_PATH)) {
                                    PackageInstaller.installApkByIntent(FilePathConstants.TXZ_SETTING_PATH);                                
                                }
                            }
                        });
	                }

	                @Override
	                public void onClickCancel() {
	                }

	                @Override
	                public void onBackPressed() {
	                    this.dismiss("back pressed");
	                }

					@Override
					public String getReportDialogId() {
						return "setting_enable";
					}
	            };
	            mInstallSettingDialog.showImediately();
	            dismiss();
				return;
			} catch (Exception e) {
			}
		}
		RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"), null);
	}
	
	public static boolean copyApkFromAssets(Context context, String fileName, String path) {  
        boolean copyIsFinish = false;  
        try {  
            InputStream is = context.getAssets().open(fileName);  
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();  
            FileOutputStream fos = new FileOutputStream(file);  
            byte[] temp = new byte[1024];  
            int i = 0;  
            while ((i = is.read(temp)) > 0) {  
                fos.write(temp, 0, i);  
            }  
            fos.close();  
            is.close();  
            copyIsFinish = true;  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return copyIsFinish;  
    }
	
	public static boolean isNotifyRecordShow;

	public static void notifyShow() {
		isNotifyRecordShow = true;
		JNIHelper.logd(TAG+"RecorderWin winUI show");
		Intent intent = new Intent("com.txznet.txz.record.show");
		GlobalContext.get().sendBroadcast(intent);
		OBSERVABLE.notifyShow();
		
		SDKFloatView.getInstance().recordState(true);
	}

	public static void notifyDismiss() {
		isNotifyRecordShow = false;
		JNIHelper.logd(TAG + "winUI dismiss");
		Intent intent = new Intent("com.txznet.txz.record.dismiss");
		GlobalContext.get().sendBroadcast(intent);
		OBSERVABLE.notifyDismiss();
		HelpGuideManager.getInstance().notifyRecordDismiss();
		AsrManager.getInstance().mUnknowCount = 0;
		AsrManager.getInstance().mEmptyCount = 0;
		AsrManager.getInstance().unregCommand("GLOBAL_CMD_END_CHAT",
				"GLOBAL_CMD_END_CHAT_BY_QUESTION");

		BeepPlayer.cancel();

		{
			// 声控消失取消延时任务
			NavManager.getInstance().disMiss();
//			if(DebugCfg.ROADTRAFFIC_ENABLE_DEBUG){
				RoadTrafficManager.getInstance().cancleInqury();				
//			}
			MusicManager.getInstance().cancelSearchMedia();
			AudioManager.getInstance().cancelAllRequest();
			LocationManager.getInstance().cancelReverseGeo();
			LocationManager.getInstance().cancelQueryTraffic();
			LocationManager.getInstance().cancelRequestGeoCode();
			TextResultHandle.getInstance().onDismiss();
			SimManager.getInstance().cancel();
			
			synchronized (mCloseRunnables) {
				for (Runnable r : mCloseRunnables) {
					AppLogic.runOnBackGround(r, 0);
				}
				mCloseRunnables.clear();
			}
			
			synchronized (sSpeakIds) {
				for (int taskId : sSpeakIds) {
					if (taskId != TtsManager.INVALID_TTS_TASK_ID) {
						TtsManager.getInstance().cancelSpeak(taskId);
					}
				}
				sSpeakIds.clear();
			}
			
			InterruptTts.getInstance().endInterruptWakeup();
			
			MusicManager.getInstance().setStartAsrMusicTool("");
			ChoiceManager.getInstance().clearIsSelecting();
			removeHelpTip();
		}

		SDKFloatView.getInstance().recordState(false);

	}
	
	public static void notifyDismissWithoutBroadcast() {
		OBSERVABLE.notifyDismiss();

		{
			// 声控消失取消延时任务
			NavManager.getInstance().cancelAllPoiSearch();
			MusicManager.getInstance().cancelSearchMedia();
			AudioManager.getInstance().cancelAllRequest();
			LocationManager.getInstance().cancelReverseGeo();
			LocationManager.getInstance().cancelQueryTraffic();
			LocationManager.getInstance().cancelRequestGeoCode();
			synchronized (mCloseRunnables) {
				for (Runnable r : mCloseRunnables) {
					AppLogic.runOnBackGround(r, 0);
				}
				mCloseRunnables.clear();
			}
		}
	}

	private static class StartAsrReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				String hintTxt = bundle.getString(START_ASR_HINT);
				int grammar = bundle.getInt(START_ASR_GRAMMAR, AsrManager
						.getInstance().getCurrentGrammarId());
				if (bundle.getBoolean(START_KEEP_GRAMMAR, true)) {
					AsrManager.getInstance().mKeepGrammar = grammar;
				}
				int intentFlags = intent.getIntExtra(START_ASR_INTENT_FLAGS, 0);
				if (intentFlags != 0) {
					WinManager.getInstance().setIntentFlags(intentFlags);
				}
				open(hintTxt, grammar);
			} else {
				open();
			}
		}
	}
}

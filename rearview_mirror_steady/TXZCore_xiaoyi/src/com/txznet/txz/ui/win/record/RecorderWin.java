package com.txznet.txz.ui.win.record;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import com.txz.ui.data.UiData;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.selector.Selector;
import com.txznet.txz.component.selector.SelectorHelper;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.audio.AudioManager;
import com.txznet.txz.module.call.CallSelectControl;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.text.TextManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.nav.SearchEditDialog;
import com.txznet.txz.util.runnables.Runnable1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Observable;
import android.os.Bundle;

public class RecorderWin {
	public static final String START_ASR_HINT = "hinTxt";
	public static final String START_ASR_GRAMMAR = "grammar";
	public static final String START_KEEP_GRAMMAR = "keep";
	public static final String START_ASR_ACTION = "com.txznet.txz.asr.start.action";

	static {
		GlobalContext.get().registerReceiver(new StartAsrReceiver(),
				new IntentFilter(START_ASR_ACTION));
	}

	public static class StatusObervable extends
			Observable<StatusObervable.StatusObserver> {
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
	public static final int AudioSence = 4;

	public static final int STATE_NORMAL = 0; // 正常状态，显示一个录音图标
	public static final int STATE_RECORD_START = 1; // 录音开始，显示一个声纹动画
	public static final int STATE_RECORD_END = 2; // 录音结束，显示一个处理中动画

	public static final int OWNER_SYS = 0; // 系统发起
	public static final int OWNER_USER = 1; // 用户发起

	private static boolean mIsAlreadyOpened = false;

	private static String mLastUserText = null;

	private static boolean mHideLastSystemText = false;

	public static void setLastUserText(String s) {
		mLastUserText = s;
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

	public static void open() {
		open(AsrManager.getInstance().getCurrentGrammarId());
	}

	public static void open(int grammar) {
		String txt;
		if (mFirst) {
			txt = NativeData.getResString("RS_RECORD_DI");
			mFirst = false;
		} else {
			txt = NativeData.getResString("RS_VOICE_ASR_START_HINT");
		}
		open(txt, grammar, null);
	}
	
	public static void open(String txt) {
		open(txt, AsrManager.getInstance().getCurrentGrammarId(), null);
	}

	public static void open(String txt, final int grammar) {
		open(txt, grammar, null);
	}

	public static void open(String txt, final int grammar,
			final Runnable speakEnd) {
		Selector.clearSelectorWakeup();
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				Selector.closeAllWin();
			}
		}, 0);

		AsrUtil.openRecordWinLock();

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
		show();
		if (txt.isEmpty()) {
			AsrManager.getInstance().setCloseRecordWinWhenProcEnd(true);
			AsrManager.getInstance().start(true, grammar);
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
						mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
					}

					@Override
					public void onSuccess() {
						AsrManager.getInstance().setCloseRecordWinWhenProcEnd(
								true);
						AsrManager.getInstance().start(true, grammar);
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
				});
	}

	// 显示录音窗口
	public static void show() {
		JNIHelper.logd("RecorderWin show");
		AsrManager.getInstance().regCommand("GLOBAL_CMD_END_CHAT");

		if (mIsAlreadyOpened) {
			return;
		}
		mIsAlreadyOpened = true;
		AppLogic.printStatementCycle("show");
		RecordInvokeFactory.getAdapter().show();
		notifyShow();
	}

	public static void close() {
		if (SearchEditDialog.getInstance().isShowing()) {
			SelectorHelper.backAsrWithCancel();
			return;
		}

		Selector.clearSelectorWakeup();
		// 取消底层识别
		NativeData.getNativeData(UiData.DATA_ID_VOICE_CANCEL_PARSE);
		dismiss();
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		AsrManager.getInstance().cancel();
		AsrManager.getInstance().setCloseRecordWinWhenProcEnd(true);
		TextManager.getInstance().cancel();
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
		
		Selector.closeAllWin();
		mRunnableCloseDelay.enable(false);
		AppLogic.removeBackGroundCallback(mRunnableCloseDelay);
		if (!mIsAlreadyOpened) {
			return;
		}
		mIsAlreadyOpened = false;
		MusicManager.getInstance().releaseAudioFocusImmediately();
		AppLogic.printStatementCycle("dismiss");
		RecordInvokeFactory.getAdapter().dismiss();
		notifyDismiss();

		// 规避录音窗口中，启动唤醒过程中，录音被stop的BUG。(云知声新版本已修正
		// WakeupManager.getInstance().stop();
		// WakeupManager.getInstance().start();
		
		/*、ANDYZHAO 2016/06/22
		 * 1、防止关闭界面之前，唤醒没有被启动，尤其是在不断点击声控图标的情况下
		 * 2、如果唤醒已经被启动了，调用以下接口，并不会执行实际耗时操作。否则，自然需要启动唤醒。
		 */
		WakeupManager.getInstance().startDelay(500);
	}

	// 更新录音窗口状态
	public static void refreshState(int state) {
		LogUtil.logd("RecorderWin state:" + state);
		RecordInvokeFactory.getAdapter().refreshState(state);
	}

	// 通知音量改变 0 - 20
	public static void notifyVolumeChanged(int volume) {
		RecordInvokeFactory.getAdapter().refreshVolume(volume);
	}

	/**
	 * 更新进度条 progress 0-100 progress < 0 时隐藏进度条
	 */
	public static void refreshProgressBar(int progress, int selection) {
		RecordInvokeFactory.getAdapter().refreshProgress(progress, selection);
	}

	/**
	 * 隐藏进度条
	 */
	public static void hideProgressBar(int selection) {
		refreshProgressBar(-1, selection);
	}

	// 发送消息
	private static void sendMsg(int owner, String text) {
		RecordInvokeFactory.getAdapter().addMsg(owner, text);
	}

	private static void showUserText() {
		if (mLastUserText != null) {
			sendMsg(RecorderWin.OWNER_USER, mLastUserText);
			mLastUserText = null;
		}
	}

	public static void addSystemMsg(String text) {
		showUserText();
		if (mHideLastSystemText)
			mHideLastSystemText = false;
		// else
		sendMsg(RecorderWin.OWNER_SYS, text);
	}
	
	// 发送选择器的列表数据
	public static void sendSelectorList(String dataJson){
		showUserText();
		RecordInvokeFactory.getAdapter().addListMsg(dataJson);
	}

	// 发送联系人列表数据
	public static void sendContactList(String strPrefix, String strName,
			String strSuffix, boolean isMutilName, List<Contact> list) {
		showUserText();

		JSONBuilder doc = new JSONBuilder();

		doc.put("type", CallSence);
		doc.put("strPrefix", strPrefix);
		doc.put("strName", strName);
		doc.put("strSuffix", strSuffix);
		doc.put("isMultiName", isMutilName);

		List<JSONObject> contacts = new ArrayList<JSONObject>();
		for (int i = 0; i < list.size(); i++) {
			Contact info = list.get(i);
			JSONObject contact = new JSONBuilder().put("name", info.name)
					.put("number", info.number).put("province", info.province)
					.put("city", info.city).put("isp", info.isp).build();
			contacts.add(contact);
		}

		doc.put("contacts", contacts.toArray());

		String data = doc.toString();
		RecordInvokeFactory.getAdapter().addListMsg(data);
	}

	// 发送联系人列表数据
//	public static void sendMediaList(List<AudioShowData> list,String hintTxt) {
//		procCloseAutoNaviProgress();
//		showUserText();
//
//		JSONBuilder doc = new JSONBuilder();
//		doc.put("type", AudioSence);
//		List<JSONObject> audios = new ArrayList<JSONObject>();
//		String text;
//		for (int i = 0; i < list.size(); i++) {
//			AudioShowData info = list.get(i);
//			JSONBuilder jsonBuilder = new JSONBuilder().put("title",
//					info.getTitle());
//			text = info.getName();
//			if (StringUtils.isEmpty(text)) {
//				// text = "未知艺术家";
//			}
//			jsonBuilder.put("text", text);
//			JSONObject audio = jsonBuilder.build();
//			audios.add(audio);
//		}
//
//		doc.put("audios", audios.toArray());
//		doc.put("prefix", hintTxt);
////		doc.put("topkeywords", hintTxt);
//
//		String data = doc.toString();
//		RecordInvokeFactory.getAdapter().addListMsg(data);
//	}

//	public static void sendAudioResultList(String key, List<MusicBean> musics) {
//		if (musics == null) {
//			musics = new ArrayList<MusicBean>();
//		}
//
//		JSONBuilder jb = new JSONBuilder();
//		jb.put("type", 3);
//		jb.put("keywords", key);
//		jb.put("count", musics.size());
//		for (int i = 0; i < musics.size(); i++) {
//			jb.put("music" + i, musics.get(i).toString());
//		}
//		RecordInvokeFactory.getAdapter().addListMsg(jb.toString());
//	}

//	public static void sendWeChatSessionTarget(WeChatContacts targets, String title,String hintTxt) {
//		showUserText();
//
//		JSONBuilder doc = new JSONBuilder();
//
//		doc.put("type", WeChatSence);
//		doc.put("title", title);
//		doc.put("action", "wechat_contacts");
//		List<JSONObject> contacts = new ArrayList<JSONObject>();
//		for (int i = 0; i < targets.cons.length; i++) {
//			WeChatContact info = targets.cons[i];
//			JSONObject contact = new JSONBuilder().put("name", info.name)
//					.put("id", info.id).build();
//			contacts.add(contact);
//		}
//
//		doc.put("contacts", contacts.toArray());
//		doc.put("prefix", hintTxt);
////		doc.put("topkeywords", hintTxt);
//
//		String data = doc.toString();
//		RecordInvokeFactory.getAdapter().addListMsg(data);
//	}

	// 显示股票信息
	public static void showStockInfo(byte[] info) {
		mHideLastSystemText = true;
		RecordInvokeFactory.getAdapter().showStock(info);
	}

	// 显示天气信息
	public static void showWeatherInfo(byte[] info) {
		mHideLastSystemText = true;
		RecordInvokeFactory.getAdapter().showWeather(info);
	}

	public static int speakTextWithClose(String text, final Runnable onEnd) {
		return speakTextWithClose(text, true, onEnd);
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

	public static int speakTextWithClose(String text, final boolean needAsr,
			final Runnable onEnd) {
		mRunnableCloseDelay.enable(false);
		AppLogic.removeBackGroundCallback(mRunnableCloseDelay);
		addSystemMsg(text);
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.getInstance().speakText(text,
				PreemptType.PREEMPT_TYPE_NEXT, new ITtsCallback() {
					@Override
					public void onCancel() {
						if (onEnd != null) {
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
								AsrManager.getInstance().start(needAsr);
							}
						if (onEnd != null) {
							onEnd.run();
						}
					}

					@Override
					public void onError(int iError) {
						dismiss();
					}
				});
		return mSpeechTaskId;
	}

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
	public static String RECORD_STATUS_KEY = "record_status";
	public static int RECORD_START = 0;
	public static int RECORD_RECORNIZE = 1;
	public static int RECORD_END = 2;

	public static void recStatusHelper(int recStatus, String recStatusStr) {
		sendRecStatusBroadIntent = new Intent(RECORD_STATUS_CHANGED_ACTION,
				null);
		sendRecStatusBroadIntent.putExtra(RECORD_STATUS_KEY, recStatus);
		AppLogic.getApp().sendBroadcast(sendRecStatusBroadIntent);
		JNIHelper.logi(RECORD_STATUS_CHANGED_ACTION + "当前录音状态:" + recStatusStr
				+ ":" + recStatus);
	}

	public static void setState(STATE state) {
		switch (state) {
		case STATE_START:
			RecorderWin.refreshState(RecorderWin.STATE_RECORD_START);
			recStatusHelper(RECORD_START, "RECORD_START");
			break;
		case STATE_RECORD:
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

	/*
	 * 处理录音对话窗口的UI事件 event: record.ui.event.item.selected data: index event:
	 * record.ui.event.button.cancel data: null event: record.ui.event.button.ok
	 * data: null event: record.ui.event.button.back data: null event:
	 * record.ui.event.button.pause data: null
	 */
	public static byte[] dealRecorderUIEvent(String event, byte[] data) {
		JNIHelper.logd("dealRecorderUIEvent:" + event);
		if (event.equals("txz.record.ui.event.item.selected")) {
			JSONBuilder json = new JSONBuilder(data);
			int index = json.getVal("index", Integer.class);
			Integer typeObject = json.getVal("type", Integer.class);

			int type = -1;
			if (typeObject == null) {
				type = CallSence;
			} else {
				type = typeObject;
			}

			if (type == CallSence && CallSelectControl.isSelecting()) {
				CallSelectControl.selectIndex(index, null);
				return null;
			}

			// if (type == WeChatSence && WeixinSelectControl.isSelecting()) {
			// WeixinSelectControl.selectIndex(index, false);
			// }
			//
			// if (type == AudioSence && MusicSelectControl.isSelecting()) {
			// MusicSelectControl.selectIndex(index, null);
			// }

			// TODO 通用的列表
			return Selector.procInvoke("", event, data);
		} else if (event.equals("txz.record.ui.event.list.ontouch")) {
			return Selector.procInvoke("", event, data);
		} else if (event.equals("txz.record.ui.event.button.cancel")) {
			Selector.onUiEventCancel();
		} else if (event.equals("txz.record.ui.event.button.ok")) {
			Selector.onUiEventOK();
		} else if (event.equals("txz.record.ui.event.button.back")) {
			Selector.onUiEventBack();
		} else if (event.equals("txz.record.ui.event.button.pause")) {
			AsrManager.getInstance().mSenceRepeateCount = -1;
			Selector.onPauseStopTtsAndAsr();

			if (mSpeechTaskId != TtsManager.INVALID_TTS_TASK_ID) {
				TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
				mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
				// 上报数据
				ReportUtil.doReport(new ReportUtil.Report.Builder()
						.setType("core").setAction("interrupt_tts")
						.buildTouchReport());
			}

			if (mWillStartAsrAfterTts) {
				AsrManager.getInstance().start(true);
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
					ReportUtil.doReport(new ReportUtil.Report.Builder()
							.setType("core").setAction("interrupt_asr")
							.buildTouchReport());
					break;
				case STATE_STARTING_SCO:
					break;
				default:
					break;
				}

			}
		} else if (event.equals("txz.record.ui.event.dismiss")) {
			mRunnableCloseDelay.enable(false);
			AppLogic.removeBackGroundCallback(mRunnableCloseDelay);

			AsrManager.getInstance().mUnknowCount = 0;
			AsrManager.getInstance().mEmptyCount = 0;
			AsrManager.getInstance().unregCommand("GLOBAL_CMD_END_CHAT",
					"GLOBAL_CMD_END_CHAT_BY_QUESTION");
			RecorderWin.close();
			Selector.onDialogDismiss();
		} else if (event.equals("txz.record.ui.status.isShowing")) {
			return Boolean.valueOf(isOpened()).toString().getBytes();
		} else if (event.equals("txz.record.ui.event.display.count")) {
			return Selector.procInvoke("", event, data);
		} else if (event.equals("txz.record.ui.event.item.right")) {
			return Selector.procInvoke("", event, data);
		} else if (event.equals("txz.record.ui.event.display.tip")) {
			return Selector.procInvoke("", event, data);
		} else if (event.equals("txz.record.ui.event.display.page")){
			return Selector.procInvoke("", event, data);
		}
		return null;
	}

	public static void notifyShow() {
		Intent intent = new Intent("com.txznet.txz.record.show");
		GlobalContext.get().sendBroadcast(intent);
		OBSERVABLE.notifyShow();
	}

	public static void notifyDismiss() {
		Intent intent = new Intent("com.txznet.txz.record.dismiss");
		GlobalContext.get().sendBroadcast(intent);
		OBSERVABLE.notifyDismiss();

		{
			// 声控消失取消延时任务
			NavManager.getInstance().cancelAllPoiSearch();
			MusicManager.getInstance().cancelSearchMedia();
			AudioManager.getInstance().cancelAllRequest();
//			LocationManager.getInstance().cancelReverseGeo();
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
	
	public static void notifyDismissWithoutBroadcast() {
		OBSERVABLE.notifyDismiss();

		{
			// 声控消失取消延时任务
			NavManager.getInstance().cancelAllPoiSearch();
			MusicManager.getInstance().cancelSearchMedia();
			AudioManager.getInstance().cancelAllRequest();
//			LocationManager.getInstance().cancelReverseGeo();
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
				open(hintTxt, grammar);
			} else {
				open();
			}
		}
	}
}

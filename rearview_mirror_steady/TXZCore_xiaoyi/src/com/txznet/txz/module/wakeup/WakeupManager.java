package com.txznet.txz.module.wakeup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.JSONArray;

import android.os.SystemClock;
import android.text.TextUtils;

import com.txz.ui.data.UiData;
import com.txz.ui.data.UiData.AppConfig;
import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.WakeupAsrKeywords;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil.IWakeupAsrCallback;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.wakeup.IWakeup;
import com.txznet.txz.component.wakeup.IWakeup.IInitCallback;
import com.txznet.txz.component.wakeup.IWakeup.IWakeupCallback;
import com.txznet.txz.component.wakeup.txz.WakeupTxzImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.call.CallSelectControl;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.launch.LaunchManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.service.TXZService;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.STATE;
import com.txznet.txz.util.BeepPlayer;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.runnables.Runnable1;

public class WakeupManager extends IModule {
	static WakeupManager sModuleInstance = new WakeupManager();

	IWakeup mWakeup = null;

	public boolean mBindStyleWithWakeup = false;

	// String[] mWakeupKeywords = null;
	public boolean mEnableWakeup = true;
	public boolean mEnableChangeWakeupKeywords = true;
	public boolean mEnableCoverDefaultKeywords = true;
	public String[] mWakeupKeywords_Sdk = null; // SDK设置的唤醒词
	public String[] mWakeupKeywords_User = null; // 用户设置的唤醒词
	Map<String, Runnable> mWakeupKeywords_Global = new HashMap<String, Runnable>(); // 全局唤醒词，如“返回桌面”

	static class WakeupAsrTask {
		public WakeupAsrTask(String service, IWakeupAsrCallback cb) {
			callback = cb;
			this.service = service;
		}

		public String service;
		public IWakeupAsrCallback callback;
		public int ttsId = TtsManager.INVALID_TTS_TASK_ID;

		public void cancelTts() {
			TtsManager.getInstance().cancelSpeak(ttsId);
			ttsId = TtsManager.INVALID_TTS_TASK_ID;
		}
	}

	List<WakeupAsrTask> mWakeupKeywords_Asr = new ArrayList<WakeupAsrTask>();// 特定服务注册的唤醒词，如导航

	private WakeupManager() {
		mInited = false;
		mInitSuccessed = false;
		mWakeupKeywords_Sdk = new String[] { "你好小踢" };
		String kws = PreferenceUtil.getInstance().getString(
				PreferenceUtil.KEY_USER_WAKEUP_KEYWORDS, null);
		try {
			JSONBuilder json = new JSONBuilder(kws);
			mWakeupKeywords_User = json.getVal("kws", String[].class);
		} catch (Exception e) {
		}
	}

	public static WakeupManager getInstance() {
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	private void addKeywords(Set<String> kws, String[] cmds) {
		if (cmds != null) {
			for (String kw : cmds) {
				if (TextUtils.isEmpty(kw))
					continue;
				kws.add(kw.replace("同行者", "同形者"));
			}
		}
	}

	Set<String> mSetWakeupKeywords = new TreeSet<String>();

	private String[] genWakeupKeywords() {
		synchronized (mSetWakeupKeywords) {
			mSetWakeupKeywords.clear();
			if (CallManager.getInstance().isRinging() == false) {
				addKeywords(mSetWakeupKeywords, mWakeupKeywords_Sdk);
				if (mEnableChangeWakeupKeywords)
					addKeywords(mSetWakeupKeywords, mWakeupKeywords_User);
				for (String kw : mWakeupKeywords_Global.keySet()) {
					if (TextUtils.isEmpty(kw))
						continue;
					mSetWakeupKeywords.add(kw.replace("同行者", "同形者"));
				}
			}
			synchronized (mWakeupKeywords_Asr) {
				for (WakeupAsrTask task : mWakeupKeywords_Asr) {
					if (CallManager.getInstance().isRinging()
							&& !CallManager.WAKEUP_INCOMING_TASK_ID
									.equals(task.callback.getTaskId()))
						continue;
					addKeywords(mSetWakeupKeywords, task.callback.genKeywords());
				}
			}
			return mSetWakeupKeywords.toArray(new String[mSetWakeupKeywords
					.size()]);
		}
	}

	private boolean updateComponentKeywords() {
		if (mWakeup == null)
			return false;

		String[] kws = genWakeupKeywords();

		{
			WakeupAsrKeywords pbWakeupAsrKeywords = new WakeupAsrKeywords();
			pbWakeupAsrKeywords.rptStrKws = kws;
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_SET_WAKEUP_ASR_KEYWORDS,
					pbWakeupAsrKeywords);
		}

		if (kws == null || kws.length <= 0) {
			JNIHelper.logw("empty wakeup keywords");
			return false;
		}

		JNIHelper.logd("update wakeup keywords: " + kws[0] + "..."
				+ kws[kws.length - 1]);

		{
			//
			// StringBuffer sb = new StringBuffer();
			// sb.append("<wkKws>\n");
			// for (String kw : kws) {
			// sb.append(kw);
			// sb.append('\n');
			// }
			// sb.append("</wkKws>\n");
			// AsrManager.getInstance().insertVocab_ext(
			// VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL, sb);
			// JNIHelper.logd("end insertVocab_ext");
		}

		mWakeup.setWakeupKeywords(kws);
		return true;
	}

	public void initializeComponent() {
		if (mWakeup != null)
			return;

		try {
			mWakeup = (IWakeup) Class.forName(ImplCfg.getWakeupImpClass())
					.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] kws;
		synchronized (WakeupManager.class) {
			kws = genWakeupKeywords();
		}
		mWakeup.initialize(kws, new IInitCallback() {
			@Override
			public void onInit(boolean bSuccess) {
				JNIHelper.logd("init wakeup: " + bSuccess);
				mInited = true;
				mInitSuccessed = bSuccess;

				if (bSuccess) {
					WakeupManager.this.start();
				}

				TXZService.checkSdkInitResult();
			}
		});
	}

	Runnable mStartRunnable = new Runnable() {
		@Override
		public void run() {
			WakeupManager.this.start();
		}
	};

	int mStartDelay = 1000;
	private float mWakeupThreshold = -3.1f;// 正常唤醒门限
	private float mAsrWakeupThreshold = -3.5f;// 识别唤醒门限

	public void setWakeupThreshhold(float threshHold) {
		// TODO
		LogUtil.logd("setWakeupThreshhold threshHold=" + threshHold);
		mWakeupThreshold = threshHold;
		ConfigManager.getInstance().notifyRemoteSync();
	}

	public float getWakeupThreshhold() {
		return mWakeupThreshold;
	}

	public void setAsrWakeupThreshhold(float threshHold) {
		mAsrWakeupThreshold = threshHold;
	}

	private long lastWakeupVolumeTime = 0;
	
	private Runnable mRunnableRestartWakeup = new Runnable() {
		@Override
		public void run() {
			if (mEnableVoiceChannel == false) {
				AppLogic.removeBackGroundCallback(mRunnableRestartWakeup);
				AppLogic.runOnBackGround(mRunnableRestartWakeup, 3 * 1000);
				return;
			}
			// 1s内有音量回调，则不重启唤醒
			if (lastWakeupVolumeTime + 1000 > SystemClock.elapsedRealtime()) {
				AppLogic.removeBackGroundCallback(mRunnableRestartWakeup);
				AppLogic.runOnBackGround(mRunnableRestartWakeup, 5 * 60 * 1000);
				return;
			}
			JNIHelper.logw("begin restart wakeup");
			AppLogic.removeBackGroundCallback(mStartRunnable);
			stop();
			start();
		}
	};

	public void startDelay(int delay) {
		AppLogic.removeBackGroundCallback(mStartRunnable);
		AppLogic.runOnBackGround(mStartRunnable, delay);
	}

	public boolean start() {
		synchronized (WakeupManager.class) {
			return startInner();
		}
	}

	private boolean startInner() {
		JNIHelper.logd("Wakeup start....");
		AppLogic.removeBackGroundCallback(mStartRunnable);
		if (!updateComponentKeywords()) {
			JNIHelper.logw("empty wakeup keywords");
			return false;
		}
		if (isInitSuccessed() == false
				|| mEnableWakeup == false
				|| AsrManager.getInstance().forbiddenWakeup()
				/*ANDYZHAO 2016/06/22
				 * 1、播报TTS的时候已经掐断录音，所以启动唤醒没有问题。
				 * 2、有些地方启动唤醒识别的时机不对，即TTS播报非闲状态下，启动的话，会导致TTS停止播报后没法立马唤醒
				 * || (TtsManager.getInstance().isBusy() && (ProjectCfg.mEnableAEC == false || ProjectCfg
						.needStopWkWhenTts()))*/
				|| RecordManager.getInstance().isBusy()
				|| (CallManager.getInstance().isIdle() == false && CallManager
						.getInstance().isRinging() == false)) {
			String busyReason = String
					.format("wakeup is busy, waiting for idle: initSuccessd = %b; enable=%b; AsrBusy = %b; tts = %b; record = %b; call = %b",
							isInitSuccessed(), mEnableWakeup, AsrManager
									.getInstance().isBusy(), TtsManager
									.getInstance().isBusy(), RecordManager
									.getInstance().isBusy(), CallManager
									.getInstance().isIdle());
			JNIHelper.logw(busyReason);
			// 5秒后重试唤醒
			AppLogic.runOnBackGround(mStartRunnable, mStartDelay);
			if (mStartDelay < 5000) {
				mStartDelay += 1000;
			}
			return false;
		}

		mIsBusy = true;

		checkUsingAsr(new Runnable() {
			@Override
			public void run() {
				mWakeup.setWakeupThreshold(mAsrWakeupThreshold);
				if (mEnableVoiceChannel) {
					MusicManager.getInstance().onBeginAsr();
				}
				RecorderWin.setState(STATE.STATE_RECORD);
			}
		}, new Runnable() {
			@Override
			public void run() {
				mWakeup.setWakeupThreshold(mWakeupThreshold);
			}
		});

		mStartDelay = 1000;
		mWakeup.start(new IWakeupCallback() {
			@Override
			public void onWakeUp(String text) {
				JNIHelper.logd("onWakeUp wakeup result: " + text);

				WakeupPcmHelper.savePcm(text, 1);

				AsrManager.getInstance().cancel();
				// 有唤醒识别，优先处理，只处理最后注册的任务的

				List<WakeupAsrTask> tmpTaskList = new ArrayList<WakeupAsrTask>();
				synchronized (mWakeupKeywords_Asr) {
					tmpTaskList.addAll(mWakeupKeywords_Asr);
				}

				for (int i = tmpTaskList.size(); i > 0; --i) {
					WakeupAsrTask task = tmpTaskList.get(i - 1);
					RecorderWin.setState(STATE.STATE_END);
					task.callback.setIsWakeupResult(true);
					if (task.callback.onAsrResult(text)) {
						return;
					}
				}
				// 处理全局唤醒指令
				if (mWakeupKeywords_Global != null) {
					for (Entry<String, Runnable> entry : mWakeupKeywords_Global
							.entrySet()) {
						if (entry.getKey().equals(text)) {
							entry.getValue().run();
						}
					}
				}
				ReportUtil.doReport(new ReportUtil.Report.Builder().setKeywords(text).setAction("wakeup").buildWakeupReport());

				// 再处理远程唤醒场景
				JSONBuilder json = new JSONBuilder();
				json.put("keywords", text);
				json.put("action", "wakeup");
				json.put("sence", "wakeup");
				if (SenceManager.getInstance().noneedProcSence("wakeup",
						json.toBytes())) {
					return;
				}
				String style = null;
				if (mBindStyleWithWakeup) {
					for (int i = 0; i < mWakeupKeywords_Sdk.length; ++i) {
						if (text.equals(mWakeupKeywords_Sdk[i])) {
							style = "";
						}
					}
				}
				JNIHelper.logd("Wakeup doLaunch: " + text + ", style=" + style);
				LaunchManager.getInstance().launchWithWakeup(style);
			}

			@Override
			public void onVolume(int vol) {
				lastWakeupVolumeTime = SystemClock.elapsedRealtime();
				synchronized (mWakeupKeywords_Asr) {
					for (WakeupAsrTask task : mWakeupKeywords_Asr) {
						if (task.callback != null) {
							task.callback.onVolume(vol);
						}
					}
				}
			}

			@Override
			public void onSpeechBegin() {
				// TODO Auto-generated method stub
				synchronized (mWakeupKeywords_Asr) {
					for (WakeupAsrTask task : mWakeupKeywords_Asr) {
						if (task.callback != null) {
							task.callback.onSpeechBegin();
						}
					}
				}
			}

			@Override
			public void onSpeechEnd() {
				// TODO Auto-generated method stub
				synchronized (mWakeupKeywords_Asr) {
					for (WakeupAsrTask task : mWakeupKeywords_Asr) {
						if (task.callback != null) {
							task.callback.onSpeechEnd();
						}
					}
				}
			}
		});

		// 设置定时重启，防止老化出现不可唤醒
		AppLogic.removeBackGroundCallback(mRunnableRestartWakeup);
		AppLogic.runOnBackGround(mRunnableRestartWakeup, 6 * 1000);

		return true;
	}

	public void stopComplete() {
		stop();
		AppLogic.removeBackGroundCallback(mStartRunnable);
	}

	public void stop() {
		synchronized (WakeupManager.class) {
			stopInner();
			checkUsingAsr(new Runnable() {
				@Override
				public void run() {
					MusicManager.getInstance().onEndAsr();
				}
			}, null);
		}
	}

	private boolean mIsBusy = false;

	public boolean isBusy() {
		return mIsBusy && mEnableVoiceChannel;
	}

	private void stopInner() {
		JNIHelper.logd("Wakeup stop....");
		// 10秒后自动尝试启动声控，防止逻辑上忘了恢复
		AppLogic.removeBackGroundCallback(mRunnableRestartWakeup);
		AppLogic.removeBackGroundCallback(mStartRunnable);
		AppLogic.runOnBackGround(mStartRunnable, 10000);
		if (isInitSuccessed() == false)
			return;
		mWakeup.stop();
		updateComponentKeywords();
		mIsBusy = false;
	}

	public int startWithRecord(IWakeupCallback oCallback, RecordOption options,
			String[] overTag) {
		if (mWakeup == null || !isInitSuccessed()) {
			return -1;
		}
		mWakeup.startWithRecord(oCallback, options, overTag);
		return 0;
	}

	public void stopWithRecord() {
		if (mWakeup == null || !isInitSuccessed()) {
			return;
		}
		mWakeup.stopWithRecord();
	}

	@Override
	public int initialize_BeforeStartJni() {
		// 注册需要处理的事件
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_WAKEUP_KEYWORDS);
		regEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_SET_WAKEUP_KEYWORDS);

		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterStartJni() {
		// 发送初始化需要触发的事件
		return super.initialize_AfterStartJni();
	}

	private void onAsrWakeup(String text) {
		CallSelectControl.clearProgress();
		JSONBuilder json = new JSONBuilder();
		json.put("keywords", text);
		json.put("action", "asr");
		json.put("sence", "wakeup");
		if (SenceManager.getInstance()
				.noneedProcSence("wakeup", json.toBytes())) {
			return;
		}
		RecorderWin.open();
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		// 处理事件
		switch (eventId) {
		case UiEvent.EVENT_VOICE: {
			switch (subEventId) {
			case VoiceData.SUBEVENT_VOICE_WAKEUP_KEYWORDS: {
				String text = new String(data);
				JNIHelper.logd("recive asr wakeup keywords: " + text);
				// 有唤醒识别，优先处理，只处理最后注册的任务的
				List<WakeupAsrTask> tmpTaskList = new ArrayList<WakeupAsrTask>();
				synchronized (mWakeupKeywords_Asr) {
					tmpTaskList.addAll(mWakeupKeywords_Asr);
				}
				for (int i = tmpTaskList.size(); i > 0; --i) {
					WakeupAsrTask task = tmpTaskList.get(i - 1);
					RecorderWin.setState(STATE.STATE_END);
					task.callback.setIsWakeupResult(false);
					if (task.callback.onAsrResult(text)) {
						break;
					}
				}
				if (mWakeupKeywords_Sdk != null) {
					for (String kw : mWakeupKeywords_Sdk) {
						if (kw.equals(text)) {
							ReportUtil.doReport(new ReportUtil.Report.Builder().setKeywords(text).setAction("asr").buildWakeupReport());
							onAsrWakeup(text);
							return super.onEvent(eventId, subEventId, data);
						}
					}
				}
				if (mWakeupKeywords_User != null) {
					for (String kw : mWakeupKeywords_User) {
						if (kw.equals(text)) {
							ReportUtil.doReport(new ReportUtil.Report.Builder().setKeywords(text).setAction("asr").buildWakeupReport());
							onAsrWakeup(text);
							break;
						}
					}
				}
				break;
			}
			case VoiceData.SUBEVENT_VOICE_SET_WAKEUP_KEYWORDS: {
				JNIHelper.logd("updated wakeup keywords: " + new String(data)
						+ ", EnableChangeWakeupKeywords="
						+ mEnableChangeWakeupKeywords);
				if (mEnableChangeWakeupKeywords) {
					updateWakupKeywords_User(new String[] { new String(data) });
				}
				break;
			}
			}
			break;
		}
		}
		return super.onEvent(eventId, subEventId, data);
	}

	public void updateWakupKeywords_Sdk(String[] cmds) {
		// TODO
		LogUtil.logd("updateWakupKeywords_Sdk cmds="
				+ (cmds == null ? "null" : Arrays.toString(cmds)));
		synchronized (WakeupManager.class) {
			mWakeupKeywords_Sdk = cmds;
			stopInner();
			startInner();
		}
		AppConfig appConfig = NativeData.getAppConfig();
		if (appConfig != null) {
			Set<String> kws = new HashSet<String>();
			if (appConfig.rptStrWakeupKeywords != null) {
				for (String kw : appConfig.rptStrWakeupKeywords) {
					kws.add(kw);
				}
			}
			for (String kw : cmds) {
				kws.add(kw);
			}
			appConfig.rptStrWakeupKeywords = kws
					.toArray(new String[kws.size()]);
			JNIHelper.sendEvent(UiEvent.EVENT_CONFIG,
					UiData.DATA_ID_CONFIG_APP, appConfig);
		}
		ConfigManager.getInstance().notifyRemoteSync();
	}

	public String[] getWakeupKeywords_Sdk() {
		return mWakeupKeywords_Sdk;
	}

	public String[] getWakeupKeywords_User() {
		return mWakeupKeywords_User;
	}
	
	public void updateWakupKeywords_User(String[] cmds) {
		LogUtil.logd("updateWakupKeywords_User isShowSettings:"+ConfigUtil.isShowSettings()+
				"mEnableCoverDefaultKeywords:"+mEnableCoverDefaultKeywords);
		if (ConfigUtil.isShowSettings() && mEnableCoverDefaultKeywords) {
			mWakeupKeywords_User = cmds;
			updateWakupKeywords_Sdk(cmds);
		} else {
			synchronized (WakeupManager.class) {
				mWakeupKeywords_User = cmds;
				stopInner();
				startInner();
			}
		}
		JSONBuilder json = new JSONBuilder();
		json.put("kws", cmds);
		String data = json.toString();
		PreferenceUtil.getInstance().setString(
				PreferenceUtil.KEY_USER_WAKEUP_KEYWORDS, data);
		ServiceManager.getInstance().broadInvoke(
				"userconfig.onChangeWakeupKeywords", data.getBytes());
		ConfigManager.getInstance().notifyRemoteSync();
		
	}

	public void enableWakeup(boolean wakeup) {
		if (wakeup) {
			mEnableWakeup = true;
			start();
		} else {
			mEnableWakeup = false;
			stop();
		}
		ConfigManager.getInstance().notifyRemoteSync();
	}


	public void enableCoverDefaultKeywords(boolean coverDeafaultKeywords) {
		this.mEnableCoverDefaultKeywords = coverDeafaultKeywords;
		ConfigManager.getInstance().notifyRemoteSync();
	}

	public byte[] invokeTXZWakeup(final String packageName, String command,
			byte[] data) {
		if (command.equals("stop")) {
			enableWakeup(false);
			return null;
		}
		if (command.equals("start")) {
			enableWakeup(true);
			return null;
		}
		if (command.equals("update")) {
			try {
				JSONArray json = new JSONArray(new String(data));
				String[] kws = new String[json.length()];
				for (int i = 0; i < json.length(); ++i) {
					kws[i] = json.getString(i);
				}
				if (kws.length > 0)
					mEnableWakeup = true;
				updateWakupKeywords_Sdk(kws);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("enableChangeWakeupKeywords")) {
			boolean b = Boolean.parseBoolean(new String(data));
			if (mEnableChangeWakeupKeywords != b) {
				mEnableChangeWakeupKeywords = b;
				JNIHelper.logd("change enable = "+mEnableChangeWakeupKeywords);
				stop();
				start();
			}
			return null;
		}
		if (command.equals("enableCoverDefaultKeywords")) {
			boolean b = Boolean.parseBoolean(new String(data));
			if (mEnableCoverDefaultKeywords != b) {
				enableCoverDefaultKeywords(b);
			}
			return null;
		}
		if (command.equals("set.asrwkscore")) {
			if (data == null) {
				return null;
			}
			setAsrWakeupThreshhold(Float.parseFloat(new String(data)));
			return null;
		}
		if (command.equals("set.wkscore")) {
			if (data == null) {
				return null;
			}
			setWakeupThreshhold(Float.parseFloat(new String(data)));
			return null;
		}
		if (command.equals("forceStopWkWhenTts")) {
			if (data == null) {
				return null;
			}
			ProjectCfg.forceStopWkWhenTts(Boolean
					.parseBoolean(new String(data)));
			return null;
		}
		return null;
	}

	Runnable mBeepCompletionRunnable = new Runnable() {

		@Override
		public void run() {
			checkUsingAsr(new Runnable() {
				@Override
				public void run() {
					MusicManager.getInstance().onEndBeep();
				}
			}, null);
		}
	};

	public boolean playAsrTipSound() {
		BeepPlayer.play(mBeepCompletionRunnable);
		return true;
	}

	public void useWakeupAsAsr(IWakeupAsrCallback callback) {
		useWakeupAsAsr(ServiceManager.TXZ, callback);
	}

	public void useWakeupAsAsr(String service, final IWakeupAsrCallback callback) {

		JNIHelper.logd("useWakeupAsAsr enter: " + service + "@"
				+ callback.getTaskId());
		boolean noChange = recoverWakeupFromAsrInner(service,
				callback.getTaskId(), false, callback);
		if (noChange) {
			JNIHelper.logd("useWakeupAsAsr nochange: " + service + "@"
					+ callback.getTaskId());
		}

		JNIHelper.logd("useWakeupAsAsr begin: " + service + "@"
				+ callback.getTaskId());

		synchronized (WakeupManager.class) {
			final WakeupAsrTask task = new WakeupAsrTask(service, callback);
			synchronized (mWakeupKeywords_Asr) {
				mWakeupKeywords_Asr.add(task);
				mReadWriteLock.writeLock().lock();
				if (task.callback != null && task.callback.needAsrState()) {
					++mUsingAsrInnerCount;
				}
				mReadWriteLock.writeLock().unlock();
			}

			if (!noChange) {
				stopInner();
			}

			String hint = callback.needTts();

			RecorderWin.setState(STATE.STATE_RECORD);

			if (hint == null) {
				if (startInner()) {
					// 需要识别态没有tts时默认beep音结束
					if (callback.needAsrState()
							&& !TtsManager.getInstance().isBusy()
							&& !AsrManager.getInstance().isBusy()) {
						MusicManager.getInstance().onEndBeep();
					}
				}
				return;
			}

			if (ProjectCfg.mEnableAEC && !ProjectCfg.needStopWkWhenTts()) {
				startInner();
			}

			task.ttsId = TtsManager.getInstance().speakText(hint,
					new TtsUtil.ITtsCallback() {
						public void onEnd() {
							callback.onTtsEnd();

							boolean hasTask = false;
							synchronized (mWakeupKeywords_Asr) {
								hasTask = mWakeupKeywords_Asr.contains(task);
							}
							if (/* ProjectCfg.mEnableAEC == false && */hasTask) {
								playAsrTipSound();
							} else {
									checkUsingAsr(new Runnable() {
										@Override
										public void run() {
											MusicManager.getInstance().onEndBeep();
										}
									}, null);
							}
							start();
						}

						@Override
						public void onCancel() {
							onEnd();
						}

						@Override
						public void onSuccess() {
							onEnd();
						}
					});
		}

		JNIHelper.logd("useWakeupAsAsr end: " + service + "@"
				+ callback.getTaskId());
	}

	public void recoverWakeupFromAsr(String taskId) {
		synchronized (WakeupManager.class) {
			recoverWakeupFromAsrInner(ServiceManager.TXZ, taskId, true, null);
		}
	}

	public void recoverWakeupFromAsr(String service, String taskId) {
		synchronized (WakeupManager.class) {
			recoverWakeupFromAsrInner(service, taskId, true, null);
		}
	}

	public boolean checkSameArray(String[] a, String[] b) {
		if (a == null || b == null || a.length != b.length)
			return false;
		Set<String> setKw = new HashSet<String>();
		Set<String> setRemove = new HashSet<String>();
		for (String s : a) {
			setKw.add(s);
		}
		for (String s : b) {
			if (setKw.remove(s) == false && setRemove.contains(s) == false) {
				return false;
			}
			setRemove.add(s);
		}
		return setKw.isEmpty();
	}

	// 返回关键字是否没有变化
	private boolean recoverWakeupFromAsrInner(String service, String taskId,
			boolean restartWakeup, IWakeupAsrCallback callback) {
		BeepPlayer.cancel();

		boolean ret = false;

		JNIHelper.logd(service + " recoverWakeupFromAsr " + service + "@"
				+ taskId);

		synchronized (mWakeupKeywords_Asr) {
			boolean bNeedUpdate = false;
			mReadWriteLock.writeLock().lock();
			for (int i = 0; i < mWakeupKeywords_Asr.size();) {
				WakeupAsrTask oldTask = mWakeupKeywords_Asr.get(i);
				if (oldTask.service.equals(service)
						&& taskId.equals(oldTask.callback.getTaskId())) {
					if (callback != null) {
						ret = checkSameArray(callback.genKeywords(),
								oldTask.callback.genKeywords());
					}
					oldTask = mWakeupKeywords_Asr.remove(i);
					if (oldTask != null) {
						if (oldTask.callback != null
								&& oldTask.callback.needAsrState()) {
							--mUsingAsrInnerCount;
						}
						oldTask.cancelTts();
					}
					bNeedUpdate = true;
					break;
				} else
					++i;
			}
			mReadWriteLock.writeLock().unlock();
			if (!bNeedUpdate){
				return ret;
			}
		}
        
		// 重启唤醒
		if (restartWakeup) {
			checkUsingAsr(null, new Runnable() {
				@Override
				public void run() {
					MusicManager.getInstance().onEndAsr();
				}
			});
			stopInner();
			startInner();
		}

		return ret;
	}

	int mUsingAsrInnerCount = 0;

//	private boolean isUsingAsrInner() {
//		return mUsingAsrInnerCount > 0;
//		// synchronized (mWakeupKeywords_Asr) {
//		// for (WakeupAsrTask task : mWakeupKeywords_Asr) {
//		// if (task.callback.needAsrState()) {
//		// LogUtil.logd("UsingAsrInner WakeupAsrTask taskid:"
//		// + task.callback.getTaskId());
//		// return true;
//		// }
//		// }
//		// }
//		// return false;
//	}
//
//	public boolean isUsingAsr() {
//		return isUsingAsrInner();
//	}
	
	private static final ReadWriteLock mReadWriteLock = new ReentrantReadWriteLock(false);
	
	/**
	 * 替换了之前的isUsingAsr和isUsingAsrInner方法
	 * 使用此方法注意检查有没有可能出现死锁的情况
	 * @param runTure
	 * @param runFalse
	 */
	public void checkUsingAsr(Runnable runTure,Runnable runFalse) {
		mReadWriteLock.readLock().lock();
		
		if(mUsingAsrInnerCount > 0){
			if (runTure != null) {
				runTure.run();	
			}
		}else {
			if (runFalse != null) {
				runFalse.run();
			}
		}
		
		mReadWriteLock.readLock().unlock();
	}

	public void invokeWakeup(String packageName, String command, byte[] data) {
		if (mWakeup != null && mWakeup instanceof WakeupTxzImpl) {
			((WakeupTxzImpl) mWakeup).invokeWakeup(packageName, command, data);
		}

	}

	boolean mEnableVoiceChannel = true;
	
	public void enbaleVoiceChannel(boolean enable) {
		mEnableVoiceChannel = enable;
		if (mWakeup != null && isInitSuccessed()) {
			mWakeup.enableVoiceChannel(enable);
		}

		checkUsingAsr(new Runnable1<Boolean>(enable) {
			@Override
			public void run() {
				if (mP1 == false) {
					MusicManager.getInstance().onEndAsr();
				} else {
					MusicManager.getInstance().onBeginAsr();
					MusicManager.getInstance().onEndBeep();
				}
			}
		}, null);
	}

}

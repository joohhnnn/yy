package com.txznet.txz.module.tts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.SystemClock;
import android.text.TextUtils;

import com.spreada.utils.chinese.ZHConverter;
import com.txz.equipment_manager.EquipmentManager;
import com.txz.equipment_manager.EquipmentManager.Req_TTSPatchDownResult;
import com.txz.equipment_manager.EquipmentManager.Req_TTSThemeDownResult;
import com.txz.equipment_manager.EquipmentManager.Req_TTSThemeInfoList;
import com.txz.equipment_manager.EquipmentManager.Req_TTSThemeSetUseResult;
import com.txz.equipment_manager.EquipmentManager.TTSTheme_Info;
import com.txz.push_manager.PushManager;
import com.txz.push_manager.PushManager.PushCmd_NotifyTTSThemeDown;
import com.txz.push_manager.PushManager.PushCmd_NotifyTTSThemeDownPatch;
import com.txz.push_manager.PushManager.PushCmd_NotifyTTSThemeSetUse;
import com.txz.push_manager.PushManager.TTSPatchInfo;
import com.txz.push_manager.PushManager.TtsText;
import com.txz.ui.event.UiEvent;
import com.txz.ui.innernet.UiInnerNet;
import com.txz.ui.innernet.UiInnerNet.DownloadHttpFileTask;
import com.txz.ui.tts.UiTts;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.remote.util.TtsUtil.VoiceTask;
import com.txznet.comm.util.FilePathConstants;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZMediaFocusManager;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.component.tts.ITts.IInitCallback;
import com.txznet.txz.component.tts.mix.TtsEngineManager;
import com.txznet.txz.component.tts.mix.TtsMix;
import com.txznet.txz.component.tts.mix.TtsTheme;
import com.txznet.txz.component.tts.remote.TtsRemoteImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.bt.BluetoothManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.download.DownloadManager;
import com.txznet.txz.module.fm.FmManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.music.focus.MusicFocusManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.version.LicenseManager;
import com.txznet.txz.module.volume.VolumeManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;
import com.txznet.txz.plugin.interfaces.AbsTextJsonParse;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.service.TXZService;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.AudioTrackPlayer;
import com.txznet.txz.util.BeepPlayer;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.TXZStatisticser;
import com.txznet.txz.util.TtsAuthorizeUtil;
import com.txznet.txz.util.UnZipUtil;
import com.txznet.txz.util.player.Error;
import com.txznet.txz.util.player.FFMPEGAudioPlayer;
import com.txznet.txz.util.player.TXZAudioPlayer;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

/**
 * TTS语音合成管理模块，负责语音队列管理，任务调度，接口适配，语音播报
 * 
 * @author bihongpi
 *
 */
public class TtsManager extends IModule {
	
	private static final String MONITOR_TTS_THEME = "tts.theme.I.";
	private static final String MONITOR_TTS_INIT_ERROR = "tts.init.E.";
	private static final String MONITOR_TTS_CHANGE = "tts.change.I.";
	private static final String MONITOR_TTS_CHANGE_ERROR = "tts.change.E.";
	private static final String MONITOR_TTS_DOWNLOAD = "tts.download.I.";
	private static final String MONITOR_TTS_UPGRADE_SUCCESS = "tts.upgrade.I.";
	private static final String MONITOR_TTS_UPGRADE_ERROR = "tts.upgrade.E.";
	
	public static final int DEFAULT_STREAM_TYPE = -1;
	static final String DEFAULT_SPEAK_TEXT = "";
	static final PreemptType DEFAULT_PREEMPT_FLAG = PreemptType.PREEMPT_TYPE_NONE;
	static final ITtsCallback DEFAULT_TTS_CALLBACK = null;
	
	public static final String BEEP_VOICE_URL = "$BEEP";
	static final long DEFAULT_TIMEOUT = 10 * 60 * 1000;// 丢弃TTS的超时时间

	private String packageName = null;

	public String getPackageName(){
		return packageName;
	}

	public void setPackageName(String packageName){
		this.packageName = packageName;
	}
	
	long ttsDelay = 0;//tts播报延时，只有需要抢焦点时才会进行延时
	
	public boolean needProDelay = false;//唤醒保护是否需要延时
	
	// 避免误唤醒的保护引擎 0,不启用 1,前置保护引擎 2,同步保护引擎 3,切割词保护引擎
	// public int PRE_AVOID_WAKEUP_PROTECT_ENABLE = 0;
	
	String[] beginKws = new String[]{"将为","已为","已切换为","已经是","重新规划"};
	
	String ttsText = "";//当前正在播放的tts文本

	public static final int DELAY_CLEAR_TIME = 1500;
	
	static TtsManager sModuleInstance = new TtsManager();

	private TtsManager() {
		mInited = false;
		mInitSuccessed = false;
	}

	public static TtsManager getInstance() {
		return sModuleInstance;
	}

	public void initializeComponent() {
		if (mTts != null) {
			JNIHelper.logw("TTS has been instantiated");
			return;
		}
		// 启动初始化默认的TTS
		mTts = new TtsMix();
		ITts.IInitCallback callback = new ITts.IInitCallback() {
			@Override
			public void onInit(boolean bSuccess) {
				JNIHelper.logd("init tts: " + bSuccess);
				mInited = true;
				mInitSuccessed = bSuccess;
				speakNext();
				TXZService.checkSdkInitResult();
				if (mInitSuccessed) {
					checkTtsThemeUpgrade();
				} else {
					MonitorUtil.monitorCumulant(MONITOR_TTS_INIT_ERROR + mCurrThemeId);
				}
			}
		};
		configTTSTheme(callback);
		MonitorUtil.monitorCumulant(MONITOR_TTS_THEME + mCurrThemeId);

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
	
	@Override
	public int initialize_addPluginCommandProcessor() {
		PluginManager.addCommandProcessor("txznet.tts.command.", new CommandProcessor() {

			@Override
			public Object invoke(String command, Object[] args) {
				try {
					if ("changeTheme".equals(command)) { // Integer, String
						if (args == null || args.length < 2) {
							LogUtil.loge("plugin error: arguments error");
							return false;
						}
						int themeId = (Integer) args[0];
						byte[] strTheme = (null == args[1] ? null: ((String) args[1]).getBytes());
						return setCurrTheme(strTheme, themeId, System.currentTimeMillis());
					}
				} catch (Exception e) {
					e.printStackTrace();
					JNIHelper.loge("plugin args error: " + e.toString());
				}
				return null;
			}
		});
		return super.initialize_addPluginCommandProcessor();
	}

	// ///////////////////////////////////////////////////////////////////

	/**
	 * 监听第三方音频焦点的Receiver
	 */
	BroadcastReceiver mWxResReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (TXZMediaFocusManager.INTENT_FOCUS_GAINED.equals(intent.getAction())) {
				if (mEnableDownVolume) {
					float rate = 0.4f;
					try {
						rate = (float) TXZFileConfigUtil.getDoubleSingleConfig(TXZFileConfigUtil.KEY_TTS_DOWN_VOLUME_RATE, 0.4f);
					} catch (Exception e) {

					}
					LogUtil.logd("tts:setVolRate:"  + rate);
					setVolumeRate(rate);
				}
			} else if (TXZMediaFocusManager.INTENT_FOCUS_RELEASED.equals(intent.getAction())) {
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
	}

	public int speakText(int iStream, String sText, PreemptType bPreempt) {
		return speakText(iStream, sText, bPreempt, DEFAULT_TTS_CALLBACK);
	}

	public int speakText(int iStream, String sText, ITtsCallback oRun) {
		return speakText(iStream, sText, DEFAULT_PREEMPT_FLAG, oRun);
	}

	public int speakText(int iStream, String sText) {
		return speakText(iStream, sText, DEFAULT_PREEMPT_FLAG, DEFAULT_TTS_CALLBACK);
	}

	public int speakText(String sText, PreemptType bPreempt, ITtsCallback oRun) {
		return speakText(DEFAULT_STREAM_TYPE, sText, bPreempt, oRun);
	}

	public int speakText(String sText, PreemptType bPreempt) {
		return speakText(DEFAULT_STREAM_TYPE, sText, bPreempt, DEFAULT_TTS_CALLBACK);
	}

	public int speakText(String sText, ITtsCallback oRun) {
		return speakText(DEFAULT_STREAM_TYPE, sText, DEFAULT_PREEMPT_FLAG, oRun);
	}

	public int speakText(String sText) {
		return speakText(DEFAULT_STREAM_TYPE, sText, DEFAULT_PREEMPT_FLAG, DEFAULT_TTS_CALLBACK);
	}

	public int speakText(int iStream, String sText, PreemptType bPreempt, ITtsCallback oRun) {
		return speak(iStream, sText, bPreempt, oRun, false);
	}

	/*******************************************
	 * 播放tts时即使开启了回音消除，也不允许唤醒
	 *******************************************/
	public int speakTextNoWakeup(int iStream, String sText, PreemptType bPreempt) {
		return speakTextNoWakeup(iStream, sText, bPreempt, DEFAULT_TTS_CALLBACK);
	}

	public int speakTextNoWakeup(int iStream, String sText, ITtsCallback oRun) {
		return speakTextNoWakeup(iStream, sText, DEFAULT_PREEMPT_FLAG, oRun);
	}

	public int speakTextNoWakeup(int iStream, String sText) {
		return speakTextNoWakeup(iStream, sText, DEFAULT_PREEMPT_FLAG, DEFAULT_TTS_CALLBACK);
	}

	public int speakTextNoWakeup(String sText, PreemptType bPreempt, ITtsCallback oRun) {
		return speakTextNoWakeup(DEFAULT_STREAM_TYPE, sText, bPreempt, oRun);
	}

	public int speakTextNoWakeup(String sText, PreemptType bPreempt) {
		return speakTextNoWakeup(DEFAULT_STREAM_TYPE, sText, bPreempt, DEFAULT_TTS_CALLBACK);
	}

	public int speakTextNoWakeup(String sText, ITtsCallback oRun) {
		return speakTextNoWakeup(DEFAULT_STREAM_TYPE, sText, DEFAULT_PREEMPT_FLAG, oRun);
	}

	public int speakTextNoWakeup(String sText) {
		return speakTextNoWakeup(DEFAULT_STREAM_TYPE, sText, DEFAULT_PREEMPT_FLAG, DEFAULT_TTS_CALLBACK);
	}
	
	public int speakTextNoWakeup(int iStream, String sText, PreemptType bPreempt, ITtsCallback oRun) {
		return speak(iStream, sText, bPreempt, oRun, true);
	}

	public int speakVoice(String sText, String voiceUrl, PreemptType bPreempt) {
		return speakVoice(sText, voiceUrl, bPreempt, null);
	}

	public int speakVoice(String sText, String voiceUrl) {
		return speakVoice(sText, voiceUrl, DEFAULT_PREEMPT_FLAG, DEFAULT_TTS_CALLBACK);
	}

	public int speakVoice(String voiceUrl, PreemptType bPreempt) {
		return speakVoice(DEFAULT_SPEAK_TEXT, voiceUrl, bPreempt, DEFAULT_TTS_CALLBACK);
	}

	public int speakVoice(String voiceUrl) {
		return speakVoice(DEFAULT_SPEAK_TEXT, voiceUrl, DEFAULT_PREEMPT_FLAG, DEFAULT_TTS_CALLBACK);
	}

	public int speakVoice(String sText, String voiceUrl, ITtsCallback oRun) {
		return speakVoice(sText, voiceUrl, DEFAULT_PREEMPT_FLAG, oRun);
	}

	public int speakVoice(String voiceUrl, PreemptType bPreempt, ITtsCallback oRun) {
		return speakVoice(DEFAULT_SPEAK_TEXT, voiceUrl, bPreempt, oRun);
	}

	public int speakVoice(String voiceUrl, ITtsCallback oRun) {
		return speakVoice(DEFAULT_SPEAK_TEXT, voiceUrl, DEFAULT_PREEMPT_FLAG, oRun);
	}

	public int speakVoice(String sText, String voiceUrl, PreemptType bPreempt, ITtsCallback oRun) {
		return speakVoice(DEFAULT_STREAM_TYPE, sText, new String[] { voiceUrl }, bPreempt, oRun);
	}

	public int speakVoice(int iStream, String sText, String[] voiceUrls, PreemptType bPreempt, ITtsCallback oRun) {
		return speakVoice(iStream, sText, voiceUrls, bPreempt, false, oRun);
	}

	public int speakVoice(int iStream, String sText, String[] voiceUrls,
			PreemptType bPreempt, Boolean fromRemote, ITtsCallback oRun) {
		return speakVoice(iStream, sText, voiceUrls, ttsDelay, bPreempt, fromRemote, oRun);
	}
	
	public int speakVoice(int iStream, String sText, String[] voiceUrls, long delay,
			PreemptType bPreempt, boolean fromRemote, ITtsCallback oRun) {
		return speak(iStream, sText, voiceUrls, bPreempt, oRun, fromRemote, false, delay);
	}
	
	public int speakVoiceTask(PreemptType bPreempt, VoiceTask[] voiceTasks, ITtsCallback oRun) {
		return speakVoiceTask(DEFAULT_STREAM_TYPE, DEFAULT_SPEAK_TEXT, null, 0, bPreempt, false, voiceTasks, oRun);
	}

	public int speakVoiceTask(int iStream, String sText, String[] voiceUrls, long delay,
			PreemptType bPreempt, boolean fromRemote, VoiceTask[] voiceTasks, ITtsCallback oRun) {
		JNIHelper.logd("speakText: tasks stream=" + iStream + " ,text=" + sText
				+ ",url=" + Arrays.toString(voiceUrls) + ",delay=" + delay
				+ ",bPreempt=" + bPreempt + ",fromRemote=" + fromRemote
				+ ",voiceTasks=" + Arrays.toString(voiceTasks));
		// 创建新的任务
		TtsTask t = new TtsTask();
		t.iStream = iStream;
		t.sText = sText;
		t.lstVoice = voiceUrls;
		t.oRun = oRun;
		t.fromRemote = fromRemote;
		if (delay > 0) {
			// TODO 外部延时接口没有设值时，内部请求焦点延时接口无效
			t.delay = delay;
		}
		t.voiceTasks = voiceTasks;
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

	public int speak(int iStream, String sText, PreemptType bPreempt, ITtsCallback oRun, boolean forceStopWakeup) {
		return speak(iStream, sText, null, bPreempt, oRun, false, forceStopWakeup, ttsDelay);
	}
	
	public int speak(int iStream, String sText, String[] voiceUrls, PreemptType bPreempt, ITtsCallback oRun, boolean fromRemote, boolean forceStopWakeup, long delay) {
/*		String.format(
				"speakText: stream=%d,text=%s,url=%s,delay=%d,bPreempt=%s,fromRemote=%b,forceStopWakeup=%b",
				iStream, sText,
				(voiceUrls == null ? null : Arrays.toString(voiceUrls)), delay,
				bPreempt.toString(), fromRemote, forceStopWakeup);*/
		JNIHelper.logd("speakText: stream=" + iStream + " ,text=" + sText
				+ ",url=" + (voiceUrls == null ? null : Arrays.toString(voiceUrls)) 
				+ ",delay="+ delay +",bPreempt=" + bPreempt
				+ ",fromRemote=" + fromRemote + ",forceStopWakeup=" + forceStopWakeup);
		// 创建新的任务
		TtsTask t = new TtsTask();
		t.iStream = iStream;
		t.sText = sText;
		t.lstVoice = voiceUrls;
		t.oRun = oRun;
		t.fromRemote = fromRemote;
		t.forceStopWakeup = forceStopWakeup;
		t.delay = delay;
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
	public static final int INVALID_TTS_TASK_ID = 0;

	ITts mTts = null;
	TtsRemoteImpl mRemoteTool = new TtsRemoteImpl();

	ITts getTtsTool() {
		ITts iTts;
		iTts = TtsRemoteImpl.useRemoteTtsTool() ? mRemoteTool : mTts;
		return iTts;
	}

	int mNextTaskId = 1; // 下一次分配给Tts的任务ID
	List<TtsTask> mTtsTaskQueue = new ArrayList<TtsTask>(); // TTS的等待任务列表
	TtsTask mCurTask = null; // 当前的Tts任务

	public class TtsTask {
		int iTaskId = INVALID_TTS_TASK_ID;
		int iStream = DEFAULT_STREAM_TYPE;
		String sText = "";
		/** tts播报偏移量 */
		int textOffset = 0;
		int iVoiceIndex = 0;
		String[] lstVoice = null; // 语音列表
		ITtsCallback oRun;
		long createdTime = 0;
		boolean fromRemote = false; // 标识任务是否来自第三方
		boolean forceStopWakeup = false; // 播放时即使开启了回音消除也不允许打断
		long delay = ttsDelay;
		
		/**
		 * 取消语音播报时，是否回调callback事件，
		 * true 表示调用callback，默认值；
		 * false 表示不调用回调，可以插入下次播报
		 */
		boolean isRealCancel = true; 

		public void enableForceStopWakeup(boolean froceStopWakeUp) {
			this.forceStopWakeup = froceStopWakeUp;
		}

		public boolean isForceStopWakeup() {
			return forceStopWakeup;
		}
		
		VoiceTask[] voiceTasks; // 任务播报序列
		int voiceTaskIndex; // 任务播报序列角标
	}
	
	
	
	private static final int TEXT_SPLIT_LEN = 200;
	
	private boolean speakTextWithSplit(TtsTask task) {
		if (task == null || task.sText == null) {
			return false;
		}
		int len = task.sText.length();
		int offset = task.textOffset;
		if (len <= offset) {
			return false;
		}
		int end = offset + TEXT_SPLIT_LEN;
		if (len <= end) {
			end = len;
		} else {
			int last = task.sText.lastIndexOf('。', end);
			if (offset < last) {
				end = last + 1;
			}
		}
		getTtsTool().start(task.iStream, replaceSpeakText(task.sText.substring(offset, end)), mSpeakEndCallback);
		task.textOffset = end;
		return true;
	}
	
	private String[] mOriginals;
	private String[] mReplaces;
	
	/** 替换TTS播报文本 */
	private String replaceSpeakText(String text) {
		//.replace("同行者", "同形者") // 云知声已经处理改多音字
		String result = text.replace("星期一", "星期1").replace("空调","空条").replace("调到","条到").replace("冇", "卯").replaceAll("G网络","机网络");
		result = result.replace("4S", "四S").replace("4s", "四S").replace("12月", "十二月").replace("2月", "二月" ).replace("月2号", "月二号");
		if (null == mOriginals) {
			return result;
		}
		for (int i = 0; i < mOriginals.length; i++) {
			// 替换的原始文本不允许为空，替换文本允许是空串
			if (TextUtils.isEmpty(mOriginals[i]) || null == mReplaces[i]) {
				continue;
			}
			result = result.replace(mOriginals[i], mReplaces[i]);
		}
		return result;
	}

	int speakText(TtsTask t) {
		if (t == null) {
			speakNext();
			return ERROR_SUCCESS;
		}
		if (t.oRun != null) {
			t.oRun.onBegin();
		}
		if (TextUtils.isEmpty(ImplCfg.getTtsImplClass())) {
			if (t.oRun != null) {
				AppLogic.runOnBackGround(new Runnable1<TtsTask>(t) {
					@Override
					public void run() {
						JNIHelper.logd("speakText end onError: id=" + mP1.iTaskId);
						mP1.oRun.onError(ERROR_ABORT);
						mP1.oRun.onEnd();
					}
				}, 0);
			}
			return ERROR_SUCCESS;
		}

		mCurTask = t;
		if (mCurTask.iStream == DEFAULT_STREAM_TYPE) {
			if (BluetoothManager.getInstance().isScoStateOn())
				mCurTask.iStream = AudioManager.STREAM_VOICE_CALL;
			else
				mCurTask.iStream = TtsUtil.DEFAULT_TTS_STREAM; // 默认使用通知的通道
		}
		// 校验通道的音量，太小给出震动提示
		VolumeManager.getInstance().checkVolume(mCurTask.iStream, true, true);

		if (SystemClock.elapsedRealtime() - t.createdTime > DEFAULT_TIMEOUT) {
			JNIHelper.loge("speakText createTime:" + t.createdTime + ",drop overtimed tts task!");
			cancelCurTask();
			return ERROR_SUCCESS;
		}
		
		JNIHelper.logd("speakText begin: id=" + t.iTaskId + ",stream=" + t.iStream + ",text=" + t.sText + ",sco="
				+ BluetoothManager.getInstance().isScoStateOn());
		// 上报数据
		ReportUtil.doReport(new ReportUtil.Report.Builder().setType("tts").setAction("begin")
				.putExtra("theme", mStrCurrTheme).setSessionId().buildCommReport());
		
		// 判断条件提权，先播报文本是否为空
		if (TextUtils.isEmpty(mCurTask.sText) && (mCurTask.voiceTasks == null || mCurTask.voiceTasks.length == 0)
				&& (mCurTask.lstVoice == null || mCurTask.lstVoice.length == 0)) {
			speakNextVoice();
			return ERROR_SUCCESS;
		}
		if (!TextUtils.isEmpty(t.sText)) {
			// 不去过滤多余空格，只有去搜索录音文件时才会过滤
			t.sText = ZHConverter.convert(t.sText, ZHConverter.SIMPLIFIED);
		}

		int filterIndex = -1;
		do {
			if (TextUtils.isEmpty(t.sText)) {
				break;
			}
			// String strTextUrl = getTextUrlFromZip(t.sText.trim(), mCurrThemeId + "", mStrCurrRole);
			String strTextUrl = getAudioUrl(t.sText.trim(), mCurrTheme);
			if (TextUtils.isEmpty(strTextUrl)) {
				break;
			}
			String[] voice = null;
			if (t.lstVoice == null) {
				voice = new String[1];
				voice[0] = strTextUrl;
			} else {
				int length = t.lstVoice.length;
				voice = new String[length + 1];
				voice[0] = strTextUrl;
				for (int i = 1; i < length + 1; i++) {
					voice[i] = t.lstVoice[i - 1];
				}
			}
			t.lstVoice = voice;
			filterIndex = 0;
		} while (false);

		if (mCurTask == null || TextUtils.isEmpty(mCurTask.sText)) {
			// 文本为空时强制跳过文本播报
			filterIndex = 0;
		}
		ttsText = mCurTask.sText;
		checkProWakeup(WakeupManager.getInstance().keyWords);
		
		// 统计TTS说法 andyzhao 2016-06-01
		TXZStatisticser.append(mCurTask.sText);
		
		long mDelay = 0;
		if (!MusicFocusManager.getInstance().hasAudioFocus()) {//需要抢焦点
			mDelay = mCurTask.delay;
		}
		// 将TTS开始播报监听提前开始执行
		MusicManager.getInstance().onBeginTts(mCurTask.iStream, mCurTask);
		
		RecorderWin.addInterruptKws();
			
		if (filterIndex == 0 && mCurTask.sText != null) {
			// 等于0时跳过 TTS 文本播报
			mCurTask.textOffset = mCurTask.sText.length();
		}
		
		AppLogic.removeBackGroundCallback(mRunnableSpeakTextDelay);
		if (mDelay > 0) {
			AppLogic.runOnBackGround(mRunnableSpeakTextDelay, mDelay);
		} else {
			mRunnableSpeakTextDelay.run();
		}
		return ERROR_SUCCESS;
	}
	
	private Runnable mRunnableSpeakTextDelay = new Runnable() {
		public void run() {
			speakNextVoice();
			if (mCurTask != null) {
				//TTS打断使用
				InterruptTts.getInstance().startAsr(mCurTask.oRun,mCurTask.sText,mCurTask.iTaskId);
			}
		}
	};
	
	/**
	 * 检查是否打开保护引擎
	 */
	public void checkProWakeup(String[] keyWords){
		JNIHelper.logd("needProtectWakeup = "+ProjectCfg.getProtectWakeupType());
		if (ProjectCfg.getProtectWakeupType() == 1) {
			List<String[]> cmds = handlePreWakeupCmds(ttsText, keyWords);
			if (!cmds.isEmpty()) {// 当返回的唤醒词不为空
				startPreWakeup(cmds, checkHasRefSingal());
			}
		}else if(ProjectCfg.getProtectWakeupType() == 2) {
			if (hasWakeupCmds(ttsText, keyWords)) {
				needProDelay = true;
				startSyncWakeup(checkHasRefSingal());
			}else{
				needProDelay = false;
			}
		}else if(ProjectCfg.getProtectWakeupType() == 3) {
			List<String[]> cmds = handleSplitWakeupCmds(ttsText, keyWords);
			if(!cmds.isEmpty()) {
				startPreWakeup(cmds, checkHasRefSingal());
			}
		}
		
		ArrayList<String> banKws = handleSpecialText(ttsText, keyWords, beginKws);
		if(banKws != null && !banKws.isEmpty()){//返回的唤醒词list不为空
			WakeupManager.getInstance().putPreCmds(banKws, WakeupManager.WAKEUP_PRO_END_TIME);
		}
	}
	
	/**
	 * 检查是否有参考信号
	 * @return
	 */
	private boolean checkHasRefSingal(){
		if(!ProjectCfg.mEnableAEC){
			return false;
		}else{
			if(FmManager.getInstance().fmeEnable && !FmManager.getInstance().hasRefSingal){
				return false;
			}
			return true;
		}
	}
	
	/**
	 * 处理播报文本，防止出现误打断导致无限死循环
	 */
	private ArrayList<String> handleSpecialText(String text, String[] kWs, String[] beginKws) {
		if(TextUtils.isEmpty(text)){
			return null;
		}
		for (String bKw : beginKws) {
			if(text.indexOf(bKw) != -1){
				return checkKws(text, kWs);
			}
		}
		return null;
	}
	
	/**
	 * 处理文本，筛选可能造成死循环的唤醒词
	 * @param kWs 
	 * @param text 
	 * @return
	 */
	private ArrayList<String> checkKws(String text, String[] kWs) {
		if(kWs == null || kWs.length == 0){
			return null;
		}
		boolean isSelKw = false;//判断唤醒词位置相邻的词是否是“或”和“还是”的标志位
		ArrayList<String> list = new ArrayList<String>();
		for (String kW : kWs) {
			if(TextUtils.isEmpty(kW)){
				continue;
			}
			int pos = text.indexOf(kW);
			if(pos == -1){
				continue;
			}
			if(pos-1 >= 0 && TextUtils.equals(text.substring(pos-1, pos), "或")){//关键词前一个字存在，判断关键词前面一个字是否为“或”,下同
				isSelKw = true;
			}
			if(pos+kW.length()+1 <= text.length()-1 && TextUtils.equals(text.substring(pos+kW.length(), pos+kW.length()+1), "或")){
				isSelKw = true;
			}
			if(pos-2 >= 0 && TextUtils.equals(text.substring(pos-2, pos), "还是")){
				isSelKw = true;
			}
			if(pos+kW.length()+2 <= text.length()-1 && TextUtils.equals(text.substring(pos+kW.length(), pos+kW.length()+2), "还是")){
				isSelKw = true;
			}
			if(!isSelKw){
				JNIHelper.logd("handleSpecialText kW:"+kW);
				list.add(kW);
			}else{
				isSelKw = false;
			}
		}
		
		if(list.contains("确定")){
			list.remove("确定");
		}
		return list;
	}
	
	// 处理需要抢占焦点时的逻辑
	/*
	 * private void requestMediaFocus(){ if(mCurTask.fromRemote &&
	 * TXZMediaFocusManager.getInstance().isFocusGained()){ setVolumeRate(0.4f);
	 * }else{ setVolumeRate(1.0f);
	 * MusicManager.getInstance().onBeginTts(mCurTask.iStream); } }
	 */
	

	/**
	 * 避免误唤醒操作，开启一个前置唤醒词的唤醒
	 */
	private void startPreWakeup(List<String[]> cmds, boolean isRef) {
		WakeupManager.getInstance().cmdList = cmds;
		AppLogic.removeBackGroundCallback(closeProWakeup);
		WakeupManager.getInstance().startPreWakeup(isRef);
	}
	
	private void startSyncWakeup(boolean isRef) {
		AppLogic.removeBackGroundCallback(closeProWakeup);
		WakeupManager.getInstance().startSyncWakeup(isRef);
	}
	
	/**
	 * 语音播报结束后结束前置唤醒引擎
	 */
	public void stopProWakeup() {
		AppLogic.removeBackGroundCallback(closeProWakeup);
		AppLogic.runOnBackGround(closeProWakeup, DELAY_CLEAR_TIME);
	}
	
	Runnable closeProWakeup = new Runnable() {
		public void run() {
			WakeupManager.getInstance().clearProCmds();
			WakeupManager.getInstance().stopProWakeup();
		}
	};
	
	/**
	 * 对进行播报的文本进行处理，得到前置唤醒词，用于开启新的唤醒
	 * @param sText 
	 * @param userKws 
	 * @param sdkKws 
	 * @return
	 */
	private List<String[]> handlePreWakeupCmds(String sText, String[] userKws){
		JNIHelper.logd("handlePreWakeupCmds sText:"+sText);
		if(sText == null){
			return null;
		}
		List<String[]> list = new ArrayList<String[]>();
		
		if(userKws != null){
			for (String uKw : userKws) {
				JNIHelper.logd("analysisText uKw:"+uKw);
				int pos =sText.indexOf(uKw);
				if(pos < 0 || pos > sText.length()-uKw.length()){//未找到或位置异常
					continue;
				}else if(pos == 0){//文本首位
					String preKw = "";
					if(uKw.length() > 1){
						preKw = sText.substring(0,uKw.length()-1);
					}else{//唤醒词位于首位，且为单字，这种是不建议设置的唤醒词，不处理
						continue;
					}
					list.add(new String[]{preKw,uKw});
				}else{
					String preKw = sText.substring(pos-1, pos-1+uKw.length());
					list.add(new String[]{preKw,uKw});
				}
			}
		}
		return list;
	}
	
	/**
	 * 对进行播报的文本进行处理，将唤醒词减小一位，去掉最后一个字，用于开启新的唤醒
	 * @param sText
	 * @param keyWords
	 * @return
	 */
	private List<String[]> handleSplitWakeupCmds(String sText, String[] keyWords) {
		JNIHelper.logd("handleSplitWakeupCmds sText:"+sText);
		if(sText == null){
			return null;
		}
		List<String[]> list = new ArrayList<String[]>();
		if(keyWords != null){
			for (String kW : keyWords) {
				if(TextUtils.isEmpty(kW)){
					continue;
				}
				JNIHelper.logd("analysisText uKw:"+kW);
				int pos =sText.indexOf(kW);
				if(pos < 0 || pos > sText.length()-kW.length()){//未找到或位置异常
					continue;
				} else {
					String preKw = "";
					if(kW.length() > 1){
						preKw = kW.substring(0, kW.length()-1);
					}else{//单字唤醒词，这种是不建议设置的唤醒词，不处理
						continue;
					}
					list.add(new String[]{preKw,kW});
				}
			}
		}
		return list;
	}

	/**
	 * 设置当前播报的tts文本
	 */
	public void setTtsText(String text){
		ttsText = text;
	}
	
	public String getTtsText(){
		return ttsText;
	}
	
	/**
	 * 播报文本中含有唤醒词
	 * @param sText
	 * @param sdkKws
	 * @param userKws
	 * @return
	 */
	private boolean hasWakeupCmds(String sText, String[] userKws){
//		JNIHelper.logd("hasWakeupCmds sText:"+sText);
//		for (String s : userKws) {
//			JNIHelper.logd("hasWakeupCmds KeyWords:"+s);
//		}
		if(sText == null){
			return false;
		}
		if(userKws != null){
			for (String uKw : userKws) {
				if(TextUtils.isEmpty(uKw)){
					continue;
				}
				int pos =sText.indexOf(uKw);
				if(pos != -1){
					JNIHelper.logd("hasWakeupCmds true sText:"+sText);
					return true;
				}
			}
		}
		return false;
	}
	
	AudioTrackPlayer mThemePlayer = null;
	byte[] secretKey = null;

	void speakNextVoice() {
		AppLogic.removeBackGroundCallback(mRunnableSpeakNextVoice);
		AppLogic.runOnBackGround(mRunnableSpeakNextVoice, 0);
	}
	
	private void speakNextVoice(long delay) {
		AppLogic.removeBackGroundCallback(mRunnableSpeakNextVoice);
		AppLogic.runOnBackGround(mRunnableSpeakNextVoice, delay);
	}

	Runnable mRunnableSpeakNextVoice = new Runnable() {
		@Override
		public void run() {
			if (mCurTask == null
					|| (mCurTask.voiceTasks != null
							&& mCurTask.voiceTasks.length > 0 && mCurTask.voiceTaskIndex >= mCurTask.voiceTasks.length) // 子任务数组不为空且播报完成
					|| ((mCurTask.voiceTasks == null || mCurTask.voiceTasks.length == 0)
							&& (mCurTask.sText == null || mCurTask.textOffset >= mCurTask.sText
									.length()) && (mCurTask.lstVoice == null || mCurTask.iVoiceIndex >= mCurTask.lstVoice.length))) {
				onSuccessCallback();
				return;
			}
			
			if (mCurTask.voiceTasks != null && mCurTask.voiceTasks.length > 0
					&& mCurTask.voiceTaskIndex < mCurTask.voiceTasks.length) {
				VoiceTask voiceTask = mCurTask.voiceTasks[mCurTask.voiceTaskIndex];
				mCurTask.voiceTaskIndex++;
				if (voiceTask == null) {
					JNIHelper.logd("speakText: voiceTask == null");
					speakNextVoice();
					return;
				}
				switch (voiceTask.type) {
				case TEXT:
					if (TextUtils.isEmpty(voiceTask.text)) {
						JNIHelper.logd("speakText: voiceTask.text is empty");
						speakNextVoice();
						return;
					}
					getTtsTool().start(mCurTask.iStream, replaceSpeakText(voiceTask.text), mSpeakEndCallback);
					return;
				case BEEP:
					//因为beep声播报无法取消，可能导致这个任务执行过程中，父任务已经被cancel掉，新的任务被执行，导致结束回调影响新的任务。因此记录下父任务的ID以做丢弃处理。
					final int curTaskId = mCurTask.iTaskId;
					BeepPlayer.play(mCurTask.iStream, new Runnable() {
						@Override
						public void run() {
							if(mCurTask != null && curTaskId == mCurTask.iTaskId){
								speakNextVoice();
							}
						}
					});
					return;
				case QUIET:
					speakNextVoice(voiceTask.duration);
					return;
				case LOCAL_URL:
					playAuido(voiceTask.url);
					return;
				case NET_URL:
				case ALERT:
				default:
					JNIHelper.logw("unkown voice task type:" + voiceTask.type);
					speakNextVoice();
					return;
				}
			}
			
			if (speakTextWithSplit(mCurTask)) {
				return;
			}
			
			// 播放下一段声音文件
			JNIHelper.logd("begin play voice[" + mCurTask.iVoiceIndex + "/" + mCurTask.lstVoice.length + "]=[" + mCurTask.lstVoice[mCurTask.iVoiceIndex] + "]");
			if (TextUtils.isEmpty(mCurTask.lstVoice[mCurTask.iVoiceIndex])) {
				mCurTask.iVoiceIndex++;
				speakNextVoice();
				return;
			}
			// 播放BEEP
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
			// 播放主题
			if (mCurTask.lstVoice[mCurTask.iVoiceIndex].startsWith(TTS_THEME_HEAD)) {
				if (mThemePlayer == null) {
					mThemePlayer = new AudioTrackPlayer();
				}
				secretKey = getKey();
				mThemePlayer.setAudioStreamType(mCurTask.iStream);
				mThemePlayer.setOnCompletionListener(new AudioTrackPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(AudioTrackPlayer player) {
						JNIHelper.loge("speakText: play theme voice onCompletion");
						speakNextVoice();
					};

				});
				mThemePlayer.setDecryption(new AudioTrackPlayer.Decryption() {
					@Override
					public void decrypt(byte[] data, int offset, int size, long offsetInFile) {
						if(mCurrTheme.isEncrypt()) {
							decryptAudio(data, offset, size, offsetInFile, secretKey);
						}
					}
				});
				TtsThemeUrl oUrl = parseTtsUrl(mCurTask.lstVoice[mCurTask.iVoiceIndex]);
				try {
					if(!mCurrTheme.isEncrypt()) {
						mThemePlayer.setDataSource(mCurrTheme.getAudioPath(oUrl.strText));
					} else {
						mThemePlayer.setDataSource(oUrl.strZipPath, oUrl.strRole + "/" + oUrl.strText + oUrl.strSuffix);
					}
				} catch (Exception e) {
					JNIHelper.loge("speakText: play voice failed " + e.toString());
					mCurTask.iVoiceIndex++;
					speakNextVoice();
					return;
				}
				mCurTask.iVoiceIndex++;
				mThemePlayer.prepare();
				mThemePlayer.start();
				return;
			}
			
			String url = mCurTask.lstVoice[mCurTask.iVoiceIndex];
			mCurTask.iVoiceIndex++;
			playAuido(url);
		}
	};
	
	TXZAudioPlayer mAudioPlayer = null;
	private void playAuido(String path) {
		JNIHelper.logd("speakText: play voice " + path);
		if(TextUtils.isEmpty(path)) {
			speakNextVoice();
			return;
		}
		if (mAudioPlayer != null) {
			mAudioPlayer.release();
			mAudioPlayer = null;
		}
		mAudioPlayer = new FFMPEGAudioPlayer(mCurTask.iStream, path);
		mAudioPlayer.setVolume(mTtsVolumeRate, mTtsVolumeRate);
		mAudioPlayer.setOnPreparedListener(new TXZAudioPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(TXZAudioPlayer ap) {
				ap.start();
				if (mCurTask != null) {
					MusicManager.getInstance().onBeginTts(mCurTask.iStream, mCurTask);
				} else {
					speakNext();
				}
			}
		});
		mAudioPlayer.setOnCompletionListener(new TXZAudioPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(TXZAudioPlayer ap) {
				if (mAudioPlayer != null) {
					mAudioPlayer.reset();
					mAudioPlayer.release();
					mAudioPlayer = null;
				}
				speakNextVoice();
			}
		});
		mAudioPlayer.setOnErrorListener(new TXZAudioPlayer.OnErrorListener() {
			@Override
			public boolean onError(TXZAudioPlayer ap, Error err) {
				JNIHelper.loge("play voice error:" + err.toString());
				if (mAudioPlayer != null) {
					mAudioPlayer.reset();
					mAudioPlayer.release();
					mAudioPlayer = null;
				}
				speakNextVoice();
				return false;
			}
		});
		mAudioPlayer.prepareAsync();
	}

	/*private void playMediaVoice(String path) {
		if(TextUtils.isEmpty(path)) {
			speakNextVoice();
			return;
		}
		if(mVoicePlayer == null) {
			mVoicePlayer = new MediaPlayer();
			mVoicePlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					mVoicePlayer.start();
					if (mCurTask != null) {
						MusicManager.getInstance().onBeginTts(mCurTask.iStream, mCurTask);
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
					// TODO 威仕特的专车专用设备（K27，云智）播报后系统静音
					try {    
				        Class<?> c = Class.forName("android.os.SystemProperties");  
				        Method set = c.getMethod("set", String.class, String.class);
				        set.invoke(c, "sys.ttsplaying", "false");
				    } catch (Exception e) {
				    	JNIHelper.logd("speakText MediaPlayer:set ttsplaying failed");
				        e.printStackTrace();
				    }  
				}
			});
		}
		mVoicePlayer.reset();
		mVoicePlayer.setAudioStreamType(mCurTask.iStream);
		mVoicePlayer.setVolume(mTtsVolumeRate, mTtsVolumeRate);
		try {
			mVoicePlayer.setDataSource(path);
		} catch (Exception e) {
			JNIHelper.loge("play voice setDataSource error[" + path + "] " + e.toString());
			speakNextVoice();
			return;
		}
		mVoicePlayer.prepareAsync();
	}*/

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
	
	/** TTS onError 回调处理逻辑, 需要保证在 TTS 线程调用 */
	private void onErrorCallback(int iError) {
		if (mCurTask != null) {
			JNIHelper.loge("speakText end: id=" + mCurTask.iTaskId + ",stream=" + mCurTask.iStream + ",text="
					+ mCurTask.sText + ",error=" + iError);
		}
		// 切换线程防止阻塞当前播报
		AppLogic.runOnBackGround(new Runnable2<TtsTask, Integer>(mCurTask, iError) {
			@Override
			public void run() {
				if (mP1 != null && mP1.oRun != null) {
					JNIHelper.logd("speakText end onError: id=" + mP1.iTaskId);
					mP1.oRun.onError(mP2);
					mP1.oRun.onEnd();
					mP1.oRun = null; // 防止二次回调异常
				}
			}
		}, 0);
		mCurTask = null;
		speakNext();
	}
	
	/** TTS onCancel 回调处理逻辑, 需要保证在 TTS 线程调用 */
	private void onCancelCallback() {
		do {
			if (mCurTask != null) {
				JNIHelper.logd("speakText end: id=" + mCurTask.iTaskId + ",stream=" + mCurTask.iStream + ",text="
						+ mCurTask.sText);
			} else {
				break;
			}
			
			if (cancelTaskId != mCurTask.iTaskId) {
				JNIHelper.loge("speakText : mCurTask.iTaskId=" + mCurTask.iTaskId + ",id=" + cancelTaskId);
				break; // 防止多次cancel TTS时一个任务多个回调
			}
			
			// 上报数据
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType("tts").setAction("end")
					.setSessionId().buildCommReport());
			
			AppLogic.runOnBackGround(new Runnable1<TtsTask>(mCurTask) {
				@Override
				public void run() {
					if (mP1 != null && mP1.oRun != null) {
						// callback不为空时，isRealCancel值才有价值
						if (mP1.isRealCancel) {
							JNIHelper.logd("speakText end onCancel: id=" + mP1.iTaskId);
							mP1.oRun.onCancel();
							mP1.oRun.onEnd();
							mP1.oRun = null; // 防止二次回调异常
						} else {
							// 下次取消还是要回调的
							mP1.isRealCancel = true;
						}
					}
				}
			}, 0);
			mCurTask = null;
		} while (false);
		speakNext();
	}
	
	/** TTS onSuccess 回调处理逻辑, 需要保证在 TTS 线程调用 */
	private void onSuccessCallback() {
		if (mCurTask != null) {
			JNIHelper.logd("speakText end: id=" + mCurTask.iTaskId + ",stream=" + mCurTask.iStream + ",text="
					+ mCurTask.sText);
		}
		// 上报数据
		ReportUtil.doReport(new ReportUtil.Report.Builder().setType("tts").setAction("end")
				.setSessionId().buildCommReport());

		AppLogic.runOnBackGround(new Runnable1<TtsTask>(mCurTask) {
			@Override
			public void run() {
				if (mP1 != null && mP1.oRun != null) {
					JNIHelper.logd("speakText end onSuccess: id=" + mP1.iTaskId);
					mP1.oRun.onSuccess();
					mP1.oRun.onEnd();
					mP1.oRun = null; // 防止二次回调异常
				}
			}
		}, 0);
		mCurTask = null;
		speakNext();
	}
	
	private int cancelTaskId; // 当前取消任务ID
	
	/** TTS 播报回调，需要在回调中切换到 TTS 线程中 */
	ITtsCallback mSpeakEndCallback = new ITtsCallback() {
		@Override
		public void onError(int iError) {
			JNIHelper.loge("speakText : onError");
			AppLogic.runOnBackGround(new Runnable1<Integer>(iError) {
				@Override
				public void run() {
					onErrorCallback(mP1);
				}
			}, 0);
		}

		@Override
		public void onCancel() {
			JNIHelper.logd("speakText : onCancel");
			AppLogic.runOnBackGround(new Runnable() {
				@Override
				public void run() {
					onCancelCallback();
				}
			}, 0);
		}

		@Override
		public void onSuccess() {
			JNIHelper.logd("speakText : next text");
			speakNextVoice();
		}
	};

	boolean canSpeakNow() {
		if (getTtsTool() == null // 组件未构造
				|| isInitSuccessed() == false // 初始化未完成
				|| mCurTask != null // 当前有TTS任务
				|| AsrManager.getInstance().canSpeakTts() // 正在录音中
				|| RecordManager.getInstance().isBusy()
				|| (!CallManager.getInstance().isIdle() && !CallManager.getInstance().isRinging())// 电话忙，并且不是来电响铃
				|| TXZPowerControl.isEnterReverse() //在倒车影像状态中
				)
			return false;
		return true;
	}

	public void insertSpeakTask(PreemptType bPreempt, TtsTask t) {
		t.createdTime = SystemClock.elapsedRealtime();
		AppLogic.runOnBackGround(new Runnable2<PreemptType, TtsTask>(bPreempt, t) {
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
							mCurTask.isRealCancel = false;
							final TtsTask mOldCurTask = mCurTask;
							cancelCurTask();
							AppLogic.runOnBackGround(new Runnable() {

								@Override
								public void run() {
									insertSpeakTask(PreemptType.PREEMPT_TYPE_NEXT, mOldCurTask);
								}
							}, 200);
						}
					}
					return;
				}
				// 直接合成
				if (ITts.ERROR_SUCCESS != speakText(t)) {
					if (t.oRun != null) {
						JNIHelper.logd("speakText end onError: id=" + t.iTaskId);
						t.oRun.onError(ITts.ERROR_UNKNOW);
						t.oRun.onEnd();
					}
				}
			}
		}, 0);
	}

	// /////////////////////////////////////////////////////////////////

	void cancelCurTask() {
		// 不用处理当前mCurTask，stop后会回调speakEnd，里面会进行处理
		if (null != mCurTask) {
			// 防止多次cancel TTS时一个任务多个回调
			if (cancelTaskId == mCurTask.iTaskId) {
				JNIHelper.logw("cancelCurTask : mCurTask.iTaskId=" + mCurTask.iTaskId );
				return;
			}
			cancelTaskId = mCurTask.iTaskId; //TODO 保存当前cancel task id，存在风险问题，不再TTS线程
			if (getTtsTool() != null && getTtsTool().isBusy()) {
				getTtsTool().stop();
			} else {
				//需要remove掉,否则可能会出现没有cancel掉直接播放Voice的任务
				AppLogic.removeBackGroundCallback(mRunnableSpeakNextVoice);
				// 移除tts延时播报任务
				AppLogic.removeBackGroundCallback(mRunnableSpeakTextDelay);
				if (mAudioPlayer != null) {
					mAudioPlayer.stop();
					mAudioPlayer.release();
					mAudioPlayer = null;
				}
				if (mThemePlayer != null) {
					mThemePlayer.setOnCompletionListener(null);
					mThemePlayer.stop();
				}
				mSpeakEndCallback.onCancel();
			}
		}
	}

	public void errorCurTask() {
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				if (null != mCurTask) {
					JNIHelper.logd("speakText end errorCurTask : id=" + mCurTask.iTaskId);
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
		if (iTaskId == INVALID_TTS_TASK_ID) {
			LogUtil.logd("invalid task id");
			return;
		}
		TtsTask t = mCurTask;
		if (t == null)
			t = new TtsTask();
		JNIHelper.recordLogStack(1);
		JNIHelper.logd("cancelSpeak[" + iTaskId + "]: curTask=" + t.iTaskId + ",text=" + t.sText);
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
							AppLogic.runOnBackGround(new Runnable1<TtsTask>(mTtsTaskQueue.get(i)) {
								@Override
								public void run() {
									JNIHelper.logd("speakText end onCancel: id=" + mP1.iTaskId);
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
						AppLogic.runOnBackGround(new Runnable1<TtsTask>(t) {
							@Override
							public void run() {
								JNIHelper.logd("speakText end onCancel: id=" + mP1.iTaskId);
								mP1.oRun.onCancel();
								mP1.oRun.onEnd();
							}
						}, 0);
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
	
	public int getCurTaskId() {
		if (mCurTask != null) {
			return mCurTask.iTaskId;
		}
		return INVALID_TTS_TASK_ID;
	}

	// /////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SPEAK_WORDS);
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SPEAK_WORDS_CLOSE_RECORD);
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SPEAK_WORDS_NOT_CLOSE_RECORD);
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SPEAK_WORDS_WITHOUT_CANCEL);
		regEvent(UiEvent.EVENT_INNER_NET, UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_RESP);
		regEvent(UiEvent.EVENT_TTS, UiTts.SUBEVENT_TTSTHEME_SET_REQ);
		regEvent(UiEvent.EVENT_TTS, UiTts.SUBEVENT_TTSTHEME_DOWN_REQ);
		regEvent(UiEvent.EVENT_TTS, UiTts.SUBEVENT_TTSTHEME_QUERY_REQ);
		regEvent(UiEvent.EVENT_TTS, UiTts.SUBEVENT_TTSTHEME_DOWN_PATCH_REQ);
		regEvent(UiEvent.EVENT_TTS, UiTts.SUBEVENT_TTS_SPEAK_TEXT_REQ);
		regCommand("TTS_THEME_CHANGE");
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
					case VoiceData.SUBEVENT_VOICE_SPEAK_WORDS_NOT_CLOSE_RECORD:
						AsrManager.getInstance().setNeedCloseRecord(false);
						RecorderWin.speakTextWithClose(new String(data), null);
						break;
					case VoiceData.SUBEVENT_VOICE_SPEAK_WORDS_WITHOUT_CANCEL:
						speakText(new String(data), PreemptType.PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE);
						break;
				}
				break;
			case UiEvent.EVENT_INNER_NET:
				switch (subEventId) {
					case UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_RESP:
						try {
							UiInnerNet.DownloadHttpFileTask task = UiInnerNet.DownloadHttpFileTask.parseFrom(data);
							onDownLoad(task);
						} catch (Exception e) {
							JNIHelper.logd(e.toString());
						}
						break;
				}
				break;
			case UiEvent.EVENT_TTS:
				switch (subEventId) {
					case UiTts.SUBEVENT_TTSTHEME_SET_REQ:
						doTTSThemeSetReq(data);
						break;
					case UiTts.SUBEVENT_TTSTHEME_DOWN_REQ:
						doTTSThemeDownReq(data);
						break;
					case UiTts.SUBEVENT_TTSTHEME_QUERY_REQ:
						doTTSThemeQueryReq(data);
						break;
					case UiTts.SUBEVENT_TTSTHEME_DOWN_PATCH_REQ:
						doTTSThemeDownPatchReq(data);
						break;
					case UiTts.SUBEVENT_TTS_SPEAK_TEXT_REQ:
						doTTSSpeakTextReq(data);
						break;
				}
				break;
		}

		return super.onEvent(eventId, subEventId, data);
	}
	
	private void doTTSSpeakTextReq(byte[] data) {
		try {
			String text = DEFAULT_SPEAK_TEXT;
			PreemptType preemptType = DEFAULT_PREEMPT_FLAG;
			TtsText cmd = TtsText.parseFrom(data);
			if (cmd.strText != null) {
				text = cmd.strText;
			}
			if (cmd.ttstextopertype != null) {
				if (cmd.ttstextopertype == PushManager.PREEMPT_TYPE_NONE) {
					preemptType = PreemptType.PREEMPT_TYPE_NONE;
				} else if (cmd.ttstextopertype == PushManager.PREEMPT_TYPE_NEXT) {
					preemptType = PreemptType.PREEMPT_TYPE_NEXT;
				} else if (cmd.ttstextopertype == PushManager.PREEMPT_TYPE_IMMEADIATELY) {
					preemptType = PreemptType.PREEMPT_TYPE_IMMEADIATELY;
				}
			}
			JNIHelper.logd("speakText: From server: " + text);
			speakText(text, preemptType);
		} catch (Exception e) {
			e.printStackTrace();
			JNIHelper.loge("speakText: " + e.toString());
		}
	}

	/** 暂停TTS，暂时直接停掉当前任务 */
	public void pause() {
		JNIHelper.logd("pause: ");
		TtsTask t = mCurTask;
		if (t == null)
			t = new TtsTask();
		cancelSpeak(t.iTaskId);
	}

	/** 恢复TTS */
	public void resume() {
		JNIHelper.logd("resume: ");
		speakNext();
	}

	/*private String[] tts_map = { "没有匹配的结果", "RS_NONE_MATCH", "呼叫已取消", "RS_CALL_CANCEL", "抱歉，我不太理解您的意思",
			"RS_VOICE_UNKNOW_LOCAL", "没有听清楚，请再说一遍", "RS_VOICE_UNKNOW", "没有找到相关应用", "RS_VOICE_APP_NOT_FOUND",
			"抱歉，当前不支持该操作", "RS_VOICE_UNSUPPORT_OPERATE", "请问你要去哪里", "RS_VOICE_UNKNOW_NAVIGATE_TARGET", "没有找到目的地",
			"RS_VOICE_POI_NOT_FOUND", "网络不稳定，无法搜索歌曲", "RS_VOICE_MUSIC_NOT_FOUND", "当前没有任何收藏音乐",
			"RS_VOICE_MUSIC_NO_FAVOURITED", "抱歉，本地未找到音乐文件，请先下载", "RS_VOICE_NONE_MUSIC", "没有找到相关新闻",
			"RS_VOICE_NEWS_NOT_FOUND", "抱歉，暂时没有下载到新闻资源", "RS_VOICE_NONE_NEWS", "您可以说打电话给张三",
			"RS_VOICE_WHO_DO_YOU_WANT_TO_MAKE_CALL", "您要导航到什么地方", "RS_VOICE_WHERE_DO_YOU_WANT_TO_NAVIGATE",
			"没有找到相关联系人", "RS_VOICE_CONTACT_NOT_FOUND", "找到以下联系人，请选择", "RS_VOICE_MAKE_CALL_LIST", "更新联系人完成",
			"RS_VOICE_UPDATE_CONTACTS_COMPLETED", "创建别名完成", "RS_VOICE_CREATE_CONTACTS_ALIAS_COMPLETED", "导入服务号码列表完成",
			"RS_VOICE_REFRESH_SERVICE_CONTACTS_COMPLETED", "本地未找到，即将上网进行搜索", "RS_VOICE_SEARCH_MUSIC", "抱歉，未能找到结果",
			"RS_VOICE_NAV_NONE_FOUND", "导航结束", "RS_VOICE_NAV_END", "找到如下结果，请说第几个选择，或取消", "RS_VOICE_NAV_SELECT",
			"找到如下结果，您要选择第几个为公司的地址", "RS_VOICE_NAV_SET_COMPANY", "找到如下结果，您要选择第几个为家的地址", "RS_VOICE_NAV_SET_HOME",
			"找到一个结果，即将开始导航，确定还是取消", "RS_VOICE_NAV_CONFIRM", "已经连接上HDIT数据云中心，数据将实时更新!", "RS_HDIT",
			"语音功能已开启，唤醒词为你好小维，你好魔方", "RS_DXWY", "先走一步", "RS_VOICE_ASR_CHAT_END_HINT_3", "我在呢",
			"RS_VOICE_ASR_START_HINT_1", "乐意为您效劳", "RS_VOICE_ASR_START_HINT_4", "请在嘀的一声后开始说话", "RS_VOICE_FIRST", "臣妾在",
			"RS_VOICE_ASR_START_HINT_KING", "需要帮忙吗", "RS_VOICE_ASR_START_HINT_5", "有什么可以帮您",
			"RS_VOICE_ASR_START_HINT_6", "臣妾告退", "RS_VOICE_ASR_CHAT_END_HINT_KING", "哈喽", "RS_VOICE_ASR_START_HINT_3",
			"您好", "RS_VOICE_ASR_START_HINT_2", "下次见", "RS_VOICE_ASR_CHAT_END_HINT_1", "持续为您服务",
			"RS_VOICE_ASR_CHAT_END_HINT_2" };*/

	/*public int filterText(String text) {
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
	}*/

	public void setVoiceSpeed(int speed) {
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

	public void setTtsModel(String strModel) {
		if (mInitSuccessed && getTtsTool() != null) {
			getTtsTool().setTtsModel(strModel);
		}
	}

	/*
	 * 1、TTS主题音频包存放路径为/sdcard/txz/tts_role
	 * 2、主题音频包中放置一个txz文件,里面存放该压缩包的ID。该文件的作用既可以用来校验该压缩包是不是TTS主题音频包，又便于获取主题包的ID。
	 */
	private final static String DOWNLOAD_MODULE_ID = TtsManager.class.getSimpleName();

	public final static String TTS_ROLE_ROOT = FilePathConstants.TTS_THEME_PATH_PRIOR;

	private final static String TTS_THEME_PKT_SUFFIX = TtsTheme.TTS_THEME_PKT_SUFFIX;
	private final static String TTS_THEME_PKT_TXZ = TtsTheme.TTS_THEME_PKT_FILE_TXZ;// 主题包标志文件名
	private final static String TTS_THEME_PKT_ID = TtsTheme.TTS_THEME_PKT_KEY_ID;
	private final static String TTS_THEME_PKT_NAME = TtsTheme.TTS_THEME_PKT_KEY_NAME;
	private final static String TTS_THEME_PKT_VERSION = TtsTheme.TTS_THEME_PKT_KEY_VERSION;
	private final static String TTS_THEME_PKT_TYPE = TtsTheme.TTS_THEME_PKT_KEY_TYPE;
	private final static String TTS_THEME_PKT_ROLES = TtsTheme.TTS_THEME_PKT_KEY_ROLES;
	private final static String TTS_THEME_PKT_LANGUAGE = TtsTheme.TTS_THEME_PKT_KEY_ROLE_LANGUAGE;
	private final static String TTS_THEME_PKT_SEX = TtsTheme.TTS_THEME_PKT_KEY_ROLE_SEX;
	private final static String TTS_THEME_PKT_AGE = TtsTheme.TTS_THEME_PKT_KEY_ROLE_AGE;
	private final static String TTS_THEME_PKT_PRIORITY = TtsTheme.TTS_THEME_PKT_KEY_ROLE_PRIORITY;
	
	private final static String TTS_THEME_TEXT = TtsTheme.TTS_THEME_PKT_FILE_TEXT;
	private final static String TTS_THEME_PKT_AUDIO_SUFFIX = ".mp3";
	private final static String TTS_THEME_HEAD = "tts://";
	private final static String TTS_THEME_DEFAULT_THEME_NAME = TtsTheme.TTS_THEME_DEFAULT_THEME_NAME;// 默认主题主题名称
	private final static String TTS_THEME_DEFAULT_THEME_ROLE = TtsTheme.TTS_THEME_DEFAULT_THEME_ROLE;//默认主题角色
	private final static int TTS_THEME_DEFAULT_THEME_ID = TtsTheme.TTS_THEME_DEFAULT_THEME_ID; 
	public final static String TTS_THEME_MORE_THEME_NAME = "更多主题";// 默认主题主题名称
	public final static int TTS_THEME_INVALID_THEME_ID = -1;
	private final static int TTS_THEME_STATUS_USE = 6;
	private final static int TTS_THEME_STATUS_NO_USE = 7;

	/*
	 * TTS主题音频文件名加密方式：md5(appid + txzing.com+原始文件名)
	 * TTS主题音频文件内容加密方式：异或。加密Key:md5(appid+ACSDFLKlasdkfkjllasdf)
	 */
//	private final static String TTS_THEME_AUDIO_NAME_PUBLIC_KEY = "txzing.com";
	private final static String TTS_THEME_AUDIO_CONTENT_PUBLIC_KEY = "ACSDFLKlasdkfkjllasdf";

	private Map<String, Object> mDownReqQueue = new HashMap<String, Object>();
	private String mStrCurrTheme = null;
	private String mStrCurrRole = null;
	private int mCurrThemeId;
	private TtsTheme mCurrTheme = null;

	public void startLoadDownTask(String strTaskId, String url, String strDefineParam) {
		UiInnerNet.DownloadHttpFileTask task = new UiInnerNet.DownloadHttpFileTask();
		task.strTaskId = strTaskId;
		task.strUrl = url;
		task.strDefineParam = strDefineParam;
		task.bForbidUseReservedSpace = true;
		JNIHelper.sendEvent(UiEvent.EVENT_INNER_NET, UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_REQ, task);
	}

	public void stopLoadDownTask(String strTaskId, String url, String strDefineParam) {
		UiInnerNet.DownloadHttpFileTask task = new UiInnerNet.DownloadHttpFileTask();
		task.strTaskId = strTaskId;
		task.strUrl = url;
		task.strDefineParam = strDefineParam;
		JNIHelper.sendEvent(UiEvent.EVENT_INNER_NET, UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_STOP, task);
	}

	private void onDownLoad(UiInnerNet.DownloadHttpFileTask task) {
		if (DOWNLOAD_MODULE_ID.equals(task.strDefineParam)) {
			JNIHelper.logd("download : \n" + "taskId : " + task.strTaskId + "\n" + "curl : "
					+ subString(task.strUrl, 30) + "\n" + "resultCode : " + task.int32ResultCode);
			JNIHelper.logd("resultCode : " + task.int32ResultCode);
			Object object = mDownReqQueue.remove(task.strTaskId);
			if (object != null) {
				if (object instanceof PushCmd_NotifyTTSThemeDown) {
					onTTSThemeLoadDown(task, (PushCmd_NotifyTTSThemeDown) object);
				} else if (object instanceof TTSPatchInfo) {
					onTTSPatchLoadDown(task, (TTSPatchInfo) object);
				}
			}
		}
	}
	
	class TtsUpgradeRunnable implements Runnable{
		
		private static final int MAX_COUNT = 20;
		
		TTSPatchInfo mPatchInfo;
		int mCount;
		public TtsUpgradeRunnable(TTSPatchInfo patch, int count) {
			super();
			mPatchInfo = patch;
			mCount = count;
		}

		@Override
		public void run() {
			int mThemeid = mPatchInfo.uint32ThemeId;
			if (mCount > MAX_COUNT) {
				// 失败此时超过20次，上报失败
				Req_TTSPatchDownResult result = new Req_TTSPatchDownResult();
				result.uint32ThemeId = mThemeid;
				result.uint32SrcVersion = mPatchInfo.uint32SrcVersion;
				result.uint32DstVersion = mPatchInfo.uint32DstVersion;
				// （0：成功， 1：失败）
				result.uint32ErrCode = 1;
				JNIHelper.sendEvent(UiEvent.EVENT_TTS, UiTts.SUBEVENT_TTSTHEME_DOWN_PATCH_RESP, result);
				JNIHelper.logw("tts theme : upgrade tts fail times : " + mCount +  " / themeId " + mThemeid);
				MonitorUtil.monitorCumulant(MONITOR_TTS_UPGRADE_ERROR + mThemeid);
				return;
			}
			boolean bSuccessed = false;
			do {
				File file = new File(DownloadManager.DOWNLOAD_FILE_ROOT, mThemeid + TTS_THEME_PKT_SUFFIX);
				if (!file.exists()) {
					bSuccessed = false;
					JNIHelper.loge("tts theme : new tts file not exists : " + file.getPath());
					break;
				}
				File newFile = new File(TTS_ROLE_ROOT, mThemeid + TTS_THEME_PKT_SUFFIX);
				// 保证复制到的目标文件不存在
				newFile.delete();
				bSuccessed = file.renameTo(newFile);
				if (!bSuccessed) {
					JNIHelper.logw("tts theme : delete file failed : " + newFile.getPath());
					break;
				}
				
			} while (false);
			
			if (bSuccessed) {
				// 上报TTS主题升级成功的结果
				Req_TTSPatchDownResult result = new Req_TTSPatchDownResult();
				result.uint32ThemeId = mThemeid;
				result.uint32SrcVersion = mPatchInfo.uint32SrcVersion;
				result.uint32DstVersion = mPatchInfo.uint32DstVersion;
				// （0：成功， 1：失败）
				result.uint32ErrCode = 0;
				JNIHelper.sendEvent(UiEvent.EVENT_TTS, UiTts.SUBEVENT_TTSTHEME_DOWN_PATCH_RESP, result);
				JNIHelper.logw("tts theme : upgrade tts success : ThemeId = " + mThemeid);
				MonitorUtil.monitorCumulant(MONITOR_TTS_UPGRADE_SUCCESS + mThemeid);
			} else {
				// 复制文件失败需要等待一段时间处理 5分钟
				AppLogic.runOnSlowGround(new TtsUpgradeRunnable(mPatchInfo, mCount+1), 300000);
				JNIHelper.logw("tts theme : tts file rename failed : themeId " + mThemeid + " / time " + mCount);
			}
		}
		
	}

	/**
	 * 处理tts补丁包的下载结果
	 * @param task
	 * @param patch
	 */
	private void onTTSPatchLoadDown(DownloadHttpFileTask task, TTSPatchInfo patch) {
		if (patch.uint32ThemeId == null) {
			JNIHelper.logw("tts theme : patch theme id is null");
			return;
		}
		String strThemeName = (patch.strThemeName == null ? null : new String(patch.strThemeName));
		int uint32ThemeId = patch.uint32ThemeId;
		boolean bSuccessed = false;
		do {
			if (task.int32ResultCode != UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_SUCCESS) {
				JNIHelper.logw("tts theme : down tts patch error : " + uint32ThemeId + " / " + strThemeName);
				break;
			}
			
			File patchFile = new File(DownloadManager.DOWNLOAD_FILE_ROOT, task.strTaskId);
			
			String themePath = findThemePathById(uint32ThemeId);
			if (themePath == null) {
				JNIHelper.logw("tts theme : not found old tts : " + uint32ThemeId + " / " + strThemeName);
				break;
			}
			
			// 临时文件用来保存授权主题清除授权后的TTS主题包
			File cacheFile = new File(DownloadManager.DOWNLOAD_FILE_ROOT, ""+uint32ThemeId);
			// copy授权TTS主题包，并且移除授权信息
			bSuccessed = TtsAuthorizeUtil.removeAuthorization(new File(themePath), cacheFile, getAuthorization());
			if (!bSuccessed) {
				JNIHelper.logw("tts theme : copy old tts file fail : " + uint32ThemeId + " / " + strThemeName + " / " +themePath);
				break;
			}
			
			// 合并差分包，放置缓存目录
			File file = new File(DownloadManager.DOWNLOAD_FILE_ROOT, uint32ThemeId + TTS_THEME_PKT_SUFFIX);
			bSuccessed = NativeData.comboFileByNames(cacheFile.getPath(), patchFile.getPath(), file.getPath());
			if (!bSuccessed) {
				JNIHelper.logw("tts theme : bspatch fail : " + uint32ThemeId + " / " + strThemeName);
				break;
			}
			
			// 校验MD5值
			bSuccessed = MD5Util.generateMD5(file).equalsIgnoreCase(patch.strThemeMd5);
			if (!bSuccessed) {
				JNIHelper.logw("tts theme : new tts file bad MD5 : " + file.getPath() + " / " + patch.strThemeMd5);
				break;
			}
			
			// 为TTS主题包授权
			bSuccessed = TtsAuthorizeUtil.authorize(file, getAuthorization());
			if (!bSuccessed) {
				JNIHelper.logd("tts theme : Authorize fail : " + file.getPath());
				break;
			}
			
			// 清除缓存文件
			patchFile.delete();
			cacheFile.delete();
		} while (false);
		
		if (bSuccessed) {
			// 这里传参是当前主题的ID，和上面的 file 文件名是固定的
			AppLogic.runOnSlowGround(new TtsUpgradeRunnable(patch, 1), 0);
		} else {
			// 上报TTS主题升级失败的结果
			Req_TTSPatchDownResult result = new Req_TTSPatchDownResult();
			result.uint32ThemeId = uint32ThemeId;
			result.uint32SrcVersion = patch.uint32SrcVersion;
			result.uint32DstVersion = patch.uint32DstVersion;
			// （0：成功， 1：失败）
			result.uint32ErrCode = 1;
			JNIHelper.sendEvent(UiEvent.EVENT_TTS, UiTts.SUBEVENT_TTSTHEME_DOWN_PATCH_RESP, result);
			JNIHelper.logw("tts theme : upgrade tts fail : ThemeId = " + uint32ThemeId);
			MonitorUtil.monitorCumulant(MONITOR_TTS_UPGRADE_ERROR + uint32ThemeId);
		}
	}

	private void onTTSThemeLoadDown(UiInnerNet.DownloadHttpFileTask task, PushCmd_NotifyTTSThemeDown cmd) {
		String strThemeName = new String(cmd.strThemeName);
		int uint32ThemeId = cmd.uint32ThemeId;
		boolean bSuccessed = false;
		do {
			if (task.int32ResultCode == UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_SUCCESS) {
				File file = new File(DownloadManager.DOWNLOAD_FILE_ROOT, task.strTaskId);
				File root = new File(TTS_ROLE_ROOT);
				boolean bRet = false;
				if (!root.exists()) {
					bRet = root.mkdirs();
					if (!bRet) {
						JNIHelper.logw("mkdirs fail : " + root.getPath());
						break;
					}
				}
				File newFile = new File(TTS_ROLE_ROOT, uint32ThemeId + TTS_THEME_PKT_SUFFIX);
				bRet = file.renameTo(newFile);
				JNIHelper.logd("bRet : " + bRet);
				if (!bRet) {
					break;
				}
				bRet = TtsAuthorizeUtil.authorize(newFile, getAuthorization());
				JNIHelper.logd("Authorize bRet : " + bRet);
				if (!bRet) {
					break;
				}
				bSuccessed = bRet;
			}
		} while (false);

		Req_TTSThemeDownResult result = new Req_TTSThemeDownResult();
		result.uint32ThemeId = uint32ThemeId;
		result.uint32ErrCode = bSuccessed ? task.int32ResultCode : UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_IO;
		JNIHelper.sendEvent(UiEvent.EVENT_TTS, UiTts.SUBEVENT_TTSTHEME_DOWN_RESP, result);
		String strSpeakText = NativeData.getResPlaceholderString("RS_VOICE_TTS_THEME_DOWN_RESULT", "%THEME%", strThemeName);
		strSpeakText = strSpeakText.replace("%RESULT%", bSuccessed ? "成功" : "失败");
		TtsManager.getInstance().speakText(strSpeakText);
	}

	private String subString(String strContent, int most) {
		if (TextUtils.isEmpty(strContent)) {
			return strContent;
		} else {
			int len = most > strContent.length() ? strContent.length() : most;
			return strContent.substring(0, len);
		}
	}

	private void doTTSThemeSetReq(byte[] data) {
		try {
			PushCmd_NotifyTTSThemeSetUse cmd = PushCmd_NotifyTTSThemeSetUse.parseFrom(data);
			String themeName = null;
			if (null == cmd.strThemeName) {
				themeName = "";
			} else {
				themeName = new String(cmd.strThemeName);
			}
			JNIHelper.logd("themeName = " + themeName + ", themeId = " + cmd.uint32ThemeId + ", time = " + new Date(cmd.uint32Time));
			boolean bExisted = false;
			bExisted = existTheme(themeName, queryAllTheme());
			String strSpeakText = "";
			if (bExisted){
				setCurrTheme(cmd.strThemeName, cmd.uint32ThemeId, (long)cmd.uint32Time);
				strSpeakText = NativeData.getResPlaceholderString("RS_VOICE_TTS_THEME_SWITCH_DONE", "%THEME%", TextUtils.isEmpty(themeName) ?  "默认": themeName);
			}else{
				strSpeakText = NativeData.getResPlaceholderString("RS_VOICE_TTS_THEME_NOEXIST", "%THEME%", themeName);
			}
			TtsManager.getInstance().speakText(strSpeakText);
			Req_TTSThemeSetUseResult result = new Req_TTSThemeSetUseResult();
			result.uint32ThemeId = cmd.uint32ThemeId;
			result.uint32ErrCode = bExisted ? 0 : 1;// 0,设置成功，1主题包不存在
			JNIHelper.sendEvent(UiEvent.EVENT_TTS, UiTts.SUBEVENT_TTSTHEME_SET_RESP, result);
		} catch (Exception e) {
		}
	}

	private boolean setCurrTheme(final byte[] strTheme, final int themeId, final long configTime) {
		return changeTtsTheme(themeId, new String(strTheme), configTime, true);
	}

	private void doTTSThemeDownReq(byte[] data) {
		try {
			PushCmd_NotifyTTSThemeDown cmd = PushCmd_NotifyTTSThemeDown.parseFrom(data);
			JNIHelper.logd("themeName = " + new String(cmd.strThemeName) + ", themeId = " + cmd.uint32ThemeId + ", time = " + new Date(cmd.uint32Time));
			mDownReqQueue.put(cmd.strThemeMd5, cmd);
			startLoadDownTask(cmd.strThemeMd5, cmd.strThemeUrl, DOWNLOAD_MODULE_ID);
			MonitorUtil.monitorCumulant(MONITOR_TTS_DOWNLOAD + cmd.uint32ThemeId);
		} catch (Exception e) {

		}
	}
	
	private void doTTSThemeDownPatchReq(byte[] data) {
		JNIHelper.logd("tts theme : push tts upgrade info");
		try {
			PushCmd_NotifyTTSThemeDownPatch cmd = PushCmd_NotifyTTSThemeDownPatch.parseFrom(data);
			// 遍历所有的升级请求
			for (TTSPatchInfo patch : cmd.patches) {
				JNIHelper.logd("tts theme : down patch file"
						+ (patch.strThemeName == null ? null : ("Name = " + new String(patch.strThemeName)))
						+ ", themeId = " +  patch.uint32ThemeId
						+ ", oldVersion = " + patch.uint32SrcVersion
						+ ", newVersion = " + patch.uint32DstVersion
						+ ", URL = " + patch.strPatchUrl
						+ ", MD5 = " + patch.strThemeMd5);
				if (patch.strPatchUrl == null || patch.strThemeMd5 == null) {
					break;
				}
				mDownReqQueue.put(patch.strThemeMd5, patch);
				startLoadDownTask(patch.strThemeMd5, patch.strPatchUrl, DOWNLOAD_MODULE_ID);
				MonitorUtil.monitorCumulant(MONITOR_TTS_DOWNLOAD + patch.uint32ThemeId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			JNIHelper.logd("tts theme : upgrade tts error : " + e.toString());
		}
	}

	private void doTTSThemeQueryReq(byte[] data) {
		Req_TTSThemeInfoList pbThemeInfoList = new Req_TTSThemeInfoList();
		List<TTSTheme_Info> themeList = queryAllTheme();
		
		/*if (themeList == null) {
			themeList = new ArrayList<TTSTheme_Info>();
		}
		// 默认主题,一定存在
		TTSTheme_Info defaultInfo = new TTSTheme_Info();
		defaultInfo.strThemeName = TTS_THEME_DEFAULT_THEME_NAME.getBytes();
		defaultInfo.uint32ThemeId = TTS_THEME_DEFAULT_THEME_ID;
		defaultInfo.uint32State = TextUtils.isEmpty(getCurrentThemeName())
				? TTS_THEME_STATUS_USE
				: TTS_THEME_STATUS_NO_USE;

		// 当前主题对应的主题包不存在
		if (!existTheme(mStrCurrTheme, themeList)) {
			// 本地不存在TTS主题包时,需要将主题设置成默认主题
			setCurrTheme(TTS_THEME_DEFAULT_THEME_NAME.getBytes(), TTS_THEME_DEFAULT_THEME_ID,
					System.currentTimeMillis());
			defaultInfo.uint32State = TTS_THEME_STATUS_USE;
		}

		themeList.add(defaultInfo);*/
		pbThemeInfoList.uin32ReportType = EquipmentManager.TTS_UPLOAD_NOMORL; // 判断是否是升级的标志位，标准不升级
		pbThemeInfoList.rptTtsThemeInfo = (TTSTheme_Info[]) themeList.toArray(new TTSTheme_Info[themeList.size()]);
		JNIHelper.sendEvent(UiEvent.EVENT_TTS, UiTts.SUBEVENT_TTSTHEME_QUERY_RESP, pbThemeInfoList);
	}
	
	//当前是否使用的是默认主题
	public boolean isUsingDefaultTheme(){
		return TextUtils.isEmpty(mStrCurrTheme);
	}
	
	/**
	 * 获取本地下载的所有主题(包括默认主题)。并且当前主题对应的主题包不存在时，切换为默认主题
	 * @return
	 */
	private List<TTSTheme_Info> queryAllTheme() {
		// 当前正在使用的主题包
		int currentThemeId = mCurrThemeId;
		String currentThemeName = mStrCurrTheme;
		
		JNIHelper.logd("tts theme: query all themes");
		
		// 遍历所有的主题
		List<TTSTheme_Info> themeList = new ArrayList<TTSTheme_Info>();
		
		HashSet<Integer> themeSet = new HashSet<Integer>();
		for (String ttsThemePath : FilePathConstants.getUserTtsThemePathRoot()) {
			File file = new File(ttsThemePath);
			if (file.exists() && file.isDirectory() && file.canRead()) {
				String[] list = file.list();
				if (list == null) {
					// 不要有侥幸心理，不要相信系统可以自己处理
					continue;
				}
				for (String strName : list) {
					if (strName.endsWith(TTS_THEME_PKT_SUFFIX)) {
						File themeZip = new File(file, strName);
						
						// 规避一个坑爹的问题，文件名显示乱码，找不到这个文件
						if (!themeZip.exists()) {
							JNIHelper.logw("tts theme: " + themeZip.getPath() + " not exists or chinese messy code");
							continue;
						}

						// 检查授权信息,防止不同的项目之间互相拷贝使用
						if (!TtsAuthorizeUtil.checkAuthorization(themeZip, getAuthorization())) {
							JNIHelper.loge("tts theme: authorize fail: " + themeZip.getPath());
							continue;
						}

						String strJson = UnZipUtil.getInstance().UnZipToString(themeZip.getPath(), TTS_THEME_PKT_TXZ);
						if (strJson == null) {
							JNIHelper.logw("tts theme: zip file is error: " + themeZip.getPath());
							continue;
						}
						JSONObject jsonObj = null;
						int uint32ThemeId = 0;
						String strThemeName = null;
						int uint32ThemeVersion = 1;
						try {
							jsonObj = new JSONObject(strJson);
							uint32ThemeId = jsonObj.getInt(TTS_THEME_PKT_ID);
							strThemeName = jsonObj.getString(TTS_THEME_PKT_NAME);
							uint32ThemeVersion = jsonObj.optInt(TTS_THEME_PKT_VERSION, 1);
						} catch (JSONException e) {
							JNIHelper.loge("tts theme: " + TTS_THEME_PKT_TXZ + "file error " + themeZip.getPath());
							continue;
						}
						
						if (!strName.equals(uint32ThemeId + TTS_THEME_PKT_SUFFIX)) {
							JNIHelper.logw("tts theme: " + themeZip.getPath() + " not tts theme");
							continue;
						}
						
						if (!themeSet.add(uint32ThemeId)) {
							JNIHelper.logw("tts theme: more than one theme " +  themeZip.getPath());
							continue;
						}

						TTSTheme_Info info = new TTSTheme_Info();
						info.uint32ThemeId = uint32ThemeId;
						info.strThemeName = strThemeName.getBytes();
						info.uint32Version = uint32ThemeVersion;
						if (currentThemeId == uint32ThemeId && currentThemeName.equals(strThemeName)) {
							info.uint32State = TTS_THEME_STATUS_USE;
							currentThemeId = -1;
						} else {
							info.uint32State = TTS_THEME_STATUS_NO_USE;
						}
						themeList.add(info);
					}
				}
			} else {
				// JNIHelper.logw("tts theme: root directory " + file.getPath() + " not readable or directory or exist");
			}
		}
		
		// 添加默认的主题，放在列表的第一位
		TTSTheme_Info defaultTTSTheme = new TTSTheme_Info();
		defaultTTSTheme.strThemeName = TTS_THEME_DEFAULT_THEME_NAME.getBytes();
		defaultTTSTheme.uint32ThemeId = TTS_THEME_DEFAULT_THEME_ID;
		if (currentThemeId != -1) { 
			defaultTTSTheme.uint32State = TTS_THEME_STATUS_USE;
			if (currentThemeId != TTS_THEME_DEFAULT_THEME_ID) {
				// 当前主题对应的主题包不存在时，切换为默认主题
				setCurrTheme(TTS_THEME_DEFAULT_THEME_NAME.getBytes(), TTS_THEME_DEFAULT_THEME_ID, System.currentTimeMillis());
			}
		} else {
			defaultTTSTheme.uint32State = TTS_THEME_STATUS_NO_USE;
		}
		themeList.add(0, defaultTTSTheme);
		
		return themeList;
	}

	private void updateResJsonData(String strJson) {
		if (!TextUtils.isEmpty(strJson)) {
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_UPDATE_RESOURCE, strJson.getBytes());
		}
	}

	private void setResStyle(int themeId, String strStyle, String strRole) {
		String resStype = "";
		if (!TextUtils.isEmpty(strStyle) && !TextUtils.isEmpty(strRole)) {
			resStype = strStyle + "/" + strRole;
		}
		mCurrThemeId = themeId;
		mStrCurrTheme = strStyle;
		mStrCurrRole = strRole;
		mCurrTheme.setThemeId(themeId);
		mCurrTheme.setThemeName(strStyle);
		mCurrTheme.setThemeRole(strRole);
		
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SET_STYLE, resStype.getBytes());
	}

	/**
	 * 配置当前TTS主题
	 * 这里必须保证启动应用是初始化成功，不需要一与是配置信息中的主题对应
	 * @param oRun
	 */
	private void configTTSTheme(IInitCallback oRun) {
		String strThemeName = null;
		int nThemeId = 0;
		com.txz.ui.tts.UiTts.TTSConfig config = NativeData.getTTSConfig();
		if (config != null && config.msgCurrentThemeConfig != null && config.msgCurrentThemeConfig.msgTheme != null
				&& config.msgCurrentThemeConfig.msgTheme.strThemeName != null 
				&& config.msgCurrentThemeConfig.msgTheme.uint32ThemeId != null) {
			strThemeName = new String(config.msgCurrentThemeConfig.msgTheme.strThemeName);
			nThemeId = config.msgCurrentThemeConfig.msgTheme.uint32ThemeId;
		} else {
			// 第一次初始化时，查看有没有默认的信息
			try {
				ArrayList<String> mTtsRoleSysRoot = FilePathConstants.getUserTtsThemePath();
				for (String ttsPath : mTtsRoleSysRoot) {
					File file = new File(ttsPath, "default");
					if (file.canRead()) {
						Properties properties = new Properties();
						InputStreamReader input = new InputStreamReader(new FileInputStream(file));
						properties.load(input);
						input.close();
						String ThemeId = properties.getProperty("ThemeId");
						if (ThemeId != null && ThemeId.length() != 0) {
							nThemeId = Integer.parseInt(ThemeId);
						}
						strThemeName = properties.getProperty("ThemeName");
						JNIHelper.logd("tts theme : first active theme");
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		changeTtsTheme(oRun, nThemeId, strThemeName);
		return;
	}
   
   private String createRoleInCurrentTheme(String filePath){
	   String strRole = null;
		try {
			String strJson = UnZipUtil.getInstance().UnZipToString(filePath, TTS_THEME_PKT_TXZ);
			JSONObject json = new JSONObject(strJson);
			JSONArray jsonArray = json.getJSONArray(TTS_THEME_PKT_ROLES);
			if (jsonArray.length() > 0) {
				Random random = new Random();
				strRole = (String) jsonArray.get(random.nextInt(100) % jsonArray.length());
			}
		} catch (Exception e) {
		}
	   return strRole;
   }
   
	/**
	 * 主题包的根路径可以指定多个目录存放，每次查找主题时都要去遍历根目录，返回zip所在文件夹
	 * @return
	 */
	private String findThemePathById(int nThemeId) {
		// 多个目录下存在语音包
		for (String ttsThemePath : FilePathConstants.getUserTtsThemePathRoot()) {
			File file = new File(ttsThemePath);
			// 根目录是否存在、是目录、可读
			if (file.exists() && file.isDirectory() && file.canRead()) {
				File zipFile = new File(file, nThemeId + TTS_THEME_PKT_SUFFIX);
				if (zipFile.exists() && TtsAuthorizeUtil.checkAuthorization(zipFile, getAuthorization())) {
					return zipFile.getPath();
				}
			}
		}
		// 没有找到
		return null;
	}
	private String findParentPathById(int nThemeId) {
		// 多个目录下存在语音包
		for (String ttsThemePath : FilePathConstants.getUserTtsThemePathRoot()) {
			File file = new File(ttsThemePath);
			// 根目录是否存在、是目录、可读
			if (file.exists() && file.isDirectory() && file.canRead()) {
				File zipFile = new File(file, nThemeId + TTS_THEME_PKT_SUFFIX);
				if (zipFile.exists() && TtsAuthorizeUtil.checkAuthorization(zipFile, getAuthorization())) {
					return ttsThemePath;
				}
			}
		}
		// 没有找到
		return null;
	}
   
   //tts:///sdcard/txz/tts_roles/king.zip?role=strRole&text=strText&suffix=strSuffix
   private String getTextUrlFromZip(String strText, String strTheme, String strRole){
	   if (TextUtils.isEmpty(strText) || TextUtils.isEmpty(strTheme) || TextUtils.isEmpty(strRole)){
		   return null;
	   }
		// 多个目录下存在语音包
	   for (String ttsThemePath : FilePathConstants.getUserTtsThemePathRoot()) {
		   File zipFile = new File(ttsThemePath + File.separator + strTheme + TTS_THEME_PKT_SUFFIX);
		   if (zipFile.exists()) {
			   String strUnZipFilePath = zipFile.getPath();
			   String strUnEntryName = strRole + File.separator + strText + TTS_THEME_PKT_AUDIO_SUFFIX;
			   if (UnZipUtil.getInstance().HasEntry(strUnZipFilePath, strUnEntryName)) {
				   String strUrl = String.format("%s%s?role=%s&text=%s&suffix=%s", TTS_THEME_HEAD, strUnZipFilePath, strRole,
						   strText, TTS_THEME_PKT_AUDIO_SUFFIX);
				   JNIHelper.logd("url : " + strUrl);
				   return strUrl;
			   }
		   }
	   }
	   return null;
   }
   
   private String getAudioUrl(String strText, TtsTheme ttsTheme){
	   if (TextUtils.isEmpty(strText) || ttsTheme == null || TextUtils.isEmpty(ttsTheme.getFilePath())){
		   return null;
	   }
	   if (!ttsTheme.isEncrypt()) {
		   File file = new File(ttsTheme.getAudioPath(strText));
		   if (file.canRead()) {
			   String strUrl = String.format("%s%s?role=%s&text=%s&suffix=%s", TTS_THEME_HEAD, ttsTheme.getFilePath(), ttsTheme.getThemeRole(),
					   strText, ttsTheme.getThemeAudioSuffix());
			   JNIHelper.logd("url : " + strUrl);
			   return strUrl;
		   } else {
			return null;
		}
	   }
	   File zipFile = new File(ttsTheme.getFilePath());
	   if (zipFile.exists()) {
		   String strUnZipFilePath = zipFile.getPath();
		   String strUnEntryName = ttsTheme.getThemeRole() + File.separator + strText + ttsTheme.getThemeAudioSuffix();
		   if (UnZipUtil.getInstance().HasEntry(strUnZipFilePath, strUnEntryName)) {
			   String strUrl = String.format("%s%s?role=%s&text=%s&suffix=%s", TTS_THEME_HEAD, strUnZipFilePath, ttsTheme.getThemeRole(),
					   strText, ttsTheme.getThemeAudioSuffix());
			   JNIHelper.logd("url : " + strUrl);
			   return strUrl;
		   }
	   }
	   return null;
   }
   
   public static class TtsThemeUrl{
	   public String strZipPath = null;
	   public String strRole = null;
	   public String strText = null;
	   public String strSuffix = null;
   }
   
   private TtsThemeUrl parseTtsUrl(String url){
	   TtsThemeUrl oUrl = new TtsThemeUrl();
	   if (TextUtils.isEmpty(url)){
		   return oUrl;
	   }
	   url = url.trim();
	   if (TextUtils.isEmpty(url)){
		   return oUrl;
	   }
	   //tts:///sdcard/txz/tts_roles/king.zip?role=strRole&text=strText&suffix=strSuffix
		try {
			oUrl.strZipPath = url.substring(TTS_THEME_HEAD.length(), url.indexOf("?"));
			String[] strParams = url.substring(url.indexOf("?") + 1).split("&");
			for (String strParam : strParams) {
				if (strParam.startsWith("role=")) {
					oUrl.strRole = strParam.replace("role=", "");
				} else if (strParam.startsWith("text=")) {
					oUrl.strText = strParam.replace("text=", "");
				} else if (strParam.startsWith("suffix=")) {
					oUrl.strSuffix = strParam.replace("suffix=", "");
				}
			}

		} catch (Exception e) {

		}

		return oUrl;
	}
	
	public static boolean sShowTTSThemeQr = true;

	public void gotoMoreThemes() {
		if (!sShowTTSThemeQr) {
			String strText = NativeData.getResString("RS_VOICE_TTS_THEME_SELECT_LIST_NONE_SPK");
			RecorderWin.speakTextWithClose(strText, null);
		} else {
			if (WeixinManager.getInstance().hasBind()) {
				String text = NativeData.getResString("RS_VOICE_TTS_THEME_SELECT_HAS_BINDQR");
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(text, null);
				return;
			}

			flushQRCode(WeixinManager.getInstance().getBindQr());
		}
	}
	
	private void flushQRCode(String qrUrl) {
		JSONBuilder jb = new JSONBuilder();
		jb.put("title", NativeData.getResString("RS_DISPLAY_TTS_THEME_SELECT_LIST_NONE_TEXT"));
		jb.put("qrCode", qrUrl);
		JSONBuilder jBuilder = new JSONBuilder();
		jBuilder.put("key", AbsTextJsonParse.TYPE_TTS_NO_RESULT);
		jBuilder.put("value", jb.toString());
		jBuilder.put("type", 2);
		
		ChoiceManager.getInstance().showTtsQr(jBuilder.toString());
	}
	
	public void switchTTSTheme(TTSTheme_Info info) {
		setCurrTheme(info.strThemeName, info.uint32ThemeId, System.currentTimeMillis());
		doTTSThemeQueryReq(null);
	}

	// 需要考虑,当前主题被删除了的情况
	private void onChangeThemeBySelector() {
		List<TTSTheme_Info> themeList = queryAllTheme();
		
		// 移除当前正在使用的主题
		for (int i = 0; i < themeList.size(); i++) {
			if (themeList.get(i).uint32State == TTS_THEME_STATUS_USE) {
				themeList.remove(i);
			}
		}
		if (themeList == null || themeList.isEmpty()) {
			gotoMoreThemes();
		} else {
			// 更多主题
			if (sShowTTSThemeQr) {
				TTSTheme_Info invalidTTSTheme = new TTSTheme_Info();
				invalidTTSTheme.strThemeName = TTS_THEME_MORE_THEME_NAME.getBytes();
				invalidTTSTheme.uint32ThemeId = TTS_THEME_INVALID_THEME_ID;
				invalidTTSTheme.uint32State = TTS_THEME_STATUS_NO_USE;
				themeList.add(invalidTTSTheme);
			}
			
			ChoiceManager.getInstance().showTtsList(themeList);
		}
	}

	// 需要考虑,当前主题被删除了的情况
	private void onChangeTheme() {
		List<TTSTheme_Info> themeList = null;
		themeList = queryAllTheme();
		if (themeList == null || themeList.isEmpty()) {
			// 本地不存在TTS主题包时,需要将主题设置成默认主题
			setCurrTheme(TTS_THEME_DEFAULT_THEME_NAME.getBytes(), TTS_THEME_DEFAULT_THEME_ID, System.currentTimeMillis());
			String strSpeakText = NativeData.getResString("RS_VOICE_TTS_THEME_EMPTY");
			RecorderWin.speakTextWithClose(strSpeakText, null);
			return;
		}
		int k = -1;
		for (int i = 0; i < themeList.size(); i++){
			TTSTheme_Info info = themeList.get(i);
			if (info.uint32State == TTS_THEME_STATUS_USE){
				k = i;
				break;
			}
		}
		//k = -1 或者k = [0, themeList.size() - 1]
		if (k == -1 || k == themeList.size() - 1){
			setCurrTheme(TTS_THEME_DEFAULT_THEME_NAME.getBytes(), TTS_THEME_DEFAULT_THEME_ID, System.currentTimeMillis());
		}else{
			setCurrTheme(themeList.get(k+ 1).strThemeName, themeList.get(k + 1).uint32ThemeId, System.currentTimeMillis());
		}
		String strSpeakText = NativeData.getResPlaceholderString("RS_VOICE_TTS_THEME_SWITCH_DONE", "%THEME%", TextUtils.isEmpty(mStrCurrTheme) ? "默认" : mStrCurrTheme);
		RecorderWin.speakTextWithClose(strSpeakText, null);
		doTTSThemeQueryReq(null);
	}

	// 判断strTheme主题是否存在
	private boolean existTheme(String strTheme, List<TTSTheme_Info> themeList) {
		// 默认主题一定存在
		if (TextUtils.isEmpty(strTheme)) {
			return true;
		}

		boolean bRet = false;
		if (themeList == null || themeList.isEmpty()) {
			return bRet;
		}
		for (int i = 0; i < themeList.size(); i++) {
			TTSTheme_Info info = themeList.get(i);
			if (info != null && info.strThemeName != null) {
				if (strTheme.equals(new String(info.strThemeName))) {
					bRet = true;
				}
			}
		}
		return bRet;
	}

	@Override
	public int onCommand(String cmd) {
		if ("TTS_THEME_CHANGE".equals(cmd)) {
			// 本地文件配置禁用切换主题功能
			if (!TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_TTS_ENABLE_CHANGE_THEME, true)) {
				 RecorderWin.speakText(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"), null);
				 return super.onCommand(cmd);
			}
			boolean useSelector = WinManager.getInstance().hasThirdImpl();
			if (!useSelector || mForceShowChoiceView) {
				onChangeThemeBySelector();
			} else {
				onChangeTheme();
			}
		}
		return super.onCommand(cmd);
	}

	// 返回用于音频加密的MD5值
	private String getMD5() {
		return MD5Util.generateMD5(TTS_THEME_AUDIO_CONTENT_PUBLIC_KEY);
	}

	private byte[] getKey() {
		return getBytesFromMD5(getMD5(), SECRECT_KEY_LENGHT);
	}

	// 返回用于授权TTS主题的MD5值
	private String getAuthorizedMD5() {
		return MD5Util.generateMD5(LicenseManager.getInstance().getAppId() + TTS_THEME_AUDIO_CONTENT_PUBLIC_KEY);
	}

	private byte[] getAuthorization() {
		return getBytesFromMD5(getAuthorizedMD5(), SECRECT_KEY_LENGHT);
	}

	private final static int SECRECT_KEY_LENGHT = 16;

	private byte[] getBytesFromMD5(String strMD5, int len) {
		byte[] data = null;
		do {
			if (strMD5 == null) {
				break;
			}
			if (len < 0) {
				break;
			}
			if (len * 2 > strMD5.length()) {
				len = strMD5.length() / 2;
			}
			data = new byte[len];

			for (int i = 0; i < data.length; i++) {
				int j = 2 * i;
				String strHex = strMD5.substring(j, j + 2);
				int value = Integer.parseInt(strHex, 16);
				data[i] = (byte) (value & 0x00ff);
			}
		} while (false);
		return data;
	}

	private void decryptAudio(byte[] data, int offset, int size, long offsetInFile, byte[] key) {
		if (data == null) {
			return;
		}
		if (key == null) {
			return;
		}
		if (key.length == 0) {
			return;
		}

		int k = (int) (offsetInFile % key.length);// k >=0 and k < key.lenght
		int i = offset;
		for (; i < size;) {
			data[i] = (byte) (data[i] ^ key[k]);
			// 计算下一次使用key中那个偏移量
			k++;
			if (k == key.length) {
				k = 0;
			}
			i++;
		}
	}
	
	private boolean isLogin;
	
	/** TXZ登录成功回调 */
	public void loginSuccess() {
		isLogin = true;
		checkTtsThemeUpgrade();
	}
	
	/** 校验TTS主题，是否需要升级 */
	private void checkTtsThemeUpgrade() {
		if (!mInitSuccessed || !isLogin) {
			// login成功，且tts引擎初始化成功；
			JNIHelper.logw("tts theme : tts not init or not login");
			return;
		}
		AppLogic.runOnSlowGround(new Runnable() {
			@Override
			public void run() {
				JNIHelper.logd("tts theme : check tts theme upgrade");
				Req_TTSThemeInfoList pbThemeInfoList = new Req_TTSThemeInfoList();
				List<TTSTheme_Info> themeList = queryAllTheme();
				JNIHelper.logd("tts theme : report tts size : " + themeList.size());
				if (themeList != null && themeList.size() > 1) {
					// 本地有下载主题包，才会上报主题包信息
					pbThemeInfoList.uin32ReportType = EquipmentManager.TTS_UPLOAD_PATCH; // 判断是否是升级的标志位，升级
					pbThemeInfoList.rptTtsThemeInfo = (TTSTheme_Info[]) themeList.toArray(new TTSTheme_Info[themeList.size()]);
					JNIHelper.sendEvent(UiEvent.EVENT_TTS, UiTts.SUBEVENT_TTSTHEME_QUERY_RESP, pbThemeInfoList);
					for (TTSTheme_Info ttsTheme_Info : themeList) {
						JNIHelper.logd("tts theme : current theme info : ID:" + ttsTheme_Info.uint32ThemeId + " Name:" + new String(ttsTheme_Info.strThemeName) + " Version:" + ttsTheme_Info.uint32Version);
					}
				}
			}
		}, 0);
	}
	
	/**
	 * 设置我们内部默认的tts延时
	 */
	public void setTtsDelay(long delay){
		ttsDelay = delay;
	}

	public void setBufferTime(int nTime) {
		JNIHelper.logd("set buffer time : " + nTime);
		if (mInitSuccessed && getTtsTool() != null) {
			ITts.TTSOption oOption = new ITts.TTSOption();
			oOption.mPlayStartBufferTime = nTime;
			getTtsTool().setOption(oOption);
		}
	}
	
	/** 设置beep资源路径 */
	public void setBeepResources(String beepPath) {
		BeepPlayer.setBeepResources(beepPath);
	}
	
	public void setReplaceSpeakWord(String replaceJson) {
		LogUtil.logd("tts: replace word: " + replaceJson);
		JSONArray jsonArray = null;
		try {
			jsonArray = new JSONArray(replaceJson);
		} catch (JSONException e) {
			e.printStackTrace();
			JNIHelper.loge("tts: replace word: " + e.getMessage());
			return;
		}
		if (null == jsonArray || jsonArray.length() == 0) {
			return;
		}
		mOriginals = new String[jsonArray.length()];
		mReplaces = new String[jsonArray.length()];
		for (int i = 0; i < jsonArray.length(); i++) {
			String original = null;
			String replace = null;
			try {
				JSONObject object = jsonArray.getJSONObject(i);
				original = object.getString("original");
				replace = object.getString("replace");
			} catch (JSONException e) {
				e.printStackTrace();
				continue;
			}
			// 替换的原始文本不允许为空，替换文本允许是空串
			if (TextUtils.isEmpty(original) || null == replace) {
				continue;
			}
			mOriginals[i] = original;
			mReplaces[i] = replace;
		}
	}
	
	private boolean mEnableDownVolume = true;
	/** 设置导航播报语音时TTS是否降低音量 */
	public void enableDownVolumeWhenNav(boolean enable) {
		JNIHelper.logd("enable lower the volume when navigation: " + enable);
		mEnableDownVolume = enable;
	}
	
	private boolean mForceShowChoiceView = TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_TTS_FORCE_SHOW_CHOICE_VIEW, false);
	/** 强制开始外放 TTS主题选择列表 */
	public void forceShowChoiceView(boolean enable) {
		JNIHelper.logd("TTS: forceShowChoiceView=" + enable);
		mForceShowChoiceView = enable;
	}

    private boolean changeTtsTheme(final int themeId, final String themeName, final long configTime, final boolean needSave) {
        boolean bRet;
        String oldTheme = mStrCurrTheme;
        bRet = changeTtsTheme(new IInitCallback() {
            @Override
            public void onInit(boolean bSuccess) {
                if (bSuccess) {
                    // 初始化成功
					if(packageName != null && !"".equals(packageName)){
						JSONBuilder jsonBuilder = new JSONBuilder();
						if(themeId == TTS_THEME_DEFAULT_THEME_ID){
							jsonBuilder.put("themeName","默认主题");
						}else{
							jsonBuilder.put("themeName",themeName);
						}
						jsonBuilder.put("themeId",themeId);
						ServiceManager.getInstance().sendInvokeSync(packageName,"theme.tts.change",jsonBuilder.toBytes());
					}
                    if (needSave) {
                        saveTtsTheme(themeName.getBytes(), themeId, configTime);
                    }
                } else {
                    // 初始化失败
                    // 是否需要切换为默认主题，如果当前不是默认主题，可能导致切换逻辑不流畅
                    MonitorUtil.monitorCumulant(MONITOR_TTS_CHANGE_ERROR + themeId);
                }
			}
        }, themeId, themeName);
        // 上报数据
        ReportUtil.doReport(new ReportUtil.Report.Builder().setType("tts").setAction("change")
                .putExtra("from", oldTheme).setSessionId().putExtra("to", mStrCurrTheme)
                .buildCommReport());
        MonitorUtil.monitorCumulant(MONITOR_TTS_CHANGE + mCurrThemeId);
        return bRet;
    }

    private boolean changeTtsTheme(IInitCallback oRun, int nThemeId, String strThemeName) {
		JNIHelper.logd("tts theme : " + strThemeName + " / "+ nThemeId);
		if (nThemeId == mCurrThemeId && !TextUtils.isEmpty(strThemeName) && strThemeName.equals(mStrCurrTheme)) {
			// 当前使用的主题与设置的主题相同，直接返回。这里的主题是已经配置过的
			JNIHelper.logw("tts theme : Select theme is the same as the theme being used, so do nothing");
			return true;
		}

		mCurrTheme = new TtsTheme();
		mCurrTheme.setThemeName(strThemeName);
		mCurrTheme.setThemeId(nThemeId);

		if (TTS_THEME_DEFAULT_THEME_NAME.equals(strThemeName)) {
			JNIHelper.logw("tts theme : change default theme");
			setResStyle(TTS_THEME_DEFAULT_THEME_ID, TTS_THEME_DEFAULT_THEME_NAME, TTS_THEME_DEFAULT_THEME_ROLE);
			mTts.initialize(oRun);
			return true;
		}

		boolean result = false;

		while(true) {
			// 使用默认主题、默认角色 ： 没有设置主题名
			if (nThemeId <= 1 || TextUtils.isEmpty(strThemeName)) {
				JNIHelper.logw("tts theme : no current theme, change default theme");
				break;
			}
			String parentPath = findParentPathById(nThemeId);
			if (parentPath == null) { // 没有找到当前主题包，使用默认主题
				JNIHelper.logw("tts theme : " + strThemeName + " / "+ nThemeId + " not find theme package");
				break;
			}
			String themePath = parentPath + nThemeId + TTS_THEME_PKT_SUFFIX;
			// 主题包存在且已授权
			String strJson = UnZipUtil.getInstance().UnZipToString(themePath, TTS_THEME_PKT_TXZ);
			if (strJson == null) { // 主题包中标志性文件不存在，或异常
				JNIHelper.logw("tts theme : " + strThemeName + " / "+ nThemeId + " txz file error");
				break;
			}
			JSONObject json;
			int themeType;
			try {
				json = new JSONObject(strJson);
				themeType = json.optInt(TTS_THEME_PKT_TYPE);

				String audioSuffix = json.optString(TtsTheme.TTS_THEME_PKT_KEY_SUFFIX, TtsTheme.TTS_THEME_SUFFIX_MP3);
				mCurrTheme.setThemeAudioSuffix(audioSuffix);

				int magic = json.optInt(TtsTheme.TTS_THEME_PKT_KEY_MAGIC);
				if (magic == MD5Util.generateMD5(nThemeId + "").charAt(themeType)) {
					mCurrTheme.setEncrypt(false);
				}
			} catch (JSONException e) {
				JNIHelper.logw("tts theme : Json data parsing error");
				break;
			}
			mCurrTheme.setFilePath(themePath);
			mCurrTheme.setDirPath(parentPath);
			mCurrTheme.setThemeType(themeType);
			if (themeType == TtsTheme.TTS_THEME_TYPE_ENGINE) {
				// 主题包是引擎包
				setResStyle(nThemeId, strThemeName, TTS_THEME_DEFAULT_THEME_ROLE);
				// 非默认主题,需要随机生成一个角色
				String strThemeRole = createRoleInCurrentTheme(themePath);
				// 读取录音包中的特性信息
				JSONObject jsonObj = json.optJSONObject(strThemeRole);
				if (jsonObj != null) {
					// 不为空时，走适配逻辑，没有角色特性，使用默认引擎
					String language = jsonObj.optString(TTS_THEME_PKT_LANGUAGE);
					if(!jsonObj.isNull(TTS_THEME_PKT_SEX)){
						int sex = jsonObj.optInt(TTS_THEME_PKT_SEX);
					}

				}
				TtsEngineManager.getInstance().initializeTtsEngine(themePath, oRun);
				result = true;
				break;
			} else if (themeType == TtsTheme.TTS_THEME_TYPE_AUDIO ) {
				String strTextJson = UnZipUtil.getInstance().UnZipToString(themePath, TTS_THEME_TEXT);
				if (!TextUtils.isEmpty(strTextJson)) {
					updateResJsonData(strTextJson);
					// 非默认主题,需要随机生成一个角色
					String strThemeRole = createRoleInCurrentTheme(themePath);
					if (strThemeRole != null) {
						setResStyle(nThemeId, strThemeName, strThemeRole);
						// 读取录音包中的特性信息
						JSONObject jsonObj = json.optJSONObject(strThemeRole);
						if (jsonObj != null) {
							// 不为空时，走适配逻辑，没有角色特性，使用默认引擎
							String language = jsonObj.optString(TTS_THEME_PKT_LANGUAGE);
							int sex = jsonObj.optInt(TTS_THEME_PKT_SEX);
							int age = jsonObj.optInt(TTS_THEME_PKT_AGE);
							int priority = jsonObj.optInt(TTS_THEME_PKT_PRIORITY);
							TtsEngineManager.getInstance().initializeTtsEngine(language, sex, age, priority, oRun);
							result = true;
							break;
						} else {
							JNIHelper.logw("tts theme : Roles feature information not found : " + strThemeRole);
						}
					} else {
						JNIHelper.logw("tts theme : role of currTheme is null" + themePath);
					}
				}
				// 遍历所有的根目录，没有查找到配置使用的ID，使用默认的主题(为什么不使用setCurrTheme()，初始化调用时，会带callback)
			} else {
				// TtsTheme.TTS_THEME_TYPE_MIX:
				// 混合类型，预留
				// TtsTheme.TTS_THEME_TYPE_NONE:
				// 未知类型，或没有配置这个属性
			}
		}
		if (!result) {
			setResStyle(TTS_THEME_DEFAULT_THEME_ID, TTS_THEME_DEFAULT_THEME_NAME, TTS_THEME_DEFAULT_THEME_ROLE);
			mTts.initialize(oRun);
		}
        return result;
	}

    private boolean saveTtsTheme(byte[] strTheme, final int themeId, long configTime) {
        boolean result;
        UiTts.TTSConfig config = NativeData.getTTSConfig();
        if (config == null) {
            config = new UiTts.TTSConfig();
        }
        if (config.msgCurrentThemeConfig == null) {
            config.msgCurrentThemeConfig = new UiTts.TTSThemeConfig();
        }
        if (config.msgCurrentThemeConfig.msgTheme == null) {
            config.msgCurrentThemeConfig.msgTheme = new UiTts.TTSTheme();
        }
        config.msgCurrentThemeConfig.msgTheme.strThemeName = strTheme;
        config.msgCurrentThemeConfig.msgTheme.uint32ThemeId = themeId;
        config.msgCurrentThemeConfig.uint64ConfigTime = configTime;
        result = NativeData.setTTSConfig(config);
        JNIHelper.logd("tts theme : save theme result: " + result);
        return result;
    }

    public TXZTtsManager.TtsTheme[] getTtsThemes() {
		List<TTSTheme_Info> themeInfos = queryAllTheme();
		int len = themeInfos.size();
		TXZTtsManager.TtsTheme[] ttsThemes = new TXZTtsManager.TtsTheme[len];
		for (int i = 0; i < len; i++) {
			TTSTheme_Info ttsTheme_Info = themeInfos.get(i);
			TXZTtsManager.TtsTheme ttsTheme = new TXZTtsManager.TtsTheme();
			ttsTheme.mThemeId = ttsTheme_Info.uint32ThemeId;
			ttsTheme.mThemeName = new String(ttsTheme_Info.strThemeName);
			if (ttsTheme.mThemeId == TTS_THEME_DEFAULT_THEME_ID) {
				ttsTheme.mThemeName = "默认主题";
			}
			if (ttsTheme_Info.uint32State == TTS_THEME_STATUS_USE) {
				ttsTheme.isUsed = true;
			} else {
				ttsTheme.isUsed = false;
			}
			ttsThemes[i] = ttsTheme;
		}
		return ttsThemes;
	}

	public boolean setTtsTheme(int themeId, String themeName) {
		if (themeName == null) {
			return false;
		}
		if (themeId == TTS_THEME_DEFAULT_THEME_ID) {
			themeName = TTS_THEME_DEFAULT_THEME_NAME;
		}

		final String finalThemeName = themeName;
		final int finalThemeId = themeId;

		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				changeTtsTheme(finalThemeId, finalThemeName, System.currentTimeMillis(),true);
			}
		});
		return true;
	}
	
}

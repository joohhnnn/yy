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
import org.json.JSONException;
import org.json.JSONObject;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.data.UiData;
import com.txz.ui.data.UiData.AppConfig;
import com.txz.ui.data.UiData.TTime;
import com.txz.ui.event.UiEvent;
import com.txz.ui.record.UiRecord;
import com.txz.ui.record.UiRecord.TriggerKw;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.WakeupAsrKeywords;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.IWakeupAsrCallback;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.record.ui.WinRecord;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.wakeup.ISenceWakeup;
import com.txznet.txz.component.wakeup.ISenceWakeup.ISenceWakeupCallback;
import com.txznet.txz.component.wakeup.ISenceWakeup.SenceWakeupOption;
import com.txznet.txz.component.wakeup.IWakeup;
import com.txznet.txz.component.wakeup.IWakeup.IInitCallback;
import com.txznet.txz.component.wakeup.IWakeup.IWakeupCallback;
import com.txznet.txz.component.wakeup.IWakeup.WakeupKw;
import com.txznet.txz.component.wakeup.IWakeup.WakeupKwType;
import com.txznet.txz.component.wakeup.IWakeup.WakeupOption;
import com.txznet.txz.component.wakeup.txz.WakeupTxzImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.advertising.AdvertisingManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.feedback.FeedbackManager;
import com.txznet.txz.module.fm.FmManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.launch.LaunchManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.userconf.UserConf;
import com.txznet.txz.module.version.LicenseManager;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.service.TXZService;
import com.txznet.txz.ui.win.help.WinHelpManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.STATE;
import com.txznet.txz.util.BeepPlayer;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.ThreshHoldAdapter;
import com.txznet.txz.util.recordcenter.ITXZSourceRecorder;
import com.txznet.txz.util.recordcenter.RecorderCenter;
import com.txznet.txz.util.runnables.Runnable1;

import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;

public class WakeupManager extends IModule {
	private static final String INSTANT_ASR_PLACEHOLDER_SIGN = "%";
	private static final String INSTANT_ASR_PLACEHOLDER_NICK = "%name%";
	private static final String DEVICE_NICK_REPLACE_REGX = "你好|您好|召见|嗨";
	
	static WakeupManager sModuleInstance = new WakeupManager();

	IWakeup mWakeup = null;
	ISenceWakeup mProWakeup = null;//避免误唤醒的唤醒引擎

	public boolean mBindStyleWithWakeup = false;

	// String[] mWakeupKeywords = null;
	public boolean mEnableWakeup = true;
	public boolean mEnableChangeWakeupKeywords = true;
	public boolean mEnableCoverDefaultKeywords = false;//默认声控取的名字不要覆盖SDK设置的唤醒词 2017/10/24 AndyZhao
	public String[] mWakeupKeywords_Sdk = null; // SDK设置的唤醒词
	public String[] mWakeupKeywords_User = null; // 用户设置的唤醒词
	public String[] mWakeupKeywords_Tag = null; // 下发的命中关键词的唤醒词
	public Set<String> mWakeupOneShotKws = new HashSet<String>();//金手指获取当前可用的OneShot指令
	Map<String, Runnable> mWakeupKeywords_Global = new HashMap<String, Runnable>(); // 全局唤醒词，如“返回桌面”
	
	// 免唤醒导航相关变量
	private static final long WAKEUP_START_TIME_OFFSET = -200;
	private boolean bEnableInstantAsr = true; // 是否开启免唤醒，来自sdk配置
	private long mLastBeginSpeechTime; // 上次开始说话时间
	private long mLastWakeupStartTime; // 上次唤醒开始说话时间
	private String[] mRawInstantAsrKeywords; // 原始免唤醒命令词， 可能包含占位符
	private String[] mInstantAsrKeywords; // 实际的免唤醒命令词
	private String[] mRearInstantAsrKws; // 后置的免唤醒词

	private String[] mInterruptKws = {"停止播报"};
	private String mJSZInterruptKw = "打开金手指";
	public int mInterruptTipsCount = 0;
	
	
	//存放禁用关键词和禁用时间
	private Map<String, Long> proCmds = new HashMap<String, Long>();
	//存放含有阈值的唤醒词
	private Map<String, Float> thresholdKeyWords = new HashMap<String, Float>();
	
	/**
	 * 是否包含不让添加免唤醒指令的唤醒词
	 * 	例如：微信导航过去命令字注册时会与导航到xxx冲突
	 */
	private boolean hasNoInstantWkCmd(){
		boolean hasNoAsrCmd = false;
		synchronized (mWakeupKeywords_Asr) {
			for (WakeupAsrTask task : mWakeupKeywords_Asr) {
				if (task.callback != null && task.callback.getPriority() == AsrUtil.WKASR_PRIORITY_NO_INSTANT_WK
						&& task.callback.needAsrState() == true) {
					hasNoAsrCmd = true;
					break;
				}
			}
		}
		return hasNoAsrCmd;
	}
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
		mWakeupKeywords_Sdk = new String[] { };//避免通过sdk在非初始化时设置唤醒词后, 再次重启进程, 又触发一次唤醒词/oneshot离线识别槽位的重新编译。new String[] { "你好小踢" }; andyzhao
		String kws = PreferenceUtil.getInstance().getString(
				PreferenceUtil.KEY_USER_WAKEUP_KEYWORDS, null);
		try {
			JSONBuilder json = new JSONBuilder(kws);
			mWakeupKeywords_User = json.getVal("kws", String[].class);
		} catch (Exception e) {
		}
		updateOneShotKeyWords();
		updateWakeupKeywords2Asr(mWakeupKeywords_Sdk);
		updateWakeupKeywords2Asr(mWakeupKeywords_User);
		
		//V3引擎需要单独调整一下容易误唤醒的唤醒词的阈值
		//放到构造方法中,避免覆盖通过sdk设置的阈值
		setWakeupKeywordsThreshold(ThreshHoldAdapter.genKwsThreshValue());
		
		++updateKeywordsCur;
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
			//插入关键词唤醒词
			addKeywords(mSetWakeupKeywords, mWakeupKeywords_Tag);
			//响铃或者正在微信录音中，不插sdk和用户设置的唤醒词
			if (CallManager.getInstance().isRinging() == false && RecordManager.getInstance().isBusy() == false && FeedbackManager.getInstance().isBusy() == false) {
				addKeywords(mSetWakeupKeywords, mWakeupKeywords_Sdk);
				if (mEnableChangeWakeupKeywords){
					//用户关了唤醒,则取得名字也不能唤醒
					Boolean bUserEnable = UserConf.getInstance().getUserConfigData().mWakeupEnable;
					boolean bRet = true;
					if (bUserEnable != null && bUserEnable == false){
						bRet = false;
					}
					if (bRet){
						addKeywords(mSetWakeupKeywords, mWakeupKeywords_User);
					}
				}
				for (String kw : mWakeupKeywords_Global.keySet()) {
					if (TextUtils.isEmpty(kw))
						continue;
					mSetWakeupKeywords.add(kw.replace("同行者", "同形者"));
				}
			}
			synchronized (mWakeupKeywords_Asr) {
				boolean hasNoAsrCmd = hasNoInstantWkCmd();
				for (WakeupAsrTask task : mWakeupKeywords_Asr) {
					//来电任务比微信录音优先级高
					//响铃中，不插其他任务的唤醒词
					if (CallManager.getInstance().isRinging() && !CallManager.WAKEUP_INCOMING_TASK_ID.equals(task.callback.getTaskId())){
						continue;
					}
					//微信录音中，不插非响铃和微信录音任务的唤醒词
					if (RecordManager.getInstance().isBusy() 
							&& !RecordManager.RECORD_TASK_ID.equals(task.callback.getTaskId())
							&& !CallManager.WAKEUP_INCOMING_TASK_ID.equals(task.callback.getTaskId())){
						continue;
					}
					if (FeedbackManager.getInstance().isBusy()
							&& !FeedbackManager.RECORD_TASK_ID.equals(task.callback.getTaskId())
							&& !CallManager.WAKEUP_INCOMING_TASK_ID.equals(task.callback.getTaskId())
					        && !RecordManager.RECORD_TASK_ID.equals(task.callback.getTaskId())){
						continue;
					}
					//与唤醒识别命令有冲突或者不想要添加唤醒识别指令时
					if (hasNoAsrCmd && task.callback.getPriority()!=AsrUtil.WKASR_PRIORITY_NO_ASR
							&& !RecordManager.RECORD_TASK_ID.equals(task.callback.getTaskId())
							&& !CallManager.WAKEUP_INCOMING_TASK_ID.equals(task.callback.getTaskId())){
						continue;
					}
					addKeywords(mSetWakeupKeywords, task.callback.genKeywords());
				}
			}
			return mSetWakeupKeywords.toArray(new String[mSetWakeupKeywords
					.size()]);
		}
	}
	
	private String[] mOneShotKwsRaw = null;
	private String[] mOneShotKws = null; // 合并了设备昵称的最终oneshot唤醒词列表
	public void setOneShotKeyWords(String[] keywords){
		if (keywords == null || keywords.length == 0){
			JNIHelper.loge("empty keywords : " + keywords);
			return;
		}
		synchronized (WakeupKw.class) {
			if (checkSameArray(mOneShotKwsRaw, keywords)) {
				JNIHelper.logd("oneshot kws is sample");
				return;
			}
			
			mOneShotKwsRaw = keywords;
			updateOneShotKeyWords();
		}
	}
	
	/**
	 * 设置下发的关键词的唤醒词
	 * @param keywords
	 */
	public void setTagKeyWords(String[] keywords){
		if(keywords != null && keywords.length != 0){
			mWakeupKeywords_Tag = keywords;
			++updateKeywordsCur;
		}
	}
	
	private void updateOneShotKeyWords() {
		String[] arrDeviceNicks = getDeviceNicks();
		int len = (null == mOneShotKwsRaw) ? arrDeviceNicks.length : mOneShotKwsRaw.length + arrDeviceNicks.length;
		String[] arrOneshotKws = new String[len];
		
		// 合并设备昵称列表和设置的oneshot唤醒词列表
		System.arraycopy(arrDeviceNicks, 0, arrOneshotKws, 0, arrDeviceNicks.length);
		
		if(null != mOneShotKwsRaw) {
			System.arraycopy(mOneShotKwsRaw, 0, arrOneshotKws, arrDeviceNicks.length, mOneShotKwsRaw.length);
		}
		
		mOneShotKws = arrOneshotKws;
		JNIHelper.logd("instantAsr::updateOneShotKeyWords : " + Arrays.toString(mOneShotKws));
		
		WakeupAsrKeywords pbWakeupAsrKeywords = new WakeupAsrKeywords();
		pbWakeupAsrKeywords.rptStrKws = arrOneshotKws;
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SET_ONESHOT_KEYWORDS, pbWakeupAsrKeywords);
	}
	
	public String[] getAllOneShotKws(){
		return mOneShotKws;
	}
	
	private String mCurrDirectAsrKw = null;
	public String getCurrDirectAsrKw(){
		return mCurrDirectAsrKw;
	}
	
	public WakeupKw getWakeupKw(String strKw){
		JNIHelper.logd("instantAsr::getWakeupKw");
		WakeupKw info = new WakeupKw();
		if (TextUtils.isEmpty(strKw)){
			return info;
		}
		do {
			synchronized (WakeupKw.class) {
				/*if (mOneShotKws == null || mOneShotKws.length == 0) {
					break;
				}*/
				
				if(null != mOneShotKws) {
					for (String str : mOneShotKws) {
						JNIHelper.logd("instantAsr:getWakeupKw: comparing " + strKw + " to " + str);
						if (strKw.equals(str)) {
							info.mKwType = WakeupKwType.KW_TYPE_ONESHOT_ONLY;
							info.mOneShotKw = str;
							info.mDirectAsrKw = null;
							return info;
						} else if (strKw.startsWith(str)) {
							info.mKwType = WakeupKwType.KW_TYPE_ONESHOT_DIRECTASR;
							info.mOneShotKw = str;
							info.mDirectAsrKw = strKw.substring(str.length());
							return info;
						}
					}
				}
				
				if(null != mRearInstantAsrKws) {
					for (String str : mRearInstantAsrKws) {
						if(strKw.equals(str)) {
							info.mKwType = WakeupKwType.KW_TYPE_DIRECTASR_REAR;
							info.mOneShotKw = null;
							info.mDirectAsrKw = strKw;
							
							return info;
						}
					}
				}
				
				if (null != mInstantAsrKeywords) {
					for (String str : mInstantAsrKeywords) {
						if(strKw.equals(str)) {
							info.mKwType = WakeupKwType.KW_TYPE_DIRECTASR_FRONT;
							info.mOneShotKw = null;
							info.mDirectAsrKw = strKw;
							return info;
						}
					}
				}
				
			}
		} while (false);
		info.mKwType = WakeupKwType.KW_TYPE_DEFAULT;
		info.mOneShotKw = null;
		info.mDirectAsrKw = strKw;
		JNIHelper.logd("instantAsr::getWakeupKw: " + info.mOneShotKw + ", " + info.mDirectAsrKw);
		return info;
	}
	
	/**
	 * 获取当前所有的唤醒词
	 * @return
	 */
	public String[] getActiveKeyWords() {
		return genWakeupKeywords();
	}
	
	public String[] keyWords = null;
	private int updateKeywordsGen = 0;
	private int updateKeywordsCur = 0;
	private boolean updateComponentKeywords() {
		if (mWakeup == null)
			return false;
		
		int cur = updateKeywordsCur;
		if (cur == updateKeywordsGen) {
			LogUtil.logd("no change wakeup keywords");
			String[] kws = keyWords;
			return kws != null && kws.length > 0;
		}
		
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
			keyWords = kws;
			//updateKeywordsGen = cur;
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
//		updateProtectKeywords(kws);
		keyWords = genWakeupKeywordsWithInstantAsr(kws);
		TtsManager.getInstance().checkProWakeup(keyWords);
		
		updateKeywordsGen = cur;
		
		return true;
	}
	

	/**
	 * 构造用于唤醒的命令词列表，根据免唤醒命令词设置状态判断是否进行添加
	 * @param rawKeywords
	 * @return
	 */
	private String[] genWakeupKeywordsWithInstantAsr(String[] rawKeywords){
		synchronized (mWakeupKeywords_Asr) {
			String[] rawKws = rawKeywords;
			for (WakeupAsrTask task : mWakeupKeywords_Asr) {
				if (task.callback.getPriority() == AsrUtil.WKASR_PRIORITY_WK_NO_ASR) {
					String[] kws = task.callback.genKeywords();
					if (kws == null || kws.length == 0) {
						continue;
					}

					String[] ns = new String[rawKws.length + kws.length];
					System.arraycopy(kws, 0, ns, 0, kws.length);
					System.arraycopy(rawKws, 0, ns, kws.length, rawKws.length);

					rawKws = ns;
				}
			}
			rawKeywords = rawKws;
		}
		
		// 若sdk已配置关闭免唤醒，不进行插词
		if (!bEnableInstantAsr) {
			JNIHelper.logi("instantAsr::genWakeupKeywordsWithInstantAsr keywords [" + Arrays.toString(rawKeywords) + "]");
			return rawKeywords;
		}
		
		// 电话中或微信录音时，不进行插词
		if (CallManager.getInstance().isRinging() || RecordManager.getInstance().isBusy() || hasNoInstantWkCmd() || FeedbackManager.getInstance().isBusy()){
			JNIHelper.logi("instantAsr::genWakeupKeywordsWithInstantAsr keywords [" + Arrays.toString(rawKeywords) + "]");
			return rawKeywords;
		}
		
		//ArrayList<String> retList = (ArrayList<String>) Arrays.asList(rawKeywords);
		ArrayList<String> retList = new ArrayList<String>();
		retList.addAll(Arrays.asList(rawKeywords));

		mWakeupOneShotKws.clear();
		if(null != mRawInstantAsrKeywords && 0 != mRawInstantAsrKeywords.length) {
			mInstantAsrKeywords = getInstantWakeupKwsWithNick();
			for(String str : mInstantAsrKeywords) {
				retList.add(str);
				mWakeupOneShotKws.add(str);
			}
		}
		
		if(null != mRearInstantAsrKws && 0 != mRearInstantAsrKws.length) {
			for(String str : mRearInstantAsrKws) {
				retList.add(str);
				mWakeupOneShotKws.add(str);
			}
		}
		
		String[] ret = new String[retList.size()];
		retList.toArray(ret);
		
		JNIHelper.logi("instantAsr::genWakeupKeywordsWithInstantAsr keywords [" + Arrays.toString(ret) + "]");
		
		return ret;
	}
	
	private String[] getInstantWakeupKwsWithNick() {
		String[] deviceNickList = getDeviceNicks();
		List<String> resultList = new ArrayList<String>();

		List<String> nickHolderList = new ArrayList<String>();
		for (String kws : mRawInstantAsrKeywords) {
			// 只要命令字包含转义符就不进行添加，规避格式不支持的转义符
			if (kws.contains(INSTANT_ASR_PLACEHOLDER_SIGN)) {
				// 添加格式支持的转义符
				if(kws.contains(INSTANT_ASR_PLACEHOLDER_NICK)) {
					nickHolderList.add(kws);
				}
				
			} else {
				resultList.add(kws);
			}
		}
		
		if (0 != deviceNickList.length && !nickHolderList.isEmpty()) {
			// 处理占位符
			for (String ph : nickHolderList) {
				for (String nick : deviceNickList) {
					resultList.add(ph.replace(INSTANT_ASR_PLACEHOLDER_NICK,
							nick));
				}
			}
		}

		String[] ret = new String[resultList.size()];
		resultList.toArray(ret);
		
		JNIHelper.logd("instantAsr::getInstantWakeupKwsWithNick, kws = " + Arrays.toString(ret));
		
		return ret;
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
					LicenseManager.getInstance().initAsrComponent();
				}

				TXZService.checkSdkInitResult();
			}
		});
		//初始化前置唤醒引擎
		JNIHelper.logd("Protect Wakeup init type = "+ProjectCfg.getProtectWakeupType());
		if(ProjectCfg.getProtectWakeupType() != 0){
			initProWakeup();
		}
		
		mInterruptTipsCount = PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_INTERRUPT_TIPS_COUNT, 0);
			
	}

	Runnable mStartRunnable = new Runnable() {
		@Override
		public void run() {
			WakeupManager.this.start();
		}
	};

	int mStartDelay = 1000;
	public static final float DEFAULT_WAKEUP_THRESHHLOD = -3.1f;
	private float mWakeupThreshold = DEFAULT_WAKEUP_THRESHHLOD;// 正常唤醒门限
	private float mAsrWakeupThreshold = -3.1f;// 识别唤醒门限。云知声V3版本灵敏度太高，因此默认阈值调整为-3.1f。
	private float mWakeupMinThreshold = -3.5f;//最低门限值

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
		LogUtil.logd("setAsrWakeupThreshhold :" + threshHold);
		mAsrWakeupThreshold = threshHold;
	}

	private long lastWakeupVolumeTime = 0;
	private long mWakeupBeginTime = 0;//唤醒的开始时间
	
	private Runnable mRunnableRestartWakeup = new Runnable() {
		@Override
		public void run() {
			/******检测移到子进程中。因为，去掉了音量回调, 减少AIDL调用次数。*****/
			/******AndyZhao 2017/9/11 for 意图YunOS********************************/
			/*
			//使用了固定唤醒词的,不检测。因为固定唤醒词的唤醒APP没有回传音量
			if (mWakeup != null && mWakeup instanceof WakeupTxzImpl){
				return;
			}
			if (mEnableVoiceChannel == false) {
				AppLogic.removeBackGroundCallback(mRunnableRestartWakeup);
				AppLogic.runOnBackGround(mRunnableRestartWakeup, 3 * 1000);
				return;
			}
			// 1s内有音量回调，则不重启唤醒
			if (lastWakeupVolumeTime + 1000 > SystemClock.elapsedRealtime()) {
				AppLogic.removeBackGroundCallback(mRunnableRestartWakeup);
				AppLogic.runOnBackGround(mRunnableRestartWakeup, 3 * 1000);
				return;
			}
			JNIHelper.logw("begin restart wakeup");
			AppLogic.removeBackGroundCallback(mStartRunnable);
			stop();
			start();
			*/
		}
	};

	private Runnable mRunnableRestartWakeupNow = new Runnable() {
		
		@Override
		public void run() {
			if (mEnableVoiceChannel == false) {
				AppLogic.removeBackGroundCallback(mRunnableRestartWakeupNow);
				AppLogic.runOnBackGround(mRunnableRestartWakeupNow, 3 * 1000);
				return;
			}
			JNIHelper.logw("begin restart wakeup now");
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
	
	
	
	/**
	 * 返回上次唤醒开始说话的时间点
	 * @return
	 */
	public long getLastWakeupStartTime(){
		return mLastWakeupStartTime;
	}

	private boolean startInner() {
		JNIHelper.logd("Wakeup start....");
		AppLogic.removeBackGroundCallback(mStartRunnable);
		if (!updateComponentKeywords()) {
			JNIHelper.logw("empty wakeup keywords");
			return false;
		}
		mWakeup.setWakeupKeywords(keyWords);
		if (isInitSuccessed() == false
				|| mEnableWakeup == false
				|| AsrManager.getInstance().forbiddenWakeup()
				/*ANDYZHAO 2016/06/22
				 * 1、播报TTS的时候已经掐断录音，所以启动唤醒没有问题。
				 * 2、有些地方启动唤醒识别的时机不对，即TTS播报非闲状态下，启动的话，会导致TTS停止播报后没法立马唤醒
				 * || (TtsManager.getInstance().isBusy() && (ProjectCfg.mEnableAEC == false || ProjectCfg
						.needStopWkWhenTts()))*/
				|| (CallManager.getInstance().isIdle() == false && CallManager
						.getInstance().isRinging() == false)
				|| TXZPowerControl.hasReleased()	
				|| ProjectCfg.isEnableRecording() == false
				|| TXZPowerControl.isEnterReverse()) {
			String busyReason = String
					.format("wakeup is busy, waiting for idle: initSuccessd=%b; enable=%b; AsrBusy=%b; tts=%b; record=%b; call=%b, released=%b, isEnterReverse=%b",
							isInitSuccessed(), mEnableWakeup, AsrManager
									.getInstance().isBusy(), TtsManager
									.getInstance().isBusy(), RecordManager
									.getInstance().isBusy(), CallManager
									.getInstance().isIdle(),
									TXZPowerControl.hasReleased(),
									TXZPowerControl.isEnterReverse()
							);
			JNIHelper.logw(busyReason);
			// 5秒后重试唤醒
			AppLogic.runOnBackGround(mStartRunnable, mStartDelay);
			if (mStartDelay < 5000) {
				mStartDelay += 1000;
			}
			return false;
		}

		mIsBusy = true;
		//优先使用用户设置的唤醒词的阈值
		float wakeupThreshold = mWakeupThreshold;
		final Float userWakeupThreshVal = UserConf.getInstance().getUserConfigData().mWakeupThreshholdVal;
		if( userWakeupThreshVal != null){
			wakeupThreshold = userWakeupThreshVal;
			JNIHelper.logd("use user conf value : " + wakeupThreshold);
		}
		
		if(mAsrWakeupThreshold < mWakeupMinThreshold){
			mWakeupMinThreshold = mAsrWakeupThreshold;
		}
		if(wakeupThreshold < mWakeupMinThreshold){
			mWakeupMinThreshold = wakeupThreshold;
		}
		JNIHelper.logd("wakeupThreshold = " + wakeupThreshold +" ,mWakeupThreshhold =  " + mWakeupThreshold + ", mAsrWakeupThreshhold = " + mAsrWakeupThreshold);
		JNIHelper.logd("mWakeupMinThreshold = "+mWakeupMinThreshold);
		checkUsingAsr(new Runnable() {
			@Override
			public void run() {
				mWakeup.setWakeupThreshold(mWakeupMinThreshold);
				if (mEnableVoiceChannel) {
					MusicManager.getInstance().onBeginAsr();
					if (RecorderWin.isSelecting()) {
						RecorderWin.setState(STATE.STATE_WAKEUP_RECORD);
					}
				}
			}
		}, new Runnable() {
			@Override
			public void run() {
			mWakeup.setWakeupThreshold(mWakeupMinThreshold);
		}
		});
		
		mStartDelay = 1000;
		
		WakeupOption oOption = new WakeupOption();
		oOption.mBeginSpeechTime = mWakeupBeginTime;
		mWakeupBeginTime = 0;
		JNIHelper.logd("start wakeup oOption.mBeginSpeechTime = "+oOption.mBeginSpeechTime);
		oOption.wakeupCallback = new IWakeupCallback() {
			@Override
			public void onWakeUp(final String text, final float score) {
				long delay = 0;
				if(ProjectCfg.getProtectWakeupType() == 2 && TtsManager.getInstance().needProDelay){//同步保护，需要延迟300毫秒处理唤醒
					delay = 300;
				}
				final long aTime = SystemClock.elapsedRealtime();
				JNIHelper.logd("mWakeup onWakeUp text:"+text+" ,score:"+score+" ,attackTime:"+aTime+" ,preCmds:"+proCmds.toString());
				AppLogic.runOnBackGround(new Runnable() {
					@Override
					public void run() {
						onWakeup(text, aTime, score);
					}
				}, delay);
			}

			@Override
			public void onVolume(int vol) {
				if (WinRecord.getInstance().isShowing()) {
					RecorderWin.notifyVolumeChanged(vol);
				}
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
			public void onError(int errCode) {
				if (errCode != IWakeup.ERROR_CODE_OK){
					JNIHelper.logw("checkWakeup begin restart wakeup : " + errCode);
					AppLogic.removeBackGroundCallback(mStartRunnable);
					stop();
					start();
				}
			}
			
			@Override
			public void onSpeechBegin() {
				// 保存开始说话时间
				mLastBeginSpeechTime = SystemClock.elapsedRealtime();
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
		};
		mWakeup.start(oOption);
		RecorderCenter.setEnableCacheAEC(false);
		// 设置定时重启，防止老化出现不可唤醒
		AppLogic.removeBackGroundCallback(mRunnableRestartWakeup);
		AppLogic.runOnBackGround(mRunnableRestartWakeup, 6 * 1000);

		return true;
	}
	
	/**
	 * 唤醒
	 * @param text
	 * @param aTime 
	 */
	private void onWakeup(String text, long attackTime, float score) {
		if(proCmds.containsKey(text)){//在处理唤醒词之前先判断是不是在禁用列表
			long endTime = proCmds.get(text);
			JNIHelper.logd("onWakeUp handle text:"+text+" ,attackTime:"+attackTime+" ,endTime:"+endTime);
			JNIHelper.logd("onWakeUp endTime-attackTime="+(endTime-attackTime));
			
			if(endTime == WAKEUP_PRO_END_TIME){//禁用时间为空时则一直禁到tts播放结束
				return;
			}
			if(attackTime < endTime){
				return;
			}
		}
		
		JNIHelper.logd("onWakeup text = "+text+" ,threshold = "+thresholdKeyWords.get(text)+" ,score = "+score);
		if(thresholdKeyWords.containsKey(text) && thresholdKeyWords.get(text) != null){//唤醒词的阈值大于返回的分数，不进行处理
			if(thresholdKeyWords.get(text) > score){
				return;
			}
		} else {
			// 判断返回的结果是否高于阈值，低于阈值的不处理
			if (mUsingAsrInnerCount > 0) {
				//V3引擎两个字的唤醒词需要设置更高的阈值
				//特殊的两个字的唤醒词(比如确定,取消,接听,挂断,上翻,下翻等)需要单独添加到阈值Map中,防止影响到这些词的唤醒率
				// if (text.length() < 3 && score < ThreshHoldAdapter.SHORT_WORD_THRESH){
				// 	JNIHelper.logd("too short word and low score, SHORT_WORD_THRESH = " + ThreshHoldAdapter.SHORT_WORD_THRESH);
				// 	return;
				// }
				
				if (mAsrWakeupThreshold > score) {
					JNIHelper.logd("mAsrWakeupThreshold: "
							+ mAsrWakeupThreshold + " > score: " + score);
					return;
				}
			} else {
				float ruleScore = mWakeupThreshold;
				//优先使用用户设置的唤醒词的阈值
				final Float userWakeupThreshVal = UserConf.getInstance().getUserConfigData().mWakeupThreshholdVal;
				if( userWakeupThreshVal != null){
					ruleScore = userWakeupThreshVal;
					JNIHelper.logd("use user conf value : " + userWakeupThreshVal);
				}
				if (ruleScore > score) {
					JNIHelper.logd("mRuleScore: " + ruleScore
							+ " > score: " + score);
					return;
				}
			}
		}
		
		TTime tTime = NativeData.getMilleServerTime();
		long wakeupId = tTime.uint64Time;
		LogUtil.logd("save wakeup pcm data enable=" + ProjectCfg.enableSaveRawPCM());
		
		// onBeginSpeech回调会有延迟
		// 唤醒开始说话时间  = 上次开始说话时间 + 偏移量
		long now = SystemClock.elapsedRealtime();
		if (mLastBeginSpeechTime >= now - 2000 && mLastBeginSpeechTime < now){
			mLastWakeupStartTime = mLastBeginSpeechTime + WAKEUP_START_TIME_OFFSET;
		}else{
			//缓存录音长度超过5秒
		    mLastWakeupStartTime = now - 2000 + WAKEUP_START_TIME_OFFSET;
		}
		//mLastWakeupStartTime = mLastBeginSpeechTime + WAKEUP_START_TIME_OFFSET;
		JNIHelper.logd("onWakeUp wakeup result: " + text+" ,time: "+SystemClock.elapsedRealtime());


		
		
		//开始回调
		String style = null;
		if (mBindStyleWithWakeup) {
			for (int i = 0; i < mWakeupKeywords_Sdk.length; ++i) {
				if (text.equals(mWakeupKeywords_Sdk[i])) {
					style = "";
				}
			}
		}
		HelpGuideManager.getInstance().recordTime();
		AdvertisingManager.getInstance().clearOpenAdvertising();
		WinHelpManager.getInstance().closeQRCodeDialog();
		// 1、唤醒识别任务：有唤醒识别，优先处理，只处理最后注册的任务的
		List<WakeupAsrTask> tmpTaskList = new ArrayList<WakeupAsrTask>();
		synchronized (mWakeupKeywords_Asr) {
			tmpTaskList.addAll(mWakeupKeywords_Asr);
		}

		for (int i = tmpTaskList.size(); i > 0; --i) {
			WakeupAsrTask task = tmpTaskList.get(i - 1);
			checkUsingAsr(null, new Runnable() {
				@Override
				public void run() {
					RecorderWin.setState(STATE.STATE_END);
				}
			});
			task.callback.setIsWakeupResult(true);
			if (task.callback.onAsrResult(text)) {
				AsrManager.getInstance().cancel();
				JNIHelper.logd(MusicManager.TAG+"finish wakeup command="+text+",by id="+task.callback.getTaskId());
				reportWakeup(text, wakeupId);
				notifyOnWakeup(text, score, TXZConfigManager.WAKEUP_NOTIFY_FLAG_TASK);
				return;
			}
			if ("同形者".equals(text)) {
				if(task.callback.onAsrResult("同行者")){
					AsrManager.getInstance().cancel();
					reportWakeup("同行者", wakeupId);
					notifyOnWakeup(text, score, TXZConfigManager.WAKEUP_NOTIFY_FLAG_TASK);
					return;
				}
			}
		}
		
		//2、主唤醒词（原先放在1的位置：优先回调唤醒词，提升唤醒响应速度，但有优先级问题）
		if (isWakeupKWS(text)) {
			AsrManager.getInstance().cancel();
			JNIHelper.logd("Wakeup doLaunch: " + text + ", style=" + style);
			LaunchManager.getInstance().launchWithWakeup(style, text, wakeupId);
			notifyOnWakeup(text, score, TXZConfigManager.WAKEUP_NOTIFY_FLAG_LAUNCH);
			return;
		}

		// 3.处理全局唤醒指令 //废弃
		if (mWakeupKeywords_Global != null) {
			for (Entry<String, Runnable> entry : mWakeupKeywords_Global
					.entrySet()) {
				if (entry.getKey().equals(text)) {
					entry.getValue().run();
				}
			}
		}

		// 4.再处理远程唤醒场景
		JSONBuilder json = new JSONBuilder();
		json.put("keywords", text);
		json.put("action", "wakeup");
		json.put("scene", "wakeup");
		if (SenceManager.getInstance().noneedProcSence("wakeup",
				json.toBytes())) {
			AsrManager.getInstance().cancel();
			reportWakeup(text, wakeupId);
			notifyOnWakeup(text, score, TXZConfigManager.WAKEUP_NOTIFY_FLAG_SCREEN);
			return;
		}
		
		// 5.OneShot: 根据唤醒词类别判断是否需要进入免唤醒识别
		if(isInstantAsrKeyword(text)){
			AsrManager.getInstance().cancel();
			JNIHelper.logd("Wakeup doLaunch with instance asr: " + text);
			mCurrDirectAsrKw = text;
			LaunchManager.getInstance().launchWithInstantAsr(style, text, wakeupId);
			notifyOnWakeup(text, score, TXZConfigManager.WAKEUP_NOTIFY_FLAG_ONESHOT);
			return;
		} 
		
		// 6.后台下发的关键字记录
		if(isTagWakeup(text)){
			AsrManager.getInstance().cancel();
			JNIHelper.logd("Wakeup is tag wakeup: " + text);
			ReportUtil.doVoiceReport(new ReportUtil.Report.Builder().setKeywords(text)
			        .setRecordType(UiRecord.RECORD_TYPE_TRIGGER_KW).setTaskID(wakeupId+"")
			        .buildVoiceReport(), UiRecord.RECORD_TYPE_TRIGGER_KW, wakeupId);
			WakeupPcmHelper.savePcm(text, UiRecord.RECORD_TYPE_TRIGGER_KW, wakeupId+"");
			return;
		} 
		
		LogUtil.logw("trigger unknow wakeup keywords: " + text);
		// 需要往引擎更新唤醒词
		stopInner();
		startInner();
	}
	private void reportWakeup(String text, long wakeupId) {
		ReportUtil.doVoiceReport(new ReportUtil.Report.Builder().setKeywords(text).setAction("wakeup").setSessionId()
                .setRecordType(UiRecord.RECORD_TYPE_WAKEUP_CMD).setTaskID(wakeupId+"").buildWakeupReport(),
                UiRecord.RECORD_TYPE_WAKEUP_CMD, wakeupId);
		WakeupPcmHelper.savePcm(text, UiRecord.RECORD_TYPE_WAKEUP_CMD, wakeupId+"");
	}




	/**
	 * 设置需要发送到外部的唤醒词类型标志位
	 * @param notifyOnWakeupFlags
	 */
	public void setNotifyOnWakeupFlags(int notifyOnWakeupFlags) {
		mNotifyOnWakeupFlags = notifyOnWakeupFlags;
	}

	private int mNotifyOnWakeupFlags = 0;

	/**
	 * 通知外部已经唤醒
	 * @param kw
	 * @param score
	 * @param type
	 */
	private void notifyOnWakeup(String kw, float score, int type) {
		if ((mNotifyOnWakeupFlags & type) != 0) {
			LogUtil.logd("notifyOnWakeup kw : " + kw + " ; score : " + score);
			Intent intent = new Intent();
			intent.setAction("com.txznet.txz.onWakeup");
			intent.putExtra("keyword", kw);
			intent.putExtra("score", score);
			intent.putExtra("type", type);
			GlobalContext.get().sendBroadcast(intent);
		}
	}
	
	/**
	 * 判断是否属于唤醒词
	 * @return
	 */
	private boolean isWakeupKWS(String keyWords) {
		if(mWakeupKeywords_Sdk != null){
			for (String s : mWakeupKeywords_Sdk) {
				if(TextUtils.equals(s, keyWords)){
					return true;
				}
				//判断“同形者”，插词时将同行者插为了“同形者”
				if(keyWords.contains("同形者")){
					String strKey = keyWords.replace("同形者", "同行者");
					if(TextUtils.equals(s, strKey)){
						return true;
					}
				}
			}
		}
		Boolean bUserEnable = UserConf.getInstance().getUserConfigData().mWakeupEnable;//该值默认为null,且不可与布尔类型直接比较
		boolean bRet = true;
		if (bUserEnable != null && bUserEnable == false){
			bRet = false;
		}
		if(mWakeupKeywords_User != null && mEnableChangeWakeupKeywords && bRet){
			for (String s : mWakeupKeywords_User) {
				if(TextUtils.equals(s, keyWords)){
					return true;
				}
				//判断“同形者”，插词时将同行者插为了“同形者”
				if(keyWords.contains("同形者")){
					String strKey = keyWords.replace("同形者", "同行者");
					if(TextUtils.equals(s, strKey)){
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 判断唤醒词是否属于关键词唤醒
	 * @param keyWords
	 * @return
	 */
	private boolean isTagWakeup(String keyWords) {
		if(mWakeupKeywords_Tag == null){
			return false;
		}
		for (String kw : mWakeupKeywords_Tag) {
			if(TextUtils.equals(kw, keyWords)){
				return true;
			}
		}
		return false;
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
		mWakeup.setWakeupKeywords(keyWords);
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
		regEvent(UiEvent.EVENT_VOICE, UiRecord.SUBEVENT_TRIGGER_KEYWORDS);

		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterStartJni() {
		// 发送初始化需要触发的事件
		return super.initialize_AfterStartJni();
	}

	private void onAsrWakeup(String text) {
		ChoiceManager.getInstance().clearProgress();
		ChoiceManager.getInstance().clearIsSelecting();
		JSONBuilder json = new JSONBuilder();
		json.put("keywords", text);
		json.put("action", "asr");
		json.put("scene", "wakeup");
		if (SenceManager.getInstance()
				.noneedProcSence("wakeup", json.toBytes())) {
			notifyOnWakeup(text, 100, TXZConfigManager.WAKEUP_NOTIFY_FLAG_ASR_SCREEN);
			return;
		}
		AsrManager.getInstance().mAsrFromWakeup = true;
		notifyOnWakeup(text, 100, TXZConfigManager.WAKEUP_NOTIFY_FLAG_ASR_LAUNCH);
		RecorderWin.open();
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		// 处理事件
		switch (eventId) {
		case UiEvent.EVENT_VOICE: {
			switch (subEventId) {
			case VoiceData.SUBEVENT_VOICE_WAKEUP_KEYWORDS: {
				HelpGuideManager.getInstance().recordTime();
				String text = new String(data);
				JNIHelper.logd("recive asr wakeup keywords: " + text);
				// 有唤醒识别，优先处理，只处理最后注册的任务的
				List<WakeupAsrTask> tmpTaskList = new ArrayList<WakeupAsrTask>();
				synchronized (mWakeupKeywords_Asr) {
					tmpTaskList.addAll(mWakeupKeywords_Asr);
				}
				for (int i = tmpTaskList.size(); i > 0; --i) {
					WakeupAsrTask task = tmpTaskList.get(i - 1);
					checkUsingAsr(null, new Runnable() {

						@Override
						public void run() {
							RecorderWin.setState(STATE.STATE_END);
						}
					});
					task.callback.setIsWakeupResult(false);
					if (task.callback.onAsrResult(text)) {
						LogUtil.logd("task onAsrResult:" + task.callback.getTaskId());
						notifyOnWakeup(text, 100, TXZConfigManager.WAKEUP_NOTIFY_FLAG_ASR_TASK);
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
			case UiRecord.SUBEVENT_TRIGGER_KEYWORDS:{//下发关键词
				if(data == null){
					break;
				}
				try {
					TriggerKw mKw = TriggerKw.parseFrom(data);
					if(mKw == null || mKw.bytesTriggerKws == null){
						break;
					}
					String mKwString = new String(mKw.bytesTriggerKws);
					JNIHelper.logd("receive TagKws = "+mKwString);
					String[] tagKws = mKwString.split("，");
					setTagKeyWords(tagKws);
					AppLogic.removeBackGroundCallback(mRunnableRestartWakeupNow);
					AppLogic.runOnBackGround(mRunnableRestartWakeupNow, 0);
				} catch (InvalidProtocolBufferNanoException e) {
				}
				break;
			}
			}
			break;
		}
		}
		return super.onEvent(eventId, subEventId, data);
	}
	
	
	private String[] mInstantWakeupKeywordsCache; // 本地免唤醒缓存
	/**
	 * 更新免唤醒的命令词
	 * @param keywords
	 */
	public void updateInstantWakeupKeywords(String[] keywords){
		if (Arrays.equals(mInstantWakeupKeywordsCache, keywords)) {
			JNIHelper.logd("updateInstantWakeupKeywords keywords not changed");
			return;
		}
		
		mInstantWakeupKeywordsCache = keywords;
		
		do {
			JNIHelper.logd("updateInstantWakeupKeywords keywords = " + Arrays.toString(keywords));
			if (keywords == null || keywords.length == 0) {
				JNIHelper.logd("updateInstantWakeupKeywords keywords is empty : " + keywords);
				break;
			}
			List<String> instantAsrkwsList = new ArrayList<String>();
			List<String> oneshotKwsList = new ArrayList<String>();
			List<String> rearInstantAsrKwsList = new ArrayList<String>();
			for (String kw : keywords){
				if (TextUtils.isEmpty(kw)){
					continue;
				}else if(kw.startsWith("<")){
					oneshotKwsList.add(kw.substring(1, kw.length() - 1));
				}else if (kw.endsWith("$")) {
					rearInstantAsrKwsList.add(kw.substring(0, kw.length() - 1));
				}else{
					instantAsrkwsList.add(kw);
				}
			}
			if (!oneshotKwsList.isEmpty()){
				String[] oneshotKws = new String[oneshotKwsList.size()];
				oneshotKws = oneshotKwsList.toArray(oneshotKws);
				JNIHelper.logd("oneshotKws :  "+ Arrays.toString(oneshotKws));
				setOneShotKeyWords(oneshotKws);
			}
			
			if (!instantAsrkwsList.isEmpty()){
				String[] Kws = new String[instantAsrkwsList.size()];
				Kws = instantAsrkwsList.toArray(Kws);
				JNIHelper.logd("instantAsrkws : "+ Arrays.toString(Kws));
				mRawInstantAsrKeywords = Kws;
			}
			
			if (!rearInstantAsrKwsList.isEmpty()) {
				String[] rearKws = new String[rearInstantAsrKwsList.size()];
				rearInstantAsrKwsList.toArray(rearKws);
				JNIHelper.logd("rearInstantAsrKws : " + Arrays.toString(rearKws));	
				mRearInstantAsrKws = rearKws;
			}
			
		} while (false);
		
		/*JNIHelper.logd("instantAsr::updating wakeup keywords: "+ Arrays.toString(keywords));
		mRawInstantAsrKeywords = keywords;*/
		
		if(null == mRawInstantAsrKeywords || 0 == mRawInstantAsrKeywords.length){
			RecorderCenter.setEnableInstantAsr(false);
		}else{
			RecorderCenter.setEnableInstantAsr(true);
		}
		++updateKeywordsCur;
		
		synchronized (WakeupManager.class) {
			stopInner();
			startInner();
		}
	}
	
	public String[] getDeviceNicks() {
		Set<String> nickList = new HashSet<String>();

		if (null != mWakeupKeywords_User) {
			for (String kws : mWakeupKeywords_User) {
				String str = null;
				try {
                    str = kws.replaceAll(DEVICE_NICK_REPLACE_REGX, "").trim();
                } catch (Exception e) {
					JNIHelper.loge("replace " + kws + e.toString());
					str = kws;
				}
				if (!TextUtils.isEmpty(str)) {
					nickList.add(str);
				}

			}
		}

		if (null != mWakeupKeywords_Sdk) {
			for (String kws : mWakeupKeywords_Sdk) {
				String str = null;
				try {
                    str = kws.replaceAll(DEVICE_NICK_REPLACE_REGX, "").trim();
                } catch (Exception e) {
					JNIHelper.loge("replace " + kws + e.toString());
					str = kws;
				}
				if (!TextUtils.isEmpty(str)) {
					nickList.add(str);
				}
			}
		}

		String[] ret = new String[nickList.size()];
		nickList.toArray(ret);
		JNIHelper.logd("instantAsr::getDeviceNicks nick = " + Arrays.toString(ret));
		return ret;
	}
	
	public void setInstantAsrEnabled(Boolean enable) {
		if (bEnableInstantAsr != enable) {
			bEnableInstantAsr = enable;
			++updateKeywordsCur;
			stop();
			start();
		}
	}
	
	//更新唤醒词(包括方案商的出厂唤醒词和用户配置的唤醒词)
	public void updateWakuepKeywords_Normal(String[] cmds){
		LogUtil.logd("updateWakuepKeywords_Normal cmds="
				+ (cmds == null ? "null" : Arrays.toString(cmds)));
		synchronized (WakeupManager.class) {
			mWakeupKeywords_Sdk = cmds;
			updateOneShotKeyWords();
			updateWakeupKeywords2Asr(mWakeupKeywords_Sdk);
			
			++updateKeywordsCur;
			
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
	
	public void updateWakupKeywords_Sdk(String[] cmds) {
		LogUtil.logd("updateWakupKeywords_Sdk cmds="
				+ (cmds == null ? "null" : Arrays.toString(cmds)));
		UserConf.getInstance().getFactoryConfigData().mWakeupWords = cmds;
		UserConf.getInstance().saveFactoryConfigData();
		mergeWakeupWords();
	}
	
	public String[] getWakeupKeywords_Sdk() {
		return mWakeupKeywords_Sdk;
	}

	public String[] getWakeupKeywords_User() {
		return mWakeupKeywords_User;
	}

	public void updateWakupKeywords_User(String[] cmds) {
		LogUtil.logd("updateWakupKeywords_User isShowSettings:" + ConfigUtil.isShowSettings()
				+ "mEnableCoverDefaultKeywords:" + mEnableCoverDefaultKeywords);
		if (ConfigUtil.isShowSettings() && mEnableCoverDefaultKeywords) {
			// 当可以覆盖默认唤醒词，将所有的唤醒词保存在mWakeupKeywords_Sdk中
			// 目前为了解决设置界面唤醒词重复问题，后期需要优化整个唤醒词逻辑
			// mWakeupKeywords_User = cmds;
			mWakeupKeywords_User = null;
			++updateKeywordsCur;
			updateWakupKeywords_Sdk(cmds);
		} else {
			synchronized (WakeupManager.class) {
				mWakeupKeywords_User = cmds;
				++updateKeywordsCur;
				updateOneShotKeyWords();
				updateWakeupKeywords2Asr(mWakeupKeywords_User);
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
	}
	
	/**
	 * 将唤醒词插入到离线词表中，不包含唤醒命令
	 * @param cmds
	 */
	private void updateWakeupKeywords2Asr(String[] cmds) {
		if (cmds != null) {
			WakeupAsrKeywords pbWakeupAsrKeywords = new WakeupAsrKeywords();
			pbWakeupAsrKeywords.rptStrKws = cmds;
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_ADD_WAKEUP_KEYWORDS, pbWakeupAsrKeywords);
		}
	}

	public void enableWakeup(boolean wakeup) {
		LogUtil.logd("enableWakeup "+wakeup);
		if (wakeup) {
			mEnableWakeup = true;
			start();
		} else {
			mEnableWakeup = false;
			stop();
		}
		ConfigManager.getInstance().notifyRemoteSync();
	}

	public boolean coverDefaultKeywords = true;

	public void enableCoverDefaultKeywords(boolean coverDeafaultKeywords) {
		this.mEnableCoverDefaultKeywords = coverDeafaultKeywords;
		ConfigManager.getInstance().notifyRemoteSync();
	}

	public void mergeWakeupWords(){
		List<String> kwList = new ArrayList<String>();
		do{
			//添加方案商设置的出厂的唤醒词
			String[] factoryWakeupWords = UserConf.getInstance().getFactoryConfigData().mWakeupWords;
			if (factoryWakeupWords != null && factoryWakeupWords.length > 0) {
				for (String kw : factoryWakeupWords){
					if (!kwList.contains(kw)){
						kwList.add(kw);
					}
				}
			}else{
				//适配程序关闭了唤醒
				kwList.clear();
				//break;
			}
			
			//优先使用用户设置的唤醒开关和唤醒词
			Boolean userWakeupEnable = UserConf.getInstance().getUserConfigData().mWakeupEnable;
			//用户选择了关闭唤醒, 忽略适配设置唤醒词的操作
			if (userWakeupEnable != null && userWakeupEnable == false) {
				kwList.clear();
				break;
			}
			
			//用户未选择或者选择了开启唤醒
			//用户设置了唤醒词
			String[] userWakeupWords = UserConf.getInstance().getUserConfigData().mWakeupWords;
			if (userWakeupWords != null && userWakeupWords.length > 0) {
				for (String kw : userWakeupWords){
					if (!kwList.contains(kw)){
						kwList.add(kw);
					}
				}
			}
		}
		while(false);
		
		String[] kwArray = new String[kwList.size()];
		updateWakuepKeywords_Normal(kwList.toArray(kwArray));
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
				final String[] kws = new String[json.length()];
				for (int i = 0; i < json.length(); ++i) {
					kws[i] = json.getString(i);
				}
				AppLogic.runOnBackGround(new Runnable() {
					@Override
					public void run() {
						if (kws.length > 0) {
							mEnableWakeup = true;
						}
						updateWakupKeywords_Sdk(kws);
						
						RecorderWin.delInterruptKws();
					}
				});
			} catch (Exception e) {
			}
			return null;
		}
		if ("setInstantAsrEnable".equals(command)) {
			boolean enable = Boolean.parseBoolean(new String(data));
			
			setInstantAsrEnabled(enable);
			
			return null;
		}
		if ("setKwsThreshold".equals(command)) {
			setWakeupKeywordsThreshold(new String(data));
			return null;
		}
		
		// 设置免唤醒命令词
//		if("update_instant".equals(command)){
//			try {
//				JSONArray json = new JSONArray(new String(data));
//				String[] kws = new String[json.length()];
//				for (int i = 0; i < json.length(); ++i) {
//					kws[i] = json.getString(i);
//				}
//				updateInstantWakeupKeywords(kws);
//			} catch (Exception e) {
//				JNIHelper.logd("instantAsr::resolving keywords json encountered error: " + e.toString());
//			}
//			return null;
//		}
		if (command.equals("enableChangeWakeupKeywords")) {
			boolean b = Boolean.parseBoolean(new String(data));
			if (mEnableChangeWakeupKeywords != b) {
				mEnableChangeWakeupKeywords = b;
				++updateKeywordsCur;
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
			try {
				float score = Float.parseFloat(new String(data));
				UserConf.getInstance().getFactoryConfigData().mWakeupThreshholdVal = score;
				UserConf.getInstance().saveFactoryConfigData();
				setWakeupThreshhold(score);
			} catch (Exception e) {

			}
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
		if (command.equals("getkeywords")) {
			return getUserWakeupkeywords();
		}
		if (command.equals("setNotifyOnWakeupFlags")) {
			if (data == null) {
				return null;
			}
			JSONBuilder jsonData = new JSONBuilder(data);
			int notifyOnWakeupFlags = jsonData.getVal("notifyOnWakeupFlags", Integer.class, 0);
			setNotifyOnWakeupFlags(notifyOnWakeupFlags);
			return null;
		}
		return null;
	}
	

	
	/**
	 * 针对唤醒词设置不同的阈值
	 * @param jsonScoreKws
	 */
	public void setWakeupKeywordsThreshold(String jsonScoreKws) {
		if(DebugCfg.DISABLE_WAKEUPKW_THRESHOULD){
			LogUtil.logd("setWakeupKeywordsThreshold disabled");
			return;
		}
		JNIHelper.logd("setWakeupKeywordsThreshold jsonScoreKws = "+jsonScoreKws);
		try {
			JSONArray ja = new JSONArray(jsonScoreKws);
			for (int i = 0; i < ja.length(); i++) {
				JSONObject jo = (JSONObject) ja.get(i);
				thresholdKeyWords.put(jo.getString("keyWords"),
						(float) jo.getDouble("threshold"));
				if((float)jo.getDouble("threshold") < mWakeupMinThreshold){
					mWakeupMinThreshold = (float) jo.getDouble("threshold");
				}
			}
		} catch (JSONException e) {
		}
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
		recoverWakeupFromAsrInner(service,
				callback.getTaskId(), false, callback);
		
		String[] oldKws = keyWords;

		JNIHelper.logd("useWakeupAsAsr begin: " + service + "@"
				+ callback.getTaskId());

		synchronized (WakeupManager.class) {
			final WakeupAsrTask task = new WakeupAsrTask(service, callback);
			synchronized (mWakeupKeywords_Asr) {
				mWakeupKeywords_Asr.add(task);
				++updateKeywordsCur;
				mReadWriteLock.writeLock().lock();
				if (task.callback != null && task.callback.needAsrState()) {
					++mUsingAsrInnerCount;
				}
				mReadWriteLock.writeLock().unlock();
			}
			
			updateComponentKeywords();
			
			boolean noChange = checkSameArray(oldKws, keyWords);

			if (noChange) {
				JNIHelper.logd("useWakeupAsAsr nochange: " + service + "@"
						+ callback.getTaskId());
			}
			
			if (!noChange) {
				stopInner();
			}

			String hint = callback.needTts();

			// RecorderWin.setState(STATE.STATE_RECORD);

			if (hint == null) {
				if (startInner()) {
					// 需要识别态没有tts时默认beep音结束
					if (callback.needAsrState()
							&& !TtsManager.getInstance().isBusy()
							&& !AsrManager.getInstance().isBusy()) {
						MusicManager.getInstance().onEndBeep();
					}
				}
				JNIHelper.logd("useWakeupAsAsr end with no hint: " + service + "@"
						+ callback.getTaskId());
				return;
			}

			if (/*ProjectCfg.mEnableAEC && */!ProjectCfg.needStopWkWhenTts()) {
				startInner();
			}

			task.ttsId = TtsManager.getInstance().speakVoice(hint, TtsManager.BEEP_VOICE_URL,
					new TtsUtil.ITtsCallback() {
						@Override
						public void onBegin() {
							callback.onTtsBegin();
						}
				
						@Override
						public void onEnd() {
							callback.onTtsEnd();
							
							checkUsingAsr(new Runnable() {
								@Override
								public void run() {
									MusicManager.getInstance().onEndBeep();
								}
							}, null);
								
							start();
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
		boolean ret = false;

		JNIHelper.logd(service + " recoverWakeupFromAsr " + service + "@"
				+ taskId);
		
		boolean reallyRestart = false;

		synchronized (mWakeupKeywords_Asr) {
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
					//对于特殊任务，需要特殊处理，反注册时确实需要重启唤醒引擎
					if ((oldTask.callback != null && oldTask.callback.getPriority() == AsrUtil.WKASR_PRIORITY_NO_INSTANT_WK) //焦点唤醒任务
							|| RecordManager.RECORD_TASK_ID.equals(taskId) //录音识别任务
							|| CallManager.WAKEUP_INCOMING_TASK_ID.equals(taskId) //来点识别任务
					) {
						reallyRestart = true;
					}
					++updateKeywordsCur;
					if (oldTask != null) {
						if (oldTask.callback != null
								&& oldTask.callback.needAsrState()) {
							--mUsingAsrInnerCount;
						}
						oldTask.cancelTts();
					}
					break;
				} else
					++i;
			}
			mReadWriteLock.writeLock().unlock();
		}

		// 重启唤醒
		if (restartWakeup) {
			checkUsingAsr(null, new Runnable() {
				@Override
				public void run() {
					if (RecorderWin.isSelecting()) {
						RecorderWin.setState(STATE.STATE_END);
					}
					MusicManager.getInstance().onEndAsr();
				}
			});
			
			String kws[] = mWakeupKeywords_Sdk; //大部分客户唤醒词都不为空，为空的则有关唤醒的需求，需要强制重启次唤醒防止发生异常
			if (reallyRestart || (kws == null || kws.length == 0)) {
				stopInner();
				startInner();
			} else {
				//解决部分应用在core启动的时候反注册唤醒词，还可以识别出唤醒词的问题
				String[] strKws = genWakeupKeywords();
				{
					WakeupAsrKeywords pbWakeupAsrKeywords = new WakeupAsrKeywords();
					pbWakeupAsrKeywords.rptStrKws = strKws;
					JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
							VoiceData.SUBEVENT_VOICE_SET_WAKEUP_ASR_KEYWORDS,
							pbWakeupAsrKeywords);
				}
			}
		}

		return ret;
	}
	
	/**
	 * 判断是否是免唤醒的命令词
	 * @param keyword
	 * @return
	 */
	private boolean isInstantAsrKeyword(String keyword){	
		if(null != mInstantAsrKeywords) {
			for(String str : mInstantAsrKeywords){
				if(str.equals(keyword)){
					return true;
				}
			}
		}
		
		if(null != mRearInstantAsrKws) {
			for(String str : mRearInstantAsrKws) {
				if(str.equals(keyword)) {
					return true;
				}
			}
		}
			
		return false;
	}
	
	/**
	 * 是否包含免唤醒前缀
	 */
	public boolean isSimilarAsrKeyword(String keywords) {
		if (null == mRawInstantAsrKeywords || 0 == mRawInstantAsrKeywords.length) {
			return false;
		}

		for (String str : mRawInstantAsrKeywords) {
			if (!TextUtils.isEmpty(str) && keywords.startsWith(str)) {
				return true;
			}
		}

		return false;
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
		if (enable == false)
			mWakeupBeginTime = 0;//重启唤醒记录了时间后，若不给唤醒引擎声音数据，当开始给引擎声音数据时，引擎会拿到时间点开始的所有声音数据
		if (!InterruptTts.getInstance().isInterruptTTS() &&  (!enable)) {
			RecorderWin.setState(STATE.STATE_END);
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
	
	//保护引擎需要的变量
	protected boolean mProInited = false;
	
	/**
	 * 对避免误唤醒的唤醒引擎进行初始化
	 */
	public void initProWakeup(){
		if (mProWakeup != null && mProInited)
			return;
		JNIHelper.logd("init ProWakeup");
		try {
			mProWakeup = (ISenceWakeup) Class.forName(ImplCfg.getPreWakeupImpClass())
					.newInstance();
		} catch (Exception e) {
			JNIHelper.loge("mProWakeup init failed e = "+e.getMessage());
		}
		mProWakeup.initialize(new  ISenceWakeup.IInitCallback() {
			
			@Override
			public void onInit(boolean bSuccess) {
				JNIHelper.logd("init ProWakeup:"+bSuccess);
				mProInited = bSuccess;
			}
		});
		
	}
	
	public void startSyncWakeup(boolean isRef){
		JNIHelper.logd("startSyncWakeup isRef = "+isRef);
		if(!mProInited){//未初始化
			initProWakeup();
			return;
		}
		
		mProWakeup.start(new ISenceWakeupCallback() {

			@Override
			public void onVolume(int vol) {
			}

			@Override
			public void onSpeechBegin() {
				JNIHelper.logd("startSyncWakeup onSpeechBegin");
			}

			@Override
			public void onSpeechEnd() {
				JNIHelper.logd("startSyncWakeup onSpeechBegin");
			}

			@Override
			public void onWakeUp(String text, int time) {
				long attackTime = SystemClock.elapsedRealtime();
				JNIHelper.logd("startSyncWakeup onWakeUp text:"+text+" ,attackTime:"+attackTime);
				putPreCmds(text, FmManager.getInstance().fmeEnable ? attackTime
						+ 500 + FmManager.getInstance().fmeDelay
						: attackTime + 500);
			}
			
		}, new SenceWakeupOption().setBeginTime(mWakeupBeginTime), isRef ? ITXZSourceRecorder.READER_TYPE_REFER:ITXZSourceRecorder.READER_TYPE_INNER, keyWords);
		
		
	}
	public List<String[]> cmdList = new ArrayList<String[]>();
	//开始前置识别引擎的识别
	public void startPreWakeup(boolean isRef) {
		JNIHelper.logd("startPreWakeup ");
		if(!mProInited){//未初始化
			initProWakeup();
			return;
		}
		String[] cmds = handleCmdsToArray(cmdList);
		mProWakeup.start(new ISenceWakeupCallback() {

			@Override
			public void onVolume(int vol) {
			}

			@Override
			public void onSpeechBegin() {
				JNIHelper.logd("PreWakeup onSpeechBegin");
			}

			@Override
			public void onSpeechEnd() {
				JNIHelper.logd("PreWakeup onSpeechEnd");
			}

			@Override
			public void onWakeUp(String text, int time) {
				JNIHelper.logd("PreWakeup onWakeUp text:"+text+" ,time:"+time+" cmdList:"+(cmdList == null ? null:cmdList.toString()));
				for (String[] cL : cmdList) {
					if(TextUtils.equals(text, cL[0])){
						long attackTime = SystemClock.elapsedRealtime();
						putPreCmds(cL[1], attackTime+300+FmManager.getInstance().fmeDelay);
					}
				}
				
			}
		}, null, isRef ? ITXZSourceRecorder.READER_TYPE_REFER:ITXZSourceRecorder.READER_TYPE_INNER, cmds);
	}
	
	public void stopProWakeup(){
		JNIHelper.logd("stopProWakeup");
		if(mProWakeup == null){
			return;
		}
		mProWakeup.stop();
	}
	
	/**
	 * 设置唤醒的开始时间
	 * @param time
	 */
	public void setWakeupBeginTime(long time){
		mWakeupBeginTime = time;
	}


	/**
	 * 将集合中的数据取出，返回String[]
	 * @param cmds
	 */
	private String[] handleCmdsToArray(List<String[]> cmds) {
		if(cmds.isEmpty()){
			return null;
		}
		List<String> list = new ArrayList<String>();
		for (String[] c : cmds) {
			list.add(c[0]);
			JNIHelper.logd("handleCmdsToArray keyWords:"+c[0]);
		}
		String[] cmdArray =  (String[]) list.toArray(new String[list.size()]);
		return cmdArray;
	}
	
	public static final long WAKEUP_PRO_END_TIME = -1;
	
	//往map中添加数据
	public synchronized boolean putPreCmds(String keyWords, long time){
		if(TextUtils.isEmpty(keyWords)){
			return false;
		}
		proCmds.put(keyWords, time);
		return true;
	}
	/**
	 * 插入一个列表的禁用唤醒词
	 * @param keyWords 禁用的唤醒词的list
	 * @param time 统一的禁用时间
	 * @return
	 */
	public synchronized boolean putPreCmds(ArrayList<String> keyWords, long time){
		if(keyWords.isEmpty()){
			return false;
		}
		for (String kW : keyWords) {
			if(TextUtils.isEmpty(kW)){
				continue;
			}
			putPreCmds(kW, time);
		}
		return true;
	}
	
	/**
	 * 清除禁用唤醒词列表
	 */
	public synchronized void clearProCmds(){
		proCmds.clear();
	}
	
	public byte[] getUserWakeupkeywords() {
		if (mWakeupKeywords_User==null) {
			JNIHelper.logd("userWakeupKeywords is empty!!");
			return null;
		}	
		JSONObject json = new JSONObject();
		JSONArray keywords = new JSONArray();
		for (String wakeup : mWakeupKeywords_User) {
			keywords.put(wakeup);
		}
		try {
			json.put("keywords", keywords);
		} catch (JSONException e) {
			JNIHelper.loge("userWakeupKeywords error:: " + e);
		}
		JNIHelper.logd("userWakeupKeywords::" + json);
		return json.toString().getBytes();	
	}
	
	/**
	 * 设置播报的打断词
	 * @param kws
	 */
	public void setInterruptTips(String[] kws){
		LogUtil.logd("setInterruptTips "+Arrays.toString(kws));
		mInterruptKws = kws;
	}

	public String[] getInterruptTips() {
		return mInterruptKws;
	}

	public String getJSZInterruptKw(){
		return mJSZInterruptKw;
	}
	
	public boolean needDelInterruptKws() {
		String kws[] = mWakeupKeywords_Sdk;
		return (mInterruptKws == null || mInterruptKws.length ==0 ) || kws == null || kws.length == 0;
	}

	public boolean needDelJSZInterruptKws() {
		String kws[] = mWakeupKeywords_Sdk;
		return TextUtils.isEmpty(mJSZInterruptKw) || kws == null || kws.length == 0;
	}

	public List<String> getWakeupOneShotKws() {
		return new ArrayList<String>(mWakeupOneShotKws);
	}
}

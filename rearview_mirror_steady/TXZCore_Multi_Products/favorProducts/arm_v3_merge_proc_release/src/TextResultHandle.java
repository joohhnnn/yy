package com.txznet.txz.module.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.txz.report_manager.ReportManager;
import com.txz.ui.data.UiData.TestResp;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.ReportUtil.Report;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.mix.local.LocalAsrPachiraImpl;
import com.txznet.txz.component.text.IText;
import com.txznet.txz.component.text.IText.IInitCallback;
import com.txznet.txz.component.text.IText.ITextCallBack;
import com.txznet.txz.component.text.IText.PreemptLevel;
import com.txznet.txz.component.text.ifly.TextIflyImpl;
import com.txznet.txz.component.text.txz.TextTxzHighLocalImpl;
import com.txznet.txz.component.text.txz.TextTxzImpl;
import com.txznet.txz.component.text.yunzhisheng_3_0.TextYunzhishengImpl;
import com.txznet.txz.component.wakeup.IWakeup.WakeupKwType;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.netdata.NetDataManager;
import com.txznet.txz.module.sim.SimManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;
import com.txznet.txz.plugin.interfaces.NlpTransitionToTxz;
import com.txznet.txz.plugin.interfaces.TextImplBasic;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.STATE;
import com.txznet.txz.util.runnables.Runnable3;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;

public class TextResultHandle extends IModule {
	// 分数常量 如分数比TEXT_SCORE_CONFIDENCE高则可以进行直接处理
	public static final float TEXT_SCORE_MAX = 100f;
	public static final float TEXT_SCORE_MIDDLE = 50f;
	public static final float TEXT_SCORE_LOW = 20f;
	/** 不识别场景使用 */
	public static final float TEXT_SCORE_MIN = 1f;
	/** 一般场景使用 */
	public static final float TEXT_SCORE_CONFIDENCE = 90f;
	public static final float TEXT_SCORE_CONFIDENCE_LITTLE = 85;
	public static final float TEXT_SCORE_CONFIDENCE_MORE = 95f;
	/** 聊天场景使用 */
	public static final float TEXT_SCORE_NOCONFIDENCE = 30f;
	public static final float TEXT_SCORE_NOCONFIDENCE_LITTLE = 25;
	public static final float TEXT_SCORE_NOCONFIDENCE_MORE = 35f;
	/** 传入数据无效 */
	public static final float TEXT_SCORE_INVALID = -1f;
	public static final float TEXT_SCORE_NOCONFIDENCE_MINUS = 5f;
	// 回传的三种消息
	private static final int ONLINE_SUCCESS = 0;
	private static final int LOCAL = 1;
	private static final int ONLINE_ERROR = -1;
	// 是否已经结束解析
	private boolean mEndParse;
	// 临时存储的最优json
	private VoiceParseData mTxzScene;
	private TestResp mResp;
	// 处理引擎集合
	private Vector<IText> mTexts = new Vector<IText>();
	// 处理结果回调
	private VoiceDataResultCallBack sysCallBack;
	private TextResultCallBack sysTextCallBack;
	// 初始处理时间
	private static long beginTime = 0;
	// 是否开启同行者语义 同行者语义级别(都由后台下发)
	public boolean mOpenTxzNlp = false;
	public int mTxzNlpLevel = UiEquipment.NLP_LEVEL_NORMAL;
	private int mDisableTxzNlpScene = 0;
	private int mEnableTxzNlpScene = 0;
	private int mDisableTxzNlpChatScene = 0;
	private int mEnableTxzNlpChatScene = 0;
	public static final int MODULE_LOCAL_HIGH_MASK = 0x1;
	public static final int MODULE_LOCAL_NORMAL_MASK = 0x2;
	public static final int MODULE_YUNZHISHENG_MASK = 0x4;
	public static final int MODULE_TXZ_MASK = 0x8;
	// 引擎解析结果对应开启语义组件掩码
	private int mYxzOnlineMask = MODULE_LOCAL_HIGH_MASK
			+ MODULE_LOCAL_NORMAL_MASK;
	private int mIfyOnlineMask = MODULE_LOCAL_HIGH_MASK
			+ MODULE_LOCAL_NORMAL_MASK + MODULE_YUNZHISHENG_MASK;
	private int mYxzOfflineMask = MODULE_LOCAL_HIGH_MASK;
	private int mRawMask = MODULE_LOCAL_HIGH_MASK + MODULE_LOCAL_NORMAL_MASK
			+ MODULE_YUNZHISHENG_MASK;
	private int mEnableMask = MODULE_LOCAL_HIGH_MASK | MODULE_LOCAL_NORMAL_MASK
			| MODULE_YUNZHISHENG_MASK | MODULE_TXZ_MASK;
	private int mTextMask = 0;
	
	//当前使用解析的mask
	private int mCurrentMask = 0;

	// 最大优先级
	private static final int MAX_LEVEL = 101;
	// 每个优先级最大组件数
	private int mModuleMaxCount[] = new int[MAX_LEVEL];
	// 每个优先级已返回组件数
	private int mModuleCount[] = new int[MAX_LEVEL];
	// 每个优先级是否有正确结果
	private boolean mResult[] = new boolean[MAX_LEVEL];
	// 结果类型
	private int mType = 0;
	private static final int TYPE_JSON = 1;
	private static final int TYPE_RESP = 2;
	// 当前最好优先级结果
	private int mCurrentPriority = 0;
	// 当前最好优先级结果
	private boolean mFastParse = true;
	private float mFastScore = TEXT_SCORE_CONFIDENCE;
	// 识别结束后可以执行的runnable
	private Runnable mDismissRunnable = null;
	// 后台下发的规则信息存储
	private HashMap<String, String> mBeforeRegexps = null;
	private HashMap<String, String> mAfterRegexps = null;
	private HashSet<String> mServerRegexps = null;
	
	// 识别电话的类型
	public static final int CALL_LOCAL_YZS_CH = 0;//离线中文
	public static final int CALL_LOCAL_YZS_EN = 1;//离线英文
	public static final int CALL_NET_YZS = 2;//在线云知声
	public static final int CALL_NET_IFLY = 3;//在线讯飞

	public void setBeforeRegexps(HashMap<String, String> mBeforeRegexps) {
		this.mBeforeRegexps = mBeforeRegexps;
	}

	public void setAfterRegexps(HashMap<String, String> mAfterRegexps) {
		this.mAfterRegexps = mAfterRegexps;
	}

	public void setServerRegexps(HashSet<String> mServerRegexps) {
		this.mServerRegexps = mServerRegexps;
	}

	public HashMap<String, String> getBeforeRegexps() {
		return mBeforeRegexps;
	}

	public HashMap<String, String> getAfterRegexps() {
		return mAfterRegexps;
	}


	private static TextResultHandle sModuleInstance = new TextResultHandle();
	private HandlerThread mWorkThread = null;
	private Handler mHandler = null;
	private String mStrText = null;

	private List<ParserTask> mParserQueue = new LinkedList<ParserTask>();
	private ParserTask mCurrTask = null;

	private class ParserTask {
		VoiceParseData data;
		ITextCallBack callBack;
		ITextCallBack preCallBack;
		PreemptLevel preemptLevel;
		int moduleMask;
	}

	private TextResultHandle() {
		mInited = false;
	}

	public static TextResultHandle getInstance() {
		return sModuleInstance;
	}

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_PARSE_NEW);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_NLP);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_NLP);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_RESP_WEATHER);
		return ERROR_SUCCESS;
	}

	private class NlpCommandProcessor implements CommandProcessor {

		@Override
		public Object invoke(String command, Object[] args) {
			if (command.equals("changeImpl")) {
				if (!(args[1] instanceof Integer)
						|| !(args[0] instanceof TextImplBasic))
					return false;
				int location = (Integer) args[1];
				IText textImple = (TextImplBasic) args[0];
				if (location >= mTexts.size())
					return false;
				mTexts.setElementAt(textImple, location);
			} else if (command.equals("addImpl")) {
				if (!(args[0] instanceof TextImplBasic))
					return false;
				IText textImpl = (TextImplBasic) args[0];
				mTexts.add(textImpl);
			} else if (command.equals("changeMask")) {
				if (!(args[0] instanceof Integer)
						|| !(args[1] instanceof Integer))
					return false;
				int type = (Integer) args[0];
				int mask = (Integer) args[1];
				switch (type) {
				case 0:
					mRawMask = mask;
					break;
				case 1:
					mIfyOnlineMask = mask;
					break;
				case 2:
					mYxzOnlineMask = mask;
					break;
				case 3:
					mYxzOfflineMask = mask;
					break;
				default:
					break;
				}
			} else if (command.equals("ifyImpl")) {
				if (!(args[0] instanceof NlpTransitionToTxz))
					return false;
				TextIflyImpl.mTransitionImpl = (NlpTransitionToTxz) args[0];
			} else if (command.equals("yzsOnLImpl")) {
				if (!(args[0] instanceof NlpTransitionToTxz))
					return false;
				TextYunzhishengImpl.mOnlineTransitionImpl = (NlpTransitionToTxz) args[0];
			} else if (command.equals("yzsOffLImpl")) {
				if (!(args[0] instanceof NlpTransitionToTxz))
					return false;
				TextYunzhishengImpl.mLocalTransitionImpl = (NlpTransitionToTxz) args[0];
			} else if (command.equals("send")) {
				if (!(args[0] instanceof Integer)
						|| !(args[1] instanceof Integer))
					return false;
				int event = (Integer) args[0];
				int subEvent = (Integer) args[1];
				JNIHelper.sendEvent(event, subEvent);
			} else if (command.equals("sendString")) {
				if (!(args[0] instanceof Integer)
						|| !(args[1] instanceof Integer)
						|| !(args[2] instanceof String))
					return false;
				int event = (Integer) args[0];
				int subEvent = (Integer) args[1];
				String data = (String) args[2];
				JNIHelper.sendEvent(event, subEvent, data);
			} else if (command.equals("sendMessage")) {
				if (!(args[0] instanceof Integer)
						|| !(args[1] instanceof Integer)
						|| !(args[2] instanceof MessageNano))
					return false;
				int event = (Integer) args[0];
				int subEvent = (Integer) args[1];
				MessageNano message = (MessageNano) args[2];
				JNIHelper.sendEvent(event, subEvent, message);
			} else
				return false;
			return true;
		}

	}

	@Override
	public int initialize_addPluginCommandProcessor() {
		PluginManager
				.addCommandProcessor("txz.nlp.", new NlpCommandProcessor());
		PluginManager.addCommandProcessor("txz.event.",
				new NlpCommandProcessor());
		return 0;
	}

	public void initializeComponent() {
		if (mTexts.size() != 0) {
			return;
		}
		// 可以增加多个引擎，如果不需要不可以在这里注释，可以在getMaskByType函数更改mask
		String iTextClass[] = {
				"com.txznet.txz.component.text.txz.TextTxzHighLocalImpl",
				"com.txznet.txz.component.text.txz.TextTxzLocalImpl",
				"com.txznet.txz.component.text.yunzhisheng_3_0.TextYunzhishengImpl",
				"com.txznet.txz.component.text.txz.TextTxzImpl" };
		for (int i = 0; i < iTextClass.length; i++) {
			IText iText = null;
			try {
				iText = (IText) Class.forName(iTextClass[i]).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			iText.initialize(new IInitCallback() {
				@Override
				public void onInit(boolean bSuccess) {
					JNIHelper.logd("text init:" + bSuccess);
					mInited = bSuccess;
				}
			});
			mTexts.add(iText);
		}
		// 设置后台语义优先级
		mTexts.get(3).setPriority(mTxzNlpLevel);

		sysCallBack = new VoiceDataResultCallBack();
		sysTextCallBack = new TextResultCallBack();
		mWorkThread = new HandlerThread("TextHandle");
		mWorkThread.start();
		mHandler = new Handler(mWorkThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				handleMsg(msg);
			}
		};
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		switch (subEventId) {
		case VoiceData.SUBEVENT_VOICE_PARSE_NEW:
			try {
				mOpenTxzNlp = false;
				parseVoiceData(VoiceParseData.parseFrom(data), sysCallBack);
			} catch (InvalidProtocolBufferNanoException e) {
				e.printStackTrace();
			}
			break;
		case UiEquipment.SUBEVENT_RESP_NLP:
			((TextTxzImpl) mTexts.get(3)).onResult(data);
			break;
		case UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE:
			try {
				UiEquipment.ServerConfig pbServerConfig = UiEquipment.ServerConfig
						.parseFrom(data);
				JNIHelper.logd("nlp:enable=" + pbServerConfig.bTxzNlp
						+ ",level=" + pbServerConfig.uint32TxzNlpLevel);
				if (pbServerConfig.bTxzNlp != null && pbServerConfig.bTxzNlp) {
					mOpenTxzNlp = true;
				} else
					mOpenTxzNlp = false;
				if (pbServerConfig.uint32TxzNlpLevel != null){
					mTxzNlpLevel = pbServerConfig.uint32TxzNlpLevel;
					mTxzNlpLevel = mTxzNlpLevel + (UiEquipment.NLP_LEVEL_GOD - mTxzNlpLevel) / 2;
				}
				if (mTexts != null && mTexts.size() >= 4) {
					mTexts.get(3).setPriority(mTxzNlpLevel);
				}
				if (pbServerConfig.uint32TxzNlpDisableScene != null
						&& pbServerConfig.uint32TxzNlpDisableScene != 0)
					mDisableTxzNlpScene = pbServerConfig.uint32TxzNlpDisableScene;
				else
					mDisableTxzNlpScene = 0;
				if (pbServerConfig.uint32TxzNlpEnableScene != null
						&& pbServerConfig.uint32TxzNlpEnableScene != 0)
					mEnableTxzNlpScene = pbServerConfig.uint32TxzNlpEnableScene;
				else
					mEnableTxzNlpScene = 0;
				if (pbServerConfig.uint32TxzNlpChatDisableScense != null
						&& pbServerConfig.uint32TxzNlpChatDisableScense != 0)
					mDisableTxzNlpChatScene = pbServerConfig.uint32TxzNlpChatDisableScense;
				else
					mDisableTxzNlpChatScene = 0;
				if (pbServerConfig.uint32TxzNlpChatEnableScense != null
						&& pbServerConfig.uint32TxzNlpChatEnableScense != 0)
					mEnableTxzNlpChatScene = pbServerConfig.uint32TxzNlpChatEnableScense;
				else
					mEnableTxzNlpChatScene = 0;
				
				JNIHelper.logd("nlp:mEnableTxzNlpChatScene:"+mEnableTxzNlpChatScene
						+";mDisableTxzNlpChatScene:"+mDisableTxzNlpChatScene
						+";mEnableTxzNlpScene:"+mEnableTxzNlpScene
						+";mDisableTxzNlpScene:"+mDisableTxzNlpScene);
				
			} catch (InvalidProtocolBufferNanoException e) {
				e.printStackTrace();
			}
			break;
		case UiEquipment.SUBEVENT_NOTIFY_NLP:
			try {
				UiEquipment.Push_NLP pNlp = UiEquipment.Push_NLP
						.parseFrom(data);
				handleTxzPushNlp(pNlp.strText, new String(pNlp.strJsonResult),
						pNlp.bInterrupt);
			} catch (InvalidProtocolBufferNanoException e) {
				JNIHelper.logd("server nlp parse error:" + e.getMessage());
			}
			break;
		case UiEquipment.SUBEVENT_RESP_WEATHER:
			try {
				UiEquipment.Resp_Weather weatherData = UiEquipment.Resp_Weather.parseFrom(data);
				NetDataManager.getInstance().handleWeatherData(weatherData);
			} catch (InvalidProtocolBufferNanoException e) {
				JNIHelper.logd("server weather parse error:"+e.getMessage());
			}
			break;
		default:
			break;
		}
		return super.onEvent(eventId, subEventId, data);
	}

	public void parseText(String text, ITextCallBack callBack) {
		if (!mInited) {
			return;
		}
		ParserTask task = new ParserTask();
		VoiceParseData parseData = new VoiceParseData();
		parseData.uint32DataType = VoiceData.VOICE_DATA_TYPE_RAW;
		parseData.strText = text;
		parseData.floatTextScore = TEXT_SCORE_INVALID;
		parseData.uint32Sence = VoiceData.GRAMMAR_SENCE_DEFAULT;
		task.data = parseData;
		task.callBack = callBack;
		task.preCallBack = sysTextCallBack;
		task.preemptLevel = PreemptLevel.PREEMPT_LEVEL_NONE;
		task.moduleMask = 0;
		insertText(task);
	}

	public void parseText(String text, int moduleMask, ITextCallBack callBack) {
		if (!mInited) {
			return;
		}
		ParserTask task = new ParserTask();
		VoiceParseData parseData = new VoiceParseData();
		parseData.uint32DataType = VoiceData.VOICE_DATA_TYPE_RAW;
		parseData.strText = text;
		parseData.floatTextScore = TEXT_SCORE_INVALID;
		parseData.uint32Sence = VoiceData.GRAMMAR_SENCE_DEFAULT;
		task.data = parseData;
		task.callBack = callBack;
		task.preCallBack = sysTextCallBack;
		task.moduleMask = moduleMask;
		insertText(task);
	}

	public void parseText(String text, int moduleMask, ITextCallBack callBack,
			PreemptLevel level) {
		if (!mInited) {
			return;
		}
		ParserTask task = new ParserTask();
		VoiceParseData parseData = new VoiceParseData();
		parseData.uint32DataType = VoiceData.VOICE_DATA_TYPE_RAW;
		parseData.strText = text;
		parseData.floatTextScore = TEXT_SCORE_INVALID;
		parseData.uint32Sence = VoiceData.GRAMMAR_SENCE_DEFAULT;
		task.data = parseData;
		task.callBack = callBack;
		task.preCallBack = sysTextCallBack;
		task.preemptLevel = level;
		task.moduleMask = moduleMask;
		insertText(task);
	}
	
	public void parseText(String text, ITextCallBack callBack,
			PreemptLevel level) {
		if (!mInited) {
			return;
		}
		ParserTask task = new ParserTask();
		VoiceParseData parseData = new VoiceParseData();
		parseData.uint32DataType = VoiceData.VOICE_DATA_TYPE_RAW;
		parseData.strText = text;
		parseData.floatTextScore = TEXT_SCORE_INVALID;
		parseData.uint32Sence = VoiceData.GRAMMAR_SENCE_DEFAULT;
		task.data = parseData;
		task.callBack = callBack;
		task.preCallBack = sysTextCallBack;
		task.preemptLevel = level;
		task.moduleMask = 0;
		insertText(task);
	}

	public void parseVoiceData(VoiceParseData data, int moduleMask,
			ITextCallBack callBack) {
		if (!mInited) {
			return;
		}
		ParserTask task = new ParserTask();
		task.data = data;
		task.callBack = callBack;
		task.preCallBack = sysCallBack;
		task.moduleMask = moduleMask;
		task.preemptLevel = PreemptLevel.PREEMPT_LEVEL_NONE;
		insertText(task);
	}
	
	public void parseVoiceData(VoiceParseData data, ITextCallBack callBack) {
		if (!mInited) {
			return;
		}
		ParserTask task = new ParserTask();
		task.data = data;
		task.callBack = callBack;
		task.preCallBack = sysCallBack;
		task.preemptLevel = PreemptLevel.PREEMPT_LEVEL_NONE;
		insertText(task);
	}

	public void parseVoiceData(VoiceParseData data, ITextCallBack callBack,
			PreemptLevel level) {
		if (!mInited) {
			return;
		}
		ParserTask task = new ParserTask();
		task.data = data;
		task.callBack = callBack;
		task.preCallBack = sysCallBack;
		task.preemptLevel = level;
		insertText(task);
	}

	private void insertText(ParserTask task) {
		synchronized (ParserTask.class) {
			JNIHelper.logd("insertText level=" + task.preemptLevel + ",text="
					+ task.data.strText + ",data=" + task.data.strVoiceData);
			if (task.preemptLevel == PreemptLevel.PREEMPT_LEVEL_NONE) {
				mParserQueue.add(task);
			} else {
				mParserQueue.add(0, task);
			}

			// 紧急任务先处理
			if (task.preemptLevel == PreemptLevel.PREEMPT_LEVEL_IMMEDIATELY) {
				// 当前有任务, 直接取消
				if (mCurrTask != null) {
					JNIHelper.logd("cancel immeadiately");
					cancel();
					return;
				}
			}
			if (mCurrTask == null) {
				parseNext();
			}
		}
	}

	private void parseNext() {
		if (mParserQueue.isEmpty()) {
			return;
		}

		mCurrTask = mParserQueue.get(0);
		mParserQueue.remove(0);
		JNIHelper.logd("parseText text = " + mCurrTask.data.strText);
		mTextMask = mCurrTask.moduleMask;
		handleResult(mCurrTask.data, mCurrTask.preCallBack);
	}
	
	public boolean comparePinYin(String str1, String str2){
		boolean bRet = false;
		if (TextUtils.isEmpty(str1) || TextUtils.isEmpty(str2)){
			return false;
		}
		
		if (str1.length() != str2.length()){
			return false;
		}
		
		if (str1.equals(str2)) {
			bRet = true;
		} else {
			//逐个字符拼音比较,确保匹配出的每个字符至少都是同音字。NOTE:不要使用该方式比较过长的字符串。
			for (int i = 0; i < str1.length(); i++) {
				bRet = NativeData.compareStringWithPinyin("" + str1.charAt(i), 	"" + str2.charAt(i), 5999);
				if (!bRet) {
					return false;
				}
			}
			bRet = true;
		}
		return bRet;
	}
	
	private boolean cutOneShotKw(VoiceParseData oVoiceData){	
		if (null == oVoiceData.uint32DirectAsrType) {
			return false;
		}

		String strText = oVoiceData.strText;
		String strOneShotKw = oVoiceData.strOneshotKw;
		String strDirectAsrKw = oVoiceData.strDirectAsrKw;
		int directAsrType = oVoiceData.uint32DirectAsrType;

		JNIHelper.logd("instantAsr::cutOneShotKw: strText = " + strText
				+ ", oneshotKw = " + strOneShotKw + ", directAsrKw = "
				+ strDirectAsrKw + ", wakeupType = " + directAsrType);

		if (WakeupKwType.KW_TYPE_ONESHOT_ONLY.ordinal() == directAsrType) {
			if (strText.length() <= strOneShotKw.length()) {
				JNIHelper.logd("cut only oneshot, strText = " + strText
						+ ", oneshotKw = " + strOneShotKw);
				oVoiceData.strText = "";
				return true;
			}
			
			if (comparePinYin(strOneShotKw,
					strText.substring(0, strOneShotKw.length()))) {
				oVoiceData.strText = oVoiceData.strText.substring(strOneShotKw
						.length());
				JNIHelper.logd("cut OneShot keyword : " + strOneShotKw
						+ ", new strText : " + oVoiceData.strText);
				
				return true;
			}

			return false;
		}

		if (WakeupKwType.KW_TYPE_DIRECTASR_FRONT.ordinal() == directAsrType
				|| WakeupKwType.KW_TYPE_ONESHOT_DIRECTASR.ordinal() == directAsrType) {
			// 处理免唤醒命令词之前的昵称
			int directAsrKwIndex = strText.indexOf(strDirectAsrKw);
			if (directAsrKwIndex > 0) {
				oVoiceData.strText = strText.substring(directAsrKwIndex);
				JNIHelper.logd("cut confusing nick : " + strOneShotKw
						+ ", new strText : " + oVoiceData.strText);
				
				return true;
			}

			return false;
		}

		if (WakeupKwType.KW_TYPE_DIRECTASR_REAR.ordinal() == directAsrType) {
			// 截断唤醒词后面的文字
			int index = strText.indexOf(strDirectAsrKw);
			if (index >= 0
					&& index < strText.length() - strDirectAsrKw.length()) {
				oVoiceData.strText = strText.substring(0, index
						+ strDirectAsrKw.length());
				JNIHelper.logd("cut word after rearWakeupKw : new text = "
						+ oVoiceData.strText + ", rawText = " + strText);
				
				return true;
			}

			return false;
		}
		
		return false;
	}

	/**
	 * 对外接口，处理传进来的VoiceParseData(在线原始结果)并进行多线程处理。
	 * 
	 * @param parseData
	 */
	public void handleResult(VoiceParseData parseData, ITextCallBack callBack) {
		if (!mInited || parseData == null) {
			// 未初始化异常
			JNIHelper.loge("nlp:why not init or data=null?" + (parseData == null));
			return;
		}
		JNIHelper.logd("nlp:beginHandle");
		initWhenBeginHandle(parseData);
		mTxzScene = voiceDataToTXZVoiceData(parseData);

		// WTF,这个异常不应该存在的
		if (mTxzScene == null) {
			handleError("empty");
			return;
		}

		// 非官方流量卡配置了语音识别成功率
		if (SimManager.getInstance().mAsrPercent != -1) {
			if (Math.random() * 100 > SimManager.getInstance().mAsrPercent) {
				JNIHelper.logd("nlp:percent="
						+ SimManager.getInstance().mAsrPercent);
				handleError("unknown");
				return;
			}
		}

		mCurrentMask = getMaskAndSetTextByScene();
		if (mCurrentMask == 0) {
			cancelHandleResult();
			return ;
		}

		byte[] voiceData = VoiceParseData.toByteArray(mTxzScene);
		for (int i = 0; i < mTexts.size(); i++) {
			if (((0x1 << i) & mCurrentMask) != 0) {
				try {
					mTexts.get(i).setVoiceData(
							VoiceParseData.parseFrom(voiceData), callBack);
				} catch (InvalidProtocolBufferNanoException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void setText() {
		if (!TextUtils.isEmpty(mTxzScene.strText)) {
			// 处理语义运行中原始文本的替换
			if (mBeforeRegexps != null && mBeforeRegexps.size() > 0) {
				for (String key : mBeforeRegexps.keySet()) {
					String value = mBeforeRegexps.get(key);
					if (mTxzScene.strText.matches(key)) {
						mTxzScene.strText = mTxzScene.strText
								.replaceAll(key, value);
						mTxzScene.floatTextScore -= TEXT_SCORE_NOCONFIDENCE_MINUS;
					}
				}
			}
		}
		mStrText = mTxzScene.strText;
	}
	
	private void handleError(String errMsg) {
		mTxzScene.strVoiceData = getStringJSON(errMsg, errMsg, "");
		TextSemanticAnalysis.getInstance().parse(mTxzScene);
		cancelHandleResult();
	}
	
	private void initWhenBeginHandle(VoiceParseData parseData) {
		beginTime = SystemClock.elapsedRealtime();
		mResp = null;
		mEndParse = false;
		mType = 0;
		mFastScore = TEXT_SCORE_CONFIDENCE;
		for (int i = 0; i < MAX_LEVEL; i++) {
			mModuleCount[i] = 0;
			mResult[i] = false;
		}

		// 处理选择页面唤醒和识别一起开的场景
		if (parseData.uint32AsrWakeupType != null
				&& parseData.uint32AsrWakeupType == VoiceData.VOICE_ASR_WAKEUP_TYPE_MIX) {
			mFastParse = false;
			mTexts.get(0).setPriority(IText.PRIORITY_LEVEL_NORMAL);
		} else {
			mFastParse = true;
			mTexts.get(0).setPriority(IText.PRIORITY_LEVEL_LOCAL_HIGH);
		}
	}
	
	private int getMaskAndSetTextByScene() {
		int mask = getMaskByType(mTxzScene.uint32DataType);
		JNIHelper.logd("nlp:mask=" + Integer.toBinaryString(mask));
		// oneshot场景处理
		if (cutOneShotKw(mTxzScene)) {
			// 对原结果降分处理， 强制开启云之声语义
			mTxzScene.floatTextScore -= TEXT_SCORE_NOCONFIDENCE_MINUS;
			mask = mask | MODULE_YUNZHISHENG_MASK;
		}

		setText();

		// 处理免唤醒场景识别文本为空的情况
		// 可能是因为用户只喊了设备昵称
		// 或者网络超时/识别结果可信度太低
		if (TextUtils.isEmpty(mTxzScene.strText) && null != mTxzScene.uint32DirectAsrType) {
			RecorderWin.speakText(
					NativeData.getResString("RS_INTERACTION_HELLO"), null);
			return 0;
		}

		// 开启后台语义组件黑白名单和语义配置文件处理
		mask = txzNlpSwitch(mTxzScene, mask);

		JNIHelper.logd("nlp:final mask=" + Integer.toBinaryString(mask));
		setMaxCountByMask(mask);
		return mask;
	}

	private void cancelHandleResult() {
		synchronized (ParserTask.class) {
			mCurrTask = null;
		}
		parseNext();
	}

	/**
	 * 对传入引擎数据处理，根据类型转换成txz的场景
	 * 
	 * @param parseData
	 * @return
	 */
	public VoiceParseData voiceDataToTXZVoiceData(VoiceParseData parseData) {
		mCurrentPriority = IText.PRIORITY_LEVEL_NORMAL;
		switch (parseData.uint32DataType) {
		case VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON:
			return TextYunzhishengImpl.yzsDataToTxzScene(parseData);
		case VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON:
			return TextYunzhishengImpl.yzsLocalDataToTxzScene(parseData);
		case VoiceData.VOICE_DATA_TYPE_SENCE_JSON:
			return TextIflyImpl.iflyDataToTxzScene(parseData);
		case VoiceData.VOICE_DATA_TYPE_PACHIRA_LOCAL_JSON:
			return LocalAsrPachiraImpl.pachiraDataToTxzScene(parseData);
		default:
			try {
				return VoiceParseData.parseFrom(VoiceParseData
						.toByteArray(parseData));
			} catch (InvalidProtocolBufferNanoException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * 获取引擎ID对应的mask和对应引擎需要哪些其他引擎进行二次处理 mask用作开启解析工具的掩码，mMaxCount用做启动引擎数记录
	 * 
	 * @param dataType
	 * @return mask
	 */
	private int getMaskByType(int dataType) {
		int mask = 0;
		boolean isLocal = false;
		switch (dataType) {
		case VoiceData.VOICE_DATA_TYPE_MIX_JSON:
		case VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON:
			mask = mYxzOnlineMask;
			break;
		case VoiceData.VOICE_DATA_TYPE_BAIDU_SCENE_JSON:
		case VoiceData.VOICE_DATA_TYPE_SENCE_JSON:
		case VoiceData.VOICE_DATA_TYPE_SOGOU_SCENE_JSON:
			mask = mIfyOnlineMask;
			break;
		case VoiceData.VOICE_DATA_TYPE_LOCAL_JSON:
		case VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON:
			isLocal = true;
			mask = mYxzOfflineMask;
			break;
		case VoiceData.VOICE_DATA_TYPE_RAW:
			if (mTextMask != 0)
				mask = mTextMask;
			else
				mask = mRawMask;
			break;
		default:
			mask = mYxzOfflineMask;
			break;
		}
		if (!isLocal && mOpenTxzNlp) {
			mask = mask | MODULE_TXZ_MASK;
		}
		//后台配置关闭语义组件
		if ((ProjectCfg.getNlpEngineDisableType() & UiEquipment.NLP_YUNZHISHENG) == UiEquipment.NLP_YUNZHISHENG)
			mEnableMask = mEnableMask & ~MODULE_YUNZHISHENG_MASK;
		mask = mask & mEnableMask;
		return mask;
	}

	private int txzNlpSwitch(VoiceParseData txzScene, int oldMask) {
		JNIHelper.logd("oldMask=" + Integer.toBinaryString(oldMask)
				+ ",enable=" + Integer.toHexString(mEnableTxzNlpScene)
				+ ",disable=" + Integer.toHexString(mDisableTxzNlpScene));
		int newMask = oldMask;
		if ((newMask & MODULE_TXZ_MASK) == MODULE_TXZ_MASK) {
			if (txzScene == null || txzScene.strVoiceData == null) {
				if (mEnableTxzNlpScene != 0)
					newMask = newMask & ~MODULE_TXZ_MASK;
				return newMask;
			}
			try {
				Boolean disable = checkDisableScene(txzScene.strVoiceData);
				if (disable)
					newMask = newMask & ~MODULE_TXZ_MASK;
			} catch (JSONException e) {
				if (mEnableTxzNlpScene != 0)
					newMask = newMask & ~MODULE_TXZ_MASK;
			}
		}
		if (!TextUtils.isEmpty(mTxzScene.strText)) {
			if (mServerRegexps != null && mServerRegexps.size() > 0) {
				for (String key : mServerRegexps) {
					if (mTxzScene.strText.matches(key)) {
						mTxzScene.floatTextScore -= TEXT_SCORE_NOCONFIDENCE_MINUS;
						newMask = newMask | MODULE_TXZ_MASK;
					}
				}
			}
		}
		return newMask;
	}
	
	private boolean useYZSScene(VoiceParseData mParseData){
		boolean useYZSScene = false;
		try {
			useYZSScene = checkDisableScene(mParseData.strVoiceData);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (mServerRegexps != null && mServerRegexps.size() > 0) {
			for (String key : mServerRegexps) {
				if (mParseData.strText.matches(key)) {
					mParseData.floatTextScore -= TEXT_SCORE_NOCONFIDENCE_MINUS;
					useYZSScene = false;
				}
			}
		}
		
		return useYZSScene;
	}
	
	private boolean checkDisableScene(String data) throws JSONException{
		JSONObject object = new JSONObject(data);
		String scene = object.getString("scene");
		JNIHelper.logd("first scene=" + scene);
		Boolean disable = null;
		if (scene.equals("music")) {
			disable = checkDisableScene(UiEquipment.CONTROL_MUSIC);
		} else if (scene.equals("call")) {
			disable = checkDisableScene(UiEquipment.CONTROL_CALL);
		} else if (scene.equals("location")) {
			disable = checkDisableScene(UiEquipment.CONTROL_CUR_POS);
		} else if (scene.equals("query")) {
			disable = checkDisableScene(UiEquipment.CONTROL_CUSTOM);
		} else if (scene.equals("radio")) {
			disable = checkDisableScene(UiEquipment.CONTROL_FM);
		} else if (scene.equals("unknown")) {
//			if (object.has("answer"))
			disable = checkDisableChatScene(object);
		} else if (scene.equals("help")) {
			disable = checkDisableScene(UiEquipment.CONTROL_HELP);
		} else if (scene.equals("movie")) {
			disable = checkDisableScene(UiEquipment.CONTROL_MOVIE);
		} else if (scene.equals("nav")) {
			disable = checkDisableScene(UiEquipment.CONTROL_NAVI);
		} else if (scene.equals("app")) {
			disable = checkDisableScene(UiEquipment.CONTROL_OPEN);
		} else if (scene.equals("audio")) {
			disable = checkDisableScene(UiEquipment.CONTROL_RADIO);
		} else if (scene.equals("stock")) {
			disable = checkDisableScene(UiEquipment.CONTROL_STOCK);
		} else if (scene.equals("traffic")) {
			disable = checkDisableScene(UiEquipment.CONTROL_TRAFFIC_COND);
		} else if (scene.equals("limit_number")) {
			disable = checkDisableScene(UiEquipment.CONTROL_TRAFFIC_LIMT);
		} else if (scene.equals("wechat")) {
			disable = checkDisableScene(UiEquipment.CONTROL_WE_CHAT);
		} else if (scene.equals("weather")) {
			disable = checkDisableScene(UiEquipment.CONTROL_WEATHER);
		}else if (mEnableTxzNlpScene != 0) {
			disable = true;
		}
		if (disable == null){
			disable = false;
		}else if (disable == false) {
			mFastScore++;
		}
		return disable;
	}

	private boolean checkDisableScene(int scene) {
		boolean disable = false;
		if ((mEnableTxzNlpScene & scene) == 0 && mEnableTxzNlpScene != 0)
			disable = true;
		else if ((mDisableTxzNlpScene & scene) != 0)
			disable = true;
		return disable;
	}
	
	private boolean checkDisableChatScene(JSONObject object){
		//默认使用不识别场景的
		boolean ret = checkDisableChatScene(UiEquipment.CHAT_TYPE_NONE_SENSE);
		//聊天的白名单开启了
		if (!checkDisableScene(UiEquipment.CONTROL_CHAT)) {
			if (object.has("style")) {
				String mStyle;
				try {
					mStyle = object.getString("style");
					if (mStyle.equals("joke")) {//笑话
						ret = checkDisableChatScene(UiEquipment.CHAT_TYPE_JOKE);
					}else if (mStyle.equals("calculator")) {//计算
						ret = checkDisableChatScene(UiEquipment.CHAT_TYPE_CACULATION);
					}else if (mStyle.equals("calendar")) {//日历
						ret = checkDisableChatScene(UiEquipment.CHAT_TYPE_CALENDAR);
					}else if (mStyle.equals("baike")) {//百科
						ret = checkDisableChatScene(UiEquipment.CHAT_TYPE_BAIKE);
					}else if (mStyle.equals("story")) {//故事
						ret = checkDisableChatScene(UiEquipment.CHAT_TYPE_STORY);
					}else if (mStyle.equals("translation")) {//翻译
						ret = checkDisableChatScene(UiEquipment.CHAT_TYPE_TRANSLATION);
					}else if (mStyle.equals("cookbook")) {//菜谱
						ret = checkDisableChatScene(UiEquipment.CHAT_TYPE_COOK_BOOK);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
	
	private boolean checkDisableChatScene(int scene) {
		boolean disable = false;
		if ((mEnableTxzNlpChatScene & scene) == 0 && mEnableTxzNlpChatScene != 0)
			disable = true;
		else if ((mDisableTxzNlpChatScene & scene) != 0)
			disable = true;
		return disable;
	}

	private void setMaxCountByMask(int mask) {
		for (int i = 0; i < mModuleMaxCount.length; i++)
			mModuleMaxCount[i] = 0;
		for (int i = 0; i < mTexts.size(); i++) {
			if (((0x1 << i) & mask) != 0) {
				mModuleMaxCount[mTexts.get(i).getPriority()]++;
			}
		}
	}

	class TextResultCallBack extends ITextCallBack {
		@Override
		public void onResult(VoiceParseData dataResult, int priority) {
			synchronized (ParserTask.class) {
				if (null != mCurrTask && null != mCurrTask.callBack) {
					mCurrTask.callBack.onResult(dataResult,priority);
				}
				mCurrTask = null;
				parseNext();
			}
		}

		@Override
		public void onError(int errorCode, int priority) {
			synchronized (ParserTask.class) {
				if (null != mCurrTask && null != mCurrTask.callBack) {
					mCurrTask.callBack.onError(errorCode);
				}
				mCurrTask = null;
				parseNext();
			}
		}
	}

	class VoiceDataResultCallBack extends ITextCallBack {
		@Override
		public void onResult(VoiceParseData dataResult, int priority) {
			JNIHelper.logd("onOnlineResult");
			Message message = Message.obtain();
			message.what = ONLINE_SUCCESS;
			message.obj = dataResult;
			message.arg2 = priority;
			mHandler.sendMessage(message);
		}

		@Override
		public void onError(int errorCode, int priority) {
			JNIHelper.logd("onError" + errorCode);
			Message message = Message.obtain();
			message.what = ONLINE_ERROR;
			message.arg1 = errorCode;
			message.arg2 = priority;
			mHandler.sendMessage(message);
		}

		@Override
		public void onResult(byte result[], int priority) {
			try {
				JNIHelper.logd("onLocalResult");
				TestResp resp = TestResp.parseFrom(result);
				Message message = Message.obtain();
				message.obj = resp;
				message.what = LOCAL;
				message.arg2 = priority;
				mHandler.sendMessage(message);
			} catch (InvalidProtocolBufferNanoException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleMsg(Message msg) {
		JNIHelper.logd("msg=" + msg.what + ",mEndParse=" + mEndParse);
		if (mEndParse) {
			return;
		}
		mModuleCount[msg.arg2]++;
		switch (msg.what) {
		case ONLINE_ERROR:
			break;
		case ONLINE_SUCCESS:
			compareJson((VoiceParseData) msg.obj, msg.arg2);
			mResult[msg.arg2] = true;
			break;
		case LOCAL:
			TestResp resp = (TestResp) msg.obj;
			if (resp.success) {
				compareResp(resp, msg.arg2);
				mResult[msg.arg2] = true;
			}
			break;
		default:
			break;
		}
		checkOver();
	}

	/**
	 * 把data和存储的data字段进行判断，保存高score
	 * 
	 * @param parseData
	 */
	private void compareResp(TestResp resp, int priority) {
		if (mResp == null) {
			mResp = resp;
			if (mCurrentPriority < priority) {
				mCurrentPriority = priority;
				mType = TYPE_RESP;
			}
			return;
		}
		if (mCurrentPriority > priority)
			return;
		if (mCurrentPriority <= priority) {
			mResp.clear();
			mResp = resp;
			mCurrentPriority = priority;
			mType = TYPE_RESP;
		}
	}

	private void compareJson(VoiceParseData parseData, int priority) {
		if (parseData.floatTextScore < TEXT_SCORE_MIN)
			return;
		if (mTxzScene == null) {
			mTxzScene = parseData;
			if (mCurrentPriority < priority) {
				mCurrentPriority = priority;
				mType = TYPE_JSON;
			}
			JNIHelper.logd("compareJson scene=null,mScore="
					+ mTxzScene.floatTextScore + ",mData="
					+ mTxzScene.strVoiceData + ",priority=" + mCurrentPriority);
			return;
		}
		if (mCurrentPriority > priority)
			return;
		if (mCurrentPriority < priority) {
			mTxzScene.clear();
			mTxzScene = parseData;
			mCurrentPriority = priority;
			mType = TYPE_JSON;
			JNIHelper.logd("compareJson priority more,mScore="
					+ mTxzScene.floatTextScore + ",mData="
					+ mTxzScene.strVoiceData + ",priority=" + mCurrentPriority);
			return;
		}
		JNIHelper.logd("compareJson,score=" + parseData.floatTextScore
				+ ",mScore=" + mTxzScene.floatTextScore + ",newData="
				+ parseData.strVoiceData + ",mData=" + mTxzScene.strVoiceData);
		if (parseData.floatTextScore > mTxzScene.floatTextScore) {
			mTxzScene.clear();
			mTxzScene = parseData;
			mType = TYPE_JSON;
		}else {
			if (parseData.uint32DataType == VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON) {
				if (mTxzScene.uint32DataType == VoiceData.VOICE_DATA_TYPE_TXZ_SENCE_NEW) {
					if (useYZSScene(parseData)) {
						JNIHelper.logd("compareJson useYZSScene");
						mTxzScene.clear();
						mTxzScene = parseData;
						mType = TYPE_JSON;
					}
				}
			}
		}
	}

	public void checkOver() {
		if (mEndParse)
			return;
		for (int i = MAX_LEVEL - 1; i >= 0; i--) {
			if (mModuleMaxCount[i] != 0)
			JNIHelper.logd("nlp:module["+i+"]:("+mModuleMaxCount[i]+")-("+mModuleCount[i]+"):"+mResult[i]);
			if (mModuleCount[i] != mModuleMaxCount[i]) {
				if (mFastParse && mModuleCount[i] != 0 && i == mCurrentPriority
						&& mTxzScene != null){
					if ((mCurrentMask & MODULE_TXZ_MASK) != 0) {
						if ((mTxzScene.uint32DataType == VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON)) {
							if (useYZSScene(mTxzScene)) {
								onJsonResult();
								return;
							}
						}
					}
					if (mTxzScene.floatTextScore >= mFastScore) {
						onJsonResult();
						return;
					} 
				}
				break;
			}
			if (mResult[i]) {
				if (mType == TYPE_RESP) {
					onRespResult();
					return;
				}
				onJsonResult();
				return;
			}
			if (i == 0)
				onJsonResult();
		}
	}

	private void onJsonResult() {
		mEndParse = true;
		cancel();
		handleTxzJson(mTxzScene);
		synchronized (ParserTask.class) {
			mCurrTask = null;
		}
		parseNext();
	}

	private void onRespResult() {
		mEndParse = true;
		cancel();
		handleTxzResp(mResp, mStrText);
		ReportUtil.doReport(new ReportUtil.Report.Builder()
				.setKeywords(TextTxzHighLocalImpl.getRealText(mStrText))
				.setAction("exec").putExtra("scene", "command")
				.putExtra("_rt", "voice").buildVoiceReport());
		synchronized (ParserTask.class) {
			mCurrTask = null;
		}
		parseNext();
	}

	public static int handleTxzResp(TestResp resp, String text) {
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_RECORD_SHOW_USER_TEXT, text);
		JNIHelper.sendEvent(resp.event, resp.subEvent, resp.data);
		return 0;
	}
	private static String getStringJSON(String scene, String action,
			String text) {
		return getStringJSON(scene,action,text,-1);
	}
	private static String getStringJSON(String scene, String action,
			String text,int type) {

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("scene", scene);
			jsonObject.put("action", action);
			jsonObject.put("text", text);
			jsonObject.put("t", type);
		} catch (JSONException e) {
		}
		return jsonObject.toString();
	}
	/**
	 * 处理最后认为可信任的txzScene
	 * 
	 * @param txzScene
	 *            voiceData must be txz json
	 * @return 0
	 */
	public static int handleTxzJson(VoiceParseData txzScene) {
		long costTime = SystemClock.elapsedRealtime() - beginTime;
		if (txzScene == null) {
			txzScene = new VoiceParseData();
			txzScene.strVoiceData = getStringJSON("empty","empty","");
//			JNIHelper.logd("text result cost " + costTime + ",score="
//					+ txzScene.floatTextScore + ",result="
//					+ txzScene.strVoiceData);
//			RecorderWin.setLastUserText(NativeData
//					.getResString("RS_USER_EMPTY_TEXT"));
//			AsrManager.getInstance().setNeedCloseRecord(true);
//			RecorderWin.speakTextWithClose(
//					NativeData.getResString("RS_VOICE_UNKNOW_WITH_BYE"), null);
			MonitorUtil.monitorCumulant("text.handle.E.handleNull");
//			return 0;
		}
		if (txzScene.floatTextScore < TEXT_SCORE_MIN) {
			txzScene.strVoiceData = getStringJSON("unknown","unknown",txzScene.strText);
			MonitorUtil.monitorCumulant("text.handle.E.handleNull");
		}
		try {
			JSONObject root = new JSONObject(txzScene.strVoiceData);
			String scene = root.getString("scene");
			String action = root.getString("action");
			if (scene != null && action != null) {
				txzScene.uint32LastDataType = txzScene.uint32DataType;
				txzScene.uint32DataType = VoiceData.VOICE_DATA_TYPE_TXZ_SENCE_NEW;
			}
	//		 JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_VOICE,
	//		 VoiceData.SUBEVENT_VOICE_PARSE, txzScene);
	
			JNIHelper.logd("text result cost " + costTime + ",score="
					+ txzScene.floatTextScore + ",result=" + txzScene.strVoiceData);
			if (!root.has("text")) {
				root.put("text", txzScene.strText);
			}
			root.put("t", txzScene.uint32LastDataType);
			final JSONObject jsonReport = new JSONObject(txzScene.strVoiceData);
			if (VoiceData.VOICE_DATA_TYPE_TXZ_SENCE_NEW == txzScene.uint32DataType) {
	
				if ("call".equals(jsonReport.getString("scene"))) {
					jsonReport.remove("name");
					jsonReport.remove("list");
					jsonReport.remove("number");
				} else if ("wechat".equals(jsonReport.getString("scene"))) {
					jsonReport.remove("keywords");
				}
				jsonReport.put("_rt", "voice");
				jsonReport.put("t", txzScene.uint32LastDataType);
				jsonReport.put("taskID", txzScene.uint64VoiceFileId+"");
				jsonReport.put("dataType", txzScene.uint32DataType);
			}
//			final int type = txzScene.uint32DataType;
			ReportUtil.doReport(new Report() {
	
				@Override
				public int getType() {
					return ReportManager.UAT_VOICE;
				}
	
				@Override
				public String getData() {
					return jsonReport.toString();
				}
			});
			txzScene.strVoiceData = root.toString();
		} catch (JSONException e) {
			JNIHelper.logd("json err="+e.getMessage());
			JNIHelper.logd("text result cost " + costTime + ",score="
					+ txzScene.floatTextScore + ",result="
					+ txzScene.strVoiceData);
			txzScene.strVoiceData = getStringJSON("unknown","unknown",txzScene.strText,txzScene.uint32DataType);
			MonitorUtil.monitorCumulant("text.handle.E.handleNull");
		}
		TextSemanticAnalysis.getInstance().parse(txzScene);

		return 0;
	}

	public void cancel() {
		if (!mInited)
			return;
		mEndParse = true;
		synchronized (ParserTask.class) {
			mCurrTask = null;
		}
		JNIHelper.logd("cancel!");
		for (int i = 0; i < mTexts.size(); i++) {
			mTexts.get(i).cancel();
		}
		if (mHandler != null) {
			mHandler.removeMessages(LOCAL);
			mHandler.removeMessages(ONLINE_ERROR);
			mHandler.removeMessages(ONLINE_SUCCESS);
		}
	}

	public String getShowText(String text) {
		String showText = text;
		HashMap<String, String> afterRegexps = getAfterRegexps();
		if (afterRegexps != null && afterRegexps.size() > 0 ) {
			for(String key : afterRegexps.keySet()){
				String value = afterRegexps.get(key);
				showText = showText.replaceAll(key, value);
			}
		}
		return showText;
	}
	public void onDismiss() {
		if (mDismissRunnable != null)
			mDismissRunnable.run();
	}
	public void handleTxzPushNlp(String text,String json,boolean interrupt) {
		final Runnable3<String, String,Boolean> startrRunnable = new Runnable3<String, String,Boolean>(text,json,interrupt) {
			@Override
			public void run() {
				if (RecorderWin.isOpened() && !mP3) {
					return ;
				}
				mDismissRunnable = null;
				String json = mP2;
				String text = mP1;
				RecorderWin.show();
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.setState(STATE.STATE_PROCESSING);
				if (!TextUtils.isEmpty(json) && json.contains("scene") && json.contains("action")) {
					VoiceParseData parseData = new VoiceParseData();
					parseData.uint32DataType = VoiceData.VOICE_DATA_TYPE_TXZ_SENCE_NEW;
					parseData.strVoiceData = json;
					parseData.uint32Sence = VoiceData.GRAMMAR_SENCE_DEFAULT;
					parseData.strText = text;
					parseData.floatTextScore = (float) 96.0;
					handleTxzJson(parseData);
				} else {
					AsrManager.getInstance().startWithText(text);
				}
			}
		};
		if (RecorderWin.isOpened()) {
			if (interrupt) {
				RecorderWin.cancel();
				startrRunnable.run();
			}
			else {
				mDismissRunnable = new Runnable() {
					
					@Override
					public void run() {
						AppLogic.runOnBackGround(startrRunnable, 200);
					}
				};
			}
		}
		else {
			if (TtsManager.getInstance().isBusy())
				TtsManager.getInstance().speakText("", PreemptType.PREEMPT_TYPE_IMMEADIATELY);
			startrRunnable.run();
		}
	}
	
	public String getParseText() {
		return mStrText;
	}
	
	public byte[] invokeCommTextTest(final String packageName, String command,
			byte[] data) {
		String debug = Environment.getExternalStorageDirectory().getPath()
				+ "/txz/debug_text_test.txt";
		try {
			String number = new String(data);
			int lineNumber = 0;
			boolean interrupt = false;
			if (!number.isEmpty()) {
				if (number.charAt(0) == 'e') {
					lineNumber = Integer.valueOf(number.substring(1));
					mEnableTxzNlpScene = lineNumber;
					return null;
				}
				if (number.charAt(0) == 'd') {
					lineNumber = Integer.valueOf(number.substring(1));
					mDisableTxzNlpScene = lineNumber;
					return null;
				}
				lineNumber = Integer.valueOf(number);
				JNIHelper.logd("number=" + lineNumber);
				if (lineNumber == -1) {
					mTxzNlpLevel = UiEquipment.NLP_LEVEL_NORMAL;
					if (mTexts != null && mTexts.size() >= 4)
						mTexts.get(3).setPriority(mTxzNlpLevel);
					mOpenTxzNlp = false;
				} else if (lineNumber == -2) {
					mTxzNlpLevel = UiEquipment.NLP_LEVEL_GOD;
					if (mTexts != null && mTexts.size() >= 4)
						mTexts.get(3).setPriority(mTxzNlpLevel);
					mOpenTxzNlp = true;
				}
				if (lineNumber < 0 || lineNumber > 1000)
					lineNumber = 0;
			}
			interrupt = lineNumber % 2 == 0;
			File file = new File(debug);
			if (!file.exists())
				return null;
			FileInputStream fileInputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(
					fileInputStream);
			BufferedReader reader = new BufferedReader(inputStreamReader);
			String lineString = null;
			do {
				lineString = reader.readLine();
			} while (lineNumber-- > 0);
			JNIHelper.logd("line=" + lineString);
			reader.close();
			inputStreamReader.close();
			fileInputStream.close();
			if (lineString != null && lineString.contains("scene")
					&& lineString.contains("action"))
				handleTxzPushNlp("测试", lineString, interrupt);
			else
				handleTxzPushNlp(lineString, "", interrupt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

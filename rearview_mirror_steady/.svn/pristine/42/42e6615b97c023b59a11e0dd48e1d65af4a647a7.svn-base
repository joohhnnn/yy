package com.txznet.sdk;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ConnectionListener;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.udprpc.TXZUdpClient;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.ConfigUtil.ConfigListener;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.version.TXZVersion;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZNavManager.NavToolType;
import com.txznet.sdk.TXZService.CommandProcessor;
import com.txznet.sdkinner.TXZInnerUpgradeManager;
import android.util.Base64;

/**
 * 类名称：语音配置管理器
 * 类描述：同行者主要参数控制类，主要包含语音运行相关参数及语音初始化方法。
 * 		   本类是语音SDK调用的主要入口，初始化方法为TXZConfigManager.initialize()
 */
public class TXZConfigManager {
	/**
	 * 常量名：最大唤醒词数量
	 * 常量描述：可以对引擎设置的最大唤醒数量，最大为10个
	 */
	public static final int MAX_WAKEUP_KEYWORDS_COUNT = 10;
	/**
	 * 常量名：不设置回声消除参数
	 * 常量描述：回声消除参数之一，不开启回声消除功能，则不支持语音打断功能
	 */
	public static final int AEC_TYPE_NONE = 0;
	/**
	 * 常量名：右参考回声消除参数
	 * 常量描述：回声消除参数之一，使用立体声的右声道为回路参考信息，开启回声消除功能
	 */
	public static final int AEC_TYPE_STERO_COMPARE_WITH_RIGHT = 1;
	/**
	 * 常量名：外部回声消除参数
	 * 常量描述：回声消除参数之一，默认外部提供录音已完成回声消除，直接开启打断相关功能
	 */
	public static final int AEC_TYPE_MONO_COMPARE_WITH_UDP = 2;
	/**
	 * 常量名：左参考回声消除参数
	 * 常量描述：回声消除参数之一，使用立体声的左声道为回路参考信息，开启回声消除功能
	 */
	public static final int AEC_TYPE_STERO_COMPARE_WITH_LEFT = 3;
	/**
	 * 常量名：回声消除参数
	 * 常量描述：回声消除参数之一，使用立体声的左声道为回路参考信息，开启回声消除功能
	 */
	public static final int AEC_TYPE_MONO_BY_INNER = 4;
	/**
	 * 常量名：同行者外部录音源特定参数
	 * 常量描述：外部录音源参数，针对特定方案商制定的参数
	 */
    public static final int EXT_AUDIOSOURCE_TYPE_MSD = 0;
	/**
	 * 常量名：同行者外部录音源参数
	 * 常量描述：外部录音源参数，当车机无法使用安卓标准接口录音时，通过setExtAudioSourceType()启用外部录音
	 * 			需要使用此方法时，请联系同行者对应支持人员
	 */
    public static final int EXT_AUDIOSOURCE_TYPE_TXZ = 1;
	/**
	 * 常量名：语音悬浮图标默认位置参数
	 * 常量描述：语音悬浮图标默认位置参数，上部
	 */
    public static final int FT_POSITION_TOP = -1;
	/**
	 * 常量名：语音悬浮图标默认位置参数
	 * 常量描述：语音悬浮图标默认位置参数，中间
	 */
    public static final int FT_POSITION_MIDDLE = -2;
	/**
	 * 常量名：语音悬浮图标默认位置参数
	 * 常量描述：语音悬浮图标默认位置参数，底部
	 */
    public static final int FT_POSITION_BOTTOM = -3;
	/**
	 * 常量名：语音悬浮图标默认位置参数
	 * 常量描述：语音悬浮图标默认位置参数，左侧
	 */
    public static final int FT_POSITION_LEFT = -1;
	/**
	 * 常量名：语音悬浮图标默认位置参数
	 * 常量描述：语音悬浮图标默认位置参数，右侧
	 */
    public static final int FT_POSITION_RIGHT = -3;
	/**
	 * 常量名：FM发射延时参数
	 * 常量描述：原车开启FM发射时，由于车机端发声和原车功放端发声有时差，需要调整回声消除时延，依据不同FM芯片有区别
	 */
    public static final String FME_DELAY = "FMEDelay";
	/**
	 * 常量名：FM发射延时功能启用参数
	 * 常量描述：原车开启FM发射时，由于车机端发声和原车功放端发声有时差，需要开启此功能，并调整时延参数
	 */
	public static final String FME_ENABLE = "FMEEnable";
	/**
	 * 常量名：TTS防误打断功能参数
	 * 常量描述：回声消除功能欠优时，通过initParam.enableProtectWakeup开启防误打断功能
	 */
	public static final String HAS_REF= "HasRefSignal";
	/**
	 * 常量名：语音内存优化配置，不开启
	 * 常量描述：语音内存优化配置，不开启内存优化功能
	 */
	public static final int MEM_MODE_NONE = 0;
	/**
	 * 常量名：语音内存优化配置，开启预编译
	 * 常量描述：语音内存优化配置，开启预编译唤醒词方式优化内存占用
	 */
	public static final int MEM_MODE_PREBUILD = 1;//预编译唤醒词方式优化内存占用
	/**
	 * 常量名：语音内存优化默认配置，开启合并进程
	 * 常量描述：语音内存优化配置，开启合并进程方式优化内存占用，默认开启此配置
	 */
	public static final int MEM_MODE_PREBUILD_MERGE = 2;//合并进程方式优化内存占用

	private static TXZConfigManager sInstance = new TXZConfigManager();

	private TXZConfigManager() {
	}

	/**
	 * 获取单例
	 *
	 * @return 类实例
	 */
	public static TXZConfigManager getInstance() {
		return sInstance;
	}

	void reconnectOtherModule() {
		if (mInitedSuccess == null || mInitedSuccess == false)
			return;
		if (mEnableWakeup != null) {
			enableWakeup(mEnableWakeup);
		}
		if (mEnableChangeWakeupKeywords != null) {
			enableChangeWakeupKeywords(mEnableChangeWakeupKeywords);
		}
		if (mFinishDelay != 0) {
			setPoiSearchActivityFinishDelay(mFinishDelay);
		}
		if (mStartNavDelayFinish >= 0) {
			setPoiSearchActivityStartNavFinishDelay(mStartNavDelayFinish);
		}
		if (mDismissDelay != 0) {
			setConfirAsrWinDismissDelay(mDismissDelay);
		}
		if (mShowCount > 0) {
//			setDisplayLvCount(mShowCount);
			setPoiSearchCount(mShowCount);
		}
		if (mBanSelectAsr != null) {
			setBanSelectListAsr(mBanSelectAsr);
		}
		if (mPagingCount > 0) {
			setPagingBenchmarkCount(mPagingCount);
		}
		if (mMoviePagingCount > 0) {
			setMoviePagingBenchmarkCount(mMoviePagingCount);
		}

		if (!TextUtils.isEmpty(mVersionConfig)) {
			setVersionConfig(mVersionConfig);
		}
		if (this.keys != null) {
			try {
				setPreferenceConfig(configs, keys);
			} catch (IllegalAccessException e) {
				LogUtil.loge(e.toString());
			}
		}
		if(mEnableCoverDefaultKeywords!=null){
			enableCoverDefaultKeywords(mEnableCoverDefaultKeywords);
		}
		if (mEnableWinAnim != null) {
			enableWinAnim(mEnableWinAnim);
		}
		for (Entry<String, Integer> entry : mLogLevelMap.entrySet()) {
			setLogLevel(entry.getKey(), entry.getValue());
		}
		for (Entry<String, Integer> entry : mFileLogLevelMap.entrySet()) {
			setFileLogLevel(entry.getKey(), entry.getValue());
		}

		disableChangeWakeupKeywordsStyle(null);
		if (mSetStyleBindWithWakeupKeywords != null) {
			setStyleBindWithWakeupKeywords(mSetStyleBindWithWakeupKeywords);
		}

		if (mMaxEmpty != null) {
			setChatMaxEmpty(mMaxEmpty);
		}

		if (mMaxUnknow != null) {
			setChatMaxUnknow(mMaxUnknow);
		}

		if(mHideSettingOptions != null){
			hideSettingOptions(mHideSettingOptions%2==1, (mHideSettingOptions/2)%2==1, (mHideSettingOptions/4)%2==1,
					(mHideSettingOptions/8)%2==1, (mHideSettingOptions/16)%2==1, (mHideSettingOptions/32)%2==1);
		}

		if(mSettingWkWordsEditable != null){
			enableSettingWkWordsEditable(mSettingWkWordsEditable);
		}
		if (!mEnableRecording) {
			setEnableRecording(mEnableRecording);
		}

		if (mEnableQueryTicket != null){
			enableQueryTrafficTicket(mEnableQueryTicket);
		}

		if(mInterruptText != null){
			setInterruptTips(mInterruptText);
		}

		if (mInterruptTextArr != null) {
			setInterruptTips(mInterruptTextArr);
		}

		if (mEnableInterruptTips != null) {
			enableInterruptTips(mEnableInterruptTips);
		}

		if (mNeedHelpFloat != null) {
			setNeedHelpFloat(mNeedHelpFloat);
		}

		if (isNeedGuideAnim != null) {
			setIsNeedGuideAnim(isNeedGuideAnim);
		}

		if (isNeedSearchTts != null) {
			setNeedBlockSearchTipTts(isNeedSearchTts);
		}

		for (Map.Entry<PageType,Integer> entry: mPagingCountMap.entrySet()) {
			setPagingBenchmarkCount(entry.getKey(), entry.getValue());
		}
		for (Map.Entry<PageType,Long> entry: mPagingTimeoutMap.entrySet()) {
			setPageTimeout(entry.getKey(), entry.getValue());
		}
		if (mNotifyOnWakeupFlags != null) {
			setNotifyOnWakeupFlags(mNotifyOnWakeupFlags);
		}

		if (mEnableShowHelpQRCode != null ) {
			setEnableShowHelpQRCode(mEnableShowHelpQRCode);
		}

		if ( mNeedSetIntentPackage != null ) {
			setNeedSetIntentPackage(mNeedSetIntentPackage);
		}

		if ( mNeedShowOfflinePromote != null ) {
			setNeedShowOfflinePromote(mNeedShowOfflinePromote);
		}

		if (isDisableAutoJust != null && isDisableAutoJust) {
			setFloatViewEnableAutoAdjust();
		}else if(isDisableAutoJust != null && !isDisableAutoJust){
			setFloatViewDisableAutoAdjust();
		}

		ConfigUtil.sendConfigs();
		ConfigUtil.requestSync();
		TXZAsrManager.getInstance().onReconnectTXZ();
		TXZCallManager.getInstance().onReconnectTXZ();
		TXZCameraManager.getInstance().onReconnectTXZ();
		TXZLocationManager.getInstance().onReconnectTXZ();
		TXZMusicManager.getInstance().onReconnectTXZ();
		TXZAudioManager.getInstance().onReconnectTXZ();
		TXZNavManager.getInstance().onReconnectTXZ();
		TXZPoiSearchManager.getInstance().onReconnectTXZ();
		TXZSenceManager.getInstance().onReconnectTXZ();
		TXZSceneManager.getInstance().onReconnectTXZ();
		TXZStatusManager.getInstance().onReconnectTXZ();
		TXZTtsManager.getInstance().onReconnectTXZ();
		TXZResourceManager.getInstance().onReconnectTXZ();
		TXZSysManager.getInstance().onReconnectTXZ();
		AsrUtil.regCmdAgain();
		TXZPowerManager.getInstance().onReconnectTXZ();
		TXZWechatManager.getInstance().onReconnectTXZ();
		TXZWechatManagerV2.getInstance().onReconnectTXZ();
		TXZAsrKeyManager.getInstance().onReconnectTXZ();
		TXZRecordWinManager.getInstance().onReconnectTXZ();
		TXZSimManager.getInstance().onReconnectTXZ();
		TXZWheelControlManager.getInstance().onReconnectTXZ();
		TXZCarControlManager.getInstance().onReconnectTXZ();
		TXZUpgradeManager.getInstance().onReconnectTXZ();
		TXZReminderManager.getInstance().onReconnectTXZ();
		TXZConstellationManager.getInstance().onReconnectTXZ();
		TXZWeatherManager.getInstance().onReconnectTXZ();
		TXZDownloadManager.getInstance().onReconnectTXZ();
		TXZInnerUpgradeManager.getInstance().onReconnectTXZ();
		TXZCarControlHomeManager.getInstance().onReconnectTXZ();
		TXZMovieManager.getInstance().onReconnectTXZ();
		TXZTicketManager.getInstance().onReconnectTXZ();
		TXZStockManager.getInstance().onReconnectTXZ();
		TXZTtsPlayerManager.getInstance().onReconnectTXZ();
		TXZVoiceprintRecognitionManager.getInstance().onReconnectTXZ();
		TXZNetDataProvider.getInstance().onReconnectTXZ();


		if(AppLogicBase.getInstance()!=null){
			AppLogicBase.getInstance().reBindCore();
		}
	}

	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	void onReconnectTXZ() {
		initializeSDK();
	}

	/**
	 * 枚举类名：语音引擎类型
	 * 枚举类描述：默认TTS为云知声引擎
	 */
	public enum TtsEngineType {
		/**
		 * 不使用语音引擎
		 */
		NONE,
		/**
		 * 云知声
		 */
		TTS_YUNZHISHENG,
		/**
		 * Android系统
		 */
		TTS_SYSTEM
	}

	/**
	 * 枚举类名：识别引擎类型
	 * 枚举类描述：引擎识别类型
	 */
	public enum AsrEngineType {
		/**
		 * 不使用声控引擎
		 */
		NONE,
		/**
		 * 云知声
		 */
		ASR_YUNZHISHENG
	}

	/**
	 * 枚举类名：识别模式设置
	 * 枚举类描述：识别模式类型
	 */
	public static enum AsrMode {
		/**
		 * 聊天模式，根据场景来停止对话，没有说话或者连续2次识别不到时停止
		 */
		ASR_MODE_CHAT,
		/**
		 * 单次识别模式
		 */
		ASR_MODE_SINGLE,
		/**
		 * 连续识别模式，直到发起结束场景，如导航、电话、音乐
		 */
		ASR_MODE_CONTINUE,
	}

	/**
	 * 枚举类名：识别服务模式设置
	 * 枚举类描述：识别服务模式类型
	 */
	public static enum AsrServiceMode {
		/**
		 * 混合识别模式，默认模式
		 */
		ASR_SVR_MODE_MIX,
		/**
		 * 纯离线识别模式，仅使用离线语义
		 */
		ASR_SVR_MODE_LOCAL,
		/**
		 * 纯在线识别模式，仅使用在线语义
		 */
		ASR_SVR_MODE_NET,
		/**
		 * 智能模式。能上网的情况下，只使用在线识别，降低CPU占用
		 */
		ASR_SVR_MODE_AUTO
	}

	/**
	 * 枚举类名：悬浮工具类型
	 * 枚举类描述：控制语音悬浮图标显示层级
	 */
	public enum FloatToolType {
		/**
		 * 永久置顶，默认
		 */
		FLOAT_TOP,
		/**
		 * 普通置顶
		 */
		FLOAT_NORMAL,
		/**
		 * 无悬浮工具
		 */
		FLOAT_NONE
	}

	/**
	 * 枚举类名：配置项json key
	 * 枚举类描述：通过JSON配置部分语音功能
	 */
	public static enum ConfigJsonKey {
		/**
		 * 是否需要Poi展示地图，Boolean类型
		 */
		needPoiMap,
		/**
		 * 是否打开并自动播放酷我音乐，Boolean类型
		 */
		autoPlayKuwo,
		/**
		 * 阈值，Float类型
		 */
		asrThreshold,
		/**
		 * 定位切换为GPS定位，Boolean类型
		 */
		changeGpsStyle,
		/**
		 * 是否需要reset MediaPlayer，Boolean类型
		 */
		needResetWav,
		/**
		 * 是否是WindowManager展示对话框，Boolean类型
		 */
		showOnWindowManager,
		/**
		 * 对话框显示的层级数值，Int类型
		 */
		wmType
	}

	/**
	 * 枚举类名：语音打断模式
	 * 枚举类描述：打断，即在语音界面播报时，可随时发出新的语音指令（非聊天），在识别到新的指令后停止播报并进入到新指令的处理状态。
	 */
	public static enum InterruptMode {
		/**
		 * 普通打断模式：播报中仅支持唤醒词打断，默认值
		 */
		INTERRUPT_MODE_DEFAULT,
		/**
		 * 后置打断模式：播报中识别到具体指令后打断播报，聊天语句不打断，对降噪要求较高
		 */
		INTERRUPT_MODE_ORDER
	}

	/**
	 * 类名：初始化参数
	 * 类描述：初始化参数入口类，包含初始化相关参数配置
	 */
	public static class InitParam {
		/**
		 * 变量名：语音激活ID
		 * 变量描述：语音激活ID，需要由同行者提供
		 */
		String appId = null;
		/**
		 * 变量名：语音激活TOKEN校验码
		 * 变量描述：语音激活TOKEN校验码，需要由同行者提供
		 */
		String appToken = null;
		/**
		 * 变量名：语音自定义ID
		 * 变量描述：语音自定义ID，会在激活时检验，方便对同一APPID的用户作区分，用户自行选择使用
		 */
		String appCustomId = null;
		/**
		 * 变量名：语音自定义唯一码
		 * 变量描述：自定义唯一码，确认机器在绝大数正常使用情况下不会更换，包括重启、升级、升级等操作，确保用户唯一
		 */
		String uuid = null;
		/**
		 * 变量名：格式保留区
		 * 变量描述：系统格式化时，不会格式化的指定路径，方便存储部分数据，避免刷机后出现数据丢失情况
		 */
		String neverFormatRoot = null;
		/**
		 * 变量名：TTS引擎类型
		 * 变量描述：TTS引擎类型配置
		 */
		TtsEngineType ttsType = null;
		/**
		 * 变量名：识别引擎类型
		 * 变量描述：识别引擎类型配置
		 */
		AsrEngineType asrType = null;
		/**
		 * 变量名：语音图标层级
		 * 变量描述：语音图标层级配置
		 */
		FloatToolType ftType = null;
		/**
		 * 变量名：语音图标图标文件路径，常态
		 * 变量描述：语音图标默认图标需要更换时，传入图片路径，常态图片
		 */
		String ftUrl_N = null;
		/**
		 * 变量名：语音图标图标文件路径，点击态
		 * 变量描述：语音图标默认图标需要更换时，传入图片路径，点击态图片
		 */
		String ftUrl_P = null;
		/**
		 * 变量名：语音图标点击有效间隔
		 * 变量描述：语音图标点击响应间隔时间，默认间隔0，单位：ms
		 */
		Long ftInterval = null;
		/**
		 * 变量名：语音图标显示区域X
		 * 变量描述：语音图标大小显示位置X
		 */
		Integer ftX = null;
		/**
		 * 变量名：语音图标显示区域Y
		 * 变量描述：语音图标大小显示位置Y
		 */
		Integer ftY = null;
		/**
		 * 变量名：语音唤醒词
		 * 变量描述：语音唤醒词数组
		 */
		String[] wakeupKeywords = null;
		/**
		 * 变量名：语音免唤醒词灵敏度JSON
		 * 变量描述：语音免唤醒词灵敏度JSON，需要调整免唤醒灵敏度时设置
		 */
		String jsonScoreKws = null;
		/**
		 * 变量名：是否开启免唤醒
		 * 变量描述：默认开启，免唤醒由两部分控制，sdk只可控制开关，具体唤醒词
		 * 			由同行者后台下发，sdk关闭免唤醒可屏蔽后台配置的唤醒词，
		 */
		Boolean enableInstantAsr = null;
//		String[] instantAsrKeywords = null; // 免唤醒命令字
		/**
		 * 变量名：是否启用内置服务号联系人
		 * 变量描述：启用内置服务号联系人， 中国移动、中国联通等固定常用号码
		 */
		Boolean enableServiceContact = null;
		/**
		 * 变量名：是否固定功能调用
		 * 变量描述：当设置了对应工具时，功能调用为外部功能，默认启用
		 */
		Boolean fixCallFunction = null;
		/**
		 * 变量名：默认导航工具
		 * 变量描述：同行者根据当前已安装导航自适应导航工具
		 */
		String defaultNavTool = null;
		/**
		 * 变量名：识别模式
		 * 变量描述：识别模式配置
		 */
		AsrMode asrMode = null;
		/**
		 * 变量名：是否启用场景限定
		 * 变量描述：启用场景限定，对语义结果有影响，默认不启用
		 */
		Boolean coexistAsrAndWakeup = null;
		/**
		 * 变量名：唤醒词灵敏度
		 * 变量描述：出厂唤醒词灵敏度，-2.0 到-5.0之间，越大越灵敏
		 */
		Float wakeupThreshHold = null;
		/**
		 * 变量名：免唤醒命令灵敏度
		 * 变量描述：免唤醒命令灵敏度，0 到-5.0之间，越大越灵敏
		 */
		Float asrWakeupThreshHold = null;
		/**
		 * 变量名：语音Beep音延时
		 * 变量描述：语音录音前Beep音在部分车机切通道时会被漏掉，通过延时处理
		 */
		Integer beepTimeOut = null;
		/**
		 * 变量名：回声消除类型
		 * 变量描述：回声消除使用类型
		 */
		Integer filterNoiseType = null;
		/**
		 * 变量名：识别模式
		 * 变量描述：识别模式设置
		 */
		AsrServiceMode asrServiceMode = null;
		/**
		 * 变量名：tts播放速度
		 * 变量描述：tts播报速度，20-100，默认70
		 */
		Integer ttsVoiceSpeed = null;
		/**
		 * 变量名：默认配置
		 * 变量描述：可配置唤醒敏感度，语音播报速度，唤醒词
		 * @deprecated
		 */
		String defaultConfig = null;
		/**
		 * 变量名：最大识别录音时间
		 * 变量描述：语音录音识别录音时间,单位毫秒。建议不要低于5000毫秒,不要大于30000毫秒
		 */
		Integer maxAsrRecordTime = null;
		/**
		 * 变量名：是否启用静音Toast
		 * 变量描述：音量低时，是否需要语音弹Toast提示，默认false
		 */
		Boolean zeroVolToast = null;
		/**
		 * 变量名：TTS默认流
		 * 变量描述：TTS播报流，默认STREAM_ALARM
		 */
		Integer txzStream = null;
		/**
		 * 变量名：是否启用外部录音源
		 * 变量描述：是否需要通过外部录音进行识别，不采用安卓标准
		 */
		Boolean useExternalAudioSource = null;
		/**
		 * 变量名：是否启用唤醒录音循环写入
		 * 变量描述：设置是否将唤醒的录音循环写入一个文件
		 */
		Boolean enableBlackHole = null;
		/**
		 * 变量名：当播报TTS时是否关闭唤醒
		 * 变量描述：设置开启回声消除的时候, 是否停止唤醒，防误打断
		 */
		Boolean forceStopWkWhenTts = null;
		/**
		 * 变量名：开启保护唤醒引擎
		 * 变量描述：设置开启保护唤醒引擎，防止回音消除不好造成的误打断
		 * 			 开启会造成CPU和内存的一定消耗
		 */
		Boolean enableProtectWakeup = null;
		/**
		 * 变量名：设置Android录音类型
		 * 变量描述：设置录音机的AudioSource的类型
		 */
		Integer audioSourceForRecord = null;
		/**
		 * 变量名：设置外部录音类型
		 * 变量描述：使用外部录音时，一般使用EXT_AUDIOSOURCE_TYPE_TXZ
		 */
		Integer extAudioSourceType = null;
		/**
		 * 变量名：是否添加默认音乐类型
		 * 变量描述：是否默认使用语音已适配音乐资源，默认使用
		 */
		Boolean addDefaultMusicType = null;
		/**
		 * 变量名：是否启用唤醒大模型
		 * 变量描述：引擎识别、唤醒模型，默认关闭，开启后会增加CPU和内存消耗，推荐通过调整唤醒词灵敏度
		 */
		Boolean useHQualityWakeupModel = null;
		/**
		 * 变量名：外部录音源包名
		 * 变量描述：使用外部录音源时，需要指定对端包名
		 */
		String extAudioSourcePkg = null;
		/**
		 * 变量名：默认语音界面层级
		 * 变量描述：默认语音界面层，参考WindowManager
		 */
		Integer winType;
		/**
		 * 变量名：语音皮肤包路径
		 * 变量描述：语音默认加载皮肤包路径
		 */
		String resApkPath = null;
		/**
		 * 变量名：是否使用UI1.0
		 * 变量描述：UI1.0是语音低功耗UI，动画少，简洁，占用资源少
		 */
		Boolean forceUseUI1 = null;
		/**
		 * 变量名：语音打断模式
		 * 变量描述：语音默认打断模式，默认普通模式，推荐不修改
		 */
		InterruptMode interruptMode = null;
		/**
		 * 变量名：默认语音界面背景透明度
		 * 变量描述：默认语音界面背景透明度，0-1.0f，参考View透明度
		 */
		Float winBgAlpha = null;
		/**
		 * 变量名：是否在线识别
		 * 变量描述：设置是否在主进程中使用在线识别，默认关闭
		 */
		Boolean useLocalNetAsr = null;
		/**
		 * 变量名：默认语音弹窗关闭超时
		 * 变量描述：设置弹窗的自动取消时间，仅针对导航弹窗，默认不关闭
		 */
		Integer dialogTimeout = null;
		/**
		 * 变量名：设置对话框是否是可撤销的
		 * 变量描述：设置语音界面是否可以撤销
		 */
		Boolean cancelable = null;
		/**
		 * 变量名：悬浮图标宽度
		 * 变量描述：可以设置悬浮图标宽度
		 */
		Integer floatToolWidth = null;
		/**
		 * 变量名：悬浮图标高度
		 * 变量描述：可以设置悬浮图标高度
		 */
		Integer floatToolHeight = null;
		/**
		 * 变量名：语音界面继承类型
		 * 变量描述：设置语音界面的实现类型
		 * 			{@link #WIN_RECORD_IMPL_NORMAL}
		 * 			{@link #WIN_RECORD_IMPL_LOW_MEMORY}
		 * 			{@link #WIN_RECORD_IMPL_ACTIVITY}
		 */
		Integer winRecordImpl = null;
		/**
		 * 变量名：语音外部设置包名
		 * 变量描述：使用外部自定义语音设置时，需要传入包名
		 */
		String settingPackageName;
		/**
		 * 变量名：是否强制全屏
		 * 变量描述：是否启用语音界面强制全屏
		 */
		Boolean enableFullScreen = null;
		/**
		 * 变量名：是否禁用电台抢占收音机
		 * 变量描述：默认启用，禁用后，部分收音机指令会当电台搜索
		 */
		Boolean useRadioAsAudio = null;
		/**
		 * 变量名：设置网络模块
		 * 变量描述：设备拥有的网络模块
		 *          {@link #NET_MOUDLE_NONE}
		 *          {@link #NET_MOUDLE_2G}
		 *          {@link #NET_MOUDLE_3G}
		 *          {@link #NET_MOUDLE_4G}
		 */
		Integer netModule = null;
		/**
		 * 变量名：设置WinMessageDialog弹窗样式
		 * 变量描述：设置WinMessageDialog弹窗样式，例如退出导航弹窗等
		 */
		Integer messageDialogType = null;
		/**
		 * 变量名：是否启用特殊场景防误打断功能
		 * 变量描述：是否针对回声消除不佳设备在“确定”、“取消”等场景下禁用打断功能，优化体验
		 */
		Boolean aecPreventFalseWakeup = null;
		/**
		 * 变量名：配置免唤醒状态ID
		 * 变量描述：针对需要有说话状态回调的useWakeupAsAsr的taskid
		 */
		String needSpeechStateTaskId = null;
		/**
		 * 变量名：内存模式
		 * 变量描述：配置采用何种模式优化内存占用
		 * 			{@link #MEM_MODE_NONE}
		 * 			{@link #MEM_MODE_PREBUILD}
		 * 			{@link #MEM_MODE_PREBUILD_MERGE}
		 */
		Integer memMode = null;
		/**
		 * 变量名：外部自定义命令字备份路径
		 * 变量描述：使用文本指注册指令时，传递文本备份路径
		 */
		String adapterLocalCommandBackupPath = null;
		/**
		 * 变量名：外部自定义命令字读取路径
		 * 变量描述：使用文本指注册指令时，传递文本路径，供语音读取并注册
		 */
		String adapterLocalCommandLoadPath = null;
		/**
		 * 变量名：声扬声纹验证分数
		 * 变量描述：根据不同的项目对声扬声纹验证分数进行调整
		 */
		Double voiceprintRecognitionScore = null;
		/**
		 * 变量名：调用getCoreGroupId接口的包名
		 * 变量描述：可调用getCoreGroupId接口的包名
		 */
		String voiceprintRecognitionPackageName = null;
		/**
		 * 变量名：是否启用打字效果
		 * 变量描述：是否启用文字实时上屏功能，仅支持语音2.8.0以上版本
		 */
		Boolean useTypingEffect = null;
		/**
		 * 变量名：是否点击语音外部可消失
		 * 变量描述：点击语音未显示区域时，关闭语音界面，默认不启用
		 */
		Boolean canceledOnTouchOutside = null;
		/**
		 * 变量名：是否启用点击语音外部时，传递点击事件
		 * 变量描述：点击语音未显示区域时，是否需要将点击事件透传
		 */
		Boolean allowOutSideClickSentToBehind = null;
		/**
		 * 变量名：电台下发功能保存路径
		 * 变量描述：设置同行者后台下发电台指令功能的保存路径
		 */
		String fmNamesPath = null;
		/**
		 * 变量名：硬件参数
		 * 变量描述：使用同行者硬件参数绑定激活时，设置此参数，请先与同行者对应人员确认，否则无法使用
		 */
		byte[] hardwareParams = null;
		/**
		 * 变量名：VIN码
		 * 变量描述：针对提醒事项功能，需要上报字段携带，车架号
		 */
		String vin = null;

		/**
		 * 变量名：启用tts播放器
		 * 变量描述：设置为true代表启用tts播放器功能
		 */
		Boolean enableTtsPlayer = null;


		/**
		 * 变量名：录音机缓冲区大小
		 * 变量描述：针对设备调整录音缓冲区大小
		 */
		Integer recorderBufferSize = null;

		/**
		 * 方法名：语音初始化参数构造
		 * 方法描述：激活语音必备类型，可以设置语音详细参数
		 *
		 * @param appId    语音激活ID，需要由同行者提供
		 * @param appToken 语音激活TOKEN校验码，需要由同行者提供
		 */
		public InitParam(String appId, String appToken) {
			this.appId = appId;
			this.appToken = appToken;
		}

		/**
		 * 方法名：设置接入的appId
		 * 方法描述：设置appId，激活语音必备类型，可以设置语音详细参数
		 *
		 * @param appId 语音激活ID，需要由同行者提供
		 * @return 初始化参数实例
		 */
		public InitParam setAppId(String appId) {
			this.appId = appId;
			return this;
		}

		/**
		 * 方法名：设置接入的appToken
		 * 方法描述：设置appToken，激活语音必备类型，可以设置语音详细参数
		 *
		 * @param appToken 语音激活TOKEN校验码，需要由同行者提供
		 * @return 初始化参数实例
		 */
		public InitParam setAppToken(String appToken) {
			this.appToken = appToken;
			return this;
		}

		/**
		 * 方法名：设置接入的appCustomId
		 * 方法描述：语音自定义ID，会在激活时检验，方便对同一APPID的用户作区分，用户自行选择使用
		 *
		 * @param appCustomId 语音自定义ID
		 * @return 初始化参数实例
		 */
		public InitParam setAppCustomId(String appCustomId) {
			this.appCustomId = appCustomId;
			return this;
		}

		/**
		 * 方法名：强制使用UI1.0
		 * 方法描述：UI1.0是语音低功耗UI，动画少，简洁，占用资源少，默认不使用
		 *
		 * @param use 是否使用
		 * @return 初始化参数实例
		 */
		public InitParam forceUseUI1(boolean use){
			this.forceUseUI1 = use;
			return this;
		}

		/**
		 * 方法名：设置语音界面背景透明度
		 * 方法描述：默认语音界面背景透明度，0-1.0f，参考View透明度
		 *
		 * @param alpha 透明度 ， 1.0  不透明 ， 0.0  完全透明
		 * @return 初始化参数实例
		 */
		public InitParam setWinBgAlpha(float alpha) {
			this.winBgAlpha = alpha;
			return this;
		}

		/**
		 * 方法名：是否设置语音界面为全屏
		 * 方法描述：是否启用语音界面强制全屏，默念不全屏
		 *
		 * @param fullScreen 是否全屏
		 * @return 初始化参数实例
		 */
		public InitParam enableFullScreen(boolean fullScreen){
			this.enableFullScreen = fullScreen;
			return this;
		}

		/**
		 * 方法名：设置设备的uuid
		 * 方法描述：自定义唯一码，确认机器在绝大数正常使用情况下不会更换，包括重启、升级、升级等操作，确保用户唯一，最长128位
		 *
		 * @param uuid 自定义唯一码
		 * @return 初始化参数实例
		 */
		public InitParam setUUID(String uuid) {
			this.uuid = uuid;
			return this;
		}

		/**
		 * 方法名：设置不会格式化的分区的根目录
		 * 方法描述：系统格式化时，不会格式化的指定路径，方便存储部分数据，避免刷机后出现数据丢失情况
		 *
		 * @param root 根目录
		 * @return 初始化参数实例
		 */
		public InitParam setNeverFormatRoot(String root) {
			this.neverFormatRoot = root;
			return this;
		}

		/**
		 * 方法名：设置语音合成引擎类型
		 * 方法描述：TTS引擎类型配置，默认云知声
		 *
		 * @param ttsType 语音合成引擎类型
		 * @return 初始化参数实例
		 */
		public InitParam setTtsType(TtsEngineType ttsType) {
			this.ttsType = ttsType;
			return this;
		}

		/**
		 * 方法名：设置收音机电台表的适配指定路径
		 * 方法描述：设置同行者后台下发电台指令功能的保存路径
		 *
		 * @param url FM电台频道列表文件的URL
		 * @return 初始化参数实例
		 */
		public InitParam setFmNamesPath(String url){
			this.fmNamesPath = url;
			return this;
		}


		/*
		 * 设置语音合成工具
		 */
		// public InitParam setTtsTool(TtsTool tool) {
		// this.ttsTool = tool;
		// this.ttsType = null;
		// if (tool == null) {
		// TXZService.setCommandProcessor("tool.tts.", null);
		// } else {
		// TXZService.setCommandProcessor("tool.tts.",
		// new CommandProcessor() {
		// @Override
		// public byte[] process(String packageName,
		// String command, final byte[] data) {
		// if ("stop".equals(command)) {
		// LogUtil.logd("tts tool cancel");
		// InitParam.this.ttsTool.cancel();
		// return null;
		// }
		// if ("start".equals(command)) {
		// JSONBuilder json = new JSONBuilder(data);
		// int stream = json.getVal("stream",
		// Integer.class,
		// TtsUtil.DEFAULT_TTS_STREAM);
		// String text = json.getVal("text",
		// String.class);
		// LogUtil.logd("tts tool start: stream="
		// + stream + ", text=" + text);
		// InitParam.this.ttsTool.start(stream, text,
		// new TtsCallback() {
		// @Override
		// public void onSuccess() {
		// ServiceManager
		// .getInstance()
		// .sendInvoke(
		// ServiceManager.TXZ,
		// "txz.tool.tts.onSuccess",
		// data, null);
		// }
		//
		// @Override
		// public void onError() {
		// ServiceManager
		// .getInstance()
		// .sendInvoke(
		// ServiceManager.TXZ,
		// "txz.tool.tts.onError",
		// data, null);
		// }
		//
		// @Override
		// public void onCancel() {
		// ServiceManager
		// .getInstance()
		// .sendInvoke(
		// ServiceManager.TXZ,
		// "txz.tool.tts.onCancel",
		// data, null);
		// }
		// });
		// return null;
		// }
		// return null;
		// }
		// });
		// }
		// return this;
		// }

		/**
		 * 方法名：设置语音识别引擎类型
		 * 方法描述：识别引擎类型配置，默认云知声
		 *
		 * @param asrType 语音识别引擎类型
		 * @return 初始化参数实例
		 */
		@Deprecated
		public InitParam setAsrType(AsrEngineType asrType) {
			this.asrType = asrType;
			return this;
		}

		/**
		 * 常量名：默认弹窗样式
		 * 常量描述：默认样式，默认大小
		 */
		public static final int MESSAGE_DIALOG_TYPE_NORMAL = 0;

		/**
		 * 常量名：small弹窗样式
		 * 常量描述：比默认小一些
		 */
		public static final int MESSAGE_DIALOG_TYPE_SMALL = 1;

		/**
		 * 方法名：设置WinMessageDialog弹窗样式
		 * 方法描述：设置WinMessageDialog弹窗样式，例如退出导航弹窗等
		 *
		 * @param type 弹窗样式
		 *             {@link #MESSAGE_DIALOG_TYPE_NORMAL}
		 *             {@link #MESSAGE_DIALOG_TYPE_SMALL}
		 * @return 初始化参数实例
		 */
		public InitParam setMessageDialogType(int type){
			this.messageDialogType = type;
			return this;
		}

		/**
		 * 方法名：设置浮动工具图标类型
		 * 方法描述：语音图标层级配置
		 *
		 * @param ftType 浮动工具图标类型，默认FLOAT_TOP
		 * @return 初始化参数实例
		 */
		public InitParam setFloatToolType(FloatToolType ftType) {
			this.ftType = ftType;
			return this;
		}

		/**
		 * 方法名：设置浮动工具图标的大小
		 * 方法描述：可以设置悬浮图标显示宽度、高度
		 *
		 * @param width  宽度
		 * @param height 高度
		 * @return 初始化参数实例
		 */
		public InitParam setFloatToolSize(int width,int height){
			floatToolWidth = width;
			floatToolHeight = height;
			return this;
		}

		/**
		 * 方法名：设置浮动工具图标的位置
		 * 方法描述：调整图标默认显示位置
		 *
		 * @param x TXZConfigManager.FT_POSITION_LEFT/FT_POSITION_MIDDLE/FT_POSITION_RIGHT
		 * @param y TXZConfigManager.FT_POSITION_BOTTOM/FT_POSITION_MIDDLE/FT_POSITION_TOP
		 * @return 初始化参数实例
		 */
		public InitParam setFloatToolPosition(int x,int y){
			this.ftX = x;
			this.ftY = y;
			return this;
		}

		/**
		 * 方法名：设置浮动工具图标图片
		 * 方法描述：语音图标默认图标需要更换时，传入图片路径以替换
		 *
		 * @param ftUrl_N 普通状态的图片
		 * @param ftUrl_P 按下状态的图片，如果值为null，则与普通状态一致
		 * @return 初始化参数实例
		 */
		public InitParam setFloatToolIcon(String ftUrl_N, String ftUrl_P) {
			this.ftUrl_N = ftUrl_N;
			this.ftUrl_P = ftUrl_P;
			return this;
		}

		/**
		 * 方法名：设置浮动工具点击间隔限制
		 * 方法描述：语音图标点击响应间隔时间，默认间隔0，单位：ms
		 *
		 * @param interval 响应间隔
		 * @return 初始化参数实例
		 */
		public InitParam setFloatToolClickInterval(long interval){
			this.ftInterval = interval;
			return this;
		}

		/**
		 * 方法名：设置对话框是否是可撤销的
		 * 方法描述：设置语音界面是否可以撤销
		 *
		 * @param flag 与Dialog的setCancelable相同
		 * @return 初始化参数实例
		 */
		public InitParam setCancelable(boolean flag){
			cancelable = flag;
			return this;
		}

		/**
		 * 常量名：语音界面实现模式，普通
		 * 常量描述：普通正常单例实现类
		 */
		public static final int WIN_RECORD_IMPL_NORMAL = 1;
		/**
		 * 常量名：语音界面实现模式，低内存
		 * 常量描述：低内存实现类，dismiss时会销毁掉，但Dialog出现有延时
		 */
		public static final int WIN_RECORD_IMPL_LOW_MEMORY = 2;
		/**
		 * 常量名：语音界面实现模式，Activity实现
		 * 常量描述：Activity实现类，语音界面通过activity承载
		 */
		public static final int WIN_RECORD_IMPL_ACTIVITY = 3;
		/**
		 * 常量名：语音界面实现模式，移动
		 * 常量描述：移动语音界面，为了解决他们全屏问题添加的，不是移动项目不要用这个实现类
		 */
		public static final int WIN_RECORD_IMPL_YIDONG = 4;

		/**
		 * 方法名：设置语音界面的实现类型
		 * 方法描述：根据需求改变语音界面的实现类型，节省性能或提升体验
		 *
		 * @param type 实现类型
		 *             {@link #WIN_RECORD_IMPL_NORMAL}
		 *             {@link #WIN_RECORD_IMPL_LOW_MEMORY}
		 *             {@link #WIN_RECORD_IMPL_ACTIVITY}
		 * @return 初始化参数实例
		 */
		public InitParam setWinRecordImpl(int type){
			this.winRecordImpl = type;
			return this;
		}

		/**
		 * 方法名：设置语音唤醒词
		 * 方法描述：设置唤醒词，老的接口已屏蔽，最多设置{@link #MAX_WAKEUP_KEYWORDS_COUNT}个
		 *
		 * @param wakeupKeywords 设置语音唤醒词，默认“你好小踢”
		 * @return 初始化参数实例
		 */
		public InitParam setWakeupKeywordsNew(String... wakeupKeywords) {
			if (wakeupKeywords == null) {
				wakeupKeywords = new String[0];
			}
			Set<String> setKws = new HashSet<String>();
			for (String kw : wakeupKeywords) {
				if (!TextUtils.isEmpty(kw)) {
					setKws.add(kw);
				}
			}
			if (setKws.size() > MAX_WAKEUP_KEYWORDS_COUNT) {
				return this;
			}
			this.wakeupKeywords = setKws.toArray(new String[setKws.size()]);
			return this;
		}

		/**
		 * 方法名：是否启用免唤醒词功能
		 * 方法描述：是否启用免唤醒词功能,默认启用，不推荐关闭
		 *
		 * @param enable 是否启用
		 * @return 初始化参数实例
		 */
		public InitParam setInstantAsrEnabled(boolean enable) {
			this.enableInstantAsr = enable;
			return this;
		}

		/**
		 * 方法名：设置唤醒词阈值
		 * 方法描述：针对唤醒词设置不同的阈值
		 *
		 * @param jsonScoreKws json文本，格式如下，默认-3.1
		 *                     [{"keyWords":"你好小踢","threshold":-3.1},
		 *                     {"keyWords":"小踢你好","threshold":-3.2},
		 *                     {"keyWords":"小踢小踢","threshold":-3.3}]
		 * @return 初始化参数实例
		 */
		public InitParam setWakeupKeyWordsThreshold(String jsonScoreKws){
			if(jsonScoreKws == null){
				jsonScoreKws = "";
			}
			this.jsonScoreKws = jsonScoreKws;
			return this;
		}


		/*
		 * 设置免唤醒命令词
		 * @param keywords
		 * @return
		 */
//		public InitParam setInstantAsrKeywords(String... keywords){
//			if (null == keywords) {
//				keywords = new String[0];
//			}
//			Set<String> setKws = new HashSet<String>();
//			for (String kw : keywords) {
//				if (!TextUtils.isEmpty(kw)) {
//					setKws.add(kw);
//				}
//			}
//			
//			this.instantAsrKeywords = setKws.toArray(new String[setKws.size()]);
//			return this;
//		}

		/**
		 * 方法名：设置是否固定功能调用
		 * 方法描述：当设置了对应工具时，功能调用为外部功能，默认启用
		 *
		 * @param fix 是否固定功能，默认true
		 * @return 初始化参数实例
		 */
		public InitParam setFixCallFunction(boolean fix) {
			this.fixCallFunction = fix;
			return this;
		}

		/**
		 * 方法名：设置默认导航工具
		 * 方法描述：同行者根据当前已安装导航自适应导航工具
		 *
		 * @param toolType 导航类型
		 * @return 初始化参数实例
		 */
		public InitParam setDefaultNavTool(NavToolType toolType) {
			this.defaultNavTool = TXZNavManager.getNavPackageNameByType(toolType);
			return this;
		}

		/**
		 * 方法名：是否启用内置服务号联系人
		 * 方法描述：启用内置服务号联系人， 中国移动、中国联通等固定常用号码
		 *
		 * @param en 是否启用，默认true
		 * @return 初始化参数实例
		 */
		public InitParam setEnableServiceContact(boolean en) {
			this.enableServiceContact = en;
			return this;
		}

		/**
		 * 方法名：设置识别模式
		 * 方法描述：识别模式配置
		 *
		 * @return 初始化参数实例
		 */
		public InitParam setAsrMode(AsrMode mode) {
			this.asrMode = mode;
			return this;
		}

		/**
		 * 方法名：设置场景限定
		 * 方法描述：启用场景限定，对语义结果有影响，默认不启用
		 *
		 * @param b 是否启用
		 * @return 初始化参数实例
		 */
		public InitParam setCoexistAsrAndWakeup(boolean b) {
			this.coexistAsrAndWakeup = b;
			return this;
		}

		/**
		 * 方法名：设置唤醒词灵敏度
		 * 方法描述：正常唤醒阀值,建议值为 -2.7f 到 -3.1f 分数值越大，越容易唤醒，但是误唤醒率越高。
		 *
		 * @param threshHold 阈值
		 * @return 初始化参数实例
		 */
		public InitParam setWakeupThreshhold(float threshHold) {
			this.wakeupThreshHold = threshHold;
			return this;
		}

		/**
		 * 方法名：设置免唤醒词阈值
		 * 方法描述：识别唤醒阀值,建议值为 -2.7f 到 -3.1f 分数值越大，越容易唤醒，但是误唤醒率越高。
		 *
		 * @param threshHold 阈值
		 * @return 初始化参数实例
		 */
		public InitParam setAsrWakeupThreshhold(float threshHold) {
			this.asrWakeupThreshHold = threshHold;
			return this;
		}

		/**
		 * 方法名：设置TTS播报速度
		 * 方法描述：TTS播报速度，取值范围在20到100之间，标准语速为70。
		 *
		 * @param ttsVoiceSpeed 播报速度
		 * @return 初始化参数实例
		 */
		public InitParam setTtsVoiceSpeed(int ttsVoiceSpeed) {
			if (ttsVoiceSpeed < 20) {
				ttsVoiceSpeed = 20;
			} else if (ttsVoiceSpeed > 100) {
				ttsVoiceSpeed = 100;
			}
			this.ttsVoiceSpeed = ttsVoiceSpeed;
			return this;
		}

		/**
		 * 方法名：设置默认参数
		 * 方法描述：设置默认参数（唤醒敏感度，语音播报速度，唤醒词）
		 *
		 * @param mDefaultDoc 默认参数
		 * @return 初始化参数实例
		 */
		public InitParam setDefaultConfig(String mDefaultDoc) {
			if (mDefaultDoc != null) {
				this.defaultConfig = mDefaultDoc;
			}
			return this;
		}

		/**
		 * 方法名：设置Beep音超时时间
		 * 方法描述：语音录音前Beep音在部分车机切通道时会被漏掉，通过延时处理
		 *
		 * @param timeOut Beep音超时时间, 单位毫秒， 建议值 100ms - 500ms
		 * @return 初始化参数实例
		 */
		public InitParam setBeepTimeOut(int timeOut) {
			this.beepTimeOut = timeOut;
			return this;
		}

		/**
		 * 方法名：设置滤噪方式
		 * 方法描述：回声消除使用类型
		 *
		 * @param filterNoiseType 滤噪方式，参考AEC_TYPE_常量
		 *                        0无，1双麦右声道参考回音消除，2为内录回音消除模式，3为双麦左声道参考回音消除
		 * @return 初始化参数实例
		 */
		public InitParam setFilterNoiseType(int filterNoiseType) {
			this.filterNoiseType = filterNoiseType;
			return this;
		}

		/**
		 * 方法名：设置识别模式
		 * 方法描述：设置识别模式:混合模式, 纯离线, 纯在线, 自动模式。
		 *           默认是混合模式,暂不支持设置纯离线和纯在线。
		 *           低配置机器可选择自动模式。
		 *
		 * @param asrServiceMode 识别模式
		 * @return 初始化参数实例
		 */
		public InitParam setAsrServiceMode(AsrServiceMode asrServiceMode) {
			this.asrServiceMode = asrServiceMode;
			return this;
		}


		/**
		 * 方法名：设置是否添加默认音乐类型
		 * 方法描述：是否默认使用语音已适配音乐资源，默认使用
		 *
		 * @param defaultMusicType false 不添加 默认添加。
		 * @return 初始化参数实例
		 */
		public InitParam setAddDefaultMusicType(Boolean defaultMusicType) {
			this.addDefaultMusicType = defaultMusicType;
			return this;
		}

		/**
		 * 方法名：设置最大识别录音时长
		 * 方法描述：语音录音识别录音时间,单位毫秒。建议不要低于5000毫秒,不要大于30000毫秒
		 *
		 * @param maxTime 最大识别录音时长
		 * @return 初始化参数实例
		 */
		public InitParam setMaxAsrRecordTime(int maxTime) {
			this.maxAsrRecordTime = maxTime;
			return this;
		}

		/**
		 * 方法名：是否音量偏低时弹出Toast
		 * 方法描述：音量低时，是否需要语音弹Toast提示，默认false
		 *
		 * @param enable true允许显示toast,false 不允许显示toast。
		 * @return 初始化参数实例
		 */
		public InitParam enableZeroVolToast(boolean enable) {
			this.zeroVolToast = enable;
			return this;
		}

		/**
		 * 方法名：设置同行者声音通道
		 * 方法描述：TTS播报流，默认STREAM_ALARM
		 *
		 * @param stream 流
		 * @return 初始化参数实例
		 */
		public InitParam setTxzStream(int stream) {
			txzStream = stream;
			return this;
		}

		/**
		 * 方法名：设置皮肤包的路径
		 * 方法描述：语音默认加载皮肤包路径，默认使用/system/txz/resource/ResHolder.apk
		 *
		 * @param path 路径
		 * @return 初始化参数实例
		 */
		public InitParam setResApkPath(String path){
			this.resApkPath = path;
			return this;
		}

		/**
		 * 方法名：设置适配指令表加载路径
		 * 方法描述：使用文本指注册指令时，传递文本路径，供语音读取并注册
		 *
		 * @param path 路径
		 * @return 初始化参数实例
		 */
		public InitParam setAdapterLocalCommandLoadPath(String path){
			this.adapterLocalCommandLoadPath = path;
			return this;
		}

		/**
		 * 方法名：设置适配指令表备份路径
		 * 方法描述：使用文本指注册指令时，传递文本备份路径
		 *
		 * @param path 路径
		 * @return 初始化参数实例
		 */
		public InitParam setAdapterLocalCommandBackupPath(String path) {
			this.adapterLocalCommandBackupPath = path;
			return this;
		}

		public InitParam setVoiceprintRecognitionScore(double score) {
			this.voiceprintRecognitionScore = score;
			return this;
		}

		public InitParam setVoiceprintRecognitionPackageName(String packageName) {
			this.voiceprintRecognitionPackageName = packageName;
			return this;
		}


		/**
		 * 方法名：设置是否使用外部声音源输入
		 * 方法描述：是否需要通过外部录音进行识别，不采用安卓标准录音。需要配合setExtAudioSourceType使用
		 *
		 * @param enable true使用外部声音源， false使用默认声音源输入*
		 * @return 初始化参数实例
		 */
		public InitParam useExternalAudioSource(boolean enable) {
			useExternalAudioSource = enable;
			return this;
		}

		/**
		 * 方法名：设置使用外部声音源的类型
		 * 方法描述：需要配合useExternalAudioSource使用，使用外部录音时，一般使用EXT_AUDIOSOURCE_TYPE_TXZ
		 *
		 * @param type EXT_AUDIOSOURCE_TYPE_MSD 美赛达专用
		 *             EXT_AUDIOSOURCE_TYPE_TXZ 使用TXZ提供的aidl接口实现的外部音频输入方式的话，设置该常量。
		 *             默认为EXT_AUDIOSOURCE_TYPE_MSD
		 * @return 初始化参数实例
		 */
		public InitParam setExtAudioSourceType(int type) {
			extAudioSourceType = type;
			return this;
		}

		/**
		 * 方法名：设置是否将唤醒的录音循环写入一个文件
		 * 方法描述：设置是否将唤醒的录音循环写入一个文件
		 *
		 * @param enable ：true 将唤醒的录音循环写入一个文件 false：不保存唤醒的录音
		 * @return 初始化参数实例
		 */
		public InitParam enableBlackHole(boolean enable) {
			this.enableBlackHole = enable;
			return this;
		}

		/**
		 * 方法名：设置开启回声消除的时候, 是否停止唤醒，防误打断
		 * 方法描述：默认不强制停止
		 *
		 * @param force ：true 强制停止， false不停止。
		 * @return 初始化参数实例
		 */
		public InitParam forceStopWkWhenTts(boolean force) {
			this.forceStopWkWhenTts = force;
			return this;
		}

		/**
		 * 方法名：设置开启保护唤醒引擎
		 * 方法描述：防止回音消除不好造成的误打断;无回音消除设备开启打断的前置条件；
		 * 			开启会造成CPU和内存的一定消耗，默认不开启
		 *
		 * @param enable 是否启用 默认不开启
		 * @return 初始化参数实例
		 */
		public InitParam enableProtectWakeup(boolean enable){
			this.enableProtectWakeup = enable;
			return this;
		}

		/**
		 * 方法名：设置录音机的AudioSource的类型。
		 * 方法描述：默认录音不满足时，使用此接口，默认MediaRecorder.AudioSource.DEFAULT
		 *
		 * @param audioSource ：与Android的MediaRecorder.AudioSource数值一致
		 * @return 初始化参数实例
		 */
		public InitParam setAudioSourceForRecord(int audioSource) {
			this.audioSourceForRecord = audioSource;
			return this;
		}

		/**
		 * 方法名：设置是否使用唤醒大模型
		 * 方法描述：引擎识别、唤醒模型，默认关闭，开启后会增加CPU和内存消耗，推荐通过调整唤醒词灵敏度
		 *
		 * @param useHQualityWakeupModel 是否启用大模型
		 * @return 初始化参数实例
		 */
		public InitParam setUseHQualityWakeupModel(boolean useHQualityWakeupModel) {
			this.useHQualityWakeupModel = useHQualityWakeupModel;
			return this;
		}


		/**
		 * 方法名：设置外部音频输入的服务的包名。(5.0以后的系统需要设置)
		 * 方法描述：设置外部音频输入的服务的包名，Android5.0以后的AIDL需要显示调用，需要设置
		 *
		 * @param pkgName 外部音频输入的服务的包名
		 * @return 初始化参数实例
		 */
		public InitParam setExtAudioSourcePkg(String pkgName){
			this.extAudioSourcePkg = pkgName;
			return this;
		}

		/**
		 * 方法名：设置语音窗口的优先级
		 * 方法描述：默认语音界面层，参考WindowManager.LayoutParams中的type
		 *
		 * @param winType 层级
		 * @return 初始化参数实例
		 */
		public InitParam setWinType(Integer winType) {
			this.winType = winType;
			return this;
		}

		/**
		 * 方法名：设置弹窗的自动取消时间
		 * 方法描述：设置弹窗的自动取消时间，仅针对导航弹窗，默认不关闭
		 *
		 * @param timeout 单位ms
		 * @return 初始化参数实例
		 */
		public InitParam setDialogTimeOut(Integer timeout){
			this.dialogTimeout = timeout;
			return this;
		}

		/**
		 * 方法名：设置打断模式
		 * 方法描述：使用回声消除后的打断开启模式
		 * 			INTERRUPT_MODE_DEFAULT,普通打断模式：播报中仅支持唤醒词打断<br>
		 * 			INTERRUPT_MODE_ORDER,后置打断模式：播报中识别到具体指令后打断播报，聊天指令不打断<br>
		 *
		 * @param interruptMode 打断模式
		 * @return 初始化参数实例
		 */
		public InitParam setInterruptMode(InterruptMode interruptMode) {
			this.interruptMode = interruptMode;
			return this;
		}

		/**
		 * 方法名：设置是否在主进程中使用在线识别
		 * 方法描述：设置是否在主进程中使用在线识别，默认关闭
		 *
		 * @param bLocal true：在主进程中使用在线识别  false：另开一个进程运行在线识别  默认为false
		 * @return 初始化参数实例
		 */
		public InitParam setNetAsr(boolean bLocal){
			this.useLocalNetAsr = bLocal;
			return this;
		}

		/**
		 * 方法名：设置语音设置包名
		 * 方法描述：点击声控图标上的设置按钮，会跳转到指定package的apk，当使用外部自定义语音设置时，需要传入包名
		 *
		 * @param settingPackageName 包名
		 * @return 初始化参数实例
		 */
		public InitParam setSettingPackageName(String settingPackageName) {
			this.settingPackageName = settingPackageName;
			return this;
		}

		/**
		 * 方法名：是否禁用电台抢占收音机
		 * 方法描述：当安装了电台工具的时候，广播节目使用电台工具搜索，默认为true
		 *
		 * @param useRadioAsAudio 是否启用
		 * @return 初始化参数实例
		 */
		public InitParam setUseRadioAsAudio(Boolean useRadioAsAudio) {
			this.useRadioAsAudio = useRadioAsAudio;
			return this;
		}

		/**
		 * 常量名：网络模式，无
		 * 常量描述：当前设备无网络制式
		 */
		public static final int NET_MOUDLE_NONE = 2;
		/**
		 * 常量名：网络模式，2G
		 * 常量描述：当前设备2G制式
		 */
		public static final int NET_MOUDLE_2G = 3;
		/**
		 * 常量名：网络模式，3G
		 * 常量描述：当前设备3G制式
		 */
		public static final int NET_MOUDLE_3G = 4;
		/**
		 * 常量名：网络模式，4G
		 * 常量描述：当前设备4G制式
		 */
		public static final int NET_MOUDLE_4G = 5;

		/**
		 * 方法名：设置设备拥有的网络模块
		 * 方法描述：设备拥有的网络模块类型
		 *
		 * @param module {@link #NET_MOUDLE_NONE}
		 *               {@link #NET_MOUDLE_2G}
		 *               {@link #NET_MOUDLE_3G}
		 *               {@link #NET_MOUDLE_4G}
		 * @return 初始化参数实例
		 */
		public InitParam setNetModule(int module){
			this.netModule = module;
			return this;
		}

		/**
		 * 方法名：是否启用特殊场景防误打断功能
		 * 方法描述：针对新引擎开了aec情况下的误唤醒，同行者产品定义做了一套交互
		 * 			1、在选择第X个后，禁止打断
		 * 			2、在播报有“确定”、“取消”的tts中，不响应“确定”、“取消”，播报完才支持
		 *
		 * @param aecPreventFalseWakeup 是否启用
		 * @return 初始化参数实例
		 */
		public InitParam setAECPreventFalseWakeup(boolean aecPreventFalseWakeup){
			this.aecPreventFalseWakeup = aecPreventFalseWakeup;
			return this;
		}

		/**
		 * 方法名：配置免唤醒状态ID
		 * 方法描述：配置需要有说话状态回调的useWakeupAsAsr的taskid
		 *
		 * @param taskId 免唤醒ID
		 * @return 初始化参数实例
		 */
		public InitParam setNeedSpeechStateTaskId(String taskId){
			this.needSpeechStateTaskId = taskId;
			return this;
		}

		/**
		 * 方法名：配置采用何种模式优化内存占用
		 * 方法描述：配置采用何种模式优化内存占用
		 * 			{@link #MEM_MODE_NONE}
		 * 			{@link #MEM_MODE_PREBUILD}
		 * 			{@link #MEM_MODE_PREBUILD_MERGE}
		 *
		 * @param mode 模式
		 * @return 初始化参数实例
		 */
		public InitParam setMemMode(int mode){
			this.memMode = mode;
			return this;
		}

		/**
		 * 方法名：配置是否需要使用打字效果
		 * 方法描述：是否启用文字实时上屏功能，仅支持语音2.8.0以上版本
		 *
		 * @param useTypingEffect 是否启用
		 * @return 初始化参数实例
		 */
		public InitParam setUseTypingEffect(Boolean useTypingEffect) {
			this.useTypingEffect = useTypingEffect;
			return this;
		}

		/**
		 * 方法名：是否点击语音外部显示区域关闭语音界面
		 * 方法描述：点击语音未显示区域时，关闭语音界面，默认不启用
		 *
		 * @param canceledOnTouchOutside 是否启用
		 * @return 初始化参数实例
		 */
		public InitParam setCanceledOnTouchOutside(Boolean canceledOnTouchOutside){
			this.canceledOnTouchOutside = canceledOnTouchOutside;
			return this;
		}

		/**
		 * 方法名：是否启用点击语音外部时，传递点击事件
		 * 方法描述：点击语音未显示区域时，是否需要将点击事件透传
		 *
		 * @param allow 是否透传
		 * @return 初始化参数实例
		 */
		public InitParam setAllowOutSideClickSentToBehind(Boolean allow){
			this.allowOutSideClickSentToBehind = allow;
			return this;
		}

		/**
		 * 方法名：设置VIN码
		 * 方法描述：针对提醒事项功能，需要上报字段携带，车架号，确保用户唯一
		 *
		 * @param vin VIN码
		 * @return 初始化参数实例
		 */
		public InitParam setVin(String vin){
			this.vin = vin;
			return this;
		}

		/**
		 * 方法名：设置硬件模块校验的参数
		 * 方法描述：使用同行者硬件参数绑定激活时，设置此参数，请先与同行者对应人员确认，否则无法使用
		 *
		 * @param params 硬件模块校验的参数
		 * @return 初始化参数实例
		 */
		public InitParam setHardWareParams(byte[] params){
			this.hardwareParams = params;
			return this;
		}

		/**
		 * 方法名：设置启用和关闭tts播放器
		 * 方法描述：配置是否启用tts播放器
		 * @param enableTtsPlayer 启用tts播放器额参数
		 * @return 初始化参数实例
		 */
		public InitParam enableTtsPlayer(boolean enableTtsPlayer) {
			this.enableTtsPlayer = enableTtsPlayer;
			return this;
		}

		/**
		 * 方法名：设置录音机的缓冲区大小
		 * 方法描述：设置录音机的缓冲区大小
		 * @param recorderBufferSize 缓冲区大小
		 * @return 初始化参数实例
		 */
		public InitParam setRecorderBufferSize(int recorderBufferSize){
			this.recorderBufferSize = recorderBufferSize;
			return this;
		}

	}

	/**
	 * 接口名：初始化监听器
	 * 接口描述：初始化时传入，通知激活状态，同时，与同行者核心服务断开连接后会自动重连并初始化
	 */
	public static interface InitListener {
		/**
		 * 方法名：连接成功
		 * 方法描述：语音完全连接成功后触发，需要激活语音
		 */
		void onSuccess();

		/**
		 * 方法名：发生异常
		 * 方法描述：语音异常时触发
		 *
		 * @param errCode 错误码
		 * @param errDesc 错误描述
		 */
		void onError(int errCode, String errDesc);
	}

	/**
	 * 接口名：激活监听器
	 * 接口描述：首次联网激活触发
	 */
	public static interface ActiveListener {
		/**
		 * 方法名：首次联网激活
		 * 方法描述：首次联网激活时触发，若语音本地文件全部被破坏，则会再次触发
		 */
		void onFirstActived();
	}

	/**
	 * 接口名：连接状态监听器
	 * 接口描述：SDK与语音连接状态监听器
	 */
	public static interface ConnectListener {
		/**
		 * 方法名：连接上
		 * 方法描述：SDK与语音连接时触发
		 */
		void onConnect();
		/**
		 * 方法名：断开连接
		 * 方法描述：SDK与语音断开连接时触发
		 */
		void onDisconnect();
		/**
		 * 方法名：异常
		 * 方法描述：SDK与语音连接异常时触发
		 */
		void onExcepiton();
	}

	ConnectListener mConnectListener = null;

	/**
	 * 方法名：设置连接监听器
	 * 方法描述：SDK与语音出现异常时将会断开连接
	 */
	public void setConnectListener(ConnectListener listener) {
		mConnectListener = listener;
	}

	/**
	 * 同行者服务连接监听器
	 */
	private ConnectionListener mConnectionListener = new ConnectionListener() {
		@Override
		public void onDisconnected(String serviceName) {
			if (ServiceManager.TXZ.equals(serviceName)) {
				LogUtil.logd("txz disconnected");
				mInited = false;

				if (mConnectListener != null) {
					if (TXZService.mTXZHasExited == false) {
						mConnectListener.onExcepiton();
					}
					mConnectListener.onDisconnect();
				}
			}
		}

		@Override
		public void onConnected(String serviceName) {
			// 同行者连接上后才真正开始初始化SDK
			if (ServiceManager.TXZ.equals(serviceName)) {
				LogUtil.logd("txz connected: initializeSDK");
				initializeSDK();

				if (mConnectListener != null) {
					mConnectListener.onConnect();
				}
			}
		}
	};

	/**
	 * 记录的初始化参数
	 */
	InitParam mInitParam;
	/**
	 * 记录的初始化监听器
	 */
	private InitListener mInitListener;
	/**
	 * 记录的激活监听器
	 */
	private ActiveListener mActiveListener;
	/**
	 * 是否已经初始化成功
	 */
	private boolean mInited = false;

	/**
	 * 是否初始化过了
	 *
	 * @return 是否初始化过
	 */
	boolean isInited() {
		return mInited;
	}

	private Boolean mInitedSuccess = null;

	/**
	 * 方法名：判断当前语音是否初始化成功了
	 * 方法描述：用于判断当前语音初始化状态，部分SDK接口无法在未初始化情况下调用
	 *
	 * @return 是否初始化成功
	 */
	public boolean isInitedSuccess() {
		return mInitedSuccess != null && mInitedSuccess == true;
	}

	/**
	 * 方法名：初始化SDK
	 * 方法描述：初始化SDK，不携带初始化数据，不会初始化语音引擎，仅连接SDK语音服务
	 * 			当外部已有SDK带初始化参数初始化语音时，额外SDK端可以使用此方法连接SDK而不需要带InitParam
	 *
	 * @param context  上下文
	 * @param listener 回调监听器
	 */
	public void initialize(Context context, InitListener listener) {
		initialize(context, null, listener);
	}

	/**
	 * 方法名：初始化SDK
	 * 方法描述：初始化SDK，需要初始化设置appId和appToken，会进行语音引擎的初始化
	 *
	 * @param context  上下文
	 * @param param    初始化参数
	 * @param listener 回调监听器
	 */
	public void initialize(Context context, InitParam param,
			InitListener listener) {
		initialize(context, param, listener, null);
	}

	/**
	 * 方法名：初始化SDK
	 * 方法描述：初始化SDK，需要初始化设置appId和appToken，会进行语音引擎的初始化
	 *
	 * @param context        上下文
	 * @param param          初始化参数
	 * @param listener       回调监听器
	 * @param activeListener 激活监听器
	 */
	public void initialize(Context context, InitParam param,
			InitListener listener, ActiveListener activeListener) {
		// 已经调用过初始化了
		if (mInitListener != null) {
			return;
		}

		mInitParam = param;
		mInitListener = listener;
		mActiveListener = activeListener;
		GlobalContext.set(context);

		// 启动服务
		context.startService(new Intent(context, TXZService.class));
		ServiceManager.getInstance().addConnectionListener(mConnectionListener);

		// 监听同行者启动
		ServiceManager.getInstance().keepConnection(ServiceManager.TXZ,
				new Runnable() {
					@Override
					public void run() {
						onReconnectTXZ();
					}
				});

		// 已连接同行者，直接初始化
		if (ServiceManager.getInstance().getService(ServiceManager.TXZ) != null) {
			initializeSDK();
			return;
		}

		// 发送一个空指令来建立连接
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "", null,
				null);
		// 初始化UdpRpc
		TXZUdpClient.getInstance().init();

		// 要求同步一次数据
		ConfigUtil.requestSync();
	}

	boolean mInitCompleted = false;
	int mLastInitStatus = -2;
	private static final int STATUS_SUCCESS = 0;
	private static final int STATUS_ERROR = -1;

	Runnable mRunnableInitSuccess = new Runnable() {
		@Override
		public void run() {
			LogUtil.logi("sdk init connected with txz");
		}
	};

	/**
	 * 常量名：语音识别引擎初始化异常
	 * 常量描述：语音识别初始化异常
	 */
	public final static int INIT_ERROR_ASR = 10001;
	/**
	 * 常量名：语音播报引擎初始化异常
	 * 常量描述：语音播报引擎初始化异常
	 */
	public final static int INIT_ERROR_TTS = 10002;
	/**
	 * 常量名：语音唤醒引擎初始化异常
	 * 常量描述：语音唤醒引擎初始化异常
	 */
	public final static int INIT_ERROR_WAKEUP = 10003;

	private CommandProcessor mInitSDKResultProcessor = new CommandProcessor() {
		@Override
		public byte[] process(String packageName, String command, byte[] data) {
			if (command.equals("actived")) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						if (mActiveListener != null) {
							mActiveListener.onFirstActived();
						}
					}
				});
				return null;
			}
			if (command.equals("success")) {
				mInitedSuccess = true;
				reconnectOtherModule();
				TXZPowerManager.getInstance().notifyReInitFinished();
				if (mLastInitStatus != STATUS_SUCCESS || mInitCompleted == false) {
					mInitCompleted = true;
					mLastInitStatus = STATUS_SUCCESS;
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							if (mInitListener != null) {
								mInitListener.onSuccess();
							}
						}
					});
				}
				return null;
			}
			if (command.equals("error.asr")) {
				mInitedSuccess = false;
				if (mLastInitStatus != STATUS_ERROR || mInitCompleted == false) {
					mInitCompleted = true;
					mLastInitStatus = STATUS_ERROR;
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							if (mInitListener != null) {
								mInitListener.onError(INIT_ERROR_ASR, "语音识别初始化发生异常");
							}

						}
					});
				}
				return null;
			}
			if (command.equals("error.tts")) {
				mInitedSuccess = false;
				if (mLastInitStatus != STATUS_ERROR || mInitCompleted == false) {
					mInitCompleted = true;
					mLastInitStatus = STATUS_ERROR;
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							if (mInitListener != null) {
								mInitListener.onError(INIT_ERROR_TTS, "语音播报初始化发生异常");
							}

						}
					});
				}
				return null;
			}
			if (command.equals("error.wakeup")) {
				mInitedSuccess = false;
				if (mLastInitStatus != STATUS_ERROR || mInitCompleted == false) {
					mInitCompleted = true;
					mLastInitStatus = STATUS_ERROR;
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							if (mInitListener != null) {
								mInitListener.onError(INIT_ERROR_WAKEUP, "语音唤醒初始化发生异常");
							}

						}
					});
				}
				return null;
			}
			return null;
		}
	};

	private Runnable mRunnableInitSDK = new Runnable() {
		@Override
		public void run() {
			if (TXZService.mTXZHasExited
					&& (TXZPowerManager.mReleased == null || TXZPowerManager.mReleased == true))
				return;
			TXZService
					.setCommandProcessor("sdk.init.", mInitSDKResultProcessor);

			byte[] param = null;
			if (mInitParam != null) {
				JSONBuilder doc = new JSONBuilder();
				doc.put("version", VERSION);
				if (mInitParam.appId != null)
					doc.put("appId", mInitParam.appId);
				if (mInitParam.appToken != null)
					doc.put("appToken", mInitParam.appToken);
				if (mInitParam.appCustomId != null)
					doc.put("appCustomId", mInitParam.appCustomId);
				if (mInitParam.uuid != null)
					doc.put("uuid", mInitParam.uuid);
				if (mInitParam.neverFormatRoot != null)
					doc.put("neverFormatRoot", mInitParam.neverFormatRoot);
				if (mInitParam.ftType != null)
					doc.put("ftType", mInitParam.ftType.name());
				if (mInitParam.ftUrl_N != null)
					doc.put("ftUrl_N", mInitParam.ftUrl_N);
				if (mInitParam.ftUrl_P != null)
					doc.put("ftUrl_P", mInitParam.ftUrl_P);
				if (mInitParam.ftInterval != null)
					doc.put("ftInterval", mInitParam.ftInterval);
				if(mInitParam.ftX != null)
					doc.put("ftX", mInitParam.ftX);
				if(mInitParam.ftY != null)
					doc.put("ftY", mInitParam.ftY);
				if (mInitParam.asrType != null)
					doc.put("asrType", mInitParam.asrType.name());
				if (mInitParam.ttsType != null)
					doc.put("ttsType", mInitParam.ttsType.name());
				if (mInitParam.wakeupKeywords != null)
					doc.put("wakeupKeywords", mInitParam.wakeupKeywords);
				if(mInitParam.jsonScoreKws != null)
					doc.put("jsonScoreKws", mInitParam.jsonScoreKws);
				if (mInitParam.enableInstantAsr != null)
					doc.put("enableInstantAsr", mInitParam.enableInstantAsr);
//				if (mInitParam.instantAsrKeywords != null)
//					doc.put("instantWakeupKeywords", mInitParam.instantAsrKeywords);
				if (mInitParam.enableServiceContact != null)
					doc.put("enableServiceContact",
							mInitParam.enableServiceContact);
				if (mInitParam.fixCallFunction != null)
					doc.put("fixCallFunction", mInitParam.fixCallFunction);
				if (mInitParam.defaultNavTool != null) {
					doc.put("defaultNavTool", mInitParam.defaultNavTool);
				}
				if (mInitParam.asrMode != null)
					doc.put("asrMode", mInitParam.asrMode.name());
				if (mInitParam.coexistAsrAndWakeup != null)
					doc.put("coexistAsrAndWakeup",
							mInitParam.coexistAsrAndWakeup);
				if (mInitParam.wakeupThreshHold != null)
					doc.put("wakeupThreshHold", mInitParam.wakeupThreshHold);
				if (mInitParam.asrWakeupThreshHold != null)
					doc.put("asrWakeupThreshHold",
							mInitParam.asrWakeupThreshHold);
				if (mInitParam.beepTimeOut != null)
					doc.put("beepTimeOut", mInitParam.beepTimeOut);
				if (mInitParam.filterNoiseType != null)
					doc.put("filterNoiseType", mInitParam.filterNoiseType);
				if (mInitParam.asrServiceMode != null)
					doc.put("asrServiceMode", mInitParam.asrServiceMode.name());
				if (mInitParam.addDefaultMusicType != null)
					doc.put("addDefaultMusicType", mInitParam.addDefaultMusicType);
				if (mInitParam.ttsVoiceSpeed != null) {
					doc.put("ttsVoiceSpeed", mInitParam.ttsVoiceSpeed);
				}
				if (mInitParam.maxAsrRecordTime != null) {
					doc.put("maxAsrRecordTime", mInitParam.maxAsrRecordTime);
				}
				if (mInitParam.zeroVolToast != null) {
					doc.put("zeroVolToast", mInitParam.zeroVolToast);
				}
				if (mInitParam.txzStream != null) {
					doc.put("txzStream", mInitParam.txzStream);
				}
				if (mInitParam.useExternalAudioSource != null) {
					doc.put("useExternalAudioSource",
							mInitParam.useExternalAudioSource);
				}
				if (mInitParam.enableBlackHole != null) {
					doc.put("enableBlackHole", mInitParam.enableBlackHole);
				}
				if (mInitParam.audioSourceForRecord != null) {
					doc.put("audioSourceForRecord", mInitParam.audioSourceForRecord);
				}
				if (mInitParam.forceStopWkWhenTts != null) {
					doc.put("forceStopWkWhenTts", mInitParam.forceStopWkWhenTts);
				}
				if (mInitParam.enableProtectWakeup != null) {
					doc.put("enableProtectWakeup", mInitParam.enableProtectWakeup);
				}
				if (mInitParam.extAudioSourceType != null){
					doc.put("extAudioSourceType", mInitParam.extAudioSourceType);
				}
				if (mInitParam.useHQualityWakeupModel !=null) {
					doc.put("useHQualityWakeupModel", mInitParam.useHQualityWakeupModel);
				}
				if (mInitParam.extAudioSourcePkg != null){
					doc.put("extAudioSourcePkg", mInitParam.extAudioSourcePkg);
				}
				if (mInitParam.winType != null) {
					doc.put("winType", mInitParam.winType);
				}
				if (mInitParam.dialogTimeout != null) {
					doc.put("dialogTimeout", mInitParam.dialogTimeout);
				}
				if (mInitParam.resApkPath != null) {
					doc.put("resApkPath", mInitParam.resApkPath);
				}
				if (mInitParam.adapterLocalCommandBackupPath != null) {
                    doc.put("adapterLocalCommandBackupPath", mInitParam.adapterLocalCommandBackupPath);
                }
                if (mInitParam.adapterLocalCommandLoadPath != null) {
                    doc.put("adapterLocalCommandLoadPath", mInitParam.adapterLocalCommandLoadPath);
				}
				if (mInitParam.voiceprintRecognitionScore != null) {
					doc.put("voiceprintRecognitionScore", mInitParam.voiceprintRecognitionScore);
				}
				if (mInitParam.voiceprintRecognitionPackageName != null) {
					doc.put("voiceprintRecognitionPackageName", mInitParam.voiceprintRecognitionPackageName);
				}
				if (mInitParam.forceUseUI1 != null) {
					doc.put("forceUseUI1", mInitParam.forceUseUI1);
				}
				if (mInitParam.interruptMode != null) {
					doc.put("interruptTTSType", mInitParam.interruptMode.name());
				}
				if (mInitParam.winBgAlpha != null) {
					doc.put("winBgAlpha", mInitParam.winBgAlpha);
				}
				if (mInitParam.useLocalNetAsr != null){
					doc.put("useLocalNetAsr", mInitParam.useLocalNetAsr);
				}
				if (mInitParam.winRecordImpl != null) {
					doc.put("winRecordImpl", mInitParam.winRecordImpl);
				}
				//设悬浮图标大小
				if (mInitParam.floatToolWidth != null && mInitParam.floatToolHeight != null) {
					doc.put("floatToolWidth", mInitParam.floatToolWidth);
					doc.put("floatToolHeight", mInitParam.floatToolHeight);
				}
				//设置对话框是否是可撤销的
				if (mInitParam.cancelable != null) {
					doc.put("cancelable", mInitParam.cancelable);
				}

				if (mInitParam.settingPackageName != null) {
					doc.put("settingPackageName", mInitParam.settingPackageName);
				}
				if (mInitParam.enableFullScreen != null) {
					doc.put("enableFullScreen", mInitParam.enableFullScreen);
				}
				if (mInitParam.useRadioAsAudio != null) {
					doc.put("useRadioAsAudio", mInitParam.useRadioAsAudio);
				}
				if(mInitParam.netModule != null) {
					doc.put("netModule", mInitParam.netModule);
				}
				if (mInitParam.aecPreventFalseWakeup != null) {
					doc.put("aecPreventFalseWakeup", mInitParam.aecPreventFalseWakeup);
				}
				if (mInitParam.messageDialogType != null) {
					doc.put("messageDialogType", mInitParam.messageDialogType);
				}
				if (mInitParam.needSpeechStateTaskId != null) {
					doc.put("needSpeechStateTaskId",mInitParam.needSpeechStateTaskId);
				}
				if (mInitParam.memMode != null){
					doc.put("memMode", mInitParam.memMode);
				}
				if (mInitParam.useTypingEffect != null) {
					doc.put("useTypingEffect",mInitParam.useTypingEffect);
				}
				if (mInitParam.canceledOnTouchOutside != null) {
					doc.put("canceledOnTouchOutside",mInitParam.canceledOnTouchOutside);
				}
				if (mInitParam.allowOutSideClickSentToBehind != null) {
					doc.put("allowOutSideClickSentToBehind",mInitParam.allowOutSideClickSentToBehind);
				}
				if (mInitParam.vin != null){
					doc.put("vin",mInitParam.vin);
				}

				if (mInitParam.hardwareParams != null){
					doc.put("hardwareParams", Base64.encodeToString(mInitParam.hardwareParams, Base64.DEFAULT));
				}

				if(mInitParam.fmNamesPath != null){
					doc.put("fmNamesPath", mInitParam.fmNamesPath);
				}

				if (mInitParam.enableTtsPlayer != null) {
					doc.put("enableTtsPlayer", mInitParam.enableTtsPlayer);
				}

				if (mInitParam.recorderBufferSize != null) {
					doc.put("recorderBufferSize", mInitParam.recorderBufferSize);
				}

				param = doc.toBytes();
			}

			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.sdk.init", param, new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							if (data != null) {
								mInited = true;
								ServiceManager.getInstance()
										.removeOnServiceThread(
												mRunnableInitSuccess);
								ServiceManager.getInstance()
										.runOnServiceThread(
												mRunnableInitSuccess, 100);
							}
						}
					});


			reconnectOtherModule();
		}
	};

	/**
	 * 真正的初始化，重连或收到TXZ启动时会重复执行
	 */
	void initializeSDK() {
		if (TXZService.mTXZHasExited
				&& (TXZPowerManager.mReleased == null || TXZPowerManager.mReleased == true))
			return;
		ServiceManager.getInstance().removeOnServiceThread(mRunnableInitSDK);
		ServiceManager.getInstance().runOnServiceThread(mRunnableInitSDK, 0);
	}

	/**
	 * 设置随意打断模式
	 * @param mode 随意打断模式
	 */
	public void setInterruptMode(final InterruptMode mode){
		if (null != mInitParam) {
			mInitParam.setInterruptMode(mode);
		}
		JSONBuilder doc = new JSONBuilder();
		doc.put("mode", mode.name());
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"comm.asr.setInterruptMode", doc.toString().getBytes(), null);
	}

	/**
	 * 方法名：设置悬浮图标状态
	 * 方法描述：动态设置悬浮图标状态
	 *
	 * @param type 层级状态
	 */
	public void showFloatTool(FloatToolType type) {
		if (null != mInitParam) {
			mInitParam.setFloatToolType(type);
		}

		JSONBuilder doc = new JSONBuilder();
		doc.put("floatToolType", type.name());
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.sdk.ft.status.type", doc.toString().getBytes(), null);
	}

	/**
	 * 方法名：设置悬浮工具点击间隔限制
	 * 方法描述：修改语音图标点击响应间隔时间，默认间隔0，单位：ms
	 *
	 * @param interval 间隔，ms
	 */
	public void setFloatToolClickInterval(long interval) {
		if (null != mInitParam) {
			mInitParam.setFloatToolClickInterval(interval);
		}
		JSONBuilder doc = new JSONBuilder();
		doc.put("ftInterval", interval);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.sdk.ft.status.interval", doc.toString().getBytes(), null);
	}

	/**
	 * 方法名：设置悬浮图标自适
	 * 方法描述：开启悬浮图标自适应，默认间隔0，单位：ms
	 */
	public void setFloatViewEnableAutoAdjust() {
		isDisableAutoJust = true;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.sdk.ft.status.enableAutoAdjust", null, null);
	}


	Boolean isDisableAutoJust = null;

	/**
	 * 方法名：设置悬浮图标自适
	 * 方法描述：关闭悬浮图标自适应，默认间隔0，单位：ms
	 */
	public void setFloatViewDisableAutoAdjust() {
		isDisableAutoJust = false;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.sdk.ft.status.disableAutoAdjust", null, null);
	}

	/**
	 * 方法名：设置悬浮图标的图片资源
	 * 方法描述：语音图标默认图标不符合需求时，可以传入指定图片路径，以替换默认图标
	 *
	 * @param ftUrl_N 普通状态的图片
	 * @param ftUrl_P 按下状态的图片，如果值为null，则与普通状态一致
	 */
	public void setFloatToolIcon(String ftUrl_N, String ftUrl_P) {
		if (null != mInitParam) {
			mInitParam.setFloatToolIcon(ftUrl_N, ftUrl_P);
		}

		JSONBuilder doc = new JSONBuilder();
		doc.put("floatToolUrl_N", ftUrl_N);
		doc.put("floatToolUrl_P", ftUrl_P);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.sdk.ft.status.icon", doc.toString().getBytes(), null);
	}

	/**
	 * 方法名：是否开启语音界面的设置显示按钮
	 * 方法描述：是否开启语音界面的设置显示按钮，默认不显示，已弃用，只允许初始化时调用一次
	 *
	 * @param enable 是否启用设置
	 * @deprecated 已弃用，只允许初始化时调用一次
	 */
	@Deprecated
	public void enableSettings(boolean enable) {
		LogUtil.loge("Deprecated method TXZConfigManager::enableSettings");
//		ConfigUtil.setShowSettings(enable);
//		ConfigUtil.sendConfigs();
	}


	Boolean mEnableWinAnim = null;

	/**
	 * 方法名：是否开启窗口显示动画
	 * 方法描述：窗口进入时是否有动画，默认开启，建议开启
	 *
	 * @param enable 是否开启动画
	 */
	public void enableWinAnim(boolean enable){
		LogUtil.logd("enableWinAnim enable:"+enable);
		mEnableWinAnim = enable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.win.enableAnim",
				(""+enable).getBytes(), null);
	}

	Boolean mEnableCoverDefaultKeywords = null;

	/**
	 * 方法名：是否启用覆盖默认唤醒词（使用同行者设置时）
	 * 方法描述：当显示设置菜单时，设置新唤醒词是否会覆盖默认唤醒词，默认覆盖。
	 * 			NOTE:只有设置了显示设置菜单时这个设置才会起作用，否则不覆盖。
	 *
	 * @param enable 是否覆盖默认唤醒词
	 */
	public void enableCoverDefaultKeywords(boolean enable) {
		LogUtil.logd("TXZConfigManager::enableCoverDefaultKeywords, enable=" + enable);
		mEnableCoverDefaultKeywords = enable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.wakeup.enableCoverDefaultKeywords",
				("" + enable).toString().getBytes(), null);
	}

	/**
	 * 方法名：设置语音唤醒词
	 * 方法描述：动态设置语音唤醒词，最多设置{@link #MAX_WAKEUP_KEYWORDS_COUNT}
	 * 			设置null时，则无唤醒功能
	 *
	 * @param keywords 唤醒使用的关键字，传入若不为空，则会启用唤醒功能
	 */
	public void setWakeupKeywordsNew(String... keywords) {
		if (keywords == null) {
			keywords = new String[0];
		}
		if (null != mInitParam) {
			mInitParam.setWakeupKeywordsNew(keywords);
		}

		try {
			JSONArray json = new JSONArray();
			for (String kw : keywords) {
				if (!TextUtils.isEmpty(kw)) {
					json.put(kw);
				}
			}
			if (json.length() > MAX_WAKEUP_KEYWORDS_COUNT) {
				return;
			}
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.wakeup.update", json.toString().getBytes(), null);
		} catch (Exception e) {
		}
	}

	/**
	 * 方法名：设置唤醒词的阈值
	 * 方法描述：针对唤醒词设置不同的阈值
	 *
	 * @param jsonScoreKws json文本，格式如下，默认-3.1
	 *                     [{"keyWords":"你好小踢","threshold":-3.1},
	 *                     {"keyWords":"小踢你好","threshold":-3.2},
	 *                     {"keyWords":"小踢小踢","threshold":-3.3}]
	 */
	public void setWakeupKeyWordsThreshold(String jsonScoreKws) {
		if(mInitParam != null){
			mInitParam.setWakeupKeyWordsThreshold(jsonScoreKws);
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.wakeup.setKwsThreshold", jsonScoreKws.getBytes(),
				null);
	}

	/**
	 * 方法名：设置是否开启免唤醒功能
	 * 方法描述：是否启用免唤醒词功能,默认启用，不推荐关闭
	 *
	 * @param enable 是否启用
	 */
	public void setInstantAsrEnabled(boolean enable) {
		if (null != mInitParam) {
			mInitParam.setInstantAsrEnabled(enable);
		}

		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.wakeup.setInstantAsrEnable",
				String.valueOf(enable).getBytes(), null);
	}

	/*
	 * 方法名：设置免唤醒命令词，传null关闭免唤醒功能
	 * @param keywords
	 */
//	public void setInstantAsrKeywords(String... keywords){
//		if(null == keywords) {
//			keywords = new String[0];
//		}
//		if (null != mInitParam) {
//			mInitParam.setInstantAsrKeywords(keywords);
//		}
//		
//		try {
//			JSONArray json = new JSONArray();
//			for (String kw : keywords) {
//				if (!TextUtils.isEmpty(kw)) {
//					json.put(kw);
//				}
//			}
//			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
//					"txz.wakeup.update_instant", json.toString().getBytes(), null);
//		} catch (Exception e) {
//			LogUtil.loge("instantWakeup::generate json data in sdk encountered error: " + e.toString());
//		}
//	}

	Boolean mEnableWakeup = null;

	/**
	 * 方法名：是否启用语音唤醒功能
	 * 方法描述：启用语音唤醒功能，默认启用，只用于休眠等设置，会屏蔽唤醒识别功能
	 * 			若要禁用唤醒词，推荐使用setWakeupKeywordsNew(null)
	 *
	 * @param enable 是否启用语音唤醒
	 */
	public void enableWakeup(boolean enable) {
		mEnableWakeup = enable;
		if (enable) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.wakeup.start", null, null);
		} else {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.wakeup.stop", null, null);
		}
	}


	/**
	 * 接口名：设置界面配置项监听器
	 * 接口描述：设置界面相关配置改变时，通过此接口通知外部
	 */
	public static interface UIConfigListener extends ConfigListener {
		/**
		 * 方法名：当配置发生改变时
		 * 方法描述：配置项发生改变时回调此方法
		 *
		 * @param data 改变的配置项数据
		 */
		@Override
		void onConfigChanged(String data);
	}

	private UIConfigListener mUIConfigListener;

	/**
	 * 方法名：设置UI配置项监听器
	 * 方法描述：设置UI配置项监听器，设置界面相关配置改变时，会收到相应回调
	 *
	 * @param listener 配置项监听器
	 */
	public void setUIConfigListener(UIConfigListener listener) {
		if (mUIConfigListener != null) {
			ConfigUtil.unregisterConfigListener(mUIConfigListener);
		}
		if (listener != null) {
			ConfigUtil.registerConfigListener(listener);
		}
		mUIConfigListener = listener;
	}

	/**
	 * 接口名：用户设置项监听器
	 * 接口描述：用户通过声控修改配置时，会回调此监听器通知外部
	 */
	public static interface UserConfigListener {
		/**
		 * 方法名：当修改唤醒词
		 * 方法描述：当用户通过语音修改唤醒词时，会回调此方法
		 *
		 * @param keywords 新的唤醒词
		 */
		public void onChangeWakeupKeywords(String[] keywords);

		/**
		 * 方法名：当修改语音反馈语风格
		 * 方法描述：当用户修改语音交流风格时，回调此方法
		 *
		 * @param style 新的风格
		 */
		public void onChangeCommunicationStyle(String style);
	}

	/**
	 * 接口名：设置用户配置监听器
	 * 接口描述：用户通过声控修改配置时，会回调此监听器的方法
	 */
	static class UserConfigCommandProcessor implements CommandProcessor {
		public UserConfigListener mUserConfigListener = null;

		@Override
		public byte[] process(String packageName, String command, byte[] data) {
			try {
				if ("onChangeWakeupKeywords".equals(command)) {
					if (mUserConfigListener != null) {
						JSONBuilder json = new JSONBuilder(new String(data));
						mUserConfigListener.onChangeWakeupKeywords(json.getVal(
								"kws", String[].class));
					}
					return null;
				}
				if ("onChangeCommunicationStyle".equals(command)) {
					if (mUserConfigListener != null) {
						mUserConfigListener
								.onChangeCommunicationStyle(new String(data));
					}
					return null;
				}
			} catch (Exception e) {
			}
			return null;
		}
	};

	UserConfigCommandProcessor mUserConfigCommandProcessor = new UserConfigCommandProcessor();

	/**
	 * 方法名：设置用户设置监听器
	 * 方法描述：手动设置用户设置监听器
	 *
	 * @param listener 用户配置监听器
	 */
	public void setUserConfigListener(final UserConfigListener listener) {
		CommandProcessor p = null;
		mUserConfigCommandProcessor.mUserConfigListener = listener;
		if (listener != null) {
			p = mUserConfigCommandProcessor;
		}
		TXZService.setCommandProcessor("userconfig.", p);
	}

	Boolean mEnableChangeWakeupKeywords = null;

	/**
	 * 方法名：是否允许用户修改唤醒词
	 * 方法描述：是否允许用户修改唤醒词，默认允许
	 *
	 * @param enable 是否需要允许
	 */
	public void enableChangeWakeupKeywords(boolean enable) {
		mEnableChangeWakeupKeywords = enable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.wakeup.enableChangeWakeupKeywords",
				("" + enable).toString().getBytes(), null);
	}

	Boolean mEnableQueryTicket = null;

	/**
	 * 方法名：是否启用机票查询的功能
	 * 方法描述：是否需要使用语音查询机票功能，启用此功能需要联系同行者对应支持人员，默认不启用
	 *
	 * @param enable 是否启用此功能
	 */
	public void enableQueryTrafficTicket(boolean enable){
		mEnableQueryTicket = enable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.config.enable.ticket",
				(enable + "").getBytes(), null);
	}

	private Map<String, Integer> mLogLevelMap = new ConcurrentHashMap<String, Integer>();

	/**
	 * 方法名：设置指定包的日志等级
	 * 方法描述：设置语指定包输出到控制台的日志等级，高于或等于此等级的日志才会输出到控制台（Logcat）
	 * 			 具体等级参考android Log类
	 *
	 * @param packageName 需要修改等级的包名，包括语音、同听、微信助手
	 * @param level       日志等级，使用Log的常量
	 */
	public void setLogLevel(String packageName, int level) {
		mLogLevelMap.put(packageName, level);
		ServiceManager.getInstance().sendInvoke(packageName,
				"comm.log.setConsoleLogLevel", ("" + level).getBytes(), null);
	}

	/**
	 * 方法名：设置日志等级
	 * 方法描述：设置语音相关程序输出到控制台的日志等级，高于或等于此等级的日志才会输出到控制台（Logcat）
	 * 			 具体等级参考android Log类
	 *
	 * @param level
	 *            日志等级，使用Log的常量
	 */
	public void setLogLevel(int level) {
		LogUtil.setConsoleLogLevel(level);
		setLogLevel(ServiceManager.TXZ, level);
		setLogLevel(ServiceManager.BT, level);
		setLogLevel(ServiceManager.NAV, level);
		setLogLevel(ServiceManager.MUSIC, level);
		setLogLevel(ServiceManager.RECORD, level);
		setLogLevel(ServiceManager.WEBCHAT, level);
	}

	private Map<String, Integer> mFileLogLevelMap = new ConcurrentHashMap<String, Integer>();

	/**
	 * 方法名：设置指定包的文件日志等级
	 * 方法描述：当开启日志保存功能时，设置指定包的输出到文件日志等级
	 * 			高于或等于此等级的日志才会输出到文件/sdcard/txz/log
	 * 			具体等级参考android Log类
	 *
	 * @param packageName 包名
	 * @param level       日志等级，使用Log的常量
	 */
	public void setFileLogLevel(String packageName, int level) {
		mFileLogLevelMap.put(packageName, level);
		ServiceManager.getInstance().sendInvoke(packageName,
				"comm.log.setFileLogLevel", ("" + level).getBytes(), null);
	}


	/**
	 * 方法名：设置文件日志等级
	 * 方法描述：当开启日志保存功能时，设置语音相关程序的输出到文件日志等级
	 * 			高于或等于此等级的日志才会输出到文件/sdcard/txz/log
	 * 			具体等级参考android Log类
	 *
	 * @param level 日志等级，使用Log的常量
	 */
	public void setFileLogLevel(int level) {
		LogUtil.setFileLogLevel(level);
		setFileLogLevel(ServiceManager.TXZ, level);
		setFileLogLevel(ServiceManager.BT, level);
		setFileLogLevel(ServiceManager.NAV, level);
		setFileLogLevel(ServiceManager.MUSIC, level);
		setFileLogLevel(ServiceManager.RECORD, level);
		setFileLogLevel(ServiceManager.WEBCHAT, level);
	}

	/**
	 * 方法名：是否在语音助手界面显示帮助信息
	 * 方法描述：是否启用语音界面的帮助图标显示功能，默认启用
	 *
	 * @param show true=显示，false=隐藏
	 */
	public void showHelpInfos(boolean show) {
		ConfigUtil.setShowHelpInfos(show);
		ConfigUtil.sendConfigs();
	}

	/**
	 * 方法名：是否在语音助手界面显示关闭图标
	 * 方法描述：是否启用语音助手界面左上角的“关闭”图标，语音3.0以上版本默认不启用
	 */
	public void enableCloseWin(boolean enable) {
		LogUtil.logd("TXZConfigManager::enableCloseWin, enable=" + enable);
		ConfigUtil.setShowCloseIcon(enable);
		ConfigUtil.sendConfigs();
	}

	/**
	 * 方法名：设置唤醒词的唤醒阀值
	 * 方法描述：设置唤醒词的唤醒阀值,建议值为 -2.7f 到 -3.1f, 分数越低，越容易唤醒，但是误唤醒率越高。
	 *
	 * @param threshHold 唤醒阈值
	 */
	public void setWakeupThreshhold(float threshHold) {
		if (null != mInitParam) {
			mInitParam.setWakeupThreshhold(threshHold);
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.wakeup.set.wkscore",
				("" + threshHold).toString().getBytes(), null);
	}

	/**
	 * 方法名：设置免唤醒词阈值
	 * 方法描述：设置POI选择界面和联系人选择界面等界面下的识别唤醒词的唤醒阀值
	 * 			 建议值为 -2.7f 到 -3.5f 分数越低，越容易唤醒，但是误唤醒率越高。
	 *
	 * @param threshHold 免唤醒词阈值
	 */
	public void setAsrWakeupThreshhold(float threshHold) {
		if (null != mInitParam) {
			mInitParam.setAsrWakeupThreshhold(threshHold);
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.wakeup.set.asrwkscore",
				("" + threshHold).toString().getBytes(), null);
	}

	/**
	 * 方法名：设置界面重置参数
	 * 方法描述：设置界面重置参数，设置此参数后，则使用修改后的默认参数
	 *
	 * @param jsonConfig 重置参数JSON，格式形如以下：
	 *                   {"wakeupThreshold":-3.1f; "voiceSpeed":70
	 *                   "wakeupKeywords":["你好小踢";"小踢你好"] }
	 */
	public boolean setDefaultConfig(String jsonConfig) {
		if (jsonConfig != null) {
			try {
				JSONObject data = new JSONObject(jsonConfig);
				if (!data.has("wakeupThreshold") || !data.has("voiceSpeed")
						|| !data.has("wakeupKeywords")) {
					throw new RuntimeException(jsonConfig
							+ " is not a valid config msg");
				}
				ConfigUtil.setDefaultConfig(new JSONObject(jsonConfig));
			} catch (JSONException e) {
				throw new RuntimeException(jsonConfig
						+ " is not a valid config msg");
			}
		} else {
			ConfigUtil.setDefaultConfig(null);
		}
		return true;
	}

	String mVersionConfig;

	/**
	 * 方法名：设置版本配置参数
	 * 方法描述：配置版本参数，已过时，不推荐使用此方法
	 *
	 * @param jsonConfig 配置参数JSON
	 * @deprecated
	 */
	@Deprecated
	public void setVersionConfig(String jsonConfig) {
		mVersionConfig = jsonConfig;
		if (!TextUtils.isEmpty(jsonConfig)) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.config.version.prefer", jsonConfig.getBytes(), null);
		}
	}

	ConfigJsonKey[] keys;
	Object[] configs;

	/**
	 * 方法名：设置UI参数配置
	 * 方法描述：已过时，不推荐使用
	 *
	 * @param vals 对应设置内容
	 * @param keys JSON key值
	 */
	@Deprecated
	public void setPreferenceConfig(Object[] vals, ConfigJsonKey... keys)
			throws IllegalAccessException {
		if (vals == null || keys == null || vals.length != keys.length) {
			throw new IllegalAccessException(
					"ConfigJsonKey and values should be same count ,or should not null!");
		}

		this.keys = keys;
		this.configs = vals;
		if (this.keys != null) {
			JSONBuilder jb = new JSONBuilder();
			int index = 0;
			for (ConfigJsonKey cjk : this.keys) {
				jb.put(cjk.name(), vals[index++]);
			}

			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.config.version.prefer", jb.toBytes(), null);
		}
	}

	long mFinishDelay;

	/**
	 * 方法名：设置导航搜索结果页是否自动消失
	 * 方法描述：设置导航搜索结果页是否自动消失,小于1000时不自动消失，单位ms
	 *
	 * @param finishDelay 消失延时
	 * @deprecated 已过时，不推荐使用{@link #mInitParam#setDialogTimeOut}
	 */
	@Deprecated
	public void setPoiSearchActivityFinishDelay(long finishDelay) {
		mFinishDelay = finishDelay;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.poi.finish", (mFinishDelay + "").getBytes(), null);
	}

	long mStartNavDelayFinish = -1;

	/**
	 * 方法名：设置导航搜索结果页出发后自动消失
	 * 方法描述：设置导航搜索结果页出发后自动消失,小于1000时不自动消失，单位ms
	 *
	 * @param finishDelay 消失延时
	 * @deprecated 已过时，不推荐使用{@link #mInitParam#setDialogTimeOut}
	 */
	@Deprecated
	public void setPoiSearchActivityStartNavFinishDelay(long finishDelay) {
		mStartNavDelayFinish = finishDelay;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.poi.afterStartNav.finish",
				(mStartNavDelayFinish + "").getBytes(), null);
	}

	/**
	 * 方法名：设置声控界面列表选择的超时时间
	 * 方法描述：设置声控界面列表选择的超时时间，超过时间自动发起导航
	 *
	 * @param delay 超时时间，单位ms
	 */
	public void setSelectListTimeout(long delay){
		mFinishDelay = delay;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.poi.finish", (mFinishDelay + "").getBytes(), null);
	}

	long mDismissDelay;

	/**
	 * 方法名：设置Tts选择对话框超时时间
	 * 方法描述：Tts选择对话框不操作delay后自动关闭，小于1000不关闭
	 *
	 * @param delay 超时时间
	 */
	public void setConfirAsrWinDismissDelay(long delay) {
		mDismissDelay = delay;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.wx.dismiss", (mDismissDelay + "").getBytes(), null);
	}

    int mShowCount;

	/**
	 * 方法名：设置显示列表条目数
	 * 方法描述：设置语音列表展示的最大显示Item数
	 *
	 * @param count Item数
	 * @deprecated 已过时，不推荐使用，推荐使用初始化参数进行设置
	 */
	@Deprecated
	public void setDisplayLvCount(int count) {
		mShowCount = count;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.selector.show.count", (mShowCount + "").getBytes(), null);
	}

	/**
	 * 方法名：设置Poi搜索的个数
	 * 方法描述：设置POI发起搜索最大结果数，默认8个
	 *
	 * @param count 最大结果数
	 */
    public void setPoiSearchCount(int count){
    	mShowCount = count;
    	ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.selector.show.count", (mShowCount + "").getBytes(), null);
    }

    int mPagingCount;

	/**
	 * 方法名：设定分页基准数
	 * 方法描述：设置分页基准数，即最大一页显示Item数量
	 *
	 * @param count 分页基准数
	 */
	public void setPagingBenchmarkCount(int count) {
		this.mPagingCount = count;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.record.ui.event.setDisplayCount",
				(mPagingCount + "").getBytes(), null);
	}

	ConcurrentHashMap<PageType,Integer> mPagingCountMap = new ConcurrentHashMap<PageType, Integer>();
	ConcurrentHashMap<PageType,Long> mPagingTimeoutMap = new ConcurrentHashMap<PageType, Long>();

	/**
	 * 方法名：设定分页基准数
	 * 方法描述：针对不同页面类型，设置分页基准数，即最大一页显示Item数量
	 *
	 * @param page  页面类型
	 * @param count 分页基准数
	 */
	public void setPagingBenchmarkCount(PageType page, int count) {
		mPagingCountMap.put(page, count);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.record.ui.event.setDisplayCount2",
				new JSONBuilder().put("page",page.name()).put("count",count).toBytes(), null);
	}

	/**
	 * 单独设置各个列表的超时时间，优先级比用户默认配置高
	 *
	 * @param timeout
	 */
	public void setPageTimeout(PageType page, long timeout) {
		mPagingTimeoutMap.put(page, timeout);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.record.ui.event.setDisplayTimeout",
				new JSONBuilder().put("page",page.name()).put("timeout",timeout).toBytes(), null);
	}

	/**
	 * 枚举类名：页面类型
	 * 枚举类描述：语音展示列表页的不同类型
	 */
	public static enum PageType {

		/**
		 * poi列表界面
		 */
		PAGE_TYPE_POI_LIST,
		/**
		 * poi地图模式列表界面
		 */
		PAGE_TYPE_POI_MAP_LIST,
		/**
		 * 商圈列表模式界面
		 */
		PAGE_TYPE_POI_BUSSINESS_LIST,

		/**
		 * 商圈地图模式界面
		 */
		PAGE_TYPE_POI_BUSSINESS_MAP_LIST,
		/**
		 * 联系人界面
		 */
		PAGE_TYPE_CALL_LIST,
		/**
		 * 微信界面
		 */
		PAGE_TYPE_WECHAT_LIST,
		/**
		 * 音乐界面
		 */
		PAGE_TYPE_AUDIO_LIST,
		/**
		 * 带标签的电子书界面
		 */
		PAGE_TYPE_AUDIO_WITH_TAG,
		/**
		 * 流量充值界面
		 */
		PAGE_TYPE_SIM_LIST,
		/**
		 * tts主题界面
		 */
		PAGE_TYPE_TTS_LIST,
		/**
		 * 帮助列表界面
		 */
		PAGE_TYPE_HELP_LIST,
		/**
		 * 帮助详细列表界面
		 */
		PAGE_TYPE_HELP_DETAIL_LIST,
//		PAGE_TYPE_HELP_IMAGE_DETAIL_LIST,
		/**
		 *电影界面
		 */
		PAGE_TYPE_MOVIE_LIST,

		/**
		 * 提醒界面
		 */
		PAGE_TYPE_REMINDER_LIST,
		/**
		 * 机票界面
		 */
		PAGE_TYPE_FLIGHT_LIST,
		/**
		 * 火车票界面
		 */
		PAGE_TYPE_TRAIN_LIST,
		/**
		 * 主题样式界面
		 */
		PAGE_TYPE_STYLE_LIST,
		/**
		 * 导航历史
		 */
		PAGE_TYPE_NAV_HISTORY_LIST,
		/**
		 * 导航历史地图模式
		 */
		PAGE_TYPE_NAV_HISTORY_MAP_LIST,

		/**
		 * 导航app列表界面
		 */
		PAGE_TYPE_NAV_APP_LIST,

		/*
		* 电影票电影选择列表
		* */
		PAGE_TYPE_FILM_LIST,
		/*
		* 电影票电影院选择列表
		* */
		PAGE_TYPE_MOVIE_THEATER_LIST,
		/*
		* 电影票电影场次选择列表
		* */
		PAGE_TYPE_MOVIE_TIMES_LIST,

		/**
		 * 赛事的选择列表
		 */
		PAGE_TYPE_COMPETITION_LIST,
		/**
		 * 桑德的车控家列表
		 */
		 PAGE_TYPE_MI_HOME_LIST,

         /**
		 *齐悟火车票选择列表
		 */
		PAGE_TYPE_TRAIN_TICKET_LIST,

		/**
		 *齐悟飞机票选择列表
		 */
		PAGE_TYPE_FLIGHT_TICKET_LIST,
		/**
		 * 齐悟票务支付列表
		 * **/
		PAGE_TYPE_TICKET_PAY_LIST


	}




	int mMoviePagingCount;

	/**
	 * 方法名：设定电影页分页基准数
	 * 方法描述：设置分页基准数，即最大一页显示Item数量
	 *
	 * @param count 分页基准数
	 */
	public void setMoviePagingBenchmarkCount(int count) {
		this.mMoviePagingCount = count;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.record.ui.event.setMovieDisplayCount",
				(mMoviePagingCount + "").getBytes(), null);
	}

	Boolean mBanSelectAsr = null;

	/**
	 * 方法名：选择列表是否启用唤醒词开关
	 * 方法描述：选择列表是否启用唤醒词开关，如果关掉，则不能语音喊“第一个”“下一页”等，默认启用
	 *
	 * @param isBanAsr 是否需要唤醒词
	 */
	public void setBanSelectListAsr(boolean isBanAsr) {
		this.mBanSelectAsr = isBanAsr;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.toggleWp",
				(isBanAsr + "").getBytes(), null);
	}

	/**
	 * 方法名：设置Beep音超时时间
	 * 方法描述：语音录音前Beep音在部分车机切通道时会被漏掉，通过延时处理
	 *
	 * @param timeOut Beep音超时时间, 单位毫秒， 建议值 100ms - 500ms
	 */
	public void setBeepTimeOut(int timeOut) {
		if (null != mInitParam) {
			mInitParam.setBeepTimeOut(timeOut);
		}
		String cmd = "comm.asr.set.beeptimeout";
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd,
				("" + timeOut).toString().getBytes(), null);
	}

	Integer mMaxEmpty = null;

	/**
	 * 方法名：设置聊天模式下最多几次不说话退出声控
	 * 方法描述：设置聊天模式下最多几次不说话退出声控，默认为1次
	 *
	 * @param count 最大次数
	 */
	public void setChatMaxEmpty(int count) {
		mMaxEmpty = count;
		String cmd = "comm.asr.set.MaxEmpty";
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd,
				("" + count).toString().getBytes(), null);
	}

	Integer mMaxUnknow = null;

	/**
	 * 方法名：设置聊天模式下最多几次不可识别退出声控
	 * 方法描述：设置聊天模式下最多几次不可识别退出声控，默认3次
	 *
	 * @param count 最大次数
	 */
	public void setChatMaxUnknow(int count) {
		mMaxUnknow = count;
		String cmd = "comm.asr.set.MaxUnknow";
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd,
				("" + count).toString().getBytes(), null);
	}

	/**
	 * 方法名：设置识别模式
	 * 方法描述：设置识别模式，包括混合模式, 纯离线, 纯在线, 自动模式。
	 * 			默认混合模式，暂不支持设置纯离线和纯在线。
	 * 			由于离线占用本地资源，低配置机器可选择自动模式。
	 *
	 * @param asrServiceMode 识别模式。
	 */
	public void setAsrServiceMode(AsrServiceMode asrServiceMode) {
		if (null != mInitParam) {
			mInitParam.setAsrServiceMode(asrServiceMode);
		}
		if (null == asrServiceMode) {
			asrServiceMode = AsrServiceMode.ASR_SVR_MODE_MIX;
		}
		String cmd = "comm.asr.set.asrsrvmode";
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd,
				asrServiceMode.name().getBytes(), null);
	}

	/**
	 * 方法名：设置开启回音消除时,是否强制停掉唤醒当播报TTS的时候
	 * 方法描述：默认不强制停止
	 *
	 * @param force ：true 强制停止， false不停止。
	 */
	public void forceStopWkWhenTts(boolean force) {
		if (null != mInitParam){
			mInitParam.forceStopWkWhenTts(force);
		}
		String cmd = "txz.wakeup.forceStopWkWhenTts";
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd, ("" + force).getBytes(), null);
	}

	Set<String> mSetDisableChangeWakeupKeywordsStyle = new HashSet<String>();

	/**
	 * 方法名：设置禁用风格设置说法
	 * 方法描述：设置禁用风格设置说法，当前仅支持“king”（宫廷风）
	 *
	 * @param style 风格字符串，如king
	 */
	public void disableChangeWakeupKeywordsStyle(String style) {
		String[] ss;
		synchronized (mSetDisableChangeWakeupKeywordsStyle) {
			if (style != null) {
				mSetDisableChangeWakeupKeywordsStyle.add(style);
			}
			if (mSetDisableChangeWakeupKeywordsStyle.isEmpty()) {
				return;
			}
			ss = mSetDisableChangeWakeupKeywordsStyle
					.toArray(new String[mSetDisableChangeWakeupKeywordsStyle
							.size()]);
		}
		JSONBuilder json = new JSONBuilder();
		json.put("style", ss);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.config.disableChangeWakeupKeywordsStyle", json.toBytes(),
				null);

	}

	public Integer mHideSettingOptions = null;

	/**
	 * 方法名：隐藏设置页的某些选项
	 * 方法描述：启用设置后，可以选择性开启对应功能
	 * 			设置页只有在TXZConfigManager.getInstance().enableSettings(true)时才会显示
	 *
	 * @param voiceWake   是否隐藏语音唤醒开关
	 * @param floatTool   是否隐藏悬浮窗开关
	 * @param wakeUpWords 是否隐藏唤醒词选项
	 * @param sensitivity 是否隐藏灵敏度选项
	 * @param ttsSpeed    是否隐藏tts播报速度选项
	 * @param reset       是否隐藏重置选项
	 */
	public void hideSettingOptions(boolean voiceWake,boolean floatTool,boolean wakeUpWords
			,boolean sensitivity,boolean ttsSpeed,boolean reset){
		mHideSettingOptions = 0;
		mHideSettingOptions = voiceWake?mHideSettingOptions | 1<<0:mHideSettingOptions;
		mHideSettingOptions = floatTool?mHideSettingOptions | 1<<1:mHideSettingOptions;
		mHideSettingOptions = wakeUpWords?mHideSettingOptions | 1<<2:mHideSettingOptions;
		mHideSettingOptions = sensitivity?mHideSettingOptions | 1<<3:mHideSettingOptions;
		mHideSettingOptions = ttsSpeed?mHideSettingOptions | 1<<4:mHideSettingOptions;
		mHideSettingOptions = reset?mHideSettingOptions | 1<<5:mHideSettingOptions;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.config.setting.hideOptions", (""+mHideSettingOptions).getBytes(), null);
	}

	public Boolean mSettingWkWordsEditable = null;

	/**
	 * 方法名：是否启用设置唤醒词编辑功能
	 * 方法描述：是否启用外部设置唤醒词编辑功能，默认允许
	 *
	 * @param editable 是否允许编辑
	 */
	public void enableSettingWkWordsEditable(boolean editable){
		mSettingWkWordsEditable = editable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,"txz.config.setting.wkwordsEditable",
				("" + mSettingWkWordsEditable).getBytes(), null);
	}


	Boolean mSetStyleBindWithWakeupKeywords = null;

	/**
	 * 方法名：设置不同唤醒语走不同风格开关
	 * 方法描述：设置不同唤醒语走不同风格开关，默认关闭；如，你好小T是基础风格，召见杨贵妃响应为宫廷体
	 */
	public void setStyleBindWithWakeupKeywords(boolean bind) {
		mSetStyleBindWithWakeupKeywords = bind;
		JSONBuilder json = new JSONBuilder();
		json.put("bind", bind);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.config.setStyleBindWithWakeupKeywords", json.toBytes(),
				null);
	}

	/**
	 * 常量名：版本号
	 * 常量描述：同行者编译时间_编译版本号
	 */
	public final static String VERSION = "" + TXZVersion.PACKTIME + "_"
			+ TXZVersion.SVNVERSION;

	/**
	 * 方法名：设置是否启用唤醒大模型
	 * 方法描述：引擎识别、唤醒模型，默认关闭，开启后会增加CPU和内存消耗，推荐通过调整唤醒词灵敏度
	 *
	 * @param useHQualityWakeupModel 是否启用唤醒大模型
	 */
	public void setUseHQualityWakeupModel(boolean useHQualityWakeupModel) {
		if (null != mInitParam) {
			mInitParam.setUseHQualityWakeupModel(useHQualityWakeupModel);
		}
		String cmd = "comm.asr.set.useHQualityWakeupModel";
		JSONBuilder json = new JSONBuilder();
		json.put("useHQualityWakeupModel", useHQualityWakeupModel);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd,
				json.toBytes(), null);
	}

	/**
	 * 方法名：监听当前用户唤醒词修改
	 * 方法描述：监听后，当用户自定义唤醒词时，会收到对应唤醒词回调
	 *
	 * @param callback 回调
	 */
	public void getUserWakeupKeywords(final UserKeywordsCallback callback){
		if (callback == null) {
			return;
		}
		ServiceData data = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ, "txz.wakeup.getkeywords", null);

		if (data!=null) {
			String json = data.getString();
			if (TextUtils.isEmpty(json)) {
				callback.result(null);
				return;
			}
			try {
				JSONObject job = new JSONObject(json);
				if (job.has("keywords")) {
					String keywords = job.getString("keywords");
					JSONArray jry = new JSONArray(keywords);
					String[] strKeywords = new String[jry.length()];
					for (int i = 0; i < strKeywords.length; i++) {
						strKeywords[i] = jry.getString(i);
					}
					LogUtil.logd("leng keywords json::" + keywords);
					callback.result(strKeywords);
					return;
				}
			} catch (JSONException e) {
			}
			callback.result(null);
		}
	}

	private boolean mEnableRecording = true;

	/**
	 * 方法名：录音功能是否可用
	 * 方法描述：是否需要禁用同行者录音，实时生效，特殊情况下需要暂停语音录音焦点时使用
	 *
	 * @param enable 是否启用录音,true 启用 false 禁用
	 */
	public void setEnableRecording(boolean enable) {
		mEnableRecording = enable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,"txz.config.enableRecording",
				("" + mEnableRecording).getBytes(), null);
	}

	/**
	 * 接口名：用户修改唤醒词监听器
	 * 接口描述：需要监听用户修改唤醒时使用
	 */
	public static interface UserKeywordsCallback {
		/**
		 * 方法名：结果返回
		 * 方法描述：用户修改唤醒时，回调此方法
		 *
		 * @param result 唤醒词
		 */
		public void result(String[] result);
	}

	private String mInterruptText = null;

	/**
	 * 方法名：设置长文本打断词
	 * 方法描述：设置长文本打断词。语音播放长文本时，默认以“停止播报”打断，或者通过唤醒词打断
	 * 			设置此接口时，会替换“停止播报”。长文本包括“讲个笑话”、“红烧肉怎么做”等场景
	 *
	 * @param text 打断词文本，2 - 4字，推荐4个字
	 */
	public void setInterruptTips(String text){
		if(TextUtils.isEmpty(text)){
			return;
		}
		mInterruptText = text;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.config.setInterruptTips", text.getBytes(), null);
	}

	private String[] mInterruptTextArr = null;
	/**
	 * 方法名：设置长文本打断词
	 * 方法描述：设置长文本打断词。语音播放长文本时，默认以“停止播报”打断，或者通过唤醒词打断
	 * 			设置此接口时，会替换“停止播报”。长文本包括“讲个笑话”、“红烧肉怎么做”等场景
	 *
	 * @param arr 打断词文本数组，2 - 4字，推荐4个字
	 */
	public void setInterruptTips(String[] arr){
		if (null != arr && arr.length > 0) {
			mInterruptTextArr = arr;
			JSONArray jsonArray = new JSONArray();
			for (String s : arr) {
				if (!TextUtils.isEmpty(s)) {
					jsonArray.put(s);
				}
			}
			if (jsonArray.length() > 0) {
				ServiceManager.getInstance()
						.sendInvoke(ServiceManager.TXZ, "txz.config.setInterruptTipsArr",
								jsonArray.toString().getBytes(), null);
			}
		}
	}

	private Boolean mEnableInterruptTips = null;
	public void enableInterruptTips(boolean enable) {
		mEnableInterruptTips = enable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.config.enableInterruptTips", (mEnableInterruptTips+"").getBytes(), null);
	}

	private Boolean mNeedHelpFloat = null;

	/**
	 * 方法名：设置是否需要显示浮窗帮助
	 * 方法描述：设置是否需要显示浮窗帮助，默认不启用
	 *
	 * @param needFloat 是否启用帮助悬浮窗
	 */
	public void setNeedHelpFloat(boolean needFloat) {
		mNeedHelpFloat = needFloat;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.config.setNeedHelpFloat",
				(needFloat + "").getBytes(), null);
	}

	private Boolean isNeedGuideAnim;

	/**
	 * 方法名：设置是否需要新手引导动画
	 * 方法描述：设置是否需要新手引导动画，默认不启用，针对新用户提示语音相关使用操作
	 *
	 * @param needAnim 是否需要
	 */
	public void setIsNeedGuideAnim(boolean needAnim) {
		isNeedGuideAnim = needAnim;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.config.isNeedGuideAnim", ("" + needAnim).getBytes(), null);
	}

	private Boolean isNeedSearchTts;

	/**
	 * 方法名：设置搜索的时候是否需要播报正在搜索
	 * 方法描述：设置搜索的时候是否需要播报“正在搜索，请稍后”的提示，默认不启用
	 *
	 * @param needTts 是否启用播报
	 */
	public void setNeedBlockSearchTipTts(boolean needTts) {
		isNeedSearchTts = needTts;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.config.isNeedBlockSearchTts", ("" + needTts).getBytes(), null);
	}

	/**
	 * 方法名：获取是否打开自营销开关
	 * 方法描述：获取是否打开同行者自营销开关。自营销为同行者新手引导类功能，默认关闭
	 *
	 * @return 自营销开关状态
	 */
	public boolean getEnableSelfMarkting(){
		ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ, "txz.config.getEnableSelfMarkting", null);
		Boolean enable = serviceData.getBoolean();
		if(enable != null){
			return enable;
		}
		return false;
	}

    /**
     * 方法名：获取是否设置了问候语
	 * 方法描述：获取是否设置问候语，即开机时语音初始化成功后的播报语句，可自定义，默认无问候语。
	 *
     * @return 问候语状态
     */
	public boolean hasDefaultWelcomeMessage() {
		ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ, "txz.config.getHasDefaultWelcomeMessage", null);
		Boolean enable = serviceData.getBoolean();
		if (enable != null) {
			return enable;
		}
		return false;
	}

	/**
	 * 判断launcher是否开启注册。（只提供给launcher使用）
	 *
	 * @return true表示开启。
	 */
	public boolean getLauncherEnableRegister() {
		ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ, "txz.config.getLauncherEnableRegister", null);
		if (serviceData == null) {
			return false;
		}
		Boolean enable = serviceData.getBoolean();
		if (enable != null) {
			return enable;
		}
		return false;
	}

	/**
	 * 判断launcher是否要支持修改车辆信息。（只提供给launcher使用）
	 *
	 * @return true表示开启。
	 */
	public boolean getLauncherEnableModifyVehicleInfo() {
		ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ, "txz.config.getLauncherEnableModifyVehicleInfo", null);
		if (serviceData == null) {
			return false;
		}
		Boolean enable = serviceData.getBoolean();
		if (enable != null) {
			return enable;
		}
		return false;
	}

	/**
	 * 方法名：停止新闻播报
	 * 方法描述：同行者新闻播报时，可以通过此方法强行关闭
	 */
	public void stopNews() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.news.stop",
				null, null);
	}

	/**
	 * 是否开启声纹识别功能
	 */
	public boolean isVoiceprintRecognitionEnable() {
		ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ, "txz.config.isVoiceprintRecognitionEnable", null);
		if (serviceData == null) {
			return false;
		}
		Boolean enable = serviceData.getBoolean();
		if (enable != null) {
			return enable;
		}
		return false;
	}

	/**
	 * 判断是否支持停止播报功能
	 * @return Boolean 为null时属于获取失败
	 */
	public Boolean isSupportInterruptTips(){
		ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ, "txz.config.isSupportInterruptTips", null);
		if (serviceData == null) {
			return null;
		}
		Boolean enable = serviceData.getBoolean();
		if (enable != null) {
			return enable;
		}
		return null;
	}

	private Boolean mEnableShowHelpQRCode;

	/**
	 * 设置是否开启帮助界面二维码
	 * @param enableShowHelpQRCode
	 */
	public void setEnableShowHelpQRCode(boolean enableShowHelpQRCode){
		mEnableShowHelpQRCode = enableShowHelpQRCode;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.config.enableShowHelpQRCode", ("" + enableShowHelpQRCode).getBytes(), null);
	}

	private Boolean mNeedSetIntentPackage;

	/**
	 * 是否设置Intent package
	 * @param needSetIntentPackage
	 */
	public void setNeedSetIntentPackage(boolean needSetIntentPackage){
		mNeedSetIntentPackage = needSetIntentPackage;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.config.needSetIntentPackage", ("" + needSetIntentPackage).getBytes(), null);
	}

	private Boolean mNeedShowOfflinePromote;

	public void setNeedShowOfflinePromote(boolean needShow){
		mNeedShowOfflinePromote = needShow;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.config.needShowOfflinePromote", ("" + needShow).getBytes(), null);

	}

	/**
	 * 走唤醒引擎命中的唤醒任务
	 */
	public static final int WAKEUP_NOTIFY_FLAG_TASK = 0x01;
	/**
	 * 走唤醒引擎命中的主唤醒词
	 */
	public static final int WAKEUP_NOTIFY_FLAG_LAUNCH = 0x02;
	/**
	 * 走唤醒引擎命中的唤醒场景拦截
	 */
	public static final int WAKEUP_NOTIFY_FLAG_SCREEN = 0x04;
	/**
	 * 走唤醒引擎命中的oneshot唤醒场景
	 */
	public static final int WAKEUP_NOTIFY_FLAG_ONESHOT = 0x08;

	/**
	 * 走识别引擎命中的唤醒任务
	 */
	public static final int WAKEUP_NOTIFY_FLAG_ASR_TASK = 0x10;
	/**
	 * 走识别引擎命中的主唤醒词
	 */
	public static final int WAKEUP_NOTIFY_FLAG_ASR_LAUNCH = 0x20;
	/**
	 * 走识别引擎命中的唤醒场景拦截
	 */
	public static final int WAKEUP_NOTIFY_FLAG_ASR_SCREEN = 0x40;

	/**
	 * 通知所有命中的唤醒词，包括唤醒引擎和识别引擎命中的
	 */
	public static final int WAKEUP_NOTIFY_FLAG_ALL = WAKEUP_NOTIFY_FLAG_TASK | WAKEUP_NOTIFY_FLAG_LAUNCH | WAKEUP_NOTIFY_FLAG_SCREEN | WAKEUP_NOTIFY_FLAG_ONESHOT | WAKEUP_NOTIFY_FLAG_ASR_TASK | WAKEUP_NOTIFY_FLAG_ASR_LAUNCH | WAKEUP_NOTIFY_FLAG_ASR_SCREEN;
	/**
	 * 通知所有通过唤醒引擎命中的唤醒词，不包含识别引擎命中的结果
	 */
	public static final int WAKEUP_NOTIFY_FLAG_ALL_WAKEUP = WAKEUP_NOTIFY_FLAG_TASK | WAKEUP_NOTIFY_FLAG_LAUNCH | WAKEUP_NOTIFY_FLAG_SCREEN | WAKEUP_NOTIFY_FLAG_ONESHOT ;
	/**
	 * 通知所有通过识别引擎命中的唤醒词，不包含唤醒引擎命中的结果
	 */
	public static final int WAKEUP_NOTIFY_FLAG_ALL_ASR = WAKEUP_NOTIFY_FLAG_ASR_TASK | WAKEUP_NOTIFY_FLAG_ASR_LAUNCH | WAKEUP_NOTIFY_FLAG_ASR_SCREEN;

	private Integer mNotifyOnWakeupFlags = null;

	/**
	 * 设置标志位，开启唤醒词命中后发送广播通知 <br>
	 * 广播的action: <br>
	 * 1. action --- com.txznet.txz.onWakeup<br>
	 * 携带的字段: <br>
	 * 1. keyword --- 命中的唤醒词<br>
	 * 2. score ---  唤醒的分数，如果是识别的结果，分数为100<br>
	 * 3. type --- 返回的唤醒词类型<br>
	 *
	 * @param flags 需要发送广播的唤醒词标志位<br>
	 *
	 *
	 * {@link #WAKEUP_NOTIFY_FLAG_TASK} 走唤醒引擎命中的唤醒任务 <br>
	 * {@link #WAKEUP_NOTIFY_FLAG_LAUNCH} 走唤醒引擎命中的主唤醒词 <br>
	 * {@link #WAKEUP_NOTIFY_FLAG_SCREEN} 走唤醒引擎命中的唤醒场景拦截 <br>
	 * {@link #WAKEUP_NOTIFY_FLAG_ONESHOT} 走唤醒引擎命中的oneshot唤醒场景 <br>
	 * {@link #WAKEUP_NOTIFY_FLAG_ASR_TASK} 走识别引擎命中的唤醒任务 <br>
	 * {@link #WAKEUP_NOTIFY_FLAG_ASR_LAUNCH} 走识别引擎命中的主唤醒词 <br>
	 * {@link #WAKEUP_NOTIFY_FLAG_ASR_SCREEN} 走识别引擎命中的唤醒场景拦截 <br>
	 * or<br>
	 * {@link #WAKEUP_NOTIFY_FLAG_ALL} 通知所有命中的唤醒词，包括唤醒引擎和识别引擎命中的 <br>
	 * {@link #WAKEUP_NOTIFY_FLAG_ALL_WAKEUP} 通知所有通过唤醒引擎命中的唤醒词，不包含识别引擎命中的结果 <br>
	 * {@link #WAKEUP_NOTIFY_FLAG_ALL_ASR} 通知所有通过识别引擎命中的唤醒词，不包含唤醒引擎命中的结果 <br>
	 */
	public void setNotifyOnWakeupFlags(int flags) {
		mNotifyOnWakeupFlags = flags;
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("notifyOnWakeupFlags", flags);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.wakeup.setNotifyOnWakeupFlags", jsonBuilder.toBytes(), null);
	}
}

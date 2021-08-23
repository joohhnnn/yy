package com.txznet.txz.cfg;

import java.io.File;

import com.txz.ui.equipment.UiEquipment;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.util.recordcenter.TXZAudioTrack;

import android.media.MediaRecorder;
import android.os.Environment;
import android.text.TextUtils;

public class ProjectCfg {
	/**
	 * 是否允许离线电话语法，不允许则离线不支持打电话
	 * 
	 * @return
	 */
	public static boolean enableOfflineCallGrammar() {
		if (PackageManager.getInstance().checkBluetoothModulerExist())
			return true;
		return false;
	}

	public static boolean DEBUG_MODE = false;
	
	public static boolean mCoexistAsrAndWakeup = false;
	
	public static boolean mEnableAEC = false;
	
	public static boolean mNeedRecoverVol = true;
	
	public static boolean mUseHQualityWakeupModel = false;

	// /////////////////////////////////

//	static String mYunzhishengAppId = "b25kmr3ztigd2m5i4qazswkm7uytykbwbsrflwqv";
//	static String mYunzhishengSecret = "b84a5a184b2ae88953caf53abeb6821a";
//	static String mIflyAppId = "548a6747";
	
	public static String getTencentAppkey() {
		return mTencentAppkey;
	}

	public static void setTencentAppkey(String mTencentAppkey) {
		ProjectCfg.mTencentAppkey = mTencentAppkey;
	}

	public static String getTencentToken() {
		return mTencentToken;
	}

	public static void setTencentToken(String mTencentToken) {
		ProjectCfg.mTencentToken = mTencentToken;
	}

	static String mTencentAppkey;//="2c336a52-0379-490e-88c3-de556d3426b5";
	static String mTencentToken;//="ed93bf06d2e64b84ac20ce91830d3d3e";
	static String mYunzhishengAppId ;
	static String mYunzhishengSecret ;
	static String mIflyAppId ;
	static Long mUid;
	static boolean mAddDefaultMusicType = false;
	static int mVoiceEngineType = 0;
	static int mVoiceBakEngineType = 0;
	static int mNlpEngineDisableType = 0;
	
	/**
	 * 针对v3开了aec情况下的误唤醒，产品定义做了一套交互
	 * 1、在选择第X个后，禁止打断
	 * 2、在播报有“确定”、“取消”的tts中，不响应“确定”、“取消”，播报完才支持
	 */
	private static boolean mAECPreventFalseWakeup = false;
	
	public static boolean getAECPreventFalseWakeup() {
		return mAECPreventFalseWakeup;
	}

	public static void setAECPreventFalseWakeup(boolean aecPreventFalseWakeup) {
		JNIHelper.logd("setAECPreventFalseWakeup = " +aecPreventFalseWakeup);
		ProjectCfg.mAECPreventFalseWakeup = aecPreventFalseWakeup;
	}
	
	public static int getVoiceBakEngineType() {
		return mVoiceBakEngineType;
	}

	public static void setVoiceBakEngineType(int mVoiceBakEngineType) {
		ProjectCfg.mVoiceBakEngineType = mVoiceBakEngineType;
	}

	public static int getNlpEngineDisableType() {
		return mNlpEngineDisableType;
	}

	public static void setNlpEngineDisableType(int mNlpEngineDisableType) {
		ProjectCfg.mNlpEngineDisableType = mNlpEngineDisableType;
	}

	public static Long getUid() {
		return mUid;
	}

	public static void setUid(long mUid) {
		ProjectCfg.mUid = mUid;
	}

	public static String getYunzhishengAppId() {
		return mYunzhishengAppId;
	}

	public static void setYunzhishengAppId(String mYunzhishengAppId) {
		ProjectCfg.mYunzhishengAppId = mYunzhishengAppId;
	}

	public static String getYunzhishengSecret() {
		return mYunzhishengSecret;
	}

	public static void setYunzhishengSecret(String mYunzhishengSecret) {
		ProjectCfg.mYunzhishengSecret = mYunzhishengSecret;
	}

	public static String getIflyAppId() {
		return mIflyAppId;
	}

	public static void setIflyAppId(String mIflyAppId) {
		ProjectCfg.mIflyAppId = mIflyAppId;
	}
	
	public static void setVoiceEngineType(int mVoiceEngineType) {
		ProjectCfg.mVoiceEngineType = mVoiceEngineType;
	}
	
	public static int getVoiceEngineType() {
		return ProjectCfg.mVoiceEngineType;
	}
	
	static boolean fixCallFunction = false;

	public static boolean isFixCallFunction() {
		return fixCallFunction;
	}

	public static void setFixCallFunction(boolean fixCallFunction) {
		ProjectCfg.fixCallFunction = fixCallFunction;
	}
	
	static String sDefaultNavTool = "";

	/**
	 * 出厂设置的默认导航
	 * @param navTool
	 */
	public static void setDefaultNavTool(String navTool) {
		JNIHelper.logd("setDefaultNavTool:" + navTool);
		sDefaultNavTool = navTool;
	}

	public static String getDefaultNavTool() {
		return sDefaultNavTool;
	}
	
	private static int sFilterNoiseType = 0;
	public static synchronized void setFilterNoiseType(int filterNoiseType){
		JNIHelper.logd("setNoiseType = " +filterNoiseType);
		sFilterNoiseType = filterNoiseType;
		if (sFilterNoiseType != 0){
			mEnableAEC = true;
		}
	}
	public static synchronized int getFilterNoiseType(){
		return sFilterNoiseType;
	}
    
	private static boolean sUseExtAudioSource = false;
	public static void useExtAudioSource(boolean useExternalAudioSource) {
		sUseExtAudioSource = useExternalAudioSource;
	}
	
	public static  boolean isUseExtAudioSource(){
		return sUseExtAudioSource;
	}
    
	private static boolean sEnableBlackHole = false;
	public static void enableBlackHole(Boolean enableBlackHole) {
		JNIHelper.logd("enableBlackHole = " + enableBlackHole);
		sEnableBlackHole = enableBlackHole;
	}
	public static boolean needBlackHole(){
		return sEnableBlackHole;
	}
	
	private static boolean sForceStopWkWhenTts = true;
	public static void forceStopWkWhenTts(boolean force){
		JNIHelper.logd("sForceStopWkWhenTts = " + force +" sFilterNoiseType = "+sFilterNoiseType);
		if(!force && !sEnableProtectWakeup && sFilterNoiseType == 0){//没开启保护的非回音消除设备开启不成功
			return;
		}
		sForceStopWkWhenTts = force;
	}
	
	public static boolean needStopWkWhenTts(){
		return sForceStopWkWhenTts;
	}
	
	private static boolean sEnableProtectWakeup = false;
	public static void enableProtectWakeup(boolean enable) {
		JNIHelper.logd("sEnableProtectWakeup = " + enable);
		sEnableProtectWakeup = enable;
		TXZAudioTrack.enableWriteBuffer(enable);
	}

	public static int getProtectWakeupType() {
		return sEnableProtectWakeup ? 2 : 0;
	}
	
	private static int sAudioSourceForRecord = MediaRecorder.AudioSource.DEFAULT;

	public static void setAudioSourceForRecord(int audioSource) {
		if (audioSource >= MediaRecorder.AudioSource.DEFAULT
				&& audioSource <= MediaRecorder.AudioSource.REMOTE_SUBMIX) {
			sAudioSourceForRecord = audioSource;
		}
		JNIHelper.logd("sAudioSourceForRecord = " + sAudioSourceForRecord + ", audioSource = " + audioSource);
	}
	
	public static int getAudioSourceForRecord(){
		return sAudioSourceForRecord;
	}
	
	private static boolean sRecognOnline = false;
	public static void setRecognOnline(boolean b){
		JNIHelper.logd("setRecognOnline  oldValue =  " + sRecognOnline + ", b = " + b);
		sRecognOnline = b;
	}
	
	public static boolean RecognOnline(){
		return sRecognOnline;
	}
    
	public static final int EXT_AUDIOSOURCE_TYPE_MSD = 0;
	public static final int EXT_AUDIOSOURCE_TYPE_TXZ = 1;
	private static int sExtAudioSourceType = EXT_AUDIOSOURCE_TYPE_MSD; //默认为MSD,主要是兼容美赛达
	public static void setExtAudioSourceType(Integer extAudioSourceType) {
		JNIHelper.logd("setExtAudioSourceType = " + extAudioSourceType);
		sExtAudioSourceType = extAudioSourceType;
	}
	public static int extAudioSourceType(){
		return sExtAudioSourceType;
	}
	public static String ext_audiosource_pkg = null;
	
	public static void setAddDefaultMusicType(boolean addDefaultMusicType) {
		mAddDefaultMusicType = addDefaultMusicType;
	}
	public static boolean getAddDefaultMusicType() {
		return mAddDefaultMusicType;
	}
	
	private static String getVoicePath(){
		String strPath = "";
		try {
			strPath = Environment.getExternalStorageDirectory().getPath() + "/txz/voice";
			// 创建.nomedia文件防止录音文件被其他播放器扫描到
			new File(strPath).mkdirs();
			String nomedia = strPath + "/.nomedia";
			new File(nomedia).createNewFile();
		} catch (Exception e) {
			
		}
		return strPath;
	}
	
	public static final String AUDIO_SAVE_PATH = getVoicePath();
	
	
	private static boolean sKeepRecorderOpened = false;//录音是否保持常开。默认不常开。
	public static void keepRecoderOpened(boolean bOpened){
		JNIHelper.logd("keepRecorderOpened : " +  bOpened);
		sKeepRecorderOpened = bOpened;
	}
	
	public static boolean isKeepingRecorderOpened(){
		return sKeepRecorderOpened;
	}
	
	private static boolean sNeedWinRecorder = true;//是否需要保持录音界面打开的时候，一直持有录音机
	public static boolean needWinRecorder(){
		return sNeedWinRecorder;
	}
	
	public static void enableNeedWinRecorder(boolean enable){
		JNIHelper.logd("needWinRecorder : " + enable);
		sNeedWinRecorder = enable;
	}
	
	public static String sStrSdkVersionInfo = null;
	
	public static boolean useLocalNetAsr = true;
	
	/**
	 * 是否保存明文的录音文件
	 * @return
	 */
	public static boolean enableSaveRawPCM(){
		if(DebugCfg.SAVE_RAW_PCM_CACHE){
			LogUtil.logd("save pcm true");
			return true;
		}
		UiEquipment.ServerConfig pbServerConfig = ConfigManager.getInstance()
				.getServerConfig();
		if (pbServerConfig == null
				|| pbServerConfig.uint64Flags == null
				|| ((pbServerConfig.uint64Flags & UiEquipment.SERVER_CONFIG_FLAG_SAVE_RAW_PCM) == 0)) {
			LogUtil.logd("save pcm false");
			return false;
		}
		LogUtil.logd("save pcm true");
		return true;
	}
	
	/**
	 * 是否保存录音
	 * @return
	 */
	public static boolean enableSaveVoice(){
		if(DebugCfg.SAVE_VOICE){
			LogUtil.logd("save rf true");
			return true;
		}
		UiEquipment.ServerConfig pbServerConfig = ConfigManager.getInstance()
				.getServerConfig();
		if(pbServerConfig != null && pbServerConfig.msgLogInfo != null && pbServerConfig.msgLogInfo.uint32CacheVoiceFileCnt != null 
				&& pbServerConfig.msgLogInfo.uint32CacheVoiceFileCnt == 0){
			LogUtil.logd("save rf false");
			return false;
		}
		LogUtil.logd("save rf true");
		return true;
	}
	


	//联系人搜索最多搜索40个联系人
	static int sMaxShowContactCount = 40;
	
	/**
	 * 最多返回的联系人结果，显示5页
	 * @return
	 */
	public static int getMaxShowContactCount() {
//		int num = ChoiceManager.getInstance().getNumPageSize();
//		if (num > 0) {
//			sMaxShowContactCount = num * 5;
//		}
		return sMaxShowContactCount;
	}
	
	//从sdk设置的app包名
	static String sSDKSettingPackage = null;
	public static String getSDKSettingPackage() {
		return sSDKSettingPackage;
	}
	public static void setSDKSettingPackage(String sSDKSettingPackage) {
		ProjectCfg.sSDKSettingPackage = sSDKSettingPackage;
		if (!TextUtils.isEmpty(sSDKSettingPackage)) {
			ConfigUtil.setShowSettings(true);
		}else if (!PackageManager.getInstance().mInstalledSetting) {
			ConfigUtil.setShowSettings(false);
		}
	}
	
	public static final int YZS_SDK_VERSION = 3;//云知声重要更新版本号, 0表示忽略版本号信息
    public static final String YZS_ASR_BASE_DIR = "yzs_asr";
	private static byte[] sYzsActivator = null;
	public static void setYzsActivator(byte[] activator) {
		sYzsActivator = activator;
	}
	public static byte[] getYzsActivator(){
		return sYzsActivator;
	}
	
	public static String getYzsFileDir(){
		String sDir = "files";//V2引擎识别文件存放路径
		int yzs_sdk = YZS_SDK_VERSION;
		//大版本引擎识别文件存放路径需要分开, 解决模型和so不兼容，导致连续crash的问题
		if (3 == yzs_sdk){
			sDir = YZS_ASR_BASE_DIR + "/" + "v3";
		}
		return sDir;
	}
	
	private static boolean mEnableRecording = true;
	
	/**
	 * 录音功能是否可用
	 * @param enable
	 */
	public static void setEnableRecording(boolean enable) {
		mEnableRecording = enable;
	}
	
	public static boolean isEnableRecording() {
		return mEnableRecording;
	}

	/**
	 * 获取加密盐
	 * @return 
	 */
	public static String getEncryptKey(){
	    UiEquipment.ServerConfig mServerConfig = ConfigManager.getInstance().getServerConfig();
	    if(mServerConfig != null && mServerConfig.msgEncryptKey != null && mServerConfig.msgEncryptKey.strEncryptKey != null){
	        return new String(mServerConfig.msgEncryptKey.strEncryptKey);
	    }
	    return "";
	}

	/**
	 * 使用电台工具的时候，将广播转成电台去搜索
	 */
	private static boolean sUseRadioAsAudio = true;

	public static void setUseRadioAsAudio(boolean sUseRadioAsAudio) {
		ProjectCfg.sUseRadioAsAudio = sUseRadioAsAudio;
	}

	public static boolean isUseRadioAsAudio() {
		return sUseRadioAsAudio;
	}
	
	/**
	 * 设备拥有的网络模块,初始化程序设置进来的
	 * 0是默认值，属于无法判断的情况
	 * 	public static final int NET_MOUDLE_NONE = 2;
	 * 	public static final int NET_MOUDLE_2G = 3;
	 *  public static final int NET_MOUDLE_3G = 4;
     *  public static final int NET_MOUDLE_4G = 5;
	 */
	private static int sNetModule = 0;
	
	public static void setNetModule(int netModule){
		ProjectCfg.sNetModule = netModule;
	}
	
	public static int getNetModule(){
		return sNetModule;
	}
	
	public static boolean hasNetModule(){//设备是否含有网络模块
		return sNetModule > 2;
	}
	
	private static boolean bTestFlag = false;
	
	public static void setTestFlag(String envType){
	    if(!TextUtils.isEmpty(envType) && TextUtils.equals("测", envType)){
	        bTestFlag = true;
	    }
	}

	public static boolean getTestFlag(){
	    return bTestFlag;
	}
	

	//需要支持说话状态回调的唤醒任务id
	private static String sNeedSpeechStateTaskId = "need_speech_state_task";

	public static void setNeedSpeechStateTaskId(String sNeedSpeechStateTaskId) {
		ProjectCfg.sNeedSpeechStateTaskId = sNeedSpeechStateTaskId;
	}

	public static String getNeedSpeechStateTaskId() {
		return sNeedSpeechStateTaskId;
	}
}

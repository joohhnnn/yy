package com.txznet.txz.cfg;

import android.media.MediaRecorder;

import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.app.PackageManager;

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
	
	public static boolean mUseHQualityWakeupModel = true;
	
	public static boolean mSaveEngineData = false;//是否保存识别和唤醒的录音,小蚁项目默认否。

	// /////////////////////////////////

//	static String mYunzhishengAppId = "b25kmr3ztigd2m5i4qazswkm7uytykbwbsrflwqv";
//	static String mYunzhishengSecret = "b84a5a184b2ae88953caf53abeb6821a";
//	static String mIflyAppId = "548a6747";
	
	static String mYunzhishengAppId ;
	static String mYunzhishengSecret ;
	static String mIflyAppId ;
	static Long mUid;

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
	
	private static boolean sForceStopWkWhenTts = false;
	public static void forceStopWkWhenTts(boolean force){
		JNIHelper.logd("sForceStopWkWhenTts = " + force);
		sForceStopWkWhenTts = force;
	}
	
	public static boolean needStopWkWhenTts(){
		return sForceStopWkWhenTts;
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
}


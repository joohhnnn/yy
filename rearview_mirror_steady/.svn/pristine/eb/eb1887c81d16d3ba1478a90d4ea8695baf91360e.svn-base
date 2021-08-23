package com.txznet.txz.util.recordcenter;

import android.os.SystemClock;
import android.widget.Toast;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.TXZFileConfigUtil;

public class TXZSourceRecorderManager {
	private static ITXZSourceRecorder sSourceRecorder = null;

	private static ITXZSourceRecorder sBaseSourceRecorder = null;
	private static int sRecorderDeathCount = 0;
	private final static int MAX_RECORDER_DEATH_COUNT = 10;
	private static   ITXZSourceRecorder create(){
		if (sBaseSourceRecorder != null){
			if (sBaseSourceRecorder.isLive()){
				return sBaseSourceRecorder;
			}
			++sRecorderDeathCount;
			JNIHelper.logd("last SourceRecorder has died : " + sRecorderDeathCount);
			if (sRecorderDeathCount > MAX_RECORDER_DEATH_COUNT){
				JNIHelper.loge("SourceRecorder has died too many times : " + sRecorderDeathCount);
				AppLogic.exit();
			}
			sBaseSourceRecorder = null;
		}
		IRecorderFactory recorderFactory = new IRecorderFactory() {
			@Override
			public ITXZRecorder create(int audioSource, int sampleRateInHz,
					int channelConfig, int audioFormat, int bufferSizeInBytes) {
				ITXZRecorder recorder = null;
				if (ProjectCfg.isUseExtAudioSource()) {
					//使用外部音频源
					if (ProjectCfg.EXT_AUDIOSOURCE_TYPE_TXZ == ProjectCfg.extAudioSourceType()) {
						//使用TXZ提供的AIDL接口
						recorder = new RemoteRecorder(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
					}else{
						//兼容美赛达
						recorder = new MSDRecorder(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
					}
				}else{ 
				   recorder = new SystemRecorder(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
				}
				return recorder;
			}
		};
		ITXZSourceRecorder txzSourceRecorder = null;
		if (ProjectCfg.getFilterNoiseType() == 3){
			txzSourceRecorder = new TXZStereoAECSourceRecorder(ProjectCfg.getAudioSourceForRecord(), 16000, false, recorderFactory);
		}else if (ProjectCfg.getFilterNoiseType() == 1){
			txzSourceRecorder = new TXZStereoAECSourceRecorder(ProjectCfg.getAudioSourceForRecord(), 16000, true, recorderFactory);
		}else{
			txzSourceRecorder = new TXZMonoSourceRecorder(ProjectCfg.getAudioSourceForRecord(), 16000, recorderFactory);
		}
		sBaseSourceRecorder = txzSourceRecorder;
		return txzSourceRecorder;
	}
	
	private static final long DEFAULT_DELAY_ERROR_TIME = 500;
	private static final long IGNORED_ERROR_TIME = 3000;
	private static final long MAX_DELAY_ERROR_TIME = 16000;
	private static long sLastErrorTime = 0;
	private static long sLastDelayErrorTime = DEFAULT_DELAY_ERROR_TIME;
	
	private static Runnable mRunnableRestart = new Runnable() {
		@Override
		public void run() {
			//需要切线程，否则在stop中会发生死锁
			//回调出错的地方应该停止继续read，否则会连续触发onError回调
			AppLogic.removeBackGroundCallback(sAsyncRunnableRestart);
			
			//恢复录音3秒内就出错，需要加长延时恢复的逻辑，避免录音根本恢复不了的情况下, 
			//打印太多的消息
			if (SystemClock.elapsedRealtime() - sLastErrorTime > sLastDelayErrorTime + IGNORED_ERROR_TIME){
				sLastDelayErrorTime = DEFAULT_DELAY_ERROR_TIME;
			}else{
				sLastDelayErrorTime = sLastDelayErrorTime +  sLastDelayErrorTime;
			}
			
			if (sLastDelayErrorTime >= MAX_DELAY_ERROR_TIME){
				sLastDelayErrorTime = MAX_DELAY_ERROR_TIME;
			}
			
			sLastErrorTime = SystemClock.elapsedRealtime();
			AppLogic.runOnBackGround(sAsyncRunnableRestart, sLastDelayErrorTime);
		}
	};

	private static void notifyRecordException() {
		if (RecorderWin.isOpened()) {
			//  读取配置项判断录音出错时，是否弹toast，默认弹toast。
			boolean bShowMsg = TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_SHOW_MSG_WHEN_RECORD_ERROR, true);
			if (bShowMsg) {
				AppLogic.showToast(NativeData.getResString("RS_RECORD_ERROR"), Toast.LENGTH_LONG);
			}
			RecorderWin.close();
		}
	}
	
	private static Runnable sAsyncRunnableRestart = new Runnable() {
		@Override
		public void run() {
			synchronized (TXZSourceRecorderManager.class) {
				notifyRecordException();
				JNIHelper.logd("SourceRecorder restart...");
				stop();
				start();
			}
		}
	};
	
	/*
	 * 录音非常开方案
	 * 1、客户端start和stop(release)应该是成对的。
	 * 2、请求录音的客户端recorder的个数大于0，此时需要打开录音。
	 * 3、请求录音的客户端recorder的个数等于0的时候，此时需要关闭录音。
	 * 4、因为正确用法中，客户端start和stop(release)应该是成对的。因此，即使不同进程中，
	 *      客户端recorder的start和stop方法不同步，也不会影响整体录音recorder的个数。
	 * 5、唤醒和识别启动录音的顺序流程大致为 start(唤醒)-stop(唤醒)-start(识别)-stop(识别)-start(识别)。
	 * 		优化这种重复stop-start录音流程的方法有:stop录音操作可以延时3000ms到5000ms。
	 * 6、操作recorder个数的代码都跑在同一个线程。
	 */
	private static int sLastNeedRecorderCount = 0;
	private static Runnable sStopRecordRun = new Runnable() {
		@Override
		public void run() {
			stopCompletely();
		}
	}; 
	
	private static IRecorderListener sRecorderListener = new IRecorderListener() {
		@Override
		public boolean onRecorderCount(int nRawRecorderCount, int nAecRecorderCount) {
			synchronized (IRecorderListener.class) {
				sRawRecorderCount = nRawRecorderCount;
				sAecRecorderCount = nAecRecorderCount;
				dealRecorderCount(sRawRecorderCount, sAecRecorderCount, sWinRecorderCount);
			}
			return true;
		}
	};
	
	private static void dealRecorderCount(int nRawRecorderCount, int nAecRecorderCount, int nWinRecorderCount){
		int nNeedRecorderCount = nRawRecorderCount + nAecRecorderCount + nWinRecorderCount ;
		Boolean cmd = null;
		if (nNeedRecorderCount > 0 && sLastNeedRecorderCount == 0){
			cmd = true;
		}else if (nNeedRecorderCount == 0 && sLastNeedRecorderCount > 0){
			cmd = false;
		}
		sLastNeedRecorderCount = nNeedRecorderCount;
		if (cmd != null){
			dealRecorderCount(cmd);
		}
	}
	private static int sRawRecorderCount = 0;
	private static int sAecRecorderCount = 0;
	private static int sWinRecorderCount = 0;
	public static void requestWinRecorder(){
		if (ProjectCfg.isKeepingRecorderOpened() 
				|| !needStopRecording()
				|| !ProjectCfg.needWinRecorder()) {
			return;
		}
		synchronized (IRecorderListener.class) {
			if (sWinRecorderCount == 0){
				sWinRecorderCount = 1;
				dealRecorderCount(sRawRecorderCount, sAecRecorderCount, sWinRecorderCount); 
			}
		}
	}
	
	public static void releaseWinRecorder(){
		if (ProjectCfg.isKeepingRecorderOpened() 
				|| !needStopRecording()
				|| !ProjectCfg.needWinRecorder()) {
			return;
		}
		synchronized (IRecorderListener.class) {
			if (sWinRecorderCount > 0) {
				sWinRecorderCount = 0;
				dealRecorderCount(sRawRecorderCount, sAecRecorderCount, sWinRecorderCount); 
			}
		}
	}
	
	//唤醒被禁用或者唤醒词为空需要停止录音。即不可以唤醒时需要停录音
	private static boolean needStopRecording(){
		boolean bRet = true;
		do {
			//通话中,不能启动录音
			if(!CallManager.getInstance().isIdle()&& !CallManager.getInstance().isRinging()){
				JNIHelper.loge("needStopRecording : call_isIdle = " + CallManager.getInstance().isIdle() + ", call_isRinging = " + CallManager.getInstance().isRinging());
				bRet = true;
				break;
			}
			
			//已经休眠不能启动录音
			if (TXZPowerControl.hasReleased()){
				JNIHelper.loge("needStopRecording : txz has been released");
				bRet = true;
				break;
			}
			
			if (!WakeupManager.getInstance().mEnableWakeup){
				bRet = true;
				break;
			}
			
			String[] temp = null;
			temp = WakeupManager.getInstance().getWakeupKeywords_Sdk();
			if (temp != null && temp.length > 0) {
				bRet = false;
				break;
			}
			
			temp = null;
			temp = WakeupManager.getInstance().getWakeupKeywords_User();//如果不允许用户设置唤醒词,就没有判断的必要了
			if (WakeupManager.getInstance().mEnableChangeWakeupKeywords && temp != null && temp.length > 0) {
				bRet = false;
				break;
			}
			
			bRet = true;
		} while (false);
		
		return bRet;
	}
	
	public static void start() {
		synchronized (TXZSourceRecorderManager.class) {
			if (ProjectCfg.isEnableRecording()) {
				if (sLastRecordCmd || ProjectCfg.isKeepingRecorderOpened()) {
					startCompletely();
				} else {
					startCtrlThread();
				}
			} else {
				LogUtil.logw("record is disable!!!");
			}
		}
	}
	
	public static void stop() {
		synchronized (TXZSourceRecorderManager.class) {
			stopCompletely();
		}
	}
	
	private static boolean sLastRecordCmd = false;
	private static void dealRecorderCount(boolean bRecordCmd){
		synchronized (TXZSourceRecorderManager.class) {
			if (bRecordCmd){
				JNIHelper.logd("recorder count became nozero, start recording immediately");
				AppLogic.removeBackGroundCallback(sStopRecordRun);
				sLastRecordCmd = bRecordCmd;
				//通话中,不能启动录音
				if(!CallManager.getInstance().isIdle()&& !CallManager.getInstance().isRinging()){
					JNIHelper.loge("call_isIdle = " + CallManager.getInstance().isIdle() + ", call_isRinging = " + CallManager.getInstance().isRinging());
					return;
				}
				//已经休眠不能启动录音
				if (TXZPowerControl.hasReleased()){
					JNIHelper.loge("txz has been released");
					return;
				}
				
				// 录音功能是否可用
				if (!ProjectCfg.isEnableRecording()) {
					JNIHelper.loge("The recording is disabled");
					return;
				}
				startCompletely();
			}else{
				if (!needStopRecording() || ProjectCfg.isKeepingRecorderOpened()){
					JNIHelper.logd("quit stop recording because of needStopRecording = false");
					return;
				}
				JNIHelper.logd("recorder count became zero, stop recording after 3000ms");
				sLastRecordCmd = bRecordCmd;
				AppLogic.runOnBackGround(sStopRecordRun, 3000);
			}
		}
	}
	
	private static void startCtrlThread() {
		synchronized (TXZSourceRecorderManager.class) {
			JNIHelper.logd("SourceRecorder start only ctrl thread ...");
			RecorderCenter.run(sRecorderListener);
		}
	}
	
	private static void startCompletely(){
		synchronized (TXZSourceRecorderManager.class) {
			if (sSourceRecorder == null) {
				sSourceRecorder = create();
				sSourceRecorder.setErrorRunnable(mRunnableRestart); // 出现异常时重启录音
				JNIHelper.logd("SourceRecorder start...");
				RecorderCenter.run(sSourceRecorder, sRecorderListener);
			}
		}
	}
	
	private static void stopCompletely() {
		synchronized (TXZSourceRecorderManager.class) {
			if (sSourceRecorder != null) {
				sSourceRecorder.setErrorRunnable(null);
				sSourceRecorder = null;
				JNIHelper.logd("SourceRecorder stop...");
				RecorderCenter.run(sSourceRecorder, sRecorderListener);
			}
		}
	}
}

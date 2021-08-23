package com.txznet.txz.module.asr;

import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.util.AsrUtil.IWakeupAsrCallback;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.sdk.TXZConfigManager.InterruptMode;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.STATE;

import android.os.SystemClock;
import android.text.TextUtils;

/**
 * 随意打断的辅助类<br>
 * 1.在TTS播报时启动识别，根据打断模式执行打断功能<br>
 * 2.在启动识别的时候使用唤醒来监听用户是否说话，如果在TTS播放完的时候，用户没有说话，则重启识别<br>
 */
public class InterruptTts {
	
	private static InterruptTts instance = new InterruptTts();
	
	public static InterruptTts getInstance() {
		return instance;
	}
	
	private InterruptTts() {
	}
	
	/**
	 * 由SDK配置的打断类型
	 */
	private InterruptMode mSDKInterruptMode = null;
	private AsrOption mAsrOption;
	private boolean isUseWakeupAsAsr = false;
	
	public void setSDKInterruptMode(InterruptMode mSDKInterruptMode) {
		this.mSDKInterruptMode = mSDKInterruptMode;
	}
	
	public InterruptMode getInterruptMode() {
		//先判断是否开启回音消除，没有开的话不开启
		if (!ProjectCfg.mEnableAEC) {
			return InterruptMode.INTERRUPT_MODE_DEFAULT;
		}
		//先判断服务器的，没有设置则取sdk的
		UiEquipment.ServerConfig pbServerConfig = ConfigManager.getInstance().getServerConfig();
		if (pbServerConfig != null) {
			if (pbServerConfig.uint32InterruptMode != null) {
				if (pbServerConfig.uint32InterruptMode == UiEquipment.INTERRUPT_MODE_DEFAULT) {
					return InterruptMode.INTERRUPT_MODE_DEFAULT;
				}else if (pbServerConfig.uint32InterruptMode == UiEquipment.INTERRUPT_MODE_ORDER) {
					return InterruptMode.INTERRUPT_MODE_ORDER;
				}else if (pbServerConfig.uint32InterruptMode == UiEquipment.INTERRUPT_MODE_SPEAK) {
//					return InterruptMode.INTERRUPT_MODE_SPEAK;
				}
			}
		}
		
		//判断SDK设置的的type
		if (mSDKInterruptMode != null) {
			return mSDKInterruptMode;
		}
		return InterruptMode.INTERRUPT_MODE_DEFAULT;
	}
	
	/**
	 * 是否开启了打断功能
	 * @return
	 */
	public boolean isInterruptTTS() {
		return getInterruptMode() != InterruptMode.INTERRUPT_MODE_DEFAULT;
	}
	
	/**
	 * 是否识别到场景才打断
	 * @return
	 */
	public boolean isInterruptModeOrder() {
		return getInterruptMode() == InterruptMode.INTERRUPT_MODE_ORDER;
	}
	
	/**
	 * 是否说话就打断
	 * @return
	 */
	public boolean isInterruptModeSpeak() {
//		return getInterruptMode() == InterruptMode.INTERRUPT_MODE_SPEAK;
		return false;
	}
	
	/**
	 * 是否开启了识别
	 * @return
	 */
	public boolean isInterruptModeDefault() {
		return getInterruptMode() == InterruptMode.INTERRUPT_MODE_DEFAULT;
	}
	
	/**
	 * 使用打断功能时,并且启动识别的ID和TTS的ID相等的时候不需要暂停tts
	 * @param oOption
	 * @return
	 */
	public boolean dontPauseTts(AsrOption oOption) {
		return isInterruptTTS() &&
				(oOption!= null && oOption.mTtsId != null && (TtsManager.getInstance().getCurTaskId() == oOption.mTtsId));
	}
	/**
	 * 在TTS播报的时候启动识别
	 * @param oRun tts的回调，isNeedStartAsr判断是否要启动识别
	 * @param sText 不可为空
	 * @param iTaskId tts的ID，用来标记识别
	 */
	public void startAsr(final ITtsCallback oRun,String sText,int iTaskId) {
		JNIHelper.logd("sInterruptTTS:"+isInterruptTTS()+";needStartAsr:"+(oRun!=null&&oRun.isNeedStartAsr())+";needStartWakeUp:"+!isSelecting());
		//启动识别
		if (isInterruptTTS()) {
			//是否可以启动识别，只有在随意打断时有效
			if (oRun != null && oRun.isNeedStartAsr()) {
				//开启识别
				mAsrOption = new AsrOption()
					.setManual(false)
					.setPlayBeepSound(false);
				if (isSelecting()) {
					mAsrOption.setNeedStopWakeup(false);//TODO 这个在列表的情况下不要关掉唤醒或者识别结束后打开唤醒
				}else {
					mAsrOption.setNeedStopWakeup(true);
					//打断后不是在选择界面，但是场景id还是列表场景，则重置为默认场景开启识别
					if (AsrManager.getInstance().getCurrentGrammarId() == VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL) {
						mAsrOption.setGrammar(VoiceData.GRAMMAR_SENCE_DEFAULT);
					}
				}
				mAsrOption.mTtsId = iTaskId;
				mFirstSpeech = true;
				RecorderWin.setState(STATE.STATE_START);
				isUseWakeupAsAsr = true;
				WakeupManager.getInstance().useWakeupAsAsr(getWakeupAsrCallback());
			}
		}
	}
	
	private IWakeupAsrCallback getWakeupAsrCallback() {
		IWakeupAsrCallback wakeupAsrCallback = new IWakeupAsrCallback() {
			
			@Override
			public void onSpeechBegin() {
				super.onSpeechBegin();
				lastSpeechTime = SystemClock.elapsedRealtime();
				if (mFirstSpeech) {
					mFirstSpeech = false;
					JNIHelper.loge("onSpeechBegin");
					if (mAsrOption != null) {
						mAsrOption.mBeginSpeechTime = lastSpeechTime - 200;
						lastStartAsrTime = SystemClock.elapsedRealtime();
						AsrManager.getInstance().start(mAsrOption);
					}
					
					
					//开口打断，只是停掉tts，接着识别
					if (isInterruptModeSpeak()) {
						if (AsrManager.getInstance().isBusy()) {
							if (AsrManager.getInstance().getTtsId() == TtsManager.getInstance().getCurTaskId()) {
								TtsManager.getInstance().cancelSpeak(TtsManager.getInstance().getCurTaskId());
							}
						}
					}
				}
			}
			
			@Override
			public void onSpeechEnd() {
				super.onSpeechEnd();
			}
			
			@Override
			public boolean needAsrState() {
				return false;
			}
			
			@Override
			public String getTaskId() {
				return "InterruptTTS";
			}
		};
		return wakeupAsrCallback;
	}
	
	private long lastStartAsrTime = -1;
	private long lastSpeechTime = -1;
	private boolean mFirstSpeech = true;
	
	public boolean isBeginSpeech() {
		return lastSpeechTime > lastStartAsrTime;
	}
	
	/**
	 * 在结果返回的时候，判断结果是否需要拦截
	 * @param strScene 识别到的场景
	 * @param uint32SessionId 识别的ID
	 * @return true为需要打断，抛弃当前结果，false为继续当前逻辑
	 */
	public boolean doInterrupt(String strScene,Integer uint32SessionId) {
		boolean ret = false;
		if (isInterruptModeOrder()) {
			if (TextUtils.equals("empty", strScene)
					||TextUtils.equals("unsupport", strScene)
					||TextUtils.equals("unknown", strScene)) {
				ret = interrupt(uint32SessionId);
			}else if (!TextUtils.equals("fix", strScene)) {
				//和识别一起开始的TTS播报是否还在播报，还在播报则直接打断
				if (uint32SessionId != null && uint32SessionId == TtsManager.getInstance().getCurTaskId()) {
					JNIHelper.logd("strScene:"+ strScene +"; ttsId: "+uint32SessionId);
					TtsManager.getInstance().cancelSpeak(uint32SessionId);
				}
				//如果上一次的结果返回来的时候，这次的识别已经开启了，TODO 还需要判断是不是打断tts并且启动了识别的状态
				if (AsrManager.getInstance().isBusy()) {
					JNIHelper.logd("strScene:"+ strScene +"; ttsId: "+uint32SessionId);
					AsrManager.getInstance().cancel();
				}
			}
		}
		return ret;
	}
	
	/**
	 * 场景模式下打断没有处理的场景
	 * @param uint32SessionId 识别的ID
	 * @return true为需要打断，抛弃当前结果，false为继续当前逻辑
	 */
	public boolean doInterrupt(Integer uint32SessionId) {
		if (isInterruptModeOrder()) {
			return interrupt(uint32SessionId);
		}
		return false;
	}
	
	private boolean interrupt(Integer uint32SessionId) {
		mAsrOption = null;
//		JNIHelper.logd("strScene:"+ strScene +"; uint32SessionId: "+uint32SessionId);
		//和识别一起开始的TTS播报是否还在播报，或者是列表选择界面，还在播报的时候则丢掉这次的结果
		if (uint32SessionId != null && (uint32SessionId == TtsManager.getInstance().getCurTaskId()||isSelecting())) {
			RecorderWin.setState(STATE.STATE_END);
			//重新开启识别
			mAsrOption = new AsrOption()
			.setManual(false)
			.setPlayBeepSound(false);
			mAsrOption.mTtsId = uint32SessionId;
			if (isSelecting()) {
				mAsrOption.setNeedStopWakeup(false);
			}else {
				mAsrOption.setNeedStopWakeup(true);
				//打断后不是在选择界面，但是场景id还是列表场景，则重置为默认场景开启识别
				if (AsrManager.getInstance().getCurrentGrammarId() == VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL) {
					mAsrOption.setGrammar(VoiceData.GRAMMAR_SENCE_DEFAULT);
				}
			}
//			lastStartAsrTime = SystemClock.elapsedRealtime();
			mFirstSpeech = true;
//			AsrManager.getInstance().start(mAsrOption);
			RecorderWin.setState(STATE.STATE_START);
			isUseWakeupAsAsr = true;
			WakeupManager.getInstance().useWakeupAsAsr(getWakeupAsrCallback());
			return true;
		}
		//本次的结果返回来了的时候，下一次的识别已经开启了，则丢弃掉本次的结果
		if (AsrManager.getInstance().isBusy()) {
			return true;
		}
		return false;
	}
	
	/**
	 * 识别结果出错的时候是否需要打断
	 * @param uint32SessionId
	 * @return
	 */
	public boolean needInterruptOnError(Integer uint32SessionId) {
		mAsrOption = null;
		if (isInterruptTTS()) {
			//和识别一起开始的TTS播报是否还在播报，或者是列表选择界面，还在播报的时候则丢掉这次的结果
			if (uint32SessionId != null && (uint32SessionId == TtsManager.getInstance().getCurTaskId()||isSelecting())) {
				return true;
			}
			
		}
		return false;
	}
	
	/**
	 * 实际的错误打断操作
	 * @param uint32SessionId
	 * @return
	 */
	public boolean doInterruptOnError(Integer uint32SessionId) {
		RecorderWin.setState(STATE.STATE_END);
		//重新开启识别
		mAsrOption = new AsrOption()
		.setManual(false)
		.setPlayBeepSound(false);
		mAsrOption.mTtsId = uint32SessionId;
		if (isSelecting()) {
			mAsrOption.setNeedStopWakeup(false);
		}else {
			mAsrOption.setNeedStopWakeup(true);
			//打断后不是在选择界面，但是场景id还是列表场景，则重置为默认场景开启识别
			if (AsrManager.getInstance().getCurrentGrammarId() == VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL) {
				mAsrOption.setGrammar(VoiceData.GRAMMAR_SENCE_DEFAULT);
			}
		}
		//lastStartAsrTime = SystemClock.elapsedRealtime();
		//AsrManager.getInstance().start(mAsrOption);
		mFirstSpeech = true;
		RecorderWin.setState(STATE.STATE_START);
		isUseWakeupAsAsr = true;
		WakeupManager.getInstance().useWakeupAsAsr(getWakeupAsrCallback());
		return false;
	}
	
	/**
	 * 取消掉唤醒
	 */
	public void endInterruptWakeup(){
		if (isUseWakeupAsAsr) {
			WakeupManager.getInstance().recoverWakeupFromAsr("InterruptTTS");
			isUseWakeupAsAsr = false;
		}
	}
	
	public boolean isSelecting(){
		return ChoiceManager.getInstance().isSelecting();
	}
	
}

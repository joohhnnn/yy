package com.txznet.txz.module.asr;

import java.io.File;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;

import com.txz.ui.data.UiData;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.makecall.UiMakecall;
import com.txz.ui.record.UiRecord;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.ReportAsrError;
import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseCommResult;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.IWakeupAsrCallback;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.ReportUtil.Report;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZConfigManager.AsrMode;
import com.txznet.sdk.TXZConfigManager.AsrServiceMode;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.AsrType;
import com.txznet.txz.component.asr.IAsr.IAsrCallback;
import com.txznet.txz.component.asr.IAsr.IBuildGrammarCallback;
import com.txznet.txz.component.asr.IAsr.IImportKeywordsCallback;
import com.txznet.txz.component.asr.remote.AsrRemoteImpl;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine;
import com.txznet.txz.component.choice.list.CallWorkChoice;
import com.txznet.txz.component.text.IText;
import com.txznet.txz.component.text.IText.ITextCallBack;
import com.txznet.txz.component.text.IText.PreemptLevel;
import com.txznet.txz.component.wakeup.IWakeup.WakeupKw;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.bt.BluetoothManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.launch.LaunchManager;
import com.txznet.txz.module.mtj.MtjModule;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.remoteregcmd.RemoteRegCmdManager;
import com.txznet.txz.module.resource.ResourceManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.sim.SimManager;
import com.txznet.txz.module.text.TextManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.version.LicenseManager;
import com.txznet.txz.module.version.VersionManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.widget.SDKFloatViewInner;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.ui.win.help.WinHelpManager;
import com.txznet.txz.ui.win.help.WinHelpTops;
import com.txznet.txz.ui.win.help.WinHelpTops.INDEX;
import com.txznet.txz.ui.win.nav.SearchEditDialog;
import com.txznet.txz.ui.win.nav.SeleceCityDialog;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.STATE;
import com.txznet.txz.util.BeepPlayer;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.recordcenter.RecordFile;
import com.txznet.txz.util.recordcenter.RecorderCenter;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;

/**
 * 语音识别管理模块，负责声控的启动/停止控制，负责声控相关事件处理
 * 
 * @author bihongpi
 *
 */
public class AsrManager extends IModule {
	public static int ASR_CHAT_MODE_MAX_UNKNOW_COUNT = 2; // 聊天模式下最多支持几次识别不了后退出
	public static int ASR_CHAT_MODE_MAX_EMPTY_COUNT = 2; // 聊天模式下最多支持没有说话后退出
	public static int ASR_CHOISE_MODE_MAX_EMPTY_COUNT = 2; // 列表识别模式下最多支持没有说话后退出
	public static int ASR_SENCE_REPEATE_COUNT = 5; // 场景模式下启用识别的次数
	public boolean mStartFromWeakup = false;	
	static AsrManager sModuleInstance = new AsrManager();
	private long mTimeCost = 0;
	public boolean isFirstRecord = true;

	private AsrManager() {
		mInited = false;
		mInitSuccessed = false;
		
		needImportKw();
		
 		IntentFilter inf = new IntentFilter();
		inf.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
		// inf.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				JNIHelper.logd("Sco change "
						+ intent.getIntExtra(
								BluetoothHeadset.EXTRA_PREVIOUS_STATE, -1)
						+ " to "
						+ intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, -1));

				AppLogic.runOnBackGround(new Runnable() {
					@Override
					public void run() {
						if (BluetoothManager.getInstance().isScoStateOn()) {
							JNIHelper.logd("bluetooth Sco On: " + mState.name());
							if (mState == RecogonizeState.STATE_STARTING_SCO) {
								start_startTipVoice();
							}
						}
						if (BluetoothManager.getInstance().isScoStateOn() == false) {
							JNIHelper.logd("bluetooth Sco Off: "
									+ mState.name());
						}
					}
				}, 0);
			}
		}, inf);
	}
	
	/**
	 * 普强特殊处理，启动前删除配置文件进行插词
	 * @return
	 */
	private void needImportKw() {
 		int engineType = ProjectCfg.getVoiceEngineType();
		if (engineType != 0) {
			if ((engineType & UiEquipment.VET_PACHIRA_OFFLINE) == UiEquipment.VET_PACHIRA_OFFLINE){
				String appDir = GlobalContext.get().getApplicationInfo().dataDir;
		 		FileUtil.removeDirectory(appDir + "/grm");
			}
		}
	}

	public static AsrManager getInstance() {
		return sModuleInstance;
	}

	// ///////////////////////////////////////////////////////////////////

	IAsr mAsr = null;
	IAsr mAsrRemote = null;

	private IAsr getIAsr() {
		if (mAsrRemote != null) {
			return mAsrRemote;
		}

		return mAsr;
	}

	public void initializeComponent() {
		if (mAsr != null)
			return;

		if (TextUtils.isEmpty(ImplCfg.getAsrImplClass()))
			return;

		try {
			mAsr = (IAsr) Class.forName(ImplCfg.getAsrImplClass())
					.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}

		mAsr.initialize(new IAsr.IInitCallback() {
			@Override
			public void onInit(boolean bSuccess) {
				mInited = true;
				mInitSuccessed = bSuccess;
				JNIHelper.logd("init asr: " + bSuccess);
				if (bSuccess) {
					//JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_ENGINE_READY);
					//意图设备install -r 方式升级,需要延时5秒通知下层进行离线词表编译，不然会出现连续crash。
					AppLogic.runOnBackGround(new Runnable() {
						@Override
						public void run() {
							JNIHelper.logd("init asr send voice engine ready event");
							JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_ENGINE_READY);
						}
					}, 5*1000);

					AppLogic.runOnBackGround(new Runnable() {
						@Override
						public void run() {
							try {
								if (mAsrOption != null
										&& mLastUninitStartTime != 0) {
									LogUtil.logd("start asr before init, start again: "
											+ mLastUninitStartTime);
									mAsrOption.mBeginSpeechTime = mLastUninitStartTime;
									mAsrOption.mDirectAsrKw = null;
									int ret = getIAsr().start(mAsrOption);
									if (ret != IAsr.ERROR_SUCCESS) {
										mSysAsrCallBack.onAbort(mAsrOption, 0);
									}
								}
							} catch (Exception e) {
							}
						}
					}, 0);
				}

				//TXZService.checkSdkInitResult();
			}
		});
	}

	@Override
	public int initialize_AfterStartJni() {
		forTest();
		return ERROR_SUCCESS;
	}

	private void forTest() {
		if(RecordFile.ENABLE_TEST_DEFINIT_VOICE_NAME) {
			GlobalContext.get().registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					RecordFile.mDefinitVoiceName = intent.getStringExtra("name");
					String testing = intent.getStringExtra("testing");
					if (!TextUtils.isEmpty(testing)) {
						RecordFile.ENABLE_TEST_DEFINIT_VOICE_NAME = Boolean.parseBoolean(testing);
					}
				}
			}, new IntentFilter("com.txznet.definitVoiceName"));
			RecordFile.ENABLE_TEST_DEFINIT_VOICE_NAME = false;
		}
	}

	public int start(boolean bManual, int iGrammar, IAsrCallback oRun) {
		return start(new AsrOption().setManual(bManual).setGrammar(iGrammar)
				.setCallback(oRun));
	}

	public int start(boolean bManual, int iGrammar) {
		return start(new AsrOption().setManual(bManual).setGrammar(iGrammar));
	}
	
	public int startWithInstantAsr(boolean bManual, int iGrammar){
		AsrOption option = new AsrOption();
		option.mPlayBeepSound = false;
		option.setManual(bManual);
		option.setGrammar(iGrammar);
		option.mBeginSpeechTime = WakeupManager.getInstance().getLastWakeupStartTime();
		option.mDirectAsrKw = WakeupManager.getInstance().getCurrDirectAsrKw();
		return start(option);
	}

	public int start(boolean bManual, IAsrCallback oRun) {
		return start(new AsrOption().setManual(bManual).setCallback(oRun));
	}

	public int start(int iGrammar, IAsrCallback oRun) {
		return start(new AsrOption().setGrammar(iGrammar).setCallback(oRun));
	}

	public int start(boolean bManual) {
		return start(new AsrOption().setManual(bManual));
	}

	public int start(int iGrammar) {
		return start(new AsrOption().setGrammar(iGrammar));
	}

	public int start(IAsrCallback oRun) {
		return start(new AsrOption().setCallback(oRun));
	}

	public int start() {
		return start(new AsrOption());
	}

	// ////////////////////////////////////////////////////////////////////

	IAsrCallback mUsrAsrCallback;
	public int mUnknowCount = 0;
	public int mEmptyCount = 0;
	public int mAsrCount = 0;//识别次数

	IAsrCallback mSysAsrCallBack = new IAsrCallback() {
		@Override
		public void onStart(AsrOption option) {
			if (mState == RecogonizeState.STATE_IDLE)
				return;
			// 录音窗口显示“请说话”
			JNIHelper.logd("startAsrRecord");
			MusicManager.getInstance().onEndBeep();
			RecorderWin.setState(RecorderWin.STATE.STATE_RECORD);
			if (mUsrAsrCallback != null) {
				mUsrAsrCallback.onStart(option);
			}
		}

		@Override
		public void onEnd(AsrOption option) {
			releaseSco();
			// 录音窗口显示“正在识别”
			RecorderWin.setState(RecorderWin.STATE.STATE_RECOGONIZE);
			if (mUsrAsrCallback != null) {
				mUsrAsrCallback.onEnd(option);
			}
			AppLogic.removeBackGroundCallback(mVolumeUpdater);
			BeepPlayer.playWaitMusic();
		}

		@Override
		public void onSpeechEnd(AsrOption option) {
			if(mUsrAsrCallback != null) {
				mUsrAsrCallback.onSpeechEnd(option);
			}
			ReportUtil.doReport(new ReportUtil.Report.Builder().setSessionId()
					.setTaskID(option.mVoiceID+"").setType("asr").setAction("speechend")
					.buildCommReport());
		}

		@Override
		public void onSpeechBegin(AsrOption option) {
			if (mUsrAsrCallback != null) {
				mUsrAsrCallback.onSpeechBegin(option);
			}
			ReportUtil.doReport(new ReportUtil.Report.Builder().setSessionId()
					.setTaskID(option.mVoiceID+"").setType("asr").setAction("speechbegin")
					.buildCommReport());

		}

		int mMaxVolume = 20;
		int mCurrVolume = 0;
		Runnable mVolumeUpdater = new Runnable() {
			@Override
			public void run() {
				AppLogic.removeBackGroundCallback(mVolumeUpdater);
				AppLogic.runOnBackGround(mVolumeUpdater, 50);
				RecorderWin.notifyVolumeChanged(mCurrVolume);
			}
		};

		int last = 0;
		int sign = 0;

		@Override
		public void onVolume(AsrOption option, int volume) {
			RecorderWin.notifyVolumeChanged(volume);

			// 要清理掉录音状态的超时保护
			if (volume > 0) {
				AppLogic.removeBackGroundCallback(mStateTimeoutRunnable);
			}

			// JNIHelper.logd("volume=" + volume + ", last = " + last +
			// " , sign = " + sign);

			// // 录音窗口显示“正在倾听”，并设置波纹
			// if (volume == last) {
			// volume = last + sign * 10;
			// sign = -sign;
			// } else {
			// if (volume > last) {
			// sign = 1;
			// }
			// if (volume < last) {
			// sign = -1;
			// }
			// last = volume;
			// }
			//
			// WinRecord.getInstance().setVolume(
			// (volume - 18) * 6 + (new Random().nextInt(11) - 5) + 50);
			if (mUsrAsrCallback != null) {
				mUsrAsrCallback.onVolume(option, volume);
			}
		}

		@Override
		public void onSuccess(AsrOption option, VoiceParseData oVoiceParseData) {
			JNIHelper.logd("endAsr onSuccess");
			mTimeCost = SystemClock.elapsedRealtime();
			AppLogic.runOnSlowGround(new Runnable2<AsrOption, VoiceParseData>(option, oVoiceParseData) {

				@Override
				public void run() {
					File f = new File(ProjectCfg.AUDIO_SAVE_PATH+"/txz_asr_"+mP1.mVoiceID+".rf");
					RecordFile rf = RecordFile.openFile(f);
					if(rf != null){
						rf.updateRecordResult(mP2.strVoiceData);
						LogUtil.logd("Asr updateRecordResult txz_asr_"+mP1.mVoiceID+".rf");
					}else{
						LogUtil.logd("Asr rf = null = "+ProjectCfg.AUDIO_SAVE_PATH+"/txz_asr_"+mP1.mVoiceID+".rf");
					}
				}
			}, 500);
			
			if(!TextUtils.isEmpty(oVoiceParseData.strText)){
				mStartFromWeakup = false;
			}

			VoiceData.RecognizeReleaseReason reason = new VoiceData.RecognizeReleaseReason();
			reason.uint32ResultCode = 0;

			onAsrEnd(option, reason);
			if (!TextUtils.isEmpty(option.mDirectAsrKw)) {
				WakeupKw kw = WakeupManager.getInstance().getWakeupKw(option.mDirectAsrKw);
				oVoiceParseData.strOneshotKw = kw.mOneShotKw;
				oVoiceParseData.strDirectAsrKw = kw.mDirectAsrKw;
				oVoiceParseData.uint32DirectAsrType = kw.mKwType.ordinal();
			}
			oVoiceParseData.uint64VoiceFileId = option.mVoiceID;
			oVoiceParseData.uint32SessionId = option.mTtsId;
			setNeedCloseRecord(mAsrMode == AsrMode.ASR_MODE_SINGLE);
			String debug = Environment.getExternalStorageDirectory().getPath()
					+ "/txz/debug_old_text";
			File file = new File(debug);
			if (!file.exists()) {
				JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_PARSE_NEW, oVoiceParseData);
			}else {
				JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_PARSE, oVoiceParseData);
			}

			// TODO 设置调试日志AppState.setLog(voice.strVoiceData);

			// 录音窗口显示“正在处理”
			RecorderWin.setState(RecorderWin.STATE.STATE_PROCESSING);

			if (mUsrAsrCallback != null) {
				mUsrAsrCallback.onSuccess(option, oVoiceParseData);
				mUsrAsrCallback = null;
			}
		}

		@Override
		public void onError(AsrOption option, int error, String desc,
				String speech, int error2) {
			MtjModule.getInstance().event(MtjModule.EVENTID_VOICE_ERROR);
			JNIHelper.logd("endAsr onError: erorr2=" + error2 + ",error="
					+ error + ",desc=" + desc + ",speech=" + speech);

			// TODO doReportError
			ReportAsrError reportError = new ReportAsrError();
			reportError.int32ErrCode1 = error;
			reportError.strErrMsg1 = desc;
			reportError.int32ErrCode2 = error2;
			reportError.uint64VoiceFileId = option.mVoiceID;
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_REPORT_ASR_ERROR, reportError);
			if (speech == null)
				speech = desc;
			
			if (InterruptTts.getInstance().needInterruptOnError(option.mTtsId)) {
				VoiceData.RecognizeReleaseReason reason = new VoiceData.RecognizeReleaseReason();
				reason.uint32ResultCode = VoiceData.RECOGNIZE_RELEASE_ERROR;
				reason.uint32ErrorCode = error;
				onAsrEnd(option, reason);
				
				if (mUsrAsrCallback != null) {
					mUsrAsrCallback.onError(option, error, desc, speech, error2);
					mUsrAsrCallback = null;
				}
				
				InterruptTts.getInstance().doInterruptOnError(option.mTtsId);
				return;
			}
			
			
			

			switch (error2) {
			case IAsr.ERROR_NO_MATCH: {
				RecorderWin.setLastUserText(NativeData
						.getResString("RS_USER_UNKNOW_TEXT"));
				VoiceParseCommResult result = new VoiceParseCommResult();
				result.boolLocal = true;
				result.boolManual = option.mManual;
				result.strUserText = "";
				result.uint32ResultType = VoiceData.COMMON_RESULT_TYPE_UNKNOW;
				result.uint32GrammarId = option.mGrammar;
				result.uint32GrammarCompileStatus = 1;
				result.uint64VoiceFileId = option.mVoiceID;
				result.uint32SessionId = option.mTtsId;
				JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
						VoiceData.SUBEVENT_VOICE_COMMON_RESULT, result);
				break;
			}
			case IAsr.ERROR_NO_SPEECH: {
				RecorderWin.setLastUserText(NativeData
						.getResString("RS_USER_EMPTY_TEXT"));
				ReportUtil.doVoiceReport(new ReportUtil.Report.Builder().setAction("empty")
						.putExtra("scene", "empty").setSessionId().putExtra("_rt", "voice")
						.buildVoiceReport(), UiRecord.RECORD_TYPE_ASR, option.mVoiceID);
				VoiceParseCommResult result = new VoiceParseCommResult();
				result.boolLocal = true;
				result.boolManual = option.mManual;
				result.strUserText = "";
				result.uint32ResultType = VoiceData.COMMON_RESULT_TYPE_EMPTY;
				result.uint32GrammarId = option.mGrammar;
				result.uint32GrammarCompileStatus = 1;
				result.uint64VoiceFileId = option.mVoiceID;
				result.uint32SessionId = option.mTtsId;
				JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
						VoiceData.SUBEVENT_VOICE_COMMON_RESULT, result);
			}
				break;
			case IAsr.ERROR_ASR_NET_REQUEST: {
				RecorderWin.setLastUserText(NativeData.getResString("RS_USER_EMPTY_TEXT"));
				VoiceParseCommResult result = new VoiceParseCommResult();
				result.boolLocal = true;
				result.boolManual = option.mManual;
				result.strUserText = "";
				result.uint32ResultType = VoiceData.COMMON_RESULT_TYPE_NET_REQUEST_FAIL;
				result.uint32GrammarId = option.mGrammar;
				result.uint32GrammarCompileStatus = 1;
				result.uint64VoiceFileId = option.mVoiceID;
				result.uint32SessionId = option.mTtsId;
				JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
						VoiceData.SUBEVENT_VOICE_COMMON_RESULT, result);
			}
				break;
			case IAsr.ERROR_ASR_NET_NLU_EMTPY: {
				RecorderWin.setLastUserText(NativeData.getResString("RS_USER_EMPTY_TEXT"));
				VoiceParseCommResult result = new VoiceParseCommResult();
				result.boolLocal = true;
				result.boolManual = option.mManual;
				result.strUserText = "";
				result.uint32ResultType = VoiceData.COMMON_RESULT_TYPE_NET_NLU_EMPTY;
				result.uint32GrammarId = option.mGrammar;
				result.uint32GrammarCompileStatus = 1;
				result.uint64VoiceFileId = option.mVoiceID;
				result.uint32SessionId = option.mTtsId;
				JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
						VoiceData.SUBEVENT_VOICE_COMMON_RESULT, result);
			}
				break;
			default: {
				RecorderWin.setLastUserText(NativeData.getResString("RS_USER_EMPTY_TEXT"));
				VoiceParseCommResult result = new VoiceParseCommResult();
				result.boolLocal = true;
				result.boolManual = option.mManual;
				result.strUserText = "";
				result.uint32ResultType = VoiceData.COMMON_RESULT_TYPE_UNKNOW_ERROR;
				result.uint32GrammarId = option.mGrammar;
				result.uint32GrammarCompileStatus = 1;
				result.uint64VoiceFileId = option.mVoiceID;
				result.uint32SessionId = option.mTtsId;
				JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
						VoiceData.SUBEVENT_VOICE_COMMON_RESULT, result);
			}
			}

			VoiceData.RecognizeReleaseReason reason = new VoiceData.RecognizeReleaseReason();
			reason.uint32ResultCode = VoiceData.RECOGNIZE_RELEASE_ERROR;
			reason.uint32ErrorCode = error;
			onAsrEnd(option, reason);

			// 录音结束，关闭录音中窗口
			// WinRecord.getInstance().close();

			if (mUsrAsrCallback != null) {
				mUsrAsrCallback.onError(option, error, desc, speech, error2);
				mUsrAsrCallback = null;
			}
		}

		@Override
		public void onAbort(AsrOption option, int error) {
			JNIHelper.logd("endAsr onAbort: erorr=" + error);

			// TODO doReportError
			// 启动录音失败的交互提示
			if (ConfigUtil.isShowHelpInfos()) {
				WinHelpTops
						.getInstance()
						.show(com.txznet.txz.ui.win.help.WinHelpTops.INDEX.RECORD_FAILED);
			}
			String spk = NativeData.getResString("RS_ASR_START_FAIL");
			TtsManager.getInstance().speakText(spk);

			VoiceData.RecognizeReleaseReason reason = new VoiceData.RecognizeReleaseReason();
			reason.uint32ResultCode = VoiceData.RECOGNIZE_RELEASE_ABORT;
			reason.uint32ErrorCode = error;
			onAsrEnd(option, reason);

			// 录音结束，关闭录音中窗口
			RecorderWin.close();

			if (mUsrAsrCallback != null) {
				mUsrAsrCallback.onAbort(option, error);
				mUsrAsrCallback = null;
			}
		}

		@Override
		public void onCancel(AsrOption option) {
			JNIHelper.logd("endAsr onCancel");

			if (mState != RecogonizeState.STATE_IDLE) {
				VoiceData.RecognizeReleaseReason reason = new VoiceData.RecognizeReleaseReason();
				reason.uint32ResultCode = VoiceData.RECOGNIZE_RELEASE_CANCEL;
				onAsrEnd(option, reason);
			}

			if (mUsrAsrCallback != null) {
				mUsrAsrCallback.onCancel(option);
				mUsrAsrCallback = null;
			}
		}

	};

	public void onAsrEnd(AsrOption option,
			VoiceData.RecognizeReleaseReason reason) {
		JNIHelper.logd("endAsr complete");
		if(option.mVoiceID != 0){
			ReportUtil.doVoiceReport(new Report.Builder().setRecordType(UiRecord.RECORD_TYPE_ASR)
					.setTaskID(option.mVoiceID+"").setSessionId()
			        .setAction("AsrEnd").buildVoiceReport(), UiRecord.RECORD_TYPE_ASR, option.mVoiceID);
		}
		RecorderCenter.setEnableCacheAEC(false);
		setRecogonizeState(RecogonizeState.STATE_IDLE);
		stopWav();
		BluetoothManager.getInstance().stopSco();

		if (null != reason)
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_RECORD_RELEASE, reason);

		// RecorderWin.setState(RecorderWin.STATE.STATE_END);
		TtsManager.getInstance().resume();
		MusicManager.getInstance().onEndAsr();
		WakeupManager.getInstance().startDelay(500);

		if (option.mCallback == mSysAsrCallBack) {
			option.mCallback = mUsrAsrCallback;
		}
	}

	AsrOption mAsrOption;

	public int getLastGrammarId() {
		if (mAsrOption != null && mAsrOption.mGrammar != null) {
			return mAsrOption.mGrammar;
		}
		return mKeepGrammar;
	}
	
	public int getTtsId(){
		if (mAsrOption != null && mAsrOption.mTtsId != null) {
			return mAsrOption.mTtsId;
		}
		return -1;
	}

	private int mKeySpeechTimeout = IAsr.DEFAULT_SPEECH_TIMEOUT;

	public void setKeySpeechTimeout(int KeySpeechTimeout) {
		JNIHelper.logd("KeySpeechTimeout = " + KeySpeechTimeout);
		if (KeySpeechTimeout < 5000) {
			JNIHelper.logd("KeySpeechTimeout is too short  : "
					+ KeySpeechTimeout);
			return;
		}
		mKeySpeechTimeout = KeySpeechTimeout;
	}

	public int start(final AsrOption oOption) {
		return start(oOption,AsrType.ASR_DEFAULT);
	}
	public int start(final AsrOption oOption,AsrType type) {
		RecorderWin.sPauseRecordWin = false;
		setCloseRecordWinWhenProcEnd(true);

		if (getIAsr() == null) {
			JNIHelper.loge("mAsr == null");
			if (oOption.mCallback != null) {
				oOption.mCallback.onAbort(oOption, 0);
			}
			return ERROR_FAILURE;
		}

		if (mState != RecogonizeState.STATE_IDLE) {
			if (oOption.mCallback != null
					&& oOption.mCallback != mSysAsrCallBack) {
				oOption.mCallback.onError(oOption, 0, null, null,
						IAsr.ERROR_ASR_ISBUSY);
			}
			JNIHelper.loge("State error: " + mState);
			return ERROR_FAILURE;
		}

		//RecordManager.getInstance().stop();
		RecordManager.getInstance().cancel();

		AppLogic.runOnBackGround(new Runnable2<AsrOption,AsrType>(oOption,type) {
			@Override
			public void run() {
				RecorderWin.setState(RecorderWin.STATE.STATE_START);

				if (VersionManager.getInstance().isLisenceForbidden()) {
					TtsManager
							.getInstance()
							.speakText(
									NativeData
											.getResString("RS_TIPS_VERSION_LISENCE_FORBIDDEN"),
									PreemptType.PREEMPT_TYPE_IMMEADIATELY);

					VoiceData.RecognizeReleaseReason reason = new VoiceData.RecognizeReleaseReason();
					reason.uint32ResultCode = VoiceData.RECOGNIZE_RELEASE_ABORT;
					reason.uint32ErrorCode = 0;
					onAsrEnd(mP1, reason);

					// 录音结束，关闭录音中窗口
					RecorderWin.close();

					if (mP1 != null && mP1.mCallback != null) {
						mP1.mCallback.onAbort(mP1, 0);
						mP1.mCallback = null;
					}

					return;
				}
				
				if (InterruptTts.getInstance().dontPauseTts(oOption)) {
					//使用打断功能时,并且启动识别的ID和TTS的ID相等的时候不需要暂停tts
				}else {
					// 暂停TTS
					TtsManager.getInstance().pause();
				}

				mAsrOption = mP1;
				if (mAsrOption == null) {
					mAsrOption = new AsrOption();
				}
				mAsrOption.check();

				JNIHelper.logd("startAsr: mManual=" + mAsrOption.mManual
						+ ",mGrammar=" + mAsrOption.mGrammar);

				mUsrAsrCallback = mAsrOption.mCallback; // 记录用户回调
				mAsrOption.setCallback(mSysAsrCallBack); // 替换成框架回调
				mAsrOption.setKeySpeechTimeout(mKeySpeechTimeout);
				if (mP2 != AsrType.ASR_DEFAULT) {
					mAsrOption.setBOS(mSelectBOS);
					mAsrOption.setEOS(mSelectEOS);
					mAsrOption.mAsrType = mP2;
				}
				else {
					if (mAsrOption.mGrammar == VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_NAVIGATE
				            || mAsrOption.mGrammar == VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_SET_COMPANY
				            ||mAsrOption.mGrammar == VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_SET_HOME) {
				        mAsrOption.setBOS(mSelectBOS);
				        mAsrOption.setEOS(mSelectEOS);
                    }else if (isFirstRecord){
                        mAsrOption.setBOS(mFirstRecordBOS);
                        mAsrOption.setEOS(mEOS);
                    } else {
						mAsrOption.setBOS(mBOS);
						mAsrOption.setEOS(mEOS);
					}
					mAsrOption.mAsrType = switchAsrType(mAsrSvrMode);
				}
				mAsrOption.mBeginSpeechTime = oOption.mBeginSpeechTime;
				start_inner(oOption.mPlayBeepSound);
			}
		}, 0);

		return IAsr.ERROR_SUCCESS;
	}

	// 停止录音
	public void stop() {
		if (mState == RecogonizeState.STATE_IDLE) {
			JNIHelper.loge("State error: " + mState);
			return;
		}

		if (getIAsr() == null) {
			return;
		}
		if (mState != RecogonizeState.STATE_RECORDING) {
			cancel();
			return;
		}
		getIAsr().stop();
		setRecogonizeState(RecogonizeState.STATE_RECOGONIZING);
	}

	public void cancel() {
		mLastUninitStartTime = 0;
		if (mState == RecogonizeState.STATE_IDLE) {
			JNIHelper.loge("State error: " + mState);
			return;
		}

//		cancelIFlyOnlineOnly();

		if (getIAsr() == null) {
			return;
		}

		// if
		// (mAsr.getClass().getName().endsWith("yunzhisheng_3_0.AsrYunzhishengImpl"))
		// {
		// JNIHelper.logd("yzs3.0!!!");
		// mWavPlayer.stop();
		// BluetoothManager.getInstance().stopSco();
		// setRecogonizeState(RecogonizeState.STATE_IDLE);
		// if (mState ==RecogonizeState.STATE_PLAYING_START_TIP_VOICE){
		// //还在播放beep中,还没真真正启动识别，需要在这一层回调onCancel
		// mSysAsrCallBack.onCancel(mAsrOption);
		// }
		// } else {
		mSysAsrCallBack.onCancel(mAsrOption);
		// }
		getIAsr().cancel();
	}

	public boolean isBusy() {
		return mState != RecogonizeState.STATE_IDLE;
	}

	public boolean forbiddenWakeup() {
		if (InterruptTts.getInstance().isInterruptTTS()) {
			//打断开唤醒的时候不需要判断mNeedStopWakeup
//			return isBusy() && mAsrOption.mNeedStopWakeup;
			return false;
		}
		return isBusy()
				&& (mAsrOption.mNeedStopWakeup
						|| ProjectCfg.mCoexistAsrAndWakeup == false);
	}

	// ///////////////////////////////////////////////

	public enum RecogonizeState {
		STATE_IDLE, // 空闲未识别状态
		STATE_STARTING_SCO, // 启动SCO中
		STATE_PLAYING_START_TIP_VOICE, // 播放结束提示音中
		STATE_RECORDING, // 录音中
		STATE_RECOGONIZING, // 识别中
	};

	public RecogonizeState mState = RecogonizeState.STATE_IDLE;

	Runnable mStateTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			JNIHelper.logd("timeout: " + mState.name());
			switch (mState) {
			case STATE_IDLE:
				break;
			case STATE_STARTING_SCO:
				if (BluetoothManager.getInstance().isScoStateOn()) {
					start_startTipVoice();
				} else {
					mSysAsrCallBack.onAbort(mAsrOption, 0);
				}
				break;
			case STATE_PLAYING_START_TIP_VOICE:
				// mWavPlayer.stop();
				start_startRecorder();
				break;
			case STATE_RECORDING:
				// onAbort(0); //没有采集到声音会进这里
				JNIHelper.loge("no voice data");
				break;
			case STATE_RECOGONIZING:
				break;
			}
		}
	};

	void setRecogonizeState(RecogonizeState s) {
		AppLogic.removeBackGroundCallback(mStateTimeoutRunnable);
		JNIHelper.logd("setRecogonizeState: from " + mState.name() + " to "
				+ s.name());
		mState = s;
		long t = 0;
		switch (mState) {
		case STATE_IDLE:
			break;
		case STATE_STARTING_SCO:
			t = 2000;
			break;
		case STATE_PLAYING_START_TIP_VOICE:
			// t = mBeepTimeout;
			break;
		case STATE_RECORDING:
			t = 1000; // 1s没采集到有音量的数据则认为录音失败
			break;
		case STATE_RECOGONIZING:
			break;
		}
		if (t > 0) {
			AppLogic.runOnBackGround(mStateTimeoutRunnable, t);
		}
	}

	boolean start_inner(boolean bPlayBeepSound) {
		JNIHelper.logd("need stop wakeup: " + mAsrOption.mNeedStopWakeup);
		if (mAsrOption.mNeedStopWakeup && !InterruptTts.getInstance().isInterruptTTS()) {
			WakeupManager.getInstance().stop();// 正真启动识别前才停止唤醒，因为cancel是阻塞接口，耗时200ms以上，使嘀的一声出来的很晚
			// 影响：1可能滴的一声之后，并没有正真开始录音可能延时300ms左右，但是在用户的反应时间之内
			// 可考虑将唤醒的stop操作和start操作分别放到recordWin的open和close中。
		}
		MusicManager.getInstance().onBeginAsr();

		if (BluetoothManager.getInstance().isBluetoothDeviceConnected()) {
			com.txz.ui.data.UiData.UserConfig userConfig = NativeData
					.getCurUserConfig();
			if (userConfig != null
					&& userConfig.msgNetCfgInfo != null
					&& userConfig.msgNetCfgInfo.msgUiSetting != null
					&& userConfig.msgNetCfgInfo.msgUiSetting.bInited != null
					&& userConfig.msgNetCfgInfo.msgUiSetting.bInited == true
					&& userConfig.msgNetCfgInfo.msgUiSetting.bUseBluetoothRecogonize != null
					&& userConfig.msgNetCfgInfo.msgUiSetting.bUseBluetoothRecogonize == true) {
				// 启动SCO
				if (!start_startSco()) {
					if (mSysAsrCallBack != null) {
						mSysAsrCallBack.onAbort(mAsrOption, 0);
					}
					return false;
				}
				return true;
			} else {
				BluetoothManager.getInstance().stopSco();
			}
		}

		setRecogonizeState(RecogonizeState.STATE_STARTING_SCO);
		start_startTipVoice(bPlayBeepSound);

		return true;
	}

	boolean start_startSco() {
		setRecogonizeState(RecogonizeState.STATE_STARTING_SCO);

		if (!BluetoothManager.getInstance().startSco()) {
			return false;
		}

		if (BluetoothManager.getInstance().isScoStateOn()) {
			start_startTipVoice();
		}

		return true;
	}

	public int mBeepTimeout = 0; //暂时屏蔽BEEP音超时处理

	public void setBeepTimeout(int timeout) {
		if (timeout > 0 && timeout < 1000) {
			mBeepTimeout = timeout;
		}
		JNIHelper.logd("setBeepTimeout = " + mBeepTimeout);
	}

	Runnable mPlayBeepCompletionRunnable = new Runnable() {
		@Override
		public void run() {
			AppLogic.runOnBackGround(new Runnable() {
				@Override
				public void run() {
					JNIHelper.logd("play complete: " + mState.name());
					if (mState == RecogonizeState.STATE_PLAYING_START_TIP_VOICE) {
						start_startRecorder();
					}
				}
			}, mAsrDelayAfterBeep);
		}
	};
	
	/**
	 * 在mAsrDelayAfterBeep延迟后再开启识别，因为有个方案公司"滴"的回调比实际播放要快一些导致滴播放不出来
	 */
	private int mAsrDelayAfterBeep = 0;

	public void stopWav() {
		BeepPlayer.cancel();
	}

	public void enableAutoRun(boolean enable) {
		AsrWakeupEngine.getEngine().enableAutoRun(enable);
	}

	public boolean playWav() {
		BeepPlayer.play(mPlayBeepCompletionRunnable);
		return true;
	}

	boolean playStartTips() {
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				JNIHelper.logd("playStartTips: " + mState.name());

				if (mState != RecogonizeState.STATE_STARTING_SCO)
					return;

				setRecogonizeState(RecogonizeState.STATE_PLAYING_START_TIP_VOICE);

				playWav();

				return;
			}
		}, 0);
		return true;
	}
	
	boolean start_startTipVoice(){
		return start_startTipVoice(true);
	}

	boolean start_startTipVoice(boolean bPlayBeepSound) {
		if(bPlayBeepSound && !InterruptTts.getInstance().isInterruptTTS()){
			if (playStartTips()) {
				return true;
			}
		}
		
		start_startRecorder();
		return false;
	}

	private long mLastUninitStartTime = 0;
	
	boolean start_startRecorder() {
		// ServiceManager.getInstance().broadInvoke("comm.status.onBeepEnd",
		// null);
		// MusicManager.getInstance().onEndBeep();
		setRecogonizeState(RecogonizeState.STATE_RECORDING);
		RecorderCenter.setEnableCacheAEC(true);
		if (!mInitSuccessed) {
			mLastUninitStartTime = SystemClock.elapsedRealtime();
			return true;
		}
		int ret = getIAsr().start(mAsrOption);

		if (ret != IAsr.ERROR_SUCCESS) {
			mSysAsrCallBack.onAbort(mAsrOption, 0);
			return false;
		}

		return true;
	}

	void releaseSco() {
		if (BluetoothManager.getInstance().isBluetoothDeviceConnected()) {
			BluetoothManager.getInstance().stopSco();
			if (BluetoothManager.getInstance().isScoStateOn()) {
				return;
			}
		}
	}

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_NETWORK_CHANGE);

		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_REG_SDK_KEYWORDS);
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_UPDATE_GRAMMAR);
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_RECORD_BEGIN);
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_RECORD_END);
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_RECORD_CLOSE);
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_RECORD_RELEASE);
		regEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_RECORD_SHOW_ABORT);
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_RECORD_SHOW_HELP);
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SHOW_DEBUG_TEXT);
		regEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_RECORD_SHOW_USER_TEXT);
		regEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_RECORD_ADD_SYSTEM_TEXT);
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SHOW_STOCK_INFO);
		regEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_SHOW_WEATHER_INFO);
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_COMMON_RESULT);
		regEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_PARSE_RAWTEXT_ONLINE);
		return ERROR_SUCCESS;
	}

	IBuildGrammarCallback mBuildGrammarCallback = new IBuildGrammarCallback() {
		@Override
		public void onSuccess(SdkGrammar oGrammarData) {
			JNIHelper.logd("构建语法" + oGrammarData.msgGrammarInfo.strId
					+ "成功: len=" + oGrammarData.strContent.length());
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_UPDATE_GRAMMAR_SUCCESS,
					oGrammarData);
		}

		@Override
		public void onError(int error, SdkGrammar oGrammarData) {
			JNIHelper.loge("构建语法" + oGrammarData.msgGrammarInfo.strId + "失败: "
					+ error);
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_UPDATE_GRAMMAR_FAILED,
					oGrammarData);
		}
	};

	IImportKeywordsCallback mImportKeywordsCallback = new IImportKeywordsCallback() {
		@Override
		public void onSuccess(SdkKeywords mSdkKeywords) {
			String grammar = "online";
			if (mSdkKeywords.msgGrammarInfo != null
					&& mSdkKeywords.msgGrammarInfo.strId != null) {
				grammar = mSdkKeywords.msgGrammarInfo.strId;
			}
			JNIHelper.logd("更新" + grammar + "语法的" + mSdkKeywords.strType
					+ "词库成功: len=" + mSdkKeywords.strContent.length()
					+ " session_id = " + mSdkKeywords.uint32SessionId);
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_REG_SDK_KEYWORDS_SUCCESS,
					mSdkKeywords);
		}

		@Override
		public void onError(int error, SdkKeywords mSdkKeywords) {
			String grammar = "online";
			if (mSdkKeywords.msgGrammarInfo != null
					&& mSdkKeywords.msgGrammarInfo.strId != null) {
				grammar = mSdkKeywords.msgGrammarInfo.strId;
			}
			if (error != 0)
				JNIHelper.loge("更新" + grammar + "语法的" + mSdkKeywords.strType
						+ "词库失败: " + error + " session_id = "
						+ mSdkKeywords.uint32SessionId);

			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_REG_SDK_KEYWORDS_FAILED,
					mSdkKeywords);
		}
	};

	boolean mCloseRecordWinWhenProcEnd = true;

	public void setCloseRecordWinWhenProcEnd(boolean b) {
		mCloseRecordWinWhenProcEnd = b;
	}

	public AsrMode mAsrMode = AsrMode.ASR_MODE_CHAT;
	boolean mNeedCloseRecord = false;
	boolean mNeedStopWavAfterPlayTips = true;

	public void setNeedCloseRecord(boolean b) {
		JNIHelper.logd("setNeedCloseRecord " + b);
		mNeedCloseRecord = b;
	}

	public boolean needCloseRecord() {
		return mNeedCloseRecord;
	}

	public void setNeedStopWavAfterPlayTips(boolean needStop) {
		JNIHelper.logd("setNeedStopWavAfterPlayTips " + needStop);
		mNeedStopWavAfterPlayTips = needStop;
	}

	public boolean needStopWavAfterPlayTips() {
		return mNeedStopWavAfterPlayTips;
	}

	public void setAsrMode(AsrMode mode) {
		mAsrMode = mode;
	}

	private AsrServiceMode mAsrSvrMode = AsrServiceMode.ASR_SVR_MODE_MIX;
	public boolean mEnableFMOnlineCmds = false;

	public void setAsrServiceMode(AsrServiceMode mode) {
		mAsrSvrMode = mode;
		JNIHelper.logd("mAsrSvrMode = " + mAsrSvrMode.name());
	}

	public AsrServiceMode getAsrSvrMode() {
		return mAsrSvrMode;
	}
    
	private AsrType switchAsrType(AsrServiceMode asrSrvMode){
		AsrType asrType = AsrType.ASR_MIX;
        if (asrSrvMode == AsrServiceMode.ASR_SVR_MODE_AUTO){
        	asrType = AsrType.ASR_AUTO;
        }else if (asrSrvMode == AsrServiceMode.ASR_SVR_MODE_MIX){
        	asrType = AsrType.ASR_MIX;
        }else if (asrSrvMode == AsrServiceMode.ASR_SVR_MODE_NET){
        	asrType = AsrType.ASR_ONLINE;
        }else if (asrSrvMode == AsrServiceMode.ASR_SVR_MODE_LOCAL){
        	asrType = AsrType.ASR_LOCAL;
        }
		return asrType;
	}

	@Override
	public int onCommand(String cmd) {
		if (cmd.startsWith("GLOBAL_CMD_END_CHAT")) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin
					.speakTextWithClose(NativeData
							.getResString("RS_VOICE_ASR_CHAT_END_HINT"), null);
		}
		return 0;
	}

	public void endSelectSence() {
		ChoiceManager.getInstance().clearIsSelecting();
	}

	private static final int ERROR_MASK = 0x10000;

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		switch (eventId) {
		case UiEvent.EVENT_NETWORK_CHANGE: {
			switch (NetworkManager.getInstance().getNetType()) {
			case UiData.NETWORK_STATUS_2G:
			case UiData.NETWORK_STATUS_3G:
			case UiData.NETWORK_STATUS_4G:
			case UiData.NETWORK_STATUS_WIFI:
				if (mAsr != null) {
					JNIHelper
							.logd("retryImportOnlineKeywords while network change");
					mAsr.retryImportOnlineKeywords();
				}
			}
			break;
		}
		case UiEvent.EVENT_VOICE:
			switch (subEventId) {
			case VoiceData.SUBEVENT_VOICE_REG_SDK_KEYWORDS:
				try {
					VoiceData.SdkKeywords sdkKeywords = VoiceData.SdkKeywords
							.parseFrom(data);
					JNIHelper.logd("reg_sdk_kw : " + sdkKeywords.strType);
					if (mAsr != null)
						mAsr.importKeywords(sdkKeywords,
								mImportKeywordsCallback);
					else
						mImportKeywordsCallback.onError(0, sdkKeywords);
				} catch (Exception e) {
					JNIHelper.loge("reg_sdk_kw exception : " + e.toString());
				}
				break;
			case VoiceData.SUBEVENT_VOICE_UPDATE_GRAMMAR:
				try {
					VoiceData.SdkGrammar sdkGrammar = VoiceData.SdkGrammar
							.parseFrom(data);
					if (mAsr != null)
						mAsr.buildGrammar(sdkGrammar, mBuildGrammarCallback);
					else
						mBuildGrammarCallback.onError(0, sdkGrammar);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case VoiceData.SUBEVENT_VOICE_ALL_KEYWORDS_READY:
				if (mAsr != null)
					mAsr.releaseBuildGrammarData();
				break;
			case VoiceData.SUBEVENT_VOICE_RECORD_BEGIN: {
				String txt = "";
				if (data != null)
					txt = new String(data);
				RecorderWin.open(txt);
				break;
			}
			case VoiceData.SUBEVENT_VOICE_RECORD_END:
				RecorderWin.setState(RecorderWin.STATE.STATE_END);
				break;
			case VoiceData.SUBEVENT_VOICE_RECORD_CLOSE:
				if (mCloseRecordWinWhenProcEnd) {
					RecorderWin.close();
				}
				break;
			case VoiceData.SUBEVENT_VOICE_RECORD_RELEASE:
				break;
			case VoiceData.SUBEVENT_VOICE_RECORD_SHOW_ABORT:
				RecorderWin.setNetworkTipsVisibility(true);
				break;
			case VoiceData.SUBEVENT_VOICE_COMMAND_SENCE:
				setNeedCloseRecord(true);
				endSelectSence();
				break;
			case VoiceData.SUBEVENT_VOICE_SHOW_DEBUG_TEXT:
				if (ProjectCfg.DEBUG_MODE) {
					AppLogic.showToast(new String(data));
				}
				break;
			case VoiceData.SUBEVENT_VOICE_RECORD_SHOW_USER_TEXT:
				// 将用户说的话显示到聊天界面
				RecorderWin.setLastUserText(new String(data));
				break;
			case VoiceData.SUBEVENT_VOICE_RECORD_ADD_SYSTEM_TEXT:
				RecorderWin.addSystemMsg(new String(data));
				break;
			case VoiceData.SUBEVENT_VOICE_PARSE_RAWTEXT_ONLINE:
				if (data == null) {
					break;
				}
				try {
					mRawParseData = VoiceParseData.parseFrom(data);
					startWithText(mRawParseData.strVoiceData, null);
				} catch (Exception e1) {
				}
				break;
			case VoiceData.SUBEVENT_VOICE_COMMON_RESULT: {

				WinHelpTops.getInstance().dismiss();
				boolean showHelpTips = isFirstRecord;
				isFirstRecord = false;

				try {
					VoiceParseCommResult result = VoiceParseCommResult
							.parseFrom(data);

					JNIHelper.logd("asr common result: "
							+ result.uint32ResultType + ", unknow:"
							+ mUnknowCount + ",gammer="
							+ result.uint32GrammarId);

					RecorderWin.setState(STATE.STATE_END);
					
					// 当通过免唤醒启动时，出现没有说话时，变成不可识别场景
					if (LaunchManager.getInstance().mStartWithInstanceAsr
							&& result.uint32ResultType == VoiceData.COMMON_RESULT_TYPE_EMPTY) {
						result.uint32ResultType = VoiceData.COMMON_RESULT_TYPE_UNKNOW;
						LaunchManager.getInstance().mStartWithInstanceAsr = false;
					}

					if (result.uint32ResultType == VoiceData.COMMON_RESULT_TYPE_UNKNOW
							&& (result.uint32GrammarId & ERROR_MASK) != 0) {
						result.uint32ResultType = result.uint32GrammarId
								- ERROR_MASK;
					}

					RecorderWin.removeHelpTip();

					// 识别结果未知错误,出错处理
					if (result.uint32ResultType == VoiceData.COMMON_RESULT_TYPE_UNKNOW_ERROR) {
						setNeedCloseRecord(true);
						RecorderWin.speakTextWithClose(NativeData
								.getResString("RS_VOICE_ASR_UNKNOW_ERROR"),
								null);
						break;
					}

					// 识别结果连接服务器出错处理
					if (result.uint32ResultType == VoiceData.COMMON_RESULT_TYPE_NET_REQUEST_FAIL) {
						setNeedCloseRecord(true);
						noNetJudgment(false);
//						RecorderWin.speakTextWithClose(NativeData
//								.getResString("RS_VOICE_ASR_NET_REQUST_FAIL"),
//								null);
						break;
					}
					
					//识别结果中语义结果为空
					if (result.uint32ResultType == VoiceData.COMMON_RESULT_TYPE_NET_NLU_EMPTY) {
						setNeedCloseRecord(true);
						RecorderWin.speakTextWithClose(NativeData
								.getResString("RS_VOICE_ASR_NET_NLU_EMTPY"),
								null);
						break;
					}

					// 远程通用场景通知
					if (result.uint32ResultType != VoiceData.COMMON_RESULT_TYPE_PROCESSED) {
						if (result.uint32ResultType == VoiceData.COMMON_RESULT_TYPE_EMPTY
								&& SenceManager.getInstance().noneedProcSence(
										"empty", new JSONBuilder().put("action", "empty").toBytes()))
							break;

						JSONBuilder json = new JSONBuilder();
						json.put("text", result.strUserText);
						json.put("answer", result.strAnswerText);

						if (result.uint32ResultType == VoiceData.COMMON_RESULT_TYPE_UNKNOW
								&& SenceManager.getInstance().noneedProcSence(
										"unknow", json.toBytes()))
							break;

						if (result.uint32ResultType == VoiceData.COMMON_RESULT_TYPE_UNSUPPORT
								&& SenceManager.getInstance().noneedProcSence(
										"unsupport", json.toBytes()))
							break;
					}

					// 类型计数
					if (result.uint32ResultType != VoiceData.COMMON_RESULT_TYPE_UNKNOW || !TextUtils.isEmpty(result.strAnswerText)) {
						mUnknowCount = 0;
					} else {
						if (result.boolLocal) {
							RecorderWin.setLastUserText(NativeData
									.getResString("RS_USER_UNKNOW_TEXT"));
						}
						mUnknowCount++;
					}
					if (result.uint32ResultType != VoiceData.COMMON_RESULT_TYPE_EMPTY) {
						mEmptyCount = 0;
					} else {
						mEmptyCount++;
					}

					// 已处理的结果
					if (result.uint32ResultType == VoiceData.COMMON_RESULT_TYPE_PROCESSED) {
						switch (result.uint32GrammarId) {
						case VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL:
						case VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_NAVIGATE:
						case VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_SET_COMPANY:
						case VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_SET_HOME:
							if (result.uint32SessionId != null) {
								
							}else {
								endSelectSence();
							}
						case VoiceData.GRAMMAR_SENCE_NAVIGATE:
						case VoiceData.GRAMMAR_SENCE_SET_HOME:
						case VoiceData.GRAMMAR_SENCE_SET_COMPANY:
						case VoiceData.GRAMMAR_SENCE_CALL_SELECT:
							break;
						default:
							AsrManager.getInstance().mSenceRepeateCount = -1;
							break;
						}
						break;
					}

					// 来电处理重复播报
					if (result.uint32GrammarId == VoiceData.GRAMMAR_SENCE_INCOMING_MAKE_SURE) {
						JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_CALL,
								UiMakecall.SUBEVENT_INCOMING_CALL_REPEAT);
						break;
					}
					// 选择结果处理
					if (result.uint32GrammarId == VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL
							|| result.uint32GrammarId == VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_NAVIGATE
							|| result.uint32GrammarId == VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_SET_COMPANY
							|| result.uint32GrammarId == VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_SET_HOME) {
						// 选择界面中的空文本处理
						if (result.uint32ResultType == VoiceData.COMMON_RESULT_TYPE_EMPTY) {
							setNeedCloseRecord(mEmptyCount >= ASR_CHOISE_MODE_MAX_EMPTY_COUNT);
							
							if (mNeedCloseRecord){
								TtsManager.getInstance().speakText(
										NativeData.getResString("RS_VOICE_EMPTY_CLOSE"),
										new TtsUtil.ITtsCallback() {
											@Override
											public void onEnd() {
												RecorderWin.close();
											}
										});
							}else {
								TtsManager.getInstance().speakVoice(
										NativeData.getResString("RS_VOICE_EMPTY_CONTINUE"),
										TtsManager.BEEP_VOICE_URL,
										new TtsUtil.ITtsCallback() {
											@Override
											public void onEnd() {
												ChoiceManager.getInstance().selectAgain();
											}
										});
							}
						}else {
							String spk = NativeData.getResString("RS_ASR_RESELECT");
							TtsManager.getInstance().speakVoice(spk, 
									TtsManager.BEEP_VOICE_URL, 
									new TtsUtil.ITtsCallback() {
										@Override
										public void onEnd() {
											ChoiceManager.getInstance().selectAgain();
										}
							});
						}
						break;
					}
					// 选择结果处理
					// if (result.uint32GrammarId ==
					// VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL) {
					// setNeedCloseRecord(true);
					// RecorderWin.speakTextWithClose(PoiSearchActivity.mLastHintText,
					// new Runnable() {
					// @Override
					// public void run() {
					// PoiSearchActivity.selectAgain();
					// }
					// });
					// break;
					// }
					// 联系人选择结果处理
					if (result.uint32GrammarId == VoiceData.GRAMMAR_SENCE_CALL_SELECT) {
						RecorderWin.setLastUserText(null);
						TtsManager.getInstance().speakVoice(CallWorkChoice.mLastHintTts, TtsManager.BEEP_VOICE_URL,
								new TtsUtil.ITtsCallback() {
									@Override
									public void onEnd() {
										ChoiceManager.getInstance().selectAgain();
									}
								});
						break;
					}
					// 打电话结果处理
					if (result.uint32GrammarId == VoiceData.GRAMMAR_SENCE_MAKE_CALL) {
						if (result.uint32ResultType == VoiceData.COMMON_RESULT_TYPE_UNKNOW) {
							if (result.boolLocal) {
								RecorderWin
										.speakTextWithClose(
												NativeData
														.getResString("RS_VOICE_CONTACT_NOT_FOUND_LOCAL"),
												null);
							} else {
								RecorderWin
										.speakTextWithClose(
												NativeData
														.getResString("RS_VOICE_CONTACT_NOT_FOUND"),
												null);
							}
							break;
						}
					}
					// 导航结果处理
					if (result.uint32GrammarId == VoiceData.GRAMMAR_SENCE_NAVIGATE
							|| result.uint32GrammarId == VoiceData.GRAMMAR_SENCE_SET_HOME
							|| result.uint32GrammarId == VoiceData.GRAMMAR_SENCE_SET_COMPANY) {
						if (result.uint32ResultType == VoiceData.COMMON_RESULT_TYPE_UNKNOW) {
							if (result.boolLocal) {
								RecorderWin
										.speakTextWithClose(
												NativeData
														.getResString("RS_VOICE_POI_NOT_FOUND_LOCAL"),
												null);

							} else {
								RecorderWin
										.speakTextWithClose(
												NativeData
														.getResString("RS_VOICE_POI_NOT_FOUND"),
												null);
							}
							break;
						}
					}

					// 非手动的不用反馈
					if (!result.boolManual){
						if (InterruptTts.getInstance().isInterruptTTS()) {
//							//在这里判断TTS是否播报完了，播报完成就不需要打断
						}else {
							break;
						}
					}

					// 空文本处理
					if (result.uint32ResultType == VoiceData.COMMON_RESULT_TYPE_EMPTY) {
						setNeedCloseRecord((mAsrMode == AsrMode.ASR_MODE_SINGLE)
								|| (mAsrMode == AsrMode.ASR_MODE_CHAT && mEmptyCount >= ASR_CHAT_MODE_MAX_EMPTY_COUNT));
						
						RecorderWin.setLastUserText(NativeData
								.getResString("RS_USER_EMPTY_TEXT"));

						if (mNeedCloseRecord) {
							RecorderWin
									.speakTextWithClose(
											NativeData
													.getResString("RS_VOICE_EMPTY_CLOSE"),
											null);

						} else {
							if ((showHelpTips && !result.boolLocal) && WinManager.getInstance().canShowHelpTips()) {
								RecorderWin.speakTextNotEqualsDisplay(NativeData.getResString("RS_HELP_VOICE_TIPS"), null, new Runnable() {
									@Override
									public void run() {
										RecorderWin.showHelpTips();
									}
								});
							} else {
								RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_EMPTY_CONTINUE"), null);
							}

						}
						break;
					}

					// 处理回答
					if (!TextUtils.isEmpty(result.strAnswerText)
							&& result.uint32ResultType != VoiceData.COMMON_RESULT_TYPE_PROCESSED) {
						RecorderWin.enableInterruptTips = true;
						RecorderWin.speakTextWithClose(result.strAnswerText,
								new Runnable() {
									
									@Override
									public void run() {
										RecorderWin.enableInterruptTips = false;
									}
								});
						break;
					}

					// 无法识别的文字
					if (result.uint32ResultType == VoiceData.COMMON_RESULT_TYPE_UNKNOW) {
						setNeedCloseRecord((mAsrMode == AsrMode.ASR_MODE_SINGLE)
								|| (mAsrMode == AsrMode.ASR_MODE_CHAT && mUnknowCount >= ASR_CHAT_MODE_MAX_UNKNOW_COUNT));

						INDEX helpIndex = INDEX.COMMON;
						// 如果是本地识别结果，需要将其设置为...
						if (result.boolLocal) {
							RecorderWin.setLastUserText(NativeData
									.getResString("RS_USER_UNKNOW_TEXT"));

							switch (result.uint32GrammarCompileStatus) {
							case 0:
								helpIndex = INDEX.UNKNOW_LOCAL_PROCESSING;
								break;
							case 1:
								helpIndex = INDEX.UNKNOW_LOCAL_OK;
								break;
							case -1:
								helpIndex = INDEX.UNKNOW_LOCAL_ABORT;
								break;
							}

							noNetJudgment(true);
						} else {
							if (mNeedCloseRecord)
								RecorderWin
										.speakTextWithClose(
												NativeData
														.getResString("RS_VOICE_UNKNOW_WITH_BYE"),
												null);
							else {
								String answer = NativeData
										.getResString("RS_VOICE_CAN_NOT_PROC_RESULT");
								RecorderWin
										.speakTextWithClose(
												answer.replace(
														"%CONTENT%",
														NativeData
																.getResString("RS_VOICE_USUAL_SPEAK_GRAMMAR")),
												null);
							}
						}

						// 显示帮助
						if (ConfigUtil.isShowHelpInfos()) {
							WinHelpTops.getInstance().show(helpIndex);
						}
						break;
					}

					// 不支持的操作
					if (result.uint32ResultType == VoiceData.COMMON_RESULT_TYPE_UNSUPPORT) {
						if (result.strUserText.length() > 10) {
							RecorderWin
									.speakTextWithClose(
											NativeData
													.getResString("RS_VOICE_UNSUPPORT_LONG_AGAIN"),
											null);
						} else {
							String answer = NativeData
									.getResString("RS_VOICE_UNSUPPORT_OPERATE_2");
							RecorderWin.speakTextWithClose(answer, null);
						}
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case VoiceData.SUBEVENT_VOICE_SHOW_STOCK_INFO:
				RecorderWin.show();
				RecorderWin.showStockInfo(data);
				endSelectSence();
				break;
			case VoiceData.SUBEVENT_VOICE_SHOW_WEATHER_INFO:
				RecorderWin.show();
				RecorderWin.showWeatherInfo(data);
				endSelectSence();
				break;
			case VoiceData.SUBEVENT_VOICE_RECORD_SHOW_HELP:
				int index = VoiceData.HELP_INFO_COMMON;
				try {
					String sindex = new String(data, "utf-8");
					index = Integer.valueOf(sindex);
				} catch (Exception e) {
					// e.printStackTrace();
				}
				WinHelpTops.getInstance().dismiss();
				if (ConfigUtil.isShowHelpInfos()) {
					switch (index) {
					case VoiceData.HELP_INFO_LOCAL_OK:
						WinHelpTops.getInstance().show(INDEX.UNKNOW_LOCAL_OK);
						break;
					case VoiceData.HELP_INFO_LOCAL_ABORT:
						WinHelpTops.getInstance()
								.show(INDEX.UNKNOW_LOCAL_ABORT);
						break;
					case VoiceData.HELP_INFO_LOCAL_PROCESSING:
						WinHelpTops.getInstance().show(
								INDEX.UNKNOW_LOCAL_PROCESSING);
						break;
					case VoiceData.HELP_INFO_COMMON:
					default:
						WinHelpTops.getInstance().show(INDEX.COMMON);
						break;
					}
				}
				break;
			}
			break;
		}

		return super.onEvent(eventId, subEventId, data);
	}
	
	/**
	 * 没网络区分回复
	 * @param bHandleFirst 是否区分是否是第一次未识别
	 */
	private void noNetJudgment(boolean bHandleFirst) {
		int netModule = ProjectCfg.getNetModule();
		int netType = NetworkManager.getInstance().getNetType();
		if(netType == UiData.NETWORK_STATUS_WIFI){//wifi连接
			if(RecorderWin.mUnknowFirst && bHandleFirst){
				RecorderWin.speakText(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_NEW", 0),null);
				RecorderWin.mUnknowFirst = false;
			}else{
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_NEW", 1),null);
			}
		}else if(netModule == 0){
			if(SimManager.getInstance().isDataPartner() && SimManager.getInstance().mFlowControl != 0){
				if(RecorderWin.mUnknowFirst && bHandleFirst){
					RecorderWin.speakText(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_NEW", 0),null);
					RecorderWin.mUnknowFirst = false;
				}else{
					RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_NEW", 1),null);
				}
			}else{
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_NEW", 3),null);
			}
		}else if(ProjectCfg.hasNetModule()){
			if(SimManager.getInstance().isDataPartner() && SimManager.getInstance().mFlowControl != 0){//流量合作方
				if(RecorderWin.mUnknowFirst && bHandleFirst){
					RecorderWin.speakText(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_NEW", 0),null);
					RecorderWin.mUnknowFirst = false;
				}else{
					RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_NEW", 1),null);
				}
			}else{
				if(RecorderWin.mUnknowFirst && bHandleFirst){
					RecorderWin.speakText(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_NEW", 0),null);
					RecorderWin.mUnknowFirst = false;
				}else{
					RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_NEW", 2),null);
				}
			}
		}else{
			RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_NO_NET"),null);
		}
	}

	
	private long lastLaunch = 0;
	
	public byte[] invokeCommAsr(final String packageName, String command,
								final byte[] data) {
		if ("comm.asr.triggerRecordButton".equals(command)) {
			AppLogic.runOnBackGround(new Runnable() {
				@Override
				public void run() {
					//在倒车影像中，不允许启动声控
					if (TXZPowerControl.isEnterReverse()) {
						return;
					}
		            long now = SystemClock.elapsedRealtime();
		            if(now - lastLaunch < SDKFloatViewInner.CLICK_INTERVAL_LIMIT){
		                return;
		            }
					JNIHelper.logd("comm.asr.triggerRecordBtn");
		            LaunchManager.getInstance().launchWithRecord();
		            lastLaunch = now;
				}
			});
			return null;
		}
		if (command.equals("comm.asr.startWithRecordWin")) {
			AppLogic.runOnBackGround(new Runnable() {
				@Override
				public void run() {
					// 判断初始化
					if (LicenseManager.getInstance().checkInited() == false) {
						return;
					}
					//在倒车影像中，不允许启动声控
					if (TXZPowerControl.isEnterReverse()) {
						return;
					}

					WinHelpTops.getInstance().dismiss();
					// 处于电话中禁止声控
					if (!CallManager.getInstance().isIdle()) {
						AppLogic.showToast("电话中无法使用声控");
						return;
					}
					// 录音功能已禁用
					if (!ProjectCfg.isEnableRecording()) {
						AppLogic.showToast("录音功能已禁用");
						return;
					}
					// 如果已经启动录音窗口或者正在识别中，则直接返回
					if (AsrManager.getInstance().isBusy() || RecorderWin.isOpened()) {
						return;
					}

					if (WakeupManager.getInstance().mBindStyleWithWakeup) {
						ResourceManager.getInstance().setTmpStyle("");
					} else {
						ResourceManager.getInstance().setTmpStyle(null);
					}

					String hint = new String(data);
					JNIHelper.logd("comm.asr.startWithRecordWin: " + hint);
					MtjModule.getInstance().event(MtjModule.EVENTID_VOICE_MANUAL);
					LaunchManager.getInstance().saveClickVoice();
					RecorderWin.open(hint, VoiceData.GRAMMAR_SENCE_DEFAULT);
				}
			});
			return null;
		}
		if (command.equals("comm.asr.restartWithRecordWin")) {

			//在倒车影像中，不允许启动声控
			if (TXZPowerControl.isEnterReverse()) {
				return null;
			}

			// 如果已经启动录音窗口或者正在识别中，则直接返回
			if (AsrManager.getInstance().isBusy() || RecorderWin.isOpened()) {
				if (SearchEditDialog.getInstance().isShowing()) {
					ChoiceManager.getInstance().selectBackAsr();
				}
				if (SeleceCityDialog.getInstance().isShowing()) {
					ChoiceManager.getInstance().selectBackAsr();
				}
				ChoiceManager.getInstance().clearIsSelecting();
				// 取消底层识别
				NativeData.getNativeData(UiData.DATA_ID_VOICE_CANCEL_PARSE);
				AsrManager.getInstance().cancel();
				TtsManager.getInstance().cancelSpeak(RecorderWin.mSpeechTaskId);
				RecorderWin.mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
				TextManager.getInstance().cancel();
				TextResultHandle.getInstance().cancel();
				RecorderWin.notifyDismissWithoutBroadcast();
			}
			
			// 判断初始化
			if (LicenseManager.getInstance().checkInited() == false) {
				return null;
			}
			
			if (!AsrManager.getInstance().isInitSuccessed()
					|| !TtsManager.getInstance().isInitSuccessed()
					|| !WakeupManager.getInstance().isInitSuccessed()) {
				return null;
			}
			WinHelpTops.getInstance().dismiss();
			// 处于电话中禁止声控
			if (!CallManager.getInstance().isIdle()) {
				AppLogic.showToast("电话中无法使用声控");
				return null;
			}
			// 录音功能已禁用
			if (!ProjectCfg.isEnableRecording()) {
				AppLogic.showToast("录音功能已禁用");
				return null;
			}
			// 处于引导动画中禁止声控
			if (HelpGuideManager.getInstance().isAniming()) {
				AppLogic.showToast("引导中无法使用声控");
				return null;
			}
			// 启动语音引导界面
			if (HelpGuideManager.getInstance().isNeedGuideAnim()) {
				HelpGuideManager.getInstance().execGuideAnim();
				return null;
			}
			if (WakeupManager.getInstance().mBindStyleWithWakeup) {
				ResourceManager.getInstance().setTmpStyle("");
			} else {
				ResourceManager.getInstance().setTmpStyle(null);
			}

			String hint = new String(data);
			JNIHelper.logd("comm.asr.restart: " + hint);
			if(TextUtils.isEmpty(hint)){
				hint = NativeData.getResString("RS_VOICE_ASR_START_HINT");
			}
			RecorderWin.open(hint, VoiceData.GRAMMAR_SENCE_DEFAULT);
			return null;
		}
		if (command.equals("comm.asr.start")) {
			LogUtil.logd("comm.asr.start");
			AppLogic.runOnBackGround(new Runnable() {
				@Override
				public void run() {
					// 判断初始化
					if (LicenseManager.getInstance().checkInited() == false) {
						return;
					}

					//在倒车影像中，不允许启动声控
					if (TXZPowerControl.isEnterReverse()) {
						return;
					}

					WinHelpTops.getInstance().dismiss();
					// 处于电话中禁止声控
					if (!CallManager.getInstance().isIdle()) {
						AppLogic.showToast("电话中无法使用声控");
						return;
					}
					// 录音功能已禁用
					if (!ProjectCfg.isEnableRecording()) {
						AppLogic.showToast("录音功能已禁用");
						return;
					}

					AsrOption asrOption = new AsrOption();
					if (data != null) {
						JSONBuilder jsonData = new JSONBuilder(new String(data));
						String asrType = jsonData.getVal("AsrType", String.class);
						if (asrType != null) {
							if (asrType.equals("ASR_AUTO")) {
								asrOption.mAsrType = AsrType.ASR_AUTO;
							}
							if (asrType.equals("ASR_MIX")) {
								asrOption.mAsrType = AsrType.ASR_MIX;
							}
							if (asrType.equals("ASR_ONLINE")) {
								asrOption.mAsrType = AsrType.ASR_ONLINE;
							}
							if (asrType.equals("ASR_LOCAL")) {
								asrOption.mAsrType = AsrType.ASR_LOCAL;
							}
						} else {
							asrOption.mAsrType = AsrType.ASR_AUTO;
						}

						asrOption.mId = jsonData.getVal("ID", Integer.class);
						asrOption.mBOS = jsonData.getVal("BOS", Integer.class);
						asrOption.mEOS = jsonData.getVal("EOS", Integer.class);
						asrOption.mAccent = jsonData.getVal("Accent", String.class);
						asrOption.mGrammar = jsonData.getVal("Grammar", Integer.class);
						asrOption.mKeySpeechTimeout = jsonData.getVal(
								"KeySpeechTimeout", Integer.class);
						asrOption.mLanguage = jsonData.getVal("Language", String.class);
						asrOption.mManual = jsonData.getVal("Manual", Boolean.class);
					}
					IAsrCallback asrCallBack = new IAsrCallback() {
						@Override
						public void onStart(AsrOption option) {
							JNIHelper.logd("remote asr start");
							ServiceManager.getInstance().sendInvoke(packageName,
									"comm.asr.event.start", null, null);
						}

						@Override
						public void onEnd(AsrOption option) {
							ServiceManager.getInstance().sendInvoke(packageName,
									"comm.asr.event.end", null, null);
						}

						@Override
						public void onVolume(AsrOption option, int volume) {
							ServiceManager.getInstance().sendInvoke(packageName,
									"comm.asr.event.volume", ("" + volume).getBytes(),
									null);
						}

						@Override
						public void onSuccess(AsrOption option,
											  VoiceParseData oVoiceParseData) {
							JNIHelper.logd("remote asr sucess");
							ServiceManager.getInstance().sendInvoke(packageName,
									"comm.asr.event.success",
									oVoiceParseData.strVoiceData.getBytes(), null);
						}

						@Override
						public void onError(AsrOption option, int error, String desc,
											String speech, int error2) {
							ServiceManager.getInstance().sendInvoke(packageName,
									"comm.asr.event.error", ("" + error2).getBytes(),
									null);
						}

						@Override
						public void onAbort(AsrOption option, int error) {
							JNIHelper.logd("endAsr onAbort: erorr=" + error);
							ServiceManager.getInstance().sendInvoke(packageName,
									"comm.asr.event.abort", ("" + error).getBytes(),
									null);
						}

						@Override
						public void onCancel(AsrOption option) {
							JNIHelper.logd("endAsr onCancel");
							ServiceManager.getInstance().sendInvoke(packageName,
									"comm.asr.event.cancel", null, null);
						};

					};

					if (WakeupManager.getInstance().mBindStyleWithWakeup) {
						ResourceManager.getInstance().setTmpStyle("");
					} else {
						ResourceManager.getInstance().setTmpStyle(null);
					}

					AsrManager.getInstance().start(asrOption.setCallback(asrCallBack));
				}
			});
		} else if (command.equals("comm.asr.stop")) {
			AsrManager.getInstance().stop();
		} else if (command.equals("comm.asr.cancel")) {
			// 如果已经启动录音窗口或者正在识别中，则直接返回
			if (AsrManager.getInstance().isBusy() || RecorderWin.isOpened()) {
				AsrManager.getInstance().cancel();
				RecorderWin.close();
			}
		} else if (command.equals("comm.asr.regcmd")) {
			// try {
			// String dataStr = new String(data);
			// JNIHelper.logd("reg " + dataStr);
			// JSONObject json = new JSONObject(new String(data));
			// RemoteRegCmdManager.getInstance().regComand(packageName,
			// json.getString("data"), json.getString("cmd"));
			// } catch (JSONException e) {
			// e.printStackTrace();
			// }
			JSONBuilder json = new JSONBuilder(new String(data));
			RemoteRegCmdManager.getInstance().regComand(packageName,
					json.getVal("data", String.class),
					json.getVal("cmds", String[].class));

		} else if (command.equals("comm.asr.unregcmd")) {
			JSONBuilder json = new JSONBuilder(new String(data));
			RemoteRegCmdManager.getInstance().unregCommand(packageName,
					json.getVal("cmds", String[].class));
		} else if (command.equals("comm.asr.useWakeupAsAsr")) {
            JSONBuilder json = new JSONBuilder(new String(data));
            final String[] cmds = json.getVal("cmds", String[].class);
            final String tts = json.getVal("tts", String.class);
            final String taskId = json.getVal("taskId", String.class);
            final Boolean needAsrState = json.getVal("state", Boolean.class);
            final Integer priority = json.getVal("priority", Integer.class);
            IWakeupAsrCallback callback = new IWakeupAsrCallback() {
                @Override
                public boolean onAsrResult(String text) {
                    for (int i = 0; i < cmds.length; ++i) {
                        if (text.equals(cmds[i])) {
                            // 通知远端唤醒识别到了
                            JSONBuilder json = new JSONBuilder();
                            json.put("taskId", taskId);
                            json.put("text", text);
                            json.put("isWakeupResult", this.isWakeupResult());
                            ServiceManager.getInstance().sendInvoke(
                                    packageName,
                                    "comm.asr.event.onWakeupAsrResult",
                                    json.toString().getBytes(), null);
                            ReportUtil.doReport(new ReportUtil.Report.Builder().setAction("asr").setKeywords(text).setTaskID(getTaskId()).buildWakeupReport());
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void onTtsBegin() {
                    JSONBuilder json = new JSONBuilder();
                    json.put("taskId", taskId);
                    ServiceManager.getInstance().sendInvoke(packageName,
                            "comm.asr.event.onTtsBegin",
                            json.toString().getBytes(), null);
                }

                @Override
                public void onTtsEnd() {
                    JSONBuilder json = new JSONBuilder();
                    json.put("taskId", taskId);
                    ServiceManager.getInstance().sendInvoke(packageName,
                            "comm.asr.event.onTtsEnd",
                            json.toString().getBytes(), null);
                }

                @Override
                public String[] genKeywords() {
                    return cmds;
                }

                @Override
                public String needTts() {
                    return tts;
                }

                @Override
                public String getTaskId() {
                    return taskId;
                }

                @Override
                public boolean needAsrState() {
                    return needAsrState;
                }

                @Override
                public int getPriority() {
                    return priority == null ? AsrUtil.WKASR_PRIORITY_DEFAULT : priority;
                }
            };
			HelpGuideManager.getInstance().interceptTTWakeupTask(packageName, taskId, cmds);
            WakeupManager.getInstance().useWakeupAsAsr(packageName, callback);
		} else if (command.equals("comm.asr.recoverWakeupFromAsr")) {
			HelpGuideManager.getInstance().recoverTaskId(packageName, new String(data));
			WakeupManager.getInstance().recoverWakeupFromAsr(packageName,
					new String(data));
		} else if (command.equals("comm.asr.set.rawaudio")) {
			if (data == null) {
				return null;
			}
			String sSource = new String(data);
			JSONObject jSource = null;
			try {
				jSource = new JSONObject(sSource);
				String audioPath = jSource.optString("audioSourcePath");
				RecorderCenter.setSourceFile(audioPath);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else if (command.equals("comm.asr.set.beeptimeout")) {
			if (data == null) {
				return null;
			}
			try {
				int timeout = Integer.parseInt(new String(data));
				setBeepTimeout(timeout);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (command.equals("comm.asr.startWithRawText")) {
			//在倒车影像中，不允许启动声控
			if (TXZPowerControl.isEnterReverse()) {
				return null;
			}

			if (data == null) {
				return null;
			}
			try {
				String rawText = new String(data);
				startWithText(rawText);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (command.equals("comm.asr.clearAsrTool")) {
			AsrRemoteImpl.setRemoteAsrService(null);
			mAsrRemote = null;
		} else if (command.equals("comm.asr.setAsrTool")) {
			AsrRemoteImpl.setRemoteAsrService(packageName);
			mAsrRemote = new AsrRemoteImpl();
			mAsrRemote.initialize(null);
		} else if (command.equals("comm.asr.set.bos")) {
			if (data == null) {
				return null;
			}
			try {
				setBOS(Integer.parseInt(new String(data)));
			} catch (Exception e) {

			}
		} else if (command.equals("comm.asr.set.eos")) {
			if (data == null) {
				return null;
			}
			try {
				setEOS(Integer.parseInt(new String(data)));
			} catch (Exception e) {

			}
		} else if (command.equals("comm.asr.set.asrsrvmode")) {
			if (data == null) {
				return null;
			}
			String strAsrSvrMode = new String(data);
			try {
				AsrServiceMode asrServiceMode = AsrServiceMode
						.valueOf(strAsrSvrMode);
				setAsrServiceMode(asrServiceMode);
			} catch (Exception e) {

			}
		} else if (command.equals("comm.asr.set.MaxEmpty")) {
			if (data == null) {
				return null;
			}
			String count = new String(data);
			try {
				AsrManager.ASR_CHAT_MODE_MAX_EMPTY_COUNT = Integer
						.parseInt(count);
			} catch (Exception e) {
			}
		} else if (command.equals("comm.asr.set.MaxUnknow")) {
			if (data == null) {
				return null;
			}
			String count = new String(data);
			try {
				AsrManager.ASR_CHAT_MODE_MAX_UNKNOW_COUNT = Integer
						.parseInt(count);
			} catch (Exception e) {
			}
		}else if (command.equals("comm.asr.enableFMOnlineCmds")) {
			if(data ==null){
				return null;
			}
			mEnableFMOnlineCmds  = Boolean.parseBoolean(new String(data));
		} else if (command.equals("comm.asr.set.useHQualityWakeupModel")) {
			if (data == null) {
				return null;
			}
			try {
				JSONBuilder jsonBuilder = new JSONBuilder(data);
				boolean useHQualityWakeupModel = jsonBuilder.getVal("useHQualityWakeupModel", Boolean.class);
				ProjectCfg.mUseHQualityWakeupModel = useHQualityWakeupModel;
			} catch (Exception e) {
				// TODO: handle exception
			}
		} else if (command.equals("comm.asr.set.asrDelayAfterBeep")) {
			if(data == null){
				return null;
			}
			try {
				mAsrDelayAfterBeep = Integer.parseInt(new String(data));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			
		}
		return null;
	}

	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {
		mAsr.insertVocab_ext(nGrammar, vocab);
	}

	private int mFirstRecordBOS = 5000;
	private int mBOS = 3000;
	private int mSelectBOS = 10000;
	private int mEOS = 1000;
	private int mSelectEOS = 1000;

	// 设置识别录音前端超时时间
	public void setBOS(int val) {
		if (val >= 1000 && val <= 20000) {
			mBOS = val;
		}
		JNIHelper.logd("setBOS : " + val);
	}

	// 设置识别录音后端超时时间
	public void setEOS(int val) {
		if (val >= 50 && val <= 5000) {
			mEOS = val;
		}
		JNIHelper.logd("setEOS : " + val);
	}

	/**
	 * 设置录音文件路径，和是否播放录音文件
	 * 
	 * @param path
	 * @param isPlaying
	 */

	Stack<Integer> mGrammarIdStack = new Stack<Integer>();

	public void pushGrammarId(int grammarId) {
		JNIHelper.logd("pushGrammarId: " + grammarId);
		synchronized (mGrammarIdStack) {
			mGrammarIdStack.remove((Integer) grammarId);
			mGrammarIdStack.push(grammarId);
		}
	}

	public int mSenceRepeateCount = 0;

	public void popGrammarId(int grammarId) {
		JNIHelper.logd("popGrammarId: " + grammarId);
		synchronized (mGrammarIdStack) {
			mGrammarIdStack.remove((Integer) grammarId);
		}
	}
	
	public int mKeepGrammar = VoiceData.GRAMMAR_SENCE_DEFAULT;

	public int getCurrentGrammarId() {
		int ret = mKeepGrammar;
		if (!mGrammarIdStack.empty()) {
			synchronized (mGrammarIdStack) {
				ret = mGrammarIdStack.lastElement();
			}
		}
		JNIHelper.logd("getCurrentGrammarId: " + ret);
		return ret;
	}

	private VoiceParseData mRawParseData = null;
	private ITextCallBack mSysTextCallBack = new ITextCallBack() {
		@Override
		public void onResult(String jsonResult) {
			if (TextUtils.isEmpty(jsonResult)) {
				onError(IText.UnkownError);
				return;
			}
			VoiceParseData voiceData = new VoiceData.VoiceParseData();
			voiceData.uint32DataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_TEXT_JSON;
			voiceData.strVoiceData = jsonResult;
			if (mRawParseData != null) {
				JNIHelper.logd("sence=" + mRawParseData.uint32Sence);
				if (mRawParseData.strLastVoiceAnswer != null) {
					voiceData.strLastVoiceAnswer = mRawParseData.strLastVoiceAnswer;
					voiceData.uint32LastDataType = mRawParseData.uint32LastDataType;
				}
				if (mRawParseData.uint32Sence != null)
					voiceData.uint32Sence = mRawParseData.uint32Sence;
				if (mRawParseData.strVoiceEngineId != null)
					voiceData.strVoiceEngineId = mRawParseData.strVoiceEngineId;
			}
			JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_PARSE, voiceData);
		}

		@Override
		public void onError(int errorCode) {
			JNIHelper.logd("errorCode = " + errorCode);
			// 被取消掉的文本识别没有必要再处理，因为正常情况不会主动去取消优先级最高的文本识别任务
			if (errorCode == IText.InterruptedError) {
				JNIHelper.logd("need not deal Interrupted case");
				return;
			}
			VoiceParseData voiceData = new VoiceData.VoiceParseData();
			voiceData.uint32DataType = VoiceData.VOICE_DATA_TYPE_RAW;
			if (mRawParseData != null) {
				voiceData.strVoiceData = mRawParseData.strVoiceData;
				voiceData.strLastVoiceAnswer = mRawParseData.strLastVoiceAnswer;
				voiceData.uint32LastDataType = mRawParseData.uint32LastDataType;
			}
			if (errorCode == IText.NetTimeOutError)
				voiceData.uint32Sence = VoiceData.GRAMMAR_SENCE_LAST_ERROR
						+ VoiceData.COMMON_RESULT_TYPE_NET_REQUEST_FAIL;
			// result.uint32ResultType =
			// VoiceData.COMMON_RESULT_TYPE_NET_REQUEST_FAIL;
			JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_PARSE, voiceData);
		}
	};
	@Deprecated
	public void startWithText(String text, ITextCallBack callBack) {
		TextManager.getInstance().parseText(text, mSysTextCallBack,
				PreemptLevel.PREEMPT_LEVEL_IMMEDIATELY);
	}

	public void startWithText(String text) {
		RecorderWin.show();

		Runnable1<String> startRunnable = new Runnable1<String>(text) {
			@Override
			public void run() {
				VoiceParseData voiceData = new VoiceData.VoiceParseData();
				voiceData.uint32DataType = VoiceData.VOICE_DATA_TYPE_RAW;
				voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_INVALID;
				voiceData.uint32Sence = VoiceData.GRAMMAR_SENCE_DEFAULT;
//		voiceData.strVoiceData = text;
//		JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_VOICE,
//				VoiceData.SUBEVENT_VOICE_PARSE_RAWTEXT_ONLINE, voiceData);
				voiceData.strText = mP1;
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("scene", "unknown");
					jsonObject.put("action", "unknown");
					jsonObject.put("text", mP1);
					jsonObject.put("t", -1);
				} catch (JSONException e) {
				}
				voiceData.strVoiceData = jsonObject.toString();
				JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_VOICE,
						VoiceData.SUBEVENT_VOICE_PARSE_NEW, voiceData);
			}
		};
		AppLogic.runOnUiGround(startRunnable,1000);
	}

//	public void startIFlyOnlineOnly(AsrOption asrOption) {
//		if (!mUseIflyOnline)
//			return;
//		IFlyOnlineEngine.getInstance().cancel();
//		if (asrOption == null) {
//			mAsrOption = new AsrOption();
//		} else {
//			mAsrOption = asrOption;
//		}
//		mAsrOption.setNeedStopWakeup(false);
//		mUsrAsrCallback = null; // 记录用户回调
//		mAsrOption.setCallback(mSysAsrCallBack); // 替换成框架回调
//		if (mAsrOption.mKeySpeechTimeout == null)
//			mAsrOption.setKeySpeechTimeout(mKeySpeechTimeout);
//		if (mAsrOption.mBOS == null)
//			mAsrOption.setBOS(mSelectBOS);
//		if (mAsrOption.mEOS == null)
//			mAsrOption.setEOS(mSelectEOS);
//		if (mAsrOption.mGrammar == null)
//			mAsrOption.mGrammar = AsrManager.getInstance().getCurrentGrammarId();
//		mAsrOption.mAsrType = AsrType.ASR_AUTO;
//		mAsrOption.setNeedStopWakeup(false);
//		JNIHelper.logd("startAsr: mManual=" + mAsrOption.mManual + ",mGrammar="
//				+ mAsrOption.mGrammar);
//		start(mAsrOption,AsrType.ASR_AUTO);
//		getIAsr().start(mAsrOption);
//		IFlyOnlineEngine.getInstance().start(mAsrOption);
//	}
//
//	public void cancelIFlyOnlineOnly() {
//		if (!mUseIflyOnline)
//			return;
//		cancel();
//		IFlyOnlineEngine.getInstance().cancel();
//	}
	public void printNlpTimeCast() {
		JNIHelper.logd("nlp:parseNlp timeCast:"
				+ (SystemClock.elapsedRealtime() - mTimeCost));
	}
}

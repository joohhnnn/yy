package com.txznet.txz.module.call;

import java.lang.reflect.Method;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.internal.telephony.ITelephony;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.contact.ContactData.MobileContact;
import com.txz.ui.contact.ContactData.MobileContacts;
import com.txz.ui.event.UiEvent;
import com.txz.ui.makecall.UiMakecall;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ConnectionListener;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZCallManager;
import com.txznet.sdk.TXZTtsManager.ITtsCallback;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.component.call.ICall;
import com.txznet.txz.component.call.ICall.ICallStateListener;
import com.txznet.txz.component.call.dxwy.CallToolImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.contact.ContactManager;
import com.txznet.txz.module.mtj.MtjModule;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.CallUtil;
import com.txznet.txz.util.runnables.Runnable1;

/**
 * 电话管理模块，负责电话接口适配，电话状态管理，电话事件处理
 * 
 * @author bihongpi
 *
 */
public class CallManager extends IModule {
	private static final int TTS_AGAIN_INASR_TIME = 8000;
	private static final int TTS_AGAIN_NOTASR_TIME = 8000;
	private Boolean mCanProgress;

	static CallManager sModuleInstance = new CallManager();

	private CallManager() {
		try {
			mCallStateListener = new CallStateListener();
			mCall = (ICall) Class.forName(ImplCfg.getCallImplClass()).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static CallManager getInstance() {
		return sModuleInstance;
	}

	// ///////////////////////////////////////////////////////////////////

	@Override
	public int initialize_addPluginCommandProcessor() {
		PluginManager.addCommandProcessor("txz.call.", new CommandProcessor() {
			
			@Override
			public Object invoke(String command, Object[] args) {
				try {
					if("makeCall".equals(command)){
						if(!(args[0] instanceof String) || !(args[1] instanceof String)){
							return false;
						}
						String num = (String) args[0];
						String name = (String) args[1];
						makeCall(num, name);
						return true;
					}else if("makeSimpleCall".equals(command)){
						if(!(args[0] instanceof String) || !(args[1] instanceof String)){
							return false;
						}
						String num = (String) args[0];
						String name = (String) args[1];
						makeSimpleCall(num, name);
						return true;
					}else if("answerCall".equals(command)){
						return answerCall();
					}else if("rejectCall".equals(command)){
						return rejectCall();
					}else if("silenceCall".equals(command)){
						return silenceCall();
					}else if("invokeTXZCall".equals(command)){
						String callCommand = (String) args[0];
						byte[] data = (byte[]) args[1];
						return invokeTXZCall("", callCommand, data);
					}
				} catch (Exception e) {
				}
				return null;
			}
		});
		return super.initialize_addPluginCommandProcessor();
	}
	
	
	ICall mCall;
	ICallStateListener mCallStateListener;

	boolean mIncomingNeedTts = true;
	boolean mIncomingNeedAsr = true;

	public void setIncomingOption(boolean tts, boolean asr) {
		mIncomingNeedTts = tts;
		mIncomingNeedAsr = asr;
	}

	@Override
	public int initialize_AfterLoadLibrary() {
		mCall.initialize(new ICall.IInitCallback() {
			@Override
			public void onInit(boolean bSuccess) {
				if (bSuccess) {
					mCall.setStateListener(mCallStateListener);
				}
			}
		});

		regEvent(UiEvent.EVENT_SYSTEM_CALL);

		return ERROR_SUCCESS;
	}

	// ///////////////////////////////////////////////////////////////////
	
	/**
	 * 获取电话工具的包名
	 * 
	 * @return
	 */
	public String getPackageName(){
		return mCallToolServiceName;
	}

	/**
	 * 设置联系人选择是否可以自动进度条 （方易通）
	 * @param canProgress
	 */
	public void setCanProgress(boolean canProgress) {
		LogUtil.logd("setCanProgress:" + canProgress);
		this.mCanProgress = canProgress;
	}

	/**
	 * 是否允许进度条
	 * @return
	 */
	public Boolean canCallProgress() {
		return this.mCanProgress;
	}

	/**
	 * 获取状态
	 * 
	 * @return
	 */
	public int getState() {
		return mCall.getState();
	}

	public boolean isIdle() {
		return getState() == ICall.STATE_IDLE;
	}

	public boolean isRinging() {
		return getState() == ICall.STATE_RINGING;
	}

	/**
	 * 发起呼叫
	 * 
	 * @param num
	 *            号码
	 * @param name
	 *            显示名字
	 */
	public boolean makeCall(String num, String name) {
		JNIHelper.logd("enter: [" + name + "][" + num + "]");
		if (name == null)
			name = num;
		num = num.replace("-", "");
		if (!checkMakeCall()) {
			return false;
		}
		if (num.trim().isEmpty()) {
			String spk = NativeData.getResString("RS_CALL_NUMBER_NULL");
			TtsManager.getInstance().speakText(spk);
			return false;
		}
		ChoiceManager.getInstance().selectCallCancel();
		return mCall.makeCall(num, name);
	}

	/**
	 * 针对打开聊天对话框不关闭对话框
	 * 
	 * @param num
	 * @param name
	 * @return
	 */
	public boolean makeSimpleCall(String num, String name) {
		JNIHelper.logd("enter: [" + name + "][" + num + "]");
		if (name == null)
			name = num;
		num = num.replace("-", "");

		boolean isAccess = false;
		String reason = getDisableResaon();
		if (TextUtils.isEmpty(reason))
			isAccess = true;
		if (!isAccess) {
			TtsManager.getInstance().speakText(reason);
			return false;
		}

		if (num.trim().isEmpty()) {
			String spk = NativeData.getResString("RS_CALL_NUMBER_NULL");
			TtsManager.getInstance().speakText(spk);
			return false;
		}
		return mCall.makeCall(num, name);
	}

	/**
	 * 停止呼叫
	 */
	public boolean stopCall() {
		JNIHelper.logd("enter");
		return mCall.stopCall();
	}

	/**
	 * 接听来电
	 */
	public boolean answerCall() {
		JNIHelper.logd("enter");
		stopIncomingInteraction();
		return mCall.answerCall();
	}

	/**
	 * 拒接来电
	 */
	public boolean rejectCall() {
		JNIHelper.logd("enter");
		stopIncomingInteraction();
		return mCall.rejectCall();
	}

	/**
	 * 来电静音
	 */
	public boolean silenceCall() {
		JNIHelper.logd("enter");
		return mCall.silenceCall();
	}

	/**
	 * 发送数字按键
	 * 
	 * @param key
	 *            发送的按键字符串
	 */
	public boolean sendKey(String key) {
		JNIHelper.logd("enter: " + key);
		return mCall.sendKey(key);
	}

	/**
	 * 判断是否可以使用电话功能
	 * 
	 * @return
	 */
	public boolean checkMakeCall() {
		String reason = getDisableResaon();
		if (TextUtils.isEmpty(reason))
			return true;
		RecorderWin.speakTextWithClose(reason, null);
		return false;
	}

	// ///////////////////////////////////////////////////////////////////////

	public void silenceRinger() {
		try {

			TelephonyManager mTelephonyManager = (TelephonyManager) GlobalContext.get()
					.getSystemService(Context.TELEPHONY_SERVICE);
			Class<?> c = TelephonyManager.class;
			Method getITelephonyMethod = null;
			try {
				// 获取声明的方法
				getITelephonyMethod = c.getDeclaredMethod("getITelephony", (Class[]) null);
				getITelephonyMethod.setAccessible(true);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}

			try {
				ITelephony tel = (ITelephony) getITelephonyMethod.invoke(mTelephonyManager, (Object[]) null);
				tel.silenceRinger();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} catch (Throwable e) {
		}
	}

	public static final String WAKEUP_INCOMING_TASK_ID = "Incoming";
	Runnable mIncomingHintRunnable;
	int mIncomingHintTts = TtsManager.INVALID_TTS_TASK_ID;

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		if (eventId != UiEvent.EVENT_SYSTEM_CALL)
			return 0;

		MtjModule.getInstance().event(MtjModule.EVENTID_CALL);
		
		switch (subEventId) {
		case UiMakecall.SUBEVENT_MAKE_CALL_DIRECT:
		case UiMakecall.SUBEVENT_MAKE_CALL_LIST_NUMBER:
		case UiMakecall.SUBEVENT_MAKE_CALL_CHECK:
			// break;
		case UiMakecall.SUBEVENT_MAKE_CALL_NUMBER_DIRECT:
		case UiMakecall.SUBEVENT_MAKE_CALL_NUMBER:
			reportCall();
			if (!checkMakeCall())
				break;
			try {
				MobileContacts mMobileContacts = new MobileContacts();
				mMobileContacts.cons = new MobileContact[1];
				mMobileContacts.cons[0] = MobileContact.parseFrom(data);
				AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
				ChoiceManager.getInstance().showContactSelectList(subEventId, mMobileContacts);
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.loge("CallManager onEvent exp:" + e.getMessage());
			}
			break;
		case UiMakecall.SUBEVENT_MAKE_CALL_CANDIDATE:
		case UiMakecall.SUBEVENT_MAKE_CALL_LIST:
			reportCall();
			if (!checkMakeCall()) {
				break;
			}
			try {
				MobileContacts mMobileContacts = MobileContacts.parseFrom(data);
				AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
				ChoiceManager.getInstance().showContactSelectList(subEventId, mMobileContacts);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case UiMakecall.SUBEVENT_ACCEPT_CALL:
			// 接听电话
			answerCall();
			// TODO 打开接听电话窗口
			break;
		case UiMakecall.SUBEVENT_INCOMING_CALL:
			AppLogic.removeBackGroundCallback(mIncomingHintRunnable);
			TtsManager.getInstance().cancelSpeak(mIncomingHintTts);
			mIncomingHintTts = TtsManager.INVALID_TTS_TASK_ID;
			if (mIncomingNeedAsr && isRinging()) {

				AsrManager.getInstance().pushGrammarId(VoiceData.GRAMMAR_SENCE_INCOMING_MAKE_SURE);
				WakeupManager.getInstance().useWakeupAsAsr(new AsrUtil.AsrConfirmCallback(
						NativeData.getResStringArray("RS_CALL_INCOMING_ANSWER"), NativeData.getResStringArray("RS_CALL_INCOMING_REJECT")) {
					@Override
					public boolean needAsrState() {
						return true;
					}

					@Override
					public String getTaskId() {
						return WAKEUP_INCOMING_TASK_ID;
					}

					@Override
					public void onSure() {
						answerCall();
					}

					@Override
					public void onCancel() {
						rejectCall();
					};
				});
			}
			if (!mIncomingNeedTts)
				break;
			silenceRinger();
			if (isRinging()) {
				MobileContact con;
				try {
					con = MobileContact.parseFrom(data);
				} catch (InvalidProtocolBufferNanoException e) {
					e.printStackTrace();
					break;
				}
				String speech = NativeData.getResString("RS_CALL_INCOMING_IS_HANDLE_SPK");

				if (!TextUtils.isEmpty(con.name)) {
					speech = speech.replace("%SLOT%", con.name);
				} else {
					speech = speech.replace("%SLOT%", NativeData.getPhoneArea(con.phones[0]) + CallUtil.converToSpeechDigits(con.phones[0]));
				}
				if (!mIncomingNeedAsr)
					speech = NativeData.getResString("RS_CALL_INCOMING_IS_NOTICE_SPK");
				mIncomingHintRunnable = new Runnable1<String>(speech) {
					@Override
					public void run() {
						mIncomingHintTts = TtsManager.getInstance().speakVoice(mP1, mIncomingNeedAsr?TtsManager.BEEP_VOICE_URL:null,new ITtsCallback() {
							@Override
							public void onSuccess() {
								// if (mIncomingNeedAsr)
								// WakeupManager.getInstance().playAsrTipSound();
								AppLogic.removeBackGroundCallback(mIncomingHintRunnable);
								if (isRinging()) {
									AppLogic.runOnBackGround(mIncomingHintRunnable, mIncomingNeedAsr ? TTS_AGAIN_INASR_TIME : TTS_AGAIN_NOTASR_TIME);
								}
							}
						});
					}
				};
				mIncomingHintRunnable.run();
			}
			break;
		case UiMakecall.SUBEVENT_INCOMING_CALL_REPEAT: {
			if (!mIncomingNeedTts)
				break;
			// if (mIncomingTips != null)
			// mIncomingTips.speakIncomingInfo(true);
			break;
		}
		case UiMakecall.SUBEVENT_REJECT_CALL:
			rejectCall();
			break;
		case UiMakecall.SUBEVENT_CANCEL_CALL:
			stopCall();
			break;
		}

		return super.onEvent(eventId, subEventId, data);
	}

	private void reportCall() {
		ReportUtil.doReport(new ReportUtil.Report.Builder().setType("call").setAction("make")
                .setSessionId().buildCommReport());
	}

	@Override
	public int onCommand(String cmd) {
		return super.onCommand(cmd);
	}

	public void stopIncomingInteraction() {
		AsrManager.getInstance().popGrammarId(VoiceData.GRAMMAR_SENCE_INCOMING_MAKE_SURE);
		WakeupManager.getInstance().recoverWakeupFromAsr(WAKEUP_INCOMING_TASK_ID);
		AppLogic.removeBackGroundCallback(mIncomingHintRunnable);
		TtsManager.getInstance().cancelSpeak(mIncomingHintTts);
		mIncomingHintTts = TtsManager.INVALID_TTS_TASK_ID;
	}

	private String mCallToolServiceName;
	private String mDisableReason;// = "未找到拨号工具，无法使用电话功能";

	public String getDisableResaon() {
		if (TextUtils.isEmpty(mCallToolServiceName)) {
			return NativeData.getResString("RS_VOICE_NO_CALL_TOOL");
		}
		if (mDisableReason == null)
			return "";
		return mDisableReason;
	}

	public boolean hasRemoteProcTool() {
		return !TextUtils.isEmpty(mCallToolServiceName);
	}

	ConnectionListener mConnectionListener = new ConnectionListener() {
		@Override
		public void onConnected(String serviceName) {
		}

		@Override
		public void onDisconnected(String serviceName) {
			if (serviceName.equals(mCallToolServiceName)) {
				invokeTXZCall(null, "cleartool", null);
			}
		}
	};

	public byte[] invokeTXZCall(final String packageName, final String command, final byte[] data) {
		if (command.equals("sync")) {
			// 同步联系人
			try {
				MobileContacts cons = MobileContacts.parseFrom(data);
				ContactManager.getInstance().syncRemoteContacts(cons);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("canProgress")) {
			try {
				setCanProgress(Boolean.parseBoolean(new String(data)));
			} catch (Exception e) {
			}
		}
		if (command.equals("cleartool")) {
			// 记录工具
			JNIHelper.logd("clear call tool");
			mCallToolServiceName = null;
			// mDisableReason = "未找到拨号工具，无法使用电话功能";
			ServiceManager.getInstance().removeConnectionListener(mConnectionListener);
			return null;
		}
		if (command.equals("settool")) {
			JNIHelper.logd("set call tool: " + packageName);
			ServiceManager.getInstance().addConnectionListener(mConnectionListener);
			ServiceManager.getInstance().sendInvoke(packageName, "", null, new GetDataCallback() {
				@Override
				public void onGetInvokeResponse(ServiceData data) {
					// 记录电话工具
					if (data != null){
						JNIHelper.logd("set call tool success:" + packageName);
						mCallToolServiceName = packageName;
					}
				}
			});

			return null;
		}
		if (command.startsWith("notify")) {
			createHandlerThread();
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					CallToolImpl.onCallStateUpdate(command, data);
				}
			});

			return null;
		}
		if (command.equals("enable")) {
			mDisableReason = null;
			JNIHelper.logd("enableCall :"  + packageName);
			return null;
		}
		if (command.equals("disable")) {
			mDisableReason = new String(data);
			JNIHelper.logd("disableCall :" + packageName + " disableReason :" + mDisableReason);
			return null;
		}
		return null;
	}
	private volatile HandlerThread mThread;
	private Handler mHandler;
	private void createHandlerThread() {
		if (mThread == null) {
			synchronized (CallManager.class) {
				if (mThread == null) {
					mThread = new HandlerThread("call_notify");
					mThread.start();
					mHandler = new Handler(mThread.getLooper());
				}
			}
		}
	}


	public boolean sendInvoke(String command, byte[] data, GetDataCallback callback) {

		if (mCallToolServiceName == null){
			JNIHelper.loge("send command failed because call tool is null!");
			return false;
		}
		ServiceManager.getInstance().sendInvoke(mCallToolServiceName, command, data, callback);
		return true;
	}

}

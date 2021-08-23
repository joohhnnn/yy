package com.txznet.txz.component.asr.remote;

import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.jni.JNIHelper;

public class AsrRemoteImpl implements IAsr {
	private static String mRemoteAsrService = null;
	private static int mSessionId = 0;
	private static boolean mIsBusy = false;

	public static void setRemoteAsrService(String serviceName) {
		synchronized (AsrRemoteImpl.class) {
			mRemoteAsrService = serviceName;
			if (serviceName == null) {
				mIsBusy = false;
			}
			JNIHelper.logd("update remote asr service: " + mRemoteAsrService);

			ServiceManager.getInstance().sendInvoke(mRemoteAsrService, "",
					null, null);
		}
	}

	public static byte[] procRemoteResponse(String serviceName, String command,
			byte[] data) {
		synchronized (AsrRemoteImpl.class) {
			if (!serviceName.equals(mRemoteAsrService))
				return null;
			if (null == mAsrOption)
				return null;

			if (data == null)
				return null;

			JSONBuilder json = new JSONBuilder(data);
			if (json.getVal("id", Integer.class, mSessionId + 1) != mSessionId)
				return null;

			if (command.equals("onVolume")) {
				if (mAsrOption.mCallback != null) {
					mAsrOption.mCallback.onVolume(mAsrOption,
							json.getVal("volume", Integer.class, -1));
				}
				return null;
			}
			if (command.equals("onSenceResult")) {
				VoiceParseData voiceData = new VoiceParseData();
				voiceData.boolManual = (mAsrOption.mManual == null || mAsrOption.mManual == true) ? 1
						: 0;
				voiceData.strVoiceData = json.getVal("data", String.class);
				voiceData.uint32DataType = VoiceData.VOICE_DATA_TYPE_TXZ_SENCE;
				voiceData.uint32Sence = mAsrOption.mGrammar;
				mIsBusy = false;
				AsrOption opt = mAsrOption;
				mAsrOption = null;
				if (opt.mCallback != null) {
					opt.mCallback.onSuccess(opt, voiceData);
				}
				return null;
			}
			if (command.equals("onEndSpeech")) {
				if (mAsrOption.mCallback != null) {
					mAsrOption.mCallback.onSpeechEnd(mAsrOption);
				}
				return null;
			}
			if (command.equals("onEndRecord")) {
				if (mAsrOption.mCallback != null) {
					mAsrOption.mCallback.onEnd(mAsrOption);
				}
				return null;
			}
			if (command.equals("onCancel")) {
				mIsBusy = false;
				AsrOption opt = mAsrOption;
				mAsrOption = null;
				if (opt.mCallback != null) {
					opt.mCallback.onCancel(opt);
				}
				return null;
			}
			if (command.equals("onBeginSpeech")) {
				if (mAsrOption.mCallback != null) {
					mAsrOption.mCallback.onSpeechBegin(mAsrOption);
				}
				return null;
			}
			if (command.equals("onAbort")) {
				mIsBusy = false;
				AsrOption opt = mAsrOption;
				mAsrOption = null;
				if (opt.mCallback != null) {
					opt.mCallback.onAbort(opt, 0);
				}
				return null;
			}
			if (command.equals("onError")) {
				String errDesc = json.getVal("errDesc", String.class, "未知错误");
				String errHint = json.getVal("errHint", String.class, errDesc);
				mIsBusy = false;
				AsrOption opt = mAsrOption;
				mAsrOption = null;
				if (opt.mCallback != null) {
					opt.mCallback.onError(opt,
							json.getVal("errCode", Integer.class, ERROR_CODE),
							errDesc, errHint, ERROR_CODE);
				}
				return null;
			}
			return null;
		}
	}

	@Override
	public int initialize(final IInitCallback oRun) {
		synchronized (AsrRemoteImpl.class) {
			ServiceManager.getInstance().sendInvoke(mRemoteAsrService,
					"tool.asr.init", null, new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							oRun.onInit(data != null);
						}
					});
		}
		return 0;
	}

	@Override
	public void release() {
	}

	static AsrOption mAsrOption = null;

	@Override
	public int start(AsrOption oOption) {
		if (mIsBusy)
			return ERROR_ASR_ISBUSY;
		mIsBusy = true;
		synchronized (AsrRemoteImpl.class) {
			mAsrOption = oOption;

			JSONBuilder json = new JSONBuilder();
			json.put("id", ++mSessionId);
			json.put("BOS", mAsrOption.mBOS);
			json.put("EOS", mAsrOption.mEOS);
			json.put("Manual", mAsrOption.mManual);
			json.put("KeySpeechTimeout", mAsrOption.mKeySpeechTimeout);

			ServiceManager.getInstance().sendInvoke(mRemoteAsrService,
					"tool.asr.start", json.toBytes(), new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							// 调用超时
							if (data == null) {
								mIsBusy = false;
								AsrOption opt = mAsrOption;
								mAsrOption = null;
								if (opt != null && opt.mCallback != null) {
									opt.mCallback.onAbort(opt, 0);
								}
							}
						}
					});
		}
		return ERROR_SUCCESS;
	}

	@Override
	public void stop() {
		synchronized (AsrRemoteImpl.class) {
			ServiceManager.getInstance().sendInvoke(mRemoteAsrService,
					"tool.asr.stop", null, null);
		}
	}

	@Override
	public void cancel() {
		synchronized (AsrRemoteImpl.class) {
			ServiceManager.getInstance().sendInvoke(mRemoteAsrService,
					"tool.asr.cancel", null, new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							mIsBusy = false;
							AsrOption opt = mAsrOption;
							mAsrOption = null;
							if (opt != null && opt.mCallback != null) {
								opt.mCallback.onCancel(opt);
							}
						}
					});
		}
	}

	@Override
	public boolean isBusy() {
		return mIsBusy;
	}

	@Override
	public boolean buildGrammar(final SdkGrammar oGrammarData,
			final IBuildGrammarCallback oCallback) {
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				oCallback.onSuccess(oGrammarData);
			}
		}, 0);
		return true;
	}

	@Override
	public boolean importKeywords(final SdkKeywords oKeywords,
			final IImportKeywordsCallback oCallback) {
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				oCallback.onSuccess(oKeywords);
			}
		}, 0);
		return true;
	}

	@Override
	public void releaseBuildGrammarData() {

	}
	
	@Override
	public void retryImportOnlineKeywords() {
		
	}

	@Override
	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {

	}

}

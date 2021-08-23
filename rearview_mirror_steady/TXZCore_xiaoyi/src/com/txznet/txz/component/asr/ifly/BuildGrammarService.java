package com.txznet.txz.component.asr.ifly;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;
import com.txz.ui.voice.VoiceData;
import com.txznet.txz.jni.JNIHelper;

public class BuildGrammarService extends Service {
	class IncomingHandler extends Handler {
		public IncomingHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MsgConst.MSG_REGISTER_CLIENT:
				client = msg.replyTo;
				initSDK(msg.getData().getString("appId"));
				break;
			case MsgConst.MSG_EXIT_SERVICE:
				android.os.Process.killProcess(android.os.Process.myPid());
				break;
			default:
				handleClientMsg(msg);
			}
		}
	}

	SpeechRecognizer mIat = null;

	void initSDK(String appId) {
		// Log.d(Tag, "SpeechRecognizer init begin");

		SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + appId
				+ "," + SpeechConstant.ENGINE_MODE + "="
				+ SpeechConstant.MODE_MSC + "," + SpeechConstant.FORCE_LOGIN
				+ "=true");

		mIat = SpeechRecognizer.createRecognizer(this, new InitListener() {
			@Override
			public void onInit(int code) {
				if (code == ErrorCode.SUCCESS) {
					// Log.d(Tag, "SpeechRecognizer init() success");
					Message msg = new Message();
					msg.what = MsgConst.MSG_READY;
					sendMsgBack(msg);
				} else {
					JNIHelper.logd("SpeechRecognizer init() code = " + code);
					android.os.Process.killProcess(android.os.Process.myPid()); // 异常直接退出
				}
			}
		});
	}

	final Messenger mMessenger = new Messenger(new IncomingHandler(
			Looper.getMainLooper()));
	Messenger client = null;

	@Override
	public IBinder onBind(Intent intent) {
		// Log.d(Tag, "onBind");
		return mMessenger.getBinder();
	}

	public void sendMsgBack(Message msg) {
		if (client != null) {
			try {
				client.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	void setGrammarParams(VoiceData.GrammarInfo grammarInfo) {
		mIat.setParameter(SpeechConstant.PARAMS, null);
		if (grammarInfo != null && grammarInfo.strId != null
				&& grammarInfo.strId.length() > 0) {
			mIat.setParameter(ResourceUtil.GRM_BUILD_PATH,
					grammarInfo.strBuildPath);
			// 设置资源路径
			mIat.setParameter(ResourceUtil.ASR_RES_PATH, ResourceUtil
					.generateResourcePath(this, RESOURCE_TYPE.assets,
							"asr/common.jet"));
			mIat.setParameter(SpeechConstant.GRAMMAR_LIST, grammarInfo.strId);
			mIat.setParameter(SpeechConstant.ENGINE_TYPE,
					SpeechConstant.TYPE_LOCAL);
		} else {
			mIat.setParameter(SpeechConstant.ENGINE_TYPE,
					SpeechConstant.TYPE_CLOUD);
		}
		mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");

		// Log.d(Tag, mIat.getParameter(SpeechConstant.PARAMS));
	}

	class TXZLexiconListener implements LexiconListener {
		VoiceData.SdkKeywords mParam;

		public TXZLexiconListener(VoiceData.SdkKeywords param) {
			mParam = param;
		}

		@Override
		public void onLexiconUpdated(String lexiconId, SpeechError error) {
			String grammar = "online";
			if (mParam.msgGrammarInfo != null
					&& mParam.msgGrammarInfo.strId != null) {
				grammar = mParam.msgGrammarInfo.strId;
			}
			if (error != null) {
				JNIHelper.loge("更新" + grammar + "语法的" + mParam.strType
						+ "词库失败: " + error.getErrorCode() + "-"
						+ error.getErrorDescription());
				android.os.Process.killProcess(android.os.Process.myPid());
			} else {
				JNIHelper.logd("更新" + grammar + "语法的" + mParam.strType
						+ "词库成功: len=" + mParam.strContent.length());
				Message msg = new Message();
				msg.what = MsgConst.MSG_SUCCESS;
				sendMsgBack(msg);
			}
		}
	};

	public void handleClientMsg(Message msg) {
		switch (msg.what) {
		case MsgConst.MSG_REG_KEYWORDS: {
			Bundle b = msg.getData();
			byte[] data = b.getByteArray("data");
			try {
				VoiceData.SdkKeywords sdkKeywords = VoiceData.SdkKeywords
						.parseFrom(data);
				setGrammarParams(sdkKeywords.msgGrammarInfo);
				TXZLexiconListener lexiconListener = new TXZLexiconListener(
						sdkKeywords);
				int ret = mIat.updateLexicon(sdkKeywords.strType,
						sdkKeywords.strContent, lexiconListener);
				if (ret != ErrorCode.SUCCESS) {
					SpeechError error = new SpeechError(0, "异常码:(" + ret + ")");
					lexiconListener.onLexiconUpdated("", error);
				}
			} catch (Exception e) {
				e.printStackTrace();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
			break;
		}
		case MsgConst.MSG_EXIT_SERVICE:
			android.os.Process.killProcess(android.os.Process.myPid());
			break;
		default:
			break;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		//Setting.showLogcat(false);
		// Log.d(Tag, "onCreate");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// Log.d(Tag, "onUnbind");
		boolean ret = super.onUnbind(intent);
		android.os.Process.killProcess(android.os.Process.myPid());
		return ret;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Log.d(Tag, "onDestroy");
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}

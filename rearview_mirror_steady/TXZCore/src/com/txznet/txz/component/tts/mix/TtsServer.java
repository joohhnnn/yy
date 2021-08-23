package com.txznet.txz.component.tts.mix;

import java.io.File;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.component.tts.ITts.IInitCallback;

public class TtsServer {

	// private String mStrEngine = null;

	private HandlerThread mWorkThread = null;
	private Handler mHandler = null;

	private Messenger mMessenger = null;
	private Messenger mClient = null;

	private ITts mTts = null;

	public TtsServer(String strEngine) {
		mWorkThread = new HandlerThread("TtsServer");
		mWorkThread.start();
		mHandler = new Handler(mWorkThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				handleMsg(msg);
			}

		};
		mMessenger = new Messenger(mHandler);
		// mStrEngine = strEngine;
	}

	public Messenger getMessenger() {
		return mMessenger;
	}

	private void handleMsg(Message msg) {
		mClient = msg.replyTo;
		Bundle b = msg.getData();
		switch (msg.what) {
		case TtsMsgConstants.MSG_REQ_INIT:
			// 只是为了兼容使用内部引擎代码中使用到的APPID
			ProjectCfg.setIflyAppId(b.getString(TtsMsgConstants.APPID_STR));
			ProjectCfg.setYunzhishengAppId(b.getString(TtsMsgConstants.APPKEY_STR));
			ProjectCfg.setYunzhishengSecret(b.getString(TtsMsgConstants.SECRET_STR));

			mTts = initEngine(b.getString(TtsMsgConstants.TTS_PKT_FILE_PATH_STR));
			break;
		case TtsMsgConstants.MSG_REQ_START:
			if (mTts == null) {
				ProjectCfg.setIflyAppId(b.getString(TtsMsgConstants.APPID_STR));
				ProjectCfg.setYunzhishengAppId(b.getString(TtsMsgConstants.APPKEY_STR));
				ProjectCfg.setYunzhishengSecret(b.getString(TtsMsgConstants.SECRET_STR));

				mTts = initEngine(b.getString(TtsMsgConstants.TTS_PKT_FILE_PATH_STR));
			}
			
			if (mTts == null) {
				mTtsCallback.onError(TtsMsgConstants.ERROR_CODE_TTS_NULL);
			} else {
				int iStream = b.getInt(TtsMsgConstants.TTS_START_STREAM_INT);
				String sText = b.getString(TtsMsgConstants.TTS_START_TEXT_STR);
				mTts.start(iStream, sText, mTtsCallback);
			}
			break;
		case TtsMsgConstants.MSG_REQ_STOP:
			if (mTts != null) {
				mTts.stop();
			}
			break;
		case TtsMsgConstants.MSG_REQ_RELEASE:
			if (mTts != null) {
				mTts.release();
			}
			LogUtil.logd("tts theme : TtsServer : process will exit");
			System.exit(0);
			break;
		default:
		}
	}

	private void sendMsg(int what, Bundle b) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.setData(b);
		try {
			if (mClient != null) {
				mClient.send(msg);
			}
		} catch (RemoteException e) {
			LogUtil.loge("tts theme : TtsServer : init tts : ERROR : " + e.toString());
		}
	}

	/**
	 * 创建TTS引擎，并且初始化TTS
	 * 
	 * @param filePath
	 * @return
	 */
	private ITts initEngine(String filePath) {
		LogUtil.logd("tts theme : TtsServer : init tts : " + filePath);

		TtsEngine ttsEngine = null;
		if (!TextUtils.isEmpty(filePath)) {
			ttsEngine = new OuterTtsEngine(new File(filePath));
		}

		ITts tts = null;
		if (ttsEngine != null) {
			tts = ttsEngine.getEngine();
		}

		InitCallback mInitCallback = new InitCallback();
		if (tts == null) {
			mInitCallback.onInit(false);
			LogUtil.loge("tts theme : TtsServer : init tts : ERROR : " + filePath);
		} else {
			tts.initialize(mInitCallback);
		}
		return tts;
	}

	private class InitCallback implements IInitCallback {

		@Override
		public void onInit(boolean bSuccess) {
			LogUtil.loge("tts theme : TtsServer : init tts : " + bSuccess);
			Bundle b = new Bundle();
			b.putBoolean(TtsMsgConstants.TTS_INIT_RESULT_BOOL, bSuccess);
			sendMsg(TtsMsgConstants.MSG_NOTIFY_INIT_RESULT, b);
		}

	}

	private ITtsCallback mTtsCallback = new ITtsCallback() {

		@Override
		public void onEnd() {
			sendMsg(TtsMsgConstants.MSG_NOTIFY_CALLBACKE_END, null);
		}

		@Override
		public void onCancel() {
			sendMsg(TtsMsgConstants.MSG_NOTIFY_CALLBACKE_CANCEL, null);
		}

		@Override
		public void onSuccess() {
			sendMsg(TtsMsgConstants.MSG_NOTIFY_CALLBACKE_SUCCESS, null);
		}

		@Override
		public void onError(int iError) {
			Bundle b = new Bundle();
			b.putInt(TtsMsgConstants.TTS_CALLBACK_ERROR_INT, iError);
			sendMsg(TtsMsgConstants.MSG_NOTIFY_CALLBACKE_ERROR, b);
		}
	};
}

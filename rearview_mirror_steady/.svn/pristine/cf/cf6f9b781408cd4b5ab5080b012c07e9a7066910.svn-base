package com.txznet.txz.component.ttsplayer.proxy;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.sdk.TXZTtsPlayerManager;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.component.tts.ITts.IInitCallback;
import com.txznet.txz.component.ttsplayer.yzs.TtsYunzhishengImpl;


public class TtsPlayerServer {

    private HandlerThread mWorkThread = null;
    private Handler mHandler = null;

    private Messenger mMessenger = null;
    private Messenger mClient = null;

    private ITts mTts = null;

    public TtsPlayerServer() {
        mWorkThread = new HandlerThread("TtsPlayerServer");
        mWorkThread.start();
        mHandler = new Handler(mWorkThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                handleMsg(msg);
            }

        };
        mMessenger = new Messenger(mHandler);
    }

    public Messenger getMessenger() {
        return mMessenger;
    }

    private void handleMsg(Message msg) {
        mClient = msg.replyTo;
        Bundle b = msg.getData();
        switch (msg.what) {
            case TtsPlayerMsgConstants.MSG_REQ_INIT:
                // 只是为了兼容使用内部引擎代码中使用到的APPID
                ProjectCfg.setYunzhishengAppId(b.getString(TtsPlayerMsgConstants.APPKEY_STR));
                ProjectCfg.setYunzhishengSecret(b.getString(TtsPlayerMsgConstants.SECRET_STR));

                mTts = initEngine();
                break;
            case TtsPlayerMsgConstants.MSG_REQ_START:
                if (mTts == null) {
                    ProjectCfg.setYunzhishengAppId(b.getString(TtsPlayerMsgConstants.APPKEY_STR));
                    ProjectCfg.setYunzhishengSecret(b.getString(TtsPlayerMsgConstants.SECRET_STR));

                    mTts = initEngine();
                }

                if (mTts == null) {
                    mTtsCallback.onError(TtsPlayerMsgConstants.ERROR_CODE_TTS_NULL);
                } else {
                    int iStream = b.getInt(TtsPlayerMsgConstants.TTS_START_STREAM_INT);
                    String sText = b.getString(TtsPlayerMsgConstants.TTS_START_TEXT_STR);
                    mTts.start(iStream, sText, mTtsCallback);
                }
                break;
            case TtsPlayerMsgConstants.MSG_REQ_STOP:
                if (mTts != null) {
                    mTts.stop();
                }
                break;
            case TtsPlayerMsgConstants.MSG_REQ_RELEASE:
                if (mTts != null) {
                    mTts.release();
                }
                LogUtil.logd("TtsPlayerServer : process will exit");
                System.exit(0);
                break;
            case TtsPlayerMsgConstants.MSG_REQ_PAUSE:
                if (mTts != null) {
                    mTts.pause();
                }
                break;
            case TtsPlayerMsgConstants.MSG_REQ_RESUME:
                if (mTts != null) {
                    mTts.resume();
                }
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
            LogUtil.loge("TtsPlayerServer : init tts : ERROR : " + e.toString());
        }
    }

    /**
     * 创建TTS引擎，并且初始化TTS
     *
     * @return
     */
    private ITts initEngine() {
        LogUtil.logd(" TtsPlayerServer : init tts ");

        ITts mTts = new TtsYunzhishengImpl();

        InitCallback mInitCallback = new InitCallback();
        mTts.initialize(mInitCallback);
        return mTts;
    }

    private class InitCallback implements IInitCallback {

        @Override
        public void onInit(boolean bSuccess) {
            LogUtil.loge("TtsPlayerServer : init tts : " + bSuccess);
            Bundle b = new Bundle();
            b.putBoolean(TtsPlayerMsgConstants.TTS_INIT_RESULT_BOOL, bSuccess);
            sendMsg(TtsPlayerMsgConstants.MSG_NOTIFY_INIT_RESULT, b);
        }

    }

    private TXZTtsPlayerManager.ITtsCallback mTtsCallback = new TXZTtsPlayerManager.ITtsCallback() {

        @Override
        public void onEnd() {
            sendMsg(TtsPlayerMsgConstants.MSG_NOTIFY_CALLBACKE_END, null);
        }

        @Override
        public void onCancel() {
            sendMsg(TtsPlayerMsgConstants.MSG_NOTIFY_CALLBACKE_CANCEL, null);
        }

        @Override
        public void onSuccess() {
            sendMsg(TtsPlayerMsgConstants.MSG_NOTIFY_CALLBACKE_SUCCESS, null);
        }

        @Override
        public void onError(int iError) {
            Bundle b = new Bundle();
            b.putInt(TtsPlayerMsgConstants.TTS_CALLBACK_ERROR_INT, iError);
            sendMsg(TtsPlayerMsgConstants.MSG_NOTIFY_CALLBACKE_ERROR, b);
        }

        @Override
        public void onResume() {
            sendMsg(TtsPlayerMsgConstants.MSG_NOTIFY_CALLBACKE_RESUME, null);
        }

        @Override
        public void onPause() {
            sendMsg(TtsPlayerMsgConstants.MSG_NOTIFY_CALLBACKE_PAUSE, null);
        }
    };
}

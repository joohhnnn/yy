package com.txznet.webchat.actions;

import android.os.Bundle;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.webchat.dispatcher.Dispatcher;

import org.json.JSONObject;

public class UploadVoiceActionCreator {
    private static UploadVoiceActionCreator sInstance;
    private Dispatcher dispatcher;

    UploadVoiceActionCreator(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static UploadVoiceActionCreator get() {
        if (sInstance == null) {
            synchronized (UploadVoiceActionCreator.class) {
                if (sInstance == null) {
                    sInstance = new UploadVoiceActionCreator(Dispatcher.get());
                }
            }
        }
        return sInstance;
    }

    @Deprecated
    public void uploadVoice(String path, int length) {
        JSONObject req = new JSONObject();
        try {
            req.put("path", path);
            req.put("length", length);
            int id = ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "wx.upload.voice",
                    req.toString().getBytes(), null);
            Bundle bundle = new Bundle();
            bundle.putString("path", path);
            bundle.putInt("length", length);
            bundle.putLong("id", id);
            dispatcher.dispatch(new Action<>(ActionType.VOICE_UPLOAD_REQ, bundle));
        } catch (Exception e) {
        }
    }

    public void notifyUploadVoice() {
        Bundle bundle = new Bundle();
        bundle.putLong("id", (int) System.currentTimeMillis());
        dispatcher.dispatch(new Action<>(ActionType.VOICE_UPLOAD_REQ, bundle));
    }

    public void notifyUploadVoiceSucc(String url, int voiceLength) {
        notifyUploadVoiceSucc("", url, voiceLength);
    }

    public void notifyUploadVoiceSucc(String txt, String url, int voiceLength) {
        Bundle bundle = new Bundle();
        bundle.putString("txt", txt);
        bundle.putString("url", url);
        bundle.putInt("length", voiceLength);
        dispatcher.dispatch(new Action<>(ActionType.VOICE_UPLOAD_RESP, bundle));
    }

    public void notifyUploadVoiceError(String desc) {
        dispatcher.dispatch(new Action<>(ActionType.VOICE_UPLOAD_RESP_ERROR, desc));
    }
}

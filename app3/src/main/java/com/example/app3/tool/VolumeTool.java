package com.example.app3.tool;

import android.media.AudioManager;

import com.example.app3.MyApplication;
import com.example.app3.util.JsonUtil;
import com.txznet.adapter.aidl.TXZAIDLManager;

public class VolumeTool {
    private final String TAG = "VolumeTool";

    private static VolumeTool instance;

    private VolumeTool(){}

    public static VolumeTool getInstance(){
        if (instance == null){
            instance = new VolumeTool();
        }
        return instance;
    }

    private AudioManager mAudioManager = (AudioManager) MyApplication.getInstance().getSystemService(MyApplication.getInstance().AUDIO_SERVICE);
    private int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

    private int getCurrentVolume(){
        return mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
    }

    public void incVolume(){
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        sendCurVolume();
    }

    public void decVolume(){
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        sendCurVolume();
    }

    public void setMaxVolume(){
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_SHOW_UI);
        sendCurVolume();
    }

    public void setMinVolume(){
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_SHOW_UI);
        sendCurVolume();
    }

    private void sendCurVolume(){
        TXZAIDLManager.getInstance().sendCommand(2030, "volume.number", JsonUtil.transParamToJson("number", getCurrentVolume()).getBytes(), false);
    }
}

package com.txznet.marketing.util;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import com.txznet.marketing.MainActivity;
import com.txznet.marketing.R;
import com.txznet.marketing.bean.CommandPoint;

import java.util.HashMap;
import java.util.Random;

public class VocieUtil {

    private static VocieUtil instance;
    private HashMap<Integer,Integer> voiceId;
    private SoundPool mSoundPool;

    public static VocieUtil getInstance(){
        if (instance == null){
            instance = new VocieUtil();
        }
        return instance;
    }

    public void loadVoice(){
        voiceId = new HashMap<Integer, Integer>();
        //创建SoundPool对象
        mSoundPool = new SoundPool(3,AudioManager.STREAM_VOICE_CALL,5);
        /*voiceId.put(1,mSoundPool.load(MainActivity.getInstance(), R.raw.voice1,1));
        voiceId.put(2,mSoundPool.load(MainActivity.getInstance(), R.raw.voice2,1));
        voiceId.put(3,mSoundPool.load(MainActivity.getInstance(), R.raw.voice3,1));*/
    }

    public void playVoice(){
        Random r = new Random();
        //播放指定的音频流
        int i = r.nextInt(3);
        Log.d("jack", "playVoice:---------------");
        int j = mSoundPool.play(voiceId.get(i),1,1, 0, 0, 1);
        Log.d("jack", "playVoice: "+j);
    }
}

package com.txznet.txz.component.tts.yunzhisheng_3_0;

import com.txznet.txz.component.tts.yunzhisheng_3_0.AudioTrackPlayer.Decryption;
import com.txznet.txz.component.tts.yunzhisheng_3_0.AudioTrackPlayer.OnCompletionListener;

import android.media.AudioManager;

public class Player {
     private static Player sIntance = new Player();
     private AudioTrackPlayer mAudioTrackPlayer = null;
     private Player(){
    	 mAudioTrackPlayer = new AudioTrackPlayer();
     }
     
     public static Player getInstance(){
    	 return sIntance;
     }
     
     public void start(String path, final Runnable oRun){
    	 mAudioTrackPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(AudioTrackPlayer player) {
				if (oRun != null){
					oRun.run();
				}
			}
		});
		try {
			mAudioTrackPlayer.setDataSource(path);
		} catch (Exception e) {
			return;
		}
		mAudioTrackPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    	 mAudioTrackPlayer.prepare();
    	 mAudioTrackPlayer.setDecryption(new Decryption() {
			@Override
			public void decrypt(byte[] data, int offset, int size, long offsetInFile) {
				//decryptAudio(data, offset, size, offsetInFile, getKey());
			}
		});
    	 mAudioTrackPlayer.start();
     }
     
     public void stop(){
    	 mAudioTrackPlayer.stop();
     }
 	
}

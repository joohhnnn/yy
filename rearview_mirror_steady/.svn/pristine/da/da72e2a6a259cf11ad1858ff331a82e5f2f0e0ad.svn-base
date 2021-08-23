package com.txznet.music.playerModule.logic.factory;

import com.txznet.audio.player.RemoteAudioPlayer;
import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.music.albumModule.bean.Audio;

public class TxzAudioPlayerFactory {

    public static TXZAudioPlayer createPlayer(Audio currentAudio) {
        TXZAudioPlayer audioPlayer = null;
        if (currentAudio == null) {
            return audioPlayer;
        }
        //多进程方案
        audioPlayer = RemoteAudioPlayer.createAudioPlayer(currentAudio);
//			audioPlayer =  new RemoteAudioPlayer();

        //单进程方案
//		audioPlayer =new
//		}

//		if (null == audioPlayer) {
//			SystemClock.sleep(200);
//			return createPlayer(currentAudio);
//		}
        return audioPlayer;
    }
}

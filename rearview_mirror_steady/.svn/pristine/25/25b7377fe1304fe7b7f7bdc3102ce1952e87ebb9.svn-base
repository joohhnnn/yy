package com.txznet.music.engine.factory;

import com.txznet.audio.player.RemoteAudioPlayer;
import com.txznet.audio.player.SessionManager.SessionInfo;
import com.txznet.audio.player.SysAudioPlayer;
import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.audio.player.audio.FileAudio;
import com.txznet.music.Constant;
import com.txznet.music.bean.response.Audio;

public class TxzAudioPlayerFactory {

	public static TXZAudioPlayer createPlayer(Audio currentAudio) {
		TXZAudioPlayer audioPlayer = null;
		if (currentAudio == null) {
			return audioPlayer;
		}

//		if ((currentAudio.getSid() == Constant.LOCAL_MUSIC_TYPE && !currentAudio
//				.getStrDownloadUrl().endsWith(".tmd"))
//				|| ((currentAudio.getStrDownloadUrl().startsWith("http"))&&(currentAudio.getStrDownloadUrl().endsWith(".m4a")))) {
//			audioPlayer = new SysAudioPlayer(new SessionInfo(new FileAudio(
//					currentAudio.getStrDownloadUrl())),
//					currentAudio.getStrDownloadUrl());
//		} else {
			audioPlayer = RemoteAudioPlayer.createAudioPlayer(currentAudio);
//		}
		return audioPlayer;
	}
}

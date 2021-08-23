package com.txznet.audio.player.factory;

import com.txznet.audio.player.audio.FileAudio;
import com.txznet.audio.player.audio.NetAudio;
import com.txznet.audio.player.audio.PlayerAudio;
import com.txznet.audio.player.audio.QQMusicAudio;
import com.txznet.music.Constant;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.utils.StringUtils;

import android.text.TextUtils;

public class PlayAudioFactory {

	public static PlayerAudio createPlayAudio(Audio currentAudio) {

		PlayerAudio playAudio = null;
		if (TextUtils.equals("1", currentAudio.getDownloadType())&&(currentAudio.getSid()!=Constant.LOCAL_MUSIC_TYPE)) {
			playAudio = new QQMusicAudio(currentAudio);
		} else if (StringUtils.isNotEmpty(currentAudio.getStrDownloadUrl())
				&& !currentAudio.getStrDownloadUrl().startsWith("http")) {
			playAudio = new FileAudio(currentAudio.getStrDownloadUrl());
		} else {
			playAudio = new NetAudio(currentAudio);
		}

		return playAudio;
	}
}

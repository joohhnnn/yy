package com.txznet.music.bean.response;

import com.txznet.music.utils.CollectionUtils;

/**
 * 共有信息
 * 
 * @author ASUS User
 *
 */
public class BaseAudio {
	public static final int MUSIC_TYPE=1;
	public static final int ALBUM_TYPE=2;
	
	private Audio audio;
	private Album album;

	public Audio getAudio() {
		return audio;
	}

	public void setAudio(Audio audio) {
		this.audio = audio;
	}

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Album album) {
		this.album = album;
	}

	public int getType() {
		if (audio != null) {
			return 1;
		} else if (album != null) {
			return 2;
		}
		return 0;
	}

}

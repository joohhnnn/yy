package com.txznet.fm.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.txznet.music.bean.response.Audio;

/**
 * 音乐管理类
 * 
 * @author ASUS User
 *
 */
public class MediaManager implements Observer {

	List<Audio> audios = new ArrayList<Audio>();

	Audio audio;//当前播放的歌曲
	
	private int playIndex = -1;

	private MediaManager() {
	}

	private static MediaManager manager;

	public static MediaManager getInstance() {
		if (manager == null) {
			synchronized (MediaManager.class) {
				if (manager == null) {
					manager = new MediaManager();
				}
			}
		}
		return manager;
	}

	@Override
	public void update(Observable observable, Object data) {

	}

}

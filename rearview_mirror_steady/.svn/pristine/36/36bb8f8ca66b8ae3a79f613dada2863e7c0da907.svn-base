package com.txznet.music.fragment.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.txznet.fm.bean.InfoMessage;
import com.txznet.music.bean.response.Audio;

/**
 * 播放管理器,存放当前播放状态，当前播放歌曲，播放列表
 * 
 * @author ASUS User
 *
 */
public class MediaManager implements Observer {

	// 单例
	private static MediaManager instance;

	private MediaManager() {
	}

	public static MediaManager getInstance() {
		if (instance == null) {
			synchronized (MediaManager.class) {
				if (instance == null) {
					instance = new MediaManager();
				}
			}
		}
		return instance;
	}

	public enum Status {
		Init, // 初始化
		prepared, // 准备完毕
		play, // 播放中
		pause, // 暂停
		release// 释放
	}

	private List<Audio> mAudios = new ArrayList<Audio>();

	private Audio mAudio;

	// 有管理器控制是否播放与暂停
	private Status status = Status.Init;

	public List<Audio> getmAudios() {
		return mAudios;
	}

	/**
	 * 设置当前需要播放的列表
	 * 
	 * @param mAudios
	 */
	public void setmAudios(List<Audio> mAudios) {
		this.mAudios = mAudios;
	}

	/**
	 * 获得当前播放的音频
	 * 
	 * @return
	 */
	public Audio getCurrentAudio() {
		return mAudio;
	}

	public void setmAudio(Audio mAudio) {
		this.mAudio = mAudio;
	}

	/**
	 * 设置当前播放的状态
	 * 
	 * @return
	 */
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof InfoMessage) {
			InfoMessage info = (InfoMessage) data;
			switch (info.getType()) {
			case InfoMessage.PLAYCHOICE:
				break;
			case InfoMessage.PLAY:
				break;
			case InfoMessage.PAUSE:
				break;
			default:
				break;
			}

		}
	}

}

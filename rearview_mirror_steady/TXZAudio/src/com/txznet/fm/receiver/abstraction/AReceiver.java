package com.txznet.fm.receiver.abstraction;

import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.fm.receiver.interfase.IReceiver;
import com.txznet.music.bean.response.Audio;

/**
 * 所有播放的父类
 * 
 * @author ASUS User
 *
 */
public abstract class AReceiver implements IReceiver {
	List<Audio> audios = new ArrayList<Audio>();

	Audio audio;// 当前播放的歌曲

	protected int playIndex = -1;

	protected AReceiver() {
		ObserverManage.getObserver().addObserver(this);
	}

	@Override
	public void play() {
		Toast.makeText(GlobalContext.get(), "play", 0).show();
	}

	@Override
	public void pause() {

	}

}

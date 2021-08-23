package com.txznet.txz.component.music.kaola;

import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.txz.component.music.IMusic;
import com.txznet.txz.jni.JNIHelper;

public class MusicKaolaImpl implements IMusic {
	public static final String PACKAGE_NAME = "com.edog.car";
	public static boolean SHOW_SEARCH_RESULT = true;

	public MusicKaolaImpl() {
		AdapterKaola.getAdapter();
	}

	@Override
	public String getPackageName() {
		return AdapterKaola.getAdapter().getPackageName();
	}

	@Override
	public boolean isPlaying() {
		return AdapterKaola.getAdapter().isPlaying();
	}

	@Override
	public void start() {
		AdapterKaola.getAdapter().start();
	}

	@Override
	public void pause() {
		AdapterKaola.getAdapter().pause();
	}

	@Override
	public void exit() {
		AdapterKaola.getAdapter().exit();
	}

	@Override
	public void next() {
		AdapterKaola.getAdapter().next();
	}

	@Override
	public void prev() {
		AdapterKaola.getAdapter().prev();
	}

	@Override
	public void switchModeLoopAll() {
		AdapterKaola.getAdapter().switchModeLoopAll();
	}

	@Override
	public void switchModeLoopOne() {
		AdapterKaola.getAdapter().switchModeLoopOne();
	}

	@Override
	public void switchModeRandom() {
		AdapterKaola.getAdapter().switchModeRandom();
	}

	@Override
	public void switchSong() {
		AdapterKaola.getAdapter().switchSong();
	}

	@Override
	public void playRandom() {
		AdapterKaola.getAdapter().playRandom();
	}

	@Override
	public void playMusic(MusicModel musicModel) {
		JNIHelper.logd("musicModel:" + musicModel);
		AdapterKaola.getAdapter().playMusic(musicModel);
	}

	@Override
	public MusicModel getCurrentMusicModel() {
		return AdapterKaola.getAdapter().getCurrentMusicModel();
	}

	@Override
	public void favourMusic() {
		AdapterKaola.getAdapter().favourMusic();
	}

	@Override
	public void unfavourMusic() {
		AdapterKaola.getAdapter().unfavourMusic();
	}

	@Override
	public void playFavourMusic() {
		AdapterKaola.getAdapter().playFavourMusic();
	}

	@Override
	public void setStatusListener(MusicToolStatusListener listener) {
		AdapterKaola.getAdapter().setStatusListener(listener);
	}

	@Override
	public boolean isBuffering() {
		return false;
	}

	@Override
	public void cancelRequest() {
		AdapterKaola.getAdapter().cancelRequest();
	}
}

package com.txznet.txz.component.music.adapter;

import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.txz.component.music.IMusic;
import com.txznet.txz.util.SendBroadcast;

public class AdapterSendBroadcastMusic implements IMusic {

	IMusic mImusic;
	
	public AdapterSendBroadcastMusic(IMusic music) {
		super();
		mImusic=music;
	}

	@Override
	public String getPackageName() {
		return mImusic.getPackageName();
	}

	@Override
	public boolean isPlaying() {
		return mImusic.isPlaying();
	}

	@Override
	public boolean isBuffering() {
		return mImusic.isBuffering();
	}

	@Override
	public void start() {
		SendBroadcast.sendplay();
		mImusic.start();
	}

	@Override
	public void pause() {
		SendBroadcast.sendPause();
		mImusic.pause();
	}

	@Override
	public void exit() {
		SendBroadcast.sendExit();
		mImusic.exit();
	}

	@Override
	public void exitImmediately() {
		SendBroadcast.sendExitImmediately();
		mImusic.exitImmediately();
	}

	@Override
	public void next() {
		SendBroadcast.sendNext();
		mImusic.next();
	}

	@Override
	public void prev() {
		SendBroadcast.sendPre();
		mImusic.prev();
	}

	@Override
	public void switchModeLoopAll() {
		SendBroadcast.sendSwitchModeLoopAll();
		mImusic.switchModeLoopAll();
	}

	@Override
	public void switchModeLoopOne() {
		SendBroadcast.sendSwitchModeLoopOne();
		mImusic.switchModeLoopOne();
	}

	@Override
	public void switchModeRandom() {
		SendBroadcast.sendSwitchModeRandom();
		mImusic.switchModeRandom();
	}

	@Override
	public void switchSong() {
		SendBroadcast.sendSwitchSong();
		mImusic.switchSong();
	}

	@Override
	public void playRandom() {
		SendBroadcast.sendPlayRandom();
		mImusic.playRandom();
	}

	@Override
	public void playMusic(MusicModel musicModel) {
		SendBroadcast.sendPlayMusic();
		mImusic.playMusic(musicModel);
	}

	@Override
	public MusicModel getCurrentMusicModel() {
		return mImusic.getCurrentMusicModel();
	}

	@Override
	public void favourMusic() {
		mImusic.favourMusic();
	}

	@Override
	public void unfavourMusic() {
		mImusic.unfavourMusic();
	}

	@Override
	public void playFavourMusic() {
		mImusic.playFavourMusic();
	}

	@Override
	public void setStatusListener(MusicToolStatusListener listener) {
		mImusic.setStatusListener(listener);
	}

	@Override
	public void cancelRequest() {
		mImusic.cancelRequest();
	}

}

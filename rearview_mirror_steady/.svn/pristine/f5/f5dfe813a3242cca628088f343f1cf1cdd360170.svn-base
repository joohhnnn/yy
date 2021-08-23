package com.txznet.txz.component.music;

import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.txz.util.SendBroadcast;

public abstract class BroadcastMusicTool implements IMusic {

	@Override
	public void start() {
		SendBroadcast.sendplay();
	}

	@Override
	public void pause() {
		SendBroadcast.sendPause();
	}

	@Override
	public void next() {
		SendBroadcast.sendNext();
	}

	@Override
	public void prev() {
		SendBroadcast.sendPre();
	}


	@Override
	public void exit() {
		SendBroadcast.sendExit();
	}

	@Override
	public void exitImmediately() {
		SendBroadcast.sendExitImmediately();
	}

	@Override
	public void switchModeLoopAll() {
		SendBroadcast.sendSwitchModeLoopAll();
	}

	@Override
	public void switchModeLoopOne() {
		SendBroadcast.sendSwitchModeLoopOne();
	}

	@Override
	public void switchModeRandom() {
		SendBroadcast.sendSwitchModeRandom();
	}

	@Override
	public void switchSong() {
		SendBroadcast.sendSwitchSong();
	}

	@Override
	public void playRandom() {
		SendBroadcast.sendPlayRandom();
	}

	@Override
	public void playMusic(MusicModel musicModel) {
		SendBroadcast.sendPlayMusic();
	}

}

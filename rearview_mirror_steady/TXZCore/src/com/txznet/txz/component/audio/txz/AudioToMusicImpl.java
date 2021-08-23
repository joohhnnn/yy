package com.txznet.txz.component.audio.txz;

import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.txz.component.audio.IAudio;
import com.txznet.txz.component.music.IMusic;
import com.txznet.txz.component.music.txz.AudioTxzImpl;

public class AudioToMusicImpl implements IAudio {
	private IMusic mMusicTool;

	public AudioToMusicImpl(IMusic musicTool) {
		mMusicTool = musicTool;
	}

	@Override
	public String getPackageName() {
		return mMusicTool.getPackageName();
	}

	@Override
	public void start() {
		mMusicTool.start();
	}

	@Override
	public void pause() {
		mMusicTool.pause();
	}

	@Override
	public void exit() {
		mMusicTool.exit();
	}

	@Override
	public void next() {
		mMusicTool.next();
	}

	@Override
	public void prev() {
		mMusicTool.prev();
	}

	@Override
	public void playFm(String jsonData) {
		MusicModel musicModel = new MusicModel();
		JSONBuilder builder = new JSONBuilder(jsonData);
		musicModel.setArtist(builder.getVal("artists", String[].class));
		musicModel.setKeywords(new String[] { builder.getVal("tag",
				String.class) });
		musicModel.setAlbum(builder.getVal("album", String.class));
		musicModel.setTitle(builder.getVal("title", String.class));
		musicModel.setText(builder.getVal("text", String.class));
		musicModel.setField(AudioTxzImpl.MUSICFLAG);
		mMusicTool.playMusic(musicModel);
	}

	@Override
	public String getCurrentFmName() {
		try {
			return mMusicTool.getCurrentMusicModel().getTitle();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void cancelRequest() {
		mMusicTool.cancelRequest();
	}
}

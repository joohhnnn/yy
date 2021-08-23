package com.txznet.txz.component.music.txz;

import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.txz.component.audio.IAudio;
import com.txznet.txz.component.music.BroadcastMusicTool;
import com.txznet.txz.component.music.IMusic;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.audio.AudioManager;
import com.txznet.txz.ui.win.record.RecorderWin;

public class MusicToAudioImpl extends BroadcastMusicTool{
	private IAudio mAudioTool;

	public MusicToAudioImpl(IAudio audioTool) {
		mAudioTool = audioTool;
	}
	@Override
	public String getPackageName() {
		return mAudioTool.getPackageName();
	}

	@Override
	public boolean isPlaying() {
		return AudioManager.getInstance().isPlaying();
	}

	@Override
	public boolean isBuffering() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void start() {
		super.start();
		mAudioTool.start();
	}

	@Override
	public void pause() {
		super.pause();
		mAudioTool.pause();
	}

	@Override
	public void exit() {
		super.exit();
		mAudioTool.exit();
	}

	@Override
	public void exitImmediately() {
		super.exitImmediately();
		mAudioTool.exit();
	}
	
	@Override
	public void next() {
		super.next();
		mAudioTool.next();
	}

	@Override
	public void prev() {
		super.prev();
		mAudioTool.prev();
	}

	@Override
	public void switchModeLoopAll() {
		// TODO Auto-generated method stub
		super.switchModeLoopAll();
		unsupportAction();
	}

	@Override
	public void switchModeLoopOne() {
		// TODO Auto-generated method stub
		super.switchModeLoopOne();
		unsupportAction();
	}

	@Override
	public void switchModeRandom() {
		// TODO Auto-generated method stub
		super.switchModeRandom();
		unsupportAction();
	}

	@Override
	public void switchSong() {
		// TODO Auto-generated method stub
		super.switchSong();
		unsupportAction();
	}

	@Override
	public void playRandom() {
		super.playRandom();
		mAudioTool.start();
	}

	@Override
	public void playMusic(MusicModel musicModel) {
		super.playMusic(musicModel);
		JSONBuilder builder = new JSONBuilder();
		if (musicModel != null) {
			builder.put("artists", musicModel.getArtist());
			builder.put("tag", musicModel.getKeywords());
			builder.put("album", musicModel.getAlbum());
			builder.put("title", musicModel.getTitle());
			builder.put("text", musicModel.getText());
		}
		mAudioTool.playFm(builder.toString());
	}

	@Override
	public MusicModel getCurrentMusicModel() {
		// TODO Auto-generated method stub
		MusicModel model = new MusicModel();
		model.setTitle(mAudioTool.getCurrentFmName());
		return model;
	}

	@Override
	public void favourMusic() {
		// TODO Auto-generated method stub
		unsupportAction();
	}

	@Override
	public void unfavourMusic() {
		// TODO Auto-generated method stub
		unsupportAction();
	}

	@Override
	public void playFavourMusic() {
		// TODO Auto-generated method stub
		unsupportAction();
	}

	@Override
	public void setStatusListener(MusicToolStatusListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancelRequest() {
		// TODO Auto-generated method stub
		unsupportAction();
	}
	private void unsupportAction() {
		String spk = NativeData.getResString("RS_VOICE_OPERATION_UNSUPPORT");
		RecorderWin.speakTextWithClose(spk, null);
	}
}

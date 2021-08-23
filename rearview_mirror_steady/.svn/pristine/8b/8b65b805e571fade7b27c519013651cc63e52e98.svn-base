package com.txznet.txz.component.music.txz;

import com.txz.ui.event.UiEvent;
import com.txz.ui.music.UiMusic;
import com.txz.ui.music.UiMusic.MediaItem;
import com.txz.ui.music.UiMusic.MediaModel;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.txz.component.music.BroadcastMusicTool;
import com.txznet.txz.component.music.IMusic;
import com.txznet.txz.component.music.ITxzMedia;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.music.MusicManager;

public class MusicTxzImpl extends BroadcastMusicTool {
	public static boolean mTxzMusicToolisPlaying = false;
	public static boolean mTxzMusicToolisBuffering = false;
	public static MediaItem mTxzMusicToolCurrentMusicModel = null;

	@Override
	public String getPackageName() {
		return ServiceManager.MUSIC;
	}

	@Override
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		return mTxzMusicToolisPlaying;
	}

	@Override
	public void start() {
		super.start();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.play", null, null);
	}

	@Override
	public void pause() {
		super.pause();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.pause", null, null);
	}

	@Override
	public void exit() {
		super.exit();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.exit", null, null);
	}
	
	@Override
	public void exitImmediately() {
		super.exitImmediately();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.exit", null, null);
	}

	@Override
	public void next() {
		super.next();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.next", null, null);
	}

	@Override
	public void prev() {
		super.prev();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.prev", null, null);
	}

	@Override
	public void switchModeLoopAll() {
		super.switchModeLoopAll();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.switchModeLoopAll", null, null);
	}

	@Override
	public void switchModeLoopOne() {
		super.switchModeLoopOne();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.switchModeLoopOne", null, null);
	}

	@Override
	public void switchModeRandom() {
		super.switchModeRandom();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.switchModeRandom", null, null);
	}

	@Override
	public void switchSong() {
		super.switchSong();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.switchSong", null, null);
	}

	@Override
	public void playRandom() {
		super.playRandom();
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_REFRESH_MUSIC_LIST);
	}

	@Override
	public void playMusic(MusicModel musicModel) {
		super.playMusic(musicModel);
		MediaModel model = new MediaModel();
		model.strTitle = musicModel.getTitle();
		model.strAlbum = musicModel.getAlbum();
		model.rptStrArtist = musicModel.getArtist();
		model.rptStrKeywords = musicModel.getKeywords();
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_SEARCH_AND_PLAY, model);
	}

	@Override
	public MusicModel getCurrentMusicModel() {
		if (mTxzMusicToolCurrentMusicModel == null
				|| mTxzMusicToolCurrentMusicModel.msgMedia == null)
			return null;
		MusicModel model = new MusicModel();
		model.setTitle(mTxzMusicToolCurrentMusicModel.msgMedia.strTitle);
		model.setAlbum(mTxzMusicToolCurrentMusicModel.msgMedia.strAlbum);
		model.setArtist(mTxzMusicToolCurrentMusicModel.msgMedia.rptStrArtist);
		model.setKeywords(mTxzMusicToolCurrentMusicModel.msgMedia.rptStrKeywords);
		return model;
	}

	@Override
	public void favourMusic() {
		//MusicManager.getInstance().favouriteMusicFromVoice(true, true);
	}

	@Override
	public void unfavourMusic() {
		//MusicManager.getInstance().favouriteMusicFromVoice(false, true);
	}

	@Override
	public void playFavourMusic() {
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_PLAY_FAVOURITE_LIST);
	}

	@Override
	public void setStatusListener(MusicToolStatusListener listener) {
		JNIHelper
				.loge("play music source error, not support play by this interface");
	}

	@Override
	public boolean isBuffering() {
		return mTxzMusicToolisBuffering;
	}

	@Override
	public void cancelRequest() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.sound.cancelfind", null, null);
	}

//	@Override
//	public void openApp() {
//		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
//				"music.open", null, null);
//	}
//
//	@Override
//	public void openAndPlay() {
//		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
//				"music.open.play", null, null);
//	}
}

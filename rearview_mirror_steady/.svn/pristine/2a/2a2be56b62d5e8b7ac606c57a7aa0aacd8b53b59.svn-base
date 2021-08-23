package com.txznet.music.utils;

import android.content.Intent;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.sdk.TXZMusicManager.MusicModel;

/**
 * 向服务器上报当前的数据
 * 
 * @author ASUS User
 *
 */
public class SyncCoreData {
	// 同步所有状态
	public static void syncCurStatusFullStyle() {
		// 请求端口
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.inner.syncMusicList", null, null);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.isNewVersion", new String("true").getBytes(), null);
		// TXZMusicManager.getInstance()
		// .setMusicTool(MusicToolType.MUSIC_TOOL_TXZ);
		syncCurPlayerStatus();
		syncCurMusicModel();
	}

	// 同步当前音乐模型
	public static void syncCurMusicModel() {
		Audio audio = MediaPlayerActivityEngine.getInstance().getCurrentAudio();
		LogUtil.logd("syncCoreData current Audio is :" + audio);
		MusicModel data = AudioToMusicModel(audio);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.inner.audioModel",
				(data != null ? JsonHelper.toJson(data) : "").getBytes(), null);
		// 发送广播
		Intent intent = new Intent("com.txznet.music.action.MUSIC_MODEL_CHANGE");
		intent.putExtra("audio", audio);
		AppLogic.getApp().sendBroadcast(intent);
	}

	public static void syncNextMusicModel(Audio nextAudio) {
		if (null == nextAudio)
			return;
		LogUtil.logd("syncCoreData next audio is :" + nextAudio.getName());

		MusicModel data = AudioToMusicModel(nextAudio);

		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.inner.audioModel.next",
				(data != null ? JsonHelper.toJson(data) : "").getBytes(), null);

	}

	// 同步当前播放状态
	public static void syncCurPlayerStatus() {
		boolean isPlaying = MediaPlayerActivityEngine.getInstance().isPlaying();
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.inner.isPlaying", ("" + isPlaying).getBytes(), null);
		LogUtil.logd("syncCoreData isPlaying is :" + isPlaying);
		MediaPlayerActivityEngine.getInstance().sendStatusByCurrent();
	}

	// 同步当前播放状态
	public static void syncCurPlayerBufferingStatus() {
		ServiceManager.getInstance().sendInvoke(
				ServiceManager.TXZ,
				"txz.music.inner.isBuffering",
				("" + MediaPlayerActivityEngine.getInstance().isBuffer)
						.getBytes(), null);
	}

	private static MusicModel AudioToMusicModel(Audio audio) {
		if (audio == null) {
			return null;
		}
		MusicModel data = new MusicModel();
		try {
			data.setTitle(audio.getName());
			data.setAlbum(audio.getAlbumName());
			data.setArtist(audio.getArrArtistName().toArray(
					new String[audio.getArrArtistName().size()]));
		} catch (Exception e) {
		}
		return data;
	}

}

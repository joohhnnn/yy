package com.txznet.music.utils;

import java.util.ArrayList;
import java.util.List;

import com.txznet.fm.dao.LocalAudioDBHelper;
import com.txznet.music.bean.response.Audio;
import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.TXZMusicManager.MusicModel;

/**
 * @author telenewbie
 * @version 创建时间：2016年4月21日 下午5:16:37
 * 
 */
public class UpdateToCoreUtil {

	public static void updateMusicModel() {
		// 同步
		List<Audio> resultAudios = LocalAudioDBHelper.getInstance().findAll(
				Audio.class);
		updateMusicModel(resultAudios);
	}

	/**
	 * 上报本地歌曲到Core
	 */
	public static void updateMusicModel(List<Audio> audios) {
		List<MusicModel> musics = new ArrayList<TXZMusicManager.MusicModel>();
		for (Audio audio : audios) {
			MusicModel model = new MusicModel();
			model.setTitle(audio.getName());
			model.setArtist(CollectionUtils.toStrings(audio.getArrArtistName()));
			model.setAlbum(audio.getAlbumName());
			model.setPath(audio.getStrDownloadUrl());
			musics.add(model);
		}
		TXZMusicManager.getInstance().syncExMuicList(musics);
	}
}

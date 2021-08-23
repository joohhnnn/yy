package com.txznet.music.utils;

import com.txznet.music.Constant;
import com.txznet.music.bean.req.RequestAlbum;
import com.txznet.music.bean.response.Audio;

public class RequestUtils {
	public static void reqData(Audio currentSong) {
		RequestAlbum reqData = new RequestAlbum();
		reqData.setAlbumId(currentSong.getAlbumId());
		reqData.setAudioId(currentSong.getId());
		reqData.setCategoryId(currentSong.getStrCategoryId());
		reqData.setSourceId(currentSong.getSid());
		NetHelp.sendRequest(Constant.GET_REPORT, reqData);
	}
}

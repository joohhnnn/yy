package com.txznet.txz.module.music.util;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.ArrayUtils;

import com.txz.ui.music.UiMusic.MediaModel;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.comm.util.StringUtils;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.music.MusicManager;

import android.text.TextUtils;

public class StringInfoUtils {
	public static String getMediaSpeakInfo(MediaModel mediaModel) {
		StringBuilder singer = new StringBuilder();
		String name = "";

		if (null != mediaModel) {
			if (mediaModel.rptStrArtist != null) {
				for (int i = 0; i < mediaModel.rptStrArtist.length; ++i) {
					if (mediaModel.rptStrArtist[i].length() <= 0)
						continue;
					if (singer.length() > 0) {
						if (i == mediaModel.rptStrArtist.length - 1) {
							singer.append("和");
						} else {
							singer.append("、");
						}
					}
					singer.append(mediaModel.rptStrArtist[i]);
				}
			}
			if (mediaModel.strTitle != null) {
				name = mediaModel.strTitle;
			}
			if (name.length() <= 0) {
				if (mediaModel.strFileName != null) {
					singer = new StringBuilder();
					name = mediaModel.strFileName; // 使用文件名
				}
			}
		}

		if (singer.length() > 0)
			singer.append("的");

		if (name.length() <= 0) {
			if (singer.length() <= 0)
				name = "未知音乐";
			else
				name = "歌";
		}
		return singer.toString() + name;
	}

	public static String getMediaSpeakInfo(MusicModel mediaModel) {
		String speakInfo;// TODO:将要播放的文本;默认为没有播放内容
		if (mediaModel == null) {
			speakInfo = NativeData.getResString("RS_MUSIC_NO_PLAY");
		} else {
			StringBuilder singer = new StringBuilder();
			//1.2和3
			if (ArrayUtils.isNotEmpty(mediaModel.getArtist())) {
				for (int i = 0; i < mediaModel.getArtist().length; ++i) {
					if (StringUtils.isNotEmpty(mediaModel.getArtist()[i])) {
						singer.append(mediaModel.getArtist()[i]);
						if (i < mediaModel.getArtist().length - 2) {
							singer.append("、");
						} else if (i == mediaModel.getArtist().length - 2) {
							singer.append("和");
						}
					}
				}
			}
			if (singer.length() > 0)
				singer.append("的");
			if (StringUtils.isNotEmpty(mediaModel.getTitle())) {
				singer.append(mediaModel.getTitle());
			} else if (StringUtils.isNotEmpty(mediaModel.getText())) {
				singer.append(mediaModel.getText()); // 使用文件名
			} else {
				if (singer.length() <= 0)
					singer.append("未知音乐");
				else
					singer.append("歌");
			}
			speakInfo = NativeData.getResPlaceholderString("RS_MUSIC_IS_PLAY", "%MUSIC%", singer.toString());
		}

		return speakInfo;
	}
	
	
	public static String genMediaModelTitle(String title, String album, String[] artists, String keywrod, String type) {
		StringBuilder artist = new StringBuilder();
		if (artists != null) {
			for (int i = 0; i < artists.length; ++i) {
				if (artist.length() == 0) {
					artist.append(artists[i]);
				} else if (i == artists.length - 1) {
					artist.append("和");
					artist.append(artists[i]);
				} else {
					artist.append("、");
					artist.append(artists[i]);
				}
			}
		}
		if (TextUtils.isEmpty(title) && TextUtils.isEmpty(artist)) {
			if (TextUtils.isEmpty(album)) {
				if (!TextUtils.isEmpty(keywrod)) {
					return keywrod + "类型" + type;
				}
			} else {
				return album;
			}
		}
		if (TextUtils.isEmpty(artist) == false) {
			artist.append("的");
		}
		if (TextUtils.isEmpty(title)) {
			if (!TextUtils.isEmpty(artist)) {
				artist.append(type);
			}
		} else {
			artist.append(title);
		}
		JNIHelper.logd(MusicManager.TAG + "search:modle" + artist.toString());
		return artist.toString();
	}
	
	public static String genMediaModelTitle(String title, String album, String[] artists, String[] keywrods, String type) {
		String keyword="";
		if (keywrods != null && keywrods.length > 0) {
			keyword= keywrods[0];
		}
		return genMediaModelTitle(title, album, artists, keyword, type);
	}
}

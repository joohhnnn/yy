package com.txznet.music.engine;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
//import android.support.v4.app.FragmentActivity;

import com.txznet.fm.dao.HistoryAudioDBHelper;
import com.txznet.music.bean.HistoryAudio;
import com.txznet.music.engine.base.BaseEngine;
import com.txznet.music.utils.StringUtils;

public class SongListFragmentEngine extends BaseEngine {

	private static final String TAG = SongListFragmentEngine.class.getSimpleName();
	private static Uri contentUri = Media.EXTERNAL_CONTENT_URI;

	private static String[] projection = { Media._ID, MediaStore.Video.Media.DISPLAY_NAME, Media.TITLE, Media.DATA, Media.ALBUM, Media.ARTIST, Media.DURATION, Media.SIZE, Media.IS_MUSIC };
	private static String sortOrder = Media.TITLE;
	private static Cursor cursor;

	public SongListFragmentEngine(Activity activity) {
		super(activity);
	}

	// public static List<Audio> getLocalMusic(String params) {
	// List<Audio> data = new ArrayList<Audio>();
	// StringBuffer sb = new StringBuffer();
	// if (StringUtils.isNotEmpty(params)) {
	// sb.append(params);
	// sb.append(" and ");
	// }
	// sb.append(MediaStore.Video.Media.DURATION + " > 10000");// 大于10s
	//
	// cursor = GlobalContext.get().getContentResolver().query(contentUri,
	// projection, sb.toString(), null, sortOrder + " desc ");
	// if (cursor == null) {
	// Log.v(TAG, "Music Loader cursor == null.");
	// } else {
	// int titleCol = cursor.getColumnIndex(Media.TITLE);
	// int albumCol = cursor.getColumnIndex(Media.ALBUM);
	// int idCol = cursor.getColumnIndex(Media._ID);
	// int durationCol = cursor.getColumnIndex(Media.DURATION);
	// int sizeCol = cursor.getColumnIndex(Media.SIZE);
	// int artistCol = cursor.getColumnIndex(Media.ARTIST);
	// int urlCol = cursor.getColumnIndex(Media.DATA);
	// int isMusicCol = cursor.getColumnIndex(Media.IS_MUSIC);
	// int displayNameCol =
	// cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
	// while (cursor.moveToNext()) {
	// String title = cursor.getString(titleCol);
	// String album = cursor.getString(albumCol);
	// int id = cursor.getInt(idCol);
	// int duration = cursor.getInt(durationCol);
	// long size = cursor.getLong(sizeCol);
	// String artist = cursor.getString(artistCol);
	// String url = cursor.getString(urlCol);
	// int isMusic = cursor.getInt(isMusicCol);
	// String displayName = cursor.getString(displayNameCol);
	//
	// Audio musicInfo = new Audio();
	//
	// musicInfo.setId(id);
	// musicInfo.setSid(0);// 本地音乐
	// musicInfo.setFileSize(size);
	// musicInfo.setName(title);
	// if (StringUtils.isEmpty(title) || title.contains("???")) {
	// int lastIndexOf = displayName.lastIndexOf(".");
	// if (lastIndexOf <= 0) {
	// lastIndexOf = displayName.length();
	// }
	// musicInfo.setName(displayName.substring(0, lastIndexOf));
	// }
	// List<String> valueStrings = new ArrayList<String>();
	//
	// if ("<unknown>".equals(artist) || artist.contains("??")) {
	// valueStrings.add("未知艺术家");
	// } else {
	// valueStrings.add(artist);
	// }
	// musicInfo.setArrArtistName(valueStrings);
	// musicInfo.setDuration(duration);
	// musicInfo.setStrDownloadUrl(url);
	// musicInfo.setStrCategoryId("0");
	// // dao.add(musicInfo);// 保存到数据库
	// if (isMusic == 1) {// 是歌曲
	// data.add(musicInfo);
	// }
	// }
	// // DBSQLHelper.insertOrUpdate(Audio.class, data);
	//
	// cursor.close();// 关闭
	// }
	// // LogUtil.logd("localData::cursor::" + data.toString());
	//
	// return data;
	// }

	// public static void reqData() {
	// ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
	// "txz.music.inner.syncMusicList", null, null);
	// }



	public void removeAllMusicFromHistory() {
		HistoryAudioDBHelper.getInstance().removeAll();
	}

}

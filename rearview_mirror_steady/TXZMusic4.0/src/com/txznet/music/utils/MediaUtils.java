package com.txznet.music.utils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.albumModule.bean.Audio;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

public class MediaUtils {
	// 获取专辑封面的Uri
	private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");

	/**
	 * 获取媒体的游标
	 * 
	 * @return Cursor
	 */
	public static Cursor getMediaCursor(Context context) {
		Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.TITLE + " desc ");
		return cursor;
	}

	/**
	 * 从数据库中查询歌曲信息保存在List中
	 */
	public static List<Audio> getMp3Infos(Context context) {
		Cursor cursor = getMediaCursor(context);
		List<Audio> mp3Infos = new ArrayList<Audio>();
		for (int i = 0; i < cursor.getCount(); i++) {
			Audio mp3Info = getMp3InfoByCursor(cursor);
			mp3Infos.add(mp3Info);
		}
		cursor.close();
		return mp3Infos;
	}

	/**
	 * 获取歌曲长度
	 * 
	 * @param trackLengthAsString
	 * @return
	 */
	private static long getTrackLength(String trackLengthAsString) {

		if (trackLengthAsString.contains(":")) {
			String temp[] = trackLengthAsString.split(":");
			if (temp.length == 2) {
				int m = Integer.parseInt(temp[0]);// 分
				int s = Integer.parseInt(temp[1]);// 秒
				int currTime = (m * 60 + s) * 1000;
				return currTime;
			}
		}
		return 0;
	}

	/**
	 * 通过游标获取Audio
	 */
	public static Audio getMp3InfoByCursor(Cursor cursor) {
		cursor.moveToNext();

		int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
		if (isMusic == 0)
			return null;

		String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
		if (url.startsWith(".")) {
			return null;
		}
		// String extension = ".mp3";
		// if (!url.substring(url.length() -
		// extension.length()).equals(extension)) {
		// return null;
		// }

		// Audio mp3Info = getMp3InfoByFile(url);
		Audio mp3Info = new Audio();
		String artist = "";
		String title = "";

		long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

		String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));

		if (displayName.contains(".mp3")) {
			String[] displayNameArr = displayName.split(".mp3");
			displayName = displayNameArr[0].trim();
		}

		title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
		artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
		if (StringUtils.isEmpty(title) || title.contains("???")) {
			int lastIndexOf = displayName.lastIndexOf(".");
			if (lastIndexOf <= 0) {
				lastIndexOf = displayName.length();
			}
			title = displayName.substring(0, lastIndexOf);
		}

		List<String> valueStrings = new ArrayList<String>();
		
		if ("<unknown>".equals(artist) || artist.contains("??")) {
//			valueStrings.add("未知艺术家");
		} else {
			valueStrings.add(artist);
		}


		long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
		long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
		String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)); // 专辑

		long albumid = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

		if (size < 1024 * 1024) {
			return null;
		}
		mp3Info.setId(id);
		mp3Info.setName(title);
		mp3Info.setArrArtistName(valueStrings);
		mp3Info.setDuration(duration);
		// mp3Info.setDisplayName(displayName);
		mp3Info.setAlbumId(album);
		// mp3Info.seta(album);
		mp3Info.setDuration(duration);
		mp3Info.setFileSize(size);
		mp3Info.setStrDownloadUrl(url);
		mp3Info.setAlbumId(albumid + "");
		LogUtil.logd("title=" + title + ",isMusic=" + isMusic);
		
		return mp3Info;
	}

	/**
	 * 时间格式转换
	 * 
	 * @param time
	 * @return
	 */
	public static String formatTime(int time) {

		time /= 1000;
		int minute = time / 60;
		// int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}

	/**
	 * 计算文件的大小，返回相关的m字符串
	 * 
	 * @param fileS
	 * @return
	 */
	public static String getFileSize(long fileS) {// 转换文件大小
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	/**
	 * 从文件当中获取专辑封面位图
	 * 
	 * @param context
	 * @param songid
	 * @param albumid
	 * @return
	 */
	private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
		Bitmap bm = null;
		if (albumid < 0 && songid < 0) {
			throw new IllegalArgumentException("Must specify an album or a song id");
		}
		try {
			Options options = new Options();
			FileDescriptor fd = null;
			if (albumid < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
				}
			} else {
				Uri uri = ContentUris.withAppendedId(albumArtUri, albumid);
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
				}
			}
			options.inSampleSize = 1;
			// 只进行大小判断
			options.inJustDecodeBounds = true;
			// 调用此方法得到options得到图片大小
			BitmapFactory.decodeFileDescriptor(fd, null, options);
			// 我们的目标是在800pixel的画面上显示
			// 所以需要调用computeSampleSize得到图片缩放的比例
			options.inSampleSize = 2;
			// 我们得到了缩放的比例，现在开始正式读入Bitmap数据
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			// 根据options参数，减少所需要的内存
			bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return bm;
	}

	/**
	 * 对图片进行合适的缩放
	 */
	public static int computeSampleSize(Options options, int target) {
		int w = options.outWidth;
		int h = options.outHeight;
		int candidateW = w / target;
		int candidateH = h / target;
		int candidate = Math.max(candidateW, candidateH);
		if (candidate == 0) {
			return 1;
		}
		if (candidate > 1) {
			if ((w > target) && (w / candidate) < target) {
				candidate -= 1;
			}
		}
		if (candidate > 1) {
			if ((h > target) && (h / candidate) < target) {
				candidate -= 1;
			}
		}
		return candidate;
	}
}

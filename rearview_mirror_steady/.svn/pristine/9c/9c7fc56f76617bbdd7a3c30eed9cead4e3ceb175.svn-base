package com.txznet.music.receiver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.database.AbstractCursor;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.utils.MediaUtils;
import com.txznet.music.utils.StringUtils;

public class AndroidMediaLibrary {

	private static String[] colNum = new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.IS_MUSIC, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DISPLAY_NAME };

	private static String orderParam = MediaStore.Audio.Media.TITLE + " desc ";
	// static {
	// BroadcastReceiver mVolReceiver = new BroadcastReceiver() {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(intent.getAction())) {
	// // 音乐扫描中
	// LogUtil.logd("ACTION_MEDIA_SCANNER_STARTED");
	// } else if
	// (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(intent.getAction())) {
	// // 音乐扫描完毕
	// LogUtil.logd("ACTION_MEDIA_SCANNER_FINISHED");
	// refreshSystemMedia(null);
	// }
	// }
	// };
	//
	// IntentFilter filter = new IntentFilter();
	// filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
	// filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
	// GlobalContext.get().registerReceiver(mVolReceiver, filter);
	// }

	public final static String EXTD1 = "content://media/external_extd1/audio/media";
	public final static String EXTD2 = "content://media/external_extd2/audio/media";
	public final static String USB1 = "content://media/external_usb1/audio/media";
	public final static String USB2 = "content://media/external_usb2/audio/media";
	public static String newSDPath = "";

	public final static String[] EXT_CONTENT_URIS = new String[] { EXTD1, EXTD2, USB1, USB2 };

	public static List<Audio> refreshSystemMedia() {
		ArrayList<Audio> arr = new ArrayList<Audio>();
		try {
			MergeCursor mergeCursor = (MergeCursor) getCursor();
			for (int i = 0; i < mergeCursor.getCount(); i++) {
				Audio audio = MediaUtils.getMp3InfoByCursor(mergeCursor);
				if (audio == null) {
					continue;
				}
				File file = new File(audio.getStrDownloadUrl());
				if (!file.exists()) {
					continue;
				}
				arr.add(audio);
			}
			mergeCursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return arr;
	}

	public static AbstractCursor getCursor() {

		StringBuffer sb = new StringBuffer();
//		if (StringUtils.isNotEmpty(param)) {
//			sb.append(param);
//			sb.append(" and ");
//		}
		sb.append(MediaStore.Video.Media.DURATION + " > 10000");// 大于10s

		ArrayList<Cursor> cursors = new ArrayList<Cursor>();
		{
			Cursor cursor = GlobalContext.get().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, colNum, sb.toString(), null, orderParam);
			if (cursor != null) {
				cursors.add(cursor);
			}
			for (String url : EXT_CONTENT_URIS) {
				try {
					cursor = GlobalContext.get().getContentResolver().query(Uri.parse(url), colNum, sb.toString(), null, orderParam);
					if (cursor != null) {
						cursors.add(cursor);
					}
				} catch (Exception e) {
				}
			}
			if (StringUtils.isNotEmpty(newSDPath)) {
				try {
					cursor = GlobalContext.get().getContentResolver().query(Uri.parse(newSDPath), colNum, sb.toString(), null, orderParam);
					if (cursor != null) {
						cursors.add(cursor);
					}
				} catch (Exception e) {
				}
			}
		}

		MergeCursor mergeCursor = new MergeCursor(cursors.toArray(new Cursor[cursors.size()]));
		return mergeCursor;
	}
}

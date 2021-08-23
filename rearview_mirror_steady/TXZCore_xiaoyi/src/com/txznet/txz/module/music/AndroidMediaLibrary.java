package com.txznet.txz.module.music;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.HandlerThread;
import android.provider.MediaStore;

import com.txz.ui.event.UiEvent;
import com.txz.ui.music.UiMusic;
import com.txz.ui.music.UiMusic.MediaItem;
import com.txz.ui.music.UiMusic.MediaList;
import com.txz.ui.music.UiMusic.MediaModel;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.TXZHandler;

public class AndroidMediaLibrary {
	static HandlerThread mMediaScanThread = null;
	static TXZHandler mMediaScanHandler = null;

	static {
		BroadcastReceiver mVolReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(intent
						.getAction())) {
					// 音乐扫描中
					JNIHelper.logd("ACTION_MEDIA_SCANNER_STARTED");
				} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(intent
						.getAction())) {
					// 音乐扫描完毕
					JNIHelper.logd("ACTION_MEDIA_SCANNER_FINISHED");

					if (mLastSyncMusics == null) {
						refreshSystemMedia();
					}
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		filter.addDataScheme("file");
		GlobalContext.get().registerReceiver(mVolReceiver, filter);
	}

	public final static String EXTD1 = "content://media/external_extd1/audio/media";
	public final static String EXTD2 = "content://media/external_extd2/audio/media";
	public final static String USB1 = "content://media/external_usb1/audio/media";
	public final static String USB2 = "content://media/external_usb2/audio/media";

	public final static String[] EXT_CONTENT_URIS = new String[] { EXTD1,
			EXTD2, USB1, USB2 };

	private static int mCurSeq = new Random().nextInt();

	private static int getNextRefreshSeq() {
		++mCurSeq;
		if (mCurSeq == 0)
			++mCurSeq;
		return mCurSeq;
	}

	private static int mLastSeq = 0;

	public static void refreshSystemMedia() {
		synchronized (AndroidMediaLibrary.class) {
			if (mMediaScanThread == null) {
				mMediaScanThread = new HandlerThread("ScanMedia");
				mMediaScanThread.start();
				mMediaScanHandler = new TXZHandler(mMediaScanThread.getLooper());
			}

			mLastSeq = getNextRefreshSeq();
			mMediaScanHandler.post(new RunnableRefreshSystemMedia(mLastSeq));
		}
	}

	private static class RunnableRefreshSystemMedia implements Runnable {
		int mSeq = 0;

		public RunnableRefreshSystemMedia(int seq) {
			mSeq = seq;
		}

		public void run() {
			try {
				if (mSeq != mLastSeq)
					return;
				ArrayList<Cursor> cursors = new ArrayList<Cursor>();
				{
					Cursor cursor = GlobalContext
							.get()
							.getContentResolver()
							.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
									new String[] { MediaStore.Audio.Media._ID,
											MediaStore.Audio.Media.TITLE,
											MediaStore.Audio.Media.ARTIST,
											MediaStore.Audio.Media.ALBUM,
											MediaStore.Audio.Media.DATA,
											MediaStore.Audio.Media.DURATION,
											MediaStore.Audio.Media.SIZE,
											MediaStore.Audio.Media.IS_MUSIC },
									null, null, null);
					if (cursor != null) {
						cursors.add(cursor);
					}
					for (String url : EXT_CONTENT_URIS) {
						if (mSeq != mLastSeq)
							return;
						try {
							cursor = GlobalContext
									.get()
									.getContentResolver()
									.query(Uri.parse(url),
											new String[] {
													MediaStore.Audio.Media._ID,
													MediaStore.Audio.Media.TITLE,
													MediaStore.Audio.Media.ARTIST,
													MediaStore.Audio.Media.ALBUM,
													MediaStore.Audio.Media.DATA,
													MediaStore.Audio.Media.DURATION,
													MediaStore.Audio.Media.SIZE,
													MediaStore.Audio.Media.IS_MUSIC },
											null, null, null);
							if (cursor != null) {
								cursors.add(cursor);
							}
						} catch (Exception e) {
						}
					}
				}
				if (cursors.size() == 0)
					return;

				MergeCursor mergeCursor = new MergeCursor(
						cursors.toArray(new Cursor[cursors.size()]));

				ArrayList<MediaItem> arr = new ArrayList<MediaItem>();
				if (mergeCursor.moveToFirst()) {
					do {
						if (mSeq != mLastSeq) {
							mergeCursor.close();
							for (Cursor lCursor:cursors) {
								lCursor.close();
							}
							return;
						}
						// 判断是否为音乐
						if (mergeCursor.getInt(7) == 0)
							continue;
						// 判断路径是否存在
						String path = mergeCursor.getString(4);
						File f = new File(path);
						if (!f.exists())
							continue;
						JNIHelper.logd("get android system song: " + path);
						MediaItem item = new MediaItem();
						item.msgMedia = new MediaModel();
						item.msgMedia.int32Id = mergeCursor.getInt(0);
						item.msgMedia.strTitle = mergeCursor.getString(1);
						item.msgMedia.rptStrArtist = new String[] { mergeCursor
								.getString(2) };
						item.msgMedia.strAlbum = mergeCursor.getString(3);
						item.msgMedia.strPath = path;
						item.msgMedia.strFileName = f.getName();
						int n = item.msgMedia.strFileName.lastIndexOf('.');
						if (n > 0) {
							item.msgMedia.strFileExt = item.msgMedia.strFileName
									.substring(n + 1);
							item.msgMedia.strFileName = item.msgMedia.strFileName
									.substring(0, n);
						}
						item.msgMedia.uint32Duration = mergeCursor.getInt(5);
						item.msgMedia.uint32Size = mergeCursor.getInt(6);
						arr.add(item);
					} while (mergeCursor.moveToNext());
				}
				mergeCursor.close();
				for (Cursor lCursor:cursors) {
					lCursor.close();
				}
				if (mLastSyncExMusics != null) {
					for (MusicModel m : mLastSyncExMusics) {
						JNIHelper.logd("add sdk ext song: " + m.getPath());
						arr.add(MusicModelToMusicItem(m));
					}
				}
				MediaList lst = new MediaList();
				lst.rptMediaItem = arr.toArray(new MediaItem[arr.size()]);
				if (mLastSyncMusics != null) {
					return;
				}
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_SYNC_SYSTEM_MEDIA_LIST, lst);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (mSeq == mLastSeq) {
				synchronized (AndroidMediaLibrary.class) {
					if (mMediaScanThread != null) {
						mMediaScanThread.quit();
						mMediaScanThread = null;
					}
				}
			}
		}
	};

	static Collection<MusicModel> mLastSyncMusics = null;
	static Collection<MusicModel> mLastSyncExMusics = null;

	private static MediaItem MusicModelToMusicItem(MusicModel m) {
		MediaItem item = new MediaItem();
		MediaModel model = item.msgMedia = new MediaModel();
		model.strPath = m.getPath();
		try {
			File f = new File(model.strPath);
			String name = f.getName();
			int i = name.lastIndexOf('.');
			if (i > 0) {
				model.strFileName = name.substring(0, i);
				model.strFileExt = name.substring(i + 1);
			} else {
				model.strFileName = name;
			}
		} catch (Exception e) {
		}
		model.rptStrArtist = m.getArtist();
		model.strAlbum = m.getAlbum();
		model.strTitle = m.getTitle();
		model.rptStrKeywords = m.getKeywords();
		model.uint32MediaType = UiMusic.MEDIA_TYPE_MUSIC;
		return item;
	}

	private static void addMusics(MediaList lst,
			Collection<MusicModel>... musics) {
		int n = 0;
		for (Collection<MusicModel> ms : musics) {
			if (ms == null)
				continue;
			for (MusicModel m : ms) {
				++n;
			}
		}
		lst.rptMediaItem = new MediaItem[n];
		int i = 0;
		for (Collection<MusicModel> ms : musics) {
			if (ms == null)
				continue;
			for (MusicModel m : ms) {
				lst.rptMediaItem[i] = MusicModelToMusicItem(m);
				++i;
			}
		}
	}

	public static void syncMusicList(Collection<MusicModel> musics) {
		mLastSyncMusics = musics;
		MediaList lst = new MediaList();
		addMusics(lst, mLastSyncMusics, mLastSyncExMusics);
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_SYNC_SYSTEM_MEDIA_LIST, lst);
	}

	public static void syncExMusicList(Collection<MusicModel> musics) {
		mLastSyncExMusics = musics;
		if (mLastSyncMusics != null) {
			MediaList lst = new MediaList();
			addMusics(lst, mLastSyncMusics, mLastSyncExMusics);
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_SYNC_SYSTEM_MEDIA_LIST, lst);
			return;
		}
		refreshSystemMedia();
	}
}

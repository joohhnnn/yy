package com.txznet.music.service;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.audiofx.AudioEffect.Descriptor;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore.Audio.GenresColumns;
import android.widget.Toast;

import com.google.protobuf.nano.MessageNano;
import com.txz.ui.data.UiData;
import com.txz.ui.music.UiMusic;
import com.txz.ui.music.UiMusic.MediaCategory;
import com.txz.ui.music.UiMusic.MediaCategoryList;
import com.txz.ui.music.UiMusic.MediaItem;
import com.txz.ui.music.UiMusic.MediaList;
import com.txz.ui.music.UiMusic.MediaModel;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.comm.util.ProtoBufferUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.broadcast.MediaReceiver;
import com.txznet.music.Utils;

public class MusicService {
	private static MusicService sInstance = new MusicService();

	private MediaReceiver mMediaReceiver;

	private MusicService() {
		mMediaPlayer = new MediaPlayer();
		mAm = (AudioManager) GlobalContext.get().getSystemService(
				Context.AUDIO_SERVICE);
		mMediaReceiver = new MediaReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_SHARED);// ??????SDCard?????????,?????????USB???????????????????????????
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);// ??????sd???????????????????????????/?????????
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// SDCard?????????,??????SDCard???????????????????????????
		filter.addAction(Intent.ACTION_MEDIA_CHECKING); // ??????????????????????????????
		filter.addAction(Intent.ACTION_MEDIA_EJECT); // ??????????????? SDCARD
		filter.addAction(Intent.ACTION_MEDIA_REMOVED); // ????????????
		filter.addDataScheme("file"); // ?????????????????????????????????????????????
		GlobalContext.get().registerReceiver(mMediaReceiver, filter);
	}

	public static MusicService getInstance() {
		return sInstance;
	}

	public int mLocalMediaServerPort = 0;

	// ????????????
	private MediaPlayer mMediaPlayer;

	private boolean mLoadedHistory = false;
	private MediaList mMedialist = loadLastMediaStatus();

	public enum PlayMode {
		PLAY_MODE_LOOP_ALL, // ????????????
		PLAY_MODE_LOOP_SINGLE, // ????????????
		PLAY_MODE_RANDOM, // ????????????
	};

	private PlayMode mPlayMode = PlayMode.PLAY_MODE_LOOP_ALL;

	int mCurRefer = 0;
	private AudioManager mAm;

	private void _pausePlayer(boolean force) {
		// mMedialist.int32CurPosition = getCurrentPosition();
		LogUtil.logd("pause");
		if (force || mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();

			if (mMusicStatusListner != null)
				mMusicStatusListner.onPlayStop();
		}
	}

	private boolean mStop = true;

	private void _stopPlayer() {
		LogUtil.logd("stop");
		mBufferProcessing = false;
		mStop = true;
		mMediaPlayer.stop();

		if (mMusicStatusListner != null)
			mMusicStatusListner.onPlayStop();
	}

	/**
	 * ??????MediaPlayer???start??????
	 */
	private void _startPlayer() {
		mBufferProcessing = false;
		if (mBufferProcessing) {
			LogUtil.logd("startPlayer wait BufferProccessing");
			return;
		}

		if (!mHasFocus) {
			LogUtil.logd("startPlayer wait focus");
			mPausedByFocusLoss = true;
			return;
		}

		mStop = false;
		mMediaPlayer.start();

		if (mMusicStatusListner != null)
			mMusicStatusListner.onPlayStart();

		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.inner.isPlaying", ("" + true).getBytes(), null);
	}

	public boolean isStop() {
		return mStop;
	}

	private boolean _isPlaying() {
		return mMediaPlayer.isPlaying();
	}

	/**
	 * ??????????????????
	 * 
	 * @return
	 */
	public MediaList getMediaList() {
		return mMedialist;
	}

	public static final int RESUME_MUSIC_DELAY = 500;

	public boolean isLogicPaused() {
		return mPausedByFocusLoss;
	}

	// ??????
	protected void stopPlayer() {
		cleanResumePlay();
		_stopPlayer();
	}

	// ?????????????????????MediaPlayer
	public void resetPlayer() {
		stopPlayer();
		mMediaPlayer.reset();
	}

	public void refreshMusicList() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.inner.refreshMusicList", null, null);
	}

	/**
	 * ????????????????????????
	 */
	public void syncMusicList() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.inner.syncMusicList", null, null);
	}

	/*
	 * ????????????????????????
	 */
	public void refreshCategoryList() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.inner.refreshCategoryList", null, null);
	}

	// ??????
	public void startPlayer(boolean manual) {
		if (mMedialist == null || mMedialist.rptMediaItem == null
				|| mMedialist.rptMediaItem.length <= 0) {
			refreshMusicList();
			LogUtil.logi("startPlayer?????????");
		} else {
			playMusic(manual, mMedialist.int32CurIndex,
					mMedialist.int32CurPosition);
			// ????????????????????????????????????
			if (mMedialist == null || mMedialist.rptMediaItem == null
					|| mMedialist.rptMediaItem.length <= 0) {
				refreshMusicList();
				LogUtil.logi("startPlayer?????????");
			}
		}
	}

	// ??????????????????
	public void removeItemByIndex(int index) {
		if (index >= 0 && index < mMedialist.rptMediaItem.length) {
			LogUtil.logw("remove non-exist music: "
					+ mMedialist.rptMediaItem[index].msgMedia.strPath);
			MediaItem[] mediaItems = new MediaItem[mMedialist.rptMediaItem.length - 1];
			for (int i = 0; i < mediaItems.length; ++i) {
				if (i < index)
					mediaItems[i] = mMedialist.rptMediaItem[i];
				else
					mediaItems[i] = mMedialist.rptMediaItem[i + 1];
			}

			mMedialist.rptMediaItem = mediaItems;
			if (getCurIndex() >= index) {
				mMedialist.int32CurIndex = getCurIndex() - 1;
			}

			if (mMusicStatusListner != null) {
				mMusicStatusListner.onMediaListChange(mMedialist);
				mMusicStatusListner.onCategoryListUpdated(mMediaCategoryList);
			}
		}
	}

	/**
	 * @param pos
	 * @param isDelete
	 */
	public void deleteMusic(int pos, boolean isDelete) {
		LogUtil.loge("currentIndex is:" + getCurIndex());
		if (getCurIndex() == pos) {
			stopPlayer();
		}

		if (pos == -1)
			return;

		if (pos < 0 || pos >= mMedialist.rptMediaItem.length)
			return;

		MediaItem item = mMedialist.rptMediaItem[pos];
		if (item == null)
			return;

		LogUtil.logd("delete " + item.msgMedia.strTitle);

		if (item.msgMedia.bFavourite != null
				&& item.msgMedia.bFavourite == true) {
			favouriteMusic(item.msgMedia, false);
		}
		if (!"??????".equals(mLastCategoryName))
			mLastCategoryName = "";

		String filePath = item.msgMedia.strPath;
		try {
			if (isDelete) {
				if (!filePath.toLowerCase().startsWith("http:")) {
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
							"txz.music.inner.deleteMusic", filePath.getBytes(),
							null);
				}
			}
			removeItemByIndex(pos);
		} catch (Exception e) {
			LogUtil.loge("MusicService deleteMusic error:" + e.toString());
		}
		saveLastMediaStatus();
	}

	Runnable mRunnbleCheckPlayNearEnd = new Runnable() {
		@Override
		public void run() {
			AppLogic.removeBackGroundCallback(mRunnbleCheckPlayNearEnd);
			if (((long) mMediaPlayer.getCurrentPosition()) * 100
					/ mMediaPlayer.getDuration() < 80) {
				AppLogic.runOnBackGround(mRunnbleCheckPlayNearEnd, 1000);
				return;
			}

			// ????????????QQ????????????????????????
			if (!QQMusicUtil.checkQQMusicInstalled()) {
				AppLogic.runOnBackGround(mRunnbleCheckPlayNearEnd, 5000);
				return;
			}

			// ???????????????????????????????????????
			TtsUtil.speakText(mMedialist.msgSearchFilter.rptStrArtist[0]
					+ "???????????????????????????????????????????????????");

			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.music.inner.searchMoreMedia",
					MessageNano.toByteArray(mMedialist.msgSearchFilter), null);

			mMedialist.msgSearchFilter = null; // ??????????????????????????????????????????
		}
	};

	int mNoNetworkTts = TtsUtil.INVALID_TTS_TASK_ID;

	// ????????????
	public void playMusic(boolean manual, int index, int pos) {
		try {
			TtsUtil.cancelSpeak(mNoNetworkTts);
			mNoNetworkTts = TtsUtil.INVALID_TTS_TASK_ID;

			if (manual
					&& mMedialist != null
					&& mMedialist.rptMediaItem != null
					&& index >= 0
					&& index < mMedialist.rptMediaItem.length
					&& mMedialist.rptMediaItem[index].msgMedia != null
					&& mMedialist.rptMediaItem[index].msgMedia.strPath != null
					&& mMedialist.rptMediaItem[index].msgMedia.strPath
							.startsWith("http")) {
				// ????????????????????????????????????
				switch (NetworkUtil.getSystemNetwork(GlobalContext.get())) {
				case UiData.NETWORK_STATUS_NONE:
				case UiData.NETWORK_STATUS_UNKNOW:
				case UiData.NETWORK_STATUS_FLY:
					AppLogic.showToast("???????????????????????????????????????");
					mNoNetworkTts = TtsUtil.speakText("???????????????????????????????????????");
					return;
				}
			}

			AppLogic.removeBackGroundCallback(mRunnbleCheckPlayNearEnd);
			// ??????????????????
			if (index == mMedialist.rptMediaItem.length - 1
					&& mMedialist.rptMediaItem.length < 5
					&& mPlayMode == PlayMode.PLAY_MODE_LOOP_ALL) {
				if (mMedialist.msgSearchFilter != null
						&& mMedialist.msgSearchFilter.rptStrArtist != null
						&& mMedialist.msgSearchFilter.rptStrArtist.length > 0
						&& ProtoBufferUtil
								.isStringEmpty(mMedialist.msgSearchFilter.strTitle)
						&& ProtoBufferUtil
								.isStringEmpty(mMedialist.msgSearchFilter.strAlbum)
						&& ProtoBufferUtil
								.isStringEmpty(mMedialist.msgSearchFilter.strType)) {
					AppLogic.runOnBackGround(mRunnbleCheckPlayNearEnd, 1000);
				}
			}
			if (index >= 0 && index < mMedialist.rptMediaItem.length) {
				mMedialist.int32CurIndex = index;
				if (mMedialist.rptMediaItem[mMedialist.int32CurIndex].msgMedia != null
						|| mMedialist.rptMediaItem[mMedialist.int32CurIndex].msgMedia.strPath != null) {
					String strPath = mMedialist.rptMediaItem[mMedialist.int32CurIndex].msgMedia.strPath;
					// ????????????????????????????????????????????????
					if (playMusic(manual, strPath, pos) < 0) {
						// ??????????????????????????????????????????
						mPlayerInited = false;
						if (manual) {
							// TtsUtil.speakText("????????????????????????");
						} else {
							removeItemByIndex(index);
							if (mMedialist.rptMediaItem.length > 0) {
								if (index >= mMedialist.rptMediaItem.length)
									index = 0;
								playMusic(manual, index, pos);
							}
						}
						return;
					}
				} else {
					LogUtil.loge("msgMedia is null or strPath is null");
					if (mPlayMode != PlayMode.PLAY_MODE_LOOP_SINGLE)
						playNext(manual);
				}
			}
		} finally {
			try {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.music.inner.musicModel",
						MessageNano.toByteArray(getCurModel()), null);
			} catch (Exception e) {
			}

			saveLastMediaStatus();
		}
	}

	public int getCurrentListIndex() {
		return mMedialist.int32CurIndex;
	}

	/**
	 * ???????????????????????????
	 * 
	 * @return
	 */
	public MediaItem getCurModel() {
		try {
			return mMedialist.rptMediaItem[mMedialist.int32CurIndex];
		} catch (Exception e) {
			return null;
		}
	}

	// ??????????????????????????????????????????
	private boolean switchMeetLastMeida() {
		if (mMedialist != null && mMedialist.rptMediaItem != null) {
			if (mMedialist.rptMediaItem.length <= 1) {
				TtsUtil.speakText("????????????????????????????????????????????????????????????", new ITtsCallback() {
					@Override
					public void onSuccess() {
						refreshMusicList();
					}

					@Override
					public void onCancel() {
						refreshMusicList();
					}
				});
				return true;
			}
		}
		return false;
	}

	// ?????????
	public void playNext(boolean manual) {
		mCurRefer = 0;
		if (mPlayMode == PlayMode.PLAY_MODE_RANDOM) {
			playRandom(manual);
			return;
		}
		if (manual) {
			if (switchMeetLastMeida())
				return;
		}
		if (mMedialist != null && mMedialist.rptMediaItem != null) {
			if (mMedialist.rptMediaItem.length > 0)
				playMusic(manual, (mMedialist.int32CurIndex + 1)
						% (mMedialist.rptMediaItem.length), 0);
			else
				LogUtil.loge("mMedialist.rptMediaItem.length is 0");
		} else
			LogUtil.loge("mMedialist is null or  mMedialist.rptMediaItem is null");
	}

	void playNextPart() {
		if (getCurModel() != null && getCurModel().rptMsgReferenceMedia != null
				&& getCurModel().rptMsgReferenceMedia.length > 0) {
			while (mCurRefer < getCurModel().rptMsgReferenceMedia.length) {
				MediaModel m = getCurModel().rptMsgReferenceMedia[mCurRefer];
				File f = new File(m.strPath);
				if (f.exists() || m.strPath.startsWith("media://")) {
					playMusic(false, m.strPath, 0);
					return;
				}
				mCurRefer++;
			}
		}

		switch (mPlayMode) {
		case PLAY_MODE_LOOP_ALL:
			playNext(false);
			break;
		case PLAY_MODE_LOOP_SINGLE:
			mCurRefer = 0;
			playMusic(false, mMedialist.int32CurIndex, 0);
			break;
		case PLAY_MODE_RANDOM:
			playRandom(false);
			break;
		}
	}

	// ?????????
	public void playPrev(boolean manual) {
		mCurRefer = 0;
		if (mPlayMode == PlayMode.PLAY_MODE_RANDOM) {
			playRandom(manual);
			return;
		}
		if (manual) {
			if (switchMeetLastMeida())
				return;
		}

		if (mMedialist != null && mMedialist.rptMediaItem != null) {
			if (mMedialist.rptMediaItem.length > 0)
				playMusic(manual, (mMedialist.int32CurIndex
						+ mMedialist.rptMediaItem.length - 1)
						% (mMedialist.rptMediaItem.length), 0);
			else
				LogUtil.loge("mMedialist.rptMediaItem.length is 0");
		} else
			LogUtil.loge("mMedialist is null or  mMedialist.rptMediaItem is null");
	}

	// ????????????
	public void playRandom(boolean manual) {
		mCurRefer = 0;
		if (manual) {
			if (switchMeetLastMeida())
				return;
		}
		Random random = new Random();
		if (mMedialist.rptMediaItem.length > 1) {
			int idx = (Math.abs(random.nextInt()))
					% (mMedialist.rptMediaItem.length - 1);
			if (idx >= mMedialist.int32CurIndex)
				++idx;
			playMusic(manual, idx, 0);
		} else {
			refreshMusicList(); // ??????????????????????????????
		}
	}

	Runnable mDelayResumeMusic = new Runnable() {
		@Override
		public void run() {
			resumePlay();
		}
	};

	public void cleanResumePlay() {
		setResumePlayProccessing(false);
		AppLogic.removeUiGroundCallback(mDelayResumeMusic);
	}

	public void resumePlay(int delay) {
		cleanResumePlay();
		setResumePlayProccessing(true);
		AppLogic.runOnUiGround(mDelayResumeMusic, delay);
	}

	public void resumePlay() {
		cleanResumePlay();
		if (mMedialist == null || mMedialist.rptMediaItem == null
				|| mMedialist.rptMediaItem.length <= 0) {
			mPlayerInited = false;
		}
		if (mPlayerInited) {
			saveLastMediaStatus();

			if (!requestFocus()) {
				LogUtil.loge("requestFocus fail");
				return;
			}
			_startPlayer();
		} else {
			startPlayer(true);
		}
	}

	public void pausePlay() {
		pausePlayTransient(true);
		mPausedByFocusLoss = false;
		saveLastMediaStatus();

		// ???????????? ????????????
		LogUtil.logd("abandonAudioFocus");
		mAm.abandonAudioFocus(afChangeListener);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
		if (isPlaying() == false) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.music.inner.isPlaying", ("" + false).getBytes(), null);
		}
	}

	public void pausePlayTransient(boolean force) {
		cleanResumePlay();
		_pausePlayer(force);
	}

	private boolean requestFocus() {
		int result = mAm.requestAudioFocus(afChangeListener,
		// Use the music stream.
				AudioManager.STREAM_MUSIC,
				// Request permanent focus.
				AudioManager.AUDIOFOCUS_GAIN);
		mHasFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
		return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
	}

	boolean mHasFocus = false;
	boolean mPausedByFocusLoss = false;
	Integer mVolumeBeforeFocusDuck = null;

	private void resumeVolume() {
		if (mVolumeBeforeFocusDuck != null) {
			LogUtil.logd("music foucsChange: resume volme");
			// mAm.setStreamVolume(AudioManager.STREAM_MUSIC,
			// mVolumeBeforeFocusDuck, 0);
			mVolumeBeforeFocusDuck = null;
			mMediaPlayer.setVolume(1.0f, 1.0f);
		}
	}

	OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			LogUtil.logd("music foucsChange:" + focusChange);
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
			case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
			case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
			case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK: {
				// ?????????????????????
				mHasFocus = true;
				cleanResumePlay();
				resumeVolume();
				if (mPausedByFocusLoss) {
					LogUtil.logd("music foucsChange: resume play");
					mPausedByFocusLoss = false;
					resumePlay(RESUME_MUSIC_DELAY);
				}
				break;
			}
			case AudioManager.AUDIOFOCUS_LOSS: {
				// ????????????
				LogUtil.logd("music foucsChange: stop");
				mHasFocus = false;
				cleanResumePlay();
				resumeVolume();
				mPausedByFocusLoss = false;
				pausePlay();
				mAm.abandonAudioFocus(afChangeListener);
				break;
			}
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
				// ????????????
				mHasFocus = false;
				resumeVolume();
				if (isPlaying()) {
					LogUtil.logd("music foucsChange: pause");
					cleanResumePlay();
					mPausedByFocusLoss = true;
					pausePlayTransient(false);
				}
				break;
			}
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
				// ????????????
				cleanResumePlay();
				if (mPausedByFocusLoss) {
					LogUtil.logd("music foucsChange: resume play");
					mPausedByFocusLoss = false;
					resumePlay();
				}
				if (mVolumeBeforeFocusDuck == null) {
					LogUtil.logd("music foucsChange: record volume");
					mVolumeBeforeFocusDuck = mAm
							.getStreamVolume(AudioManager.STREAM_MUSIC);
				}
				int volume = mAm.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 4;
				if (volume > mVolumeBeforeFocusDuck)
					volume = mVolumeBeforeFocusDuck;
				// mAm.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
				// TODO
				mMediaPlayer.setVolume(0.2f, 0.2f);
				break;
			}
			}
		}
	};

	private boolean mPlayerInited = false;

	private String mLastMusicPath = null;

	// ????????????
	private int playMusic(boolean manual, String path, int pos) {
		mLastMusicPath = path;
		// path = "http://debug.txzing.com/a.mp3?" + new Random().nextInt();
		// path = "http://192.168.0.109:8080/a.mp3?" + new Random().nextInt();
		// path = "http://192.168.0.105:8080/a.mp3?" + new Random().nextInt();

		LogUtil.logd("begin play: " + path);

		notifyProgress(0.0);

		if (path.startsWith("http://")) {
			// TODO ??????????????????????????????music?????????????????????????????????????????????
			// MyApplication.showToast("??????????????????");

			// ??????path??????
			if (mLocalMediaServerPort != 0
					&& path.startsWith("http://127.0.0.1:")) {
				int portStart = "http://127.0.0.1:".length();
				int portEnd = path.indexOf('/', portStart);
				if (portEnd > portStart) {
					path = "http://127.0.0.1:" + mLocalMediaServerPort
							+ path.substring(portEnd);
					LogUtil.logd("change url port[" + mLocalMediaServerPort
							+ "]: " + path);
				}
			}

			if (!Utils.checkSpaceEnough()) {// ????????????????????????
				// TtsUtil.speakText("????????????????????????????????????????????????????????????");
				Utils.showToast("????????????????????????????????????????????????????????????");
				return -1;
			}
		} else {
			if (!new File(path).exists()) {
				if (manual)
					TtsUtil.speakText("??????????????????????????????");
				return -1;
			}
		}

		if (!requestFocus()) {
			if (manual)
				TtsUtil.speakText("????????????????????????");
			LogUtil.loge("request focus fail");
			return 1;
		}
		try {
			mMediaPlayer.reset();
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setDataSource(path);
			// mMediaPlayer.setDataSource("http://192.168.0.109:8080/a.mp3");
			mMediaPlayer.setOnPreparedListener(new PreparedListener(pos));
			// _startPlayer();
			// mMediaPlayer.seekTo(pos);
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					if (mStop) {
						return;
					}
					// ???????????? ????????????
					LogUtil.logd("abandonAudioFocus");
					mAm.abandonAudioFocus(afChangeListener);
					playNextPart();
				}
			});
			mMediaPlayer.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					// ????????????????????????onCompletion
					try {
						LogUtil.loge("MediaPlayer OnError: " + +what + ","
								+ extra);
					} catch (Exception e) {
					}
					return false;
				}
			});
			mBufferProcessing = true;
			MyService.broadcastSubscribService(
					"musicStatus.isBufferProccessing",
					("" + mBufferProcessing).getBytes());
			mMediaPlayer.prepareAsync();
			mMusicStatusListner.onPlayStart();

			return 0;
		} catch (Exception e) {
			LogUtil.logd("palyMusic exception" + e.getMessage());
			e.printStackTrace();
			return -1;
		}
	}

	public void setPlayMode(PlayMode mode) {
		mPlayMode = mode;

		if (mMusicStatusListner != null)
			mMusicStatusListner.onModeChanged();
	}

	public PlayMode getPlayMode() {
		return mPlayMode;
	}

	public String getPlayModeString() {
		switch (mPlayMode) {
		case PLAY_MODE_LOOP_ALL:
			return "all";
		case PLAY_MODE_LOOP_SINGLE:
			return "single";
		case PLAY_MODE_RANDOM:
			return "random";
		default:
			break;
		}
		return "";
	}

	boolean mResumePlayProccessing = false;

	private void setResumePlayProccessing(boolean b) {
		mResumePlayProccessing = b;
	}
	
	private boolean getResumePlayProccessing() {
		return mResumePlayProccessing;
	}

	public boolean isPlaying() {
		return _isPlaying() || getResumePlayProccessing();
	}

	boolean mBufferProcessing = false;

	public boolean isBufferProccessing() {
		MediaItem model = getCurModel();
		if (model == null)
			return false;
		if (!(model.msgMedia.strPath.startsWith("http://")))
			return false;
		return mBufferProcessing;
	}

	public int getCurrentPosition() {
		if (mBufferProcessing) {
			return 0;
		}
		return mMediaPlayer.getCurrentPosition();
	}

	public long getCurrentDuration() {
		if (mBufferProcessing) {
			return 0;
		}
		return mMediaPlayer.getDuration();
	}

	private final class PreparedListener implements OnPreparedListener {
		private int currentTime;

		public PreparedListener(int currentTime) {
			this.currentTime = currentTime;
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			LogUtil.logd("onPrepared");
			mBufferProcessing = false;
			MyService.broadcastSubscribService(
					"musicStatus.isBufferProccessing",
					("" + mBufferProcessing).getBytes());
			_startPlayer();
			if (currentTime > 0) { // ??????????????????????????????
				mp.seekTo(currentTime);
			}
			mPlayerInited = true;

			saveLastMediaStatus();
		}
	}

	public static String formatTime(long time) {
		return String.format("%02d:%02d", time / (1000 * 60),
				(time / 1000) % 60);
	}

	public String getGenres(long audioId) {
		Uri uri = Uri.parse("content://media/external/audio/media/" + audioId
				+ "/genres");
		Cursor c = GlobalContext
				.get()
				.getContentResolver()
				.query(uri,
						new String[] { android.provider.MediaStore.Audio.GenresColumns.NAME },
						null, null, null);
		if (c.moveToFirst()) {
			String genre = c.getString(c.getColumnIndex(GenresColumns.NAME));
			c.close();
			return genre;
		}
		return "unknown";
	}

	private String mLastCategoryName = null;

	/**
	 * ???????????????????????????
	 * 
	 * @param category
	 * @param play
	 */
	public void setCategory(MediaCategory category, boolean play) {
		setMediaList(category.msgMediaList, play);
		mLastCategoryName = category.strCategoryName;
		if (category.uint32CategoryType != null
				&& category.uint32CategoryType == UiMusic.MEDIA_CATEGORY_TYPE_ONLINE_LIST
				&& category.uint32CategoryId != null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.music.inner.getSongListByCategory",
					("" + category.uint32CategoryId).getBytes(), null);
		}
	}

	public int appendMediaList(MediaList lst) {
		// ?????????????????????
		HashSet<String> urls = new HashSet<String>();
		ArrayList<MediaItem> newitems = new ArrayList<MediaItem>();
		for (int i = 0; i < mMedialist.rptMediaItem.length; ++i) {
			newitems.add(mMedialist.rptMediaItem[i]);
			urls.add(mMedialist.rptMediaItem[i].msgMedia.strPath);
		}
		int n = 0;
		for (int i = 0; i < lst.rptMediaItem.length; ++i) {
			if (urls.contains(lst.rptMediaItem[i].msgMedia.strPath))
				continue;
			newitems.add(lst.rptMediaItem[i]);
			n++;
		}
		mMedialist.rptMediaItem = new MediaItem[newitems.size()];
		newitems.toArray(mMedialist.rptMediaItem);
		if (n > 0) {
			TtsUtil.speakText("????????????" + n + "???????????????????????????????????????");
		} else {
			TtsUtil.speakText("????????????" + lst.rptMediaItem.length + "??????????????????????????????");
		}

		if (mMusicStatusListner != null)
			mMusicStatusListner.onMediaListChange(mMedialist);

		return n;
	}

	/**
	 * ????????????ListView????????????
	 * 
	 * @param mediaList
	 * @param play
	 */
	public void setMediaList(MediaList mediaList, boolean play) {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.inner.cancelSerachMedia", null, null);

		if (mediaList.msgSearchFilter != null
				&& mediaList.msgSearchFilter.rptStrArtist != null
				&& mediaList.msgSearchFilter.rptStrArtist.length > 0
				&& ProtoBufferUtil
						.isStringEmpty(mediaList.msgSearchFilter.strTitle)
				&& ProtoBufferUtil
						.isStringEmpty(mediaList.msgSearchFilter.strAlbum)
				&& ProtoBufferUtil
						.isStringEmpty(mediaList.msgSearchFilter.strType)) {
			// ???????????????????????????????????????
			mediaList.int32CurIndex = 0;
		}
		mLastCategoryName = null;
		boolean bFind = false;
		if (mediaList.int32CurIndex == null) {
			mediaList.int32CurIndex = 0;
			// ?????????????????????????????????????????????
			if (mLastMusicPath != null) {
				for (int i = 0; i < mediaList.rptMediaItem.length; ++i) {
					if (mLastMusicPath
							.equals(mediaList.rptMediaItem[i].msgMedia.strPath)) {
						mediaList.int32CurIndex = i;
						bFind = true;
						break;
					}
				}
			}
		}
		if (mediaList.int32CurPosition == null)
			mediaList.int32CurPosition = 0;

		// ??????????????????????????????????????????????????????
		if (bFind == false) {
			if (_isPlaying())
				_stopPlayer();
			mPlayerInited = false;
		}
		mMedialist = mediaList;
		if (play) {
			mLoadedHistory = true; // ???????????????????????????????????????????????????????????????????????????
			if (_isPlaying() == false)
				startPlayer(true);
		}

		if (mMusicStatusListner != null)
			mMusicStatusListner.onMediaListChange(mMedialist);
	}

	private IMusicListner mMusicStatusListner = new IMusicListner() {
		@Override
		public void onPlayStop() {
			if (mMusicStatusListner_UI != null)
				mMusicStatusListner_UI.onPlayStop();
			MyService.broadcastSubscribService("musicStatus.isPlaying",
					("" + MusicService.getInstance().isPlaying()).getBytes());
		}

		@Override
		public void onPlayStart() {
			if (mMusicStatusListner_UI != null)
				mMusicStatusListner_UI.onPlayStart();
			MyService.broadcastSubscribService("musicStatus.isPlaying",
					("" + MusicService.getInstance().isPlaying()).getBytes());
		}

		@Override
		public void onModeChanged() {
			if (mMusicStatusListner_UI != null)
				mMusicStatusListner_UI.onModeChanged();
			MyService.broadcastSubscribService("musicStatus.updatePlayMode",
					MusicService.getInstance().getPlayModeString().getBytes());
		}

		@Override
		public void onMediaListChange(MediaList rptMediaItem) {
			if (mMusicStatusListner_UI != null)
				mMusicStatusListner_UI.onMediaListChange(rptMediaItem);
			MyService.broadcastSubscribService("musicStatus.updateMusicList",
					MessageNano.toByteArray(MusicService.getInstance()
							.getMediaList()));
		}

		@Override
		public void onCategoryListUpdated(MediaCategoryList pbMediaCategoryList) {
			if (mMusicStatusListner_UI != null)
				mMusicStatusListner_UI
						.onCategoryListUpdated(pbMediaCategoryList);
		}
	};

	private IMusicListner mMusicStatusListner_UI;

	public void setListener(IMusicListner l) {
		mMusicStatusListner_UI = l;
	}

	public interface IMusicListner {

		void onMediaListChange(MediaList rptMediaItem);

		void onPlayStart();

		void onPlayStop();

		void onModeChanged();

		void onCategoryListUpdated(MediaCategoryList pbMediaCategoryList);
	}

	// ??????????????????????????????
	Runnable mAutoSavePosRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			saveLastMediaStatus();
		}
	};

	public void notifyProgress(Double p) {
		if (p == null) {
			try {
				p = (((double) getCurrentPosition())) / getCurrentDuration();
			} catch (Exception e) {
				p = 0.0;
			}
		}
		MyService.broadcastSubscribService("musicStatus.updateProgress",
				("" + p).getBytes());
	}

	public void saveLastMediaStatus() {
		mMedialist.int32CurPosition = getCurrentPosition();
		notifyProgress(null);
		try {
			File f = new File(GlobalContext.get().getApplicationInfo().dataDir
					+ "/medias/history.dat");
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
			FileOutputStream fs = new FileOutputStream(f);
			fs.write(MediaList.toByteArray(mMedialist));
			fs.close();
		} catch (Exception e) {
			LogUtil.logd("saveLastMediaStatus exception" + e.getMessage());
		}
		if (isPlaying()) {
			AppLogic.removeBackGroundCallback(mAutoSavePosRunnable);
			AppLogic.runOnBackGround(mAutoSavePosRunnable, 5000);
		}
	}

	public MediaList loadLastMediaStatus() {
		try {
			File f = new File(GlobalContext.get().getApplicationInfo().dataDir
					+ "/medias/history.dat");
			if (f.exists()) {
				FileInputStream fs = new FileInputStream(f);
				byte[] buf = new byte[fs.available()];
				fs.read(buf);
				fs.close();
				mLoadedHistory = true;
				MediaList lst = MediaList.parseFrom(buf);
				mLastCategoryName = lst.strTitle;
			}
		} catch (Exception e) {
			LogUtil.logd("loadLastMediaStatus exception" + e.getMessage());
		}
		mLoadedHistory = false;
		return new MediaList();
	}

	public void syncMediaList(MediaList list) {
		if (!mLoadedHistory
				|| (mMedialist == null || mMedialist.rptMediaItem == null || mMedialist.rptMediaItem.length == 0)) {
			setMediaList(list, false);
		}
	}

	public String getMediaModelIndex(MediaModel model) {
		if (model == null)
			return null;
		if (model.uint64AudioId > 0) {
			return model.uint32AppId + "|" + model.uint64AudioId;
		}
		return model.strPath;
	}

	public void syncMediaCategoryList(MediaCategoryList pbMediaCategoryList) {
		LogUtil.logd("syncMediaCategoryList:" + mLastCategoryName);
		mMediaCategoryList = pbMediaCategoryList;
		if (null != mLastCategoryName) {
			for (int i = 0; i < pbMediaCategoryList.rptMsgCategoryList.length; ++i) {
				if (mLastCategoryName
						.equals(pbMediaCategoryList.rptMsgCategoryList[i].strCategoryName)) {
					Set<String> setPath = new HashSet<String>();
					for (int j = 0; j < mMedialist.rptMediaItem.length; ++j) {
						setPath.add(getMediaModelIndex(mMedialist.rptMediaItem[j].msgMedia));
					}
					List<MediaItem> lst = new ArrayList<MediaItem>();
					for (int j = 0; j < pbMediaCategoryList.rptMsgCategoryList[i].msgMediaList.rptMediaItem.length; ++j) {
						if (setPath
								.contains(getMediaModelIndex(pbMediaCategoryList.rptMsgCategoryList[i].msgMediaList.rptMediaItem[j].msgMedia)))
							continue;
						lst.add(pbMediaCategoryList.rptMsgCategoryList[i].msgMediaList.rptMediaItem[j]);

					}
					MediaCategory category = new MediaCategory();
					category.strCategoryName = mLastCategoryName;
					category.msgMediaList = new MediaList();
					category.msgMediaList.int32CurIndex = null;
					category.msgMediaList.rptMediaItem = new MediaItem[mMedialist.rptMediaItem.length
							+ lst.size()];
					for (int j = 0; j < mMedialist.rptMediaItem.length; ++j) {
						category.msgMediaList.rptMediaItem[j] = mMedialist.rptMediaItem[j];
					}
					for (int j = 0; j < lst.size(); ++j) {
						category.msgMediaList.rptMediaItem[j
								+ mMedialist.rptMediaItem.length] = lst.get(j);
					}
					category.msgMediaList.strTitle = mLastCategoryName;
					setCategory(category, false);
					break;
				}
			}
		}

		if (mMusicStatusListner != null)
			mMusicStatusListner.onCategoryListUpdated(mMediaCategoryList);
	}

	public int getCurIndex() {
		if (mMedialist != null && mMedialist.int32CurIndex != null)
			return mMedialist.int32CurIndex;
		return -1;
	}

	private MediaCategoryList mMediaCategoryList;

	public MediaCategoryList getMediaCategoryList() {
		return mMediaCategoryList;
	}

	int mCurrCategory;

	public int getCurrentCategory() {
		return mCurrCategory;
	}

	public void setCurrCategory(int index) {
		this.mCurrCategory = index;
	}

	public void favouriteMusic(MediaModel m, boolean favour) {
		LogUtil.logd("favouriteMusic: app_id=" + m.uint32AppId + ", audio_id="
				+ m.uint64AudioId);

		if (favour) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.music.inner.favourMusic", MessageNano.toByteArray(m),
					null);
		} else {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.music.inner.unfavourMusic",
					MessageNano.toByteArray(m), null);
		}
	}

	public void updateMediaModelUrl(MediaModel model) {
		try {
			// ??????????????????????????????
			if (getMediaModelIndex(model).equals(
					getMediaModelIndex(getCurModel().msgMedia))) {
				if (mBufferProcessing) {
					LogUtil.logd("play download music path: " + model.strPath);
					playMusic(false, model.strPath, 0);
				}
			}
		} catch (Exception e) {
		}
		if (updateMediaModelUrl(mMedialist, model)) {
			mMusicStatusListner.onMediaListChange(mMedialist);
		}
		boolean changed = false;
		if (mMediaCategoryList != null
				&& mMediaCategoryList.rptMsgCategoryList != null) {
			for (int i = 0; i < mMediaCategoryList.rptMsgCategoryList.length; ++i) {
				if (updateMediaModelUrl(
						mMediaCategoryList.rptMsgCategoryList[i].msgMediaList,
						model)) {
					changed = true;
				}
			}
		}
		if (changed) {
			mMusicStatusListner.onCategoryListUpdated(mMediaCategoryList);
		}
	}

	private boolean updateMediaModelUrl(MediaList list, MediaModel model) {
		if (model == null)
			return false;
		String index = getMediaModelIndex(model);
		if (index == null)
			return false;
		boolean ret = false;
		if (list != null && list.rptMediaItem != null) {
			for (int i = 0; i < list.rptMediaItem.length; ++i) {
				if (list.rptMediaItem[i].msgMedia == null)
					continue;
				if (index
						.equals(getMediaModelIndex(list.rptMediaItem[i].msgMedia))) {
					LogUtil.logd("update media: path="
							+ list.rptMediaItem[i].msgMedia.strPath + "|"
							+ model.strPath);
					if (list.rptMediaItem[i].msgMedia.strPath
							.equals(model.strPath) == false) {
						ret = true;
					}
					if (list.rptMediaItem[i].msgMedia.strPath
							.equals(mLastMusicPath))
						mLastMusicPath = model.strPath;
					list.rptMediaItem[i].msgMedia.strPath = model.strPath;
				}
			}
		}
		return ret;
	};

	public void updateMediaModelFavour(MediaModel model) {
		if (updateMediaModelFavour(mMedialist, model)) {
			mMusicStatusListner.onMediaListChange(mMedialist);
		}
		boolean changed = false;
		if (mMediaCategoryList != null
				&& mMediaCategoryList.rptMsgCategoryList != null) {
			for (int i = 0; i < mMediaCategoryList.rptMsgCategoryList.length; ++i) {
				if (updateMediaModelFavour(
						mMediaCategoryList.rptMsgCategoryList[i].msgMediaList,
						model)) {
					changed = true;
				}
			}
		}
		if (changed) {
			mMusicStatusListner.onCategoryListUpdated(mMediaCategoryList);
		}
	}

	private boolean updateMediaModelFavour(MediaList list, MediaModel model) {
		if (model == null)
			return false;
		String index = getMediaModelIndex(model);
		if (index == null)
			return false;
		boolean ret = false;
		if (list != null && list.rptMediaItem != null) {
			for (int i = 0; i < list.rptMediaItem.length; ++i) {
				if (list.rptMediaItem[i].msgMedia == null)
					continue;
				if (index
						.equals(getMediaModelIndex(list.rptMediaItem[i].msgMedia))) {
					LogUtil.logd("update media: favour="
							+ list.rptMediaItem[i].msgMedia.bFavourite + "|"
							+ model.bFavourite);
					if (list.rptMediaItem[i].msgMedia.bFavourite != model.bFavourite) {
						ret = true;
					}
					list.rptMediaItem[i].msgMedia.bFavourite = model.bFavourite;
				}
			}
		}
		return ret;
	}
}

package com.txznet.txz.component.music.kaola;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.kaolafm.sdk.client.ErrorInfo;
import com.kaolafm.sdk.client.KLClientAPI;
import com.kaolafm.sdk.client.Music;
import com.kaolafm.sdk.client.PlayState;
import com.kaolafm.sdk.client.SearchResult;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.txz.component.audio.AudioSelector;
import com.txznet.txz.component.audio.AudioSelector.AudioSelectorListener;
import com.txznet.txz.component.music.IMusic;
import com.txznet.txz.component.selector.Selector;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.TXZHandler;
import com.txznet.txz.util.runnables.Runnable1;

import android.os.HandlerThread;
import android.os.RemoteException;
import android.text.TextUtils;

public class AdapterKaola implements IMusic {
	private boolean mIsTimeOut;
	private static final String KEY = "auto";
	private static final int SEARCH_TIME_OUT = 5000;
	public static boolean SHOW_SEARCH_RESULT = true;
	private static AdapterKaola sAdapterKaola = new AdapterKaola();

	private AdapterKaola() {
		KLClientAPI.getInstance().init(GlobalContext.get(), KEY);
	}

	public static AdapterKaola getAdapter() {
		return sAdapterKaola;
	}

	@Override
	public String getPackageName() {
		return "com.edog.car";
	}

	@Override
	public boolean isPlaying() {
		return false;
	}

	@Override
	public void start() {
		AppLogic.removeBackGroundCallback(mExitRunnable);
		if (KLClientAPI.getInstance() != null) {
			KLClientAPI.getInstance().launchApp(true);
		}
	}

	Runnable mExitRunnable = new Runnable() {
		@Override
		public void run() {
			KLClientAPI.getInstance().exitApp();
		}
	};

	@Override
	public void pause() {
		MusicManager.getInstance().releaseAudioFocusImmediately();
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				if (KLClientAPI.getInstance() != null) {
					KLClientAPI.getInstance().pause();
				}
			}
		}, 1000);
	}

	@Override
	public void exit() {
		// 延迟2秒关闭，防止音量没有恢复
		MusicManager.getInstance().releaseAudioFocusImmediately();
		AppLogic.removeBackGroundCallback(mExitRunnable);
		AppLogic.runOnBackGround(mExitRunnable, 2000);
	}

	public PlayState getPlayState() {
		return KLClientAPI.getInstance().getPlayState();
	}

	@Override
	public void next() {
		AppLogic.removeBackGroundCallback(mExitRunnable);
		if (KLClientAPI.getInstance() != null) {
			KLClientAPI.getInstance().playNext();
		}
	}

	@Override
	public void prev() {
		AppLogic.removeBackGroundCallback(mExitRunnable);
		if (KLClientAPI.getInstance() != null) {
			KLClientAPI.getInstance().playPre();
		}
	}

	@Override
	public void switchModeLoopAll() {
	}

	@Override
	public void switchModeLoopOne() {
	}

	@Override
	public void switchModeRandom() {
	}

	@Override
	public void switchSong() {
		AppLogic.removeBackGroundCallback(mExitRunnable);
		for (int i = new Random().nextInt(5); i >= 0; --i) {
			next();
		}
	}

	@Override
	public void playRandom() {
		AppLogic.removeBackGroundCallback(mExitRunnable);
		start();
	}

	@Override
	public void playMusic(MusicModel musicModel) {
		AppLogic.removeBackGroundCallback(mExitRunnable);
		final String[] kws = musicModel.getKeywords();
		if (kws != null && kws.length > 0) {
			searchMusic(kws[0]);
			return;
		}
		String[] artists = musicModel.getArtist();
		String artist = null;
		if (artists != null && artists.length > 0)
			artist = artists[0];
		String title = musicModel.getTitle();
		if (title != null) {
			if (artist == null) {
				artist = "";
			}
			artist += title;
		}
		searchMusic(artist);
	}

	KLSearchRunnableTask mSearchRunnable;
	OnSearchListener mCurListener;

	public void searchMusic(String musicUrl) {
		if (mSearchRunnable == null) {
			mSearchRunnable = new KLSearchRunnableTask(musicUrl) {

				@Override
				public void run() {
					checkTimeOut();
					if (mCurListener != null) {
						mCurListener.cancelRequest();
					}

					KLClientAPI.getInstance().search(mP1, mCurListener = new OnSearchListener(0) {

						@Override
						public void onRespSuccess(final List<Music> arg0) {
							AppLogic.removeBackGroundCallback(mSearchTimeOut);
							if (mIsTimeOut) {
								return;
							}

							if (SHOW_SEARCH_RESULT) {
								List<AudioSelector.Music> musicList = new ArrayList<AudioSelector.Music>();
								if (arg0 != null) {
									for (Music m : arg0) {
										AudioSelector.Music object = new AudioSelector.Music();
										object.audioName = m.audioName;
										object.authorName = m.authorName;
										object.sourceName = m.describe;
										musicList.add(object);
									}
								}

								Selector.entryAudioSelector(musicList, mP1, new AudioSelectorListener() {

									@Override
									public void onAudioSelected(AudioSelector.Music music, int index) {
										Music m = getMusicFromMusic(arg0, music);
										KLClientAPI.getInstance().play(m);
									}
								});
							} else {
								if (arg0 != null && arg0.size() > 0) {
									KLClientAPI.getInstance().play(arg0.get(0));
								}
							}
						}

						@Override
						public void onRespFailure(ErrorInfo info) {
							AppLogic.removeBackGroundCallback(mSearchTimeOut);
							if (mIsTimeOut) {
								return;
							}
							String kaola = NativeData
									.getResString("RS_VOICE_KAOLA");
							String spk = NativeData
									.getResPlaceholderString(
											"RS_VOICE_SEARCH_ERROR",
											"%CMD%", kaola);
							procError(spk);
						}
					});
				}
			};
		}
		mSearchRunnable.update(musicUrl);

		ensureHandler();
		mTxzHandler.removeCallbacks(mSearchRunnable);
		mTxzHandler.postDelayed(mSearchRunnable, 50);
	}

	Runnable mSearchTimeOut = new Runnable() {

		@Override
		public void run() {
			mIsTimeOut = true;
			String kaola = NativeData.getResString("RS_VOICE_KAOLA");
			String spk = NativeData.getResPlaceholderString(
					"RS_VOICE_SEARCH_TIMEOUT", "%CMD%", kaola);
			procError(spk);
		}
	};

	private void procError(String hint) {
		AsrManager.getInstance().cancel();
		RecorderWin.addSystemMsg(hint);
		TtsManager.getInstance().speakText(hint, new TtsUtil.ITtsCallback() {

			@Override
			public void onSuccess() {
				AsrManager.getInstance().start();
			};
		});
	}

	private void checkTimeOut() {
		mIsTimeOut = false;
		AppLogic.removeBackGroundCallback(mSearchTimeOut);
		AppLogic.runOnBackGround(mSearchTimeOut, SEARCH_TIME_OUT);
	}

	@Override
	public MusicModel getCurrentMusicModel() {
		return null;
	}

	@Override
	public void favourMusic() {
		String kaola = NativeData.getResString("RS_VOICE_KAOLA");
		String spk = NativeData.getResPlaceholderString(
				"RS_MUSIC_FAVOUR_UNSUPPORT", "%CMD%", kaola);
		RecorderWin.speakTextWithClose(spk, null);
	}

	@Override
	public void unfavourMusic() {
		String kaola = NativeData.getResString("RS_VOICE_KAOLA");
		String spk = NativeData.getResPlaceholderString(
				"RS_MUSIC_FAVOUR_UNSUPPORT", "%CMD%", kaola);
		RecorderWin.speakTextWithClose(spk, null);
	}

	@Override
	public void playFavourMusic() {
		String kaola = NativeData.getResString("RS_VOICE_KAOLA");
		String spk = NativeData.getResPlaceholderString("RS_MUSIC_UNSUPPORT_PLAY",
				"%CMD%", kaola);
		RecorderWin.speakTextWithClose(spk, new Runnable() {

			@Override
			public void run() {
				KLClientAPI.getInstance().launchApp(true);
			}
		});
	}

	@Override
	public void setStatusListener(MusicToolStatusListener listener) {
	}

	private abstract class KLSearchRunnableTask extends Runnable1<String> {

		public KLSearchRunnableTask(String p1) {
			super(p1);
		}

		public void update(String keywords) {
			mP1 = keywords;
		}
	}

	public String getCurrentMusicName() {
		return KLClientAPI.getInstance().getCurrentMusicInfo() != null
				? KLClientAPI.getInstance().getCurrentMusicInfo().audioName : null;
	}

	HandlerThread mThread;
	TXZHandler mTxzHandler;

	private void ensureHandler() {
		if (mTxzHandler == null) {
			mThread = new HandlerThread("kaola-search-thread");
			mThread.start();
			mTxzHandler = new TXZHandler(mThread.getLooper());
		}
	}

	private Music getMusicFromMusic(List<Music> mcs, AudioSelector.Music mc) {
		if (mcs == null) {
			return null;
		}
		if (mc == null) {
			return mcs.get(0);
		}
		String audio = mc.audioName;
		String author = mc.authorName;
		String source = mc.sourceName;
		if (TextUtils.isEmpty(audio)) {
			audio = "";
		}
		if (TextUtils.isEmpty(author)) {
			author = "";
		}
		if (TextUtils.isEmpty(source)) {
			source = "";
		}

		for (Music m : mcs) {
			String audioName = m.audioName;
			String authorName = m.authorName;
			String sourceName = m.describe;
			if (TextUtils.isEmpty(audioName)) {
				audioName = "";
			}
			if (TextUtils.isEmpty(authorName)) {
				authorName = "";
			}
			if (TextUtils.isEmpty(sourceName)) {
				sourceName = "";
			}

			if (audioName.equals(audio) && author.equals(author) && sourceName.equals(source)) {
				return m;
			}
		}
		return null;
	}

	@Override
	public boolean isBuffering() {
		return false;
	}

	@Override
	public void cancelRequest() {
		if (mCurListener != null) {
			mCurListener.cancelRequest();
		}
	}

	public abstract class OnSearchListener extends SearchResult {
		public static final int INVILDATE_SESSION = -1;
		int mSession;

		public OnSearchListener(int session) {
			mSession = session;
		}

		public void cancelRequest() {
			mSession = INVILDATE_SESSION;
		}

		@Override
		public void onFailure(ErrorInfo arg0) throws RemoteException {
			if (mSession == INVILDATE_SESSION) {
				JNIHelper.logd("onFailure cancel");
				return;
			}

			onRespFailure(arg0);
		}

		@Override
		public void onSuccess(List<Music> arg0) throws RemoteException {
			if (mSession == INVILDATE_SESSION) {
				JNIHelper.logd("onSuccess cancel");
				return;
			}
			onRespSuccess(arg0);
		}

		public abstract void onRespFailure(ErrorInfo info);

		public abstract void onRespSuccess(List<Music> music);
	}
}
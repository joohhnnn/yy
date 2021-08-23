package com.txznet.txz.component.music.kuwo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.txz.component.audio.AudioSelector;
import com.txznet.txz.component.audio.AudioSelector.AudioSelectorListener;
import com.txznet.txz.component.music.BroadcastMusicTool;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import android.text.TextUtils;
import cn.kuwo.autosdk.api.KWAPI;
import cn.kuwo.autosdk.api.OnEnterListener;
import cn.kuwo.autosdk.api.OnExitListener;
import cn.kuwo.autosdk.api.OnPlayEndListener;
import cn.kuwo.autosdk.api.OnSearchListener;
import cn.kuwo.autosdk.api.PlayEndType;
import cn.kuwo.autosdk.api.PlayMode;
import cn.kuwo.autosdk.api.PlayState;
import cn.kuwo.autosdk.api.SearchStatus;
import cn.kuwo.autosdk.bean.notproguard.Music;

public class MusicKuwoImpl extends BroadcastMusicTool {
	
	static {
		if (PackageManager.getInstance().checkAppExist("cn.kuwo.kwmusiccar")) {
			PACKAGE_NAME = "cn.kuwo.kwmusiccar";
		}

		if (PackageManager.getInstance().checkAppExist("cn.kuwo.player")) {
			PACKAGE_NAME = "cn.kuwo.player";
		}
	}

	public static boolean SHOW_SEARCH_RESULT = false;
	public static String PACKAGE_NAME = "cn.kuwo.kwmusiccar";
	public static KWAPI mKWAPI = null;
	
	public MusicKuwoImpl() {
		try {
			mKWAPI = KWAPI.createKWAPI(GlobalContext.get(), "auto");
			mKWAPI.bindAutoSdkService(GlobalContext.get());
			mKWAPI.registerEnterListener(GlobalContext.get(), new OnEnterListener() {

				@Override
				public void onEnter() {
					mHasExited = false;
					LogUtil.logd("MusicKuwoImpl onEnter");
				}
			});
			mKWAPI.registerExitListener(GlobalContext.get(), new OnExitListener() {

				@Override
				public void onExit() {
					mHasExited = true;
					LogUtil.logd("MusicKuwoImpl onExit");
				}
			});
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}

	boolean mHasExited = true;

	private SeqOnSearchListener mListener;
	
	@Override
	public String getPackageName() {
		return PACKAGE_NAME;
	}

	@Override
	public boolean isPlaying() {
		return false;// mIsPlaying;
	}
	
	@Override
	public void start() {
		super.start();
		AppLogic.removeBackGroundCallback(mExitRunnable);
		JNIHelper.logd("MusicKw start");
		mKWAPI.startAPP(GlobalContext.get(), true);

		// 尝试启动播放
		AppLogic.removeBackGroundCallback(mPlayRunnable);
		AppLogic.runOnBackGround(mPlayRunnable, 1000);
	}
	
	Runnable mPlayRunnable = new Runnable() {

		@Override
		public void run() {
			continuePlay();
		}
	};

	Runnable mExitRunnable = new Runnable() {
		@Override
		public void run() {
			exitImmediately();
		}
	};

	@Override
	public void exit() {
		super.exit();
		// 延迟2秒关闭，防止音量没有恢复
		MusicManager.getInstance().releaseAudioFocusImmediately();
		AppLogic.removeBackGroundCallback(mExitRunnable);
		AppLogic.runOnBackGround(mExitRunnable, 2000);
	}

	@Override
	public void exitImmediately() {
		super.exitImmediately();
		JNIHelper.logd("MusicKw exit");
		mKWAPI.exitAPP(GlobalContext.get());
	}
	
	@Override
	public void pause() {
		super.pause();
		// 延迟1秒暂停，防止音量没有恢复
		MusicManager.getInstance().releaseAudioFocusImmediately();
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				mKWAPI.setPlayState(GlobalContext.get(), PlayState.STATE_PAUSE);
			}
		}, 1000);
	}
	
	
	/**
	 * 继续播放，不应该执行打开音乐的操作
	 */
	public void continuePlay(){
		AppLogic.removeBackGroundCallback(mExitRunnable);
		mKWAPI.setPlayState(GlobalContext.get(), PlayState.STATE_PLAY);
	}

	@Override
	public void next() {
		super.next();
		JNIHelper.logd("musicKw next");
		AppLogic.removeBackGroundCallback(mExitRunnable);
		mKWAPI.setPlayState(GlobalContext.get(), PlayState.STATE_NEXT);
	}

	@Override
	public void prev() {
		super.prev();
		JNIHelper.logd("musicKw prev");
		AppLogic.removeBackGroundCallback(mExitRunnable);
		mKWAPI.setPlayState(GlobalContext.get(), PlayState.STATE_PRE);
	}

	@Override
	public void switchModeLoopAll() {
		super.switchModeLoopAll();
		JNIHelper.logd("musicKw switchModeLoopAll");
		mKWAPI.setPlayMode(GlobalContext.get(), PlayMode.MODE_ALL_CIRCLE);
	}

	@Override
	public void switchModeLoopOne() {
		super.switchModeLoopOne();
		JNIHelper.logd("musicKw switchModeLoopOne");
		mKWAPI.setPlayMode(GlobalContext.get(), PlayMode.MODE_SINGLE_CIRCLE);
	}

	@Override
	public void switchModeRandom() {
		super.switchModeRandom();
		mKWAPI.setPlayMode(GlobalContext.get(), PlayMode.MODE_ALL_RANDOM);
	}

	@Override
	public void switchSong() {
		super.switchSong();
		AppLogic.removeBackGroundCallback(mExitRunnable);
		for (int i = new Random().nextInt(5); i >= 0; --i) {
			mKWAPI.setPlayState(GlobalContext.get(), PlayState.STATE_NEXT);
		}
	}
	
	public boolean isKuwoRunning() {
		boolean isRunning = mKWAPI.isKuwoRunning(GlobalContext.get());
		LogUtil.logd("isKuwoRunning isRunning:" + isRunning + ",mHasExited:" + mHasExited);
		if (!isRunning) {
			return !mHasExited;
		}
//		return isRunning && !mHasExited;
		return isRunning;
	}

	@Override
	public void playRandom() {
		super.playRandom();
		AppLogic.removeBackGroundCallback(mExitRunnable);
		mKWAPI.startAPP(GlobalContext.get(), true);
	}

	@Override
	public void playMusic(MusicModel musicModel) {
		super.playMusic(musicModel);
		AppLogic.removeBackGroundCallback(mExitRunnable);
		String[] kws = musicModel.getKeywords();
		String kw = "";
		String title = musicModel.getTitle();
		if (!TextUtils.isEmpty(title)) {
			kw = title;
		}
		if (kws != null && kws.length > 0) {
			for (String k : kws) {
				if (TextUtils.isEmpty(k))
					continue;
				if (!kw.isEmpty()) {
					kw += "";
				}
				kw += k;
			}
		}
		String[] artists = musicModel.getArtist();
		String artist = null;
		if (artists != null && artists.length > 0)
			artist = artists[0];
		if (SHOW_SEARCH_RESULT) {
			searchMusic((TextUtils.isEmpty(artist) ? "" : artist) + " " + kw + " "
					+ (musicModel.getAlbum() == null ? "" : musicModel.getAlbum()));
		} else {
			mKWAPI.playClientMusics(GlobalContext.get(), kw, artist, musicModel.getAlbum());
		}
	}

	private int mLastSearchSeq = 0;
	int mSearchSeq = new Random().nextInt();
	int getNextSearchSeq() {
		++mSearchSeq;
		if (mSearchSeq == 0)
			++mSearchSeq;
		return mSearchSeq;
	}
	
	abstract class SeqOnSearchListener implements OnSearchListener{
		private int seqId = -1;
		public SeqOnSearchListener(int seqId) {
			this.seqId = seqId;
		}

		public void cancelRequest() {
			this.seqId = 0;
		}

		@Override
		public void searchFinshed(SearchStatus arg0, boolean arg1, List arg2, boolean arg3) {
			onSearchFinished(seqId, arg0, arg1, arg2, arg3);
		}
		public abstract void onSearchFinished(int seqId,SearchStatus status,boolean arg2,List musics,boolean timeout);
	}
	
	private void searchMusic(final String kws) {
		if (mListener != null) {
			mListener.cancelRequest();
		}

		int seq = mLastSearchSeq = getNextSearchSeq();
		mKWAPI.searchOnlineMusic(kws, mListener = new SeqOnSearchListener(seq) {
			@Override
			public void onSearchFinished(int seqId, SearchStatus status, boolean arg1, List arg2, boolean timeout) {
				if(seqId!=mLastSearchSeq){
					return;
				}
				final List<Music> musics = (List<Music>) arg2;
				if (status == SearchStatus.SUCCESS) {
					// 搜索成功，可以将歌曲列表展现出来选择性的播放
					if (musics.size() > 0) {
						if (SHOW_SEARCH_RESULT) {
							List<AudioSelector.Music> musicList = new ArrayList<AudioSelector.Music>();
							int count = musics.size()>8?8:musics.size();
							for (int i=0;i<=count-1;i++) {//只插入8个，防止超过十个之后识别有问题
								Music m = musics.get(i);
								AudioSelector.Music object = new AudioSelector.Music();
								object.audioName = m.name;
								object.authorName = m.artist;
								object.sourceName = m.source;
								musicList.add(object);
							}

							ChoiceManager.getInstance().showMusicList(musicList, kws, new AudioSelectorListener() {

								@Override
								public void onAudioSelected(AudioSelector.Music music, int index) {
									// mKWAPI.playMusic(GlobalContext.get(),
									// musics.get(index));
									mKWAPI.playMusic(GlobalContext.get(), musics, index, false, false);
								}
							});
						}
					}
				} else {
					// 搜索失败，自己进行相关提示处理
					String kuwo = NativeData.getResString("RS_VOICE_KUWO");
					String spk = NativeData.getResPlaceholderString(
							"RS_VOICE_SEARCH_ERROR", "%CMD%", kuwo);
					procError(spk);
				}
			}
		});
	}

	private void procError(String hint) {
		AsrManager.getInstance().cancel();
		RecorderWin.addSystemMsg(hint);
		TtsManager.getInstance().speakText(hint, new TtsUtil.ITtsCallback() {

			@Override
			public void onSuccess() {
				AsrManager.getInstance().start();
			}

			@Override
			public boolean isNeedStartAsr() {
				return true;
			};
		});
	}

	@Override
	public MusicModel getCurrentMusicModel() {
		return null;
	}

	@Override
	public void favourMusic() {
		String kuwo = NativeData.getResString("RS_VOICE_KUWO");
		String spk = NativeData.getResPlaceholderString(
				"RS_MUSIC_FAVOUR_UNSUPPORT", "%MUSIC%", kuwo);
		RecorderWin.speakTextWithClose(spk, null);
	}

	@Override
	public void unfavourMusic() {
		String kuwo = NativeData.getResString("RS_VOICE_KUWO");
		String spk = NativeData.getResPlaceholderString(
				"RS_MUSIC_FAVOUR_UNSUPPORT", "%MUSIC%", kuwo);
		RecorderWin.speakTextWithClose(spk, null);
	}

	@Override
	public void playFavourMusic() {
		String kuwo = NativeData.getResString("RS_VOICE_KUWO");
		String spk = NativeData.getResPlaceholderString("RS_MUSIC_UNSUPPORT_PLAY",
				"%MUSIC%", kuwo);
		RecorderWin.speakTextWithClose(spk, new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mKWAPI.startAPP(GlobalContext.get(), true);
			}
		});
	}

	@Override
	public void setStatusListener(MusicToolStatusListener listener) {
		mKWAPI.registerPlayEndListener(GlobalContext.get(), new OnPlayEndListener() {
			@Override
			public void onPlayEnd(PlayEndType type) {
				switch (type) {
				case END_COMPLETE:
					break;
				case END_ERROR:
					break;
				case END_USER:
					break;
				}
			}
		});
	}

	@Override
	public boolean isBuffering() {
		return false;
	}

	@Override
	public void cancelRequest() {
		if(mListener != null){
			mListener.cancelRequest();
		}
	}
}

package com.txznet.music.fragment.logic;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.audio.player.MediaError;
import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.audio.player.TXZAudioPlayer.OnBufferingUpdateListener;
import com.txznet.audio.player.TXZAudioPlayer.OnCompletionListener;
import com.txznet.audio.player.TXZAudioPlayer.OnErrorListener;
import com.txznet.audio.player.TXZAudioPlayer.OnPlayProgressListener;
import com.txznet.audio.player.TXZAudioPlayer.OnPreparedListener;
import com.txznet.audio.player.TXZAudioPlayer.OnSeekCompleteListener;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.bean.req.ReqDataStats.Action;
import com.txznet.music.bean.req.ReqThirdSearch;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.fragment.manager.MediaManager;
import com.txznet.music.fragment.manager.MediaManager.Status;
import com.txznet.music.utils.NetHelp;
import com.txznet.music.utils.SyncCoreData;
import com.txznet.music.utils.Utils;

/**
 * 播放管理器
 * 
 * @author ASUS User
 *
 */
public class MediaLogic implements Observer, ILogic {

	TXZAudioPlayer audioPlayer = null;
	public static Audio currentAudio = null;

	public MediaLogic() {
		currentAudio = MediaManager.getInstance().getCurrentAudio();
		init();
	}

	/**
	 * 保证初始化成功AudioPlayer
	 */
	Runnable replayRunnable = new Runnable() {

		@Override
		public void run() {
			init();
		}
	};

	private void init() {
		// 复制出来当前播放的音频

//		audioPlayer = AudioPlayFactory.createPlayer(currentAudio);

		if (null == audioPlayer) {
			AppLogic.runOnBackGround(replayRunnable, 100);
			return;
		}
		audioPlayer
				.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

					@Override
					public void onBufferingUpdate(TXZAudioPlayer ap,
							float percent) {
					}

					@Override
					public void onDownloading(TXZAudioPlayer ap,
							List<LocalBuffer> buffers) {
						InfoMessage info = new InfoMessage(
								InfoMessage.UPDATE_BUFFER);
						info.setBuffers(buffers);
						ObserverManage.getObserver().setMessage(info);
					}
				});
		audioPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(TXZAudioPlayer ap) {
				// 播放完成
				currentAudio.setLastPlayTime(String.valueOf(0.0));
//				DBSQLHelper1.updateAudioLastPlayTime(currentAudio);
				NetHelp.sendReportData(Action.NEXT_AUTO);
				ObserverManage.getObserver().send(InfoMessage.UPDATE_BUFFER);
				next();
			}

		});
		audioPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(TXZAudioPlayer ap, final MediaError err) {
				if (MediaManager.getInstance().getStatus() == Status.pause) {// 如果处于暂停状态。则不处理该错误
					return true;
				}

				// 处理：下一首，暂停：重新播放（3中方式）
				ObserverManage.getObserver().send(InfoMessage.PLAY_ERROR,
						err.getErrCode(), err.getErrDesc());
				next(1000);
				return true;
			}
		});

		audioPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(TXZAudioPlayer ap) {
				play();
			}
		});
		audioPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {

			@Override
			public void onSeekComplete(final TXZAudioPlayer ap) {
				LogUtil.logd("onSeekComplete");
				// 哪一台设备会在声控调用的时候，音乐自动调用seekComplete（迪恩捷？）
				play();
			}
		});
		audioPlayer.setOnPlayProgressListener(new OnPlayProgressListener() {

			@Override
			public boolean onPlayProgress(TXZAudioPlayer ap, float percent) {
				ObserverManage.getObserver().send(InfoMessage.PLAY_PROGRESS);
				return false;
			}
		});

		if (audioPlayer != null) {
			// 同时上报缓冲状态
			SyncCoreData.syncCurPlayerBufferingStatus();
			audioPlayer.prepareAsync();
		}
		// 假请求
		if (null != currentAudio && !Utils.isSong(currentAudio.getSid())) {// 不是歌曲
			ReqThirdSearch reqData = new ReqThirdSearch(currentAudio.getSid(),
					currentAudio.getId(), 0);
			NetHelp.sendRequest(Constant.GET_FAKE_SEARCH, reqData);
		}

	}

	public void play() {
		LogUtil.logd(TAG + "play");
		ObserverManage.getObserver().send(InfoMessage.PLAY);
	}

	public void pause() {
		LogUtil.logd(TAG + "pause");

	}

	public void next() {
		LogUtil.logd(TAG + "next");
		// changAudio(true);

	}

	public void last() {
		LogUtil.logd(TAG + "last");
		// changAudio(false);
	}

	/**
	 * 获取下一首歌的位置并播放
	 * 
	 * @param next
	 *            上一首/下一首
	 * @param manul
	 *            手动/声控
	 */
	private void jumpToNext(boolean next, boolean manul) {
		// TODO:如果下一首nextAUdio有值得话，就播放nextAuido，为了保证播报一直（问题：被手动修改了）
//		if (null != nextAudio) {
//			currentPosition = audios.indexOf(nextAudio);
//			currentAudio = nextAudio;
//			prestart();
//			return;
//		}
//
//		int playMode = SharedPreferencesUtils.getPlayMode();
//		LogUtil.logd("jump to next =" + next + ",manul=" + manul + ",playMode="
//				+ playMode);
//		if (!Utils.isSong(currentAudio.getSid())) {
//			playMusicAtPosition(next ? ++currentPosition : --currentPosition);
//			return;
//		}
//
//		if (playMode == Constant.MODESINGLECIRCLE) {
//			if (manul) {
//				currentPosition = next ? currentPosition + 1
//						: currentPosition - 1;
//			}
//			playMusicAtPosition(currentPosition);
//		} else if (playMode == Constant.MODERANDOM) {
//			int randomInt = random.nextInt(audios.size());
//			if (currentPosition == randomInt) {
//				currentPosition++;
//			} else {
//				currentPosition = randomInt;
//			}
//			playMusicAtPosition(currentPosition);
//		} else {
//			playMusicAtPosition(next ? ++currentPosition : --currentPosition);
//			return;
//		}
	}

	/**
	 * 停止播放的时候释放资源
	 */
	public void release() {

	}

	/**
	 * 延时播放下一首
	 * 
	 * @param delay
	 */
	private void next(int delay) {
		Runnable nextRunnable = new Runnable() {

			@Override
			public void run() {
				next();
			}
		};
		AppLogic.removeBackGroundCallback(nextRunnable);
		AppLogic.runOnBackGround(nextRunnable, delay);
	}

	/**
	 * 播放指定的歌曲
	 * 
	 * @param audio
	 */
	public void playAudio(Audio audio) {

	}

	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof InfoMessage) {
			InfoMessage info = (InfoMessage) data;
			switch (info.getType()) {
			case InfoMessage.PLAYCHOICE:
				playAudio(info.getmAudio());
				break;
			case InfoMessage.PLAY:
				play();
				break;
			case InfoMessage.PAUSE:
				pause();
				break;
			default:
				break;
			}

		}

	}

}

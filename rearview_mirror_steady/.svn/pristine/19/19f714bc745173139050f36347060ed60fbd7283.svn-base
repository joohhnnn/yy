package com.txznet.music.engine;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.audio.player.MediaError;
import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.audio.player.TXZAudioPlayer.OnBufferingUpdateListener;
import com.txznet.audio.player.TXZAudioPlayer.OnCompletionListener;
import com.txznet.audio.player.TXZAudioPlayer.OnErrorListener;
import com.txznet.audio.player.TXZAudioPlayer.OnPlayProgressListener;
import com.txznet.audio.player.TXZAudioPlayer.OnPreparedListener;
import com.txznet.audio.player.TXZAudioPlayer.OnSeekCompleteListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.dao.AlbumDBHelper;
import com.txznet.fm.dao.AudioDBHelper;
import com.txznet.fm.dao.HistoryAudioDBHelper;
import com.txznet.fm.dao.LocalAudioDBHelper;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.Constant.PlayMode;
import com.txznet.music.bean.req.ReqDataStats.Action;
import com.txznet.music.bean.req.ReqError;
import com.txznet.music.bean.req.ReqSearch;
import com.txznet.music.bean.req.ReqThirdSearch;
import com.txznet.music.bean.response.Album;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.factory.TxzAudioPlayerFactory;
import com.txznet.music.helper.RequestHelpe;
import com.txznet.music.receiver.HeadSetHelper;
import com.txznet.music.service.MyService;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.FileUtils;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.MyAsyncTask;
import com.txznet.music.utils.NetHelp;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.SyncCoreData;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.view.IMediaPlayer;
import com.txznet.txz.util.runnables.Runnable1;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

/**
 * 使用系统自带的MediaPlayer
 * 
 * @author telenewbie
 * @version 2016年2月25日
 */
public class MediaPlayerActivityEngine {
	private final static String TAGFILE = "[MUSIC][engine] [file] ";
	private int stats = prepareState;
	public static int prepareState = 0x11;// 17
	public static int startState = 0x12;
	public static int pauseState = 0x13;
	public boolean isBuffer = false;// 缓冲中

	public boolean mManuTouch = false;// 是否触摸进度条

	private boolean isManual = false;// 是否是手动的;

	private int currentPosition;// 当前播放的位置
	private Audio currentAudio;// 当前播放的歌曲
	private int currentPage;// 当前播放歌单的页码
	private long currentAlbum;// 当前专辑的ID，本地为0
	private String currentAlbumName;// 当前专辑的名称
	private float currentPercent = 0f;// 当前播放的进度
	private List<LocalBuffer> currentBuffers;

	private IMediaPlayer actView;
	private Random random;
	private List<Audio> audios;
	private TXZAudioPlayer audioPlayer;

	private AudioManager audioManager;
	private MyFocusListener focusListener;
	private AtomicBoolean isEnd = new AtomicBoolean(false);
	private AtomicBoolean isNeedPause = new AtomicBoolean(false);
	protected Audio nextAudio;// 下一首要播放什么音频。

	// private float mCurrentVolum;

	// //////////////////////////////////
	private static class Instance {
		public static MediaPlayerActivityEngine instance = new MediaPlayerActivityEngine();
	}

	public static MediaPlayerActivityEngine getInstance() {
		return Instance.instance;
	}

	private MediaPlayerActivityEngine() {
		audios = new ArrayList<Audio>();
		random = new Random();
		audioManager = (AudioManager) AppLogic.getApp().getSystemService(Context.AUDIO_SERVICE);
		focusListener = new MyFocusListener();
	}

	public int getStatus() {
		return stats;
	}

	/**
	 * 添加内容到尾部
	 * 
	 * @param audios
	 */
	public void addAudios(final List<Audio> audios,boolean isAdd) {
		if (CollectionUtils.isNotEmpty(audios)) {
			this.audios.addAll(audios);
		} else {
			if (isManual) {
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						ToastUtils.showShort(Constant.RS_VOICE_SPEAK_NODATA_TIPS);
					}
				}, 0);
			}
			return;
		}
		if (actView != null) {
			if (isAdd) {
				actView.notifyMusicListInfo(audios,isAdd);
			}else{
				actView.notifyMusicListInfo(this.audios,isAdd);
			}
//			actView.notifyAndLocation(this.audios.size() - audios.size(), false);
		}
		// XXX:如果是本地音乐和历史音乐应该不需要再添加一遍，但是声控播放会和本地音乐在一起，有问题
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				// 耗时的操作放在线程中完成
				AudioDBHelper.getInstance().saveOrUpdate(audios);
				// saveCurrentAudio();
			}
		}, 0);

	}

	private boolean isSameAudio = false;
	private boolean isReport = false;// 是否上报下一首播放什么得标志

	/**
	 * 设置新的数据源
	 * 
	 * @param audios
	 */
	public void setAudios(final List<Audio> audios, int index) {
		LogUtil.logd("mediaplayer:requestFocus:setAudios:begin");
		registerFocus();
		LogUtil.logd("mediaplayer:requestFocus:setAudios:end");
		this.audios.clear();
		if (audios != null) {
			addAudios(audios,false);
		}else{
			LogUtil.loge(TAG+"set audios "+(CollectionUtils.isEmpty(audios)?"0":audios.size()));
		}
		currentPosition = index < 0 ? 0 : index;
		if (CollectionUtils.isNotEmpty(audios)) {
			currentPage = audios.size() / Constant.PAGECOUNT;
			// 数据库中取专辑名称
			Album album = null;
			if (StringUtils.isNotEmpty(audios.get(0).getAlbumId())) {
				album = AlbumDBHelper.getInstance().findOne(null,
						AlbumDBHelper.TABLE_ID + " ==? and " + AlbumDBHelper.TABLE_SID + " ==? ",
						new String[] { audios.get(0).getAlbumId(), "" + audios.get(0).getSid() });
			}
			LogUtil.logd(TAG + "set list :" + album);

			setCurrentAlbum(album != null ? album.getId() : 0);
			setCurrentAlbumName(album != null ? album.getName() : "");

			if (!getIndex(audios, currentPosition).equals(currentAudio)) {
				isSameAudio = false;
				currentAudio = getIndex(audios, currentPosition);
				setStatus(prepareState);
			} else {
				isSameAudio = true;
				LogUtil.logd("the same  song：" + currentAudio.getName());
			}
		} else {
			currentPage = 1;
		}
	}

	/**
	 * 1 缓冲 2 播放 3 暂停
	 * 
	 * @param status
	 */
	private void setStatus(int status) {
		stats = status;
		sendStatusByCurrent();
	}

	/**
	 * 根据现在的广播发送广播
	 */
	public void sendStatusByCurrent() {
		// 直接发送广播断点
		if (currentAudio != null && stats == prepareState) {
			sendStatusBroadcast(1);

		} else if (stats == pauseState) {
			sendStatusBroadcast(3);

		} else if (stats == startState) {
			sendStatusBroadcast(2);
		} else {
			sendStatusBroadcast(4);
		}
	}

	private Audio getIndex(List<Audio> audios, int index) {
		if (index < 0) {
			index = 0;
		}
		if (audios != null && audios.size() > 0) {
			index = index > audios.size() ? 0 : index;
			return audios.get(index);
		}
		return null;
	}

	/**
	 * 正式播放之前的处理
	 */
	private void prestart() {
		isSameAudio = false;
		setStatus(prepareState);
//		registerFocus();
		int index = audios.lastIndexOf(currentAudio);// 播放歌曲出错，该歌曲在歌单中不存在
		if (audios.size() > 0 && index < 0) {
			currentAudio = getIndex(audios, 0);
		}
		try {
			// 发送广播
			Intent intent = new Intent("com.txznet.music.action.PLAY_AUDIO");
			intent.putExtra("artists", CollectionUtils.toString(currentAudio.getArrArtistName()));
			intent.putExtra("title", currentAudio.getName());
			GlobalContext.get().sendBroadcast(intent);
		} catch (Exception e) {
		}

		SyncCoreData.syncCurMusicModel();
		currentPosition = index < 0 ? 0 : index;
		if (audios.size() - 1 == currentPosition) {
			AppLogic.runOnBackGround(mRunnbleCheckPlayNearEnd, 1000);
		}
		currentPercent = 0;
		
		
		isEnd.set(false);
		currentBuffers = null;
		isReport = false;
		initView();
		showBufferButton();
		nextAudio = null;
		
		AppLogic.removeBackGroundCallback(canCancleRunnable);
		AppLogic.runOnBackGround(canCancleRunnable, 0);
	}

	/**
	 * 注册媒体焦点和音频焦点
	 */
	private void registerFocus() {
		isFocusLoss = false;
		setIsLostFocus(false);
//		LogUtil.logd("mediaplayer.registerfocus:prestart:begin");
		audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
//		LogUtil.logd("mediaplayer.registerfocus:prestart:end");
		HeadSetHelper.getInstance().open(GlobalContext.get());// 注册广播
	}

	Runnable canCancleRunnable = new Runnable() {

		@Override
		public void run() {
			if (currentAudio == null) {
				return;
			}
			LogUtil.logd("mediaplayer.startPlay:" + currentAudio.getName());
			if (checkAudio()) {
				prepareStart();
			}
		}
	};

	/**
	 * 检测该音频是否可行
	 */
	public boolean checkAudio() {
		if (null == currentAudio) {
			return false;
		}
		if (currentAudio.getSid() == Constant.LOCAL_MUSIC_TYPE) {// 该歌曲为本地歌曲
			File file = new File(currentAudio.getStrDownloadUrl());
			if (!file.exists()) {
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						ToastUtils.showShort("该文件不存在");
					}
				}, 0);
				refreshAudios();
				start(currentPosition);
				return false;
			}
		}
		return true;
	}

	/**
	 * 初始化界面数据
	 */
	private void initView() {
		LogUtil.logd("initView :actView=" + actView + ",currentAudio=" + currentAudio);
		if (actView != null) {
			actView.notifyMusicInfo(currentAudio, false);
			int value = 0;
			actView.setBufferProgress(null);
			try {
				if (null != currentAudio) {
					value = (int) (currentPercent * currentAudio.getDuration());
					actView.setFinishedProgress(currentPercent, value, currentAudio.getDuration());
					if (null != currentBuffers && !currentBuffers.isEmpty()) {
						actView.setBufferProgress(currentBuffers);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			showMode();
			showPlayOrPause();
			actView.notifyMusicListInfo(audios,false);
			actView.notifyAndLocation(currentPosition, true);
		}
		ObserverManage.getObserver().send(InfoMessage.NOTIFY_LOCAL_AUDIO, currentAudio);
	}

	/**
	 * 播放或暂停
	 */
	public void playOrPause() {
		LogUtil.logd(TAG + "play or pause");
		if (currentAudio == null) {
			LogUtil.logd("当前还未准备好音频");
			return;
		}
		isFocusLoss = false;
		setIsLostFocus(false);
		if (isSameAudio) {// 相同的歌曲则，不反应
			LogUtil.logd("this is same song");
			isSameAudio = !isSameAudio;
			return;
		}
		if (stats == prepareState) {
			prestart();
		} else if (stats == startState) {
			pauseState(true);
		} else if (stats == pauseState) {
			playState(true);
		}
	}

	Runnable nextRunnable = new Runnable() {

		@Override
		public void run() {
			AppLogic.removeBackGroundCallback(this);
			jumpToNext(true, true);
		}
	};
	Runnable playRunnable = new Runnable() {

		@Override
		public void run() {
			AppLogic.removeBackGroundCallback(this);
			prestart();
		}
	};
	Runnable lastRunnable = new Runnable() {

		@Override
		public void run() {
			jumpToNext(false, true);
		}
	};
	Runnable forceRunnable = new Runnable() {

		@Override
		public void run() {
			if (audioPlayer != null) {
				audioPlayer.forceNeedMoreData(true);
			}
			if (tempBuffer) {
				AppLogic.runOnSlowGround(this, 2000);
			}
		}
	};
	Runnable checkBufferRunnable = new Runnable() {

		@Override
		public void run() {
			if (stats == startState && !isFocusLoss) {
				LogUtil.logd(
						"audio : " + (currentAudio != null ? currentAudio.getName() : "null") + " checkBufferRunnable");
				tempBuffer = true;
				showBufferButton();
				AppLogic.runOnSlowGround(forceRunnable, 0);
			}
		}
	};

	/**
	 * 下一首
	 */
	public void next() {
		LogUtil.logd("mediaplayer:requestFocus:next:begin");
		registerFocus();
		LogUtil.logd("mediaplayer:requestFocus:next:end");
		AppLogic.removeBackGroundCallback(nextRunnable);
		AppLogic.runOnBackGround(nextRunnable, 0);
	}

	/**
	 * 上一首
	 */
	public void last() {
		LogUtil.logd("mediaplayer:requestFocus:last:begin");
		registerFocus();
		LogUtil.logd("mediaplayer:requestFocus:last:end");
		AppLogic.removeBackGroundCallback(lastRunnable);
		AppLogic.runOnBackGround(lastRunnable, 0);
	}

	int speakText = 0;

	/**
	 * 获取下一首歌的位置并播放
	 * 
	 * @param next
	 *            上一首/下一首
	 * @param manul
	 *            手动/声控
	 */
	private void jumpToNext(boolean next, boolean manul) {
		if (currentAudio == null) {
			if (speakText != 0) {
				TtsUtil.cancelSpeak(speakText);
			}
			//解决bug：TXZ-5198
//			readFromFile(true);
			if (manul) {
				speakText = TtsUtil.speakResource("RS_VOICE_SPEAK_AUDIO_LOADING", Constant.RS_VOICE_SPEAK_AUDIO_LOADING);
			}
			return;
		}
		// TODO:如果下一首nextAUdio有值得话，就播放nextAuido，为了保证播报一直（问题：被手动修改了）
		if (null != nextAudio) {
			currentPosition = audios.indexOf(nextAudio);
			currentAudio = nextAudio;
			prestart();
			return;
		}

		
		int playMode = SharedPreferencesUtils.getPlayMode();
		LogUtil.logd(TAG + "[Click]jump to next =" + next + ",manul=" + manul + ",playMode=" + playMode);
		if (!Utils.isSong(currentAudio.getSid())) {
			playMusicAtPosition(next ? ++currentPosition : --currentPosition);
			return;
		}

		if (playMode == Constant.MODESINGLECIRCLE) {
			if (manul) {
				currentPosition = next ? currentPosition + 1 : currentPosition - 1;
			}
			playMusicAtPosition(currentPosition);
		} else if (playMode == Constant.MODERANDOM) {
			int randomInt = random.nextInt(audios.size());
			if (currentPosition == randomInt) {
				currentPosition++;
			} else {
				currentPosition = randomInt;
			}
			playMusicAtPosition(currentPosition);
		} else {
			playMusicAtPosition(next ? ++currentPosition : --currentPosition);
			return;
		}
	}

	private Audio getWillPlayAudio() {
		int nextPosition = getNextPlayPosition(true, false);
		Audio nextAudio = null;
		if (nextPosition != -1) {
			nextAudio = getIndex(audios, nextPosition);
		}
		return nextAudio;
	}

	/**
	 * 获得即将播放的位置
	 * 
	 * @return
	 */
	private int getNextPlayPosition(boolean next, boolean manul) {
		int position = -1;
		int playMode = SharedPreferencesUtils.getPlayMode();
		LogUtil.logd("jump to next =" + next + ",manul=" + manul + ",playMode=" + playMode);
		if (!Utils.isSong(currentAudio.getSid())) {
			position = currentPosition + 1;
		} else if (playMode == Constant.MODESINGLECIRCLE) {
			if (manul) {
				position = next ? currentPosition + 1 : currentPosition - 1;
			} else {
				position = currentPosition;
			}
		} else if (playMode == Constant.MODERANDOM) {
			int randomInt = random.nextInt(audios.size());
			if (currentPosition == randomInt) {
				position = currentPosition + 1;
			} else {
				position = randomInt;
			}
		} else {
			position = next ? currentPosition + 1 : currentPosition - 1;
		}
		// 边界处理
		if (position < 0) {
			position = audios.size() - 1;
		} else if (position >= audios.size()) {
			position = 0;
		}
		if (CollectionUtils.isEmpty(audios)) {
			LogUtil.logd("no audios when you want  to play next");
			return -1;
		}
		return position;
	}

	/**
	 * 边界处理
	 * 
	 * @param songIndex
	 */
	private void playMusicAtPosition(int songIndex) {

		if (audios.size() > songIndex && songIndex >= 0) {
			currentPosition = songIndex;
		} else if (songIndex < 0) {
			currentPosition = audios.size() - 1;
		} else if (songIndex >= audios.size()) {
			currentPosition = 0;
		}
		if (CollectionUtils.isEmpty(audios)) {
			LogUtil.logd("no audios when you want  to play next");
			TtsUtil.speakResource("RS_VOICE_SPEAK_NEXTNOAUDIOS_TIPS", Constant.RS_VOICE_SPEAK_NEXTNOAUDIOS_TIPS);
			return;
		}
		currentAudio = getIndex(audios, currentPosition);
		prestart();
	}

	/**
	 * 切换模式
	 */
	public void changeMode() {
		changeMode((SharedPreferencesUtils.getPlayMode() + 1) % 3);
	}

	/**
	 * 切换到指定模式
	 * 
	 * @param mode
	 */
	public void changeMode(int mode) {
		LogUtil.logd("you are changing you mode to " + mode);
		if (currentAudio != null && mode != PlayMode.SEQUENCE.ordinal() && !Utils.isSong(currentAudio.getSid())) {
			TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_CANTSUPPORT_TIPS", Constant.RS_VOICE_SPEAK_CANTSUPPORT_TIPS,
					false, null);
			return;
		}
		nextAudio = null;
		SharedPreferencesUtils.setPlayMode(mode);
		showMode();
	}

	// //////////////////对内////////////////////

	/**
	 * 预处理路径
	 * 
	 * @param currentAudio
	 */
	private void prepareStart() {
		isFocusLoss = false;
		this.currentAudio.setAlbumName(currentAlbumName);
		if (SharedPreferencesUtils.getAudioSource() == Constant.HISTORY_TYPE
				|| SharedPreferencesUtils.getAudioSource() == Constant.LOCAL_MUSIC_TYPE) {
			// SongListFragment.getInstance().refreshPosition();// 观察着
			ObserverManage.getObserver().send(InfoMessage.REFRESH_LOCAL_POSITION);
		}
		if (StringUtils.isEmpty(currentAudio.getStrDownloadUrl())) {
			TtsUtil.speakResource("RS_VOICE_SPEAN_NOAUDIOFOUND_TIPS", Constant.RS_VOICE_SPEAN_NOAUDIOFOUND_TIPS);
			return;
		}
		if (currentAudio.getSid() == Constant.LOCAL_MUSIC_TYPE) {// 本地音乐
			// 判断文件是否存在
			File file = new File(currentAudio.getStrDownloadUrl());
			if (!file.exists()) {
				removeAudio(currentAudio);
				return;
			}
		}
		currentAudio.setCreateTime(SystemClock.elapsedRealtime());
		playIner();
	}

	// /////////////////////////////////////////////
	Runnable mRunnbleCheckPlayNearEnd = new Runnable() {
		@Override
		public void run() {
			AppLogic.removeBackGroundCallback(mRunnbleCheckPlayNearEnd);
			if (audioPlayer != null && currentPosition == audios.size() - 1) {
				if (currentAudio.getSid() == 0) {// 本地音乐
					LogUtil.logd("can't to search more data::currentAudio::" + currentAudio.toString());
					return;
				}
				if (currentAlbum < 5) {
					LogUtil.logd("currentAlbum::" + currentAlbum + ",no need to refresh more audios");
					return;
				}
				if (audios.size() < Constant.PAGECOUNT) {
					LogUtil.logd("currentSize::" + audios.size() + ",no need to refresh more audios");
					return;
				}

				if (!Utils.isSong(currentAudio.getSid())) {
				}
				searchListData(false);
				isEnd.set(false);
			}
		}
	};

	// ///////////////////////////////

	public String getCurrentAlbumName() {
		LogUtil.logd("get AlbumName=" + currentAlbumName);
		return currentAlbumName;
	}

	private void setCurrentAlbumName(String currentAlbumName) {
		LogUtil.logd("set AlbumName=" + currentAlbumName);
		this.currentAlbumName = currentAlbumName;
	}

	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}

	public Audio getCurrentAudio() {
		return currentAudio;
	}

	public float getCurrentPercent() {
		return currentPercent;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public long getCurrentAlbum() {
		return currentAlbum;
	}

	private void setCurrentAlbum(long currentAlbum) {
		this.currentAlbum = currentAlbum;
	}

	public List<Audio> getAudios() {
		return audios;
	}

	public boolean sholdShowLoading() {
		return CollectionUtils.isEmpty(this.audios);
	}

	public void setListener(IMediaPlayer actView) {
		LogUtil.logd("set actView:" + actView);
		this.actView = actView;
	}

	/**
	 * 之后使用观察者模式，进行通知
	 * 
	 * @param control
	 *            临时状态
	 */
	public void playState(final boolean control) {
		playState(control, false);
	}

	/**
	 * 
	 * @param control
	 * @param manual
	 *            是否手动，表示强制
	 */
	public void playState(final boolean control, boolean manual) {

		// 执行结果
		try {
			if (getStatus() == prepareState) {// 当前处于缓冲状态
				LogUtil.logd("mediaplayer:-38:" + getStatus());
				return;
			}

			setIsLostFocus(false);
			setStatus(startState);
			audioPlayer.start();
		} catch (Exception e) {
			setStatus(prepareState);
			LogUtil.logd(TAG + "[state]" + stats + ",exception=" + e.getMessage());
			playOrPause();
			return;
		}
		LogUtil.logd(TAG + "[state]" + stats + ",control=" + control + ",isBuffer" + isBuffer);
		playView(control);
	}

	/**
	 * 播放状态视图
	 */
	private void playView(boolean control) {
		// 改变状态
		if (control) {
			setBufferState(false);
			setIsLostFocus(false);
			HeadSetHelper.getInstance().open(GlobalContext.get());
			LogUtil.logd("mediaplayer:requestFocus:start:begin");
			audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			LogUtil.logd("mediaplayer:requestFocus:start:end");
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.inner.isPlaying",
					("" + true).getBytes(), null);
			LogUtil.logd("sendBroadcast play");
			SyncCoreData.syncCurMusicModel();

			isNeedPause.set(false);
		}

		if (isBuffer) {
			return;
		}
		// 显示状态
		sendStartIntent();
		if (stats == startState && actView != null) {
			actView.showPause();
		} else {
			LogUtil.logd(TAG + "[state]" + stats);
		}
	}

	private void setBufferState(Boolean isBuffer) {
		this.isBuffer = isBuffer;
		// Intent intent = new
		// Intent("com.txznet.music.action.PLAY_STATUS_BUFFER");
		// intent.putExtra("isBuffer", this.isBuffer);
		// GlobalContext.get().sendBroadcast(intent);

		if (actView != null) {
			actView.setSeekBarEnable(!isBuffer);
		}
	}

	/**
	 * 发送状态的广播
	 * 
	 * @param status
	 *            1(缓冲),2(播放),3(暂停),4(退出)
	 */
	private void sendStatusBroadcast(int status) {
		// 桌面组件调用该广播
		Intent intent = new Intent("com.txznet.music.action.PLAY_STATUS_CHANGE");
		intent.putExtra("status", status);
		GlobalContext.get().sendBroadcast(intent);
	}

	private void pauseState(final boolean control) {

		if (control) {
			LogUtil.logd("sendBroadcast pause");
			setBufferState(false);
			//修改暂停的时候不释放 媒体焦点,也释放音频焦点，否则别的播放器会获得并继续播放
//			HeadSetHelper.getInstance().close(GlobalContext.get());
//			audioManager.abandonAudioFocus(focusListener);
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.inner.isPlaying",
					("" + false).getBytes(), null);

			isFocusLoss = false;
		}
		// 发送广播

		sendPauseIntent();
		sendStatusBroadcast(3);
		LogUtil.logd(TAG + " player:" + audioPlayer + " stats:" + stats + " isFocusLoss:" + isFocusLoss + " control:"
				+ control);

		if (audioPlayer != null && stats == startState) {
			if (isFocusLoss == false || control) {
				setStatus(pauseState);
			}
			audioPlayer.pause();
			// 防止播放时被抢焦点后暂停了还检测播放进度
			AppLogic.removeSlowGroundCallback(checkBufferRunnable);
		}
		LogUtil.logd("playView::stats=" + stats + ",control=" + control + ",isBuffer=" + isBuffer);
		// if (control && stats == prepareState) {// 有人在缓冲中的时候，就去强制调用不播放的指令
		// isNeedPause.set(true);
		// }
		if (actView != null && !isBuffer) {
			actView.showPlay();
		}
	}

	/**
	 * 展示缓冲状态
	 */
	private void showBufferButton() {
		AppLogic.removeSlowGroundCallback(checkBufferRunnable);
		LogUtil.logd("showBufferButton");
		setBufferState(true);
		SyncCoreData.syncCurPlayerBufferingStatus();
		if (actView != null) {
			actView.showBufferButton();
		}
		sendPauseIntent();
		sendStatusBroadcast(1);
	}

	/**
	 * 重新进行播放
	 */
	Runnable replayRunnable = new Runnable() {

		@Override
		public void run() {
			AppLogic.removeBackGroundCallback(this);
			playIner();
			LogUtil.logd(TAG + "player create retry end");
		}
	};

	// TODO:从startp开始到这里耗时3s，待优化。
	private void playIner() {

		final Audio tempAudio = currentAudio;

		if (tempAudio == null) {
			LogUtil.logw(TAG + "[play] audio is null");
			return;
		}
		if (audioPlayer != null) {
			LogUtil.logd("mediaplayer:-38:release:begin");
			audioPlayer.release();
			LogUtil.logd("mediaplayer:-38:release:end");
			audioPlayer = null;
			LogUtil.logd(TAG + "player release end");
		}
		LogUtil.logd(TAG + "player create remote begin");
		audioPlayer = TxzAudioPlayerFactory.createPlayer(tempAudio);

		// audioPlayer = new RemoteMediaPlayer(tempAudio); // 多进程方案
		// 单进程方案
		// audioPlayer = SessionManager.getInstance().createPlayer(
		// new NetAudio(currentAudio)); //
		AppLogic.removeBackGroundCallback(replayRunnable);
		if (null == audioPlayer) {
			AppLogic.runOnBackGround(replayRunnable, 100);
			return;
		}
		// LogUtil.logd("currentSound=" + Constant.currentSound);
		// setVolume(Constant.currentSound);
		audioPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

			@Override
			public void onBufferingUpdate(TXZAudioPlayer ap, float percent) {
			}

			@Override
			public void onDownloading(TXZAudioPlayer ap, List<LocalBuffer> buffers) {
				if (null != actView) {
					currentBuffers = buffers;
					actView.setBufferProgress(buffers);
				}
			}
		});
		audioPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(TXZAudioPlayer ap) {
				if (null == tempAudio) {
					return;
				}
				LogUtil.logd(TAG + "[callback]audioPlayer::onCompletion::name::" + tempAudio.getName() + "#"
						+ audioPlayer.hashCode());
				tempAudio.setLastPlayTime(String.valueOf(0.0));
				AudioDBHelper.getInstance().updateAudioLastPlayTime(tempAudio);
				// if (SystemClock.elapsedRealtime()
				// - tempAudio.getCreateTime() < 5000) {
				// LogUtil.logd("onCompletion is error");
				// } else {
				jumpToNext(true, false);
				NetHelp.sendReportData(Action.NEXT_AUTO);
				// }
			}

		});
		audioPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(TXZAudioPlayer ap, final MediaError err) {
				if (tempAudio == null) {
					return true;
				}
				LogUtil.loge(TAG + "[callback]audio::" + tempAudio.getName() + ",error=" + err.toString() + ",stats="
						+ stats);
				if (stats == pauseState) {// 如果处于暂停状态。则不处理该错误
					return true;
				}
				//在丢失焦点的情况下回调错误
				if (isLostFocus) {
					setBufferState(false);
					pauseState(false);
					return true;
				}
				
				final int what = err.getErrCode();
				if (what == MediaError.ERR_FILE_NOT_EXIST /*
															 * || what ==
															 * MediaError.ERR_IO
															 */
						|| what == MediaError.ERR_GATE_WAY || what == MediaError.ERR_URI
						|| what == MediaError.ERR_FILE_FOBIDDEN || what == MediaError.ERR_BAD_REQUEST
				/* || what == MediaError.ERR_REQ_TIMEOUT */ || what == MediaError.ERR_REQ_SERVER) {
					AppLogic.runOnUiGround(new Runnable() {

						@Override
						public void run() {
							if (tempAudio != null) {
								ReqError error = new ReqError();
								error.albumId = currentAlbum;
								error.audioId = tempAudio.getId();
								error.rawText = MyService.rawText;
								error.sourceId = tempAudio.getSid();
								error.strName = tempAudio.getName();
								error.strUrl = TextUtils.equals("1", tempAudio.getDownloadType())
										? tempAudio.getStrProcessingUrl() : tempAudio.getStrDownloadUrl();
								error.artist = JsonHelper.toJson(tempAudio.getArrArtistName());
								error.errCode = what;

								NetHelp.sendRequest(Constant.GET_REPORT_ERROR, error);
								LogUtil.loge("[Audio]error:" + error.toString());
							}

							// TODO:上报数据给服务器
							ToastUtils.showShort(err.getErrHint());
						}
					}, 0);
					if (what != MediaError.ERR_URI) {
						next();
					}
					// else{
					// TODO:网络错误：恢复网络恢复播放
					// pauseState(true);
					// stop();
					// }
				}
				if (what == MediaError.ERR_SYS_PLAYER) {
					if (getStatus()==pauseState) {
						//不处理
						return true;
					}
					
					if (audioPlayer != null) {
						LogUtil.logd("mediaplayer:-38:release:begin");
						audioPlayer.release();
						LogUtil.logd("mediaplayer:-38:release:end");
						audioPlayer = null;
					}
					if (audios != null && audios.size() > 1) {// 如果超过1首歌曲则跳歌
						AppLogic.runOnBackGround(nextRunnable, 1000);
					} else {
						stop();
						judgeAudio();
					}
				} else if (what == MediaError.ERR_REMOTE_DISCONNECT) {
					// startNotPlay(tempAudio);
				} else if (MediaError.ERR_REQ_SERVER == what) {
					LogUtil.logd("error=" + MediaError.ERR_REQ_SERVER + "," + err.getErrHint());
				} else if (what == MediaError.ERR_REMOTE) {
					if (stats != pauseState) {
//						AppLogic.runOnBackGround(nextRunnable, 1000);
						jumpToNext(true, false);
					} else {
						setStatus(prepareState);
						playOrPause();// 重新播放
					}
				} else if (what == 0) {// 解析问题
					pauseState(true);
				} else if (what == MediaError.ERR_NULL_STATE) {
					setStatus(prepareState);
					playOrPause();// 重新播放
				} else if (MediaError.ERR_REQ_TIMEOUT == what) {
					AppLogic.runOnUiGround(new Runnable() {

						@Override
						public void run() {
							ToastUtils.showShort(err.getErrHint());
						}
					}, 0);
					pauseState(true);
					// forceReleasePlayer();
				} else if (MediaError.ERR_GET_AUDIO == what) {
					if (tempAudio != null) {
						tempAudio.setLastPlayTime(String.valueOf(currentPercent));
						prestart();
					}
				}
				return true;
			}
		});

		audioPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(TXZAudioPlayer ap) {
				setBufferState(false);
				SyncCoreData.syncCurPlayerBufferingStatus();
				LogUtil.logd(TAG + "[callback]onPrepared/" + isLostFocus + ",need pause :" + isNeedPause.get());
				if (!isNeedPause.get()) {
					initStartState(audioPlayer);
				} else {
					setStatus(pauseState);
					showPlayOrPause();
					LogUtil.logd("onPrepared play ,because someone force to pause player");
				}
				tempAudio.setAlbumName(currentAlbumName);
				saveToHistory(tempAudio);
				ObserverManage.getObserver().send(InfoMessage.PLAYER_CURRENT_AUDIO, tempAudio);
			}
		});
		audioPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {

			@Override
			public void onSeekComplete(final TXZAudioPlayer ap) {
				LogUtil.logd(TAG + "[callback]onSeekComplete");
				// 哪一台设备会在声控调用的时候，音乐自动调用seekComplete（迪恩捷？）
				if (!isLostFocus) {
					playState(true);
				}
			}
		});
		audioPlayer.setOnPlayProgressListener(new OnPlayProgressListener() {

			@Override
			public boolean onPlayProgress(final TXZAudioPlayer ap, float percent) {
				if (stats == prepareState) {
					return false;
				}
				if (null == tempAudio) {
					return false;
				}

				long duration = Math.abs(ap.getDuration());
				long value = (long) (percent * duration);
				if (isWakeupSeekto && percent < currentPercent && (currentPercent - percent) * duration > 2500) {
					LogUtil.loge(TAG + "wakeup sekkto error percent:" + percent + " seekTo:" + currentPercent);
					jumpToNext(true, false);
					return false;
				}

				if (ap.isBuffering() && stats != startState) {
					tempBuffer = true;
					LogUtil.logd("media player is buffering");
					showBufferButton();
					return false;
				} else if (tempBuffer) {
					tempBuffer = false;
					playView(true);
					sendStatusBroadcast(2);
				}
				if (stats == startState) {
					if (percent > 1.0F) {
						next();
						LogUtil.loge("percent::error::" + percent);
						percent = 1.0f;
					}
					// tempAudio.setDuration(duration);
					currentPercent = percent;
					if (null != actView && !mManuTouch) {
						actView.setFinishedProgress(percent, value, ap.getDuration());
					}
					if (value / 1000 % 5 == 0) {
						LogUtil.logd("save progress :value:" + value + " percent:" + percent + " duration:" + duration);
						tempAudio.setLastPlayTime(String.valueOf(percent));
						AppLogic.runOnBackGround(new Runnable1<Audio>(tempAudio) {

							@Override
							public void run() {
								AudioDBHelper.getInstance().updateAudioLastPlayTime(tempAudio);
							}
						}, 0);
					}

					if (percent > 0.95f && !isReport) {// 达到百分之九十
						// 上报下一首的数据
						isReport = true;
						nextAudio = getWillPlayAudio();
						SyncCoreData.syncNextMusicModel(nextAudio);
					}

					checkBufferStatus();

					// 发送广播：
					try {
						Intent intent = new Intent("com.txznet.music.Action.Progress");
						intent.putExtra("progress", value);
						intent.putExtra("duration", duration);
						intent.putExtra("percent", percent);
						GlobalContext.get().sendBroadcast(intent);
					} catch (Exception e) {
					}
				}
				// 检测所处的状态

				return false;
			}
		});
//		setIsLostFocus(false);
		if (audioPlayer != null) {
			// 同时上报缓冲状态
			SyncCoreData.syncCurPlayerBufferingStatus();
			// 抢音频焦点TODO:不知道在Play的时候我在抢一次焦点有什么影响
//			HeadSetHelper.getInstance().open(GlobalContext.get());
//			audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

			audioPlayer.prepareAsync();
		}
		// 假请求
		if (null != tempAudio && !Utils.isSong(tempAudio.getSid())) {// 不是歌曲
			ReqThirdSearch reqData = new ReqThirdSearch(tempAudio.getSid(), tempAudio.getId(), 0);
			NetHelp.sendRequest(Constant.GET_FAKE_SEARCH, reqData);
		}
	}

	private void checkBufferStatus() {
		AppLogic.removeSlowGroundCallback(checkBufferRunnable);
		AppLogic.runOnSlowGround(checkBufferRunnable, 2000);
	}

	private boolean tempBuffer = false;// 临时缓冲状态
	private boolean isWakeupSeekto = false;

	private void initStartState(TXZAudioPlayer ap) {
		LogUtil.logd(TAG + " initStartState ,isLostFocus:" + isLostFocus);
		if (null == currentAudio) {
			return;
		}
		Constant.setIsExit(false);
		setStatus(pauseState);// 状态的变化，表示可以对播放器进行操作,XXX,改成一个还未准备的状态
		String lastPlayTime = currentAudio.getLastPlayTime();
		int sid = currentAudio.getSid();
		if (StringUtils.isNotEmpty(lastPlayTime) && !"0.0".equals(lastPlayTime) && !Utils.isSong(sid)) {
			LogUtil.logd(TAG + "[start]seekTo::currentAudio::[" + currentAudio.getName() + "]" + lastPlayTime);
			currentPercent = Float.parseFloat(lastPlayTime);
			// mManuTouch = true;
			isWakeupSeekto = true;
			seekTo(currentPercent);
		} else {
			LogUtil.logd(TAG + "[start]play music no need seek. last play time:" + lastPlayTime + " sid:" + sid + " "
					+ Utils.isSong(sid));
			currentPercent = 0F;
			if (!isLostFocus) {
				playState(true);
			}
		}
		// 存文件
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				saveCurrentAudio();
			}
		}, 0);

	}

	/**
	 * 首页左上角的播放状态
	 */
	private void sendStartIntent() {
		if (SharedPreferencesUtils.isFirst()) {
			SharedPreferencesUtils.setIsFirst(false);
			LocalBroadcastManager.getInstance(GlobalContext.get())
					.sendBroadcast(new Intent(Constant.ACTION_SHOW_BUTTON));
		}
		SharedPreferencesUtils.setIsPlay(true);
		Intent intent = new Intent(Constant.ACTION_MUSIC_PAUSE);
		intent.putExtra(Constant.PARAM_PAUSE_OR_NOT, true);
		LocalBroadcastManager.getInstance(GlobalContext.get()).sendBroadcast(intent);
	}

	private void sendPauseIntent() {
		SharedPreferencesUtils.setIsPlay(false);
		Intent intent = new Intent(Constant.ACTION_MUSIC_PAUSE);
		intent.putExtra(Constant.PARAM_PAUSE_OR_NOT, false);
		LocalBroadcastManager.getInstance(GlobalContext.get()).sendBroadcast(intent);
	}

	private static final int HISTORY_MAX_COUNT = 100;// 历史数据最大存放多少

	private void saveToHistory(final Audio audio) {
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				if (null == audio) {
					return;
				}
				List<Audio> findAll = HistoryAudioDBHelper.getInstance().findAll(Audio.class,
						HistoryAudioDBHelper.TABLE_INDEXID + HistoryAudioDBHelper.ASC);
				if (CollectionUtils.isNotEmpty(findAll)) {
					if (Utils.isSong(audio.getSid())) {
						if (findAll.contains(audio)) {
							findAll.remove(audio);
						}
					} else {
						for (int i = 0; i < findAll.size(); i++) {
							if (TextUtils.equals(findAll.get(i).getAlbumId(), audio.getAlbumId())) {
								HistoryAudioDBHelper.getInstance().remove("albumId='" + audio.getAlbumId() + "'", null);
								findAll.remove(i);
								break;
							}
						}
					}
					if (findAll.size() >= HISTORY_MAX_COUNT) {
						// 删除表并且合并最多数据50条记录
						HistoryAudioDBHelper.getInstance().removeAll();
						findAll = findAll.subList(findAll.size() - HISTORY_MAX_COUNT + 1, findAll.size());// 变成49条记录
					}
				}
				if (Utils.isSong(audio.getSid())) {
					findAll.add(audio);
				} else {
					audio.setAlbumName(currentAlbumName);
					findAll.add(audio);
				}
				LogUtil.logd(TAG + "save to History " + audio.toString());
				HistoryAudioDBHelper.getInstance().saveOrUpdate(findAll);
				// if (Utils.isSong(audio.getSid())) {
				// HistoryAudioDBHelper.getInstance().remove(audio.getId(),
				// audio.getName());
				// } else {
				// audio.setAlbumName(currentAlbumName);
				// HistoryAudioDBHelper.getInstance().remove(
				// "albumId='" + audio.getAlbumId() + "'", null);
				// }
				// HistoryAudioDBHelper.getInstance().saveOrUpdate(audio);
			}
		}, 0);
	}

	private void showMode() {
		if (null != actView) {
			if (null != currentAudio && !Utils.isSong(currentAudio.getSid())) {
				return;
			}
			// actView.hiddenMode(false);
			if (SharedPreferencesUtils.getPlayMode() == Constant.MODESEQUENCE) {
				actView.showSequenceMode();
			} else if (SharedPreferencesUtils.getPlayMode() == Constant.MODESINGLECIRCLE) {
				actView.showSingleCircleMode();
			} else {
				actView.showRandomMode();
			}
		}
	}

	private void showPlayOrPause() {
		if (null != actView) {
			LogUtil.logd("mediaplayer:status:" + stats + ",isBuffer=" + isBuffer);
			if (isBuffer) {
				actView.showBufferButton();
				return;
			}
			actView.closeBufferButton();
			if (stats == startState) {
				actView.showPause();
				return;
			}
			actView.showPlay();
		}
	}

	public void init() {
		isFocusLoss = false;
		mManuTouch = false;
		if (currentAudio == null) {
			new MyAsyncTask<Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					if (currentAudio == null) {
						readFromFile(false);
					} else {
						if (audioPlayer == null) {
							playIner();
						}
					}
					return null;
				}

				protected void onPostExecute(Void result) {
					initView();
				};
			}.execute();
		} else {
			initView();
		}
	}

	/**
	 * 从文件中读取数据，并填充数据，不负责，展示
	 * 
	 * @param playNow
	 *            是否直接播放
	 */
	private void readFromFile(boolean playNow) {
		ObjectInputStream inputStream = null;
		try {
			inputStream = new ObjectInputStream(new FileInputStream(new File(Constant.SAVE_PATH + Constant.SAVE_FILE)));
			LogUtil.logd("readFromFile,1-query file:1" + inputStream);
			Audio audio = (Audio) inputStream.readObject();
			LogUtil.logd("readFromFile,1-query file:2" + audio);
			List<Audio> audios = (List<Audio>) inputStream.readObject();
			currentPage = (Integer) inputStream.readObject();
			currentPercent = (Float) inputStream.readObject();
			currentPosition = (Integer) inputStream.readObject();
			currentAlbumName = (String) inputStream.readObject();
			currentAlbum = inputStream.readLong();
			LogUtil.logd(TAG + "read from file:" + audio.getName() + " percent:" + currentPercent);
			setCurrentAlbum(currentAlbum);

			Audio findOne = AudioDBHelper.getInstance().findOne(Audio.class,
					AlbumDBHelper.TABLE_ID + " ==? and " + AlbumDBHelper.TABLE_SID + " ==? ",
					new String[] { String.valueOf(audio.getId()), String.valueOf(audio.getSid()) });
			if (findOne != null) {
				LogUtil.logd(TAG + "read from db:" + findOne.toString());
				audio = findOne;
			}

			if (CollectionUtils.isNotEmpty(audios)) {
				setAudios(audios, audios.indexOf(audio));
				currentAudio = audio;
				setStatus(prepareState);
				if (playNow) {
					playOrPause();
				}
			}
		} catch (Exception e) {
			LogUtil.loge("Exception=there have error from read file," + e.getMessage());
			currentAlbum = 0;
			judgeAudio();
		} catch (Throwable e) {
			LogUtil.loge("Throwable=there have error from read file," + e.getMessage());
			currentAlbum = 0;
			judgeAudio();
		} finally {
			FileUtils.closeQuietly(inputStream);
		}
	}
	
	public void play(final boolean force){
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				LogUtil.logd("MEDIA_PLAY[" + currentAudio + "] can't finish ,stats=" + stats);
				if (stats == startState || (isBuffer)) {
					return;
				}
				if (stats == prepareState && currentAudio != null&&!force) {
					ToastUtils.showShort("还未准备好资源");
					setIsLostFocus(false);//此时用户主动调用播放。应该将标志位置回。
					return;
				}
				if (null == currentAudio) {// 从本地音乐里面获取，并播放
					readFromFile(true);
					if (null == currentAudio) {
						List<Audio> localMusic = LocalAudioDBHelper.getInstance().findAll(Audio.class);
						// AndroidMediaLibrary.refreshSystemMedia(null);
						if (CollectionUtils.isNotEmpty(localMusic)) {
							SharedPreferencesUtils.setAudioSource(Constant.LOCAL_MUSIC_TYPE);
							setCurrentAlbum(Constant.LOCAL_MUSIC_TYPE);
							setAudios(localMusic, 0);
							playOrPause();
						} else {
							Constant.SoundSessionID = NetHelp.sendRequest(Constant.GET_SEARCH, new ReqSearch());
						}
					}
					LogUtil.logd("MEDIA_PLAY[" + currentAudio + "] read and play");
					// return;
				} else {
					playState(true, true);
				}
				initView();
			}
		}, 0);
	}

	public void play() {
		play(false);
	}

	public void play(int i) {
		SharedPreferencesUtils.setAudioSource(Constant.TYPE_SOUND);
		switch (i) {
		case Constant.QQINT:
			if (null != currentAudio && Utils.isSong(currentAudio.getSid())) {
				if (stats != startState) {// 播放不支持暂停
					playOrPause();
				} else {
					LogUtil.logd(TAG + "[PLAY]audioplayer can't support ,to " + stats + ",currentAudio is "
							+ currentAudio.getName());
				}
				return;
			}
			// 从文件里面读取
			// readFromFile(false);
			// if (currentAudio != null && Utils.isSong(currentAudio.getSid()))
			// {
			// setStatus(prepareState);
			// playOrPause();
			// return;
			// }
			List<Audio> localMusic = LocalAudioDBHelper.getInstance().findAll(Audio.class);
			if (CollectionUtils.isNotEmpty(localMusic)) {
				SharedPreferencesUtils.setAudioSource(Constant.LOCAL_MUSIC_TYPE);
				setCurrentAlbum(Constant.LOCAL_MUSIC_TYPE);
				setAudios(localMusic, 0);
				playOrPause();
			} else {
				TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_SEARCHDATA_TIPS", Constant.RS_VOICE_SPEAK_SEARCHDATA_TIPS,
						false, null);
				Constant.SoundSessionID = Constant.RecommandID = NetHelp.sendRequest(Constant.GET_SEARCH,
						new ReqSearch());
			}
			break;
		case Constant.KAOLAINT:

			if (null != currentAudio && !Utils.isSong(currentAudio.getSid())) {
				if (stats != startState) {// 播放不支持暂停
					playOrPause();
				} else {
					LogUtil.logd(
							"audioplayer can't support ,to " + stats + ",currentAudio is " + currentAudio.getName());
				}
				return;
			}
			// 播放历史数据最新数据
			List<Integer> sids = Utils.getfmSid();
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < sids.size(); j++) {
				if (j > 0) {
					sb.append(" or ");
				}
				sb.append("sid").append(" = ").append(sids.get(j));
			}

			List<Audio> query = HistoryAudioDBHelper.getInstance().findAll(Audio.class, sb.toString(), null, null);
			if (CollectionUtils.isEmpty(query)) {// 走电台推荐逻辑
				ReqSearch reqSearch = new ReqSearch();
				reqSearch.setCategory("电台");
				Constant.ManualSessionID = Constant.SoundSessionID = NetHelp.sendRequest(Constant.GET_SEARCH,
						reqSearch);
				LogUtil.logd(TAG + "search fm data  from net  ");
			} else {
				List<Audio> queryData = AudioDBHelper.getInstance().find(Audio.class, " albumId == ? ",
						new String[] { query.get(0).getAlbumId() });

				MediaPlayerActivityEngine.getInstance().setAudios(queryData, queryData.indexOf(query.get(0)));
				MediaPlayerActivityEngine.getInstance().setCurrentAlbumName(query.get(0).getAlbumName());
				MediaPlayerActivityEngine.getInstance().setCurrentAlbum(Utils.toLong(query.get(0).getAlbumId()));
				playOrPause();
				// MediaPlayerActivityEngine.getInstance().start(
				// queryData.get(queryData.indexOf(query.get(0))));
			}
			break;
		}
	}

	public void pause() {
		pauseState(true);
	}

	// 取消所有的内部runnable
	public void closeAllRunnable() {
		AppLogic.removeBackGroundCallback(nextRunnable);
		AppLogic.removeBackGroundCallback(playRunnable);
		AppLogic.removeBackGroundCallback(replayRunnable);
		AppLogic.removeSlowGroundCallback(forceRunnable);
	}

	/**
	 * 清空状态
	 */
	public void clearState() {
		stats = prepareState;
	}

	public boolean isPlaying() {
		if (stats == startState) {
			return true;
		}
		return false;
	}

	/**
	 * 搜索列表数据
	 */
	public void searchListData(boolean isManual) {
		try {
			this.isManual = isManual;
			if (SharedPreferencesUtils.getAudioSource() == Constant.LOCAL_MUSIC_TYPE) {
				actView.notifyMusicListInfo(Constant.RS_VOICE_SPEAK_NODATA_TIPS);
				return;
			}
			if (SharedPreferencesUtils.getAudioSource() == Constant.TYPE_SOUND
					&& StringUtils.isEmpty(currentAlbumName)) {
				actView.notifyMusicListInfo(Constant.RS_VOICE_SPEAK_NODATA_TIPS);
				return;
			}

			if (SharedPreferencesUtils.getAudioSource() == Constant.HISTORY_TYPE
					&& StringUtils.isEmpty(currentAlbumName)) {
				actView.notifyMusicListInfo(Constant.RS_VOICE_SPEAK_NODATA_TIPS);
				return;
			}
			// 一页不足就没有必要去搜索了
			if (audios.size() < Constant.PAGECOUNT) {
				actView.notifyMusicListInfo(Constant.RS_VOICE_SPEAK_NODATA_TIPS);
				return;
			}
			RequestHelpe.reqAudio(Long.parseLong(currentAudio.getAlbumId()), currentAudio.getSid(), currentPage + 1,
					currentAlbumName, Long.parseLong(
							(StringUtils.isNumeric(currentAudio.getStrCategoryId()) ? currentAudio.getStrCategoryId()
									: StringUtils.split(currentAudio.getStrCategoryId(), ",")[0])));
		} catch (Exception e) {
			LogUtil.logd("searchList error is " + e.getMessage());
			if (null != actView) {
				actView.notifyMusicListInfo(Constant.RS_VOICE_SPEAK_CLIENTERR_TIPS);
			}
		}
	}

	public void seekTo(float percent) {
		if (null != audioPlayer && audioPlayer.seekable() && (stats != prepareState) && isBuffer == false) {
			LogUtil.logd("seekTo::" + percent);
			// showBufferButton();
			audioPlayer.seekTo(percent);
		} else {
			LogUtil.logd("dont't support seek::,status::" + stats);
		}
	}

	private boolean isFocusLoss = false;

	private boolean isLostFocus = true;// 是否丢失焦点，bug：用于解决多个入口导致缓冲中焦点丢失，后多种声音同时出现

	public boolean getIsLostFocus() {
		return isLostFocus;
	}

	public void setIsLostFocus(boolean isLost) {
		LogUtil.logd("isLost:" + isLost);
		Thread.dumpStack();
		LogUtil.recordLogStack(3);

		isLostFocus = isLost;
	}

	private static final String TAG = "music:engind:: ";

	// private int lastFocusChange = 1;

	public class MyFocusListener implements OnAudioFocusChangeListener {
		@Override
		public void onAudioFocusChange(int focusChange) {
			LogUtil.logd("MyFocusListener::focusChange::" + focusChange);
			if (null == audioPlayer) {
				return;
			}
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
				audioPlayer.setVolume(1.0f);
				isFocusLoss = false;
				setIsLostFocus(false);
				LogUtil.logd("audioPlayer losfocus AUDIOFOCUS_GAIN:" + isFocusLoss);
				if (stats==startState) {
					playState(false);
				}
				break;
			case AudioManager.AUDIOFOCUS_LOSS:
				isFocusLoss = false;
				setIsLostFocus(true);
				boolean isRelease = SharedPreferencesUtils.isReleaseAudioFocus();
				LogUtil.logd("audioPlayer losfocus AUDIOFOCUS_LOSS:" + isRelease);
				pauseState(isRelease);
				if (isRelease) {
					HeadSetHelper.getInstance().close(GlobalContext.get());
				}

				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				setIsLostFocus(true);
				isFocusLoss = true;
				LogUtil.logd("audioPlayer losfocus AUDIOFOCUS_LOSS_TRANSIENT:" + isFocusLoss);
				pauseState(false);
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				LogUtil.logd("audioPlayer losfocus AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK volum::" + 0.2);
				audioPlayer.setVolume(0.2f);
				break;
			}
		}
	}

	/**
	 * 停止播放和缓冲下一个音频（可能通过网络获取新的音频列表）
	 */
	public void stopAndLoading() {
		stop();
		ObserverManage.getObserver().send(InfoMessage.PLAYER_LOADING);
	}

	public void stop() {
		LogUtil.logd(TAG+"invoke stop");
		setStatus(prepareState);
		audios.clear();
		currentAudio = null;

		if (audioPlayer != null) {
			LogUtil.logd("TAG:abandonAudioFocus :stop:begin");
			audioManager.abandonAudioFocus(focusListener);
			LogUtil.logd("TAG:abandonAudioFocus :stop:end");
			AppLogic.runOnBackGround(new Runnable() {
				
				@Override
				public void run() {
					if (audioPlayer != null) {
						LogUtil.logd("mediaplayer:-38:release:begin");
						audioPlayer.release();
						LogUtil.logd("mediaplayer:-38:release:end");
						audioPlayer = null;
					}
				}
			}, 0);
		}
		// TODO:阻止其他操作的继续执行
		AppLogic.removeBackGroundCallback(canCancleRunnable);
		SharedPreferencesUtils.setIsFirst(true);
		LocalBroadcastManager.getInstance(GlobalContext.get()).sendBroadcast(new Intent(Constant.ACTION_SHOW_BUTTON));
		SyncCoreData.syncCurMusicModel();
	}

	/**
	 * 退出播放列表
	 */
	public void exit() {
//		stats = prepareState;
		sendStatusBroadcast(4);

		audios.clear();
		currentAudio = null;
		if (audioPlayer != null) {
			LogUtil.logd("TAG:abandonAudioFocus :exit:begin");
			audioManager.abandonAudioFocus(focusListener);
			LogUtil.logd("TAG:abandonAudioFocus :exit:end");
			LogUtil.logd("mediaplayer:-38:release:begin");
			audioPlayer.release();
			LogUtil.logd("mediaplayer:-38:release:end");
			audioPlayer = null;
		}
		// TODO:阻止其他操作的继续执行
		AppLogic.removeBackGroundCallback(canCancleRunnable);
		SharedPreferencesUtils.setIsFirst(true);
		LocalBroadcastManager.getInstance(GlobalContext.get()).sendBroadcast(new Intent(Constant.ACTION_SHOW_BUTTON));

		SyncCoreData.syncCurMusicModel();
	}

	private void judgeAudio() {
		if (CollectionUtils.isEmpty(audios)) {
			stop();
			currentAudio = null;
			SyncCoreData.syncCurMusicModel();
			SharedPreferencesUtils.setIsFirst(true);
			LocalBroadcastManager.getInstance(GlobalContext.get())
					.sendBroadcast(new Intent(Constant.ACTION_SHOW_BUTTON));
			if (null != actView) {
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						actView.dismiss();
					}
				}, 0);
			}
			return;
		}
	}

	private void saveCurrentAudio() {
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				// 保存到文件
				File file = FileUtils.getFilePath(Constant.SAVE_PATH, Constant.SAVE_FILE);
				if (currentAudio == null || CollectionUtils.isEmpty(audios)) {
					SharedPreferencesUtils.setIsFirst(true);
				}
				// 写文件
				ObjectOutputStream objectOutputStream = null;
				try {
					LogUtil.logd(TAGFILE + "currentPage=" + currentPage + ",currentAlbumName =" + currentAlbumName
							+ ",currentPosition =" + currentPosition + ",currentAudio =" + currentAudio
							+ ",currentAlbum =" + currentAlbum);
					BufferedOutputStream bfBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
					objectOutputStream = new ObjectOutputStream(bfBufferedOutputStream);
					objectOutputStream.writeObject(currentAudio);
					objectOutputStream.writeObject(audios);
					objectOutputStream.writeObject(currentPage);
					objectOutputStream.writeObject(currentPercent);
					objectOutputStream.writeObject(currentPosition);
					objectOutputStream.writeObject(currentAlbumName);
					objectOutputStream.writeLong(currentAlbum);
					objectOutputStream.flush();
				} catch (Exception e) {
					LogUtil.loge(
							TAGFILE + "tele::createFile::" + Constant.SAVE_PATH + "errorMessage::" + e.getMessage());
					e.printStackTrace();
					SharedPreferencesUtils.setIsFirst(true);
				} finally {
					FileUtils.closeQuietly(objectOutputStream);
				}
			}
		}, 0);
	}

	public void removeAudio(Audio currentAudio) {
		if (SharedPreferencesUtils.getAudioSource() == Constant.LOCAL_MUSIC_TYPE) {
			LocalAudioDBHelper.getInstance().remove(currentAudio.getStrDownloadUrl());
		} else if (SharedPreferencesUtils.getAudioSource() == Constant.HISTORY_TYPE) {
			HistoryAudioDBHelper.getInstance().remove(currentAudio.getId(), currentAudio.getName());
		}
		deleteCurrentAudio();
		audios.remove(currentAudio);
		--currentPosition;
		// if (currentPosition < 0) {
		// currentPosition = 0;
		// }
		judgeAudio();
		if (null != actView) {
			if (CollectionUtils.isNotEmpty(audios)) {
				actView.notifyMusicListInfo(audios,false);
				// actView.notifyAndLocation(currentPosition, true);
			} else {
				setAudios(null, -1);
				return;
			}
		}
		if (this.currentAudio != null && this.currentAudio.equals(currentAudio)) {
			next();
		}
	}

	private void deleteCurrentAudio() {
		File filePath = FileUtils.getFilePath(Constant.SAVE_PATH, Constant.SAVE_FILE);
		if (filePath.exists()) {
			filePath.delete();
		}
	}

	public void start(int position) {
		try {
			LogUtil.logd("mediaplayer:requestFocus:startItem:begin");
			registerFocus();
			LogUtil.logd("mediaplayer:requestFocus:startItem:begin");
			// Audio audio = getIndex(audios,position);
			// if (audio.equals(currentAudio)) {
			// return;
			// }
			currentAudio = getIndex(audios, position);
			prestart();
		} catch (Exception e) {
			TtsUtil.speakResource("RS_VOICE_SPEAK_CLIENTERR_TIPS", Constant.RS_VOICE_SPEAK_CLIENTERR_TIPS);
			LogUtil.loge(e.getMessage());
		}
	}

	/**
	 * 网络超时
	 */
	public void showNetTimeOutError() {
		if (null != actView) {
			actView.showTimeOutView();
		}
	}

	/**
	 * 刷新掉播放器列表中的数据
	 */
	public void refreshAudios() {
		if (actView != null) {
			actView.showPop(false);
		}

		List<Audio> tempAudios = new ArrayList<Audio>();
		if (audios != null && audios.size() > 0) {
			for (int i = audios.size() - 1; i >= 0; i--) {
				Audio audio = getIndex(audios, i);
				if (audio.getSid() == Constant.LOCAL_MUSIC_TYPE) {
					File file = new File(audio.getStrDownloadUrl());
					if (file.exists()) {
						tempAudios.add(audio);
					}
				} else {
					tempAudios.add(audio);
				}
			}
			LogUtil.logd(TAG + "tempAudios size:" + tempAudios.size());
			int indexOf = 0;
			if (CollectionUtils.isEmpty(tempAudios)) {
				audios.clear();
				judgeAudio();
			} else {
				if (currentAudio != null && tempAudios.contains(currentAudio)) {
					indexOf = tempAudios.indexOf(currentAudio);
				}
				setAudios(tempAudios, indexOf);
			}
		}
	}

	/**
	 * 
	 * @param volume
	 *            0.0~1.0
	 */
	public void setVolume(float volume) {
		// mCurrentVolum = volume;
		Constant.currentSound = volume;
		if (audioPlayer != null) {
			audioPlayer.setVolume(volume);
		}
		if (actView != null) {
			actView.showSoundValueView();
		}
	}

	/**
	 * 重播歌曲
	 */
	public void replay(Audio audio) {
		if (audio != null) {
			prestart();
		}
	}

}

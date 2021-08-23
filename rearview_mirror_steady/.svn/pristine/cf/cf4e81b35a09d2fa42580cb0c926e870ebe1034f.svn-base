package com.txznet.txz.component.music.txz;

import com.txz.ui.event.UiEvent;
import com.txz.ui.music.UiMusic;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.txz.component.music.IMusic;
import com.txznet.txz.component.music.ITxzMedia;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.ui.win.record.RecorderWin;

/**
 * 支持新版音乐（2.0）与就版本兼容
 * 
 * @author ASUS User
 *
 */
public class AudioTxzImpl implements IMusic, ITxzMedia {

	public static boolean mTxzMusicToolisPlaying = false;
	public static boolean mTxzMusicToolisBuffering = false;
	public static String mTxzMusicToolCurrentMusicModel = "";
	public static boolean newVersion = false;

	public static final int MUSICFLAG = 1;

	public static String keyWord = "";

	public static boolean isNewVersion() {
		if (!PackageManager.getInstance().checkAppExist(ServiceManager.MUSIC))
			return false;
		// TODO read flag from package meta data远程调用
		return newVersion;
	}

	@Override
	public String getPackageName() {
		return ServiceManager.MUSIC;
	}

	@Override
	public boolean isPlaying() {
		return mTxzMusicToolisPlaying;
	}

	@Override
	public void start() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.play.inner", null, null);
	}

	@Override
	public void pause() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.pause", null, null);
	}

	@Override
	public void exit() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.exit", null, null);
	}

	@Override
	public void next() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.next", null, null);
	}

	@Override
	public void prev() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.prev", null, null);
	}

	@Override
	public void switchModeLoopAll() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.switchModeLoopAll", null, null);
	}

	@Override
	public void switchModeLoopOne() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.switchModeLoopOne", null, null);
	}

	@Override
	public void switchModeRandom() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.switchModeRandom", null, null);
	}

	@Override
	public void switchSong() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.switchSong", null, null);
	}

	@Override
	public void playRandom() {
//		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
//				UiMusic.SUBEVENT_MEDIA_REFRESH_MUSIC_LIST);
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.playRandom", null, null);
	}

	@Override
	public void playMusic(MusicModel musicModel) {
		LogUtil.logd("playMusic:::" + musicModel.toString());
		// keyWord=musicModel.toString();
		musicModel.setField(MUSICFLAG);
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.sound.find", musicModel.toString().getBytes(), null);
		RecorderWin.addCloseRunnable(new Runnable() {
			@Override
			public void run() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
						"music.sound.cancelfind", null, null);
			}
		});
	}

	@Override
	public MusicModel getCurrentMusicModel() {
		if (mTxzMusicToolCurrentMusicModel == null
				|| mTxzMusicToolCurrentMusicModel.length() == 0)
			return null;
		// JSONObject jsonObject =
		// JSON.parseObject(mTxzMusicToolCurrentMusicModel);

		return MusicModel.fromString(mTxzMusicToolCurrentMusicModel);
		// if (jsonObject.containsKey("name")) {
		// model.setTitle(jsonObject.getString("name"));
		// }
		// if (jsonObject.containsKey("album")) {
		// model.setAlbum(jsonObject.getString("album"));
		// }
		// if (jsonObject.containsKey("artistNames")) {
		// StringTokenizer stringTokenizer = new
		// StringTokenizer(jsonObject.getString("artistNames"), ",");
		// List<String> artists = new ArrayList<String>();
		// while (stringTokenizer.hasMoreElements()) {
		// artists.add((String) stringTokenizer.nextElement());
		// }
		// int size = artists.size();
		// model.setArtist(artists.toArray(new String[size]));
		// }
		// model.setKeywords(null);
		// //
		// model.setArtist(mTxzMusicToolCurrentMusicModel.msgMedia.rptStrArtist);
		// //
		// model.setKeywords(mTxzMusicToolCurrentMusicModel.msgMedia.rptStrKeywords);
		// return model;
	}

	@Override
	public void favourMusic() {
		String spk = NativeData.getResString("RS_VOICE_FUNCTION_UNSUPPORT");
		RecorderWin.speakText(spk, null);
		// MusicManager.getInstance().favouriteMusicFromVoice(true, true);
	}

	@Override
	public void unfavourMusic() {
		String spk = NativeData.getResString("RS_VOICE_FUNCTION_UNSUPPORT");
		RecorderWin.speakText(spk, null);
		// MusicManager.getInstance().favouriteMusicFromVoice(false, true);
	}

	@Override
	public void playFavourMusic() {
		String spk = NativeData.getResString("RS_VOICE_FUNCTION_UNSUPPORT");
		RecorderWin.speakText(spk, null);
		// ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
		// "music.play.favour", null, null);
		// JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
		// UiMusic.SUBEVENT_MEDIA_PLAY_FAVOURITE_LIST);
	}

	@Override
	public void setStatusListener(MusicToolStatusListener listener) {
		JNIHelper
				.loge("play music source error, not support play by this interface");
	}

	@Override
	public boolean isBuffering() {
		return mTxzMusicToolisBuffering;
	}

	@Override
	public void cancelRequest() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.sound.cancelfind", null, null);
	}

	@Override
	public void openApp() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.open", null, null);
	}

	@Override
	public void openAndPlay() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.open.play", null, null);
	}
}

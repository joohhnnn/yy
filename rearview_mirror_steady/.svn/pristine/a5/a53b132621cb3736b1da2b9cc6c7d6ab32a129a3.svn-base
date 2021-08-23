package com.txznet.txz.component.music.txz;

import android.content.pm.PackageInfo;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.txz.component.music.BroadcastMusicTool;
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
public class AudioTxzImpl extends BroadcastMusicTool implements ITxzMedia {

	public static boolean mTxzMusicToolisPlaying = false;
	public static boolean mTxzMusicToolisBuffering = false;
	public static String mTxzMusicToolCurrentMusicModel = "";
	private static boolean newVersion = true;

	public static final int MUSICFLAG = 1;

	public static String keyWord = "";

	private static final String GET_APP_VERSION = "music.app.version";
	private static String mMusicVersion = "";// 音乐的版本

	public static String getMusicVersion() {
		if (TextUtils.isEmpty(mMusicVersion)) {
			PackageInfo apkInfo = PackageManager.getInstance().getApkInfo(ServiceManager.MUSIC);
			if (apkInfo!=null&&apkInfo.versionCode>=300) {//3.0.0版本开始支持
				mMusicVersion=String.valueOf(apkInfo.versionCode);
			}
		}
		JNIHelper.logd(MusicManager.TAG + "getMusicVersion " + mMusicVersion);
		return mMusicVersion;
	}

	public static void setMusicVersion(String musicVersion) {
		JNIHelper.logd(MusicManager.TAG+"setMusicVersion:"+musicVersion);
		if (com.txznet.comm.util.StringUtils.isNotEmpty(musicVersion)) {
			AudioTxzImpl.setVersion(true);
		}
		mMusicVersion = musicVersion;
	}

	public static void setVersion(boolean isNewVersion) {
		JNIHelper.logd(MusicManager.TAG + /*"setMusicVersion "*/ "setIsNewVersion"+ isNewVersion);
		newVersion = isNewVersion;
	}

	public static boolean isNewVersion() {
		if (!newVersion) {
			if (!PackageManager.getInstance().checkAppExist(ServiceManager.MUSIC)) {
				setVersion(false);
			}else{
				PackageInfo apkInfo = PackageManager.getInstance().getApkInfo(ServiceManager.MUSIC);
				if (apkInfo != null && apkInfo.versionCode >= 150) {// 1.5.0版本开始支持
					setVersion(true);
				}
			}
		}
		JNIHelper.logd(MusicManager.TAG + "getIsNewVersion " + newVersion);
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
		super.start();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.play.inner", null, null);
	}

	@Override
	public void pause() {
		super.pause();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.pause", null, null);
	}

	@Override
	public void exit() {
		super.exit();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.exit", null, null);
	}

	@Override
	public void exitImmediately() {
		super.exitImmediately();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.exit", null, null);
	}

	@Override
	public void next() {
		super.next();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.next", null, null);
	}

	@Override
	public void prev() {
		super.prev();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.prev", null, null);
	}

	@Override
	public void switchModeLoopAll() {
		super.switchModeLoopAll();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.switchModeLoopAll", null, null);
	}

	@Override
	public void switchModeLoopOne() {
		super.switchModeLoopOne();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.switchModeLoopOne", null, null);
	}

	@Override
	public void switchModeRandom() {
		super.switchModeRandom();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.switchModeRandom", null, null);
	}

	@Override
	public void switchSong() {
		super.switchSong();
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.switchSong", null, null);
	}

	@Override
	public void playRandom() {
		super.playRandom();
		// JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
		// UiMusic.SUBEVENT_MEDIA_REFRESH_MUSIC_LIST);
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.playRandom", null, null);
	}

	@Override
	public void playMusic(MusicModel musicModel) {
		super.playMusic(musicModel);
		LogUtil.logd(MusicManager.TAG+"playMusic:::" + musicModel.toString());
		// keyWord=musicModel.toString();
		musicModel.setField(MUSICFLAG);
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.sound.find",
				musicModel.toString().getBytes(), null);
		RecorderWin.addCloseRunnable(new Runnable() {
			@Override
			public void run() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.sound.cancelfind", null, null);
			}
		});
	}

	@Override
	public MusicModel getCurrentMusicModel() {
		if (mTxzMusicToolCurrentMusicModel == null || mTxzMusicToolCurrentMusicModel.length() == 0)
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
		if (com.txznet.comm.util.StringUtils.isNotEmpty(getMusicVersion())) {
			JSONObject json = new JSONObject();
			json.put("audio", mTxzMusicToolCurrentMusicModel);
			json.put("favour", "true");
			ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.updateFavour",
					json.toJSONString().getBytes(), null);
		} else {
			String spk = NativeData.getResString("RS_VOICE_FUNCTION_UNSUPPORT");
			RecorderWin.speakText(spk, null);
		}

	}

	@Override
	public void unfavourMusic() {
		if (com.txznet.comm.util.StringUtils.isNotEmpty(getMusicVersion())) {
			JSONObject json = new JSONObject();
			json.put("audio", mTxzMusicToolCurrentMusicModel);
			json.put("favour", "false");
			ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.updateFavour",
					json.toJSONString().getBytes(), null);
		} else {
			String spk = NativeData.getResString("RS_VOICE_FUNCTION_UNSUPPORT");
			RecorderWin.speakText(spk, null);
		}

	}

	@Override
	public void playFavourMusic() {
		if (com.txznet.comm.util.StringUtils.isNotEmpty(getMusicVersion())) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.play.favour", null, null);
		} else {
			String spk = NativeData.getResString("RS_VOICE_FUNCTION_UNSUPPORT");
			RecorderWin.speakText(spk, null);
		}
	}

	@Override
	public void setStatusListener(MusicToolStatusListener listener) {
		JNIHelper.loge("play music source error, not support play by this interface");
	}

	@Override
	public boolean isBuffering() {
		return mTxzMusicToolisBuffering;
	}

	@Override
	public void cancelRequest() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.sound.cancelfind", null, null);
	}

	@Override
	public void openApp() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.open", null, null);
	}

	@Override
	public void openAndPlay() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.open.play", null, null);
	}

	@Override
	public void play() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.play", null, null);

	}

	public static void getAppVersion() {
		sendInvoke("music.get.version");
		getNewAppVersionInvoke(GET_APP_VERSION);
	}

	private static void sendInvoke(String cmd) {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.get.version", null, new GetDataCallback() {
			@Override
			public void onGetInvokeResponse(ServiceData data) {

				if (null != data) {
					if (data.getBoolean() != null) {
						AudioTxzImpl.setVersion(data.getBoolean());
					}
				} else {
					AudioTxzImpl.setVersion(false);
				}
				LogUtil.logd(MusicManager.TAG + "AudioTxzImpl.newVersion:" + AudioTxzImpl.isNewVersion());
			}
        }, 30 * 1000);
    }

	private static void getNewAppVersionInvoke(String cmd) {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, GET_APP_VERSION, null, new GetDataCallback() {
			@Override
			public void onGetInvokeResponse(ServiceData data) {

				if (null != data) {
					AudioTxzImpl.setMusicVersion(data.getString());
				} else {
					AudioTxzImpl.setMusicVersion("");
				}
				LogUtil.logd(MusicManager.TAG + "AudioTxzImpl.musicVersion:" + AudioTxzImpl.getMusicVersion());
			}
        }, 30 * 1000);
    }

	@Override
	public void hateAudio() {
		if (com.txznet.comm.util.StringUtils.isNotEmpty(getMusicVersion())) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.hate.audio", null, null);
		} else {
			String spk = NativeData.getResString("RS_VOICE_FUNCTION_UNSUPPORT");
			RecorderWin.speakText(spk, null);
		}
	}

	@Override
	public void switchPlayModeToOnce() {
		if (com.txznet.comm.util.StringUtils.isNotEmpty(getMusicVersion())) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.switchModeLoopOnce", null, null);
		} else {
			String spk = NativeData.getResString("RS_VOICE_FUNCTION_UNSUPPORT");
			RecorderWin.speakText(spk, null);
		}
	}

	@Override
	public void addSubscribe() {
		if (com.txznet.comm.util.StringUtils.isNotEmpty(getMusicVersion())) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.addSubscribe", null, null);
		} else {
			String spk = NativeData.getResString("RS_VOICE_FUNCTION_UNSUPPORT");
			RecorderWin.speakText(spk, null);
		}
	}

	@Override
	public boolean supportRequestHistory() {
		ServiceManager.ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(ServiceManager.MUSIC, "music.history.support", null);
		if (serviceData == null) {
			return false;
		}
		byte[] result = serviceData.getBytes();
		if (result != null) {
			try {
				return Boolean.parseBoolean(new String(result));
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	@Override
	public void requestHistory(String type) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("type", type);
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.sound.history.find", jsonBuilder.toBytes(), null);
		RecorderWin.addCloseRunnable(new Runnable() {
			@Override
			public void run() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.sound.history.cancelfind", null, null);
			}
		});
	}

}

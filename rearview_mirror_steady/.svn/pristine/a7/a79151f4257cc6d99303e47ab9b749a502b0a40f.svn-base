package com.txznet.txz.module.music;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.txz.component.media.base.IMediaTool;
import com.txznet.txz.component.music.ITxzMedia;
import com.txznet.txz.component.music.txz.MusicTongTing;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.ui.win.record.RecorderWin;

public class TongtingManager {
	// ##创建一个单例类##
	private volatile static TongtingManager singleton;

	private TongtingManager() {
	}

	public static TongtingManager getInstance() {
		if (singleton == null) {
			synchronized (TongtingManager.class) {
				if (singleton == null) {
					singleton = new TongtingManager();
				}
			}
		}
		return singleton;
	}

	public IMediaTool getTongTingMusicTool() {
		return MusicTongTing.getInstance();
	}

	public byte[] preInvoke(final String packageName, String command, byte[] data) {
		JNIHelper.logd(MusicManager.TAG + "receiver:tongting:command:" + command + ",from:" + packageName);
		final IMediaTool musicTool = getTongTingMusicTool();
		if (musicTool == null)
			return null;

		// 同行者音乐远程接口实现
		if (command.equals("play")) {
			musicTool.open(true);
			return null;
		}
//		if (command.equals("cont")) {
//			if (musicTool instanceof MusicKuwoImpl) {
//				((MusicKuwoImpl) musicTool).continuePlay();
//			}
//			return null;
//		}
		// 同行者音乐远程接口实现
		if (command.equals("play.extra")) {
			((ITxzMedia) musicTool).play();
			return null;
		}
		if (command.equals("pause")) {
			musicTool.pause();
			return null;
		}
		if (command.equals("exit")) {
			musicTool.pause();
			musicTool.exit();
			return null;
		}
		if ("exitAllMusicToolImmediately".equals(command)) {
			JNIHelper.logd("exitAllMusicToolImmediately begin");
			musicTool.exit();
			JNIHelper.logd("exitAllMusicToolImmediately end");
			return null;
		}
		if (command.equals("next")) {
			musicTool.next();
			return null;
		}
		if (command.equals("prev")) {
			musicTool.prev();
			return null;
		}
		if (command.equals("playFavourMusic")) {
			musicTool.playCollection();
			return null;
		}
		if (command.equals("favourMusic")) {
			musicTool.collect();
			return null;
		}
		if (command.equals("unfavourMusic")) {
			musicTool.unCollect();
			return null;
		}
		if (command.equals("switchModeLoopAll")) {
			musicTool.switchLoopMode(IMediaTool.LOOP_MODE.SEQUENTIAL);
			return null;
		}
		if (command.equals("addSubscribe")) {
			if (musicTool.supportSubscribe()) {
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_ADD_SUBSCRIBE_MUSIC"), new Runnable() {
					@Override
					public void run() {
						musicTool.subscribe();
					}
				});
			} else {
				RecorderWin.speakText(NativeData.getResString("RS_VOICE_FUNCTION_UNSUPPORT"), null);
			}
			return null;
		}
		if (command.equals("switchModeLoopOne")) {
			musicTool.switchLoopMode(IMediaTool.LOOP_MODE.SINGLE_LOOP);
			return null;
		}
		if (command.equals("switchModeRandom")) {
			musicTool.switchLoopMode(IMediaTool.LOOP_MODE.SHUFFLE);
			return null;
		}
		if (command.equals("switchSong")) {
			musicTool.next();
			return null;
		}
		if (command.equals("playRandom")) {
			musicTool.next();
			return null;
		}

		// 同步播放状态获取
		if (command.equals("isPlaying")) {
			/*boolean isPlaying = false;
			ServiceManager.ServiceData sendInvokeSync = ServiceManager.getInstance().sendInvokeSync(ServiceManager.MUSIC, "music."+command,
					data);
			if (sendInvokeSync != null) {
				if (sendInvokeSync.getBoolean() != null) {
					isPlaying = sendInvokeSync.getBoolean();
				}
			}*/
			boolean isPlaying = IMediaTool.PLAYER_STATUS.PLAYING == musicTool.getStatus();
			return String.valueOf(isPlaying).getBytes();
		}
		// 同步缓冲状态获取
		if (command.equals("isBuffering")) {
			//return String.valueOf(musicTool.isBuffering()).getBytes();
			boolean isBuffering = IMediaTool.PLAYER_STATUS.BUFFERING == musicTool.getStatus();
			return String.valueOf(isBuffering).getBytes();
		}
		if (command.equals("getCurrentMusicModel")) {
			/*if (musicTool != null && musicTool.getCurrentMusicModel() != null) {
				return musicTool.getCurrentMusicModel().toString().getBytes();
			}*/
			return null;
		}
		if (command.equals("isShowUI")) {
			boolean isShowUI = false;
			ServiceManager.ServiceData sendInvokeSync = ServiceManager.getInstance().sendInvokeSync(ServiceManager.MUSIC, "music."+command,
					data);
			if (sendInvokeSync != null) {
				if (sendInvokeSync.getBoolean() != null) {
					isShowUI = sendInvokeSync.getBoolean();
				}
			}
			return String.valueOf(isShowUI).getBytes();
		}
		return null;
	}
}

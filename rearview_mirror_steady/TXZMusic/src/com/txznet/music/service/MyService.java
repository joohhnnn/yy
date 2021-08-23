package com.txznet.music.service;

import java.util.HashSet;
import java.util.Set;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.txz.ui.music.UiMusic.MediaCategoryList;
import com.txz.ui.music.UiMusic.MediaList;
import com.txz.ui.music.UiMusic.MediaModel;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceHandler;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;

import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;

import com.txznet.music.service.MusicService.PlayMode;
import com.txznet.music.ui.MainActivity;
import com.txznet.txz.service.IService;

public class MyService extends Service {
	public class SampleBinder extends IService.Stub {
		@Override
		public byte[] sendInvoke(String packageName, String command, byte[] data) throws RemoteException {

			// LogUtil.logd("[MODULE="
			// + GlobalContext.get().getApplicationInfo().packageName
			// + ",FROM=" + packageName + ",CMD=" + command +
			// "] do invoke data:"
			// + data + ". ");

			byte[] bytes = ServiceHandler.preInvoke(packageName, command, data);
			if (command.startsWith("music.")) {
				return invokeMusic(packageName, command.substring("music.".length()), data);
			}
			return bytes;
		}
	}

	private byte[] invokeMusic(final String packageName, String command, byte[] data) {

		// ////////////////////////////////////////////////////////////////////////////////////////////////////

		if (command.equals("isPlaying")) {
			return ("" + MusicService.getInstance().isPlaying()).getBytes();
		}
		if (command.equals("isBufferProccessing")) {
			return ("" + MusicService.getInstance().isBufferProccessing()).getBytes();
		}
		if (command.equals("getProgress")) {
			try {
				return ("" + ((float) MusicService.getInstance().getCurrentPosition()) / MusicService.getInstance().getCurrentDuration()).getBytes();
			} catch (Exception e) {
				return "0".getBytes();
			}
		}
		if (command.equals("getPlayMode")) {
			return MusicService.getInstance().getPlayModeString().getBytes();
		}
		if (command.equals("getCurrentMusicIndex")) {
			return ("" + MusicService.getInstance().getCurIndex()).getBytes();
		}
		if (command.equals("getMusicList")) {
			MediaList lst = MusicService.getInstance().getMediaList();
			return MessageNano.toByteArray(lst);
		}

		// ////////////////////////////////////////////////////////////////////////////////////////////////////

		if (command.equals("update.syncMediaCategoryList")) {
			try {
				MediaCategoryList mMediaCategoryList = MediaCategoryList.parseFrom(data);
				MusicService.getInstance().syncMediaCategoryList(mMediaCategoryList);
			} catch (InvalidProtocolBufferNanoException e) {
				LogUtil.loge("MediaCategoryList parse error!");
			}
			return null;
		}

		if (command.equals("update.setMediaList")) {
			try {
				MediaList m = MediaList.parseFrom(data);
				MusicService.getInstance().setMediaList(m, false);
			} catch (Exception e) {
				LogUtil.loge("parse MediaList error!");
			}
			return null;
		}
		if (command.equals("update.playMediaList")) {
			try {
				MediaList m = MediaList.parseFrom(data);
				MusicService.getInstance().setMediaList(m, true);
			} catch (Exception e) {
				LogUtil.loge("parse MediaList error!");
			}
			return null;
		}
		if (command.equals("update.syncMediaList")) {
			try {
				MediaList m = MediaList.parseFrom(data);
				MusicService.getInstance().syncMediaList(m);
			} catch (Exception e) {
				LogUtil.loge("parse MediaList error!");
			}
			return null;
		}
		if (command.equals("update.appendMediaList")) {
			try {
				MediaList m = MediaList.parseFrom(data);
				MusicService.getInstance().appendMediaList(m);
			} catch (Exception e) {
				LogUtil.loge("parse MediaList error!");
			}
			return null;
		}

		// ////////////////////////////////////////////////////////////////////////////////////////////////////

		if (command.equals("open")) {
			Intent it = new Intent(GlobalContext.get(), MainActivity.class);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				GlobalContext.get().startActivity(it);
			} catch (Exception e) {
				LogUtil.loge("open mainactivity error!");
			}
			return null;
		}

		if (command.equals("play")) {
			MusicService.getInstance().resumePlay();
			return null;
		}
		if (command.equals("pause")) {
			MusicService.getInstance().pausePlay();
			return null;
		}
		if (command.equals("playIndex")) {
			try {
				MusicService.getInstance().playMusic(true, Integer.parseInt(new String(data)), 0);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("favourIndex")) {
			try {
				JSONBuilder json = new JSONBuilder(data);
				MusicService.getInstance().favouriteMusic(MusicService.getInstance().getMediaList().rptMediaItem[json.getVal("index", Integer.class, 0)].msgMedia,
						json.getVal("favour", Boolean.class, false));
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("deleteIndex")) {
			try {
				JSONBuilder json = new JSONBuilder(data);
				MusicService.getInstance().deleteMusic(json.getVal("index", Integer.class, -1), json.getVal("deleteFile", Boolean.class, false));
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("exit")) {
			MusicService.getInstance().pausePlay();
			MainActivity.finishAll();
			return null;
		}
		if (command.equals("prev")) {
			MusicService.getInstance().playPrev(true);
			return null;
		}
		if (command.equals("next")) {
			MusicService.getInstance().playNext(true);
			return null;
		}
		if (command.equals("switchSong")) {
			MusicService.getInstance().playRandom(true);
			return null;
		}
		if (command.equals("switchModeLoopAll")) {
			MusicService.getInstance().setPlayMode(PlayMode.PLAY_MODE_LOOP_ALL);
			return null;
		}
		if (command.equals("switchModeLoopOne")) {
			MusicService.getInstance().setPlayMode(PlayMode.PLAY_MODE_LOOP_SINGLE);
			return null;
		}
		if (command.equals("switchModeRandom")) {
			MusicService.getInstance().setPlayMode(PlayMode.PLAY_MODE_RANDOM);
			return null;
		}

		if (command.equals("notifyDownloadFinish")) {
			try {
				MediaModel model = MediaModel.parseFrom(data);
				MusicService.getInstance().updateMediaModelUrl(model);
			} catch (Exception e) {
			}
			return null;
		}

		if (command.equals("updateFavour")) {
			try {
				MediaModel model = MediaModel.parseFrom(data);
				MusicService.getInstance().updateMediaModelFavour(model);
			} catch (Exception e) {
			}
			return null;
		}

		// ////////////////////////////////////////////////////////////////////////////////////////////////////

		if (command.equals("status.subscrib")) {
			AppLogic.runOnBackGround(new Runnable() {
				@Override
				public void run() {
					synchronized (mSubscribService) {
						mSubscribService.add(packageName);
					}
				}
			}, 0);
			return null;
		}

		// ////////////////////////////////////////////////////////////////////////////////////////////////////

		if (command.equals("notifyMediaServerPort")) {
			try {
				MusicService.getInstance().mLocalMediaServerPort = Integer.parseInt(new String(data));
				LogUtil.logd("update local media server port: " + MusicService.getInstance().mLocalMediaServerPort);
			} catch (Exception e) {
			}
			return null;
		}

		return null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new SampleBinder();
	}

	private static Set<String> mSubscribService = new HashSet<String>();

	public static void broadcastSubscribService(String command, byte[] data) {
		synchronized (mSubscribService) {
			for (String serviceName : mSubscribService) {
				ServiceManager.getInstance().sendInvoke(serviceName, command, data, null);
			}
		}
	}

}

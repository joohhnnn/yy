package com.txznet.music.receiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.pinyin4j.PinyinHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.fm.dao.LocalAudioDBHelper;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.fragment.SongListFragment;
import com.txznet.music.fragment.logic.LocalLogic;
import com.txznet.music.utils.ArrayUtils;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.FileUtils;
import com.txznet.music.utils.PinYinUtil;
import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.TXZMusicManager.MusicModel;

/**
 * 音乐管理器，负责音乐逻辑处理及事件处理
 * 
 * @author bihongpi
 *
 */
public class MusicManager {

	static MusicManager sModuleInstance = new MusicManager();

	// public static Runnable mRunnableRefreshMediaList = new Runnable() {
	// @Override
	// public void run() {
	// // 暂停音乐
	// AppLogic.removeBackGroundCallback(mRunnableRefreshMediaList);
	// List<Audio> refreshSystemMedia = AndroidMediaLibrary
	// .refreshSystemMedia();
	//
	// List<Audio> queryLocalAudios = LocalAudioDBHelper.getInstance()
	// .findAll(Audio.class);
	// if (null != queryLocalAudios) {
	// for (int i = queryLocalAudios.size() - 1; i >= 0; i--) {
	// if (!FileUtils.isExist(queryLocalAudios.get(i)
	// .getStrDownloadUrl())) {
	// LocalAudioDBHelper.getInstance().remove(
	// queryLocalAudios.get(i).getId());
	// queryLocalAudios.remove(i);
	// }
	// }
	// }
	// for (Audio audio : refreshSystemMedia) {
	// audio.setPinyin(PinYinUtil.getPinYin(audio.getName()));
	// }
	// LocalAudioDBHelper.getInstance().saveOrUpdate(refreshSystemMedia);
	//
	// // 同步
	// List<Audio> resultAudios = LocalAudioDBHelper.getInstance()
	// .findAll(Audio.class);
	// List<MusicModel> musics = new ArrayList<TXZMusicManager.MusicModel>();
	// for (Audio audio : resultAudios) {
	// MusicModel model = new MusicModel();
	// model.setTitle(audio.getName());
	// model.setArtist(CollectionUtils.toStrings(audio
	// .getArrArtistName()));
	// model.setAlbum(audio.getAlbumName());
	// musics.add(model);
	// }
	// TXZMusicManager.getInstance().syncExMuicList(musics);
	// }
	// };
	public static Runnable mRunnableSpeak = new Runnable() {
		@Override
		public void run() {
			SongListFragment.getInstance().shouldCheckSDCard = false;
			LocalLogic.getDataFromSDCard();
		}
	};

	BroadcastReceiver mRecviceSdcardEvent = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LogUtil.logd("MediaReceiver::" + context.toString() + ",intent:"
					+ intent);
			AndroidMediaLibrary.newSDPath = intent.getDataString();
			// AppLogic.removeBackGroundCallback(mRunnableRefreshMediaList);
			// AppLogic.runOnBackGround(mRunnableRefreshMediaList, 2000);
			if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
				AppLogic.removeSlowGroundCallback(mRunnableSpeak);
				AppLogic.runOnSlowGround(mRunnableSpeak, 2000);
			}
			if (intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)) {
				SongListFragment.getInstance().shouldCheckSDCard = false;
				if (MediaPlayerActivityEngine.getInstance().getCurrentAudio() != null
						&& MediaPlayerActivityEngine.getInstance()
								.getCurrentAudio().getSid() == Constant.LOCAL_MUSIC_TYPE) {
					// MediaPlayerActivityEngine.getInstance().pause();
					// MediaPlayerActivityEngine.getInstance().checkAudio();
					// 将播放器列表的有关数据都删除
					// 刷新界面
					// MediaPlayerActivityEngine.getInstance().refreshAudios();
				}
			}
		}
	};

	private MusicManager() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_SHARED);// 如果SDCard未安装,并通过USB大容量存储共享返回
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);// 表明sd对象是存在并具有读/写权限
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// SDCard已卸掉,如果SDCard是存在但没有被安装
		filter.addAction(Intent.ACTION_MEDIA_CHECKING); // 表明对象正在磁盘检查
		filter.addAction(Intent.ACTION_MEDIA_EJECT); // 物理的拔出 SDCARD
		filter.addAction(Intent.ACTION_MEDIA_REMOVED); // 完全拔出
		filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		filter.addAction(Intent.ACTION_MEDIA_NOFS);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		filter.addDataScheme("file"); // 必须要有此行，否则无法收到广播
		GlobalContext.get().registerReceiver(mRecviceSdcardEvent, filter);
	}

	public static MusicManager getInstance() {
		return sModuleInstance;
	}

	public void unregister() {
		GlobalContext.get().unregisterReceiver(mRecviceSdcardEvent);
	}
}

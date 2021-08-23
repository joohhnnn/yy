package com.txznet.music.fragment.manager;

import java.util.ArrayList;
import java.util.List;

import android.provider.MediaStore;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.dao.HistoryAudioDBHelper;
import com.txznet.fm.dao.LocalAudioDBHelper;
import com.txznet.loader.AppLogic;
import com.txznet.music.bean.IFinishCallBack;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.fragment.logic.LocalLogic;
import com.txznet.music.utils.FileUtils;

/**
 * 用于存放数据
 * 
 * @author ASUS User
 *
 */
public class LocalManager {
	// 单例
	private static LocalManager instance;

	private LocalManager() {
	}

	public static LocalManager getInstance() {
		if (instance == null) {
			synchronized (LocalManager.class) {
				if (instance == null) {
					instance = new LocalManager();
				}
			}
		}
		return instance;
	}

	// 存放本地音乐
	private List<Audio> audios = new ArrayList<Audio>();

	/**
	 * 传null表示清空数据
	 * 
	 * @param audios
	 */
	public void setLocalAudios(List<Audio> audios) {
		this.audios.clear();
		if (audios != null) {
			this.audios.addAll(audios);
		}
	}

	public List<Audio> getLocalAudios() {
		return audios;
	}

	public void deleteNotExistFile(final List<Audio> notExist, final IFinishCallBack<Audio> callback) {
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				if (null != notExist) {
					int count = notExist.size() - 1;
					for (int i = count; i >= 0; i--) {
						if (!LocalLogic.isValid(notExist.get(i))) {
							LogUtil.logd("delete not exist file :" + notExist.get(i).getStrDownloadUrl());
							GlobalContext.get().getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
									MediaStore.Audio.Media._ID + "=" + notExist.get(i).getId(), null);
							MediaPlayerActivityEngine.getInstance().removeAudio(notExist.get(i));
							LocalAudioDBHelper.getInstance().remove(notExist.get(i).getId());
							HistoryAudioDBHelper.getInstance().remove(notExist.get(i).getId(),
									notExist.get(i).getName());
							notExist.remove(i);
						}
					}
				}
				if (callback != null) {
					AppLogic.runOnUiGround(new Runnable() {

						@Override
						public void run() {
							callback.onComplete(notExist);
						}
					}, 0);
				}
			}
		}, 0);
	}

}

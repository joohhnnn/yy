package com.txznet.music.fragment.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.widget.Toast;

import com.txznet.audio.player.audio.TmdFile;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.dao.LocalAudioDBHelper;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.fragment.manager.LocalManager;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.FileUtils;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.MyAsyncTask;
import com.txznet.music.utils.PinYinUtil;
import com.txznet.music.utils.ScanFileUtils;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.UpdateToCoreUtil;
import com.txznet.txz.util.StorageUtil;

public class LocalLogic {

	protected static final String TAG = "[MUSIC][LOGIC] ";

	private boolean pressScan = false;

	private Toast toast;

	private MyAsyncTask<Integer, List<Audio>> execute = null;

	/**
	 * 判断该本地歌曲是否存在
	 * 
	 * @param audio
	 * @return
	 */
	public static boolean isValid(Audio audio) {
		if (audio == null) {
			return false;
		}

		if (audio.getSid() == Constant.LOCAL_MUSIC_TYPE && !FileUtils.isExist(audio.getStrDownloadUrl())) {
//			TtsUtil.speakResource("RS_VOICE_SPEAKNOTEXIST_TIPS", Constant.RS_VOICE_SPEAKNOTEXIST_TIPS);
			return false;
		}
		return true;
	}

	/**
	 * 开始扫描
	 */
	public void scanMusic() {

		// AniUtil.startAnimation(aniLoading);
		if (pressScan) {
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					if (toast != null) {
						toast.cancel();
					}
					toast = Toast.makeText(GlobalContext.get(), R.string.str_loding, Toast.LENGTH_SHORT);
					toast.show();
				}
			}, 100);
			return;
		}
		pressScan = true;

		execute = new MyAsyncTask<Integer, List<Audio>>() {
			protected void onPreExecute() {
				InfoMessage info = new InfoMessage();
				info.setType(InfoMessage.SCAN_STATED);
				ObserverManage.getObserver().setMessage(info);
			};

			@Override
			protected List<Audio> doInBackground(Integer... params) {
				List<Audio> noOrder = getDataFromSDCard();
				// 排序
				Collections.sort(noOrder, new Comparator<Audio>() {

					@Override
					public int compare(Audio lhs, Audio rhs) {
						return lhs.getPinyin().compareTo(rhs.getPinyin());
					}
				});
				LocalManager.getInstance().setLocalAudios(noOrder);

				LogUtil.logd("scan size =" + noOrder.size());
				return noOrder;
			}

			@Override
			protected void onPostExecute(List<Audio> result) {
				// 发送给观察者
				InfoMessage info = new InfoMessage();
				info.setType(InfoMessage.SCAN_FINISHED);
				ObserverManage.getObserver().setMessage(info);
				pressScan = false;
			}

			// @Override
			// protected void onCancelled() {
			// LogUtil.logd("is cancled");
			// pressScan = false;
			// super.onCancelled();
			// }

			protected void onCancelled(java.util.List<Audio> result) {
				pressScan = false;
				LogUtil.logd("is cancled with value");
			};

		}.execute();
	}

	/**
	 * 取消扫描
	 */
	public void scanCancle() {
		// if (!AsyncTaskManager.isInvalid(execute)) {
		pressScan = false;
		execute.cancel(true);
		// }
	}

	public static synchronized List<Audio> getDataFromSDCard() {

		List<File> resultFiles = new ArrayList<File>();
		List<String> allExterSdcardPath = com.txznet.music.fragment.logic.StorageUtil.getVolumeState(GlobalContext.get());
		if (StringUtils.isNotEmpty(SharedPreferencesUtils.getLocalPaths())) {
			JSONBuilder builder = new JSONBuilder(SharedPreferencesUtils.getLocalPaths());
			String[] val = builder.getVal("data", String[].class);

			for (int i = 0; i < val.length; i++) {
				allExterSdcardPath.add(val[i]);
			}
		} 

		LogUtil.logd(TAG + "[path]exterSdcardPath=" + allExterSdcardPath.toString() + ",innerSDcardPath="
				+ StorageUtil.getInnerSDCardPath());
		if (CollectionUtils.isNotEmpty(allExterSdcardPath)) {
			for (int i = 0; i < allExterSdcardPath.size(); i++) {
				resultFiles.addAll(ScanFileUtils.getFiles(allExterSdcardPath.get(i)));
			}
		}
//		resultFiles.addAll(ScanFileUtils.getFiles(StorageUtil.getInnerSDCardPath()));
		LocalAudioDBHelper.getInstance().removeAll();
		// 保存到数据库中
		Set<Audio> audios = new HashSet<Audio>();
		List<Audio> list = new ArrayList<Audio>();
		if (resultFiles != null && resultFiles.size() > 0) {
			// 添加导数据库中
			for (File file : resultFiles) {
				Audio audio = null;
				TmdFile openFile = null;
				// 如果是tmd文件的情况下
				if (file.getAbsolutePath().endsWith(".tmd")) {
					try {
						openFile = TmdFile.openFile(file, -1, false);
						if (openFile == null) {
							audio = MediaPlayerActivityEngine.getInstance().getCurrentAudio();
						} else {
							audio = JsonHelper.toObject(Audio.class, new String(openFile.loadInfo()));
						}
						if (audio != null) {
							audio.setStrDownloadUrl(file.getAbsolutePath());
						}

					} catch (Exception e) {
						LogUtil.loge(TAG + "path=" + file.getAbsolutePath(), e);
					} finally {
						if (openFile != null) {
							openFile.closeQuitely();
						}
					}
				} else {
					audio = new Audio();
					// audio.setDuration(file);
					audio.setStrDownloadUrl(file.getAbsolutePath());
					audio.setName(file.getName().substring(0, file.getName().lastIndexOf(".")));

				}
				if (audio == null) {
					continue;
				}
				audio.setSid(0);
				audio.setDownloadType("0");
				if (audio.getId() == 0) {
					audio.setId(Math.abs(audio.getName().hashCode()));// 使用标题名称作为id
				}
				audio.setPinyin(PinYinUtil.getPinYin(audio.getName()));
				// 小写 p 是 property 的意思，表示 Unicode 属性，用于 Unicode
				// 正表达式的前缀。中括号内的“P”表示Unicode 字符集七个字符属性之一：标点字符。
				String desc = audio.getName().replaceAll("[\\p{P}]", "");
				audio.setDesc(desc);
				if (false == audios.add(audio)) {
					LogUtil.logd("more one same audios :" + audio.getStrDownloadUrl());
				}
			}
			// Set 转List
			Iterator<Audio> iterator = audios.iterator();
			while (iterator.hasNext()) {
				Audio audio = (Audio) iterator.next();
				if (new File(audio.getStrDownloadUrl()).exists()) {
					list.add(audio);
				} else {
					LogUtil.logd("not exist file:" + audio.getStrDownloadUrl());
				}
			}
			LocalAudioDBHelper.getInstance().saveOrUpdate(list);
			UpdateToCoreUtil.updateMusicModel(list);
		}

		return list;
	}

	/**
	 * 删除全部的歌曲
	 */
	public void deleteAllLocalSong() {
		List<Audio> localAudios = LocalManager.getInstance().getLocalAudios();
		if (CollectionUtils.isNotEmpty(localAudios)) {
			for (int i = 0; i < localAudios.size(); i++) {
				FileUtils.delFile(localAudios.get(i).getStrDownloadUrl());
			}
		}
		LogUtil.logd(TAG + "全部删除成功");
	}
}

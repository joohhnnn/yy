package com.txznet.music.service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.audio.UiAudio;
import com.txz.ui.audio.UiAudio.Resp_DataInterface;
import com.txznet.audio.player.RemoteAudioPlayer;
import com.txznet.comm.base.CrashCommonHandler;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceHandler;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.dao.AlbumDBHelper;
import com.txznet.fm.dao.AudioDBHelper;
import com.txznet.fm.dao.DBUtils;
import com.txznet.fm.dao.HistoryAudioDBHelper;
import com.txznet.fm.dao.LocalAudioDBHelper;
import com.txznet.fm.dao.interfase.BaseDaoImpl;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.bean.TempReq;
import com.txznet.music.bean.req.ReqDataStats.Action;
import com.txznet.music.bean.req.ReqSearch;
import com.txznet.music.bean.req.ReqSearchAlbum;
import com.txznet.music.bean.req.ReqThirdSearch;
import com.txznet.music.bean.response.Album;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.bean.response.BaseAudio;
import com.txznet.music.bean.response.Category;
import com.txznet.music.bean.response.Homepage;
import com.txznet.music.bean.response.RespCheck;
import com.txznet.music.bean.response.RespThirdSearch;
import com.txznet.music.bean.response.ResponseAlbumAudio;
import com.txznet.music.bean.response.ResponseSearch;
import com.txznet.music.bean.response.ResponseSearchAlbum;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.helper.RequestHelpe;
import com.txznet.music.helper.RequestHelpe.TempAlbum;
import com.txznet.music.receiver.UIHelper;
import com.txznet.music.ui.MainActivity;
import com.txznet.music.utils.DataInterfaceBroadcastHelper;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.JumpUtils;
import com.txznet.music.utils.NetHelp;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.SyncCoreData;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZResourceManager;
import com.txznet.txz.service.IService;

public class MyService extends Service {
	private final static String TAG = "[Music][service] ";
	private static final int LENGTH = 20;// 上传给COre来播报的数据
	byte[] result = null;
	private long tag;
	private ResponseSearch responseSearch = null;
	private List<Audio> audios;
	private boolean isLocal = false;
	private ReqSearch reqData = null;
	private boolean isClose = false;
	private boolean isPlayRandom = false;// 随便听听
	private String sData;
	private boolean isSearchingData = false;// 声控搜索中
	private static SparseArray<Map<Integer, TempReq>> mCacheArray = new SparseArray<Map<Integer, TempReq>>();

	public byte[] _sendInvoke(final String packageName, final String command, final byte[] data) {
		byte[] ret = ServiceHandler.preInvoke(packageName, command, data);
		LogUtil.logd("[Music][Service] receive " + packageName + " command " + command);
		if (command.equals("music.client.sleep")) {
			LogUtil.logd(TAG + "POWER:: client sleep");
			MediaPlayerActivityEngine.getInstance().exit();
			return null;
		}
		if (command.equals("music.client.wakeup")) {
			boolean wakeupPlay = SharedPreferencesUtils.getWakeupPlay();
			boolean isPlay = SharedPreferencesUtils.getIsPlay();
			LogUtil.logd(TAG + "POWER:: client wakeup:" + wakeupPlay + " isPlay:" + isPlay);

			if (wakeupPlay && isPlay) {
				MediaPlayerActivityEngine.getInstance().play(true);
				return null;
			}
			if (wakeupPlay) {
				MediaPlayerActivityEngine.getInstance().init();
			}
			return null;
		}

		if (command.equals("music.client.exit")) {
			LogUtil.logd(TAG + "POWER:: client exit");
			MediaPlayerActivityEngine.getInstance().exit();
			return null;
		}
		if (command.startsWith("music.remote.callback.")) {
			ret = RemoteAudioPlayer.invokeRemotePlayerCallback(packageName,
					command.substring("music.remote.callback.".length()), data);
		} else if (command.startsWith("music.")) {
			LogUtil.logd(packageName + " invokeMusic " + command);
			ret = invokeMusic(packageName, command.substring("music.".length()), data);
		} else if (command.equals("sdk.init.success")) {
			SyncCoreData.syncCurStatusFullStyle();
			AppLogic.regOwnerComm();
		} else if (command.startsWith("audio.")) {
			invokeAudio(packageName, command.substring("audio.".length()), data);
		}
		return ret;
	}
	
	public class SampleBinder extends IService.Stub {
		@Override
		public byte[] sendInvoke(final String packageName, final String command, final byte[] data)
				throws RemoteException {
			try {
				return _sendInvoke(packageName, command, data);
			} catch (Exception e) {
				CrashCommonHandler.getInstance().uncaughtException(Thread.currentThread(), e);
			}
			return null;
		}
	}

	private void invokeAudio(String packageName, String command, byte[] data) {
		if (command.equals("play")) {// 播放电台，需要打开界面
			try {
				MediaPlayerActivityEngine.getInstance().play(Constant.KAOLAINT);
			} finally {
				Utils.jumpTOMediaPlayerAct(false);
			}
		}
	}

	private byte[] invokeMusic(final String packageName, String command, byte[] data) {
		// ////////////////////////////////////////////////////////////////////////////////////////////////////
		if (command.equals("dataInterface")) {
			UiAudio.Resp_DataInterface dataInterface = null;
			try {
				dataInterface = UiAudio.Resp_DataInterface.parseFrom(data);

			} catch (InvalidProtocolBufferNanoException e) {
				LogUtil.loge("InvalidProtocolBufferNanoException:" + e.getMessage());
				return null;
			}
			if (null != dataInterface) {
				sData = new String(dataInterface.strData);
				if (Constant.ISTESTDATA) {
					LogUtil.logd(TAG + "[Response]response:cmd[" + dataInterface.strCmd + "]" + " errorCode ="
							+ dataInterface.uint32ErrCode + "seq=" + dataInterface.uint32Seq + ",data=" + sData);
				} else {
					LogUtil.logd(TAG + "[Response]response:cmd[" + dataInterface.strCmd + "]" + "errorCode="
							+ dataInterface.uint32ErrCode);
				}
				if (DataInterfaceBroadcastHelper.sendDataInterfaceResp(dataInterface)) {
					return null;
				}
				NetHelp.mTimeOutmaps.remove(dataInterface.strCmd);
				if (doErrorCode(dataInterface)) {
					return null;
				}

				if (Constant.GET_CATEGORY.equals(dataInterface.strCmd)) {
					if (Constant.ManualSessionID == dataInterface.uint32Seq) {
						doCategory();
					}
				} else if (Constant.GET_SEARCH_LIST.equals(dataInterface.strCmd)) {
					doAlbumList();
				} else if (Constant.GET_ALBUM_AUDIO.equals(dataInterface.strCmd)) {
					if (Constant.ManualSessionID == dataInterface.uint32Seq) {
						TempAlbum tempAlbum = RequestHelpe.reqLine.get(dataInterface.uint32Seq);
						LogUtil.logd(TAG + "reqline :" + (tempAlbum != null ? tempAlbum.toString() : "null"));
						doAlbumAudio();
					} else {
						LogUtil.logd(TAG + " not support " + Constant.GET_ALBUM_AUDIO + "  " + Constant.ManualSessionID
								+ "/" + dataInterface.uint32Seq);
					}
				} else if (Constant.GET_SEARCH.equals(dataInterface.strCmd)) {
					if (isClose && !isPlayRandom) {
						return null;
					}
					if (Constant.SoundSessionID != dataInterface.uint32Seq) {
						LogUtil.logd("reqID&respID not same,searchSequenceId[" + Constant.SoundSessionID + "/"
								+ dataInterface.uint32Seq + "]");
						return null;
					}

					doSearch();
				} else if (Constant.GET_TAG.equals(dataInterface.strCmd)) {
					doTag();
				} else if (Constant.GET_FAKE_SEARCH.equals(dataInterface.strCmd)) {
					doFakeReq();
				}
			}

			return null;
		}
		LogUtil.logd(TAG + "[Command]Audio::MyService::command::" + command);
		if (command.equals("playmusiclist.index")) {
			int index = Integer.parseInt(new String(data));
			choiceIndex(index);
			return null;
		}

		if (command.equals("sound.find")) {
			isClose = false;
			isPlayRandom = false;
			Constant.SoundSessionID = 0;
			String soundData = new String(data);
			doSoundFind(soundData);
			return null;
		}

		if (command.equals("isPlaying")) {
			return ("" + MediaPlayerActivityEngine.getInstance().isPlaying()).getBytes();
		}
		if (command.equals("get.version")) {
			return String.valueOf(true).getBytes();
		}
		// 由TXZMusicManger.getInstance().play()进行调用
		if (command.equals("play")) {
			LogUtil.logd("test::---------playmusic");
			// MediaPlayerActivityEngine.getInstance().play(Constant.QQINT);
			MediaPlayerActivityEngine.getInstance().play();
			NetHelp.sendReportData(Action.PLAY);
			return null;
		}
		// 播放音乐和随便听听
		if (command.equals("play.inner") || command.equals("playRandom")) {
			try {
				isPlayRandom = true;
				LogUtil.logd("audioplayer play sound");
				MediaPlayerActivityEngine.getInstance().play(Constant.QQINT);
				NetHelp.sendReportData(Action.PLAY_SOUND);
			} finally {
				Utils.jumpTOMediaPlayerAct(false);
			}
			return null;
		}
		if (command.equals("pause")) {
			LogUtil.logd("audioplayer pause sound");
			MediaPlayerActivityEngine.getInstance().pause();
			NetHelp.sendReportData(Action.PAUSE_SOUND);
			return null;
		}
		if (command.equals("prev")) {
			MediaPlayerActivityEngine.getInstance().last();
			NetHelp.sendReportData(Action.PREVIOUS_SOUND);
			return null;
		}
		if (command.equals("next")) {
			MediaPlayerActivityEngine.getInstance().next();
			NetHelp.sendReportData(Action.NEXT_SOUND);
			return null;
		}
		if (command.equals("exit")) {
			UIHelper.exit();
			return null;
		}
		if (command.equals("switchModeLoopAll")) {
			MediaPlayerActivityEngine.getInstance().changeMode(com.txznet.music.Constant.PlayMode.SEQUENCE.ordinal());
			return null;
		}
		if (command.equals("switchModeLoopOne")) {
			MediaPlayerActivityEngine.getInstance()
					.changeMode(com.txznet.music.Constant.PlayMode.SINGLE_CIRCLE.ordinal());
			return null;
		}
		if (command.equals("switchModeRandom")) {
			MediaPlayerActivityEngine.getInstance().changeMode(com.txznet.music.Constant.PlayMode.RANDOM.ordinal());
			return null;
		}
		if (command.equals("switchSong")) {
			MediaPlayerActivityEngine.getInstance().next();
			NetHelp.sendReportData(Action.NEXT_SOUND);
			return null;
		}
		if (command.equals("startappplay")) {
			String value = new String(data);
			LogUtil.logd("startappplay::" + value);
			SharedPreferencesUtils.setAppFirstPlay(Boolean.parseBoolean(value));
			return null;
		}
		if (command.equals("needAsr")) {
			String value = new String(data);
			LogUtil.logd("from" + packageName + ",needAsr::" + value);
			boolean needAsr = Boolean.parseBoolean(value);
			SharedPreferencesUtils.setNeedAsr(needAsr);
			if (!needAsr) {
				TXZAsrManager.getInstance().recoverWakeupFromAsr("SPEAK_MUSIC_PLAYER_TEXT");// 反注册掉相应的全局唤醒字
			} else {
				AppLogic.regAsrCommand();
			}
			return null;
		}
		if (command.equals("searchSize")) {
			String searchSize = new String(data);
			LogUtil.logd("searchSize::" + searchSize);
			try {
				SharedPreferencesUtils.setSearchSize(Long.parseLong(searchSize));
			} catch (Exception e) {
				LogUtil.logd("set search size error：" + searchSize);
			}
			return null;
		}
		if (command.equals("sound.cancelfind")) {
			if (isSearchingData) {
				isSearchingData = false;
				isClose = true;
				LogUtil.logd("sound.cancelfind");
				MonitorUtil.monitorCumulant(Constant.M_SOUND_CANCLE);
				Constant.SoundSessionID = -1;
			}
			return null;
		}
		if (command.equals("update.playMediaList")) {// 将为您来一大波歌曲
			MediaPlayerActivityEngine.getInstance().play();
			return null;
		}

		if (command.equals("notOpenAppPName")) {
			LogUtil.logd("not openApp package name =" + new String(data));
			String string = new String(data);
			SharedPreferencesUtils.setNotOpenAppPName(string);
			return null;
		}

		if (command.equals("releaseAudioFocus")) {
			String value = new String(data);
			LogUtil.logd(TAG + "set releaseAudioFocus:" + value);
			Boolean isRelease = Boolean.valueOf(value);
			SharedPreferencesUtils.setReleaseAudioFocus(isRelease);
		}

		if (command.equals("wakeupPlay")) {
			String value = new String(data);
			LogUtil.logd(TAG + "set wakeupPlay:" + value);
			Boolean isPlay = Boolean.valueOf(value);
			SharedPreferencesUtils.setWakeupPlay(isPlay);
		}

		if (command.equals("maxVolume")) {
			if (!SharedPreferencesUtils.isCloseVolume()) {
				MediaPlayerActivityEngine.getInstance().setVolume(1.0f);
			}
			return null;
		}

		if (command.equals("closeVolume")) {
			boolean close = Boolean.parseBoolean(new String(data));
			SharedPreferencesUtils.setCloseVolume(close);
			// 反注册掉全局唤醒词，在重新注册
			if (close) {
				TXZAsrManager.getInstance().recoverWakeupFromAsr("SPEAK_MUSIC_PLAYER_TEXT");// 反注册掉相应的全局唤醒字
			}
			AppLogic.regAsrCommand();

			return null;
		}
		if ("open.play".equals(command)) {
			TtsUtil.speakTextOnRecordWin("RS_VOICE_RS_VOICE_SPEAK_OPEN_PLAYER", Constant.RS_VOICE_SPEAK_OPEN_PLAYER,
					true, new Runnable() {

						@Override
						public void run() {
							Intent it = new Intent(GlobalContext.get(), MainActivity.class);
							it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							try {
								GlobalContext.get().startActivity(it);
							} catch (Exception e) {
								LogUtil.loge("open mainactivity error!");
							}
							MediaPlayerActivityEngine.getInstance().play();
						}
					});
			return null;
		}

		if ("open".equals(command)) {
			TtsUtil.speakTextOnRecordWin("RS_VOICE_RS_VOICE_SPEAK_OPEN_PLAYER", Constant.RS_VOICE_SPEAK_OPEN_PLAYER,
					true, new Runnable() {

						@Override
						public void run() {
							Utils.jumpTOMediaPlayerAct(true);
						}
					});
			return null;
		}

		if ("searchPath".equals(command)) {
			// 设置本地搜索路径
			String string = new String(data);
			SharedPreferencesUtils.setLocalPaths(string);
			LogUtil.logd("[Service] set local paths:" + string);
			return null;
		}
		if ("fullscreen".equals(command)) {
			// 设置本地搜索路径
			String string = new String(data);
			SharedPreferencesUtils.setFullScreen(Boolean.parseBoolean(string));
			LogUtil.logd("[Service] set FullScreen:" + string);
			return null;
		}

		return null;
	}

	public static String rawText = "";

	/**
	 * 声控处理
	 */
	private void doSoundFind(String soundData) {
		isSearchingData = true;
		MonitorUtil.monitorCumulant(Constant.M_SOUND_FIND);
		array = new JSONArray();
		// 声控获取
		reqData = new ReqSearch();
		soundData.replaceAll(" ", "");
		LogUtil.logd("sound.find:::" + soundData);

		JSONBuilder jsonBuilder = new JSONBuilder(soundData);
		reqData.setAudioName(jsonBuilder.getVal("title", String.class));
		reqData.setAlbumName(jsonBuilder.getVal("album", String.class));
		reqData.setArtist(StringUtils.toString(jsonBuilder.getVal("artist", String[].class)));
		reqData.setCategory(
				StringUtils.StringFilter(StringUtils.toString(jsonBuilder.getVal("keywords", String[].class))));
		reqData.setField(jsonBuilder.getVal("field", int.class, 0));// 1。表示歌曲，2.表示电台
		reqData.setSubCategory(jsonBuilder.getVal("subcategory", String.class));
		rawText = jsonBuilder.getVal("text", String.class);
		reqData.setText(jsonBuilder.getVal("text", String.class));
		// 后台说:只认category
		if (StringUtils.isNotEmpty(reqData.getSubCategory())) {
			reqData.setCategory(reqData.getSubCategory());
		}

		if (StringUtils.isEmpty(reqData.getCategory())) {
			reqData.setCategory(jsonBuilder.getVal("category", String.class));
		}

		LogUtil.logd("sound.find:::ReqSearch::" + reqData.toString());

		StringBuffer sbBuffer = new StringBuffer();
		// 修改bug，歌手需要在歌名中查找bug-5184
		if (StringUtils.isNotEmpty(reqData.getArtist())) {
			// 我要听周杰伦的歌
			sbBuffer.append(" (" + BaseDaoImpl.TABLE_ARTISTS + " ");
			sbBuffer.append(" like '%");
			sbBuffer.append(reqData.getArtist());
			sbBuffer.append("%' ");
			sbBuffer.append(" or ");
			sbBuffer.append(" " + BaseDaoImpl.TABLE_NAME + " ");
			sbBuffer.append(" like '%");
			sbBuffer.append(reqData.getArtist());
			sbBuffer.append("%') ");

		}
		if (StringUtils.isNotEmpty(reqData.getAudioName())
				&& (StringUtils.isNotEmpty(reqData.getArtist()) || StringUtils.isNotEmpty(reqData.getAlbumName()))) {
			sbBuffer.append(" and ");
		}
		if (StringUtils.isNotEmpty(reqData.getAudioName())) {
			sbBuffer.append(" name ");
			sbBuffer.append(" like '%");
			sbBuffer.append(reqData.getAudioName());
			sbBuffer.append("%' ");
		}

		if (StringUtils.isEmpty(reqData.getArtist()) && StringUtils.isEmpty(reqData.getAudioName())
				&& StringUtils.isNotEmpty(reqData.getAlbumName())) {
			sbBuffer.append(" name ");
			sbBuffer.append(" like '%");
			sbBuffer.append(reqData.getAlbumName());
			sbBuffer.append("%' ");
		}
		if (StringUtils.isNotEmpty(sbBuffer.toString())) {
			LogUtil.logd("search::Local::music::" + sbBuffer.toString());
			audios = LocalAudioDBHelper.getInstance().findAll(Audio.class, sbBuffer.toString(), null, null);
		} else {
			audios = null;
		}

		if (CollectionUtils.isNotEmpty(audios)) {
			LogUtil.logd("sound.find:::localMusic::" + audios.toString());
			isLocal = true;
			// MediaPlayerActivityEngine.getInstance().setCurrentAlbumName("");
			// MediaPlayerActivityEngine.getInstance().setCurrentAlbum(
			// SystemClock.elapsedRealtime());
			if (!Utils.isNetworkConnected(GlobalContext.get())) {// 没有网络则
				// SharedPreferencesUtils.setCurrentAlbumID(0);
				if (audios.size() == 1) {// 就只有一条数据
					MediaPlayerActivityEngine.getInstance().stopAndLoading();
					SharedPreferencesUtils.setAudioSource(Constant.LOCAL_MUSIC_TYPE);
					List<Audio> localAudios = LocalAudioDBHelper.getInstance().findAll(Audio.class);
					MediaPlayerActivityEngine.getInstance().setAudios(localAudios, localAudios.indexOf(audios.get(0)));
					MediaPlayerActivityEngine.getInstance().playOrPause();
					NetHelp.sendReportData(Action.PLAY_SOUND);

					StringBuffer speakString = new StringBuffer();
					if (CollectionUtils.isNotEmpty(audios.get(0).getArrArtistName())
							&& !"未知艺术家".equals(audios.get(0).getArrArtistName())) {
						speakString.append(StringUtils.toString(audios.get(0).getArrArtistName()));
						if (speakString.length() > 0) {
							speakString.append("的");
						}
					}
					String name = speakString.toString() + audios.get(0).getName();
					TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_WILL_PLAY",
							StringUtils.replace(Constant.RS_VOICE_SPEAK_WILL_PLAY, name),
							new String[] { Constant.PLACEHODLER, name }, true, null);
				} else {
					JSONArray array = new JSONArray();
					for (int i = 0; i < audios.size(); i++) {
						JSONObject jsonObject = new JSONObject();
						try {
							jsonObject.put("title", audios.get(i).getName());
							jsonObject.put("name", StringUtils.toString(audios.get(i).getArrArtistName()));
							jsonObject.put("id", audios.get(i).getId());
							array.put(jsonObject);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					NetHelp.sendReportData(Action.FOUND_SOUND);
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.syncmusiclist",
							array.toString().getBytes(), null);
				}
				return;
			}
		} else
		// 从历史数据中查找数据
		if (StringUtils.isNotEmpty(reqData.getAudioName()) || StringUtils.isNotEmpty(reqData.getCategory())) {
			// 此处定义比较模糊故去掉
			/*
			 * List<Integer> sids = Utils.getfmSid(); StringBuffer sb = new
			 * StringBuffer(); sb.append("("); for (int j = 0; j < sids.size();
			 * j++) { if (j > 0) { sb.append(" or "); }
			 * sb.append("sid").append(" = ").append(sids.get(j)); }
			 * 
			 * sb.append(")"); sb.append(" and ( "); if
			 * (StringUtils.isNotEmpty(reqData.getAudioName())) {
			 * sb.append(" albumName like '%" + reqData.getAudioName() + "%'");
			 * } if (StringUtils.isNotEmpty(reqData.getAudioName()) &&
			 * StringUtils.isNotEmpty(reqData.getCategory())) {
			 * sb.append(" or "); } if
			 * (StringUtils.isNotEmpty(reqData.getCategory())) {
			 * sb.append(" albumName = '" + reqData.getCategory() + "'"); }
			 * sb.append(" )"); final List<Audio> audios =
			 * HistoryAudioDBHelper.getInstance() .findAll(Audio.class,
			 * sb.toString(), null, null); if
			 * (CollectionUtils.isNotEmpty(audios)) {
			 * LogUtil.logd("sound.find:::HistoryKaoLa::" + audios.toString());
			 * isLocal = true; MediaPlayerActivityEngine.getInstance().stop();
			 * // MediaPlayerActivityEngine.getInstance().setCurrentAlbum( //
			 * Utils.toLong(audios.get(0).getAlbumId())); //
			 * MediaPlayerActivityEngine.getInstance().setCurrentAlbumName( //
			 * audios.get(0).getAlbumName());
			 * MediaPlayerActivityEngine.getInstance().setCurrentPage(
			 * audios.size() % Constant.PAGECOUNT == 0 ? audios.size() /
			 * Constant.PAGECOUNT : audios.size() / Constant.PAGECOUNT + 1);
			 * SharedPreferencesUtils.setAudioSource(Constant.HISTORY_TYPE);
			 * final List<Audio> queryData = AudioDBHelper.getInstance().find(
			 * Audio.class, "albumId == ?", new String[] {
			 * audios.get(0).getAlbumId() });
			 * MediaPlayerActivityEngine.getInstance().setAudios(queryData,
			 * queryData.indexOf(audios.get(0)));
			 * TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_WILL_PLAY",
			 * StringUtils.replace(Constant.RS_VOICE_SPEAK_WILL_PLAY,
			 * audios.get(0).getAlbumName()), new String[] {
			 * Constant.PLACEHODLER, audios.get(0).getAlbumName() }, true, new
			 * Runnable() {
			 * 
			 * @Override public void run() {
			 * MediaPlayerActivityEngine.getInstance() .playOrPause(); //
			 * .start(queryData.get(queryData.indexOf(audios // .get(0)))); }
			 * }); return; }
			 */}
		if (Constant.SoundSessionID == -1) {
			LogUtil.logd("cancle so can't do net request");
			return;
		} else {
			LogUtil.logd("soundID=" + Constant.SoundSessionID);
		}
		if (!Utils.isNetworkConnected(GlobalContext.get())) {
			TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_NETNOTCON_TIPS", Constant.RS_VOICE_SPEAK_NETNOTCON_TIPS, false,
					null);
		} else {
			isLocal = false;
			TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_SEARCHDATA_TIPS", Constant.RS_VOICE_SPEAK_SEARCHDATA_TIPS,
					null, false, false, new Runnable() {

						@Override
						public void run() {
							Constant.SoundSessionID = NetHelp.sendRequest(Constant.GET_SEARCH, reqData);
						}
					});

		}

	}

	/**
	 * 选中第几个
	 * 
	 * @param index
	 */
	private void choiceIndex(int index) {
		try {
			isSearchingData = false;
			// MediaPlayerActivityEngine.getInstance().setCurrentAlbumName("");
			MediaPlayerActivityEngine.getInstance().stopAndLoading();// 暂停上一首歌曲的数据
			if (isLocal) {
				// 如果是专辑的话
				List<Audio> audios = this.audios.subList(index, index + 1);
				LogUtil.logd("playmusiclist.index::localMusic::" + audios.toString());
				if (CollectionUtils.isEmpty(audios)) {
					return;
				}
				List<Audio> localAudios = LocalAudioDBHelper.getInstance().findAll(Audio.class);
				MediaPlayerActivityEngine.getInstance().setAudios(localAudios, localAudios.indexOf(audios.get(0)));
				MediaPlayerActivityEngine.getInstance().playOrPause();
				NetHelp.sendReportData(Action.INDEX_SOUND);
				return;
			}
			if (null != responseSearch) {
				SharedPreferencesUtils.setAudioSource(Constant.TYPE_SOUND);
				if (responseSearch.getReturnType() == 2) {// 专辑
					Album album = responseSearch.getArrAlbum().get(index);
					doSelectAlbum(album);
				} else if (responseSearch.getReturnType() == 1) {
					doSelectAudio(responseSearch.getArrAudio().get(index), responseSearch.getArrAudio(), index);
				} else
				// 有可能有音频或专辑
				if (responseSearch.getReturnType() == 3) {
					BaseAudio baseAudio = responseSearch.getArrMix().get(index);
					// Audio
					if (baseAudio.getType() == BaseAudio.MUSIC_TYPE) {
						List<Audio> searchAudios = new ArrayList<Audio>();
						for (int i = 0; i < responseSearch.getArrMix().size(); i++) {
							if (responseSearch.getArrMix().get(i).getType() == BaseAudio.MUSIC_TYPE) {
								searchAudios.add(responseSearch.getArrMix().get(i).getAudio());
							}
						}
						doSelectAudio(baseAudio.getAudio(), searchAudios, index);
					} else
					// Album
					if (baseAudio.getType() == BaseAudio.ALBUM_TYPE) {
						doSelectAlbum(baseAudio.getAlbum());
					}

				}
			}
		} finally {
			// 打开界面
			Utils.jumpTOMediaPlayerAct(false);
		}
	}

	/**
	 * 播放音频
	 * 
	 * @param audio
	 *            带歌曲的时候直接播放该歌曲 播放列表为历史播放+该歌曲，否则为声控里面所有的歌曲
	 * @param playList
	 *            播放列表 当带歌曲的时候可以为null
	 * @param index
	 *            播放播放列表中的第几个歌曲
	 */
	private void doSelectAudio(Audio audio, List<Audio> playList, int index) {
		// SharedPreferencesUtils.setCurrentAlbumID(0);
		if (reqData != null && StringUtils.isNotEmpty(reqData.getAudioName())) {
			List<Integer> sids = Utils.getSongSid();
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < sids.size(); j++) {
				if (j > 0) {
					sb.append(" or ");
				}
				sb.append("sid").append(" = ").append(sids.get(j));
			}

			List<Audio> localMusic = HistoryAudioDBHelper.getInstance().findAll(Audio.class, sb.toString(), null, null);
			localMusic.add(0, audio);
			MediaPlayerActivityEngine.getInstance().setAudios(localMusic, 0);
		} else {
			MediaPlayerActivityEngine.getInstance().setAudios(playList, index);
		}
		MediaPlayerActivityEngine.getInstance().playOrPause();
		NetHelp.sendReportData(Action.INDEX_SOUND);
	}

	/**
	 * 播放选中的专辑
	 */
	private void doSelectAlbum(Album album) {
		if (MediaPlayerActivityEngine.getInstance().getCurrentAlbum() != album.getId()) {
			// 取第一个
			// 保存导数据库
			AlbumDBHelper.getInstance().save(album);
			if (CollectionUtils.isNotEmpty(album.getArrCategoryIds())) {
				RequestHelpe.reqAudio(album, album.getArrCategoryIds().get(0));
				NetHelp.sendReportData(Action.INDEX_SOUND);
			} else {
				LogUtil.loge(TAG + "[Select][Album]Album's arrCategoryIDs is null ");
			}
		}
	}

	/**
	 * 做假请求
	 */
	private void doFakeReq() {

		final RespThirdSearch object = JsonHelper.toObject(RespThirdSearch.class, sData);
		if (!object.isbIsFinished()) {// 没有完成
			try {
				Map<Integer, TempReq> temp = mCacheArray.get(object.getSid());
				if (temp != null) {
					TempReq tempReq = temp.get(object.getStepId());
					if (tempReq != null) {
						// TODO:如何算超时？？？
						if (System.currentTimeMillis() - tempReq.getTimeStemp() <= tempReq.getCacheTime()) {// 如果没有超时的话
							ReqThirdSearch reqData = new ReqThirdSearch(object.getSid(), object.getId(),
									object.getStepId());
							reqData.setDeviceNum(object.getDeviceNum());
							reqData.setTimeStamp(object.getTimeStamp());
							NetHelp.sendRequest(Constant.GET_FAKE_SEARCH, reqData);
							return;
						}
					}
				}

				StringBuffer sBuffer = new StringBuffer();
				if (object != null && object.getBody() != null && object.getBody().size() > 0) {
					sBuffer.append("?");
					Iterator<String> iterator = object.getBody().keySet().iterator();
					while (iterator.hasNext()) {
						String key = iterator.next();
						sBuffer.append(key).append("=").append(object.getBody().get(key)).append("&");
					}
					if (sBuffer.length() > 0) {
						sBuffer.deleteCharAt(sBuffer.length() - 1);
					}
				}

				if (TextUtils.isEmpty(object.getStrRequestUrl()) || TextUtils.isEmpty(sBuffer.toString())) {
					return;
				}

				HttpURLConnection connection = (HttpURLConnection) new URL(
						object.getStrRequestUrl() + sBuffer.toString()).openConnection();
				if (StringUtils.isEmpty(object.getMethod())) {
					LogUtil.loge("request method have error ");
					return;
				}
				connection.setRequestMethod(object.getMethod().toUpperCase());
				if (object != null && object.getBody() != null && object.getBody().size() > 0) {
					Iterator<String> iterator1 = object.getBody().keySet().iterator();
					while (iterator1.hasNext()) {
						String key = iterator1.next();
						connection.setRequestProperty(key, object.getBody().get(key));
					}
				}
				if (connection.getResponseCode() == 200) {// 正常
					StringBuffer sbBuffer = new StringBuffer();
					byte[] bytes = new byte[2048];
					ReqThirdSearch reqData = new ReqThirdSearch(object.getSid(), object.getId(), object.getStepId());
					reqData.setDeviceNum(object.getDeviceNum());
					reqData.setTimeStamp(object.getTimeStamp());
					int read = 0;
					if (object.isbCache()) {
						// 保存起来数据
						InputStream inputStream = connection.getInputStream();
						while ((read = inputStream.read(bytes)) > 0) {
							sbBuffer.append(new String(bytes, 0, read));
						}
						// 保存临时变量
						Map<Integer, TempReq> map = mCacheArray.get(object.getSid());
						if (map == null) {
							map = new HashMap<Integer, TempReq>();
						}
						TempReq req = new TempReq();
						req.setData(sbBuffer.toString());
						req.setTimeStemp(System.currentTimeMillis());
						req.setCacheTime(object.getCacheTime());

						map.put(object.getStepId(), req);
						mCacheArray.append(object.getSid(), map);
						reqData.setStrCache(sbBuffer.toString());
					}
					NetHelp.sendRequest(Constant.GET_FAKE_SEARCH, reqData);
				} else {
					LogUtil.logd("connection.getResponseCode()=" + connection.getResponseCode());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			try {
				HttpURLConnection connection = (HttpURLConnection) new URL(object.getStrRequestUrl()).openConnection();
				if (connection.getResponseCode() == 200) {// 正常
					// 不做任何处理
					Log.d("Myservice", "fake is over");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 标志位的处理
	 */
	private void doTag() {
		RespCheck check = JsonHelper.toObject(RespCheck.class, sData);
		tag = check.getLogoTag();
		// 保存成为配置文件
		SharedPreferencesUtils.setConfig(JsonHelper.toJson(check));

		long dbTag = 0;
		SQLiteDatabase openReadGlobe = null;
		Cursor query = null;
		try {
			openReadGlobe = DBUtils.getInstance().openReadGlobe();
			query = openReadGlobe.query(RespCheck.class.getSimpleName(), null, null, null, null, null, "logoTag desc");
			if (query.moveToNext()) {
				dbTag = query.getLong(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != query) {
				query.close();
			}
		}
		// if (tag != dbTag || Constant.ISTESTDATA) {
		RequestHelpe.reqCategory();
		// } else {
		// // 则不请求
		// LogUtil.logd("Tag相同:::" + dbTag);
		// }
	}

	JSONArray array = new JSONArray();

	/**
	 * 搜索结果数据的处理
	 */
	private void doSearch() {
		try {
			// MediaPlayerActivityEngine.getInstance().setCurrentAlbum(
			// System.currentTimeMillis());
			responseSearch = JsonHelper.toObject(ResponseSearch.class, sData);
		} catch (Exception e) {
			e.printStackTrace();
			TtsUtil.speakResource("RS_VOICE_SPEAK_JSONERR_TIPS", Constant.RS_VOICE_SPEAK_JSONERR_TIPS);
			return;
		}
		if (CollectionUtils.isEmpty(responseSearch.getArrAudio())
				&& CollectionUtils.isEmpty(responseSearch.getArrAlbum())
				&& CollectionUtils.isEmpty(responseSearch.getArrMix()) && CollectionUtils.isEmpty(audios)) {
			String tips = Constant.RS_VOICE_SPEAK_NODATAFOUND_TIPS;
			if (reqData != null && StringUtils.isNotEmpty(reqData.getArtist())
					&& StringUtils.isNotEmpty(reqData.getAudioName())) {
				tips = Constant.RS_VOICE_SPEAK_NODATAFOUND_WITH_TIPS;
			}
			MonitorUtil.monitorCumulant(Constant.M_EMPTY_SOUND);
			TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_NODATAFOUND_TIPS", tips, false, null);
			return;
		}
		if (CollectionUtils.isNotEmpty(audios)) {// 本地搜索出来的结果
			if (CollectionUtils.isNotEmpty(responseSearch.getArrAudio())) {
				// 剔除相同歌名和歌手的数据
				List<Audio> arrAudio = responseSearch.getArrAudio();
				for (Audio audio : audios) {
					for (int i = arrAudio.size() - 1; i >= 0; i--) {
						if (audio.getId() == arrAudio.get(i).getId()) {
							arrAudio.remove(i);
						}
					}
				}
				responseSearch.getArrAudio().addAll(0, audios);
			} else {
				responseSearch.setArrAudio(audios);
			}
		}

		synchronized (array) {

			// Core 只需要三个数据：title，name,id
			int length = LENGTH;
			int countAudio = 0;
			int countAlbum = 0;

			if (CollectionUtils.isNotEmpty(responseSearch.getArrMix())) {
				if (responseSearch.getArrMix().size() < LENGTH) {
					length = responseSearch.getArrMix().size();
				}

				for (int i = 0; i < length; i++) {
					countAudio++;
					JSONObject jsonObject = new JSONObject();
					BaseAudio baseAudio = responseSearch.getArrMix().get(i);
					if (null == baseAudio) {
						continue;
					}
					try {
						if (baseAudio.getAlbum() != null) {
							Album album = baseAudio.getAlbum();
							jsonObject.put("title", album.getName());
							jsonObject.put("name", StringUtils.toString(album.getArrArtistName()));
							jsonObject.put("id", album.getId());
							jsonObject.put("report", album.getReport());
							if (responseSearch.getPlayType() == ResponseSearch.DELAYPLAY) {
								jsonObject.put("delayTime", responseSearch.getDelayTime());
							}
						} else if (baseAudio.getAudio() != null) {
							Audio audio = baseAudio.getAudio();
							jsonObject.put("title", audio.getName());
							jsonObject.put("name", StringUtils.toString(audio.getArrArtistName()));
							jsonObject.put("id", audio.getId());
							jsonObject.put("report", audio.getReport());
							if (responseSearch.getPlayType() == ResponseSearch.DELAYPLAY) {
								jsonObject.put("delayTime", responseSearch.getDelayTime());
							}
						}

						array.put(jsonObject);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

			if (CollectionUtils.isNotEmpty(responseSearch.getArrAudio())) {
				if (responseSearch.getArrAudio().size() < LENGTH) {
					length = responseSearch.getArrAudio().size();
				}

				for (int i = 0; i < length; i++) {
					countAudio++;
					JSONObject jsonObject = new JSONObject();
					Audio audio = responseSearch.getArrAudio().get(i);
					if (null == audio) {
						continue;
					}
					try {
						jsonObject.put("title", audio.getName());
						jsonObject.put("name", StringUtils.toString(audio.getArrArtistName()));
						jsonObject.put("id", audio.getId());
						jsonObject.put("report", audio.getReport());
						if (responseSearch.getPlayType() == ResponseSearch.DELAYPLAY) {
							jsonObject.put("delayTime", responseSearch.getDelayTime());
						}
						array.put(jsonObject);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			if (CollectionUtils.isNotEmpty(responseSearch.getArrAlbum())) {
				if (responseSearch.getArrAlbum().size() < LENGTH) {
					length = responseSearch.getArrAlbum().size();
				}
				for (int i = 0; i < length; i++) {
					countAlbum++;
					JSONObject jsonObject = new JSONObject();
					Album album = responseSearch.getArrAlbum().get(i);
					if (album == null) {// 服务器有可能返回null对象
						continue;
					}
					try {
						jsonObject.put("title", album.getName());
						jsonObject.put("name", StringUtils.toString(album.getArrArtistName()));
						jsonObject.put("id", album.getId());
						jsonObject.put("report", album.getReport());
						if (responseSearch.getPlayType() == ResponseSearch.DELAYPLAY) {
							jsonObject.put("delayTime", responseSearch.getDelayTime());
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					array.put(jsonObject);
				}
			}

			// if (isPlayRandom) {
			// MediaPlayerActivityEngine.getInstance().setAudios(
			// responseSearch.getArrAudio(), 0);
			//
			// MediaPlayerActivityEngine.getInstance().play(Constant.QQINT);
			// } else {
			// ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
			// "txz.music.syncmusiclist", array.toString().getBytes(),
			// null);
			// }

			if (Constant.SoundSessionID == Constant.RecommandID) {
				// 取出所有的Audio，在mix中的也需要
				if (com.txznet.music.utils.CollectionUtils.isEmpty(responseSearch.getArrAudio())) {
					// 取出混合部分的音频
					if (com.txznet.music.utils.CollectionUtils.isNotEmpty(responseSearch.getArrMix())) {
						List<Audio> arrAudios = new ArrayList<Audio>();
						for (int i = 0; i < responseSearch.getArrMix().size(); i++) {
							BaseAudio baseAudio = responseSearch.getArrMix().get(i);
							if (baseAudio != null) {
								Audio audio = baseAudio.getAudio();
								arrAudios.add(audio);
							}
						}
						MediaPlayerActivityEngine.getInstance().setAudios(arrAudios, 0);
					}
				} else {
					MediaPlayerActivityEngine.getInstance().setAudios(responseSearch.getArrAudio(), 0);
				}
				MediaPlayerActivityEngine.getInstance().playOrPause();
				return;
			}

			// 直接播放
			if (responseSearch.getPlayType() == ResponseSearch.GOPLAY) {
				isSearchingData = false;
				String name = Constant.RS_VOICE_SPEAK_PARSE_ERROR;
				try {
					if (responseSearch.getReturnType() == 1) {
						Audio returnAudio = responseSearch.getArrAudio().get(responseSearch.getPlayIndex());
						if (CollectionUtils.isNotEmpty(returnAudio.getArrArtistName())) {
							name = returnAudio.getArrArtistName().get(0) + "的" + returnAudio.getName();
						} else {
							name = returnAudio.getName();
						}
					} else if (responseSearch.getReturnType() == 2) {
						name = responseSearch.getArrAlbum().get(responseSearch.getPlayIndex()).getName();
					} else if (responseSearch.getReturnType() == 3) {
						if (responseSearch.getArrMix().get(responseSearch.getPlayIndex()).getType() == 1) {

							Audio returnAudio = responseSearch.getArrMix().get(responseSearch.getPlayIndex())
									.getAudio();
							if (CollectionUtils.isNotEmpty(returnAudio.getArrArtistName())) {
								name = returnAudio.getArrArtistName().get(0) + "的" + returnAudio.getName();
							} else {
								name = returnAudio.getName();
							}
						} else {
							name = responseSearch.getArrMix().get(responseSearch.getPlayIndex()).getAlbum().getName();
						}
					} else {
						TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_PARSE_ERROR", name, true, null);
						return;
					}
				} catch (Exception e) {
					LogUtil.loge(TAG + ":parse error:" + e.toString());
					TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_PARSE_ERROR", name, true, null);
					return;
				}
				TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_WILL_PLAY",
						StringUtils.replace(Constant.RS_VOICE_SPEAK_WILL_PLAY, name),
						new String[] { Constant.PLACEHODLER, name }, true, new Runnable() {
							@Override
							public void run() {
								choiceIndex(responseSearch.getPlayIndex());
							}
						});
			} else
			// 延时播放
			if (responseSearch.getPlayType() == ResponseSearch.DELAYPLAY) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.syncmusiclist",
						array.toString().getBytes(), null);
			} else
			// 选择播放
			if (responseSearch.getPlayType() == ResponseSearch.SELECTPLAY) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.syncmusiclist",
						array.toString().getBytes(), null);
			}
			//
			// if (countAlbum < 2 && countAudio < 2) {
			// LogUtil.logd("countAlbum::" + countAlbum + ",countAudio::"
			// + countAudio);
			// if (countAlbum > 0) {// 一个专辑
			// final ReqAlbumAudio reqData = new ReqAlbumAudio();
			//
			// if (responseSearch.getArrAlbum().get(0) == null) {
			// TtsUtil.speakTextOnRecordWin(
			// Constant.SPEAK_NODATAFOUND_TIPS, false, null);
			// return;
			// }
			//
			// if (MediaPlayerActivityEngine.getInstance()
			// .getCurrentAlbum() != responseSearch.getArrAlbum()
			// .get(0).getId()) {
			// MediaPlayerActivityEngine.getInstance()
			// .setCurrentAlbumName(
			// responseSearch.getArrAlbum().get(0)
			// .getName());
			// //
			// MediaPlayerActivityEngine.getInstance().setAlbumId(responseSearch.getArrAlbum().get(index).getId());
			// reqData.setSid(responseSearch.getArrAlbum().get(0)
			// .getSid());
			// //
			// MediaPlayerActivityEngine.getInstance().setAlbumId(album2.getId());
			// reqData.setId(responseSearch.getArrAlbum().get(0)
			// .getId());
			// reqData.setOffset(Constant.PAGECOUNT);
			// List<Integer> arrCategoryIds = responseSearch
			// .getArrAlbum().get(0).getArrCategoryIds();
			// if (null != arrCategoryIds && arrCategoryIds.size() > 0) {
			// reqData.setCategoryId(StringUtils
			// .toString(arrCategoryIds));
			// Constant.categoryID = StringUtils
			// .toString(arrCategoryIds);
			// } else {
			// LogUtil.logd("playmusiclist.index::"
			// + arrCategoryIds);
			// }
			// MediaPlayerActivityEngine.getInstance().stop();
			//
			// TtsUtil.speakTextOnRecordWin(
			// "即将播放"
			// + responseSearch.getArrAlbum().get(0)
			// .getName(), true,
			// new Runnable() {
			//
			// @Override
			// public void run() {
			// NetHelp.sendRequest(
			// Constant.GET_ALBUM_AUDIO,
			// reqData);
			// Utils.jumpTOMediaPlayerAct(false);
			// }
			// });
			// }
			// } else if (countAudio > 0) {
			// List<Audio> findAll = HistoryAudioDBHelper.getInstance()
			// .findAll(Audio.class, " sid%2=0 ", null, null);
			// findAll.addAll(0, responseSearch.getArrAudio());
			// MediaPlayerActivityEngine.getInstance().setAudios(findAll,
			// 0);
			// MediaPlayerActivityEngine.getInstance()
			// .setCurrentAlbumName("");
			// String tts = "即将播放"
			// + StringUtils.toString(responseSearch.getArrAudio()
			// .get(0).getArrArtistName()) + "的"
			// + responseSearch.getArrAudio().get(0).getName();
			// TtsUtil.speakTextOnRecordWin(tts, true, new Runnable() {
			//
			// @Override
			// public void run() {
			// LogUtil.logd("startxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
			// MediaPlayerActivityEngine.getInstance().start(0);
			// Utils.jumpTOMediaPlayerAct(false);
			// }
			// });
			// }
			// return;
			// }
		}
		// currentTryNum = 0;
		// if (isPlayRandom) {
		// MediaPlayerActivityEngine.getInstance().setAudios(
		// responseSearch.getArrAudio(), 0);
		//
		// MediaPlayerActivityEngine.getInstance().play(Constant.QQINT);
		// } else {
		// ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
		// "txz.music.syncmusiclist", array.toString().getBytes(),
		// null);
		// }
		// AppLogic.runOnBackGround(sendDataToCore, 0);

	}

	/**
	 * 专辑下音频数据的操作
	 */
	private void doAlbumAudio() {
		ResponseAlbumAudio audio = JsonHelper.toObject(ResponseAlbumAudio.class, sData);
		if (audio.getPageId() == 1 && CollectionUtils.isEmpty(audio.getArrAudio())) {
			MonitorUtil.monitorCumulant(Constant.M_EMPTY_AUDIO);
			TtsUtil.speakResource("RS_VOICE_SPEAK_NOAUDIOS_TIPS", Constant.RS_VOICE_SPEAK_NOAUDIOS_TIPS);
			return;
		}

		// Album album=new Album();
		// album.setId(audio.getId());
		// album.setSid(audio.getSid());
		// album.setCategoryID(audio.getCategoryId());
		// PlayEngineFactory.getEngine().setCurrentAlbum(album);
		Album album = (Album) AlbumDBHelper.getInstance().findOne(null,
				AlbumDBHelper.TABLE_ID + " ==? and " + AlbumDBHelper.TABLE_SID + " ==? ",
				new String[] { "" + audio.getId(), "" + audio.getSid() });

		for (int i = 0; i < audio.getArrAudio().size(); i++) {
			if (audio.getArrAudio().get(i) != null) {
				audio.getArrAudio().get(i).setStrCategoryId(String.valueOf(audio.getCategoryId()));
				audio.getArrAudio().get(i).setAlbumId(String.valueOf(audio.getId()));
			}
		}
		// TODO:页数被注释，是否有必要提到接口
		MediaPlayerActivityEngine.getInstance().setCurrentPage(audio.getPageId());
		// PlayEngineFactory.getEngine().setCurrentPage(audio.getPageId());

		if (audio.getPageId() == 1) {
			MediaPlayerActivityEngine.getInstance().setAudios(audio.getArrAudio(), 0);
			MediaPlayerActivityEngine.getInstance().playOrPause();
		} else {
			MediaPlayerActivityEngine.getInstance().addAudios(audio.getArrAudio(),true);
		}
	}

	/**
	 * 专辑列表数据处理
	 */
	private void doAlbumList() {
		try {
			ResponseSearchAlbum albumListAlbum = JsonHelper.toObject(ResponseSearchAlbum.class, sData);
			ObserverManage.getObserver().send(InfoMessage.RESP_ALBUM, albumListAlbum);
			// if (StringUtils.isNotEmpty(albumListAlbum.getCategoryId())
			// && albumListAlbum.getCategoryId().equals(
			// MusicFragment.mCategory.getCategoryId())) {
			// Constant.categoryID = albumListAlbum.getCategoryId();
			// // MusicFragment.setPageOff(
			// // albumListAlbum.getPageId());
			// // MusicFragment.getInstance().refreshData(
			// // albumListAlbum.getArrAlbum());
			//
			// // ObserverManage.getObserver().send
			// } else {
			// LogUtil.logd("albumListAlbum.getCategoryId()::"
			// + albumListAlbum.getCategoryId()
			// + ",MusicFragment.mCategory.getCategoryId()"
			// + MusicFragment.mCategory.getCategoryId());
			// }
		} catch (Exception e) {
			TtsUtil.speakResource("RS_VOICE_SPEAK_JSONERR_TIPS", Constant.RS_VOICE_SPEAK_JSONERR_TIPS);
			e.printStackTrace();
			LogUtil.loge("data::error::" + e.getMessage());
		}
	}

	/**
	 * 分类数据处理
	 */
	private void doCategory() {
		Homepage<Category> homepage = JsonHelper.toObject(sData,
				new com.google.gson.reflect.TypeToken<Homepage<Category>>() {
				}.getType());
		resultCategory(homepage);
	}

	/**
	 * 错误处理
	 * 
	 * @param dataInterface
	 * @return
	 */
	private boolean doErrorCode(final Resp_DataInterface dataInterface) {
		JSONBuilder builder = new JSONBuilder(sData);
		int val = builder.getVal("errCode", int.class, 0);

		if (dataInterface.uint32ErrCode > 0 || val > 0) {
			MonitorUtil.monitorCumulant(
					Constant.M_TIMEOUT_REQ + dataInterface.strCmd.replaceAll("/", "").substring(0, 7).concat(
							String.valueOf(dataInterface.uint32ErrCode > 0 ? dataInterface.uint32ErrCode : val)));

			ObserverManage.getObserver().send(InfoMessage.NET_TIMEOUT_ERROR, dataInterface);

			if (dataInterface.uint32ErrCode <= 8) {// 没有找到服务，业务不可达
				if (dataInterface.uint32ErrCode == 5 || val == 5) {
					// 重新请求

					// if (Constant.GET_CATEGORY.equals(dataInterface.strCmd)) {
					// HomepageFragment.getInstance().sendRequest();
					// } else {
					// LogUtil.logd("no login media server and errcode is " +
					// 5);
					// }
					AppLogic.runOnUiGround(new Runnable() {

						@Override
						public void run() {
							if (!Constant.GET_PROCESSING.equals(dataInterface.strCmd)) {
								ToastUtils.showShort(Constant.RS_VOICE_MUSIC_SPEAK_NOT_LOGIN);
							}
						}
					}, 0);
				}
				ShowErrorTips(dataInterface.strCmd, dataInterface.uint32Seq, Constant.RS_VOICE_SPEAK_SEAVERERR_TIPS);
			} else {// 客户端请求超时，本地错误

				ShowErrorTips(dataInterface.strCmd, dataInterface.uint32Seq, Constant.RS_VOICE_SPEAK_NETNOTCON_TIPS);
			}
			return true;
		}
		return false;
	}

	private void ShowErrorTips(String cmd, int seqID, String tips) {
		LogUtil.logw(TAG + "[Timeout] timeout:cmd=" + cmd + ",seqID=" + seqID + ",manulID=" + Constant.ManualSessionID
				+ ",soundID=" + Constant.SoundSessionID);
		if (Constant.GET_FAKE_SEARCH.equals(cmd) || Constant.GET_STATS.equals(cmd) || Constant.GET_TAG.equals(cmd)
		/* || Constant.GET_CATEGORY.equals(cmd) */) {
			LogUtil.logw("not tips error:" + cmd);
		} else {
			if (Constant.GET_ALBUM_AUDIO.equals(cmd)) {
				if (Constant.ManualSessionID == seqID) {
					MediaPlayerActivityEngine.getInstance().showNetTimeOutError();
				}
			} else if (Constant.GET_SEARCH.equals(cmd)) {
				if (Constant.SoundSessionID == seqID) {// 如果当前搜索界面还存在的话
					TtsUtil.speakTextOnRecordWin(Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT, true, null);
				}
				return;
			} else if (Constant.GET_SEARCH_LIST.equals(cmd) || Constant.GET_CATEGORY.equals(cmd)) {
				if (Constant.ManualSessionID == seqID) {
					// MusicFragment.getInstance().showHiddenNetTimeOutView();
				}
			}
			// 此时不应该
			if (!Constant.GET_PROCESSING.equals(cmd)) {
				TXZResourceManager.getInstance().dissmissRecordWin();
			}
		}
	}

	/**
	 * 对Category请求返回结果进行处理
	 * 
	 * @param homepage
	 */
	private void resultCategory(Homepage<Category> homepage) {
		RespCheck check = new RespCheck();
		check.setLogoTag(tag);
		ArrayList<Category> arrCategory = (ArrayList<Category>) homepage.getArrCategory();
		if (homepage.getReqType() != 0) {
			ObserverManage.getObserver().send(InfoMessage.REQ_CATEGORY_SINGLE, homepage);
			ReqSearchAlbum album = new ReqSearchAlbum();
			// 当前有没有分类
			// if (MusicFragment.mCategory.getCategoryId()
			// .equals(String.valueOf(homepage.getReqType()))
			// && CollectionUtils.isNotEmpty(arrCategory)) {
			// if (CollectionUtils
			// .isNotEmpty(arrCategory.get(0).getArrChild())) {
			// album.setCategoryId(arrCategory.get(0).getArrChild().get(0)
			// .getCategoryId());
			// MusicFragment.getInstance().showFilterData(
			// arrCategory.get(0).getArrChild());
			// } else {
			// MusicFragment.getInstance().showNodataView();
			// return;
			// }
			// } else {
			// MonitorUtil.monitorCumulant(Constant.M_EMPTY_CATEGORY);
			// album.setCategoryId(homepage.getReqType());
			// }
			// if (NetHelp.sendRequest(Constant.GET_SEARCH_LIST, album) == 0) {
			// MusicFragment.getInstance().showHiddenNetTimeOutView();
			// }
		} else {
			if (CollectionUtils.isNotEmpty(arrCategory)) {
				ObserverManage.getObserver().send(InfoMessage.REQ_CATEGORY_ALL, arrCategory);
			}
		}
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

	public void jump(List<Audio> audios) {
		if (CollectionUtils.isNotEmpty(audios)) {
			isLocal = true;
			// MediaPlayerActivityEngine.getInstance().setCurrentAlbum(
			// Utils.toLong(audios.get(0).getAlbumId()));
			// MediaPlayerActivityEngine.getInstance().setCurrentAlbumName(
			// audios.get(0).getAlbumName());
			MediaPlayerActivityEngine.getInstance().setCurrentPage(audios.size() % Constant.PAGECOUNT == 0
					? audios.size() / Constant.PAGECOUNT : audios.size() / Constant.PAGECOUNT + 1);
			MediaPlayerActivityEngine.getInstance().setAudios(audios, 0);
			MediaPlayerActivityEngine.getInstance().playOrPause();
		}
	}
}

package com.txznet.music.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.os.SystemClock;

import com.google.protobuf.nano.MessageNano;
import com.txz.report_manager.ReportManager;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.bean.req.ReqAlbumAudio;
import com.txznet.music.bean.req.ReqDataStats;
import com.txznet.music.bean.req.ReqDataStats.Action;
import com.txznet.music.bean.req.ReqDataStats.ReportInfo;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.fragment.MusicFragment;

public class NetHelp {
	private static final String TAG = "[MUSIC][REQUEST]";
	public static Map<String, Map<String, byte[]>> maps = new ConcurrentHashMap<String, Map<String, byte[]>>();
	// cmd,time,seqID
	public static Map<String, Map<Integer, Long>> mTimeOutmaps = new ConcurrentHashMap<String, Map<Integer, Long>>();
	private static boolean playTip = true;
	private static long lastRequestTime = 0;
	private static int curSeq = new Random().nextInt();

	private static int getNewSeq() {
		curSeq++;
		if (curSeq == 0) {
			curSeq++;
		}
		return curSeq;
	}

	/**
	 * 超时的检测
	 */
	public static Runnable checkTimeOut = new Runnable() {

		@Override
		public void run() {
			AppLogic.removeSlowGroundCallback(this);
			Set<String> keySet = mTimeOutmaps.keySet();
			Iterator<String> iterator = keySet.iterator();
			while (iterator.hasNext()) {
				String cmd = (String) iterator.next();
				// 取出ID值，和时间
				Map<Integer, Long> timeIner = mTimeOutmaps.get(cmd);
				Iterator<Integer> seqIDs = timeIner.keySet().iterator();
				if (seqIDs.hasNext()) {
					int seqID = seqIDs.next();
					Long startTime = timeIner.get(seqID);
					if (SystemClock.elapsedRealtime() - startTime > Constant.REQTIMEOUT) {// 如果超过35s，则认为超时
						LogUtil.logw("timeout:cmd=" + cmd + ",seqID=" + seqID
								+ ",manulID=" + Constant.ManualSessionID
								+ ",soundID=" + Constant.SoundSessionID);
						mTimeOutmaps.remove(cmd);
						if (Constant.GET_ALBUM_AUDIO.equals(cmd)) {
							if (Constant.ManualSessionID == seqID) {
								MediaPlayerActivityEngine.getInstance()
										.showNetTimeOutError();
							}
						} else if (Constant.GET_SEARCH.equals(cmd)) {
							if (Constant.SoundSessionID == seqID) {// 如果当前搜索界面还存在的话
								TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_TIPS_TIMEOUT",
										Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT, false,
										null);
							}
						} else if (Constant.GET_SEARCH_LIST.equals(cmd)
								|| Constant.GET_CATEGORY.equals(cmd)) {
							if (Constant.ManualSessionID == seqID) {
								ObserverManage.getObserver().send(InfoMessage.NET_TIMEOUT_ERROR);
//								MusicFragment.getInstance()
//										.showHiddenNetTimeOutView();
							}
						}
					}
				}
			}
			AppLogic.runOnSlowGround(this, 5000);
		}
	};

	public static int sendRequest(String url, Object reqData) {

		return sendRequestByData(url, JsonHelper.toJson(reqData).getBytes());
	}

	public static int sendRequestByData(String url, byte[] reqData) {

		Map<String, byte[]> value = new ConcurrentHashMap<String, byte[]>();
		Map<Integer, Long> timeInerMap = new ConcurrentHashMap<Integer, Long>();

		long startTime = SystemClock.elapsedRealtime();

		com.txz.ui.audio.UiAudio.Req_DataInterface dataInterface = new com.txz.ui.audio.UiAudio.Req_DataInterface();
		dataInterface.strCmd = url;
		dataInterface.strData = reqData;
		dataInterface.uint32Seq = getNewSeq();
		if (Utils.isNetworkConnected(GlobalContext.get())) {
//			// 耦合代码，要分离
//			if (Constant.GET_ALBUM_AUDIO.equals(url)) {
//				ReqAlbumAudio reqAlbumAudio = JsonHelper.toObject(
//						ReqAlbumAudio.class, new String(reqData));
//				SharedPreferencesUtils.setCurrentAlbumID(reqAlbumAudio.getId());
//			}

			timeInerMap.put(dataInterface.uint32Seq,
					SystemClock.elapsedRealtime());
			// 只有这三个需要记录当前的请求ID，客户端只支持记录最近一次的seqID
			if (dataInterface.strCmd.equals(Constant.GET_ALBUM_AUDIO)
					|| dataInterface.strCmd.equals(Constant.GET_CATEGORY)
					|| dataInterface.strCmd.equals(Constant.GET_SEARCH_LIST)) {
				Constant.ManualSessionID = dataInterface.uint32Seq;
			}
			if (!Constant.GET_STATS.equals(url) && Constant.ISTESTDATA) {
				LogUtil.logd(TAG+"[url]:" + url + ",data::"
						+ new String(reqData) + " ,curseq::"
						+ dataInterface.uint32Seq);
			} else {
				LogUtil.logd(TAG + "[url]:" + url);
			}

			mTimeOutmaps.put(dataInterface.strCmd, timeInerMap);
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.music.dataInterface",
					MessageNano.toByteArray(dataInterface), null);
			return dataInterface.uint32Seq;
		} else {
			LogUtil.logd(TAG + "not net :" + url);
			if (Constant.GET_TAG.equals(url) || Constant.GET_STATS.equals(url)) {
				return 0;
			}
			value.put(url, reqData);
			maps.put("cmd", value);
			if (Constant.GET_SEARCH.equals(url)) {
				TtsUtil.speakResource("RS_VOICE_SPEAK_NONE_NET", Constant.RS_VOICE_SPEAK_NONE_NET);
				AppLogic.runOnUiGround(new Runnable() {
					
					@Override
					public void run() {
						ToastUtils.showShort(Constant.RS_VOICE_SPEAK_NONE_NET);
					}
				}, 0);
				return dataInterface.uint32Seq;
			}
			
			// AppLogic.runOnBackGround(requestAgain, 5000);
			if (playTip && (startTime - lastRequestTime > 5000 * 1000)) {// 五秒内才支持说法
				// TtsUtil.speakText(Constant.SPEAK_NETNOTCON_TIPS);
			}
			lastRequestTime = startTime;

			ObserverManage.getObserver().send(InfoMessage.NET_ERROR,
					dataInterface);

			return 0;
		}
	}

	public static Runnable requestAgain = new Runnable() {

		@Override
		public void run() {
			AppLogic.removeBackGroundCallback(requestAgain);
			playTip = false;
			Map<String, byte[]> map = maps.get("cmd");
			if (null != map) {
				for (String cmd : map.keySet()) {
					LogUtil.logd(TAG + "[requestAgain]");
					sendRequestByData(cmd, map.get(cmd));
				}
			}
		}
	};

	public static void sendReportData(Action actionName) {
		if (MediaPlayerActivityEngine.getInstance().getCurrentAudio() != null) {
			Audio currentAudio = MediaPlayerActivityEngine.getInstance()
					.getCurrentAudio();
			sendReportData(currentAudio.getId(), currentAudio.getSid(),
					currentAudio.getDuration(), MediaPlayerActivityEngine
							.getInstance().getCurrentPercent(),
					StringUtils.toString(currentAudio.getArrArtistName()),
					currentAudio.getName(), actionName);
		} else {
			sendReportData(0, 0, 0, 0, "", "", actionName);
		}
	}

	public static void sendReportData(long id, int sid, long duration,
			float currentPercent, String artists, String title,
			Action actionName) {

		ReportInfo reportInfo = new ReportInfo(id, sid, duration,
				currentPercent, artists, title);
		ReqDataStats dataStats = new ReqDataStats(reportInfo,
				SystemClock.elapsedRealtime(), actionName);
		ReportUtil.doReport(ReportManager.UAT_MUSIC,
				JsonHelper.toJson(dataStats));
	}
}

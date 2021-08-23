package com.txznet.txz.component.nav.gaode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.amap.api.services.core.LatLonPoint;
import com.txz.ui.map.UiMap;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.gaode.NavAmapControl.IAmapNavContants;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.PoiQuery.Option;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.PoiQuery.PoiQueryResultListener;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.RoadInfo.PathInfo;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.WayPoiData.WayPoi;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.runnables.Runnable1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;

public class NavAmapValueService implements IAmapNavContants {
	public static final String TASK_RECV_PLAN_ID = "TASK_RECV_PLAN_ID";
	public static final String TASK_CONTINUE_NAVI_ID = "TASK_CONTINUE_NAVI_ID";
	public static final String TASK_PLAN_FAIL = "TASK_PLAN_FAIL";
	public static final String TASK_PARKER_ID = "TASK_PARKER_ID";

	int sSpeechId = TtsManager.INVALID_TTS_TASK_ID;
	Map<String, SelectAsr> mAsrMap = new HashMap<String, SelectAsr>();
	private RoadInfo mRoadInfo;
	private boolean mIsEnableWakeup = true;

	private static NavAmapValueService sService = new NavAmapValueService();

	private NavAmapValueService() {
		try {
			GlobalContext.get().registerReceiver(new TestRecv(), new IntentFilter("ACTION_TEST_QUERY_POI"));
			GlobalContext.get().registerReceiver(new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					try {
						mAmapRecvRun.update(intent);
						AppLogic.removeBackGroundCallback(mAmapRecvRun);
						AppLogic.runOnBackGround(mAmapRecvRun, 0);
					} catch (Exception e) {
						JNIHelper.logw(e.toString());
					}
				}
			}, new IntentFilter(IAmapNavContants.RECV_ACTION));
			// TODO 指定导航工具的时候才开始查询
//			startQueryHomeCompany();
		} catch (Exception e) {
		}
	}
	
	Runnable1<Intent> mAmapRecvRun = new Runnable1<Intent>(null) {

		@Override
		public void run() {
			if (mP1 != null) {
				notifyReceive(mP1);
			}
		}
	};

	private boolean processBySence(int keyType) {
		JSONBuilder jb = new JSONBuilder();
		jb.put("sence", "nav");
		jb.put("action", "amapRecv");
		jb.put("key", keyType);
		if (SenceManager.getInstance().noneedProcSence("nav", jb.toBytes())) {
			return true;
		}
		return false;
	}

	public static NavAmapValueService getInstance() {
		return sService;
	}

	public void enableWakeup(boolean enableWakeup) {
		mIsEnableWakeup = enableWakeup;
	}

	private void notifyReceive(Intent intent) {
		int keyType = intent.getIntExtra("KEY_TYPE", -1);
		JNIHelper.logd("recv keyType:" + keyType);

		for (NavAmapRecvListener listener : mRecvListeners) {
			listener.onReceive(intent);
		}

		doNavAmapRecv(intent, keyType);
	}

	private void doNavAmapRecv(Intent intent, int key_type) {
		if (key_type == 10042) { // 离线搜索返回
			PoiQuery.getInstance().onResult(intent.getStringExtra("EXTRA_RESULT"));
		}

		if (key_type == 10043) { // 离线搜索返回
			PoiQuery.getInstance().onResult(intent.getStringExtra("EXTRA_RESULT"));
		}

		if (key_type == 10046) { // 通知家和公司的地址
			doKeyType10046(intent);
		}

		if (key_type == 10049 && mIsEnableWakeup) { // 续航信息
			if (processBySence(key_type)) {
				return;
			}
			doKeyType10049(intent);
		}

		if (key_type == 10050 && mIsEnableWakeup) {// 手机发送的位置信息导航
			if (processBySence(key_type)) {
				return;
			}
			doKeyType10050(intent);
		}

		if (key_type == 10051 && mIsEnableWakeup) { // 规划失败
			doKeyType10051(intent);
		}

		if (key_type == 10052 && mIsEnableWakeup) { // 停车场
			doKeyType10052(intent);
		}

		if (key_type == 10056) { // 当前路线信息
			doKeyType10056(intent);
		}

		if (key_type == 10057) { // 沿途搜索数据
			doKeyType10057(intent);
		}

		if (key_type == 10059) {
			doKeyType10059(intent);
		}
	}

	private void doKeyType10046(Intent intent) {
		int category = 0;
		String lat = "";
		String lng = "";
		String addr = "";
		String poiName = "";
		try {
			category = intent.getIntExtra("CATEGORY", -1);
			lat = intent.getStringExtra("LAT");
			lng = intent.getStringExtra("LON");
			addr = intent.getStringExtra("ADDRESS");
			poiName = intent.getStringExtra("POINAME");
		} catch (Exception e1) {
		}

		if (TextUtils.isEmpty(lat) || TextUtils.isEmpty(lng) || TextUtils.isEmpty(addr) || TextUtils.isEmpty(poiName)) {
			Bundle b = intent.getExtras();
			category = b.getInt("CATEGORY", -1);
			try {
				lat = b.getString("LAT");
				lng = b.getString("LON");
			} catch (Exception e) {
			}
			if (TextUtils.isEmpty(lat) || TextUtils.isEmpty(lng)) {
				lat = String.valueOf(b.getDouble("LAT"));
				lng = String.valueOf(b.getDouble("LON"));
			}
			addr = b.getString("ADDRESS");
			poiName = b.getString("POINAME");
		}
		JNIHelper.logd("recv navi :" + category + "," + lat + "," + lng + "," + addr + "," + poiName);

		if (!isLegalGPS(lat, lng)) {
			if (category == 1) {
				NavManager.getInstance().clearHomeLocation();
			}
			if (category == 2) {
				NavManager.getInstance().clearCompanyLocation();
			}
			return;
		}

		double sLat = -1;
		double sLng = -1;
		try {
			sLat = Double.parseDouble(lat);
			sLng = Double.parseDouble(lng);
		} catch (Exception e) {
		}
		if (category == 1) { // 家
			NavManager.getInstance().setHomeLocation(poiName, addr, sLat, sLng, UiMap.GPS_TYPE_GCJ02, false);
		} else if (category == 2) { // 公司
			NavManager.getInstance().setCompanyLocation(poiName, addr, sLat, sLng, UiMap.GPS_TYPE_GCJ02, false);
		}
	}

	private boolean isLegalGPS(String lat, String lng) {
		if (TextUtils.isEmpty(lat) || TextUtils.isEmpty(lng)) {
			return false;
		}

		try {
			Double dLat = Double.parseDouble(lat);
			Double dLng = Double.parseDouble(lng);
			if (dLat == null || dLat == 0) {
				return false;
			}
			if (dLng == null || dLng == 0) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private String[] getResCmdsById(String resId) {
		List<String> cmdList = new ArrayList<String>();
		int i = 0;
		for (;;) {
			String cmd = NativeData.getResString(resId, i);
			if (cmd == null || cmd.length() <= 0) {
				break;
			}
			++i;
			cmdList.add(cmd);
		}
		return cmdList.toArray(new String[cmdList.size()]);
	}

	private void doKeyType10049(Intent intent) {
		String msg = intent.getStringExtra("EXTRA_ENDURANCE_DATA");
		JNIHelper.logd("recv 10049:" + msg);
		SelectAsr mSelectAsr = createSelectAsrById(TASK_CONTINUE_NAVI_ID);
		mSelectAsr.addCmds("SURE", new Runnable() {

			@Override
			public void run() {
				Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
				intent.putExtra("KEY_TYPE", 10049);
				intent.putExtra("EXTRA_ENDURANCE_DATA", true);
				GlobalContext.get().sendBroadcast(intent);
				TtsManager.getInstance().cancelSpeak(sSpeechId);
				String continueNav = NativeData.getResString("RS_MAP_NAV_CONTINUE");
				String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", continueNav);
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(text, null);
			}
		}, getResCmdsById("RS_NAV_GD_CONT_SURE"));
		mSelectAsr.addCmds("CANCEL", new Runnable() {

			@Override
			public void run() {
				Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
				intent.putExtra("KEY_TYPE", 10049);
				intent.putExtra("EXTRA_ENDURANCE_DATA", false);
				GlobalContext.get().sendBroadcast(intent);
				TtsManager.getInstance().cancelSpeak(sSpeechId);
				String cancel = NativeData.getResString("RS_MAP_CONFIRM_CANCEL_ASR");
				String text = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", cancel);
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(text, null);
			}
		}, getResCmdsById("RS_NAV_GD_CONT_CANCEL"));

		if (!TextUtils.isEmpty(msg)) {
			mSelectAsr.build();
			TtsManager.getInstance().cancelSpeak(sSpeechId);
			String spk = NativeData.getResPlaceholderString("RS_MAP_NAV_OR_CANCEL", "%CMD%", msg);
			sSpeechId = TtsManager.getInstance().speakVoice(spk, TtsManager.BEEP_VOICE_URL);
		} else {
			destorySelectAsrTask(TASK_CONTINUE_NAVI_ID);
		}
	}

	public void destorySelectAsrTask(String taskId) {
		TtsManager.getInstance().cancelSpeak(sSpeechId);
		sSpeechId = TtsManager.INVALID_TTS_TASK_ID;
		SelectAsr asr = mAsrMap.get(taskId);
		if (asr != null) {
			asr.destory();
			mAsrMap.remove(taskId);
		}
	}

	private void doKeyType10050(Intent intent) {
		SelectAsr mSelectAsr = createSelectAsrById(TASK_RECV_PLAN_ID);
		String des = intent.getStringExtra("EXTRA_SEND2CAR_DATA");
		if (!TextUtils.isEmpty(des)) {
			mSelectAsr.addCmds("NAVI_TO_POS", new Runnable() {

				@Override
				public void run() {
					NavManager.getInstance().addNavThirdAppPlanEndRunnable(new Runnable() {

						@Override
						public void run() {
							NavThirdApp nta = NavManager.getInstance().getLocalNavImpl();
							if (nta != null && nta instanceof NavAmapAutoNavImpl) {
								((NavAmapAutoNavImpl) nta).startNavByInner();
							}
						}
					});

					String navFail = NativeData.getResString("RS_MAP_NAV_FAIL");
					NavManager.getInstance().setSpeechAfterPlanError(true, navFail);
					TtsManager.getInstance().cancelSpeak(sSpeechId);
					String navTo = NativeData.getResString("RS_MAP_NAV_TO");
					String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", navTo);
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(text, null);
					Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
					intent.putExtra("KEY_TYPE", 10050);
					intent.putExtra("EXTRA_SEND2CAR_DATA", true);
					GlobalContext.get().sendBroadcast(intent);
				}
			}, getResCmdsById("RS_NAV_GD_NAV_DIRECT"));
			mSelectAsr.addCmds("NAVI_TO_CANCEL", new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
					intent.putExtra("EXTRA_SEND2CAR_DATA", false);
					intent.putExtra("KEY_TYPE", 10050);
					GlobalContext.get().sendBroadcast(intent);
					TtsManager.getInstance().cancelSpeak(sSpeechId);
					String cancel = NativeData.getResString("RS_MAP_CONFIRM_CANCEL_ASR");
					String text = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", cancel);
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(text, null);
				}
			}, getResCmdsById("RS_NAV_GD_NAV_DIRECT_CANCEL"));
			mSelectAsr.build();
			TtsManager.getInstance().cancelSpeak(sSpeechId);
			des = des.replace("收到位置", "");
			String spk = NativeData.getResPlaceholderString("RS_MAP_NAV_PHONE", "%LOCATION%", des);
			sSpeechId = TtsManager.getInstance().speakVoice(spk, TtsManager.BEEP_VOICE_URL);
		} else {
			destorySelectAsrTask(TASK_RECV_PLAN_ID);
		}
	}

	private void doKeyType10051(Intent intent) {
		String jsonMsg = intent.getStringExtra("EXTRA_CALCULATED_FAIL_OPTION_DATA");
		SelectAsr mSelectAsr = createSelectAsrById(TASK_PLAN_FAIL);
		mSelectAsr.addCmds("NAV_TO_REPLAN", new Runnable() {

			@Override
			public void run() {
				Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
				intent.putExtra("KEY_TYPE", 10051);
				intent.putExtra("EXTRA_CALCULATED_FAIL_OPTION_DATA", true);
				GlobalContext.get().sendBroadcast(intent);
				String text = NativeData.getResString("RS_VOICE_NAV_REPLAN_AGAIN");
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(text, null);
			}
		}, getResCmdsById("RS_NAV_GD_REPLAN_SURE"));
		mSelectAsr.addCmds("NAV_TO_CANCEL", new Runnable() {

			@Override
			public void run() {
				Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
				intent.putExtra("KEY_TYPE", 10051);
				intent.putExtra("EXTRA_CALCULATED_FAIL_OPTION_DATA", false);
				GlobalContext.get().sendBroadcast(intent);
				String cancel = NativeData.getResString("RS_MAP_CONFIRM_CANCEL_ASR");
				String text = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", cancel);
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(text, null);
			}
		}, getResCmdsById("RS_NAV_GD_REPLAN_CANCEL"));
		mSelectAsr.build();
		sSpeechId = TtsManager.getInstance().speakVoice(jsonMsg, TtsManager.BEEP_VOICE_URL);
	}

	private void doKeyType10052(Intent intent) {
		SelectAsr mSelectAsr = createSelectAsrById(TASK_PARKER_ID);
		try {
			String json = intent.getStringExtra("EXTRA_PARK_DATA");
			if (!TextUtils.isEmpty(json)) {
				JSONArray jsonArray = new JSONArray(json);
				int distanceIndex = -1;
				int lastDistance = -1;
				int priceIndex = -1;
				int lastPrice = -1;
				for (int i = 0; i < jsonArray.length(); i++) {
					final int index = i;
					String strIndex = NativeData.getResString("RS_VOICE_DIGITS", index + 1);
					mSelectAsr.addCmds("TYPE_INDEX_" + strIndex, new Runnable() {

						@Override
						public void run() {
							Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
							intent.putExtra("KEY_TYPE", 10052);
							intent.putExtra("EXTRA_PARK_DATA", index);
							GlobalContext.get().sendBroadcast(intent);
							AsrManager.getInstance().setNeedCloseRecord(true);
							RecorderWin.speakTextWithClose("", null);
						}
					}, "第" + strIndex + "个", "第" + strIndex + "条");
					JSONObject jo = (JSONObject) jsonArray.get(i);
					if (jo.has("parkDistance")) {
						int distance = jo.getInt("parkDistance");
						if (distanceIndex < 0 || distance < lastDistance) {
							distanceIndex = i;
							lastDistance = distance;
						}
					}

					if (jo.has("parkPrice")) {
						int price = jo.getInt("parkDistance");
						if (priceIndex < 0 || price < lastPrice) {
							priceIndex = i;
							lastPrice = price;
						}
					}
				}

				if (distanceIndex != -1) {
					JNIHelper.logd("distanceIndex:" + distanceIndex);
					mSelectAsr.addCmds("DISTANCE_INDEX", new Runnable1<Integer>(distanceIndex) {

						@Override
						public void run() {
							Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
							intent.putExtra("KEY_TYPE", 10052);
							intent.putExtra("EXTRA_PARK_DATA", mP1);
							GlobalContext.get().sendBroadcast(intent);
							AsrManager.getInstance().setNeedCloseRecord(true);
							RecorderWin.speakTextWithClose("", null);
						}
					}, getResCmdsById("RS_NAV_GD_PARKER_LESSDISTANCE"));
				}
				if (priceIndex != -1) {
					JNIHelper.logd("priceIndex:" + priceIndex);
					mSelectAsr.addCmds("PRICE_INDEX", new Runnable1<Integer>(priceIndex) {

						@Override
						public void run() {
							Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
							intent.putExtra("KEY_TYPE", 10052);
							intent.putExtra("EXTRA_PARK_DATA", mP1);
							GlobalContext.get().sendBroadcast(intent);
							AsrManager.getInstance().setNeedCloseRecord(true);
							RecorderWin.speakTextWithClose("", null);
						}
					}, getResCmdsById("RS_NAV_GD_PARKER_LESSMONEY"));
				}
			}
			mSelectAsr.build();
		} catch (JSONException e) {
			JNIHelper.logw("Parker exception e:" + e.toString());
			destorySelectAsrTask(TASK_PARKER_ID);
		}
	}

	private SelectAsr createSelectAsrById(String taskId) {
		SelectAsr sa = mAsrMap.get(taskId);
		if (sa == null) {
			sa = new SelectAsr();
		}

		sa.destory();
		sa.setTaskId(taskId);
		mAsrMap.put(taskId, sa);
		return sa;
	}

	public void destoryAllSelectAsrs() {
		Set<Entry<String, SelectAsr>> asrset = mAsrMap.entrySet();
		for (Entry<String, SelectAsr> asr : asrset) {
			SelectAsr sa = asr.getValue();
			if (sa != null) {
				sa.destory();
			}
		}
		mAsrMap.clear();
	}

	public void onStateChange(boolean onResume) {
		Set<Entry<String, SelectAsr>> asrset = mAsrMap.entrySet();
		for (Entry<String, SelectAsr> asr : asrset) {
			SelectAsr sa = asr.getValue();
			if (sa != null) {
				if (onResume)
					sa.build();
				else
					sa.onPause();
			}
		}
	}

	private void doKeyType10056(Intent intent) {
		try {
			String json = "";
			JSONObject jo = null;
			try {
				json = intent.getStringExtra("EXTRA_ROAD_INFO");
				JNIHelper.logd("EXTRA_ROAD_INFO:" + json);
				if (TextUtils.isEmpty(json)) {
					return;
				}
				jo = new JSONObject(json);
			} catch (Exception e) {
			}

			if (mRoadInfo != null) {
				mRoadInfo = null;
			}
			mRoadInfo = new RoadInfo();

			if (jo.has("ToPoiName")) {
				mRoadInfo.toPoiName = jo.getString("ToPoiName");
			}
			if (jo.has("ToPoiLongitude")) {
				mRoadInfo.toPoiLng = jo.getDouble("ToPoiLongitude");
			}
			if (jo.has("midPoisNum")) {
				mRoadInfo.midPoisNum = jo.getInt("midPoisNum");
			}
			if (jo.has("FromPoiLongitude")) {
				mRoadInfo.fromPoiLng = jo.getDouble("FromPoiLongitude");
			}
			if (jo.has("FromPoiAddr")) {
				mRoadInfo.fromPoiAddr = jo.getString("FromPoiAddr");
			}
			if (jo.has("FromPoiName")) {
				mRoadInfo.fromPoiName = jo.getString("FromPoiName");
			}
			if (jo.has("pathNum")) {
				mRoadInfo.pathNum = jo.getInt("pathNum");
			}
			if (jo.has("path_info")) {
				mRoadInfo.pathInfo = jo.getString("path_info");
				if (!TextUtils.isEmpty(mRoadInfo.pathInfo)) {
					JSONArray jsonArray = new JSONArray(mRoadInfo.pathInfo);
					PathInfo[] pathInfo = new PathInfo[mRoadInfo.pathNum];
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jo1 = (JSONObject) jsonArray.get(i);
						pathInfo[i] = new PathInfo();
						if (jo1.has("streetNamesSize")) {
							pathInfo[i].streetNamesSize = jo1.getInt("streetNamesSize");
						}
						if (jo1.has("streetNames")) {
							pathInfo[i].streetTxt = jo1.getString("streetNames");
							if (!TextUtils.isEmpty(pathInfo[i].streetTxt)) {
								JSONArray jas = new JSONArray(pathInfo[i].streetTxt);
								String[] roads = new String[pathInfo[i].streetNamesSize];
								for (int k = 0; k < roads.length; k++) {
									roads[k] = (String) jas.get(k);
								}
								pathInfo[i].streetArrays = roads;
							}
						}
						if (jo1.has("method")) {
							pathInfo[i].tag = jo1.getString("method");
						}
					}
				}
			}
			if (jo.has("FromPoiLatitude")) {
				mRoadInfo.fromPoiLat = jo.getDouble("FromPoiLatitude");
			}
			if (jo.has("ToPoiLatitude")) {
				mRoadInfo.toPoiLat = jo.getDouble("ToPoiLatitude");
			}
			if (jo.has("ToPoiAddr")) {
				mRoadInfo.toPoiAddr = jo.getString("ToPoiAddr");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doKeyType10057(Intent intent) {
		// 沿途搜索类别 1：WC；2：ATM；3：维修站；4：加油站
		try {
			String jsonResult = intent.getStringExtra("EXTRA_SEARCH_ALONG_THE_WAY");
			if (!TextUtils.isEmpty(jsonResult)) {
				int count = 0;
				int type = 0;
				WayPoiData wpd = new WayPoiData();
				JSONObject jo = new JSONObject(jsonResult);
				if (jo.has("search_result_size")) {
					count = jo.getInt("search_result_size");
					wpd.wayPoiSize = count;
				}
				if (jo.has("search_type")) {
					type = jo.getInt("search_type");
					wpd.wayPoiType = type;
				}
				if (jo.has("poi_info")) {
					List<WayPoi> wayPois = new ArrayList<WayPoi>();
					JSONArray jsonArray = (JSONArray) jo.get("poi_info");
					for (int i = 0; i < count; i++) {
						WayPoi wayPoi = new WayPoi();
						JSONObject jObj = (JSONObject) jsonArray.get(i);
						if (jObj != null) {
							if (jObj.has("poi_Longitude")) {
								String lng = jObj.getString("poi_Longitude");
								if (!TextUtils.isEmpty(lng)) {
									wayPoi.longitude = Double.parseDouble(lng);
								} else {
									wayPoi.longitude = jObj.getDouble("poi_Longitude");
								}
							}
							if (jObj.has("poi_distance")) {
								String distance = jObj.getString("poi_distance");
								if (!TextUtils.isEmpty(distance)) {
									wayPoi.distance = distance;
								}
							}
							if (jObj.has("poi_Latitude")) {
								String lat = jObj.getString("poi_Latitude");
								if (!TextUtils.isEmpty(lat)) {
									wayPoi.latitude = Double.parseDouble(lat);
								} else {
									wayPoi.latitude = jObj.getDouble("poi_Latitude");
								}
							}
							if (jObj.has("poi_addr")) {
								wayPoi.addr = jObj.getString("poi_addr");
							}
							if (jObj.has("poi_name")) {
								wayPoi.name = jObj.getString("poi_name");
							}

							wayPois.add(wayPoi);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doKeyType10059(Intent intent) {
		int category = intent.getIntExtra("CATEGORY", -1);
		int bSucc = intent.getIntExtra("EXTRA_RESPONSE_CODE", -1);
		switch (category) {
		case 1:
			if (bSucc == 0) {
				querySet(1);
			}
			break;

		case 2:
			if (bSucc == 0) {
				querySet(2);
			}
			break;
		}
	}

	public static class RoadInfo {
		public double fromPoiLat;
		public double fromPoiLng;
		public double toPoiLat;
		public double toPoiLng;
		public int midPoisNum;
		public String fromPoiAddr;
		public String fromPoiName;
		public String toPoiAddr;
		public String toPoiName;
		public String pathInfo;
		public PathInfo[] pathInfos;
		public int pathNum;

		public static class PathInfo {
			public int streetNamesSize;
			public String streetTxt;
			public String[] streetArrays;
			public String tag;
		}

		public Poi getDestinationPoi() {
			Poi poi = new Poi();
			poi.setLat(toPoiLat);
			poi.setLng(toPoiLng);
			poi.setName(toPoiName);
			poi.setGeoinfo(toPoiAddr);
			poi.setSourceType(Poi.POI_SOURCE_GAODE_IMPL);
			return poi;
		}

		public Poi getCurrentPoi() {
			Poi poi = new Poi();
			poi.setLat(fromPoiLat);
			poi.setLng(fromPoiLng);
			poi.setName(fromPoiName);
			poi.setGeoinfo(fromPoiAddr);
			poi.setSourceType(Poi.POI_SOURCE_GAODE_IMPL);
			return poi;
		}
	}

	public static class WayPoiData {
		public int wayPoiSize;
		public int wayPoiType;
		public List<WayPoi> wayPois;

		public static class WayPoi {
			public double latitude;
			public double longitude;
			public String name;
			public String addr;
			public String distance;
		}
	}

	public static class SelectAsr extends AsrComplexSelectCallback {
		public String taskId;
		Map<String, Runnable> keySelectRunMap;

		public SelectAsr setTaskId(String taskId) {
			this.taskId = taskId;
			return this;
		}

		public void addCmds(String type, Runnable selectRun, String... cmds) {
			if (cmds == null || cmds.length <= 0) {
				JNIHelper.logw("addCmds fail cmds is null！");
				return;
			}

			if (keySelectRunMap == null) {
				keySelectRunMap = new HashMap<String, Runnable>();
			}
			keySelectRunMap.put(type, selectRun);
			addCommand(type, cmds);
		}

		public void build() {
			WakeupManager.getInstance().useWakeupAsAsr(this);
		}

		public void onPause() {
			WakeupManager.getInstance().recoverWakeupFromAsr(getTaskId());
		}

		public void destory() {
			if (keySelectRunMap != null) {
				keySelectRunMap.clear();
			}
			if (!TextUtils.isEmpty(getTaskId())) {
				WakeupManager.getInstance().recoverWakeupFromAsr(getTaskId());
				taskId = "";
			}
		}

		@Override
		public boolean needAsrState() {
			return false;
		}

		@Override
		public String getTaskId() {
			return taskId;
		}

		@Override
		public void onCommandSelected(String type, String command) {
			Runnable selectRun = keySelectRunMap.get(type);
			if (selectRun != null) {
				selectRun.run();
			}
			destory();
		}
	}

	/**
	 * 1:表示搜索家,2:表示搜索公司
	 * 
	 * @param hOrc
	 */
	public void querySet(int hOrc) {
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10045);
		intent.putExtra("EXTRA_TYPE", hOrc);
		GlobalContext.get().sendBroadcast(intent);
	}

	/**
	 * 地图标注
	 * 
	 * @param poiName
	 * @param lat
	 * @param lng
	 */
	public void markPoiPos(String poiName, double lat, double lng) {
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10039);
		intent.putExtra("SOURCE_APP", "txz");
		intent.putExtra("POINAME", poiName);
		intent.putExtra("LAT", lat);
		intent.putExtra("LON", lng);
		intent.putExtra("DEV", 0);
		GlobalContext.get().sendBroadcast(intent);
	}

	/**
	 * 选择规划路径 1、2、3
	 * 
	 * @param index
	 */
	public void selectPlanRoute(int index) {
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10055);
		intent.putExtra("EXTRA_CHANGE_ROAD", index);
		GlobalContext.get().sendBroadcast(intent);
	}

	/**
	 * 查询家和公司的地址
	 */
	public void startQueryHomeCompany() {
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				querySet(1);
				querySet(2);
			}
		}, 3000);
	}

	/**
	 * 获取导航目的地
	 * 
	 * @return
	 */
	public Poi getDestinationPoi() {
		if (mRoadInfo != null) {
			return mRoadInfo.getDestinationPoi();
		}
		return null;
	}

	List<NavAmapRecvListener> mRecvListeners = new ArrayList<NavAmapValueService.NavAmapRecvListener>();

	public void addRecvListener(NavAmapRecvListener listener) {
		mRecvListeners.add(listener);
	}

	public static interface NavAmapRecvListener {
		public void onReceive(Intent intent);
	}

	/********************* 离线搜索 ********************/
	public static class PoiQuery {

		private static PoiQuery sQuery = new PoiQuery();

		private PoiQuery() {
		}

		public static PoiQuery getInstance() {
			return sQuery;
		}

		public static interface PoiQueryResultListener {

			/**
			 * 搜索结果回调，使用原始的json数据返回
			 * 
			 * @param strData
			 */
			public void onResult(String strData);

		}

		public static class Option {
			final static int KWS_SEARCH_TYPE = 10023;
			final static int NEAR_SEARCH_TYPE = 10024;
			final static int KWS_RESULT_TYPE = 10042;
			final static int NEAR_RESULT_TYPE = 10043;

			public int searchType;
			public String kws;
			public String city;
			public int searchRaduis = -1;
			public int num;
			public LatLonPoint myPoint;
			public LatLonPoint centerPoi;
			public PoiQueryResultListener mResultListener;

			public Intent genQueryIntent() {
				Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
				if (searchType == KWS_SEARCH_TYPE) {
					intent.putExtra("KEY_TYPE", 10023);
					intent.putExtra("EXTRA_SEARCHTYPE", 0);
				}
				if (searchType == NEAR_SEARCH_TYPE) {
					intent.putExtra("KEY_TYPE", 10024);
					intent.putExtra("EXTRA_SEARCHTYPE", 1);
				}
				intent.putExtra("EXTRA_KEYWORD", kws);
				if (myPoint != null) {
					intent.putExtra("EXTRA_MYLOCLAT", myPoint.getLatitude());
					intent.putExtra("EXTRA_MYLOCLON", myPoint.getLongitude());
				}
				if (searchRaduis != -1) {
					intent.putExtra("EXTRA_RANGE", searchRaduis);
				}
				intent.putExtra("EXTRA_DEV", 0);
				intent.putExtra("EXTRA_CITY", city);
				intent.putExtra("EXTRA_MAXCOUNT", num);
				if (centerPoi != null) {
					intent.putExtra("EXTRA_CENTERLAT", centerPoi.getLatitude());
					intent.putExtra("EXTRA_CENTERLON", centerPoi.getLongitude());
				}
				return intent;
			}
		}

		private Option mTask;

		public void startQuery(Option option) {
			if (!checkSupSearch()) {
				onResult(null);
				return;
			}
			
			// 结束上次搜索的结果(每一次请求不一定对应一次结果返回，连续密集请求只返回一个结果)
			onResult(null);

			mTask = option;
			if (mTask == null) {
				return;
			}

			Intent intent = mTask.genQueryIntent();
			if (intent != null) {
				JNIHelper.logd("query of amap...");
				GlobalContext.get().sendBroadcast(intent);
			}
		}

		private boolean checkSupSearch() {
			int vc = PackageManager.getInstance().getVerionCode(NavAmapAutoNavImpl.PACKAGE_NAME);
			if (vc < 541) {
				vc = PackageManager.getInstance().getVerionCode(NavAmapAutoNavImpl.PACKAGE_NAME_LITE);
			}
			if (vc < 541) {
				return false;
			}
			return true;
		}

		public void cancel() {
			mTask = null;
		}

		void onResult(String jsonData) {
			if (mTask == null) {
				return;
			}

			mTask.mResultListener.onResult(jsonData);
			cancel();
		}
	}

	public void testQuery() {
		Option option = new Option();
		option.city = "深圳市";
		option.kws = "商场";
		option.myPoint = new LatLonPoint(22.537559, 113.951369);
		option.centerPoi = new LatLonPoint(22.537559, 113.951369);
		option.searchRaduis = -1;
		option.searchType = 10024;
		option.mResultListener = new PoiQueryResultListener() {

			@Override
			public void onResult(String strData) {
				JNIHelper.logi("strData:" + strData);
			}
		};
		PoiQuery.getInstance().startQuery(option);
	}

	private class TestRecv extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int cmd = intent.getIntExtra("CMD", -1);
			if (cmd == -1) {
				// 测试搜索功能
				testQuery();
			}
		}
	}
}

package com.txznet.txz.component.nav.txz;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrKeyManager.AsrKeyType;
import com.txznet.sdk.TXZMediaFocusManager;
import com.txznet.sdk.TXZNavManager;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.component.nav.IMapInterface;
import com.txznet.txz.component.nav.INavInquiryRoadTraffic;
import com.txznet.txz.component.nav.NavInfo;
import com.txznet.txz.component.nav.NavThirdComplexApp;
import com.txznet.txz.component.nav.base.BasePathInfo;
import com.txznet.txz.component.nav.base.BaseRoadInfo;
import com.txznet.txz.component.nav.base.SelectAsr;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class NavApiImpl extends NavThirdComplexApp implements INavInquiryRoadTraffic {
	public static final String SEND_ACTION = "com.txznet.txz.nav.comm.send";
	public static final String KEY_TYPE = "KEY_TYPE";
	public static final String SOURCE_APP = "SOURCE_APP";
	public static final String EXTRA_TYPE = "EXTRA_TYPE";
	public static final String EXTRA_STATE = "EXTRA_STATE";
	public static final String EXTRA_OPERATE = "EXTRA_OPERATE";

	public static final String TASK_SELECT_NAVIGATE_ROAD = "TASK_SELECT_NAVIGATE_ROAD";

	public static class RoadInfo extends BaseRoadInfo {
		public int midPoisNum;
		public Poi[] midPoiArray;
	}

	public static class RoadPathInfo extends BasePathInfo {

	}

    /**
     * 是否支持途径点导航，从世界之窗到白石洲
     */
    public static final int FLAG_SUPPORT_NAVIGATE_WITH_FROM_POIS = 0x00000001;
    /**
     * 是否支持播报前方路况
     */
    public static final int FLAG_SUPPORT_FRONT_TRAFFIC = 0x00000002;
    /**
     * 是否支持查询路况
     */
    public static final int FLAG_SUPPORT_INQUIRY_TRAFFIC = 0x00000004;
	/**
	 * 是否支持途径点设置
	 */
	public static final int FLAG_SUPPORT_NAVIGATE_WITH_WAY_POIS = 0x00000008;

    public int mFlags = 0;


	private String mPackageName;

	public NavApiImpl(String packageName, int flags) {
		mPackageName = packageName;
		mFlags = flags;

		checkCurrentState();
	}

	private void checkCurrentState() {
		AppLogic.removeBackGroundCallback(resume);
		AppLogic.removeBackGroundCallback(navi);
		AppLogic.runOnBackGround(resume, 3000);
		AppLogic.runOnBackGround(navi, 4000);
	}

	@Override
	public void exitNav() {
		int delay = 0;
		JSONBuilder jb = new JSONBuilder();
		jb.put("scene", "nav");
		jb.put("action", "exitNoNeedCancel");
		if (!SenceManager.getInstance().noneedProcSence("nav", jb.toBytes())) {
			AppLogic.removeBackGroundCallback(cancelNav);
			AppLogic.runOnBackGround(cancelNav, 0);
			delay = 1000;
		}
		AppLogic.removeBackGroundCallback(exit);
		AppLogic.runOnBackGround(exit, delay);

		onPause();
		onExitApp();
	}

	@Override
	public boolean isInNav() {
		return mIsStarted;
	}

	Runnable cancelNav = new Runnable() {

		@Override
		public void run() {
			Intent intent = new Intent(SEND_ACTION);
			if (!TextUtils.isEmpty(getPackageName())) {
				intent.setPackage(getPackageName());
			}
			intent.putExtra(KEY_TYPE, 10004);
			intent.putExtra(SOURCE_APP, "txz");
			intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			GlobalContext.get().sendBroadcast(intent);
			LogUtil.logd("navapi:cancelNav");
		}
	};

	Runnable exit = new Runnable() {

		@Override
		public void run() {
			Intent intent = new Intent(SEND_ACTION);
			if (!TextUtils.isEmpty(getPackageName())) {
				intent.setPackage(getPackageName());
			}
			intent.putExtra(KEY_TYPE, 10005);
			intent.putExtra(SOURCE_APP, "txz");
			intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			GlobalContext.get().sendBroadcast(intent);
			LogUtil.logd("navapi:exitNav");
		}
	};

	Runnable resume = new Runnable() {

		@Override
		public void run() {
			Intent intent = new Intent(SEND_ACTION);
			if (!TextUtils.isEmpty(getPackageName())) {
				intent.setPackage(getPackageName());
			}
			intent.putExtra(KEY_TYPE, 10013);
			intent.putExtra(SOURCE_APP, "txz");
			intent.putExtra(EXTRA_TYPE, 1);
			intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			GlobalContext.get().sendBroadcast(intent);
			LogUtil.logd("navapi:resume");
		}
	};

	Runnable navi = new Runnable() {

		@Override
		public void run() {
			Intent intent = new Intent(SEND_ACTION);
			if (!TextUtils.isEmpty(getPackageName())) {
				intent.setPackage(getPackageName());
			}
			intent.putExtra(KEY_TYPE, 10013);
			intent.putExtra(SOURCE_APP, "txz");
			intent.putExtra(EXTRA_TYPE, 2);
			intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			GlobalContext.get().sendBroadcast(intent);
			LogUtil.logd("navapi:navi");
		}
	};

	public void handleRecv(Intent intent) {
		int key_type = intent.getIntExtra(KEY_TYPE, -1);
		if (key_type == -1) {
			return;
		}

		switch (key_type) {
		case 10000:
			if (mNavInfo == null) {
				mNavInfo = new NavInfo();
			}

			try {
				mNavInfo.parseBundle(mPackageName, intent.getExtras());
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case 10001:
			parse10001(intent);
			break;
		case 10002:

			break;
		case 10003:

			break;
		case 10004:

			break;
		case 10005:

			break;
		case 10006:

			break;
		case 10007:

			break;
		case 10008:
			parse10008(intent);
			break;
		case 10009:
			parse10009(intent);
			break;
		case 10010:

			break;
		case 10011:

			break;
		case 10012:
			parse10012(intent);
			break;
		case 10013:
			break;
		case 10016:
			parse10016(intent);
			break;
		case 10017:
//			parse10017(intent);微信弃用
			break;
		case 10018:
			parse10018(intent);
			break;
		case 10022:
			parse10022(intent);
			break;
		case 10024:
			parse10024(intent);
			break;
		case 10027:
			parse10027(intent);
			break;
		default:
			break;
		}
	}

	private void parse10001(Intent intent) {
		int state = intent.getIntExtra(EXTRA_STATE, -1);
		JNIHelper.logd("navapi:parse10001 state:" + state);
		switch (state) {
		case 1:
			break;
		case 2:
			onPause();
			break;
		case 3:
			onResume();
			break;
		case 4:
			onPause();
			break;
		case 5:
			break;
		case 6:
			break;
		case 7:
			break;
		case 8:
			onStart();
			break;
		case 9:
			onEnd(false);
			cancelSpeak();
			break;
		case 10:
			TXZMediaFocusManager.getInstance().requestFocus();
			break;
		case 11:
			TXZMediaFocusManager.getInstance().releaseFocus();
			break;
		case 12:
			isPlaning = true;
			break;
		case 13:
			isPlaning = false;
			destroyPlanningSelect();
			break;
		default:
			break;
		}
	}

	private void parse10008(Intent intent) {
		Bundle bundle = intent.getExtras();
		LogUtil.logd("navapi:parse10008:" + bundle);
		if (bundle != null) {
			int category = bundle.getInt("CATEGORY");
			int type = bundle.getInt("TYPE");
			switch (type) {
			case 1:
				String name = bundle.getString("POINAME");
				String address = bundle.getString("ADDRESS");
				double lat = bundle.getDouble("LAT");
				double lng = bundle.getDouble("LON");
				if (category == 1) {
					NavManager.getInstance().setHomeLocation(name, address, lat, lng, UiMap.GPS_TYPE_GCJ02, false);
				} else if (category == 2) {
					NavManager.getInstance().setCompanyLocation(name, address, lat, lng, UiMap.GPS_TYPE_GCJ02, false);
				}
				break;
			case 2:
				if (category == 1) {
					NavManager.getInstance().clearHomeLocation();
				} else if (category == 2) {
					NavManager.getInstance().clearCompanyLocation();
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 收到10009广播，更新途经点信息
	 * @param intent
	 */
	private void parse10009(Intent intent) {
		Bundle bundle = intent.getExtras();
		LogUtil.logd("navapi:parse10009:" + bundle);
		if (bundle != null) {
			String pois = bundle.getString("EXTRA_ROAD_INFO");
			JSONObject poisInfo = null;
			try {
				poisInfo = new JSONObject(pois);
				if (jyList == null) {
					jyList = new ArrayList<Poi>();
				}
				LogUtil.d("jy clear.");
				jyList.clear();
				if (poisInfo.has("midPoiArray")) {
					JSONArray midPoiArray = poisInfo.getJSONArray("midPoiArray");
					for (int i = 0; i < midPoiArray.length(); i++) {
						JSONObject midPoi = (JSONObject) midPoiArray.get(i);
						double lat = midPoi.optDouble("midPoiLat",0.0);
						double lng = midPoi.optDouble("midPoiLng",0.0);
						int distance = 0;
						if (midPoi.has("midDistance")) {	// 协议文档里写的是这个，兼容一下
							distance = midPoi.optInt("midDistance",0);
						} else if(midPoi.has("midPoiDistance")){
							distance = midPoi.optInt("midPoiDistance",0);
						} else {
							if (mPathInfo != null) {
								distance = (int) getDistance(mPathInfo.fromPoiLat, mPathInfo.fromPoiLng, lat, lng);
							}
						}
						Poi poi = new Poi();
						poi.setLat(lat);
						poi.setLng(lng);
						poi.setName(midPoi.optString("midPoiName","null"));
						poi.setDistance(distance);
						jyList.add(poi);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private int mCurrSpeakId;
	private List<Integer> mCurrSpeakTaskList = new ArrayList<Integer>();

	/**
	 * 导航播报文字
	 * @param intent
	 */
	private void parse10012(Intent intent) {
		String spkTts = intent.getStringExtra("EXTRA_TTS");
		Boolean isBreak = intent.getBooleanExtra("EXTRA_BREAK",true);
		if (!TextUtils.isEmpty(spkTts)) {
			if (isBreak) {
				cancelSpeak();
			}

			mCurrSpeakId = TtsManager.getInstance().speakText(spkTts, new TtsUtil.ITtsCallback() {
				@Override
				public void onEnd() {
					mCurrSpeakTaskList.remove(Integer.valueOf(mCurrSpeakId));
				}
			});
			mCurrSpeakTaskList.add(mCurrSpeakId);
		}
	}

	private void cancelSpeak() {
		for (int taskId : mCurrSpeakTaskList) {
			TtsManager.getInstance().cancelSpeak(taskId);
		}
		mCurrSpeakTaskList.clear();
	}

	private void parse10016(Intent intent) {
		Bundle bundle = intent.getExtras();
		LogUtil.logd("bundle:" + bundle);
		mPathInfo = new TXZNavManager.PathInfo();
		mPathInfo.fromPoiAddr = bundle.getString("fromPoiAddr");
		mPathInfo.fromPoiLat = bundle.getDouble("fromPoiLat");
		mPathInfo.fromPoiLng = bundle.getDouble("fromPoiLng");
		mPathInfo.fromPoiName = bundle.getString("fromPoiName");
		mPathInfo.toCity = bundle.getString("toCity");
		mPathInfo.toPoiAddr = bundle.getString("toPoiAddr");
		mPathInfo.toPoiLat = bundle.getDouble("toPoiLat");
		mPathInfo.toPoiLng = bundle.getDouble("toPoiLng");
		mPathInfo.toPoiName = bundle.getString("toPoiName");
		mPathInfo.totalDistance = bundle.getInt("totalDistance");
		mPathInfo.totalTime = bundle.getInt("totalTime");
	}

	@Override
	public boolean speakLimitSpeech() {
		queryLimitSpeed();
		return true;
	}

	private void parse10017(Intent intent) {
		Bundle bundle = intent.getExtras();
		LogUtil.logd("navapi parse10017:" + bundle);
		Poi poi = new Poi();
		poi.setName(bundle.getString("toPoiName"));
		poi.setGeoinfo(bundle.getString("toPoiAddr"));
		poi.setCity(bundle.getString("ToCity"));
		poi.setLat(bundle.getDouble("toPoiLat"));
		poi.setLng(bundle.getDouble("toPoiLng"));
		NavManager.getInstance().sharePoiToWx(poi);
	}


	private int mMaxPointNum = -1;
	private String mHintText = NativeData.getResString("RS_MAP_POINT_TOO_MUCH");
	private void parse10018(Intent intent){
		Bundle bundle = intent.getExtras();
		LogUtil.logd("navapi parse10018:" + bundle);
		mMaxPointNum = bundle.getInt("MAX_POINT_NUM", -1);
		String hintText = bundle.getString("HINT_TEXT");
		if (!TextUtils.isEmpty(hintText)) {
			mHintText = hintText;
		}
	}

	// 路况查询结果
	private void parse10022(Intent intent){
		Bundle bundle = intent.getExtras();
		LogUtil.logd("navapi parse10022:" + bundle);
		int result = bundle.getInt("EXTRA_TRAFFIC_CONDITION_RESULT", 5);
		String message = bundle.getString("EXTRA_TRAFFIC_CONDITION_RESULT_MESSAGE");
		for (OnInquiryRoadTrafficResultListener listener : inquiryRoadTrafficResultListeners) {
			listener.onInquiryRoadTrafficResult(result, message);
		}
	}

	// 前方路况查询结果
	private void parse10024(Intent intent){
		Bundle bundle = intent.getExtras();
		LogUtil.logd("navapi parse10024:" + bundle);
		String message = bundle.getString("EXTRA_LOCATION_TRAFFIC_INFO");
		for (OnInquiryRoadTrafficResultListener listener : inquiryRoadTrafficResultListeners) {
			listener.onInquiryRoadTrafficByFrontResult(message);
		}
	}

	// 当前路线信息透出
	private void parse10027(Intent intent) {
		LogUtil.logd("navapi parse10027");
		try {
			String infoStr = intent.getStringExtra("EXTRA_ROAD_INFO");
			JNIHelper.logd("navapi parse10025 EXTRA_ROAD_INFO: " + infoStr);
			if (TextUtils.isEmpty(infoStr)) {
				return;
			}
			parseRoadInfo(new JSONObject(infoStr));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.logd("navapi parse10027 json error:" + e.getLocalizedMessage());
		}

	}

	private String[] tmpString = {"零","一","二","三","四","五","六","七","八"};
	private Map<Integer, ArrayList<String>> mPlanningWakeUp = new HashMap<Integer, ArrayList<String>>();
	private int roadAsrTtsId = TtsUtil.INVALID_TTS_TASK_ID; // 路线询问tts
	private boolean isPlaning;

	// copy from NavAmapValueService
	private void parseRoadInfo(JSONObject jo) throws JSONException {
		RoadInfo roadInfo = new RoadInfo();
		if (jo.has("toPoiName")) {
			roadInfo.toPoiName = jo.getString("toPoiName");
		}
		if (jo.has("toPoiLng")) {
			roadInfo.toPoiLng = jo.getDouble("toPoiLng");
		}
		if (jo.has("toPoiLat")) {
			roadInfo.toPoiLat = jo.getDouble("toPoiLat");
		}
		if (jo.has("toPoiAddr")) {
			roadInfo.toPoiAddr = jo.getString("toPoiAddr");
		}
		if (jo.has("fromPoiLng")) {
			roadInfo.fromPoiLng = jo.getDouble("fromPoiLng");
		}
		if (jo.has("fromPoiAddr")) {
			roadInfo.fromPoiAddr = jo.getString("fromPoiAddr");
		}
		if (jo.has("fromPoiName")) {
			roadInfo.fromPoiName = jo.getString("fromPoiName");
		}
		if (jo.has("fromPoiLat")) {
			roadInfo.fromPoiLat = jo.getDouble("fromPoiLat");
		}
		if (jo.has("pathNum")) {
			roadInfo.pathNum = jo.getInt("pathNum");
		}
		if (jo.has("pathInfos")) {
			String pathInfo = jo.getString("pathInfos");
			if (!TextUtils.isEmpty(pathInfo)) {
				JSONArray jsonArray = new JSONArray(pathInfo);
				RoadPathInfo[] roadPathInfo = new RoadPathInfo[roadInfo.pathNum];
				mPlanningWakeUp.clear();
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jo1 = (JSONObject) jsonArray.get(i);
					roadPathInfo[i] = new RoadPathInfo();
					if (jo1.has("streetNamesSize")) {
						roadPathInfo[i].streetNamesSize = jo1.getInt("streetNamesSize");
					}
					if (jo1.has("streetNames")) {
						String streetTxt = jo1.getString("streetNames");
						if (!TextUtils.isEmpty(streetTxt)) {
							JSONArray jas = new JSONArray(streetTxt);
							String[] roads = new String[roadPathInfo[i].streetNamesSize];
							for (int k = 0; k < roads.length; k++) {
								roads[k] = (String) jas.get(k);
							}
							roadPathInfo[i].streetArrays = roads;
						}
					}
					if (jo1.has("tag")) {
						roadPathInfo[i].tag = jo1.getString("tag");
					}
					if (isPlaning) {
						ArrayList<String> cmdList = new ArrayList<String>();
						cmdList.add("第" + tmpString[i + 1] + "个");
//							cmdList.add("第"+(i+1)+"个");
						cmdList.add("方案" + (tmpString[i + 1]));
						cmdList.add("路线" + (tmpString[i + 1]));
						if (!TextUtils.isEmpty(roadPathInfo[i].tag) && !roadPathInfo[i].tag.startsWith("路线")) {
							cmdList.add(roadPathInfo[i].tag);
						}
						mPlanningWakeUp.put(i, cmdList);
					}
				}
				if(isPlaning){
					buildPlanningSelect(false);
					if (jsonArray.length() > 1) {
						String spk = NativeData.getResString("RS_VOICE_PLANNING_ROUTE").replace("%NUMBER%",
								"" + jsonArray.length());
						if (planningSelectAsr != null) {
							planningSelectAsr.build();
						}
						roadAsrTtsId = TtsManager.getInstance().speakVoice(spk, TtsManager.BEEP_VOICE_URL,
								(com.txznet.comm.remote.util.TtsUtil.ITtsCallback) null);
					} else if (jsonArray.length() == 1) {
						AppLogic.runOnBackGround(new Runnable() {

							@Override
							public void run() {
								if(isPlaning){
									AppLogic.runOnBackGround(new Runnable() {
										@Override
										public void run() {
											beginNav();
										}
									}, 150);
								}else {
									String spk = NativeData.getResString("RS_VOICE_PLANNING_ROUTE_ONLY");
									roadAsrTtsId = TtsManager.getInstance().speakText(spk, new TXZTtsManager.ITtsCallback() {
										@Override
										public void onSuccess() {
											AppLogic.runOnBackGround(new Runnable() {

												@Override
												public void run() {
													beginNav();
												}
											}, 150);
										}
									});
								}
							}
						},1000);
					}

				}
			}
		}
		roadInfo.printLatLngInfo();
	}

	SelectAsr planningSelectAsr = null;

	public void destroyPlanningSelect() {
		TtsManager.getInstance().cancelSpeak(roadAsrTtsId);
		if(planningSelectAsr != null){
			planningSelectAsr.destory();
			planningSelectAsr = null;
		}
	}

	public void buildPlanningSelect(boolean isBuild){
		if(isPlaning && mPlanningWakeUp.size() > 0 ){
			if (planningSelectAsr != null) {
				planningSelectAsr.destory();
			}
			planningSelectAsr = new SelectAsr();
			planningSelectAsr.setTaskId(TASK_SELECT_NAVIGATE_ROAD);
			Set<Integer> keySet = mPlanningWakeUp.keySet();
			for(final Integer key: keySet){
				final ArrayList<String> arrayList = mPlanningWakeUp.get(key);
				planningSelectAsr.addCmds("SELECT_"+key, new Runnable() {
					@Override
					public void run() {
						if (roadAsrTtsId != TtsManager.INVALID_TTS_TASK_ID) {
							TtsManager.getInstance().cancelSpeak(roadAsrTtsId);
						}
						choicePlanRoad(key);
						mPlanningWakeUp.clear();
						destroyPlanningSelect();
					}
				},arrayList.toArray(new String[arrayList.size()]));
			}
			if(isBuild){
				planningSelectAsr.build();
			}
		}
	}

	// 选择路线
	public void choicePlanRoad(int index){
		Intent intent = new Intent(SEND_ACTION);
		if (!TextUtils.isEmpty(getPackageName())) {
			intent.setPackage(getPackageName());
		}
		intent.putExtra(KEY_TYPE, 10026);
		intent.putExtra(SOURCE_APP, "txz");
		intent.putExtra("EXTRA_CHANGE_ROAD", index + 1);
		intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		GlobalContext.get().sendBroadcast(intent);
		LogUtil.logd("navapi:choicePlanRoad index=" + index);

		beginNav();
		RecorderWin.close();
	}

	// 开始导航
	private void beginNav() {
		Intent intent = new Intent(SEND_ACTION);
		if (!TextUtils.isEmpty(getPackageName())) {
			intent.setPackage(getPackageName());
		}
		intent.putExtra(KEY_TYPE, 10025);
		intent.putExtra(SOURCE_APP, "txz");
		intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		GlobalContext.get().sendBroadcast(intent);
		LogUtil.logd("navapi:beginNav");
	}

	@Override
	public void queryHomeCompanyAddr() {
	}

	@Override
	public String disableProcJingYouPoi() {
		if(jyList == null){
			jyList = new ArrayList<Poi>();
		}
		if (mMaxPointNum != -1 && jyList.size() >= mMaxPointNum) {
			return mHintText;
		}
		return "";
	}

	@Override
	public boolean procJingYouPoi(Poi... pois) {
//		if(!checkPoiIsExist(pois[0])){
//			LogUtil.logd("navapi:procJingYouPoi not exist repeat.");
//			super.procJingYouPoi(pois);
//		}
		super.procJingYouPoi(pois);
		Intent intent = new Intent(SEND_ACTION);
		if (!TextUtils.isEmpty(getPackageName())) {
			intent.setPackage(getPackageName());
		}
		intent.putExtra(KEY_TYPE, 10009);
		intent.putExtra(SOURCE_APP, "txz");
		Bundle bundle = new Bundle();
		bundle.putString("POINAME", pois[0].getName());
		bundle.putString("ADDRESS", pois[0].getGeoinfo());
		bundle.putDouble("LAT", pois[0].getLat());
		bundle.putDouble("LON", pois[0].getLng());
		bundle.putInt("TYPE", 1);
		intent.putExtras(bundle);
		intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		GlobalContext.get().sendBroadcast(intent);
		LogUtil.logd("navapi:procJingYouPoi");
		return true;
	}

	/**
	 * 检测改途经点是否已经存在 存在则不再插入该途经点
	 * @param poi
	 * @return
	 */
	public boolean checkPoiIsExist(Poi poi){
		if (jyList != null && jyList.size() > 0) {
			for (int i = 0; i < jyList.size(); i++) {
				Poi current = jyList.get(i);
				if (current.getLat() == poi.getLat() &&
						current.getLng() == poi.getLng() &&
						current.getName().equals(poi.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String disableDeleteJingYou() {
		return "";
	}

	@Override
	public boolean deleteJingYou(Poi poi) {
		super.deleteJingYou(poi);
		Intent intent = new Intent(SEND_ACTION);
		if (!TextUtils.isEmpty(getPackageName())) {
			intent.setPackage(getPackageName());
		}
		intent.putExtra(KEY_TYPE, 10009);
		intent.putExtra(SOURCE_APP, "txz");
		Bundle bundle = new Bundle();
		bundle.putString("POINAME", poi.getName());
		bundle.putString("ADDRESS", poi.getGeoinfo());
		bundle.putDouble("LAT", poi.getLat());
		bundle.putDouble("LON", poi.getLng());
		bundle.putInt("TYPE", 2);
		intent.putExtras(bundle);
		intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		GlobalContext.get().sendBroadcast(intent);
		LogUtil.logd("navapi:deleteJingYou");
		return true;
	}

	@Override
	public void onNavCommand(boolean fromWakeup, String type, String command) {
		if (AsrKeyType.EXIT_NAV.equals(type) || AsrKeyType.CLOSE_MAP.equals(type)) {
			JSONBuilder json = new JSONBuilder();
			json.put("scene", "nav");
			json.put("text", command);
			json.put("action", "exit");
			if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
				return;
			}
		}

		JNIHelper.logd("onNavCommSelect:[" + fromWakeup + "," + type + "," + command + "," + getPackageName() + "]");
		if (AsrKeyType.ZOOM_IN.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_ZOOMIN", new Runnable() {

				@Override
				public void run() {
					// 放大地图
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10002);
					intent.putExtra(SOURCE_APP, "txz");
					intent.putExtra(EXTRA_TYPE, 2);
					intent.putExtra(EXTRA_OPERATE, 1);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl ZOOM_IN");
				}
			}, false);
			return;
		}
		if (AsrKeyType.ZOOM_OUT.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_ZOOMOUT", new Runnable() {

				@Override
				public void run() {
					// 缩小地图
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10002);
					intent.putExtra(SOURCE_APP, "txz");
					intent.putExtra(EXTRA_TYPE, 2);
					intent.putExtra(EXTRA_OPERATE, 2);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl ZOOM_OUT");
				}
			}, false);
			return;
		}
		if (AsrKeyType.NIGHT_MODE.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_NIGHT_MODE", new Runnable() {

				@Override
				public void run() {
					// 黑夜模式
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10002);
					intent.putExtra(SOURCE_APP, "txz");
					intent.putExtra(EXTRA_TYPE, 4);
					intent.putExtra(EXTRA_OPERATE, 2);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl NIGHT_MODE");
				}
			}, false);
			return;
		}
		if (AsrKeyType.LIGHT_MODE.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_LIGHT_MODE", new Runnable() {

				@Override
				public void run() {
					// 白天模式
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10002);
					intent.putExtra(SOURCE_APP, "txz");
					intent.putExtra(EXTRA_TYPE, 4);
					intent.putExtra(EXTRA_OPERATE, 1);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl LIGHT_MODE");
				}
			}, false);
			return;
		}
		if (AsrKeyType.AUTO_MODE.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_AUTO_MODE", new Runnable() {

				@Override
				public void run() {
					// 自动模式
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10002);
					intent.putExtra(SOURCE_APP, "txz");
					intent.putExtra(EXTRA_TYPE, 4);
					intent.putExtra(EXTRA_OPERATE, 3);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl AUTO_MODE");
				}
			}, false);
			return;
		}
		if (AsrKeyType.OPEN_TRAFFIC.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_OPEN_TRAFFIC", new Runnable() {

				@Override
				public void run() {
					// 打开路况
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10002);
					intent.putExtra(SOURCE_APP, "txz");
					intent.putExtra(EXTRA_TYPE, 1);
					intent.putExtra(EXTRA_OPERATE, 1);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl OPEN_TRAFFIC");
				}
			}, false);
			return;
		}
		if (AsrKeyType.CLOSE_TRAFFIC.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_CLOSE_TRAFFIC", new Runnable() {

				@Override
				public void run() {
					// 关闭路况
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10002);
					intent.putExtra(SOURCE_APP, "txz");
					intent.putExtra(EXTRA_TYPE, 1);
					intent.putExtra(EXTRA_OPERATE, 2);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl CLOSE_TRAFFIC");
				}
			}, false);
			return;
		}
		if (AsrKeyType.TWO_MODE.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_TWO_MODE", new Runnable() {

				@Override
				public void run() {
					// 2D模式
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10002);
					intent.putExtra(SOURCE_APP, "txz");
					intent.putExtra(EXTRA_TYPE, 3);
					intent.putExtra(EXTRA_OPERATE, 2);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl TWO_MODE");
				}
			}, false);
			return;
		}
		if (AsrKeyType.THREE_MODE.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_THREE_MODE", new Runnable() {

				@Override
				public void run() {
					// 3D模式
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10002);
					intent.putExtra(SOURCE_APP, "txz");
					intent.putExtra(EXTRA_TYPE, 3);
					intent.putExtra(EXTRA_OPERATE, 3);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl THREE_MODE");
				}
			}, false);
			return;
		}
		if (AsrKeyType.CAR_DIRECT.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_CAR_DIRECT", new Runnable() {

				@Override
				public void run() {
					// 车头朝上
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10002);
					intent.putExtra(SOURCE_APP, "txz");
					intent.putExtra(EXTRA_TYPE, 3);
					intent.putExtra(EXTRA_OPERATE, 2);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl CAR_DIRECT");
				}
			}, false);
			return;
		}
		if (AsrKeyType.NORTH_DIRECT.equals(type)) {
			// 正北朝上
			doConfirmShow(type, command, "RS_MAP_NORTH_DIRECT", new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10002);
					intent.putExtra(SOURCE_APP, "txz");
					intent.putExtra(EXTRA_TYPE, 3);
					intent.putExtra(EXTRA_OPERATE, 1);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl NORTH_DIRECT");
				}
			}, false);
			return;
		}
		if (AsrKeyType.EXPORT_MODE.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_EXPERT_MODE", new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10014);
					intent.putExtra(SOURCE_APP, "txz");
					intent.putExtra(EXTRA_TYPE, 2);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl EXPORT_MODE");
				}
			}, false);
			return;
		}
		if (AsrKeyType.MEADWAR_MODE.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_MEADWAR_MODE", new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10014);
					intent.putExtra(SOURCE_APP, "txz");
					intent.putExtra(EXTRA_TYPE, 1);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl MEADWAR_MODE");
				}
			}, false);
			return;
		}
		if (AsrKeyType.TUIJIANLUXIAN.equals(type)) {
			doRePlanWakeup(type, command, "RS_MAP_TUIJIANLUXIAN", new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10006);
					intent.putExtra("SOURCE_APP", "txz");
					intent.putExtra("NAVI_TYPE", 1);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl TUIJIANLUXIAN");
				}
			});
			return;
		}
		if (AsrKeyType.LESS_MONEY.equals(type)) {
			doRePlanWakeup(type, command, "RS_MAP_LESS_MONEY", new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10006);
					intent.putExtra("SOURCE_APP", "txz");
					intent.putExtra("NAVI_TYPE", 5);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl LESS_MONEY");
				}
			});
			return;
		}
		if (AsrKeyType.DUOBIYONGDU.equals(type)) {
			doRePlanWakeup(type, command, "RS_MAP_DUOBIYONGDU", new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10006);
					intent.putExtra("SOURCE_APP", "txz");
					intent.putExtra("NAVI_TYPE", 2);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl DUOBIYONGDU");
				}
			});
			return;
		}
		if (AsrKeyType.BUZOUGAOSU.equals(type)) {
			doRePlanWakeup(type, command, "RS_MAP_BUZOUGAOSU", new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10006);
					intent.putExtra("SOURCE_APP", "txz");
					intent.putExtra("NAVI_TYPE", 3);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl BUZOUGAOSU");
				}
			});
			return;
		}
		if (AsrKeyType.GAOSUYOUXIAN.equals(type)) {
			doRePlanWakeup(type, command, "RS_MAP_GAOSUYOUXIAN", new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10006);
					intent.putExtra("SOURCE_APP", "txz");
					intent.putExtra("NAVI_TYPE", 4);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl GAOSUYOUXIAN");
				}
			});
			return;
		}

		if (AsrKeyType.NAV_WAY_POI_CMD_GO_GASTATION.equals(type)) {
						doRePlanWakeup(type, command, "RS_NAV_CMD_NAV_WAY_POI_CMD_GASTATION", new Runnable() {
							@Override
							public void run() {
								switchPlanStyle(IMapInterface.PlanStyle.JIAYOUZHAN);
					JNIHelper.logd("NavAmapAutoNavImpl start NAV_WAY_POI_CMD_GO_GASTATION");
				}
			});
			return;
		}

		if (AsrKeyType.VIEW_ALL.equals(type)) {
			String tts = NativeData.getResString("RS_MAP_VIEW_ALL");
			tts = tts.replace("%COMMAND%", command);
			Runnable task = new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(SEND_ACTION);
					if (!TextUtils.isEmpty(getPackageName())) {
						intent.setPackage(getPackageName());
					}
					intent.putExtra(KEY_TYPE, 10010);
					intent.putExtra("SOURCE_APP", "txz");
					intent.putExtra("EXTRA_SHOW", 1);
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					GlobalContext.get().sendBroadcast(intent);
					LogUtil.logd("navapi:NavApiImpl VIEW_ALL");
				}
			};

			if (fromWakeup) {
				task.run();
//				tts = NativeData.getResString("RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_VIEW_ALL").replace("%CMD%", tts);
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(tts, null);
			} else {
//				tts = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", tts);
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(tts, task);
			}
			return;
		}
		if (AsrKeyType.BACK_NAVI.equals(type)) {
			Intent intent = new Intent(SEND_ACTION);
			if (!TextUtils.isEmpty(getPackageName())) {
				intent.setPackage(getPackageName());
			}
			intent.putExtra(KEY_TYPE, 10010);
			intent.putExtra("SOURCE_APP", "txz");
			intent.putExtra("EXTRA_SHOW", 2);
			intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			GlobalContext.get().sendBroadcast(intent);
			LogUtil.logd("NavApiImpl BACK_NAVI");
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_BACK_NAV");
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if (AsrKeyType.HOW_NAVI.equals(type)) {
			speakHowNavi(fromWakeup);
			return;
		}
		if (AsrKeyType.ASK_REMAIN.equals(type)) {
			speakAskRemain(fromWakeup);
			return;
		}
		if (AsrKeyType.START_NAVI.equals(type)) {
			startNavByInner();
			RecorderWin.close();
			return;
		}
		if (AsrKeyType.EXIT_NAV.equals(type)) {
			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%",
						NativeData.getResString("RS_MAP_NAV_EXIT"));
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						NavManager.getInstance().exitAllNavTool();
					}
				});
				return;
			}
			if (!enableWakeupExitNav) {
				return;
			}
			doExitConfirm(type, NativeData.getResString("RS_MAP_NAV_EXIT"), new Runnable() {

				@Override
				public void run() {
					NavManager.getInstance().exitAllNavTool();
				}
			});
			return;
		}
		if (AsrKeyType.CANCEL_NAV.equals(type)) {
			if (!fromWakeup && !isInNav()) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%",
						NativeData.getResString("RS_MAP_NAV_EXIT"));
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						NavManager.getInstance().exitAllNavTool();
					}
				});
				return;
			}
			doExitConfirm(type,
					isInNav() ? NativeData.getResString("RS_MAP_NAV_STOP") : NativeData.getResString("RS_MAP_NAV_EXIT"),
					new Runnable() {

						@Override
						public void run() {
							if (!isInNav()) {
								NavManager.getInstance().exitAllNavTool();
								return;
							}
							cancelNav.run();
						}
					});
			return;
		}
		if (AsrKeyType.CLOSE_MAP.equals(type)) {
			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", command);
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						NavManager.getInstance().exitAllNavTool();
					}
				});
				return;
			}
			doExitConfirm(type, command, new Runnable() {

				@Override
				public void run() {
					NavManager.getInstance().exitAllNavTool();
				}
			});
			return;
		}

		if (AsrKeyType.FRONT_TRAFFIC.equals(type)) {
			if ((mFlags & FLAG_SUPPORT_FRONT_TRAFFIC) == 0) {
				//不支持该能力
				String spk = NativeData.getResString("RS_VOICE_MAP_UNSUPPORT");
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(spk, null);
			} else {
				String spk = NativeData.getResString("RS_VOICE_DOING_COMMAND");
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(spk, new Runnable() {
					@Override
					public void run() {
						Intent intent = new Intent(SEND_ACTION);
						intent.putExtra(KEY_TYPE, 10020);
						intent.putExtra(SOURCE_APP, "txz");
						GlobalContext.get().sendBroadcast(intent);
					}
				});
			}
			return;
		}
	}

	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		if (info == null || info.msgGpsInfo == null) {
			return false;
		}
		super.NavigateTo(plan, info);

		//重新规划路线的时候把上次途经点信息清空
		if (isInNav()) {
			if (jyList != null) {
				jyList.clear();
			}
		}

		// STYLE (0 速度快; 1 费用少; 2 路程短; 3 不走高速；4 躲避拥堵；
		// 5 不走高速且避免收费；6 不走高速且躲避拥堵；7 躲避收费和拥堵；8 不走高速躲避收费和拥堵)
		int navType = 0;
		if (plan != null) {
			if (plan == NavPlanType.NAV_PLAN_TYPE_RECOMMEND) {
				navType = 1;
			} else if (plan == NavPlanType.NAV_PLAN_TYPE_AVOID_JAMS) {
				navType = 2;
			} else if (plan == NavPlanType.NAV_PLAN_TYPE_BZGS) {
				navType = 3;
			} else if (plan == NavPlanType.NAV_PLAN_TYPE_GSYX) {
				navType = 4;
			} else if (plan == NavPlanType.NAV_PLAN_TYPE_LEAST_COST) {
				navType = 5;
			}
		}

		Intent intent = new Intent(SEND_ACTION);
		if (!TextUtils.isEmpty(getPackageName())) {
			intent.setPackage(getPackageName());
		}
		intent.putExtra(KEY_TYPE, 10003);
		intent.putExtra(SOURCE_APP, "txz");
		intent.putExtra("POINAME", info.strTargetName);
		intent.putExtra("ADDRESS",info.strTargetAddress);
		intent.putExtra("LAT", info.msgGpsInfo.dblLat);
		intent.putExtra("LON", info.msgGpsInfo.dblLng);
		intent.putExtra("EXTRA", info.uint32NavType);
		intent.putExtra("DEV", 0);
		intent.putExtra("STYLE", -1);
		intent.putExtra("NAVI_TYPE", navType);
		intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		GlobalContext.get().sendBroadcast(intent);
		LogUtil.logd("navapi:NavigateTo:" + info.strTargetName + ",EXTRA:" + info.uint32NavType + ",NAVI_TYPE:"+navType);
		return true;
	}

	@Override
	public void updateHomeLocation(NavigateInfo navigateInfo) {
		Intent intent = new Intent(SEND_ACTION);
		if (!TextUtils.isEmpty(getPackageName())) {
			intent.setPackage(getPackageName());
		}
		intent.putExtra(KEY_TYPE, 10007);
		intent.putExtra(SOURCE_APP, "txz");
		intent.putExtra("CATEGORY", 1);
		intent.putExtra("POINAME", navigateInfo.strTargetName);
		intent.putExtra("ADDRESS", navigateInfo.strTargetAddress);
		intent.putExtra("LAT", navigateInfo.msgGpsInfo.dblLat);
		intent.putExtra("LON", navigateInfo.msgGpsInfo.dblLng);
		intent.putExtra("TYPE", 1);
		intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		GlobalContext.get().sendBroadcast(intent);
		LogUtil.logd("navapi:updateHomeLocation");
	}

	@Override
	public void updateCompanyLocation(NavigateInfo navigateInfo) {
		Intent intent = new Intent(SEND_ACTION);
		if (!TextUtils.isEmpty(getPackageName())) {
			intent.setPackage(getPackageName());
		}
		intent.putExtra(KEY_TYPE, 10007);
		intent.putExtra(SOURCE_APP, "txz");
		intent.putExtra("CATEGORY", 2);
		intent.putExtra("POINAME", navigateInfo.strTargetName);
		intent.putExtra("ADDRESS", navigateInfo.strTargetAddress);
		intent.putExtra("LAT", navigateInfo.msgGpsInfo.dblLat);
		intent.putExtra("LON", navigateInfo.msgGpsInfo.dblLng);
		intent.putExtra("TYPE", 1);
		intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		GlobalContext.get().sendBroadcast(intent);
		LogUtil.logd("navapi:updateCompanyLocation");
	}

	@Override
	public List<String> getBanCmds() {
		List<String> cmds = new ArrayList<String>();
		if ((mFlags & FLAG_SUPPORT_FRONT_TRAFFIC) == 0) {
			cmds.add(AsrKeyType.FRONT_TRAFFIC);
		}
		return cmds;
	}

	@Override
	public String[] getSupportCmds() {
		return new String[] {
				AsrKeyType.ZOOM_IN,
				AsrKeyType.ZOOM_OUT,
				AsrKeyType.NIGHT_MODE,
				AsrKeyType.LIGHT_MODE,
				AsrKeyType.AUTO_MODE,
				AsrKeyType.EXPORT_MODE,
				AsrKeyType.MEADWAR_MODE,
				AsrKeyType.EXIT_NAV,
				AsrKeyType.CANCEL_NAV,
				AsrKeyType.CLOSE_MAP,
				AsrKeyType.VIEW_ALL,
				AsrKeyType.TUIJIANLUXIAN,
				AsrKeyType.DUOBIYONGDU,
				AsrKeyType.BUZOUGAOSU,
				AsrKeyType.GAOSUYOUXIAN,
				AsrKeyType.LESS_MONEY,
				AsrKeyType.LESS_DISTANCE,
				AsrKeyType.HOW_NAVI,
				AsrKeyType.ASK_REMAIN,
				AsrKeyType.BACK_NAVI,
//				AsrKeyType.START_NAVI,
				AsrKeyType.OPEN_TRAFFIC,
				AsrKeyType.CLOSE_TRAFFIC,
				AsrKeyType.TWO_MODE,
				AsrKeyType.THREE_MODE,
				AsrKeyType.CAR_DIRECT,
				AsrKeyType.NAV_WAY_POI_CMD_GO_GASTATION,
				AsrKeyType.NORTH_DIRECT,
				AsrKeyType.FRONT_TRAFFIC};
	}

	@Override
	public List<String> getCmdNavOnly() {
		List<String> cmds = new ArrayList<String>();
		cmds.add(AsrKeyType.TUIJIANLUXIAN);
		cmds.add(AsrKeyType.DUOBIYONGDU);
		cmds.add(AsrKeyType.BUZOUGAOSU);
		cmds.add(AsrKeyType.GAOSUYOUXIAN);
		cmds.add(AsrKeyType.LESS_MONEY);
		cmds.add(AsrKeyType.LESS_DISTANCE);
		cmds.add(AsrKeyType.HOW_NAVI);
		cmds.add(AsrKeyType.ASK_REMAIN);
		cmds.add(AsrKeyType.EXPORT_MODE);
		cmds.add(AsrKeyType.MEADWAR_MODE);
		cmds.add(AsrKeyType.VIEW_ALL);
		cmds.add(AsrKeyType.NAV_WAY_POI_CMD_GO_GASTATION);
		cmds.add(AsrKeyType.BACK_NAVI);
		cmds.add(AsrKeyType.FRONT_TRAFFIC);
		return cmds;
	}

	@Override
	public String getPackageName() {
		return mPackageName;
	}

	private static final double EARTH_RADIUS = 6378137;//地球半径,单位米
	private static double rad(double d)
	{
		return d * Math.PI / 180.0;
	}

	/**
	 *
	 * @param lat1 第一个纬度
	 * @param lng1 第一个经度
	 * @param lat2 第二个纬度
	 * @param lng2 第二个经度
	 * @return 两个经纬度的距离
	 */
	private double getDistance(double lat1, double lng1, double lat2, double lng2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);

		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
				Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;

	}

	public void switchPlanStyle(IMapInterface.PlanStyle ps) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "switchPlanStyle");
//		jsonBuilder.put("origin", getMapDetCode());
		jsonBuilder.put("planStyle", ps.name());
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		String kw = null;
		if (ps == IMapInterface.PlanStyle.JIAYOUZHAN) {
			kw = "加油站";
		} else if (ps == IMapInterface.PlanStyle.CESUO) {
			kw = "厕所";
		} else if (ps == IMapInterface.PlanStyle.ATM) {
			kw = "ATM";
		} else if (ps == IMapInterface.PlanStyle.WEIXIUZHAN) {
			kw = "维修站";
		}
		if (kw != null) {
			navigateto(kw);
			return;
		}
	}

	private void navigateto(String kw) {
		UiMap.NearbySearchInfo pbneNearbySearchInfo = new UiMap.NearbySearchInfo();
		pbneNearbySearchInfo.strKeywords = kw;
		pbneNearbySearchInfo.strCenterPoi = "ON_WAY";
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
				UiMap.SUBEVENT_MAP_NAVIGATE_NEARBY, pbneNearbySearchInfo);
	}

	@Override
	public double[] getDestinationLatlng() {
		if (mPathInfo != null) {
			double[] latlng = {mPathInfo.toPoiLat, mPathInfo.toPoiLng};
			return latlng;
		}
		return null;
	}

	@Override
	public String getDestinationCity() {
		if (mPathInfo != null) {
			return mPathInfo.toCity;
		}
		return null;
	}

	@Override
	public String disableNavWithFromPoi() {
		if ((mFlags & FLAG_SUPPORT_NAVIGATE_WITH_FROM_POIS) == 0) {
			return super.disableNavWithFromPoi();
		}
		return "";
	}

	@Override
	public String disableNavWithWayPoi() {
		if ((mFlags & FLAG_SUPPORT_NAVIGATE_WITH_WAY_POIS) == 0) {
			return super.disableNavWithWayPoi();
		}
		return "";
	}

	@Override
	public boolean navigateWithWayPois(final Poi startPoi, final Poi endPoi,
			final List<TXZNavManager.PathInfo.WayInfo> pois) {
		//TODO 增加从起点到终点的导航协议
		if (endPoi == null) {
			throw new NullPointerException("endPoi is null！");
		}
		final Intent intent = new Intent();
		intent.setAction(SEND_ACTION);
		intent.putExtra(KEY_TYPE, 10019);
		intent.putExtra(SOURCE_APP, "txz");
		if (startPoi != null) {
			intent.putExtra("EXTRA_SNAME", startPoi.getName());
			intent.putExtra("EXTRA_SGEO_INFO", startPoi.getGeoinfo());
			intent.putExtra("EXTRA_SLON", startPoi.getLng());
			intent.putExtra("EXTRA_SLAT", startPoi.getLat());
		}

		intent.putExtra("EXTRA_DNAME", endPoi.getName());
		intent.putExtra("EXTRA_DGEO_INFO", endPoi.getGeoinfo());
		intent.putExtra("EXTRA_DLON", endPoi.getLng());
		intent.putExtra("EXTRA_DLAT", endPoi.getLat());

		if (pois != null) {
			Bundle midBundle = new Bundle();
			for (int i = 0; i < pois.size(); i++) {
				midBundle.putString("EXTRA_MIDNAME_"+i,pois.get(i).name);
				midBundle.putString("EXTRA_MIDGEO_INFO_"+i,pois.get(i).addr);
				midBundle.putDouble("EXTRA_MIDLON_"+i,pois.get(i).lng);
				midBundle.putDouble("EXTRA_MIDLAT_"+i,pois.get(i).lat);
			}
			intent.putExtra("EXTRA_MID",midBundle);
			intent.putExtra("EXTRA_MID_COUNT",pois.size());
		} else {
			intent.putExtra("EXTRA_MID_COUNT", 0);
		}
		int delay = 0;
		if (!PackageManager.getInstance().isAppRunning(getPackageName())) {
			Intent openIntent = GlobalContext.get().getPackageManager().getLaunchIntentForPackage(getPackageName());
			if (openIntent != null) {
				openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				openIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
				GlobalContext.get().startActivity(openIntent);
			}
			delay = 3000;
			LogUtil.logd("navigateTo delay:" + delay);
		}

		intent.putExtra("EXTRA_DEV", 0);
		intent.putExtra("EXTRA_M", -1);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				GlobalContext.get().sendBroadcast(intent);
			}
		},delay);
		return true;
	}

	@Override
    public boolean inquiryRoadTrafficByNearby(String city, String keywords) {
        return inquiryRoadTraffic(2, city, keywords);
    }

    @Override
    public boolean inquiryRoadTrafficByPoi(String city, String keywords) {
        return inquiryRoadTraffic(1, city, keywords);
    }

	@Override
	public boolean isInquiryRoadTrafficSupported() {
		return (mFlags & FLAG_SUPPORT_INQUIRY_TRAFFIC) != 0;
	}

	private boolean inquiryRoadTraffic(int type, String city, String keywords) {
        if (isInquiryRoadTrafficSupported()) {
            Intent intent = new Intent(SEND_ACTION);
            if (!TextUtils.isEmpty(getPackageName())) {
                intent.setPackage(getPackageName());
            }
            intent.putExtra(KEY_TYPE, 10021);
            intent.putExtra(SOURCE_APP, "txz");
            Bundle bundle = new Bundle();
            // city不含语义解析，返回的是当前城市
//            bundle.putString("EXTRA_TRAFFIC_CONDITION_CITY", city);
            bundle.putString("EXTRA_TRAFFIC_CONDITION_KEYWORD", keywords);
//            bundle.putInt("EXTRA_TRAFFIC_CONDITION_TYPE", type);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            GlobalContext.get().sendBroadcast(intent);
            LogUtil.logd("navapi:NavApiImpl INQUIRY_ROAD_TRAFFIC type=" + type +", city=" + city + ", keywords=" + keywords);
        }
        return false;
    }

    @Override
    public boolean inquiryRoadTrafficByFront() {
        if (isInquiryRoadTrafficSupported()) {
            Intent intent = new Intent(SEND_ACTION);
            if (!TextUtils.isEmpty(getPackageName())) {
                intent.setPackage(getPackageName());
            }
            intent.putExtra(KEY_TYPE, 10023);
            intent.putExtra(SOURCE_APP, "txz");
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            GlobalContext.get().sendBroadcast(intent);
            LogUtil.logd("navapi:NavApiImpl INQUIRY_ROAD_TRAFFIC_BY_FRONT");
        }
        return false;
    }

	private final CopyOnWriteArrayList<OnInquiryRoadTrafficResultListener> inquiryRoadTrafficResultListeners = new CopyOnWriteArrayList<OnInquiryRoadTrafficResultListener>();

	@Override
	public void registerInquiryRoadTrafficResultListener(OnInquiryRoadTrafficResultListener listener) {
		inquiryRoadTrafficResultListeners.add(listener);
	}

	@Override
	public void unregisterInquiryRoadTrafficResultListener(OnInquiryRoadTrafficResultListener listener) {
		inquiryRoadTrafficResultListeners.remove(listener);
	}
}

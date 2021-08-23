package com.txznet.txz.component.text.txz;

import org.json.JSONArray;
import org.json.JSONException;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.mapcore.util.o;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.equipment.UiEquipment.Req_NLP;
import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.sdk.TXZNavManager;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.home.HomeControlManager;
import com.txznet.txz.component.text.IText;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.film.FilmManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.mtj.MtjModule;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.reminder.ReminderManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.ticket.QiWuTicketManager;
import org.json.JSONException;

public class TextTxzImpl implements IText {
	private static final int MAX_COUNT = Integer.MAX_VALUE;
	private static final String MONITOR_ERROR_BEGIN = "text.txz.E.";
	private static final String MONITOR_INFO_BEGIN = "text.txz.I.";
	private ITextCallBack mCallBack;
	private VoiceParseData mParseData;
	private int id;

	@Override
	public int initialize(IInitCallback oRun) {
		mCallBack = null;
		mParseData = null;
		id = 0;
		return 0;
	}

	@Override
	public int setVoiceData(VoiceParseData parseData, ITextCallBack callBack) {
		if (parseData == null || TextUtils.isEmpty(parseData.strText)) {
			callBack.onError(TxzErrorInputNull,mPriority);
			MonitorUtil.monitorCumulant(MONITOR_ERROR_BEGIN
					+ TxzErrorInputNULLStr);
			return 0;
		}
		UiMap.GpsInfo gpsInfo = null;
		if(LocationManager.getInstance().getLastLocation() != null)
			gpsInfo = LocationManager.getInstance().getLastLocation().msgGpsInfo;
//		if (gpsInfo == null) {
//			callBack.onError(TxzErrorGPSNull,mPriority);
//			MonitorUtil.monitorCumulant(MONITOR_ERROR_BEGIN
//					+ TxzErrorGPSNullStr);
//			return 0;
//		}
		id = (id + 1) % MAX_COUNT;
		UiEquipment.Req_NLP nlp = new Req_NLP();
		nlp.strWord = parseData.strText.getBytes();
		if (gpsInfo != null)
			nlp.strCrd = gpsInfo.dblLng + "_" + gpsInfo.dblLat;
		nlp.uint32Coordtype = 2;
		nlp.uint32Id = id;
		if (parseData.uint32Sence != null) {
			if (parseData.uint32Sence == VoiceData.GRAMMAR_SENCE_NAVIGATE
					|| parseData.uint32Sence == VoiceData.GRAMMAR_SENCE_SET_HOME
					|| parseData.uint32Sence == VoiceData.GRAMMAR_SENCE_SET_COMPANY)
				nlp.uint32Scene = UiEquipment.NLP_SCENE_NAV;
			else if (parseData.uint32Sence == VoiceData.GRAMMAR_SENCE_MAKE_CALL) {
				nlp.uint32Scene = UiEquipment.NLP_SCENE_CALL;
			} else {
				nlp.uint32Scene = UiEquipment.NLP_SCENE_NORMAL;
			}
		} else {
			nlp.uint32Scene = UiEquipment.NLP_SCENE_NORMAL;
		}
		if (parseData.strVoiceData != null) {
			try {
				org.json.JSONObject object = new org.json.JSONObject(parseData.strVoiceData);
				JSONObject object2 = new JSONObject();
				object2.put("scene", object.optString("scene", ""));
				object2.put("action", object.optString("action", ""));
				JNIHelper.logd("fangde_token" + ProjectCfg.getFangdeToken());
				nlp.strScene = object2.toJSONString();
				JSONObject strJson = new JSONObject();
				strJson.put("fangde_token", ProjectCfg.getFangdeToken());
				strJson.put("fangde_wait_speech", HomeControlManager.getInstance().isWaitSpeech());
				if(ReminderManager.getInstance().getIsNeedClearReminder()){
					strJson.put("clear_tencent_session","true");
					JNIHelper.logd("GaryFlag : clearReminder");
					ReminderManager.getInstance().setIsNeedClearReminder(false);
				}
				if(FilmManager.getInstance().getBeClearWanMi()){
					strJson.put("wan_mi_new_session",true);
				}
				if(QiWuTicketManager.getInstance().getBeClearQiWu()){
					strJson.put("qi_wu_new_session",true);
				}
				TXZNavManager.PathInfo pathInfo = NavManager.getInstance().getNavPathInfo();
				if(pathInfo != null){
					String navCity = pathInfo.toCity;
					double navLat = pathInfo.toPoiLat;
					double navLng = pathInfo.toPoiLng;
					if(navLat > 0 && navLng > 0){
						strJson.put("nav_path_target_lat", navLat);
						strJson.put("nav_path_target_lng", navLng);
						if(!TextUtils.isEmpty(navCity)){
							strJson.put("nav_path_target_city", navCity);
						}
					}
				}
				if(parseData.strExtraData != null){
					try {
						String strExtraData = new String(parseData.strExtraData);
						org.json.JSONObject jsonObject = new org.json.JSONObject(strExtraData);
						JSONArray params = jsonObject.getJSONArray("params");
						for(int i = 0; i < params.length(); i++){
							org.json.JSONObject param = params.getJSONObject(i);
							String key = param.getString("key");
							String value = param.getString("value");
							strJson.put(key,value);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				JNIHelper.logd("fangde_wait_speech"+HomeControlManager.getInstance().isWaitSpeech());
				nlp.strJson = strJson.toJSONString().getBytes();

			} catch (JSONException e) {
			}
		}
		mCallBack = callBack;
		try {
			mParseData = VoiceParseData.parseFrom(VoiceParseData
					.toByteArray(parseData));
		} catch (InvalidProtocolBufferNanoException e) {
		}
		JNIHelper.logd("nlp:startTxz");
		MonitorUtil.monitorCumulant(MONITOR_INFO_BEGIN
				+ ALL);
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_REQ_NLP, nlp);
		return 0;
	}

	@Override
	public int setText(String text, ITextCallBack callBack) {
		UiMap.GpsInfo gpsInfo = LocationManager.getInstance().getLastLocation().msgGpsInfo;

		id = (id + 1) % MAX_COUNT;
		UiEquipment.Req_NLP nlp = new Req_NLP();
		nlp.strWord = text.getBytes();
		nlp.strCrd = gpsInfo.dblLng + "_" + gpsInfo.dblLat;
		nlp.uint32Coordtype = 2;
		nlp.uint32Id = id;
		nlp.uint32Scene = UiEquipment.NLP_SCENE_NORMAL;
		JSONObject object = new JSONObject();
		object.put("scene", "unknown");
		object.put("action", "unknown");
		JNIHelper.logd("fangde_token" + ProjectCfg.getFangdeToken());
		JSONObject strJson = new JSONObject();
		strJson.put("fangde_token", ProjectCfg.getFangdeToken());
		strJson.put("fangde_wait_speech", HomeControlManager.getInstance().isWaitSpeech());
        JNIHelper.logd("fangde_wait_speech"+HomeControlManager.getInstance().isWaitSpeech());
		nlp.strJson = strJson.toJSONString().getBytes();

		nlp.strScene = object.toJSONString();		
		mCallBack = callBack;
		JNIHelper.logd("nlp:startTxz");
		MonitorUtil.monitorCumulant(MONITOR_INFO_BEGIN
				+ ALL);
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_REQ_NLP, nlp);
		return 0;
	}

	@Override
	public void cancel() {
		id = (id + 1) % MAX_COUNT;
		synchronized (TextTxzImpl.class) {
			mCallBack = null;
		}
		mParseData = null;
		JNIHelper.logd("nlp:cancelTxz");
	}

	@Override
	public void release() {
	}

	public void onResult(byte[] data) {
		try {
			if (data == null || data.length == 0) {
				onError(TxzErrorDataNull);
				MonitorUtil.monitorCumulant(MONITOR_ERROR_BEGIN
						+ TxzErrorDataNullStr);
				return ;
			}
			UiEquipment.Resp_NLP nlp = UiEquipment.Resp_NLP.parseFrom(data);
			if (nlp == null || nlp.strJsonResult == null) {
				onError(TxzErrorDataNull);
				MonitorUtil.monitorCumulant(MONITOR_ERROR_BEGIN
						+ TxzErrorDataNullStr);
				return ;
			}
			String resultString = new String(nlp.strJsonResult);
			JNIHelper.logd("nlp:result=" + resultString);
			JSONObject object = JSON.parseObject(resultString);
			if (object != null) {
				if(!object.containsKey("id")) {
					onError(TxzErrorIdNull);
					MonitorUtil.monitorCumulant(MONITOR_ERROR_BEGIN
							+ TxzErrorIdNull);
					return ;
				}
				Integer currentID = object.getInteger("id");
				if (currentID == null) {
					onError(TxzErrorIdNull);
					MonitorUtil.monitorCumulant(MONITOR_ERROR_BEGIN
							+ TxzErrorIdNull);
					return ;
				}
				if (currentID != id) {
					JNIHelper.logi("nlp:give up result because now=" + id + "("
							+ currentID + ")");
					return;
				}
				if (!object.containsKey("textValue")) {
					onError(TxzErrorValueNull);
					MonitorUtil.monitorCumulant(MONITOR_ERROR_BEGIN
							+ TxzErrorValueNullStr);
					return;
				}
				float value = object.getFloat("textValue");
				if (value < TextResultHandle.TEXT_SCORE_MIN) {
					onError(ParseError);
					return;
				}
				VoiceParseData parseData = null;
				if (mParseData == null)
					parseData = new VoiceParseData();
				else
					parseData = mParseData;
				parseData.uint32DataType = VoiceData.VOICE_DATA_TYPE_TXZ_SENCE_NEW;
				parseData.strText = object.getString("text");
				parseData.floatTextScore = value;
				parseData.strVoiceData = resultString;
				if(object.containsKey("type")){
					if(object.getIntValue("type") == VoiceData.VOICE_DATA_TYPE_BAIDU_SCENE_JSON){
						MtjModule.getInstance().event(MtjModule.EVENTID_USER_NLP);
					}
				}
				synchronized (TextTxzImpl.class) {
					if (mCallBack != null) {
						mCallBack.onResult(parseData,mPriority);
						mParseData = null;
						return;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		MonitorUtil.monitorCumulant(MONITOR_ERROR_BEGIN
				+ TxzErrorUnknownStr);
		onError(TxzErrorUnknown);
	}

	public void onError(int errorCode) {
		JNIHelper.logd("nlp:TxzErrorCode=" + errorCode);
		synchronized (TextTxzImpl.class) {
			if (mCallBack != null) {
				mCallBack.onError(errorCode,mPriority);
			}
			return;
		}
	}
	private int mPriority = PRIORITY_LEVEL_NORMAL;

	@Override
	public void setPriority(int priority) {
		mPriority = priority;
	}
	@Override
	public int getPriority() {
		return mPriority;
	}
}

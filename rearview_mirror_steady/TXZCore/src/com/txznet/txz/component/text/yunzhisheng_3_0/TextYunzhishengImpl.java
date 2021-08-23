
package com.txznet.txz.component.text.yunzhisheng_3_0;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.data.UiData;
import com.txz.ui.data.UiData.TestResp;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.mix.net.NetAsrYunzhishengImpl;
import com.txznet.txz.component.text.IText;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.audio.AudioManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.plugin.interfaces.NlpTransitionToTxz;
import com.unisound.client.SpeechConstants;
import com.unisound.client.TextUnderstander;
import com.unisound.client.TextUnderstanderListener;

/*
 * TextManager中已经保证cancel、setText是同步的了，所以两个接口不必再加锁进行同步了。
 */
public class TextYunzhishengImpl implements IText {
	private TextUnderstander mTextUnderstander;
	private TextUnderstanderListener mTextUnderstanderListener;
	private IInitCallback mInitCallBack;
	private ITextCallBack mTextCallBack;
	private final static int TIMEOUT = 5000;
	private static final String MONITOR_ERROR_BEGIN = "text.yzs.E.";
	private static final String MONITOR_INFO_BEGIN = "text.yzs.I.";
	private VoiceParseData mParseData;
	private boolean isOld;

	private Runnable checkTimeOutTask = new Runnable() {
		@Override
		public void run() {
			JNIHelper.logd("nlp:parseText TimeOut");
			mTextUnderstander.cancel();
			final ITextCallBack callBack = mTextCallBack;
			mTextCallBack = null;
			if (callBack != null) {
				Runnable oRun = new Runnable() {
					@Override
					public void run() {
						MonitorUtil.monitorCumulant(MONITOR_ERROR_BEGIN
								+ NetTimeOutErrorStr);
						callBack.onError(NetTimeOutError,mPriority);
					}
				};
				AppLogic.runOnBackGround(oRun, 0);
			}
		}

	};

	@Override
	public int initialize(IInitCallback callBack) {
		isOld = false;
		mInitCallBack = callBack;
		mTextUnderstander = new TextUnderstander(GlobalContext.get(),
				ProjectCfg.getYunzhishengAppId()//
				, ProjectCfg.getYunzhishengSecret());
		mTextUnderstander.setOption(SpeechConstants.NLU_SCENARIO, "incar");

		mTextUnderstanderListener = new TextUnderstanderListener() {

			@Override
			public void onResult(int arg0, String arg1) {
				JNIHelper.logd("nlp:onResult = " + arg0 + " : " + arg1);
				doResult(arg0, arg1);
			}

			@Override
			public void onEvent(int arg0) {
				JNIHelper.logd("nlp:onEvent:" + arg0);
			}

			@Override
			public void onError(int arg0, String arg1) {
				JNIHelper.logd("nlp:onError = " + arg0 + " :" + arg1);
				doError(ParseError);
			}
		};

		mTextUnderstander.setListener(mTextUnderstanderListener);

		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				int nRet = 0;
				nRet = mTextUnderstander.init("");
				IInitCallback cb = mInitCallBack;
				mInitCallBack = null;

				if (cb != null) {
					cb.onInit(nRet == 0);
				}
			}
		};

		AppLogic.runOnBackGround(oRun, 0);
		return 0;
	}

	private void doResult(int type, String jsonResult) {
		AppLogic.removeBackGroundCallback(checkTimeOutTask);
		JNIHelper.logd("nlp:onResult:" + jsonResult);
		final ITextCallBack callBack = mTextCallBack;
		final String result = jsonResult;
		mTextCallBack = null;
		if (callBack != null) {
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					if (isOld)
						callBack.onResult(result,mPriority);
					else {
						VoiceParseData parseData;
						if (mParseData != null)
							parseData = mParseData;
						else
							parseData = new VoiceParseData();
						parseData.strVoiceData = result;
						parseData.uint32DataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON;
						callBack.onResult(yzsDataToTxzScene(parseData),mPriority);
					}
				}
			};
			AppLogic.runOnBackGround(oRun, 0);
		}
	}

	private void doError(int errorCode) {
		AppLogic.removeBackGroundCallback(checkTimeOutTask);
		JNIHelper.logd("nlp:errorCode : " + errorCode);
		final ITextCallBack callBack = mTextCallBack;
		mTextCallBack = null;
		if (callBack != null) {
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					MonitorUtil.monitorCumulant(MONITOR_ERROR_BEGIN
							+ ParseErrorStr);
					callBack.onError(ParseError,mPriority);
				}
			};
			AppLogic.runOnBackGround(oRun, 0);
		}
	}

	@Override
	public void cancel() {
		JNIHelper.logd("nlp:cancel");
		AppLogic.removeBackGroundCallback(checkTimeOutTask);
		mTextUnderstander.cancel();
		final ITextCallBack callBack = mTextCallBack;
		mTextCallBack = null;
		if (callBack != null) {
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					MonitorUtil.monitorCumulant(MONITOR_ERROR_BEGIN
							+ InterruptedErrorStr);
					callBack.onError(InterruptedError,mPriority);
				}
			};
			AppLogic.runOnBackGround(oRun, 0);
		}
	}

	@Override
	public void release() {

	}

	@Override
	public int setVoiceData(VoiceParseData parseData, ITextCallBack callBack) {
		MonitorUtil.monitorCumulant(MONITOR_INFO_BEGIN + ALL);
		isOld = false;
		mParseData = parseData;
		mTextCallBack = callBack;
		try {
			LocationInfo location = LocationManager.getInstance()
					.getLastLocation();
			if (location == null || location.msgGeoInfo == null
					|| location.msgGeoInfo.strCity == null) {
				JNIHelper.logd("nlp:定位失败!!!");// donothing
			} else {
				JNIHelper.logd("nlp:city = " + location.msgGeoInfo.strCity);
				mTextUnderstander.setOption(SpeechConstants.GENERAL_CITY,
						location.msgGeoInfo.strCity);
			}
		} catch (Exception e) {

		}

		try {
			LocationInfo location = LocationManager.getInstance()
					.getLastLocation();
			double lat = location.msgGpsInfo.dblLat;
			double lng = location.msgGpsInfo.dblLng;
			String strGpsInfo = lat + "," + lng;
			JNIHelper.logd("nlp:strGpsInfo: " + strGpsInfo);
			mTextUnderstander
					.setOption(SpeechConstants.GENERAL_GPS, strGpsInfo);
		} catch (Exception e) {
		}
		AppLogic.removeBackGroundCallback(checkTimeOutTask);
		AppLogic.runOnBackGround(checkTimeOutTask, TIMEOUT);
		JNIHelper.logd("nlp:startYzs");
		mTextUnderstander.setText(mParseData.strText);
		return 0;
	}

	@Override
	public int setText(String text, ITextCallBack callBack) {
		MonitorUtil.monitorCumulant(MONITOR_INFO_BEGIN + ALL);
		isOld = true;
		mTextCallBack = callBack;
		try {
			LocationInfo location = LocationManager.getInstance()
					.getLastLocation();
			if (location == null || location.msgGeoInfo == null
					|| location.msgGeoInfo.strCity == null) {
				JNIHelper.logd("nlp:定位失败!!!");// donothing
			} else {
				JNIHelper.logd("nlp:city = " + location.msgGeoInfo.strCity);
				mTextUnderstander.setOption(SpeechConstants.GENERAL_CITY,
						location.msgGeoInfo.strCity);
			}
		} catch (Exception e) {

		}

		try {
			LocationInfo location = LocationManager.getInstance()
					.getLastLocation();
			double lat = location.msgGpsInfo.dblLat;
			double lng = location.msgGpsInfo.dblLng;
			String strGpsInfo = lat + "," + lng;
			JNIHelper.logd("nlp:strGpsInfo: " + strGpsInfo);
			mTextUnderstander
					.setOption(SpeechConstants.GENERAL_GPS, strGpsInfo);
		} catch (Exception e) {
		}
		AppLogic.removeBackGroundCallback(checkTimeOutTask);
		AppLogic.runOnBackGround(checkTimeOutTask, TIMEOUT);
		JNIHelper.logd("nlp:startYzs");
		mTextUnderstander.setText(text);
		return 0;
	}

	public static NlpTransitionToTxz mLocalTransitionImpl = null;
	public static NlpTransitionToTxz mOnlineTransitionImpl = null;
	public static VoiceParseData yzsDataToTxzScene(VoiceParseData parseData) {
		// 创建新的VoiceParseData并把txzJson放到strVoiceData中。注意json中一定要有text字段
		if (parseData.strText == null)
			parseData.strText = "";
		JNIHelper.logd("YXZOL: rawText = " + parseData.strText);
		if (mOnlineTransitionImpl != null) {
			VoiceParseData impl = mOnlineTransitionImpl.TransitionToTxz(parseData);
			if (impl != null)
				return impl;
		}
		VoiceParseData newData = new VoiceParseData();
		JSONObject rawJson = new JSONObject();

		try {
			newData = VoiceParseData.parseFrom(VoiceParseData
					.toByteArray(parseData));
			rawJson = (JSONObject) JSON.parse(parseData.strVoiceData);
		} catch (Exception e) {
			JNIHelper
					.loge("parse new VoiceParseData failed! " + e.getMessage());
			return parseRawData(newData, "");
		}
		if (parseData.strVoiceData == null || parseData.strVoiceData.length() == 0)
			return parseRawData(newData, "");

		if (!rawJson.containsKey("text"))
			return parseRawData(newData, "");

		// 去掉末尾的标点
		newData.strText = removePunctuation(rawJson.getString("text"));


		int rc = -1;
		if (rawJson.containsKey("rc"))
			rc = rawJson.getIntValue("rc");
		if (rc != 0 && rc != 1)
			MonitorUtil.monitorCumulant(NetAsrYunzhishengImpl.MONITOR_ERROR+NetAsrYunzhishengImpl.MONITOR_NO_NLP);
		switch (rc) {
		//操作成功
		case 0:
			break;
		//云知声语义不支持的操作，可以先按聊天处理
		case 4:
			break;

		//服务器不理解或不能处理此文本
		case 5:
			return parseRawData(newData,
					newData.strText.replaceAll(" |\\t", ""));
		//业务操作失败，有业务相关的错误信息
		case 1:
			if ("2041".equals(rawJson.getJSONObject("error").getString("code"))) {
				return parseRawData(newData, "");
			} else {
				if (rawJson.containsKey("service")){
					if (rawJson.getString("service").equals("cn.yunzhisheng.traffic.control")) {
						break;
					}else if (rawJson.getString("service").equals("cn.yunzhisheng.broadcast")) {
//						newData.strText = rawJson.getString("text");
						break;
					}
				}
				String strMessage = rawJson.getJSONObject("error").getString(
						"message");
				JSONObject jsonResult = genVoiceJSONObject("unknown",
						"unknown", newData.strText);
				jsonResult.put("answer", strMessage);
				newData.floatTextScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE_LITTLE;
				newData.strVoiceData = jsonResult.toJSONString();

				return newData;
			}
		//无效请求
		case 2:
			if (TextUtils.isEmpty(newData.strText)) {
				JSONObject jsonResult = genVoiceJSONObject("empty", "empty",
						newData.strText);
				newData.strVoiceData = jsonResult.toJSONString();
				newData.floatTextScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE_LITTLE;

				JNIHelper.logd("YZSOL parse result: " + newData.strVoiceData);
				return newData;
			}

		default:
			return parseRawData(newData, "");
		}

		
		newData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
		
		newData.strVoiceData = "";

		if (null == newData.uint32Sence) {

			do {
				if (parseOLQA(newData, rawJson))
					break;
				if (parseOLCall(newData, rawJson))
					break;
				if (parseOLCommonFunction(newData, rawJson))
					break;
				if (parseOLNav(newData, rawJson))
					break;
				if (parseOLAudio(newData, rawJson))
					break;
				if (parseOLFM(newData, rawJson))
					break;
				if (parseOLHelp(newData, rawJson))
					break;
			} while (false);

		} else {

			switch (newData.uint32Sence) {

			case VoiceData.GRAMMAR_SENCE_MAKE_CALL:
				if (parseOLCall(newData, rawJson))
					break;
				if (parseOLQA(newData, rawJson))
					break;
				if (parseOLNav(newData, rawJson))
					break;
				if (parseOLCommonFunction(newData, rawJson))
					break;
				if (parseOLAudio(newData, rawJson))
					break;
				if (parseOLFM(newData, rawJson))
					break;
				if (parseOLHelp(newData, rawJson))
					break;
				break;

			case VoiceData.GRAMMAR_SENCE_NAVIGATE:
			case VoiceData.GRAMMAR_SENCE_SET_HOME:
			case VoiceData.GRAMMAR_SENCE_SET_COMPANY:
				if (parseOLNav(newData, rawJson))
					break;
				if (parseOLQA(newData, rawJson))
					break;
				if (parseOLCall(newData, rawJson))
					break;
				if (parseOLCommonFunction(newData, rawJson))
					break;
				if (parseOLAudio(newData, rawJson))
					break;
				if (parseOLFM(newData, rawJson))
					break;
				if (parseOLHelp(newData, rawJson))
					break;
				break;

			default:
				if (parseOLQA(newData, rawJson))
					break;
				if (parseOLCall(newData, rawJson))
					break;
				if (parseOLCommonFunction(newData, rawJson))
					break;
				if (parseOLNav(newData, rawJson))
					break;
				if (parseOLAudio(newData, rawJson))
					break;
				if (parseOLFM(newData, rawJson))
					break;
				if (parseOLHelp(newData, rawJson))
					break;
				break;
			}
		}

		if (TextUtils.isEmpty(newData.strVoiceData)) {
			JSONObject jsonResult = genVoiceJSONObject( "unknown","unsupport",
					newData.strText);
			jsonResult.put("answer", NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"));
			newData.strVoiceData = jsonResult.toJSONString();
			newData.floatTextScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE;

			JNIHelper.logd("YZSOL parse result: " + newData.strVoiceData);
			return newData;
		}

		JNIHelper.logd("YZSOL parse result: " + newData.strVoiceData);

		return newData;
	}

	/**
	 * 移除字符串末尾的标点
	 * 
	 * @param rawString
	 * @return
	 */
	private static String removePunctuation(String rawString) {
		if (TextUtils.isEmpty(rawString)) {
			return rawString;
		}

		// 比对移除标点
		int i = 0;
		while (true) {
			String strPunctuation = NativeData.getResString(
					"RS_VOICEDATA_CLEAR_MARK", i++);
			if (TextUtils.isEmpty(strPunctuation)) {
				break;
			}

			if (rawString.endsWith(strPunctuation)) {
				rawString = rawString.substring(0, rawString.length()
						- strPunctuation.length());
				break;
			}
		}

		return rawString;
	}

	private static VoiceParseData parseRawData(VoiceParseData voiceData,
			String rawText) {
		JSONObject jsonResult = genVoiceJSONObject("unknown", "unknown",
				voiceData.strText);
		voiceData.strVoiceData = jsonResult.toJSONString();
		voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_MIN;

		JNIHelper.logd("YZSOL parse result: " + voiceData.strVoiceData);
		return voiceData;
	}

	public static VoiceParseData yzsLocalDataToTxzScene(VoiceParseData parseData) {
		// TODO 待添加具体转换函数
		if (mLocalTransitionImpl != null) {
			VoiceParseData impl = mLocalTransitionImpl.TransitionToTxz(parseData);
			if (impl != null)
				return impl;
		}
		VoiceParseData newData = new VoiceParseData();
		try {
			newData = VoiceParseData.parseFrom(VoiceParseData
					.toByteArray(parseData));
		} catch (InvalidProtocolBufferNanoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		YZSLocalJsonConver conver = new YZSLocalJsonConver(parseData);
		newData.strText = conver.getRawText();
		newData.strVoiceData = conver.getJson();
		newData.floatTextScore = conver.getScore();
		newData.uint32TextMask = conver.getTextMask();

		JNIHelper.logd(conver.getJson());
		return newData;
	}

	private static boolean parseOLCall(VoiceParseData voiceData,
			JSONObject rawJson) {
		String strService = rawJson.getString("service");
		JSONObject jsonIntent;
		String strNumber = "";
		String strName = "";
		String strVoiceText = rawJson.getString("text");
		JSONObject jsonResult;

		if ("cn.yunzhisheng.call".equals(strService)
				|| "cn.yunzhisheng.contact".equals(strService)
				|| "cn.yunzhisheng.hotline".equals(strService)) {
			if ("cn.yunzhisheng.hotline".equals(strService)) {
				jsonIntent = rawJson.getJSONObject("semantic").getJSONObject(
						"intent");
				if (!jsonIntent.containsKey("contacts"))
					return false;
				Object contacts = jsonIntent.get("contacts");

				if (contacts instanceof JSONArray
						&& !((JSONArray) contacts).isEmpty()) {
					JSONObject contact = ((JSONArray) contacts)
							.getJSONObject(0);
					strName = contact.getString("name");
					strNumber = contact.getString("numbers");
				}

				// 处理电话号码不唯一的情况(e.g. 10086,10010)
				if (!TextUtils.isEmpty(strNumber) && strNumber.contains(",")) {
					strNumber = strNumber.split(",")[0];
//					voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
				}

				if (TextUtils.isEmpty(strNumber)
						&& TextUtils.isDigitsOnly(strName)) {
					strNumber = strName;
					strName = "";
				}

			} else {
				jsonIntent = rawJson.getJSONObject("semantic").getJSONObject(
						"intent");
				if (!jsonIntent.containsKey("name") && !jsonIntent.containsKey("numbers") && !jsonIntent.containsKey("number"))
					return false;
				strName = jsonIntent.getString("name");
				strNumber = jsonIntent.getString("number");
				if (TextUtils.isEmpty(strNumber)){
					strNumber = jsonIntent.getString("numbers");
				}
				if (TextUtils.isEmpty(strNumber)
						&& TextUtils.isDigitsOnly(strName)) {
					strNumber = strName;
					strName = "";
				}
			}
			String strOperation = rawJson.getString("code");
			if (!TextUtils.isEmpty(strNumber) && TextUtils.isEmpty(strName)) {
				jsonResult = genVoiceJSONObject("call", "make",
						rawJson.getString("text"));
				jsonResult.put("number", strNumber);
				jsonResult.put("mold", TextResultHandle.CALL_NET_YZS);
				jsonResult.put("code", strOperation);
				//号码与文本完全相同则视为模糊语义
				if(strNumber.equals(strVoiceText)){
					jsonResult.put("fuzzy", true);
				}
				voiceData.strVoiceData = jsonResult.toJSONString();

				return true;
			} else if (!TextUtils.isEmpty(strName)) {
				jsonResult = genVoiceJSONObject("call", "make",
						rawJson.getString("text"));
				jsonResult.put("name", strName);
				jsonResult.put("number", strNumber);
				jsonResult.put("mold", TextResultHandle.CALL_NET_YZS);
				jsonResult.put("code", strOperation);
				//名称与文本完全相同则视为模糊语义
				if(strName.equals(strVoiceText)){
					jsonResult.put("fuzzy", true);
				}
				voiceData.strVoiceData = jsonResult.toJSONString();

				return true;
			}
		}

		return false;
	}

	private static boolean parseOLNav(VoiceParseData voiceData,
			JSONObject rawJson) {
		String strService = rawJson.getString("service");
		String strVoiceText = rawJson.getString("text");

		String strOperation = rawJson.getString("code");

		voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
		JSONObject jsonResult = new JSONObject();
		final String strScene = "nav";

		JSONObject jsonIntent;
		if ("cn.yunzhisheng.map".equals(strService)) {
			JSONObject jsonSemantic = rawJson.getJSONObject("semantic");

			if ("ROUTE".equals(strOperation)) {
				if (null != jsonSemantic && jsonSemantic.containsKey("intent")) {
					jsonIntent = jsonSemantic.getJSONObject("intent");
					String strToCity = jsonIntent.getString("toCity");
					String strToPoi = jsonIntent.getString("toPOI");
					String strFromPoi = jsonIntent.getString("fromPOI");
					String strFromCity = jsonIntent.getString("fromCity");
					String condition = jsonIntent.getString("condition");
					if ("LOC_SCHOOL".equals(strToPoi)) {
						strToPoi = "学校";
					}
					// 如果是途径说法，降分处理
					if (isNavBy(strToPoi,strVoiceText) || strVoiceText.contains("分享")
							|| strVoiceText.contains("沿途")|| strVoiceText.contains("途经")
							|| strVoiceText.contains("途中")|| strVoiceText.contains("中途")) {
						voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
					}
					
					if (!TextUtils.isEmpty(strToCity)
							&& strToCity.equals("CURRENT_CITY")) {
						strToCity = "";
					}
					if ("LOC_OFFICE".equals(strToPoi)) {
						jsonResult = genVoiceJSONObject(strScene, "company",
								strVoiceText);
					} else if ("LOC_HOME".equals(strToPoi)) {
						jsonResult = genVoiceJSONObject(strScene, "home",
								strVoiceText);
					} else {
						if (jsonIntent.containsKey("pathPoints") && jsonIntent.getJSONArray("pathPoints").size() > 0 && !TextUtils.isEmpty(jsonIntent.getJSONArray("pathPoints").getString(0))) {
							String point = jsonIntent.getJSONArray("pathPoints").getString(0);
							// 处理途经点到目的地
							if (!"CURRENT_LOC".equals(strToPoi)) {
								if (TextUtils.equals(strToPoi, point)) {
									jsonResult = genVoiceJSONObject(strScene, "search",
											strVoiceText);
								} else {
									jsonResult =
											genVoiceJSONObject(strScene, "passToPoi", strVoiceText);
									jsonResult.put("strToPoi", strToPoi);
								}
							} else {
								jsonResult = genVoiceJSONObject(strScene, "pass",
										strVoiceText);
							}
							jsonResult.put("keywords", point);
							voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
						}
						else {
							if ("CURRENT_LOC".equals(strToPoi) && !TextUtils.isEmpty(condition)) {
								jsonResult = genVoiceJSONObject(strScene, "unKnow",
										strVoiceText);
							} else {
								jsonResult = genVoiceJSONObject(strScene, "search",
										strVoiceText);
								jsonResult.put("keywords", strToPoi);
							}
						}
						jsonResult.put("city", strToCity);
						jsonResult.put("fromPOI", strFromPoi);
						jsonResult.put("fromCity", strFromCity);
					}
					//导航的目的地和文本相同则视为模糊语义
					if(!TextUtils.isEmpty(strToPoi) && strToPoi.equals(strVoiceText)){
						jsonResult.put("fuzzy", true);
					}

					if(!TextUtils.isEmpty(condition)){
						jsonResult.put("condition", condition);
					}
					voiceData.strVoiceData = jsonResult.toJSONString();
					
					return true;
				}

			} else if ("POSITION".equals(strOperation)) {
				jsonIntent = jsonSemantic.getJSONObject("intent");
				String strToCity = jsonIntent.getString("toCity");
				String strToPoi = jsonIntent.getString("toPOI");
				if ("LOC_SCHOOL".equals(strToPoi)) {
					strToPoi = "学校";
				}
				if ("CURRENT_LOC".equals(strToPoi)) {
					jsonResult = genVoiceJSONObject("location", "query",
							strVoiceText);
					voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
				} else if ("LOC_OFFICE".equals(strToPoi)) {
					jsonResult = genVoiceJSONObject(strScene, "company",
							strVoiceText);
				} else {
					voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
					if ("CURRENT_CITY".equals(strToCity)) {
						strToCity = "";
					}
					jsonResult = genVoiceJSONObject(strScene, "search",
							strVoiceText);
					jsonResult.put("city", strToCity);
					jsonResult.put("poi", strToPoi);
					jsonResult.put("keywords", strToPoi);
				}
				//导航的目的地和文本相同则视为模糊语义
				if(!TextUtils.isEmpty(strToPoi) && strToPoi.equals(strVoiceText)){
					jsonResult.put("fuzzy", true);
				}
				voiceData.strVoiceData = jsonResult.toJSONString();
				return true;
			}
		} else if ("cn.yunzhisheng.localsearch".equals(strService)) {

			if ("BUSINESS_SEARCH".equals(strOperation)
					|| "DEAL_SEARCH".equals(strOperation)
					|| "NONBUSINESS_SEARCH".equals(strOperation)) {
				JSONObject jsonSemantic = rawJson.getJSONObject("semantic");

				if (null != jsonSemantic && jsonSemantic.containsKey("intent")) {
					jsonIntent = jsonSemantic.getJSONObject("intent");

					String strCity = jsonIntent.getString("city");
					String strKeywords = jsonIntent.getString("keyword");
					String strPoi = jsonIntent.getString("poi");
					String region=jsonIntent.getString("region");
					int radius = 0;
					String type = null;

					if ("BUSINESS_SEARCH".equals(strOperation)) {
						type = "bussiness";
					} else if ("NONBUSINESS_SEARCH".equals(strOperation)) {
						type = "nearby";
					}

					if ("LOC_SCHOOL".equals(strPoi)) {
						strPoi = "学校";
					}
					if ("CURRENT_CITY".equals(strCity)) {
						strCity = "";
					}

					if (TextUtils.isEmpty(strKeywords)) {
						strKeywords = jsonIntent.getString("category");
					}

					if ("CURRENT_LOC".equals(strPoi)) {
						strPoi = "";
					}else if(("DES_LOC").equals(strPoi)){
						strPoi = "";
					}
					

					if (jsonIntent.containsKey("radius")) {
						radius = jsonIntent.getIntValue("radius");
					}

					if (!TextUtils.isEmpty(strKeywords)) {
						if (isNavBy(strKeywords,strVoiceText)
								|| strVoiceText.contains("目的地") || strVoiceText.contains("终点")
								|| strVoiceText.contains("沿途") || strVoiceText.contains("途经")
								|| strVoiceText.contains("途中")|| strVoiceText.contains("中途")) {
							voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;

						}
						jsonResult = genVoiceJSONObject(strScene, "search",
								strVoiceText);
						jsonResult.put("city", strCity);
						jsonResult.put("poi", strPoi);
						jsonResult.put("keywords", strKeywords);
						jsonResult.put("radius", radius);
						jsonResult.put("region", region);
						if (!TextUtils.isEmpty(type)) {
							jsonResult.put("type",type);
						}

						//导航的目的地和文本相同则视为模糊语义
						if(strKeywords.equals(strVoiceText)){
							jsonResult.put("fuzzy", true);
						}
						
						voiceData.strVoiceData = jsonResult.toJSONString();
						return true;
					}

				}

			}

		} else if ("cn.yunzhisheng.traffic".equals(strService)) {
			if (strOperation.equals("TRAFFIC_QUERY")) {

				JSONObject jsonSemantic = rawJson.getJSONObject("semantic");
				if (null != jsonSemantic && jsonSemantic.containsKey("intent")) {
					jsonIntent = jsonSemantic.getJSONObject("intent");

					jsonResult = genVoiceJSONObject("traffic", "query",
							strVoiceText);
					jsonResult.put("strKeywords", jsonIntent.getString("road"));
					jsonResult.put("strDirection",
							jsonIntent.getString("direction"));
					jsonResult.put("strCity", jsonIntent.getString("city"));

					voiceData.strVoiceData = jsonResult.toJSONString();
					return true;
				}
			}
		} else if ("cn.yunzhisheng.setting.map".equals(strService)) {
			if ("QUERY_SETTING".equals(strOperation)) {
				JSONObject jsonSemantic = rawJson.getJSONObject("semantic");
				if (null != jsonSemantic && jsonSemantic.containsKey("intent")) {
					JSONObject jsonOperations = jsonSemantic.getJSONObject("intent").getJSONArray("operations").getJSONObject(0);

					String strOperator = jsonOperations.getString("operator");
					String strOperands = jsonOperations.getString("operands");
					if ("ACT_QUERY".equals(strOperator)) {
						if ("ATTR_SPEED_LIMIT".equals(strOperands)) {
							jsonResult = genVoiceJSONObject("limit_speed", "query",
									strVoiceText);
							voiceData.strVoiceData = jsonResult.toJSONString();
							return true;
						}
					}

				}
			}
		}

		return false;
	}

	private static String[] sArrNavByScene = { "途经", "先去", "经过", "走","顺便","中途","途中","沿途" };

	/**
	 * 判断是否是途径的说法
	 * 
	 * @param rawText
	 * @return
	 */
	private static boolean isNavBy(String keyWord,String rawText) {
		int index = -1;
		for (String str : sArrNavByScene) {
			index = rawText.lastIndexOf(str);
			if (index != -1) {
				if (!TextUtils.isEmpty(keyWord)) {
					if (rawText.indexOf(keyWord) > index) {
						return true;
					}
				}
				return false;
			}
		}

		return false;
	}

	private static boolean parseOLMusic(VoiceParseData voiceData,
			JSONObject rawJson) {
		String strService = rawJson.getString("service");
		String strVoiceText = rawJson.getString("text");
		String strOperation = rawJson.getString("code");

		JSONObject jsonResult;
		JSONObject jsonIntent;

		final String strScene = "music";
		final String strCMDPlay = "播放";
		final String strCMDConPlay = "继续播放";

		if (strCMDPlay.equals(strVoiceText)
				|| strCMDConPlay.equals(strVoiceText)) {
			jsonResult = genVoiceJSONObject(strScene, "continue", strVoiceText);
			voiceData.strVoiceData = jsonResult.toJSONString();

			return true;
		}

		if ("cn.yunzhisheng.music".equals(strService)) {
			jsonIntent = rawJson.getJSONObject("semantic").getJSONObject(
					"intent");

			if ("SEARCH_RANDOM".equals(strOperation)) {
				jsonResult = genVoiceJSONObject(strScene, "playRandom",
						strVoiceText);
				voiceData.strVoiceData = jsonResult.toJSONString();

				return true;
			}

			if ("QUERY_SONGNAME".equals(strOperation)) {
				jsonResult = genVoiceJSONObject(strScene, "ask", strVoiceText);
				voiceData.strVoiceData = jsonResult.toJSONString();

				return true;
			}

			String strKeyword = jsonIntent.getString("keyword");

			if (!TextUtils.isEmpty(strKeyword)
					&& strVoiceText.startsWith(strKeyword)) {
				jsonResult = genVoiceJSONObject("unknown", "unknown",
						voiceData.strText);
				jsonResult.put("fuzzy", true);
				voiceData.strVoiceData = jsonResult.toJSONString();
				voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE_LITTLE;
				return true;
			}
			
			JSONObject jsonModel = new JSONObject();
			JSONArray arrArtists = new JSONArray();
			JSONArray arrKeywords = new JSONArray();

			String strSong = jsonIntent.getString("song");
			if (!TextUtils.isEmpty(strSong)) {
				jsonModel.put("title", strSong);
			}

			String strArtist = jsonIntent.getString("artist");
			if (!TextUtils.isEmpty(strArtist)) {
				arrArtists.add(strArtist);
				jsonModel.put("artist", arrArtists);
			}

			String strAlbum = jsonIntent.getString("album");
			if (!TextUtils.isEmpty(strAlbum)) {
				jsonModel.put("album", strAlbum);
			}
			
			arrKeywords.add(strKeyword);

			if (jsonIntent.containsKey("language")) {
				arrKeywords.add(jsonIntent.getString("language"));
			} else if (jsonIntent.containsKey("genre")) {
				arrKeywords.add(jsonIntent.getString("genre"));
			} else if (jsonIntent.containsKey("musicTag")) {
				arrKeywords.add(jsonIntent.getString("musicTag"));
			} else if (jsonIntent.containsKey("mood")) {
				arrKeywords.add(jsonIntent.getString("mood"));
			}else if(jsonIntent.containsKey("billboard")){
				arrKeywords.add(jsonIntent.getString("billboard"));
			}else if (jsonIntent.containsKey("scene")) {
				String scene = jsonIntent.getString("scene");

				if ("收藏".equals(scene)) {
					jsonResult = genVoiceJSONObject(strScene,
							"playFavourMusic", strVoiceText);
					voiceData.strVoiceData = jsonResult.toJSONString();

					return true;
				}

				if (!TextUtils.isEmpty(scene)) {
					arrKeywords.add(scene);
				}
			}

			jsonModel.put("keywords", arrKeywords);
			jsonResult = genVoiceJSONObject(strScene, "play", strVoiceText);
			jsonResult.put("model", jsonModel);
			
			//语音与关键字完全相等视为模糊语义
			if(!TextUtils.isEmpty(strKeyword)
					&& strVoiceText.equals(strKeyword)){
				jsonResult.put("fuzzy", true);
			}
			
			
			voiceData.strVoiceData = jsonResult.toJSONString();

			return true;
		} else if ("cn.yunzhisheng.setting.mp".equals(strService)) {

			if ("SETTING_EXEC".equals(strOperation)
					|| "SETTING_EXEC_MP".equals(strOperation)) {
				jsonIntent = rawJson.getJSONObject("semantic").getJSONObject(
						"intent");

				String strAction = "";
				String strValue = "";
				String strConfirm = "";

				if (!jsonIntent.containsKey("operations")) {
					if (!jsonIntent.containsKey("valueExpr")) {
						strAction = jsonIntent.getString("operator");
					} else {
						strAction = jsonIntent.getString("valueExpr");
					}

					strValue = jsonIntent.getString("value");
					strConfirm = jsonIntent.getString("confirm");

					//修改音乐场景TXZ-5598，用于区分历史原因的云之声最早的版本，operands来区分是否用setting（.mp）的情况。
//					if (jsonIntent.containsKey("operands")) {
//						return false;
//					}
				} else {
					JSONArray arrOperations = jsonIntent
							.getJSONArray("operations");

					if (null != arrOperations && 0 != arrOperations.size()) {
						JSONObject jsonOperation = arrOperations
								.getJSONObject(0);

						if (!jsonOperation.containsKey("valueExpr")) {
							strAction = jsonOperation.getString("operator");
						} else {
							strAction = jsonOperation.getString("valueExpr");
						}

						strValue = jsonOperation.getString("value");
						strConfirm = jsonOperation.getString("confirm");

//						if (jsonOperation.containsKey("operands")) {
//							return false;
//						}
					}
				}

				if ("ACT_PAUSE".equals(strAction)) {
					jsonResult = genVoiceJSONObject(strScene, "pause",
							strVoiceText);
					voiceData.strVoiceData = jsonResult.toJSONString();

					return true;
				} else if ("ACT_PLAY".equals(strAction)) {
					if ("CANCEL".equals(strConfirm)) {
						jsonResult = genVoiceJSONObject(strScene, "exit",
								strVoiceText);
						voiceData.strVoiceData = jsonResult.toJSONString();

						return true;
					} else {
						jsonResult = genVoiceJSONObject(strScene, "play",
								strVoiceText);
						voiceData.strVoiceData = jsonResult.toJSONString();

						return true;
					}
				} else if ("ACT_STOP".equals(strAction)) {
					jsonResult = genVoiceJSONObject(strScene, "pause",
							strVoiceText);
					voiceData.strVoiceData = jsonResult.toJSONString();

					return true;
				} else if ("ACT_NEXT".equals(strAction)) {
					jsonResult = genVoiceJSONObject(strScene, "next",
							strVoiceText);
					voiceData.strVoiceData = jsonResult.toJSONString();

					return true;
				} else if ("ACT_PREV".equals(strAction)) {
					jsonResult = genVoiceJSONObject(strScene, "prev",
							strVoiceText);
					voiceData.strVoiceData = jsonResult.toJSONString();

					return true;
				} else if ("ACT_SET".equals(strAction)) {
					if ("MODE_SHUFFLE".equals(strValue)) {
						jsonResult = genVoiceJSONObject(strScene,
								"switchModeRandom", strVoiceText);
						voiceData.strVoiceData = jsonResult.toJSONString();

						return true;
					} else if ("MODE_ORDER".equals(strValue)) {
						jsonResult = genVoiceJSONObject(strScene,
								"switchModeLoopAll", strVoiceText);
						voiceData.strVoiceData = jsonResult.toJSONString();

						return true;
					} else if ("MODE_ALL_REPEAT".equals(strValue)) {
						jsonResult = genVoiceJSONObject(strScene,
								"switchModeLoopAll", strVoiceText);
						voiceData.strVoiceData = jsonResult.toJSONString();

						return true;
					} else if ("MODE_REPEAT_ONCE".equals(strValue)) {
						jsonResult = genVoiceJSONObject(strScene,
								"switchModeLoopOne", strVoiceText);
						voiceData.strVoiceData = jsonResult.toJSONString();

						return true;
					}
				} else if ("ACT_CLOSE".equals(strAction)) {
					jsonResult = genVoiceJSONObject(strScene, "exit",
							strVoiceText);
					voiceData.strVoiceData = jsonResult.toJSONString();

					return true;
				}

			}

		}

		return false;
	}

	private static String[] sArrAudioCmd = {"听","播放","播"};
	private static boolean parseOLFM(VoiceParseData voiceData,
			JSONObject rawJson) {
		String strService = rawJson.getString("service");

		if ("cn.yunzhisheng.broadcast".equals(strService)) {

			if (rawJson.getInteger("rc") == 0) {
				JSONObject jsonIntent = rawJson.getJSONObject("semantic")
						.getJSONObject("intent");
				JSONArray jsonChanelList = jsonIntent.getJSONArray("channelList");
				String strVoiceText = rawJson.getString("text");

				JSONObject jsonResult = new JSONObject();
				final String strScene = "radio";

				String strFreq = "";
				String strType = "";
				String strUnit = "";
				if (null != jsonChanelList && jsonChanelList.size() > 0) {
					JSONArray jsonFreqList = jsonChanelList.getJSONObject(0)
							.getJSONArray("frequencyList");

					if (null != jsonFreqList && jsonFreqList.size() > 0) {
						JSONObject jsonFreq = jsonFreqList.getJSONObject(0);
						strFreq = jsonFreq.getString("frequency");
						strType = jsonFreq.getString("type");
						strUnit = jsonFreq.getString("unit");
					}
				}
				if (TextUtils.isEmpty(strFreq)) {
					strFreq = jsonIntent.getString("frequency");
				}
				if (TextUtils.isEmpty(strType)) {
					strType = jsonIntent.getString("type");
				}
				if (TextUtils.isEmpty(strUnit)) {
					strUnit = jsonIntent.getString("unit");
				}

				String strTag = "";
				strTag = jsonIntent.getString("tag");

				if (TextUtils.isEmpty(strFreq)) {
					if (!TextUtils.isEmpty(strTag)) { //如果没有频率，但是有tag的时候，转成电台场景进行搜索；解决播放新闻电台的问题
						JSONObject jsonModel = new JSONObject();
						JSONArray jsonKeywords = new JSONArray();

						jsonKeywords.add(strTag);
						jsonModel.put("keywords", jsonKeywords);

						jsonResult = genVoiceJSONObject("audio", "play",
								strVoiceText);
						jsonResult.put("model", jsonModel);

						voiceData.strVoiceData = jsonResult.toJSONString();
						return true;
					}
					return false;
				}

				if ((!strVoiceText
						.matches("^(?:.*?)(?:调频到|调频|调幅到|调幅|(?i)FM(?-i)|(?i)AM(?-i))(?:..+)(?:兆赫|千赫|赫兹)?$")
						&& (AudioManager.getInstance().isAudioToolSet() || AudioManager
						.getInstance().hasRemoteTool())) && ProjectCfg.isUseRadioAsAudio()) {

					if (jsonChanelList != null && jsonChanelList.size() > 0) {

						String strKeyword = jsonChanelList.getJSONObject(0)
								.getString("channel");

						if (!strVoiceText.contains(strKeyword)) {
							strKeyword = strVoiceText;
							int index = -1;
							for (String cmd : sArrAudioCmd) {
								if (((index = strVoiceText.indexOf(cmd)) != -1)
										&& (index < (strVoiceText.length() - cmd.length()))) {
									strKeyword = strVoiceText.substring(index + cmd.length());
									break;
								}
							}
						}

						JSONObject jsonModel = new JSONObject();
						JSONArray jsonKeywords = new JSONArray();

						jsonKeywords.add(strKeyword);
						jsonModel.put("keywords", jsonKeywords);

						jsonResult = genVoiceJSONObject("audio", "play",
								strVoiceText);
						jsonResult.put("model", jsonModel);

						voiceData.strVoiceData = jsonResult.toJSONString();

						return true;
					}
					return false;
				}

				if ("FM".equals(strType)) {
					jsonResult = genVoiceJSONObject(strScene, "play",
							strVoiceText);

					String text = "调频";
					if (strFreq.indexOf('.') != -1) {
						text = text + strFreq.replace('.', '点');
					} else {
						text = text + strFreq;
					}
					byte result[] = NativeData.getNativeData(UiData.DATA_ID_TEST_WHOLE_REMOTE_KEYWORD, text);
					try {
						TestResp resp;
						resp = TestResp.parseFrom(result);
						if (resp.success) {
							voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_MORE;
							jsonResult.put("event", resp.event);
							jsonResult.put("subEvent", resp.subEvent);
//								jsonResult.put("cmd", new String(resp.data));
							VoiceData.RmtCmdInfo info = VoiceData.RmtCmdInfo.parseFrom(resp.data);
							jsonResult.put("data", info.rmtData);
							jsonResult.put("cmd", info.rmtCmd);
							jsonResult.put("service", info.rmtServName);
							jsonResult.put("scene", "command");
							jsonResult.put("action", "event");
							jsonResult.put("type", UiData.DATA_ID_TEST_REMOTE_KEYWORD);
							voiceData.strVoiceData = jsonResult.toJSONString();
							return true;
						}
					} catch (InvalidProtocolBufferNanoException e) {
					}

					jsonResult.put("waveband", "fm");
					jsonResult.put("hz", strFreq);
					jsonResult.put("unit", strUnit);

					voiceData.strVoiceData = jsonResult.toJSONString();
					return true;
				} else if ("AM".equals(strType)) {
					jsonResult = genVoiceJSONObject(strScene, "play",
							strVoiceText);
					jsonResult.put("waveband", "am");
					jsonResult.put("hz", strFreq);
					jsonResult.put("unit", strUnit);

					voiceData.strVoiceData = jsonResult.toJSONString();
					return true;
				}
			} else {
				//当没有找到频率的时候
				JSONObject jsonResult = new JSONObject();
				final String strScene = "radio";
				String strVoiceText = rawJson.getString("text");
				if (TextUtils.equals("FREQUENCY_INVALID",rawJson.getJSONObject("error").getString("code"))) {
					if (strVoiceText.matches("^(?:.*?)(?:调频到|调频|(?i)FM(?-i))(?:..+)(?:兆赫|千赫|赫兹)?$")) {
						jsonResult = genVoiceJSONObject(strScene, "play",
								strVoiceText);
						jsonResult.put("waveband", "fm");
						jsonResult.put("hz", -1);
						jsonResult.put("unit", "MHz");
						voiceData.strVoiceData = jsonResult.toJSONString();
						return true;
					}else if (strVoiceText.matches("^(?:.*?)(?:调幅到|调幅|(?i)AM(?-i))(?:..+)(?:兆赫|千赫|赫兹)?$")) {
						jsonResult = genVoiceJSONObject(strScene, "play",
								strVoiceText);
						jsonResult.put("waveband", "am");
						jsonResult.put("hz", -1);
						jsonResult.put("unit", "KHz");
						voiceData.strVoiceData = jsonResult.toJSONString();
						return true;
					}
				}
			}
		}

		return false;
	}

	private static boolean parseOLAudio(VoiceParseData voiceData,
			JSONObject rawJson) {
		String strService = rawJson.getString("service");
		String strVoiceText = rawJson.getString("text");

		if ("cn.yunzhisheng.audio".equals(strService)) {
			JSONObject jsonIntent = rawJson.getJSONObject("semantic")
					.getJSONObject("intent");

			JSONObject jsonResult;

			String strTitle = jsonIntent.getString("name");
			String strArtists = jsonIntent.getString("artist");
			String strKeyword = jsonIntent.getString("keyword");

			String strAlbum = jsonIntent.getString("album");

			String strCategory = jsonIntent.getString("category");
			String strTag = jsonIntent.getString("tag");
			String strEpisode = jsonIntent.getString("episode");
			String strSubCategory = jsonIntent.getString("subCategory");

			JSONObject jsonModel = new JSONObject();
			JSONArray jsonArtists = new JSONArray();
			JSONArray jsonKeywords = new JSONArray();

			if (!TextUtils.isEmpty(strTitle)) {
				jsonModel.put("title", strTitle);
			}

			if (!TextUtils.isEmpty(strArtists)) {
				jsonArtists.add(strArtists);
				jsonModel.put("artist", jsonArtists);
			}

			if (!TextUtils.isEmpty(strAlbum)) {
				jsonModel.put("album", strAlbum);
			}

			if (!TextUtils.isEmpty(strKeyword)) {
				jsonKeywords.add(strKeyword);
				jsonModel.put("keywords", jsonKeywords);
			}

			if (!TextUtils.isEmpty(strCategory)) {
				jsonModel.put("category", strCategory);
			}

			if (!TextUtils.isEmpty(strTag)) {
				jsonModel.put("tag", strTag);
			}

			if (!TextUtils.isEmpty(strEpisode)) {
				jsonModel.put("episode", strEpisode);
			}

			if (!TextUtils.isEmpty(strSubCategory)) {
				jsonModel.put("subCategory", strSubCategory);
			}

			jsonResult = genVoiceJSONObject("audio", "play", strVoiceText);
			jsonResult.put("model", jsonModel);
			jsonResult.put("code",rawJson.getString("code"));
			//电台名和文本相同则将其视为模糊语义
			if(!TextUtils.isEmpty(strTitle) && strTitle.equals(strVoiceText)){
				jsonResult.put("fuzzy", true);
			}
			if (voiceData.strText.contains("电影") && TextResultHandle.getInstance().mOpenTxzNlp)
				voiceData.floatTextScore = TextResultHandle.getInstance().TEXT_SCORE_CONFIDENCE_LITTLE;
			voiceData.strVoiceData = jsonResult.toJSONString();

			return true;
		}

		return false;
	}

	private static boolean parseOLQA(VoiceParseData voiceData,
			JSONObject rawJson) {
		String strService = rawJson.getString("service");
		String strVoiceText = rawJson.getString("text");
		String strOperation = rawJson.getString("code");

		JSONObject jsonResult;

		if ((("cn.yunzhisheng.chat".equals(strService) || "cn.yunzhisheng.calculator"
				.equals(strService)) && "ANSWER".equals(strOperation))
				|| ("cn.yunzhisheng.calendar".equals(strService) && "ANSWER"
						.equals(strOperation))
				|| "cn.yunzhisheng.cookbook".equals(strService)) {
			JSONObject jsonGeneral = rawJson.getJSONObject("general");
			if (jsonGeneral != null) {
				String strAnswer = jsonGeneral.getString(
						"text");
				if (strAnswer != null) {
					strAnswer = strAnswer.replaceAll("&nbsp;", "");
					// 去掉换行和空格白字符
					strAnswer = strAnswer
							.replaceAll("\\n", "")
							.replaceAll("\\t", "")
							.replaceAll(" ", "");
				}
				String strType = jsonGeneral.getString("type");

				if ("T".equals(strType) || "TU".equals(strType)) {
					if (!TextUtils.isEmpty(strAnswer)
							&& strAnswer.startsWith("TXZ_")) {
						strAnswer.substring(4);

						jsonResult = genVoiceJSONObject("command", "exec",
								strVoiceText);
						jsonResult.put("cmd", strAnswer);
						jsonResult.put("fuzzy", true);
						voiceData.strVoiceData = jsonResult.toJSONString();
						voiceData.strText = strAnswer;
						voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_MORE;

						return true;
					}

				}

				jsonResult = genVoiceJSONObject("unknown", "unknown", strVoiceText);
				voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE_LITTLE;
				if (jsonGeneral.containsKey("style")) {
					String mStyle = jsonGeneral.getString("style");
					if ("joke".equals(mStyle)) {
						if (isJock2AudioEnable()
								&& (AudioManager.getInstance().isAudioToolSet() || AudioManager.getInstance().hasRemoteTool())) {
							JSONObject jsonModel = new JSONObject();
							JSONArray jsonKeywords = new JSONArray();

							jsonKeywords.add("笑话");
							jsonModel.put("keywords", jsonKeywords);

							jsonResult = genVoiceJSONObject("audio", "play", strVoiceText);
							jsonResult.put("model", jsonModel);
							voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
							voiceData.strVoiceData = jsonResult.toJSONString();
							return true;
						} else {
							jsonResult.put("style", "joke");
							voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE;
						}
					} else if ("calculator".equals(mStyle)) {//计算
						jsonResult.put("style", "calculator");
						voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE;
					} else if ("calendar".equals(mStyle)) {//日历
						jsonResult.put("style", "calendar");
						voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE;
					} else if ("baike".equals(mStyle) || "BAIKE".equals(mStyle)) {//百科
						jsonResult.put("style", "baike");
						voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE;
					} else if ("story".equals(mStyle)) {//故事
						jsonResult.put("style", "story");
						voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE;
					} else if ("translation".equals(mStyle)) {//翻译
						jsonResult.put("style", "translation");
						voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE;
					} else if ("poem".equals(mStyle)) { //诗歌
						jsonResult.put("style", "poem");
						voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE;
					}
					if (!"unknown".equals(mStyle)) {
						jsonResult.put("answer", strAnswer);
					}
				} else {
					jsonResult.put("answer", strAnswer);
				}

				//聊天默认作为模糊语义
				jsonResult.put("fuzzy", true);
				voiceData.strVoiceData = jsonResult.toJSONString();

				return true;
			}
		}

		return false;
	}

	private static boolean parseOLTrafficControl(VoiceParseData voiceData,
			JSONObject rawJson) {
		String strService = rawJson.getString("service");
		String strVoiceText = rawJson.getString("text");
		String strOperation = rawJson.getString("code");
		JSONObject jsonIntent;

		final String strScene = "limit_number";
		JSONObject jsonResult;

		if ("cn.yunzhisheng.traffic.control".equals(strService)
				&& ("QUERY_TAIL_NUM_CONTROL".equals(strOperation) || "QUERY_OUTSIDE_CONTROL"
						.equals(strOperation))) {
			jsonIntent = rawJson.getJSONObject("semantic").getJSONObject(
					"intent");
			JSONObject jsonGeneral = rawJson.getJSONObject("general");

			if (jsonGeneral != null
					&& "T".equals(jsonGeneral.getString("type"))) {
				String strAnswer = rawJson.getJSONObject("general").getString(
						"text");

				if (!TextUtils.isEmpty(strAnswer)) {
					char sAnd = '和';
					char[] charArr = strAnswer.toCharArray();
					for (int i = 1, len = charArr.length; i < len - 1; i++) {
						char left = charArr[i - 1];
						char right = charArr[i + 1];
						char mid = charArr[i];

						if (',' == mid && left >= '0' && left <= '9'
								&& right >= '0' && right <= '9') {
							charArr[i] = sAnd;
						}
					}

					jsonResult = genVoiceJSONObject(strScene, "query",
							strVoiceText);
					jsonResult.put("city", jsonIntent.getString("city"));
					jsonResult.put("date", jsonIntent.getString("date"));
					jsonResult.put("result", new String(charArr));
					voiceData.strVoiceData = jsonResult.toJSONString();

					return true;
				}

			}

			jsonResult = genVoiceJSONObject(strScene, "query", strVoiceText);
			jsonResult.put("city", jsonIntent.getString("city"));
			jsonResult.put("date", jsonIntent.getString("date"));
			jsonResult.put(
					"result",
					NativeData.getResString(
							"RS_VOICE_TRAFFIC_CONTROL_NOT_FOUND").replace(
							"%CITY%", jsonIntent.getString("city")));
			voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
			voiceData.strVoiceData = jsonResult.toJSONString();

			return true;
		}

		return false;
	}
	
	private static boolean parseOLTicket(VoiceParseData voiceData,JSONObject rawJson){
		String strService = rawJson.getString("service");
		String strVoiceText = rawJson.getString("text");
		String strOperation = rawJson.getString("code");
		JSONObject jsonIntent;

		String strScene = "flight";
		JSONObject jsonResult;

		if (("cn.yunzhisheng.flight".equals(strService) && ("FLIGHT_ONEWAY".equals(strOperation) || "ANSWER".equals(strOperation)))
				|| ("cn.yunzhisheng.train".equals(strService) && ("TRAIN_ONEWAY".equals(strOperation) || "ANSWER".equals(strOperation) ))) {
			jsonIntent = rawJson.getJSONObject("semantic").getJSONObject(
					"intent");
			if ("cn.yunzhisheng.flight".equals(strService)) {
				strScene = "flight";
			}else if ("cn.yunzhisheng.train".equals(strService)) {
				strScene = "train";
			}
			
			jsonResult = genVoiceJSONObject(strScene, "query", strVoiceText);
			
			String origin = jsonIntent.getString("origin");
			if (jsonIntent.containsKey("origin")) {
				if (!TextUtils.isEmpty(origin)) {
					jsonResult.put("origin", origin);
				}
			}
			String destination = jsonIntent.getString("destination");
			if (jsonIntent.containsKey("destination")) {
				if (!TextUtils.isEmpty(destination)) {
					jsonResult.put("destination", destination);
				}
			}
			String departDate = jsonIntent.getString("departDate");
			if (jsonIntent.containsKey("departDate")) {
				if (!TextUtils.isEmpty(departDate)) {
					jsonResult.put("departDate", departDate);
				}
			}
			String departTime = jsonIntent.getString("departTime");
			if (jsonIntent.containsKey("departTime")) {
				if (!TextUtils.isEmpty(departTime)) {
					jsonResult.put("departTime", departTime);
				}
			}
			
			if ("ANSWER".equals(strOperation)) {
				if ("T".equals(rawJson.getJSONObject("general").getString("type"))) {
					if (TextUtils.equals(strScene, "flight")) {
						if (TextUtils.isEmpty(origin) || TextUtils.isEmpty(departDate) || TextUtils.isEmpty(destination) ) {
							if (!(strVoiceText.contains("飞机") || strVoiceText.contains("机票") || strVoiceText.contains("航班"))) {
								return false;
							}
						}else {
							voiceData.strVoiceData = jsonResult.toJSONString();
							return true;
						}
					}
					String answer = rawJson.getJSONObject("general").getString("text");
					if (!TextUtils.isEmpty(answer)) {
						jsonResult = genVoiceJSONObject(strScene, "answer", strVoiceText);
						jsonResult.put("answer", answer);
						voiceData.strVoiceData = jsonResult.toJSONString();
						return true;
					}
				}
			}
			
			//任意一个为空就是不完整的
			if (TextUtils.isEmpty(origin) || TextUtils.isEmpty(departDate) || TextUtils.isEmpty(destination) ) {
				jsonResult.put("action", "answer");
			}
			
			
			voiceData.strVoiceData = jsonResult.toJSONString();

			return true;
		}
		return false;
	}
	
	private static boolean parseOLAC(VoiceParseData voiceData,
			JSONObject rawJson) {
		String strService = rawJson.getString("service");
		String strVoiceText = rawJson.getString("text");
		String strCode = rawJson.getString("code");
		JSONObject jsonOperations;

		String strScene = "airC";
		JSONObject jsonResult;
		if ("cn.yunzhisheng.setting.ac".equals(strService)
				||"cn.yunzhisheng.setting.thermostat".equals(strService)
				||"cn.yunzhisheng.setting".equals(strService)
				||"cn.yunzhisheng.setting.air".equals(strService)) {
			if ("cn.yunzhisheng.setting".equals(strService)) {
				jsonOperations = rawJson.getJSONObject("semantic").getJSONObject(
						"intent");
			}else {
				jsonOperations = rawJson.getJSONObject("semantic").getJSONObject(
						"intent").getJSONArray("operations").getJSONObject(0);
			}
			jsonResult = genVoiceJSONObject(strScene, "cmd", strVoiceText);
			String strOperator = jsonOperations.getString("operator");
			String strOperands = jsonOperations.getString("operands");
			
			JSONObject jsonData = new JSONObject();
			if ("SETTING_EXEC_AC".equals(strCode)||"SETTING_EXEC".equals(strCode)) {
				if ("ATTR_TEMPERATURE".equals(strOperands)) {//温度相关
					jsonResult.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW);
					if ("ACT_SET".equals(strOperator)) {
						if (jsonOperations.containsKey("valueExpr")) {
							jsonResult.put("cmd", "AC_CMD_TEMPERATURE_CTRLTO");
							jsonData.put("tempValue", jsonOperations.getString("value"));
							jsonResult.put("voiceData", jsonData);
						}else {
							String value = jsonOperations.getString("value");
							jsonResult.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND);
							if ("TEMP_HIGHEST".equals(value)) {
								jsonResult.put("cmd", "AC_CMD_TEMPERATURE_MAX");
							}else if ("TEMP_LOWEST".equals(value)) {
								jsonResult.put("cmd", "AC_CMD_TEMPERATURE_MIN");
							}
						}
					}else if ("ACT_INCREASE".equals(strOperator)) {
						if (jsonOperations.containsKey("valueDeltaExpr")) {
							jsonResult.put("cmd", "AC_CMD_TEMPERATURE_CTRLUP");
							jsonData.put("tempRateValue", jsonOperations.getString("valueDelta"));
							jsonResult.put("voiceData", jsonData);
						}else {
//							String strValueDelta = jsonOperations.getString("valueDelta");
//							if ("VALUE_DELTA_SMALL".equals(strValueDelta)) {
								jsonResult.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND);
								jsonResult.put("cmd", "AC_CMD_TEMPERATURE_UP");
//							}
						}
					}else if ("ACT_DECREASE".equals(strOperator)) {
						if (jsonOperations.containsKey("valueDeltaExpr")) {
							jsonResult.put("cmd", "AC_CMD_TEMPERATURE_CTRLDOWN");
							jsonData.put("tempRateValue", jsonOperations.getString("valueDelta"));
							jsonResult.put("voiceData", jsonData);
						}else {
//							String strValueDelta = jsonOperations.getString("valueDelta");
//							if ("VALUE_DELTA_SMALL".equals(strValueDelta)) {
								jsonResult.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND);
								jsonResult.put("cmd", "AC_CMD_TEMPERATURE_DOWN");
//							}
						}
					}
				}else if ("OBJ_AC".equals(strOperands)) {
					jsonResult.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND);
					if ("ACT_OPEN".equals(strOperator)) {
						jsonResult.put("cmd", "AC_CMD_OPEN");
					}else if ("ACT_CLOSE".equals(strOperator)) {
						jsonResult.put("cmd", "AC_CMD_EXIT");
					}
				}else if ("ATTR_MODE".equals(strOperands)) {
					jsonResult.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND);
					if ("ACT_SET".equals(strOperator)) {
						String value = jsonOperations.getString("value");
						if ("MODE_COOL".equals(value)) {
							jsonResult.put("cmd", "AC_CMD_OPEN_AC");
						}else if ("MODE_AUTO".equals(value)) {
							jsonResult.put("cmd", "AC_CMD_OPEN_AUTO");
						}else if ("MODE_INNER_LOOP".equals(value)) {
							jsonResult.put("cmd", "AC_CMD_OPEN_INNER_LOOP");
						}else if ("MODE_OUTER_LOOP".equals(value)) {
							jsonResult.put("cmd", "AC_CMD_OPEN_OUTPUT_LOOP");
						}
					}else if ("ACT_UNSET".equals(strOperator)) {
						String value = jsonOperations.getString("value");
						if ("MODE_COOL".equals(value)) {
							jsonResult.put("cmd", "AC_CMD_EXIT_AC");
						}
					}
				}else if ("ATTR_WIND_SPEED".equals(strOperands)) {
					jsonResult.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND);
					if ("ACT_INCREASE".equals(strOperator)) {
						jsonResult.put("cmd", "AC_CMD_WIND_SPEED_UP");
					}else if ("ACT_DECREASE".equals(strOperator)) {
						jsonResult.put("cmd", "AC_CMD_WIND_SPEED_DOWN");
					}else if ("ACT_SET".equals(strOperator)) {
						String value = jsonOperations.getString("value");
						if (jsonOperations.containsKey("valueExpr")) {
							jsonResult.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW);
							jsonResult.put("cmd", "AC_CMD_WIND_SPEED_CTRLTO");
							jsonData.put("gearValue", value);
							jsonResult.put("voiceData", jsonData);
						} else if ("WIND_SPEED_HIGH".equals(value)) {
							jsonResult.put("cmd", "AC_CMD_WIND_SPEED_MAX");
						} else if ("WIND_SPEED_LOW".equals(value)) {
							jsonResult.put("cmd", "AC_CMD_WIND_SPEED_MIN");
						} else if ("WIND_SPEED_MEDIUM".equals(value)) {
							
						}
					} else if ("ACT_MAX".equals(strOperator)) {
						jsonResult.put("cmd", "AC_CMD_WIND_SPEED_MAX");
					} else if ("ACT_MIN".equals(strOperator)) {
						jsonResult.put("cmd", "AC_CMD_WIND_SPEED_MIN");
					}
				}else if ("ACT_GEAR_GOTO".equals(strOperator)) {
					jsonResult.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW);
					if (jsonOperations.containsKey("valueExpr")) {
						jsonResult.put("cmd", "AC_CMD_WIND_SPEED_CTRLTO");
						jsonData.put("gearValue", jsonOperations.getString("value"));
						jsonResult.put("voiceData", jsonData);
					}
				}
			}
			if (jsonResult.containsKey("cmd")) {
				if (!jsonResult.containsKey("voiceData")) {
					jsonResult.put("voiceData", "");
				}
				voiceData.strVoiceData = jsonResult.toJSONString();
				return true;
			}
		}
		return false;
	}
	
	private static boolean parseOLWechat(VoiceParseData voiceData,
			JSONObject rawJson) {
		String strService = rawJson.getString("service");
		String strVoiceText = rawJson.getString("text");
		String strCode = rawJson.getString("code");
		JSONObject jsonResult;
		JSONObject jsonIntent;
		if ("cn.yunzhisheng.sms".equals(strService)) {
			if ("SEND_WECHAT_MSG".equals(strCode)) {
				jsonIntent = rawJson.getJSONObject("semantic").getJSONObject("intent");
				jsonResult = genVoiceJSONObject("wechat", "send", strVoiceText);
				jsonResult.put("keywords", jsonIntent.getString("name"));
				voiceData.strVoiceData = jsonResult.toJSONString();
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean parseOLNews(VoiceParseData voiceData,
            JSONObject rawJson) {
        String strService = rawJson.getString("service");
        String strVoiceText = rawJson.getString("text");
        String strCode = rawJson.getString("code");
        if ("cn.yunzhisheng.news".equals(strService)) {
            if ("SEARCH".equals(strCode)) {
                JSONObject jsonResult;
                JSONObject jsonIntent;
                jsonIntent = rawJson.getJSONObject("semantic").getJSONObject("intent");
                
                JSONObject jsonModel = new JSONObject();
                JSONArray jsonKeywords = new JSONArray();
                String keyWord = jsonIntent.getString("keyword");
                String section = jsonIntent.getString("section");
                if (!TextUtils.isEmpty(section)) {
                    jsonKeywords.add(section);
                }
                if (TextUtils.isEmpty(keyWord) || TextUtils.equals(keyWord, "HEADLINE")) {
                    keyWord = "头条";
                }
                jsonKeywords.add(keyWord);
                jsonModel.put("keywords", jsonKeywords);

                jsonResult = genVoiceJSONObject("audio", "play", strVoiceText);
                jsonResult.put("model", jsonModel);
                voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
                voiceData.strVoiceData = jsonResult.toJSONString();
                return true;
            }
        }
        
        return false;
    }

	private static boolean parseOLWeather(VoiceParseData voiceData,
			JSONObject rawJson) {
		String strService = rawJson.getString("service");
		String strVoiceText = rawJson.getString("text");
		String strOperation = rawJson.getString("code");
		JSONObject jsonIntent;

		JSONObject jsonResult;
		try {
			if ("cn.yunzhisheng.weather".equals(strService)) {
				if ("FORECAST".equals(strOperation)) {
					if ("T".equals(rawJson.getJSONObject("general").getString(
							"type"))) {
						jsonIntent = rawJson.getJSONObject("semantic")
								.getJSONObject("intent");
						JSONObject jsonData = rawJson.getJSONObject("data");
						// 如果data没有header字段，取general.text作为header
						if (!jsonData.containsKey("header")) {
							String strAnswer = rawJson.getJSONObject("general")
									.getString("text");
							jsonData.put("header", strAnswer);
						}

					jsonResult = genVoiceJSONObject("weather", "query",
							strVoiceText);
					jsonResult.put("city", jsonIntent.getString("city"));
					jsonResult.put("date", jsonIntent.getString("focusDate"));
					jsonResult.put("data", jsonData);
					voiceData.strVoiceData = jsonResult.toJSONString();

						return true;
					}
				}
			}
		} catch (Exception e) {
			JNIHelper.logw(e.getMessage());
		}

		return false;
	}

	private static boolean parseOLStock(VoiceParseData voiceData,
			JSONObject rawJson) {
		String strService = rawJson.getString("service");
		String strVoiceText = rawJson.getString("text");
		String strOperation = rawJson.getString("code");

		JSONObject jsonResult;

		if ("cn.yunzhisheng.stock".equals(strService)) {
			if ("STOCK_INFO".equals(strOperation)) {
				JSONObject jsonGeneral = rawJson.getJSONObject("general");
				JSONObject jsonSemantic = rawJson.getJSONObject("semantic");
				String name = "";
				if(jsonSemantic != null){
					name = jsonSemantic.getJSONObject("intent").getString("name");
				}
				if (null != jsonGeneral
						&& "T".equals(jsonGeneral.getString("type"))) {
					JSONObject jsonData = rawJson.getJSONObject("data");
					if (jsonData != null) {
						String strStockName = jsonData.getJSONObject("result")
								.getString("mName");
						
						jsonResult = genVoiceJSONObject("stock", "query",
								strVoiceText);
						jsonResult.put("name", strStockName);
						jsonResult.put("data", jsonData);
						//股票信息与文本相同，视为模糊语义
						if(!TextUtils.isEmpty(name) && strVoiceText.equals(name)){
							jsonResult.put("fuzzy", true);
						}
						
						voiceData.strVoiceData = jsonResult.toJSONString();
					}else {
						voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE;
						jsonResult = genVoiceJSONObject("unknown", "unknown", strVoiceText);
						jsonResult.put("answer", jsonGeneral.getString("text"));
						voiceData.strVoiceData = jsonResult.toJSONString();
					}

					return true;
				}
			}
		}

		return false;
	}

	private static boolean parseOLSetting(VoiceParseData voiceData,
			JSONObject rawJson) {
		String strService = rawJson.getString("service");
		String strVoiceText = rawJson.getString("text");
		String strOperation = rawJson.getString("code");
		JSONObject jsonIntent;

		final String strScene = "system";
		JSONObject jsonResult = new JSONObject();

		if ("cn.yunzhisheng.setting".equals(strService)) {
			if ("SETTING_EXEC".equals(strOperation)) {
				jsonIntent = rawJson.getJSONObject("semantic").getJSONObject(
						"intent");

				String strAction = jsonIntent.getString("operator");
				String strObject = jsonIntent.getString("operands");

				String strChoice = "";

				if ("OBJ_MODEL_MUTE".equals(strObject)) {
					jsonResult = genVoiceJSONObject(strScene, "volume",
							strVoiceText);
					//关闭静音，即开启声音，使用open
					if ("ACT_CLOSE".equals(strAction)) {
						strChoice = "open";
					} else if ("ACT_OPEN".equals(strAction)) {
						strChoice = "close";
					}
				} else if ("OBJ_VOLUMN".equals(strObject)) {
					jsonResult = genVoiceJSONObject(strScene, "volume",
							strVoiceText);

					if ("ACT_MAX".equals(strAction)) {
						strChoice = "max";
					} else if ("ACT_MIN".equals(strAction)) {
						strChoice = "min";
					} else if ("ACT_INCREASE".equals(strAction)) {
						if (jsonIntent.containsKey("value")){
							strChoice = "set";
							Integer value = jsonIntent.getInteger("value");
							if (value != null) {
								jsonResult.put("value", value);
							}
						}else {
							strChoice = "up";
							Integer value = jsonIntent.getInteger("valueDelta");
							if (value != null) {
								jsonResult.put("value", value);
							}
						}
					} else if ("ACT_DECREASE".equals(strAction)) {
						if (jsonIntent.containsKey("value")){
							strChoice = "set";
							Integer value = jsonIntent.getInteger("value");
							if (value != null) {
								jsonResult.put("value", value);
							}
						}else {
							strChoice = "down";
							Integer value = jsonIntent.getInteger("valueDelta");
							if (value != null) {
								jsonResult.put("value", value);
							}
						}
					} else if ("ACT_SET".equals(strAction)) {
						strChoice = "set";
						Integer value = jsonIntent.getInteger("value");
						if (value != null){
							jsonResult.put("value",value);
						}

					}
				} else if ("OBJ_LIGHT".equals(strObject)) {
					jsonResult = genVoiceJSONObject(strScene, "light",
							strVoiceText);

					if ("ACT_MAX".equals(strAction)) {
						strChoice = "max";
					} else if ("ACT_MIN".equals(strAction)) {
						strChoice = "min";
					} else if ("ACT_INCREASE".equals(strAction)) {
						strChoice = "up";
					} else if ("ACT_DECREASE".equals(strAction)) {
						strChoice = "down";
					}
				} else if ("OBJ_WIFI".equals(strObject)) {
					jsonResult = genVoiceJSONObject(strScene, "wifi",
							strVoiceText);

					if ("ACT_CLOSE".equals(strAction)) {
						strChoice = "close";
					} else if ("ACT_OPEN".equals(strAction)) {
						strChoice = "open";
					}
				}

				if (!TextUtils.isEmpty(strChoice)) {
					jsonResult.put("choice", strChoice);
					voiceData.strVoiceData = jsonResult.toJSONString();

					return true;
				}
			}
		}

		return false;
	}

	private static boolean parseOLApp(VoiceParseData voiceData,
			JSONObject rawJson) {
		String strService = rawJson.getString("service");
		String strVoiceText = rawJson.getString("text");
		String strOperation = rawJson.getString("code");
		JSONObject jsonIntent;

		final String strScene = "app";
		JSONObject jsonResult = new JSONObject();

		if ("cn.yunzhisheng.appmgr".equals(strService)) {
			jsonIntent = rawJson.getJSONObject("semantic").getJSONObject(
					"intent");

			String strName = jsonIntent.getString("name");

			if ("微信".equals(strName) || "音乐".equals(strName)) {
				voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
			}

			if ("APP_LAUNCH".equals(strOperation)) {
				jsonResult = genVoiceJSONObject(strScene, "open", strVoiceText);
				jsonResult.put("name", strName);
				//当APP名称和文本相同时，视为模糊语义
				if(!TextUtils.isEmpty(strName) && strName.equals(strVoiceText)){
					jsonResult.put("fuzzy", true);
				}
				
				voiceData.strVoiceData = jsonResult.toJSONString();

				return true;
			} else if ("APP_EXIT".equals(strOperation)) {
				jsonResult = genVoiceJSONObject(strScene, "close", strVoiceText);
				jsonResult.put("name", strName);
				//当APP名称和文本相同时，视为模糊语义
				if(!TextUtils.isEmpty(strName) && strName.equals(strVoiceText)){
					jsonResult.put("fuzzy", true);
				}
				voiceData.strVoiceData = jsonResult.toJSONString();

				return true;
			}
		}

		return false;
	}

	private static boolean parseOLHelp(VoiceParseData voiceData,
			JSONObject rawJson) {
		String strService = rawJson.getString("service");
		String strVoiceText = rawJson.getString("text");
		String strOperation = rawJson.getString("code");

		if ("cn.yunzhisheng.help".equals(strService)
				&& "APP_HELP".equals(strOperation)) {
			JSONObject jsonResult = genVoiceJSONObject("help", "open",
					strVoiceText);
			voiceData.strVoiceData = jsonResult.toJSONString();

			return true;
		}

		return false;
	}

	private static boolean parseOLReminder(VoiceParseData voiceData,
										   JSONObject rawJson) {
		String strService = rawJson.getString("service");
		String strVoiceText = rawJson.getString("text");
		if ("cn.yunzhisheng.reminder".equals(strService)) {
				JSONObject jsonIntent = rawJson.getJSONObject("semantic");
				if (jsonIntent != null) {
					JSONObject jsonObject = jsonIntent.getJSONObject("intent");
					String dateTime = jsonObject.getString("dateTime");//UNKNOWN
					String eventTime = jsonObject.getString("eventTime");//UNKNOWN
					String content = jsonObject.getString("content");
					String repeatType = jsonObject.getString("repeatType");//	取值如下： OFF：不循环，DAY：每天循环一次 ;WORKDAY：工作日每天一次，WEEKEND：周末每天一次;WEEK：每周循环一次，MONTH：每月一次;YEAR：每年一次
					JSONObject jsonResult = genVoiceJSONObject("reminder", "unknow",strVoiceText);
					jsonResult.put("dateTime",dateTime);
					jsonResult.put("eventTime",eventTime);
					jsonResult.put("content",content);
					jsonResult.put("repeatType",repeatType);
					voiceData.strVoiceData = jsonResult.toJSONString();
				}
				voiceData.floatTextScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE_LITTLE;
				return true;
			}
		return false;
	}

	private static boolean parseOLCommonFunction(VoiceParseData voiceData,
			JSONObject rawJson) {

		if (parseOLApp(voiceData, rawJson))
			return true;
		if (parseOLMusic(voiceData, rawJson))
			return true;
		if (parseOLWeather(voiceData, rawJson))
			return true;
		if (parseOLStock(voiceData, rawJson))
			return true;
		if (parseOLSetting(voiceData, rawJson))
			return true;
		if (parseOLTrafficControl(voiceData, rawJson))
			return true;
		if (parseOLTicket(voiceData,rawJson))
			return true;
		if (parseOLAC(voiceData,rawJson)) {
			return true;
		}
		if (parseOLWechat(voiceData,rawJson)) {
			return true;
		}
		if (parseOLNews(voiceData,rawJson)) {
            return true;
        }
        if(parseOLReminder(voiceData,rawJson)) {
			return true;
		}

		return false;
	}

	private static JSONObject genVoiceJSONObject(String scene, String action,
			String text) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("scene", scene);
		jsonObject.put("action", action);
		jsonObject.put("text", text);

		return jsonObject;
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
	
	private static boolean isJock2AudioEnable(){
		UiEquipment.ServerConfig pbServerConfig = ConfigManager.getInstance()
				.getServerConfig();
		if (pbServerConfig == null
				|| pbServerConfig.uint64Flags == null
				|| ((pbServerConfig.uint64Flags & UiEquipment.SERVER_CONFIG_FLAG_JOKE_TO_AUDIO) == 0)) {
			return false;
		}
		return true;
	}
}

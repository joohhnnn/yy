package com.txznet.txz.component.text.yunzhisheng_3_0;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.txz.ui.data.UiData;
import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.text.TextResultHandle;

public class YZSLocalJsonConver {

	private String mRawText;
	private String mJson;
	private float mScore;
	private String mScene;
	private int mTextMask;

	public String getRawText() {
		return mRawText;
	}

	public String getJson() {
		return mJson;
	}

	public float getScore() {
		return mScore;
	}

	public String getScene() {
		return mScene;
	}
	
	public int getTextMask() {
		return mTextMask;
	}

	public YZSLocalJsonConver(VoiceParseData parseData) {
		mJson = jsonConvert(parseData.strVoiceData);
	}

	private void readJson(Map<String, String> mapSlots, JSONObject jo,
			StringBuffer sb) throws JSONException {
		if (jo.has("w")) {
			sb.append(jo.getString("w"));
			String t = jo.getString("t");
			String s = mapSlots.get(t);
			if (null == s) {
				mapSlots.put(t, jo.getString("w"));
			} else {
				mapSlots.put(t, s + jo.getString("w"));
			}
			return;
		}

		if (jo.has("c")) {
			readJson(mapSlots, jo.getJSONArray("c").getJSONObject(0), sb);
			return;
		}

		if (jo.has("l")) {
			JSONArray l = jo.getJSONArray("l");
			for (int i = 0; i < l.length(); i++) {
				readJson(mapSlots, l.getJSONObject(i), sb);
			}
			return;
		}
	}
	public static final double DOUBLE = 0.00000001;
	public String jsonConvert(String src) {
		double score = 0.0;
		int confidence = 1;
		Map<String, String> mapSlots = new HashMap<String, String>();

		StringBuffer sb = new StringBuffer();
		JSONObject jo;
		JSONObject des = null;
		try {
			jo = new JSONObject(src);
			JSONObject c = jo.getJSONArray("c").getJSONObject(0);
			score = c.getDouble("score");
			if (score > -3.0) {
				confidence = 90;
			} else if (score > -5.0) {
				confidence = 70;
			} else if (score > -6.0) {
				confidence = 50;
			} else if (score > -8.0) {
				confidence = 40;
			}

			readJson(mapSlots, c, sb);
			mScore = confidence;
			mRawText = sb.toString();

			des = new JSONObject();
			des.put("local", true);
			if (score > -20 - DOUBLE && score < -20 + DOUBLE) {
				des.put("score", confidence);
				mScene = "empty";
				des.put("scene", "empty");
				des.put("action", "empty");
				mRawText = "";
				return des.toString();
			}
			else if (confidence <= 30) {
				des.put("text", sb.toString());
				des.put("score", confidence);
				mScene = "unknown";
				des.put("scene", "unknown");
				des.put("action", "unknown");
				mRawText = "";
				return des.toString();
			}

			des.put("text", sb.toString());
			des.put("score", confidence);
			
			if (mapSlots.containsKey("cnclOk")) {
				mScene = "local";
				des.put("scene", "local");
				des.put("action", "makeCall");
			} else if (mapSlots.containsKey("cnclCancel")) {
				mScene = "local";
				des.put("scene", "local");
				des.put("action", "cancelCall");
			} else if (mapSlots.containsKey("incmN")
					|| mapSlots.containsKey("incmOff")) {
				mScene = "local";
				des.put("scene", "local");
				des.put("action", "reject");
			} else if (mapSlots.containsKey("incmCmd")
					|| mapSlots.containsKey("incmY")
					|| mapSlots.containsKey("incmSec")) {
				mScene = "local";
				des.put("scene", "local");
				des.put("action", "accept");
			} else if (mapSlots.containsKey("smsCmd")
					|| mapSlots.containsKey("smsY")
					|| mapSlots.containsKey("smsN")) {
				mScene = "local";
				des.put("scene", "local");
				if (mapSlots.containsKey("smsY")) {
					des.put("action", "smsY");
				} else if (mapSlots.containsKey("smsN")) {
					des.put("action", "smsN");
				}
			} else if (mapSlots.containsKey("selOk")) {
				mScene = "local";
				des.put("scene", "local");
				des.put("action", "makesure");
			} else if (mapSlots.containsKey("selCancel")) {
				mScene = "local";
				des.put("scene", "local");
				des.put("action", "cancel");
			} else if (mapSlots.containsKey("cmdKeywords")) {
				mScene = "command";
				des.put("scene", "command");
				des.put("action", "exec");
				des.put("cmd", mapSlots.get("cmdKeywords"));
			} else if (mapSlots.containsKey("cmdAppNames")) {
				if (mapSlots.containsKey("cmdAppOpen")) {
					mScene = "app";
					des.put("scene", "app");
					des.put("action", "open");
					des.put("name", mapSlots.get("cmdAppNames"));
				} else if (mapSlots.containsKey("cmdAppClose")) {
					mScene = "app";
					des.put("scene", "app");
					des.put("action", "close");
					des.put("name", mapSlots.get("cmdAppNames"));
				}
			} else if (mapSlots.containsKey("cmuKeywords")) {
				mScene = "command";
				des.put("scene", "command");
				des.put("action", "exeu");
				des.put("cmd", mapSlots.get("cmuKeywords"));
			}else if (mapSlots.containsKey("ruleKeywords")) {
				mTextMask = TextResultHandle.MODULE_LOCAL_NORMAL_MASK | TextResultHandle.MODULE_LOCAL_HIGH_MASK;
				mScene = "command";
				des.put("scene", "command");
				des.put("action", "exeu");
				des.put("cmd", mapSlots.get("ruleKeywords"));
			} else if (mapSlots.containsKey("toFm")) {
				mScene = "radio";
				des.put("scene", "radio");
				des.put("action", "play");
				des.put("waveband", "fm");
				des.put("unit", "MHZ");
				String hz = mapSlots.get("fmFreqValue");
				if (hz != null)
					des.put("hz", hz);
			} else if (mapSlots.containsKey("toAm")) {
				mScene = "app";
				des.put("scene", "radio");
				des.put("action", "play");
				des.put("waveband", "am");
				des.put("unit", "KHZ");
				String hz = mapSlots.get("amValue");
				if (hz != null)
					des.put("hz", hz);
			} else if (mapSlots.containsKey("nwsNews")) {
				mScene = "audio";
				des.put("scene", "audio");
				des.put("action", "new");
				String topic = mapSlots.get("nwsKw");
				if (topic != null)
					des.put("topic", topic);
			} else if (mapSlots.containsKey("mscSinger")
					|| mapSlots.containsKey("mscAlbum")
					|| mapSlots.containsKey("mscList")
					|| mapSlots.containsKey("mscType")) {
				mScene = "music";
				mTextMask = TextResultHandle.MODULE_LOCAL_NORMAL_MASK | TextResultHandle.MODULE_LOCAL_HIGH_MASK;
				des.put("scene", "music"); // ? music/audio
				des.put("action", "play");
				String artist = mapSlots.get("mscSinger");
				String album = mapSlots.get("mscAlbum");
				String title = mapSlots.get("mscList");
				String keyword = mapSlots.get("mscType");

				JSONObject model = new JSONObject();
				if (artist != null) {
					JSONArray artists = new JSONArray();
					artists.put(artist);
					model.put("artist", artists);
				}
				if (album != null)
					model.put("album", album);
				if (title != null)
					model.put("title", title);
				if (keyword != null) {
					JSONArray keywords = new JSONArray();
					keywords.put(keyword);
					model.put("keywords", keywords);
				}
				des.put("model", model);
			} else if ((confidence == 0 || confidence >= 50)
					&& (mapSlots.containsKey("callCon1")
							|| mapSlots.containsKey("callCon2")
							|| mapSlots.containsKey("callUsual")
							|| mapSlots.containsKey("callUnusual")
							|| mapSlots.containsKey("callService")
							|| mapSlots.containsKey("callEnglish")
							|| mapSlots.containsKey("allAlpha")
					        || mapSlots.containsKey("allCh")
							|| mapSlots.containsKey("callNumber"))) {
				// type
				if (mapSlots.containsKey("callPhoneZ")) {
					des.put("type", mapSlots.get("callPhoneZ"));
					des.put("p", "z");
				} else if (mapSlots.containsKey("callPhoneM")) {
					des.put("type", mapSlots.get("callPhoneM"));
					des.put("p", "m");
				} else if (mapSlots.containsKey("callPhoneS")) {
					des.put("type", mapSlots.get("callPhoneS"));
					des.put("p", "s");
				}
				if (mapSlots.containsKey("callSearch")) {
					des.put("code", "CONTACT_SEARCH");
				}

				String strContact = null;
				String strChaosContact = null;
				String strEnglishContact = null;
				String strNumber = null;
				String strPrefix = "";
				String strSuffix = "";

				if (mapSlots.containsKey("callPrefix")) {
					strPrefix = mapSlots.get("callPrefix");
				}
				if (mapSlots.containsKey("callSuffix")) {
					strSuffix = mapSlots.get("callSuffix");
				}

				if (mapSlots.containsKey("callCon1")) {
					strContact = mapSlots.get("callCon1");
				} else if (mapSlots.containsKey("callCon2")) {
					strContact = mapSlots.get("callCon2");
				} else if (mapSlots.containsKey("callUsual")) {
					strContact = mapSlots.get("callUsual");
				} else if (mapSlots.containsKey("callUnusual")) {
					strContact = mapSlots.get("callUnusual");
				} else if (mapSlots.containsKey("callService")) {
					strContact = mapSlots.get("callService");
				} else if (mapSlots.containsKey("callEnglish")) {
					strContact = mapSlots.get("callEnglish");
				} else if (mapSlots.containsKey("callNumber")) {
					strNumber = mapSlots.get("callNumber");
				}

				if (mapSlots.containsKey("allAlpha")) {
					strEnglishContact = mapSlots.get("allAlpha").toLowerCase();
				}

				mScene = "call";
				des.put("scene", "call");
				des.put("action", "make");
				des.put("mold", TextResultHandle.CALL_LOCAL_YZS_CH);
				if (!TextUtils.isEmpty(strContact)) {
					des.put("name", strContact);
					des.put("prefix", strPrefix);
					des.put("suffix", strSuffix);
				} else if (!TextUtils.isEmpty(strChaosContact)) {//strChaosContact没赋值
					des.put("name", strChaosContact);
				} else if (!TextUtils.isEmpty(strEnglishContact)) {
					des.put("name", strEnglishContact);
					des.put("prefix", strPrefix);
					des.put("suffix", strSuffix);
					des.put("mold", TextResultHandle.CALL_LOCAL_YZS_EN);
				} else if (!TextUtils.isEmpty(strNumber)) {
					des.put("number",strNumber);
				}
				if(mapSlots.containsKey("checkOut")){
					des.put("action", "search");
				}
			} else if ((confidence == 0 || confidence >= 50)
					&& (mapSlots.containsKey("WeChatFriend") || mapSlots
							.containsKey("WeChatGroup"))) {
				mScene = "wechat";
				if (mapSlots.containsKey("WeChatFriend")) {
					des.put("keywords", mapSlots.get("WeChatFriend"));
				}
				if (mapSlots.containsKey("WeChatGroup")) {
					des.put("keywords", mapSlots.get("WeChatGroup"));
				}
				des.put("scene", "wechat");
				des.put("action", "send");
			} else if (mapSlots.containsKey("askMscNow")) {
				mTextMask = TextResultHandle.MODULE_LOCAL_NORMAL_MASK | TextResultHandle.MODULE_LOCAL_HIGH_MASK;
				mScene = "music";
				des.put("scene", "music"); // ? music/audio
				des.put("action", "ask");
			} else if (mapSlots.containsKey("navHome")) {
				mScene = "nav";
				des.put("scene", "nav");
				des.put("action", "home");
			} else if (mapSlots.containsKey("navCompany")) {
				mScene = "nav";
				des.put("scene", "nav");
				des.put("action", "company");
			} else if (mapSlots.containsKey("navPOI")) {
				mScene = "nav";
				des.put("scene", "nav");
				des.put("action", "search");
				des.put("keywords", mapSlots.get("navPOI"));
			} else if (mapSlots.containsKey("navNearAct")) {
				mScene = "nav";
				des.put("scene", "nav");
				des.put("action", "search");
				des.put("keywords", mapSlots.get("navNearAct"));
			} else if (mapSlots.containsKey("navNearKw")) {
				mScene = "nav";
				des.put("scene", "nav");
				if (mapSlots.containsKey("navPass")) {
					des.put("action", "pass");
				} else if (mapSlots.containsKey("navEnd")) {
					des.put("action", "search");
					des.put("poi", "END_POI");
				} else {
					des.put("action", "search");
				}
				des.put("keywords", mapSlots.get("navNearKw"));
			} else if (mapSlots.containsKey("navCmdWord")) {
			    mScene = "nav";
                des.put("scene", "nav");
                des.put("action", "search");
                des.put("keywords", "CURRENT_LOC");
            }else if (mapSlots.containsKey("ctrlTo")||mapSlots.containsKey("gearValue")) {
				mScene = "command";
				des.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW);
				des.put("subEvent", 0);
				des.put("type", UiData.DATA_ID_TEST_LOCAL_KEYWORD);
				des.put("scene", "command");
				des.put("action", "event");
				JSONObject jsonData = new JSONObject();
				if (mapSlots.containsKey("tempUnit")) {
					des.put("cmd", "AC_CMD_TEMPERATURE_CTRLTO");
					jsonData.put("tempValue", mapSlots.get("tempValue"));
				}else if (mapSlots.containsKey("gearUnit")) {
					des.put("cmd", "AC_CMD_WIND_SPEED_CTRLTO");
					jsonData.put("gearValue", mapSlots.get("gearValue"));
				}
				des.put("voiceData", jsonData);
			}else if (mapSlots.containsKey("ctrlUp")) {
				mScene = "command";
				des.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW);
				des.put("subEvent", 0);
				des.put("type", UiData.DATA_ID_TEST_LOCAL_KEYWORD);
				des.put("scene", "command");
				des.put("action", "event");
				des.put("cmd", "AC_CMD_TEMPERATURE_CTRLUP");
				JSONObject jsonData = new JSONObject();
				jsonData.put("tempRateValue", mapSlots.get("tempRateValue"));
				des.put("voiceData", jsonData);
			}else if (mapSlots.containsKey("ctrlDown")) {
				mScene = "command";
				des.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW);
				des.put("subEvent", 0);
				des.put("type", UiData.DATA_ID_TEST_LOCAL_KEYWORD);
				des.put("scene", "command");
				des.put("action", "event");
				des.put("cmd", "AC_CMD_TEMPERATURE_CTRLDOWN");
				JSONObject jsonData = new JSONObject();
				jsonData.put("tempRateValue", mapSlots.get("tempRateValue"));
				des.put("voiceData", jsonData);
			}else if (mapSlots.containsKey("flow")) {
				mScene = "flow";
				des.put("scene", "flow");
				des.put("action", "setting");
				des.put("flowValue", mapSlots.get("flow"));
			}else if (mapSlots.containsKey("findNavHst")) {
                mScene = "nav";
                des.put("scene", "nav");
                des.put("action", "openHst");
            }else if (mapSlots.containsKey("vlCtrlTo")){
				mScene = "system";
				des.put("scene","system");
				des.put("choice","set");
				des.put("action","volume");
				des.put("value",mapSlots.get("volumeValue"));
			}else if (mapSlots.containsKey("vlCtrlUp")) {
				mScene = "system";
				des.put("scene","system");
				des.put("choice","up");
				des.put("action","volume");
				des.put("value",mapSlots.get("volumeRateValue"));
			}else if (mapSlots.containsKey("vlCtrlDown")) {
				mScene = "system";
				des.put("scene","system");
				des.put("choice","down");
				des.put("action","volume");
				des.put("value",mapSlots.get("volumeRateValue"));
			}else if (mapSlots.containsKey("fmFreqName")){  
				mScene = "radio";
				des.put("scene", "radio");
				des.put("action", "playName");
				des.put("unit", "MHZ");
				des.put("waveband", "fm");
				String freqName = mapSlots.get("fmFreqName");
				if (freqName != null)
					des.put("freqName", freqName);
			}else if (mapSlots.containsKey("wakeupKeywords")) {
				mScene = "unknown";
				des.put("scene", "unknown");
				des.put("action", "unknown");
				des.put("answer", NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_WAKEUP_KEYWORD"));
			}else{
				mScene = "unknown";
				des.put("scene", "unknown");
				des.put("action", "unknown");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			if (des != null && des.has("text")) {
				return des.toString();
			}
			return null;
		}

		return des.toString();
	}
}

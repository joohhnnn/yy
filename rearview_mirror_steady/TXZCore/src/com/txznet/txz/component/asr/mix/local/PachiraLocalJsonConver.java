package com.txznet.txz.component.asr.mix.local;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.txz.component.text.IText;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.text.TextResultHandle;

public class PachiraLocalJsonConver {
	private String mRawText;
	private String mJson;
	private float mScore;
	private String mScene;
	
	public PachiraLocalJsonConver(VoiceParseData parseData) {
		mRawText = "";
		mJson = "";
		mScore = 0f;
		mScene = "";
		mJson = jsonConvert(parseData);
	}
	
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

	
	private String jsonConvert(VoiceParseData parseData) {
		JSONObject newJson = new JSONObject();
		if(parseData == null){
			return newJson.toString();
		}
		String strVoiceData = parseData.strVoiceData;
		newJson.put("local", true);
		newJson.put("scene", "unknown");
		newJson.put("action", "unknown");
		mScore = TextResultHandle.TEXT_SCORE_MIN;
		if(TextUtils.isEmpty(strVoiceData)){
			return newJson.toString();
		}
		JSONObject json = new JSONObject();
		try {
			json = JSONObject.parseObject(strVoiceData);
		} catch (Exception e) {
			mRawText = "";
			JNIHelper.logd("PachiraLocalJsonConver JsonException : "+strVoiceData);
			return newJson.toString();
		}
		if(json.containsKey("pachira")){
			JSONObject jsonPachira = json.getJSONObject("pachira");
			if(jsonPachira == null || jsonPachira.isEmpty()){
				return newJson.toString();
			}
			if(jsonPachira.containsKey("rawtext")){
				mRawText = jsonPachira.getString("rawtext");
				newJson.put("text", mRawText);
			}
			String focus = "";
			String operation = "";
			JSONObject slots = new JSONObject();
			if(jsonPachira.containsKey("focus")){
				focus = jsonPachira.getString("focus");
			}
			if(jsonPachira.containsKey("operation")){
				operation = jsonPachira.getString("operation");
			}
			if(jsonPachira.containsKey("slots")){//slots???????????????????????????unknow??????
				try {
					slots = jsonPachira.getJSONObject("slots");
				} catch (Exception e) {
//					return newJson.toString();
				}
			}
			
			if(parseData.uint32Sence == VoiceData.GRAMMAR_SENCE_SET_COMPANY){
				newJson.put("scene", "nav");
				newJson.put("action", "modifyCompany");
				newJson.put("keywords", mRawText);
				return newJson.toString();
			}else if(parseData.uint32Sence == VoiceData.GRAMMAR_SENCE_SET_HOME){
				newJson.put("scene", "nav");
				newJson.put("action", "modifyHome");
				newJson.put("keywords", mRawText);
				return newJson.toString();
			}
			
			if ("NAVI".equals(focus)) {
				if ("route".equals(operation)) {
					mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
					mScene = "nav";
					newJson.put("scene", "nav");
					newJson.put("action", "search");
					newJson.put("text", mRawText);
					JSONObject jsonDest = slots.getJSONObject("destination");
					if(jsonDest.containsKey("name") && "???".equals(jsonDest.getString("name"))){
						newJson.put("action", "home");
					}
					if(jsonDest.containsKey("name") && "??????".equals(jsonDest.getString("name"))){
						newJson.put("action", "company");
					}
					if (jsonDest.containsKey("detail")) {
						newJson.put("keywords", jsonDest.getString("detail"));
					} else {
						genUnknownScene(newJson);
						return newJson.toString();
					}
					if (jsonDest.containsKey("province")) {
						newJson.put("city", jsonDest.getString("province"));
					}
					if (jsonDest.containsKey("city")) {
						newJson.put("city", jsonDest.getString("city"));
					}
				}
			}
			
			if("POI".equals(focus)){//??????????????????
				if("query".equals(operation)){
					if(slots.containsKey("name")){
						mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
						mScene = "nav";
						newJson.put("scene", "nav");
						newJson.put("action", "search");
						newJson.put("text", mRawText);
						newJson.put("keywords", slots.getString("name"));
					}
				}
			}
			
			if("HELP".equals(focus) && "action".equals(operation)){//????????????
				mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
				mScene = "help";
				newJson.put("scene", "help");
				if(TextUtils.equals("????????????", mRawText)){
					newJson.put("action", "open");
				}else{
					genUnknownScene(newJson);
				}
			}
			
			if("MUSIC".equals(focus)){//????????????
				if("play".equals(operation)){
					JSONObject model = new JSONObject();
					if(checkValid(slots, "song")){
						model.put("title", slots.getString("song"));
					}
					if(checkValid(slots, "artist")){
						model.put("artist", new String[] {slots.getString("artist")});
					}
					newJson.put("model", model);
					newJson.put("scene", "music");
					newJson.put("action", "play");
					newJson.put("text", mRawText);
					mScene = "music";
					mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
				}else if("mode".equals(operation)){
					if(slots.containsKey("type")){
						newJson.put("scene", "music");
						mScene = "music";
						mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
						String type = slots.getString("type");
						if("repeat".equals(type)){//????????????
							newJson.put("action", "switchModeLoopOne");
						}else if("sequence".equals(type)){//????????????
							newJson.put("action", "switchModeLoopAll");
						}else if("random".equals(type)){//????????????
							newJson.put("action", "switchModeRandom");
						}else if("last".equals(type)){//?????????
							newJson.put("action", "prev");
						}else if("next".equals(type)){//?????????
							newJson.put("action", "next");
						}else if("pause".equals(type)){//??????
							newJson.put("action", "pause");
						}else if("stop".equals(type)){//????????????
							newJson.put("action", "exit");
						}else if("continue".equals(type)){//????????????
							newJson.put("action", "continue");
						}else{
							genUnknownScene(newJson);
							return newJson.toString();
						}
					}
				}
			}
			
			if("PHONE".equals(focus)){//????????????
				if("call".equals(operation)){//?????????
					newJson.put("scene", "call");
					newJson.put("action", "make");
					newJson.put("text", mRawText);
					mScene = "call";
					mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
					if(slots.containsKey("name")){
						newJson.put("name", slots.getString("name"));
					}
					if(slots.containsKey("number")){
						newJson.put("number", slots.getString("number"));
					}
				}
			}
			
			if("RADIO".equals(focus)){//???????????????
				if("module".equals(operation)){
					newJson.put("scene", "radio");
					newJson.put("action", "play");
					newJson.put("text", mRawText);
					mScene = "radio";
					mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
					if(slots.containsKey("type")){
						String type = slots.getString("type");
						if("FM".equals(type)){
							newJson.put("waveband", "fm");
							newJson.put("unit", "MHZ");
						}else if("AM".equals(type)){
							newJson.put("waveband", "am");
							newJson.put("unit", "KHZ");
						}
					}
					if(slots.containsKey("frequency")){
						newJson.put("hz", slots.getString("frequency"));
					}
				}
			}
			
			if("APP".equals(focus)){//????????????????????????
				if("action".equals(operation)){
					newJson.put("scene", "app");
					newJson.put("text", mRawText);
					mScene = "app";
					mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
					if(slots.containsKey("type")){
						newJson.put("action", slots.getString("type"));
					}
					if(slots.containsKey("name")){
						newJson.put("name", slots.getString("name"));
					}
				}
			}
			
			if("OTHER".equals(focus)){//?????????poi???????????????OTHER??????
				newJson.put("fuzzy", true);
			}
		}
		
		return newJson.toString();
	}
	
	/**
	 * ??????json???????????????key?????????????????????????????????key??????????????????????????????-1???
	 * @param slots
	 * @param key
	 * @return
	 */
	private boolean checkValid(JSONObject slots, String key) {
		return slots.containsKey(key) && !TextUtils.equals(slots.getString(key),"-1");
	}

	private void genUnknownScene(JSONObject newJson) {
		mScore = TextResultHandle.TEXT_SCORE_MIN;
		newJson.put("scene", "unknown");
		newJson.put("action", "unknown");
	}
	
}

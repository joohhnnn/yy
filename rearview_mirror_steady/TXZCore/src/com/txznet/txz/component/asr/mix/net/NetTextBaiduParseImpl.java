package com.txznet.txz.component.asr.mix.net;

import com.alibaba.fastjson.JSONObject;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.text.TextResultHandle;

public class NetTextBaiduParseImpl {
	public void parseData(VoiceParseData parseData) {
		if (parseData.strText == null)
			parseData.strText = "";
		JSONObject object = null;
		try {
			object = JSONObject.parseObject(parseData.strVoiceData);
		} catch (Exception e) {
			parseUnknown(parseData);
			return;
		}
		if (object == null || !object.containsKey("result")
				|| !object.getJSONObject("result").containsKey("json_res")) {
			parseUnknown(parseData);
			return;
		}
		JSONObject root = null;
		try {
			root = JSONObject.parseObject(object.getJSONObject("result")
					.getString("json_res"));
		} catch (Exception e) {
			parseUnknown(parseData);
			return;
		}
		if (root == null
				|| !root.containsKey("merged_res")
				|| !root.getJSONObject("merged_res").containsKey(
						"semantic_form")
				|| !root.getJSONObject("merged_res")
						.getJSONObject("semantic_form").containsKey("results")) {
			parseUnknown(parseData);
			return;
		}
		JSONObject result = root.getJSONObject("merged_res")
				.getJSONObject("semantic_form").getJSONArray("results")
				.getJSONObject(0);
		if (!result.containsKey("domain") || !result.containsKey("intent")
				|| !result.containsKey("object")) {
			parseUnknown(parseData);
			return;
		}

		String domain = result.getString("domain");
		String intent = result.getString("intent");
//		int score = result.getIntValue("score");
		boolean ret = false;
		if (domain.equals("map")) {
			ret = parseMap(parseData, result.getJSONObject("object"), intent);
		} else if (domain.equals("telephone")) {
			ret = parseCall(parseData, result.getJSONObject("object"), intent);
		} else if (domain.equals("app")) {
			ret = parseApp(parseData, result.getJSONObject("object"), intent);
		} else if (domain.equals("music")) {
			ret = parseMusic(parseData, result.getJSONObject("object"), intent);
		} else if (domain.equals("codriver")) {
			ret = parseCodriver(parseData, result.getJSONObject("object"), intent);
		} else if (domain.equals("player")) {
			ret = parsePlayer(parseData, result.getJSONObject("object"), intent);
		} else if (domain.equals("navigate_instruction")) {
			ret = parseNav(parseData, result.getJSONObject("object"), intent);
		}
		if (ret == false) {
			parseUnknown(parseData);
		}
		JNIHelper.logd("nlp:bdRet="+ret+",result="+parseData.strVoiceData);
	}

	private JSONObject getJson(String scene, String action, String text) {
		JSONObject object = new JSONObject();
		object.put("scene", scene);
		object.put("action", action);
		object.put("text", text);
		return object;
	}

	private boolean parseMap(VoiceParseData parseData, JSONObject root,
			String intent) {
		if (intent.equals("location")) {
			JSONObject object = getJson("location", "query", parseData.strText);
			parseData.strVoiceData = object.toString();
			parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
			return true;
		}
		JSONObject object = getJson("nav", "search", parseData.strText);
		if (intent.equals("route")) {
			String to = root.getString("arrival");
			object.put("keywords", to);
		} else if (intent.equals("nearby")) {
			String center = root.getString("centre");
			String keywords = root.getString("keywords");
			object.put("keywords", keywords);
			object.put("poi", center);
		} else if (intent.equals("poi")) {
			String center = root.getString("centre");
			object.put("keywords", center);
		} else
			return false;
		parseData.strVoiceData = object.toString();
		parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
		return true;
	}

	private boolean parseCall(VoiceParseData parseData, JSONObject root,
			String intent) {
		if (intent.equals("call")) {
			JSONObject object = getJson("call", "make", parseData.strText);
			float subtract = 0;
			if (root.containsKey("name")) {
				String name = root.getString("name");
				if (name.matches("^[0-9]*$"))
					object.put("number", name);
				else
					object.put("name", name);
			}
			if (root.containsKey("number")) {
				String number = root.getString("number");
				if (number.length() > 11)
					subtract = 5f;
				object.put("number", number);
			}
			parseData.strVoiceData = object.toString();
			parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE - subtract;
			return true;
		}
		return false;
	}

	private boolean parseApp(VoiceParseData parseData, JSONObject root,
			String intent) {
		if (!root.containsKey("appname"))
			return false;
		if (!intent.equals("open") && !intent.equals("close"))
			return false;
		JSONObject object = getJson("app", intent, parseData.strText);
		object.put("name", root.getString("appname"));
		parseData.strVoiceData = object.toString();
		parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
		return true;
	}

	private boolean parseMusic(VoiceParseData parseData, JSONObject root,
			String intent) {
		if (intent.equals("play") || intent.equals("search")) {
			if (!root.containsKey("name") && !root.containsKey("byartist"))
				return false;
			JSONObject object = getJson("music", "play", parseData.strText);
			JSONObject model = new JSONObject();
			if (root.containsKey("name"))
				model.put("title", root.getString("name"));
			if (root.containsKey("byartist"))
				model.put("artist", root.getJSONArray("byartist"));
			object.put("model", model);
			parseData.strVoiceData = object.toString();
			parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
			return true;
		}
		return false;
	}

	private boolean parseCodriver(VoiceParseData parseData, JSONObject root,
			String intent) {
		if (intent.equals("volume_up")) {
			JSONObject object = getJson("system", "volume", parseData.strText);
			object.put("choice", "up");
			parseData.strVoiceData = object.toString();
			parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
			return true;
		} else if (intent.equals("volume_down")) {
			JSONObject object = getJson("system", "volume", parseData.strText);
			object.put("choice", "down");
			parseData.strVoiceData = object.toString();
			parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
			return true;
		} else if (intent.equals("volume_up_max")) {
			JSONObject object = getJson("system", "volume", parseData.strText);
			object.put("choice", "max");
			parseData.strVoiceData = object.toString();
			parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
			return true;
		} else if (intent.equals("volume_down_min")) {
			JSONObject object = getJson("system", "volume", parseData.strText);
			object.put("choice", "min");
			parseData.strVoiceData = object.toString();
			parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
			return true;
		} else if (intent.equals("light_up")) {
			JSONObject object = getJson("system", "light", parseData.strText);
			object.put("choice", "up");
			parseData.strVoiceData = object.toString();
			parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
			return true;
		} else if (intent.equals("light_down")) {
			JSONObject object = getJson("system", "light", parseData.strText);
			object.put("choice", "down");
			parseData.strVoiceData = object.toString();
			parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
			return true;
		} else if (intent.equals("light_up_max")) {
			JSONObject object = getJson("system", "light", parseData.strText);
			object.put("choice", "max");
			parseData.strVoiceData = object.toString();
			parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
			return true;

		} else if (intent.equals("light_down_min")) {
			JSONObject object = getJson("system", "light", parseData.strText);
			object.put("choice", "min");
			parseData.strVoiceData = object.toString();
			parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
			return true;

		} else if (intent.equals("quit")) {
			JSONObject object = getJson("txz", "close", parseData.strText);
			parseData.strVoiceData = object.toString();
			parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
			return true;
		}
		return false;
	}

	private boolean parsePlayer(VoiceParseData parseData, JSONObject root,
			String intent) {
		if (intent.equals("set")) {
			if (root.containsKey("action_type")) {
				String type = root.getString("action_type");
				JSONObject object = null;
				if (type.equals("exitplayer")) {
					object = getJson("music", "exit", parseData.strText);
				} else if (type.equals("play")) {
					object = getJson("music", "play", parseData.strText);
				} else if (type.equals("pause")) {
					object = getJson("music", "pause", parseData.strText);
				} else if (type.equals("previous")) {
					object = getJson("music", "prev", parseData.strText);
				} else if (type.equals("next")) {
					object = getJson("music", "next", parseData.strText);
				} else
					return false;
				parseData.strVoiceData = object.toString();
				parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
				return true;
			} else if (root.containsKey("mode")) {
				String mode = root.getString("mode");
				JSONObject object = null;
				if (mode.equals("single")) {
					object = getJson("music", "switchModeLoopOne",
							parseData.strText);
				} else if (mode.equals("full_loop")) {
					object = getJson("music", "switchModeLoopAll",
							parseData.strText);
				} else if (mode.equals("random")) {
					object = getJson("music", "switchModeRandom",
							parseData.strText);
				} else if (mode.equals("single_loop")) {
					object = getJson("music", "switchModeLoopOne",
							parseData.strText);
				} else if (mode.equals("order")) {
					object = getJson("music", "switchModeLoopAll",
							parseData.strText);
				} else
					return false;
				parseData.strVoiceData = object.toString();
				parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
				return true;
			}
		}
		return false;
	}

	private boolean parseNav(VoiceParseData parseData, JSONObject root,
			String intent) {
		if (intent.equals("route_home")) {
			JSONObject object = getJson("nav", "home", parseData.strText);
			parseData.strVoiceData = object.toString();
			parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
			return true;
		}else if (intent.equals("route_work")) {
			JSONObject object = getJson("nav", "company", parseData.strText);
			parseData.strVoiceData = object.toString();
			parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
			return true;
		}else if (intent.equals("location")) {
			JSONObject object = getJson("location", "query", parseData.strText);
			parseData.strVoiceData = object.toString();
			parseData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
			return true;
		}
		return false;
	}

	private void parseUnknown(VoiceParseData parseData) {
		parseData.strVoiceData = getJson("unknown", "unknown",
				parseData.strText).toString();
		parseData.floatTextScore = TextResultHandle.TEXT_SCORE_MIN;
		return;
	}
}

package com.txznet.txz.component.asr.mix.local;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.txz.ui.data.UiData;
import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.util.ExchangeHelper;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class UVoiceLocalJsonConver {
	private String mRawText;
	private String keywords;
	private String mJson;
	private float mScore;
	private int mSentenceScore;
	private String mScene;
	private int mTextMask;
	private final Pattern mDigitPattern, mFmPattern;
	private String TAG = "UVoiceLocalJsonConver";
	private final static float SCORE_ACCURACY = 0.1f;
	public final static float CONFIG_HIGH = -3.0f + SCORE_ACCURACY;
	public final static float CONFIG_MID = -6.0f + SCORE_ACCURACY;
	public final static float CONFIG_MID_LOW = -7.0f + SCORE_ACCURACY;
	public final static float CONFIG_LOW = -8.0f - SCORE_ACCURACY;
	public static int CONF_HIGH_SCORE = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_ASR_UVOICE_SEMANTICS_THRESHOLD, 9000); // 大于 9000 认为可信，分数设为 CONFIG_HIGH
	public final static int CONF_MID_SCORE = 7000; // 大于 7000 分数设为 CONFIG_MID
	public final static int CONF_LOW_SCORE = 3000; // 大于 3000 分数设为 CONFIG_LOW， 小于3000 返回unknown 场景
	public final static float SCORE_HIGH = TextResultHandle.TEXT_SCORE_CONFIDENCE;
	public final static float SCORE_MID = 70;
	public final static float SCORE_LOW = 50;
	public final static float SCORE_NONE = TextResultHandle.TEXT_SCORE_MIN - 1;



	public UVoiceLocalJsonConver(VoiceParseData parseData) {
		mRawText = "";
		mJson = "";
		mScore = 0f;
		mSentenceScore = 0;
		mScene = "";
		LogUtil.d(TAG, "UVoiceLocalJsonConver: CONF_HIGH_SCORE = " + CONF_HIGH_SCORE);
		mDigitPattern = Pattern.compile("[0-9]*");
		mFmPattern = Pattern.compile("\\d{2,3}(点\\d)?"); //108点5,87点5,101
		mJson = jsonConvert(parseData);
	}

	private class Slot{
		String _name;
		String _orthography;
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

	public int getTextMask() {
		return mTextMask;
	}

	@Override
	public String toString() {
		return "UVoiceLocalJsonConver{" +
				"mRawText='" + mRawText + '\'' +
				", keywords='" + keywords + '\'' +
				", mJson='" + mJson + '\'' +
				", mScore=" + mScore +
				", mScene='" + mScene + '\'' +
				", TAG='" + TAG + '\'' +
				'}';
	}

	private String jsonConvert(VoiceParseData parseData) {
		LogUtil.d(TAG, "jsonConvert");
		JSONObject newJson = new JSONObject();
		if (parseData == null) {
			LogUtil.e(TAG, "parseData is null");
			return newJson.toString();
		}
		String strVoiceData = parseData.strVoiceData;
		newJson.put("local", true);
		newJson.put("scene", "unknown");
		newJson.put("action", "unknown");
		mScore = TextResultHandle.TEXT_SCORE_MIN;
		newJson.put("score", mScore);
		if (TextUtils.isEmpty(strVoiceData)) {
			LogUtil.e(TAG, "strVoiceData is empty");
			return newJson.toString();
		}
		JSONObject json = new JSONObject();
		try {
			json = JSONObject.parseObject(strVoiceData);
		} catch (Exception e) {
			mRawText = "";
			JNIHelper.logd(TAG + "::JsonException : "
					+ strVoiceData);
			return newJson.toString();
		}
		JSONArray hypotheses;
		if (isValid(json, "_hypotheses")) {
			hypotheses = json.getJSONArray("_hypotheses");
		} else {
			JNIHelper.logd(TAG + "::Wrong json type");
			return newJson.toString();
		}
		if (null==hypotheses||hypotheses.isEmpty()) {
			JNIHelper.loge(TAG + "::the json result is empty");
			return newJson.toString();
		}

		//先使用UVoicePOI，判断是否是导航场景，如果是，就用UVoicePOI
		//UVoicePOI是在第一个
		JSONObject uvoicePOI = hypotheses.getJSONObject(0);
		String name = uvoicePOI.getString("_decoder");
		LogUtil.d(TAG, "name:"+name);
		if (LocalAsrUVoiceImpl.POI_NAME.equals(uvoicePOI.getString("_decoder"))) {
			String result = parseUVoicePOI(uvoicePOI);
			Log.d(TAG, "result=" + result);
			String[] words = {"导航到", "导航去", "导航回", "我要去", "我要到", "我要回", "我想去", "我想到",  "我想回", "怎么到", "怎么去", "怎么回", "带我去", "带我到", "带我回", "附近的"};
			boolean useUVoice = false;
			int wordIndex = 0;
			for (String word : words) {
				if (result.contains(word)) {
					useUVoice = true;
					int i = result.lastIndexOf(word) + word.length();
					wordIndex = wordIndex > i ? wordIndex : i;
				}
			}
			if (useUVoice) { // 导航场景
				LogUtil.d(TAG, "use poi");
				mSentenceScore = uvoicePOI.getIntValue("_conf");
				int score = getMinWordScore(uvoicePOI);
				if (score >= CONF_HIGH_SCORE){
					mScore = SCORE_HIGH;
					newJson.put("score", CONFIG_HIGH);
				} else if (score >= CONF_MID_SCORE){
					mScore = SCORE_MID;
					newJson.put("score", CONFIG_MID);
				} else if (score >= CONF_LOW_SCORE){
					mScore = SCORE_LOW;
					newJson.put("score", CONFIG_MID_LOW);
				} else {
					mScore = SCORE_NONE;
					newJson.put("score", CONFIG_LOW);
				}
				mScene = "nav";
				newJson.put("scene", mScene);
				mRawText = result;
				newJson.put("text", mRawText);
				if (result.length() <= wordIndex){
					LogUtil.d("generate unknown scene and return");
					genUnknownScene(newJson);
					return newJson.toString();
				}
				result = result.substring(wordIndex);
				keywords = result;
				if ("公司".equals(keywords)|"单位".equals(keywords)){
					keywords = "company";
					newJson.put("action", "company");
					newJson.put("keywords", keywords);
				} else if ("家".equals(keywords)||"家里".equals(keywords)) {
					keywords = "home";
					newJson.put("action", "home");
					newJson.put("keywords", keywords);
				} else if ("加".equals(keywords)) {
					keywords = "home";
					newJson.put("action", "home");
					newJson.put("keywords", keywords);
					newJson.put("text", mRawText.replace("加", "家"));
				} else {
					newJson.put("action", "search");
					newJson.put("keywords", keywords);
				}
				LogUtil.d(TAG, "newJson:" + newJson.toString());
				return newJson.toString();
			}
		}

		// 获取最高分数模型的index
		int index = getMaxScoreIndex(hypotheses);
		LogUtil.d(TAG, "index="+index);
		if (index == -1){
			LogUtil.d(TAG, "generate unknown scene and return");
			genUnknownScene(newJson);
			return newJson.toString();
		}
		JSONObject decoder = hypotheses.getJSONObject(index);
		int score = getMinWordScore(decoder); //获取该模型里对每次的打分中最低的分数
        LogUtil.d(TAG, "min score = " + score);
		if (score >= CONF_HIGH_SCORE){
			mScore = SCORE_HIGH;
			newJson.put("score", CONFIG_HIGH);
		} else if (score >= CONF_MID_SCORE){
			mScore = SCORE_MID;
			newJson.put("score", CONFIG_MID);
		} else if (score >= CONF_LOW_SCORE){
			mScore = SCORE_LOW;
			newJson.put("score", CONFIG_MID_LOW);
		} else {
			mScore = SCORE_NONE;
			newJson.put("score", CONFIG_LOW);
		}

		String decoderName = "";
		JSONArray items = new JSONArray();
		if (isValid(decoder, "_decoder")) { // 获取最高分数的模型
			decoderName = decoder.getString("_decoder");
		}
		if (isValid(decoder, "_items")) {
			items = decoder.getJSONArray("_items");
		}
		if (items == null){
			LogUtil.d(TAG, "newJson:" + newJson.toString());
			return newJson.toString();
		}

        // 获取mRawText
		for (int i = 0; i < items.size(); i++) {
			JSONObject item = items.getJSONObject(i);
			LogUtil.d(TAG, "item="+item.toString());
			if (!item.containsKey("_items")) {
				String word = item.getString("_orthography");
				mRawText += word;
			} else {
				JSONArray _items = item.getJSONArray("_items");
				if (_items == null) {
					continue;
				}
				LogUtil.d(TAG, _items.toString());
				String word = _items.getJSONObject(0).getString("_orthography");
				String _name = item.getString("_name");
				LogUtil.d(TAG, "_orthography:"+word+"  _name:"+_name);
				if (_name!=null){
					_name=_name.replaceFirst("#","");
					if (LocalAsrUVoiceImpl.mSlotMap.get(_name)!=null){
						if (LocalAsrUVoiceImpl.mSlotMap.get(_name).get(word)!=null){
							word=LocalAsrUVoiceImpl.mSlotMap.get(_name).get(word);
						}else {
							LogUtil.d(TAG, "LocalAsrUVoiceImpl.mSlotMap.get("+_name+").get("+word+") == null");
						}
					}else {
						LogUtil.e(TAG, "LocalAsrUVoiceImpl.mSlotMap.get("+_name+") == null");
					}
				}
//				word= ExchangeHelper.chineseToNumber(word) + "";
				mRawText += word;
			}
		}
		LogUtil.d(TAG, "mRawText = " + mRawText);
		mRawText = mRawText.replace("<s>", "");
		mRawText = mRawText.replace("</s>", "");

		if (parseData.uint32Sence == VoiceData.GRAMMAR_SENCE_SET_COMPANY) {
			newJson.put("scene", "nav");
			newJson.put("action", "modifyCompany");
			newJson.put("keywords", mRawText);
			LogUtil.d(TAG, "newJson:" + newJson.toString());
			return newJson.toString();
		} else if (parseData.uint32Sence == VoiceData.GRAMMAR_SENCE_SET_HOME) {
			newJson.put("scene", "nav");
			newJson.put("action", "modifyHome");
			newJson.put("keywords", mRawText);
			LogUtil.d(TAG, "newJson:" + newJson.toString());
			return newJson.toString();
		}
		if (mScore == SCORE_NONE) { // 如果分数低，就令mRawText为空
			mRawText = "";
		}

		String[] slots = getSlots(items);
		LogUtil.d(TAG, "slots are: ");
		for (String string : slots) {
			LogUtil.d(TAG, string);
		}
		// 判断场景
		mScene = parseScene(slots);
		LogUtil.d(TAG, "mScene is " + mScene);
		newJson.put("scene", mScene);
//		newJson.put("text", mRawText);
		if ("txz".equals(decoderName)) { // txz语言模型指定的语法
			LogUtil.d(TAG, "use txz bnf");
			// 导航相关
			if ("nav".equals(mScene)) {
				LogUtil.d(TAG, "poi scene");

				if (hasSlot(slots, "navHstFind")){
					newJson.put("action", "openHst");
				}

				else{
					newJson.put("action", "search");
					newJson.put("keywords", "unknown");

					if (hasSlot(slots, "navHome")) { // 回家
						newJson.put("action", "home");
						newJson.put("keywords", "home");
					} else if (hasSlot(slots, "navCompany")) { // 公司
						newJson.put("action", "company");
						newJson.put("keywords", "company");
					}
					String[] keywords = { "navPOI", "navNearAct", "navNearKw" };
					for (String keyword : keywords) {
						if (hasSlot(slots, keyword)) {
							newJson.put("keywords", getValue(items, keyword));
							break;
						}
					}

					if (newJson.getString("keywords").equals("unknown")) {
						genUnknownScene(newJson);
						LogUtil.d(TAG, "generate unknown scene and return");
						return newJson.toString();
					}
				}
			}

			// 音乐相关
			if ("music".equals(mScene)) {
				LogUtil.d("music scene");
				mTextMask = TextResultHandle.MODULE_LOCAL_NORMAL_MASK | TextResultHandle.MODULE_LOCAL_HIGH_MASK;
				if (hasSlot(slots, "askMscNow")){
					newJson.put("action", "ask");
				}
				else{
					newJson.put("action", "play");
					JSONObject model = new JSONObject();
					if (hasSlot(slots, "mscList")) {
						model.put("title", getValue(items, "mscList"));
					}
					if (hasSlot(slots, "mscAlbum")) {
						model.put("album", getValue(items, "mscAlbum"));
					}
					if (hasSlot(slots, "mscSinger")) {
						String artist = getValue(items, "mscSinger");
//						两种方式
//						JSONArray artists = new JSONArray();
//						artists.add(artist);
						model.put("artist", new String[]{artist});
					}
					if (hasSlot(slots, "mscType")) {
						model.put("keywords", getValue(items, "mscType"));
					}
					newJson.put("model", model);
				}
			}

			// 电话
			if ("call".equals(mScene)) {
				LogUtil.d(TAG, "电话相关");

				// type
				if (hasSlot(slots, "callPhoneZ")) {
					newJson.put("type", getValue(items, "callPhoneZ"));
					newJson.put("p", "z");
				} else if (hasSlot(slots, "callPhoneM")) {
					newJson.put("type", getValue(items, "callPhoneM"));
					newJson.put("p", "m");
				} else if (hasSlot(slots, "callPhoneS")) {
					newJson.put("type", getValue(items, "callPhoneS"));
					newJson.put("p", "s");
				}

				String strPrefix = "";
				String strSuffix = "";
				if (hasSlot(slots, "callPrefix")) {
					strPrefix = getValue(items, "callPrefix");
				}
				if (hasSlot(slots, "callSuffix")) {
					strSuffix = getValue(items, "callSuffix");
				}

				// 联系人
				String strContact = null;
				String[] slotArray = { "callEnglish", "callCon1",
						"callCon2", "callUsual", "callUnusual",
						"callService"};
				for (String slot : slotArray) {
					if (hasSlot(slots, slot)) {
						LogUtil.d(TAG, getValue(items, slot));
						strContact = getValue(items, slot);
						break;
					}
				}
				String strNumber = null;
				if (hasSlot(slots, "callNumber")) {
					LogUtil.d(TAG, getValue(items, "callNumber"));
					strNumber = getValue(items, "callNumber");
				}

				newJson.put("scene", "call");
				newJson.put("action", "make");
				newJson.put("mold", TextResultHandle.CALL_LOCAL_YZS_CH);

				if (!TextUtils.isEmpty(strContact)) {
					newJson.put("name", strContact);
					newJson.put("prefix", strPrefix);
					newJson.put("suffix", strSuffix);
				} else if (!TextUtils.isEmpty(strNumber)) {
					newJson.put("number",strNumber);
				}

			}

			// app
			if ("app".equals(mScene)) {
				LogUtil.d(TAG, "app scene");
				newJson.put("scene", "app");
				if (hasSlot(slots, "cmdAppNames")) {
					newJson.put("name", getValue(items, "cmdAppNames"));
				}
				if (hasSlot(slots, "cmdAppOpen")) {
					newJson.put("action", "open");
				} else if (hasSlot(slots, "cmdAppClose")) {
					newJson.put("action", "close");
				}
			}

			// 微信
			if ("wechat".equals(mScene)){
				LogUtil.d(TAG, "wechat scene");
				newJson.put("scene", "wechat");
				if (hasSlot(slots, "WeChatFriend")) {
					newJson.put("keywords", getValue(items, "WeChatFriend"));
				}
				if (hasSlot(slots, "WeChatGroup")) {
					newJson.put("keywords", getValue(items, "WeChatGroup"));
				}
				
				newJson.put("action", "send");
			}

			// 空调
			if ("airC".equals(mScene)) {
				mScene = "command";
				newJson.put("scene", "command");
				newJson.put("action", "event");
				newJson.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW);
				newJson.put("subEvent", 0);
				newJson.put("type", UiData.DATA_ID_TEST_LOCAL_KEYWORD);

				String type = "";
				if (hasSlot(slots, "tempValue") || hasSlot(slots, "tempRateValue")){
					type = "temperature";
				} else if (hasSlot(slots, "gearValue")){
					type = "windspeed";
				}

				LogUtil.d("ac type = "+type);
				if ("temperature".equals(type)) {
					LogUtil.d("temperature scene");
					newJson.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW);
					String[] slotList = { "acCtrlTo", "acCtrlUp", "acCtrlDown" };
					String[] operations = { "to", "up", "down" };
					String operation = parseSlots(slots, slotList, operations);


					if ("to".equals(operation)) { // 设置温度
						LogUtil.d("temperature to");
						String value = getValue(items, "tempValue");
						JSONObject jsonData = new JSONObject();
						newJson.put("cmd", "AC_CMD_TEMPERATURE_CTRLTO");
						LogUtil.d("value = "+value);
						jsonData.put("tempValue", value);
						newJson.put("voiceData", jsonData);
					} else if ("up".equals(operation)) { // 提高温度
						newJson.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW);
						newJson.put("subEvent", 0);
						newJson.put("type", UiData.DATA_ID_TEST_LOCAL_KEYWORD);
						String value = getValue(items, "tempRateValue");
						JSONObject jsonData = new JSONObject();
						newJson.put("cmd", "AC_CMD_TEMPERATURE_CTRLUP");
						LogUtil.d("value = "+value);
						jsonData.put("tempRateValue", value);
						newJson.put("voiceData", jsonData);
					} else if ("down".equals(operation)) { // 降低温度
						newJson.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW);
						newJson.put("subEvent", 0);
						newJson.put("type", UiData.DATA_ID_TEST_LOCAL_KEYWORD);
						String value = getValue(items, "tempRateValue");
						JSONObject jsonData = new JSONObject();
						newJson.put("cmd", "AC_CMD_TEMPERATURE_CTRLDOWN");
						LogUtil.d("value = "+value);
						jsonData.put("tempRateValue", value);
						newJson.put("voiceData", jsonData);
					}
				} else if ("windspeed".equals(type)) {
					newJson.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND);
					String value = getValue(items, "gearValue");

					// 目前只有设置到XXX的操作
					if (TextUtils.isDigitsOnly(value)) { // 设置为XXX
						newJson.put("event",
								UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW);
						JSONObject jsonData = new JSONObject();
						newJson.put("event", UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW);
						newJson.put("subEvent", 0);
						newJson.put("type", UiData.DATA_ID_TEST_LOCAL_KEYWORD);
						newJson.put("cmd", "AC_CMD_WIND_SPEED_CTRLTO");
						LogUtil.d("value = "+value);
						jsonData.put("gearValue", value);
						newJson.put("voiceData", jsonData);
					}
				} else{
					genUnknownScene(newJson);
				}
			}

			// command
			if ("command".equals(mScene)){
				//命令字分为10个槽位
				String cmdSlotName = getFinalSlotName(slots, "cmdKeywords", 10);
				if (!TextUtils.isEmpty(cmdSlotName)){
					newJson.put("scene", "command");
					newJson.put("action", "exec");
					String cmd = getValue(items, cmdSlotName);
//					mRawText = cmd;
					newJson.put("cmd", cmd);
//					newJson.put("text", cmd);
				}
				if (hasSlot(slots, "cmuKeywords")){
					newJson.put("scene", "command");
					newJson.put("action", "exeu");
					String cmu = getValue(items, "cmuKeywords");
//					mRawText = cmu;
					newJson.put("cmd", cmu);
//					newJson.put("text", cmu);
				}
				if (hasSlot(slots, "ruleKeywords")){
					mTextMask = TextResultHandle.MODULE_LOCAL_NORMAL_MASK | TextResultHandle.MODULE_LOCAL_HIGH_MASK;
					newJson.put("scene", "command");
					newJson.put("action", "exeu");
					String rule = getValue(items, "ruleKeywords");
//					mRawText = rule;
					newJson.put("cmd", rule);
//					newJson.put("text", rule);
				}
			}

			//流量
			if ("flow".equals(mScene)){
				newJson.put("action", "setting");
				if (hasSlot(slots, "flow")){
					newJson.put("flowValue", getValue(items, "flow"));
				}
			}

			// 音量
			if ("system".equals(mScene)){
				String[] slotList = { "volumeTo", "volumeUp", "volumeDown" };
				String[] choices = { "set", "up", "down" };
				String choice = parseSlots(slots, slotList, choices);
				newJson.put("choice",choice);
				newJson.put("action","volume");
				if ("set".equals(choice)){
					newJson.put("value", getValue(items, "volumeValue"));
				} else if ("up".equals(choice) || "down".equals(choice)){
					newJson.put("value", getValue(items, "volumeRateValue"));
				}
			}

			//调频
			if ("fm".equals(mScene)){
				mScene = "radio";
				newJson.put("scene", "radio");
				newJson.put("action", "play");
				newJson.put("waveband", "fm");
				newJson.put("unit", "MHZ");
				String hz = getValue(items, "fmFreqValue");
                try {
                	if (mFmPattern.matcher(hz).matches()) { //数字，87点5
						hz = hz.replace("点", ".");
					} else { //中文字：八十七点五
						String[] nums = hz.split("点");
						int num = ExchangeHelper.chineseToNumber(nums[0]);
						hz = "" + num;
						if (nums.length == 2) {
							hz += "." + nums[1];
						}
					}
					newJson.put("hz", hz);
                } catch (Exception e){
                	e.printStackTrace();
                }
			}

			//调幅
			if ("am".equals(mScene)){
				mScene = "app";
				newJson.put("scene", "radio");
				newJson.put("action", "play");
				newJson.put("waveband", "am");
				newJson.put("unit", "KHZ");
				String hz = getValue(items, "amValue");
				try {
                    if (!TextUtils.isEmpty(hz)) {
                    	if (mDigitPattern.matcher(hz).matches()) { //全是数字，999
                    		hz=hz.replace("点",".");
							newJson.put("hz", hz);
						} else { //九百九十九
							String value = ExchangeHelper.chineseToNumber(hz) + "";
							value=value.replace("点",".");
							newJson.put("hz", value);
						}
                    }
                } catch (Exception e){

                }
			}
		}

		LogUtil.d(TAG, "newJson.toString():" + newJson.toString());
		return newJson.toString();
	}

	private String parseUVoicePOI(JSONObject json){
		try {
			if (!json.containsKey("_items")) {
				return "";
			}
			JSONArray items = json.getJSONArray("_items");
			if (items == null){
				return "";
			}
			StringBuilder sb = new StringBuilder("");
			for (int i = 0; i < items.size(); i++) {
				JSONObject item = items.getJSONObject(i);
				if (item.containsKey("_orthography")) {
					String word = item.getString("_orthography");
					JSONArray _phone = item.getJSONArray("_phone");
					if (_phone == null){
						continue;
					}
					String type = _phone.getString(0);
					if (null == type || type.equals("SIL")){
						continue;
					}
					sb.append(word);
				}
			}
			return sb.toString();
		} catch(Exception e){
			LogUtil.logw(TAG+e.toString());
			return "";
		}
	}

	/**
	 * 判断是否有slotList里面的slot，有的话返回相应的的string，没有返回unknown
	 *
	 * @param slots
	 *            slot列表
	 * @param slotList
	 *            需要判断是否存在的slot列表
	 * @param strings
	 *            slot列表对应的返回值
	 * @return
	 */
	private String parseSlots(String[] slots, String[] slotList,
							  String[] strings) {
		for (String slot : slots) {
			for (int i = 0; i < slotList.length; i++) {
				if (i > strings.length) {
					return "unknown";
				}
				if (hasString(slot, slotList[i])) {
					return strings[i];
				}
			}
		}
		return "unknown";
	}

	/**
	 * 使用getValue(items, "slot名字")来获取对应的value
	 *
	 * @param items
	 * @param string
	 * @return
	 */
	private String getValue(JSONArray items, String string) {
		try {
			for (int i = 0; i < items.size(); i++) {
				JSONObject item = items.getJSONObject(i);
				if (!item.containsKey("_items")) {
					continue;
				}
				if (!item.containsKey("_name")) {
					continue;
				}
				if (!(item.getString("_name")).equals("#"+string)){
					continue;
				}
				String value = item.getJSONArray("_items").getJSONObject(0).getString("_orthography");
				LogUtil.d(TAG, "getValue: value = " + value + ", string = " + string);
				HashMap<String, String> slotMap = LocalAsrUVoiceImpl.mSlotMap.get(string);
				if (slotMap != null) {
					LogUtil.d(TAG, "getValue: not null");
					String oldSlot = slotMap.get(value);
					LogUtil.d(TAG, "getValue: old = " + oldSlot);
					if (TextUtils.isEmpty(oldSlot))
						return value;
					else
						return oldSlot;
				}
				return value;
			}
		} catch (Exception e){
			LogUtil.e("get value failed");
			e.printStackTrace();
			return "";
		}
		return "";
	}

	/**
	 * 判断slot列表里是否存在string
	 * @param slots
	 * @param string
	 * @return
	 */
	private boolean hasSlot(String[] slots, String string) {
		return getSlotIndex(slots, string) != -1;
	}

	/**
	 * 判断slot列表里是否存在string，用于槽位分开的词，比如命令字
	 * @param slots
	 * @param string
	 * @param num 一共分成多少个槽位
	 * @return
	 */
	private String getFinalSlotName(String[] slots, String string, int num) {
		for(int i=1; i<=num; i++) {
			int slotIndex = getSlotIndex(slots, string + i);
			if (slotIndex != -1)
				return slots[slotIndex];
		}
		return "";
	}

	/**
	 * 获取string在slot列表里的索引位置，不存在返回-1
	 * @param slots
	 * @param string
	 * @return
	 */
	private int getSlotIndex(String[] slots, String string) {
		for (int i = 0; i < slots.length; i++) {
			if (slots[i].equals(string)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 返回slot的列表
	 * @param items
	 * @return
	 */
	private String[] getSlots(JSONArray items) {
		ArrayList<String> results = new ArrayList<String>();
		for (int i = 0; i < items.size(); i++) {
			if (isValid(items.getJSONObject(i), "_name")){

			}
			String res = items.getJSONObject(i).getString("_name");
			if (TextUtils.isEmpty(res)) {
				continue;
			}
			results.add(res.substring(1));
		}
		return results.toArray(new String[0]);
	}

	/**
	 * 判断json对象中某个key的值是否可用（含有这个key，且不为空）
	 *
	 * @param name
	 * @param key
	 * @return
	 */
	private boolean isValid(JSONObject name, String key) {
		if (null == name){
			return false;
		}
		return name.containsKey(key) && !TextUtils.isEmpty(name.getString(key));
	}

	/**
	 * 生成unknown场景
	 * @param newJson
	 */
	private void genUnknownScene(JSONObject newJson) {
		mScore = SCORE_NONE;
		newJson.put("scene", "unknown");
		newJson.put("action", "unknown");
	}

	/**
	 * 获取最大分数的索引
	 * @param jArray
	 * @return
	 */
	private int getMaxScoreIndex(JSONArray jArray) {
		int index = -1;
		if (null==jArray){
			return index;
		}
		int score = 0;
		for (int i = 0; i < jArray.size(); i++) {
			if (LocalAsrUVoiceImpl.POI_NAME.equals(jArray.getJSONObject(i).getString("_decoder"))){
				continue;
			}
			int num = jArray.getJSONObject(i).getIntValue("_conf");
			if (num > score) {
				score = num;
				index = i;
			}
		}
		mSentenceScore = score;
		return index;
	}

	/**
	 * 解析场景
	 * @param names
	 * @return
	 */
	private String parseScene(String[] names) {
		String[] substrs = { "nav", "call", "incm", "msc", "cmdApp", "nws",
				"ac", "weChat", "cncl", "sel", "incm", "sms",
				"cmdKeywords", "cmuKeywords", "ruleKeywords", "flow", "volume", "ask", "fm", "am"};
		String[] scenes = { "nav", "call", "call", "music", "app", "audio",
				"airC", "wechat", "local", "local", "local", "local",
				"command", "command", "command", "flow", "system", "music", "fm", "am"};
		return parseSlots(names, substrs, scenes);
	}

	/**
	 * 判断string是否包含substring开头
	 * @param string
	 * @param substring
	 * @return
	 */
	private boolean hasString(String string, String substring) {
		if (string.length() < substring.length()) {
			return false;
		}
		string = string.toLowerCase();
		substring = substring.toLowerCase();
		if (!string.substring(0, substring.length()).equals(substring)) {
//		if (!string.contains(substring.toLowerCase())) {
			return false;
		}
		return true;
	}

	/**
	 * 获取该模型里所有词中，最低的分数
	 * @param decoder
	 * @return
	 */
	private int getMinWordScore(JSONObject decoder){
		int score = 10000;
		int negativeScore = -9999;
		if (null == decoder){
			return negativeScore;
		}
		JSONArray items = decoder.getJSONArray("_items");
		if (null == items){
			return negativeScore;
		}
		if (items.size() < 3){
			return negativeScore;
		}
		JSONObject lastWord = items.getJSONObject(items.size() - 1);
		if (!lastWord.containsKey("_orthography")){
			return negativeScore;
		}
		String sentenceEndSymbol = lastWord.getString("_orthography");
		if (!"</s>".equals(sentenceEndSymbol)){
			return negativeScore;
		}
		for(int i = 0; i < items.size(); i++){
			JSONObject item = items.getJSONObject(i);
			if (!item.containsKey("_items")){
				continue;
			}
			JSONArray item2 = item.getJSONArray("_items");
			if (null == item2){
				continue;
			}
			if (item2.size() < 1){
				continue;
			}

			JSONObject item3 = item2.getJSONObject(0);
			if (!item3.containsKey("_conf")){
				continue;
			}
			int conf = item3.getInteger("_conf");
			if (conf < score){
				score = conf;
			}
		}
		return score;
	}
}

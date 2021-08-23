package com.txznet.txz.util;

import org.json.JSONArray;
import org.json.JSONObject;
import com.txznet.txz.jni.JNIHelper;

import android.text.TextUtils;

public class IflytekHelper {
	public static JSONObject UserWord2JsonItem(String strName, String[] words) {
		JSONObject item = null;
		try {
			item = new JSONObject();
			item.put("name", strName);
			JSONArray wordsArray = new JSONArray();
			for (String word : words) {
				if (!TextUtils.isEmpty(word)) {
					wordsArray.put(word);
				}
			}
			item.put("words", wordsArray);
		} catch (Exception e) {
			JNIHelper.loge("userword2jsonitem : " + e.toString());
		}
		return item;
	}
	
	public static JSONObject userword(JSONObject oItem){
		JSONObject userword = null;
		try {
			userword = new JSONObject();
			JSONArray wordsArray = new JSONArray();
			wordsArray.put(oItem);
			userword.put("userword", wordsArray);
		} catch (Exception e) {
			JNIHelper.loge("userword2jsonitem : " + e.toString());
		}
		return userword;
	}
	
	public static void updateUserWordMap(JSONObject oMap, JSONObject oItem){
		try {
			JSONArray array = null;
			if (!oMap.has("userword")){
				array = new JSONArray();
				oMap.put("userword", array);
			}
			array = oMap.getJSONArray("userword");
			
			for (int i = 0; i < array.length(); i++){
				JSONObject o = array.getJSONObject(i);
				if (o.getString("name").equals(oItem.getString("name"))){
					array.put(i, oItem);
					return;
				}
			}
			array.put(oItem);
		} catch (Exception e) {
		}
		
	}
}

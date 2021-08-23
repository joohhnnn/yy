package com.txznet.txz.module.nav;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

public class ConfigFileHelper {
	public static String DEEP_SEARCH = "deepSearch";
	public static String ONWAY_SEARCH = "onWayKey";
	
	public static String NAV_IMP_BAIDU = "baidu";
	public static String NAV_IMP_GAODE = "auto";
	
	public static int POI_SEARCH_CODE_BAIDU = 1;
	public static int POI_SEARCH_CODE_GAODE = 2;	
	
	public static ConfigFileHelper mInstance = null;
	JSONArray mJsonArray = null;

	private ConfigFileHelper(Context context) {
		try {
			InputStreamReader isr = new InputStreamReader(context.getAssets()
					.open("navConfig.json"), "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String line;
			StringBuilder builder = new StringBuilder();
			while ((line = br.readLine()) != null) {
				builder.append(line);
			}
			br.close();
			br = null;
			isr.close();
			isr = null;
			mJsonArray = new JSONArray(builder.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ConfigFileHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ConfigFileHelper(context);
		}
		return mInstance;
	}

	public int ConfigValue(String type, String navImp, String keyWord,int version, int defalueValue) {
		JSONObject object = getKeyWordJson(type, navImp, keyWord, version);
		if (object != null && object.has("value")) {
			try {
				return object.getInt("value");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return defalueValue;
	}

	public String ConfigValue(String type, String navImp, String keyWord,
			int version, String defalueValue) {
		JSONObject object = getKeyWordJson(type, navImp, keyWord, version);
		if (object != null && object.has("value")) {
			try {
				return object.getString("value");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return defalueValue;
	}

	public boolean ConfigValue(String type, String navImp, String keyWord,
			int version) {
		JSONObject object = getKeyWordJson(type, navImp, keyWord, version);
		if (object != null) {
			return true;
		}
		return false;
	}

	private JSONObject getKeyWordJson(String type, String navImp,
			String keyWord, int version) {
		if (mJsonArray != null) {
			for (int i = 0; i < mJsonArray.length(); i++) {
				try {
					if (!mJsonArray.isNull(i)) {
						JSONObject object = (JSONObject) mJsonArray.get(i);
						if (object.has("type")&& type.equals(object.getString("type"))) {
							
							JSONArray jsonArray = object.getJSONArray("data");							
							if (jsonArray != null) {
								for (int ii = 0; ii < jsonArray.length(); ii++) {
									if (!jsonArray.isNull(ii)) {
										JSONObject jsonObject = (JSONObject) jsonArray.get(ii);
										
										if (jsonObject.has("nav")
												&& navImp.equals(jsonObject.getString("nav"))
												&& jsonObject.getInt("version") <= version
												&& !TextUtils.isEmpty(jsonObject.getString("keyword"))) {
											
											String[] keyWordList = jsonObject.getString("keyword").split(",");
											
											for (String str : keyWordList) {
												if (!TextUtils.isEmpty(str)) {
													if (keyWord.contains(str)) {
														return jsonObject;
													}
												}
											}
											
										}
									}
								}
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}

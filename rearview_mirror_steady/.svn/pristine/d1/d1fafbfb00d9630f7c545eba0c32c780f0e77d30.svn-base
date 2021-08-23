package com.txznet.txz.module.userconf;

import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;

public class ConfigData{
	public Float mWakeupThreshholdVal = null;
	public String[] mWakeupWords = null;
	public Boolean mWakeupEnable = null;
	public String mDeviceWelcomeMsg = null;
	public Integer mPoiMapMode = null;
	public Boolean mEnableMapMode = null;
	/** 是否使用腾讯引擎功能， 此状态只可读，不可修改*/
	public Boolean mIsUseTencent = null;
	/** 语音升级信息，不可修改*/
	public String mUpgradeInfo = null;

	//是否显示打字效果的开关
	public Boolean mEnableTypingEffectItem = null;
	//控制打字效果
	public Boolean mUseTypingEffect = true;

	public String mThemeStyle = null;

	public String mSelectThemeStyle = null;

	/**
	 * @param strKey
	 * @param json
	 * @throws Exception
	 */
	private void parseJsonKey(String strKey, JSONObject json)  throws Exception{
		if (TextUtils.isEmpty(strKey)){
			LogUtil.logw("strKey is empty");
			return;
		}
		do {
			if (strKey.startsWith("wakeup.")) {
				if ("wakeup.threshhold.value".equals(strKey)) {
					mWakeupThreshholdVal = (float) json.getDouble(strKey);
				} else if ("wakeup.words".equals(strKey)) {
					JSONArray array = json.getJSONArray(strKey);
					mWakeupWords = new String[array.length()];
					for (int i = 0; i < array.length(); ++i){
						mWakeupWords[i] = array.getString(i);
					}
				} else if ("wakeup.enable".equals(strKey)) {
					mWakeupEnable = json.getBoolean(strKey);
				}
				break;
			}
			
			if (strKey.startsWith("device.")){
				if ("device.welcome.msg".equals(strKey)){
					mDeviceWelcomeMsg = json.getString(strKey);
				}
			}
			
			if(strKey.startsWith("poi.")){
				if("poi.map.mode".equals(strKey)){
					mPoiMapMode = json.getInt(strKey);
				}
			}

			if (strKey.startsWith("typing.effect")){
				if ("typing.effect.enable.item".equals(strKey)) {
					mEnableTypingEffectItem = json.getBoolean(strKey);
				} else if ("typing.effect.state".equals(strKey)) {
					mUseTypingEffect = json.getBoolean(strKey);
				}

				break;
			}

			if (strKey.startsWith("theme.style")) {
				if ("theme.style.select".equals(strKey)) {
					mSelectThemeStyle = json.getString(strKey);
				}
			}

		} while (false);
	}
	
	//恢复默认值
	private void clear(){
		mWakeupEnable = null;
		mWakeupThreshholdVal = null;
		mDeviceWelcomeMsg = null;
		mWakeupWords = null;
		mPoiMapMode = null;
		mIsUseTencent = null;
		mUpgradeInfo = null;
		mUseTypingEffect = true;
		mEnableTypingEffectItem = null;
		mThemeStyle = null;
	}
	
	/**
	 * json格式内容转成对象
	 * @param strJson
	 {
	   "wakeup.threshhold.value" : 0.0f,
	   "wakeup.words":["你好小贱", "你好菜菜"],
	   "wakeup.enable":false,
	   "device.welcome.msg":"主人你好",
	   "operation.time":"10086"
	   "engine.tencent.isuse":false
	  }
	 *
	 */
	public void parse(String strJson){
		JSONObject json = null;
		try {
			json = new JSONObject(strJson);
		} catch (Exception e) {
			LogUtil.logw("UserConf : " + e.toString());
			return;
		}
		//复原
		clear();
		
		JSONArray jsonArray = json.names();
		if (jsonArray == null){
			LogUtil.logw("UserConf : empty UserConf");
			return;
		}
		
		for (int i = 0; i < jsonArray.length(); ++i) {
			String strkey;
			try {
				strkey = jsonArray.getString(i);
				if ("wakeup.threshhold.value".equals(strkey)){
					LogUtil.logd("int_value " + strkey + ": " + json.getInt(strkey));
					LogUtil.logd("double_value " + strkey + ": " + json.getDouble(strkey));
				}
				LogUtil.logd(strkey + ": " + json.get(strkey));
				parseJsonKey(strkey, json);
			} catch (Exception e) {
				LogUtil.logw("UserConf : " + e.toString());
			}
		}
	}
	
	//对象转json格式字符串内容
	public String toJson(){
		JSONObject json = new JSONObject();
		if (mWakeupEnable != null){
			try {
				json.put("wakeup.enable", mWakeupEnable);
			} catch (Exception e) {
				
			}
		}
		
		if (mWakeupWords != null) {
			try {
				JSONArray jsonArray = new JSONArray();
				for (String wk : mWakeupWords) {
					jsonArray.put(wk);
				}
				json.put("wakeup.words", jsonArray);
			} catch (Exception e) {
				LogUtil.logw("UserConf : " + e.toString());
			}
		}
		
		if (mWakeupThreshholdVal != null) {
			try {
				double val = mWakeupThreshholdVal;
				//保留两位小数,因为float转double会增加很多精度
				try {
					BigDecimal bg = new BigDecimal(val);
					val = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
				} catch (Exception e) {

				}
				json.put("wakeup.threshhold.value", val);
			} catch (Exception e) {

			}
		}
		
		if (mDeviceWelcomeMsg != null) {
			try {
				json.put("device.welcome.msg", mDeviceWelcomeMsg);
			} catch (Exception e) {

			}
		}
		
		if(mPoiMapMode != null){
			try {
				json.put("poi.map.mode", mPoiMapMode);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		if (mIsUseTencent != null){
			try {
				json.put("engine.tencent.isuse", mIsUseTencent);
			} catch (Exception e) {
				
			}
		}

		if (mUpgradeInfo != null) {
			try {
				json.put("device.upgrade.info",mUpgradeInfo);
			}catch (Exception e) {

			}
		}

		if (mEnableTypingEffectItem != null) {
			try {
				json.put("typing.effect.enable.item",mEnableTypingEffectItem);
			}catch (Exception e) {

			}
		}

		if (mUseTypingEffect != null) {
			try {
				json.put("typing.effect.state",mUseTypingEffect);
			}catch (Exception e) {

			}
		}

		if (mThemeStyle != null) {
			try {
				json.put("theme.style.list", mThemeStyle);
			} catch (Exception e) {

			}
		}

		if (mSelectThemeStyle != null) {
			try {
				json.put("theme.style.select", mSelectThemeStyle);
			} catch (Exception e) {

			}
		}

		LogUtil.logd("ConfigData toJson : " + json.toString());
		return json.toString();
	}
	
}

package com.txznet.txz.component.text.yunzhisheng_3_0;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.txz.plugin.IExecPlugin;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.interfaces.NlpTransitionToTxz;

public class YzsOnlineTestParse implements IExecPlugin{

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public Object execute(ClassLoader loader, String path, byte[] data) {
		ParseOnlineToTxz parse = new ParseOnlineToTxz();
		boolean ret = (Boolean) PluginManager.invoke("txz.nlp.yzsOnLImpl", parse);
		PluginManager.invoke("comm.log.logd", "liTest:yzsOnLImpl ret="+ret);
		return true;
	}
	class ParseOnlineToTxz implements NlpTransitionToTxz {

		@Override
		public VoiceParseData TransitionToTxz(VoiceParseData parseData) {
			VoiceParseData newData = new VoiceParseData();
			JSONObject rawJson = new JSONObject();

			try {
				newData = VoiceParseData.parseFrom(VoiceParseData
						.toByteArray(parseData));
				rawJson = (JSONObject) JSON.parse(parseData.strVoiceData);
			} catch (Exception e) {
				return null;
			}
			if (!rawJson.containsKey("service"))
				return null;
			String service = rawJson.getString("service");
			PluginManager.invoke("comm.log.logd", "liTest:service="+service);
			if (service.equals("cn.yunzhisheng.movie")) {
				JSONObject jsonResult = genVoiceJSONObject("unknown",
						"unknown", newData.strText);
				jsonResult.put("answer", "电影新接口测试");
				newData.floatTextScore = 90f;
				newData.strVoiceData = jsonResult.toJSONString();
	
				return newData;
			}
			return null;
		}
		private JSONObject genVoiceJSONObject(String scene, String action,
				String text) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("scene", scene);
			jsonObject.put("action", action);
			jsonObject.put("text", text);

			return jsonObject;
		}
	}
}

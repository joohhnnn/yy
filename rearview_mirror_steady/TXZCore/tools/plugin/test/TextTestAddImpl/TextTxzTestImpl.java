package com.txznet.txz.component.text.txz;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.txz.plugin.interfaces.TextImplBasic;

public class TextTxzTestImpl implements TextImplBasic{

	@Override
	public int initialize(IInitCallback oRun) {
		return 0;
	}

	@Override
	public int setText(String text, ITextCallBack callBack) {
		return 0;
	}

	@Override
	public int setVoiceData(VoiceParseData parseData, ITextCallBack callBack) {
		JSONObject object = new JSONObject();
		object.put("scene", "unknown");
		object.put("action", "unknown");
		object.put("answer", parseData.strText);
		VoiceParseData newParseData = null;
		try {
			newParseData = VoiceParseData.parseFrom(VoiceParseData.toByteArray(parseData));
		} catch (InvalidProtocolBufferNanoException e) {
			e.printStackTrace();
		}
		if (newParseData == null)
			newParseData = new VoiceParseData();
		newParseData.strVoiceData = object.toJSONString();
		newParseData.floatTextScore = 90f;
		callBack.onResult(newParseData);
		return 0;
	}

	@Override
	public void cancel() {
		
	}

	@Override
	public void release() {
		
	}

}

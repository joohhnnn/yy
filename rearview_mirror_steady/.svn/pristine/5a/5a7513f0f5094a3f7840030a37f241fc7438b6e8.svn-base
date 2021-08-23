package com.txznet.txz.component.text.txz;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.data.UiData;
import com.txz.ui.data.UiData.TestResp;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.text.IText;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.util.runnables.Runnable3;

public class TextTxzLocalImpl implements IText {

	private ITextCallBack mCallback;
	private int mSessionId;
	private boolean isCancel;
	private Runnable3<VoiceParseData,ITextCallBack,Integer> mRunnable;

	@Override
	public int initialize(IInitCallback oRun) {
		mSessionId = 0;
		oRun.onInit(true);
		return 0;
	}

	@Override
	public int setVoiceData(VoiceParseData parseData, ITextCallBack callBack) {
		if (parseData == null || TextUtils.isEmpty(parseData.strText)) {
			callBack.onError(-2,mPriority);
			return 0;
		}
		mCallback = callBack;
		mSessionId++;
		isCancel = false;
		mRunnable = new Runnable3<VoiceParseData,ITextCallBack,Integer>(parseData,callBack,mSessionId) {
			@Override
			public void run() {
				byte[] result = NativeData.getNativeData(UiData.DATA_ID_TEST_LOCAL_REGULAR, mP1);
				try {
					TestResp resp = TestResp.parseFrom(result);
					if (!isCancel)
						handleLocalResult(resp, mCallback, mP3,mP1.strText);
				} catch (InvalidProtocolBufferNanoException e) {
					e.printStackTrace();
				}
			}
		};
		AppLogic.runOnBackGround(mRunnable, 0);
		JNIHelper.logd("nlp:startLocal");
		return 0;
	}
	
	@Override
	public int setText(String text, ITextCallBack callBack) {
		mCallback = callBack;
		mSessionId++;
		isCancel = false;
		VoiceParseData parseData = new VoiceParseData();
		parseData.strText = text;
		mRunnable = new Runnable3<VoiceParseData,ITextCallBack,Integer>(parseData,callBack,mSessionId) {
			@Override
			public void run() {
				//byte[] result = NativeData.getNativeData(UiData.DATA_ID_TEST_LOCAL_REGULAR, mP1);
				try {
					//TestResp resp = TestResp.parseFrom(result);
					TestResp resp = new TestResp();
					resp.success = false;
					if (!isCancel)
						handleLocalResult(resp, mCallback, mP3,mP1.strText);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		AppLogic.runOnBackGround(mRunnable, 0);
		JNIHelper.logd("nlp:startLocal");
		return 0;
	}

	@Override
	public void cancel() {
		JNIHelper.logd("nlp:cancel!");
		if (mRunnable != null)
			AppLogic.removeBackGroundCallback(mRunnable);
		isCancel = true;
	}

	@Override
	public void release() {
	}

	private void handleLocalResult(TestResp resp, ITextCallBack callBack, int id ,String text) {
		if (mSessionId != id)
			return;
		JNIHelper.logd("nlp:handleLocal,success=" + resp.success + ",type="
				+ resp.type);
		VoiceParseData parseData = null;
		if (resp.success) {
			try {
				parseData = VoiceParseData.parseFrom(resp.data);
			} catch (InvalidProtocolBufferNanoException e) {
				e.printStackTrace();
			}
			try {
				JSONObject object = new JSONObject(parseData.strVoiceData);
				if (!object.has("text")) {
					object.put("text",text);
					parseData.strVoiceData = object.toString();
				}
			} catch (JSONException e) {
				//e.printStackTrace();
			}
		}
		if (parseData != null)
			callBack.onResult(parseData,mPriority);
		else
			callBack.onError(-1,mPriority);
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
}

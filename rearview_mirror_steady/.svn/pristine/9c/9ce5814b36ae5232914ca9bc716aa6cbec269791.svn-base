package com.txznet.txz.component.wakeup.ifly;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.wakeup.IWakeup;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.ModuleManager;

public class WakeupIflyImpl implements IWakeup {
	IInitCallback mInitCallback = null;
	IWakeupCallback mWakeupCallback = null;

	// 语音唤醒对象
	private VoiceWakeuper mIvw = null;
	// 唤醒结果内容
	private String mResultString = null;

	private boolean loadResource() {
		// 加载识唤醒地资源，resPath为本地识别资源路径
		StringBuffer param = new StringBuffer();

		String resPath = ResourceUtil.generateResourcePath(GlobalContext.get(),
				RESOURCE_TYPE.assets, "ivw/548a6747.jet");

		param.append(ResourceUtil.IVW_RES_PATH + "=" + resPath);
		param.append("," + ResourceUtil.ENGINE_START + "="
				+ SpeechConstant.ENG_IVW);
		boolean ret = SpeechUtility.getUtility().setParameter(
				ResourceUtil.ENGINE_START, param.toString());

		if (!ret) {
			JNIHelper.logd("start engine fail");
			return false;
		}

		return true;
	}

	private void initParams() {
		// 清空参数
		mIvw.setParameter(SpeechConstant.PARAMS, null);
		// 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
		mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:-40");
		// 设置唤醒模式
		mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
		// 设置持续进行唤醒
		mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "1");
	}

	@Override
	public int initialize(String[] cmds, final IInitCallback oRun) {
		ModuleManager.getInstance().initSdk_Ifly();

		mInitCallback = oRun;
		boolean bRet = loadResource();

		if (!bRet) {
			JNIHelper.logd("loadResource error");
			return -1;
		}

		// 初始化唤醒对象
		mIvw = VoiceWakeuper.createWakeuper(GlobalContext.get(), null);

		if (mIvw == null) {
			JNIHelper.logd("mIvw = null");
			return -1;
		}

		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {

				if (mInitCallback != null) {
					mInitCallback.onInit(true);
				}
			}

		}, 10);

		return 0;
	}

	@Override
	public int start(IWakeupCallback oCallback) {
		if (mIvw == null) {
			JNIHelper.logd("ivw is not inited");
			return -1;
		}
		mWakeupCallback = oCallback;
		loadResource();
		initParams();
		mIvw.startListening(mWakeuperListener);

		JNIHelper.logd("ivw start");
		return 0;
	}

	@Override
	public void stop() {
		if (mIvw != null) {
			JNIHelper.logd("ivw stop");
			mIvw.stopListening();
			mIvw.cancel();
		}
	}

	private WakeuperListener mWakeuperListener = new WakeuperListener() {

		@Override
		public void onResult(WakeuperResult result) {
			JNIHelper.logd("onResult");

			try {
				String text = result.getResultString();
				JSONObject object;
				object = new JSONObject(text);
				StringBuffer buffer = new StringBuffer();
				buffer.append("RAW: " + text);
				buffer.append("\n");
				buffer.append("operate type" + object.optString("sst"));
				buffer.append("\n");
				buffer.append("wakeup id" + object.optString("id"));
				buffer.append("\n");
				buffer.append("score" + object.optString("score"));
				buffer.append("\n");
				buffer.append("begin dian" + object.optString("bos"));
				buffer.append("\n");
				buffer.append("end dian" + object.optString("eos"));
				mResultString = buffer.toString();

				JNIHelper.logd("get score = " + object.optString("score"));
			} catch (JSONException e) {
				mResultString = "parse result error";
				e.printStackTrace();
			}

			if (mWakeupCallback != null) {
				mWakeupCallback.onWakeUp(mResultString);
			}
		}

		@Override
		public void onError(SpeechError error) {
			JNIHelper.logd("Error");
		}

		@Override
		public void onBeginOfSpeech() {
			JNIHelper.logd("beginOfSpeech");
		}

		@Override
		public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
			JNIHelper.logd("onEvent");
		}

		@Override
		public void onVolumeChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}
	};

	@Override
	public void setWakeupKeywords(String[] keywords) {
		// TODO 讯飞不支持设置唤醒词
	}

	@Override
	public int startWithRecord(IWakeupCallback oCallback,
			RecordOption options, String[] overTag) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void stopWithRecord() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWakeupThreshold(float val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enableVoiceChannel(boolean enable) {
		// TODO Auto-generated method stub
		
	}

}

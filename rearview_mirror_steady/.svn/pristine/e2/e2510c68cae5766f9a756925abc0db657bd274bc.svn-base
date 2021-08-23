package com.txznet.txz.component.wakeup.txz;

import java.util.ArrayList;
import java.util.List;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrEngineFactory;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrEngineFactory.AsrEngineAdapter;
import com.txznet.txz.component.tts.yunzhisheng_3_0.AudioSourceDistributer;
import com.txznet.txz.component.wakeup.IWakeup;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.record.OnlineParseRecorder;
import com.txznet.txz.module.record.Recorder;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.wakeup.WakeupPcmHelper;
import com.txznet.txz.util.runnables.Runnable2;

public class WakeupTxzImpl implements IWakeup {

	IInitCallback mInitCallback = null;
	IWakeupCallback mWakeupCallBack = null;
	boolean bInited = false;
	private String[] defaultWords = { "你好小踢" };
	private String[] wakeupWords;
	AsrEngineAdapter mEngine = null;
	
	public WakeupTxzImpl() {
		mEngine = AsrEngineFactory.getAdapter();
	}

	@Override
	public int initialize(final String[] cmds, IInitCallback oRun) {
		mInitCallback = oRun;
		wakeupWords = cmds;
		
		String data = new JSONBuilder().put("cmds", cmds).toString();
		ServiceManager.getInstance().sendInvoke(ServiceManager.WAKEUP, "wakeup.init", data.getBytes(), null);
		return 0;
	}
   
	private void doInit(){
		Runnable run = new Runnable() {
			@Override
			public void run() {
				bInited = true;
				setWakeupKeywords(wakeupWords);
				if (mInitCallback != null) {
					mInitCallback.onInit(true);
					mInitCallback = null;
				}
			}
		};
		AppLogic.runOnBackGround(run, 0);
	}
	
	@Override
	public synchronized int start(WakeupOption oOption) {
		mWakeupCallBack = oOption.wakeupCallback;
		WakeupManager.getInstance().checkUsingAsr(new Runnable2<AsrEngineAdapter,WakeupOption>(mEngine, oOption) {
			@Override
			public void run() {
				mP1.startWakeup(mP2.wakeupCallback);
			}
		}, new Runnable() {
			@Override
			public void run() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.WAKEUP,  "wakeup.wakeup.start",  null, null);
			}
		});
		return 0;
	}

	@Override
	public synchronized void stop() {
		mWakeupCallBack = null;
		ServiceManager.getInstance().sendInvoke(ServiceManager.WAKEUP,  "wakeup.wakeup.stop",  null, null);
		mEngine.stopWakeup();
	}

	@Override
	public void setWakeupKeywords(String[] keywords) {
		//避免未初始化完，就去set。
		if (!bInited){
			wakeupWords = keywords;
			return;
		}
		
		if (keywords == null) {
			keywords = defaultWords;
		}
		
		List<String> keyWordList = new ArrayList<String>();
		for (int i = 0; i < keywords.length; ++i) {
			keyWordList.add(keywords[i]);
		}
		mEngine.setWakeupWords(keyWordList);
		
		String data = new JSONBuilder().put("cmds", keywords).toString();
		ServiceManager.getInstance().sendInvoke(ServiceManager.WAKEUP,  "wakeup.wakeup.setwords",  data.getBytes(), null);
	}
    
	private Recorder mRecorder;
	@Override
	public int startWithRecord(IWakeupCallback oCallback, RecordOption options, String[] overTag) {
		if (options.mNeedOnLineParse) {
			mRecorder = new OnlineParseRecorder(options.mOnLineParseTaskId);
		} else {
			mRecorder = new Recorder(options.mSavePathPrefix);
		}
 		AudioSourceDistributer.getIntance().addRecorder(mRecorder);
 		if (overTag != null && overTag.length > 0){
 			setWakeupKeywords(overTag);
 		}
 		mEngine.startWithRecord(oCallback);
		return 0;
	}

	@Override
	public void stopWithRecord() {
		AudioSourceDistributer.getIntance().delRecorder(mRecorder);
		if (mRecorder != null){
			mRecorder.close();
			mRecorder = null;
		}
 		mEngine.stopWithRecord();
	}


	@Override
	public void setWakeupThreshold(float val) {
		JNIHelper.logd("setWakeupThreshold: " + val);
		AsrWakeupEngine.WAKEUP_OPT_THRESHOLD = val;
	}
	
	private synchronized void doWakeup(String text){
		if (mWakeupCallBack != null){
		    mWakeupCallBack.onWakeUp(text, 0f);	
		}
	}
	
	public byte[]  invokeWakeup(final String packageName, String command, byte[] data){
		JNIHelper.logd("packageName :" + packageName + "comm : " + command);
		if ("wakeup.event.init.result".equals(command)){
			boolean bRet = false;
			try{
				bRet = Boolean.parseBoolean(new String(data));
			}catch(Exception e){
			}
			if (bRet){
				doInit();
			}
		}else if ("wakeup.event.wakeup.result".equals(command)){
			final String text = new String(data);
			Runnable oRun = new Runnable(){
				@Override
				public void run() {
					if ("WAKEUP".equals(text)) {
                        String[] keywords = WakeupManager.getInstance().getWakeupKeywords_Sdk();
                        if (keywords != null && keywords.length > 0){
                        	doWakeup(keywords[0]);
                        }
					} else {
						doWakeup(text);
					}
				}
			};
			AppLogic.runOnBackGround(oRun, 0);
		}else if ("wakeup.event.process.start".equals(command)){
			//main purpose is to avoid of the case that wakeup process is killed by some reason
			JNIHelper.logd("wakeup remote service start");
			WakeupManager.getInstance().start();
		}
		return null;
	}

	@Override
	public void enableVoiceChannel(boolean enable) {
		JNIHelper.logd("enableVoiceChannel :  " + enable);
		WakeupPcmHelper.enableVoiceChannel(enable);
		ServiceManager.getInstance().sendInvoke(ServiceManager.WAKEUP,
				"wakeup.wakeup.enablevoice", ("" + enable).getBytes(), null);
	}
}

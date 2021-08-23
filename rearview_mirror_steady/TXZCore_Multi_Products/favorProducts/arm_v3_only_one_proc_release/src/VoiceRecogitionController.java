package com.txznet.txz.component.asr.mix;

import java.lang.reflect.Proxy;
import android.os.HandlerThread;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.IImportKeywordsCallback;
import com.txznet.txz.component.asr.mix.IVoiceRecogition.VoiceRecogitionHandler;
import com.txznet.txz.component.wakeup.IWakeup;
import com.txznet.txz.component.wakeup.IWakeup.WakeupOption;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.TXZHandler;

public class VoiceRecogitionController {
	private static VoiceRecogitionController mController = new VoiceRecogitionController();
	IWakeup.IInitCallback mWakeupInitCallBack = null;
	IAsr.IInitCallback mAsrInitCallBack = null;
	private VoiceRecognizer.InitListener mInitLister = null;
	private IVoiceRecogition mRecognitionProxy = null;
	private IVoiceRecogition mRecognizer = null;
	private TXZHandler mWorkHandler = null;
	private HandlerThread mWorkThread = null;
	private InitStatus mInitStatus = InitStatus.INIT_NONE;
	private boolean mInitSuccessed = false;
	public enum InitStatus{
		INIT_NONE,
		INIT_DOING,
		INIT_DONE
	}
	
	private VoiceRecogitionController(){
		
	}
	
	public static VoiceRecogitionController getInstance(){
		return mController;
	}
	
	private void notifyInit() {
		synchronized (InitStatus.class) {
			mInitStatus = InitStatus.INIT_DONE;

			IWakeup.IInitCallback wakeupCallBack = mWakeupInitCallBack;
			mWakeupInitCallBack = null;
			if (wakeupCallBack != null) {
				wakeupCallBack.onInit(mInitSuccessed);
			}

			IAsr.IInitCallback asrCallBack = mAsrInitCallBack;
			mAsrInitCallBack = null;
			if (asrCallBack != null) {
				asrCallBack.onInit(mInitSuccessed);
			}
		}
	}
	
	private void init(){
		synchronized (InitStatus.class) {
			if (mInitStatus == InitStatus.INIT_DOING){
				return;
			}
			mInitStatus = InitStatus.INIT_DOING;
		}
		
		mRecognizer = new VoiceRecognizer();
		mWorkThread = new HandlerThread("VoiceRecogition", Thread.MAX_PRIORITY);
		mWorkThread.start();
		mWorkHandler = new TXZHandler(mWorkThread.getLooper());
		mRecognitionProxy = proxy(mRecognizer, mWorkHandler);
		
		mInitLister = new VoiceRecognizer.InitListener() {
			@Override
			public void onInit(boolean bSuccessed) {
				mInitStatus = InitStatus.INIT_DONE;
				mInitSuccessed = true;
				JNIHelper.logd("init VoiceRecogizer : " + mInitSuccessed);
				notifyInit();
			}
		};
		
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				mRecognizer.init(mInitLister);
			}
		}, "VoiceRecognizer");
		th.setPriority(Thread.MAX_PRIORITY);
		th.start();
	}
	
	public int initialize(IWakeup.IInitCallback oRun) {
		synchronized (InitStatus.class) {
			mWakeupInitCallBack = oRun;
			if (mInitStatus == InitStatus.INIT_DONE){
				notifyInit();
				return 0;
			}else if (mInitStatus == InitStatus.INIT_DOING){
				return 0;
			}
		}
		init();
		return 0;
	}
	
	public int initialize(IAsr.IInitCallback oRun) {
		synchronized (InitStatus.class) {
			mAsrInitCallBack = oRun;
			if (mInitStatus == InitStatus.INIT_DONE) {
				notifyInit();
				return 0;
			} else if (mInitStatus == InitStatus.INIT_DOING) {
				return 0;
			}
		}
		init();
		return 0;
	}
	
	public void start(AsrOption oOption){
		mRecognitionProxy.start(oOption);
	}
	
	public void stop(AsrOption oOption){
		mRecognitionProxy.stop(oOption);
	}
	
	public void cancel(AsrOption oOption){
		mRecognitionProxy.cancel(oOption);
	}
	
	
	public void start(WakeupOption oOption){
		mRecognitionProxy.start(oOption);
	}
	
	public void stop(WakeupOption oOption){
		mRecognitionProxy.stop(oOption);
	}

	public void setWakeupKeywords(String[] keywords) {
		if (mRecognitionProxy == null) {
			return;
		}
		mRecognitionProxy.setWakeupKeywords(keywords);
	}
	
	public boolean importKeywords(SdkKeywords oKeywords, IImportKeywordsCallback oCallback) {
		mRecognitionProxy.importKeywords(oKeywords, oCallback);
		return true;
	}
	
	public void setWakeupThreshold(float val) {
		mRecognitionProxy.setWakeupThreshold(val);
	}

	public void enableVoiceChannel(boolean enable) {	
		mRecognitionProxy.enableVoiceChannel(enable);
	}
	
	public static IVoiceRecogition proxy(IVoiceRecogition oVoiceRecognizer, TXZHandler handler){
		IVoiceRecogition proxy = null;
		Class<?>[] interfaces = new Class<?>[]{IVoiceRecogition.class};
		ClassLoader classLoader = IAsrCallBackProxy.class.getClassLoader();
		proxy = (IVoiceRecogition)Proxy.newProxyInstance(classLoader, interfaces, new VoiceRecogitionHandler(oVoiceRecognizer, handler));
		return proxy;
	}
}

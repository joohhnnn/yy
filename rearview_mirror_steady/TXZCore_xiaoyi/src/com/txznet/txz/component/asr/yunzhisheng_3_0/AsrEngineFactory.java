package com.txznet.txz.component.asr.yunzhisheng_3_0;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;

import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine.AsrAndWakeupIIintCallback;
import com.txznet.txz.component.wakeup.IWakeup.IWakeupCallback;
import com.txznet.txz.jni.JNIHelper;

public class AsrEngineFactory {
	public static interface AsrEngineAdapter{
		public void initialize(AsrAndWakeupIIintCallback oRun);
		public void startAsr(AsrOption oOption);
		public void stopAsr();
		public void cancelAsr();
		public void startWakeup(IWakeupCallback oCallback);
		public void stopWakeup();
		public void setWakeupWords(List<String> keyWordList);
		public int startWithRecord(IWakeupCallback oCallback);
		public void stopWithRecord();
	}
	
	private static final AsrEngineAdapter engineAdapter = new AsrEngineAdapter(){
		
		@Override
		public void initialize(AsrAndWakeupIIintCallback oRun){
			AsrWakeupEngine.getEngine().initialize(oRun);
		}
		
		@Override
		public void stopWakeup() {
			AsrWakeupEngine.getEngine().stopWakeup();
		}
		
		@Override
		public void stopAsr() {
			AsrWakeupEngine.getEngine().stopAsr();
		}
		
		@Override
		public void startWakeup(IWakeupCallback oCallback){
			AsrWakeupEngine.getEngine().startWakeup(oCallback);
		}
		
		@Override
		public void startAsr(AsrOption oOption) {
			AsrWakeupEngine.getEngine().startAsr(oOption);
		}
		
		@Override
		public void cancelAsr() {
			AsrWakeupEngine.getEngine().cancelAsr();
		}
		@Override
		public void setWakeupWords(List<String> keyWordList){
			AsrWakeupEngine.getEngine().setWakeupWords(keyWordList);
		}

		@Override
		public int startWithRecord(IWakeupCallback oCallback) {
			AsrWakeupEngine.getEngine().startWithRecord(oCallback);
			return 0;
		}

		@Override
		public void stopWithRecord() {
			AsrWakeupEngine.getEngine().stopWithRecord();
		}
	};
	
	private static List<Runnable> sTaskQueue = new LinkedList<Runnable>(); 

	static class AsrEngineProxy implements InvocationHandler {
		@Override
		public Object invoke(final Object proxy, final Method method,
				final Object[] args) throws Throwable {
			synchronized (AsrEngineProxy.class) {
				int delay = 0;
				//if (method.getName().equals("stopWakeup")) {
					for (Runnable run : sTaskQueue) {
						JNIHelper.logd("remove last time startWakeup by " + method.getName());
						AsrWakeupEngine.getEngine().delOnBackGround(run);
					}
					sTaskQueue.clear();
				//}

				Runnable oRun = new Runnable() {
					@Override
					public void run() {
						try {
							method.invoke(engineAdapter, args);
						} catch (Exception e) {
						}
					}
				};
				if (method.getName().equals("startWakeup")) {
					sTaskQueue.add(oRun);
					delay = 100;
				}
				
				AsrWakeupEngine.getEngine().runOnBackGround(oRun, delay);
				if (method.getReturnType()  == void.class) {
					return null;
				} else if (method.getReturnType() == int.class) {
					return 0;
				}
				return method.getReturnType().newInstance();
			}
		}
	}
	
	private final static AsrEngineAdapter ADAPTER = (AsrEngineAdapter) Proxy
			.newProxyInstance(engineAdapter.getClass().getClassLoader(),
					engineAdapter.getClass().getInterfaces(), new AsrEngineProxy());

	public static AsrEngineAdapter getAdapter(){
		return ADAPTER;
	}

}

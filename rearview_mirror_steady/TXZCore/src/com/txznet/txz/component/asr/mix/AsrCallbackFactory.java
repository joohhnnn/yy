package com.txznet.txz.component.asr.mix;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.asr.IAsr.AsrOption;

public class AsrCallbackFactory {
	public static IAsrCallBackProxy proxy(){
		IAsrCallBackProxy proxy = null;
		Class<?>[] interfaces = new Class<?>[]{IAsrCallBackProxy.class};
		ClassLoader classLoader = IAsrCallBackProxy.class.getClassLoader();
		proxy = (IAsrCallBackProxy)Proxy.newProxyInstance(classLoader, interfaces, new AsrInvocationHandler(new SimpleAsrCallBackHandler()));
		return proxy;
	}
	
	public static IAsrCallBackProxy proxy(IAsrCallBackProxy callback, ICallBackNotify notify){
		IAsrCallBackProxy proxy = null;
		Class<?>[] interfaces = new Class<?>[]{IAsrCallBackProxy.class};
		ClassLoader classLoader = IAsrCallBackProxy.class.getClassLoader();
		proxy = (IAsrCallBackProxy)Proxy.newProxyInstance(classLoader, interfaces, new ComplexAsrInvocationHandler(callback, notify));
		return proxy;
	}
	
	public static class AsrInvocationHandler implements InvocationHandler{
		private IAsrCallBackProxy mProxy = null;
        public AsrInvocationHandler(IAsrCallBackProxy proxy){
        	mProxy = proxy;
        }
		@Override
		public Object invoke(Object proxy, final Method method, final Object[] args)
				throws Throwable {
			if (method.getName().equals("setAsrOption")){
				method.invoke(mProxy, args);
				return null;
			}
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					try {
						AsrOption oOption = mProxy.getAsrOption();
						if (oOption != null && oOption.mCallback != null) {
							method.invoke(mProxy, args);
						}
					} catch (Exception e) {
						 if (e instanceof InvocationTargetException)
					        {
					            Throwable targetEx = ((InvocationTargetException) e)
					                    .getTargetException();
								LogUtil.loge("exception : " + targetEx.toString(),targetEx);
					            return ;
					        }
						LogUtil.loge("exception : " + e.toString(),e);
					}
				}

			};
			AppLogic.runOnBackGround(oRun, 0);
			return null;
		}
		
	}
	
	public static interface ICallBackNotify{
		public boolean enable();
	}
	
	public static class ComplexAsrInvocationHandler implements InvocationHandler{
		private IAsrCallBackProxy mProxy = null;
		private ICallBackNotify mNotify = null;
        public ComplexAsrInvocationHandler(IAsrCallBackProxy proxy, ICallBackNotify notify){
        	mProxy = proxy;
        	mNotify = notify;
        }
		@Override
		public Object invoke(Object proxy, final Method method, final Object[] args)
				throws Throwable {
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					try {
						//没有设置notify或者设置了notify并且enable返回true才执行
						if (mNotify == null || mNotify.enable()) {
							method.invoke(mProxy, args);
						}else{
							LogUtil.logw("notify enable false");
						}
					} catch (Exception e) {
						LogUtil.loge("exception : " + e.toString(),e);
					}
				}

			};
			AppLogic.runOnBackGround(oRun, 0);
			return null;
		}
		
	}
}

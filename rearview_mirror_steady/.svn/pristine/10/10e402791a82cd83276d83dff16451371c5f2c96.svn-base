package com.txznet.txz.component.text.yunzhisheng_3_0;

import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.text.IText;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.location.LocationManager;
import com.unisound.client.SpeechConstants;
import com.unisound.client.TextUnderstander;
import com.unisound.client.TextUnderstanderListener;

/*
 * TextManager中已经保证cancel、setText是同步的了，所以两个接口不必再加锁进行同步了。
 */
public class TextYunzhishengImpl implements IText{
    private TextUnderstander mTextUnderstander;
    private TextUnderstanderListener mTextUnderstanderListener;
    private IInitCallback mInitCallBack;
    private ITextCallBack mTextCallBack;
    private final static int TIMEOUT = 5000;
    
    private Runnable checkTimeOutTask = new Runnable(){
		@Override
		public void run() {
			JNIHelper.logd("parseText TimeOut");
			mTextUnderstander.cancel();
			final ITextCallBack callBack = mTextCallBack;
			mTextCallBack = null;
			if (callBack != null){
				Runnable oRun = new Runnable() {
					@Override
					public void run() {
						callBack.onError(NetTimeOutError);
					}
				};
				AppLogic.runOnBackGround(oRun, 0);
			}
		}
    	
    };
    
	@Override
	public int initialize(IInitCallback callBack) {
		mInitCallBack = callBack;
        mTextUnderstander = new TextUnderstander(GlobalContext.get(), ProjectCfg.getYunzhishengAppId()//
		                                                            , ProjectCfg.getYunzhishengSecret());
        mTextUnderstander.setOption(SpeechConstants.NLU_SCENARIO, "incar");
        
        mTextUnderstanderListener = new TextUnderstanderListener() {
			
			@Override
			public void onResult(int arg0, String arg1) {
				JNIHelper.logd("onResult = " + arg0 + " : " + arg1);
				doResult(arg0, arg1);
			}
			
			@Override
			public void onEvent(int arg0) {
				JNIHelper.logd("onEvent:" + arg0);
			}
			
			@Override
			public void onError(int arg0, String arg1) {
				JNIHelper.logd("onError = " + arg0 + " :"+ arg1);
				doError(ParseError);
			}
		};
        
        mTextUnderstander.setListener(mTextUnderstanderListener);
        
        Runnable oRun= new Runnable(){
			@Override
			public void run() {
		        int nRet = 0;
		        nRet = mTextUnderstander.init("");
		        IInitCallback cb = mInitCallBack;
		        mInitCallBack = null;
		        
		        if (cb != null){
		        	cb.onInit(nRet == 0);
		        }
			}
        };
        
        AppLogic.runOnBackGround(oRun, 0);
		return 0;
	}
    
	private void doResult(int type, String jsonResult){
		AppLogic.removeBackGroundCallback(checkTimeOutTask);
		JNIHelper.logd("onResult:" + jsonResult);
		final ITextCallBack callBack = mTextCallBack;
		final String result = jsonResult;
		mTextCallBack = null;
		if (callBack != null){
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					callBack.onResult(result);
				}
			};
			AppLogic.runOnBackGround(oRun, 0);
		}
	}
		
	
	private void doError(int errorCode){
		AppLogic.removeBackGroundCallback(checkTimeOutTask);
		JNIHelper.logd("errorCode : " + errorCode);
		final ITextCallBack callBack = mTextCallBack;
		mTextCallBack = null;
		if (callBack != null){
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					callBack.onError(ParseError);
				}
			};
			AppLogic.runOnBackGround(oRun, 0);
		}
	}
	
	@Override
	public void cancel() {
		JNIHelper.logd("cancel");
		AppLogic.removeBackGroundCallback(checkTimeOutTask);
		mTextUnderstander.cancel();
		final ITextCallBack callBack = mTextCallBack;
		mTextCallBack = null;                         
		if (callBack != null){
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					callBack.onError(InterruptedError);
				}
			};
			AppLogic.runOnBackGround(oRun, 0);
		}
	}
		

	@Override
	public void release() {
		
	}
    
	@Override
	public int setText(String text, ITextCallBack callBack) {
		mTextCallBack = callBack;
		try {
			LocationInfo location = LocationManager.getInstance().getLastLocation();
			if (location == null || location.msgGeoInfo == null || location.msgGeoInfo.strCity == null){
				JNIHelper.logd("定位失败!!!");//donothing
			}else{
				JNIHelper.logd("city = " + location.msgGeoInfo.strCity);
			    mTextUnderstander.setOption(SpeechConstants.GENERAL_CITY, location.msgGeoInfo.strCity);
			}
		} catch (Exception e) {
			
		}
		
		try {
			LocationInfo location = LocationManager.getInstance()
					.getLastLocation();
			double lat = location.msgGpsInfo.dblLat;
			double lng = location.msgGpsInfo.dblLng;
			String strGpsInfo = lat + "," + lng;
			JNIHelper.logd("strGpsInfo: " + strGpsInfo);
			mTextUnderstander.setOption(SpeechConstants.GENERAL_GPS,
					strGpsInfo);
		} catch (Exception e) {
		}
		AppLogic.removeBackGroundCallback(checkTimeOutTask);
		AppLogic.runOnBackGround(checkTimeOutTask, TIMEOUT);
		mTextUnderstander.setText(text);
		return 0;
	}

}

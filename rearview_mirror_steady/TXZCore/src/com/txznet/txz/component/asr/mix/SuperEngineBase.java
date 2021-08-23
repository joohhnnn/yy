package com.txznet.txz.component.asr.mix;

import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.mix.IAsrCallBackProxy.CallBackOption;
import com.txznet.txz.module.location.LocationManager;

public abstract class SuperEngineBase implements IAsr{
	public final static int NONE_CAPACITY = 0x0000;
	public final static int NET_ASR_CAPACITY = 0x0001;
	public final static int LCOAL_ASR_CAPACITY = 0x0010;
	
	private int mEngineType = 0;
	private boolean mForbidConnect = false;
	public SuperEngineBase(){
		
	}
	
	public SuperEngineBase(int nEngineType){
		mEngineType = nEngineType;
	}
	
	public void setEngineType(int nEngineType){
		mEngineType = nEngineType;
	}
	
	public int getEngineType(){
		return mEngineType;
	}
	
	public int capacity(){
		return  NONE_CAPACITY;
	}
	
	public SuperEngineBase getNetEngine(){
		return null;
	}
	
	protected boolean isForbidConnect(){
		return mForbidConnect;
	}
	
	protected void forbidConnect(boolean forbid){
		mForbidConnect = forbid;
	}
	
	public void release(boolean forbidConnect){
		forbidConnect(forbidConnect);
		release();
	}
	
	public static  String getCurrentCity(){
		String strCity = null;
		try {
			LocationInfo location = LocationManager.getInstance()
					.getLastLocation();
			strCity = location.msgGeoInfo.strCity;
		} catch (Exception e) {
		}
        return strCity;
	}
	
	public static String getGpsInfo(){
		String strGpsInfo = null;
		try {
			LocationInfo location = LocationManager.getInstance()
					.getLastLocation();
			double lat = location.msgGpsInfo.dblLat;
			double lng = location.msgGpsInfo.dblLng;
			strGpsInfo = lat + "," + lng;
		} catch (Exception e) {
		}
		return strGpsInfo;
	}
	
	public static  IAsrCallback createAsrCallBack(final IAsrCallBackProxy proxy, final CallBackOption cbOption){
		IAsrCallback cb =  new IAsrCallback() {
			public void onStart(AsrOption option){
				proxy.onStart(cbOption);
			}
			
			public void onEnd(AsrOption option){
				proxy.onEnd(cbOption);
			}
			
			public void onSpeechBegin(AsrOption option){
				proxy.onBeginOfSpeech(cbOption);
			}
			
			public void onSpeechEnd(AsrOption option){
				proxy.onEndOfSpeech(cbOption);
			}
			
			public void onSuccess(AsrOption option, VoiceParseData oVoiceParseData){
				proxy.onSuccess(cbOption, oVoiceParseData);
			}
			
			public void onAbort(AsrOption option, int error){
				proxy.onError(cbOption, error);
			}
			
			public void onError(AsrOption option, int error, String desc, String speech, int error2){
				proxy.onError(cbOption, error2);
			}
			
			public void onCancel(AsrOption option){
				proxy.onError(cbOption, -1);
			}
			
			public void onVolume(AsrOption option, int volume) {
				proxy.onVolume(cbOption, volume);
			}

			@Override
			public void onPartialResult(AsrOption option,String partialResult) {
				proxy.onPartialResult(cbOption, partialResult);
			}
		};
		return cb;
	}
}

package com.txznet.txz.module.fm;

import java.util.ArrayList;
import java.util.List;

import com.txz.ui.event.UiEvent;
import com.txz.ui.fm.FmData;
import com.txz.ui.fm.FmData.FMResultData;
import com.txz.ui.fm.FmData.FMSettingData;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.ui.win.record.RecorderWin;

public class FmManager extends IModule{
	private static FmManager sModuleInstance = null;

	private FmManager() {
	}

	public static FmManager getInstance() {
		if (sModuleInstance == null) {
			synchronized (FmManager.class) {
				if (sModuleInstance == null)
					sModuleInstance = new FmManager();
			}
		}
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		// 注册需要处理的事件
		regEvent(UiEvent.EVENT_CUSTOM_FM,
						FmData.SUBEVENT_FM_TOFREQ);
		regEvent(UiEvent.EVENT_CUSTOM_AM,
				FmData.SUBEVENT_FM_TOFREQ);
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterStartJni() {
		// 发送初始化需要触发的事件
		return super.initialize_AfterStartJni();
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		// 处理事件
		switch (eventId) {
		case UiEvent.EVENT_CUSTOM_FM: {
			switch (subEventId) {
			case FmData.SUBEVENT_FM_TOFREQ:
				toFmFreq(data);
				break;
			default:
				break;
			}
		}
		break;
		case UiEvent.EVENT_CUSTOM_AM:{
			switch (subEventId) {
			case FmData.SUBEVENT_FM_TOFREQ:
				toAmFreq(data);
				break;
			default:
				break;
			}
		}
		default:
			break;
		}
		return super.onEvent(eventId, subEventId, data);
	}
	
	/**
	 * 调频工具类
	 * 
	 * @author txz
	 *
	 */
	public static interface FMTool {
		/**
		 * 
		 * 
		 * @param freq频率
		 * @param unit 单位, 0 - HZ, 1-KHZ, 3-MHZ
		 * @return
		 */
		public void toFmFreq(float freq, int unit);
	}
	
	private void toFmFreq(byte[] data){
		if (data == null){
			return;
		}
		
		if (mFmTool == null){
			String spk = NativeData.getResString("RS_FM_NO_TOOL");
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		
		try {
			FMResultData fmResultData = FMResultData.parseFrom(data);
			String freqValue = fmResultData.strFreq;
			int unit = fmResultData.uint32Unit;
			String strUnit = "";
			if (unit == 0){
				strUnit = NativeData.getResString("RS_FM_UNIT_HZ");
			}else if (unit == 1){
				strUnit = NativeData.getResString("RS_FM_UNIT_KHZ");
			}else if (unit == 2){
				strUnit = NativeData.getResString("RS_FM_UNIT_MHZ");
			}
			
			String strFreq = freqValue.replace("点", ".");
			final float fFreq = Float.parseFloat(strFreq);
			if (fFreq > mMaxFreqValue || fFreq < mMinFreqValue){
				String spk = NativeData.getResString("RS_FM_RANGE")
						.replace("%MIN%", mMinFreqValue + "")
						.replace("%MAX%", mMaxFreqValue + "");
				RecorderWin.speakTextWithClose(spk, null);
				return;
			}
			if(mJumpsPoints != null && mJumpsPoints.contains(fFreq)){
				String spk = NativeData.getResString("RS_FM_UNSUPPORT");
				RecorderWin.speakTextWithClose(spk, null);
				return;
			}
			String spk = NativeData.getResPlaceholderString("RS_FM_TO", "%CMD%", fFreq + strUnit);
			RecorderWin.speakTextWithClose(spk,
					new Runnable() {
						@Override
						public void run() {
							if (mFmTool != null) {
								mFmTool.toFmFreq(fFreq, 2);
							}
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private FMTool mFmTool = null;
	public void setFmTool(FMTool fmTool){
		mFmTool = fmTool;
	}
	
	private float mMinFreqValue = 0;
	private float mMaxFreqValue = 0;
	private List<Float> mJumpsPoints = new ArrayList<Float>();
	public void setFmFreqDistance(float min, float max){
		mMinFreqValue = min;
		mMaxFreqValue = max;
		
		FMSettingData fmSettingData = new FMSettingData();
		fmSettingData.uint32MinValue = (int)min;
		fmSettingData.uint32MaxValue = (int)max;
		if (fmSettingData.uint32MaxValue < max){
			fmSettingData.uint32MaxValue = (int)max + 1;
		}
		JNIHelper.sendEvent(UiEvent.EVENT_CUSTOM_FM, FmData.SUBEVENT_FM_SETTING, fmSettingData);
	}
	
	public void setFmFreqDistance(int min, int max){
		mMinFreqValue = min;
		mMaxFreqValue = max;
		FMSettingData fmSettingData = new FMSettingData();
		fmSettingData.uint32MinValue = min;
		fmSettingData.uint32MaxValue = max;
		JNIHelper.sendEvent(UiEvent.EVENT_CUSTOM_FM, FmData.SUBEVENT_FM_SETTING, fmSettingData);
	}
	
	public byte[] invokeTXZFM(final String packageName, String command, byte[] data) {
		if (command.equals("setdistance")) {
			if (data == null){
				return null;
			}
			try{
			JSONBuilder json = new JSONBuilder(new String(data));
			float minVal = json.getVal("minVal", Float.class);
			float maxVal = json.getVal("maxVal", Float.class);
			Boolean hasJump = json.getVal("hasJump", Boolean.class);
			if(hasJump != null && hasJump == true){
				int index = 0;
				mJumpsPoints.clear();
				while(true){
					Float fmVal = json.getVal("jump" + index, Float.class);
					if(fmVal == null){
						break;
					}
					
					index ++;
					mJumpsPoints.add(fmVal);
				}
			}else {
				mJumpsPoints.clear();
			}
			
			setFmTool(new FMTool() {
				@Override
				public void toFmFreq(float freq, int unit) {
					JSONBuilder json = new JSONBuilder();
					Float oFreq = freq;
					json.put("freqValue", oFreq);
					ServiceManager.getInstance().sendInvoke(packageName,
							"tool.fm.toFmFreq", json.toString().getBytes(), null);
				}
			});
			setFmFreqDistance(minVal, maxVal);
			return null;
			}catch(Exception e){
				e.printStackTrace();
			}
			
			try{
			JSONBuilder json = new JSONBuilder(new String(data));
			int minVal = json.getVal("minVal", Integer.class);
			int maxVal = json.getVal("maxVal", Integer.class);
			Boolean hasJump = json.getVal("hasJump", Boolean.class);
			if(hasJump != null && hasJump == true){
				int index = 0;
				mJumpsPoints.clear();
				while(true){
					Float fmVal = json.getVal("jump" + index, Float.class);
					if(fmVal == null){
						break;
					}
					
					index ++;
					mJumpsPoints.add(fmVal);
				}
			}else {
				mJumpsPoints.clear();
			}
			
			setFmTool(new FMTool() {
				@Override
				public void toFmFreq(float freq, int unit) {
					JSONBuilder json = new JSONBuilder();
					Float oFreq = freq;
					json.put("freqValue", oFreq);
					ServiceManager.getInstance().sendInvoke(packageName,
							"tool.fm.toFmFreq", json.toString().getBytes(), null);
				}
			});
			setFmFreqDistance(minVal, maxVal);
			return null;
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
		return null;
	}
    
	
	/**
	 * 调幅工具类
	 * 
	 * @author txz
	 *
	 */
	public static interface AMTool {
		/**
		 * 
		 * 
		 * @param freq频率
		 * @param unit 单位, 0 - HZ, 1-KHZ, 3-MHZ
		 * @return
		 */
		public void toAMFreq(int freq, int unit);
	}
	
	private AMTool mAmTool = null;
	public void setAmTool(AMTool amTool){
		mAmTool = amTool;
	}
	
	private void toAmFreq(byte[] data){
		if (data == null){
			return;
		}
		
		if (mAmTool == null){
			String spk = NativeData.getResString("RS_FM_NO_TOOL");
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		
		try {
			FMResultData fmResultData = FMResultData.parseFrom(data);
			String freqValue = fmResultData.strFreq;
			final int nFreq = Integer.parseInt(freqValue);
			if (nFreq > mMaxAmValue || nFreq < mMinAmValue){
				RecorderWin.speakTextWithClose("抱歉, 有效调幅范围是"+mMinAmValue+"至"+mMaxAmValue, null);
				return;
			}
			String spk = NativeData.getResPlaceholderString("RS_FM_AM_TO",
					"%CMD%", nFreq + "");
			RecorderWin.speakTextWithClose(spk,
					new Runnable() {
						@Override
						public void run() {
							if (mAmTool != null) {
								mAmTool.toAMFreq(nFreq, 2);
							}
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int mMinAmValue = 0;
	private int mMaxAmValue = 0;
	public void setAmFreqDistance(int min, int max){
		mMinAmValue = min;
		mMaxAmValue = max;
		FMSettingData fmSettingData = new FMSettingData();
		fmSettingData.uint32MinValue = min;
		fmSettingData.uint32MaxValue = max;
		JNIHelper.sendEvent(UiEvent.EVENT_CUSTOM_AM, FmData.SUBEVENT_FM_SETTING, fmSettingData);
	}
	
	public byte[] invokeTXZAM(final String packageName, String command,
			byte[] data) {
		if ("setdistance".equals(command)) {
			if (data == null) {
				return null;
			}
			try {
				String strJson = new String(data);
				JSONBuilder json = new JSONBuilder(strJson);
				int minValue = json.getVal("minVal", Integer.class);
				int maxValue = json.getVal("maxVal", Integer.class);
				setAmTool(new AMTool() {
					@Override
					public void toAMFreq(int freq, int unit) {
						JSONBuilder json = new JSONBuilder();
						Integer oFreq = freq;
						json.put("freqValue", oFreq);
						ServiceManager.getInstance().sendInvoke(packageName,
								"tool.am.toAmFreq", json.toString().getBytes(),
								null);

					}
				});
				setAmFreqDistance(minValue, maxValue);

			} catch (Exception e) {

			}

		}
		return null;
	}

}

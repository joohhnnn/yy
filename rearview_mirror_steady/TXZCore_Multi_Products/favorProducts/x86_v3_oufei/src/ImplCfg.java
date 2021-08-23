package com.txznet.txz.cfg;

/**
 * 实现类配置管理
 * 
 * @author User
 *
 */
public class ImplCfg {
	// static String mTtsClass =
	// "com.txznet.txz.component.tts.ifly.TtsIflyImpl";
	// static String mAsrClass =
	// "com.txznet.txz.component.asr.ifly.AsrIflyImpl";
	static String mTtsClass = "com.txznet.txz.component.tts.proxy.TtsBaiduFemaleImpl";
	static String mAsrClass = "com.txznet.txz.component.asr.mix.AsrMixImpl";
	//static String mWakeupClass = "com.txznet.txz.component.wakeup.yunzhishengremote.WakeupYunzhishengRemoteImpl";
	static String mWakeupClass = "com.txznet.txz.component.wakeup.yunzhisheng_3_0.WakeupYunzhishengImpl";
	static String mPreWakeupClass = "com.txznet.txz.component.wakeup.sence.WakeupSenceProxy";
	static String mCallCalss = "com.txznet.txz.component.call.dxwy.CallToolImpl";

	public static void setTtsImplClass(String c) {
		if (c.contains("yunzhisheng")) {
			// default;
		} else {
			mTtsClass = c;
		}
	}

	public static String getTtsImplClass() {
		return mTtsClass;
	}

	public static void setAsrImplClass(String c) {
		mAsrClass = c;
	}

	public static String getAsrImplClass() {
		return mAsrClass;
	}

	public static void setWakeupImplClass(String c) {
		mWakeupClass = c;
	}

	public static String getWakeupImpClass() {
		return mWakeupClass;
	}
	
	public static void setPreWakeupImplClass(String c) {
		mPreWakeupClass = c;
	}

	public static String getPreWakeupImpClass() {
		return mPreWakeupClass;
	}
	

	public static void setCallImplClass(String c) {
		mCallCalss = c;
	}

	public static String getCallImplClass() {
		return mCallCalss;
	}
}

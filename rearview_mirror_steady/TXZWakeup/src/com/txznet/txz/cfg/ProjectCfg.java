package com.txznet.txz.cfg;

import com.txznet.comm.remote.util.LogUtil;

public class ProjectCfg {
	public static boolean mEnableAEC = false;
	public static boolean mUseHQualityWakeupModel = false;
	
	static String mYunzhishengAppId ;
	static String mYunzhishengSecret ;
	static String mIflyAppId ;

	public static String getYunzhishengAppId() {
		return mYunzhishengAppId;
	}

	public static void setYunzhishengAppId(String mYunzhishengAppId) {
		ProjectCfg.mYunzhishengAppId = mYunzhishengAppId;
	}

	public static String getYunzhishengSecret() {
		return mYunzhishengSecret;
	}

	public static void setYunzhishengSecret(String mYunzhishengSecret) {
		ProjectCfg.mYunzhishengSecret = mYunzhishengSecret;
	}

	public static String getIflyAppId() {
		return mIflyAppId;
	}

	public static void setIflyAppId(String mIflyAppId) {
		ProjectCfg.mIflyAppId = mIflyAppId;
	}
	
	private static int sFilterNoiseType = 0;
	public static synchronized void setFilterNoiseType(int filterNoiseType){
		LogUtil.logd("setNoiseType = " +filterNoiseType);
		sFilterNoiseType = filterNoiseType;
		if (sFilterNoiseType != 0){
			mEnableAEC = true;
		}
	}
	public static synchronized int getFilterNoiseType(){
		return sFilterNoiseType;
	}
}


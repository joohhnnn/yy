package com.txznet.txz.module.activation;

import android.text.TextUtils;

import com.txznet.txz.jni.JNIHelper;

public class LocalActivationManager {
	private static LocalActivationManager instance = new LocalActivationManager();
	private IActivator mActivator = null;
	
	private LocalActivationManager(){
		mActivator = DBActivator.getInstance();
	}
	
	public static LocalActivationManager getInstance(){
		return instance;
	}
	
	public boolean checkActivationPermission(String sEncryptedDevSn){
		JNIHelper.logd("checkActivationPermission : " + sEncryptedDevSn);
		if (TextUtils.isEmpty(sEncryptedDevSn)){
			return false;
		}
		
		boolean bRet = false;
		if (mActivator != null){
			bRet = mActivator.checkPermission(sEncryptedDevSn);
		}
		return bRet;
	}
	
	public boolean isSupportLocalActivation(){
		boolean bRet = false;
		if (mActivator != null){
			bRet = mActivator.isSupportLocalActivation();
		}
		return bRet;
	}
}

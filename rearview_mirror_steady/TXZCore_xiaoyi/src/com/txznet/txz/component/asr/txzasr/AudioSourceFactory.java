package com.txznet.txz.component.asr.txzasr;

import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.tts.yunzhisheng_3_0.MSDAudioRecord;
import com.txznet.txz.component.tts.yunzhisheng_3_0.QZAudioRecord;
import com.txznet.txz.component.tts.yunzhisheng_3_0.RemoteAudioRecord;
import com.txznet.txz.component.tts.yunzhisheng_3_0.TxzAudioSourceImpl;
import com.txznet.txz.component.tts.yunzhisheng_3_0.TxzAudioSourceImplAEC;
import com.txznet.txz.jni.JNIHelper;
import com.unisound.client.IAudioSource;

public class AudioSourceFactory {
    public static IAudioSource createAudioSource(){
    	JNIHelper.logd("filterNoiseType = " + ProjectCfg.getFilterNoiseType());
    	IAudioSource mAudioSource = null;
    	if (ProjectCfg.getFilterNoiseType() == 2) {//单路输入设备回放回音消除方式
			mAudioSource = new TxzAudioSourceImplAEC(true);
			TxzAudioSourceImplAEC.setAudioRecord(new QZAudioRecord());
		} else if (ProjectCfg.getFilterNoiseType() == 1 || ProjectCfg.getFilterNoiseType() == 3) {//传统的双麦回音消除方式
			mAudioSource = new TxzAudioSourceImplAEC(ProjectCfg.getFilterNoiseType() == 1);
			if (ProjectCfg.isUseExtAudioSource()) {
				if (ProjectCfg.EXT_AUDIOSOURCE_TYPE_TXZ == ProjectCfg.extAudioSourceType()) {
				    TxzAudioSourceImplAEC.setAudioRecord(new RemoteAudioRecord());
				}else{
					TxzAudioSourceImplAEC.setAudioRecord(new MSDAudioRecord());
				}
			} 
		} else {//没有开启回音消除
			mAudioSource = new TxzAudioSourceImpl();
			if (ProjectCfg.isUseExtAudioSource()) {
				if (ProjectCfg.EXT_AUDIOSOURCE_TYPE_TXZ == ProjectCfg.extAudioSourceType()) {
					TxzAudioSourceImpl.setAudioRecord(new RemoteAudioRecord());
				} else {
					TxzAudioSourceImpl.setAudioRecord(new MSDAudioRecord());
				}
			}
		}
    	return mAudioSource;
    }
}

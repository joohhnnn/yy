package com.txznet.txz.component.asr.mix;

import com.txz.ui.equipment.UiEquipment;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.cfg.ProjectCfg;

public class VoicePreProcessorController {
    private static VoicePreProcessorController sInstance = new VoicePreProcessorController();

    public static VoicePreProcessorController getInstance() {
        return sInstance;
    }

    private int mServerConfigAECType = UiEquipment.VPT_DEFAULT;//服务器的配置类型
    private int mServerConfigSEype = UiEquipment.VPT_DEFAULT;////服务器的配置类型
    
    private int mUsedAECType = UiEquipment.VPT_DEFAULT;//实际使用的类型
    private int mUsedSEType = UiEquipment.VPT_DEFAULT;//实际使用的类型

    private VoicePreProcessorController() {

    }
    
    //设置服务配置的类型
    public void setPreAECType(int type) {
        LogUtil.logd("update AECPreProcessorType : " + type);
        mServerConfigAECType = type;
    }
    
  //获取服务配置的类型
    public int getPreAECType() {
        return mServerConfigAECType;
    }
   
    //设置服务配置的类型
    public void setPreSEType(int type) {
        LogUtil.logd("update SEPreProcessorType : " + type);
        mServerConfigSEype = type;
    }
    
  //获取服务配置的类型
    public int getPreSEType() {
        return mServerConfigSEype;
    }
    
  //记录实际使用的类型，实际初始化话引擎之后调用该接口
    public void setUsedAECType(int type) {
        LogUtil.logd("update AECPreProcessorType : " + type);
        mUsedAECType = type;
    }
    
  //获取实际使用的类型，实际初始化话引擎之后，获取才准确
    public int getUsedAECType() {
    	do{
    		int type = ProjectCfg.getFilterNoiseType();
    		//没有打开回音消除
    		if (type == 0){
    			mUsedAECType = UiEquipment.VPT_DEFAULT;
    			break;
    		}
    		//使用内部回音消除,即没有使用软算法AEC
    		if (type == 4){
    			mUsedAECType = UiEquipment.VPT_DEFAULT;
    			break;
    		}
    		
    		if (ImplCfg.useHobotAec()){
    			mUsedAECType = UiEquipment.VPT_AEC_HRSC;
    			break;
    		}
    		mUsedAECType = UiEquipment.VPT_AEC_YZS;
    	}while(false);
    	LogUtil.logd("UsedAECPreProcessorType : " + mUsedAECType);
        return mUsedAECType;
    }
    
  //记录实际使用的类型，实际初始化话引擎之后调用该接口
    public void setUsedSEType(int type) {
        LogUtil.logd("update SEPreProcessorType : " + type);
        mUsedSEType = type;
    }
    
  //获取实际使用的类型，实际初始化话引擎之后，获取才准确
    public int getUsedSEType() {
    	do{
    		int type = ProjectCfg.getFilterNoiseType();
    		//没有打开回音消除
    		if (type == 0){
    			mUsedSEType = UiEquipment.VPT_DEFAULT;
    			break;
    		}
    		//使用内部回音消除,即没有使用软算法AEC
    		if (type == 4){
    			mUsedSEType = UiEquipment.VPT_DEFAULT;
    			break;
    		}
    		
    		if (ImplCfg.useHobotAec()){
    			mUsedSEType = UiEquipment.VPT_SE_HRSC;
    			break;
    		}
    		mUsedSEType = UiEquipment.VPT_SE_YZS;
    	}while(false);
    	
    	LogUtil.logd("UsedSEPreProcessorType : " + mUsedSEType);
        return mUsedSEType;
    }
    
}

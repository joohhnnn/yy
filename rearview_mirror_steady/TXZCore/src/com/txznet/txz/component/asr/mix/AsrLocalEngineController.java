package com.txznet.txz.component.asr.mix;

import com.txz.ui.equipment.UiEquipment;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.cfg.ProjectCfg;

public class AsrLocalEngineController {
    private static AsrLocalEngineController sInstance = new AsrLocalEngineController();

    public static AsrLocalEngineController getInstance() {
        return sInstance;
    }
    
    private int mServerConfigType = UiEquipment.AET_DEFAULT;//服务器配置的类型
    
    private int mUsedType = UiEquipment.AET_YZS_FIX;//实际使用的类型
    
    private AsrLocalEngineController() {

    }
    
   //设置服务配置的类型
    public void setLocalAsrEngineType(int type) {
        LogUtil.logd("update LocalEngineType : " + type);
        mServerConfigType = type;
    }
    
    //获取服务配置的类型
    public int getLocalAsrEngineType() {
        return mServerConfigType;
    }
   
    //记录实际使用的类型，实际初始化话引擎之后调用该接口
    public void setUsedType(int type){
    	mUsedType = type;
    }
   
    //获取实际使用的类型，实际初始化话引擎之后，获取才准确
    public int getUsedType(){
    	do{
    		//使用声瀚离线识别的配置满足
    		if (ImplCfg.useUVoiceAsr()){
    			mUsedType = UiEquipment.AET_UVOICE_FIX;
    			break;
    		}
    		
    		mUsedType = UiEquipment.AET_YZS_FIX;
    	}while(false);
    	
    	return mUsedType;
    }

}

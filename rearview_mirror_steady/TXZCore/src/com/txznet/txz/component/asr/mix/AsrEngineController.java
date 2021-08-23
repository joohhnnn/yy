package com.txznet.txz.component.asr.mix;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import android.text.TextUtils;

import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;

public class AsrEngineController {
	private static AsrEngineController sIntance = new AsrEngineController();
	private AsrEngine sMainEngine = null;
	private List<AsrEngine> sBakEngineQueue = null;
	private ErrorConfig mErrorConfig = null;
	public static enum NetResultType{
		NETRESULT_TYPE_SUCCESS,//在线识别成功获取到结果
		NETRESULT_TYPE_FAIL,//在线识别出错
		NETRESULT_TYPE_NONE//在线识别无任何返回结果
	}
	
	public static enum AsrResultType{
		ASRRESULT_TYPE_NET,
		ASRRESULT_TYPE_LOCAL,
		ASRRESULT_TYPE_UNKONW
	}
	
	public static class ErrorConfig{
		private final static int DEFAULT_ERROR_MAX_CNT = 3;
		private int mErrorMaxCnt = DEFAULT_ERROR_MAX_CNT;
		private int mRestoreCnt = DEFAULT_ERROR_MAX_CNT;
		public int getErrorMaxCnt(){
			return mErrorMaxCnt > 0 ? mErrorMaxCnt : DEFAULT_ERROR_MAX_CNT;
		}
		
		public int getRestoreCnt(){
			return mRestoreCnt > 0 ? mRestoreCnt : DEFAULT_ERROR_MAX_CNT;
		}
		
		//内部类才可以调用，避免被其他地方不小心更改了
		private void setErrorMaxCnt(int cnt){
			if (cnt > 0 && cnt < 30){
				LogUtil.logd("setErrorMaxCnt : " + cnt);
				mErrorMaxCnt = cnt;
			}
		}
		
		//内部类才可以调用，避免被其他地方不小心更改了
		private void setRestoreCnt(int cnt){
			if (cnt > 0 && cnt < 30){
				LogUtil.logd("setRestoreCnt : " + cnt);
				mRestoreCnt = cnt;
			}
		}
	}
	
	public static class ExtraVoiceResult{
		AsrResultType mAsrResultType = AsrResultType.ASRRESULT_TYPE_UNKONW;
		NetResultType mNetResultType = NetResultType.NETRESULT_TYPE_NONE;
		int mNetErrorCode = IAsr.ERROR_SUCCESS;
	}
	
	public static class AsrEngine{
		private int mEngineType = UiEquipment.AET_DEFAULT;
		private String mEngineName = "";
		
		public AsrEngine(int type, String name){
			mEngineType = type;
			mEngineName = name;
		}
		
		public int getType(){
			return mEngineType;
		}
		
		public String getName(){
			return mEngineName;
		}
	}
	
	private AsrEngineController(){
		mErrorConfig = new ErrorConfig();
		buildBakEngineQueue();
	}
	
	public static AsrEngineController getIntance(){
		return sIntance;
	}
	
	public synchronized AsrEngine getMainEngine(){
		return sMainEngine;
	}
	
	public ErrorConfig getErrorConfig(){
		return mErrorConfig;
	}
	
	public int getOldMainEngineType(){
		int type = UiEquipment.AET_YZS;
		if (!TextUtils.isEmpty(ProjectCfg.getTencentAppkey()) && !TextUtils.isEmpty(ProjectCfg.getTencentToken())) {
			type = UiEquipment.AET_TENCENT;
		} 
		return type;
	}
	
	public synchronized void setMainEngine(int type){
		if (!isUsedOnlineEngine(type)){
			type = UiEquipment.AET_YZS;
		}
		LogUtil.logd("update mainEngineType : " + type + ", mainEngineName : " + getEngineName(type));
		if (sMainEngine != null){
			LogUtil.logw("main engine has already been set, no support set again");
			return;
		}
		
		sMainEngine = new AsrEngine(type, getEngineName(type));
		//删除与主引擎一样的备用引擎
		Iterator<AsrEngine> it = sBakEngineQueue.iterator();
		while(it.hasNext()){
			AsrEngine engine = it.next();
			if (engine.getType() == sMainEngine.getType()){
				it.remove();
			}
		}
		LogUtil.logd("actual mainEngineType : " + sMainEngine.getType() + ", mainEngineName : " + sMainEngine.getName());
	}
	
	public static boolean isValidAsrError(int errorCode){
		boolean bValid = false;
		switch(errorCode){
		case IAsr.ERROR_ASR_NET_REQUEST:
		case IAsr.ERROR_ASR_NET_NLU_EMTPY:
		case IAsr.ERROR_ABORT:
		case IAsr.ERROR_CODE:
			bValid = true;
			break;
		default:
			break;
		}
		return bValid;
	}
	
	public static ExtraVoiceResult parseExtraVoiceResult(VoiceParseData oVoiceParseData){
		ExtraVoiceResult oResult = new ExtraVoiceResult();
		if (oVoiceParseData != null){
			switch(oVoiceParseData.uint32DataType){
			case VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON:
				oResult.mAsrResultType = AsrResultType.ASRRESULT_TYPE_LOCAL;//离线识别结果
				break;
			case VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON:
			case VoiceData.VOICE_DATA_TYPE_BAIDU_SCENE_JSON:
			case VoiceData.VOICE_DATA_TYPE_TENCENT_SCENE_JSON:
			case VoiceData.VOICE_DATA_TYPE_SENCE_JSON://科大讯飞的在线识别结果
				oResult.mAsrResultType = AsrResultType.ASRRESULT_TYPE_NET;//在线识别结果
				break;
			default:	
			}
		}
		
		if (oResult.mAsrResultType == AsrResultType.ASRRESULT_TYPE_NET){
			oResult.mNetResultType = NetResultType.NETRESULT_TYPE_SUCCESS;
			return oResult;
		}
		
		if (oResult.mAsrResultType == AsrResultType.ASRRESULT_TYPE_LOCAL){
			if (oVoiceParseData.strExtraData != null) {
				String strExtraData = "";
				strExtraData = new String(oVoiceParseData.strExtraData);
				int errorCode = IAsr.ERROR_SUCCESS;
				JSONObject jsonObject = null;
				try {
					jsonObject = new JSONObject(strExtraData);
					errorCode = jsonObject.getInt("netErrorCode");
				} catch (Exception e) {

				}
				// 在线识别出错了
				if (errorCode != IAsr.ERROR_SUCCESS) {
					oResult.mNetResultType = NetResultType.NETRESULT_TYPE_FAIL; 
					oResult.mNetErrorCode = errorCode;
					return oResult;
				}
			}
		}
		return oResult;
	}
	
	public boolean isUsedOnlineEngine(int type){
		boolean bUsed = false;
		switch(type){
		case UiEquipment.AET_BAIDU:
		//case UiEquipment.AET_IFLY:
		case UiEquipment.AET_YZS:
		case UiEquipment.AET_TENCENT:
			bUsed = true;
			break;
		default:
			bUsed = false;
			LogUtil.logd("other engine type : " + type);
		}
		return bUsed;
	}
	
	public boolean isUsedOfflineEngine(int type){
		boolean bUsed = false;
		switch(type){
		case UiEquipment.AET_YZS_FIX:
		case UiEquipment.AET_PACHIRA_FIX:
			bUsed = true;
			break;
		default:
			bUsed = false;
			LogUtil.logd("other engine type : " + type);
		}
		return bUsed;
	}
	
	
	public synchronized AsrEngine top(){
		if (sBakEngineQueue.isEmpty()){
			return null;
		}
		AsrEngine engine = sBakEngineQueue.get(0);
		sBakEngineQueue.remove(0);
		printBakEngineInfo();
		return engine;
	}
	
	public synchronized void push_back(AsrEngine engine){
		if (engine != null){
			sBakEngineQueue.add(engine);
		}
		//printBakEngineInfo();
	}
	
	public synchronized void push_top(AsrEngine engine){
		if (engine != null){
			sBakEngineQueue.add(0, engine);
		}
		//printBakEngineInfo();
	}
	
	public synchronized void updateBakEngines(int[] engines){
		if (engines != null && engines.length > 0){
			int cnt = 0;
			for (int i = 0; i < engines.length; ++i){
				if (refreshQueue(cnt, engines[i])){
					++cnt;
				};
			}
		}
		printBakEngineInfo();
	}
	
	private void printBakEngineInfo(){
		LogUtil.logd("printBakEngineInfo");
		for (int i = 0; i < sBakEngineQueue.size(); ++i){
			AsrEngine engine = sBakEngineQueue.get(i);
			LogUtil.logd("BakEnginesQueue " + engine.getType() + ", "  + engine.getName());
		}
	}
	
	private boolean refreshQueue(int index, int type){
		for(int i = index; i < sBakEngineQueue.size(); ++i){
			AsrEngine engine = sBakEngineQueue.get(i);
			if (engine.getType() == type){
				sBakEngineQueue.remove(i);
				sBakEngineQueue.add(index, engine);
				return true;
			}
		}
		return false;
	}
	
	public static String getEngineName(int type){
		String strName = "unkown";
		switch(type){
		case UiEquipment.AET_YZS:
			strName = "unisound";
			break;
		case UiEquipment.AET_IFLY:
			strName = "iflyteck";
			break;
		case UiEquipment.AET_TENCENT:
			strName = "tencent";
			break;
		case UiEquipment.AET_BAIDU:
			strName = "baidu";
			break;
		case UiEquipment.AET_PACHIRA:
			strName = "pachira";
			break;
		default:
			break;
		}
		return strName;
	}
	
	//新接入的在线识别引擎，需要按如下格式, add到备用queue中
	private void buildBakEngineQueue(){
		sBakEngineQueue = new LinkedList<AsrEngine>();
		
		//add云知声在线识别引擎
		{
			AsrEngine engine = new AsrEngine(UiEquipment.AET_YZS, getEngineName(UiEquipment.AET_YZS));
			sBakEngineQueue.add(engine);
		}
		
		//add科大讯飞在线识别引擎
//		{
//			AsrEngine engine = new AsrEngine(UiEquipment.AET_IFLY, getEngineName(UiEquipment.AET_IFLY));
//			sBakEngineQueue.add(engine);
//		}
		
		//add腾讯在线识别引擎
		{
			AsrEngine engine = new AsrEngine(UiEquipment.AET_TENCENT, getEngineName(UiEquipment.AET_TENCENT));
			sBakEngineQueue.add(engine);
		}
		
		//add百度在线识别引擎
		{
			AsrEngine engine = new AsrEngine(UiEquipment.AET_BAIDU, getEngineName(UiEquipment.AET_BAIDU));
			sBakEngineQueue.add(engine);
		}
		
	}
	
	public void parseAsrEngineParams(UiEquipment.AsrEngineParams pbAsrParams){
		if (pbAsrParams.uint32MainMaxErrCnt != null){
			getErrorConfig().setErrorMaxCnt(pbAsrParams.uint32MainMaxErrCnt);
		}
		
		if (pbAsrParams.uint32BakMaxErrCnt != null){
			
		}
		
		if (pbAsrParams.uint32MainRestoreCnt != null){
			getErrorConfig().setRestoreCnt(pbAsrParams.uint32MainRestoreCnt);
		}
		
		if (pbAsrParams.strIflyAppId != null){
			String strIflyAppId = byte2str(pbAsrParams.strIflyAppId);
			if (!TextUtils.isEmpty(strIflyAppId)){
				ProjectCfg.setIflyAppId(strIflyAppId);
				printSimpleInfo("iflyteck_id", strIflyAppId);
			}
		}
		
		if (pbAsrParams.strYzsAppId != null && pbAsrParams.strYzsSecrectKey != null){
			String strYzsAppId = byte2str(pbAsrParams.strYzsAppId);
			String strYzsSecrectKey = byte2str(pbAsrParams.strYzsSecrectKey);
			if (!TextUtils.isEmpty(strYzsAppId) && !TextUtils.isEmpty(strYzsSecrectKey)){
				ProjectCfg.setYunzhishengAppId(strYzsAppId);
				ProjectCfg.setYunzhishengSecret(strYzsSecrectKey);
				printSimpleInfo("yzs_id", strYzsAppId);
				printSimpleInfo("yzs_secet", strYzsSecrectKey);
			}
		}
		
		if (pbAsrParams.strTencentAppId != null && pbAsrParams.strTencentAppToken != null){
			String strTencentAppid = byte2str(pbAsrParams.strTencentAppId);
			String strTencentAppToken = byte2str(pbAsrParams.strTencentAppToken);
			if (!TextUtils.isEmpty(strTencentAppid) && !TextUtils.isEmpty(strTencentAppToken)){
				ProjectCfg.setTencentAppkey(strTencentAppid);
				ProjectCfg.setTencentToken(strTencentAppToken);
				printSimpleInfo("tencent_id", strTencentAppid);
				printSimpleInfo("tencent_token", strTencentAppToken);
			}
		}
		
		if (pbAsrParams.strBaiduAppid != null && pbAsrParams.strBaiduAk != null && pbAsrParams.strBaiduSk != null){
			String strBaiduAppid = byte2str(pbAsrParams.strBaiduAppid);
			String strBaiduAk = byte2str(pbAsrParams.strBaiduAk);
			String strBaiduSk = byte2str(pbAsrParams.strBaiduSk);
			if (!TextUtils.isEmpty(strBaiduAppid) && !TextUtils.isEmpty(strBaiduAk) && !TextUtils.isEmpty(strBaiduSk)){
				ProjectCfg.setBaiduAppid(strBaiduAppid);
				ProjectCfg.setBaiduApikey(strBaiduAk);
				ProjectCfg.setBaiduSecret(strBaiduSk);
				printSimpleInfo("baidu_id", strBaiduAppid);
				printSimpleInfo("baidu_ak", strBaiduAk);
				printSimpleInfo("baidu_sk", strBaiduSk);
				
			}
			
		}
	}
	
	public String byte2str(byte[] data) {
		String str = null;
		if (data == null){
			return str;
		}
		
		try {
			str = new String(data);
		} catch (Exception e) {

		}
		return str;
	}
	
	public void printSimpleInfo(String tag, String info){
		String strSimpleInfo = "";
		if (TextUtils.isEmpty(info) || info.length() < 5){
			strSimpleInfo = "...";
		}else{
			strSimpleInfo = info.substring(0, 3) + "...";
		}
		LogUtil.logd("" + tag + ":" + strSimpleInfo);
	}
	
}

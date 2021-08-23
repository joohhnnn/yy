package com.txznet.txz.component.nav.gaode;

import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.txz.ui.map.UiMap;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.StatusUtil;
import com.txznet.comm.remote.util.StatusUtil.StatusListener;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZTtsManager.ITtsCallback;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.component.choice.list.PoiWorkChoice;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.base.BasePathInfo;
import com.txznet.txz.component.nav.base.BaseRoadInfo;
import com.txznet.txz.component.nav.base.SelectAsr;
import com.txznet.txz.component.nav.gaode.NavAmapControl.IAmapNavContants;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.WayPoiData.WayPoi;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.nav.tool.NavAppManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.StatusObervable.StatusObserver;
import com.txznet.txz.util.IntentUtil;
import com.txznet.txz.util.TXZHandler;
import com.txznet.txz.util.runnables.Runnable1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class NavAmapValueService implements IAmapNavContants {
	public static final String TASK_RECV_PLAN_ID = "TASK_RECV_PLAN_ID";
	public static final String TASK_CONTINUE_NAVI_ID = "TASK_CONTINUE_NAVI_ID";
	public static final String TASK_PLAN_FAIL = "TASK_PLAN_FAIL";
	public static final String TASK_PARKER_ID = "TASK_PARKER_ID";
	public static final String TASK_COMMUTING_NAVI_ID = "TASK_CONTINUE_NAVI_ID";	
	public static final String TASK_AVOID_TRAFFIC_ID = "TASK_AVOID_TRAFFIC_ID";	
	public static final String TASK_REPORT_TRAFFIC_ID = "TASK_REPORT_TRAFFIC_ID";	
	public static final String TASK_SELECT_NAVIGATE_ROAD = "TASK_SELECT_NAVIGATE_ROAD";	
	
	int sSpeechId = TtsManager.INVALID_TTS_TASK_ID;
	Map<String, SelectAsr> mAsrMap = new HashMap<String, SelectAsr>();
	public RoadInfo mRoadInfo;
//	private boolean mIsEnableWakeup = true;
//	private boolean mIsSpkConNavInfo = true;
	private boolean mIsApkIsAlive = false;

	private List<Poi> mTjPoiList = new ArrayList<Poi>();
	private static NavAmapValueService sService = new NavAmapValueService();
	private boolean mIsInAsrWorking =false;
	private boolean mIsInTtsWorking =false;
	private boolean mIsInNaving = false;
	private NavAmapValueService() {
		StatusUtil.addStatusListener(new StatusListener() {
			
			@Override
			public void onMusicPlay() {
			}
			
			@Override
			public void onMusicPause() {
			}
			
			@Override
			public void onEndTts() {
				mIsInTtsWorking=false;
				AppLogic.runOnBackGround(new Runnable() {
					
					@Override
					public void run() {
						dealDelayTtsRunnable();
					}
				},200);
			}
			
			@Override
			public void onEndCall() {
			}
			
			@Override
			public void onEndAsr() {
				mIsInAsrWorking = false;
				AppLogic.runOnBackGround(new Runnable() {
					
					@Override
					public void run() {
						dealDelayTtsRunnable();
					}
				},200);
			}
			
			@Override
			public void onBeginTts() {
				mIsInTtsWorking=true;
			}
			
			@Override
			public void onBeginCall() {
			}
			
			@Override
			public void onBeginAsr() {
				mIsInAsrWorking = true;

			}
			
			@Override
			public void onBeepEnd() {

			}
		});
		RecorderWin.OBSERVABLE.registerObserver( new StatusObserver() {
			@Override
			public void onShow() {
			}
			@Override
			public void onDismiss() {
				if(isReporting){
					reporTraffic(0);
				}
			}
		});
	}
	
	private void dealDelayTtsRunnable(){
		if( !mIsInAsrWorking && !mIsInTtsWorking && mTtsDelayRun != null){
			AppLogic.runOnBackGround(mTtsDelayRun, 0);
			mTtsDelayRun = null;
		}
	}
	
	private boolean processBySence(int keyType) {
		JSONBuilder jb = new JSONBuilder();
		jb.put("scene", "nav");
		jb.put("action", "amapRecv");
		jb.put("key", keyType);
		if (SenceManager.getInstance().noneedProcSence("nav", jb.toBytes())) {
			return true;
		}
		return false;
	}

	public static NavAmapValueService getInstance() {
		return sService;
	}

//	public void enableWakeup(boolean enableWakeup) {
//		mIsEnableWakeup = enableWakeup;
//	}
//	public void enableSpkConNavInfo(boolean enableSpk) {
//		mIsSpkConNavInfo = enableSpk;
//	}
	
	public void setNaving(boolean isNaving) {
		mIsInNaving = isNaving;
	}

	public void notifyReceive(Intent intent) {
		int keyType = intent.getIntExtra("KEY_TYPE", -1);
		JNIHelper.logd("recv intent= " + intent.toString());
		if (intent.getExtras() != null) {
			JNIHelper.logd("recv intent Extra= " + intent.getExtras().toString());
		}
		doNavAmapRecv(intent, keyType);
	}
	public void clearAllJingYou(){
		mTjPoiList.clear();
	}
	private boolean mIsPlaning = false;
	private void doNavAmapRecv(Intent intent, int key_type) {
		if(key_type == 10019){
			mIsApkIsAlive=true;
			int extraState = intent.getIntExtra("EXTRA_STATE",-1) ;
			//高德地图弹出路线规划界面会发送该事件
			if(extraState == 103){
				mIsPlaning = true;
			}
		}
		if (key_type == 10042) { // 离线搜索返回
			PoiQuery.getInstance().onResult(intent.getStringExtra("EXTRA_RESULT"),10023);
		}

		if (key_type == 10043) { // 离线搜索返回
			PoiQuery.getInstance().onResult(intent.getStringExtra("EXTRA_RESULT"),10024);
		}

		if (key_type == 10046) { // 通知家和公司的地址
			doKeyType10046(intent);
		}

		if (key_type == 10049 && NavAppManager.getInstance().enableWakeup() && getEnableAutoPopUp()) { // 续航信息	
			if (processBySence(key_type)) {
				return;
			}	
			doKeyType10049(intent);	
		}

		if (key_type == 10050 && NavAppManager.getInstance().enableWakeup()) {// 手机发送的位置信息导航
			if (processBySence(key_type)) {
				return;
			}
			if (!NavManager.getInstance().isGaodeMapNavPhone()){
				return;
			}
			doKeyType10050(intent);
		}

		if (key_type == 10051 && NavAppManager.getInstance().enableWakeup()) { // 规划失败
			doKeyType10051(intent);
		}

		if (key_type == 10052 && NavAppManager.getInstance().enableWakeup()) { // 停车场
			doKeyType10052(intent);
		}

		if (key_type == 10056) { // 当前路线信息
			taskRunnable.update(intent);
			AppLogic.removeBackGroundCallback(taskRunnable);
			AppLogic.runOnBackGround(taskRunnable, 500);
			return;
		}

		if (key_type == 10057) { // 沿途搜索数据
			String jsonResult = intent.getStringExtra("EXTRA_SEARCH_ALONG_THE_WAY");
			PoiQuery.getInstance().onResult(intent.getStringExtra("EXTRA_SEARCH_ALONG_THE_WAY"),10057);
		}

		if (key_type == 10059) {
			doKeyType10059(intent);
		}

		/*
		 * 注释原因：高德在收到该指令后会自行播报
		 */
//		if( key_type == 10109){//前方路况
//			doKeyType10109(intent);
//		}

		if(key_type == 11002){//导航去收藏点
            doKeyType11002(intent);
        }

		if(key_type == 12402){//路况查询
			doKeyType12402(intent);
		}
		if(key_type == 12004 && getEnableAutoPopUp()){//通勤信息透出
			doKeyType12004(intent);
		}
		if(key_type == 12105){//是否重新规划路线躲避拥堵
			doKeyType12105(intent);
		}
		if(key_type == 12106){//拥堵弹窗的处理反馈
			if(mAvoidTrafficSelectAsr != null){
				mAvoidTrafficSelectAsr.destory();
				mAvoidTrafficSelectAsr = null;
			}
		}
		if(key_type == 12003){//通勤信息的处理反馈
			doKeyType12003(intent);
		}
		if(key_type == 12108){//上报界面的出现和上报结果的反馈
			doKeyType12108(intent);
		}
		if(key_type == 12101){//屏幕触摸事件的通知
			doKeyType12101(intent);
		}

		if(key_type == 12102){//导航中途经点信息透出（发起导航或者更新路线的时候，高德会发送途经点信息给第三方）
			doKeyType12102(intent);
		}
		
		//		if(key_type == 12012){//刷新路线
//			doKeyType12012(intent);
//		}

		if (key_type == 12013) {//主辅路切换结果返回
			doKeyType12013(intent);
		}
	}

	Runnable1<Intent> taskRunnable = new Runnable1<Intent>(null) {
		@Override
		public void run() {
			if (mP1 == null) {
				return;
			}
			doKeyType10056(mP1);
		}
	};

	/**
	 * 取消TTS播报
	 */
	public void cancelTts() {
		if (sSpeechId != TtsManager.INVALID_TTS_TASK_ID) {
			TtsManager.getInstance().cancelSpeak(sSpeechId);
		}
	}

	private void doKeyType12003(Intent intent) {
		int intExtra = intent.getIntExtra("EXTRA_MESSAGE_TYPE", -1);
		if(intExtra == 4 || intExtra == 2){
			if(mTimeOut != null){
				AppLogic.removeBackGroundCallback(mTimeOut);
			}
			//取消续航
			if(intExtra == 4){
				AppLogic.removeBackGroundCallback(mRegContRunnable);
				AppLogic.removeBackGroundCallback(mCommutingRunnable);
				AppLogic.removeBackGroundCallback(mRegContRunTask);
			}
			destoryAllSelectAsrs();
			if(mDelayRun != null){
				mNowDelayRun = mDelayRun;
				mDelayRun = null;
				AppLogic.runOnBackGround(mNowDelayRun, 1000);
			}else{
				mNowDelayRun = null;
			}
		}
	}
	Runnable1<Intent> mCommutingRunTask = null;
	private void doKeyType12004(Intent intent) {
		JNIHelper.logd("zsbin doKeyType12004");
		if(mCommutingRunTask != null){
			AppLogic.removeBackGroundCallback(mCommutingRunTask);
		}		
		mCommutingRunTask = new Runnable1<Intent>(intent) {
			
			@Override
			public void run() {
				JNIHelper.logd("zsbin doKeyType12004 mRunTask run ");
				AppLogic.removeBackGroundCallback(mCommutingRunnable);
				mCommutingRunnable.update(mP1);
				boolean isTop =mP1.getBooleanExtra("EXTRA_MESSAGE_IS_TOP", false);
				dealNavPopup(mCommutingRunnable, isTop,0);			
			}
		};
		AppLogic.runOnBackGround(mCommutingRunTask, 3000);
		
	}
	private void doKeyType12105(Intent intent){
		AppLogic.removeBackGroundCallback(mAvoidTrafficRunnable);
		mAvoidTrafficRunnable.update(intent);
		AppLogic.runOnBackGround(mAvoidTrafficRunnable, 0);		
	}
	private void startCommutingNav(boolean isCompany){
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 12004);
		intent.putExtra("EXTRA_HOME_OR_COMPANY_WHAT", true);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
		TtsManager.getInstance().cancelSpeak(sSpeechId);
		String target = isCompany?NativeData.getResString("RS_MAP_COMMUTING_TO_HOME"):
			NativeData.getResString("RS_MAP_COMMUTING_TO_COMPANY");
		String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMUTING").replace("%CMD%", target);
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(text, null);
	}
	private void cancelCommutingNav(boolean spk){
		JNIHelper.logd("zsbin doKeyType12004 cancelCommutingNav");
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 12004);
		intent.putExtra("EXTRA_HOME_OR_COMPANY_WHAT", false);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
		if (spk) {
			String cancel = NativeData.getResString("RS_MAP_CONFIRM_CANCEL_ASR");
			String text = NativeData.getResString("RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_CANCEL_COMMUTING_NAV").replace("%CMD%", cancel);
			AsrManager.getInstance().setNeedCloseRecord(true);
			TtsManager.getInstance().cancelSpeak(sSpeechId);
			RecorderWin.speakTextWithClose(text, null);
		}
	}
//	private boolean mIsInRoadTraffic = false;
//	public void setIsInRoadTraffic(boolean inRoadTraffic){
//		mIsInRoadTraffic = inRoadTraffic;
	private boolean mIsEnableAutoPopUp = true;
	public void setEnableAutoPupUp(boolean enable){
		mIsEnableAutoPopUp = enable;
		if( !enable ){
			cancelAutoPopUp(true);
		}
	}
	
	public void setTrafficSearchEnableAutoPupUp(boolean enable) {
		mIsEnableAutoPopUp = enable;
		if (!enable) {
			cancelAutoPopUp(false);
		}
	}
	
	public boolean getEnableAutoPopUp(){
		return mIsEnableAutoPopUp;
	}
	Runnable1<Intent> mCommutingRunnable = new Runnable1<Intent>(null) {

		@Override
		public void run() {
			JNIHelper.logd("zsbin doKeyType12004 mCommutingRunnable run ");
			if (mP1 == null || !getEnableAutoPopUp()) {
				return;
			}
			
			final boolean isCompany = mP1.getBooleanExtra("EXTRA_HOME_OR_COMPANY_WHAT", false);
			String timeInfo = mP1.getStringExtra("EXTRA_HOME_OR_COMPANY_ETA");
			JNIHelper.logd("Commuting:timeInfo = "+timeInfo);
			JNIHelper.logd("Commuting:isCompany = "+isCompany);
			if(TextUtils.isEmpty(timeInfo)){
				JNIHelper.logd("Commuting:timeInfo can't null.");
				return;
			}
			String target = null;

			final SelectAsr mSelectAsr = createSelectAsrById(TASK_COMMUTING_NAVI_ID);
			mSelectAsr.addCmds("SURE", new Runnable() {

				@Override
				public void run() {
					startCommutingNav(isCompany);
				}
			}, getResCmdsById("RS_NAV_GD_NAV_SURE"));
			mSelectAsr.addCmds("CANCEL", new Runnable() {

				@Override
				public void run() {
					cancelCommutingNav(true);
				}
			}, getResCmdsById("RS_NAV_GD_CONT_CANCEL"));
			
			mTimeOut = new Runnable1<Boolean>(false) {
				
				@Override
				public void run() {
					mTimeOut = null;
					mNowDelayRun = null;
					cancelCommutingNav(false);
				}
			};
			TtsManager.getInstance().cancelSpeak(sSpeechId);
			if(isCompany){
				target = NativeData.getResString("RS_MAP_COMMUTING_TO_HOME");
			}else{		
				target = NativeData.getResString("RS_MAP_COMMUTING_TO_COMPANY");
			}
			final String spk = NativeData.getResPlaceholderString("RS_MAP_COMMUTING", "%TARGER%", target).replace("%TIME%", timeInfo);
			JNIHelper.logd("Commuting:timeInfo = "+spk);
			if(mIsInAsrWorking || mIsInTtsWorking){
				mTtsDelayRun = new Runnable() {
					
					@Override
					public void run() {
						mSelectAsr.build();
						sSpeechId = TtsManager.getInstance().speakVoice(spk, TtsManager.BEEP_VOICE_URL,new ITtsCallback() {
							@Override
							public void onSuccess() {
								AppLogic.runOnBackGround(mTimeOut,6000);
							}
						});
					}
				};				
			}else{
				mSelectAsr.build();
				sSpeechId = TtsManager.getInstance().speakVoice(spk, TtsManager.BEEP_VOICE_URL,new ITtsCallback() {
					@Override
					public void onSuccess() {
						AppLogic.runOnBackGround(mTimeOut,6000);
					}
				});
			}


		}
	};
	private void startAvoidTraffic(){
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 12106);
		intent.putExtra("EXTRA_AVOID_TRAFFIC_JAM_CONTROL", true);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
		TtsManager.getInstance().cancelSpeak(sSpeechId);
		String text = NativeData.getResString("RS_VOICE_WILL_DO_AVOIDTRAFFIC");
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(text, null);
	}
	private void cancelAvoidTraffic(boolean spk){
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 12106);
		intent.putExtra("EXTRA_AVOID_TRAFFIC_JAM_CONTROL", false);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
		TtsManager.getInstance().cancelSpeak(sSpeechId);
		if (spk) {
			String text = NativeData.getResString("RS_VOICE_WILL_DONOT_AVOIDTRAFFIC");
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(text, null);
		}
	}
	SelectAsr  mAvoidTrafficSelectAsr =null;
	Runnable1<Intent> mAvoidTrafficRunnable = new Runnable1<Intent>(null) {

		@Override
		public void run() {
			if (mP1 == null && getEnableAutoPopUp()) {
				cancelAvoidTraffic(false);
				return;
			}
			
			String timeInfo = mP1.getStringExtra("EXTRA_AVOID_TRAFFIC_JAM_MESSAGE");
			if(!TextUtils.isEmpty(timeInfo)){
				timeInfo.replace("躲避拥堵", "");
			}
			JNIHelper.logd("Commuting:timeInfo = "+timeInfo);

			final SelectAsr mSelectAsr = createSelectAsrById(TASK_AVOID_TRAFFIC_ID);
			mSelectAsr.addCmds("SURE", new Runnable() {

				@Override
				public void run() {
					startAvoidTraffic();
					mAvoidTrafficSelectAsr = null;
				}
			}, getResCmdsById("RS_NAV_GD_AVOID_SURE"));
			mSelectAsr.addCmds("CANCEL", new Runnable() {

				@Override
				public void run() {
					cancelAvoidTraffic(true);
					mAvoidTrafficSelectAsr = null;
				}
			}, getResCmdsById("RS_NAV_GD_AVOID_CANCEL"));
			mSelectAsr.build();
			mAvoidTrafficSelectAsr = mSelectAsr;
			TtsManager.getInstance().cancelSpeak(sSpeechId);
			String spk = NativeData.getResPlaceholderString("RS_MAP_AVOID_TRAFFIC", "%TIME%", timeInfo);
			JNIHelper.logd("Commuting:timeInfo = "+spk);
			sSpeechId = TtsManager.getInstance().speakVoice(spk, TtsManager.BEEP_VOICE_URL);
		}
	};
	private void doKeyType12402(Intent intent) {
		String resultMessage="";
		int resultCondition=0;
		resultCondition= intent.getIntExtra("EXTRA_TRAFFIC_CONDITION_RESULT", 0);
		resultMessage=intent.getStringExtra("EXTRA_TRAFFIC_CONDITION_RESULT_MESSAGE");
		JSONBuilder json = new JSONBuilder();
		json.put("code", resultCondition);
		json.put("text", resultMessage);
		PoiQuery.getInstance().onResult(json.build().toString(),12401);
	}

	private void doKeyType10046(Intent intent) {
		int category = 0;
		String lat = "";
		String lng = "";
		String addr = "";
		String poiName = "";
		try {
			category = intent.getIntExtra("CATEGORY", -1);
			lat = intent.getStringExtra("LAT");
			lng = intent.getStringExtra("LON");
			addr = intent.getStringExtra("ADDRESS");
			poiName = intent.getStringExtra("POINAME");
		} catch (Exception e1) {
		}

		if (TextUtils.isEmpty(lat) || TextUtils.isEmpty(lng) || TextUtils.isEmpty(addr) || TextUtils.isEmpty(poiName)) {
			Bundle b = intent.getExtras();
			category = b.getInt("CATEGORY", -1);
			try {
				lat = b.getString("LAT");
				lng = b.getString("LON");
			} catch (Exception e) {
			}
			if (TextUtils.isEmpty(lat) || TextUtils.isEmpty(lng)) {
				lat = String.valueOf(b.getDouble("LAT"));
				lng = String.valueOf(b.getDouble("LON"));
			}
			addr = b.getString("ADDRESS");
			poiName = b.getString("POINAME");
		}
		JNIHelper.logd("recv navi :" + category + "," + lat + "," + lng + "," + addr + "," + poiName);

		if (!isLegalGPS(lat, lng)) {
			if (category == 1) {
				NavManager.getInstance().clearHomeLocation();
			}
			if (category == 2) {
				NavManager.getInstance().clearCompanyLocation();
			}
			return;
		}

		double sLat = -1;
		double sLng = -1;
		try {
			sLat = Double.parseDouble(lat);
			sLng = Double.parseDouble(lng);
		} catch (Exception e) {
		}
		if (category == 1) { // 家
			NavManager.getInstance().setHomeLocation(poiName, addr, sLat, sLng, UiMap.GPS_TYPE_GCJ02, false);
		} else if (category == 2) { // 公司
			NavManager.getInstance().setCompanyLocation(poiName, addr, sLat, sLng, UiMap.GPS_TYPE_GCJ02, false);
		}
	}

	private boolean isLegalGPS(String lat, String lng) {
		if (TextUtils.isEmpty(lat) || TextUtils.isEmpty(lng)) {
			return false;
		}

		try {
			Double dLat = Double.parseDouble(lat);
			Double dLng = Double.parseDouble(lng);
			if (dLat == null || dLat == 0) {
				return false;
			}
			if (dLng == null || dLng == 0) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private String[] getResCmdsById(String resId) {
		List<String> cmdList = new ArrayList<String>();
		int i = 0;
		for (;;) {
			String cmd = NativeData.getResString(resId, i);
			if (cmd == null || cmd.length() <= 0) {
				break;
			}
			++i;
			cmdList.add(cmd);
		}
		return cmdList.toArray(new String[cmdList.size()]);
	}

	/**
	 * 延迟3000注册，阻止语音发起导航播报续航通知
	 * 
	 * @param intent
	 */
	Runnable1<Intent> mRegContRunTask = null;

	private void doKeyType10049(Intent intent) {
		JNIHelper.logd("zsbin doKeyType10049");
//		if (mIsInNaving) {
//			LogUtil.logd("doKeyType10049 mIsInNaving");
//			return;
//		}
		if (mRegContRunTask != null && !getEnableAutoPopUp()) {
			AppLogic.removeBackGroundCallback(mRegContRunTask);
		}
		mRegContRunTask = new Runnable1<Intent>(intent) {
			@Override
			public void run() {
				AppLogic.removeBackGroundCallback(mRegContRunnable);
//				if (mIsInNaving) {
//					LogUtil.logd("mRegContRunTask mIsInNaving");
//					return;
//				}
				mRegContRunnable.update(mP1);
				boolean isTop = mP1.getBooleanExtra("EXTRA_MESSAGE_IS_TOP", false);
				JNIHelper.logd("zsbin doKeyType10049 mRunTask run isTop:" + isTop);
				dealNavPopup(mRegContRunnable, isTop, 0);
			}
		};
		AppLogic.runOnBackGround(mRegContRunTask, 3000);
	}

	public void removeAutoPopUpCallback(){
		AppLogic.removeBackGroundCallback(mTimeOut);
		AppLogic.removeBackGroundCallback(mRegContRunnable);
		AppLogic.removeBackGroundCallback(mCommutingRunnable);
		AppLogic.removeBackGroundCallback(mRegContRunTask);
		destroyPlanningSelect();
	}

	/**
	 * 取消续航播报
	 */
	public void cancelAutoPopUp(boolean closeWin) {
		cancelContinueNav(false, closeWin);
		cancelCommutingNav(false);
		AppLogic.removeBackGroundCallback(mTimeOut);
		AppLogic.removeBackGroundCallback(mRegContRunnable);
		AppLogic.removeBackGroundCallback(mCommutingRunnable);
		AppLogic.removeBackGroundCallback(mRegContRunTask);

		destroyPlanningSelect();
	}

	Runnable1<Intent> mRegContRunnable = new Runnable1<Intent>(null) {

		@Override
		public void run() {
			mNowDelayRun = null;
			JNIHelper.logd("zsbin doKeyType10049 mRegContRunnable run ");
			if (mP1 == null || !getEnableAutoPopUp()) {
				return;
			}
			String msg = mP1.getStringExtra("EXTRA_ENDURANCE_DATA");
			JNIHelper.logd("recv 10049:" + msg);
			final SelectAsr select = createSelectAsrById(TASK_CONTINUE_NAVI_ID);
			select.addCmds("SURE", new Runnable() {

				@Override
				public void run() {
					if (mTimeOut != null) {
						AppLogic.removeBackGroundCallback(mTimeOut);
						mTimeOut = null;
					}
					startContinueNav();
				}
			}, getResCmdsById("RS_NAV_GD_CONT_SURE"));
			select.addCmds("CANCEL", new Runnable() {

				@Override
				public void run() {
					cancelContinueNav(true, true);
				}
			}, getResCmdsById("RS_NAV_GD_CONT_CANCEL"));

			if (!TextUtils.isEmpty(msg)) {
				TtsManager.getInstance().cancelSpeak(sSpeechId);
				final String spk = NativeData.getResPlaceholderString("RS_MAP_NAV_OR_CANCEL", "%CMD%", msg);
				mTimeOut = new Runnable1<Boolean>(true) {
					
					@Override
					public void run() {
						LogUtil.logd("mRegContRunnable timeout mp1:" + mP1);
						mTimeOut = null;
						cancelContinueNav(mP1, true);
					}
				};
				if (mIsInAsrWorking || mIsInTtsWorking) {
					mTtsDelayRun = new Runnable() {
						@Override
						public void run() {
							if (!navResumeStatus) {
								LogUtil.logd("continue mTtsDelayRun no resume");
								return;
							}
							select.build();
							sSpeechId = TtsManager.getInstance().speakVoice(spk, TtsManager.BEEP_VOICE_URL,
									new ITtsCallback() {
										@Override
										public void onEnd() {
											AppLogic.runOnBackGround(mTimeOut, 6000);
										}
									});
						}
					};
				} else {
					if (!navResumeStatus) {
						LogUtil.logd("mRegContRunnable no resume");
						return;
					}
					select.build();
					sSpeechId = TtsManager.getInstance().speakVoice(spk, TtsManager.BEEP_VOICE_URL, new ITtsCallback() {

						@Override
						public void onEnd() {
							super.onEnd();
							AppLogic.runOnBackGround(mTimeOut, 6000);
						}
					});
				}
			} else {
				destorySelectAsrTask(TASK_CONTINUE_NAVI_ID);
			}
		}
	};
	
	private Runnable1<Intent> mDelayRun = null;
	private Runnable1<Intent> mNowDelayRun = null;
	private Runnable  mTtsDelayRun = null;
	private Runnable1<Boolean> mTimeOut = null;

	private void dealNavPopup(Runnable1<Intent> run, boolean isTop, int time) {
		if (run == mNowDelayRun) {
			return;
		}
		
		if (isTop) {
			if (mNowDelayRun == null) {
				mNowDelayRun = run;
			} else {
				AppLogic.removeBackGroundCallback(mTimeOut);
				mTimeOut = null;
				destoryAllSelectAsrs();
				mDelayRun = mNowDelayRun;
				mNowDelayRun = run;
			}
			AppLogic.runOnBackGround(mNowDelayRun, time);
		} else {
			if (mNowDelayRun == null) {
				mNowDelayRun = run;
				AppLogic.runOnBackGround(mNowDelayRun, time);
			} else {
				mDelayRun = run;
			}
		}
	}

	private void startContinueNav() {
		destorySelectAsrTask(TASK_CONTINUE_NAVI_ID);
		if (mTimeOut != null) {
			AppLogic.removeBackGroundCallback(mTimeOut);
		}
		LogUtil.logd("startContinueNav");
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10049);
		intent.putExtra("EXTRA_ENDURANCE_DATA", true);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
		TtsManager.getInstance().cancelSpeak(sSpeechId);
		String continueNav = NativeData.getResString("RS_MAP_NAV_CONTINUE");
		String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", continueNav);
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(text, null);
	}

	private void cancelContinueNav(boolean speaker, boolean closeWin) {
		destorySelectAsrTask(TASK_CONTINUE_NAVI_ID);
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10049);
		intent.putExtra("EXTRA_ENDURANCE_DATA", false);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
		AppLogic.removeBackGroundCallback(mTimeOut);

		if (speaker) {
			String cancel = NativeData.getResString("RS_MAP_CONFIRM_CANCEL_ASR");
			String text = NativeData.getResString("RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_CANCEL_CONTINUE_NAV")
					.replace("%CMD%", cancel);
			AsrManager.getInstance().setNeedCloseRecord(true);
			TtsManager.getInstance().cancelSpeak(sSpeechId);
			sSpeechId = RecorderWin.speakTextWithClose(text, null);
		}

		if (RecorderWin.isOpened() && closeWin) {
			LogUtil.logd("cancelContinueNav close recordwin");
			RecorderWin.close();
		}
	}

	public void destorySelectAsrTask(String taskId) {
		TtsManager.getInstance().cancelSpeak(sSpeechId);
		sSpeechId = TtsManager.INVALID_TTS_TASK_ID;
		SelectAsr asr = mAsrMap.get(taskId);
		if (asr != null) {
			asr.destory();
			mAsrMap.remove(taskId);
		}
	}

	private void doKeyType10050(Intent intent) {
		SelectAsr mSelectAsr = createSelectAsrById(TASK_RECV_PLAN_ID);
		String des = intent.getStringExtra("EXTRA_SEND2CAR_DATA");
		if (!TextUtils.isEmpty(des)) {
			mSelectAsr.addCmds("NAVI_TO_POS", new Runnable() {

				@Override
				public void run() {
					NavManager.getInstance().addNavThirdAppPlanEndRunnable(new Runnable() {

						@Override
						public void run() {
							NavThirdApp nta = NavManager.getInstance().getLocalNavImpl();
							if (nta != null && nta instanceof NavAmapAutoNavImpl) {
								int mCode = ((NavAmapAutoNavImpl) nta).getMapCode();
								if (mCode >= 270 && NavManager.getInstance().getGaoDeAutoPlanningRoute()) {

								} else {
									((NavAmapAutoNavImpl) nta).startNavByInner();
								}
							}
						}
					});

					String navFail = NativeData.getResString("RS_MAP_NAV_FAIL");
					NavManager.getInstance().setSpeechAfterPlanError(true, navFail);
					TtsManager.getInstance().cancelSpeak(sSpeechId);
					String navTo = NativeData.getResString("RS_MAP_NAV_TO");
					String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", navTo);
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(text, null);
					Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
					intent.putExtra("KEY_TYPE", 10050);
					intent.putExtra("EXTRA_SEND2CAR_DATA", true);
					intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
					sendBroadcast(intent);
				}
			}, getResCmdsById("RS_NAV_GD_NAV_DIRECT"));
			mSelectAsr.addCmds("NAVI_TO_CANCEL", new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
					intent.putExtra("EXTRA_SEND2CAR_DATA", false);
					intent.putExtra("KEY_TYPE", 10050);
					intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
					sendBroadcast(intent);
					TtsManager.getInstance().cancelSpeak(sSpeechId);
					String cancel = NativeData.getResString("RS_MAP_CONFIRM_CANCEL_ASR");
					String text = NativeData.getResString("RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_SEND_TO_CAR").replace("%CMD%", cancel);
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(text, null);
				}
			}, getResCmdsById("RS_NAV_GD_NAV_DIRECT_CANCEL"));
			mSelectAsr.build();
			TtsManager.getInstance().cancelSpeak(sSpeechId);
			des = des.replace("收到位置", "");
			String spk = NativeData.getResPlaceholderString("RS_MAP_NAV_PHONE", "%LOCATION%", des);
			sSpeechId = TtsManager.getInstance().speakVoice(spk, TtsManager.BEEP_VOICE_URL);
		} else {
			destorySelectAsrTask(TASK_RECV_PLAN_ID);
		}
	}
	
	private void doKeyType10051(Intent intent) {
		String jsonMsg = intent.getStringExtra("EXTRA_CALCULATED_FAIL_OPTION_DATA");
		SelectAsr mSelectAsr = createSelectAsrById(TASK_PLAN_FAIL);
		mSelectAsr.addCmds("NAV_TO_REPLAN", new Runnable() {

			@Override
			public void run() {
				Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
				intent.putExtra("KEY_TYPE", 10051);
				intent.putExtra("EXTRA_CALCULATED_FAIL_OPTION_DATA", true);
				intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
				sendBroadcast(intent);
				String text = NativeData.getResString("RS_VOICE_NAV_REPLAN_AGAIN");
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(text, null);
			}
		}, getResCmdsById("RS_NAV_GD_REPLAN_SURE"));
		mSelectAsr.addCmds("NAV_TO_CANCEL", new Runnable() {

			@Override
			public void run() {
				Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
				intent.putExtra("KEY_TYPE", 10051);
				intent.putExtra("EXTRA_CALCULATED_FAIL_OPTION_DATA", false);
				intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
				sendBroadcast(intent);
				String cancel = NativeData.getResString("RS_MAP_CONFIRM_CANCEL_ASR");
				String text = NativeData.getResString("RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_CALCULATED_FAIL").replace("%CMD%", cancel);
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(text, null);
			}
		}, getResCmdsById("RS_NAV_GD_REPLAN_CANCEL"));
		mSelectAsr.build();
		sSpeechId = TtsManager.getInstance().speakVoice(jsonMsg, TtsManager.BEEP_VOICE_URL);
	}

	private void doKeyType10052(Intent intent) {
		SelectAsr mSelectAsr = createSelectAsrById(TASK_PARKER_ID);
		mSelectAsr.isSelectedClose(false);
		try {
			String json = intent.getStringExtra("EXTRA_PARK_DATA");
			if (!TextUtils.isEmpty(json)) {
				JSONArray jsonArray = new JSONArray(json);
				int distanceIndex = -1;
				int lastDistance = -1;
				int priceIndex = -1;
				int lastPrice = -1;
				final String spk = NativeData.getResString("RS_NAV_GD_PARKER_SELECT");
				for (int i = 0; i < jsonArray.length(); i++) {
					final int index = i;
					String strIndex = NativeData.getResString("RS_VOICE_DIGITS", index + 1);
					mSelectAsr.addCmds("TYPE_INDEX_" + strIndex, new Runnable() {

						@Override
						public void run() {
							Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
							intent.putExtra("KEY_TYPE", 10052);
							intent.putExtra("EXTRA_PARK_DATA", index);
							intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
							sendBroadcast(intent);
							AsrManager.getInstance().setNeedCloseRecord(true);
							RecorderWin.speakTextWithClose(spk, null);
						}
					}, "第" + strIndex + "个");
					JSONObject jo = (JSONObject) jsonArray.get(i);
					if (jo.has("parkDistance")) {
						int distance = jo.getInt("parkDistance");
						if (distanceIndex < 0 || distance < lastDistance) {
							distanceIndex = i;
							lastDistance = distance;
						}
					}

					if (jo.has("parkPrice")) {
						int price = jo.getInt("parkDistance");
						if (priceIndex < 0 || price < lastPrice) {
							priceIndex = i;
							lastPrice = price;
						}
					}
				}

				if (distanceIndex != -1) {
					JNIHelper.logd("distanceIndex:" + distanceIndex);
					mSelectAsr.addCmds("DISTANCE_INDEX", new Runnable1<Integer>(distanceIndex) {

						@Override
						public void run() {
							Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
							intent.putExtra("KEY_TYPE", 10052);
							intent.putExtra("EXTRA_PARK_DATA", mP1);
							intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
							sendBroadcast(intent);
							AsrManager.getInstance().setNeedCloseRecord(true);
							RecorderWin.speakTextWithClose(spk, null);
						}
					}, getResCmdsById("RS_NAV_GD_PARKER_LESSDISTANCE"));
				}
//				if (priceIndex != -1) {
//					JNIHelper.logd("priceIndex:" + priceIndex);
//					mSelectAsr.addCmds("PRICE_INDEX", new Runnable1<Integer>(priceIndex) {
//
//						@Override
//						public void run() {
//							Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
//							intent.putExtra("KEY_TYPE", 10052);
//							intent.putExtra("EXTRA_PARK_DATA", mP1);
//							GlobalContext.get().sendBroadcast(intent);
//							AsrManager.getInstance().setNeedCloseRecord(true);
//							RecorderWin.speakTextWithClose("", null);
//						}
//					}, getResCmdsById("RS_NAV_GD_PARKER_LESSMONEY"));
//				}
				mSelectAsr.addCmds("IGNORE_INDEX",new Runnable() {

					@Override
					public void run() {
						Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
						intent.putExtra("KEY_TYPE", 10052);
						intent.putExtra("EXTRA_PARK_DATA", -1);
						intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
						GlobalContext.get().sendBroadcast(intent);
						AsrManager.getInstance().setNeedCloseRecord(true);
						String spk = NativeData.getResString("RS_VOICE_DOING_COMMAND");
						RecorderWin.speakTextWithClose(spk, null);
					}
				}, getResCmdsById("RS_NAV_GD_PARKER_IGNORE"));

			}
			mSelectAsr.build();
		} catch (JSONException e) {
			JNIHelper.logw("Parker exception e:" + e.toString());
			destorySelectAsrTask(TASK_PARKER_ID);
		}
	}

	private SelectAsr createSelectAsrById(String taskId) {
		SelectAsr sa = mAsrMap.get(taskId);
		if (sa == null) {
			sa = new SelectAsr();
		}

		sa.destory();
		sa.setTaskId(taskId);
		mAsrMap.put(taskId, sa);
		return sa;
	}

	public void destoryAllSelectAsrs() {
		Set<Entry<String, SelectAsr>> asrset = mAsrMap.entrySet();
		for (Entry<String, SelectAsr> asr : asrset) {
			SelectAsr sa = asr.getValue();
			if (sa != null) {
				sa.destory();
			}
		}
		mAsrMap.clear();
	}

	private boolean navResumeStatus;

	public void onStateChange(boolean onResume) {
		navResumeStatus = onResume;
		Set<Entry<String, SelectAsr>> asrset = mAsrMap.entrySet();
		for (Entry<String, SelectAsr> asr : asrset) {
			SelectAsr sa = asr.getValue();
			if (sa != null) {
				if (onResume)
					sa.build();
				else
					sa.onPause();
			}
		}

		if (!onResume) {
			if (sSpeechId != TtsManager.INVALID_TTS_TASK_ID) {
				TtsManager.getInstance().cancelSpeak(sSpeechId);
			}
			if (mTimeOut != null) {
				mTimeOut.update(false);
			}
		}
	}
	private String[] tmpString = {"零","一","二","三","四","五","六","七","八"};
	private Map<Integer, ArrayList<String>>  mPlanningWakeUp = new HashMap<Integer, ArrayList<String>>();
	public Map<Integer, ArrayList<String>> getPlanningWakeUp(){
		return mPlanningWakeUp;
	}

	public void doKeyType10056(Intent intent) {
		try {
			String json = "";
			JSONObject jo = null;
			try {
				json = intent.getStringExtra("EXTRA_ROAD_INFO");
				JNIHelper.logd("EXTRA_ROAD_INFO:" + json);
				if (TextUtils.isEmpty(json)) {
					return;
				}
				jo = new JSONObject(json);
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.logd(e.getMessage());
			}

			if (mRoadInfo != null) {
				mRoadInfo = null;
			}
			mRoadInfo = new RoadInfo();
			if (jo.has("ToPoiName")) {
				mRoadInfo.toPoiName = jo.getString("ToPoiName");
			}
			if (jo.has("ToPoiLongitude")) {
				mRoadInfo.toPoiLng = jo.getDouble("ToPoiLongitude");
			}
			if (jo.has("midPoisNum")) {
				mRoadInfo.midPoisNum = jo.getInt("midPoisNum");
			}
			if (jo.has("FromPoiLongitude")) {
				mRoadInfo.fromPoiLng = jo.getDouble("FromPoiLongitude");
			}
			if (jo.has("FromPoiAddr")) {
				mRoadInfo.fromPoiAddr = jo.getString("FromPoiAddr");
			}
			if (jo.has("FromPoiName")) {
				mRoadInfo.fromPoiName = jo.getString("FromPoiName");
			}
			if (jo.has("pathNum")) {
				mRoadInfo.pathNum = jo.getInt("pathNum");
			}
			if (jo.has("path_info")) {
				mRoadInfo.pathInfo = jo.getString("path_info");
				if (!TextUtils.isEmpty(mRoadInfo.pathInfo)) {
					JSONArray jsonArray = new JSONArray(mRoadInfo.pathInfo);
					PathInfo[] pathInfo = new PathInfo[mRoadInfo.pathNum];
					int vc = 0;
					// 处于使用状态就取出版本号，否则仅解析数据
					NavThirdApp nta = NavAppManager.getInstance().getCurrActiveTool();
					if (nta instanceof NavAmapAutoNavImpl) {
						vc = ((NavAmapAutoNavImpl) nta).getMapCode();
					}
					mPlanningWakeUp.clear(); 
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jo1 = (JSONObject) jsonArray.get(i);
						pathInfo[i] = new PathInfo();
						if (jo1.has("streetNamesSize")) {
							pathInfo[i].streetNamesSize = jo1.getInt("streetNamesSize");
						}
						if (jo1.has("streetNames")) {
							pathInfo[i].streetTxt = jo1.getString("streetNames");
							if (!TextUtils.isEmpty(pathInfo[i].streetTxt)) {
								JSONArray jas = new JSONArray(pathInfo[i].streetTxt);
								String[] roads = new String[pathInfo[i].streetNamesSize];
								for (int k = 0; k < roads.length; k++) {
									roads[k] = (String) jas.get(k);
								}
								pathInfo[i].streetArrays = roads;
							}
						}
						if (jo1.has("method")) {
							pathInfo[i].tag = jo1.getString("method");
						}
						if (vc >= 270 && mIsPlaning) {
							ArrayList<String> cmdList = new ArrayList<String>();

							cmdList.add("第" + tmpString[i + 1] + "个");
//							cmdList.add("第"+(i+1)+"个");
							cmdList.add("方案" + (tmpString[i + 1]));
							cmdList.add("路线" + (tmpString[i + 1]));
							if (!TextUtils.isEmpty(pathInfo[i].tag) && !pathInfo[i].tag.startsWith("路线")) {
								cmdList.add(pathInfo[i].tag);
							}
							mPlanningWakeUp.put(i, cmdList);
						}										
					}
					if(vc >= 270 && mIsPlaning){
						buildPlanningSelect(false);
						if (jsonArray.length() > 1) {
							String spk = NativeData.getResString("RS_VOICE_PLANNING_ROUTE").replace("%NUMBER%",
									"" + jsonArray.length());
							if (mPlanningSelectAsr != null) {
								mPlanningSelectAsr.build();
							}
							sSpeechId = TtsManager.getInstance().speakVoice(spk, TtsManager.BEEP_VOICE_URL,
									(com.txznet.comm.remote.util.TtsUtil.ITtsCallback) null);
						} else if (jsonArray.length() == 1) {
							AppLogic.runOnBackGround(new Runnable() {
								
								@Override
								public void run() {
									if(mIsInNaving){
										AppLogic.runOnBackGround(new Runnable() {
											@Override
											public void run() {
												Intent intent2 = new Intent();
												intent2.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
												intent2.putExtra("KEY_TYPE", 10009);
												intent2.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
												sendBroadcast(intent2);
											}
										}, 150);
									}else {
										String spk = NativeData.getResString("RS_VOICE_PLANNING_ROUTE_ONLY");
										sSpeechId = TtsManager.getInstance().speakText(spk, new ITtsCallback() {
											@Override
											public void onSuccess() {
												AppLogic.runOnBackGround(new Runnable() {

													@Override
													public void run() {
														Intent intent2 = new Intent();
														intent2.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
														intent2.putExtra("KEY_TYPE", 10009);
														intent2.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
														sendBroadcast(intent2);
													}
												}, 150);
											}
										});
									}
								}
							},1000);
						}

					}		
				}
			}
			if (jo.has("FromPoiLatitude")) {
				mRoadInfo.fromPoiLat = jo.getDouble("FromPoiLatitude");
			}
			if (jo.has("ToPoiLatitude")) {
				mRoadInfo.toPoiLat = jo.getDouble("ToPoiLatitude");
			}
			if(jo.has("ToPoiLongitude")&&jo.has("ToPoiLatitude")){
				getToPoiCity(mRoadInfo.toPoiLat,mRoadInfo.toPoiLng);
			}
			if (jo.has("ToPoiAddr")) {
				mRoadInfo.toPoiAddr = jo.getString("ToPoiAddr");
			}
			mTjPoiList.clear();
			if (jo.has("midPoiArray")) {
				JNIHelper.logd("midPoiArray= " + jo.getString("midPoiArray"));
				JSONArray midPoi = jo.getJSONArray("midPoiArray");
				for (int i = 0; i < midPoi.length(); i++) {
					JSONObject jo1 = (JSONObject) midPoi.get(i);
					Poi poiTmp = new Poi();
					if (jo1.has("MidPoiName")) {
						poiTmp.setName(jo1.getString("MidPoiName"));
					}
					if (jo1.has("MidPoiLatitude")) {
						poiTmp.setLat(jo1.getDouble("MidPoiLatitude"));
					}
					if (jo1.has("MidPoiLongitude")) {
						poiTmp.setLng(jo1.getDouble("MidPoiLongitude"));
					}
					mTjPoiList.add(poiTmp);
				}
			}
			
			mRoadInfo.printLatLngInfo();
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.logd(e.getMessage());
		}
	}
	public void choicePlanRoad(int index){
		Intent intent = new Intent();
		intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES | Intent.FLAG_RECEIVER_FOREGROUND);
		intent.putExtra("KEY_TYPE", 10055);
		intent.putExtra("EXTRA_CHANGE_ROAD", index+1);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
		Intent intent2 = new Intent();
		intent2.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent2.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES | Intent.FLAG_RECEIVER_FOREGROUND);
		intent2.putExtra("KEY_TYPE", 10009);
		intent2.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent2);
		RecorderWin.close();
	}
	SelectAsr mPlanningSelectAsr = null;
	public void destroyPlanningSelect(){
		if(mPlanningSelectAsr != null){
			destorySelectAsrTask(TASK_SELECT_NAVIGATE_ROAD);
			mPlanningSelectAsr = null;
		}
	}
	public void buildPlanningSelect(boolean isBuild){
		if(mIsPlaning && mPlanningWakeUp.size() > 0 ){
			if(mPlanningSelectAsr == null){
				mPlanningSelectAsr = createSelectAsrById(TASK_SELECT_NAVIGATE_ROAD);
				
			}else{
				mPlanningSelectAsr.destory();
				mPlanningSelectAsr = createSelectAsrById(TASK_SELECT_NAVIGATE_ROAD);
			}
			Set<Integer> keySet = mPlanningWakeUp.keySet();
			for(final Integer key: keySet){
				final ArrayList<String> arrayList = mPlanningWakeUp.get(key);
				mPlanningSelectAsr.addCmds("SELECT_"+key, new Runnable() {
					@Override
					public void run() {
						if (sSpeechId != TtsManager.INVALID_TTS_TASK_ID) {
							TtsManager.getInstance().cancelSpeak(sSpeechId);
						}
						choicePlanRoad(key);
						mPlanningWakeUp.clear();
						destroyPlanningSelect();
					}
				},arrayList.toArray(new String[arrayList.size()]));
			}		
			if(isBuild){
				mPlanningSelectAsr.build();
			}
		}
	}
	private void getToPoiCity(double toPoiLat, double toPoiLng) {
		
		LocationManager.getInstance().reverseGeoCode(toPoiLat,toPoiLng, new OnGeocodeSearchListener() {

			@Override
			public void onRegeocodeSearched(RegeocodeResult arg0, int arg1) {
				if (arg0 != null) {
					RegeocodeAddress ra = arg0.getRegeocodeAddress();
					if (ra != null) {
						if(TextUtils.isEmpty( ra.getCity())){
							mRoadInfo.toCity =ra.getProvince();
						}else{
							mRoadInfo.toCity =ra.getCity();
						}
					}
				}
			}

			@Override
			public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
			}
		});
	}

	public List<Poi> getTjPoiList(){
		return mTjPoiList;
	}
	
	private void doKeyType10057(Intent intent) {
		// 沿途搜索类别 1：WC；2：ATM；3：维修站；4：加油站
		try {
			String jsonResult = intent.getStringExtra("EXTRA_SEARCH_ALONG_THE_WAY");
			if (!TextUtils.isEmpty(jsonResult)) {
				int count = 0;
				int type = 0;
				WayPoiData wpd = new WayPoiData();
				JSONObject jo = new JSONObject(jsonResult);
				if (jo.has("search_result_size")) {
					count = jo.getInt("search_result_size");
					wpd.wayPoiSize = count;
				}
				if (jo.has("search_type")) {
					type = jo.getInt("search_type");
					wpd.wayPoiType = type;
				}
				if (jo.has("poi_info")) {
					List<WayPoi> wayPois = new ArrayList<WayPoi>();
					JSONArray jsonArray = (JSONArray) jo.get("poi_info");
					for (int i = 0; i < count; i++) {
						WayPoi wayPoi = new WayPoi();
						JSONObject jObj = (JSONObject) jsonArray.get(i);
						if (jObj != null) {
							if (jObj.has("poi_Longitude")) {
								String lng = jObj.getString("poi_Longitude");
								if (!TextUtils.isEmpty(lng)) {
									wayPoi.longitude = Double.parseDouble(lng);
								} else {
									wayPoi.longitude = jObj.getDouble("poi_Longitude");
								}
							}
							if (jObj.has("poi_distance")) {
								String distance = jObj.getString("poi_distance");
								if (!TextUtils.isEmpty(distance)) {
									wayPoi.distance = distance;
								}
							}
							if (jObj.has("poi_Latitude")) {
								String lat = jObj.getString("poi_Latitude");
								if (!TextUtils.isEmpty(lat)) {
									wayPoi.latitude = Double.parseDouble(lat);
								} else {
									wayPoi.latitude = jObj.getDouble("poi_Latitude");
								}
							}
							if (jObj.has("poi_addr")) {
								wayPoi.addr = jObj.getString("poi_addr");
							}
							if (jObj.has("poi_name")) {
								wayPoi.name = jObj.getString("poi_name");
							}

							wayPois.add(wayPoi);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doKeyType10059(Intent intent) {
		int category = intent.getIntExtra("CATEGORY", -1);
		int bSucc = intent.getIntExtra("EXTRA_RESPONSE_CODE", -1);
		switch (category) {
		case 1:
			if (bSucc == 0) {
				querySet(1);
			}
			break;

		case 2:
			if (bSucc == 0) {
				querySet(2);
			}
			break;
		}
	}

	private void doKeyType10109(Intent intent){
		NavThirdApp nta = NavAppManager.getInstance().getCurrActiveTool();
		if (nta instanceof NavAmapAutoNavImpl) {
			if(((NavAmapAutoNavImpl) nta).getTaskIsTimeOut()){
				return;
			}
			((NavAmapAutoNavImpl) nta).clearTimeOutTask();

			String info = intent.getStringExtra("EXTRA_LOCATION_TRAFFIC_INFO");
			if(TextUtils.isEmpty(info)){
				return;
			}
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(info,null);
		}
	}

    private void doKeyType11002(Intent intent){
		NavThirdApp nta = NavAppManager.getInstance().getCurrActiveTool();
		//高德地图后台运行时也会发送exit命令，我们会认为此导航已关闭，设置正在使用的导航工具为null，但导航工具仍运行，因此，nta会为空,重新设置正在使用的导航工具
		if (nta==null){
			NavAppManager.getInstance().setMCurrActivePkn(NavAmapAutoNavImpl.PACKAGE_NAME);
			nta = NavAppManager.getInstance().getCurrActiveTool();
		}
		if (nta instanceof NavAmapAutoNavImpl) {
			if(((NavAmapAutoNavImpl) nta).getTaskIsTimeOut()){
				return;
			}
			((NavAmapAutoNavImpl) nta).clearTimeOutTask();
			String extraData = intent.getStringExtra("EXTRA_FAVORITE_DATA");
			try {
				JSONArray array = new JSONArray(extraData);
				if (TextUtils.isEmpty(extraData) || array.length() == 0) {
					String spk = NativeData.getResString("RS_VOICE_NOT_FIND_COLLECTION_POINT");
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(spk, null);
					return;
				}

				List<Poi> pois = new ArrayList<Poi>();
				for (int i = 0; i < array.length(); i++) {
					Poi poi = new Poi();
					JSONObject object = array.getJSONObject(i);
					poi.setLat(object.optDouble("latitude"));
					poi.setLng(object.optDouble("longitude"));
					poi.setAction(Poi.PoiAction.ACTION_NAV_COLLECTION_POINT);
					poi.setName(object.optString("name"));
					//不同版本的高德距离返回格式不一致，例如：distance:"7.6公里"、distance:"700米"、distance:2000
					int distance = object.optInt("distance",-1);
					if (distance == -1) {
						String strDistance = object.optString("distance");
						if (strDistance.indexOf("公里") != -1) {
							strDistance = strDistance.substring(0, strDistance.indexOf("公里"));
							distance = (int) (Float.valueOf(strDistance) * 1000);
						} else if (strDistance.indexOf("米") != -1) {
							strDistance = strDistance.substring(0, strDistance.indexOf("米"));
							distance = Float.valueOf(strDistance).intValue();
						}
					}
					poi.setDistance(distance);
					poi.setGeoinfo(object.optString("addr"));
					pois.add(poi);
				}
				String spk;
				if (pois.size() == 1) {
					spk = NativeData.getResString("RS_POI_SELECT_NAV_SINGLE_SPK");
				} else {
					spk = NativeData.getResString("RS_POI_SELECT_NAV_LIST_SPK");
				}

				PoiWorkChoice.PoisData poisData = new PoiWorkChoice.PoisData();
				poisData.action = Poi.PoiAction.ACTION_NAV_COLLECTION_POINT;
				poisData.isBus = false;
				poisData.mPois = pois;

				CompentOption<Poi> option = new CompentOption<Poi>();
				option.setTtsText(spk);
				option.setCanSure(false);
				option.setProgressDelay(0);
				ChoiceManager.getInstance().showPoiList(poisData, option);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
    }

	private int mReportEvent = 0;
	String[] mEventStr =NativeData.getResStringArray("RS_VOICE_REPORT_EVENT_TYPE");
	private void doKeyType12108(Intent intent){		
		if(intent.hasExtra("IS_REPORT_VIEW_SHOW")){
			if(isReporting){
				return ;
			}
			isReporting = true;
			JNIHelper.logd("isReporting Show== "+isReporting);
			mReportEvent= NavManager.getInstance().mExactEvent;
			String spk = null;
			if(mReportEvent ==0){
				spk =NativeData.getResString("RS_VOICE_REPORT_EVENT_HINT");
			}else if(mReportEvent >0){
				spk = NativeData.getResString("RS_VOICE_REPORT_ENSURE_SELECT").replace("%EVENT%", mEventStr[mReportEvent-1]);
			}
			sSpeechId = TtsManager.getInstance().speakVoice(spk,TtsManager.BEEP_VOICE_URL,new ITtsCallback() {
				@Override
				public void onSuccess() {
					buildReportWakeup();
					AppLogic.runOnBackGround(mReportTimeout,10000);
				}
			});		
		}else if(intent.hasExtra("REPORT_RESULT_CODE")){
			AppLogic.removeBackGroundCallback(mReportTimeout);
			isReporting = false;
			JNIHelper.logd("isReporting Code== "+isReporting);
			int status = intent.getIntExtra("REPORT_RESULT_CODE", 1);
			String spk = null;
			String event =NativeData.getResString("RS_VOICE_REPORT_EVENT_DEFAULT");
			if(mReportEvent>0){
				event = mEventStr[mReportEvent-1];
			}
			if(status ==1 ){
				spk = NativeData.getResString("RS_VOICE_REPORT_SUCCESS").replace("%EVENT%", event);
			}else{
				spk = NativeData.getResString("RS_VOICE_REPORT_FAIL").replace("%EVENT%", event);
			}
			cancelReportTraffic();	
			sSpeechId = TtsManager.getInstance().speakText(spk);
		}else if(intent.hasExtra("IS_REPORT_VIEW_CLOSE")){
			if(isReporting){
				isReporting = false;
				cancelReportTraffic();				
			}
		}
	}
	private void buildReportWakeup(){
		final SelectAsr selectAsr = createSelectAsrById(TASK_REPORT_TRAFFIC_ID);
		selectAsr.addCmds("SURE", new Runnable() {
			@Override
			public void run() {
				reporTraffic(-1);
				AppLogic.removeBackGroundCallback(mReportTimeout);
				AppLogic.runOnBackGround(new Runnable() {
					@Override
					public void run() {
						JNIHelper.logd("isReporting SURE== "+isReporting);
						if (isReporting) {
							sSpeechId = TtsManager.getInstance().speakVoice(
									NativeData.getResString("RS_VOICE_REPORT_NULL"),TtsManager.BEEP_VOICE_URL,new ITtsCallback() {
								@Override
								public void onSuccess() {
									buildReportWakeup();
									AppLogic.runOnBackGround(mReportTimeout,10000);
								}
							});
						}
					}
				}, 2000);
			}
		}, getResCmdsById("RS_NAV_GD_REPORT_SURE"));
		selectAsr.addCmds("CANCEL", new Runnable() {
			@Override
			public void run() {
				JNIHelper.logd("isReporting CANCEL== "+isReporting);
				isReporting = false;
				reporTraffic(0);
			}
		}, getResCmdsById("RS_NAV_GD_CONT_CANCEL"));
		selectAsr.addCmds("TRAFFIC", new Runnable() {
			@Override
			public void run() {
				reporTraffic(1);
			}
		}, getResCmdsById("RS_NAV_GD_REPORT_TRAFFIC"));
		selectAsr.addCmds("CONGESTION", new Runnable() {
			@Override
			public void run() {
				reporTraffic(2);
			}
		}, getResCmdsById("RS_NAV_GD_REPORT_CONGESTION"));
		selectAsr.addCmds("CONSTRUCTION", new Runnable() {
			@Override
			public void run() {
				reporTraffic(3);
			}
		}, getResCmdsById("RS_NAV_GD_REPORT_CONSTRUCTION"));
		selectAsr.addCmds("ROADCLOSE", new Runnable() {
			@Override
			public void run() {
				reporTraffic(4);
			}
		}, getResCmdsById("RS_NAV_GD_REPORT_ROADCLOSE"));
		selectAsr.addCmds("SEEPER", new Runnable() {
			@Override
			public void run() {
				reporTraffic(5);
			}
		}, getResCmdsById("RS_NAV_GD_REPORT_SEEPER"));
		selectAsr.build();
	}
	private boolean isReporting = false;
	private void reporTraffic(int event) {
		LogUtil.logd("reporTraffic:" + event);
		if (event > 0) {
			mReportEvent = event;
		}
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		if (RecorderWin.isOpened()) {
			RecorderWin.dismiss();
		}
		intent.putExtra("KEY_TYPE", 12108);
		if (event < 1) {
			intent.putExtra("REPORT_OPERATE_TYPE", 2);
			intent.putExtra("REPORT_VIEW_OPERATE", event + 2);
		} else {
			intent.putExtra("REPORT_OPERATE_TYPE", 2);
			intent.putExtra("REPORT_TYPE", 2);
			intent.putExtra("REPORT_EVENT_TYPE", event);
			intent.putExtra("REPORT_VIEW_OPERATE", 1);
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}
	
	private void cancelReportTraffic(){
		setEnableAutoPupUp(true);
		AppLogic.removeBackGroundCallback(mReportTimeout);
		destorySelectAsrTask(TASK_REPORT_TRAFFIC_ID);
	}
	Runnable mReportTimeout = new Runnable() {	
		@Override
		public void run() {
			JNIHelper.logd("isReporting Timeout== "+isReporting);
			reporTraffic(0);
		}
	};
	private void doKeyType12101(Intent intent){
		int type = intent.getIntExtra("EXTRA_EVENT_TOUCH_FRAGMENT", -1);
		if(type == 0){
//			mIsPlaning = false;
//			if(mPlanningSelectAsr != null ){
//				destorySelectAsrTask(TASK_SELECT_NAVIGATE_ROAD);
//				mPlanningSelectAsr = null;
//				mPlanningWakeUp.clear();
//			}
		}else if(type ==4){
			mReportEvent = -1;
		}
	}

	/**
	 * 刷新路线
	 * @param intent
	 */
	private void doKeyType12012(Intent intent){
		String info = intent.getStringExtra("EXTRA_ROUTE_REFRESH_INFO");
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(info,null);
	}

	/**
	 * 主辅路切换结果返回
	 * @param intent
	 */
	private void doKeyType12013(Intent intent){
		NavThirdApp nta = NavAppManager.getInstance().getCurrActiveTool();
		if (nta instanceof NavAmapAutoNavImpl) {
			if(((NavAmapAutoNavImpl) nta).getTaskIsTimeOut()){
				return;
			}
			((NavAmapAutoNavImpl) nta).clearTimeOutTask();
			int state = intent.getIntExtra("EXTRA_STATE", 0);
			LogUtil.d("doKeyType12013 state:" + state);
			if (state == 0) {
				String spk = NativeData.getResString("RS_VOICE_UNSUPPORT_SWITCH_PATH");
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(spk,null);
			}
		}
	}
	
	private boolean mProcJingYou = false;
	private String mProcJingYouPoiName = null;
	private void doKeyType12102(Intent intent){
	    if(!mProcJingYou || TextUtils.isEmpty(mProcJingYouPoiName)){
	        LogUtil.d("doKeyType12102 procJingYouPoi:"+mProcJingYou);
	        return;
        }
        mProcJingYou = false;
		String json = intent.getExtras().getString("EXTRA_NAVI_VIA_INFO");
		int oldSize = mTjPoiList.size();
        try {
            JSONObject jsonObject = new JSONObject(json);
            int size = jsonObject.getInt("KEY_VIA_POIS_SIZE");
            if(oldSize < size){
                String text = NativeData.getResString("RS_MAP_ADD_JINGYOU_SUCCESS").replace("%NAME%",mProcJingYouPoiName);
                TtsManager.getInstance().speakText(text);
            }
            LogUtil.d("kevin","doKeyType12102 size:"+size);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public void proJingYouPoiSpeakTask(String poiName){
        mProcJingYou = true;
        mProcJingYouPoiName = poiName;
        AppLogic.runOnBackGround(resetProJingYouPoiStatusTask,10000);//异常机制
    }

	private Runnable resetProJingYouPoiStatusTask = new Runnable() {
        @Override
        public void run() {
            mProcJingYou = false;
            mProcJingYouPoiName = null;
        }
    };

	public static class RoadInfo extends BaseRoadInfo {
		public int midPoisNum;
		public String toCity;
		public String pathInfo;
	}

	public static class PathInfo extends BasePathInfo {
		public String streetTxt;
	}

	public static class WayPoiData {
		public int wayPoiSize;
		public int wayPoiType;
		public List<WayPoi> wayPois;

		public static class WayPoi {
			public double latitude;
			public double longitude;
			public String name;
			public String addr;
			public String distance;
		}
	}

	/**
	 * 1:表示搜索家,2:表示搜索公司
	 * 
	 * @param hOrc
	 */
	public void querySet(int hOrc) {
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10045);
		intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		intent.putExtra("EXTRA_TYPE", hOrc);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}
	
	/**
	 * 查询前后台状态
	 */
	public void queryNavFocus() {
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 12404);
		intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		intent.putExtra("EXTRA_REQUEST_AUTO_STATE", 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
		LogUtil.logd("queryNavFocus");
	}

	/**
	 * 查询导航状态
	 */
	public void queryNavStatus() {
	    Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
	    intent.putExtra("KEY_TYPE", 12404);
	    intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
	    intent.putExtra("EXTRA_REQUEST_AUTO_STATE", 1);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
	    sendBroadcast(intent);
		LogUtil.logd("queryNavStatus");
	}
	/**
	 * 地图标注
	 * 
	 * @param poiName
	 * @param lat
	 * @param lng
	 */
	public void markPoiPos(String poiName, double lat, double lng) {
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10039);
		intent.putExtra("SOURCE_APP", "txz");
		intent.putExtra("POINAME", poiName);
		intent.putExtra("LAT", lat);
		intent.putExtra("LON", lng);
		intent.putExtra("DEV", 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}

	/**
	 * 选择规划路径 1、2、3
	 * 
	 * @param index
	 */
	public void selectPlanRoute(int index) {
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10055);
		intent.putExtra("EXTRA_CHANGE_ROAD", index);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}
	//该方法用于判断当前高德导航是否存活
	public void checkAutoIsAlive(final int type){
		mIsApkIsAlive=false;
		Intent intent2 = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent2.putExtra("KEY_TYPE", 10061);
		intent2.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent2);
		AppLogic.runOnBackGround(new Runnable() {					
			@Override
			public void run() {
				LogUtil.logd("checkAutoIsAlive:" + mIsApkIsAlive);
				if (!mIsApkIsAlive) {
					PoiQuery.getInstance().onResult("", type);
				}
			}
		}, 500);
	}
	
	
	/**
	 * 查询家和公司的地址
	 */
	public void startQueryHomeCompany() {
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				querySet(1);
				querySet(2);
			}
		}, 3000);
	}

	/**
	 * 获取导航目的地
	 * 
	 * @return
	 */
	public Poi getDestinationPoi() {
		if (mRoadInfo != null) {
			return mRoadInfo.getDestinationPoi();
		}
		return null;
	}
	public String getDestinationCity(){
		if (mRoadInfo != null) {
			return mRoadInfo.toCity;
		}
		return null;
	}

	/********************* 离线搜索 ********************/
	public static class PoiQuery {
		public static class Option {
			final static int KWS_SEARCH_TYPE = 10023;
			final static int NEAR_SEARCH_TYPE = 10024;
			final static int TRAFFIC_SEARCH_TYPE=12401;
			final static int ONWAY_SEARCH_TYPE = 10057;
			
			final static int KWS_RESULT_TYPE = 10042;
			final static int NEAR_RESULT_TYPE = 10043;
			final static int TRAFFIC_RESULT_TYPE=12402;
			final static int ONWAY_RESULT_TYPE=10057;
			
			public int searchType;
			public String kws;
			public String city;
			public String traffic;
			public int searchRaduis = -1;
			public int num;
			public LatLonPoint myPoint;
			public LatLonPoint centerPoi;
			public PoiQueryResultListener mResultListener;

			public Intent genQueryIntent() {
				Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
				intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				if(searchType == ONWAY_SEARCH_TYPE){
					intent.putExtra("KEY_TYPE", 10057);
					intent.putExtra("EXTRA_SEARCHTYPE", Integer.parseInt(kws));
					return intent;
				}
				if (myPoint != null) {
					intent.putExtra("EXTRA_MYLOCLAT", myPoint.getLatitude());
					intent.putExtra("EXTRA_MYLOCLON", myPoint.getLongitude());
				}
				if(!TextUtils.isEmpty(city) && 
						!city.endsWith("市") && 
						!city.contains("自治") && 
						!city.contains("行政") ){
					city += "市";
				}
				intent.putExtra("EXTRA_CITY", city);
				intent.putExtra("EXTRA_MAXCOUNT", num);
				
				if (searchType == KWS_SEARCH_TYPE) {
					intent.putExtra("KEY_TYPE", 10023);
					intent.putExtra("EXTRA_SEARCHTYPE", 0);
				}
				if (searchType == NEAR_SEARCH_TYPE) {
					intent.putExtra("KEY_TYPE", 10024);
					intent.putExtra("EXTRA_SEARCHTYPE", 1);
				}
				if(searchType == TRAFFIC_SEARCH_TYPE){
					intent.putExtra("KEY_TYPE", 12401);
					intent.putExtra("EXTRA_TRAFFIC_CONDITION", traffic);
					return intent;
				}
				intent.putExtra("EXTRA_KEYWORD", kws);
				if (searchRaduis != -1) {
					intent.putExtra("EXTRA_RANGE", searchRaduis);
				}

				intent.putExtra("EXTRA_DEV", 0);
				
				
				if (centerPoi != null) {
					intent.putExtra("EXTRA_CENTERLAT", centerPoi.getLatitude());
					intent.putExtra("EXTRA_CENTERLON", centerPoi.getLongitude());
				}
				if(intent!=null){
					JNIHelper.logd("GDAutoToLito:intent="+intent);
					JNIHelper.logd("GDAutoToLito:intent extras="+intent.getExtras());
				}
				return intent;
			}
		}

		public class QueryRecord implements Runnable {
			public boolean mDirty;
			public Option mOption;

			@Override
			public void run() {
				if (mDirty || mOption == null || mOption.mResultListener == null) {
					return;
				}
				JNIHelper.loge("query kws:" + mOption.kws);

				mDirty = true;
				if (!checkSupSearch()) {
					// mOption.mResultListener.onResult(null);
					onResult(null,mOption.searchType);
					procQueue();
					return;
				}
				sIsBusy = true;

				Intent intent = mOption.genQueryIntent();
				if (intent != null) {
					JNIHelper.logd("query of amap...");
					intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
					sendBroadcast(intent);
				}
			}
		}

		public static interface PoiQueryResultListener {

			/**
			 * 搜索结果回调，使用原始的json数据返回
			 * 
			 * @param strData
			 */
			public void onResult(String strData);

		}

		private static PoiQuery sQuery = new PoiQuery();

		private PoiQuery() {
		}

		public static PoiQuery getInstance() {
			return sQuery;
		}

		List<QueryRecord> mRequests = new ArrayList<NavAmapValueService.PoiQuery.QueryRecord>();

		static boolean sIsBusy;
		static TXZHandler mHandler;
		static HandlerThread mThread;

		static void initHandler() {
			if (mHandler == null) {
				synchronized (PoiQuery.class) {
					if (mHandler == null) {
						mThread = new HandlerThread("gd-poi-query");
						mThread.start();
						mHandler = new TXZHandler(mThread.getLooper());
					}
				}
			}
		}
		
		public void startQuery(Option option) {
			if (option == null || option.mResultListener == null) {
				JNIHelper.loge("resultListener shouldn't be null！");
				return;
			}
			initHandler();

			QueryRecord task = new QueryRecord();
			task.mDirty = false;
			task.mOption = option;

			synchronized (mRequests) {
				JNIHelper.logd("adding");
				mRequests.add(task);
			}
			procQueue();
		}

		private void procQueue() {
			sIsBusy = false;
			synchronized (mRequests) {
				if (!mRequests.isEmpty()) {
					for (int i = 0; i < mRequests.size(); i++) {
						QueryRecord qr = mRequests.get(i);
						if (qr != null && !qr.mDirty) {
							mHandler.post(qr);
						}
					}
				}
			}
		}

		private boolean checkSupSearch() {
			int vc = PackageManager.getInstance().getVerionCode(NavAmapAutoNavImpl.PACKAGE_NAME);
			if (vc < 541) {
				vc = PackageManager.getInstance().getVerionCode(NavAmapAutoNavImpl.PACKAGE_NAME_LITE);
			}
			if (vc < 541) {
				return false;
			}
			return true;
		}

		
		public void cancel(Option option) {
			if (option == null) {
				return;
			}

			synchronized (mRequests) {
				for (QueryRecord qr : mRequests) {
					if (qr.mOption == option) {
						mRequests.remove(qr);
						JNIHelper.logd("cancel query");
						break;
					}
				}
			}
		}

		void onResult(String jsonData,int type) {
			synchronized (mRequests) {
				for (int i = 0; i < mRequests.size(); i++) {				
					QueryRecord qr = mRequests.get(i);
					if(qr.mOption.searchType==type){
						if (!qr.mDirty || qr.mOption == null || qr.mOption.mResultListener == null) {
							continue;
						}
						mRequests.remove(i);
						
						qr.mOption.mResultListener.onResult(jsonData);
					break;
					}
				}
			}
			sIsBusy = false;
			procQueue();
		}
	}

	private static String getPackageName(){
		NavThirdApp nta = NavAppManager.getInstance().getCurrActiveTool();
		if (nta instanceof NavAmapAutoNavImpl) {
			return nta.getPackageName();
		}
		return null;
	}

	private static void sendBroadcast(Intent intent){
		IntentUtil.getInstance().sendBroadcastFixSetPackage(intent,getPackageName());
	}

}

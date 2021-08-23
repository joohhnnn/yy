package com.txznet.txz.module.roadtraffic;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import android.text.TextUtils;

import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.RoadTrafficQueryInfo;
import com.txznet.comm.notification.NotificationInfo;
import com.txznet.comm.notification.NotificationInfo.TrafficBuilder;
import com.txznet.comm.remote.util.AsrUtil.IWakeupAsrCallback;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.gaode.NavAmapValueService;
import com.txznet.txz.component.roadtraffic.IInquiryRoadTrafficListener;
import com.txznet.txz.component.roadtraffic.RoadTrafficResult;
import com.txznet.txz.component.roadtraffic.navapi.RoadTrafficNavApiTool;
import com.txznet.txz.component.roadtraffic.gaode.RoadTrafficGaoDeTool;
import com.txznet.txz.component.roadtraffic.txz.RoadTrafficToolConImp;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.notification.NotificationManager;
import com.txznet.txz.ui.win.record.RecorderWin;

public class RoadTrafficManager extends IModule{

	static RoadTrafficManager sModuleInstance= new RoadTrafficManager();
	
	public static final int GAODE_SUPPORT_VERSION = 210548; 
	
	public static final int INQUiRY_TYPE_FRONT=1;
	public static final int INQUiRY_TYPE_POI=2;
	public static final int INQUiRY_TYPE_NEARBY=3;
	
	public static final int ERROR_CODE_SUCCESS =0;
	public static final int ERROR_CODE_NULL =1;
	public static final int ERROR_CODE_TIMEOUT =2;
	public static final int ERROR_CODE_OTHER=3;
	
	public static final int SOURCE_TYPE_GAODE=1;
	public static final int  SOURCE_TYPE_TENCENT=2;
	public static final int  SOURCE_TYPE_NAV_API=3;

	private int mInquiryType=-1;
	private String mCurrentKeyWord;
	IInquiryRoadTrafficListener mListener=null;
	private String mSpeakText;
	private boolean isPlayEnd=true;
	private boolean isResultBack=true;
	RoadTrafficResult mRoadTrafficeResult;
	boolean isInFocus = false;
	private boolean mShouldExitNav= false;
	
	public boolean getShouldExitNav(){
		return mShouldExitNav;
	}
	public void setShowExitNav(boolean should){
		mShouldExitNav=should;
	}
	
	private void playInqueryResult(){
		if(isPlayEnd&&isResultBack){
			boolean isNeadAsr =WinLayoutManager.getInstance().getChatMapView()==null;
			if(mRoadTrafficeResult!=null&&mRoadTrafficeResult.getPolyline()!=null){
				RecorderWin.speakTextWithClose(mSpeakText, isNeadAsr, new Runnable() {	
					@Override
					public void run() {
						
					}
				});		
				if(!isNeadAsr){
					IWakeupAsrCallback acc=new IWakeupAsrCallback() {
						
						@Override
						public boolean needAsrState() {
							// TODO Auto-generated method stub
							return false;
						}
						
						@Override
						public String getTaskId() {
							return "CTRL_TrafficControl";
						}
						
						public boolean onAsrResult(String text) {
							if("取消".equals(text)){
								
								WakeupManager.getInstance().recoverWakeupFromAsr("CTRL_TrafficControl");
								RecorderWin.refreshState(RecorderWin.STATE_NORMAL);
							}				
							return false;
						}
						public String[] genKeywords() {
							Set<String> setKeywords = new HashSet<String>();
							setKeywords.add("取消");
							String[] ret = new String[setKeywords.size()];
							setKeywords.toArray(ret);
							return ret;
						}
					};		
					WakeupManager.getInstance().useWakeupAsAsr(acc);
					RecorderWin.show();
				}

				
				String polyLine =mRoadTrafficeResult.getPolyline();
				GpsInfo msgGpsInfo = LocationManager.getInstance().getLastLocation().msgGpsInfo;
				String myLocal = msgGpsInfo.dblLat+","+msgGpsInfo.dblLng;
				JSONBuilder js = new JSONBuilder();
				js.put("polyLine", polyLine);
				if(!(mInquiryType==INQUiRY_TYPE_POI)){
					js.put("local", myLocal);						
				}
				RecorderWin.showMapInfo(js.build().toString().getBytes());

			}else if(mRoadTrafficeResult!=null&&
					!TextUtils.isEmpty(mRoadTrafficeResult.getResultText())&&
					mRoadTrafficeResult.getSourceType()==SOURCE_TYPE_GAODE){
				RecorderWin.dismiss();
				NotificationInfo notificationInfo = new TrafficBuilder().
						setMsgId(NativeData.getResString("RS_TRAFFIC_PLAYING_RESULT").replace("%KEY%", mCurrentKeyWord)).
						setRecorderStatus(true).setTrafficInfo(mRoadTrafficeResult.getResultText()).build();				
				NotificationManager.getInstance().notify(notificationInfo);
			} else if (mRoadTrafficeResult != null
					&& !TextUtils.isEmpty(mRoadTrafficeResult.getResultText())
					&& mRoadTrafficeResult.getSourceType() == SOURCE_TYPE_NAV_API) {
				RecorderWin.speakTextWithClose(mSpeakText, true, new Runnable() {
					@Override
					public void run() {
					}
				});
			} else{
                NavThirdApp navThirdApp = NavManager.getInstance().getLocalNavImpl();
                if (navThirdApp == null) {
//                    RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_NO_NAV_TOOL"), null);
                    return;
                }
                int mapVersion = navThirdApp.getMapVersion();
	      		if(mapVersion>=GAODE_SUPPORT_VERSION&&mRoadTrafficeResult!=null){
					RecorderWin.speakTextWithClose(mSpeakText, true, new Runnable() {	
						@Override
						public void run() {						
						}				
					});	
		      	}else{
					if(NetworkManager.getInstance().checkLeastFlow()){
						String resText = NativeData.getResString("RS_VOICE_SIM_WITHOUT_FLOW_TIP");
						TtsManager.getInstance().speakText(resText);
					}
					RecorderWin.speakTextWithClose(mSpeakText,null);
				}
			}
		}
	}
	
	private SearchReq mSearchReq;
	public  void cancleInqury(){
		if(mSearchReq!=null)
			mSearchReq.cancel();
		mSearchReq=null;
		WakeupManager.getInstance().recoverWakeupFromAsr("CTRL_TrafficControl");
	}
	
	
	static public RoadTrafficManager getInstance(){
		return sModuleInstance;
	}
	
	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SHOW_ROAD_TRAFFIC);
		return super.initialize_BeforeStartJni();
	}
	
	
	@Override
	public int initialize_AfterInitSuccess() {
		initRoadTraffic();
		return super.initialize_AfterInitSuccess();
	}
	
	
	private void initRoadTraffic() {
//		TNEngineManager.getInstance().init(GlobalContext.get());
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		switch (eventId) {
		case UiEvent.EVENT_VOICE:
			switch (subEventId) {
			case VoiceData.SUBEVENT_VOICE_SHOW_ROAD_TRAFFIC: {
				if(NetworkManager.getInstance().checkLeastFlow()){
					String resText = NativeData.getResString("RS_VOICE_SIM_WITHOUT_FLOW_TIP");
					TtsManager.getInstance().speakText(resText);
					break;
				}
				
				String city = "";
				mCurrentKeyWord = "";
				
				// 情景预处理
				try {
					final VoiceData.RoadTrafficQueryInfo info = VoiceData.RoadTrafficQueryInfo.parseFrom(data);
					city = info.strCity;
					mCurrentKeyWord = info.strKeywords;
					JSONObject jData = new JSONObject().put("strCity", info.strCity)
							.put("strDirection", info.strDirection).put("strKeywords", info.strKeywords);
					if (SenceManager.getInstance().noneedProcSence("traffic", jData.toString().getBytes())) {
						break;
					}

					NavThirdApp navThirdApp = NavManager.getInstance().getLocalNavImpl();
					if (navThirdApp == null) {
						RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_NO_NAV_TOOL"), null);
						break;
					}

					ReportUtil.doReport(new ReportUtil.Report.Builder().setType("traffic").setAction("show")
							.setSessionId().buildCommReport());
//					AsrManager.getInstance().stop();
					String spk = NativeData.getResString("RS_TRAFFIC_INQURYING").replace("%KEY%", mCurrentKeyWord);
					isResultBack=false;
					isPlayEnd=false;
					AsrManager.getInstance().stop();
					RecorderWin.speakTextWithClose(spk, false,new Runnable() {
						
						@Override
						public void run() {
							isPlayEnd=true;
							playInqueryResult();
						}
					});
					int type= getInQuiryType(city,mCurrentKeyWord);
					startInquiry(type,info);
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			}
			}
		}
		return super.onEvent(eventId, subEventId, data);
	}
	public static interface SearchReq {
		public void cancel();
	}

	private void startInquiry(int type,RoadTrafficQueryInfo info) {
		mRoadTrafficeResult=null;
		NavAmapValueService.getInstance().setTrafficSearchEnableAutoPupUp(false);
		NavThirdApp localNavImpl = NavManager.getInstance().getLocalNavImpl();
		if(localNavImpl!=null){
			isInFocus=localNavImpl.isInFocus();
		}
		mListener=new IInquiryRoadTrafficListener() {
			@Override
			public void onResult(RoadTrafficResult result) {
				mSpeakText =result.getResultText();
				isResultBack=true;
				mRoadTrafficeResult=result;
				playInqueryResult();
			}
			
			@Override
			public void onError(int errCode, String errDesc) {
				switch (errCode){//错误码根据高德协议定义的
					case 1://未安装高德
						mSpeakText = NativeData.getResString("RS_TRAFFIC_NO_UNSUPPORT");
						break;
					case 2://网络异常
						mSpeakText = NativeData.getResString("RS_TRAFFIC_NO_NETWORK");
						break;
					case 3://没有路况信息
						mSpeakText = NativeData.getResString("RS_TRAFFIC_ERROR_NO_INFO");
						break;
					case 4://所在城市暂未开通
						mSpeakText = NativeData.getResString("RS_TRAFFIC_ERROR");
						break;
					case 5://失败
//						break;
					default:
						mSpeakText = NativeData.getResString("RS_TRAFFIC_QUERY_ERROR");
						break;
				}
				isResultBack=true;
				playInqueryResult();
			}
		};
		LocationInfo myLocation = LocationManager.getInstance()
				.getLastLocation();
		if (myLocation != null && myLocation.msgGeoInfo != null
				&& !TextUtils.isEmpty(myLocation.msgGeoInfo.strCity)) {
				info.strCity=myLocation.msgGeoInfo.strCity;
		}
		
		RoadTrafficToolConImp tool = new RoadTrafficToolConImp();
		tool.addTools(new RoadTrafficGaoDeTool())
				.addTools(new RoadTrafficNavApiTool());
			 //.addTools(new RoadTrafficTencentTool());	
		mInquiryType=type;
		switch (type) {
		case INQUiRY_TYPE_NEARBY:
			JNIHelper.logd("RoadTrafficDebug:inquiry nearby roadtraffic by tool "+tool.getClass().toString());
			mSearchReq = tool.inquiryRoadTrafficByNearby(info, mListener);
			break;
		case INQUiRY_TYPE_FRONT:
			JNIHelper.logd("RoadTrafficDebug:inquiry front roadtraffic by tool "+tool.getClass().toString());
			mSearchReq = tool.inquiryRoadTrafficByFront(info, mListener);
			break;
		case INQUiRY_TYPE_POI:
			JNIHelper.logd("RoadTrafficDebug:inquiry ["+info.strKeywords+"] roadtraffic by tool "+tool.getClass().toString());
			mSearchReq = tool.inquiryRoadTrafficByPoi(info,mListener );
			break;
		default:
			break;
		}		
	}

	private int getInQuiryType(String city,String addr) {
		if(TextUtils.isEmpty(addr))
			return 0;
		if (!TextUtils.isEmpty(city) && "附近".equals(addr)) {
		 		return INQUiRY_TYPE_NEARBY;
		}else if( "前方".equals(addr)){
//			if(NavManager.getInstance().getNavStatue()){
				return INQUiRY_TYPE_FRONT;
//			}else{
//				return INQUiRY_TYPE_NEARBY;
//			}
		}else{
			return INQUiRY_TYPE_POI;
		}
	}
}

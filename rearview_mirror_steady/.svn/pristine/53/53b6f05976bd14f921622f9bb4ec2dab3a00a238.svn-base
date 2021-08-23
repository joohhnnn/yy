package com.txznet.txz.module.sim;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.txz.equipment_manager.EquipmentManager;
import com.txz.push_manager.PushManager;
import com.txz.push_manager.PushManager.SimCommonPush;
import com.txz.ui.data.UiData;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.equipment.UiEquipment.ServerConfig;
import com.txz.ui.equipment.UiEquipment.SimCertifyMsg;
import com.txz.ui.event.UiEvent;
import com.txz.ui.netflow.NetFlowData;
import com.txz.ui.netflow.NetFlowData.NetFlowSettingData;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.ui.dialog2.WinDialog.DialogBuildData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.activity.ReserveSingleTaskActivity2;
import com.txznet.txz.R;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.version.LicenseManager;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.ui.widget.SimAlertDialog;
import com.txznet.txz.ui.widget.SimAlertDialog.Builder;
import com.txznet.txz.ui.widget.SimAuthDialog;
import com.txznet.txz.ui.widget.SimEmptyDialog;
import com.txznet.txz.ui.widget.SimNoticeDialog;
import com.txznet.txz.ui.widget.SimWebDialog;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.DeviceInfo;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.runnables.Runnable2;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * 管理流量卡相关逻辑
 *
 * @author J
 *
 */
public class SimManager extends IModule {
	private final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private final static String ACTION_NAV_ONRESUME = "com.txznet.nav.autoamap.onresume";
    private final static String ACTION_MUSIC_ONRESUME = "com.txznet.music.status.foreground";
    private final static String ACTION_WEBCHAT_ONRESUME = "com.txznet.webchat.status.foreground";
    private final static String ACTION_SIM_FLOW_SERVICE = "com.txznet.sim.service.open";
    
    // web mode
	private final static int WEB_MODE_RECHARGE = 0;//充值
	private final static int WEB_MODE_SERVICE = 1;//服务包
	private final static int WEB_MODE_OFFLINE = 2;//离线充值
	private final static int WEB_MODE_CUSTOM = 3;//自定义
    
	// report
	public final static String TYPE_SIM_LOGIN = "login";
	public final static String TYPE_SIM_CHANGED = "change";
	public final static String TYPE_SIM_RESULT = "result";

	private final static String TAG = "Sim::";
	private static final SimManager sInstance = new SimManager();

	private UiEquipment.SC_SIM mSimInfo;
	private BroadcastReceiver mSimEventReceiver;
	private String mTargetPackageName = "";
	private SimNoticeDialog dialog;

	public long mFlowControl = 0;
	public int mLastFlow = -1;
	public float mAsrPercent = -1;//识别率
	public int mAsrDelay = -1;
	private int mPopOccasion = 0;
	
	private boolean bReportGuide = true;

	private boolean txzInit = false;

	private String mLastIccid;
	private String mPopUrl = null;
	private String mPopParam = null;

	public static SimManager getInstance() {
		return sInstance;
	}

	private SimManager() {

	}

	@Override
	public int initialize_BeforeStartJni() {
		// reg event
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE);

		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_SIM_RECHARGE_SELECT);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_SIM_RECHARGE_RESULT);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_SIM_FLOW_CHANGED);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
                UiEquipment.SUBEVENT_RESP_FLOW_THREASHOLD);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
        		UiEquipment.SUBEVENT_NOTIFY_SIM_PUSH);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
        		UiEquipment.SUBEVENT_NOTIFY_SIM_QUERY_FLOW_RESULT);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_SIM_AUTH_PUSH);

        regCommand("SIM_ASK_FLOW");
        regCommand("SIM_RECHANGE");

		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterInitSuccess() {
		log("sim manager inited");
		txzInit = true;
		// 初始化sim卡插拔事件监听
		initSimEventReceiver();
		checkSimInfo(mSimInfo);
        
        NetFlowSettingData netFlowSettingData = new NetFlowSettingData();
        netFlowSettingData.uint32MinValue = 0;
        netFlowSettingData.uint32MaxValue = 999;
        JNIHelper.sendEvent(UiEvent.EVENT_NET_FLOW, NetFlowData.SUBEVENT_FLOW_SETTING,netFlowSettingData);
        registerObservable();
        checkStatus();
		return super.initialize_AfterInitSuccess();
	}

	private void registerObservable() {
		RecorderWin.OBSERVABLE.registerObserver(new RecorderWin.StatusObervable.StatusObserver() {
			@Override
			public void onShow() {
				SimWebDialog.closeWebDialog("recordWin open");
			}

			@Override
			public void onDismiss() {
			}
		});
	}
	private void checkStatus() {
		//没有网的开机推送
		if(PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_FLOW_EMPTY_FLAG, 0) == 1){
			NetworkManager.getInstance().checkNetConnect(5000, null, new Runnable() {
			
				@Override
				public void run() {
					if(TextUtils.isEmpty(getNumber())){
						RecorderWin.speakTextWithClose(NativeData.getResString("RS_SIM_NO_EXIST"), null);
						return;
					}
					showWeb(WEB_MODE_OFFLINE, null, null);
				}
			});
		}

		//检测有无Sim卡
		int delay = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_SIM_EMPTY_CHECK_DELAY_SEC, 30);
		if(delay < 0){
			delay = 0;
		}else if(delay > 60 * 10){
			delay = 60 * 10;
		}
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				checkEmptySim();
			}
		}, delay * 1000);
		LogUtil.logd(TAG + "checkEmptySim delay = " + delay*1000);

		//检测实名认证状态
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				checkAuthentication();
			}
		}, delay * 1000);
	}

	/**
	 * 检查是否没有sim卡并进行弹窗
	 */
	private void checkEmptySim() {
		ServerConfig mServerConfig = ConfigManager.getInstance().getServerConfig();
		if (mServerConfig == null || mServerConfig.msgSimInfo == null
				|| mServerConfig.msgSimInfo.emptyCardConf == null) {
			LogUtil.logd(TAG + "checkEmptySim data is null");
			return;
		}
		LogUtil.logd(TAG + "checkEmptySim number = " + getNumber());
		if(getNumber() == null){
			UiEquipment.SimEmptyMsg msg = mServerConfig.msgSimInfo.emptyCardConf;
			if(TextUtils.isEmpty(msg.strMsg)){//和后台定的，strMsg为空就不弹
				LogUtil.logd(TAG + "checkEmptySim strMsg is empty");
				return;
			}
			if(msg.bCancel == null){
				msg.bCancel = true;
			}
			SimEmptyDialog.Builder builder = new SimEmptyDialog.Builder().setbExit(msg.bCancel)
					.setmContent(msg.strMsg).setmTips(msg.strAttach).setmUrl(msg.strUrl);
			builder.setCancelable(msg.bCancel);
			builder.setFullScreen(true);
			SimEmptyDialog dialog = new SimEmptyDialog(builder);
			dialog.show();
			LogUtil.logd(TAG + "checkEmptySim msg = " + msg.strMsg + ", bCancel = "
					+ msg.bCancel + ", strAttach = " + msg.strAttach + ", url = " + msg.strUrl);
		}
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
        switch (eventId) {
            case UiEvent.EVENT_ACTION_EQUIPMENT:

                switch (subEventId) {
                    case UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE:
                        handleSimInfoChanged(data);
                        break;

                    case UiEquipment.SUBEVENT_NOTIFY_SIM_RECHARGE_SELECT:
//                        showSimRechargeInfo(data);
                        break;

                    case UiEquipment.SUBEVENT_NOTIFY_SIM_RECHARGE_RESULT:
                        showSimRechargeResult(data);
                        break;
                    case UiEquipment.SUBEVENT_NOTIFY_SIM_FLOW_CHANGED:
                        handleSimFlowChanged(data);
                        break;
                    case UiEquipment.SUBEVENT_RESP_FLOW_THREASHOLD:
                        handleRespFlowThreashold(data);
                        break;
                    case UiEquipment.SUBEVENT_NOTIFY_SIM_PUSH:
                    	handleSimCommonPush(data);
                    	break;
                    case UiEquipment.SUBEVENT_NOTIFY_SIM_QUERY_FLOW_RESULT:
                    	showQueryFlowResult(data);
                    	break;
					case UiEquipment.SUBEVENT_NOTIFY_SIM_AUTH_PUSH:
						handleSimAuthPush(data);
						break;
                    default:
                        break;
                }

                break;

            default:
                break;
        }

		return super.onEvent(eventId, subEventId, data);
	}

	private void handleSimAuthPush(byte[] data) {
		LogUtil.logd(TAG + "handleSimAuthPush");
		if (data == null) {
			return;
		}
		PushManager.PushCmd_SimAuthNotify pushCmdSimAuthNotify = null;
		try {
			pushCmdSimAuthNotify = PushManager.PushCmd_SimAuthNotify.parseFrom(data);
		} catch (InvalidProtocolBufferNanoException e) {
		}
		if (pushCmdSimAuthNotify == null) {
			return;
		}
		LogUtil.logd(TAG + "handleSimAuthPush bCancelWnd = "
				+ pushCmdSimAuthNotify.bCancelWnd + ", url = "
				+ (pushCmdSimAuthNotify.strUrl == null ? "null" : new String(pushCmdSimAuthNotify.strUrl))
				+ ", bCancelable = " + pushCmdSimAuthNotify.bCancelable + ", limit = "
				+ pushCmdSimAuthNotify.uint32Limit);
		if (pushCmdSimAuthNotify.bCancelWnd != null && pushCmdSimAuthNotify.bCancelWnd) {
//			PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_SIM_AUTH_CANCEL_TIME, 0);
			SimAuthDialog.closeAuthDialog("auth success");
			PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_SIM_AUTH_SUCCESS_NUM, getNumber());
		}
		if (pushCmdSimAuthNotify.strUrl != null
				&& pushCmdSimAuthNotify.bCancelable != null
				&& pushCmdSimAuthNotify.uint32Limit != null) {
			showAuthDialog(new String(pushCmdSimAuthNotify.strUrl),
					pushCmdSimAuthNotify.bCancelable, pushCmdSimAuthNotify.uint32Limit);
		}
	}

	/**
	 * 处理查询流量返回结果
	 * @param data
	 */
    private void showQueryFlowResult(byte[] data) {
    	if(!RecorderWin.isOpened()) {
    		return;
		}
    	EquipmentManager.Resp_SIMQueryData pbSimQueryData = null;
    	try {
			pbSimQueryData = EquipmentManager.Resp_SIMQueryData.parseFrom(data);
		} catch (InvalidProtocolBufferNanoException e) {
			LogUtil.loge(TAG+"EquipmentManager.Resp_SIMQueryData.parseFrom fail");
		}
    	if(pbSimQueryData == null || pbSimQueryData.strNoticeText == null){
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(NativeData.getResString( "RS_VOICE_ASR_QUERY_FAIL"), null);
			return;
    	}
    	
    	if(pbSimQueryData.strNoticeText != null && pbSimQueryData.strNoticeText.contains("M")){
    		String ttsText = pbSimQueryData.strNoticeText.replace("M", "兆");
    		AsrManager.getInstance().setNeedCloseRecord(false);
    		RecorderWin.speakTextNotEqualsDisplay(ttsText, pbSimQueryData.strNoticeText);
    	}else{
    		AsrManager.getInstance().setNeedCloseRecord(false);
			RecorderWin.speakTextWithClose(pbSimQueryData.strNoticeText, null);
    	}
		if(pbSimQueryData.strQrcodeUrl != null){
			JSONObject json = new JSONObject();
			try {
				json.put("type", 3);
				json.put("qrCode", new String(pbSimQueryData.strQrcodeUrl));
			} catch (JSONException e) {
			}
			RecorderWin.showData(json.toString());
		}
		
    }

	private void handleSimCommonPush(byte[] data) {
    	LogUtil.logd(TAG+"handleSimCommonPush");
		if (TXZPowerControl.isEnterReverse()) {
			TXZPowerControl.setmLastSimCommonPushData(data);
			return;
		}
    	PushManager.SimCommonPush pbSimCommonPush = null;
    	try {
			pbSimCommonPush = PushManager.SimCommonPush.parseFrom(data);
		} catch (InvalidProtocolBufferNanoException e) {
			LogUtil.loge(TAG+"PushManager.SimCommonPush.parseFrom fail");
			return;
		}
    	if(pbSimCommonPush == null || pbSimCommonPush.uint32Type == null){
    		return;
    	}
    	LogUtil.logd(TAG+"type = "+pbSimCommonPush.uint32Type+", tts = "+new String(pbSimCommonPush.strTts)+", data = "+new String(pbSimCommonPush.strData));
    	if(PushManager.SIM_PUSH_TYPE_GUIDE == pbSimCommonPush.uint32Type){
    		showGuide(pbSimCommonPush);
    	}else if(PushManager.SIM_PUSH_TYPE_DIALOG_WEB == pbSimCommonPush.uint32Type){
    		showDialogGuide(pbSimCommonPush);
    	}
    	
    }

	/**
	 * windText:显示的文本
	 * confirmText:确认按键的文本
	 * cancelText:取消按键的文本
	 * url:确认按键跳转的url
	 * @param pbSimCommonPush
	 */
	private void showDialogGuide(SimCommonPush pbSimCommonPush) {
		log("showDialogGuide");
		if(pbSimCommonPush.strTts != null){
			TtsManager.getInstance().speakText(new String(pbSimCommonPush.strTts));
		}
		if(pbSimCommonPush.strData != null){
			JSONBuilder jsonBuilder = new JSONBuilder(pbSimCommonPush.strData);
			String text = jsonBuilder.getVal("windText", String.class, "");
			if(TextUtils.isEmpty(text)){
				log("dialog text is null");
				return;
			}
			String sureText = jsonBuilder.getVal("confirmText", String.class, "确认");
			String cancelText = jsonBuilder.getVal("cancelText", String.class, "取消");
			final String url = jsonBuilder.getVal("url", String.class, "");
			final String param = jsonBuilder.getVal("param", String.class, "");
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss("new dialog show");;
			}
			dialog = new SimNoticeDialog(new DialogBuildData());
			dialog.setPositiveBtn(sureText, new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss("click");
					showWeb(WEB_MODE_CUSTOM, url, param);
				}
			}, sureText);
			dialog.setNegativeBtn(cancelText, null, cancelText);
			dialog.setContents(text);
			dialog.showImediately();
		}
		
	}

	private void showGuide(PushManager.SimCommonPush pbSimCommonPush) {
		LogUtil.logd(TAG+"showGuide");
		if(pbSimCommonPush.strTts != null){
			TtsManager.getInstance().speakText(new String(pbSimCommonPush.strTts));
		}
		if(pbSimCommonPush.strData != null){
			try {
				JSONObject json = new JSONObject(new String(pbSimCommonPush.strData));
				log("strData = "+json.toString());
				if(json.has("close")){
					int close = json.getInt("close");
					if(0 == close){
						bReportGuide = true;
					}else if(1 == close){
						bReportGuide = false;
					}
				}
				
				String url = "";
				String param = "";
				if(json.has("url")){
					url = json.getString("url");
				}
				if(TextUtils.isEmpty(url)){
					return;
				}
				if(json.has("param")){
					param = json.getString("param");
				}
				showWeb(WEB_MODE_CUSTOM, url, param);
			} catch (JSONException e) {
				LogUtil.loge(TAG+"showGuide data parse error");
			}
			
		}
	}

	private void handleRespFlowThreashold(byte[] data) {
        UiEquipment.Resp_flow_Threshold respFlowThreshold = null;
        try {
            respFlowThreshold = UiEquipment.Resp_flow_Threshold.parseFrom(data);
        } catch (InvalidProtocolBufferNanoException e) {
            LogUtil.loge(TAG, e);
            e.printStackTrace();
        }
        if (null != respFlowThreshold && null != respFlowThreshold.retCode && respFlowThreshold.retCode != 0) {
            LogUtil.d(TAG + " resp flow threashold success");
            AsrManager.getInstance().setNeedCloseRecord(true);
            RecorderWin.speakTextWithClose(NativeData.getResString("RS_SIM_SET_THREASHOLD_SUCCESS"), null);
        } else {
            LogUtil.d(TAG + " resp flow threashold error");
            AsrManager.getInstance().setNeedCloseRecord(true);
            RecorderWin.speakTextWithClose(NativeData.getResString("RS_SIM_SET_THREASHOLD_FAILED"), null);
        }
    }

	private void handleSimFlowChanged(byte[] data) {
		log("flow changed");
		if (TXZPowerControl.isEnterReverse()) {
			TXZPowerControl.setmLastSimFlowChangeData(data);
			return;
		}
		UiEquipment.Resp_FlowInfo info = null;
		try {
			info = UiEquipment.Resp_FlowInfo.parseFrom(data);
		} catch (InvalidProtocolBufferNanoException e) {
			LogUtil.loge(TAG + "InvalidProtocolBufferNanoException " + e.toString());
		}
		if(info == null){
			return;
		}
		mFlowControl = info.uint64Control;
		mLastFlow = info.uint32Flow;
		log("flow:" + info.uint32Flow + " notice:" + info.strNoticeText + "control:" + info.uint64Control);
		if(info.popWndMsg != null && info.popWndMsg.popOccasion != null){
			mPopOccasion = info.popWndMsg.popOccasion;
			log("occasion:" + mPopOccasion);
		}
		if(info.strPopParam != null){
			mPopParam = new String(info.strPopParam);
		}
    	if(info.strPopUrl != null && !TextUtils.isEmpty(mPopUrl = new String(info.strPopUrl))){
    		log("popUrl:"+mPopUrl);
            entrySimRechargeWithoutFlow(info.strNoticeText);
    	}else if(!TextUtils.isEmpty(info.strNoticeText)){
    		TtsManager.getInstance().speakText(info.strNoticeText);
    	}
    	if(info.flowEmptyFlag != null && info.flowEmptyFlag){
    		log("flowEmptyFlag:"+info.flowEmptyFlag);
    		PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_FLOW_EMPTY_FLAG, 1);
    	}else{
    		PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_FLOW_EMPTY_FLAG, 0);
    	}
    	
	}
	

    
    public void entrySimRechargeWithoutFlow(String noticeText) {
    	String resText = NativeData.getResString("RS_VOICE_SIM_WITHOUT_FLOW_RECHARGE");
    	if(!TextUtils.isEmpty(noticeText)){
    		resText = noticeText;
    	}
    	AsrManager.getInstance().setNeedCloseRecord(true);
    	RecorderWin.speakTextWithClose(resText, false, new Runnable() {
			
			@Override
			public void run() {
				NetworkManager.getInstance().checkNetConnect(2000, new Runnable() {
					
					@Override
					public void run() {
						if(!TextUtils.isEmpty(mPopUrl)){
							showWeb(WEB_MODE_CUSTOM, mPopUrl, mPopParam);
						}else{
							showWeb(WEB_MODE_RECHARGE, null, null);
						}
					}
				}, new Runnable() {
					
					@Override
					public void run() {
						showWeb(WEB_MODE_OFFLINE, null, null);
					}
				});
			}
		});
    	
    }

    public int onCommand(String cmd) {
        if ("SIM_ASK_FLOW".equals(cmd)) {
        	if(TextUtils.isEmpty(getNumber())){
        		RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"), null);
        	}else{
        		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_SIM_QUERY_FLOW);
        	}
        } else if ("SIM_RECHANGE".equals(cmd)) {
        	UiEquipment.ServerConfig mServerConfig = ConfigManager.getInstance().getServerConfig();
			if (mServerConfig == null
					|| mServerConfig.bDataPartner == null
					|| mServerConfig.bDataPartner == false
					|| mServerConfig.msgSimInfo == null
					|| TextUtils.isEmpty(getNumber())) {
				RecorderWin.speakTextWithClose(
						NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"),
						null);
			} else {
				recharge(mServerConfig);
			}
        }
        return super.onCommand(cmd);
    }

    /**
     * 充值
     */
	private void recharge(UiEquipment.ServerConfig mServerConfig) {
		ReportUtil.doReport(new ReportUtil.Report.Builder().setType("recharge").buildSimReport());
		if(mServerConfig.msgSimInfo.uint32State != null && mServerConfig.msgSimInfo.uint32State != UiEquipment.SC_SIM_LOGIN){
			if(mServerConfig.msgSimInfo.uint32State == UiEquipment.SC_SIM_BOUND){
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_ASR_BINDED_SIM"), null);
			}else if(mServerConfig.msgSimInfo.uint32State == UiEquipment.SC_SIM_USE_OUT){
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_ASR_ANNUL_SIM"), null);
			}else{
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_ASR_ILLEGALITY_SIM"), null);
			}
			return;
		}
		log("SIM_RECHANGE");
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose("将为您打开充值页面", null);
		NetworkManager.getInstance().checkNetConnect(2000, new Runnable() {
			
			@Override
			public void run() {
				log("net true.");
				showWeb(WEB_MODE_RECHARGE, null, null);
			}
		}, new Runnable() {
			
			@Override
			public void run() {
				log("net false.");
				showWeb(WEB_MODE_OFFLINE, null, null);
			}
		});
		
		return;
	}
	
	/**
	 * 访问网络
	 * @param WEB_MODE Url(WEB_MODE_CUSTOM)
	 */
	private void showWeb(int mode, String customUrl, String param){
		String time = NativeData.getMilleServerTime().uint64Time+"";
	    String str = getNumber()+""+NativeData.getUID()+""+time+ProjectCfg.getEncryptKey();
		String sign = MD5Util.generateMD5(str);
		String test = ProjectCfg.getTestFlag() ? "1":"0";
		DisplayMetrics metrics = GlobalContext.get().getResources().getDisplayMetrics();
		DecimalFormat decimalFormat = new DecimalFormat(".0");
		String dpiscale = decimalFormat.format(metrics.scaledDensity);
		LogUtil.logd(TAG + "set init scale = " + dpiscale);
		String url = "";
	    String suffix = "";
	    switch (mode) {
        case WEB_MODE_RECHARGE:
            url = "http://thirdparty.txzing.com/sim/txz/device/index.html?";
            suffix = "#/recharge";
            break;
        case WEB_MODE_SERVICE:
            url = "http://thirdparty.txzing.com/sim/txz/device/index.html?";
            suffix = "#/";
            break;
        case WEB_MODE_OFFLINE:
            url = "file:///android_asset/offline.html?";
            suffix = "#/";
            break;
        case WEB_MODE_CUSTOM:
            url = customUrl;
            if(param == null){
                param = "";
            }
            suffix = "#/"+param;
            break;
        default:
            break;
        }
	    url = url +"iccid="+getNumber()+"&uid="+NativeData.getUID()+"&time="+time+"&sign="+sign+"&dpiscale="+dpiscale+"&test="+test+suffix;
		if(TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_SIM_WEB_ACTIVITY, false)){
			ReserveSingleTaskActivity2.showWeb(GlobalContext.get(), url, null);
		}else{
			SimWebDialog.showWeb(GlobalContext.get(), url, null);
		}
	}

	private String getAuthUrl(String url){
		if(url == null){
			return null;
		}
		String iccid = getNumber();
		if(TextUtils.isEmpty(iccid)){
			return null;
		}
		DisplayMetrics metrics = GlobalContext.get().getResources().getDisplayMetrics();
		DecimalFormat decimalFormat = new DecimalFormat(".0");
		String dpiscale = decimalFormat.format(metrics.scaledDensity);
		String suffix = "#/";
		if(!url.endsWith("?")){
			url = url + "?";
		}
		return url + "iccid="+iccid+"&uid="+NativeData.getUID()+"&dpiscale="+dpiscale+suffix;
	}
	
	private SimAlertDialog mSimAlarmDialog;

	private String mAlarmText;

	private int mTtsId;


	/**
	 * 取消tts播报任务
	 */
	public void cancel() {
		if (mTtsId != TtsManager.INVALID_TTS_TASK_ID) {
			TtsManager.getInstance().cancelSpeak(mTtsId);
			mTtsId = TtsManager.INVALID_TTS_TASK_ID;
		}
	}

	private void showSimRechargeResult(byte[] data) {
		log("showSimRechargeResult");
		if (TXZPowerControl.isEnterReverse()) {
			TXZPowerControl.setLastRechargeResult(data);
			return;
		}
		try {
			UiEquipment.PushCmd_NotifyDataRechargeResult result = UiEquipment.PushCmd_NotifyDataRechargeResult
					.parseFrom(data);

			ReportUtil.doReport(new ReportUtil.Report.Builder()
					.setType(SimManager.TYPE_SIM_RESULT)
					.putExtra("iccid", result.strSimIccid)
					.putExtra("state", result.uint32State).buildSimReport());

			if (TextUtils.isEmpty(mTargetPackageName) && !TextUtils.isEmpty(result.strNoticeText)) {
				String text = new String(result.strNoticeText);
				if(text.contains("M")){
					text = text.replace("M", "兆");
				}
				TtsUtil.speakText(text, new ITtsCallback() {

					@Override
					public void onEnd() {
						if(ActivityStack.getInstance().currentActivity() != null 
								&& ActivityStack.getInstance().currentActivity().getClass() == ReserveSingleTaskActivity2.class){
							ActivityStack.getInstance().currentActivity().finish();
						}else{
							SimWebDialog.closeWebDialog("recharge success");
						}
						super.onEnd();
					}
					
				});
				if (result.uint32State == 2) {
					Builder builder = new SimAlertDialog.Builder();
					builder.setTitle("充值成功").setContent(result.strNoticeText)
							.setIconId(R.drawable.sim_success).setDelay(8000)
							.setShowBtn(false).getDialog().showImediately();
				} else {
					Builder builder = new SimAlertDialog.Builder();
					builder.setTitle("充值失败").setContent(result.strNoticeText)
							.setIconId(R.drawable.sim_waning).setDelay(8000)
							.setShowBtn(false).getDialog().showImediately();
				}
			} else {
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("state", result.uint32State);
					jsonObject.put("msg", result.strNoticeText);
				} catch (JSONException e) {
					e.printStackTrace();
					LogUtil.loge(TAG + "recharge result", e);
				}
				ServiceManager.getInstance().sendInvoke(mTargetPackageName,
						"tool.sim.recharge.result",
						jsonObject.toString().getBytes(), null);
			}

		} catch (InvalidProtocolBufferNanoException e) {
			log("showSimRechargeResult model resolve encountered error: "
					+ e.toString());
		}
	}

	private void initSimEventReceiver() {
		if (null == mSimEventReceiver) {
			mSimEventReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					if (checkSimStatus()) {
						log("sim inserted");

						if (null == mSimInfo
								|| !checkStartsWith(getNumber(),
										mSimInfo.strSimIccid)) {
							log("sim changed, notify");
							notifySimChanged();
						}

					}
				}
			};

			IntentFilter filter = new IntentFilter(ACTION_SIM_STATE_CHANGED);
			GlobalContext.get().registerReceiver(mSimEventReceiver, filter);
		}
		
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				if(TextUtils.isEmpty(getNumber())){
					RecorderWin.speakTextWithClose(NativeData.getResString("RS_SIM_NO_EXIST"), null);
					return;
				}
				NetworkManager.getInstance().checkNetConnect(2000, new Runnable() {
					
					@Override
					public void run() {
						log("net true.");
						RecorderWin.dismiss();
						showWeb(WEB_MODE_SERVICE, null, null);
					}
				}, new Runnable() {
					
					@Override
					public void run() {
						log("net false.");
						RecorderWin.dismiss();
						showWeb(WEB_MODE_OFFLINE, null, null);
					}
				});
			}
		}, new IntentFilter(ACTION_SIM_FLOW_SERVICE));
		
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_NAV_ONRESUME);
		intentFilter.addAction(ACTION_MUSIC_ONRESUME);
		intentFilter.addAction(ACTION_WEBCHAT_ONRESUME);
		
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				LogUtil.logd("sim receive action = " + action);
				if(mSimInfo == null || !bReportGuide){
					return;
				}
				if(NetworkManager.getInstance().getNetType() == UiData.NETWORK_STATUS_WIFI){
				    return;
				}
				if(mSimInfo.uint32State == null || mSimInfo.uint32State != UiEquipment.SC_SIM_LOGIN){
					return;
				}
				if(TextUtils.equals(ACTION_NAV_ONRESUME, action)){
					ReportUtil.doReportImmediate(new ReportUtil.Report.Builder().setAction("open")
							.putExtra("iccid", getNumber()).setType("nav").buildSimReport());
				}else if(TextUtils.equals(ACTION_MUSIC_ONRESUME, action)){
					ReportUtil.doReportImmediate(new ReportUtil.Report.Builder().setAction("open")
							.putExtra("iccid", getNumber()).setType("music").buildSimReport());
				}else if(TextUtils.equals(ACTION_WEBCHAT_ONRESUME, action)){
					ReportUtil.doReportImmediate(new ReportUtil.Report.Builder().setAction("open")
							.putExtra("iccid", getNumber()).setType("webchat").buildSimReport());
				}
			}
		}, intentFilter);
		
	}

	/**
	 * 实名认证相关
	 *
	 * @param simInfo
	 */
	private void checkAuthentication() {
		ServerConfig mServerConfig = ConfigManager.getInstance().getServerConfig();
		if (mServerConfig == null || mServerConfig.msgSimInfo == null || mServerConfig.msgSimInfo.certiMsg == null) {
			log("checkAuthentication data is null");
			return;
		}
		if(!LicenseManager.getInstance().isInitLogin() && TextUtils.equals(PreferenceUtil
				.getInstance().getString(PreferenceUtil.KEY_SIM_AUTH_SUCCESS_NUM, "-1"),
				getNumber())){
			log("checkAuthentication iccid has been success");
			return;
		}
		SimCertifyMsg certifyMsg = mServerConfig.msgSimInfo.certiMsg;
		String url = "";
		boolean bCancel = true;
		int limit = 0;
		if (certifyMsg.redirectUrl != null) {
			url = new String(certifyMsg.redirectUrl);
		}
		if (certifyMsg.bCancel != null) {
			bCancel = certifyMsg.bCancel;
		}
		if (certifyMsg.uint32Limit != null) {
			limit = certifyMsg.uint32Limit;
		}
		log("checkAuthentication url = " + url + ",bCancel = " + bCancel
				+ ",limit = " + limit);
		if (TextUtils.isEmpty(url)) {
			return;
		}
		showAuthDialog(url, bCancel, limit);
	}

	private void showAuthDialog(String url, boolean bCancel, int limit) {
		if(url == null){
			return;
		}
		if (bCancel) {//可取消时将取消次数全部置为0
			PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_SIM_AUTH_CANCEL_TIME, 0);
			PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_SIM_AUTH_CANCEL_LIMIT, 0);
		} else {
			int lastLimit = PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_SIM_AUTH_CANCEL_LIMIT, 0);
			if(limit != lastLimit){
				PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_SIM_AUTH_CANCEL_LIMIT, limit);
				PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_SIM_AUTH_CANCEL_TIME, 0);
			}
		}
		int time = PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_SIM_AUTH_CANCEL_TIME, 0);
		//两个字段控制是否霸屏，bCancel为true时表示不霸屏且取消次数置0，bCancel为false时判断取消次数是否达到
		// 限制，bCancel为false时限制次数为0则直接霸屏
		LogUtil.logd(TAG + "auth dialog cancel time (" + time +") limit time (" + limit + ")");
		int cancelTime = 0;//传给后台的霸屏次数限制，0代表没有限制可取消，-1代表霸屏，其它值表示还有多少后会霸屏
		if(bCancel){
			cancelTime = 0;
		}else if(limit == 0){
			cancelTime = -1;
		}else if(time >= limit){
			cancelTime = -1;
		}else{
			cancelTime = limit - time;
//			bCancel = true;
		}
		String finalUrl = getAuthUrl(url);
		NetworkManager.getInstance().checkNetConnect(5000
				, new Runnable2<String, Integer>(finalUrl, cancelTime) {
					@Override
					public void run() {
						SimAuthDialog.showAuthDialog(mP1, mP2, true);
					}
				}, new Runnable2<String, Integer>(finalUrl, cancelTime) {
					@Override
					public void run() {
						SimAuthDialog.showAuthDialog(mP1, mP2, false);
					}
				});
	}

	public void handleSimInfoChanged(byte[] data) {
		log("handleSimInfoChanged");
		if (TXZPowerControl.isEnterReverse()) {
			TXZPowerControl.setLastSimInfoChangeData(data);
			return;
		}
		try {
			UiEquipment.ServerConfig serverConfig = UiEquipment.ServerConfig
					.parseFrom(data);
			log("sim partner:" + serverConfig.bDataPartner);
			UiEquipment.SC_SIM simInfo = serverConfig.msgSimInfo;

			// if (null == mSimInfo
			// || (mSimInfo.strSimIccid != null &&
			// !mSimInfo.strSimIccid.equals(simInfo.strSimIccid))) {
			checkSimInfo(simInfo);
			// }

			if (LicenseManager.getInstance().isInitLogin()) {
                showPopDialog(simInfo);
                // 记录后台返回的sim卡信息
				mSimInfo = simInfo;
				PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_SIM_AUTH_SUCCESS_NUM, "-1");
			}

//			checkAuthentication(simInfo);
		} catch (InvalidProtocolBufferNanoException e) {
			log("parse data error: " + e.toString());
		}
	}

	public String getNumber() {
		return DeviceInfo.getSimSerialNumber();
	}

	/**
	 * 检查sim卡信息，进行下一步操作
	 */
	private void checkSimInfo(UiEquipment.SC_SIM simInfo) {
		if (null == simInfo || !checkSimStatus()) {
			log("siminfo null, local iccid:" + getNumber());
			return;
		}
		if(mLastIccid!=null && !TextUtils.equals(mLastIccid, simInfo.strSimIccid)){
			PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_SIM_AUTH_CANCEL_TIME, 0);
		}
		mLastIccid = simInfo.strSimIccid;
		log("start compare sim info:");
		log("from DeviceInfo:SimSerialNumber:" + getNumber() + " Line1Number:" + DeviceInfo.getLine1Number());
		log("from remote:Iccid:" + simInfo.strSimIccid + " cardId:" + simInfo.strSimCardId);

		// 若sim卡信息不符，重新请求
		// sim卡iccid最后一位不可信，后台返回的只有前19位，所以只判断前面19位是否符合
		// if (!getNumber().startsWith(simInfo.strSimIccid))
		// {
		if (!checkStartsWith(getNumber(), simInfo.strSimIccid)) {
			log("sim info not match, notify");
			notifySimChanged();
			return;
		}

        log("txz inited?:" + txzInit + ", simInfo is null?" + (null == simInfo));
        if (!txzInit || null == simInfo) {
			return;
		}

		log("sim state: " + simInfo.uint32State);
		if (simInfo.uint32State != null) {
			log("state:" + simInfo.uint32State + " iccid:" + simInfo.strSimIccid + " cardid:" + simInfo.strSimCardId);
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType(SimManager.TYPE_SIM_LOGIN)
					.putExtra("state", simInfo.uint32State).putExtra("iccid", simInfo.strSimIccid)
					.putExtra("cardid", simInfo.strSimCardId).buildSimReport());
		}

	}

	/**
	 * 显示提示弹窗，例如非官方卡
	 *
	 * @param simInfo
	 */
	private void showPopDialog(UiEquipment.SC_SIM simInfo) {
        if(null == simInfo){
            return;
        }

        log("text:" + simInfo.strText);
        if (!TextUtils.isEmpty(simInfo.strText) && TextUtils.isEmpty(mTargetPackageName)
                && !TextUtils.equals(mAlarmText, simInfo.strText)) {
            TtsUtil.speakText(simInfo.strText);
        }
        mAlarmText = simInfo.strText;

		if (simInfo.popMsg != null) {
			String title = simInfo.popMsg.strHeader;
			String content = simInfo.popMsg.strMsg;
			log("dialog title:" + title + " content:" + content);

			if (mSimInfo != null && mSimInfo.popMsg != null) {
				if (TextUtils.equals(simInfo.popMsg.strHeader,
						mSimInfo.popMsg.strHeader)
						&& TextUtils.equals(simInfo.popMsg.strMsg,
								mSimInfo.popMsg.strMsg)) {
					return;
				}
			}

			if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
				return;
			}
			if (TextUtils.isEmpty(mTargetPackageName)) {
				// new SimAlarmDialog(title, content, R.drawable.sim_waning,
				// true).show();
				log("isCancel:" + simInfo.popMsg.bCancel);
				Boolean isCancel = simInfo.popMsg.bCancel == null ? false
						: simInfo.popMsg.bCancel;
				Builder builder = new SimAlertDialog.Builder();
				if (mSimAlarmDialog != null && mSimAlarmDialog.isShowing()
						&& mSimAlarmDialog.isDisableCancel() == isCancel) {
					mSimAlarmDialog.dismiss("new dialog show");
				}
				mSimAlarmDialog = builder.setTitle(title).setContent(content)
						.setIconId(R.drawable.sim_waning).setShowBtn(true)
						.setDisableCancel(!isCancel).getDialog();
				mSimAlarmDialog.show();

			} else {
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("title", title);
					jsonObject.put("msg", content);
					jsonObject.put("text", simInfo.strText);
				} catch (JSONException e) {
					e.printStackTrace();
					LogUtil.loge(TAG + "alarm", e);
				}
				ServiceManager.getInstance().sendInvoke(mTargetPackageName,
						"tool.sim.alarm", jsonObject.toString().getBytes(),
						null);
			}
		} else {
			log("dialog is null");
		}
	}

	public byte[] invokeTXZSim(final String packageName, String command,
			byte[] data) {
		if (TextUtils.isEmpty(command)) {
			return null;
		}
		if (command.equals("tool.set")) {
			mTargetPackageName = packageName;
		} else if (command.equals("tool.clear")) {
			mTargetPackageName = "";
		}
		return null;
	}

	/**
	 * 向后台发送sim卡改变消息
	 */
	private void notifySimChanged() {
		if (!TextUtils.isEmpty(getNumber())) {
			JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
					UiEquipment.SUBEVENT_NOTIFY_SIM_CHANGE);
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType(
					SimManager.TYPE_SIM_CHANGED).buildSimReport());
		}
	}

	/**
	 * 检查sim卡状态
	 *
	 * @return
	 */
	private boolean checkSimStatus() {
		TelephonyManager tm = (TelephonyManager) GlobalContext.get()
				.getSystemService(Service.TELEPHONY_SERVICE);

		int simState = tm.getSimState();
		log("checkSimStatus: " + simState);
		if (TelephonyManager.SIM_STATE_READY == simState) {
			return true;
		}

		return false;
	}

	/**
	 * equals to str1.startsWith(str2)
	 *
	 * @param str1
	 * @param str2
	 * @return
	 */
	private boolean checkStartsWith(String str1, String str2) {
		if (null == str1 || null == str2) {
			return false;
		}

		if (str1.startsWith(str2)) {
			return true;
		}

		return false;
	}

	private void log(String content) {
		LogUtil.logd(TAG + content);
	}

	/**
	 * 检测是否需要拦截操作
	 *
	 * @param type
	 *            操作标志位
	 * @return true 拦截 false 不拦截
	 */
	public boolean checkFlowControl(long type) {
		ServerConfig mServerConfig = ConfigManager.getInstance().getServerConfig();
		if(mServerConfig == null || mServerConfig.bDataPartner == null || !mServerConfig.bDataPartner 
				|| mServerConfig.msgSimInfo == null || mServerConfig.msgSimInfo.uint32State == null
				|| mServerConfig.msgSimInfo.uint32State != UiEquipment.SC_SIM_LOGIN){
			return false;
		}
		if (mLastFlow == 0
				&& NetworkManager.getInstance().getNetType() != UiData.NETWORK_STATUS_WIFI) {
			long control = mFlowControl;
			if ((control & type) != type) {
				if ((mPopOccasion & UiEquipment.ON_USE_DATA) == UiEquipment.ON_USE_DATA) {
					entrySimRechargeWithoutFlow(null);
				} else {
					String resText = NativeData
							.getResString("RS_VOICE_SIM_WITHOUT_FLOW_TIP");
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(resText, false, null);
				}
				ReportUtil.doReport(new ReportUtil.Report.Builder().setSessionId().setType("flow")
						.setAction("interrupt").buildSimReport());
				return true;
			}
		}
		return false;
	}


    /**
     * 设置流量提醒阈值
     *
     * @param threshold 需要设置的流量阈值
     */
    public void setFlowThreshold(int threshold) {
        if (threshold < 0) {
            LogUtil.d(TAG + "set flow threshold error:" + threshold);
            return;
        }
        if (null == mSimInfo || TextUtils.isEmpty(mSimInfo.strSimIccid)) {
        	RecorderWin.speakText(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"), null);
            LogUtil.d(TAG + "set flow threshold error: iccid is empty");
            return;
        }
        LogUtil.d(TAG + "set flow threshold " + threshold);
        UiEquipment.Req_flow_Threshold req_flow_threshold = new UiEquipment.Req_flow_Threshold();
        req_flow_threshold.iccid = mSimInfo.strSimIccid.getBytes();
        req_flow_threshold.flowThreshold = threshold;
        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_FLOW_THREASHOLD,
                MessageNano.toByteArray(req_flow_threshold));
    }
    
    /**
     * 是否是流量合作方
     */
    public boolean isDataPartner() {
    	ServerConfig mServerConfig = ConfigManager.getInstance().getServerConfig();
    	if(mServerConfig == null || mServerConfig.bDataPartner == null){
    		return false;
    	}
    	return mServerConfig.bDataPartner;
    }


//    private boolean isUseLocalUrl() {
//        SharedPreferences sp = GlobalContext.get().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
//        return sp.getBoolean(SP_KEY_FLOW_EMPTY_FLAG, false);
//    }


//    /**
//     * 没有白名单功能时，提供离线二维码扫码充值
//     */
//    private void showLocalRecharge() {
//        SharedPreferences sp = GlobalContext.get().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
//        String localUrl = sp.getString(SP_KEY_LCOAL_URL, null);
//        if (TextUtils.isEmpty(localUrl)) {
//            LogUtil.loge(TAG + "local url is empty");
//            return;
//        }
//        Builder builder = new SimAlertDialog.Builder();
//        SimAlertDialog dialog = builder.setTitle("流量充值").setContent("请扫描二维码进行充值")
//                .setQRCodeUrl(localUrl)
//                .setIconId(R.drawable.sim_success).setShowBtn(false)
//                .setShowBtn(true)
//                .getDialog();
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.show();
//    }
    
    
}
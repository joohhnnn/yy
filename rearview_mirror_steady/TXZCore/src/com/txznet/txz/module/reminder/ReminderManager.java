package com.txznet.txz.module.reminder;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.equipment_manager.EquipmentManager;
import com.txz.equipment_manager.EquipmentManager.Req_Reminder_Operation;
import com.txz.equipment_manager.EquipmentManager.Resp_Reminder_Operation;
import com.txz.push_manager.PushManager.PushCmd_Notify_Reminder;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;

import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.choice.OnItemSelectListener;
import com.txznet.txz.component.choice.list.ReminderWorkChoice;
import com.txznet.txz.component.choice.list.ReminderWorkChoice.ReminderItem;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.reminder.ReminderPushView;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.StatusObervable.StatusObserver;

public class ReminderManager extends IModule {
	private final static String TAG = "Reminder::";
	private static ReminderManager sIntance = new ReminderManager();

	private static String mDeleteId = "-1";
	private static int mOuttime = 5;
	
	private NavigateInfo mNavigateInfo = null;
	private byte[] pushData = null;
	private static String[] weeks = {"日","一","二","三","四","五","六"};
	private String mReminderTool = "";
		
	private Boolean mEnableReminder = null;
	private Boolean mCloseTimeView = false;
	//是否需要向后台清除提醒事项的标志
	private boolean isNeedClearReminder = true;

	public boolean getIsNeedClearReminder(){
		return isNeedClearReminder;
	}

	public void setIsNeedClearReminder(Boolean isNeedClearReminder){
		LogUtil.logd(TAG + "setIsNeedClearReminder to " + isNeedClearReminder);
		this.isNeedClearReminder = isNeedClearReminder;
	}


	//正在进行提醒事件的标志
	private boolean isReminding = false;

	public boolean getIsReminding(){
		return  isReminding;
	}

	public void setIsReminding(boolean isReminding){
		this.isReminding = isReminding;
	}

	//记录用户连续的第一次不说话超时。
	private boolean isFirstReminderEmpty = false;

	public boolean getFirstReminderEmpty() {
		return isFirstReminderEmpty;
	}

	public void setFirstReminderEmpty(boolean outTime){
		isFirstReminderEmpty = outTime;
	}

	public static ReminderManager getInstance(){
        return sIntance;
    }

    private ReminderManager(){

    }
    
    @Override
    public int initialize_BeforeStartJni() {
    	regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_REMINDER_OPERATE);
    	regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_REMINDER_PUSH);
    	regCommand("REMINDER_SEARCH");
    	regCommand("REMINDER_DELETE");
    	return super.initialize_BeforeStartJni();
    }
    


	@Override
	public int initialize_AfterInitSuccess() {
		RecorderWin.OBSERVABLE.registerObserver(new StatusObserver() {
			
			@Override
			public void onShow() {
				ReminderPushView.closePushView();
			}
			
			@Override
			public void onDismiss() {
				if (ReminderManager.getInstance().getIsReminding()) {
					ReminderManager.getInstance().setIsNeedClearReminder(true);
				}
				triggerPush();
			}
		});
		return super.initialize_AfterInitSuccess();
	}

	public byte[] invokeReminder(final String packageName, String command, byte[] data) {
		if(command.equals("txz.reminder.tool.set")){
			mReminderTool = packageName;
		}else if(command.equals("txz.reminder.tool.clear")){
			mReminderTool = "";
		}else if(command.equals("txz.reminder.push.nav")){
			naviReminder();
		} else if (command.equals("txz.reminder.set.enable")) {
			try {
				mEnableReminder = Boolean.parseBoolean(new String(data));
				LogUtil.logd("parse enableReminder : " + mEnableReminder);
			} catch (Exception e) {
				LogUtil.loge("parse enableReminder error : " + e.getLocalizedMessage());
			}
		}else if (command.equals("txz.reminder.closeView")) {
			mCloseTimeView = Boolean.parseBoolean(new String(data));
			if(mCloseTimeView){
				ReminderPushView.closePushView();
			}
		}
		return null;
	}

	@Override
    public int onCommand(String cmd) {
    	if(!enableReminderFunc()){
			String text = NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE");
			RecorderWin.speakText(text, null);
    		return 0;
		}
    	if("REMINDER_SEARCH".equals(cmd)){
    		searchReminder();
    	}else if("REMINDER_DELETE".equals(cmd)){
    		deleteReminder();
    	}
    	return super.onCommand(cmd);
    }
    
    @Override
    public int onEvent(int eventId, int subEventId, byte[] data) {
		if(!enableReminderFunc()){
			return 0;
		}
    	switch (eventId) {
		case UiEvent.EVENT_ACTION_EQUIPMENT:
			switch (subEventId) {
			case UiEquipment.SUBEVENT_RESP_REMINDER_OPERATE:
				handleOperateResp(data);
				break;
			case UiEquipment.SUBEVENT_NOTIFY_REMINDER_PUSH:
				handleReminderPush(data);
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
    
    /**
     * 处理push请求
     * @param data
     */
    private void handleReminderPush(byte[] data) {
    	if(TXZPowerControl.isEnterReverse()){
    		TXZPowerControl.setmLastReimderPushData(data);
    		return;
    	}
    	if(RecorderWin.isOpened() || !CallManager.getInstance().isIdle()){
    		pushData = data;
    		return;
    	}
    	pushData = null;
    	if(data == null || data.length == 0){
    		LogUtil.logd(TAG + "push message is null");
    		return;
    	}
    	PushCmd_Notify_Reminder pushReminder = null;
    	try {
			pushReminder =  PushCmd_Notify_Reminder.parseFrom(data);
		} catch (InvalidProtocolBufferNanoException e) {
			LogUtil.loge(e.getMessage());
		}
    	if(pushReminder == null || pushReminder.uint64Time == null || pushReminder.uint64Time == 0){
    		LogUtil.logd(TAG + "push message param error");
    		return;
    	}
    	long time = pushReminder.uint64Time;
    	String info = (pushReminder.strInfo == null ? "":pushReminder.strInfo);
    	JSONObject json = new JSONObject();
    	json.put("remind_time", time);
    	json.put("info", info);
    	sendReminderOperate(EquipmentManager.REMINDER_OPERATE_PULL, json.toString(), 0l);
    }
    
    /**
     * 发送请求
     * @param subEvent
     * @param strJson
     */
    private void sendReminderOperate(int operateType, String strJson, long delOperateId){
    	Req_Reminder_Operation req = new Req_Reminder_Operation();
    	req.uint32OperateType = operateType;
    	long id = 0l;
    	if(delOperateId != 0){
    		id = delOperateId;
    	}else{
    		id = NativeData.getMilleServerTime().uint64Time;
    	}
    	req.strOperateId = (id + "").getBytes();
    	if(!TextUtils.isEmpty(strJson)){
    		req.strJsonData = strJson.getBytes();
    	}
    	LogUtil.logd(TAG + "send message Id = " + id + ", type = " + req.uint32OperateType 
    			+ ", json = " + strJson); 
    	JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_REMINDER_OPERATE, req);
    }
    
	/**
     * 处理操作返回的结果
     * @param data
     */
    private void handleOperateResp(byte[] data) {
    	if(data == null || data.length == 0){
    		String text = NativeData.getResString("RS_VOICE_REMINDER_OPERATE_NET_FAIL");
    		RecorderWin.speakTextWithClose(text, null);
    		LogUtil.logd(TAG + "operate result is null");
    		return;
    	}
    	Resp_Reminder_Operation resp = null;
    	try {
			resp = Resp_Reminder_Operation.parseFrom(data);
		} catch (InvalidProtocolBufferNanoException e) {
			LogUtil.loge(e.getMessage());
		}
		//resp.uint32OperateStatus 状态码 1成功，2操作失败，3未开启提醒功能，4新建的提醒重复
    	if(resp == null || resp.uint32OperateType == null || resp.uint32OperateStatus == 2){
    		String text = NativeData.getResString("RS_VOICE_REMINDER_OPERATE_NET_FAIL");
    		RecorderWin.speakTextWithClose(text, null);
    		LogUtil.logd(TAG + "operate result is wrong");
    		return;
    	}
    	if(resp.uint32OperateStatus == 3){
			String text = NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE");
			RecorderWin.speakTextWithClose(text, null);
			LogUtil.logd(TAG + "operate result reminder disabled");
			return;
		}
    	LogUtil.logd(TAG + "operate result json = "
				+ (resp.strJsonData == null ? "null":new String(resp.strJsonData)) + ", status = "
				+ resp.uint32OperateStatus + ", type = " + resp.uint32OperateType + ", id = "
				+ (resp.strOperateId == null ? "null":new String(resp.strOperateId)));
    	switch (resp.uint32OperateType) {
		case EquipmentManager.REMINDER_OPERATE_ADD:
			if(!handleAddResult(resp)){
				String text = NativeData.getResString("RS_VOICE_REMINDER_OPERATE_FAIL");
	    		RecorderWin.speakText(text, null);
			}
			break;
		case EquipmentManager.REMINDER_OPERATE_DELETE:
			String text = NativeData.getResString("RS_VOICE_REMINDER_DELETED");
			RecorderWin.speakText(text, null);
			break;
		case EquipmentManager.REMINDER_OPERATE_SELECT:
			showSearchResult(resp);
			break;
		case EquipmentManager.REMINDER_OPERATE_PULL:
			showPushReminder(resp);
			break;
		default:
			break;
		}
    	
	}



    private boolean handleAddResult(Resp_Reminder_Operation resp) {
		if(resp == null || resp.strJsonData == null || TextUtils.isEmpty(new String(resp.strJsonData))){
			return false;
		}
		if(resp.uint32OperateStatus == 4){
			String text = NativeData.getResString("RS_VOICE_REMINDER_ADD_FAIL_REPEAT");
			RecorderWin.speakTextWithClose(text, null);
			return true;
		}
		JSONObject json = JSONObject.parseObject(new String(resp.strJsonData));
		if(json == null){
			return false;
		}
		Integer errno = json.getInteger("errno");
		if(errno != null && errno == EquipmentManager.EC_REMIND_TIME_ERALIER_THAN_NOW){
			String text = NativeData.getResString("RS_VOICE_REMINDER_ADD_FAIL_EARLIER");
			RecorderWin.speakText(text, null);
			return true;
		}else if(errno != null && errno != EquipmentManager.EC_REMIND_OK){
			return false;
		}
		String remindTime = json.getString("remind_time");
		String content = json.getString("content");
		String remindType = json.getString("remind_type");
		String answer =  json.getString("answer");
		if(TextUtils.isEmpty(remindTime)){
			return false;
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = null;
		try {
			date = formatter.parse(remindTime);
		} catch (ParseException e) {
			LogUtil.loge(e.getMessage());
		}
		if(date == null){
			return false;
		}
		Calendar calendar = Calendar.getInstance();
		int nowYear = calendar.get(Calendar.YEAR);
		calendar.setTime(date);
		String time = "";
		if(remindType != null){
			if(remindType.equals("DAY")){
				time = "每天" + calTimeBelong(calendar);
			}else if(remindType.equals("WORKDAY")){
				time = "每个工作日" + calTimeBelong(calendar);
			}else if(remindType.equals("WEEKEND")){
				time = "每个周末" + calTimeBelong(calendar);
			}else if(remindType.equals("WEEK")){
				time = "每周" + weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]+calTimeBelong(calendar);
			}else if(remindType.equals("MONTH")){
				time = "每月" + calendar.get(Calendar.DAY_OF_MONTH)+"号"+calTimeBelong(calendar);
			}else if(remindType.equals("YEAR")){
				time = "每年" + (calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "号" + calTimeBelong(calendar);
			}
		}
		if(time.equals("")){
			String strDate = calDateBelong(calendar);
			if (TextUtils.isEmpty(strDate)) {
				if (nowYear == calendar.get(Calendar.YEAR)) {
					strDate = (calendar.get(Calendar.MONTH) + 1) + "月"
							+ calendar.get(Calendar.DAY_OF_MONTH) + "号";
				} else {
					strDate = calendar.get(Calendar.YEAR) + "年"
							+ (calendar.get(Calendar.MONTH) + 1) + "月"
							+ calendar.get(Calendar.DAY_OF_MONTH) + "号";
				}
			}
			time = strDate + calTimeBelong(calendar);
		}


        AsrManager.getInstance().setNeedCloseRecord(true);
        RecorderWin.speakTextWithClose(answer,null);
		return true;
	}

	/**
     * 展示push消息
     * @param resp
     */
	private void showPushReminder(Resp_Reminder_Operation resp) {
		if(resp == null || resp.strJsonData == null){
			return;
		}
		JSONObject json = JSONObject.parseObject(new String(resp.strJsonData));
		if(json != null && json.containsKey("expired_time")){
			int outtime = json.getIntValue("expired_time");
			if(outtime > 0){
				mOuttime = outtime;
			}
		}
		String strJson = parsePushList(json.getString("list"));
		if(TextUtils.isEmpty(strJson)){
			return;
		}
		if(TextUtils.isEmpty(mReminderTool)){
			if(mCloseTimeView){
				return;//适配设置此时不响应后台推送。
			}
			ReminderPushView.showPushView(strJson);
		}else{
			ServiceManager.getInstance().sendInvoke(mReminderTool,
					"tool.reminder.push.show", strJson.getBytes(), null);
		}
	}

	/**
	 * 生成推送消息的播报文本
	 * @param strJson
	 * @return
	 */
	private String parsePushList(String strJson) {
		LogUtil.logd("parsePushList:"+strJson);
		JSONArray ja =JSONArray.parseArray(strJson);
		if(ja == null || ja.size() == 0){
			return "";
		}
		mNavigateInfo = null;
		StringBuilder strRes = new StringBuilder();
		StringBuilder strOuttimeRes = new StringBuilder();
		int count = 0;	//未超时数量
		int nullContent = 0;//未超时，内容为空的数量
		int outtimeCount = 0;//超时数量
		int positionCount = 0;//地址信息的数量
		int outtimeNullContent = 0;//内容为空的超时提醒数量
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (int i = 0; i < ja.size(); i++) {
			if(i >= 20){//最多拼装20条内容
				break;
			}
			try {
				JSONObject item = ja.getJSONObject(i);
				String content = item.getString("content");
				LogUtil.logd("parsePushList con:"+content);
				String strTime = item.getString("remind_time");
				String position = item.getString("position");
				Double logitude = item.getDouble("longitude");
				Double latitude = item.getDouble("latitude");
				Date date = sdf.parse(strTime);
				if(NativeData.getMilleServerTime().uint64Time - date.getTime() > mOuttime * 60 * 1000l){
					if(TextUtils.isEmpty(content)){
						outtimeNullContent++;
						continue;
					}
					outtimeCount ++;
					strOuttimeRes.append(content);
					strOuttimeRes.append("和");
				}else{
					if(TextUtils.isEmpty(content)){
						nullContent++;
						continue;
					}else {
						count ++;
						strRes.append(content);
						strRes.append("和");
					}
				}
				if(!TextUtils.isEmpty(position) && logitude != null && latitude != null){
					if(mNavigateInfo == null){
						mNavigateInfo = new NavigateInfo();
						mNavigateInfo.msgGpsInfo = new GpsInfo();
						mNavigateInfo.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
					}
					mNavigateInfo.msgGpsInfo.dblLat = latitude;
					mNavigateInfo.msgGpsInfo.dblLng = logitude;
					mNavigateInfo.strTargetName = position;
					positionCount ++;
				}
			} catch (ParseException e) {
				continue;
			}
		}
		String text = "";
		if(count > 0 || nullContent > 0){
			if (count == 0){
				count = 1;
				strRes.append("提醒内容为空");
			}
			String res = StringUtils.substringBeforeLast(strRes.toString(), "和");
			text = NativeData.getResString("RS_VOICE_REMINDER_PUSH")
					.replace("%COUNT%", count +"")
					.replace("%CONTENT%", res);
		}
		if(outtimeCount > 0 || outtimeNullContent > 0){
			if(outtimeCount == 0){
				outtimeCount += outtimeNullContent;
				strOuttimeRes.append("提醒内容为空");
			}
			if(count > 0){
				text = text + ",";
			}
			String outRes = StringUtils.substringBeforeLast(strOuttimeRes.toString(), "和");
			text = text + NativeData.getResString("RS_VOICE_REMINDER_PUSH_OUTTIME")
					.replace("%COUNT%", outtimeCount + "")
					.replace("%CONTENT%", outRes);
		}
		if(positionCount != 1){
			mNavigateInfo = null;
		}else{
			text = text + "," + NativeData.getResString("RS_VOICE_REMINDER_PUSH_NAV_TO");
		}
		JSONObject json = new JSONObject();
		json.put("text", text);
		json.put("nav", mNavigateInfo != null);
		return json.toString();
	}

	private void showSearchResult(final Resp_Reminder_Operation resp) {
		if(resp == null || resp.uint32OperateStatus != 1){
			LogUtil.logd(TAG + "search result error");
			String text = NativeData.getResString("RS_VOICE_REMINDER_ERROR_RESULT");
			RecorderWin.speakText(text, null);
			return;
		}
		if(resp.strJsonData == null || TextUtils.isEmpty(new String(resp.strJsonData))){
			LogUtil.logd(TAG + "search result empty");
			String text = NativeData.getResString("RS_VOICE_REMINDER_EMPTY_RESULT");
			RecorderWin.speakText(text, null);
			return;
		}
		LogUtil.logd(TAG + "search result "+ new String(resp.strJsonData));
		String strJson = new String(resp.strJsonData);
		JSONObject json = JSONObject.parseObject(strJson);
		JSONArray ja = json.getJSONArray("data");
		if(ja == null || ja.size() == 0){
			LogUtil.logd(TAG + "search result empty");
			String text = NativeData.getResString("RS_VOICE_REMINDER_EMPTY_RESULT");
			RecorderWin.speakText(text, null);
			return;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
		List<ReminderItem> reminders = new ArrayList<ReminderItem>();
		for (int i = 0; i < ja.size(); i++) {
			ReminderItem item = new ReminderItem();
			JSONObject obj = ja.getJSONObject(i);
			if(obj == null){
				continue;
			}
			item.content = obj.getString("content");
			if(TextUtils.isEmpty(item.content)){
				item.content ="内容为空";
			}
			item.id = obj.getString("id");
			item.position = obj.getString("position");
			String time = obj.getString("remind_time");
			try {
				Date date = sdf1.parse(time);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				String strDate = calDateBelong(calendar);
				if(TextUtils.isEmpty(strDate)){
					item.time = sdf.format(date);
				}else{
					item.time = strDate + " "+sdf2.format(date);
				}
			} catch (ParseException e) {
				LogUtil.logd(e.getMessage());
			}
			reminders.add(item);
		}
		CompentOption<ReminderItem> option = new CompentOption<ReminderItem>();
		option.setCallbackListener(new OnItemSelectListener<ReminderWorkChoice.ReminderItem>() {
			
			@Override
			public boolean onItemSelected(boolean isPreSelect, ReminderItem v,
					boolean fromPage, int idx, String fromVoice) {
				ChoiceManager.getInstance().clearIsSelecting();
				if(resp.strOperateId != null && TextUtils.equals(new String(resp.strOperateId), mDeleteId)){
					JSONObject json = new JSONObject();
					json.put("id", v.id);
					sendReminderOperate(EquipmentManager.REMINDER_OPERATE_DELETE, json.toString(), 0l);
				}else{
					String text = NativeData.getResString("RS_VOICE_REMINDER_SHOW_DETAILS") + v.content;
					RecorderWin.speakText(text, null);
					RecorderWin.setState(RecorderWin.STATE.STATE_END);
				}
				return true;
			}
		});
		ChoiceManager.getInstance().showReminderList(reminders, option);
	}

	/**
     * 发起删除
     */
    private void deleteReminder() {
    	long id = NativeData.getMilleServerTime().uint64Time;
    	mDeleteId = id + "";
    	sendReminderOperate(EquipmentManager.REMINDER_OPERATE_SELECT, "", id);
	}

	/**
     * 查询提醒
     */
    private void searchReminder() {
    	sendReminderOperate(EquipmentManager.REMINDER_OPERATE_SELECT, "", 0);
	}

	public boolean createReminder(JSONObject json) {
		//将提醒事件进行标志设为false，表示结束提醒事件
		isReminding =false;
//		isFirstReminderEmpty = false;
    	if(!enableReminderFunc()){
    		return false;
		}
        String content = "";
		String dateTime = "";
		String repeatType = "OFF";
		String answer = "";
		if(json.containsKey("content")){
			content = json.getString("content");
		}
		if(json.containsKey("dateTime")){
			dateTime = json.getString("dateTime");
		}
		if(json.containsKey("answer")){
			answer = json.getString("answer");
		}
		//腾讯返回的事件值可能为null，需要做转为空串处理。
		if(TextUtils.isEmpty(content)){
			content = "";
		}
        if(TextUtils.isEmpty(dateTime) || "UNKNOWN".equals(dateTime)){
        	RecorderWin.speakText(answer, null);
        	return true;
        }
        if(json.containsKey("repeatType")){
        	repeatType = json.getString("repeatType");
        }
        if("D1".equals(repeatType) || "D2".equals(repeatType) || "D3".equals(repeatType) || "D4".equals(repeatType) 
        		|| "D5".equals(repeatType) || "D6".equals(repeatType) || "D7".equals(repeatType)){
        	repeatType = "WEEK";
        }

		String vin = ProjectCfg.getVin();
		Log.d(TAG, "createReminder: vin = "+ vin);
        JSONObject createJson = new JSONObject();
        createJson.put("answer",answer);
        createJson.put("remind_time", dateTime);
        createJson.put("content", content);
        createJson.put("remind_type", repeatType);

		JSONObject extra_info = new JSONObject();
		extra_info.put("vin",vin);
		createJson.put("extra_info",extra_info);

		sendReminderOperate(EquipmentManager.REMINDER_OPERATE_ADD, createJson.toString(), 0);
        return true;
    }

	public boolean replyReminder(JSONObject json){
		if(!enableReminderFunc()){
			String text = NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE");
			RecorderWin.speakText(text, null);
			return true;
		}


		String strReplyText = json.getString("answer");
		RecorderWin.speakText(strReplyText, null);
		return true;
	}

	public boolean replyUnknow(){
		if(!enableReminderFunc()){
			String text = NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE");
			RecorderWin.speakText(text, null);
			return true;
		}
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_REMINDER_UNLNOW"),null);
		return true;
	}
	
	//处理提醒事件中用户超时不说话的场景
	public boolean  parseEmpty() {
			//判断是不是连续第一次用户不说话
			if(!isFirstReminderEmpty){
				isFirstReminderEmpty = true;
				String text = NativeData.getResString("RS_VOICE_REMINDER_OUTTIME_EMPTY");
				RecorderWin.speakText(text, null);
				return true;
			}
		AsrManager.getInstance().setNeedCloseRecord(true);
		String text = NativeData.getResString("RS_VOICE_REMINDER_EMPTY_EXIT");
		RecorderWin.speakTextWithClose(text, null);
		isFirstReminderEmpty = false;
		return true;
	}


	private String calTimeBelong(Calendar now) {
        String strRes = "";
        int time = now.get(Calendar.HOUR_OF_DAY);
        int time12 = now.get(Calendar.HOUR);
        if(time12 == 0){
        	time12 = 12;
        }
        if(time >= 0 && time < 6){
			strRes = "凌晨" + time + "点";
		} else if(time >= 6 && time < 8){
            strRes = "早上" + time + "点";
        }else if(time >= 8 && time < 12){
            strRes = "上午" + time + "点";
        }else if(time >= 12 && time < 13){
			strRes = "中午" + time12 + "点";
		} else if(time >= 13 && time < 18){
            strRes = "下午" + time12 + "点";
        }else if(time >= 18 && time < 24){
            strRes = "晚上" + time12 + "点";
        }
        int min = now.get(Calendar.MINUTE);
        if(min == 0){
        	return strRes;
        }
        return strRes + min + "分";
    }

    private String calDateBelong(Calendar calendar){
        String strRes = "";
//        Calendar now = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        today.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH),0,0,0);
        today.set(Calendar.MILLISECOND, 0);
        Calendar yesterday = (Calendar) today.clone();
        yesterday.set(Calendar.MILLISECOND, 0);
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        Calendar theDayBefore = (Calendar) today.clone();
        theDayBefore.set(Calendar.MILLISECOND, 0);
        theDayBefore.add(Calendar.DAY_OF_MONTH, -2);
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.set(Calendar.MILLISECOND, 0);
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        Calendar theDayAfter = (Calendar) today.clone();
        theDayAfter.set(Calendar.MILLISECOND, 0);
        theDayAfter.add(Calendar.DAY_OF_MONTH, 2);
        Calendar theDayAfterAfter = (Calendar) today.clone();
        theDayAfterAfter.set(Calendar.MILLISECOND, 0);
        theDayAfterAfter.add(Calendar.DAY_OF_MONTH, 3);
        if((calendar.after(theDayBefore) && calendar.before(yesterday)) || calendar.equals(theDayBefore)){
            strRes = "前天";
        }else if((calendar.after(yesterday) && calendar.before(today)) || calendar.equals(yesterday)){
            strRes = "昨天";
        }else if((calendar.after(today) && calendar.before(tomorrow)) || calendar.equals(today)){
            strRes = "今天";
        }else if((calendar.after(tomorrow) && calendar.before(theDayAfter)) || calendar.equals(tomorrow)){
            strRes = "明天";
        }else if((calendar.after(theDayAfter) && calendar.before(theDayAfterAfter)) || calendar.equals(theDayAfter)){
            strRes = "后天";
        }else{
            strRes = "";
        }
        return strRes;
    }
    
    public NavigateInfo getNavInfo(){
    	return mNavigateInfo;
    }
    
    public void triggerPush(){
    	if(pushData != null){
    		LogUtil.logd(TAG + "triggerPush");
    		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_REMINDER_PUSH, pushData);
    	}
    }

    public boolean enableReminderFunc(){
    	//1. 判断后台开关提醒事项
		UiEquipment.ServerConfig mServerConfig = ConfigManager.getInstance().getServerConfig();
		if (mServerConfig == null
				|| mServerConfig.uint64Flags == null
				|| ((mServerConfig.uint64Flags & UiEquipment.SERVER_CONFIG_FLAG_REMINDER) == 0)) {
			LogUtil.logd(TAG + "enableReminderFunc false");
			return false;
		}
		//2. 判断适配关闭了提醒事项
		if (mEnableReminder != null && !mEnableReminder) {
			LogUtil.logd(TAG + "sdk enableReminderFunc false");
			return false;
		}
		//3. 判断ui不支持，并且sdk未开启
		if (!WinManager.getInstance().isSupportNewContent() && mEnableReminder == null) {
			LogUtil.logd(TAG + "win enableReminderFunc false");
			return false;
		}

		LogUtil.logd(TAG + "enableReminderFunc true");
		return true;
	}


	public void naviReminder(){
		if(mNavigateInfo == null){
			LogUtil.logd(TAG + "naviReminder fail");
			return;
		}
		LogUtil.logd(TAG + "naviReminder");
		TtsManager.getInstance().speakText(NativeData.getResJson("RS_VOICE_REMINDER_HINT_NAV")
				.replace("%TAR%", mNavigateInfo.strTargetName), TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY);
		NavManager.getInstance().NavigateTo(mNavigateInfo, Poi.PoiAction.ACTION_NAVI);
	}

}

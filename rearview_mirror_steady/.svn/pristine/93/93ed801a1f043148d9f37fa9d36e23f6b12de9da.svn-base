package com.txznet.txz.module.ticket;

import android.text.TextUtils;

import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.equipment.UiEquipment.Req_Ticket;
import com.txz.ui.equipment.UiEquipment.Resp_Ticket;
import com.txz.ui.event.UiEvent;
import com.txz.ui.flight.FlightData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.util.DateUtils;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.activity.ReserveSingleTaskActivity1;
import com.txznet.sdk.TXZTicketManager;
import com.txznet.txz.component.choice.list.FlightWorkChoice;
import com.txznet.txz.component.choice.list.FlightWorkChoice.FlightDataBean;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.runnables.Runnable1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class TicketManager extends IModule {
	private String mCurrScene;
	private final static String TAG = "TicketManager::";
	static TicketManager sWeb = new TicketManager();

	private String mRemoteTrainTool = null;
    private String mRemoteFlightTool = null;
	private static final long DEF_TIMEOUT = 2000;
	private long mTrainTimeout = DEF_TIMEOUT;
	private long mFlightTimeout = DEF_TIMEOUT;
	private ArrayList<String> mTextTrainTaskIds = new ArrayList<String>();
    private ArrayList<String> mTextFlightTaskIds = new ArrayList<String>();
	private TicketResult[] mTrainResult = new TicketResult[2]; //0存放后台火车数据，1存放SDK火车数据
    private TicketResult[] mFlightResult = new TicketResult[2]; //0存放后台航班数据，1存放SDK航班数据
	public boolean mOnTrainFinish = false;
    public boolean mOnFlightFinish = false;
	private int trainTaskId = 0;

	public static TicketManager getInstance() {
		return sWeb;
	}

	private TicketManager() {
		RecorderWin.OBSERVABLE.registerObserver(new RecorderWin.StatusObervable.StatusObserver() {
			@Override
			public void onShow() {
			}

			@Override
			public void onDismiss() {
				mOnTrainFinish = true;
                mOnFlightFinish = true;
                AppLogic.removeBackGroundCallback(flightSDKTimeOut);
                mTextFlightTaskIds.clear();
				AppLogic.removeBackGroundCallback(trainSDKTimeOut);
				mTextTrainTaskIds.clear();
			}
		});
	}
	
	@Override
	public int initialize_AfterStartJni() {
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_TICKET);
		regEvent(UiEvent.EVENT_FLIGHT, FlightData.SUBEVENT_PLANE_QUERY);
		return super.initialize_AfterStartJni();
	}
	
	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		if (UiEvent.EVENT_ACTION_EQUIPMENT == eventId) {
			switch (subEventId) {
			case UiEquipment.SUBEVENT_RESP_TICKET:
				try {
					// 清除超时检测
					AppLogic.removeBackGroundCallback(mTimeoutRunnable);
					
					Resp_Ticket resp_Ticket = Resp_Ticket.parseFrom(data);
					LogUtil.logd("resp_ticket:" + resp_Ticket);
					if (resp_Ticket != null) {
						String url = null;
						String header = null;
						if (resp_Ticket.strTktUrl != null) {
							url = new String(resp_Ticket.strTktUrl);
						}
						if (resp_Ticket.strHeader != null) {
							header = new String(resp_Ticket.strHeader);
						}
						LogUtil.logd("resp_ticket url:" + url + ",header:" + header);
						if (!TextUtils.isEmpty(url)) {
							sendToTargetView(url, header);
							break;
						}
					}

					// 错误提示
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_TICKET_ERROR_FAIL"), null);
				} catch (Exception e) {
					LogUtil.loge(e.getMessage());
					e.printStackTrace();

					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_TICKET_ERROR_FAIL"), null);
				}
				break;
			default:
				break;
			}
		}else if (UiEvent.EVENT_FLIGHT == eventId){//TXZ后台航班语义处理
			switch(subEventId){
			case FlightData.SUBEVENT_PLANE_QUERY:
				handleFlightEvent(data);
				break;
			}
		}
		return super.onEvent(eventId, subEventId, data);
	}
	
	public boolean queryTicket(String extraJson) {
		ReportUtil.doReport(new ReportUtil.Report.Builder().setType("ticket").setAction("query")
				.setSessionId().buildCommReport());

		JSONBuilder builder = new JSONBuilder(extraJson);
		String scene = builder.getVal("scene", String.class);
		String dd = builder.getVal("departDate", String.class);
		String dt = builder.getVal("departTime", String.class);
		String destin = builder.getVal("destination", String.class);
		String origin = builder.getVal("origin", String.class);

		Integer ticketType = UiEquipment.TICKET_TYPE_DEFAULT;
		if ("flight".equals(scene)) {
			ticketType = UiEquipment.TICKET_TYPE_PLANE;
		} else if ("train".equals(scene)) {
			ticketType = UiEquipment.TICKET_TYPE_TRAIN;
		}
		mCurrScene = scene;

		UiEquipment.Req_Ticket req_Ticket = new Req_Ticket();
		if (!TextUtils.isEmpty(origin)) {
			req_Ticket.strBeginCity = origin.getBytes();
		}
		if (!TextUtils.isEmpty(destin)) {
			req_Ticket.strArrCity = destin.getBytes();
		}
		if (!TextUtils.isEmpty(dd)) {
			req_Ticket.strStartTime = dd.getBytes();
		}
		req_Ticket.uint32TktType = ticketType;
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_TICKET, req_Ticket);

		AppLogic.removeBackGroundCallback(mTimeoutRunnable);
		AppLogic.runOnBackGround(mTimeoutRunnable, 3000);
		return true;
	}
	
	public boolean queryFlight(String extraJson){
		String text = NativeData.getResString("RS_VOICE_FLIGHT_SEARCH_ERROR_TIP");
		RecorderWin.speakText(text, null);
		return true;
	}
	
	Runnable mTimeoutRunnable = new Runnable() {

		@Override
		public void run() {
			mCurrScene = "";
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_TICKET_ERROR_TIMEOUT"), null);
		}
	};
	
	private void sendToTargetView(String url, String headParams) {
		String type = "车票";
		if ("flight".equals(mCurrScene)) {
			type = "航班";
		} else if ("train".equals(mCurrScene)) {
			type = "火车票";
		}
		ReserveSingleTaskActivity1.showTicket(GlobalContext.get(), url, headParams);
		String spk = NativeData.getResPlaceholderString("RS_VOICE_WILL_SHOW_FLIGHT_TICKET", "%TICKET%", type);
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(spk, null);
	}
	
	//TXZ后台航班语义处理
	private void handleFlightEvent(byte[] data){
        mOnFlightFinish = false;
		FlightData.FlightInfos info = null;
		try {
			info = FlightData.FlightInfos.parseFrom(data);
		} catch (Exception e) {
		}
		
		do {
			if (info == null){
				break;
			}
			if (isOverTime(info)){
				String text = NativeData.getResString("RS_VOICE_FLIGHT_SEARCH_ERROR_OVERTIME");
				RecorderWin.speakText(text, null);
				return;
			}
			if (info.rptMsgTickets == null || info.rptMsgTickets.length == 0) {
				break;
			}
			LogUtil.logd("FlightInfos = " + info.toString());
			FlightDataBean dataBean = parseFlightInfo(info);
			if(dataBean == null){
				break;
			}
            mFlightResult[0] = new TicketResult();
            mFlightResult[0].state = TicketResult.STATE_SUCCESS;
            mFlightResult[0].taskId = trainTaskId+"";
            mFlightResult[0].flightInfo = dataBean;
            if(mRemoteTrainTool != null){
                mFlightResult[1] = new TicketResult();
                mFlightResult[1].state = TicketResult.STATE_REQUEST;
                mFlightResult[1].taskId = trainTaskId+"";
                JSONBuilder jsonBuilder = new JSONBuilder();
                jsonBuilder.put("taskid", trainTaskId+"");
                jsonBuilder.put("arrivalCity", dataBean.arrivalCity);
                jsonBuilder.put("departureDate", dataBean.date);
                jsonBuilder.put("departureCity", dataBean.departCity);
                ServiceManager.getInstance().sendInvoke(mRemoteFlightTool, TXZTicketManager.FLIGHT_CMD_PREFIX + TXZTicketManager.REQUEST_FLIGHT, jsonBuilder.toBytes(), null);
                AppLogic.removeBackGroundCallback(flightSDKTimeOut);
                flightSDKTimeOut.update(trainTaskId+"");
                AppLogic.runOnBackGround(flightSDKTimeOut, mFlightTimeout);
            }
			mTextFlightTaskIds.add(trainTaskId+"");
			trainTaskId++;
            showFlightList();

			return;
		} while (false);
		RecorderWin.speakText(NativeData.getResString("RS_VOICE_FLIGHT_SEARCH_ERROR_TIP"), null);
	}

	private synchronized void showFlightList(){
	    if(!mOnFlightFinish){
	        if(mRemoteFlightTool == null){
                if(mFlightResult[0].state == TicketResult.STATE_SUCCESS){
					mTextFlightTaskIds.remove(mFlightResult[0].taskId);
                    ChoiceManager.getInstance().showFlightList(mFlightResult[0].flightInfo, null);
                }
            }else {
                switch (mFlightResult[1].state){
                    case TicketResult.STATE_REQUEST:break;//等待
                    case TicketResult.STATE_SUCCESS:
                        if(!mTextFlightTaskIds.contains(mFlightResult[1].taskId)){
                            return;
                        }
                        mTextFlightTaskIds.remove((mFlightResult[1].taskId));
                        ChoiceManager.getInstance().showFlightList(mFlightResult[1].flightInfo, null);
                        break;
                    case TicketResult.STATE_ERROR:
                        if(!mTextFlightTaskIds.contains(mFlightResult[1].taskId)){
                            return;
                        }
						mTextFlightTaskIds.remove((mFlightResult[1].taskId));
                        ChoiceManager.getInstance().showFlightList(mFlightResult[0].flightInfo, null);
                        break;
                }
            }
        }
    }

    private synchronized void checkTrainResult() {
        if (!mOnTrainFinish) {
            if (mRemoteTrainTool == null){
                if(mTrainResult[0].state == TicketResult.STATE_SUCCESS){
                    processTrainTicket(mTrainResult[0].trainData);
                    mTextTrainTaskIds.remove(mTrainResult[0].taskId);
                }
            }else {
                switch (mTrainResult[1].state){
                    case TicketResult.STATE_REQUEST:break;//等待
                    case TicketResult.STATE_SUCCESS:
                        if(!mTextTrainTaskIds.contains(mTrainResult[1].taskId)){
                            return;
                        }
                        mTextTrainTaskIds.remove((mTrainResult[1].taskId));
                        processTrainTicket(mTrainResult[1].trainData);
                        break;
                    case TicketResult.STATE_ERROR:
                        if(!mTextTrainTaskIds.contains(mTrainResult[1].taskId)){
                            return;
                        }
                        processTrainTicket(mTrainResult[0].trainData);
                        mTextTrainTaskIds.remove((mTrainResult[0].taskId));
                        break;
                }
            }
        }

    }

	private boolean isOverTime(FlightData.FlightInfos info) {
		if(info == null || info.strDepartureDate == null){
			return false;
		}
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = sdf.parse(new String(info.strDepartureDate));
			float gapMonth = DateUtils.getGapMonthWithHalf(new Date(), date);
			if(gapMonth > 3.0f){
				return true;
			}
		} catch (ParseException e) {
		}
		return false;
	}

	private FlightDataBean parseFlightInfo(FlightData.FlightInfos info) {
		FlightDataBean flightData = new FlightDataBean();
		if(info != null){
			FlightData.TicketData[] ticketDatas = info.rptMsgTickets;
			if(ticketDatas == null || ticketDatas.length == 0){
				return null;
			}
			if(info.strDepartureCity != null){
				flightData.departCity = new String(info.strDepartureCity);
			}
			if(info.strArrivalCity != null){
				flightData.arrivalCity = new String(info.strArrivalCity);
			}
			if(info.strDepartureDate != null){
				String strDate = new String(info.strDepartureDate);
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					SimpleDateFormat sdf1 = new SimpleDateFormat("MM月dd日");
					Date date = sdf.parse(new String(info.strDepartureDate));
					strDate = sdf1.format(date);
				} catch (ParseException e) {
				}
				flightData.date = strDate;
			}
			List<FlightWorkChoice.FlightItem> items = new ArrayList<FlightWorkChoice.FlightItem>();
			for (FlightData.TicketData ticketData:ticketDatas) {
				FlightWorkChoice.FlightItem item = new FlightWorkChoice.FlightItem();
				if(ticketData == null){
					continue;
				}
				if(ticketData.strAirline != null){
					item.airline = new String(ticketData.strAirline);
				}
				if(ticketData.strArrivalAirportName != null){
					item.arrivalAirportName = new String(ticketData.strArrivalAirportName);
				}
				if(ticketData.strArrivalTime != null){
					item.arrivalTime = new String(ticketData.strArrivalTime);
				}
				if(ticketData.strArrivalTimeHm != null){
					item.arrivalTimeHm = new String(ticketData.strArrivalTimeHm);
				}
				if(ticketData.uint64ArrivalUnixTimestamp != null){
					item.arrivalTimestamp = ticketData.uint64ArrivalUnixTimestamp;
				}
				if(ticketData.strDepartAirportName != null){
					item.departAirportName = new String(ticketData.strDepartAirportName);
				}
				if(ticketData.strDepartTime != null){
					item.departTime = new String(ticketData.strDepartTime);
				}
				if(ticketData.strDepartTimeHm != null){
					item.departTimeHm = new String(ticketData.strDepartTimeHm);
				}
				if(ticketData.uint64DepartUnixTimestamp != null){
					item.departTimestamp = ticketData.uint64DepartUnixTimestamp;
				}
				if(ticketData.strEconomyCabinDiscount != null){
					item.economyCabinDiscount = new String(ticketData.strEconomyCabinDiscount);
				}
				if(ticketData.strFlightNum != null){
					item.flightNo = new String(ticketData.strFlightNum);
				}
				if(ticketData.uint32EconomyCabinPrice != null){
					item.economyCabinPrice = ticketData.uint32EconomyCabinPrice;
				}
				if(ticketData.uint32TicketCount != null){
					item.ticketCount = ticketData.uint32TicketCount;
				}
				item.addDate = calAddDate(ticketData.uint64DepartUnixTimestamp, ticketData.uint64ArrivalUnixTimestamp);
				items.add(item);
			}
			flightData.datas = items;
			
		}
		return flightData;
	}

	private String calAddDate(Long startTime, Long endTime) {
		String res = "";
		if(startTime != null && endTime != null){
			Date startDate = new Date(startTime);
			Date endDate = new Date(endTime);
			int count = DateUtils.getGapCount(startDate,endDate);
			if(count > 0 && count < 10){
				res = "+" + count + "天";
			}
		}
		return res;
	}

    private Runnable1<String> flightSDKTimeOut =  new Runnable1<String>(""){
        @Override
        public void run() {
            mFlightResult[1].taskId = mP1;
			mFlightResult[1].state = TicketResult.STATE_ERROR;
            LogUtil.logd(TAG + " SDKData mFlightTimeout " + mFlightTimeout);
            showFlightList();
        }
    };

	private Runnable1<String> trainSDKTimeOut =  new Runnable1<String>(""){
		@Override
		public void run() {
			mTrainResult[1].taskId = mP1;
			mTrainResult[1].state = TicketResult.STATE_ERROR;
			LogUtil.logd(TAG + " SDKData mTrainTimeout " + mTrainTimeout);
			checkTrainResult();
		}
	};

	//飞机票回调
    public byte[] invokeFlight(final String packageName, String command, final byte[] data) {
        String cmd = command.substring(TXZTicketManager.FLIGHT_INVOKE_PREFIX.length());
        if (TextUtils.equals(cmd, TXZTicketManager.SET_FLIGHT_TOOL)) {
            mRemoteFlightTool = packageName;
        } else if (TextUtils.equals(cmd, TXZTicketManager.CLEAR_FLIGHT_TOOL)) {
            mRemoteFlightTool = null;
        } else if (TextUtils.equals(cmd, TXZTicketManager.FLIGHT_SET_TIMEOUT)) {
            mFlightTimeout = new JSONBuilder(data).getVal("flightTimeout", Long.class, DEF_TIMEOUT);
            LogUtil.logd(TAG + " mFlightTimeout = " + mFlightTimeout);
        } else if (TextUtils.equals(cmd, TXZTicketManager.RESULT_FLIGHT)) {
            JSONBuilder json = new JSONBuilder(new String((data)));
            String taskId = json.getVal("taskid", String.class);
            if (mTextFlightTaskIds.contains(taskId)) {
                LogUtil.logd(TAG + "RESULT_FLIGHT");
                mFlightResult[1].state = TicketResult.STATE_SUCCESS;
                mFlightResult[1].taskId = taskId;
                FlightDataBean flightDataBean = parseFlightSDKData(new String(data));
                if(flightDataBean == null){
                    mFlightResult[1].state = TicketResult.STATE_ERROR;
                }else {
                    mFlightResult[1].state = TicketResult.STATE_SUCCESS;
                    mFlightResult[1].flightInfo = flightDataBean;
                }
                AppLogic.removeBackGroundCallback(flightSDKTimeOut);
                showFlightList();
            }
        }
        else if (TextUtils.equals(cmd, TXZTicketManager.ERROR_FLIGHT)) {
            JSONObject json = null;
            try {
                json = new JSONObject(new String(data));
                String taskId = json.getString("taskid");
                if (mTextFlightTaskIds.contains(taskId)) {
                    LogUtil.logd(TAG + "ERROR_FLIGHT");
                    mTrainResult[1].state = TicketResult.STATE_ERROR;
                    mTrainResult[1].taskId = taskId;
                    checkTrainResult();
                    AppLogic.removeBackGroundCallback(flightSDKTimeOut);
                }
            } catch (JSONException e) {
                LogUtil.logd(TAG + "ERROR_FLIGHT DATA error");
            }
        } else {
            LogUtil.logd(TAG + "ERROR_FLIGHT taskId error");
        }

        return null;
    }

    private FlightDataBean parseFlightSDKData(String data) {
        FlightDataBean flightInfos = new FlightDataBean();
        flightInfos.datas = new LinkedList<FlightWorkChoice.FlightItem>();
        JSONBuilder jsonBuilder = new JSONBuilder(data);
        flightInfos.arrivalCity = jsonBuilder.getVal("arrivalCity",String.class);
        flightInfos.departCity = jsonBuilder.getVal("departureCity",String.class);
        flightInfos.date = jsonBuilder.getVal("departureCity",String.class);
        JSONArray planeTicketList = jsonBuilder.getVal("planeTicketList", JSONArray.class);
        if(planeTicketList == null && planeTicketList.length() == 0){
            return null;
        }
        for(int i = 0; i < planeTicketList.length(); i++){
            FlightWorkChoice.FlightItem flightItem = new FlightWorkChoice.FlightItem();
            try {
                JSONObject jsonObject = planeTicketList.getJSONObject(i);
                flightItem.airline = jsonObject.getString("airline");
                flightItem.arrivalTimestamp = jsonObject.getLong("arrivalUnixTimestamp");
                flightItem.departTimestamp = jsonObject.getLong("departUnixTimestamp");
                flightItem.addDate  = calAddDate(flightItem.arrivalTimestamp,flightItem.departTimestamp);
                flightItem.departTime = jsonObject.getString("departTime");
                flightItem.arrivalTime = jsonObject.getString("arrivalTime");
                flightItem.arrivalTimeHm = jsonObject.getString("arrivalTimeHm");
                flightItem.departTimeHm = jsonObject.getString("departTimeHm");
                flightItem.economyCabinDiscount = jsonObject.getString("economyCabinDiscount");
                flightItem.economyCabinPrice = jsonObject.getInt("economyCabinPrice");
                flightItem.ticketCount = jsonObject.getInt("ticketCount");
                flightItem.flightNo = jsonObject.getString("flightNo");
                flightItem.arrivalAirportName = jsonObject.getString("arrivalAirportName");
                flightItem.departAirportName = jsonObject.getString("departAirportName");
                flightInfos.datas.add(flightItem);
            } catch (JSONException e) {
                return null;
            }
        }
        return flightInfos;
    }

    //火车票回调
	public byte[] invokeTrain(final String packageName, String command, final byte[] data) {
		String cmd = command.substring(TXZTicketManager.TRAIN_INVOKE_PREFIX.length());
		if (TextUtils.equals(cmd, TXZTicketManager.SET_TRAIN_TOOL)) {
			mRemoteTrainTool = packageName;
		} else if (TextUtils.equals(cmd, TXZTicketManager.CLEAR_TRAIN_TOOL)) {
			mRemoteTrainTool = null;
		} else if (TextUtils.equals(cmd, TXZTicketManager.TRAIN_SET_TIMEOUT)) {
			mTrainTimeout = new JSONBuilder(data).getVal("trainTimeout", Long.class, DEF_TIMEOUT);
			LogUtil.logd(TAG + " mTrainTimeout = " + mTrainTimeout);
		} else if (TextUtils.equals(cmd, TXZTicketManager.RESULT_TRAIN)) {
			JSONBuilder json = new JSONBuilder(new String((data)));
			String taskId = json.getVal("taskid", String.class);
			if (mTextTrainTaskIds.contains(taskId)) {
				LogUtil.logd(TAG + "RESULT_TRAIN");
				mTrainResult[1].state = TicketResult.STATE_SUCCESS;
				mTrainResult[1].taskId = taskId;
				mTrainResult[1].trainData = json.getJSONObject().toString();
				AppLogic.removeBackGroundCallback(trainSDKTimeOut);
				checkTrainResult();
			}
		}
		else if (TextUtils.equals(cmd, TXZTicketManager.ERROR_TRAIN)) {
			JSONObject json = null;
			try {
				json = new JSONObject(new String(data));
				String taskId = json.getString("taskid");
				if (mTextTrainTaskIds.contains(taskId)) {
					LogUtil.logd(TAG + "ERROR_TRAIN");
					mTrainResult[1].state = TicketResult.STATE_ERROR;
					mTrainResult[1].taskId = taskId;
					checkTrainResult();
					AppLogic.removeBackGroundCallback(trainSDKTimeOut);
				}
			} catch (JSONException e) {
					LogUtil.logd(TAG + "ERROR_TRAIN DATA error");
			}
		} else {
				LogUtil.logd(TAG + "ERROR_TRAIN taskId error");
		}

		return null;
	}

	public boolean checkTrainNLP(String jsonString){
		TrainTicketData trainTicketData = null;
		mOnTrainFinish = false;
		try {
			trainTicketData = TrainTicketData.objectFromData(jsonString);
		} catch (Exception e) {
			e.printStackTrace();
			JNIHelper.loge(TAG + "Gson parse data error: " + e.toString());
		}
		if (trainTicketData == null) {
			JNIHelper.logw(TAG + "trainTicketData=null");
			return false;
		}
		if (trainTicketData.result == null) {
			JNIHelper.logw(TAG + "result=null");
			return false;
		}
//		if (TextUtils.isEmpty(trainTicketData.origin)) {
//			return false;
//		}
		if (TextUtils.isEmpty(trainTicketData.destination)) {
//			String spk = NativeData.getResString("RS_TRAIN_TICKET_NO_DESTINATION");
			// 目前百度语义错误结果下的语义不提供解析数据，统一使用错误码
			String spk = NativeData.getResString("RS_TRAIN_TICKET_ERROR");
			RecorderWin.speakTextWithClose(spk, null);
			return true;
		}
		trainTicketData.preprocess();
		if (trainTicketData.result.ticketList == null || trainTicketData.result.ticketList.size() == 0) {
			String spk = NativeData.getResString("RS_TRAIN_TICKET_NO_RESULT");
			RecorderWin.speakTextWithClose(spk, null);
			return true;
		}
		if (TextUtils.isEmpty(trainTicketData.departDate)) {
			return false;
		}
		Date date;
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			date = simpleDateFormat.parse(trainTicketData.departDate);
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
		long differ = betweenDaysByNow(date);
		if (differ < 0) {
			String spk = NativeData.getResString("RS_TRAIN_TICKET_TIME_PASS");
			RecorderWin.speakTextWithClose(spk, null);
			return true;
		}
		if (differ > 30) {
			String spk = NativeData.getResString("RS_TRAIN_TICKET_TIME_OVERRUN");
			RecorderWin.speakTextWithClose(spk, null);
			return true;
		}
		JNIHelper.logd(TAG + "show train ticket list[" + trainTicketData.origin + "-" + trainTicketData.destination + "]=" + trainTicketData.result.ticketList.size());
		mTrainResult[0] = new TicketResult();
		mTrainResult[0].state = TicketResult.STATE_SUCCESS;
		mTrainResult[0].taskId = trainTaskId+"";
		mTrainResult[0].trainData = jsonString;
		if(mRemoteTrainTool != null){
			mTrainResult[1] = new TicketResult();
			mTrainResult[1].state = TicketResult.STATE_REQUEST;
			mTrainResult[1].taskId = trainTaskId+"";
			JSONBuilder jsonBuilder = new JSONBuilder();
			jsonBuilder.put("taskid", trainTaskId+"");
			jsonBuilder.put("origin", trainTicketData.origin);
			jsonBuilder.put("departDate", trainTicketData.departDate);
			jsonBuilder.put("destination", trainTicketData.destination);
			ServiceManager.getInstance().sendInvoke(mRemoteTrainTool, TXZTicketManager.TRAIN_CMD_PREFIX + TXZTicketManager.REQUEST_TRAIN, jsonBuilder.toBytes(), null);
			AppLogic.removeBackGroundCallback(trainSDKTimeOut);
			trainSDKTimeOut.update(trainTaskId+"");
			AppLogic.runOnBackGround(trainSDKTimeOut, mTrainTimeout);
		}
		mTextTrainTaskIds.add(trainTaskId+"");
		trainTaskId++;
		checkTrainResult();
		return true;
	}

	public boolean processTrainTicket(String jsonString) {
		TrainTicketData trainTicketData = null;
		try {
			trainTicketData = TrainTicketData.objectFromData(jsonString);
		} catch (Exception e) {
			if(mTrainResult[1] != null && mTrainResult[1].state == TicketResult.STATE_SUCCESS){
				processTrainTicket(mTrainResult[0].trainData);
			}
			e.printStackTrace();
			JNIHelper.loge(TAG + "Gson parse data error: " + e.toString());
		}
		if (trainTicketData == null) {
			JNIHelper.logw(TAG + "trainTicketData=null");
			if(mTrainResult[1] != null && mTrainResult[1].state == TicketResult.STATE_SUCCESS){
				processTrainTicket(mTrainResult[0].trainData);
			}
			return false;
		}
		if (trainTicketData.result == null) {
			JNIHelper.logw(TAG + "result=null");
			if(mTrainResult[1] != null && mTrainResult[1].state == TicketResult.STATE_SUCCESS){
				processTrainTicket(mTrainResult[0].trainData);
			}
			return false;
		}
//		if (TextUtils.isEmpty(trainTicketData.origin)) {
//			return false;
//		}
		if (TextUtils.isEmpty(trainTicketData.destination)) {
//			String spk = NativeData.getResString("RS_TRAIN_TICKET_NO_DESTINATION");
			// 目前百度语义错误结果下的语义不提供解析数据，统一使用错误码
			String spk = NativeData.getResString("RS_TRAIN_TICKET_ERROR");
			RecorderWin.speakTextWithClose(spk, null);
			return true;
		}
		trainTicketData.preprocess();
		if (trainTicketData.result.ticketList == null || trainTicketData.result.ticketList.size() == 0) {
			String spk = NativeData.getResString("RS_TRAIN_TICKET_NO_RESULT");
			RecorderWin.speakTextWithClose(spk, null);
			return true;
		}
		if (TextUtils.isEmpty(trainTicketData.departDate)) {
			if(mTrainResult[1] != null && mTrainResult[1].state == TicketResult.STATE_SUCCESS){
				processTrainTicket(mTrainResult[0].trainData);
			}
			return false;
		}
		Date date;
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			date = simpleDateFormat.parse(trainTicketData.departDate);
		} catch (ParseException e) {
			if(mTrainResult[1] != null && mTrainResult[1].state == TicketResult.STATE_SUCCESS){
				processTrainTicket(mTrainResult[0].trainData);
			}
			return false;
		}
		long differ = betweenDaysByNow(date);
		if (differ < 0) {
			String spk = NativeData.getResString("RS_TRAIN_TICKET_TIME_PASS");
			RecorderWin.speakTextWithClose(spk, null);
			return true;
		}
		if (differ > 30) {
			String spk = NativeData.getResString("RS_TRAIN_TICKET_TIME_OVERRUN");
			RecorderWin.speakTextWithClose(spk, null);
			return true;
		}
		JNIHelper.logd(TAG + "show train ticket list[" + trainTicketData.origin + "-" + trainTicketData.destination + "]=" + trainTicketData.result.ticketList.size());
        ChoiceManager.getInstance().showTrainList(trainTicketData, null);
		return true;
	}

	private long betweenDaysByNow(Date date) {
		Date now = new Date();
		Calendar fromCalendar = Calendar.getInstance();
		fromCalendar.setTime(now);
		fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
		fromCalendar.set(Calendar.MINUTE, 0);
		fromCalendar.set(Calendar.SECOND, 0);
		fromCalendar.set(Calendar.MILLISECOND, 0);

		Calendar toCalendar = Calendar.getInstance();
		toCalendar.setTime(date);
		toCalendar.set(Calendar.HOUR_OF_DAY, 0);
		toCalendar.set(Calendar.MINUTE, 0);
		toCalendar.set(Calendar.SECOND, 0);
		toCalendar.set(Calendar.MILLISECOND, 0);

		long differ = ((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
		return differ;
	}


	class TicketResult {
		/**
		 * 默认状态，没有使用
		 */
		static final int STATE_NONE = 0;
		/**
		 * 请求中状态
		 */
		static final int STATE_REQUEST = 1;
		/**
		 * 成功状态
		 */
		static final int STATE_SUCCESS = 2;
		/**
		 * 失败状态
		 */
		static final int STATE_ERROR = 3;
		String taskId = null;
		int state = STATE_NONE;
		String trainData = null;
        FlightDataBean flightInfo = null;
	}


}

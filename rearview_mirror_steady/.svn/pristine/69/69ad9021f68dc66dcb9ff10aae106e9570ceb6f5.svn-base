package com.txznet.sdk;

import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.FlightBean;
import com.txznet.sdk.bean.TrainBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TXZTicketManager {

    public static final String TRAIN_CMD_PREFIX = "txz.train.cmd.";//core->sdk
    public static final String TRAIN_INVOKE_PREFIX = "txz.train.invoke.";//sdk->core
    public static final String TRAIN_SET_TIMEOUT = "trainSetTimeout";
    public static final String SET_TRAIN_TOOL = "trainSetTool";
    public static final String CLEAR_TRAIN_TOOL = "clearTrainTool";
    public static final String REQUEST_TRAIN = "trainRequest";
    public static final String RESULT_TRAIN = "trainResult";
    public static final String ERROR_TRAIN= "trainError";

    public static final String FLIGHT_CMD_PREFIX = "txz.flight.cmd.";//core->sdk
    public static final String FLIGHT_INVOKE_PREFIX = "txz.flight.invoke.";//sdk->core
    public static final String FLIGHT_SET_TIMEOUT = "flightSetTimeout";
    public static final String SET_FLIGHT_TOOL = "flightSetTool";
    public static final String CLEAR_FLIGHT_TOOL = "clearFlightTool";
    public static final String REQUEST_FLIGHT = "flightRequest";
    public static final String RESULT_FLIGHT= "flightResult";
    public static final String ERROR_FLIGHT= "flightError";

    private int trainTaskId = 0;


    private static TXZTicketManager sInstance = new TXZTicketManager();

    private TXZTicketManager() {
    }

    public static TXZTicketManager getInstance() {
        return sInstance;
    }

    private Boolean enable;
    private TrainTool mTrainTool;
    private FlightTool mFlightTool;
    private Long mFlightTimeOut = null;
    private Long mTrainTimeout = null;


    /**
     * 重连时需要重新通知同行者的操作放这里。
     */
    void onReconnectTXZ() {

        if(enable != null){
            enableTheTicketSence(enable);
        }

        if(mTrainTool != null){
            setTrainTool(mTrainTool);
        }
        if(mTrainTimeout != null){
            setTrainTimeout(mTrainTimeout);
        }

        if(mFlightTool != null){
            setFlightTool(mFlightTool);
        }
        if(mFlightTimeOut != null){
            setFlightTimeout(mFlightTimeOut);
        }
        if(mCloseWaitingPayView != null){
            closeWaitingPayView(mCloseWaitingPayView);
        }
    }

    /**
     * 设置 火车车次查询工具（注意不是火车票功能）
     *
     * @param trainTool
     */
    public void setTrainTool(TrainTool trainTool) {
        mTrainTool = trainTool;
        if (mTrainTool == null) {
            TXZService.setCommandProcessor(TRAIN_CMD_PREFIX, null);
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, TRAIN_INVOKE_PREFIX + CLEAR_TRAIN_TOOL, null, null);
        } else {
            TXZService.setCommandProcessor(TRAIN_CMD_PREFIX, mTrainCommandProcessor);
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, TRAIN_INVOKE_PREFIX + SET_TRAIN_TOOL, null, null);
        }
    }

    /**
     * 设置 火车车次查询工具（注意不是火车票功能）
     *
     * @param flightTool
     */
    public void setFlightTool(FlightTool flightTool) {
        mFlightTool = flightTool;
        if (mFlightTool == null) {
            TXZService.setCommandProcessor(FLIGHT_CMD_PREFIX, null);
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, FLIGHT_INVOKE_PREFIX + CLEAR_FLIGHT_TOOL, null, null);
        } else {
            TXZService.setCommandProcessor(FLIGHT_CMD_PREFIX, mFlightCommandProcessor);
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, FLIGHT_INVOKE_PREFIX + SET_FLIGHT_TOOL, null, null);
        }
    }


    /**
     * 设置航班请求超时时间
     *
     * @param timeout
     */
    public void setFlightTimeout(long timeout) {
        mTrainTimeout = timeout;
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("flightTimeout", timeout);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, FLIGHT_INVOKE_PREFIX + FLIGHT_SET_TIMEOUT, jsonBuilder.toBytes(), null);
    }

    /**
     * 设置车票请求超时时间
     *
     * @param timeout
     */
    public void setTrainTimeout(long timeout) {
        mTrainTimeout = timeout;
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("trainTimeout", timeout);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, TRAIN_INVOKE_PREFIX + TRAIN_SET_TIMEOUT, jsonBuilder.toBytes(), null);
    }

    private TXZService.CommandProcessor mFlightCommandProcessor = new TXZService.CommandProcessor() {
        @Override
        public byte[] process(String packageName, String command, byte[] data) {
            if (TextUtils.equals(REQUEST_FLIGHT, command)) {
                if (mFlightTool != null) {
                    JSONBuilder jsonBuilder = new JSONBuilder(data);
                    String taskId = jsonBuilder.getVal("taskid", String.class);
                    String arrivalCity = jsonBuilder.getVal("arrivalCity", String.class);
                    String departDate = jsonBuilder.getVal("departureDate", String.class);
                    String departureCity = jsonBuilder.getVal("departureCity", String.class);
                    mFlightTool.requestTrain(arrivalCity, departDate, departureCity, new FlightRequestListener(taskId));
                }
            }
            return null;
        }
    };

    private TXZService.CommandProcessor mTrainCommandProcessor = new TXZService.CommandProcessor() {
        @Override
        public byte[] process(String packageName, String command, byte[] data) {
            if (TextUtils.equals(REQUEST_TRAIN, command)) {
                if (mTrainTool != null) {
                    JSONBuilder jsonBuilder = new JSONBuilder(data);
                    String taskId = jsonBuilder.getVal("taskid", String.class);
                    String origin = jsonBuilder.getVal("origin", String.class);
                    String departDate = jsonBuilder.getVal("departDate", String.class);
                    String departureStation = jsonBuilder.getVal("destination", String.class);
                    mTrainTool.requestTrain(origin, departDate, departureStation, new TrainRequestListener(taskId));
                }
            }
            return null;
        }
    };


    /**
     * 航班查询请求的回调接口
     */
    public static class FlightRequestListener {

        private String mTaskId;
        private boolean mOnFinish = false;
        public FlightRequestListener(String mTaskId) {
            this.mTaskId = mTaskId;
        }

        /**
         * 请求飞机航班成功回调
         *
         * @param flightData
         */
        public synchronized void onResult(FlightBean flightData){
            if (!mOnFinish) {
                mOnFinish = true;
                if (flightData != null) {
                    JSONBuilder jsonBuilder = parseFlight(flightData);
                    if(jsonBuilder == null){
                        onError("data parse NULL");
                    }else {
                        jsonBuilder.put("taskid", mTaskId);
                        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, FLIGHT_INVOKE_PREFIX + RESULT_FLIGHT, jsonBuilder.toBytes(), null);
                    }
                } else {
                    onError("data is null");
                }
            }

        }

        /**
         * 请求航班失败时的回调
         *
         * @param msg
         */
        public synchronized void onError(String msg) {
            if (!mOnFinish) {
                mOnFinish = true;
                JSONBuilder jsonBuilder = new JSONBuilder();
                jsonBuilder.put("taskid", mTaskId);
                jsonBuilder.put("msg", msg);
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, FLIGHT_INVOKE_PREFIX + ERROR_FLIGHT, jsonBuilder.toBytes(), null);
            }
        }

        private JSONBuilder parseFlight(FlightBean flightData){
                JSONBuilder jsonBuilder = new JSONBuilder();
                if(TextUtils.isEmpty(flightData.arrivalCity) &&
                        TextUtils.isEmpty(flightData.departureCity) &&
                        TextUtils.isEmpty(flightData.departureDate) &&
                        flightData.planeTickets == null &&
                        flightData.planeTickets.size() == 0){
                    return null;
                }
                jsonBuilder.put("arrivalCity", flightData.arrivalCity);
                jsonBuilder.put("departureCity", flightData.departureCity);
                jsonBuilder.put("departureDate", flightData.departureDate);
                JSONArray planeTicketList = new JSONArray();
                jsonBuilder.put("planeTicketList",planeTicketList);
                for(int i = 0; i < flightData.planeTickets.size(); i++){
                    JSONObject planeTicektJson = new JSONObject();
                    FlightBean.PlaneTicket planeTicket = flightData.planeTickets.get(i);
                    try {
                        planeTicektJson.put("airline", planeTicket.airline);
                        planeTicektJson.put("arrivalTime", planeTicket.arrivalTime);
                        planeTicektJson.put("arrivalTimeHm", planeTicket.arrivalTimeHm);
                        planeTicektJson.put("arrivalUnixTimestamp", planeTicket.arrivalUnixTimestamp);
                        planeTicektJson.put("departTime", planeTicket.departTime);
                        planeTicektJson.put("departTimeHm", planeTicket.departTimeHm);
                        planeTicektJson.put("departUnixTimestamp", planeTicket.departUnixTimestamp);
                        planeTicektJson.put("economyCabinPrice", planeTicket.economyCabinPrice);
                        planeTicektJson.put("flightNo", planeTicket.flightNo);
                        planeTicektJson.put("ticketCount", planeTicket.ticketCount);
                        planeTicektJson.put("arrivalAirportName", planeTicket.arrivalAirportName);
                        planeTicektJson.put("departAirportName", planeTicket.departAirportName);
                        planeTicektJson.put("economyCabinDiscount", planeTicket.economyCabinDiscount);
                        planeTicketList.put(planeTicektJson);
                    } catch (JSONException e) {
                       return null;
                    }
                }
            return jsonBuilder;
        }

    }

    /**
     * 火车车次查询请求的回调接口
     */
    public static class TrainRequestListener {

        private String mTaskId;
        private boolean mOnFinish = false;
        public TrainRequestListener(String mTaskId) {
            this.mTaskId = mTaskId;
        }

        /**
         * 请求火车车次成功回调
         *
         * @param trainData
         */
        public synchronized void onResult(TrainBean trainData){
            if (!mOnFinish) {
                mOnFinish = true;
                if (trainData != null) {
                    JSONBuilder jsonBuilder = parseTrain(trainData);
                    if(jsonBuilder == null){
                        onError("data parse NULL");
                    }else {
                        jsonBuilder.put("taskid", mTaskId);
                        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, TRAIN_INVOKE_PREFIX + RESULT_TRAIN, jsonBuilder.toBytes(), null);
                    }
                } else {
                    onError("data is null");
                }
            }
        }

        /**
         * 请求火车车次失败时的回调
         *
         * @param msg
         */
        public synchronized void onError(String msg) {
            if (!mOnFinish) {
                mOnFinish = true;
                JSONBuilder jsonBuilder = new JSONBuilder();
                jsonBuilder.put("taskid", mTaskId);
                jsonBuilder.put("msg", msg);
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, TRAIN_INVOKE_PREFIX + ERROR_TRAIN, jsonBuilder.toBytes(), null);
            }
        }

        private JSONBuilder parseTrain(TrainBean trainData){
            JSONBuilder jsonBuilder = new JSONBuilder();

            jsonBuilder.put("origin", trainData.origin);
            jsonBuilder.put("destination", trainData.destination);
            jsonBuilder.put("departDate", trainData.departDate);
            jsonBuilder.put("departTime", trainData.departTime);

            JSONObject jsonObject = new JSONObject();
            jsonBuilder.put("result", jsonObject);
            JSONArray jsonTrain= new JSONArray();
            try {
                jsonObject.put("ticketList",jsonTrain);
                TrainBean.ResultBean result = trainData.result;
                for(int i = 0 ;i < result.ticketList.size(); i++){
                    JSONObject trainObject = new JSONObject();
                    jsonTrain.put(trainObject);
                    trainObject.put("arrivalStation", result.ticketList.get(i).arrivalStation);
                    trainObject.put("arrivalTime", result.ticketList.get(i).arrivalTime);
                    trainObject.put("departureStation", result.ticketList.get(i).departureStation);
                    trainObject.put("departureTime", result.ticketList.get(i).departureTime);
                    trainObject.put("trainNo", result.ticketList.get(i).trainNo);
                    trainObject.put("daysApart", result.ticketList.get(i).daysApart);
                    trainObject.put("journeyTime", result.ticketList.get(i).journeyTime);
                    JSONArray seats = new JSONArray();
                    trainObject.put("trainSeats", seats);
                    for(int j = 0; j <  result.ticketList.get(i).trainSeats.size(); j++){
                        JSONObject seatJson = new JSONObject();
                        seats.put(seatJson);
                        TrainBean.TicketListBean.TrainSeatsBean seatsBean = result.ticketList.get(i).trainSeats.get(j);
                        seatJson.put("seatName", seatsBean.seatName);
                        seatJson.put("seatType", seatsBean.seatType);
                        seatJson.put("price", seatsBean.price);
                        seatJson.put("ticketsRemainingNumer", seatsBean.ticketsRemainingNumer);
                        seatJson.put("isBookable", seatsBean.isBookable);
                    }
                }
            } catch (JSONException e) {
               return null;
            }
            return jsonBuilder;
        }

    }

    /**
     * 飞机航班查询工具（注意不是飞机票功能）
     * */
    public static interface FlightTool{

        /**
         * @param arrivalCity                   到达城市
         * @param departureDate                 出发日期  2020-03-10
         * @param departureCity                 出发城市
         * @param flightRequestListener         当次请求结果回调的接口
         * **/
        public void requestTrain(String arrivalCity, String departureDate, String departureCity, FlightRequestListener flightRequestListener);
    }

    /**
     * 火车车次查询工具（注意不是火车票功能）
     * */
    public static interface TrainTool{

        /**
         * @param origin                    出发地
         * @param departDate                出发日期  2020-03-10
         * @param departureStation          到达站
         * @param trainRequestListener      当次请求结果回调的接口
         * **/
        public void requestTrain(String origin, String departDate, String departureStation, TrainRequestListener trainRequestListener);
    }

    /*
     *使用皮肤包UI1.0时，该接口才会生效
     *
     * 使用UI1.0时，需要将该接口设置为true，票务购买场景才能使用
     *
     * */
    public void  enableTheTicketSence(Boolean enable){
        this.enable =enable ;
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,"txz.ticket.enable", String.valueOf(this.enable).getBytes(), null);
    }

    private Boolean mCloseWaitingPayView = null;

    /*
     *
     *关闭等待支付的弹窗，true为关闭，同时不再打开，需要重新设置为false
     * */
    public void  closeWaitingPayView(boolean closeWaitingPayView){
        mCloseWaitingPayView = closeWaitingPayView;
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,"txz.ticket.closePushView",(mCloseWaitingPayView + "").getBytes(), null);
    }

}

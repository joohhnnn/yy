package com.txznet.txz.component.choice.list;

import com.txz.ui.data.UiData;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.viewfactory.data.QiwuTrainTicketPayViewData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResTicketPayPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.ticket.QiWuTicketManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QiWuFlightTicketPayWorkChioce extends WorkChoice<QiwuTrainTicketPayViewData, QiwuTrainTicketPayViewData.TicketPayBean> {

    String currenPayType = "";

    public QiWuFlightTicketPayWorkChioce(CompentOption<QiwuTrainTicketPayViewData.TicketPayBean> option) {
        super(option);
    }
/*
    @Override
    protected void updateDisplay(QiwuTrainTicketPayViewData items){
        // 重写刷新界面
        if (WinManager.getInstance().hasThirdImpl() || WinManager.getInstance().isRecordWin2()) {
            super.updateDisplay(items);
            return;
        }
        if (mPage == null) {
            return;
        }

    }*/

    @Override
    protected void onConvToJson(QiwuTrainTicketPayViewData ts, JSONBuilder jsonBuilder) {
        jsonBuilder.put("type", RecorderWin.TICKET_PAY);
        jsonBuilder.put("prefix", ts.title);
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < ts.mTicketBeans.size(); i++) {
            QiwuTrainTicketPayViewData.TicketPayBean bean = ts.mTicketBeans.get(i);
            String type = bean.payType;
            currenPayType = type;
            UiData.TTime currentTime = NativeData.getMilleServerTime();
            long currentTimeSS = currentTime.uint64Time / 1000;
            TtsManager.getInstance().cancelSpeak(QiWuTicketManager.mSpeechTaskId);
            AppLogic.removeBackGroundCallback(QiWuTicketManager.mPayTip);
            if(type.contains("flight")){
                jsonBuilder.put("prefix", "飞机票");
                QiWuTicketManager.payType ="机票";
                QiwuTrainTicketPayViewData.FlightTicketPayBean fp = (QiwuTrainTicketPayViewData.FlightTicketPayBean) bean;
                long expirationTimeL = Long.valueOf(fp.expirationTime);
                long expiration = expirationTimeL - currentTimeSS;
                if(expiration < 0){
                    expiration = 0;
                }
                String expirationTime =  String.valueOf(expiration);
                JSONObject obj = new JSONBuilder().put("passengerName", fp.passengerName).put("flightNo", fp.ticketNo).
                        put("ticketType", type).put("departureTime",fp.departureTime).put("sonAccount",fp.sonAccount).put("canRefund", fp.canRefund).
                        put("station",fp.station).put("endStation", fp.endStation).put("departureDate", fp.departureDate).put("orderUniqueId",fp.orderUniqueId).
                        put("expirationTime",expirationTime).put("orderId",fp.orderId).put("price", fp.price).put("phoneNum", fp.phoneNum).put("idNumber", fp.idNumber).
                        put("fuelSurcharge", fp.fuelSurcharge).put("payUrlZFB", fp.payUrlZFB).put("parUrlWX", fp.parUrlWX).build();
                jsonArray.put(obj);
                if(type.equals("flight") && expiration > 32){
                    AppLogic.runOnBackGround(QiWuTicketManager.mPayTip, 30000);
                }
            }else if(type.contains("train")){
                jsonBuilder.put("prefix", "火车票");
                QiWuTicketManager.payType = "火车票";
                QiwuTrainTicketPayViewData.TrainTicketPayBean fp = (QiwuTrainTicketPayViewData.TrainTicketPayBean) bean;
                long expirationTimeL = Long.valueOf(fp.expirationTime);
                long expiration = expirationTimeL - currentTimeSS;
                if(expiration < 0){
                    expiration = 0;
                }
                String expirationTime =  String.valueOf(expiration);
                JSONObject obj = new JSONBuilder().put("passengerName", fp.passengerName).put("ticketNo", fp.ticketNo).put("sonAccount",fp.sonAccount).put("canRefund", fp.canRefund).put("departureTime",fp.departureTime).
                        put("ticketType", type).put("costTime",fp.costTime).put("station",fp.station).put("orderId",fp.orderId).put("passengeId", fp.passengeId).put("orderUniqueId",fp.orderUniqueId).
                        put("endStation", fp.endStation).put("departureDate", fp.departureDate).put("expirationTime",expirationTime).put("phoneNum", fp.phoneNum).put("idNumber", fp.idNumber).
                        put("price", fp.price).put("seat", fp.seat).put("payUrlZFB", fp.payUrlZFB).put("parUrlWX", fp.parUrlWX).build();
                jsonArray.put(obj);
                if(type.equals("train") && expiration > 32){
                    AppLogic.runOnBackGround(QiWuTicketManager.mPayTip, 30000);
                }
            }
            QiWuTicketManager.currentSelectIndexId = bean.orderUniqueId;
        }
        jsonBuilder.put("cines", jsonArray);
        jsonBuilder.put("count", jsonArray.length());
        jsonBuilder.put("vTips",getTips());
    }

    private String getTips(){
        String tips = "";
        if (mPage != null) {
            if (mPage.getMaxPage() == (mPage.getCurrPage() + 1)) { //是最后一页或者只有一页
                if (mPage.getMaxPage() == 1) {
                   // tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE_ONE_PAGE");
                } else {
                    tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE_LAST");
                }
            } else if ((mPage.getCurrPage() + 1) == 1) {  //第一页
                tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE_FIRST");
            } else { //其他中间页
                tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE");
            }
        }
        return tips;
    }

    @Override
    protected String convItemToString(QiwuTrainTicketPayViewData.TicketPayBean item) {
        String type = item.payType;
        JSONBuilder obj;
        if(type.contains("flight")){
            QiwuTrainTicketPayViewData.FlightTicketPayBean fp = (QiwuTrainTicketPayViewData.FlightTicketPayBean) item;
            obj = new JSONBuilder().put("passengerName", fp.passengerName).put("flightNo", fp.ticketNo).put("ticektType", type).put("departureTime",fp.departureTime).put("phoneNum", fp.phoneNum).put("idNumber", fp.idNumber).
                    put("station",fp.station).put("endStation", fp.endStation).put("departureDate", fp.departureDate).put("expirationTime",fp.expirationTime).put("orderId",fp.orderId).put("orderUniqueId",fp.orderUniqueId).
                    put("price", fp.price).put("fuelSurcharge", fp.fuelSurcharge).put("payUrlZFB", fp.payUrlZFB).put("parUrlWX", fp.parUrlWX).put("sonAccount",fp.sonAccount).put("canRefund", fp.canRefund);
        }else{
            QiwuTrainTicketPayViewData.TrainTicketPayBean fp = (QiwuTrainTicketPayViewData.TrainTicketPayBean) item;

            obj = new JSONBuilder().put("passengerName", fp.passengerName).put("ticketNo", fp.ticketNo).put("ticektType", type).put("costTime",fp.costTime).put("phoneNum", fp.phoneNum).put("idNumber", fp.idNumber).put("departureTime",fp.departureTime).
                    put("station",fp.station).put("endStation", fp.endStation).put("departureDate", fp.departureDate).put("expirationTime",fp.expirationTime).put("passengeId", fp.passengeId).put("orderUniqueId",fp.orderUniqueId).
                    put("price", fp.price).put("seat", fp.seat).put("payUrlZFB", fp.payUrlZFB).put("parUrlWX", fp.parUrlWX).put("sonAccount",fp.sonAccount).put("orderId",fp.orderId).put("canRefund", fp.canRefund);
        }
        return obj.toString();
    }

    @Override
    protected void onSelectIndex(QiwuTrainTicketPayViewData.TicketPayBean ticketPayBean, boolean isFromPage, int idx, String fromVoice) {
        if(ticketPayBean.payType.equals("flight") || ticketPayBean.payType.equals("train")){
            if("取消订单".equals(fromVoice)){
                cancelOrd(ticketPayBean);
            }
        }
        if(ticketPayBean.payType.contains("Payed")){
            if("退票".equals(fromVoice)){
                cancelOrd(ticketPayBean);
            }
        }
    }

    private void cancelOrd(QiwuTrainTicketPayViewData.TicketPayBean ticketPayBean){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", ticketPayBean.payType);
            jsonObject.put("orderId", ticketPayBean.orderId);
            jsonObject.put("sonAccount", ticketPayBean.sonAccount);
            jsonObject.put("idNumber", ticketPayBean.idNumber);
            jsonObject.put("phoneNum", ticketPayBean.phoneNum);
            jsonObject.put("sonAccount", ticketPayBean.sonAccount);
            jsonObject.put("passengerName", ticketPayBean.passengerName);
            jsonObject.put("orderUniqueId", ticketPayBean.orderUniqueId);
            jsonObject.put("canRefund", ticketPayBean.canRefund);
            if(ticketPayBean.payType.contains("train")){
                QiwuTrainTicketPayViewData.TrainTicketPayBean tb = (QiwuTrainTicketPayViewData.TrainTicketPayBean)ticketPayBean;
                jsonObject.put("passengerId", tb.passengeId);
            }else {
                jsonObject.put("passengerId", "");
            }
            RecordWin2Manager.getInstance().operateView(
                    TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                    TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_TICKET_INFO_CANCEL, 0, 0,
                    1,jsonObject.toString());
        } catch (JSONException e) {
            return;
        }
    }

    @Override
    protected ResourcePage<QiwuTrainTicketPayViewData, QiwuTrainTicketPayViewData.TicketPayBean> createPage(QiwuTrainTicketPayViewData sources) {
        return new ResTicketPayPage(sources){

            @Override
            protected int numOfPageSize() {
                return getOption().getNumPageSize();
            }
        };
    }

    @Override
    protected boolean onCommandSelect(String type, String speech) {
        final String command = speech;
      /*  if ("CANCEL_ORD".equals(type)) {
            selectIndex(0,speech);
            return true;
        }else if("REFUND_ORD".equals(type)){
            selectIndex(0,speech);
            return true;
        }*/
        return super.onCommandSelect(type, command);
    }

    @Override
    protected void onAddWakeupAsrCmd(AsrUtil.AsrComplexSelectCallback acsc, QiwuTrainTicketPayViewData data) {
        super.onAddWakeupAsrCmd(acsc, data);
        /*acsc.addCommand("CANCEL_ORD", "取消订单");
        acsc.addCommand("REFUND_ORD", "退票");*/
    }


    @Override
    public String getReportId() {
        return "Ticket_pay_Select";
    }

    @Override
    public void stopTtsAndAsr() {
        JNIHelper.logd("stopTtsAndAsr");
        TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
        mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
        AsrManager.getInstance().cancel();
        if(isCoexistAsrAndWakeup()){
            TextResultHandle.getInstance().cancel();
        }
    }
    @Override
    protected void onClearSelecting() {
        TtsManager.getInstance().cancelSpeak(QiWuTicketManager.mSpeechTaskId);
        QiWuTicketManager.currentSelectIndexId = "-1";
        AppLogic.removeBackGroundCallback(QiWuTicketManager.mPayTip);
    }

}

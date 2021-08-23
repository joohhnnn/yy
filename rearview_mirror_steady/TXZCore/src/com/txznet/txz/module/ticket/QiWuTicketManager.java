package com.txznet.txz.module.ticket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.push_manager.PushManager;
import com.txz.ui.data.UiData;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.component.ticket.TicketOrdTimeComparator;
import com.txznet.txz.module.reminder.ReminderManager;
import com.txznet.txz.ui.widget.QiWuTicketCancellingDialog;
import com.txznet.txz.ui.widget.QiWuTicketConfirmDialog;
import com.txznet.txz.ui.widget.QiWuTicketReminderView;
import com.txznet.loader.AppLogic;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.widget.TicketUseInfoDialog;
import com.txznet.txz.ui.widget.TicketWaitingDialog;
import com.txznet.comm.ui.viewfactory.data.QiWuFlightTicketData;
import com.txznet.comm.ui.viewfactory.data.QiWuTrainTicketData;
import com.txznet.comm.ui.viewfactory.data.QiwuTrainTicketPayViewData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedList;

import static com.txznet.comm.ui.util.LayouUtil.getDrawable;

public class QiWuTicketManager extends IModule {

    public static String currentSelectIndexId = "";

    //当前主题皮肤包风格，作为票务场景的一个开关的标志位。目前只在全屏时打开，之后可继续增加
    public static boolean themeSwitch = true;

    public static int mSpeechTaskId;

    public static boolean mCloseWaitingPayView = false;

    //功能开关。
    private static boolean qiWuControl = true;

    public static String payType;
    //待支付的提醒播报任务。
    public static Runnable mPayTip = new Runnable() {
        @Override
        public void run() {
            TtsManager.getInstance().cancelSpeak(QiWuTicketManager.mSpeechTaskId);
            QiWuTicketManager.mSpeechTaskId = TtsManager.getInstance().speakText(NativeData.getResPlaceholderString("RS_VOICE_TIPS_QIWU_TICKET_PAY", "%payType%", payType));
        }
    };

    public void setQiWuControl(boolean qiWuControl){
        this.qiWuControl = qiWuControl;
    };

    private static boolean newContent = false;
    public void setNewContent(boolean newContent){
        if(newContent){
            currentSelectIndexId = "-1";
        }
        QiWuTicketManager.newContent = newContent;
    }

    private QiWuTicketManager(){
    }

    private static final String TAG = "QiWuTicketManager::";

    //当值为true表示这次请求需要向后台发送清除齐悟场景。
    private boolean beClearQIWu = true;

    public void setBeClearQIWu(boolean beClearQIWu){
        this.beClearQIWu = beClearQIWu;
    }

    public boolean getBeClearQiWu() {
        boolean tempClear = beClearQIWu;
        beClearQIWu = false;
        return tempClear;
    }

    private static QiWuTicketManager sIntance = new QiWuTicketManager();

    public static QiWuTicketManager getInstance() {
        return sIntance;
    }

    //窗口监听器
    StatusOb statusOb;

     abstract class StatusOb implements RecorderWin.StatusObervable.StatusObserver{

         public boolean isRegister  = false;

        @Override
        public void onShow() {
        }

     }

    @Override
    public int initialize_BeforeStartJni() {
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_QIWU_GET_USER_INFO);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_QIWU_ORDER);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_QIWU_CANCEL_ORDER);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_QIWU_REFUND_ORDER);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_PUSH_TXZ_NLP);
        return super.initialize_BeforeStartJni();
    }


    @Override
    public int initialize_AfterInitSuccess() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                //为了确保该窗体能一定在主线程里面进行初始化
                QiWuTicketCancellingDialog.getInstance();
                TicketWaitingDialog.getInstance();
                TicketUseInfoDialog.getInstance();
            }
        });

        return ERROR_SUCCESS;
    }

    @Override
    public int onEvent(int eventId, int subEventId, byte[] data) {

         switch (eventId){
             case UiEvent.EVENT_ACTION_EQUIPMENT:
                 switch (subEventId){
                     case UiEquipment.SUBEVENT_RESP_QIWU_GET_USER_INFO:
                         handleUserInfo(data);
                         break;
                     case UiEquipment.SUBEVENT_RESP_QIWU_CANCEL_ORDER:
                          handleCancelResults(data,"CANCEL");
                          break;
                     case UiEquipment.SUBEVENT_RESP_QIWU_REFUND_ORDER:
                          handleCancelResults(data,"REFUND");
                          break;
                     case UiEquipment.SUBEVENT_RESP_QIWU_ORDER:
                         handleCancelResults(data, "COMMIT");
                         break;
                     case UiEquipment.SUBEVENT_PUSH_TXZ_NLP:handleQRResults(data);break;
                 }
                 break;
         }

        return super.onEvent(eventId, subEventId, data);
    }

    private void showUseInfoDialog(){
        final TicketUseInfoDialog dialog = TicketUseInfoDialog.getInstance();
        if(currentSelectIndexId == dialog.getCurrentSelectIndexId() && RecorderWin.isOpened()){
            AppLogicBase.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });
        }
    }

    private void handleUserInfo(byte[] data){
        TicketUseInfoDialog dialog = TicketUseInfoDialog.getInstance();
        LogUtil.logd(TAG + "handleUserInfo");
        if (data == null) {
            LogUtil.logd(TAG + "handleUserInfo data = null");
            showUseInfoDialog();
            return;
        }
        PushManager.PushCmd_NLP pushCmdQRResults = null;
        try{
            pushCmdQRResults = PushManager.PushCmd_NLP.parseFrom(data);
        }catch (InvalidProtocolBufferNanoException e){}
        if(pushCmdQRResults == null){
            showUseInfoDialog();
            return;
        }
        LogUtil.logd(TAG + "handleUserInfo strJson = "
                + (pushCmdQRResults.strJson == null ? "null" : new String(pushCmdQRResults.strJson)));
        JSONObject resultsJson = null;
        if(pushCmdQRResults.strJson != null){
            try {
                resultsJson = new JSONObject(new String(pushCmdQRResults.strJson));
                JSONArray userJsonArray = resultsJson.getJSONArray("user_list");
                LinkedList<TicketUseInfoDialog.UseInfoBean> useInfoBeans = new LinkedList<TicketUseInfoDialog.UseInfoBean>();
                for(int i = 0; i < userJsonArray.length(); i++){
                    String phone = userJsonArray.getJSONObject(i).getString("phone");
                    String name = userJsonArray.getJSONObject(i).getString("passenger_name");
                    String idNum = userJsonArray.getJSONObject(i).getString("passenger_id_number");
                    String sonAcount = userJsonArray.getJSONObject(i).getString("son_account");
                    TicketUseInfoDialog.UseInfoBean useInfoBean = new TicketUseInfoDialog.UseInfoBean();
                    useInfoBean.idNumber = idNum;
                    useInfoBean.name = name;
                    useInfoBean.sonAccount = sonAcount;
                    useInfoBean.phone = phone;
                    useInfoBeans.add(useInfoBean);
                }
                dialog.setUseInfoBeanList(useInfoBeans);
                JSONObject lastUseJson = resultsJson.getJSONObject("last_order_info");
                String phone = lastUseJson.getString("phone");
                String name = lastUseJson.getString("passenger_name");
                String idNum = lastUseJson.getString("passenger_id_number");
                String sonAcount = lastUseJson.getString("son_account");
                TicketUseInfoDialog.UseInfoBean useInfoBean = new TicketUseInfoDialog.UseInfoBean();
                useInfoBean.idNumber = idNum;
                useInfoBean.name = name;
                useInfoBean.sonAccount = sonAcount;
                useInfoBean.phone = phone;
                dialog.setLastUseInfo(useInfoBean);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        showUseInfoDialog();
    }

    private void handleCancelResults(byte[] data,String cancelType){
        LogUtil.logd(TAG + "handleCancelResults");
        if (data == null) {
            LogUtil.logd(TAG + "handleQRresults data = null");      //收到后台数据异常，可能是超时，也可能是后台下发错误，统一处理为网络异常。
            if(QiWuTicketCancellingDialog.getInstance().isShowing()){
                AppLogicBase.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        QiWuTicketCancellingDialog.getInstance().dismiss();
                    }
                });

                RecorderWin.mSpeechTaskId = TtsManager.getInstance().speakText("网络异常，请稍后再试。");
            }
            return;
        }
        PushManager.PushCmd_NLP pushCmdQRResults = null;
        try{
            pushCmdQRResults = PushManager.PushCmd_NLP.parseFrom(data);
        }catch (InvalidProtocolBufferNanoException e){}
        if(pushCmdQRResults == null){
            return;
        }
        LogUtil.logd(TAG + "handleCancelResults strJson = "
                + (pushCmdQRResults.strJson == null ? "null" : new String(pushCmdQRResults.strJson)));
        JSONObject resultsJson = null;
        int code = -1;
        try {
            if(pushCmdQRResults.strJson != null){
                resultsJson = new JSONObject(new String(pushCmdQRResults.strJson));
                code = resultsJson.getInt("code");
                QiwuTrainTicketPayViewData mData = new QiwuTrainTicketPayViewData();
                CompentOption<QiwuTrainTicketPayViewData.TicketPayBean> option = new CompentOption<QiwuTrainTicketPayViewData.TicketPayBean>();
                if(code == 0){
                    if(cancelType.equals("COMMIT")){
                        return;//提交下单在这里只处理出错的情景，所以正常场景直接返回
                    }
                    if(!QiWuTicketCancellingDialog.getInstance().isShowing()){
                        return;
                    }
                    String ticketType = resultsJson.getString("order_type");
                    JSONObject orderJson = resultsJson.getJSONObject("order_info");
                    if("flight".equals(ticketType)){
                        JSONObject flightJson = resultsJson;
                        mData.title = "飞机票";
                        QiwuTrainTicketPayViewData.FlightTicketPayBean bean = getFightBean(orderJson);
                        if(cancelType.equals("CANCEL")){
                            bean.payType = "flightCANCEL";
                            option.setTtsText(NativeData.getResString("RS_VOICE_TIPS_QIWU_FLIGHT_CANCEL_ORD"));
                            String expirationTime = flightJson.getJSONObject("order_info").getString("expiration_time");//订单过期时间戳,单位：秒
                            bean.expirationTime =expirationTime;
                            mData.mTicketBeans.add(bean);
                            AppLogicBase.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    QiWuTicketCancellingDialog.getInstance().dismiss();
                                }
                            });
                            ChoiceManager.getInstance().showTicketPayList(mData, option);
                        }
                    }else {
                        mData.title = "火车票";
                        QiwuTrainTicketPayViewData.TrainTicketPayBean bean = getTrainBean(orderJson);
                        if(cancelType.equals("CANCEL")){
                            bean.payType = "trainCANCEL";
                            option.setTtsText(NativeData.getResString("RS_VOICE_TIPS_QIWU_TRAIN_CANCEL_ORD"));
                            String expirationTime = orderJson.getString("expiration_time");//订单过期时间戳,单位：秒
                            bean.expirationTime = expirationTime;
                            mData.mTicketBeans.add(bean);
                            AppLogicBase.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    QiWuTicketCancellingDialog.getInstance().dismiss();
                                }
                            });
                            ChoiceManager.getInstance().showTicketPayList(mData, option);
                        }
                    }
                }else{
                    handleQRErr(code, resultsJson);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleQRErr(int errCode, JSONObject resultsJson) throws JSONException {
            String errMsg = resultsJson.getString("msg");
        LogUtil.logd(TAG+"onErr code = "+ errCode+" errMsg = "+errMsg);
        if(RecorderWin.isOpened()){
                if(QiWuTicketCancellingDialog.getInstance().isShowing() || TicketWaitingDialog.getInstance().isShowing()){

                    TicketUseInfoDialog.getInstance().setAfterDissmiss(null);
                    AppLogicBase.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            QiWuTicketCancellingDialog.getInstance().dismiss();
                            TicketWaitingDialog.getInstance().dismiss();
                            TicketUseInfoDialog.getInstance().dismiss();
                        }
                    });
                    RecorderWin.open(errMsg);
                }
            }
    }

    //处理下单结果
    private void handleQRResults(byte[] data) {

        LogUtil.logd(TAG + "handleQRresults");
        if (data == null) {
            LogUtil.logd(TAG + "handleQRresults data = null");
            return;
        }

        PushManager.PushCmd_NLP pushCmdQRResults = null;
        try{
            pushCmdQRResults = PushManager.PushCmd_NLP.parseFrom(data);
        }catch (InvalidProtocolBufferNanoException e){}
        if(pushCmdQRResults == null){
            return;
        }

        LogUtil.logd(TAG + "handleQRResults strJson = "
                + (pushCmdQRResults.strJson == null ? "null" : new String(pushCmdQRResults.strJson)));
        JSONObject resultsJson = null;
        int action = 1;
        try {
            if(pushCmdQRResults.strJson != null){
                resultsJson = new JSONObject(new String(pushCmdQRResults.strJson));
            }

            if(resultsJson != null){
                action = resultsJson.getInt("action");
            }else {
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            QiwuTrainTicketPayViewData mData = new QiwuTrainTicketPayViewData();
            final CompentOption<QiwuTrainTicketPayViewData.TicketPayBean> option = new CompentOption<QiwuTrainTicketPayViewData.TicketPayBean>();
            switch (action){
                case 5:
                case 7:
                case 8:
                    String ticketType = resultsJson.getString("ticket_ype");
                    JSONObject orderJson = resultsJson.getJSONObject("order_info");
                    if("train".equals(ticketType)){
                        mData.title = "火车票";
                        final QiwuTrainTicketPayViewData.TrainTicketPayBean bean = getTrainBean(orderJson);

                        if(action == 5){
                            bean.payType = "train";
                            String taskId = orderJson.getString("order_unique_id");
                            option.setTtsText(NativeData.getResString("RS_VOICE_TIPS_QIWU_TRAIN_PAY"));
                            if(!taskId.equals(currentSelectIndexId)){

                                AppLogicBase.runOnUiGround(new Runnable() {
                                    @Override
                                    public void run() {
                                        final QiWuTicketReminderView.Data viewData = new QiWuTicketReminderView.Data("train_ord_icon","你有车票订单待支付", "关闭");
                                        final QiWuTicketReminderView.OnClickTitle onClickTitle = new QiWuTicketReminderView.OnClickTitle() {
                                            @Override
                                            public void onClickTitle() {
                                                QiwuTrainTicketPayViewData mData = new QiwuTrainTicketPayViewData();
                                                mData.mTicketBeans.add(bean);
                                                ChoiceManager.getInstance().showTicketPayList(mData, option);
                                            }
                                        };
                                        UiData.TTime currentTime = NativeData.getMilleServerTime();
                                        long currentTimeSS = currentTime.uint64Time / 1000;
                                        long expirationTimeL = Long.valueOf(bean.expirationTime);
                                        long outTime =  expirationTimeL - currentTimeSS;
                                        QiWuTicketReminderView.showPushView(viewData, onClickTitle, outTime);
                                    }
                                });
                                return;
                            }else if(!TicketWaitingDialog.getInstance().isShowing()){

                                AppLogicBase.runOnUiGround(new Runnable() {
                                    @Override
                                    public void run() {
                                        final QiWuTicketReminderView.Data viewData = new QiWuTicketReminderView.Data("train_ord_icon","你有车票订单待支付", "关闭");
                                        final QiWuTicketReminderView.OnClickTitle onClickTitle = new QiWuTicketReminderView.OnClickTitle() {
                                            @Override
                                            public void onClickTitle() {
                                                QiwuTrainTicketPayViewData mData = new QiwuTrainTicketPayViewData();
                                                mData.mTicketBeans.add(bean);
                                                ChoiceManager.getInstance().showTicketPayList(mData, option);
                                            }
                                        };
                                        UiData.TTime currentTime = NativeData.getMilleServerTime();
                                        long currentTimeSS = currentTime.uint64Time / 1000;
                                        long expirationTimeL = Long.valueOf(bean.expirationTime);
                                        long outTime =  expirationTimeL - currentTimeSS;
                                        QiWuTicketReminderView.showPushView(viewData, onClickTitle, outTime);
                                    }
                                });

                                return;
                            }
                        }else if(action == 7){
                            bean.payType = "trainPayed";
                            String taskId = orderJson.getString("order_unique_id");
                            if(!taskId.equals(currentSelectIndexId)){
                                return;
                            }
                            option.setTtsText("您已成功预订"+bean.departureDate.split("-")[0]+"月"+bean.departureDate.split("-")[1]+"日从"+bean.station+"开往"+bean.endStation+"的车票，祝您出行愉快！");
                        }else if(action == 8){
                            bean.payType = "trainREFUND";
                            String taskId = orderJson.getString("order_unique_id");
                            if(!taskId.equals(currentSelectIndexId)){
                                return;
                            }
                            option.setTtsText("您已成功退订"+bean.departureDate.split("-")[0]+"月"+bean.departureDate.split("-")[1]+"日从"+bean.station+"开往"+bean.endStation+"的车票。");
                        }
                        mData.mTicketBeans.add(bean);
                    }else if("flight".equals(ticketType)){
                        //订单状态(0：待支付；1：出票中；2： 待出行；3：已出行；5：退款中；6： 已退款；7：已取消；8：交易关闭；10:占座中；12： 退票中；13：出票失败退款中)
                        mData.title = "飞机票";
                        final QiwuTrainTicketPayViewData.FlightTicketPayBean bean = getFightBean(orderJson);

                        if(action == 5){
                            bean.payType = "flight";
                            option.setTtsText(NativeData.getResString("RS_VOICE_TIPS_QIWU_FLIGHT_PAY"));
                            String taskId = orderJson.getString("order_unique_id");
                            if(!taskId.equals(currentSelectIndexId)){
                                AppLogicBase.runOnUiGround(new Runnable() {
                                    @Override
                                    public void run() {
                                        QiWuTicketReminderView.Data viewData = new QiWuTicketReminderView.Data("flight_ord_icon","你有机票订单待支付", "关闭");
                                        QiWuTicketReminderView.OnClickTitle onClickTitle = new QiWuTicketReminderView.OnClickTitle() {
                                            @Override
                                            public void onClickTitle() {
                                                QiwuTrainTicketPayViewData mData = new QiwuTrainTicketPayViewData();
                                                mData.mTicketBeans.add(bean);
                                                ChoiceManager.getInstance().showTicketPayList(mData, option);
                                            }
                                        };
                                        UiData.TTime currentTime = NativeData.getMilleServerTime();
                                        long currentTimeSS = currentTime.uint64Time / 1000;
                                        long expirationTimeL = Long.valueOf(bean.expirationTime);
                                        long outTime =  expirationTimeL - currentTimeSS;
                                        QiWuTicketReminderView.showPushView(viewData, onClickTitle, outTime);
                                    }
                                });

                                return;
                            }else if(!TicketWaitingDialog.getInstance().isShowing()){

                                AppLogicBase.runOnUiGround(new Runnable() {
                                    @Override
                                    public void run() {
                                        final QiWuTicketReminderView.Data viewData = new QiWuTicketReminderView.Data("flight_ord_icon","你有机票订单待支付", "关闭");
                                        final QiWuTicketReminderView.OnClickTitle onClickTitle = new QiWuTicketReminderView.OnClickTitle() {
                                            @Override
                                            public void onClickTitle() {
                                                QiwuTrainTicketPayViewData mData = new QiwuTrainTicketPayViewData();
                                                mData.mTicketBeans.add(bean);
                                                ChoiceManager.getInstance().showTicketPayList(mData, option);
                                            }
                                        };
                                        UiData.TTime currentTime = NativeData.getMilleServerTime();
                                        long currentTimeSS = currentTime.uint64Time / 1000;
                                        long expirationTimeL = Long.valueOf(bean.expirationTime);
                                        long outTime =  expirationTimeL - currentTimeSS;
                                        QiWuTicketReminderView.showPushView(viewData, onClickTitle , outTime);
                                    }
                                });
                                return;
                            }
                        }else if(action == 7){
                            bean.payType = "flightPayed";
                            String taskId = orderJson.getString("order_unique_id");
                            if(!taskId.equals(currentSelectIndexId)){
                                return;
                            }
                            option.setTtsText("您已成功预订从"+bean.departureDate.split("-")[0]+"月"+bean.departureDate.split("-")[1]+"日"+bean.station+"飞往"+bean.endStation+"的机票，祝您出行愉快！");
                        }else if(action == 8){
                            bean.payType = "flightREFUND";
                            long taskId = orderJson.getLong("order_unique_id");
                            if(!QiWuTicketCancellingDialog.getInstance().isShowing()){
                                return;
                            }else {
                                AppLogicBase.runOnUiGround(new Runnable() {
                                    @Override
                                    public void run() {
                                        QiWuTicketCancellingDialog.getInstance().dismiss();
                                    }
                                });

                            }
                            option.setTtsText("您已成功退订从"+bean.departureDate.split("-")[0]+"月"+bean.departureDate.split("-")[1]+"日"+bean.station+"飞往"+bean.endStation+"的机票。");
                        }
                        mData.mTicketBeans.add(bean);
                    }
                    showTicketPayList(mData, option);
                    break;
                case 6:break;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void showTicketPayList(QiwuTrainTicketPayViewData mData, final CompentOption<QiwuTrainTicketPayViewData.TicketPayBean> option){
        ChoiceManager.getInstance().showTicketPayList(mData, option);
        TicketUseInfoDialog.getInstance().removeAfterDissmiss();
        AppLogicBase.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                TicketUseInfoDialog.getInstance().dismiss();
                TicketWaitingDialog.getInstance().dismiss();
                QiWuTicketCancellingDialog.getInstance().dismiss();;
            }
        });
    }

    private QiwuTrainTicketPayViewData.TrainTicketPayBean getTrainBean(JSONObject orderJson) throws JSONException {
        final QiwuTrainTicketPayViewData.TrainTicketPayBean bean = new QiwuTrainTicketPayViewData.TrainTicketPayBean();
        String ordTime = orderJson.getString("order_time");
        bean.orderTime = ordTime;
        String orderId = orderJson.getString("order_id");
        bean.orderId = orderId;
        String sonAccount = orderJson.getString("son_account");
        bean.sonAccount = sonAccount;
        String passengerName = orderJson.getString("passenger_name");
        bean.passengerName = passengerName;
        String departTime = orderJson.getString("depart_at");
        departTime = departTime.split(" ")[1];
        bean.departureTime = departTime.split(":")[0]+":"+departTime.split(":")[1];
        String ticketNo = orderJson.getString("train_no");
        bean.ticketNo = ticketNo;
        String departCity = orderJson.getString("depart_city");
        bean.station = departCity;
        String arriveCity = orderJson.getString("arrive_city");
        bean.endStation = arriveCity;
        String departDate = orderJson.getString("train_at");
        bean.departureDate = departDate.substring(4,6)+"-"+departDate.substring(6,8);
        String costTime = orderJson.getString("run_times");
        bean.costTime = costTime;
        String seat =  orderJson.getString("seat_type");
        bean.seat = seat;
        String ticketPrice = orderJson.getString("order_price");
        bean.price = ticketPrice;
        String idNumber = orderJson.getString("passenger_id_number");
        bean.idNumber = idNumber;
        String phone = orderJson.getString("phone");
        bean.phoneNum = phone;
        String payUrlZFB = orderJson.getString("zfb_qr_code");
        bean.payUrlZFB = payUrlZFB;
        String payUrlWX = orderJson.getString("wx_qr_code");
        bean.parUrlWX = payUrlWX;
        String orderUniqueId = orderJson.getString("order_unique_id");
        bean.orderUniqueId = orderUniqueId;
        String expirationTime = orderJson.getString("expiration_time");//订单过期时间戳,单位：秒
        bean.expirationTime = expirationTime;
        String passengeId = orderJson.getString("passenger_id");
        bean.passengeId = passengeId;
        boolean canRefund = orderJson.getInt("is_can_refund") == 1;
        bean.canRefund = canRefund;
        return bean;
    }

    private QiwuTrainTicketPayViewData.FlightTicketPayBean getFightBean(JSONObject orderJson) throws JSONException {
        final QiwuTrainTicketPayViewData.FlightTicketPayBean bean = new QiwuTrainTicketPayViewData.FlightTicketPayBean();
        String ordTime = orderJson.getString("order_time");
        bean.orderTime = ordTime;
        String orderId = orderJson.getString("order_id");
        bean.orderId = orderId;
        String sonAccount = orderJson.getString("son_account");
        bean.sonAccount = sonAccount;
        String passengerName = orderJson.getString("passenger_name");
        bean.passengerName = passengerName;
        String flightNo = orderJson.getString("flight_no");
        bean.ticketNo = flightNo;
        String departCity = orderJson.getString("depart_city");
        bean.station = departCity;
        String arriveCity = orderJson.getString("arrive_city");
        bean.endStation = arriveCity;
        String departDate = orderJson.getString("depart_date");
        bean.departureDate = departDate.split("-")[1]+"-"+departDate.split("-")[2];
        String departTime = orderJson.getString("depart_time");
        bean.departureTime = departTime.split(":")[0]+":"+departTime.split(":")[1];
        String jiJianPrice =  orderJson.getString("ji_jian_price");
        String fuelSurcharge = orderJson.getString("ran_you_price");
        float extraPrice = Float.valueOf(jiJianPrice) + Float.valueOf(fuelSurcharge);
        bean.fuelSurcharge = String.valueOf(extraPrice);
        String ticketPrice = orderJson.getString("order_price");
        bean.price = ticketPrice;
        String idNumber = orderJson.getString("passenger_id_number");
        bean.idNumber = idNumber;
        String phone = orderJson.getString("phone");
        bean.phoneNum = phone;
        String payUrlZFB = orderJson.getString("zfb_qr_code");
        bean.payUrlZFB = payUrlZFB;
        String payUrlWX = orderJson.getString("wx_qr_code");
        bean.parUrlWX = payUrlWX;
        String orderUniqueId = orderJson.getString("order_unique_id");
        bean.orderUniqueId = orderUniqueId;
        String expirationTime = orderJson.getString("expiration_time");//订单过期时间戳,单位：秒
        bean.expirationTime = expirationTime;
        boolean canRefund = orderJson.getInt("is_can_refund") == 1;
        bean.canRefund = canRefund;
        return bean;
    }


    public boolean parseQiWu(com.alibaba.fastjson.JSONObject json, String strAction){
        ReminderManager.getInstance().setIsNeedClearReminder(true);
        if(!enableQiWuFunc()){
            beClearQIWu = true;
            RecorderWin.speakText(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"),null);
            return true;
        }
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json.toJSONString());
        } catch (JSONException e) {
           return false;
        }
        if("chat".equals(strAction)){
            if(statusOb == null){
                statusOb = new StatusOb(){

                    @Override
                    public void onDismiss() {
                        beClearQIWu = true;
                        isRegister = false;
                        RecorderWin.OBSERVABLE.unregisterObserver(this);
                    }
                };
            }
            if(!statusOb.isRegister){
                RecorderWin.OBSERVABLE.registerObserver(statusOb);
                statusOb.isRegister = true;
            }
            return qiWuChat(jsonObject);
        }else if("train".equals(strAction)){
            return queryTrainTicket(jsonObject);
        }else if("flight".equals(strAction)){
            return queryFlightTicket(jsonObject);
        }else if("qiwu_search_order".equals(strAction)){
            return handleTicketOrd(jsonObject);
        }else if("qiwu_can_cancel_order".equals(strAction)){
            return handleTicketOrd(jsonObject);
        }else if("qiwu_can_refund_order".equals(strAction)){
            return handleTicketOrd(jsonObject);
        }
        return false;
    }

    private boolean handleTicketOrd(JSONObject jsonObject){
        try {
            QiwuTrainTicketPayViewData mData = parseOrdItem(jsonObject);
            String answer = jsonObject.getString("answer");

            if(mData.mTicketBeans.size() <= 0){
                RecorderWin.speakText(answer, null);
                return true;
            }
            TicketOrdTimeComparator ordTimeComparator = new TicketOrdTimeComparator();
            Collections.sort(mData.mTicketBeans,ordTimeComparator);
            CompentOption<QiwuTrainTicketPayViewData.TicketPayBean> option = new CompentOption<QiwuTrainTicketPayViewData.TicketPayBean>();
            option.setTtsText(answer);
            ChoiceManager.getInstance().showTicketPayList(mData, option);
        } catch (JSONException e) {
            LogUtil.d(TAG+"checkOrd fail.");
            return false;
        }
        return true;
    }

    private boolean queryFlightTicket(JSONObject jsonObject) {
        QiWuFlightTicketData fb = QiWuFlightTicketData.objectFromData(jsonObject);
        if(fb == null){
            return false;
        }
        CompentOption<QiWuFlightTicketData.FlightTicketBean> option = new CompentOption<QiWuFlightTicketData.FlightTicketBean>();
        if(fb.mFlightTicketBeans.size() > 0){

        }else {
            RecorderWin.speakText(NativeData.getResString("RS_VOICE_TIPS_QIWU_NO_TICKET"),null);
            return true;
        }
        ChoiceManager.getInstance().showFlightTicketList(fb, option);
        return true;
    }

    private boolean queryTrainTicket(JSONObject jsonObject) {
        QiWuTrainTicketData td = QiWuTrainTicketData.objectFromData(jsonObject);
        if(td == null){
            return false;
        }
        CompentOption<QiWuTrainTicketData.TrainTicketBean> option = new CompentOption<QiWuTrainTicketData.TrainTicketBean>();
        if(td.mTrainTicketBeans.size() > 0){
        }else {
            RecorderWin.speakText(NativeData.getResString("RS_VOICE_TIPS_QIWU_NO_TRAIN"),null);
            return true;
        }
        ChoiceManager.getInstance().showTrainTicketList(td, option);
        return true;
    }



    private boolean qiWuChat(JSONObject extraJson){
        try {
            String answer = extraJson.getString("answer");
            String[] deviceNicks = WakeupManager.getInstance().getDeviceNicks();
            if(deviceNicks != null && deviceNicks.length > 0){
                answer = answer.replaceAll("小悟", deviceNicks[0]);
            }

            RecorderWin.speakText(answer,null);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    JSONObject currentPushJson;

    /**
     * 皮肤包状态回调
     * @param packageName
     * @param command
     * @param data
     * @return
     */
    public byte[] invokeCommand(final String packageName, String command, byte[] data) {
        if("info.commit".equals(command)){
            commitOrd(data);
        }else if("info.cancel".equals(command)){
            return cancelOrd(data);
        }else if("enable".equals(command)){//默认齐悟票务不支持第三方UI适配，若第三方适配了UI后，需要调用SDK打开该功能。
            if(WinManager.getInstance().isSupportNewContent()){
                return null;
            }
            newContent = Boolean.parseBoolean(new String(data));
        }else if("closePushView".equals(command)){//
            mCloseWaitingPayView =  Boolean.parseBoolean(new String(data));
            if(mCloseWaitingPayView){
                QiWuTicketReminderView.closePushView();
            }
         }
        return null;
    }

    private byte[] commitOrd(byte[] data){
        JSONBuilder dataJson = new JSONBuilder(data);
        final JSONObject userInfo;
        try {
            userInfo = new JSONObject(dataJson.getJSONObject().getString("extraString"));
            LogUtil.logd(TAG +userInfo.toString());
            final TicketWaitingDialog.TicketType currentTicketType = Enum.valueOf(TicketWaitingDialog.TicketType.class ,userInfo.getString("ticketType"));
            UiEquipment.Req_NLP pushCmdQRResults = new UiEquipment.Req_NLP();
            JSONObject pushJson = new JSONObject();
            UiData.TTime currentTime = NativeData.getMilleServerTime();
            int UID = NativeData.getUID();
            boolean isRepetitionCommit = false;
            JSONObject pushUserInfo = new JSONObject();
            String name = userInfo.getString("name");
            pushUserInfo.put("userName", name);
            String idNumber = userInfo.getString("id");
            pushUserInfo.put("idNumber", idNumber);
            String phone = userInfo.getString("moNum");
            pushUserInfo.put("phone", phone);
            if(userInfo.has("sonAccount")){
                pushUserInfo.put("sonAccount", userInfo.getString("sonAccount"));
            }
            pushJson.put("userInfo", pushUserInfo);
            switch (currentTicketType){
                case Flight:
                    pushJson.put("ticketType","flight");
                    JSONObject ticketFlightInfo = new JSONObject();
                    String departureCode = userInfo.getJSONObject("ticketInfoJson").getString("departAirportCode");
                    ticketFlightInfo.put("departureCode", departureCode);
                    String departureTime = userInfo.getJSONObject("ticketInfoJson").getString("departureTime");
                    ticketFlightInfo.put("departureTime", departureTime);
                    ticketFlightInfo.put("arriveCode", userInfo.getJSONObject("ticketInfoJson").getString("arrivalAirportCode"));
                    String departureAt = userInfo.getJSONObject("ticketInfoJson").getString("departDate");
                    ticketFlightInfo.put("departureAt", departureAt);
                    String departureNo = userInfo.getJSONObject("ticketInfoJson").getString("ticketNum");
                    ticketFlightInfo.put("departureNo", departureNo);
                    String departureCabin =  userInfo.getString("seatCode");
                    ticketFlightInfo.put("departureCabin", departureCabin);
                    if(currentPushJson != null  && "flight".equals(currentPushJson.getString("ticketType")) && name.equals(currentPushJson.getJSONObject("userInfo").getString("userName")) &&
                            idNumber.equals(currentPushJson.getJSONObject("userInfo").getString("idNumber")) &&
                            phone.equals(currentPushJson.getJSONObject("userInfo").getString("phone"))){
                        if(departureAt.equals(currentPushJson.getJSONObject("ticketInfo").getString("departureAt")) &&
                                departureNo.equals(currentPushJson.getJSONObject("ticketInfo").getString("departureNo")) &&
                                departureCabin.equals(currentPushJson.getJSONObject("ticketInfo").getString("departureCabin"))){
                            isRepetitionCommit = true;
                        }
                    }
                    pushJson.put("ticketInfo", ticketFlightInfo);
                    break;
                case Train:
                    pushJson.put("ticketType","train");
                    JSONObject ticketTrainInfo = new JSONObject();
                    String fromStation =userInfo.getJSONObject("ticketInfoJson").getString("station");
                    ticketTrainInfo.put("fromStation", fromStation);
                    String trainDepartureTime = userInfo.getJSONObject("ticketInfoJson").getString("departureTime");
                    ticketTrainInfo.put("departureTime", trainDepartureTime);
                    String toStation =userInfo.getJSONObject("ticketInfoJson").getString("endStation");
                    ticketTrainInfo.put("toStation", toStation);
                    String trainNo =userInfo.getJSONObject("ticketInfoJson").getString("ticketNum");
                    ticketTrainInfo.put("trainNo", trainNo);
                    String trainAt = userInfo.getJSONObject("ticketInfoJson").getString("departDate").replaceAll("-", "");
                    ticketTrainInfo.put("trainAt", trainAt);
                    String seatClass = userInfo.getString("seatCode");
                    ticketTrainInfo.put("seatClass", seatClass);
                    String ticketPrice = userInfo.getString("seatPrice");
                    ticketTrainInfo.put("ticketPrice", ticketPrice);
                    String queryKey = userInfo.getJSONObject("ticketInfoJson").getString("queryKey");
                    ticketTrainInfo.put("queryKey", queryKey);
                    if(currentPushJson != null  && "train".equals(currentPushJson.getString("ticketType")) && name.equals(currentPushJson.getJSONObject("userInfo").getString("userName")) &&
                            idNumber.equals(currentPushJson.getJSONObject("userInfo").getString("idNumber")) &&
                            phone.equals(currentPushJson.getJSONObject("userInfo").getString("phone"))){
                        if(fromStation.equals(currentPushJson.getJSONObject("ticketInfo").getString("fromStation")) &&
                                toStation.equals(currentPushJson.getJSONObject("ticketInfo").getString("toStation")) &&
                                trainNo.equals(currentPushJson.getJSONObject("ticketInfo").getString("trainNo")) &&
                                seatClass.equals(currentPushJson.getJSONObject("ticketInfo").getString("seatClass"))){
                            isRepetitionCommit = true;
                        }
                    }
                    pushJson.put("ticketInfo", ticketTrainInfo);
                    break;
            }
            String currentPushId = ""+UID + currentTime.uint64Time / 1000;
            pushJson.put("orderTime",  currentTime.uint64Time / 1000);
            if(isRepetitionCommit){
                pushJson.put("orderUniqueId", currentPushJson.getString("orderUniqueId"));
            }else {
                pushJson.put("orderUniqueId", currentPushId);
            }
            currentSelectIndexId =pushJson.getString("orderUniqueId");
            TicketUseInfoDialog.getInstance().setCurrentSelectIndexId(currentSelectIndexId);
            currentPushJson = pushJson;
            pushCmdQRResults.strJson = pushJson.toString().getBytes();
            JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_QIWU_ORDER, pushCmdQRResults);
            AppLogicBase.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    TicketWaitingDialog.getInstance().setTicketType(currentTicketType);
                    TicketWaitingDialog.getInstance().setUserInfo(userInfo);
                    TicketWaitingDialog.getInstance().show();
                }
            });

        } catch (JSONException e) {
            LogUtil.logd(TAG +"userInfo commit exception");
            return null;
        }
        return null;
    }

    private byte[] cancelOrd(byte[] data){
        JSONBuilder dataJson = new JSONBuilder(data);
        final JSONObject ticketInfo;
        TtsManager.getInstance().cancelSpeak(QiWuTicketManager.mSpeechTaskId);
        AppLogic.removeBackGroundCallback(QiWuTicketManager.mPayTip);
        try {
            ticketInfo = new JSONObject(dataJson.getJSONObject().getString("extraString"));
            String canceType = ticketInfo.getString("type");
            String orderId = ticketInfo.getString("orderId");
            String sonAccount = ticketInfo.getString("sonAccount");
            String phone = ticketInfo.getString("phoneNum");
            String passengerIdNumber = ticketInfo.getString("idNumber");
            String passengerName = ticketInfo.getString("passengerName");
            String orderUniqueId = ticketInfo.getString("orderUniqueId");
            String passengerId = "";
            String ticketType = "";
            boolean canRefund = ticketInfo.getBoolean("canRefund");
            if(canceType.contains("flight")){
                ticketType = "flight";
            }else {
                ticketType = "train";
                passengerId = ticketInfo.getString("passengerId");
            }
            final UiEquipment.Req_NLP pushCmdQRResults = new UiEquipment.Req_NLP();
            final JSONObject reqJson = new JSONObject();
            reqJson.put("ticketType", ticketType);
            reqJson.put("orderId", orderId);
            reqJson.put("sonAccount", sonAccount);
            reqJson.put("passengerId", passengerId);
            reqJson.put("phone", phone);
            reqJson.put("passengerIdNumber", passengerIdNumber);
            reqJson.put("passengerName", passengerName);
            reqJson.put("orderUniqueId", orderUniqueId);
            pushCmdQRResults.strJson = reqJson.toString().getBytes();
            currentSelectIndexId =reqJson.getString("orderUniqueId");
            if(canceType.contains("Success")){
                if(!canRefund){
                    showNoRefundDialog();
                    return null;
                }
                showCancelOrdDialog(reqJson, pushCmdQRResults);
            }else {
                showRefundOrdDialog(reqJson, pushCmdQRResults);
            }
        } catch (JSONException e) {
            return null;
        }
        return null;
    }

    public boolean isCancel(){
        if(noRefundDialog != null){
            if(noRefundDialog.isShowing()){
                return true;
            }
        }
        if(cancelOrdDialog != null){
            if(cancelOrdDialog.isShowing()){
                return true;
            }
        }
        if(refundOrdDialog != null){
            if(refundOrdDialog.isShowing()){
                return true;
            }
        }
        return false;
    }

    private QiWuTicketConfirmDialog noRefundDialog;

    private void showNoRefundDialog(){
        if(noRefundDialog == null){
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    noRefundDialog = new QiWuTicketConfirmDialog();
                    noRefundDialog.setNegativeText(NativeData.getResString("RS_VOICE_TIPS_QIWU_I_KNOW"));
                    noRefundDialog.setTitle(NativeData.getResString("RS_VOICE_TIPS_QIWU_DIA_TITLE"));
                    noRefundDialog.setMessage(NativeData.getResString("RS_VOICE_TIPS_QIWU_NO_REFUND"));
                    noRefundDialog.show();
                }
            });
        }else{
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    noRefundDialog.show();
                }
            });
        }
    }

    private QiWuTicketConfirmDialog cancelOrdDialog;

    private void showCancelOrdDialog(final JSONObject reqJson, final UiEquipment.Req_NLP pushCmdQRResults){
        if(refundOrdDialog == null){
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    refundOrdDialog = new QiWuTicketConfirmDialog();
                    QiWuTicketConfirmDialog.OnClickTask onClickTask = new QiWuTicketConfirmDialog.OnClickTask() {
                        @Override
                        public void onClickAction() {
                            LogUtil.logd(TAG +"提交退票："+reqJson.toString());
                            JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_QIWU_REFUND_ORDER, pushCmdQRResults);
                            QiWuTicketCancellingDialog.getInstance().setIcon(getDrawable("qiwu_ticket_return_a_ticket"));
                            QiWuTicketCancellingDialog.getInstance().setPrompt(NativeData.getResString("RS_VOICE_TIPS_QIWU_REFUND_WAIT"));
                            AppLogicBase.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    QiWuTicketCancellingDialog.getInstance().show();
                                }
                            });
                            refundOrdDialog.dismiss();
                        }
                    };
                    refundOrdDialog.setonClickTask(onClickTask);
                    refundOrdDialog.setTitle(NativeData.getResString("RS_VOICE_TIPS_QIWU_DIA_TITLE"));
                    refundOrdDialog.setMessage(NativeData.getResString("RS_VOICE_TIPS_QIWU_REFUND"));
                    refundOrdDialog.setNegativeText("关闭");
                    refundOrdDialog.setPositiveText("确定");
                    refundOrdDialog.show();
                }
            });
        }else {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    QiWuTicketConfirmDialog.OnClickTask onClickTask = new QiWuTicketConfirmDialog.OnClickTask() {
                        @Override
                        public void onClickAction() {
                            LogUtil.logd(TAG +"提交退票："+reqJson.toString());
                            JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_QIWU_REFUND_ORDER, pushCmdQRResults);
                            QiWuTicketCancellingDialog.getInstance().setIcon(getDrawable("qiwu_ticket_return_a_ticket"));
                            QiWuTicketCancellingDialog.getInstance().setPrompt(NativeData.getResString("RS_VOICE_TIPS_QIWU_REFUND_WAIT"));
                            AppLogicBase.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    QiWuTicketCancellingDialog.getInstance().show();
                                }
                            });
                            refundOrdDialog.dismiss();
                        }
                    };
                    refundOrdDialog.setonClickTask(onClickTask);
                    refundOrdDialog.show();
                }
            });

        }
    }

    private QiWuTicketConfirmDialog refundOrdDialog;

    private void showRefundOrdDialog(final JSONObject reqJson, final UiEquipment.Req_NLP pushCmdQRResults){
        if(cancelOrdDialog == null){
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    cancelOrdDialog = new QiWuTicketConfirmDialog();
                    QiWuTicketConfirmDialog.OnClickTask onClickTask = new QiWuTicketConfirmDialog.OnClickTask() {
                        @Override
                        public void onClickAction() {
                            LogUtil.logd(TAG +"取消订单："+reqJson.toString());
                            JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_QIWU_CANCEL_ORDER, pushCmdQRResults);
                            QiWuTicketCancellingDialog.getInstance().setIcon(getDrawable("qiwu_ticket_cancelling_order"));
                            QiWuTicketCancellingDialog.getInstance().setPrompt(NativeData.getResString("RS_VOICE_TIPS_QIWU_CANCEL_WAIT"));
                            AppLogicBase.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    QiWuTicketCancellingDialog.getInstance().show();
                                }
                            });
                            AppLogic.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    cancelOrdDialog.dismiss();
                                }
                            });

                        }
                    };
                    cancelOrdDialog.setonClickTask(onClickTask);
                    cancelOrdDialog.setTitle(NativeData.getResString("RS_VOICE_TIPS_QIWU_DIA_TITLE"));
                    cancelOrdDialog.setMessage(NativeData.getResString("RS_VOICE_TIPS_QIWU_CANCEL"));
                    cancelOrdDialog.setNegativeText("关闭");
                    cancelOrdDialog.setPositiveText("确定");
                    cancelOrdDialog.show();
                }
            });

        }else {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    QiWuTicketConfirmDialog.OnClickTask onClickTask = new QiWuTicketConfirmDialog.OnClickTask() {
                        @Override
                        public void onClickAction() {
                            LogUtil.logd(TAG +"取消订单："+reqJson.toString());
                            JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_QIWU_CANCEL_ORDER, pushCmdQRResults);
                            QiWuTicketCancellingDialog.getInstance().setIcon(getDrawable("qiwu_ticket_cancelling_order"));
                            QiWuTicketCancellingDialog.getInstance().setPrompt(NativeData.getResString("RS_VOICE_TIPS_QIWU_CANCEL_WAIT"));
                            AppLogicBase.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    QiWuTicketCancellingDialog.getInstance().show();
                                }
                            });
                            AppLogic.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    cancelOrdDialog.dismiss();
                                }
                            });

                        }
                    };
                    cancelOrdDialog.setonClickTask(onClickTask);
                    cancelOrdDialog.show();
                }
            });

        }
    }

    private static QiwuTrainTicketPayViewData parseOrdItem(JSONObject jsonObject) {
        QiwuTrainTicketPayViewData qiwuTrainTicketPayViewData = new QiwuTrainTicketPayViewData();
        try {
            jsonObject = jsonObject.getJSONObject("data");
            JSONArray trainJsonArray = jsonObject.getJSONArray("train");
            for(int i = 0; i < trainJsonArray.length(); i++){
                JSONObject trainJson = trainJsonArray.getJSONObject(i);
                QiwuTrainTicketPayViewData.TrainTicketPayBean bean = QiWuTicketManager.getInstance().getTrainBean(trainJson);
                int orderState = trainJson.getInt("order_state");
                switch (orderState){
                    case 0:
                        bean.payType = "train";
                        break;//待支付
                    case 2:
                        bean.payType = "trainSuccess";
                        break;//待出行
                }
                qiwuTrainTicketPayViewData.mTicketBeans.add(bean);
            }
            JSONArray flightJsonArray = jsonObject.getJSONArray("flight");
            for(int i = 0; i < flightJsonArray.length(); i++){
                JSONObject flightJson = flightJsonArray.getJSONObject(i);
                QiwuTrainTicketPayViewData.FlightTicketPayBean bean = QiWuTicketManager.getInstance().getFightBean(flightJson);
                int orderState = flightJson.getInt("order_state");
                switch (orderState){
                    case 0:
                        bean.payType = "flight";
                        break;//待支付
                    case 2:
                        bean.payType = "flightSuccess";
                        break;//待出行
                }
                qiwuTrainTicketPayViewData.mTicketBeans.add(bean);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return qiwuTrainTicketPayViewData;
    }

    public void closeAllDialog(){
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                QiWuTicketCancellingDialog.getInstance().dismiss();
                TicketWaitingDialog.getInstance().dismiss();
                TicketUseInfoDialog.getInstance().setAfterDissmiss(null);
                TicketUseInfoDialog.getInstance().dismiss();
                if(noRefundDialog != null){
                    noRefundDialog.dismiss();
                }
                if(refundOrdDialog != null){
                    refundOrdDialog.dismiss();
                }
                if(cancelOrdDialog != null){
                    cancelOrdDialog.dismiss();
                }
            }
        });
    }



    //功能开关，当条件不满足是不让齐悟订票场景走下去。
    private boolean enableQiWuFunc(){

        if((qiWuControl || newContent) && themeSwitch){
            LogUtil.logd(TAG + "enableFilmFunc true");
            return true;
        }
        LogUtil.logd(TAG + "enableFilmFunc false");
        return false;
    }
}
